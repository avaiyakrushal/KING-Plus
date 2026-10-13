import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,updateDoc,deleteDoc,serverTimestamp,Timestamp,getDoc} from 'firebase/firestore';
import fs from 'node:fs';

const env=await initializeTestEnvironment({
  projectId:'demo-king-sheep-9940',
  firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}
});
const H=env.authenticatedContext('host').firestore();
const A=env.authenticatedContext('alice').firestore();
const B=env.authenticatedContext('bob').firestore();
const E=env.authenticatedContext('eve').firestore();
const room=(db)=>doc(db,'live_rooms','sheep-9940');
const member=(db,uid)=>doc(db,'live_rooms','sheep-9940','members',uid);
const state=(db)=>doc(db,'live_rooms','sheep-9940','game_state','current');
const start=(db,uid)=>doc(db,'live_rooms','sheep-9940','sheep_starts',uid);
const move=(db,uid)=>doc(db,'live_rooms','sheep-9940','game_moves',uid);
const startData=(uid,round)=>({uid,roundId:round,startedAt:serverTimestamp()});
const race=(uid,round,score,text='Sheep sprint '+score+' taps in 8s')=>({
 uid,name:uid,roundId:round,type:'sheep',move:text,score,updatedAt:serverTimestamp()
});
try {
 await env.clearFirestore();
 await assertSucceeds(setDoc(room(H),{
  ownerUid:'host',name:'KING Sheep Battle',isPrivate:false,hasPassword:false,entranceMode:'free',closed:false
 }));
 await assertSucceeds(setDoc(member(H,'host'),{uid:'host',name:'host'}));
 await assertSucceeds(setDoc(member(A,'alice'),{uid:'alice',name:'alice'}));
 await assertSucceeds(setDoc(member(B,'bob'),{uid:'bob',name:'bob'}));
 await assertSucceeds(setDoc(state(H),{
  actorUid:'host',actorName:'Host',type:'sheep',status:'active',roundId:1040,
  prompt:'Run for 8 seconds',result:'',updatedAt:serverTimestamp()
 }));
 await assertFails(setDoc(move(A,'alice'),race('alice',1040,45))); // no started race
 await assertFails(setDoc(start(E,'eve'),startData('eve',1040))); // not in Party
 await assertFails(setDoc(start(A,'bob'),startData('bob',1040))); // owner mismatch
 await assertFails(setDoc(start(A,'alice'),startData('alice',1039))); // old round
 await assertSucceeds(setDoc(start(A,'alice'),startData('alice',1040)));
 await assertFails(setDoc(move(A,'alice'),race('alice',1040,45))); // within 8 seconds, reject
 await assertFails(updateDoc(start(A,'alice'),startData('alice',1040))); // no same-round timer reset
 await env.withSecurityRulesDisabled(async ctx=>{
  await setDoc(start(ctx.firestore(),'alice'),{uid:'alice',roundId:1040,
   startedAt:Timestamp.fromMillis(Date.now()-10000)});
  await setDoc(start(ctx.firestore(),'bob'),{uid:'bob',roundId:1040,
   startedAt:Timestamp.fromMillis(Date.now()-12000)});
 });
 await assertFails(setDoc(move(A,'alice'),race('alice',1040,201))); // max 200
 await assertFails(setDoc(move(A,'alice'),race('alice',1040,-2)));
 await assertFails(setDoc(move(A,'alice'),race('alice',1040,45,'Fake winner 💎999')));
 await assertFails(setDoc(move(A,'alice'),{...race('alice',1040,43),type:'dice'})); // cannot spoof another type
 await assertFails(setDoc(move(A,'alice'),race('alice',1041,40))); // stale/future round
 await assertSucceeds(setDoc(move(A,'alice'),race('alice',1040,43))); // true started client time
 await assertSucceeds(setDoc(move(B,'bob'),race('bob',1040,25))); // second signed-in player
 await assertSucceeds(getDoc(move(B,'alice'))); // both see shared result
 await assertSucceeds(updateDoc(state(H),{
  status:'closed',result:'Sheep winner Alice',
  actorUid:'host',updatedAt:serverTimestamp()
 }));
 await assertFails(setDoc(move(E,'eve'),race('eve',1040,55)));
 await assertFails(setDoc(start(B,'bob'),startData('bob',1041))); // closed game cannot start
 await assertSucceeds(deleteDoc(member(H,'alice')));
 await assertFails(getDoc(move(A,'bob'))); // membership revoked by Firestore
 console.log('PASS two real Sheep players, >=8s server clock, fake score blocked, member/round validation and revocation');
} finally {
 await env.cleanup();
}
