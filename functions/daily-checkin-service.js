'use strict';
const XP_PER_CHECKIN=10;
const DAY_RE=/^\d{4}-\d{2}-\d{2}$/;
function dayOrdinal(day){
  if(typeof day!=='string'||!DAY_RE.test(day))return null;
  const ms=Date.parse(day+'T00:00:00.000Z');
  return Number.isFinite(ms)&&new Date(ms).toISOString().slice(0,10)===day?Math.floor(ms/86400000):null;
}
function todayInKolkata(now=new Date()){
  const parts=new Intl.DateTimeFormat('en-CA',{timeZone:'Asia/Kolkata',year:'numeric',month:'2-digit',day:'2-digit'}).formatToParts(now);
  const value={};for(const part of parts)if(part.type!=='literal')value[part.type]=part.value;
  return value.year+'-'+value.month+'-'+value.day;
}
function createDailyCheckinService({db,timestamp,today=todayInKolkata}){
  const userId=uid=>typeof uid==='string'&&uid.length>0&&uid.length<=128;
  const refs=uid=>{
    const root=db.collection('king_daily_checkins').doc(uid);
    return {root,event:root.collection('events').doc(today())};
  };
  async function status({uid}){
    if(!userId(uid))throw Object.assign(new Error('Sign in required.'),{code:'unauthenticated'});
    const day=today();const root=db.collection('king_daily_checkins').doc(uid);
    const snap=await root.get();const data=snap.exists?(snap.data()||{}):{};
    return {ok:true,day,claimedToday:data.lastClaimDay===day,streak:Number(data.streak)||0,
      totalClaims:Number(data.totalClaims)||0,activityXp:Number(data.activityXp)||0,
      todayXp:data.lastClaimDay===day?Number(data.lastClaimXp)||XP_PER_CHECKIN:0};
  }
  async function claim({uid}){
    if(!userId(uid))throw Object.assign(new Error('Sign in required.'),{code:'unauthenticated'});
    const day=today(),ordinal=dayOrdinal(day);
    if(ordinal===null)throw Object.assign(new Error('Check-in day is unavailable.'),{code:'failed-precondition'});
    const root=db.collection('king_daily_checkins').doc(uid),event=root.collection('events').doc(day);
    return db.runTransaction(async tx=>{
      const [account,receipt]=await Promise.all([tx.get(root),tx.get(event)]);
      const old=account.exists?(account.data()||{}):{};
      if(old.lastClaimDay===day||receipt.exists){
        const eventData=receipt.exists?(receipt.data()||{}):{};
        return {ok:true,claimed:false,day,streak:Number(eventData.streak||old.streak)||0,
          totalClaims:Number(old.totalClaims)||0,activityXp:Number(old.activityXp)||0,todayXp:Number(eventData.xp)||0};
      }
      const previous=dayOrdinal(old.lastClaimDay);
      const streak=previous!==null&&ordinal-previous===1?(Number(old.streak)||0)+1:1;
      const totalClaims=(Number(old.totalClaims)||0)+1;
      const activityXp=(Number(old.activityXp)||0)+XP_PER_CHECKIN;
      const at=timestamp();
      const next={uid,lastClaimDay:day,streak,totalClaims,activityXp,lastClaimXp:XP_PER_CHECKIN,updatedAt:at};
      tx.set(root,next,{merge:true});
      tx.set(event,{uid,day,streak,xp:XP_PER_CHECKIN,nonRedeemable:true,claimedAt:at});
      return {ok:true,claimed:true,day,streak,totalClaims,activityXp,todayXp:XP_PER_CHECKIN};
    });
  }
  return {status,claim};
}
module.exports={createDailyCheckinService,todayInKolkata,dayOrdinal,XP_PER_CHECKIN};
