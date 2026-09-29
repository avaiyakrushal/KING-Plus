from pathlib import Path
import re

MAIN = Path('app/src/main/java/com/kingplus/social/MainActivity.java')
BUILD = Path('app/build.gradle')

src = MAIN.read_text(encoding='utf-8')

# 1) BoloHi-style Me header: balances + settings.
old = '''        TextView actions=new TextView(this);\n        actions.setText("⚙");\n        actions.setTextSize(24);\n        actions.setGravity(Gravity.CENTER);\n        actions.setTextColor(0xff332a3d);\n        actions.setOnClickListener(v->settingsPage());\n        top.addView(actions,new LinearLayout.LayoutParams(dp(52),dp(52)));\n        root.addView(top);\n'''
new = '''        TextView balance=new TextView(this);\n        balance.setText("🔸 "+giftCount+"   💎 "+coinBalance);\n        balance.setTextSize(13);\n        balance.setGravity(Gravity.CENTER);\n        balance.setTextColor(0xff5e496f);\n        balance.setBackground(background(0xffeee8f8,16));\n        balance.setOnClickListener(v->walletPage());\n        top.addView(balance,new LinearLayout.LayoutParams(dp(142),dp(40)));\n        TextView actions=new TextView(this);\n        actions.setText("⚙");\n        actions.setTextSize(24);\n        actions.setGravity(Gravity.CENTER);\n        actions.setTextColor(0xff332a3d);\n        actions.setOnClickListener(v->settingsPage());\n        top.addView(actions,new LinearLayout.LayoutParams(dp(48),dp(48)));\n        root.addView(top);\n'''
if old not in src:
    raise SystemExit('Me header template changed')
src = src.replace(old, new, 1)

# 2) Photo/edit/share quick actions below the hero card.
old = '''        hero.setOnClickListener(v->editProfile());\n        content.addView(hero,new LinearLayout.LayoutParams(-1,dp(118)));\n\n        LinearLayout stats=new LinearLayout(this);\n'''
new = '''        hero.setOnClickListener(v->editProfile());\n        content.addView(hero,new LinearLayout.LayoutParams(-1,dp(118)));\n\n        LinearLayout profileActions=new LinearLayout(this);\n        profileActions.setGravity(Gravity.CENTER);\n        profileActions.setPadding(0,dp(5),0,dp(2));\n        addMeChip(profileActions,"📷 Photo",this::editProfile);\n        addMeChip(profileActions,"✎ Edit",this::editProfile);\n        addMeChip(profileActions,"↗ Share",this::shareMyProfile);\n        content.addView(profileActions,new LinearLayout.LayoutParams(-1,dp(48)));\n\n        LinearLayout stats=new LinearLayout(this);\n'''
if old not in src:
    raise SystemExit('Me hero template changed')
src = src.replace(old, new, 1)

# 3) BoloHi-style profile counters + About/Photos/Videos tabs.
old = '''        addProfileStat(stats,String.valueOf(following),"Following",()->peoplePage("Following"));\n        addProfileStat(stats,String.valueOf(followers),"Followers",()->peoplePage("Followers"));\n        addProfileStat(stats,String.valueOf(friends),"Friends",()->peoplePage("Friends"));\n        addProfileStat(stats,String.valueOf(charm),"Charm",this::rankingsPage);\n        LinearLayout.LayoutParams stp=new LinearLayout.LayoutParams(-1,dp(74)); stp.setMargins(0,dp(8),0,0);\n        content.addView(stats,stp);\n\n        LinearLayout wallet=new LinearLayout(this);\n'''
new = '''        addProfileStat(stats,String.valueOf(followers),"Followers",()->peoplePage("Followers"));\n        addProfileStat(stats,String.valueOf(following),"Following",()->peoplePage("Following"));\n        addProfileStat(stats,String.valueOf(friends),"Friends",()->peoplePage("Friends"));\n        String coupling=getPreferences(0).getString("coupling_name","");\n        addProfileStat(stats,coupling.isEmpty()?"0":"1","Coupling",this::couplingPage);\n        LinearLayout.LayoutParams stp=new LinearLayout.LayoutParams(-1,dp(74)); stp.setMargins(0,dp(8),0,0);\n        content.addView(stats,stp);\n\n        LinearLayout socialMeta=new LinearLayout(this);\n        socialMeta.setGravity(Gravity.CENTER);\n        socialMeta.setPadding(0,dp(4),0,dp(4));\n        addMeChip(socialMeta,"♥ "+charm+" Likes",this::rankingsPage);\n        addMeChip(socialMeta,"VIP",this::vipPage);\n        addMeChip(socialMeta,"Lv."+level,this::levelPage);\n        content.addView(socialMeta,new LinearLayout.LayoutParams(-1,dp(48)));\n\n        LinearLayout tabs=new LinearLayout(this);\n        tabs.setGravity(Gravity.CENTER);\n        tabs.setPadding(0,dp(4),0,dp(6));\n        addMeTab(tabs,"About",this::profileAboutPage);\n        addMeTab(tabs,"Photos",this::galleryPage);\n        addMeTab(tabs,"Videos",this::videoPage);\n        content.addView(tabs,new LinearLayout.LayoutParams(-1,dp(52)));\n\n        LinearLayout wallet=new LinearLayout(this);\n'''
if old not in src:
    raise SystemExit('Me stats template changed')
