export const RECHARGE_PACKS = Object.freeze({
  diamonds_100: 100,
  diamonds_600: 600,
  diamonds_1300: 1300,
});
export function indianPhone(raw) {
  if(typeof raw!=='string')return null;
  const digits=raw.replace(/[ -]/g,'');
  const match=/^(?:\+?91)?([6-9][0-9]{9})$/.exec(digits);
  return match ? '91'+match[1] : null;
}
export function amountFor(pack, prices) {
  if(!Object.hasOwn(RECHARGE_PACKS,pack)||
     !prices || !Object.hasOwn(prices,pack))return null;
  const paise=prices[pack];
  return Number.isSafeInteger(paise) && paise>=100 && paise<=10000000
    ? {diamonds:RECHARGE_PACKS[pack],amountPaise:paise} : null;
}
export function vipFromRecharge(total) {
  if(!Number.isSafeInteger(total)||total<0)return 0;
  const thresholds=[0,500,1500,3500,7000,13000,22000,36000,
    56000,85000,125000,180000,250000];
  let level=0;
  for(let i=1;i<thresholds.length;i++)if(total>=thresholds[i])level=i;
  return level;
}
export function isCaptureForOrder(payment, order) {
  return Boolean(payment && order && payment.status==='captured' &&
    payment.order_id===order.order_id && payment.currency==='INR' &&
    payment.amount===order.amount_paise &&
    typeof payment.id==='string' && /^pay_[a-zA-Z0-9]+$/.test(payment.id));
}
export async function hmacHex(secret, text){
  const key=await crypto.subtle.importKey('raw',new TextEncoder().encode(secret),
    {name:'HMAC',hash:'SHA-256'},false,['sign']);
  const bytes=new Uint8Array(await crypto.subtle.sign('HMAC',key,
    new TextEncoder().encode(text)));
  return Array.from(bytes,b=>b.toString(16).padStart(2,'0')).join('');
}
export function secureHexEquals(a,b){
  if(typeof a!=='string'||typeof b!=='string')return false;
  if(a.length!==b.length||!/^[a-f0-9]+$/i.test(a)||!/^[a-f0-9]+$/i.test(b))return false;
  let diff=0;
  for(let i=0;i<a.length;i++)diff|=a.charCodeAt(i)^b.charCodeAt(i);
  return diff===0;
}
