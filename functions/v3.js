const base = require('./index');
const crypto = require('crypto');
const { onCall, HttpsError } = require('firebase-functions/v2/https');
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

  // Data-only delivery lets KingMessagingService create the notification and attach
  // the correct PendingIntent, so a direct-message alert opens the exact chat.
  const payload = { ...data, title, body };
  const response = await getMessaging().sendEachForMulticast({
    tokens,
    data: Object.fromEntries(Object.entries(payload).map(([key, value]) => [key, String(value)]))
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
  if (!Number.isInteger(cost) || cost < 1 || cost > 100000) throw new HttpsError('invalid-argument', 'Invalid gift cost.');

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
      alreadyProcessed = true;
      senderBalance = Number(opSnap.get('senderBalance') || 0);
      return;
    }

    const senderSnap = await tx.get(senderRef);
    const receiverSnap = await tx.get(receiverRef);
    const senderCoins = senderSnap.exists ? Number(senderSnap.get('coins') || 0) : 0;
    const receiverCoins = receiverSnap.exists ? Number(receiverSnap.get('coins') || 0) : 0;
    if (senderCoins < cost) throw new HttpsError('failed-precondition', 'Not enough server coins.');

    senderBalance = senderCoins - cost;
    tx.set(senderRef, { coins: senderBalance, updatedAt: FieldValue.serverTimestamp() }, { merge: true });
    tx.set(receiverRef, { coins: receiverCoins + cost, updatedAt: FieldValue.serverTimestamp() }, { merge: true });
    tx.set(ledgerRef, {
      type: 'gift', senderUid, targetUid, giftName, cost,
      requestIdHash: hash(requestId), createdAt: FieldValue.serverTimestamp()
    });
    tx.set(operationRef, {
      type: 'gift', senderUid, targetUid, senderBalance,
      requestIdHash: hash(requestId), createdAt: FieldValue.serverTimestamp()
    });
  });

  if (!alreadyProcessed) {
    await sendUserNotification(targetUid, 'You received a gift', `${giftName} • ${cost} coins`, { type: 'gift', senderUid });
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
  const productId = cleanText(request.data && request.data.productId, 120);
  const purchaseToken = cleanText(request.data && request.data.purchaseToken, 4096);
  const coins = PRODUCT_COINS[productId];
  if (!coins || !purchaseToken) throw new HttpsError('invalid-argument', 'Unknown Play product or missing purchase token.');

  const auth = new google.auth.GoogleAuth({ scopes: ['https://www.googleapis.com/auth/androidpublisher'] });
  const androidpublisher = google.androidpublisher({ version: 'v3', auth });

  let purchase;
  try {
    const response = await androidpublisher.purchases.products.get({
      packageName: PACKAGE_NAME, productId, token: purchaseToken,
    });
    purchase = response.data || {};
  } catch (error) {
    console.error('Google Play verification failed', error);
    throw new HttpsError('failed-precondition',
      'Google Play verification is not available yet. Link the Cloud Functions service account in Play Console, enable Android Publisher API, and grant order/purchase permissions.');
  }

  if (Number(purchase.purchaseState) !== 0) throw new HttpsError('failed-precondition', 'Purchase is not in PURCHASED state.');

  const expectedAccount = hash(uid);
  if (purchase.obfuscatedExternalAccountId && purchase.obfuscatedExternalAccountId !== expectedAccount) {
    throw new HttpsError('permission-denied', 'Purchase belongs to a different KING Plus account.');
  }

  const tokenHash = hash(purchaseToken);
  const receiptRef = db.collection('play_purchase_receipts').doc(tokenHash);
  const walletRef = db.collection('wallets').doc(uid);
  const ledgerRef = db.collection('wallet_ledger').doc(`play_${tokenHash}`);
  let balance = 0;
  let newlyCredited = false;

  await db.runTransaction(async tx => {
    const receiptSnap = await tx.get(receiptRef);
    if (receiptSnap.exists) {
      if (receiptSnap.get('uid') !== uid) throw new HttpsError('permission-denied', 'Purchase token already belongs to another account.');
      balance = Number(receiptSnap.get('balanceAfter') || 0);
      return;
    }

    const walletSnap = await tx.get(walletRef);
    const current = walletSnap.exists ? Number(walletSnap.get('coins') || 0) : 0;
    balance = current + coins;
    newlyCredited = true;

    tx.set(walletRef, { coins: balance, updatedAt: FieldValue.serverTimestamp() }, { merge: true });
    tx.set(receiptRef, {
      uid, productId, coins, orderId: purchase.orderId || null,
      purchaseTimeMillis: purchase.purchaseTimeMillis || null,
      purchaseTokenHash: tokenHash, balanceAfter: balance,
      verifiedAt: FieldValue.serverTimestamp()
    });
    tx.set(ledgerRef, {
      type: 'play_purchase', targetUid: uid, productId, coins,
      orderId: purchase.orderId || null, purchaseTokenHash: tokenHash,
      createdAt: FieldValue.serverTimestamp()
    });
  });

  try {
    await androidpublisher.purchases.products.consume({ packageName: PACKAGE_NAME, productId, token: purchaseToken });
  } catch (error) {
    console.warn('Purchase verified/credited but consume acknowledgement needs retry', error);
  }

  if (newlyCredited) {
    await sendUserNotification(uid, 'Recharge complete', `${coins} coins added`, { type: 'play_purchase', productId });
  }

  return {
    ok: true, alreadyCredited: !newlyCredited, balance, coins,
    message: newlyCredited
      ? `${coins} verified Play coins added. Server balance: ${balance}`
      : `This purchase was already verified. Server balance: ${balance}`
  };
});

const sendDirectMessageNotification = onCall(async request => {
  const senderUid = requireAuth(request);
  const targetUid = cleanText(request.data && request.data.targetUid, 160);
  const senderName = cleanText(request.data && request.data.senderName, 60) || 'KING user';
  const preview = cleanText(request.data && request.data.preview, 100) || 'New message';
  if (!targetUid || targetUid === senderUid) throw new HttpsError('invalid-argument', 'Target user required.');
  const result = await sendUserNotification(targetUid, senderName, preview, { type: 'direct_message', senderUid, senderName });
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
    type: 'direct_message', senderUid, senderName, threadId: event.params.threadId
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
