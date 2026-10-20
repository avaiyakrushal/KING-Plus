import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,serverTimestamp,getDoc} from 'firebase/firestore';
import fs from 'node:fs';
import assert from 'node:assert/strict';
import {createRequire} from 'node:module';
const require=createRequire(new URL('../../functions/package.json',import.meta.url));
const {initializeApp,deleteApp}=require('firebase-admin/app');
const {getFirestore,FieldValue}=require('firebase-admin/firestore');
const {createGiftService}=require('./room-gift-service');
const projectId='demo-king-paid-gifts-10064';
const env=await initializeTestEnvironment({projectId,firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}});
const app=initializeApp({projectId},'gifts10064'),db=getFirestore(app);
const send=createGiftService({db,timestamp:()=>FieldValue.serverTimestamp()});
const root='live_rooms/paid-room';
const request={senderUid:'alice',targetUid:'bob',giftName:'Gold Rose x3',cost:150,requestId:'retry-paid-1',roomId:'paid-room'};
try{
 await env.clearFirestore();
 await db.doc(root).set({ownerUid:'alice',name:'Room',closed:false,locked:false,isPrivate:false,entranceMode:'free'});
 for(const uid of ['alice','bob']){
  await db.doc('public_profiles/'+uid).set({displayName:uid});
  await db.doc(root+'/members/'+uid).set({uid,name:uid});
 }
 await db.doc('wallets/alice').set({coins:500,vipPoints:42});
 await db.doc('wallets/bob').set({coins:7,giftScore:0});
 const results=await Promise.all([send(request),send(request),send(request)]);
 assert.equal(new Set(results.map(r=>r.giftId)).size,1);
 const giftId=results[0].giftId;
 assert.equal((await db.doc('wallets/alice').get()).get('coins'),350);
 assert.equal((await db.doc('wallets/bob').get()).get('giftScore'),150);
 assert.equal((await db.doc('wallets/bob').get()).get('coins'),7);
 assert.equal((await db.collection('wallet_ledger').get()).size,1);
 const A=env.authenticatedContext('alice').firestore(),B=env.authenticatedContext('bob').firestore();
 await assertSucceeds(getDoc(doc(B,root,'events',giftId)));
 const receipt={giftId,senderUid:'alice',recipientUid:'bob',giftName:'Gold Rose',createdAt:serverTimestamp()};
 await assertFails(setDoc(doc(A,root,'gift_receipts',giftId),receipt));
 await assertSucceeds(setDoc(doc(B,root,'gift_receipts',giftId),receipt));
 await assertFails(setDoc(doc(A,root,'events','forged-paid'),{type:'gift',actorUid:'alice',targetUid:'bob',giftValue:150,giftName:'Gold Rose',giftQty:3,createdAt:serverTimestamp()}));
 await assertFails(setDoc(doc(B,'wallets','bob'),{coins:100000}));
 await db.doc(root+'/members/bob').delete();
 await assert.rejects(send({...request,requestId:'recipient-left'}));
 assert.equal((await db.doc('wallets/alice').get()).get('coins'),350);
 console.log('PASS real Firestore: atomic paid gifts, retry, recipient-only playback receipt, forged gift and wallet rejection, leave rejection');
}finally{await env.cleanup();await deleteApp(app);}
