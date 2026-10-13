import fs from 'node:fs';
import {initializeTestEnvironment,assertFails,assertSucceeds} from '@firebase/rules-unit-testing';
import {doc,setDoc,getDoc,serverTimestamp,writeBatch,collection,query,where,getDocs} from 'firebase/firestore';

const env=await initializeTestEnvironment({
 projectId:'demo-king-gift-receipts-9941',
 firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}
});
const H=env.authenticatedContext('host').firestore();
const B=env.authenticatedContext('bob').firestore();
const E=env.authenticatedContext('eve').firestore();
const room='live_rooms/ack-9941';
const root=db=>doc(db,room);
const member=(db,uid)=>doc(db,room,'members',uid);
const event=(db,id)=>doc(db,room,'events',id);
const chat=(db,id)=>doc(db,room,'messages',id);
const receipt=(db,id)=>doc(db,room,'gift_receipts',id);
const gift=(id,to)=>({giftId:id,actorUid:'host',actorName:'Host',type:'gift',text:'Host sent FREE Rose to '+to,
 freeGift:true,giftName:'Rose',giftIcon:'🌹',giftValue:0,giftUnitCost:0,giftQty:1,
 targetUid:to,targetName:to,createdAt:serverTimestamp()});
const message=(id,to)=>({giftId:id,senderUid:'host',senderName:'Host',senderLevel:1,senderVip:0,type:'gift',
 text:'Host sent FREE Rose to '+to,giftName:'Rose',giftIcon:'🌹',giftValue:0,giftQty:1,
 targetUid:to,targetName:to,createdAt:serverTimestamp()});
const ack=(id,recipient='bob',sender='host',name='Rose')=>({
 giftId:id,senderUid:sender,recipientUid:recipient,giftName:name,createdAt:serverTimestamp()
});
try {
 await env.clearFirestore();
 await assertSucceeds(setDoc(root(H),{
  ownerUid:'host',name:'Gift Live Room',isPrivate:false,hasPassword:false,entranceMode:'free',closed:false
 }));
 await assertSucceeds(setDoc(member(H,'host'),{uid:'host',name:'Host'}));
 await assertSucceeds(setDoc(member(B,'bob'),{uid:'bob',name:'Bob'}));
 await assertSucceeds(setDoc(member(E,'eve'),{uid:'eve',name:'Eve'}));
 const sent=writeBatch(H);
 sent.set(event(H,'gift-41'),gift('gift-41','bob'));
 sent.set(chat(H,'gift-41'),message('gift-41','bob'));
 await assertSucceeds(sent.commit()); // sender saved gift and chat atomically
 await assertFails(getDoc(receipt(H,'gift-41'))); // not yet delivered
 await assertFails(setDoc(receipt(H,'gift-41'),ack('gift-41'))); // sender cannot self-confirm
 await assertFails(setDoc(receipt(E,'gift-41'),ack('gift-41','eve'))); // spectator cannot fake
 await assertFails(setDoc(receipt(B,'gift-41'),ack('gift-41','bob','evil-sender'))); // wrong sender
 await assertFails(setDoc(receipt(B,'gift-41'),ack('gift-41','bob','host','Diamond'))); // wrong gift
 await assertFails(setDoc(receipt(B,'not-gift'),ack('not-gift'))); // no real event
 await assertSucceeds(setDoc(receipt(B,'gift-41'),ack('gift-41'))); // real recipient visible
 await assertSucceeds(getDoc(receipt(H,'gift-41'))); // sender can now verify confirmed
 await assertSucceeds(getDoc(receipt(B,'gift-41'))); // recipient sees own receipt
 await assertFails(getDoc(receipt(E,'gift-41'))); // unrelated Room member blocked
 await assertFails(setDoc(receipt(B,'gift-41'),ack('gift-41'))); // no overwriting history
 const sentReceipts=query(collection(H,room,'gift_receipts'),where('senderUid','==','host'));
 const own=await getDocs(sentReceipts);if(own.size!==1)throw new Error('sender should see exactly one confirmed delivery');
 const partyReceipts=query(collection(E,room,'gift_receipts'),where('recipientUid','==','bob'));
 await assertFails(getDocs(partyReceipts));
 console.log('PASS real Gift recipient ack, cross-device sender reads, spectator/sender forgery denied, immutable record');
} finally {await env.cleanup();}
