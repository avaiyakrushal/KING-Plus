import fs from 'node:fs';
import {initializeTestEnvironment,assertSucceeds} from '@firebase/rules-unit-testing';
import {doc,setDoc,getDoc,deleteDoc,runTransaction,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({projectId:'demo-king-departure-9975',firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}});
const H=env.authenticatedContext('host').firestore();
const A=env.authenticatedContext('alice').firestore(),B=env.authenticatedContext('bob').firestore(),C=env.authenticatedContext('cara').firestore(),D=env.authenticatedContext('dan').firestore();
const root='live_rooms/real-departed-party-9975',roster=['alice','bob','cara','dan'];
const member=(db,uid)=>doc(db,root,'members',uid);
const state=db=>doc(db,root,'game_state','current');
const move=(db,uid)=>doc(db,root,'game_moves',uid);
async function finishFromRealHost(){
 return runTransaction(H,async tx=>{
   const st=await tx.get(state(H));
   if(!st.exists()||st.data().status!=='active'||st.data().roundId!==7501||st.data().type!=='wheel')throw Error('wrong active game');
   const current=st.data().participantUids;
   if(JSON.stringify(current)!==JSON.stringify(roster))throw Error('roster changed');
   // Mirrors actual Android current-live-member and immutable-move reads.
   for(const uid of current){
     const live=await tx.get(member(H,uid));
     if(!live.exists()||live.data().uid!==uid)throw Error('Ready player left Party • Cancel without winner');
     const played=await tx.get(move(H,uid));
     if(!played.exists()||played.data().uid!==uid||played.data().roundId!==7501||played.data().type!=='wheel')throw Error('missing real move');
   }
   tx.update(state(H),{actorUid:'host',status:'closed',result:'🏁 All 4 real members finished',updatedAt:serverTimestamp()});
 });
}
try{
 await env.clearFirestore();
 await assertSucceeds(setDoc(doc(H,root),{ownerUid:'host',name:'KING multiplayer departure',isPrivate:false,hasPassword:false,closed:false,locked:false,entranceMode:'free'}));
 for(const [db,uid] of [[H,'host'],[A,'alice'],[B,'bob'],[C,'cara'],[D,'dan']])
   await assertSucceeds(setDoc(member(db,uid),{uid,name:uid}));
 await assertSucceeds(setDoc(state(H),{actorUid:'host',type:'wheel',status:'active',roundId:7501,participantUids:roster,prompt:'Four real players',result:'',updatedAt:serverTimestamp()}));
 for(const [db,uid,score] of [[A,'alice',2],[B,'bob',4],[C,'cara',5],[D,'dan',8]])
   await assertSucceeds(setDoc(move(db,uid),{uid,name:uid,type:'wheel',roundId:7501,move:'Spin '+score,score,updatedAt:serverTimestamp()}));
 // Dan has ALREADY played and scored before leaving. Old code declared winner.
 await assertSucceeds(deleteDoc(member(D,'dan')));
 let refused=false;
 try{await finishFromRealHost()}catch(e){refused=String(e).includes('Ready player left Party')}
 if(!refused)throw Error('Departed scorer falsely won');
 const still=await getDoc(state(B));
 if(still.data().status!=='active'||still.data().result!=='')throw Error('Bob saw false winner');
 // Only after Dan is once again a real member may a result be published.
 await assertSucceeds(setDoc(member(D,'dan'),{uid:'dan',name:'dan'}));
 await assertSucceeds(finishFromRealHost());
 const final=await getDoc(state(C));
 if(final.data().status!=='closed'||!final.data().result.includes('All 4'))throw Error('Valid round did not finish');
 console.log('PASS Firebase 5-account Party: departed 4th scorer blocks winner; no false result on Bob; verified live roster can finish');
}finally{await env.cleanup();}
