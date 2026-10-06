package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** KING Plus original parity center: KTV stage, PK arena, family/VIP entry and social matching. */
public class KingParityHubActivity extends Activity {
    private static final int BG=0xff170b2d, CARD=0xff2a1747, CARD2=0xff35205a, PURPLE=0xff8a49ed, GOLD=0xffffd768, MUTED=0xffc7b9d8;
    private FirebaseFirestore db; private FirebaseUser me; private LinearLayout body; private TextView title;
    private String route="home", roomId="", roomName="KING Party";
    private SharedPreferences prefs;

    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    private TextView tv(String s,int z,int c,boolean b){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);if(b)t.setTypeface(null,Typeface.BOLD);t.setPadding(dp(12),dp(7),dp(12),dp(7));return t;}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    private String safe(String s,String f){return s==null||s.trim().isEmpty()?f:s.trim();}
    private String name(){if(me!=null&&me.getDisplayName()!=null&&!me.getDisplayName().trim().isEmpty())return me.getDisplayName().trim();return prefs.getString("name","KING User");}

    @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("MainActivity",MODE_PRIVATE);route=safe(getIntent().getStringExtra("route"),"home");roomId=safe(getIntent().getStringExtra("roomId"),"");roomName=safe(getIntent().getStringExtra("roomName"),"KING Party");try{db=FirebaseFirestore.getInstance();me=FirebaseAuth.getInstance().getCurrentUser();}catch(Exception ignored){}shell();show(route);}

    private void shell(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(8),dp(8),dp(8),0);
        TextView back=tv("‹",38,Color.WHITE,true);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->finish());head.addView(back,new LinearLayout.LayoutParams(dp(52),dp(56)));
        title=tv("KING Center",20,Color.WHITE,true);head.addView(title,new LinearLayout.LayoutParams(0,dp(56),1));root.addView(head);
        HorizontalScrollView hsv=new HorizontalScrollView(this);hsv.setHorizontalScrollBarEnabled(false);LinearLayout tabs=new LinearLayout(this);tabs.setPadding(dp(8),0,dp(8),dp(8));
        String[][] all={{"✨","Home","home"},{"🎤","KTV","ktv"},{"⚔️","PK","pk"},{"🎲","Match","match"},{"🏰","Stage","stage"},{"👑","Family","family"},{"💎","VIP/Rank","vip"}};
        for(String[] x:all){TextView t=tv(x[0]+" "+x[1],12,Color.WHITE,true);t.setGravity(Gravity.CENTER);t.setBackground(bg(CARD2,16));String r=x[2];t.setOnClickListener(v->show(r));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(104),dp(42));lp.setMargins(dp(4),0,dp(4),0);tabs.addView(t,lp);}hsv.addView(tabs);root.addView(hsv,new LinearLayout.LayoutParams(-1,dp(52)));
        ScrollView sv=new ScrollView(this);sv.setFillViewport(true);body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(14),dp(10),dp(14),dp(30));sv.addView(body);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
    }

    private void show(String r){route=r;body.removeAllViews();if(title!=null)title.setText(titleFor(r));if("ktv".equals(r))ktv();else if("pk".equals(r))pk();else if("match".equals(r))match();else if("stage".equals(r))stage();else if("family".equals(r))openFamily();else if("vip".equals(r))vip();else home();}
    private String titleFor(String r){if("ktv".equals(r))return "KTV Stage";if("pk".equals(r))return "PK Arena";if("match".equals(r))return "Match";if("stage".equals(r))return "Party Stage";if("family".equals(r))return "Family";if("vip".equals(r))return "VIP & Rank";return "KING Center";}
    private void hero(String a,String b){TextView h=tv(a+"
"+b,18,Color.WHITE,true);h.setGravity(Gravity.CENTER_VERTICAL);h.setBackground(bg(CARD,18));h.setPadding(dp(18),dp(16),dp(18),dp(16));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(12));body.addView(h,lp);}
    private TextView action(String s,Runnable r){TextView t=tv(s,14,Color.WHITE,true);t.setGravity(Gravity.CENTER);t.setBackground(bg(PURPLE,14));t.setOnClickListener(v->r.run());return t;}
    private void card(String icon,String a,String b,Runnable r){LinearLayout x=new LinearLayout(this);x.setGravity(Gravity.CENTER_VERTICAL);x.setPadding(dp(12),dp(8),dp(12),dp(8));x.setBackground(bg(CARD,16));TextView i=tv(icon,28,Color.WHITE,false);i.setGravity(Gravity.CENTER);x.addView(i,new LinearLayout.LayoutParams(dp(54),dp(62)));LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);TextView aa=tv(a,16,Color.WHITE,true);aa.setPadding(0,0,0,0);TextView bb=tv(b,11,MUTED,false);bb.setPadding(0,dp(2),0,0);info.addView(aa);info.addView(bb);x.addView(info,new LinearLayout.LayoutParams(0,dp(62),1));TextView ar=tv("›",28,GOLD,true);ar.setGravity(Gravity.CENTER);x.addView(ar,new LinearLayout.LayoutParams(dp(36),dp(62)));x.setOnClickListener(v->r.run());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(78));lp.setMargins(0,dp(5),0,dp(5));body.addView(x,lp);}
    private boolean cloud(){if(me==null||db==null){toast("Google / Firebase sign-in required");return false;}return true;}
    private boolean room(){if(!cloud())return false;if(roomId.isEmpty()){toast("Open this from a live Party room");return false;}return true;}

    private void home(){hero("✨ KING Plus Parity Center","Original KING Plus features inspired by modern social voice-room apps — no copied proprietary assets.");card("🎤","KTV Stage","Shared song requests and current singer stage",()->show("ktv"));card("⚔️","PK Arena","Red vs Blue room battle with gift-backed score",()->show("pk"));card("🎲","Match","Browse real signed-in KING profiles",()->show("match"));card("🏰","Party Stage","Switch room scene/theme: Royal, KTV, PK, Galaxy",()->show("stage"));card("👑","Family","Real Family create/join/chat center",()->show("family"));card("💎","VIP & Rank","VIP progress, cosmetics and public level ranking",()->show("vip"));}

    private void ktv(){hero("🎤 KTV • "+roomName,roomId.isEmpty()?"Open from Party Room for synchronized queue":"Room-synchronized requests • host/co-host can put a song on stage");if(!roomId.isEmpty()){LinearLayout buttons=new LinearLayout(this);TextView add=action("＋ Request song",this::requestSong);buttons.addView(add,new LinearLayout.LayoutParams(0,dp(48),1));TextView refresh=action("↻ Refresh",this::ktv);LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,dp(48),1);rp.setMargins(dp(8),0,0,0);buttons.addView(refresh,rp);body.addView(buttons);}
        if(!room())return;db.collection("live_rooms").document(roomId).get().addOnSuccessListener(d->{String current=safe(d.getString("currentSong"),"No song on stage");TextView now=tv("🎙 NOW ON STAGE
"+current,16,GOLD,true);now.setGravity(Gravity.CENTER);now.setBackground(bg(0xff3c245e,16));body.addView(now,new LinearLayout.LayoutParams(-1,dp(78)));});
        body.addView(tv("Queue",16,Color.WHITE,true));db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(100).get().addOnSuccessListener(q->{int shown=0;for(DocumentSnapshot d:q.getDocuments()){if(!"ktv_request".equals(d.getString("type")))continue;String song=safe(d.getString("songName"),safe(d.getString("text"),"Song"));String who=safe(d.getString("actorName"),"User");TextView row=tv("🎵 "+song+"
   requested by "+who,14,Color.WHITE,true);row.setBackground(bg(CARD,14));row.setOnClickListener(v->startSong(song));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(64));lp.setMargins(0,dp(4),0,dp(4));body.addView(row,lp);if(++shown>=25)break;}if(shown==0)body.addView(tv("No song requests yet.",13,MUTED,false));}).addOnFailureListener(e->body.addView(tv("KTV queue unavailable: "+safe(e.getMessage(),"permission"),12,MUTED,false)));}
    private void requestSong(){if(!room())return;EditText e=new EditText(this);e.setHint("Song title");e.setSingleLine(true);new AlertDialog.Builder(this).setTitle("Request KTV song").setView(e).setPositiveButton("Request",(d,w)->{String s=e.getText().toString().trim();if(s.isEmpty())return;Map<String,Object>m=new HashMap<>();m.put("actorUid",me.getUid());m.put("actorName",name());m.put("type","ktv_request");m.put("songName",s);m.put("text",name()+" requested 🎵 "+s);m.put("createdAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).collection("events").add(m).addOnSuccessListener(v->{toast("Added to KTV queue");show("ktv");}).addOnFailureListener(x->toast("Request failed"));}).setNegativeButton("Cancel",null).show();}
    private void startSong(String song){if(!room())return;new AlertDialog.Builder(this).setTitle(song).setMessage("Host/co-host can put this song on the room stage.").setPositiveButton("Start on stage",(d,w)->{Map<String,Object>m=new HashMap<>();m.put("currentSong",song);m.put("updatedAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).update(m).addOnSuccessListener(v->{toast("KTV stage updated");show("ktv");}).addOnFailureListener(e->toast("Host/co-host permission required"));}).setNegativeButton("Close",null).show();}

    private void pk(){hero("⚔️ Audio PK • "+roomName,"Join Red or Blue. Gifts sent by team members increase the live PK score.");if(!room())return;LinearLayout join=new LinearLayout(this);TextView red=action("🔴 Join Red",()->joinPk("red"));red.setBackground(bg(0xffb93852,14));join.addView(red,new LinearLayout.LayoutParams(0,dp(50),1));TextView blue=action("🔵 Join Blue",()->joinPk("blue"));blue.setBackground(bg(0xff356ac3,14));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,dp(50),1);bp.setMargins(dp(8),0,0,0);join.addView(blue,bp);body.addView(join);TextView start=action("⚡ Start / reset PK round",this::startPk);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(48));sp.setMargins(0,dp(8),0,dp(10));body.addView(start,sp);loadPkScore();}
    private void joinPk(String team){if(!room())return;Map<String,Object>m=new HashMap<>();m.put("actorUid",me.getUid());m.put("actorName",name());m.put("type","pk_join");m.put("team",team);m.put("text",name()+" joined "+("red".equals(team)?"Red":"Blue")+" PK team");m.put("createdAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).collection("events").add(m).addOnSuccessListener(v->{toast("Joined "+team+" team");show("pk");}).addOnFailureListener(e->toast("Could not join PK"));}
    private void startPk(){if(!room())return;Map<String,Object>m=new HashMap<>();m.put("active",true);m.put("roundId",System.currentTimeMillis());m.put("actorUid",me.getUid());m.put("startedAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).collection("room_settings").document("pk").set(m).addOnSuccessListener(v->toast("PK round started")).addOnFailureListener(e->toast("Host/co-host permission required"));}
    private void loadPkScore(){db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(250).get().addOnSuccessListener(q->{Map<String,String>team=new LinkedHashMap<>();long red=0,blue=0;for(DocumentSnapshot d:q.getDocuments()){String actor=d.getString("actorUid");String type=d.getString("type");if("pk_join".equals(type)&&actor!=null&&!team.containsKey(actor))team.put(actor,safe(d.getString("team"),""));}for(DocumentSnapshot d:q.getDocuments()){if(!"gift".equals(d.getString("type")))continue;String actor=d.getString("actorUid");Long v=d.getLong("giftValue");long score=v==null?0:Math.max(0,v);String t=team.get(actor);if("red".equals(t))red+=score;else if("blue".equals(t))blue+=score;}TextView score=tv("🔴 RED  "+red+"      VS      "+blue+"  BLUE 🔵
"+(red==blue?"🤝 Draw":red>blue?"🏆 Red leads":"🏆 Blue leads"),20,Color.WHITE,true);score.setGravity(Gravity.CENTER);score.setBackground(bg(CARD2,18));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(100));lp.setMargins(0,dp(8),0,dp(8));body.addView(score,lp);TextView refresh=action("↻ Refresh score",()->show("pk"));body.addView(refresh,new LinearLayout.LayoutParams(-1,dp(48)));}).addOnFailureListener(e->body.addView(tv("PK score unavailable",13,MUTED,false)));}

    private void match(){hero("🎲 KING Match","Browse real public KING Plus profiles. Open a card to follow, message or send gifts.");if(!cloud())return;db.collection("public_profiles").limit(40).get().addOnSuccessListener(q->{List<DocumentSnapshot>list=new ArrayList<>(q.getDocuments());Collections.shuffle(list);int n=0;for(DocumentSnapshot d:list){String uid=safe(d.getString("uid"),d.getId());if(me!=null&&uid.equals(me.getUid()))continue;String nm=safe(d.getString("displayName"),"KING User");Long lv=d.getLong("level"),vip=d.getLong("vipLevel");TextView row=tv("👤  "+nm+"   💎 VIP "+(vip==null?0:vip)+"
     KING ID "+safe(d.getString("publicId"),publicId(uid))+" • Lv."+(lv==null?1:lv),14,Color.WHITE,true);row.setBackground(bg(CARD,16));row.setOnClickListener(v->openProfile(uid,nm));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(70));lp.setMargins(0,dp(5),0,dp(5));body.addView(row,lp);if(++n>=24)break;}if(n==0)body.addView(tv("No public profiles yet.",13,MUTED,false));}).addOnFailureListener(e->body.addView(tv("Match list unavailable: "+safe(e.getMessage(),"permission"),13,MUTED,false)));}
    private String publicId(String uid){long h=Math.abs((long)safe(uid,"").hashCode());return String.format(Locale.US,"%06d",h%1000000L);}
    private void openProfile(String uid,String nm){Intent i=new Intent(this,KingPublicProfileActivity.class);i.putExtra("uid",uid);i.putExtra("name",nm);startActivity(i);}

    private void stage(){hero("🏰 Party Stage","Original KING Plus room scenes. Host can apply a scene to the live room.");String[] themes={"Royal","KTV","PK","Galaxy","Festival","Ice","Neon","Love","Game"};String[] icons={"👑","🎤","⚔️","🌌","🎉","❄️","💜","💗","🎮"};for(int i=0;i<themes.length;i++){String th=themes[i],ic=icons[i];card(ic,th+" Stage",stageDescription(th),()->applyTheme(th));}}
    private String stageDescription(String t){if("KTV".equals(t))return "Blue music stage with singer focus";if("PK".equals(t))return "Red/blue battle presentation";if("Royal".equals(t))return "Gold VIP room presentation";if("Galaxy".equals(t))return "Purple cosmic party scene";return t+" room presentation";}
    private void applyTheme(String theme){if(!room())return;Map<String,Object>m=new HashMap<>();m.put("theme",theme);m.put("updatedAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).update(m).addOnSuccessListener(v->toast(theme+" stage applied")).addOnFailureListener(e->toast("Host permission required"));}

    private void openFamily(){Intent i=new Intent(this,CommunityHubActivity.class);i.putExtra("tab","Family");startActivity(i);body.addView(tv("Family Center opened. Return here to continue.",13,MUTED,false));}
    private void vip(){hero("💎 VIP & Rank","VIP remains progression-based in this no-billing build. Levels come from real KING Plus activity.");LevelSystem.Snapshot p=LevelSystem.read(this);TextView meCard=tv("👑 "+name()+"
VIP "+p.vipLevel+" • "+LevelSystem.vipName(p.vipLevel)+"   |   Lv."+p.level+" • "+LevelSystem.levelTier(p.level)+"
"+p.vipPoints+" VIP points • "+p.xp+" XP",17,Color.WHITE,true);meCard.setBackground(bg(CARD2,18));body.addView(meCard,new LinearLayout.LayoutParams(-1,dp(106)));LinearLayout acts=new LinearLayout(this);TextView perks=action("VIP privileges",()->{Intent i=new Intent(this,KingDeepFlowActivity.class);i.putExtra("route","vip");startActivity(i);});acts.addView(perks,new LinearLayout.LayoutParams(0,dp(48),1));TextView collection=action("Collection",()->{Intent i=new Intent(this,CommunityHubActivity.class);i.putExtra("tab","Collection");startActivity(i);});LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(48),1);cp.setMargins(dp(8),0,0,0);acts.addView(collection,cp);body.addView(acts);body.addView(tv("Public level ranking",16,Color.WHITE,true));if(!cloud())return;db.collection("public_profiles").orderBy("level",Query.Direction.DESCENDING).limit(30).get().addOnSuccessListener(q->{int rank=1;for(DocumentSnapshot d:q.getDocuments()){String uid=safe(d.getString("uid"),d.getId()),nm=safe(d.getString("displayName"),"KING User");Long lv=d.getLong("level"),vv=d.getLong("vipLevel");TextView row=tv((rank<=3?(rank==1?"🥇":rank==2?"🥈":"🥉"):"#"+rank)+"   "+nm+"   • Lv."+(lv==null?1:lv)+" • VIP "+(vv==null?0:vv),13,Color.WHITE,true);row.setBackground(bg(CARD,12));row.setOnClickListener(v->openProfile(uid,nm));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(48));lp.setMargins(0,dp(3),0,dp(3));body.addView(row,lp);rank++;}}).addOnFailureListener(e->body.addView(tv("Ranking unavailable",13,MUTED,false)));}
}
