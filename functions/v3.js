const base = require('./index');
const crypto = require('crypto');
const { onCall, HttpsError } = require('firebase-functions/v2/https');
const { defineSecret } = require('firebase-functions/params');
const { onDocumentCreated } = require('firebase-functions/v2/firestore');
const { getFirestore, FieldValue } = require('firebase-admin/firestore');
const { getMessaging } = require('firebase-admin/messaging');
const { google } = require('googleapis');

const db = getFirestore();
const PACKAGE_NAME = 'com.kingplus.social';
const PRODUCT_COINS = Object.freeze({
  king_coins_100: 100,
  king_coins_600: 600,
  king_coins_1300: 1300,
});

function requireAuth(request) {
  if (!request.auth) throw new HttpsError('unauthenticated', 'Sign in required.');
  return request.auth.uid;
}

function cleanText(value, max = 160) {
  if (typeof value !== 'string') return '';
  return value.trim().slice(0, max);
}

async function isBanned(uid) {
  if (!uid) return true;
  const snap = await db.collection('bans').doc(uid).get();
  return snap.exists && snap.get('active') === true;
}

async function assertActiveUser(uid) {
  if (await isBanned(uid)) {
    throw new HttpsError('permission-denied', 'This account is restricted.');
  }
}

async function assertReceivableUser(uid) {
  if (await isBanned(uid)) {
    throw new HttpsError('failed-precondition', 'Target account is restricted.');
  }
}

async function isBlockedEither(uidA, uidB) {
  if (!uidA || !uidB) return true;
  const [a, b] = await Promise.all([
    db.collection('blocks').doc(`${uidA}_${uidB}`).get(),
    db.collection('blocks').doc(`${uidB}_${uidA}`).get(),
  ]);
  return a.exists || b.exists;
}

async function assertSocialAllowed(uidA, uidB) {
  if (await isBlockedEither(uidA, uidB)) {
    throw new HttpsError('permission-denied', 'This interaction is blocked.');
  }
}

function hash(value) {
  return crypto.createHash('sha256').update(String(value)).digest('hex');
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
  await db.collection('notifications').doc(uid).collection('items').add({
    title, body, data, read: false, createdAt: FieldValue.serverTimestamp()
  });
  const tokens = await tokensForUser(uid);
  if (!tokens.length) return { sent: 0, failed: 0 };

  const response = await getMessaging().sendEachForMulticast({
    tokens,
    notification: { title, body },
    data: Object.fromEntries(Object.entries(data).map(([key, value]) => [key, String(value)]))
  });

  const invalid = [];
  response.responses.forEach((item, index) => {
    const code = item.error && item.error.code ? item.error.code : '';
    if (code === 'messaging/registration-token-not-registered' ||
        code === 'messaging/invalid-registration-token') invalid.push(tokens[index]);
  });
  await Promise.all(invalid.map(token =>
    db.collection('users').doc(uid).collection('devices').doc(token).delete().catch(() => null)
  ));

  return { sent: response.successCount, failed: response.failureCount };
}

const {createGiftService, GiftError} = require('./room-gift-service');
const commitGift10064 = createGiftService({db, timestamp: () => FieldValue.serverTimestamp()});
// VIP 000001 is reserved by a trusted function for a server-verified Super Admin.
const claimOwnerVipNumber = onCall(async request => {
  const uid=requireAuth(request);
  if(request.auth.token?.admin!==true)
    throw new HttpsError('permission-denied','Super Admin access required.');
  const registry=db.collection('king_vip_number_registry').doc('000001');
  const grant=db.collection('king_vip_identity_grants').doc(uid);
  try {
    return await db.runTransaction(async tx=>{
      const [reserved,existing]=await Promise.all([tx.get(registry),tx.get(grant)]);
      if(reserved.exists&&reserved.get('uid')!==uid)
        throw new HttpsError('already-exists','VIP number 000001 is already reserved.');
      if(existing.exists&&existing.get('vipNumber')&&existing.get('vipNumber')!=='000001')
        throw new HttpsError('failed-precondition','This account already has a different VIP number.');
      const now=FieldValue.serverTimestamp();
      tx.set(registry,{uid,number:'000001',reservedForOwner:true,createdAt:now},{merge:true});
      tx.set(grant,{uid,vipNumber:'000001',updatedAt:now},{merge:true});
      return {ok:true,number:'000001'};
    });
  } catch(error) {
    if(error instanceof HttpsError)throw error;
    throw new HttpsError('internal','Owner VIP number could not be verified.');
  }
});
async function purchaseGift10064(request, inRoom) {
  const senderUid=requireAuth(request);
  const data=request.data||{};
  if(inRoom&&(typeof data.roomId!=='string'||!data.roomId.trim()))
    throw new HttpsError('invalid-argument','A live Party room is required.');
  if(request.auth.token?.firebase?.sign_in_provider==='anonymous')
    throw new HttpsError('unauthenticated','Sign in to send Diamond gifts.');
  try {
    const result=await commitGift10064({senderUid,targetUid:data.targetUid,
      giftName:data.giftName,cost:data.cost,requestId:data.requestId,
      roomId:inRoom?data.roomId:null,
      unlimitedSender:request.auth.token?.admin===true});
    if(!result.idempotent) {
      try { await sendUserNotification(data.targetUid,'You received a gift',data.giftName,
        {type:'gift',senderUid,giftId:result.giftId}); }
      catch(error){console.warn('Gift committed; notification unavailable.');}
    }
    return result;
  } catch(error) {
    if(error instanceof GiftError)throw new HttpsError(error.code,error.message);
    throw new HttpsError('internal','Gift confirmation unavailable. Retry the same gift.');
  }
}
const secureSendGift = onCall(request => purchaseGift10064(request,false));
const sendRoomGift = onCall(request => purchaseGift10064(request,true));

