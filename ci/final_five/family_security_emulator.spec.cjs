/* Synthetic family-owner and ordinary-member Firestore security regression tests.
   Local Firebase Emulator only. No production project, credentials or recharge. */
'use strict';
const fs=require('fs');
const {initializeTestEnvironment,assertSucceeds,assertFails}=require('@firebase/rules-unit-testing');
const {doc,getDoc,setDoc,updateDoc}=require('firebase/firestore');
async function main(){
 const env=await initializeTestEnvironment({
  projectId:'demo-king-family-security-2026',
  firestore:{host:'127.0.0.1',port:8092,
    rules:fs.readFileSync(__dirname+'/firestore.rules','utf8')}
 });
 let total=0;
 async function pass(name,p){await assertSucceeds(p);total++;console.log('PASS allow '+name)}
 async function deny(name,p){await assertFails(p);total++;console.log('PASS deny '+name)}
 try{
  const owner=env.authenticatedContext('phone-a').firestore();
  const member=env.authenticatedContext('phone-b').firestore();
  const stranger=env.authenticatedContext('phone-c').firestore();
  const guest=env.unauthenticatedContext().firestore();
  await pass('A creates original Family',
   setDoc(doc(owner,'families','ABC123'),
     {code:'ABC123',ownerUid:'phone-a',name:'Our KING Family'}));
  await pass('A self-registers owner role',
   setDoc(doc(owner,'families','ABC123','members','phone-a'),
     {uid:'phone-a',role:'owner',name:'Alice'}));
  await pass('B joins Family as member',
   setDoc(doc(member,'families','ABC123','members','phone-b'),
     {uid:'phone-b',role:'member',name:'Bob'}));
  await pass('B reads Family member details',
   getDoc(doc(member,'families','ABC123','members','phone-a')));
  await pass('B updates own display name without role change',
   updateDoc(doc(member,'families','ABC123','members','phone-b'),
     {name:'Bob updated'}));
  await deny('B cannot promote self to owner',
   updateDoc(doc(member,'families','ABC123','members','phone-b'),
     {role:'owner'}));
  await deny('B cannot promote self to admin or cohost',
   updateDoc(doc(member,'families','ABC123','members','phone-b'),
     {role:'cohost'}));
  await deny('B cannot change owner member document',
   updateDoc(doc(member,'families','ABC123','members','phone-a'),
     {role:'member'}));
  await deny('B cannot create an owner role for phone C',
   setDoc(doc(member,'families','ABC123','members','phone-c'),
     {uid:'phone-c',role:'owner',name:'Forgery'}));
  await deny('unauthenticated user cannot read Family',
   getDoc(doc(guest,'families','ABC123')));
  await pass('A as real Family owner may moderate member role',
   updateDoc(doc(owner,'families','ABC123','members','phone-b'),
     {role:'moderator'}));
  await deny('B still cannot change own moderator role to owner',
   updateDoc(doc(member,'families','ABC123','members','phone-b'),
     {role:'owner'}));
  console.log('ALL_FAMILY_RULES_TESTS_PASSED '+total);
 }finally{await env.cleanup()}
}
main().catch(e=>{console.error(e.stack||e);process.exitCode=1});
