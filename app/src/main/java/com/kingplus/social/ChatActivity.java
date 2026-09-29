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
    private ListenerRegistration threadListener;
    private LinearLayout messages;
    private ScrollView scroll;
    private EditText composer;
    private TextView status;
    private boolean cloudMode;
    private Timestamp peerReadAt;
    private MediaRecorder recorder;
    private File voiceFile;
    private boolean recording;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        peerName = clean(getIntent().getStringExtra("peerName"), "KING Friend");
        peerUid = clean(getIntent().getStringExtra("peerUid"), "");
        myName = mainPrefs().getString("name", "KING User");
        try { me = FirebaseAuth.getInstance().getCurrentUser(); } catch (Exception ignored) { me = null; }
        try { db = FirebaseFirestore.getInstance(); } catch (Exception ignored) { db = null; }
        cloudMode = me != null && db != null && !peerUid.isEmpty() && !peerUid.equals(me.getUid());
        chatId = cloudMode ? threadId(me.getUid(), peerUid) : "local_" + safeKey(peerName);
        rememberRecent(peerName);
        render();
        if (cloudMode) listenCloud(); else loadLocal();
    }

    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
    private GradientDrawable bg(int color, int radius) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d; }
    private TextView label(String s, int size, int color, boolean bold) { TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); if (bold) t.setTypeface(null, Typeface.BOLD); return t; }

    private void render() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(0xfff7f7fb);

        LinearLayout head = new LinearLayout(this); head.setGravity(Gravity.CENTER_VERTICAL); head.setPadding(dp(10), dp(10), dp(8), dp(8)); head.setBackgroundColor(Color.WHITE);
        TextView back = label("‹", 38, DARK, false); back.setGravity(Gravity.CENTER); back.setOnClickListener(v -> finish()); head.addView(back, new LinearLayout.LayoutParams(dp(46), dp(56)));
        TextView avatar = label("●", 26, PURPLE, true); avatar.setGravity(Gravity.CENTER); avatar.setBackground(bg(0xffefe8ff, 28)); head.addView(avatar, new LinearLayout.LayoutParams(dp(50), dp(50)));
        LinearLayout info = new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL); info.setPadding(dp(10), 0, 0, 0);
        TextView n = label(peerName, 17, DARK, true); info.addView(n); status = label(cloudMode ? "Connecting…" : "Local / test chat", 12, 0xff777186, false); info.addView(status);
        head.addView(info, new LinearLayout.LayoutParams(0, dp(54), 1));
        TextView more = label("⋮", 30, DARK, true); more.setGravity(Gravity.CENTER); more.setOnClickListener(v -> safetyMenu()); head.addView(more, new LinearLayout.LayoutParams(dp(48), dp(54)));
        root.addView(head);

        scroll = new ScrollView(this); scroll.setFillViewport(true); messages = new LinearLayout(this); messages.setOrientation(LinearLayout.VERTICAL); messages.setPadding(dp(12), dp(12), dp(12), dp(18)); scroll.addView(messages);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout emoji = new LinearLayout(this); emoji.setPadding(dp(10), dp(4), dp(10), dp(4)); String[] es = {"❤️","😂","👍","🎁","🔥","👑"};
        for (String e : es) { TextView x = label(e, 22, DARK, false); x.setGravity(Gravity.CENTER); x.setOnClickListener(v -> sendText(((TextView)v).getText().toString())); emoji.addView(x, new LinearLayout.LayoutParams(0, dp(42), 1)); }
        root.addView(emoji);

        LinearLayout tools = new LinearLayout(this); tools.setPadding(dp(10), dp(2), dp(10), dp(5));
        addTool(tools, "📷 Photo", this::pickPhoto); addTool(tools, "🎤 Voice", this::toggleVoice); addTool(tools, "🔗 Room", this::shareRoom);
        root.addView(tools, new LinearLayout.LayoutParams(-1, dp(45)));

        LinearLayout compose = new LinearLayout(this); compose.setGravity(Gravity.CENTER_VERTICAL); compose.setPadding(dp(10), dp(6), dp(10), dp(10)); compose.setBackgroundColor(Color.WHITE);
        composer = new EditText(this); composer.setHint("Message…"); composer.setSingleLine(false); composer.setMaxLines(4); composer.setTextColor(DARK); composer.setHintTextColor(0xff9a95a4); composer.setPadding(dp(14), dp(4), dp(14), dp(4)); composer.setBackground(bg(0xfff0eff4, 22));
        compose.addView(composer, new LinearLayout.LayoutParams(0, dp(50), 1));
        TextView send = label("➤", 26, Color.WHITE, true); send.setGravity(Gravity.CENTER); send.setBackground(bg(PURPLE, 25)); LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(dp(50), dp(50)); sp.setMargins(dp(8),0,0,0); compose.addView(send, sp); send.setOnClickListener(v -> { String text = composer.getText().toString().trim(); if (!text.isEmpty()) sendText(text); });
        root.addView(compose);
        setContentView(root);
    }

    private void addTool(LinearLayout row, String text, Runnable action) {
        TextView x = label(text, 12, 0xff5c5668, true); x.setGravity(Gravity.CENTER); x.setBackground(bg(0xffeeecf3, 14)); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(38), 1); p.setMargins(dp(3),0,dp(3),0); row.addView(x,p); x.setOnClickListener(v -> action.run());
    }

    private void listenCloud() {
        status.setText("Realtime • online when connected");
        threadListener = db.collection("direct_threads").document(chatId).addSnapshotListener((doc, err) -> {
            if (doc == null || !doc.exists()) return;
            Object reads = doc.get("readAt");
            if (reads instanceof Map) { Object r = ((Map<?,?>) reads).get(peerUid); if (r instanceof Timestamp) peerReadAt = (Timestamp) r; }
        });
        messagesListener = db.collection("direct_threads").document(chatId).collection("messages")
            .orderBy("createdAt", Query.Direction.DESCENDING).limit(100)
            .addSnapshotListener((snap, error) -> {
                if (error != null) { status.setText("Cloud unavailable • local fallback available"); Toast.makeText(this,"Realtime chat error: "+safe(error.getLocalizedMessage()),Toast.LENGTH_SHORT).show(); return; }
                if (snap == null) return;
                List<DocumentSnapshot> docs = new ArrayList<>(snap.getDocuments()); java.util.Collections.reverse(docs);
                messages.removeAllViews();
                for (DocumentSnapshot doc : docs) renderCloudMessage(doc);
                markRead(); scrollDown();
            });
    }

    private void renderCloudMessage(DocumentSnapshot doc) {
        String sender = clean(doc.getString("senderUid"), ""); boolean mine = me != null && me.getUid().equals(sender);
        String text = clean(doc.getString("text"), ""); String type = clean(doc.getString("type"), "text"); String media = clean(doc.getString("mediaUrl"), ""); Timestamp ts = doc.getTimestamp("createdAt");
        addBubble(mine, text, type, media, ts == null ? new Date() : ts.toDate(), mine && ts != null && peerReadAt != null && peerReadAt.compareTo(ts) >= 0);
    }

    private void sendText(String text) {
        if (isBlocked()) { Toast.makeText(this,"Unblock this user before messaging",Toast.LENGTH_SHORT).show(); return; }
        if (text.length() > 1000) { Toast.makeText(this,"Message is too long",Toast.LENGTH_SHORT).show(); return; }
        if (cloudMode) sendCloudMessage(text, "text", ""); else saveLocal(text, "text", "", true);
        composer.setText("");
    }

    private void sendCloudMessage(String text, String type, String mediaUrl) {
        Map<String,Object> thread = new HashMap<>(); List<String> members = new ArrayList<>(); members.add(me.getUid()); members.add(peerUid); thread.put("members", members);
        Map<String,Object> names = new HashMap<>(); names.put(me.getUid(), myName); names.put(peerUid, peerName); thread.put("memberNames", names);
        thread.put("lastMessage", type.equals("text") ? text : type.equals("photo") ? "📷 Photo" : "🎤 Voice note"); thread.put("lastSenderUid", me.getUid()); thread.put("updatedAt", FieldValue.serverTimestamp());
        db.collection("direct_threads").document(chatId).set(thread, SetOptions.merge())
            .addOnSuccessListener(v -> {
                Map<String,Object> msg = new HashMap<>(); msg.put("senderUid", me.getUid()); msg.put("recipientUid", peerUid); msg.put("senderName", myName); msg.put("text", text); msg.put("type", type); if (!mediaUrl.isEmpty()) msg.put("mediaUrl", mediaUrl); msg.put("createdAt", FieldValue.serverTimestamp());
                db.collection("direct_threads").document(chatId).collection("messages").add(msg)
                    .addOnSuccessListener(r -> CloudBackend.sendDirectMessageNotification(peerUid, myName, text, (ok,m)->{}))
                    .addOnFailureListener(e -> Toast.makeText(this,"Message failed: "+safe(e.getLocalizedMessage()),Toast.LENGTH_SHORT).show());
            }).addOnFailureListener(e -> Toast.makeText(this,"Chat could not start: "+safe(e.getLocalizedMessage()),Toast.LENGTH_SHORT).show());
    }

    private void markRead() {
        if (!cloudMode) return;
        Map<String,Object> patch = new HashMap<>(); patch.put("readAt." + me.getUid(), FieldValue.serverTimestamp());
        db.collection("direct_threads").document(chatId).update(patch);
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
            .addOnFailureListener(e -> { status.setText("Upload failed"); Toast.makeText(this,"Upload failed: "+safe(e.getLocalizedMessage()),Toast.LENGTH_SHORT).show(); });
    }

    private void shareRoom() {
        String room = mainPrefs().getString("last_room", "KING Lounge"); sendText("🔗 Join me in KING Plus room: " + room);
    }

    private void saveLocal(String text, String type, String media, boolean mine) {
        try {
            SharedPreferences p = getSharedPreferences("chat_store", MODE_PRIVATE); String key = "thread_" + safeKey(peerName); JSONArray a = new JSONArray(p.getString(key, "[]")); JSONObject o = new JSONObject(); o.put("mine", mine); o.put("text", text); o.put("type", type); o.put("media", media); o.put("ts", System.currentTimeMillis()); a.put(o); while (a.length() > 200) { JSONArray n = new JSONArray(); for (int i=1;i<a.length();i++) n.put(a.get(i)); a=n; } p.edit().putString(key, a.toString()).apply(); loadLocal();
        } catch (Exception e) { Toast.makeText(this,"Could not save message",Toast.LENGTH_SHORT).show(); }
    }

    private void loadLocal() {
        messages.removeAllViews();
        try {
            SharedPreferences p = getSharedPreferences("chat_store", MODE_PRIVATE); JSONArray a = new JSONArray(p.getString("thread_" + safeKey(peerName), "[]"));
            if (a.length() == 0) addBubble(false, "Hello 👋", "text", "", new Date(), false);
            for (int i=0;i<a.length();i++) { JSONObject o=a.getJSONObject(i); addBubble(o.optBoolean("mine"), o.optString("text"), o.optString("type","text"), o.optString("media"), new Date(o.optLong("ts",System.currentTimeMillis())), true); }
        } catch (Exception ignored) { }
        scrollDown();
    }

    private void addBubble(boolean mine, String text, String type, String media, Date when, boolean seen) {
        LinearLayout wrap = new LinearLayout(this); wrap.setGravity(mine ? Gravity.END : Gravity.START); wrap.setPadding(0, dp(3), 0, dp(3));
        LinearLayout bubble = new LinearLayout(this); bubble.setOrientation(LinearLayout.VERTICAL); bubble.setPadding(dp(12), dp(8), dp(12), dp(7)); bubble.setBackground(bg(mine ? PURPLE : Color.WHITE, 18));
        TextView body = label(text, 15, mine ? Color.WHITE : DARK, false); bubble.addView(body);
        if (!media.isEmpty()) { TextView open = label(type.equals("photo") ? "Tap to view photo" : "Tap to play voice", 12, mine ? 0xffeee7ff : PURPLE, true); open.setPadding(0,dp(5),0,0); open.setOnClickListener(v -> openMedia(media, type)); bubble.addView(open); }
        String meta = DateFormat.getTimeInstance(DateFormat.SHORT).format(when) + (mine ? (seen ? "  ✓✓" : "  ✓") : ""); TextView time = label(meta, 10, mine ? 0xffe4dcff : 0xff96909f, false); time.setGravity(Gravity.END); bubble.addView(time);
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

    private void safetyMenu() {
        boolean blocked = isBlocked(); String[] opts = { blocked ? "✅ Unblock user" : "🚫 Block user", "⚑ Report user", "🗑 Clear local chat" };
        new AlertDialog.Builder(this).setTitle(peerName).setItems(opts,(d,which)->{ if(which==0) toggleBlock(); else if(which==1) report(); else clearLocal(); }).show();
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

    private void clearLocal() { getSharedPreferences("chat_store",MODE_PRIVATE).edit().remove("thread_"+safeKey(peerName)).apply(); if(!cloudMode) loadLocal(); Toast.makeText(this,"Local chat cleared",Toast.LENGTH_SHORT).show(); }
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
    @Override protected void onDestroy() { if(messagesListener!=null)messagesListener.remove(); if(threadListener!=null)threadListener.remove(); stopVoice(false); super.onDestroy(); }
}
