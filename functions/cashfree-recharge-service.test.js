'use strict';
const test=require('node:test');
const assert=require('node:assert/strict');
const {createCashfreeRechargeService,validatePack,isProviderPaid,safeLink}=require('./cashfree-recharge-service');
function dbMock(){
  const docs=new Map();const snapshot=(key)=>({exists:docs.has(key),data:()=>structuredClone(docs.get(key)),get:k=>docs.get(key)?.[k]});
  function ref(key){return {key,get:async()=>snapshot(key),create:async v=>{assert(!docs.has(key));docs.set(key,v);},update:async v=>docs.set(key,{...docs.get(key),...v})};}
  return {docs,collection:name=>({
    doc:id=>ref(name+'/'+id),
    where:(field,op,value)=>({orderBy:(sortKey,direction)=>({limit:max=>({get:async()=>({docs:[...docs.entries()]
      .filter(([key,row])=>key.startsWith(name+'/')&&op==='=='&&row[field]===value)
      .sort((a,b)=>direction==='desc'?b[1][sortKey]-a[1][sortKey]:a[1][sortKey]-b[1][sortKey])
      .slice(0,max).map(([key,row])=>({id:key.slice(name.length+1),data:()=>structuredClone(row)}))})})})})
  }),runTransaction:async fn=>fn({
    get:async r=>snapshot(r.key),set:(r,data,opts)=>docs.set(r.key,opts?.merge?{...docs.get(r.key),...data}:data),create:(r,data)=>{assert(!docs.has(r.key));docs.set(r.key,data);},update:(r,v)=>docs.set(r.key,{...docs.get(r.key),...v})
  })};
}
const make=(db,linkStatus='PAID',opts={})=>{
  const provider={
    async createLink({id,pack}){return {link_id:id,link_url:'https://payments-test.cashfree.com/links/'+id,link_currency:'INR',link_amount:pack.amountPaise/100,link_partial_payments:false,link_status:'ACTIVE'};},
    async getLink(id){const order=db.docs.get('cashfree_recharge_orders/'+id);return {link_id:id,link_status:linkStatus,link_url:'https://payments-test.cashfree.com/links/'+id,link_currency:'INR',link_amount:order.amountPaise/100,link_amount_paid:order.amountPaise/100,link_partial_payments:false,...opts};}
  };
  return createCashfreeRechargeService({db,provider,stamp:()=>1234,isBanned:async()=>false,enabled:true,mode:'sandbox'});
};
const setup=()=>{
  const db=dbMock();db.docs.set('cashfree_recharge_catalog/king_coins_100',{active:true,currency:'INR',amountPaise:5000});return db;
};
test('catalog amounts cannot come from Android',async()=>{
  const db=setup(),s=make(db);const c=await s.catalog({uid:'alice'});
  assert.equal(c.packs[0].amountPaise,5000);assert.equal(c.packs[0].diamonds,100);
  assert.equal(validatePack('king_coins_100',{active:true,currency:'INR',amountPaise:0}),null);
  assert.equal(validatePack('king_coins_100',{active:true,currency:'INR',amountPaise:10.25}),null);
});
test('disabled mode never returns a payment URL',async()=>{
  const db=setup(),s=createCashfreeRechargeService({db,isBanned:async()=>false,stamp:()=>1,provider:{},enabled:false,mode:'sandbox'});
  assert.deepEqual((await s.catalog({uid:'alice'})).packs,[]);
  await assert.rejects(()=>s.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'}),/not been enabled/);
});
test('only valid payment link host can open browser',()=>{
  assert.equal(safeLink('https://payments-test.cashfree.com/links/a','sandbox'),true);
  assert.equal(safeLink('https://payments-test.cashfree.com.evil.com/links/a','sandbox'),false);
  assert.equal(safeLink('http://payments-test.cashfree.com/links/a','sandbox'),false);
  assert.equal(safeLink('https://payments.cashfree.com/links/a','sandbox'),false);
});
test('verified full INR amount exact with paid, no partial',()=>{
  const i={linkId:'id',amountPaise:5000};
  const l={link_id:'id',link_status:'PAID',link_partial_payments:false,link_currency:'INR',link_amount:50,link_amount_paid:50};
  assert.equal(isProviderPaid(l,i),true);
  for(const v of [{link_amount_paid:49.99},{link_status:'ACTIVE'},{link_currency:'USD'},{link_partial_payments:true},{link_id:'different'}])assert.equal(isProviderPaid({...l,...v},i),false);
});
test('paid link credits wallet once, ties receipt to UID',async()=>{
  const db=setup(),s=make(db);
  const opened=await s.create({uid:'alice',productId:'king_coins_100',phone:'9876543210',amountPaise:100});
  assert.equal(opened.amountPaise,5000);
  assert.equal(db.docs.has('wallets/alice'),false);
  const result=await s.confirm({uid:'alice',orderId:opened.orderId});
  assert.equal(result.ok,true);assert.equal(result.diamonds,100);
  assert.equal(db.docs.get('wallets/alice').coins,100);
  const again=await s.confirm({uid:'alice',orderId:opened.orderId});
  assert.equal(again.alreadyCredited,true);assert.equal(db.docs.get('wallets/alice').coins,100);
  await assert.rejects(()=>s.confirm({uid:'bob',orderId:opened.orderId}),/does not belong/);
});
test('unpaid or underpaid link never gives diamonds',async()=>{
  const db=setup(),s=make(db,'ACTIVE');
  const opened=await s.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'});
  assert.equal((await s.confirm({uid:'alice',orderId:opened.orderId})).pending,true);
  assert.equal(db.docs.has('wallets/alice'),false);
  const underpaid=make(db,'PAID',{link_amount_paid:1});
  assert.equal((await underpaid.confirm({uid:'alice',orderId:opened.orderId})).pending,true);
  assert.equal(db.docs.has('wallets/alice'),false);
});
test('invalid UID and phone cannot create a recharge',async()=>{
  const db=setup(),s=make(db);
  await assert.rejects(()=>s.create({uid:'a/b',productId:'king_coins_100',phone:'9876543210'}));
  await assert.rejects(()=>s.create({uid:'alice',productId:'king_coins_100',phone:'1234567890'}));
  await assert.rejects(()=>s.create({uid:'alice',productId:'fake',phone:'9876543210'}));
});

test('gateway create timeout recovers same payment link and never double-creates',async()=>{
  const db=setup(), provider={calls:0,
    async createLink(){this.calls++;throw Error('socket timed out after Cashfree committed');},
    async getLink(id){const pack=db.docs.get('cashfree_recharge_orders/'+id);
      return {link_id:id,link_url:'https://payments-test.cashfree.com/links/'+id,
        link_currency:'INR',link_amount:pack.amountPaise/100,link_partial_payments:false,
        link_status:'ACTIVE'};}
  };
  const s=createCashfreeRechargeService({db,provider,stamp:()=>1234,isBanned:async()=>false,enabled:true,mode:'sandbox'});
  const res=await s.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'});
  assert.equal(provider.calls,1);
  assert.equal(db.docs.get('cashfree_recharge_orders/'+res.orderId).status,'pending');
  assert.equal(res.amountPaise,5000);
});

test('gateway timeout with unknown ACK retains recoverable order for paid confirmation',async()=>{
  const db=setup(), provider={
    async createLink(){throw Error('timeout');},
    async getLink(id){if(this.offline)throw Error('offline');let x=db.docs.get('cashfree_recharge_orders/'+id);
      return {link_id:id,link_status:'PAID',link_partial_payments:false,link_currency:'INR',
        link_amount:x.amountPaise/100,link_amount_paid:x.amountPaise/100};}
  };
  provider.offline=true;
  const s=createCashfreeRechargeService({db,provider,stamp:()=>1234,isBanned:async()=>false,enabled:true,mode:'sandbox'});
  await assert.rejects(()=>s.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'}),/Existing order saved/);
  const orders=await s.history({uid:'alice'});
  assert.equal(orders.orders.length,1);assert.equal(orders.orders[0].status,'verify-needed');
  assert.equal(db.docs.has('wallets/alice'),false);
  provider.offline=false;
  const got=await s.confirm({uid:'alice',orderId:orders.orders[0].orderId});
  assert.equal(got.diamonds,100);
  assert.equal(db.docs.get('wallets/alice').coins,100);
  const again=await s.confirm({uid:'alice',orderId:orders.orders[0].orderId});
  assert.equal(again.alreadyCredited,true);
  assert.equal(db.docs.get('wallets/alice').coins,100);
});

test('history only shows own orders and never includes phone/link URL',async()=>{
  const db=setup(),s=make(db);
  const alice=await s.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'});
  await s.create({uid:'bob',productId:'king_coins_100',phone:'9876543210'});
  const hist=await s.history({uid:'alice'});
  assert.equal(hist.orders.length,1);assert.equal(hist.orders[0].orderId,alice.orderId);
  assert.equal(Object.hasOwn(hist.orders[0],'linkUrl'),false);
  assert.equal(Object.hasOwn(hist.orders[0],'phone'),false);
});

test('tampered order data is rejected before wallet mutation',async()=>{
  const db=setup(),s=make(db);
  const opened=await s.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'});
  const order=db.docs.get('cashfree_recharge_orders/'+opened.orderId);
  order.diamonds=100000;
  db.docs.set('cashfree_recharge_orders/'+opened.orderId,order);
  await assert.rejects(()=>s.confirm({uid:'alice',orderId:opened.orderId}),/Recharge details changed/);
  assert.equal(db.docs.has('wallets/alice'),false);
});

// Recharge resume must be owned, payable, provider-verified and idempotent.
test('resume reopens exact existing link and never creates a second order',async()=>{
  const db=setup(),s=make(db,'ACTIVE');
  const existing=await s.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'});
  const before=[...db.docs.keys()].filter(key=>key.startsWith('cashfree_recharge_orders/')).length;
  const same=await s.resume({uid:'alice',orderId:existing.orderId});
  assert.equal(same.ok,true);assert.equal(same.status,'pending');
  assert.equal(same.checkoutUrl,'https://payments-test.cashfree.com/links/'+existing.orderId);
  assert.equal([...db.docs.keys()].filter(key=>key.startsWith('cashfree_recharge_orders/')).length,before);
  assert.equal(db.docs.has('wallets/alice'),false);
});
test('resume rejects foreign order and invalid order ID',async()=>{
  const db=setup(),s=make(db,'ACTIVE');
  const order=await s.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'});
  await assert.rejects(()=>s.resume({uid:'bob',orderId:order.orderId}),/does not belong/);
  await assert.rejects(()=>s.resume({uid:'alice',orderId:'other'}),/Invalid recharge order/);
});
test('resume paid order returns confirmation, no checkout URL and never credits itself',async()=>{
  const db=setup(),s=make(db,'PAID');
  const order=await s.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'});
  const response=await s.resume({uid:'alice',orderId:order.orderId});
  assert.equal(response.status,'paid');assert.equal(Object.hasOwn(response,'checkoutUrl'),false);
  assert.equal(db.docs.has('wallets/alice'),false);
  await s.confirm({uid:'alice',orderId:order.orderId});
  assert.equal((await s.resume({uid:'alice',orderId:order.orderId})).status,'credited');
});
test('resume rejects altered provider URL, amount and status',async()=>{
  const db=setup(),initial=make(db,'ACTIVE');
  const order=await initial.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'});
  for(const bad of [{link_url:'https://payments-test.cashfree.com.evil.com/links/x'},
    {link_amount:1},{link_currency:'USD'},{link_partial_payments:true}]){
    const s=make(db,'ACTIVE',{link_url:'https://payments-test.cashfree.com/links/'+order.orderId,...bad});
    await assert.rejects(()=>s.resume({uid:'alice',orderId:order.orderId}),/could not be verified/);
  }
  assert.equal((await make(db,'CANCELLED',{link_url:'https://payments-test.cashfree.com/links/'+order.orderId}).resume({uid:'alice',orderId:order.orderId})).status,'unavailable');
  assert.equal(db.docs.has('wallets/alice'),false);
});
test('recovery of provider timeout order uses original link and does not credit',async()=>{
  const db=setup(),s=make(db,'ACTIVE');
  const order=await s.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'});
  db.docs.set('cashfree_recharge_orders/'+order.orderId,{
    ...db.docs.get('cashfree_recharge_orders/'+order.orderId),status:'verify-needed'
  });
  const resumed=await s.resume({uid:'alice',orderId:order.orderId});
  assert.equal(resumed.status,'pending');
  assert.equal(db.docs.get('cashfree_recharge_orders/'+order.orderId).status,'pending');
  assert.equal(db.docs.has('wallets/alice'),false);
});
test('history returns newest orders before applying 20-item page limit',async()=>{
  const db=setup(),s=make(db,'ACTIVE');
  for(let i=0;i<55;i++)db.docs.set('cashfree_recharge_orders/king_'+i.toString(16).padStart(32,'0'),{
    uid:'alice',productId:'king_coins_100',diamonds:100,amountPaise:5000,status:'pending',createdAt:i
  });
  const rows=(await s.history({uid:'alice'})).orders;
  assert.equal(rows.length,20);assert.equal(rows[0].createdAt,54);assert.equal(rows.at(-1).createdAt,35);
});
test('disabled merchant resume cannot open or return any checkout link',async()=>{
  const db=setup(),s=createCashfreeRechargeService({db,provider:{},stamp:()=>1,isBanned:async()=>false,enabled:false,mode:'sandbox'});
  await assert.rejects(()=>s.resume({uid:'alice',orderId:'king_'+('a'.repeat(32))}),/not been enabled/);
});

test('paid gateway result without a checkout URL can still be verified, never opens browser',async()=>{
  const db=setup(),s=make(db,'PAID',{link_url:undefined});
  const id=(await s.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'})).orderId;
  const result=await s.resume({uid:'alice',orderId:id});
  assert.equal(result.status,'paid');
  assert.equal(Object.hasOwn(result,'checkoutUrl'),false);
  assert.equal(db.docs.has('wallets/alice'),false);
});

// v10.0.53: one active merchant order per account and pack; closed orders can be replaced.
test('double taps and cross-device retry cannot create multiple payment links for the same pack',async()=>{
  const db=setup(),s=make(db,'ACTIVE');
  const first=await s.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'});
  await assert.rejects(()=>s.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'}),/Previous recharge is still pending/);
  assert.equal([...db.docs.keys()].filter(k=>k.startsWith('cashfree_recharge_orders/')).length,1);
  assert.equal((await s.resume({uid:'alice',orderId:first.orderId})).orderId,first.orderId);
});
test('separate accounts cannot block one another',async()=>{
  const db=setup(),s=make(db,'ACTIVE');
  const a=await s.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'});
  const b=await s.create({uid:'bob',productId:'king_coins_100',phone:'9876543210'});
  assert.notEqual(a.orderId,b.orderId);
});
test('provider-verified expired unpaid link is terminal, a replacement can be created',async()=>{
  const db=setup(),s=make(db,'ACTIVE');
  const first=await s.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'});
  const expired=make(db,'EXPIRED',{link_amount_paid:0});
  const status=await expired.confirm({uid:'alice',orderId:first.orderId});
  assert.equal(status.status,'expired');assert.equal(status.pending,false);
  assert.equal(db.docs.get('cashfree_recharge_orders/'+first.orderId).status,'expired');
  assert.equal(db.docs.has('wallets/alice'),false);
  const replacement=await s.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'});
  assert.notEqual(replacement.orderId,first.orderId);
});
test('provider-verified cancelled link cannot be reopened, and enables a replacement',async()=>{
  const db=setup(),s=make(db,'ACTIVE');
  const first=await s.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'});
  const cancelled=make(db,'CANCELLED',{link_amount_paid:0,link_url:undefined});
  const stopped=await cancelled.resume({uid:'alice',orderId:first.orderId});
  assert.equal(stopped.status,'cancelled');
  assert.equal(db.docs.get('cashfree_recharge_orders/'+first.orderId).status,'cancelled');
  assert.equal(db.docs.has('wallets/alice'),false);
  const next=await s.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'});
  assert.notEqual(first.orderId,next.orderId);
});
test('gateway missing zero-payment evidence never closes or replaces pending link',async()=>{
  const db=setup(),s=make(db,'ACTIVE');
  const first=await s.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'});
  const uncertain=make(db,'EXPIRED',{link_amount_paid:undefined});
  const status=await uncertain.confirm({uid:'alice',orderId:first.orderId});
  assert.equal(status.pending,true);
  assert.equal(db.docs.get('cashfree_recharge_orders/'+first.orderId).status,'pending');
  await assert.rejects(()=>s.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'}),/Previous recharge is still pending/);
});
test('credited recharge is final and a subsequent recharge may start',async()=>{
  const db=setup(),s=make(db,'PAID');
  const first=await s.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'});
  await s.confirm({uid:'alice',orderId:first.orderId});
  const second=await s.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'});
  assert.notEqual(first.orderId,second.orderId);
  assert.equal(db.docs.get('wallets/alice').coins,100);
});

// v10.0.54: a gateway create response must be ACTIVE before a payment URL is returned.
test('new checkout never opens a gateway-declared cancelled or paid link',async()=>{
  for(const status of ['CANCELLED','PAID','EXPIRED']){
    const db=setup();
    const provider={
      async createLink({id,pack}){return {link_id:id,link_status:status,
        link_url:'https://payments-test.cashfree.com/links/'+id,
        link_currency:'INR',link_amount:pack.amountPaise/100,link_partial_payments:false};},
      async getLink(id){const row=db.docs.get('cashfree_recharge_orders/'+id);
        return {link_id:id,link_status:status,link_url:'https://payments-test.cashfree.com/links/'+id,
          link_currency:'INR',link_amount:row.amountPaise/100,link_partial_payments:false};}
    };
    const s=createCashfreeRechargeService({db,provider,stamp:()=>1234,isBanned:async()=>false,enabled:true,mode:'sandbox'});
    await assert.rejects(()=>s.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'}),/Existing order saved/);
    const orders=(await s.history({uid:'alice'})).orders;
    assert.equal(orders.length,1);assert.equal(orders[0].status,'verify-needed');
    assert.equal(db.docs.has('wallets/alice'),false);
  }
});
test('unknown create status is resolved by querying original link, without duplicate order',async()=>{
  const db=setup();let checks=0;
  const provider={
    async createLink({id,pack}){return {link_id:id,
      link_url:'https://payments-test.cashfree.com/links/'+id,
      link_currency:'INR',link_amount:pack.amountPaise/100,link_partial_payments:false};},
    async getLink(id){checks++; const row=db.docs.get('cashfree_recharge_orders/'+id);
      return {link_id:id,link_status:'ACTIVE',link_url:'https://payments-test.cashfree.com/links/'+id,
        link_currency:'INR',link_amount:row.amountPaise/100,link_partial_payments:false};}
  };
  const s=createCashfreeRechargeService({db,provider,stamp:()=>1234,isBanned:async()=>false,enabled:true,mode:'sandbox'});
  const opened=await s.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'});
  assert.equal(checks,1);assert.match(opened.checkoutUrl,/payments-test\.cashfree\.com/);
  assert.equal((await s.history({uid:'alice'})).orders.length,1);
});
test('expired provider response cannot report terminal after another worker credited wallet',async()=>{
  const db=setup(),initial=make(db,'ACTIVE');
  const order=await initial.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'});
  const original=db.runTransaction;
  let fired=false;
  db.runTransaction=async fn=>{
    // Simulate a successful parallel payment credit just before terminal tx reads.
    if(!fired){fired=true;
      const row=db.docs.get('cashfree_recharge_orders/'+order.orderId);
      db.docs.set('cashfree_recharge_orders/'+order.orderId,{...row,status:'credited'});
      db.docs.set('wallets/alice',{coins:100});
    }
    return original(fn);
  };
  const stale=make(db,'EXPIRED',{link_amount_paid:0});
  const confirmed=await stale.confirm({uid:'alice',orderId:order.orderId});
  assert.equal(confirmed.ok,true);assert.equal(confirmed.alreadyCredited,true);
  assert.equal(db.docs.get('cashfree_recharge_orders/'+order.orderId).status,'credited');
  assert.equal(db.docs.get('wallets/alice').coins,100);
});
test('resume cannot claim expiry when another worker credited order',async()=>{
  const db=setup(),initial=make(db,'ACTIVE');
  const order=await initial.create({uid:'alice',productId:'king_coins_100',phone:'9876543210'});
  const original=db.runTransaction;
  db.runTransaction=async fn=>{
    const row=db.docs.get('cashfree_recharge_orders/'+order.orderId);
    db.docs.set('cashfree_recharge_orders/'+order.orderId,{...row,status:'credited'});
    return original(fn);
  };
  const stale=make(db,'EXPIRED',{link_amount_paid:0});
  const result=await stale.resume({uid:'alice',orderId:order.orderId});
  assert.equal(result.ok,true);assert.equal(result.status,'credited');
});
