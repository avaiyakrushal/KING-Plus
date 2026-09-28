'use strict';
const catalog = Object.freeze({rose:1, heart:5, candy:10, cake:50, diamond:100, car:500, crown:999, fireworks:1999});
function text(value, max=128) {
  if(typeof value !== 'string' || !value.trim() || value.length > max) throw new Error('invalid-argument');
  return value.trim();
}
function id(value) { const v=text(value); if(/[\/\x00-\x1f]/.test(v)) throw new Error('invalid-argument'); return v; }
function giftPlan(sender, recipient, key, balance) {
  if(sender===recipient) throw new Error('invalid-argument');
  if(!Object.hasOwn(catalog,key)) throw new Error('invalid-argument');
  if(!Number.isSafeInteger(balance) || balance<0) throw new Error('failed-precondition');
  const price=catalog[key];
  if(balance<price) throw new Error('failed-precondition');
  return {price,balance:balance-price};
}
function requireAuth(auth) {
  if(!auth || !auth.uid || auth.token?.firebase?.sign_in_provider === 'anonymous') throw new Error('unauthenticated');
  return id(auth.uid);
}
function requireAdmin(auth) {requireAuth(auth); if(auth.token?.admin !== true) throw new Error('permission-denied');}
function dayKey(now) {return new Date(now).toISOString().slice(0,10);}
module.exports={catalog,text,id,giftPlan,requireAuth,requireAdmin,dayKey};
