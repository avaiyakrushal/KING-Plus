import {readFileSync} from 'node:fs';
import {initializeTestEnvironment, assertSucceeds, assertFails} from '@firebase/rules-unit-testing';
import {doc, collection, query, where, getDocs, getDoc, setDoc, serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({projectId:'demo-king-moments-10048',firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}});
try {
  await env.clearFirestore();
  const alice=env.authenticatedContext('alice').firestore();
  const aliceSecondPhone=env.authenticatedContext('alice').firestore();
  const bob=env.authenticatedContext('bob').firestore();
  const id='moment_aabbcc112233';
  const ref=doc(alice,'moments',id);
  const payload=()=>({uid:'alice',name:'Alice',text:'Verified moment',topic:'General',createdAt:serverTimestamp()});
  await assertSucceeds(setDoc(ref,payload()));
  // Simulate retry after the server accepted the first write but the client missed the ACK.
  await assertSucceeds(setDoc(doc(aliceSecondPhone,'moments',id),payload()));
  const existing=await assertSucceeds(getDoc(ref));
  if(!existing.exists()||existing.get('uid')!=='alice'||existing.get('text')!=='Verified moment') throw Error('Moment not verified');
  const matching=await assertSucceeds(getDocs(query(collection(alice,'moments'),where('uid','==','alice'))));
  if(matching.size!==1)throw Error('Retried moment created duplicate documents');
  await assertFails(setDoc(doc(bob,'moments',id),{uid:'bob',name:'Bob',text:'Hijack',topic:'General',createdAt:serverTimestamp()}));
  const again=await assertSucceeds(getDoc(doc(aliceSecondPhone,'moments',id)));
  if(again.get('uid')!=='alice')throw Error('Cross-user overwrote moment owner');
  console.log('PASS duplicate-safe Moment retry, cross-device verification and owner rules');
}finally{await env.cleanup();}
