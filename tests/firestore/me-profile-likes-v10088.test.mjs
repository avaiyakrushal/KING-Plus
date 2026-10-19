import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds} from '@firebase/rules-unit-testing';
import {collectionGroup,doc,onSnapshot,query,serverTimestamp,setDoc,deleteDoc,where,limit} from 'firebase/firestore';

const env=await initializeTestEnvironment({projectId:'demo-king-me-liked-count-10088',firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}});
const phoneA=env.authenticatedContext('alice').firestore();
const phoneB=env.authenticatedContext('alice').firestore();
const author=env.authenticatedContext('bob').firestore();
let stop=()=>{};
const waitForCount=(q,count)=>new Promise((resolve,reject)=>{
 const timer=setTimeout(()=>{stop();reject(Error('Live liked-Moment count did not reach '+count));},10000);
 stop=onSnapshot(q,snap=>{if(snap.size===count){clearTimeout(timer);resolve(snap.size);}},error=>{clearTimeout(timer);reject(error);});
});
try{
 await env.clearFirestore();
 const first=doc(phoneA,'moments/post-a/likes/alice');
 const second=doc(phoneA,'moments/post-b/likes/alice');
 const liveQuery=query(collectionGroup(phoneB,'likes'),where('uid','==','alice'),limit(501));
 let seen=waitForCount(liveQuery,1);
 await assertSucceeds(setDoc(doc(author,'moments/post-a'),{uid:'bob',name:'Bob',text:'Post A',createdAt:serverTimestamp()}));
 await assertSucceeds(setDoc(first,{uid:'alice',createdAt:serverTimestamp()}));
 if(await seen!==1)throw Error('First liked Moment count was not shown');
 stop();
 seen=waitForCount(liveQuery,2);
 await assertSucceeds(setDoc(doc(author,'moments/post-b'),{uid:'bob',name:'Bob',text:'Post B',createdAt:serverTimestamp()}));
 await assertSucceeds(setDoc(second,{uid:'alice',createdAt:serverTimestamp()}));
 if(await seen!==2)throw Error('Second liked Moment count was not live');
 stop();
 seen=waitForCount(liveQuery,1);
 await assertSucceeds(deleteDoc(first));
 if(await seen!==1)throw Error('Unlike did not reduce the live count');
 console.log('PASS v10.0.88 Me liked-Moments counter updates across two signed-in clients');
}finally{stop();await env.cleanup();}
