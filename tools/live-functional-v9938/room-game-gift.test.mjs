import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import fs from 'node:fs';
import {doc,setDoc,getDoc,writeBatch,serverTimestamp} from 'firebase/firestore';

const env=await initializeTestEnvironment({projectId:'demo-king-9938-live',
 firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}});
const H=env.authenticatedContext('host').firestore(),A=env.authenticatedContext('alice').firestore(),B=env.authenticatedContext('bob').firestore(),E=env.authenticatedContext('eve').firestore();
const base='live_rooms/party-9938',room=db=>doc(db,base);
const member=(db,uid)=>doc(db,base,'members',uid);
const ev=(db,id)=>doc(db,base,'events',id);
const msg=(db,id)=>doc(db,base,'messages',id);
const state=db=>doc(db,base,'game_state','current');
const move=(db,uid)=>doc(db,base,'game_moves',uid);
const g=(id,who,to)=>({giftId:id,actorUid:who,actorName:who,type:'gift',text:who+' sent FREE Rose to '+to,
 freeGift:true,giftName:'Rose',giftIcon:'🌹',giftValue:0,giftUnitCost:0,giftQty:2,targetUid:to,targetName:to,createdAt:serverTimestamp()});
const m=(id,who,to)=>({giftId:id,senderUid:who,senderName:who,senderLevel:1,senderVip:0,type:'gift',
 text:who+' sent FREE Rose to '+to,giftName:'Rose',giftIcon:'🌹',giftValue:0,giftQty:2,targetUid:to,targetName:to,createdAt:serverTimestamp()});
const ttt=(uid,round,pos,type='ttt')=>({uid,name:uid,type,roundId:round,move:'cell:'+pos,score:pos,updatedAt:serverTimestamp()});
try {
 await env.clearFirestore();
 await assertSucceeds(setDoc(room(H),{ownerUid:'host',name:'KING Party',isPrivate:false,hasPassword:false,entranceMode:'free',closed:false}));
 await assertSucceeds(setDoc(member(H,'host'),{uid:'host',name:'Host'}));
 await assertSucceeds(setDoc(member(A,'alice'),{uid:'alice',name:'Alice'}));
 await assertSucceeds(setDoc(member(B,'bob'),{uid:'bob',name:'Bob'}));
 const giftBatch=writeBatch(A);
 giftBatch.set(ev(A,'gift_1'),g('gift_1','alice','bob'));giftBatch.set(msg(A,'gift_1'),m('gift_1','alice','bob'));
 await assertSucceeds(giftBatch.commit());
 await assertSucceeds(getDoc(ev(B,'gift_1')));
 await assertSucceeds(getDoc(msg(B,'gift_1')));
 await assertFails(setDoc(ev(A,'ghost_no_chat'),g('ghost_no_chat','alice','bob')));
 await assertFails(setDoc(msg(A,'ghost_no_event'),m('ghost_no_event','alice','bob')));
 const altered=writeBatch(A);
 altered.set(ev(A,'gift_mismatch'),g('gift_mismatch','alice','bob'));
 altered.set(msg(A,'gift_mismatch'),m('gift_mismatch','alice','host'));
 await assertFails(altered.commit());
 const noMember=writeBatch(A);
 noMember.set(ev(A,'ghost_target'),g('ghost_target','alice','eve'));
 noMember.set(msg(A,'ghost_target'),m('ghost_target','alice','eve'));
 await assertFails(noMember.commit());
 const forged=writeBatch(E);
 forged.set(ev(E,'unjoined'),g('unjoined','eve','host'));
 forged.set(msg(E,'unjoined'),m('unjoined','eve','host'));
 await assertFails(forged.commit());
 // Tic Tac Toe round exists before any game moves, and only the named actor can move.
 await assertSucceeds(setDoc(state(H),{actorUid:'host',type:'ttt',status:'active',roundId:1001,
   xUid:'alice',oUid:'bob',turnUid:'alice',board:'.........',xName:'Alice',oName:'Bob',prompt:'Match',result:''}));
 await assertFails(setDoc(move(B,'bob'),ttt('bob',1001,1)));
 await assertFails(setDoc(move(E,'eve'),ttt('eve',1001,1)));
 await assertFails(setDoc(move(A,'alice'),ttt('alice',1000,1)));
 await assertFails(setDoc(move(A,'alice'),ttt('alice',1001,11)));
 await assertFails(setDoc(move(A,'alice'),ttt('alice',1001,1,'ttt')));
 await assertSucceeds(setDoc(move(A,'alice'),ttt('alice',1001,1)));
 await assertFails(setDoc(move(B,'bob'),ttt('bob',1001,2)));
 // The host accepts Alice's move, deletes her pending entry, updates current turn.
 await assertSucceeds(setDoc(state(H),{actorUid:'host',type:'ttt',status:'active',roundId:1001,
   xUid:'alice',oUid:'bob',turnUid:'bob',board:'.X.......',xName:'Alice',oName:'Bob',prompt:'Match',result:''}));
 await assertFails(setDoc(move(A,'alice'),ttt('alice',1001,2)));
 await assertSucceeds(setDoc(move(B,'bob'),ttt('bob',1001,2)));
 // Host closes game. Stale move denied.
 await assertSucceeds(setDoc(state(H),{actorUid:'host',type:'ttt',status:'closed',roundId:1001,
   xUid:'alice',oUid:'bob',turnUid:'',board:'.XO......',xName:'Alice',oName:'Bob',prompt:'Match',result:'Draw'}));
 await assertFails(setDoc(move(A,'alice'),ttt('alice',1001,3)));
 console.log('PASS Gift event + chat atomic, no ghost/mismatch/no-member; TicTacToe turn and stale-round reject');
} finally {await env.cleanup();}
