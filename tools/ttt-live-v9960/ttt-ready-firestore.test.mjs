import fs from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,deleteDoc,getDoc,getDocs,collection,runTransaction,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({
 projectId:'demo-king-real-ttt-9960',
 firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}
});
const H=env.authenticatedContext('host').firestore();
const A=env.authenticatedContext('alice').firestore();
const B=env.authenticatedContext('bob').firestore();
const D=env.authenticatedContext('dora').firestore();
const E=env.authenticatedContext('eve').firestore();
const base='live_rooms/ttt-live-9960';
const room=db=>doc(db,base);
const member=(db,uid)=>doc(db,base,'members',uid);
const ready=(db,uid)=>doc(db,base,'game_ready',uid);
const state=db=>doc(db,base,'game_state','current');
try{
 await env.clearFirestore();
 await assertSucceeds(setDoc(room(H),{
  ownerUid:'host',name:'KING Real Tic Tac Toe',
  entranceMode:'free',closed:false,isPrivate:false,hasPassword:false
 }));
 for(const [db,uid,flag] of [[H,'host',false],[A,'alice',true],[B,'bob',false],[D,'dora',true],[E,'eve',true]]){
  await assertSucceeds(setDoc(member(db,uid),{uid,name:uid}));
  await assertSucceeds(setDoc(ready(db,uid),{uid,name:uid,ready:flag,updatedAt:serverTimestamp()}));
 }
 await assertSucceeds(deleteDoc(member(E,'eve'))); // ghost has stale ready:true
 const initial=await getDocs(collection(H,base,'game_ready'));
 const candidates=initial.docs.map(d=>d.id).sort();
 const chosen=await assertSucceeds(runTransaction(H,async tx=>{
  const old=await tx.get(state(H));
  if(old.exists())throw new Error('Round already running');
  const actual=[];
  for(const uid of candidates){
    const rd=await tx.get(ready(H,uid));
    const mb=await tx.get(member(H,uid));
    if(rd.exists()&&rd.data().ready===true&&rd.data().uid===uid
       &&mb.exists()&&mb.data().uid===uid)actual.push(uid);
  }
  if(actual.length<2)throw new Error('Need two live ready players');
  // Deterministic selection for Firestore contract testing;
  // Android native SecureRandom pair fairness has separate Java tests.
  const [x,o]=[actual[0],actual[1]];
  tx.set(state(H),{
   actorUid:'host',actorName:'host',type:'ttt',status:'active',result:'',
   roundId:9960,prompt:x+' (X) vs '+o+' (O)',board:'.........',
   turnUid:x,xUid:x,oUid:o,xName:x,oName:o,participantUids:[x,o],
   updatedAt:serverTimestamp()
  });
  return [x,o];
 }));
 if(JSON.stringify(chosen)!==JSON.stringify(['alice','dora']))
   throw new Error('Unready Bob or ghost Eve was incorrectly admitted: '+chosen);
 const after=await getDoc(state(A));
 if(!after.exists()||after.data().type!=='ttt'
     ||JSON.stringify(after.data().participantUids)!==JSON.stringify(['alice','dora']))
    throw new Error('Tic Tac Toe did not publish the real two player round');
 console.log('PASS Firebase actual Ready:true live players Alice/Dora, unready Bob and ghost Eve excluded, new TTT state synchronized');
}finally{await env.cleanup();}
