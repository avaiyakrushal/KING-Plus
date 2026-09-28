'use strict';
const {onCall,HttpsError}=require('firebase-functions/v2/https');
const {onDocumentCreated}=require('firebase-functions/v2/firestore');
const {initializeApp}=require('firebase-admin/app');
const {getFirestore,FieldValue}=require('firebase-admin/firestore');
const {getMessaging}=require('firebase-admin/messaging');
const {createHash}=require('node:crypto');
const D=require('./domain');
initializeApp();
const db=getFirestore();
const account=uid=>db.doc(`kpAccounts/${uid}`);
const now=()=>FieldValue.serverTimestamp();
const wrap=fn=>onCall({region:'us-central1',maxInstances:10},async req=>{
  try {const uid=D.requireAuth(req.auth);return await fn(uid,req.data||{},req);}
  catch(e){if(e instanceof HttpsError)throw e;const code=['invalid-argument','permission-denied','failed-precondition','unauthenticated','not-found','already-exists','resource-exhausted'].includes(e.message)?e.message:'internal';throw new HttpsError(code,code==='internal'?'Service unavailable; retry later.':code);}
});
async function active(uid){const s=await account(uid).get();if(!s.exists)throw new Error('failed-precondition');if(s.get('suspended'))throw new Error('permission-denied');return s;}
const rows=s=>s.docs.map(d=>({id:d.id,...d.data(),createdAt:d.get('createdAt')?.toMillis?.()||0}));
function notice(tx,uid,key,body){tx.set(account(uid).collection('inbox').doc(key),{body,read:false,createdAt:now()});}
exports.kpBootstrap=wrap(async(uid,data,req)=>{
  await db.runTransaction(async tx=>{const ref=account(uid),s=await tx.get(ref);if(s.exists){if(s.get('suspended'))throw new Error('permission-denied');return;}
    tx.create(ref,{name:typeof req.auth.token.name==='string'?req.auth.token.name.slice(0,60):'KING User',balance:0,sentCoins:0,receivedCoins:0,sentGifts:0,receivedGifts:0,suspended:false,notificationsEnabled:false,createdAt:now()});});
  const a=await active(uid);return {uid,admin:req.auth.token.admin===true,catalog:D.catalog,balance:a.get('balance'),notificationsEnabled:a.get('notificationsEnabled')};
});
exports.kpWallet=wrap(async uid=>{const s=await active(uid);return {uid,balance:s.get('balance'),sentGifts:s.get('sentGifts'),receivedGifts:s.get('receivedGifts')};});
exports.kpDailyReward=wrap(async uid=>{
  const day=D.dayKey(Date.now()),ref=account(uid),entry=ref.collection('ledger').doc('daily-'+day);
  return db.runTransaction(async tx=>{const [s,e]=await Promise.all([tx.get(ref),tx.get(entry)]);if(!s.exists||s.get('suspended'))throw new Error('permission-denied');if(e.exists)return {alreadyClaimed:true,balance:s.get('balance')};
    const balance=s.get('balance')+100;tx.update(ref,{balance});tx.create(entry,{amount:100,kind:'daily',createdAt:now()});return {balance,alreadyClaimed:false};});
});
exports.kpSendGift=wrap(async(uid,data)=>{
  const to=D.id(data.to),gift=D.id(data.gift),requestId=D.id(data.requestId);
  const fromRef=account(uid),toRef=account(to),op=fromRef.collection('operations').doc(requestId);
  return db.runTransaction(async tx=>{
    const [sender,receiver,previous,b1,b2]=await Promise.all([tx.get(fromRef),tx.get(toRef),tx.get(op),tx.get(fromRef.collection('blocks').doc(to)),tx.get(toRef.collection('blocks').doc(uid))]);
    if(!sender.exists||sender.get('suspended'))throw new Error('permission-denied');
    if(previous.exists){if(previous.get('to')!==to||previous.get('gift')!==gift)throw new Error('already-exists');return previous.get('result');}
    if(!receiver.exists)throw new Error('not-found');
    if(receiver.get('suspended')||b1.exists||b2.exists)throw new Error('permission-denied');
    const plan=D.giftPlan(uid,to,gift,sender.get('balance'));
    tx.update(fromRef,{balance:plan.balance,sentCoins:FieldValue.increment(plan.price),sentGifts:FieldValue.increment(1)});
    // Received gifts are charm/ranking points, NOT spendable coins or money.
    tx.update(toRef,{receivedCoins:FieldValue.increment(plan.price),receivedGifts:FieldValue.increment(1)});
    const result={balance:plan.balance,price:plan.price};
    tx.create(op,{to,gift,result,createdAt:now()});
    tx.create(fromRef.collection('ledger').doc('gift-'+requestId),{kind:'gift_sent',amount:-plan.price,to,gift,createdAt:now()});
    const receivedId=createHash('sha256').update(uid+':'+requestId).digest('hex');
    tx.create(toRef.collection('ledger').doc(receivedId),{kind:'gift_received',amount:0,charm:plan.price,from:uid,gift,createdAt:now()});
    notice(tx,to,receivedId,`You received a ${gift} gift.`);
    return result;
  });
});
exports.kpLedger=wrap(async uid=>{await active(uid);return rows(await account(uid).collection('ledger').orderBy('createdAt','desc').limit(50).get());});
exports.kpRankings=wrap(async(uid,data)=>{
  await active(uid);const metric=data.metric==='receivedCoins'?'receivedCoins':'sentCoins';
  const s=await db.collection('kpAccounts').where('suspended','==',false).orderBy(metric,'desc').limit(50).get();
  return s.docs.filter(d=>!d.get('suspended')&&d.get(metric)>0).slice(0,50).map(d=>({uid:d.id,name:d.get('name'),score:d.get(metric)}));
});
exports.kpSetBlock=wrap(async(uid,data)=>{
  await active(uid);const target=D.id(data.target);if(uid===target||typeof data.blocked!=='boolean')throw new Error('invalid-argument');
  const ref=account(uid).collection('blocks').doc(target);
  if(data.blocked)await ref.set({createdAt:now()});else await ref.delete();return {blocked:data.blocked};
});
exports.kpBlocks=wrap(async uid=>{await active(uid);return rows(await account(uid).collection('blocks').limit(100).get());});
exports.kpReport=wrap(async(uid,data)=>{
  await active(uid);const target=D.text(data.target),reason=D.text(data.reason,500),kind=data.kind;
  if(!['user','room'].includes(kind))throw new Error('invalid-argument');
  const requestId=D.id(data.requestId),ref=db.collection('kpReports').doc(createHash('sha256').update(uid+':'+requestId).digest('hex'));
  const quota=account(uid).collection('quotas').doc('reports-'+D.dayKey(Date.now()));
  await db.runTransaction(async tx=>{const [old,q]=await Promise.all([tx.get(ref),tx.get(quota)]);if(old.exists){if(old.get('target')!==target||old.get('kind')!==kind||old.get('reason')!==reason)throw new Error('already-exists');return;}
    if((q.get('count')||0)>=10)throw new Error('resource-exhausted');
    tx.set(quota,{count:(q.get('count')||0)+1});tx.create(ref,{reporter:uid,target,kind,reason,status:'open',createdAt:now()});});return {reportId:ref.id};
});
exports.kpInbox=wrap(async uid=>{await active(uid);return rows(await account(uid).collection('inbox').orderBy('createdAt','desc').limit(50).get());});
exports.kpReadNotice=wrap(async(uid,data)=>{await active(uid);await account(uid).collection('inbox').doc(D.id(data.id)).update({read:true});return {ok:true};});
exports.kpNotificationSettings=wrap(async(uid,data)=>{await active(uid);if(typeof data.enabled!=='boolean')throw new Error('invalid-argument');await account(uid).update({notificationsEnabled:data.enabled});return {enabled:data.enabled};});
exports.kpRegisterToken=wrap(async(uid,data)=>{
  await active(uid);const token=D.text(data.token,4096),ref=db.collection('kpDevices').doc(createHash('sha256').update(token).digest('hex'));
  // A device token has exactly one owner; account switches replace ownership.
  await ref.set({uid,token,updatedAt:now()});return {ok:true};
});
exports.kpAdminReports=wrap(async(uid,data,req)=>{D.requireAdmin(req.auth);await active(uid);const status=data.status==='resolved'?'resolved':'open';return rows(await db.collection('kpReports').where('status','==',status).limit(50).get());});
exports.kpResolveReport=wrap(async(uid,data,req)=>{
  D.requireAdmin(req.auth);await active(uid);const ref=db.collection('kpReports').doc(D.id(data.id)),note=D.text(data.note,500);
  const action=data.action;if(!['dismiss','warn','suspend'].includes(action))throw new Error('invalid-argument');
  await db.runTransaction(async tx=>{const r=await tx.get(ref);if(!r.exists)throw new Error('not-found');if(r.get('status')==='resolved')return;
    let target=null;if(action!=='dismiss'){if(r.get('kind')!=='user')throw new Error('invalid-argument');target=account(D.id(r.get('target')));const s=await tx.get(target);if(!s.exists)throw new Error('not-found');if(target.id===uid)throw new Error('invalid-argument');}
    tx.update(ref,{status:'resolved',action,note,reviewer:uid,resolvedAt:now()});
    tx.create(db.collection('kpAudit').doc(),{reportId:ref.id,action,note,reviewer:uid,createdAt:now()});
    notice(tx,r.get('reporter'),'report-'+ref.id,'Your report has been reviewed.');
    if(target){if(action==='suspend')tx.update(target,{suspended:true});notice(tx,target.id,'moderation-'+ref.id,'Account moderation: '+note);}
  });return {ok:true};
});
exports.kpPushNotice=onDocumentCreated({document:'kpAccounts/{uid}/inbox/{noticeId}',region:'us-central1',maxInstances:10},async event=>{
  const uid=event.params.uid,s=await account(uid).get();if(!s.exists||!s.get('notificationsEnabled')||s.get('suspended'))return;
  const devices=await db.collection('kpDevices').where('uid','==',uid).limit(20).get();if(devices.empty)return;
  // Data-only payload: Android checks the currently signed-in UID before display.
  // Inbox is durable even if push delivery fails or permission is denied.
  const result=await getMessaging().sendEachForMulticast({tokens:devices.docs.map(d=>d.get('token')),data:{uid,noticeId:event.params.noticeId,title:'KING Plus',body:'You have a new notification.'},android:{priority:'high',ttl:3600000}});
  await Promise.all(result.responses.map((r,i)=>!r.success&&['messaging/invalid-registration-token','messaging/registration-token-not-registered'].includes(r.error?.code)?devices.docs[i].ref.delete():Promise.resolve()));
});
