import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,getDoc,updateDoc,deleteDoc,collection,query,where,
  getDocsFromServer,serverTimestamp} from 'firebase/firestore';

const env=await initializeTestEnvironment({projectId:'demo-king-me-live-9983',
 firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}});
const Me=env.authenticatedContext('me-user').firestore();
const Friend=env.authenticatedContext('friend-user').firestore();
const Stranger=env.authenticatedContext('other-user').firestore();
const meWish=db=>doc(db,'king_me_wishes','me-user');
const friendWish=db=>doc(db,'king_me_wishes','friend-user');
const wallet=db=>doc(db,'wallets','me-user');
const record=(db,id)=>doc(db,'wallet_ledger',id);
const good=(uid,wish)=>({uid,wish,updatedAt:serverTimestamp()});
const ledger=(senderUid,targetUid,giftName,cost)=>({
  senderUid,targetUid,giftName,cost,createdAt:serverTimestamp()
});
try{
 await env.clearFirestore();
 // A Me wish is a real server document, visible on friends' profile phones.
 await assertSucceeds(setDoc(meWish(Me),good('me-user','Royal Castle')));
 const seen=await assertSucceeds(getDoc(meWish(Friend)));
 if(!seen.exists()||seen.data().wish!=='Royal Castle')
   throw Error('Other phone cannot see current server-backed wish');
 await assertFails(setDoc(meWish(Stranger),good('other-user','Diamond Rain')));
 await assertFails(setDoc(meWish(Me),good('other-user','Diamond Rain')));
 await assertFails(setDoc(meWish(Me),good('me-user','999999 Free Diamonds')));
 await assertSucceeds(updateDoc(meWish(Me),{wish:'King Dragon',updatedAt:serverTimestamp()}));
 const updated=await assertSucceeds(getDoc(meWish(Friend)));
 if(updated.data().wish!=='King Dragon')throw Error('Cross-phone Wish update not synced');
 await assertSucceeds(setDoc(friendWish(Friend),good('friend-user','Galaxy Ship')));
 await assertFails(deleteDoc(friendWish(Me)));
 // Trusted backend seeds existing ledger receipts; signed-in users can ONLY
 // see receipts where they are sender or recipient.
 await env.withSecurityRulesDisabled(async ctx=>{
    const root=ctx.firestore();
    await setDoc(record(root,'gift-to-me'),ledger('friend-user','me-user','Rose',0));
    await setDoc(record(root,'gift-sent-me'),ledger('me-user','friend-user','Heart',0));
    await setDoc(record(root,'other-people'),ledger('friend-user','other-user','Crown',100));
 });
 const received=await assertSucceeds(getDocsFromServer(
    query(collection(Me,'wallet_ledger'),where('targetUid','==','me-user'))));
 if(received.size!==1||received.docs[0].id!=='gift-to-me')
    throw Error('Me Wall must contain only own received gifts');
 const sent=await assertSucceeds(getDocsFromServer(
    query(collection(Me,'wallet_ledger'),where('senderUid','==','me-user'))));
 if(sent.size!==1||sent.docs[0].id!=='gift-sent-me')
    throw Error('My gift history must contain only own sent gifts');
 await assertFails(getDoc(record(Me,'other-people')));
 await assertFails(getDoc(record(Stranger,'gift-to-me')));
 await assertFails(setDoc(wallet(Me),{coins:999999}));
 await assertFails(setDoc(record(Me,'fake-credit'),ledger('friend-user','me-user','100000 Diamonds',100000)));
 console.log('PASS Me: verified gift Wall/History isolated by signed-in UID; wishes sync across 2 real Firebase clients; forged currency and other users blocked');
}finally{await env.cleanup();}
