import test from 'node:test';
import assert from 'node:assert/strict';
import {webcrypto} from 'node:crypto';
globalThis.crypto=globalThis.crypto||webcrypto;
import {indianPhone,amountFor,vipFromRecharge,isCaptureForOrder,hmacHex,secureHexEquals}
  from './policy.mjs';
import worker from './worker.mjs';

test('Phone UID normalization prevents prefixes and invalid Indian mobile',()=>{
  assert.equal(indianPhone('98765 43210'),'919876543210');
  assert.equal(indianPhone('+91 9876543210'),'919876543210');
  assert.equal(indianPhone('919876543210'),'919876543210');
  for(const x of ['12345','5123456789','+1 9876543210',null,{},'999999999999999'])
    assert.equal(indianPhone(x),null);
});
test('Server prices cannot be selected by user requests',()=>{
  assert.deepEqual(amountFor('diamonds_600',{diamonds_600:49900}),
    {diamonds:600,amountPaise:49900});
  assert.equal(amountFor('diamonds_600',{diamonds_600:'100'}),null);
  assert.equal(amountFor('fake_product',{fake_product:1}),null);
  assert.equal(amountFor('diamonds_600',{}),null);
  assert.equal(amountFor('diamonds_100',{diamonds_100:0}),null);
});
test('VIP grows from verified recharge only, not gift spending',()=>{
  assert.equal(vipFromRecharge(0),0);
  assert.equal(vipFromRecharge(100),0);
  assert.equal(vipFromRecharge(500),1);
  assert.equal(vipFromRecharge(600),1);
  assert.equal(vipFromRecharge(1500),2);
  assert.equal(vipFromRecharge(-10),0);
});
test('Payment captured must match original trusted order',()=>{
  const payment={id:'pay_AAAA01',order_id:'order_BBBB02',
    currency:'INR',amount:49900,status:'captured'};
  const order={order_id:'order_BBBB02',amount_paise:49900};
  assert.equal(isCaptureForOrder(payment,order),true);
  assert.equal(isCaptureForOrder({...payment,status:'created'},order),false);
  assert.equal(isCaptureForOrder({...payment,amount:100},order),false);
  assert.equal(isCaptureForOrder({...payment,order_id:'other'},order),false);
  assert.equal(isCaptureForOrder({...payment,currency:'USD'},order),false);
});
test('Razorpay webhook HMAC is raw-body bound and signature comparison exact',async()=>{
  const secret='test-webhook-secret';
  const raw='{"event":"payment.captured","status":"captured"}';
  const signature=await hmacHex(secret,raw);
  assert.equal(secureHexEquals(signature,signature),true);
  assert.equal(secureHexEquals(signature,await hmacHex(secret,raw+' ')),false);
  assert.equal(secureHexEquals(signature,'abcd'),false);
  assert.equal(secureHexEquals(signature,null),false);
});
test('Worker prevents both OTP and payment without configured secrets',async()=>{
  const request=new Request('https://example.workers.dev/otp/request',{
    method:'POST',headers:{'content-type':'application/json'},
    body:JSON.stringify({phone:'9876543210'})
  });
  const response=await worker.fetch(request,{});
  assert.equal(response.status,503);
  const pay=new Request('https://example.workers.dev/recharge/order',{
    method:'POST',headers:{'content-type':'application/json'},
    body:JSON.stringify({sku:'diamonds_600'})
  });
  assert.equal((await worker.fetch(pay,{})).status,503);
  const ready=await worker.fetch(new Request('https://example.workers.dev/health'),{});
  assert.equal((await ready.json()).livePayments,false);
});
