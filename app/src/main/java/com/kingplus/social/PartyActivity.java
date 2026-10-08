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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PartyActivity extends Activity {
    private static final int BG = 0xff24133f;
    private static final int BG2 = 0xff160d2a;
    private static final int CARD = 0xff3a2856;
    private static final int PURPLE = 0xff8a49ed;
    private static final int PINK = 0xffe04f9d;
    private static final int MUTED = 0xffcfc4df;
    private static final int GOLD = 0xffffd768;

    private FirebaseFirestore db;
    private FirebaseUser user;
    private SharedPreferences prefs;
    private String displayName;
    private String roomId;
    private String roomName;
    private String ownerUid;
    private String ownerName;
    private boolean cloudRoom;
    private int mySeat = -1;
    private boolean micOn;
    private boolean soundOn = true;
    private boolean roomLocked;
    private boolean muteAll;
    private String announcement = "Welcome to KING Plus • Be friendly and have fun";
    private int localCoins = 2500;

    private LinearLayout page;
    private LinearLayout seatsBox;
    private LinearLayout feedBox;
    private LinearLayout chatBox;
    private TextView viewerLabel;
    private TextView announcementLabel;
    private TextView micLabel;
    private TextView followLabel;

    private ListenerRegistration roomsListener;
    private ListenerRegistration roomListener;
    private ListenerRegistration seatsListener;
    private ListenerRegistration membersListener;
    private ListenerRegistration messagesListener;
    private ListenerRegistration eventsListener;

    private final Map<Integer,String> seatNames = new HashMap<>();
    private final Map<Integer,String> seatUids = new HashMap<>();
    private final Map<Integer,Boolean> seatMics = new HashMap<>();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        try { db = FirebaseFirestore.getInstance(); } catch (Exception ignored) { db = null; }
        try { user = FirebaseAuth.getInstance().getCurrentUser(); } catch (Exception ignored) { user = null; }
        prefs = getSharedPreferences("king_party", MODE_PRIVATE);
        displayName = getIntent().getStringExtra("displayName");
        if (displayName == null || displayName.trim().isEmpty()) displayName = safeName();
        localCoins = prefs.getInt("coins", 2500);
        try {
            renderLobby("Hot");
        } catch (RuntimeException error) {
            showPartyRecovery(error);
        }
    }

    private void showPartyRecovery(RuntimeException error) {
        android.util.Log.e("KINGPlusParty", "Party room failed to open", error);
        String detail = android.util.Log.getStackTraceString(error);
        if (detail.length() > 1800) detail = detail.substring(0, 1800);
        getSharedPreferences("king_party", MODE_PRIVATE).edit()
            .putString("last_party_error", detail).apply();
        LinearLayout recovery = new LinearLayout(this);
        recovery.setOrientation(LinearLayout.VERTICAL);
        recovery.setPadding(dp(24), dp(48), dp(24), dp(24));
        recovery.setBackgroundColor(Color.WHITE);
        TextView title = tv("Party Room could not open", 22, Color.BLACK, true);
        recovery.addView(title);
        TextView details = tv("An error was saved on this phone. You can return to Home safely.\n\n" + detail,
            13, 0xff555555, false);
        recovery.addView(details);
        Button back = new Button(this);
        back.setText("Back to Home");
        back.setOnClickListener(v -> finish());
        recovery.addView(back);
        setContentView(recovery);
    }

    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
    private GradientDrawable bg(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private TextView tv(String text, int size, int color, boolean bold) {
        TextView v = new TextView(this); v.setText(text); v.setTextSize(size); v.setTextColor(color);
        if (bold) v.setTypeface(null, Typeface.BOLD);
        v.setPadding(dp(5), dp(6), dp(5), dp(6)); return v;
    }
    private TextView pill(String text, int color, Runnable action) {
        TextView v = tv(text, 13, Color.WHITE, true); v.setGravity(Gravity.CENTER); v.setBackground(bg(color, 18));
        if (action != null) v.setOnClickListener(x -> action.run()); return v;
    }
    private void clearListeners() {
        ListenerRegistration[] ls = {roomsListener,roomListener,seatsListener,membersListener,messagesListener,eventsListener};
        for (ListenerRegistration l : ls) if (l != null) l.remove();
        roomsListener = roomListener = seatsListener = membersListener = messagesListener = eventsListener = null;
    }

    private void renderLobby(String selected) {
        clearListeners(); unregisterMember(); roomId = null; cloudRoom = false;
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(0xfff7f7fb);

        LinearLayout head = new LinearLayout(this); head.setGravity(Gravity.CENTER_VERTICAL); head.setPadding(dp(16),dp(12),dp(12),dp(4));
        TextView title = tv("Party", 30, 0xff171717, true); head.addView(title,new LinearLayout.LayoutParams(0,dp(58),1));
        TextView create = pill("＋ Create", PURPLE, this::createRoomDialog); head.addView(create,new LinearLayout.LayoutParams(dp(105),dp(44))); root.addView(head);

        LinearLayout tabs = new LinearLayout(this); tabs.setPadding(dp(12),dp(4),dp(12),dp(8));
        String[] names = {"Hot","Event","Date","Music","Game"};
        for (String n : names) {
            TextView t = pill(n, n.equals(selected)?PURPLE:0xffdcd9e4, () -> renderLobby(n));
            t.setTextColor(n.equals(selected)?Color.WHITE:0xff55515f);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,dp(42),1); p.setMargins(dp(3),0,dp(3),0); tabs.addView(t,p);
        }
        root.addView(tabs);

        ScrollView scroll = new ScrollView(this); page = new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setPadding(dp(14),dp(4),dp(14),dp(18)); scroll.addView(page);
        addPromoCard();
        if (user != null && db != null) {
            TextView mode = tv("☁ Live Firebase rooms • signed in as " + safeName(), 12, 0xff77717f, false); page.addView(mode);
            LinearLayout cloudList = new LinearLayout(this); cloudList.setOrientation(LinearLayout.VERTICAL); page.addView(cloudList);
            roomsListener = db.collection("live_rooms").orderBy("createdAt", Query.Direction.DESCENDING).limit(30)
                .addSnapshotListener((snap,error) -> {
                    if (error != null) { toast("Cloud rooms unavailable: " + msg(error)); return; }
                    cloudList.removeAllViews();
                    if (snap == null || snap.isEmpty()) {
                        TextView e = tv("No cloud rooms yet. Create the first one.",14,0xff88828f,false); cloudList.addView(e);
                    } else {
                        for (DocumentSnapshot doc : snap.getDocuments()) {
                            if (Boolean.TRUE.equals(doc.getBoolean("closed"))) continue;
                            String name = str(doc,"name","Live Party");
                            String host = str(doc,"ownerName","KING Host");
                            String id = doc.getId();
                            addRoomCard(cloudList,"🔥 " + name,"Host: " + host + "   •   LIVE",() -> openCloudRoom(id,name,doc.getString("ownerUid"),host));
                        }
                    }
                });
        } else {
            TextView mode = tv("FREE TEST MODE • local Party works without Firebase billing",12,0xff77717f,false); page.addView(mode);
        }

        addRoomGridRow(page,
            "💜 Sweet Girls","Priya • 358","Sweet Girls","Priya",
            "🎵 Music & Friends","DJ Max • 212","Music & Friends","DJ Max");
        addRoomGridRow(page,
            "👑 KING Lounge","KING Host • 186","KING Lounge","KING Host",
            "🎮 Game Talk","Alex • 96","Game Talk","Alex");
        addRoomGridRow(page,
            "💞 Make Friends","Riya • 154","Make Friends","Riya",
            "🎤 Singing Club","Neha • 128","Singing Club","Neha");
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        addBottomNav(root,0); setContentView(root);
    }

    private void addPromoCard() {
        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(dp(16),dp(14),dp(16),dp(14)); card.setBackground(bg(0xff5d35a8,18));
        TextView a = tv("🎤 Live Voice Party",20,Color.WHITE,true); card.addView(a);
        TextView b = tv("Join rooms • take a seat • talk • chat • send gifts • make friends",13,0xffeee6ff,false); card.addView(b);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,dp(92)); p.setMargins(0,dp(5),0,dp(10)); page.addView(card,p);
    }
    private void addRoomCard(LinearLayout host,String title,String sub,Runnable action) {
        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(dp(16),dp(11),dp(16),dp(11)); card.setBackground(bg(Color.WHITE,16));
        TextView a = tv(title,16,0xff202020,true); card.addView(a); TextView b = tv(sub,13,0xff888888,false); card.addView(b);
        card.setOnClickListener(v -> action.run()); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,dp(72)); p.setMargins(0,dp(5),0,dp(5)); host.addView(card,p);
    }
    private void addRoomGridRow(LinearLayout host,
                                String title1,String sub1,String room1,String owner1,
                                String title2,String sub2,String room2,String owner2) {
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER);
        addSmallRoomCard(row,title1,sub1,() -> openLocalRoom(room1,owner1));
        addSmallRoomCard(row,title2,sub2,() -> openLocalRoom(room2,owner2));
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1,dp(128)); rp.setMargins(0,dp(3),0,dp(3)); host.addView(row,rp);
    }
    private void addSmallRoomCard(LinearLayout row,String title,String sub,Runnable action) {
        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setGravity(Gravity.BOTTOM); card.setPadding(dp(12),dp(12),dp(12),dp(10));
        card.setBackground(bg(0xffeee8f8,18));
        TextView live = pill("● LIVE",PINK,null); live.setTextSize(10); card.addView(live,new LinearLayout.LayoutParams(dp(62),dp(28)));
        TextView a = tv(title,15,0xff21172f,true); card.addView(a);
        TextView b = tv(sub,12,0xff756b7d,false); card.addView(b);
        card.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0,-1,1); cp.setMargins(dp(3),dp(3),dp(3),dp(3)); row.addView(card,cp);
    }

    private void createRoomDialog() {
        final EditText e = new EditText(this); e.setHint("Party room name"); e.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("Create Party Room").setView(e).setNegativeButton("Cancel",null)
            .setPositiveButton("Create",(d,w) -> {
                String name = e.getText().toString().trim(); if (name.length()<2) { toast("Enter a room name"); return; }
                if (user != null && db != null) createCloudRoom(name); else openLocalRoom(name,displayName);
            }).show();
    }
    private void createCloudRoom(String name) {
        Map<String,Object> r = new HashMap<>(); r.put("name",name); r.put("ownerUid",user.getUid()); r.put("ownerName",safeName());
        r.put("announcement",announcement); r.put("locked",false); r.put("muteAll",false); r.put("closed",false); r.put("createdAt",FieldValue.serverTimestamp()); r.put("updatedAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").add(r).addOnSuccessListener(ref -> openCloudRoom(ref.getId(),name,user.getUid(),safeName()))
            .addOnFailureListener(e -> toast("Create failed: " + msg(e)));
    }
    private void openCloudRoom(String id,String name,String hostUid,String hostName) {
        roomId=id; roomName=name; ownerUid=hostUid; ownerName=hostName; cloudRoom=true; mySeat=-1; micOn=false;
        registerMember(); renderParty();
    }
    private void openLocalRoom(String name,String hostName) {
        roomName=name; roomId="local_"+Math.abs(name.hashCode()); ownerName=hostName; ownerUid=null; cloudRoom=false;
        mySeat=prefs.getInt("seat_"+roomId,-1); micOn=prefs.getBoolean("mic_"+roomId,false); roomLocked=prefs.getBoolean("locked_"+roomId,false);
        muteAll=prefs.getBoolean("mute_"+roomId,false); announcement=prefs.getString("notice_"+roomId,"Welcome to KING Plus • Be friendly and have fun"); renderParty();
    }

    private void renderParty() {
        clearListeners();
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(BG2);
        page = new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setPadding(dp(12),dp(14),dp(12),dp(20)); scroll.addView(page); setContentView(scroll);

        LinearLayout head = new LinearLayout(this); head.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout info = new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL); info.setPadding(dp(4),0,0,0);
        TextView rn = tv(roomName,19,Color.WHITE,true); info.addView(rn); TextView id = tv("ID: "+shortId()+"   •   "+(cloudRoom?"LIVE":"TEST"),11,MUTED,false); info.addView(id);
        head.addView(info,new LinearLayout.LayoutParams(0,dp(52),1));
        viewerLabel = pill("👥 1",0xff49315f,null); head.addView(viewerLabel,new LinearLayout.LayoutParams(dp(78),dp(42)));
        TextView more = pill("⋯",0xff49315f,this::roomMenu); LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(dp(48),dp(42));mp.setMargins(dp(5),0,0,0);head.addView(more,mp);
        TextView close = pill("✕",0xff49315f,this::leaveRoom); LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(dp(46),dp(42));cp.setMargins(dp(5),0,0,0);head.addView(close,cp); page.addView(head);
        addMemberStrip();

        announcementLabel = tv("📢  "+announcement,12,GOLD,true); announcementLabel.setBackground(bg(0xff342048,12)); announcementLabel.setPadding(dp(12),dp(9),dp(12),dp(9));
        LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,-2);ap.setMargins(0,dp(8),0,dp(8));page.addView(announcementLabel,ap);

        LinearLayout host = new LinearLayout(this); host.setOrientation(LinearLayout.VERTICAL); host.setGravity(Gravity.CENTER); host.setPadding(0,dp(4),0,dp(4));
        TextView crown = tv("🪽  👑  🪽",38,Color.WHITE,true); crown.setGravity(Gravity.CENTER); crown.setBackground(bg(0xff7b42d3,60)); host.addView(crown,new LinearLayout.LayoutParams(dp(150),dp(76)));
        TextView hn = tv("Host  •  "+ownerName,15,Color.WHITE,true); hn.setGravity(Gravity.CENTER); host.addView(hn);
        if (!isOwner()) { followLabel = pill("＋ Follow",PINK,this::toggleFollow); LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(dp(106),dp(36));fp.gravity=Gravity.CENTER_HORIZONTAL;host.addView(followLabel,fp); }
        page.addView(host,new LinearLayout.LayoutParams(-1,dp(135)));

        TextView seatsTitle = tv("Mic Seats",14,MUTED,true); page.addView(seatsTitle);
        seatsBox = new LinearLayout(this); seatsBox.setOrientation(LinearLayout.VERTICAL); page.addView(seatsBox); rebuildSeats();

        LinearLayout quick = new LinearLayout(this); quick.setGravity(Gravity.CENTER); quick.setPadding(0,dp(6),0,dp(6));
        addQuick(quick,"🎙\nVoice",this::openVoice); addQuick(quick,"🔊\nSound",this::toggleSound); addQuick(quick,"💬\nChat",() -> toast("Room chat is below")); addQuick(quick,"🎁\nGift",this::giftDialog); addQuick(quick,"⋯\nMore",this::roomMenu);
        page.addView(quick,new LinearLayout.LayoutParams(-1,dp(64)));

        TextView feedTitle=tv("Room Activity",13,MUTED,true);page.addView(feedTitle); feedBox=new LinearLayout(this);feedBox.setOrientation(LinearLayout.VERTICAL);page.addView(feedBox); seedLocalFeed();
        TextView chatTitle=tv("Room Chat",13,MUTED,true);page.addView(chatTitle); chatBox=new LinearLayout(this);chatBox.setOrientation(LinearLayout.VERTICAL);page.addView(chatBox); seedLocalChat();

        LinearLayout composer = new LinearLayout(this); composer.setGravity(Gravity.CENTER_VERTICAL); composer.setPadding(0,dp(8),0,0);
        EditText message = new EditText(this); message.setHint("Say something..."); message.setTextColor(Color.WHITE); message.setHintTextColor(0xffb9aec7); message.setSingleLine(true); message.setBackground(bg(CARD,20)); message.setPadding(dp(14),0,dp(14),0); composer.addView(message,new LinearLayout.LayoutParams(0,dp(50),1));
        TextView send = pill("Send",PURPLE,() -> sendMessage(message)); LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(dp(80),dp(48));sp.setMargins(dp(7),0,0,0);composer.addView(send,sp); page.addView(composer);

        micLabel = pill(micOn?"🎤 Mic ON":"🎤 Mic OFF",micOn?0xff26a269:0xff51366d,this::toggleMic);
        LinearLayout.LayoutParams ml=new LinearLayout.LayoutParams(-1,dp(48));ml.setMargins(0,dp(8),0,0);page.addView(micLabel,ml);

        if (cloudRoom) attachCloudRoom(); else {
            viewerLabel.setText("👥 "+(120+Math.abs(roomName.hashCode()%260)));
            fillLocalSeats();
        }
    }

    private void addMemberStrip() {
        LinearLayout strip = new LinearLayout(this); strip.setGravity(Gravity.CENTER_VERTICAL); strip.setPadding(0,dp(5),0,dp(5));
        String[] names = {ownerName == null ? "Host" : ownerName,"Neha","Riya","Amit","Pooja"};
        for (String n : names) {
            TextView av = tv(n.substring(0,1).toUpperCase(),12,Color.WHITE,true); av.setGravity(Gravity.CENTER); av.setBackground(bg(0xff5b3a78,40));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(34),dp(34)); p.setMargins(0,0,dp(5),0); strip.addView(av,p);
        }
        TextView label = tv("Members",11,MUTED,false); strip.addView(label,new LinearLayout.LayoutParams(0,dp(34),1));
        page.addView(strip,new LinearLayout.LayoutParams(-1,dp(46)));
    }

    private void addQuick(LinearLayout row,String text,Runnable action) {
        TextView v = pill(text,CARD,action); v.setTextSize(11); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(56),1);p.setMargins(dp(2),0,dp(2),0);row.addView(v,p);
    }
    private void rebuildSeats() {
        if (seatsBox == null) return; seatsBox.removeAllViews();
        for (int r=0;r<3;r++) {
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);
            for (int c=0;c<4;c++) {
                int no=r*4+c+1; LinearLayout seat=new LinearLayout(this);seat.setOrientation(LinearLayout.VERTICAL);seat.setGravity(Gravity.CENTER);
                String n=seatNames.get(no); boolean mine=cloudRoom?user!=null&&user.getUid().equals(seatUids.get(no)):no==mySeat;
                TextView av=tv(mine?"👑":(n==null?"＋":"●"),mine?27:24,Color.WHITE,true);av.setGravity(Gravity.CENTER);av.setBackground(bg(mine?PURPLE:0xff4a3768,50));seat.addView(av,new LinearLayout.LayoutParams(dp(54),dp(54)));
                String label=n==null?"Seat "+no:n; if(Boolean.FALSE.equals(seatMics.get(no))) label="🔇 "+label; TextView lab=tv(label,10,mine?Color.WHITE:MUTED,false);lab.setGravity(Gravity.CENTER);seat.addView(lab,new LinearLayout.LayoutParams(-1,dp(26)));
                final int seatNo=no;seat.setOnClickListener(v->seatAction(seatNo));row.addView(seat,new LinearLayout.LayoutParams(0,dp(84),1));
            }
            seatsBox.addView(row,new LinearLayout.LayoutParams(-1,dp(84)));
        }
    }
    private void fillLocalSeats() {
        seatNames.clear();seatUids.clear();seatMics.clear();
        String[] samples={"Neha","Riya","Anjali","Pooja","Simran","Kajal","Amit","Rohit"};
        for(int i=0;i<samples.length;i++){int no=i+1;if(no==mySeat)continue;seatNames.put(no,samples[i]);seatMics.put(no,i%3!=0);}
        if(mySeat>0){seatNames.put(mySeat,displayName);seatMics.put(mySeat,micOn);}rebuildSeats();
    }
    private void seatAction(int no) {
        if (roomLocked && !isOwner() && mySeat<0) { toast("Room seats are locked by host"); return; }
        if (cloudRoom) cloudSeatAction(no); else {
            if (no==mySeat) { mySeat=-1;micOn=false;prefs.edit().remove("seat_"+roomId).putBoolean("mic_"+roomId,false).apply(); }
            else if (seatNames.get(no)!=null) { toast("Seat is occupied"); return; }
            else { mySeat=no;prefs.edit().putInt("seat_"+roomId,no).apply(); }
            fillLocalSeats(); if(micLabel!=null)micLabel.setText(micOn?"🎤 Mic ON":"🎤 Mic OFF");
        }
    }
    private void cloudSeatAction(int no) {
        if(user==null||db==null)return; DocumentReference ref=db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(no));
        String existing=seatUids.get(no);
        if(existing!=null&&!existing.equals(user.getUid())){toast("Seat is occupied");return;}
        if(existing!=null){ref.delete().addOnSuccessListener(v->{mySeat=-1;micOn=false;addEvent("leave",displayName+" left mic seat");});return;}
        Map<String,Object>d=new HashMap<>();d.put("uid",user.getUid());d.put("name",safeName());d.put("micOn",false);d.put("joinedAt",FieldValue.serverTimestamp());
        ref.set(d).addOnSuccessListener(v->{mySeat=no;addEvent("seat",safeName()+" took seat "+no);}).addOnFailureListener(e->toast("Seat unavailable: "+msg(e)));
    }

    private void toggleMic() {
        if(mySeat<1){toast("Take a mic seat first");return;} if(muteAll&&!isOwner()){toast("Host muted all seats");return;} micOn=!micOn;
        if(cloudRoom&&user!=null&&db!=null) db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(mySeat)).update("micOn",micOn);
        else { prefs.edit().putBoolean("mic_"+roomId,micOn).apply();seatMics.put(mySeat,micOn);rebuildSeats(); }
        micLabel.setText(micOn?"🎤 Mic ON":"🎤 Mic OFF");micLabel.setBackground(bg(micOn?0xff26a269:0xff51366d,18));
    }
    private void toggleSound(){soundOn=!soundOn;toast(soundOn?"Room sound on":"Room sound muted");}
    private void openVoice(){Intent i=new Intent(this,VoiceWebActivity.class);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);startActivity(i);}

    private void sendMessage(EditText box) {
        String text=box.getText().toString().trim();if(text.isEmpty())return;if(text.length()>500){box.setError("Maximum 500 characters");return;}
        if(cloudRoom&&user!=null&&db!=null){Map<String,Object>d=new HashMap<>();d.put("senderUid",user.getUid());d.put("senderName",safeName());d.put("text",text);d.put("createdAt",FieldValue.serverTimestamp());
            db.collection("live_rooms").document(roomId).collection("messages").add(d).addOnSuccessListener(v->box.setText("")).addOnFailureListener(e->toast("Message failed: "+msg(e)));
        }else{appendLocalChat(displayName,text);addChatRow(displayName,text);box.setText("");}
    }
    private void seedLocalChat(){if(cloudRoom)return;addChatRow("Amit","Hi everyone 👋");addChatRow("Riya","Welcome to the party 💜");String saved=prefs.getString("chat_"+roomId,"");if(!saved.isEmpty())for(String row:saved.split("\\n")){String[]p=row.split("\\|",2);if(p.length==2)addChatRow(p[0],p[1]);}}
    private void appendLocalChat(String who,String text){String old=prefs.getString("chat_"+roomId,"");prefs.edit().putString("chat_"+roomId,old+who.replace("|","")+"|"+text.replace("\n"," ")+"\n").apply();}
    private void addChatRow(String who,String text){if(chatBox==null)return;TextView t=tv(who+":  "+text,14,Color.WHITE,false);t.setBackground(bg(0xff2e1c45,10));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(2),0,dp(2));chatBox.addView(t,p);}
    private void seedLocalFeed(){if(cloudRoom)return;addFeed("Amit joined the room");addFeed("Rohit sent Rose 🌹 x1");addFeed("Sanjay sent Crown 👑 x1");}
    private void addFeed(String text){if(feedBox==null)return;TextView t=tv("• "+text,12,0xffe7dcf2,false);feedBox.addView(t,new LinearLayout.LayoutParams(-1,-2));}

    private void giftDialog() {
        String[] gifts={"🌹 Rose • 10","❤️ Heart • 50","🍫 Chocolate • 100","🚗 Car • 500","👑 Crown • 1000","🏰 Castle • 5000","🎆 Firework • 10000"};
        int[] costs={10,50,100,500,1000,5000,10000};String[] names={"Rose","Heart","Chocolate","Car","Crown","Castle","Firework"};
        new AlertDialog.Builder(this).setTitle("🎁 Gift Store • "+localCoins+" coins").setItems(gifts,(d,w)->sendGift(names[w],costs[w])).setNegativeButton("Close",null).show();
    }
    private void sendGift(String gift,int cost) {
        if(cloudRoom&&user!=null&&ownerUid!=null&&!ownerUid.equals(user.getUid())){
            CloudBackend.sendGift(ownerUid,gift,cost,(ok,message)->runOnUiThread(()->{toast(message);if(ok)addEvent("gift",safeName()+" sent "+gift+" 🎁 x1");}));return;
        }
        if(localCoins<cost){toast("Not enough TEST coins");return;}localCoins-=cost;prefs.edit().putInt("coins",localCoins).apply();addFeed(displayName+" sent "+gift+" 🎁 x1");toast("Gift sent • TEST balance "+localCoins);
    }

    private void shareRoom(){Intent s=new Intent(Intent.ACTION_SEND);s.setType("text/plain");s.putExtra(Intent.EXTRA_TEXT,"Join my KING Plus Party: "+roomName+" • Room ID "+shortId());startActivity(Intent.createChooser(s,"Share Party"));}
    private void pkBattle(){String msg="⚔ PK Battle started: "+ownerName+" vs Guest Team\n\nGifts and room activity decide the winner in this test preview.";new AlertDialog.Builder(this).setTitle("PK Battle").setMessage(msg).setPositiveButton("Start",(d,w)->addEvent("pk",displayName+" started a PK battle ⚔")).setNegativeButton("Close",null).show();}

    private void roomMenu() {
        List<String> items=new ArrayList<>();items.add("🎙 Open voice room");items.add("⚔ PK battle");items.add("🔗 Share room");items.add("🔔 Invite by Firebase UID");items.add("⚑ Report host");items.add("🚫 Block host");if(isOwner()){items.add(roomLocked?"🔓 Unlock seats":"🔒 Lock seats");items.add(muteAll?"🎤 Unmute all seats":"🔇 Mute all seats");items.add("📢 Edit announcement");items.add("⛔ Close room");}
        String[] a=items.toArray(new String[0]);new AlertDialog.Builder(this).setTitle(roomName).setItems(a,(d,w)->{
            String x=a[w];if(x.contains("Open voice"))openVoice();else if(x.contains("PK battle"))pkBattle();else if(x.contains("Share"))shareRoom();else if(x.contains("Invite"))inviteDialog();else if(x.contains("Report"))reportHost();else if(x.contains("Block"))blockHost();else if(x.contains("Lock")||x.contains("Unlock"))setRoomFlag("locked",!roomLocked);else if(x.contains("Mute all")||x.contains("Unmute"))setRoomFlag("muteAll",!muteAll);else if(x.contains("announcement"))editAnnouncement();else if(x.contains("Close room"))closeRoom();
        }).show();
    }
    private void inviteDialog(){if(!cloudRoom||user==null){shareRoom();return;}final EditText e=new EditText(this);e.setHint("Friend Firebase UID");new AlertDialog.Builder(this).setTitle("Invite to Party").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Invite",(d,w)->{String uid=e.getText().toString().trim();if(uid.isEmpty())return;CloudBackend.sendRoomInvite(uid,roomId,roomName,(ok,m)->runOnUiThread(()->toast(m)));}).show();}
    private void reportHost(){String target=ownerUid==null?ownerName:ownerUid;CloudSync.submitReport(this,target,"Live party report",(ok,m)->runOnUiThread(()->toast(m)));}
    private void blockHost(){Set<String>b=new HashSet<>(prefs.getStringSet("blocked",new HashSet<>()));b.add(ownerUid==null?ownerName:ownerUid);prefs.edit().putStringSet("blocked",b).apply();toast("Host blocked locally");}
    private void editAnnouncement(){final EditText e=new EditText(this);e.setText(announcement);new AlertDialog.Builder(this).setTitle("Room announcement").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{String s=e.getText().toString().trim();if(s.isEmpty())return;if(cloudRoom)setRoomValue("announcement",s);else{announcement=s;prefs.edit().putString("notice_"+roomId,s).apply();announcementLabel.setText("📢  "+s);}}).show();}
    private void setRoomFlag(String key,boolean value){if(!isOwner()){toast("Host only");return;}if(cloudRoom)setRoomValue(key,value);else{if("locked".equals(key))roomLocked=value;else muteAll=value;prefs.edit().putBoolean(("locked".equals(key)?"locked_":"mute_")+roomId,value).apply();toast("Room updated");}}
    private void setRoomValue(String key,Object value){if(db==null)return;db.collection("live_rooms").document(roomId).update(key,value,"updatedAt",FieldValue.serverTimestamp()).addOnFailureListener(e->toast(msg(e)));}
    private void closeRoom(){if(!isOwner()){toast("Host only");return;}if(cloudRoom)setRoomValue("closed",true);leaveRoom();}

    private void toggleFollow(){if(user==null||ownerUid==null){toast("Firebase sign-in required to follow");return;}String id=user.getUid()+"_"+ownerUid;DocumentReference ref=db.collection("follows").document(id);ref.get().addOnSuccessListener(doc->{if(doc.exists()){ref.delete();followLabel.setText("＋ Follow");}else{Map<String,Object>d=new HashMap<>();d.put("followerUid",user.getUid());d.put("targetUid",ownerUid);d.put("followerName",safeName());d.put("createdAt",FieldValue.serverTimestamp());ref.set(d);followLabel.setText("✓ Following");}});}

    private void attachCloudRoom() {
        if(db==null||user==null)return;DocumentReference room=db.collection("live_rooms").document(roomId);
        roomListener=room.addSnapshotListener((doc,e)->{if(e!=null||doc==null||!doc.exists())return;announcement=str(doc,"announcement",announcement);roomLocked=Boolean.TRUE.equals(doc.getBoolean("locked"));muteAll=Boolean.TRUE.equals(doc.getBoolean("muteAll"));if(announcementLabel!=null)announcementLabel.setText((roomLocked?"🔒  ":"📢  ")+announcement);if(Boolean.TRUE.equals(doc.getBoolean("closed"))&&!isOwner()){toast("Room was closed by host");leaveRoom();}});
        seatsListener=room.collection("seats").addSnapshotListener((snap,e)->{if(e!=null||snap==null)return;seatNames.clear();seatUids.clear();seatMics.clear();mySeat=-1;for(DocumentSnapshot d:snap.getDocuments()){int no;try{no=Integer.parseInt(d.getId());}catch(Exception ex){continue;}seatNames.put(no,str(d,"name","Guest"));seatUids.put(no,d.getString("uid"));seatMics.put(no,!Boolean.FALSE.equals(d.getBoolean("micOn")));if(user.getUid().equals(d.getString("uid"))){mySeat=no;micOn=Boolean.TRUE.equals(d.getBoolean("micOn"));}}rebuildSeats();if(micLabel!=null){micLabel.setText(micOn?"🎤 Mic ON":"🎤 Mic OFF");micLabel.setBackground(bg(micOn?0xff26a269:0xff51366d,18));}});
        membersListener=room.collection("members").addSnapshotListener((snap,e)->{if(viewerLabel!=null&&snap!=null)viewerLabel.setText("👥 "+snap.size());});
        messagesListener=room.collection("messages").orderBy("createdAt",Query.Direction.ASCENDING).limit(80).addSnapshotListener((snap,e)->{if(e!=null||snap==null||chatBox==null)return;chatBox.removeAllViews();for(DocumentSnapshot d:snap.getDocuments())addChatRow(str(d,"senderName","User"),str(d,"text",""));});
        eventsListener=room.collection("events").orderBy("createdAt",Query.Direction.ASCENDING).limit(50).addSnapshotListener((snap,e)->{if(e!=null||snap==null||feedBox==null)return;feedBox.removeAllViews();for(DocumentSnapshot d:snap.getDocuments())addFeed(str(d,"text","Room activity"));});
    }
    private void registerMember(){if(user==null||db==null||roomId==null)return;Map<String,Object>d=new HashMap<>();d.put("uid",user.getUid());d.put("name",safeName());d.put("joinedAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).collection("members").document(user.getUid()).set(d);addEvent("join",safeName()+" joined the room");}
    private void unregisterMember(){if(user!=null&&db!=null&&cloudRoom&&roomId!=null)db.collection("live_rooms").document(roomId).collection("members").document(user.getUid()).delete();}
    private void addEvent(String type,String text){if(cloudRoom&&user!=null&&db!=null&&roomId!=null){Map<String,Object>d=new HashMap<>();d.put("actorUid",user.getUid());d.put("actorName",safeName());d.put("type",type);d.put("text",text);d.put("createdAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).collection("events").add(d);}else addFeed(text);}

    private boolean isOwner(){return cloudRoom?user!=null&&ownerUid!=null&&ownerUid.equals(user.getUid()):displayName.equals(ownerName);}
    private String safeName(){if(user!=null){String n=user.getDisplayName();if(n!=null&&!n.trim().isEmpty())return n.trim();String p=user.getPhoneNumber();if(p!=null&&p.length()>=4)return "KING "+p.substring(p.length()-4);}return displayName==null||displayName.trim().isEmpty()?"KING User":displayName;}
    private String shortId(){if(roomId==null)return "000000";return String.valueOf(Math.abs(roomId.hashCode()%900000)+100000);}
    private String str(DocumentSnapshot d,String key,String fallback){String v=d.getString(key);return v==null||v.trim().isEmpty()?fallback:v;}
    private String msg(Exception e){return e==null||e.getLocalizedMessage()==null?"unknown error":e.getLocalizedMessage();}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}

    private void leaveRoom(){unregisterMember();clearListeners();renderLobby("Hot");}
    private void addBottomNav(LinearLayout root,int selected){LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER);nav.setBackgroundColor(Color.WHITE);String[] ni={"⌂\nParty","♟\nGame","◇\nDiscover","✉\nMessages","●\nMe"};for(int i=0;i<ni.length;i++){TextView n=tv(ni[i],12,i==selected?PURPLE:0xff777777,true);n.setGravity(Gravity.CENTER);final int k=i;n.setOnClickListener(v->{if(k==0)renderLobby("Hot");else{Intent back=new Intent(this,MainActivity.class);back.putExtra("openTab",k);startActivity(back);finish();}});nav.addView(n,new LinearLayout.LayoutParams(0,dp(62),1));}root.addView(nav);}

    @Override public void onBackPressed(){if(roomId!=null)leaveRoom();else super.onBackPressed();}
    @Override protected void onDestroy(){unregisterMember();clearListeners();super.onDestroy();}
}
