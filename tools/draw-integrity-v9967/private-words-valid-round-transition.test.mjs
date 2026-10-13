import fs from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,updateDoc,getDoc,deleteDoc,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({
 projectId:'demo-king-private-words-9965',
 firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}
});
const H=env.authenticatedContext('host').firestore();
const A=env.authenticatedContext('alice').firestore();
const B=env.authenticatedContext('bob').firestore();
const base='live_rooms/private-words-9965';
const member=(db,uid)=>doc(db,base,'members',uid);
const state=db=>doc(db,base,'game_state','current');
const privateWord=(db,round,uid)=>doc(db,base,'room_invites','game_'+round+'_'+uid);
const role=(recipientUid,role,secret,game,round)=>({
 senderUid:'host',senderName:'Host',recipientUid,recipientName:recipientUid,
 kind:'game_role',gameType:game,roundId:round,role,secret,
 createdAt:serverTimestamp()
});
try{
 await env.clearFirestore();
 await assertSucceeds(setDoc(doc(H,base),{
   ownerUid:'host',name:'KING real secret games',
   entranceMode:'free',closed:false,isPrivate:false,hasPassword:false
 }));
 for(const [db,uid] of [[H,'host'],[A,'alice'],[B,'bob']])
   await assertSucceeds(setDoc(member(db,uid),{uid,name:uid}));
 await assertSucceeds(setDoc(state(H),{
   actorUid:'host',actorName:'Host',type:'draw',status:'active',
   roundId:100,participantUids:['host','alice','bob'],
   drawerUid:'host',drawerName:'Host',sketch:'',result:'',
   prompt:'Draw & Guess',updatedAt:serverTimestamp()
 }));
 await assertSucceeds(setDoc(privateWord(H,100,'host'),role(
   'host','Drawer','TIGER','draw',100)));
 const publicState=await assertSucceeds(getDoc(state(B)));
 const data=publicState.data();
 if(JSON.stringify(data).includes('TIGER')||'secret' in data||'word' in data)
    throw new Error('Draw answer leaked into readable public Party round');
 const own=await assertSucceeds(getDoc(privateWord(H,100,'host')));
 if(!own.exists()||own.data().secret!=='TIGER')
   throw new Error('Real drawer cannot restore private word after recreation');
 await assertFails(getDoc(privateWord(A,100,'host')));
 await assertFails(getDoc(privateWord(B,100,'host')));
 // Real Party lifecycle: a Host finishes Draw before activating Spy.
 // Replacing the active Draw state would destroy the other device's canvas.
 await assertSucceeds(updateDoc(state(H),{
   status:'closed',actorUid:'host',updatedAt:serverTimestamp()
 }));
 await assertSucceeds(setDoc(state(H),{
   actorUid:'host',actorName:'Host',type:'spy',status:'active',
   roundId:101,participantUids:['host','alice','bob'],
   result:'',prompt:'Find the spy',updatedAt:serverTimestamp()
 }));
 await assertSucceeds(setDoc(privateWord(H,101,'alice'),role(
   'alice','Spy','','spy',101)));
 await assertSucceeds(setDoc(privateWord(H,101,'bob'),role(
   'bob','Player','Museum','spy',101)));
 const bob=await assertSucceeds(getDoc(privateWord(B,101,'bob')));
 if(bob.data().secret!=='Museum')throw new Error('Nonspy clue unavailable');
 const spy=await assertSucceeds(getDoc(privateWord(A,101,'alice')));
 if(spy.data().secret!=='')throw new Error('Spy learns private location');
 await assertFails(getDoc(privateWord(A,101,'bob')));
 const spyState=await getDoc(state(A));
 if(JSON.stringify(spyState.data()).includes('Museum')||
    'secret' in spyState.data())
   throw new Error('Public Party Spy state leaks secret word');
 console.log('PASS real Draw word recipient only & survives reentry; Spy gets no clue; clues hidden from other Party players and public game state');
}finally{await env.cleanup();}
