Warning: truncated output (original token count: 21103)
Total output lines: 931

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
    …5103 tokens truncated…ds",this::settingsPage));
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
        int xp=getPreferences(0).getInt("xp",120); FirebaseUser profileUser=firebaseAuth==null?null:firebaseAuth.getCurrentUser(); String profileId=profileUser==null?"TEST "+roomId(displayName):profileUser.getUid(); if(profileId.length()>12)profileId=profileId.substring(0,12); TextView id=new TextView(this); id.setText("ID: "+profileId+"   ♢ Lv."+(xp/500+1)); id.setTextSize(13); id.setTextColor(0xff777777); id.setGravity(Gravity.CENTER); hero.addView(id);
        TextView bio=new TextView(this); bio.setText(getPreferences(0).getString("bio","Love music, games and new friends ✨")); bio.setTextSize(13); bio.setTextColor(0xff666666); bio.setGravity(Gravity.CENTER); bio.setPadding(dp(20),dp(4),dp(20),0); hero.addView(bio); root.addView(hero,new LinearLayout.LayoutParams(-1,dp(185)));
        int following=new HashSet<>(getPreferences(0).getStringSet("following",new HashSet<>())).size(); int friends=new HashSet<>(getPreferences(0).getStringSet("friends",new HashSet<>())).size();
        LinearLayout stats=new LinearLayout(this); stats.setGravity(Gravity.CENTER);
        String[] ss={"—\nFollowing","—\nFollowers","—\nFriends",(1200+xp)+"\nCharm"};
        TextView[] statViews=new TextView[4];
        for(int i=0;i<ss.length;i++){TextView x=new TextView(this);x.setText(ss[i]);x.setGravity(Gravity.CENTER);x.setTextSize(14);x.setTextColor(0xff333333);statViews[i]=x;stats.addView(x,new LinearLayout.LayoutParams(0,dp(64),1));}
        root.addView(stats);
        FirebaseUser signedUser=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
        if(signedUser!=null){
            try {
                com.google.firebase.firestore.FirebaseFirestore firestore=com.google.firebase.firestore.FirebaseFirestore.getInstance();
                String uid=signedUser.getUid();
                firestore.collection("follows").whereEqualTo("followerUid",uid).get().addOnSuccessListener(out->{
                    if(!"profile".equals(screen)||isFinishing())return;
                    java.util.Set<String> outgoing=new java.util.HashSet<>();
                    for(com.google.firebase.firestore.DocumentSnapshot d:out.getDocuments()){String targetId=d.getString("targetUid");if(targetId!=null)outgoing.add(targetId);}
                    statViews[0].setText(outgoing.size()+"\nFollowing");
                    firestore.collection("follows").whereEqualTo("targetUid",uid).get().addOnSuccessListener(in->{
                        if(!"profile".equals(screen)||isFinishing())return;
                        int mutual=0;
                        for(com.google.firebase.firestore.DocumentSnapshot d:in.getDocuments()){String followerId=d.getString("followerUid");if(followerId!=null&&outgoing.contains(followerId))mutual++;}
                        statViews[1].setText(in.size()+"\nFollowers");
                        statViews[2].setText(mutual+"\nFriends");
                    });
                });
            }catch(Exception ignored){}
        }else{
            statViews[0].setText(following+"\nFollowing");
            statViews[2].setText(friends+"\nFriends");
        }
        ScrollView sv=new ScrollView(this); LinearLayout list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); list.setPadding(dp(14),dp(5),dp(14),dp(14)); sv.addView(list);
        String hometown=getPreferences(0).getString("hometown",""); if(!hometown.isEmpty()) cardLine(list,"📍 "+hometown,"Hometown",null);
        cardLine(list,"👥 Live Followers & Following","Open Firebase people directory",()->startActivity(new Intent(this,SocialActivity.class))); cardLine(list,"💎 My Wallet","Coins, gifts and transaction history",this::walletPage); cardLine(list,"🎒 Backpack","Gifts, frames and room items",this::backpackPage); cardLine(list,"🏆 Level & Achievements","Level "+(xp/500+1)+" • "+xp+" XP",this::levelPage); cardLine(list,"👑 Rankings","Weekly stars and top gifters",this::rankingsPage); cardLine(list,"💠 VIP Center","Badges, frames and entrance effects",this::vipPage); cardLine(list,"🏠 Family & Agency","Family members, code sharing and host tools",this::familyAgencyPage); cardLine(list,"📅 Daily Check-in","Claim a daily reward and build streak",this::dailyCheckInPage); cardLine(list,"🏅 Missions","Daily activity missions and rewards",this::missionsPage); cardLine(list,"⚙ Settings","Account, privacy and notifications",this::settingsPage);
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
    @Override protected void onResume() { super.onResume(); if ("home".equals(screen) && homeRoomsListener == null) renderHome(selectedHomeCategory); }
    @Override protected void onPause() { super.onPause(); stopMic(); if(homeRoomsListener!=null){homeRoomsListener.remove();homeRoomsListener=null;} }
    @Override protected void onDestroy() { stopMic(); if(homeRoomsListener!=null){homeRoomsListener.remove();homeRoomsListener=null;} super.onDestroy(); }
    private void notificationsCenter(){
        screen="notifications"; base("Notifications","Activity from your KING Plus community");
        String raw=getPreferences(0).getString("notifications_log","");
        if(raw.isEmpty()) raw="No notifications yet";
        for(String n:raw.split("\\|\\|\\|")) if(!n.trim().isEmpty()) button(n,CARD,()->Toast.makeText(this,"Opened",Toast.LENGTH_SHORT).show());
        button("Mark all as read",PURPLE,()->{getPreferences(0).edit().putBoolean("notifications_read",true).apply();Toast.makeText(this,"All notifications marked as read",Toast.LENGTH_SHORT).show();});
        button("Clear notifications",CARD,()->{getPreferences(0).edit().putString("notifications_log","").apply();notificationsCenter();});
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
