'use strict';
const test=require('node:test'),assert=require('node:assert/strict');
const {createVipStyleService}=require('./vip-style-service');
class FakeHttpsError extends Error{constructor(code,msg){super(msg);this.code=code;}}
const SERVER_TIME={server:true};
const FieldValue={serverTimestamp:()=>SERVER_TIME,arrayUnion:(...xs)=>({union:xs})};
function fakeDB(){
  const documents=new Map();
  const ref=path=>({path});
  const db={collection:name=>({doc:id=>ref(name+'/'+id)}),runTransaction:async fn=>{
    const writes=[];
    const tx={get:async r=>({exists:documents.has(r.path),
      data:()=>structuredClone(documents.get(r.path)),
      get:k=>(documents.get(r.path)||{})[k]}),
      set:(r,v,opts)=>writes.push({type:'set',r,v,opts}),
      update:(r,v)=>writes.push({type:'update',r,v}),
      create:(r,v)=>writes.push({type:'create',r,v})};
    const result=await fn(tx);
    for(const w of writes){
      let existing=documents.get(w.r.path)||{};
      if(w.type==='create'&&documents.has(w.r.path))throw new Error('Document already exists');
      if(w.type==='update'&&!documents.has(w.r.path))throw new Error('Missing doc');
      let incoming={};for(const [k,v] of Object.entries(w.v))incoming[k]=v&&v.union?[...(existing[k]||[]),...v.union]:v;
      documents.set(w.r.path,w.opts?.merge||w.type==='update'?{...existing,...incoming}:incoming);
    }
    return result;
  }};
  return {db,documents};
}
function setup(){let {db,documents}=fakeDB();
  documents.set('king_vip_style_catalog/current',{enabled:true,paymentMode:'verified_diamonds',vipNumberDiamonds:500,nameColorDiamonds:100});
  documents.set('wallets/alice',{coins:1000});documents.set('wallets/bob',{coins:1000});
  const purchase=createVipStyleService({db,FieldValue,HttpsError:FakeHttpsError,requireAuth:r=>r.auth.uid,isBanned:async uid=>false});
  const req=(uid,product,selection,requestId)=>({auth:{uid,token:{}},data:{product,requestId,[product==='vip_number'?'number':'color']:selection}});
  return {documents,purchase,req};
}
test('VIP number debits once and reserves unique ID',async()=>{
  let {documents,purchase,req}=setup();
  let r=await purchase(req('alice','vip_number','888888','vip_11111'));
  assert.equal(r.balance,500);assert.equal(r.idempotent,false);
  assert.equal(documents.get('king_vip_number_registry/888888').uid,'alice');
  assert.equal(documents.get('king_vip_identity_grants/alice').vipNumber,'888888');
  const replay=await purchase(req('alice','vip_number','888888','vip_11111'));
  assert.equal(replay.idempotent,true);assert.equal(documents.get('wallets/alice').coins,500);
  await assert.rejects(()=>purchase(req('bob','vip_number','888888','vip_22222')),{code:'already-exists'});
  assert.equal(documents.get('wallets/bob').coins,1000);
});
test('cannot buy a second VIP number or spoof a replay',async()=>{
  const {purchase,req}=setup();await purchase(req('alice','vip_number','777777','vip_11111'));
  await assert.rejects(()=>purchase(req('alice','vip_number','999999','vip_33333')),{code:'already-exists'});
  await assert.rejects(()=>purchase(req('alice','name_color','gold','vip_11111')),{code:'already-exists'});
});
test('color purchase stores a server grant and owned color',async()=>{
  const {documents,purchase,req}=setup();await purchase(req('alice','name_color','gold','color_12345678'));
  assert.equal(documents.get('wallets/alice').coins,900);
  assert.equal(documents.get('king_vip_identity_grants/alice').nameColor,'gold');
  assert.deepEqual(documents.get('king_vip_identity_grants/alice').ownedColors,['gold']);
  await assert.rejects(()=>purchase(req('alice','name_color','gold','color_23456789')),{code:'already-exists'});
  await purchase(req('alice','name_color','rose','color_34567890'));
  assert.deepEqual(documents.get('king_vip_identity_grants/alice').ownedColors,['gold','rose']);
});
test('disabled shop and insufficient balance cannot grant',async()=>{
  const {documents,purchase,req}=setup();documents.get('king_vip_style_catalog/current').enabled=false;
  await assert.rejects(()=>purchase(req('alice','vip_number','123456','vip_11111')),{code:'failed-precondition'});
  assert.equal(documents.has('king_vip_identity_grants/alice'),false);
  documents.get('king_vip_style_catalog/current').enabled=true;documents.get('wallets/alice').coins=0;
  await assert.rejects(()=>purchase(req('alice','vip_number','123456','vip_12345')),{code:'failed-precondition'});
  assert.equal(documents.has('king_vip_number_registry/123456'),false);
});
test('input cannot choose price, bad UID, arbitrary color or anonymous account',async()=>{
  const {purchase,req}=setup();
  let r=req('alice','vip_number','123456','vip_11111');r.data.price=1;
  const out=await purchase(r);assert.equal(out.balance,500);
  await assert.rejects(()=>purchase(req('bob','name_color','#000000','bad_11111')),{code:'invalid-argument'});
  let anon=req('bob','name_color','gold','color_88888888');anon.auth.token.firebase={sign_in_provider:'anonymous'};
  await assert.rejects(()=>purchase(anon),{code:'unauthenticated'});
});
