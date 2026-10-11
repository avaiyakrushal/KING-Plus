import fs from 'node:fs';
import assert from 'node:assert/strict';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,collection,query,where,getDocs,setDoc,updateDoc,deleteDoc,getDoc,runTransaction,serverTimestamp} from 'firebase/firestore';

const env=await initializeTestEnvironment({
  projectId:'demo-kingplus-video-9921',
  firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}
});
const alice=env.authenticatedContext('alice').firestore();
const bob=env.authenticatedContext('bob').firestore();
const eve=env.authenticatedContext('eve').firestore();
const stranger=env.authenticatedContext('stranger').firestore();
const room=db=>doc(db,'live_rooms','video-test');
const member=(db,id)=>doc(db,'live_rooms','video-test','members',id);
const seat=(db,n)=>doc(db,'live_rooms','video-test','video_seats',String(n));
const owner=(db,id)=>doc(db,'live_rooms','video-test','video_member_seats',id);
const invite=(db,id)=>doc(db,'live_rooms','video-test','video_seat_invites',id);
const role=(db,id)=>doc(db,'live_rooms','video-test','roles',id);

async function claim(db,id,n){
  return runTransaction(db,async tx=>{
    const m=await tx.get(member(db,id)),own=await tx.get(owner(db,id)),target=await tx.get(seat(db,n));
    if(!m.exists()||own.exists()||target.exists())throw Error('Member or seat unavailable');
    tx.set(seat(db,n),{uid:id,name:id,micOn:false,cameraOn:false,updatedAt:serverTimestamp()});
    tx.set(owner(db,id),{uid:id,seatNo:n,updatedAt:serverTimestamp()});
  });
}
async function leave(db,id,n){
  return runTransaction(db,async tx=>{
    const s=await tx.get(seat(db,n)),o=await tx.get(owner(db,id));
    if(!s.exists()||s.data().uid!==id)return false;
    tx.delete(seat(db,n));
    if(o.exists()&&o.data().uid===id&&o.data().seatNo===n)tx.delete(owner(db,id));
    return true;
  });
}
try{
  await env.clearFirestore();
  await assertSucceeds(setDoc(room(alice),{ownerUid:'alice',name:'KING Video',isPrivate:false,locked:false,closed:false}));
  for(const [db,id] of [[alice,'alice'],[bob,'bob'],[eve,'eve']])
    await assertSucceeds(setDoc(member(db,id),{uid:id,name:id}));
  const outcomes=await Promise.allSettled([claim(alice,'alice',1),claim(bob,'bob',1)]);
  assert.equal(outcomes.filter(x=>x.status==='fulfilled').length,1,'exactly one winner of same seat');
  const seatOne=(await assertSucceeds(getDoc(seat(alice,1)))).data();
  assert.ok(['alice','bob'].includes(seatOne.uid));
  console.log('PASS contested video seat assigned atomically to one real account');

  const loser=seatOne.uid==='alice'?{id:'bob',db:bob}:{id:'alice',db:alice};
  const winner=seatOne.uid==='alice'?{id:'alice',db:alice}:{id:'bob',db:bob};
  await assertFails(setDoc(seat(eve,1),{uid:'eve',name:'Eve',micOn:false,cameraOn:false,updatedAt:serverTimestamp()}));
  await assertFails(updateDoc(seat(loser.db,1),{micOn:true,updatedAt:serverTimestamp()}));
  await assertSucceeds(updateDoc(seat(winner.db,1),{micOn:true,updatedAt:serverTimestamp()}));
  assert.equal((await getDoc(seat(alice,1))).data().micOn,true);
  await assertFails(getDoc(seat(stranger,1)));
  console.log('PASS other user cannot mic/unmute occupied seat or read without membership');

  await assert.rejects(claim(winner.db,winner.id,2),/Member or seat unavailable/);
  await assertSucceeds(claim(loser.db,loser.id,2));
  assert.equal((await getDoc(seat(alice,2))).data().uid,loser.id);
  console.log('PASS one live video seat per authenticated account');

  await assertFails(setDoc(invite(eve,'bob'),{recipientUid:'bob',seatNo:3,actorUid:'eve',actorName:'Eve',createdAt:serverTimestamp()}));
  await assertSucceeds(setDoc(invite(alice,'bob'),{recipientUid:'bob',seatNo:3,actorUid:'alice',actorName:'Host',createdAt:serverTimestamp()}));
  await assertSucceeds(getDoc(invite(bob,'bob')));
  await assertSucceeds(deleteDoc(invite(bob,'bob')));
  console.log('PASS moderator-only video invitation delivered privately');

  await assertSucceeds(setDoc(role(alice,'eve'),{uid:'eve',role:'cohost'}));
  await assertSucceeds(deleteDoc(seat(eve,2)));
  // cohost may remove somebody, while a nonmoderator cannot change other seat.
  await assertFails(deleteDoc(seat(stranger,1)));
  await assertSucceeds(leave(winner.db,winner.id,1));
  assert.equal((await getDoc(seat(alice,1))).exists(),false);
  assert.equal((await getDoc(owner(winner.db,winner.id))).exists(),false);
  console.log('PASS moderator rights and owner-safe seat release');

  // Stale same-UID claim cannot evict anyone after its seat disappears.
  assert.equal(await leave(winner.db,winner.id,1),false);
  console.log('PASS no phantom event-based video seating on stale leave');
}finally{await env.cleanup();}
