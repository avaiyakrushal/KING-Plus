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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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
    private TextView profileFollowersNumber, profileFollowingNumber, profileFriendsNumber, profileTopBalance, profileWalletCoins, profileVisitorsChip940;
    // Live social counters use two subscriptions only while Profile is visible.
    private com.google.firebase.firestore.ListenerRegistration followingListener965,followersListener965;
    private com.google.firebase.firestore.ListenerRegistration meGalleryListener9993;
    private int meGalleryGeneration9994;
    private int visitorDialogGeneration10004;
    private int visitorCountGeneration10007;
    // The latest Me hosted-room request alone may update the visible list.
    private int hostedRoomsGeneration10014;
    private String renderedProfileUid10030;
    private String renderedWalletUid10030;
    private LinearLayout meHostedRoomsHost10018;
    private int meGalleryLimit9996=40;
    private LinearLayout meCloudGalleryHost9993;
    private final java.util.Set<String> liveFollowing965=new java.util.HashSet<>();
    private final java.util.Set<String> liveFollowers965=new java.util.HashSet<>();
    private boolean followingLoaded965,followersLoaded965;

    private GoogleSignInClient googleSignInClient;
    private String phoneVerificationId;
    private PhoneAuthProvider.ForceResendingToken phoneResendToken940;
    private AlertDialog otpDialog940;
    private boolean otpVerifyInFlight940;
    private boolean giftEffectsEnabled = true;
    private boolean muteAllSeats = false;
    private String selectedHomeCategory = "Hot";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        KingStability.install(this);
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(()->{
            if(isFinishing()||isDestroyed())return;
            Intent incoming959=getIntent();
            String incomingRoom959=KingRoomInvite959.parse(incoming959==null?null:incoming959.getDataString());
            if(!incomingRoom959.isEmpty())routePartyInvite959(incoming959);
            else if(firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null){
                String pending959=getPreferences(0).getString("pendingRoomInvite959","");
                if(!pending959.isEmpty())routePartyInvite959(new Intent().setData(
                    android.net.Uri.parse(KingRoomInvite959.link(pending959))));
            }
        },1250L);
        // Do not install an Activity-capturing global exception handler every launch.
        // KingApplication already owns the process-level fatal handler.
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
            () -> KingCrashWatch958.showPreviousExit(this), 1700L);
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this::showGlobalCrashDiagnostic940,900L);
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
        try{PushNotifications.initialize(this);}catch(Throwable pushInit940){KingStability.nonFatal(this,"push-init",pushInit940);}
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
            try{PushNotifications.refreshToken();}catch(Throwable pushToken940){KingStability.nonFatal(this,"push-token",pushToken940);}
            try{restoreCanonicalGoogleProfile931(signedInUser,displayName,false);}catch(Throwable identity940){KingStability.nonFatal(this,"identity-restore",identity940);}
        }
        String previousCrash940=getPreferences(0).getString("last_crash","");
        boolean forceLogin = getIntent() != null && getIntent().getBooleanExtra("forceLogin", false);
        try{
            if(forceLogin)login();
            else if(displayName.isEmpty() || firebaseAuth==null || firebaseAuth.getCurrentUser()==null)login();
            else{
                String action9981=getIntent()==null?null:getIntent().getStringExtra("meAction9981");
                int openTab=getIntent()==null?0:getIntent().getIntExtra("openTab",0);
                if(action9981!=null&&routeMeAction9981(action9981)){}
                else if(openTab==1)games();
                else if(openTab==2)discover();
                else if(openTab==3)messages();
                else if(openTab==4)profile();
                else home();
            }
        }catch(Throwable startupError940){
            String trace940=android.util.Log.getStackTraceString(startupError940);
            if(trace940.length()>3500)trace940=trace940.substring(0,3500);
            getPreferences(0).edit().putString("last_crash",trace940).apply();
            showStartupRecovery940(trace940);
            return;
        }
        if(!previousCrash940.isEmpty()){
            getPreferences(0).edit().remove("last_crash").apply();
            final String crash940=previousCrash940;
            new android.os.Handler(android.os.Looper.getMainLooper()).post(()->{
                if(isFinishing()||isDestroyed())return;
                new AlertDialog.Builder(this).setTitle("KING Plus recovered from a crash")
                    .setMessage(crash940).setPositiveButton("OK",null).show();
            });
        }
    }
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if(routePartyInvite959(intent))return;
        safeUiAction940("main-on-new-intent",()->{
            if (intent != null && intent.getBooleanExtra("forceLogin", false)) { login(); return; }
            String action9981=intent==null?null:intent.getStringExtra("meAction9981");
            int tab = intent == null ? 0 : intent.getIntExtra("openTab", 0);
            if(action9981!=null&&routeMeAction9981(action9981))return;
            if (tab == 1) games();
            else if (tab == 2) discover();
            else if (tab == 3) messages();
            else if (tab == 4) profile();
            else home();
        });
    }

    private void showStartupRecovery940(String detail){
        try{
            stopMic();
            LinearLayout root940=new LinearLayout(this);root940.setOrientation(LinearLayout.VERTICAL);root940.setGravity(Gravity.CENTER);root940.setPadding(dp(24),dp(40),dp(24),dp(40));root940.setBackgroundColor(0xff101323);
            TextView title940=new TextView(this);title940.setText("KING Plus recovery");title940.setTextSize(25);title940.setTypeface(null,Typeface.BOLD);title940.setTextColor(Color.WHITE);title940.setGravity(Gravity.CENTER);root940.addView(title940,new LinearLayout.LayoutParams(-1,dp(64)));
            TextView msg940=new TextView(this);msg940.setText("The previous screen could not open safely. Your account data has not been deleted.");msg940.setTextSize(14);msg940.setTextColor(0xffd6d0e0);msg940.setGravity(Gravity.CENTER);root940.addView(msg940,new LinearLayout.LayoutParams(-1,dp(90)));
            Button login940=new Button(this);login940.setText("Open Login Safely");login940.setAllCaps(false);login940.setOnClickListener(v->{try{login();}catch(Throwable e){Toast.makeText(this,"Login recovery failed",Toast.LENGTH_LONG).show();}});root940.addView(login940,new LinearLayout.LayoutParams(-1,dp(56)));
            Button resetUi940=new Button(this);resetUi940.setText("Reset screen state & retry");resetUi940.setAllCaps(false);resetUi940.setOnClickListener(v->{getPreferences(0).edit().remove("screen").apply();try{if(displayName==null||displayName.trim().isEmpty()||firebaseAuth==null||firebaseAuth.getCurrentUser()==null)login();else home();}catch(Throwable e){Toast.makeText(this,"Home still has an error; use Login Safely",Toast.LENGTH_LONG).show();}});LinearLayout.LayoutParams rp940=new LinearLayout.LayoutParams(-1,dp(56));rp940.setMargins(0,dp(10),0,0);root940.addView(resetUi940,rp940);
            TextView details940=new TextView(this);details940.setText("Crash details saved for diagnosis");details940.setTextSize(11);details940.setTextColor(0xff8f879c);details940.setGravity(Gravity.CENTER);root940.addView(details940,new LinearLayout.LayoutParams(-1,dp(50)));
            setContentView(root940);
        }catch(Throwable ignored940){
            try{login();}catch(Throwable ignoredAgain940){}
        }
    }

    private void showGlobalCrashDiagnostic940(){
        try{
            if(isFinishing()||isDestroyed())return;
            SharedPreferences p=getSharedPreferences("king_stability",MODE_PRIVATE);
            String crash=p.getString("lastCrash","");
            if(crash==null||crash.trim().isEmpty())return;
            long at=p.getLong("lastCrashAt",0L);
            String device=p.getString("device","");
            if(crash.length()>2200)crash=crash.substring(0,2200);
            final String message=(at>0?new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",java.util.Locale.US).format(new java.util.Date(at))+"\\n":"")+(device==null?"":device+"\\n\\n")+crash;
            p.edit().remove("lastCrash").remove("lastCrashAt").remove("lastCrashThread").apply();
            new AlertDialog.Builder(this).setTitle("KING Plus previous crash")
                .setMessage(message)
                .setPositiveButton("Continue",null)
                .setNeutralButton("Copy details",(d,w)->{try{android.content.ClipboardManager cm=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);cm.setPrimaryClip(android.content.ClipData.newPlainText("KING Plus crash",message));Toast.makeText(this,"Crash details copied",Toast.LENGTH_SHORT).show();}catch(Exception ignored){}})
                .show();
        }catch(Throwable e){KingStability.nonFatal(this,"show-global-crash",e);}
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
    private void safeUiAction940(String area,Runnable action){
        try{if(action!=null)action.run();}
        catch(Throwable e){KingStability.nonFatal(this,area,e);if(!isFinishing()&&!isDestroyed())Toast.makeText(this,"This action could not open safely. Please try again.",Toast.LENGTH_LONG).show();}
    }
    private Button button(String title, int color, Runnable action) {
        Button b = new Button(this); b.setText(title); b.setTextColor(Color.WHITE); b.setAllCaps(false);
        b.setTextSize(16); b.setBackground(background(color, 18));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(56)); p.setMargins(0, dp(7), 0, dp(7));
        page.addView(b,p);b.setOnClickListener(v->safeUiAction940("main-button:"+title,action));return b;
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
        root.setBackgroundColor(0xff0a0927);
        // Animated original, high-contrast royal login backdrop.
        KingLoginBackdropView backdrop=new KingLoginBackdropView(this);
        root.addView(backdrop,new android.widget.FrameLayout.LayoutParams(-1,-1));
        View shade=new View(this);
        shade.setBackgroundColor(0x1d030515);
        root.addView(shade,new android.widget.FrameLayout.LayoutParams(-1,-1));
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        page=new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setPadding(dp(24),dp(20),dp(24),dp(30));
        scroll.addView(page);
        root.addView(scroll,new android.widget.FrameLayout.LayoutParams(-1,-1));
        setSafeContentView(root);

        View heroSpacer=new View(this);
        page.addView(heroSpacer,new LinearLayout.LayoutParams(-1,dp(90)));

        ImageView logo=new ImageView(this);
        logo.setImageResource(R.drawable.king_logo_royal10021);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logo.setElevation(dp(8));
        LinearLayout.LayoutParams logoLp=new LinearLayout.LayoutParams(dp(114),dp(114));
        logoLp.gravity=Gravity.CENTER_HORIZONTAL;
        logoLp.bottomMargin=dp(5);
        page.addView(logo,logoLp);
        TextView heading=new TextView(this);
        heading.setText("👑 KING Plus");heading.setTextSize(35);
        heading.setTextColor(Color.WHITE);
        heading.setTypeface(android.graphics.Typeface.create("sans-serif-black",android.graphics.Typeface.BOLD));
        heading.setGravity(Gravity.CENTER);
        heading.setShadowLayer(10f,0,5,0xee05001f);
        page.addView(heading,new LinearLayout.LayoutParams(-1,dp(54)));
        TextView subtitle=new TextView(this);
        subtitle.setText("MEET  ✦  CHAT  ✦  PLAY  ✦  PARTY");
        subtitle.setTextColor(0xffffe6a5);subtitle.setTextSize(12);
        subtitle.setLetterSpacing(.08f);subtitle.setGravity(Gravity.CENTER);
        page.addView(subtitle,new LinearLayout.LayoutParams(-1,dp(27)));
        View gap=new View(this);page.addView(gap,new LinearLayout.LayoutParams(-1,dp(19)));

        LinearLayout outer=page;
        LinearLayout authCard=new LinearLayout(this);
        authCard.setOrientation(LinearLayout.VERTICAL);
        authCard.setPadding(dp(16),dp(18),dp(16),dp(14));
        android.graphics.drawable.GradientDrawable card=new android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
            new int[]{0xee281644,0xee151639,0xf02b1048});
        card.setCornerRadius(dp(24));
        card.setStroke(dp(1),0x88ffdd98);
        authCard.setBackground(card);
        authCard.setElevation(dp(8));
        LinearLayout.LayoutParams cardLp=new LinearLayout.LayoutParams(-1,-2);
        cardLp.leftMargin=dp(1);cardLp.rightMargin=dp(1);
        outer.addView(authCard,cardLp);
        page=authCard;
        TextView welcome=text("WELCOME TO KING",19,Color.WHITE,true);
        welcome.setGravity(Gravity.CENTER);
        TextView hint=text("Choose how you'd like to sign in",12,0xffe1cfee,false);
        hint.setGravity(Gravity.CENTER);
        button("G  Continue with Google / Gmail",0xff2569db,this::googleLogin);
        button("f  Facebook • setup required",0xff2452a8,
              ()->socialProviderSetupRequired("Facebook"));
        button("📱  Continue with Mobile Number",0xffa72da7,this::mobileLogin);
        page=outer;

        TextView terms=new TextView(this);
        terms.setText("Terms & Privacy  •  Trouble logging in?");
        terms.setContentDescription("Terms of service, privacy policy, and login help");
        terms.setFocusable(true);
        terms.setTextColor(0xfff5e5f7);terms.setTextSize(12);
        terms.setGravity(Gravity.CENTER);terms.setPadding(0,dp(16),0,dp(8));
        terms.setOnClickListener(v->new android.app.AlertDialog.Builder(this)
              .setItems(new String[]{"Terms & Privacy","Trouble logging in?"},(d,w)->{
                   if(w==0)termsPrivacyPage();else troubleLoginPage();
              }).show());
        outer.addView(terms,new LinearLayout.LayoutParams(-1,-2));
        TextView legal=new TextView(this);
        legal.setText("Google sign-in ready • Mobile OTP and Facebook need secure provider setup.");
        legal.setTextColor(0xffc3b7de);legal.setTextSize(11);
        legal.setGravity(Gravity.CENTER);
        outer.addView(legal,new LinearLayout.LayoutParams(-1,-2));
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
        text("Mobile OTP", 18, Color.WHITE, true);
        text("Mobile OTP needs a configured low-cost Cloudflare Worker and approved SMS provider. It is not a free TEST OTP, and Firebase SMS/Blaze is not required.", 15, MUTED, false);
        text("Google", 18, Color.WHITE, true);
        text("Google error 10 means the APK signing SHA-1 is not registered for the Firebase/Google OAuth Android client.", 15, MUTED, false);
        text("Facebook / WhatsApp OTP", 18, Color.WHITE, true);
        text("Facebook needs Meta/Firebase provider setup. WhatsApp OTP needs an approved provider/backend.", 15, MUTED, false);
        button("Run login diagnostics", CARD, this::authDiagnostics910);
        button("Back to Login", PURPLE, this::login);
    }

    private void authDiagnostics910(){
        String project="unavailable";try{project=String.valueOf(com.google.firebase.FirebaseApp.getInstance().getOptions().getProjectId());}catch(Exception ignored){}
        int googleId=getResources().getIdentifier("default_web_client_id","string",getPackageName());
        int facebookId=getResources().getIdentifier("facebook_app_id","string",getPackageName());
        boolean signed=firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null;String line=System.lineSeparator();
        String text="Firebase project: "+project+line+"Firebase signed in: "+(signed?"YES":"NO")+line+"Google OAuth client: "+(googleId!=0?"present":"missing")+line+"Facebook App ID resource: "+(facebookId!=0?"present":"missing")+line+"Secure custom-token Mobile OTP: setup needed"+line+"WhatsApp OTP provider: not configured";
        new AlertDialog.Builder(this).setTitle("Login diagnostics").setMessage(text).setPositiveButton("Test mobile login",(d,w)->mobileLogin()).setNeutralButton("Google login",(d,w)->googleLogin()).setNegativeButton("Close",null).show();
    }
    private void shareDiagnostics910(){
        try{java.io.File f=KingStability.logFile(this);if(f==null||!f.exists()){Toast.makeText(this,"No crash diagnostics recorded",Toast.LENGTH_SHORT).show();return;}String raw=new String(java.nio.file.Files.readAllBytes(f.toPath()),java.nio.charset.StandardCharsets.UTF_8);if(raw.length()>12000)raw=raw.substring(raw.length()-12000);Intent send=new Intent(Intent.ACTION_SEND);send.setType("text/plain");send.putExtra(Intent.EXTRA_TEXT,"KING Plus diagnostics"+System.lineSeparator()+raw);startActivity(Intent.createChooser(send,"Share diagnostics"));}catch(Exception e){Toast.makeText(this,"Diagnostics could not be shared",Toast.LENGTH_SHORT).show();}
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
            restoreCanonicalGoogleProfile931(user, fallbackName, true);
        });
    }
    private void mobileLogin() {
        startActivity(new Intent(this,KingPhoneOtp975Activity.class));
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
                    phoneResendToken940 = token;
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
        if(otpDialog940!=null&&otpDialog940.isShowing())otpDialog940.dismiss();
        final EditText otp = new EditText(this); otp.setHint("6-digit OTP"); otp.setSingleLine(true);otp.setMaxLines(1);
        otp.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        otpDialog940=new AlertDialog.Builder(this).setTitle("Verify " + number).setMessage("Enter the 6-digit SMS OTP. You can resend if it does not arrive.")
            .setView(otp).setNegativeButton("Cancel",(d,w)->{otpVerifyInFlight940=false;otpDialog940=null;})
            .setNeutralButton("Resend OTP",null).setPositiveButton("Verify",null).create();
        otpDialog940.setOnShowListener(x->{
            Button verify=otpDialog940.getButton(AlertDialog.BUTTON_POSITIVE);
            Button resend=otpDialog940.getButton(AlertDialog.BUTTON_NEUTRAL);
            verify.setOnClickListener(v->{
                if(otpVerifyInFlight940)return;
                String code=otp.getText().toString().trim();
                if(phoneVerificationId==null||!code.matches("[0-9]{6}")){otp.setError("Enter the 6-digit OTP");return;}
                otpVerifyInFlight940=true;verify.setEnabled(false);verify.setText("Verifying…");
                signInWithPhoneCredential(PhoneAuthProvider.getCredential(phoneVerificationId,code),number);
            });
            resend.setOnClickListener(v->{if(!otpVerifyInFlight940)resendRealOtp940(number,resend);});
        });
        otpDialog940.setOnDismissListener(d->{if(otpDialog940!=null&&!otpVerifyInFlight940)otpDialog940=null;});
        otpDialog940.show();
    }
    private void resendRealOtp940(String number,Button resend){
        if(firebaseAuth==null||phoneResendToken940==null){Toast.makeText(this,"Resend is not ready yet",Toast.LENGTH_SHORT).show();return;}
        resend.setEnabled(false);resend.setText("Sending…");
        try{
            PhoneAuthOptions options=PhoneAuthOptions.newBuilder(firebaseAuth).setPhoneNumber(number).setTimeout(60L,TimeUnit.SECONDS).setActivity(this)
                .setForceResendingToken(phoneResendToken940)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                    @Override public void onVerificationCompleted(PhoneAuthCredential credential){signInWithPhoneCredential(credential,number);}
                    @Override public void onVerificationFailed(com.google.firebase.FirebaseException e){runOnUiThread(()->{resend.setEnabled(true);resend.setText("Resend OTP");showPhoneError(e.getLocalizedMessage());});}
                    @Override public void onCodeSent(String verificationId,PhoneAuthProvider.ForceResendingToken token){phoneVerificationId=verificationId;phoneResendToken940=token;runOnUiThread(()->{resend.setEnabled(true);resend.setText("Resend OTP");Toast.makeText(MainActivity.this,"New OTP sent",Toast.LENGTH_SHORT).show();});}
                }).build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        }catch(RuntimeException e){resend.setEnabled(true);resend.setText("Resend OTP");showPhoneError(e.getLocalizedMessage());}
    }
    private void signInWithPhoneCredential(PhoneAuthCredential credential, String number) {
        if(firebaseAuth==null){otpVerifyInFlight940=false;showPhoneError("Firebase Authentication is unavailable.");return;}
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (!task.isSuccessful()) {
                otpVerifyInFlight940=false;
                if(otpDialog940!=null&&otpDialog940.isShowing()){Button verify=otpDialog940.getButton(AlertDialog.BUTTON_POSITIVE);if(verify!=null){verify.setEnabled(true);verify.setText("Verify");}}
                Toast.makeText(this, "OTP verification failed: " + (task.getException() == null ? "Invalid OTP" : task.getException().getMessage()), Toast.LENGTH_LONG).show();
                return;
            }
            otpVerifyInFlight940=false;
            if(otpDialog940!=null&&otpDialog940.isShowing())otpDialog940.dismiss();otpDialog940=null;
            String shortNumber = number.length() > 4 ? "KING " + number.substring(number.length() - 4) : "KING User";
            FirebaseUser phoneUser940=firebaseAuth.getCurrentUser();
            if(phoneUser940!=null&&firestore!=null){
                firestore.collection("public_profiles").document(phoneUser940.getUid()).get().addOnSuccessListener(doc->{
                    if(doc!=null&&doc.exists()){getPreferences(0).edit().putBoolean("profile_complete_"+phoneUser940.getUid(),true).apply();saveLocalSession(shortNumber,"Mobile");}
                    else showFirstProfileSetup940(phoneUser940,shortNumber,"",true);
                }).addOnFailureListener(e->saveLocalSession(shortNumber,"Mobile"));
            }else if(phoneUser940!=null&&!getPreferences(0).getBoolean("profile_complete_"+phoneUser940.getUid(),false))showFirstProfileSetup940(phoneUser940,shortNumber,"",true);
            else saveLocalSession(shortNumber,"Mobile");
        });
    }
    private void showFirstProfileSetup940(FirebaseUser user,String suggestedName,String photoUrl,boolean navigateHome){
        if(user==null){if(navigateHome)home();return;}
        screen="profile_setup";stopMic();
        final String[] selectedGender940={getPreferences(0).getString("gender","Male")};
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.WHITE);root.setPadding(dp(22),dp(32),dp(22),dp(26));
        TextView skipTitle940=new TextView(this);skipTitle940.setText("Complete your profile");skipTitle940.setTextSize(20);skipTitle940.setTypeface(null,Typeface.BOLD);skipTitle940.setTextColor(0xff222222);skipTitle940.setGravity(Gravity.CENTER);root.addView(skipTitle940,new LinearLayout.LayoutParams(-1,dp(46)));

        android.widget.FrameLayout avatarWrap940=new android.widget.FrameLayout(this);TextView avatar940=new TextView(this);String n0=(suggestedName==null||suggestedName.trim().isEmpty())?"K":suggestedName.trim().substring(0,1).toUpperCase();avatar940.setText(n0);avatar940.setTextSize(34);avatar940.setTypeface(null,Typeface.BOLD);avatar940.setTextColor(Color.WHITE);avatar940.setGravity(Gravity.CENTER);avatar940.setBackground(background(0xff27a9ad,52));avatarWrap940.addView(avatar940,new android.widget.FrameLayout.LayoutParams(-1,-1));LinearLayout.LayoutParams avp940=new LinearLayout.LayoutParams(dp(104),dp(104));avp940.gravity=Gravity.CENTER_HORIZONTAL;avp940.setMargins(0,dp(8),0,dp(18));root.addView(avatarWrap940,avp940);

        EditText name940=new EditText(this);name940.setHint("Your name");name940.setSingleLine(true);name940.setText(suggestedName==null?"":suggestedName);name940.setTextColor(0xff222222);name940.setHintTextColor(0xff9a9a9a);name940.setPadding(dp(14),0,dp(14),0);name940.setBackground(background(0xfff5f5f7,10));root.addView(name940,new LinearLayout.LayoutParams(-1,dp(54)));

        TextView sexLabel940=new TextView(this);sexLabel940.setText("Your sex");sexLabel940.setTextSize(14);sexLabel940.setTypeface(null,Typeface.BOLD);sexLabel940.setTextColor(0xff555555);sexLabel940.setPadding(dp(2),dp(18),0,dp(8));root.addView(sexLabel940,new LinearLayout.LayoutParams(-1,dp(52)));
        LinearLayout genders940=new LinearLayout(this);genders940.setGravity(Gravity.CENTER);
        TextView male940=new TextView(this);male940.setText("♂  Male");male940.setTextSize(14);male940.setGravity(Gravity.CENTER);male940.setTypeface(null,Typeface.BOLD);
        TextView female940=new TextView(this);female940.setText("♀  Female");female940.setTextSize(14);female940.setGravity(Gravity.CENTER);female940.setTypeface(null,Typeface.BOLD);
        Runnable refreshGender940=()->{boolean male="Male".equals(selectedGender940[0]);male940.setTextColor(male?0xff3f67dc:0xff777777);female940.setTextColor(male?0xff777777:0xffd85ba2);male940.setBackground(background(male?0xffeef2ff:0xfff5f5f5,10));female940.setBackground(background(male?0xfff5f5f5:0xffffeef7,10));};
        male940.setOnClickListener(v->{selectedGender940[0]="Male";refreshGender940.run();});female940.setOnClickListener(v->{selectedGender940[0]="Female";refreshGender940.run();});refreshGender940.run();
        LinearLayout.LayoutParams gp940=new LinearLayout.LayoutParams(0,dp(54),1);gp940.setMargins(dp(4),0,dp(4),0);genders940.addView(male940,gp940);genders940.addView(female940,new LinearLayout.LayoutParams(gp940));root.addView(genders940,new LinearLayout.LayoutParams(-1,dp(58)));

        android.widget.Space flex940=new android.widget.Space(this);root.addView(flex940,new LinearLayout.LayoutParams(-1,0,1));
        TextView cont940=new TextView(this);cont940.setText("Continue");cont940.setTextSize(16);cont940.setTypeface(null,Typeface.BOLD);cont940.setTextColor(0xff171717);cont940.setGravity(Gravity.CENTER);cont940.setBackground(background(0xffffe500,10));
        cont940.setOnClickListener(v->{String name=name940.getText().toString().trim();if(name.isEmpty()){name940.setError("Name required");return;}displayName=name;SharedPreferences.Editor ed=getPreferences(0).edit().putString("name",name).putString("gender",selectedGender940[0]).putString("firebase_uid",user.getUid()).putBoolean("profile_complete_"+user.getUid(),true);if(photoUrl!=null&&!photoUrl.trim().isEmpty())ed.putString("profile_photo_cloud",photoUrl.trim());ed.apply();syncPublicProfile();if(navigateHome)home();});
        root.addView(cont940,new LinearLayout.LayoutParams(-1,dp(56)));setContentView(root);
    }

    private void recordLoginHistory940(String provider){
        try{
            SharedPreferences sec=getSharedPreferences("king_security",MODE_PRIVATE);
            String old=sec.getString("login_history","");
            String device=(android.os.Build.MANUFACTURER+" "+android.os.Build.MODEL).trim().replace("::"," ");
            String p=provider==null||provider.trim().isEmpty()?"Unknown":provider.trim().replace("::"," ");
            String entry=String.format(java.util.Locale.US,"%013d::%s::%s",System.currentTimeMillis(),p,device);
            ArrayList<String> rows=new ArrayList<>();rows.add(entry);
            if(old!=null&&!old.isEmpty())for(String x:old.split("\\|\\|\\|"))if(x!=null&&!x.trim().isEmpty()&&rows.size()<12)rows.add(x);
            StringBuilder out=new StringBuilder();for(String x:rows){if(out.length()>0)out.append("|||");out.append(x);}
            sec.edit().putString("login_history",out.toString()).putLong("last_login_at",System.currentTimeMillis()).putString("last_provider",p).apply();
        }catch(Throwable e){KingStability.nonFatal(this,"login-history",e);}
    }

    private void saveLocalSession(String name, String provider) {
        displayName=name; getPreferences(0).edit().putString("name",name).putString("login_provider",provider).apply();
        recordLoginHistory940(provider);
        if (CloudSync.isSignedIn()) {
            PushNotifications.refreshToken();
            restoreCloudCosmeticsThenSync();
        }
        home();
    }

    private void clearCrossAccountIdentity931(String uid){
        SharedPreferences p=getPreferences(0);String previous=p.getString("firebase_uid","");
        if(previous!=null&&!previous.isEmpty()&&!previous.equals(uid)){
            p.edit().remove("name").remove("profile_photo").remove("profile_photo_cloud").remove("bio").remove("tags").remove("hometown").remove("birthday").remove("gender").apply();
            displayName="";
        }
    }
    private String googleName931(FirebaseUser user,String fallback){
        String n=user==null?null:user.getDisplayName();
        if(n==null||n.trim().isEmpty())n=fallback;
        if((n==null||n.trim().isEmpty())&&user!=null&&user.getEmail()!=null&&user.getEmail().contains("@"))n=user.getEmail().substring(0,user.getEmail().indexOf('@'));
        if(n==null||n.trim().isEmpty())n=user==null?"KING User":"KING "+publicId(user.getUid());
        return n.trim();
    }
    // Account A's late Firebase reply must never overwrite account B's Me profile.
    private boolean currentGoogleIdentity10045(FirebaseUser expected){
        FirebaseUser now=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
        return expected!=null&&now!=null&&expected.getUid().equals(now.getUid())
            &&!isFinishing()&&!isDestroyed();
    }
    private void restoreCanonicalGoogleProfile931(FirebaseUser user,String fallbackName,boolean navigateHome){
        if(user==null)return;
        clearCrossAccountIdentity931(user.getUid());
        final SharedPreferences identityPrefs=getPreferences(0);
        final boolean sameAccount=user.getUid().equals(identityPrefs.getString("firebase_uid",""));
        final String localName=sameAccount?identityPrefs.getString("name",""):"";
        final String localPhoto=sameAccount?identityPrefs.getString("profile_photo_cloud",""):"";
        final String googleName=googleName931(user,fallbackName);
        final String googlePhoto=user.getPhotoUrl()==null?"":user.getPhotoUrl().toString();
        final String safeLocalName=localName==null?"":localName.trim();
        final String safeLocalPhoto=localPhoto==null?"":localPhoto.trim();
        if(firestore==null){
            applyCanonicalIdentity931(user,safeLocalName.isEmpty()?googleName:safeLocalName,safeLocalPhoto.isEmpty()?googlePhoto:safeLocalPhoto,null);
            finishGoogleIdentity931(navigateHome,false);return;
        }
        firestore.collection("public_profiles").document(user.getUid()).get()
            .addOnSuccessListener(doc->{
                if(!currentGoogleIdentity10045(user))return;
                boolean existing=doc!=null&&doc.exists();
                String canonicalName=existing?doc.getString("displayName"):null;
                if(canonicalName==null||canonicalName.trim().isEmpty())canonicalName=safeLocalName.isEmpty()?googleName:safeLocalName;
                String canonicalPhoto=existing?doc.getString("photoUrl"):null;
                if(canonicalPhoto==null||canonicalPhoto.trim().isEmpty())canonicalPhoto=safeLocalPhoto.isEmpty()?googlePhoto:safeLocalPhoto;
                applyCanonicalIdentity931(user,canonicalName,canonicalPhoto,doc);
                if(!existing){showFirstProfileSetup940(user,canonicalName,canonicalPhoto,navigateHome);return;}
                getPreferences(0).edit().putBoolean("profile_complete_"+user.getUid(),true).apply();
                finishGoogleIdentity931(navigateHome,true);
            })
            .addOnFailureListener(e->{
                if(!currentGoogleIdentity10045(user))return;
                applyCanonicalIdentity931(user,safeLocalName.isEmpty()?googleName:safeLocalName,safeLocalPhoto.isEmpty()?googlePhoto:safeLocalPhoto,null);
                KingStability.nonFatal(this,"canonical-profile-read",e);finishGoogleIdentity931(navigateHome,false);
            });
    }
    private void applyCanonicalIdentity931(FirebaseUser user,String canonicalName,String canonicalPhoto,com.google.firebase.firestore.DocumentSnapshot doc){
        if(!currentGoogleIdentity10045(user))return;
        displayName=canonicalName==null||canonicalName.trim().isEmpty()?googleName931(user,null):canonicalName.trim();
        SharedPreferences.Editor e=getPreferences(0).edit().putString("name",displayName).putString("login_provider","Google").putString("firebase_uid",user.getUid());
        if(user.getEmail()!=null)e.putString("google_email",user.getEmail());
        if(user.getPhotoUrl()!=null)e.putString("google_photo_url",user.getPhotoUrl().toString());
        if(canonicalPhoto!=null&&!canonicalPhoto.trim().isEmpty())e.putString("profile_photo_cloud",canonicalPhoto.trim());
        if(doc!=null&&doc.exists()){
            String bio=doc.getString("bio"),tags=doc.getString("tags"),home=doc.getString("hometown"),birthday=doc.getString("birthday");
            for(String field10067:new String[]{"bio","tags","hometown","birthday","gender"}){
                Object raw10067=doc.get(field10067);String value10067=raw10067 instanceof String?(String)raw10067:"";
                e.putString(field10067,value10067).putString(field10067+"_"+user.getUid(),value10067);
            }
            String frame=doc.getString("equippedFrame"),effect=doc.getString("entranceEffect");
            if(frame!=null&&!frame.trim().isEmpty())KingCosmetics.setFrame(this,frame.trim());
            if(effect!=null&&!effect.trim().isEmpty())KingCosmetics.setEffect(this,effect.trim());
        }
        e.apply();
        try{
            com.google.firebase.auth.UserProfileChangeRequest.Builder b=new com.google.firebase.auth.UserProfileChangeRequest.Builder().setDisplayName(displayName);
            if(canonicalPhoto!=null&&!canonicalPhoto.trim().isEmpty())b.setPhotoUri(android.net.Uri.parse(canonicalPhoto.trim()));
            user.updateProfile(b.build()).addOnFailureListener(x->{});
        }catch(Exception ignored){}
    }
    private void finishGoogleIdentity931(boolean navigateHome,boolean cloudRestored){
        recordLoginHistory940("Google");
        try{PushNotifications.refreshToken();}catch(Exception ignored){}
        try{CloudSync.syncProfileAndTestWallet(this,getPreferences(0),displayName,coinBalance,giftCount,receivedGiftCount,(ok,message)->{});}catch(Exception ignored){}
        if(navigateHome){Toast.makeText(this,cloudRestored?"Google login • KING profile restored":"Google login successful",Toast.LENGTH_SHORT).show();home();}
        else if("profile".equals(screen))profile();
    }
    private FrameLayout liveMeAvatar10067, editPhotoSlot10067;
    private String liveMeAvatarUrl10067="";
    private TextView photoStatus10067;
    private boolean photoUploadPending10067;
    private final java.util.concurrent.ThreadPoolExecutor profileImages10067=new java.util.concurrent.ThreadPoolExecutor(
        1,1,0L,java.util.concurrent.TimeUnit.MILLISECONDS,new java.util.concurrent.ArrayBlockingQueue<>(3),
        new java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy());
    private void loadRemoteAvatar931(ImageView image,TextView fallback,String url){
        if(url==null||!url.trim().startsWith("https://")||profileImages10067.isShutdown())return;
        final String source=url.trim();
        final String account10067=firebaseAuth==null||firebaseAuth.getCurrentUser()==null?"":firebaseAuth.getCurrentUser().getUid();
        image.setTag(source);
        profileImages10067.execute(()->{
            try {
                java.net.URLConnection connection=new java.net.URL(source).openConnection();
                connection.setConnectTimeout(8000);connection.setReadTimeout(8000);
                if(connection.getContentLengthLong()>3L*1024*1024)return;
                byte[] data;
                try(java.io.InputStream input=connection.getInputStream();java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream()){
                    byte[] block=new byte[8192];int n,total=0;
                    while((n=input.read(block))!=-1){total+=n;if(total>3*1024*1024)return;bytes.write(block,0,n);}data=bytes.toByteArray();
                }
                android.graphics.BitmapFactory.Options options=new android.graphics.BitmapFactory.Options();options.inJustDecodeBounds=true;
                android.graphics.BitmapFactory.decodeByteArray(data,0,data.length,options);
                if(options.outWidth<1||options.outHeight<1)return;
                options.inSampleSize=1;
                while(options.outWidth/options.inSampleSize>640||options.outHeight/options.inSampleSize>640)options.inSampleSize*=2;
                options.inJustDecodeBounds=false;
                android.graphics.Bitmap bitmap=android.graphics.BitmapFactory.decodeByteArray(data,0,data.length,options);
                if(bitmap!=null)runOnUiThread(()->{
                    FirebaseUser now=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
                    if(!isFinishing()&&!isDestroyed()&&source.equals(image.getTag())&&now!=null&&account10067.equals(now.getUid())){
                        image.setImageBitmap(bitmap);if(fallback!=null)fallback.setVisibility(View.GONE);
                    }else bitmap.recycle();
                });
            }catch(Exception|OutOfMemoryError ignored){}
        });
    }

    private View profileAvatarView931(String localPhoto,String remotePhoto){
        FrameLayout box=new FrameLayout(this);TextView fallback=new TextView(this);fallback.setText("👑");fallback.setTextSize(39);fallback.setGravity(Gravity.CENTER);fallback.setBackground(background(0xffffefd1,44));box.addView(fallback,new FrameLayout.LayoutParams(-1,-1));
        ImageView img=new ImageView(this);img.setScaleType(ImageView.ScaleType.CENTER_CROP);img.setBackground(background(0x00ffffff,44));box.addView(img,new FrameLayout.LayoutParams(-1,-1));
        if(localPhoto!=null&&!localPhoto.trim().isEmpty()&&!profileImages10067.isShutdown()){
            final String local10067=localPhoto.trim();
            final String owner10067=firebaseAuth==null||firebaseAuth.getCurrentUser()==null?"":firebaseAuth.getCurrentUser().getUid();
            img.setTag("local:"+local10067);
            profileImages10067.execute(()->{
                android.graphics.Bitmap decoded10067=null;
                try{byte[] bytes10067=KingProfilePhoto10067.prepare(getApplicationContext(),Uri.parse(local10067));
                    decoded10067=android.graphics.BitmapFactory.decodeByteArray(bytes10067,0,bytes10067.length);
                }catch(Exception|OutOfMemoryError ignored){}
                final android.graphics.Bitmap result10067=decoded10067;
                runOnUiThread(()->{
                    FirebaseUser actual10067=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
                    if(isFinishing()||isDestroyed()||actual10067==null||!owner10067.equals(actual10067.getUid())||!("local:"+local10067).equals(img.getTag())){
                        if(result10067!=null)result10067.recycle();return;
                    }
                    if(result10067!=null){img.setImageBitmap(result10067);fallback.setVisibility(View.GONE);}
                    else loadRemoteAvatar931(img,fallback,remotePhoto);
                });
            });
        }else if(remotePhoto!=null&&!remotePhoto.trim().isEmpty())loadRemoteAvatar931(img,fallback,remotePhoto);
        return box;
    }

    private void restoreCloudCosmeticsThenSync() {
        if (firestore == null || firebaseAuth == null || firebaseAuth.getCurrentUser() == null) { syncPublicProfile(); return; }
        FirebaseUser me = firebaseAuth.getCurrentUser();
        firestore.collection("public_profiles").document(me.getUid()).get()
            .addOnSuccessListener(doc -> {
                if(!currentGoogleIdentity10045(me))return;
                if (doc.exists()) {
                    String frame = doc.getString("equippedFrame");
                    String effect = doc.getString("entranceEffect");
                    if (frame != null && !frame.trim().isEmpty()) KingCosmetics.setFrame(this, frame.trim());
                    if (effect != null && !effect.trim().isEmpty()) KingCosmetics.setEffect(this, effect.trim());
                }
                syncPublicProfile();
            })
            .addOnFailureListener(e -> {if(currentGoogleIdentity10045(me))syncPublicProfile();});
    }

    private void syncPublicProfile() {
        if (firestore == null || firebaseAuth == null || firebaseAuth.getCurrentUser() == null) return;
        FirebaseUser me = firebaseAuth.getCurrentUser();
        boolean ownLocal10039=me.getUid().equals(getPreferences(0).getString("firebase_uid",""));
        String name = ownLocal10039?displayName:me.getDisplayName();
        if (name == null || name.trim().isEmpty()) name = me.getDisplayName();
        if (name == null || name.trim().isEmpty()) name = "KING " + publicId(me.getUid());
        String bio = getPreferences(0).getString("bio","Love music, games and new friends ✨");
        String tags = getPreferences(0).getString("tags","Music, Games");
        Map<String,Object> profile = new HashMap<>();
        profile.put("uid", me.getUid());
        profile.put("displayName", name.trim());
        profile.put("searchName", name.trim().toLowerCase(java.util.Locale.US));
        if(ownLocal10039)profile.put("bio", bio == null ? "" : bio.trim());
        if(ownLocal10039)profile.put("tags", tags == null ? "" : tags.trim());
        // Issued KING ID belongs to the identity provisioning flow; a periodic
        // profile sync must never replace it with a UID-derived guess.
        if(ownLocal10039)profile.put("hometown", getPreferences(0).getString("hometown",""));
        if(ownLocal10039)profile.put("birthday", getPreferences(0).getString("birthday",""));
        if(ownLocal10039)profile.put("gender", getPreferences(0).getString("gender","Not specified"));
        String cloudPhoto868=ownLocal10039?getPreferences(0).getString("profile_photo_cloud",""):"";
        if(cloudPhoto868!=null&&!cloudPhoto868.trim().isEmpty())profile.put("photoUrl",cloudPhoto868.trim());
        else if(me.getPhotoUrl()!=null)profile.put("photoUrl",me.getPhotoUrl().toString());
        // v10.0.48: Activity XP and cosmetics are not account-scoped in local
        // preferences. Only explicit server-owned progression/cosmetic changes
        // may update those fields; a routine profile refresh must not erase the
        // signed-in account's real level, frame or purchased entrance effect.
        profile.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
        final String nameForRefresh10067=name;
        final com.google.firebase.firestore.DocumentReference canonical10067=firestore.collection("public_profiles").document(me.getUid());
        firestore.runTransaction(transaction10067->{
            DocumentSnapshot existing10067=transaction10067.get(canonical10067);
            if(!existing10067.exists())transaction10067.set(canonical10067,profile,com.google.firebase.firestore.SetOptions.merge());
            return existing10067;
        }).addOnSuccessListener(v->{if(currentGoogleIdentity10045(me)){
                if(v.exists()){
                    String n10067=v.getString("displayName"),p10067=v.getString("photoUrl");
                    applyCanonicalIdentity931(me,n10067==null?nameForRefresh10067:n10067,p10067==null?"":p10067,v);
                }
                getSharedPreferences("king_social_sync_996",MODE_PRIVATE)
                    .edit().putString("uid",me.getUid()).putString("state","SYNCED")
                    .putString("error","").putLong("confirmedAt",System.currentTimeMillis()).apply();}})
            .addOnFailureListener(error->{if(currentGoogleIdentity10045(me))
                getSharedPreferences("king_social_sync_996",MODE_PRIVATE)
                    .edit().putString("uid",me.getUid()).putString("state","FAILED")
                    .putString("error",String.valueOf(error.getLocalizedMessage())).apply();});
    }

    private String publicId(String uid){return KingSocialIdentity965.publicId(uid);}
    private void openCommunityHub700(String tab) { Intent i=new Intent(this,CommunityHubActivity.class);i.putExtra("tab",tab);startActivity(i); }
    private void openCommunityTarget700(String tab,String uid,String name){Intent i=new Intent(this,CommunityHubActivity.class);i.putExtra("tab",tab);i.putExtra("targetUid",uid);i.putExtra("targetName",name);startActivity(i);}

    private boolean routePartyInvite959(Intent input959){
        String id959=KingRoomInvite959.parse(input959==null?null:input959.getDataString());
        if(id959.isEmpty())return false;
        if(firebaseAuth==null||firebaseAuth.getCurrentUser()==null){
            getPreferences(0).edit().putString("pendingRoomInvite959",id959).apply();
            Toast.makeText(this,"Sign in to KING Plus, then open the Party invite again.",Toast.LENGTH_LONG).show();
            login();
            return true;
        }
        getPreferences(0).edit().remove("pendingRoomInvite959").apply();
        KingCrashWatch958.mark(this,"main-open-party-invite-959");
        Intent party959=new Intent(this,PartyActivity.class);
        party959.putExtra("directRoomId",id959);
        startActivity(party959);
        return true;
    }

    private void openPartyActivity() {
        KingNav.openRoot(this,0);
    }

    private void home() {
        // There is now only one Party UI in the app. MainActivity never renders a second Party page.
        openPartyActivity();
    }

    private void simplePage(String title, String message){ screen="sub"; base(title,null); text(message,18,Color.WHITE,true); button("પાછા Party પર જાઓ",PURPLE,this::home); }
    // v9.9.91: Me's composer publishes to the SAME shared Moments stream as Home/Community.
    // Pre-9.9.91 device-only posts remain in the legacy Gallery below, never silently deleted.
    private void composePost() {
        screen = "compose"; selectedPhoto = null; base("નવી પોસ્ટ", "બંને ફોનમાં દેખાય તેવી પોસ્ટ શેર કરો");
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
        TextView status=text("આ પોસ્ટ તમારા KING Plus એકાઉન્ટમાં સેવ થશે.",13,MUTED,false);
        Button publish=button("પોસ્ટ શેર કરો", PURPLE, () -> {});
        publish.setOnClickListener(v -> {
            String body=caption.getText().toString().trim();
            if(body.length()>1000){caption.setError("1000 અક્ષરથી ઓછી પોસ્ટ લખો");return;}
            final Uri photo=selectedPhoto;
            if(body.isEmpty()&&photo==null){caption.setError("લખાણ અથવા ફોટો ઉમેરો");return;}
            FirebaseUser me=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
            if(me==null||firestore==null){status.setText("પોસ્ટ માટે ફરી લોગીન કરો.");return;}
            final String uid=me.getUid();
            publish.setEnabled(false);status.setText("પોસ્ટ અપલોડ થઈ રહી છે…");
            final String momentText=body.isEmpty()?"📷 Photo":body;
            java.util.function.Consumer<String> save=photoUrl->{
                if(isFinishing()||isDestroyed())return;
                FirebaseUser current=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
                if(current==null||!uid.equals(current.getUid())){publish.setEnabled(true);status.setText("એકાઉન્ટ બદલાયું. ફરી પ્રયાસ કરો.");return;}
                Map<String,Object> moment=new HashMap<>();
                moment.put("uid",uid);moment.put("name",displayName==null||displayName.trim().isEmpty()?"KING User":displayName);
                moment.put("text",momentText);moment.put("topic","General");
                moment.put("createdAt",com.google.firebase.firestore.FieldValue.serverTimestamp());
                if(photoUrl!=null&&!photoUrl.isEmpty())moment.put("photoUrl",photoUrl);
                firestore.collection("moments").add(moment).addOnSuccessListener(doc->{
                    if(isFinishing()||isDestroyed())return;
                    Toast.makeText(this,"પોસ્ટ સફળતાપૂર્વક શેર થઈ",Toast.LENGTH_SHORT).show();
                    momentsPage();
                }).addOnFailureListener(e->{
                    if(isFinishing()||isDestroyed())return;
                    publish.setEnabled(true);status.setText("પોસ્ટ શેર થઈ નથી. ઈન્ટરનેટ ચકાસી ફરી પ્રયાસ કરો.");
                });
            };
            if(photo==null){save.accept("");return;}
            try {
                android.content.res.AssetFileDescriptor file=getContentResolver().openAssetFileDescriptor(photo,"r");
                if(file==null){publish.setEnabled(true);status.setText("ફોટો વાંચી શકાયો નથી.");return;}
                long length=file.getLength();file.close();
                if(length>10L*1024*1024){publish.setEnabled(true);status.setText("10 MBથી નાનો ફોટો પસંદ કરો.");return;}
                StorageReference target=FirebaseStorage.getInstance().getReference().child(
                    "chat_media/"+uid+"/moments/"+java.util.UUID.randomUUID());
                target.putFile(photo).continueWithTask(t->{
                    if(!t.isSuccessful()){Exception e=t.getException();if(e!=null)throw e;throw new IllegalStateException("Upload failed");}
                    return target.getDownloadUrl();
                }).addOnSuccessListener(url->save.accept(url.toString()))
                  .addOnFailureListener(e->{
                    if(isFinishing()||isDestroyed())return;
                    publish.setEnabled(true);status.setText("ફોટો અપલોડ થયો નથી. Storage/ઇન્ટરનેટ ચકાસો.");
                  });
            } catch(Exception e){publish.setEnabled(true);status.setText("ફોટો ખોલવામાં સમસ્યા છે.");}
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
                if (photoPreview != null && !KingSafeImageUri10070.show(photoPreview, selectedPhoto)) throw new SecurityException("Photo access unavailable");
            } catch (Exception ex) { selectedPhoto = null; Toast.makeText(this, "ફોટો ખોલી શકાતો નથી", Toast.LENGTH_SHORT).show(); }
        }

        if (requestCode == 24 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri=data.getData();
            try {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                FirebaseUser photoOwner=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
                if(photoOwner==null||!photoOwner.getUid().equals(photoPickerOwner10042)){
                    Toast.makeText(this,"Account changed • choose photo again",Toast.LENGTH_LONG).show();return;}
                uploadProfilePhoto868(uri);
            } catch (Exception ex) { Toast.makeText(this,"Profile photo could not be saved",Toast.LENGTH_SHORT).show(); }
            return;
        }
        if (requestCode == 26 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri=data.getData();
            try {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                FirebaseUser signedVideo10035=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
                if(signedVideo10035==null||!signedVideo10035.getUid().equals(videoPickerOwner10035)){
                    Toast.makeText(this,"Sign in with the same KING account to save this video",Toast.LENGTH_LONG).show();return;
                }
                String key10035="profile_videos_"+signedVideo10035.getUid();
                String old=getPreferences(0).getString(key10035,"");
                if(java.util.Arrays.asList(old.split("\\|\\|\\|")).contains(uri.toString())){
                    Toast.makeText(this,"Video already saved",Toast.LENGTH_SHORT).show();videoPage();return;
                }
                String updated=old.isEmpty()?uri.toString():old+"|||"+uri.toString();
                getPreferences(0).edit().putString(key10035,updated).apply();
                Toast.makeText(this,"Video saved on this phone",Toast.LENGTH_SHORT).show();
                videoPage();
            } catch (Exception ex) { Toast.makeText(this,"Video could not be saved",Toast.LENGTH_SHORT).show(); }
            return;
        }
    }
    private void photoStatus10067(String message){
        if(photoStatus10067!=null&&photoStatus10067.isAttachedToWindow())photoStatus10067.setText(message);
        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }
    private void uploadProfilePhoto868(Uri uri){
        if(uri==null||firebaseAuth==null||firebaseAuth.getCurrentUser()==null||firestore==null)return;
        if(photoUploadPending10067){photoStatus10067("A photo is still uploading. Please wait.");return;}
        if(!KingNetwork.online(this)){photoStatus10067("Connect to upload your photo. Your current photo is unchanged.");return;}
        final FirebaseUser owner10067=firebaseAuth.getCurrentUser();
        final TextView expectedStatus10067=photoStatus10067;
        photoUploadPending10067=true;photoStatus10067("Preparing profile photo…");
        new Thread(()->{
            try {
                byte[] jpeg=KingProfilePhoto10067.prepare(getApplicationContext(),uri);
                runOnUiThread(()->{
                    if(!currentGoogleIdentity10045(owner10067)){photoUploadPending10067=false;return;}
                    photoStatus10067("Uploading profile photo…");
                    StorageReference ref=FirebaseStorage.getInstance().getReference().child("chat_media/"+owner10067.getUid()+"/profile/avatar_"+java.util.UUID.randomUUID()+".jpg");
                    com.google.firebase.storage.StorageMetadata metadata=new com.google.firebase.storage.StorageMetadata.Builder().setContentType("image/jpeg").build();
                    ref.putBytes(jpeg,metadata).continueWithTask(task->{
                        if(!task.isSuccessful())throw task.getException()==null?new IllegalStateException("Upload failed"):task.getException();
                        return ref.getDownloadUrl();
                    }).addOnSuccessListener(download->{
                        if(!currentGoogleIdentity10045(owner10067)){photoUploadPending10067=false;return;}
                        String url=download.toString();
                        Map<String,Object> update=new HashMap<>();update.put("photoUrl",url);update.put("updatedAt",com.google.firebase.firestore.FieldValue.serverTimestamp());
                        com.google.firebase.firestore.WriteBatch batch=firestore.batch();
                        batch.set(firestore.collection("public_profiles").document(owner10067.getUid()),update,com.google.firebase.firestore.SetOptions.merge());
                        batch.set(firestore.collection("users").document(owner10067.getUid()),update,com.google.firebase.firestore.SetOptions.merge());
                        batch.commit().addOnSuccessListener(done->{
                            photoUploadPending10067=false;if(!currentGoogleIdentity10045(owner10067))return;
                            getPreferences(0).edit().putString("profile_photo_"+owner10067.getUid(),"")
                                .putString("profile_photo_cloud",url).putString("firebase_uid",owner10067.getUid()).apply();
                            owner10067.updateProfile(new com.google.firebase.auth.UserProfileChangeRequest.Builder().setPhotoUri(download).build());
                            if(expectedStatus10067==photoStatus10067)photoStatus10067("Profile photo saved online");
                            if("editProfile".equals(screen)&&editPhotoSlot10067!=null&&editPhotoSlot10067.isAttachedToWindow()){
                                editPhotoSlot10067.removeAllViews();editPhotoSlot10067.addView(profileAvatarView931("",url),new FrameLayout.LayoutParams(-1,-1));
                            }
                        }).addOnFailureListener(error->{
                            photoUploadPending10067=false;
                            if(currentGoogleIdentity10045(owner10067))photoStatus10067("Photo was not saved to your profile. Select the photo to retry.");
                        });
                    }).addOnFailureListener(error->{photoUploadPending10067=false;
                        if(currentGoogleIdentity10045(owner10067))photoStatus10067("Photo upload failed. Your current photo is unchanged.");});
                });
            }catch(Exception|OutOfMemoryError error){runOnUiThread(()->{photoUploadPending10067=false;
                if(currentGoogleIdentity10045(owner10067))photoStatus10067(error instanceof IllegalArgumentException?error.getMessage():"Photo could not be prepared. Choose another image.");});}
        },"king-profile-photo").start();
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
                    if (KingSafeImageUri10070.show(picture, Uri.parse(photo))) page.addView(picture, new LinearLayout.LayoutParams(-1, -2));
                    else text("ફોટો ઉપલબ્ધ નથી", 14, MUTED, false);
                }
                text("──────────────", 14, MUTED, false);
            }
        } catch (Exception ex) { text("પોસ્ટ લોડ થઈ શકી નથી", 14, MUTED, false); }
    }
    // All real rooms must be created and controlled by Firebase PartyActivity.
    private void createRoom(){ openPartyActivity(); }
    private void room(String name){ openPartyActivity(); }

    private void addChat(LinearLayout chat,String who,String message){ TextView t=new TextView(this); t.setText(who+":  "+message); t.setTextColor(Color.WHITE); t.setTextSize(14); t.setPadding(dp(10),dp(7),dp(10),dp(7)); chat.addView(t,new LinearLayout.LayoutParams(-1,-2)); }
    private void chatDialog(LinearLayout chat){
        final EditText e=new EditText(this); e.setHint("Say something...");
        new AlertDialog.Builder(this).setTitle("Room chat").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Send",(d,w)->{
            String m=e.getText().toString().trim();
            if(!m.isEmpty()){ addChat(chat,displayName,m); appendRoomChat(displayName,m); }
        }).show();
    }

    private void giftDialog(LinearLayout chat){
        new AlertDialog.Builder(this)
            .setTitle("🎁 FREE KING Gifts")
            .setMessage("Real-time gifts need a signed-in recipient. Open a live Party or Private Chat to send FREE decorative gifts. This device-only room cannot transfer Diamonds.")
            .setPositiveButton("Live Party",(d,w)->openPartyActivity())
            .setNeutralButton("Private Chat",(d,w)->messages())
            .setNegativeButton("Close",null).show();
    }

    private void roomGames(){
        final String[] labels={
            "🎲 Ludo","🐑 Sheep Fight","🐺 Werewolf",
            "🕵 Spy","🎨 Draw & Guess","🎱 Bingo",
            "🦁 Crazy Zoo","🧠 Memory","✊ Rock Paper Scissors",
            "🎲 Dice Duel","🁢 Domino","❎ Tic Tac Toe"
        };
        final String[] codes={
            "ludo","sheep","werewolf","spy","draw","bingo",
            "zoo","memory","rps","dice","domino","tic_tac_toe"
        };
        new AlertDialog.Builder(this).setTitle("🎮 Play a real game")
            .setItems(labels,(d,w)->openPlayableGame(codes[w]))
            .setNeutralButton("Party Multiplayer",(d,w)->kingOpenGameRoom("rps"))
            .setNegativeButton("Close",null).show();
    }
    private void playMiniGame(String game){
        String g=game==null?"":game.toLowerCase(java.util.Locale.US);
        if(g.contains("dart"))g="reaction";
        else if(g.contains("racing"))g="highlow";
        else if(g.contains("snake"))g="memory";
        else if(g.contains("draw"))g="draw";
        openPlayableGame(g);
        // Never add TEST Diamonds or XP merely because the user opened a
        // fake random-result dialog; score must come from actual gameplay.
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
        String members="open live Party to view";
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
        new AlertDialog.Builder(this).setTitle("Members • local preview unavailable").setItems(rows,(d,w)->{if(w==0)profile();else userProfile(names[w]);}).setNegativeButton("Close",null).show();
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
        if(android.os.Build.VERSION.SDK_INT>=23&&checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
            try{requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},MIC_REQUEST);}catch(Throwable e){KingStability.nonFatal(this,"mic-permission-request",e);Toast.makeText(this,"Microphone permission is required",Toast.LENGTH_LONG).show();}
            return;
        }
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
        }catch(SecurityException ex){stopMic();KingStability.nonFatal(this,"mic-security",ex);Toast.makeText(this,"Microphone permission was blocked",Toast.LENGTH_LONG).show();}
        catch(Exception ex){stopMic();KingStability.nonFatal(this,"mic-start",ex);Toast.makeText(this,"માઇક શરૂ થઈ શક્યો નથી",Toast.LENGTH_SHORT).show();}
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
        if(action!=null)card.setOnClickListener(v->safeUiAction940("main-card:"+title,action));return a;
    }
    private void messages() {
        screen="messages";
        stopMic();
        KingNav.openRoot(this,3);
    }

    private void addBottomNav(LinearLayout root,int selected){
        LinearLayout nav=new LinearLayout(this); nav.setGravity(Gravity.CENTER); nav.setBackground(KingRoyalTheme10021.bottomNav(this)); String[] ni={"⌂\nParty","♟\nGame","◇\nDiscover","✉\nMessages","●\nMe"};
        for(int i=0;i<ni.length;i++){TextView n=new TextView(this);n.setText(ni[i]);n.setTextSize(12);n.setGravity(Gravity.CENTER);n.setTextColor(i==selected?0xffd2b100:0xff777777);final int k=i;n.setOnClickListener(v->{if(k==0)openPartyActivity();else if(k==1)games();else if(k==2)discover();else if(k==3)messages();else profile();});nav.addView(n,new LinearLayout.LayoutParams(0,dp(62),1));} root.addView(nav);
    }

    private void games(){
        // Opening a Games tab is not a completed game.
        screen="games";stopMic();
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);root.setBackground(KingRoyalTheme10021.page());
        LinearLayout head=new LinearLayout(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setPadding(dp(16),dp(10),dp(10),dp(6));head.setBackground(KingRoyalTheme10021.header(this));
        TextView title=new TextView(this);
        title.setText("🎮 KING Games");title.setTextSize(25);
        title.setTextColor(0xff171717);title.setTypeface(null,Typeface.BOLD);
        head.addView(title,new LinearLayout.LayoutParams(0,dp(56),1));
        TextView search=new TextView(this);search.setText("⌕");
        search.setTextSize(26);search.setTextColor(0xff4f4a57);
        search.setGravity(Gravity.CENTER);search.setOnClickListener(v->discover());
        head.addView(search,new LinearLayout.LayoutParams(dp(50),dp(50)));
        root.addView(head,new LinearLayout.LayoutParams(-1,dp(78)));

        ScrollView scroll=new ScrollView(this);
        LinearLayout host=new LinearLayout(this);host.setOrientation(LinearLayout.VERTICAL);
        host.setPadding(dp(14),dp(10),dp(14),dp(36));scroll.addView(host);
        TextView instructions=new TextView(this);
        instructions.setText("Tap any game to PLAY NOW • solo modes work without another phone");
        instructions.setTextSize(13);instructions.setTextColor(0xff645e70);
        instructions.setPadding(dp(6),dp(8),dp(6),dp(10));host.addView(instructions);

        TextView multiplayer=new TextView(this);
        multiplayer.setText("👥  PLAY ONLINE IN PARTY ROOM\nRPS • Dice • Bingo • Coin • Wheel • Number Pick");
        multiplayer.setTextColor(Color.WHITE);multiplayer.setTextSize(14);
        multiplayer.setGravity(Gravity.CENTER);multiplayer.setTypeface(null,Typeface.BOLD);
        multiplayer.setPadding(dp(12),dp(12),dp(12),dp(12));
        multiplayer.setBackground(KingRoyalTheme10021.violetCard(this,18));
        multiplayer.setOnClickListener(v->kingOpenGameRoom("rps"));
        LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(-1,dp(102));
        mp.setMargins(0,0,0,dp(12));host.addView(multiplayer,mp);

        TextView f=new TextView(this);f.setText("My Games");
        f.setTextSize(17);f.setTypeface(null,Typeface.BOLD);
        f.setTextColor(0xff27242b);f.setPadding(dp(3),dp(8),0,dp(8));
        host.addView(f,new LinearLayout.LayoutParams(-1,dp(42)));
        LinearLayout highlights=new LinearLayout(this);
        highlights.setGravity(Gravity.TOP);
        addGameVideoCard940(highlights,"ludo","Ludo","Online + solo");
        addGameVideoCard940(highlights,"werewolf","Werewolf","Play now");
        addGameVideoCard940(highlights,"draw","Draw & Guess","Draw now");
        host.addView(highlights,new LinearLayout.LayoutParams(-1,-2));

        TextView next=new TextView(this);
        next.setText("18 playable games • free practice, no cash prizes");
        next.setTextSize(16);next.setTypeface(null,Typeface.BOLD);
        next.setTextColor(0xff28232f);
        next.setPadding(dp(3),dp(10),0,dp(8));
        host.addView(next);

        String[][] games={
            {"bingo","Bingo"},{"domino","Domino"},{"spy","Spy"},
            {"sheep","Sheep Fight"},{"zoo","Crazy Zoo"},{"memory","Memory"},
            {"rps","Rock Paper Scissors"},{"dice","Dice Duel"},{"wheel","Wheel"},
            {"guess","Number Guess"},{"coin","Coin Toss"},{"reaction","Reaction Tap"},
            {"highlow","High / Low"},{"tic_tac_toe","Tic Tac Toe"},{"slot","Lucky Slot"},
            {"werewolf","Werewolf"},{"draw","Draw & Guess"},{"ludo","Ludo"}
        };
        LinearLayout row=null;
        for(int i=0;i<games.length;i++){
            if(i%3==0){
                row=new LinearLayout(this);row.setGravity(Gravity.TOP);
                host.addView(row,new LinearLayout.LayoutParams(-1,-2));
            }
            addGameVideoCard940(row,games[i][0],games[i][1],
                "ludo".equals(games[i][0])?"Online + solo":"Play now");
        }
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        addBottomNav(root,1);setSafeContentView(root);
    }
    private void addGameVideoCard940(LinearLayout row,String code,String title,String badge){
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(4),dp(4),dp(4),dp(6));card.setBackground(KingRoyalTheme10021.card(this,14));card.setOnClickListener(v->openPlayableGame(code));
        KingGameArtView art=new KingGameArtView(this,code,title);card.addView(art,new LinearLayout.LayoutParams(-1,dp(94)));
        TextView nm=new TextView(this);nm.setText(title);nm.setTextSize(12);nm.setTypeface(null,Typeface.BOLD);nm.setTextColor(0xff27242b);nm.setGravity(Gravity.CENTER_VERTICAL);nm.setSingleLine(false);nm.setMaxLines(2);card.addView(nm,new LinearLayout.LayoutParams(-1,dp(38)));
        TextView sub=new TextView(this);sub.setText("● "+badge);sub.setTextSize(9);sub.setTextColor(0xff23a578);sub.setSingleLine(false);sub.setMaxLines(2);card.addView(sub,new LinearLayout.LayoutParams(-1,dp(27)));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(190),1);p.setMargins(dp(4),dp(2),dp(4),dp(6));row.addView(card,p);
    }

    private void openPlayableGame(String game){
        final String code=KingGameRoutes980.localGame(game);
        if(code==null){
            Toast.makeText(this,"This game is not installed yet",Toast.LENGTH_SHORT).show();
            return;
        }
        if("ludo".equals(code)){
            startActivity(new Intent(this,KingLudoLobbyActivity.class));
            return;
        }
        // Existing GamePlayActivity contains the playable game boards and
        // user-controlled actions for all advertised solo codes.
        Intent play=new Intent(this,GamePlayActivity.class);
        play.putExtra("game",code);
        startActivity(play);
    }
    private void kingOpenGameRoom(String game){
        Intent i=new Intent(this,PartyActivity.class);
        i.putExtra("requestedGame",game);
        i.putExtra("displayName",displayName==null||displayName.trim().isEmpty()?"KING User":displayName);
        startActivity(i);
    }
    private void kingGameModes(String game){openPlayableGame(game);}

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
        openCommunityHub700("Moments");
    }

    private void peoplePage(String title){
        // Open the ACTUAL cloud list selected by the profile counter.
        // Previously all three counters incorrectly opened Discover.
        Intent social10018=new Intent(this,SocialActivity.class);
        String tab10018="discover";
        if("Followers".equalsIgnoreCase(title))tab10018="followers";
        else if("Following".equalsIgnoreCase(title))tab10018="following";
        else if("Friends".equalsIgnoreCase(title))tab10018="friends";
        social10018.putExtra("initialSocialTab10018",tab10018);
        startActivity(social10018);
    }

    private void userProfile(String who){
        // A local seat label is not an account UID. Never invent profile IDs,
        // follower totals, VIP status or device-only Follow for a non-account.
        new AlertDialog.Builder(this).setTitle("Find a real KING user")
            .setMessage("A room seat name is not a verified KING account. Open People Search to find this user by their issued KING ID, and use their real live profile.")
            .setPositiveButton("Open People Search",(d,w)->startActivity(new Intent(this,SocialActivity.class)))
            .setNegativeButton("Close",null).show();
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
            FirebaseUser reporter= firebaseAuth==null?null:firebaseAuth.getCurrentUser();
            String ownerKey=reporter==null?"signed_out":reporter.getUid();
            String countKey="reports_"+ownerKey,historyKey="report_history_"+ownerKey;
            int count=getPreferences(0).getInt(countKey,0)+1;
            String entry=System.currentTimeMillis()+" | "+who+" | "+reason;
            String old=getPreferences(0).getString(historyKey,"");
            getPreferences(0).edit().putInt(countKey,count).putString(historyKey,entry+"\n"+old).apply();
            CloudSync.submitReport(this,who,reason,(ok,message)->runOnUiThread(()->Toast.makeText(this,message,Toast.LENGTH_LONG).show()));
        }).setNegativeButton("Cancel",null).show();
    }
    private void notifications(){ notificationsCenter(); }

    private void sideMenu(){
        // Keep Bolo Hi's common Me drawer shortcuts directly reachable while
        // routing each one to an existing KING Plus screen.
        String[] items={"Square / Moments","Coin Mall","Data Center","Privilege Pack","Privilege Shop","VIP Level","Noble Center","Family","User Level Center","Wallet","Web Diamond Recharge","Gift Wall / Share Gift","Social Media","Invite Friends","Settings","Help & Customer Support","Rules and Policies","All Me Options"};
        String[] icons={"▣","🪙","📊","🎁","🛍","💎","♛","🏠","⭐","💳","💎","🎁","👥","📨","⚙","❓","📜","☰"};
        Runnable[] actions={
            ()->openCommunityHub700("Moments"),
            ()->openDeepFlow810("coin_mall"),
            ()->openDeepFlow810("data_center"),
            ()->openDeepFlow810("privilege_pack"),
            ()->openDeepFlow810("privilege_shop"),
            ()->startActivity(new Intent(this,KingVipVisualActivity.class)),
            ()->openDeepFlow810("noble"),
            ()->openCommunityHub700("Family"),
            ()->openDeepFlow810("level_center"),
            ()->openDeepFlow810("wallet"),
            ()->openDeepFlow810("web_diamond"),
            ()->startActivity(new Intent(this,KingGiftCenterActivity.class)),
            ()->openDeepFlow810("social_center"),
            ()->shareMyIssuedKingId9998(),
            ()->openDeepFlow810("settings"),
            ()->openDeepFlow810("help"),
            ()->openDeepFlow810("rules"),
            ()->openDeepFlow810("profile_menu")
        };
        ScrollView scroll=new ScrollView(this);LinearLayout drawer=new LinearLayout(this);drawer.setOrientation(LinearLayout.VERTICAL);drawer.setPadding(dp(14),dp(20),dp(10),dp(20));drawer.setBackgroundColor(Color.WHITE);scroll.addView(drawer);
        AlertDialog dialog=new AlertDialog.Builder(this).setView(scroll).create();
        for(int i=0;i<items.length;i++){final int k=i;TextView row=new TextView(this);row.setText(icons[i]+"   "+items[i]);row.setTextSize(15);row.setTextColor(0xff222222);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(8),0,0,0);row.setOnClickListener(v->{dialog.dismiss();KingSafe.run(this,"drawer:"+items[k],actions[k]);});drawer.addView(row,new LinearLayout.LayoutParams(-1,dp(52)));}
        dialog.setOnShowListener(v->{android.view.Window w=dialog.getWindow();if(w!=null){w.setBackgroundDrawableResource(android.R.color.transparent);w.setGravity(Gravity.LEFT|Gravity.TOP);w.setLayout((int)(getResources().getDisplayMetrics().widthPixels*.82f),-1);}});dialog.show();
    }

    private void openDeepFlow810(String route){Intent i=new Intent(this,KingDeepFlowActivity.class);i.putExtra("route",route);startActivity(i);}

    /** Real Me sub-option route; preserves normal MainActivity navigation and login. */
    private boolean routeMeAction9981(String action){
        if(action==null||firebaseAuth==null||firebaseAuth.getCurrentUser()==null)return false;
        switch(action){
            case "edit_profile":editProfile();return true;
            case "profile_about":profile();profileAboutPage();return true;
            case "profile_gallery":profile();galleryPage();return true;
            case "profile_videos":profile();videoPage();return true;
            case "share_profile":shareMyIssuedKingId9998();return true;
            case "notifications":startActivity(new Intent(this,InboxActivity.class));return true;
            case "account_delete":profile();requestAccountDeletionFlow();return true;
            case "safety":privacySafetyPage();return true;
            case "profile_settings":settingsPage();return true;
            case "favorites":profile();showMeRoomHistory9981("favorites");return true;
            case "recent_rooms":profile();showMeRoomHistory9981("recent");return true;
            case "backpack":backpackPage();return true;
            case "help":helpCenterPage();return true;
            default:return false;
        }
    }
    private String photoPickerOwner10042="";
    private String profilePhoto10042(){
        FirebaseUser u=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
        if(u==null)return "";
        String key="profile_photo_"+u.getUid();
        String cached=getPreferences(0).getString(key,null);
        if(cached!=null)return cached;
        String legacy=u.getUid().equals(getPreferences(0).getString("firebase_uid",""))
            ?getPreferences(0).getString("profile_photo",""):"";
        getPreferences(0).edit().putString(key,legacy).apply();return legacy;
    }
    private String profileField10043(String field,String fallback){
        FirebaseUser user10043=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
        if(user10043==null)return fallback;
        SharedPreferences profilePrefs10043=getPreferences(0);
        String unique10043=field+"_"+user10043.getUid();
        if(profilePrefs10043.contains(unique10043))return profilePrefs10043.getString(unique10043,fallback);
        // Legacy unscoped values belong only to the account which saved them.
        if(user10043.getUid().equals(profilePrefs10043.getString("firebase_uid","")))
            return profilePrefs10043.getString(field,fallback);
        return fallback;
    }
    private void chooseProfilePhoto10067(){
        FirebaseUser owner=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
        if(owner==null){photoStatus10067("Sign in to change your profile photo");return;}
        if(photoUploadPending10067){photoStatus10067("A photo is still uploading. Please wait.");return;}
        photoPickerOwner10042=owner.getUid();
        Intent picker=new Intent(Intent.ACTION_OPEN_DOCUMENT);picker.addCategory(Intent.CATEGORY_OPENABLE);picker.setType("image/*");
        picker.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);startActivityForResult(picker,24);
    }
    private void editProfile(){
        screen="editProfile";
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.WHITE);
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(8),dp(4),dp(12),0);TextView back=new TextView(this);back.setText("‹");back.setTextSize(34);back.setTextColor(0xff222222);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->profile());head.addView(back,new LinearLayout.LayoutParams(dp(48),dp(52)));TextView title=new TextView(this);title.setText("Edit Profile");title.setTextSize(20);title.setTypeface(null,Typeface.BOLD);title.setTextColor(0xff222222);head.addView(title,new LinearLayout.LayoutParams(0,dp(52),1));TextView saveTop=new TextView(this);saveTop.setText("Save");saveTop.setTextSize(13);saveTop.setTextColor(0xff7a6500);saveTop.setGravity(Gravity.CENTER);head.addView(saveTop,new LinearLayout.LayoutParams(dp(56),dp(42)));root.addView(head);
        ScrollView sv=new ScrollView(this);LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(14),dp(6),dp(14),dp(20));sv.addView(body);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        TextView warn=new TextView(this);warn.setText("You can restrict other users from downloading/saving your posted images in Settings.");warn.setTextSize(11);warn.setTextColor(0xff725d00);warn.setGravity(Gravity.CENTER_VERTICAL);warn.setPadding(dp(12),dp(8),dp(12),dp(8));warn.setBackground(background(0xfffff3bf,8));body.addView(warn,new LinearLayout.LayoutParams(-1,dp(54)));
        String photo=profilePhoto10042();LinearLayout album=new LinearLayout(this);album.setGravity(Gravity.CENTER_VERTICAL);album.setPadding(dp(4),dp(10),dp(4),dp(10));TextView albLabel=new TextView(this);albLabel.setText("Album");albLabel.setTextSize(14);albLabel.setTextColor(0xff333333);album.addView(albLabel,new LinearLayout.LayoutParams(dp(88),dp(60)));View av=profileAvatarView931(photo,getPreferences(0).getString("profile_photo_cloud",""));
        av.setOnClickListener(v->chooseProfilePhoto10067());FrameLayout photoSlot10067=new FrameLayout(this);editPhotoSlot10067=photoSlot10067;photoSlot10067.setOnClickListener(v->chooseProfilePhoto10067());
        photoSlot10067.addView(av,new FrameLayout.LayoutParams(-1,-1));album.addView(photoSlot10067,new LinearLayout.LayoutParams(dp(62),dp(62)));body.addView(album,new LinearLayout.LayoutParams(-1,dp(82)));
        photoStatus10067=new TextView(this);photoStatus10067.setText(photoUploadPending10067?"Photo upload in progress…":"Tap your photo to update it online");
        photoStatus10067.setTextSize(12);photoStatus10067.setTextColor(0xff725d00);photoStatus10067.setPadding(dp(4),dp(4),dp(4),dp(8));body.addView(photoStatus10067);
        final EditText name=profileInputV600(body,"Nickname",displayName);final EditText bio=profileInputV600(body,"Bio",profileField10043("bio","Love music, games and new friends ✨"));final EditText birthday=profileInputV600(body,"Birthday",profileField10043("birthday",""));final EditText hometown=profileInputV600(body,"Hometown",profileField10043("hometown",""));final EditText tags=profileInputV600(body,"Labels",profileField10043("tags","Music, Games"));
        TextView gender=new TextView(this);gender.setText("Gender                         Not specified  ›");gender.setTextSize(13);gender.setTextColor(0xff555555);gender.setGravity(Gravity.CENTER_VERTICAL);gender.setPadding(dp(4),0,dp(4),0);body.addView(gender,new LinearLayout.LayoutParams(-1,dp(48)));
        TextView tip=new TextView(this);tip.setText("Tip: a complete profile helps other people discover and follow you.");tip.setTextSize(11);tip.setTextColor(0xff999999);tip.setPadding(dp(4),dp(8),dp(4),dp(10));body.addView(tip,new LinearLayout.LayoutParams(-1,dp(48)));
        TextView save=new TextView(this);save.setText("Save Profile");save.setTextSize(15);save.setTypeface(null,Typeface.BOLD);save.setTextColor(0xff222222);save.setGravity(Gravity.CENTER);save.setBackground(background(0xffffea00,10));LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(52));sp.setMargins(0,dp(8),0,0);body.addView(save,sp);
        final FirebaseUser editOwner10036=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
        final String editUid10036=editOwner10036==null?"":editOwner10036.getUid();
        final String[] selectedGender10036={profileField10043("gender","Not specified")};
        if(selectedGender10036[0].trim().isEmpty())selectedGender10036[0]="Not specified";
        gender.setText("Gender                         "+selectedGender10036[0]+"  ›");
        gender.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Gender")
            .setItems(new String[]{"Not specified","Male","Female","Other"},(d,w)->{
                selectedGender10036[0]=new String[]{"Not specified","Male","Female","Other"}[w];
                gender.setText("Gender                         "+selectedGender10036[0]+"  ›");
            }).show());
        Runnable doSave=()->{
            FirebaseUser current10036=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
            if(current10036==null||!editUid10036.equals(current10036.getUid())||firestore==null){
                Toast.makeText(this,"Sign in again before saving your profile",Toast.LENGTH_LONG).show();return;
            }
            if(!save.isEnabled())return;
            if(!KingNetwork.online(this)){Toast.makeText(this,"Connect to save your profile. Your edits are still here.",Toast.LENGTH_LONG).show();return;}
            final String n=name.getText().toString().trim();
            if(n.isEmpty()||n.length()>60){name.setError("Use a name of 1–60 characters");return;}
            final String bioValue=bio.getText().toString().trim(),townValue=hometown.getText().toString().trim();
            final String birthValue=birthday.getText().toString().trim(),tagValue=tags.getText().toString().trim(),genderValue=selectedGender10036[0];
            String badField10038=KingSixPages10038.profileError(bioValue,townValue,birthValue,tagValue);
            if(badField10038!=null){
                Toast.makeText(this,badField10038,Toast.LENGTH_LONG).show();return;
            }
            Map<String,Object> update10036=new HashMap<>();
            update10036.put("uid",editUid10036);update10036.put("displayName",n);
            update10036.put("searchName",n.toLowerCase(java.util.Locale.US));
            update10036.put("bio",bioValue);update10036.put("hometown",townValue);
            update10036.put("birthday",birthValue);update10036.put("tags",tagValue);
            update10036.put("gender",genderValue);
            update10036.put("updatedAt",com.google.firebase.firestore.FieldValue.serverTimestamp());
            save.setEnabled(false);saveTop.setEnabled(false);save.setText("Saving…");
            firestore.collection("public_profiles").document(editUid10036)
                .set(update10036,com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(v->{
                    FirebaseUser actual10036=firebaseAuth.getCurrentUser();
                    if(actual10036==null||!editUid10036.equals(actual10036.getUid()))return;
                    displayName=n;
                    getPreferences(0).edit().putString("name",n).putString("bio",bioValue)
                        .putString("hometown",townValue).putString("birthday",birthValue)
                        .putString("tags",tagValue).putString("gender",genderValue)
                        .putString("bio_"+editUid10036,bioValue)
                        .putString("hometown_"+editUid10036,townValue)
                        .putString("birthday_"+editUid10036,birthValue)
                        .putString("tags_"+editUid10036,tagValue)
                        .putString("gender_"+editUid10036,genderValue).apply();
                    actual10036.updateProfile(new com.google.firebase.auth.UserProfileChangeRequest.Builder().setDisplayName(n).build());
                    if(isFinishing()||isDestroyed())return;
                    save.setEnabled(true);saveTop.setEnabled(true);save.setText("Save Profile");
                    Toast.makeText(this,"Profile saved online",Toast.LENGTH_SHORT).show();
                    if("editProfile".equals(screen)&&root.isAttachedToWindow())profile();
                }).addOnFailureListener(error->{
                    FirebaseUser actual10036=firebaseAuth.getCurrentUser();
                    if(isFinishing()||isDestroyed()||actual10036==null||!editUid10036.equals(actual10036.getUid()))return;
                    save.setEnabled(true);saveTop.setEnabled(true);save.setText("Retry Save Profile");
                    Toast.makeText(this,"Profile was not saved. Your edits are still here; tap Retry.",Toast.LENGTH_LONG).show();
                });
        };
        save.setOnClickListener(v->doSave.run());saveTop.setOnClickListener(v->doSave.run());
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
        FirebaseUser reporter=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
        String ownerKey=reporter==null?"signed_out":reporter.getUid();
        Set<String> blocked=new HashSet<>(getPreferences(0).getStringSet("blocked",new HashSet<>()));
        int reports=getPreferences(0).getInt("reports_"+ownerKey,0);
        text("🚫 Blocked users: "+blocked.size(),18,Color.WHITE,true);
        text("⚑ Reports submitted locally: "+reports,18,Color.WHITE,true);
        String history=getPreferences(0).getString("report_history_"+ownerKey,"");
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
        startActivity(new Intent(this,KingRecharge974Activity.class));
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
        base("KING Plus v9.4","Security, notifications and release readiness");
        text("🔐 Server wallet & gifts",19,Color.WHITE,true);
        text("Production wallet writes stay blocked from normal clients. Gifts use trusted Cloud Functions, idempotent operation IDs and server ledger entries.",13,MUTED,false);
        text("🔔 Push notifications",19,Color.WHITE,true);
        text("FCM supports gift, room invite, live-room message, direct-message and follow notification paths once Cloud Functions are deployed.",13,MUTED,false);
        text("💳 No-billing mode",19,Color.WHITE,true);
        text("KING Plus does not start Google Play purchases or charge real money in this build. Wallet, gifts and parity flows remain no-billing.",13,MUTED,false);
        button("🌐 Open Live Cloud",CARD,this::openLiveCloud);
        button("🛡 Open Admin Dashboard",CARD,this::openAdminDashboard);
        text("Release candidate note: the CI APK/AAB is signed with the stable KING Plus test key so it can be tested consistently. A Play production upload key and Play App Signing are still required for store release.",12,0xffffd768,true);
        button("Back to Settings",CARD,this::settingsPage);
    }

    private com.google.firebase.firestore.ListenerRegistration walletPageListener10065;
    private void walletPage(){
        stopWallet9989();
        screen="wallet";
        renderedWalletUid10030=firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null
            ?firebaseAuth.getCurrentUser().getUid():null;
        // A local test-coin value is not a verified spendable server balance.
        coinBalance=0;
        base("My Wallet","Server-authoritative KING Plus diamonds");
        TextView bal=text("💎 Checking verified balance…",28,Color.WHITE,true); text("Only server-verified Diamonds can be spent on paid Gifts",14,MUTED,false);
        if(firestore!=null&&firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null){
            final String walletOwner10029=firebaseAuth.getCurrentUser().getUid();
            walletPageListener10065=firestore.collection("wallets").document(walletOwner10029)
                .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(doc,error)->{
                    if(!walletPageCurrent10029(walletOwner10029))return;
                    Object raw=doc==null?null:doc.get("coins");
                    if(error!=null){bal.setText("↻ Wallet unavailable • tap to retry");bal.setOnClickListener(v->{if(walletPageCurrent10029(walletOwner10029))walletPage();});return;}
                    if(doc==null||doc.getMetadata().isFromCache()||doc.getMetadata().hasPendingWrites()){
                        bal.setText("💎 Waiting for online confirmation…");return;
                    }
                    if(!(raw instanceof Long)||((Long)raw)<0||((Long)raw)>1000000000L){bal.setText("💎 No verified Diamond balance yet");return;}
                    coinBalance=(int)Math.min(Integer.MAX_VALUE,((Long)raw));bal.setText("💎 "+raw+" Diamonds");
                });
        }
        button("Open Gift Catalog",PURPLE,this::giftCatalogPage);
        button("💎 Recharge Diamonds",CARD,this::rechargeCenterPage);
        button("📜 Gift transaction history",CARD,()->startActivity(new Intent(this,KingGiftCenterActivity.class)));
        button("Back",CARD,this::profile);
    }

    private boolean walletPageCurrent10029(String account10029){
        return !isFinishing()&&!isDestroyed()&&"wallet".equals(screen)
            &&firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null
            &&account10029.equals(firebaseAuth.getCurrentUser().getUid());
    }

    private void giftCatalogPage(){
        screen="gift_catalog";
        base("KING Gift Center","Live Party gifts and gift history");
        text("🌹 💝 👑 🎉 💎 🎁",32,Color.WHITE,true);
        text("Diamond gifts use your verified balance. Select a live Party member to send a gift. Your received gift credit and history update online.",15,MUTED,false);
        button("🎁 Open Live Party Gift Shop",PURPLE,this::openPartyActivity);
        button("📜 Sent and received gift history",CARD,()->startActivity(new Intent(this,KingGiftCenterActivity.class)));
        button("💬 Private Chat",CARD,this::messages);
        text("Free backpack gifts remain available separately. VIP is earned only through verified recharge.",13,MUTED,false);
        button("Back to Wallet",CARD,this::walletPage);
    }
    private void backpackPage(){
        screen="backpack"; base("Backpack","Your room items and rewards");
        text("On-device gift activity (not paid Diamonds)",18,Color.WHITE,true);text("🎁 Sent gifts   ×"+giftCount,17,Color.WHITE,false);text("📥 Received gifts   ×"+receivedGiftCount,17,Color.WHITE,false);
        String frame=KingCosmetics.frame(this);String effect=KingCosmetics.effect(this);
        text("ID Frames & Entrance Effects",18,Color.WHITE,true);text(KingCosmetics.frameEmoji(frame)+"  ID frame: "+frame,15,MUTED,false);text(KingCosmetics.effectEmoji(effect)+"  Entry effect: "+effect,15,MUTED,false);button("🖼 Choose ID / Profile Frame",CARD,()->selectCosmetic730(true));button("✨ Choose Room Entry Effect",CARD,()->selectCosmetic730(false));
        button("Back",CARD,this::profile);
    }

    private void levelPage(){
        screen="level";
        base("Level & Achievements","TEST preview only • device activity is not server verified or reward eligible");
        LevelSystem.Snapshot p=LevelSystem.read(this);
        long next=p.nextLevelXp();
        long prev=LevelSystem.levelThreshold(p.level);
        long span=Math.max(1,next-prev);
        int percent=p.level>=LevelSystem.MAX_LEVEL?100:
            (int)Math.max(0,Math.min(100,(p.xp-prev)*100/span));
        text("🏆 TEST Activity Level "+p.level+" / 99",28,Color.WHITE,true);
        text(LevelSystem.levelTier(p.level)+" • device-only "+p.xp+" XP • "+percent+"% preview to next",15,MUTED,false);
        button("👑 Verified Recharge VIP",PURPLE,
            ()->startActivity(new Intent(this,KingVipVisualActivity.class)));
        text("VIP cannot increase from free Gifts, offline coins, or local TEST activity.",
            13,MUTED,false);
        text("Achievements • locked",18,Color.WHITE,true);
        text("Voice, room and game activity are not verified for rewards yet.",14,MUTED,false);
        button("🎒 Collection & Frames",PURPLE,()->openCommunityHub700("Collection"));
        button("Back",CARD,this::profile);
    }

    private void settingsPage(){
        screen="settings"; base("Settings","Account and app controls");
        text("Account",18,Color.WHITE,true); text("Signed in as "+displayName,15,MUTED,false);
        button("Edit Profile",CARD,this::editProfile);
        button("🔔 Notification Preferences • Firebase Account",CARD,()->openDeepFlow810("settings_notifications"));
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
        Intent i=new Intent(this,KingMePrivacy9984Activity.class);
        i.putExtra("page","privacy");
        startActivity(i);
    }

    private TextView liveMeName10064, liveMeGift10064, liveMeVip10064;
    private com.google.firebase.firestore.ListenerRegistration meIdentityListener10064;
    private void profile() { profileV600(); }

    private void profileV600(){
        screen="profile";stopMic();
        renderedProfileUid10030=firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null
            ?firebaseAuth.getCurrentUser().getUid():null;
        final LevelSystem.Snapshot progress700=LevelSystem.read(this);final int level=progress700.level;
        // Never show a guessed hash-based public ID. Fetch the issued ID from Firestore.
        final String uid="Not issued";
        // An avatar stored for account A must never render on account B's Me page.
        final String currentProfileOwner10031=firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null
            ?firebaseAuth.getCurrentUser().getUid():"";
        final boolean ownProfileMedia10031=currentProfileOwner10031.equals(
            getPreferences(0).getString("firebase_uid",""));
        final String photo=ownProfileMedia10031?profilePhoto10042():"";
        final String remotePhoto931=ownProfileMedia10031
            ?getPreferences(0).getString("profile_photo_cloud",getPreferences(0).getString("google_photo_url","")):"";
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackground(KingRoyalTheme10021.page());
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);top.setPadding(dp(12),dp(5),dp(8),0);top.setBackground(KingRoyalTheme10021.header(this));
        TextView menu=new TextView(this);menu.setText("☰");menu.setTextSize(27);menu.setTextColor(0xff222222);menu.setGravity(Gravity.CENTER);menu.setOnClickListener(v->sideMenu());top.addView(menu,new LinearLayout.LayoutParams(dp(48),dp(50)));
        LinearLayout balances10067=(LinearLayout)getLayoutInflater().inflate(R.layout.king_me_balances_10067,top,false);
        TextView beans=balances10067.findViewById(R.id.me_diamonds_10067);profileTopBalance=beans;
        beans.setBackground(background(0xfffff7cc,14));beans.setOnClickListener(v->walletPage());
        TextView coins=balances10067.findViewById(R.id.me_received_credit_10067);profileWalletCoins=coins;
        coins.setBackground(background(0xfffff7cc,14));coins.setOnClickListener(v->startActivity(new Intent(this,KingGiftCenterActivity.class)));
        top.addView(balances10067,new LinearLayout.LayoutParams(0,dp(48),1));root.addView(top,new LinearLayout.LayoutParams(-1,dp(56)));


        ScrollView sv=new ScrollView(this);LinearLayout content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(14),dp(12),dp(14),dp(34));sv.addView(content);
        LinearLayout header=(LinearLayout)getLayoutInflater().inflate(R.layout.king_me_identity_10064,content,false);
        String equippedFrame730=KingCosmetics.frame(this);
        FrameLayout avatarFrame730=header.findViewById(R.id.me_avatar_10064);
        liveMeAvatar10067=avatarFrame730;liveMeAvatarUrl10067=remotePhoto931;
        avatarFrame730.setBackground(KingCosmetics.avatarFrame(this,equippedFrame730,false));
        avatarFrame730.setPadding(dp(4),dp(4),dp(4),dp(4));
        avatarFrame730.addView(profileAvatarView931(photo,remotePhoto931),new FrameLayout.LayoutParams(-1,-1));
        avatarFrame730.setOnClickListener(v->editProfile());
        TextView name=header.findViewById(R.id.me_name_10064);liveMeName10064=name;
        name.setText(displayName+"  ›");name.setOnClickListener(v->editProfile());
        TextView id=header.findViewById(R.id.me_id_10064);id.setText("ID: "+uid);
        id.setBackground(KingCosmetics.idFrame(this,equippedFrame730));
        loadVerifiedMeId10002(id,"profile",equippedFrame730);
        KingVipIdentityStyle.load(this,currentProfileOwner10031,name,null);
        LinearLayout mini=header.findViewById(R.id.me_badges_10064);
        TextView vip=smallBadge("VIP …",0xffffe57b,0xff5a4100);
        liveMeVip10064=vip;mini.addView(vip,new LinearLayout.LayoutParams(dp(65),dp(28)));
        TextView lv=smallBadge("Lv."+level,0xff8865e8,Color.WHITE);
        mini.addView(lv,new LinearLayout.LayoutParams(dp(65),dp(28)));
        liveMeGift10064=header.findViewById(R.id.me_gifts_10064);
        liveMeGift10064.setOnClickListener(v->startActivity(new Intent(this,KingGiftCenterActivity.class)));
        content.addView(header,new LinearLayout.LayoutParams(-1,-2));

        LinearLayout counts=new LinearLayout(this);counts.setGravity(Gravity.CENTER);profileFollowersNumber=addProfileStat(counts,"…","Followers",()->peoplePage("Followers"));profileFollowingNumber=addProfileStat(counts,"…","Following",()->peoplePage("Following"));profileFriendsNumber=addProfileStat(counts,"…","Friends",()->peoplePage("Friends"));TextView realCoupling9985=addProfileStat(counts,"…","Coupling",()->openCommunityHub700("Relationship"));loadRealCoupleCount9985(realCoupling9985);content.addView(counts,new LinearLayout.LayoutParams(-1,dp(66)));

        LinearLayout family=new LinearLayout(this);family.setGravity(Gravity.CENTER_VERTICAL);family.setPadding(dp(12),0,dp(12),0);family.setBackground(KingRoyalTheme10021.card(this,16));TextView fi=new TextView(this);fi.setText("👑");fi.setTextSize(21);fi.setGravity(Gravity.CENTER);family.addView(fi,new LinearLayout.LayoutParams(dp(42),dp(48)));TextView ft=new TextView(this);ft.setText("Family Square");ft.setTextSize(14);ft.setTypeface(null,Typeface.BOLD);ft.setTextColor(0xff473819);family.addView(ft,new LinearLayout.LayoutParams(0,dp(48),1));TextView ar=new TextView(this);ar.setText("›");ar.setTextSize(25);ar.setGravity(Gravity.CENTER);family.addView(ar,new LinearLayout.LayoutParams(dp(34),dp(48)));family.setOnClickListener(v->openCommunityHub700("Family"));LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,dp(52));fp.setMargins(0,dp(7),0,dp(12));content.addView(family,fp);
        // Avoid truncating Me shortcuts on narrow phones or increased Android font scale.
        android.widget.HorizontalScrollView meMenuScroll10022=new android.widget.HorizontalScrollView(this);
        meMenuScroll10022.setHorizontalScrollBarEnabled(false);
        LinearLayout hub=new LinearLayout(this);hub.setPadding(dp(3),dp(3),dp(3),dp(3));
        String[] hi={"👣 Visitors","📝 Moments","💞 CP","🎒 Collection"};String[] ht={"Visitors","Moments","Relationship","Collection"};
        for(int i=0;i<hi.length;i++){final String tab=ht[i];TextView x=new TextView(this);x.setText(hi[i]);x.setTextSize(12);x.setGravity(Gravity.CENTER);x.setTextColor(0xff41394b);x.setSingleLine(false);x.setMaxLines(2);x.setBackground(KingRoyalTheme10021.card(this,12));if("Visitors".equals(tab))profileVisitorsChip940=x;x.setOnClickListener(v->{if("Visitors".equals(tab))showProfileVisitors940();else openCommunityHub700(tab);});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(100),dp(56));lp.setMargins(dp(4),0,dp(4),0);hub.addView(x,lp);}meMenuScroll10022.addView(hub);content.addView(meMenuScroll10022,new LinearLayout.LayoutParams(-1,dp(68)));
        addProfileVideoTabs940(content);
        addProfileRecommendations940(content);
        TextView more9981=new TextView(this);
        more9981.setText("☰  All Me options  ›   Account • VIP • Family • Privacy • Help");
        more9981.setTextSize(14);more9981.setTypeface(null,Typeface.BOLD);
        more9981.setTextColor(0xff34254e);more9981.setPadding(dp(14),dp(12),dp(14),dp(12));
        more9981.setBackground(background(0xffffedbb,12));
        more9981.setOnClickListener(v->openDeepFlow810("profile_menu"));
        TextView agencyEntry9982=new TextView(this);
        agencyEntry9982.setText("🏢 Agency & Agent Recharge   ›");
        agencyEntry9982.setTextSize(15);agencyEntry9982.setTextColor(0xff34254e);
        agencyEntry9982.setPadding(dp(14),dp(12),dp(14),dp(12));
        agencyEntry9982.setBackground(background(0xffffedbb,12));
        agencyEntry9982.setOnClickListener(v->startActivity(
            new Intent(this,KingAgency9982Activity.class)));
        LinearLayout.LayoutParams agencyEntryLp9982=new LinearLayout.LayoutParams(-1,dp(52));
        agencyEntryLp9982.setMargins(0,dp(5),0,dp(10));content.addView(agencyEntry9982,agencyEntryLp9982);

        LinearLayout.LayoutParams moreLp9981=new LinearLayout.LayoutParams(-1,dp(60));
        moreLp9981.setMargins(0,dp(8),0,dp(10));content.addView(more9981,moreLp9981);

        LinearLayout tabs=new LinearLayout(this);tabs.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);TextView fav=new TextView(this);fav.setText("Favorites");fav.setTextSize(14);fav.setTypeface(null,Typeface.BOLD);fav.setTextColor(0xff222222);fav.setGravity(Gravity.CENTER);fav.setBackground(background(0xfffff200,3));fav.setOnClickListener(v->showMeRoomHistory9981("favorites"));
        tabs.addView(fav,new LinearLayout.LayoutParams(dp(86),dp(38)));
        TextView rec9981=new TextView(this);rec9981.setText("Recent");rec9981.setTextSize(14);
        rec9981.setTextColor(0xff333333);rec9981.setGravity(Gravity.CENTER);
        rec9981.setOnClickListener(v->showMeRoomHistory9981("recent"));
        tabs.addView(rec9981,new LinearLayout.LayoutParams(dp(78),dp(38)));TextView follow=new TextView(this);follow.setText("Follow");follow.setTextSize(14);follow.setTextColor(0xff555555);follow.setGravity(Gravity.CENTER);follow.setOnClickListener(v->peoplePage("Following"));tabs.addView(follow,new LinearLayout.LayoutParams(dp(78),dp(38)));content.addView(tabs,new LinearLayout.LayoutParams(-1,dp(44)));
        TextView records=new TextView(this);records.setText("My Hosted Party Rooms");records.setTextSize(15);records.setTypeface(null,Typeface.BOLD);records.setTextColor(0xff222222);records.setPadding(0,dp(8),0,dp(6));content.addView(records,new LinearLayout.LayoutParams(-1,dp(44)));
        LinearLayout recordList=new LinearLayout(this);recordList.setOrientation(LinearLayout.VERTICAL);
        meHostedRoomsHost10018=recordList;
        content.addView(recordList,new LinearLayout.LayoutParams(-1,-2));loadPartyRecordsV600(recordList);
        root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));addBottomNav(root,4);setSafeContentView(root);loadRealProfileData();
    }

    // Hosted rooms are real cloud records. Do not hide the 13th room or older pages.
    private void loadPartyRecordsV600(LinearLayout host){
        final int generation10015=++hostedRoomsGeneration10014;
        host.removeAllViews();
        TextView reload10016=new TextView(this);
        reload10016.setText("↻ Refresh my hosted Party rooms");
        reload10016.setTextColor(0xff4c2c90);
        reload10016.setTextSize(14);
        reload10016.setPadding(dp(12),dp(10),dp(12),dp(10));
        reload10016.setBackground(background(0xfff3edff,12));
        reload10016.setOnClickListener(v->{
            if(reload10016.isEnabled()&&!isFinishing()&&!isDestroyed()&&host.getParent()!=null
                &&"profile".equals(screen)){
                // Loading a fresh page regenerates this button, preventing duplicate requests.
                reload10016.setEnabled(false);
                if(!isFinishing()&&!isDestroyed()&&host.getParent()!=null&&"profile".equals(screen))
                    loadPartyRecordsV600(host);
            }
        });
        host.addView(reload10016,new LinearLayout.LayoutParams(-1,-2));
        if(firestore==null||firebaseAuth==null||firebaseAuth.getCurrentUser()==null){
            mePartyRecordV600(host,"🎤","Sign in for hosted rooms","Only your real rooms appear here",null);return;
        }
        final String account10015=firebaseAuth.getCurrentUser().getUid();
        loadHostedRoomsPage10015(host,account10015,generation10015,null,0);
    }
    private boolean hostedRoomsCurrent10015(LinearLayout host,String account,int generation){
        return !isFinishing()&&!isDestroyed()&&"profile".equals(screen)
            && generation==hostedRoomsGeneration10014 && host.getParent()!=null
            && firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null
            && account.equals(firebaseAuth.getCurrentUser().getUid());
    }
    private void loadHostedRoomsPage10015(LinearLayout host,String account10015,int generation10015,
                                           DocumentSnapshot cursor10015,int page10015){
        if(!hostedRoomsCurrent10015(host,account10015,generation10015))return;
        final TextView progress10015=new TextView(this);
        progress10015.setText(page10015==0?"⌛ Loading hosted Party rooms…":"⌛ Loading more of your rooms…");
        progress10015.setTextSize(13);progress10015.setPadding(dp(10),dp(12),dp(10),dp(12));
        host.addView(progress10015,new LinearLayout.LayoutParams(-1,-2));
        com.google.firebase.firestore.Query query10015=firestore.collection("live_rooms")
            .whereEqualTo("ownerUid",account10015).limit(30);
        if(cursor10015!=null)query10015=query10015.startAfter(cursor10015);
        query10015.get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(q->{
            if(!hostedRoomsCurrent10015(host,account10015,generation10015))return;
            host.removeView(progress10015);
            java.util.List<DocumentSnapshot> docs=new ArrayList<>(q.getDocuments());
            java.util.Collections.sort(docs,(a,b)->{
                com.google.firebase.Timestamp at=a.getTimestamp("createdAt"),bt=b.getTimestamp("createdAt");
                return at==null?(bt==null?0:1):bt==null?-1:bt.compareTo(at);
            });
            int count=0;
            for(DocumentSnapshot doc:docs){
                if(!account10015.equals(doc.getString("ownerUid")))continue;
                String id=doc.getId(),name=doc.getString("name");
                if(name==null||name.trim().isEmpty())name="KING Party";
                boolean closed=Boolean.TRUE.equals(doc.getBoolean("closed"));
                mePartyRecordV600(host,closed?"🔒":"🎤",name,
                    closed?"Closed room • long press to copy Room ID":"Hosted by you • long press to copy Room ID",
                    closed?null:()->{
                        if(hostedRoomsCurrent10015(host,account10015,generation10015))openVerifiedMeRoom9981(id);
                    });
                // A hosted room's real ID can be shared without disclosing its password.
                // Never copy a stale room after account/profile navigation changes.
                final android.view.View roomRow10017=host.getChildAt(host.getChildCount()-1);
                roomRow10017.setOnLongClickListener(v->{
                    if(!hostedRoomsCurrent10015(host,account10015,generation10015))return true;
                    android.content.ClipboardManager clip10017=(android.content.ClipboardManager)
                        getSystemService(CLIPBOARD_SERVICE);
                    if(clip10017!=null){
                        clip10017.setPrimaryClip(android.content.ClipData.newPlainText("KING Party Room ID",id));
                        Toast.makeText(this,"Hosted Party Room ID copied",Toast.LENGTH_SHORT).show();
                    }
                    return true;
                });
                count++;
            }
            if(page10015==0 && count==0)mePartyRecordV600(host,"🎤","No hosted Party rooms yet",
                "Open Party to create one",this::openPartyActivity);
            if(q.size()==30){
                final DocumentSnapshot next10015=q.getDocuments().get(q.size()-1);
                TextView more10015=new TextView(this);
                more10015.setText("↓ Load more hosted Party rooms");more10015.setTextSize(14);
                more10015.setPadding(dp(12),dp(13),dp(12),dp(13));
                more10015.setBackground(background(0xffffedbb,12));
                more10015.setOnClickListener(v->{
                    if(!hostedRoomsCurrent10015(host,account10015,generation10015)
                        ||more10015.getParent()!=host)return;
                    more10015.setEnabled(false);
                    host.removeView(more10015);
                    loadHostedRoomsPage10015(host,account10015,generation10015,next10015,page10015+1);
                });
                host.addView(more10015,new LinearLayout.LayoutParams(-1,-2));
            }else if(page10015>0){
                mePartyRecordV600(host,"✓","All hosted rooms loaded","End of your Party rooms",null);
            }
        }).addOnFailureListener(e->{
            if(!hostedRoomsCurrent10015(host,account10015,generation10015))return;
            progress10015.setText("↻ Hosted rooms unavailable • tap to retry");
            progress10015.setOnClickListener(v->{
                if(!hostedRoomsCurrent10015(host,account10015,generation10015)
                    ||progress10015.getParent()!=host)return;
                progress10015.setEnabled(false);
                host.removeView(progress10015);
                loadHostedRoomsPage10015(host,account10015,generation10015,cursor10015,page10015);
            });
        });
    }
    private void showMeRoomHistory9981(String tab){
        if(firebaseAuth==null||firebaseAuth.getCurrentUser()==null){login();return;}
        Intent intent=new Intent(this,KingSavedRooms9990Activity.class);intent.putExtra("tab",tab);startActivity(intent);
    }
    private void openVerifiedMeRoom9981(String roomId9981){
        if(firestore==null||firebaseAuth==null||firebaseAuth.getCurrentUser()==null
                ||!KingMeRoomHistory9981.validId(roomId9981)){
            Toast.makeText(this,"Sign in to enter a real Party",Toast.LENGTH_LONG).show();return;
        }
        final String account9981=firebaseAuth.getCurrentUser().getUid();
        firestore.collection("live_rooms").document(roomId9981)
            .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(snapshot->{
                if(isFinishing()||isDestroyed()||firebaseAuth.getCurrentUser()==null
                   ||!account9981.equals(firebaseAuth.getCurrentUser().getUid()))return;
                if(!snapshot.exists()||Boolean.TRUE.equals(snapshot.getBoolean("closed"))){
                    Toast.makeText(this,"Party room has closed",Toast.LENGTH_LONG).show();return;
                }
                Intent intent=new Intent(this,PartyActivity.class);
                intent.putExtra("directRoomId",roomId9981);
                startActivity(intent); // PartyActivity still checks privacy/password and membership.
            }).addOnFailureListener(e->{
                if(!isFinishing()&&!isDestroyed()&&firebaseAuth!=null
                    &&firebaseAuth.getCurrentUser()!=null
                    &&account9981.equals(firebaseAuth.getCurrentUser().getUid()))
                    Toast.makeText(this,"Unable to verify Party room online",Toast.LENGTH_LONG).show();
            });
    }

    private void mePartyRecordV600(LinearLayout host,String icon,String title,String sub,Runnable action){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(4),dp(5),dp(4),dp(5));TextView av=new TextView(this);av.setText(icon);av.setTextSize(24);av.setGravity(Gravity.CENTER);av.setBackground(background(0xfff2f2f2,9));row.addView(av,new LinearLayout.LayoutParams(dp(54),dp(54)));LinearLayout txt=new LinearLayout(this);txt.setOrientation(LinearLayout.VERTICAL);txt.setPadding(dp(10),0,0,0);TextView t=new TextView(this);t.setText(title);t.setTextSize(13);t.setTextColor(0xff222222);t.setTypeface(null,Typeface.BOLD);txt.addView(t);TextView s=new TextView(this);s.setText(sub);s.setTextSize(11);s.setTextColor(0xff888888);txt.addView(s);row.addView(txt,new LinearLayout.LayoutParams(0,dp(52),1));TextView online=new TextView(this);online.setText("◉");online.setTextColor(0xff6b6b6b);online.setGravity(Gravity.CENTER);row.addView(online,new LinearLayout.LayoutParams(dp(34),dp(52)));if(action!=null)row.setOnClickListener(v->KingSafe.run(this,"ui-action",action));host.addView(row,new LinearLayout.LayoutParams(-1,dp(64)));
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
            stopProfileFollowUpdates965();stopWallet9989();
            if(profileFollowersNumber!=null)profileFollowersNumber.setText("—");
            if(profileFollowingNumber!=null)profileFollowingNumber.setText("—");
            if(profileFriendsNumber!=null)profileFriendsNumber.setText("—");
            if(profileTopBalance!=null)profileTopBalance.setText("💎 —");
            if(profileWalletCoins!=null)profileWalletCoins.setText("💎 —");
            return;
        }
        final String uid=firebaseAuth.getCurrentUser().getUid();
        startProfileFollowUpdates965(uid);
        loadProfileVisitors940(uid);
        refreshServerWallet();
    }
    private int profileFollowGeneration10008;
    private void stopProfileFollowUpdates965(){
        ++profileFollowGeneration10008;
        if(followingListener965!=null){followingListener965.remove();followingListener965=null;}
        if(followersListener965!=null){followersListener965.remove();followersListener965=null;}
        liveFollowing965.clear();liveFollowers965.clear();
        followingLoaded965=false;followersLoaded965=false;
    }
    private void renderProfileFollowUpdates965(){
        if(!"profile".equals(screen)||isFinishing()||isDestroyed())return;
        if(profileFollowingNumber!=null&&followingLoaded965)
            profileFollowingNumber.setText(String.valueOf(liveFollowing965.size()));
        if(profileFollowersNumber!=null&&followersLoaded965)
            profileFollowersNumber.setText(String.valueOf(liveFollowers965.size()));
        if(profileFriendsNumber!=null&&followingLoaded965&&followersLoaded965){
            int mutual965=0;
            for(String follower965:liveFollowers965)if(liveFollowing965.contains(follower965))mutual965++;
            profileFriendsNumber.setText(String.valueOf(mutual965));
        }
    }
    private void startProfileFollowUpdates965(String uid){
        stopProfileFollowUpdates965();
        // Reset the visible counters while the new account/session is verified.
        if(profileFollowingNumber!=null)profileFollowingNumber.setText("—");
        if(profileFollowersNumber!=null)profileFollowersNumber.setText("—");
        if(profileFriendsNumber!=null)profileFriendsNumber.setText("—");
        if(firestore==null||uid==null||uid.isEmpty()||!"profile".equals(screen))return;
        final int followTicket10008=profileFollowGeneration10008;
        followingListener965=firestore.collection("follows")
            .whereEqualTo("followerUid",uid).addSnapshotListener((snapshot,error)->{
                if(followTicket10008!=profileFollowGeneration10008
                    ||isFinishing()||isDestroyed()||!"profile".equals(screen))return;
                if(firebaseAuth==null||firebaseAuth.getCurrentUser()==null||
                   !uid.equals(firebaseAuth.getCurrentUser().getUid()))return;
                if(error!=null||snapshot==null){
                    followingLoaded965=false;
                    liveFollowing965.clear();
                    if(profileFollowingNumber!=null)profileFollowingNumber.setText("—");
                    if(profileFriendsNumber!=null)profileFriendsNumber.setText("—");
                    return;
                }
                liveFollowing965.clear();
                for(DocumentSnapshot doc:snapshot.getDocuments()){
                    String target=doc.getString("targetUid");
                    if(target!=null&&!target.isEmpty())liveFollowing965.add(target);
                }
                followingLoaded965=true;
                renderProfileFollowUpdates965();
            });
        followersListener965=firestore.collection("follows")
            .whereEqualTo("targetUid",uid).addSnapshotListener((snapshot,error)->{
                if(followTicket10008!=profileFollowGeneration10008
                    ||isFinishing()||isDestroyed()||!"profile".equals(screen))return;
                if(firebaseAuth==null||firebaseAuth.getCurrentUser()==null||
                   !uid.equals(firebaseAuth.getCurrentUser().getUid()))return;
                if(error!=null||snapshot==null){
                    followersLoaded965=false;
                    liveFollowers965.clear();
                    if(profileFollowersNumber!=null)profileFollowersNumber.setText("—");
                    if(profileFriendsNumber!=null)profileFriendsNumber.setText("—");
                    return;
                }
                liveFollowers965.clear();
                for(DocumentSnapshot doc:snapshot.getDocuments()){
                    String source=doc.getString("followerUid");
                    if(source!=null&&!source.isEmpty())liveFollowers965.add(source);
                }
                followersLoaded965=true;
                renderProfileFollowUpdates965();
            });
    }

    private void loadProfileVisitors940(String uid){
        final int countTicket10007=++visitorCountGeneration10007;
        final TextView chip10003=profileVisitorsChip940;
        if(chip10003!=null)chip10003.setText("👣 Visitors …");
        if(firestore==null||uid==null||uid.isEmpty()||chip10003==null)return;
        // A 100-document query displayed "100" even when there were many more.
        firestore.collection("public_profiles").document(uid).collection("visitors")
            .count().get(com.google.firebase.firestore.AggregateSource.SERVER)
            .addOnSuccessListener(total->{
                if(!KingMeHomePartyGuard10007.visitorCountCurrent(
                    uid,firebaseAuth==null||firebaseAuth.getCurrentUser()==null?null:firebaseAuth.getCurrentUser().getUid(),
                    countTicket10007,visitorCountGeneration10007,
                    "profile".equals(screen),profileVisitorsChip940==chip10003)
                    ||isFinishing()||isDestroyed())return;
                chip10003.setText("👣 Visitors "+total.getCount());
            }).addOnFailureListener(e->{
                if(!KingMeHomePartyGuard10007.visitorCountCurrent(
                    uid,firebaseAuth==null||firebaseAuth.getCurrentUser()==null?null:firebaseAuth.getCurrentUser().getUid(),
                    countTicket10007,visitorCountGeneration10007,
                    "profile".equals(screen),profileVisitorsChip940==chip10003)
                    ||isFinishing()||isDestroyed())return;
                chip10003.setText("👣 Visitors —");
            });
    }
    private void showProfileVisitors940(){
        if(firestore==null||firebaseAuth==null||firebaseAuth.getCurrentUser()==null){
            Toast.makeText(this,"Sign in to view visitors",Toast.LENGTH_SHORT).show();return;
        }
        final String account10003=firebaseAuth.getCurrentUser().getUid();
        final int visitorTicket10004=++visitorDialogGeneration10004;
        if(!"profile".equals(screen))return;
        loadVisitorPage10019(account10003,visitorTicket10004,null,1);
    }
    // Complete visitor history, scoped to the exact Me account and view generation.
    private boolean visitorsCurrent10019(String account10019,int ticket10019){
        return !isFinishing()&&!isDestroyed()&&"profile".equals(screen)
            &&ticket10019==visitorDialogGeneration10004
            &&firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null
            &&account10019.equals(firebaseAuth.getCurrentUser().getUid());
    }
    private void loadVisitorPage10019(String account10003,int visitorTicket10004,
             DocumentSnapshot cursor10019,int page10019){
        if(!visitorsCurrent10019(account10003,visitorTicket10004))return;
        com.google.firebase.firestore.Query q10019=firestore.collection("public_profiles")
            .document(account10003).collection("visitors")
            .orderBy("visitedAt",com.google.firebase.firestore.Query.Direction.DESCENDING).limit(50);
        if(cursor10019!=null)q10019=q10019.startAfter(cursor10019);
        q10019.get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(snap->{
            if(isFinishing()||isDestroyed()||!"profile".equals(screen)
                ||visitorTicket10004!=visitorDialogGeneration10004
                ||firebaseAuth.getCurrentUser()==null
                ||!account10003.equals(firebaseAuth.getCurrentUser().getUid()))return;
            if(snap.isEmpty()){
                new AlertDialog.Builder(this).setTitle("👣 Profile Visitors")
                    .setMessage(page10019==1?"No profile visitors yet.":"✓ End of visitor history. Earlier pages remain available in Visitor Center.")
                    .setPositiveButton("View all",(dialog,which)->openVerifiedVisitors10012(account10003,visitorTicket10004))
                    .setNegativeButton("Close",null).show();return;
            }
            java.util.List<String> rows=new java.util.ArrayList<>();
            java.util.List<String> ids=new java.util.ArrayList<>();
            for(DocumentSnapshot d:snap.getDocuments()){
                String uid10003=d.getString("uid");
                if(uid10003==null||uid10003.trim().isEmpty())uid10003=d.getId();
                if(account10003.equals(uid10003))continue;
                String n=d.getString("name");if(n==null||n.trim().isEmpty())n="KING User";
                com.google.firebase.Timestamp t=d.getTimestamp("visitedAt");
                String when=t==null?"":java.text.DateFormat.getDateTimeInstance(
                    java.text.DateFormat.SHORT,java.text.DateFormat.SHORT).format(t.toDate());
                rows.add("👤 "+n+(when.isEmpty()?"":"  •  "+when));ids.add(uid10003);
            }
            if(rows.isEmpty()){
                new AlertDialog.Builder(this).setTitle("👣 Profile Visitors")
                    .setMessage("No visitor profiles to display yet.")
                    .setPositiveButton("View all",(dialog,which)->openVerifiedVisitors10012(account10003,visitorTicket10004))
                    .setNegativeButton("Close",null).show();return;
            }
            final DocumentSnapshot next10019=snap.size()==50
                ?snap.getDocuments().get(snap.size()-1):null;
            new AlertDialog.Builder(this).setTitle("👣 Visitors • Page "+page10019+" ("+rows.size()+")")
                .setItems(rows.toArray(new String[0]),(dlg,w)->{
                    if(!visitorsCurrent10019(account10003,visitorTicket10004))return;
                    Intent i=new Intent(this,KingPublicProfileActivity.class);
                    i.putExtra("uid",ids.get(w));startActivity(i);
                }).setPositiveButton(next10019==null?"Visitor Center":"↓ Next 50 visitors",(dlg,w)->{
                    if(!visitorsCurrent10019(account10003,visitorTicket10004))return;
                    if(next10019==null)openVerifiedVisitors10012(account10003,visitorTicket10004);
                    else loadVisitorPage10019(account10003,visitorTicket10004,next10019,page10019+1);
                }).setNeutralButton("Visitor Center",(dlg,w)->
                    openVerifiedVisitors10012(account10003,visitorTicket10004))
                .setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->{
            if(isFinishing()||isDestroyed()||!"profile".equals(screen)
                ||visitorTicket10004!=visitorDialogGeneration10004
                ||firebaseAuth.getCurrentUser()==null
                ||!account10003.equals(firebaseAuth.getCurrentUser().getUid()))return;
            new AlertDialog.Builder(this).setTitle("👣 Profile Visitors")
                .setMessage("Visitors are unavailable right now. Page "+page10019)
                .setPositiveButton("Retry",(d,w)->{
                    if(visitorsCurrent10019(account10003,visitorTicket10004))
                        loadVisitorPage10019(account10003,visitorTicket10004,cursor10019,page10019);
                })
                .setNegativeButton("Close",null).show();
        });
    }

    // A stale Visitors dialog must never open a different account's history.
    private void openVerifiedVisitors10012(String account10012,int ticket10012){
        if(isFinishing()||isDestroyed()||!"profile".equals(screen)
            ||ticket10012!=visitorDialogGeneration10004
            ||firebaseAuth==null||firebaseAuth.getCurrentUser()==null
            ||!account10012.equals(firebaseAuth.getCurrentUser().getUid()))return;
        openCommunityHub700("Visitors");
    }

    private com.google.firebase.firestore.ListenerRegistration walletListener9989;
    private int walletGeneration9989;
    private void stopWallet9989(){
        ++walletGeneration9989;
        if(walletPageListener10065!=null){walletPageListener10065.remove();walletPageListener10065=null;}
        if(meIdentityListener10064!=null){meIdentityListener10064.remove();meIdentityListener10064=null;}
        if(walletListener9989!=null){walletListener9989.remove();walletListener9989=null;}
    }
    private void refreshServerWallet(){
        stopWallet9989();
        if(profileTopBalance!=null)profileTopBalance.setText("💎 —");
        if(profileWalletCoins!=null)profileWalletCoins.setText("◈ —");
        if(firestore==null||firebaseAuth==null||firebaseAuth.getCurrentUser()==null)return;
        final String account=firebaseAuth.getCurrentUser().getUid();
        final int generation=walletGeneration9989;
        final TextView expectedName10064=liveMeName10064;
        meIdentityListener10064=firestore.collection("public_profiles").document(account)
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(profile,error)->{
                if(generation!=walletGeneration9989||isFinishing()||isDestroyed()||!"profile".equals(screen)
                    ||firebaseAuth.getCurrentUser()==null||!account.equals(firebaseAuth.getCurrentUser().getUid())
                    ||expectedName10064!=liveMeName10064)return;
                if(error!=null||profile==null||!profile.exists()||profile.getMetadata().isFromCache()||profile.getMetadata().hasPendingWrites())return;
                SharedPreferences.Editor identity10067=getPreferences(0).edit().putString("firebase_uid",account);
                for(String field10067:new String[]{"bio","tags","hometown","birthday","gender"}){
                    Object raw10067=profile.get(field10067);String value10067=raw10067 instanceof String?(String)raw10067:"";
                    identity10067.putString(field10067+"_"+account,value10067).putString(field10067,value10067);
                }
                String cloudPhoto10067=profile.getString("photoUrl");
                if(cloudPhoto10067!=null&&!cloudPhoto10067.equals(liveMeAvatarUrl10067)&&liveMeAvatar10067!=null){
                    liveMeAvatarUrl10067=cloudPhoto10067;
                    identity10067.putString("profile_photo_cloud",cloudPhoto10067).putString("profile_photo_"+account,"");
                    liveMeAvatar10067.removeAllViews();
                    liveMeAvatar10067.addView(profileAvatarView931("",cloudPhoto10067),new FrameLayout.LayoutParams(-1,-1));
                }
                identity10067.apply();
                String n=profile.getString("displayName");
                if(expectedName10064!=null&&n!=null&&!n.trim().isEmpty()){displayName=n.trim();getPreferences(0).edit().putString("name",displayName).apply();expectedName10064.setText(displayName+"  ›");}
            });
        walletListener9989=firestore.collection("wallets").document(account)
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(doc,error)->{
                if(generation!=walletGeneration9989||isFinishing()||isDestroyed()
                    ||!"profile".equals(screen)||firebaseAuth.getCurrentUser()==null
                    ||!account.equals(firebaseAuth.getCurrentUser().getUid()))return;
                Object raw=doc==null?null:doc.get("coins");
                boolean verified=error==null&&doc!=null&&!doc.getMetadata().isFromCache()
                    &&!doc.getMetadata().hasPendingWrites()&&raw instanceof Long&&((Long)raw)>=0;
                String value=verified?String.valueOf(raw):"—";
                if(verified)coinBalance=(int)Math.min(Integer.MAX_VALUE,(Long)raw);
                if(profileTopBalance!=null)profileTopBalance.setText("💎 "+value);

                Object recharged10064=doc==null?null:doc.get("rechargeDiamondsTotal");
                if(liveMeVip10064!=null)liveMeVip10064.setText(verified&&recharged10064 instanceof Long
                    &&((Long)recharged10064)>=0&&((Long)recharged10064)<=1000000000L
                    ?"VIP "+LevelSystem.vipFor((Long)recharged10064):"VIP —");
                Object giftScore10064=doc==null?null:doc.get("giftScore");
                Long credit10067=KingPagePolicy10067.balance(giftScore10064==null&&doc!=null&&doc.exists()?Long.valueOf(0):giftScore10064);
                boolean giftVerified10064=error==null&&doc!=null&&!doc.getMetadata().isFromCache()
                    &&!doc.getMetadata().hasPendingWrites();
                String creditLabel10067=giftVerified10064&&credit10067!=null?String.valueOf(credit10067):"—";
                if(profileWalletCoins!=null)profileWalletCoins.setText("◈ "+creditLabel10067);
                if(liveMeGift10064!=null)liveMeGift10064.setText("Received gifts: "+creditLabel10067+"  ›");
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
        if(action!=null)box.setOnClickListener(v->KingSafe.run(this,"ui-action",action));
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
        if(action!=null)box.setOnClickListener(v->KingSafe.run(this,"ui-action",action));
        row.addView(box,new LinearLayout.LayoutParams(0,dp(84),1));
    }

    private void addMeChip(LinearLayout row,String label,Runnable action){
        TextView v=new TextView(this);
        v.setText(label); v.setTextSize(12); v.setTextColor(0xff4b3f57); v.setGravity(Gravity.CENTER);
        v.setBackground(background(0xffeee8f8,16));
        if(action!=null)v.setOnClickListener(x->KingSafe.run(this,"ui-action",action));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(36),1); p.setMargins(dp(3),0,dp(3),0); row.addView(v,p);
    }

    private void addMeTab(LinearLayout row,String label,Runnable action){
        TextView v=new TextView(this);
        v.setText(label); v.setTextSize(14); v.setTypeface(null,Typeface.BOLD); v.setTextColor(0xff30283a); v.setGravity(Gravity.CENTER);
        v.setBackground(background(Color.WHITE,14));
        if(action!=null)v.setOnClickListener(x->KingSafe.run(this,"ui-action",action));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(42),1); p.setMargins(dp(3),0,dp(3),0); row.addView(v,p);
    }

    private void shareMyProfile(){
        shareMyIssuedKingId9998();
    }

    /** Only display six-digit public IDs confirmed by the actual Firestore profile. */
    private void loadVerifiedMeId10002(TextView label,String expectedScreen,String frame){
        if(label==null||firebaseAuth==null||firestore==null||firebaseAuth.getCurrentUser()==null){
            if(label!=null)label.setText("KING ID: Sign in required");return;
        }
        final String account=firebaseAuth.getCurrentUser().getUid();
        label.setText("KING ID: Verifying…");
        firestore.collection("public_profiles").document(account)
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(doc->{
                if(isFinishing()||isDestroyed()||!expectedScreen.equals(screen)
                    ||label.getParent()==null||firebaseAuth.getCurrentUser()==null
                    ||!account.equals(firebaseAuth.getCurrentUser().getUid()))return;
                String issued=KingPublicId9996.verified(doc.getString("publicId"));
                String suffix=issued==null?"Not issued":issued;
                label.setText(frame==null?"KING ID: "+suffix:
                    KingCosmetics.frameEmoji(frame)+"  ID: "+suffix+"  "+KingCosmetics.frameEmoji(frame));
                KingVipIdentityStyle.load(this,account,null,label);
            }).addOnFailureListener(e->{
                if(isFinishing()||isDestroyed()||!expectedScreen.equals(screen)
                    ||label.getParent()==null||firebaseAuth.getCurrentUser()==null
                    ||!account.equals(firebaseAuth.getCurrentUser().getUid()))return;
                label.setText("KING ID: Verification unavailable • tap to retry");
                label.setOnClickListener(v->loadVerifiedMeId10002(label,expectedScreen,frame));
            });
    }

    private void profileAboutPage(){
        screen="profile_about"; base("About","KING Plus profile details");
        String bio=profileField10043("bio","");
        String hometown=profileField10043("hometown","");
        String birthday=profileField10043("birthday","");
        String tags=profileField10043("tags","");
        text("👤 "+displayName,24,Color.WHITE,true);
        TextView verifiedAboutId10002=new TextView(this);
        verifiedAboutId10002.setTextColor(MUTED);verifiedAboutId10002.setTextSize(14);
        verifiedAboutId10002.setPadding(dp(12),dp(8),dp(12),dp(8));
        page.addView(verifiedAboutId10002,new LinearLayout.LayoutParams(-1,dp(46)));
        loadVerifiedMeId10002(verifiedAboutId10002,"profile_about",null);
        text("💬 "+(bio.isEmpty()?"Not set":bio),16,Color.WHITE,false); text("📍 "+(hometown.isEmpty()?"Not set":hometown),15,Color.WHITE,false);
        text("🎂 "+(birthday.isEmpty()?"Not set":birthday),15,Color.WHITE,false); text("🏷 "+tags,15,Color.WHITE,false);

        button("Edit Profile",PURPLE,this::editProfile); button("Back to Me",CARD,this::profile);
    }

    private void galleryPage(){
        screen="profile_gallery"; base("My Gallery","Photos and profile Moments");
        String photo=profilePhoto10042();
        if(!photo.isEmpty()){
            ImageView img=new ImageView(this); img.setAdjustViewBounds(true); img.setMaxHeight(dp(320));
            if(KingSafeImageUri10070.show(img,Uri.parse(photo)))page.addView(img,new LinearLayout.LayoutParams(-1,-2));
        }
        button("＋ Add Photo / Moment",PURPLE,this::composePost);
        button("☁ Live Moments (બંને ફોન)",PURPLE,this::momentsPage);
        text("☁ My published Moments (બંને ફોન)",18,Color.WHITE,true);
        LinearLayout myCloudGallery9992=new LinearLayout(this);
        myCloudGallery9992.setOrientation(LinearLayout.VERTICAL);
        page.addView(myCloudGallery9992,new LinearLayout.LayoutParams(-1,-2));
        meCloudGalleryHost9993=myCloudGallery9992;
        showMyCloudGallery9992(myCloudGallery9992);
        text("આ ફોનની જૂની પોસ્ટ (Local Archive)",16,Color.WHITE,true); showPosts();
        button("Back to Me",CARD,this::profile);
    }

    /** Account-scoped shared Moments appear in Me > Gallery as well as Community.
     * No synthetic profiles, local-only posts or auto-publishing old photos. */
    private void showMyCloudGallery9992(LinearLayout host){
        stopMeGallery9993();
        host.removeAllViews();
        if(firestore==null||firebaseAuth==null||firebaseAuth.getCurrentUser()==null){
            TextView t=new TextView(this);t.setText("Sign in to view your published Moments.");
            t.setTextColor(MUTED);host.addView(t);return;
        }
        final String account=firebaseAuth.getCurrentUser().getUid();
        final TextView status=new TextView(this);status.setTextColor(MUTED);
        status.setPadding(dp(6),dp(8),dp(6),dp(8));host.addView(status);
        final Runnable[] refresh=new Runnable[1];
        refresh[0]=()->{
            if(!"profile_gallery".equals(screen)||isFinishing()||isDestroyed()
                ||firebaseAuth.getCurrentUser()==null
                ||!account.equals(firebaseAuth.getCurrentUser().getUid()))return;
            status.setText("Loading your live Moments…");status.setOnClickListener(null);
            stopMeGallery9993();
            final int galleryTicket9994=meGalleryGeneration9994;
            meGalleryListener9993=firestore.collection("moments").whereEqualTo("uid",account).limit(meGalleryLimit9996)
                .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(result,error)->{
                    if(galleryTicket9994!=meGalleryGeneration9994)return;
                    if(error!=null||result==null){
                        if("profile_gallery".equals(screen)&&!isFinishing()&&!isDestroyed()
                            &&firebaseAuth.getCurrentUser()!=null&&account.equals(firebaseAuth.getCurrentUser().getUid())){
                            status.setText("Published Moments unavailable. Tap to retry.");
                            status.setOnClickListener(v->refresh[0].run());
                        }
                        return;
                    }
                    if(!"profile_gallery".equals(screen)||isFinishing()||isDestroyed()
                        ||firebaseAuth.getCurrentUser()==null
                        ||!account.equals(firebaseAuth.getCurrentUser().getUid()))return;
                    host.removeViews(1,host.getChildCount()-1);
                    java.util.List<DocumentSnapshot> docs=new ArrayList<>(result.getDocuments());
                    java.util.Collections.sort(docs,(a,b)->{
                        com.google.firebase.Timestamp ta=a.getTimestamp("createdAt"),tb=b.getTimestamp("createdAt");
                        return ta==null?(tb==null?0:1):tb==null?-1:tb.compareTo(ta);
                    });
                    status.setText(result.getMetadata().isFromCache()
                        ? "Offline Gallery • showing cached Moments • tap to retry"
                        : (docs.isEmpty()?"No published Moments yet. Create your first post above."
                            :docs.size()+" published Moments • live on both phones • tap to refresh"));
                    status.setOnClickListener(v->refresh[0].run());
                    int images=0;
                    for(DocumentSnapshot doc:docs){
                        LinearLayout entry=new LinearLayout(this);entry.setOrientation(LinearLayout.VERTICAL);
                        entry.setPadding(dp(10),dp(8),dp(10),dp(12));
                        TextView caption=new TextView(this);caption.setTextColor(Color.WHITE);
                        caption.setTextSize(15);caption.setText(doc.getString("text")==null?"📷 Photo":doc.getString("text"));
                        entry.addView(caption);
                        String url=doc.getString("photoUrl");
                        if(url!=null&&url.startsWith("https://firebasestorage.googleapis.com/")){
                            // Render every Gallery photo, not only the first six; avoid
                            // launching hundreds of 10 MB downloads at once.
                            final boolean autoPreview10001=images++<6;
                            ImageView preview=new ImageView(this);
                            preview.setAdjustViewBounds(true);preview.setMaxHeight(dp(230));
                            entry.addView(preview,new LinearLayout.LayoutParams(-1,-2));
                            TextView photoStatus10001=new TextView(this);
                            photoStatus10001.setTextColor(MUTED);photoStatus10001.setTextSize(13);
                            photoStatus10001.setPadding(dp(4),dp(6),0,dp(6));
                            entry.addView(photoStatus10001);
                            final boolean[] photoLoading10001={false};
                            Runnable loadPhoto10001=()->{
                                if(photoLoading10001[0] || galleryTicket9994!=meGalleryGeneration9994
                                    ||!"profile_gallery".equals(screen)||isFinishing()||isDestroyed()
                                    ||firebaseAuth.getCurrentUser()==null
                                    ||!account.equals(firebaseAuth.getCurrentUser().getUid()))return;
                                photoLoading10001[0]=true;
                                photoStatus10001.setText("📷 Loading photo…");
                                try{FirebaseStorage.getInstance().getReferenceFromUrl(url).getBytes(10L*1024*1024)
                                    .addOnSuccessListener(bytes->{
                                        photoLoading10001[0]=false;
                                        if(galleryTicket9994!=meGalleryGeneration9994
                                            ||!"profile_gallery".equals(screen)||isFinishing()||isDestroyed()
                                            ||preview.getParent()==null||firebaseAuth.getCurrentUser()==null
                                            ||!account.equals(firebaseAuth.getCurrentUser().getUid()))return;
                                        android.graphics.BitmapFactory.Options o=new android.graphics.BitmapFactory.Options();
                                        o.inJustDecodeBounds=true;
                                        android.graphics.BitmapFactory.decodeByteArray(bytes,0,bytes.length,o);
                                        o.inJustDecodeBounds=false;
                                        o.inSampleSize=Math.max(1,Math.max(o.outWidth/480,o.outHeight/480));
                                        android.graphics.Bitmap photo=android.graphics.BitmapFactory.decodeByteArray(bytes,0,bytes.length,o);
                                        if(photo!=null){preview.setImageBitmap(photo);photoStatus10001.setVisibility(View.GONE);}
                                        else photoStatus10001.setText("Photo unavailable • tap to retry");
                                    }).addOnFailureListener(failure->{
                                        photoLoading10001[0]=false;
                                        if(galleryTicket9994==meGalleryGeneration9994&&preview.getParent()!=null)
                                            photoStatus10001.setText("Photo unavailable • tap to retry");
                                    });
                                }catch(Exception ignored){photoLoading10001[0]=false;
                                    photoStatus10001.setText("Photo unavailable • tap to retry");}
                            };
                            photoStatus10001.setOnClickListener(v->loadPhoto10001.run());
                            if(autoPreview10001)loadPhoto10001.run();
                            else photoStatus10001.setText("📷 Tap to load this photo");
                        }
                        TextView view=new TextView(this);view.setText("Open in Live Moments  ›");
                        view.setTextSize(13);view.setTextColor(MUTED);
                        view.setPadding(dp(4),dp(9),0,dp(3));
                        view.setOnClickListener(v->momentsPage());entry.addView(view);
                        // Me > Gallery must let the owner manage the SAME cloud Moment
                        // that is displayed in Community; never delete local archives.
                        TextView deleteMoment9995=new TextView(this);
                        deleteMoment9995.setText("Delete published Moment  ×");
                        deleteMoment9995.setTextSize(13);
                        deleteMoment9995.setTextColor(0xffffb5b5);
                        deleteMoment9995.setPadding(dp(4),dp(8),0,dp(9));
                        deleteMoment9995.setOnClickListener(v->{
                            FirebaseUser current=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
                            if(current==null||!account.equals(current.getUid())||
                                !account.equals(doc.getString("uid"))||
                                galleryTicket9994!=meGalleryGeneration9994)return;
                            new AlertDialog.Builder(this).setTitle("Delete published Moment?")
                                .setMessage("Remove this post from Me Gallery and the shared Moments feed on both phones?")
                                .setNegativeButton("Cancel",null)
                                .setPositiveButton("Delete",(dialog,which)->{
                                    FirebaseUser signed=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
                                    if(signed==null||!account.equals(signed.getUid()))return;
                                    deleteMoment9995.setEnabled(false);
                                    deleteMoment9995.setText("Deleting Moment…");
                                    doc.getReference().delete().addOnFailureListener(deleteFailure9995->{
                                        if(isFinishing()||isDestroyed()||deleteMoment9995.getParent()==null)return;
                                        deleteMoment9995.setEnabled(true);
                                        deleteMoment9995.setText("Delete published Moment  ×");
                                        Toast.makeText(this,"Moment could not be deleted. Check connection.",Toast.LENGTH_LONG).show();
                                    });
                                }).show();
                        });
                        entry.addView(deleteMoment9995);
                        host.addView(entry,new LinearLayout.LayoutParams(-1,-2));
                    }
                    // The older implementation silently hid every published Moment
                    // beyond 40. Expand the same owner-scoped live query on demand.
                    if(docs.size()>=meGalleryLimit9996 && meGalleryLimit9996<320){
                        TextView loadMore9996=new TextView(this);
                        loadMore9996.setText("↓ Load more published Moments ("+docs.size()+" shown)");
                        loadMore9996.setTextSize(15);loadMore9996.setTextColor(0xffffdc88);
                        loadMore9996.setPadding(dp(12),dp(18),dp(12),dp(18));
                        loadMore9996.setOnClickListener(v->{
                            if(galleryTicket9994!=meGalleryGeneration9994 ||
                                !"profile_gallery".equals(screen) || firebaseAuth.getCurrentUser()==null ||
                                !account.equals(firebaseAuth.getCurrentUser().getUid()))return;
                            meGalleryLimit9996=Math.min(320,meGalleryLimit9996+40);
                            showMyCloudGallery9992(host);
                        });
                        host.addView(loadMore9996,new LinearLayout.LayoutParams(-1,-2));
                    }
                });
        };
        refresh[0].run();
    }

    private void stopMeGallery9993(){
        ++meGalleryGeneration9994;
        if(meGalleryListener9993!=null){meGalleryListener9993.remove();meGalleryListener9993=null;}
    }

    private String videoPickerOwner10035="";
    private String videoPreferenceKey10035(){
        FirebaseUser u10035=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
        return u10035==null?null:"profile_videos_"+u10035.getUid();
    }
    private void videoPage(){
        screen="profile_videos";base("My Videos","On-device saved videos • not uploaded or shared");
        final String key10035=videoPreferenceKey10035();
        if(key10035==null){text("Sign in to manage saved videos",15,MUTED,false);button("Back to Me",CARD,this::profile);return;}
        final String videoValue10035=getPreferences(0).getString(key10035,"");
        final java.util.List<String> entries10035=new java.util.ArrayList<>();
        for(String uri10035:videoValue10035.split("\\|\\|\\|"))
            if(!uri10035.trim().isEmpty())entries10035.add(uri10035);
        if(entries10035.isEmpty())text("No saved videos yet. Add one from your phone.",15,MUTED,false);
        for(int index10035=0;index10035<entries10035.size();index10035++){
            final String saved10035=entries10035.get(index10035);
            final int number10035=index10035+1;
            button("▶  Play video "+number10035,CARD,()->{
                if(!key10035.equals(videoPreferenceKey10035()))return;
                try{Intent open10035=new Intent(Intent.ACTION_VIEW);
                    open10035.setDataAndType(Uri.parse(saved10035),"video/*");
                    open10035.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(open10035);
                }catch(Exception ex){Toast.makeText(this,"Video cannot be opened on this phone",Toast.LENGTH_LONG).show();}
            });
            button("Remove saved video "+number10035,CARD,()->new AlertDialog.Builder(this)
                .setTitle("Remove video from KING Plus?")
                .setMessage("Only the saved link in this app will be removed. Your phone's original video stays intact.")
                .setNegativeButton("Cancel",null)
                .setPositiveButton("Remove",(d,w)->{
                    if(!key10035.equals(videoPreferenceKey10035()))return;
                    java.util.List<String> latest10035=new java.util.ArrayList<>(java.util.Arrays.asList(
                        getPreferences(0).getString(key10035,"").split("\\|\\|\\|")));
                    latest10035.remove(saved10035);
                    getPreferences(0).edit().putString(key10035,android.text.TextUtils.join("|||",latest10035)).apply();
                    videoPage();
                }).show());
        }
        button("＋ Add Video",PURPLE,()->{
            FirebaseUser account10035=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
            if(account10035==null){Toast.makeText(this,"Sign in before selecting a video",Toast.LENGTH_SHORT).show();return;}
            videoPickerOwner10035=account10035.getUid();
            Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("video/*");i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(i,26);
        });
        button("Back to Me",CARD,this::profile);
    }

    private void couplingPage(){
        // CP exists only after the other person accepts the Firebase request.
        // A typed name on one phone is not proof of a relationship.
        openCommunityHub700("Relationship");
    }

    /** Paid VIP badge reads from the authenticated wallet server, NOT local gifts. */
    private void loadVerifiedMeVip9988(TextView badge){
        if(badge==null)return;
        if(firebaseAuth==null||firebaseAuth.getCurrentUser()==null){
            badge.setText("VIP —");return;
        }
        final String currentUid=firebaseAuth.getCurrentUser().getUid();
        KingBackend976.fetchWallet(this,(wallet,error)->{
            if(isFinishing()||isDestroyed()||firebaseAuth==null||
                firebaseAuth.getCurrentUser()==null||
                !currentUid.equals(firebaseAuth.getCurrentUser().getUid()))return;
            badge.setText(wallet!=null &&
                KingPrestigeIdentity9988.validVip(wallet.vipLevel)
                ?"VIP "+wallet.vipLevel:"VIP —");
        });
    }

    private void loadRealCoupleCount9985(TextView counter){
        if(counter==null||firestore==null||firebaseAuth==null||
           firebaseAuth.getCurrentUser()==null){if(counter!=null)counter.setText("—");return;}
        final String account9985=firebaseAuth.getCurrentUser().getUid();
        final java.util.List<String[]> pairs9985=new java.util.ArrayList<>();
        final int[] remaining9985={2};
        final boolean[] unavailable9985={false};
        Runnable finish9985=()->{
            remaining9985[0]--;
            if(remaining9985[0]!=0||isFinishing()||isDestroyed()||
               firebaseAuth==null||firebaseAuth.getCurrentUser()==null||
               !account9985.equals(firebaseAuth.getCurrentUser().getUid()))return;
            if(unavailable9985[0])counter.setText("—");
            else counter.setText(String.valueOf(
                KingMeCouplePolicy9985.countAccepted(account9985,pairs9985)));
        };
        java.util.function.Consumer<com.google.firebase.firestore.QuerySnapshot> take9985=snapshot->{
            if(snapshot!=null)for(DocumentSnapshot item:snapshot.getDocuments()){
                String a=item.getString("uidA"),b=item.getString("uidB");
                String status=item.getString("status");
                pairs9985.add(new String[]{a,b,status});
            }
            finish9985.run();
        };
        firestore.collection("relationships").whereEqualTo("uidA",account9985).limit(100)
          .get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(take9985::accept)
          .addOnFailureListener(e->{unavailable9985[0]=true;finish9985.run();});
        firestore.collection("relationships").whereEqualTo("uidB",account9985).limit(100)
          .get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(take9985::accept)
          .addOnFailureListener(e->{unavailable9985[0]=true;finish9985.run();});
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
        if(action!=null)row.setOnClickListener(v->KingSafe.run(this,"ui-action",action));
        host.addView(row,new LinearLayout.LayoutParams(-1,dp(62)));
    }

    private void addProfileVideoTabs940(LinearLayout list){
        LinearLayout tabs=new LinearLayout(this);tabs.setGravity(Gravity.CENTER);tabs.setPadding(0,dp(3),0,dp(8));
        TextView fav=new TextView(this);fav.setText("Favorites");fav.setTextSize(14);fav.setTypeface(null,Typeface.BOLD);fav.setTextColor(0xff222222);fav.setGravity(Gravity.CENTER);fav.setBackground(background(0xffffe500,10));fav.setOnClickListener(v->backpackPage());
        TextView status=new TextView(this);status.setText("Status");status.setTextSize(14);status.setTypeface(null,Typeface.BOLD);status.setTextColor(0xff6f6877);status.setGravity(Gravity.CENTER);status.setBackground(background(0xfff0eef4,10));status.setOnClickListener(v->momentsPage());
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(42),1);p.setMargins(dp(3),0,dp(3),0);tabs.addView(fav,p);tabs.addView(status,new LinearLayout.LayoutParams(p));list.addView(tabs,new LinearLayout.LayoutParams(-1,dp(52)));
    }
    private void addProfileRecommendations940(LinearLayout list){
        TextView title940=new TextView(this);title940.setText("Recommended for you");title940.setTextSize(16);title940.setTypeface(null,Typeface.BOLD);title940.setTextColor(0xff2a2730);title940.setPadding(dp(3),dp(8),0,dp(5));list.addView(title940,new LinearLayout.LayoutParams(-1,dp(42)));
        LinearLayout box940=new LinearLayout(this);box940.setOrientation(LinearLayout.VERTICAL);list.addView(box940,new LinearLayout.LayoutParams(-1,-2));
        TextView loading940=new TextView(this);loading940.setText("Finding KING profiles…");loading940.setTextSize(12);loading940.setTextColor(0xff8a8391);loading940.setPadding(dp(10),dp(12),dp(10),dp(12));box940.addView(loading940,new LinearLayout.LayoutParams(-1,dp(48)));
        if(firestore==null||firebaseAuth==null||firebaseAuth.getCurrentUser()==null){loading940.setText("Sign in to see recommendations");return;}
        final String own=firebaseAuth.getCurrentUser().getUid();
        // A delayed recommendation cannot paint the Me page of another Firebase account.
        firestore.collection("public_profiles").limit(30)
            .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(snap->{
                if(!recommendationsCurrent10032(own,box940,list))return;
                box940.removeAllViews();int shown=0;
                java.util.List<DocumentSnapshot> suggestions10033=new java.util.ArrayList<>(snap.getDocuments());
                java.util.Collections.shuffle(suggestions10033);
                for(DocumentSnapshot d:suggestions10033){
                    String uid=d.getString("uid");if(uid==null||uid.isEmpty())uid=d.getId();
                    if(uid.equals(own))continue;
                    String n=d.getString("displayName");if(n==null||n.trim().isEmpty())n="KING User";
                    Long vip=d.getLong("vipLevel"),lv=d.getLong("level");
                    String verifiedRecommendedId10002=KingPublicId9996.verified(d.getString("publicId"));
                    addProfileRecommendationRow940(box940,uid,n,vip==null?0:vip,lv==null?1:lv,verifiedRecommendedId10002);
                    if(++shown>=5)break;
                }
                if(shown==0){TextView e=new TextView(this);e.setText("No recommendations yet");
                    e.setTextSize(12);e.setTextColor(0xff8a8391);e.setPadding(dp(10),dp(12),dp(10),dp(12));
                    box940.addView(e,new LinearLayout.LayoutParams(-1,dp(48)));}
                TextView refresh10033=new TextView(this);
                refresh10033.setText("↻ Refresh real KING recommendations");
                refresh10033.setTextSize(13);refresh10033.setTextColor(0xff7650b3);
                refresh10033.setGravity(Gravity.CENTER);
                refresh10033.setPadding(dp(8),dp(6),dp(8),dp(6));
                refresh10033.setOnClickListener(v->{
                    if(!recommendationsCurrent10032(own,box940,list)||!refresh10033.isEnabled())return;
                    refresh10033.setEnabled(false);
                    list.removeView(title940);list.removeView(box940);
                    addProfileRecommendations940(list);
                });
                box940.addView(refresh10033,new LinearLayout.LayoutParams(-1,dp(45)));
            }).addOnFailureListener(error->{
                if(recommendationsCurrent10032(own,box940,list)){
                    loading940.setText("Recommendations unavailable • tap to retry");
                    loading940.setOnClickListener(v->{if(recommendationsCurrent10032(own,box940,list)){
                        list.removeView(title940);list.removeView(box940);addProfileRecommendations940(list);}});
                }
            });
    }
    private boolean recommendationsCurrent10032(String owner,LinearLayout box,LinearLayout list){
        com.google.firebase.auth.FirebaseUser signed=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
        return !isFinishing()&&!isDestroyed()&&"profile".equals(screen)&&signed!=null
            &&owner.equals(signed.getUid())&&box.getParent()==list&&list.isAttachedToWindow();
    }
    private void addProfileRecommendationRow940(LinearLayout box,String uid,String name,long vip,long level,String issuedKingId10002){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(10),dp(8),dp(10),dp(8));row.setBackground(background(Color.WHITE,12));
        TextView av=new TextView(this);av.setText(name.isEmpty()?"K":name.substring(0,1).toUpperCase());av.setTextSize(18);av.setTypeface(null,Typeface.BOLD);av.setTextColor(Color.WHITE);av.setGravity(Gravity.CENTER);av.setBackground(background(0xff7c62d5,24));row.addView(av,new LinearLayout.LayoutParams(dp(48),dp(48)));
        LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);info.setPadding(dp(10),0,0,0);TextView nm=new TextView(this);nm.setText(name);nm.setTextSize(14);nm.setTypeface(null,Typeface.BOLD);nm.setTextColor(0xff25222a);info.addView(nm,new LinearLayout.LayoutParams(-1,dp(25)));TextView sub=new TextView(this);sub.setText("VIP "+vip+"  •  Lv."+level+"  •  ID "+(issuedKingId10002==null?"Not issued":issuedKingId10002));sub.setTextSize(10);sub.setTextColor(0xff7b7480);info.addView(sub,new LinearLayout.LayoutParams(-1,dp(20)));row.addView(info,new LinearLayout.LayoutParams(0,dp(48),1));TextView arrow=new TextView(this);arrow.setText("›");arrow.setTextSize(24);arrow.setTextColor(0xffaaa4ad);arrow.setGravity(Gravity.CENTER);row.addView(arrow,new LinearLayout.LayoutParams(dp(30),dp(48)));row.setOnClickListener(v->{Intent i=new Intent(this,KingPublicProfileActivity.class);i.putExtra("uid",uid);i.putExtra("name",name);startActivity(i);});LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(70));rp.setMargins(0,dp(4),0,dp(4));box.addView(row,rp);
    }

    private void rankingsPage(){
        screen="rankings"; base("Rankings","Real KING Plus activity only");
        text("👑 KING Rankings",20,Color.WHITE,true);
        if(firebaseAuth==null || firebaseAuth.getCurrentUser()==null || firestore==null){
            text("Sign in to view real KING profiles and live room rankings.",14,MUTED,false);
            button("Sign in",PURPLE,this::login);button("Back to Profile",CARD,this::profile);return;
        }
        text("No sample leaderboard names • ranks use signed-in KING profile progress.",13,MUTED,false);
        button("⭐ Level & XP Rank",PURPLE,()->showGlobalRank952("level"));
        button("💎 VIP Progress Rank",CARD,()->showGlobalRank952("vip"));
        button("🎤 Room Gift Rank",CARD,this::openPartyActivity);
        button("👥 Followers / Friends",CARD,()->startActivity(new Intent(this,SocialActivity.class)));
        text("Top KING progress preview",15,Color.WHITE,true);
        final String requestedScreen952=screen;
        firestore.collection("public_profiles").limit(60).get().addOnSuccessListener(q->{
            if(!requestedScreen952.equals(screen))return;
            java.util.List<com.google.firebase.firestore.DocumentSnapshot> docs=new ArrayList<>(q.getDocuments());
            java.util.Collections.sort(docs,(a,b)->Long.compare(rankMetric952(b,"level"),rankMetric952(a,"level")));
            if(docs.isEmpty()){text("No public KING profiles available yet.",13,MUTED,false);return;}
            int shown=0;for(com.google.firebase.firestore.DocumentSnapshot d:docs){if(shown>=5)break;String n=d.getString("displayName");if(n==null||n.trim().isEmpty())n="KING User";Long lv=d.getLong("level"),vip=d.getLong("vipLevel");String medal=shown==0?"🥇":shown==1?"🥈":shown==2?"🥉":"#"+(shown+1);text(medal+"  "+n+"  •  Lv."+(lv==null?1:lv)+"  •  VIP "+(vip==null?0:vip),14,Color.WHITE,true);shown++;}
        }).addOnFailureListener(e->{if(requestedScreen952.equals(screen))text("Ranking preview unavailable right now.",13,MUTED,false);});
        button("Back to Profile",CARD,this::profile);
    }
    private int vipPreviewLevel540=0;
    private void vipPage(){
        startActivity(new Intent(this,KingVipVisualActivity.class));
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
        screen="checkin"; base("Daily Check-in","Server-verified rewards only");
        text("Daily check-in is not active in this TEST build.",18,Color.WHITE,true);
        text("There is no deployed KING Plus service verifying check-in streaks or granting rewards, so this page cannot mark a check-in or add coins/XP.",14,MUTED,false);
        button("Open Tasks & Check-in",PURPLE,()->openDeepFlow810("task_center"));
        button("Back to Profile",CARD,this::profile);
    }

    private void missionsPage(){
        screen="missions"; base("Missions","Progress and rewards require a verified service");
        text("Daily mission tracking is not active in this TEST build.",18,Color.WHITE,true);
        text("Device-only activity is not proof of a completed mission. No mission can claim coins or XP here.",14,MUTED,false);
        button("Open Tasks & Check-in",PURPLE,()->openDeepFlow810("task_center"));
        button("Back to Profile",CARD,this::profile);
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
    private void sendGift(LinearLayout chat,String gift,int price,String target){
        // Never subtract locally invented coins or claim a gift was sent online.
        new AlertDialog.Builder(this).setTitle("FREE Gift • "+gift)
            .setMessage("Select the actual KING Plus recipient in a live Party or Private Chat. No local TEST coins are charged.")
            .setPositiveButton("Open Party",(d,w)->openPartyActivity())
            .setNegativeButton("Close",null).show();
    }
    private void manageAdmins(){Set<String> admins=new HashSet<>(getPreferences(0).getStringSet("room_admins_"+safeKey(currentRoom),new HashSet<>()));String[] names={"Lily","Alex","Mia","DJ Max"};boolean[] checked=new boolean[names.length];for(int i=0;i<names.length;i++)checked[i]=admins.contains(names[i]);new AlertDialog.Builder(this).setTitle("Manage admins").setMultiChoiceItems(names,checked,(d,w,isChecked)->{if(isChecked)admins.add(names[w]);else admins.remove(names[w]);}).setPositiveButton("Save",(d,w)->{getPreferences(0).edit().putStringSet("room_admins_"+safeKey(currentRoom),admins).apply();Toast.makeText(this,"Admins updated",Toast.LENGTH_SHORT).show();}).setNegativeButton("Cancel",null).show();}
    private int getMissionValue(String key){return 0;}
    private void appendTransaction(String line){String old=getPreferences(0).getString("transactions","");String stamp=new java.text.SimpleDateFormat("dd MMM HH:mm",java.util.Locale.getDefault()).format(new java.util.Date());String updated=stamp+" • "+line+(old.isEmpty()?"":"|||"+old);getPreferences(0).edit().putString("transactions",updated).apply();}
    private long rankMetric952(com.google.firebase.firestore.DocumentSnapshot d,String mode){Long level=d.getLong("level"),xp=d.getLong("xp"),vip=d.getLong("vipLevel"),vp=d.getLong("vipPoints");long l=level==null?1:level,x=xp==null?0:xp,v=vip==null?0:vip,p=vp==null?0:vp;return "vip".equals(mode)?v*1000000000L+p:l*1000000000L+x;}
    private void showGlobalRank952(String mode){
        if(firestore==null){Toast.makeText(this,"Rankings unavailable",Toast.LENGTH_SHORT).show();return;}
        final String title="vip".equals(mode)?"💎 VIP Progress Rank":"⭐ Level & XP Rank";
        // This query checks a maximum of 100 profiles. Do not label it an all-time global ranking.
        final String rankingOwner10020=firebaseAuth==null||firebaseAuth.getCurrentUser()==null?"":firebaseAuth.getCurrentUser().getUid();
        if(rankingOwner10020.isEmpty()){Toast.makeText(this,"Sign in to view rankings",Toast.LENGTH_SHORT).show();return;}
        LinearLayout load=new LinearLayout(this);load.setGravity(Gravity.CENTER);android.widget.ProgressBar spin=new android.widget.ProgressBar(this);load.addView(spin,new LinearLayout.LayoutParams(dp(42),dp(42)));TextView msg=new TextView(this);msg.setText("  Loading real KING profiles…");msg.setTextSize(13);load.addView(msg);
        final AlertDialog waiting=new AlertDialog.Builder(this).setTitle(title).setView(load).setCancelable(false).create();waiting.show();
        firestore.collection("public_profiles").limit(100).get().addOnSuccessListener(q->{
            if(waiting.isShowing())waiting.dismiss();
            if(isFinishing()||isDestroyed()||firebaseAuth==null||firebaseAuth.getCurrentUser()==null
                ||!rankingOwner10020.equals(firebaseAuth.getCurrentUser().getUid()))return;
            java.util.List<com.google.firebase.firestore.DocumentSnapshot> docs=new ArrayList<>(q.getDocuments());java.util.Collections.sort(docs,(a,b)->Long.compare(rankMetric952(b,mode),rankMetric952(a,mode)));
            LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(12),dp(8),dp(12),dp(10));ScrollView sc=new ScrollView(this);LinearLayout rows=new LinearLayout(this);rows.setOrientation(LinearLayout.VERTICAL);sc.addView(rows);
            int rank=1;for(com.google.firebase.firestore.DocumentSnapshot d:docs){if(rank>30)break;String n=d.getString("displayName");if(n==null||n.trim().isEmpty())n="KING User";Long lv=d.getLong("level"),vip=d.getLong("vipLevel"),xp=d.getLong("xp"),vp=d.getLong("vipPoints");String medal=rank==1?"🥇":rank==2?"🥈":rank==3?"🥉":"#"+rank;TextView row=new TextView(this);row.setText(medal+"  "+n+"\\n"+("vip".equals(mode)?"VIP "+(vip==null?0:vip)+" • "+(vp==null?0:vp)+" points":"Lv."+(lv==null?1:lv)+" • "+(xp==null?0:xp)+" XP"));row.setTextSize(13);row.setTextColor(0xff27232c);row.setTypeface(null,Typeface.BOLD);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(12),dp(5),dp(12),dp(5));row.setBackground(background(rank<=3?0xfffff4ca:0xfff7f5f9,12));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(58));lp.setMargins(0,dp(2),0,dp(2));rows.addView(row,lp);rank++;}if(rank==1){TextView empty=new TextView(this);empty.setText("No public KING profiles available yet.");empty.setGravity(Gravity.CENTER);rows.addView(empty,new LinearLayout.LayoutParams(-1,dp(100)));}root.addView(sc,new LinearLayout.LayoutParams(-1,Math.min(dp(620),Math.max(dp(120),(rank-1)*dp(61)))));new AlertDialog.Builder(this).setTitle(title+" • 100-profile sample").setMessage("Rankings are based on up to 100 retrieved public profiles, not all KING users.").setView(root).setPositiveButton("Refresh",(d,w)->showGlobalRank952(mode)).setNegativeButton("Close",null).show();
         }).addOnFailureListener(e->{if(waiting.isShowing())waiting.dismiss();
            if(isFinishing()||isDestroyed()||firebaseAuth==null||firebaseAuth.getCurrentUser()==null
                ||!rankingOwner10020.equals(firebaseAuth.getCurrentUser().getUid()))return;
            new AlertDialog.Builder(this).setTitle(title).setMessage("Ranking unavailable right now.").setPositiveButton("Retry",(d,w)->showGlobalRank952(mode)).setNegativeButton("Close",null).show();});
    }

    private void transactionHistoryPage(){screen="transactions";base("Transaction History","Local wallet activity");String raw=getPreferences(0).getString("transactions","");if(raw.isEmpty())text("No wallet activity yet",15,MUTED,false);else for(String x:raw.split("\\|\\|\\|"))text(x,15,Color.WHITE,false);button("Back to Wallet",PURPLE,this::walletPage);}
    private void toggleSetting(String key,String label,Runnable refresh){boolean def=!"privacy_friends_dm".equals(key);boolean v=!getPreferences(0).getBoolean(key,def);getPreferences(0).edit().putBoolean(key,v).apply();Toast.makeText(this,label+": "+(v?"ON":"OFF"),Toast.LENGTH_SHORT).show();refresh.run();}
    private void shareMyIssuedKingId9998(){
        FirebaseUser current=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
        if(current==null||firestore==null){Toast.makeText(this,"Sign in to share your KING ID",Toast.LENGTH_SHORT).show();return;}
        final String account=current.getUid();
        firestore.collection("public_profiles").document(account)
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(p->{
                if(isFinishing()||isDestroyed()||firebaseAuth.getCurrentUser()==null
                    ||!account.equals(firebaseAuth.getCurrentUser().getUid()))return;
                String id=KingPublicId9996.verified(p.getString("publicId"));
                if(id==null){Toast.makeText(this,"Your KING ID is not issued yet",Toast.LENGTH_SHORT).show();return;}
                shareText("Join me on KING Plus • KING ID "+id);
            }).addOnFailureListener(e->{if(!isFinishing()&&!isDestroyed())
                Toast.makeText(this,"Cannot verify your KING ID now. Retry online.",Toast.LENGTH_LONG).show();});
    }
    private void shareText(String value){Intent send=new Intent(Intent.ACTION_SEND);send.setType("text/plain");send.putExtra(Intent.EXTRA_TEXT,value);startActivity(Intent.createChooser(send,"Share with"));}
    private void appendNotification(String value){String old=getPreferences(0).getString("notifications_log","");getPreferences(0).edit().putString("notifications_log",value+(old.isEmpty()?"":"|||"+old)).putBoolean("notifications_read",false).apply();}
    private void newMessageDialog(){final EditText e=new EditText(this);e.setHint("User name");new AlertDialog.Builder(this).setTitle("New message").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Open",(d,w)->{String n=e.getText().toString().trim();if(n.isEmpty())n="New Friend";conversation(n);}).show();}
    private void selectCosmetic730(boolean frame){
        Intent i=new Intent(this,KingFrameGallery981Activity.class);
        i.putExtra("frames",frame);
        startActivity(i);
    }
    private void selectCosmetic(String key,String[] values,Runnable refresh){int selected=0;String current=getPreferences(0).getString(key,values[0]);for(int i=0;i<values.length;i++)if(values[i].equals(current))selected=i;new AlertDialog.Builder(this).setTitle("Choose item").setSingleChoiceItems(values,selected,(d,w)->{getPreferences(0).edit().putString(key,values[w]).apply();d.dismiss();refresh.run();}).setNegativeButton("Cancel",null).show();}

    private void helpCenterPage(){screen="help";base("Help & Feedback","KING Plus support center");button("Login & OTP help",CARD,this::authDiagnostics910);
        button("🔌 Test Firebase / Party Room connection",CARD,
            ()->startActivity(new Intent(this,KingConnection983Activity.class)));button("Share crash diagnostics",CARD,this::shareDiagnostics910);button("Clear crash diagnostics",CARD,()->{KingStability.clear(this);Toast.makeText(this,"Diagnostics cleared",Toast.LENGTH_SHORT).show();});button("Voice room help",CARD,()->new AlertDialog.Builder(this).setTitle("Voice rooms").setMessage("Join a seat, allow microphone permission, then tap Mic. Live multi-user audio still requires a real-time voice service/backend.").setPositiveButton("OK",null).show());button("Send feedback",PURPLE,()->{final EditText e=new EditText(this);e.setHint("Describe the issue or suggestion");new AlertDialog.Builder(this).setTitle("Feedback").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{String m=e.getText().toString().trim();if(!m.isEmpty()){String old=getPreferences(0).getString("feedback","");getPreferences(0).edit().putString("feedback",m+"|||"+old).apply();Toast.makeText(this,"Feedback saved",Toast.LENGTH_SHORT).show();}}).show();});button("Back to Settings",CARD,this::settingsPage);}

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
    private long lastProfileAttempt996;
    @Override protected void onResume(){
        super.onResume();
        String resumedUid10030=firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null
            ?firebaseAuth.getCurrentUser().getUid():null;
        if("profile".equals(screen)&&!java.util.Objects.equals(renderedProfileUid10030,resumedUid10030)){
            com.google.firebase.auth.FirebaseUser signed10031=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
            displayName=signed10031==null||signed10031.getDisplayName()==null
                ||signed10031.getDisplayName().trim().isEmpty()
                ?"KING User":signed10031.getDisplayName().trim();
        }
        if("profile".equals(screen)
            &&!java.util.Objects.equals(renderedProfileUid10030,resumedUid10030)){
            stopWallet9989();stopProfileFollowUpdates965();profile();return;
        }
        if("wallet".equals(screen)
            &&!java.util.Objects.equals(renderedWalletUid10030,resumedUid10030)){
            stopWallet9989();walletPage();return;
        }
        if(firestore!=null&&firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null){
            long now=android.os.SystemClock.elapsedRealtime();
            if(lastProfileAttempt996==0||now-lastProfileAttempt996>120000L){
                lastProfileAttempt996=now;
                syncPublicProfile();
            }
        }
        // Refresh the real hosted-room list after returning from Party creation/closure.
        // Generation guards prevent in-flight replies from an older account overwriting it.
        if("profile".equals(screen)&&meHostedRoomsHost10018!=null
                &&meHostedRoomsHost10018.getParent()!=null)
            loadPartyRecordsV600(meHostedRoomsHost10018);
        if("profile".equals(screen) && followingListener965==null && followersListener965==null
                && firestore!=null && firebaseAuth!=null && firebaseAuth.getCurrentUser()!=null)
            startProfileFollowUpdates965(firebaseAuth.getCurrentUser().getUid());
        if("profile".equals(screen)){
            refreshServerWallet();
            // Returning from another screen must pick up visits made on another phone.
            if(firebaseAuth!=null && firebaseAuth.getCurrentUser()!=null)
                loadProfileVisitors940(firebaseAuth.getCurrentUser().getUid());
        }
        if("wallet".equals(screen)&&walletPageListener10065==null)walletPage();
        if("profile_gallery".equals(screen)&&meCloudGalleryHost9993!=null&&meGalleryListener9993==null)
            showMyCloudGallery9992(meCloudGalleryHost9993);
    }
    @Override protected void onPause() { ++visitorCountGeneration10007; ++visitorDialogGeneration10004; stopMeGallery9993(); stopWallet9989(); stopProfileFollowUpdates965(); super.onPause(); stopMic(); }
    @Override protected void onDestroy() { profileImages10067.shutdownNow(); stopMeGallery9993(); stopWallet9989(); stopProfileFollowUpdates965(); stopMic(); super.onDestroy(); }
    private void notificationsCenter(){
        // The legacy Preferences-only list had dead "Opened" buttons and a
        // local read flag unrelated to actual Firebase notification items.
        // Reuse Inbox's real per-account feed and interactive destinations.
        if(firebaseAuth==null||firebaseAuth.getCurrentUser()==null){
            Toast.makeText(this,"Sign in to view your notifications",Toast.LENGTH_SHORT).show();
            return;
        }
        Intent inbox10010=new Intent(this,InboxActivity.class);
        inbox10010.putExtra("openNotifications10010",true);
        startActivity(inbox10010);
    }

    private void searchCommunity(){
        startActivity(new Intent(this,SocialActivity.class));
    }

    private void createRoomDialog(){
        // Legacy local preview creation is disabled. The real Party screen creates Firebase rooms.
        openPartyActivity();
    }

}
