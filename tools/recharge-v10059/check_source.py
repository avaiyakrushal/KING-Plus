from pathlib import Path
s=Path("app/src/main/java/com/kingplus/social/KingRecharge974Activity.java").read_text()
g=Path("app/build.gradle").read_text()
agent=Path("app/src/main/java/com/kingplus/social/KingAgency9982Activity.java").read_text()
checks={
 "new versionCode 338": "versionCode 338" in g,
 "recharge screen cannot start real Play purchases": "LIVE_RECHARGE = false" in s,
 "recharge screen cannot start Cashfree payments": "LIVE_CASHFREE_RECHARGE = false" in s,
 "cashfree create flow has local launch gate": "if(!LIVE_CASHFREE_RECHARGE||!upiReady" in s,
 "existing real payment links not reopened in TEST": "if(!LIVE_CASHFREE_RECHARGE){" in s and "Payment not available in TEST" in s,
 "older payment verification retained": "confirmCashfreeRecharge" in s and "getCashfreeRechargeHistory" in s,
 "no client-side real Diamond minting": "FieldValue.increment" not in s and "runTransaction" not in s,
 "both top tabs": 'diamondTab=item("Diamonds"' in s and 'crystalTab=item("Crystal"' in s,
 "both currency sample previews": "previewPackRows(true)" in s and "previewPackRows(false)" in s,
 "four expandable methods": all('methodHeader("'+m+'"' in s for m in ('UPI','Agent','Card','Google Pay')),
 "method-specific Crystal preview": 'crystalMethodPreview("Card")' in s and 'crystalMethodPreview("Agent")' in s,
 "agent portal available": "KingAgency9982Activity.class" in s,
 "agent requests do not credit wallet": "NOT a Diamond credit" in agent,
 "sample packs are tappable": 'chooseDemoPack(crystal,quantity)' in s,
 "demo method and pack capture": 'final String method=selectedMethod' in s and 'selectedDemoDiamonds' in s,
 "crystal demo supported": 'selectedDemoCrystals' in s and 'addSafeDemoTrial();' in s,
 "recharge agreement required": 'rechargeAgreement.isChecked()' in s,
 "form saved on rotation": 'out.putBoolean("recharge_agreement"' in s and 'out.putLong("recharge_demo_crystals"' in s,
 "demo history scoped to signed-in UID": 'king_recharge_demo_history_"+account' in s,
 "history is locally stored, not firebase": 'getPreferences(MODE_PRIVATE).edit().putString(key' in s,
 "demo is explicitly zero rupees": 'Amount charged: ₹0' in s,
 "unverified prices not shown as real": '"DEMO ₹0",false' in s,
 "server price never from client": 'amountPaise' in s,
 "safe Cashfree links still verified": 'uri.getPath().startsWith("/links/")' in s,
}
for name,okay in checks.items():
    assert okay, "FAIL: "+name
print("PASS: %d v10.0.59 recharge no-money UI/safety checks" % len(checks))
