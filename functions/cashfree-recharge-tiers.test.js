'use strict';
const test=require('node:test');
const assert=require('node:assert/strict');
const {purchaseAmount,applyVerifiedRecharge}=require('./recharge-policy');
const {validatePack,ALLOWED_SKUS}=require('./cashfree-recharge-service');
const tiers=[8800,17800,45000,90000,180000,450000];
test('six new Bolo-style denomination sizes exist only as server-approved UPI SKUs',()=>{
  for(const diamonds of tiers){
    const sku='king_upi_'+diamonds;
    assert(ALLOWED_SKUS.includes(sku));
    assert.equal(purchaseAmount(sku),diamonds);
    assert.equal(validatePack(sku,{active:true,currency:'INR',amountPaise:10900}).diamonds,diamonds);
    for(const invalid of [0,-1,12.5,NaN,Infinity])
      assert.equal(validatePack(sku,{active:true,currency:'INR',amountPaise:invalid}),null);
    assert.equal(validatePack(sku,{active:false,currency:'INR',amountPaise:10900}),null);
  }
});
test('server catalog rejects client-derived diamond quantities and unapproved currencies',()=>{
  const sku='king_upi_45000';
  const validated=validatePack(sku,{active:true,currency:'INR',amountPaise:44700,diamonds:999999999});
  assert.equal(validated.diamonds,45000);
  assert.equal(validated.amountPaise,44700);
  assert.equal(validatePack(sku,{active:true,currency:'USD',amountPaise:44700}),null);
  assert.equal(validatePack('king_upi_hacked',{active:true,currency:'INR',amountPaise:44700}),null);
});
test('verified additional tier credits wallet in existing VIP path; balance cap still enforced',()=>{
  const updated=applyVerifiedRecharge({coins:1200,rechargeDiamondsTotal:1000},'king_upi_8800');
  assert.equal(updated.coins,10000);assert.equal(updated.creditedDiamonds,8800);
  assert.equal(updated.rechargeDiamondsTotal,9800);
  assert.throws(()=>applyVerifiedRecharge({coins:999999999,rechargeDiamondsTotal:0},'king_upi_8800'),/upper limit/);
});
