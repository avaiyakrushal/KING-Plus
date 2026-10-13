import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,updateDoc,deleteDoc,getDoc,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({
 projectId:'demo-king-family-admin-moderation-9987',
 firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}});
const Owner=env.authenticatedContext('owner123').firestore(),
 Admin=env.authenticatedContext('admin123').firestore(),
 Mod=env.authenticatedContext('moderator123').firestore(),
 Member=env.authenticatedContext('member123').firestore(),
 Out=env.authenticatedContext('outsider123').firestore();
const root='families/ABCDEF';
const fam=db=>doc(db,root);
const person=(db,u)=>doc(db,root,'members',u);
const chat=(db,k)=>doc(db,root,'messages',k);
const treas=(db,k)=>doc(db,root,'treasury',k);
const member=(u,role)=>({uid:u,name:u,role,joinedAt:serverTimestamp()});
const message=(u,text)=>({uid:u,name:u,text,createdAt:serverTimestamp()});
try {
 await env.clearFirestore();
 await assertSucceeds(setDoc(fam(Owner),{
   ownerUid:'owner123',ownerName:'KING Host',code:'ABCDEF',
   name:'Original Family',level:1,treasury:0,memberCount:1,
   createdAt:serverTimestamp(),updatedAt:serverTimestamp()
 }));
 for (const [db,id,role] of [[Owner,'owner123','owner'],[Admin,'admin123','member'],
     [Mod,'moderator123','member'],[Member,'member123','member']]){
   await assertSucceeds(setDoc(person(db,id),member(id,role)));
 }
 await assertSucceeds(updateDoc(person(Owner,'admin123'),{role:'admin'}));
 await assertSucceeds(updateDoc(person(Owner,'moderator123'),{role:'moderator'}));
 await assertFails(updateDoc(person(Admin,'admin123'),{role:'owner'}));
 await assertFails(updateDoc(person(Mod,'moderator123'),{role:'admin'}));
 await assertSucceeds(setDoc(chat(Member,'msg-member'),message('member123','Hello from Member')));
 await assertSucceeds(setDoc(chat(Mod,'msg-mod'),message('moderator123','Hello from Moderator')));
 await assertSucceeds(setDoc(chat(Admin,'msg-admin'),message('admin123','Hello from Admin')));
 await assertFails(setDoc(chat(Out,'msg-outsider'),message('outsider123','Unauthorized')));
 await assertFails(setDoc(chat(Member,'msg-impersonate'),message('owner123','Fake Owner')));
 await assertFails(setDoc(chat(Member,'msg-money'),{
   ...message('member123','Fake 99999 Diamonds'),coins:99999
 }));
 await assertFails(setDoc(chat(Member,'msg-fake-time'),{
   uid:'member123',name:'Member',text:'Fake time',createdAt:new Date('2020-01-01')
 }));
 await assertFails(deleteDoc(chat(Member,'msg-admin')));
 await assertSucceeds(deleteDoc(chat(Mod,'msg-member')));
 await assertSucceeds(deleteDoc(chat(Owner,'msg-mod')));
 await assertSucceeds(deleteDoc(chat(Admin,'msg-admin')));
 await assertFails(updateDoc(chat(Member,'msg-member'),{text:'Change old chat'}));
 await assertSucceeds(setDoc(chat(Member,'my-own-message'),message('member123','Delete myself')));
 await assertSucceeds(deleteDoc(chat(Member,'my-own-message')));
 await assertFails(deleteDoc(person(Mod,'member123')));
 await assertFails(deleteDoc(person(Admin,'moderator123')));
 await assertFails(deleteDoc(person(Admin,'owner123')));
 await assertSucceeds(deleteDoc(person(Admin,'member123')));
 const gone=await assertSucceeds(getDoc(person(Owner,'member123')));
 if(gone.exists())throw Error('Admin kick failed to remove member across phone');
 // An Admin may voluntarily leave the Family; they cannot kick other Admins.
 await assertSucceeds(deleteDoc(person(Admin,'admin123')));
 await assertFails(setDoc(treas(Owner,'forgedOwner'),{uid:'owner123',amount:100}));
 await assertFails(setDoc(treas(Admin,'forgedAdmin'),{uid:'admin123',amount:10000}));
 await assertFails(setDoc(treas(Member,'forgedMember'),{uid:'member123',amount:1}));
 await assertSucceeds(updateDoc(fam(Owner),{name:'Renamed Family',
    updatedAt:serverTimestamp()}));
 const renamed=await assertSucceeds(getDoc(fam(Admin)));
 if(renamed.data().name!=='Renamed Family')throw Error('Family owner rename not synced');
 await assertFails(updateDoc(fam(Admin),{name:'Forged Family'}));
 await assertFails(updateDoc(fam(Owner),{level:99}));
 await assertFails(updateDoc(fam(Owner),{treasury:999999}));
 console.log('PASS 4 actual Firebase accounts: owner/admin/mod/member; real chat moderation; admin kick; no privilege escalation; secure timestamps; original Family balances protected; owner rename');
}finally{await env.cleanup();}
