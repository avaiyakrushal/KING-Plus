import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,getDoc,runTransaction,onSnapshot} from 'firebase/firestore';
const env=await initializeTestEnvironment({projectId:'demo-king-saved-remove-9992',firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}});
const a=env.authenticatedContext('alice').firestore(),a2=env.authenticatedContext('alice').firestore(),b=env.authenticatedContext('bob').firestore(),guest=env.unauthenticatedContext().firestore();
const field='savedPartyRooms9990', ref=db=>doc(db,'users','alice');
try{
 await env.clearFirestore();
 await assertSucceeds(setDoc(ref(a),{displayName:'Alice',familyCode:'family', [field]:{
   first:{name:'First',favorite:true,visited:100},
   second:{name:'Second',favorite:false,visited:200}
 }}));
 const second=await assertSucceeds(getDoc(ref(a2)));
 if(!second.data()?.[field]?.first?.favorite)throw Error('Second phone missing favorites');
 const removed=new Promise((resolve,reject)=>{
   let off;let completed=false;
   let timer=setTimeout(()=>{off?.();reject(Error('Remote delete listener timeout'));},12000);
   off=onSnapshot(ref(a2),snapshot=>{
     if(completed)return;
     if(snapshot.data()?.[field]?.first===undefined&&snapshot.data()?.[field]?.second){
       completed=true;clearTimeout(timer);off?.();resolve();
     }
   },reject);
 });
 await assertSucceeds(runTransaction(a,async tx=>{
    let s=await tx.get(ref(a));let next={...s.data()[field]};delete next.first;
    tx.set(ref(a),{[field]:next},{mergeFields:[field]});
 }));
 await removed;
 const updated=(await assertSucceeds(getDoc(ref(a2)))).data();
 if(updated.displayName!=='Alice'||updated.familyCode!=='family'||!updated[field].second||updated[field].first)
    throw Error('Saved room removal overwrote other profile/rooms');
 await assertFails(getDoc(ref(b)));await assertFails(getDoc(ref(guest)));
 await assertFails(setDoc(ref(b),{[field]:{}},{mergeFields:[field]}));
 console.log('PASS two-phone room removal sync, preservation and cross-user privacy');
}finally{await env.cleanup();}
