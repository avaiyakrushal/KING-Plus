'use strict';
const crypto=require('node:crypto');
const {COLORS,validPremiumNumber,validColor,priceFor,validOperationId}=require('./vip-style-policy');

// No card processing here: spend only existing SERVER-verifiable wallet Diamonds.
// Live charge requires Play Billing / eligible enrolled alternative billing and
// deployment of the verified recharge service. Catalog is admin/server-written.
function createVipStyleService({db, FieldValue, HttpsError, requireAuth, isBanned}) {
  return async function purchase(request) {
    const uid=requireAuth(request);
    if (request.auth.token?.firebase?.sign_in_provider==='anonymous')
      throw new HttpsError('unauthenticated','Sign in to a permanent account.');
    if (await isBanned(uid)) throw new HttpsError('permission-denied','Account restricted.');
    const {product, number, color, requestId}=request.data||{};
    if (!['vip_number','name_color'].includes(product) || !validOperationId(requestId))
      throw new HttpsError('invalid-argument','Invalid purchase request.');
    const selection=product==='vip_number'?number:color;
    if ((product==='vip_number'&&!validPremiumNumber(number)) ||
        (product==='name_color'&&!validColor(color)))
      throw new HttpsError('invalid-argument','Invalid VIP selection.');
    if (product==='vip_number'&&color!==undefined || product==='name_color'&&number!==undefined)
      throw new HttpsError('invalid-argument','Unsupported extra selection.');

    const fingerprint=crypto.createHash('sha256').update(JSON.stringify([uid,product,selection])).digest('hex');
    const operationId=uid+'_'+crypto.createHash('sha256').update(requestId).digest('hex');
    const op=db.collection('wallet_operations').doc(operationId);
    const catalog=db.collection('king_vip_style_catalog').doc('current');
    const grant=db.collection('king_vip_identity_grants').doc(uid);
    const wallet=db.collection('wallets').doc(uid);
    const reservation=product==='vip_number'?db.collection('king_vip_number_registry').doc(number):null;
    const ledger=db.collection('wallet_ledger').doc('vip_'+crypto.createHash('sha256').update(operationId).digest('hex'));
    let result;
    await db.runTransaction(async tx=>{
      // All reads precede writes, to support Firestore transaction semantics.
      const opSnap=await tx.get(op);
      if(opSnap.exists){
        if(opSnap.get('type')!=='vip_style' || opSnap.get('fingerprint')!==fingerprint)
          throw new HttpsError('already-exists','Purchase request ID belongs to a different selection.');
        result={ok:true,idempotent:true,product,selection,balance:opSnap.get('balance')};
        return;
      }
      const [settings,oldGrant,oldWallet,reserved]=await Promise.all([
        tx.get(catalog),tx.get(grant),tx.get(wallet),reservation?tx.get(reservation):Promise.resolve(null)
      ]);
      const cost=priceFor(settings.exists?settings.data():null,product);
      if(cost===null)throw new HttpsError('failed-precondition','VIP shop payments are not enabled. No Diamonds deducted.');
      const current=oldGrant.exists?oldGrant.data():{};
      if(product==='vip_number'){
        if(typeof current.vipNumber==='string')
          throw new HttpsError('already-exists','This account already owns a VIP number.');
        if(reserved&&reserved.exists)
          throw new HttpsError('already-exists','This VIP number is already taken.');
      }else if(Array.isArray(current.ownedColors)&&current.ownedColors.includes(color)){
        throw new HttpsError('already-exists','You already own this name color.');
      }
      const balance=oldWallet.exists?oldWallet.get('coins'):undefined;
      if(!Number.isSafeInteger(balance)||balance<cost)
        throw new HttpsError('failed-precondition','Not enough verified Diamonds.');
      const next=balance-cost;
      const timestamp=FieldValue.serverTimestamp();
      tx.update(wallet,{coins:next,updatedAt:timestamp});
      if(reservation) {
        tx.create(reservation,{uid,number,createdAt:timestamp});
        tx.set(grant,{uid,vipNumber:number,updatedAt:timestamp},{merge:true});
      }else{
        tx.set(grant,{uid,nameColor:color,ownedColors:FieldValue.arrayUnion(color),updatedAt:timestamp},{merge:true});
      }
      tx.set(op,{type:'vip_style',uid,product,selection,fingerprint,cost,balance:next,createdAt:timestamp});
      tx.set(ledger,{type:'vip_style',senderUid:uid,targetUid:uid,product,selection,cost,requestIdHash:crypto.createHash('sha256').update(requestId).digest('hex'),createdAt:timestamp});
      result={ok:true,idempotent:false,product,selection,balance:next};
    });
    return result;
  };
}
module.exports={createVipStyleService};
