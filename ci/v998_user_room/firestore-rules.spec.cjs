/* Runs ONLY against local Firestore emulator, with synthetic phone A/B auth IDs.
   NEVER connects to production project king-plus-2f365. */
'use strict';
const fs=require('fs');
const {initializeTestEnvironment,assertSucceeds,assertFails}=
    require('@firebase/rules-unit-testing');
const {doc,setDoc,getDoc,updateDoc,deleteDoc,collection,addDoc}=require('firebase/firestore');
const RULES=fs.readFileSync(__dirname+'/firestore.rules','utf8');
async function main(){
  const env=await initializeTestEnvironment({
    projectId:'demo-king-plus-parity-test',
    firestore:{host:'127.0.0.1',port:8089,rules:RULES}
  });
  let tests=0;
  async function allow(desc,p){
    try{await assertSucceeds(p);console.log('PASS allow: '+desc);tests++;}
    catch(e){console.error('FAIL allow: '+desc);throw e;}
  }
  async function deny(desc,p){
    try{await assertFails(p);console.log('PASS deny: '+desc);tests++;}
    catch(e){console.error('FAIL deny: '+desc);throw e;}
  }
  try{
    const a=env.authenticatedContext('phone-a').firestore();
    const b=env.authenticatedContext('phone-b').firestore();
    const c=env.authenticatedContext('outsider-c').firestore();
    const guest=env.unauthenticatedContext().firestore();
    await allow('phone A creates own discoverable profile',
      setDoc(doc(a,'public_profiles','phone-a'),{uid:'phone-a',publicId:'210901',
        searchName:'alice',displayName:'Alice'}));
    await allow('phone B creates own discoverable profile',
      setDoc(doc(b,'public_profiles','phone-b'),{uid:'phone-b',publicId:'210902',
        searchName:'bob',displayName:'Bob'}));
    await allow('phone B finds profile A',
      getDoc(doc(b,'public_profiles','phone-a')));
    await deny('unauthenticated phone cannot read public profiles',
      getDoc(doc(guest,'public_profiles','phone-a')));
    await deny('B cannot overwrite A public ID/profile',
      setDoc(doc(b,'public_profiles','phone-a'),{uid:'phone-a',publicId:'000000'}));
    await allow('A follows B with signed-in identity',
      setDoc(doc(a,'follows','A-to-B'),{followerUid:'phone-a',
        targetUid:'phone-b'}));
    await allow('B follows back A',
      setDoc(doc(b,'follows','B-to-A'),{followerUid:'phone-b',
        targetUid:'phone-a'}));
    await allow('A can read Follow Back state',
      getDoc(doc(a,'follows','B-to-A')));
    await deny('B cannot impersonate A to create a Follow',
      setDoc(doc(b,'follows','impersonation'),{followerUid:'phone-a',
        targetUid:'phone-b'}));
    await allow('A creates live room for both phones',
      setDoc(doc(a,'live_rooms','room-test-001'),{name:'Friends Room',
        ownerUid:'phone-a',ownerName:'Alice',isPrivate:false,
        closed:false,joinCode:'734901'}));
    await allow('B can discover live room',
      getDoc(doc(b,'live_rooms','room-test-001')));
    await allow('B joins the public Party as its own member',
      setDoc(doc(b,'live_rooms','room-test-001','members','phone-b'),
        {uid:'phone-b',name:'Bob'}));
    await allow('A joins own Party',
      setDoc(doc(a,'live_rooms','room-test-001','members','phone-a'),
        {uid:'phone-a',name:'Alice'}));
    await deny('B cannot forge Host member identity',
      setDoc(doc(b,'live_rooms','room-test-001','members','phone-a'),
        {uid:'phone-a',name:'Imposter'}));
    await allow('B can send genuine room dice event',
      addDoc(collection(b,'live_rooms','room-test-001','events'),
        {actorUid:'phone-b',actorName:'Bob',type:'dice_room_997',text:'🎲 4 / 6'}));
    await deny('B cannot forge A sender in room events',
      addDoc(collection(b,'live_rooms','room-test-001','events'),
        {actorUid:'phone-a',actorName:'Alice',type:'dice_room_997',text:'🎲 6 / 6'}));
    await allow('A assigns B authorized moderator role',
      setDoc(doc(a,'live_rooms','room-test-001','roles','phone-b'),
        {uid:'phone-b',role:'cohost'}));
    await deny('B cannot grant self or another user host-like role',
      setDoc(doc(b,'live_rooms','room-test-001','roles','outsider-c'),
        {uid:'outsider-c',role:'cohost'}));
    await allow('A revokes B moderator role',
      deleteDoc(doc(a,'live_rooms','room-test-001','roles','phone-b')));
    await allow('A creates private message thread including B',
      setDoc(doc(a,'direct_threads','thread-a-b'),
        {members:['phone-a','phone-b']}));
    await allow('B receives thread',
      getDoc(doc(b,'direct_threads','thread-a-b')));
    await deny('outsider cannot access A+B private chat',
      getDoc(doc(c,'direct_threads','thread-a-b')));
    await allow('B can send A a private text message',
      addDoc(collection(b,'direct_threads','thread-a-b','messages'),
        {senderUid:'phone-b',recipientUid:'phone-a',text:'Hello friend!'}));
    await deny('outsider cannot send private message to A+B thread',
      addDoc(collection(c,'direct_threads','thread-a-b','messages'),
        {senderUid:'outsider-c',recipientUid:'phone-a',text:'Not a member'}));
    await deny('client cannot mint paid diamond wallet',
      setDoc(doc(a,'wallets','phone-a'),{balance:999999}));
    console.log('ALL_RULES_TESTS_PASSED '+tests+' synthetic cross-account scenarios');
  }finally{
    await env.cleanup();
  }
}
main().catch(e=>{console.error(e&&e.stack||e);process.exitCode=1;});
