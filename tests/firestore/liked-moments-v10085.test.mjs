import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {collection,collectionGroup,doc,getDocs,query,serverTimestamp,setDoc,where} from 'firebase/firestore';

const env=await initializeTestEnvironment({
  projectId:'demo-king-liked-moments-v10085',
  firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}
});
try{
  await env.clearFirestore();
  const alice=env.authenticatedContext('alice').firestore();
  const bob=env.authenticatedContext('bob').firestore();
  await env.withSecurityRulesDisabled(async context=>{
    const admin=context.firestore();
    await setDoc(doc(admin,'moments','post-a'),{uid:'bob',name:'Bob',text:'Live moment',createdAt:serverTimestamp()});
    await setDoc(doc(admin,'moments','post-b'),{uid:'alice',name:'Alice',text:'Own moment',createdAt:serverTimestamp()});
  });
  await assertSucceeds(setDoc(doc(alice,'moments','post-a','likes','alice'),{uid:'alice',createdAt:serverTimestamp()}));
  await assertSucceeds(setDoc(doc(alice,'moments','post-b','likes','alice'),{uid:'alice',createdAt:serverTimestamp()}));
  await assertFails(setDoc(doc(bob,'moments','post-a','likes','alice'),{uid:'alice',createdAt:serverTimestamp()}));
  const own=await assertSucceeds(getDocs(query(collectionGroup(alice,'likes'),where('uid','==','alice'))));
  if(own.size!==2)throw Error(`Expected Alice's two likes, found ${own.size}`);
  const sourcePaths=new Set(own.docs.map(d=>d.ref.parent.parent.path));
  if(!sourcePaths.has('moments/post-a')||!sourcePaths.has('moments/post-b'))throw Error('Liked records did not point to their source Moments');
  const none=await assertSucceeds(getDocs(query(collectionGroup(bob,'likes'),where('uid','==','bob'))));
  if(!none.empty)throw Error('A different account query returned Alice likes');
  console.log('PASS account-filtered collectionGroup likes resolve to their live source Moments; users cannot write another account like');
}finally{await env.cleanup();}
