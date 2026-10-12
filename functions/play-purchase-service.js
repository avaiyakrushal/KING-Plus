'use strict';

const {createHash} = require('node:crypto');
const {purchaseAmount, applyVerifiedRecharge, WALLET_MAX_DIAMONDS} = require('./recharge-policy');
const hash = value => createHash('sha256').update(value).digest('hex');

class PurchaseError extends Error {
  constructor(code, message) { super(message); this.code = code; }
}
const fail = (code, message) => { throw new PurchaseError(code, message); };

// Only the server's Google Play response is accepted here, never Android fields.
function validatePurchase(purchase, uid, productId) {
  if (!purchase || purchase.purchaseState !== 0)
    fail('failed-precondition', 'Purchase is not in PURCHASED state.');
  if (purchase.obfuscatedExternalAccountId !== hash(uid))
    fail('permission-denied', 'Purchase does not belong to this KING Plus account.');
  if (purchase.productId !== undefined && purchase.productId !== productId)
    fail('failed-precondition', 'Verified product does not match the requested product.');
  // This wallet is for paid purchases. Google sets purchaseType for license
  // tests, promotions and rewarded purchases; those require isolated wallets.
  if (purchase.purchaseType !== undefined)
    fail('failed-precondition', 'Non-paid purchases require an isolated test or promotional wallet.');
  if (purchase.quantity !== undefined && purchase.quantity !== 1)
    fail('failed-precondition', 'Only a single product quantity is supported.');
  if (purchase.refundableQuantity !== undefined && purchase.refundableQuantity !== 1)
    fail('failed-precondition', 'Refunded purchase cannot be credited.');
  if (purchase.consumptionState !== 0 && purchase.consumptionState !== 1)
    fail('failed-precondition', 'Invalid purchase consumption state.');
}

// Dependencies are injected so the same transaction used by the callable can be
// exercised with a real Firestore emulator without any Google Play charges.
function createPurchaseService({db, getPurchase, consumePurchase, timestamp, notify}) {
  return async ({uid, productId, purchaseToken}) => {
    if (typeof uid !== 'string' || !uid || uid.includes('/'))
      fail('unauthenticated', 'Sign in required.');
    if (typeof productId !== 'string' || !purchaseAmount(productId) ||
        typeof purchaseToken !== 'string' || !purchaseToken.trim() ||
        purchaseToken !== purchaseToken.trim() || purchaseToken.length > 4096)
      fail('invalid-argument', 'Unknown Play product or invalid purchase token.');
    const coins = purchaseAmount(productId);
    let purchase;
    try { purchase = await getPurchase(productId, purchaseToken); }
    catch (_) { fail('unavailable', 'Google Play verification is unavailable. Retry this purchase later.'); }
    validatePurchase(purchase, uid, productId);
    const tokenHash = hash(purchaseToken);
    const receiptRef = db.collection('play_purchase_receipts').doc(tokenHash);
    const walletRef = db.collection('wallets').doc(uid);
    const banRef = db.collection('bans').doc(uid);
    const ledgerRef = db.collection('wallet_ledger').doc(`play_${tokenHash}`);

    // Return the committed attempt's result. Firestore can retry this callback;
    // mutable outer flags would incorrectly mark a duplicate as newly credited.
    const result = await db.runTransaction(async tx => {
      const receipt = await tx.get(receiptRef);
      const wallet = await tx.get(walletRef);
      const ban = await tx.get(banRef);
      if (ban.exists && ban.get('active') === true)
        fail('permission-denied', 'This account is restricted.');
      const state = wallet.exists ? wallet.data() : {};
      const current = state.coins == null ? 0 : state.coins;
      if (!Number.isSafeInteger(current) || current < 0 || current > WALLET_MAX_DIAMONDS)
        fail('failed-precondition', 'Wallet needs support review.');
      if (receipt.exists) {
        if (receipt.get('uid') !== uid || receipt.get('productId') !== productId || receipt.get('coins') !== coins)
          fail('permission-denied', 'Purchase receipt does not match this account and product.');
        if (receipt.get('refundedAt') != null || ['refunded', 'revoked'].includes(receipt.get('status')))
          fail('failed-precondition', 'Purchase entitlement was revoked.');
        return {alreadyCredited: true, balance: current};
      }
      if (purchase.consumptionState !== 0)
        fail('failed-precondition', 'Consumed purchase has no matching receipt. Contact support.');
      let next;
      try { next = applyVerifiedRecharge(state, productId); }
      catch (_) { fail('failed-precondition', 'Wallet cannot accept this recharge. Contact support.'); }
      tx.set(walletRef, {
        coins: next.coins, vipPoints: next.vipPoints, vipLevel: next.vipLevel,
        rechargeDiamondsTotal: next.rechargeDiamondsTotal, updatedAt: timestamp()
      }, {merge: true});
      tx.set(receiptRef, {
        uid, productId, coins, purchaseTokenHash: tokenHash,
        orderId: purchase.orderId || null, purchaseTimeMillis: purchase.purchaseTimeMillis || null,
        balanceAfter: next.coins, vipPointsEarned: coins, vipLevelAfter: next.vipLevel,
        status: 'credited', verifiedAt: timestamp()
      });
      tx.set(ledgerRef, {
        type: 'play_purchase', targetUid: uid, productId, coins, purchaseTokenHash: tokenHash,
        orderId: purchase.orderId || null, vipPointsEarned: coins,
        vipLevelAfter: next.vipLevel, createdAt: timestamp()
      });
      return {alreadyCredited: false, balance: next.coins};
    });

    let consumptionPending = false;
    if (purchase.consumptionState === 0) {
      try { await consumePurchase(productId, purchaseToken); }
      catch (_) { consumptionPending = true; }
    }
    if (!result.alreadyCredited && notify) {
      // A notification failure must never turn committed credit into a failure.
      try { await notify(uid, coins, productId); } catch (_) { /* best effort */ }
    }
    return {
      ok: true, ...result, coins, consumptionPending,
      message: `${result.alreadyCredited ? 'Purchase already credited.' : `${coins} verified Diamonds added.`} Server balance: ${result.balance}`
    };
  };
}

module.exports = {createPurchaseService, validatePurchase, PurchaseError};
