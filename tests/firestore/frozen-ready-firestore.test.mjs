import fs from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,updateDoc,deleteDoc,getDoc,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({
 projectId:'demo-king-frozen-roster-9946',
 firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}
});
const H=env.authenticatedContext('host').firestore();
const A=env.authenticatedContext('alice').firestore();
const B=env.authenticatedContext('bob').firestore();
const C=env.authenticatedContext('charlie').firestore();
const E=env.authenticatedContext('eve').firestore();
const root='live_rooms/ready-room-9946';
const room=db=>doc(db,root);
const member=(db,uid)=>doc(db,root,'members',uid);
const state=db=>doc(db,root,'game_state','current');
const move=(db,uid)=>doc(db,root,'game_moves',uid);
const vote=(uid,game,round,txt,score)=>({
 uid,name:uid,type:game,roundId:round,move:txt,score,updatedAt:serverTimestamp()
});
const initial=(game,round,players)=>({
 type:game,roundId:round,actorUid:'host',actorName:'Host',prompt:game,
 status:'active',result:'',participantUids:players,updatedAt:serverTimestamp()
});
try {
 await env.clearFirestore();
 await assertSucceeds(setDoc(room(H),{ownerUid:'host',name:'KING Real Ready Game',isPrivate:false,
   hasPassword:false,entranceMode:'free',closed:false}));
 await assertSucceeds(setDoc(member(H,'host'),{uid:'host',name:'Host'}));
 await assertSucceeds(setDoc(member(A,'alice'),{uid:'alice',name:'Alice'}));
 await assertSucceeds(setDoc(member(B,'bob'),{uid:'bob',name:'Bob'}));
 await assertSucceeds(setDoc(member(C,'charlie'),{uid:'charlie',name:'Charlie'}));
 await assertSucceeds(setDoc(member(E,'eve'),{uid:'eve',name:'Eve'}));
 // Three players pressed Ready; Eve is only a Party spectator.
 await assertSucceeds(setDoc(state(H),initial('rps',9946,['alice','bob','charlie'])));
 await assertFails(setDoc(move(E,'eve'),vote('eve','rps',9946,'Rock',0)));
 await assertFails(setDoc(move(H,'host'),vote('host','rps',9946,'Paper',0))); // even host cannot fake being ready
 await assertSucceeds(setDoc(move(A,'alice'),vote('alice','rps',9946,'Rock',0)));
 await assertSucceeds(setDoc(move(B,'bob'),vote('bob','rps',9946,'Paper',0)));
 await assertSucceeds(getDoc(move(C,'alice'))); // online shared room game
 await assertFails(updateDoc(state(H),{participantUids:['alice','bob'],actorUid:'host',
   updatedAt:serverTimestamp()})); // host cannot shrink roster to force 2-player victory
 await assertFails(updateDoc(state(H),{participantUids:['alice','bob','eve'],actorUid:'host',
   updatedAt:serverTimestamp()}));
 await assertSucceeds(setDoc(move(C,'charlie'),vote('charlie','rps',9946,'Scissors',0)));
 await assertSucceeds(updateDoc(state(H),{status:'closed',result:'🤝 3 moves draw',
   actorUid:'host',updatedAt:serverTimestamp()}));
 // Once settled, starting a new game may freeze a new Ready roster.
 await assertSucceeds(updateDoc(state(H),{
   type:'dice',status:'active',roundId:9947,result:'',actorUid:'host',
   participantUids:['alice','bob'],updatedAt:serverTimestamp()
 }));
 await assertSucceeds(deleteDoc(move(H,'alice')));
 await assertSucceeds(deleteDoc(move(H,'bob')));
 await assertSucceeds(deleteDoc(move(H,'charlie')));
 await assertFails(setDoc(move(C,'charlie'),vote('charlie','dice',9947,'Rolled 2',2)));
 await assertSucceeds(setDoc(move(A,'alice'),vote('alice','dice',9947,'Rolled 4',4)));
 await assertSucceeds(setDoc(move(B,'bob'),vote('bob','dice',9947,'Rolled 6',6)));
 await assertFails(updateDoc(state(H),{
   participantUids:['alice','bob','charlie'],actorUid:'host',updatedAt:serverTimestamp()
 }));
 console.log('PASS Frozen Ready Party: non-ready users/host cannot submit, Host cannot shrink/rewrite roster, new round can refreeze');
}finally{await env.cleanup();}
