import fs from 'node:fs';
import assert from 'node:assert/strict';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,updateDoc,getDoc,serverTimestamp} from 'firebase/firestore';

const env=await initializeTestEnvironment({projectId:'demo-king-avatar-motion-9929',
 firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}});
const alice=env.authenticatedContext('alice').firestore();
const bob=env.authenticatedContext('bob').firestore();
const stranger=env.authenticatedContext('eve').firestore();
const room=db=>doc(db,'live_rooms','real-avatar-motion');
const member=(db,uid)=>doc(db,'live_rooms','real-avatar-motion','members',uid);
const pose=(db,uid)=>doc(db,'live_rooms','real-avatar-motion','avatar_motion',uid);
const state=(uid,x,z,action)=>({uid,x,z,action,updatedAt:serverTimestamp()});
try{
  await env.clearFirestore();
  await assertSucceeds(setDoc(room(alice),{ownerUid:'alice',name:'KING 3D Party',isPrivate:false,closed:false,locked:false}));
  await assertSucceeds(setDoc(member(alice,'alice'),{uid:'alice',name:'Alice'}));
  await assertSucceeds(setDoc(member(bob,'bob'),{uid:'bob',name:'Bob'}));
  await assertFails(setDoc(pose(stranger,'eve'),state('eve',1,0,'dance')));
  await assertFails(setDoc(pose(bob,'alice'),state('alice',1,0,'dance')));
  await assertSucceeds(setDoc(pose(alice,'alice'),state('alice',-1.5,1.2,'walk')));
  await assertSucceeds(setDoc(pose(bob,'bob'),state('bob',1.5,-1.2,'idle')));
  assert.equal((await assertSucceeds(getDoc(pose(bob,'alice')))).data().x,-1.5);
  await assertFails(updateDoc(pose(bob,'alice'),{x:2.0,updatedAt:serverTimestamp()}));
  await assertFails(updateDoc(pose(alice,'alice'),{x:5.0,updatedAt:serverTimestamp()}));
  await assertFails(updateDoc(pose(alice,'alice'),{z:4.0,updatedAt:serverTimestamp()}));
  await assertFails(updateDoc(pose(alice,'alice'),{action:'admin',updatedAt:serverTimestamp()}));
  await assertFails(updateDoc(pose(alice,'alice'),{uid:'eve',updatedAt:serverTimestamp()}));
  await assertSucceeds(updateDoc(pose(alice,'alice'),{x:-.95,action:'dance',updatedAt:serverTimestamp()}));
  assert.equal((await assertSucceeds(getDoc(pose(bob,'alice')))).data().action,'dance');
  assert.equal((await assertSucceeds(getDoc(pose(alice,'bob')))).data().action,'idle');
  await assertFails(getDoc(pose(stranger,'alice')));
  console.log('PASS two real Firebase emulator accounts can see live 3D walk/dance; no cross-account edits or out-of-world motion');
}finally{await env.cleanup();}
