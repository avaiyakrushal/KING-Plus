#!/usr/bin/env python3
"""KING v9.9.81: make Me/Profile and its nested menus real, not dummy toggles.
Apply only on the complete signed v9.9.80 source from Actions 38418909642.
"""
from pathlib import Path
import sys,re,shutil
root=Path(sys.argv[1])
src=root/'app/src/main/java/com/kingplus/social'
tools=Path(__file__).parent
f=src/'MainActivity.java';s=f.read_text()
def swap(old,new):
 global s
 assert s.count(old)==1, 'Me MainActivity changed: '+old[:80]
 s=s.replace(old,new,1)
swap('''                int openTab=getIntent()==null?0:getIntent().getIntExtra("openTab",0);
                if(openTab==1)games();''','''                String action9981=getIntent()==null?null:getIntent().getStringExtra("meAction9981");
                int openTab=getIntent()==null?0:getIntent().getIntExtra("openTab",0);
                if(action9981!=null&&routeMeAction9981(action9981)){}
                else if(openTab==1)games();''')
swap('''            int tab = intent == null ? 0 : intent.getIntExtra("openTab", 0);
            if (tab == 1) games();''','''            String action9981=intent==null?null:intent.getStringExtra("meAction9981");
            int tab = intent == null ? 0 : intent.getIntExtra("openTab", 0);
            if(action9981!=null&&routeMeAction9981(action9981))return;
            if (tab == 1) games();''')
swap('''tabs.addView(fav,new LinearLayout.LayoutParams(dp(86),dp(38)));TextView follow=''', '''fav.setOnClickListener(v->showMeRoomHistory9981("favorites"));
        tabs.addView(fav,new LinearLayout.LayoutParams(dp(86),dp(38)));
        TextView rec9981=new TextView(this);rec9981.setText("Recent");rec9981.setTextSize(14);
        rec9981.setTextColor(0xff333333);rec9981.setGravity(Gravity.CENTER);
        rec9981.setOnClickListener(v->showMeRoomHistory9981("recent"));
        tabs.addView(rec9981,new LinearLayout.LayoutParams(dp(78),dp(38)));TextView follow=''')
swap('records.setText("Party Records");','records.setText("My Hosted Party Rooms");')
swap('''        addProfileVideoTabs940(content);
        addProfileRecommendations940(content);''','''        addProfileVideoTabs940(content);
        addProfileRecommendations940(content);
        TextView more9981=new TextView(this);
        more9981.setText("☰  All Me options  ›   Account • VIP • Family • Privacy • Help");
        more9981.setTextSize(14);more9981.setTypeface(null,Typeface.BOLD);
        more9981.setTextColor(0xff34254e);more9981.setPadding(dp(14),dp(12),dp(14),dp(12));
        more9981.setBackground(background(0xffffedbb,12));
        more9981.setOnClickListener(v->openDeepFlow810("profile_menu"));
        LinearLayout.LayoutParams moreLp9981=new LinearLayout.LayoutParams(-1,dp(60));
        moreLp9981.setMargins(0,dp(8),0,dp(10));content.addView(more9981,moreLp9981);''')
swap('"Help Center","Rules and Policies"};', '"Help Center","Rules and Policies","All Me Options"};')
swap('"help_centre","rules_policies"};', '"help_centre","rules_policies","square"};')
swap('''()->openDeepFlow810("help"),()->openDeepFlow810("rules")};''',
     '''()->openDeepFlow810("help"),()->openDeepFlow810("rules"),()->openDeepFlow810("profile_menu")};''')
