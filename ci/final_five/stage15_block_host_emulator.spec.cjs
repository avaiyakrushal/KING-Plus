/* Only local emulator. Real KING Firebase never touched. */
'use strict';
const fs=require('fs');
const {initializeTestEnvironment,assertSucceeds,assertFails}=
  require('@firebase/rules-unit-testing');
const {doc,setDoc,getDoc,deleteDoc}=require('firebase/firestore');
async function main(){
 const env=await initializeTestEnvironment({
   projectId:'demo-king-block-test',
   firestore:{host:'127.0.0.1',port:8093,
     rules:fs.readFileSync(__dirname+'/firestore.rules','utf8')}
 });
 let count=0;
 async function allow(t,x){await assertSucceeds(x);count++;console.log('PASS '+t)}
 async function deny(t,x){await assertFails(x);count++;console.log('PASS deny '+t)}
 try{
 const a=env.authenticatedContext('phone-a').firestore();
 const b=env.authenticatedContext('phone-b').firestore();
 const c=env.authenticatedContext('phone-c').firestore();
 const guest=env.unauthenticatedContext().firestore();
 const docA=doc(a,'blocks','phone-a_phone-b');
 const docB=doc(b,'blocks','phone-a_phone-b');
 await allow('A creates own host block',
   setDoc(docA,{ownerUid:'phone-a',targetUid:'phone-b'}));
 await allow('A reads own block',
   getDoc(docA));
 await deny('B cannot read A private block',
   getDoc(docB));
 await deny('B cannot spoof A block',
   setDoc(doc(b,'blocks','phone-b_phone-a'),
     {ownerUid:'phone-a',targetUid:'phone-b'}));
 await deny('A cannot self-block',
   setDoc(doc(a,'blocks','phone-a_phone-a'),
     {ownerUid:'phone-a',targetUid:'phone-a'}));
 await deny('guest cannot block user',
   setDoc(doc(guest,'blocks','guest_phone-b'),
     {ownerUid:'guest',targetUid:'phone-b'}));
 await deny('C cannot remove A block',
   deleteDoc(doc(c,'blocks','phone-a_phone-b')));
 await allow('A can unblock B',
   deleteDoc(docA));
 console.log('ALL_BLOCK_RULES_TESTS_PASSED '+count);
 }finally{await env.cleanup()}
}
main().catch(e=>{console.error(e.stack||e);process.exitCode=1});
