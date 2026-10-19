package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deep, stateful navigation layer for KING Plus.
 * Keeps nested option -> sub-option -> detail flows inside one predictable stack,
 * while delegating live/social/game work to the real activities that own it.
 */
public class KingDeepFlowActivity extends Activity {
    private static final int YELLOW=0xffffdf19, INK=0xff211b29, MUTED=0xff81798b, WHITE=0xffffffff;
    private final ArrayDeque<String> stack=new ArrayDeque<>();
    private String route="profile_menu", roomId="", roomName="KING Room";
    private LinearLayout body;
    private SharedPreferences prefs, mainPrefs;
    private FirebaseFirestore db; private FirebaseUser me;

    @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("king_deep_flow",MODE_PRIVATE);mainPrefs=getSharedPreferences("MainActivity",MODE_PRIVATE);try{db=FirebaseFirestore.getInstance();me=FirebaseAuth.getInstance().getCurrentUser();}catch(Exception ignored){}String r=getIntent().getStringExtra("route");if(r!=null&&!r.isEmpty())route=r;roomId=safe(getIntent().getStringExtra("roomId"),"");roomName=safe(getIntent().getStringExtra("roomName"),"KING Room");render();}
    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}
    private String safe(String s,String f){return s==null||s.trim().isEmpty()?f:s.trim();}
    private GradientDrawable bg(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    private TextView tv(String s,int z,int c,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);if(bold)t.setTypeface(null,Typeface.BOLD);t.setGravity(Gravity.CENTER_VERTICAL);t.setPadding(dp(12),dp(6),dp(12),dp(6));return t;}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    private boolean dark(){return route.startsWith("party_")||route.startsWith("gift_")||route.startsWith("game_")||route.startsWith("vip")||route.startsWith("noble")||route.startsWith("music_")||route.startsWith("video_");}
    private int fg(){return dark()?Color.WHITE:INK;} private int bgc(){return dark()?0xff171420:0xfff8f7fb;} private int card(){return dark()?0xff292535:WHITE;} private int sub(){return dark()?0xffaaa2b5:MUTED;}

    private void render(){
        boolean d=dark();LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(bgc());
        LinearLayout head=(LinearLayout)getLayoutInflater().inflate(R.layout.king_deepflow_header,root,false);
        TextView back=head.findViewById(R.id.king_ui_back);
        back.setText("‹");back.setTextSize(38);back.setTextColor(fg());
        back.setOnClickListener(v->back());
        TextView title=head.findViewById(R.id.king_ui_title);
        title.setText(titleFor(route));title.setTextSize(21);title.setTypeface(null,Typeface.BOLD);title.setTextColor(fg());
        TextView close=head.findViewById(R.id.king_ui_close);
        close.setText("✕");close.setTextSize(22);close.setTextColor(fg());
        if(route.equals("profile_menu"))close.setOnClickListener(v->finish());
        else close.setVisibility(View.GONE);
        root.addView(head);
        ScrollView sc=new ScrollView(this);body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(12),dp(8),dp(12),dp(30));sc.addView(body);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
        switch(route){
            case "agency": startActivity(new Intent(this,KingAgency9982Activity.class));finish();break;
            case "profile_menu": profileMenu();break; case "data_center": dataCenter();break;
            case "coin_mall": case "web_diamond": startActivity(new Intent(this,KingRecharge974Activity.class));finish();break;
            case "privilege_pack": privilegePack();break; case "privilege_shop": startActivity(new Intent(this,KingWardrobeActivity.class));break;
            case "vip": startActivity(new Intent(this,KingVipVisualActivity.class));finish();break; case "vip_rules": vipRules();break;
            case "noble": startActivity(new Intent(this,KingMeVerifiedPrestige9988Activity.class));finish();break;
            case "wallet": wallet();break; case "wallet_history": walletHistory();break; case "settings": settings();break; case "settings_account":settingsAccount();break;case "settings_privacy":openMeCloud9984("privacy");break;case "settings_notifications":openMeCloud9984("notifications");break;case "settings_network":settingsNetwork();break;
            case "help":help();break;case "rules":rules();break;case "party_play":partyPlay();break;case "party_templates":partyTemplates();break;case "party_events":partyEvents();break;case "party_tools":partyTools();break;case "party_settings":partySettings();break;case "party_management":partyManagement();break;case "party_admin":partyAdmin();break;
            case "ui_design_library": designLibrary9934();break;case "drawable_library": drawableLibrary9935();break;
            case "gift_center":giftCenter();break;case "gift_wall":giftWall();break;case "gift_history":giftHistory();break;case "gift_wish":giftWish();break;case "gift_lucky":giftLucky();break;
            case "game_center":gameCenter();break;case "game_hot":gameHot();break;case "game_party":gameParty();break;case "game_team":gameTeam();break;
            case "music_center":musicCenter();break;case "video_center":videoCenter();break;case "task_center":taskCenter();break;case "event_center":eventCenter();break;case "profile_center":profileCenter();break;case "level_center":levelCenter();break;case "medal_center":medalCenter();break;case "social_center":socialCenter();break;case "discover_center":discoverCenter();break;case "report_history":reportHistory9981();break;
            case "detail_frames": case "detail_entrance": case "detail_chat_bubble": case "detail_name_badge": startActivity(new Intent(this,KingWardrobeActivity.class));break;
            case "detail_music_library": openPartyPanel940("music");break;
            case "detail_song_request": openPartyPanel940("song");break;
            case "detail_ktv_queue": openPartyPanel940("ktv");break;
            case "detail_radio_queue": openPartyPanel940("radio");break;
            case "detail_room_theme": openPartyPanel940("theme");break;
            case "detail_room_seats": case "detail_vip_seat": openPartyPanel940("seats");break;
            case "detail_billboard": openPartyPanel940("billboard");break;
            case "detail_cohosts": openPartyPanel940("admins");break;
            case "detail_banned": openPartyPanel940("banned");break;
            case "detail_room_name": openPartyPanel940("rename");break;
            case "detail_room_category": openPartyPanel940("category");break;
            case "detail_room_privacy": case "detail_room_password": case "detail_mute": case "detail_close_room": openPartyPanel940("privacy");break;
            case "detail_room_income": openPartyPanel940("data");break;
            case "detail_atmosphere": openPartyPanel940("atmosphere");break;
            case "detail_host_role": openPartyPanel940("host");break;
            case "detail_group": openPartyPanel940("channel");break;
            case "detail_gift_ranking": openPartyPanel940("ranking");break;
            case "detail_friend_broadcast": openPartyPanel940("broadcast");break;
            case "detail_edit_profile":openMeAction9981("edit_profile");break;
            case "detail_delete_account":openMeAction9981("account_delete");break;
            case "detail_profile_safety":openMeCloud9984("privacy");break;
            case "detail_help_safety":safetyHelp9981();break;
            case "detail_blocked":openMeCloud9984("blocked");break;
            case "detail_reports":go("report_history");break;
            case "detail_language":language9981();break;
            case "detail_storage":storage9981();break;
            case "detail_about":about9981();break;
            case "detail_rules_community":go("rules");break;
            case "detail_rules_privacy":policyPage9981("privacy");break;
            case "detail_rules_terms":policyPage9981("terms");break;
            case "detail_help_login":help9981("login");break;
            case "detail_help_party":help9981("party");break;
            case "detail_help_gift":help9981("gift");break;
            case "detail_help_games":help9981("games");break;
            case "detail_rules_games":fairPlay9981();break;
            default:detail(route);break;
        }
    }
    private String titleFor(String r){
        if(r.equals("profile_menu"))return "KING Plus";if(r.equals("data_center"))return "Data Center";if(r.equals("coin_mall"))return "Coin Mall";if(r.equals("web_diamond"))return "Web Diamond";if(r.startsWith("vip"))return "VIP Center";if(r.startsWith("noble"))return "Noble Center";if(r.startsWith("gift"))return "Gift Center";if(r.startsWith("game"))return "Game";if(r.startsWith("party"))return roomName; if(r.startsWith("music"))return "Music / KTV";if(r.startsWith("video"))return "Voice & Video";if(r.startsWith("settings"))return "Settings";if(r.startsWith("wallet"))return "Wallet";if(r.startsWith("profile"))return "Profile";if(r.startsWith("level"))return "Level & Achievements";if(r.startsWith("medal"))return "Medals & Influence";if(r.startsWith("task"))return "Tasks";if(r.startsWith("event"))return "Events";return "KING Plus";}
    private void go(String r){if(!route.equals(r))stack.push(route);route=r;render();}
    private void back(){if(!stack.isEmpty()){route=stack.pop();render();}else finish();}
    @Override public void onBackPressed(){back();}

    private void section(String s){
        TextView v=(TextView)getLayoutInflater().inflate(R.layout.king_deepflow_section,body,false);
        v.setText(s.toUpperCase(java.util.Locale.ROOT));
        v.setTextSize(12);v.setTextColor(sub());v.setTypeface(null,Typeface.BOLD);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(38));lp.setMargins(0,dp(8),0,0);body.addView(v,lp);
    }
    private void row(String icon,String name,String desc,String target){row(icon,name,desc,()->go(target));}
    private void row(String icon,String name,String desc,Runnable action){
        LinearLayout r=(LinearLayout)getLayoutInflater().inflate(R.layout.king_deepflow_row,body,false);
        r.setBackground(bg(card(),14));
        TextView ic=r.findViewById(R.id.king_row_icon);
        ic.setText(icon);ic.setTextSize(22);ic.setTextColor(fg());
        TextView n=r.findViewById(R.id.king_row_title);
        n.setText(name);n.setTextSize(15);n.setTextColor(fg());n.setTypeface(null,Typeface.BOLD);
        TextView ds=r.findViewById(R.id.king_row_desc);
        if(desc==null||desc.isEmpty())ds.setVisibility(View.GONE);
        else{ds.setText(desc);ds.setTextSize(11);ds.setTextColor(sub());}
        TextView ar=r.findViewById(R.id.king_row_arrow);
        ar.setText("›");ar.setTextSize(25);ar.setTextColor(sub());
        r.setContentDescription(name+(desc==null?"":", "+desc));
        r.setOnClickListener(v->KingSafe.run(this,"ui-action",action));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(62));
        lp.setMargins(0,dp(4),0,dp(4));body.addView(r,lp);
    }
    private void chip(String text,Runnable action){
        TextView t=(TextView)getLayoutInflater().inflate(R.layout.king_deepflow_chip,body,false);
        t.setText(text);t.setTextSize(13);t.setTextColor(dark()?Color.WHITE:INK);t.setTypeface(null,Typeface.BOLD);
        t.setBackground(bg(dark()?0xff383246:0xffeeeaf6,14));
        t.setOnClickListener(v->KingSafe.run(this,"ui-action",action));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(46));
        lp.setMargins(0,dp(4),0,dp(4));body.addView(t,lp);
    }
    private void hero(String title,String desc){
        LinearLayout h=(LinearLayout)getLayoutInflater().inflate(R.layout.king_deepflow_hero,body,false);
        h.setBackground(bg(dark()?0xff2d293b:0xfffff3bd,18));
        String artFamily=prefs.getString("king_selected_art_family",null);
        boolean usingArt=artFamily!=null;
        if(usingArt){
            int artNum=Math.min(280,Math.max(0,prefs.getInt("king_selected_art_variant",0)));
            String artName="king_art_"+artFamily+"_panel_"+String.format(java.util.Locale.ROOT,"%03d",artNum);
            int artId=getResources().getIdentifier(artName,"drawable",getPackageName());
            if(artId!=0)h.setBackgroundResource(artId);
            else usingArt=false;
        }
        TextView a=h.findViewById(R.id.king_hero_title),b=h.findViewById(R.id.king_hero_desc);
        a.setText(title);a.setTextSize(18);a.setTextColor(usingArt?Color.WHITE:fg());a.setTypeface(null,Typeface.BOLD);
        b.setText(desc);b.setTextSize(12);b.setTextColor(usingArt?0xffeeeeee:sub());
        body.addView(h,new LinearLayout.LayoutParams(-1,dp(92)));
    }
    private void profileMenu(){hero("👤  "+safe(mainPrefs.getString("name","KING User"),"KING User"),
    "Activity level is a device-only TEST preview • VIP is read from verified Wallet");
        section("Account & status");
        row("📊","Data Center","Profile activity, social counts, gifts and rooms","data_center");
        row("🏢","Agency & Agent Recharge","Super Admin, Agency owner, Agent requests","agency");
        row("⭐","User Level Center","Normal level, XP and achievements","level_center");
        row("💎","VIP Level","VIP 0–50 progression and privileges","vip");
        row("♛","Noble Center","Noble identity and room privileges","noble");
        row("💳","Wallet","Verified Diamonds, recharge and transaction history","wallet");
        row("🪙","Coin Mall","Secure recharge packages","coin_mall");
        row("💎","Web Diamond","Open secure KING Plus recharge","web_diamond");
        section("Social & profile");
        row("▣","Square / Moments","Posts and activity","social_center");
        row("👥","Social Media","People search, friends, followers and messages","social_center");
        row("👤","Profile Center","Edit profile, Gallery, Level, Visitors, Gift Wall and Safety","profile_center");
        row("🖼","My Gallery","Open Moments and profile media",()->openCommunity("Moments"));
        row("👥","Followers & Friends","Open live people lists",()->startActivity(new Intent(this,SocialActivity.class)));
        row("👜","Backpack / Collection","Open saved gifts and profile collection",()->openCommunity("Collection"));
        row("🔔","Notifications","Open account notifications",this::openNotifications9981);
        row("👀","Visitors","Recent profile visitors","profile_center");
        row("🏠","Family","Family square, members, tasks and party",()->openCommunity("Family"));
        row("💞","Relationship / CP","CP, gifts, greetings and relationship",()->openCommunity("Relationship"));
        row("📍","Room Match","Find people and active rooms",()->openCommunity("Search"));
        row("👥","Invite Friends","Share your issued KING ID",this::shareInvite);
        row("📣","Friend Broadcast","Party / game / KTV broadcast tools","detail_friend_broadcast");
        section("Gifts & rewards");
        row("🎁","Share Gift / Gift Center","Send gifts, view Gift Wall, wishes and history","gift_center");
        row("🧱","My Gift Wall","Received gift collection","gift_wall");
        row("🎭","Privilege Pack","Frames, entrances, bubbles and badges","privilege_pack");
        row("🛍","Privilege Shop","Equip KING Plus cosmetics","privilege_shop");
        row("🎭","Dress & Effects","Frames, entrance, bubbles and room cosmetics","privilege_shop");
        row("🏆","Rankings","Contribution, charisma, games and events","discover_center");
        row("✅","Tasks / Check-in","Daily level and activity tasks","task_center");
        row("🎪","Events","Party, PK, gift and relationship events","event_center");
        section("Help & settings");
        row("⚙","Settings","Account, privacy, notifications and network","settings");
        row("❓","Help & Customer Support","Login, room, gift and game help","help");
        row("📜","Rules and Policies","Safety, privacy and community rules","rules");
    }

    private void dataCenter(){
        hero("📊 Data Center","Open KING Plus account pages. Each page identifies whether its data is account-synced or a device-only TEST preview.");
        section("Profile & social data");
        row("👤","Profile Center","Edit profile, Gallery, Level, VIP, medals, visitors, Gift Wall and Safety","profile_center");
        row("👥","Followers & Friends","Open live people and follower lists",()->startActivity(new Intent(this,SocialActivity.class)));
        row("▣","Moments & Gallery","Open your posts and profile media",()->openCommunity("Moments"));
        section("Wallet & activity");
        row("🎁","Gift Records","Gift Wall, wish, lucky gift and gift history","gift_center");
        row("💳","Wallet & Recharge","Verified wallet and transaction history","wallet");
        row("🏠","Party Rooms","Open room activity and saved room records",()->KingNav.openRoot(this,0));
        row("✅","Level & Achievements","Activity level, XP and medals","level_center");
        row("🏆","Rankings","Contribution, charisma and activity rankings","discover_center");
    }

    private void privilegePack(){
        LevelSystem.Snapshot s=LevelSystem.read(this);hero("🎁 Privilege Pack","Activity Lv."+s.level+" • Paid VIP cosmetics require verified server entitlement");
        TextView current953=tv("🖼 "+KingCosmetics.frame(this)+"\\n✨ "+KingCosmetics.effect(this)+"\\n💬 "+KingCosmetics.bubble(this),13,INK,true);current953.setPadding(dp(12),dp(8),dp(12),dp(8));current953.setBackground(bg(0xfffff2c8,14));body.addView(current953,new LinearLayout.LayoutParams(-1,dp(82)));
        section("Identity");row("🖼","ID / Profile Frames","Minimal, Music, Heart, Royal, Galaxy, Crown","detail_frames");row("✨","Entrance Effects","Sparkle, Crown Drop, Rose Shower, Galaxy Portal, Royal Arrival","detail_entrance");row("💬","Chat Bubble","VIP identity styling and message badge","detail_chat_bubble");row("🏷","Name Badge","VIP + Level badges in room and chat","detail_name_badge");
        section("Room");row("🎨","Room Theme","Green, Royal, Galaxy and event themes","detail_room_theme");row("🪑","VIP Seat Style","Seat frame and identity","detail_vip_seat");
        section("Manage");row("🛍","Open Privilege Shop","Equip unlocked frames, effects and bubbles","privilege_shop");row("💎","VIP Progress","Review VIP tiers and privileges","vip");
    }
    private void privilegeShop(){hero("🛍 Privilege Shop","Equip unlocked KING Plus cosmetics. Selection persists on this device and cloud profile.");String[] frames={"Minimal Frame","Music Frame","Heart Frame","Royal Frame","Galaxy Frame","Crown Frame"};section("ID Frames");for(String f:frames)row(KingCosmetics.frameEmoji(f),f,"Required: "+KingCosmetics.requirementFrame(f),()->equipFrame(f));section("Entrance Effects");String[] eff={"Welcome Sparkle","Crown Drop","Rose Shower","Galaxy Portal","Royal Arrival"};for(String e:eff)row(KingCosmetics.effectEmoji(e),e,"Required: "+KingCosmetics.requirementEffect(e),()->equipEffect(e));section("Chat Bubbles");for(String b:KingCosmetics.BUBBLES)row("💬",b,"Required: "+KingCosmetics.requirementBubble(b),()->equipBubble940(b));}
    private LevelSystem.Snapshot freeActivityOnly9988(){
        LevelSystem.Snapshot raw=LevelSystem.read(this);
        return new LevelSystem.Snapshot(raw.level,raw.xp,0,0);
    }
    private void equipFrame(String f){if(!KingCosmetics.unlockedFrame(f,freeActivityOnly9988())){toast("Locked • "+KingCosmetics.requirementFrame(f));return;}KingCosmetics.setFrame(this,f);syncCosmetics();toast(f+" frame equipped");}
    private void equipEffect(String e){if(!KingCosmetics.unlockedEffect(e,freeActivityOnly9988())){toast("Locked • "+KingCosmetics.requirementEffect(e));return;}KingCosmetics.setEffect(this,e);syncCosmetics();toast(e+" equipped");}
    private void equipBubble940(String b){if(!KingCosmetics.unlockedBubble(b,freeActivityOnly9988())){toast("Locked • "+KingCosmetics.requirementBubble(b));return;}KingCosmetics.setBubble(this,b);syncCosmetics();toast(b+" chat bubble equipped");}
    private void syncCosmetics(){if(me==null||db==null)return;Map<String,Object>m=new HashMap<>();m.put("equippedFrame",KingCosmetics.frame(this));m.put("entranceEffect",KingCosmetics.effect(this));m.put("chatBubble",KingCosmetics.bubble(this));m.put("updatedAt",FieldValue.serverTimestamp());db.collection("public_profiles").document(me.getUid()).set(m,com.google.firebase.firestore.SetOptions.merge());}

    private void vipCenter(){LevelSystem.Snapshot s=LevelSystem.read(this);hero("💎 VIP "+s.vipLevel+" / "+LevelSystem.MAX_VIP,LevelSystem.vipName(s.vipLevel)+" • "+s.vipPoints+" VIP points");long from=LevelSystem.vipThreshold(s.vipLevel),to=s.nextVipPoints();int pc=s.vipLevel>=LevelSystem.MAX_VIP?100:(int)Math.max(0,Math.min(100,(s.vipPoints-from)*100/Math.max(1,to-from)));ProgressBar p=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);p.setMax(100);p.setProgress(pc);body.addView(p,new LinearLayout.LayoutParams(-1,dp(24)));TextView progress=tv(s.vipLevel>=LevelSystem.MAX_VIP?"MAX VIP 50":pc+"% to VIP "+(s.vipLevel+1)+" • next at "+to,12,sub(),false);body.addView(progress);section("VIP level cards");for(int start=0;start<=50;start+=5){int end=Math.min(50,start+4);final int a=start,b=end;row("💠","VIP "+a+" – "+b,"Tap to preview privileges",()->showVipRange(a,b,s.vipLevel));}section("Privileges");row("👑","Identity","VIP badge, name styling and profile card","detail_vip_identity");row("✨","Entrance","VIP linked entrance effect","privilege_shop");row("🖼","Frames","VIP linked profile / ID frame","privilege_shop");row("🪑","VIP Seat","Room seat styling and identity","detail_vip_seat");row("🎁","Gift privileges","VIP gift catalog unlocks","gift_center");row("ⓘ","VIP Rules","Progression and maintenance rules","vip_rules");}
    private void showVipRange(int a,int b,int current){LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setPadding(dp(12),dp(8),dp(12),dp(8));for(int v=a;v<=b;v++){long need=LevelSystem.vipThreshold(v);TextView x=tv((current>=v?"✓ ":"🔒 ")+"VIP "+v+" • "+LevelSystem.vipName(v)+"\nRequired points: "+need,14,INK,true);x.setBackground(bg(0xfff5f1fb,12));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(62));lp.setMargins(0,dp(3),0,dp(3));r.addView(x,lp);}new AlertDialog.Builder(this).setTitle("VIP "+a+"–"+b).setView(r).setPositiveButton("OK",null).show();}
    private void vipRules(){hero("Verified VIP Rules","Paid VIP ONLY rises after validated Recharge in trusted Wallet Server. Local free gifts, XP or TEST coins grant no VIP.");section("Progression");chip("VIP 0–50 preview: account proof comes from authenticated Wallet",()->{});chip("VIP/Noble entitlements cannot be bought or granted by this TEST app",()->{});chip("No production billing or unverified VIP benefits enabled",()->{});section("Unlock path");row("💎","VIP 1–9","Identity, starter frames, stickers","detail_vip_1_9");row("👑","VIP 10–19","Royal frames, entrance effects, seat styles","detail_vip_10_19");row("🏆","VIP 20–34","Premium cosmetics and gift classes","detail_vip_20_34");row("♛","VIP 35–49","Mythic identity and room prestige","detail_vip_35_49");row("🔥","VIP 50","KING Supreme tier","detail_vip_50");}
    private void nobleCenter(){
        LevelSystem.Snapshot s=LevelSystem.read(this);int noble=Math.min(10,s.vipLevel/5);int nextNoble=Math.min(10,noble+1);int targetVip=Math.min(50,Math.max(5,nextNoble*5));
        hero("♛ Noble "+noble,"VIP "+s.vipLevel+" • next Noble tier at VIP "+targetVip);
        android.widget.ProgressBar bar953=new android.widget.ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);bar953.setMax(50);bar953.setProgress(Math.min(50,s.vipLevel));LinearLayout.LayoutParams bp953=new LinearLayout.LayoutParams(-1,dp(16));bp953.setMargins(dp(4),dp(4),dp(4),dp(8));body.addView(bar953,bp953);
        TextView status953=tv(noble>=10?"MAX NOBLE 10":"VIP "+s.vipLevel+" / "+targetVip+" for Noble "+nextNoble,12,sub(),true);status953.setGravity(Gravity.CENTER);body.addView(status953,new LinearLayout.LayoutParams(-1,dp(34)));
        section("Noble privileges");
        String[] perks953={"Profile crown & Noble identity","Priority room entrance styling","Noble seat / name presentation","Exclusive badge & collection status"};
        for(int i=0;i<perks953.length;i++)row(i<noble?"♛":"◇",perks953[i],noble>i?"Active":"Progress through VIP to unlock","detail_noble_"+Math.min(10,i+1));
        section("Noble tiers");
        for(int i=1;i<=10;i++){final int n=i;row(i<=noble?"♛":"🔒","Noble "+i,"Unlocks at VIP "+(i*5),()->{if(s.vipLevel<n*5)toast("Unlocks at VIP "+(n*5));else go("detail_noble_"+n);});}
    }

    /** Me > Wallet now opens the verified recharge shop rather than the old TEST wallet mockup. */
    private void wallet(){startActivity(new Intent(this,KingRecharge974Activity.class));finish();}

    private void walletHistory(){
        hero("🧾 Verified Wallet Ledger","Real Firebase transactions only. No phone-local TEST balance or imaginary paid credit.");
        fetchAccountLedger9983(false,false);
    }

    private final String[] libraryFamilies9934={"party","stage","mic","gift","vip","avatar","games","social","profile","level","family","wallet"};
    private final String[] libraryThemes9934={"royal","neon","gold","rose","sky","forest","indigo","ember","slate"};
    private final String[] libraryPatterns9934={"banner","two_column","badge","progress","roster","tile","rank","summary","action"};
    private final String[] librarySizes9934={"micro","compact","regular","large"};
    private int libraryFamilyIdx9934=0,libraryThemeIdx9934=0,libraryPatternIdx9934=0,librarySizeIdx9934=0;
    private void designLibrary9934(){
        hero("🎨 KING UI Library · 3,888 previews","Each design is a real compiled XML layout. Designs are not 3,888 live games or new payment features.");
        section("Feature family");
        for(int i=0;i<libraryFamilies9934.length;i++){
            final int idx=i;String family=libraryFamilies9934[i];
            chip((i==libraryFamilyIdx9934?"● ":"○ ")+family.toUpperCase(java.util.Locale.ROOT),()->{
                libraryFamilyIdx9934=idx;render();});
        }
        section("Visual theme");
        for(int i=0;i<libraryThemes9934.length;i++){
            final int idx=i;String theme=libraryThemes9934[i];
            row(i==libraryThemeIdx9934?"✓":"◇",theme.toUpperCase(java.util.Locale.ROOT),"Preview style",()->{
                libraryThemeIdx9934=idx;render();});
        }
        section("Card structure");
        for(int i=0;i<libraryPatterns9934.length;i++){
            final int idx=i;String pattern=libraryPatterns9934[i];
            row(i==libraryPatternIdx9934?"✓":"▣",pattern.replace('_',' ').toUpperCase(java.util.Locale.ROOT),"UI composition",()->{
                libraryPatternIdx9934=idx;render();});
        }
        section("Spacing / text size");
        for(int i=0;i<librarySizes9934.length;i++){
            final int idx=i;String size=librarySizes9934[i];
            chip((i==librarySizeIdx9934?"✓ ":"◇ ")+size.toUpperCase(java.util.Locale.ROOT),()->{
                librarySizeIdx9934=idx;render();});
        }
        String family=libraryFamilies9934[libraryFamilyIdx9934];
        String resource="king_library_"+family+"_"+libraryThemes9934[libraryThemeIdx9934]+"_"
                       +libraryPatterns9934[libraryPatternIdx9934]+"_"+librarySizes9934[librarySizeIdx9934];
        int rid=getResources().getIdentifier(resource,"layout",getPackageName());
        if(rid==0){toast("UI design resource missing");return;}
        View preview=getLayoutInflater().inflate(rid,body,false);
        body.addView(preview,new LinearLayout.LayoutParams(-1,-2));
        TextView action=preview.findViewById(R.id.king_catalog_action);
        action.setOnClickListener(v->go(libraryFeatureRoute9934(family)));
        chip("XML resource: "+resource,()->{});
        chip("Next preview ▶",()->{
            librarySizeIdx9934++;
            if(librarySizeIdx9934>=4){librarySizeIdx9934=0;libraryPatternIdx9934++;}
            if(libraryPatternIdx9934>=9){libraryPatternIdx9934=0;libraryThemeIdx9934++;}
            if(libraryThemeIdx9934>=9){libraryThemeIdx9934=0;libraryFamilyIdx9934=(libraryFamilyIdx9934+1)%12;}
            render();
        });
    }
    private String libraryFeatureRoute9934(String family){
        switch(family){
            case "party":case "stage":case "mic":return "party_tools";
            case "gift":return "gift_center";
            case "vip":return "vip";
            case "avatar":return "privilege_pack";
            case "games":return "game_center";
            case "social":case "family":return "social_center";
            case "profile":return "profile_center";
            case "level":return "level_center";
            case "wallet":return "wallet";
            default:return "profile_menu";
        }
    }

    private final String[] artFamilies9935={"party","mic","avatar","gift","vip","games","medal","profile","entrance"};
    private int artFamily9935=0,artVariant9935=0;
    private void drawableLibrary9935(){
        hero("KING Drawable Studio · 7,587 graphics","Original KING graphics. This design library does not add 7,587 new live game features.");
        section("Choose art family");
        for(int i=0;i<artFamilies9935.length;i++){
            final int selected=i;
            chip((artFamily9935==i?"● ":"○ ")+artFamilies9935[i].toUpperCase(java.util.Locale.ROOT),()->{
                artFamily9935=selected;render();
            });
        }
        section("Palette "+(artVariant9935+1)+" / 281");
        String family=artFamilies9935[artFamily9935];
        String index=String.format(java.util.Locale.ROOT,"%03d",artVariant9935);
        for(String kind:new String[]{"symbol","panel","halo"}){
            String key="king_art_"+family+"_"+kind+"_"+index;
            int id=getResources().getIdentifier(key,"drawable",getPackageName());
            if(id==0){toast("Drawable missing: "+key);continue;}
            if(kind.equals("symbol")){
                android.widget.ImageView img=new android.widget.ImageView(this);
                img.setImageResource(id);img.setContentDescription(family+" original vector icon variant "+(artVariant9935+1));
                img.setPadding(dp(12),dp(8),dp(12),dp(8));
                body.addView(img,new LinearLayout.LayoutParams(-1,dp(94)));
            }else{
                TextView tile=tv((kind.equals("panel")?"ROYAL PANEL  ·  ":"ROOM AURA  ·  ")+family.toUpperCase(java.util.Locale.ROOT),
                                16,Color.WHITE,true);
                tile.setBackgroundResource(id);tile.setGravity(Gravity.CENTER);
                tile.setContentDescription(key);
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(85));
                lp.setMargins(0,dp(5),0,dp(5));
                body.addView(tile,lp);
            }
        }
        chip("◀ Previous graphic",()->{artVariant9935=(artVariant9935+280)%281;render();});
        chip("Next graphic ▶",()->{artVariant9935=(artVariant9935+1)%281;render();});
        chip("Use this panel on KING menu headers",()->{
            prefs.edit().putString("king_selected_art_family",family)
                .putInt("king_selected_art_variant",artVariant9935).apply();
            toast("Original KING artwork applied to menu headers");render();
        });
        chip("Remove applied artwork",()->{
            prefs.edit().remove("king_selected_art_family").remove("king_selected_art_variant").apply();
            toast("Default menu artwork restored");render();
        });
        chip("Open related KING feature",()->go(libraryFeatureRoute9934(family.equals("medal")?"level":family.equals("entrance")?"party":family)));
    }

    private void settings(){
        hero("⚙ KING Plus Settings","Account, privacy, notifications and media controls");
        section("Account & Safety");
        row("🔐","Account & Security","Login devices, account identity and safety","settings_account");
        row("🛡","Privacy","Profile, room, block and discoverability","settings_privacy");
        section("App Experience");
        row("🔔","Notifications","Message, room, gifts and activity notifications","settings_notifications");
        row("🌐","Network & Media","Voice/video quality, data and reconnect","settings_network");
        row("🎨","KING XML Design Library","3,888 original XML screen designs","ui_design_library");
        row("🖼","KING Drawable Studio","7,587 original badges, panels and effects","drawable_library");
        row("🌍","Language","App language and content language","detail_language");
        row("🧹","Storage","Cache and downloaded media","detail_storage");
        section("About");
        row("ℹ","About KING Plus","Version, terms and policies","detail_about");
        row("❓","Help Center","Login, Party, gifts, games and safety","help");
        row("📜","Rules & Policies","Community, privacy, terms and fair play","rules");
    }
    private void settingsAccount(){
        com.google.firebase.auth.FirebaseUser u955=null;try{u955=com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();}catch(Exception ignored){}
        hero("🔐 Account & Security",u955==null?"Not signed in":"Signed in • "+(u955.getEmail()==null?"KING account":u955.getEmail()));
        toggle("Remember login","remember_login",true);
        section("Identity");
        row("📱","Login devices & history","Current device • "+android.os.Build.MODEL,this::loginDevices940);
        row("🔑","Change / bind phone","Mobile identity flow","detail_bind_phone");
        section("Account Controls");
        row("🛡","Safety Center","Block, reports and account protection","detail_help_safety");
        row("🗑","Account deletion","Review consequences before deleting","detail_delete_account");
    }
    private void loginDevices940(){
        body.removeAllViews();hero("📱 Login Devices & History","Local security history on this phone. No advertiser or third-party access.");
        String provider=mainPrefs.getString("login_provider","Unknown");
        String device=(android.os.Build.MANUFACTURER+" "+android.os.Build.MODEL).trim();
        String account=me==null?"Signed out":("KING ID • "+me.getUid().substring(0,Math.min(8,me.getUid().length())).toUpperCase(java.util.Locale.US));
        section("Current session");chip("Device: "+device,()->{});chip("Provider: "+provider,()->{});chip(account,()->{});
        section("Successful sign-ins on this device");
        SharedPreferences sec=getSharedPreferences("king_security",MODE_PRIVATE);String raw=sec.getString("login_history","");
        if(raw==null||raw.isEmpty())chip("No local login history yet.",()->{});
        else{
            int shown=0;for(String x:raw.split("[|][|][|]")){String[] p=x.split("::",3);if(p.length<3)continue;long at=0;try{at=Long.parseLong(p[0]);}catch(Exception ignored){}String when=at<=0?"Unknown time":new java.text.SimpleDateFormat("dd MMM yyyy • HH:mm",java.util.Locale.US).format(new java.util.Date(at));chip("✓ "+p[1]+" • "+p[2]+"\n"+when,()->{});if(++shown>=12)break;}
        }
        chip("Clear local login history",()->{sec.edit().remove("login_history").apply();toast("Local login history cleared");loginDevices940();});
    }

    private void openMeCloud9984(String page) {
        Intent activity=new Intent(this,KingMePrivacy9984Activity.class);
        activity.putExtra("page",page);
        startActivity(activity);
    }
    private void settingsPrivacy(){
        hero("🛡 Privacy","Control who can find, contact and invite you");
        section("Discoverability");
        toggle("Allow people to find me","find_me",true);toggle("Show online status","online_status",true);
        section("Messages & Rooms");
        toggle("Allow room invites","room_invites",true);toggle("Allow private messages","private_messages",true);toggle("Show entrance effects","entrance_fx",true);
        section("Safety");
        row("🚫","Blocked users","Review blocked accounts","detail_blocked");row("⚑","Reports & safety","Report history and safety center","detail_reports");
    }
    private void settingsNotifications(){
        hero("🔔 Notifications","Choose which KING activity can alert you");
        section("Social");toggle("Messages","notify_messages",true);toggle("Followers / Friends","notify_social",true);
        section("Party & Gifts");toggle("Room invitations","notify_room_invites",true);toggle("Gifts and VIP","notify_gifts",true);toggle("Game invitations","notify_games",true);
        section("System");
        row("⚙","Android notification settings","Open system notification permissions",()->{try{Intent i=new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);i.putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName());startActivity(i);}catch(Exception e){toast("Open Android Settings → Notifications");}});
    }
    private void settingsNetwork(){
        boolean online955=KingNetwork.online(this);
        hero("🌐 Network & Media",(online955?"● Online":"○ Offline")+" • "+android.os.Build.MODEL);
        section("Reconnect & Data");toggle("Auto reconnect voice/video","auto_reconnect",true);toggle("Low-data voice mode","low_data_voice",false);
        section("Visual Effects");toggle("Auto-play gift effects","autoplay_gifts",true);toggle("Auto-play entrance effects","autoplay_entrance",true);
        section("Diagnostics");row("🎙","Voice diagnostics","Open voice room test","video_center");row("📹","Video diagnostics","Open video room test","video_center");
        row("↻","Connection status",online955?"Network available":"Offline • realtime features pause",()->toast(KingNetwork.online(this)?"KING Plus is online":"KING Plus is offline"));
    }
    private void toggle(String label,String key,boolean def){boolean on=prefs.getBoolean(key,def);row(on?"🟢":"⚪",label,on?"ON":"OFF",()->{prefs.edit().putBoolean(key,!prefs.getBoolean(key,def)).apply();render();});}

    private void help(){
        hero("❓ Help Center","Quick guides for the main KING Plus flows");
        section("Account");row("🔑","Login / OTP","Google login, mobile OTP and account setup","detail_help_login");
        section("Live Party");row("🎤","Party Room","Seats, mic, host, co-host and room controls","detail_help_party");row("🎁","Gifts","Recipients, quantity, history, wall and VIP unlocks","detail_help_gift");
        section("Games & Progress");row("🎮","Games","Offline Ludo and realtime room multiplayer","detail_help_games");row("💎","VIP / Levels","VIP 0–50, Normal Level 1–99 and Noble tiers","vip_rules");
        section("Safety");row("⚑","Block, report & moderation","Keep rooms and social activity safe","detail_help_safety");row("📜","Rules & Policies","Community, privacy, terms and fair play","rules");
    }
    private void rules(){
        hero("📜 Rules & Policies","KING Plus community safety, privacy and fair play");
        TextView note955=tv("Use KING Plus respectfully. Harassment, spam, impersonation, illegal content and abuse of rooms/games can lead to moderation.",12,sub(),false);note955.setPadding(dp(12),dp(8),dp(12),dp(8));note955.setBackground(bg(0xfffff2c8,14));body.addView(note955,new LinearLayout.LayoutParams(-1,dp(82)));
        section("Community");row("🛡","Community Rules","Respect, harassment, spam and safety","detail_rules_community");row("⚑","Moderation & Reporting","How block, kick, ban and reports work","detail_help_safety");
        section("Legal & Privacy");row("🔐","Privacy Policy","Profile, messages and account data","detail_rules_privacy");row("📄","Terms of Service","Account and service terms","detail_rules_terms");
        section("Games");row("🎮","Game Fair Play","Room game conduct and abuse prevention","detail_rules_games");
    }

    private void partyPlay(){hero("🎮 Play Center","Bolo-style nested Party activity flow using original KING Plus implementation.");row("▦","Templates","Interaction, game and themed room templates","party_templates");row("🎪","Events","Relationship, game events and privileges","party_events");row("🛠","Tools","Room event, private chat, music, atmosphere and family","party_tools");row("🎮","Realtime Room Games","Ready / Start / result / rematch","game_team");}
    private void partyTemplates(){section("Interaction");row("💬","Chat Room","General conversation template",()->applyTemplate("Chat Room"));row("💘","Pick Me!","Random member interaction",()->applyTemplate("Pick Me!"));row("💞","Coupling","Relationship interaction",()->applyTemplate("Coupling"));row("🎊","Ceremony","Celebration room template",()->applyTemplate("Ceremony"));section("Games");String[] n={"Dominoes Chat Room","Mobile Legends","PUBG Mobile","Free Fire","Ludo Chat Room","SHEEP & PUZZLES","Draw & Guess","Bingo"};String[] i={"🀄","⚔","🔫","🔥","🎲","🐑","🎨","🎯"};for(int x=0;x<n.length;x++){final String q=n[x];row(i[x],q,"Apply template and open playable flow",()->{applyTemplate(q);if(q.contains("Draw"))openGame("draw");else if(q.contains("Bingo"))openGame("bingo");else if(q.contains("SHEEP"))openGame("sheep");else openRoomGame();});}}
    private void partyEvents(){String[][] e={{"💞","Relationship","relationship"},{"🍒","Crazy Fruits","slot"},{"🐔","Chicken Run","reaction"},{"🐯","Animal PK","zoo"},{"🪄","Magic Lamp","wheel"},{"🏎","Forza Motorsport","highlow"},{"🍬","Candy Legend","memory"},{"🎰","Lucky Slot","slot"}};for(String[] x:e)row(x[0],x[1],"Open interactive KING Plus equivalent",()->{if("relationship".equals(x[2]))openCommunity("Relationship");else openGame(x[2]);});section("Privileges");row("💗","Couple","Relationship / CP privileges",()->openCommunity("Relationship"));row("🎁","Gift Wall","Received gift collection","gift_wall");}
    private void partyTools(){row("📅","Events","Room events and activity","event_center");row("🪑","Room seat","Mic seat and ready status","game_team");row("👥","Enable group","Channel / group tools","detail_group");row("💬","Private chat","Open Messages",()->KingNav.openRoot(this,3));row("🎵","Music","KTV library, queue and audio settings","music_center");row("🎆","Atmosphere","Room effects and entrance display","detail_atmosphere");row("💰","Income","Gift contribution and room data","detail_room_income");row("🏠","Share Family","Family party and members",()->openCommunity("Family"));row("🎨","Theme Room","Theme and scene options","detail_room_theme");row("⚙","Room Management","Privacy, password, notice and admin","party_management");}
    private void partySettings(){row("▣","Billboard","Room notice and pinned message","detail_billboard");row("🎆","Atmosphere","Room visual effects","detail_atmosphere");row("🛡","Administrator","Host / co-host permissions","party_admin");row("🔑","Password settings","Private / password room","detail_room_password");toggle("Invite users to take seats","invite_seats",true);toggle("Show entrance effects","room_entrance_fx",true);toggle("Play entrance voice","room_entrance_voice",true);row("⚙","Room Management","Name, category, seats and close room","party_management");}
    private void partyManagement(){row("✏","Room name","Rename and room identity","detail_room_name");row("🗂","Category","Chat, Games, KTV, Radio, Dating","detail_room_category");row("🎨","Theme","Default, Royal, Galaxy and event themes","detail_room_theme");row("🪑","Mic seats","8 / 10 / 12 seat layouts","detail_room_seats");row("🔐","Privacy","Public / private / password","detail_room_privacy");row("📢","Announcement","Room notice and welcome text","detail_billboard");row("⛔","Close room","Host-only close flow","detail_close_room");}
    private void partyAdmin(){row("👑","Host","Owner room permissions","detail_host_role");row("🛡","Co-hosts","Administrator permissions","detail_cohosts");row("📥","Seat requests","Approve / reject mic requests","game_team");row("🚫","Banned users","Kick / ban management","detail_banned");row("🔇","Mute controls","Mute all / individual seat","detail_mute");}
    private void applyTemplate(String name){prefs.edit().putString("room_template",name).apply();if(db!=null&&!roomId.isEmpty()&&me!=null){Map<String,Object>m=new HashMap<>();m.put("name",name);m.put("actorUid",me.getUid());m.put("actorName",safe(mainPrefs.getString("name","KING User"),"KING User"));m.put("active",true);m.put("updatedAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).collection("room_templates").document("current").set(m);}toast(name+" template active");}

    private void giftCenter(){hero("🎁 Gift Center","Recipients, quantity, VIP unlocks, wall, wish, lucky gift and history.");row("🎁","Send Gift","Open full-screen gift catalog",this::openGiftCenter);row("🧱","Gift Wall","Received gift collection","gift_wall");row("💫","Gift Wish","Wish progress and thanks","gift_wish");row("🍀","Lucky Gift","Lucky reward / gift event","gift_lucky");row("👜","Gift Bag / Backpack","Free gifts and inventory",()->openCommunity("Collection"));row("🏆","Contribution Ranking","Room gift contribution","detail_gift_ranking");row("🕘","Gift History","Secure sent/received wallet ledger",this::openGiftCenter);row("💎","VIP Gifts","VIP-gated catalog","vip");}

    private void giftWall(){
        if(!roomId.isEmpty()){
            hero("🧱 Room Gift Wall","Real free gift events from this Party Room only.");
            if(db==null){chip("Firebase unavailable",()->{});return;}
            db.collection("live_rooms").document(roomId).collection("events")
                .orderBy("createdAt",com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(100).get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(q->{
                    if(isFinishing()||isDestroyed()||!"gift_wall".equals(route))return;
                    Map<String,Long> counts=new HashMap<>();
                    for(DocumentSnapshot d:q.getDocuments())if("gift".equals(d.getString("type"))){
                        String target=d.getString("targetUid"),gift=d.getString("giftName");
                        if(target==null||gift==null||gift.trim().isEmpty())continue;
                        counts.put(gift,counts.getOrDefault(gift,0L)+1);
                    }
                    if(counts.isEmpty())chip("No recorded room gifts yet",()->{});
                    else for(Map.Entry<String,Long> g:counts.entrySet())
                        chip("🎁 "+g.getKey()+" ×"+g.getValue(),()->{});
                }).addOnFailureListener(e->{
                    if(!isFinishing()&&"gift_wall".equals(route))
                        chip("Room gift events unavailable online",()->{});
                });
            return;
        }
        hero("🧱 My Gift Wall","Gifts received by your signed-in account in the trusted Wallet Ledger. Free Party gifts are visible in the Party Room.");
        fetchAccountLedger9983(true,true);
    }

    private void giftHistory(){
        if(!roomId.isEmpty()){
            hero("🕘 Room Gift History","Real gifts in this live room, not the whole account.");
            if(db==null)return;
            db.collection("live_rooms").document(roomId).collection("events")
                .orderBy("createdAt",com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(80).get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(q->{
                    if(isFinishing()||isDestroyed()||!"gift_history".equals(route))return;
                    int shown=0;
                    for(DocumentSnapshot d:q.getDocuments())if("gift".equals(d.getString("type"))){
                        chip("🎁 "+safe(d.getString("text"),"Recorded gift"),()->{});shown++;
                    }
                    if(shown==0)chip("No room gifts recorded",()->{});
                }).addOnFailureListener(e->{
                    if(!isFinishing()&&"gift_history".equals(route))
                        chip("Room gift history unavailable online",()->{});
                });
            return;
        }
        hero("🕘 My Gift History","Server-recorded paid gifts sent or received by your account only. Free Party gift receipts are in the original rooms.");
        fetchAccountLedger9983(true,false);
    }

    /** Never display local pretend wallet transactions as verified payments. */
    private void fetchAccountLedger9983(boolean giftsOnly,boolean receivedOnly){
        if(db==null||me==null){chip("Log in to view your actual account ledger",()->{});return;}
        final String owner=me.getUid(),expectedRoute=route;
        final java.util.ArrayList<DocumentSnapshot> records=new java.util.ArrayList<>();
        final int[] pending={receivedOnly?1:2};
        final boolean[] failed={false};
        chip("Loading real Firebase ledger…",()->{});
        java.util.function.Consumer<com.google.firebase.firestore.QuerySnapshot> onPage=q->{
            if(q!=null)records.addAll(q.getDocuments());
            pending[0]--;
            if(pending[0]!=0)return;
            if(isFinishing()||isDestroyed()||!expectedRoute.equals(route)||me==null||
               !owner.equals(me.getUid()))return;
            if(failed[0])chip("Some transactions could not be retrieved. History may be incomplete.",()->{});
            java.util.HashMap<String,DocumentSnapshot> unique=new java.util.HashMap<>();
            for(DocumentSnapshot d:records) {
                if(owner.equals(d.getString("senderUid"))||owner.equals(d.getString("targetUid")))
                    unique.put(d.getId(),d);
            }
            java.util.ArrayList<DocumentSnapshot> ordered=new java.util.ArrayList<>(unique.values());
            ordered.sort((a,b)->{
                com.google.firebase.Timestamp at=a.getTimestamp("createdAt");
                com.google.firebase.Timestamp bt=b.getTimestamp("createdAt");
                long x=at==null?0:at.toDate().getTime(),y=bt==null?0:bt.toDate().getTime();
                return Long.compare(y,x);
            });
            java.util.ArrayList<KingMeGiftSummary9983.Gift> gifts=new java.util.ArrayList<>();
            for(DocumentSnapshot d:ordered) {
                com.google.firebase.Timestamp at=d.getTimestamp("createdAt");
                Long cost=d.getLong("cost");
                String name=d.getString("giftName");
                gifts.add(new KingMeGiftSummary9983.Gift(d.getId(),
                    d.getString("senderUid"),d.getString("targetUid"),
                    name,cost==null?0:cost,at==null?0:at.toDate().getTime()));
            }
            java.util.List<KingMeGiftSummary9983.Gift> verified=
                KingMeGiftSummary9983.forAccount(gifts,owner,receivedOnly);
            if(receivedOnly){
                Map<String,Integer> counts=KingMeGiftSummary9983.receivedWall(gifts,owner);
                if(counts.isEmpty())chip("No verified received gifts in the Wallet Ledger yet.",()->{});
                else for(Map.Entry<String,Integer> d:counts.entrySet())
                    chip("🎁 "+d.getKey()+" ×"+d.getValue(),()->{});
            }else if(giftsOnly){
                if(verified.isEmpty())chip("No verified paid gifts recorded for your account.",()->{});
                for(KingMeGiftSummary9983.Gift d:verified){
                    boolean sent=owner.equals(d.senderUid);
                    chip((sent?"↗ Sent ":"↙ Received ")+d.name+" • "+d.amount+" 💎",()->{});
                }
            }else{
                int shown=0;
                for(DocumentSnapshot d:ordered) {
                    if(shown++>=60)break;
                    boolean sent=owner.equals(d.getString("senderUid"));
                    String name=safe(d.getString("giftName"),
                        safe(d.getString("type"),"Verified transaction"));
                    Long amount=d.getLong("cost");
                    if(amount==null)amount=d.getLong("amount");
                    chip((sent?"↗ ":"↙ ")+name+" • "+(amount==null?"—":amount)+" 💎",()->{});
                }
                if(shown==0)chip("No server-verified wallet transactions yet.",()->{});
            }
        };
        db.collection("wallet_ledger").whereEqualTo("targetUid",owner).limit(60)
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(onPage::accept)
            .addOnFailureListener(e->{failed[0]=true;onPage.accept(null);});
        if(!receivedOnly)db.collection("wallet_ledger").whereEqualTo("senderUid",owner).limit(60)
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(onPage::accept)
            .addOnFailureListener(e->{failed[0]=true;onPage.accept(null);});
    }

    private void giftWish(){
        hero("💫 My Live Gift Wish","This gift wish is saved to your KING Plus Firebase account and visible on your public profile.");
        if(db==null||me==null){chip("Sign in to choose a wish",()->{});return;}
        final String signedUid=me.getUid();
        final TextView current=tv("Loading current Wish from Firebase…",13,sub(),true);
        body.addView(current,new LinearLayout.LayoutParams(-1,dp(50)));
        db.collection("king_me_wishes").document(signedUid)
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(d->{
                if(isFinishing()||isDestroyed()||!"gift_wish".equals(route)||me==null||
                    !signedUid.equals(me.getUid()))return;
                String wish=d.exists()?d.getString("wish"):null;
                current.setText(wish==null?"No saved Gift Wish yet":"🎁 Current wish: "+wish);
            }).addOnFailureListener(e->current.setText("Gift Wish unavailable online. Nothing changed."));
        final String[] options={"Royal Castle","Galaxy Ship","King Dragon","VIP Crown","Diamond Rain"};
        for(String option:options)row("🎁",option,"Save as my public Gift Wish",()->{
            if(!KingMeGiftSummary9983.validWish(option)||me==null||
               !signedUid.equals(me.getUid())){toast("Sign in again");return;}
            Map<String,Object> data=new HashMap<>();
            data.put("uid",signedUid);data.put("wish",option);
            data.put("updatedAt",FieldValue.serverTimestamp());
            db.collection("king_me_wishes").document(signedUid).set(data)
                .addOnSuccessListener(v->{
                    if(!isFinishing()&&!isDestroyed()&&"gift_wish".equals(route))
                        current.setText("🎁 Current wish: "+option+"  ✓ Synced online");
                })
                .addOnFailureListener(e->toast("Gift Wish not saved online: "+safe(e.getMessage(),"Permission")));
        });
        chip("Wish is a profile preference only. It gives no free Diamonds or paid Gifts.",()->{});
    }

    private void giftLucky(){
        hero("🍀 Lucky Gift","Real random reward settlement is not enabled in this TEST build.");
        chip("No false Jackpot, Diamonds or VIP gifts are awarded locally.",()->{});
        row("🎁","Gift Center","Open available verified free gift catalog",this::openGiftCenter);
    }

    private void gameCenter(){hero("🎮 Game","Playable local games + realtime room multiplayer");row("🔥","Hot","Quick playable games","game_hot");row("🎲","LUDO","Ludo and board games",()->openGame("ludo"));row("🎉","Party","Sheep, Werewolf, Spy, Draw, Bingo and more","game_party");row("👥","Team / Live Room","Realtime ready/start/result/rematch","game_team");}
    private void gameHot(){String[][] g={{"🎰","Slot","slot"},{"🪙","Coin Toss","coin"},{"✊","RPS","rps"},{"🎲","Dice","dice"},{"🔢","Guess","guess"},{"🧠","Memory","memory"},{"⚡","Reaction","reaction"},{"🃏","High / Low","highlow"},{"🎡","Lucky Wheel","wheel"},{"⭕","Tic Tac Toe","tic_tac_toe"}};for(String[] x:g)row(x[0],x[1],"Play now",()->openGame(x[2]));}
    private void gameParty(){String[][] g={{"🐑","Sheep Fight","sheep"},{"🐺","Werewolf","werewolf"},{"🕵","Spy Game","spy"},{"🎨","Draw & Guess","draw"},{"🎯","Bingo","bingo"},{"🦁","Crazy Zoo","zoo"},{"🀄","Domino","domino"}};for(String[] x:g)row(x[0],x[1],"Play now",()->openGame(x[2]));}
    private void gameTeam(){hero("👥 Realtime Room Games","Players join the same Firebase room, tap Ready, host starts a round, results are shared and Rematch starts the next round.");row("✊","Room RPS","Realtime synchronized round",this::openRoomGame);row("🎲","Dice Duel","Realtime synchronized round",this::openRoomGame);row("🔢","Pick Number","Realtime synchronized round",this::openRoomGame);row("🪙","Coin Pick","Realtime synchronized round",this::openRoomGame);row("🎡","Lucky Wheel","Realtime synchronized round",this::openRoomGame);row("🎯","Bingo Pick","Realtime synchronized round",this::openRoomGame);row("🎤","Return to Party","Invite more players",()->KingNav.openRoot(this,0));}
    private void openGame(String code){
        if("ludo".equalsIgnoreCase(code)){startActivity(new Intent(this,KingLudoLobbyActivity.class));return;}
        if(roomId!=null&&!roomId.trim().isEmpty()){
            Intent i=new Intent(this,RoomGameActivity.class);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);i.putExtra("displayName",safe(mainPrefs.getString("name","KING User"),"KING User"));i.putExtra("requestedGame",code);startActivity(i);return;
        }
        Intent i=new Intent(this,PartyActivity.class);i.putExtra("requestedGame",code);i.putExtra("displayName",safe(mainPrefs.getString("name","KING User"),"KING User"));startActivity(i);
    }
    private void openRoomGame(){if(roomId.isEmpty()){toast("Join a live Party room first");KingNav.openRoot(this,0);return;}Intent i=new Intent(this,RoomGameActivity.class);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);startActivity(i);}

    private void musicCenter(){row("🎵","Music Library","Browse room music and songs","detail_music_library");row("🎤","Song Request","KTV request queue","detail_song_request");row("📋","KTV Queue","Requested / playing / completed","detail_ktv_queue");row("📻","Radio Mic Queue","Loop mic and radio queue","detail_radio_queue");row("🎚","Audio Settings","Mic, music and room audio","detail_audio_settings");}
    private void videoCenter(){hero("🎙 Voice & Video","Native Jitsi/WebRTC engine with fallback");row("🎙","Voice Room","Open native voice session",()->openMeet(false));row("📹","Multi Video","Open native video session",()->openMeet(true));row("🎚","Voice Filter","Original KING Plus filter presets","detail_voice_filter");row("✨","Beauty / Camera","Video camera and beauty controls","detail_beauty");row("🎛","Audio Focus & Reconnect","Fallback reconnect and permission recovery","detail_media_recovery");}
    private void openMeet(boolean video){String rid=roomId.isEmpty()?"king-public":roomId;NativeMeetBridge.launch(this,rid,roomName,safe(mainPrefs.getString("name","KING User"),"KING User"),video);}
    private void taskCenter(){hero("✅ Tasks & Check-in","Activity shortcuts are available. This TEST build does not grant unverified XP or check-in rewards.");section("Open an activity");dailyTask("Join a Party room","task_party",0);dailyTask("Send a room message","task_chat",0);dailyTask("Play a game","task_game",0);dailyTask("Visit Gift Center","task_gift",0);dailyTask("Visit Community","task_social",0);section("Reward status");chip("No daily reward is claimable until a verified KING Plus server task/reward service is enabled.",()->{});}
    private void dailyTask(String name,String key,int ignoredReward){row("›",name,"Open the real activity • completion is not tracked in this TEST build",()->{if("task_party".equals(key)||"task_game".equals(key)){KingNav.openRoot(this,0);}else if("task_chat".equals(key)){KingNav.openRoot(this,3);}else if("task_gift".equals(key)){openGiftCenter();}else if("task_social".equals(key)){openCommunity("Moments");}});}
    private void eventCenter(){row("🎪","Room Activity","Templates and live room events","party_events");row("⚔","Audio PK","Party PK / score flow",()->KingNav.openRoot(this,0));row("🏆","Ranking Events","Discover rankings","discover_center");row("🎁","Gift Events","Lucky Gift, Wish and Wall","gift_center");row("💞","Relationship Events","CP and Family activities",()->openCommunity("Relationship"));}
    private void profileCenter(){row("✏","Edit Profile","Name, bio, birthday and tags","detail_edit_profile");row("🖼","My Gallery","Photos and Moments",()->openCommunity("Moments"));row("⭐","Level & Achievements","Normal Level 1–99","level_center");row("💎","VIP","VIP 0–50","vip");row("🏅","Medals & Influence","Achievements and contribution","medal_center");row("👀","Visitors","Recent profile visitors",()->openCommunity("Visitors"));row("🎁","Profile Gift Wall","Gift collection","gift_wall");row("🛡","Safety Center","Block, report and privacy tools","detail_profile_safety");}
    private void reportHistory9981(){
        String owner=me==null?"":me.getUid();
        hero("⚑ My Report History",owner.isEmpty()?"Sign in to view report receipts":"Your own submitted-report receipts sync to this KING account across devices");
        if(owner.isEmpty()){chip("No signed-in account",()->{});return;}
        section("Account-synced submissions");
        LinearLayout cloudHost=new LinearLayout(this);cloudHost.setOrientation(LinearLayout.VERTICAL);
        TextView cloudState=tv("Loading report receipts from KING Plus…",13,sub(),false);
        cloudHost.addView(cloudState,new LinearLayout.LayoutParams(-1,dp(48)));
        body.addView(cloudHost,new LinearLayout.LayoutParams(-1,-2));

        String local=mainPrefs.getString("report_history_"+owner,"");
        LinearLayout localHost=new LinearLayout(this);localHost.setOrientation(LinearLayout.VERTICAL);
        TextView localHeading=tv("On this device",12,sub(),true);localHost.addView(localHeading);
        int localCount=0;
        for(String line:local.split("\\n")){
            if(line.trim().isEmpty()||localCount>=30)continue;
            TextView item=tv("⚑ "+line,13,fg(),false);item.setBackground(bg(card(),12));
            localHost.addView(item,new LinearLayout.LayoutParams(-1,dp(54)));localCount++;
        }
        final int savedLocalCount=localCount;
        localHost.setVisibility(View.GONE);body.addView(localHost,new LinearLayout.LayoutParams(-1,-2));
        if(db==null){
            cloudState.setText("Report history is unavailable from the server.");
            if(savedLocalCount>0)localHost.setVisibility(View.VISIBLE);
        }else{
            final String expectedOwner=owner;
            db.collection("user_report_history").document(owner).collection("items")
                .orderBy("submittedAt",com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(30).get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(result->{
                    FirebaseUser current=null;try{current=FirebaseAuth.getInstance().getCurrentUser();}catch(Exception ignored){}
                    if(isFinishing()||isDestroyed()||!"report_history".equals(route)||current==null||!expectedOwner.equals(current.getUid()))return;
                    cloudHost.removeAllViews();
                    if(result.isEmpty()){
                        cloudState.setText("No account-synced reports found yet.");
                        cloudHost.addView(cloudState,new LinearLayout.LayoutParams(-1,dp(48)));
                        if(savedLocalCount>0)localHost.setVisibility(View.VISIBLE);
                    }else{
                        int shown=0;
                        for(DocumentSnapshot doc:result.getDocuments()){
                            if(shown++>=30)break;
                            String target=safe(doc.getString("target"),"KING user");
                            String reason=safe(doc.getString("reason"),"Other");
                            String when="Time unavailable";
                            com.google.firebase.Timestamp stamp=doc.getTimestamp("submittedAt");
                            if(stamp!=null)when=new java.text.SimpleDateFormat("dd MMM yyyy • HH:mm",java.util.Locale.US).format(stamp.toDate());
                            TextView item=tv("⚑ "+target+"\n"+reason+" • "+when,13,fg(),false);item.setBackground(bg(card(),12));
                            cloudHost.addView(item,new LinearLayout.LayoutParams(-1,dp(62)));
                        }
                    }
                }).addOnFailureListener(error->{
                    FirebaseUser current=null;try{current=FirebaseAuth.getInstance().getCurrentUser();}catch(Exception ignored){}
                    if(isFinishing()||isDestroyed()||!"report_history".equals(route)||current==null||!expectedOwner.equals(current.getUid()))return;
                    cloudHost.removeAllViews();cloudState.setText("Could not load account-synced report history. Check your connection.");
                    cloudHost.addView(cloudState,new LinearLayout.LayoutParams(-1,dp(58)));
                    if(savedLocalCount>0)localHost.setVisibility(View.VISIBLE);
                });
        }
        chip("This page shows your own submission receipts only. Moderation decisions and other users’ reports are private.",()->{});
    }
    private void safetyHelp9981(){
        hero("🛡 Safety & Moderation","Review account privacy, your cloud block list, and account-scoped report receipts.");
        row("🚫","Blocked users","Review and unblock cloud-blocked KING accounts","detail_blocked");
        row("⚑","My report history","Account-scoped report receipts from the server","detail_reports");
        row("🔐","Privacy controls","Discoverability, room invites and private messages","settings_privacy");
        row("📜","Community rules","Safety and fair-play rules","rules");
    }
    private void levelCenter(){LevelSystem.Snapshot s=LevelSystem.read(this);hero("🏆 TEST Activity Level "+s.level+" / "+LevelSystem.MAX_LEVEL,"Device-only preview • not account-synced or server-verified • XP has no reward value");row("🔒","Achievements","Locked until verified activity tracking is available","medal_center");row("🎮","Game Master","Game activity is not credited to a verified level","game_center");row("🎤","Voice Explorer","Party / voice participation is not credited to a verified level",()->KingNav.openRoot(this,0));row("👥","Friends","Community and followers","social_center");}
    private void medalCenter(){hero("🏅 Medals & Influence","TEST catalog • no medal is granted from device-only XP or unverified VIP");String[] n={"Rising Star","Elite Voice","Game Master","VIP Supporter","Royal Patron","KING Supreme"};String[] req={"Verified activity tracking pending","Verified activity tracking pending","Verified game results pending","Verified Wallet entitlement pending","Verified Wallet entitlement pending","Verified Wallet entitlement pending"};for(int i=0;i<n.length;i++)row("🔒",n[i],req[i],()->{});}
    private void socialCenter(){row("▣","Square / Moments","Posts and activity",()->openCommunity("Moments"));row("👤","People Search","KING ID and profiles",()->openCommunity("Search"));row("👥","Friends / Followers","Follow and private chat",()->{Intent i=new Intent(this,SocialActivity.class);startActivity(i);});row("🏠","Family","Family square and members",()->openCommunity("Family"));row("💞","Relationship / CP","Pairing and gifts",()->openCommunity("Relationship"));row("💬","Messages","Chats and notifications",()->KingNav.openRoot(this,3));}
    private void discoverCenter(){row("🏆","Contribution Ranking","Discover room/user rankings",()->startActivity(new Intent(this,DiscoverActivity.class)));row("✨","Charisma Ranking","Profiles and contribution",()->startActivity(new Intent(this,DiscoverActivity.class)));row("🎪","Activity Recommendation","Rooms and events",()->startActivity(new Intent(this,DiscoverActivity.class)));row("🎮","Game Master","Game discovery","game_center");row("📍","Nearby / Recommended","People and rooms",()->openCommunity("Search"));}

    private void openPartyPanel940(String panel){
        if(roomId==null||roomId.trim().isEmpty()){toast("Join a live Party room first");KingNav.openRoot(this,0);return;}
        Intent i=new Intent(this,PartyActivity.class);i.putExtra("directRoomId",roomId);i.putExtra("directRoomName",roomName);i.putExtra("displayName",safe(mainPrefs.getString("name","KING User"),"KING User"));i.putExtra("requestedPanel",panel);i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);startActivity(i);
    }
    private void openGiftCenter(){Intent i=new Intent(this,KingGiftCenterActivity.class);startActivity(i);}
    private void openCommunity(String tab){Intent i=new Intent(this,CommunityHubActivity.class);i.putExtra("tab",tab);startActivity(i);}
    private void openNotifications9981(){Intent i=new Intent(this,InboxActivity.class);i.putExtra("openNotifications10010",true);startActivity(i);}
    private void shareInvite(){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,"Join me on KING Plus • "+safe(mainPrefs.getString("name","KING User"),"KING User"));startActivity(Intent.createChooser(i,"Invite Friends"));}

    private void detail(String r){
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
    private void policyPage9981(String kind){
        if("privacy".equals(kind)){
            hero("🔐 Privacy Policy","Control the privacy options that are available in this TEST build.");
            section("Available controls");
            row("🛡","Open live privacy controls","Find-me, online status, room invites and private messages","settings_privacy");
            row("🚫","Blocked accounts","Review or unblock cloud-saved accounts","detail_blocked");
            chip("Official privacy-policy web URL is not configured in this app build.",()->{});
            chip("Some cloud reports are write-only for normal accounts; this page does not claim server report access.",()->{});
        }else{
            hero("📄 Terms of Service","Read the current in-app service summary for this TEST build.");
            section("Service summary");
            chip("Use KING Plus lawfully and respectfully. Do not harass, spam, impersonate, or abuse rooms, messages, gifts, or games.",()->{});
            chip("Wallet, VIP, Noble and task rewards are not granted from local TEST actions.",()->{});
            chip("Official Terms of Service URL is not configured in this app build.",()->{});
            row("📜","Community rules","Open detailed community and fair-play rules","rules");
        }
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
    }
    private String titleCase(String s){StringBuilder b=new StringBuilder();for(String x:s.split(" ")){if(x.isEmpty())continue;if(b.length()>0)b.append(' ');b.append(Character.toUpperCase(x.charAt(0))).append(x.substring(1));}return b.toString();}
}
