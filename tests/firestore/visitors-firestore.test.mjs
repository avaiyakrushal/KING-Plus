import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {collection,doc,writeBatch,query,orderBy,limit,getDocs,getCountFromServer} from 'firebase/firestore';

const env=await initializeTestEnvironment({projectId:'demo-king-visitors-10003',firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}});
try {
    await env.clearFirestore();
    await env.withSecurityRulesDisabled(async context=>{
        const batch=writeBatch(context.firestore());
        for(let n=0;n<125;n++){
            const uid='guest'+String(n).padStart(3,'0');
            batch.set(doc(context.firestore(),'public_profiles','owner','visitors',uid),{
                uid,name:'Guest '+n,visitedAt:new Date(1750000000000+n*1000)
            });
        }
        await batch.commit();
    });
    const owner=env.authenticatedContext('owner').firestore();
    const visitors=collection(owner,'public_profiles','owner','visitors');
    const recent=await assertSucceeds(getDocs(query(visitors,orderBy('visitedAt','desc'),limit(50))));
    if(recent.size!==50) throw new Error('Expected 50 most recent visitors');
    if(recent.docs[0].id!=='guest124'||recent.docs[49].id!=='guest075')
        throw new Error('Visitor order is not based on visitedAt');
    const total=await assertSucceeds(getCountFromServer(visitors));
    if(total.data().count!==125)throw Error('Me Page visitors count is capped: '+total.data().count);
    const other=env.authenticatedContext('unrelated').firestore();
    await assertFails(getDocs(query(collection(other,'public_profiles','owner','visitors'),limit(1))));
    console.log('PASS 125 visitor records: accurate count, newest 50 by visitedAt, private to profile owner');
} finally {await env.cleanup();}
