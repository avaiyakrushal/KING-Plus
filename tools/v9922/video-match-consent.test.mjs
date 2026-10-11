import fs from 'node:fs';
import assert from 'node:assert/strict';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,updateDoc,getDoc,deleteDoc,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({
 projectId:'demo-kingplus-match-9922',
 firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}
});
const alice=env.authenticatedContext('alice').firestore();
const bob=env.authenticatedContext('bob').firestore();
const eve=env.authenticatedContext('eve').firestore();
const room=db=>doc(db,'live_rooms','match-test');
const mem=(db,uid)=>doc(db,'live_rooms','match-test','members',uid);
const seat=(db,id)=>doc(db,'live_rooms','match-test','video_seats',String(id));
const match=(db,uid)=>doc(db,'live_rooms','match-test','video_match_requests',uid);
try {
 await env.clearFirestore();
 await assertSucceeds(setDoc(room(alice),{ownerUid:'alice',name:'KING Video Party',isPrivate:false,locked:false,closed:false}));
 for(const [db,uid] of [[alice,'alice'],[bob,'bob'],[eve,'eve']])
   await assertSucceeds(setDoc(mem(db,uid),{uid,name:uid}));
 await assertSucceeds(setDoc(seat(alice,1),{uid:'alice',name:'Alice',micOn:false,cameraOn:false,updatedAt:serverTimestamp()}));
 await assertSucceeds(setDoc(seat(bob,2),{uid:'bob',name:'Bob',micOn:false,cameraOn:false,updatedAt:serverTimestamp()}));
 const proposal={fromUid:'alice',fromName:'Alice',recipientUid:'bob',recipientName:'Bob',
   fromSeat:1,toSeat:2,status:'pending',createdAt:serverTimestamp(),updatedAt:serverTimestamp()};
 await assertFails(setDoc(match(eve,'bob'),proposal));
 await assertSucceeds(setDoc(match(alice,'bob'),proposal));
 assert.equal((await assertSucceeds(getDoc(match(bob,'bob')))).data().status,'pending');
 await assertFails(getDoc(match(eve,'bob')));
 await assertFails(updateDoc(match(alice,'bob'),{status:'accepted',updatedAt:serverTimestamp()}));
 await assertSucceeds(updateDoc(match(bob,'bob'),{status:'accepted',updatedAt:serverTimestamp()}));
 assert.equal((await assertSucceeds(getDoc(match(alice,'bob')))).data().status,'accepted');
 await assertFails(updateDoc(match(bob,'bob'),{status:'declined',updatedAt:serverTimestamp()}));
 console.log('PASS only recipient can accept once; sender observes actual acceptance');
 await assertSucceeds(setDoc(match(alice,'bob'),proposal));
 await assertSucceeds(updateDoc(match(bob,'bob'),{status:'declined',updatedAt:serverTimestamp()}));
 assert.equal((await assertSucceeds(getDoc(match(alice,'bob')))).data().status,'declined');
 console.log('PASS declined invitation can be re-requested, no false match');
 await assertSucceeds(deleteDoc(seat(bob,2)));
 await assertFails(setDoc(match(alice,'bob'),proposal));
 console.log('PASS cannot invite another player who left their video seat');
} finally {await env.cleanup();}
