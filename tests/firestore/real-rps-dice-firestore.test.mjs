import fs from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,getDoc,updateDoc,deleteDoc,serverTimestamp} from 'firebase/firestore';

const env=await initializeTestEnvironment({
 projectId:'demo-king-real-rps-dice-9945',
 firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}
});
const H=env.authenticatedContext('host').firestore();
const A=env.authenticatedContext('alice').firestore();
const B=env.authenticatedContext('bob').firestore();
const E=env.authenticatedContext('eve').firestore();
const root='live_rooms/real-match-9945';
const room=db=>doc(db,root);
const member=(db,uid)=>doc(db,root,'members',uid);
const state=db=>doc(db,root,'game_state','current');
const move=(db,uid)=>doc(db,root,'game_moves',uid);
const submit=(uid,type,round,text,score)=>({
 uid,name:uid,type,roundId:round,move:text,score,updatedAt:serverTimestamp()
});
try{
 await env.clearFirestore();
 await assertSucceeds(setDoc(room(H),{
   ownerUid:'host',name:'KING RPS/Dice Game',isPrivate:false,hasPassword:false,
   entranceMode:'free',closed:false
 }));
 await assertSucceeds(setDoc(member(H,'host'),{uid:'host',name:'Host'}));
 await assertSucceeds(setDoc(member(A,'alice'),{uid:'alice',name:'Alice'}));
 await assertSucceeds(setDoc(member(B,'bob'),{uid:'bob',name:'Bob'}));
 await assertSucceeds(setDoc(state(H),{
   actorUid:'host',actorName:'Host',type:'rps',status:'active',
   roundId:1045,prompt:'Rock/Paper/Scissors',result:'',updatedAt:serverTimestamp()
 }));
 await assertFails(setDoc(move(E,'eve'),submit('eve','rps',1045,'Rock',0))); // outsider
 await assertFails(setDoc(move(A,'alice'),submit('alice','rps',1045,'Dragon',0)));
 await assertFails(setDoc(move(A,'alice'),submit('alice','rps',1045,'rock',0)));
 await assertFails(setDoc(move(A,'alice'),submit('alice','rps',1045,'Rock',9000)));
 await assertFails(setDoc(move(A,'alice'),submit('alice','rps',1045,'Rock',1)));
 await assertFails(setDoc(move(A,'alice'),submit('alice','rps',1044,'Rock',0))); // wrong round
 await assertSucceeds(setDoc(move(A,'alice'),submit('alice','rps',1045,'Rock',0)));
 await assertSucceeds(setDoc(move(B,'bob'),submit('bob','rps',1045,'Scissors',0)));
 await assertSucceeds(getDoc(move(A,'bob')));
 await assertFails(updateDoc(move(B,'bob'),{move:'Paper'})); // immutable
 await assertFails(deleteDoc(move(B,'bob'))); // cannot re-submit
 await assertSucceeds(updateDoc(state(H),{
   type:'dice',roundId:1046,status:'active',
   actorUid:'host',result:'',updatedAt:serverTimestamp()
 }));
 await assertSucceeds(deleteDoc(move(H,'alice')));
 await assertSucceeds(deleteDoc(move(H,'bob')));
 await assertFails(setDoc(move(A,'alice'),submit('alice','dice',1046,'Rolled 999',999)));
 await assertFails(setDoc(move(A,'alice'),submit('alice','dice',1046,'Rolled 7',7)));
 await assertFails(setDoc(move(A,'alice'),submit('alice','dice',1046,'Rolled 6',1))); // mismatch
 await assertFails(setDoc(move(A,'alice'),submit('alice','dice',1046,'Rolled 0',0)));
 await assertFails(setDoc(move(A,'alice'),submit('alice','dice',1046,'6',6)));
 await assertSucceeds(setDoc(move(A,'alice'),submit('alice','dice',1046,'Rolled 3',3)));
 await assertSucceeds(setDoc(move(B,'bob'),submit('bob','dice',1046,'Rolled 6',6)));
 await assertSucceeds(getDoc(move(A,'bob')));
 await assertSucceeds(updateDoc(state(H),{
   type:'coin',roundId:1047,status:'active',actorUid:'host',result:'',updatedAt:serverTimestamp()
 }));
 await assertSucceeds(deleteDoc(move(H,'alice')));
 await assertSucceeds(deleteDoc(move(H,'bob')));
 await assertFails(setDoc(move(A,'alice'),submit('alice','coin',1047,'Heads',999)));
 await assertFails(setDoc(move(A,'alice'),submit('alice','coin',1047,'Side',1)));
 await assertFails(setDoc(move(A,'alice'),submit('alice','coin',1047,'Tails',1)));
 await assertSucceeds(setDoc(move(A,'alice'),submit('alice','coin',1047,'Heads',1)));
 await assertSucceeds(setDoc(move(B,'bob'),submit('bob','coin',1047,'Tails',0)));
 console.log('PASS RPS real 2-player valid moves, fake/forged blocked; Dice 1-6 label+score match; Coin valid; member+round+immutability');
} finally {await env.cleanup();}
