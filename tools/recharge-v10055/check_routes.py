from pathlib import Path
b=Path(__file__).resolve().parents[2]
me=(b/'app/src/main/java/com/kingplus/social/KingDeepFlowActivity.java').read_text()
main=(b/'app/src/main/java/com/kingplus/social/MainActivity.java').read_text()
wallet=(b/'app/src/main/java/com/kingplus/social/KingWalletVisualActivity.java').read_text()
recharge=(b/'app/src/main/java/com/kingplus/social/KingRecharge974Activity.java').read_text()
gradle=(b/'app/build.gradle').read_text()
manifest=(b/'app/src/main/AndroidManifest.xml').read_text()
checks={
    'Me wallet navigates to live recharge': 'private void wallet(){startActivity(new Intent(this,KingRecharge974Activity.class));finish();}' in me,
    'Main recharge center routes to shop': 'private void rechargeCenterPage(){\n        startActivity(new Intent(this,KingRecharge974Activity.class));' in main,
    'Wallet test screen has direct Recharge CTA': 'recharge.setOnClickListener(v->openRecharge(false));' in wallet,
    'Wallet Diamond tab is clickable': 'diamond.setOnClickListener(v->openRecharge(false));' in wallet,
    'Wallet Crystal tab is clickable': 'crystal.setOnClickListener(v->openRecharge(true));' in wallet,
    'Shop accepts Crystal tab intent': 'getBooleanExtra("KING_RECHARGE_CRYSTAL",false)' in recharge,
    'Shop can navigate to Wallet tools': 'new Intent(this,KingWalletVisualActivity.class)' in recharge,
    'Shop includes UPI, Agent, Card, Google Pay': all('methodHeader("'+x+'"' in recharge for x in ('UPI','Agent','Card','Google Pay')),
    'Full shop is in manifest': all('android:name=".'+x+'"' in manifest for x in ('KingRecharge974Activity','KingWalletVisualActivity')),
    'Real money remains disabled': 'LIVE_RECHARGE = false' in recharge,
    'No previous test-wallet Me routing remains': 'private void wallet(){startActivity(new Intent(this,KingWalletVisualActivity.class));finish();}' not in me,
    'Version 336': 'versionCode 336' in gradle,
}
for name,ok in checks.items():print(('PASS' if ok else 'FAIL')+': '+name)
assert all(checks.values()),'Routing regression failure'
print('PASS '+str(len(checks))+' route tests')