src = src.replace(old, new, 1)

# 4) Replace the placeholder gallery/menu rows with working destinations.
old = '''        meRow(list,"✏","Edit Profile","Photo, name, bio & hometown",this::editProfile);\n        meRow(list,"🖼","My Gallery","Photos and profile moments",()->simplePage("My Gallery","Your KING Plus profile gallery will appear here."));\n        meRow(list,"📅","Daily Check-in","Claim daily rewards",this::dailyCheckInPage);\n'''
new = '''        meRow(list,"💎","My Wallet","Coins, gifts and transactions",this::walletPage);\n        meRow(list,"🏆","My Level","XP, level and achievements",this::levelPage);\n        meRow(list,"🖼","My Gallery","Profile photos and Moments",this::galleryPage);\n        meRow(list,"🎬","My Videos","Saved profile videos",this::videoPage);\n        meRow(list,"✏","Edit Profile","Photo, name, bio, hometown & tags",this::editProfile);\n        meRow(list,"↗","Share Profile","Share KING Plus name and ID",this::shareMyProfile);\n        meRow(list,"📅","Daily Check-in","Claim daily rewards",this::dailyCheckInPage);\n'''
if old not in src:
    raise SystemExit('Me menu template changed')
src = src.replace(old, new, 1)

# 5) Helper/destination screens.
marker = '    private void meRow(LinearLayout host,String icon,String title,String subtitle,Runnable action){\n'
helpers = r'''    private void addMeChip(LinearLayout row,String label,Runnable action){
        TextView v=new TextView(this);
        v.setText(label); v.setTextSize(12); v.setTextColor(0xff4b3f57); v.setGravity(Gravity.CENTER);
        v.setBackground(background(0xffeee8f8,16));
        if(action!=null)v.setOnClickListener(x->action.run());
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(36),1); p.setMargins(dp(3),0,dp(3),0); row.addView(v,p);
    }

    private void addMeTab(LinearLayout row,String label,Runnable action){
        TextView v=new TextView(this);
        v.setText(label); v.setTextSize(14); v.setTypeface(null,Typeface.BOLD); v.setTextColor(0xff30283a); v.setGravity(Gravity.CENTER);
        v.setBackground(background(Color.WHITE,14));
        if(action!=null)v.setOnClickListener(x->action.run());
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(42),1); p.setMargins(dp(3),0,dp(3),0); row.addView(v,p);
    }

    private void shareMyProfile(){
        shareText("KING Plus • "+displayName+" • ID: "+roomId(displayName));
    }

    private void profileAboutPage(){
        screen="profile_about"; base("About","KING Plus profile details");
        String bio=getPreferences(0).getString("bio","Love music, games and new friends ✨");
        String hometown=getPreferences(0).getString("hometown","India");
        String birthday=getPreferences(0).getString("birthday","2000-01-01");
        String tags=getPreferences(0).getString("tags","Music, Games");
        text("👤 "+displayName,24,Color.WHITE,true); text("ID: "+roomId(displayName),14,MUTED,false);
        text("💬 "+bio,16,Color.WHITE,false); text("📍 "+(hometown.isEmpty()?"India":hometown),15,Color.WHITE,false);
        text("🎂 "+birthday,15,Color.WHITE,false); text("🏷 "+tags,15,Color.WHITE,false);
        text("🕘 Online Time: "+getPreferences(0).getString("online_time","8 PM - 12 AM"),15,Color.WHITE,false);
        button("Edit Profile",PURPLE,this::editProfile); button("Back to Me",CARD,this::profile);
    }

    private void galleryPage(){
        screen="profile_gallery"; base("My Gallery","Photos and profile Moments");
        String photo=getPreferences(0).getString("profile_photo","");
        if(!photo.isEmpty()){
            ImageView img=new ImageView(this); img.setAdjustViewBounds(true); img.setMaxHeight(dp(320));
            try{img.setImageURI(Uri.parse(photo));page.addView(img,new LinearLayout.LayoutParams(-1,-2));}catch(Exception ignored){}
        }
        button("＋ Add Photo / Moment",PURPLE,this::composePost); text("Photos & Moments",18,Color.WHITE,true); showPosts();
        button("Back to Me",CARD,this::profile);
    }

    private void videoPage(){
        screen="profile_videos"; base("My Videos","Saved profile videos");
        String videos=getPreferences(0).getString("profile_videos","");
        if(videos.isEmpty()) text("No profile videos yet.",15,MUTED,false);
        else { String[] all=videos.split("\\|\\|\\|"); for(int i=0;i<all.length;i++) if(!all[i].trim().isEmpty()) text("🎬 Video "+(i+1)+" • saved on device",16,Color.WHITE,false); }
        button("＋ Add Video",PURPLE,()->{ Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT); i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("video/*"); i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION); startActivityForResult(i,26); });
        button("Back to Me",CARD,this::profile);
    }

    private void couplingPage(){
        screen="coupling"; base("Coupling","KING Plus connection");
        String current=getPreferences(0).getString("coupling_name","");
        text(current.isEmpty()?"No coupling yet":"💞 Coupled with "+current,20,Color.WHITE,true);
        button(current.isEmpty()?"Set Coupling":"Change Coupling",PURPLE,()->{
            final EditText e=new EditText(this); e.setHint("User name");
            new AlertDialog.Builder(this).setTitle("Coupling").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{ String n=e.getText().toString().trim(); if(!n.isEmpty()) getPreferences(0).edit().putString("coupling_name",n).apply(); couplingPage(); }).show();
        });
        if(!current.isEmpty()) button("Remove Coupling",CARD,()->{getPreferences(0).edit().remove("coupling_name").apply();couplingPage();});
        button("Back to Me",CARD,this::profile);
    }

'''
if marker not in src:
    raise SystemExit('Me helper insertion point changed')
