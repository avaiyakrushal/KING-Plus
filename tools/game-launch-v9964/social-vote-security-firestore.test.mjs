import fs from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,deleteDoc,getDoc,runTransaction,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({
  projectId:'demo-king-vote-integrity-9964',
  firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}
});
const H=env.authenticatedContext('host').firestore();
const A=env.authenticatedContext('alice').firestore();
const B=env.authenticatedContext('bob').firestore();
const D=env.authenticatedContext('dora').firestore();
const E=env.authenticatedContext('eve').firestore();
const base='live_rooms/real-social-vote-9964';
const member=(db,uid)=>doc(db,base,'members',uid);
const ready=(db,uid)=>doc(db,base,'game_ready',uid);
const vote=(db,uid)=>doc(db,base,'game_votes',uid);
const state=db=>doc(db,base,'game_state','current');
const ballot=(uid,choice,round)=>({
  uid,name:uid,roundId:round,choice,choiceName:choice,updatedAt:serverTimestamp()
});
const game=(type,round,users,status='active')=>({
  actorUid:'host',actorName:'Host',type,roundId:round,status,
  participantUids:users,prompt:'Find the true player',result:'',updatedAt:serverTimestamp()
});
try{
 await env.clearFirestore();
 await assertSucceeds(setDoc(doc(H,base),{
   ownerUid:'host',name:'KING Real Party Game',entranceMode:'free',
   isPrivate:false,closed:false,hasPassword:false
 }));
 for(const [db,uid] of [[H,'host'],[A,'alice'],[B,'bob'],[D,'dora'],[E,'eve']]){
   await assertSucceeds(setDoc(member(db,uid),{uid,name:uid}));
   await assertSucceeds(setDoc(ready(db,uid),{uid,name:uid,ready:true}));
 }
 await assertSucceeds(deleteDoc(member(E,'eve'))); // ghost Ready exists
 await assertSucceeds(setDoc(state(H),game('werewolf',77,['host','alice','bob'])));
 await assertSucceeds(setDoc(vote(A,'alice'),ballot('alice','bob',77)));
 await assertSucceeds(setDoc(vote(B,'bob'),ballot('bob','alice',77)));
 await assertSucceeds(setDoc(vote(H,'host'),ballot('host','bob',77)));
 await assertFails(setDoc(vote(D,'dora'),ballot('dora','alice',77))); // joined spectator
 await assertFails(setDoc(vote(E,'eve'),ballot('eve','bob',77))); // removed Party user
 await assertFails(setDoc(vote(A,'alice'),ballot('alice','dora',77))); // spectator target
 await assertFails(setDoc(vote(A,'alice'),ballot('alice','eve',77))); // departed target
 await assertFails(setDoc(vote(A,'alice'),ballot('alice','bob',76))); // past round
 await assertFails(setDoc(vote(H,'alice'),ballot('alice','bob',77))); // forged doc owner
 await assertSucceeds(setDoc(vote(A,'alice'),ballot('alice','host',77))); // change vote
 await assertSucceeds(setDoc(state(H),game('spy',78,['host','alice','bob'])));
 await assertFails(setDoc(vote(A,'alice'),ballot('alice','host',77))); // stale vote
 let rejected=false;
 try{
   await runTransaction(H,async tx=>{
     const fresh=await tx.get(state(H));
     if(fresh.data().roundId!==77||fresh.data().type!=='werewolf'
         ||fresh.data().status!=='active')
       throw new Error('Party round changed; cannot finish');
     tx.update(state(H),{status:'closed',result:'stale',updatedAt:serverTimestamp()});
   });
 }catch(e){rejected=String(e).includes('Party round changed');}
 if(!rejected)throw new Error('old moderator completed a different live round');
 const now=await getDoc(state(A));
 if(now.data().roundId!==78||now.data().type!=='spy'||now.data().status!=='active')
   throw new Error('stale moderator callback overwrote newer Spy game');
 await assertSucceeds(setDoc(state(H),game('draw',79,['host','alice','bob'])));
 await assertSucceeds(setDoc(vote(A,'alice'),ballot('alice','DIAMOND',79)));
 await assertFails(setDoc(vote(D,'dora'),ballot('dora','TIGER',79))); // spectator guess
 await assertSucceeds(setDoc(state(H),game('draw',79,['host','alice','bob'],'closed')));
 await assertFails(setDoc(vote(B,'bob'),ballot('bob','DIAMOND',79))); // finished
 console.log('PASS social-vote rules: actual Party roster, ghost/spectator/spoofed/stale votes denied; DRAW guesses allowed only when active; old Host cannot finish new round');
}finally{await env.cleanup();}
