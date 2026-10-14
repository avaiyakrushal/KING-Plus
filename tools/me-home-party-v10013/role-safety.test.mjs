import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,deleteDoc,getDoc} from 'firebase/firestore';
const env=await initializeTestEnvironment({projectId:'demo-king-atomic-moderation-v10011',firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}});
try{
 await env.clearFirestore();
 const as=uid=>env.authenticatedContext(uid).firestore();
 const host=as('host'),cohost=as('bob'),charlie=as('charlie'),dave=as('dave');
 const base='live_rooms/v10013-room';
 const at=(db,path)=>doc(db,base,...path.split('/'));
 await assertSucceeds(setDoc(doc(host,base),{ownerUid:'host',name:'KING v10013',entranceMode:'free',closed:false,isPrivate:false,locked:false,muteAll:false,maxSeats:12}));
 for(const [db,uid] of [[host,'host'],[cohost,'bob'],[charlie,'charlie'],[dave,'dave']])await assertSucceeds(setDoc(at(db,'members/'+uid),{uid,name:uid}));
 await assertFails(setDoc(at(host,'roles/ghost'),{uid:'ghost',role:'cohost',grantedBy:'host'}));
 await assertSucceeds(setDoc(at(host,'roles/bob'),{uid:'bob',role:'cohost',grantedBy:'host'}));
 await assertSucceeds(setDoc(at(host,'roles/charlie'),{uid:'charlie',role:'cohost',grantedBy:'host'}));
 await assertSucceeds(setDoc(at(host,'room_bans/dave'),{uid:'dave',active:true,reason:'Rules',byUid:'host'}));
 await assertFails(setDoc(at(host,'roles/dave'),{uid:'dave',role:'cohost',grantedBy:'host'}));
 await assertSucceeds(setDoc(at(host,'room_bans/charlie'),{uid:'charlie',active:true,reason:'Rules',byUid:'host'}));
 await assertFails(deleteDoc(at(cohost,'room_bans/charlie')));
 if(!(await assertSucceeds(getDoc(at(host,'room_bans/charlie')))).exists())throw Error('Co-host removed protected ban');
 await assertSucceeds(deleteDoc(at(host,'room_bans/charlie')));
 await assertSucceeds(deleteDoc(at(host,'room_bans/dave')));
 await assertSucceeds(setDoc(at(host,'roles/dave'),{uid:'dave',role:'cohost',grantedBy:'host'}));
 console.log('PASS v10.0.13: no ghost/banned co-hosts, protected unban, valid grants');
}finally{await env.cleanup();}
