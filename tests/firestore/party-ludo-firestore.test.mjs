import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import fs from 'node:fs';
import {doc,setDoc,getDoc,updateDoc,serverTimestamp,deleteDoc} from 'firebase/firestore';

const env=await initializeTestEnvironment({
 projectId:'demo-king-party-ludo-9939',
 firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}
});
const H=env.authenticatedContext('host').firestore();
const A=env.authenticatedContext('alice').firestore();
const E=env.authenticatedContext('eve').firestore();
const R='party-ludo-9939', code='LUD039';
const room=db=>doc(db,'live_rooms',R);
const member=(db,uid)=>doc(db,'live_rooms',R,'members',uid);
const match=db=>doc(db,'ludo_matches',code);
const fresh=(actor,party=R)=>({
 schema:1,code,ownerUid:actor,actorUid:actor,phase:'lobby',capacity:4,revision:0,action:'create',selected:-1,pieces:Array(16).fill(-1),turn:0,dice:0,sixes:0,winner:-1,rollAt:serverTimestamp(),
 players:[actor,'','',''],ready:[false,false,false,false],
 names:[actor,'','',''],partyRoomKey:party,updatedAt:serverTimestamp()
});
const join=(actor)=>({actorUid:actor,action:'join',selected:1,players:['host',actor,'',''],ready:[false,false,false,false],
 names:['host',actor,'',''],revision:1,updatedAt:serverTimestamp()});
try {
 await env.clearFirestore();
 await assertSucceeds(setDoc(room(H),{
  ownerUid:'host',name:'KING Party Game',isPrivate:false,hasPassword:false,
  entranceMode:'free',closed:false
 }));
 await assertSucceeds(setDoc(member(H,'host'),{uid:'host',name:'Host'}));
 await assertSucceeds(setDoc(member(A,'alice'),{uid:'alice',name:'Alice'}));
 // Non-members cannot create a Party Ludo match by guessing its Party ID.
 await assertFails(setDoc(match(E),fresh('eve')));
 // Ordinary room member may play, but only Host/Co-host creates the match.
 await assertFails(setDoc(match(A),fresh('alice')));
 await assertSucceeds(setDoc(match(H),fresh('host')));
 await assertFails(getDoc(match(E)));
 await assertSucceeds(getDoc(match(A)));
 await assertFails(updateDoc(match(E),join('eve')));
 await assertSucceeds(updateDoc(match(A),join('alice')));
 // Cannot move this Party game into another room by rewriting its room key.
 await assertFails(updateDoc(match(A),{partyRoomKey:'different-party',revision:2,actorUid:'alice',updatedAt:serverTimestamp()}));
 // After membership is removed, an already-joined player loses game access.
 await assertSucceeds(deleteDoc(member(H,'alice')));
 await assertFails(getDoc(match(A)));
 await assertFails(updateDoc(match(A),{actorUid:'alice',action:'ready',revision:2,updatedAt:serverTimestamp()}));
 await assertSucceeds(getDoc(match(H)));
 // Standalone Online Ludo still supports ordinary signed-in discovery.
 await env.withSecurityRulesDisabled(async context=>{
   await setDoc(doc(context.firestore(),'ludo_matches','FREE24'),{
     code:'FREE24',ownerUid:'host',players:['host','alice','',''],revision:1,
     phase:'lobby',actorUid:'host',capacity:2
   });
 });
 await assertSucceeds(getDoc(doc(E,'ludo_matches','FREE24')));
 console.log('PASS Ludo: host creates, Party member joins, stranger denied, kicked player blocked, standalone match unaffected');
} finally {await env.cleanup();}
