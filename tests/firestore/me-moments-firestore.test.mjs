import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,getDoc,deleteDoc,updateDoc,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({projectId:'demo-king-me-moments-9991',firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}});
const alice=env.authenticatedContext('alice').firestore(), bob=env.authenticatedContext('bob').firestore(), guest=env.unauthenticatedContext().firestore();
try{
  await env.clearFirestore();
  const id='test-main-me-composer-9991';
  const path=db=>doc(db,'moments',id);
  const image='https://firebasestorage.googleapis.com/v0/b/example.appspot.com/o/chat_media%2Falice%2Fmoments%2Fpicture.jpg?alt=media';
  await assertSucceeds(setDoc(path(alice),{uid:'alice',name:'Alice',text:'My shared Me Moment',topic:'General',createdAt:serverTimestamp(),photoUrl:image}));
  const seen=await assertSucceeds(getDoc(path(bob)));
  if(seen.data()?.photoUrl!==image||seen.data()?.text!=='My shared Me Moment')throw Error('Shared Me photo/text absent on second account');
  await assertFails(setDoc(path(bob),{uid:'bob',text:'forged'}));
  await assertFails(updateDoc(path(bob),{text:'Edited by stranger'}));
  await assertFails(deleteDoc(path(bob)));
  await assertFails(getDoc(path(guest)));
  await assertSucceeds(updateDoc(path(alice),{text:'Owner-edited Moment'}));
  await assertSucceeds(deleteDoc(path(alice)));
  const photoOnly=doc(alice,'moments','photo-only-9991');
  await assertSucceeds(setDoc(photoOnly,{uid:'alice',name:'Alice',text:'📷 Photo',topic:'General',createdAt:serverTimestamp(),photoUrl:image}));
  await assertSucceeds(deleteDoc(photoOnly));
  console.log('PASS v9.9.91 shared Me/photo moments: cross-account view, no forged writes, owner delete and photo-only');
}finally{await env.cleanup();}
