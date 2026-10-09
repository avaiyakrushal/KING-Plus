'use strict';

// KING Plus diamond policy — server-authoritative. No payment can be credited
// from Android-reported price, UPI screenshot, TEST coins or manual client fields.
const {levelFromVerifiedSpend}=require('./king-vip-level');
const DIAMOND_PRODUCTS=Object.freeze({
  king_coins_100:100,
  king_coins_600:600,
  king_coins_1300:1300,
});
const VIP_MAX_POINTS=1000000000;
const WALLET_MAX_DIAMONDS=1000000000;

function isInt(value,min,max){
  return Number.isSafeInteger(value)&&value>=min&&value<=max;
}
function purchaseAmount(productId){
  return Object.hasOwn(DIAMOND_PRODUCTS,productId) ? DIAMOND_PRODUCTS[productId] : null;
}
function applyVerifiedRecharge(wallet,productId){
  const qty=purchaseAmount(productId);
  if(!qty)throw new Error('Unknown Google Play Diamond product');
  const current=wallet&&wallet.coins!=null?wallet.coins:0;
  // Recharge VIP is based exclusively on verified Play purchase history, never Gifts.
  // Legacy wallet.vipPoints may include old Gift points and must not be trusted.
  const earned=wallet&&wallet.rechargeDiamondsTotal!=null?wallet.rechargeDiamondsTotal:0;
  if(!isInt(current,0,WALLET_MAX_DIAMONDS)||!isInt(earned,0,VIP_MAX_POINTS))
    throw new Error('Invalid wallet state');
  if(current>WALLET_MAX_DIAMONDS-qty)throw new Error('Wallet upper limit reached');
  const vipPoints=Math.min(VIP_MAX_POINTS,earned+qty);
  return Object.freeze({
    coins:current+qty,
    vipPoints,
    vipLevel:levelFromVerifiedSpend(vipPoints),
    rechargeDiamondsTotal:vipPoints,
    creditedDiamonds:qty
  });
}
function applyGiftDebit(senderWallet,cost){
  if(!isInt(cost,1,1000000))throw new Error('Invalid gift cost');
  const balance=senderWallet&&senderWallet.coins!=null?senderWallet.coins:0;
  if(!isInt(balance,0,WALLET_MAX_DIAMONDS)||balance<cost)
    throw new Error('Insufficient verified Diamond balance');
  // Sending a Gift spends diamonds. VIP points are *only* earned on purchase.
  return Object.freeze({
    coins:balance-cost,
    vipPoints:(senderWallet&&isInt(senderWallet.vipPoints,0,VIP_MAX_POINTS))
      ?senderWallet.vipPoints:0,
    vipLevel:(senderWallet&&isInt(senderWallet.vipPoints,0,VIP_MAX_POINTS))
      ?levelFromVerifiedSpend(senderWallet.vipPoints):0
  });
}
// Gift receivers earn non-redeemable recognition, not spendable diamonds.
// This avoids peer-to-peer currency transfers, withdrawals, or cash-out.
function applyGiftReception(receiverWallet,cost){
  if(!isInt(cost,1,1000000))throw new Error('Invalid gift cost');
  const score=receiverWallet&&receiverWallet.giftScore!=null?receiverWallet.giftScore:0;
  if(!isInt(score,0,WALLET_MAX_DIAMONDS)||score>WALLET_MAX_DIAMONDS-cost)
    throw new Error('Recipient Gift score upper limit reached');
  return Object.freeze({giftScore:score+cost});
}
module.exports={DIAMOND_PRODUCTS,purchaseAmount,applyVerifiedRecharge,applyGiftDebit,applyGiftReception,
  VIP_MAX_POINTS,WALLET_MAX_DIAMONDS};