// Both flags default OFF. An allowance is not a guaranteed zero-cost spending cap.
// Never enable these without checking the account's billing behavior and backend costs.
const {defineBoolean} = require('firebase-functions/params');
const voiceEnabled10066 = defineBoolean('CLOUDFLARE_VOICE_ENABLED', {default:false});
const voiceZeroCost10066 = defineBoolean('CLOUDFLARE_ZERO_COST_VERIFIED', {default:false});
const voiceApp10066 = defineSecret('CLOUDFLARE_SFU_APP_ID');
const voiceSecret10066 = defineSecret('CLOUDFLARE_SFU_APP_SECRET');
const partyVoice = onCall({secrets:[voiceApp10066,voiceSecret10066], timeoutSeconds:30}, async request => {
  const uid = requireAuth(request);
  const {createFirebaseVoice} = require('./cloudflare-voice-firebase');
  const {VoiceError} = require('./cloudflare-voice');
  const {service} = createFirebaseVoice({db, config:async () => ({
    enabled:voiceEnabled10066.value(), zeroCostVerified:voiceZeroCost10066.value(),
    appId:voiceApp10066.value(), secret:voiceSecret10066.value()
  })});
  try { return await service({uid, data:request.data}); }
  catch(error) {
    if(error instanceof VoiceError) throw new HttpsError(error.code,error.message);
    throw new HttpsError('unavailable','Room voice could not complete. Reconnect voice.');
  }
});
const {onDocumentWritten} = require('firebase-functions/v2/firestore');
function voiceModeration10066(document, byUser=false, fields=[]) {
  return onDocumentWritten({document,secrets:[voiceApp10066,voiceSecret10066],retry:true},async event=>{
    const before=event.data?.before,after=event.data?.after;
    if(before?.exists&&after?.exists&&fields.length&&
        fields.every(key=>JSON.stringify(before.get(key))===JSON.stringify(after.get(key))))return;
    const {createFirebaseVoice} = require('./cloudflare-voice-firebase');
    const voice=createFirebaseVoice({db,config:async()=>({appId:voiceApp10066.value(),secret:voiceSecret10066.value()})});
    return byUser?voice.reconcileUser(event.params.uid):voice.reconcileRoom(event.params.roomId);
  });
}
const voiceRoomChanged10066=voiceModeration10066('live_rooms/{roomId}',false,['closed','muteAll','ownerUid','maxSeats']);
const voiceSeatChanged10066=voiceModeration10066('live_rooms/{roomId}/seats/{seatId}',false,['uid','micOn']);
const voiceMemberChanged10066=voiceModeration10066('live_rooms/{roomId}/members/{uid}',true,['uid','sessionId']);
const voiceRoomBanChanged10066=voiceModeration10066('live_rooms/{roomId}/room_bans/{uid}',true,['active']);
const voiceAccountBanChanged10066=voiceModeration10066('bans/{uid}',true,['active']);
const voiceRoleChanged10066=voiceModeration10066('live_rooms/{roomId}/roles/{uid}',true,['role']);

