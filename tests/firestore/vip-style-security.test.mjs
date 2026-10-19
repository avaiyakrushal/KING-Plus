import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,getDoc,setDoc,updateDoc,deleteDoc} from 'firebase/firestore';
const env=await initializeTestEnvironment({projectId:'demo-king-vipstyle-10041',firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}});
const owner=env.authenticatedContext('alice').firestore();
const friend=env.authenticatedContext('bob').firestore();
const fakeAdmin=env.authenticatedContext('admin',{admin:true}).firestore();
const anon=env.unauthenticatedContext().firestore();
const grant=(db)=>doc(db,'king_vip_identity_grants','alice');
const reservation=(db)=>doc(db,'king_vip_number_registry','888888');
const catalog=(db)=>doc(db,'king_vip_style_catalog','current');
try {
 await env.clearFirestore();
 await env.withSecurityRulesDisabled(async ctx=>{
   const root=ctx.firestore();
   await setDoc(catalog(root),{enabled:false,paymentMode:'verified_diamonds',vipNumberDiamonds:5000,nameColorDiamonds:1000});
   await setDoc(grant(root),{uid:'alice',vipNumber:'888888',nameColor:'gold',ownedColors:['gold']});
   await setDoc(reservation(root),{uid:'alice',number:'888888'});
 });
 const seen=await assertSucceeds(getDoc(grant(friend)));
 if(!seen.exists()||seen.data().vipNumber!=='888888')throw Error('Valid owner prestige not readable by other authenticated account');
 await assertSucceeds(getDoc(reservation(friend)));
 await assertSucceeds(getDoc(catalog(friend)));
 await assertFails(getDoc(grant(anon)));
 await assertFails(getDoc(catalog(anon)));
 for(const c of [owner,friend,fakeAdmin]){
   await assertFails(setDoc(grant(c),{uid:'alice',vipNumber:'123456',nameColor:'gold'}));
   await assertFails(updateDoc(grant(c),{nameColor:'rose'}));
   await assertFails(deleteDoc(grant(c)));
   await assertFails(setDoc(reservation(c),{uid:'bob',number:'888888'}));
   await assertFails(updateDoc(reservation(c),{uid:'bob'}));
   await assertFails(setDoc(catalog(c),{enabled:true,paymentMode:'verified_diamonds',vipNumberDiamonds:1,nameColorDiamonds:1}));
 }
 await assertFails(setDoc(doc(owner,'wallets','alice'),{coins:9999999}));
 console.log('PASS VIP ownership, registry, pricing and wallet are server-only; signed-in viewers can see verified ownership');
}finally{await env.cleanup();}
