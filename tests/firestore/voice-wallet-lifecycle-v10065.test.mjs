import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,updateDoc,getDoc,deleteDoc,runTransaction,onSnapshot} from 'firebase/firestore';
import fs from 'node:fs';
import assert from 'node:assert/strict';
const env=await initializeTestEnvironment({projectId:'demo-king-voice-wallet-10065',firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}});
const A=env.authenticatedContext('alice').firestore(),B=env.authenticatedContext('bob').firestore();
const room='live_rooms/lifecycle-room',member=doc(A,room,'members','alice');
function watch(db,path,predicate){return new Promise((resolve,reject)=>{
 let off=()=>{};const timeout=setTimeout(()=>{off();reject(new Error('Live snapshot did not arrive'));},10000);
 off=onSnapshot(doc(db,path),d=>{if(predicate(d)){clearTimeout(timeout);off();resolve(d);}},e=>{clearTimeout(timeout);off();reject(e);});
});}
async function publishVoice(session,value){return runTransaction(A,async tx=>{
 const snap=await tx.get(member);
 if(snap.exists()&&snap.data().uid==='alice'&&snap.data().sessionId===session){tx.update(member,{voiceJoined:value});return true;}
 return false;
});}
try{
 await env.clearFirestore();
 await env.withSecurityRulesDisabled(async context=>{
  const d=context.firestore();
  await setDoc(doc(d,room),{ownerUid:'alice',name:'Room',closed:false,locked:false,isPrivate:false,entranceMode:'free'});
  for(const uid of ['alice','bob']){
   await setDoc(doc(d,'public_profiles',uid),{uid,displayName:uid,searchName:uid});
   await setDoc(doc(d,room,'members',uid),{uid,name:uid,sessionId:'visit-1',voiceJoined:false});
  }
  await setDoc(doc(d,'wallets','alice'),{coins:100,giftScore:0,rechargeDiamondsTotal:0});
 });
 const liveName=watch(B,'public_profiles/alice',d=>d.data()?.displayName==='Alice updated');
 await assertSucceeds(updateDoc(doc(A,'public_profiles','alice'),{displayName:'Alice updated',searchName:'alice updated'}));await liveName;
 const liveBalance=watch(A,'wallets/alice',d=>d.data()?.coins===50&&d.data()?.giftScore===25);
 await env.withSecurityRulesDisabled(async c=>updateDoc(doc(c.firestore(),'wallets','alice'),{coins:50,giftScore:25}));await liveBalance;
 await assertFails(getDoc(doc(B,'wallets','alice')));
 await assertFails(updateDoc(doc(A,'wallets','alice'),{coins:10000}));
 await assertFails(updateDoc(doc(A,'public_profiles','alice'),{vipLevel:50}));
 assert.equal(await publishVoice('visit-1',true),true);
 await assertSucceeds(updateDoc(member,{sessionId:'visit-2',voiceJoined:true}));
 assert.equal(await publishVoice('visit-1',false),false);
 assert.equal((await getDoc(member)).data().voiceJoined,true);
 await assertSucceeds(deleteDoc(member));
 assert.equal(await publishVoice('visit-2',false),false);
 assert.equal((await getDoc(member)).exists(),false);
 console.log('PASS live profile and own wallet snapshots, forged VIP/balance rejected, stale voice callbacks cannot overwrite new visit or resurrect left members');
}finally{await env.cleanup();}
