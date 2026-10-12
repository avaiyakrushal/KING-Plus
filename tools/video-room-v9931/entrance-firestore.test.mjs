import fs from 'node:fs';
import assert from 'node:assert/strict';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,updateDoc,getDoc,deleteDoc,serverTimestamp} from 'firebase/firestore';

const env=await initializeTestEnvironment({projectId:'demo-king-entrance-9931',
 firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}});
const host=env.authenticatedContext('host').firestore();
const bob=env.authenticatedContext('bob').firestore();
const eve=env.authenticatedContext('eve').firestore();
const mallory=env.authenticatedContext('mallory').firestore();
const room=db=>doc(db,'live_rooms','new-entry-room');
const member=(db,uid)=>doc(db,'live_rooms','new-entry-room','members',uid);
const enter=(db,uid)=>setDoc(member(db,uid),{uid,name:uid});
const mode=(name,extra={})=>({entranceMode:name,isPrivate:name!=='free',hasPassword:name==='password',familyCode:'',updatedAt:serverTimestamp(),...extra});
try {
 await env.clearFirestore();
 await assertSucceeds(setDoc(room(host),{ownerUid:'host',name:'New Party Room',isPrivate:false,entranceMode:'free',closed:false,locked:false}));
 await assertSucceeds(enter(host,'host'));
 await assertSucceeds(enter(bob,'bob'));
 await assertSucceeds(updateDoc(room(host),mode('invite')));
 await assertFails(getDoc(member(bob,'bob')));
 await assertFails(enter(eve,'eve'));
 await assertSucceeds(setDoc(doc(host,'live_rooms','new-entry-room','access','eve'),{uid:'eve',grantedBy:'host'}));
 await assertSucceeds(enter(eve,'eve'));
 await assertSucceeds(updateDoc(room(host),mode('family',{familyCode:'FAM99'})));
 await assertSucceeds(setDoc(doc(host,'families','FAM99'),{ownerUid:'host',code:'FAM99',name:'Real family'}));
 await assertFails(enter(eve,'eve')); // previous invite cannot override family membership
 await assertFails(enter(bob,'bob'));
 await assertSucceeds(setDoc(doc(bob,'families','FAM99','members','bob'),{uid:'bob',role:'member'}));
 await assertSucceeds(enter(bob,'bob'));
 await assertSucceeds(deleteDoc(doc(bob,'families','FAM99','members','bob')));
 await assertFails(getDoc(member(bob,'bob'))); // dynamic family revocation
 await assertSucceeds(updateDoc(room(host),mode('follow')));
 await assertFails(enter(bob,'bob'));
 await assertFails(setDoc(doc(mallory,'follows','bob__host'),{followerUid:'mallory',targetUid:'host'}));
 await assertSucceeds(setDoc(doc(bob,'follows','bob__host'),{followerUid:'bob',targetUid:'host'}));
 await assertSucceeds(enter(bob,'bob'));
 await assertSucceeds(setDoc(doc(host,'follows','host__eve'),{followerUid:'host',targetUid:'eve'}));
 await assertSucceeds(enter(eve,'eve'));
 await assertFails(enter(mallory,'mallory'));
 await assertSucceeds(deleteDoc(doc(bob,'follows','bob__host')));
 await assertFails(getDoc(member(bob,'bob')));
 await assertSucceeds(updateDoc(room(host),mode('password')));
 await assertFails(enter(eve,'eve'));
 await assertSucceeds(setDoc(doc(host,'live_rooms','new-entry-room','access','bob'),{uid:'bob',grantedBy:'host'}));
 await assertSucceeds(enter(bob,'bob'));
 await assertSucceeds(updateDoc(room(host),mode('free')));
 await assertSucceeds(enter(mallory,'mallory'));
 await assertSucceeds(updateDoc(room(host),{entranceMode:'unexpected'}));
 await assertFails(getDoc(member(mallory,'mallory')));
 console.log('PASS free/invite/family/host-follow/password and unknown mode, revocation and spoofing denied');
} finally {await env.cleanup();}
