package com.kingplus.social;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.InputType;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private static final int MIC_REQUEST = 21;
    private static final int PHOTO_REQUEST = 22;
    private static final int GOOGLE_SIGN_IN_REQUEST = 23;
    private static final int NAVY = 0xff0c1026, CARD = 0xff1b2141, PURPLE = 0xff7146ec;
    private static final int MUTED = 0xffc6bfdf;
    private final ArrayList<String> rooms = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private LinearLayout page;
    private AudioRecord recorder;
    private Thread meterThread;
    private volatile boolean recording;
    private TextView meter;
    private String currentRoom;
    private String displayName;
    private String screen = "login";
    private Uri selectedPhoto;
    private ImageView photoPreview;
    private int mySeat = 1;
    private boolean roomLocked = false;
    private String roomAnnouncement = "Welcome to KING Plus • Be friendly and have fun";
    private int coinBalance = 2500;
    private int giftCount = 0;
    private int receivedGiftCount = 0;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private String phoneVerificationId;
    private boolean giftEffectsEnabled = true;
    private boolean muteAllSeats = false;
    private String selectedHomeCategory = "Hot";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        rooms.add("Music & Friends"); rooms.add("KING Lounge"); rooms.add("Game Talk");
        Set<String> saved = getPreferences(0).getStringSet("rooms", new HashSet<>());
        for (String name : saved) if (!rooms.contains(name)) rooms.add(0, name);
        displayName = getPreferences(0).getString("name", "");
        coinBalance = getPreferences(0).getInt("coins", 2500);
        giftCount = getPreferences(0).getInt("gift_count", 0);
        receivedGiftCount = getPreferences(0).getInt("received_gifts", 0);
        giftEffectsEnabled = getPreferences(0).getBoolean("gift_effects", true);
        muteAllSeats = getPreferences(0).getBoolean("mute_all_seats", false);
        try {
            FirebaseApp app = FirebaseApp.initializeApp(this);
            if (app != null) firebaseAuth = FirebaseAuth.getInstance();
        } catch (Exception ignored) { firebaseAuth = null; }
        if (displayName.isEmpty()) login(); else home();
    }
    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
    private GradientDrawable background(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private void base(String title, String subtitle) {
        stopMic();
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(NAVY);
        page = new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(22), dp(30), dp(22), dp(36)); scroll.addView(page); setContentView(scroll);
        text(title, 29, Color.WHITE, true);
        if (subtitle != null) text(subtitle, 15, MUTED, false);
    }
    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(null, Typeface.BOLD);
        t.setPadding(dp(3), dp(10), dp(3), dp(12)); page.addView(t); return t;
    }
    private void button(String title, int color, Runnable action) {
        Button b = new Button(this); b.setText(title); b.setTextColor(Color.WHITE); b.setAllCaps(false);
        b.setTextSize(16); b.setBackground(background(color, 18));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(56)); p.setMargins(0, dp(7), 0, dp(7));
        page.addView(b, p); b.setOnClickListener(v -> action.run());
    }
    private EditText input(String hint, String value) {
        EditText e = new EditText(this); e.setSingleLine(true); e.setHint(hint); e.setText(value);
        e.setTextColor(Color.WHITE); e.setHintTextColor(MUTED); e.setPadding(dp(16), 0, dp(16), 0);
        e.setBackground(background(CARD, 15));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(56)); p.setMargins(0, dp(16), 0, dp(12));
        page.addView(e, p); return e;
    }
    private void login() {
        screen = "login"; base("👑 KING Plus", "Login or create your account");
        text("Welcome", 23, Color.WHITE, true);
        text("Choose a sign-in method", 14, MUTED, false);
        button("G  Continue with Google", 0xff4285f4, this::googleLogin);
        button("f  Continue with Facebook", 0xff1877f2, () -> socialProviderSetupRequired("Facebook"));
        button("📱  Continue with Mobile Number", PURPLE, this::mobileLogin);
        text("Terms & Privacy  •  Trouble logging in?", 13, MUTED, false);
        text("Mobile login now uses Firebase SMS OTP. Google login is Firebase-ready. Facebook still needs Meta App credentials.", 12, MUTED, false);
    }
    private void socialProviderSetupRequired(String provider) {
        new AlertDialog.Builder(this).setTitle(provider + " sign-in")
            .setMessage(provider + " login needs the provider App ID/secret and Firebase provider setup. No fake local login is used here.")
            .setPositiveButton("OK", null).show();
    }
    private boolean ensureFirebaseReady() {
        if (firebaseAuth != null) return true;
        new AlertDialog.Builder(this).setTitle("Firebase setup required")
            .setMessage("Add google-services.json to the app/ folder, enable Authentication in Firebase, then rebuild the app.")
            .setPositiveButton("OK", null).show();
        return false;
    }
    private void googleLogin() {
        if (!ensureFirebaseReady()) return;
        int id = getResources().getIdentifier("default_web_client_id", "string", getPackageName());
        if (id == 0) {
            new AlertDialog.Builder(this).setTitle("Google login setup")
                .setMessage("default_web_client_id was not generated. Check google-services.json and enable Google under Firebase Authentication > Sign-in method.")
                .setPositiveButton("OK", null).show();
            return;
        }
        String webClientId = getString(id);
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId).requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(this, options);
        startActivityForResult(googleSignInClient.getSignInIntent(), GOOGLE_SIGN_IN_REQUEST);
    }
    private void firebaseAuthWithGoogle(String idToken, String fallbackName) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(this, "Google sign-in failed: " + (task.getException() == null ? "Unknown error" : task.getException().getMessage()), Toast.LENGTH_LONG).show();
                return;
            }
            FirebaseUser user = firebaseAuth.getCurrentUser();
            String name = user != null && user.getDisplayName() != null && !user.getDisplayName().trim().isEmpty() ? user.getDisplayName() : fallbackName;
            saveLocalSession(name, "Google");
        });
    }
    private void mobileLogin() {
        if (!ensureFirebaseReady()) return;
        final EditText phone = new EditText(this);
        phone.setHint("Mobile number, e.g. +919876543210"); phone.setSingleLine(true); phone.setInputType(InputType.TYPE_CLASS_PHONE);
        new AlertDialog.Builder(this).setTitle("Mobile login").setView(phone).setNegativeButton("Cancel", null)
            .setPositiveButton("Send OTP", (d,w) -> {
                String number = normalizePhone(phone.getText().toString());
                if (number == null) { Toast.makeText(this, "Enter a valid mobile number", Toast.LENGTH_SHORT).show(); return; }
                sendRealOtp(number);
            }).show();
    }
    private String normalizePhone(String raw) {
        if (raw == null) return null;
        String n = raw.replaceAll("[\\s()-]", "");
        if (n.matches("[0-9]{10}")) n = "+91" + n;
        if (!n.matches("\\+[0-9]{10,15}")) return null;
        return n;
    }
    private void sendRealOtp(String number) {
        if (firebaseAuth == null || isFinishing() || isDestroyed()) {
            showPhoneError("Firebase is unavailable. Check the app configuration and reopen the app.");
            return;
        }
        try {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(number)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override public void onVerificationCompleted(PhoneAuthCredential credential) { signInWithPhoneCredential(credential, number); }
                @Override public void onVerificationFailed(com.google.firebase.FirebaseException e) {
                    showPhoneError(e.getLocalizedMessage());
                }
                @Override public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                    phoneVerificationId = verificationId;
                    Toast.makeText(MainActivity.this, "OTP sent", Toast.LENGTH_SHORT).show();
                    if (!isFinishing() && !isDestroyed()) showOtpDialog(number);
                }
            }).build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        } catch (RuntimeException e) {
            showPhoneError(e.getLocalizedMessage());
        }
    }
    private void showPhoneError(String reason) {
        if (isFinishing() || isDestroyed()) return;
        new AlertDialog.Builder(this).setTitle("Mobile OTP could not start")
            .setMessage(reason == null || reason.trim().isEmpty() ? "Check Firebase Phone Authentication setup and try again." : reason)
            .setPositiveButton("OK", null).show();
    }
    private void showOtpDialog(String number) {
        final EditText otp = new EditText(this); otp.setHint("6-digit OTP"); otp.setSingleLine(true);
        otp.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        new AlertDialog.Builder(this).setTitle("Verify " + number).setMessage("Enter the SMS OTP sent by Firebase.")
            .setView(otp).setNegativeButton("Cancel", null).setPositiveButton("Verify", (d,w) -> {
                String code = otp.getText().toString().trim();
                if (phoneVerificationId == null || code.length() < 6) { Toast.makeText(this, "Enter the 6-digit OTP", Toast.LENGTH_SHORT).show(); return; }
                signInWithPhoneCredential(PhoneAuthProvider.getCredential(phoneVerificationId, code), number);
            }).show();
    }
    private void signInWithPhoneCredential(PhoneAuthCredential credential, String number) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(this, "OTP verification failed: " + (task.getException() == null ? "Invalid OTP" : task.getException().getMessage()), Toast.LENGTH_LONG).show();
                return;
            }
            String shortNumber = number.length() > 4 ? "KING " + number.substring(number.length() - 4) : "KING User";
            saveLocalSession(shortNumber, "Mobile");
        });
    }
    private void saveLocalSession(String name, String provider) {
        displayName=name; getPreferences(0).edit().putString("name",name).putString("login_provider",provider).apply(); home();
    }
    private void home() {
        renderHome("Hot");
    }

    private void simplePage(String title, String message){ screen="sub"; base(title,null); text(message,18,Color.WHITE,true); button("પાછા Party પર જાઓ",PURPLE,this::home); }
    private void composePost() {
        screen = "compose"; selectedPhoto = null; base("નવી પોસ્ટ", "તમારી વાત અને ફોટો શેર કરો");
        EditText caption = input("શું ચાલી રહ્યું છે?", "");
        caption.setSingleLine(false); caption.setMinLines(3);
        button("ગેલેરીમાંથી ફોટો પસંદ કરો", CARD, () -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(intent, PHOTO_REQUEST);
        });
        photoPreview = new ImageView(this);
        photoPreview.setAdjustViewBounds(true);
        photoPreview.setMaxHeight(dp(260));
        page.addView(photoPreview, new LinearLayout.LayoutParams(-1, -2));
        button("પોસ્ટ સાચવો", PURPLE, () -> {
            String body = caption.getText().toString().trim();
            if (body.isEmpty() && selectedPhoto == null) { caption.setError("લખાણ અથવા ફોટો ઉમેરો"); return; }
            try {
                JSONArray posts = new JSONArray(getPreferences(0).getString("posts", "[]"));
                JSONObject post = new JSONObject();
                post.put("name", displayName); post.put("body", body);
                post.put("photo", selectedPhoto == null ? "" : selectedPhoto.toString());
                post.put("time", System.currentTimeMillis());
                JSONArray updated = new JSONArray(); updated.put(post);
                for (int i = 0; i < Math.min(posts.length(), 99); i++) updated.put(posts.get(i));
                getPreferences(0).edit().putString("posts", updated.toString()).apply();
                addXp(5); incrementMission("mission_post",1);
                momentsPage();
            } catch (Exception ex) { Toast.makeText(this, "પોસ્ટ સાચવી શકાઈ નથી", Toast.LENGTH_SHORT).show(); }
        });
        button("પાછા જાઓ", CARD, this::home);
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_IN_REQUEST) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null && account.getIdToken() != null) firebaseAuthWithGoogle(account.getIdToken(), account.getDisplayName() == null ? "Google User" : account.getDisplayName());
            } catch (ApiException e) { Toast.makeText(this, "Google sign-in cancelled/failed: " + e.getStatusCode(), Toast.LENGTH_LONG).show(); }
            return;
        }
        if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedPhoto = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(selectedPhoto, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (photoPreview != null) photoPreview.setImageURI(selectedPhoto);
            } catch (Exception ex) { selectedPhoto = null; Toast.makeText(this, "ફોટો ખોલી શકાતો નથી", Toast.LENGTH_SHORT).show(); }
        }
    
        if (requestCode == 24 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri=data.getData();
            try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); getPreferences(0).edit().putString("profile_photo",uri.toString()).apply(); Toast.makeText(this,"Profile photo updated",Toast.LENGTH_SHORT).show(); profile(); }
            catch (Exception ex) { Toast.makeText(this,"Profile photo could not be saved",Toast.LENGTH_SHORT).show(); }
            return;
        }
    }
    private void showPosts() {
        try {
            JSONArray posts = new JSONArray(getPreferences(0).getString("posts", "[]"));
            if (posts.length() == 0) { text("હજુ કોઈ પોસ્ટ નથી. તમારી પહેલી પોસ્ટ બનાવો!", 15, MUTED, false); return; }
            for (int i = 0; i < posts.length(); i++) {
                JSONObject post = posts.getJSONObject(i);
                text("👤 " + post.optString("name", "સભ્ય"), 17, Color.WHITE, true);
                if (!post.optString("body").isEmpty()) text(post.optString("body"), 16, Color.WHITE, false);
                String photo = post.optString("photo");
                if (!photo.isEmpty()) {
                    ImageView picture = new ImageView(this);
                    picture.setAdjustViewBounds(true); picture.setMaxHeight(dp(360));
                    try { picture.setImageURI(Uri.parse(photo)); page.addView(picture, new LinearLayout.LayoutParams(-1, -2)); }
                    catch (Exception ignored) { text("ફોટો ઉપલબ્ધ નથી", 14, MUTED, false); }
                }
                text("──────────────", 14, MUTED, false);
            }
        } catch (Exception ex) { text("પોસ્ટ લોડ થઈ શકી નથી", 14, MUTED, false); }
    }
    private void createRoom() {
        EditText e = new EditText(this); e.setSingleLine(true); e.setHint("રૂમનું નામ");
        LinearLayout wrap = new LinearLayout(this); wrap.setPadding(dp(20), 0, dp(20), 0); wrap.addView(e);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("નવો રૂમ").setView(wrap)
            .setNegativeButton("રદ કરો", null).setPositiveButton("બનાવો", null).create();
        dialog.setOnShowListener(v -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(w -> {
            String name = e.getText().toString().trim();
            if (name.isEmpty()) { e.setError("રૂમનું નામ લખો"); return; }
            if (rooms.contains(name)) { e.setError("આ નામનો રૂમ પહેલેથી છે"); return; }
            rooms.add(0, name);
            getPreferences(0).edit().putStringSet("rooms", new HashSet<>(rooms)).apply();
            dialog.dismiss(); room(name);
        })); dialog.show();
    }
    private void room(String name) {
        boolean entering = !"room".equals(screen) || currentRoom == null || !currentRoom.equals(name);
        screen="room"; currentRoom=name; stopMic();
        String rk = safeKey(name);
        roomLocked = getPreferences(0).getBoolean("room_locked_"+rk,false);
        roomAnnouncement = getPreferences(0).getString("room_announcement_"+rk,"Welcome to KING Plus • Be friendly and have fun");
        if (entering) { incrementMission("mission_room",1); addXp(10); }
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(0xff24133f);
        LinearLayout header=new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL); header.setPadding(dp(12),dp(12),dp(10),dp(4));
        TextView back=new TextView(this); back.setText("‹"); back.setTextSize(38); back.setTextColor(Color.WHITE); back.setGravity(Gravity.CENTER); back.setOnClickListener(v->home()); header.addView(back,new LinearLayout.LayoutParams(dp(42),dp(58)));
        LinearLayout info=new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL);
        TextView title=new TextView(this); title.setText(name); title.setTextColor(Color.WHITE); title.setTextSize(17); title.setTypeface(null,Typeface.BOLD); info.addView(title);
        TextView id=new TextView(this); id.setText("ID: "+roomId(name)+"   👥 "+(28+Math.abs(name.hashCode()%220))); id.setTextColor(0xffcfc4df); id.setTextSize(12); info.addView(id);
        header.addView(info,new LinearLayout.LayoutParams(0,dp(58),1));
        TextView menu=new TextView(this); menu.setText("⋯"); menu.setTextSize(30); menu.setTextColor(Color.WHITE); menu.setGravity(Gravity.CENTER); menu.setOnClickListener(v->roomMenu()); header.addView(menu,new LinearLayout.LayoutParams(dp(52),dp(52))); root.addView(header);
        TextView notice=new TextView(this); notice.setText((roomLocked?"🔒  ":"📢  ")+roomAnnouncement); notice.setTextColor(0xffffd768); notice.setTextSize(12); notice.setPadding(dp(16),dp(7),dp(16),dp(10)); root.addView(notice);
        LinearLayout seats=new LinearLayout(this); seats.setOrientation(LinearLayout.VERTICAL); seats.setPadding(dp(8),dp(2),dp(8),0);
        for(int r=0;r<3;r++){
            LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER);
            for(int c=0;c<4;c++){
                int n=r*4+c+1; LinearLayout seat=new LinearLayout(this); seat.setOrientation(LinearLayout.VERTICAL); seat.setGravity(Gravity.CENTER);
                TextView av=new TextView(this); av.setText(n==mySeat?"👑":seatAvatar(n)); av.setTextSize(n==mySeat?29:24); av.setGravity(Gravity.CENTER); av.setTextColor(Color.WHITE); av.setBackground(background(n==mySeat?0xff8b4ff4:0xff4a3768,50)); seat.addView(av,new LinearLayout.LayoutParams(dp(56),dp(56)));
                TextView lab=new TextView(this); Set<String> muted=getPreferences(0).getStringSet("muted_seats_"+rk,new HashSet<>()); Set<String> admins=getPreferences(0).getStringSet("room_admins_"+rk,new HashSet<>()); String otherName=seatLabel(n); String seatName=(muted.contains(String.valueOf(n))?"🔇 ":"")+(admins.contains(otherName)?"🛡 ":"")+otherName; lab.setText(n==mySeat?displayName:seatName); lab.setTextColor(n==mySeat?Color.WHITE:0xffc7bbd8); lab.setTextSize(10); lab.setGravity(Gravity.CENTER); seat.addView(lab,new LinearLayout.LayoutParams(-1,dp(25)));
                final int seatNo=n; seat.setOnClickListener(v->seatActions(seatNo)); row.addView(seat,new LinearLayout.LayoutParams(0,dp(86),1));
            }
            seats.addView(row,new LinearLayout.LayoutParams(-1,dp(86)));
        }
        root.addView(seats);
        TextView divider=new TextView(this); divider.setText("  Room Chat"); divider.setTextColor(0xffd8cce8); divider.setTextSize(12); divider.setPadding(dp(10),dp(4),0,dp(4)); root.addView(divider);
        ScrollView chatScroll=new ScrollView(this); LinearLayout chat=new LinearLayout(this); chat.setOrientation(LinearLayout.VERTICAL); chat.setPadding(dp(12),dp(4),dp(12),dp(4)); chatScroll.addView(chat);
        addChat(chat,"System","Welcome to KING Plus 👋"); loadRoomChat(chat);
        root.addView(chatScroll,new LinearLayout.LayoutParams(-1,0,1));
        meter=new TextView(this); meter.setText(muteAllSeats?"All seats muted by host":"Mic off"); meter.setTextColor(0xffbfb1d1); meter.setTextSize(11); meter.setGravity(Gravity.CENTER); root.addView(meter,new LinearLayout.LayoutParams(-1,dp(22)));
        LinearLayout controls=new LinearLayout(this); controls.setGravity(Gravity.CENTER); controls.setPadding(dp(7),dp(5),dp(7),dp(10)); String[] icons={"💬\nChat","🎤\nMic","🎁\nGift","🎮\nGame","🚪\nLeave"};
        for(int i=0;i<icons.length;i++){ TextView x=new TextView(this); x.setText(icons[i]); x.setTextColor(Color.WHITE); x.setTextSize(11); x.setGravity(Gravity.CENTER); x.setBackground(background(i==2?0xff8a49ed:0xff3a2856,18)); final int k=i; x.setOnClickListener(v->{if(k==0)chatDialog(chat);else if(k==1){if(muteAllSeats){Toast.makeText(this,"Host has muted all seats",Toast.LENGTH_SHORT).show();return;}if(recording){stopMic();meter.setText("Mic off");}else requestMic();}else if(k==2)giftDialog(chat);else if(k==3)roomGames();else home();}); LinearLayout.LayoutParams xp=new LinearLayout.LayoutParams(0,dp(58),1); xp.setMargins(dp(3),0,dp(3),0); controls.addView(x,xp);}
        root.addView(controls); setContentView(root);
    }

    private void addChat(LinearLayout chat,String who,String message){ TextView t=new TextView(this); t.setText(who+":  "+message); t.setTextColor(Color.WHITE); t.setTextSize(14); t.setPadding(dp(10),dp(7),dp(10),dp(7)); chat.addView(t,new LinearLayout.LayoutParams(-1,-2)); }
    private void chatDialog(LinearLayout chat){
        final EditText e=new EditText(this); e.setHint("Say something...");
        new AlertDialog.Builder(this).setTitle("Room chat").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Send",(d,w)->{
            String m=e.getText().toString().trim();
            if(!m.isEmpty()){ addChat(chat,displayName,m); appendRoomChat(displayName,m); incrementMission("mission_chat",1); addXp(2); }
        }).show();
    }

    private void giftDialog(LinearLayout chat){
        String[] gifts={"🌹 Rose  1","❤️ Heart  5","🍭 Candy  10","🎂 Cake  50","💎 Diamond  100","🚗 Sports Car  500","👑 Crown  999","🎆 Fireworks  1999"};
        int[] prices={1,5,10,50,100,500,999,1999};
        new AlertDialog.Builder(this).setTitle("🎁 Gift Store • Balance "+coinBalance).setItems(gifts,(d,w)->{
            if(coinBalance < prices[w]) { Toast.makeText(this,"Not enough coins",Toast.LENGTH_SHORT).show(); return; }
            final int giftIndex=w; String[] targets={"🌟 Whole room","🎵 Lily","🎮 Alex","💜 Mia","✨ DJ Max"};
            new AlertDialog.Builder(this).setTitle("Send "+gifts[giftIndex]+" to…").setItems(targets,(d2,t)->sendGift(chat,gifts[giftIndex],prices[giftIndex],targets[t])).setNegativeButton("Cancel",null).show();
        }).setNegativeButton("Close",null).show();
    }

    private void roomGames(){
        final String[] gameNames={"🎲 Ludo Party","🎯 Lucky Dart","🐍 Snake Battle","🏁 Racing","🎁 Lucky Draw"};
        new AlertDialog.Builder(this).setTitle("🎮 Room Games").setItems(gameNames,(d,w)->playMiniGame(gameNames[w])).setNegativeButton("Close",null).show();
    }
    private void playMiniGame(String game){
        String result; int reward=0;
        if(game.contains("Dart")){int score=60+(int)(Math.random()*41);result="Score: "+score+" / 100";reward=score>=90?20:5;}
        else if(game.contains("Draw")){reward=10+(int)(Math.random()*91);result="Reward: "+reward+" coins";}
        else if(game.contains("Racing")){int pos=1+(int)(Math.random()*4);result="Finished #"+pos;reward=pos==1?30:5;}
        else if(game.contains("Snake")){int score=100+(int)(Math.random()*901);result="Battle score: "+score;reward=score>700?25:5;}
        else {result="Ludo table created • Waiting for players";reward=5;}
        if(reward>0){coinBalance=getPreferences(0).getInt("coins",2500)+reward;getPreferences(0).edit().putInt("coins",coinBalance).apply();appendTransaction("+"+reward+" coins • Game reward");addXp(3);}
        final int won=reward; new AlertDialog.Builder(this).setTitle(game).setMessage(result+(won>0?"\n+"+won+" coins added":"")).setPositiveButton("Play again",(d,w)->playMiniGame(game)).setNegativeButton("Back",null).show();
    }

    private void giftEffect(String gift){ final Toast t=Toast.makeText(this,"✨  "+gift+"  ✨",Toast.LENGTH_LONG); t.setGravity(Gravity.CENTER,0,0); t.show(); }
    private void seatActions(int seatNo){
        String user=seatLabel(seatNo);
        String[] actions = seatNo==mySeat ? new String[]{"🎤 Mic on/off","↔ Change seat","🚪 Leave seat"} : new String[]{"🪑 Take seat","👤 View profile","🛡 Make / remove admin","🔇 Mute / unmute seat"};
        new AlertDialog.Builder(this).setTitle("Seat "+seatNo+" • "+user).setItems(actions,(d,w)->{
            if(seatNo==mySeat){ if(w==0){ if(recording) stopMic(); else requestMic(); } else if(w==1) chooseSeat(); else { mySeat=0; Toast.makeText(this,"Seat left",Toast.LENGTH_SHORT).show(); room(currentRoom); } }
            else if(w==0){ if(roomLocked){Toast.makeText(this,"Room is locked",Toast.LENGTH_SHORT).show();return;} mySeat=seatNo; Toast.makeText(this,"You joined Seat "+seatNo,Toast.LENGTH_SHORT).show(); room(currentRoom); }
            else if(w==1) userProfile(user);
            else if(w==2){Set<String>a=new HashSet<>(getPreferences(0).getStringSet("room_admins_"+safeKey(currentRoom),new HashSet<>()));if(a.contains(user))a.remove(user);else a.add(user);getPreferences(0).edit().putStringSet("room_admins_"+safeKey(currentRoom),a).apply();Toast.makeText(this,a.contains(user)?"Admin added":"Admin removed",Toast.LENGTH_SHORT).show();}
            else {Set<String>m=new HashSet<>(getPreferences(0).getStringSet("muted_seats_"+safeKey(currentRoom),new HashSet<>()));String k=String.valueOf(seatNo);if(m.contains(k))m.remove(k);else m.add(k);getPreferences(0).edit().putStringSet("muted_seats_"+safeKey(currentRoom),m).apply();Toast.makeText(this,m.contains(k)?"Seat muted":"Seat unmuted",Toast.LENGTH_SHORT).show();room(currentRoom);}
        }).show();
    }

    private void chooseSeat(){ final String[] seats=new String[12]; for(int i=0;i<12;i++) seats[i]="Seat "+(i+1); new AlertDialog.Builder(this).setTitle("Change seat").setItems(seats,(d,w)->{mySeat=w+1; room(currentRoom);}).show(); }

    private void roomMenu(){ String[] items={"ℹ Room info","📢 Announcement","👥 Members","☰ Channel list","⚙ Room settings","🛡 Admin controls","🔒 Lock / Unlock room","🚩 Report room"}; new AlertDialog.Builder(this).setTitle(currentRoom).setItems(items,(d,w)->{ if(w==0) roomInfo(); else if(w==1) editAnnouncement(); else if(w==2) roomMembers(); else if(w==3) channelList(); else if(w==4) roomSettings(); else if(w==5) adminControls(); else if(w==6){roomLocked=!roomLocked;getPreferences(0).edit().putBoolean("room_locked_"+safeKey(currentRoom),roomLocked).apply();Toast.makeText(this,roomLocked?"Room locked":"Room unlocked",Toast.LENGTH_SHORT).show();room(currentRoom);} else reportDialog(currentRoom); }).setNegativeButton("Close",null).show(); }
    private void roomInfo(){
        int members=28+Math.abs((currentRoom==null?0:currentRoom.hashCode())%220);
        new AlertDialog.Builder(this).setTitle("Room information").setMessage("👑 "+currentRoom+"\nID: "+roomId(currentRoom)+"\nMembers: "+members+"\nSeats: 12\nCategory: Party\nStatus: "+(roomLocked?"Locked":"Open")+"\n\n"+roomAnnouncement).setPositiveButton("Share Room",(d,w)->shareText("Join my KING Plus room: "+currentRoom+"\nRoom ID: "+roomId(currentRoom))).setNegativeButton("Close",null).show();
    }

    private void editAnnouncement(){
        final EditText e=new EditText(this); e.setText(roomAnnouncement); e.setSelectAllOnFocus(true);
        new AlertDialog.Builder(this).setTitle("Room announcement").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{
            String x=e.getText().toString().trim();if(!x.isEmpty())roomAnnouncement=x;
            getPreferences(0).edit().putString("room_announcement_"+safeKey(currentRoom),roomAnnouncement).apply();
            Toast.makeText(this,"Announcement saved",Toast.LENGTH_SHORT).show();room(currentRoom);
        }).show();
    }

    private void roomMembers(){
        String[] names={displayName,"Lily","Alex","Mia","DJ Max","Queen","Sam","Gaur"};
        String[] roles={"Host","Admin","Member","Member","Member","Member","Member","Member"};
        String[] rows=new String[names.length]; for(int i=0;i<names.length;i++) rows[i]=(i==0?"👑 ":"👤 ")+names[i]+" • "+roles[i];
        new AlertDialog.Builder(this).setTitle("Members • "+(28+Math.abs(currentRoom.hashCode()%220))).setItems(rows,(d,w)->{if(w==0)profile();else userProfile(names[w]);}).setNegativeButton("Close",null).show();
    }


    private void channelList(){
        String[] channels={"🔥 Hot Party","🎵 Music Live","💜 Friends Forever","🎮 Game Talk","🌙 Late Night Talk","✨ New Friends"};
        new AlertDialog.Builder(this).setTitle("Channels").setItems(channels,(d,w)->room(channels[w].substring(channels[w].indexOf(' ')+1))).setNegativeButton("Close",null).show();
    }
    private void roomSettings(){
        final String[] settings={"🔒 Room lock: "+(roomLocked?"ON":"OFF"),"🎤 Everyone can use mic: "+(getPreferences(0).getBoolean("room_mic_anyone_"+safeKey(currentRoom),true)?"ON":"OFF"),"🎁 Gift effects: "+(giftEffectsEnabled?"ON":"OFF"),"👥 Change my seat","📢 Edit announcement","🧹 Clear local chat"};
        new AlertDialog.Builder(this).setTitle("Room Settings").setItems(settings,(d,w)->{
            if(w==0){roomLocked=!roomLocked;getPreferences(0).edit().putBoolean("room_locked_"+safeKey(currentRoom),roomLocked).apply();room(currentRoom);}
            else if(w==1){String k="room_mic_anyone_"+safeKey(currentRoom);boolean v=!getPreferences(0).getBoolean(k,true);getPreferences(0).edit().putBoolean(k,v).apply();Toast.makeText(this,v?"Mic permission enabled":"Mic permission disabled",Toast.LENGTH_SHORT).show();}
            else if(w==2){giftEffectsEnabled=!giftEffectsEnabled;getPreferences(0).edit().putBoolean("gift_effects",giftEffectsEnabled).apply();Toast.makeText(this,giftEffectsEnabled?"Gift effects enabled":"Gift effects disabled",Toast.LENGTH_SHORT).show();}
            else if(w==3) chooseSeat();
            else if(w==4) editAnnouncement();
            else { getPreferences(0).edit().remove("room_chat_"+safeKey(currentRoom)).apply(); Toast.makeText(this,"Room chat cleared",Toast.LENGTH_SHORT).show(); room(currentRoom);}
        }).setNegativeButton("Close",null).show();
    }

    private void adminControls(){
        String[] a={"🎤 Open all mics","🔇 Mute all seats","🧹 Clear chat","👥 Manage admins","🚫 Block list","⚙ Room permissions"};
        new AlertDialog.Builder(this).setTitle("Admin controls").setItems(a,(d,w)->{
            if(w==0){muteAllSeats=false;getPreferences(0).edit().putBoolean("mute_all_seats",false).apply();Toast.makeText(this,"All seat mics opened",Toast.LENGTH_SHORT).show();room(currentRoom);}
            else if(w==1){muteAllSeats=true;stopMic();getPreferences(0).edit().putBoolean("mute_all_seats",true).apply();Toast.makeText(this,"All seats muted",Toast.LENGTH_SHORT).show();room(currentRoom);}
            else if(w==2){getPreferences(0).edit().remove("room_chat_"+safeKey(currentRoom)).apply();Toast.makeText(this,"Chat cleared",Toast.LENGTH_SHORT).show();room(currentRoom);}
            else if(w==3) manageAdmins();
            else if(w==4) privacySafetyPage();
            else roomSettings();
        }).setNegativeButton("Close",null).show();
    }


    private void requestMic() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MIC_REQUEST); return;
        }
        startMic();
    }
    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == MIC_REQUEST && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED && "room".equals(screen)) startMic();
        else if (requestCode == MIC_REQUEST) Toast.makeText(this, "માઇક પરવાનગી જરૂરી છે", Toast.LENGTH_SHORT).show();
    }
    private void startMic() {
        stopMic();
        int rate = 16000;
        int min = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (min <= 0) { Toast.makeText(this, "માઇક ઉપલબ્ધ નથી", Toast.LENGTH_SHORT).show(); return; }
        try {
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, rate, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, Math.max(min, 2048));
            if (recorder.getState() != AudioRecord.STATE_INITIALIZED) { stopMic(); return; }
            recorder.startRecording(); recording = true;
            if (meter != null) meter.setText("માઇક ચાલુ છે • બોલીને તપાસો");
            AudioRecord source = recorder;
            meterThread = new Thread(() -> {
                short[] samples = new short[1024];
                while (recording) {
                    int count = source.read(samples, 0, samples.length);
                    if (count <= 0) break;
                    long sum = 0;
                    for (int i = 0; i < count; i++) sum += Math.abs((int)samples[i]);
                    int level = (int)Math.min(10, (sum / count) / 1800);
                    handler.post(() -> { if (recording && meter != null) meter.setText("માઇક ચાલુ છે  " + bars(level)); });
                }
            }); meterThread.start();
        } catch (Exception ex) { stopMic(); Toast.makeText(this, "માઇક શરૂ થઈ શક્યો નથી", Toast.LENGTH_SHORT).show(); }
    }
    private void stopMic() {
        recording = false;
        if (recorder != null) {
            try { recorder.stop(); } catch (Exception ignored) { }
            recorder.release(); recorder = null;
        }
        meterThread = null;
    }
    private String bars(int level) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 10; i++) result.append(i < level ? '▮' : '▯');
        return result.toString();
    }
    private TextView cardLine(LinearLayout host, String title, String subtitle, Runnable action) {
        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16),dp(11),dp(16),dp(11)); card.setBackground(background(0xffffffff,16));
        TextView a=new TextView(this); a.setText(title); a.setTextSize(16); a.setTextColor(0xff202020); a.setTypeface(null,Typeface.BOLD); card.addView(a);
        TextView b=new TextView(this); b.setText(subtitle); b.setTextSize(13); b.setTextColor(0xff888888); b.setPadding(0,dp(4),0,0); card.addView(b);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(70)); lp.setMargins(0,dp(5),0,dp(5)); host.addView(card,lp);
        if(action!=null) card.setOnClickListener(v->action.run()); return a;
    }
    private void messages() {
        screen="messages"; stopMic();
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(0xfff7f7fb);
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(18),dp(12),dp(10),dp(4));TextView h=new TextView(this);h.setText("Messages");h.setTextSize(30);h.setTextColor(0xff171717);h.setTypeface(null,Typeface.BOLD);head.addView(h,new LinearLayout.LayoutParams(0,dp(60),1));TextView act=new TextView(this);act.setText("▣   ＋");act.setTextSize(24);act.setGravity(Gravity.CENTER);act.setTextColor(0xff222222);act.setOnClickListener(v->newMessageDialog());head.addView(act,new LinearLayout.LayoutParams(dp(100),dp(60)));root.addView(head);
        LinearLayout quick=new LinearLayout(this); quick.setGravity(Gravity.CENTER); String[] q={"👁\nVisited Me","👥\nGroups","🔔\nNotifications"};
        for(int i=0;i<q.length;i++){TextView x=new TextView(this);x.setText(q[i]);x.setGravity(Gravity.CENTER);x.setTextSize(13);x.setTextColor(0xff333333);final int k=i;x.setOnClickListener(v->{if(k==0)peoplePage("People Who Visited Me");else if(k==1)peoplePage("Groups & Friends");else notificationsCenter();});quick.addView(x,new LinearLayout.LayoutParams(0,dp(78),1));}root.addView(quick);
        ScrollView sv=new ScrollView(this);LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);list.setPadding(dp(14),dp(8),dp(14),dp(16));sv.addView(list);
        cardLine(list,"🔥  KING Party Room","[Chat]  Tap to open room conversation",()->conversation("KING Party Room"));
        cardLine(list,"👁  People Who Visited Me","See recent profile visitors",()->peoplePage("People Who Visited Me"));
        cardLine(list,"🎵  Lily","See you in the music room ✨",()->conversation("Lily"));
        cardLine(list,"🎮  Alex","Let's play tonight",()->conversation("Alex"));
        cardLine(list,"👑  KING Plus Official","Welcome and account updates",()->conversation("KING Plus Official"));
        cardLine(list,"👥  Group Notifications","Family and room admin changes",this::notificationsCenter);
        cardLine(list,"🔔  Interactive notifications","Likes, follows and post activity",this::notificationsCenter);
        root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));addBottomNav(root,3);setContentView(root);
    }

    private void addBottomNav(LinearLayout root,int selected){
        LinearLayout nav=new LinearLayout(this); nav.setGravity(Gravity.CENTER); nav.setBackgroundColor(Color.WHITE); String[] ni={"⌂\nParty","♟\nGame","◇\nDiscover","✉\nMessages","●\nMe"};
        for(int i=0;i<ni.length;i++){TextView n=new TextView(this);n.setText(ni[i]);n.setTextSize(12);n.setGravity(Gravity.CENTER);n.setTextColor(i==selected?0xff8a43ff:0xff777777);final int k=i;n.setOnClickListener(v->{if(k==0)home();else if(k==1)games();else if(k==2)discover();else if(k==3)messages();else profile();});nav.addView(n,new LinearLayout.LayoutParams(0,dp(62),1));} root.addView(nav);
    }

    private void games(){
        incrementMission("mission_game",1);
        screen="games"; stopMic(); LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(0xfff7f7fb);
        TextView h=new TextView(this); h.setText("Game"); h.setTextSize(30); h.setTextColor(0xff171717); h.setTypeface(null,Typeface.BOLD); h.setPadding(dp(18),dp(18),0,dp(10)); root.addView(h,new LinearLayout.LayoutParams(-1,dp(70)));
        LinearLayout tabs=new LinearLayout(this); tabs.setPadding(dp(14),0,dp(14),dp(8)); TextView hot=new TextView(this); hot.setText("Hot"); hot.setGravity(Gravity.CENTER); hot.setTypeface(null,Typeface.BOLD); hot.setBackground(background(0xffffe500,12)); hot.setTextColor(0xff111111); tabs.addView(hot,new LinearLayout.LayoutParams(0,dp(48),1)); TextView ludo=new TextView(this); ludo.setText("LUDO"); ludo.setGravity(Gravity.CENTER); ludo.setTypeface(null,Typeface.BOLD); ludo.setTextColor(0xff333333); ludo.setBackground(background(0xffefeff5,12)); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(48),1);lp.setMargins(dp(8),0,0,0);tabs.addView(ludo,lp);root.addView(tabs);
        ScrollView sv=new ScrollView(this); LinearLayout list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); list.setPadding(dp(14),dp(8),dp(14),dp(18)); sv.addView(list);
        cardLine(list,"🎲  Ludo","18,791 players • 2–4 players",()->playMiniGame("🎲 Ludo Party"));
        cardLine(list,"🐑  Sheep Fight","18,049 players • Quick battle",()->playMiniGame("🐍 Snake Battle"));
        cardLine(list,"🗡  Knife Hit","18,161 players • Skill challenge",()->playMiniGame("🎯 Lucky Dart"));
        cardLine(list,"⬡  Hexagon Fight","19,023 players • Arena",()->playMiniGame("🏁 Racing"));
        cardLine(list,"🃏  Rummy","17,277 players • Cards",()->playMiniGame("🎁 Lucky Draw"));
        cardLine(list,"♠  Spider Challenge","19,060 players • Solitaire",()->playMiniGame("🎯 Lucky Dart"));
        root.addView(sv,new LinearLayout.LayoutParams(-1,0,1)); addBottomNav(root,1); setContentView(root);
    }

    private void discover(){
        screen="discover"; stopMic(); LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(0xfff7f7fb);
        TextView h=new TextView(this); h.setText("Discover"); h.setTextSize(25); h.setTextColor(0xff171717); h.setTypeface(null,Typeface.BOLD); h.setPadding(dp(18),dp(18),0,dp(10)); root.addView(h,new LinearLayout.LayoutParams(-1,dp(66)));
        ScrollView sv=new ScrollView(this); LinearLayout list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); list.setPadding(dp(14),dp(8),dp(14),dp(18)); sv.addView(list);
        cardLine(list,"📝  Moments","Share posts with the KING Plus community",this::momentsPage);
        cardLine(list,"🔥  Trending Rooms","Popular voice parties right now",()->room("Trending Party"));
        cardLine(list,"🎉  Events","Music nights, games and community events",()->simplePage("Events","🎉 Upcoming KING Plus events will appear here"));
        cardLine(list,"👥  Find Friends","Discover new people in the community",()->peoplePage("Discover People"));
        cardLine(list,"🎵  Music","Live music and singing rooms",()->room("Music Live"));
        cardLine(list,"💜  New & Nearby","Fresh rooms and new creators",()->room("New Friends"));
        root.addView(sv,new LinearLayout.LayoutParams(-1,0,1)); addBottomNav(root,2); setContentView(root);
    }
    private void conversation(String who){
        screen="conversation"; base(who,getPreferences(0).getBoolean("privacy_online",true)?"Online":"KING Plus member"); text("Today",13,MUTED,false);
        text(who+":  Hello 👋",16,Color.WHITE,false);
        String key="chat_"+safeKey(who); String saved=getPreferences(0).getString(key,"");
        if(!saved.isEmpty()) for(String m:saved.split("\n")) if(!m.trim().isEmpty()) text(displayName+":  "+m,16,0xffd9caff,false);
        EditText e=input("Write a message…","");
        button("Send",PURPLE,()->{if(getPreferences(0).getBoolean("privacy_friends_dm",false)){Set<String> f=getPreferences(0).getStringSet("friends",new HashSet<>());if(!f.contains(who)&&!who.contains("Official")&&!who.contains("Party")){Toast.makeText(this,"Friends-only messages is enabled",Toast.LENGTH_SHORT).show();return;}}String m=e.getText().toString().trim();if(!m.isEmpty()){String prev=getPreferences(0).getString(key,"");getPreferences(0).edit().putString(key,prev+m+"\n").apply();addXp(1);Toast.makeText(this,"Message saved",Toast.LENGTH_SHORT).show();conversation(who);}});
        button("Share current room",CARD,()->{String r=currentRoom==null?"KING Lounge":currentRoom;shareText("Join me in KING Plus room: "+r+" • ID "+roomId(r));});
        button("Back to Messages",CARD,this::messages);
    }

    private void momentsPage(){
        screen="moments"; base("Moments","Share updates with the KING Plus community");
        button("＋ Create photo/text post",PURPLE,this::composePost);
        text("Your recent posts",18,Color.WHITE,true); showPosts();
        EditText post=input("Quick text Moment…","");
        button("Post quick Moment",CARD,()->{String m=post.getText().toString().trim(); if(m.isEmpty()){post.setError("Write something first");return;} String old=getPreferences(0).getString("moments",""); getPreferences(0).edit().putString("moments",m+"|||"+old).apply(); addXp(5); momentsPage();});
        String saved=getPreferences(0).getString("moments","");
        if(!saved.isEmpty()){text("Quick Moments",18,Color.WHITE,true);String[] posts=saved.split("\\|\\|\\|"); for(String m:posts) if(!m.trim().isEmpty()){text("●  "+displayName,15,Color.WHITE,true); text(m,16,Color.WHITE,false); button("♡ Like   💬 Comment",CARD,()->Toast.makeText(this,"Interaction saved locally",Toast.LENGTH_SHORT).show());}}
        button("Back to Discover",CARD,this::discover);
    }

    private void peoplePage(String title){
        screen="people"; base(title,"KING Plus community");
        ArrayList<String> list=new ArrayList<>();
        if(title.toLowerCase().contains("friend")){list.addAll(getPreferences(0).getStringSet("friends",new HashSet<>()));}
        else if(title.toLowerCase().contains("following")){list.addAll(getPreferences(0).getStringSet("following",new HashSet<>()));}
        if(list.isEmpty()){list.add("Lily ✨");list.add("Alex 🎮");list.add("Mia 💜");list.add("DJ Max 🎵");list.add("Queen 👑");}
        for(String n:list){String clean=n.replace(" ✨","").replace(" 🎮","").replace(" 💜","").replace(" 🎵","").replace(" 👑","");button("👤  "+n,CARD,()->userProfile(clean));}
        button("Back",PURPLE,this::messages);
    }

    private void userProfile(String who){
        incrementMission("mission_profile",1);
        screen="user_profile"; base(who,"KING Plus member");
        text("●",54,0xffd9caff,true); text("ID: "+Math.abs(who.hashCode()%900000+100000),14,MUTED,false);
        text("Music • Games • Voice rooms",15,Color.WHITE,false);
        Set<String> following=new HashSet<>(getPreferences(0).getStringSet("following",new HashSet<>())); boolean isFollowing=following.contains(who);
        Set<String> friends=new HashSet<>(getPreferences(0).getStringSet("friends",new HashSet<>())); boolean isFriend=friends.contains(who);
        button(isFollowing?"✓ Following":"＋ Follow",PURPLE,()->{Set<String> f=new HashSet<>(getPreferences(0).getStringSet("following",new HashSet<>())); if(f.contains(who))f.remove(who);else f.add(who);getPreferences(0).edit().putStringSet("following",f).apply();userProfile(who);});
        button(isFriend?"✓ Friend":"＋ Add Friend",CARD,()->{Set<String> f=new HashSet<>(getPreferences(0).getStringSet("friends",new HashSet<>())); if(f.contains(who))f.remove(who);else f.add(who);getPreferences(0).edit().putStringSet("friends",f).apply();Toast.makeText(this,f.contains(who)?"Friend added":"Friend removed",Toast.LENGTH_SHORT).show();userProfile(who);});
        button("✉ Message",CARD,()->conversation(who));
        button("🎤 Invite to Room",CARD,()->{String r=currentRoom==null?"KING Lounge":currentRoom;appendNotification("🎤 Room invitation prepared for "+who+" • "+r);Toast.makeText(this,"Room invite prepared",Toast.LENGTH_SHORT).show();});
        button("⋮ Safety options",CARD,()->safetyOptions(who)); button("Back",CARD,()->peoplePage("Discover People"));
    }

    private void safetyOptions(String who){
        String[] a={"🚫 Block / Unblock","⚑ Report user"};
        new AlertDialog.Builder(this).setTitle(who).setItems(a,(d,w)->{if(w==0){Set<String>b=new HashSet<>(getPreferences(0).getStringSet("blocked",new HashSet<>()));boolean blocked;if(b.contains(who)){b.remove(who);blocked=false;}else{b.add(who);blocked=true;}getPreferences(0).edit().putStringSet("blocked",b).apply();Toast.makeText(this,blocked?"User blocked":"User unblocked",Toast.LENGTH_SHORT).show();}else reportDialog(who);}).show();
    }
    private void reportDialog(String who){
        String[] reasons={"Spam","Harassment","Inappropriate content","Fake account","Other"};
        new AlertDialog.Builder(this).setTitle("Report "+who).setItems(reasons,(d,w)->{int count=getPreferences(0).getInt("reports",0)+1;getPreferences(0).edit().putInt("reports",count).apply();Toast.makeText(this,"Report saved for review",Toast.LENGTH_SHORT).show();}).setNegativeButton("Cancel",null).show();
    }
    private void notifications(){ notificationsCenter(); }

    private void sideMenu(){ String[] items={"👤 My Profile","✏ Edit Profile","💜 Friends","👥 Followers","🎒 Backpack","🏆 Level","👑 Rankings","💠 VIP Center","🏠 Family & Agency","💎 Wallet","🔔 Notifications","⚙ Settings","❓ Help & Feedback"}; new AlertDialog.Builder(this).setTitle("KING Plus").setItems(items,(d,w)->{if(w==0)profile();else if(w==1)editProfile();else if(w==2)peoplePage("Friends");else if(w==3)peoplePage("Followers");else if(w==4)backpackPage();else if(w==5)levelPage();else if(w==6)rankingsPage();else if(w==7)vipPage();else if(w==8)familyAgencyPage();else if(w==9)walletPage();else if(w==10)notificationsCenter();else if(w==11)settingsPage();else helpCenterPage();}).setNegativeButton("Close",null).show(); }
    private void editProfile(){
        screen="editProfile"; base("Edit Profile","Update your KING Plus identity");
        String photo=getPreferences(0).getString("profile_photo","");
        if(!photo.isEmpty()){ImageView img=new ImageView(this);img.setAdjustViewBounds(true);img.setMaxHeight(dp(220));try{img.setImageURI(Uri.parse(photo));page.addView(img,new LinearLayout.LayoutParams(-1,-2));}catch(Exception ignored){}}
        button("📷 Choose profile photo",CARD,()->{Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType("image/*");intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);startActivityForResult(intent,24);});
        EditText name=input("Display name",displayName);
        EditText bio=input("Bio",getPreferences(0).getString("bio","Love music, games and new friends ✨"));
        EditText hometown=input("Hometown",getPreferences(0).getString("hometown",""));
        EditText birthday=input("Birthday (YYYY-MM-DD)",getPreferences(0).getString("birthday","2000-01-01"));
        EditText tags=input("Tags (music, games, friends)",getPreferences(0).getString("tags","Music, Games"));
        button("Save changes",PURPLE,()->{String n=name.getText().toString().trim(); if(n.isEmpty()){name.setError("Name required");return;} displayName=n; getPreferences(0).edit().putString("name",n).putString("bio",bio.getText().toString().trim()).putString("hometown",hometown.getText().toString().trim()).putString("birthday",birthday.getText().toString().trim()).putString("tags",tags.getText().toString().trim()).apply(); Toast.makeText(this,"Profile updated",Toast.LENGTH_SHORT).show(); profile();});
        button("Back",CARD,this::profile);
    }


    private void walletPage(){
        screen="wallet"; base("My Wallet","KING Plus balance");
        text("💎 "+coinBalance+" Coins",28,Color.WHITE,true); text("Available balance",14,MUTED,false);
        text("🎁 Gifts sent: "+giftCount+"   •   Received: "+receivedGiftCount,16,Color.WHITE,false);
        button("Open Gift Catalog",PURPLE,this::giftCatalogPage);
        button("📜 Transaction History",CARD,this::transactionHistoryPage);
        button("📅 Daily Check-in",CARD,this::dailyCheckInPage);
        text("Real-money recharge/payout is intentionally disabled until a verified payment backend is connected.",13,MUTED,false);
        button("Back",CARD,this::profile);
    }

    private void giftCatalogPage(){
        screen="gift_catalog"; base("Gift Center","Choose gifts for voice rooms");
        String[] names={"🌹 Rose","❤️ Heart","🍭 Candy","🎂 Cake","💎 Diamond","🚗 Sports Car","👑 Crown","🎆 Fireworks"};
        int[] prices={1,5,10,50,100,500,999,1999};
        for(int i=0;i<names.length;i++){ final int k=i; button(names[i]+"   •   "+prices[i]+" coins",CARD,()->new AlertDialog.Builder(this).setTitle(names[k]).setMessage("Gift price: "+prices[k]+" coins\n\nSend gifts from inside a voice room so a recipient/room is selected.").setPositiveButton("OK",null).show()); }
        button("Back to Wallet",CARD,this::walletPage);
    }
    private void backpackPage(){
        screen="backpack"; base("Backpack","Your room items and rewards");
        text("Gift Activity",18,Color.WHITE,true);text("🎁 Sent gifts   ×"+giftCount,17,Color.WHITE,false);text("📥 Received gifts   ×"+receivedGiftCount,17,Color.WHITE,false);
        String frame=getPreferences(0).getString("equipped_frame","Royal Frame");String effect=getPreferences(0).getString("equipped_effect","Welcome Sparkle");
        text("Frames & Effects",18,Color.WHITE,true);button("👑 Equipped frame: "+frame,CARD,()->selectCosmetic("equipped_frame",new String[]{"Royal Frame","Music Frame","Heart Frame","Minimal Frame"},this::backpackPage));button("✨ Entrance effect: "+effect,CARD,()->selectCosmetic("equipped_effect",new String[]{"Welcome Sparkle","Crown Drop","Rose Shower","None"},this::backpackPage));
        button("Back",CARD,this::profile);
    }

    private void levelPage(){
        screen="level"; base("Level & Achievements","Your KING Plus progress");
        int xp=getPreferences(0).getInt("xp",120); int level=xp/500+1; int inLevel=xp%500;
        text("🏆 Level "+level,30,Color.WHITE,true); text(inLevel+" / 500 XP to Level "+(level+1),15,MUTED,false);
        text("Achievements",18,Color.WHITE,true);
        text("🎤 Voice Explorer  "+(getMissionValue("mission_room")>0?"✓":"○"),16,Color.WHITE,false);
        text("🎁 First Gift  "+(giftCount>0?"✓":"○"),16,Color.WHITE,false);
        text("👥 Friends  "+new HashSet<>(getPreferences(0).getStringSet("friends",new HashSet<>())).size()+"/10",16,MUTED,false);
        text("💬 Room messages today  "+getMissionValue("mission_chat"),16,MUTED,false);
        button("Back",CARD,this::profile);
    }

    private void settingsPage(){
        screen="settings"; base("Settings","Account and app controls");
        text("Account",18,Color.WHITE,true); text("Signed in as "+displayName,15,MUTED,false);
        button("Edit Profile",CARD,this::editProfile);
        button("🔔 Notifications: "+(getPreferences(0).getBoolean("setting_notifications",true)?"ON":"OFF"),CARD,()->toggleSetting("setting_notifications","Notifications",this::settingsPage));
        button("✨ Entrance effects: "+(getPreferences(0).getBoolean("setting_entrance_effects",true)?"ON":"OFF"),CARD,()->toggleSetting("setting_entrance_effects","Entrance effects",this::settingsPage));
        button("🔊 Room sounds: "+(getPreferences(0).getBoolean("setting_room_sounds",true)?"ON":"OFF"),CARD,()->toggleSetting("setting_room_sounds","Room sounds",this::settingsPage));
        button("Privacy & Safety",CARD,this::privacySafetyPage);
        button("Help & Feedback",CARD,this::helpCenterPage);
        button("Log out",0xffb23a48,()->new AlertDialog.Builder(this).setTitle("Log out?").setMessage("Your local KING Plus session will be cleared. Saved local content will remain on this device.").setNegativeButton("Cancel",null).setPositiveButton("Log out",(d,w)->{if(firebaseAuth!=null)firebaseAuth.signOut();if(googleSignInClient!=null)googleSignInClient.signOut();getPreferences(0).edit().remove("name").apply();displayName="";login();}).show());
        button("Back",PURPLE,this::profile);
    }

    private void privacySafetyPage(){
        screen="privacy"; base("Privacy & Safety","Control your KING Plus experience");
        Set<String> blocked=new HashSet<>(getPreferences(0).getStringSet("blocked",new HashSet<>()));
        button("💬 Messages from friends only: "+(getPreferences(0).getBoolean("privacy_friends_dm",false)?"ON":"OFF"),CARD,()->toggleSetting("privacy_friends_dm","Friends-only messages",this::privacySafetyPage));
        button("🟢 Show online status: "+(getPreferences(0).getBoolean("privacy_online",true)?"ON":"OFF"),CARD,()->toggleSetting("privacy_online","Online status",this::privacySafetyPage));
        button("🎁 Allow room gifts: "+(getPreferences(0).getBoolean("privacy_gifts",true)?"ON":"OFF"),CARD,()->toggleSetting("privacy_gifts","Room gifts",this::privacySafetyPage));
        text("Blocked users: "+blocked.size(),17,Color.WHITE,true);
        if(blocked.isEmpty()) text("No blocked users",14,MUTED,false); else for(String n:blocked) button("🚫 "+n+"  • tap to unblock",CARD,()->{Set<String>b=new HashSet<>(getPreferences(0).getStringSet("blocked",new HashSet<>()));b.remove(n);getPreferences(0).edit().putStringSet("blocked",b).apply();privacySafetyPage();});
        text("Reports submitted: "+getPreferences(0).getInt("reports",0),15,MUTED,false);
        button("Back to Settings",PURPLE,this::settingsPage);
    }

    private void profile() {
        screen="profile"; stopMic();
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(0xfff7f7fb);
        LinearLayout top=new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL); top.setPadding(dp(16),dp(16),dp(12),dp(6)); TextView title=new TextView(this); title.setText("Me"); title.setTextSize(25); title.setTextColor(0xff191919); title.setTypeface(null,Typeface.BOLD); top.addView(title,new LinearLayout.LayoutParams(0,dp(52),1)); TextView menu=new TextView(this); menu.setText("☰"); menu.setTextSize(27); menu.setGravity(Gravity.CENTER); menu.setOnClickListener(v->sideMenu()); top.addView(menu,new LinearLayout.LayoutParams(dp(54),dp(52))); root.addView(top);
        LinearLayout hero=new LinearLayout(this); hero.setOrientation(LinearLayout.VERTICAL); hero.setGravity(Gravity.CENTER); hero.setPadding(dp(12),dp(10),dp(12),dp(10));
        String photo=getPreferences(0).getString("profile_photo",""); if(photo.isEmpty()){TextView av=new TextView(this);av.setText("👑");av.setTextSize(48);av.setGravity(Gravity.CENTER);av.setBackground(background(0xffeadfff,50));hero.addView(av,new LinearLayout.LayoutParams(dp(88),dp(88)));}else{ImageView av=new ImageView(this);av.setScaleType(ImageView.ScaleType.CENTER_CROP);try{av.setImageURI(Uri.parse(photo));}catch(Exception ignored){}hero.addView(av,new LinearLayout.LayoutParams(dp(88),dp(88)));}
        TextView nm=new TextView(this); nm.setText(displayName); nm.setTextSize(23); nm.setTextColor(0xff202020); nm.setTypeface(null,Typeface.BOLD); nm.setGravity(Gravity.CENTER); hero.addView(nm);
        int xp=getPreferences(0).getInt("xp",120); TextView id=new TextView(this); id.setText("ID: "+roomId(displayName)+"   ♢ Lv."+(xp/500+1)); id.setTextSize(13); id.setTextColor(0xff777777); id.setGravity(Gravity.CENTER); hero.addView(id);
        TextView bio=new TextView(this); bio.setText(getPreferences(0).getString("bio","Love music, games and new friends ✨")); bio.setTextSize(13); bio.setTextColor(0xff666666); bio.setGravity(Gravity.CENTER); bio.setPadding(dp(20),dp(4),dp(20),0); hero.addView(bio); root.addView(hero,new LinearLayout.LayoutParams(-1,dp(185)));
        int following=new HashSet<>(getPreferences(0).getStringSet("following",new HashSet<>())).size(); int friends=new HashSet<>(getPreferences(0).getStringSet("friends",new HashSet<>())).size();
        LinearLayout stats=new LinearLayout(this); stats.setGravity(Gravity.CENTER); String[] ss={following+"\nFollowing","356\nFollowers",friends+"\nFriends",(1200+xp)+"\nCharm"}; for(String z:ss){TextView x=new TextView(this);x.setText(z);x.setGravity(Gravity.CENTER);x.setTextSize(14);x.setTextColor(0xff333333);stats.addView(x,new LinearLayout.LayoutParams(0,dp(64),1));} root.addView(stats);
        ScrollView sv=new ScrollView(this); LinearLayout list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); list.setPadding(dp(14),dp(5),dp(14),dp(14)); sv.addView(list);
        String hometown=getPreferences(0).getString("hometown",""); if(!hometown.isEmpty()) cardLine(list,"📍 "+hometown,"Hometown",null);
        cardLine(list,"💎 My Wallet","Coins, gifts and transaction history",this::walletPage); cardLine(list,"🎒 Backpack","Gifts, frames and room items",this::backpackPage); cardLine(list,"🏆 Level & Achievements","Level "+(xp/500+1)+" • "+xp+" XP",this::levelPage); cardLine(list,"👑 Rankings","Weekly stars and top gifters",this::rankingsPage); cardLine(list,"💠 VIP Center","Badges, frames and entrance effects",this::vipPage); cardLine(list,"🏠 Family & Agency","Family members, code sharing and host tools",this::familyAgencyPage); cardLine(list,"📅 Daily Check-in","Claim a daily reward and build streak",this::dailyCheckInPage); cardLine(list,"🏅 Missions","Daily activity missions and rewards",this::missionsPage); cardLine(list,"⚙ Settings","Account, privacy and notifications",this::settingsPage);
        root.addView(sv,new LinearLayout.LayoutParams(-1,0,1)); addBottomNav(root,4); setContentView(root);
    }

    private void rankingsPage(){
        screen="rankings"; base("Rankings","Community leaderboard preview");
        text("👑 Weekly Stars",20,Color.WHITE,true);
        String[] stars={"🥇 Lily   •  28.4K charm","🥈 Alex   •  21.7K charm","🥉 Mia   •  18.9K charm","4   KING Host   •  12.6K charm","5   Music Fan   •  9.8K charm"};
        for(String x:stars) button(x,CARD,()->Toast.makeText(this,"Profile preview opened",Toast.LENGTH_SHORT).show());
        text("🎁 Top Gifters",20,Color.WHITE,true);
        text("1. RoyalKing  15.2K coins     2. Moon  11.8K     3. DJ Max  9.4K",14,MUTED,false);
        text("Rankings are demo/local until live server statistics are connected.",13,MUTED,false);
        button("Back to Profile",CARD,this::profile);
    }
    private void vipPage(){
        screen="vip"; base("VIP Center","KING Plus membership preview");
        text("💠 VIP 1",28,Color.WHITE,true);
        text("Unlock profile badge, room entrance effect, special frame and VIP identity styling.",15,MUTED,false);
        button("👑 VIP Badge Preview",CARD,()->Toast.makeText(this,"VIP badge preview",Toast.LENGTH_SHORT).show());
        button("✨ Entrance Effect Preview",CARD,()->Toast.makeText(this,"Entrance effect preview",Toast.LENGTH_SHORT).show());
        button("🖼 Profile Frame Preview",CARD,()->Toast.makeText(this,"VIP frame preview",Toast.LENGTH_SHORT).show());
        text("No real purchase is enabled in this prototype.",13,MUTED,false);
        button("Back to Profile",CARD,this::profile);
    }
    private void familyAgencyPage(){
        screen="family_agency"; base("Family & Agency","Community and host center");
        String family=getPreferences(0).getString("family_name","Not joined"); String code=getPreferences(0).getString("family_code","");
        text("🏠 KING Family",24,Color.WHITE,true); text("Current family: "+family,16,Color.WHITE,true); if(!code.isEmpty()) text("Family code: "+code,16,0xffffd768,true);
        button("Create Family",PURPLE,()->{ final EditText name=new EditText(this); name.setHint("Family name"); new AlertDialog.Builder(this).setTitle("Create Family").setView(name).setNegativeButton("Cancel",null).setPositiveButton("Create",(d,w)->{String n=name.getText().toString().trim(); if(n.isEmpty()) n="KING Family"; String c=String.valueOf(100000+(int)(Math.random()*900000));getPreferences(0).edit().putString("family_name",n).putString("family_code",c).apply();Toast.makeText(this,"Family created",Toast.LENGTH_SHORT).show();familyAgencyPage();}).show();});
        button("Join Family by Code",CARD,()->{final EditText codeInput=new EditText(this);codeInput.setHint("6-digit family code");new AlertDialog.Builder(this).setTitle("Join Family").setView(codeInput).setNegativeButton("Cancel",null).setPositiveButton("Join",(d,w)->{String c=codeInput.getText().toString().trim();if(!c.matches("[0-9]{6}")){Toast.makeText(this,"Enter a valid 6-digit code",Toast.LENGTH_SHORT).show();return;}getPreferences(0).edit().putString("family_name","Family "+c).putString("family_code",c).apply();Toast.makeText(this,"Family joined locally",Toast.LENGTH_SHORT).show();familyAgencyPage();}).show();});
        if(!code.isEmpty()) button("Share Family Name + Code",CARD,()->shareText("Join my KING Plus family: "+family+"\nFamily code: "+code));
        button("Family Members",CARD,()->new AlertDialog.Builder(this).setTitle("Family Members").setItems(new String[]{"👑 "+displayName+"  • Owner","🎤 Lily  • Host","💜 Alex  • Member","🎮 Mia  • Member"},null).setPositiveButton("Close",null).show());
        text("🎙 Host Agency",20,Color.WHITE,true);
        boolean applied=getPreferences(0).getBoolean("host_application",false); text(applied?"✅ Host application submitted":"No host application submitted",14,MUTED,false);
        button(applied?"Application Submitted":"Host Application",CARD,()->{if(applied){Toast.makeText(this,"Application is already saved",Toast.LENGTH_SHORT).show();return;}getPreferences(0).edit().putBoolean("host_application",true).apply();Toast.makeText(this,"Application saved",Toast.LENGTH_SHORT).show();familyAgencyPage();});
        button("Agency Dashboard",CARD,()->simplePage("Agency Dashboard","Hosts: 4\nActive rooms: 2\nLocal preview data; online agency verification requires backend connection."));
        button("Back to Profile",CARD,this::profile);
    }

    private void dailyCheckInPage(){
        screen="checkin"; base("Daily Check-in","Build a streak and earn local rewards");
        SharedPreferences p=getPreferences(0); long today=System.currentTimeMillis()/86400000L; long last=p.getLong("checkin_day_long",-10); int streak=p.getInt("checkin_streak",0); boolean claimed=last==today;
        int shownStreak=claimed?streak:(last==today-1?streak+1:1); int reward=Math.min(100+Math.max(0,shownStreak-1)*20,300);
        text("🔥 Streak: "+(claimed?streak:Math.max(streak,0))+" days",20,Color.WHITE,true);
        text(claimed?"✅ Today's reward already claimed":"🎁 Today's reward: "+reward+" coins",18,Color.WHITE,true);
        button(claimed?"Claimed":"Claim "+reward+" coins",claimed?CARD:PURPLE,()->{if(p.getLong("checkin_day_long",-10)==today){Toast.makeText(this,"Already claimed today",Toast.LENGTH_SHORT).show();return;}long prev=p.getLong("checkin_day_long",-10);int newStreak=prev==today-1?p.getInt("checkin_streak",0)+1:1;int gain=Math.min(100+Math.max(0,newStreak-1)*20,300);coinBalance=p.getInt("coins",2500)+gain;p.edit().putInt("coins",coinBalance).putLong("checkin_day_long",today).putInt("checkin_streak",newStreak).apply();appendTransaction("+"+gain+" coins • Daily check-in");addXp(10);Toast.makeText(this,gain+" coins added",Toast.LENGTH_SHORT).show();dailyCheckInPage();});
        text("Rewards increase with your streak up to 300 coins/day.",14,MUTED,false); button("Back to Profile",CARD,this::profile);
    }

    private void missionsPage(){
        screen="missions"; base("Missions","Daily activity progress and rewards");
        missionButton("mission_room","🎤 Join a voice room",getMissionValue("mission_room"),1,40);
        missionButton("mission_chat","💬 Send room messages",getMissionValue("mission_chat"),3,30);
        missionButton("mission_gift","🎁 Send a gift",getMissionValue("mission_gift"),1,50);
        missionButton("mission_profile","👤 Visit profiles",getMissionValue("mission_profile"),2,25);
        missionButton("mission_game","🎮 Open Game Center",getMissionValue("mission_game"),1,35);
        text("Missions reset by date on this device. Rewards are local coins until the online account backend is connected.",13,MUTED,false); button("Back to Profile",CARD,this::profile);
    }


    private void renderHome(String category) {
        selectedHomeCategory=category; screen="home"; stopMic();
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(0xfffaf9fc);
        LinearLayout top=new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL); top.setPadding(dp(16),dp(10),dp(12),dp(4));
        TextView brand=new TextView(this);brand.setText("KING Plus");brand.setTextSize(26);brand.setTextColor(0xff17131f);brand.setTypeface(null,Typeface.BOLD);top.addView(brand,new LinearLayout.LayoutParams(0,dp(52),1));
        TextView actions=new TextView(this);actions.setText("⌕    ⊕");actions.setTextSize(27);actions.setTextColor(0xff2b2730);actions.setGravity(Gravity.CENTER);actions.setOnClickListener(v->new AlertDialog.Builder(this).setItems(new String[]{"Search","Create Party","Notifications"},(d,w)->{if(w==0)searchCommunity();else if(w==1)createRoomDialog();else notificationsCenter();}).show());top.addView(actions,new LinearLayout.LayoutParams(dp(105),dp(52)));root.addView(top);
        LinearLayout tabs=new LinearLayout(this);tabs.setPadding(dp(8),0,dp(8),dp(6));String[] cats={"Hot","Event","Date","Music","Game"};for(String c:cats){TextView t=new TextView(this);t.setText(c);t.setGravity(Gravity.CENTER);t.setTextSize(16);boolean active=c.equals(category);t.setTypeface(null,active?Typeface.BOLD:Typeface.NORMAL);t.setTextColor(active?0xff111111:0xff55525c);t.setBackground(active?background(0xffffe600,10):background(0xfff0eff4,10));LinearLayout.LayoutParams tlp=new LinearLayout.LayoutParams(0,dp(42),1);tlp.setMargins(dp(3),0,dp(3),0);tabs.addView(t,tlp);t.setOnClickListener(v->renderHome(c));}root.addView(tabs);
        ScrollView sv=new ScrollView(this);LinearLayout content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(12),dp(8),dp(12),dp(14));sv.addView(content);
        String[][] data=roomData(category);
        if("Hot".equals(category) && rooms.size()>3){TextView yours=new TextView(this);yours.setText("Your rooms");yours.setTextSize(17);yours.setTypeface(null,Typeface.BOLD);yours.setTextColor(0xff222222);yours.setPadding(dp(2),dp(2),0,dp(6));content.addView(yours);for(String r:rooms){if(r.equals("Music & Friends")||r.equals("KING Lounge")||r.equals("Game Talk"))continue;TextView rr=new TextView(this);rr.setText("👑  "+r+"   • tap to open");rr.setTextSize(14);rr.setTextColor(0xff222222);rr.setGravity(Gravity.CENTER_VERTICAL);rr.setPadding(dp(14),0,dp(10),0);rr.setBackground(background(Color.WHITE,14));rr.setOnClickListener(v->room(r));LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(54));rp.setMargins(0,0,0,dp(6));content.addView(rr,rp);}}
        if("Hot".equals(category)){TextView banner=new TextView(this);banner.setText("👑  KING PLUS PARTY\n      Voice • Friends • Games");banner.setTextSize(20);banner.setTextColor(Color.WHITE);banner.setTypeface(null,Typeface.BOLD);banner.setGravity(Gravity.CENTER_VERTICAL);banner.setPadding(dp(18),0,dp(10),0);banner.setBackground(background(0xff7648e8,20));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(105));bp.setMargins(0,0,0,dp(12));content.addView(banner,bp);}
        for(int idx=0;idx<data.length;idx+=2){LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);for(int c=0;c<2;c++){int k=idx+c;if(k>=data.length){View spacer=new View(this);row.addView(spacer,new LinearLayout.LayoutParams(0,dp(148),1));continue;}String[] d=data[k];LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(10),dp(9),dp(10),dp(9));card.setBackground(background(Color.WHITE,16));TextView cover=new TextView(this);cover.setText(d[0]);cover.setTextSize(38);cover.setGravity(Gravity.CENTER);cover.setBackground(background(k%2==0?0xffffe7f1:0xffeee7ff,14));card.addView(cover,new LinearLayout.LayoutParams(-1,dp(75)));TextView title=new TextView(this);title.setText(d[1]);title.setTextSize(14);title.setTextColor(0xff211d26);title.setTypeface(null,Typeface.BOLD);title.setPadding(0,dp(7),0,0);card.addView(title);TextView meta=new TextView(this);meta.setText("💬 Chat   •   "+d[2]+" online");meta.setTextSize(11);meta.setTextColor(0xff8b8790);card.addView(meta);card.setOnClickListener(v->room(d[1]));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(148),1);cp.setMargins(c==0?0:dp(5),dp(5),c==0?dp(5):0,dp(5));row.addView(card,cp);}content.addView(row);}
        root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));addBottomNav(root,0);setContentView(root);
    }
    private String[][] roomData(String category){
        if("Music".equals(category))return new String[][]{{"🎤","Music & Friends","11"},{"🎵","Night Music","8"},{"🎧","DJ Party","12"},{"🎸","Acoustic House","6"},{"🎹","Hindi Songs","9"},{"🎶","Music Lovers","10"}};
        if("Game".equals(category))return new String[][]{{"🎮","Game Talk","8"},{"🎲","Ludo Friends","6"},{"🎯","Challenge Room","9"},{"🏁","Racing Fans","5"},{"🃏","Card Club","7"},{"🏆","Winner Zone","11"}};
        if("Event".equals(category))return new String[][]{{"🎉","Weekend Party","12"},{"🎁","Gift Festival","10"},{"👑","KING Night","11"},{"✨","New Star Event","8"},{"🎤","Singer Contest","9"},{"🏅","Room Championship","6"}};
        if("Date".equals(category))return new String[][]{{"💜","Friends Forever","6"},{"🌹","Coffee & Talk","8"},{"🌙","Late Night Talk","9"},{"✨","New Friends","5"},{"💬","Open Chat","7"},{"🌸","Good Vibes","10"}};
        return new String[][]{{"🎤","Music & Friends","11"},{"👑","KING Lounge","12"},{"🎮","Game Talk","8"},{"💜","Friends Forever","6"},{"🎵","Night Music","9"},{"✨","New Friends","7"},{"🌙","Late Night Talk","5"},{"🎧","DJ Party","10"}};
    }
    private String safeKey(String v){return v==null?"none":v.replaceAll("[^A-Za-z0-9]","_");}
    private String roomId(String v){return String.valueOf(100000+Math.abs((v==null?0:v.hashCode())%900000));}
    private String seatAvatar(int n){String[] a={"🎵","🎮","💜","✨","🌸","🎧","🌙","👑","🎤","⭐","🌹","🎲"};return a[(n-1)%a.length];}
    private String seatLabel(int n){String[] names={"Lily","Alex","Mia","DJ Max","Queen","Sam","Gaur","Sona","Harsh","Music","Star","Guest"};return names[(n-1)%names.length];}
    private void appendRoomChat(String who,String message){
        String k="room_chat_"+safeKey(currentRoom);String old=getPreferences(0).getString(k,"");String line=who+"~~"+message.replace("\n"," ");String updated=old.isEmpty()?line:old+"|||"+line;String[] all=updated.split("\\|\\|\\|");if(all.length>40){StringBuilder b=new StringBuilder();for(int i=all.length-40;i<all.length;i++){if(b.length()>0)b.append("|||");b.append(all[i]);}updated=b.toString();}getPreferences(0).edit().putString(k,updated).apply();
    }
    private void loadRoomChat(LinearLayout chat){String raw=getPreferences(0).getString("room_chat_"+safeKey(currentRoom),"");if(raw.isEmpty()){addChat(chat,"Lily","Hello everyone ✨");addChat(chat,"Alex","Nice room! 🎵");return;}for(String line:raw.split("\\|\\|\\|")){String[] p=line.split("~~",2);if(p.length==2)addChat(chat,p[0],p[1]);}}
    private void sendGift(LinearLayout chat,String gift,int price,String target){if(!getPreferences(0).getBoolean("privacy_gifts",true)){Toast.makeText(this,"Room gifts are disabled in Privacy",Toast.LENGTH_SHORT).show();return;}if(coinBalance<price){Toast.makeText(this,"Not enough coins",Toast.LENGTH_SHORT).show();return;}coinBalance-=price;giftCount++;getPreferences(0).edit().putInt("coins",coinBalance).putInt("gift_count",giftCount).apply();String msg="sent "+gift+" to "+target;addChat(chat,displayName,msg);appendRoomChat(displayName,msg);appendTransaction("-"+price+" coins • "+gift+" → "+target);incrementMission("mission_gift",1);addXp(Math.min(20,Math.max(2,price/25)));if(giftEffectsEnabled)giftEffect(gift);}
    private void manageAdmins(){Set<String> admins=new HashSet<>(getPreferences(0).getStringSet("room_admins_"+safeKey(currentRoom),new HashSet<>()));String[] names={"Lily","Alex","Mia","DJ Max"};boolean[] checked=new boolean[names.length];for(int i=0;i<names.length;i++)checked[i]=admins.contains(names[i]);new AlertDialog.Builder(this).setTitle("Manage admins").setMultiChoiceItems(names,checked,(d,w,isChecked)->{if(isChecked)admins.add(names[w]);else admins.remove(names[w]);}).setPositiveButton("Save",(d,w)->{getPreferences(0).edit().putStringSet("room_admins_"+safeKey(currentRoom),admins).apply();Toast.makeText(this,"Admins updated",Toast.LENGTH_SHORT).show();}).setNegativeButton("Cancel",null).show();}
    private String todayKey(){return String.valueOf(System.currentTimeMillis()/86400000L);}
    private void incrementMission(String key,int amount){String k=key+"_"+todayKey();int v=getPreferences(0).getInt(k,0)+amount;getPreferences(0).edit().putInt(k,v).apply();}
    private int getMissionValue(String key){return getPreferences(0).getInt(key+"_"+todayKey(),0);}
    private void missionButton(String key,String title,int progress,int target,int reward){boolean done=progress>=target;boolean claimed=getPreferences(0).getBoolean("claimed_"+key+"_"+todayKey(),false);String label=title+"   "+Math.min(progress,target)+"/"+target+(claimed?"   ✓ Claimed":done?"   • Claim "+reward:" ");button(label,claimed?CARD:(done?PURPLE:CARD),()->{if(!done){Toast.makeText(this,"Mission not complete yet",Toast.LENGTH_SHORT).show();return;}if(getPreferences(0).getBoolean("claimed_"+key+"_"+todayKey(),false)){Toast.makeText(this,"Already claimed",Toast.LENGTH_SHORT).show();return;}coinBalance=getPreferences(0).getInt("coins",2500)+reward;getPreferences(0).edit().putInt("coins",coinBalance).putBoolean("claimed_"+key+"_"+todayKey(),true).apply();appendTransaction("+"+reward+" coins • Mission reward");addXp(10);Toast.makeText(this,reward+" coins claimed",Toast.LENGTH_SHORT).show();missionsPage();});}
    private void addXp(int value){int xp=getPreferences(0).getInt("xp",120)+value;getPreferences(0).edit().putInt("xp",xp).apply();}
    private void appendTransaction(String line){String old=getPreferences(0).getString("transactions","");String stamp=new java.text.SimpleDateFormat("dd MMM HH:mm",java.util.Locale.getDefault()).format(new java.util.Date());String updated=stamp+" • "+line+(old.isEmpty()?"":"|||"+old);getPreferences(0).edit().putString("transactions",updated).apply();}
    private void transactionHistoryPage(){screen="transactions";base("Transaction History","Local wallet activity");String raw=getPreferences(0).getString("transactions","");if(raw.isEmpty())text("No wallet activity yet",15,MUTED,false);else for(String x:raw.split("\\|\\|\\|"))text(x,15,Color.WHITE,false);button("Back to Wallet",PURPLE,this::walletPage);}
    private void toggleSetting(String key,String label,Runnable refresh){boolean def=!"privacy_friends_dm".equals(key);boolean v=!getPreferences(0).getBoolean(key,def);getPreferences(0).edit().putBoolean(key,v).apply();Toast.makeText(this,label+": "+(v?"ON":"OFF"),Toast.LENGTH_SHORT).show();refresh.run();}
    private void shareText(String value){Intent send=new Intent(Intent.ACTION_SEND);send.setType("text/plain");send.putExtra(Intent.EXTRA_TEXT,value);startActivity(Intent.createChooser(send,"Share with"));}
    private void appendNotification(String value){String old=getPreferences(0).getString("notifications_log","");getPreferences(0).edit().putString("notifications_log",value+(old.isEmpty()?"":"|||"+old)).putBoolean("notifications_read",false).apply();}
    private void newMessageDialog(){final EditText e=new EditText(this);e.setHint("User name");new AlertDialog.Builder(this).setTitle("New message").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Open",(d,w)->{String n=e.getText().toString().trim();if(n.isEmpty())n="New Friend";conversation(n);}).show();}
    private void selectCosmetic(String key,String[] values,Runnable refresh){int selected=0;String current=getPreferences(0).getString(key,values[0]);for(int i=0;i<values.length;i++)if(values[i].equals(current))selected=i;new AlertDialog.Builder(this).setTitle("Choose item").setSingleChoiceItems(values,selected,(d,w)->{getPreferences(0).edit().putString(key,values[w]).apply();d.dismiss();refresh.run();}).setNegativeButton("Cancel",null).show();}

    private void helpCenterPage(){screen="help";base("Help & Feedback","KING Plus support center");button("Login & OTP help",CARD,()->new AlertDialog.Builder(this).setTitle("Login & OTP").setMessage("Use a real phone number with country code. Firebase Phone Authentication must be enabled and SHA fingerprints configured.").setPositiveButton("OK",null).show());button("Voice room help",CARD,()->new AlertDialog.Builder(this).setTitle("Voice rooms").setMessage("Join a seat, allow microphone permission, then tap Mic. Live multi-user audio still requires a real-time voice service/backend.").setPositiveButton("OK",null).show());button("Send feedback",PURPLE,()->{final EditText e=new EditText(this);e.setHint("Describe the issue or suggestion");new AlertDialog.Builder(this).setTitle("Feedback").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{String m=e.getText().toString().trim();if(!m.isEmpty()){String old=getPreferences(0).getString("feedback","");getPreferences(0).edit().putString("feedback",m+"|||"+old).apply();Toast.makeText(this,"Feedback saved",Toast.LENGTH_SHORT).show();}}).show();});button("Back to Settings",CARD,this::settingsPage);}

    @Override public void onBackPressed() {
        if ("login".equals(screen) || "home".equals(screen)) super.onBackPressed(); else home();
    }
    @Override protected void onPause() { super.onPause(); stopMic(); }
    @Override protected void onDestroy() { stopMic(); super.onDestroy(); }
    private void notificationsCenter(){
        screen="notifications"; base("Notifications","Activity from your KING Plus community");
        String raw=getPreferences(0).getString("notifications_log","");
        if(raw.isEmpty()) raw="👑 Welcome to KING Plus|||💜 Lily followed you|||🎁 Alex sent a Rose in KING Lounge|||🎮 Game Talk invited you to play|||🎤 Music & Friends is live now";
        for(String n:raw.split("\\|\\|\\|")) if(!n.trim().isEmpty()) button(n,CARD,()->Toast.makeText(this,"Opened",Toast.LENGTH_SHORT).show());
        button("Mark all as read",PURPLE,()->{getPreferences(0).edit().putBoolean("notifications_read",true).apply();Toast.makeText(this,"All notifications marked as read",Toast.LENGTH_SHORT).show();});
        button("Clear notifications",CARD,()->{getPreferences(0).edit().putString("notifications_log","").apply();notificationsCenter();});
        button("Back to Messages",CARD,this::messages);
    }

    private void searchCommunity(){
        screen="search"; base("Search","Find rooms and people"); EditText q=input("Search KING Plus…","");
        button("Search",PURPLE,()->{String x=q.getText().toString().trim();if(x.isEmpty()){q.setError("Type a name or room");return;}ArrayList<String> results=new ArrayList<>();String low=x.toLowerCase();for(String cat:new String[]{"Hot","Music","Game","Date","Event"})for(String[] d:roomData(cat))if(d[1].toLowerCase().contains(low)&&!results.contains("🎤 "+d[1]))results.add("🎤 "+d[1]);for(String n:new String[]{"Lily","Alex","Mia","DJ Max","Queen","Sam","Gaur"})if(n.toLowerCase().contains(low))results.add("👤 "+n);if(results.isEmpty())results.add("✨ No exact match • Explore similar rooms");String[] arr=results.toArray(new String[0]);new AlertDialog.Builder(this).setTitle("Results for “"+x+"”").setItems(arr,(d,w)->{String item=arr[w];if(item.startsWith("🎤 "))room(item.substring(3));else if(item.startsWith("👤 "))userProfile(item.substring(3));}).setNegativeButton("Close",null).show();});
        text("Try: Music, Friends, Game, KING",13,MUTED,false);button("Back",CARD,this::home);
    }

    private void createRoomDialog(){
        final EditText name=new EditText(this); name.setHint("Room name"); name.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("Create Party Room").setMessage("Create a local room preview. Online room sync will be connected with the backend.").setView(name).setNegativeButton("Cancel",null).setPositiveButton("Create",(d,w)->{String x=name.getText().toString().trim();if(x.isEmpty())x=displayName+"'s Room";if(!rooms.contains(x)) rooms.add(0,x); Set<String> savedRooms=new HashSet<>(rooms); savedRooms.remove("Music & Friends"); savedRooms.remove("KING Lounge"); savedRooms.remove("Game Talk"); getPreferences(0).edit().putStringSet("rooms",savedRooms).apply(); room(x);}).show();
    }

}
