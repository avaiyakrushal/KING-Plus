import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,collection,query,where,limit,writeBatch,getDocs,getDoc,getCountFromServer,deleteDoc} from 'firebase/firestore';
const env=await initializeTestEnvironment({projectId:'demo-king-fanclub-9998',firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}});
try{
  await env.clearFirestore();
  const host='host', member='zzlast';
  await env.withSecurityRulesDisabled(async ctx=>{
    const b=writeBatch(ctx.firestore());
    for(let i=1;i<=120;i++){
      const id='f'+String(i).padStart(3,'0')+'__'+host;
      b.set(doc(ctx.firestore(),'follows',id),{followerUid:'f'+String(i).padStart(3,'0'),targetUid:host,followerName:'Fan '+i});
    }
    b.set(doc(ctx.firestore(),'follows',member+'__'+host),{followerUid:member,targetUid:host,followerName:'Final Fan'});
    await b.commit();
  });
  const db=env.authenticatedContext(member).firestore();
  const fans=query(collection(db,'follows'),where('targetUid','==',host));
  const preview=await assertSucceeds(getDocs(query(fans,limit(100))));
  if(preview.size!==100)throw Error('Preview should cap at 100');
  if(preview.docs.some(d=>d.id===member+'__'+host))throw Error('Test member must not appear in preview');
  const membership=await assertSucceeds(getDoc(doc(db,'follows',member+'__'+host)));
  if(!membership.exists()||membership.data()?.followerUid!==member)throw Error('Exact fan membership not found');
  const count=await assertSucceeds(getCountFromServer(fans));
  if(count.data().count!==121)throw Error('Fan count incorrectly capped: '+count.data().count);
  await assertSucceeds(deleteDoc(doc(db,'follows',member+'__'+host)));
  if((await assertSucceeds(getDoc(doc(db,'follows',member+'__'+host)))).exists())throw Error('Fan Club leave failed');
  const other=env.authenticatedContext('outsider').firestore();
  await assertFails(deleteDoc(doc(other,'follows','f001__'+host)));
  console.log('PASS 121 fans: direct Fan Club membership, true count, preview cap, allowed leave, denied foreign deletion');
}finally{await env.cleanup();}