start=s.index('    private void loadPartyRecordsV600(')
end=s.index('    private void mePartyRecordV600(',start)
s=s[:start]+'''    private void loadPartyRecordsV600(LinearLayout host){
        host.removeAllViews();
        if(firestore==null||firebaseAuth==null||firebaseAuth.getCurrentUser()==null){
            mePartyRecordV600(host,"🎤","Sign in for hosted rooms","Only your real rooms appear here",null);return;
        }
        final String currentUid9981=firebaseAuth.getCurrentUser().getUid();
        mePartyRecordV600(host,"⌛","Loading hosted rooms","Account-verified Party records",null);
        firestore.collection("live_rooms").whereEqualTo("ownerUid",currentUid9981).limit(30).get()
            .addOnSuccessListener(q->{
                if(isFinishing()||isDestroyed()||firebaseAuth==null||firebaseAuth.getCurrentUser()==null
                        ||!currentUid9981.equals(firebaseAuth.getCurrentUser().getUid()))return;
                host.removeAllViews();
                java.util.List<DocumentSnapshot> docs=new ArrayList<>(q.getDocuments());
                java.util.Collections.sort(docs,(a,b)->{
                    com.google.firebase.Timestamp at=a.getTimestamp("createdAt"),bt=b.getTimestamp("createdAt");
                    return at==null?(bt==null?0:1):bt==null?-1:bt.compareTo(at);
                });
                int count=0;
                for(DocumentSnapshot doc:docs){
                    if(!currentUid9981.equals(doc.getString("ownerUid")))continue;
                    String id=doc.getId(),name=doc.getString("name");
                    if(name==null||name.trim().isEmpty())name="KING Party";
                    boolean closed=Boolean.TRUE.equals(doc.getBoolean("closed"));
                    mePartyRecordV600(host,closed?"🔒":"🎤",name,
                        closed?"Closed room":"Hosted by your account",
                        closed?null:()->openVerifiedMeRoom9981(id));
                    if(++count>=12)break;
                }
                if(count==0)mePartyRecordV600(host,"🎤","No hosted Party rooms yet",
                    "Open Party to create one",this::openPartyActivity);
            }).addOnFailureListener(e->{host.removeAllViews();mePartyRecordV600(host,"🎤",
                    "Hosted rooms unavailable","Open Party or retry",this::openPartyActivity);});
    }
    private void showMeRoomHistory9981(String tab){
        if(firebaseAuth==null||firebaseAuth.getCurrentUser()==null){login();return;}
        SharedPreferences p=getSharedPreferences("king_party",MODE_PRIVATE);
        java.util.List<KingMeRoomHistory9981.Room> rooms9981=KingMeRoomHistory9981.from(
            p.getStringSet("favorite_rooms_940",new HashSet<>()),
            p.getStringSet("recent_rooms_940",new HashSet<>()),tab);
        if(rooms9981.isEmpty()){
            new AlertDialog.Builder(this).setTitle("Me • "+tab+" rooms")
                .setMessage("No saved live rooms. Tap ★ Favorite in a real Party room.")
                .setPositiveButton("Open Party",(d,w)->openPartyActivity())
                .setNegativeButton("Close",null).show();return;
        }
        String[] labels=new String[rooms9981.size()];
        for(int i=0;i<rooms9981.size();i++)labels[i]=rooms9981.get(i).label();
        new AlertDialog.Builder(this).setTitle("Me • "+tab+" rooms")
            .setItems(labels,(d,index)->openVerifiedMeRoom9981(rooms9981.get(index).id))
            .setPositiveButton("Party",(d,w)->openPartyActivity())
            .setNegativeButton("Close",null).show();
    }
    private void openVerifiedMeRoom9981(String roomId9981){
        if(firestore==null||firebaseAuth==null||firebaseAuth.getCurrentUser()==null
                ||!KingMeRoomHistory9981.validId(roomId9981)){
            Toast.makeText(this,"Sign in to enter a real Party",Toast.LENGTH_LONG).show();return;
        }
        final String account9981=firebaseAuth.getCurrentUser().getUid();
        firestore.collection("live_rooms").document(roomId9981)
            .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(snapshot->{
                if(isFinishing()||isDestroyed()||firebaseAuth.getCurrentUser()==null
                   ||!account9981.equals(firebaseAuth.getCurrentUser().getUid()))return;
                if(!snapshot.exists()||Boolean.TRUE.equals(snapshot.getBoolean("closed"))){
                    Toast.makeText(this,"Party room has closed",Toast.LENGTH_LONG).show();return;
                }
                Intent intent=new Intent(this,PartyActivity.class);
                intent.putExtra("directRoomId",roomId9981);
                startActivity(intent); // PartyActivity still checks privacy/password and membership.
            }).addOnFailureListener(e->Toast.makeText(this,
                "Unable to verify Party room online",Toast.LENGTH_LONG).show());
    }

'''+s[end:]
anchor='    private void editProfile(){'
assert s.count(anchor)==1
s=s.replace(anchor,'''    /** Real Me sub-option route; preserves normal MainActivity navigation and login. */
    private boolean routeMeAction9981(String action){
        if(action==null||firebaseAuth==null||firebaseAuth.getCurrentUser()==null)return false;
        switch(action){
            case "edit_profile":editProfile();return true;
            case "account_delete":profile();requestAccountDeletionFlow();return true;
            case "safety":privacySafetyPage();return true;
            case "profile_settings":settingsPage();return true;
            case "favorites":profile();showMeRoomHistory9981("favorites");return true;
            case "recent_rooms":profile();showMeRoomHistory9981("recent");return true;
            case "backpack":backpackPage();return true;
            case "help":helpCenterPage();return true;
            default:return false;
        }
    }
'''+anchor,1)
f.write_text(s)
# Replace nested Me ghost Enabled pages with correct screen routing and explicit NO-GO.
deep=src/'KingDeepFlowActivity.java'
d=deep.read_text()
key='''            case "detail_friend_broadcast": openPartyPanel940("broadcast");break;
            default:detail(route);break;'''
assert d.count(key)==1
d=d.replace(key,'''            case "detail_friend_broadcast": openPartyPanel940("broadcast");break;
            case "detail_edit_profile":openMeAction9981("edit_profile");break;
            case "detail_delete_account":openMeAction9981("account_delete");break;
            case "detail_profile_safety":case "detail_help_safety":case "detail_blocked":
            case "detail_reports":openMeAction9981("safety");break;
            case "detail_language":language9981();break;
            case "detail_storage":storage9981();break;
            case "detail_about":about9981();break;
            case "detail_help_login":help9981("login");break;
            case "detail_help_party":help9981("party");break;
            case "detail_help_gift":help9981("gift");break;
            case "detail_help_games":help9981("games");break;
            case "detail_rules_games":fairPlay9981();break;
            default:detail(route);break;''',1)
