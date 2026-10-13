#!/usr/bin/env python3
"""KING Plus 9.9.82: safe invitation-only Agency → Agent → pending Diamond orders.

Restore only the complete signed v9.9.81 Android source. Preserve v9.9.80
real Bingo, v9.9.81 Me page and all earlier Party/Gifts and Wallet safeguards.
"""
from pathlib import Path
import re,sys,shutil
root=Path(sys.argv[1])
here=Path(__file__).resolve().parent
src=root/'app/src/main/java/com/kingplus/social'
for name in ('KingAgency9982Activity.java','KingAgencyPolicy9982.java'):
 shutil.copyfile(here/name,src/name)
rules=root/'firestore.rules'
s=rules.read_text()
anchor='''    match /wallets/{uid} {'''
assert s.count(anchor)==1,'Wallet root rules changed'
s=s.replace(anchor,(here/'agency-firestore.rules').read_text()+anchor,1)
rules.write_text(s)
manifest=root/'app/src/main/AndroidManifest.xml';m=manifest.read_text()
anchor='<activity android:name=".RoomGameActivity" android:exported="false" />'
assert m.count(anchor)==1,'Expected room game manifest registration'
m=m.replace(anchor,anchor+'\n        <activity android:name=".KingAgency9982Activity" android:exported="false" />',1)
manifest.write_text(m)
deep=src/'KingDeepFlowActivity.java';s=deep.read_text()
anchor='''            case "profile_menu": profileMenu();break;'''
assert s.count(anchor)==1,'Me menu route unexpectedly changed'
s=s.replace(anchor,'''            case "agency": startActivity(new Intent(this,KingAgency9982Activity.class));finish();break;
'''+anchor,1)
anchor='''section("Account");'''
assert anchor in s.split('    private void profileMenu(){',1)[1].split('    private void privilegePack(){',1)[0]
head,tail=s.split('    private void profileMenu(){',1)
profile,rest=tail.split('    private void privilegePack(){',1)
profile=profile.replace(anchor,anchor+'''row("🏢","Agency & Agent Recharge","Super Admin, Agency owner, Agent requests","agency");''',1)
s=head+'    private void profileMenu(){'+profile+'    private void privilegePack(){'+rest
deep.write_text(s)
main=src/'MainActivity.java';s=main.read_text()
anchor='''        more9981.setOnClickListener(v->openDeepFlow810("profile_menu"));'''
assert s.count(anchor)==1,'Expected existing Profile All Me options'
s=s.replace(anchor,anchor+'''
        TextView agencyEntry9982=new TextView(this);
        agencyEntry9982.setText("🏢 Agency & Agent Recharge   ›");
        agencyEntry9982.setTextSize(15);agencyEntry9982.setTextColor(0xff34254e);
        agencyEntry9982.setPadding(dp(14),dp(12),dp(14),dp(12));
        agencyEntry9982.setBackground(background(0xffffedbb,12));
        agencyEntry9982.setOnClickListener(v->startActivity(
            new Intent(this,KingAgency9982Activity.class)));
        LinearLayout.LayoutParams agencyEntryLp9982=new LinearLayout.LayoutParams(-1,dp(52));
        agencyEntryLp9982.setMargins(0,dp(5),0,dp(10));content.addView(agencyEntry9982,agencyEntryLp9982);
''',1)
main.write_text(s)
build=root/'app/build.gradle';g=build.read_text()
g,n=re.subn(r"versionCode\s+260\s*;\s*versionName\s+'[^']+'",
"versionCode 261; versionName '9.9.82-agency-agent-pending-recharge-test'",g)
assert n==1,'Expected last signed v9.9.81 source (versionCode 260)'
build.write_text(g)
assert 'allow write: if false;' in rules.read_text().split('match /wallets/{uid} {')[1].split('}',1)[0]
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
assert 'KingLiveBingo9980Activity' in manifest.read_text()
assert 'KingMeRoomHistory9981' in main.read_text()
print('PASS Agency/Agent UI + nested Me navigation + Firestore fail-closed rules; no client wallet writes')
