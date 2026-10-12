const base = require('./index');
const crypto = require('crypto');
const {levelFromVerifiedSpend} = require('./king-vip-level');
const {DIAMOND_PRODUCTS, purchaseAmount, applyVerifiedRecharge, applyGiftDebit, applyGiftReception, WALLET_MAX_DIAMONDS} = require('./recharge-policy');
const {priceFor, MAX_VALUE} = require('./gift-catalog');
const { onCall, HttpsError } = require('firebase-functions/v2/https');
const { onDocumentCreated } = require('firebase-functions/v2/firestore');
const { getFirestore, FieldValue } = require('firebase-admin/firestore');
const { getMessaging } = require('firebase-admin/messaging');
const { google } = require('googleapis');

const db = getFirestore();
const PACKAGE_NAME = 'com.kingplus.social';
// Google Play Console must register these exact consumable one-time product IDs.
const PRODUCT_COINS = DIAMOND_PRODUCTS;

function requireAuth(request) {
  if (!request.auth) throw new HttpsError('unauthenticated', 'Sign in required.');
  return request.auth.uid;
}

function cleanText(value, max = 160) {
  if (typeof value !== 'string') return '';
  return value.trim().slice(0, max);
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

const secureSendGift = onCall(async request => {
  const senderUid = requireAuth(request);
  const targetUid = cleanText(request.data && request.data.targetUid, 160);
  const giftName = cleanText(request.data && request.data.giftName, 60) || 'Gift';
  const cost = Number(request.data && request.data.cost);
  const requestId = cleanText(request.data && request.data.requestId, 120) || crypto.randomUUID();

  if (!targetUid || targetUid === senderUid) throw new HttpsError('invalid-argument', 'Choose another user.');
  const officialGift = priceFor(giftName);
  if (!officialGift || !Number.isSafeInteger(cost) || cost !== officialGift.total || cost > MAX_VALUE)
    throw new HttpsError('invalid-argument', 'Unknown Gift or price does not match the verified KING catalog.');

  const operationId = `${senderUid}_${hash(requestId)}`;
  const operationRef = db.collection('wallet_operations').doc(operationId);
  const senderRef = db.collection('wallets').doc(senderUid);
  const receiverRef = db.collection('wallets').doc(targetUid);
  const ledgerRef = db.collection('wallet_ledger').doc(`gift_${hash(operationId)}`);
  let senderBalance = 0;
  let alreadyProcessed = false;

  await db.runTransaction(async tx => {
    const opSnap = await tx.get(operationRef);
    if (opSnap.exists) {
      if (opSnap.get('targetUid') !== targetUid ||
          (opSnap.get('giftCost') != null && Number(opSnap.get('giftCost')) !== cost)) {
        throw new HttpsError('already-exists', 'Gift request ID already belongs to a different purchase.');
      }
      alreadyProcessed = true;
      senderBalance = Number(opSnap.get('senderBalance') || 0);
      return;
    }

    const senderSnap = await tx.get(senderRef);
    const receiverSnap = await tx.get(receiverRef);
    const currentSender = senderSnap.exists ? senderSnap.data() : {};
    const senderAfter = applyGiftDebit(currentSender, cost);
    const recipientAfter = applyGiftReception(receiverSnap.exists ? receiverSnap.data() : {}, cost);
    senderBalance = senderAfter.coins;
    // The sender's VIP is NOT incremented when sending Gifts.
    // VIP increases only after a verified Google Play Diamond recharge.
    tx.set(senderRef, { coins: senderBalance, updatedAt: FieldValue.serverTimestamp() }, { merge: true });
    tx.set(receiverRef, { giftScore: recipientAfter.giftScore, updatedAt: FieldValue.serverTimestamp() }, { merge: true });
    tx.set(ledgerRef, {
      type: 'gift', senderUid, targetUid, giftName: officialGift.giftName,
      quantity: officialGift.quantity, cost,
      vipPointsEarned: 0, nonRedeemable: true, requestIdHash: hash(requestId),
      createdAt: FieldValue.serverTimestamp()
    });
    tx.set(operationRef, {
      type: 'gift', senderUid, targetUid, senderBalance, giftCost: cost,
      requestIdHash: hash(requestId), createdAt: FieldValue.serverTimestamp()
    });
  });

  if (!alreadyProcessed) {
    try {
      await sendUserNotification(targetUid, 'You received a gift', `${giftName} • ${cost} coins`, { type: 'gift', senderUid });
    } catch (notificationError) {
      // The secure wallet transaction has committed. FCM failure cannot undo it.
      console.warn('Gift paid; push notification deferred', notificationError);
    }
  }

  return {
    ok: true,
    idempotent: alreadyProcessed,
    balance: senderBalance,
    message: alreadyProcessed
      ? `Gift request already processed. Balance: ${senderBalance}`
      : `${giftName} sent securely. Balance: ${senderBalance}`
  };
});

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

const sendDirectMessageNotification = onCall(async request => {
  const senderUid = requireAuth(request);
  const targetUid = cleanText(request.data && request.data.targetUid, 160);
  const senderName = cleanText(request.data && request.data.senderName, 60) || 'KING user';
  const preview = cleanText(request.data && request.data.preview, 100) || 'New message';
  if (!targetUid || targetUid === senderUid) throw new HttpsError('invalid-argument', 'Target user required.');
  const result = await sendUserNotification(targetUid, senderName, preview, { type: 'direct_message', senderUid });
  return { ok: true, ...result, message: result.sent ? 'Message push sent.' : 'Message saved; no active push token found.' };
});

const sendFollowNotification = onCall(async request => {
  const followerUid = requireAuth(request);
  const targetUid = cleanText(request.data && request.data.targetUid, 160);
  const followerName = cleanText(request.data && request.data.followerName, 60) || 'KING user';
  if (!targetUid || targetUid === followerUid) throw new HttpsError('invalid-argument', 'Target user required.');
  const result = await sendUserNotification(targetUid, 'New follower', `${followerName} followed you`, { type: 'follow', followerUid });
  return { ok: true, ...result, message: result.sent ? 'Follow push sent.' : 'Follow notification saved.' };
});

const onDirectMessageCreated = onDocumentCreated('direct_threads/{threadId}/messages/{messageId}', async event => {
  if (!event.data) return;
  const message = event.data.data();
  const senderUid = cleanText(message.senderUid, 160);
  const targetUid = cleanText(message.recipientUid, 160);
  if (!senderUid || !targetUid || senderUid === targetUid) return;
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
  const followerName = cleanText(follow.followerName, 60) || 'KING user';
  await sendUserNotification(targetUid, 'New follower', `${followerName} followed you`, { type: 'follow', followerUid });
});

module.exports = {
  ...base,
  sendGift: secureSendGift,
  verifyPlayPurchase,
  sendDirectMessageNotification,
  sendFollowNotification,
  onDirectMessageCreated,
  onFollowCreated,
};
