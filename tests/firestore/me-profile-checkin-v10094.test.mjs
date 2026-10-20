import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,getDoc,setDoc,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({projectId:'demo-king-me-privacy-9984',
 firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}});
const Alice=env.authenticatedContext('alice123').firestore();
const Bob=env.authenticatedContext('bob123').firestore();
const root=(db,uid)=>doc(db,'king_daily_checkins',uid);
const event=(db,uid,day)=>doc(db,'king_daily_checkins',uid,'events',day);
try{
 await env.clearFirestore();
 await env.withSecurityRulesDisabled(async context=>{
  await setDoc(root(context.firestore(),'alice123'),{uid:'alice123',lastClaimDay:'2026-10-20',streak:2,totalClaims:4,activityXp:40});
  await setDoc(event(context.firestore(),'alice123','2026-10-20'),{uid:'alice123',day:'2026-10-20',xp:10,streak:2,nonRedeemable:true,claimedAt:serverTimestamp()});
 });
 const own=await assertSucceeds(getDoc(root(Alice,'alice123')));
 if(own.data().activityXp!==40)throw Error('Account lost own check-in progress');
 await assertSucceeds(getDoc(event(Alice,'alice123','2026-10-20')));
 await assertFails(getDoc(root(Bob,'alice123')));
 await assertFails(getDoc(event(Bob,'alice123','2026-10-20')));
 await assertFails(setDoc(root(Alice,'alice123'),{uid:'alice123',activityXp:999999}));
 await assertFails(setDoc(event(Alice,'alice123','2026-10-21'),{uid:'alice123',xp:999999}));
 console.log('PASS check-in history is owner-readable and client writes/currency spoofing are denied');
}finally{await env.cleanup();}
