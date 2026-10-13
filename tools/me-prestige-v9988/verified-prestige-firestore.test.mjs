import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,getDoc,updateDoc,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({
 projectId:'demo-king-me-verified-prestige-9988',
 firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}
});
const A=env.authenticatedContext('alice123').firestore(),
 B=env.authenticatedContext('bob123').firestore(),
 FakeAdmin=env.authenticatedContext('admin123',{admin:true}).firestore();
const profile=(db,u)=>doc(db,'public_profiles',u);
const prestige=(db,u)=>doc(db,'king_public_prestige',u);
try{
 await env.clearFirestore();
 // Public profile is personal metadata, NEVER a source of paid VIP.
 await assertSucceeds(setDoc(profile(A,'alice123'),{
   uid:'alice123',displayName:'Alice',vipLevel:0,vipPoints:0,
   level:1,xp:0,updatedAt:serverTimestamp()
 }));
 await assertFails(setDoc(profile(A,'alice123'),{
   uid:'alice123',displayName:'Alice',vipLevel:50,vipPoints:999999,
   level:99,updatedAt:serverTimestamp()
 }));
 await assertFails(updateDoc(profile(A,'alice123'),{vipLevel:50}));
 await assertFails(updateDoc(profile(A,'alice123'),{vipPoints:999999}));
 await assertFails(updateDoc(profile(A,'alice123'),{nobleLevel:10}));
 await assertFails(updateDoc(profile(B,'alice123'),{displayName:'Hijack'}));
 await assertSucceeds(updateDoc(profile(A,'alice123'),{
   displayName:'Alice Real',bio:'Music, games and friends'
 }));
 await assertFails(setDoc(prestige(A,'alice123'),{
   uid:'alice123',vipLevel:50,nobleLevel:10,rechargeTotal:99999999
 }));
 await assertFails(setDoc(prestige(FakeAdmin,'alice123'),{
   uid:'alice123',vipLevel:50,nobleLevel:10,rechargeTotal:99999999
 }));
 await env.withSecurityRulesDisabled(async ctx=>{
   await setDoc(prestige(ctx.firestore(),'alice123'),{
     uid:'alice123',vipLevel:4,nobleLevel:0,rechargeTotal:7000,
     verifiedAt:serverTimestamp()
   });
 });
 const snapshot=await assertSucceeds(getDoc(prestige(B,'alice123')));
 if(!snapshot.exists()||snapshot.data().vipLevel!==4)
    throw Error('Friend cannot view server-authorized VIP grant');
 await assertFails(updateDoc(prestige(A,'alice123'),{vipLevel:50}));
 await assertFails(updateDoc(prestige(FakeAdmin,'alice123'),{vipLevel:50}));
 await assertFails(setDoc(doc(A,'wallets','alice123'),{diamonds:999999}));
 await assertFails(setDoc(doc(A,'wallet_ledger','paid-fake'),{diamonds:900000}));
 console.log('PASS 3 Firebase accounts: fake public VIP/Noble levels denied, owner name edits pass, public paid badge server-only, even Admin claim cannot create grants, wallet immutable');
}finally{await env.cleanup();}
