import fs from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,deleteDoc,getDoc,runTransaction,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({projectId:'demo-king-social-party-9958',
 firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}});
const H=env.authenticatedContext('host').firestore();
const C=env.authenticatedContext('cohost').firestore();
const A=env.authenticatedContext('alice').firestore();
const B=env.authenticatedContext('bob').firestore();
const D=env.authenticatedContext('dora').firestore();
const E=env.authenticatedContext('eve').firestore();
const base='live_rooms/social-9958',room=db=>doc(db,base);
const ready=(db,uid)=>doc(db,base,'game_ready',uid);
const member=(db,uid)=>doc(db,base,'members',uid);
const state=db=>doc(db,base,'game_state','current');
const role=(db,round,uid)=>doc(db,base,'room_invites','game_'+round+'_'+uid);
try {
 await env.clearFirestore();
 await assertSucceeds(setDoc(room(H),{ownerUid:'host',name:'KING Social',entranceMode:'free',
   closed:false,isPrivate:false,hasPassword:false}));
 for(const [db,id] of [[H,'host'],[C,'cohost'],[A,'alice'],[B,'bob'],[D,'dora'],[E,'eve']]){
  await assertSucceeds(setDoc(member(db,id),{uid:id,name:id}));
  if(id!=='cohost')await assertSucceeds(setDoc(ready(db,id),{uid:id,name:id,ready:true}));
 }
 await assertSucceeds(setDoc(doc(H,base,'roles','cohost'),{uid:'cohost',role:'cohost'}));
 await assertSucceeds(deleteDoc(member(E,'eve'))); // ghost READY remains
 await assertSucceeds(setDoc(state(H),{type:'coin',roundId:10,status:'closed',
   result:'done',participantUids:['alice','bob'],actorUid:'host',updatedAt:serverTimestamp()}));
 const all=['host','alice','bob','dora','eve'];
 const publish=(db,expected,next,game)=>runTransaction(db,async tx=>{
   const prior=await tx.get(state(db));
   if(prior.data().status!=='closed'||prior.data().roundId!==expected)
     throw new Error('Round changed');
   const live=[];
   for(const uid of all){
     const rd=await tx.get(ready(db,uid)),mb=await tx.get(member(db,uid));
     if(rd.exists()&&mb.exists()&&rd.data().uid===uid&&mb.data().uid===uid)live.push(uid);
   }
   if(live.length<4)throw new Error('Need four live Party members');
   for(const uid of live){
     tx.set(role(db,next,uid),{senderUid:'host',senderName:'Host',recipientUid:uid,
      recipientName:uid,kind:'game_role',gameType:game,roundId:next,
      role:uid==='alice'?'Werewolf':'Villager',secret:'',createdAt:serverTimestamp()});
   }
   tx.set(state(db),{type:game,roundId:next,status:'active',participantUids:live,
     actorUid:'host',actorName:'Host',result:'',prompt:'Find the wolf',
     updatedAt:serverTimestamp()});
 });
 await assertSucceeds(publish(H,10,11,'werewolf'));
 const round=await getDoc(state(B));
 if(JSON.stringify(round.data().participantUids)!==JSON.stringify(['host','alice','bob','dora']))
   throw new Error('Departed Ready user improperly included');
 await assertSucceeds(getDoc(role(H,11,'host'))); // Host's own private role survives Activity recreation
 await assertFails(getDoc(role(B,11,'host'))); // Other Party accounts cannot peek at Host's role
 await assertSucceeds(getDoc(role(A,11,'alice')));
 await assertSucceeds(getDoc(role(B,11,'bob')));
 await assertFails(getDoc(role(B,11,'alice'))); // role private, not to other member
 await assertFails(getDoc(role(E,11,'bob')));
 await assertFails(getDoc(role(E,11,'eve')));
 for(const actor of [H,C]){
   let staleRejected=false;
   try{await publish(actor,10,12,'spy');}
   catch(e){staleRejected=String(e).includes('Round changed');}
   if(!staleRejected)throw new Error('Concurrent Host stale-round race was accepted');
 } // both Host and CoHost are rejected BEFORE any role writes
 const after=await getDoc(state(A));
 if(after.data().roundId!==11||after.data().type!=='werewolf')
   throw new Error('Concurrent Host overwrote social round');
 console.log('PASS live Werewolf: Host role persisted privately, member cannot read Host role, ghost Ready excluded and concurrent round denied');
}finally{await env.cleanup();}
