import fs from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,updateDoc,getDoc,runTransaction,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({
 projectId:'demo-king-draw-integrity-9967',
 firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}
});
const H=env.authenticatedContext('host').firestore();
const C=env.authenticatedContext('cohost').firestore();
const A=env.authenticatedContext('alice').firestore();
const base='live_rooms/draw-integrity-9967';
const state=db=>doc(db,base,'game_state','current');
const member=(db,uid)=>doc(db,base,'members',uid);
const starting=(round,type='draw',status='active',sketch='')=>({
 actorUid:'host',actorName:'Host',type,roundId:round,status,sketch,
 drawerUid:'host',drawerName:'Host',prompt:'Draw online',result:'',
 participantUids:['host','cohost','alice'],updatedAt:serverTimestamp()
});
try{
 await env.clearFirestore();
 await assertSucceeds(setDoc(doc(H,base),{ownerUid:'host',name:'KING live Party Draw',
     entranceMode:'free',isPrivate:false,closed:false,hasPassword:false}));
 for(const [db,id] of [[H,'host'],[C,'cohost'],[A,'alice']])
     await assertSucceeds(setDoc(member(db,id),{uid:id,name:id}));
 await assertSucceeds(setDoc(doc(H,base,'roles','cohost'),{uid:'cohost',role:'cohost'}));
 await assertSucceeds(setDoc(state(H),starting(301)));
 await assertSucceeds(updateDoc(state(H),{
     actorUid:'host',sketch:'|100,200;200,200;',updatedAt:serverTimestamp()}));
 const visual=await assertSucceeds(getDoc(state(A)));
 if(visual.data().sketch!=='|100,200;200,200;')
     throw new Error('Real Party participant did not receive drawing');
 await assertFails(updateDoc(state(C),{
   actorUid:'cohost',sketch:'|5,5;',updatedAt:serverTimestamp()}));
 await assertFails(updateDoc(state(C),{
   actorUid:'host',sketch:'|6,6;',updatedAt:serverTimestamp()}));
 await assertFails(updateDoc(state(A),{
   actorUid:'alice',sketch:'|7,7;',updatedAt:serverTimestamp()}));
 await assertFails(updateDoc(state(H),{
   actorUid:'host',sketch:'1'.repeat(12001),updatedAt:serverTimestamp()}));
 // Legitimate moderator can still finish an active round without touching sketch
 await assertSucceeds(updateDoc(state(C),{
   status:'closed',actorUid:'cohost',updatedAt:serverTimestamp()}));
 await assertFails(updateDoc(state(H),{
   actorUid:'host',sketch:'|8,8;',updatedAt:serverTimestamp()}));
 // A new actual round may reset the old sketch atomically via the Host.
 await assertSucceeds(setDoc(state(H),starting(302,'draw','active','')));
 let newer=await getDoc(state(A));
 if(newer.data().roundId!==302||newer.data().sketch!=='')
   throw new Error('New online Draw round reset failed');
 let rejected=false;
 try{
   await runTransaction(H,async tx=>{
     const live=await tx.get(state(H));
     const own=await tx.get(member(H,'host'));
     if(!live.exists()||live.data().roundId!==301||
       live.data().status!=='active'||live.data().type!=='draw'||
       live.data().drawerUid!=='host'||!own.exists()||
       own.data().uid!=='host')
        throw new Error('Party round/drawer changed');
     tx.update(state(H),{actorUid:'host',sketch:'|1,1;',
                         updatedAt:serverTimestamp()});
   });
 }catch(e){rejected=String(e).includes('Party round/drawer changed')}
 if(!rejected)throw new Error('Old round wrote on newer shared canvas');
 newer=await getDoc(state(A));
 if(newer.data().sketch!=='')
   throw new Error('Drawing from old Party round overwrote new live round');
 await assertSucceeds(updateDoc(state(H),{
    actorUid:'host',sketch:'|700,800;',updatedAt:serverTimestamp()}));
 console.log('PASS live Party Draw: host stroke to other account; moderator/outsider blocked; closed stroke refused; new round reset and stale transaction rejected');
}finally{await env.cleanup();}
