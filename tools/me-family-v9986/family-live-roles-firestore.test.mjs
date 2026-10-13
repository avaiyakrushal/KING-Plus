import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,collection,setDoc,updateDoc,deleteDoc,getDoc,getDocsFromServer,query,
  serverTimestamp} from 'firebase/firestore';

const env=await initializeTestEnvironment({
 projectId:'demo-king-family-real-9986',
 firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}
});
const Owner=env.authenticatedContext('owner123').firestore(),
 Alice=env.authenticatedContext('alice123').firestore(),
 Bob=env.authenticatedContext('bob123').firestore(),
 Eve=env.authenticatedContext('eve123').firestore();
const family=db=>doc(db,'families','ABCDEF');
const member=(db,uid)=>doc(db,'families','ABCDEF','members',uid);
const members=db=>collection(db,'families','ABCDEF','members');
const memberData=(uid,role)=>({uid,role,name:uid,joinedAt:serverTimestamp()});
try{
 await env.clearFirestore();
 await assertSucceeds(setDoc(family(Owner),{
  ownerUid:'owner123',ownerName:'KING Owner',code:'ABCDEF',
  name:'Real KING Family',level:1,treasury:0,memberCount:1,
  createdAt:serverTimestamp(),updatedAt:serverTimestamp()
 }));
 await assertFails(setDoc(member(Alice,'owner123'),memberData('owner123','owner')));
 await assertFails(setDoc(member(Alice,'alice123'),memberData('alice123','owner')));
 await assertFails(setDoc(member(Eve,'eve123'),{...memberData('eve123','member'),treasury:999999}));
 await assertSucceeds(setDoc(member(Owner,'owner123'),memberData('owner123','owner')));
 await assertSucceeds(setDoc(member(Alice,'alice123'),memberData('alice123','member')));
 await assertSucceeds(setDoc(member(Bob,'bob123'),memberData('bob123','member')));
 const initial=await assertSucceeds(getDocsFromServer(query(members(Alice))));
 if(initial.size!==3)throw Error('Alice did not get live Family membership list');
 await assertFails(updateDoc(member(Alice,'alice123'),{role:'admin'}));
 await assertFails(updateDoc(member(Alice,'bob123'),{role:'admin'}));
 await assertFails(updateDoc(member(Bob,'bob123'),{joinedAt:serverTimestamp()}));
 await assertFails(updateDoc(member(Owner,'owner123'),{role:'member'}));
 await assertSucceeds(updateDoc(member(Owner,'alice123'),{role:'admin'}));
 const admin=await assertSucceeds(getDoc(member(Alice,'alice123')));
 if(admin.data().role!=='admin')throw Error('Granted Admin role not synced to Alice phone');
 await assertSucceeds(updateDoc(member(Owner,'bob123'),{role:'moderator'}));
 const moderator=await assertSucceeds(getDoc(member(Bob,'bob123')));
 if(moderator.data().role!=='moderator')throw Error('Granted Moderator role not synced to Bob phone');
 await assertFails(updateDoc(member(Alice,'alice123'),{role:'owner'}));
 await assertFails(updateDoc(member(Owner,'alice123'),{role:'owner'}));
 await assertFails(updateDoc(member(Owner,'alice123'),{name:'Forged display name'}));
 await assertSucceeds(updateDoc(member(Owner,'alice123'),{role:'member'}));
 await assertFails(deleteDoc(member(Bob,'alice123')));
 await assertFails(deleteDoc(member(Owner,'owner123')));
 await assertSucceeds(deleteDoc(member(Bob,'bob123')));
 await assertSucceeds(deleteDoc(member(Owner,'alice123')));
 const remaining=await assertSucceeds(getDocsFromServer(query(members(Owner))));
 if(remaining.size!==1||remaining.docs[0].id!=='owner123')
  throw Error('Real Family remaining owner-member sync did not match');
 await assertFails(setDoc(doc(Alice,'wallets','alice123'),{diamonds:990000}));
 console.log('PASS real Family 2-phone member joins, owner-only admin/moderator changes, demotion, kick/leave and owner role lock, Wallet unchanged');
}finally{await env.cleanup();}
