import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,getDoc,runTransaction,onSnapshot} from 'firebase/firestore';
const env=await initializeTestEnvironment({projectId:'demo-king-saved-rooms-9990',firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}});
const a=env.authenticatedContext('alice').firestore(),a2=env.authenticatedContext('alice').firestore(),b=env.authenticatedContext('bob').firestore(),guest=env.unauthenticatedContext().firestore();
const account=db=>doc(db,'users','alice');const field='savedPartyRooms9990';
try{
 await env.clearFirestore();
 await assertSucceeds(setDoc(account(a),{familyCode:'ABCDEF',displayName:'Alice'}));
 await assertSucceeds(setDoc(account(a),{[field]:{room1:{name:'Music',favorite:true,visited:100}}},{mergeFields:[field]}));
 const first=await assertSucceeds(getDoc(account(a2)));
 if(first.data()[field].room1.favorite!==true||first.data().familyCode!=='ABCDEF')throw Error('Cross-device data missing or profile overwritten');
 await assertFails(getDoc(account(b)));await assertFails(getDoc(account(guest)));
 await assertFails(setDoc(account(b),{[field]:{}},{mergeFields:[field]}));
 // Simultaneous phones mutate distinct rooms using retrying transactions.
 const change=(db,id)=>runTransaction(db,async tx=>{const ref=account(db),s=await tx.get(ref);tx.set(ref,{[field]:{...s.data()[field],[id]:{name:id,favorite:true,visited:200}}},{mergeFields:[field]});});
 await Promise.all([assertSucceeds(change(a,'room2')),assertSucceeds(change(a2,'room3'))]);
 const merged=(await getDoc(account(a))).data();
 if(Object.keys(merged[field]).length!==3||merged.displayName!=='Alice')throw Error('Concurrent room save lost data');
 // Remote listener receives favorite removal while keeping visit history.
 let stop;const live=new Promise((resolve,reject)=>{const timeout=setTimeout(()=>{stop?.();reject(Error('Remote live update timed out'));},12000);stop=onSnapshot(account(a2),s=>{if(s.data()?.[field]?.room1?.favorite===false){clearTimeout(timeout);stop();resolve();}},reject);});
 await assertSucceeds(runTransaction(a,async tx=>{const s=await tx.get(account(a));const rooms=s.data()[field];rooms.room1.favorite=false;tx.set(account(a),{[field]:rooms},{mergeFields:[field]});}));
 await live;
 if((await getDoc(account(a2))).data()[field].room1.visited!==100)throw Error('Unfavorite removed visit');
 console.log('PASS two emulator clients: live sync, account privacy, concurrent writes and profile preservation');
}finally{await env.cleanup();}
