'use strict';
// The merchant must deliberately turn payments ON; secrets alone are never consent.
// Google Play distributed builds require eligible merchant's billing enrollment.
function paymentEnabled({mode,hasId,hasSecret,enabled,billingApproved}){
  return (mode==='sandbox'||mode==='production') && hasId===true && hasSecret===true &&
    enabled==='true' && (mode==='sandbox'||billingApproved==='true');
}
module.exports={paymentEnabled};
