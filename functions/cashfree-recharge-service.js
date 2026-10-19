'use strict';
// Merchant-link recharge: only authenticated provider API checks can mint Diamonds.
// Config and gateway credentials MUST be server-side. No customer-supplied price or quantity.
const {randomBytes, createHash} = require('node:crypto');
const {purchaseAmount, applyVerifiedRecharge} = require('./recharge-policy');
const ALLOWED_SKUS = Object.freeze([
  'king_coins_100','king_coins_600','king_coins_1300',
  'king_upi_8800','king_upi_17800','king_upi_45000',
  'king_upi_90000','king_upi_180000','king_upi_450000'
]);
const fail = (code, message) => {const e=new Error(message);e.code=code;throw e;};
const sha = v => createHash('sha256').update(v).digest('hex');
const validUid = uid => typeof uid==='string' && uid.length>=2 && uid.length<=128 && !uid.includes('/');
const rupeesToPaise = value => Number.isFinite(value) && value>=0 && Math.abs(value*100-Math.round(value*100))<1e-6 ? Math.round(value*100): null;
function validatePack(id, data) {
  const diamonds=purchaseAmount(id);
  if(!ALLOWED_SKUS.includes(id)||!diamonds||!data||data.active!==true||data.currency!=='INR'||
    !Number.isSafeInteger(data.amountPaise)||data.amountPaise<100||data.amountPaise>10000000) return null;
  return {productId:id,diamonds,amountPaise:data.amountPaise,currency:'INR'};
}
function isProviderPaid(link, intent){
  return !!link && link.link_id===intent.linkId && link.link_status==='PAID' &&
    link.link_partial_payments===false && link.link_currency==='INR' &&
    rupeesToPaise(Number(link.link_amount))===intent.amountPaise &&
    rupeesToPaise(Number(link.link_amount_paid))===intent.amountPaise;
}
function safeLink(url,mode){
  let parsed;try{parsed=new URL(url);}catch(_){return false;}
  const expected=mode==='production'?'payments.cashfree.com':'payments-test.cashfree.com';
  return parsed.protocol==='https:'&&parsed.hostname===expected&&parsed.pathname.startsWith('/links/')&&
    !parsed.username&&!parsed.password;
}
// Only resume the exact merchant-created link, never a URL supplied by Android.
function isProviderLink(link,intent,mode){
  return !!link && link.link_id===intent.linkId &&
    link.link_currency==='INR' && link.link_partial_payments===false &&
    rupeesToPaise(Number(link.link_amount))===intent.amountPaise &&
    safeLink(link.link_url,mode);
}
function eligibleForRecovery(row){
  return !!row && ['pending','creating','verify-needed','gateway-error'].includes(row.status);
}
// A link can be marked terminal only after checking the exact merchant order,
// amount and an explicitly reported zero payment. Never infer failure from a timeout.
function verifiedUnpaidTerminal(link,intent){
  return !!link && link.link_id===intent.linkId &&
    ['EXPIRED','CANCELLED'].includes(link.link_status) &&
    link.link_currency==='INR' && link.link_partial_payments===false &&
    rupeesToPaise(Number(link.link_amount))===intent.amountPaise &&
    link.link_amount_paid!=null && rupeesToPaise(Number(link.link_amount_paid))===0;
}
function createCashfreeRechargeService({db, provider, stamp, isBanned, enabled, mode}){
  const catalogRef=id=>db.collection('cashfree_recharge_catalog').doc(id);
  const ordersRef=id=>db.collection('cashfree_recharge_orders').doc(id);
  // Stable server-only slot: one unresolved gateway order per user+SKU.
  // It serializes double taps and network retries across different devices.
  const activeRef=(uid,id)=>db.collection('cashfree_recharge_active').doc(sha(uid+'\u0000'+id));
  async function markTerminal({ref,uid,row,link}){
    if(!verifiedUnpaidTerminal(link,row))return false;
    return await db.runTransaction(async tx=>{
      const latest=await tx.get(ref);
      if(!latest.exists||latest.get('uid')!==uid)fail('permission-denied','Order owner changed.');
      const current=latest.data();
      if(current.linkId!==row.linkId||current.amountPaise!==row.amountPaise||
         current.productId!==row.productId||current.diamonds!==row.diamonds)
        fail('failed-precondition','Order changed while checking its status.');
      // A concurrent successful confirm may have credited this order already.
      // Never tell the client that a paid order expired or cancelled.
      if(!eligibleForRecovery(current))return false;
      tx.update(ref,{status:link.link_status==='EXPIRED'?'expired':'cancelled',updatedAt:stamp()});
      return true;
    });
  }
  async function assertUser(uid){
    if(!validUid(uid))fail('unauthenticated','Sign in to a permanent account.');
    if(await isBanned(uid))fail('permission-denied','Account restricted.');
  }
  async function loadPack(id){
    if(!ALLOWED_SKUS.includes(id))fail('invalid-argument','Unknown recharge pack.');
    const snap=await catalogRef(id).get();
    const pack=snap.exists?validatePack(id,snap.data()):null;
    if(!pack)fail('failed-precondition','Recharge price not approved by merchant.');
    return pack;
  }
  const disabled =()=>fail('failed-precondition','UPI payments have not been enabled by the verified merchant. No payment was started.');
  return {
    // Order history survives reinstall and lets a customer recover an uncertain gateway ACK.
    // Never leak another customer's order and never trust order IDs supplied by Android.
    async history({uid}){
      await assertUser(uid);
      const snap=await db.collection('cashfree_recharge_orders').where('uid','==',uid).orderBy('createdAt','desc').limit(50).get();
      const rows=snap.docs.map(doc=>{
        const row=doc.data();
        const stampValue=row.createdAt;
        const createdAt=typeof stampValue?.toMillis==='function'?stampValue.toMillis():
          (Number.isSafeInteger(stampValue)?stampValue:0);
        return {orderId:doc.id,productId:row.productId,status:row.status,
          amountPaise:row.amountPaise,diamonds:row.diamonds,createdAt};
      }).filter(row=>/^king_[a-f0-9]{32}$/.test(row.orderId) &&
          ALLOWED_SKUS.includes(row.productId) && Number.isSafeInteger(row.amountPaise) &&
          Number.isSafeInteger(row.diamonds));
      rows.sort((a,b)=>b.createdAt-a.createdAt || b.orderId.localeCompare(a.orderId));
      return {orders:rows.slice(0,20),enabled:!!enabled};
    },
    async catalog({uid}){
      await assertUser(uid);
      if(!enabled)return {enabled:false,packs:[],message:'UPI merchant setup pending. No payment will be requested.'};
      const packs=await Promise.all(ALLOWED_SKUS.map(async id=>{
        const snap=await catalogRef(id).get();return snap.exists?validatePack(id,snap.data()):null;
      }));
      return {enabled:true,packs:packs.filter(Boolean)};
    },
    async create({uid,productId,phone}){
      if(!enabled)return disabled();
      await assertUser(uid);
      if(typeof phone!=='string'||!/^[6-9][0-9]{9}$/.test(phone))fail('invalid-argument','Enter a valid 10-digit India mobile number.');
      const pack=await loadPack(productId);
      const id='king_'+randomBytes(16).toString('hex');
      const ref=ordersRef(id);
      const payload={uid,productId,diamonds:pack.diamonds,amountPaise:pack.amountPaise,
        currency:'INR',linkId:id,status:'creating',createdAt:stamp()};
      await db.runTransaction(async tx=>{
        const lock=activeRef(uid,productId);
        const slot=await tx.get(lock);
        if(slot.exists){
          if(slot.get('uid')!==uid||slot.get('productId')!==productId)
            fail('failed-precondition','Existing recharge session cannot be verified.');
          const oldId=slot.get('orderId');
          if(typeof oldId!=='string'||!/^king_[a-f0-9]{32}$/.test(oldId))
            fail('failed-precondition','Previous recharge order is invalid.');
          const old=await tx.get(ordersRef(oldId));
          if(!old.exists||old.get('uid')!==uid||old.get('productId')!==productId)
            fail('failed-precondition','Previous recharge needs manual review.');
          if(!['credited','expired','cancelled'].includes(old.get('status')))
            fail('failed-precondition','Previous recharge is still pending. Use Recharge History to check or continue it.');
        }
        tx.create(ref,payload);
        tx.set(lock,{uid,productId,orderId:id,updatedAt:stamp()});
      });
      let link;
      const matchesLink=value=>isProviderLink(value,{linkId:id,amountPaise:pack.amountPaise},mode);
      try {link=await provider.createLink({id,pack,phone});}
      catch(_){
        // A timeout can happen AFTER Cashfree has created and charged the link.
        // Retry the same ID by asking Cashfree, never create a second charge silently.
        try {link=await provider.getLink(id);} catch(_err){link=null;}
      }
      // A create response must not send the user into a cancelled, paid, or
      // unknown-state checkout. Query the existing link ID once if necessary.
      if(link?.link_status!=='ACTIVE'){
        try{link=await provider.getLink(id);}catch(_){/* preserve existing order */}
      }
      if(!matchesLink(link)||link.link_status!=='ACTIVE'){
        // Crucial: preserve the order for later recovery instead of marking it
        // gateway-error (which previously prevented credit for a paid link).
        await ref.update({status:'verify-needed',updatedAt:stamp()}).catch(()=>null);
        fail('unavailable','Gateway response unclear. Existing order saved; recover it from payment history.');
      }
      await ref.update({status:'pending',linkUrl:link.link_url,updatedAt:stamp()});
      return {ok:true,orderId:id,checkoutUrl:link.link_url,amountPaise:pack.amountPaise,diamonds:pack.diamonds};
    },
    // Existing authenticated customer can reopen exactly the same gateway link after
    // app reinstall, browser close or a timeout; no second payment link is created.
    async resume({uid,orderId}){
      if(!enabled)return disabled();
      await assertUser(uid);
      if(typeof orderId!=='string'||!/^king_[a-f0-9]{32}$/.test(orderId))
        fail('invalid-argument','Invalid recharge order.');
      const ref=ordersRef(orderId),snap=await ref.get();
      if(!snap.exists||snap.get('uid')!==uid)
        fail('permission-denied','Order does not belong to this account.');
      const row=snap.data();
      if(row.status==='credited')return {ok:true,status:'credited',message:'Diamonds were already credited.'};
      if(!eligibleForRecovery(row))return {ok:false,status:'unavailable',message:'Order is not payable.'};
      if(row.linkId!==orderId||!ALLOWED_SKUS.includes(row.productId)||
        row.diamonds!==purchaseAmount(row.productId)||
        !Number.isSafeInteger(row.amountPaise)||row.amountPaise<100||row.amountPaise>10000000||
        row.currency!=='INR')
        fail('failed-precondition','Order price or identity was changed.');
      let link;
      try{link=await provider.getLink(orderId);}
      catch(_){fail('unavailable','Cashfree temporarily unavailable. Saved order was not changed.');}
      // A paid link may no longer expose a checkout URL. Paid status is verified
      // separately from whether an ACTIVE link is safe to reopen in a browser.
      if(isProviderPaid(link,row))
        return {ok:true,status:'paid',message:'Payment received. Check Payment to credit Diamonds.'};
      if(await markTerminal({ref,uid,row,link}))
        return {ok:false,status:link.link_status==='EXPIRED'?'expired':'cancelled',message:'Previous payment link has closed. No Diamonds were credited. You can start a new order.'};
      // A receipt confirmation can commit while a previous provider response
      // is in flight; surface credited instead of reporting an expired order.
      const current=await ref.get();
      if(current.exists&&current.get('uid')===uid&&current.get('status')==='credited')
        return {ok:true,status:'credited',message:'Diamonds were already credited.'};
      if(!isProviderLink(link,row,mode))
        fail('failed-precondition','Gateway link could not be verified. No payment opened.');
      if(link.link_status!=='ACTIVE')
        return {ok:false,status:'unavailable',message:'This payment link is no longer available.'};
      if(row.status!=='pending')await ref.update({status:'pending',linkUrl:link.link_url,updatedAt:stamp()});
      return {ok:true,status:'pending',orderId,checkoutUrl:link.link_url,
        amountPaise:row.amountPaise,diamonds:row.diamonds};
    },
    async confirm({uid,orderId}){
      if(!enabled)return disabled();
      await assertUser(uid);
      if(typeof orderId!=='string'||!/^king_[a-f0-9]{32}$/.test(orderId))fail('invalid-argument','Invalid order.');
      const intentRef=ordersRef(orderId);
      const doc=await intentRef.get();
      if(!doc.exists||doc.get('uid')!==uid)fail('permission-denied','This order does not belong to your account.');
      const intent=doc.data();
      if(intent.status==='credited')return {ok:true,alreadyCredited:true,message:'Previously verified recharge.'};
      const recoverable=['pending','creating','verify-needed','gateway-error'].includes(intent.status);
      if(!recoverable)return {ok:false,pending:false,message:'Recharge unavailable for verification.'};
      let link;try{link=await provider.getLink(orderId);}catch(_){fail('unavailable','Gateway confirmation unavailable. No Diamonds credited.');}
      if(!isProviderPaid(link,intent)){
        if(await markTerminal({ref:intentRef,uid,row:intent,link}))
          return {ok:false,pending:false,status:link.link_status==='EXPIRED'?'expired':'cancelled',message:'Payment link closed without verified payment. You may start another order.'};
        const current=await intentRef.get();
        if(current.exists&&current.get('uid')===uid&&current.get('status')==='credited')
          return {ok:true,alreadyCredited:true,message:'Recharge previously credited.'};
        return {ok:false,pending:true,message:'Payment not confirmed by gateway. No Diamonds credited.'};
      }
      const result=await db.runTransaction(async tx=>{
        const [latest,receipt,wallet,ban]=await Promise.all([
          tx.get(intentRef),tx.get(db.collection('cashfree_recharge_receipts').doc(sha(orderId))),
          tx.get(db.collection('wallets').doc(uid)),tx.get(db.collection('bans').doc(uid))
        ]);
        if(ban.exists&&ban.get('active')===true)fail('permission-denied','Account restricted.');
        if(!latest.exists||latest.get('uid')!==uid)fail('permission-denied','Recharge ownership changed.');
        const row=latest.data();
        if(row.linkId!==orderId||row.productId!==intent.productId||row.amountPaise!==intent.amountPaise||
          row.diamonds!==intent.diamonds||row.diamonds!==purchaseAmount(row.productId)||row.uid!==uid)
          fail('failed-precondition','Recharge details changed.');
        if(receipt.exists){
          if(receipt.get('uid')!==uid||receipt.get('orderId')!==orderId)fail('permission-denied','Receipt ownership mismatch.');
          return {alreadyCredited:true};
        }
        if(!['pending','creating','verify-needed','gateway-error'].includes(row.status))
          fail('failed-precondition','Order not eligible for credit.');
        let next;try{next=applyVerifiedRecharge(wallet.exists?wallet.data():{},row.productId);}
        catch(_){fail('failed-precondition','Wallet unable to accept Diamonds. Contact support.');}
        tx.set(db.collection('wallets').doc(uid),{
          coins:next.coins,vipPoints:next.vipPoints,vipLevel:next.vipLevel,
          rechargeDiamondsTotal:next.rechargeDiamondsTotal,updatedAt:stamp()
        },{merge:true});
        tx.set(db.collection('cashfree_recharge_receipts').doc(sha(orderId)),{
          uid,orderId,productId:row.productId,amountPaise:row.amountPaise,
          coins:next.creditedDiamonds,status:'credited',verifiedAt:stamp()
        });
        tx.set(db.collection('wallet_ledger').doc('cashfree_'+sha(orderId)),{
          type:'cashfree_paid_recharge',targetUid:uid,orderId,
          productId:row.productId,amountPaise:row.amountPaise,
          coins:next.creditedDiamonds,createdAt:stamp()
        });
        tx.update(intentRef,{status:'credited',creditedAt:stamp()});
        return {alreadyCredited:false,diamonds:next.creditedDiamonds,balance:next.coins};
      });
      return {ok:true,...result,message:result.alreadyCredited?'Recharge previously credited.':'Verified Diamonds added to wallet.'};
    }
  };
}
module.exports={createCashfreeRechargeService,validatePack,isProviderPaid,isProviderLink,verifiedUnpaidTerminal,safeLink,rupeesToPaise,ALLOWED_SKUS};
