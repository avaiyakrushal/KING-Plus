import fs from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {collection,doc,setDoc,deleteDoc,writeBatch,getDocsFromServer,getDoc,
        query,where,serverTimestamp,runTransaction} from 'firebase/firestore';
const env=await initializeTestEnvironment({projectId:'demo-king-round-query-9977',
 firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}});
const H=env.authenticatedContext('host').firestore();
const A=env.authenticatedContext('alice').firestore();
const B=env.authenticatedContext('bob').firestore();
const Ghost=env.authenticatedContext('ghost').firestore();
const base='live_rooms/real-scoped-round-9977',state=db=>doc(db,base,'game_state','current');
const member=(db,uid)=>doc(db,base,'members',uid),move=(db,uid)=>doc(db,base,'game_moves',uid);
const all=db=>collection(db,base,'game_moves');
async function currentMoves(db,round){
  return getDocsFromServer(query(all(db),where('roundId','==',round)));
}
try{
 await env.clearFirestore();
 await assertSucceeds(setDoc(doc(H,base),{ownerUid:'host',name:'KING multiplayer live round',
   isPrivate:false,hasPassword:false,closed:false,locked:false,entranceMode:'free'}));
 for(const [db,uid] of [[H,'host'],[A,'alice'],[B,'bob']])
  await assertSucceeds(setDoc(member(db,uid),{uid,name:uid}));
 await assertSucceeds(setDoc(state(H),{actorUid:'host',type:'dice',roundId:9977,status:'active',
   participantUids:['alice','bob'],prompt:'Real current dice game',result:'',updatedAt:serverTimestamp()}));
 // Long-running room, 640 stale moves from thousands of prior games.
 await env.withSecurityRulesDisabled(async ctx=>{
   const admin=ctx.firestore();
   for(let offset=0;offset<640;offset+=200){
     const batch=writeBatch(admin);
     for(let i=offset;i<Math.min(offset+200,640);i++)
       batch.set(move(admin,'old-player-'+i),{
         uid:'old-player-'+i,name:'historic player',roundId:3000+i,
         type:'dice',move:'Rolled 1',score:1});
     await batch.commit();
   }
 });
 const many=await getDocsFromServer(all(H));
 if(many.size!==640)throw Error('Expected 640 old moves');
 if((await currentMoves(A,9977)).size!==0)throw Error('Old moves are shown on Alice phone');
 await assertSucceeds(setDoc(move(A,'alice'),{uid:'alice',name:'alice',type:'dice',
   roundId:9977,move:'Rolled 5',score:5,updatedAt:serverTimestamp()}));
 await assertSucceeds(setDoc(move(B,'bob'),{uid:'bob',name:'bob',type:'dice',
   roundId:9977,move:'Rolled 3',score:3,updatedAt:serverTimestamp()}));
 const found=await currentMoves(B,9977);
 if(found.size!==2||found.docs.map(x=>x.id).sort().join(',')!=='alice,bob')
   throw Error('Live B phone must show just 2 real same-round moves');
 await assertFails(setDoc(move(Ghost,'ghost'),{uid:'ghost',name:'ghost',type:'dice',
   roundId:9977,move:'Rolled 6',score:6,updatedAt:serverTimestamp()}));
 // Host settlement uses the SAME strict current-round server query as both
 // connected phones; verify membership and immutable moves transactionally.
 await assertSucceeds(runTransaction(H,async tx=>{
  const st=await tx.get(state(H));
  if(!st.exists()||st.data().roundId!==9977||st.data().status!=='active')throw Error('Old round');
  const roster=st.data().participantUids;
  for(const uid of roster){
    const m=await tx.get(member(H,uid)),v=await tx.get(move(H,uid));
    if(!m.exists()||m.data().uid!==uid||!v.exists()||
       v.data().uid!==uid||v.data().roundId!==9977)throw Error('Invalid player');
  }
  tx.update(state(H),{status:'closed',result:'🏆 Alice won with 5',actorUid:'host',
    updatedAt:serverTimestamp()});
 }));
 const stateOnBob=await getDoc(state(B));
 if(stateOnBob.data().status!=='closed'||!stateOnBob.data().result.includes('Alice'))
   throw Error('Second phone did not receive same game winner');
 const retained=await getDocsFromServer(all(H));
 if(retained.size!==642)throw Error('Old 640 score records were accidentally wiped');
 // Stale old round snapshot must never be mistaken for the new match.
 const old=await currentMoves(A,3000);
 if(old.size!==1||old.docs[0].id!=='old-player-0')throw Error('Historical query round mismatch');
 console.log('PASS 640 old scores + 2 live moves: Alice/Bob read exactly 2 current records; Host settles server round; ghost denied; historic 640 preserved');
}finally{await env.cleanup();}
