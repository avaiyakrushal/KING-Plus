import {readFileSync} from 'node:fs';
import {initializeTestEnvironment, assertSucceeds, assertFails} from '@firebase/rules-unit-testing';
import {doc, setDoc, getDoc, runTransaction, serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({
  projectId:'demo-king-vote-race-10004',
  firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}
});
try{
  await env.clearFirestore();
  const H=env.authenticatedContext('host').firestore();
  const A=env.authenticatedContext('alice').firestore();
  const base='live_rooms/room-vote-race-10004';
  const game=db=>doc(db,base,'game_state','current');
  const vote=db=>doc(db,base,'game_votes','alice');
  await assertSucceeds(setDoc(doc(H,base),{ownerUid:'host',name:'Race Test',entranceMode:'free',isPrivate:false,closed:false,hasPassword:false}));
  for(const [db,uid] of [[H,'host'],[A,'alice']]){
    await assertSucceeds(setDoc(doc(db,base,'members',uid),{uid,name:uid}));
  }
  const state=round=>({type:'vote',roundId:round,actorUid:'host',prompt:'Which game?',status:'active',result:'',
    participantUids:[],options:['Ludo','KTV'],updatedAt:serverTimestamp()});
  const ballot=(round,choice)=>({uid:'alice',name:'alice',roundId:round,choice,choiceIndex:0,updatedAt:serverTimestamp()});
  const commit=async(round,choice)=>runTransaction(A,async tx=>{
    const latest=await tx.get(game(A));
    if(!latest.exists()||latest.get('type')!=='vote'||latest.get('status')!=='active'||latest.get('roundId')!==round)
      throw new Error('Vote changed; reject stale submission');
    tx.set(vote(A),ballot(round,choice));
  });
  await assertSucceeds(setDoc(game(H),state(300)));
  await assertSucceeds(commit(300,'Ludo'));
  await assertSucceeds(setDoc(game(H),state(301)));
  let staleRejected=false;
  try {await commit(300,'KTV');} catch(e) {
    if(!String(e).includes('Vote changed'))throw e;
    staleRejected=true;
  }
  if(!staleRejected)throw Error('Stale round unexpectedly committed');
  const old=await assertSucceeds(getDoc(vote(A)));
  if(old.data().roundId!==300||old.data().choice!=='Ludo')throw Error('Rejected transaction mutated existing ballot');
  await assertFails(setDoc(vote(A),ballot(300,'KTV')));
  await assertSucceeds(commit(301,'KTV'));
  const current=await assertSucceeds(getDoc(vote(A)));
  if(current.data().roundId!==301||current.data().choice!=='KTV')throw Error('Current vote failed');
  await assertSucceeds(setDoc(game(H),{...state(301),status:'closed'}));
  let closedRejected=false;
  try {await commit(301,'Ludo');} catch(e) {
    if(!String(e).includes('Vote changed'))throw e;
    closedRejected=true;
  }
  if(!closedRejected)throw Error('Closed round unexpectedly committed');
  console.log('PASS v10.0.4: atomic vote, stale-round denial, live member ballot, closed-round denial');
} finally {await env.cleanup();}
