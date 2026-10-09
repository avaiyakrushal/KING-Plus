import {indianPhone,amountFor,vipFromRecharge,isCaptureForOrder,hmacHex,secureHexEquals} from './policy.mjs';

// Cloudflare Worker Free + D1. Every SMS is sent by a configured paid provider;
// every Diamond is credited by a signed, captured Razorpay event, not Android.
const json=(value,status=200)=>new Response(JSON.stringify(value),{status,headers:{
  'content-type':'application/json','cache-control':'no-store'}});
const err=(message,status=400)=>json({ok:false,error:message},status);
const now=()=>Math.floor(Date.now()/1000);
const present=(env,...keys)=>keys.every(k=>typeof env[k]==='string'&&env[k].trim());
const enc=new TextEncoder();
async function body(req){
  if(Number(req.headers.get('content-length')||0)>4096)return null;
  try{const raw=await req.text();if(raw.length>4096)return null;
    const value=JSON.parse(raw);return value&&typeof value==='object'&&!Array.isArray(value)?value:null;
  }catch(_){return null;}
}
async function turnstile(env,token,ip){
  if(!present(env,'TURNSTILE_SECRET')||typeof token!=='string'||token.length<5)return false;
  const r=await fetch('https://challenges.cloudflare.com/turnstile/v0/siteverify',{
    method:'POST',body:new URLSearchParams({
      secret:env.TURNSTILE_SECRET,response:token,remoteip:ip||''
    })
  });
  return r.ok&&(await r.json()).success===true;
}
function b64(bytes){return btoa(String.fromCharCode(...bytes)).replace(/\+/g,'-').replace(/\//g,'_').replace(/=/g,'');}
function base64dec(s){return Uint8Array.from(atob(s.replace(/-/g,'+').replace(/_/g,'/').padEnd(Math.ceil(s.length/4)*4,'=')),x=>x.charCodeAt(0));}
async function customToken(env,phone){
  if(!present(env,'PHONE_UID_SECRET','FIREBASE_SA_EMAIL','FIREBASE_PRIVATE_KEY'))
    throw Error('Firebase custom auth not configured');
  const hex=await hmacHex(env.PHONE_UID_SECRET,phone);
  const uid='phone_'+hex.slice(0,40);
  const t=now(),mail=env.FIREBASE_SA_EMAIL;
  const jwtHead={alg:'RS256',typ:'JWT'};
  const claims={iss:mail,sub:mail,
    aud:'https://identitytoolkit.googleapis.com/google.identity.identitytoolkit.v1.IdentityToolkit',
    uid,iat:t,exp:t+3600};
  const pem=env.FIREBASE_PRIVATE_KEY.replace(/\\n/g,'\n');
  const der=Uint8Array.from(atob(pem.replace(/-----BEGIN PRIVATE KEY-----|-----END PRIVATE KEY-----|\s/g,'')),c=>c.charCodeAt(0));
  const key=await crypto.subtle.importKey('pkcs8',der,
    {name:'RSASSA-PKCS1-v1_5',hash:'SHA-256'},false,['sign']);
  const signed=b64(enc.encode(JSON.stringify(jwtHead)))+'.'+b64(enc.encode(JSON.stringify(claims)));
  const signature=await crypto.subtle.sign('RSASSA-PKCS1-v1_5',key,enc.encode(signed));
  return {uid,customToken:signed+'.'+b64(new Uint8Array(signature))};
}
async function smsSend(env,phone){
  const qs=new URLSearchParams({template_id:env.MSG91_OTP_TEMPLATE_ID,
    mobile:phone,otp_expiry:'5',otp_length:'6'});
  const r=await fetch('https://control.msg91.com/api/v5/otp?'+qs,{
    method:'POST',headers:{authkey:env.MSG91_AUTHKEY,accept:'application/json'}
  });
  return r.ok&&(await r.json()).type==='success';
}
async function smsVerify(env,phone,otp){
  const qs=new URLSearchParams({mobile:phone,otp});
  const r=await fetch('https://control.msg91.com/api/v5/otp/verify?'+qs,{
    headers:{authkey:env.MSG91_AUTHKEY,accept:'application/json'}
  });
  if(!r.ok)return false;
  const data=await r.json();
  return data.type==='success'||data.message==='OTP verified success';
}
async function otpRequest(req,env){
  if(!env.DB||!present(env,'PHONE_UID_SECRET','FIREBASE_SA_EMAIL','FIREBASE_PRIVATE_KEY',
    'MSG91_AUTHKEY','MSG91_OTP_TEMPLATE_ID','TURNSTILE_SECRET'))
    return err('OTP provider is not configured',503);
  const data=await body(req),phone=indianPhone(data?.phone);
  if(!phone)return err('Enter a valid Indian mobile number');
  const ip=req.headers.get('CF-Connecting-IP')||'unknown';
  if(!(await turnstile(env,data?.turnstileToken,ip)))
    return err('Complete the anti-spam verification',403);
  const time=now(),ipKey=await hmacHex(env.PHONE_UID_SECRET,'ip:'+ip);
  const limit=await env.DB.prepare([
    'INSERT INTO otp_ip_windows(ip_hash,window_start,sent_count) VALUES(?,?,1)',
    'ON CONFLICT(ip_hash) DO UPDATE SET',
    'sent_count=CASE WHEN otp_ip_windows.window_start<? THEN 1 ELSE otp_ip_windows.sent_count+1 END,',
    'window_start=CASE WHEN otp_ip_windows.window_start<? THEN ? ELSE otp_ip_windows.window_start END',
    'WHERE otp_ip_windows.window_start<? OR otp_ip_windows.sent_count<5'
  ].join(' ')).bind(ipKey,time,time-3600,time-3600,time,time-3600).run();
  if(limit.meta.changes!==1)return err('Too many SMS requests',429);
  const cooldown=await env.DB.prepare([
    'INSERT INTO otp_requests(phone,requested_at,expires_at,attempts,used)',
    'VALUES(?,?,?,0,0) ON CONFLICT(phone) DO UPDATE SET',
    'requested_at=excluded.requested_at,expires_at=excluded.expires_at,attempts=0,used=0',
    'WHERE otp_requests.requested_at<=?'
  ].join(' ')).bind(phone,time,time+300,time-60).run();
  if(cooldown.meta.changes!==1)return err('Wait 60 seconds before resending',429);
  let delivered=false;
  try{delivered=await smsSend(env,phone)}catch(_){}
  if(!delivered)return err('SMS delivery unavailable; no login granted',503);
  return json({ok:true,message:'OTP sent by SMS, valid for five minutes'});
}
async function otpVerify(req,env){
  if(!env.DB||!present(env,'MSG91_AUTHKEY','PHONE_UID_SECRET',
    'FIREBASE_SA_EMAIL','FIREBASE_PRIVATE_KEY'))return err('OTP service unavailable',503);
  const data=await body(req),phone=indianPhone(data?.phone),otp=data?.otp;
  if(!phone||typeof otp!=='string'||!/^[0-9]{6}$/.test(otp))
    return err('Phone number and six-digit OTP required');
  const time=now();
  const claim=await env.DB.prepare(
    'UPDATE otp_requests SET attempts=attempts+1 WHERE phone=? AND used=0 AND expires_at>? AND attempts<5'
  ).bind(phone,time).run();
  if(claim.meta.changes!==1)return err('OTP expired or too many attempts',429);
  let verified=false;
  try{verified=await smsVerify(env,phone,otp)}catch(_){}
  if(!verified)return err('Incorrect OTP',401);
  const consume=await env.DB.prepare(
    'UPDATE otp_requests SET used=1 WHERE phone=? AND used=0 AND expires_at>?'
  ).bind(phone,time).run();
  if(consume.meta.changes!==1)return err('OTP already used or expired',409);
  try{return json({ok:true,...await customToken(env,phone)})}
  catch(_){return err('Firebase sign-in temporarily unavailable',503)}
}

// Authenticate requests with the *actual Firebase ID token*, not a client UID.
let keysCache=null,keysUntil=0;
async function firebaseUid(req){
  const bearer=req.headers.get('Authorization')||'';
  if(!bearer.startsWith('Bearer '))return null;
  const raw=bearer.slice(7),parts=raw.split('.');
  if(parts.length!==3||raw.length>8000)return null;
  let h,p;
  try{h=JSON.parse(new TextDecoder().decode(base64dec(parts[0])));
    p=JSON.parse(new TextDecoder().decode(base64dec(parts[1])))}catch(_){return null}
  const time=now();
  if(h.alg!=='RS256'||!h.kid||p.aud!=='king-plus-2f365'||
    p.iss!=='https://securetoken.google.com/king-plus-2f365'||
    typeof p.sub!=='string'||!p.sub||p.sub.length>128||
    !Number.isInteger(p.exp)||p.exp<=time||
    !Number.isInteger(p.iat)||p.iat>time+60||
    !Number.isInteger(p.auth_time)||p.auth_time>time+60)return null;
  if(!keysCache||keysUntil<time){
    const r=await fetch('https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com');
    if(!r.ok)return null;
    keysCache=await r.json();keysUntil=time+3600;
  }
  const jwk=keysCache.keys?.find(k=>k.kid===h.kid);
  if(!jwk)return null;
  try{
    const key=await crypto.subtle.importKey('jwk',jwk,
      {name:'RSASSA-PKCS1-v1_5',hash:'SHA-256'},false,['verify']);
    return await crypto.subtle.verify('RSASSA-PKCS1-v1_5',key,
      base64dec(parts[2]),enc.encode(parts[0]+'.'+parts[1]))?p.sub:null;
  }catch(_){return null}
}
async function wallet(req,env){
  if(!env.DB)return err('Database unavailable',503);
  const uid=await firebaseUid(req);if(!uid)return err('Firebase sign-in required',401);
  const x=await env.DB.prepare('SELECT diamonds,recharge_total FROM wallets WHERE firebase_uid=?')
    .bind(uid).first();
  return json({ok:true,diamonds:x?.diamonds??0,rechargeTotal:x?.recharge_total??0,
    vipLevel:vipFromRecharge(x?.recharge_total??0)});
}
async function newOrder(req,env){
  if(!env.DB)return err('Database unavailable',503);
  const uid=await firebaseUid(req);if(!uid)return err('Sign in first',401);
  if(env.PAYMENTS_ENABLED!=='true'||
     !present(env,'RAZORPAY_KEY_ID','RAZORPAY_KEY_SECRET','RECHARGE_PRICES_PAISE_JSON'))
    return err('Merchant payments are not activated',503);
  const data=await body(req);
  let configuredPrices;
  try{configuredPrices=JSON.parse(env.RECHARGE_PRICES_PAISE_JSON)}catch(_){}
  const sku=typeof data?.sku==='string'?data.sku:'';
  const item=amountFor(sku,configuredPrices);
  if(!item)return err('Unknown Recharge pack');
  const auth=btoa(env.RAZORPAY_KEY_ID+':'+env.RAZORPAY_KEY_SECRET);
  const payment=await fetch('https://api.razorpay.com/v1/orders',{
    method:'POST',
    headers:{Authorization:'Basic '+auth,'content-type':'application/json'},
    body:JSON.stringify({
      amount:item.amountPaise,currency:'INR',
      receipt:'king_'+crypto.randomUUID().replace(/-/g,'').slice(0,32)
    })
  });
  if(!payment.ok)return err('Razorpay order unavailable',502);
  const result=await payment.json();
  if(!/^order_[A-Za-z0-9]+$/.test(result.id||'')||
     result.currency!=='INR'||result.amount!==item.amountPaise)
    return err('Invalid payment order',502);
  await env.DB.prepare([
    'INSERT INTO recharge_orders(order_id,firebase_uid,sku,diamonds,amount_paise,currency,status,created_at)',
    "VALUES(?,?,?,?,?,'INR','created',?)"
  ].join(' ')).bind(result.id,uid,sku,item.diamonds,item.amountPaise,now()).run();
  return json({ok:true,orderId:result.id,keyId:env.RAZORPAY_KEY_ID,
    amountPaise:item.amountPaise,diamonds:item.diamonds,currency:'INR'});
}
async function paymentWebhook(req,env){
  if(!env.DB||!present(env,'RAZORPAY_WEBHOOK_SECRET'))
    return err('Merchant webhook not configured',503);
  const raw=await req.text();
  if(raw.length>128000)return err('Webhook oversized',413);
  const signature=req.headers.get('X-Razorpay-Signature');
  if(!secureHexEquals(signature,await hmacHex(env.RAZORPAY_WEBHOOK_SECRET,raw)))
    return err('Invalid webhook signature',401);
  let event;try{event=JSON.parse(raw)}catch(_){return err('Bad webhook')}
  if(event.event!=='payment.captured')return json({ok:true,ignored:true});
  const captured=event.payload?.payment?.entity;
  if(!captured?.order_id)return err('Missing order',400);
  const order=await env.DB.prepare('SELECT * FROM recharge_orders WHERE order_id=?')
    .bind(captured.order_id).first();
  if(!order)return err('Unknown order',404);
  if(!isCaptureForOrder(captured,order))return err('Payment mismatch',400);
  const update=await env.DB.prepare([
    "UPDATE recharge_orders SET status='captured',payment_id=?,captured_at=?",
    "WHERE order_id=? AND status='created' AND amount_paise=? AND currency='INR'"
  ].join(' ')).bind(captured.id,now(),order.order_id,captured.amount).run();
  // The D1 trigger credits the wallet and purchase ledger atomically ONCE.
  return json({ok:true,credited:update.meta.changes===1});
}
export default {async fetch(req,env){
  try{
    const path=new URL(req.url).pathname;
    if(req.method==='GET'&&path==='/health')
      return json({ok:true,service:'kingplus-lowcost',livePayments:false});
    if(req.method==='POST'&&path==='/otp/request')return await otpRequest(req,env);
    if(req.method==='POST'&&path==='/otp/verify')return await otpVerify(req,env);
    if(req.method==='GET'&&path==='/wallet')return await wallet(req,env);
    if(req.method==='POST'&&path==='/recharge/order')return await newOrder(req,env);
    if(req.method==='POST'&&path==='/razorpay/webhook')return await paymentWebhook(req,env);
    return err('Not found',404);
  }catch(failure){
    console.error('Cloudflare request failed',failure?.name||'Error');
    return err('Secure backend temporarily unavailable',503);
  }
}};
