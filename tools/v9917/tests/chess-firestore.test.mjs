import fs from 'node:fs';
import assert from 'node:assert/strict';
import { initializeTestEnvironment, assertSucceeds, assertFails } from '@firebase/rules-unit-testing';
import { doc, getDoc, setDoc, updateDoc, serverTimestamp } from 'firebase/firestore';

const start='rnbqkbnrpppppppp................................PPPPPPPPRNBQKBNR';
const env=await initializeTestEnvironment({projectId:'demo-kingplus-chess',
  firestore:{host:'127.0.0.1',port:8080,rules:fs.readFileSync('firestore.rules','utf8')}});
const alice=env.authenticatedContext('alice').firestore();
const bob=env.authenticatedContext('bob').firestore();
const eve=env.authenticatedContext('eve').firestore();
const guest=env.authenticatedContext('outsider').firestore();
const room=db=>doc(db,'live_rooms','chess-test-room');
const membership=(db,id)=>doc(db,'live_rooms','chess-test-room','members',id);
const board=db=>doc(db,'live_rooms','chess-test-room','chess_games','main');
const change=(input,from,to)=>{let s=input.split('');s[to]=s[from];s[from]='.';return s.join('');};
try {
  await env.clearFirestore();
  await assertSucceeds(setDoc(room(alice),{ownerUid:'alice',name:'KING Chess',isPrivate:false,closed:false}));
  for(const [db,id] of [[alice,'alice'],[bob,'bob'],[eve,'eve']])
    await assertSucceeds(setDoc(membership(db,id),{uid:id,name:id}));
  const initial={whiteUid:'alice',whiteName:'Alice',blackUid:'',blackName:'',board:start,
    turn:'w',rights:'KQkq',enPassant:-1,status:'waiting',lastMove:'',
    winnerUid:'',revision:0,actorUid:'alice',updatedAt:serverTimestamp()};
  await assertFails(setDoc(board(bob),initial));
  await assertSucceeds(setDoc(board(alice),initial));
  await assertFails(getDoc(board(guest)));
  await assertFails(updateDoc(board(eve),{blackUid:'bob',blackName:'Bob',actorUid:'eve',revision:1,updatedAt:serverTimestamp()}));
  await assertSucceeds(updateDoc(board(bob),{blackUid:'bob',blackName:'Bob',status:'active',
    actorUid:'bob',revision:1,updatedAt:serverTimestamp()}));
  await assertFails(updateDoc(board(eve),{board:'X'.repeat(64),revision:2,
    actorUid:'eve',updatedAt:serverTimestamp()}));
  let pos=change(start,52,36); // Alice e2-e4
  await assertSucceeds(updateDoc(board(alice),{board:pos,turn:'b',enPassant:44,
    actorUid:'alice',revision:2,lastMove:'e2 → e4',updatedAt:serverTimestamp()}));
  await assertFails(updateDoc(board(alice),{board:'X'.repeat(64),turn:'w',
    revision:3,actorUid:'alice',updatedAt:serverTimestamp()}));
  pos=change(pos,12,28); // Bob e7-e5
  await assertSucceeds(updateDoc(board(bob),{board:pos,turn:'w',enPassant:20,
    actorUid:'bob',revision:3,lastMove:'e7 → e5',updatedAt:serverTimestamp()}));
  await assertFails(updateDoc(board(bob),{board:'X'.repeat(64),turn:'b',revision:4,
    actorUid:'bob',updatedAt:serverTimestamp()}));
  await assertSucceeds(updateDoc(board(alice),{status:'resigned',winnerUid:'bob',
    actorUid:'alice',lastMove:'Resigned',revision:4,updatedAt:serverTimestamp()}));
  await assertFails(updateDoc(board(bob),{status:'active',board:start,turn:'w',
    revision:5,actorUid:'bob',updatedAt:serverTimestamp()}));
  await assertSucceeds(updateDoc(board(alice),{status:'active',board:start,turn:'w',
    rights:'KQkq',enPassant:-1,winnerUid:'',lastMove:'',revision:5,
    actorUid:'alice',updatedAt:serverTimestamp()}));
  assert.equal((await assertSucceeds(getDoc(board(bob)))).data().revision,5);
  console.log('PASS Chess Firestore: verified seats, outsider denied, turn order, revision, resign and rematch');
} finally { await env.cleanup(); }