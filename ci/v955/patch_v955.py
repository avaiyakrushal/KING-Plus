from pathlib import Path
import sys

root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'

def replace_method(path, signature, replacement):
    p=pkg/path
    s=p.read_text()
    start=s.find(signature)
    if start<0: raise SystemExit(f'{path}: signature not found: {signature}')
    brace=s.find('{',start)
    depth=0; state='code'; quote=''; esc=False; i=brace; end=None
    while i<len(s):
        ch=s[i]; nx=s[i+1] if i+1<len(s) else ''
        if state=='string':
            if esc: esc=False
            elif ch=='\\': esc=True
            elif ch==quote: state='code'
        elif state=='line':
            if ch=='\n': state='code'
        elif state=='block':
            if ch=='*' and nx=='/': state='code'; i+=1
        else:
            if ch=='/' and nx=='/': state='line'; i+=1
            elif ch=='/' and nx=='*': state='block'; i+=1
            elif ch in ('"',"'"): state='string'; quote=ch
            elif ch=='{': depth+=1
            elif ch=='}':
                depth-=1
                if depth==0: end=i+1; break
        i+=1
    if end is None: raise SystemExit(f'{path}: method end missing')
    p.write_text(s[:start]+replacement+s[end:])

replace_method('CommunityHubActivity.java','private void familyJoinCreate()',r'''private void familyJoinCreate(){
        TextView intro955=tv("👑 Create or Join a KING Family",20,DARK,true);intro955.setGravity(Gravity.CENTER);intro955.setBackground(bg(0xffffefd0,18));body.addView(intro955,new LinearLayout.LayoutParams(-1,dp(60)));
        TextView note955=tv("Families include members, Family Chat, Lucky Bag, activity history, level and treasury.",12,MUTED,false);note955.setGravity(Gravity.CENTER);note955.setPadding(dp(10),dp(6),dp(10),dp(8));body.addView(note955,new LinearLayout.LayoutParams(-1,dp(62)));

        body.addView(tv("Create Family",16,DARK,true));
        EditText name=new EditText(this);name.setHint("Family name • 3–24 characters");name.setSingleLine(true);name.setMaxLines(1);name.setBackground(bg(CARD,14));name.setPadding(dp(12),0,dp(12),0);body.addView(name,new LinearLayout.LayoutParams(-1,dp(52)));
        TextView create=button("👑 Create Family",()->{String n=name.getText().toString().trim();if(n.length()<3||n.length()>24){toast("Family name must be 3–24 characters");return;}new AlertDialog.Builder(this).setTitle("Create Family?").setMessage(n+"\\n\\nYou will become the Family owner.").setPositiveButton("Create",(d,w)->createFamily(n,0)).setNegativeButton("Cancel",null).show();});LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(48));cp.setMargins(0,dp(8),0,dp(18));body.addView(create,cp);

        body.addView(tv("Join with Family Code",16,DARK,true));
        EditText code=new EditText(this);code.setHint("6-character Family code");code.setSingleLine(true);code.setAllCaps(true);code.setBackground(bg(CARD,14));code.setPadding(dp(12),0,dp(12),0);body.addView(code,new LinearLayout.LayoutParams(-1,dp(52)));
        TextView join=button("Join Family",()->{String c=code.getText().toString().trim().toUpperCase(Locale.US);if(c.length()!=6){toast("Enter a valid 6-character code");return;}joinFamily(c);});LinearLayout.LayoutParams jp=new LinearLayout.LayoutParams(-1,dp(48));jp.setMargins(0,dp(8),0,0);body.addView(join,jp);
        TextView privacy955=tv("Only people with the exact Family code can join. The owner can remove members.",11,MUTED,false);privacy955.setGravity(Gravity.CENTER);LinearLayout.LayoutParams pp955=new LinearLayout.LayoutParams(-1,dp(52));pp955.setMargins(0,dp(8),0,0);body.addView(privacy955,pp955);
    }''')

