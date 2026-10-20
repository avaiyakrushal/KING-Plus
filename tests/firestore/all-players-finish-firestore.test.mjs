import fs from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,getDoc,getDocsFromServer,collection,updateDoc,deleteDoc,runTransaction,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({projectId:'demo-king-all-played-9973',firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}});
const H=env.authenticatedContext('host').firestore(),
 A=env.authenticatedContext('alice').firestore(),B=env.authenticatedContext('bob').firestore(),
 C=env.authenticatedContext('cara').firestore(),D=env.authenticatedContext('dan').firestore(),
 E=env.authenticatedContext('eve').firestore();
const root='live_rooms/all-ready-must-play-9973',roster=['alice','bob','cara','dan'];
const member=(db,uid)=>doc(db,root,'members',uid),
 state=db=>doc(db,root,'game_state','current'),
 move=(db,uid)=>doc(db,root,'game_moves',uid);
const makeMove=(uid,round)=>({
 uid,name:uid,type:'wheel',roundId:round,move:'Spin 4',score:4,updatedAt:serverTimestamp()
});
async function finish(round){
 const q=await getDocsFromServer(collection(H,root,'game_moves'));
 const moves=q.docs.filter(x=>x.data().roundId===round&&
  x.data().type==='wheel'&&x.id===x.data().uid);
 const submitted=moves.map(x=>x.id);
 if(submitted.length!==roster.length||
    new Set(submitted).size!==roster.length||
    !submitted.every(x=>roster.includes(x)))
    throw new Error('Waiting for all real Ready players');
 const expected=new Map(moves.map(x=>[x.id,x.data()]));
 await runTransaction(H,async tx=>{
   const active=await tx.get(state(H));
   if(!active.exists()||active.data().status!=='active'||
      active.data().roundId!==round||
      JSON.stringify(active.data().participantUids)!==JSON.stringify(roster))
     throw new Error('Game changed before winner settlement');
   for(const uid of roster){
     const actual=await tx.get(move(H,uid));
     if(!actual.exists()||actual.data().uid!==uid||
        actual.data().roundId!==round||actual.data().type!=='wheel'||
        actual.data().score!==expected.get(uid)?.score||
        actual.data().move!==expected.get(uid)?.move)
       throw new Error('Actual Party move disappeared/changed');
   }
   tx.update(state(H),{actorUid:'host',status:'closed',result:'🏁 Every Ready player played',
                      updatedAt:serverTimestamp()});
 });
}
async function expectWaiting(round,label){
 let denied=false;try{await finish(round)}catch(e){denied=String(e).includes('Waiting')}
 if(!denied)throw new Error(label);
 const live=await getDoc(state(B));
 if(live.data().status!=='active'||live.data().result!=='')
   throw new Error('Premature winner was displayed to second Android account');
}
try{
 await env.clearFirestore();
 await assertSucceeds(setDoc(doc(H,root),{
 ownerUid:'host',name:'KING all players real Party',isPrivate:false,
 hasPassword:false,locked:false,closed:false,entranceMode:'free'}));
 for(const [db,uid] of [[H,'host'],[A,'alice'],[B,'bob'],[C,'cara'],[D,'dan'],[E,'eve']])
  await assertSucceeds(setDoc(member(db,uid),{uid,name:uid}));
 await assertSucceeds(setDoc(state(H),{
  actorUid:'host',type:'wheel',status:'active',roundId:4301,
  participantUids:roster,prompt:'All four should spin',result:'',updatedAt:serverTimestamp()
 }));
 await assertSucceeds(setDoc(move(A,'alice'),makeMove('alice',4301)));
 await assertSucceeds(setDoc(move(B,'bob'),makeMove('bob',4301)));
 await expectWaiting(4301,'2 of 4 moves MUST NOT settle');
 await assertFails(setDoc(move(E,'eve'),makeMove('eve',4301)));
 await assertSucceeds(setDoc(move(C,'cara'),makeMove('cara',4301)));
 await expectWaiting(4301,'3 of 4 moves MUST NOT settle');
 await assertSucceeds(setDoc(move(D,'dan'),makeMove('dan',4301)));
 // A host accidentally deletes an actual player move just before settlement.
 await assertSucceeds(deleteDoc(move(H,'dan')));
 await expectWaiting(4301,'Deleted 4th move must BLOCK settlement');
 await assertSucceeds(setDoc(move(D,'dan'),makeMove('dan',4301)));
 await assertSucceeds(finish(4301));
 const result=await assertSucceeds(getDoc(state(B)));
 if(result.data().status!=='closed'||!result.data().result.includes('Every Ready player'))
  throw new Error('All four players played but second phone did not receive winner');
 await assertFails(setDoc(move(E,'eve'),makeMove('eve',4301)));
 console.log('PASS 4-account real Party: 2-of-4 and 3-of-4 blocked, spectator denied, missing move blocked, 4-of-4 server-transaction winner on second phone');
}finally{await env.cleanup();}
