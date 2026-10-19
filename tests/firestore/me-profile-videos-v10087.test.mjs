import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {collection,doc,setDoc,deleteDoc,onSnapshot,query,limit,serverTimestamp} from 'firebase/firestore';

const env=await initializeTestEnvironment({projectId:'demo-king-me-videos-10087',firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}});
const alice=env.authenticatedContext('alice').firestore();
const secondPhone=env.authenticatedContext('alice').firestore();
const bob=env.authenticatedContext('bob').firestore();
const video=doc(alice,'public_profiles/alice/videos/video-10087');
const record={uid:'alice',url:'https://firebasestorage.googleapis.com/v0/b/demo/o/video-10087?alt=media',storagePath:'chat_media/alice/profile_videos/video-10087',fileName:'Profile video',contentType:'video/mp4',sizeBytes:1024,createdAt:serverTimestamp()};
let stop=()=>{};
try{
 await env.clearFirestore();
 let received=false;
 const visible=new Promise((resolve,reject)=>{
   const timeout=setTimeout(()=>reject(Error('Second signed-in device did not receive the cloud video')),10000);
   stop=onSnapshot(query(collection(secondPhone,'public_profiles/alice/videos'),limit(40)),snap=>{
     if(snap.docs.some(d=>d.id==='video-10087'&&d.get('uid')==='alice')){
       received=true;clearTimeout(timeout);resolve();
     }
   },error=>{clearTimeout(timeout);reject(error)});
 });
 await assertSucceeds(setDoc(video,record));
 await visible;
 if(!received)throw Error('Cloud video not visible from the second device');
 await assertFails(setDoc(doc(bob,'public_profiles/alice/videos/forged'),{...record,storagePath:'chat_media/alice/profile_videos/forged'}));
 await assertFails(deleteDoc(doc(bob,'public_profiles/alice/videos/video-10087')));
 await assertSucceeds(deleteDoc(video));
 console.log('PASS v10.0.87 Me Profile video cross-device visibility and owner enforcement');
}finally{stop();await env.cleanup();}
