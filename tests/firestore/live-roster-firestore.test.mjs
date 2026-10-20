import fs from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,collection,getDocs,setDoc,deleteDoc,getDoc,runTransaction,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({
 projectId:'demo-king-live-roster-9952',
 firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}
});
const H=env.authenticatedContext('host').firestore();
const A=env.authenticatedContext('alice').firestore();
const B=env.authenticatedContext('bob').firestore();
const C=env.authenticatedContext('charlie').firestore();
const path='live_rooms/real-roster-9952';
const room=d=>doc(d,path);
const member=(d,u)=>doc(d,path,'members',u);
const ready=(d,u)=>doc(d,path,'game_ready',u);
const state=d=>doc(d,path,'game_state','current');
const move=(d,u)=>doc(d,path,'game_moves',u);
try{
 await env.clearFirestore();
 await assertSucceeds(setDoc(room(H),{ownerUid:'host',name:'KING Live Room',
    entranceMode:'free',closed:false,hasPassword:false,isPrivate:false}));
 for(const [d,u] of [[H,'host'],[A,'alice'],[B,'bob'],[C,'charlie']]){
   await assertSucceeds(setDoc(member(d,u),{uid:u,name:u}));
   await assertSucceeds(setDoc(ready(d,u),{uid:u,name:u,ready:true,updatedAt:serverTimestamp()}));
 }
 // Reproduce: stale Ready row remains after player exits the live Party.
 await assertSucceeds(deleteDoc(member(C,'charlie')));
 const snap=await getDocs(collection(H,path,'game_ready'));
 const candidates=snap.docs.map(d=>d.id).filter(u=>u!=='host');
 if(!candidates.includes('charlie'))throw Error('Ghost Ready setup missing');
 await assertSucceeds(runTransaction(H,async tx=>{
   const current=await tx.get(state(H));
   if(current.exists())throw Error('Round unexpectedly started');
   const real=[];
   for(const uid of candidates){
     const rd=await tx.get(ready(H,uid));
     const md=await tx.get(member(H,uid));
     if(rd.exists()&&md.exists()&&rd.data().uid===uid&&md.data().uid===uid)
       real.push(uid);
   }
   if(real.length<2)throw Error('Not enough live players');
   tx.set(state(H),{actorUid:'host',actorName:'Host',type:'rps',status:'active',
     roundId:9952,prompt:'Rock Paper Scissors',result:'',
     participantUids:real,updatedAt:serverTimestamp()});
 }));
 const round=await getDoc(state(H));
 if(JSON.stringify(round.data().participantUids)!==JSON.stringify(['alice','bob']))
   throw Error('Ghost player included: '+JSON.stringify(round.data().participantUids));
 const play=(uid,label)=>({uid,name:uid,roundId:9952,type:'rps',move:label,
   score:0,updatedAt:serverTimestamp()});
 await assertFails(setDoc(move(C,'charlie'),play('charlie','Rock')));
 await assertSucceeds(setDoc(move(A,'alice'),play('alice','Rock')));
 await assertSucceeds(setDoc(move(B,'bob'),play('bob','Paper')));
 console.log('PASS live RPS: ghost Ready excluded, two real players allowed, removed user denied');
}finally{await env.cleanup();}
