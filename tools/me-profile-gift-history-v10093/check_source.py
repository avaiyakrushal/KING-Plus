from pathlib import Path
root=Path(__file__).resolve().parents[2]
flow=(root/"app/src/main/java/com/kingplus/social/KingDeepFlowActivity.java").read_text()
assert 'row("🕘","Gift History","Secure sent/received wallet ledger","gift_history");' in flow
assert 'case "gift_history":giftHistory();break;' in flow
assert 'private void giftHistory()' in flow and 'fetchAccountLedger9983(true,false)' in flow
print("PASS: Gift Center history opens the account-scoped, server-backed Gift History route")