const verifyPlayPurchase = onCall(async request => {
  const uid = requireAuth(request);
  if (request.auth.token?.firebase?.sign_in_provider === 'anonymous')
    throw new HttpsError('unauthenticated', 'Sign in to a permanent account before recharge.');
  const {createPurchaseService, PurchaseError} = require('./play-purchase-service');
  const auth = new google.auth.GoogleAuth({scopes: ['https://www.googleapis.com/auth/androidpublisher']});
  const publisher = google.androidpublisher({version: 'v3', auth});
  const service = createPurchaseService({
    db, timestamp: () => FieldValue.serverTimestamp(),
    getPurchase: async (productId, token) => (await publisher.purchases.products.get({packageName: PACKAGE_NAME, productId, token})).data,
    consumePurchase: (productId, token) => publisher.purchases.products.consume({packageName: PACKAGE_NAME, productId, token}),
    notify: (target, coins, productId) => sendUserNotification(target, 'Recharge complete', `${coins} Diamonds added`, {type: 'play_purchase', productId})
  });
  try {
    return await service({uid, productId: request.data?.productId, purchaseToken: request.data?.purchaseToken});
  } catch (error) {
    if (error instanceof PurchaseError) throw new HttpsError(error.code, error.message);
    // Do not log Google API error objects: they can contain purchase tokens.
    throw new HttpsError('internal', 'Recharge verification could not finish. Retry later.');
  }
});

const secureSendRoomInvite = onCall(async request => {
  const senderUid = requireAuth(request);
  const targetUid = cleanText(request.data && request.data.targetUid, 160);
  const roomId = cleanText(request.data && request.data.roomId, 160);
  const roomName = cleanText(request.data && request.data.roomName, 100) || 'KING Plus room';
  if (!targetUid || !roomId || targetUid === senderUid) {
    throw new HttpsError('invalid-argument', 'Target user and room are required.');
  }
  await assertActiveUser(senderUid);
  await assertReceivableUser(targetUid);
  await assertSocialAllowed(senderUid, targetUid);
  const result = await sendUserNotification(targetUid, 'Room invitation', roomName, {
    type: 'room_invite', roomId, senderUid
  });
  return { ok: true, ...result, message: result.sent ? 'Room invite push sent.' : 'Invite saved; no active push token found.' };
});

const sendDirectMessageNotification = onCall(async request => {
  const senderUid = requireAuth(request);
  const targetUid = cleanText(request.data && request.data.targetUid, 160);
  const senderName = cleanText(request.data && request.data.senderName, 60) || 'KING user';
  const preview = cleanText(request.data && request.data.preview, 100) || 'New message';
  if (!targetUid || targetUid === senderUid) throw new HttpsError('invalid-argument', 'Target user required.');
  await assertActiveUser(senderUid);
  await assertReceivableUser(targetUid);
  await assertSocialAllowed(senderUid, targetUid);
  const result = await sendUserNotification(targetUid, senderName, preview, { type: 'direct_message', senderUid });
  return { ok: true, ...result, message: result.sent ? 'Message push sent.' : 'Message saved; no active push token found.' };
});

const sendFollowNotification = onCall(async request => {
  const followerUid = requireAuth(request);
  const targetUid = cleanText(request.data && request.data.targetUid, 160);
  const followerName = cleanText(request.data && request.data.followerName, 60) || 'KING user';
  if (!targetUid || targetUid === followerUid) throw new HttpsError('invalid-argument', 'Target user required.');
  await assertActiveUser(followerUid);
  await assertReceivableUser(targetUid);
  await assertSocialAllowed(followerUid, targetUid);
  const result = await sendUserNotification(targetUid, 'New follower', `${followerName} followed you`, { type: 'follow', followerUid });
  return { ok: true, ...result, message: result.sent ? 'Follow push sent.' : 'Follow notification saved.' };
});

const requestAccountDeletion = onCall(async request => {
  const uid = requireAuth(request);
  await db.collection('account_deletion_requests').doc(uid).set({
    uid, status: 'requested', requestedAt: FieldValue.serverTimestamp()
  }, { merge: true });
  return { ok: true, message: 'Account deletion request recorded. Sign out after saving anything you need.' };
});

const onDirectMessageCreated = onDocumentCreated('direct_threads/{threadId}/messages/{messageId}', async event => {
  if (!event.data) return;
  const message = event.data.data();
  const senderUid = cleanText(message.senderUid, 160);
  const targetUid = cleanText(message.recipientUid, 160);
  if (!senderUid || !targetUid || senderUid === targetUid) return;
  if (await isBanned(senderUid) || await isBanned(targetUid) || await isBlockedEither(senderUid, targetUid)) return;
  const senderName = cleanText(message.senderName, 60) || 'KING user';
  const preview = cleanText(message.text, 100) || 'New message';
  await sendUserNotification(targetUid, senderName, preview, {
    type: 'direct_message', senderUid, threadId: event.params.threadId
  });
});

