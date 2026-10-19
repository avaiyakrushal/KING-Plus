'use strict';
const crypto = require('node:crypto');
const {priceFor} = require('./gift-catalog');
const {applyGiftDebit, applyGiftReception} = require('./recharge-policy');
class GiftError extends Error { constructor(code,message){ super(message); this.code=code; } }
const id = value => typeof value==='string' && /^[A-Za-z0-9_-]{1,128}$/.test(value);
function createGiftService({db,timestamp}) {
  return async ({senderUid,targetUid,giftName,cost,requestId,roomId=null}) => {
    const gift = priceFor(giftName);
    if(!id(senderUid)||!id(targetUid)||senderUid===targetUid||!id(requestId)||
       (roomId!==null&&!id(roomId))||!gift||cost!==gift.total)
      throw new GiftError('invalid-argument','Choose a valid recipient, gift and quantity.');
    const digest=crypto.createHash('sha256').update(senderUid+'\0'+requestId).digest('hex');
    const giftId='gift_'+digest;
    const operation=db.collection('wallet_operations').doc(giftId);
    const sender=db.collection('wallets').doc(senderUid), recipient=db.collection('wallets').doc(targetUid);
    const ledger=db.collection('wallet_ledger').doc(giftId);
    const room=roomId===null?null:db.collection('live_rooms').doc(roomId);
    return db.runTransaction(async tx => {
      const previous=await tx.get(operation);
      if(previous.exists){
        const p=previous.data();
        if(p.targetUid!==targetUid||p.giftName!==gift.giftName||p.quantity!==gift.quantity||
           p.cost!==gift.total||p.roomId!==roomId)
          throw new GiftError('already-exists','This gift request belongs to another purchase.');
        return {ok:true,idempotent:true,balance:p.balance,giftId,message:'Gift already sent.'};
      }
      // All authority reads precede all writes, including on Firestore retries.
      const refs=[sender,recipient,db.collection('public_profiles').doc(senderUid),
        db.collection('public_profiles').doc(targetUid),db.collection('bans').doc(senderUid),
        db.collection('bans').doc(targetUid),db.collection('blocks').doc(senderUid+'_'+targetUid),
        db.collection('blocks').doc(targetUid+'_'+senderUid)];
      if(room)refs.push(room,room.collection('members').doc(senderUid),room.collection('members').doc(targetUid));
      const docs=[];for(const ref of refs)docs.push(await tx.get(ref));
      const [a,b,profile,target,banA,banB,blockA,blockB,roomDoc,memberA,memberB]=docs;
      if(!profile.exists||!target.exists||banA.get('active')===true||banB.get('active')===true||blockA.exists||blockB.exists)
        throw new GiftError('permission-denied','Gift unavailable for these accounts.');
      if(room&&(!roomDoc.exists||roomDoc.get('closed')===true||!memberA.exists||!memberB.exists||
          memberA.get('uid')!==senderUid||memberB.get('uid')!==targetUid))
        throw new GiftError('failed-precondition','Both members must be in the open Party room.');
      let debit,credit;
      try { debit=applyGiftDebit(a.exists?a.data():{},gift.total);credit=applyGiftReception(b.exists?b.data():{},gift.total); }
      catch(error){throw new GiftError('failed-precondition',error.message);}
      const at=timestamp();
      tx.set(sender,{coins:debit.coins,updatedAt:at},{merge:true});
      tx.set(recipient,{giftScore:credit.giftScore,updatedAt:at},{merge:true});
      const record={type:'gift',senderUid,targetUid,giftName:gift.giftName,quantity:gift.quantity,
        cost:gift.total,roomId,giftId,nonRedeemable:true,createdAt:at};
      tx.set(ledger,record);
      tx.set(operation,{...record,balance:debit.coins});
      if(room){
        const actorName=String(profile.get('displayName')||'KING member').slice(0,60);
        const targetName=String(target.get('displayName')||'KING member').slice(0,60);
        const text=actorName+' sent '+gift.giftName+' x'+gift.quantity+' to '+targetName;
        const event={type:'gift',actorUid:senderUid,actorName,targetUid,targetName,text,giftId,
          giftName:gift.giftName,giftQty:gift.quantity,giftUnitCost:gift.unit,
          giftValue:gift.total,freeGift:false,createdAt:at};
        tx.set(room.collection('events').doc(giftId),event);
        tx.set(room.collection('messages').doc(giftId),{...event,senderUid,senderName:actorName});
      }
      return {ok:true,idempotent:false,balance:debit.coins,giftId,message:'Gift sent • '+gift.total+' Diamonds'};
    });
  };
}
module.exports={createGiftService,GiftError};
