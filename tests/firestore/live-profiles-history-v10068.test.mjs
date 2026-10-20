import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,updateDoc,getDocs,writeBatch,collection,query,orderBy,limit,startAfter,onSnapshot,Timestamp} from 'firebase/firestore';
import fs from 'node:fs';
import assert from 'node:assert/strict';
const env=await initializeTestEnvironment({projectId:'demo-king-history-10068',firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}});
const A=env.authenticatedContext('alice').firestore(),B=env.authenticatedContext('bob').firestore(),C=env.authenticatedContext('charlie').firestore();
try{
 await env.clearFirestore();
 await env.withSecurityRulesDisabled(async c=>{
  const db=c.firestore();const batch=writeBatch(db);
  batch.set(doc(db,'public_profiles','alice'),{uid:'alice',displayName:'Alice',vipLevel:3,photoUrl:'https://example.com/old.jpg'});
  batch.set(doc(db,'direct_threads','alice_bob'),{members:['alice','bob']});
  for(let i=0;i<265;i++)batch.set(doc(db,'direct_threads','alice_bob','messages','m'+String(i).padStart(4,'0')),
   {senderUid:i%2?'alice':'bob',recipientUid:i%2?'bob':'alice',text:i<10?'Rose':'Message '+i,type:'text',freeGift:i<10,
    createdAt:Timestamp.fromMillis(1700000000000+Math.floor(i/3)*1000)});
  await batch.commit();
 });
 const history=collection(A,'direct_threads','alice_bob','messages');let cursor=null;const ids=new Set();let pages=0,gifts=0,first;
 while(true){
  const filters=[orderBy('createdAt','desc'),limit(100)];if(cursor)filters.push(startAfter(cursor));
  const page=await assertSucceeds(getDocs(query(history,...filters)));pages++;
  if(!first)first=page;
  for(const message of page.docs){assert(!ids.has(message.id),'duplicate across tied-timestamp cursors');ids.add(message.id);if(message.data().freeGift)gifts++;}
  if(page.size<100)break;cursor=page.docs.at(-1);
 }
 assert.equal(ids.size,265);assert.equal(pages,3);assert.equal(gifts,10);
 assert(!first.docs.some(d=>d.data().freeGift),'old gifts must be reachable beyond latest 100');
 const second=await getDocs(query(history,orderBy('createdAt','desc'),startAfter(first.docs.at(-1)),limit(100)));
 const secondAgain=await getDocs(query(history,orderBy('createdAt','desc'),startAfter(first.docs.at(-1)),limit(100)));
 assert.deepEqual(second.docs.map(d=>d.id),secondAgain.docs.map(d=>d.id),'Newer must restore the same cursor page');
 await assertFails(getDocs(query(collection(C,'direct_threads','alice_bob','messages'),orderBy('createdAt','desc'),limit(100))));
 await assertFails(updateDoc(doc(A,'direct_threads','alice_bob','messages','m0000'),{text:'edited'}));
 let observed;
 const changed=new Promise((resolve,reject)=>{
  let off;const timer=setTimeout(()=>{off?.();reject(Error('Live profile timed out'));},10000);
  off=onSnapshot(doc(B,'public_profiles','alice'),p=>{
   if(p.data()?.displayName==='Alice edited'&&p.data()?.photoUrl==='https://example.com/new.jpg'){
    observed=p.data();clearTimeout(timer);off();resolve();
   }
  },reject);
 });
 await assertSucceeds(updateDoc(doc(A,'public_profiles','alice'),{displayName:'Alice edited',photoUrl:'https://example.com/new.jpg'}));await changed;
 assert.equal(observed.vipLevel,3,'ordinary live identity edit must preserve VIP');
 console.log('PASS 265-message cursor history with tied timestamps, older gifts, repeatable newer page, outsider denial, immutable messages and live name/photo across accounts');
}finally{await env.cleanup();}
