import fs from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,writeBatch,getDoc,getDocsFromServer,collection,
        query,orderBy,limit,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({
 projectId:'demo-king-gift-rejoin-9968',
 firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}
});
const H=env.authenticatedContext('host').firestore(),
      B=env.authenticatedContext('bob').firestore(),
      E=env.authenticatedContext('eve').firestore();
const base='live_rooms/gift-rejoin-9968';
const room=db=>doc(db,base),member=(db,id)=>doc(db,base,'members',id);
const event=(db,id)=>doc(db,base,'events',id);
const chat=(db,id)=>doc(db,base,'messages',id);
const ack=(db,id)=>doc(db,base,'gift_receipts',id);
const gift=(id)=>({
 giftId:id,actorUid:'host',actorName:'Host',type:'gift',
 text:'Host sent FREE Rose to bob',freeGift:true,giftName:'Rose',giftIcon:'🌹',
 giftValue:0,giftUnitCost:0,giftQty:1,targetUid:'bob',targetName:'Bob',
 createdAt:serverTimestamp()
});
const message=(id)=>({
 giftId:id,senderUid:'host',senderName:'Host',senderLevel:1,senderVip:0,
 type:'gift',text:'Host sent FREE Rose to Bob',giftName:'Rose',giftIcon:'🌹',
 giftValue:0,giftQty:1,targetUid:'bob',targetName:'Bob',createdAt:serverTimestamp()
});
const receipt=id=>({giftId:id,senderUid:'host',recipientUid:'bob',
 giftName:'Rose',createdAt:serverTimestamp()});
try{
 await env.clearFirestore();
 await assertSucceeds(setDoc(room(H),{ownerUid:'host',name:'Real Gift Rejoin',
  isPrivate:false,hasPassword:false,entranceMode:'free',closed:false}));
 for(const [db,uid] of [[H,'host'],[B,'bob'],[E,'eve']])
  await assertSucceeds(setDoc(member(db,uid),{uid,name:uid}));
 // First gift truly played, so Bob confirms once.
 for(const id of ['gift-delivered','gift-while-offline']){
  const batch=writeBatch(H);
  batch.set(event(H,id),gift(id));batch.set(chat(H,id),message(id));
  await assertSucceeds(batch.commit());
 }
 await assertSucceeds(setDoc(ack(B,'gift-delivered'),receipt('gift-delivered')));
 await assertFails(getDoc(ack(H,'gift-while-offline')));
 // Bob's Android reconnects; SERVER fetch recovers the gift that was
 // missed during the outage. The client plays it, THEN ACKs real receipt.
 const recent=await assertSucceeds(getDocsFromServer(query(
  collection(B,base,'events'),orderBy('createdAt','desc'),limit(60))));
 if(recent.docs.length!==2)throw new Error('Server gift rejoin did not fetch two real events');
 const unseen=recent.docs.filter(d=>d.id==='gift-while-offline'&&
  d.data().type==='gift'&&d.data().targetUid==='bob');
 if(unseen.length!==1)throw new Error('Missed recipient event lost on reconnect');
 await assertSucceeds(setDoc(ack(B,'gift-while-offline'),receipt('gift-while-offline')));
 await assertFails(setDoc(ack(B,'gift-delivered'),receipt('gift-delivered')));
 await assertFails(setDoc(ack(E,'gift-while-offline'),{
  ...receipt('gift-while-offline'),recipientUid:'eve'}));
 await assertFails(setDoc(ack(H,'gift-while-offline'),receipt('gift-while-offline')));
 for(const id of ['gift-delivered','gift-while-offline']){
  const received=await assertSucceeds(getDoc(ack(H,id)));
  if(received.data().recipientUid!=='bob')throw new Error('Wrong recipient ACK persisted');
 }
 console.log('PASS emulator multi-account: two real Gifts, receiver resumes server event and ACK after rejoin, sender sees receipts, no duplicate or spectator forgery');
}finally{await env.cleanup();}
