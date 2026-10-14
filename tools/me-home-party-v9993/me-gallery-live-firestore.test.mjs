import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds} from '@firebase/rules-unit-testing';
import {collection,doc,setDoc,deleteDoc,onSnapshot,query,where,limit,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({projectId:'demo-king-me-gallery-9993',firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}});
const a=env.authenticatedContext('alice').firestore();
const second=env.authenticatedContext('alice').firestore();
const b=env.authenticatedContext('bob').firestore();
let unsubscribe=()=>{};
try {
 await env.clearFirestore();
 const target=doc(a,'moments','gallery-live-9993');
 let initialized=false;
 const seen=new Promise((resolve,reject)=>{
   const timeout=setTimeout(()=>reject(Error('Live Me Gallery did not update on phone B')),12000);
   unsubscribe=onSnapshot(query(collection(second,'moments'),where('uid','==','alice'),limit(40)),snap=>{
     initialized=true;
     if(snap.docs.some(d=>d.id==='gallery-live-9993' && d.get('text')==='Live from phone A')){
       clearTimeout(timeout);resolve();
     }
   },e=>{clearTimeout(timeout);reject(e)});
 });
 await assertSucceeds(setDoc(target,{uid:'alice',name:'Alice',text:'Live from phone A',topic:'General',createdAt:serverTimestamp()}));
 await seen;
 if(!initialized)throw Error('No initial Gallery snapshot');
 await assertSucceeds(deleteDoc(target));
 let bobRows=0;
 const queryBob=query(collection(b,'moments'),where('uid','==','bob'),limit(40));
 const offBob=onSnapshot(queryBob,s=>{bobRows=s.docs.length});
 offBob();
 if(bobRows!==0)throw Error('Unexpected cross-user gallery');
 console.log('PASS v9.9.93 Me Gallery realtime listener on two clients');
}finally {unsubscribe();await env.cleanup();}
