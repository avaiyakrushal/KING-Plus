import fs from 'node:fs';
import {initializeTestEnvironment,assertSucceeds} from '@firebase/rules-unit-testing';
import {doc,setDoc,deleteDoc,getDoc,getDocs,collection,query,where,runTransaction,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({
 projectId:'demo-king-ready-filter-9963',
 firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}
});
const H=env.authenticatedContext('host').firestore();
const A=env.authenticatedContext('alice').firestore();
const D=env.authenticatedContext('dora').firestore();
const E=env.authenticatedContext('eve').firestore();
const base='live_rooms/ttt-ready-filter-9963';
const ready=(db,id)=>doc(db,base,'game_ready',id);
const member=(db,id)=>doc(db,base,'members',id);
try{
 await env.clearFirestore();
 await assertSucceeds(setDoc(doc(H,base),{ownerUid:'host',name:'Ready filtered game',
  entranceMode:'free',closed:false,isPrivate:false,hasPassword:false}));
 for(const [db,id] of [[H,'host'],[A,'alice'],[D,'dora'],[E,'eve']]){
   await assertSucceeds(setDoc(member(db,id),{uid:id,name:id}));
   await assertSucceeds(setDoc(ready(db,id),{uid:id,name:id,ready:id!=='host'}));
 }
 await assertSucceeds(deleteDoc(member(E,'eve'))); // old Ready:true ghost
 await env.withSecurityRulesDisabled(async ctx=>{
   const admin=ctx.firestore();
   for(let i=0;i<54;i++){
     const id='unready_'+String(i).padStart(3,'0');
     await setDoc(ready(admin,id),{uid:id,name:id,ready:false});
   }
 });
 const countAll=(await getDocs(collection(H,base,'game_ready'))).size;
 if(countAll<=50)throw new Error('Test fixture must exceed the old 50-doc cap');
 const onlyOpted=await assertSucceeds(getDocs(query(collection(H,base,'game_ready'),where('ready','==',true))));
 const optedUids=onlyOpted.docs.map(x=>x.id).sort();
 if(JSON.stringify(optedUids)!==JSON.stringify(['alice','dora','eve']))
   throw new Error('Filtering must exclude 55 non-ready entries, found '+optedUids);
 const live=await assertSucceeds(runTransaction(H,async tx=>{
   const picked=[];
   for(const u of optedUids){
     const r=await tx.get(ready(H,u));
     const m=await tx.get(member(H,u));
     if(r.exists()&&r.data().ready===true&&r.data().uid===u
         &&m.exists()&&m.data().uid===u)picked.push(u);
   }
   if(picked.length<2)throw new Error('Need two real Party players');
   tx.set(doc(H,base,'game_state','current'),{
     actorUid:'host',actorName:'host',type:'ttt',status:'active',
     roundId:9963,prompt:'Alice versus Dora',result:'',board:'.........',
     turnUid:picked[0],xUid:picked[0],oUid:picked[1],
     xName:'alice',oName:'dora',participantUids:picked,updatedAt:serverTimestamp()
   });
   return picked;
 }));
 if(JSON.stringify(live)!==JSON.stringify(['alice','dora']))
   throw new Error('Ghost Eve admitted / valid Party players rejected: '+live);
 const started=await getDoc(doc(A,base,'game_state','current'));
 if(!started.exists()||JSON.stringify(started.data().participantUids)!==JSON.stringify(['alice','dora']))
   throw new Error('Actual synchronized online TTT start failed');
 console.log('PASS 58+ game_ready docs, 55 unready hidden, ghost Ready:true member removed, 2 live game players locked');
}finally{await env.cleanup();}
