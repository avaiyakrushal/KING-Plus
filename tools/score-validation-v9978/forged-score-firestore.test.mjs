import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,serverTimestamp} from 'firebase/firestore';
const projectId='demo-king-real-scores-9978';
const env=await initializeTestEnvironment({projectId,
 firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}});
const H=env.authenticatedContext('host').firestore();
const A=env.authenticatedContext('alice').firestore();
const B=env.authenticatedContext('bob').firestore();
const E=env.authenticatedContext('eve').firestore();
const games=[
 {type:'wheel',valid:{move:'Spin 100',score:100},fake:{move:'Spin 999999',score:999999}},
 {type:'number',valid:{move:'30',score:30},fake:{move:'99999',score:99999}},
 {type:'bingo',valid:{move:'Pick 9',score:9},fake:{move:'Pick 999',score:999}},
 {type:'highlow',valid:{move:'High',score:1},fake:{move:'High',score:999999}},
 {type:'slot',valid:{move:'Spin 9-9-9',score:77},fake:{move:'Spin 9-9-9',score:9999}},
 {type:'zoo',valid:{move:'🦁 Lion power 100',score:100},fake:{move:'🦁 Lion power 9999',score:9999}},
 {type:'reaction',valid:{move:'Reaction 215 ms',score:99785},fake:{move:'Reaction 1 ms',score:999999}}
];
const roomId=(game)=>'real-game-scores-'+game;
const root=(game)=>'live_rooms/'+roomId(game);
const room=(db,game)=>doc(db,root(game));
const member=(db,game,uid)=>doc(db,root(game),'members',uid);
const move=(db,game,uid)=>doc(db,root(game),'game_moves',uid);
const state=(db,game)=>doc(db,root(game),'game_state','current');
const payload=(uid,game,round,gameMove)=>({uid,name:uid,
 type:game,roundId:round,move:gameMove.move,score:gameMove.score,updatedAt:serverTimestamp()});
try{
 await env.clearFirestore();
 let count=0;
 for(let i=0;i<games.length;i++){
   const g=games[i],round=78000+i;
   await assertSucceeds(setDoc(room(H,g.type),{ownerUid:'host',name:'KING '+g.type,
     isPrivate:false,hasPassword:false,closed:false,locked:false,entranceMode:'free'}));
   for(const [db,uid] of [[H,'host'],[A,'alice'],[B,'bob'],[E,'eve']])
     await assertSucceeds(setDoc(member(db,g.type,uid),{uid,name:uid}));
   await assertSucceeds(setDoc(state(H,g.type),{actorUid:'host',type:g.type,status:'active',
     roundId:round,participantUids:['alice','bob'],prompt:'Play real '+g.type,
     result:'',updatedAt:serverTimestamp()}));
   await assertSucceeds(setDoc(move(A,g.type,'alice'),payload('alice',g.type,round,g.valid)));
   await assertFails(setDoc(move(B,g.type,'bob'),payload('bob',g.type,round,g.fake)));
   // A valid-looking move from a user NOT in the frozen roster remains denied.
   await assertFails(setDoc(move(E,g.type,'eve'),payload('eve',g.type,round,g.valid)));
   // Normal valid Bob is accepted after the invalid attempt (no poisoned state).
   await assertSucceeds(setDoc(move(B,g.type,'bob'),payload('bob',g.type,round,g.valid)));
   count++;
 }
 console.log('PASS '+count+' real Party game types: honest 2/2 moves allowed, fabricated score blocked, spectator rejected');
}finally{await env.cleanup();}
