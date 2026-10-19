import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,getDoc,getDocs,writeBatch,collection,query,where,limit,startAfter,onSnapshot} from 'firebase/firestore';
import fs from 'node:fs';
import assert from 'node:assert/strict';
const env=await initializeTestEnvironment({projectId:'demo-king-pages-10067',firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}});
const A=env.authenticatedContext('alice').firestore(),B=env.authenticatedContext('bob').firestore();
try{
 await env.clearFirestore();
 await env.withSecurityRulesDisabled(async context=>{
  const db=context.firestore();
  for(let offset=0;offset<650;offset+=400){
   const batch=writeBatch(db);
   for(let i=offset;i<Math.min(650,offset+400);i++)batch.set(doc(db,'live_rooms','r'+String(i).padStart(4,'0')),
    {ownerUid:'alice',ownerName:'Alice',name:'Room '+i,category:i<350?'Game':i%2?'Music':'KTV',closed:i%13===0});
   await batch.commit();
  }
  await setDoc(doc(db,'public_profiles','alice'),{uid:'alice',displayName:'Alice',searchName:'alice',bio:'Existing bio',vipLevel:3});
  await setDoc(doc(db,'users','alice'),{uid:'alice'});
  await setDoc(doc(db,'wallets','alice'),{coins:73,giftScore:91,rechargeDiamondsTotal:0});
 });
 // A category must find rooms beyond the first 300 records without loading them all first.
 let cursor=null, pages=0;const seen=new Set();
 do{
  const filters=[where('category','in',['Music','music','Sing','sing','KTV','ktv','Radio','radio']),limit(50)];
  if(cursor)filters.push(startAfter(cursor));
  const snapshot=await assertSucceeds(getDocs(query(collection(A,'live_rooms'),...filters)));
  for(const room of snapshot.docs){assert(!seen.has(room.id));seen.add(room.id);assert(['Music','KTV'].includes(room.data().category));}
  cursor=snapshot.docs.at(-1);pages++;
  if(snapshot.size<50)break;
 }while(pages<20);
 assert.equal(seen.size,300);assert(seen.has('r0649'));assert(pages>1);
 const first=await getDocs(query(collection(A,'live_rooms'),where('closed','==',false),limit(50)));
 const second=await getDocs(query(collection(A,'live_rooms'),where('closed','==',false),startAfter(first.docs.at(-1)),limit(50)));
 assert.equal(first.size,50);assert.equal(second.size,50);
 assert(!second.docs.some(d=>first.docs.some(a=>a.id===d.id)));
 const wallet=(await assertSucceeds(getDoc(doc(A,'wallets','alice')))).data();
 assert.equal(wallet.coins,73);assert.equal(wallet.giftScore,91);
 await assertFails(setDoc(doc(A,'wallets','alice'),{coins:91,giftScore:73}));
 const livePhoto=new Promise((resolve,reject)=>{
  const timer=setTimeout(()=>{off();reject(Error('Photo update did not reach another account'));},10000);
  const off=onSnapshot(doc(B,'public_profiles','alice'),d=>{if(d.data()?.photoUrl==='https://example.com/avatar.jpg'){clearTimeout(timer);off();resolve();}},reject);
 });
 const photo=writeBatch(A);photo.update(doc(A,'public_profiles','alice'),{photoUrl:'https://example.com/avatar.jpg'});
 photo.update(doc(A,'users','alice'),{photoUrl:'https://example.com/avatar.jpg'});await assertSucceeds(photo.commit());await livePhoto;
 assert.equal((await getDoc(doc(A,'public_profiles','alice'))).data().bio,'Existing bio');
 // A denied public identity edit must not partially update the private photo record.
 const denied=writeBatch(A);denied.update(doc(A,'public_profiles','alice'),{vipLevel:99});
 denied.update(doc(A,'users','alice'),{photoUrl:'https://example.com/forged.jpg'});
 await assertFails(denied.commit());
 assert.equal((await getDoc(doc(A,'users','alice'))).data().photoUrl,'https://example.com/avatar.jpg');
 await assertFails(setDoc(doc(A,'public_profiles','alice'),{uid:'alice',vipLevel:0}));
 assert.equal((await getDoc(doc(A,'public_profiles','alice'))).data().vipLevel,3);
 console.log('PASS category pagination past the old 300-room cutoff, nonoverlapping pages, distinct wallet fields, cross-account live photo and atomic profile-write rejection');
}finally{await env.cleanup();}
