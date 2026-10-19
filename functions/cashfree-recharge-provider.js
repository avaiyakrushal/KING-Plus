'use strict';
// Node.js 22 fetch; credentials only in Firebase Secret Manager, never Android or Firestore.
const BASES=Object.freeze({sandbox:'https://sandbox.cashfree.com/pg',production:'https://api.cashfree.com/pg'});
function createProvider({clientId,clientSecret,mode,fetchImpl=fetch}){
  if(!['sandbox','production'].includes(mode)||!clientId||!clientSecret)throw Error('Gateway merchant credentials not configured.');
  const base=BASES[mode];
  async function call(path,method,body,id){
    const res=await fetchImpl(base+path,{
      method, headers:{'x-client-id':clientId,'x-client-secret':clientSecret,
        'x-api-version':'2025-01-01','Content-Type':'application/json',
        ...(id?{'x-idempotency-key':id}:{})},
      ...(body?{body:JSON.stringify(body)}:{}),signal:AbortSignal.timeout(15000)
    });
    if(!res.ok)throw Error('Cashfree API response: '+res.status);
    return await res.json();
  }
  return {
    async createLink({id,pack,phone}){
      return call('/links','POST',{
        link_id:id,link_amount:pack.amountPaise/100,link_currency:'INR',
        link_purpose:'KING Plus '+pack.diamonds+' Diamonds',link_partial_payments:false,
        customer_details:{customer_phone:phone},link_notify:{send_sms:false,send_email:false},
        link_auto_reminders:false,link_meta:{upi_intent:true}
      },id);
    },
    getLink:id=>call('/links/'+encodeURIComponent(id),'GET')
  };
}
module.exports={createProvider};
