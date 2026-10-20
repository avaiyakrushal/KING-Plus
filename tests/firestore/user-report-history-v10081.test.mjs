import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {collection,doc,writeBatch,getDoc,updateDoc,deleteDoc,serverTimestamp} from 'firebase/firestore';

const env=await initializeTestEnvironment({
  projectId:'demo-king-user-report-history-v10081',
  firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}
});
try{
  await env.clearFirestore();
  const alice=env.authenticatedContext('alice').firestore();
  const bob=env.authenticatedContext('bob').firestore();
  const admin=env.authenticatedContext('moderator',{admin:true}).firestore();
  const reportId='report-1';
  const report=doc(alice,'reports',reportId);
  const receipt=doc(alice,'user_report_history','alice','items',reportId);
  const batch=writeBatch(alice);
  batch.set(report,{reporterUid:'alice',target:'KING User 18',reason:'Spam',status:'new',createdAt:serverTimestamp()});
  batch.set(receipt,{reporterUid:'alice',reportId,target:'KING User 18',reason:'Spam',submittedAt:serverTimestamp()});
  await assertSucceeds(batch.commit());
  const ownReceipt=await assertSucceeds(getDoc(receipt));
  if(ownReceipt.data().reportId!=='report-1'||'status' in ownReceipt.data())throw Error('Receipt must contain only a submission record, not moderation status');
  await assertSucceeds(getDoc(doc(admin,'reports',reportId)));
  await assertFails(getDoc(doc(alice,'reports',reportId)));
  await assertFails(getDoc(doc(bob,'user_report_history','alice','items',reportId)));
  await assertFails(updateDoc(receipt,{target:'changed'}));
  await assertFails(deleteDoc(receipt));

  // A receipt must be paired atomically with the matching private moderation report.
  await assertFails(writeBatch(alice).set(
    doc(alice,'user_report_history','alice','items','orphan'),
    {reporterUid:'alice',reportId:'orphan',target:'KING User 18',reason:'Spam',submittedAt:serverTimestamp()}
  ).commit());

  const forged=writeBatch(bob);
  forged.set(doc(bob,'reports','forged'),{reporterUid:'bob',target:'KING User 18',reason:'Spam',status:'new',createdAt:serverTimestamp()});
  forged.set(doc(bob,'user_report_history','alice','items','forged'),{reporterUid:'alice',reportId:'forged',target:'KING User 18',reason:'Spam',submittedAt:serverTimestamp()});
  await assertFails(forged.commit());

  const mismatched=writeBatch(alice);
  mismatched.set(doc(alice,'reports','mismatch'),{reporterUid:'alice',target:'KING User 19',reason:'Spam',status:'new',createdAt:serverTimestamp()});
  mismatched.set(doc(alice,'user_report_history','alice','items','mismatch'),{reporterUid:'alice',reportId:'mismatch',target:'KING User 18',reason:'Spam',submittedAt:serverTimestamp()});
  await assertFails(mismatched.commit());
  console.log('PASS own cross-device report receipts; no other-account reads, edits, deletes, orphan receipts, or mismatched mirrors');
}finally{await env.cleanup();}
