import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,getDoc,updateDoc,deleteDoc,serverTimestamp} from 'firebase/firestore';

const env=await initializeTestEnvironment({
 projectId:'demo-king-me-mutual-cp-9985',
 firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}
});
const A=env.authenticatedContext('alice123').firestore(),
 B=env.authenticatedContext('bob123').firestore(),
 C=env.authenticatedContext('charlie123').firestore();
const cp=db=>doc(db,'relationships','alice123__bob123');
const reversed=db=>doc(db,'relationships','bob123__alice123');
const family=db=>doc(db,'families','ABCDEF');
const pair=(init,st='pending')=>({
 uidA:'alice123',uidB:'bob123',nameA:'Alice',nameB:'Bob',
 initiatorUid:init,status:st,createdAt:serverTimestamp()
});
try{
 await env.clearFirestore();
 await assertFails(setDoc(cp(C),pair('charlie123')));
 await assertFails(setDoc(cp(A),pair('alice123','accepted')));
 await assertFails(setDoc(reversed(B),{
   uidA:'bob123',uidB:'alice123',nameA:'Bob',nameB:'Alice',
   initiatorUid:'bob123',status:'pending',createdAt:serverTimestamp()
 }));
 await assertSucceeds(setDoc(cp(A),pair('alice123')));
 const received=await assertSucceeds(getDoc(cp(B)));
 if(!received.exists()||received.data().status!=='pending')
   throw Error('CP incoming request must appear on recipient device');
 await assertFails(updateDoc(cp(A),{status:'accepted',updatedAt:serverTimestamp()}));
 await assertFails(updateDoc(cp(C),{status:'accepted',updatedAt:serverTimestamp()}));
 await assertFails(updateDoc(cp(B),{status:'magic_win',updatedAt:serverTimestamp()}));
 await assertFails(updateDoc(cp(B),{status:'accepted',uidA:'charlie123',updatedAt:serverTimestamp()}));
 await assertSucceeds(updateDoc(cp(B),{status:'accepted',updatedAt:serverTimestamp()}));
 const accepted=await assertSucceeds(getDoc(cp(A)));
 if(accepted.data().status!=='accepted')throw Error('accepted CP not visible across both phones');
 await assertFails(updateDoc(cp(A),{status:'declined',updatedAt:serverTimestamp()}));
 await assertFails(updateDoc(cp(B),{status:'declined',updatedAt:serverTimestamp()}));
 await assertSucceeds(deleteDoc(cp(A)));
 await assertSucceeds(setDoc(cp(B),pair('bob123')));
 await assertFails(updateDoc(cp(B),{status:'accepted',updatedAt:serverTimestamp()}));
 await assertSucceeds(updateDoc(cp(A),{status:'declined',updatedAt:serverTimestamp()}));
 await assertFails(updateDoc(cp(B),{status:'accepted',updatedAt:serverTimestamp()}));
 await assertSucceeds(deleteDoc(cp(B)));
 // Existing owner-only Family features remain; name edits permitted, but
 // no client can mint fake Family Treasury, Level or member count.
 await assertFails(setDoc(family(B),{
   ownerUid:'alice123',ownerName:'Alice',code:'ABCDEF',name:'Not my Family',
   level:1,treasury:0,memberCount:1,createdAt:serverTimestamp(),updatedAt:serverTimestamp()
 }));
 await assertSucceeds(setDoc(family(A),{
   ownerUid:'alice123',ownerName:'Alice',code:'ABCDEF',name:'KING Family',
   level:1,treasury:0,memberCount:1,createdAt:serverTimestamp(),updatedAt:serverTimestamp()
 }));
 await assertFails(updateDoc(family(A),{treasury:999999}));
 await assertFails(updateDoc(family(A),{level:50}));
 await assertFails(updateDoc(family(A),{memberCount:9000}));
 await assertFails(updateDoc(family(B),{name:'Takeover'}));
 await assertSucceeds(updateDoc(family(A),{name:'Real Family'}));
 console.log('PASS 2-phone Relationship: recipient-only consent; canonical pair; no self-accept or stranger; settled immutable; server Family treasury/level spoofing rejected');
}finally{await env.cleanup();}
