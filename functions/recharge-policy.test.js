'use strict';
const assert=require('node:assert/strict');
const {DIAMOND_PRODUCTS,purchaseAmount,applyVerifiedRecharge,applyGiftDebit}=require('./recharge-policy');
const {threshold}=require('./king-vip-level');
let checks=0;
function eq(a,b,label){assert.deepEqual(a,b,label);checks++}
eq(Object.keys(DIAMOND_PRODUCTS).length,3,'three known Play products');
eq(purchaseAmount('king_coins_100'),100,'100 diamond package');
eq(purchaseAmount('king_coins_600'),600,'600 diamond package');
eq(purchaseAmount('king_coins_1300'),1300,'1300 diamond package');
eq(purchaseAmount('invalid'),null,'unrecognized product');
eq(applyVerifiedRecharge({},'king_coins_100'),
 {coins:100,vipPoints:100,vipLevel:0,rechargeDiamondsTotal:100,creditedDiamonds:100},
 'first paid purchase awards diamonds and VIP progress');
eq(applyVerifiedRecharge({coins:75,vipPoints:100,rechargeDiamondsTotal:100},'king_coins_600'),
 {coins:675,vipPoints:700,vipLevel:1,rechargeDiamondsTotal:700,creditedDiamonds:600},
 'second recharge reaches VIP1 after gifting depleted balance');
const before={coins:750,vipPoints:1500,rechargeDiamondsTotal:1500};
const after=applyGiftDebit(before,150);
eq(after,{coins:600,vipPoints:1500,vipLevel:2},'gift spends without increasing VIP');
eq(before,{coins:750,vipPoints:1500,rechargeDiamondsTotal:1500},'pure function');
eq(applyGiftDebit({coins:1000,vipPoints:7000},100).coins,900,'gift debit');
eq(applyGiftDebit({coins:1000,vipPoints:7000},100).vipPoints,7000,'VIP not gifted');
assert.throws(()=>applyGiftDebit({coins:10},11));checks++;
assert.throws(()=>applyGiftDebit({coins:100},-1));checks++;
assert.throws(()=>applyVerifiedRecharge({coins:-1},'king_coins_100'));checks++;
assert.throws(()=>applyVerifiedRecharge({coins:100},'wrong_product'));checks++;
eq(applyVerifiedRecharge({coins:0,vipPoints:threshold(2)-100},'king_coins_100').vipLevel,2,'VIP2 exactly on verified recharge');
eq(applyVerifiedRecharge({coins:0,vipPoints:0},'king_coins_100').vipPoints,100,'no fake VIP from zero');
console.log('PASS KING Plus verified Diamond Recharge + VIP policy: '+checks+' tests');
