const { onCall, HttpsError } = require('firebase-functions/v2/https');
const { onDocumentCreated } = require('firebase-functions/v2/firestore');
const { initializeApp } = require('firebase-admin/app');
const { getFirestore, FieldValue } = require('firebase-admin/firestore');
const { getMessaging } = require('firebase-admin/messaging');

initializeApp();
const db = getFirestore();

function requireAuth(request) {
  if (!request.auth) throw new HttpsError('unauthenticated', 'Sign in required.');
  return request.auth.uid;
}

function requireAdmin(request) {
  const uid = requireAuth(request);
  if (!request.auth.token || request.auth.token.admin !== true) {
    throw new HttpsError('permission-denied', 'Admin access required.');
  }
  return uid;
}

function cleanText(value, max = 120) {
  if (typeof value !== 'string') return '';
  return value.trim().slice(0, max);
}

async function tokensForUser(uid) {
  const snap = await db.collection('users').doc(uid).collection('devices').limit(50).get();
  const tokens = [];
  snap.forEach(doc => {
    const token = doc.get('token') || doc.id;
    if (typeof token === 'string' && token.length > 20) tokens.push(token);
  });
  return [...new Set(tokens)];
}

async function sendUserNotification(uid, title, body, data = {}) {
  const tokens = await tokensForUser(uid);
  const payload = { ...data, title, body };
  await db.collection('notifications').doc(uid).collection('items').add({
    title, body, data, read: false, createdAt: FieldValue.serverTimestamp()
  });
  if (!tokens.length) return { sent: 0 };
  const response = await getMessaging().sendEachForMulticast({
    tokens,
    data: Object.fromEntries(Object.entries(payload).map(([k,v]) => [k, String(v)]))
  });
  return { sent: response.successCount, failed: response.failureCount };
}

exports.sendGift = onCall(async request => {
  const senderUid = requireAuth(request);
  const targetUid = cleanText(request.data && request.data.targetUid, 160);
  const giftName = cleanText(request.data && request.data.giftName, 60) || 'Gift';
  const cost = Number(request.data && request.data.cost);
  if (!targetUid || targetUid === senderUid) throw new HttpsError('invalid-argument', 'Choose another user.');
  if (!Number.isInteger(cost) || cost < 1 || cost > 100000) throw new HttpsError('invalid-argument', 'Invalid gift cost.');

  const senderRef = db.collection('wallets').doc(senderUid);
  const receiverRef = db.collection('wallets').doc(targetUid);
  const ledgerRef = db.collection('wallet_ledger').doc();
  let senderBalance = 0;

  await db.runTransaction(async tx => {
    const [senderSnap, receiverSnap] = await Promise.all([tx.get(senderRef), tx.get(receiverRef)]);
    const senderCoins = senderSnap.exists ? Number(senderSnap.get('coins') || 0) : 0;
    const receiverCoins = receiverSnap.exists ? Number(receiverSnap.get('coins') || 0) : 0;
    if (senderCoins < cost) throw new HttpsError('failed-precondition', 'Not enough server coins.');
    senderBalance = senderCoins - cost;
    tx.set(senderRef, { coins: senderBalance, updatedAt: FieldValue.serverTimestamp() }, { merge: true });
    tx.set(receiverRef, { coins: receiverCoins + cost, updatedAt: FieldValue.serverTimestamp() }, { merge: true });
    tx.set(ledgerRef, {
      type: 'gift', senderUid, targetUid, giftName, cost,
      createdAt: FieldValue.serverTimestamp()
    });
  });

  await sendUserNotification(targetUid, 'You received a gift', `${giftName} • ${cost} coins`, { type: 'gift', senderUid });
  return { ok: true, balance: senderBalance, message: `${giftName} sent securely. Balance: ${senderBalance}` };
});

exports.sendRoomInvite = onCall(async request => {
  const senderUid = requireAuth(request);
  const targetUid = cleanText(request.data && request.data.targetUid, 160);
  const roomId = cleanText(request.data && request.data.roomId, 160);
  const roomName = cleanText(request.data && request.data.roomName, 100) || 'KING Plus room';
  if (!targetUid || !roomId) throw new HttpsError('invalid-argument', 'Target UID and room are required.');
  const result = await sendUserNotification(targetUid, 'Room invitation', roomName, { type: 'room_invite', roomId, senderUid });
  return { ok: true, ...result, message: result.sent ? 'Room invite push sent.' : 'Invite saved; no active push token found.' };
});

