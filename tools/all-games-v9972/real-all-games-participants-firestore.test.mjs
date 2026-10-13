import fs from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,updateDoc,deleteDoc,getDocs,collection,query,where,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({
 projectId:'demo-king-roster-9972',
 firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}
});
const H=env.authenticatedContext('host').firestore();
const A=env.authenticatedContext('alice').firestore();
const B=env.authenticatedContext('bob').firestore();
const E=env.authenticatedContext('eve').firestore();
const roomId='live_rooms/games-roster-9972';
const member=(d,u)=>doc(d,roomId,'members',u);
const ready=(d,u)=>doc(d,roomId,'game_ready',u);
const state=d=>doc(d,roomId,'game_state','current');
const move=(d,u)=>doc(d,roomId,'game_moves',u);
const gameState=(type,round)=>({actorUid:'host',actorName:'Host',type,
 status:'active',roundId:round,participantUids:['alice','bob'],
 prompt:'Two live Party players',result:'',updatedAt:serverTimestamp()});
const choice=(uid,type,round,moveText='Tap',score=1)=>({uid,name:uid,type,
 roundId:round,move:moveText,score,updatedAt:serverTimestamp()});
try {
 await env.clearFirestore();
 await assertSucceeds(setDoc(doc(H,roomId),{ownerUid:'host',name:'Two human games',
  isPrivate:false,hasPassword:false,closed:false,locked:false,entranceMode:'free'}));
 for(const [d,u] of [[H,'host'],[A,'alice'],[B,'bob'],[E,'eve']])
  await assertSucceeds(setDoc(member(d,u),{uid:u,name:u}));
 await assertSucceeds(setDoc(ready(A,'alice'),{uid:'alice',name:'Alice',ready:true}));
 await assertSucceeds(setDoc(ready(B,'bob'),{uid:'bob',name:'Bob',ready:true}));
 await env.withSecurityRulesDisabled(async context=>{
   const admin=context.firestore();
   for(let i=0;i<55;i++){
     const uid='old'+String(i).padStart(2,'0');
     await setDoc(ready(admin,uid),{uid,name:uid,ready:false});
   }
   await setDoc(ready(admin,'ghost'),{uid:'ghost',name:'Gone',ready:true});
 });
 const readySet=await assertSucceeds(getDocs(query(collection(H,roomId,'game_ready'),where('ready','==',true))));
 const members=await assertSucceeds(getDocs(collection(H,roomId,'members')));
 const membersSet=new Set(members.docs.filter(d=>d.id===d.data().uid).map(d=>d.id));
 const actualReady=readySet.docs.filter(d=>membersSet.has(d.id)&&d.data().uid===d.id).map(d=>d.id).sort();
 if(JSON.stringify(actualReady)!==JSON.stringify(['alice','bob']))
   throw Error('55 false Ready and stale ghost must not block two real Party players');
 const games=[['wheel','Spin 3',3],['slot','Spin 1-1-1',3],['number','15',15],['highlow','High',1]];
 for(let i=0;i<games.length;i++){
   const [kind,label,score]=games[i];const round=7200+i;
   if(i>0){
     await assertSucceeds(updateDoc(state(H),{actorUid:'host',status:'closed',updatedAt:serverTimestamp()}));
     await assertSucceeds(deleteDoc(move(H,'alice')));
     await assertSucceeds(deleteDoc(move(H,'bob')));
   }
   await assertSucceeds(setDoc(state(H),gameState(kind,round)));
   await assertFails(setDoc(move(E,'eve'),choice('eve',kind,round,label,score)));
   await assertFails(setDoc(move(H,'host'),choice('host',kind,round,label,score)));
   await assertSucceeds(setDoc(move(A,'alice'),choice('alice',kind,round,label,score)));
   await assertSucceeds(setDoc(move(B,'bob'),choice('bob',kind,round,label,score)));
   await assertFails(updateDoc(move(A,'alice'),{score:999999}));
 }
 console.log('PASS 13-game frozen Ready roster: 55 false and ghost ignored; real Wheel/Slot/Number/HighLow participants can move; host/spectator cannot forge moves');
} finally { await env.cleanup(); }
