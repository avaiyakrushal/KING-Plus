import fs from 'node:fs';
import assert from 'node:assert/strict';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,getDoc,setDoc,updateDoc,deleteDoc,runTransaction,serverTimestamp} from 'firebase/firestore';

// Two independent authenticated Firebase clients; this test does not pretend
// to replace two physical Android devices or prove live RTC connectivity.
const env=await initializeTestEnvironment({
  projectId:'demo-kingplus-party-parity',
  firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}
});
const alice=env.authenticatedContext('alice').firestore();
const bob=env.authenticatedContext('bob').firestore();
const eve=env.authenticatedContext('eve').firestore();
const dave=env.authenticatedContext('dave').firestore();
const room=(db)=>doc(db,'live_rooms','PARITY-ROOM1');
const member=(db,uid)=>doc(db,'live_rooms','PARITY-ROOM1','members',uid);
const seat=(db,n)=>doc(db,'live_rooms','PARITY-ROOM1','seats',String(n));
const role=(db,uid)=>doc(db,'live_rooms','PARITY-ROOM1','roles',uid);
const request=(db,uid)=>doc(db,'live_rooms','PARITY-ROOM1','seat_requests',uid);
const invite=(db,uid)=>doc(db,'live_rooms','PARITY-ROOM1','seat_invites',uid);
const banned=(db,uid)=>doc(db,'live_rooms','PARITY-ROOM1','room_bans',uid);
const check=label=>console.log('PASS',label);

try {
  await env.clearFirestore();
  await assertSucceeds(setDoc(room(alice),{
    ownerUid:'alice',ownerName:'Alice',name:'KING Party',
    isPrivate:false,locked:false,closed:false,createdAt:serverTimestamp()
  }));
  for (const [db,uid] of [[alice,'alice'],[bob,'bob'],[eve,'eve'],[dave,'dave']]){
    await assertSucceeds(setDoc(member(db,uid),{uid,name:uid}));
  }
  await assertFails(setDoc(member(eve,'bob'),{uid:'bob',name:'Fake Bob'}));
  await assertFails(updateDoc(room(eve),{ownerUid:'eve'}));
  check('login identity and owner ownership rules');

  await assertSucceeds(setDoc(seat(bob,1),{uid:'bob',name:'Bob',micOn:false}));
  assert.equal((await assertSucceeds(getDoc(seat(alice,1)))).data().uid,'bob');
  await assertFails(setDoc(seat(eve,1),{uid:'eve',name:'Eve',micOn:true}));
  await assertFails(setDoc(seat(eve,2),{uid:'bob',name:'Impersonated',micOn:false}));
  await assertFails(updateDoc(seat(eve,1),{micOn:true}));
  await assertSucceeds(updateDoc(seat(bob,1),{micOn:true}));
  assert.equal((await getDoc(seat(alice,1))).data().micOn,true);
  await assertSucceeds(updateDoc(seat(alice,1),{micOn:false}));
  check('two-account seat read, microphone toggle, no seat theft or spoof');

  await assertFails(setDoc(role(eve,'eve'),{uid:'eve',role:'cohost'}));
  await assertSucceeds(setDoc(role(alice,'dave'),{uid:'dave',role:'cohost'}));
  await assertSucceeds(updateDoc(seat(dave,1),{micOn:true}));
  await assertFails(updateDoc(seat(eve,1),{uid:'eve'}));
  check('co-host moderation; ordinary member cannot self-promote');

  await assertSucceeds(setDoc(request(bob,'bob'),{uid:'bob',name:'Bob',seatNo:2,createdAt:serverTimestamp()}));
  await assertFails(setDoc(request(eve,'bob'),{uid:'bob',name:'Fake',seatNo:2}));
  await assertSucceeds(deleteDoc(request(alice,'bob')));
  await assertFails(setDoc(invite(eve,'eve'),{targetUid:'eve',invitedByUid:'eve',seatId:'3'}));
  await assertSucceeds(setDoc(invite(alice,'eve'),{targetUid:'eve',invitedByUid:'alice',seatId:'3',createdAt:serverTimestamp()}));
  await assertSucceeds(getDoc(invite(eve,'eve')));
  await assertSucceeds(deleteDoc(invite(eve,'eve')));
  check('real seat request and invite ownership');

  // Competing Android clients run Firestore transactions to claim the same
  // seat: exactly one successful claim, without rewriting another UID.
  const claim=async(db,uid)=>runTransaction(db,async tx=>{
    const s=seat(db,4),snapshot=await tx.get(s);
    if(snapshot.exists())throw Error('Already occupied');
    tx.set(s,{uid,name:uid,micOn:false});
  });
  const outcomes=await Promise.allSettled([claim(eve,'eve'),claim(dave,'dave')]);
  assert.equal(outcomes.filter(r=>r.status==='fulfilled').length,1);
  const winner=(await getDoc(seat(alice,4))).data().uid;
  assert.ok(['eve','dave'].includes(winner));
  check('two-account concurrent Firestore seat transaction');

  await assertSucceeds(setDoc(banned(alice,'bob'),{uid:'bob',active:true}));
  await assertFails(getDoc(seat(bob,1)));
  await assertFails(setDoc(seat(bob,6),{uid:'bob',name:'Bob',micOn:false}));
  await assertSucceeds(getDoc(seat(eve,1)));
  await assertFails(setDoc(banned(eve,'alice'),{uid:'alice',active:true}));
  await assertFails(deleteDoc(banned(eve,'bob')));
  check('blocked room user loses live room access; moderator rights protected');
  console.log('ALL KING PARTY TWO-ACCOUNT FIRESTORE CHECKS PASSED');
} finally {
  await env.cleanup();
}
