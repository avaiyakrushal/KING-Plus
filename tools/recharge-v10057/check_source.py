from pathlib import Path
s=Path('app/src/main/java/com/kingplus/social/KingRecharge974Activity.java').read_text()
policy=Path('functions/recharge-policy.js').read_text()
service=Path('functions/cashfree-recharge-service.js').read_text()
assert 'versionCode 336' in Path('app/build.gradle').read_text()
for needle in ['goldenPanel()', 'packBackground()', 'UPI_PRODUCT_IDS','methodHeader("UPI"', 'methodHeader("Agent"', 'methodHeader("Card"', 'methodHeader("Google Pay"',
               'previewPackRows(false)', 'Preview • OFF','Server', 'rechargeAgreement.isChecked()', 'getCashfreeRechargeCatalog',
               'getCashfreeRechargeHistory', 'KingAgency9982Activity.class', 'KingWalletVisualActivity.class', 'LIVE_RECHARGE = false']:
    assert needle in s,needle
for qty in [8800,17800,45000,90000,180000,450000]:
    sku='king_upi_'+str(qty)
    assert sku in s and sku in policy and sku in service, sku
assert 'if(!Arrays.asList(UPI_PRODUCT_IDS).contains(id))continue;' in s
assert 'if(!Arrays.asList(PRODUCT_IDS).contains(id))continue;' not in s
assert 'if("Agent".equals(label)){' not in s, 'Agent should expand rather than automatically navigate'
assert 'LIVE_RECHARGE = true' not in s
assert 'runTransaction' not in s, 'Do not mint Diamonds on Android'
print('PASS: merchant-priced package parity, UX sections and no-client-credit checks')