replace_method('CommunityHubActivity.java','private void createFamily(String name,int attempt)',r'''private void createFamily(String name,int attempt){
        if(name==null)name="";name=name.trim();
        if(name.length()<3||name.length()>24){toast("Family name must be 3–24 characters");return;}
        if(me==null||db==null){toast("Sign in to create a Family");return;}
        if(attempt>5){new AlertDialog.Builder(this).setTitle("Could not create Family").setMessage("We could not reserve a unique Family code. Please try again.").setPositiveButton("Retry",(d,w)->createFamily(name,0)).setNegativeButton("Close",null).show();return;}
        final String familyName955=name;final String code=newCode();DocumentReference r=db.collection("families").document(code);
        final AlertDialog loading955=familyLoading955("Creating "+familyName955+"…");
        r.get().addOnSuccessListener(x->{
            if(x.exists()){if(loading955.isShowing())loading955.dismiss();createFamily(familyName955,attempt+1);return;}
            Map<String,Object>f=new HashMap<>();f.put("name",familyName955);f.put("code",code);f.put("ownerUid",me.getUid());f.put("ownerName",displayName);f.put("level",1);f.put("treasury",0);f.put("memberCount",1);f.put("createdAt",FieldValue.serverTimestamp());f.put("updatedAt",FieldValue.serverTimestamp());
            r.set(f).addOnSuccessListener(v->{
                Map<String,Object>mem=new HashMap<>();mem.put("uid",me.getUid());mem.put("name",displayName);mem.put("role","owner");mem.put("joinedAt",FieldValue.serverTimestamp());
                r.collection("members").document(me.getUid()).set(mem).addOnSuccessListener(z->{
                    Map<String,Object>a=new HashMap<>();a.put("type","created");a.put("text",displayName+" created the Family");a.put("uid",me.getUid());a.put("createdAt",FieldValue.serverTimestamp());
                    r.collection("activities").add(a).addOnCompleteListener(done->{if(loading955.isShowing())loading955.dismiss();saveFamily(code,familyName955);});
                }).addOnFailureListener(e->{if(loading955.isShowing())loading955.dismiss();r.delete();familyError955("Could not create owner membership",e,()->createFamily(familyName955,0));});
            }).addOnFailureListener(e->{if(loading955.isShowing())loading955.dismiss();familyError955("Family creation failed",e,()->createFamily(familyName955,0));});
        }).addOnFailureListener(e->{if(loading955.isShowing())loading955.dismiss();familyError955("Could not reserve Family code",e,()->createFamily(familyName955,0));});
    }''')

replace_method('CommunityHubActivity.java','private void joinFamily(String code)',r'''private void joinFamily(String code){
        code=code==null?"":code.trim().toUpperCase(Locale.US);
        if(code.length()!=6){toast("Enter a valid 6-character code");return;}
        if(me==null||db==null){toast("Sign in to join a Family");return;}
        final String familyCode955=code;DocumentReference r=db.collection("families").document(familyCode955);final AlertDialog loading955=familyLoading955("Checking Family "+familyCode955+"…");
        r.get().addOnSuccessListener(f->{
            if(!f.exists()){if(loading955.isShowing())loading955.dismiss();new AlertDialog.Builder(this).setTitle("Family not found").setMessage("Check the 6-character code and try again.").setPositiveButton("OK",null).show();return;}
            String familyName955=safe(f.getString("name"),"KING Family"),ownerUid955=safe(f.getString("ownerUid"),"");
            if(me.getUid().equals(ownerUid955)){if(loading955.isShowing())loading955.dismiss();saveFamily(familyCode955,familyName955);return;}
            r.collection("members").document(me.getUid()).get().addOnSuccessListener(existing->{
                if(existing.exists()){if(loading955.isShowing())loading955.dismiss();saveFamily(familyCode955,familyName955);return;}
                Map<String,Object>m=new HashMap<>();m.put("uid",me.getUid());m.put("name",displayName);m.put("role","member");m.put("joinedAt",FieldValue.serverTimestamp());
                r.collection("members").document(me.getUid()).set(m).addOnSuccessListener(v->{
                    Map<String,Object>a=new HashMap<>();a.put("type","joined");a.put("text",displayName+" joined the Family");a.put("uid",me.getUid());a.put("createdAt",FieldValue.serverTimestamp());
                    r.collection("activities").add(a);r.update("memberCount",FieldValue.increment(1),"updatedAt",FieldValue.serverTimestamp()).addOnCompleteListener(done->{if(loading955.isShowing())loading955.dismiss();saveFamily(familyCode955,familyName955);});
                }).addOnFailureListener(e->{if(loading955.isShowing())loading955.dismiss();familyError955("Could not join Family",e,()->joinFamily(familyCode955));});
            }).addOnFailureListener(e->{if(loading955.isShowing())loading955.dismiss();familyError955("Could not check membership",e,()->joinFamily(familyCode955));});
        }).addOnFailureListener(e->{if(loading955.isShowing())loading955.dismiss();familyError955("Family lookup failed",e,()->joinFamily(familyCode955));});
    }''')

