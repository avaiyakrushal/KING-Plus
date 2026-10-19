import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,getDoc,updateDoc,deleteDoc,collection,query,where,
  getDocsFromServer,serverTimestamp} from 'firebase/firestore';

const env=await initializeTestEnvironment({projectId:'demo-king-me-privacy-9984',
 firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}});
const Alice=env.authenticatedContext('alice123').firestore(),
 AliceSecondPhone=env.authenticatedContext('alice123').firestore(),
 Bob=env.authenticatedContext('bob123').firestore(),
 Eve=env.authenticatedContext('eve123').firestore();
const pref=(db,uid)=>doc(db,'king_me_preferences',uid);
const block=(db,owner,target)=>doc(db,'blocks',owner+'_'+target);
const thread=db=>doc(db,'direct_threads','alice_bob_9984');
const msg=(db,name)=>doc(db,'direct_threads','alice_bob_9984','messages',name);
const makePrefs=uid=>({
 uid,findMe:true,showOnlineStatus:true,allowRoomInvites:true,allowPrivateMessages:true,
 showEntranceEffects:true,notifyMessages:true,notifySocial:true,notifyRoomInvites:true,
 notifyGifts:true,notifyGames:true,updatedAt:serverTimestamp()
});
const message=(senderUid,recipientUid)=>({
 senderUid,recipientUid,text:'Hello real KING friend',createdAt:serverTimestamp()
});
try{
 await env.clearFirestore();
 await assertSucceeds(setDoc(pref(Alice,'alice123'),makePrefs('alice123')));
 // Cross-phone persisted settings, not SharedPreferences fake local toggle.
 await assertSucceeds(updateDoc(pref(Alice,'alice123'),{
    notifyGifts:false,notifyGames:false,updatedAt:serverTimestamp()}));
 const synced=await assertSucceeds(getDoc(pref(AliceSecondPhone,'alice123')));
 if(synced.data().notifyGifts!==false||synced.data().notifyGames!==false)
    throw Error('Different phone with same signed UID lost server preferences');
 await assertFails(getDoc(pref(Bob,'alice123')));
 await assertFails(setDoc(pref(Bob,'alice123'),makePrefs('bob123')));
 await assertFails(setDoc(pref(Alice,'alice123'),{
    ...makePrefs('alice123'),admin:true}));
 await assertFails(updateDoc(pref(Alice,'alice123'),{
    coins:9000000,updatedAt:serverTimestamp()}));
 await assertFails(deleteDoc(pref(Alice,'alice123')));
 await assertSucceeds(setDoc(thread(Alice),{
    members:['alice123','bob123'],updatedAt:serverTimestamp()}));
 // User opts out of direct messages -> server rejects attempts from Bob.
 await assertSucceeds(updateDoc(pref(Alice,'alice123'),{
    allowPrivateMessages:false,updatedAt:serverTimestamp()}));
 await assertFails(setDoc(msg(Bob,'blocked-by-setting'),message('bob123','alice123')));
 await assertSucceeds(updateDoc(pref(Alice,'alice123'),{
    allowPrivateMessages:true,updatedAt:serverTimestamp()}));
 await assertSucceeds(setDoc(msg(Bob,'allowed-before-block'),message('bob123','alice123')));
 // Real Firestore Block List, only owner can see/unblock. Deny both directions.
 await assertSucceeds(setDoc(block(Alice,'alice123','bob123'),{
    ownerUid:'alice123',targetUid:'bob123',createdAt:serverTimestamp()}));
 const shown=await assertSucceeds(getDocsFromServer(
   query(collection(Alice,'blocks'),where('ownerUid','==','alice123'))));
 if(shown.size!==1||shown.docs[0].data().targetUid!=='bob123')
    throw Error('Real Me Blocked list not returned by owner query');
 await assertFails(getDoc(block(Bob,'alice123','bob123')));
 await assertFails(setDoc(block(Bob,'bob123','alice123'),{
   ownerUid:'alice123',targetUid:'bob123',createdAt:serverTimestamp()}));
 await assertFails(setDoc(block(Alice,'alice123','eve123'),{
   ownerUid:'alice123',targetUid:'bob123',createdAt:serverTimestamp()}));
 await assertFails(setDoc(msg(Bob,'bob-to-blocker'),message('bob123','alice123')));
 await assertFails(setDoc(msg(Alice,'blocker-to-bob'),message('alice123','bob123')));
 await assertFails(deleteDoc(block(Bob,'alice123','bob123')));
 await assertSucceeds(deleteDoc(block(Alice,'alice123','bob123')));
 await assertSucceeds(setDoc(msg(Alice,'allowed-after-unblock'),message('alice123','bob123')));
 await assertSucceeds(setDoc(block(Bob,'bob123','alice123'),{
    ownerUid:'bob123',targetUid:'alice123',createdAt:serverTimestamp()}));
 await assertFails(setDoc(msg(Alice,'alice-to-bob-who-blocked'),message('alice123','bob123')));
 await assertFails(setDoc(doc(Alice,'wallets','alice123'),{diamonds:999999}));
 await assertFails(setDoc(doc(Alice,'wallet_ledger','fake-credit'),{diamonds:999999}));
 console.log('PASS Me cloud toggles sync on second device; no reads/edits by outsider; owner-only Block/Unblock; both directions DM rejected; opt-out DM rejected; allowed after unblock; Wallet remains locked');
}finally{await env.cleanup();}
