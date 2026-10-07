from pathlib import Path
import sys

root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'

def update(name, replacements):
    p=pkg/name
    s=p.read_text()
    for label, old, new in replacements:
        if old not in s:
            raise SystemExit(f'{name}: missing marker: {label}')
        s=s.replace(old,new,1)
    p.write_text(s)

update('KingNav.java',[
('openRoot safe start',
'''        activity.startActivity(i);
        if (!(activity instanceof MainActivity && (tab == 1 || tab == 4))) activity.finish();
''',
'''        final Intent target = i;
        KingSafe.run(activity,"root-nav-"+tab,()->activity.startActivity(target));
'''),
('openMainTab safe start',
'''        activity.startActivity(i);
        if (!(activity instanceof MainActivity)) activity.finish();
''',
'''        final Intent target = i;
        KingSafe.run(activity,"main-tab-"+tab,()->activity.startActivity(target));
''')
])

update('MainActivity.java',[
('onNewIntent guard',
'''    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null && intent.getBooleanExtra("forceLogin", false)) { login(); return; }
        int tab = intent == null ? 0 : intent.getIntExtra("openTab", 0);
        if (tab == 1) games();
        else if (tab == 2) discover();
        else if (tab == 3) messages();
        else if (tab == 4) profile();
        else home();
    }
''',
'''    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        safeUiAction940("main-on-new-intent",()->{
            if (intent != null && intent.getBooleanExtra("forceLogin", false)) { login(); return; }
            int tab = intent == null ? 0 : intent.getIntExtra("openTab", 0);
            if (tab == 1) games();
            else if (tab == 2) discover();
            else if (tab == 3) messages();
            else if (tab == 4) profile();
            else home();
        });
    }
'''),
('drawer guard',
'''row.setOnClickListener(v->{dialog.dismiss();actions[k].run();});''',
'''row.setOnClickListener(v->{dialog.dismiss();KingSafe.run(this,"drawer:"+items[k],actions[k]);});''')
])

update('CommunityHubActivity.java',[
('button guard','''t.setOnClickListener(v->r.run());return t;''','''t.setOnClickListener(v->KingSafe.run(this,"community-button:"+s,r));return t;''')
])

update('KingEcosystemProActivity.java',[
('button guard','''t.setOnClickListener(v->r.run());return t;''','''t.setOnClickListener(v->KingSafe.run(this,"ecosystem-button:"+s,r));return t;'''),
('row guard','''x.setOnClickListener(v->r.run());LinearLayout.LayoutParams lp''','''x.setOnClickListener(v->KingSafe.run(this,"ecosystem-row:"+a,r));LinearLayout.LayoutParams lp'''),
('tab guard','''t.setOnClickListener(v->show(r));LinearLayout.LayoutParams lp''','''t.setOnClickListener(v->KingSafe.run(this,"ecosystem-tab:"+r,()->show(r)));LinearLayout.LayoutParams lp''')
])

update('KingParityHubActivity.java',[
('action guard','''t.setOnClickListener(v->r.run());return t;''','''t.setOnClickListener(v->KingSafe.run(this,"parity-action:"+s,r));return t;'''),
('card guard','''x.setOnClickListener(v->r.run());LinearLayout.LayoutParams lp''','''x.setOnClickListener(v->KingSafe.run(this,"parity-card:"+a,r));LinearLayout.LayoutParams lp'''),
('tab guard','''t.setOnClickListener(v->show(r));LinearLayout.LayoutParams lp''','''t.setOnClickListener(v->KingSafe.run(this,"parity-tab:"+r,()->show(r)));LinearLayout.LayoutParams lp''')
])

update('KingProductionTestActivity.java',[
('button guard','''t.setOnClickListener(v->r.run());return t;''','''t.setOnClickListener(v->KingSafe.run(this,"production-test:"+s,r));return t;''')
])

update('KingPublicProfileActivity.java',[
('row guard','''x.setOnClickListener(v->r.run());LinearLayout.LayoutParams lp''','''x.setOnClickListener(v->KingSafe.run(this,"public-profile-row:"+a,r));LinearLayout.LayoutParams lp'''),
('follow guard','''followBtn.setOnClickListener(v->toggleFollow());''','''followBtn.setOnClickListener(v->KingSafe.run(this,"public-profile-follow",this::toggleFollow));'''),
('chat guard','''msg.setOnClickListener(v->openChat());''','''msg.setOnClickListener(v->KingSafe.run(this,"public-profile-chat",this::openChat));'''),
('gift guard','''gift.setOnClickListener(v->openGift());''','''gift.setOnClickListener(v->KingSafe.run(this,"public-profile-gift",this::openGift));''')
])

update('PartyActivity.java',[
('party shared action throwable',
'''        try{ action.run(); }catch(Exception e){
            android.util.Log.e("KINGPlusParty","Party action failed: "+label,e);
            if(!isFinishing()&&!isDestroyed()) toast("Could not complete that action • please try again");
        }
''',
'''        try{ action.run(); }catch(Throwable e){
            KingStability.nonFatal(this,"party-action:"+label,e);
            android.util.Log.e("KINGPlusParty","Party action failed: "+label,e);
            if(!isFinishing()&&!isDestroyed()) toast("Could not complete that action • please try again");
        }
'''),
('party lobby back',
'''    @Override public void onBackPressed(){if(roomId!=null)confirmLeave940();else KingNav.confirmExit(this);}''',
'''    @Override public void onBackPressed(){if(roomId!=null){confirmLeave940();return;}if(isTaskRoot())KingNav.confirmExit(this);else finish();}''')
])

update('DiscoverActivity.java',[
('discover back',
'''    @Override public void onBackPressed(){KingNav.confirmExit(this);}''',
'''    @Override public void onBackPressed(){if(isTaskRoot())KingNav.confirmExit(this);else finish();}''')
])

update('InboxActivity.java',[
('inbox back',
'''    @Override public void onBackPressed(){KingNav.confirmExit(this);}''',
'''    @Override public void onBackPressed(){if(isTaskRoot())KingNav.confirmExit(this);else finish();}''')
])

update('VoiceWebActivity.java',[
('url encoder compatibility',
'''String name=URLEncoder.encode(displayName,StandardCharsets.UTF_8);''',
'''String name=android.net.Uri.encode(displayName);''')
])

gradle=root/'app/build.gradle'
g=gradle.read_text()
old="versionCode 139; versionName '9.4.0-dev'"
if old not in g:
    raise SystemExit('build.gradle v9.4.0 marker missing')
gradle.write_text(g.replace(old,"versionCode 140; versionName '9.4.1-stability'",1))

print('KING Plus v9.4.1 stability hardening applied')
