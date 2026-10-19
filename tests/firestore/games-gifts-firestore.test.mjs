import fs from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,writeBatch,serverTimestamp,getDocs,collection} from 'firebase/firestore';

const env=await initializeTestEnvironment({
 projectId:'demo-king-games-gifts-9937',
 firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}
});
const host=env.authenticatedContext('host').firestore();
const alice=env.authenticatedContext('alice').firestore();
const eve=env.authenticatedContext('eve').firestore();
const path='live_rooms/party-gaming-9937';
const room=db=>doc(db,path);
const member=(db,uid)=>doc(db,path,'members',uid);
const event=(db,id)=>doc(db,path,'events',id);
const message=(db,id)=>doc(db,path,'messages',id);
const createMember=(db,uid)=>setDoc(member(db,uid),{uid,name:uid});
const gift=(who,recipient,override={})=>({
 actorUid:who,actorName:who,type:'gift',freeGift:true,giftName:'Rose',
 giftIcon:'🌹',giftValue:0,giftUnitCost:0,giftQty:2,targetUid:recipient,
 targetName:recipient,text:who+' sent FREE Rose to '+recipient,
 createdAt:serverTimestamp(),...override
});
const dice=(who,value)=>({
 actorUid:who,actorName:who,type:'dice_room_997',
 text:'🎲 '+value+' / 6',createdAt:serverTimestamp()
});
try {
 await env.clearFirestore();
 await assertSucceeds(setDoc(room(host),{name:'KING Gaming Party',ownerUid:'host',ownerName:'KING',isPrivate:false,hasPassword:false,entranceMode:'free',closed:false}));
 await assertSucceeds(createMember(host,'host'));
 await assertSucceeds(createMember(alice,'alice'));
 // A free gift event is only valid with its matching chat in the same atomic batch.
 await assertFails(setDoc(event(alice,'gift-ok'),gift('alice','host')));
 await assertFails(setDoc(event(alice,'gift-nonmember'),gift('alice','eve')));
 await assertFails(setDoc(event(eve,'gift-notjoined'),gift('eve','host')));
 await assertFails(setDoc(event(alice,'gift-paid-forgery'),gift('alice','host',{freeGift:false,giftValue:900})));
 await assertFails(setDoc(event(alice,'gift-too-many'),gift('alice','host',{giftQty:10})));
 await assertFails(setDoc(event(alice,'gift-fake-actor'),gift('host','alice')));
 // Both room event AND message must succeed or fail atomically when sent as a batch.
 const okBatch=writeBatch(alice);
 okBatch.set(event(alice,'gift-batch-ok'),gift('alice','host',{giftId:'gift-batch-ok'}));
 okBatch.set(message(alice,'gift-batch-ok'),{
    giftId:'gift-batch-ok',senderUid:'alice',senderName:'alice',senderLevel:1,senderVip:0,
    text:'alice sent FREE Rose to host',type:'gift',
    giftIcon:'🌹',giftName:'Rose',giftValue:0,giftQty:2,
    targetUid:'host',targetName:'host',createdAt:serverTimestamp()});
 await assertSucceeds(okBatch.commit());
 const failBatch=writeBatch(alice);
 failBatch.set(event(alice,'gift-invalid-batch'),gift('alice','eve'));
 failBatch.set(message(alice,'gift-message-must-not-save'),{
    senderUid:'alice',senderName:'alice',text:'invalid receiver',type:'gift',
    createdAt:serverTimestamp()});
 await assertFails(failBatch.commit());
 await assertSucceeds(setDoc(event(alice,'dice1'),dice('alice',1)));
 await assertSucceeds(setDoc(event(alice,'dice6'),dice('alice',6)));
 await assertFails(setDoc(event(alice,'dice7'),dice('alice',7)));
 await assertFails(setDoc(event(alice,'dicehack'),dice('alice','999')));
 await assertFails(setDoc(event(alice,'dicewrong'),dice('alice','1 + 💎100')));
 await assertFails(setDoc(event(eve,'dice-unjoined'),dice('eve',5)));
 console.log('PASS true signed-in room member gifts, invalid recipient rejection, atomic gift+chat batch, real dice and forged roll denial');
} finally {await env.cleanup();}
