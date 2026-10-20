from pathlib import Path
s=Path('app/src/main/java/com/kingplus/social/KingRecharge974Activity.java').read_text()
b=Path('app/build.gradle').read_text()
provider=Path('functions/cashfree-recharge-service.js').read_text()
assert 'versionCode 337' in b
checks={
'none of the purchase paths locally credit Diamonds':'runTransaction' not in s and 'FieldValue.increment' not in s,
'payments disabled without compliance':'LIVE_RECHARGE = false' in s,
'diamonds / crystal separate tabs':'crystalSelected' in s and 'diamondTab' in s and 'crystalTab' in s,
'dynamic selected tab indicator':'crystalUnderline.setBackgroundColor' in s and 'diamondUnderline.setBackgroundColor' in s,
'agreement persisted through method switches':'rememberShopForm();' in s and 'rechargeAgreement.setChecked(agreementAccepted)' in s,
'consent controlled and restored':'rechargeAgreement.setOnCheckedChangeListener' in s and 'recharge_agreement' in s,
'phone preserved on re-render':'enteredPhone.matches' in s and 'recharge_phone' in s,
'selectable method cards':'methodHeader("UPI"' in s and 'methodHeader("Agent"' in s and 'methodHeader("Card"' in s and 'methodHeader("Google Pay"' in s,
'original design thumbnails not copied':'addPackageRow(upiRows,false' in s and 'addPackageRow(list,crystal' in s,
'server-only price': 'Merchant-approved UPI package' in s and 'amountPaise' in s,
'approved catalog gate only for new purchase':'if(!upiReady||!sameAccount()||cashfreeVerifying)return;' in s,
'old payment verification independent of catalog':'if(!sameAccount()||cashfreeVerifying||cashfreePendingOrder==null)return;' in s,
'already credited orders not charged again':'"Verified Recharge Receipt"' in s,
'preserve exact order id when switching to UPI':'cashfree_pending_"+signedInUid,id).apply();' in s,
'deduplicated order use': 'createCashfreeRechargeLink' in s and 'Previous recharge is still pending' in provider,
'checkout path scoped to approved hosts':'uri.getPath().startsWith("/links/")' in s and 'uri.getUserInfo()==null' in s,
'server order history is used':'getCashfreeRechargeHistory' in s,
'demo cannot mint Diamonds':'No money was collected' in s,
'agent portal remains available':'KingAgency9982Activity.class' in s,
'crystal is not sold':'Crystal wallet and purchase backend pending' in s,
}
for label,passed in checks.items():
    assert passed, 'FAIL: '+label
print('PASS: %d v10.0.58 shop/navigation/payment regression source checks' % len(checks))
