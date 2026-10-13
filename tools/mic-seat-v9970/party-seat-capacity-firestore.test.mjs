import fs from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,updateDoc,deleteDoc,getDoc,runTransaction,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({projectId:'demo-king-real-seats-9970',firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}});
const H=env.authenticatedContext('host').firestore(),A=env.authenticatedContext('alice').firestore(),B=env.authenticatedContext('bob').firestore(),E=env.authenticatedContext('eve').firestore();
const base='live_rooms/live-seats-9970';
const room=db=>doc(db,base), member=(db,uid)=>doc(db,base,'members',uid),seat=(db,id)=>doc(db,base,'seats',String(id));
const seatRecord=(uid)=>({uid,name:uid,micOn:false,joinedAt:serverTimestamp()});
async function move(db,uid,want){
 return runTransaction(db,async tx=>{
   const rr=await tx.get(room(db)),m=await tx.get(member(db,uid));
   if(!rr.exists()||!m.exists()||m.data().uid!==uid)throw Error('member missing');
   const max=rr.data().maxSeats??8;
   if(![8,10,12].includes(max)||want>max||want<1)throw Error('out-of-range');
   const snapshots=[];
   for(let i=1;i<=12;i++)snapshots.push(await tx.get(seat(db,i)));
   if(snapshots[want-1].exists()&&snapshots[want-1].data().uid!==uid)
       throw Error('occupied by another member');
   // All reads precede writes. Simultaneous phones auto-retry against same set.
   for(let i=1;i<=12;i++)if(i!==want&&snapshots[i-1].exists()&&snapshots[i-1].data().uid===uid)tx.delete(seat(db,i));
   if(snapshots[want-1].exists())tx.delete(seat(db,want));
   else tx.set(seat(db,want),seatRecord(uid));
 });
}
try{
 await env.clearFirestore();
 await assertSucceeds(setDoc(room(H),{ownerUid:'host',name:'Eight-seat Party',maxSeats:8,entranceMode:'free',isPrivate:false,closed:false,hasPassword:false}));
 for(const [db,uid] of [[H,'host'],[A,'alice'],[B,'bob'],[E,'eve']])await assertSucceeds(setDoc(member(db,uid),{uid,name:uid}));
 await assertSucceeds(setDoc(seat(A,8),seatRecord('alice')));
 await assertFails(setDoc(seat(A,9),seatRecord('alice')));
 await assertFails(setDoc(seat(H,13),seatRecord('host')));
 await assertFails(setDoc(seat(E,4),{...seatRecord('eve'),micOn:true}));
 await assertSucceeds(move(A,'alice',4));
 if((await getDoc(seat(H,8))).exists()||(await getDoc(seat(B,4))).data().uid!=='alice')throw Error('seat move left ghost');
 await assertFails(setDoc(seat(B,4),seatRecord('bob')));
 await assertSucceeds(updateDoc(room(H),{maxSeats:10}));
 await assertSucceeds(setDoc(seat(B,9),seatRecord('bob')));
 await assertFails(setDoc(seat(H,11),seatRecord('host')));
 await assertSucceeds(updateDoc(room(H),{maxSeats:12}));
 await assertSucceeds(setDoc(seat(H,12),seatRecord('host')));
 await Promise.allSettled([move(A,'alice',5),move(A,'alice',6)]);
 let count=0;
 for(let n=1;n<=12;n++){
   const d=await getDoc(seat(H,n));
   if(d.exists()&&d.data().uid==='alice')count++;
 }
 if(count!==1)throw Error('A signed-in user occupied '+count+' mic seats after simultaneous phone moves');
 await assertSucceeds(deleteDoc(seat(H,12)));
 console.log('PASS real Party two devices: 8→10→12 layouts, no invalid seat, no forced micOn, simultaneous claims reconcile');
}finally{await env.cleanup()}
