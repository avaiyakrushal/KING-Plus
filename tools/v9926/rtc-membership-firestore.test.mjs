import fs from 'node:fs';
import assert from 'node:assert/strict';
import {initializeTestEnvironment,assertSucceeds} from '@firebase/rules-unit-testing';
import {doc,setDoc,getDoc,updateDoc} from 'firebase/firestore';

const env=await initializeTestEnvironment({projectId:'demo-king-rtc-9926',
  firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}});
const owner=env.authenticatedContext('alice').firestore();
const guest=env.authenticatedContext('bob').firestore();
try{
  await env.clearFirestore();
  const party=db=>doc(db,'live_rooms','verified-room-123');
  const partyMember=(db,uid)=>doc(db,'live_rooms','verified-room-123','members',uid);
  await assertSucceeds(setDoc(party(owner),{ownerUid:'alice',name:'KING Voice',locked:false,closed:false}));
  await assertSucceeds(setDoc(partyMember(owner,'alice'),{uid:'alice',name:'Alice'}));
  assert.equal((await assertSucceeds(getDoc(partyMember(guest,'bob')))).exists(),false);
  await assertSucceeds(setDoc(partyMember(guest,'bob'),{uid:'bob',name:'Bob'}));
  assert.equal((await assertSucceeds(getDoc(partyMember(guest,'bob')))).data().uid,'bob');
  await assertSucceeds(updateDoc(party(owner),{closed:true}));
  assert.equal((await assertSucceeds(getDoc(party(guest)))).data().closed,true);
  console.log('PASS RTC Party membership: never infer from signed-in status or stale/closed room');

  const family=db=>doc(db,'families','family-123');
  const familyMember=(db,uid)=>doc(db,'families','family-123','members',uid);
  await assertSucceeds(setDoc(family(owner),{ownerUid:'alice',code:'family-123',name:'KING Family'}));
  await assertSucceeds(setDoc(familyMember(owner,'alice'),{uid:'alice',role:'owner',name:'Alice'}));
  await assertSucceeds(setDoc(familyMember(guest,'bob'),{uid:'bob',role:'member',name:'Bob'}));
  assert.equal((await assertSucceeds(getDoc(familyMember(guest,'bob')))).data().uid,'bob');
  assert.equal((await assertSucceeds(getDoc(familyMember(owner,'alice')))).data().role,'owner');
  console.log('PASS RTC Family membership: authenticated server-visible member documents');
} finally { await env.cleanup(); }