import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,deleteDoc,getDoc,updateDoc,runTransaction,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({projectId:'demo-king-atomic-moderation-v10011',firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}});
try{
 await env.clearFirestore();
 const host=env.authenticatedContext('host').firestore(),bob=env.authenticatedContext('bob').firestore(),charlie=env.authenticatedContext('charlie').firestore(),dave=env.authenticatedContext('dave').firestore(),eve=env.authenticatedContext('eve').firestore();
 const room='live_rooms/moderation-room-10011';
 const r=(db,path)=>doc(db,room,...path.split('/'));
 await assertSucceeds(setDoc(doc(host,room),{ownerUid:'host',name:'KING Moderation TEST',entranceMode:'free',closed:false,isPrivate:false,maxSeats:12}));
 for(const [db,uid] of [[host,'host'],[bob,'bob'],[charlie,'charlie'],[dave,'dave']])await assertSucceeds(setDoc(r(db,'members/'+uid),{uid,name:uid}));
 for(const uid of ['bob','charlie'])await assertSucceeds(setDoc(r(host,'roles/'+uid),{uid,role:'cohost',grantedBy:'host'}));
 await assertSucceeds(setDoc(r(dave,'seats/1'),{uid:'dave',name:'Dave',micOn:false}));
 await assertSucceeds(setDoc(r(charlie,'seats/2'),{uid:'charlie',name:'Charlie',micOn:false}));
 await assertSucceeds(setDoc(r(dave,'seats/12'),{uid:'dave',name:'Dave',micOn:false}));
 await assertFails(deleteDoc(r(bob,'members/charlie')));
 await assertFails(setDoc(r(bob,'room_bans/charlie'),{uid:'charlie',active:true}));
 await assertFails(setDoc(r(bob,'room_bans/host'),{uid:'host',active:true}));
 await assertFails(deleteDoc(r(eve,'members/dave')));
 async function moderate(db,actor,target,ban=false){
  return runTransaction(db,async tx=>{
   const roomDoc=await tx.get(doc(db,room));
   const member=r(db,'members/'+target),role=r(db,'roles/'+target);
   const memberDoc=await tx.get(member),roleDoc=await tx.get(role);
   if(!memberDoc.exists())throw Error('already left');
   const cohost=roleDoc.data()?.role==='cohost';
   if(cohost&&actor!==roomDoc.data()?.ownerUid)throw Error('only host may remove cohost');
   const seats=[];
   for(let n=1;n<=12;n++){const ref=r(db,'seats/'+n),snap=await tx.get(ref);if(snap.exists()&&snap.data().uid===target)seats.push(ref);}
   tx.delete(member);for(const seat of seats)tx.delete(seat);
   if(cohost)tx.delete(role);
   if(ban)tx.set(r(db,'room_bans/'+target),{uid:target,active:true,reason:'Room violation',byUid:actor,createdAt:serverTimestamp()});
   return seats.length;
  });
 }
 if(await assertSucceeds(moderate(bob,'bob','dave'))!==2)throw Error('Not all mic seats cleared');
 for(const path of ['members/dave','seats/1','seats/12'])if((await assertSucceeds(getDoc(r(host,path)))).exists())throw Error('Ghost member or seat: '+path);
 await assertFails(setDoc(r(dave,'seats/1'),{uid:'dave',name:'Dave',micOn:false}));
 if(await assertSucceeds(moderate(host,'host','charlie',true))!==1)throw Error('Ban failed to clear mic');
 if((await assertSucceeds(getDoc(r(host,'roles/charlie')))).exists())throw Error('Stale cohost role');
 if(!(await assertSucceeds(getDoc(r(host,'room_bans/charlie')))).exists())throw Error('Ban not atomic');
 await assertFails(updateDoc(doc(charlie,room),{locked:true}));
 await assertFails(setDoc(r(charlie,'room_bans/bob'),{uid:'bob',active:true}));
 await assertFails(setDoc(r(charlie,'members/charlie'),{uid:'charlie',name:'Charlie'}));
 await assertSucceeds(deleteDoc(r(host,'members/bob')));
 await assertFails(updateDoc(doc(bob,room),{locked:true}));
 await assertFails(setDoc(r(bob,'room_bans/eve'),{uid:'eve',active:true}));
 console.log('PASS atomic kick/ban, 12 mic seats, revoked cohost, no unauthorized admin writes');
}finally{await env.cleanup();}