p=pkg/'CommunityHubActivity.java';s=p.read_text()
marker='''    private void familyJoinCreate(){'''
helpers=r'''    private AlertDialog familyLoading955(String text){LinearLayout box=new LinearLayout(this);box.setGravity(Gravity.CENTER_VERTICAL);box.setPadding(dp(18),dp(10),dp(18),dp(10));android.widget.ProgressBar spin=new android.widget.ProgressBar(this);box.addView(spin,new LinearLayout.LayoutParams(dp(34),dp(34)));TextView label=tv(text,13,MUTED,true);box.addView(label,new LinearLayout.LayoutParams(0,dp(52),1));AlertDialog d=new AlertDialog.Builder(this).setTitle("KING Family").setView(box).setCancelable(false).create();d.show();return d;}
    private void familyError955(String title,Exception e,Runnable retry){new AlertDialog.Builder(this).setTitle(title).setMessage(e==null?"Please try again.":String.valueOf(e.getMessage())).setPositiveButton("Retry",(d,w)->retry.run()).setNegativeButton("Close",null).show();}
    
'''
if marker not in s: raise SystemExit('family helper marker missing')
s=s.replace(marker,helpers+marker,1);p.write_text(s)

replace_method('KingDeepFlowActivity.java','private void settings()',r'''private void settings(){
        hero("⚙ KING Plus Settings","Account, privacy, notifications and media controls");
        section("Account & Safety");
        row("🔐","Account & Security","Login devices, account identity and safety","settings_account");
        row("🛡","Privacy","Profile, room, block and discoverability","settings_privacy");
        section("App Experience");
        row("🔔","Notifications","Message, room, gifts and activity notifications","settings_notifications");
        row("🌐","Network & Media","Voice/video quality, data and reconnect","settings_network");
        row("🌍","Language","App language and content language","detail_language");
        row("🧹","Storage","Cache and downloaded media","detail_storage");
        section("About");
        row("ℹ","About KING Plus","Version, terms and policies","detail_about");
        row("❓","Help Center","Login, Party, gifts, games and safety","help");
        row("📜","Rules & Policies","Community, privacy, terms and fair play","rules");
    }''')

replace_method('KingDeepFlowActivity.java','private void settingsAccount()',r'''private void settingsAccount(){
        com.google.firebase.auth.FirebaseUser u955=null;try{u955=com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();}catch(Exception ignored){}
        hero("🔐 Account & Security",u955==null?"Not signed in":"Signed in • "+(u955.getEmail()==null?"KING account":u955.getEmail()));
        toggle("Remember login","remember_login",true);
        section("Identity");
        row("📱","Login devices & history","Current device • "+android.os.Build.MODEL,this::loginDevices940);
        row("🔑","Change / bind phone","Mobile identity flow","detail_bind_phone");
        section("Account Controls");
        row("🛡","Safety Center","Block, reports and account protection","detail_help_safety");
        row("🗑","Account deletion","Review consequences before deleting","detail_delete_account");
    }''')

replace_method('KingDeepFlowActivity.java','private void settingsPrivacy()',r'''private void settingsPrivacy(){
        hero("🛡 Privacy","Control who can find, contact and invite you");
        section("Discoverability");
        toggle("Allow people to find me","find_me",true);toggle("Show online status","online_status",true);
        section("Messages & Rooms");
        toggle("Allow room invites","room_invites",true);toggle("Allow private messages","private_messages",true);toggle("Show entrance effects","entrance_fx",true);
        section("Safety");
        row("🚫","Blocked users","Review blocked accounts","detail_blocked");row("⚑","Reports & safety","Report history and safety center","detail_reports");
    }''')

