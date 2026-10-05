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
import android.widget.FrameLayout;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
    private FirebaseFirestore firestore;
    private TextView profileFollowersNumber, profileFollowingNumber, profileFriendsNumber, profileTopBalance, profileWalletCoins;
    private GoogleSignInClient googleSignInClient;
    private String phoneVerificationId;
    private boolean giftEffectsEnabled = true;
    private boolean muteAllSeats = false;
    private String selectedHomeCategory = "Hot";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        installCrashReport();
        // v5.2: do not seed fake/demo rooms. Real Party rooms come from Firebase.
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
            if (app != null) { firebaseAuth = FirebaseAuth.getInstance(); firestore = FirebaseFirestore.getInstance(); }
        } catch (Exception ignored) { firebaseAuth = null; }
        PushNotifications.initialize(this);
        if (firebaseAuth != null && firebaseAuth.getCurrentUser() != null) {
            FirebaseUser signedInUser = firebaseAuth.getCurrentUser();
            if (displayName.isEmpty()) {
                String restored = signedInUser.getDisplayName();
                if (restored == null || restored.trim().isEmpty()) {
                    String email = signedInUser.getEmail();
                    restored = email != null && email.contains("@") ? email.substring(0, email.indexOf('@')) : "KING " + publicId(signedInUser.getUid());
                }
                displayName = restored.trim();
            }
            SharedPreferences.Editor restoredIdentity = getPreferences(0).edit()
                .putString("name", displayName)
                .putString("login_provider", "Google/Firebase")
                .putString("firebase_uid", signedInUser.getUid());
            if (signedInUser.getEmail() != null) restoredIdentity.putString("google_email", signedInUser.getEmail());
            if (signedInUser.getPhotoUrl() != null) restoredIdentity.putString("google_photo_url", signedInUser.getPhotoUrl().toString());
            restoredIdentity.apply();
            PushNotifications.refreshToken();
            restoreCloudCosmeticsThenSync();
        }
        boolean forceLogin = getIntent() != null && getIntent().getBooleanExtra("forceLogin", false);
        if (forceLogin) login();
        else if (displayName.isEmpty()) login();
        else {
            int openTab = getIntent() == null ? 0 : getIntent().getIntExtra("openTab", 0);
            if (openTab == 1) games();
            else if (openTab == 2) discover();
            else if (openTab == 3) messages();
            else if (openTab == 4) profile();
            else home();
        }
        String crash = getPreferences(0).getString("last_crash", "");
        if (!crash.isEmpty()) {
            getPreferences(0).edit().remove("last_crash").apply();
            new AlertDialog.Builder(this).setTitle("KING Plus crash details")
                .setMessage(crash).setPositiveButton("OK", null).show();
        }
    }
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null && intent.getBooleanExtra("forceLogin", false)) { login(); return; }
        int tab = intent == null ? 0 : intent.getIntExtra("openTab", 0);
        if (tab == 1) games();
        else if (tab == 2) discover();
        else if (tab == 3) messages();
        else if (tab == 4) profile();
        else home();
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
    private void setSafeContentView(View root) {
        setContentView(root);
        final int l=root.getPaddingLeft(), t=root.getPaddingTop(), r=root.getPaddingRight(), b=root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root,(v,insets)->{
            Insets bars=insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(l,t+bars.top,r,b+bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }
    private void base(String title, String subtitle) {
        stopMic();
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(NAVY);
        page = new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(22), dp(30), dp(22), dp(36)); scroll.addView(page); setSafeContentView(scroll);
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
        setSafeContentView(root);
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
        button("G  Continue with Google / Gmail", 0xff4285f4, this::googleLogin);
        button("f  Continue with Facebook", 0xff1877f2, () -> socialProviderSetupRequired("Facebook"));
        button("📱  Continue with Mobile Number • TEST", PURPLE, this::mobileLogin);
        button("Terms & Privacy", CARD, this::termsPrivacyPage);
        button("Trouble logging in?", CARD, this::troubleLoginPage);
        text("Google / Gmail uses real Firebase Authentication. Mobile stays in FREE TEST mode with OTP 123456. Facebook still requires Meta provider setup.", 12, MUTED, false);
    }
    private void socialProviderSetupRequired(String provider) {
        new AlertDialog.Builder(this).setTitle(provider + " sign-in")
            .setMessage(provider + " login needs the provider App ID/secret and Firebase provider setup. No fake local login is used here.")
            .setPositiveButton("OK", null).show();
    }
    private void termsPrivacyPage() {
        screen = "terms";
        base("Terms & Privacy", "KING Plus test build");
        text("Terms", 20, Color.WHITE, true);
        text("Use KING Plus respectfully. Do not post illegal, abusive, deceptive or harmful content. Test coins, gifts, ranks and rewards have no cash value.", 15, MUTED, false);
        text("Privacy", 20, Color.WHITE, true);
        text("This test build stores local-mode chat and profile activity on this device. Firebase is used for enabled real-account cloud features.", 15, MUTED, false);
        button("Back to Login", PURPLE, this::login);
    }
    private void troubleLoginPage() {
        screen = "login_help";
        base("Trouble logging in?", "KING Plus sign-in help");
        text("Mobile • FREE TEST MODE", 18, Color.WHITE, true);
        text("Enter a valid mobile number and use OTP 123456. No SMS is sent and no billing is required.", 15, MUTED, false);
        text("Google", 18, Color.WHITE, true);
        text("Google error 10 means the installed APK signing SHA-1 is not registered for the Firebase/Google OAuth Android client.", 15, MUTED, false);
        text("Facebook", 18, Color.WHITE, true);
        text("Facebook sign-in stays disabled until Meta App credentials and the Firebase Facebook provider are configured.", 15, MUTED, false);
        button("Back to Login", PURPLE, this::login);
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
                .setMessage("Google OAuth client ID is missing from this build. Check app/google-services.json.")
                .setPositiveButton("OK", null).show();
            return;
        }
        String webClientId = getString(id);
        if (webClientId == null || webClientId.trim().isEmpty()) {
            new AlertDialog.Builder(this).setTitle("Google login setup")
                .setMessage("Google OAuth web client ID is empty in this build.")
                .setPositiveButton("OK", null).show();
            return;
        }
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .build();
        googleSignInClient = GoogleSignIn.getClient(this, options);
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            if (isFinishing() || isDestroyed()) return;
            startActivityForResult(googleSignInClient.getSignInIntent(), GOOGLE_SIGN_IN_REQUEST);
        });
    }
    private void firebaseAuthWithGoogle(String idToken, String fallbackName) {
        if (firebaseAuth == null || idToken == null || idToken.trim().isEmpty()) {
            Toast.makeText(this, "Google token is unavailable. Please try again.", Toast.LENGTH_LONG).show();
            return;
        }
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (!task.isSuccessful()) {
                String reason = task.getException() == null ? "Unknown Firebase error" : task.getException().getLocalizedMessage();
                new AlertDialog.Builder(this).setTitle("Google login failed")
                    .setMessage(reason == null ? "Firebase could not sign in this Google account." : reason)
                    .setPositiveButton("Try again", (d,w) -> googleLogin())
                    .setNegativeButton("Cancel", null).show();
                return;
            }
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Google account connected but Firebase user is unavailable.", Toast.LENGTH_LONG).show();
                return;
            }
            String name = user.getDisplayName();
            if (name == null || name.trim().isEmpty()) name = fallbackName;
            if ((name == null || name.trim().isEmpty()) && user.getEmail() != null && user.getEmail().contains("@"))
                name = user.getEmail().substring(0, user.getEmail().indexOf('@'));
            if (name == null || name.trim().isEmpty()) name = "KING User";
            displayName = name.trim();

            SharedPreferences.Editor e = getPreferences(0).edit()
                .putString("name", displayName)
                .putString("login_provider", "Google")
                .putString("firebase_uid", user.getUid());
            if (user.getEmail() != null) e.putString("google_email", user.getEmail());
            if (user.getPhotoUrl() != null) e.putString("google_photo_url", user.getPhotoUrl().toString());
            e.apply();

            try { PushNotifications.refreshToken(); } catch (Exception ignored) { }
            try { restoreCloudCosmeticsThenSync(); } catch (Exception ignored) { }
            try {
                CloudSync.syncProfileAndTestWallet(this, getPreferences(0), displayName,
                    coinBalance, giftCount, receivedGiftCount,
                    (ok,message) -> runOnUiThread(() -> {
                        if (!ok) Toast.makeText(this, "Google login successful • cloud sync pending", Toast.LENGTH_SHORT).show();
                    }));
            } catch (Exception ignored) { }
            Toast.makeText(this, "Google login successful", Toast.LENGTH_SHORT).show();
            home();
        });
    }
    private void mobileLogin() {
        final EditText phone = new EditText(this);
        phone.setHint("Mobile number, e.g. +919876543210");
        phone.setSingleLine(true);
        phone.setInputType(InputType.TYPE_CLASS_PHONE);
        new AlertDialog.Builder(this)
            .setTitle("Mobile login • FREE TEST MODE")
            .setMessage("No SMS will be sent. Enter any valid mobile number, then use OTP 123456.")
            .setView(phone)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Continue", (d,w) -> {
                String number = normalizePhone(phone.getText().toString());
                if (number == null) {
                    Toast.makeText(this, "Enter a valid mobile number", Toast.LENGTH_SHORT).show();
                    return;
                }
                showTestOtpDialog(number);
            }).show();
    }
    private void showTestOtpDialog(String number) {
        final EditText otp = new EditText(this);
        otp.setHint("Test OTP: 123456");
        otp.setSingleLine(true);
        otp.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        new AlertDialog.Builder(this)
            .setTitle("Verify • TEST MODE")
            .setMessage("No SMS was sent. Use test OTP 123456 for " + number + ".")
            .setView(otp)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Verify", (d,w) -> {
                String code = otp.getText().toString().trim();
                if (!"123456".equals(code)) {
                    Toast.makeText(this, "Wrong test OTP. Use 123456", Toast.LENGTH_LONG).show();
                    return;
                }
                String shortNumber = number.length() > 4 ? "KING " + number.substring(number.length() - 4) : "KING User";
                saveLocalSession(shortNumber, "Mobile Test");
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
        if (CloudSync.isSignedIn()) {
            PushNotifications.refreshToken();
            restoreCloudCosmeticsThenSync();
        }
        home();
    }

    private void restoreCloudCosmeticsThenSync() {
        if (firestore == null || firebaseAuth == null || firebaseAuth.getCurrentUser() == null) { syncPublicProfile(); return; }
        FirebaseUser me = firebaseAuth.getCurrentUser();
        firestore.collection("public_profiles").document(me.getUid()).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String frame = doc.getString("equippedFrame");
                    String effect = doc.getString("entranceEffect");
                    if (frame != null && !frame.trim().isEmpty()) KingCosmetics.setFrame(this, frame.trim());
                    if (effect != null && !effect.trim().isEmpty()) KingCosmetics.setEffect(this, effect.trim());
                }
                syncPublicProfile();
            })
            .addOnFailureListener(e -> syncPublicProfile());
    }

    private void syncPublicProfile() {
        if (firestore == null || firebaseAuth == null || firebaseAuth.getCurrentUser() == null) return;
        FirebaseUser me = firebaseAuth.getCurrentUser();
        String name = displayName;
        if (name == null || name.trim().isEmpty()) name = me.getDisplayName();
        if (name == null || name.trim().isEmpty()) name = "KING " + publicId(me.getUid());
        String bio = getPreferences(0).getString("bio","Love music, games and new friends ✨");
        String tags = getPreferences(0).getString("tags","Music, Games");
        Map<String,Object> profile = new HashMap<>();
        profile.put("uid", me.getUid());
        profile.put("displayName", name.trim());
        profile.put("searchName", name.trim().toLowerCase(java.util.Locale.US));
        profile.put("bio", bio == null ? "" : bio.trim());
        profile.put("tags", tags == null ? "" : tags.trim());
        profile.put("publicId", publicId(me.getUid()));
        profile.put("hometown", getPreferences(0).getString("hometown",""));
        profile.put("birthday", getPreferences(0).getString("birthday",""));
        LevelSystem.Snapshot progress=LevelSystem.read(this);
        profile.put("level",progress.level); profile.put("xp",progress.xp); profile.put("vipLevel",progress.vipLevel); profile.put("vipPoints",progress.vipPoints); profile.put("levelTier",LevelSystem.levelTier(progress.level));
        profile.put("equippedFrame",KingCosmetics.frame(this)); profile.put("entranceEffect",KingCosmetics.effect(this));
        profile.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
        firestore.collection("public_profiles").document(me.getUid()).set(profile, com.google.firebase.firestore.SetOptions.merge());
    }

    private String publicId(String firebaseUid) {
        if (firebaseUid == null || firebaseUid.isEmpty()) return "000000";
        long value = Integer.toUnsignedLong(firebaseUid.hashCode());
        return String.valueOf((value % 900000L) + 100000L);
    }
    private void openCommunityHub700(String tab) { Intent i=new Intent(this,CommunityHubActivity.class);i.putExtra("tab",tab);startActivity(i); }
    private void openCommunityTarget700(String tab,String uid,String name){Intent i=new Intent(this,CommunityHubActivity.class);i.putExtra("tab",tab);i.putExtra("targetUid",uid);i.putExtra("targetName",name);startActivity(i);}

    private void openPartyActivity() {
        KingNav.openRoot(this,0);
    }

    private void home() {
        // There is now only one Party UI in the app. MainActivity never renders a second Party page.
        openPartyActivity();
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
            if (data == null) {
                Toast.makeText(this, "Google sign-in cancelled", Toast.LENGTH_SHORT).show();
                return;
            }
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account == null || account.getIdToken() == null) {
                    Toast.makeText(this, "Google account did not return a Firebase token", Toast.LENGTH_LONG).show();
                    return;
                }
                String fallback = account.getDisplayName();
                if ((fallback == null || fallback.trim().isEmpty()) && account.getEmail() != null && account.getEmail().contains("@"))
                    fallback = account.getEmail().substring(0, account.getEmail().indexOf('@'));
                firebaseAuthWithGoogle(account.getIdToken(), fallback == null ? "Google User" : fallback);
            } catch (ApiException e) {
                int code = e.getStatusCode();
                String message;
                if (code == 10) message = "Google error 10: this APK signing SHA-1 is not registered for the Android OAuth client.";
                else if (code == 7) message = "Network error while contacting Google. Check internet and try again.";
                else if (code == 12501) message = "Google sign-in was cancelled.";
                else message = "Google sign-in failed. Error code: " + code;
                new AlertDialog.Builder(this).setTitle("Google sign-in")
                    .setMessage(message)
                    .setPositiveButton("Try again", (d,w) -> googleLogin())
                    .setNegativeButton("Close", null).show();
            }
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
        if (requestCode == 26 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri=data.getData();
            try {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                String old=getPreferences(0).getString("profile_videos","");
                String updated=old.isEmpty()?uri.toString():old+"|||"+uri.toString();
                getPreferences(0).edit().putString("profile_videos",updated).apply();
                Toast.makeText(this,"Video added to profile",Toast.LENGTH_SHORT).show();
                videoPage();
            } catch (Exception ex) { Toast.makeText(this,"Video could not be saved",Toast.LENGTH_SHORT).show(); }
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
        root.addView(controls); setSafeContentView(root);
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
        final String[] gameNames={"🎲 Ludo Master","🐑 Sheep Fight","🐺 Werewolf","🕵 Spy Game","🎨 Draw & Guess","🎱 Bingo","🦁 Crazy Zoo"};
        new AlertDialog.Builder(this).setTitle("🎮 Room Games").setItems(gameNames,(d,w)->{
            String raw=gameNames[w]; String game=raw.substring(raw.indexOf(' ')+1);
            kingGameModes(game);
        }).setNegativeButton("Close",null).show();
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
        screen="messages";
        stopMic();
        KingNav.openRoot(this,3);
    }

    private void addBottomNav(LinearLayout root,int selected){
        LinearLayout nav=new LinearLayout(this); nav.setGravity(Gravity.CENTER); nav.setBackgroundColor(Color.WHITE); String[] ni={"⌂\nParty","♟\nGame","◇\nDiscover","✉\nMessages","●\nMe"};
        for(int i=0;i<ni.length;i++){TextView n=new TextView(this);n.setText(ni[i]);n.setTextSize(12);n.setGravity(Gravity.CENTER);n.setTextColor(i==selected?0xffd2b100:0xff777777);final int k=i;n.setOnClickListener(v->{if(k==0)openPartyActivity();else if(k==1)games();else if(k==2)discover();else if(k==3)messages();else profile();});nav.addView(n,new LinearLayout.LayoutParams(0,dp(62),1));} root.addView(nav);
    }

    private void games(){
        incrementMission("mission_game",1);
        renderGamesCategory("Hot");
    }

    private void renderGamesCategory(String selected){
        screen="games"; stopMic();
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(0xfffbfafc);

        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(14),dp(4),dp(10),0);
        TextView gameTitle=new TextView(this);gameTitle.setText("Game");gameTitle.setTextSize(25);gameTitle.setTextColor(0xff171717);gameTitle.setTypeface(null,Typeface.BOLD);head.addView(gameTitle,new LinearLayout.LayoutParams(0,dp(54),1));
        TextView search=new TextView(this);search.setText("⌕");search.setTextSize(28);search.setGravity(Gravity.CENTER);search.setOnClickListener(v->showGameSearch());head.addView(search,new LinearLayout.LayoutParams(dp(46),dp(50)));
        TextView stats=new TextView(this);stats.setText("🔥");stats.setTextSize(23);stats.setGravity(Gravity.CENTER);stats.setOnClickListener(v->kingGameStats());head.addView(stats,new LinearLayout.LayoutParams(dp(44),dp(50)));root.addView(head);

        LinearLayout tabs=new LinearLayout(this);tabs.setPadding(dp(12),dp(3),dp(12),dp(8));String[] names={"Hot","LUDO","Party","Team"};
        for(String n:names){boolean active=n.equalsIgnoreCase(selected);TextView t=new TextView(this);t.setText(n);t.setTextSize(13);t.setTypeface(null,active?Typeface.BOLD:Typeface.NORMAL);t.setGravity(Gravity.CENTER);t.setTextColor(active?0xff19151e:0xff6c6671);t.setBackground(background(active?0xffffec00:0xfff7f7f7,14));t.setOnClickListener(v->renderGamesCategory(n));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(34),1);lp.setMargins(dp(3),0,dp(3),0);tabs.addView(t,lp);}root.addView(tabs,new LinearLayout.LayoutParams(-1,dp(46)));

        ScrollView sv=new ScrollView(this);LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);list.setPadding(dp(10),dp(2),dp(10),dp(16));sv.addView(list);
        TextView section=new TextView(this);section.setText("For You");section.setTextSize(15);section.setTextColor(0xff222222);section.setTypeface(null,Typeface.BOLD);section.setPadding(dp(5),dp(8),0,dp(8));list.addView(section);
        addLudoHeroV600(list);
        if("LUDO".equalsIgnoreCase(selected)){
            addGameGridRow(list,new String[][]{{"🎲","Ludo Master","Classic • Quick Match","🎲 Ludo Master"},{"👥","Ludo Team","Team room • Friends","🎲 Ludo Master"}});
        }else if("Party".equalsIgnoreCase(selected)){
            addGameGridRow(list,new String[][]{{"🐺","Werewolf","Voice role game","🐺 Werewolf"},{"🕵","Spy Game","Find the hidden spy","🕵 Spy Game"}});
            addGameGridRow(list,new String[][]{{"🎨","Draw & Guess","Draw • Guess • Laugh","🎨 Draw & Guess"},{"🎱","Bingo","Fast party rounds","🎱 Bingo"},{"🁣","Domino","Match the chain","🁣 Domino"}});
        }else if("Team".equalsIgnoreCase(selected)){
            addGameGridRow(list,new String[][]{{"🐑","Sheep Fight","1v1 • Team battle","🐑 Sheep Fight"},{"🦁","Crazy Zoo","Collect • Team fun","🦁 Crazy Zoo"}});
            cardLine(list,"🎤 Voice Game Room","Create or join a real Firebase room",()->kingOpenGameRoom("Game Room"));
        }else{
            addGameGridRow(list,new String[][]{{"🎲","Ludo Master","Quick match","🎲 Ludo Master"},{"🐑","Sheep Fight","Battle","🐑 Sheep Fight"},{"🐺","Werewolf","Voice roles","🐺 Werewolf"}});
            addGameGridRow(list,new String[][]{{"🎨","Draw & Guess","Drawing","🎨 Draw & Guess"},{"🎱","Bingo","Number rounds","🎱 Bingo"},{"🕵","Spy Game","Secret clues","🕵 Spy Game"}});
            TextView featured=new TextView(this);featured.setText("Featured Games");featured.setTextSize(15);featured.setTypeface(null,Typeface.BOLD);featured.setTextColor(0xff222222);featured.setPadding(dp(5),dp(12),0,dp(8));list.addView(featured);
            addGameGridRow(list,new String[][]{{"🦁","Crazy Zoo","Collect & play","🦁 Crazy Zoo"},{"🁣","Domino","Playable matching","🁣 Domino"},{"🎤","Voice Room","Play & talk","Game Room"}});
        }
        LinearLayout shortcuts=new LinearLayout(this);shortcuts.setGravity(Gravity.CENTER);shortcuts.setPadding(0,dp(8),0,dp(4));
        addGameShortcut(shortcuts,"📜\nHistory",this::kingGameHistory);addGameShortcut(shortcuts,"⭐\nStats",this::kingGameStats);addGameShortcut(shortcuts,"🎤\nRoom",()->kingOpenGameRoom("Game Room"));list.addView(shortcuts,new LinearLayout.LayoutParams(-1,dp(72)));
        root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));addBottomNav(root,1);setSafeContentView(root);
    }

    private void addLudoHeroV600(LinearLayout host){
        LinearLayout hero=new LinearLayout(this);hero.setOrientation(LinearLayout.VERTICAL);hero.setPadding(dp(16),dp(14),dp(16),dp(12));hero.setBackground(background(0xffdff4ff,16));hero.setOnClickListener(v->openPlayableGame("ludo"));TextView title=new TextView(this);title.setText("🎲  LUDO LORD");title.setTextSize(28);title.setTypeface(null,Typeface.BOLD);title.setTextColor(0xfff0a400);title.setGravity(Gravity.CENTER);hero.addView(title,new LinearLayout.LayoutParams(-1,dp(46)));TextView sub=new TextView(this);sub.setText("The Christmas feature is online, tap to join now!");sub.setTextSize(11);sub.setTextColor(0xff687984);sub.setGravity(Gravity.CENTER);hero.addView(sub,new LinearLayout.LayoutParams(-1,dp(28)));LinearLayout quick=new LinearLayout(this);quick.setGravity(Gravity.CENTER);String[] q={"1 vs 1","ONLINE","SKIN","EVENTS"};for(String x:q){TextView b=new TextView(this);b.setText(x);b.setTextSize(11);b.setTypeface(null,Typeface.BOLD);b.setTextColor(0xff3e5460);b.setGravity(Gravity.CENTER);b.setBackground(background(Color.WHITE,9));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(46),1);lp.setMargins(dp(3),0,dp(3),0);quick.addView(b,lp);}hero.addView(quick,new LinearLayout.LayoutParams(-1,dp(50)));LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,dp(142));hp.setMargins(0,0,0,dp(12));host.addView(hero,hp);TextView best=new TextView(this);best.setText("Best Game Collections");best.setTextSize(15);best.setTypeface(null,Typeface.BOLD);best.setTextColor(0xff222222);best.setPadding(dp(5),dp(4),0,dp(7));host.addView(best,new LinearLayout.LayoutParams(-1,dp(38)));
    }

    private void addGameHero(LinearLayout host,String selected){
        LinearLayout hero=new LinearLayout(this);hero.setOrientation(LinearLayout.VERTICAL);hero.setGravity(Gravity.CENTER_VERTICAL);hero.setPadding(dp(18),dp(12),dp(14),dp(12));hero.setBackground(background(0xff7547e8,20));
        TextView a=new TextView(this);a.setText("🎮  KING Plus Games");a.setTextSize(21);a.setTextColor(Color.WHITE);a.setTypeface(null,Typeface.BOLD);hero.addView(a);
        TextView b=new TextView(this);b.setText("Play • Voice • Friends • Team");b.setTextSize(13);b.setTextColor(0xffeee7ff);hero.addView(b);
        LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,dp(92));hp.setMargins(0,0,0,dp(10));host.addView(hero,hp);
    }

    private void addGameGridRow(LinearLayout host,String[][] games){
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.TOP);
        for(String[] g:games)addGameGridCard(row,g[0],g[1],g[2],g[3]);
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(142));rp.setMargins(0,dp(4),0,dp(4));host.addView(row,rp);
    }

    private void addGameGridCard(LinearLayout row,String icon,String title,String sub,String game){
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(4),dp(3),dp(4),dp(3));card.setBackground(background(0xffffffff,18));
        TextView art=new TextView(this);art.setText(icon);art.setTextSize(43);art.setGravity(Gravity.CENTER);int[] palette={0xff39a7d8,0xffab5ed9,0xffed7b6b,0xff54b9a7,0xffd4a447};int tint=palette[(title.hashCode()&0x7fffffff)%palette.length];android.graphics.drawable.GradientDrawable artBg=new android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.TL_BR,new int[]{tint,0xfff2dffc});artBg.setCornerRadius(dp(14));art.setBackground(artBg);card.addView(art,new LinearLayout.LayoutParams(-1,dp(86)));
        TextView t=new TextView(this);t.setText(title);t.setTextSize(12);t.setSingleLine(true);t.setEllipsize(android.text.TextUtils.TruncateAt.END);t.setTextColor(0xff1c1820);t.setTypeface(null,Typeface.BOLD);t.setPadding(0,dp(7),0,0);card.addView(t);
        TextView s=new TextView(this);s.setText(sub);s.setTextSize(11);s.setTextColor(0xff7e7783);card.addView(s);
        card.setOnClickListener(v->{if("Game Room".equals(game))kingOpenGameRoom(game);else openPlayableGame(playableCode730(game));});LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-1,1);cp.setMargins(dp(4),0,dp(4),0);row.addView(card,cp);
    }

    private void addGameShortcut(LinearLayout host,String text,Runnable action){TextView x=new TextView(this);x.setText(text);x.setTextSize(12);x.setTextColor(0xff5f5866);x.setGravity(Gravity.CENTER);x.setBackground(background(0xfff0eef3,15));x.setOnClickListener(v->action.run());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(58),1);p.setMargins(dp(4),0,dp(4),0);host.addView(x,p);}

    private void showGameSearch(){
        String[] games={"🎲 Ludo","🎰 Lucky Slot","🪙 Coin Toss","✊ Rock Paper Scissors","🎲 Dice Duel","🔢 Guess Number","🧠 Memory Match","⚡ Reaction Tap","🃏 High / Low","🎡 Lucky Wheel","🐑 Sheep Fight","🐺 Werewolf","🕵 Spy Game","🎨 Draw & Guess","🎱 Bingo","🦁 Crazy Zoo","🁣 Domino","🎤 Voice Game Room"};
        new AlertDialog.Builder(this).setTitle("Playable Games").setItems(games,(d,w)->{if(w==games.length-1)kingOpenGameRoom("Game Room");else openPlayableGame(new String[]{"ludo","slot","coin","rps","dice","guess","memory","reaction","highlow","wheel","sheep","werewolf","spy","draw","bingo","zoo","domino"}[w]);}).setNegativeButton("Close",null).show();
    }
    private void openPlayableGame(String game){if("ludo".equalsIgnoreCase(game)){startActivity(new Intent(this,OnlineLudoActivity.class));return;}Intent i=new Intent(this,GamePlayActivity.class);i.putExtra("game",game);startActivity(i);}
    private String playableCode730(String game){String g=game==null?"":game.toLowerCase(java.util.Locale.US);if(g.contains("ludo"))return "ludo";if(g.contains("sheep"))return "sheep";if(g.contains("werewolf"))return "werewolf";if(g.contains("spy"))return "spy";if(g.contains("draw"))return "draw";if(g.contains("bingo"))return "bingo";if(g.contains("zoo"))return "zoo";if(g.contains("domino"))return "domino";if(g.contains("slot"))return "slot";return "dice";}
    private void textInto(LinearLayout host,String value,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,Typeface.BOLD);t.setPadding(dp(10),dp(12),dp(10),dp(12));host.addView(t,new LinearLayout.LayoutParams(-1,-2));}

    private void kingGameCard(LinearLayout host,String title,String sub,String game){
        cardLine(host,title,sub,()->openPlayableGame(playableCode730(game)));
    }

    private void kingGameModes(String game){
        final String[] modes={"⚡ Quick Match","⚔ 1 vs 1","👥 Team Match","🔗 Play with Friends","🎤 Voice Game Room","📜 Rules"};
        new AlertDialog.Builder(this).setTitle(game).setItems(modes,(d,w)->{
            if(w==0)kingStartGame(game,"Quick Match");
            else if(w==1)kingStartGame(game,"1 vs 1");
            else if(w==2)kingStartGame(game,"Team Match");
            else if(w==3)kingInviteGame(game);
            else if(w==4)kingOpenGameRoom(game);
            else kingGameRules(game);
        }).setNegativeButton("Close",null).show();
    }

    private void kingStartGame(String game,String mode){
        String result; int points; boolean win;
        if(game.contains("Ludo")){
            int you=1+(int)(Math.random()*6), rival=1+(int)(Math.random()*6); win=you>=rival; points=win?20:6;
            result="You rolled "+you+" • Rival "+rival+" • "+(win?"WIN":"ROUND LOST");
        } else if(game.contains("Sheep")){
            int you=40+(int)(Math.random()*61), rival=40+(int)(Math.random()*61); win=you>=rival; points=win?18:5;
            result="Power "+you+" vs "+rival+" • "+(win?"WIN":"ROUND LOST");
        } else if(game.contains("Werewolf")){
            String[] roles={"Villager","Werewolf","Seer","Doctor"}; String role=roles[(int)(Math.random()*roles.length)]; win=Math.random()>.45; points=win?22:8;
            result="Private role: "+role+" • "+(win?"Your side survived":"Round finished");
        } else if(game.contains("Spy")){
            String[] words={"Beach","Cinema","Airport","School","Market","Hotel"}; String word=words[(int)(Math.random()*words.length)]; win=Math.random()>.45; points=win?20:7;
            result="Secret clue: "+word+" • "+(win?"Spy round cleared":"Spy escaped");
        } else if(game.contains("Draw")){
            String[] words={"Crown","Tiger","Diamond","Guitar","Rocket","Mango"}; String word=words[(int)(Math.random()*words.length)]; win=true; points=15;
            result="Draw this word: "+word+" • Share clues with friends";
        } else if(game.contains("Bingo")){
            int a=1+(int)(Math.random()*25), b=26+(int)(Math.random()*25), c=51+(int)(Math.random()*25); win=Math.random()>.5; points=win?18:6;
            result="Draw: "+a+", "+b+", "+c+" • "+(win?"BINGO!":"Keep playing");
        } else if(game.contains("Zoo")){
            String[] animals={"Lion","Panda","Elephant","Tiger","Fox","Peacock"}; String animal=animals[(int)(Math.random()*animals.length)]; win=true; points=10+(int)(Math.random()*11);
            result="You found: "+animal+" • Collection +1";
        } else {
            win=Math.random()>.5; points=win?15:5; result=win?"WIN":"ROUND COMPLETE";
        }
        kingRecordGame(game,mode,result,points,win);
        final String finalResult=result; final int finalPoints=points;
        new AlertDialog.Builder(this).setTitle(game+" • "+mode)
            .setMessage(finalResult+"\n\n+"+finalPoints+" Game Points\nNo real-money purchase or billing is used.")
            .setPositiveButton("Play Again",(d,w)->kingStartGame(game,mode))
            .setNeutralButton("Voice Room",(d,w)->kingOpenGameRoom(game))
            .setNegativeButton("Done",null).show();
    }

    private void kingRecordGame(String game,String mode,String result,int points,boolean win){
        SharedPreferences p=getPreferences(0);
        int matches=p.getInt("king_game_matches",0)+1;
        int wins=p.getInt("king_game_wins",0)+(win?1:0);
        int total=p.getInt("king_game_points",0)+Math.max(0,points);
        String old=p.getString("king_game_history","");
        String row="#"+matches+" • "+game+" • "+mode+" • "+result+" • +"+points+" GP";
        String history=row+(old.isEmpty()?"":"\n"+old);
        if(history.length()>7000)history=history.substring(0,7000);
        p.edit().putInt("king_game_matches",matches).putInt("king_game_wins",wins).putInt("king_game_points",total).putString("king_game_history",history).apply();
    }

    private void kingGameHistory(){
        screen="game_history"; base("Game History","KING Plus local/test match history");
        String history=getPreferences(0).getString("king_game_history","");
        if(history.isEmpty())text("No matches played yet.",15,MUTED,false);
        else{
            String[] rows=history.split("\\n");
            for(int i=0;i<Math.min(rows.length,15);i++)if(!rows[i].trim().isEmpty())text(rows[i],13,MUTED,false);
        }
        button("Back to Games",PURPLE,this::games);
    }

    private void kingGameStats(){
        screen="game_stats"; base("Game Stats","KING Plus no-billing game progress");
        SharedPreferences p=getPreferences(0); int matches=p.getInt("king_game_matches",0), wins=p.getInt("king_game_wins",0), points=p.getInt("king_game_points",0);
        int rate=matches==0?0:(wins*100/matches);
        text("🎮 Matches: "+matches,20,Color.WHITE,true);
        text("🏆 Wins: "+wins+" • Win rate: "+rate+"%",18,Color.WHITE,true);
        text("⭐ Game Points: "+points,20,0xffffd768,true);
        text("Game Points are local/test progression only and are not purchased coins or cash value.",13,MUTED,false);
        button("Match History",CARD,this::kingGameHistory);
        button("Back to Games",PURPLE,this::games);
    }

    private void kingGameRules(String game){
        String rules;
        if(game.contains("Ludo"))rules="Roll the dice and race your pieces toward the finish. KING Plus test rounds use a compact dice challenge; live friends can coordinate in a voice room.";
        else if(game.contains("Sheep"))rules="Choose quick, 1v1 or team play. Higher battle power wins the compact test round.";
        else if(game.contains("Werewolf"))rules="Players receive hidden roles. Villagers find the werewolf while special roles help the village. Use a voice room for group discussion.";
        else if(game.contains("Spy"))rules="Most players share a location clue while one player is the spy. Ask questions and identify the spy before the round ends.";
        else if(game.contains("Draw"))rules="One player gets a word to draw while friends try to guess it. Use Play with Friends or a voice game room for a group round.";
        else if(game.contains("Bingo"))rules="Mark called numbers. Complete the target pattern before the other players.";
        else if(game.contains("Zoo"))rules="Collect animals through casual rounds and build your local test collection.";
        else rules="Use KING Plus voice rooms to coordinate with your team. Third-party game software is not embedded in KING Plus.";
        new AlertDialog.Builder(this).setTitle(game+" • Rules").setMessage(rules).setPositiveButton("OK",null).show();
    }

    private void kingInviteGame(String game){
        Intent share=new Intent(Intent.ACTION_SEND); share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT,"Join me in KING Plus • "+game+" • open the Game tab and choose Play with Friends.");
        try{startActivity(Intent.createChooser(share,"Invite friends"));}catch(Exception e){Toast.makeText(this,"Share unavailable",Toast.LENGTH_SHORT).show();}
    }

    private void kingOpenGameRoom(String game){
        Intent i=new Intent(this,PartyActivity.class); i.putExtra("displayName",displayName); i.putExtra("requestedGame",game); startActivity(i);
        Toast.makeText(this,"Party center opened • create/join a Game room for "+game,Toast.LENGTH_LONG).show();
    }

    private void discover(){
        screen="discover";
        int gameScore=Math.max(0,getPreferences(0).getInt("king_game_points",0)+getPreferences(0).getInt("king_game_wins",0)*50+getPreferences(0).getInt("king_game_matches",0)*20);
        int chatScore=Math.max(0,getMissionValue("mission_chat")*20+getPreferences(0).getInt("friends_count",0)*10);
        int eventScore=Math.max(0,getMissionValue("mission_room")*25+getMissionValue("mission_profile")*5+giftCount*10+receivedGiftCount*10);
        Intent i=new Intent(this,DiscoverActivity.class);
        i.putExtra("displayName",displayName);i.putExtra("gameScore",gameScore);i.putExtra("chatScore",chatScore);i.putExtra("eventScore",eventScore);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i); finish();
    }
    private void conversation(String who){
        Intent i = new Intent(this, ChatActivity.class);
        i.putExtra("peerName", who);
        startActivity(i);
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
        // Use the Firebase-backed social screen instead of seeding fake people.
        startActivity(new Intent(this,SocialActivity.class));
    }

    private void userProfile(String who){
        incrementMission("mission_profile",1);screen="user_profile";
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.WHITE);
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(10),dp(4),dp(8),0);TextView back=new TextView(this);back.setText("‹");back.setTextSize(34);back.setTextColor(0xff222222);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->peoplePage("Discover People"));head.addView(back,new LinearLayout.LayoutParams(dp(48),dp(52)));TextView ttl=new TextView(this);ttl.setText("");head.addView(ttl,new LinearLayout.LayoutParams(0,dp(52),1));TextView share=new TextView(this);share.setText("↗");share.setTextSize(22);share.setGravity(Gravity.CENTER);share.setOnClickListener(v->shareText("Find "+who+" on KING Plus"));head.addView(share,new LinearLayout.LayoutParams(dp(46),dp(52)));root.addView(head);
        ScrollView sv=new ScrollView(this);LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);sv.addView(body);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        FrameLayout stage=new FrameLayout(this);stage.setBackgroundColor(0xfff1f1f1);TextView avatars=new TextView(this);avatars.setText("🧍‍♀️   🧍   🧍‍♂️");avatars.setTextSize(52);avatars.setGravity(Gravity.CENTER);avatars.setTextColor(0xffaaaaaa);stage.addView(avatars,new FrameLayout.LayoutParams(-1,-1));TextView create=new TextView(this);create.setText("🏆  Create My Avatar");create.setTextSize(12);create.setTextColor(0xff333333);create.setTypeface(null,Typeface.BOLD);create.setGravity(Gravity.CENTER);create.setBackground(background(Color.WHITE,16));FrameLayout.LayoutParams crp=new FrameLayout.LayoutParams(dp(154),dp(38),Gravity.RIGHT|Gravity.BOTTOM);crp.setMargins(0,0,dp(12),dp(12));stage.addView(create,crp);body.addView(stage,new LinearLayout.LayoutParams(-1,dp(250)));
        LinearLayout profileRow=new LinearLayout(this);profileRow.setGravity(Gravity.CENTER_VERTICAL);profileRow.setPadding(dp(14),dp(10),dp(14),dp(8));TextView av=new TextView(this);av.setText("👤");av.setTextSize(36);av.setGravity(Gravity.CENTER);av.setBackground(background(0xffffe8a2,44));profileRow.addView(av,new LinearLayout.LayoutParams(dp(78),dp(78)));LinearLayout pi=new LinearLayout(this);pi.setOrientation(LinearLayout.VERTICAL);pi.setPadding(dp(12),0,0,0);TextView nm=new TextView(this);nm.setText(who);nm.setTextSize(18);nm.setTypeface(null,Typeface.BOLD);nm.setTextColor(0xff222222);pi.addView(nm);TextView id=new TextView(this);id.setText("ID: "+Math.abs(who.hashCode()%900000+100000));id.setTextSize(11);id.setTextColor(0xff777777);pi.addView(id);TextView badges=new TextView(this);badges.setText("VIP   Lv.1");badges.setTextSize(11);badges.setTextColor(0xff9b6a00);pi.addView(badges);profileRow.addView(pi,new LinearLayout.LayoutParams(0,dp(78),1));body.addView(profileRow,new LinearLayout.LayoutParams(-1,dp(96)));
        LinearLayout stats=new LinearLayout(this);stats.setGravity(Gravity.CENTER);addProfileStat(stats,"3","Followers",null);addProfileStat(stats,"3","Following",null);addProfileStat(stats,"3","Friends",null);addProfileStat(stats,"0","Coupling",null);body.addView(stats,new LinearLayout.LayoutParams(-1,dp(66)));
        View line=new View(this);line.setBackgroundColor(0xffeeeeee);body.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));
        LinearLayout tabs=new LinearLayout(this);tabs.setGravity(Gravity.CENTER_VERTICAL);TextView about=new TextView(this);about.setText("About me");about.setTextSize(14);about.setTypeface(null,Typeface.BOLD);about.setGravity(Gravity.CENTER);about.setBackground(background(0xfffff000,2));tabs.addView(about,new LinearLayout.LayoutParams(0,dp(44),1));TextView posts=new TextView(this);posts.setText("Posts  1");posts.setTextSize(14);posts.setGravity(Gravity.CENTER);tabs.addView(posts,new LinearLayout.LayoutParams(0,dp(44),1));body.addView(tabs);
        LinearLayout details=new LinearLayout(this);details.setOrientation(LinearLayout.VERTICAL);details.setPadding(dp(18),dp(12),dp(18),dp(10));profileInfoRowV600(details,"▣","Love music, games and new friends ✨");profileInfoRowV600(details,"▢","2000-01-01  Capricorn");profileInfoRowV600(details,"⌂","Add your hometown");profileInfoRowV600(details,"♡","Choose tags that fit you");body.addView(details);
        TextView influence=new TextView(this);influence.setText("Influence   ›");influence.setTextSize(15);influence.setTypeface(null,Typeface.BOLD);influence.setTextColor(0xff222222);influence.setPadding(dp(18),dp(10),0,dp(5));body.addView(influence,new LinearLayout.LayoutParams(-1,dp(46)));LinearLayout medals=new LinearLayout(this);medals.setGravity(Gravity.LEFT);String[] m={"💠","⬡","⬡"};for(String x:m){TextView v=new TextView(this);v.setText(x);v.setTextSize(31);v.setGravity(Gravity.CENTER);medals.addView(v,new LinearLayout.LayoutParams(dp(70),dp(58)));}body.addView(medals,new LinearLayout.LayoutParams(-1,dp(64)));
        TextView channels=new TextView(this);channels.setText("Channels joined   ›");channels.setTextSize(15);channels.setTypeface(null,Typeface.BOLD);channels.setTextColor(0xff222222);channels.setPadding(dp(18),dp(8),0,dp(5));body.addView(channels,new LinearLayout.LayoutParams(-1,dp(44)));TextView ch=new TextView(this);ch.setText("👥  KING Plus Party");ch.setTextSize(13);ch.setTextColor(0xff444444);ch.setPadding(dp(18),0,0,0);body.addView(ch,new LinearLayout.LayoutParams(-1,dp(54)));
        Set<String> following=new HashSet<>(getPreferences(0).getStringSet("following",new HashSet<>()));boolean isFollowing=following.contains(who);LinearLayout actions=new LinearLayout(this);actions.setPadding(dp(14),dp(8),dp(14),dp(8));TextView followBtn=new TextView(this);followBtn.setText(isFollowing?"✓ Following":"＋ Follow");followBtn.setGravity(Gravity.CENTER);followBtn.setTextSize(14);followBtn.setTypeface(null,Typeface.BOLD);followBtn.setTextColor(0xff222222);followBtn.setBackground(background(0xffffed00,12));followBtn.setOnClickListener(v->{Set<String> f=new HashSet<>(getPreferences(0).getStringSet("following",new HashSet<>()));if(f.contains(who))f.remove(who);else f.add(who);getPreferences(0).edit().putStringSet("following",f).apply();userProfile(who);});actions.addView(followBtn,new LinearLayout.LayoutParams(0,dp(46),1));TextView msg=new TextView(this);msg.setText("Message");msg.setGravity(Gravity.CENTER);msg.setTextSize(14);msg.setTextColor(0xff333333);msg.setBackground(background(0xfff3f3f3,12));msg.setOnClickListener(v->conversation(who));LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(0,dp(46),1);mp.setMargins(dp(8),0,0,0);actions.addView(msg,mp);body.addView(actions,new LinearLayout.LayoutParams(-1,dp(64)));
        setSafeContentView(root);
    }
    private void profileInfoRowV600(LinearLayout host,String icon,String text){TextView v=new TextView(this);v.setText(icon+"   "+text);v.setTextSize(13);v.setTextColor(0xff4c4c4c);v.setGravity(Gravity.CENTER_VERTICAL);host.addView(v,new LinearLayout.LayoutParams(-1,dp(38)));}

    private void safetyOptions(String who){
        String[] a={"🚫 Block / Unblock","⚑ Report user"};
        new AlertDialog.Builder(this).setTitle(who).setItems(a,(d,w)->{if(w==0){Set<String>b=new HashSet<>(getPreferences(0).getStringSet("blocked",new HashSet<>()));boolean blocked;if(b.contains(who)){b.remove(who);blocked=false;}else{b.add(who);blocked=true;}getPreferences(0).edit().putStringSet("blocked",b).apply();Toast.makeText(this,blocked?"User blocked":"User unblocked",Toast.LENGTH_SHORT).show();}else reportDialog(who);}).show();
    }
    private void reportDialog(String who){
        String[] reasons={"Spam","Harassment","Inappropriate content","Fake account","Other"};
        new AlertDialog.Builder(this).setTitle("Report "+who).setItems(reasons,(d,w)->{
            String reason=reasons[w];
            int count=getPreferences(0).getInt("reports",0)+1;
            String entry=System.currentTimeMillis()+" | "+who+" | "+reason;
            String old=getPreferences(0).getString("report_history","");
            getPreferences(0).edit().putInt("reports",count).putString("report_history",entry+"\n"+old).apply();
            CloudSync.submitReport(this,who,reason,(ok,message)->runOnUiThread(()->Toast.makeText(this,message,Toast.LENGTH_LONG).show()));
        }).setNegativeButton("Cancel",null).show();
    }
    private void notifications(){ notificationsCenter(); }

    private void sideMenu(){
        String[] items={"Community Center","Square / Moments","Privilege Pack","Privilege Shop","Vip Level","Noble Center","Family","Relationship / CP","User level center","Wallet","Invite Friends","Settings","Help Center","Rules and Policies"};
        String[] icons={"square","square","privilege_pack","privilege_shop","vip_level","data_center","family","family","user_level_center","wallet","invite_friends","settings","help_centre","rules_policies"};
        Runnable[] actions={()->openCommunityHub700("Search"),()->openCommunityHub700("Moments"),()->openDeepFlow810("privilege_pack"),()->openDeepFlow810("privilege_shop"),()->openDeepFlow810("vip"),()->openDeepFlow810("noble"),()->openCommunityHub700("Family"),()->openCommunityHub700("Relationship"),()->openDeepFlow810("level_center"),()->openDeepFlow810("wallet"),()->shareText("Join me on KING Plus. My ID: "+(firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null?publicId(firebaseAuth.getCurrentUser().getUid()):displayName)),()->openDeepFlow810("settings"),()->openDeepFlow810("help"),()->openDeepFlow810("rules")};
        ScrollView scroll=new ScrollView(this);LinearLayout drawer=new LinearLayout(this);drawer.setOrientation(LinearLayout.VERTICAL);drawer.setPadding(dp(14),dp(20),dp(10),dp(20));drawer.setBackgroundColor(Color.WHITE);scroll.addView(drawer);
        AlertDialog dialog=new AlertDialog.Builder(this).setView(scroll).create();
        for(int i=0;i<items.length;i++){final int k=i;TextView row=new TextView(this);row.setText(items[i]);int icon=getResources().getIdentifier("ref_me_drawer_icon_"+icons[i],"drawable",getPackageName());if(icon!=0){android.graphics.drawable.Drawable art=getDrawable(icon);art.setBounds(0,0,dp(25),dp(25));row.setCompoundDrawables(art,null,null,null);row.setCompoundDrawablePadding(dp(16));}row.setTextSize(15);row.setTextColor(0xff222222);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(8),0,0,0);row.setOnClickListener(v->{dialog.dismiss();actions[k].run();});drawer.addView(row,new LinearLayout.LayoutParams(-1,dp(52)));}
        dialog.setOnShowListener(v->{android.view.Window w=dialog.getWindow();if(w!=null){w.setBackgroundDrawableResource(android.R.color.transparent);w.setGravity(Gravity.LEFT|Gravity.TOP);w.setLayout((int)(getResources().getDisplayMetrics().widthPixels*.82f),-1);}});dialog.show();
    }

    private void openDeepFlow810(String route){Intent i=new Intent(this,KingDeepFlowActivity.class);i.putExtra("route",route);startActivity(i);}

    private void editProfile(){
        screen="editProfile";
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.WHITE);
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(8),dp(4),dp(12),0);TextView back=new TextView(this);back.setText("‹");back.setTextSize(34);back.setTextColor(0xff222222);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->profile());head.addView(back,new LinearLayout.LayoutParams(dp(48),dp(52)));TextView title=new TextView(this);title.setText("Edit Profile");title.setTextSize(20);title.setTypeface(null,Typeface.BOLD);title.setTextColor(0xff222222);head.addView(title,new LinearLayout.LayoutParams(0,dp(52),1));TextView saveTop=new TextView(this);saveTop.setText("Save");saveTop.setTextSize(13);saveTop.setTextColor(0xff7a6500);saveTop.setGravity(Gravity.CENTER);head.addView(saveTop,new LinearLayout.LayoutParams(dp(56),dp(42)));root.addView(head);
        ScrollView sv=new ScrollView(this);LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(14),dp(6),dp(14),dp(20));sv.addView(body);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        TextView warn=new TextView(this);warn.setText("You can restrict other users from downloading/saving your posted images in Settings.");warn.setTextSize(11);warn.setTextColor(0xff725d00);warn.setGravity(Gravity.CENTER_VERTICAL);warn.setPadding(dp(12),dp(8),dp(12),dp(8));warn.setBackground(background(0xfffff3bf,8));body.addView(warn,new LinearLayout.LayoutParams(-1,dp(54)));
        String photo=getPreferences(0).getString("profile_photo","");LinearLayout album=new LinearLayout(this);album.setGravity(Gravity.CENTER_VERTICAL);album.setPadding(dp(4),dp(10),dp(4),dp(10));TextView albLabel=new TextView(this);albLabel.setText("Album");albLabel.setTextSize(14);albLabel.setTextColor(0xff333333);album.addView(albLabel,new LinearLayout.LayoutParams(dp(88),dp(60)));View av;if(photo.isEmpty()){TextView a=new TextView(this);a.setText("＋");a.setTextSize(27);a.setGravity(Gravity.CENTER);a.setTextColor(0xff888888);a.setBackground(background(0xfff1f1f1,8));av=a;}else{ImageView a=new ImageView(this);a.setScaleType(ImageView.ScaleType.CENTER_CROP);try{a.setImageURI(Uri.parse(photo));}catch(Exception ignored){}a.setBackground(background(0xfff1f1f1,8));av=a;}av.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("image/*");i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);startActivityForResult(i,24);});album.addView(av,new LinearLayout.LayoutParams(dp(62),dp(62)));body.addView(album,new LinearLayout.LayoutParams(-1,dp(82)));
        final EditText name=profileInputV600(body,"Nickname",displayName);final EditText bio=profileInputV600(body,"Bio",getPreferences(0).getString("bio","Love music, games and new friends ✨"));final EditText birthday=profileInputV600(body,"Birthday",getPreferences(0).getString("birthday","2000-01-01"));final EditText hometown=profileInputV600(body,"Hometown",getPreferences(0).getString("hometown",""));final EditText tags=profileInputV600(body,"Labels",getPreferences(0).getString("tags","Music, Games"));
        TextView gender=new TextView(this);gender.setText("Gender                         Not specified  ›");gender.setTextSize(13);gender.setTextColor(0xff555555);gender.setGravity(Gravity.CENTER_VERTICAL);gender.setPadding(dp(4),0,dp(4),0);body.addView(gender,new LinearLayout.LayoutParams(-1,dp(48)));
        TextView tip=new TextView(this);tip.setText("Tip: a complete profile helps other people discover and follow you.");tip.setTextSize(11);tip.setTextColor(0xff999999);tip.setPadding(dp(4),dp(8),dp(4),dp(10));body.addView(tip,new LinearLayout.LayoutParams(-1,dp(48)));
        TextView save=new TextView(this);save.setText("Save Profile");save.setTextSize(15);save.setTypeface(null,Typeface.BOLD);save.setTextColor(0xff222222);save.setGravity(Gravity.CENTER);save.setBackground(background(0xffffea00,10));LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(52));sp.setMargins(0,dp(8),0,0);body.addView(save,sp);
        Runnable doSave=()->{String n=name.getText().toString().trim();if(n.isEmpty()){name.setError("Name required");return;}displayName=n;getPreferences(0).edit().putString("name",n).putString("bio",bio.getText().toString().trim()).putString("hometown",hometown.getText().toString().trim()).putString("birthday",birthday.getText().toString().trim()).putString("tags",tags.getText().toString().trim()).apply();if(CloudSync.isSignedIn())syncPublicProfile();Toast.makeText(this,"Profile updated",Toast.LENGTH_SHORT).show();profile();};save.setOnClickListener(v->doSave.run());saveTop.setOnClickListener(v->doSave.run());
        setSafeContentView(root);
    }
    private EditText profileInputV600(LinearLayout host,String label,String value){LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(4),0,dp(4),0);TextView l=new TextView(this);l.setText(label);l.setTextSize(13);l.setTextColor(0xff555555);row.addView(l,new LinearLayout.LayoutParams(dp(96),dp(52)));EditText e=new EditText(this);e.setText(value);e.setTextSize(13);e.setTextColor(0xff333333);e.setSingleLine(true);e.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);e.setBackgroundColor(Color.TRANSPARENT);row.addView(e,new LinearLayout.LayoutParams(0,dp(52),1));host.addView(row,new LinearLayout.LayoutParams(-1,dp(52)));View line=new View(this);line.setBackgroundColor(0xffeeeeee);host.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));return e;}

    private void cloudSafetyCenter(){
        screen="cloud_safety"; base("Cloud & Safety Center","KING Plus v2.8 foundation");
        text(CloudSync.isSignedIn()?"☁ Firebase account connected":"☁ Local/test session • cloud actions need real Firebase sign-in",16,Color.WHITE,true);
        button("☁ Sync profile + TEST wallet snapshot",PURPLE,()->CloudSync.syncProfileAndTestWallet(this,getPreferences(0),displayName,coinBalance,giftCount,receivedGiftCount,(ok,message)->runOnUiThread(()->Toast.makeText(this,message,Toast.LENGTH_LONG).show())));
        button("🔔 Enable / refresh push notifications",CARD,()->{PushNotifications.requestPermission(this);Toast.makeText(this,"Push notification setup requested",Toast.LENGTH_SHORT).show();});
        button("🛡 Moderation & Safety",CARD,this::moderationCenterPage);
        button("💳 Recharge Center",CARD,this::rechargeCenterPage);
        text("Cloud profile sync, FCM token registration and cloud report submission are wired. Real push delivery still needs messages sent through Firebase/your backend.",13,MUTED,false);
        button("Back to Settings",CARD,this::settingsPage);
    }

    private void moderationCenterPage(){
        screen="moderation"; base("Moderation & Safety","Block, report and review foundation");
        Set<String> blocked=new HashSet<>(getPreferences(0).getStringSet("blocked",new HashSet<>()));
        int reports=getPreferences(0).getInt("reports",0);
        text("🚫 Blocked users: "+blocked.size(),18,Color.WHITE,true);
        text("⚑ Reports submitted locally: "+reports,18,Color.WHITE,true);
        String history=getPreferences(0).getString("report_history","");
        if(history.isEmpty()) text("No local report history yet.",14,MUTED,false);
        else {
            text("Recent local report audit",16,Color.WHITE,true);
            String[] rows=history.split("\n");
            for(int i=0;i<Math.min(rows.length,8);i++) if(!rows[i].trim().isEmpty()) text(rows[i],12,MUTED,false);
        }
        text("Signed-in Firebase users also submit reports to the Firestore moderation queue. Reading/updating that queue is denied to normal app clients by the included starter security rules.",13,MUTED,false);
        button("Privacy controls",CARD,this::privacySafetyPage);
        button("Back",CARD,this::cloudSafetyCenter);
    }

    private void rechargeCenterPage(){
        screen="recharge"; base("Recharge Center","TEST purchase foundation • no real money charged");
        text("Current balance: 💎 "+coinBalance+" coins",24,Color.WHITE,true);
        button("TEST +100 coins • ₹10 display price",CARD,()->testRecharge(100,10));
        button("TEST +600 coins • ₹50 display price",CARD,()->testRecharge(600,50));
        button("TEST +1300 coins • ₹100 display price",PURPLE,()->testRecharge(1300,100));
        String history=getPreferences(0).getString("test_recharge_history","");
        if(!history.isEmpty()) { text("Test receipts",17,Color.WHITE,true); String[] rows=history.split("\n"); for(int i=0;i<Math.min(rows.length,5);i++) if(!rows[i].trim().isEmpty()) text(rows[i],11,MUTED,false); }
        text("PRODUCTION LOCK: this build cannot charge money. Real recharge requires Play Billing/store products plus trusted backend receipt verification and server-authoritative coin crediting.",13,0xffffd768,true);
        button("Back to Wallet",CARD,this::walletPage);
    }

    private void testRecharge(int coins,int rupees){
        RechargeManager.simulateTestRecharge(getPreferences(0),coins,rupees,(newBalance,receipt)->{
            coinBalance=newBalance;
            appendTransaction("+"+coins+" TEST coins • recharge simulator • NO MONEY CHARGED");
            Toast.makeText(this,"TEST balance updated • no money charged",Toast.LENGTH_LONG).show();
            rechargeCenterPage();
        });
    }

    private void openLiveCloud(){
        if(!CloudSync.isSignedIn()){
            new AlertDialog.Builder(this).setTitle("Real Firebase sign-in required")
                .setMessage("Live rooms, cloud chat, server wallet, push invites and admin tools require a real Firebase Google/phone account. The v2.9 build uses the real mobile OTP path.")
                .setPositiveButton("OK",null).show();
            return;
        }
        startActivity(new Intent(this,LiveCloudActivity.class));
    }
    private void openAdminDashboard(){
        if(!CloudSync.isSignedIn()){
            Toast.makeText(this,"Sign in with Firebase first",Toast.LENGTH_LONG).show();
            return;
        }
        startActivity(new Intent(this,AdminActivity.class));
    }

    private void productionReadinessPage(){
        screen="production_readiness";
        base("KING Plus v3.0","Security, notifications, billing and release readiness");
        text("🔐 Server wallet & gifts",19,Color.WHITE,true);
        text("Production wallet writes stay blocked from normal clients. Gifts use trusted Cloud Functions, idempotent operation IDs and server ledger entries.",13,MUTED,false);
        text("🔔 Push notifications",19,Color.WHITE,true);
        text("FCM supports gift, room invite, live-room message, direct-message and follow notification paths once Cloud Functions are deployed.",13,MUTED,false);
        text("💳 Google Play recharge",19,Color.WHITE,true);
        text("Play Billing never credits coins locally. A completed purchase token is sent to the backend for Google Play verification before server wallet credit.",13,MUTED,false);
        button("💳 Open Play Recharge",PURPLE,()->BillingManager.showRecharge(this));
        button("🌐 Open Live Cloud",CARD,this::openLiveCloud);
        button("🛡 Open Admin Dashboard",CARD,this::openAdminDashboard);
        text("Release candidate note: the CI APK/AAB is signed with the stable KING Plus test key so it can be tested consistently. A Play production upload key and Play App Signing are still required for store release.",12,0xffffd768,true);
        button("Back to Settings",CARD,this::settingsPage);
    }

    private void walletPage(){
        screen="wallet"; base("My Wallet","Server-authoritative KING Plus diamonds");
        TextView bal=text("💎 "+coinBalance+" Diamonds",28,Color.WHITE,true); text("Verified server balance",14,MUTED,false);
        if(firestore!=null&&firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null){
            firestore.collection("wallets").document(firebaseAuth.getCurrentUser().getUid()).get().addOnSuccessListener(doc->{Object raw=doc.get("coins");long c=raw instanceof Number?Math.max(0,Math.round(((Number)raw).doubleValue())):0;coinBalance=(int)Math.min(Integer.MAX_VALUE,c);bal.setText("💎 "+c+" Diamonds");});
        }
        button("Open Gift Catalog",PURPLE,this::giftCatalogPage);
        button("Recharge",CARD,()->new AlertDialog.Builder(this).setTitle("Recharge").setMessage("Real-money recharge stays disabled in this no-billing build. The server wallet is real; paid recharge will be enabled only after Play products and server verification are activated.").setPositiveButton("OK",null).show());
        button("📜 Server transaction history",CARD,()->Toast.makeText(this,"Server wallet history is shown after verified gift/recharge ledger entries are available.",Toast.LENGTH_LONG).show());
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
        String frame=KingCosmetics.frame(this);String effect=KingCosmetics.effect(this);
        text("ID Frames & Entrance Effects",18,Color.WHITE,true);text(KingCosmetics.frameEmoji(frame)+"  ID frame: "+frame,15,MUTED,false);text(KingCosmetics.effectEmoji(effect)+"  Entry effect: "+effect,15,MUTED,false);button("🖼 Choose ID / Profile Frame",CARD,()->selectCosmetic730(true));button("✨ Choose Room Entry Effect",CARD,()->selectCosmetic730(false));
        button("Back",CARD,this::profile);
    }

    private void levelPage(){
        screen="level"; base("Level & Achievements","Your real KING Plus progression");
        LevelSystem.Snapshot p=LevelSystem.read(this); long next=p.nextLevelXp(); long prev=LevelSystem.levelThreshold(p.level); long span=Math.max(1,next-prev); int percent=p.level>=LevelSystem.MAX_LEVEL?100:(int)Math.max(0,Math.min(100,(p.xp-prev)*100/span));
        text("🏆 Normal Level "+p.level+" / 99",30,Color.WHITE,true);text(LevelSystem.levelTier(p.level)+" • "+p.xp+" XP • "+percent+"% to next level",15,MUTED,false);
        text("💎 VIP "+p.vipLevel+" / "+LevelSystem.MAX_VIP+" • "+LevelSystem.vipName(p.vipLevel),22,Color.WHITE,true);text(p.vipPoints+" VIP points • next "+p.nextVipPoints(),14,MUTED,false);
        text("Achievements",18,Color.WHITE,true);text("🎤 Voice Explorer  "+(getMissionValue("mission_room")>0?"✓":"○"),16,Color.WHITE,false);text("🎁 Gift Supporter  "+(giftCount>0?"✓":"○"),16,Color.WHITE,false);text("💬 Room messages today  "+getMissionValue("mission_chat"),16,MUTED,false);
        button("🎒 Collection & Medals",PURPLE,()->openCommunityHub700("Collection"));button("Back",CARD,this::profile);
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

    private void requestAccountDeletionFlow(){
        if (!CloudSync.isSignedIn()) {
            new AlertDialog.Builder(this).setTitle("Sign-in required")
                .setMessage("Account deletion can only be requested for a real Firebase account.")
                .setPositiveButton("OK",null).show();
            return;
        }
        new AlertDialog.Builder(this).setTitle("Request account deletion?")
            .setMessage("This sends a server-side deletion request for your KING Plus account. Purchase and moderation records may need limited retention where required for security, fraud prevention or legal obligations.")
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Request deletion",(d,w)->CloudBackend.requestAccountDeletion((ok,message)->runOnUiThread(()->{
                Toast.makeText(this,message,Toast.LENGTH_LONG).show();
                if(ok){
                    if(firebaseAuth!=null) firebaseAuth.signOut();
                    if(googleSignInClient!=null) googleSignInClient.signOut();
                    getPreferences(0).edit().remove("name").apply();
                    displayName="";
                    login();
                }
            }))).show();
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

    private void profile() { profileV600(); }

    private void profileV600(){
        screen="profile";stopMic();
        final LevelSystem.Snapshot progress700=LevelSystem.read(this);final int level=progress700.level;
        final String uid=(firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null)?publicId(firebaseAuth.getCurrentUser().getUid()):roomId(displayName);
        final String photo=getPreferences(0).getString("profile_photo","");
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.WHITE);
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);top.setPadding(dp(12),dp(5),dp(8),0);
        TextView menu=new TextView(this);menu.setText("☰");menu.setTextSize(27);menu.setTextColor(0xff222222);menu.setGravity(Gravity.CENTER);menu.setOnClickListener(v->sideMenu());top.addView(menu,new LinearLayout.LayoutParams(dp(48),dp(50)));
        TextView spacer=new TextView(this);spacer.setText("");top.addView(spacer,new LinearLayout.LayoutParams(0,dp(50),1));
        TextView beans=smallBadge("💎 0",0xfffff7cc,0xff6c5b00);profileTopBalance=beans;beans.setOnClickListener(v->walletPage());top.addView(beans,new LinearLayout.LayoutParams(dp(78),dp(34)));
        TextView coins=smallBadge("◈ 0",0xfffff7cc,0xff6c5b00);profileWalletCoins=coins;coins.setOnClickListener(v->walletPage());LinearLayout.LayoutParams cpl=new LinearLayout.LayoutParams(dp(68),dp(34));cpl.setMargins(dp(5),0,0,0);top.addView(coins,cpl);root.addView(top,new LinearLayout.LayoutParams(-1,dp(56)));

        ScrollView sv=new ScrollView(this);LinearLayout content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(14),dp(6),dp(14),dp(16));sv.addView(content);
        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(dp(4),dp(4),dp(4),dp(4));
        String equippedFrame730=KingCosmetics.frame(this);View avatar;
        if(photo.isEmpty()){TextView a=new TextView(this);a.setText("👑");a.setTextSize(39);a.setGravity(Gravity.CENTER);a.setBackground(background(0xffffefd1,44));avatar=a;}else{ImageView a=new ImageView(this);a.setScaleType(ImageView.ScaleType.CENTER_CROP);try{a.setImageURI(Uri.parse(photo));}catch(Exception ignored){}a.setBackground(background(0xffffefd1,44));avatar=a;}
        FrameLayout avatarFrame730=new FrameLayout(this);avatarFrame730.setBackground(KingCosmetics.avatarFrame(this,equippedFrame730,false));avatarFrame730.setPadding(dp(4),dp(4),dp(4),dp(4));avatarFrame730.addView(avatar,new FrameLayout.LayoutParams(-1,-1));avatarFrame730.setOnClickListener(v->editProfile());header.addView(avatarFrame730,new LinearLayout.LayoutParams(dp(86),dp(86)));
        LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);info.setPadding(dp(12),0,0,0);
        TextView name=new TextView(this);name.setText(displayName+"  ›");name.setTextSize(18);name.setTypeface(null,Typeface.BOLD);name.setTextColor(0xff191919);name.setOnClickListener(v->editProfile());info.addView(name,new LinearLayout.LayoutParams(-1,dp(32)));
        TextView id=new TextView(this);id.setText(KingCosmetics.frameEmoji(equippedFrame730)+"  ID: "+uid+"  "+KingCosmetics.frameEmoji(equippedFrame730));id.setTextSize(11);id.setTextColor(0xff3d3348);id.setGravity(Gravity.CENTER_VERTICAL);id.setPadding(dp(10),0,dp(10),0);id.setBackground(KingCosmetics.idFrame(this,equippedFrame730));info.addView(id,new LinearLayout.LayoutParams(-1,dp(28)));
        LinearLayout mini=new LinearLayout(this);mini.setGravity(Gravity.CENTER_VERTICAL);TextView vip=smallBadge("VIP "+progress700.vipLevel,0xffffe57b,0xff5a4100);mini.addView(vip,new LinearLayout.LayoutParams(dp(50),dp(25)));TextView lv=smallBadge("Lv."+level,0xff8865e8,Color.WHITE);LinearLayout.LayoutParams lvp=new LinearLayout.LayoutParams(dp(58),dp(25));lvp.setMargins(dp(5),0,0,0);mini.addView(lv,lvp);info.addView(mini,new LinearLayout.LayoutParams(-1,dp(28)));header.addView(info,new LinearLayout.LayoutParams(0,dp(86),1));content.addView(header,new LinearLayout.LayoutParams(-1,dp(94)));

        LinearLayout counts=new LinearLayout(this);counts.setGravity(Gravity.CENTER);profileFollowersNumber=addProfileStat(counts,"0","Followers",()->peoplePage("Followers"));profileFollowingNumber=addProfileStat(counts,"0","Following",()->peoplePage("Following"));profileFriendsNumber=addProfileStat(counts,"0","Friends",()->peoplePage("Friends"));String coupling=getPreferences(0).getString("coupling_name","");addProfileStat(counts,coupling.isEmpty()?"0":"1","Coupling",()->openCommunityHub700("Relationship"));content.addView(counts,new LinearLayout.LayoutParams(-1,dp(66)));

        LinearLayout family=new LinearLayout(this);family.setGravity(Gravity.CENTER_VERTICAL);family.setPadding(dp(12),0,dp(12),0);family.setBackground(background(0xfffff3dc,7));TextView fi=new TextView(this);fi.setText("👑");fi.setTextSize(21);fi.setGravity(Gravity.CENTER);family.addView(fi,new LinearLayout.LayoutParams(dp(42),dp(48)));TextView ft=new TextView(this);ft.setText("Family Square");ft.setTextSize(14);ft.setTypeface(null,Typeface.BOLD);ft.setTextColor(0xff473819);family.addView(ft,new LinearLayout.LayoutParams(0,dp(48),1));TextView ar=new TextView(this);ar.setText("›");ar.setTextSize(25);ar.setGravity(Gravity.CENTER);family.addView(ar,new LinearLayout.LayoutParams(dp(34),dp(48)));family.setOnClickListener(v->openCommunityHub700("Family"));LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,dp(52));fp.setMargins(0,dp(7),0,dp(12));content.addView(family,fp);
        LinearLayout hub=new LinearLayout(this);String[] hi={"🔎 Search","📝 Moments","💞 CP","🎒 Collection"};String[] ht={"Search","Moments","Relationship","Collection"};for(int i=0;i<hi.length;i++){final String tab=ht[i];TextView x=new TextView(this);x.setText(hi[i]);x.setTextSize(11);x.setGravity(Gravity.CENTER);x.setTextColor(0xff41394b);x.setBackground(background(0xfff3f0f7,12));x.setOnClickListener(v->openCommunityHub700(tab));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(46),1);lp.setMargins(dp(3),0,dp(3),0);hub.addView(x,lp);}content.addView(hub,new LinearLayout.LayoutParams(-1,dp(52)));

        LinearLayout tabs=new LinearLayout(this);tabs.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);TextView fav=new TextView(this);fav.setText("Favorites");fav.setTextSize(14);fav.setTypeface(null,Typeface.BOLD);fav.setTextColor(0xff222222);fav.setGravity(Gravity.CENTER);fav.setBackground(background(0xfffff200,3));tabs.addView(fav,new LinearLayout.LayoutParams(dp(86),dp(38)));TextView follow=new TextView(this);follow.setText("Follow");follow.setTextSize(14);follow.setTextColor(0xff555555);follow.setGravity(Gravity.CENTER);follow.setOnClickListener(v->peoplePage("Following"));tabs.addView(follow,new LinearLayout.LayoutParams(dp(78),dp(38)));content.addView(tabs,new LinearLayout.LayoutParams(-1,dp(44)));
        TextView records=new TextView(this);records.setText("Party Records");records.setTextSize(15);records.setTypeface(null,Typeface.BOLD);records.setTextColor(0xff222222);records.setPadding(0,dp(8),0,dp(6));content.addView(records,new LinearLayout.LayoutParams(-1,dp(44)));
        LinearLayout recordList=new LinearLayout(this);recordList.setOrientation(LinearLayout.VERTICAL);content.addView(recordList,new LinearLayout.LayoutParams(-1,-2));loadPartyRecordsV600(recordList);
        root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));addBottomNav(root,4);setSafeContentView(root);loadRealProfileData();
    }

    private void loadPartyRecordsV600(LinearLayout host){
        host.removeAllViews();
        if(firestore==null||firebaseAuth==null||firebaseAuth.getCurrentUser()==null){mePartyRecordV600(host,"🎤","No signed-in Party records","Sign in to load real rooms",null);return;}
        firestore.collection("live_rooms").orderBy("createdAt",com.google.firebase.firestore.Query.Direction.DESCENDING).limit(8).get().addOnSuccessListener(snap->{host.removeAllViews();int n=0;for(com.google.firebase.firestore.DocumentSnapshot d:snap.getDocuments()){if(Boolean.TRUE.equals(d.getBoolean("closed")))continue;String rn=d.getString("name");String cat=d.getString("category");String own=d.getString("ownerName");String rid=d.getId();mePartyRecordV600(host,"Music".equalsIgnoreCase(cat)?"🎵":"🎤",rn==null?"Live Party":rn,(cat==null?"Chat":cat)+"  •  "+(own==null?"KING Host":own),()->{Intent i=new Intent(this,PartyActivity.class);i.putExtra("directRoomId",rid);i.putExtra("directRoomName",rn);i.putExtra("directOwnerUid",d.getString("ownerUid"));i.putExtra("directOwnerName",own);i.putExtra("directPrivate",Boolean.TRUE.equals(d.getBoolean("isPrivate")));i.putExtra("directPassword",Boolean.TRUE.equals(d.getBoolean("hasPassword")));startActivity(i);});if(++n>=6)break;}if(n==0)mePartyRecordV600(host,"🎤","No Party records yet","Join a live Party room",this::openPartyActivity);}).addOnFailureListener(e->mePartyRecordV600(host,"🎤","Party records unavailable","Tap to open Party",this::openPartyActivity));
    }
    private void mePartyRecordV600(LinearLayout host,String icon,String title,String sub,Runnable action){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(4),dp(5),dp(4),dp(5));TextView av=new TextView(this);av.setText(icon);av.setTextSize(24);av.setGravity(Gravity.CENTER);av.setBackground(background(0xfff2f2f2,9));row.addView(av,new LinearLayout.LayoutParams(dp(54),dp(54)));LinearLayout txt=new LinearLayout(this);txt.setOrientation(LinearLayout.VERTICAL);txt.setPadding(dp(10),0,0,0);TextView t=new TextView(this);t.setText(title);t.setTextSize(13);t.setTextColor(0xff222222);t.setTypeface(null,Typeface.BOLD);txt.addView(t);TextView s=new TextView(this);s.setText(sub);s.setTextSize(11);s.setTextColor(0xff888888);txt.addView(s);row.addView(txt,new LinearLayout.LayoutParams(0,dp(52),1));TextView online=new TextView(this);online.setText("◉");online.setTextColor(0xff6b6b6b);online.setGravity(Gravity.CENTER);row.addView(online,new LinearLayout.LayoutParams(dp(34),dp(52)));if(action!=null)row.setOnClickListener(v->action.run());host.addView(row,new LinearLayout.LayoutParams(-1,dp(64)));
    }

    private TextView smallBadge(String label,int bgColor,int textColor){
        TextView v=new TextView(this);
        v.setText(label);
        v.setTextSize(12);
        v.setTextColor(textColor);
        v.setTypeface(null,Typeface.BOLD);
        v.setGravity(Gravity.CENTER);
        v.setBackground(background(bgColor,14));
        return v;
    }

    private void loadRealProfileData(){
        if(firestore==null||firebaseAuth==null||firebaseAuth.getCurrentUser()==null){
            if(profileFollowersNumber!=null)profileFollowersNumber.setText("0");
            if(profileFollowingNumber!=null)profileFollowingNumber.setText("0");
            if(profileFriendsNumber!=null)profileFriendsNumber.setText("0");
            if(profileTopBalance!=null)profileTopBalance.setText("💎 0");
            if(profileWalletCoins!=null)profileWalletCoins.setText("💎 0");
            return;
        }
        final String uid=firebaseAuth.getCurrentUser().getUid();
        firestore.collection("follows").whereEqualTo("followerUid",uid).get().addOnSuccessListener(out->{
            final Set<String> followingSet=new HashSet<>();
            for(DocumentSnapshot d:out.getDocuments()){String x=d.getString("targetUid");if(x!=null)followingSet.add(x);}
            if(profileFollowingNumber!=null)profileFollowingNumber.setText(String.valueOf(followingSet.size()));
            firestore.collection("follows").whereEqualTo("targetUid",uid).get().addOnSuccessListener(in->{
                int followers=0,friends=0;
                for(DocumentSnapshot d:in.getDocuments()){String x=d.getString("followerUid");if(x!=null){followers++;if(followingSet.contains(x))friends++;}}
                if(profileFollowersNumber!=null)profileFollowersNumber.setText(String.valueOf(followers));
                if(profileFriendsNumber!=null)profileFriendsNumber.setText(String.valueOf(friends));
            });
        });
        refreshServerWallet();
    }
    private void refreshServerWallet(){
        if(firestore==null||firebaseAuth==null||firebaseAuth.getCurrentUser()==null)return;
        firestore.collection("wallets").document(firebaseAuth.getCurrentUser().getUid()).get().addOnSuccessListener(doc->{
            Object raw=doc.get("coins"); long coins=raw instanceof Number?Math.max(0,Math.round(((Number)raw).doubleValue())):0;
            coinBalance=(int)Math.min(Integer.MAX_VALUE,coins);
            if(profileTopBalance!=null)profileTopBalance.setText("💎 "+coins);
            if(profileWalletCoins!=null)profileWalletCoins.setText("💎 "+coins);
        });
    }

    private TextView addProfileStat(LinearLayout row,String value,String label,Runnable action){
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        TextView number=new TextView(this);
        number.setText(value);
        number.setTextSize(17);
        number.setTextColor(0xff211b27);
        number.setTypeface(null,Typeface.BOLD);
        number.setGravity(Gravity.CENTER);
        box.addView(number,new LinearLayout.LayoutParams(-1,dp(30)));
        TextView name=new TextView(this);
        name.setText(label);
        name.setTextSize(11);
        name.setTextColor(0xff77717e);
        name.setGravity(Gravity.CENTER);
        box.addView(name,new LinearLayout.LayoutParams(-1,dp(25)));
        if(action!=null)box.setOnClickListener(v->action.run());
        row.addView(box,new LinearLayout.LayoutParams(0,dp(60),1));
        return number;
    }

    private void addMeQuick(LinearLayout row,String icon,String label,Runnable action){
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        TextView iv=new TextView(this);
        iv.setText(icon);
        iv.setTextSize(24);
        iv.setGravity(Gravity.CENTER);
        iv.setBackground(background(0xffeee8f8,18));
        box.addView(iv,new LinearLayout.LayoutParams(dp(50),dp(50)));
        TextView tv=new TextView(this);
        tv.setText(label);
        tv.setTextSize(11);
        tv.setTextColor(0xff39323f);
        tv.setGravity(Gravity.CENTER);
        box.addView(tv,new LinearLayout.LayoutParams(-1,dp(28)));
        if(action!=null)box.setOnClickListener(v->action.run());
        row.addView(box,new LinearLayout.LayoutParams(0,dp(84),1));
    }

    private void addMeChip(LinearLayout row,String label,Runnable action){
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

    private void meRow(LinearLayout host,String icon,String title,String subtitle,Runnable action){
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(8),dp(7),dp(5),dp(7));
        TextView iv=new TextView(this);
        iv.setText(icon);
        iv.setTextSize(21);
        iv.setGravity(Gravity.CENTER);
        iv.setBackground(background(0xfff0ebf7,16));
        row.addView(iv,new LinearLayout.LayoutParams(dp(46),dp(46)));
        LinearLayout mid=new LinearLayout(this);
        mid.setOrientation(LinearLayout.VERTICAL);
        mid.setPadding(dp(11),0,0,0);
        TextView t=new TextView(this);
        t.setText(title);
        t.setTextSize(15);
        t.setTextColor(0xff29232f);
        t.setTypeface(null,Typeface.BOLD);
        mid.addView(t,new LinearLayout.LayoutParams(-1,dp(25)));
        TextView st=new TextView(this);
        st.setText(subtitle);
        st.setTextSize(11);
        st.setTextColor(0xff8a8490);
        mid.addView(st,new LinearLayout.LayoutParams(-1,dp(21)));
        row.addView(mid,new LinearLayout.LayoutParams(0,dp(48),1));
        TextView arrow=new TextView(this);
        arrow.setText("›");
        arrow.setTextSize(27);
        arrow.setTextColor(0xffa39ca9);
        arrow.setGravity(Gravity.CENTER);
        row.addView(arrow,new LinearLayout.LayoutParams(dp(30),dp(48)));
        if(action!=null)row.setOnClickListener(v->action.run());
        host.addView(row,new LinearLayout.LayoutParams(-1,dp(62)));
    }

    private void rankingsPage(){
        screen="rankings"; base("Rankings","Real KING Plus activity only");
        text("👑 Live rankings",20,Color.WHITE,true);
        if(firebaseAuth==null || firebaseAuth.getCurrentUser()==null || firestore==null){
            text("Sign in to view real community and room activity. KING Plus no longer shows sample leaderboard names.",14,MUTED,false);
            button("Sign in",PURPLE,this::login);
            button("Back to Profile",CARD,this::profile);
            return;
        }
        text("Sample users and made-up charm/coin totals have been removed.",14,MUTED,false);
        text("Open Party to view the real Room Ranking, Gift Senders, Gift History and live room activity generated by signed-in members.",14,Color.WHITE,false);
        button("🎤 Open real Party rankings",PURPLE,this::openPartyActivity);
        button("👥 Open real Followers / Friends",CARD,()->startActivity(new Intent(this,SocialActivity.class)));
        button("Back to Profile",CARD,this::profile);
    }
    private int vipPreviewLevel540=0;
    private void vipPage(){
        screen="vip";LevelSystem.Snapshot pr=LevelSystem.read(this);vipPreviewLevel540=pr.vipLevel;
        int[] accents={0xffa5b6c2,0xff39c7d5,0xff94d33b,0xffd42d67,0xffc186e8};int accent=accents[Math.min(accents.length-1,pr.vipLevel/3)];
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(16),dp(8),dp(16),dp(12));root.setBackground(new android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,new int[]{accent,0xff081413,Color.BLACK}));
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);TextView back=vipText540("‹",30,Color.WHITE);back.setOnClickListener(v->profile());head.addView(back,new LinearLayout.LayoutParams(dp(44),dp(48)));TextView title=vipText540("VIP / Noble",21,Color.WHITE);head.addView(title,new LinearLayout.LayoutParams(0,dp(48),1));TextView rules=vipText540("ⓘ",23,Color.WHITE);rules.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("VIP Rules").setMessage("VIP points rise from gifting activity in TEST/no-billing mode. VIP does not represent a paid subscription in this build.").setPositiveButton("OK",null).show());head.addView(rules,new LinearLayout.LayoutParams(dp(44),dp(48)));root.addView(head);
        ScrollView scroll=new ScrollView(this);LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));TextView badge=vipText540(pr.vipLevel>=8?"♛":"♦",84,accent);body.addView(badge,new LinearLayout.LayoutParams(-1,dp(120)));
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(20),dp(14),dp(20),dp(16));card.setBackground(background(0xefffffff,18));card.addView(vipText540("VIP "+pr.vipLevel+" • "+LevelSystem.vipName(pr.vipLevel),30,0xff253c38));long from=LevelSystem.vipThreshold(pr.vipLevel),to=pr.nextVipPoints(),span=Math.max(1,to-from);int pc=pr.vipLevel>=LevelSystem.MAX_VIP?100:(int)Math.max(0,Math.min(100,(pr.vipPoints-from)*100/span));card.addView(vipText540(pr.vipPoints+" VIP points • "+pc+"% to next tier",13,0xff53655f));android.widget.ProgressBar progress=new android.widget.ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(100);progress.setProgress(pc);card.addView(progress,new LinearLayout.LayoutParams(-1,dp(24)));body.addView(card,new LinearLayout.LayoutParams(-1,dp(144)));
        body.addView(vipText540("VIP privileges",18,Color.WHITE),new LinearLayout.LayoutParams(-1,dp(58)));String[] icons={"💎","👑","✨","🖼","🎤","🎁"};String[] labels={"VIP identity","Profile badge","Entrance effect","Profile frame","Room badge","Gift support"};int[] unlock={1,2,3,4,6,8};for(int r=0;r<2;r++){LinearLayout row=new LinearLayout(this);for(int c=0;c<3;c++){int i=r*3+c;boolean ok=pr.vipLevel>=unlock[i];TextView tile=vipText540((ok?icons[i]:"🔒")+"\n"+labels[i]+"\nVIP "+unlock[i],12,ok?Color.WHITE:0xff999999);tile.setBackground(background(ok?0x24ffffff:0x16000000,12));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(92),1);lp.setMargins(dp(4),dp(4),dp(4),dp(4));row.addView(tile,lp);}body.addView(row);}
        TextView note=vipText540("Gift in rooms or private chat to grow VIP points • no real billing",12,0xffe0e0e0);body.addView(note,new LinearLayout.LayoutParams(-1,dp(48)));TextView collection=vipText540("🎒 Open Collection & Medals",15,Color.WHITE);collection.setBackground(background(0x24ffffff,14));collection.setOnClickListener(v->openCommunityHub700("Collection"));body.addView(collection,new LinearLayout.LayoutParams(-1,dp(52)));setSafeContentView(root);
    }
    private TextView vipText540(String text,int size,int color){TextView t=new TextView(this);t.setText(text);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.CENTER);t.setPadding(dp(4),dp(4),dp(4),dp(4));return t;}
    private void familyAgencyPage(){
        screen="family_agency"; base("Family & Agency","Verified account data only");
        text("🏠 Family & Agency",24,Color.WHITE,true);
        text("The old device-only sample family members, fake host counts and fake active-room counts have been removed.",14,MUTED,false);
        if(firebaseAuth==null || firebaseAuth.getCurrentUser()==null || firestore==null){
            text("Sign in with Google/Firebase first. Family/agency membership will only be shown when it is backed by a real account record.",14,Color.WHITE,false);
            button("Sign in",PURPLE,this::login);
        } else {
            text("Signed in as: "+displayName,15,Color.WHITE,true);
            text("No verified Family/Agency record is available for this account yet.",14,MUTED,false);
            button("🎤 Open Party",PURPLE,this::openPartyActivity);
        }
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
        openPartyActivity();
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
    private void selectCosmetic730(boolean frame){LevelSystem.Snapshot pr=LevelSystem.read(this);String[] raw=frame?KingCosmetics.FRAMES:KingCosmetics.EFFECTS;String current=frame?KingCosmetics.frame(this):KingCosmetics.effect(this);String[] labels=new String[raw.length];int selected=0;for(int i=0;i<raw.length;i++){boolean ok=frame?KingCosmetics.unlockedFrame(raw[i],pr):KingCosmetics.unlockedEffect(raw[i],pr);String req=frame?KingCosmetics.requirementFrame(raw[i]):KingCosmetics.requirementEffect(raw[i]);labels[i]=(ok?"✓ ":"🔒 ")+raw[i]+"  •  "+req;if(raw[i].equals(current))selected=i;}final int picked=selected;new AlertDialog.Builder(this).setTitle(frame?"ID / Profile Frames":"Room Entry Effects").setSingleChoiceItems(labels,picked,(d,w)->{boolean ok=frame?KingCosmetics.unlockedFrame(raw[w],pr):KingCosmetics.unlockedEffect(raw[w],pr);if(!ok){Toast.makeText(this,"Locked • "+(frame?KingCosmetics.requirementFrame(raw[w]):KingCosmetics.requirementEffect(raw[w])),Toast.LENGTH_SHORT).show();return;}if(frame)KingCosmetics.setFrame(this,raw[w]);else KingCosmetics.setEffect(this,raw[w]);if(CloudSync.isSignedIn())syncPublicProfile();d.dismiss();backpackPage();}).setNegativeButton("Cancel",null).show();}
    private void selectCosmetic(String key,String[] values,Runnable refresh){int selected=0;String current=getPreferences(0).getString(key,values[0]);for(int i=0;i<values.length;i++)if(values[i].equals(current))selected=i;new AlertDialog.Builder(this).setTitle("Choose item").setSingleChoiceItems(values,selected,(d,w)->{getPreferences(0).edit().putString(key,values[w]).apply();d.dismiss();refresh.run();}).setNegativeButton("Cancel",null).show();}

    private void helpCenterPage(){screen="help";base("Help & Feedback","KING Plus support center");button("Login & OTP help",CARD,()->new AlertDialog.Builder(this).setTitle("Login & OTP").setMessage("FREE TEST MODE: enter a valid phone number and use OTP 123456. No SMS is sent. Production SMS OTP can be restored after Firebase provider setup.").setPositiveButton("OK",null).show());button("Voice room help",CARD,()->new AlertDialog.Builder(this).setTitle("Voice rooms").setMessage("Join a seat, allow microphone permission, then tap Mic. Live multi-user audio still requires a real-time voice service/backend.").setPositiveButton("OK",null).show());button("Send feedback",PURPLE,()->{final EditText e=new EditText(this);e.setHint("Describe the issue or suggestion");new AlertDialog.Builder(this).setTitle("Feedback").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{String m=e.getText().toString().trim();if(!m.isEmpty()){String old=getPreferences(0).getString("feedback","");getPreferences(0).edit().putString("feedback",m+"|||"+old).apply();Toast.makeText(this,"Feedback saved",Toast.LENGTH_SHORT).show();}}).show();});button("Back to Settings",CARD,this::settingsPage);}

    @Override public void onBackPressed() {
        if ("terms".equals(screen) || "login_help".equals(screen)) { login(); return; }
        if ("transactions".equals(screen)) { walletPage(); return; }
        if ("privacy".equals(screen) || "help".equals(screen)) { settingsPage(); return; }
        if ("game_history".equals(screen) || "game_stats".equals(screen)) { games(); return; }
        if ("moments".equals(screen) || "user_profile".equals(screen)) { discover(); return; }
        if ("editProfile".equals(screen) || "profile_about".equals(screen) || "profile_gallery".equals(screen)
                || "profile_videos".equals(screen) || "coupling".equals(screen) || "rankings".equals(screen)
                || "vip".equals(screen) || "family_agency".equals(screen) || "checkin".equals(screen)
                || "missions".equals(screen) || "wallet".equals(screen) || "gift_catalog".equals(screen)
                || "backpack".equals(screen) || "level".equals(screen) || "settings".equals(screen)
                || "cloud_safety".equals(screen) || "moderation".equals(screen) || "recharge".equals(screen)
                || "production_readiness".equals(screen) || "notifications".equals(screen)) { profile(); return; }
        if ("sub".equals(screen) || "compose".equals(screen)) { profile(); return; }
        if ("room".equals(screen)) { home(); return; }
        KingNav.confirmExit(this);
    }
    @Override protected void onPause() { super.onPause(); stopMic(); }
    @Override protected void onDestroy() { stopMic(); super.onDestroy(); }
    private void notificationsCenter(){
        screen="notifications"; base("Notifications","Real activity from your KING Plus account");
        String raw=getPreferences(0).getString("notifications_log","");
        if(raw.isEmpty()) {
            text("No notifications yet.",16,Color.WHITE,true);
            text("KING Plus no longer inserts sample follows, gifts, invites or live-room notifications.",13,MUTED,false);
        } else {
            for(String n:raw.split("[|][|][|]")) if(!n.trim().isEmpty()) button(n,CARD,()->Toast.makeText(this,"Opened",Toast.LENGTH_SHORT).show());
        }
        button("🔔 Enable / refresh push notifications",PURPLE,()->{PushNotifications.requestPermission(this);PushNotifications.refreshToken();Toast.makeText(this,"Push notification setup refreshed",Toast.LENGTH_SHORT).show();});
        button("Mark all as read",CARD,()->{getPreferences(0).edit().putBoolean("notifications_read",true).apply();Toast.makeText(this,"All notifications marked as read",Toast.LENGTH_SHORT).show();});
        if(!raw.isEmpty()) button("Clear notifications",CARD,()->{getPreferences(0).edit().putString("notifications_log","").apply();notificationsCenter();});
        button("Back to Messages",CARD,this::messages);
    }

    private void searchCommunity(){
        startActivity(new Intent(this,SocialActivity.class));
    }

    private void createRoomDialog(){
        // Legacy local preview creation is disabled. The real Party screen creates Firebase rooms.
        openPartyActivity();
    }

}
