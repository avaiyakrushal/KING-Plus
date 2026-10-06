package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** Final production verification dashboard for two-phone room testing. */
public class KingProductionTestActivity extends Activity {
    private static final int BG=0xff130a24,CARD=0xff291747,CARD2=0xff39215f,PURPLE=0xff8a49ed,GREEN=0xff45e6a0,RED=0xffff8d9b,GOLD=0xffffd768,MUTED=0xffc5bad2;
    private FirebaseFirestore db; private FirebaseUser me; private LinearLayout body;
    private String roomId="",roomName="KING Party",displayName="KING User",lastPing="";
    private int memberCount,onlineCount,deviceCount,seatCount,voiceCount,messageCount,giftCount,emojiCount,ackCount,readyCount;
    private long lastRefresh;

    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    private TextView tv(String s,int z,int c,boolean b){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);if(b)t.setTypeface(null,Typeface.BOLD);t.setPadding(dp(12),dp(8),dp(12),dp(8));return t;}
    private TextView button(String s,Runnable r){TextView t=tv(s,14,Color.WHITE,true);t.setGravity(Gravity.CENTER);t.setBackground(bg(PURPLE,14));t.setOnClickListener(v->r.run());return t;}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}

    @Override public void onCreate(Bundle b){
        super.onCreate(b);KingStability.install(this);
        roomId=getIntent().getStringExtra("roomId");if(roomId==null)roomId="";
        roomName=getIntent().getStringExtra("roomName");if(roomName==null||roomName.trim().isEmpty())roomName="KING Party";
        try{db=FirebaseFirestore.getInstance();me=FirebaseAuth.getInstance().getCurrentUser();}catch(Exception ignored){}
        if(me!=null&&me.getDisplayName()!=null&&!me.getDisplayName().trim().isEmpty())displayName=me.getDisplayName().trim();
        shell();refresh();
    }

    private void shell(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(8),dp(8),dp(8),0);
        TextView back=tv("‹",38,Color.WHITE,true);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->finish());head.addView(back,new LinearLayout.LayoutParams(dp(52),dp(56)));
        TextView title=tv("Production Test Center",20,Color.WHITE,true);head.addView(title,new LinearLayout.LayoutParams(0,dp(56),1));root.addView(head);
        ScrollView sv=new ScrollView(this);body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(14),dp(8),dp(14),dp(30));sv.addView(body);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
    }

    private void refresh(){
        body.removeAllViews();
        hero();
        if(me==null||db==null||roomId.isEmpty()){
            state("Firebase / Room",false,"Sign in and open this from a live Party Room.");
            return;
        }
        state("Firebase project",true,safeProject());
        state("Signed in",true,displayName+" • "+me.getUid().substring(0,Math.min(8,me.getUid().length())));
        state("Room",true,roomName+" • "+roomId);
        memberCount=onlineCount=deviceCount=seatCount=voiceCount=messageCount=giftCount=emojiCount=ackCount=readyCount=0;
        LinearLayout actions=new LinearLayout(this);
        actions.addView(button("↻ Refresh",this::refresh),new LinearLayout.LayoutParams(0,dp(48),1));
        TextView ping=button("📡 Two-phone Ping",this::sendPing);LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(0,dp(48),1);pp.setMargins(dp(8),0,0,0);actions.addView(ping,pp);body.addView(actions);
        LinearLayout tests=new LinearLayout(this);
        tests.addView(button("✨ Test Emoji",this::sendEmojiTest),new LinearLayout.LayoutParams(0,dp(48),1));
        TextView gift=button("🎁 Test Gift",this::sendGiftTest);LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(0,dp(48),1);gp.setMargins(dp(8),0,0,0);tests.addView(gift,gp);LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,dp(48));tp.setMargins(0,dp(8),0,0);body.addView(tests,tp);
        LinearLayout voiceGame=new LinearLayout(this);
        voiceGame.addView(button("🎙 Voice Room",this::openVoice),new LinearLayout.LayoutParams(0,dp(48),1));
        TextView game=button("🎮 Room Games",this::openGames);LinearLayout.LayoutParams gameLp=new LinearLayout.LayoutParams(0,dp(48),1);gameLp.setMargins(dp(8),0,0,0);voiceGame.addView(game,gameLp);LinearLayout.LayoutParams vgp=new LinearLayout.LayoutParams(-1,dp(48));vgp.setMargins(0,dp(8),0,dp(8));body.addView(voiceGame,vgp);
        loadRoomHealth();
    }

    private void hero(){
        TextView h=tv("🧪 "+roomName+"\nUse this screen on both phones. Green status means the client can see the same Firebase room/state.",17,Color.WHITE,true);
        h.setBackground(bg(CARD2,18));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(10));body.addView(h,lp);
    }

    private void loadRoomHealth(){
        final long now=System.currentTimeMillis();lastRefresh=now;
        db.collection("live_rooms").document(roomId).get().addOnSuccessListener(room->{
            state("Room readable",room.exists(),room.exists()?"YES":"Room document not found");
        }).addOnFailureListener(e->{state("Room readable",false,msg(e));KingStability.nonFatal(this,"prod-room",e);});

        db.collection("live_rooms").document(roomId).collection("members").get().addOnSuccessListener(q->{
            HashSet<String> devices=new HashSet<>();int online=0,voice=0;
            StringBuilder names=new StringBuilder();
            for(DocumentSnapshot d:q.getDocuments()){
                memberCount++;
                String device=d.getString("deviceId");if(device!=null&&!device.trim().isEmpty())devices.add(device);
                Timestamp ts=d.getTimestamp("lastSeenAt");boolean fresh=ts!=null&&now-ts.toDate().getTime()<60000L;
                if(fresh)online++;
                if(Boolean.TRUE.equals(d.getBoolean("voiceJoined")))voice++;
                if(names.length()<240){if(names.length()>0)names.append(", ");names.append(safe(d.getString("name"),"User"));}
            }
            onlineCount=online;voiceCount=voice;deviceCount=devices.size();
            boolean two=onlineCount>=2&&deviceCount>=2;
            state("Two-phone presence",two,"members="+memberCount+" • online<60s="+onlineCount+" • unique devices="+deviceCount+"\n"+names);
            state("Voice presence",voiceCount>=2,"voiceJoined="+voiceCount+" • both phones should tap Mic/Voice");
        }).addOnFailureListener(e->{state("Two-phone presence",false,msg(e));KingStability.nonFatal(this,"prod-members",e);});

        db.collection("live_rooms").document(roomId).collection("seats").get().addOnSuccessListener(q->{seatCount=q.size();state("Seats sync",seatCount>=1,"occupied seats="+seatCount);}).addOnFailureListener(e->state("Seats sync",false,msg(e)));
        db.collection("live_rooms").document(roomId).collection("messages").orderBy("createdAt",Query.Direction.DESCENDING).limit(20).get().addOnSuccessListener(q->{messageCount=q.size();state("Chat sync",true,"recent messages readable="+messageCount);}).addOnFailureListener(e->state("Chat sync",false,msg(e)));
        db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(100).get().addOnSuccessListener(q->{
            int gifts=0,emoji=0,acks=0;for(DocumentSnapshot d:q.getDocuments()){String type=d.getString("type");if("gift".equals(type))gifts++;if("live_emoji".equals(type))emoji++;if("sync_ack".equals(type)&&lastPing.equals(d.getString("testId")))acks++;}
            giftCount=gifts;emojiCount=emoji;ackCount=acks;state("Gift / Emoji events",true,"recent gifts="+giftCount+" • live emoji="+emojiCount);
            if(!lastPing.isEmpty())state("Two-phone ping echo",ackCount>=1,"acknowledgements from other devices="+ackCount);
        }).addOnFailureListener(e->state("Gift / Emoji events",false,msg(e)));
        db.collection("live_rooms").document(roomId).collection("game_ready").get().addOnSuccessListener(q->{readyCount=q.size();state("Multiplayer game backend",true,"Ready players="+readyCount+" • open Room Games on both phones");}).addOnFailureListener(e->state("Multiplayer game backend",false,msg(e)));
        db.collection("live_rooms").document(roomId).collection("game_state").document("current").get().addOnSuccessListener(d->{String st=d.exists()?safe(d.getString("status"),"state present"):"no active round";state("Shared game state",true,st);}).addOnFailureListener(e->state("Shared game state",false,msg(e)));
        db.collection("public_profiles").document(me.getUid()).get().addOnSuccessListener(d->state("Profile cloud sync",d.exists(),"public profile "+(d.exists()?"found":"missing"))).addOnFailureListener(e->state("Profile cloud sync",false,msg(e)));
    }

    private void sendPing(){
        if(!ready())return;lastPing=UUID.randomUUID().toString().substring(0,8);
        Map<String,Object> e=baseEvent("sync_test","Production sync test "+lastPing);e.put("testId",lastPing);
        db.collection("live_rooms").document(roomId).collection("events").add(e).addOnSuccessListener(v->{toast("Ping sent • keep Party Room open on the second phone");body.postDelayed(this::refresh,3500);}).addOnFailureListener(x->error("Ping failed",x));
    }
    private void sendEmojiTest(){
        if(!ready())return;Map<String,Object> e=baseEvent("live_emoji","🧪✨");e.put("emoji","🧪✨");
        db.collection("live_rooms").document(roomId).collection("events").add(e).addOnSuccessListener(v->toast("Test emoji sent to the room")).addOnFailureListener(x->error("Emoji test failed",x));
    }
    private void sendGiftTest(){
        if(!ready())return;Map<String,Object> e=baseEvent("gift",displayName+" sent 🧪 Test Gift • no coins");e.put("giftName","Test Gift");e.put("giftIcon","🧪");e.put("giftQty",1);e.put("giftValue",0);e.put("targetUid","");e.put("targetName","Room");
        db.collection("live_rooms").document(roomId).collection("events").add(e).addOnSuccessListener(v->toast("Zero-cost test gift event sent")).addOnFailureListener(x->error("Gift test failed",x));
    }
    private Map<String,Object> baseEvent(String type,String text){Map<String,Object> e=new HashMap<>();e.put("actorUid",me.getUid());e.put("actorName",displayName);e.put("type",type);e.put("text",text);e.put("createdAt",FieldValue.serverTimestamp());return e;}
    private void openVoice(){if(!ready())return;NativeMeetBridge.launch(this,roomId,roomName,displayName,false);}
    private void openGames(){if(!ready())return;Intent i=new Intent(this,RoomGameActivity.class);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);startActivity(i);}
    private boolean ready(){if(me==null||db==null||roomId.isEmpty()){toast("Live signed-in Party Room required");return false;}return true;}

    private void state(String name,boolean ok,String detail){
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setBackground(bg(CARD,15));card.setPadding(dp(12),dp(8),dp(12),dp(8));
        TextView a=tv((ok?"✅ ":"⚠️ ")+name,15,ok?GREEN:RED,true);a.setPadding(0,0,0,0);TextView b=tv(detail,12,MUTED,false);b.setPadding(0,dp(3),0,0);card.addView(a);card.addView(b);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(4),0,dp(4));body.addView(card,lp);
    }
    private String safeProject(){try{return String.valueOf(com.google.firebase.FirebaseApp.getInstance().getOptions().getProjectId());}catch(Exception e){return "unavailable";}}
    private String safe(String s,String f){return s==null||s.trim().isEmpty()?f:s.trim();}
    private String msg(Exception e){return e==null||e.getLocalizedMessage()==null?"unknown error":e.getLocalizedMessage();}
    private void error(String title,Exception e){KingStability.nonFatal(this,"production-test",e);new AlertDialog.Builder(this).setTitle(title).setMessage(msg(e)).setPositiveButton("OK",null).show();}
}