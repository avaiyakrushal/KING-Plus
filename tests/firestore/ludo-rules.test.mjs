import fs from 'node:fs';
import assert from 'node:assert/strict';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,getDoc,setDoc,updateDoc,deleteDoc,serverTimestamp,Timestamp,collection,getDocs,runTransaction} from 'firebase/firestore';
const env=await initializeTestEnvironment({projectId:'demo-kingplus',firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}});
const a=env.authenticatedContext('alice').firestore(),b=env.authenticatedContext('bob').firestore(),e=env.authenticatedContext('eve').firestore();
const ref=(db,c='ABC234')=>doc(db,'ludo_matches',c);
const fresh=(code='ABC234',capacity=2)=>({schema:1,code,ownerUid:'alice',players:['alice','','',''],names:['Alice','','',''],ready:[false,false,false,false],capacity,pieces:Array(16).fill(-1),turn:0,dice:0,sixes:0,winner:-1,phase:'lobby',revision:0,action:'create',actorUid:'alice',selected:-1,updatedAt:serverTimestamp(),rollAt:serverTimestamp()});
const read=async(db=a,c='ABC234')=>(await getDoc(ref(db,c))).data();
async function write(db,uid,action,changes={},c='ABC234'){let old=await read(db,c);let n={...old,...changes,actorUid:uid,action,revision:old.revision+1,updatedAt:serverTimestamp()};return setDoc(ref(db,c),n);}
async function seed(changes={},c='ABC234'){await env.withSecurityRulesDisabled(async ctx=>setDoc(ref(ctx.firestore(),c),{...fresh(c),players:['alice','','bob',''],names:['Alice','','Bob',''],ready:[true,false,true,false],phase:'roll',updatedAt:Timestamp.fromMillis(Date.now()),rollAt:Timestamp.fromMillis(6000),...changes}));}
const pass=msg=>console.log('PASS',msg);
try {
 await env.clearFirestore();
 await assertSucceeds(getDoc(ref(a)));await assertSucceeds(setDoc(ref(a),fresh()));
 await assertFails(setDoc(ref(e,'EEE234'),{...fresh('EEE234'),ownerUid:'eve',actorUid:'eve'}));
 await assertSucceeds(write(b,'bob','join',{players:['alice','','bob',''],names:['Alice','','Bob',''],selected:2}));
 await assertFails(write(e,'eve','join',{players:['alice','eve','bob',''],names:['Alice','Eve','Bob',''],selected:1}));
 await assertFails(write(a,'alice','start',{phase:'roll'}));
 await assertSucceeds(write(a,'alice','ready',{ready:[true,false,false,false],selected:0}));
 await assertSucceeds(write(b,'bob','ready',{ready:[true,false,true,false],selected:2}));
 await assertSucceeds(write(a,'alice','start',{phase:'roll',selected:-1}));
 await assertFails(getDoc(ref(e)));await assertFails(getDocs(collection(a,'ludo_matches')));
 await assertFails(write(b,'bob','roll',{phase:'resolve',rollAt:serverTimestamp(),selected:-1}));
 await assertSucceeds(write(a,'alice','roll',{phase:'resolve',rollAt:serverTimestamp(),selected:-1}));
 let o=await read();let d=o.rollAt.toMillis()%6+1,can=d===6;
 await assertFails(write(a,'alice','resolve',{dice:d===6?5:6,phase:can?'move':'roll',turn:can?0:2,sixes:can?1:0}));
 await assertSucceeds(write(b,'bob','resolve',{dice:d,phase:can?'move':'roll',turn:can?0:2,sixes:can?1:0}));
 pass('create/join/ready/start, private reads, turn ownership, server timestamp dice');
 await seed({phase:'move',dice:6,sixes:1});let pieces=Array(16).fill(-1);pieces[0]=0;
 await assertFails(write(b,'bob','move',{selected:0,pieces}));
 let bad=[...pieces];bad[0]=56;await assertFails(write(a,'alice','move',{selected:0,pieces:bad,phase:'roll'}));
 await assertSucceeds(write(a,'alice','move',{selected:0,pieces,phase:'roll'}));
 pass('six enters, opponent and teleport rejected');
 pieces=Array(16).fill(-1);pieces[0]=5;pieces[8]=32;await seed({phase:'move',dice:1,pieces});let capture=[...pieces];capture[0]=6;capture[8]=-1;
 await assertSucceeds(write(a,'alice','move',{selected:0,pieces:capture,phase:'roll'}));
 pieces[0]=7;pieces[8]=34;await seed({phase:'move',dice:1,pieces});let safe=[...pieces];safe[0]=8;
 await assertFails(write(a,'alice','move',{selected:0,pieces:[...safe.slice(0,8),-1,...safe.slice(9)],phase:'roll',turn:2}));
 await assertSucceeds(write(a,'alice','move',{selected:0,pieces:safe,phase:'roll',turn:2}));
 pass('capture and safe-star protection');
 pieces=Array(16).fill(-1);pieces[0]=55;pieces[1]=pieces[2]=pieces[3]=56;await seed({phase:'move',dice:1,pieces});let home=[...pieces];home[0]=56;
 await assertSucceeds(write(a,'alice','move',{selected:0,pieces:home,phase:'finished',winner:0}));
 await assertSucceeds(write(a,'alice','rematch',{pieces:Array(16).fill(-1),phase:'lobby',ready:[false,false,false,false],dice:0,sixes:0,turn:0,winner:-1,selected:-1}));
 pass('exact home, real winner and rematch');
 await seed();await assertFails(write(b,'bob','timeout',{turn:2}));await seed({updatedAt:Timestamp.fromMillis(Date.now()-65000)});await assertSucceeds(write(b,'bob','timeout',{turn:2}));
 await assertSucceeds(write(b,'bob','cancel',{phase:'cancelled'}));pass('early timeout rejected, expired turn skips and cancellation');
 await seed({phase:'resolve',sixes:2,rollAt:Timestamp.fromMillis(6005)});await assertSucceeds(write(a,'alice','resolve',{phase:'roll',dice:6,sixes:0,turn:2}));pass('third six forfeits the turn');
 await seed({phase:'lobby',ready:[false,false,false,false]});await assertSucceeds(write(a,'alice','leave',{players:['','','bob',''],names:['','','Bob',''],ownerUid:'bob',selected:0}));await assertSucceeds(deleteDoc(ref(b)));pass('host transfer and empty lobby cleanup');
 // Simultaneous joins: transactions must never assign the same seat to two accounts.
 await setDoc(ref(a,'RACE22'),fresh('RACE22'));
 const tryJoin=async(db,uid)=>runTransaction(db,async tx=>{const rr=ref(db,'RACE22'),ss=await tx.get(rr),x=ss.data();if(x.players[2])throw Error('full');tx.set(rr,{...x,players:['alice','',uid,''],names:['Alice','',uid,''],selected:2,actorUid:uid,action:'join',revision:x.revision+1,updatedAt:serverTimestamp()});});
 const race=await Promise.allSettled([tryJoin(b,'bob'),tryJoin(e,'eve')]);assert.equal(race.filter(x=>x.status==='fulfilled').length,1);pass('concurrent seat claiming');
 await assertSucceeds(setDoc(ref(a,'FOUR22'),fresh('FOUR22',4)));
 for(const [uid,i] of [['bob',1],['eve',2],['dave',3]]){const db=env.authenticatedContext(uid).firestore(),x=await read(db,'FOUR22');x.players[i]=uid;x.names[i]=uid;await assertSucceeds(write(db,uid,'join',{players:x.players,names:x.names,selected:i},'FOUR22'));}
 for(const [uid,i] of [['alice',0],['bob',1],['eve',2],['dave',3]]){const db=env.authenticatedContext(uid).firestore(),x=await read(db,'FOUR22');x.ready[i]=true;await assertSucceeds(write(db,uid,'ready',{ready:x.ready,selected:i},'FOUR22'));}
 await assertSucceeds(write(a,'alice','start',{phase:'roll',selected:-1},'FOUR22'));pass('four real clients join, ready and start');

 for(let turn=0;turn<4;turn++){
  const ids=['alice','bob','eve','dave'],db=env.authenticatedContext(ids[turn]).firestore(),pos=Array(16).fill(9),other=(turn+1)%4;
  pos[turn*4]=5;for(let k=0;k<4;k++)pos[other*4+k]=((turn*13+6-other*13)%52+52)%52;
  await seed({capacity:4,players:ids,names:ids,ready:[true,true,true,true],phase:'move',dice:1,turn,pieces:pos});
  const after=[...pos];after[turn*4]=6;for(let k=0;k<4;k++)after[other*4+k]=-1;
  await assertSucceeds(write(db,ids[turn],'move',{pieces:after,selected:0,phase:'roll'}));
 }
 pass('all four colors: crowded-board captures stay within rule evaluation limits');
 console.log('ALL ONLINE LUDO SECURITY TESTS PASSED');
}finally{await env.cleanup();}
