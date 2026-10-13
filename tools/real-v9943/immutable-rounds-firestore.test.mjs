import fs from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,updateDoc,deleteDoc,getDoc,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({
 projectId:'demo-king-immutable-games-9943',
 firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}
});
const H=env.authenticatedContext('host').firestore();
const A=env.authenticatedContext('alice').firestore();
const B=env.authenticatedContext('bob').firestore();
const E=env.authenticatedContext('eve').firestore();
const path='live_rooms/party-turns-9943';
const room=db=>doc(db,path);
const member=(db,uid)=>doc(db,path,'members',uid);
const state=db=>doc(db,path,'game_state','current');
const move=(db,uid)=>doc(db,path,'game_moves',uid);
const play=(uid,type,round,moveLabel,score)=>({
 uid,name:uid,roundId:round,type,move:moveLabel,score,updatedAt:serverTimestamp()
});
try {
 await env.clearFirestore();
 await assertSucceeds(setDoc(room(H),{ownerUid:'host',name:'Real KING Turn Room',
   entranceMode:'free',closed:false,hasPassword:false,isPrivate:false}));
 await assertSucceeds(setDoc(member(H,'host'),{uid:'host',name:'Host'}));
 await assertSucceeds(setDoc(member(A,'alice'),{uid:'alice',name:'Alice'}));
 await assertSucceeds(setDoc(member(B,'bob'),{uid:'bob',name:'Bob'}));
 await assertFails(setDoc(move(A,'alice'),play('alice','rps',1043,'Rock',1))); // no active round
 await assertSucceeds(setDoc(state(H),{
  actorUid:'host',actorName:'Host',type:'rps',status:'active',
  roundId:1043,prompt:'Rock Paper Scissors',result:'',updatedAt:serverTimestamp()
 }));
 await assertFails(setDoc(move(E,'eve'),play('eve','rps',1043,'Scissors',0))); // not a member
 await assertFails(setDoc(move(A,'alice'),play('alice','rps',1042,'Rock',1))); // stale
 await assertFails(setDoc(move(A,'alice'),play('alice','rps',1044,'Rock',1))); // future
 await assertFails(setDoc(move(A,'alice'),play('alice','dice',1043,'Rolled 6',6))); // wrong game
 await assertFails(setDoc(move(A,'bob'),play('bob','rps',1043,'Rock',1))); // impersonation
 await assertSucceeds(setDoc(move(A,'alice'),play('alice','rps',1043,'Rock',1)));
 await assertSucceeds(setDoc(move(B,'bob'),play('bob','rps',1043,'Paper',2)));
 await assertSucceeds(getDoc(move(B,'alice'))); // both players see each other's submission
 await assertFails(updateDoc(move(A,'alice'),{move:'Scissors',score:3,updatedAt:serverTimestamp()}));
 await assertFails(setDoc(move(A,'alice'),play('alice','rps',1043,'Scissors',3))); // overwrite denied
 await assertFails(setDoc(move(B,'bob'),play('bob','rps',1043,'Rock',1))); // no second move
 await assertSucceeds(updateDoc(state(H),{
  status:'closed',result:'🏆 bob won',
  actorUid:'host',updatedAt:serverTimestamp()
 }));
 await assertFails(setDoc(move(H,'host'),play('host','rps',1043,'Scissors',3))); // closed
 await assertSucceeds(deleteDoc(move(H,'alice'))); // moderator can clean up
 await assertFails(setDoc(move(A,'alice'),play('alice','rps',1043,'Rock',1))); // still closed
 await assertSucceeds(updateDoc(state(H),{
  type:'dice',roundId:1044,status:'active',result:'',actorUid:'host',updatedAt:serverTimestamp()
 }));
 await assertFails(setDoc(move(A,'alice'),play('alice','rps',1044,'Rock',1))); // previous game
 await assertSucceeds(setDoc(move(A,'alice'),play('alice','dice',1044,'Rolled 4',4)));
 await assertSucceeds(getDoc(move(B,'alice')));
 console.log('PASS 2-user RPS and Dice: authentic immutable move, exact round/type, no closed/stale/wrong player');
} finally {await env.cleanup();}
