import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,serverTimestamp,getDoc} from 'firebase/firestore';
const env=await initializeTestEnvironment({projectId:'demo-king-paired-scores-9979',
 firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}});
const Host=env.authenticatedContext('host').firestore(),
 Alice=env.authenticatedContext('alice').firestore(),
 Bob=env.authenticatedContext('bob').firestore(),
 Ghost=env.authenticatedContext('ghost').firestore();
const games=[
 {type:'wheel',ok:{move:'Spin 100',score:100},lie:{move:'Spin 1',score:100}},
 {type:'number',ok:{move:'30',score:30},lie:{move:'1',score:30}},
 {type:'bingo',ok:{move:'Pick 9',score:9},lie:{move:'Pick 1',score:9}},
 {type:'zoo',ok:{move:'🦁 Lion power 100',score:100},lie:{move:'🦁 Lion power 25',score:100}},
 {type:'reaction',ok:{move:'Reaction 215 ms',score:99785},lie:{move:'Reaction 30000 ms',score:99999}}
];
const root=t=>'live_rooms/integrated-real-'+t;
const room=(db,t)=>doc(db,root(t));
const member=(db,t,uid)=>doc(db,root(t),'members',uid);
const state=(db,t)=>doc(db,root(t),'game_state','current');
const move=(db,t,uid)=>doc(db,root(t),'game_moves',uid);
const data=(uid,t,round,x)=>({uid,name:uid,type:t,roundId:round,move:x.move,score:x.score,
 updatedAt:serverTimestamp()});
try{
 await env.clearFirestore();
 for(let i=0;i<games.length;i++){
   const t=games[i].type,round=997900+i;
   await assertSucceeds(setDoc(room(Host,t),{ownerUid:'host',name:'KING '+t,
      isPrivate:false,hasPassword:false,closed:false,locked:false,entranceMode:'free'}));
   for(const [db,uid] of [[Host,'host'],[Alice,'alice'],[Bob,'bob'],[Ghost,'ghost']]){
      await assertSucceeds(setDoc(member(db,t,uid),{uid,name:uid}));
   }
   await assertSucceeds(setDoc(state(Host,t),{actorUid:'host',type:t,status:'active',
     roundId:round,participantUids:['alice','bob'],prompt:'Play '+t,
     result:'',updatedAt:serverTimestamp()}));
   await assertSucceeds(setDoc(move(Alice,t,'alice'),data('alice',t,round,games[i].ok)));
   // Both numeric score and move label are individually "valid", but CONTRADICT.
   await assertFails(setDoc(move(Bob,t,'bob'),data('bob',t,round,games[i].lie)));
   await assertFails(setDoc(move(Ghost,t,'ghost'),data('ghost',t,round,games[i].ok)));
   await assertSucceeds(setDoc(move(Bob,t,'bob'),data('bob',t,round,games[i].ok)));
   const fromBob=await getDoc(move(Bob,t,'alice'));
   if(!fromBob.exists()||fromBob.data().move!==games[i].ok.move)
      throw new Error('Second account did not see Alice valid result for '+t);
 }
 console.log('PASS 5 Party games: mismatched but plausible score rejected, 2 signed-in players allowed, outsider denied, remote read matches');
}finally{await env.cleanup();}
