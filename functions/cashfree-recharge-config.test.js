'use strict';
const {test}=require('node:test');const assert=require('node:assert/strict');
const {paymentEnabled}=require('./cashfree-recharge-config');
test('merchant secrets alone never turn recharge live',()=>{
  assert.equal(paymentEnabled({mode:'production',hasId:true,hasSecret:true}),false);
  assert.equal(paymentEnabled({mode:'sandbox',hasId:true,hasSecret:true}),false);
});
test('production requires explicit merchant and policy enrollment',()=>{
  assert.equal(paymentEnabled({mode:'production',hasId:true,hasSecret:true,enabled:'true',billingApproved:'false'}),false);
  assert.equal(paymentEnabled({mode:'production',hasId:true,hasSecret:true,enabled:'true',billingApproved:'true'}),true);
  assert.equal(paymentEnabled({mode:'production',hasId:true,hasSecret:false,enabled:'true',billingApproved:'true'}),false);
});
test('sandbox can be enabled independently without production authorization',()=>{
  assert.equal(paymentEnabled({mode:'sandbox',hasId:true,hasSecret:true,enabled:'true'}),true);
});
