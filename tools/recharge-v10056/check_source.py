from pathlib import Path
app=Path('app/src/main/java/com/kingplus/social/KingRecharge974Activity.java').read_text()
gradle=Path('app/build.gradle').read_text()
assert 'versionCode 336' in gradle
assert 'LIVE_RECHARGE = false' in app
assert 'private void addSafeDemoTrial()' in app
assert 'if(!crystalSelected)addSafeDemoTrial();' in app
assert 'Accept Recharge Agreement before the DEMO.' in app
assert 'No UPI app or payment gateway will open.' in app
assert '₹0 charged • 0 real Diamonds credited' in app
assert 'Real Diamonds / VIP / Crystal balances stay unchanged.' in app
assert 'king_recharge_demo_last_' in app
assert 'account.equals(signedInUid)' in app
assert 'createCashfreeRechargeLink' in app
assert 'getCashfreeRechargeCatalog' in app
assert 'resumeCashfreeRecharge' in Path('functions/v3.js').read_text()
assert 'LIVE_RECHARGE = true' not in app
method=app.split('private void addSafeDemoTrial(){',1)[1].split('    @SuppressWarnings("unchecked")',1)[0]
for forbidden in ['getHttpsCallable(', 'FirebaseFirestore', 'collection(', 'startActivity(', 'coins:', 'wallets', 'putLong(', 'applyVerifiedRecharge', 'startUpiLink(', 'startPurchase(']:
    assert forbidden not in method, ('DEMO must not mutate wallet, call gateway or launch payment',forbidden)
assert 'new AlertDialog.Builder(this)' in method
print('Recharge DEMO: 16 checks passed; no gateway call or wallet credit inside DEMO')
