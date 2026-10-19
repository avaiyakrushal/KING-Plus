import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds} from '@firebase/rules-unit-testing';
import {collectionGroup,doc,query,serverTimestamp,setDoc,deleteDoc,where,limit,getDocs} from 'firebase/firestore';

const env=await initializeTestEnvironment({projectId:'demo-king-me-liked-count-10088',firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}});
const phoneA=env.authenticatedContext('liked-count-alice-v10088').firestore();
const phoneB=env.authenticatedContext('liked-count-alice-v10088').firestore();
const author=env.authenticatedContext('liked-count-bob-v10088').firestore();
try{
 await env.clearFirestore();
 const first=doc(phoneA,'moments/post-a/likes/liked-count-alice-v10088');
 const second=doc(phoneA,'moments/post-b/likes/liked-count-alice-v10088');
 const liveQuery=query(collectionGroup(phoneB,'likes'),where('uid','==','liked-count-alice-v10088'),limit(501));
 await assertSucceeds(setDoc(doc(author,'moments/post-a'),{uid:'liked-count-bob-v10088',name:'Bob',text:'Post A',createdAt:serverTimestamp()}));
 await assertSucceeds(setDoc(first,{uid:'liked-count-alice-v10088',createdAt:serverTimestamp()}));
 let count=await assertSucceeds(getDocs(liveQuery));
 if(count.size!==1)throw Error('First signed-in device like was not visible to the second device');
 await assertSucceeds(setDoc(doc(author,'moments/post-b'),{uid:'liked-count-bob-v10088',name:'Bob',text:'Post B',createdAt:serverTimestamp()}));
 await assertSucceeds(setDoc(second,{uid:'liked-count-alice-v10088',createdAt:serverTimestamp()}));
 count=await assertSucceeds(getDocs(liveQuery));
 if(count.size!==2)throw Error('Second signed-in device like was not visible to the second device');
 await assertSucceeds(deleteDoc(first));
 count=await assertSucceeds(getDocs(liveQuery));
 if(count.size!==1)throw Error('Unlike did not reduce the cross-device liked count');
 console.log('PASS v10.0.88 Me liked-Moments count reflects cross-device likes and unlikes');
}finally{await env.cleanup();}
