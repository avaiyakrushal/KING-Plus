import {readFileSync} from 'node:fs';
import {initializeTestEnvironment, assertSucceeds, assertFails} from '@firebase/rules-unit-testing';
import {doc,collection,query,orderBy,limit,getDocs,getDoc,setDoc,deleteDoc,writeBatch,Timestamp,onSnapshot,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({projectId:'demo-king-moments-v10000',firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}});
try{
 await env.clearFirestore();
 await env.withSecurityRulesDisabled(async ctx=>{
  const db=ctx.firestore(),batch=writeBatch(db);
  for(let i=1;i<=125;i++)batch.set(doc(db,'moments','moment-'+String(i).padStart(3,'0')),{uid:'alice',name:'Alice',
   topic:i===5?'Music':'General',text:i===5?'Old party broadcast':'KING Moment '+i,
   createdAt:Timestamp.fromMillis(1700000000000+i*1000),
   ...(i===5?{type:'room_broadcast',roomId:'room-old',roomName:'Music Party',photoUrl:'https://firebasestorage.googleapis.com/v0/b/demo/o/chat_media%2Falice%2Fmoments%2Flarge.jpg?alt=media'}:{})});
  await batch.commit();
 });
 const alice1=env.authenticatedContext('alice').firestore(),alice2=env.authenticatedContext('alice').firestore();
 const bob=env.authenticatedContext('bob').firestore(),guest=env.unauthenticatedContext().firestore();
 const collectionRef=collection(alice1,'moments');
 const recent=await assertSucceeds(getDocs(query(collectionRef,orderBy('createdAt','desc'),limit(80))));
 if(recent.size!==80||recent.docs.some(d=>d.get('topic')==='Music'))throw Error('Seed did not exercise older-topic paging');
 const extended=await assertSucceeds(getDocs(query(collectionRef,orderBy('createdAt','desc'),limit(160))));
 if(extended.size!==125||!extended.docs.some(d=>d.get('topic')==='Music'&&d.get('type')==='room_broadcast'))throw Error('Older Party room Moment missing after increased limit');
 const target=doc(alice1,'moments','moment-005'),accountLike=doc(alice1,'moments','moment-005','likes','alice');
 const secondDeviceLike=doc(alice2,'moments','moment-005','likes','alice');
 let liveSeen=false;let stop=()=>{};
 const live=new Promise((resolve,reject)=>{
  const timer=setTimeout(()=>reject(new Error('Second phone never received live Like')),6000);
  stop=onSnapshot(secondDeviceLike,snap=>{if(snap.exists()){liveSeen=true;clearTimeout(timer);resolve(true);}},error=>{clearTimeout(timer);reject(error);});
 });
 await assertSucceeds(setDoc(accountLike,{uid:'alice',createdAt:serverTimestamp()}));
 await live;
 stop();
 if(!liveSeen)throw Error('Like was not realtime');
 if(!(await assertSucceeds(getDoc(doc(bob,'moments','moment-005')))).exists())throw Error('Room broadcast not visible to second user');
 await assertFails(setDoc(doc(bob,'moments','moment-005','likes','alice'),{uid:'alice'}));
 await assertFails(getDoc(doc(guest,'moments','moment-005')));
 await assertSucceeds(deleteDoc(accountLike));
 if((await assertSucceeds(getDoc(secondDeviceLike))).exists())throw Error('Unliked moment still liked on second phone');
 console.log('PASS 125 Moments: older topic/Party discovery, real cross-device Like updates, unlike and security');
}finally{await env.cleanup();}
