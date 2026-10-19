import fs from 'node:fs';
import assert from 'node:assert/strict';
import {initializeTestEnvironment, assertSucceeds, assertFails} from '@firebase/rules-unit-testing';
import {doc, setDoc, updateDoc, getDoc, serverTimestamp} from 'firebase/firestore';

const env=await initializeTestEnvironment({
 projectId:'demo-king-room-v9936',
 firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}
});
const h=env.authenticatedContext('host').firestore();
const b=env.authenticatedContext('bob').firestore();
const e=env.authenticatedContext('eve').firestore();
const room=db=>doc(db,'live_rooms','functional-9936');
const member=(db,uid)=>doc(db,'live_rooms','functional-9936','members',uid);
const grant=(db,uid)=>doc(db,'live_rooms','functional-9936','access',uid);
const passwordSecret=db=>doc(db,'live_rooms','functional-9936','secrets','password');
const enter=(db,uid)=>setDoc(member(db,uid),{uid,name:uid});
const change=(mode,epoch)=>updateDoc(room(h),{entranceMode:mode,
 isPrivate:mode!=='free',hasPassword:mode==='password',entryEpoch:epoch,
 updatedAt:serverTimestamp()});
const proof=(uid,hash,epoch)=>({uid,passwordHash:hash,entryEpoch:epoch,createdAt:serverTimestamp()});
try {
 await env.clearFirestore();
 await assertSucceeds(setDoc(room(h),{
  ownerUid:'host',name:'Real functional KING Party',isPrivate:false,
  hasPassword:false,entranceMode:'free',closed:false
 }));
 await assertSucceeds(enter(h,'host'));
 await assertSucceeds(enter(b,'bob'));
 await assertSucceeds(setDoc(grant(h,'bob'),{uid:'bob',grantedBy:'host',entryEpoch:'',createdAt:serverTimestamp()}));
 const hashA='correct-A-hash';
 const hashB='correct-B-hash';
 await assertSucceeds(setDoc(passwordSecret(h),{ownerUid:'host',passwordHash:hashA}));
 await assertSucceeds(change('password','EPOCH-1'));
 await assertFails(getDoc(member(b,'bob'))); // stale free/invite grants are no longer valid
 await assertFails(enter(e,'eve'));
 await assertFails(setDoc(grant(b,'bob'),proof('bob','wrong','EPOCH-1')));
 await assertFails(setDoc(grant(b,'bob'),proof('bob',hashA,'OLD-EPOCH')));
 await assertSucceeds(setDoc(grant(b,'bob'),proof('bob',hashA,'EPOCH-1'))); // update existing marker after proof
 await assertSucceeds(getDoc(member(b,'bob')));
 await assertSucceeds(setDoc(grant(e,'eve'),proof('eve',hashA,'EPOCH-1')));
 await assertSucceeds(enter(e,'eve'));
 await assertSucceeds(updateDoc(passwordSecret(h),{passwordHash:hashB}));
 await assertSucceeds(change('password','EPOCH-2'));
 await assertFails(getDoc(member(b,'bob')));
 await assertFails(getDoc(member(e,'eve')));
 await assertFails(setDoc(grant(b,'bob'),proof('bob',hashA,'EPOCH-2'))); // old password cannot extend
 await assertFails(setDoc(grant(b,'bob'),proof('bob',hashB,'EPOCH-1')));
 await assertSucceeds(setDoc(grant(b,'bob'),proof('bob',hashB,'EPOCH-2')));
 await assertSucceeds(getDoc(member(b,'bob')));
 await assertSucceeds(change('invite','EPOCH-3'));
 await assertFails(getDoc(member(b,'bob')));
 await assertFails(setDoc(grant(b,'bob'),proof('bob',hashB,'EPOCH-3'))); // password invalid after switching to invites
 await assertFails(setDoc(grant(e,'bob'),{uid:'bob',entryEpoch:'EPOCH-3',grantedBy:'eve'}));
 await assertSucceeds(setDoc(grant(h,'bob'),{uid:'bob',entryEpoch:'EPOCH-3',grantedBy:'host'}));
 await assertSucceeds(getDoc(member(b,'bob')));
 await assertSucceeds(change('follow','EPOCH-4'));
 await assertFails(getDoc(member(b,'bob')));
 await assertSucceeds(change('free','EPOCH-5'));
 await assertSucceeds(enter(e,'eve'));

 // Old v9.9.31 test rooms without entryEpoch remain backward compatible.
 await assertSucceeds(setDoc(room(h),{
  ownerUid:'host',name:'Prior KING Room',isPrivate:true,
  hasPassword:false,entranceMode:'invite',closed:false
 }));
 await assertSucceeds(getDoc(member(b,'bob'))); // the old access doc exists
 console.log('PASS room mode/password rotation, stale access denied, correct new password can reauthorize, host invite, backwards compatibility');
} finally {await env.cleanup();}
