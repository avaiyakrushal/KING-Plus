'use strict';
const {test}=require('node:test');
const assert=require('node:assert/strict');
const {createDailyCheckinService,todayInKolkata,dayOrdinal}=require('./daily-checkin-service');
class Database{
 constructor(){this.data=new Map();this.tail=Promise.resolve();}
 collection(path){return{doc:name=>this.ref(path+'/'+name)};}
 ref(path){return{path,collection:name=>this.collection(path+'/'+name),get:async()=>{const v=this.data.get(path);return{exists:v!==undefined,data:()=>v};}};}
 async runTransaction(callback){let unlock;const gate=new Promise(r=>unlock=r),prior=this.tail;this.tail=gate;await prior;try{
  const writes=[];let wrote=false;const result=await callback({get:async ref=>{assert.equal(wrote,false,'all transaction reads precede writes');const v=this.data.get(ref.path);return{exists:v!==undefined,data:()=>v};},set:(ref,data,opts)=>{wrote=true;writes.push([ref.path,data,opts]);}});
  for(const [path,data,opts] of writes)this.data.set(path,opts?.merge?{...this.data.get(path),...data}:data);return result;
 }finally{unlock();}}
}
function setup(day){const db=new Database();let stamp=0;const service=createDailyCheckinService({db,timestamp:()=>++stamp,today:()=>day.current});return{db,service,day};}
test('claim grants one nonredeemable activity XP receipt and current streak',async()=>{
 const day={current:'2026-10-20'}, {db,service}=setup(day);const out=await service.claim({uid:'memberA'});
 assert.deepEqual([out.claimed,out.day,out.streak,out.totalClaims,out.todayXp],[true,'2026-10-20',1,1,10]);
 assert.equal(db.data.get('king_daily_checkins/memberA').activityXp,10);
 assert.equal(db.data.get('king_daily_checkins/memberA/events/2026-10-20').nonRedeemable,true);
});
test('duplicate same-day and concurrent claims never award twice',async()=>{
 const day={current:'2026-10-20'}, {db,service}=setup(day);
 const results=await Promise.all(Array.from({length:8},()=>service.claim({uid:'memberA'})));
 assert.equal(results.filter(x=>x.claimed).length,1);assert.equal(db.data.get('king_daily_checkins/memberA').activityXp,10);
 const again=await service.claim({uid:'memberA'});assert.equal(again.claimed,false);assert.equal(again.totalClaims,1);
});
test('check-in streak increments only for consecutive Kolkata calendar days',async()=>{
 const day={current:'2026-10-20'}, {db,service}=setup(day);await service.claim({uid:'memberA'});
 day.current='2026-10-21';const next=await service.claim({uid:'memberA'});assert.equal(next.streak,2);assert.equal(next.activityXp,20);
 day.current='2026-10-23';const missed=await service.claim({uid:'memberA'});assert.equal(missed.streak,1);assert.equal(missed.totalClaims,3);
});
test('status returns account-scoped values and no claim when absent',async()=>{
 const day={current:'2026-10-20'}, {service}=setup(day);const out=await service.status({uid:'memberA'});
 assert.deepEqual([out.claimedToday,out.streak,out.totalClaims,out.activityXp],[false,0,0,0]);
});
test('rejects missing identity and malformed dates',async()=>{
 const day={current:'2026-10-20'}, {service}=setup(day);
 await assert.rejects(service.claim({uid:''}),{code:'unauthenticated'});
 assert.equal(dayOrdinal('2026-02-30'),null);assert.equal(dayOrdinal('2026-03-01')-dayOrdinal('2026-02-28'),1);
});
test('Kolkata date uses India local day boundary',()=>{
 assert.equal(todayInKolkata(new Date('2026-10-19T19:00:00Z')),'2026-10-20');
});
