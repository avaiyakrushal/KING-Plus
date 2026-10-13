import fs from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,deleteDoc,getDoc,getDocs,collection,query,where,runTransaction,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({
  projectId:'demo-king-live-ready-9971',
  firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}
});
const H=env.authenticatedContext('host').firestore();
const A=env.authenticatedContext('alice').firestore();
const B=env.authenticatedContext('bob').firestore();
const E=env.authenticatedContext('eve').firestore();
const path='live_rooms/actual-party-ready-9971';
const member=(d,u)=>doc(d,path,'members',u);
const ready=(d,u)=>doc(d,path,'game_ready',u);
async function toggle(db,uid){
 return runTransaction(db,async tx=>{
   const m=await tx.get(member(db,uid));
   const r=await tx.get(ready(db,uid));
   if(!m.exists()||m.data().uid!==uid)throw new Error('no live Party member');
   const on=r.exists()&&r.data().uid===uid&&r.data().ready===true;
   if(on)tx.delete(ready(db,uid));
   else tx.set(ready(db,uid),{uid,name:m.data().name,ready:true,
                              updatedAt:serverTimestamp()});
   return !on;
 });
}
async function visible(db){
 const r=await getDocs(query(collection(db,path,'game_ready'),where('ready','==',true)));
 const m=await getDocs(collection(db,path,'members'));
 const members=new Set(m.docs.filter(x=>x.id===x.data().uid).map(x=>x.id));
 return r.docs.filter(x=>x.id===x.data().uid&&x.data().ready===true&&members.has(x.id)).map(x=>x.id).sort();
}
try{
 await env.clearFirestore();
 await assertSucceeds(setDoc(doc(H,path),{
   ownerUid:'host',name:'Party verified Ready',entranceMode:'free',
   isPrivate:false,closed:false,hasPassword:false,locked:false
 }));
 for(const [db,uid] of [[H,'host'],[A,'alice'],[B,'bob'],[E,'eve']])
   await assertSucceeds(setDoc(member(db,uid),{uid,name:uid}));
 await assertSucceeds(setDoc(ready(H,'host'),{
   uid:'host',name:'host',ready:false,updatedAt:serverTimestamp()
 }));
 await assertSucceeds(toggle(A,'alice'));
 await assertSucceeds(toggle(B,'bob'));
 await assertSucceeds(toggle(E,'eve'));
 await assertSucceeds(deleteDoc(member(E,'eve'))); // stale ghost Ready document
 if(JSON.stringify(await visible(H))!==JSON.stringify(['alice','bob']))
   throw new Error('departed/false Ready displayed as live');
 // Same account's two sessions both run the SAME transaction: two rapid
 // clicks may flip the shared value, but cannot write stale unrelated docs.
 await assertSucceeds(toggle(A,'alice'));
 if(JSON.stringify(await visible(B))!==JSON.stringify(['bob']))
   throw new Error('explicit Not Ready not reflected across phones');
 await assertSucceeds(toggle(A,'alice'));
 if(JSON.stringify(await visible(H))!==JSON.stringify(['alice','bob']))
   throw new Error('Ready failed to resume after screen reconnect');
 await assertFails(toggle(E,'eve'));
 await assertFails(setDoc(ready(B,'alice'),{
   uid:'alice',name:'fake',ready:true,updatedAt:serverTimestamp()
 }));
 await env.withSecurityRulesDisabled(async ctx=>{
   const admin=ctx.firestore();
   for(let i=0;i<55;i++){
     const id='not_ready_'+String(i).padStart(3,'0');
     await setDoc(ready(admin,id),{uid:id,name:id,ready:false});
   }
 });
 if(JSON.stringify(await visible(B))!==JSON.stringify(['alice','bob']))
   throw new Error('legacy unready rows blocked actual live player list');
 const me=await assertSucceeds(getDoc(ready(B,'bob')));
 if(!me.exists()||!me.data().ready)throw new Error('Phone B Ready was lost');
 console.log('PASS two real Party accounts: opt-in toggles, live member roster, false/ghosts hidden, 55+ unready rows ignored');
}finally{await env.cleanup();}
