import fs from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {collection,doc,setDoc,getDoc,getDocsFromServer,where,query,
        writeBatch,runTransaction,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({projectId:'demo-king-rematch-9976',
 firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}});
const H=env.authenticatedContext('host').firestore(),
 A=env.authenticatedContext('alice').firestore(),
 B=env.authenticatedContext('bob').firestore(),
 G=env.authenticatedContext('ghost').firestore();
const room='live_rooms/stale-game-history-9976',R=db=>doc(db,room);
const state=db=>doc(db,room,'game_state','current');
const member=(db,uid)=>doc(db,room,'members',uid);
const ready=(db,uid)=>doc(db,room,'game_ready',uid);
const move=(db,uid)=>doc(db,room,'game_moves',uid);
try{
 await env.clearFirestore();
 await assertSucceeds(setDoc(R(H),{ownerUid:'host',name:'KING long-lived real Party',
    isPrivate:false,hasPassword:false,locked:false,closed:false,entranceMode:'free'}));
 for(const [db,uid] of [[H,'host'],[A,'alice'],[B,'bob']])
   await assertSucceeds(setDoc(member(db,uid),{uid,name:uid}));
 for(const [db,uid] of [[A,'alice'],[B,'bob']])
   await assertSucceeds(setDoc(ready(db,uid),{uid,name:uid,ready:true,updatedAt:serverTimestamp()}));
 await assertSucceeds(setDoc(state(H),{actorUid:'host',type:'dice',status:'closed',roundId:9975,
  participantUids:['alice','bob'],prompt:'Previous round',result:'old',updatedAt:serverTimestamp()}));
 // 610 historic scores from accounts no longer in room. A legacy unbounded
 // transaction trying to delete all 612 docs would exceed 500 writes.
 await env.withSecurityRulesDisabled(async ctx=>{
  const admin=ctx.firestore();
  for(let offset=0;offset<610;offset+=250){
   const batch=writeBatch(admin);
   for(let i=offset;i<Math.min(offset+250,610);i++)
    batch.set(move(admin,'ghost-'+i),{uid:'ghost-'+i,type:'dice',
     name:'departed',roundId:7000+i,move:'Rolled 1',score:1});
   await batch.commit();
  }
  const batch=writeBatch(admin);
  for(const uid of ['alice','bob'])
   batch.set(move(admin,uid),{uid,name:uid,type:'dice',roundId:9975,move:'Rolled 2',score:2});
  await batch.commit();
 });
 const initial=await getDocsFromServer(collection(H,room,'game_moves'));
 if(initial.size!==612)throw Error('Expected 612 historic moves but got '+initial.size);
 const freshReady=await getDocsFromServer(query(collection(H,room,'game_ready'),where('ready','==',true)));
 const participants=freshReady.docs.map(d=>d.id).sort();
 if(participants.join(',')!=='alice,bob')throw Error('Wrong selected real Ready roster');
 const newRound=9976;
 await assertSucceeds(runTransaction(H,async tx=>{
  const current=await tx.get(state(H));
  if(!current.exists()||current.data().status!=='closed'||current.data().roundId!==9975)
    throw Error('Round changed');
  // Mirrors real Android code: all SERVER reads before transaction writes.
  const prior=[];
  for(const uid of participants){
   const r=await tx.get(ready(H,uid)),m=await tx.get(member(H,uid));
   if(!r.exists()||r.data().ready!==true||r.data().uid!==uid||
      !m.exists()||m.data().uid!==uid)throw Error('Player left or unready');
  }
  for(const uid of participants){
   const old=await tx.get(move(H,uid));
   if(old.exists())prior.push(move(H,uid));
  }
  for(const ref of prior)tx.delete(ref);
  tx.set(state(H),{actorUid:'host',type:'dice',status:'active',
    roundId:newRound,participantUids:participants,prompt:'Rematch in same room',
    result:'',updatedAt:serverTimestamp()});
 }));
 const live=await assertSucceeds(getDoc(state(B)));
 if(live.data().status!=='active'||live.data().roundId!==newRound)throw Error('Bob did not receive real new round');
 for(const uid of participants){
  const old=await getDoc(move(H,uid));
  if(old.exists())throw Error('Old move blocks '+uid);
 }
 for(const uid of ['ghost-0','ghost-609']){
  const stale=await getDoc(move(H,uid));
  if(!stale.exists())throw Error('Accidental wipe of historical ghost');
 }
 await assertSucceeds(setDoc(move(A,'alice'),{uid:'alice',name:'alice',
   type:'dice',roundId:newRound,move:'Rolled 6',score:6,updatedAt:serverTimestamp()}));
 await assertSucceeds(setDoc(move(B,'bob'),{uid:'bob',name:'bob',
   type:'dice',roundId:newRound,move:'Rolled 3',score:3,updatedAt:serverTimestamp()}));
 await assertFails(setDoc(move(G,'ghost'),{uid:'ghost',name:'ghost',
   type:'dice',roundId:newRound,move:'Rolled 6',score:6,updatedAt:serverTimestamp()}));
 console.log('PASS 612 old room game_moves, only Alice/Bob cleared atomically; both submit new real round; ghost denied; 610 historic docs safe');
}finally{await env.cleanup();}
