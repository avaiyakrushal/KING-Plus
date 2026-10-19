#!/usr/bin/env python3
"""Static UX and security invariants for v10.0.51 recharge refactor."""
from pathlib import Path
s=Path('app/src/main/java/com/kingplus/social/KingRecharge974Activity.java').read_text()
required={
 'Diamonds/Crystal tabs':'crystalTab.setOnClickListener',
 'independent Crystal blocked':'Crystal recharge is unavailable',
 'UPI section':'methodHeader("UPI"',
 'Card section':'methodHeader("Card"',
 'Agent portal':'new Intent(this,KingAgency9982Activity.class)',
 'Google Pay section':'methodHeader("Google Pay"',
 'Merchant-sourced price':'getCashfreeRechargeCatalog',
 'Server-sourced balance':'doc.getMetadata().isFromCache()',
 'Customer history':'getCashfreeRechargeHistory',
 'Agreement gate':'rechargeAgreement.isChecked()',
 'Real-money offline':'LIVE_RECHARGE = false',
 'No local credits':'confirmCashfreeRecharge',
 'Only verified payment link':'payments.cashfree.com',
 'Preview package notice':'Preview • OFF',
}
for description,needle in required.items():
 assert needle in s, (description,needle)
assert 'setText("0")' not in s, 'Do not fabricate zero for unverified balance'
assert 'coins:++' not in s
assert 'runTransaction' not in s, 'Wallet credits MUST live exclusively on server'
print(f'PASS: {len(required)} recharge UI and payment-safety checks')
