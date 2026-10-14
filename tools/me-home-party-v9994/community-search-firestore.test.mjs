import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {collection,doc,writeBatch,getDocs,query,orderBy,limit,startAfter,Timestamp} from 'firebase/firestore';

const env=await initializeTestEnvironment({projectId:'demo-king-search-paging-9994',firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}});
try {
  await env.clearFirestore();
  await env.withSecurityRulesDisabled(async ctx=>{
    const db=ctx.firestore(),batch=writeBatch(db);
    for(let i=1;i<=105;i++)batch.set(doc(db,'live_rooms',`test-${String(i).padStart(3,'0')}`),{
      ownerUid:'owner',name:`Room ${i}`,closed:i%13===0,
      createdAt:Timestamp.fromMillis(1700000000000+i*1000)});
    await batch.commit();
  });
  const alice=env.authenticatedContext('alice').firestore();
  const roomCollection=collection(alice,'live_rooms');
  const first=await assertSucceeds(getDocs(query(roomCollection,orderBy('createdAt','desc'),limit(80))));
  if(first.size!==80 || first.docs[0].id!=='test-105' || first.docs[79].id!=='test-026')
    throw new Error('Incorrect first page or sort order');
  const second=await assertSucceeds(getDocs(query(roomCollection,orderBy('createdAt','desc'),startAfter(first.docs.at(-1)),limit(80))));
  if(second.size!==25 || second.docs[0].id!=='test-025'||second.docs[24].id!=='test-001')
    throw new Error('Cursor pagination skips or repeats rooms');
  const ids=[...first.docs,...second.docs].map(d=>d.id);
  if(new Set(ids).size!==105)throw new Error('Duplicate Room IDs across pages');
  await assertFails(getDocs(query(collection(env.unauthenticatedContext().firestore(),'live_rooms'),orderBy('createdAt','desc'),limit(80))));
  console.log('PASS v9.9.94 Home room pagination and signed-in privacy');
} finally {await env.cleanup();}
