import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import fs from 'node:fs';
import {doc,setDoc,updateDoc,getDoc} from 'firebase/firestore';
const env=await initializeTestEnvironment({projectId:'demo-king-mic-consent-9956',
 firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}});
const H=env.authenticatedContext('host').firestore(),C=env.authenticatedContext('cohost').firestore(),
 A=env.authenticatedContext('alice').firestore(),B=env.authenticatedContext('bob').firestore();
const root='live_rooms/mic-safety-9956';
const room=db=>doc(db,root),member=(db,id)=>doc(db,root,'members',id),seat=(db,id)=>doc(db,root,'seats',String(id));
try{
 await env.clearFirestore();
 await assertSucceeds(setDoc(room(H),{ownerUid:'host',name:'Real Party',
    entranceMode:'free',locked:false,isPrivate:false,closed:false}));
 for(const [db,id] of [[H,'host'],[C,'cohost'],[A,'alice'],[B,'bob']])
    await assertSucceeds(setDoc(member(db,id),{uid:id,name:id}));
 await assertSucceeds(setDoc(doc(H,root,'roles','cohost'),{uid:'cohost',role:'cohost'}));
 await assertFails(setDoc(seat(H,1),{uid:'alice',name:'Alice',micOn:true}));
 await assertSucceeds(setDoc(seat(H,1),{uid:'alice',name:'Alice',micOn:false}));
 await assertFails(updateDoc(seat(H,1),{micOn:true}));
 await assertFails(updateDoc(seat(C,1),{micOn:true}));
 await assertFails(updateDoc(seat(B,1),{micOn:true}));
 await assertSucceeds(updateDoc(seat(A,1),{micOn:true}));
 await assertSucceeds(updateDoc(seat(H,1),{micOn:false}));
 await assertSucceeds(updateDoc(seat(A,1),{micOn:true}));
 await assertSucceeds(updateDoc(seat(C,1),{micOn:false}));
 await assertFails(updateDoc(seat(H,1),{micOn:true}));
 const d=await getDoc(seat(A,1));
 if(d.data().micOn!==false)throw new Error('remote mute not synchronized');
 await assertFails(setDoc(seat(C,2),{uid:'bob',name:'Bob',micOn:true}));
 await assertSucceeds(setDoc(seat(B,2),{uid:'bob',name:'Bob',micOn:false}));
 await assertSucceeds(updateDoc(seat(B,2),{micOn:true}));
 console.log('PASS Firebase: Host/CoHost may mute but cannot remotely activate another member Mic');
}finally{await env.cleanup();}