const onFollowCreated = onDocumentCreated('follows/{followId}', async event => {
  if (!event.data) return;
  const follow = event.data.data();
  const followerUid = cleanText(follow.followerUid, 160);
  const targetUid = cleanText(follow.targetUid, 160);
  if (!followerUid || !targetUid || followerUid === targetUid) return;
  if (await isBanned(followerUid) || await isBanned(targetUid) || await isBlockedEither(followerUid, targetUid)) return;
  const followerName = cleanText(follow.followerName, 60) || 'KING user';
  await sendUserNotification(targetUid, 'New follower', `${followerName} followed you`, { type: 'follow', followerUid });
});

// Cashfree is an optional zero-upfront merchant integration, never active by default.
// Merchant must do their own KYC, create server secrets and explicitly enable a
// permitted distribution/billing mode. No API keys ship in an APK or Git.
const cashfreeClientId = defineSecret('KING_CASHFREE_CLIENT_ID');
const cashfreeClientSecret = defineSecret('KING_CASHFREE_CLIENT_SECRET');
function cashfreeService(){
  const mode = process.env.KING_CASHFREE_MODE;
  const id = cashfreeClientId.value();
  const secret = cashfreeClientSecret.value();
  // Explicit opt-in: credentials alone MUST NEVER enable real money charging.
  // Production also requires merchant confirmation of the distribution policy.
  const enabled=require('./cashfree-recharge-config').paymentEnabled({
    mode,hasId:!!id,hasSecret:!!secret,
    enabled:process.env.KING_CASHFREE_ENABLED,
    billingApproved:process.env.KING_CASHFREE_BILLING_APPROVED
  });
  const provider = enabled
    ? require('./cashfree-recharge-provider').createProvider({clientId:id,clientSecret:secret,mode})
    : null;
  return require('./cashfree-recharge-service').createCashfreeRechargeService({
    db, provider, enabled, mode, isBanned,
    stamp:()=>FieldValue.serverTimestamp()
  });
}
function cashfreeUser(request){
  const uid=requireAuth(request);
  if(request.auth.token?.firebase?.sign_in_provider==='anonymous')
    throw new HttpsError('unauthenticated','Permanent account required for recharge.');
  return uid;
}
async function cashfreeCall(request,action){
  const uid=cashfreeUser(request);
  try {return await action(cashfreeService(),uid);}
  catch(error){
    if(error instanceof HttpsError)throw error;
    const codes=['unauthenticated','permission-denied','invalid-argument','failed-precondition','unavailable'];
    throw new HttpsError(codes.includes(error.code)?error.code:'internal',
      codes.includes(error.code)?error.message:'Recharge verification unavailable. No Diamonds credited.');
  }
}
const cashfreeConfig={secrets:[cashfreeClientId,cashfreeClientSecret]};
const getCashfreeRechargeCatalog=onCall(cashfreeConfig,req=>cashfreeCall(req,(service,uid)=>service.catalog({uid})));
const getCashfreeRechargeHistory=onCall(cashfreeConfig,req=>cashfreeCall(req,(service,uid)=>service.history({uid})));
const createCashfreeRechargeLink=onCall(cashfreeConfig,req=>cashfreeCall(req,(service,uid)=>service.create({
  uid,productId:req.data?.productId,phone:req.data?.phone
})));
const confirmCashfreeRecharge=onCall(cashfreeConfig,req=>cashfreeCall(req,(service,uid)=>service.confirm({
  uid,orderId:req.data?.orderId
})));
const resumeCashfreeRecharge=onCall(cashfreeConfig,req=>cashfreeCall(req,(service,uid)=>service.resume({
  uid,orderId:req.data?.orderId
})));

module.exports = {
  ...base,
  sendGift: secureSendGift,
  sendRoomGift,
  partyVoice,
  voiceRoomChanged10066,voiceSeatChanged10066,voiceMemberChanged10066,
  voiceRoomBanChanged10066,voiceAccountBanChanged10066,voiceRoleChanged10066,
  sendRoomInvite: secureSendRoomInvite,
  verifyPlayPurchase,
  getCashfreeRechargeCatalog,
  getCashfreeRechargeHistory,
  createCashfreeRechargeLink,
  confirmCashfreeRecharge,
  resumeCashfreeRecharge,
  buyVipIdentity: require('./vip-style-service').createVipStyleService({
    db, FieldValue, HttpsError, requireAuth, isBanned
  }),
  claimOwnerVipNumber,
  sendDirectMessageNotification,
  sendFollowNotification,
  requestAccountDeletion,
  onDirectMessageCreated,
  onFollowCreated,
};