src = src.replace(marker, helpers + marker, 1)

# 6) Persist selected profile videos.
photo_handler = '''        if (requestCode == 24 && resultCode == RESULT_OK && data != null && data.getData() != null) {\n            Uri uri=data.getData();\n            try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); getPreferences(0).edit().putString("profile_photo",uri.toString()).apply(); Toast.makeText(this,"Profile photo updated",Toast.LENGTH_SHORT).show(); profile(); }\n            catch (Exception ex) { Toast.makeText(this,"Profile photo could not be saved",Toast.LENGTH_SHORT).show(); }\n            return;\n        }\n'''
video_handler = photo_handler + '''        if (requestCode == 26 && resultCode == RESULT_OK && data != null && data.getData() != null) {\n            Uri uri=data.getData();\n            try {\n                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);\n                String old=getPreferences(0).getString("profile_videos","");\n                String updated=old.isEmpty()?uri.toString():old+"|||"+uri.toString();\n                getPreferences(0).edit().putString("profile_videos",updated).apply();\n                Toast.makeText(this,"Video added to profile",Toast.LENGTH_SHORT).show();\n                videoPage();\n            } catch (Exception ex) { Toast.makeText(this,"Video could not be saved",Toast.LENGTH_SHORT).show(); }\n            return;\n        }\n'''
if photo_handler not in src:
    raise SystemExit('Profile photo result handler changed')
src = src.replace(photo_handler, video_handler, 1)

# 7) Online-time field belongs to Edit Profile and is visible under About.
tags_line = '        EditText tags=input("Tags (music, games, friends)",getPreferences(0).getString("tags","Music, Games"));\n'
if tags_line not in src:
    raise SystemExit('Edit Profile tags field changed')
src = src.replace(tags_line, tags_line + '        EditText onlineTime=input("Online time",getPreferences(0).getString("online_time","8 PM - 12 AM"));\n', 1)
old_save = '.putString("tags",tags.getText().toString().trim()).apply();'
new_save = '.putString("tags",tags.getText().toString().trim()).putString("online_time",onlineTime.getText().toString().trim()).apply();'
if old_save not in src:
    raise SystemExit('Edit Profile save chain changed')
src = src.replace(old_save, new_save, 1)

MAIN.write_text(src, encoding='utf-8')

gradle = BUILD.read_text(encoding='utf-8')
gradle = re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 43; versionName '3.1.2'", gradle)
BUILD.write_text(gradle, encoding='utf-8')

print('Prepared KING Plus v3.1.2 BoloHi-style Me/Profile complete')
