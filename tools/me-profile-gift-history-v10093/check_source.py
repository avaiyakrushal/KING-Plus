from pathlib import Path
root=Path(__file__).resolve().parents[2]
flow=(root/"app/src/main/java/com/kingplus/social/KingDeepFlowActivity.java").read_text()
gradle=(root/"app/build.gradle").read_text()
workflow=(root/".github/workflows/king-v10077-party-rejoin-choice.yml").read_text()
assert 'row("🕘","Gift History","Secure sent/received wallet ledger","gift_history");' in flow
assert 'case "gift_history":giftHistory();break;' in flow
assert 'private void giftHistory()' in flow and 'fetchAccountLedger9983(true,false)' in flow
assert 'versionCode 378' in gradle and "10.0.99-home-party-category-options" in gradle
assert "versionCode='378'" in workflow and 'KING-Plus-v10.0.99-Home-Party-TEST.apk' in workflow
print("PASS: Gift Center history opens the account-scoped, server-backed Gift History route")
