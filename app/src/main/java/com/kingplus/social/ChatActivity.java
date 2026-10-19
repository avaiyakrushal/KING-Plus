package com.kingplus.social;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ChatActivity extends Activity {
    private static final int PURPLE = 0xff7c4dff;
    private static final int DARK = 0xff181528;
    private static final int PHOTO_REQUEST = 410;
    private static final int MIC_PERMISSION = 411;

    private String peerName;
    private String peerUid;
    private String myName;
    private String chatId;
    private FirebaseUser me;
    private FirebaseFirestore db;
    private ListenerRegistration messagesListener;
    private ListenerRegistration threadListener,peerProfileLive10068;
    private boolean chatVisible10068,historyLoading10068,cloudListenFailed10068,latestConfirmed10068;
    private int liveEpoch10068,historyEpoch10068,historyPage10068;
    private final List<DocumentSnapshot> latest10068=new ArrayList<>(),visible10068=new ArrayList<>();
    private final List<DocumentSnapshot> pageAnchors10068=new ArrayList<>();
    private TextView older10068,newer10068,latestButton10068;
    private boolean liveCurrent10068(int epoch){return chatVisible10068&&epoch==liveEpoch10068&&chatSessionCurrent10040();}
    private Query messageQuery10068(){return db.collection("direct_threads").document(chatId).collection("messages")
        .orderBy("createdAt",Query.Direction.DESCENDING).limit(100);}
    private void stopCloud10068(){
        ++liveEpoch10068;++historyEpoch10068;historyLoading10068=false;
        if(messagesListener!=null){messagesListener.remove();messagesListener=null;}
        if(threadListener!=null){threadListener.remove();threadListener=null;}
        if(peerProfileLive10068!=null){peerProfileLive10068.remove();peerProfileLive10068=null;}
    }
    private void restartCloud10068(){if(!chatVisible10068||!chatSessionCurrent10040())return;
        stopCloud10068();loadPeerHeader940();listenCloud();}
    private LinearLayout messages;
    private ScrollView scroll;
    private EditText composer;
    private TextView status;
    private TextView peerNameView940;
    private TextView peerAvatarFallback940;
    private android.widget.ImageView peerAvatarImage940;
    private boolean cloudMode;
    private android.net.ConnectivityManager.NetworkCallback chatNetworkCallback940;
    private boolean chatLastOnline940;
    private Timestamp peerReadAt;
    private MediaRecorder recorder;
    private File voiceFile;
    private boolean recording;
    private String replyName = "";
    private String replyPreview = "";
    private TextView replyBar;
    private int testCoins = 2500;
    private final String[] giftNames620 = {"Rose","Love Heart","Crown","Firework","Lucky Fish","Couple Ring","Magic Star","Royal Castle","Super Car","Lionheart","Parcel Box","Party Star"};
    private final String[] giftIcons620 = {"🌹","💗","👑","🎆","🐠","💍","🌟","🏰","🏎️","🦁","📦","⭐"};
    private final int[] giftCosts620 = {50,100,500,800,1200,1800,2200,5000,8800,12800,999,1680};

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        peerName = clean(getIntent().getStringExtra("peerName"), "KING Friend");
        peerUid = clean(getIntent().getStringExtra("peerUid"), "");
        myName = mainPrefs().getString("name", "KING User");
        testCoins = mainPrefs().getInt("coins", 2500);
        try { me = FirebaseAuth.getInstance().getCurrentUser(); } catch (Exception ignored) { me = null; }
        try { db = FirebaseFirestore.getInstance(); } catch (Exception ignored) { db = null; }
        cloudMode = me != null && db != null && !peerUid.isEmpty() && !peerUid.equals(me.getUid());
        chatId = cloudMode ? threadId(me.getUid(), peerUid) : "local_" + safeKey(peerName);
        if(cloudMode)rememberRecent(peerName);
        render();
        if (!cloudMode) loadLocal();
        chatLastOnline940=KingNetwork.online(this);
        chatNetworkCallback940=KingNetwork.watch(this,online->runOnUiThread(()->{
            boolean recovered=online&&!chatLastOnline940;chatLastOnline940=online;
            if(recovered&&cloudMode&&!isFinishing()&&!isDestroyed()){status.setText("Back online • syncing pending messages…");flushPendingText940();}
        }));
        if(cloudMode&&chatLastOnline940)flushPendingText940();
    }

    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
    private GradientDrawable bg(int color, int radius) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d; }
    private TextView label(String s, int size, int color, boolean bold) { TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); if (bold) t.setTypeface(null, Typeface.BOLD); return t; }

    private void render() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(0xfff7f7fb);

        LinearLayout head = new LinearLayout(this); head.setGravity(Gravity.CENTER_VERTICAL); head.setPadding(dp(10), dp(10), dp(8), dp(8)); head.setBackgroundColor(Color.WHITE);
        TextView back = label("‹", 38, DARK, false); back.setGravity(Gravity.CENTER); back.setOnClickListener(v -> finish()); head.addView(back, new LinearLayout.LayoutParams(dp(46), dp(56)));
        android.widget.FrameLayout avatarBox940=new android.widget.FrameLayout(this);peerAvatarFallback940=label(peerName==null||peerName.trim().isEmpty()?"K":peerName.trim().substring(0,1).toUpperCase(Locale.US),20,Color.WHITE,true);peerAvatarFallback940.setGravity(Gravity.CENTER);peerAvatarFallback940.setBackground(bg(PURPLE,28));avatarBox940.addView(peerAvatarFallback940,new android.widget.FrameLayout.LayoutParams(-1,-1));peerAvatarImage940=new android.widget.ImageView(this);peerAvatarImage940.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);avatarBox940.addView(peerAvatarImage940,new android.widget.FrameLayout.LayoutParams(-1,-1));avatarBox940.setOnClickListener(v->openPeerProfile940());head.addView(avatarBox940,new LinearLayout.LayoutParams(dp(50),dp(50)));
        LinearLayout info = new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL); info.setPadding(dp(10), 0, 0, 0);info.setOnClickListener(v->openPeerProfile940());
        peerNameView940 = label(peerName, 17, DARK, true); info.addView(peerNameView940); status = label(cloudMode ? "Connecting…" : "Local / test chat", 12, 0xff777186, false); info.addView(status);
        head.addView(info, new LinearLayout.LayoutParams(0, dp(54), 1));
        TextView more = label("⋮", 30, DARK, true); more.setGravity(Gravity.CENTER); more.setOnClickListener(v -> safetyMenu()); head.addView(more, new LinearLayout.LayoutParams(dp(48), dp(54)));
        root.addView(head);
        if(cloudMode){
            View history=getLayoutInflater().inflate(R.layout.king_chat_history_10068,root,false);
            older10068=history.findViewById(R.id.chat_older_10068);newer10068=history.findViewById(R.id.chat_newer_10068);
            latestButton10068=history.findViewById(R.id.chat_latest_10068);
            older10068.setOnClickListener(v->olderPage10068());newer10068.setOnClickListener(v->newerPage10068());
            latestButton10068.setOnClickListener(v->showLatest10068());root.addView(history);
            status.setOnClickListener(v->restartCloud10068());
        }

        scroll = new ScrollView(this); scroll.setFillViewport(true); messages = new LinearLayout(this); messages.setOrientation(LinearLayout.VERTICAL); messages.setPadding(dp(12), dp(12), dp(12), dp(18)); scroll.addView(messages);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        scroll.setOnScrollChangeListener((view,x,y,oldX,oldY)->{
            if(cloudMode)acknowledgeVisible10068(messages.getHeight()-scroll.getHeight()-y<dp(24));
        });

        LinearLayout emoji = new LinearLayout(this); emoji.setPadding(dp(10), dp(4), dp(10), dp(4)); String[] es = {"❤️","😂","👍","🎁","🔥","👑"};
        for (String e : es) { TextView x = label(e, 22, DARK, false); x.setGravity(Gravity.CENTER); x.setOnClickListener(v -> sendText(((TextView)v).getText().toString())); emoji.addView(x, new LinearLayout.LayoutParams(0, dp(42), 1)); }
        root.addView(emoji);

        LinearLayout tools = new LinearLayout(this); tools.setPadding(dp(8), dp(2), dp(8), dp(5));
        addTool(tools, "🎁 Gift", this::directGiftPanel620); addTool(tools, "😊 Emoji", this::directEmojiPanel620); addTool(tools, "📷 Photo", this::pickPhoto); addTool(tools, "🎤 Voice", this::toggleVoice); addTool(tools, "🔗 Room", this::shareRoom);
        root.addView(tools, new LinearLayout.LayoutParams(-1, dp(45)));

        replyBar = label("", 12, 0xff655e72, true); replyBar.setPadding(dp(14),dp(7),dp(14),dp(7)); replyBar.setBackgroundColor(0xffeeeaf7); replyBar.setVisibility(View.GONE); replyBar.setOnClickListener(v -> clearReply620()); root.addView(replyBar,new LinearLayout.LayoutParams(-1,dp(38)));

        LinearLayout compose = new LinearLayout(this); compose.setGravity(Gravity.CENTER_VERTICAL); compose.setPadding(dp(10), dp(6), dp(10), dp(10)); compose.setBackgroundColor(Color.WHITE);
        composer = new EditText(this); composer.setHint("Message…"); composer.setSingleLine(false); composer.setMaxLines(4); composer.setTextColor(DARK); composer.setHintTextColor(0xff9a95a4); composer.setPadding(dp(14), dp(4), dp(14), dp(4)); composer.setBackground(bg(0xfff0eff4, 22));
        compose.addView(composer, new LinearLayout.LayoutParams(0, dp(50), 1));
        TextView send = label("➤", 26, Color.WHITE, true); send.setGravity(Gravity.CENTER); send.setBackground(bg(PURPLE, 25)); LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(dp(50), dp(50)); sp.setMargins(dp(8),0,0,0); compose.addView(send, sp); send.setOnClickListener(v -> { String text = composer.getText().toString().trim(); if (!text.isEmpty()) sendText(text); });
        root.addView(compose);
        setContentView(root);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(root,(view,insets)->{
            androidx.core.graphics.Insets bars=insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            androidx.core.graphics.Insets ime=insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left,bars.top,bars.right,Math.max(bars.bottom,ime.bottom));
            boolean keyboard=insets.isVisible(androidx.core.view.WindowInsetsCompat.Type.ime());
            emoji.setVisibility(keyboard?View.GONE:View.VISIBLE);tools.setVisibility(keyboard?View.GONE:View.VISIBLE);
            return insets;
        });androidx.core.view.ViewCompat.requestApplyInsets(root);
    }

    private void openPeerProfile940(){
        if(peerUid==null||peerUid.trim().isEmpty())return;
        Intent i=new Intent(this,KingPublicProfileActivity.class);i.putExtra("uid",peerUid);i.putExtra("name",peerName);startActivity(i);
    }
    private void loadPeerHeader940(){
        if(!cloudMode||db==null||!chatVisible10068)return;
        if(peerProfileLive10068!=null)peerProfileLive10068.remove();
        final int epoch=liveEpoch10068;
        peerProfileLive10068=db.collection("public_profiles").document(peerUid)
            .addSnapshotListener((profile,error)->{
                if(!liveCurrent10068(epoch)||error!=null||profile==null||!profile.exists())return;
                peerName=clean(profile.getString("displayName"),"KING Friend");
                peerNameView940.setText(peerName);
                peerAvatarFallback940.setText(peerName.substring(0,1).toUpperCase(Locale.US));
                KingProfileImages10068.load(this,peerAvatarImage940,peerAvatarFallback940,profile.getString("photoUrl"));
            });
    }

    private void addTool(LinearLayout row, String text, Runnable action) {
        TextView x = label(text, 12, 0xff5c5668, true); x.setGravity(Gravity.CENTER); x.setBackground(bg(0xffeeecf3, 14)); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(38), 1); p.setMargins(dp(3),0,dp(3),0); row.addView(x,p); x.setOnClickListener(v->KingSafe.run(this,"ui-action",action));
    }

    private void listenCloud() {
        if(!chatVisible10068||!chatSessionCurrent10040())return;
        final int epoch=liveEpoch10068;cloudListenFailed10068=false;
        status.setText("Connecting to chat…");
        threadListener=db.collection("direct_threads").document(chatId).addSnapshotListener((doc,error)->{
            if(!liveCurrent10068(epoch)||doc==null||!doc.exists())return;
            Object reads=doc.get("readAt");Timestamp previous=peerReadAt;
            if(reads instanceof Map){Object read=((Map<?,?>)reads).get(peerUid);if(read instanceof Timestamp)peerReadAt=(Timestamp)read;}
            if(peerReadAt!=null&&!peerReadAt.equals(previous)&&!visible10068.isEmpty())renderHistory10068(false);
        });
        messagesListener=messageQuery10068().addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(snap,error)->{
            if(!liveCurrent10068(epoch))return;
            if(error!=null||snap==null){cloudListenFailed10068=true;status.setText("Chat unavailable • tap here to reconnect");return;}
            boolean bottom=messages.getHeight()-scroll.getHeight()-scroll.getScrollY()<dp(96);
            latest10068.clear();latest10068.addAll(snap.getDocuments());
            if(historyPage10068==0){visible10068.clear();visible10068.addAll(latest10068);renderHistory10068(bottom);}
            status.setText(snap.getMetadata().isFromCache()?"Cached messages • connecting…":historyPage10068>0?"History • tap Latest for live messages":"Realtime chat");
            latestConfirmed10068=!snap.getMetadata().isFromCache()&&!snap.getMetadata().hasPendingWrites();
            acknowledgeVisible10068(bottom);
        });
    }
    private void acknowledgeVisible10068(boolean bottom){
        if(!KingChatRead10068.canAcknowledge(chatVisible10068,latestConfirmed10068,historyPage10068,bottom)
            ||!chatSessionCurrent10040())return;
        for(DocumentSnapshot incoming:latest10068){
            Timestamp time=incoming.getTimestamp("createdAt");String sender=incoming.getString("senderUid");
            if(time!=null&&sender!=null&&!me.getUid().equals(sender)&&!incoming.getMetadata().hasPendingWrites()){
                long millis=time.toDate().getTime();if(millis>lastReadConfirmed10047)markRead(millis);break;
            }
        }
    }
    private void renderHistory10068(boolean bottom){
        int position=scroll.getScrollY();messages.removeAllViews();
        List<DocumentSnapshot> chronological=new ArrayList<>(visible10068);java.util.Collections.reverse(chronological);
        if(chronological.isEmpty()){TextView empty=label("No messages yet. Say hello 👋",14,0xff777186,false);messages.addView(empty);}
        else for(DocumentSnapshot message:chronological)renderCloudMessage(message);
        if(older10068!=null){older10068.setEnabled(!historyLoading10068&&visible10068.size()==100);
            newer10068.setEnabled(!historyLoading10068&&historyPage10068>0);
            latestButton10068.setText(historyLoading10068?"Loading…":historyPage10068==0?"Latest":"Latest • live");}
        if(bottom)scrollDown();else scroll.post(()->scroll.scrollTo(0,position));
    }
    private void showLatest10068(){
        ++historyEpoch10068;historyLoading10068=false;historyPage10068=0;pageAnchors10068.clear();
        visible10068.clear();visible10068.addAll(latest10068);renderHistory10068(true);acknowledgeVisible10068(true);
    }
    private void olderPage10068(){
        if(historyLoading10068||visible10068.size()<100||!chatVisible10068||!chatSessionCurrent10040())return;
        DocumentSnapshot anchor=visible10068.get(visible10068.size()-1);
        loadHistory10068(anchor,historyPage10068+1);
    }
    private void newerPage10068(){
        if(historyLoading10068||historyPage10068<1)return;
        if(historyPage10068==1){showLatest10068();return;}
        loadHistory10068(pageAnchors10068.get(historyPage10068-2),historyPage10068-1);
    }
    private void loadHistory10068(DocumentSnapshot anchor,int targetPage){
        if(!chatVisible10068||!chatSessionCurrent10040())return;
        historyLoading10068=true;renderHistory10068(false);
        final int request=++historyEpoch10068,epoch=liveEpoch10068;
        messageQuery10068().startAfter(anchor).get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(page->{
            if(!liveCurrent10068(epoch)||request!=historyEpoch10068)return;
            historyLoading10068=false;
            if(page.isEmpty()){renderHistory10068(false);Toast.makeText(this,"No older messages",Toast.LENGTH_SHORT).show();return;}
            while(pageAnchors10068.size()>=targetPage)pageAnchors10068.remove(pageAnchors10068.size()-1);
            pageAnchors10068.add(anchor);historyPage10068=targetPage;
            visible10068.clear();visible10068.addAll(page.getDocuments());renderHistory10068(true);
            status.setText("History page "+targetPage+" • Latest returns to live chat");
        }).addOnFailureListener(error->{
            if(!liveCurrent10068(epoch)||request!=historyEpoch10068)return;
            historyLoading10068=false;renderHistory10068(false);status.setText("History unavailable • tap Older or Newer to retry");
        });
    }

    private void renderCloudMessage(DocumentSnapshot doc) {
        String sender = clean(doc.getString("senderUid"), ""); boolean mine = me != null && me.getUid().equals(sender);
        String text = clean(doc.getString("text"), ""); String type = clean(doc.getString("type"), "text"); String media = clean(doc.getString("mediaUrl"), ""); Timestamp ts = doc.getTimestamp("createdAt");
        if ("gift".equals(type)) {
            String icon=clean(doc.getString("giftIcon"),"🎁"), name=clean(doc.getString("giftName"),"Gift"); Long q=doc.getLong("giftQty"), value=doc.getLong("giftValue");
            text=icon+" "+name+"  ×"+(q==null?1:q)+"  •  💎"+(value==null?0:value);
        }
        addBubble(mine, text, type, media, ts == null ? new Date() : ts.toDate(), mine && ts != null && peerReadAt != null && peerReadAt.compareTo(ts) >= 0);
    }

    private boolean cloudTextSending10042;
    private void sendText(String text) {
        if(cloudTextSending10042){Toast.makeText(this,"Sending previous message…",Toast.LENGTH_SHORT).show();return;}
        if(!cloudMode && peerUid!=null && !peerUid.isEmpty()){
            Toast.makeText(this,"Sign in again to send a real message",Toast.LENGTH_LONG).show();return;
        }
        if (isBlocked()) { Toast.makeText(this,"Unblock this user before messaging",Toast.LENGTH_SHORT).show(); return; }
        if (text.length() > 1000) { Toast.makeText(this,"Message is too long",Toast.LENGTH_SHORT).show(); return; }
        if (!replyPreview.isEmpty()) text="↪ "+replyName+": "+shortReply620(replyPreview)+"\n"+text;
        if (cloudMode){cloudTextSending10042=true;sendCloudMessage(text, "text", "");} else saveLocal(text, "text", "", true);
        clearReply620();
        composer.setText("");
    }

    private void sendCloudMessage(String text, String type, String mediaUrl) {
        Map<String,Object> thread = new HashMap<>(); List<String> members = new ArrayList<>(); members.add(me.getUid()); members.add(peerUid); thread.put("members", members);
        Map<String,Object> names = new HashMap<>(); names.put(me.getUid(), myName); names.put(peerUid, peerName); thread.put("memberNames", names);
        thread.put("lastMessage", type.equals("text") ? text : type.equals("photo") ? "📷 Photo" : "🎤 Voice note"); thread.put("lastSenderUid", me.getUid()); thread.put("updatedAt", FieldValue.serverTimestamp());
        if(!chatSessionCurrent10040()){
            cloudTextSending10042=false;Toast.makeText(this,"Account changed • reopen chat",Toast.LENGTH_LONG).show();return;
        }
        final String sendOwner10040=me.getUid();
        final String sendId10040=java.util.UUID.randomUUID().toString().replace("-","");
        Map<String,Object> msg = new HashMap<>();
        msg.put("senderUid",sendOwner10040);msg.put("recipientUid",peerUid);
        msg.put("senderName",myName);msg.put("text",text);msg.put("type",type);
        msg.put("clientId",sendId10040);
        if(!mediaUrl.isEmpty())msg.put("mediaUrl",mediaUrl);
        msg.put("createdAt",FieldValue.serverTimestamp());
        com.google.firebase.firestore.DocumentReference threadRef10040=db.collection("direct_threads").document(chatId);
        // Firestore rules check an existing parent thread for each message. First
        // create a non-visible skeleton; publish the inbox summary only AFTER
        // the message itself was confirmed on the server.
        Map<String,Object> skeleton10040=new HashMap<>();
        skeleton10040.put("members",members);skeleton10040.put("memberNames",names);
        threadRef10040.set(skeleton10040,SetOptions.merge()).addOnSuccessListener(v->{
            if(!chatSessionCurrent10040())return;
            if(cloudListenFailed10068)restartCloud10068();
            threadRef10040.collection("messages").document("m_"+sendId10040).set(msg)
                .addOnSuccessListener(sent->{
                    cloudTextSending10042=false;
                    if(!chatSessionCurrent10040())return;
                    threadRef10040.set(thread,SetOptions.merge())
                        .addOnSuccessListener(done->{if(chatSessionCurrent10040())status.setText("Realtime chat");})
                        .addOnFailureListener(err->{if(chatSessionCurrent10040())status.setText("Sent • thread preview will refresh");});
                }).addOnFailureListener(err->handleSendFailure10040(sendId10040,text,type,mediaUrl));
        }).addOnFailureListener(err->handleSendFailure10040(sendId10040,text,type,mediaUrl));
    }
    private void handleSendFailure10040(String id,String text,String type,String mediaUrl){
        if(!chatSessionCurrent10040()){cloudTextSending10042=false;return;}
        // A timed-out write may actually have succeeded. Check the exact message
        // before queuing the same client ID, avoiding an offline duplicate.
        db.collection("direct_threads").document(chatId).collection("messages")
            .document("m_"+id).get(com.google.firebase.firestore.Source.SERVER)
            .addOnCompleteListener(result->{
                cloudTextSending10042=false;
                if(!chatSessionCurrent10040())return;
                if(result.isSuccessful()&&result.getResult()!=null&&result.getResult().exists()){
                    status.setText("Sent • syncing conversation");return;
                }
                status.setText("Not sent • saved locally for retry");
                if("text".equals(type))queuePendingText940(id,text);
                saveLocal(text,type,mediaUrl,true);
            });
    }

    private long lastReadConfirmed10047=-1L;
    private long readReceiptTarget10048=-1L;
    private boolean readReceiptPending10048;
    private void markRead(long latestIncomingMillis10048) {
        if (!chatSessionCurrent10040()||!chatVisible10068||db==null)return;
        readReceiptTarget10048=Math.max(readReceiptTarget10048,latestIncomingMillis10048);
        if(readReceiptPending10048||readReceiptTarget10048<=lastReadConfirmed10047)return;
        final long confirming10048=readReceiptTarget10048;
        final String owner10048=me.getUid();
        readReceiptPending10048=true;
        Map<String,Object> patch=new HashMap<>();
        patch.put("readAt."+owner10048,FieldValue.serverTimestamp());
        db.collection("direct_threads").document(chatId).update(patch)
            .addOnSuccessListener(done10048->{
                readReceiptPending10048=false;
                if(!chatSessionCurrent10040()||!owner10048.equals(me.getUid()))return;
                lastReadConfirmed10047=Math.max(lastReadConfirmed10047,confirming10048);
                if(readReceiptTarget10048>lastReadConfirmed10047)markRead(readReceiptTarget10048);
            }).addOnFailureListener(error10048->{
                readReceiptPending10048=false;
                if(chatSessionCurrent10040())status.setText("Chat live • read receipt will retry");
            });
    }

    private void pickPhoto() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT); i.setType("image/*"); i.addCategory(Intent.CATEGORY_OPENABLE); startActivityForResult(i, PHOTO_REQUEST);
    }

    private void toggleVoice() {
        if (recording) { stopVoice(true); return; }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) { requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MIC_PERMISSION); return; }
        startVoice();
    }

    private void startVoice() {
        try {
            voiceFile = new File(getCacheDir(), "voice_" + System.currentTimeMillis() + ".m4a");
            recorder = new MediaRecorder(); recorder.setAudioSource(MediaRecorder.AudioSource.MIC); recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC); recorder.setAudioEncodingBitRate(64000); recorder.setAudioSamplingRate(44100); recorder.setOutputFile(voiceFile.getAbsolutePath()); recorder.prepare(); recorder.start(); recording = true; status.setText("Recording voice… tap Voice again to send");
        } catch (Exception e) { recording = false; Toast.makeText(this,"Voice recorder failed: "+safe(e.getLocalizedMessage()),Toast.LENGTH_SHORT).show(); }
    }

    private void stopVoice(boolean send) {
        if (!recording) return; recording = false;
        try { recorder.stop(); } catch (Exception ignored) { send = false; }
        try { recorder.release(); } catch (Exception ignored) {} recorder = null; status.setText(cloudMode ? "Realtime chat" : "Local / test chat");
        if (send && voiceFile != null && voiceFile.exists()) sendMedia(Uri.fromFile(voiceFile), "voice", "audio/mp4");
    }

    private void sendMedia(Uri uri, String type, String mime) {
        if (uri == null) return;
        if (!cloudMode) { saveLocal(type.equals("photo") ? "📷 Photo" : "🎤 Voice note", type, uri.toString(), true); return; }
        String ext = type.equals("photo") ? ".jpg" : ".m4a";
        StorageReference ref = FirebaseStorage.getInstance().getReference().child("chat_media/" + me.getUid() + "/" + chatId + "/" + System.currentTimeMillis() + ext);
        status.setText("Uploading " + type + "…");
        ref.putFile(uri).continueWithTask(task -> { if (!task.isSuccessful() && task.getException()!=null) throw task.getException(); return ref.getDownloadUrl(); })
            .addOnSuccessListener(url -> { status.setText("Realtime chat"); sendCloudMessage(type.equals("photo") ? "📷 Photo" : "🎤 Voice note", type, url.toString()); })
            .addOnFailureListener(e -> { status.setText("Offline • media saved locally"); saveLocal(type.equals("photo") ? "📷 Photo" : "🎤 Voice note", type, uri.toString(), true); });
    }

    private void shareRoom() {
        if(!cloudMode||me==null){Toast.makeText(this,
            "Sign in to share a real Firebase Party",Toast.LENGTH_LONG).show();return;}
        final String room992=mainPrefs().getString("last_live_room_id_992","");
        final String account992=mainPrefs().getString("last_live_room_account_992","");
        final String name992=mainPrefs().getString("last_live_room_name_992","KING Party");
        if(!account992.equals(me.getUid())||KingRoomInvite959.parse(room992).isEmpty()){
            new AlertDialog.Builder(this).setTitle("No recent live Party")
              .setMessage("Open and join a live KING Plus Party Room first. Then return here and share its real Room ID.")
              .setPositiveButton("Open Party",(d,w)->startActivity(new Intent(this,PartyActivity.class)))
              .setNegativeButton("Cancel",null).show();
            return;
        }
        sendText("🎉 Join me in KING Plus: "+name992+
             "\nKINGROOM:"+room992+"\n"+KingRoomInvite959.link(room992));
    }

    private String pendingKey940(){return "pending_cloud_"+safeKey(chatId==null?peerName:chatId);}
    private void queuePendingText940(String id,String text){
        if(text==null||text.trim().isEmpty())return;
        try{
            SharedPreferences p=getSharedPreferences("chat_store",MODE_PRIVATE);JSONArray a=new JSONArray(p.getString(pendingKey940(),"[]"));
            JSONObject o=new JSONObject();o.put("id",id);o.put("text",text);o.put("ts",System.currentTimeMillis());a.put(o);
            while(a.length()>100){JSONArray n=new JSONArray();for(int i=1;i<a.length();i++)n.put(a.get(i));a=n;}
            p.edit().putString(pendingKey940(),a.toString()).apply();
        }catch(Exception ignored){}
    }
    private void flushPendingText940(){
        if(!chatSessionCurrent10040()||!KingNetwork.online(this))return;
        try{
            SharedPreferences p=getSharedPreferences("chat_store",MODE_PRIVATE);JSONArray a=new JSONArray(p.getString(pendingKey940(),"[]"));if(a.length()==0){if(status!=null)status.setText("Realtime chat");return;}
            JSONObject o=a.getJSONObject(0);String id=o.optString("id",""),text=o.optString("text","");if(id.isEmpty()||text.isEmpty()){removePendingHead940(a,p);flushPendingText940();return;}
            Map<String,Object> thread=new HashMap<>();List<String> members=new ArrayList<>();members.add(me.getUid());members.add(peerUid);thread.put("members",members);Map<String,Object> names=new HashMap<>();names.put(me.getUid(),myName);names.put(peerUid,peerName);thread.put("memberNames",names);thread.put("lastMessage",text);thread.put("lastSenderUid",me.getUid());thread.put("updatedAt",FieldValue.serverTimestamp());
            if(!chatSessionCurrent10040())return;
            Map<String,Object> msg=new HashMap<>();msg.put("senderUid",me.getUid());msg.put("recipientUid",peerUid);msg.put("senderName",myName);msg.put("text",text);msg.put("type","text");msg.put("clientId",id);msg.put("createdAt",FieldValue.serverTimestamp());
            com.google.firebase.firestore.DocumentReference ref10040=db.collection("direct_threads").document(chatId);
            Map<String,Object> skeleton10040=new HashMap<>();
            skeleton10040.put("members",members);skeleton10040.put("memberNames",names);
            ref10040.set(skeleton10040,SetOptions.merge()).addOnSuccessListener(v->{
                if(!chatSessionCurrent10040())return;
                ref10040.collection("messages").document("m_"+id).set(msg)
                    .addOnSuccessListener(sent->{
                        if(!chatSessionCurrent10040())return;
                        ref10040.set(thread,SetOptions.merge());
                        removePendingHead940(a,p);flushPendingText940();
                    }).addOnFailureListener(e->{
                        if(!chatSessionCurrent10040())return;
                        ref10040.collection("messages").document("m_"+id)
                            .get(com.google.firebase.firestore.Source.SERVER)
                            .addOnSuccessListener(found->{
                                if(!chatSessionCurrent10040())return;
                                if(found.exists()){removePendingHead940(a,p);flushPendingText940();}
                                else if(status!=null)status.setText("Pending message will retry");
                            }).addOnFailureListener(f->{if(status!=null)status.setText("Pending message will retry");});
                    });
            }).addOnFailureListener(x->{if(chatSessionCurrent10040()&&status!=null)status.setText("Pending message will retry");});
        }catch(Exception ignored){}
    }
    private void removePendingHead940(JSONArray a,SharedPreferences p){
        try{JSONArray n=new JSONArray();for(int i=1;i<a.length();i++)n.put(a.get(i));p.edit().putString(pendingKey940(),n.toString()).apply();}catch(Exception ignored){}
    }

    private String localThreadKey10040(){
        return "thread_"+safeKey((me==null?"guest":me.getUid())+"_"+
            (peerUid==null||peerUid.isEmpty()?peerName:peerUid));
    }
    private boolean chatSessionCurrent10040(){
        FirebaseUser now=FirebaseAuth.getInstance().getCurrentUser();
        return !isFinishing()&&!isDestroyed()&&cloudMode&&me!=null&&now!=null
            &&KingSixPages10040.sameAccount(me.getUid(),now.getUid())
            &&KingSixPages10040.safePeer(now.getUid(),peerUid);
    }
    @Override protected void onStart(){
        super.onStart();chatVisible10068=true;
        if(cloudMode&&chatSessionCurrent10040())restartCloud10068();
    }
    @Override protected void onStop(){
        chatVisible10068=false;stopCloud10068();stopVoice(false);super.onStop();
    }
    @Override protected void onResume(){
        super.onResume();
        FirebaseUser now=FirebaseAuth.getInstance().getCurrentUser();
        if(me!=null && (now==null||!KingSixPages10040.sameAccount(me.getUid(),now.getUid()))){
            if(messagesListener!=null){messagesListener.remove();messagesListener=null;}
            if(threadListener!=null){threadListener.remove();threadListener=null;}
            Toast.makeText(this,"Account changed • reopen Messages",Toast.LENGTH_SHORT).show();finish();
        }
    }
    private void saveLocal(String text, String type, String media, boolean mine) {
        try {
            SharedPreferences p = getSharedPreferences("chat_store", MODE_PRIVATE); String key = localThreadKey10040(); JSONArray a = new JSONArray(p.getString(key, "[]")); JSONObject o = new JSONObject(); o.put("mine", mine); o.put("text", text); o.put("type", type); o.put("media", media); o.put("ts", System.currentTimeMillis()); a.put(o); while (a.length() > 200) { JSONArray n = new JSONArray(); for (int i=1;i<a.length();i++) n.put(a.get(i)); a=n; } p.edit().putString(key, a.toString()).apply(); loadLocal();
        } catch (Exception e) { Toast.makeText(this,"Could not save message",Toast.LENGTH_SHORT).show(); }
    }

    private void loadLocal() {
        messages.removeAllViews();
        try {
            SharedPreferences p = getSharedPreferences("chat_store", MODE_PRIVATE); JSONArray a = new JSONArray(p.getString(localThreadKey10040(), "[]"));
            if (a.length() == 0) addBubble(false, "Hello 👋", "text", "", new Date(), false);
            for (int i=0;i<a.length();i++) { JSONObject o=a.getJSONObject(i); addBubble(o.optBoolean("mine"), o.optString("text"), o.optString("type","text"), o.optString("media"), new Date(o.optLong("ts",System.currentTimeMillis())), true); }
        } catch (Exception ignored) { }
        scrollDown();
    }

    private void addBubble(boolean mine, String text, String type, String media, Date when, boolean seen) {
        LinearLayout wrap = new LinearLayout(this); wrap.setGravity(mine ? Gravity.END : Gravity.START); wrap.setPadding(0, dp(3), 0, dp(3));
        String ownBubble940=KingCosmetics.bubble(this);int ownBubbleColor940=KingCosmetics.bubbleColor(ownBubble940);
        LinearLayout bubble = new LinearLayout(this); bubble.setOrientation(LinearLayout.VERTICAL); bubble.setPadding(dp(12), dp(8), dp(12), dp(7)); bubble.setBackground(bg("gift".equals(type)?0xfffff2c7:(mine ? ownBubbleColor940 : Color.WHITE), 18));
        if("gift".equals(type)){ TextView giftTitle=label("🎁  GIFT",11,0xff8c6515,true); bubble.addView(giftTitle); TextView body=label(text,18,0xff3d2d12,true); body.setPadding(0,dp(5),0,dp(3)); bubble.addView(body); TextView sub=label("Sent in KING Plus chat • TEST/no billing",10,0xff8a7756,false); bubble.addView(sub); }
        else { TextView body = label(text, 15, mine ? Color.WHITE : DARK, false); bubble.addView(body); }
        final String fullRoomId992=cloudMode?KingRoomInvite959.parse(text):"";
        if(!fullRoomId992.isEmpty()){
            TextView joinParty992=label("🎉 Join this Party Room  →",14,
                mine?0xffffe4ac:0xff7131b5,true);
            joinParty992.setPadding(dp(6),dp(12),dp(6),dp(10));
            joinParty992.setOnClickListener(v->{
                if(me==null){Toast.makeText(this,"Sign in first",Toast.LENGTH_SHORT).show();return;}
                Intent intent992=new Intent(this,PartyActivity.class);
                intent992.putExtra("directRoomId",fullRoomId992);
                intent992.putExtra("displayName",myName);
                startActivity(intent992);
            });
            bubble.addView(joinParty992);
        }
        if (!media.isEmpty()) { TextView open = label(type.equals("photo") ? "Tap to view photo" : "Tap to play voice", 12, mine ? 0xffeee7ff : PURPLE, true); open.setPadding(0,dp(5),0,0); open.setOnClickListener(v -> openMedia(media, type)); bubble.addView(open); }
        String meta = DateFormat.getTimeInstance(DateFormat.SHORT).format(when) + (mine ? (seen ? "  ✓✓" : "  ✓") : ""); TextView time = label(meta, 10, "gift".equals(type)?0xff8a7756:(mine ? KingCosmetics.bubbleMetaColor(ownBubble940) : 0xff96909f), false); time.setGravity(Gravity.END); bubble.addView(time);
        bubble.setOnLongClickListener(v->{messageActions620(text);return true;});
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-2, -2); bp.width = (int)(getResources().getDisplayMetrics().widthPixels * .76f); wrap.addView(bubble,bp); messages.addView(wrap,new LinearLayout.LayoutParams(-1,-2));
    }

    private void openMedia(String value, String type) {
        try {
            Uri uri = Uri.parse(value);
            if (type.equals("voice") && !value.startsWith("http://") && !value.startsWith("https://")) {
                MediaPlayer player = MediaPlayer.create(this, uri);
                if (player == null) throw new IllegalStateException("Audio unavailable");
                player.setOnCompletionListener(MediaPlayer::release);
                player.start();
                return;
            }
            Intent i = new Intent(Intent.ACTION_VIEW, uri);
            i.setDataAndType(uri, type.equals("photo") ? "image/*" : "audio/*");
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(i);
        } catch (Exception e) { Toast.makeText(this,"No app can open this media",Toast.LENGTH_SHORT).show(); }
    }

    private void directEmojiPanel620() {
        final String[][] packs={{"😀","😃","😄","😁","😂","🤣","😊","😍","🥰","😘","😎","🤩","🥳","😇","🤗","🤭","😴","😭","😡","🤯"},{"❤️","💗","💖","💕","💞","💓","🔥","✨","⭐","🌟","🎉","🎊","👑","💎","🌹","🎁","💯","👏","🙌","🙏"},{"👍","👎","👌","✌️","🤞","🤟","🤘","💪","👋","🤝","🫶","🙋","💃","🕺","🎤","🎵","⚔️","🏆","🚀","🦁"}};
        final String[] names={"Faces","Expressions","Party"};
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(12),dp(10),dp(12),dp(12));root.setBackgroundColor(Color.WHITE);
        TextView title=label("Emoji & Stickers",18,DARK,true);root.addView(title,new LinearLayout.LayoutParams(-1,dp(42)));
        LinearLayout tabs=new LinearLayout(this);LinearLayout grid=new LinearLayout(this);grid.setOrientation(LinearLayout.VERTICAL);
        final Runnable[] rebuild=new Runnable[1]; final int[] selected={0};
        rebuild[0]=()->{grid.removeAllViews();String[] data=packs[selected[0]];for(int i=0;i<data.length;i+=5){LinearLayout row=new LinearLayout(this);for(int c=0;c<5;c++){if(i+c>=data.length){row.addView(new View(this),new LinearLayout.LayoutParams(0,dp(56),1));continue;}String e=data[i+c];TextView v=label(e,27,DARK,false);v.setGravity(Gravity.CENTER);v.setOnClickListener(x->{sendText(e);});row.addView(v,new LinearLayout.LayoutParams(0,dp(56),1));}grid.addView(row,new LinearLayout.LayoutParams(-1,dp(58)));}};
        for(int i=0;i<names.length;i++){final int idx=i;TextView t=label(names[i],12,i==0?PURPLE:0xff777186,true);t.setGravity(Gravity.CENTER);t.setOnClickListener(v->{selected[0]=idx;rebuild[0].run();});tabs.addView(t,new LinearLayout.LayoutParams(0,dp(40),1));}
        root.addView(tabs);ScrollView sv=new ScrollView(this);sv.addView(grid);root.addView(sv,new LinearLayout.LayoutParams(-1,dp(245)));rebuild[0].run();
        new AlertDialog.Builder(this).setView(root).setNegativeButton("Close",null).show();
    }

    private void directGiftPanel620() {
        final int[] selected={0}; final int[] qty={1};
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(12),dp(10),dp(12),dp(12));root.setBackgroundColor(0xff181824);
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);TextView title=label("Gift • "+peerName,18,Color.WHITE,true);head.addView(title,new LinearLayout.LayoutParams(0,dp(44),1));TextView bal=label("FREE • No billing",13,0xffffdf6b,true);bal.setGravity(Gravity.CENTER);head.addView(bal,new LinearLayout.LayoutParams(dp(100),dp(40)));root.addView(head);
        TextView selection=label(giftIcons620[0]+" "+giftNames620[0]+" ×1",13,0xffffdf6b,true);root.addView(selection,new LinearLayout.LayoutParams(-1,dp(34)));
        LinearLayout grid=new LinearLayout(this);grid.setOrientation(LinearLayout.VERTICAL);for(int i=0;i<giftNames620.length;i+=4){LinearLayout row=new LinearLayout(this);for(int c=0;c<4;c++){if(i+c>=giftNames620.length){row.addView(new View(this),new LinearLayout.LayoutParams(0,dp(88),1));continue;}final int idx=i+c;LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setBackground(bg(0xff292735,12));TextView ic=label(giftIcons620[idx],28,Color.WHITE,false);ic.setGravity(Gravity.CENTER);card.addView(ic,new LinearLayout.LayoutParams(-1,dp(44)));TextView nm=label(giftNames620[idx],10,Color.WHITE,true);nm.setGravity(Gravity.CENTER);card.addView(nm,new LinearLayout.LayoutParams(-1,dp(20)));TextView co=label("FREE",9,0xffaaa4b7,false);co.setGravity(Gravity.CENTER);card.addView(co,new LinearLayout.LayoutParams(-1,dp(18)));card.setOnClickListener(v->{selected[0]=idx;selection.setText(giftIcons620[idx]+" "+giftNames620[idx]+" ×"+qty[0]);});LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(86),1);cp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(card,cp);}grid.addView(row,new LinearLayout.LayoutParams(-1,dp(92)));}root.addView(grid);
        LinearLayout amounts=new LinearLayout(this);int[] qs={1,3,9};for(int q:qs){TextView b=label("×"+q,12,Color.WHITE,true);b.setGravity(Gravity.CENTER);b.setBackground(bg(0xff343143,12));b.setOnClickListener(v->{qty[0]=q;selection.setText(giftIcons620[selected[0]]+" "+giftNames620[selected[0]]+" ×"+q);});LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(0,dp(42),1);ap.setMargins(dp(3),dp(2),dp(3),0);amounts.addView(b,ap);}root.addView(amounts);
        AlertDialog dialog=new AlertDialog.Builder(this).setView(root).setNegativeButton("Close",null).setPositiveButton("Send",null).create();dialog.setOnShowListener(v->{dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(x->{int total=giftCosts620[selected[0]]*qty[0];if(sendDirectGift620(giftNames620[selected[0]],giftIcons620[selected[0]],giftCosts620[selected[0]],qty[0],total)){bal.setText("FREE • No billing");dialog.dismiss();}});});dialog.show();
    }

    private boolean directGiftPending972;

    private boolean sendDirectGift620(String name,String icon,int unit,int qty,int total){
        if(isBlocked()){
            Toast.makeText(this,"Unblock this person first",Toast.LENGTH_SHORT).show();
            return false;
        }
        final String sender973=me==null?null:me.getUid();
        if(qty<1||qty>KingFreeGift973.MAX_QTY){
            Toast.makeText(this,"FREE Gift quantity must be 1–9",Toast.LENGTH_SHORT).show();
            return false;
        }
        final String giftText973=icon+" "+name+" ×"+qty+" • FREE decorative gift (no coins)";
        if(!cloudMode){
            // Local cosmetic preview doesn't claim any Cloud delivery or wallet activity.
            saveLocal(giftText973,"text","",true);
            Toast.makeText(this,"Local FREE Gift preview",Toast.LENGTH_SHORT).show();
            return true;
        }
        if(directGiftPending972){Toast.makeText(this,"Gift delivery pending…",Toast.LENGTH_SHORT).show();return false;}
        if(!KingFreeGift973.validDirect(sender973,peerUid,qty)||db==null||
           chatId==null||chatId.isEmpty()||isFinishing()||isDestroyed()){
            Toast.makeText(this,"Sign in to send a FREE Gift",Toast.LENGTH_SHORT).show();
            return false;
        }
        directGiftPending972=true;
        final String target973=peerUid;
        final String thread973=chatId;
        Map<String,Object> thread973Data=new HashMap<>();
        java.util.ArrayList<String> members973=new java.util.ArrayList<>();
        members973.add(sender973);members973.add(target973);
        thread973Data.put("members",members973);
        Map<String,Object> memberNames973=new HashMap<>();
        memberNames973.put(sender973,myName==null?"KING User":myName);
        memberNames973.put(target973,peerName==null?"KING User":peerName);
        thread973Data.put("memberNames",memberNames973);
        thread973Data.put("lastMessage",giftText973);
        thread973Data.put("lastSenderUid",sender973);
        thread973Data.put("updatedAt",FieldValue.serverTimestamp());
        final com.google.firebase.firestore.DocumentReference doc973=
            db.collection("direct_threads").document(thread973);
        Map<String,Object> threadSkeleton10044=new HashMap<>();
        threadSkeleton10044.put("members",members973);
        threadSkeleton10044.put("memberNames",memberNames973);
        final String giftId10044="fg_"+java.util.UUID.randomUUID().toString().replace("-","");
        doc973.set(threadSkeleton10044,SetOptions.merge()).addOnSuccessListener(v->{
            if(!chatSessionCurrent10040())return;
            if(cloudListenFailed10068)restartCloud10068();
            Map<String,Object> gift973=new HashMap<>();
            gift973.put("senderUid",sender973);
            gift973.put("recipientUid",target973);
            gift973.put("senderName",myName==null?"KING User":myName);
            gift973.put("text",giftText973);
            gift973.put("type","text");
            gift973.put("freeGift",true);
            gift973.put("giftValue",0);
            gift973.put("createdAt",FieldValue.serverTimestamp());
            gift973.put("clientId",giftId10044);
            doc973.collection("messages").document(giftId10044).set(gift973)
                .addOnSuccessListener(saved973->{
                    directGiftPending972=false;
                    if(!chatSessionCurrent10040())return;
                    doc973.set(thread973Data,SetOptions.merge());
                    Toast.makeText(this,"FREE Gift delivered • no coins used",Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(err973->{
                    if(!chatSessionCurrent10040()){directGiftPending972=false;return;}
                    doc973.collection("messages").document(giftId10044)
                        .get(com.google.firebase.firestore.Source.SERVER)
                        .addOnSuccessListener(found->{
                            if(!chatSessionCurrent10040())return;
                            directGiftPending972=false;
                            if(found.exists()){
                                doc973.set(thread973Data,SetOptions.merge());
                                Toast.makeText(this,"FREE Gift confirmed on server",Toast.LENGTH_SHORT).show();
                            }else Toast.makeText(this,"FREE Gift not delivered • retry",Toast.LENGTH_LONG).show();
                        }).addOnFailureListener(checkError->{
                            directGiftPending972=false;
                            if(chatSessionCurrent10040())Toast.makeText(this,
                                "Gift delivery uncertain • check Messages first",Toast.LENGTH_LONG).show();});
                });
        }).addOnFailureListener(err973->{
            directGiftPending972=false;
            if(!isFinishing()&&!isDestroyed())
                Toast.makeText(this,"FREE Gift was NOT sent; Chat unavailable",
                    Toast.LENGTH_LONG).show();
        });
        return true;
    }

    private void syncDirectProgress700(LevelSystem.Snapshot p){
        // XP updates must never reset paid VIP identity, server-issued KING ID,
        // or an edited name/photo stored by another device.
        if(p==null||!chatSessionCurrent10040())return;
        final String owner=me.getUid();
        Map<String,Object> patch=KingLiveGuard10046.progressPatch(p.level,p.xp,LevelSystem.levelTier(p.level));
        patch.put("updatedAt",FieldValue.serverTimestamp());
        db.collection("public_profiles").document(owner).set(patch,SetOptions.merge());
    }
    private String kingPublicId700(String uid){if(uid==null||uid.isEmpty())return "000000";return String.valueOf(Integer.toUnsignedLong(uid.hashCode())%900000L+100000L);}

    private void directGiftHistory620(){directGiftHistoryPage10068(null);}
    private void directGiftHistoryPage10068(DocumentSnapshot cursor){
        if(!cloudMode){new AlertDialog.Builder(this).setTitle("Gift history").setMessage("Local gift cards are shown in this conversation.").setPositiveButton("OK",null).show();return;}
        if(!chatVisible10068||!chatSessionCurrent10040())return;
        final int epoch=liveEpoch10068;
        Query query=messageQuery10068();if(cursor!=null)query=query.startAfter(cursor);
        query.get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(snap->{
            if(!liveCurrent10068(epoch))return;
            List<String> rows=new ArrayList<>();
            for(DocumentSnapshot d:snap.getDocuments())
                if("gift".equals(d.getString("type"))||Boolean.TRUE.equals(d.getBoolean("freeGift")))
                    rows.add(clean(d.getString("text"),"🎁 Gift"));
            if(rows.isEmpty())rows.add("No gifts on this message page");
            AlertDialog.Builder dialog=new AlertDialog.Builder(this).setTitle("Gift history • 100-message page")
                .setItems(rows.toArray(new String[0]),null).setNegativeButton("Close",null);
            if(snap.size()==100){DocumentSnapshot next=snap.getDocuments().get(snap.size()-1);
                dialog.setPositiveButton("Older",(d,w)->directGiftHistoryPage10068(next));}
            if(cursor!=null)dialog.setNeutralButton("Latest",(d,w)->directGiftHistory620());
            dialog.show();
        }).addOnFailureListener(error->{if(liveCurrent10068(epoch))Toast.makeText(this,"Gift history unavailable • reconnect",Toast.LENGTH_SHORT).show();});
    }

    private void messageActions620(String text) {
        String[] a={"↩ Reply","🎁 Send gift","😊 Send emoji","🔗 Share room"};new AlertDialog.Builder(this).setTitle(peerName).setItems(a,(d,w)->{if(w==0){replyName=peerName;replyPreview=text;replyBar.setText("↩ Replying to "+peerName+" • "+shortReply620(text)+"   ✕");replyBar.setVisibility(View.VISIBLE);composer.requestFocus();}else if(w==1)directGiftPanel620();else if(w==2)directEmojiPanel620();else shareRoom();}).show();
    }
    private void clearReply620(){replyName="";replyPreview="";if(replyBar!=null)replyBar.setVisibility(View.GONE);}
    private String shortReply620(String s){if(s==null)return "";String x=s.replace('\n',' ').trim();return x.length()>42?x.substring(0,42)+"…":x;}

    private void safetyMenu() {
        boolean blocked = isBlocked(); String[] opts = {"🎁 Send gift","🎁 Gift history","😊 Emoji & stickers", blocked ? "✅ Unblock user" : "🚫 Block user", "⚑ Report user", "🗑 Clear local chat" };
        new AlertDialog.Builder(this).setTitle(peerName).setItems(opts,(d,which)->{ if(which==0) directGiftPanel620(); else if(which==1) directGiftHistory620(); else if(which==2) directEmojiPanel620(); else if(which==3) toggleBlock(); else if(which==4) report(); else clearLocal(); }).show();
    }

    private boolean isBlocked() {
        Set<String> b = mainPrefs().getStringSet("blocked", new HashSet<>()); if (b.contains(peerName)) return true;
        Set<String> ids = mainPrefs().getStringSet("blocked_uids", new HashSet<>()); return !peerUid.isEmpty() && ids.contains(peerUid);
    }

    private void toggleBlock() {
        Set<String> b = new HashSet<>(mainPrefs().getStringSet("blocked",new HashSet<>())); Set<String> ids = new HashSet<>(mainPrefs().getStringSet("blocked_uids",new HashSet<>()));
        boolean block = !isBlocked(); if (block) { b.add(peerName); if(!peerUid.isEmpty()) ids.add(peerUid); } else { b.remove(peerName); ids.remove(peerUid); }
        mainPrefs().edit().putStringSet("blocked",b).putStringSet("blocked_uids",ids).apply(); Toast.makeText(this,block?"User blocked":"User unblocked",Toast.LENGTH_SHORT).show();
    }

    private void report() {
        final EditText reason = new EditText(this); reason.setHint("Reason"); new AlertDialog.Builder(this).setTitle("Report " + peerName).setView(reason).setNegativeButton("Cancel",null).setPositiveButton("Submit",(d,w)->{
            String r=reason.getText().toString().trim(); if(r.isEmpty()) r="Chat safety report"; CloudSync.submitReport(this, peerUid.isEmpty()?peerName:peerUid, r, (ok,m)->runOnUiThread(()->Toast.makeText(this,m,Toast.LENGTH_LONG).show()));
        }).show();
    }

    private void clearLocal() { getSharedPreferences("chat_store",MODE_PRIVATE).edit().remove(localThreadKey10040()).apply(); if(!cloudMode) loadLocal(); Toast.makeText(this,"Local chat cleared",Toast.LENGTH_SHORT).show(); }
    private SharedPreferences mainPrefs() { return getSharedPreferences("MainActivity", MODE_PRIVATE); }
    private void rememberRecent(String name) { SharedPreferences p=getSharedPreferences("chat_store",MODE_PRIVATE); Set<String>s=new HashSet<>(p.getStringSet("recent_names",new HashSet<>())); s.add(name); p.edit().putStringSet("recent_names",s).apply(); }
    private void scrollDown() { if (scroll != null) scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN)); }
    private String threadId(String a, String b) { return a.compareTo(b) < 0 ? a + "_" + b : b + "_" + a; }
    private String safeKey(String s) { return s.replaceAll("[^A-Za-z0-9_-]", "_").toLowerCase(Locale.US); }
    private String clean(String s, String fallback) { return s == null || s.trim().isEmpty() ? fallback : s.trim(); }
    private String safe(String s) { return s == null || s.trim().isEmpty() ? "unknown error" : s; }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data); if(requestCode==PHOTO_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()!=null){ Uri uri=data.getData(); try{getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){} sendMedia(uri,"photo","image/*"); }
    }
    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) { super.onRequestPermissionsResult(requestCode,permissions,results); if(requestCode==MIC_PERMISSION && results.length>0 && results[0]==PackageManager.PERMISSION_GRANTED) startVoice(); }
    @Override protected void onDestroy() { KingNetwork.unwatch(this,chatNetworkCallback940);chatNetworkCallback940=null;stopCloud10068();stopVoice(false);super.onDestroy(); }
}
