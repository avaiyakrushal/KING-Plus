import fs from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,updateDoc,getDoc,serverTimestamp,deleteDoc} from 'firebase/firestore';

const env=await initializeTestEnvironment({
 projectId:'demo-king-real-cancel-9947',
 firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}
});
const H=env.authenticatedContext('host').firestore();
const A=env.authenticatedContext('alice').firestore();
const B=env.authenticatedContext('bob').firestore();
const C=env.authenticatedContext('charlie').firestore();
const E=env.authenticatedContext('eve').firestore();
const base='live_rooms/cancel-test-9947';
const room=db=>doc(db,base);
const member=(db,uid)=>doc(db,base,'members',uid);
const state=db=>doc(db,base,'game_state','current');
const move=(db,uid)=>doc(db,base,'game_moves',uid);
const rps=(uid,moveText)=>({
 uid,name:uid,type:'rps',roundId:9947,move:moveText,score:0,
 updatedAt:serverTimestamp()
});
const cancel=(actor='host',result='🚫 Round cancelled • no winner')=>({
 actorUid:actor,status:'closed',cancelled:true,result,
 updatedAt:serverTimestamp()
});
try {
 await env.clearFirestore();
 await assertSucceeds(setDoc(room(H),{
  ownerUid:'host',name:'KING Party Active Round',hasPassword:false,
  isPrivate:false,entranceMode:'free',closed:false
 }));
 await assertSucceeds(setDoc(member(H,'host'),{uid:'host',name:'Host'}));
 await assertSucceeds(setDoc(member(A,'alice'),{uid:'alice',name:'Alice'}));
 await assertSucceeds(setDoc(member(B,'bob'),{uid:'bob',name:'Bob'}));
 await assertSucceeds(setDoc(member(C,'charlie'),{uid:'charlie',name:'Charlie'}));
 await assertSucceeds(setDoc(member(E,'eve'),{uid:'eve',name:'Spectator'}));
 await assertSucceeds(setDoc(state(H),{
  actorUid:'host',actorName:'Host',type:'rps',roundId:9947,
  status:'active',prompt:'Rock Paper Scissors',result:'',
  participantUids:['alice','bob','charlie'],
  updatedAt:serverTimestamp()
 }));
 await assertSucceeds(setDoc(move(A,'alice'),rps('alice','Rock')));
 await assertSucceeds(setDoc(move(B,'bob'),rps('bob','Paper')));
 // Charlie is a frozen Ready player but hasn't submitted. Host must not
 // invent a champion just because two players have already made their moves.
 await assertFails(updateDoc(state(A),cancel('alice')));
 await assertFails(updateDoc(state(E),cancel('eve')));
 await assertFails(updateDoc(state(H),cancel('host','🏆 Alice wins'))); // forged cancellation
 await assertFails(updateDoc(state(H),{...cancel(),status:'active'}));
 await assertFails(updateDoc(state(H),{...cancel(),participantUids:['alice','bob']}));
 await assertFails(updateDoc(state(H),{...cancel(),roundId:9948}));
 // Legitimate Host-initiated abort is a distinct no-winner result.
 await assertSucceeds(updateDoc(state(H),cancel()));
 const after=await assertSucceeds(getDoc(state(B)));
 if(after.data().status!=='closed'||after.data().cancelled!==true||
    after.data().result!=='🚫 Round cancelled • no winner'){
    throw new Error('No-winner cancellation not saved correctly');
 }
 await assertFails(updateDoc(state(H),cancel())); // one-time, not a new result
 await assertFails(updateDoc(state(H),{
   cancelled:false,result:'🏆 Host won',actorUid:'host',
   updatedAt:serverTimestamp()
 })); // cannot rewrite cancelled round
 await assertFails(updateDoc(state(H),{
   status:'closed',result:'🏆 Bob won',actorUid:'host',updatedAt:serverTimestamp()
 }));
 await assertFails(updateDoc(state(A),{
   status:'active',roundId:9948,cancelled:false,actorUid:'alice',updatedAt:serverTimestamp()
 }));
 // A true new round has a new ID, and a clean uncancelled Active state.
 await assertSucceeds(setDoc(state(H),{
   actorUid:'host',actorName:'Host',type:'dice',roundId:9948,
   status:'active',prompt:'Roll 1..6',result:'',
   participantUids:['alice','bob'],updatedAt:serverTimestamp()
 }));
 const rematch=await assertSucceeds(getDoc(state(B)));
 if(rematch.data().roundId!==9948||rematch.data().cancelled===true)
   throw new Error('Rematch should be fresh');
 await assertSucceeds(deleteDoc(move(H,'alice')));
 await assertSucceeds(deleteDoc(move(H,'bob')));
 console.log('PASS Game cancellation: 2/3 ready players, Host only, exact no winner, immutable closure, clean new rematch');
} finally {await env.cleanup();}
