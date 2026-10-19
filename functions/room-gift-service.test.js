'use strict';
const {test}=require('node:test');
const assert=require('node:assert/strict');
const {createGiftService}=require('./room-gift-service');
// Transaction harness: isolate writes until commit, serialize contenders and
// deliberately rerun the callback once to model Firestore optimistic retries.
class Database {
 constructor(){this.data=new Map();this.tail=Promise.resolve();this.retry=false;}
 collection(path){return {doc:name=>this.ref(path+'/'+name)};}
 ref(path){return {path,collection:name=>this.collection(path+'/'+name)};}
 async runTransaction(callback){
  let release;const turn=new Promise(r=>release=r);const prev=this.tail;this.tail=turn;await prev;
  try{
   const run=async()=>{const writes=[];let wrote=false;const result=await callback({
    get:async ref=>{assert.equal(wrote,false,'transaction read after write');const value=this.data.get(ref.path);return {exists:value!==undefined,data:()=>value,get:key=>value?.[key]};},
    set:(ref,value,opts)=>{wrote=true;writes.push([ref.path,value,opts]);}
   });return {result,writes};};
   if(this.retry)await run();const {result,writes}=await run();
   for(const [path,value,opts] of writes)this.data.set(path,opts?.merge?{...this.data.get(path),...value}:value);
   return result;
  }finally{release();}
 }
}
function setup(){
 const db=new Database();
 for(const uid of ['sender','receiver']){
  db.data.set('public_profiles/'+uid,{displayName:uid});
  db.data.set('live_rooms/room/members/'+uid,{uid});
 }
 db.data.set('live_rooms/room',{closed:false});
 db.data.set('wallets/sender',{coins:1000,vipPoints:42,rechargeDiamondsTotal:42});
 db.data.set('wallets/receiver',{coins:9,giftScore:10});
 const send=createGiftService({db,timestamp:()=>12345});
 const request={senderUid:'sender',targetUid:'receiver',giftName:'Gold Rose x3',cost:150,requestId:'request-1',roomId:'room'};
 return {db,send,request};
}
test('one atomic commit debits, credits recognition, records history and publishes room gift/chat',async()=>{
 const {db,send,request}=setup();const result=await send(request);
 assert.equal(db.data.get('wallets/sender').coins,850);
 assert.equal(db.data.get('wallets/sender').vipPoints,42);
 assert.equal(db.data.get('wallets/receiver').coins,9);
 assert.equal(db.data.get('wallets/receiver').giftScore,160);
 assert.equal(db.data.get('wallet_ledger/'+result.giftId).quantity,3);
 assert.equal(db.data.get('live_rooms/room/events/'+result.giftId).giftValue,150);
 assert.equal(db.data.get('live_rooms/room/messages/'+result.giftId).senderUid,'sender');
});
test('concurrent retries after lost response charge once and return same event',async()=>{
 const {db,send,request}=setup();db.retry=true;
 const results=await Promise.all(Array.from({length:10},()=>send(request)));
 assert.equal(new Set(results.map(x=>x.giftId)).size,1);
 assert.equal(results.filter(x=>!x.idempotent).length,1);
 assert.equal(db.data.get('wallets/sender').coins,850);assert.equal(db.data.get('wallets/receiver').giftScore,160);
});
for(const change of [{targetUid:'other'},{cost:50,giftName:'Gold Rose x1'},{roomId:null}])
 test('request ID reuse with changed purchase rejected '+JSON.stringify(change),async()=>{
  const {db,send,request}=setup();await send(request);
  await assert.rejects(send({...request,...change}),{code:'already-exists'});assert.equal(db.data.get('wallets/sender').coins,850);
 });
for(const change of [{cost:1},{cost:'150'},{giftName:'Unknown x3'},{giftName:'Gold Rose x99'},{requestId:''},{roomId:""},{targetUid:'sender'}])
 test('invalid catalog, price or identity rejected '+JSON.stringify(change),async()=>{
  const {db,send,request}=setup();await assert.rejects(send({...request,...change}));assert.equal(db.data.get('wallets/sender').coins,1000);
 });
for(const scenario of ['closed','left','blocked','banned','insufficient','overflow'])
 test('no partial balance, history or event writes: '+scenario,async()=>{
  const {db,send,request}=setup();
  if(scenario==='closed')db.data.set('live_rooms/room',{closed:true});
  if(scenario==='left')db.data.delete('live_rooms/room/members/receiver');
  if(scenario==='blocked')db.data.set('blocks/receiver_sender',{});
  if(scenario==='banned')db.data.set('bans/sender',{active:true});
  if(scenario==='insufficient')db.data.set('wallets/sender',{coins:100});
  if(scenario==='overflow')db.data.set('wallets/receiver',{giftScore:1000000000});
  const before=JSON.stringify([...db.data]);await assert.rejects(send(request));assert.equal(JSON.stringify([...db.data]),before);
 });
test('direct profile gift shares the same catalog and history without room event',async()=>{
 const {db,send,request}=setup();const result=await send({...request,roomId:null});
 assert.equal(result.ok,true);assert.equal(db.data.get('wallets/sender').coins,850);
 assert.equal(db.data.has('live_rooms/room/events/'+result.giftId),false);
});
