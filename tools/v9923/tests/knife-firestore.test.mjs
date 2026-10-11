import fs from 'node:fs';
import assert from 'node:assert/strict';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,updateDoc,getDoc,deleteDoc,query,collection,orderBy,limit,getDocs,serverTimestamp} from 'firebase/firestore';

const env=await initializeTestEnvironment({
 projectId:'demo-kingplus-knife-9923',
 firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}
});
const alice=env.authenticatedContext('alice').firestore();
const bob=env.authenticatedContext('bob').firestore();
const eve=env.authenticatedContext('eve').firestore();
const room=db=>doc(db,'live_rooms','knife-live-test');
const member=(db,uid)=>doc(db,'live_rooms','knife-live-test','members',uid);
const score=(db,uid)=>doc(db,'live_rooms','knife-live-test','knife_duel_scores',uid);
const entry=(uid,name,best,last,plays)=>({uid,name,bestScore:best,lastScore:last,plays,updatedAt:serverTimestamp()});
try{
 await env.clearFirestore();
 await assertSucceeds(setDoc(room(alice),{ownerUid:'alice',name:'KING Knife Live',
   isPrivate:false,locked:false,closed:false}));
 await assertSucceeds(setDoc(member(alice,'alice'),{uid:'alice',name:'Alice'}));
 await assertSucceeds(setDoc(member(bob,'bob'),{uid:'bob',name:'Bob'}));
 await assertFails(setDoc(score(eve,'eve'),entry('eve','Eve',7,7,1)));
 await assertFails(setDoc(score(bob,'alice'),entry('alice','Bogus Alice',7,7,1)));
 await assertSucceeds(setDoc(score(alice,'alice'),entry('alice','Alice',3,3,1)));
 await assertSucceeds(setDoc(score(bob,'bob'),entry('bob','Bob',5,5,1)));
 const ranks=await assertSucceeds(getDocs(query(collection(alice,'live_rooms','knife-live-test','knife_duel_scores'),orderBy('bestScore','desc'),limit(12))));
 assert.deepEqual(ranks.docs.map(d=>d.id),['bob','alice']);
 await assertFails(getDoc(score(eve,'bob')));
 console.log('PASS leaderboard synchronized and restricted to actual signed-in Party members');

 await assertFails(updateDoc(score(bob,'alice'),{bestScore:7,lastScore:7,plays:2,updatedAt:serverTimestamp()}));
 await assertFails(updateDoc(score(alice,'alice'),{bestScore:9,lastScore:9,plays:2,updatedAt:serverTimestamp()}));
 await assertFails(updateDoc(score(alice,'alice'),{bestScore:4,lastScore:4,plays:22,updatedAt:serverTimestamp()}));
 await assertFails(updateDoc(score(alice,'alice'),{uid:'eve',plays:2,updatedAt:serverTimestamp()}));
 await assertSucceeds(updateDoc(score(alice,'alice'),{bestScore:4,lastScore:4,plays:2,updatedAt:serverTimestamp()}));
 await assertSucceeds(updateDoc(score(alice,'alice'),{bestScore:4,lastScore:2,plays:3,updatedAt:serverTimestamp()}));
 assert.equal((await assertSucceeds(getDoc(score(alice,'alice')))).data().bestScore,4);
 await assertFails(deleteDoc(score(alice,'alice')));
 console.log('PASS only own score, monotonic best, per-round play counter, no deletion');

 await assertSucceeds(updateDoc(score(bob,'bob'),{bestScore:7,lastScore:7,plays:2,updatedAt:serverTimestamp()}));
 assert.equal((await assertSucceeds(getDoc(score(alice,'bob')))).data().bestScore,7);
 console.log('PASS two real Firebase users can submit playable Knife results');
}finally{await env.cleanup();}