exports.moderateReport = onCall(async request => {
  const adminUid = requireAdmin(request);
  const reportId = cleanText(request.data && request.data.reportId, 160);
  const status = cleanText(request.data && request.data.status, 20);
  if (!reportId || !['resolved','dismissed','reviewing'].includes(status)) throw new HttpsError('invalid-argument', 'Invalid report update.');
  await db.collection('reports').doc(reportId).set({ status, reviewedBy: adminUid, reviewedAt: FieldValue.serverTimestamp() }, { merge: true });
  return { ok: true, message: `Report marked ${status}.` };
});

exports.banUser = onCall(async request => {
  const adminUid = requireAdmin(request);
  const targetUid = cleanText(request.data && request.data.targetUid, 160);
  const reason = cleanText(request.data && request.data.reason, 250) || 'Policy violation';
  if (!targetUid) throw new HttpsError('invalid-argument', 'Target UID required.');
  await db.collection('bans').doc(targetUid).set({ active: true, reason, bannedBy: adminUid, updatedAt: FieldValue.serverTimestamp() }, { merge: true });
  await sendUserNotification(targetUid, 'Account moderation update', 'Your account has been restricted.', { type: 'ban' });
  return { ok: true, message: 'User ban recorded.' };
});

exports.adminAdjustWallet = onCall(async request => {
  const adminUid = requireAdmin(request);
  const targetUid = cleanText(request.data && request.data.targetUid, 160);
  const reason = cleanText(request.data && request.data.reason, 250) || 'Admin adjustment';
  const delta = Number(request.data && request.data.delta);
  if (!targetUid || !Number.isInteger(delta) || delta === 0 || Math.abs(delta) > 1000000) throw new HttpsError('invalid-argument', 'Invalid wallet adjustment.');
  const walletRef = db.collection('wallets').doc(targetUid);
  const ledgerRef = db.collection('wallet_ledger').doc();
  let newBalance = 0;
  await db.runTransaction(async tx => {
    const snap = await tx.get(walletRef);
    const current = snap.exists ? Number(snap.get('coins') || 0) : 0;
    newBalance = current + delta;
    if (newBalance < 0) throw new HttpsError('failed-precondition', 'Adjustment would make balance negative.');
    tx.set(walletRef, { coins: newBalance, updatedAt: FieldValue.serverTimestamp() }, { merge: true });
    tx.set(ledgerRef, { type: 'admin_adjustment', targetUid, delta, reason, adminUid, createdAt: FieldValue.serverTimestamp() });
  });
  return { ok: true, balance: newBalance, message: `Server wallet updated to ${newBalance} coins.` };
});

exports.onRoomMessageCreated = onDocumentCreated('live_rooms/{roomId}/messages/{messageId}', async event => {
  if (!event.data) return;
  const message = event.data.data();
  const roomSnap = await db.collection('live_rooms').doc(event.params.roomId).get();
  if (!roomSnap.exists) return;
  const ownerUid = roomSnap.get('ownerUid');
  const senderUid = message.senderUid;
  if (!ownerUid || ownerUid === senderUid) return;
  const senderName = cleanText(message.senderName, 60) || 'KING user';
  const text = cleanText(message.text, 100);
  await sendUserNotification(ownerUid, `${senderName} in ${roomSnap.get('name') || 'your room'}`, text || 'New message', {
    type: 'room_message', roomId: event.params.roomId, senderUid: senderUid || ''
  });
});

exports.onDirectMessageCreated = onDocumentCreated('direct_threads/{threadId}/messages/{messageId}', async event => {
  if (!event.data) return;
  const message = event.data.data();
  const senderUid = cleanText(message.senderUid, 160);
  const recipientUid = cleanText(message.recipientUid, 160);
  if (!senderUid || !recipientUid || senderUid === recipientUid) return;
  const senderName = cleanText(message.senderName, 60) || 'KING User';
  const preview = cleanText(message.text, 120) || 'New private message';
  await sendUserNotification(recipientUid, senderName, preview, {
    type: 'direct_message',
    threadId: event.params.threadId,
    senderUid,
    senderName
  });
});
