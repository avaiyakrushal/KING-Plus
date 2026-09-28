'use strict';
const {test,beforeEach}=require('node:test');
const assert=require('node:assert/strict');
const Module=require('node:module');
// In-memory transaction double exercises the exported callable handlers.
// It serializes transactions and stages writes, including rollback on error.
const records=new Map();let queue=Promise.resolve(),seq=0;
const snap=ref=>({exists:records.has(ref.path),id:ref.id,ref,data:()=>records.get(ref.path),get:k=>records.get(ref.path)?.[k]});
function ref(path){return {path,id:path.split('/').pop(),collection:n=>collection(path+'/'+n),get:async()=>snap(ref(path)),set:async v=>write(path,v,false),delete:async()=>records.delete(path),update:async v=>{if(!records.has(path))throw Error('not-found');write(path,v,true);}};}
function write(path,v,merge){const old=records.get(path)||{},out=merge?{...old}:{};for(const[k,x]of Object.entries(v))out[k]=x&&x.increment!==undefined?(old[k]||0)+x.increment:x;records.set(path,out);}
function collection(path){return {doc:id=>ref(path+'/'+(id||'auto'+ ++seq)),where(){return this;},orderBy(){return this;},limit(){return this;},get:async()=>({docs:[],empty:true})};}
const db={doc:ref,collection,runTransaction:fn=>{
  const run=queue.then(async()=>{const staged=[];let wrote=false;const tx={get:async r=>{assert.equal(wrote,false,'Firestore requires reads before writes');return snap(r);},create:(r,v)=>{wrote=true;assert.equal(records.has(r.path),false);staged.push(()=>write(r.path,v,false));},set:(r,v)=>{wrote=true;staged.push(()=>write(r.path,v,false));},update:(r,v)=>{wrote=true;staged.push(()=>write(r.path,v,true));}};const result=await fn(tx);for(const write of staged)write();return result;});queue=run.catch(()=>{});return run;
}};
class HttpsError extends Error {constructor(code,message){super(message);this.code=code;}}
const original=Module._load;
Module._load=function(name,...rest){
  if(name==='firebase-functions/v2/https')return {onCall:(_,fn)=>fn,HttpsError};
  if(name==='firebase-functions/v2/firestore')return {onDocumentCreated:(_,fn)=>fn};
  if(name==='firebase-admin/app')return {initializeApp(){}};
  if(name==='firebase-admin/firestore')return {getFirestore:()=>db,FieldValue:{increment:n=>({increment:n}),serverTimestamp:()=>({toMillis:()=>1})}};
  if(name==='firebase-admin/messaging')return {getMessaging:()=>({})};
  return original.call(this,name,...rest);
};
const api=require('../index');Module._load=original;
const auth=(uid,admin=false)=>({uid,token:{admin,firebase:{sign_in_provider:'google.com'}}});
const call=(fn,uid,data={},admin=false)=>api[fn]({auth:uid?auth(uid,admin):null,data});
async function setup(){await call('kpBootstrap','alice');await call('kpBootstrap','bob');await call('kpDailyReward','alice');}
beforeEach(()=>{records.clear();seq=0;});
test('requires real authenticated account and rejects anonymous',async()=>{
  await assert.rejects(call('kpWallet',null),{code:'unauthenticated'});
  await assert.rejects(api.kpBootstrap({auth:{uid:'test',token:{firebase:{sign_in_provider:'anonymous'}}}}),{code:'unauthenticated'});
});
test('bootstrap never resets or imports a local balance',async()=>{await setup();await call('kpBootstrap','alice',{balance:999999});assert.equal(records.get('kpAccounts/alice').balance,100);});
test('concurrent daily claims grant only once',async()=>{await call('kpBootstrap','alice');await Promise.all(Array.from({length:8},()=>call('kpDailyReward','alice')));assert.equal(records.get('kpAccounts/alice').balance,100);});
test('concurrent duplicate gifts debit and notify exactly once',async()=>{
  await setup();const request={to:'bob',gift:'cake',requestId:'one'};await Promise.all(Array.from({length:5},()=>call('kpSendGift','alice',request)));
  assert.equal(records.get('kpAccounts/alice').balance,50);assert.equal(records.get('kpAccounts/bob').receivedCoins,50);assert.equal(records.get('kpAccounts/bob').balance,0);
  assert.equal([...records.keys()].filter(k=>k.startsWith('kpAccounts/bob/inbox/')).length,1);
});
test('concurrent spends cannot overdraw wallet',async()=>{await setup();const outcomes=await Promise.allSettled([call('kpSendGift','alice',{to:'bob',gift:'diamond',requestId:'a'}),call('kpSendGift','alice',{to:'bob',gift:'diamond',requestId:'b'})]);assert.equal(outcomes.filter(x=>x.status==='fulfilled').length,1);assert.equal(records.get('kpAccounts/alice').balance,0);});
test('reusing request ID with different content fails',async()=>{await setup();await call('kpSendGift','alice',{to:'bob',gift:'rose',requestId:'same'});await assert.rejects(call('kpSendGift','alice',{to:'bob',gift:'cake',requestId:'same'}),{code:'already-exists'});});
test('blocked gifts are rejected in both directions',async()=>{await setup();await call('kpSetBlock','bob',{target:'alice',blocked:true});await assert.rejects(call('kpSendGift','alice',{to:'bob',gift:'rose',requestId:'x'}),{code:'permission-denied'});await assert.rejects(call('kpSendGift','bob',{to:'alice',gift:'rose',requestId:'y'}),{code:'permission-denied'});assert.equal(records.get('kpAccounts/alice').balance,100);});
test('cannot choose own price, unknown catalog or self gift',async()=>{await setup();await call('kpSendGift','alice',{to:'bob',gift:'cake',price:0,requestId:'ok'});assert.equal(records.get('kpAccounts/alice').balance,50);await assert.rejects(call('kpSendGift','alice',{to:'bob',gift:'invalid',requestId:'bad'}),{code:'invalid-argument'});await assert.rejects(call('kpSendGift','alice',{to:'alice',gift:'rose',requestId:'self'}),{code:'invalid-argument'});});
test('report deduplicates and limits abuse',async()=>{await setup();const data={target:'bob',kind:'user',reason:'Spam',requestId:'r'};const a=await call('kpReport','alice',data),b=await call('kpReport','alice',data);assert.equal(a.reportId,b.reportId);for(let i=0;i<9;i++)await call('kpReport','alice',{...data,requestId:'r'+i});await assert.rejects(call('kpReport','alice',{...data,requestId:'over'}),{code:'resource-exhausted'});});
test('non-admin cannot review, suspend or grant itself admin',async()=>{await setup();await assert.rejects(call('kpAdminReports','alice',{admin:true}),{code:'permission-denied'});await assert.rejects(call('kpResolveReport','alice',{id:'x',action:'suspend',note:'test',admin:true}),{code:'permission-denied'});});
test('admin resolution is audited and suspended account loses access',async()=>{await setup();const r=await call('kpReport','alice',{target:'bob',kind:'user',reason:'Spam',requestId:'r'});await call('kpResolveReport','alice',{id:r.reportId,action:'suspend',note:'Reviewed abuse'},true);assert.equal(records.get('kpAccounts/bob').suspended,true);assert.equal([...records.keys()].filter(k=>k.startsWith('kpAudit/')).length,1);await assert.rejects(call('kpWallet','bob'),{code:'permission-denied'});});
test('account IDs cannot inject collection paths',async()=>{await setup();await assert.rejects(call('kpSetBlock','alice',{target:'bob/other',blocked:true}),{code:'invalid-argument'});});