replace_method('KingDeepFlowActivity.java','private void settingsNotifications()',r'''private void settingsNotifications(){
        hero("🔔 Notifications","Choose which KING activity can alert you");
        section("Social");toggle("Messages","notify_messages",true);toggle("Followers / Friends","notify_social",true);
        section("Party & Gifts");toggle("Room invitations","notify_room_invites",true);toggle("Gifts and VIP","notify_gifts",true);toggle("Game invitations","notify_games",true);
        section("System");
        row("⚙","Android notification settings","Open system notification permissions",()->{try{Intent i=new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);i.putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName());startActivity(i);}catch(Exception e){toast("Open Android Settings → Notifications");}});
    }''')

replace_method('KingDeepFlowActivity.java','private void settingsNetwork()',r'''private void settingsNetwork(){
        boolean online955=KingNetwork.online(this);
        hero("🌐 Network & Media",(online955?"● Online":"○ Offline")+" • "+android.os.Build.MODEL);
        section("Reconnect & Data");toggle("Auto reconnect voice/video","auto_reconnect",true);toggle("Low-data voice mode","low_data_voice",false);
        section("Visual Effects");toggle("Auto-play gift effects","autoplay_gifts",true);toggle("Auto-play entrance effects","autoplay_entrance",true);
        section("Diagnostics");row("🎙","Voice diagnostics","Open voice room test","video_center");row("📹","Video diagnostics","Open video room test","video_center");
        row("↻","Connection status",online955?"Network available":"Offline • realtime features pause",()->toast(KingNetwork.online(this)?"KING Plus is online":"KING Plus is offline"));
    }''')

replace_method('KingDeepFlowActivity.java','private void help()',r'''private void help(){
        hero("❓ Help Center","Quick guides for the main KING Plus flows");
        section("Account");row("🔑","Login / OTP","Google login, mobile OTP and account setup","detail_help_login");
        section("Live Party");row("🎤","Party Room","Seats, mic, host, co-host and room controls","detail_help_party");row("🎁","Gifts","Recipients, quantity, history, wall and VIP unlocks","detail_help_gift");
        section("Games & Progress");row("🎮","Games","Offline Ludo and realtime room multiplayer","detail_help_games");row("💎","VIP / Levels","VIP 0–50, Normal Level 1–99 and Noble tiers","vip_rules");
        section("Safety");row("⚑","Block, report & moderation","Keep rooms and social activity safe","detail_help_safety");row("📜","Rules & Policies","Community, privacy, terms and fair play","rules");
    }''')

replace_method('KingDeepFlowActivity.java','private void rules()',r'''private void rules(){
        hero("📜 Rules & Policies","KING Plus community safety, privacy and fair play");
        TextView note955=tv("Use KING Plus respectfully. Harassment, spam, impersonation, illegal content and abuse of rooms/games can lead to moderation.",12,sub(),false);note955.setPadding(dp(12),dp(8),dp(12),dp(8));note955.setBackground(bg(0xfffff2c8,14));body.addView(note955,new LinearLayout.LayoutParams(-1,dp(82)));
        section("Community");row("🛡","Community Rules","Respect, harassment, spam and safety","detail_rules_community");row("⚑","Moderation & Reporting","How block, kick, ban and reports work","detail_help_safety");
        section("Legal & Privacy");row("🔐","Privacy Policy","Profile, messages and account data","detail_rules_privacy");row("📄","Terms of Service","Account and service terms","detail_rules_terms");
        section("Games");row("🎮","Game Fair Play","Room game conduct and abuse prevention","detail_rules_games");
    }''')

gradle=root/'app/build.gradle';g=gradle.read_text()
old="versionCode 145; versionName '9.5.4-theme-polish'"
if old not in g: raise SystemExit('v9.5.4 version marker missing')
gradle.write_text(g.replace(old,"versionCode 146; versionName '9.5.5-family-settings-polish'",1))
print('KING Plus v9.5.5 Family onboarding and Settings/Help/Rules polish applied')
