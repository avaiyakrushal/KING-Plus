'use strict';

// A premium ID is a different, labelled namespace from the regular KING ID.
const COLORS = Object.freeze({
  gold: '#FFD54F', royal_blue: '#448AFF', rose: '#F06292',
  emerald: '#40C987', violet: '#B388FF', cyan: '#4DD0E1'
});
function validPremiumNumber(v){return typeof v === 'string' && /^[1-9][0-9]{5}$/.test(v);}
function validColor(v){return typeof v === 'string' && Object.prototype.hasOwnProperty.call(COLORS,v);}
function priceFor(catalog,product){
  if (!catalog || catalog.enabled !== true || catalog.paymentMode !== 'verified_diamonds') return null;
  const field = product === 'vip_number' ? 'vipNumberDiamonds' : product === 'name_color' ? 'nameColorDiamonds' : null;
  if (!field) return null;
  const value=catalog[field];
  return Number.isSafeInteger(value) && value > 0 && value <= 10000000 ? value : null;
}
function validOperationId(id){return typeof id==='string' && /^[a-zA-Z0-9_-]{8,80}$/.test(id);}
module.exports={COLORS,validPremiumNumber,validColor,priceFor,validOperationId};