begin=d.index('    private void detail(String r){')
finish=d.index('\n',begin)
assert finish-begin>400
d=d[:begin]+'''    private void detail(String r){
        String name=r.replace("detail_","").replace('_',' ');
        hero("Me • "+titleCase(name),"This sub-option is not yet backed by a verified live service.");
        section("Real available actions");
        if(r.startsWith("detail_rules_")){
            chip("No published legal document link is configured for this option.",()->{});
            row("📜","Current community rules","Read current in-app rules","rules");
        }else if(r.startsWith("detail_vip_")||r.startsWith("detail_noble_")){
            row("👑","Verified level information","No unverified VIP or paid privileges","level_center");
            row("💎","VIP eligibility","See current TEST limitations","vip_rules");
        }else if("detail_bind_phone".equals(r)){
            chip("Phone linking requires real Firebase OTP verification; no fake binding is done.",()->{});
            row("🔐","Account Security","Review your signed-in account","settings_account");
            row("❓","Login Help","Open diagnostics and sign-in advice","help");
        }else{
            chip("This is not a functioning payment, privacy or account setting yet.",()->{});
            row("❓","Open Help","Working diagnostics and contacts","help");
        }
    }
    private void openMeAction9981(String action){
        if(me==null){toast("Sign in for your Me account options");return;}
        Intent i=new Intent(this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra("meAction9981",action);startActivity(i);
    }
    private void language9981(){
        hero("🌍 Language & Region","Open Android language settings; a full KING translation pack is not available yet.");
        row("🌐","Android language settings","Open real system settings",()->{
            try{startActivity(new Intent(Settings.ACTION_LOCALE_SETTINGS));}
            catch(Exception e){toast("Open Android Settings → Languages");}
        });
    }
    private void storage9981(){
        hero("🧹 App Storage","Review KING Plus device storage in Android; this does not delete your cloud data.");
        row("📦","Android app settings","Manage cache or app data",()->{
            try{Intent i=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                i.setData(android.net.Uri.parse("package:"+getPackageName()));startActivity(i);}
            catch(Exception e){toast("Open Android Settings → Apps → KING Plus");}
        });
    }
    private void about9981(){
        String version="Unknown";
        try{android.content.pm.PackageInfo p=getPackageManager().getPackageInfo(getPackageName(),0);
            if(p.versionName!=null)version=p.versionName;}catch(Exception ignored){}
        hero("ℹ KING Plus","Installed version "+version+" • "+getPackageName());
        chip("Party/Games/Gifts TEST version; paid recharge/VIP production not enabled.",()->{});
        row("🛡","Community safety","Current in-app rules","rules");
        row("❓","Help Center","Real support and diagnostics","help");
    }
    private void help9981(String topic){
        switch(topic){
            case "login":
                hero("🔐 Login Help","Google and phone OTP require correctly configured Firebase provider.");
                row("🔌","Connection Diagnostics","Test real Firebase connection",
                    ()->startActivity(new Intent(this,KingConnection983Activity.class)));
                row("🔐","Account Settings","Sign-in and device history","settings_account");break;
            case "party":
                hero("🎤 Party Help","Join a live room, sit on a Mic seat and approve microphone permission.");
                row("🎤","Open live Party","Real host, cohost and mic options",
                    ()->KingNav.openRoot(this,0));break;
            case "gift":
                hero("🎁 Gift Help","Free Gift catalog and receiver ACK need connected accounts.");
                row("🎁","Gift Center","Open functional catalog",this::openGiftCenter);break;
            case "games":
                hero("🎮 Games Help","Ready, Start, Submit and Rematch with real Room members.");
                row("🎮","Game Center","Open actual playable options","game_center");break;
        }
        row("❓","All help","Other support","help");
    }
    private void fairPlay9981(){
        hero("🎮 Fair Play","FREE social multiplayer, not wagering.");
        chip("Only living Ready members should move; 4-of-5 Bingo cannot win.",()->{});
        chip("Random scores require trusted server authority for full anti-cheat.",()->{});
        row("🎮","Game Center","Play a real room game","game_center");
    }'''+d[finish:]
assert 'toggle("Enabled","detail_"+r,true)' not in d
deep.write_text(d)
shutil.copyfile(tools/'KingMeRoomHistory9981.java',src/'KingMeRoomHistory9981.java')
build=root/'app/build.gradle';g=build.read_text()
g,n=re.subn(r"versionCode\s+259\s*;\s*versionName\s+'[^']+'",
  "versionCode 260; versionName '9.9.81-real-me-page-nested-test'",g)
assert n==1,"Expected signed v9.9.80 Android"
build.write_text(g)
assert 'KingLiveBingo9980Activity' in (root/'app/src/main/AndroidManifest.xml').read_text()
assert 'KingGiftPlayedHistory9969' in (src/'PartyActivity.java').read_text()
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print("PASS Me page favorites/recent/hosted-room account filters and deep nested route integration")
