from pathlib import Path
import re

BACKEND = Path('functions/v3.js')
BUILD = Path('app/build.gradle')

src = BACKEND.read_text(encoding='utf-8')

if 'async function isBanned(uid)' not in src:
    needle = "function cleanText(value, max = 160) {\n  if (typeof value !== 'string') return '';\n  return value.trim().slice(0, max);\n}\n"
    helper = needle + "\nasync function isBanned(uid) {\n  if (!uid) return true;\n  const snap = await db.collection('bans').doc(uid).get();\n  return snap.exists && snap.get('active') === true;\n}\n\nasync function assertActiveUser(uid) {\n  if (await isBanned(uid)) {\n    throw new HttpsError('permission-denied', 'This account is restricted.');\n  }\n}\n\nasync function assertReceivableUser(uid) {\n  if (await isBanned(uid)) {\n    throw new HttpsError('failed-precondition', 'Target account is restricted.');\n  }\n}\n"
    src = src.replace(needle, helper)

# Server-authoritative gift: deny banned senders and banned recipients.
gift_needle = "  if (!targetUid || targetUid === senderUid) throw new HttpsError('invalid-argument', 'Choose another user.');\n  if (!Number.isInteger(cost) || cost < 1 || cost > 100000) throw new HttpsError('invalid-argument', 'Invalid gift cost.');\n"
if 'await assertActiveUser(senderUid);' not in src.split('const secureSendGift', 1)[1].split('const verifyPlayPurchase', 1)[0]:
    src = src.replace(gift_needle, gift_needle + "  await assertActiveUser(senderUid);\n  await assertReceivableUser(targetUid);\n", 1)

# Add a hardened room invite callable and override the legacy base export.
if 'const secureSendRoomInvite = onCall' not in src:
    marker = "const sendDirectMessageNotification = onCall(async request => {"
    room_fn = "const secureSendRoomInvite = onCall(async request => {\n  const senderUid = requireAuth(request);\n  const targetUid = cleanText(request.data && request.data.targetUid, 160);\n  const roomId = cleanText(request.data && request.data.roomId, 160);\n  const roomName = cleanText(request.data && request.data.roomName, 100) || 'KING Plus room';\n  if (!targetUid || !roomId || targetUid === senderUid) {\n    throw new HttpsError('invalid-argument', 'Target user and room are required.');\n  }\n  await assertActiveUser(senderUid);\n  await assertReceivableUser(targetUid);\n  const result = await sendUserNotification(targetUid, 'Room invitation', roomName, {\n    type: 'room_invite', roomId, senderUid\n  });\n  return { ok: true, ...result, message: result.sent ? 'Room invite push sent.' : 'Invite saved; no active push token found.' };\n});\n\n"
    src = src.replace(marker, room_fn + marker)

# Protect direct-message and follow notification callables.
dm_needle = "  if (!targetUid || targetUid === senderUid) throw new HttpsError('invalid-argument', 'Target user required.');\n  const result = await sendUserNotification(targetUid, senderName, preview, { type: 'direct_message', senderUid });\n"
if "await assertActiveUser(senderUid);\n  await assertReceivableUser(targetUid);\n  const result = await sendUserNotification(targetUid, senderName" not in src:
    src = src.replace(dm_needle, "  if (!targetUid || targetUid === senderUid) throw new HttpsError('invalid-argument', 'Target user required.');\n  await assertActiveUser(senderUid);\n  await assertReceivableUser(targetUid);\n  const result = await sendUserNotification(targetUid, senderName, preview, { type: 'direct_message', senderUid });\n")

follow_needle = "  if (!targetUid || targetUid === followerUid) throw new HttpsError('invalid-argument', 'Target user required.');\n  const result = await sendUserNotification(targetUid, 'New follower', `${followerName} followed you`, { type: 'follow', followerUid });\n"
if "await assertActiveUser(followerUid);\n  await assertReceivableUser(targetUid);" not in src:
    src = src.replace(follow_needle, "  if (!targetUid || targetUid === followerUid) throw new HttpsError('invalid-argument', 'Target user required.');\n  await assertActiveUser(followerUid);\n  await assertReceivableUser(targetUid);\n  const result = await sendUserNotification(targetUid, 'New follower', `${followerName} followed you`, { type: 'follow', followerUid });\n")

# Harden notification triggers too. Firestore rules already block banned client writes,
# but this prevents server-created records from notifying on behalf of a restricted sender.
if "if (await isBanned(senderUid)) return;" not in src:
    src = src.replace("  if (!senderUid || !targetUid || senderUid === targetUid) return;\n  const senderName = cleanText(message.senderName, 60) || 'KING user';", "  if (!senderUid || !targetUid || senderUid === targetUid) return;\n  if (await isBanned(senderUid) || await isBanned(targetUid)) return;\n  const senderName = cleanText(message.senderName, 60) || 'KING user';")
if "if (await isBanned(followerUid) || await isBanned(targetUid)) return;" not in src:
    src = src.replace("  if (!followerUid || !targetUid || followerUid === targetUid) return;\n  const followerName = cleanText(follow.followerName, 60) || 'KING user';", "  if (!followerUid || !targetUid || followerUid === targetUid) return;\n  if (await isBanned(followerUid) || await isBanned(targetUid)) return;\n  const followerName = cleanText(follow.followerName, 60) || 'KING user';")

# Override base room invite export with the hardened callable.
if 'sendRoomInvite: secureSendRoomInvite' not in src:
    src = src.replace("  sendGift: secureSendGift,\n", "  sendGift: secureSendGift,\n  sendRoomInvite: secureSendRoomInvite,\n")

BACKEND.write_text(src, encoding='utf-8')

gradle = BUILD.read_text(encoding='utf-8')
gradle = re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 41; versionName '3.0.1'", gradle)
BUILD.write_text(gradle, encoding='utf-8')

print('Prepared KING Plus v3.0.1 ban enforcement + backend abuse hardening')
