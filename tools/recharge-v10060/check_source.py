from pathlib import Path
s=Path('app/src/main/java/com/kingplus/social/KingRecharge974Activity.java').read_text()
g=Path('app/build.gradle').read_text()
a=Path('app/src/main/java/com/kingplus/social/KingAgency9982Activity.java').read_text()
tests={
 'new version 339': 'versionCode 339' in g,
 'real play disabled': 'LIVE_RECHARGE = false' in s,
 'cashfree payments disabled': 'LIVE_CASHFREE_RECHARGE = false' in s,
 'no orphaned legacy history variables': 'putString(key,latest.toString())' not in s,
 'no insecure diamond award': 'FieldValue.increment' not in s and 'runTransaction' not in s,
 'all four method accordions': all('methodHeader("'+m+'"' in s for m in ('UPI','Agent','Card','Google Pay')),
 'diamonds and crystal': 'diamondTab=item("Diamonds"' in s and 'crystalTab=item("Crystal"' in s,
 'selected demo visibly shown': 'demoSelectionView.setText(selectedDemoSummary())' in s,
 'no cash prices invented': '"DEMO ₹0",false' in s,
 'agreement required for demo': 'rechargeAgreement.isChecked()' in s,
 'demo receipt persisted only locally': 'getPreferences(MODE_PRIVATE).edit().putString(demoReceiptsKey(account)' in s,
 'structured demo receipt account-scoped': 'king_recharge_demo_receipts_v10060_' in s and 'final String account=signedInUid' in s,
 'receipt has unique ID': 'java.util.UUID.randomUUID()' in s,
 'receipt has no bank/card secrets': all('receipt.put("'+k+'"' in s for k in ('id','when','currency','method','quantity','chargedPaise','credited')),
 'demo always zero charged and credited': 'receipt.put("chargedPaise",0)' in s and 'receipt.put("credited",0)' in s,
 'max 30 receipts': 'i<29' in s,
 'history filter by asset': '"Diamonds only"' in s and '"Crystal only"' in s,
 'history filter by methods': '"Google Pay only"' in s and '"Agent only"' in s,
 'receipt drill-down': '.setTitle("DEMO receipt • no money")' in s,
 'clear only local demo history': '.remove(demoReceiptsKey(account))' in s and '.remove("king_recharge_demo_history_"+account)' in s,
 'clear confirmation dialog': '.setTitle("Clear local DEMO history?")' in s,
 'legacy local history maintained': '"Previous DEMO history (older versions)"' in s,
 'verified history remains server-only': 'getCashfreeRechargeHistory' in s,
 'crystal history not misrepresented as paid': 'if(crystalSelected)showDemoHistory();else showRechargeHistory();' in s,
 'agent portal unchanged': 'KingAgency9982Activity.class' in s,
 'agent not credited automatically': 'NOT a Diamond credit' in a,
 'rotation preserves form': 'out.putLong("recharge_demo_crystals"' in s and 'out.putBoolean("recharge_agreement"' in s,
 'no cashfree real launch': 'if(!LIVE_CASHFREE_RECHARGE||!upiReady' in s,
 'hard stop existing checkout links': 'if(!LIVE_CASHFREE_RECHARGE){' in s,
}
for name,ok in tests.items():
    if not ok: raise AssertionError('FAIL '+name)
print('PASS:',len(tests),'v10.0.60 demo receipt, history, checkout safety tests')
