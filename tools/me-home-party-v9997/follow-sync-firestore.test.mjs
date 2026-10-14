import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,collection,getDoc,setDoc,getDocs,query,where,runTransaction,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({projectId:'demo-king-follow-9997',firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}});
try{
  await env.clearFirestore();
  const a1=env.authenticatedContext('alice').firestore();
  const a2=env.authenticatedContext('alice').firestore();
  const bob=env.authenticatedContext('bob').firestore();
  const guest=env.unauthenticatedContext().firestore();
  const id='alice__bob';
  const toggle=(db)=>runTransaction(db,async tx=>{
    const ref=doc(db,'follows',id), current=await tx.get(ref);
    if(current.exists()){tx.delete(ref);return false;}
    tx.set(ref,{followerUid:'alice',targetUid:'bob',followerName:'Alice',createdAt:serverTimestamp()});return true;
  });
  if(await assertSucceeds(toggle(a1))!==true)throw Error('Follow did not commit');
  if(!(await assertSucceeds(getDoc(doc(a2,'follows',id)))).exists())throw Error('Second phone failed to see Follow');
  if((await assertSucceeds(getDocs(query(collection(bob,'follows'),where('targetUid','==','bob'))))).size!==1)throw Error('Me Followers count not synced');
  if((await assertSucceeds(getDocs(query(collection(a2,'follows'),where('followerUid','==','alice'))))).size!==1)throw Error('Me Following count not synced');
  if(await assertSucceeds(toggle(a2))!==false)throw Error('Unfollow did not commit');
  if((await assertSucceeds(getDoc(doc(a1,'follows',id)))).exists())throw Error('Unfollow did not sync back to first phone');
  await assertFails(setDoc(doc(bob,'follows',id),{followerUid:'alice',targetUid:'bob'}));
  await assertFails(setDoc(doc(a1,'follows','alice__alice'),{followerUid:'alice',targetUid:'alice'}));
  await assertFails(getDoc(doc(guest,'follows',id)));
  await assertSucceeds(toggle(a1));
  await assertFails(setDoc(doc(bob,'follows','alice__charlie'),{followerUid:'alice',targetUid:'charlie'}));
  console.log('PASS cross-device Follow/Unfollow + Me Followers and Following + no forged writes or guest reads');
}finally{await env.cleanup();}
