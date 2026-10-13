#!/usr/bin/env python3
"""KING Plus v9.9.84: REAL account-scoped Me privacy/notification settings,
read/unblock actual Firestore blocks, enforce incoming DM block and opt-out.
Start with full v9.9.83 signed source. No Wallet or Gift entitlement edits.
"""
from pathlib import Path
import re,shutil,sys
root=Path(sys.argv[1])
src=root/'app/src/main/java/com/kingplus/social'
here=Path(__file__).resolve().parent
for name in ('KingMePrivacyPolicy9984.java','KingMePrivacy9984Activity.java'):
    shutil.copyfile(here/name,src/name)
rules=root/'firestore.rules';r=rules.read_text()
anchor='    match /wallets/{uid} {'
assert r.count(anchor)==1,'Wallet boundary changed'
r=r.replace(anchor,(here/'me-privacy-firestore.rules').read_text()+anchor,1)
# Preserve existing block read path but strengthen deterministic identifiers
old="""        && request.resource.data.targetUid != request.auth.uid;
      allow update: if false;
      allow delete: if signedIn()
        && resource.data.ownerUid == request.auth.uid;
    }

    match /ludo_matches/{matchCode} {"""
new="""        && request.resource.data.targetUid != request.auth.uid
        && request.resource.data.targetUid.size() >= 4
        && request.resource.data.targetUid.size() <= 128
        && blockId == request.auth.uid + '_' + request.resource.data.targetUid
        && request.resource.data.keys().hasOnly(['ownerUid','targetUid','createdAt'])
        && request.resource.data.keys().hasAll(['ownerUid','targetUid','createdAt'])
        && request.resource.data.createdAt == request.time;
      allow update: if false;
      allow delete: if signedIn()
        && resource.data.ownerUid == request.auth.uid;
    }

    match /ludo_matches/{matchCode} {"""
assert r.count(old)==1,'Existing block rules changed'
r=r.replace(old,new,1)
# Direct private messages, no fake "blocked" toggle; server stops NEW messages
old="""          && request.resource.data.recipientUid is string
          && request.resource.data.text is string"""
new="""          && request.resource.data.recipientUid is string
          && request.resource.data.recipientUid != request.auth.uid
          && request.resource.data.recipientUid in
             get(/databases/$(database)/documents/direct_threads/$(threadId)).data.members
          && !exists(/databases/$(database)/documents/blocks/$(request.auth.uid + '_' + request.resource.data.recipientUid))
          && !exists(/databases/$(database)/documents/blocks/$(request.resource.data.recipientUid + '_' + request.auth.uid))
          && (!exists(/databases/$(database)/documents/king_me_preferences/$(request.resource.data.recipientUid))
              || get(/databases/$(database)/documents/king_me_preferences/$(request.resource.data.recipientUid)).data.allowPrivateMessages == true)
          && request.resource.data.text is string"""
assert r.count(old)==1,'DM rules anchor missing'
r=r.replace(old,new,1)
rules.write_text(r)
manifest=root/'app/src/main/AndroidManifest.xml';m=manifest.read_text()
anchor='<activity android:name=".KingDeepFlowActivity" android:exported="false" />'
assert m.count(anchor)==1,'Main deep flow Activity manifest changed'
m=m.replace(anchor,anchor+'\n        <activity android:name=".KingMePrivacy9984Activity" android:exported="false" />',1)
manifest.write_text(m)
deep=src/'KingDeepFlowActivity.java';d=deep.read_text()
# dedicated cloud settings activity from both deep route and nested safety
a='''case "settings_privacy":settingsPrivacy();break;case "settings_notifications":settingsNotifications();break;'''
b='''case "settings_privacy":openMeCloud9984("privacy");break;case "settings_notifications":openMeCloud9984("notifications");break;'''
assert d.count(a)==1
d=d.replace(a,b,1)
a='''case "detail_profile_safety":case "detail_help_safety":case "detail_blocked":
            case "detail_reports":openMeAction9981("safety");break;'''
b='''case "detail_profile_safety":case "detail_help_safety":case "detail_blocked":
            case "detail_reports":openMeCloud9984("blocked");break;'''
assert d.count(a)==1
d=d.replace(a,b,1)
anchor='    private void settingsPrivacy(){'
assert d.count(anchor)==1
d=d.replace(anchor,'''    private void openMeCloud9984(String page) {
        Intent activity=new Intent(this,KingMePrivacy9984Activity.class);
        activity.putExtra("page",page);
        startActivity(activity);
    }
'''+anchor,1)
deep.write_text(d)
main=src/'MainActivity.java';s=main.read_text()
start=s.index('    private void privacySafetyPage(){')
end=s.index('    private void profile() {',start)
s=s[:start]+'''    private void privacySafetyPage(){
        Intent i=new Intent(this,KingMePrivacy9984Activity.class);
        i.putExtra("page","privacy");
        startActivity(i);
    }

'''+s[end:]
# The Main settings Notifications switch was local-only and did not filter pushes.
old='''button("🔔 Notifications: "+(getPreferences(0).getBoolean("setting_notifications",true)?"ON":"OFF"),CARD,()->toggleSetting("setting_notifications","Notifications",this::settingsPage));'''
new='''button("🔔 Notification Preferences • Firebase Account",CARD,()->openDeepFlow810("settings_notifications"));'''
assert s.count(old)==1
s=s.replace(old,new,1)
main.write_text(s)
gradle=root/'app/build.gradle';g=gradle.read_text()
g,n=re.subn(r"versionCode\s+262\s*;\s*versionName\s+'[^']+'",
    "versionCode 263; versionName '9.9.84-me-cloud-privacy-blocked-test'",g)
assert n==1,'Must use complete v9.9.83 source'
gradle.write_text(g)
assert 'KingLiveBingo9980Activity' in m
assert 'KingAgency9982Activity' in m
assert 'KingMeGiftSummary9983' in (src/'KingDeepFlowActivity.java').read_text()
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print('PASS Me cloud privacy/alerts account sync + actual Firebase Block list + DM server protection; prior Wallet and Games untouched')
