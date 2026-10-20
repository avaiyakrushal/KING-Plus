import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,updateDoc,getDoc,getDocs,collection,serverTimestamp} from 'firebase/firestore';

const env=await initializeTestEnvironment({projectId:'demo-king-real-bingo-9980',
  firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}});
const H=env.authenticatedContext('host').firestore();
const A=env.authenticatedContext('alice').firestore();
const B=env.authenticatedContext('bob').firestore();
const G=env.authenticatedContext('ghost').firestore();
const root='live_rooms/live-bingo-two-phones-9980';
const room=db=>doc(db,root),member=(db,uid)=>doc(db,root,'members',uid);
const game=db=>doc(db,root,'live_bingo9980','current');
const card=(db,uid)=>doc(db,root,'live_bingo9980','current','cards',uid);
const claim=(db,uid)=>doc(db,root,'live_bingo9980','current','claims',uid);
function board(offset){
 const nums=Array(25).fill(0);
 for(let row=0;row<5;row++)for(let col=0;col<5;col++)
   nums[row*5+col]=col*15+row+1+offset;
 nums[12]=0;
 return nums;
}
const alice=board(0),bob=board(5);
try{
 await env.clearFirestore();
 await assertSucceeds(setDoc(room(H),{ownerUid:'host',name:'KING two-phone real Bingo',
   isPrivate:false,hasPassword:false,closed:false,locked:false,entranceMode:'free'}));
 for(const [db,uid] of [[H,'host'],[A,'alice'],[B,'bob']])
   await assertSucceeds(setDoc(member(db,uid),{uid,name:uid}));
 await assertSucceeds(setDoc(game(H),{roundId:9980,status:'waiting',
     called:[0],participants:[],updatedAt:serverTimestamp()}));
 await assertFails(setDoc(card(G,'ghost'),{uid:'ghost',roundId:9980,numbers:alice,
     createdAt:serverTimestamp()}));
 await assertFails(setDoc(card(A,'alice'),{uid:'alice',roundId:9980,
     numbers:Array(25).fill(1),createdAt:serverTimestamp()}));
 await assertSucceeds(setDoc(card(A,'alice'),{uid:'alice',roundId:9980,
     numbers:alice,createdAt:serverTimestamp()}));
 await assertSucceeds(setDoc(card(B,'bob'),{uid:'bob',roundId:9980,
     numbers:bob,createdAt:serverTimestamp()}));
 await assertFails(setDoc(card(A,'alice'),{uid:'alice',roundId:9980,
     numbers:bob,createdAt:serverTimestamp()}));
 await assertFails(setDoc(claim(A,'alice'),{uid:'alice',roundId:9980,
     createdAt:serverTimestamp()}));
 await assertFails(updateDoc(game(B),{status:'active',participants:['alice','bob'],
     updatedAt:serverTimestamp()}));
 await assertSucceeds(updateDoc(game(H),{status:'active',participants:['alice','bob'],
     updatedAt:serverTimestamp()}));
 await assertFails(setDoc(card(G,'ghost'),{uid:'ghost',roundId:9980,numbers:alice,
     createdAt:serverTimestamp()}));
 await assertFails(setDoc(claim(A,'alice'),{uid:'alice',roundId:9980,
     createdAt:serverTimestamp()}));
 let called=[0],draws=alice.slice(0,5);
 for(const n of draws.slice(0,4)){
    called=[...called,n];
    await assertSucceeds(updateDoc(game(H),{called,updatedAt:serverTimestamp()}));
 }
 await assertFails(setDoc(claim(A,'alice'),{uid:'alice',roundId:9980,
     createdAt:serverTimestamp()}));
 await assertFails(updateDoc(game(H),{called:[...called,called[1]],
     updatedAt:serverTimestamp()}));
 called=[...called,draws[4]];
 await assertSucceeds(updateDoc(game(H),{called,updatedAt:serverTimestamp()}));
 const bobView=await assertSucceeds(getDoc(game(B)));
 if(!bobView.exists()||bobView.data().called.length!==6)
    throw new Error('Bob live client did not see all Bingo calls');
 await assertSucceeds(setDoc(claim(A,'alice'),{uid:'alice',roundId:9980,
    createdAt:serverTimestamp()}));
 await assertFails(setDoc(claim(B,'bob'),{uid:'bob',roundId:9980,
    createdAt:serverTimestamp()}));
 await assertFails(setDoc(claim(A,'alice'),{uid:'alice',roundId:9980,
    createdAt:serverTimestamp()}));
 const onBob=await assertSucceeds(getDoc(claim(B,'alice')));
 if(!onBob.exists()||onBob.data().uid!=='alice')
    throw new Error('Real Bingo win not synchronized to Bob phone');
 await assertSucceeds(updateDoc(game(H),{status:'closed',updatedAt:serverTimestamp()}));
 await assertSucceeds(setDoc(game(H),{roundId:9981,status:'waiting',
     called:[0],participants:[],updatedAt:serverTimestamp()}));
 await assertSucceeds(setDoc(card(A,'alice'),{uid:'alice',roundId:9981,numbers:alice,
     createdAt:serverTimestamp()}));
 const again=await assertSucceeds(getDoc(card(B,'alice')));
 if(again.data().roundId!==9981)throw Error('Real rematch card not persisted');
 console.log('PASS 5x5 BINGO: two actual Firebase clients, moderator-only calls, 5-ball real line win, incomplete line denied, guest rejected, claim once, new round');
}finally{await env.cleanup();}
