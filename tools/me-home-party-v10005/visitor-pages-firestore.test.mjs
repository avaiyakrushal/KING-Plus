import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {collection,doc,writeBatch,query,orderBy,limit,startAfter,getDocs,getCountFromServer} from 'firebase/firestore';
const env=await initializeTestEnvironment({projectId:'demo-king-visitor-pages-10005',firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}});
try{
  await env.clearFirestore();
  await env.withSecurityRulesDisabled(async c=>{
    const b=writeBatch(c.firestore());
    for(let n=0;n<126;n++){
      const uid='guest'+String(n).padStart(3,'0');
      b.set(doc(c.firestore(),'public_profiles','owner','visitors',uid),{uid,name:'Guest '+n,visitedAt:new Date(1760000000000+n*1000)});
    }
    await b.commit();
  });
  const owner=env.authenticatedContext('owner').firestore();
  const records=collection(owner,'public_profiles','owner','visitors');
  const all=[];let cursor=null;const pages=[];
  for(let page=0;page<4;page++){
    let q=query(records,orderBy('visitedAt','desc'),limit(50));
    if(cursor)q=query(q,startAfter(cursor));
    const snap=await assertSucceeds(getDocs(q));
    pages.push(snap.size);
    for(const d of snap.docs)all.push(d.id);
    if(snap.size<50)break;
    cursor=snap.docs[snap.size-1];
  }
  if(pages.join(',')!=='50,50,26')throw Error('Wrong paged sizes: '+pages.join(','));
  if(all.length!==126 || new Set(all).size!==126)throw Error('Missing or duplicate visitor records');
  if(all[0]!=='guest125'||all[49]!=='guest076'||all[50]!=='guest075'||all[125]!=='guest000')throw Error('Wrong visitor chronology');
  const total=await assertSucceeds(getCountFromServer(records));
  if(total.data().count!==126)throw Error('Me visitor total inaccurate');
  const outsider=env.authenticatedContext('outsider').firestore();
  await assertFails(getDocs(query(collection(outsider,'public_profiles','owner','visitors'),orderBy('visitedAt','desc'),limit(50))));
  console.log('PASS v10.0.5: 126 visitors in 50+50+26, no missing/duplicate pages, private count and reads');
}finally{await env.cleanup();}
