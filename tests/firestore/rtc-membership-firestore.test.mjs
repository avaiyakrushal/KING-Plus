import fs from 'node:fs';
import assert from 'node:assert/strict';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,getDoc,updateDoc,serverTimestamp} from 'firebase/firestore';

// Current Rules require immutable family level/treasury/memberCount and true
// server-timestamp creation. Do not weaken production rules for a legacy test.
const env=await initializeTestEnvironment({projectId:'demo-king-rtc-family-v10019',
  firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}});
const owner=env.authenticatedContext('alice').firestore();
const guest=env.authenticatedContext('bob').firestore();
const outsider=env.authenticatedContext('eve').firestore();
try {
  await env.clearFirestore();
  const party=db=>doc(db,'live_rooms','verified-room-123');
  const member=(db,uid)=>doc(db,'live_rooms','verified-room-123','members',uid);
  await assertSucceeds(setDoc(party(owner),{ownerUid:'alice',name:'KING Voice',isPrivate:false,locked:false,closed:false}));
  await assertSucceeds(setDoc(member(owner,'alice'),{uid:'alice',name:'Alice'}));
  assert.equal((await assertSucceeds(getDoc(member(guest,'bob')))).exists(),false);
  await assertSucceeds(setDoc(member(guest,'bob'),{uid:'bob',name:'Bob'}));
  assert.equal((await assertSucceeds(getDoc(member(guest,'bob')))).data().uid,'bob');
  await assertSucceeds(updateDoc(party(owner),{closed:true}));
  assert.equal((await assertSucceeds(getDoc(party(guest)))).data().closed,true);
  const family=db=>doc(db,'families','family-123');
  const familyMember=(db,uid)=>doc(db,'families','family-123','members',uid);
  await assertSucceeds(setDoc(family(owner),{
     ownerUid:'alice',code:'family-123',name:'KING Family',
     level:1,treasury:0,memberCount:1,
     createdAt:serverTimestamp(),updatedAt:serverTimestamp()
  }));
  await assertSucceeds(setDoc(familyMember(owner,'alice'),{
     uid:'alice',role:'owner',name:'Alice',joinedAt:serverTimestamp()
  }));
  await assertSucceeds(setDoc(familyMember(guest,'bob'),{
     uid:'bob',role:'member',name:'Bob',joinedAt:serverTimestamp()
  }));
  assert.equal((await assertSucceeds(getDoc(familyMember(guest,'bob')))).data().uid,'bob');
  assert.equal((await assertSucceeds(getDoc(familyMember(owner,'alice')))).data().role,'owner');
  await assertFails(setDoc(familyMember(outsider,'eve'),{
     uid:'eve',role:'owner',name:'Eve',joinedAt:serverTimestamp()
  }));
  console.log('PASS room RTC membership + current family security: real member, no forged owner');
} finally { await env.cleanup(); }
