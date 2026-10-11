import fs from 'node:fs';
import assert from 'node:assert/strict';
import {initializeTestEnvironment,assertSucceeds} from '@firebase/rules-unit-testing';
import {doc,setDoc,getDoc,runTransaction,collection,query,where,limit,getDocs} from 'firebase/firestore';

const env=await initializeTestEnvironment({projectId:'demo-kingplus-exit-9919',
  firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}});
const a=env.authenticatedContext('alice').firestore(), b=env.authenticatedContext('bob').firestore();
const room=db=>doc(db,'live_rooms','exit-test');
const mem=(db,uid)=>doc(db,'live_rooms','exit-test','members',uid);
const seat=(db,id)=>doc(db,'live_rooms','exit-test','seats',String(id));
const same=(joined,session,device)=>{
  if(!joined)return false;
  const data=joined.data();
  return data?.sessionId ? data.sessionId===session : data?.deviceId===device;
};
async function leave(db,uid,session,device){
  const q=await getDocs(query(collection(db,'live_rooms','exit-test','seats'),where('uid','==',uid),limit(16)));
  const refs=q.docs.map(d=>d.ref);
  return runTransaction(db,async tx=>{
    const joined=await tx.get(mem(db,uid));
    const held=[];for(const ref of refs)held.push(await tx.get(ref));
    if(!joined.exists()||joined.data().uid!==uid||!same(joined,session,device))return 0;
    let deleted=0;for(let i=0;i<held.length;i++){
      if(held[i].exists()&&held[i].data().uid===uid){tx.delete(refs[i]);deleted++;}
    }
    tx.delete(mem(db,uid));return deleted;
  });
}
try {
  await env.clearFirestore();
  await assertSucceeds(setDoc(room(a),{ownerUid:'alice',name:'KING Live Room',isPrivate:false,closed:false}));
  await assertSucceeds(setDoc(mem(a,'alice'),{uid:'alice',sessionId:'session-a',deviceId:'device-a',name:'Alice'}));
  await assertSucceeds(setDoc(mem(b,'bob'),{uid:'bob',sessionId:'session-b',deviceId:'device-b',name:'Bob'}));
  await assertSucceeds(setDoc(seat(a,1),{uid:'alice',name:'Alice',micOn:false}));
  await assertSucceeds(setDoc(seat(b,2),{uid:'bob',name:'Bob',micOn:false}));
  assert.equal(await assertSucceeds(leave(a,'alice','session-a','device-a')),1);
  assert.equal((await assertSucceeds(getDoc(seat(a,1)))).exists(),false);
  assert.equal((await assertSucceeds(getDoc(mem(a,'alice')))).exists(),false);
  assert.equal((await assertSucceeds(getDoc(seat(b,2)))).data().uid,'bob');
  console.log('PASS live Room exit releases own seat and membership, not other user');

  await assertSucceeds(setDoc(mem(a,'alice'),{uid:'alice',sessionId:'session-new',deviceId:'device-new',name:'Alice'}));
  await assertSucceeds(setDoc(seat(a,3),{uid:'alice',name:'Alice',micOn:false}));
  assert.equal(await assertSucceeds(leave(a,'alice','session-a','device-a')),0);
  assert.equal((await assertSucceeds(getDoc(seat(a,3)))).data().uid,'alice');
  assert.equal((await assertSucceeds(getDoc(mem(a,'alice')))).data().sessionId,'session-new');
  console.log('PASS stale older-phone exit cannot clear newer same-UID session');

  assert.equal(await assertSucceeds(leave(a,'alice','session-new','device-new')),1);
  assert.equal((await assertSucceeds(getDoc(seat(a,3)))).exists(),false);
  console.log('PASS current second session safely releases its seat');
} finally {await env.cleanup();}
