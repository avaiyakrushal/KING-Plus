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
    private com.google.firebase.firestore.ListenerRegistration homeRoomsListener;
    private com.google.firebase.firestore.ListenerRegistration notificationListener;
    private com.google.firebase.firestore.ListenerRegistration familyListener;
    private com.google.firebase.firestore.ListenerRegistration familyMembersListener;
    private com.google.firebase.firestore.ListenerRegistration momentListener;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        installCrashReport();
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
        try { PushNotifications.initialize(this); if (firebaseAuth != null && firebaseAuth.getCurrentUser() != null) PushNotifications.requestPermission(this); } catch (Exception ignored) { }
        if (displayName.isEmpty()) login(); else home();
        String crash = getPreferences(0).getString("last_crash", "");
        if (!crash.isEmpty()) {
            getPreferences(0).edit().remove("last_crash").apply();
            new AlertDialog.Builder(this).setTitle("KING Plus crash details")
                .setMessage(crash).setPositiveButton("OK", null).show();
        }
    }
    private void installCrashReport() {
        final Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            try {
                String trace = android.util.Log.getStackTraceString(error);
                if (trace.length() > 3500) trace = trace.substring(0, 3500);
                getPreferences(0).edit().putString("last_crash", trace).commit();
            } catch (Throwable ignored) { }
            if (previous != null) previous.uncaughtException(thread, error);
            else android.os.Process.killProcess(android.os.Process.myPid());
        });
    }
    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
    private GradientDrawable background(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private void base(String title, String subtitle) {
        stopMic();
        if (notificationListener != null) { notificationListener.remove(); notificationListener = null; }
        if (familyListener != null) { familyListener.remove(); familyListener = null; }
        if (familyMembersListener != null) { familyMembersListener.remove(); familyMembersListener = null; }
        if (momentListener != null) { momentListener.remove(); momentListener = null; }
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
        screen = "login";
        stopMic();
        android.widget.FrameLayout root = new android.widget.FrameLayout(this);
        root.setBackgroundResource(R.drawable.login_background);
        ImageView diamonds = new ImageView(this);
        diamonds.setImageResource(R.drawable.diamond_art);
        diamonds.setAlpha(0.42f);
        android.widget.FrameLayout.LayoutParams art = new android.widget.FrameLayout.LayoutParams(dp(320), dp(320), Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        art.topMargin = dp(78);
        root.addView(diamonds, art);
        View shade = new View(this);
        shade.setBackgroundColor(0x55000015);
        root.addView(shade, new android.widget.FrameLayout.LayoutParams(-1, -1));
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(26), dp(55), dp(26), dp(32));
        scroll.addView(page);
        root.addView(scroll, new android.widget.FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.king_logo);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(110), dp(110));
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        lp.bottomMargin = dp(12);
        page.addView(logo, lp);
        TextView heading = text("KING Plus", 31, Color.WHITE, true);
        heading.setGravity(Gravity.CENTER);
        TextView subtitle = text("Login or create your account", 15, MUTED, false);
        subtitle.setGravity(Gravity.CENTER);
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
        displayName=name; getPreferences(0).edit().putString("name",name).putString("login_provider",provider).apply();
        if(firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null){PushNotifications.requestPermission(this);CloudSync.syncPublicProfile(name,getPreferences(0).getString("bio","Love music, games and new friends ✨"),getPreferences(0).getString("hometown",""),getPreferences(0).getString("birthday",""),getPreferences(0).getString("tags","Music, Games"),(ok,message)->{});}
        home();
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
        // Route all room discovery through PartyActivity, which reads live_rooms
        // from Firestore. The old inline screen displayed synthetic IDs, counts,
        // occupants, and chat as if they were live.
        Intent intent = new Intent(this, PartyActivity.class);
        intent.putExtra("roomSearchName", name == null ? "" : name.trim());
        startActivity(intent);
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
        String result;
        if(game.contains("Ludo")) result="Local practice table opened. Online players are not connected in this version.";
        else if(game.contains("Dart")) result="Practice mode opened. Score is for this device only.";
        else if(game.contains("Draw")) result="Practice preview only. No coins or prizes are awarded.";
        else if(game.contains("Racing")) result="Local racing practice preview. No live players are connected.";
        else if(game.contains("Snake")) result="Local practice round. This is not an online battle.";
        else result="Local practice preview. Multiplayer is not connected in this version.";
        new AlertDialog.Builder(this).setTitle(game).setMessage(result+"\n\nTEST mode • no wallet changes or rewards").setPositiveButton("Play again",(d,w)->playMiniGame(game)).setNegativeButton("Back",null).show();
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
        stopMic();
        startActivity(new Intent(this, InboxActivity.class));
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
        TextView mode=new TextView(this);mode.setText("Practice games • live player counts and multiplayer are not connected in this build");mode.setTextSize(13);mode.setTextColor(0xff77717f);mode.setPadding(dp(4),dp(4),dp(4),dp(10));list.addView(mode);
        cardLine(list,"🎲  Ludo","Practice table",()->playMiniGame("🎲 Ludo Party"));
        cardLine(list,"🐑  Sheep Fight","Practice round",()->playMiniGame("🐍 Snake Battle"));
        cardLine(list,"🗡  Knife Hit","Skill practice",()->playMiniGame("🎯 Lucky Dart"));
        cardLine(list,"⬡  Hexagon Fight","Practice arena",()->playMiniGame("🏁 Racing"));
        cardLine(list,"🃏  Rummy","Practice cards",()->playMiniGame("🎁 Lucky Draw"));
        cardLine(list,"♠  Spider Challenge","Solitaire practice",()->playMiniGame("🎯 Lucky Dart"));
        root.addView(sv,new LinearLayout.LayoutParams(-1,0,1)); addBottomNav(root,1); setContentView(root);
    }

    private void discover(){
        if(firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null){startActivity(new Intent(this,SocialActivity.class));return;}
        screen="discover"; stopMic(); LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(0xfff7f7fb);
        TextView h=new TextView(this); h.setText("Discover"); h.setTextSize(25); h.setTextColor(0xff171717); h.setTypeface(null,Typeface.BOLD); h.setPadding(dp(18),dp(18),0,dp(10)); root.addView(h,new LinearLayout.LayoutParams(-1,dp(66)));
        ScrollView sv=new ScrollView(this); LinearLayout list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); list.setPadding(dp(14),dp(8),dp(14),dp(18)); sv.addView(list);
        TextView note=new TextView(this);note.setText("Sign in to Firebase for live people, rooms and messages. Local preview activity is hidden here.");note.setTextSize(14);note.setTextColor(0xff77717f);note.setPadding(dp(8),dp(10),dp(8),dp(10));list.addView(note);
        cardLine(list,"👥  Discover people","Sign in to search real KING Plus accounts",()->startActivity(new Intent(this,SocialActivity.class)));
        cardLine(list,"🎤  Live Party rooms","Sign in to view rooms created by members",()->startActivity(new Intent(this,PartyActivity.class)));
        root.addView(sv,new LinearLayout.LayoutParams(-1,0,1)); addBottomNav(root,2); setContentView(root);
    }
    private void conversation(String who){
        Intent intent = new Intent(this, SocialActivity.class);
        intent.putExtra("searchQuery", who == null ? "" : who.trim());
        startActivity(intent);
    }

    private void momentsPage(){
        screen="moments"; base("Moments","Share updates with the KING Plus community");
        FirebaseUser signed=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
        button(signed==null?"＋ Create TEST/local post":"＋ Create live text post",PURPLE,()->{if(signed==null)composePost();else composeLiveMoment(signed);});
        text(signed==null?"TEST/local posts on this phone":"Live community feed",18,Color.WHITE,true);
        if(signed==null){showPosts();return;}
        final LinearLayout rows=new LinearLayout(this);rows.setOrientation(LinearLayout.VERTICAL);page.addView(rows);
        final TextView state=text("Loading live Moments…",14,MUTED,false);
        momentListener=com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("moments")
            .orderBy("createdAt",com.google.firebase.firestore.Query.Direction.DESCENDING).limit(50)
            .addSnapshotListener((snap,error)->{
                if(!"moments".equals(screen)||isFinishing()||isDestroyed())return;
                rows.removeAllViews();
                if(error!=null){state.setText("Live Moments could not be loaded. Check your connection.");return;}
                if(snap==null||snap.isEmpty()){state.setText("No community posts yet. Share the first one.");return;}
                state.setText("");
                for(com.google.firebase.firestore.DocumentSnapshot doc:snap.getDocuments()){
                    String author=doc.getString("name"),body=doc.getString("text");
                    LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(12),dp(8),dp(12),dp(8));card.setBackground(background(0xff251b32,14));
                    LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.setMargins(0,dp(4),0,dp(7));rows.addView(card,cp);
                    addText(card,"👤 "+(author==null?"KING Member":author),16,Color.WHITE,true);
                    if(body!=null&&!body.isEmpty())addText(card,body,15,Color.WHITE,false);
                    com.google.firebase.Timestamp created=doc.getTimestamp("createdAt");if(created!=null)addText(card,new java.text.SimpleDateFormat("dd MMM, HH:mm",java.util.Locale.getDefault()).format(created.toDate()),12,MUTED,false);
                    buttonIn(card,"♡ Like",CARD,()->toggleMomentLike(doc.getId(),signed.getUid()));
                }
            });
        button("Back to Discover",CARD,this::discover);
    }

    private void composeLiveMoment(FirebaseUser signed){
        final EditText body=new EditText(this);body.setHint("Write a Moment (max 1000 characters)…");body.setSingleLine(false);body.setMinLines(3);
        new AlertDialog.Builder(this).setTitle("Share a live Moment").setView(body).setNegativeButton("Cancel",null).setPositiveButton("Post",(d,w)->{
            String value=body.getText().toString().trim();if(value.isEmpty()||value.length()>1000){body.setError("Write 1–1000 characters");return;}
            java.util.Map<String,Object> post=new java.util.HashMap<>();post.put("uid",signed.getUid());post.put("name",displayName);post.put("text",value);post.put("createdAt",com.google.firebase.firestore.FieldValue.serverTimestamp());
            com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("moments").add(post)
                .addOnSuccessListener(ref->{addXp(5);Toast.makeText(this,"Moment posted to the community",Toast.LENGTH_SHORT).show();momentsPage();})
                .addOnFailureListener(e->Toast.makeText(this,"Moment not posted: "+e.getLocalizedMessage(),Toast.LENGTH_LONG).show());
        }).show();
    }

    private void toggleMomentLike(String momentId,String uid){
        com.google.firebase.firestore.DocumentReference like=com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("moments").document(momentId).collection("likes").document(uid);
        like.get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(doc->{
            if(doc.exists())like.delete().addOnFailureListener(e->Toast.makeText(this,"Like could not be removed",Toast.LENGTH_SHORT).show());
            else{java.util.Map<String,Object> value=new java.util.HashMap<>();value.put("uid",uid);value.put("createdAt",com.google.firebase.firestore.FieldValue.serverTimestamp());like.set(value).addOnFailureListener(e->Toast.makeText(this,"Like could not be saved",Toast.LENGTH_SHORT).show());}
        }).addOnFailureListener(e->Toast.makeText(this,"Live like unavailable",Toast.LENGTH_SHORT).show());
    }

    private void buttonIn(LinearLayout host,String label,int color,Runnable action){
        TextView b=new TextView(this);b.setText(label);b.setTextColor(Color.WHITE);b.setTextSize(14);b.setGravity(Gravity.CENTER);b.setPadding(dp(10),dp(8),dp(10),dp(8));b.setBackground(background(color,10));b.setOnClickListener(v->action.run());host.addView(b,new LinearLayout.LayoutParams(-1,dp(40)));
    }

    private void peoplePage(String title){
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, SocialActivity.class));
            return;
        }
        screen="people"; base(title,"KING Plus community");
        text("Sign in with Firebase to load real people. Preview accounts are hidden.",14,MUTED,false);
        button("Open live people directory",PURPLE,()->startActivity(new Intent(this,SocialActivity.class)));
        button("Back",PURPLE,this::messages);
    }

    private void userProfile(String who){
        conversation(who);
    }
    private void safetyOptions(String who){
        String[] a={"🚫 Block / Unblock","⚑ Report user"};
        new AlertDialog.Builder(this).setTitle(who).setItems(a,(d,w)->{if(w==0){Set<String>b=new HashSet<>(getPreferences(0).getStringSet("blocked",new HashSet<>()));boolean blocked;if(b.contains(who)){b.remove(who);blocked=false;}else{b.add(who);blocked=true;}getPreferences(0).edit().putStringSet("blocked",b).apply();Toast.makeText(this,blocked?"User blocked":"User unblocked",Toast.LENGTH_SHORT).show();}else reportDialog(who);}).show();
    }
    private void reportDialog(String who){
        String[] reasons={"Spam","Harassment","Inappropriate content","Fake account","Other"};
        new AlertDialog.Builder(this).setTitle("Report "+who).setItems(reasons,(d,w)->CloudSync.submitReport(this,who,reasons[w],(ok,message)->runOnUiThread(()->{
            if(ok){int count=getPreferences(0).getInt("reports",0)+1;getPreferences(0).edit().putInt("reports",count).apply();}
            Toast.makeText(this,message,Toast.LENGTH_LONG).show();
        }))).setNegativeButton("Cancel",null).show();
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
        button("Save changes",PURPLE,()->{String n=name.getText().toString().trim(); if(n.isEmpty()){name.setError("Name required");return;} String b=bio.getText().toString().trim(), h=hometown.getText().toString().trim(), bd=birthday.getText().toString().trim(), tg=tags.getText().toString().trim(); displayName=n; getPreferences(0).edit().putString("name",n).putString("bio",b).putString("hometown",h).putString("birthday",bd).putString("tags",tg).apply(); profile(); if(firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null)CloudSync.syncPublicProfile(n,b,h,bd,tg,(ok,message)->runOnUiThread(()->Toast.makeText(this,message,Toast.LENGTH_LONG).show())); else Toast.makeText(this,"Profile saved on this phone",Toast.LENGTH_SHORT).show();});
        button("Back",CARD,this::profile);
    }


    private void walletPage(){
        screen="wallet"; base("My Wallet","KING Plus balance");
        FirebaseUser signed=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
        if(signed==null){
            text("TEST wallet • local preview only",19,Color.WHITE,true);
            text("💎 "+coinBalance+" coins on this phone",15,MUTED,false);
            text("This balance is not live, transferable, or server-verified.",13,MUTED,false);
        }else{
            final TextView state=text("Checking server wallet…",18,Color.WHITE,true);
            com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("wallets").document(signed.getUid()).get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(doc->{if(!"wallet".equals(screen)||isFinishing()||isDestroyed())return;
                    if(!doc.exists()){state.setText("Server wallet is not initialized. No balance is confirmed.");return;}
                    Object coins=doc.get("coins");state.setText(coins instanceof Number?"💎 "+((Number)coins).longValue()+" server coins":"Server wallet balance is not available.");
                    Object score=doc.get("giftScore");if(score instanceof Number)text("🎁 Received gift score: "+((Number)score).longValue(),15,Color.WHITE,false);
                })
                .addOnFailureListener(error->{if("wallet".equals(screen)&&!isFinishing())state.setText("Server wallet unavailable. Balance was not verified.");});
        }
        button("Open Gift Catalog",PURPLE,this::giftCatalogPage);
        button("📜 Transaction History",CARD,this::transactionHistoryPage);
        button("📅 Daily Check-in",CARD,this::dailyCheckInPage);
        text("Paid recharge and payout stay disabled until verified Play Billing is configured.",13,MUTED,false);
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
        int xp=getPreferences(0).getInt("xp",0); FirebaseUser profileUser=firebaseAuth==null?null:firebaseAuth.getCurrentUser(); String profileId=profileUser==null?"TEST "+roomId(displayName):profileUser.getUid(); if(profileId.length()>12)profileId=profileId.substring(0,12); TextView id=new TextView(this); id.setText("ID: "+profileId+(profileUser==null?"   • TEST profile":"")); id.setTextSize(13); id.setTextColor(0xff777777); id.setGravity(Gravity.CENTER); hero.addView(id);
        TextView bio=new TextView(this); bio.setText(getPreferences(0).getString("bio","Love music, games and new friends ✨")); bio.setTextSize(13); bio.setTextColor(0xff666666); bio.setGravity(Gravity.CENTER); bio.setPadding(dp(20),dp(4),dp(20),0); hero.addView(bio); root.addView(hero,new LinearLayout.LayoutParams(-1,dp(185)));
        int following=new HashSet<>(getPreferences(0).getStringSet("following",new HashSet<>())).size(); int friends=new HashSet<>(getPreferences(0).getStringSet("friends",new HashSet<>())).size();
        LinearLayout stats=new LinearLayout(this); stats.setGravity(Gravity.CENTER);
        String[] ss={"Loading…\nFollowing","Loading…\nFollowers","Loading…\nFriends","—\nCharm"};
        TextView[] statViews=new TextView[4];
        for(int i=0;i<ss.length;i++){TextView x=new TextView(this);x.setText(ss[i]);x.setGravity(Gravity.CENTER);x.setTextSize(14);x.setTextColor(0xff333333);statViews[i]=x;stats.addView(x,new LinearLayout.LayoutParams(0,dp(64),1));}
        root.addView(stats);
        FirebaseUser signedUser=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
        if(signedUser!=null){
            try {
                com.google.firebase.firestore.FirebaseFirestore firestore=com.google.firebase.firestore.FirebaseFirestore.getInstance();
                String uid=signedUser.getUid();
                    firestore.collection("follows").whereEqualTo("followerUid",uid).get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(out->{
                    if(!"profile".equals(screen)||isFinishing())return;
                    java.util.Set<String> outgoing=new java.util.HashSet<>();
                    for(com.google.firebase.firestore.DocumentSnapshot d:out.getDocuments()){String targetId=d.getString("targetUid");if(targetId!=null)outgoing.add(targetId);}
                    statViews[0].setText(outgoing.size()+"\nFollowing");
                    firestore.collection("follows").whereEqualTo("targetUid",uid).get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(in->{
                        if(!"profile".equals(screen)||isFinishing())return;
                        int mutual=0;
                        for(com.google.firebase.firestore.DocumentSnapshot d:in.getDocuments()){String followerId=d.getString("followerUid");if(followerId!=null&&outgoing.contains(followerId))mutual++;}
                        statViews[1].setText(in.size()+"\nFollowers");
                        statViews[2].setText(mutual+"\nFriends");
                    }).addOnFailureListener(error->{if("profile".equals(screen)&&!isFinishing()){statViews[1].setText("—\nFollowers");statViews[2].setText("—\nFriends");}});
                }).addOnFailureListener(error->{if("profile".equals(screen)&&!isFinishing())statViews[0].setText("—\nFollowing");});
                firestore.collection("public_profiles").document(uid).get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(doc->{
                    if(!"profile".equals(screen)||isFinishing()||isDestroyed())return;
                    Object charm=doc.get("charm");statViews[3].setText(charm instanceof Number?((Number)charm).longValue()+"\nCharm":"—\nCharm");
                }).addOnFailureListener(error->{if("profile".equals(screen)&&!isFinishing())statViews[3].setText("—\nCharm");});
            }catch(Exception ignored){}
        }else{
            statViews[0].setText(following+"\nFollowing");
            statViews[2].setText(friends+"\nFriends");
            statViews[3].setText("—\nCharm");
        }
        ScrollView sv=new ScrollView(this); LinearLayout list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); list.setPadding(dp(14),dp(5),dp(14),dp(14)); sv.addView(list);
        String hometown=getPreferences(0).getString("hometown",""); if(!hometown.isEmpty()) cardLine(list,"📍 "+hometown,"Hometown",null);
        cardLine(list,"👥 Live Followers & Following","Open Firebase people directory",()->startActivity(new Intent(this,SocialActivity.class))); cardLine(list,"💎 My Wallet","Coins, gifts and transaction history",this::walletPage); cardLine(list,"🎒 Backpack","Gifts, frames and room items",this::backpackPage); cardLine(list,"🏆 Level & Achievements","Level "+(xp/500+1)+" • "+xp+" XP",this::levelPage); cardLine(list,"👑 Rankings","Weekly stars and top gifters",this::rankingsPage); cardLine(list,"💠 VIP Center","Badges, frames and entrance effects",this::vipPage); cardLine(list,"🏠 Family & Agency","Family members, code sharing and host tools",this::familyAgencyPage); cardLine(list,"📅 Daily Check-in","Claim a daily reward and build streak",this::dailyCheckInPage); cardLine(list,"🏅 Missions","Daily activity missions and rewards",this::missionsPage); cardLine(list,"⚙ Settings","Account, privacy and notifications",this::settingsPage);
        root.addView(sv,new LinearLayout.LayoutParams(-1,0,1)); addBottomNav(root,4); setContentView(root);
    }

    private void rankingsPage(){
        screen="rankings"; base("Rankings","Live leaderboard");
        text("Live rankings are not published by the server yet. No sample users or scores are shown.",15,MUTED,false);
        button("Back to Profile",CARD,this::profile);
    }
    private void vipPage(){
        screen="vip"; base("VIP Center","Verified membership status");
        FirebaseUser signed=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
        if(signed==null) text("Sign in to check entitlements. Preview badges do not grant VIP.",15,MUTED,false);
        else {
            final TextView state=text("Checking verified membership…",17,Color.WHITE,true);
            com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(signed.getUid()).get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(doc->{if(!"vip".equals(screen)||isFinishing()||isDestroyed())return;
                    Object level=doc.get("vipLevel");Object until=doc.get("vipUntil");
                    if(level instanceof Number&&((Number)level).intValue()>0&&until instanceof com.google.firebase.Timestamp&&((com.google.firebase.Timestamp)until).toDate().after(new java.util.Date())) state.setText("Verified VIP "+((Number)level).intValue()+" • active until "+new java.text.SimpleDateFormat("dd MMM yyyy",java.util.Locale.getDefault()).format(((com.google.firebase.Timestamp)until).toDate()));
                    else state.setText("No active server-verified VIP entitlement.");
                })
                .addOnFailureListener(error->{if("vip".equals(screen)&&!isFinishing())state.setText("Membership status could not be verified online.");});
        }
        text("VIP purchase and benefits remain locked until verified Play Billing and server entitlements are configured.",14,MUTED,false);
        button("Back to Profile",CARD,this::profile);
    }
    private void familyAgencyPage(){
        screen="family_agency"; base("Family & Agency","Live family membership");
        FirebaseUser user=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
        if(user==null){text("Sign in to create or join a live family. Preview members are not shown.",15,MUTED,false);button("Sign in",PURPLE,this::login);button("Back",CARD,this::profile);return;}
        final LinearLayout familyBox=new LinearLayout(this);familyBox.setOrientation(LinearLayout.VERTICAL);page.addView(familyBox);
        addText(familyBox,"Checking live family membership…",15,MUTED,false);
        com.google.firebase.firestore.FirebaseFirestore db=com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid()).get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(userDoc->{
            if(!"family_agency".equals(screen)||isFinishing()||isDestroyed())return;
            String code=userDoc.getString("familyCode");if(code==null||!code.matches("[0-9]{8}")){familyBox.removeAllViews();addText(familyBox,"No live family joined yet.",16,Color.WHITE,true);return;}
            listenFamily(code,user.getUid(),familyBox);
            button("Share Family Code",CARD,()->shareText("Join my KING Plus family • Code: "+code));
            button("Leave Family",0xffb23a48,()->leaveLiveFamily(code,user.getUid()));
        }).addOnFailureListener(error->{if("family_agency".equals(screen))addText(familyBox,"Family membership could not be checked online.",14,MUTED,false);});
        button("Create Family",PURPLE,()->createLiveFamily(user.getUid()));
        button("Join Family by Code",CARD,()->joinLiveFamily(user.getUid()));
        text("Host/agency applications and rankings require server approval; no local preview is counted as submitted.",13,MUTED,false);
        button("Back to Profile",CARD,this::profile);
    }

    private void listenFamily(String code,String uid,LinearLayout host){
        com.google.firebase.firestore.FirebaseFirestore db=com.google.firebase.firestore.FirebaseFirestore.getInstance();
        if(familyMembersListener!=null)familyMembersListener.remove();
        db.collection("families").document(code).get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(family->{
            if(!"family_agency".equals(screen)||isFinishing()||isDestroyed())return;
            if(!family.exists()){host.removeAllViews();addText(host,"Family is not available on the server.",14,MUTED,false);return;}
            host.removeAllViews();String name=family.getString("name");addText(host,"🏠 "+(name==null?"KING Family":name),22,Color.WHITE,true);addText(host,"Family code: "+code,16,0xffffd768,true);
            familyMembersListener=db.collection("families").document(code).collection("members").addSnapshotListener((members,memberError)->{
                if(!"family_agency".equals(screen)||isFinishing()||isDestroyed())return;
                if(memberError!=null||members==null){addText(host,"Family members could not be loaded.",13,MUTED,false);return;}
                int old=host.getChildCount();if(old>2)host.removeViews(2,old-2);
                addText(host,"Members • "+members.size(),17,Color.WHITE,true);
                for(com.google.firebase.firestore.DocumentSnapshot member:members.getDocuments())addText(host,("owner".equals(member.getString("role"))?"👑 ":"👤 ")+member.getString("name"),14,Color.WHITE,false);
            });
        }).addOnFailureListener(error->{if("family_agency".equals(screen)){host.removeAllViews();addText(host,"Family could not be checked online.",14,MUTED,false);}});
    }
    private void leaveLiveFamily(String code,String uid){
        new AlertDialog.Builder(this).setTitle("Leave family?").setMessage("Your live family membership will be removed.").setNegativeButton("Cancel",null).setPositiveButton("Leave",(d,w)->{
            com.google.firebase.firestore.FirebaseFirestore db=com.google.firebase.firestore.FirebaseFirestore.getInstance();
            com.google.firebase.firestore.DocumentReference family=db.collection("families").document(code);
            family.get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(doc->{if(uid.equals(doc.getString("ownerUid"))){Toast.makeText(this,"Family owner cannot leave until an owner-transfer option is added.",Toast.LENGTH_LONG).show();return;}
            family.collection("members").document(uid).delete().continueWithTask(task->{if(!task.isSuccessful())throw task.getException();return db.collection("users").document(uid).update("familyCode",com.google.firebase.firestore.FieldValue.delete());})
                .addOnSuccessListener(v->{Toast.makeText(this,"Left family",Toast.LENGTH_SHORT).show();familyAgencyPage();}).addOnFailureListener(e->Toast.makeText(this,"Could not leave family: "+e.getLocalizedMessage(),Toast.LENGTH_LONG).show());
            }).addOnFailureListener(e->Toast.makeText(this,"Family owner could not be verified. Nothing was changed.",Toast.LENGTH_LONG).show());
        }).show());
    }

    private void createLiveFamily(String uid){
        final EditText input=new EditText(this);input.setHint("Family name");
        new AlertDialog.Builder(this).setTitle("Create live family").setView(input).setNegativeButton("Cancel",null).setPositiveButton("Create",(d,w)->{
            String name=input.getText().toString().trim();if(name.isEmpty())name="KING Family";if(name.length()>120){input.setError("Maximum 120 characters");return;}
            String code=String.format(java.util.Locale.US,"%08d",java.util.concurrent.ThreadLocalRandom.current().nextInt(10000000,100000000));
            final String familyName=name,familyCode=code;
            com.google.firebase.firestore.FirebaseFirestore db=com.google.firebase.firestore.FirebaseFirestore.getInstance();com.google.firebase.firestore.DocumentReference ref=db.collection("families").document(code);
            java.util.Map<String,Object> data=new java.util.HashMap<>();data.put("code",code);data.put("name",name);data.put("ownerUid",uid);data.put("createdAt",com.google.firebase.firestore.FieldValue.serverTimestamp());
            db.runTransaction(tx->{com.google.firebase.firestore.DocumentSnapshot existing=tx.get(ref);if(existing.exists())throw new IllegalStateException("Code collision. Please try again.");tx.set(ref,data);return null;})
                .continueWithTask(task->{if(!task.isSuccessful())throw task.getException();java.util.Map<String,Object> member=new java.util.HashMap<>();member.put("uid",uid);member.put("name",displayName);member.put("role","owner");member.put("joinedAt",com.google.firebase.firestore.FieldValue.serverTimestamp());return ref.collection("members").document(uid).set(member);})
                .continueWithTask(task->{if(!task.isSuccessful())throw task.getException();return db.collection("users").document(uid).set(java.util.Collections.singletonMap("familyCode",familyCode),com.google.firebase.firestore.SetOptions.merge());})
                .addOnSuccessListener(v->{getPreferences(0).edit().putString("family_name",familyName).putString("family_code",familyCode).apply();Toast.makeText(this,"Family created on the server",Toast.LENGTH_SHORT).show();familyAgencyPage();})
                .addOnFailureListener(e->Toast.makeText(this,"Family could not be created: "+e.getLocalizedMessage(),Toast.LENGTH_LONG).show());
        }).show();
    }

    private void joinLiveFamily(String uid){
        final EditText input=new EditText(this);input.setHint("8-digit family code");input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(this).setTitle("Join live family").setView(input).setNegativeButton("Cancel",null).setPositiveButton("Join",(d,w)->{
            String code=input.getText().toString().trim();if(!code.matches("[0-9]{8}")){input.setError("Enter the 8-digit code");return;}
            com.google.firebase.firestore.FirebaseFirestore db=com.google.firebase.firestore.FirebaseFirestore.getInstance();com.google.firebase.firestore.DocumentReference ref=db.collection("families").document(code);
            ref.get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(family->{if(!family.exists()){Toast.makeText(this,"Family code not found",Toast.LENGTH_SHORT).show();return;}java.util.Map<String,Object> member=new java.util.HashMap<>();member.put("uid",uid);member.put("name",displayName);member.put("role","member");member.put("joinedAt",com.google.firebase.firestore.FieldValue.serverTimestamp());
                ref.collection("members").document(uid).set(member).continueWithTask(task->{if(!task.isSuccessful())throw task.getException();return db.collection("users").document(uid).set(java.util.Collections.singletonMap("familyCode",code),com.google.firebase.firestore.SetOptions.merge());})
                    .addOnSuccessListener(v->{getPreferences(0).edit().putString("family_name",family.getString("name")).putString("family_code",code).apply();Toast.makeText(this,"Joined family on the server",Toast.LENGTH_SHORT).show();familyAgencyPage();})
                    .addOnFailureListener(e->Toast.makeText(this,"Could not join family: "+e.getLocalizedMessage(),Toast.LENGTH_LONG).show());
            }).addOnFailureListener(e->Toast.makeText(this,"Family could not be checked online",Toast.LENGTH_SHORT).show());
        }).show();
    }

    private void dailyCheckInPage(){
        screen="checkin"; base("Daily Check-in","Daily reward status");
        FirebaseUser signed=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
        if(signed!=null){text("Daily rewards are not connected to a trusted server claim yet. No coins were added.",15,MUTED,false);button("Back to Wallet",PURPLE,this::walletPage);return;}
        text("TEST only • reward exists on this phone and is not transferable.",13,MUTED,false);
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
        TextView actions=new TextView(this);actions.setText("⌕    ⊕");actions.setTextSize(27);actions.setTextColor(0xff2b2730);actions.setGravity(Gravity.CENTER);actions.setOnClickListener(v->new AlertDialog.Builder(this).setItems(new String[]{"Search people","Create Party","Notifications"},(d,w)->{if(w==0)startActivity(new Intent(this,SocialActivity.class));else if(w==1)createRoomDialog();else notificationsCenter();}).show());top.addView(actions,new LinearLayout.LayoutParams(dp(105),dp(52)));root.addView(top);
        LinearLayout tabs=new LinearLayout(this);tabs.setPadding(dp(8),0,dp(8),dp(6));String[] cats={"Hot","Event","Date","Music","Game"};for(String c:cats){TextView t=new TextView(this);t.setText(c);t.setGravity(Gravity.CENTER);t.setTextSize(16);boolean active=c.equals(category);t.setTypeface(null,active?Typeface.BOLD:Typeface.NORMAL);t.setTextColor(active?0xff111111:0xff55525c);t.setBackground(active?background(0xffffe600,10):background(0xfff0eff4,10));LinearLayout.LayoutParams tlp=new LinearLayout.LayoutParams(0,dp(42),1);tlp.setMargins(dp(3),0,dp(3),0);tabs.addView(t,tlp);t.setOnClickListener(v->renderHome(c));}root.addView(tabs);
        ScrollView sv=new ScrollView(this);LinearLayout content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(12),dp(8),dp(12),dp(14));sv.addView(content);
        if("Hot".equals(category)){TextView banner=new TextView(this);banner.setText("👑  KING PLUS PARTY\n      Voice • Friends • Games");banner.setTextSize(20);banner.setTextColor(Color.WHITE);banner.setTypeface(null,Typeface.BOLD);banner.setGravity(Gravity.CENTER_VERTICAL);banner.setPadding(dp(18),0,dp(10),0);banner.setBackground(background(0xff7648e8,20));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(92));bp.setMargins(0,0,0,dp(12));content.addView(banner,bp);banner.setOnClickListener(v->startActivity(new Intent(this,PartyActivity.class)));}
        loadLiveHomeRooms(content);
        root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));addBottomNav(root,0);setContentView(root);
    }

    private void loadLiveHomeRooms(LinearLayout content) {
        if (homeRoomsListener != null) { homeRoomsListener.remove(); homeRoomsListener = null; }
        FirebaseUser signed = firebaseAuth == null ? null : firebaseAuth.getCurrentUser();
        if (signed == null) {
            TextView state = new TextView(this); state.setText("Sign in to discover live rooms. Local room previews are not shown as live."); state.setTextSize(14); state.setTextColor(0xff77717f); state.setPadding(dp(8),dp(12),dp(8),dp(12)); content.addView(state);
            return;
        }
        TextView heading = new TextView(this); heading.setText("Live rooms"); heading.setTextSize(18); heading.setTypeface(null,Typeface.BOLD); heading.setTextColor(0xff211d26); content.addView(heading);
        LinearLayout rows = new LinearLayout(this); rows.setOrientation(LinearLayout.VERTICAL); content.addView(rows);
        TextView state = new TextView(this); state.setText("Loading live rooms…"); state.setTextSize(13); state.setTextColor(0xff77717f); rows.addView(state);
        homeRoomsListener = com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("live_rooms")
            .orderBy("createdAt",com.google.firebase.firestore.Query.Direction.DESCENDING).limit(30)
            .addSnapshotListener((snap,error)->{
                if (!"home".equals(screen) || isFinishing() || isDestroyed()) return;
                rows.removeAllViews();
                if (error != null) { TextView e=new TextView(this);e.setText("Live rooms unavailable: "+(error.getLocalizedMessage()==null?"please retry":error.getLocalizedMessage()));e.setTextColor(0xff9b3434);e.setPadding(dp(8),dp(8),dp(8),dp(8));rows.addView(e);return; }
                int count=0;
                if (snap != null) for (com.google.firebase.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                    if (Boolean.TRUE.equals(doc.getBoolean("closed"))) continue;
                    String rawName=doc.getString("name"); final String name=rawName==null||rawName.trim().isEmpty()?"Live Party":rawName.trim();
                    String rawHost=doc.getString("ownerName"); final String host=rawHost==null||rawHost.trim().isEmpty()?"KING Host":rawHost.trim();
                    String id=doc.getId(), ownerUid=doc.getString("ownerUid");
                    LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(14),dp(10),dp(14),dp(10));card.setBackground(background(Color.WHITE,16));
                    TextView title=new TextView(this);title.setText("🎤  "+name);title.setTextSize(16);title.setTextColor(0xff211d26);title.setTypeface(null,Typeface.BOLD);card.addView(title);
                    TextView meta=new TextView(this);meta.setText("LIVE • Host: "+host);meta.setTextSize(12);meta.setTextColor(0xff77717f);card.addView(meta);
                    card.setOnClickListener(v->{Intent i=new Intent(this,PartyActivity.class);i.putExtra("roomId",id);i.putExtra("roomName",name);i.putExtra("ownerUid",ownerUid);i.putExtra("ownerName",host);startActivity(i);});
                    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(68));p.setMargins(0,dp(5),0,dp(5));rows.addView(card,p);count++;
                }
                if(count==0){TextView e=new TextView(this);e.setText("No live rooms yet. Create one from Party.");e.setTextColor(0xff77717f);e.setPadding(dp(8),dp(10),dp(8),dp(10));rows.addView(e);}
            });
    }
    private String safeKey(String v){return v==null?"none":v.replaceAll("[^A-Za-z0-9]","_");}
    private String roomId(String v){return String.valueOf(100000+Math.abs((v==null?0:v.hashCode())%900000));}
    private String seatAvatar(int n){String[] a={"🎵","🎮","💜","✨","🌸","🎧","🌙","👑","🎤","⭐","🌹","🎲"};return a[(n-1)%a.length];}
    private String seatLabel(int n){String[] names={"Lily","Alex","Mia","DJ Max","Queen","Sam","Gaur","Sona","Harsh","Music","Star","Guest"};return names[(n-1)%names.length];}
    private void appendRoomChat(String who,String message){
        String k="room_chat_"+safeKey(currentRoom);String old=getPreferences(0).getString(k,"");String line=who+"~~"+message.replace("\n"," ");String updated=old.isEmpty()?line:old+"|||"+line;String[] all=updated.split("\\|\\|\\|");if(all.length>40){StringBuilder b=new StringBuilder();for(int i=all.length-40;i<all.length;i++){if(b.length()>0)b.append("|||");b.append(all[i]);}updated=b.toString();}getPreferences(0).edit().putString(k,updated).apply();
    }
    private void loadRoomChat(LinearLayout chat){String raw=getPreferences(0).getString("room_chat_"+safeKey(currentRoom),"");if(raw.isEmpty()){addChat(chat,"Lily","Hello everyone ✨");addChat(chat,"Alex","Nice room! 🎵");return;}for(String line:raw.split("\\|\\|\\|")){String[] p=line.split("~~",2);if(p.length==2)addChat(chat,p[0],p[1]);}}
    private void sendGift(LinearLayout chat,String gift,int price,String target){if(firebaseAuth!=null && firebaseAuth.getCurrentUser()!=null){Toast.makeText(this,"Online gifts require a verified recipient and server wallet. Demo transfer blocked.",Toast.LENGTH_LONG).show();return;}if(!getPreferences(0).getBoolean("privacy_gifts",true)){Toast.makeText(this,"Room gifts are disabled in Privacy",Toast.LENGTH_SHORT).show();return;}if(coinBalance<price){Toast.makeText(this,"Not enough coins",Toast.LENGTH_SHORT).show();return;}coinBalance-=price;giftCount++;getPreferences(0).edit().putInt("coins",coinBalance).putInt("gift_count",giftCount).apply();String msg="sent "+gift+" to "+target;addChat(chat,displayName,msg);appendRoomChat(displayName,msg);appendTransaction("-"+price+" coins • "+gift+" → "+target);incrementMission("mission_gift",1);addXp(Math.min(20,Math.max(2,price/25)));if(giftEffectsEnabled)giftEffect(gift);}
    private void manageAdmins(){Set<String> admins=new HashSet<>(getPreferences(0).getStringSet("room_admins_"+safeKey(currentRoom),new HashSet<>()));String[] names={"Lily","Alex","Mia","DJ Max"};boolean[] checked=new boolean[names.length];for(int i=0;i<names.length;i++)checked[i]=admins.contains(names[i]);new AlertDialog.Builder(this).setTitle("Manage admins").setMultiChoiceItems(names,checked,(d,w,isChecked)->{if(isChecked)admins.add(names[w]);else admins.remove(names[w]);}).setPositiveButton("Save",(d,w)->{getPreferences(0).edit().putStringSet("room_admins_"+safeKey(currentRoom),admins).apply();Toast.makeText(this,"Admins updated",Toast.LENGTH_SHORT).show();}).setNegativeButton("Cancel",null).show();}
    private String todayKey(){return String.valueOf(System.currentTimeMillis()/86400000L);}
    private void incrementMission(String key,int amount){String k=key+"_"+todayKey();int v=getPreferences(0).getInt(k,0)+amount;getPreferences(0).edit().putInt(k,v).apply();}
    private int getMissionValue(String key){return getPreferences(0).getInt(key+"_"+todayKey(),0);}
    private void missionButton(String key,String title,int progress,int target,int reward){boolean done=progress>=target;boolean claimed=getPreferences(0).getBoolean("claimed_"+key+"_"+todayKey(),false);String label=title+"   "+Math.min(progress,target)+"/"+target+(claimed?"   ✓ Claimed":done?"   • Claim "+reward:" ");button(label,claimed?CARD:(done?PURPLE:CARD),()->{if(firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null){Toast.makeText(this,"Live mission rewards are not server-backed; no coins were added.",Toast.LENGTH_LONG).show();return;}if(!done){Toast.makeText(this,"Mission not complete yet",Toast.LENGTH_SHORT).show();return;}if(getPreferences(0).getBoolean("claimed_"+key+"_"+todayKey(),false)){Toast.makeText(this,"Already claimed",Toast.LENGTH_SHORT).show();return;}coinBalance=getPreferences(0).getInt("coins",2500)+reward;getPreferences(0).edit().putInt("coins",coinBalance).putBoolean("claimed_"+key+"_"+todayKey(),true).apply();appendTransaction("+"+reward+" coins • Mission reward");addXp(10);Toast.makeText(this,reward+" coins claimed",Toast.LENGTH_SHORT).show();missionsPage();});}
    private void addXp(int value){int xp=getPreferences(0).getInt("xp",120)+value;getPreferences(0).edit().putInt("xp",xp).apply();}
    private void appendTransaction(String line){String old=getPreferences(0).getString("transactions","");String stamp=new java.text.SimpleDateFormat("dd MMM HH:mm",java.util.Locale.getDefault()).format(new java.util.Date());String updated=stamp+" • "+line+(old.isEmpty()?"":"|||"+old);getPreferences(0).edit().putString("transactions",updated).apply();}
    private void transactionHistoryPage(){
        screen="transactions";base("Transaction History","Wallet activity");
        FirebaseUser signed=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
        if(signed==null){String raw=getPreferences(0).getString("transactions","");text(raw.isEmpty()?"No TEST transactions on this phone.":"TEST history only • not a server wallet ledger.",15,MUTED,false);if(!raw.isEmpty())for(String x:raw.split("\\|\\|\\|"))text(x,14,Color.WHITE,false);}
        else{
            final LinearLayout rows=new LinearLayout(this);rows.setOrientation(LinearLayout.VERTICAL);page.addView(rows);
            final TextView loading=text("Loading server ledger…",15,MUTED,false);
            com.google.firebase.firestore.FirebaseFirestore db=com.google.firebase.firestore.FirebaseFirestore.getInstance();
            java.util.Map<String,com.google.firebase.firestore.DocumentSnapshot> entries=new java.util.concurrent.ConcurrentHashMap<>();
            final int[] pending={2};final boolean[] failed={false};
            com.google.android.gms.tasks.OnSuccessListener<com.google.firebase.firestore.QuerySnapshot> success=snap->{for(com.google.firebase.firestore.DocumentSnapshot doc:snap.getDocuments())entries.put(doc.getId(),doc);if(--pending[0]==0){loading.setText("");renderLedgerRows(rows,entries,failed[0]);}};
            com.google.android.gms.tasks.OnFailureListener failure=error->{failed[0]=true;if(--pending[0]==0){loading.setText(entries.isEmpty()?"Server history is unavailable.":"Some server history could not be loaded.");renderLedgerRows(rows,entries,true);}};
            db.collection("wallet_ledger").whereEqualTo("senderUid",signed.getUid()).get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(success).addOnFailureListener(failure);
            db.collection("wallet_ledger").whereEqualTo("targetUid",signed.getUid()).get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(success).addOnFailureListener(failure);
        }
        button("Back to Wallet",PURPLE,this::walletPage);
    }
    private void renderLedgerRows(LinearLayout host,java.util.Map<String,com.google.firebase.firestore.DocumentSnapshot> entries,boolean failed){
        if(!"transactions".equals(screen)||isFinishing()||isDestroyed())return;host.removeAllViews();
        if(failed)addText(host,"Some server ledger rows could not be loaded; the list may be incomplete.",13,MUTED,false);
        if(entries.isEmpty()){addText(host,failed?"Server history is unavailable.":"No server wallet transactions yet.",15,MUTED,false);return;}
        java.util.List<com.google.firebase.firestore.DocumentSnapshot> ordered=new java.util.ArrayList<>(entries.values());
        java.util.Collections.sort(ordered,(a,b)->Long.compare(ledgerTime(b),ledgerTime(a)));
        for(com.google.firebase.firestore.DocumentSnapshot doc:ordered){
            FirebaseUser signed=firebaseAuth==null?null:firebaseAuth.getCurrentUser();String type=doc.getString("type"),gift=doc.getString("giftName"),sender=doc.getString("senderUid");Object cost=doc.get("cost");boolean sent=signed!=null&&sender!=null&&sender.equals(signed.getUid());
            String label=(sent?"Sent":"Received")+" • "+(gift==null?(type==null?"Wallet activity":type):gift)+(cost instanceof Number?" • "+((Number)cost).longValue()+" coins":"");
            com.google.firebase.Timestamp created=doc.getTimestamp("createdAt");if(created!=null)label+="\n"+new java.text.SimpleDateFormat("dd MMM yyyy, HH:mm",java.util.Locale.getDefault()).format(created.toDate());
            addText(host,label,14,Color.WHITE,false);
        }
    }
    private long ledgerTime(com.google.firebase.firestore.DocumentSnapshot doc){com.google.firebase.Timestamp t=doc.getTimestamp("createdAt");return t==null?0:t.toDate().getTime();}
    private TextView addText(LinearLayout host,String value,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,Typeface.BOLD);t.setPadding(dp(3),dp(10),dp(3),dp(12));host.addView(t);return t;}
    private void toggleSetting(String key,String label,Runnable refresh){boolean def=!"privacy_friends_dm".equals(key);boolean v=!getPreferences(0).getBoolean(key,def);getPreferences(0).edit().putBoolean(key,v).apply();Toast.makeText(this,label+": "+(v?"ON":"OFF"),Toast.LENGTH_SHORT).show();refresh.run();}
    private void shareText(String value){Intent send=new Intent(Intent.ACTION_SEND);send.setType("text/plain");send.putExtra(Intent.EXTRA_TEXT,value);startActivity(Intent.createChooser(send,"Share with"));}
    private void appendNotification(String value){String old=getPreferences(0).getString("notifications_log","");getPreferences(0).edit().putString("notifications_log",value+(old.isEmpty()?"":"|||"+old)).putBoolean("notifications_read",false).apply();}
    private void newMessageDialog(){final EditText e=new EditText(this);e.setHint("User name");new AlertDialog.Builder(this).setTitle("New message").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Open",(d,w)->{String n=e.getText().toString().trim();if(n.isEmpty())n="New Friend";conversation(n);}).show();}
    private void selectCosmetic(String key,String[] values,Runnable refresh){int selected=0;String current=getPreferences(0).getString(key,values[0]);for(int i=0;i<values.length;i++)if(values[i].equals(current))selected=i;new AlertDialog.Builder(this).setTitle("Choose item").setSingleChoiceItems(values,selected,(d,w)->{getPreferences(0).edit().putString(key,values[w]).apply();d.dismiss();refresh.run();}).setNegativeButton("Cancel",null).show();}

    private void helpCenterPage(){screen="help";base("Help & Feedback","KING Plus support center");button("Login & OTP help",CARD,()->new AlertDialog.Builder(this).setTitle("Login & OTP").setMessage("Use a real phone number with country code. Firebase Phone Authentication must be enabled and SHA fingerprints configured.").setPositiveButton("OK",null).show());button("Voice room help",CARD,()->new AlertDialog.Builder(this).setTitle("Voice rooms").setMessage("Join a seat, allow microphone permission, then tap Mic. Live multi-user audio still requires a real-time voice service/backend.").setPositiveButton("OK",null).show());button("Send feedback",PURPLE,()->{final EditText e=new EditText(this);e.setHint("Describe the issue or suggestion");new AlertDialog.Builder(this).setTitle("Feedback").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{String m=e.getText().toString().trim();if(!m.isEmpty()){String old=getPreferences(0).getString("feedback","");getPreferences(0).edit().putString("feedback",m+"|||"+old).apply();Toast.makeText(this,"Feedback saved",Toast.LENGTH_SHORT).show();}}).show();});button("Back to Settings",CARD,this::settingsPage);}

    @Override public void onBackPressed() {
        if ("login".equals(screen) || "home".equals(screen)) super.onBackPressed(); else home();
    }
    @Override protected void onResume() { super.onResume(); if ("home".equals(screen) && homeRoomsListener == null) renderHome(selectedHomeCategory); else if("notifications".equals(screen)&&notificationListener==null)notificationsCenter(); else if("family_agency".equals(screen)&&familyMembersListener==null)familyAgencyPage(); else if("moments".equals(screen)&&firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null&&momentListener==null)momentsPage(); }
    @Override protected void onPause() { super.onPause(); stopMic(); if(homeRoomsListener!=null){homeRoomsListener.remove();homeRoomsListener=null;} if(notificationListener!=null){notificationListener.remove();notificationListener=null;} if(familyListener!=null){familyListener.remove();familyListener=null;} if(familyMembersListener!=null){familyMembersListener.remove();familyMembersListener=null;} if(momentListener!=null){momentListener.remove();momentListener=null;} }
    @Override protected void onDestroy() { stopMic(); if(homeRoomsListener!=null){homeRoomsListener.remove();homeRoomsListener=null;} if(notificationListener!=null){notificationListener.remove();notificationListener=null;} if(familyListener!=null){familyListener.remove();familyListener=null;} if(familyMembersListener!=null){familyMembersListener.remove();familyMembersListener=null;} if(momentListener!=null){momentListener.remove();momentListener=null;} super.onDestroy(); }
    private void notificationsCenter(){
        screen="notifications"; base("Notifications","Live account activity");
        FirebaseUser signed=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
        if(signed==null){text("Sign in to load live alerts. Notifications on this phone are TEST/local only.",14,MUTED,false);String raw=getPreferences(0).getString("notifications_log","");if(raw.isEmpty())text("No local TEST notifications.",15,MUTED,false);else for(String n:raw.split("\\|\\|\\|"))if(!n.trim().isEmpty())text(n,14,Color.WHITE,false);button("Back to Messages",CARD,this::messages);return;}
        final LinearLayout rows=new LinearLayout(this);rows.setOrientation(LinearLayout.VERTICAL);page.addView(rows);
        final TextView state=text("Connecting to live notifications…",14,MUTED,false);
        notificationListener=com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("notifications").document(signed.getUid()).collection("items").orderBy("createdAt",com.google.firebase.firestore.Query.Direction.DESCENDING).limit(100)
            .addSnapshotListener((snap,error)->{
                if(!"notifications".equals(screen)||isFinishing()||isDestroyed())return;rows.removeAllViews();
                if(error!=null){state.setText("Live notifications unavailable. Check Firebase connection.");return;}
                state.setText("");if(snap==null||snap.isEmpty()){state.setText("No live notifications yet.");return;}
                for(com.google.firebase.firestore.DocumentSnapshot doc:snap.getDocuments()){
                    boolean read=Boolean.TRUE.equals(doc.getBoolean("read"));String title=doc.getString("title"),body=doc.getString("body");
                    String label=(read?"":"●  ")+(title==null?"KING Plus update":title)+(body==null||body.isEmpty()?"":"\n"+body);
                    cardLine(rows,label,read?"Read":"Unread",()->doc.getReference().update("read",true).addOnFailureListener(e->Toast.makeText(this,"Could not update notification",Toast.LENGTH_SHORT).show()));
                }
            });
        button("Mark all as read",PURPLE,()->com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("notifications").document(signed.getUid()).collection("items").whereEqualTo("read",false).limit(100).get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(snap->{com.google.firebase.firestore.WriteBatch batch=com.google.firebase.firestore.FirebaseFirestore.getInstance().batch();for(com.google.firebase.firestore.DocumentSnapshot doc:snap.getDocuments())batch.update(doc.getReference(),"read",true);batch.commit().addOnFailureListener(e->Toast.makeText(this,"Could not mark notifications as read",Toast.LENGTH_SHORT).show());}).addOnFailureListener(e->Toast.makeText(this,"Live notifications could not be updated",Toast.LENGTH_SHORT).show()));
        button("Back to Messages",CARD,this::messages);
    }

    private void searchCommunity(){
        final EditText query = input("Search people or live rooms…", "");
        new AlertDialog.Builder(this).setTitle("Search KING Plus").setView(query)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("People", (d,w) -> {
                String value = query.getText().toString().trim();
                if (value.isEmpty()) { Toast.makeText(this,"Enter a name to search",Toast.LENGTH_SHORT).show(); return; }
                Intent intent = new Intent(this, SocialActivity.class);
                intent.putExtra("searchQuery", value);
                startActivity(intent);
            })
            .setNeutralButton("Live rooms", (d,w) -> {
                String value = query.getText().toString().trim();
                if (value.isEmpty()) { Toast.makeText(this,"Enter a room name to search",Toast.LENGTH_SHORT).show(); return; }
                Intent intent = new Intent(this, PartyActivity.class);
                intent.putExtra("roomSearchName", value);
                startActivity(intent);
            }).show();
    }

    private void createRoomDialog(){
        if(firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null){startActivity(new Intent(this,PartyActivity.class));return;}
        new AlertDialog.Builder(this).setTitle("Sign in for live Party rooms").setMessage("Live room creation needs a Firebase account. Local previews are not saved as live rooms.").setNegativeButton("Close",null).setPositiveButton("Sign in",(d,w)->login()).show();
    }

}
