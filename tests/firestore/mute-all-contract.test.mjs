import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import fs from 'node:fs';
import assert from 'node:assert/strict';
import {doc,setDoc,updateDoc,getDoc} from 'firebase/firestore';
const env=await initializeTestEnvironment({projectId:'demo-king-mute-all-v10020',
 firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}});
const H=env.authenticatedContext('host').firestore();
const A=env.authenticatedContext('alice').firestore();
const B=env.authenticatedContext('bob').firestore();
const root='live_rooms/king-mute-all-10020';
const room=db=>doc(db,root);
const member=(db,uid)=>doc(db,root,'members',uid);
const seat=(db,num)=>doc(db,root,'seats',String(num));
try {
 await env.clearFirestore();
 await assertSucceeds(setDoc(room(H),{ownerUid:'host',name:'KING Audio',isPrivate:false,entranceMode:'free',
   closed:false,locked:false,muteAll:false,maxSeats:8}));
 for(const [db,uid] of [[H,'host'],[A,'alice'],[B,'bob']])
   await assertSucceeds(setDoc(member(db,uid),{uid,name:uid}));
 await assertSucceeds(setDoc(seat(A,1),{uid:'alice',name:'Alice',micOn:false}));
 await assertSucceeds(setDoc(seat(B,2),{uid:'bob',name:'Bob',micOn:false}));
 await assertSucceeds(updateDoc(seat(A,1),{micOn:true}));
 await assertSucceeds(updateDoc(room(H),{muteAll:true}));
 await assertSucceeds(updateDoc(seat(H,1),{micOn:false}));
 await assertFails(updateDoc(seat(A,1),{micOn:true}));
 await assertFails(updateDoc(seat(B,2),{micOn:true}));
 await assertFails(updateDoc(seat(H,1),{micOn:true}));
 assert.equal((await assertSucceeds(getDoc(seat(A,1)))).data().micOn,false);
 await assertSucceeds(updateDoc(room(H),{muteAll:false}));
 await assertSucceeds(updateDoc(seat(A,1),{micOn:true}));
 await assertFails(updateDoc(seat(B,1),{micOn:false}));
 console.log('PASS muteAll: member microphones blocked until host unmutes; remote impersonation denied');
} finally {await env.cleanup();}
