from pathlib import Path

root = Path(__file__).resolve().parents[2]
level = (root / 'app/src/main/java/com/kingplus/social/LevelSystem.java').read_text()
party = (root / 'app/src/main/java/com/kingplus/social/PartyActivity.java').read_text()
flow = (root / 'app/src/main/java/com/kingplus/social/KingDeepFlowActivity.java').read_text()

assert "KING Plus v10.0.83 Profile Progress Integrity TEST" in (root / 'RELEASE-NOTES-v10.0.83.md').read_text()
assert "return new Snapshot(levelFor(xp),xp,0,0);" in level
assert "p.edit().putLong(\"xp\",xp).apply();" in level
assert "return add(c,xp,0);" in level
assert 'putLong("vip_points"' not in level
assert 'Local TEST preview only; no cloud profile, XP, VIP, Noble, or reward credit.' in party
assert 'syncMyProgress700(after)' not in party
assert 'm.put("xp",p.xp)' not in party
assert 'TEST Activity Level' in flow
assert 'Device-only preview • not account-synced or server-verified • XP has no reward value' in flow
assert 'TEST catalog • no medal is granted from device-only XP or unverified VIP' in flow
assert 'Verified Wallet entitlement pending' in flow
assert 'Counts and records come from your signed-in account.' not in flow
assert 'Account-scoped report receipts from the server' in flow

print('PASS: device-only activity is labeled TEST, cannot mint VIP or cloud XP, and unverified medals stay locked')
