package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PartyActivity extends androidx.fragment.app.FragmentActivity implements org.jitsi.meet.sdk.JitsiMeetActivityInterface {
    private static final int MUSIC_PICK_REQUEST = 760;
    private static final int ROOM_COVER_PICK_REQUEST = 761;
    private static final int BG = 0xff24133f;
    private static final int BG2 = 0xff160d2a;
    private static final int CARD = 0xff3a2856;
    private static final int PURPLE = 0xff8a49ed;
    private static final int PINK = 0xffe04f9d;
    private static final int MUTED = 0xffcfc4df;
    private static final int GOLD = 0xffffd768;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
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
    private boolean micTogglePending968;
    private boolean localMicConsent9956=false;
    private String consentRoom9956="";
    private int consentSeat9956=-1;
    private void revokeMicConsent9956(){
        localMicConsent9956=false;consentRoom9956="";consentSeat9956=-1;
        setInlineAudioMuted940(true);
    }
    private boolean canTransmitMic9956(){
        FirebaseUser signed10065=FirebaseAuth.getInstance().getCurrentUser();
        if(user==null||signed10065==null||!user.getUid().equals(signed10065.getUid()))return false;
        return KingPartyMicConsent9956.canTransmit(localMicConsent9956,cloudRoom,
            roomId,consentRoom9956,mySeat,consentSeat9956,
            user==null?null:user.getUid(),seatUids.get(mySeat),
            micOn,muteAll,isModerator());
    }

    private boolean soundOn = true;
    private boolean roomLocked;
    private boolean muteAll;
    private String announcement = "Welcome to KING Plus • Be friendly and have fun";
    private int localCoins = 2500;
    private String roomCategory = "Hot";
    private String roomTheme = "Classic";
    private String currentSong = "";
    private String lobbyFilter = "";
    private int maxSeats = 8;
    private int maxMembers930 = 100;
    private String pendingCreateCategory = "Hot";
    private int pendingCreateSeats = 8;
    private String pendingCreateName953="";
    private boolean pendingCreatePrivate = false;
    private Uri pendingCreateCoverUri = null;
    private boolean hostSeatMode = false;
    private boolean skipEntranceVisuals = false;
    private boolean skipEntranceSound = false;
    private boolean skipEntranceVibration = false;
    private String activeRoomTemplate = "Chat room";
    private final List<String> memberNames = new ArrayList<>();
    private final Map<String,String> memberUids = new HashMap<>();
    private final Set<Integer> localRemovedSeats = new HashSet<>();
    private boolean coHost;
    private boolean roomPrivate;
    private boolean memberSeen;
    private ListenerRegistration roleListener;
    private ListenerRegistration selfMemberListener;
    private ListenerRegistration roomBanListener;
    private boolean roomHasPassword;
    private boolean memberRecoveryDialogOpen10077;
    private String roomEntranceMode9931="legacy";
    private String roomEntryEpoch9936="";
    private final Set<Integer> lockedSeats = new HashSet<>();
    private ListenerRegistration seatLocksListener;
    private ListenerRegistration seatRequestsListener;
    private int pendingSeatRequests;
    private int liveMemberCount;

    private LinearLayout page;
    private LinearLayout seatsBox;
    private LinearLayout feedBox;
    private LinearLayout chatBox;
    private ScrollView roomChatScroll;
    // Only live events created during this visit appear in the live feed.
    // Server history is preserved for the explicit History screen.
    private long roomVisitStarted10026;
    // Show a selected cover locally while its Firebase Storage upload completes.
    private String localCoverRoom10026="";
    private Uri localCoverUri10026;
    private final Map<Integer,View> seatViews560=new HashMap<>();
    private final Map<String,View> activeReactions560=new HashMap<>();
    private TextView viewerLabel;
    private TextView announcementLabel;
    private TextView micLabel;
    private TextView followLabel;
    private EditText composerBox;
    private TextView reactionBanner;
    // One token shared by games, gifts, entrances and reactions: older timers
    // can never hide a newer notification on the same room's overlay.
    private final KingRoomLifecycle10006 roomLifecycle10006=new KingRoomLifecycle10006();
    private int claimBanner10006(TextView banner){
        int token=roomLifecycle10006.nextBanner();
        banner.animate().cancel();banner.clearAnimation();
        banner.setTranslationX(0f);banner.setTranslationY(0f);banner.setAlpha(1f);
        return token;
    }
    private boolean currentBanner10006(TextView banner,int token,int binding){
        return roomLifecycle10006.ownsBanner(token) && banner==reactionBanner
            && binding==roomListenerGeneration982 && !partyUiDead() && partyVisualForeground995;
    }
    private LinearLayout memberStripBox;
    private TextView musicStatusLabel;
    private TextView roomLiveBadge;
    private TextView roomStateLabel;
    private TextView seatRequestLabel;
    private TextView roomLevelLabel;
    private TextView heartLevelLabel;
    private MediaPlayer roomMusicPlayer;
    private final ArrayList<String> musicUris = new ArrayList<>();
    private final ArrayList<String> musicNames = new ArrayList<>();
    private int currentSongIndex = -1;
    private float roomMusicVolume = 1.0f;
    private final ArrayList<String> musicRemoteUrls = new ArrayList<>();
    private ListenerRegistration musicStateListener;
    private ListenerRegistration musicPlaylistListener;
    private ListenerRegistration gameStateListener;
    private long activeGameRound = 0L;
    private String activeGameType = "";
    private String activeGamePrompt = "";
    private String activeGameResult = "";
    private boolean gameSnapshotReady;
    private final ArrayList<String> musicCloudIds = new ArrayList<>();
    private String sharedMusicUrl = "";
    private boolean applyingSharedMusic;

    // KING Plus room controls inspired by the reference screenshots.
    private AlertDialog giftShopDialog;
    private LinearLayout giftGridBox;
    private TextView giftSelectionLabel;
    private TextView giftBalanceLabel;
    private LinearLayout giftCategoryBar867;
    private TextView supporterLabel;
    private final Set<String> processedEventIds = new HashSet<>();
    private boolean eventSnapshotReady;
    private String giftTargetUid = "";
    private String giftTargetName = "";
    private String giftCategory = "All";
    private int giftQuantity = 1;
    private int giftSelectedIndex = 0;
    private final String[] giftNames = {"Gold Rose","Love Heart","Diamond Ring","Sweet Kiss","Lucky Star","Birthday Cake","Magic Wand","Coffee Date","Ice Cream","Candy Box","Teddy Bear","Perfume","Gold Teapot","Gold Mask","Luxury Bag","Jeweled Box","Crystal Swan","Royal Throne","Moon Palace","Golden Horse","Golden Eagle","Royal Crown","Super Car","Sport Bike","Yacht","Private Jet","Helicopter","Rocket","Galaxy Ship","Flying Carpet","Royal Castle","Dream Villa","Firework","Party Bus","Music Stage","DJ Booth","Lucky Fish","Peacock","Butterfly","Unicorn","Panda","Tiger","Lionheart Glory","King Lion","White Wolf","Phoenix","King Dragon","Ice Dragon","Super Star","Diamond Rain","Heart Rain","Galaxy","Universe","Aurora","Meteor Shower","Moon Walk","VIP Crown","Emperor Crown","Royal Scepter","Golden Wings","Angel Wings","Fame Trophy","Champion Cup","KING Throne",
        "Flying Kiss","Kiss Sound","Royal Horn","Heart Pulse","Rose Rain","Balloon Sky","Flying Hearts","Firework Music","Rocket Parade","Disco Dance","Panda Dance","Golden Confetti","VIP Night","Royal Kiss Show","Royal Car Show","Moon Palace Night","Royal Castle","Golden Dragon Sky","Galaxy Kingdom","Private Jet Parade","Crown Storm","Kiss Kingdom","Flying Throne","Universe Royal Entry"
    };
    private final String[] giftIcons = {"🌹","💗","💍","💋","🌟","🎂","🪄","☕","🍦","🍬","🧸","🌸","🫖","🎭","👜","🎁","🦢","🪑","🌙","🐎","🦅","👑","🏎️","🏍️","🛥️","✈️","🚁","🚀","🛸","🧞","🏰","🏡","🎆","🚌","🎤","🎧","🐠","🦚","🦋","🦄","🐼","🐯","🦁","🦁","🐺","🔥","🐉","🐲","⭐","💎","💖","🌌","🪐","🌈","☄️","🌙","♛","👑","⚜️","🪽","😇","🏆","🥇","👑",
        "💋","😘","📯","💓","🌹","🎈","💖","🎆","🚀","🪩","🐼","🎊","🌃","💋","🏎️","🌙","🏰","🐉","🌌","✈️","👑","💝","🪑","✨"
    };
    private final int[] giftCosts = {50,58,68,80,94,110,128,150,176,205,240,281,329,385,450,527,617,721,844,987,1155,1352,1581,1850,2165,2533,2963,3467,4057,4746,5553,6497,7602,8894,10406,12175,14245,16667,19500,22815,26693,31231,36541,42753,50020,58524,68473,80113,93733,109667,128311,150124,175645,205504,240440,281315,329138,385092,450557,520000,520000,520000,520000,520000,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
    };
    private final String[] giftCategories = {"Relationship","Relationship","Relationship","Relationship","Relationship","Relationship","Relationship","Relationship","Relationship","Relationship","Relationship","Relationship","Activity","Activity","Activity","Activity","Activity","Activity","Activity","Activity","Classic","Classic","Classic","Classic","Classic","Classic","Classic","Classic","Flying","Flying","Flying","Flying","Flying","Flying","Flying","Flying","Fame","Fame","Fame","Fame","Fame","Fame","Fame","Fame","Privilege","Privilege","Privilege","Privilege","Privilege","Privilege","Privilege","Privilege","Filters","Filters","Filters","Filters","Filters","Filters","Parcel","Parcel","Parcel","Parcel","Parcel","Parcel",
        "Voice","Voice","Voice","Voice","Relationship","Flying","Flying","Voice","Flying","Voice","Voice","Flying","VIP Night","VIP Night","VIP Night","VIP Night","VIP Night","VIP Night","VIP Night","VIP Night","VIP Night","VIP Night","VIP Night","VIP Night"
    };
    private final int[] giftVipRequired = {0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,2,2,2,2,2,3,3,3,3,3,4,4,4,4,4,5,5,5,5,5,6,6,6,6,6,7,7,7,7,7,8,8,8,8,8,9,9,9,9,9,10,10,10,10,10,11,11,
        0,0,0,0,0,0,0,0,0,0,0,0,1,1,2,3,4,5,6,7,8,9,10,11
    };
    private String pendingReplyName620 = "";
    private String pendingReplyText620 = "";

    private ListenerRegistration roomsListener;
    private ListenerRegistration roomListener;
    private ListenerRegistration seatsListener;
    private ListenerRegistration membersListener;
    private ListenerRegistration messagesListener;
    private ListenerRegistration eventsListener;

    private final Map<String,String> memberPhotos540 = new HashMap<>();
    private final Map<String,Integer> memberVip720 = new HashMap<>();
    private final Map<String,Integer> memberLevel720 = new HashMap<>();
    private final Map<String,String> memberFrame730 = new HashMap<>();
    private final Map<String,String> memberEffect730 = new HashMap<>();
    private String lastGiftActor720 = "";
    private String lastGiftName720 = "";
    private long lastGiftAt720 = 0L;
    private int giftCombo720 = 0;
    // Bitmap cache is measured in bytes, not image count. 24 large photos could exceed 100 MB.
    private final android.util.LruCache<String,Bitmap> photoCache540 =
        new android.util.LruCache<String,Bitmap>(6*1024*1024){
            @Override protected int sizeOf(String key,Bitmap value){
                return value==null?0:value.getByteCount();
            }
        };
    private final java.util.concurrent.ExecutorService roomImageExecutor964 =
        java.util.concurrent.Executors.newFixedThreadPool(2);
    private final Map<Integer,String> seatNames = new HashMap<>();
    private final Map<Integer,String> seatUids = new HashMap<>();
    private final Map<Integer,Boolean> seatMics = new HashMap<>();
    private final Set<Integer> lockedSeats940 = new HashSet<>();
    private ListenerRegistration seatLocksListener940;
    private FrameLayout liveEmojiStageV530;
    private ListenerRegistration liveEmojiListenerV530;
    private ListenerRegistration seatInviteListenerV530;
    private ListenerRegistration roleListenerV530;
    private boolean roomModeratorV530 = false;
    private boolean seatInviteDialogOpenV530 = false;
    private String lastSeatInviteKeyV530 = "";
    private final Set<String> seenLiveEmojiEventsV530 = new HashSet<>();
    private final android.os.Handler onlineHandler900 = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable onlineTick900 = new Runnable(){@Override public void run(){heartbeat900();if(cloudRoom&&roomId!=null&&user!=null)onlineHandler900.postDelayed(this,20000);}};
    private TextView onlineStateLabel900;
    private boolean voiceLaunched900;
    private long voiceLaunchAt900;
    private long lastCleanup910;
    private boolean roomCreateInFlight921;
    private org.jitsi.meet.sdk.JitsiMeetView inRoomVoiceView940;
    private boolean inRoomVoiceJoined940;
    private boolean voiceConferenceConnected10065;
    private KingPartyVoiceMonitor10065 voiceMonitor10065;
    private KingRoomVoice10066 cloudflareVoice10066;
    private FirebaseAuth.AuthStateListener partyAuth10065;
    private TextView voiceStatus10065;
    private AlertDialog voiceConnectionDialog10065;

    private String inRoomVoiceSlug940="";
    private String requestedGame940="";
    private String requestedPanel940="";
    private android.net.ConnectivityManager.NetworkCallback partyNetworkCallback940;
    private boolean partyLastOnline940;
    private String currentLobbyTab940="Hot";
    private FrameLayout partyShell940;
    private boolean roomVoiceOptedIn957=false;
    private boolean voiceReconnectPending9957=false;
    private boolean voiceRejoinChecking9957=false;
    private long lastVoiceRejoinAt9957=0L;
    private final android.os.Handler voiceRejoinHandler9957=
        new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable voiceRejoinRunnable9957;
    private int voiceRejoinToken9957=0;
    private void clearVoiceRejoin9957(){
        ++voiceRejoinToken9957;
        if(voiceRejoinRunnable9957!=null)
            voiceRejoinHandler9957.removeCallbacks(voiceRejoinRunnable9957);
        voiceRejoinRunnable9957=null;
        voiceReconnectPending9957=false;
        voiceRejoinChecking9957=false;
    }
    /** Safe on lifecycle stop: cancel pending work, retain intention for resume. */
    private void pauseVoiceRejoin9957(){
        ++voiceRejoinToken9957;
        if(voiceRejoinRunnable9957!=null)
            voiceRejoinHandler9957.removeCallbacks(voiceRejoinRunnable9957);
        voiceRejoinRunnable9957=null;
        voiceRejoinChecking9957=false;
    }
    private boolean voiceRejoinReady9957(String id,String uid,int generation){
        return KingPartyVoiceReconnect9957.eligible(
            voiceReconnectPending9957,roomVoiceOptedIn957,partyVisualForeground995,
            KingNetwork.online(this),cloudRoom,memberSeen,
            id,roomId,uid,user==null?null:user.getUid(),
            generation==presenceGeneration967,partyShell940!=null);
    }
    private void verifyAndRejoinVoice9957(){
        if(voiceRejoinChecking9957||!voiceReconnectPending9957||db==null||user==null)return;
        final String expectedRoom9957=roomId;
        final String expectedUid9957=user.getUid();
        final int presence9957=presenceGeneration967;
        final int token9957=++voiceRejoinToken9957;
        if(!voiceRejoinReady9957(expectedRoom9957,expectedUid9957,presence9957))return;
        if(System.currentTimeMillis()-lastVoiceRejoinAt9957<8000L)return;
        voiceRejoinChecking9957=true;
        final DocumentReference roomRef9957=db.collection("live_rooms").document(expectedRoom9957);
        roomRef9957.get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(room9957->{
                if(token9957!=voiceRejoinToken9957||!voiceRejoinReady9957(expectedRoom9957,expectedUid9957,presence9957)){
                    voiceRejoinChecking9957=false;return;
                }
                if(room9957==null||!room9957.exists()||
                   Boolean.TRUE.equals(room9957.getBoolean("closed"))){
                    voiceRejoinChecking9957=false;
                    setOnlineState900(false,"Voice room unavailable");return;
                }
                roomRef9957.collection("members").document(expectedUid9957)
                    .get(com.google.firebase.firestore.Source.SERVER)
                    .addOnSuccessListener(member9957->{
                        if(token9957!=voiceRejoinToken9957||
                          !voiceRejoinReady9957(expectedRoom9957,expectedUid9957,presence9957)){
                            voiceRejoinChecking9957=false;return;
                        }
                        if(member9957==null||!member9957.exists()||
                           !expectedUid9957.equals(member9957.getString("uid"))){
                            voiceRejoinChecking9957=false;
                            setOnlineState900(false,"Voice membership not verified");return;
                        }
                        voiceRejoinRunnable9957=()->{
                            voiceRejoinRunnable9957=null;
                            voiceRejoinChecking9957=false;
                            if(token9957!=voiceRejoinToken9957||
                               !voiceRejoinReady9957(expectedRoom9957,expectedUid9957,presence9957))
                                return;
                            lastVoiceRejoinAt9957=System.currentTimeMillis();
                            voiceReconnectPending9957=false;
                            // Any prior native Jitsi conference may be stuck after
                            // connectivity changes. Replace it once after verified rejoin.
                            setInlineAudioMuted940(true);
                            stopInRoomVoice940(true);
                            attachInRoomVoice940(partyShell940);
                            // Never re-enable local microphone from a remote flag.
                            setInlineAudioMuted940(!canTransmitMic9956());
                            if(inRoomVoiceJoined940)voiceState10065("Voice connecting…");
                            else{
                                voiceReconnectPending9957=true;
                                setOnlineState900(false,"Voice reconnect failed • retry Voice");
                            }
                        };
                        voiceRejoinHandler9957.postDelayed(voiceRejoinRunnable9957,700L);
                    }).addOnFailureListener(err9957->{
                        voiceRejoinChecking9957=false;
                        if(token9957==voiceRejoinToken9957)
                            KingStability.nonFatal(this,"voice-rejoin-member",err9957);
                    });
            }).addOnFailureListener(err9957->{
                voiceRejoinChecking9957=false;
                if(token9957==voiceRejoinToken9957)
                    KingStability.nonFatal(this,"voice-rejoin-room",err9957);
            });
    }
    private final List<ListenerRegistration> lobbyMemberCountListeners940=new ArrayList<>();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        try { db = FirebaseFirestore.getInstance(); } catch (Exception ignored) { db = null; }
        try { storage = FirebaseStorage.getInstance(); } catch (Exception ignored) { storage = null; }
        try { user = FirebaseAuth.getInstance().getCurrentUser(); } catch (Exception ignored) { user = null; }
        prefs = getSharedPreferences("king_party", MODE_PRIVATE);
        partyAuth10065=auth->{
            FirebaseUser now=auth.getCurrentUser();String uid=now==null?"":now.getUid();
            if(user!=null&&!uid.equals(user.getUid())&&!isFinishing()&&!isDestroyed()){
                micOn=false;revokeMicConsent9956();leaveRoom();user=now;renderLobby("Hot");
            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(partyAuth10065);
        loadMusicPlaylist();
        displayName = getIntent().getStringExtra("displayName");
        if (displayName == null || displayName.trim().isEmpty()) displayName = safeName();
        requestedGame940=safe(getIntent().getStringExtra("requestedGame"),"");
        requestedPanel940=safe(getIntent().getStringExtra("requestedPanel"),"");
        localCoins = prefs.getInt("coins", 2500);
        partyLastOnline940=KingNetwork.online(this);
        partyNetworkCallback940=KingNetwork.watch(this,online->runOnUiThread(()->{
            boolean recovered=online&&!partyLastOnline940;
            boolean disconnected9957=!online&&partyLastOnline940;
            partyLastOnline940=online;
            if(disconnected9957&&roomVoiceOptedIn957&&cloudRoom&&roomId!=null)
                voiceReconnectPending9957=true;
            if(!recovered||isFinishing()||isDestroyed())return;
            if(roomId==null){renderLobby(currentLobbyTab940);return;}
            if(cloudRoom&&db!=null){
                db.enableNetwork().addOnSuccessListener(v->{
                    if(isFinishing()||isDestroyed())return;
                    if(cloudRoom&&roomId!=null&&memberSeen){
                        ensureRoomMembership900();
                        // Voice is rejoined ONLY for people who explicitly opted in,
                        // and only after fresh SERVER room/member checks.
                        verifyAndRejoinVoice9957();
                        // Resume truly displayed recipient ACK and recover
                        // recent missed server Gifts without reopening Party.
                        recoverGiftsAfterNetworkReturn9968();
                    }else if(cloudRoom&&roomId!=null&&!memberSeen&&
                        !partyReconnecting979&&!partyAutoRepairUsed979){
                        partyAutoRepairUsed979=true;
                        reconnectPartyFirestore979(false);
                    }
                });
            }
        }));
        renderLobby("Hot");
        handlePartyIntent9989(getIntent());
    }

    /** Refresh real Gift delivery when Android network comes back while
     * Party remains on screen. Verify current signed-in member from SERVER
     * before attempting any recipient receipt or animation.
     */
    private void recoverGiftsAfterNetworkReturn9968(){
        if(!KingGiftReturnGate9968.shouldRecover(
            cloudRoom,memberSeen,partyVisualForeground995,db!=null,
            user!=null,roomId!=null&&!roomId.isEmpty()))return;
        final String expectedRoom=roomId,expectedUid=user.getUid();
        final int expectedGeneration=presenceGeneration967;
        db.collection("live_rooms").document(expectedRoom)
          .collection("members").document(expectedUid)
          .get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(member->{
              if(!isActivePresence967(expectedRoom,expectedUid,expectedGeneration)
                ||!partyVisualForeground995||isFinishing()||isDestroyed()
                ||member==null||!member.exists()
                ||!expectedUid.equals(member.getString("uid")))return;
              // Previously animated Gifts must only retry ACK; never
              // replay the same sound/burst for already acknowledged Gifts.
              retryDisplayedGiftReceipts9955();
              recoverRecentGifts9942();
              drainPendingGifts9944();
          }).addOnFailureListener(e->
                KingStability.nonFatal(this,"gift-reconnect-server",e));
    }
    @Override protected void onResume(){
        super.onResume();
        partyVisualForeground995=true;
        listenSavedRooms9990();
        FirebaseUser signed9989=FirebaseAuth.getInstance().getCurrentUser();
        String previous9989=user==null?"":user.getUid();
        String current9989=signed9989==null?"":signed9989.getUid();
        if(!previous9989.equals(current9989)){
            leaveRoom(); // Revoke the previous account's microphone and pending callbacks.
            user=signed9989;
            listenSavedRooms9990();
            renderLobby("Hot");
            return;
        }
        if(voiceReconnectPending9957)verifyAndRejoinVoice9957();
        drainPendingGifts9944();
        retryDisplayedGiftReceipts9955();
        recoverRecentGifts9942();
        if(inRoomVoiceJoined940&&inRoomVoiceView940!=null)try{org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostResume(this);}catch(Throwable e){KingStability.nonFatal(this,"party-jitsi-resume",e);}
        if(cloudRoom&&roomId!=null&&partyShell940!=null&&roomVoiceOptedIn957&&!inRoomVoiceJoined940)attachInRoomVoice940(partyShell940);
        setInlineAudioMuted940(!(micOn&&mySeat>0));
        if(seatsBox!=null)rebuildSeats();
        refreshOwnMemberPhoto868();
        if(voiceLaunched900 && System.currentTimeMillis()-voiceLaunchAt900>500){
            voiceLaunched900=false;
            setVoicePresence900(false);
            if(mySeat>0&&cloudRoom&&db!=null&&user!=null){
                micOn=false;
                muteOwnedSeat9920("return-from-external-voice");
                refreshMicControl();
            }
        }
        if(cloudRoom&&roomId!=null&&user!=null&&memberSeen){ensureRoomMembership900();startHeartbeat900();}
    }

    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
    private String safe(String s,String fallback){ return s==null||s.trim().isEmpty()?fallback:s.trim(); }
    private GradientDrawable bg(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    // Original KING Plus Royal Neon theme, kept programmatic for light APK weight.
    // All tap targets and cloud queries are existing handlers; styling adds none.
    private GradientDrawable royalNeon9924(int start,int end,int stroke,int radius) {
        GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            new int[]{start,end});
        d.setCornerRadius(dp(radius));
        d.setStroke(dp(1),stroke);
        return d;
    }
    private void addRoyalHero9924(LinearLayout host) {
        LinearLayout hero=new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(14),dp(8),dp(14),dp(11));
        hero.setBackground(royalNeon9924(0xff58208b,0xff181036,0xaaffdb81,20));
        TextView crown=tv("👑  KING Plus  ✨",21,0xffffda80,true);
        crown.setGravity(Gravity.CENTER);
        hero.addView(crown,new LinearLayout.LayoutParams(-1,dp(39)));
        TextView caption=tv("MEET  •  CHAT  •  PLAY  •  PARTY",10,0xfff6dffa,true);
        caption.setLetterSpacing(.07f);
        caption.setGravity(Gravity.CENTER);
        hero.addView(caption,new LinearLayout.LayoutParams(-1,dp(24)));
        LinearLayout links=new LinearLayout(this);
        links.setGravity(Gravity.CENTER_VERTICAL);
        String[] labels={"🎙 Voice","📹 Video","🎮 Games"};
        String[] filters={"Hot","Video","Game"};
        for(int i=0;i<labels.length;i++){
            final String choice=filters[i];
            TextView link=tv(labels[i],11,Color.WHITE,true);
            link.setGravity(Gravity.CENTER);
            link.setBackground(royalNeon9924(0xffb536a5,0xff6328a2,0x99ffe3a5,12));
            link.setOnClickListener(v->renderLobby(choice));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(37),1);
            lp.setMargins(dp(3),dp(4),dp(3),0);links.addView(link,lp);
        }
        hero.addView(links,new LinearLayout.LayoutParams(-1,dp(41)));
        LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,dp(123));
        hp.setMargins(dp(5),dp(8),dp(5),dp(10));host.addView(hero,hp);
    }

    private TextView tv(String text, int size, int color, boolean bold) {
        TextView v = new TextView(this); v.setText(text); v.setTextSize(size); v.setTextColor(color);
        if (bold) v.setTypeface(null, Typeface.BOLD);
        v.setPadding(dp(5), dp(6), dp(5), dp(6)); return v;
    }
    private void safePartyAction940(String area,Runnable action){
        try{if(action!=null)action.run();}
        catch(Throwable e){KingStability.nonFatal(this,area,e);if(!isFinishing()&&!isDestroyed())Toast.makeText(this,"Party action could not open safely. Try again.",Toast.LENGTH_LONG).show();}
    }
    private TextView pill(String text, int color, Runnable action) {
        TextView v = tv(text, 13, Color.WHITE, true); v.setGravity(Gravity.CENTER); v.setBackground(bg(color, 18));
        if (action != null) v.setOnClickListener(x -> runPartyAction(text, action)); return v;
    }
    private void runPartyAction(String label, Runnable action){
        if(action==null || isFinishing() || isDestroyed()) return;
        try{ action.run(); }catch(Throwable e){
            KingStability.nonFatal(this,"party-action:"+label,e);
            android.util.Log.e("KINGPlusParty","Party action failed: "+label,e);
            if(!isFinishing()&&!isDestroyed()) toast("Could not complete that action • please try again");
        }
    }
    private boolean partyUiDead(){ return isFinishing() || isDestroyed(); }
    private void setSafeContentView(View root) {
        try{ getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE); }catch(Exception ignored){}
        setContentView(root);
        final int l=root.getPaddingLeft(), t=root.getPaddingTop(), r=root.getPaddingRight(), b=root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root,(v,insets)->{
            Insets bars=insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime=insets.getInsets(WindowInsetsCompat.Type.ime());
            boolean keyboard=insets.isVisible(WindowInsetsCompat.Type.ime());
            // ADJUST_RESIZE already reduces the content height for the keyboard.
            // Applying the entire IME inset again hid the composer on compact screens.
            int bottom=bars.bottom;
            if(v==partyShell940&&cloudRoom&&roomId!=null){
                int visibility10067=keyboard?View.GONE:View.VISIBLE;
                if(seatsBox!=null)seatsBox.setVisibility(visibility10067);
                if(roomActions10067!=null)roomActions10067.setVisibility(visibility10067);
                if(roomBadges10067!=null)roomBadges10067.setVisibility(visibility10067);
            }
            v.setPadding(l,t+bars.top,r,b+bottom);
            if(keyboard&&roomChatScroll!=null)roomChatScroll.post(()->roomChatScroll.fullScroll(View.FOCUS_DOWN));
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }
    private int roomListenerGeneration982;
    private boolean activeRoomCallback982(String room982,String uid982,
                                          int visit982,int binding982){
        if(roomListenerGeneration982!=binding982)return false;
        com.google.firebase.auth.FirebaseUser signed982=null;
        try{signed982=com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();}
        catch(Exception ignored){}
        return KingRoomCallback982.current(room982,uid982,visit982,roomId,
            signed982==null?null:signed982.getUid(),presenceGeneration967,
            cloudRoom,!isFinishing()&&!isDestroyed());
    }

    private boolean partyVisualForeground995=true;
    // Ignore stale Firebase lobby results after page change or navigation.
    private int lobbyRenderGeneration9911;

    /** Clears transient presentation only — never removes membership or seats.
     * Detaching GiftBurstView stops its active ValueAnimator. An entire stage
     * can be removed when Room becomes invisible without affecting Firebase.
     */
    private void clearPartyVisuals995(){
        for(View v995:new ArrayList<>(activeReactions560.values())){
            if(v995==null)continue;
            try{
                v995.animate().cancel();
                v995.clearAnimation();
                if(v995.getParent() instanceof android.view.ViewGroup)
                    ((android.view.ViewGroup)v995.getParent()).removeView(v995);
            }catch(Exception ignored){}
        }
        activeReactions560.clear();
        FrameLayout stage995=liveEmojiStageV530;
        if(stage995!=null){
            try{
                for(int idx995=stage995.getChildCount()-1;idx995>=0;idx995--){
                    View visual995=stage995.getChildAt(idx995);
                    if(visual995==null)continue;
                    visual995.animate().cancel();
                    visual995.clearAnimation();
                    stage995.removeViewAt(idx995);
                }
            }catch(Exception e){
                KingStability.nonFatal(this,"party-visual-trim",e);
            }
        }
        giftFxActive971=0;
        roomLifecycle10006.invalidateBanners();
        if(reactionBanner!=null){
            reactionBanner.animate().cancel();
            reactionBanner.clearAnimation();
            reactionBanner.setVisibility(View.GONE);
        }
    }

    private void clearListeners() {
        if(giftReceiptListener9941!=null){giftReceiptListener9941.remove();giftReceiptListener9941=null;}
        giftReceiptInitial9941=false;
        seenGiftReceipts9941.clear();
        // Cancel pending/unfinished visual deliveries. Their gift IDs become
        // retryable when the Party comes back, rather than faking an ACK.
        for(KingGiftDeliveryQueue9944.Entry<PendingGift9944> item9944:pendingGiftEvents9944.clear())
            shownGiftIds9942.remove(item9944.id);
        for(String id9944:inFlightGiftIds9944)shownGiftIds9942.remove(id9944);
        inFlightGiftIds9944.clear();
        // Preserve only successfully displayed per-room IDs across rerenders.
        ++roomListenerGeneration982; // Cancel queued callbacks from old subscriptions.
        eventSnapshotReady=false;
        processedEventIds.clear();
        try{if(seatLocksListener940!=null)seatLocksListener940.remove();}catch(Exception ignored){}
        seatLocksListener940=null;
        stopHeartbeat900();
        for(ListenerRegistration l940:new ArrayList<>(lobbyMemberCountListeners940)){try{if(l940!=null)l940.remove();}catch(Exception ignored){}}
        lobbyMemberCountListeners940.clear();
        clearPartyVisuals995();
        ListenerRegistration[] ls = {roomsListener,roomListener,seatsListener,membersListener,messagesListener,eventsListener,roleListener,selfMemberListener,roomBanListener,seatLocksListener,seatRequestsListener,musicStateListener,musicPlaylistListener,gameStateListener};
        for (ListenerRegistration l : ls) if (l != null) l.remove();
        if (liveEmojiListenerV530 != null) liveEmojiListenerV530.remove();
        if (seatInviteListenerV530 != null) seatInviteListenerV530.remove();
        if (roleListenerV530 != null) roleListenerV530.remove();
        liveEmojiListenerV530 = seatInviteListenerV530 = roleListenerV530 = null;
        roomModeratorV530 = false;
        seatInviteDialogOpenV530 = false;
        seenLiveEmojiEventsV530.clear();
        roomsListener = roomListener = seatsListener = membersListener = messagesListener = eventsListener = null;
        roleListener = selfMemberListener = roomBanListener = null;
        seatLocksListener = seatRequestsListener = musicStateListener = musicPlaylistListener = gameStateListener = null;
        gameSnapshotReady=false; activeGameRound=0L; activeGameType=""; activeGamePrompt=""; activeGameResult="";
    }

    private boolean partyNavigationPending10033;
    private View roomActions10067,roomBadges10067;
    private int lobbyRoomLimit9989=50;
    private DocumentSnapshot lobbyCursor10067;
    private final ArrayList<DocumentSnapshot> lobbyPrevious10067=new ArrayList<>();
    private String lobbyQuery10067="";
    private int inviteGeneration9989;
    private void handlePartyIntent9989(Intent incoming){
        if(incoming==null)return;
        String requested=incoming.getStringExtra("directRoomId");
        if(requested==null)requested=KingRoomInvite959.parse(incoming.getDataString());
        incoming.removeExtra("directRoomId");
        if(requested==null||requested.isEmpty())return;
        if(!KingMeRoomHistory9981.validId(requested)){
            ++inviteGeneration9989;
            requestedPanel940="";toast("Invalid Party invite");return;
        }
        // Each new invite invalidates older in-flight server checks and switch dialogs.
        final int intentGeneration9989=++inviteGeneration9989;
        final String target=requested;
        requestedPanel940=safe(incoming.getStringExtra("requestedPanel"),"");
        incoming.removeExtra("requestedPanel");
        if(target.equals(roomId)){
            if(!memberSeen)return;
            final String owner10040=user==null?"":user.getUid();
            if(db==null||!KingSixPages10040.sameAccount(owner10040,
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser()==null?null:
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid()))return;
            db.collection("live_rooms").document(target).get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(live10040->{
                    if(intentGeneration9989!=inviteGeneration9989||partyUiDead()||user==null
                        ||!owner10040.equals(user.getUid())||!target.equals(roomId))return;
                    if(live10040==null||!live10040.exists()||Boolean.TRUE.equals(live10040.getBoolean("closed"))){
                        toast("This Party Room is closed");leaveRoom();return;
                    }
                    openRequestedPanel940();
                }).addOnFailureListener(e->roomJoinError891("Room verification failed",e));
            return;
        }
        if(db==null||user==null){toast("Sign in to open Party invites");return;}
        Runnable join=()->{
            // Verify the destination BEFORE leaving the currently joined room.
            // A stale/invalid favorite or invite must never throw users out.
            if(intentGeneration9989!=inviteGeneration9989)return;
            final int generation=intentGeneration9989;
            final String account=user.getUid();
            db.collection("live_rooms").document(target)
                .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(doc->{
                    if(generation!=inviteGeneration9989||partyUiDead()||user==null
                        ||!account.equals(user.getUid()))return;
                    FirebaseUser signed=FirebaseAuth.getInstance().getCurrentUser();
                    if(signed==null||!account.equals(signed.getUid()))return;
                    if(!doc.exists()||Boolean.TRUE.equals(doc.getBoolean("closed"))){
                        requestedPanel940="";
                        toast("This Party room is unavailable. You stayed in your current room.");return;
                    }
                    if(roomId!=null)leaveRoom();
                    openRoomDoc891(doc);
                }).addOnFailureListener(e->{
                    if(generation==inviteGeneration9989&&!partyUiDead()){
                        requestedPanel940="";
                        toast("Cannot verify Party invite. Check connection and retry.");
                    }
                });
        };
        if(roomId==null)join.run();
        else new AlertDialog.Builder(this).setTitle("Switch Party room?")
            .setMessage("Leave this room and open the invited Party?")
            .setOnCancelListener(d->{if(intentGeneration9989==inviteGeneration9989){++inviteGeneration9989;requestedPanel940="";}})
            .setNegativeButton("Stay",(d,w)->{if(intentGeneration9989==inviteGeneration9989){++inviteGeneration9989;requestedPanel940="";}})
            .setPositiveButton("Switch",(d,w)->join.run()).show();
    }

    private void renderLobby(String selected) {
        ++inviteGeneration9989;
        final int generation9911=++lobbyRenderGeneration9911;
        stopPartyJoinTimeout977();
        ++partyReconnectGeneration979;clearPartyReconnect979();
        currentLobbyTab940=selected==null||selected.trim().isEmpty()?"Hot":selected;
        String query10067=currentLobbyTab940+"\n"+lobbyFilter;
        if(!query10067.equals(lobbyQuery10067)){
            lobbyQuery10067=query10067;lobbyCursor10067=null;lobbyPrevious10067.clear();
        }
        ++roomJoinGeneration966; // Invalidate all pending Firebase room-join callbacks.
        clearVoiceRejoin9957();
        stopInRoomVoice940(true);
        roomVoiceOptedIn957=false;
        revokeMicConsent9956();
        ++presenceGeneration967; micTogglePending968=false; giftSendPending971=false; giftFxActive971=0; // Clear previous room visuals.
        clearListeners(); clearDisplayedGiftReceipts9955();
        // Do not let a previous Party keep playing its shared soundtrack
        // after Leaving or switching into the Home/Party lobby.
        releaseMusicPlayer();sharedMusicUrl="";currentSong="";
        currentSongIndex=-1;applyingSharedMusic=false;musicStatusLabel=null;
        unregisterMember(); // Capture old room + session for guarded server release first.
        if(chatBox!=null)chatBox.removeAllViews();
        if(feedBox!=null)feedBox.removeAllViews();
        roomChatScroll=null; chatBox=null; feedBox=null; composerBox=null;
        seatNames.clear();seatUids.clear();seatMics.clear();lockedSeats.clear();
        memberNames.clear();memberUids.clear();memberPhotos540.clear();
        memberVip720.clear();memberLevel720.clear();memberFrame730.clear();memberEffect730.clear();
        mySeat=-1;micOn=false;liveMemberCount=0;pendingSeatRequests=0;
        pendingReplyName620="";pendingReplyText620="";
        roomVisitStarted10026=0L;
        roomId = null; cloudRoom = false; memberSeen=false;
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{0xff100824,0xff261344,0xff10091f}));

        // Consistent KING Plus identity, explicit live status and familiar room-create icon.
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(14),dp(12),dp(10),dp(9));
        head.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            new int[]{0xff12091f,0xff57217d,0xff1d103e}));
        LinearLayout titleStack9911=new LinearLayout(this);titleStack9911.setOrientation(LinearLayout.VERTICAL);
        TextView brand=tv("👑 KING Plus",24,0xffffdc88,true);
        brand.setShadowLayer(dp(4),0,0,0x88fb73ff);
        titleStack9911.addView(brand,new LinearLayout.LayoutParams(-1,dp(39)));
        String connection9911=user==null?"SIGN IN FOR LIVE ROOMS":(KingNetwork.online(this)?"● LIVE PARTY ROOMS":"● OFFLINE • RETRY WHEN CONNECTED");
        TextView status9911=tv(connection9911,10,0xffffdd8b,true);titleStack9911.addView(status9911,new LinearLayout.LayoutParams(-1,dp(24)));
        head.addView(titleStack9911,new LinearLayout.LayoutParams(0,dp(64),1));
        TextView searchIcon=tv("⌕",28,Color.WHITE,false);searchIcon.setGravity(Gravity.CENTER);searchIcon.setContentDescription("Search Party rooms");searchIcon.setOnClickListener(v->showLobbySearch(selected));head.addView(searchIcon,new LinearLayout.LayoutParams(dp(42),dp(50)));
        TextView history940=tv("🕘",18,Color.WHITE,false);history940.setGravity(Gravity.CENTER);history940.setContentDescription("Recent and favorite rooms");history940.setOnClickListener(v->roomHistoryPanel940());head.addView(history940,new LinearLayout.LayoutParams(dp(42),dp(50)));
        TextView hot=tv("🏠",23,Color.WHITE,false);hot.setGravity(Gravity.CENTER);hot.setContentDescription("Create Party");hot.setOnClickListener(v->{if(user!=null&&db!=null){pendingCreateCategory="Video".equalsIgnoreCase(selected)?"Multi Video":selected;createRoomDialog();}else requireSignInForCreate();});head.addView(hot,new LinearLayout.LayoutParams(dp(46),dp(50)));root.addView(head);

        HorizontalScrollView chipScroll = new HorizontalScrollView(this); chipScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout chips = new LinearLayout(this); chips.setOrientation(LinearLayout.HORIZONTAL); chips.setPadding(dp(12),dp(3),dp(12),dp(8)); chipScroll.addView(chips);
        String[] names = {"Hot","Event","Date","Music","Game","Video"};
        for (String n : names) {
            boolean active=n.equalsIgnoreCase(selected);
            TextView t = tv(n,13,active?0xff241033:0xfff4e8ff,active);
            t.setGravity(Gravity.CENTER);
            t.setBackground(active
                ?royalNeon9924(0xffffe8a3,0xffffc66a,0xffffdca6,12)
                :royalNeon9924(0xff432567,0xff211339,0x88a36ec9,12));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(74),dp(32)); p.setMargins(dp(3),0,dp(3),0); chips.addView(t,p);
            t.setOnClickListener(v -> renderLobby(n));
        }
        root.addView(chipScroll,new LinearLayout.LayoutParams(-1,dp(46)));
        TextView joinLink959=pill("🔗 Join by link / Room ID",0xff6244a0,this::joinRoomLinkDialog959);
        joinLink959.setBackground(royalNeon9924(0xffc437a3,0xff6630a7,0xaaffdf92,15));
        joinLink959.setTextSize(12);
        joinLink959.setContentDescription("Join KING Plus Party using invitation link or full room ID");
        LinearLayout.LayoutParams joinLp959=new LinearLayout.LayoutParams(-1,dp(42));
        joinLp959.setMargins(dp(12),0,dp(12),dp(5));
        root.addView(joinLink959,joinLp959);

        if(!lobbyFilter.isEmpty()) {
            LinearLayout filterBar=new LinearLayout(this);filterBar.setGravity(Gravity.CENTER_VERTICAL);filterBar.setPadding(dp(14),0,dp(14),dp(6));
            TextView f=tv("Search all categories: "+lobbyFilter,12,0xffffd88f,true);
            f.setBackground(bg(0xff3e2459,14));f.setGravity(Gravity.CENTER_VERTICAL);filterBar.addView(f,new LinearLayout.LayoutParams(0,dp(32),1));
            TextView clear=tv("×",22,0xffffdc91,true);clear.setGravity(Gravity.CENTER);clear.setOnClickListener(v->{lobbyFilter="";renderLobby(selected);});LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(dp(42),dp(32));cp.setMargins(dp(6),0,0,0);filterBar.addView(clear,cp);root.addView(filterBar);
        }

        ScrollView scroll = new ScrollView(this); page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(10),dp(2),dp(10),dp(16));
        scroll.addView(page);
        addRoyalHero9924(page);
        if (user != null && db != null) {
            final String lobbyOwner10035=user.getUid();
            LinearLayout cloudList = new LinearLayout(this); cloudList.setOrientation(LinearLayout.VERTICAL); page.addView(cloudList);
            TextView lobbyState940=tv(KingNetwork.online(this)?"Loading live Party rooms…":"Offline • checking cached Party rooms…",13,0xffdacdf1,true);
            lobbyState940.setGravity(Gravity.CENTER);
            lobbyState940.setBackground(royalNeon9924(0xff372050,0xff1b1231,0x88bb88d8,14));LinearLayout.LayoutParams lobbyStateLp940=new LinearLayout.LayoutParams(-1,dp(64));lobbyStateLp940.setMargins(dp(6),dp(8),dp(6),0);cloudList.addView(lobbyState940,lobbyStateLp940);
            java.util.List<String> aliases10067=KingPagePolicy10067.categories(selected);
            // Search spans all room categories. Browsing without a search keeps
            // the category-specific server query and its cursor pagination.
            Query lobbyRequest10067=!lobbyFilter.trim().isEmpty()
                ?db.collection("live_rooms").whereEqualTo("closed",false)
                :aliases10067.isEmpty()
                    ?db.collection("live_rooms").whereEqualTo("closed",false)
                    :db.collection("live_rooms").whereIn("category",aliases10067);
            if(lobbyCursor10067!=null)lobbyRequest10067=lobbyRequest10067.startAfter(lobbyCursor10067);
            roomsListener = lobbyRequest10067.limit(lobbyRoomLimit9989)
                .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(snap,error) -> {
                    FirebaseUser currentLobbyAccount10035=FirebaseAuth.getInstance().getCurrentUser();
                    if(partyUiDead() || generation9911!=lobbyRenderGeneration9911 || roomId!=null
                        ||currentLobbyAccount10035==null||!lobbyOwner10035.equals(currentLobbyAccount10035.getUid()))return;
                    if (error != null) { showLobbyError(cloudList,KingNetwork.online(this)?"Live rooms unavailable • tap to retry":"Offline • cached rooms unavailable • tap to retry",selected); return; }
                    cloudList.removeAllViews();
                    final boolean cachedLobby10033=snap!=null&&snap.getMetadata().isFromCache();
                    if(cachedLobby10033){
                        TextView cached10033=tv("Offline/cached rooms • opening any room requires fresh server verification",12,0xffffd98c,true);
                        cached10033.setGravity(Gravity.CENTER_VERTICAL);cached10033.setPadding(dp(10),dp(6),dp(10),dp(6));
                        cloudList.addView(cached10033,new LinearLayout.LayoutParams(-1,dp(52)));
                    }
                    int shown = 0;
                    LinearLayout row = null;
                    if (snap != null) {
                        java.util.List<DocumentSnapshot> activeRooms9989=new ArrayList<>(snap.getDocuments());
                        activeRooms9989.sort((a,b)->{
                            com.google.firebase.Timestamp at=a.getTimestamp("createdAt"),bt=b.getTimestamp("createdAt");
                            return at==null?(bt==null?0:1):bt==null?-1:bt.compareTo(at);
                        });
                        for (DocumentSnapshot doc : activeRooms9989) {
                            if (Boolean.TRUE.equals(doc.getBoolean("closed"))) continue;
                            String name = str(doc,"name","Live Party");
                            String host = str(doc,"ownerName","KING Host");
                            String id = doc.getId();
                            String category = str(doc,"category","Hot");
                            boolean priv = Boolean.TRUE.equals(doc.getBoolean("isPrivate"));
                            boolean password = Boolean.TRUE.equals(doc.getBoolean("hasPassword"));
                            String q = lobbyFilter.toLowerCase(java.util.Locale.US);
                            if(q.isEmpty()&&!categoryMatches(selected,category)) continue;
                            if(!q.isEmpty() && !(name.toLowerCase(java.util.Locale.US).contains(q) || host.toLowerCase(java.util.Locale.US).contains(q) || category.toLowerCase(java.util.Locale.US).contains(q) || shortId(id).contains(q))) continue;
                            if (shown % 2 == 0) {
                                row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.TOP);
                                LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1,dp(206)); rp.setMargins(0,dp(4),0,dp(4)); cloudList.addView(row,rp);
                            }
                            String ownerPhoto = str(doc,"coverUrl",str(doc,"ownerPhoto",""));
                            addLiveRoomGridCard(row,name,host,id,category,priv,password,ownerPhoto,
                                () -> openCloudRoom(id,name,doc.getString("ownerUid"),host,priv,password));
                            shown++;
                        }
                    }
                    boolean more10067=snap!=null&&snap.size()>=lobbyRoomLimit9989;
                    if(shown==0){
                        if(cachedLobby10033)cloudList.addView(tv("No cached matches • connect to refresh",13,0xffffd98c,true));
                        else if(more10067||lobbyCursor10067!=null||!lobbyFilter.isEmpty())
                            cloudList.addView(tv("No matching open rooms on this page. Search checks every category; tap Next to check more rooms.",13,0xffffd98c,true));
                        else showEmptyLobby(cloudList,selected);
                    }
                    LinearLayout paging10067=new LinearLayout(this);paging10067.setGravity(Gravity.CENTER);
                    if(!lobbyPrevious10067.isEmpty()){
                        TextView previous10067=pill("‹ Previous",0xff6244a0,null);
                        previous10067.setOnClickListener(v->{
                            if(partyUiDead()||generation9911!=lobbyRenderGeneration9911||roomId!=null)return;
                            lobbyCursor10067=lobbyPrevious10067.remove(lobbyPrevious10067.size()-1);renderLobby(selected);
                        });paging10067.addView(previous10067,new LinearLayout.LayoutParams(0,dp(48),1));
                    }
                    TextView page10067=tv("Page "+(lobbyPrevious10067.size()+1),12,Color.WHITE,true);
                    page10067.setGravity(Gravity.CENTER);paging10067.addView(page10067,new LinearLayout.LayoutParams(dp(72),dp(48)));
                    if(more10067){
                        final DocumentSnapshot last10067=snap.getDocuments().get(snap.size()-1);
                        TextView next10067=pill(cachedLobby10033?"Next • connect first":"Next ›",0xff6244a0,null);
                        next10067.setEnabled(!cachedLobby10033);
                        next10067.setOnClickListener(v->{
                            if(!next10067.isEnabled()||partyUiDead()||generation9911!=lobbyRenderGeneration9911||roomId!=null)return;
                            next10067.setEnabled(false);lobbyPrevious10067.add(lobbyCursor10067);lobbyCursor10067=last10067;renderLobby(selected);
                        });paging10067.addView(next10067,new LinearLayout.LayoutParams(0,dp(48),1));
                    }
                    cloudList.addView(paging10067,new LinearLayout.LayoutParams(-1,dp(56)));

                });
        } else {
            addSignInPartyCard();
        }

        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        addBottomNav(root,0); setSafeContentView(root);
    }


    private String roomAvatarText(String hostName,String category){
        if("Music".equalsIgnoreCase(category)||"Sing".equalsIgnoreCase(category))return "🎤";
        if("Game".equalsIgnoreCase(category))return "🎮";
        if("Date".equalsIgnoreCase(category))return "💞";
        if("Event".equalsIgnoreCase(category)||"Birthday".equalsIgnoreCase(category)||"Wedding".equalsIgnoreCase(category))return "🎉";
        if(hostName!=null&&!hostName.trim().isEmpty())return hostName.trim().substring(0,1).toUpperCase(java.util.Locale.US);
        return "👑";
    }
    private void openMainTab(int tab){
        if(roomId!=null)leaveRoom();
        KingNav.openRoot(this,tab);
        finish();
    }
    private void showPartyActions(String selected){
        new AlertDialog.Builder(this).setItems(new String[]{"＋ Create Party","🔗 Join by Room ID / Code","↻ Refresh live rooms"},(d,w)->{
            if(w==0){if(user!=null&&db!=null)createRoomDialog();else requireSignInForCreate();}
            else if(w==1)joinRoomByCode891();
            else renderLobby(selected);
        }).show();
    }
    private void showLobbySearch(String selected){
        final EditText e=new EditText(this);e.setHint("Search room, host or ID");e.setSingleLine(true);e.setText(lobbyFilter);
        new AlertDialog.Builder(this).setTitle("Search Party").setView(e).setNegativeButton("Cancel",null)
            .setNeutralButton("Recent",(d,w)->showLobbySearchHistory940(selected))
            .setPositiveButton("Search",(d,w)->{lobbyFilter=e.getText().toString().trim();if(!lobbyFilter.isEmpty())saveLobbySearch940(lobbyFilter);if(KingIdentity964.joinCode(lobbyFilter))joinRoomByCode964(lobbyFilter);else renderLobby(selected);}).show();
    }
    private void saveLobbySearch940(String query){
        if(query==null||query.trim().isEmpty())return;String q=query.trim().replace("|||"," ");
        String raw=prefs.getString("party_search_history_940","");List<String> rows=new ArrayList<>();rows.add(q);
        if(raw!=null&&!raw.isEmpty())for(String x:raw.split("[|][|][|]"))if(x!=null&&!x.trim().isEmpty()&&!q.equalsIgnoreCase(x.trim())&&rows.size()<10)rows.add(x.trim());
        StringBuilder out=new StringBuilder();for(String x:rows){if(out.length()>0)out.append("|||");out.append(x);}prefs.edit().putString("party_search_history_940",out.toString()).apply();
    }
    private void showLobbySearchHistory940(String selected){
        String raw=prefs.getString("party_search_history_940","");if(raw==null||raw.trim().isEmpty()){new AlertDialog.Builder(this).setTitle("Recent Searches").setMessage("No recent Party searches yet.").setPositiveButton("OK",null).show();return;}
        List<String> rows=new ArrayList<>();for(String x:raw.split("[|][|][|]"))if(x!=null&&!x.trim().isEmpty())rows.add(x.trim());
        String[] labels=new String[rows.size()+1];for(int i=0;i<rows.size();i++)labels[i]="⌕  "+rows.get(i);labels[rows.size()]="🗑 Clear search history";
        new AlertDialog.Builder(this).setTitle("Recent Searches").setItems(labels,(d,w)->{
            if(w==rows.size()){prefs.edit().remove("party_search_history_940").apply();toast("Search history cleared");return;}
            lobbyFilter=rows.get(w);renderLobby(selected);
        }).setNegativeButton("Close",null).show();
    }
    private void openRealLogin(){
        try {
            Intent back=new Intent(this,MainActivity.class);
            back.putExtra("forceLogin",true);
            startActivity(back);
        } catch (Exception e) {
            toast("Could not open sign in: " + msg(e));
        }
    }
    private void requireSignInForCreate(){
        new AlertDialog.Builder(this)
            .setTitle("Sign in required")
            .setMessage("Sign in first to create a real KING Plus Party room. Your Party screen will stay open so you can return after login.")
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Sign in",(d,w)->openRealLogin())
            .show();
    }
    private void addSignInPartyCard(){
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(20),dp(22),dp(20),dp(22));card.setBackground(bg(0xffffd5e7,16));
        TextView icon=tv("👑",48,0xffa24472,true);icon.setGravity(Gravity.CENTER);card.addView(icon,new LinearLayout.LayoutParams(-1,dp(70)));
        TextView title=tv("Real Party rooms need Google/Firebase sign-in",17,0xff2c222b,true);title.setGravity(Gravity.CENTER);card.addView(title);
        TextView sub=tv("No fake rooms are shown. Sign in to load real rooms, real hosts and real member counts.",13,0xff6f5c69,false);sub.setGravity(Gravity.CENTER);card.addView(sub);
        TextView go=tv("Sign in",14,Color.WHITE,true);go.setGravity(Gravity.CENTER);go.setBackground(bg(PURPLE,18));go.setOnClickListener(v->openRealLogin());LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(dp(140),dp(44));gp.gravity=Gravity.CENTER_HORIZONTAL;gp.setMargins(0,dp(10),0,0);card.addView(go,gp);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(250));p.setMargins(dp(8),dp(14),dp(8),0);page.addView(card,p);
    }
    private void showEmptyLobby(LinearLayout host,String selected){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setGravity(Gravity.CENTER);box.setPadding(dp(18),dp(18),dp(18),dp(18));box.setBackground(royalNeon9924(0xff43235f,0xff211332,0x99e4ac8d,16));
        TextView icon=tv("🎙",42,0xff8a49ed,true);icon.setGravity(Gravity.CENTER);box.addView(icon);
        TextView title=tv(lobbyFilter.isEmpty()?"No live rooms here yet":"No rooms match in loaded results",16,0xffffd998,true);title.setGravity(Gravity.CENTER);box.addView(title);
        TextView sub=tv(lobbyFilter.isEmpty()?"Create the first real "+selected+" Party room.":"Try another term or tap Search more live rooms to check more results.",13,0xffe2d3ee,false);sub.setGravity(Gravity.CENTER);box.addView(sub);
        TextView create=tv("＋ Create Party",14,Color.WHITE,true);create.setGravity(Gravity.CENTER);create.setBackground(bg(PURPLE,18));create.setOnClickListener(v->createRoomDialog());LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(dp(160),dp(44));cp.gravity=Gravity.CENTER_HORIZONTAL;cp.setMargins(0,dp(8),0,0);box.addView(create,cp);
        TextView join=tv("🔗 Join by Room Code",13,0xffffd994,true);join.setGravity(Gravity.CENTER);join.setOnClickListener(v->joinRoomByCode891());LinearLayout.LayoutParams jp=new LinearLayout.LayoutParams(dp(180),dp(38));jp.gravity=Gravity.CENTER_HORIZONTAL;jp.setMargins(0,dp(4),0,0);box.addView(join,jp);
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(250));bp.setMargins(dp(6),dp(12),dp(6),0);host.addView(box,bp);
    }
    private void showLobbyError(LinearLayout host,String message,String selected){
        host.removeAllViews();LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setGravity(Gravity.CENTER);box.setBackground(royalNeon9924(0xff51203c,0xff231331,0xffd977a4,16));
        TextView e=tv(message,14,0xffffc9e4,true);e.setGravity(Gravity.CENTER);box.addView(e,new LinearLayout.LayoutParams(-1,dp(48)));
        TextView retry=tv("↻ Retry",13,0xffffd893,true);retry.setGravity(Gravity.CENTER);
        retry.setBackground(bg(0xff53326b,16));retry.setOnClickListener(v->renderLobby(selected));LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(dp(120),dp(38));rp.gravity=Gravity.CENTER;box.addView(retry,rp);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(112));p.setMargins(dp(6),dp(12),dp(6),0);host.addView(box,p);
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
        card.setOnClickListener(v->safePartyAction940("party-room-card:"+title,action));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(72));p.setMargins(0,dp(5),0,dp(5));host.addView(card,p);
    }
    private void addLiveRoomGridCard(LinearLayout row,String name,String hostName,String id,String category,boolean priv,boolean password,String photoUrl,Runnable action) {
        int[] palette={0xff6724a8,0xffa43a78,0xff284aaf,0xff6a327a,0xff4a379e,0xff9e4a80};
        int cardColor=palette[KingPagePolicy10067.palette(id==null?name:id,palette.length)];
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(royalNeon9924(0xff48245c,0xff18142e,0xb5f1bf82,16));
        card.setElevation(dp(2));
        FrameLayout cover=new FrameLayout(this);
        cover.setBackground(royalNeon9924(cardColor,0xff21133d,0x88f7c47f,14));
        KingRoomCoverView art940=new KingRoomCoverView(this,category,name);cover.addView(art940,new FrameLayout.LayoutParams(-1,-1));
        ImageView img=new ImageView(this);img.setScaleType(ImageView.ScaleType.CENTER_CROP);cover.addView(img,new FrameLayout.LayoutParams(-1,-1));
        TextView initial=tv("",1,Color.TRANSPARENT,false);initial.setVisibility(View.GONE);cover.addView(initial,new FrameLayout.LayoutParams(1,1));
        if(id!=null&&id.equals(localCoverRoom10026)&&localCoverUri10026!=null){
            KingSafeImageUri10070.show(img,localCoverUri10026);
        }
        if(img.getDrawable()==null&&photoUrl!=null&&!photoUrl.trim().isEmpty())
            loadProfilePhoto(img,initial,photoUrl.trim());
        TextView tag=tv(("Music".equalsIgnoreCase(category)?"Music":"● "+("Hot".equalsIgnoreCase(category)?"Chat":category)),10,Color.WHITE,true);tag.setGravity(Gravity.CENTER);tag.setBackground(royalNeon9924(0xfff34b97,0xff8b2ab9,0x88ffe2b0,8));FrameLayout.LayoutParams tp=new FrameLayout.LayoutParams(dp(58),dp(23),Gravity.BOTTOM|Gravity.LEFT);tp.setMargins(dp(7),0,0,dp(7));cover.addView(tag,tp);
        TextView count=tv("LIVE",10,Color.WHITE,true);count.setGravity(Gravity.CENTER);count.setBackground(bg(0x99000000,10));FrameLayout.LayoutParams ctp=new FrameLayout.LayoutParams(dp(48),dp(23),Gravity.TOP|Gravity.RIGHT);ctp.setMargins(0,dp(7),dp(7),0);cover.addView(count,ctp);
        if(db!=null&&id!=null&&!id.isEmpty()){
            // One-time count per room card; dozens of persistent listeners would consume memory.
            db.collection("live_rooms").document(id).collection("members").limit(100).get()
                .addOnSuccessListener(x->{if(!isFinishing()&&!isDestroyed()&&count.isAttachedToWindow())count.setText(String.valueOf(x.size()));})
                .addOnFailureListener(e->{if(!isFinishing()&&!isDestroyed()&&count.isAttachedToWindow())count.setText("LIVE");});
        }
        card.addView(cover,new LinearLayout.LayoutParams(-1,dp(137)));
        TextView title=tv(name,13,0xffffe4a0,true);title.setMaxLines(1);title.setPadding(dp(5),dp(4),dp(4),0);card.addView(title,new LinearLayout.LayoutParams(-1,dp(30)));
        TextView host=tv((priv?"🔒 ":"")+(password?"🔑 ":"")+hostName,11,0xffd6c5ed,false);host.setMaxLines(1);host.setPadding(dp(5),0,dp(4),dp(3));card.addView(host,new LinearLayout.LayoutParams(-1,dp(28)));
        card.setOnClickListener(v->KingSafe.run(this,"ui-action",action));
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-1,1);cp.setMargins(dp(4),dp(2),dp(4),dp(2));row.addView(card,cp);
    }
    private void addRoomGridRow(LinearLayout host,
                                String title1,String sub1,String room1,String owner1,
                                String title2,String sub2,String room2,String owner2) {
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER);
        addSmallRoomCard(row,title1,sub1,() -> openLocalRoom(room1,owner1));
        addSmallRoomCard(row,title2,sub2,() -> openLocalRoom(room2,owner2));
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1,dp(108)); rp.setMargins(0,dp(3),0,dp(3)); host.addView(row,rp);
    }
    private void addSmallRoomCard(LinearLayout row,String title,String sub,Runnable action) {
        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setGravity(Gravity.BOTTOM); card.setPadding(dp(12),dp(12),dp(12),dp(10));
        card.setBackground(bg(0xffeee8f8,18));
        TextView live = pill("● LIVE",PINK,null); live.setTextSize(10); card.addView(live,new LinearLayout.LayoutParams(dp(62),dp(28)));
        TextView a = tv(title,15,0xff21172f,true); card.addView(a);
        TextView b = tv(sub,12,0xff756b7d,false); card.addView(b);
        card.setOnClickListener(v->safePartyAction940("party-card:"+title,action));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0,-1,1); cp.setMargins(dp(3),dp(3),dp(3),dp(3)); row.addView(card,cp);
    }

    private void createRoomDialog() {
        if(user==null||db==null){requireSignInForCreate();return;}
        pendingCreateSeats=8;pendingCreatePrivate=false;pendingCreateCoverUri=null;
        renderCreateRoomPage();
    }
    private void renderCreateRoomPage(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(14),dp(6),dp(14),dp(10));
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{0xff120a2b,0xff382055,0xff160c2c}));

        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);
        TextView back=tv("‹",32,Color.WHITE,false);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->renderLobby("Hot"));head.addView(back,new LinearLayout.LayoutParams(dp(44),dp(50)));
        TextView title953=tv("👑 Create Royal Party",18,0xffffdf9a,true);title953.setGravity(Gravity.CENTER);head.addView(title953,new LinearLayout.LayoutParams(0,dp(50),1));
        TextView help953=tv("ⓘ",19,0xffd0ebe2,true);help953.setGravity(Gravity.CENTER);help953.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Create Party").setMessage("Choose a cover, privacy, mic seats and room type. Up to 100 listeners can join while the selected seats are used for stage / mic users.").setPositiveButton("OK",null).show());head.addView(help953,new LinearLayout.LayoutParams(dp(44),dp(50)));root.addView(head);

        ScrollView sv=new ScrollView(this);sv.setFillViewport(false);LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(4),0,dp(4),dp(8));sv.addView(body);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));

        FrameLayout cover=new FrameLayout(this);cover.setBackground(bg(0x33000000,18));
        KingRoomCoverView createCover940=new KingRoomCoverView(this,pendingCreateCategory,safeName()+"'s Party");cover.addView(createCover940,new FrameLayout.LayoutParams(-1,-1));
        ImageView preview=new ImageView(this);preview.setScaleType(ImageView.ScaleType.CENTER_CROP);cover.addView(preview,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout overlay953=new LinearLayout(this);overlay953.setOrientation(LinearLayout.VERTICAL);overlay953.setGravity(Gravity.BOTTOM|Gravity.LEFT);overlay953.setPadding(dp(12),dp(10),dp(12),dp(10));overlay953.setBackground(bg(0x52000000,18));
        TextView coverTitle953=tv("▣  Change cover",13,Color.WHITE,true);TextView coverType953=tv(pendingCreateCategory+"  •  "+(pendingCreatePrivate?"Private":"Public"),11,0xffe6eee9,true);overlay953.addView(coverTitle953,new LinearLayout.LayoutParams(-1,dp(28)));overlay953.addView(coverType953,new LinearLayout.LayoutParams(-1,dp(23)));cover.addView(overlay953,new FrameLayout.LayoutParams(-1,-1));
        cover.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("image/*");i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);startActivityForResult(i,ROOM_COVER_PICK_REQUEST);});
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(178));cp.setMargins(0,dp(6),0,dp(7));body.addView(cover,cp);
        if(pendingCreateCoverUri!=null&&KingSafeImageUri10070.show(preview,pendingCreateCoverUri)){coverTitle953.setText("✎  Change cover");}
        TextView tip=tv("A clear cover helps your room stand out",11,0xffffe86c,true);tip.setGravity(Gravity.CENTER);body.addView(tip,new LinearLayout.LayoutParams(-1,dp(30)));

        LinearLayout flags=new LinearLayout(this);flags.setGravity(Gravity.CENTER);
        TextView pub=tv(pendingCreatePrivate?"🔒  Private":"🌐  Public",13,Color.WHITE,true);pub.setGravity(Gravity.CENTER);pub.setBackground(bg(0x2b4fd0aa,14));
        LinearLayout.LayoutParams f953=new LinearLayout.LayoutParams(0,dp(48),1);f953.setMargins(dp(2),0,dp(4),0);flags.addView(pub,f953);
        TextView seat=tv("🎙  "+pendingCreateSeats+" Seats",13,Color.WHITE,true);seat.setGravity(Gravity.CENTER);seat.setBackground(bg(0x2b4fd0aa,14));LinearLayout.LayoutParams s953=new LinearLayout.LayoutParams(0,dp(48),1);s953.setMargins(dp(4),0,dp(2),0);flags.addView(seat,s953);body.addView(flags,new LinearLayout.LayoutParams(-1,dp(54)));

        final LinearLayout seatPreview953=new LinearLayout(this);seatPreview953.setOrientation(LinearLayout.VERTICAL);
        TextView seatsTitle953=tv("Stage / Mic Preview",12,0xffbfe5d8,true);seatsTitle953.setPadding(dp(2),dp(4),0,0);body.addView(seatsTitle953,new LinearLayout.LayoutParams(-1,dp(30)));
        renderCreateSeatPreview953(seatPreview953,pendingCreateSeats);body.addView(seatPreview953,new LinearLayout.LayoutParams(-1,-2));

        pub.setOnClickListener(v->{pendingCreatePrivate=!pendingCreatePrivate;pub.setText(pendingCreatePrivate?"🔒  Private":"🌐  Public");coverType953.setText(pendingCreateCategory+"  •  "+(pendingCreatePrivate?"Private":"Public"));});
        seat.setOnClickListener(v->{String[] values={"8 seats","10 seats","12 seats"};new AlertDialog.Builder(this).setTitle("Stage / mic seats").setItems(values,(d,w)->{pendingCreateSeats=w==0?8:w==1?10:12;seat.setText("🎙  "+pendingCreateSeats+" Seats");renderCreateSeatPreview953(seatPreview953,pendingCreateSeats);}).setNegativeButton("Cancel",null).show();});

        LinearLayout nameBox953=new LinearLayout(this);nameBox953.setOrientation(LinearLayout.VERTICAL);nameBox953.setPadding(dp(12),dp(5),dp(12),dp(4));nameBox953.setBackground(bg(0x22000000,12));
        TextView nameLabel953=tv("Room name",11,0xff93b8ac,true);nameBox953.addView(nameLabel953,new LinearLayout.LayoutParams(-1,dp(24)));
        final EditText name=new EditText(this);name.setHint("Room name");name.setHintTextColor(0xff9cb8ae);name.setTextColor(Color.WHITE);name.setTextSize(17);name.setSingleLine(true);name.setBackgroundColor(Color.TRANSPARENT);name.setPadding(0,0,0,0);
        String defaultName953=pendingCreateName953==null?"":pendingCreateName953.trim();if(defaultName953.isEmpty()){defaultName953=safeName().trim();if(defaultName953.isEmpty())defaultName953="KING";defaultName953=defaultName953+"'s Party";}name.setText(defaultName953);name.setSelection(name.getText().length());
        name.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){pendingCreateName953=s==null?"":s.toString();}public void afterTextChanged(android.text.Editable e){}});
        nameBox953.addView(name,new LinearLayout.LayoutParams(-1,dp(42)));LinearLayout.LayoutParams nlp953=new LinearLayout.LayoutParams(-1,dp(72));nlp953.setMargins(0,dp(8),0,dp(6));body.addView(nameBox953,nlp953);

        TextView type=tv("Room type:  "+pendingCreateCategory+"   ›",13,0xffe4f5ef,true);type.setGravity(Gravity.CENTER_VERTICAL);type.setPadding(dp(12),0,dp(10),0);type.setBackground(bg(0x22000000,12));
        type.setOnClickListener(v->{String[] cats={"Hot","Chat","Event","Date","Music","Game","KTV","Radio","PK","Pick Me","Family","Multi Video"};new AlertDialog.Builder(this).setTitle("Choose room type").setItems(cats,(d,w)->{pendingCreateCategory=cats[w];type.setText("Room type:  "+pendingCreateCategory+"   ›");coverType953.setText(pendingCreateCategory+"  •  "+(pendingCreatePrivate?"Private":"Public"));}).show();});body.addView(type,new LinearLayout.LayoutParams(-1,dp(52)));
        TextView audience953=tv("👥 Up to 100 listeners  •  "+pendingCreateSeats+" stage seats",11,0xffbfe5d8,true);audience953.setGravity(Gravity.CENTER);body.addView(audience953,new LinearLayout.LayoutParams(-1,dp(34)));

        TextView start=tv(roomCreateInFlight921?"Creating Party…":"🎉  Start Room",16,0xff171717,true);start.setGravity(Gravity.CENTER);start.setBackground(bg(roomCreateInFlight921?0xffb9ad48:0xffffee00,14));start.setEnabled(!roomCreateInFlight921);
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(56));sp.setMargins(dp(4),dp(6),dp(4),dp(2));root.addView(start,sp);
        start.setOnClickListener(v->{if(roomCreateInFlight921)return;String n=name.getText().toString().trim();if(n.length()<2){String base=safeName().trim();if(base.isEmpty())base="KING";n=base+"'s Party";name.setText(n);}pendingCreateName953=n;createCloudRoom(n,pendingCreateCategory,pendingCreateSeats,pendingCreatePrivate);});
        setSafeContentView(root);
    }
    private void renderCreateSeatPreview953(LinearLayout host,int count){host.removeAllViews();int rows=(count+3)/4,no=1;for(int r=0;r<rows;r++){LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);for(int c=0;c<4;c++){if(no<=count){TextView seat=tv(no==1?"👑\\nHOST":"＋\\n"+no,10,0xffd8eee7,true);seat.setGravity(Gravity.CENTER);seat.setBackground(bg(no==1?0x445fd7b2:0x2b4fd0aa,40));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(62),1);lp.setMargins(dp(5),dp(3),dp(5),dp(3));row.addView(seat,lp);no++;}else row.addView(new View(this),new LinearLayout.LayoutParams(0,dp(62),1));}host.addView(row,new LinearLayout.LayoutParams(-1,dp(68)));}}
    
    // Set ONLY in successful new-room Firestore .set() callback, consumed once.
    // A normal room re-entry cannot use this newly-created-room shortcut.
    private String newlyCreatedServerRoom991;
    private void createCloudRoom(String name,String category,int seats,boolean priv){
        if(user==null||db==null){requireSignInForCreate();return;}
        if(roomCreateInFlight921)return;
        roomCreateInFlight921=true;toast("Creating Party…");renderCreateRoomPage();
        DocumentReference ref=db.collection("live_rooms").document();
        String joinCode=shortId(ref.getId());
        Map<String,Object> full=new HashMap<>();full.put("name",name);full.put("ownerUid",user.getUid());full.put("ownerName",safeName());String ownerPhoto931=cloudProfilePhoto868();if(ownerPhoto931.isEmpty()&&user.getPhotoUrl()!=null)ownerPhoto931=user.getPhotoUrl().toString();if(!ownerPhoto931.isEmpty())full.put("ownerPhoto",ownerPhoto931);full.put("category",category);full.put("theme","Classic");full.put("maxSeats",seats);full.put("maxMembers",100);full.put("audienceEnabled",true);full.put("isPrivate",priv);full.put("hasPassword",false);full.put("joinCode",joinCode);full.put("announcement",announcement);full.put("locked",false);full.put("muteAll",false);full.put("closed",false);full.put("createdAt",FieldValue.serverTimestamp());full.put("updatedAt",FieldValue.serverTimestamp());
        ref.set(full).addOnSuccessListener(v->finishRoomCreate921(ref,name,seats,priv))
            .addOnFailureListener(first->retryRoomCreateCompact921(ref,name,category,seats,priv,joinCode,first));
    }
    private void retryRoomCreateCompact921(DocumentReference ref,String name,String category,int seats,boolean priv,String joinCode,Exception first){
        Map<String,Object> compact=new HashMap<>();compact.put("name",name);compact.put("ownerUid",user.getUid());compact.put("ownerName",safeName());compact.put("category",category);compact.put("maxSeats",seats);compact.put("maxMembers",100);compact.put("audienceEnabled",true);compact.put("isPrivate",priv);compact.put("hasPassword",false);compact.put("joinCode",joinCode);compact.put("locked",false);compact.put("muteAll",false);compact.put("closed",false);compact.put("createdAt",FieldValue.serverTimestamp());compact.put("updatedAt",FieldValue.serverTimestamp());
        ref.set(compact).addOnSuccessListener(v->finishRoomCreate921(ref,name,seats,priv)).addOnFailureListener(second->retryRoomCreateLegacy921(ref,name,category,seats,priv,joinCode,first,second));
    }
    private void retryRoomCreateLegacy921(DocumentReference ref,String name,String category,int seats,boolean priv,String joinCode,Exception first,Exception second){
        Map<String,Object> legacy=new HashMap<>();legacy.put("name",name);legacy.put("ownerUid",user.getUid());legacy.put("ownerName",safeName());legacy.put("category",category);legacy.put("maxSeats",seats);legacy.put("isPrivate",priv);legacy.put("hasPassword",false);legacy.put("joinCode",joinCode);legacy.put("locked",false);legacy.put("muteAll",false);legacy.put("closed",false);legacy.put("createdAt",FieldValue.serverTimestamp());legacy.put("updatedAt",FieldValue.serverTimestamp());
        ref.set(legacy).addOnSuccessListener(v->finishRoomCreate921(ref,name,seats,priv)).addOnFailureListener(last->{roomCreateInFlight921=false;KingStability.nonFatal(this,"room-create-full",first);KingStability.nonFatal(this,"room-create-compact",second);KingStability.nonFatal(this,"room-create-legacy",last);roomJoinError891("Room create failed",last);renderCreateRoomPage();});
    }
    private void finishRoomCreate921(DocumentReference ref,String name,int seats,boolean priv){
        roomCreateInFlight921=false;maxSeats=seats;
        if(pendingCreateCoverUri!=null){
            localCoverRoom10026=ref.getId();localCoverUri10026=pendingCreateCoverUri;
            if(storage!=null)uploadRoomCoverV600(ref,pendingCreateCoverUri);
            else toast("Room created, but photo upload needs Firebase Storage");
        }
        newlyCreatedServerRoom991=ref.getId();openCloudRoom(ref.getId(),name,user.getUid(),safeName(),priv,false);
    }
    private void uploadRoomCoverV600(DocumentReference room,Uri uri){
        if(room==null||uri==null||storage==null||user==null)return;String path="chat_media/"+user.getUid()+"/room_covers/"+room.getId()+"_"+System.currentTimeMillis()+".jpg";StorageReference ref=storage.getReference().child(path);ref.putFile(uri).continueWithTask(t->{if(!t.isSuccessful()){Exception e=t.getException();if(e!=null)throw e;}return ref.getDownloadUrl();}).addOnSuccessListener(u->room.update("coverUrl",u.toString(),"updatedAt",FieldValue.serverTimestamp())).addOnFailureListener(e->toast("Room cover upload failed: "+msg(e)));
    }
    private void chooseCreateCategory(String name){renderCreateRoomPage();}
    private void createCloudRoom(String name,String category) {
        createCloudRoom(name,category,8,false);
    }
    private void openCloudRoom(String id,String name,String hostUid,String hostName) {
        openCloudRoom(id,name,hostUid,hostName,false,false);
    }
    private void openCloudRoom(String id,String name,String hostUid,String hostName,boolean priv,boolean password) {
        ++inviteGeneration9989;
        if(!KingDocumentId10036.valid(id)){toast("Invalid Party Room ID");return;}
        if(cloudRoom&&roomId!=null&&!roomId.equals(id.trim()))unregisterMember();
        clearVoiceRejoin9957();
        revokeMicConsent9956();
        ++presenceGeneration967;
        partyAutoRepairUsed979=false;
        ++partyReconnectGeneration979;
        clearPartyReconnect979();
        presenceSession967=java.util.UUID.randomUUID().toString();
        roomVisitStarted10026=System.currentTimeMillis();
        roomId=id.trim(); micTogglePending968=false; roomName=name; ownerUid=hostUid; ownerName=hostName; cloudRoom=true; mySeat=-1; micOn=false; coHost=false; roomPrivate=priv; roomHasPassword=password;roomEntranceMode9931="legacy";roomEntryEpoch9936=""; memberSeen=false; localRemovedSeats.clear(); lockedSeats.clear();
        processedEventIds.clear(); eventSnapshotReady=false;
        if(roomPrivate&&!isOwner()) tryPrivateEntry();
        else {
            // Build the expensive Party UI only AFTER Firebase confirms membership.
            // Previously renderParty ran here and a second time in finishMemberJoin921().
            showJoiningRoom964();
            joinMemberThenOpen891();
        }
    }
    private String deviceId891(){
        try{
            String id=android.provider.Settings.Secure.getString(getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);
            return id==null?"unknown-device":id;
        }catch(Exception e){return "unknown-device";}
    }
    private Map<String,Object> memberPayload891(){
        LevelSystem.Snapshot p720=LevelSystem.read(this);String frame730=KingCosmetics.frame(this),effect730=KingCosmetics.effect(this);
        Map<String,Object>d=new HashMap<>();d.put("uid",user.getUid());d.put("name",safeName());d.put("level",p720.level);
        // VIP is server-managed; rejoining a Party must not force it to zero.
        boolean showEntrance10098=getSharedPreferences("MainActivity",MODE_PRIVATE)
            .getBoolean("me_show_entrance_effects",true);
        d.put("equippedFrame",frame730);d.put("entranceEffect",showEntrance10098?effect730:"None");d.put("nameBadge",KingCosmetics.badge(this));d.put("deviceId",deviceId891());d.put("sessionId",presenceSession967);d.put("online",true);try{d.put("appVersion",getPackageManager().getPackageInfo(getPackageName(),0).versionName);}
        catch(Exception ignored){d.put("appVersion","9.6.7");}
        d.put("voiceJoined",voiceConferenceConnected10065);
        String cloudPhoto868=cloudProfilePhoto868();if(!cloudPhoto868.isEmpty())d.put("photoUrl",cloudPhoto868);else if(user.getPhotoUrl()!=null)d.put("photoUrl",user.getPhotoUrl().toString());
        d.put("lastSeenAt",FieldValue.serverTimestamp());return d;
    }
    // Each join has its own Firestore room reference and generation. A listener
    // from a previous room must NEVER act on mutable roomId or user.
    private int presenceGeneration967;
    private String presenceSession967="";
    private boolean isActivePresence967(String expectedRoom,String expectedUid,int expectedGeneration){
        return db!=null && KingRoomPresence967.isActive(
            expectedRoom,expectedUid,expectedGeneration,roomId,
            user==null?null:user.getUid(),presenceGeneration967,
            cloudRoom,!isFinishing()&&!isDestroyed());
    }
    private int roomJoinGeneration966;
    private boolean isCurrentPartyJoin966(String requestedRoom966,String requestedUid966,int token966){
        return db!=null && KingPartyJoin966.current(
            requestedRoom966,requestedUid966,token966,
            roomId,user==null?null:user.getUid(),roomJoinGeneration966,
            cloudRoom,!isFinishing()&&!isDestroyed());
    }
    // A Firebase .get()/set() task can wait indefinitely on a slow/offline device.
    // Never leave a fullscreen spinner without Retry or an exit route.
    private final android.os.Handler partyJoinHandler977=
        new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable partyJoinTimeoutTask977;

    private void stopPartyJoinTimeout977(){
        if(partyJoinTimeoutTask977!=null){
            partyJoinHandler977.removeCallbacks(partyJoinTimeoutTask977);
            partyJoinTimeoutTask977=null;
        }
    }

    private void startPartyJoinTimeout977(String expectedRoom977,String expectedUid977,int token977){
        stopPartyJoinTimeout977();
        final long begin977=android.os.SystemClock.elapsedRealtime();
        partyJoinTimeoutTask977=()->{
            final String currentUid977=user==null?null:user.getUid();
            if(!KingPartyJoinTimeout977.waiting(expectedRoom977,roomId,
                    expectedUid977,currentUid977,token977,roomJoinGeneration966,
                    cloudRoom&&!isFinishing()&&!isDestroyed(),memberSeen))return;
            if(!KingPartyJoinTimeout977.expired(begin977,
                    android.os.SystemClock.elapsedRealtime()))return;
            KingCrashWatch958.mark(this,"party-member-join-timeout-977");
            showPartyJoinFailure977(
                "Firebase did not confirm room membership within 18 seconds.\n\n"+
                "Check internet, Google/Phone sign-in, and Firebase access rules. "+
                "You may retry or return to the Party list.");
        };
        partyJoinHandler977.postDelayed(partyJoinTimeoutTask977,
            KingPartyJoinTimeout977.JOIN_TIMEOUT_MS);
    }

    private void showPartyJoinFailure977(String reason977){
        if(isFinishing()||isDestroyed())return;
        stopPartyJoinTimeout977();
        ++roomJoinGeneration966; // Old Firebase read/write callbacks cannot reopen an abandoned room.
        ++partyReconnectGeneration979;
        clearPartyReconnect979();
        KingCrashWatch958.mark(this,"party-member-join-error-977");
        LinearLayout root977=new LinearLayout(this);
        root977.setOrientation(LinearLayout.VERTICAL);
        root977.setGravity(Gravity.CENTER);
        root977.setPadding(dp(22),dp(22),dp(22),dp(22));
        root977.setBackgroundColor(0xff172b36);
        TextView title977=tv("⚠  Party Room connection failed",20,Color.WHITE,true);
        title977.setGravity(Gravity.CENTER);
        root977.addView(title977,new LinearLayout.LayoutParams(-1,dp(90)));
        String actualUid979=user==null?"not signed in":user.getUid();
        String shownUid979=actualUid979.length()>8?actualUid979.substring(0,8)+"…":actualUid979;
        String hint979=KingPartyConnection979.status(KingNetwork.online(this),
            user!=null,true);
        String reasonUi979=(reason977==null?"Room verification unavailable":reason977);
        String version982="unknown";
        try{version982=getPackageManager().getPackageInfo(getPackageName(),0).versionName;}
        catch(Exception ignored){}
        final String support982=KingRoomCallback982.supportReport(
            version982,roomId==null?"none":shortId(roomId),shownUid979,
            reasonUi979,KingNetwork.online(this),user!=null);
        TextView detail977=tv(reasonUi979+"\n\n"+hint979+
            "\nRoom: "+(roomId==null?"none":shortId(roomId))+
            " • UID: "+shownUid979+
            "\nBoth phones: same KING Plus APK, two DIFFERENT Firebase accounts.",
            12,0xffd5e0e7,false);
        detail977.setGravity(Gravity.CENTER);
        root977.addView(detail977,new LinearLayout.LayoutParams(-1,dp(250)));
        TextView retry977=tv("↻  Retry secure connection",16,Color.WHITE,true);
        retry977.setGravity(Gravity.CENTER);
        retry977.setBackgroundColor(0xff12856d);
        LinearLayout.LayoutParams retryParams977=
            new LinearLayout.LayoutParams(-1,dp(52));
        retryParams977.setMargins(0,dp(14),0,0);
        root977.addView(retry977,retryParams977);
        TextView copy982=tv("📋  Copy connection report",15,Color.WHITE,true);
        copy982.setGravity(Gravity.CENTER);
        copy982.setBackgroundColor(0xff45587a);
        LinearLayout.LayoutParams copyParams982=
            new LinearLayout.LayoutParams(-1,dp(52));
        copyParams982.setMargins(0,dp(10),0,0);
        root977.addView(copy982,copyParams982);
        copy982.setOnClickListener(v->{
            try{
                android.content.ClipboardManager cb982=
                    (android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                if(cb982==null){toast("Clipboard unavailable");return;}
                cb982.setPrimaryClip(android.content.ClipData.newPlainText(
                    "KING Plus Party connection report",support982));
                toast("Copied KING Plus connection report. Share it for troubleshooting.");
            }catch(Exception error){toast("Unable to copy Party report");}
        });
        TextView testConnection983=tv("🔌  Test Firebase / Party connection",15,Color.WHITE,true);
        testConnection983.setGravity(Gravity.CENTER);
        testConnection983.setBackgroundColor(0xff3c516b);
        LinearLayout.LayoutParams testParams983=new LinearLayout.LayoutParams(-1,dp(52));
        testParams983.setMargins(0,dp(10),0,0);
        root977.addView(testConnection983,testParams983);
        testConnection983.setOnClickListener(v->{
            if(isFinishing()||isDestroyed())return;
            Intent intent983=new Intent(this,KingConnection983Activity.class);
            if(roomId!=null&&!roomId.isEmpty())
                intent983.putExtra("roomInput983",roomId);
            startActivity(intent983);
        });
        retry977.setOnClickListener(v->{
            if(isFinishing()||isDestroyed())return;
            if(!KingNetwork.online(this)){
                toast("Internet is disconnected. Reconnect and retry.");return;
            }
            try{user=FirebaseAuth.getInstance().getCurrentUser();}
            catch(Exception ignored){}
            if(user==null||db==null||roomId==null||roomId.trim().isEmpty()){
                toast("Sign in and choose the Party again");
                renderLobby("Hot");return;
            }
            reconnectPartyFirestore979(true);
        });
        TextView back977=tv("←  Back to Party list",16,Color.WHITE,true);
        back977.setGravity(Gravity.CENTER);
        back977.setBackgroundColor(0xff324658);
        LinearLayout.LayoutParams backParams977=
            new LinearLayout.LayoutParams(-1,dp(52));
        backParams977.setMargins(0,dp(14),0,0);
        root977.addView(back977,backParams977);
        back977.setOnClickListener(v->renderLobby("Hot"));
        setSafeContentView(root977);
    }

    private int partyReconnectGeneration979;
    private boolean partyReconnecting979;
    private boolean partyAutoRepairUsed979;
    private Runnable partyReconnectTimeout979;

    private void clearPartyReconnect979(){
        if(partyReconnectTimeout979!=null){
            partyJoinHandler977.removeCallbacks(partyReconnectTimeout979);
            partyReconnectTimeout979=null;
        }
        partyReconnecting979=false;
    }

    private boolean currentReconnect979(String expectedRoom979,String expectedUid979,int generation979){
        com.google.firebase.auth.FirebaseUser actual979=null;
        try{actual979=com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();}
        catch(Exception ignored){}
        return KingPartyConnection979.canRejoin(expectedRoom979,roomId,
            expectedUid979,actual979==null?null:actual979.getUid(),
            generation979,partyReconnectGeneration979,
            !isFinishing()&&!isDestroyed(),cloudRoom&&!memberSeen);
    }

    private String firebaseProject986(){
        try{
            String id986=com.google.firebase.FirebaseApp.getInstance()
                .getOptions().getProjectId();
            return id986==null?"unknown":id986;
        }catch(Exception ignored){return "unknown";}
    }

    private void reconnectPartyFirestore979(boolean refreshToken979){
        if(isFinishing()||isDestroyed()||partyReconnecting979)return;
        if(db==null||!cloudRoom||roomId==null||roomId.trim().isEmpty()){
            showPartyJoinFailure977("Choose an active Party Room and sign in first.");return;
        }
        if(!KingNetwork.online(this)){
            showPartyJoinFailure977("Android reports no validated Internet connection. "+
                "Try mobile data instead of Wi-Fi, disable VPN/Private DNS, then tap Retry.");
            return;
        }
        try{user=com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();}
        catch(Exception ignored){user=null;}
        if(user==null){
            showPartyJoinFailure977("Firebase sign-in session is missing. "+
                "Go back and sign in using your Google/verified mobile account.");return;
        }
        final String expectedRoom979=roomId;
        final String expectedUid979=user.getUid();
        final int generation979=++partyReconnectGeneration979;
        partyReconnecting979=true;
        ++roomJoinGeneration966;  // Invalidate pending writes from previous offline attempt.
        stopPartyJoinTimeout977();
        showJoiningRoom964();
        KingCrashWatch958.mark(this,"party-firestore-network-recovery-979");
        partyReconnectTimeout979=()->{
            if(!currentReconnect979(expectedRoom979,expectedUid979,generation979))return;
            showPartyJoinFailure977("Firestore reconnect took more than 18 seconds. "+
                "Try switching Wi-Fi / mobile data or disabling VPN. "+
                "The Party Room was NOT joined without verification.");
        };
        partyJoinHandler977.postDelayed(partyReconnectTimeout979,18000L);
        db.enableNetwork()
            .addOnSuccessListener(v->{
                if(!currentReconnect979(expectedRoom979,expectedUid979,generation979))return;
                user.getIdToken(refreshToken979)
                    .addOnSuccessListener(token979->{
                        if(!currentReconnect979(expectedRoom979,expectedUid979,generation979))return;
                        db.collection("live_rooms").document(expectedRoom979)
                            .get(com.google.firebase.firestore.Source.SERVER)
                            .addOnSuccessListener(room979->{
                                if(!currentReconnect979(expectedRoom979,expectedUid979,generation979))return;
                                clearPartyReconnect979();
                                if(room979==null||!room979.exists()||
                                   Boolean.TRUE.equals(room979.getBoolean("closed"))){
                                    showPartyJoinFailure977("The exact Party Room was removed or closed. "+
                                        "Ask the host to share a new full Room invitation.");
                                    return;
                                }
                                if(room979.getString("ownerUid")==null){
                                    showPartyJoinFailure977("Room record has no valid Host UID. "+
                                        "Ask the Host to recreate the Room.");return;
                                }
                                // Never change the Room ID, signed-in account, room access
                                // or private/password checks while recovering transport.
                                joinMemberThenOpen891();
                            })
                            .addOnFailureListener(err979->{
                                if(currentReconnect979(expectedRoom979,expectedUid979,generation979))
                                    showPartyJoinFailure977(KingFirestoreRecovery986.guidance(
                                        err979 instanceof com.google.firebase.firestore.FirebaseFirestoreException
                                            ?((com.google.firebase.firestore.FirebaseFirestoreException)err979)
                                                .getCode().name():"UNKNOWN",
                                        err979==null?null:err979.getMessage(),
                                        firebaseProject986()));
                            });
                    })
                    .addOnFailureListener(err979->{
                        if(currentReconnect979(expectedRoom979,expectedUid979,generation979))
                            showPartyJoinFailure977("Your Firebase login token could not refresh: "+
                                msg(err979)+". Sign in again and retry.");
                    });
            })
            .addOnFailureListener(err979->{
                if(currentReconnect979(expectedRoom979,expectedUid979,generation979))
                    showPartyJoinFailure977("Could not enable Firebase network: "+msg(err979)+
                        ". Verify internet/VPN and try again.");
            });
    }

    private boolean firestoreTransportError979(Exception error979){
        String code979="";
        if(error979 instanceof com.google.firebase.firestore.FirebaseFirestoreException)
            code979=((com.google.firebase.firestore.FirebaseFirestoreException)error979)
                .getCode().name();
        return KingPartyConnection979.isRetryable(code979,error979==null?null:error979.getMessage());
    }

    private void joinMemberThenOpen891(){
        if(user==null||db==null||roomId==null||roomId.trim().isEmpty()||!cloudRoom){
            showPartyJoinFailure977("Sign in and choose an active Party Room to join.");
            return;
        }
        final String requestedRoom966=roomId;
        final String requestedUid966=user.getUid();
        final int requestToken966=++roomJoinGeneration966;
        // A success callback from creation is a SERVER write acknowledgement.
        // Reuse it only once, for the matching signed-in host and same room.
        final boolean newlyCreatedHost991=KingFreshHostRoom991.canWriteWithoutPreRead(
            newlyCreatedServerRoom991,requestedRoom966,
            ownerUid,requestedUid966,isOwner());
        newlyCreatedServerRoom991=null;
        startPartyJoinTimeout977(requestedRoom966,requestedUid966,requestToken966);
        final DocumentReference requestedRoomRef966=db.collection("live_rooms").document(requestedRoom966);
        final DocumentReference ref=requestedRoomRef966.collection("members").document(requestedUid966);
        final Runnable write=()->{
            if(!isCurrentPartyJoin966(requestedRoom966,requestedUid966,requestToken966))return;
            Map<String,Object> member=memberPayload891();
            member.put("joinedAt",FieldValue.serverTimestamp());
            ref.set(member,SetOptions.merge())
                .addOnSuccessListener(v->{
                    if(isCurrentPartyJoin966(requestedRoom966,requestedUid966,requestToken966))
                        finishMemberJoin921();
                })
                .addOnFailureListener(error->{
                    if(isCurrentPartyJoin966(requestedRoom966,requestedUid966,requestToken966))
                        retryMinimalMember921(ref,error,requestedRoom966,requestedUid966,requestToken966);
                });
        };
        if(newlyCreatedHost991){
            // Skip only the pre-read after server-confirmed CREATE. Firestore still
            // validates member write, room access, bans and signed-in identity.
            write.run();
            return;
        }
        ref.get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(oldDoc->{
                if(!isCurrentPartyJoin966(requestedRoom966,requestedUid966,requestToken966))return;
                String oldDevice=oldDoc!=null&&oldDoc.exists()?oldDoc.getString("deviceId"):null;
                String now=deviceId891();
                if(oldDevice!=null&&!oldDevice.isEmpty()&&!oldDevice.equals(now)){
                    toast("Both phones use the same KING account: one member ID. Use separate Google accounts to appear as different people.");
                    verifyExistingRoomOpen10037(requestedRoomRef966,requestedRoom966,requestedUid966,requestToken966,write);
                }else if(oldDoc!=null&&oldDoc.exists())
                    verifyExistingRoomOpen10037(requestedRoomRef966,requestedRoom966,requestedUid966,requestToken966,write);
                else checkCrowdCapacity930(requestedRoomRef966,requestedRoom966,requestedUid966,requestToken966,write);
            })
            .addOnFailureListener(error->{
                // The user may have returned to the lobby or switched rooms
                // while Firebase was resolving this old query.
                if(!isCurrentPartyJoin966(requestedRoom966,requestedUid966,requestToken966))return;
                KingStability.nonFatal(this,"party-member-lookup-966",error);
                if(firestoreTransportError979(error)&&!partyAutoRepairUsed979&&
                   KingNetwork.online(this)){
                    partyAutoRepairUsed979=true;
                    reconnectPartyFirestore979(false);
                    return;
                }
                roomJoinError891("Cannot verify signed-in Party membership",error);
            });
    }
    private void verifyExistingRoomOpen10037(DocumentReference expectedRoom10037,String id10037,
                                            String uid10037,int token10037,Runnable proceed10037){
        if(!isCurrentPartyJoin966(id10037,uid10037,token10037))return;
        expectedRoom10037.get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(room10037->{
                if(!isCurrentPartyJoin966(id10037,uid10037,token10037))return;
                if(room10037==null||!room10037.exists()
                    ||Boolean.TRUE.equals(room10037.getBoolean("closed"))){
                    showPartyJoinFailure977("This Party Room is closed or no longer exists.");return;
                }
                if(!KingSixPages10038.roomOwnerMatches(ownerUid,room10037.getString("ownerUid"))){
                    showPartyJoinFailure977("Party host changed or account is invalid. Refresh the room list.");return;
                }
                proceed10037.run();
            })
            .addOnFailureListener(error10037->{
                if(isCurrentPartyJoin966(id10037,uid10037,token10037))
                    roomJoinError891("Could not verify Party Room status",error10037);
            });
    }
    private void checkCrowdCapacity930(DocumentReference expectedRoom966,
                                        String expectedId966,String expectedUid966,int token966,
                                        Runnable join){
        if(!isCurrentPartyJoin966(expectedId966,expectedUid966,token966))return;
        // This reference was created from a nonempty room ID BEFORE scheduling
        // any callback. Never read mutable roomId from this async method.
        if(expectedRoom966==null){
            showPartyJoinFailure977("Invalid Party Room reference");return;
        }
        if(isOwner()){verifyExistingRoomOpen10037(expectedRoom966,expectedId966,expectedUid966,token966,join);return;}
        // Fetch room state and member capacity together, not in two serial server
        // round-trips. Keep the existing signed-in room+generation guards.
        com.google.android.gms.tasks.Tasks.whenAllSuccess(
                expectedRoom966.get(com.google.firebase.firestore.Source.SERVER),
                expectedRoom966.collection("members").get(com.google.firebase.firestore.Source.SERVER))
            .addOnSuccessListener(responses10027->{
                if(!isCurrentPartyJoin966(expectedId966,expectedUid966,token966))return;
                if(responses10027.size()!=2){
                    showPartyJoinFailure977("Cannot verify Party Room capacity");return;
                }
                DocumentSnapshot roomDoc10027=(DocumentSnapshot)responses10027.get(0);
                com.google.firebase.firestore.QuerySnapshot members10027=
                    (com.google.firebase.firestore.QuerySnapshot)responses10027.get(1);
                if(roomDoc10027==null||!roomDoc10027.exists()
                    ||Boolean.TRUE.equals(roomDoc10027.getBoolean("closed"))){
                    toast("This Party room is no longer available");renderLobby("Hot");return;
                }
                Long cap10027=roomDoc10027.getLong("maxMembers");
                int limit10027=cap10027==null?100:(int)Math.max(10,Math.min(200,cap10027));
                maxMembers930=limit10027;
                if(members10027==null){showPartyJoinFailure977("Cannot verify Party Room members");return;}
                if(members10027.size()>=limit10027){
                    toast("This Party Room is full ("+limit10027+" users)");
                    renderLobby("Hot");return;
                }
                join.run();
            })
            .addOnFailureListener(error10027->{
                if(isCurrentPartyJoin966(expectedId966,expectedUid966,token966))
                    roomJoinError891("Could not verify Party room and member capacity",error10027);
            });
    }
    private Map<String,Object> minimalMember921(){
        Map<String,Object>m=new HashMap<>();m.put("uid",user.getUid());m.put("name",safeName());m.put("deviceId",deviceId891());m.put("sessionId",presenceSession967);m.put("joinedAt",FieldValue.serverTimestamp());m.put("lastSeenAt",FieldValue.serverTimestamp());return m;
    }
    private void retryMinimalMember921(DocumentReference ref,Exception fullError,
                                       String expectedRoom966,String expectedUid966,int token966){
        if(!isCurrentPartyJoin966(expectedRoom966,expectedUid966,token966))return;
        ref.set(minimalMember921(),SetOptions.merge())
            .addOnSuccessListener(v->{
                if(!isCurrentPartyJoin966(expectedRoom966,expectedUid966,token966))return;
                KingStability.nonFatal(this,"member-full-fallback",fullError);
                finishMemberJoin921();
            })
            .addOnFailureListener(last->{
                if(!isCurrentPartyJoin966(expectedRoom966,expectedUid966,token966))return;
                KingStability.nonFatal(this,"member-full",fullError);
                KingStability.nonFatal(this,"member-minimal",last);
                roomJoinError891("Could not join this Party Room",last);
            });
    }
    private void finishMemberJoin921(){
        if(isFinishing()||isDestroyed()||roomId==null||!cloudRoom||memberSeen)return;
        stopPartyJoinTimeout977();clearPartyReconnect979();memberSeen=true;roomVoiceOptedIn957=soundOn;renderParty();startHeartbeat900();addEvent("join",safeName()+" joined the room");if(page!=null){page.postDelayed(()->showEntranceEffect(safeName()),350);page.postDelayed(this::openRequestedPanel940,650);}
    }
    private void startHeartbeat900(){stopHeartbeat900();if(cloudRoom&&roomId!=null&&user!=null){heartbeat900();onlineHandler900.postDelayed(onlineTick900,20000);}}
    private void stopHeartbeat900(){onlineHandler900.removeCallbacks(onlineTick900);}
    private void heartbeat900(){
        if(!cloudRoom||db==null||user==null||roomId==null||roomId.trim().isEmpty()||!memberSeen)return;
        final String expectedRoom967=roomId,expectedUid967=user.getUid();
        final int expectedGeneration967=presenceGeneration967;
        final DocumentReference memberRef967=db.collection("live_rooms").document(expectedRoom967)
            .collection("members").document(expectedUid967);
        final Map<String,Object>memberPayload967=memberPayload891();
        memberRef967.set(memberPayload967,SetOptions.merge())
            .addOnSuccessListener(v->{
                if(!isActivePresence967(expectedRoom967,expectedUid967,expectedGeneration967))return;
                setOnlineState900(true,"Online • synced");
                cleanupStaleRoom910();
            })
            .addOnFailureListener(error->{
                if(!isActivePresence967(expectedRoom967,expectedUid967,expectedGeneration967))return;
                Map<String,Object>minimal967=new HashMap<>();
                minimal967.put("uid",expectedUid967);
                minimal967.put("name",safeName());
                minimal967.put("deviceId",deviceId891());
                minimal967.put("sessionId",presenceSession967);
                minimal967.put("lastSeenAt",FieldValue.serverTimestamp());
                memberRef967.set(minimal967,SetOptions.merge())
                    .addOnSuccessListener(v->{
                        if(!isActivePresence967(expectedRoom967,expectedUid967,expectedGeneration967))return;
                        setOnlineState900(true,"Online • compatibility");
                        cleanupStaleRoom910();
                    })
                    .addOnFailureListener(last->{
                        if(!isActivePresence967(expectedRoom967,expectedUid967,expectedGeneration967))return;
                        setOnlineState900(false,"Sync blocked");
                        KingStability.nonFatal(this,"party-heartbeat",error);
                        KingStability.nonFatal(this,"party-heartbeat-minimal",last);
                    });
            });
    }
    private void cleanupStaleRoom910(){
        if(!isOwner()||db==null||roomId==null||user==null||roomId.isEmpty())return;
        long now967=System.currentTimeMillis();
        if(now967-lastCleanup910<60000L)return;
        lastCleanup910=now967;
        final String expectedRoom967=roomId,expectedUid967=user.getUid(),owner967=ownerUid;
        final int generation967=presenceGeneration967;
        final DocumentReference expectedRoomRef967=db.collection("live_rooms").document(expectedRoom967);
        // Server-only reads prevent cached stale snapshots from evicting valid remote members.
        expectedRoomRef967.collection("members").get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(members967->{
                if(!isActivePresence967(expectedRoom967,expectedUid967,generation967)||!isOwner())return;
                for(DocumentSnapshot m967:members967.getDocuments()){
                    String uid967=m967.getId();
                    com.google.firebase.Timestamp last967=m967.getTimestamp("lastSeenAt");
                    boolean stale967=last967!=null
                        // Keep transient mobile-network drops from removing a live
                        // member before Firestore can reconnect and refresh presence.
                        && KingRoomPresence967.isStale(last967.toDate().getTime(),now967,300000L);
                    if(stale967&&!uid967.equals(owner967)){
                        // Re-read the current member and its seats atomically before
                        // cleanup. A reconnect may refresh lastSeenAt after this
                        // server scan but before its cleanup callback runs.
                        cleanupStaleMember910(expectedRoomRef967,uid967,
                            expectedRoom967,expectedUid967,generation967,owner967);
                    }
                }
            })
            .addOnFailureListener(e->{
                if(isActivePresence967(expectedRoom967,expectedUid967,generation967))
                    KingStability.nonFatal(this,"stale-scan",e);
            });
    }
    private void cleanupStaleMember910(DocumentReference roomRef,String candidateUid,
            String expectedRoom,String expectedUid,int expectedGeneration,String expectedOwner){
        if(roomRef==null||candidateUid==null||candidateUid.isEmpty()
            ||candidateUid.equals(expectedOwner)
            ||!isActivePresence967(expectedRoom,expectedUid,expectedGeneration)||!isOwner())return;
        DocumentReference memberRef=roomRef.collection("members").document(candidateUid);
        java.util.ArrayList<DocumentReference> seatRefs=new java.util.ArrayList<>();
        for(int seatNo=1;seatNo<=12;seatNo++)
            seatRefs.add(roomRef.collection("seats").document(String.valueOf(seatNo)));
        db.runTransaction(tx->{
            DocumentSnapshot current=tx.get(memberRef);
            if(!current.exists()||!candidateUid.equals(current.getString("uid")))return 0;
            com.google.firebase.Timestamp seen=current.getTimestamp("lastSeenAt");
            long checkedAt=System.currentTimeMillis();
            if(seen==null||!KingRoomPresence967.isStale(
                    seen.toDate().getTime(),checkedAt,300000L))return 0;
            java.util.ArrayList<DocumentSnapshot> currentSeats=new java.util.ArrayList<>();
            for(DocumentReference seatRef:seatRefs)currentSeats.add(tx.get(seatRef));
            int deletedSeats=0;
            for(int i=0;i<seatRefs.size();i++){
                DocumentSnapshot seat=currentSeats.get(i);
                if(seat.exists()&&candidateUid.equals(seat.getString("uid"))){
                    tx.delete(seatRefs.get(i));deletedSeats++;
                }
            }
            tx.delete(memberRef);
            return deletedSeats+1;
        }).addOnFailureListener(error->{
            if(isActivePresence967(expectedRoom,expectedUid,expectedGeneration))
                KingStability.nonFatal(this,"stale-member-transaction",error);
        });
    }
    private void setOnlineState900(boolean ok,String text){if(onlineStateLabel900!=null){onlineStateLabel900.setText((ok?"● ":"● ")+text);onlineStateLabel900.setTextColor(ok?0xff4ff0a3:0xffff9a9a);}}
    private void ensureRoomMembership900(){
        if(!cloudRoom||db==null||user==null||roomId==null||roomId.isEmpty()||!memberSeen)return;
        final String requestedRoom967=roomId,requestedUid967=user.getUid();
        final int requestedGeneration967=presenceGeneration967;
        final DocumentReference room967=db.collection("live_rooms").document(requestedRoom967);
        final DocumentReference member967=room967.collection("members").document(requestedUid967);
        room967.get()
            .addOnSuccessListener(roomDoc967->{
                if(!isActivePresence967(requestedRoom967,requestedUid967,requestedGeneration967))return;
                if(roomDoc967==null||!roomDoc967.exists()||
                   Boolean.TRUE.equals(roomDoc967.getBoolean("closed"))){
                    setOnlineState900(false,"Room unavailable");return;
                }
                member967.get()
                    .addOnSuccessListener(memberDoc967->{
                        if(!isActivePresence967(requestedRoom967,requestedUid967,requestedGeneration967))return;
                        if(memberDoc967!=null&&memberDoc967.exists()){
                            heartbeat900();return;
                        }
                        Map<String,Object>join967=memberPayload891();
                        join967.put("joinedAt",FieldValue.serverTimestamp());
                        member967.set(join967,SetOptions.merge())
                            .addOnSuccessListener(v->{
                                if(!isActivePresence967(requestedRoom967,requestedUid967,requestedGeneration967))return;
                                memberSeen=true;setOnlineState900(true,"Online • rejoined");
                            })
                            .addOnFailureListener(error->{
                                if(!isActivePresence967(requestedRoom967,requestedUid967,requestedGeneration967))return;
                                member967.set(minimalMember921(),SetOptions.merge())
                                    .addOnSuccessListener(v->{
                                        if(!isActivePresence967(requestedRoom967,requestedUid967,requestedGeneration967))return;
                                        memberSeen=true;setOnlineState900(true,"Online • compatibility rejoin");
                                    })
                                    .addOnFailureListener(last->{
                                        if(isActivePresence967(requestedRoom967,requestedUid967,requestedGeneration967))
                                            setOnlineState900(false,"Join blocked");
                                    });
                            });
                    })
                    .addOnFailureListener(e->{
                        if(isActivePresence967(requestedRoom967,requestedUid967,requestedGeneration967))
                            setOnlineState900(false,"Member lookup failed");
                    });
            })
            .addOnFailureListener(e->{
                if(isActivePresence967(requestedRoom967,requestedUid967,requestedGeneration967))
                    setOnlineState900(false,"Backend blocked");
            });
    }
    private void setVoicePresence900(boolean joined){
        if(!cloudRoom||db==null||user==null||roomId==null)return;
        FirebaseUser current=FirebaseAuth.getInstance().getCurrentUser();
        if(current==null||!user.getUid().equals(current.getUid()))return;
        final String uid=user.getUid(),session=presenceSession967;
        final boolean connected=joined&&voiceConferenceConnected10065;
        final DocumentReference member=db.collection("live_rooms").document(roomId).collection("members").document(uid);
        db.runTransaction(tx->{
            DocumentSnapshot existing=tx.get(member);
            if(existing.exists()&&uid.equals(existing.getString("uid"))&&session.equals(existing.getString("sessionId"))){
                Map<String,Object> changes=new HashMap<>();changes.put("voiceJoined",connected);changes.put("lastSeenAt",FieldValue.serverTimestamp());tx.update(member,changes);
            }
            return null;
        }).addOnFailureListener(error->KingStability.nonFatal(this,"voice-presence",error));
    }
    private void onlineDiagnostics900(){
        if(user==null||db==null||roomId==null){toast("Open a signed-in live Party Room first");return;}
        final String project;try{project=String.valueOf(com.google.firebase.FirebaseApp.getInstance().getOptions().getProjectId());}catch(Exception e){toast("Firebase not ready");return;}
        DocumentReference room=db.collection("live_rooms").document(roomId),member=room.collection("members").document(user.getUid());
        room.get().addOnSuccessListener(rd->member.get().addOnSuccessListener(md->room.collection("messages").limit(1).get().addOnSuccessListener(q->{
            String text="Firebase project: "+project+"\nRoom ID: "+roomId+"\nRoom Code: "+shortId()+"\nSigned in: YES\nRoom readable: "+(rd!=null&&rd.exists()?"YES":"NO")+"\nMembership: "+(md!=null&&md.exists()?"YES":"NO")+"\nMessages readable: YES\nMembers seen: "+liveMemberCount+"/"+maxMembers930+"\nStage seats: "+seatNames.size()+"/"+maxSeats+"\nMy seat: "+(mySeat>0?mySeat:"audience")+"\n\nIf the second phone shows permission denied, Firestore Security Rules are still not deployed.";
            new AlertDialog.Builder(this).setTitle("🧪 Online diagnostics").setMessage(text).setPositiveButton("Refresh membership",(d,w)->ensureRoomMembership900()).setNeutralButton("Share Room",(d,w)->shareRoom()).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->roomJoinError891("Message sync test failed",e))).addOnFailureListener(e->roomJoinError891("Membership test failed",e))).addOnFailureListener(e->roomJoinError891("Room backend test failed",e));
    }

    private void roomJoinError891(String title,Exception e){
        if(cloudRoom&&roomId!=null&&!memberSeen){
            showPartyJoinFailure977(title+"\n"+msg(e));
            return;
        }

        String detail=msg(e);String lower=detail.toLowerCase(java.util.Locale.US);
        String hint=(lower.contains("permission")||lower.contains("denied"))
            ?"Firebase backend permission denied. The latest Firestore rules must be deployed to project king-plus-2f365."
            :"Check internet, sign-in and that both phones use the same KING Plus Firebase project.";
        KingStability.nonFatal(this,"party-join",e);
        new AlertDialog.Builder(this).setTitle(title).setMessage(detail+"\n\n"+hint)
            .setPositiveButton("OK",null).setNeutralButton("Try Room Code",(d,w)->joinRoomByCode891()).show();
    }
    private void joinRoomByCode891(){
        if(user==null||db==null){requireSignInForCreate();return;}
        final EditText input=new EditText(this);input.setHint("6-digit code or KINGROOM:full-id");input.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("Join KING Party").setMessage("Paste the Room Code or full Room ID shared by the host.").setView(input)
            .setNegativeButton("Cancel",null).setPositiveButton("Join",(d,w)->{
                String raw=input.getText().toString().trim();if(raw.isEmpty())return;
                if(raw.startsWith("KINGROOM:"))raw=raw.substring("KINGROOM:".length()).trim();
                final String token=raw;
                if(token.length()>10&&!token.contains(" ")){joinRoomDocument891(token);return;}
                db.collection("live_rooms").whereEqualTo("joinCode",token).limit(5).get().addOnSuccessListener(q->{
                    if(q!=null&&!q.isEmpty()){openRoomDoc891(q.getDocuments().get(0));return;}
                    fallbackFindRoom891(token);
                }).addOnFailureListener(e->fallbackFindRoom891(token));
            }).show();
    }
    private void fallbackFindRoom891(String token){
        db.collection("live_rooms").limit(100).get().addOnSuccessListener(q->{
            for(DocumentSnapshot d:q.getDocuments()){
                if(Boolean.TRUE.equals(d.getBoolean("closed")))continue;
                String code=str(d,"joinCode",shortId(d.getId()));
                if(token.equalsIgnoreCase(code)||token.equalsIgnoreCase(shortId(d.getId()))||token.equalsIgnoreCase(d.getId())||token.equalsIgnoreCase(str(d,"name",""))){openRoomDoc891(d);return;}
            }
            toast("Room not found. Ask host to share the full KINGROOM ID.");
        }).addOnFailureListener(e->roomJoinError891("Room lookup failed",e));
    }
    private void joinRoomDocument891(String id){
        db.collection("live_rooms").document(id).get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(d->{if(d==null||!d.exists()||Boolean.TRUE.equals(d.getBoolean("closed"))){toast("Room not found or already closed");return;}openRoomDoc891(d);}).addOnFailureListener(e->roomJoinError891("Room lookup failed",e));
    }
    private void openRoomDoc891(DocumentSnapshot d){
        // Older fallback room records predate joinCode. Its own host may safely
        // backfill the code, so another phone can search by the displayed ID.
        if(d!=null && user!=null && user.getUid().equals(d.getString("ownerUid"))
           && (d.getString("joinCode")==null||d.getString("joinCode").isEmpty())){
            d.getReference().update("joinCode",shortId(d.getId()))
                .addOnFailureListener(e->KingStability.nonFatal(this,"room-code-backfill-970",e));
        }
        openCloudRoom(d.getId(),str(d,"name","Live Party"),d.getString("ownerUid"),str(d,"ownerName","Host"),Boolean.TRUE.equals(d.getBoolean("isPrivate")),Boolean.TRUE.equals(d.getBoolean("hasPassword")));
        roomEntranceMode9931=str(d,"entranceMode","legacy");roomEntryEpoch9936=str(d,"entryEpoch","");
    }

    private void tryPrivateEntry(){
        if(user==null||db==null||roomId==null)return;
        Map<String,Object>d=new HashMap<>();d.put("uid",user.getUid());d.put("name",safeName());d.put("deviceId",deviceId891());d.put("sessionId",presenceSession967);d.put("joinedAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).collection("members").document(user.getUid()).set(d)
            .addOnSuccessListener(v->{memberSeen=true;addEvent("join",safeName()+" joined the room");renderParty();if(page!=null){page.postDelayed(()->showEntranceEffect(safeName()),350);page.postDelayed(this::openRequestedPanel940,650);}})
            .addOnFailureListener(e->{if(roomHasPassword)passwordRoomJoinDialog();else toast("family".equals(roomEntranceMode9931)?"Verified KING Family membership required":"follow".equals(roomEntranceMode9931)?"Host follow connection required":"Private room • invite access required");});
    }
    private void passwordRoomJoinDialog(){
        final EditText e=new EditText(this);e.setHint("Room password");e.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("🔐 Private Party").setMessage("Enter the room password").setView(e)
            .setNegativeButton("Cancel",null).setPositiveButton("Join",(d,w)->{
                String password=e.getText().toString();if(password.length()<4){toast("Password must be at least 4 characters");return;}
                String proof=passwordProof(roomId,password);if(proof.isEmpty()){toast("Password check unavailable");return;}
                Map<String,Object> access=new HashMap<>();access.put("uid",user.getUid());access.put("passwordHash",proof);access.put("entryEpoch",roomEntryEpoch9936);access.put("createdAt",FieldValue.serverTimestamp());
                db.collection("live_rooms").document(roomId).collection("access").document(user.getUid()).set(access)
                    .addOnSuccessListener(v->tryPrivateEntry())
                    .addOnFailureListener(x->toast("Wrong room password"));
            }).show();
    }
    private String passwordProof(String id,String password){
        try{
            java.security.MessageDigest md=java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes=md.digest((id+"::"+password).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder out=new StringBuilder();for(byte b:bytes)out.append(String.format("%02x",b));return out.toString();
        }catch(Exception e){return "";}
    }
    private void openLocalRoom(String name,String hostName) { openLocalRoom(name,hostName,"Chat"); }
    private void openLocalRoom(String name,String hostName,String category) {
        roomName=name; roomCategory=category; roomTheme=prefs.getString("theme_"+name,"Classic"); maxSeats=prefs.getInt("maxSeats_"+name,8); roomId="local_"+Math.abs(name.hashCode()); ownerName=hostName; ownerUid=null; cloudRoom=false; localRemovedSeats.clear();
        hostSeatMode=prefs.getBoolean("hostSeatMode_"+roomId,false);
        loadRoomPersonalSettings();
        processedEventIds.clear(); eventSnapshotReady=false;
        mySeat=prefs.getInt("seat_"+roomId,-1); micOn=prefs.getBoolean("mic_"+roomId,false); roomLocked=prefs.getBoolean("locked_"+roomId,false);
        muteAll=prefs.getBoolean("mute_"+roomId,false); announcement=prefs.getString("notice_"+roomId,"Welcome to KING Plus • Be friendly and have fun"); renderParty();
    }

    private void showJoiningRoom964(){
        KingCrashWatch958.mark(this,"party-member-sync-964");
        LinearLayout waiting964=new LinearLayout(this);
        waiting964.setOrientation(LinearLayout.VERTICAL);
        waiting964.setPadding(dp(20),dp(20),dp(20),dp(20));
        waiting964.setGravity(Gravity.CENTER);
        waiting964.setBackgroundColor(0xff172b36);
        TextView label964=tv("Connecting to Party…\nVerifying your account and room access",
            17,Color.WHITE,true);
        label964.setGravity(Gravity.CENTER);
        waiting964.addView(label964,new LinearLayout.LayoutParams(-1,dp(110)));
        android.widget.ProgressBar progress977=new android.widget.ProgressBar(this);
        LinearLayout.LayoutParams progressParams977=new LinearLayout.LayoutParams(dp(40),dp(40));
        progressParams977.gravity=Gravity.CENTER_HORIZONTAL;
        waiting964.addView(progress977,progressParams977);
        TextView hint977=tv("If verification takes too long, you can return to the Party list.",
            12,0xffbdd3dd,false);
        hint977.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hintParams977=new LinearLayout.LayoutParams(-1,dp(75));
        waiting964.addView(hint977,hintParams977);
        TextView diagnose985=tv("🔌  Test Firebase / Room connection",15,Color.WHITE,true);
        diagnose985.setGravity(Gravity.CENTER);
        diagnose985.setBackgroundColor(0xff146e72);
        LinearLayout.LayoutParams diagnoseParams985=
            new LinearLayout.LayoutParams(-1,dp(52));
        diagnoseParams985.setMargins(0,dp(14),0,0);
        waiting964.addView(diagnose985,diagnoseParams985);
        diagnose985.setOnClickListener(v->{
            if(isFinishing()||isDestroyed())return;
            final String inspectingRoom985=roomId;
            // Abandon the join before opening a new Activity: never accept a
            // late Firebase membership/seat callback from the old screen.
            ++roomJoinGeneration966;
            ++partyReconnectGeneration979;
            clearPartyReconnect979();
            stopPartyJoinTimeout977();
            renderLobby("Hot");
            Intent connection985=new Intent(this,KingConnection983Activity.class);
            if(inspectingRoom985!=null&&!inspectingRoom985.trim().isEmpty())
                connection985.putExtra("roomInput983",inspectingRoom985);
            startActivity(connection985);
        });
        TextView back977=tv("←  Cancel and return to Party",15,Color.WHITE,true);
        back977.setGravity(Gravity.CENTER);
        back977.setBackgroundColor(0xff314659);
        LinearLayout.LayoutParams backParams977=new LinearLayout.LayoutParams(-1,dp(52));
        backParams977.setMargins(0,dp(16),0,0);
        waiting964.addView(back977,backParams977);
        back977.setOnClickListener(v->renderLobby("Hot"));
        setSafeContentView(waiting964);
    }

    private void shareRealRoom996(){
        if(!cloudRoom||!memberSeen||roomId==null||user==null
            ||KingRoomInvite959.parse(roomId).isEmpty()){
            toast("Join a real Firebase Room before sharing");return;
        }
        final String liveRoom=roomId;
        final String joinCode=shortId(liveRoom);
        final String invite="🎉 Join my KING Plus Party: "+
            (roomName==null?"KING Party":roomName)+
            "\nKINGROOM:"+liveRoom+
            "\n"+KingRoomInvite959.link(liveRoom)+
            "\n6-digit Join Code: "+joinCode;
        new AlertDialog.Builder(this)
            .setTitle("Share this Party with friends")
            .setMessage("Share the FULL Room ID; friends can open People → Join Party ID. "+
                "Private rooms still require access.")
            .setItems(new String[]{"↗ Share invitation",
                   "📋 Copy complete Room invite","📋 Copy six-digit Join Code"},(d,option)->{
                if(option==0){
                    Intent share=new Intent(Intent.ACTION_SEND);
                    share.setType("text/plain");
                    share.putExtra(Intent.EXTRA_TEXT,invite);
                    startActivity(Intent.createChooser(share,"Share KING Party"));
                }else{
                    android.content.ClipboardManager clipboard=
                        (android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                    if(clipboard!=null)clipboard.setPrimaryClip(
                        android.content.ClipData.newPlainText("KING Party invite",
                            option==1?invite:joinCode));
                    toast("Party invitation copied");
                }
            }).setNegativeButton("Close",null).show();
    }

    private void renderParty() {
        KingCrashWatch958.mark(this,"party-render");
        // Store only a SERVER-verified joined Room for the current signed-in
        // account, never a local/fake/private bypass or unconfirmed lobby room.
        if(cloudRoom&&memberSeen&&roomId!=null&&user!=null
               &&!KingRoomInvite959.parse(roomId).isEmpty()){
            getSharedPreferences("MainActivity",MODE_PRIVATE).edit()
                .putString("last_live_room_id_992",roomId)
                .putString("last_live_room_name_992",roomName==null?"KING Party":roomName)
                .putString("last_live_room_account_992",user.getUid())
                .apply();
        }
        clearListeners();
        if(cloudRoom)recordRoomVisit940();

        final int ROOM_GREEN_V532 = 0xff008e6b;
        final int SEAT_GREEN_V532 = 0x38ffffff;
        final int SEAT_TEXT_V532 = 0xbfffffff;

        FrameLayout shell = new FrameLayout(this);
        shell.setBackground(premiumRoomBackground());
        addRoomBackgroundGlow(shell);
        KingRoomStageView stageBackdrop880=new KingRoomStageView(this,roomTheme);
        shell.addView(stageBackdrop880,new FrameLayout.LayoutParams(-1,-1));

        LinearLayout roomRoot = new LinearLayout(this);
        roomRoot.setOrientation(LinearLayout.VERTICAL);
        roomRoot.setPadding(0,0,0,0);
        FrameLayout.LayoutParams roomRootLp = new FrameLayout.LayoutParams(-1,-1);
        roomRootLp.bottomMargin = dp(84);
        shell.addView(roomRoot, roomRootLp);

        // Top header: back, room title + online count, share, menu.
        LinearLayout head = new LinearLayout(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setPadding(dp(14),0,dp(14),0);
        head.setBackground(royalNeon9924(0xb31b0b34,0x7a4c207a,0x66ffdb91,12));
        TextView back = tv("‹",34,Color.WHITE,true); back.setGravity(Gravity.CENTER); back.setOnClickListener(v->confirmLeave940());
        head.addView(back,new LinearLayout.LayoutParams(dp(30),dp(56)));

        LinearLayout info = new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL); info.setGravity(Gravity.CENTER_VERTICAL); info.setPadding(dp(10),0,0,0);
        TextView rn = tv(roomName==null?"KING Plus Party":roomName,15,0xffffda8a,true); rn.setSingleLine(true); rn.setPadding(0,0,0,0); rn.setOnClickListener(v->hostProfileDialog());
        info.addView(rn,new LinearLayout.LayoutParams(-1,dp(28)));
        viewerLabel = pill("👥 …",0x33000000,this::membersDialog); viewerLabel.setTextSize(10); viewerLabel.setPadding(dp(6),0,dp(8),0);
        info.addView(viewerLabel,new LinearLayout.LayoutParams(dp(58),dp(22)));
        head.addView(info,new LinearLayout.LayoutParams(0,dp(56),1));

        TextView share = pill("↗",0x2effffff,this::shareRealRoom996); share.setTextSize(18); head.addView(share,new LinearLayout.LayoutParams(dp(32),dp(32)));
        TextView more = pill("≡",0x2effffff,this::roomMenu); more.setTextSize(18); LinearLayout.LayoutParams moreLp=new LinearLayout.LayoutParams(dp(32),dp(32)); moreLp.setMargins(dp(8),0,0,0); head.addView(more,moreLp);
        roomRoot.addView(head,new LinearLayout.LayoutParams(-1,dp(56)));

        // Rank / heart / edit bar.
        LinearLayout badges = new LinearLayout(this); badges.setGravity(Gravity.CENTER_VERTICAL); badges.setPadding(dp(14),dp(2),dp(14),dp(6));
        String compact=shortId(); if(compact.length()>2) compact=compact.substring(compact.length()-2);
        roomLevelLabel=pill("👑 Room • syncing",0xff8e24aa,()->openParity870("vip")); roomLevelLabel.setTextSize(10); roomLevelLabel.setPadding(dp(8),0,dp(8),0);
        badges.addView(roomLevelLabel,new LinearLayout.LayoutParams(-2,dp(24)));
        heartLevelLabel=pill("💖 Heart • syncing",0x26000000,()->openParity870("match")); heartLevelLabel.setTextSize(10); heartLevelLabel.setPadding(dp(8),0,dp(8),0);
        LinearLayout.LayoutParams heartLp=new LinearLayout.LayoutParams(-2,dp(24)); heartLp.setMargins(dp(8),0,0,0); badges.addView(heartLevelLabel,heartLp);
        onlineStateLabel900=pill("● Online",0x33000000,this::onlineDiagnostics900);onlineStateLabel900.setTextSize(9);onlineStateLabel900.setTextColor(0xff4ff0a3);LinearLayout.LayoutParams onlineLp=new LinearLayout.LayoutParams(-2,dp(24));onlineLp.setMargins(dp(8),0,0,0);badges.addView(onlineStateLabel900,onlineLp);
        TextView themeChip954=pill(themeIcon954()+" "+roomTheme,0x33000000,this::atmospherePanel);themeChip954.setTextSize(9);themeChip954.setTextColor(themeAccent954());LinearLayout.LayoutParams themeLp954=new LinearLayout.LayoutParams(-2,dp(24));themeLp954.setMargins(dp(8),0,0,0);badges.addView(themeChip954,themeLp954);
        View badgeSpacer=new View(this); badges.addView(badgeSpacer,new LinearLayout.LayoutParams(0,1,1));
        TextView edit=pill("📑 Edit",0x33000000,isOwner()?this::roomControlCenter:this::roomBillboardDialog); edit.setTextSize(11); edit.setPadding(dp(10),0,dp(10),0);
        badges.addView(edit,new LinearLayout.LayoutParams(-2,dp(26)));
        HorizontalScrollView badgeScroll10067=new HorizontalScrollView(this);roomBadges10067=badgeScroll10067;
        badgeScroll10067.setHorizontalScrollBarEnabled(false);badgeScroll10067.addView(badges);
        roomRoot.addView(badgeScroll10067,new LinearLayout.LayoutParams(-1,dp(36)));
        // Visible actions open existing real Firebase-backed screens.
        HorizontalScrollView roomActions9916=new HorizontalScrollView(this);roomActions10067=roomActions9916;
        roomActions9916.setHorizontalScrollBarEnabled(false);
        LinearLayout actionRow9916=new LinearLayout(this);
        actionRow9916.setGravity(Gravity.CENTER_VERTICAL);
        actionRow9916.setPadding(dp(12),0,dp(12),0);
        String[] actionNames9916={"🎙 Seats","🎮 Games","👥 Members","🔗 Invite","🛡 Host","🎁 Gifts"};
        Runnable[] actionCallbacks9916={this::roomSeatPanel,this::openPartyGameChooser9915,this::membersDialog,
            this::shareRealRoom996,this::moderationPanel,this::giftShopPanel};
        for(int i9916=0;i9916<actionNames9916.length;i9916++){
            TextView action9916=(TextView)getLayoutInflater().inflate(R.layout.king_party_host_action_10064,actionRow9916,false);action9916.setText(actionNames9916[i9916]);final Runnable action10064=actionCallbacks9916[i9916];action9916.setOnClickListener(v->action10064.run());
            action9916.setBackground(royalNeon9924(0xff713099,0xff2d1a58,0x99efb0ff,13));
            action9916.setTextSize(11);action9916.setTextColor(Color.WHITE);
            action9916.setContentDescription(actionNames9916[i9916]);
            LinearLayout.LayoutParams lp9916=new LinearLayout.LayoutParams(dp(96),dp(48));
            lp9916.setMargins(dp(3),0,dp(3),0);
            actionRow9916.addView(action9916,lp9916);
        }
        roomActions9916.addView(actionRow9916,new HorizontalScrollView.LayoutParams(-2,dp(48)));
        roomRoot.addView(roomActions9916,new LinearLayout.LayoutParams(-1,dp(50)));
        voiceStatus10065=(TextView)getLayoutInflater().inflate(R.layout.king_party_voice_status_10065,roomRoot,false);voiceStatus10065.setText("Voice connecting…");
        voiceStatus10065.setMinHeight(dp(44));voiceStatus10065.setOnClickListener(v->showVoiceConnection10065());
        roomRoot.addView(voiceStatus10065,new LinearLayout.LayoutParams(-1,-2));
        if(!requestedGame940.isEmpty()){
            TextView gamePrompt940=pill("🎮 "+requestedGame940+" • Open Room Games",0x553b1b6b,()->{
                Intent gi940=new Intent(this,RoomGameActivity.class);
                gi940.putExtra("roomId",roomId);
                gi940.putExtra("roomName",roomName);
                gi940.putExtra("displayName",safeName());
                gi940.putExtra("requestedGame",requestedGame940);
                startActivity(gi940);
            });
            gamePrompt940.setTextSize(11);
            roomRoot.addView(gamePrompt940,new LinearLayout.LayoutParams(-1,dp(34)));
        }

        // Hidden state labels retained for existing realtime/controller logic.
        roomLiveBadge=pill("👥 1 • 🎙 0",0x00302747,null); roomLiveBadge.setVisibility(View.GONE); roomRoot.addView(roomLiveBadge,new LinearLayout.LayoutParams(1,1));
        roomStateLabel=tv("",1,Color.TRANSPARENT,false); roomStateLabel.setVisibility(View.GONE); roomRoot.addView(roomStateLabel,new LinearLayout.LayoutParams(1,1)); refreshRoomState();
        supporterLabel=tv("",1,Color.TRANSPARENT,false); supporterLabel.setVisibility(View.GONE); roomRoot.addView(supporterLabel,new LinearLayout.LayoutParams(1,1));
        announcementLabel=tv("",1,Color.TRANSPARENT,false); announcementLabel.setVisibility(View.GONE); roomRoot.addView(announcementLabel,new LinearLayout.LayoutParams(1,1));
        seatRequestLabel=tv("",1,Color.TRANSPARENT,false); seatRequestLabel.setVisibility(View.GONE); roomRoot.addView(seatRequestLabel,new LinearLayout.LayoutParams(1,1));
        // Display over the seats rather than in a 1×1 invisible placeholder.
        // Attach after the FX layer so voice/game/gift notices are actually readable.
        reactionBanner=tv("",15,Color.WHITE,true);
        reactionBanner.setGravity(Gravity.CENTER);
        reactionBanner.setMaxLines(2);
        reactionBanner.setPadding(dp(16),dp(10),dp(16),dp(10));
        reactionBanner.setVisibility(View.GONE);

        // 8-seat compact grid.
        seatsBox = new LinearLayout(this); seatsBox.setOrientation(LinearLayout.VERTICAL); seatsBox.setPadding(0,dp(4),0,dp(6));
        int displayDp9916=(int)(getResources().getDisplayMetrics().heightPixels/
            Math.max(1f,getResources().getDisplayMetrics().density));
        int seatsDp9916=displayDp9916<720?152:178;
        roomRoot.addView(seatsBox,new LinearLayout.LayoutParams(-1,dp(seatsDp9916)));
        rebuildSeats(); // compute final row/avatar dimensions based on current screen

        // Chat + safety section takes the remaining height.
        LinearLayout chatSection = new LinearLayout(this); chatSection.setOrientation(LinearLayout.VERTICAL); chatSection.setPadding(dp(12),0,dp(12),0);
        TextView safety = tv("KING Plus Safety • Be respectful. Child endangerment, harassment and prohibited content can lead to removal or bans.",10,0xffffe580,true);
        safety.setBackground(bg(0x2e004d3a,10)); safety.setPadding(dp(8),dp(7),dp(8),dp(7));
        safety.setOnClickListener(v->{ if(isModerator()) editAnnouncement(); });
        chatSection.addView(safety,new LinearLayout.LayoutParams(-1,-2));

        ScrollView chatScroll = new ScrollView(this); roomChatScroll=chatScroll; chatScroll.setFillViewport(true); chatScroll.setVerticalScrollBarEnabled(false); chatScroll.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout chatContent = new LinearLayout(this); chatContent.setOrientation(LinearLayout.VERTICAL); chatContent.setPadding(0,dp(2),0,dp(2));
        feedBox = new LinearLayout(this); feedBox.setOrientation(LinearLayout.VERTICAL); chatContent.addView(feedBox); seedLocalFeed();
        chatBox = new LinearLayout(this); chatBox.setOrientation(LinearLayout.VERTICAL); chatContent.addView(chatBox); seedLocalChat();
        chatScroll.addView(chatContent,new ScrollView.LayoutParams(-1,-2));
        chatSection.addView(chatScroll,new LinearLayout.LayoutParams(-1,0,1));

        HorizontalScrollView quickScroll = new HorizontalScrollView(this); quickScroll.setHorizontalScrollBarEnabled(false); quickScroll.setFillViewport(false);
        LinearLayout quick = new LinearLayout(this); quick.setOrientation(LinearLayout.HORIZONTAL); quick.setGravity(Gravity.CENTER_VERTICAL);
        addQuickChipV532(quick,"Nice to meet everyone!",composerBox);
        addQuickChipV532(quick,"😂😂😂😂😂",composerBox);
        addQuickChipV532(quick,"Hi",composerBox);
        addQuickChipV532(quick,"Nice",composerBox);
        quickScroll.addView(quick,new HorizontalScrollView.LayoutParams(-2,dp(34)));
        chatSection.addView(quickScroll,new LinearLayout.LayoutParams(-1,dp(38)));
        roomRoot.addView(chatSection,new LinearLayout.LayoutParams(-1,0,1));

        // Bottom input panel, matching the supplied layout while preserving all room functions.
        LinearLayout composer = new LinearLayout(this); composer.setGravity(Gravity.CENTER_VERTICAL); composer.setPadding(dp(6),dp(7),dp(6),dp(9)); composer.setBackground(royalNeon9924(0xff34164f,0xff17102f,0xaaffcf9a,12));
        composer.setMinimumHeight(dp(76));
        TextView plus=pill("＋",0x00ffffff,this::toolsPanel); plus.setTextColor(0xffffd991); plus.setTextSize(24); composer.addView(plus,new LinearLayout.LayoutParams(dp(34),dp(48)));
        composerBox=new EditText(this); composerBox.setHint("Type message..."); composerBox.setTextColor(Color.WHITE); composerBox.setHintTextColor(0xffbfaad6); composerBox.setSingleLine(true); composerBox.setTextSize(14); composerBox.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEND); composerBox.setOnEditorActionListener((v, actionId, event)->{ if(actionId==android.view.inputmethod.EditorInfo.IME_ACTION_SEND){sendMessage(composerBox);return true;}return false;}); composerBox.setBackground(royalNeon9924(0xff322053,0xff211438,0x88d2a5ea,20)); composerBox.setPadding(dp(10),0,dp(10),0);
        composerBox.setOnFocusChangeListener((v,focused)->{if(focused){ViewCompat.requestApplyInsets(shell);if(roomChatScroll!=null)roomChatScroll.postDelayed(()->roomChatScroll.fullScroll(View.FOCUS_DOWN),120);}});
        composerBox.setMinWidth(0); composer.addView(composerBox,new LinearLayout.LayoutParams(0,dp(48),1));
        TextView emoji=pill("🌈",0x00ffffff,this::showLiveEmojiPanelV530); emoji.setTextColor(0xff732eb6); emoji.setTextSize(23); composer.addView(emoji,new LinearLayout.LayoutParams(dp(38),dp(46)));
        micLabel=pill(micOn?"🎤":"🎙",0x00ffffff,this::toggleMic); micLabel.setTextColor(0xffffd991); micLabel.setTextSize(22); composer.addView(micLabel,new LinearLayout.LayoutParams(dp(38),dp(46)));
        TextView gift=pill("🎁",0x00ffffff,this::giftShopPanel); gift.setTextColor(0xffaf3583); gift.setTextSize(24); gift.setContentDescription("Animated cinematic Gift Shop"); composer.addView(gift,new LinearLayout.LayoutParams(dp(38),dp(46)));
        TextView send=pill("➤",0x00ffffff,()->sendMessage(composerBox)); send.setTextColor(themeAccent954()); send.setTextSize(24); send.setGravity(Gravity.CENTER); composer.addView(send,new LinearLayout.LayoutParams(dp(40),dp(48)));
        refreshMicControl();
        plus.setContentDescription("Open Play Center"); emoji.setContentDescription("Emoji and reactions"); gift.setContentDescription("Send a gift"); send.setContentDescription("Send message");
        FrameLayout.LayoutParams composerLp=new FrameLayout.LayoutParams(-1,dp(82)); composerLp.gravity=Gravity.BOTTOM; shell.addView(composer,composerLp);

        setSafeContentView(shell);
        partyShell940=shell;
        // v9.5.7 diagnostic: native Jitsi must not start just because the room UI renders.
        if(roomVoiceOptedIn957)attachInRoomVoice940(shell);

        // Full-screen transparent live emoji overlay must remain above the room UI.
        liveEmojiStageV530 = new FrameLayout(this); liveEmojiStageV530.setClipChildren(false); liveEmojiStageV530.setClipToPadding(false); liveEmojiStageV530.setClickable(false); liveEmojiStageV530.setFocusable(false);
        FrameLayout.LayoutParams liveOverlayLpV531 = new FrameLayout.LayoutParams(-1,-1); liveOverlayLpV531.bottomMargin=dp(64); shell.addView(liveEmojiStageV530,liveOverlayLpV531);
        FrameLayout.LayoutParams bannerLp10006=new FrameLayout.LayoutParams(-1,-2,Gravity.TOP|Gravity.CENTER_HORIZONTAL);
        bannerLp10006.topMargin=dp(184);
        bannerLp10006.leftMargin=dp(18);bannerLp10006.rightMargin=dp(18);
        reactionBanner.setElevation(dp(9));
        shell.addView(reactionBanner,bannerLp10006);

        musicStatusLabel=tv(currentSong.isEmpty()?"":"🎵 "+currentSong,11,Color.WHITE,true); musicStatusLabel.setVisibility(View.GONE);

        if(cloudRoom) attachCloudRoom();
        else { viewerLabel.setText("👥 —"); fillLocalSeats(); if(roomRoot!=null) roomRoot.postDelayed(()->showEntranceEffect(safeName()),250); }
    }

    private void addQuickChipV532(LinearLayout row, String text, EditText ignored) {
        TextView chip = tv(text,11,0xffefdefd,false); chip.setGravity(Gravity.CENTER);
        chip.setBackground(royalNeon9924(0xff45265f,0xff281941,0x88c992e4,14)); chip.setPadding(dp(12),dp(4),dp(12),dp(4));
        chip.setOnClickListener(v->{ if(composerBox!=null){ composerBox.setText(text); composerBox.setSelection(composerBox.getText().length()); } });
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,dp(30)); lp.setMargins(0,dp(2),dp(6),dp(2)); row.addView(chip,lp);
    }

    private void addMemberStrip() {
        memberStripBox = new LinearLayout(this); memberStripBox.setGravity(Gravity.CENTER_VERTICAL); memberStripBox.setPadding(0,dp(5),0,dp(5));
        page.addView(memberStripBox,new LinearLayout.LayoutParams(-1,dp(46)));
        rebuildMemberStrip();
    }
    private void rebuildMemberStrip() {
        if(memberStripBox==null)return;
        memberStripBox.removeAllViews();
        List<String> names=new ArrayList<>();List<String> uids=new ArrayList<>();
        if(ownerName!=null&&!ownerName.trim().isEmpty()){names.add(ownerName);uids.add(ownerUid==null?"":ownerUid);}
        if(cloudRoom){for(String n:memberNames){if(n==null||n.trim().isEmpty()||names.contains(n))continue;names.add(n);String uid=memberUids.get(n);uids.add(uid==null?"":uid);}}
        if(names.isEmpty()){names.add("Host");uids.add("");}
        int shown=0;
        for(int i=0;i<names.size()&&shown<6;i++,shown++){
            String n=names.get(i),uid=i<uids.size()?uids.get(i):"";
            String initial=n.trim().isEmpty()?"?":n.trim().substring(0,1).toUpperCase();
            TextView fallback=tv(initial,12,Color.WHITE,true);fallback.setGravity(Gravity.CENTER);fallback.setBackground(bg(0xff5b3a78,40));
            FrameLayout frame=new FrameLayout(this);String equipped=memberFrame730.get(uid);if(equipped==null||equipped.trim().isEmpty())equipped="Minimal Frame";frame.setBackground(KingCosmetics.avatarFrame(this,equipped,uid!=null&&!uid.isEmpty()&&uid.equals(ownerUid)));frame.setPadding(dp(2),dp(2),dp(2),dp(2));
            frame.addView(fallback,new FrameLayout.LayoutParams(-1,-1));ImageView photo=new ImageView(this);photo.setScaleType(ImageView.ScaleType.CENTER_CROP);frame.addView(photo,new FrameLayout.LayoutParams(-1,-1));
            String photoUrl=memberPhotos540.get(uid);if(user!=null&&uid!=null&&uid.equals(user.getUid())){String own=cloudProfilePhoto868();if(own.isEmpty()&&user.getPhotoUrl()!=null)own=user.getPhotoUrl().toString();photoUrl=own;}if(photoUrl!=null&&!photoUrl.isEmpty())applyProfilePhoto868(photo,fallback,photoUrl);
            final String tapUid=uid, tapName=n;frame.setOnClickListener(v->{if(tapUid!=null&&!tapUid.isEmpty())memberProfileDialog(tapUid,tapName);});
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(36),dp(36));lp.setMargins(0,0,dp(5),0);memberStripBox.addView(frame,lp);
        }
        int extra=Math.max(0,names.size()-6);TextView label=tv(cloudRoom?("Members "+liveMemberCount+(extra>0?" • +"+extra:"")):"Host",11,MUTED,false);memberStripBox.addView(label,new LinearLayout.LayoutParams(0,dp(36),1));
    }

    private void addQuick(LinearLayout row,String text,Runnable action) {
        TextView v = pill(text,CARD,action); v.setTextSize(11); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(56),1);p.setMargins(dp(2),0,dp(2),0);row.addView(v,p);
    }
    private void rebuildSeats() {
        if (seatsBox == null) return;
        seatsBox.removeAllViews();seatViews560.clear();
        final int displaySeatsV532 = maxSeats==10?10:(maxSeats==12?12:8);
        final int columns=displaySeatsV532==10?5:4;
        final int rows=(displaySeatsV532+columns-1)/columns;
        final float density9920=Math.max(1f,getResources().getDisplayMetrics().density);
        final int heightDp9920=(int)(getResources().getDisplayMetrics().heightPixels/density9920);
        final int widthDp9920=(int)(getResources().getDisplayMetrics().widthPixels/density9920);
        final boolean compact9920=heightDp9920<720||widthDp9920<370;
        final int avatarDp9920=compact9920?(widthDp9920<340?40:44):56;
        final int labelDp9920=compact9920?19:22;
        final int rowDp9920=compact9920?68:80;
        final int rowGapDp9920=compact9920?4:8;
        android.view.ViewGroup.LayoutParams layout=seatsBox.getLayoutParams();
        if(layout!=null){
            layout.height=dp(rows*rowDp9920+(rows-1)*rowGapDp9920+10);
            seatsBox.setLayoutParams(layout);
        }
        for (int r=0;r<rows;r++) {
            LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER); row.setWeightSum(columns);
            for (int c=0;c<columns;c++) {
                int no=r*columns+c+1; if(no>displaySeatsV532) break;
                LinearLayout seat=(LinearLayout)getLayoutInflater().inflate(R.layout.king_party_mic_seat_10064,row,false);
                String n=seatNames.get(no); boolean mine=cloudRoom?user!=null&&user.getUid().equals(seatUids.get(no)):no==mySeat; boolean seatLocked=cloudRoom&&lockedSeats.contains(no);
                boolean hostVisualV532 = n!=null && (cloudRoom ? ownerUid!=null && ownerUid.equals(seatUids.get(no)) : no==mySeat);
                String icon = hostVisualV532 ? "👑" : (n==null?(seatLocked?"🔒":"＋"):(mine?"👑":avatarForSeat(no,n)));
                TextView av=seat.findViewById(R.id.party_seat_fallback_10064);av.setText(icon);av.setTextSize(hostVisualV532?25:(n==null?22:24));av.setTextColor(hostVisualV532?Color.WHITE:(n==null?0xbfffffff:Color.WHITE));
                av.setGravity(Gravity.CENTER); av.setBackground(bg(hostVisualV532?0xff52b69a:(n==null?0x38ffffff:(mine?0xff2e9d83:seatColor(no))),27));
                String seatUid720=seatUids.get(no);int vip720=seatUid720==null?0:(memberVip720.containsKey(seatUid720)?memberVip720.get(seatUid720):0);int level720=seatUid720==null?0:(memberLevel720.containsKey(seatUid720)?memberLevel720.get(seatUid720):0);String frame730=seatUid720==null?"Minimal Frame":memberFrame730.get(seatUid720);
                if(frame730==null||frame730.trim().isEmpty())frame730=vip720>=10?"Crown Frame":vip720>=7?"Galaxy Frame":vip720>=4?"Royal Frame":"Minimal Frame";
                if(mine){LevelSystem.Snapshot me720=LevelSystem.read(this);vip720=KingPremiumAccess981.PAID_VIP_ACTIVE?me720.vipLevel:0;level720=me720.level;frame730=KingCosmetics.frame(this);}
                FrameLayout avatarFrame=seat.findViewById(R.id.party_seat_avatar_10064);avatarFrame.setBackground(KingCosmetics.avatarFrame(this,frame730,hostVisualV532));avatarFrame.setLayoutParams(new LinearLayout.LayoutParams(dp(avatarDp9920),dp(avatarDp9920)));ImageView photo=seat.findViewById(R.id.party_seat_photo_10064);
                String photoUrl=memberPhotos540.get(seatUids.get(no));if(mine){String localPhoto868=localProfilePhoto868();if(!localPhoto868.isEmpty())photoUrl=localPhoto868;else{String cloudPhoto868=cloudProfilePhoto868();if(!cloudPhoto868.isEmpty())photoUrl=cloudPhoto868;else if(user!=null&&user.getPhotoUrl()!=null)photoUrl=user.getPhotoUrl().toString();}}if(n!=null&&photoUrl!=null&&!photoUrl.isEmpty())applyProfilePhoto868(photo,av,photoUrl);
                String label;
                if(hostVisualV532) label="👑 "+shortSeatName(n)+(vip720>0?" • V"+vip720:"");
                else if(n==null) label=seatLocked?"NO."+no+" 🔒":"NO."+no;
                else label=(mine?"👑 ":"")+shortSeatName(n)+(level720>0?" • Lv"+level720:"")+(vip720>0?" • V"+vip720:"")+(Boolean.FALSE.equals(seatMics.get(no))?" 🔇":"");
                TextView lab=seat.findViewById(R.id.party_seat_label_10064);lab.setText(label);lab.setTextColor(n==null&&!hostVisualV532?0xd8ffffff:Color.WHITE);lab.setTypeface(null,n!=null||hostVisualV532?Typeface.BOLD:Typeface.NORMAL);lab.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(labelDp9920)));seat.setContentDescription("Mic seat "+no+", "+(n==null?(seatLocked?"locked":"empty"):n+", "+(Boolean.TRUE.equals(seatMics.get(no))?"microphone on":"microphone muted")));
                seatViews560.put(no,seat);final int seatNo=no; seat.setOnClickListener(v->seatAction(seatNo));
                seat.setOnLongClickListener(v->{
                    if(canModerateSeatsV530()&&seatUids.get(seatNo)!=null){
                        showOccupiedSeatMenuV530(seatNo);
                        return true;
                    }
                    return false;
                });
                row.addView(seat,new LinearLayout.LayoutParams(0,dp(rowDp9920),1));
            }
            LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(rowDp9920)); if(r>0) rp.setMargins(0,dp(rowGapDp9920),0,0); seatsBox.addView(row,rp);
        }
    }
    private int seatColor(int no){int[] colors={0xff5d315f,0xff1f6f8d,0xff67408a,0xff365a82,0xff6d4e3a,0xff27655f};return colors[Math.abs(no)%colors.length];}
    private GradientDrawable vipFrame720(int vip,boolean host){GradientDrawable d=new GradientDrawable();d.setShape(GradientDrawable.OVAL);d.setColor(host?0xffe9b949:0x38ffffff);int stroke=0xff8f7b99;if(vip>=10)stroke=0xffff4fc3;else if(vip>=7)stroke=0xffffcf47;else if(vip>=4)stroke=0xff70dcff;else if(vip>0)stroke=0xffc78cff;if(host)stroke=0xffffe27a;d.setStroke(dp(vip>0||host?3:1),stroke);return d;}
    private String avatarForSeat(int no,String name){String[] icons={"🙂","👩","👨","😎","🧑","👸","🤴","🧔","👩‍🎤","🧑‍🎤","🕺","💃"};return icons[(Math.max(1,no)-1)%icons.length];}
    private String shortSeatName(String name){if(name==null)return "Guest";String n=name.trim();return n.length()>10?n.substring(0,9)+"…":n;}
    private void fillLocalSeats() {
        seatNames.clear();seatUids.clear();seatMics.clear();
        if(mySeat>0){seatNames.put(mySeat,displayName);seatMics.put(mySeat,micOn);}rebuildSeats();
    }
    private void seatAction(int no) {
        if (cloudRoom && canModerateSeatsV530() && user != null) {
            String targetUidV530 = seatUids.get(no);
            if (targetUidV530 != null && !user.getUid().equals(targetUidV530)) { openSeatProfile(no); return; }
            if (targetUidV530 == null) { showEmptySeatMenuV530(no); return; }
        }
        if(cloudRoom){
            String existing=seatUids.get(no);
            if(existing!=null){openSeatProfile(no);return;}
            if(isModerator()){moderatorEmptySeatMenu(no);return;}
            if(roomLocked||lockedSeats.contains(no)){requestSeat(no);return;}
            cloudSeatAction(no);return;
        }
        if (roomLocked && !isModerator() && mySeat<0) { toast("Room seats are locked by host"); return; }
        if (no==mySeat) { mySeat=-1;micOn=false;prefs.edit().remove("seat_"+roomId).putBoolean("mic_"+roomId,false).apply(); }
        else if (seatNames.get(no)!=null) { openSeatProfile(no); return; }
        else { mySeat=no;prefs.edit().putInt("seat_"+roomId,no).apply(); }
        fillLocalSeats(); refreshMicControl();
    }
    private void moderatorEmptySeatMenu(int no){
        String[] items={lockedSeats.contains(no)?"🔓 Unlock this seat":"🔒 Lock this seat","🎙 Take this seat","👤 Assign member","📥 Seat requests"};
        new AlertDialog.Builder(this).setTitle("Seat "+no).setItems(items,(d,w)->{
            if(w==0)toggleIndividualSeatLock(no);else if(w==1)cloudSeatAction(no);else if(w==2)assignMemberToSeatDialog(no);else showSeatRequests(no);
        }).setNegativeButton("Close",null).show();
    }
    private void toggleIndividualSeatLock(int no){
        if(!isModerator()||db==null)return;
        DocumentReference ref=db.collection("live_rooms").document(roomId).collection("seat_locks").document(String.valueOf(no));
        if(lockedSeats.contains(no))ref.delete().addOnFailureListener(e->toast(msg(e)));
        else{Map<String,Object>d=new HashMap<>();d.put("locked",true);d.put("byUid",user.getUid());d.put("updatedAt",FieldValue.serverTimestamp());ref.set(d).addOnFailureListener(e->toast(msg(e)));}
    }
    private void requestSeat(int no){
        final String requestedRoom968=roomId;
        final String requestedUid968=user==null?null:user.getUid();
        if(!KingPartyMic968.validRequest(requestedRoom968,requestedUid968,no,maxSeats,
                cloudRoom&&memberSeen&&!isFinishing()&&!isDestroyed(),true)||db==null){
            toast("Join an active Party before requesting a valid Mic seat");return;
        }
        final int generation968=presenceGeneration967;
        final DocumentReference req968=db.collection("live_rooms").document(requestedRoom968)
            .collection("seat_requests").document(KingPartyMic968.requestId(requestedUid968));
        final DocumentReference room10024=db.collection("live_rooms").document(requestedRoom968);
        final DocumentReference member10024=room10024.collection("members").document(requestedUid968);
        db.runTransaction(tx10024->{
            DocumentSnapshot live10024=tx10024.get(room10024);
            DocumentSnapshot membership10024=tx10024.get(member10024);
            DocumentSnapshot old10024=tx10024.get(req968);
            if(!live10024.exists()||Boolean.TRUE.equals(live10024.getBoolean("closed"))
                ||!membership10024.exists()
                ||!requestedUid968.equals(membership10024.getString("uid")))
                throw new IllegalStateException("Room membership is no longer active");
            Long capacity10024=live10024.getLong("maxSeats");
            if(no<1||no>(capacity10024==null?8:capacity10024.intValue()))
                throw new IllegalStateException("Seat is not in the live room layout");
            if(old10024.exists())return false;
            Map<String,Object> request10024=new HashMap<>();
            request10024.put("uid",requestedUid968);
            request10024.put("name",safeName());
            request10024.put("seatNo",no);
            request10024.put("createdAt",FieldValue.serverTimestamp());
            tx10024.set(req968,request10024);
            return true;
        }).addOnSuccessListener(created10024->{
            if(isActivePresence967(requestedRoom968,requestedUid968,generation968))
                toast(Boolean.TRUE.equals(created10024)?"Seat "+no+" request sent to host":"Mic request already pending");
        }).addOnFailureListener(error10024->{
            if(isActivePresence967(requestedRoom968,requestedUid968,generation968))
                toast("Seat request not saved: "+msg(error10024));
        });
    }
    private void showSeatRequests(int no){
        if(!canModerateSeatsV530()||db==null||user==null||roomId==null)return;
        final String room10016=roomId,actor10016=user.getUid();
        final int generation10016=presenceGeneration967;
        db.collection("live_rooms").document(room10016).collection("seat_requests")
            .whereEqualTo("seatNo",no).get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(snap->{
                if(!seatRequestContext10016(room10016,actor10016,generation10016))return;
                if(snap==null||snap.isEmpty()){toast("No requests for seat "+no);return;}
                List<DocumentSnapshot> docs=snap.getDocuments();List<String> labels=new ArrayList<>();
                for(DocumentSnapshot d:docs)labels.add(str(d,"name","User"));
                new AlertDialog.Builder(this).setTitle("Seat "+no+" requests")
                    .setItems(labels.toArray(new String[0]),(x,w)->{
                        if(!seatRequestContext10016(room10016,actor10016,generation10016)){
                            toast("Party room changed. Reopen requests.");return;
                        }
                        seatRequestDecision(docs.get(w),no,room10016,actor10016,generation10016);
                    }).setNegativeButton("Close",null).show();
            }).addOnFailureListener(e->{
                if(seatRequestContext10016(room10016,actor10016,generation10016))toast(msg(e));
            });
    }
    private boolean seatRequestContext10016(String room10016,String actor10016,int generation10016){
        return isActivePresence967(room10016,actor10016,generation10016)
            && canModerateSeatsV530();
    }
    private void seatRequestDecision(DocumentSnapshot req,int no){
        if(req==null||roomId==null||user==null)return;
        seatRequestDecision(req,no,roomId,user.getUid(),presenceGeneration967);
    }
    private void seatRequestDecision(DocumentSnapshot req,int no,String room10016,String actor10016,int generation10016){
        if(req==null||!seatRequestContext10016(room10016,actor10016,generation10016))return;
        String uid=req.getString("uid"),name=str(req,"name","User");
        if(uid==null||uid.isEmpty()||!req.getReference().getParent().getParent().getId().equals(room10016))return;
        String[] items={"✓ Approve","✕ Deny"};
        new AlertDialog.Builder(this).setTitle(name+" → Seat "+no).setItems(items,(d,w)->{
            if(!seatRequestContext10016(room10016,actor10016,generation10016)){
                toast("Party or moderator role changed. Reopen requests.");return;
            }
            if(w==0){approveSeatRequest(req.getId(),uid,name,no);return;}
            // Deny only the exact still-pending request, never a new request by
            // another account reusing an old menu row on a different phone.
            db.runTransaction(tx10016->{
                DocumentSnapshot latest10016=tx10016.get(req.getReference());
                Long requestedSeat10016=latest10016.getLong("seatNo");
                if(!latest10016.exists()||!uid.equals(latest10016.getString("uid"))
                    ||requestedSeat10016==null||requestedSeat10016.intValue()!=no)
                    throw new IllegalStateException("Seat request changed. Refresh requests.");
                tx10016.delete(req.getReference());return null;
            }).addOnSuccessListener(v->{
                if(seatRequestContext10016(room10016,actor10016,generation10016))toast("Seat request declined");
            }).addOnFailureListener(e->{
                if(seatRequestContext10016(room10016,actor10016,generation10016))toast("Deny failed: "+msg(e));
            });
        }).show();
    }
    // One transaction checks a member, target seat, previous seat and optional
    // pending request before writing. It never silently displaces other users.
    private void approveOrMoveMemberSeat9913(String uid9913,String name9913,int prior9913,
            int target9913,DocumentReference request9913,String action9913){
        if(!cloudRoom||!memberSeen||!canModerateSeatsV530()||db==null||user==null||roomId==null){
            toast("Host/co-host must be in this live room");return;
        }
        if(uid9913==null||uid9913.isEmpty()||target9913<1||target9913>maxSeats){
            toast("Invalid member or mic seat");return;
        }
        final String roomId9913=roomId, actor9913=user.getUid();
        final int generation9913=presenceGeneration967;
        DocumentReference room9913=db.collection("live_rooms").document(roomId9913);
        DocumentReference member9913=room9913.collection("members").document(uid9913);
        DocumentReference seat9913=room9913.collection("seats").document(String.valueOf(target9913));
        if(prior9913<1){
            for(Map.Entry<Integer,String> entry9913:new HashMap<>(seatUids).entrySet()){
                if(uid9913.equals(entry9913.getValue())){prior9913=entry9913.getKey();break;}
            }
        }
        db.runTransaction(tx9913->{
            // Room capacity, membership and every possible seat are read
            // BEFORE ANY write, so stale phones/Host approvals cannot race.
            DocumentSnapshot roomFresh9970=tx9913.get(room9913);
            DocumentSnapshot memberDoc9913=tx9913.get(member9913);
            DocumentSnapshot reqDoc9913=request9913==null?null:tx9913.get(request9913);
            if(!roomFresh9970.exists()||!memberDoc9913.exists()
                ||!uid9913.equals(memberDoc9913.getString("uid")))
                throw new IllegalStateException("Party member left; seat not assigned");
            Long capacity9970=roomFresh9970.getLong("maxSeats");
            int max9970=capacity9970==null?8:capacity9970.intValue();
            if(!KingPartyLiveSeats9970.allowed(max9970,target9913))
                throw new IllegalStateException("Mic seat no longer exists in room");
            java.util.ArrayList<DocumentReference> refs9970=new java.util.ArrayList<>();
            java.util.ArrayList<DocumentSnapshot> seats9970=new java.util.ArrayList<>();
            for(int number9970=1;number9970<=12;number9970++){
                DocumentReference ref9970=room9913.collection("seats")
                    .document(String.valueOf(number9970));
                refs9970.add(ref9970);seats9970.add(tx9913.get(ref9970));
            }
            DocumentSnapshot targetDoc9913=seats9970.get(target9913-1);
            if(targetDoc9913.exists()&&!uid9913.equals(targetDoc9913.getString("uid")))
                throw new IllegalStateException("Mic seat already belongs to another member");
            if(request9913!=null){
                Long requestedSeat10017=reqDoc9913.exists()?reqDoc9913.getLong("seatNo"):null;
                if(!reqDoc9913.exists()||!uid9913.equals(reqDoc9913.getString("uid"))
                    ||requestedSeat10017==null||requestedSeat10017.intValue()!=target9913)
                    throw new IllegalStateException("Mic seat request changed. Reopen queue.");
            }
            if(Boolean.TRUE.equals(roomFresh9970.getBoolean("closed")))
                throw new IllegalStateException("Party room is closed");
            // Atomic cleanup includes any stale seat(s) from another phone.
            for(int i9970=0;i9970<seats9970.size();i9970++)
                if(i9970!=target9913-1&&seats9970.get(i9970).exists()
                    &&uid9913.equals(seats9970.get(i9970).getString("uid")))
                    tx9913.delete(refs9970.get(i9970));
            if(!targetDoc9913.exists()){
                Map<String,Object> seat9970=new HashMap<>();
                seat9970.put("uid",uid9913);
                seat9970.put("name",name9913==null||name9913.trim().isEmpty()?"Member":name9913);
                seat9970.put("micOn",false);
                seat9970.put("joinedAt",FieldValue.serverTimestamp());
                tx9913.set(seat9913,seat9970);
            }
            if(request9913!=null)tx9913.delete(request9913);
            return null;
        }).addOnSuccessListener(v9913->{
            if(!isActivePresence967(roomId9913,actor9913,generation9913))return;
            addEvent("seat",(name9913==null?"Member":name9913)+" moved to Seat "+target9913);
            toast(action9913+" • Seat "+target9913);
        }).addOnFailureListener(err9913->{
            if(isActivePresence967(roomId9913,actor9913,generation9913))
                toast("Seat unchanged: "+msg(err9913));
        });
    }

    private void approveSeatRequest(String requestId,String uid,String name,int no){
        if(db==null||roomId==null)return;
        DocumentReference request9913=db.collection("live_rooms").document(roomId)
            .collection("seat_requests").document(requestId);
        approveOrMoveMemberSeat9913(uid,name,-1,no,request9913,"Mic approved");
    }

    private void cloudSeatAction(int no) {
        if(!cloudRoom||!memberSeen||user==null||db==null||roomId==null)return;
        if(no<1||no>maxSeats){toast("Invalid seat");return;}
        final String expectedRoom9912=roomId, expectedUid9912=user.getUid();
        final int expectedGeneration9912=presenceGeneration967, oldSeat9912=mySeat;
        final DocumentReference room9912=db.collection("live_rooms").document(expectedRoom9912);
        final DocumentReference target9912=room9912.collection("seats").document(String.valueOf(no));
        db.runTransaction(tx9912->{
            DocumentSnapshot roomFresh9970=tx9912.get(room9912);
            DocumentSnapshot memberFresh9970=tx9912.get(
                room9912.collection("members").document(expectedUid9912));
            if(!roomFresh9970.exists()||!memberFresh9970.exists()
                ||!expectedUid9912.equals(memberFresh9970.getString("uid")))
                throw new IllegalStateException("Party membership changed");
            Long capacity9970=roomFresh9970.getLong("maxSeats");
            int max9970=capacity9970==null?8:capacity9970.intValue();
            if(!KingPartyLiveSeats9970.allowed(max9970,no))
                throw new IllegalStateException("Mic seat not available in current layout");
            java.util.ArrayList<DocumentReference> refs9970=new java.util.ArrayList<>();
            java.util.ArrayList<DocumentSnapshot> seats9970=new java.util.ArrayList<>();
            for(int number9970=1;number9970<=12;number9970++){
                DocumentReference ref9970=room9912.collection("seats")
                    .document(String.valueOf(number9970));
                refs9970.add(ref9970);seats9970.add(tx9912.get(ref9970));
            }
            DocumentSnapshot targetDoc9912=seats9970.get(no-1);
            String occupant9912=targetDoc9912.exists()?targetDoc9912.getString("uid"):null;
            if(occupant9912!=null&&!expectedUid9912.equals(occupant9912))
                throw new IllegalStateException("Seat already occupied by another member");
            // Remove every other seat owned by this user, even if the local
            // listener missed a change made by their other Android phone.
            for(int i9970=0;i9970<seats9970.size();i9970++)
                if(i9970!=no-1&&seats9970.get(i9970).exists()
                   &&expectedUid9912.equals(seats9970.get(i9970).getString("uid")))
                    tx9912.delete(refs9970.get(i9970));
            if(targetDoc9912.exists())tx9912.delete(target9912);
            else{
                Map<String,Object> seat9912=new HashMap<>();
                seat9912.put("uid",expectedUid9912);seat9912.put("name",safeName());
                seat9912.put("micOn",false);
                seat9912.put("joinedAt",FieldValue.serverTimestamp());
                tx9912.set(target9912,seat9912);
            }
            return targetDoc9912.exists()?"leave":"take";
        }).addOnSuccessListener(action9912->{
            if(!isActivePresence967(expectedRoom9912,expectedUid9912,expectedGeneration9912))return;
            addEvent("seat",safeName()+("leave".equals(action9912)?" left mic seat":" took seat "+no));
        }).addOnFailureListener(e->{
            if(isActivePresence967(expectedRoom9912,expectedUid9912,expectedGeneration9912))
                toast("Seat was not changed: "+msg(e));
        });
    }

        private boolean canModerateSeatsV530() {
        return isOwner() || roomModeratorV530;
    }

    private void searchPartyRoomsV530() {
        if (db == null || user == null) { toast("Sign in to search live Party rooms"); return; }
        final EditText inputV530 = new EditText(this); inputV530.setHint("Search room name"); inputV530.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("Search Party rooms").setView(inputV530).setNegativeButton("Cancel",null)
            .setPositiveButton("Search",(d,w) -> {
                final String qV530 = inputV530.getText().toString().trim().toLowerCase();
                if (qV530.isEmpty()) return;
                db.collection("live_rooms").limit(60).get().addOnSuccessListener(snap -> {
                    final List<DocumentSnapshot> hitsV530 = new ArrayList<>();
                    final List<String> labelsV530 = new ArrayList<>();
                    for (DocumentSnapshot docV530 : snap.getDocuments()) {
                        if (Boolean.TRUE.equals(docV530.getBoolean("closed"))) continue;
                        String nameV530 = str(docV530,"name","Live Party");
                        String codeV530=str(docV530,"joinCode",shortId(docV530.getId()));
                        if (nameV530.toLowerCase().contains(qV530) || codeV530.toLowerCase().contains(qV530) || shortId(docV530.getId()).toLowerCase().contains(qV530) || docV530.getId().toLowerCase().contains(qV530)) {
                            hitsV530.add(docV530);
                            labelsV530.add(nameV530 + "  •  " + str(docV530,"ownerName","Host") + "  •  " + codeV530);
                        }
                    }
                    if (hitsV530.isEmpty()) { toast("No matching live rooms"); return; }
                    new AlertDialog.Builder(this).setTitle("Live rooms").setItems(labelsV530.toArray(new String[0]),(x,which) -> {
                        DocumentSnapshot docV530 = hitsV530.get(which);
                        openCloudRoom(docV530.getId(),str(docV530,"name","Live Party"),docV530.getString("ownerUid"),str(docV530,"ownerName","Host"),Boolean.TRUE.equals(docV530.getBoolean("isPrivate")),Boolean.TRUE.equals(docV530.getBoolean("hasPassword")));
                    }).setNegativeButton("Close",null).show();
                }).addOnFailureListener(e -> toast("Search failed: " + msg(e)));
            }).show();
    }

    private String[] emojiSlice993(int from,int to){
        int[] ids=KingEmojiAccess993.range(from,to);
        String[] out=new String[ids.length];
        for(int i=0;i<ids.length;i++)out[i]=LiveEmojiView.token(ids[i]);
        return out;
    }
    private void showLiveEmojiPanelV530() {
        final LinearLayout sheet=new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setBackgroundColor(0xff171326);
        LinearLayout bar=new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);bar.setPadding(dp(12),0,dp(8),0);
        TextView title=tv("KING LIVE • 120 ACTION EMOJI",16,Color.WHITE,true);
        bar.addView(title,new LinearLayout.LayoutParams(0,dp(48),1));
        TextView close=tv("×",26,Color.WHITE,true);
        close.setGravity(Gravity.CENTER);
        bar.addView(close,new LinearLayout.LayoutParams(dp(48),dp(48)));
        sheet.addView(bar);
        TextView subtitle=tv("50 classic • 50 action • 10 level • 10 VIP preview",
            11,0xffffdc95,true);
        subtitle.setPadding(dp(12),0,dp(8),dp(5));
        sheet.addView(subtitle,new LinearLayout.LayoutParams(-1,dp(25)));
        HorizontalScrollView strip=new HorizontalScrollView(this);
        strip.setHorizontalScrollBarEnabled(false);
        LinearLayout tabs=new LinearLayout(this);
        strip.addView(tabs);
        sheet.addView(strip,new LinearLayout.LayoutParams(-1,dp(54)));
        ScrollView scroll=new ScrollView(this);
        LinearLayout grid=new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        grid.setPadding(dp(7),dp(5),dp(7),dp(7));
        scroll.addView(grid);sheet.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        final String[] groups={"Action 25","Party 25","Classic A","Classic B",
            "Level 10","VIP 10","Stickers","Saved"};
        final AlertDialog dialog=new AlertDialog.Builder(this).setView(sheet).create();
        close.setOnClickListener(v->dialog.dismiss());
        for(int i=0;i<groups.length;i++){
            final int group=i;
            TextView tab=tv(groups[i],12,Color.WHITE,true);
            tab.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(92),dp(44));
            lp.setMargins(dp(2),dp(2),dp(2),dp(2));tabs.addView(tab,lp);
            tab.setOnClickListener(v->{
                String[] pack;
                if(group==0)pack=emojiSlice993(50,75);
                else if(group==1)pack=emojiSlice993(75,100);
                else if(group==2)pack=emojiSlice993(0,25);
                else if(group==3)pack=emojiSlice993(25,50);
                else if(group==4)pack=emojiSlice993(100,110);
                else if(group==5)pack=emojiSlice993(110,120);
                else if(group==6)pack=stickerPack550(0,67);
                else pack=favouriteEmojiPack610();
                fillEmojiGrid610(grid,pack,group==6?6:5,group<6);
                for(int n=0;n<tabs.getChildCount();n++)
                    tabs.getChildAt(n).setBackground(
                        bg(n==group?0xfff5b83e:0xff383047,14));
            });
        }
        tabs.getChildAt(0).performClick();
        dialog.setOnShowListener(v->{
            android.view.Window w=dialog.getWindow();
            if(w!=null){
                w.setBackgroundDrawableResource(android.R.color.transparent);
                w.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                w.setGravity(Gravity.BOTTOM);
                int h=Math.min(dp(580),Math.max(dp(390),
                    (int)(getResources().getDisplayMetrics().heightPixels*.64f)));
                w.setLayout(-1,h);
            }
        });
        dialog.show();
    }
    private void previewLockedAction993(int id){
        if(id<0||id>=LiveEmojiView.LABELS.length)return;
        KingEmojiAuraView992 preview=new KingEmojiAuraView992(this,id,3500);
        preview.setBackground(bg(0xff1a1431,16));
        new AlertDialog.Builder(this)
            .setTitle("✨ "+LiveEmojiView.LABELS[id])
            .setMessage(KingEmojiAccess993.lockMessage(id)+
                "\nLOCAL preview only. VIP payments/unlocks are not active.")
            .setView(preview)
            .setPositiveButton("Close",null).show();
    }
    private void fillEmojiGrid610(LinearLayout grid,String[] emojis,int columns,boolean livePack){
        grid.removeAllViews();
        if(emojis==null||emojis.length==0){TextView empty=tv("No favourites yet\nLong-press any Face or Expression to add it here.",14,0xff777777,false);empty.setGravity(Gravity.CENTER);grid.addView(empty,new LinearLayout.LayoutParams(-1,dp(150)));return;}
        final int rowHeight=columns>=6?dp(64):dp(72);
        final int artSize=columns>=6?dp(46):dp(52);
        LinearLayout row=null;
        for(int i=0;i<emojis.length;i++){
            if(i%columns==0){row=new LinearLayout(this);row.setGravity(Gravity.CENTER);grid.addView(row,new LinearLayout.LayoutParams(-1,rowHeight));}
            final String token=emojis[i];int live=LiveEmojiView.parse(token);View art;
            if(ReferenceEmojiView.parse(token)>=0){art=new ReferenceEmojiView(this,ReferenceEmojiView.parse(token),false);}
            else if(stickerResource550(token)!=0){android.widget.ImageView im=new android.widget.ImageView(this);im.setImageResource(stickerResource550(token));im.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);im.setAdjustViewBounds(false);im.setContentDescription("Sticker "+token);art=im;}
            else if(live>=0){art=new LiveEmojiView(this,live,-1);art.setContentDescription("Preview "+LiveEmojiView.LABELS[live]+"; tap to animate in the room");}
            else{TextView text=tv(token,columns>=6?30:33,0xff202020,false);text.setGravity(Gravity.CENTER);text.setPadding(0,0,0,0);text.setContentDescription("Send "+token);art=text;}
            FrameLayout cell=new FrameLayout(this);cell.setForegroundGravity(Gravity.CENTER);cell.setPadding(dp(2),dp(2),dp(2),dp(2));
            FrameLayout.LayoutParams ap=new FrameLayout.LayoutParams(artSize,artSize,Gravity.CENTER);cell.addView(art,ap);
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,rowHeight,1);cp.setMargins(dp(1),dp(1),dp(1),dp(1));row.addView(cell,cp);
            final int emojiId993=live;
            final boolean locked993=emojiId993>=0 &&
                !KingEmojiAccess993.canSend(emojiId993,LevelSystem.read(this).level,0);
            if(locked993){
                String badge=KingEmojiAccess993.requiredVip(emojiId993)>0
                    ?"VIP "+KingEmojiAccess993.requiredVip(emojiId993)
                    :"LV "+KingEmojiAccess993.requiredLevel(emojiId993);
                TextView lock=tv("🔒 "+badge,10,0xffffd889,true);
                lock.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
                cell.addView(lock,new FrameLayout.LayoutParams(-1,dp(20),Gravity.BOTTOM));
            }
            cell.setOnClickListener(v->{
                if(locked993){previewLockedAction993(emojiId993);return;}
                sendLiveEmojiV530(token);
            });
            cell.setOnLongClickListener(v->{toggleFavouriteEmoji610(token);return true;});
        }
        if(row!=null){int rem=emojis.length%columns;if(rem>0)for(int i=rem;i<columns;i++)row.addView(new View(this),new LinearLayout.LayoutParams(0,rowHeight,1));}
    }

    private String[] favouriteEmojiPack610(){
        java.util.Set<String> set=prefs.getStringSet("favorite_emojis_610",null);
        if(set==null||set.isEmpty())return new String[]{"😂","😍","🥰","😎","🥳","🤩","😭","😡","🤗","🤔","😘","😴"};
        java.util.ArrayList<String> list=new java.util.ArrayList<>(set);java.util.Collections.sort(list);return list.toArray(new String[0]);
    }

    private void toggleFavouriteEmoji610(String emoji){
        java.util.Set<String> current=new java.util.LinkedHashSet<>(prefs.getStringSet("favorite_emojis_610",new java.util.LinkedHashSet<>()));
        boolean added;if(current.contains(emoji)){current.remove(emoji);added=false;}else{current.add(emoji);added=true;}
        prefs.edit().putStringSet("favorite_emojis_610",current).apply();toast(added?"Added to Favourites":"Removed from Favourites");
    }

    private String[] stickerPack550(int start,int end){String[] out=new String[Math.max(0,end-start)];for(int i=0;i<out.length;i++)out[i]=String.format(java.util.Locale.US,"[sticker:%02d]",start+i);return out;}
    private int stickerResource550(String text){if(text==null||!text.matches("\\[sticker:[0-9]{2}\\]"))return 0;int index=Integer.parseInt(text.substring(9,11));if(index>66)return 0;return getResources().getIdentifier(String.format(java.util.Locale.US,"kp_emoji_%02d",index),"drawable",getPackageName());}
    private String stickerFallback610(String text){return text;}
    private android.widget.ImageView stickerImage852(int resource){android.widget.ImageView im=new android.widget.ImageView(this);im.setImageResource(resource);im.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);im.setContentDescription("Room sticker");return im;}

    private void sendLiveEmojiV530(String emoji){
        int id993=LiveEmojiView.parse(emoji);
        if(id993>=0&&!KingEmojiAccess993.canSend(id993,LevelSystem.read(this).level,0)){
            toast(KingEmojiAccess993.lockMessage(id993));return;
        }
        String visual=stickerFallback610(emoji);
        if(!cloudRoom){
            showLiveEmojiEffect560(visual,displayName,user==null?null:user.getUid());
            appendLocalChat(displayName,visual); // clearly local/test effect.
            return;
        }
        if(user==null||db==null||roomId==null||!KingGiftSafety971.validLiveEmoji(
                visual,memberSeen)){
            toast("Join a live KING Plus Party to send animated emoji");return;
        }
        long now971=android.os.SystemClock.elapsedRealtime();
        if(now971-lastLiveEmojiSend971<350L){toast("Emoji is sending • try again shortly");return;}
        lastLiveEmojiSend971=now971;
        final String expectedRoom971=roomId,expectedUid971=user.getUid();
        final int expectedGeneration971=presenceGeneration967;
        Map<String,Object> ev971=new HashMap<>();
        ev971.put("actorUid",expectedUid971);
        ev971.put("actorName",safeName());
        ev971.put("type","live_emoji");
        ev971.put("text",visual);
        ev971.put("emoji",visual);
        ev971.put("createdAt",FieldValue.serverTimestamp());
        Map<String,Object> msg971=new HashMap<>();
        msg971.put("senderUid",expectedUid971);
        msg971.put("senderName",safeName());
        msg971.put("text",visual);
        msg971.put("createdAt",FieldValue.serverTimestamp());
        DocumentReference room971=db.collection("live_rooms").document(expectedRoom971);
        WriteBatch batch971=db.batch();
        batch971.set(room971.collection("messages").document(),msg971);
        batch971.set(room971.collection("events").document(),ev971);
        batch971.commit().addOnSuccessListener(v->{
            if(isActivePresence967(expectedRoom971,expectedUid971,expectedGeneration971))
                showLiveEmojiEffect560(visual,safeName(),expectedUid971);
        }).addOnFailureListener(error->{
            if(isActivePresence967(expectedRoom971,expectedUid971,expectedGeneration971))
                toast("Emoji was not sent to Party: "+msg(error));
        });
    }

    private void showLiveEmojiEffectV530(String emoji,String sender){showLiveEmojiEffect560(emoji,sender,null);}
    private void showLiveEmojiEffect560(String emoji,String sender,String uid){
        if(!partyVisualForeground995||emoji==null||emoji.trim().isEmpty()||isFinishing()||isDestroyed())return;
        emoji=stickerFallback610(emoji);
        if(!cloudRoom)addChatRow(sender==null?"User":sender,emoji);
        final FrameLayout stage=liveEmojiStageV530;if(stage==null||stage.getParent()==null)return;
        final String key=uid!=null?uid:(sender==null?"local":sender);
        View previous=activeReactions560.remove(key);if(previous!=null){previous.animate().cancel();stage.removeView(previous);}
        int live=LiveEmojiView.parse(emoji);final View reaction;
        if(ReferenceEmojiView.parse(emoji)>=0)reaction=new ReferenceEmojiView(this,ReferenceEmojiView.parse(emoji),false);
        else if(stickerResource550(emoji)!=0)reaction=stickerImage852(stickerResource550(emoji));
        else if(live>=0)reaction=new KingEmojiAuraView992(this,live,4000);
        else{TextView text=tv(emoji,42,Color.WHITE,false);text.setGravity(Gravity.CENTER);reaction=text;}
        activeReactions560.put(key,reaction);
        // Cap simultaneous visual effects. A room of many members can otherwise
        // accumulate full-screen animated views, increasing CPU/GPU/native memory.
        if(KingPartyMemory969.overEffectLimit(activeReactions560.size())){
            for(Map.Entry<String,View> effect969:new HashMap<>(activeReactions560).entrySet()){
                if(!KingPartyMemory969.overEffectLimit(activeReactions560.size()))break;
                if(effect969.getValue()==reaction)continue;
                View old969=activeReactions560.remove(effect969.getKey());
                if(old969!=null){
                    old969.animate().cancel();
                    if(old969.getParent() instanceof android.view.ViewGroup)
                        ((android.view.ViewGroup)old969.getParent()).removeView(old969);
                }
            }
        }
        FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(dp(82),dp(82));lp.gravity=Gravity.TOP|Gravity.LEFT;lp.leftMargin=Math.max(0,(stage.getWidth()-dp(82))/2);lp.topMargin=dp(102);
        for(Map.Entry<Integer,View> entry:seatViews560.entrySet()){
            boolean match=uid!=null?uid.equals(seatUids.get(entry.getKey())):entry.getKey()==mySeat;
            if(match){int[] seatXY=new int[2],stageXY=new int[2];entry.getValue().getLocationOnScreen(seatXY);stage.getLocationOnScreen(stageXY);lp.leftMargin=Math.max(0,seatXY[0]-stageXY[0]+(entry.getValue().getWidth()-dp(82))/2);lp.topMargin=Math.max(0,seatXY[1]-stageXY[1]-dp(7));break;}
        }
        if(ReferenceEmojiView.parse(emoji)>=3 || live>=12){lp.width=-1;lp.height=-1;lp.leftMargin=0;lp.topMargin=0;}
        stage.addView(reaction,lp);reaction.setScaleX(.6f);reaction.setScaleY(.6f);reaction.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
        reaction.postDelayed(()->{reaction.animate().alpha(0f).setDuration(250).withEndAction(()->{stage.removeView(reaction);if(activeReactions560.get(key)==reaction)activeReactions560.remove(key);}).start();},3750);
    }

    private void showEmptySeatMenuV530(int seatNoV530) {
        new AlertDialog.Builder(this).setTitle("Seat "+seatNoV530).setItems(new String[]{"👑 Take this seat","📨 Invite member to this seat"},(d,w) -> {
            if(w==0) cloudSeatAction(seatNoV530); else inviteMemberToSeatV530(seatNoV530);
        }).setNegativeButton("Cancel",null).show();
    }

    private void showOccupiedSeatMenuV530(int seatNoV530) {
        final String uidV530=seatUids.get(seatNoV530);
        if(uidV530==null||uidV530.trim().isEmpty()){showEmptySeatMenuV530(seatNoV530);return;}
        final String nameV530=seatNames.get(seatNoV530)==null?"Member":seatNames.get(seatNoV530);
        final boolean micV530=!Boolean.FALSE.equals(seatMics.get(seatNoV530));
        final boolean protectV9912=uidV530.equals(ownerUid)||(user!=null&&uidV530.equals(user.getUid()));
        final boolean canAct10012=canModerateSeatsV530();
        final String[] actionsV530=protectV9912||!canAct10012
            ?new String[]{"👤 View real profile",micV530?"🔇 Mute seat":"🎤 Only seat owner can enable Mic"}
            :new String[]{"👤 View real profile",micV530?"🔇 Mute seat":"🎤 Only seat owner can enable Mic","⤵ Remove from seat","🚪 Kick from room"};
        new AlertDialog.Builder(this).setTitle(nameV530+" • Seat "+seatNoV530).setItems(actionsV530,(d,w)->{
            if(w==0){memberProfileDialog(uidV530,nameV530);return;}
            if(w==1){
                if(micV530)setSeatMicV530(seatNoV530,false,uidV530);
                else if(user!=null&&uidV530.equals(user.getUid()))toggleMic();
                else toast("Only that member can turn ON their own Mic");
                return;
            }
            if(protectV9912||!canModerateSeatsV530()){toast("Host / co-host permission required");return;}
            if(w==2)kickSeatV530(seatNoV530,false,uidV530);
            else confirmKickRoomV530(seatNoV530,uidV530,nameV530);
        }).setNegativeButton("Cancel",null).show();
    }

    private void setSeatMicV530(int seatNoV530,boolean onV530,String expectedUid10014){
        if(!canModerateSeatsV530()||db==null||roomId==null){toast("Host / co-host only");return;}
        if(onV530){toast("Only member may enable own Mic");return;}
        if(expectedUid10014==null||!expectedUid10014.equals(seatUids.get(seatNoV530))){
            toast("Seat changed; reopen its menu");return;
        }
        final String boundRoom10014=roomId;
        DocumentReference seatRef10014=db.collection("live_rooms").document(boundRoom10014)
            .collection("seats").document(String.valueOf(seatNoV530));
        db.runTransaction(tx10014->{
            DocumentSnapshot liveSeat10014=tx10014.get(seatRef10014);
            if(!liveSeat10014.exists()||!expectedUid10014.equals(liveSeat10014.getString("uid")))
                throw new IllegalStateException("Seat changed; reopen its menu");
            if(!Boolean.TRUE.equals(liveSeat10014.getBoolean("micOn")))return false;
            tx10014.update(seatRef10014,"micOn",false);
            return true;
        }).addOnSuccessListener(changed->{
            if(boundRoom10014.equals(roomId)&&Boolean.TRUE.equals(changed))
                addEvent("seat_mic","Host muted seat "+seatNoV530);
        }).addOnFailureListener(error->{
            if(boundRoom10014.equals(roomId))toast("Seat mute failed: "+msg(error));
        });
    }

    private void kickSeatV530(int seatNoV530,boolean silentV530){
        kickSeatV530(seatNoV530,silentV530,seatUids.get(seatNoV530));
    }
    private void kickSeatV530(int seatNoV530,boolean silentV530,String expectedUid10012){
        if(!canModerateSeatsV530()||db==null||roomId==null){toast("Host / co-host only");return;}
        if(expectedUid10012==null||!expectedUid10012.equals(seatUids.get(seatNoV530))){
            toast("Seat changed; reopen its menu");return;
        }
        if(expectedUid10012==null||expectedUid10012.isEmpty()){toast("Seat is already empty");return;}
        final String currentRoom10012=roomId;
        final String name10012=seatNames.get(seatNoV530)==null?"Member":seatNames.get(seatNoV530);
        final DocumentReference seat10012=db.collection("live_rooms").document(currentRoom10012)
            .collection("seats").document(String.valueOf(seatNoV530));
        db.runTransaction(tx->{
            DocumentSnapshot current10012=tx.get(seat10012);
            if(!current10012.exists()||!expectedUid10012.equals(current10012.getString("uid")))
                throw new IllegalStateException("Seat changed; refresh room before removing");
            tx.delete(seat10012);return null;
        }).addOnSuccessListener(result->{
            if(!currentRoom10012.equals(roomId))return;
            if(!silentV530)addEvent("seat_kick",name10012+" was removed from seat "+seatNoV530);
        }).addOnFailureListener(error->{
            if(currentRoom10012.equals(roomId))toast("Seat removal failed: "+msg(error));
        });
    }

    private void confirmKickRoomV530(int seatNoV530, String uidV530, String nameV530) {
        new AlertDialog.Builder(this).setTitle("Kick "+nameV530+" from room?")
            .setMessage("This removes the member from all seats and the room. It does not ban them.")
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Kick",(d,w)->kickFromRoomV530(seatNoV530,uidV530,nameV530)).show();
    }

    private void kickFromRoomV530(int seatNoV530, String uidV530, String nameV530) {
        // The old direct batch cleared only one seat and could leave a co-host role.
        // All Party kick entry points now use the same atomic moderator transaction.
        moderateMember10011(uidV530,nameV530,"Kick from mic seat "+seatNoV530,false);
    }

    private void inviteMemberToSeatV530(int seatNoV530) {
        if(!canModerateSeatsV530() || db==null || user==null || roomId==null || !cloudRoom){
            toast("Host / co-host only");return;
        }
        final String inviteRoom10015=roomId,inviter10015=user.getUid();
        final DocumentReference roomRef10015=db.collection("live_rooms").document(inviteRoom10015);
        roomRef10015.collection("members").get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(snapV530 -> {
                if(!inviteContextCurrent10015(inviteRoom10015,inviter10015))return;
                final List<String> uidsV530=new ArrayList<>();final List<String> namesV530=new ArrayList<>();
                for(DocumentSnapshot docV530:snapV530.getDocuments()){
                    String uidV530=docV530.getString("uid");if(uidV530==null)uidV530=docV530.getId();
                    if(uidV530.equals(inviter10015)||seatUids.containsValue(uidV530))continue;
                    uidsV530.add(uidV530);namesV530.add(str(docV530,"name","Member"));
                }
                if(uidsV530.isEmpty()){toast("No available room members to invite");return;}
                String[] labelsV530=new String[namesV530.size()];
                for(int i=0;i<labelsV530.length;i++)labelsV530[i]="👤 "+namesV530.get(i);
                new AlertDialog.Builder(this).setTitle("Invite to Seat "+seatNoV530)
                    .setItems(labelsV530,(dialog,w)->{
                        if(!inviteContextCurrent10015(inviteRoom10015,inviter10015)){
                            toast("Room or host role changed. Reopen seat menu.");return;
                        }
                        final String uidV530=uidsV530.get(w),nameV530=namesV530.get(w);
                        if(seatUids.containsValue(uidV530)){
                            toast("Member already joined a seat. Refresh room.");return;
                        }
                        // A member might leave between listing and tap; only send to
                        // a person still in this same room on the server.
                        roomRef10015.collection("members").document(uidV530)
                            .get(com.google.firebase.firestore.Source.SERVER)
                            .addOnSuccessListener(liveMember10015->{
                                if(!inviteContextCurrent10015(inviteRoom10015,inviter10015))return;
                                if(!liveMember10015.exists()||seatUids.containsValue(uidV530)){
                                    toast("Member left or took a seat. Refresh room.");return;
                                }
                                Map<String,Object> inviteV530=new HashMap<>();
                                inviteV530.put("targetUid",uidV530);inviteV530.put("targetName",nameV530);
                                inviteV530.put("seatId",String.valueOf(seatNoV530));inviteV530.put("seatNo",seatNoV530);
                                inviteV530.put("invitedByUid",inviter10015);inviteV530.put("invitedByName",safeName());
                                inviteV530.put("createdAt",FieldValue.serverTimestamp());
                                roomRef10015.collection("seat_invites").document(uidV530).set(inviteV530)
                                    .addOnSuccessListener(v->{
                                        if(inviteContextCurrent10015(inviteRoom10015,inviter10015)){
                                            toast("Invite sent to "+nameV530);
                                            addEvent("seat_invite",safeName()+" invited "+nameV530+" to seat "+seatNoV530);
                                        }
                                    }).addOnFailureListener(e->{
                                        if(inviteContextCurrent10015(inviteRoom10015,inviter10015))toast("Invite failed: "+msg(e));
                                    });
                            }).addOnFailureListener(e->{
                                if(inviteContextCurrent10015(inviteRoom10015,inviter10015))toast("Member unavailable: "+msg(e));
                            });
                    }).setNegativeButton("Cancel",null).show();
            }).addOnFailureListener(e->{
                if(inviteContextCurrent10015(inviteRoom10015,inviter10015))toast("Members unavailable: "+msg(e));
            });
    }
    private boolean inviteContextCurrent10015(String boundRoom,String inviterUid){
        return !isFinishing()&&!isDestroyed()&&cloudRoom&&db!=null
            &&user!=null&&boundRoom!=null&&boundRoom.equals(roomId)
            &&inviterUid.equals(user.getUid())&&canModerateSeatsV530();
    }

    private void attachV530RealtimeHelpers() {
        if(db==null || user==null || roomId==null) return;
        final String boundRoom9912=roomId, boundUid9912=user.getUid();
        final int boundGeneration9912=presenceGeneration967;
        DocumentReference roomV530=db.collection("live_rooms").document(boundRoom9912);
        roleListenerV530=roomV530.collection("roles").document(boundUid9912).addSnapshotListener((docV530,eV530) -> {
            if(!isActivePresence967(boundRoom9912,boundUid9912,boundGeneration9912))return;
            roomModeratorV530 = isOwner() || (eV530==null && docV530!=null && docV530.exists() && "cohost".equals(docV530.getString("role")));
        });
        seatInviteListenerV530=roomV530.collection("seat_invites").document(boundUid9912).addSnapshotListener((docV530,eV530) -> {
            if(!isActivePresence967(boundRoom9912,boundUid9912,boundGeneration9912))return;
            if(eV530!=null || docV530==null || !docV530.exists() || seatInviteDialogOpenV530) return;
            String seatIdV530=docV530.getString("seatId"); if(seatIdV530==null) return;
            Object createdV530=docV530.get("createdAt"); String keyV530=seatIdV530+"|"+String.valueOf(createdV530);
            if(keyV530.equals(lastSeatInviteKeyV530)) return; lastSeatInviteKeyV530=keyV530; seatInviteDialogOpenV530=true;
            int seatNoV530; try{seatNoV530=Integer.parseInt(seatIdV530);}catch(Exception ex){seatInviteDialogOpenV530=false;return;}
            String byV530=str(docV530,"invitedByName","Host");
            new AlertDialog.Builder(this).setTitle("Seat invite").setMessage(byV530+" invited you to Seat "+seatNoV530)
                .setNegativeButton("Decline",(d,w) -> {seatInviteDialogOpenV530=false;docV530.getReference().delete();})
                .setPositiveButton("Accept",(d,w) -> {seatInviteDialogOpenV530=false;acceptSeatInviteV530(docV530,seatNoV530);}).setOnCancelListener(d -> seatInviteDialogOpenV530=false).show();
        });
        final String activeRoomForEmoji960=roomId;
        final String activeUserForEmoji960=user.getUid();
        final int activeEmojiSession981=presenceGeneration967;
        liveEmojiListenerV530=roomV530.collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(20).addSnapshotListener((snapV530,eV530) -> {
            if(!isActivePresence967(activeRoomForEmoji960,activeUserForEmoji960,activeEmojiSession981))return;
            if(eV530!=null){toast("Live emoji sync unavailable: "+msg(eV530));return;}if(snapV530==null)return;
            java.util.HashSet<String> currentEmojiIds940=new java.util.HashSet<>();
            for(DocumentSnapshot docV530:snapV530.getDocuments()){
                currentEmojiIds940.add(docV530.getId());
                if(!"live_emoji".equals(docV530.getString("type")) || seenLiveEmojiEventsV530.contains(docV530.getId())) continue;
                seenLiveEmojiEventsV530.add(docV530.getId()); Object tsV530=docV530.get("createdAt");
                if(!(tsV530 instanceof com.google.firebase.Timestamp))continue;
                long ageV530=System.currentTimeMillis()-((com.google.firebase.Timestamp)tsV530).toDate().getTime();
                if(ageV530>12000||ageV530 < -10000||!currentVisitDocument10026(docV530))continue;
                String actorV530=str(docV530,"actorName","User"); String emojiV530=str(docV530,"emoji",str(docV530,"text","😊"));
                if(!activeUserForEmoji960.equals(docV530.getString("actorUid"))) showLiveEmojiEffect560(emojiV530,actorV530,docV530.getString("actorUid"));
            }
            if(seenLiveEmojiEventsV530.size()>80)seenLiveEmojiEventsV530.retainAll(currentEmojiIds940);
        });
    }

    private void acceptSeatInviteV530(DocumentSnapshot inviteV530, int seatNoV530) {
        if(!cloudRoom||!memberSeen||db==null||user==null||roomId==null)return;
        if(seatNoV530<1||seatNoV530>maxSeats){toast("Invalid invite seat");return;}
        final String expectedRoom9912=roomId, expectedUid9912=user.getUid();
        final int generation9912=presenceGeneration967, previousSeat9912=mySeat;
        final DocumentReference room9912=db.collection("live_rooms").document(expectedRoom9912);
        final DocumentReference seat9912=room9912.collection("seats").document(String.valueOf(seatNoV530));
        final DocumentReference invitation9912=inviteV530.getReference();
        db.runTransaction(tx9912->{
            DocumentSnapshot freshRoom9970=tx9912.get(room9912);
            DocumentSnapshot ownMember9970=tx9912.get(
                room9912.collection("members").document(expectedUid9912));
            DocumentSnapshot currentInvite9912=tx9912.get(invitation9912);
            if(!freshRoom9970.exists()||!ownMember9970.exists()
                ||!expectedUid9912.equals(ownMember9970.getString("uid")))
                throw new IllegalStateException("Party member must rejoin");
            Long capacity9970=freshRoom9970.getLong("maxSeats");
            if(!KingPartyLiveSeats9970.allowed(
                 capacity9970==null?8:capacity9970.intValue(),seatNoV530))
                throw new IllegalStateException("Invited seat was removed by Host");
            if(!currentInvite9912.exists()
               ||!expectedUid9912.equals(currentInvite9912.getString("targetUid"))
               ||!String.valueOf(seatNoV530).equals(currentInvite9912.getString("seatId")))
                throw new IllegalStateException("Seat invite expired");
            java.util.ArrayList<DocumentReference> refs9970=new java.util.ArrayList<>();
            java.util.ArrayList<DocumentSnapshot> seats9970=new java.util.ArrayList<>();
            for(int number9970=1;number9970<=12;number9970++){
                DocumentReference ref9970=room9912.collection("seats")
                    .document(String.valueOf(number9970));
                refs9970.add(ref9970);seats9970.add(tx9912.get(ref9970));
            }
            DocumentSnapshot occupied9912=seats9970.get(seatNoV530-1);
            if(occupied9912.exists()
               &&!expectedUid9912.equals(occupied9912.getString("uid")))
                throw new IllegalStateException("Invited mic seat is occupied");
            for(int i9970=0;i9970<seats9970.size();i9970++)
                if(i9970!=seatNoV530-1&&seats9970.get(i9970).exists()
                  &&expectedUid9912.equals(seats9970.get(i9970).getString("uid")))
                    tx9912.delete(refs9970.get(i9970));
            if(!occupied9912.exists()){
                Map<String,Object> seatData9912=new HashMap<>();
                seatData9912.put("uid",expectedUid9912);
                seatData9912.put("name",safeName());seatData9912.put("micOn",false);
                seatData9912.put("joinedAt",FieldValue.serverTimestamp());
                tx9912.set(seat9912,seatData9912);
            }
            tx9912.delete(invitation9912);
            return null;
        }).addOnSuccessListener(v->{
            if(!isActivePresence967(expectedRoom9912,expectedUid9912,generation9912))return;
            addEvent("seat",safeName()+" accepted a seat invite for seat "+seatNoV530);
        }).addOnFailureListener(e->{
            if(isActivePresence967(expectedRoom9912,expectedUid9912,generation9912))
                toast("Could not accept seat invite: "+msg(e));
        });
    }

        private void toggleMic() {
        if(mySeat<1){
            if(cloudRoom){requestMicSeat940();return;}
            toast("Take a Mic seat first");return;
        }
        if(muteAll&&!isModerator()){toast("Host muted all seats");return;}
        if(!cloudRoom){
            micOn=!micOn;
            prefs.edit().putBoolean("mic_"+roomId,micOn).apply();
            seatMics.put(mySeat,micOn);rebuildSeats();refreshMicControl();
            return;
        }
        if(micTogglePending968){toast("Waiting for Mic sync…");return;}
        final String expectedRoom968=roomId;
        final String expectedUid968=user==null?null:user.getUid();
        final int expectedSeat968=mySeat;
        final int expectedGeneration968=presenceGeneration967;
        final boolean desiredMic968=!micOn;
        if(!desiredMic968)revokeMicConsent9956();
        if(desiredMic968 && android.os.Build.VERSION.SDK_INT>=23 &&
                checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
                  !=android.content.pm.PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO},992);
            toast("Allow microphone access, then tap Mic again to speak.");
            return;
        }
        if(db==null||!memberSeen||!isActivePresence967(expectedRoom968,expectedUid968,expectedGeneration968)
           ||!KingPartyMic968.canToggle(expectedRoom968,expectedUid968,
               seatUids.get(expectedSeat968),expectedSeat968,mySeat,
               cloudRoom,!isFinishing()&&!isDestroyed(),muteAll,isModerator())){
            toast("Your Mic seat is not ready. Wait for the room to sync.");return;
        }
        micTogglePending968=true;
        // Privacy: on an OFF request mute native audio immediately, even if offline.
        if(!desiredMic968)setInlineAudioMuted940(true);
        final DocumentReference seatRef968=db.collection("live_rooms").document(expectedRoom968)
            .collection("seats").document(String.valueOf(expectedSeat968));
        db.runTransaction(transaction968->{
            DocumentSnapshot occupied968=transaction968.get(seatRef968);
            if(!occupied968.exists()||!expectedUid968.equals(occupied968.getString("uid")))
                throw new IllegalStateException("Mic seat is no longer assigned to this account");
            transaction968.update(seatRef968,"micOn",desiredMic968);
            return null;
        }).addOnSuccessListener(v->{
            if(!isActivePresence967(expectedRoom968,expectedUid968,expectedGeneration968))return;
            micTogglePending968=false;
            if(mySeat!=expectedSeat968)return;
            if(desiredMic968){
                localMicConsent9956=true;
                consentRoom9956=expectedRoom968;
                consentSeat9956=expectedSeat968;
            }else revokeMicConsent9956();
            micOn=desiredMic968;
            seatMics.put(expectedSeat968,desiredMic968);
            if(desiredMic968){
                roomVoiceOptedIn957=true;soundOn=true;
                if(partyShell940!=null&&!inRoomVoiceJoined940)
                    attachInRoomVoice940(partyShell940);
                if(!inRoomVoiceJoined940){
                    // A Firestore mic flag is NOT itself an audio connection.
                    micOn=false;
                    revokeMicConsent9956();
                    seatMics.put(expectedSeat968,false);
                    setVoicePresence900(false);
                    refreshMicControl();
                    db.runTransaction(revert9918->{
                        DocumentSnapshot current9918=revert9918.get(seatRef968);
                        if(current9918.exists()&&expectedUid968.equals(current9918.getString("uid")))
                            revert9918.update(seatRef968,"micOn",false);
                        return null;
                    }).addOnFailureListener(err9918->
                        android.util.Log.w("KINGPlusParty","Voice failure mic rollback failed",err9918));
                    toast("Voice unavailable • Mic OFF (retry in room)");
                    return;
                }
            }
            setInlineAudioMuted940(!desiredMic968);
            setVoicePresence900(voiceConferenceConnected10065);
            refreshMicControl();
            if(desiredMic968&&!voiceConferenceConnected10065)voiceState10065("Mic waiting • tap Voice connection");
            // Jitsi native view created. Conference connection still needs a
            // real two-phone network test before it is called LIVE.
            toast(desiredMic968?"Mic ON requested • connecting voice":"Mic OFF");
        }).addOnFailureListener(e->{
            if(!isActivePresence967(expectedRoom968,expectedUid968,expectedGeneration968))return;
            micTogglePending968=false;
            if(!desiredMic968)revokeMicConsent9956();
            refreshMicControl();
            toast("Mic sync failed: "+msg(e));
        });
    }
    /**
     * Privacy guard: turn OFF this account's current live mic only if Firestore
     * still says the same UID owns that seat. Local UI numbers may be stale
     * after a multi-phone room switch or moderator's seat reassignment.
     */
    private void muteOwnedSeat9920(String cause9920){
        if(db==null||user==null||!cloudRoom||roomId==null||mySeat<1)return;
        final String uid9920=user.getUid();
        final String room9920=roomId;
        final int seat9920=mySeat;
        final DocumentReference ref9920=db.collection("live_rooms").document(room9920)
            .collection("seats").document(String.valueOf(seat9920));
        db.runTransaction(tx9920->{
            DocumentSnapshot doc9920=tx9920.get(ref9920);
            if(doc9920.exists()&&uid9920.equals(doc9920.getString("uid"))
                &&Boolean.TRUE.equals(doc9920.getBoolean("micOn")))
                tx9920.update(ref9920,"micOn",false);
            return null;
        }).addOnFailureListener(error9920->
            KingStability.nonFatal(this,"mic-owner-guard-9920:"+cause9920,error9920));
    }

    private void refreshMicControl(){
        if(micLabel==null)return;
        boolean live10065=micOn&&voiceConferenceConnected10065;
        micLabel.setText("");android.graphics.drawable.Drawable micArt=getDrawable(live10065?R.drawable.ref_ico_bottom_mic_on_black:R.drawable.ref_ico_bottom_mic_off_black);micArt.setBounds(0,0,dp(24),dp(24));micLabel.setCompoundDrawables(null,micArt,null,null);
        micLabel.setSingleLine(true);
        micLabel.setTextSize(21);
        micLabel.setPadding(0,dp(10),0,0);
        micLabel.setTextColor(live10065?0xff008e6b:0xff666666);
        micLabel.setBackground(bg(live10065?0xffd8f5e9:0xfff0edf5,22));
        micLabel.setContentDescription(micOn&&!voiceConferenceConnected10065?"Microphone waiting for voice connection":live10065?"Mic is on in Party room. Tap to turn off":"Mic is off. Tap to turn on without leaving Party room");
    }
    private void toggleSound(){
        soundOn=!soundOn;roomVoiceOptedIn957=soundOn;
        if(!soundOn){micOn=false;revokeMicConsent9956();if(mySeat>0)muteOwnedSeat9920("sound-off");stopInRoomVoice940(true);refreshMicControl();voiceState10065("Voice off • tap to connect");toast("Voice and microphone off");}
        else if(cloudRoom&&memberSeen&&partyShell940!=null){attachInRoomVoice940(partyShell940);voiceState10065("Voice connecting…");}
    }
    private String musicPrefsKey(){return "music_playlist_"+(user==null?"local":user.getUid());}
    private void loadMusicPlaylist(){
        musicUris.clear();musicNames.clear();musicRemoteUrls.clear();musicCloudIds.clear();
        String raw=prefs==null?"":prefs.getString(musicPrefsKey(),"");
        if(raw==null||raw.isEmpty())return;
        for(String row:raw.split("\\n")){
            if(row==null||row.isEmpty())continue;String[] parts=row.split("\\t",-1);if(parts.length<2)continue;
            try{
                String u=Uri.decode(parts[0]);String n=Uri.decode(parts[1]);String remote=parts.length>2?Uri.decode(parts[2]):"";
                if(!u.isEmpty()){musicUris.add(u);musicNames.add(n.isEmpty()?"Unknown song":n);musicRemoteUrls.add(remote==null?"":remote);musicCloudIds.add("");}
            }catch(Exception ignored){}
        }
    }
    private void saveMusicPlaylist(){
        if(prefs==null)return;StringBuilder out=new StringBuilder();
        while(musicRemoteUrls.size()<musicUris.size())musicRemoteUrls.add("");
        for(int i=0;i<musicUris.size();i++){
            if(i>0)out.append('\n');
            out.append(Uri.encode(musicUris.get(i))).append('\t').append(Uri.encode(musicNames.get(i))).append('\t').append(Uri.encode(musicRemoteUrls.get(i)==null?"":musicRemoteUrls.get(i)));
        }
        prefs.edit().putString(musicPrefsKey(),out.toString()).apply();
    }

    private void openRequestedPanel940(){
        if(!memberSeen||!cloudRoom||roomId==null||requestedPanel940==null||requestedPanel940.trim().isEmpty()||isFinishing()||isDestroyed())return;
        String panel=requestedPanel940;requestedPanel940="";
        if("music".equals(panel))musicPanel();
        else if("song".equals(panel))karaokeDialog();
        else if("ktv".equals(panel))ktvQueuePanel();
        else if("radio".equals(panel))radioMicPanel();
        else if("theme".equals(panel))themeRoomPanel();
        else if("seats".equals(panel))roomSeatPanel();
        else if("games".equals(panel))openRoomGames740();
        else if("billboard".equals(panel))roomBillboardDialog();
        else if("admins".equals(panel))administratorDialog();
        else if("banned".equals(panel))banListDialog();
        else if("rename".equals(panel))renameRoomDialog();
        else if("category".equals(panel))changeCategoryDialog();
        else if("privacy".equals(panel))roomSettingsPanel();
        else if("data".equals(panel))partyDataPanel();
        else if("atmosphere".equals(panel))atmospherePanel();
        else if("host".equals(panel))hostProfileDialog();
        else if("channel".equals(panel))channelPanel();
        else if("ranking".equals(panel))roomRankingDialog();
        else if("broadcast".equals(panel))friendBroadcast610();
    }

    private void musicPanel(){
        final AlertDialog[] holder=new AlertDialog[1];
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(0,dp(8),0,dp(10));root.setBackgroundColor(0xff18a88b);
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(12),0,dp(10),0);
        TextView back=tv("‹",38,Color.WHITE,true);back.setGravity(Gravity.CENTER);head.addView(back,new LinearLayout.LayoutParams(dp(54),dp(58)));
        TextView title=tv("Playlist ("+musicNames.size()+")",21,Color.WHITE,true);title.setGravity(Gravity.CENTER_VERTICAL);head.addView(title,new LinearLayout.LayoutParams(0,dp(58),1));
        final boolean[] editMode={false};
        TextView edit=tv("Edit",16,Color.WHITE,true);edit.setGravity(Gravity.CENTER);head.addView(edit,new LinearLayout.LayoutParams(dp(64),dp(58)));root.addView(head,new LinearLayout.LayoutParams(-1,dp(62)));

        LinearLayout searchBar=new LinearLayout(this);searchBar.setGravity(Gravity.CENTER_VERTICAL);searchBar.setPadding(dp(14),dp(4),dp(14),dp(8));searchBar.setBackgroundColor(0x22111111);
        EditText search=new EditText(this);search.setSingleLine(true);search.setHint("Search for songs");search.setHintTextColor(0x99ffffff);search.setTextColor(Color.WHITE);search.setBackgroundColor(Color.TRANSPARENT);searchBar.addView(search,new LinearLayout.LayoutParams(0,dp(54),1));
        TextView go=pill("⌕",0x22000000,null);go.setTextSize(24);searchBar.addView(go,new LinearLayout.LayoutParams(dp(56),dp(46)));root.addView(searchBar,new LinearLayout.LayoutParams(-1,dp(66)));

        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);scroll.addView(list);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        TextView add=pill("ADD MUSIC",0xfff7f7f7,this::openMusicPicker);add.setTextColor(0xff46a991);add.setTextSize(16);LinearLayout.LayoutParams alp=new LinearLayout.LayoutParams(-1,dp(58));alp.setMargins(dp(42),dp(10),dp(42),0);root.addView(add,alp);

        Runnable refresh=()->{title.setText("Playlist ("+musicNames.size()+")");renderMusicPlaylistRows(list,search.getText().toString(),editMode[0],holder[0]);};
        go.setOnClickListener(v->refresh.run());
        edit.setOnClickListener(v->{editMode[0]=!editMode[0];edit.setText(editMode[0]?"Done":"Edit");refresh.run();});
        AlertDialog dialog=new AlertDialog.Builder(this).setView(root).create();holder[0]=dialog;back.setOnClickListener(v->dialog.dismiss());
        dialog.setOnShowListener(v->{android.view.Window w=dialog.getWindow();if(w!=null){w.setLayout(-1,-1);w.setGravity(Gravity.BOTTOM);w.setBackgroundDrawable(bg(0xff18a88b,0));}});
        refresh.run();dialog.show();
    }
    private void renderMusicPlaylistRows(LinearLayout list,String query,boolean editMode,AlertDialog dialog){
        list.removeAllViews();String q=query==null?"":query.trim().toLowerCase(java.util.Locale.ROOT);int shown=0;
        for(int i=0;i<musicNames.size();i++){
            String name=musicNames.get(i);if(!q.isEmpty()&&!name.toLowerCase(java.util.Locale.ROOT).contains(q))continue;shown++;final int index=i;
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(28),dp(10),dp(18),dp(10));row.setBackgroundColor(0x10000000);
            LinearLayout text=new LinearLayout(this);text.setOrientation(LinearLayout.VERTICAL);TextView n=tv(name,17,Color.WHITE,true);n.setSingleLine(true);text.addView(n,new LinearLayout.LayoutParams(-1,dp(34)));TextView sub=tv("<unknown>",12,0x99ffffff,true);text.addView(sub,new LinearLayout.LayoutParams(-1,dp(26)));row.addView(text,new LinearLayout.LayoutParams(0,dp(68),1));
            if(editMode){TextView del=pill("✕",0x33ffffff,()->{removeMusicAt(index);renderMusicPlaylistRows(list,query,true,dialog);});row.addView(del,new LinearLayout.LayoutParams(dp(46),dp(42)));}
            row.setOnClickListener(v->{if(editMode)return;playSong(index);if(dialog!=null)dialog.dismiss();showRoomMusicPlayer();});
            LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(86));rp.setMargins(0,0,0,dp(1));list.addView(row,rp);
        }
        if(shown==0){TextView empty=tv(musicNames.isEmpty()?"No songs added yet":"No matching songs",16,0xddffffff,true);empty.setGravity(Gravity.CENTER);list.addView(empty,new LinearLayout.LayoutParams(-1,dp(150)));}
    }
    private void removeMusicAt(int index){
        if(index<0||index>=musicUris.size())return;
        if(cloudRoom){
            if(!isModerator()){toast("Host/co-host only");return;}
            String cloudId=index<musicCloudIds.size()?musicCloudIds.get(index):"";
            if(cloudId!=null&&!cloudId.isEmpty()&&db!=null&&roomId!=null){
                boolean playing=index==currentSongIndex;
                db.collection("live_rooms").document(roomId).collection("music_playlist").document(cloudId).delete()
                    .addOnSuccessListener(v->{if(playing)stopRoomMusic();toast("Song removed from room playlist");})
                    .addOnFailureListener(e->toast("Could not remove song: "+msg(e)));
                return;
            }
        }
        boolean playing=index==currentSongIndex;musicUris.remove(index);musicNames.remove(index);if(index<musicRemoteUrls.size())musicRemoteUrls.remove(index);if(index<musicCloudIds.size())musicCloudIds.remove(index);
        if(playing)stopRoomMusic();else if(currentSongIndex>index)currentSongIndex--;
        saveMusicPlaylist();toast("Song removed");
    }
    private void openMusicPicker(){
        if(!isModerator()){toast("Host/co-host only");return;}
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("audio/*");i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(i,MUSIC_PICK_REQUEST);
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        try{org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onActivityResult(this,requestCode,resultCode,data);}catch(Throwable e){KingStability.nonFatal(this,"party-jitsi-result",e);}
        if(requestCode==ROOM_COVER_PICK_REQUEST){if(resultCode==RESULT_OK&&data!=null&&data.getData()!=null){pendingCreateCoverUri=data.getData();try{getContentResolver().takePersistableUriPermission(pendingCreateCoverUri,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}renderCreateRoomPage();}return;}
        if(requestCode!=MUSIC_PICK_REQUEST||resultCode!=RESULT_OK||data==null)return;
        ArrayList<Uri> picked=new ArrayList<>();
        if(data.getClipData()!=null){for(int i=0;i<data.getClipData().getItemCount();i++){Uri u=data.getClipData().getItemAt(i).getUri();if(u!=null)picked.add(u);}}
        else if(data.getData()!=null)picked.add(data.getData());
        int added=0;
        if(cloudRoom){
            if(!isModerator()){toast("Host/co-host only");return;}
            for(Uri uri:picked){try{getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}uploadRoomPlaylistSong(uri,songName(uri));added++;}
            if(added>0)toast("Uploading "+added+" song"+(added==1?"":"s")+" to the room playlist…");else toast("No new songs added");
        }else{
            for(Uri uri:picked){String us=uri.toString();if(musicUris.contains(us))continue;try{getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}musicUris.add(us);musicNames.add(songName(uri));musicRemoteUrls.add("");musicCloudIds.add("");added++;}
            if(added>0){saveMusicPlaylist();toast(added+" song"+(added==1?"":"s")+" added and saved");musicPanel();}else toast("No new songs added");
        }
    }
    private String songName(Uri uri){
        String name=null;Cursor c=null;try{c=getContentResolver().query(uri,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null);if(c!=null&&c.moveToFirst()){int col=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(col>=0)name=c.getString(col);}}catch(Exception ignored){}finally{if(c!=null)c.close();}
        if(name==null||name.trim().isEmpty())name=uri.getLastPathSegment();if(name==null||name.trim().isEmpty())name="Unknown song";return name;
    }
    private void uploadRoomPlaylistSong(Uri local,String name){
        if(local==null||storage==null||db==null||user==null||roomId==null){toast("Room playlist upload unavailable");return;}
        long bytes=audioSize(local);if(bytes>=12L*1024L*1024L){toast(name+" is over the 12 MB room-music limit");return;}
        String path="chat_media/"+user.getUid()+"/room_music_"+roomId+"/playlist_"+System.currentTimeMillis()+"_"+safeStorageName(name);
        StorageReference ref=storage.getReference().child(path);
        ref.putFile(local).continueWithTask(task->{if(!task.isSuccessful()){Exception ex=task.getException();if(ex!=null)throw ex;throw new RuntimeException("upload failed");}return ref.getDownloadUrl();})
            .addOnSuccessListener(uri->{Map<String,Object>d=new HashMap<>();d.put("name",name);d.put("url",uri.toString());d.put("ownerUid",user.getUid());d.put("addedBy",safeName());d.put("addedAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).collection("music_playlist").add(d).addOnFailureListener(e->toast("Playlist save failed: "+msg(e)));})
            .addOnFailureListener(e->toast("Music upload failed: "+msg(e)));
    }
    private void attachCloudMusicPlaylist(DocumentReference room){
        musicPlaylistListener=room.collection("music_playlist").orderBy("addedAt",Query.Direction.ASCENDING).limit(100).addSnapshotListener((snap,e)->{
            if(e!=null||snap==null)return;
            musicUris.clear();musicNames.clear();musicRemoteUrls.clear();musicCloudIds.clear();
            for(DocumentSnapshot d:snap.getDocuments()){String url=str(d,"url","");if(url.isEmpty())continue;musicUris.add(url);musicRemoteUrls.add(url);musicNames.add(str(d,"name","Unknown song"));musicCloudIds.add(d.getId());}
            if(currentSongIndex>=musicUris.size())currentSongIndex=-1;updateMusicStatus();
        });
    }

    private void playSong(int index){
        if(index<0||index>=musicUris.size())return;
        if(cloudRoom&&isModerator()){shareSongAndPlay(index);return;}
        // A listener is not allowed to start a private, out-of-sync copy
        // of room music and mistake it for synchronized Party playback.
        if(cloudRoom){toast("Only the host or co-host can control shared room music");return;}
        playLocalSong(index);
    }
    private void playLocalSong(int index){
        if(index<0||index>=musicUris.size())return;releaseMusicPlayer();applyingSharedMusic=false;
        try{
            Uri uri=Uri.parse(musicUris.get(index));roomMusicPlayer=MediaPlayer.create(this,uri);
            if(roomMusicPlayer==null){toast("Could not open this audio file");return;}
            currentSongIndex=index;currentSong=musicNames.get(index);roomMusicPlayer.setLooping(false);roomMusicPlayer.setVolume(roomMusicVolume,roomMusicVolume);
            roomMusicPlayer.setOnCompletionListener(mp->{if(cloudRoom&&isModerator())playNextSong();else if(!cloudRoom)playNextSong();});
            roomMusicPlayer.start();updateMusicStatus();addEvent("music",safeName()+" played 🎵 "+currentSong);
        }catch(Exception e){releaseMusicPlayer();toast("Music failed: "+msg(e));}
    }
    private String safeStorageName(String name){
        String x=name==null?"song":name.replaceAll("[^A-Za-z0-9._-]","_");if(x.length()>80)x=x.substring(x.length()-80);return x.isEmpty()?"song":x;
    }
    private long audioSize(Uri uri){
        Cursor c=null;try{c=getContentResolver().query(uri,new String[]{OpenableColumns.SIZE},null,null,null);if(c!=null&&c.moveToFirst()){int col=c.getColumnIndex(OpenableColumns.SIZE);if(col>=0&&!c.isNull(col))return c.getLong(col);}}catch(Exception ignored){}finally{if(c!=null)c.close();}return -1;
    }
    private void shareSongAndPlay(int index){
        if(index<0||index>=musicUris.size())return;if(!isModerator()){toast("Host/co-host controls room music");return;}
        while(musicRemoteUrls.size()<musicUris.size())musicRemoteUrls.add("");
        String known=musicRemoteUrls.get(index);
        if(known!=null&&!known.isEmpty()){
            playLocalSong(index);sharedMusicUrl=known;publishMusicSync(known,musicNames.get(index),0,true);return;
        }
        if(storage==null||user==null||roomId==null){toast("Shared upload unavailable • playing on this phone only");playLocalSong(index);return;}
        playLocalSong(index);
        Uri local=Uri.parse(musicUris.get(index));long bytes=audioSize(local);if(bytes>=12L*1024L*1024L){toast("Song is over the 12 MB shared-music limit • local playback continues");return;}
        toast("Uploading room music…");
        String path="chat_media/"+user.getUid()+"/room_music_"+roomId+"/"+System.currentTimeMillis()+"_"+safeStorageName(musicNames.get(index));
        StorageReference ref=storage.getReference().child(path);
        ref.putFile(local).continueWithTask(task->{if(!task.isSuccessful()){Exception ex=task.getException();if(ex!=null)throw ex;throw new RuntimeException("upload failed");}return ref.getDownloadUrl();})
            .addOnSuccessListener(uri->{if(index>=musicRemoteUrls.size())return;String url=uri.toString();musicRemoteUrls.set(index,url);saveMusicPlaylist();if(currentSongIndex==index){sharedMusicUrl=url;boolean nowPlaying=false;try{nowPlaying=roomMusicPlayer!=null&&roomMusicPlayer.isPlaying();}catch(Exception ignored){}publishMusicSync(url,musicNames.get(index),safePlayerPosition(),nowPlaying);toast("Room music shared");}})
            .addOnFailureListener(e->toast("Shared upload failed • local playback continues: "+msg(e)));
    }
    private int safePlayerPosition(){try{return roomMusicPlayer==null?0:Math.max(0,roomMusicPlayer.getCurrentPosition());}catch(Exception ignored){return 0;}}
    private void publishMusicSync(String url,String name,long positionMs,boolean playing){
        if(!cloudRoom||!isModerator()||db==null||user==null||roomId==null)return;
        Map<String,Object>d=new HashMap<>();d.put("actorUid",user.getUid());d.put("actorName",safeName());d.put("musicUrl",url==null?"":url);d.put("musicName",name==null?"":name);d.put("musicPlaying",playing);d.put("musicPositionMs",Math.max(0,positionMs));d.put("updatedAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).collection("music_state").document("current").set(d).addOnFailureListener(e->toast("Music sync failed: "+msg(e)));
    }
    private void attachMusicSync(DocumentReference room){
        final String expectedRoom9996=room.getId();
        final String expectedAccount9996=user==null?null:user.getUid();
        final int generation9996=roomListenerGeneration982;
        musicStateListener=room.collection("music_state").document("current").addSnapshotListener((d,e)->{
            // A late Firebase callback for an old room must not resume its
            // audio in the new room, even if the observer was just removed.
            if(!cloudRoom||roomId==null||!expectedRoom9996.equals(roomId)
                ||generation9996!=roomListenerGeneration982||user==null
                ||expectedAccount9996==null||!expectedAccount9996.equals(user.getUid()))return;
            if(e!=null||d==null||!d.exists())return;
            String actor=d.getString("actorUid");if(user.getUid().equals(actor))return;
            applyMusicSync(d);
        });
    }
    private void applyMusicSync(DocumentSnapshot d){
        String url=str(d,"musicUrl","");String name=str(d,"musicName","");boolean playing=Boolean.TRUE.equals(d.getBoolean("musicPlaying"));Long posObj=d.getLong("musicPositionMs");long pos=posObj==null?0:Math.max(0,posObj);
        com.google.firebase.Timestamp ts=d.getTimestamp("updatedAt");if(playing&&ts!=null)pos+=Math.max(0,System.currentTimeMillis()-ts.toDate().getTime());
        if(url.isEmpty()){releaseMusicPlayer();sharedMusicUrl="";currentSong=name;currentSongIndex=-1;updateMusicStatus();return;}
        if(!url.equals(sharedMusicUrl)||roomMusicPlayer==null){startSharedStream(url,name,pos,playing);return;}
        currentSong=name;try{int now=roomMusicPlayer.getCurrentPosition();if(Math.abs(now-pos)>1800)roomMusicPlayer.seekTo((int)Math.min(Integer.MAX_VALUE,pos));if(playing&&!roomMusicPlayer.isPlaying())roomMusicPlayer.start();else if(!playing&&roomMusicPlayer.isPlaying())roomMusicPlayer.pause();}catch(Exception ignored){}updateMusicStatus();
    }
    private void startSharedStream(String url,String name,long positionMs,boolean playing){
        releaseMusicPlayer();sharedMusicUrl=url;currentSong=name;currentSongIndex=-1;applyingSharedMusic=true;updateMusicStatus();
        try{
            MediaPlayer p=new MediaPlayer();roomMusicPlayer=p;p.setDataSource(url);p.setVolume(roomMusicVolume,roomMusicVolume);p.setLooping(false);
            final long target=Math.max(0,positionMs);p.setOnPreparedListener(mp->{try{if(target>0)mp.seekTo((int)Math.min(Integer.MAX_VALUE,target));if(playing)mp.start();}catch(Exception ignored){}updateMusicStatus();});
            p.setOnCompletionListener(mp->{currentSong="";sharedMusicUrl="";updateMusicStatus();});
            p.setOnErrorListener((mp,what,extra)->{toast("Shared music could not play");return false;});p.prepareAsync();
        }catch(Exception e){releaseMusicPlayer();toast("Shared music failed: "+msg(e));}
    }
    private void releaseMusicPlayer(){try{if(roomMusicPlayer!=null){roomMusicPlayer.stop();roomMusicPlayer.release();}}catch(Exception ignored){}roomMusicPlayer=null;}
    private void playNextSong(){if(cloudRoom&&!isModerator()){toast("Host/co-host controls room music");return;}if(musicUris.isEmpty()){stopRoomMusic();return;}int n=currentSongIndex<0?0:(currentSongIndex+1)%musicUris.size();playSong(n);}
    private void playPreviousSong(){if(cloudRoom&&!isModerator()){toast("Host/co-host controls room music");return;}if(musicUris.isEmpty()){toast("Playlist is empty");return;}int n=currentSongIndex<0?0:(currentSongIndex-1+musicUris.size())%musicUris.size();playSong(n);}
    private void toggleRoomMusic(){
        if(cloudRoom&&!isModerator()){toast("Host/co-host controls room music");return;}
        if(roomMusicPlayer==null){if(musicUris.isEmpty()){toast("Add music first");return;}playSong(currentSongIndex>=0?currentSongIndex:0);return;}
        try{if(roomMusicPlayer.isPlaying()){int p=safePlayerPosition();roomMusicPlayer.pause();if(cloudRoom)publishMusicSync(sharedMusicUrl,currentSong,p,false);}else{int p=safePlayerPosition();roomMusicPlayer.start();if(cloudRoom)publishMusicSync(sharedMusicUrl,currentSong,p,true);}}catch(Exception e){toast("Music control failed");}
        updateMusicStatus();
    }

    private void showRoomMusicPlayer(){
        if(currentSongIndex<0&&musicUris.isEmpty()&&currentSong.isEmpty()){musicPanel();return;}
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(14),dp(18),dp(18));root.setBackgroundColor(0xee202734);
        TextView title=tv(currentSongIndex>=0&&currentSongIndex<musicNames.size()?musicNames.get(currentSongIndex):(currentSong.isEmpty()?"Room Music":currentSong),19,Color.WHITE,true);title.setSingleLine(true);root.addView(title,new LinearLayout.LayoutParams(-1,dp(48)));
        LinearLayout vol=new LinearLayout(this);vol.setGravity(Gravity.CENTER_VERTICAL);TextView speaker=tv("🔊",24,Color.WHITE,true);vol.addView(speaker,new LinearLayout.LayoutParams(dp(48),dp(48)));SeekBar seek=new SeekBar(this);seek.setMax(100);seek.setProgress(Math.round(roomMusicVolume*100));vol.addView(seek,new LinearLayout.LayoutParams(0,dp(48),1));TextView pct=tv(Math.round(roomMusicVolume*100)+"%",13,Color.WHITE,true);pct.setGravity(Gravity.CENTER);vol.addView(pct,new LinearLayout.LayoutParams(dp(58),dp(48)));root.addView(vol,new LinearLayout.LayoutParams(-1,dp(56)));
        LinearLayout controls=new LinearLayout(this);controls.setGravity(Gravity.CENTER);TextView prev=pill("|◀",0x00302a3a,this::playPreviousSong);controls.addView(prev,new LinearLayout.LayoutParams(0,dp(62),1));TextView play=pill(roomMusicPlayer!=null&&roomMusicPlayer.isPlaying()?"Ⅱ":"▶",0xfff7f7f7,null);play.setTextColor(0xff202734);play.setTextSize(26);controls.addView(play,new LinearLayout.LayoutParams(dp(74),dp(66)));TextView next=pill("▶|",0x00302a3a,this::playNextSong);controls.addView(next,new LinearLayout.LayoutParams(0,dp(62),1));TextView list=pill("☷",0x00302a3a,cloudRoom&&!isModerator()?()->toast("Host/co-host controls the room playlist"):this::musicPanel);list.setTextSize(26);controls.addView(list,new LinearLayout.LayoutParams(0,dp(62),1));root.addView(controls,new LinearLayout.LayoutParams(-1,dp(78)));
        AlertDialog dialog=new AlertDialog.Builder(this).setView(root).create();
        play.setOnClickListener(v->{toggleRoomMusic();dialog.dismiss();showRoomMusicPlayer();});
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int p,boolean from){roomMusicVolume=p/100f;pct.setText(p+"%");if(roomMusicPlayer!=null)roomMusicPlayer.setVolume(roomMusicVolume,roomMusicVolume);}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});
        dialog.setOnShowListener(v->{android.view.Window w=dialog.getWindow();if(w!=null){w.setLayout(-1,-2);w.setGravity(Gravity.BOTTOM);w.setBackgroundDrawable(bg(0xee202734,22));}});dialog.show();
    }
    private void stopRoomMusic(){
        if(cloudRoom&&!isModerator()){releaseMusicPlayer();sharedMusicUrl="";currentSongIndex=-1;currentSong="";updateMusicStatus();return;}
        releaseMusicPlayer();currentSongIndex=-1;currentSong="";sharedMusicUrl="";updateMusicStatus();if(cloudRoom&&isModerator())publishMusicSync("","",0,false);
    }
    private void updateMusicStatus(){
        if(musicStatusLabel==null)return;String state=currentSong.isEmpty()?"":"🎵 "+currentSong+(roomMusicPlayer!=null&&roomMusicPlayer.isPlaying()?" • Playing":" • Paused")+(cloudRoom&&!isModerator()?" • Room sync":"");
        runOnUiThread(()->{musicStatusLabel.setText(state);musicStatusLabel.setVisibility(state.isEmpty()?View.GONE:View.VISIBLE);});
    }

    private void openVoice(){
        if(!cloudRoom||user==null||db==null||roomId==null){toast("Join a live Firebase Party room first");return;}
        if(mySeat<1){toast("Take a mic seat first");return;}
        if(!micOn)toggleMic();else toast("Mic is already ON in this Party room");
    }
    private void openVideoRoom(){
        if(!cloudRoom||roomId==null||roomId.trim().isEmpty()){toast("Join a live Party room first");return;}
        stopInRoomVoice940(true);
        Intent i=new Intent(this,KingMultiVideoActivity.class);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);i.putExtra("displayName",safeName());startActivity(i);
    }

    private void sendMessage(EditText box) {
        if(box==null)return;
        if(!cloudRoom||!memberSeen||user==null||db==null||roomId==null||
            roomId.trim().isEmpty()){
            toast("Join a live KING Party before sending messages");return;
        }
        final String chatRoom9918=roomId, chatUid9918=user.getUid();
        final int chatGeneration9918=presenceGeneration967;
        String text=box.getText()==null?"":box.getText().toString().trim();
        if(text.isEmpty())return;
        if(text.length()>500){box.setError("Maximum 500 characters");return;}
        String outgoing=text;
        if(!pendingReplyText620.isEmpty())outgoing="↪ "+pendingReplyName620+": "+shortChatPreview620(pendingReplyText620)+"\n"+text;
        final String finalOutgoing=outgoing;
        final String originalText=text;
        final boolean keepKeyboard=box.hasFocus();
        // Clear immediately so a successfully sent message never remains in the composer.
        box.setText("");
        clearRoomReply620();
        if(keepKeyboard){box.requestFocus();ViewCompat.requestApplyInsets(box);}
        if(cloudRoom&&user!=null&&db!=null){
            Map<String,Object>d=new HashMap<>();d.put("senderUid",user.getUid());d.put("senderName",safeName());d.put("text",finalOutgoing);d.put("type","text");d.put("createdAt",FieldValue.serverTimestamp());
            db.collection("live_rooms").document(chatRoom9918).collection("messages").add(d)
                .addOnSuccessListener(v->{
                    if(partyUiDead()||!isActivePresence967(chatRoom9918,chatUid9918,chatGeneration9918))return;
                    if(keepKeyboard){box.requestFocus();keepRoomKeyboard881(box);}else hideRoomKeyboard(box);
                    if(roomChatScroll!=null)roomChatScroll.post(()->roomChatScroll.fullScroll(View.FOCUS_DOWN));
                })
                .addOnFailureListener(e->{
                    if(partyUiDead()||!isActivePresence967(chatRoom9918,chatUid9918,chatGeneration9918))return;
                    if(box.getText()==null||box.getText().length()==0){box.setText(originalText);box.setSelection(box.length());}
                    box.requestFocus();keepRoomKeyboard881(box);toast("Message failed: "+msg(e));
                });
        }else{
            appendLocalChat(displayName,finalOutgoing);
            try{ addChatRow(displayName,finalOutgoing); }catch(Exception e){ android.util.Log.e("KINGPlusParty","Local chat row render failed",e); }
            if(keepKeyboard){box.requestFocus();keepRoomKeyboard881(box);}else hideRoomKeyboard(box);
            if(roomChatScroll!=null)roomChatScroll.post(()->roomChatScroll.fullScroll(View.FOCUS_DOWN));
        }
    }
    private void keepRoomKeyboard881(EditText box){
        if(box==null)return;
        try{
            box.postDelayed(()->{
                try{
                    box.requestFocus();
                    android.view.inputmethod.InputMethodManager imm=(android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    if(imm!=null)imm.showSoftInput(box,android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                    ViewCompat.requestApplyInsets(box);
                }catch(Exception ignored){}
            },80);
        }catch(Exception ignored){}
    }
    private void hideRoomKeyboard(EditText box){
        if(box==null)return;
        try{
            box.clearFocus();
            android.view.inputmethod.InputMethodManager imm=(android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            if(imm!=null)imm.hideSoftInputFromWindow(box.getWindowToken(),0);
        }catch(Exception ignored){}
    }
    private void seedLocalChat(){if(cloudRoom)return;String saved=prefs.getString("chat_"+roomId,"");if(!saved.isEmpty())for(String row:saved.split("\\n")){String[]p=row.split("\\|",2);if(p.length==2)addChatRow(p[0],p[1]);}}
    private void appendLocalChat(String who,String text){String old=prefs.getString("chat_"+roomId,"");prefs.edit().putString("chat_"+roomId,old+who.replace("|","")+"|"+text.replace("\n"," ")+"\n").apply();}
    private int safeRoomMessageTextSize861(String text){
        if(text==null||text.isEmpty())return 14;
        try{
            int count=0;boolean hasSymbol=false;
            for(int i=0;i<text.length()&&count<=6;){
                int cp=text.codePointAt(i);
                if(cp>0x2000)hasSymbol=true;
                i+=Math.max(1,Character.charCount(cp));count++;
            }
            return count<=6&&hasSymbol?34:14;
        }catch(Exception ignored){return 14;}
    }

    private void addChatRow(String who,String text){
        if(chatBox==null)return;
        boolean follow=roomChatScroll!=null && !roomChatScroll.canScrollVertically(1);
        int bubbleWidth=Math.min(dp(280),Math.max(dp(220),getResources().getDisplayMetrics().widthPixels-dp(54)));
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setMinimumHeight(dp(58));
        card.setPadding(dp(10),dp(6),dp(10),dp(7));boolean giftMessage=text!=null&&text.contains("🎁")&&(text.contains(" sent ")||text.contains("Gift"));boolean mineBubble940=who!=null&&who.equals(safeName());int partyBubble940=mineBubble940?KingCosmetics.bubbleColor(KingCosmetics.bubble(this)):0x20ffffff;card.setBackground(bg(giftMessage?0x55b78925:partyBubble940,12));
        TextView n=tv((giftMessage?"🎁  ":"👤  ")+chatIdentity750(who),11,giftMessage?0xffffdf6b:0xffffedaf,true);n.setPadding(0,0,0,dp(3));n.setSingleLine(true);n.setEllipsize(android.text.TextUtils.TruncateAt.END);
        card.addView(n,new LinearLayout.LayoutParams(-1,dp(24)));
        TextView msg=tv(text,14,Color.WHITE,false);msg.setPadding(0,0,0,0);msg.setMinHeight(dp(24));
        text=stickerFallback610(text);msg.setText(text);int live=LiveEmojiView.parse(text);
        if(ReferenceEmojiView.parse(text)>=0){ReferenceEmojiView art=new ReferenceEmojiView(this,ReferenceEmojiView.parse(text),false);LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(dp(64),dp(64));ap.gravity=Gravity.CENTER_HORIZONTAL;card.addView(art,ap);}
        else if(stickerResource550(text)!=0){android.widget.ImageView art=stickerImage852(stickerResource550(text));LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(dp(64),dp(64));ap.gravity=Gravity.CENTER_HORIZONTAL;card.addView(art,ap);}
        else if(live>=0){LiveEmojiView art=new LiveEmojiView(this,live,4500);LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(dp(64),dp(64));ap.gravity=Gravity.CENTER_HORIZONTAL;card.addView(art,ap);}
        else{card.addView(msg,new LinearLayout.LayoutParams(-1,-2));}
        final String actionText620=text;card.setOnLongClickListener(v->{roomChatMessageActions620(who,actionText620);return true;});n.setOnClickListener(v->roomChatMessageActions620(who,actionText620));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(bubbleWidth,-2);p.setMargins(0,dp(3),0,dp(3));chatBox.addView(card,p);
        if(follow){final ScrollView scroll=roomChatScroll;scroll.post(()->scroll.fullScroll(View.FOCUS_DOWN));}
    }
    private String chatIdentity750(String who){String uid=memberUids.get(who);int lv=1,vip=0;if(user!=null&&who!=null&&who.equals(safeName())){LevelSystem.Snapshot me=LevelSystem.read(this);lv=me.level;vip=me.vipLevel;}else if(uid!=null){Integer l=memberLevel720.get(uid),v=memberVip720.get(uid);if(l!=null)lv=l;if(v!=null)vip=v;}return (vip>0?"💎VIP"+vip+"  ":"")+"Lv."+lv+"  "+who;}
    private void roomChatMessageActions620(String who,String text){
        String uid=memberUids.get(who);if((uid==null||uid.isEmpty())&&who!=null&&who.equals(ownerName))uid=ownerUid;final String targetUid=uid==null?"":uid;
        List<String> items=new ArrayList<>();items.add("↩ Reply");items.add("@ Mention");if(!targetUid.isEmpty()&&(user==null||!targetUid.equals(user.getUid()))){items.add("💬 Private chat");items.add("🎁 Send gift");items.add("👤 Profile");}
        new AlertDialog.Builder(this).setTitle(who).setItems(items.toArray(new String[0]),(d,w)->{String x=items.get(w);if(x.contains("Reply")){pendingReplyName620=who;pendingReplyText620=text;if(composerBox!=null){composerBox.setHint("Reply to "+who+" • "+shortChatPreview620(text));composerBox.requestFocus();}}else if(x.contains("Mention")){if(composerBox!=null){String now=composerBox.getText().toString();composerBox.setText(now+(now.isEmpty()?"":" ")+"@"+who+" ");composerBox.setSelection(composerBox.length());}}else if(x.contains("Private"))openPrivateChat(targetUid,who);else if(x.contains("gift"))giftDialogFor(targetUid,who);else showRichProfile(targetUid,who,targetUid.equals(ownerUid));}).show();
    }
    private void clearRoomReply620(){pendingReplyName620="";pendingReplyText620="";if(composerBox!=null)composerBox.setHint("Type message...");}
    private String shortChatPreview620(String s){if(s==null)return "";String x=s.replace('\n',' ').trim();return x.length()>38?x.substring(0,38)+"…":x;}

    private void roomChatHistory620(){
        if(!cloudRoom||db==null){new AlertDialog.Builder(this).setTitle("🗨 Chat history").setMessage("Local room chat is already shown on this device.").setPositiveButton("OK",null).show();return;}
        db.collection("live_rooms").document(roomId).collection("messages").orderBy("createdAt",Query.Direction.DESCENDING).limit(80).get().addOnSuccessListener(snap->{List<String> rows=new ArrayList<>();for(DocumentSnapshot d:snap.getDocuments()){String t=str(d,"text","");if(!t.isEmpty())rows.add(str(d,"senderName","User")+": "+shortChatPreview620(t));}showRows("🗨 Recent room chat",rows,"No messages yet");}).addOnFailureListener(e->toast("Chat history unavailable: "+msg(e)));
    }

    private void giftWall620(){
        if(!cloudRoom||db==null){new AlertDialog.Builder(this).setTitle("🎁 Gift Wall").setMessage("Gift Wall becomes live inside a Firebase room.").setPositiveButton("Gift Shop",(d,w)->giftShopPanel()).setNegativeButton("Close",null).show();return;}
        db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(250).get().addOnSuccessListener(snap->{
            Map<String,Long> giftValue=new HashMap<>();Map<String,Integer> giftCount=new HashMap<>();long total=0;int events=0;
            for(DocumentSnapshot d:snap.getDocuments())if("gift".equals(d.getString("type"))){String g=str(d,"giftIcon","🎁")+" "+str(d,"giftName","Gift");Long v=d.getLong("giftValue");long value=v==null?0:Math.max(0,v);giftValue.put(g,(giftValue.containsKey(g)?giftValue.get(g):0L)+value);giftCount.put(g,(giftCount.containsKey(g)?giftCount.get(g):0)+1);total+=value;events++;}
            List<Map.Entry<String,Long>> list=new ArrayList<>(giftValue.entrySet());java.util.Collections.sort(list,(a,b)->Long.compare(b.getValue(),a.getValue()));
            LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(14),dp(10),dp(14),dp(14));root.setBackgroundColor(0xff171320);
            LinearLayout summary=new LinearLayout(this);summary.setGravity(Gravity.CENTER);summary.setBackground(bg(0xff261c36,16));
            TextView totalGifts=tv("🎁 "+events,17,Color.WHITE,true);totalGifts.setGravity(Gravity.CENTER);summary.addView(totalGifts,new LinearLayout.LayoutParams(0,dp(58),1));
            TextView totalValue=tv("💎 "+compactNumber(total),17,0xffffd35a,true);totalValue.setGravity(Gravity.CENTER);summary.addView(totalValue,new LinearLayout.LayoutParams(0,dp(58),1));root.addView(summary,new LinearLayout.LayoutParams(-1,dp(62)));
            ScrollView scroll=new ScrollView(this);LinearLayout grid=new LinearLayout(this);grid.setOrientation(LinearLayout.VERTICAL);grid.setPadding(0,dp(8),0,dp(6));scroll.addView(grid);
            if(list.isEmpty()){TextView empty=tv("No gifts in this room yet",14,MUTED,true);empty.setGravity(Gravity.CENTER);grid.addView(empty,new LinearLayout.LayoutParams(-1,dp(100)));}
            LinearLayout row=null;int col=0,shown=0;
            for(Map.Entry<String,Long> e:list){if(shown++>=12)break;if(row==null||col==3){row=new LinearLayout(this);row.setGravity(Gravity.TOP);grid.addView(row,new LinearLayout.LayoutParams(-1,dp(116)));col=0;}
                String gift=e.getKey();Integer cnt=giftCount.get(gift);LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(3),dp(4),dp(3),dp(4));card.setBackground(bg(0xff282235,14));
                String icon="🎁",name=gift;int sp=gift.indexOf(' ');if(sp>0){icon=gift.substring(0,sp);name=gift.substring(sp+1);}
                TextView iv=tv(icon,31,Color.WHITE,false);iv.setGravity(Gravity.CENTER);card.addView(iv,new LinearLayout.LayoutParams(-1,dp(43)));
                TextView nm=tv(name,10,Color.WHITE,true);nm.setGravity(Gravity.CENTER);nm.setSingleLine(true);card.addView(nm,new LinearLayout.LayoutParams(-1,dp(22)));
                TextView meta=tv("×"+(cnt==null?0:cnt)+"   💎"+compactNumber(e.getValue()),9,0xffffd35a,true);meta.setGravity(Gravity.CENTER);card.addView(meta,new LinearLayout.LayoutParams(-1,dp(24)));
                LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(108),1);cp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(card,cp);col++;
            }
            if(row!=null&&col>0&&col<3)for(int i=col;i<3;i++){View spacer=new View(this);LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(0,dp(108),1);ep.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(spacer,ep);}
            root.addView(scroll,new LinearLayout.LayoutParams(-1,Math.min(dp(420),Math.max(dp(120),((list.size()+2)/3)*dp(116)+dp(14)))));
            new AlertDialog.Builder(this).setTitle("🎁 Gift Wall").setView(root).setPositiveButton("Gift Shop",(d,w)->giftShopPanel()).setNeutralButton("Ranking",(d,w)->roomRankingDialog()).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast("Gift Wall unavailable: "+msg(e)));
    }

    private void seedLocalFeed(){if(cloudRoom)return;}
    private void addFeed(String text){if(feedBox==null)return;TextView t=tv("✨  "+text,12,0xffffd46b,true);t.setBackground(bg(0x18000000,9));t.setPadding(dp(8),dp(4),dp(8),dp(4));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(2),0,dp(2));feedBox.addView(t,p);}

    private void giftRecipientDialog(){ giftShopPanel(); }
    private void giftDialog(){ giftShopPanel(); }
    private void giftDialogFor(String targetUid,String targetLabel) {
        giftTargetUid=targetUid==null?"":targetUid; giftTargetName=targetLabel==null?"Host":targetLabel; giftShopPanel();
    }
    private KingGiftArtView giftHero994;
    private String giftSelectionText867(){
        int i=Math.max(0,Math.min(giftNames.length-1,giftSelectedIndex));
        long total=(long)giftCosts[i]*Math.max(1,giftQuantity);
        String tier994=i>=64&&giftVipRequired[i]>0
            ?" • VIP "+giftVipRequired[i]+" PREVIEW ONLY"
            :(i<64?" • 💎 "+total:" • FREE (no coins)");
        return giftIcons[i]+"  "+giftNames[i]+"  • x"+Math.max(1,giftQuantity)
            +tier994+"\nTo: "+giftTargetName;
    }
    private void refreshGiftSelection867(){
        if(giftSelectionLabel!=null)giftSelectionLabel.setText(giftSelectionText867());
        if(giftHero994!=null&&giftSelectedIndex>=0&&giftSelectedIndex<giftNames.length)
            giftHero994.setGift(giftSelectedIndex,giftNames[giftSelectedIndex],
                                 giftIcons[giftSelectedIndex]);
    }
    private void giftShopPanel(){
        if(ownerName==null)ownerName="KING Host";
        if(giftTargetName==null||giftTargetName.isEmpty()){giftTargetName="👑 "+ownerName;giftTargetUid=ownerUid==null?"":ownerUid;}
        giftQuantity=1; giftCategory="All"; giftSelectedIndex=0;giftHero994=null;
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(10),dp(8),dp(10),dp(10));root.setBackgroundColor(0xff181824);
        LinearLayout banner=new LinearLayout(this);banner.setGravity(Gravity.CENTER_VERTICAL);banner.setPadding(dp(10),dp(4),dp(8),dp(4));banner.setBackground(bg(0xff7d36d6,14));
        TextView wt=tv("🎟  Weekly Gift Card",16,Color.WHITE,true);banner.addView(wt,new LinearLayout.LayoutParams(0,dp(42),1));TextView arrow=tv("›",28,Color.WHITE,true);arrow.setGravity(Gravity.CENTER);banner.addView(arrow,new LinearLayout.LayoutParams(dp(40),dp(42)));banner.setOnClickListener(v->{if(giftShopDialog!=null)giftShopDialog.dismiss();weeklyGiftCardDialog();});root.addView(banner,new LinearLayout.LayoutParams(-1,dp(40)));
        LinearLayout top=(LinearLayout)getLayoutInflater().inflate(R.layout.king_party_gift_header_10064,root,false);
        TextView title=top.findViewById(R.id.party_gift_title_10064);title.setText("Gift Shop • "+giftNames.length+" gifts");
        giftBalanceLabel=top.findViewById(R.id.party_gift_balance_10064);giftBalanceLabel.setText("💎 checking…");
        root.addView(top);refreshPartyGiftBalance10064();
        LinearLayout giftQuick950=new LinearLayout(this);giftQuick950.setGravity(Gravity.CENTER);String[] gq950={"🎁 Wall","🏆 Rank","🎒 Backpack"};for(String q950:gq950){TextView b950=pill(q950,0xff2b2938,()->{if(giftShopDialog!=null)giftShopDialog.dismiss();if(q950.contains("Wall"))giftWall620();else if(q950.contains("Rank"))roomRankingDialog();else backpackGiftDialog700();});b950.setTextSize(11);LinearLayout.LayoutParams qp950=new LinearLayout.LayoutParams(0,dp(38),1);qp950.setMargins(dp(2),0,dp(2),dp(4));giftQuick950.addView(b950,qp950);}root.addView(giftQuick950,new LinearLayout.LayoutParams(-1,dp(42)));
        TextView soundToggle993=pill(
            KingGiftSound993.enabled(this)?"🔊 Gift Sound ON":"🔇 Gift Sound OFF",
            0xff342c55,null);
        soundToggle993.setTextSize(11);
        soundToggle993.setOnClickListener(v->{
            boolean on=!KingGiftSound993.enabled(this);
            KingGiftSound993.setEnabled(this,on);
            soundToggle993.setText(on?"🔊 Gift Sound ON":"🔇 Gift Sound OFF");
        });
        root.addView(soundToggle993,new LinearLayout.LayoutParams(-1,dp(34)));
        TextView preview9932=pill("▶ Hear selected gift sound • FREE PREVIEW",0xff36305c,
            ()->KingGiftSound993.play(this,giftNames[giftSelectedIndex]));
        preview9932.setTextSize(11);
        root.addView(preview9932,new LinearLayout.LayoutParams(-1,dp(36)));
        LinearLayout heroPanel994=new LinearLayout(this);
        heroPanel994.setGravity(Gravity.CENTER_VERTICAL);
        heroPanel994.setPadding(dp(6),dp(4),dp(8),dp(4));
        heroPanel994.setBackground(bg(0xff282038,16));
        giftHero994=new KingGiftArtView(this,giftSelectedIndex,
            giftNames[giftSelectedIndex],giftIcons[giftSelectedIndex]);
        giftHero994.setShowcaseMode(true);
        heroPanel994.addView(giftHero994,new LinearLayout.LayoutParams(dp(112),dp(104)));
        LinearLayout heroInfo994=new LinearLayout(this);
        heroInfo994.setOrientation(LinearLayout.VERTICAL);
        giftSelectionLabel=tv(giftSelectionText867(),12,0xffffe38a,true);
        giftSelectionLabel.setGravity(Gravity.CENTER_VERTICAL);
        giftSelectionLabel.setMaxLines(3);
        heroInfo994.addView(giftSelectionLabel,
            new LinearLayout.LayoutParams(-1,dp(75)));
        TextView hint994=tv("✨ Animated gift preview • choose any gift",10,
            0xfff5cfee,false);
        heroInfo994.addView(hint994,new LinearLayout.LayoutParams(-1,dp(22)));
        heroPanel994.addView(heroInfo994,
            new LinearLayout.LayoutParams(0,dp(104),1));
        LinearLayout.LayoutParams heroLp994=new LinearLayout.LayoutParams(-1,dp(110));
        heroLp994.bottomMargin=dp(4);
        root.addView(heroPanel994,heroLp994);
        HorizontalScrollView peopleScroll=new HorizontalScrollView(this);peopleScroll.setHorizontalScrollBarEnabled(false);LinearLayout people=new LinearLayout(this);people.setGravity(Gravity.CENTER_VERTICAL);people.setPadding(0,dp(3),0,dp(5));addGiftRecipientChip(people,"🙋 ME • "+safeName(),
            user==null?"":user.getUid());
        addGiftRecipientChip(people,"👑 "+ownerName,ownerUid==null?"":ownerUid);for(int no=1;no<=maxSeats;no++){String n=seatNames.get(no);if(n==null||n.equals(displayName))continue;addGiftRecipientChip(people,no+" • "+n,seatUids.get(no)==null?"":seatUids.get(no));}peopleScroll.addView(people);root.addView(peopleScroll,new LinearLayout.LayoutParams(-1,dp(50)));
        HorizontalScrollView catScroll=new HorizontalScrollView(this);catScroll.setHorizontalScrollBarEnabled(false);giftCategoryBar867=new LinearLayout(this);giftCategoryBar867.setGravity(Gravity.CENTER_VERTICAL);catScroll.addView(giftCategoryBar867);root.addView(catScroll,new LinearLayout.LayoutParams(-1,dp(44)));rebuildGiftCategories867();
        ScrollView giftsScroll=new ScrollView(this);giftGridBox=new LinearLayout(this);giftGridBox.setOrientation(LinearLayout.VERTICAL);giftsScroll.addView(giftGridBox);root.addView(giftsScroll,new LinearLayout.LayoutParams(-1,0,1));rebuildGiftGrid();
        LinearLayout qty=new LinearLayout(this);qty.setGravity(Gravity.CENTER);int[] qs={1,3,9};for(int q:qs){TextView b=pill(String.valueOf(q),q==1?0xff776a00:0xff343143,()->{giftQuantity=q;refreshGiftSelection867();});LinearLayout.LayoutParams qp=new LinearLayout.LayoutParams(0,dp(44),1);qp.setMargins(dp(3),0,dp(3),0);qty.addView(b,qp);}TextView send=pill("SEND",0xffffd92e,()->KingSafe.run(this,"gift-send",()->{if(giftSelectedIndex>=64&&giftVipRequired[giftSelectedIndex]>0){
                    toast("VIP gift locked • Payments and verified entitlements are OFF");
                    return;
                }
                if(giftSelectedIndex<64)sendPaidPartyGift10064();
                else sendGiftQuantity(giftTargetUid,giftTargetName,giftNames[giftSelectedIndex],giftIcons[giftSelectedIndex],0,giftQuantity,0);}));send.setTextColor(Color.BLACK);send.setTextSize(13);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(dp(78),dp(44));sp.setMargins(dp(5),0,0,0);qty.addView(send,sp);root.addView(qty);
        giftShopDialog=new AlertDialog.Builder(this).setView(root).create();
        giftShopDialog.setOnDismissListener(d->{if(giftHero994!=null){
            giftHero994.setShowcaseMode(false);
            giftHero994=null;
        }});
        giftShopDialog.setOnShowListener(v->{android.view.Window w=giftShopDialog.getWindow();if(w!=null){w.setBackgroundDrawableResource(android.R.color.transparent);w.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);w.setGravity(Gravity.BOTTOM);int h950=Math.min(dp(650),Math.max(dp(430),(int)(getResources().getDisplayMetrics().heightPixels*.72f)));w.setLayout(-1,h950);root.setTranslationY(dp(42));root.setAlpha(.88f);root.animate().translationY(0).alpha(1f).setDuration(190).start();}});giftShopDialog.show();
    }
    private void rebuildGiftCategories867(){
        if(giftCategoryBar867==null)return;giftCategoryBar867.removeAllViews();String[] cs={"All","Voice","VIP Night","Flying","Relationship","Activity","Classic","Fame","Privilege","Filters","Parcel"};
        for(String c:cs){boolean selected=c.equals(giftCategory);TextView t=tv(c,12,selected?Color.BLACK:0xffd0cad8,true);t.setGravity(Gravity.CENTER);t.setBackground(bg(selected?0xffffd92e:0xff2a2938,12));t.setOnClickListener(v->{giftCategory=c;rebuildGiftCategories867();rebuildGiftGrid();});LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(dp(88),dp(36));cp.setMargins(dp(2),dp(2),dp(2),dp(2));giftCategoryBar867.addView(t,cp);}
    }
    private void addGiftRecipientChip(LinearLayout row,String label,String uid){
        TextView chip=pill(label,0xff343143,()->{giftTargetName=label;giftTargetUid=uid==null?"":uid;refreshGiftSelection867();});
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(132),dp(42));p.setMargins(dp(2),0,dp(2),0);row.addView(chip,p);
    }
    private void rebuildGiftGrid(){
        if(giftGridBox==null)return;giftGridBox.removeAllViews();
        int first=-1;boolean selectedVisible=false;for(int i=0;i<giftNames.length;i++){if("All".equals(giftCategory)||giftCategory.equals(giftCategories[i])){if(first<0)first=i;if(i==giftSelectedIndex)selectedVisible=true;}}
        if(first<0){giftGridBox.addView(tv("No gifts in this category yet",13,MUTED,false));return;}if(!selectedVisible)giftSelectedIndex=first;refreshGiftSelection867();
        LinearLayout row=null;int col=0;int myVip=LevelSystem.read(this).vipLevel;
        for(int i=0;i<giftNames.length;i++){
            if(!"All".equals(giftCategory)&&!giftCategory.equals(giftCategories[i]))continue;
            if(row==null||col==4){row=new LinearLayout(this);row.setGravity(Gravity.CENTER);giftGridBox.addView(row,new LinearLayout.LayoutParams(-1,dp(139)));col=0;}
            final int idx=i;final int vipNeed=giftVipRequired[i];
            final boolean unlocked=idx<64||vipNeed==0;final boolean selected=idx==giftSelectedIndex;LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setBackground(bg(selected?0xff47336c:(unlocked?0xff232231:0xff171720),14));card.setPadding(dp(2),dp(3),dp(2),dp(2));card.setAlpha(unlocked?1f:.82f);
            TextView nw=tv(selected?"✓ SELECTED":unlocked?(idx<64?"💎 "+giftCosts[idx]:"FREE"):"🔒 VIP "+vipNeed,9,selected?0xffffd92e:(unlocked?0xff68e89d:0xffffb04f),true);nw.setGravity(Gravity.CENTER);card.addView(nw,new LinearLayout.LayoutParams(-1,dp(18)));
            FrameLayout artStage994=new FrameLayout(this);
            KingGiftArtView art994=new KingGiftArtView(this,idx,
                giftNames[i],giftIcons[i]);
            artStage994.addView(art994,new FrameLayout.LayoutParams(-1,-1));
            if(!unlocked){
                TextView badge994=tv("🔒",13,0xffffd58b,true);
                badge994.setGravity(Gravity.RIGHT|Gravity.TOP);
                artStage994.addView(badge994,
                    new FrameLayout.LayoutParams(-1,dp(22),Gravity.TOP));
            }
            card.addView(artStage994,new LinearLayout.LayoutParams(-1,dp(61)));
            TextView name=tv(giftNames[i],9,unlocked?0xfff5f2f7:0xff817b88,true);name.setGravity(Gravity.CENTER);name.setMaxLines(1);card.addView(name,new LinearLayout.LayoutParams(-1,dp(22)));
            TextView cost=tv(unlocked?(idx<64?"💎 "+giftCosts[idx]:"FREE"):"PREVIEW",9,
                selected?0xffffd92e:0xffaaa5b4,true);cost.setGravity(Gravity.CENTER);card.addView(cost,new LinearLayout.LayoutParams(-1,dp(20)));
            name.setSingleLine(true);name.setEllipsize(android.text.TextUtils.TruncateAt.END);
            card.setOnClickListener(v->{
                if(!unlocked){
                    showGiftEffect("VIP PREVIEW",safeName(),giftNames[idx],giftIcons[idx],1,0);
                    toast("VIP "+vipNeed+" preview only • Verified recharge is OFF");
                    return;
                }
                giftSelectedIndex=idx;refreshGiftSelection867();rebuildGiftGrid();
            });LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(133),1);cp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(card,cp);col++;
        }
        // Keep incomplete rows at four equal columns so cards never stretch larger than the others.
        if(row!=null&&col>0&&col<4){
            for(int i=col;i<4;i++){View spacer=new View(this);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(0,dp(133),1);sp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(spacer,sp);}
        }
    }
    private boolean paidGiftEvent10064(DocumentSnapshot event){
        if(event==null||!event.exists()||!event.getId().startsWith("gift_"))return false;
        Long quantity=event.getLong("giftQty"),value=event.getLong("giftValue");
        if(quantity==null||quantity<1||quantity>9||value==null||value<=0||value>1000000)return false;
        String name=event.getString("giftName");
        for(int i=0;i<KingGiftCatalog10064.NAMES.length;i++)
            if(KingGiftCatalog10064.NAMES[i].equals(name))return value==quantity*KingGiftCatalog10064.COSTS[i];
        return false;
    }
    private void refreshPartyGiftBalance10064(){
        if(db==null||user==null||giftBalanceLabel==null)return;
        final TextView label=giftBalanceLabel;final String uid=user.getUid();
        db.collection("wallets").document(uid).get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(d->{
                if(isFinishing()||isDestroyed()||user==null||!uid.equals(user.getUid())||label!=giftBalanceLabel)return;
                Object coins=d.get("coins");label.setText("💎 "+(coins instanceof Long?coins:"—"));
            }).addOnFailureListener(e->{if(label==giftBalanceLabel)label.setText("💎 unavailable");});
    }
    private void sendPaidPartyGift10064(){
        if(!cloudRoom||roomId==null||user==null||!memberSeen){toast("Join a live Party to send Diamond gifts.");return;}
        if(giftSendPending971){toast("Your gift is still being confirmed.");return;}
        final int index=giftSelectedIndex,quantity=giftQuantity;
        long total=(long)giftCosts[index]*quantity;
        if(index<0||index>=64||total<1||total>1000000){toast("Choose a smaller gift quantity.");return;}
        final String sendingRoom=roomId,uid=user.getUid(),targetName10064=giftTargetName;final int visit=presenceGeneration967;
        giftSendPending971=true;
        KingGiftPurchase10064.send(this,sendingRoom,giftTargetUid,giftNames[index],quantity,(int)total,(ok,message)->{
            giftSendPending971=false;
            if(!isActivePresence967(sendingRoom,uid,visit))return;
            // Remote members animate from the event; sender is excluded by the replay guard.
            if(ok)showGiftEffect(safeName(),targetName10064,giftNames[index],giftIcons[index],quantity,total);
            toast(message);refreshPartyGiftBalance10064();
        });
    }
    private void sendGift(String gift,int cost) { sendGiftTo(ownerUid==null?"":ownerUid,ownerName,gift,cost); }
    private void sendGiftTo(String targetUid,String targetName,String gift,int cost) {
        sendGiftQuantity(targetUid,targetName,gift,"🎁",cost,1,cost);
    }
    private boolean giftSendPending971;
    private ListenerRegistration giftReceiptListener9941;
    private boolean giftReceiptInitial9941=false;
    private final java.util.HashSet<String> seenGiftReceipts9941=new java.util.HashSet<>();
    private final java.util.LinkedHashMap<String,PendingGiftReceipt9955> receiptBacklog9955=
        new java.util.LinkedHashMap<>();
    private final android.os.Handler receiptHandler9955=
        new android.os.Handler(android.os.Looper.getMainLooper());
    private static final class PendingGiftReceipt9955 {
        final String key,uid; final DocumentReference room;
        final DocumentSnapshot event; final int generation;
        int failures; boolean inFlight; Runnable retryTask;
        PendingGiftReceipt9955(String k,DocumentReference r,DocumentSnapshot e,
                                String u,int gen){key=k;room=r;event=e;uid=u;generation=gen;}
    }
    private final java.util.HashSet<String> shownGiftIds9942=new java.util.HashSet<>();
    private final java.util.HashSet<String> inFlightGiftIds9944=new java.util.HashSet<>();
    private final KingGiftDeliveryQueue9944<PendingGift9944> pendingGiftEvents9944=
        new KingGiftDeliveryQueue9944<>();
    private static final class PendingGift9944 {
        final DocumentReference room;
        final DocumentSnapshot event;
        final boolean recipient;
        PendingGift9944(DocumentReference r,DocumentSnapshot d,boolean target){
            room=r;event=d;recipient=target;
        }
    }

    private String shownGiftRoom9942="";


    private int giftFxActive971;
    private long lastLiveEmojiSend971;

    private long lastFreeGift973;
    private void sendGiftQuantity(String targetUid,String targetName,String gift,
            String icon,int unitCost,int quantity,int totalCost){
        sendGiftQuantityVerified9950(targetUid,targetName,gift,icon,unitCost,quantity,totalCost,null);
    }
    private void sendGiftQuantityVerified9950(String targetUid,String targetName,String gift,
            String icon,int unitCost,int quantity,int totalCost,Runnable onCommitted9950){
        final String sender973=user==null?null:user.getUid();
        final long now973=System.currentTimeMillis();
        if(!cloudRoom){
            if(quantity<1||quantity>KingFreeGift973.MAX_QTY){toast("FREE gift quantity: 1 to 9");return;}
            // This is explicitly an offline preview, never a charged gift.
            showGiftEffect(safeName(),targetName,gift,icon,quantity,0);
            toast("FREE Gift preview • no coins or VIP points");
            return;
        }
        if(!KingFreeGift973.allowed(sender973,targetUid,quantity,lastFreeGift973,
                now973,memberSeen&&db!=null&&!isFinishing()&&!isDestroyed())){
            toast("Choose another Party member; max 9 FREE gifts, 1.5 seconds between sends");
            return;
        }
        // Verify the intended recipient exists in this room on SERVER, not in
        // possibly-stale local cached member lists. No fictitious "delivered" gift.
        if(giftSendPending971){toast("Previous gift is still synchronizing");return;}
        giftSendPending971=true;
        final String giftRoom9937=roomId,giftUid9937=sender973;
        final int giftVisit9937=presenceGeneration967;
        db.collection("live_rooms").document(giftRoom9937)
          .collection("members").document(targetUid)
          .get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(member->{
            if(!isActivePresence967(giftRoom9937,giftUid9937,giftVisit9937)){
                giftSendPending971=false;return;
            }
            if(member==null||!member.exists()
                    ||!targetUid.equals(member.getString("uid"))){
                giftSendPending971=false;
                toast("Gift not sent: recipient must join this live Party first");
                return;
            }
            lastFreeGift973=System.currentTimeMillis();
            addGiftEvent(targetUid,targetName,gift,icon,0,quantity,0,onCommitted9950);
          })
          .addOnFailureListener(e->{
            giftSendPending971=false;
            if(isActivePresence967(giftRoom9937,giftUid9937,giftVisit9937))
                toast("Could not verify gift recipient: "+msg(e));
          });
    }
    private void addGiftEvent(String targetUid,String targetName,String gift,String icon,
                              int unitCost,int quantity,int totalCost,Runnable onCommitted9950){
        if(!cloudRoom||user==null||db==null||roomId==null||!memberSeen)return;
        final String requestedRoom971=roomId,expectedUid971=user.getUid();
        final int generation971=presenceGeneration967;
        String actor971=safeName();
        String recipient971=targetName==null||targetName.trim().isEmpty()?"Member":targetName;
        String text971=actor971+" sent FREE "+KingGiftSafety971.giftMessage(gift,icon,quantity)+" to "+recipient971;
        if(text971.length()>300)text971=text971.substring(0,300);
        LevelSystem.Snapshot progress971=LevelSystem.read(this);
        Map<String,Object> event971=new HashMap<>();
        event971.put("actorUid",expectedUid971);
        event971.put("actorName",actor971);
        event971.put("actorLevel",progress971.level);
        event971.put("actorVip",0);
        event971.put("type","gift");
        event971.put("text",text971);
        final String giftDeliveryId9937=java.util.UUID.randomUUID().toString();
        event971.put("giftId",giftDeliveryId9937);
        event971.put("giftName",gift);
        event971.put("giftIcon",icon);
        event971.put("freeGift",true);
        event971.put("giftValue",0);
        event971.put("giftQty",quantity);
        event971.put("giftUnitCost",0);
        event971.put("targetUid",targetUid);
        event971.put("targetName",recipient971);
        event971.put("createdAt",FieldValue.serverTimestamp());
        Map<String,Object> message971=new HashMap<>();
        message971.put("senderUid",expectedUid971);
        message971.put("senderName",actor971);
        message971.put("senderLevel",progress971.level);
        message971.put("senderVip",0);
        message971.put("text",text971);
        message971.put("type","gift");
        message971.put("giftId",giftDeliveryId9937);
        message971.put("giftName",gift);
        message971.put("giftIcon",icon);
        message971.put("giftValue",totalCost);
        message971.put("giftQty",quantity);
        message971.put("targetUid",targetUid);
        message971.put("targetName",recipient971);
        message971.put("createdAt",FieldValue.serverTimestamp());
        DocumentReference room971=db.collection("live_rooms").document(requestedRoom971);
        WriteBatch batch971=db.batch();
        batch971.set(room971.collection("events").document(giftDeliveryId9937),event971);
        batch971.set(room971.collection("messages").document(giftDeliveryId9937),message971);
        batch971.commit().addOnSuccessListener(v->{
            giftSendPending971=false;
            // Only the SERVER-accepted atomic event+chat spends free Backpack stock.
            if(onCommitted9950!=null)onCommitted9950.run();
            if(isActivePresence967(requestedRoom971,expectedUid971,generation971)){
                showGiftEffect(actor971,recipient971,gift,icon,quantity,0);
                toast("FREE Gift sent to Party • awaiting recipient phone acknowledgement");
            }
        }).addOnFailureListener(e->{
            giftSendPending971=false;
            if(isActivePresence967(requestedRoom971,expectedUid971,generation971))
                toast("FREE gift was not sent: "+msg(e));
        });
    }
    /** Only the exact receiving member can write this Firestore receipt.
     *  Firestore rules cross-check the immutable matching Gift event.
     */
    // As a user returns from a game, call, or screen lock, reload recent Gift
    // events from SERVER. Old static initial-snapshot dedupe must not eat Gifts.
    private void recoverRecentGifts9942(){
        if(!cloudRoom||roomId==null||user==null||db==null||!memberSeen
           ||!partyVisualForeground995||partyShell940==null)return;
        final String recoveringRoom9942=roomId, recoveringUid9942=user.getUid();
        final int gen9942=presenceGeneration967;
        final DocumentReference room9942=db.collection("live_rooms").document(recoveringRoom9942);
        room9942.collection("events")
          .orderBy("createdAt",Query.Direction.DESCENDING).limit(60)
          .get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(events->{
             if(!isActivePresence967(recoveringRoom9942,recoveringUid9942,gen9942)
                 ||!partyVisualForeground995||events==null)return;
             for(DocumentSnapshot event:events.getDocuments())
                 if("gift".equals(event.getString("type")))
                     displayConfirmedGift9942(room9942,event,true);
          }).addOnFailureListener(error->KingStability.nonFatal(this,"gift-reconnect",error));
    }

    // ACK only a real, fresh, server-confirmed event while Party UI is
    // actually foregrounded. Suppress duplicate sounds/effects per event ID.
    private void displayConfirmedGift9942(DocumentReference room,DocumentSnapshot event,boolean replay){
        if(room==null||event==null||user==null||!cloudRoom||!memberSeen
           ||!partyVisualForeground995||partyShell940==null||liveEmojiStageV530==null
           ||liveEmojiStageV530.getParent()==null||event.getMetadata().hasPendingWrites()
           ||!"gift".equals(event.getString("type")))return;
        String ownUid9942=user.getUid();
        String sender9942=event.getString("actorUid");
        String recipient9942=event.getString("targetUid");
        boolean isRecipient9942=ownUid9942.equals(recipient9942);
        if(replay&&!isRecipient9942)return;
        // A freshly restored Activity can receive the same SERVER Gift again.
        // Suppress duplicate receiver sound/effect ONLY after actual playback.
        if(isRecipient9942&&alreadyPlayedRecipientGift9969(
              room.getId(),event.getId(),ownUid9942))return;
        com.google.firebase.Timestamp created9942=event.getTimestamp("createdAt");
        if(created9942==null||
           !KingGiftReplayGuard9942.allow(System.currentTimeMillis(),
               created9942.toDate().getTime(),sender9942,ownUid9942,replay,isRecipient9942))return;
        if(!room.getId().equals(shownGiftRoom9942)){
            shownGiftRoom9942=room.getId();shownGiftIds9942.clear();
        }
        if(!shownGiftIds9942.add(event.getId()))return;
        if(shownGiftIds9942.size()>220){
            shownGiftIds9942.clear();shownGiftIds9942.add(event.getId());
        }
        String name9942=str(event,"giftName","Gift");
        Long count9942=event.getLong("giftQty"),value9942=event.getLong("giftValue");
        enqueueConfirmedGift9944(room,event,isRecipient9942);
    }

    /** A server-confirmed Gift waits for a real foreground animation slot. */
    private void enqueueConfirmedGift9944(DocumentReference room,DocumentSnapshot event,boolean recipient){
        if(room==null||event==null||roomId==null||!room.getId().equals(roomId)){
            if(event!=null)shownGiftIds9942.remove(event.getId());return;
        }
        if(!pendingGiftEvents9944.offer(event.getId(),new PendingGift9944(room,event,recipient))){
            // No fictitious delivery receipt when animation playback is saturated.
            shownGiftIds9942.remove(event.getId());
            return;
        }
        drainPendingGifts9944();
    }

    /** Bounded playback queue, one animation+sound per actual visible Gift. */
    private void drainPendingGifts9944(){
        if(!cloudRoom||!memberSeen||!partyVisualForeground995||partyUiDead()
           ||user==null||roomId==null||liveEmojiStageV530==null
           ||liveEmojiStageV530.getParent()==null)return;
        while(giftFxActive971<3&&!pendingGiftEvents9944.isEmpty()){
            final KingGiftDeliveryQueue9944.Entry<PendingGift9944> entry=pendingGiftEvents9944.poll();
            if(entry==null)break;
            final PendingGift9944 next=entry.payload;
            if(next==null||next.room==null||next.event==null
                ||!roomId.equals(next.room.getId())){
                shownGiftIds9942.remove(entry.id);continue;
            }
            final com.google.firebase.Timestamp created=next.event.getTimestamp("createdAt");
            if(created==null||!KingGiftReplayGuard9942.allow(
                 System.currentTimeMillis(),created.toDate().getTime(),
                 next.event.getString("actorUid"),user.getUid(),false,next.recipient)){
                shownGiftIds9942.remove(entry.id);continue;
            }
            final String expectedRoom=roomId,expectedUid=user.getUid();
            final int expectedGen=presenceGeneration967;
            final String name=str(next.event,"giftName","Gift");
            Long qty=next.event.getLong("giftQty"),value=next.event.getLong("giftValue");
            inFlightGiftIds9944.add(entry.id);
            boolean actuallyStarted=showGiftEffectConfirmed9944(
                str(next.event,"actorName","User"),str(next.event,"targetName","Member"),
                name,str(next.event,"giftIcon",giftIconFor(name)),
                qty==null?1:qty,value==null?0:value,()->{
                    inFlightGiftIds9944.remove(entry.id);
                    // Receipt is written AFTER GiftBurstView completes successfully.
                    if(next.recipient&&isActivePresence967(expectedRoom,expectedUid,expectedGen)
                       &&partyVisualForeground995&&!partyUiDead()&&memberSeen)
                        confirmGiftReceived9941(next.room,next.event,expectedUid);
                    else if(!partyVisualForeground995||!memberSeen||partyUiDead())
                        shownGiftIds9942.remove(entry.id);
                    if(isActivePresence967(expectedRoom,expectedUid,expectedGen))
                        drainPendingGifts9944();
                });
            if(!actuallyStarted){
                inFlightGiftIds9944.remove(entry.id);
                shownGiftIds9942.remove(entry.id);
                // Playback stage may not be ready; caller will retry via
                // recoverRecentGifts9942(), without acknowledging an unseen Gift.
                break;
            }
        }
    }

    /** A gift is put in this backlog ONLY after its on-screen burst completes.
     * Failures of the Firestore receipt write must not be lost to gift-ID
     * visual deduplication, nor may retries replay a sound/animation.
     */
    /** Durable local record written ONLY after real recipient burst playback.
     * The Firestore event and current room membership are re-read from SERVER.
     * Local preferences are not trusted payment proof or a wallet balance.
     */
    private static final String GIFT_JOURNAL9962="king_gift_receipt_journal_v9962";
    private static final String GIFT_PENDING9962="pending";
    private final java.util.HashSet<String> restoringGiftIds9962=new java.util.HashSet<>();
    private android.content.SharedPreferences giftPrefs9962(){
        return getSharedPreferences(GIFT_JOURNAL9962,MODE_PRIVATE);
    }
    private org.json.JSONObject giftRecord9962(String room,String id,String uid){
        org.json.JSONObject o=new org.json.JSONObject();
        try{
            o.put("room",room);o.put("gift",id);o.put("uid",uid);
            o.put("at",System.currentTimeMillis());
        }catch(org.json.JSONException ignored){}
        return o;
    }
    private java.util.HashSet<String> storedGiftRecords9962(){
        java.util.Set<String> stored=giftPrefs9962().getStringSet(
            GIFT_PENDING9962,java.util.Collections.<String>emptySet());
        return new java.util.HashSet<>(stored);
    }
    private boolean storeGiftReceipt9962(String room,String gift,String uid){
        if(!KingGiftReceiptDurable9962.valid(room,gift,uid))return false;
        java.util.HashSet<String> all=storedGiftRecords9962();
        for(String entry:all){
            try{
                org.json.JSONObject obj=new org.json.JSONObject(entry);
                if(KingGiftReceiptDurable9962.same(room,gift,uid,
                    obj.optString("room"),obj.optString("gift"),obj.optString("uid")))
                    return true; // same physical on-screen Gift ACK, never duplicate
            }catch(org.json.JSONException ignored){}
        }
        if(!KingGiftReceiptDurable9962.canAdd(all.size()))return false;
        all.add(giftRecord9962(room,gift,uid).toString());
        // Synchronous commit: survive process death immediately after playback.
        return giftPrefs9962().edit().putStringSet(GIFT_PENDING9962,all).commit();
    }
    private void removeGiftReceipt9962(String room,String gift,String uid){
        java.util.HashSet<String> all=storedGiftRecords9962();
        boolean changed=false;
        for(String entry:new java.util.HashSet<>(all)){
            try{
                org.json.JSONObject obj=new org.json.JSONObject(entry);
                if(KingGiftReceiptDurable9962.same(room,gift,uid,
                    obj.optString("room"),obj.optString("gift"),obj.optString("uid"))){
                    all.remove(entry);changed=true;
                }
            }catch(org.json.JSONException ignored){}
        }
        if(changed)giftPrefs9962().edit().putStringSet(GIFT_PENDING9962,all).commit();
    }
    private boolean recoverReceiptContext9962(String id,String uid){
        return cloudRoom&&memberSeen&&partyVisualForeground995&&db!=null&&user!=null
            &&roomId!=null&&roomId.equals(id)&&uid.equals(user.getUid())
            &&!isFinishing()&&!isDestroyed();
    }
    /** After reopen, replay ACK not animation. Always verify original gift and
     * receiver membership online. Failed reads leave the durable record intact.
     */
    private void recoverDurableGiftReceipts9962(){
        if(!cloudRoom||!memberSeen||!partyVisualForeground995||user==null||db==null
             ||roomId==null||roomId.isEmpty())return;
        final String current9962=roomId,uid9962=user.getUid();
        final DocumentReference room9962=db.collection("live_rooms").document(current9962);
        for(String record:storedGiftRecords9962()){
            final org.json.JSONObject obj;
            try{obj=new org.json.JSONObject(record);}
            catch(org.json.JSONException ignored){continue;}
            final String storedRoom=obj.optString("room");
            final String giftId=obj.optString("gift");
            final String recipient=obj.optString("uid");
            if(!KingGiftReceiptDurable9962.valid(storedRoom,giftId,recipient)
                ||!current9962.equals(storedRoom)||!uid9962.equals(recipient))continue;
            final String key=current9962+"/"+giftId;
            if(receiptBacklog9955.containsKey(key)||!restoringGiftIds9962.add(key))continue;
            room9962.collection("members").document(uid9962)
                .get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(member->{
                    if(!recoverReceiptContext9962(current9962,uid9962)
                       ||member==null||!member.exists()
                       ||!uid9962.equals(member.getString("uid"))){
                        restoringGiftIds9962.remove(key);return;
                    }
                    room9962.collection("events").document(giftId)
                      .get(com.google.firebase.firestore.Source.SERVER)
                      .addOnSuccessListener(event->{
                          restoringGiftIds9962.remove(key);
                          if(!recoverReceiptContext9962(current9962,uid9962))return;
                          // A device journal is untrusted. Never acknowledge a
                          // different recipient, nonexistent gift or own gift.
                          if(event==null||!event.exists())return;
                          Long count9968=event.getLong("giftQty");
                          if(!KingGiftReturnGate9968.validServerGift(
                                giftId,event.getString("giftId"),
                                event.getString("type"),
                                (Boolean.TRUE.equals(event.getBoolean("freeGift"))||paidGiftEvent10064(event)),
                                count9968==null?0:count9968.longValue(),
                                event.getString("actorUid"),
                                event.getString("targetUid"),uid9962))return;
                          confirmGiftReceived9941(room9962,event,uid9962);
                      }).addOnFailureListener(e->{
                          restoringGiftIds9962.remove(key);
                          KingStability.nonFatal(this,"gift-journal-event",e);
                      });
                }).addOnFailureListener(e->{
                    restoringGiftIds9962.remove(key);
                    KingStability.nonFatal(this,"gift-journal-member",e);
                });
        }
    }
    /** An on-screen finished Gift stays remembered on this account/room,
     * even after the Party Activity is destroyed. Local records do not
     * authorize any payment or new receipt; SERVER still verifies the gift.
     */
    private static final String GIFT_PLAYED_PREF9969="king_gift_recipient_seen_v9969";
    private static final String GIFT_PLAYED_KEY9969="seen";
    private java.util.HashSet<String> playedGiftRecords9969(){
        return new java.util.HashSet<>(getSharedPreferences(
            GIFT_PLAYED_PREF9969,MODE_PRIVATE).getStringSet(GIFT_PLAYED_KEY9969,
            java.util.Collections.<String>emptySet()));
    }
    private boolean alreadyPlayedRecipientGift9969(String room,String gift,String uid){
        if(!KingGiftPlayedHistory9969.valid(room,gift,uid))return false;
        // Pending durable journal contains only completed receiver playback,
        // not an event merely sent to Firestore.
        for(String entry:storedGiftRecords9962()){
            try{
                org.json.JSONObject d=new org.json.JSONObject(entry);
                if(KingGiftPlayedHistory9969.same(room,gift,uid,
                     d.optString("room"),d.optString("gift"),d.optString("uid")))
                    return true;
            }catch(org.json.JSONException ignored){}
        }
        long now=System.currentTimeMillis();
        for(String entry:playedGiftRecords9969()){
            try{
                org.json.JSONObject d=new org.json.JSONObject(entry);
                if(KingGiftPlayedHistory9969.withinHistory(now,d.optLong("at"))
                  &&KingGiftPlayedHistory9969.same(room,gift,uid,
                     d.optString("room"),d.optString("gift"),d.optString("uid")))
                    return true;
            }catch(org.json.JSONException ignored){}
        }
        return false;
    }
    /** Called ONLY on successful real recipient SERVER receipt. Commit
     * before removing the pending ACK, avoiding a replay on process death.
     */
    private boolean rememberReceivedGift9969(String room,String gift,String uid){
        if(!KingGiftPlayedHistory9969.valid(room,gift,uid))return false;
        long now=System.currentTimeMillis();
        java.util.HashSet<String> seen=playedGiftRecords9969();
        String oldest=null;long oldestTime=Long.MAX_VALUE;
        for(String entry:new java.util.HashSet<>(seen)){
            try{
                org.json.JSONObject d=new org.json.JSONObject(entry);
                long at=d.optLong("at");
                if(!KingGiftPlayedHistory9969.withinHistory(now,at)
                  ||KingGiftPlayedHistory9969.same(room,gift,uid,
                     d.optString("room"),d.optString("gift"),d.optString("uid"))){
                    seen.remove(entry);continue;
                }
                if(at<oldestTime){oldestTime=at;oldest=entry;}
            }catch(org.json.JSONException ignored){seen.remove(entry);}
        }
        if(seen.size()>=KingGiftPlayedHistory9969.MAX_EVENTS&&oldest!=null)
            seen.remove(oldest);
        org.json.JSONObject item=new org.json.JSONObject();
        try{
            item.put("room",room);item.put("gift",gift);item.put("uid",uid);
            item.put("at",now);
        }catch(org.json.JSONException ignored){return false;}
        seen.add(item.toString());
        return getSharedPreferences(GIFT_PLAYED_PREF9969,MODE_PRIVATE).edit()
            .putStringSet(GIFT_PLAYED_KEY9969,seen).commit();
    }
    private void confirmGiftReceived9941(DocumentReference room,DocumentSnapshot event,String uid){
        if(room==null||event==null||!event.exists()||uid==null||uid.isEmpty()||db==null)return;
        String id=event.getId(),sender=event.getString("actorUid");
        if(sender==null||sender.isEmpty()||uid.equals(sender)
            ||!uid.equals(event.getString("targetUid")))return;
        String key9955=room.getId()+"/"+id;
        if(receiptBacklog9955.containsKey(key9955))return;
        // v9.9.62 wrongly returned at the 64-job in-memory limit BEFORE
        // journaling the actual completed GiftBurst. Preserve that display
        // first so reconnect/process-kill can safely finish the ACK later.
        boolean journaled9962=storeGiftReceipt9962(room.getId(),id,uid);
        if(!journaled9962)KingStability.nonFatal(this,"gift-journal-save",
            new IllegalStateException("Playback confirmation not durable"));
        if(!KingGiftBurstBackpressure9963.canSendNow(receiptBacklog9955.size())){
            KingStability.nonFatal(this,"gift-receipt-inmemory-full",
                new IllegalStateException("Gift receipt saved, will drain when in-memory slot opens"));
            return;
        }
        PendingGiftReceipt9955 job9955=new PendingGiftReceipt9955(
            key9955,room,event,uid,presenceGeneration967);
        // Real on-screen GiftBurst was recorded before the capacity gate.
        receiptBacklog9955.put(key9955,job9955);
        attemptDisplayedGiftReceipt9955(job9955);
    }
    private boolean receiptContext9955(PendingGiftReceipt9955 job){
        return job!=null&&receiptBacklog9955.get(job.key)==job
            &&cloudRoom&&memberSeen&&db!=null&&user!=null
            &&roomId!=null&&roomId.equals(job.room.getId())
            &&job.uid.equals(user.getUid())&&job.generation==presenceGeneration967
            &&!isFinishing()&&!isDestroyed();
    }
    private void pauseDisplayedGiftReceipts9955(){
        for(PendingGiftReceipt9955 job:new java.util.ArrayList<>(receiptBacklog9955.values())){
            if(job.retryTask!=null){receiptHandler9955.removeCallbacks(job.retryTask);
                job.retryTask=null;}
        }
    }
    private void clearDisplayedGiftReceipts9955(){
        pauseDisplayedGiftReceipts9955();
        receiptBacklog9955.clear();
        // In-flight async tasks belong to the prior room generation.
        restoringGiftIds9962.clear();
        // Persisted items are deliberately NOT discarded on exit/destroy.
        // They may be retried after same account rejoins that Party.
    }
    private void retryDisplayedGiftReceipts9955(){
        if(!partyVisualForeground995||!memberSeen)return;
        // Recover displayed-but-not-yet-ACKed gifts even after process death.
        // Never replay visuals or grant diamonds from journal entries.
        recoverDurableGiftReceipts9962();
        for(PendingGiftReceipt9955 job:new java.util.ArrayList<>(receiptBacklog9955.values())){
            if(!receiptContext9955(job))continue;
            if(job.failures>=KingGiftReceiptRetry9955.MAX_RETRIES)job.failures=0;
            if(!job.inFlight&&job.retryTask==null)
                attemptDisplayedGiftReceipt9955(job);
        }
    }
    private void finishedDisplayedGiftReceipt9955(PendingGiftReceipt9955 job){
        if(receiptBacklog9955.get(job.key)!=job)return;
        if(job.retryTask!=null)receiptHandler9955.removeCallbacks(job.retryTask);
        receiptBacklog9955.remove(job.key);
        // A completed Gift ACK is the only event that can enter the bounded
        // replay history. If the disk write fails, keep the durable pending
        // journal; an ACK retry will still be possible after process restart.
        if(rememberReceivedGift9969(job.room.getId(),job.event.getId(),job.uid))
            removeGiftReceipt9962(job.room.getId(),job.event.getId(),job.uid);
        else KingStability.nonFatal(this,"gift-visual-history-save",
            new IllegalStateException("Keeping durable Gift ACK pending"));
        // A slot opened: drain genuinely displayed overflow Gift ACKs.
        // Server still verifies membership, gift event and recipient.
        if(partyVisualForeground995&&memberSeen)
            recoverDurableGiftReceipts9962();
    }
    private void retryFailureDisplayedGiftReceipt9955(PendingGiftReceipt9955 job,Exception failure){
        job.inFlight=false;
        if(!receiptContext9955(job))return;
        KingStability.nonFatal(this,"gift-receipt-retry",failure);
        if(!partyVisualForeground995||job.retryTask!=null)return;
        if(!KingGiftReceiptRetry9955.shouldRetry(job.failures,true,true)){
            toast("Gift animation shown • receipt not synced. Reopen Party to retry.");
            return;
        }
        job.failures++;
        final long wait9955=KingGiftReceiptRetry9955.delayMs(job.failures);
        job.retryTask=()->{
            job.retryTask=null;
            if(receiptContext9955(job)&&partyVisualForeground995)
                attemptDisplayedGiftReceipt9955(job);
        };
        receiptHandler9955.postDelayed(job.retryTask,wait9955);
    }
    private void attemptDisplayedGiftReceipt9955(PendingGiftReceipt9955 job){
        if(job.inFlight||!receiptContext9955(job)||!partyVisualForeground995)return;
        job.inFlight=true;
        final String id=job.event.getId(),sender=job.event.getString("actorUid");
        if(sender==null||sender.isEmpty()||!job.uid.equals(job.event.getString("targetUid"))){
            job.inFlight=false;finishedDisplayedGiftReceipt9955(job);return;
        }
        final DocumentReference receipt=job.room.collection("gift_receipts").document(id);
        receipt.get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(existing->{
              if(!receiptContext9955(job)){job.inFlight=false;return;}
              if(existing!=null&&existing.exists()){
                  job.inFlight=false;
                  if(id.equals(existing.getString("giftId"))&&
                      job.uid.equals(existing.getString("recipientUid"))&&
                      sender.equals(existing.getString("senderUid")))
                      finishedDisplayedGiftReceipt9955(job);
                  else KingStability.nonFatal(this,"gift-receipt-conflict",
                      new IllegalStateException("Existing receipt mismatched"));
                  return;
              }
              java.util.Map<String,Object> data=new HashMap<>();
              data.put("giftId",id);data.put("senderUid",sender);
              data.put("recipientUid",job.uid);
              data.put("giftName",str(job.event,"giftName","Gift"));
              data.put("createdAt",FieldValue.serverTimestamp());
              receipt.set(data).addOnSuccessListener(v->{
                  job.inFlight=false;
                  finishedDisplayedGiftReceipt9955(job);
              }).addOnFailureListener(e->retryFailureDisplayedGiftReceipt9955(job,e));
          }).addOnFailureListener(e->retryFailureDisplayedGiftReceipt9955(job,e));
    }
    private void awardGiftProgress700(long value){LevelSystem.Snapshot before=LevelSystem.read(this),after=LevelSystem.gift(this,value);if(after.level>before.level)toast("⭐ TEST activity level • Lv."+after.level);/* Local TEST preview only; no cloud profile, XP, VIP, Noble, or reward credit. */}

    private void shareRoom(){
        if(!cloudRoom || roomId==null || roomId.trim().isEmpty()){
            toast("Open a live Party room before sharing.");
            return;
        }
        String link959=KingRoomInvite959.link(roomId);
        if(link959.isEmpty()){toast("Room ID is invalid for sharing");return;}
        String message959="Join my KING Plus Party: "+(roomName==null?"Live Party":roomName)
            +"\nOpen room: "+link959
            +"\nRoom Code: "+shortId()
            +"\nKINGROOM:"+roomId
            +"\n\nInstall/open KING Plus, sign in, and tap the link. If the link is not clickable, Party → Join by link / ID and paste this message."
            +"\nPrivate/password rooms still require host approval or their existing access checks.";
        Intent share959=new Intent(Intent.ACTION_SEND);
        share959.setType("text/plain");
        share959.putExtra(Intent.EXTRA_TEXT,message959);
        try{startActivity(Intent.createChooser(share959,"Share Party Room"));}
        catch(Exception e){KingStability.nonFatal(this,"share-party-invite-959",e);toast("No app available to share Party");}
    }

    private void joinRoomByCode964(String code964){
        if(db==null||user==null){toast("Sign in before joining Party");return;}
        if(!KingIdentity964.joinCode(code964)){toast("Enter a valid 6-digit Room Code");return;}
        KingCrashWatch958.mark(this,"party-room-code-lookup-964");
        db.collection("live_rooms").whereEqualTo("joinCode",code964).limit(10).get()
            .addOnSuccessListener(result964->{
                if(isFinishing()||isDestroyed())return;
                java.util.ArrayList<DocumentSnapshot> live964=new java.util.ArrayList<>();
                if(result964!=null)for(DocumentSnapshot d964:result964.getDocuments()){
                    if(d964.exists()&&code964.equals(d964.getString("joinCode"))
                       &&!Boolean.TRUE.equals(d964.getBoolean("closed")))live964.add(d964);
                }
                if(live964.isEmpty()){toast("No active Party matches that code. Ask the host to share the full invite link.");return;}
                if(live964.size()==1){openRoomDoc891(live964.get(0));return;}
                String[] choices964=new String[live964.size()];
                for(int k964=0;k964<live964.size();k964++){
                    DocumentSnapshot d964=live964.get(k964);
                    choices964[k964]=str(d964,"name","Live Party")+" • "+str(d964,"ownerName","Host")
                        +" • "+d964.getId().substring(0,Math.min(8,d964.getId().length()));
                }
                new AlertDialog.Builder(this).setTitle("Multiple Party rooms use this short code")
                    .setMessage("Choose the host and Party name, or ask for the exact invitation link.")
                    .setItems(choices964,(dialog,index)->openRoomDoc891(live964.get(index)))
                    .setNegativeButton("Cancel",null).show();
            })
            .addOnFailureListener(e->{if(!isFinishing()&&!isDestroyed())toast("Room lookup failed: "+msg(e));});
    }

    private boolean tryJoinRoomInput959(String input959){
        final String candidate964=input959==null?"":input959.trim();
        if(KingIdentity964.joinCode(candidate964)){joinRoomByCode964(candidate964);return true;}
        final String id959=KingRoomInvite959.parse(input959);
        if(id959.isEmpty())return false;
        if(user==null||db==null){toast("Sign in first to join Party rooms");return true;}
        KingCrashWatch958.mark(this,"party-join-invite-959");
        db.collection("live_rooms").document(id959).get()
            .addOnSuccessListener(doc959->{
                if(isFinishing()||isDestroyed())return;
                if(doc959==null||!doc959.exists()||Boolean.TRUE.equals(doc959.getBoolean("closed"))){
                    toast("Party room not found or already closed");return;
                }
                openRoomDoc891(doc959); // existing private/ban/password enforcement
            }).addOnFailureListener(e->{
                if(!isFinishing()&&!isDestroyed())toast("Could not look up Party room: "+msg(e));
            });
        return true;
    }

    private void joinRoomLinkDialog959(){
        EditText box959=new EditText(this);
        box959.setSingleLine(true);
        box959.setHint("6-digit code, full ID, or kingplus://party/...");
        box959.setSelectAllOnFocus(true);
        new AlertDialog.Builder(this).setTitle("🔗 Join Party by link / ID")
            .setMessage("Enter the 6-digit room code, full Room ID, or shared invitation.")
            .setView(box959)
            .setPositiveButton("Join",(d,w)->{
                if(!tryJoinRoomInput959(box959.getText().toString().trim()))
                    toast("Paste a valid full Room ID or KING Plus invite");
            }).setNegativeButton("Cancel",null).show();
    }
    private void pkBattle(){
        if(db==null||!cloudRoom||!memberSeen||user==null||roomId==null){toast("Room Battle needs a joined live Firebase Party");return;}
        if(!isModerator()){if("battle".equals(activeGameType))new AlertDialog.Builder(this).setTitle("⚔ Room Battle").setMessage(activeGameResult.isEmpty()?activeGamePrompt:activeGameResult).setPositiveButton("OK",null).show();else toast("Host/co-host starts Room Battle");return;}
        db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(100).get().addOnSuccessListener(q->{long hostScore=0,guestScore=0;for(DocumentSnapshot e:q.getDocuments()){Long v=e.getLong("giftValue");long score=v==null?1:Math.max(1,v);String actor=e.getString("actorUid");if(ownerUid!=null&&ownerUid.equals(actor))hostScore+=score;else guestScore+=score;}String result="Team Host  "+compactNumber(hostScore)+"  vs  Room  "+compactNumber(guestScore)+"\n\n"+(hostScore>=guestScore?"🏆 Host Team leads":"🏆 Room Team leads");publishGameState("battle","Room Battle",result);new AlertDialog.Builder(this).setTitle("⚔ Room Battle").setMessage(result).setPositiveButton("Refresh",(d,w)->pkBattle()).setNegativeButton("Close",null).show();}).addOnFailureListener(e->toast("Battle score unavailable: "+msg(e)));
    }

    private void toolsPanel(){
        ScrollView sv=new ScrollView(this);sv.setFillViewport(true);sv.setVerticalScrollBarEnabled(false);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(16),dp(14),dp(16),dp(22));root.setBackgroundColor(0xff242333);sv.addView(root);
        TextView playTitle=tv("Play Center",22,Color.WHITE,true);root.addView(playTitle,new LinearLayout.LayoutParams(-1,dp(48)));
        String[] pIcons={"⭐","🌷","🥊","💣","📦","🎡","🎯","⚔️","💌"};
        String[] pLabels={"Counter","Guess It","Truth or Dare","Pass the Bomb","Fan Box","Wheel Challenge","Party Wheel","Room Battle","Vote"};
        addToolGrid(root,pIcons,pLabels,true);
        TextView toolsTitle=tv("Tools",22,Color.WHITE,true);LinearLayout.LayoutParams ttp=new LinearLayout.LayoutParams(-1,dp(52));ttp.topMargin=dp(12);root.addView(toolsTitle,ttp);
        boolean queueOn=cloudRoom?false:prefs.getBoolean("queue_"+roomId,false);
        String[] icons={"🎟","🪑","✋","💬","🎵","🎨","📋","👑","🏰","🛡","📊","🎤","📻","🎁","💫","🔎","📢","🎙","📹","🎁","🏆","🗨","🎒","🌐","💞","⚔️","🔁","🎮","📦","✨","🎤","⚔️","🎲","🏰","💎","🚀","🧪"};
        String[] labels={"Events","Room seat",queueOn?"Queue ON":"Enable queue","Private chat","Music","Atmosphere","Income","Share Family","Theme Room","Party Master","Party Data","KTV Queue","Radio Mic","Lucky Gift","Gift Wish","Find User","Notice",micOn?"Mic OFF":"Mic ON","Multi Video","Gift Wall","Gift Rank","Chat History","Backpack","Community","Relationship","Audio PK","Loop Mic","Room Games","Asset Pack","Parity Center","KTV Stage","PK Arena","Match","Party Stage","VIP Rank","Ecosystem Pro","Production Test"};
        addToolGrid(root,icons,labels,false);
        AlertDialog dialog=new AlertDialog.Builder(this).setView(sv).create();
        dialog.setOnShowListener(v->{android.view.Window w=dialog.getWindow();if(w!=null){w.setLayout(-1,(int)(getResources().getDisplayMetrics().heightPixels*.52f));w.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);w.setGravity(Gravity.BOTTOM);w.setBackgroundDrawable(bg(0xff242333,22));}});
        dialog.show();
    }

    private void addToolGrid(LinearLayout host,String[] icons,String[] labels,boolean playCenter){
        int index=0;
        while(index<labels.length){
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);
            for(int c=0;c<5;c++){
                if(index>=labels.length){View spacer=new View(this);row.addView(spacer,new LinearLayout.LayoutParams(0,dp(94),1));continue;}
                final String label=labels[index];final String icon=icons[index];
                TextView tile=toolTile(icon,label,()->{if(playCenter)runPlayCenter(label);else runRoomTool(label);});
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(94),1);lp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(tile,lp);index++;
            }
            host.addView(row,new LinearLayout.LayoutParams(-1,dp(100)));
        }
    }

    private void runRoomTool(String label){
        if("Events".equals(label))activityCenterPanel();
        else if("Room seat".equals(label))roomSeatPanel();
        else if(label.contains("queue")||label.contains("Queue"))toggleSeatQueue();
        else if("Private chat".equals(label))privateChatDialog();
        else if("Music".equals(label))musicPanel();
        else if("Atmosphere".equals(label))atmospherePanel();
        else if("Income".equals(label))giftHistoryDialog();
        else if("Share Family".equals(label))shareFamilyRoom();
        else if("Theme Room".equals(label))themeRoomPanel();
        else if("Party Master".equals(label))partyMasterPanel();
        else if("Party Data".equals(label))partyDataPanel();
        else if("KTV Queue".equals(label))ktvQueuePanel();
        else if("Radio Mic".equals(label))radioMicPanel();
        else if("Lucky Gift".equals(label))luckyGiftPanel();
        else if("Gift Wish".equals(label))giftWishPanel();
        else if("Find User".equals(label))findRoomUserPanel();
        else if("Notice".equals(label))roomNoticePanel();
        else if("Mic ON".equals(label)||"Mic OFF".equals(label))toggleMic();
        else if("Multi Video".equals(label))openVideoRoom();
        else if("Gift Wall".equals(label))giftWall620();
        else if("Gift Rank".equals(label))roomRankingDialog();
        else if("Chat History".equals(label))roomChatHistory620();
        else if("Backpack".equals(label))backpackGiftDialog700();
        else if("Community".equals(label))openCommunityHub700("Search");
        else if("Relationship".equals(label))openCommunityHub700("Relationship");
        else if("Audio PK".equals(label))audioPkPanel700();
        else if("Loop Mic".equals(label))loopMicPanel700();
        else if("Room Games".equals(label))openRoomGames740();
        else if("Asset Pack".equals(label))startActivity(new Intent(this,AssetPackActivity.class));
        else if("Parity Center".equals(label))openParity870("home");
        else if("KTV Stage".equals(label))ktvQueuePanel();
        else if("PK Arena".equals(label))audioPkPanel700();
        else if("Match".equals(label))openRoomGames740();
        else if("Party Stage".equals(label))roomSeatPanel();
        else if("VIP Rank".equals(label))roomRankingDialog();
        else if("Ecosystem Pro".equals(label))openEcosystem880("home");
        else if("Production Test".equals(label))openProductionTest920();
    }

    private void runPlayCenter(String label){
        if("Room Battle".equals(label)){pkBattle();return;}
        if("Vote".equals(label)){votePanel();return;}
        if("Truth or Dare".equals(label)){truthOrDare();return;}
        if("Guess It".equals(label)){guessItPanel();return;}
        if("Counter".equals(label)){counterPanel();return;}
        if("Pass the Bomb".equals(label)){passBombPanel();return;}
        if("Fan Box".equals(label)){fanBoxPanel();return;}
        if("Wheel Challenge".equals(label)||"Party Wheel".equals(label)){wheelPanel(label);return;}
        toast(label);
    }

    private boolean canStartRoomGame(String label){
        if(cloudRoom&&!isModerator()){toast("Host/co-host starts "+label+" for the whole room");return false;}
        return true;
    }

    private void attachGameState(DocumentReference room){
        final String listenerRoom10005=room.getId();
        final String listenerUid10005=user==null?"":user.getUid();
        final int listenerBinding10005=roomListenerGeneration982;
        gameStateListener=room.collection("game_state").document("current").addSnapshotListener((doc,e)->{
            if(!cloudRoom||roomId==null||!listenerRoom10005.equals(roomId)
                ||user==null||!listenerUid10005.equals(user.getUid())
                ||listenerBinding10005!=roomListenerGeneration982
                ||isFinishing()||isDestroyed())return;
            if(e!=null||doc==null)return;
            if(!doc.exists()){activeGameRound=0L;activeGameType="";activeGamePrompt="";activeGameResult="";gameSnapshotReady=true;return;}
            Long rawRound=doc.getLong("roundId");long round=rawRound==null?0L:rawRound;
            String type=str(doc,"type","game"),prompt=str(doc,"prompt",""),result=str(doc,"result",""),status=str(doc,"status","active");
            if(!KingMeHomePartyGuard10007.gameIsActive(status,round)){
                activeGameRound=0L;activeGameType="";activeGamePrompt="";activeGameResult="";
                gameSnapshotReady=true;return;
            }
            boolean changed=round>0&&round!=activeGameRound;
            activeGameRound=round;activeGameType=type;activeGamePrompt=prompt;activeGameResult=result;
            if("active".equals(status)&&(changed||!gameSnapshotReady))showGameStateBanner(type,prompt,result);
            gameSnapshotReady=true;
        });
    }

    private void showGameStateBanner(String type,String prompt,String result){
        if(reactionBanner==null)return;
        String icon="🎮";
        if("vote".equals(type))icon="💌";else if("bomb".equals(type))icon="💣";else if("wheel".equals(type))icon="🎡";else if("truth_dare".equals(type))icon="🥊";else if("guess".equals(type))icon="🌷";else if("battle".equals(type))icon="⚔️";
        String text=icon+"  "+(prompt==null?"Room game":prompt)+(result==null||result.isEmpty()?"":"  •  "+result);
        final TextView gameBanner10005=reactionBanner;
        final int bannerSerial=claimBanner10006(gameBanner10005);
        reactionBanner.setText(text);reactionBanner.setTextSize(15);reactionBanner.setVisibility(View.VISIBLE);reactionBanner.setBackground(bg(0xcc38265d,28));
        final int bannerGeneration10005=roomListenerGeneration982;
        final String bannerRoom10005=roomId;
        gameBanner10005.postDelayed(()->{
            if(!currentBanner10006(gameBanner10005,bannerSerial,bannerGeneration10005)
                ||(bannerRoom10005!=null&&!bannerRoom10005.equals(roomId)))return;
            gameBanner10005.animate().alpha(0f).setDuration(600).withEndAction(()->{
                if(!currentBanner10006(gameBanner10005,bannerSerial,bannerGeneration10005))return;
                gameBanner10005.setVisibility(View.GONE);gameBanner10005.setAlpha(1f);
                gameBanner10005.setTextSize(42);gameBanner10005.setBackground(bg(0x663c1f58,30));
            }).start();
        },2600);
    }

    private void publishGameState(String type,String prompt,String result){publishGameState(type,prompt,result,null);}
    private void publishGameState(String type,String prompt,String result,Map<String,Object> extras){
        long round=System.currentTimeMillis();
        if(!cloudRoom||db==null||user==null){activeGameRound=round;activeGameType=type;activeGamePrompt=prompt;activeGameResult=result;showGameStateBanner(type,prompt,result);addEvent("game",safeName()+" started "+prompt);return;}
        if(!isModerator()||!memberSeen){toast("Host/co-host membership required to start games");return;}
        if(!gameSnapshotReady){toast("Loading room game state • try again");return;}
        final long observedRound10009=activeGameRound;
        final String originRoom10006=roomId,originUser10006=user.getUid();
        final int originBinding10006=roomListenerGeneration982;
        if(!currentGameWrite10006(originRoom10006,originUser10006,originBinding10006))return;
        Map<String,Object> d=new HashMap<>();
        // Extras are limited to game-specific fields; never let a game overwrite
        // the authoritative round/status/actor fields.
        if(extras!=null){for(String key:extras.keySet())
            if("options".equals(key)||"participantUids".equals(key)||"answer".equals(key))
                d.put(key,extras.get(key));}
        d.put("actorUid",originUser10006);d.put("actorName",safeName());
        d.put("type",type);d.put("prompt",prompt==null?"":prompt);
        d.put("result",result==null?"":result);d.put("status","active");
        d.put("updatedAt",FieldValue.serverTimestamp());
        final DocumentReference currentGame10009=db.collection("live_rooms")
            .document(originRoom10006).collection("game_state").document("current");
        db.runTransaction(tx->{
            DocumentSnapshot latest10009=tx.get(currentGame10009);
            Long stored10009=latest10009.getLong("roundId");
            long serverRound10009=stored10009==null?0L:stored10009;
            String status10009=latest10009.getString("status");
            if(!KingMeHomePartyGuard10009.canPublishGame(
                latest10009.exists(),status10009,serverRound10009,observedRound10009))
                throw new IllegalStateException("KING_ROOM_GAME_CHANGED_10009");
            d.put("roundId",KingMeHomePartyGuard10009.nextGameRound(round,serverRound10009));
            tx.set(currentGame10009,d);
            return null;
        }).addOnSuccessListener(v->{
            if(!currentGameWrite10006(originRoom10006,originUser10006,originBinding10006))return;
            // The live snapshot remains the source of truth for what is visible.
            addEvent("game",safeName()+" started "+(prompt==null?type:prompt));
        }).addOnFailureListener(e->{
            if(currentGameWrite10006(originRoom10006,originUser10006,originBinding10006))
                toast("Game changed or start denied • reopen the game");
        });
    }

    private boolean currentGameWrite10006(String originRoom,String originUid,int binding){
        return KingRoomLifecycle10006.acceptsRoomWrite(originRoom,roomId,originUid,
            user==null?null:user.getUid(),binding,roomListenerGeneration982,
            cloudRoom&&!isFinishing()&&!isDestroyed());
    }
    private void closeActiveGame(){
        if(!cloudRoom||db==null||user==null||!isModerator()||!memberSeen){
            toast("Host/co-host room membership required");return;
        }
        final long expectedRound10008=activeGameRound;
        if(!gameSnapshotReady||expectedRound10008<=0L){toast("No active game to close");return;}
        final String originRoom10008=roomId,originUser10008=user.getUid();
        final int originBinding10008=roomListenerGeneration982;
        if(!currentGameWrite10006(originRoom10008,originUser10008,originBinding10008))return;
        final DocumentReference gameRef10008=db.collection("live_rooms")
            .document(originRoom10008).collection("game_state").document("current");
        db.runTransaction(tx->{
            DocumentSnapshot latest10008=tx.get(gameRef10008);
            Long serverRound10008=latest10008.getLong("roundId");
            if(!latest10008.exists() || !KingMeHomePartyGuard10008.mayCloseCurrentGame(
                    latest10008.getString("status"),serverRound10008,expectedRound10008))
                throw new IllegalStateException("Room game changed or already closed");
            Map<String,Object> update10008=new HashMap<>();
            update10008.put("status","closed");
            update10008.put("actorUid",originUser10008);
            update10008.put("updatedAt",FieldValue.serverTimestamp());
            tx.update(gameRef10008,update10008);
            return null;
        }).addOnSuccessListener(v->{
            if(currentGameWrite10006(originRoom10008,originUser10008,originBinding10008)){
                addEvent("game",safeName()+" closed the room game");toast("Game closed");
            }
        }).addOnFailureListener(e->{
            if(currentGameWrite10006(originRoom10008,originUser10008,originBinding10008))
                toast("Game not closed • newer round or permission changed");
        });
    }

    private void startNewVoteDialog(){
        if(!canStartRoomGame("Vote"))return;
        final String startedInRoom10002=roomId;
        final int startedBinding10002=roomListenerGeneration982;
        final String startedUid10002=user==null?"":user.getUid();
        final EditText input=new EditText(this);input.setHint("Vote question");input.setText("Do you like this Party Room?");
        new AlertDialog.Builder(this).setTitle("💌 Start Room Vote").setView(input).setPositiveButton("Start",(d,w)->{
            if(startedInRoom10002==null||!startedInRoom10002.equals(roomId)||!isModerator()
                ||(cloudRoom&&!currentVoteRoom10001(startedInRoom10002,startedUid10002,startedBinding10002))){
                toast("Room changed. Open a new vote in the current room.");return;
            }
            String q=input.getText().toString().trim();if(q.isEmpty())q="Room vote";
            ArrayList<String> opts=new ArrayList<>();opts.add("👍 Yes");opts.add("👎 No");opts.add("❤️ Love it");opts.add("😂 Funny");Map<String,Object>x=new HashMap<>();x.put("options",opts);x.put("participantUids",new ArrayList<String>());publishGameState("vote",q,"",x);
        }).setNegativeButton("Cancel",null).show();
    }

    private boolean currentVoteRoom10001(String expectedRoom,String expectedUid,int binding){
        return cloudRoom&&memberSeen&&db!=null&&user!=null&&roomId!=null
            &&roomId.equals(expectedRoom)&&expectedUid.equals(user.getUid())
            &&binding==roomListenerGeneration982&&!isFinishing()&&!isDestroyed();
    }

    private void submitCurrentVote(){
        if(!cloudRoom||db==null||user==null||roomId==null){toast("Join a live Party Room to vote");return;}
        final String expectedRoom10001=roomId,expectedUid10001=user.getUid();
        final int binding10001=roomListenerGeneration982;
        if(!currentVoteRoom10001(expectedRoom10001,expectedUid10001,binding10001)){
            toast("Room membership required to vote");return;
        }
        final DocumentReference liveRoom10001=db.collection("live_rooms").document(expectedRoom10001);
        liveRoom10001.collection("game_state").document("current").get().addOnSuccessListener(doc->{
            if(!currentVoteRoom10001(expectedRoom10001,expectedUid10001,binding10001))return;
            if(!doc.exists()||!"vote".equals(doc.getString("type"))||!"active".equals(doc.getString("status"))){toast("No active room vote");return;}
            Long r=doc.getLong("roundId");long round=r==null?0:r;
            String question=str(doc,"prompt","Room vote");ArrayList<String> options=new ArrayList<>();
            Object raw=doc.get("options");if(raw instanceof List)for(Object o:(List<?>)raw)if(o!=null)options.add(String.valueOf(o));
            if(options.isEmpty()){options.add("👍 Yes");options.add("👎 No");options.add("❤️ Love it");options.add("😂 Funny");}
            String[] a=options.toArray(new String[0]);
            new AlertDialog.Builder(this).setTitle("💌 "+question).setItems(a,(d,w)->{
                // Never submit a delayed dialog's vote into a different room.
                if(!currentVoteRoom10001(expectedRoom10001,expectedUid10001,binding10001)){
                    toast("Room changed. Open voting again.");return;
                }
                Map<String,Object>vote=new HashMap<>();vote.put("uid",expectedUid10001);
                vote.put("name",safeName());vote.put("roundId",round);
                vote.put("choice",a[w]);vote.put("choiceIndex",w);
                vote.put("updatedAt",FieldValue.serverTimestamp());
                // Atomic read+write: never commit an old ballot to a replaced round.
                // Room rules also require current membership and a published option.
                final DocumentReference stateRef10004=liveRoom10001.collection("game_state").document("current");
                final DocumentReference ballotRef10004=liveRoom10001.collection("game_votes").document(expectedUid10001);
                db.runTransaction(tx->{
                    DocumentSnapshot latest=tx.get(stateRef10004);
                    Long latestRound10004=latest.getLong("roundId");
                    if(!latest.exists()||!"vote".equals(latest.getString("type"))
                        ||!"active".equals(latest.getString("status"))
                        ||latestRound10004==null||latestRound10004.longValue()!=round)
                        throw new IllegalStateException("Vote round changed");
                    tx.set(ballotRef10004,vote);
                    return null;
                }).addOnSuccessListener(done->{
                    if(currentVoteRoom10001(expectedRoom10001,expectedUid10001,binding10001)){
                        addEvent("vote",safeName()+" voted "+a[w]);toast("Vote sent: "+a[w]);
                    }
                }).addOnFailureListener(e->{
                    if(currentVoteRoom10001(expectedRoom10001,expectedUid10001,binding10001))
                        toast("Vote changed or permission denied • try again");
                });
            }).setNeutralButton(isModerator()?"Results":"Close",(d,w)->{
                if(currentVoteRoom10001(expectedRoom10001,expectedUid10001,binding10001)&&isModerator())showVoteResults();
            }).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->{if(currentVoteRoom10001(expectedRoom10001,expectedUid10001,binding10001))
            toast("Vote unavailable: "+msg(e));});
    }

    /** End only the vote round the host actually reviewed, never another game. */
    private void closeVoteRound10003(String expectedRoom,String expectedUid,int binding,long round){
        if(!currentVoteRoom10001(expectedRoom,expectedUid,binding)||!isModerator()){
            toast("Room changed or host access lost. Vote was not closed.");return;
        }
        final DocumentReference ref10003=db.collection("live_rooms").document(expectedRoom)
            .collection("game_state").document("current");
        db.runTransaction(transaction->{
            DocumentSnapshot latest=transaction.get(ref10003);
            Long current=latest.getLong("roundId");
            if(!latest.exists()||!"vote".equals(latest.getString("type"))
                ||!"active".equals(latest.getString("status"))
                ||current==null||current!=round)
                throw new IllegalStateException("Vote round changed or closed");
            Map<String,Object> updates=new HashMap<>();
            updates.put("status","closed");updates.put("actorUid",expectedUid);
            updates.put("updatedAt",FieldValue.serverTimestamp());
            transaction.update(ref10003,updates);
            return null;
        }).addOnSuccessListener(done->{
            if(currentVoteRoom10001(expectedRoom,expectedUid,binding)){
                addEvent("game",safeName()+" closed the room vote");toast("Vote closed");
            }
        }).addOnFailureListener(e->{
            if(currentVoteRoom10001(expectedRoom,expectedUid,binding))
                toast("Vote was not closed • round changed or permission denied");
        });
    }

    private void showVoteResults(){
        if(!cloudRoom||db==null||user==null||roomId==null||!memberSeen){toast("Join a live room to see votes");return;}
        final String resultsRoom10002=roomId,resultsUid10002=user.getUid();
        final int resultsBinding10002=roomListenerGeneration982;
        final DocumentReference resultsRef10002=db.collection("live_rooms").document(resultsRoom10002);
        resultsRef10002.collection("game_state").document("current")
            .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(state->{
            if(!currentVoteRoom10001(resultsRoom10002,resultsUid10002,resultsBinding10002))return;
            if(!state.exists()||!"vote".equals(state.getString("type"))){toast("No room vote");return;}
            Long rr=state.getLong("roundId");long round=rr==null?0:rr;
            String question=str(state,"prompt","Room vote");
            resultsRef10002.collection("game_votes").get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(snap->{
                if(!currentVoteRoom10001(resultsRoom10002,resultsUid10002,resultsBinding10002))return;
                // A second fetch prevents delayed vote results from showing an old
                // round after a moderator has published a newer question.
                resultsRef10002.collection("game_state").document("current")
                    .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(latest10003->{
                    if(!currentVoteRoom10001(resultsRoom10002,resultsUid10002,resultsBinding10002))return;
                    Long latestRound10003=latest10003.getLong("roundId");
                    if(!latest10003.exists()||!"vote".equals(latest10003.getString("type"))
                        ||latestRound10003==null||latestRound10003!=round){
                        toast("Vote changed. Open results again.");return;
                    }
                Map<String,Integer>counts=new HashMap<>();int total=0;
                for(DocumentSnapshot v:snap.getDocuments()){
                    Long r=v.getLong("roundId");if(r==null||r!=round)continue;
                    String c=str(v,"choice","Vote");counts.put(c,counts.containsKey(c)?counts.get(c)+1:1);total++;
                }
                StringBuilder out=new StringBuilder(question).append("\n\n");
                ArrayList<String> known=new ArrayList<>();Object rawOptions=state.get("options");
                if(rawOptions instanceof List)for(Object o:(List<?>)rawOptions)
                    if(o instanceof String&&!known.contains(o))known.add((String)o);
                if(known.isEmpty()){
                    known.add("👍 Yes");known.add("👎 No");known.add("❤️ Love it");known.add("😂 Funny");
                }
                for(String k:known)out.append(k).append("  •  ").append(counts.containsKey(k)?counts.get(k):0).append('\n');
                out.append("\nTotal votes: ").append(total);
                new AlertDialog.Builder(this).setTitle("💌 Vote Results").setMessage(out.toString())
                    .setPositiveButton("Vote",(d,w)->{
                        if(currentVoteRoom10001(resultsRoom10002,resultsUid10002,resultsBinding10002))submitCurrentVote();
                    }).setNeutralButton(isModerator()?"Close vote":null,(d,w)->{
                        closeVoteRound10003(resultsRoom10002,resultsUid10002,resultsBinding10002,round);
                    }).setNegativeButton("Close",null).show();
                }).addOnFailureListener(e->{
                    if(currentVoteRoom10001(resultsRoom10002,resultsUid10002,resultsBinding10002))
                        toast("Unable to verify the current vote round");
                });
            }).addOnFailureListener(e->{if(currentVoteRoom10001(resultsRoom10002,resultsUid10002,resultsBinding10002))toast("Results unavailable: "+msg(e));});
        }).addOnFailureListener(e->{if(currentVoteRoom10001(resultsRoom10002,resultsUid10002,resultsBinding10002))toast("Vote unavailable: "+msg(e));});
    }

    private void localVotePanel(){
        String[] options={"👍 Yes","👎 No","❤️ Love it","😂 Funny"};new AlertDialog.Builder(this).setTitle("💌 Room Vote").setItems(options,(d,w)->{addEvent("vote",safeName()+" voted "+options[w]);toast("Vote sent: "+options[w]);}).setNegativeButton("Close",null).show();
    }

    private void roomSeatPanel(){
        if(isOwner()){seatLayoutPanel();return;}
        List<String> rows=new ArrayList<>();
        for(int i=1;i<=maxSeats;i++){String n=seatNames.get(i);boolean locked=lockedSeats.contains(i);rows.add("NO."+i+"  "+(n==null?(locked?"🔒 Locked":"＋ Empty"):"👤 "+n));}
        new AlertDialog.Builder(this).setTitle("🪑 Room seat • "+maxSeats+" seats").setItems(rows.toArray(new String[0]),(d,w)->seatAction(w+1)).setNegativeButton("Close",null).show();
    }

    private void toggleSeatQueue(){
        if(!isModerator()){toast("Host/co-host controls the seat queue");return;}
        if(cloudRoom&&db!=null){
            db.collection("live_rooms").document(roomId).get().addOnSuccessListener(doc->{boolean next=!Boolean.TRUE.equals(doc.getBoolean("queueEnabled"));setRoomValue("queueEnabled",next);addEvent("queue",safeName()+" turned seat queue "+(next?"ON":"OFF"));toast("Seat queue "+(next?"enabled":"disabled"));}).addOnFailureListener(e->toast(msg(e)));
        }else{boolean next=!prefs.getBoolean("queue_"+roomId,false);prefs.edit().putBoolean("queue_"+roomId,next).apply();toast("Seat queue "+(next?"enabled":"disabled"));}
    }

    private void atmospherePanel(){
        String[] names954={"Classic","KTV","PK","Love","Game","Royal","Neon","Galaxy","Festival","Ice","Diamond","Sakura","Ocean","Sunset","Aurora"};
        String[] icons954={"✨","🎤","⚔️","💗","🎮","👑","💡","🌌","🎉","❄️","💎","🌸","🌊","🌇","🌈"};
        String[] desc954={"KING stage • green","Blue KTV light show","Battle spotlights","Rose pink glow","Game arena","Golden royal stage","Electric cyan","Space starfield","Celebration lights","Frozen lounge","Sparkling crystal palace","Pink sakura night","Underwater neon","Warm purple sunset","Northern sky lights"};
        LinearLayout root954=new LinearLayout(this);root954.setOrientation(LinearLayout.VERTICAL);root954.setPadding(dp(12),dp(8),dp(12),dp(10));root954.setBackgroundColor(0xfff8f6fa);
        TextView current954=tv("Current  •  "+themeIcon954()+"  "+roomTheme,13,0xff38313f,true);current954.setGravity(Gravity.CENTER);current954.setBackground(bg(0xfffff0bd,14));root954.addView(current954,new LinearLayout.LayoutParams(-1,dp(42)));
        ScrollView scroll954=new ScrollView(this);LinearLayout list954=new LinearLayout(this);list954.setOrientation(LinearLayout.VERTICAL);scroll954.addView(list954);
        for(int i=0;i<names954.length;i++){
            final String chosen954=names954[i];
            LinearLayout row954=new LinearLayout(this);row954.setGravity(Gravity.CENTER_VERTICAL);row954.setPadding(dp(10),dp(4),dp(10),dp(4));row954.setBackground(bg(chosen954.equals(roomTheme)?0xfffff2c8:Color.WHITE,14));
            TextView icon954=tv(icons954[i],24,themeAccentFor954(chosen954),false);icon954.setGravity(Gravity.CENTER);row954.addView(icon954,new LinearLayout.LayoutParams(dp(48),dp(56)));
            LinearLayout info954=new LinearLayout(this);info954.setOrientation(LinearLayout.VERTICAL);info954.addView(tv(names954[i],14,0xff2e2933,true),new LinearLayout.LayoutParams(-1,dp(28)));info954.addView(tv(desc954[i],11,0xff81798a,false),new LinearLayout.LayoutParams(-1,dp(24)));row954.addView(info954,new LinearLayout.LayoutParams(0,dp(56),1));
            TextView mark954=tv(chosen954.equals(roomTheme)?"✓":"›",16,chosen954.equals(roomTheme)?0xff1a8f67:0xff8c8493,true);mark954.setGravity(Gravity.CENTER);row954.addView(mark954,new LinearLayout.LayoutParams(dp(36),dp(56)));
            row954.setOnClickListener(v->{if(!isOwner()){toast("Host controls the room atmosphere");return;}roomTheme=chosen954;if(cloudRoom)setRoomValue("theme",roomTheme);else prefs.edit().putString("theme_"+roomName,roomTheme).apply();renderParty();});
            LinearLayout.LayoutParams rp954=new LinearLayout.LayoutParams(-1,dp(62));rp954.setMargins(0,dp(3),0,dp(3));list954.addView(row954,rp954);
        }
        root954.addView(scroll954,new LinearLayout.LayoutParams(-1,Math.min(dp(520),(int)(getResources().getDisplayMetrics().heightPixels*.60f))));
        new AlertDialog.Builder(this).setTitle("🎨 Room Atmosphere").setView(root954).setNegativeButton("Close",null).show();
    }

    private void themeRoomPanel(){atmospherePanel();}

    private void shareFamilyRoom(){
        Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,"👑 Join my KING Plus family room • "+roomName+" • Room ID "+shortId());startActivity(Intent.createChooser(i,"Share Family"));
    }

    private void counterPanel(){giftSendingCounterDialog();}

    private void guessItPanel(){
        if(!cloudRoom||db==null||user==null){final int answer=1+new java.util.Random().nextInt(9);final EditText input=new EditText(this);input.setHint("Guess 1 to 9");input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);new AlertDialog.Builder(this).setTitle("🌷 Guess It").setView(input).setPositiveButton("Guess",(d,w)->{int g=-1;try{g=Integer.parseInt(input.getText().toString().trim());}catch(Exception ignored){}String r=g==answer?"🎉 Correct!":"Answer was "+answer;addEvent("play",safeName()+" played Guess It • "+r);toast(r);}).setNegativeButton("Close",null).show();return;}
        if(isModerator()){int answer=1+new java.util.Random().nextInt(9);Map<String,Object>x=new HashMap<>();x.put("answer",answer);publishGameState("guess","Guess a number from 1 to 9","",x);new AlertDialog.Builder(this).setTitle("🌷 Guess It").setMessage("Room game started. Everyone can open Guess It and submit a guess.").setPositiveButton("OK",null).show();return;}
        if(!memberSeen||!currentVoteRoom10001(roomId,user.getUid(),roomListenerGeneration982)){
            toast("Join the Party Room before guessing");return;
        }
        final String guessRoom10010=roomId,guessUid10010=user.getUid();
        final int guessBinding10010=roomListenerGeneration982;
        final DocumentReference guessRef10010=db.collection("live_rooms").document(guessRoom10010)
            .collection("game_state").document("current");
        guessRef10010.get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(doc->{
            if(!currentVoteRoom10001(guessRoom10010,guessUid10010,guessBinding10010))return;
            Long rr=doc.getLong("roundId");
            if(!KingMeHomePartyPolicy10010.activeGuess(doc.exists(),doc.getString("type"),
                doc.getString("status"),rr==null?0L:rr)){
                toast("Host/co-host has not started Guess It");return;
            }
            final long guessRound10010=rr;
            final EditText input=new EditText(this);input.setHint("Guess 1 to 9");
            input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            new AlertDialog.Builder(this).setTitle("🌷 "+str(doc,"prompt","Guess It"))
                .setView(input).setPositiveButton("Guess",(d,w)->{
                    if(!currentVoteRoom10001(guessRoom10010,guessUid10010,guessBinding10010))return;
                    int guess10010=-1;
                    try{guess10010=Integer.parseInt(input.getText().toString().trim());}catch(Exception ignored){}
                    if(guess10010<1||guess10010>9){toast("Guess must be from 1 to 9");return;}
                    final int submittedGuess10010=guess10010;
                    guessRef10010.get(com.google.firebase.firestore.Source.SERVER)
                        .addOnSuccessListener(current->{
                            if(!currentVoteRoom10001(guessRoom10010,guessUid10010,guessBinding10010))return;
                            Long liveRound=current.getLong("roundId");
                            if(!KingMeHomePartyPolicy10010.sameGuessRound(current.exists(),
                                current.getString("type"),current.getString("status"),
                                liveRound==null?0L:liveRound,guessRound10010)){
                                toast("Game ended or changed • reopen Guess It");return;
                            }
                            Long ans=current.getLong("answer");
                            if(ans==null||ans<1||ans>9){toast("Game answer unavailable");return;}
                            String result10010=submittedGuess10010==ans.intValue()
                                ?"🎉 Correct!":"❌ Try again";
                            addEvent("guess",safeName()+" guessed "+submittedGuess10010
                                +" • "+result10010);toast(result10010);
                        }).addOnFailureListener(error->{
                            if(currentVoteRoom10001(guessRoom10010,guessUid10010,guessBinding10010))
                                toast("Cannot verify live game • try again");
                        });
                }).setNegativeButton("Close",null).show();
        }).addOnFailureListener(error->{
            if(currentVoteRoom10001(guessRoom10010,guessUid10010,guessBinding10010))
                toast("Cannot load Guess It • check connection");
        });
    }

    private void truthOrDare(){
        String[] truth={"What made you smile today?","Who is your best friend here?","What is your favourite song?","What is your dream trip?"};String[] dare={"Send 😂 in chat","Say hello to everyone","Send a rose gift","Take an empty mic seat"};
        if(cloudRoom&&!isModerator()){if("truth_dare".equals(activeGameType)&&activeGamePrompt!=null&&!activeGamePrompt.isEmpty())new AlertDialog.Builder(this).setTitle("🥊 Truth or Dare").setMessage(activeGamePrompt).setPositiveButton("Done",(d,w)->addEvent("play",safeName()+" completed Truth or Dare")).setNegativeButton("Close",null).show();else toast("Host/co-host has not started Truth or Dare");return;}
        new AlertDialog.Builder(this).setTitle("🥊 Truth or Dare").setItems(new String[]{"Truth","Dare"},(d,w)->{String text=w==0?truth[new java.util.Random().nextInt(truth.length)]:dare[new java.util.Random().nextInt(dare.length)];if(cloudRoom)publishGameState("truth_dare",(w==0?"Truth: ":"Dare: ")+text,"");else addEvent("play",safeName()+" chose "+(w==0?"Truth":"Dare"));new AlertDialog.Builder(this).setTitle(w==0?"Truth":"Dare").setMessage(text).setPositiveButton("Done",null).show();}).setNegativeButton("Close",null).show();
    }

    private void passBombPanel(){
        if(cloudRoom&&!isModerator()){if("bomb".equals(activeGameType))new AlertDialog.Builder(this).setTitle("💣 Pass the Bomb").setMessage(activeGameResult.isEmpty()?activeGamePrompt:activeGameResult).setPositiveButton("OK",null).show();else toast("Host/co-host has not started Pass the Bomb");return;}
        List<String> names=new ArrayList<>();names.add(ownerName==null?"Host":ownerName);for(String n:seatNames.values())if(n!=null&&!names.contains(n))names.add(n);for(String n:memberNames)if(n!=null&&!names.contains(n))names.add(n);String picked=names.get(new java.util.Random().nextInt(names.size()));String result="🔥 Bomb landed on "+picked+" 🔥";if(cloudRoom)publishGameState("bomb","Pass the Bomb",result);else addEvent("play","💣 Bomb passed to "+picked);new AlertDialog.Builder(this).setTitle("💣 Pass the Bomb").setMessage(result).setPositiveButton("Pass again",(d,w)->passBombPanel()).setNegativeButton("Close",null).show();
    }

    private void fanBoxPanel(){
        new AlertDialog.Builder(this).setTitle("📦 Fan Box").setItems(new String[]{"🔥 Charisma & Gift Senders","🎁 Gift Wall / History","🏆 Room Ranking"},(d,w)->{if(w==0)giftSendingCounterDialog();else if(w==1)giftHistoryDialog();else roomRankingDialog();}).setNegativeButton("Close",null).show();
    }

    private void wheelPanel(String title){
        if(cloudRoom&&!isModerator()){if("wheel".equals(activeGameType))new AlertDialog.Builder(this).setTitle("🎡 "+title).setMessage(activeGameResult.isEmpty()?activeGamePrompt:activeGameResult).setPositiveButton("OK",null).show();else toast("Host/co-host starts the room wheel");return;}
        String[] prizes={"🌹 Rose","⭐ 10 points","😂 Funny task","🎤 Take mic","💗 Heart","👑 Crown challenge","🎁 Gift challenge","🎉 Party shoutout"};String result=prizes[new java.util.Random().nextInt(prizes.length)];if(cloudRoom)publishGameState("wheel",title,result);else addEvent("play",safeName()+" spun "+title+" • "+result);new AlertDialog.Builder(this).setTitle("🎡 "+title).setMessage(result).setPositiveButton("Spin again",(d,w)->wheelPanel(title)).setNegativeButton("Close",null).show();
    }

    private void closeVoteFromPanel10004(String expectedRoom,String expectedUid,int binding){
        if(!currentVoteRoom10001(expectedRoom,expectedUid,binding)||!isModerator()){
            toast("Room changed or host access lost");return;
        }
        db.collection("live_rooms").document(expectedRoom).collection("game_state").document("current")
            .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(state->{
                if(!currentVoteRoom10001(expectedRoom,expectedUid,binding)||!isModerator())return;
                Long round=state.getLong("roundId");
                if(!state.exists()||!"vote".equals(state.getString("type"))
                    ||!"active".equals(state.getString("status"))||round==null){
                    toast("No active vote to close");return;
                }
                closeVoteRound10003(expectedRoom,expectedUid,binding,round.longValue());
            }).addOnFailureListener(e->{
                if(currentVoteRoom10001(expectedRoom,expectedUid,binding))toast("Cannot verify vote • try again");
            });
    }

    private void votePanel(){
        if(!cloudRoom||db==null||user==null){localVotePanel();return;}
        if(!isModerator()){submitCurrentVote();return;}
        final String panelRoom10004=roomId,panelUid10004=user.getUid();
        final int panelBinding10004=roomListenerGeneration982;
        String current="vote".equals(activeGameType)&&activeGameRound>0?"Current: "+activeGamePrompt:"No active vote";
        new AlertDialog.Builder(this).setTitle("💌 Room Vote • "+current)
            .setItems(new String[]{"Start new vote","Vote now","View results","Close active vote"},(d,w)->{
                if(!currentVoteRoom10001(panelRoom10004,panelUid10004,panelBinding10004)){
                    toast("Room changed • open the vote menu again");return;
                }
                if(w==0)startNewVoteDialog();
                else if(w==1)submitCurrentVote();
                else if(w==2)showVoteResults();
                else closeVoteFromPanel10004(panelRoom10004,panelUid10004,panelBinding10004);
            }).setNegativeButton("Close",null).show();
    }

    private TextView toolTile(String icon,String label,Runnable action){
        android.text.SpannableString title=new android.text.SpannableString(icon+"\n\n"+label);
        title.setSpan(new android.text.style.AbsoluteSizeSpan(24,true),0,icon.length(),android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextView t=tv("",10,Color.WHITE,false);t.setText(title);t.setGravity(Gravity.CENTER);t.setBackground(bg(0x123f3d53,16));t.setOnClickListener(v->KingSafe.run(this,"ui-action",action));t.setPadding(dp(2),dp(6),dp(2),dp(6));t.setContentDescription(label);return t;
    }
    private void privateChatDialog(){
        List<String> labels=new ArrayList<>();List<String> ids=new ArrayList<>();
        if(ownerName!=null&&!ownerName.equals(displayName)){labels.add("👑 "+ownerName);ids.add(ownerUid==null?"":ownerUid);}
        for(int no=1;no<=maxSeats;no++){String n=seatNames.get(no);if(n==null||n.equals(displayName))continue;labels.add("🎙 "+n);String uid=seatUids.get(no);ids.add(uid==null?"":uid);}
        if(cloudRoom){for(String n:memberNames){String uid=memberUids.get(n);if(n.equals(displayName)||uid==null)continue;boolean exists=false;for(String l:labels)if(l.endsWith(n)){exists=true;break;}if(!exists){labels.add("👤 "+n);ids.add(uid);}}}
        if(labels.isEmpty()){toast("No room member available for private chat");return;}
        new AlertDialog.Builder(this).setTitle("💬 Private chat").setItems(labels.toArray(new String[0]),(d,w)->{
            String name=labels.get(w).replace("👑 ","").replace("🎙 ","").replace("👤 ","");
            openPrivateChat(ids.get(w),name);
        }).setNegativeButton("Close",null).show();
    }
    private void openPrivateChat(String uid,String name){
        if(name==null||name.trim().isEmpty())name="KING User";
        Intent i=new Intent(this,ChatActivity.class);i.putExtra("peerName",name);i.putExtra("peerUid",uid==null?"":uid);startActivity(i);
    }
    private void emojiPanel(){
        stickerCategoryPanel("Emoji");
    }

    private void stickerCategoryPanel(String selected){
        final String[] cats={"Emoji","Festival","Laugh","World","Yellow","Cute","VIP","Lion"};
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(10),dp(10),dp(10),dp(12));
        root.setBackgroundColor(0xfff7f8fc);

        LinearLayout quick=new LinearLayout(this);
        quick.setGravity(Gravity.CENTER);
        String[] phrases={"Nice to meet everyone!","😂😂😂😂😂","Hi","Nice"};
        for(String q:phrases){
            TextView t=tv(q,10,0xff3d3d46,true);t.setGravity(Gravity.CENTER);t.setBackground(bg(0xffffffff,18));
            t.setOnClickListener(v->{if(composerBox!=null){composerBox.setText(q);sendMessage(composerBox);}});
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(36),1);lp.setMargins(dp(2),0,dp(2),0);quick.addView(t,lp);
        }
        root.addView(quick,new LinearLayout.LayoutParams(-1,dp(42)));

        HorizontalScrollView hsv=new HorizontalScrollView(this);hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout tabs=new LinearLayout(this);
        for(String c:cats){
            TextView t=tv(c,11,c.equals(selected)?0xffffb900:0xff777780,c.equals(selected));t.setGravity(Gravity.CENTER);t.setPadding(dp(10),0,dp(10),0);
            t.setOnClickListener(v->stickerCategoryPanel(c));tabs.addView(t,new LinearLayout.LayoutParams(dp(82),dp(42)));
        }
        hsv.addView(tabs);root.addView(hsv,new LinearLayout.LayoutParams(-1,dp(46)));

        if("VIP".equals(selected)){
            TextView lock=tv("VIP Exclusive Stickers • no billing in this build",12,0xff4a3442,true);lock.setGravity(Gravity.CENTER_VERTICAL);lock.setPadding(dp(12),0,dp(12),0);lock.setBackground(bg(0xffffedf2,12));root.addView(lock,new LinearLayout.LayoutParams(-1,dp(54)));
        }
        if("Yellow".equals(selected)||"Lion".equals(selected)){
            TextView join=tv(selected+" sticker series • JOIN",12,0xff493900,true);join.setGravity(Gravity.CENTER_VERTICAL);join.setPadding(dp(12),0,dp(12),0);join.setBackground(bg(0xfffff1bf,12));join.setOnClickListener(v->addEvent("sticker",safeName()+" joined the "+selected+" sticker series"));root.addView(join,new LinearLayout.LayoutParams(-1,dp(54)));
        }

        String[] icons=stickerSet(selected);
        ScrollView sc=new ScrollView(this);LinearLayout grid=new LinearLayout(this);grid.setOrientation(LinearLayout.VERTICAL);
        int idx=0;
        while(idx<icons.length){
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);
            for(int c=0;c<4;c++){
                if(idx>=icons.length){row.addView(new View(this),new LinearLayout.LayoutParams(0,dp(72),1));continue;}
                final String em=icons[idx++];TextView x=tv(em,31,0xff222222,false);x.setGravity(Gravity.CENTER);x.setOnClickListener(v->sendStickerOrEmoji(selected,em));row.addView(x,new LinearLayout.LayoutParams(0,dp(72),1));
            }
            grid.addView(row,new LinearLayout.LayoutParams(-1,dp(76)));
        }
        sc.addView(grid);root.addView(sc,new LinearLayout.LayoutParams(-1,dp(360)));
        AlertDialog dialog=new AlertDialog.Builder(this).setView(root).create();
        dialog.setOnShowListener(v->{android.view.Window w=dialog.getWindow();if(w!=null){w.setLayout(-1,(int)(getResources().getDisplayMetrics().heightPixels*.68f));w.setGravity(Gravity.BOTTOM);w.setBackgroundDrawable(bg(0xfff7f8fc,24));}});
        dialog.show();
    }

    private String[] stickerSet(String category){
        if("Festival".equals(category))return new String[]{"🥳","🎉","🎊","🎈","🇮🇳","🪔","🌸","✨","🙏","🎆","🎇","🎁"};
        if("Laugh".equals(category))return new String[]{"😂","🤣","😹","😆","😁","😜","🤪","😝","😅","🙃","😎","🤭"};
        if("World".equals(category))return new String[]{"🌍","🌎","🌏","🌙","⭐","☀️","🌈","🔥","💫","🌊","🌺","🦋"};
        if("Yellow".equals(category))return new String[]{"😇","😈","😘","😍","😔","😄","🤨","🤗","😭","😓","👋","🚨","🪙","🎲","🎰","😴"};
        if("Cute".equals(category))return new String[]{"🐹","🐰","🐱","🐶","🐼","🦊","🐻","🐣","🦄","🐥","🐨","🐯"};
        if("VIP".equals(category))return new String[]{"👑","💎","🏆","🛡️","✨","🌟","🥇","💝","🪽","🎖️","🏰","🪄"};
        if("Lion".equals(category))return new String[]{"🦁","👋","😠","😭","🥳","🎤","❤️","🔥","👑","🎉","✨","💛"};
        return new String[]{"🙂","🥺","😍","😭","😎","😂","😘","🤐","😴","🤒","😜","😁","🤣","😏","😢","😤","🤩","🥳","❤️","💗","🔥","👏","👑","🎉","👍","💯","🌹","✨"};
    }

    private void sendStickerOrEmoji(String category,String value){
        if("VIP".equals(category)&&!KingPremiumAccess981.PAID_VIP_ACTIVE){toast("VIP sticker is locked in this no-billing build");return;}
        if(composerBox!=null){composerBox.setText(value);sendMessage(composerBox);}
        showReactionEffect(value);addEvent("sticker",safeName()+" sent "+category+" sticker "+value);
    }

    private void showReactionEffect(String emoji){
        final TextView banner=reactionBanner;if(banner==null||partyUiDead())return;
        try{
            final int serial10006=claimBanner10006(banner);
            final int binding10006=roomListenerGeneration982;
            banner.setVisibility(View.VISIBLE);
            banner.setText((emoji==null?"✨":emoji)+"   "+safeName());
            banner.postDelayed(()->{
                if(!currentBanner10006(banner,serial10006,binding10006))return;
                try{banner.animate().alpha(0f).setDuration(500).withEndAction(()->{
                    if(currentBanner10006(banner,serial10006,binding10006)){
                        banner.setVisibility(View.GONE);banner.setAlpha(1f);banner.setTranslationX(0f);
                    }}).start();}
                catch(Exception e){android.util.Log.e("KINGPlusParty","Reaction animation failed",e);}
            },1400);
        }catch(Exception e){android.util.Log.e("KINGPlusParty","Reaction effect failed",e);}
    }
    private void showEntranceEffect(String name){LevelSystem.Snapshot p720=LevelSystem.read(this);showEntranceEffect730(name,p720.level,p720.vipLevel,KingCosmetics.effect(this),KingCosmetics.frame(this));}
    private void showEntranceEffect720(String name,int level,int vip){showEntranceEffect730(name,level,vip,vip>=10?"Royal Arrival":vip>=7?"Galaxy Portal":vip>=5?"Rose Shower":vip>=3?"Crown Drop":"Welcome Sparkle",vip>=10?"Crown Frame":vip>=7?"Galaxy Frame":vip>=4?"Royal Frame":"Minimal Frame");}
    private void showEntranceEffect730(String name,int level,int vip,String effect,String frame){if(partyUiDead()||!partyVisualForeground995)return;loadRoomPersonalSettings();if(skipEntranceVisuals||"None".equals(effect)||liveEmojiStageV530==null)return;if(!skipEntranceSound)KingSoundFx.join(this);
        try{KingEntranceView fx=new KingEntranceView(this,name,effect,frame);FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-1,-1);liveEmojiStageV530.addView(fx,lp);fx.start(()->{try{if(liveEmojiStageV530!=null&&fx.getParent()==liveEmojiStageV530)liveEmojiStageV530.removeView(fx);}catch(Exception ignored){}});}catch(Exception e){android.util.Log.e("KINGPlusParty","Entrance overlay failed",e);}
        if(reactionBanner==null||partyUiDead())return;try{final TextView entranceBanner10006=reactionBanner;
        final int entranceSerial10006=claimBanner10006(entranceBanner10006);
        final int entranceBinding10006=roomListenerGeneration982;
        String badge=(vip>0?" 💎VIP "+vip:"")+(level>0?" • Lv."+level:"");reactionBanner.setText(KingCosmetics.effectEmoji(effect)+" "+(name==null?"Guest":name)+badge+" • "+frame);reactionBanner.setTextSize(vip>=7?18:17);reactionBanner.setAlpha(1f);reactionBanner.setVisibility(View.VISIBLE);reactionBanner.setBackground(KingCosmetics.idFrame(this,frame));reactionBanner.setTranslationY(-dp(60));reactionBanner.animate().translationY(0).setDuration(360).start();reactionBanner.postDelayed(()->{
            if(!currentBanner10006(entranceBanner10006,entranceSerial10006,entranceBinding10006))return;
            entranceBanner10006.animate().translationY(-dp(40)).alpha(0f).setDuration(480).withEndAction(()->{
                if(currentBanner10006(entranceBanner10006,entranceSerial10006,entranceBinding10006)){
                    entranceBanner10006.setTranslationY(0);entranceBanner10006.setVisibility(View.GONE);
                    entranceBanner10006.setAlpha(1f);entranceBanner10006.setTextSize(42);
                    entranceBanner10006.setBackground(bg(0x663c1f58,30));
                }}).start();
        },2100);}catch(Exception e){android.util.Log.e("KINGPlusParty","Entrance banner failed",e);}
    }
    private void showGiftEffect(String actor,String target,String gift,String icon,long qty,long value){
        showGiftEffectConfirmed9944(actor,target,gift,icon,qty,value,null);
    }
    /** true only when a visible GiftBurstView has actually been started. */
    private boolean showGiftEffectConfirmed9944(String actor,String target,String gift,
                              String icon,long qty,long value,Runnable afterVisualComplete9944){
        if(partyUiDead()||!partyVisualForeground995||liveEmojiStageV530==null
           ||giftFxActive971>=3)return false;
        boolean started9944=false;
        String a=(actor==null||actor.isEmpty())?"User":actor;String t=(target==null||target.isEmpty())?"Host":target;String g=(gift==null||gift.isEmpty())?"Gift":gift;String ic=(icon==null||icon.isEmpty())?"🎁":icon;
        long now=System.currentTimeMillis();if(a.equals(lastGiftActor720)&&g.equals(lastGiftName720)&&now-lastGiftAt720<7000)giftCombo720++;else giftCombo720=1;lastGiftActor720=a;lastGiftName720=g;lastGiftAt720=now;
        long total=Math.max(0,value);String tier=total>=50000?"MYTHIC":total>=10000?"ROYAL":total>=2500?"PREMIUM":"GIFT";int accent=total>=50000?0xffff2fa6:total>=10000?0xffffc13a:total>=2500?0xff38c8ff:0xff9a67ff;
        String title=tier+" • "+ic+" "+g+(giftCombo720>1?"  COMBO ×"+giftCombo720:"");String sub=a+" → "+t+" • x"+Math.max(1,qty)+(total<=0?" • FREE":" • 💎"+compactNumber(total));
        if(!partyUiDead()&&liveEmojiStageV530.getParent()!=null&&giftFxActive971<3){
            GiftBurstView fx=null;
            try{
                fx=new GiftBurstView(this,ic,title,sub,accent);
                FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-1,-1);
                liveEmojiStageV530.addView(fx,lp);
                giftFxActive971++;
                final GiftBurstView startedView9944=fx;
                long duration=total>=50000?4300:total>=10000?3600:2900;
                fx.start(()->{
                    giftFxActive971=Math.max(0,giftFxActive971-1);
                    try{if(liveEmojiStageV530!=null
                       &&startedView9944.getParent()==liveEmojiStageV530)
                       liveEmojiStageV530.removeView(startedView9944);}
                    catch(Exception ignored){}
                    if(afterVisualComplete9944!=null)afterVisualComplete9944.run();
                    else drainPendingGifts9944();
                },duration);
                KingGiftSound993.play(this,gift);
                started9944=true;
            }catch(Exception e){
                if(fx!=null&&fx.getParent()==liveEmojiStageV530)
                    liveEmojiStageV530.removeView(fx);
                giftFxActive971=Math.max(0,giftFxActive971-1);
                android.util.Log.e("KINGPlusParty","Gift burst failed",e);
            }
        }
        if(reactionBanner!=null&&!partyUiDead()){try{
            final TextView giftBanner10006=reactionBanner;
            final int giftSerial10006=claimBanner10006(giftBanner10006);
            final int giftBinding10006=roomListenerGeneration982;
            reactionBanner.setText(ic+"  "+a+" → "+t+"  "+g+" x"+Math.max(1,qty)+(giftCombo720>1?"  🔥x"+giftCombo720:"")+(total<=0?"  FREE":"  💎"+compactNumber(total)));reactionBanner.setTextSize(16);reactionBanner.setAlpha(1f);reactionBanner.setVisibility(View.VISIBLE);reactionBanner.setBackground(bg(accent,28));reactionBanner.postDelayed(()->{
            if(!currentBanner10006(giftBanner10006,giftSerial10006,giftBinding10006))return;
            try{giftBanner10006.animate().alpha(0f).setDuration(500).withEndAction(()->{
                if(currentBanner10006(giftBanner10006,giftSerial10006,giftBinding10006)){
                    giftBanner10006.setVisibility(View.GONE);giftBanner10006.setAlpha(1f);
                    giftBanner10006.setTextSize(42);giftBanner10006.setBackground(bg(0x663c1f58,30));
                }}).start();}catch(Exception ignored){}
        },2000);}catch(Exception e){android.util.Log.e("KINGPlusParty","Gift banner failed",e);}}
        return started9944;
    }
    private void refreshSupporters(com.google.firebase.firestore.QuerySnapshot snap){
        if(snap==null)return;Map<String,Long> score=new HashMap<>();long totalGift=0;long activity=Math.max(1,snap.size());
        for(DocumentSnapshot d:snap.getDocuments())if("gift".equals(d.getString("type"))){String n=str(d,"actorName","User");Long raw=d.getLong("giftValue");long v=raw==null?0L:Math.max(0L,raw);totalGift+=v;
            if(v>0)score.put(n,score.containsKey(n)?score.get(n)+v:v);}
        int roomLv=(int)Math.min(99,1+(activity/12)+(totalGift/25000));int heartLv=(int)Math.min(99,1+(totalGift/10000));String compact=shortId();if(compact.length()>2)compact=compact.substring(compact.length()-2);
        if(roomLevelLabel!=null)roomLevelLabel.setText("🔷 Lv."+roomLv+" • No."+compact);if(heartLevelLabel!=null)heartLevelLabel.setText("💗 Heart Lv."+heartLv);
        if(supporterLabel==null)return;List<Map.Entry<String,Long>> list=new ArrayList<>(score.entrySet());java.util.Collections.sort(list,(a,b)->Long.compare(b.getValue(),a.getValue()));
        if(list.isEmpty()){supporterLabel.setText("🏆 Paid supporters • no verified diamond gifts (FREE gifts cost 0)");return;}
        StringBuilder out=new StringBuilder("🏆 Top supporters  ");int max=Math.min(3,list.size());for(int i=0;i<max;i++){if(i>0)out.append("   ");out.append(i==0?"🥇":i==1?"🥈":"🥉").append(' ').append(list.get(i).getKey()).append(" • ").append(list.get(i).getValue());}supporterLabel.setText(out.toString());
    }
    private String giftIconFor(String gift){
        if(gift==null)return "🎁";for(int i=0;i<giftNames.length;i++)if(gift.equals(giftNames[i]))return giftIcons[i];return "🎁";
    }
    private void activityCenterPanel(){activityCenterPanel("Templates");}

    private void activityCenterPanel(String selected){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(12),dp(10),dp(12),dp(14));root.setBackgroundColor(0xff242333);
        LinearLayout tabs=new LinearLayout(this);
        String[] tabNames={"Templates","Events"};
        for(String tab:tabNames){TextView t=tv(tab,17,tab.equals(selected)?0xffffd21c:0xff9f9aa8,true);t.setGravity(Gravity.CENTER);t.setOnClickListener(v->activityCenterPanel(tab));tabs.addView(t,new LinearLayout.LayoutParams(0,dp(48),1));}
        root.addView(tabs,new LinearLayout.LayoutParams(-1,dp(50)));
        ScrollView sc=new ScrollView(this);LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);sc.addView(body);root.addView(sc,new LinearLayout.LayoutParams(-1,dp(470)));
        if("Templates".equals(selected)){
            body.addView(tv("Interaction",15,Color.WHITE,true),new LinearLayout.LayoutParams(-1,dp(40)));
            addActivityTiles(body,new String[]{"💬","💘","💞","🎊"},new String[]{"Chat room","Pick Me!","Coupling","Ceremony"},true);
            body.addView(tv("Games",15,Color.WHITE,true),new LinearLayout.LayoutParams(-1,dp(42)));
            addActivityTiles(body,new String[]{"🎲","⚔️","🔫","🔥","🎯","🐑","🎨","🧩"},new String[]{"Dominoes Chat Room","Mobile Legends","PUBG Mobile","Free Fire","Ludo Chat Room","SHEEP & PUZZLES","Draw & Guess","Bingo"},true);
        }else{
            addActivityTiles(body,new String[]{"💞","🍒","🐔","🐯","🪄","🏎️","🍬","🎰"},new String[]{"Relationship","Crazy Fruits","Chicken Run","Animal PK","Magic Lamp","KING Tap Sprint","Candy Legend","Lucky Slot"},false);
            body.addView(tv("PRIVILEGES",15,Color.WHITE,true),new LinearLayout.LayoutParams(-1,dp(44)));
            addActivityTiles(body,new String[]{"💗","🎁"},new String[]{"Couple","Gift Wall"},false);
        }
        AlertDialog dialog=new AlertDialog.Builder(this).setView(root).create();
        dialog.setOnShowListener(v->{android.view.Window w=dialog.getWindow();if(w!=null){w.setLayout(-1,(int)(getResources().getDisplayMetrics().heightPixels*.52f));w.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);w.setGravity(Gravity.BOTTOM);w.setBackgroundDrawable(bg(0xff242333,24));}});
        dialog.show();
    }

    private void addActivityTiles(LinearLayout body,String[] icons,String[] labels,boolean template){
        int index=0;
        while(index<labels.length){
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);
            for(int c=0;c<4;c++){
                if(index>=labels.length){row.addView(new View(this),new LinearLayout.LayoutParams(0,dp(108),1));continue;}
                final String name=labels[index];final String icon=icons[index];
                TextView tile=toolTile(icon,name,()->{if(template)startRoomTemplate(name);else runRoomEvent(name);});
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(108),1);lp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(tile,lp);index++;
            }
            body.addView(row,new LinearLayout.LayoutParams(-1,dp(114)));
        }
    }

    private void startRoomTemplate(String name){
        if("Pick Me!".equals(name)){pickMePanel940();return;}
        if(cloudRoom&&!isModerator()){toast("Host/co-host selects the room template");return;}
        activeRoomTemplate=name;
        if(cloudRoom&&db!=null&&user!=null){Map<String,Object>t=new HashMap<>();t.put("name",name);t.put("active",true);t.put("actorUid",user.getUid());t.put("actorName",safeName());t.put("updatedAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).collection("room_templates").document("current").set(t).addOnFailureListener(e->toast("Template save failed: "+msg(e)));}
        addEvent("template",safeName()+" started "+name+" 🎉");
        if("Pick Me!".equals(name)){
            List<String> pool=new ArrayList<>();pool.addAll(memberNames);if(pool.isEmpty())pool.addAll(seatNames.values());
            if(pool.isEmpty()){toast("No other room members yet");return;}
            String picked=pool.get(new java.util.Random().nextInt(pool.size()));new AlertDialog.Builder(this).setTitle("💘 Pick Me!").setMessage("Selected: "+picked).setPositiveButton("Again",(d,w)->startRoomTemplate(name)).setNegativeButton("Close",null).show();return;
        }
        if("Coupling".equals(name)){relationshipEvent();return;}
        if("Draw & Guess".equals(name)){guessItPanel();return;}
        if("Bingo".equals(name)||name.contains("Ludo")||name.contains("Dominoes")||name.contains("SHEEP")){openRoomGames740();return;}
        if(name.contains("Mobile Legends")||name.contains("PUBG")||name.contains("Free Fire")){
            new AlertDialog.Builder(this).setTitle("Game not embedded")
                .setMessage("This third-party title is not inside KING Plus. Choose one of the KING games that is actually playable in this Party Room.")
                .setPositiveButton("Open KING Room Games",(d,w)->openRoomGames740())
                .setNegativeButton("Close",null).show();return;}
        new AlertDialog.Builder(this).setTitle(name).setMessage("Template is active in this room. Members can chat, take seats, send gifts and join room activities.").setPositiveButton("OK",null).show();
    }

    private void pickMePanel940(){
        if(!cloudRoom||db==null||user==null||roomId==null){
            List<String> pool=new ArrayList<>();pool.addAll(memberNames);if(pool.isEmpty())pool.addAll(seatNames.values());
            if(pool.isEmpty()){toast("No room members yet");return;}
            String picked=pool.get(new java.util.Random().nextInt(pool.size()));
            new AlertDialog.Builder(this).setTitle("💘 Pick Me!").setMessage("Selected: "+picked).setPositiveButton("Again",(d,w)->pickMePanel940()).setNegativeButton("Close",null).show();return;
        }
        com.google.firebase.firestore.CollectionReference queue=db.collection("live_rooms").document(roomId).collection("game_ready");
        queue.get().addOnSuccessListener(snap->{
            List<DocumentSnapshot> docs=new ArrayList<>();List<String> names=new ArrayList<>();boolean joined=false;
            for(DocumentSnapshot d:snap.getDocuments()){
                if(!"pick_me".equals(d.getString("gameType")))continue;
                String uid=d.getString("uid");if(uid==null||uid.isEmpty())continue;
                docs.add(d);names.add(str(d,"name","KING User"));if(uid.equals(user.getUid()))joined=true;
            }
            List<String> actions=new ArrayList<>();
            if(!isModerator())actions.add(joined?"💔 Leave waiting queue":"💘 Join waiting queue");
            actions.add("👥 View waiting queue ("+docs.size()+")");
            if(isModerator()){actions.add("🎯 Pick one now");actions.add("🗑 Clear waiting queue");}
            final boolean alreadyJoined=joined;
            new AlertDialog.Builder(this).setTitle("💘 Pick Me! • Waiting Stage").setItems(actions.toArray(new String[0]),(dlg,w)->{
                String x=actions.get(w);
                if(x.contains("Join waiting"))pickMeJoin940(queue);
                else if(x.contains("Leave waiting"))pickMeLeave940(queue);
                else if(x.contains("View waiting"))pickMeQueueList940(docs,names);
                else if(x.contains("Pick one"))pickMeSelect940(queue,docs,names);
                else if(x.contains("Clear waiting"))pickMeClear940(queue,docs);
            }).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast("Pick Me queue unavailable: "+msg(e)));
    }
    private void pickMeJoin940(com.google.firebase.firestore.CollectionReference queue){
        Map<String,Object>d=new HashMap<>();d.put("uid",user.getUid());d.put("name",safeName());d.put("gameType","pick_me");d.put("ready",true);d.put("updatedAt",FieldValue.serverTimestamp());
        queue.document(user.getUid()).set(d).addOnSuccessListener(v->{addEvent("pick_me_join",safeName()+" joined Pick Me waiting queue 💘");toast("Joined Pick Me waiting queue");}).addOnFailureListener(e->toast("Could not join queue: "+msg(e)));
    }
    private void pickMeLeave940(com.google.firebase.firestore.CollectionReference queue){
        queue.document(user.getUid()).delete().addOnSuccessListener(v->{addEvent("pick_me_leave",safeName()+" left Pick Me waiting queue");toast("Left Pick Me waiting queue");}).addOnFailureListener(e->toast("Could not leave queue: "+msg(e)));
    }
    private void pickMeQueueList940(List<DocumentSnapshot> docs,List<String> names){
        if(docs.isEmpty()){new AlertDialog.Builder(this).setTitle("💘 Waiting Queue").setMessage("No one is waiting yet.").setPositiveButton("OK",null).show();return;}
        String[] rows=new String[names.size()];for(int i=0;i<names.size();i++)rows[i]=(i+1)+".  "+names.get(i);
        new AlertDialog.Builder(this).setTitle("💘 Waiting Queue • "+rows.length).setItems(rows,null).setNegativeButton("Close",null).show();
    }
    private void pickMeSelect940(com.google.firebase.firestore.CollectionReference queue,List<DocumentSnapshot> docs,List<String> names){
        if(!isModerator()){toast("Host/co-host only");return;}
        if(docs.isEmpty()){toast("Waiting queue is empty");return;}
        int pick=new java.util.Random().nextInt(docs.size());DocumentSnapshot chosen=docs.get(pick);String uid=chosen.getString("uid"),name=names.get(pick);
        Map<String,Object>state=new HashMap<>();state.put("active",true);state.put("selectedUid",uid==null?"":uid);state.put("selectedName",name);state.put("selectedAt",FieldValue.serverTimestamp());state.put("selectedBy",user.getUid());
        db.collection("live_rooms").document(roomId).collection("game_state").document("pick_me").set(state,SetOptions.merge())
            .addOnSuccessListener(v->{chosen.getReference().delete().addOnFailureListener(e->{});addEvent("pick_me_result","💘 Pick Me selected "+name);new AlertDialog.Builder(this).setTitle("💘 Pick Me!").setMessage("Selected: "+name).setPositiveButton("Pick again",(d,w)->pickMePanel940()).setNegativeButton("Close",null).show();})
            .addOnFailureListener(e->toast("Pick Me selection failed: "+msg(e)));
    }
    private void pickMeClear940(com.google.firebase.firestore.CollectionReference queue,List<DocumentSnapshot> docs){
        if(!isModerator()){toast("Host/co-host only");return;}
        if(docs.isEmpty()){toast("Waiting queue is already empty");return;}
        WriteBatch batch=db.batch();for(DocumentSnapshot d:docs)batch.delete(d.getReference());
        batch.commit().addOnSuccessListener(v->{addEvent("pick_me_clear",safeName()+" cleared Pick Me waiting queue");toast("Pick Me queue cleared");}).addOnFailureListener(e->toast("Could not clear queue: "+msg(e)));
    }

    private void eventsPanel(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(12),dp(8),dp(12),dp(10));root.setBackgroundColor(0xff242333);
        TextView title=tv("Events • Activities",21,Color.WHITE,true);root.addView(title);
        String[] names={"Relationship","Crazy Fruits","Chicken Run","Animal PK","Lucky Slot","Treasure Hunt","Room Quiz","PK Battle"};
        String[] icons={"💞","🍒","🐔","🐯","🎰","💎","❓","⚔️"};
        LinearLayout row=null;int col=0;
        for(int i=0;i<names.length;i++){if(row==null||col==4){row=new LinearLayout(this);row.setGravity(Gravity.CENTER);root.addView(row,new LinearLayout.LayoutParams(-1,dp(112)));col=0;}final String name=names[i];TextView tile=toolTile(icons[i],name,()->runRoomEvent(name));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(104),1);p.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(tile,p);col++;}
        new AlertDialog.Builder(this).setView(root).setNegativeButton("Close",null).show();
    }
    private void runRoomEvent(String name){
        if("PK Battle".equals(name)){pkBattle();return;}
        if("Relationship".equals(name)){relationshipEvent();return;}
        if("Crazy Fruits".equals(name)){fruitEvent();return;}
        if("Chicken Run".equals(name)){chickenRunEvent();return;}
        if("Animal PK".equals(name)){animalPkEvent();return;}
        if("Lucky Slot".equals(name)){luckySlotEvent();return;}
        if("Treasure Hunt".equals(name)){treasureHuntEvent();return;}
        if("Room Quiz".equals(name)){roomQuizEvent();return;}
        if("Magic Lamp".equals(name)){simplePrizeEvent("🪄 Magic Lamp",new String[]{"✨ Wish granted","💎 Treasure sparkle","👑 Royal challenge","🌹 Send a rose"});return;}
        if("KING Tap Sprint".equals(name)||"Forza Motorsport".equals(name)){simpleRaceEvent();return;}
        if("Candy Legend".equals(name)){simplePrizeEvent("🍬 Candy Legend",new String[]{"🍬 Sweet win","🍭 Bonus round","⭐ Star candy","🎁 Candy gift challenge"});return;}
        if("Couple".equals(name)){relationshipEvent();return;}
        if("Gift Wall".equals(name)){giftHistoryDialog();return;}
        toast(name);
    }
    private void relationshipEvent(){
        List<String> labels=new ArrayList<>();List<String> ids=new ArrayList<>();
        for(String n:memberNames){String uid=memberUids.get(n);if(n!=null&&!n.equals(displayName)&&uid!=null&&!uid.isEmpty()){labels.add(n);ids.add(uid);}}
        for(int i=1;i<=maxSeats;i++){String n=seatNames.get(i),uid=seatUids.get(i);if(n!=null&&!n.equals(displayName)&&uid!=null&&!uid.isEmpty()&&!labels.contains(n)){labels.add(n);ids.add(uid);}}
        if(labels.isEmpty()){toast("Another signed-in room member is required");return;}
        new AlertDialog.Builder(this).setTitle("💞 Relationship").setItems(labels.toArray(new String[0]),(d,w)->{String n=labels.get(w),uid=ids.get(w);addEvent("relationship",safeName()+" sent a relationship invite to "+n+" 💞");new AlertDialog.Builder(this).setTitle("Invite sent").setMessage("A room event was posted for "+n+". You can continue in private chat or send a gift.").setPositiveButton("Private chat",(x,y)->openPrivateChat(uid,n)).setNeutralButton("Send Gift",(x,y)->giftDialogFor(uid,n)).setNegativeButton("Close",null).show();}).setNegativeButton("Close",null).show();
    }
    private void fruitEvent(){String[] f={"🍒","🍋","🍇","🍉","🍓"};java.util.Random r=new java.util.Random();String a=f[r.nextInt(f.length)],b=f[r.nextInt(f.length)],c=f[r.nextInt(f.length)];boolean win=a.equals(b)&&b.equals(c);String result=a+"   "+b+"   "+c+"\n\n"+(win?"🎉 JACKPOT":"Try again");addEvent("play",safeName()+" played Crazy Fruits • "+a+b+c);new AlertDialog.Builder(this).setTitle("🍒 Crazy Fruits").setMessage(result).setPositiveButton("Spin again",(d,w)->fruitEvent()).setNegativeButton("Close",null).show();}
    private void chickenRunEvent(){String[] lanes={"Lane 1","Lane 2","Lane 3"};new AlertDialog.Builder(this).setTitle("🐔 Chicken Run").setItems(lanes,(d,w)->{int winner=new java.util.Random().nextInt(3);boolean ok=w==winner;String result=(ok?"🏆 Winner! ":"🐔 Winner was ")+lanes[winner];addEvent("play",safeName()+" played Chicken Run • "+result);new AlertDialog.Builder(this).setTitle("Chicken Run").setMessage(result).setPositiveButton("Again",(x,y)->chickenRunEvent()).setNegativeButton("Close",null).show();}).setNegativeButton("Close",null).show();}
    private void animalPkEvent(){String[] animals={"🦁 Lion","🐯 Tiger","🐼 Panda","🦊 Fox"};new AlertDialog.Builder(this).setTitle("🐯 Animal PK").setItems(animals,(d,w)->{int me=30+new java.util.Random().nextInt(71),other=30+new java.util.Random().nextInt(71);String res=animals[w]+" "+me+"  vs  KING Bot "+other+"\n\n"+(me>=other?"🏆 You win":"Try again");addEvent("play",safeName()+" played Animal PK • "+me+":"+other);new AlertDialog.Builder(this).setTitle("Animal PK").setMessage(res).setPositiveButton("Again",(x,y)->animalPkEvent()).setNegativeButton("Close",null).show();}).setNegativeButton("Close",null).show();}
    private void luckySlotEvent(){String[] x={"7️⃣","⭐","💎","👑","🍒"};java.util.Random r=new java.util.Random();String a=x[r.nextInt(x.length)],b=x[r.nextInt(x.length)],c=x[r.nextInt(x.length)];int score=(a.equals(b)&&b.equals(c))?100:(a.equals(b)||b.equals(c)||a.equals(c)?30:5);addEvent("play",safeName()+" spun Lucky Slot • score "+score);new AlertDialog.Builder(this).setTitle("🎰 Lucky Slot").setMessage(a+"   "+b+"   "+c+"\n\nScore: "+score).setPositiveButton("Spin again",(d,w)->luckySlotEvent()).setNegativeButton("Close",null).show();}
    private void treasureHuntEvent(){String[] ch={"Chest 1","Chest 2","Chest 3"};new AlertDialog.Builder(this).setTitle("💎 Treasure Hunt").setItems(ch,(d,w)->{int treasure=new java.util.Random().nextInt(3);String res=w==treasure?"🎁 Treasure found!":"Empty chest • treasure was in "+ch[treasure];addEvent("play",safeName()+" played Treasure Hunt • "+res);new AlertDialog.Builder(this).setTitle("Treasure Hunt").setMessage(res).setPositiveButton("Again",(x,y)->treasureHuntEvent()).setNegativeButton("Close",null).show();}).setNegativeButton("Close",null).show();}
    private void roomQuizEvent(){String q="Which feature opens the room tool sheet?";String[] a={"＋ button","Back button","Room name","Heart badge"};new AlertDialog.Builder(this).setTitle("❓ Room Quiz").setMessage(q).setItems(a,(d,w)->{boolean ok=w==0;String res=ok?"✅ Correct":"❌ Correct answer: ＋ button";addEvent("play",safeName()+" answered Room Quiz • "+(ok?"correct":"wrong"));new AlertDialog.Builder(this).setTitle("Room Quiz").setMessage(res).setPositiveButton("OK",null).show();}).setNegativeButton("Close",null).show();}

    private void simplePrizeEvent(String title,String[] prizes){
        String result=prizes[new java.util.Random().nextInt(prizes.length)];addEvent("play",safeName()+" played "+title+" • "+result);
        new AlertDialog.Builder(this).setTitle(title).setMessage(result).setPositiveButton("Play again",(d,w)->simplePrizeEvent(title,prizes)).setNegativeButton("Close",null).show();
    }

    private void simpleRaceEvent(){
        // An actual timed touch game, not the old random winner message.
        // This is a FREE local skill challenge whose result is visible in room events.
        final long[] started={0L};
        final int[] taps={0};
        final boolean[] finished={false};
        final android.os.Handler clock=new android.os.Handler(android.os.Looper.getMainLooper());
        final Runnable[] finish={null};
        LinearLayout panel=new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(18),dp(16),dp(18),dp(20));
        panel.setBackgroundColor(0xff1d1835);
        TextView status=tv("Tap START, then race for 8 seconds!",18,Color.WHITE,true);
        status.setGravity(Gravity.CENTER);
        panel.addView(status,new LinearLayout.LayoutParams(-1,dp(84)));
        TextView tap=tv("🏎️ START RACE",24,0xffffdd60,true);
        tap.setGravity(Gravity.CENTER);
        tap.setBackground(bg(0xff423078,20));
        panel.addView(tap,new LinearLayout.LayoutParams(-1,dp(130)));
        final AlertDialog dialog=new AlertDialog.Builder(this)
            .setTitle("🏎️ KING Tap Sprint • FREE")
            .setView(panel).setNegativeButton("Close",null).create();
        finish[0]=()->{
            if(finished[0]||!dialog.isShowing())return;
            finished[0]=true;
            long elapsed=android.os.SystemClock.elapsedRealtime()-started[0];
            int score=KingTapRace9932.score(taps[0],elapsed);
            tap.setText("🏁 "+score+" points");
            status.setText("Finished • "+taps[0]+" taps in 8 seconds\nNo Coins or Diamonds awarded");
            addEvent("play",safeName()+" finished KING Tap Sprint • "+score+" skill points (FREE)");
        };
        tap.setOnClickListener(v->{
            if(finished[0])return;
            if(started[0]==0){
                started[0]=android.os.SystemClock.elapsedRealtime();
                clock.postDelayed(finish[0],KingTapRace9932.DURATION_MS);
            }
            long elapsed=android.os.SystemClock.elapsedRealtime()-started[0];
            if(elapsed>=KingTapRace9932.DURATION_MS){finish[0].run();return;}
            taps[0]++;
            tap.setText("🏎️ TAP FASTER! • "+taps[0]);
            status.setText("⏱ "+Math.max(1,(KingTapRace9932.DURATION_MS-elapsed+999)/1000)+"s remaining");
        });
        dialog.setOnDismissListener(d->clock.removeCallbacks(finish[0]));
        dialog.show();
    }

    private void addQuickMessageStrip(LinearLayout host){
        HorizontalScrollView hsv=new HorizontalScrollView(this);hsv.setHorizontalScrollBarEnabled(false);LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);
        String[] q={"Nice to meet everyone!","😂😂😂😂😂","Hi","Nice","Welcome","Good morning"};
        for(String text:q){TextView chip=tv(text,11,0xff403a46,true);chip.setGravity(Gravity.CENTER);chip.setBackground(bg(0xfff7f7fb,18));chip.setOnClickListener(v->{if(composerBox!=null){composerBox.setText(text);sendMessage(composerBox);}});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,dp(34));lp.setMargins(dp(3),dp(2),dp(3),dp(2));row.addView(chip,lp);}
        hsv.addView(row);host.addView(hsv,new LinearLayout.LayoutParams(-1,dp(40)));
    }

    private void seatLayoutPanel(){
        if(!isOwner()){roomSeatPanel();return;}
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(16),dp(12),dp(16),dp(18));root.setBackgroundColor(0xff242333);
        TextView title=tv("Room seat layout",22,Color.WHITE,true);title.setGravity(Gravity.CENTER);root.addView(title,new LinearLayout.LayoutParams(-1,dp(48)));
        LinearLayout tabs=new LinearLayout(this);
        TextView noHost=pill("No host",!hostSeatMode?0xffffd21c:0xff353444,()->{hostSeatMode=false;persistSeatLayout();seatLayoutPanel();});noHost.setTextColor(!hostSeatMode?Color.BLACK:Color.WHITE);tabs.addView(noHost,new LinearLayout.LayoutParams(0,dp(46),1));
        TextView hasHost=pill("Has a host",hostSeatMode?0xffffd21c:0xff353444,()->{hostSeatMode=true;if(maxSeats<10)maxSeats=10;persistSeatLayout();seatLayoutPanel();});hasHost.setTextColor(hostSeatMode?Color.BLACK:Color.WHITE);LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(0,dp(46),1);hp.setMargins(dp(5),0,0,0);tabs.addView(hasHost,hp);root.addView(tabs,new LinearLayout.LayoutParams(-1,dp(50)));
        LinearLayout choices=new LinearLayout(this);choices.setGravity(Gravity.CENTER);int[] nums=hostSeatMode?new int[]{10,12}:new int[]{8,10,12};
        for(int n:nums){TextView card=tv("● ● ● ●\n● ● ● ●\n"+n+" people",13,n==maxSeats?0xffffd21c:Color.WHITE,true);card.setGravity(Gravity.CENTER);card.setBackground(bg(n==maxSeats?0xff484215:0xff17171f,12));card.setOnClickListener(v->{for(Integer occupied:seatNames.keySet())if(occupied>n){toast("Move members out of seats above "+n+" first");return;}maxSeats=n;persistSeatLayout();rebuildSeats();toast("Seat layout: "+n);});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(124),1);lp.setMargins(dp(4),dp(8),dp(4),dp(8));choices.addView(card,lp);}
        root.addView(choices,new LinearLayout.LayoutParams(-1,dp(142)));
        TextView confirm=pill("Confirm",0xffffeb11,()->{persistSeatLayout();toast("Seat layout saved");renderParty();});confirm.setTextColor(Color.BLACK);root.addView(confirm,new LinearLayout.LayoutParams(-1,dp(58)));
        AlertDialog d=new AlertDialog.Builder(this).setView(root).create();d.setOnShowListener(v->{android.view.Window w=d.getWindow();if(w!=null){w.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);w.setGravity(Gravity.BOTTOM);w.setBackgroundDrawable(bg(0xff242333,22));}});d.show();
    }

    private void persistSeatLayout(){
        if(cloudRoom&&isOwner()&&db!=null){Map<String,Object> u=new HashMap<>();u.put("maxSeats",maxSeats);u.put("hostSeatMode",hostSeatMode);u.put("updatedAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).update(u).addOnFailureListener(e->toast(msg(e)));}
        else prefs.edit().putInt("maxSeats_"+roomName,maxSeats).putBoolean("hostSeatMode_"+roomId,hostSeatMode).apply();
    }

    private void giftSendingCounterDialog(){
        if(!cloudRoom||db==null){new AlertDialog.Builder(this).setTitle("Gift sending counter").setMessage("Live gift counting starts in a Firebase room.").setPositiveButton("OK",null).show();return;}
        db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(150).get().addOnSuccessListener(snap->{
            long charisma=0;Map<String,Integer> senders=new HashMap<>();
            for(DocumentSnapshot d:snap.getDocuments())if("gift".equals(d.getString("type"))){Long val=d.getLong("giftValue");long v=val==null?0:val;String target=d.getString("targetUid");if(user!=null&&user.getUid().equals(target))charisma+=Math.max(1,v/10);String actor=str(d,"actorName","User");senders.put(actor,senders.containsKey(actor)?senders.get(actor)+1:1);}
            List<Map.Entry<String,Integer>> rank=new ArrayList<>(senders.entrySet());java.util.Collections.sort(rank,(a,b)->b.getValue()-a.getValue());StringBuilder out=new StringBuilder("Charisma: ").append(charisma).append("\n\nGift senders\n");int i=0;for(Map.Entry<String,Integer> e:rank){out.append(++i).append(". ").append(e.getKey()).append(" • ").append(e.getValue()).append(" gifts\n");if(i>=8)break;}if(rank.isEmpty())out.append("No gifts yet\n");
            new AlertDialog.Builder(this).setTitle("Gift sending counter").setMessage(out.toString()).setPositiveButton("Start Game",(d,w)->addEvent("counter",safeName()+" started Gift sending counter ⭐")).setNeutralButton("Gift Shop",(d,w)->giftShopPanel()).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast(msg(e)));
    }

    private void roomBillboardDialog(){
        String msg=(announcement==null||announcement.trim().isEmpty())?"Welcome to the room":announcement;
        new AlertDialog.Builder(this).setTitle("▣ Billboard").setMessage(msg).setPositiveButton(isModerator()?"Edit":"OK",(d,w)->{if(isModerator())editAnnouncement();}).setNeutralButton("Room info",(d,w)->roomInfoDialog()).setNegativeButton("Close",null).show();
    }

    private void roomControlCenter(){
        String[] items={"▣ Billboard","🪑 Room seat layout","🛡 Administrator","🎨 Theme Room","📢 Announcement","⚙ Room settings","👥 Channel / Group","🎯 Templates & Events"};
        new AlertDialog.Builder(this).setTitle("Edit room").setItems(items,(d,w)->{String x=items[w];if(x.contains("Billboard"))roomBillboardDialog();else if(x.contains("seat"))seatLayoutPanel();else if(x.contains("Administrator"))administratorDialog();else if(x.contains("Theme"))themeRoomPanel();else if(x.contains("Announcement"))editAnnouncement();else if(x.contains("settings"))roomSettingsPanel();else if(x.contains("Channel"))channelPanel();else activityCenterPanel();}).setNegativeButton("Close",null).show();
    }

    private void administratorDialog(){
        if(!cloudRoom||db==null||roomId==null||!memberSeen){
            toast("Join an active Firebase Party first");return;
        }
        if(!isModerator()){
            new AlertDialog.Builder(this).setTitle("Administrator")
                .setMessage("Only Room Host and authorized administrators can manage roles.")
                .setPositiveButton("OK",null).show();return;
        }
        db.collection("live_rooms").document(roomId).collection("roles")
          .get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(snapshot->{
              List<String> labels=new ArrayList<>();
              List<String> uids=new ArrayList<>();
              labels.add("👑 Host • "+(ownerName==null?"KING Host":ownerName));uids.add("");
              for(DocumentSnapshot entry:snapshot.getDocuments()){
                  if(!"cohost".equals(entry.getString("role")))continue;
                  labels.add("🛡 Administrator • "+memberNameForUid(entry.getId()));
                  uids.add(entry.getId());
              }
              int count=uids.size()-1;
              labels.add("ℹ Administrator permissions ("+count+"/5)");uids.add("");
              if(isOwner()&&count<5){labels.add("＋ Add administrator");uids.add("");}
              new AlertDialog.Builder(this).setTitle("🛡 Room Administrators")
                .setItems(labels.toArray(new String[0]),(d,w)->{
                    String label=labels.get(w);
                    if(label.startsWith("＋")){chooseAdministrator(count);return;}
                    if(label.startsWith("ℹ")){
                        new AlertDialog.Builder(this).setTitle("Admin permissions")
                          .setMessage("Admins can moderate mic seats, members and announcements. "+
                                      "Only the Host can add/remove administrators.")
                          .setPositiveButton("OK",null).show();return;
                    }
                    String selectedUid=uids.get(w);
                    if(selectedUid.isEmpty()||!isOwner())return;
                    final String currentRoom=roomId;
                    new AlertDialog.Builder(this).setTitle("Remove Administrator?")
                      .setMessage("Remove the Administrator role from "+label+
                                  "? The user remains in the Party.")
                      .setNegativeButton("Cancel",null)
                      .setPositiveButton("Remove",(x,y)->{
                          if(!isOwner()||!currentRoom.equals(roomId))return;
                          db.collection("live_rooms").document(currentRoom)
                            .collection("roles").document(selectedUid).delete()
                            .addOnSuccessListener(v->{
                                if(!isFinishing()&&!isDestroyed()){
                                    toast("Administrator role removed");administratorDialog();
                                }
                            }).addOnFailureListener(e->toast("Admin update failed: "+msg(e)));
                      }).show();
                }).setNegativeButton("Close",null).show();
          }).addOnFailureListener(e->toast("Admin list unavailable: "+msg(e)));
    }

    private void chooseAdministrator(int current){
        if(!isOwner()){toast("Host only");return;}if(current>=5){toast("Maximum 5 administrators");return;}
        List<String> names=new ArrayList<>();List<String> ids=new ArrayList<>();for(String n:memberNames){String uid=memberUids.get(n);if(uid!=null&&!uid.isEmpty()&&!uid.equals(ownerUid)&&!ids.contains(uid)){names.add(n);ids.add(uid);}}
        if(names.isEmpty()){toast("No eligible online members");return;}
        new AlertDialog.Builder(this).setTitle("Search online users").setItems(names.toArray(new String[0]),(d,w)->{String uid=ids.get(w);Map<String,Object> role=new HashMap<>();role.put("uid",uid);role.put("role","cohost");role.put("createdAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).collection("roles").document(uid).set(role).addOnSuccessListener(v->{addEvent("admin",names.get(w)+" became an administrator");toast("Administrator added");}).addOnFailureListener(e->toast(msg(e)));}).setNegativeButton("Close",null).show();
    }

    private void roomSettingsPanel(){
        loadRoomPersonalSettings();
        String[] labels={skipEntranceVisuals?"✓ Skip entrance effects":"○ Skip entrance effects",skipEntranceSound?"✓ Block entrance sound":"○ Block entrance sound",skipEntranceVibration?"✓ Block entrance vibration":"○ Block entrance vibration","🚫 Block this room locally","👥 Channel / Group","⚑ Report the group"};
        new AlertDialog.Builder(this).setTitle("Room settings").setItems(labels,(d,w)->{if(w==0){skipEntranceVisuals=!skipEntranceVisuals;saveRoomPersonalSettings();}else if(w==1){skipEntranceSound=!skipEntranceSound;saveRoomPersonalSettings();}else if(w==2){skipEntranceVibration=!skipEntranceVibration;saveRoomPersonalSettings();}else if(w==3){Set<String>b=new HashSet<>(prefs.getStringSet("blocked_rooms",new HashSet<>()));b.add(roomId);prefs.edit().putStringSet("blocked_rooms",b).apply();toast("Room blocked locally");}else if(w==4)channelPanel();else reportHost();}).setNegativeButton("Close",null).show();
    }

    private void loadRoomPersonalSettings(){if(prefs==null||roomId==null)return;skipEntranceVisuals=prefs.getBoolean("skip_fx_"+roomId,false);skipEntranceSound=prefs.getBoolean("skip_sound_"+roomId,false);skipEntranceVibration=prefs.getBoolean("skip_vibe_"+roomId,false);}
    private void saveRoomPersonalSettings(){prefs.edit().putBoolean("skip_fx_"+roomId,skipEntranceVisuals).putBoolean("skip_sound_"+roomId,skipEntranceSound).putBoolean("skip_vibe_"+roomId,skipEntranceVibration).apply();toast("Room preference saved");}

    private void channelPanel(){
        String[] items={"👥 Members ("+(cloudRoom?liveMemberCount:Math.max(1,seatNames.size()))+")","🟢 Party & Live in the channel","▣ Billboard","🎨 Channel theme","👤 Channel profile","🤖 Robot","🛡 Administrator","⚙ Room settings","⚑ Report the group"};
        new AlertDialog.Builder(this).setTitle("From the channel • "+roomName).setItems(items,(d,w)->{String x=items[w];if(x.contains("Members"))membersDialog();else if(x.contains("Party & Live"))roomInfoDialog();else if(x.contains("Billboard"))roomBillboardDialog();else if(x.contains("theme"))themeRoomPanel();else if(x.contains("profile"))channelProfile940();else if(x.contains("Robot"))roomRobotDialog();else if(x.contains("Administrator"))administratorDialog();else if(x.contains("settings"))roomSettingsPanel();else reportHost();}).setNegativeButton("Close",null).show();
    }

    private void channelProfile940(){
        if(!cloudRoom||db==null||roomId==null){
            new AlertDialog.Builder(this).setTitle("👥 Channel Profile").setMessage("Channel: "+roomName+"\nMembers: "+Math.max(1,seatNames.size())+"\n\nLive group profile is available in Firebase Party rooms.").setPositiveButton("OK",null).show();return;
        }
        DocumentReference ref=db.collection("live_rooms").document(roomId).collection("room_settings").document("group_profile");
        ref.get().addOnSuccessListener(doc->{
            String groupName=doc!=null&&doc.exists()?str(doc,"name",roomName):roomName;
            String description=doc!=null&&doc.exists()?str(doc,"description","KING Plus Party group"):"KING Plus Party group";
            String code=shortId(roomId);
            String msg="👥 "+groupName+"\n\n"+description+"\n\nGroup code: "+code+"\nOnline members: "+liveMemberCount+"\nHost: "+(ownerName==null?"KING Host":ownerName);
            AlertDialog.Builder b=new AlertDialog.Builder(this).setTitle("Channel / Group Profile").setMessage(msg)
                .setPositiveButton("Share",(d,w)->shareRoom())
                .setNegativeButton("Close",null);
            if(isModerator())b.setNeutralButton("Edit",(d,w)->editChannelProfile940(ref,groupName,description));
            b.show();
        }).addOnFailureListener(e->toast("Channel profile unavailable: "+msg(e)));
    }
    private void editChannelProfile940(DocumentReference ref,String currentName,String currentDescription){
        if(!isModerator()){toast("Host/co-host only");return;}
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(20),0,dp(20),0);
        EditText name=new EditText(this);name.setHint("Channel / group name");name.setSingleLine(true);name.setText(currentName);box.addView(name,new LinearLayout.LayoutParams(-1,dp(54)));
        EditText desc=new EditText(this);desc.setHint("Description");desc.setText(currentDescription);desc.setMinLines(2);desc.setMaxLines(4);box.addView(desc,new LinearLayout.LayoutParams(-1,dp(92)));
        new AlertDialog.Builder(this).setTitle("Edit Channel Profile").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{
            String n=name.getText().toString().trim(),s=desc.getText().toString().trim();if(n.length()<2){toast("Enter a group name");return;}if(n.length()>40)n=n.substring(0,40);if(s.length()>180)s=s.substring(0,180);
            Map<String,Object>x=new HashMap<>();x.put("name",n);x.put("description",s);x.put("actorUid",user==null?"":user.getUid());x.put("updatedAt",FieldValue.serverTimestamp());
            ref.set(x,SetOptions.merge()).addOnSuccessListener(v->{addEvent("group_profile",safeName()+" updated the Channel profile");toast("Channel profile updated");}).addOnFailureListener(e->toast("Could not update Channel profile: "+msg(e)));
        }).show();
    }

    private void roomRobotDialog(){
        String[] cmds={"Welcome new members","Post safety reminder","Start a room vote","Open Templates & Events"};
        new AlertDialog.Builder(this).setTitle("🤖 KING Room Robot").setItems(cmds,(d,w)->{if(w==0)addEvent("robot","🤖 Welcome to "+roomName+"! Be kind and enjoy the party.");else if(w==1)addEvent("robot","🤖 Safety reminder: keep the room respectful and report prohibited content.");else if(w==2)votePanel();else activityCenterPanel();}).setNegativeButton("Close",null).show();
    }

    private GradientDrawable premiumRoomBackground(){
        int a=0xff07956f,b=0xff04b4a3,c=0xff062f38;
        if("KTV".equals(roomTheme)){a=0xff15245e;b=0xff1b5c88;c=0xff07182f;}
        else if("PK".equals(roomTheme)){a=0xff7a193a;b=0xff392a75;c=0xff153c82;}
        else if("Love".equals(roomTheme)){a=0xff6b1739;b=0xffa3315c;c=0xff2b1021;}
        else if("Game".equals(roomTheme)){a=0xff078d70;b=0xff0aaf93;c=0xff073a37;}
        else if("Royal".equals(roomTheme)){a=0xff2f1c5e;b=0xff67429d;c=0xff17102f;}
        else if("Neon".equals(roomTheme)){a=0xff071b2e;b=0xff0b8f8a;c=0xff260a49;}
        else if("Galaxy".equals(roomTheme)){a=0xff080d2a;b=0xff40206f;c=0xff140c38;}
        else if("Festival".equals(roomTheme)){a=0xff6d1f31;b=0xffd46b1d;c=0xff2c1234;}
        else if("Ice".equals(roomTheme)){a=0xff0c3651;b=0xff49a7c7;c=0xffd2f5ff;}
        GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{a,b,c});g.setGradientType(GradientDrawable.LINEAR_GRADIENT);return g;
    }

    private void addRoomBackgroundGlow(FrameLayout shell){
        View glow=new View(this);GradientDrawable gd=new GradientDrawable();gd.setShape(GradientDrawable.OVAL);gd.setColors(new int[]{0x33ffffff,0x00ffffff});glow.setBackground(gd);glow.setAlpha(.45f);FrameLayout.LayoutParams gp=new FrameLayout.LayoutParams(dp(290),dp(290));gp.gravity=Gravity.TOP|Gravity.RIGHT;gp.topMargin=dp(90);gp.rightMargin=-dp(130);shell.addView(glow,gp);
        View glow2=new View(this);GradientDrawable gd2=new GradientDrawable();gd2.setShape(GradientDrawable.OVAL);gd2.setColors(new int[]{0x22ffe179,0x00ffffff});glow2.setBackground(gd2);FrameLayout.LayoutParams g2=new FrameLayout.LayoutParams(dp(250),dp(250));g2.gravity=Gravity.BOTTOM|Gravity.LEFT;g2.bottomMargin=dp(120);g2.leftMargin=-dp(130);shell.addView(glow2,g2);
    }

    private void weeklyGiftCardDialog(){ weeklyGiftCardDialog(prefs.getString("weekly_preview_tier","Silver")); }
    private void weeklyGiftCardDialog(String tier){
        boolean gold="Gold".equals(tier);prefs.edit().putString("weekly_preview_tier",tier).apply();
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(12),dp(10),dp(12),dp(10));root.setBackgroundColor(0xff8858dc);
        TextView title=tv("WEEKLY GIFT\nCARD",29,Color.WHITE,true);title.setGravity(Gravity.CENTER);root.addView(title,new LinearLayout.LayoutParams(-1,dp(86)));
        TextView sub=tv("Grants TEST rewards for 7 days after activation",12,0xfffff3d2,true);sub.setGravity(Gravity.CENTER);root.addView(sub);
        LinearLayout tabs=new LinearLayout(this);tabs.setGravity(Gravity.CENTER);AlertDialog[] holder=new AlertDialog[1];TextView silver=pill("Silver Card",gold?0xff7148b9:0xffffde73,()->{if(holder[0]!=null)holder[0].dismiss();weeklyGiftCardDialog("Silver");});silver.setTextColor(gold?Color.WHITE:0xff5a3a08);tabs.addView(silver,new LinearLayout.LayoutParams(0,dp(46),1));TextView goldTab=pill("Gold Card",gold?0xffffde73:0xff7148b9,()->{if(holder[0]!=null)holder[0].dismiss();weeklyGiftCardDialog("Gold");});goldTab.setTextColor(gold?0xff5a3a08:Color.WHITE);LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(0,dp(46),1);gp.setMargins(dp(5),0,0,0);tabs.addView(goldTab,gp);root.addView(tabs);
        ScrollView sc=new ScrollView(this);LinearLayout days=new LinearLayout(this);days.setOrientation(LinearLayout.VERTICAL);days.setPadding(0,dp(6),0,dp(6));long activated=prefs.getLong("weekly_"+tier+"_activated",0L);String claimed=prefs.getString("weekly_"+tier+"_claimed","");int unlocked=activated==0?0:(int)Math.min(7,1+((System.currentTimeMillis()-activated)/(24L*60*60*1000)));
        for(int rowNo=0;rowNo<4;rowNo++){LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);for(int col=0;col<2;col++){int day=rowNo*2+col+1;if(day>7){TextView blank=tv("",12,Color.WHITE,false);row.addView(blank,new LinearLayout.LayoutParams(0,dp(130),1));continue;}final int d=day;boolean done=claimed.contains(","+day+",");boolean open=day<=unlocked;int reward=(gold?150:50)*day;TextView card=tv("Day "+day+"\n\n"+(gold?"🏝️   👑":"🎁   👑")+"\n\n"+(done?"✓ Claimed":open?"CLAIM":"🔒 Locked"),13,gold?0xff6c4210:0xff5c30a0,true);card.setGravity(Gravity.CENTER);card.setBackground(bg(gold?0xffffefc7:0xffeee2ff,16));card.setOnClickListener(v->claimWeeklyReward(tier,d,reward));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(130),1);cp.setMargins(dp(4),dp(4),dp(4),dp(4));row.addView(card,cp);}days.addView(row,new LinearLayout.LayoutParams(-1,dp(138)));}
        sc.addView(days);root.addView(sc,new LinearLayout.LayoutParams(-1,dp(410)));int price=gold?756:75;TextView activate=pill((activated==0?"₹ ":"Active • ")+price+"  •  "+tier,0xffffd95a,()->{if(prefs.getLong("weekly_"+tier+"_activated",0L)==0)activateWeeklyCard(tier,price);else toast(tier+" card is active");});activate.setTextSize(21);activate.setTextColor(0xff5a3500);root.addView(activate,new LinearLayout.LayoutParams(-1,dp(58)));holder[0]=new AlertDialog.Builder(this).setView(root).setNegativeButton("Close",null).create();holder[0].show();
    }
    private void activateWeeklyCard(String tier,int price){
        if(localCoins<price){toast("Need "+price+" TEST coins");return;}localCoins-=price;prefs.edit().putInt("coins",localCoins).putLong("weekly_"+tier+"_activated",System.currentTimeMillis()).putString("weekly_"+tier+"_claimed","").apply();toast(tier+" Weekly Gift Card activated in TEST mode");weeklyGiftCardDialog(tier);
    }
    private void claimWeeklyReward(String tier,int day,int reward){
        long activated=prefs.getLong("weekly_"+tier+"_activated",0L);if(activated==0){toast("Activate the "+tier+" card first");return;}
        int unlocked=(int)Math.min(7,1+((System.currentTimeMillis()-activated)/(24L*60*60*1000)));if(day>unlocked){toast("Day "+day+" unlocks later");return;}
        String key="weekly_"+tier+"_claimed";String claimed=prefs.getString(key,"");String token=","+day+",";if(claimed.contains(token)){toast("Day "+day+" already claimed");return;}
        localCoins+=reward;prefs.edit().putInt("coins",localCoins).putString(key,claimed+token).apply();addEvent("weekly",safeName()+" claimed "+tier+" Weekly Card Day "+day+" reward 🎁");toast("Claimed "+reward+" TEST coins • balance "+localCoins);weeklyGiftCardDialog(tier);
    }

    private void refreshPeopleCounts(){
        int members=cloudRoom?liveMemberCount:Math.max(1,seatNames.size()+1);
        int onMic=seatUids.isEmpty()?seatNames.size():seatUids.size();
        if(viewerLabel!=null)viewerLabel.setText("👥 "+members);
        if(roomLiveBadge!=null)roomLiveBadge.setText("👥 "+members+" • 🎙 "+onMic);
        refreshRoomState();
    }

    private void refreshRoomState(){
        if(roomStateLabel==null)return;
        String access="family".equals(roomEntranceMode9931)?"👪 Family members":"follow".equals(roomEntranceMode9931)?"💜 Host follows":roomPrivate?(roomHasPassword?"🔑 Password":"🔐 Invite only"):"🌐 Free";
        String role=isOwner()?"👑 Host":(coHost?"🛡 Co-host":"👤 Member");
        String lock=roomLocked?"🔒 Seats locked":"🔓 Seats open";
        String mute=muteAll?"🔇 Mute all":"🎤 Voice open";
        String req=isModerator()?" • 📥 "+pendingSeatRequests:"";
        int occupied=seatUids.isEmpty()?seatNames.size():seatUids.size();
        roomStateLabel.setText(access+"  •  "+role+"  •  "+lock+"  •  "+mute+"  •  👥 "+(cloudRoom?liveMemberCount:Math.max(1,memberNames.size()))+"/"+maxMembers930+"  •  🎙 "+occupied+"/"+maxSeats+req);
    }

    private void addModeratorControls(){
        LinearLayout admin=new LinearLayout(this);admin.setGravity(Gravity.CENTER);admin.setPadding(0,0,0,dp(6));
        addQuick(admin,"🛡\nAdmin",this::moderationPanel);
        seatRequestLabel=pill("📥\nRequests "+pendingSeatRequests,CARD,this::allSeatRequestsDialog);seatRequestLabel.setTextSize(11);
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,dp(56),1);rp.setMargins(dp(2),0,dp(2),0);admin.addView(seatRequestLabel,rp);
        addQuick(admin,roomLocked?"🔓\nUnlock":"🔒\nLock",()->setRoomFlag("locked",!roomLocked));
        addQuick(admin,muteAll?"🎤\nUnmute":"🔇\nMute all",()->setRoomFlag("muteAll",!muteAll));
        page.addView(admin,new LinearLayout.LayoutParams(-1,dp(62)));
    }

    private void moderationPanel(){
        if(!isModerator()){toast("Host/co-host only");return;}
        List<String> items=new ArrayList<>();
        items.add("👥 Manage members");
        items.add("📥 Seat request center ("+pendingSeatRequests+")");
        items.add(roomLocked?"🔓 Unlock all seat joining":"🔒 Lock all seat joining");
        items.add(muteAll?"🎤 Unmute all seats":"🔇 Mute all seats");
        items.add("🔓 Unlock individual seat locks");
        items.add("⬇ Clear all mic seats");
        items.add("🚫 Banned users");
        items.add("📢 Edit announcement");
        if(isOwner()){
            items.add("👑 Co-host list");
            items.add("🚪 Channel Entrance Settings");
            items.add("🎙 Mic seat count");
            items.add(roomPrivate?"🌐 Make room public":"🔐 Make room private");
            items.add(roomHasPassword?"🔑 Change room password":"🔑 Set room password");
            if(roomHasPassword)items.add("🗑 Remove room password");
            items.add("⛔ Close room");
        }
        String[] a=items.toArray(new String[0]);
        new AlertDialog.Builder(this).setTitle("🛡 Host controls").setItems(a,(d,w)->{
            String x=a[w];
            if(x.contains("Manage members"))membersDialog();
            else if(x.contains("Seat request center"))allSeatRequestsDialog();
            else if(x.contains("Lock all")||x.contains("Unlock all seat"))setRoomFlag("locked",!roomLocked);
            else if(x.contains("Mute all")||x.contains("Unmute all"))setRoomFlag("muteAll",!muteAll);
            else if(x.contains("individual seat locks"))unlockAllIndividualSeats();
            else if(x.contains("Clear all mic"))clearAllSeats();
            else if(x.contains("Banned users"))banListDialog();
            else if(x.contains("announcement"))editAnnouncement();
            else if(x.contains("Co-host list"))coHostListDialog();
            else if(x.contains("Channel Entrance Settings"))channelEntranceSettings9931();
            else if(x.contains("Mic seat count"))changeSeatCount();
            else if(x.contains("private")||x.contains("public"))setPrivateRoom(!roomPrivate);
            else if(x.contains("Set room password")||x.contains("Change room password"))setRoomPasswordDialog();
            else if(x.contains("Remove room password"))removeRoomPassword();
            else if(x.contains("Close room"))closeRoom();
        }).setNegativeButton("Close",null).show();
    }

    private void unlockAllIndividualSeats(){
        if(!isModerator()){toast("Host/co-host only");return;}
        if(!cloudRoom||db==null){lockedSeats.clear();rebuildSeats();toast("Seat locks cleared");return;}
        if(lockedSeats.isEmpty()){toast("No individual seat locks");return;}
        WriteBatch batch=db.batch();
        for(Integer no:new HashSet<>(lockedSeats))batch.delete(db.collection("live_rooms").document(roomId).collection("seat_locks").document(String.valueOf(no)));
        batch.commit().addOnSuccessListener(v->{lockedSeats.clear();rebuildSeats();addEvent("seat_lock",safeName()+" unlocked all mic seats");toast("All individual seat locks cleared");})
            .addOnFailureListener(e->toast("Unlock failed: "+msg(e)));
    }

    private void clearAllSeats(){
        if(!isModerator()){toast("Host/co-host only");return;}
        new AlertDialog.Builder(this).setTitle("Clear all mic seats").setMessage("Remove everyone from mic seats? Members will stay in the room.")
            .setNegativeButton("Cancel",null).setPositiveButton("Clear",(d,w)->{
                if(!cloudRoom||db==null){seatNames.clear();seatUids.clear();seatMics.clear();mySeat=-1;micOn=false;rebuildSeats();toast("Mic seats cleared");return;}
                WriteBatch batch=db.batch();
                for(Integer no:new HashSet<>(seatUids.keySet()))batch.delete(db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(no)));
                batch.commit().addOnSuccessListener(v->{addEvent("seat",safeName()+" cleared all mic seats");toast("All mic seats cleared");})
                    .addOnFailureListener(e->toast("Clear seats failed: "+msg(e)));
            }).show();
    }

    private void assignMemberToSeatDialog(int no){
        if(!isModerator()||!cloudRoom||db==null){toast("Live host/co-host only");return;}
        List<String> labels=new ArrayList<>();List<String> ids=new ArrayList<>();
        for(String n:memberNames){
            String uid=memberUids.get(n);if(uid==null||uid.isEmpty())continue;
            labels.add(n);ids.add(uid);
        }
        if(labels.isEmpty()){toast("No members available");return;}
        new AlertDialog.Builder(this).setTitle("Assign Seat "+no).setItems(labels.toArray(new String[0]),(d,w)->moveUserToSeat(ids.get(w),labels.get(w),-1,no))
            .setNegativeButton("Close",null).show();
    }

    private void moveUserToSeatDialog(String uid,String name,int currentSeat){
        if(!isModerator()||uid==null||uid.isEmpty()){toast("Host/co-host only");return;}
        List<Integer> seats=new ArrayList<>();List<String> labels=new ArrayList<>();
        for(int no=1;no<=maxSeats;no++){
            if(no==currentSeat||seatUids.get(no)!=null)continue;
            seats.add(no);labels.add("Seat "+no+(lockedSeats.contains(no)?" • locked":""));
        }
        if(seats.isEmpty()){toast("No empty mic seat");return;}
        new AlertDialog.Builder(this).setTitle("Move "+name).setItems(labels.toArray(new String[0]),(d,w)->moveUserToSeat(uid,name,currentSeat,seats.get(w)))
            .setNegativeButton("Cancel",null).show();
    }

    private void moveUserToSeat(String uid,String name,int currentSeat,int targetSeat){
        approveOrMoveMemberSeat9913(uid,name,currentSeat,targetSeat,null,"Member moved");
    }

    private void grantCurrentMembersAccessThen(Runnable done){
        if(!cloudRoom||db==null||user==null){done.run();return;}
        WriteBatch batch=db.batch();Set<String> ids=new HashSet<>(memberUids.values());ids.add(user.getUid());
        for(String uid:ids){
            if(uid==null||uid.isEmpty())continue;
            Map<String,Object> grant=new HashMap<>();grant.put("uid",uid);grant.put("grantedBy",user.getUid());grant.put("entryEpoch",roomEntryEpoch9936);grant.put("createdAt",FieldValue.serverTimestamp());
            batch.set(db.collection("live_rooms").document(roomId).collection("access").document(uid),grant);
        }
        batch.commit().addOnSuccessListener(v->done.run()).addOnFailureListener(e->toast("Could not preserve current member access: "+msg(e)));
    }

    private String roomFavKey940(String id,String name){
        return (id==null?"":id.replace("\u0001"," "))+"\u0001"+(name==null?"Live Party":name.replace("\u0001"," "));
    }
    private boolean isFavoriteRoom940(){
        if(!cloudRoom||roomId==null)return false;
        Set<String> fav=new HashSet<>(prefs.getStringSet(KingAccountRoomHistory9989.key("favorite_rooms_940",user==null?null:user.getUid()),new HashSet<>()));
        for(String x:fav){int p=x.indexOf('\u0001');String id=p<0?x:x.substring(0,p);if(roomId.equals(id))return true;}
        return false;
    }
    private boolean savingFavorite9990;
    private void toggleFavoriteRoom940(){
        if(!cloudRoom||roomId==null||user==null||savingFavorite9990)return;
        savingFavorite9990=true;
        KingSavedRoomsCloud9990.change(this,db,user.getUid(),roomId,roomName,true,error->{
            savingFavorite9990=false;if(partyUiDead())return;
            toast(error==null?"Favorite updated on your account":"Could not save favorite. Check connection; maximum 60 favorites.");
        });
    }
    private void recordRoomVisit940(){
        if(!cloudRoom||roomId==null||user==null)return;
        KingSavedRoomsCloud9990.change(this,db,user.getUid(),roomId,roomName,false,error->{
            if(error!=null)KingStability.nonFatal(this,"saved-room-visit",error);
        });
    }
    private void roomHistoryPanel940(){
        Intent intent=new Intent(this,KingSavedRooms9990Activity.class);intent.putExtra("tab","recent");startActivity(intent);
    }
    private com.google.firebase.firestore.ListenerRegistration savedRooms9990;
    private int savedRoomsGeneration9990;
    private void stopSavedRooms9990(){
        ++savedRoomsGeneration9990;
        if(savedRooms9990!=null){savedRooms9990.remove();savedRooms9990=null;}
        savingFavorite9990=false;
    }
    private void listenSavedRooms9990(){
        stopSavedRooms9990();if(user==null||db==null)return;
        final String uid=user.getUid();final int generation=savedRoomsGeneration9990;
        savedRooms9990=db.collection("users").document(uid).addSnapshotListener((doc,error)->{
            if(generation!=savedRoomsGeneration9990||partyUiDead()||!KingSavedRoomsCloud9990.account(uid)||doc==null||error!=null)return;
            if(!doc.contains(KingSavedRoomsPolicy9990.FIELD)){
                if(!doc.getMetadata().isFromCache())KingSavedRoomsCloud9990.importLocal(this,db,uid);
                return;
            }
            KingSavedRoomsCloud9990.cache(this,uid,doc.get(KingSavedRoomsPolicy9990.FIELD));
        });
    }

    private void teamUpPanel940(){
        if(!cloudRoom||db==null||user==null||roomId==null){toast("Open a live Party room first");return;}
        db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(180).get().addOnSuccessListener(snap->{
            Set<String> closed=new HashSet<>();Map<String,Set<String>> joined=new HashMap<>();
            for(DocumentSnapshot d:snap.getDocuments()){
                String type=d.getString("type"),card=d.getString("teamCardId");
                if("team_up_close".equals(type)&&card!=null)closed.add(card);
                else if("team_up_join".equals(type)&&card!=null){Set<String> s=joined.get(card);if(s==null){s=new HashSet<>();joined.put(card,s);}String uid=d.getString("actorUid");if(uid!=null)s.add(uid);}
            }
            List<DocumentSnapshot> cards=new ArrayList<>();List<String> rows=new ArrayList<>();rows.add("＋ Create Team Up card");rows.add("⚡ Quick Match");long now=System.currentTimeMillis();
            for(DocumentSnapshot d:snap.getDocuments()){
                if(!"team_up_create".equals(d.getString("type"))||closed.contains(d.getId()))continue;
                com.google.firebase.Timestamp ts=d.getTimestamp("createdAt");if(ts!=null&&now-ts.toDate().getTime()>3600000L)continue;
                String game=str(d,"teamGame","Game"),host=str(d,"actorName","KING User");Long cap=d.getLong("teamCapacity");int capacity=cap==null?4:Math.max(2,Math.min(6,cap.intValue()));
                Set<String> members=joined.get(d.getId());int count=1+(members==null?0:members.size());if(count>=capacity)continue;
                cards.add(d);rows.add("🤝 "+game+"  •  "+host+"  •  "+count+"/"+capacity);if(cards.size()>=15)break;
            }
            new AlertDialog.Builder(this).setTitle("🤝 Team Up").setItems(rows.toArray(new String[0]),(dlg,w)->{
                if(w==0){createTeamUpCard940();return;}if(w==1){quickTeamUp940(cards);return;}DocumentSnapshot card=cards.get(w-2);String hostUid=card.getString("actorUid"),hostName=str(card,"actorName","KING User"),game=str(card,"teamGame","Game");
                if(user.getUid().equals(hostUid))teamUpOwnerMenu940(card,game);else joinTeamUpCard940(card,game,hostName);
            }).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast("Team Up unavailable: "+msg(e)));
    }
    private void quickTeamUp940(List<DocumentSnapshot> cards){
        if(cards!=null&&!cards.isEmpty()){
            for(DocumentSnapshot card:cards){String host=card.getString("actorUid");if(host!=null&&user!=null&&!user.getUid().equals(host)){joinTeamUpCard940(card,str(card,"teamGame","Game"),str(card,"actorName","KING User"));return;}}
        }
        publishTeamUpCard940("Ludo",4);toast("No open team found • created a 4-player Ludo team");
    }
    private void createTeamUpCard940(){
        String[] games={"Ludo","Werewolf","Spy","Draw & Guess","Domino","Dice"};
        new AlertDialog.Builder(this).setTitle("Choose Team Up game").setItems(games,(d,w)->{final String game=games[w];String[] sizes={"2 players","4 players","6 players"};new AlertDialog.Builder(this).setTitle(game+" team size").setItems(sizes,(x,k)->publishTeamUpCard940(game,new int[]{2,4,6}[k])).setNegativeButton("Cancel",null).show();}).setNegativeButton("Cancel",null).show();
    }
    private void publishTeamUpCard940(String game,int capacity){
        Map<String,Object> e=new HashMap<>();e.put("actorUid",user.getUid());e.put("actorName",safeName());e.put("type","team_up_create");e.put("text",safeName()+" created a "+game+" Team Up card");e.put("teamGame",game);e.put("teamCapacity",capacity);e.put("createdAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).collection("events").add(e).addOnSuccessListener(v->{toast("Team Up card published");teamUpPanel940();}).addOnFailureListener(x->toast("Could not create Team Up card: "+msg(x)));
    }
    private void joinTeamUpCard940(DocumentSnapshot card,String game,String hostName){
        Map<String,Object> e=new HashMap<>();e.put("actorUid",user.getUid());e.put("actorName",safeName());e.put("type","team_up_join");e.put("text",safeName()+" joined "+hostName+"'s "+game+" team");e.put("teamCardId",card.getId());e.put("teamGame",game);e.put("createdAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).collection("events").add(e).addOnSuccessListener(v->{toast("Joined Team Up • "+game);openTeamGame940(game);}).addOnFailureListener(x->toast("Could not join team: "+msg(x)));
    }
    private void teamUpOwnerMenu940(DocumentSnapshot card,String game){
        new AlertDialog.Builder(this).setTitle("Your Team Up • "+game).setItems(new String[]{"🎮 Start / open game","✖ Close Team Up card"},(d,w)->{
            if(w==0)openTeamGame940(game);else{Map<String,Object> e=new HashMap<>();e.put("actorUid",user.getUid());e.put("actorName",safeName());e.put("type","team_up_close");e.put("text",safeName()+" closed a Team Up card");e.put("teamCardId",card.getId());e.put("createdAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).collection("events").add(e).addOnSuccessListener(v->toast("Team Up card closed"));}
        }).setNegativeButton("Cancel",null).show();
    }
    private void openTeamGame940(String game){
        Intent i=new Intent(this,RoomGameActivity.class);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);i.putExtra("displayName",safeName());i.putExtra("requestedGame",game);startActivity(i);
    }

    /** First true GLES20 3D Party view, using existing Firebase seat permissions.
     * Keep the original Party Activity/voice visible behind the dialog.
     * The 📹 button routes to the already available Jitsi Multi Video room.
     */
    private void openNativeWorld998(){
        if(!cloudRoom||!memberSeen||db==null||user==null||roomId==null){
            toast("Join a real online KING Party first");return;
        }
        KingNative3DRoom.open(this,db,user,roomId,this::openVideoRoom);
    }

    /** Primary Party controls must be reachable without the 50+ item toolbox.
     * In-room voice/seat/game state remains in the SAME PartyActivity.
     * The long tool panel is still available as "All Tools".
     */
    private void roomMenu(){
        final java.util.List<String> quick=new java.util.ArrayList<>();
        quick.add(micOn?"🎤 Mic OFF":"🎤 Mic ON");
        quick.add("👥 Members");
        quick.add("🎮 Games");
        quick.add("💫 Interactive Emoji");
        quick.add("🎁 Gift Wall");
        quick.add("💬 Private chat");
        quick.add("📹 Multi Video");
        quick.add("🌐 3D World");
        quick.add("🔎 Find room user");
        if(isModerator())quick.add("🛡 Administrator");
        else quick.add("⚙ Room settings");
        quick.add("🔗 Share room");
        quick.add("⋯ All Tools");
        android.widget.ScrollView scroll=new android.widget.ScrollView(this);
        scroll.setFillViewport(false);
        scroll.setVerticalScrollBarEnabled(false);
        android.widget.LinearLayout panel=new android.widget.LinearLayout(this);
        panel.setOrientation(android.widget.LinearLayout.VERTICAL);
        panel.setPadding(dp(16),dp(14),dp(16),dp(20));
        panel.setBackgroundColor(0xfffcfbff);
        scroll.addView(panel);
        android.widget.TextView heading=tv("KING Party • Quick Tools",19,0xff282036,true);
        heading.setGravity(android.view.Gravity.CENTER_VERTICAL);
        panel.addView(heading,new android.widget.LinearLayout.LayoutParams(-1,dp(40)));
        android.widget.TextView intro=tv("Tap once to open • Your voice room stays active",12,0xff78707d,false);
        panel.addView(intro,new android.widget.LinearLayout.LayoutParams(-1,dp(30)));
        final android.app.AlertDialog[] dialog={null};
        android.widget.LinearLayout line=null;
        for(int i=0;i<quick.size();i++){
            if(i%3==0){
                line=new android.widget.LinearLayout(this);
                line.setGravity(android.view.Gravity.CENTER_VERTICAL);
                line.setWeightSum(3f);
                android.widget.LinearLayout.LayoutParams rowp=
                    new android.widget.LinearLayout.LayoutParams(-1,dp(88));
                rowp.setMargins(0,dp(4),0,dp(4));
                panel.addView(line,rowp);
            }
            final String item=quick.get(i);
            android.widget.TextView tile=tv(item,12,0xff292036,true);
            tile.setGravity(android.view.Gravity.CENTER);
            tile.setMaxLines(2);
            tile.setEllipsize(android.text.TextUtils.TruncateAt.END);
            tile.setContentDescription(item);
            tile.setBackground(bg(0xfff0ecf8,14));
            android.widget.LinearLayout.LayoutParams tp=
                new android.widget.LinearLayout.LayoutParams(0,dp(81),1f);
            tp.setMargins(dp(4),dp(3),dp(4),dp(3));
            line.addView(tile,tp);
            tile.setOnClickListener(v->{
                if(dialog[0]!=null)dialog[0].dismiss();
                if(item.contains("All Tools"))roomMenuAll();
                else runPartyAction("quick-tool:"+item,()->handleRoomMenuItem810(item));
            });
        }
        final android.app.AlertDialog sheet=new android.app.AlertDialog.Builder(this)
            .setView(scroll)
            .setNegativeButton("Close",null)
            .create();
        dialog[0]=sheet;
        sheet.setOnShowListener(v->{
            android.view.Window win=sheet.getWindow();
            if(win!=null){
                win.setBackgroundDrawable(bg(0xfffcfbff,22));
                win.setGravity(android.view.Gravity.BOTTOM);
                win.setLayout(-1,(int)(getResources().getDisplayMetrics().heightPixels*.64f));
            }
        });
        sheet.show();
    }

    // Advanced tools preserved for all previously built features.
    private void roomMenuAll() {
        List<String> items=new ArrayList<>();
        items.add("ℹ Room info"); items.add("👤 Host profile"); items.add("💬 Private chat"); items.add("🎯 Templates & Events"); items.add("🎟 Weekly Gift Card");
        items.add("▣ Billboard"); items.add("👥 Channel / Group"); items.add("🛡 Administrator"); items.add("⚙ Room settings");
        items.add(micOn?"🎤 Mic OFF":"🎤 Mic ON"); if(isModerator())items.add("🎙 Mic requests"); items.add("📹 Multi Video"); items.add(KingGiftSound993.enabled(this)?"🔊 Gift Audio ON":"🔇 Gift Audio OFF"); items.add("🌐 3D World"); items.add("🎵 Song request"); items.add("😊 Reaction");
        items.add("👥 Members"); items.add("🏆 Room ranking"); items.add("🎁 Gift history"); items.add("🕘 Recent activity");
        items.add("🎮 Games"); items.add("🤝 Team Up"); items.add("🕘 Room history"); if(cloudRoom)items.add(isFavoriteRoom940()?"★ Unfavorite room":"☆ Favorite room"); items.add("⚔ PK battle"); items.add("🧭 Full feature center");
        items.add("🛡 Party Master"); items.add("📊 Party Data"); items.add("🎤 KTV Queue"); items.add("📻 Radio Mic Queue");
        items.add("🧧 Red Packet"); items.add("🎁 Lucky Gift"); items.add("💫 Gift Wish"); items.add("🔎 Find room user"); items.add("📢 Notice center");
        items.add("💫 Interactive Emoji"); items.add("🎧 Audio PK"); if(isModerator())items.add("⚔ PK Challenge"); items.add("📜 PK History"); items.add("🎁 Gift Wall"); items.add("🔍 Online User Search");
        items.add("💞 Family Party"); items.add("💜 Fan Club"); items.add("📣 Friend Broadcast"); items.add("📚 Party Master Help");
        items.add("✨ KING Parity Center"); items.add("🚀 Ecosystem Pro"); items.add("🎤 KTV Stage"); items.add("⚔️ PK Arena"); items.add("🎲 Match Center"); items.add("🏰 Party Stage"); items.add("💎 VIP & Rank");
        items.add("🧪 Final production test"); items.add("🧪 Online diagnostics"); items.add("🔗 Share room"); items.add("🔔 Invite by Firebase UID"); items.add("⚑ Report host"); items.add("🚫 Block host");
        if(isModerator()){
            items.add("🛡 Host controls"); items.add("📥 Seat request center"); items.add("🚫 Banned users");
            items.add(roomLocked?"🔓 Unlock room":"🔒 Lock room"); items.add(muteAll?"🎤 Unmute all seats":"🔇 Mute all seats"); items.add("📢 Edit announcement");
        }
        if(isOwner()){
            items.add("🚪 Channel Entrance Settings"); items.add("👑 Co-host list"); items.add("✏ Rename room"); items.add("🗂 Change category"); items.add("🎨 Room theme"); items.add("🎙 Change mic seats");
            items.add(roomPrivate?"🔓 Make room public":"🔐 Make room private"); items.add(roomHasPassword?"🔑 Change room password":"🔑 Set room password");
            if(roomHasPassword)items.add("🗑 Remove room password"); items.add("⛔ Close room");
        }
        ScrollView scroll=new ScrollView(this);scroll.setVerticalScrollBarEnabled(false);LinearLayout panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(dp(14),dp(14),dp(14),dp(18));panel.setBackgroundColor(Color.WHITE);scroll.addView(panel);
        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);TextView av=tv("👑",28,0xff222222,true);av.setGravity(Gravity.CENTER);header.addView(av,new LinearLayout.LayoutParams(dp(52),dp(58)));LinearLayout hi=new LinearLayout(this);hi.setOrientation(LinearLayout.VERTICAL);TextView nm=tv(roomName,18,0xff202020,true);nm.setPadding(0,0,0,0);TextView meta=tv((coHost?"Administrator / Co-host":"KING Party")+"  •  "+(roomPrivate?"Private":"Public"),11,0xff777777,false);meta.setPadding(0,0,0,0);hi.addView(nm,new LinearLayout.LayoutParams(-1,dp(30)));hi.addView(meta,new LinearLayout.LayoutParams(-1,dp(24)));header.addView(hi,new LinearLayout.LayoutParams(0,dp(58),1));panel.addView(header,new LinearLayout.LayoutParams(-1,dp(64)));
        View div=new View(this);div.setBackgroundColor(0xffeeeeee);panel.addView(div,new LinearLayout.LayoutParams(-1,dp(1)));
        TextView tools940=tv("More tools",13,0xff777777,true);tools940.setPadding(dp(6),dp(8),0,dp(5));panel.addView(tools940,new LinearLayout.LayoutParams(-1,dp(34)));
        final AlertDialog[] holder=new AlertDialog[1];
        LinearLayout gridRow940=null;
        for(int i940=0;i940<items.size();i940++){
            if(i940%4==0){gridRow940=new LinearLayout(this);gridRow940.setGravity(Gravity.TOP);gridRow940.setWeightSum(4f);LinearLayout.LayoutParams rp940=new LinearLayout.LayoutParams(-1,dp(82));rp940.setMargins(0,dp(2),0,dp(2));panel.addView(gridRow940,rp940);}
            final String x=items.get(i940);TextView tile940=tv(x,11,0xff2c2731,true);tile940.setGravity(Gravity.CENTER);tile940.setMaxLines(3);tile940.setEllipsize(android.text.TextUtils.TruncateAt.END);tile940.setPadding(dp(4),dp(6),dp(4),dp(6));tile940.setBackground(bg(0xfff5f2f8,14));tile940.setOnClickListener(v->{if(holder[0]!=null)holder[0].dismiss();handleRoomMenuItem810(x);});LinearLayout.LayoutParams tp940=new LinearLayout.LayoutParams(0,dp(76),1);tp940.setMargins(dp(3),dp(3),dp(3),dp(3));gridRow940.addView(tile940,tp940);
        }
        holder[0]=new AlertDialog.Builder(this).setView(scroll).create();holder[0].setOnShowListener(v->{android.view.Window w=holder[0].getWindow();if(w!=null){w.setBackgroundDrawable(bg(Color.WHITE,24));w.setGravity(Gravity.BOTTOM);w.setLayout(-1,(int)(getResources().getDisplayMetrics().heightPixels*.72f));android.view.WindowManager.LayoutParams lp=w.getAttributes();lp.dimAmount=.28f;w.setAttributes(lp);w.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);}});holder[0].show();
    }

    private void handleRoomMenuItem810(String x){
        if(x.contains("Room info"))roomInfoDialog();
        else if(x.contains("Host profile"))hostProfileDialog();
        else if(x.contains("Private chat"))privateChatDialog();
        else if(x.contains("Weekly Gift Card"))weeklyGiftCardDialog();
        else if(x.contains("Templates & Events"))activityCenterPanel();
        else if(x.contains("Billboard"))roomBillboardDialog();
        else if(x.contains("Channel / Group"))channelPanel();
        else if(x.contains("Administrator"))administratorDialog();
        else if(x.contains("Channel Entrance Settings"))channelEntranceSettings9931();
        else if(x.contains("Room settings"))roomSettingsPanel();
        else if(x.contains("Mic ON")||x.contains("Mic OFF"))toggleMic();
        else if(x.contains("Mic requests"))openMicRequests940();
        else if(x.contains("Multi Video"))openVideoRoom();
        else if(x.contains("Gift Audio")){
            boolean nextAudio=!KingGiftSound993.enabled(this);
            KingGiftSound993.setEnabled(this,nextAudio);
            toast(nextAudio?"Gift sounds enabled":"Gift sounds muted on this device");
        }
        else if(x.contains("3D World"))openNativeWorld998();
        else if(x.contains("Song request"))karaokeDialog();
        else if(x.contains("Reaction"))reactionDialog();
        else if(x.contains("Members"))membersDialog();
        else if(x.contains("Room ranking"))roomRankingDialog();
        else if(x.contains("Gift history"))giftHistoryDialog();
        else if(x.contains("Recent activity"))recentActivityDialog();
        else if(x.contains("Host controls"))moderationPanel();
        else if(x.contains("Seat request center"))allSeatRequestsDialog();
        else if(x.contains("Banned users"))banListDialog();
        else if(x.contains("Co-host list"))coHostListDialog();
        else if(x.contains("Full feature center"))openDeepFlow810("party_play");
        else if(x.contains("Team Up"))teamUpPanel940();
        else if(x.contains("Room history"))roomHistoryPanel940();
        else if(x.contains("Favorite room")||x.contains("Unfavorite room"))toggleFavoriteRoom940();
        else if(x.contains("Games"))openRoomGames740();
        else if(x.contains("PK battle"))pkBattle();
        // Specific Help must come before generic Party Master to avoid shadowing.
        else if(x.contains("Party Master Help"))partyMasterHelp610();
        else if(x.contains("Party Master"))partyMasterPanel();
        else if(x.contains("Party Data"))partyDataPanel();
        else if(x.contains("KTV Queue"))ktvQueuePanel();
        else if(x.contains("Radio Mic"))radioMicPanel();
        else if(x.contains("Red Packet"))redPacketPanel940();
        else if(x.contains("Lucky Gift"))luckyGiftPanel();
        else if(x.contains("Gift Wish"))giftWishPanel();
        else if(x.contains("Find room user"))findRoomUserPanel();
        else if(x.contains("Notice center"))roomNoticePanel();
        else if(x.contains("Interactive Emoji"))showLiveEmojiPanelV530();
        else if(x.contains("Audio PK"))pkBattle();
        else if(x.contains("PK Challenge"))pkChallengeSearch940();
        else if(x.contains("PK History"))pkHistory940();
        else if(x.contains("Gift Wall"))giftWall620();
        else if(x.contains("Online User Search"))findRoomUserPanel();
        else if(x.contains("Family Party"))familyPartyPanel610();
        else if(x.contains("Fan Club"))fanClubPanel940();
        else if(x.contains("Friend Broadcast"))friendBroadcast610();
        else if(x.contains("Ecosystem Pro"))openEcosystem880("home");
        else if(x.contains("Parity Center"))openParity870("home");
        else if(x.contains("KTV Stage"))ktvQueuePanel();
        else if(x.contains("PK Arena"))audioPkPanel700();
        else if(x.contains("Match Center"))openRoomGames740();
        else if(x.contains("Party Stage"))roomSeatPanel();
        else if(x.contains("VIP & Rank"))roomRankingDialog();
        else if(x.contains("Final production test"))openProductionTest920();
        else if(x.contains("Online diagnostics"))onlineDiagnostics900();
        else if(x.contains("Share"))shareRoom();
        else if(x.contains("Invite"))inviteDialog();
        else if(x.contains("Report"))reportHost();
        else if(x.contains("Block"))blockHost();
        else if(x.contains("Rename room"))renameRoomDialog();
        else if(x.contains("Change category"))changeCategoryDialog();
        else if(x.contains("Room theme"))changeThemeDialog();
        else if(x.contains("mic seats"))changeSeatCount();
        else if(x.contains("Lock")||x.contains("Unlock"))setRoomFlag("locked",!roomLocked);
        else if(x.contains("Mute all")||x.contains("Unmute"))setRoomFlag("muteAll",!muteAll);
        else if(x.contains("announcement"))editAnnouncement();
        else if(x.contains("private")||x.contains("public"))setPrivateRoom(!roomPrivate);
        else if(x.contains("Set room password")||x.contains("Change room password"))setRoomPasswordDialog();
        else if(x.contains("Remove room password"))removeRoomPassword();
        else if(x.contains("Close room"))closeRoom();
    }

    private void openDeepFlow810(String route){Intent i=new Intent(this,KingDeepFlowActivity.class);i.putExtra("route",route);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);startActivity(i);}
    private void openParity870(String route){Intent i=new Intent(this,KingParityHubActivity.class);i.putExtra("route",route);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);startActivity(i);}
    private void openEcosystem880(String route){Intent i=new Intent(this,KingEcosystemProActivity.class);i.putExtra("route",route);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);startActivity(i);}
    private void openProductionTest920(){if(roomId==null||!cloudRoom){toast("Open a live Firebase Party Room first");return;}Intent i=new Intent(this,KingProductionTestActivity.class);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);startActivity(i);}

    private void partyMasterPanel(){
        String role=isOwner()?"Channel Host":(coHost?"Administrator / Co-host":"Member");
        StringBuilder summary=new StringBuilder();
        summary.append("Role: ").append(role).append("\n")
            .append("Members online: ").append(cloudRoom?liveMemberCount:Math.max(1,memberNames.size())).append("\n")
            .append("Occupied mic seats: ").append(seatNames.size()).append("/").append(maxSeats).append("\n")
            .append("Pending seat requests: ").append(pendingSeatRequests).append("\n")
            .append("Room privacy: ").append(roomPrivate?(roomHasPassword?"Private • password":"Private • invite only"):"Public").append("\n")
            .append("Seat lock: ").append(roomLocked?"ON":"OFF").append("  •  Mute all: ").append(muteAll?"ON":"OFF");
        List<String> actions=new ArrayList<>();
        actions.add("👥 Members"); actions.add("🪑 Room seat"); actions.add("📥 Seat request center"); actions.add("🏆 Room ranking");
        if(isModerator()){actions.add("🛡 Moderation controls"); actions.add("📢 Announcement"); actions.add("🚫 Banned users");}
        if(isOwner()){actions.add("👑 Administrator / Co-host"); actions.add("⚙ Room settings");}
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(16),dp(12),dp(16),dp(12));
        TextView info=tv(summary.toString(),14,0xff222222,false);root.addView(info);
        new AlertDialog.Builder(this).setTitle("🛡 Party Master").setView(root).setItems(actions.toArray(new String[0]),(d,w)->{
            String x=actions.get(w);if(x.contains("Members"))membersDialog();else if(x.contains("Room seat"))roomSeatPanel();else if(x.contains("Seat request"))allSeatRequestsDialog();else if(x.contains("ranking"))roomRankingDialog();else if(x.contains("Moderation"))moderationPanel();else if(x.contains("Announcement"))editAnnouncement();else if(x.contains("Banned"))banListDialog();else if(x.contains("Administrator"))administratorDialog();else roomSettingsPanel();
        }).setNegativeButton("Close",null).show();
    }

    private void partyDataPanel(){
        if(!cloudRoom||db==null||roomId==null){
            LinearLayout root954=new LinearLayout(this);root954.setOrientation(LinearLayout.VERTICAL);root954.setPadding(dp(12),dp(8),dp(12),dp(12));
            addPartyStat954(root954,"👥","Members",String.valueOf(Math.max(1,memberNames.size())));
            addPartyStat954(root954,"🎙","Occupied seats",seatNames.size()+" / "+maxSeats);
            addPartyStat954(root954,themeIcon954(),"Theme",roomTheme);
            addPartyStat954(root954,"🏷","Room type",roomCategory);
            new AlertDialog.Builder(this).setTitle("📊 Party Data").setView(root954).setPositiveButton("OK",null).show();return;
        }
        LinearLayout wait954=new LinearLayout(this);wait954.setGravity(Gravity.CENTER_VERTICAL);wait954.setPadding(dp(18),dp(10),dp(18),dp(10));android.widget.ProgressBar spin954=new android.widget.ProgressBar(this);wait954.addView(spin954,new LinearLayout.LayoutParams(dp(34),dp(34)));TextView loading954=tv("Loading room activity…",13,0xff5c5564,true);wait954.addView(loading954,new LinearLayout.LayoutParams(0,dp(52),1));final AlertDialog waiting954=new AlertDialog.Builder(this).setTitle("📊 Party Data").setView(wait954).setCancelable(false).create();waiting954.show();
        final long[] giftValue={0};final int[] gifts={0};final int[] events={0};
        db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(200).get().addOnSuccessListener(es->{
            events[0]=es.size();for(DocumentSnapshot e:es.getDocuments())if("gift".equals(e.getString("type"))){gifts[0]++;Long v=e.getLong("giftValue");if(v!=null)giftValue[0]+=Math.max(0,v);}
            db.collection("live_rooms").document(roomId).collection("messages").orderBy("createdAt",Query.Direction.DESCENDING).limit(200).get().addOnSuccessListener(ms->{
                if(waiting954.isShowing())waiting954.dismiss();
                LinearLayout root954=new LinearLayout(this);root954.setOrientation(LinearLayout.VERTICAL);root954.setPadding(dp(12),dp(8),dp(12),dp(12));root954.setBackgroundColor(0xfffaf8fc);
                TextView live954=tv("● LIVE ROOM  •  "+themeIcon954()+" "+roomTheme,12,0xff198b64,true);live954.setGravity(Gravity.CENTER);live954.setBackground(bg(0xffe5f8f0,14));root954.addView(live954,new LinearLayout.LayoutParams(-1,dp(40)));
                LinearLayout statsA954=new LinearLayout(this);statsA954.setGravity(Gravity.CENTER);statsA954.addView(partyStatCard954("👥",String.valueOf(liveMemberCount),"Online"),new LinearLayout.LayoutParams(0,dp(84),1));statsA954.addView(partyStatCard954("🎙",seatNames.size()+"/"+maxSeats,"Seats"),new LinearLayout.LayoutParams(0,dp(84),1));root954.addView(statsA954,new LinearLayout.LayoutParams(-1,dp(90)));
                LinearLayout statsB954=new LinearLayout(this);statsB954.setGravity(Gravity.CENTER);statsB954.addView(partyStatCard954("💬",String.valueOf(ms.size()),"Messages"),new LinearLayout.LayoutParams(0,dp(84),1));statsB954.addView(partyStatCard954("🎁",String.valueOf(gifts[0]),"Gifts"),new LinearLayout.LayoutParams(0,dp(84),1));statsB954.addView(partyStatCard954("💎",compactNumber(giftValue[0]),"Gift value"),new LinearLayout.LayoutParams(0,dp(84),1));root954.addView(statsB954,new LinearLayout.LayoutParams(-1,dp(90)));
                TextView details954=tv("Category  "+roomCategory+"     •     Recent events  "+events[0],11,0xff77707f,true);details954.setGravity(Gravity.CENTER);root954.addView(details954,new LinearLayout.LayoutParams(-1,dp(40)));
                new AlertDialog.Builder(this).setTitle("📊 Party Data").setView(root954).setPositiveButton("Ranking",(d,w)->roomRankingDialog()).setNeutralButton("Gift history",(d,w)->giftHistoryDialog()).setNegativeButton("Close",null).show();
            }).addOnFailureListener(e->{if(waiting954.isShowing())waiting954.dismiss();new AlertDialog.Builder(this).setTitle("Party data unavailable").setMessage(msg(e)).setPositiveButton("Retry",(d,w)->partyDataPanel()).setNegativeButton("Close",null).show();});
        }).addOnFailureListener(e->{if(waiting954.isShowing())waiting954.dismiss();new AlertDialog.Builder(this).setTitle("Party data unavailable").setMessage(msg(e)).setPositiveButton("Retry",(d,w)->partyDataPanel()).setNegativeButton("Close",null).show();});
    }

    private void ktvQueuePanel(){
        if(!cloudRoom||db==null||roomId==null){karaokeDialog();return;}
        LinearLayout loadingBox951=new LinearLayout(this);loadingBox951.setGravity(Gravity.CENTER_VERTICAL);loadingBox951.setPadding(dp(18),dp(12),dp(18),dp(12));
        android.widget.ProgressBar loadingSpin951=new android.widget.ProgressBar(this);loadingBox951.addView(loadingSpin951,new LinearLayout.LayoutParams(dp(34),dp(34)));
        TextView loadingText951=tv("Loading KTV stage and queue…",13,0xff55515f,true);loadingBox951.addView(loadingText951,new LinearLayout.LayoutParams(0,dp(52),1));
        final AlertDialog loading951=new AlertDialog.Builder(this).setTitle("🎤 KTV").setView(loadingBox951).setCancelable(false).create();loading951.show();

        DocumentReference ktv=db.collection("live_rooms").document(roomId).collection("room_settings").document("ktv");
        ktv.get().addOnSuccessListener(state->db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(120).get().addOnSuccessListener(snap->{
            if(loading951.isShowing())loading951.dismiss();
            boolean active=Boolean.TRUE.equals(state.getBoolean("active"));String nowSong=str(state,"song","");String singer=str(state,"singerName","");
            String currentRequest=str(state,"requestEventId","");Set<String> completedRequests951=new HashSet<>();Object completedRaw951=state.get("completedRequestIds");if(completedRaw951 instanceof List)for(Object x:(List<?>)completedRaw951)if(x!=null)completedRequests951.add(String.valueOf(x));
            List<DocumentSnapshot> waiting951=new ArrayList<>();
            for(DocumentSnapshot req:snap.getDocuments())if("song".equals(req.getString("type"))&&!req.getId().equals(currentRequest)&&!completedRequests951.contains(req.getId()))waiting951.add(req);

            LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(14),dp(10),dp(14),dp(12));root.setBackgroundColor(0xff17131f);
            TextView live951=tv(active?"● LIVE KTV  •  "+waiting951.size()+" waiting":"○ KTV STAGE IDLE  •  "+waiting951.size()+" waiting",11,active?0xff65e6a6:0xffb9afc8,true);live951.setGravity(Gravity.CENTER);live951.setBackground(bg(active?0xff183c31:0xff282332,14));root.addView(live951,new LinearLayout.LayoutParams(-1,dp(36)));

            LinearLayout stage=new LinearLayout(this);stage.setOrientation(LinearLayout.VERTICAL);stage.setGravity(Gravity.CENTER);stage.setPadding(dp(10),dp(8),dp(10),dp(8));stage.setBackground(bg(active?0xff452858:0xff24202c,18));
            TextView mic=tv(active?"🎤":"🎙",36,Color.WHITE,false);mic.setGravity(Gravity.CENTER);stage.addView(mic,new LinearLayout.LayoutParams(-1,dp(50)));
            TextView singerView=tv(active?singer:"KTV Stage",18,active?0xffffd75a:Color.WHITE,true);singerView.setGravity(Gravity.CENTER);stage.addView(singerView,new LinearLayout.LayoutParams(-1,dp(32)));
            TextView songView=tv(active?(nowSong.isEmpty()?"Live performance":nowSong):"Request a song to enter the queue",12,MUTED,true);songView.setGravity(Gravity.CENTER);stage.addView(songView,new LinearLayout.LayoutParams(-1,dp(26)));
            TextView stageState951=tv(active?"Mic ON • live inside Party room":"Waiting for the next singer",10,active?0xff7beab7:MUTED,true);stageState951.setGravity(Gravity.CENTER);stage.addView(stageState951,new LinearLayout.LayoutParams(-1,dp(22)));
            LinearLayout.LayoutParams stageLp951=new LinearLayout.LayoutParams(-1,dp(138));stageLp951.setMargins(0,dp(7),0,0);root.addView(stage,stageLp951);

            LinearLayout actions=new LinearLayout(this);actions.setGravity(Gravity.CENTER);actions.setPadding(0,dp(6),0,dp(6));
            TextView request=tv("＋ Request Song",11,Color.WHITE,true);request.setGravity(Gravity.CENTER);request.setBackground(bg(0xff553979,12));request.setOnClickListener(v->karaokeDialog());LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(0,dp(42),1);ap.setMargins(dp(3),0,dp(3),0);actions.addView(request,ap);
            if(isModerator()){TextView control=tv(active?"⏹ End Singer":"🎛 Music",11,Color.WHITE,true);control.setGravity(Gravity.CENTER);control.setBackground(bg(0xff4a395c,12));control.setOnClickListener(v->{if(active){String finishedRequest951=str(state,"requestEventId","");String finishedSong951=str(state,"song","");String finishedSinger951=str(state,"singerName","Guest");Map<String,Object>end=new HashMap<>();end.put("active",false);end.put("endedAt",FieldValue.serverTimestamp());end.put("endedBy",user==null?"":user.getUid());end.put("requestEventId","");if(!finishedRequest951.isEmpty())end.put("completedRequestIds",FieldValue.arrayUnion(finishedRequest951));ktv.set(end,SetOptions.merge()).addOnSuccessListener(x->{addEvent("ktv_end",finishedSinger951+" finished KTV • "+finishedSong951);ktvQueuePanel();}).addOnFailureListener(e->toast("Could not end KTV: "+msg(e)));}else musicPanel();});actions.addView(control,new LinearLayout.LayoutParams(ap));}
            TextView history951=tv("🕘 History",11,Color.WHITE,true);history951.setGravity(Gravity.CENTER);history951.setBackground(bg(0xff3e3550,12));history951.setOnClickListener(v->ktvHistory940());actions.addView(history951,new LinearLayout.LayoutParams(ap));
            root.addView(actions,new LinearLayout.LayoutParams(-1,dp(54)));

            TextView qTitle=tv("Song Request Queue  •  "+waiting951.size(),14,Color.WHITE,true);qTitle.setPadding(dp(2),dp(6),0,dp(5));root.addView(qTitle,new LinearLayout.LayoutParams(-1,dp(38)));
            ScrollView scroll=new ScrollView(this);LinearLayout queue=new LinearLayout(this);queue.setOrientation(LinearLayout.VERTICAL);scroll.addView(queue);
            int shown=0;
            for(DocumentSnapshot req:waiting951){
                String raw=str(req,"text","Song request");String requestedSong=raw;int mark=raw.indexOf(" requested 🎵 ");if(mark>=0)requestedSong=raw.substring(mark+" requested 🎵 ".length()).trim();
                final String song=requestedSong;final DocumentSnapshot requestDoc=req;
                LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(10),dp(6),dp(8),dp(6));row.setBackground(bg(0xff24202e,12));
                TextView icon=tv("🎵",22,Color.WHITE,false);icon.setGravity(Gravity.CENTER);row.addView(icon,new LinearLayout.LayoutParams(dp(42),dp(46)));
                LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);TextView songText=tv(song,13,Color.WHITE,true);songText.setSingleLine(true);TextView userText=tv(str(req,"actorName","Guest"),10,MUTED,false);info.addView(songText,new LinearLayout.LayoutParams(-1,dp(24)));info.addView(userText,new LinearLayout.LayoutParams(-1,dp(18)));row.addView(info,new LinearLayout.LayoutParams(0,dp(46),1));
                TextView next=tv(isModerator()?"START ›":"WAIT",10,isModerator()?0xffffd75a:MUTED,true);next.setGravity(Gravity.CENTER);row.addView(next,new LinearLayout.LayoutParams(dp(64),dp(46)));
                row.setOnClickListener(v->{if(!isModerator()){toast("Host/co-host selects the next singer");return;}new AlertDialog.Builder(this).setTitle("Start KTV singer?").setMessage(str(requestDoc,"actorName","Guest")+" • "+song).setPositiveButton("Start",(d,w)->startKtvRequestVisual940(requestDoc,song,ktv)).setNegativeButton("Cancel",null).show();});
                LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(60));rp.setMargins(0,dp(3),0,dp(3));queue.addView(row,rp);if(++shown>=20)break;
            }
            if(shown==0){TextView empty=tv(active?"Queue empty • current singer is on stage":"No waiting song requests\\nTap Request Song to be first in queue",12,MUTED,true);empty.setGravity(Gravity.CENTER);queue.addView(empty,new LinearLayout.LayoutParams(-1,dp(86)));}
            root.addView(scroll,new LinearLayout.LayoutParams(-1,Math.min(dp(350),Math.max(dp(92),shown*dp(66)+dp(24)))));
            AlertDialog dialog951=new AlertDialog.Builder(this).setTitle("🎤 KTV Stage").setView(root).setPositiveButton("Refresh",(d,w)->ktvQueuePanel()).setNegativeButton("Close",null).create();
            dialog951.setOnShowListener(v->{android.view.Window win=dialog951.getWindow();if(win!=null){int h=Math.min(dp(690),(int)(getResources().getDisplayMetrics().heightPixels*.82f));win.setLayout(-1,h);}});
            dialog951.show();
        }).addOnFailureListener(e->{if(loading951.isShowing())loading951.dismiss();new AlertDialog.Builder(this).setTitle("KTV unavailable").setMessage("Could not load the song queue.\\n\\n"+msg(e)).setPositiveButton("Retry",(d,w)->ktvQueuePanel()).setNegativeButton("Close",null).show();})).addOnFailureListener(e->{if(loading951.isShowing())loading951.dismiss();new AlertDialog.Builder(this).setTitle("KTV unavailable").setMessage("Could not load the KTV stage state.\\n\\n"+msg(e)).setPositiveButton("Retry",(d,w)->ktvQueuePanel()).setNegativeButton("Close",null).show();});
    }
    private void ktvHistory940(){
        if(!cloudRoom||db==null||roomId==null){toast("Open a live Party room first");return;}
        db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(180).get().addOnSuccessListener(snap->{
            List<String> rows=new ArrayList<>();
            for(DocumentSnapshot d:snap.getDocuments()){
                String type=d.getString("type");if(!"ktv_start".equals(type)&&!"ktv_end".equals(type))continue;
                String text=d.getString("text");if(text==null||text.trim().isEmpty())continue;
                com.google.firebase.Timestamp at=d.getTimestamp("createdAt");String when=at==null?"":new java.text.SimpleDateFormat("dd MMM • HH:mm",java.util.Locale.US).format(at.toDate());
                rows.add(("ktv_start".equals(type)?"🎤 START  ":"🏁 END  ")+(when.isEmpty()?"":when+"  •  ")+text);if(rows.size()>=40)break;
            }
            if(rows.isEmpty()){new AlertDialog.Builder(this).setTitle("🎤 KTV History").setMessage("No completed KTV activity in this room yet.").setPositiveButton("OK",null).show();return;}
            new AlertDialog.Builder(this).setTitle("🎤 KTV History").setItems(rows.toArray(new String[0]),null).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast("KTV history unavailable: "+msg(e)));
    }

    private void startKtvRequestVisual940(DocumentSnapshot req,String song,DocumentReference ktv){
        Map<String,Object>stage=new HashMap<>();stage.put("active",true);stage.put("song",song);stage.put("singerUid",str(req,"actorUid",""));stage.put("singerName",str(req,"actorName","Guest"));stage.put("requestEventId",req.getId());stage.put("startedAt",FieldValue.serverTimestamp());stage.put("startedBy",user==null?"":user.getUid());
        ktv.set(stage,SetOptions.merge()).addOnSuccessListener(v->{addEvent("ktv_start",safeName()+" put "+str(req,"actorName","Guest")+" on KTV stage • "+song);ktvQueuePanel();}).addOnFailureListener(e->toast("KTV stage failed: "+msg(e)));
    }


    private void radioMicPanel(){
        if(!cloudRoom||db==null||user==null){
            boolean queueOn=prefs.getBoolean("queue_"+roomId,false);
            String msg="Mic queue: "+(queueOn?"ON":"OFF")+"\n\n"+(mySeat>0?"You are on mic seat "+mySeat:"You are in the audience");
            new AlertDialog.Builder(this).setTitle("📻 Radio Mic").setMessage(msg)
                .setPositiveButton(isModerator()?(queueOn?"Disable queue":"Enable queue"):"View seats",(d,w)->{if(isModerator())toggleSeatQueue();else roomSeatPanel();})
                .setNeutralButton(isModerator()?"Seat requests":"Request mic",(d,w)->{if(isModerator())allSeatRequestsDialog();else{for(int i=1;i<=maxSeats;i++)if(seatNames.get(i)==null&&!lockedSeats.contains(i)){requestSeat(i);return;}toast("No empty mic seat");}})
                .setNegativeButton("Close",null).show();return;
        }
        DocumentReference room=db.collection("live_rooms").document(roomId);
        room.get().addOnSuccessListener(doc->{
            boolean queueOn=Boolean.TRUE.equals(doc.getBoolean("queueEnabled"));
            room.collection("seat_requests").limit(50).get().addOnSuccessListener(reqs->{
                int waiting=reqs==null?0:reqs.size();
                LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(14),dp(10),dp(14),dp(14));root.setBackgroundColor(0xff151722);
                LinearLayout hero=new LinearLayout(this);hero.setOrientation(LinearLayout.VERTICAL);hero.setGravity(Gravity.CENTER);hero.setBackground(bg(queueOn?0xff263c55:0xff342d3d,18));
                TextView icon=tv("📻",34,Color.WHITE,false);icon.setGravity(Gravity.CENTER);hero.addView(icon,new LinearLayout.LayoutParams(-1,dp(50)));
                TextView title=tv(queueOn?"Radio Mic Queue is OPEN":"Radio Mic Queue is CLOSED",17,queueOn?0xff8fe6ff:0xffffcfdb,true);title.setGravity(Gravity.CENTER);hero.addView(title,new LinearLayout.LayoutParams(-1,dp(32)));
                TextView status=tv("Waiting "+waiting+"   •   Seats "+seatNames.size()+"/"+maxSeats,12,MUTED,true);status.setGravity(Gravity.CENTER);hero.addView(status,new LinearLayout.LayoutParams(-1,dp(30)));root.addView(hero,new LinearLayout.LayoutParams(-1,dp(118)));

                LinearLayout meRow=new LinearLayout(this);meRow.setGravity(Gravity.CENTER_VERTICAL);meRow.setPadding(dp(12),dp(6),dp(10),dp(6));meRow.setBackground(bg(0xff252737,14));
                TextView myIcon=tv(mySeat>0?"🎙":"👤",24,Color.WHITE,false);myIcon.setGravity(Gravity.CENTER);meRow.addView(myIcon,new LinearLayout.LayoutParams(dp(48),dp(52)));
                LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);TextView myName=tv(safeName(),13,Color.WHITE,true);TextView myState=tv(mySeat>0?("On mic seat "+mySeat+(micOn?" • Mic ON":" • Mic OFF")):(queueOn?"Audience • request a mic seat":"Audience • queue closed"),11,MUTED,false);info.addView(myName,new LinearLayout.LayoutParams(-1,dp(26)));info.addView(myState,new LinearLayout.LayoutParams(-1,dp(22)));meRow.addView(info,new LinearLayout.LayoutParams(0,dp(52),1));root.addView(meRow,new LinearLayout.LayoutParams(-1,dp(64)));

                TextView hint=tv(isModerator()?"Host/co-host can open requests, assign seats and toggle the queue.":"Request an empty mic seat. Host/co-host approves before you go on stage.",11,MUTED,false);hint.setPadding(dp(4),dp(8),dp(4),dp(8));root.addView(hint,new LinearLayout.LayoutParams(-1,dp(58)));

                LinearLayout actions=new LinearLayout(this);actions.setGravity(Gravity.CENTER);
                if(isModerator()){
                    TextView requests=tv("📥 Requests "+waiting,11,Color.WHITE,true);requests.setGravity(Gravity.CENTER);requests.setBackground(bg(0xff4b3b6a,12));requests.setOnClickListener(v->allSeatRequestsDialog());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(46),1);p.setMargins(dp(3),0,dp(3),0);actions.addView(requests,p);
                    TextView toggle=tv(queueOn?"✋ Close Queue":"✋ Open Queue",11,Color.WHITE,true);toggle.setGravity(Gravity.CENTER);toggle.setBackground(bg(queueOn?0xff744353:0xff315f57,12));toggle.setOnClickListener(v->toggleSeatQueue());actions.addView(toggle,new LinearLayout.LayoutParams(p));
                    TextView seats=tv("🪑 Seats",11,Color.WHITE,true);seats.setGravity(Gravity.CENTER);seats.setBackground(bg(0xff3c4b65,12));seats.setOnClickListener(v->roomSeatPanel());actions.addView(seats,new LinearLayout.LayoutParams(p));
                }else{
                    TextView request=tv(queueOn?"✋ Request Mic":"🔒 Queue Closed",11,Color.WHITE,true);request.setGravity(Gravity.CENTER);request.setBackground(bg(queueOn?0xff315f57:0xff4a414a,12));request.setEnabled(queueOn);request.setOnClickListener(v->{for(int i=1;i<=maxSeats;i++)if(seatNames.get(i)==null&&!lockedSeats.contains(i)){requestSeat(i);return;}toast("No empty mic seat");});LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(46),1);p.setMargins(dp(3),0,dp(3),0);actions.addView(request,p);
                    TextView seats=tv("🪑 View Seats",11,Color.WHITE,true);seats.setGravity(Gravity.CENTER);seats.setBackground(bg(0xff3c4b65,12));seats.setOnClickListener(v->roomSeatPanel());actions.addView(seats,new LinearLayout.LayoutParams(p));
                }
                root.addView(actions,new LinearLayout.LayoutParams(-1,dp(52)));
                new AlertDialog.Builder(this).setTitle("📻 Radio Mic").setView(root).setPositiveButton("Refresh",(d,w)->radioMicPanel()).setNegativeButton("Close",null).show();
            }).addOnFailureListener(e->toast("Mic queue unavailable: "+msg(e)));
        }).addOnFailureListener(e->toast("Radio room state unavailable: "+msg(e)));
    }


    private void redPacketPanel940(){
        if(!cloudRoom||db==null||user==null||roomId==null){toast("Join a live Party room first");return;}
        final DocumentReference state=db.collection("live_rooms").document(roomId).collection("game_state").document("lucky_packet");
        state.get().addOnSuccessListener(doc->{
            boolean active=doc!=null&&doc.exists()&&Boolean.TRUE.equals(doc.getBoolean("active"));
            String packetId=active?str(doc,"packetId",""):"";
            Long expRaw=active?doc.getLong("expiresAtMs"):null;long expiresAt=expRaw==null?0L:expRaw;
            Long totalRaw=active?doc.getLong("totalCoins"):null;long total=totalRaw==null?0L:totalRaw;
            Long slotsRaw=active?doc.getLong("slots"):null;int slots=slotsRaw==null?0:slotsRaw.intValue();
            String sender=active?str(doc,"senderName","Host"):"";
            if(active&&(packetId.isEmpty()||expiresAt<=System.currentTimeMillis()))active=false;
            final boolean packetActive=active;final String pid=packetId;final long packetTotal=total;final int packetSlots=slots;final String packetSender=sender;
            if(packetActive){
                db.collection("live_rooms").document(roomId).collection("events").whereEqualTo("packetId",pid).limit(100).get().addOnSuccessListener(claims->{
                    int claimed=0;for(DocumentSnapshot d:claims.getDocuments())if("packet_claim".equals(d.getString("type")))claimed++;
                    int left=Math.max(0,packetSlots-claimed);
                    String msg="From: "+packetSender+"\nTEST coins: "+packetTotal+"\nClaims left: "+left+"/"+packetSlots+"\n\nNo real money or billing is used.";
                    AlertDialog.Builder b=new AlertDialog.Builder(this).setTitle("🧧 KING Red Packet").setMessage(msg).setNegativeButton("Close",null);
                    if(left>0)b.setPositiveButton("Open",(d,w)->claimRedPacket940(pid,packetTotal,packetSlots));
                    if(isModerator())b.setNeutralButton("New packet",(d,w)->createRedPacket940());
                    b.show();
                }).addOnFailureListener(e->toast("Packet status unavailable: "+msg(e)));
            }else{
                AlertDialog.Builder b=new AlertDialog.Builder(this).setTitle("🧧 KING Red Packet").setMessage("No active Red Packet in this room.\n\nPackets use TEST coins only.");
                if(isModerator())b.setPositiveButton("Create packet",(d,w)->createRedPacket940());
                else b.setPositiveButton("OK",null);
                b.setNegativeButton("Close",null).show();
            }
        }).addOnFailureListener(e->toast("Red Packet unavailable: "+msg(e)));
    }
    private void createRedPacket940(){
        if(!isModerator()||db==null||user==null){toast("Host/co-host only");return;}
        final EditText amount=new EditText(this);amount.setHint("Total TEST coins (10–1000)");amount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        final EditText slots=new EditText(this);slots.setHint("Number of claims (1–20)");slots.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(22),0,dp(22),0);box.addView(amount,new LinearLayout.LayoutParams(-1,dp(54)));box.addView(slots,new LinearLayout.LayoutParams(-1,dp(54)));
        new AlertDialog.Builder(this).setTitle("🧧 Create Red Packet").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Create",(d,w)->{
            int total,claimSlots;try{total=Integer.parseInt(amount.getText().toString().trim());claimSlots=Integer.parseInt(slots.getText().toString().trim());}catch(Exception e){toast("Enter valid numbers");return;}
            if(total<10||total>1000||claimSlots<1||claimSlots>20||total<claimSlots){toast("Use 10–1000 coins and 1–20 claims");return;}
            String pid=String.valueOf(System.currentTimeMillis())+"_"+user.getUid().substring(0,Math.min(6,user.getUid().length()));
            Map<String,Object>x=new HashMap<>();x.put("active",true);x.put("packetId",pid);x.put("totalCoins",total);x.put("slots",claimSlots);x.put("senderUid",user.getUid());x.put("senderName",safeName());x.put("createdAt",FieldValue.serverTimestamp());x.put("expiresAtMs",System.currentTimeMillis()+300000L);
            db.collection("live_rooms").document(roomId).collection("game_state").document("lucky_packet").set(x)
                .addOnSuccessListener(v->{addEvent("red_packet",safeName()+" dropped a KING Red Packet 🧧");toast("Red Packet created");redPacketPanel940();})
                .addOnFailureListener(e->toast("Could not create packet: "+msg(e)));
        }).show();
    }
    private int redPacketReward940(String packetId,long total,int slots){
        String key=packetId+":"+user.getUid();int h=key.hashCode()&0x7fffffff;
        int average=(int)Math.max(1L,total/Math.max(1,slots));
        int low=Math.max(1,average/2),high=Math.max(low,Math.min((int)Math.max(1,total),average+Math.max(1,average/2)));
        return low+(h%Math.max(1,high-low+1));
    }
    private void claimRedPacket940(String packetId,long total,int slots){
        if(user==null||db==null||packetId==null||packetId.isEmpty())return;
        final String claimId=("packet_"+packetId+"_"+user.getUid()).replace("/","_");
        final DocumentReference claim=db.collection("live_rooms").document(roomId).collection("events").document(claimId);
        claim.get().addOnSuccessListener(existing->{
            if(existing.exists()){toast("You already opened this Red Packet");return;}
            db.collection("live_rooms").document(roomId).collection("events").whereEqualTo("packetId",packetId).limit(100).get().addOnSuccessListener(all->{
                int count=0;for(DocumentSnapshot d:all.getDocuments())if("packet_claim".equals(d.getString("type")))count++;
                if(count>=slots){toast("Red Packet is finished");return;}
                int reward=redPacketReward940(packetId,total,slots);
                Map<String,Object>e=new HashMap<>();e.put("actorUid",user.getUid());e.put("actorName",safeName());e.put("type","packet_claim");e.put("text",safeName()+" opened Red Packet • +"+reward+" TEST coins 🧧");e.put("packetId",packetId);e.put("reward",reward);e.put("createdAt",FieldValue.serverTimestamp());
                claim.set(e).addOnSuccessListener(v->{localCoins+=reward;prefs.edit().putInt("coins",localCoins).apply();showReactionEffect("🧧");toast("Red Packet +"+reward+" TEST coins");})
                    .addOnFailureListener(err->toast("Packet already claimed or unavailable"));
            }).addOnFailureListener(e->toast("Could not open packet: "+msg(e)));
        }).addOnFailureListener(e->toast("Could not check packet: "+msg(e)));
    }

    private void luckyGiftPanel(){
        String[] rewards={"🌹 Gold Rose","⭐ Super Star","💗 Love Heart","🎆 Firework","🐠 Lucky Fish","🌟 Magic Star"};
        String picked=rewards[new java.util.Random().nextInt(rewards.length)];
        String text=safeName()+" opened Lucky Gift • "+picked;
        addEvent("lucky_gift",text);showReactionEffect(picked.substring(0,picked.indexOf(' ')));
        new AlertDialog.Builder(this).setTitle("🎁 Lucky Gift").setMessage("You opened: "+picked+"\n\nThis room event does not charge or credit real money/diamonds.").setPositiveButton("Send a gift",(d,w)->giftShopPanel()).setNeutralButton("Open again",(d,w)->luckyGiftPanel()).setNegativeButton("Close",null).show();
    }

    private void giftWishPanel(){
        String[] wishes={"🌹 Gold Rose","💗 Love Heart","👑 Flying Crown","🎆 Firework","🐠 Lucky Fish","🏰 Royal Castle"};
        new AlertDialog.Builder(this).setTitle("💫 Gift Wish").setMessage("Choose a gift you wish to receive in this room").setItems(wishes,(d,w)->{String wish=wishes[w];addEvent("gift_wish",safeName()+" wishes for "+wish);toast("Gift wish posted: "+wish);}).setNeutralButton("Gift Shop",(d,w)->giftShopPanel()).setNegativeButton("Close",null).show();
    }

    private void findRoomUserPanel(){
        if(!cloudRoom){toast("Open a live Party room first");return;}
        final EditText search=new EditText(this);search.setHint("Name or user ID");search.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("🔎 Online User Search").setView(search).setNegativeButton("Cancel",null)
            .setNeutralButton("Show all",(d,w)->showRoomUserResults940(""))
            .setPositiveButton("Search",(d,w)->showRoomUserResults940(search.getText().toString().trim())).show();
    }
    private void showRoomUserResults940(String query){
        String q=query==null?"":query.trim().toLowerCase(java.util.Locale.US);
        List<String> names=new ArrayList<>();List<String> uids=new ArrayList<>();
        if(ownerName!=null&&ownerUid!=null){
            String label="👑 "+ownerName;String hay=(ownerName+" "+ownerUid).toLowerCase(java.util.Locale.US);
            if(q.isEmpty()||hay.contains(q)){names.add(label);uids.add(ownerUid);}
        }
        for(String n:memberNames){
            String uid=memberUids.get(n);if(uid==null||uid.isEmpty()||uids.contains(uid))continue;
            String hay=((n==null?"":n)+" "+uid).toLowerCase(java.util.Locale.US);if(!q.isEmpty()&&!hay.contains(q))continue;
            names.add("👤 "+(n==null?"KING User":n));uids.add(uid);
        }
        if(names.isEmpty()){new AlertDialog.Builder(this).setTitle("Online User Search").setMessage(q.isEmpty()?"No room members loaded.":"No room user matched “"+query+"”.").setPositiveButton("Search again",(d,w)->findRoomUserPanel()).setNegativeButton("Close",null).show();return;}
        new AlertDialog.Builder(this).setTitle("Online Users • "+names.size()).setItems(names.toArray(new String[0]),(d,w)->{
            String uid=uids.get(w),name=names.get(w).replace("👑 ","").replace("👤 ","");
            if(uid.isEmpty()){toast("Profile unavailable");return;}
            if(isModerator()&&user!=null&&!uid.equals(user.getUid()))showProfileMore(uid,name);else memberProfileDialog(uid,name);
        }).setNeutralButton("Search again",(d,w)->findRoomUserPanel()).setNegativeButton("Close",null).show();
    }

    private void roomNoticePanel(){
        String msg=(announcement==null||announcement.trim().isEmpty())?"No room notice":announcement;
        AlertDialog.Builder b=new AlertDialog.Builder(this).setTitle("📢 Room Notice").setMessage(msg)
            .setPositiveButton(isModerator()?"Edit":"OK",(d,w)->{if(isModerator())editAnnouncement();})
            .setNeutralButton("History",(d,w)->roomNoticeHistory940())
            .setNegativeButton("Close",null);
        b.show();
    }
    private void roomNoticeHistory940(){
        if(!cloudRoom||db==null||roomId==null){new AlertDialog.Builder(this).setTitle("📢 Notice History").setMessage(announcement==null?"No room notice":announcement).setPositiveButton("OK",null).show();return;}
        db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(120).get().addOnSuccessListener(snap->{
            List<String> rows=new ArrayList<>();
            for(DocumentSnapshot d:snap.getDocuments()){
                if(!"notice".equals(d.getString("type")))continue;
                String text=d.getString("text");if(text==null||text.trim().isEmpty())continue;
                com.google.firebase.Timestamp at=d.getTimestamp("createdAt");String when=at==null?"":new java.text.SimpleDateFormat("dd MMM • HH:mm",java.util.Locale.US).format(at.toDate());
                rows.add((when.isEmpty()?"":when+"  •  ")+text);if(rows.size()>=30)break;
            }
            if(rows.isEmpty())rows.add("Current notice: "+((announcement==null||announcement.trim().isEmpty())?"No room notice":announcement));
            new AlertDialog.Builder(this).setTitle("📢 Notice History").setItems(rows.toArray(new String[0]),null).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast("Notice history unavailable: "+msg(e)));
    }

    private void familyPartyPanel610(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(14),dp(10),dp(14),dp(12));root.setBackgroundColor(0xfffaf7fc);
        TextView hero951=tv("👑  FAMILY PARTY",18,0xff302839,true);hero951.setGravity(Gravity.CENTER);hero951.setBackground(bg(0xffffe9b8,16));root.addView(hero951,new LinearLayout.LayoutParams(-1,dp(52)));
        TextView room951=tv(roomName+"\\n"+(cloudRoom?liveMemberCount:Math.max(1,memberNames.size()))+" online  •  "+seatNames.size()+"/"+maxSeats+" seats",13,0xff625b69,true);room951.setGravity(Gravity.CENTER);root.addView(room951,new LinearLayout.LayoutParams(-1,dp(62)));
        LinearLayout grid951=new LinearLayout(this);grid951.setOrientation(LinearLayout.VERTICAL);
        String[] labels951={"👑 Family Center","👥 Invite Family / Friends","🎉 Family Activity","📢 Family Announcement","🏠 Room Info","🎁 Family Gifts"};
        for(int i=0;i<labels951.length;i+=2){LinearLayout row=new LinearLayout(this);for(int j=i;j<Math.min(i+2,labels951.length);j++){final int k=j;TextView v=tv(labels951[j],12,0xff382f43,true);v.setGravity(Gravity.CENTER);v.setBackground(bg(Color.WHITE,13));v.setOnClickListener(x->{if(k==0)openCommunityHub700("Family");else if(k==1)shareRoom();else if(k==2){addEvent("family_activity",safeName()+" started a Family Party activity");toast("Family activity posted to room");}else if(k==3)roomNoticePanel();else if(k==4)new AlertDialog.Builder(this).setTitle("Family Party Room").setMessage("Room: "+roomName+"\\nOnline: "+(cloudRoom?liveMemberCount:Math.max(1,memberNames.size()))+"\\nSeats: "+seatNames.size()+"/"+maxSeats).setPositiveButton("Family Center",(d,w)->openCommunityHub700("Family")).setNegativeButton("Close",null).show();else giftShopPanel();});LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(58),1);cp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(v,cp);}grid951.addView(row,new LinearLayout.LayoutParams(-1,dp(64)));}
        root.addView(grid951,new LinearLayout.LayoutParams(-1,-2));
        TextView note951=tv("Family activities, gifts and chat stay inside KING Plus. Open Family Center for members, Lucky Bag and Family Chat.",11,0xff81798b,false);note951.setPadding(dp(8),dp(8),dp(8),dp(8));root.addView(note951,new LinearLayout.LayoutParams(-1,dp(58)));
        new AlertDialog.Builder(this).setTitle("💞 Family Party").setView(root).setNegativeButton("Close",null).show();
    }
    private int fanClubRequestGeneration9998;
    private void fanClubPanel940(){
        if(!cloudRoom||db==null||user==null||ownerUid==null||ownerUid.isEmpty()){toast("Open a live Party room first");return;}
        final String hostUid=ownerUid, account=user.getUid(), openRoom=roomId;
        final int ticket=++fanClubRequestGeneration9998;
        final String relation=KingFollowPolicy9997.key(account,hostUid);
        if(relation==null){toast("Host cannot join their own Fan Club");return;}
        // Exact Follow doc, not a 100-person preview, determines Fan Club membership.
        db.collection("follows").document(relation)
            .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(member->{
                if(partyUiDead()||ticket!=fanClubRequestGeneration9998||user==null
                    ||!account.equals(user.getUid())||!hostUid.equals(ownerUid)
                    ||!java.util.Objects.equals(openRoom,roomId))return;
                final boolean joined=member.exists()
                    && account.equals(member.getString("followerUid"))
                    && hostUid.equals(member.getString("targetUid"));
                com.google.firebase.firestore.Query fans=db.collection("follows").whereEqualTo("targetUid",hostUid);
                fans.count().get(com.google.firebase.firestore.AggregateSource.SERVER)
                    .addOnSuccessListener(count->{
                        if(partyUiDead()||ticket!=fanClubRequestGeneration9998||!hostUid.equals(ownerUid)
                            ||!java.util.Objects.equals(openRoom,roomId))return;
                        final long total=count.getCount();
                        fans.limit(100).get(com.google.firebase.firestore.Source.SERVER)
                            .addOnSuccessListener(preview->{
                                if(partyUiDead()||ticket!=fanClubRequestGeneration9998||!hostUid.equals(ownerUid)
                                    ||!java.util.Objects.equals(openRoom,roomId))return;
                                List<String> names=new ArrayList<>();
                                for(DocumentSnapshot fan:preview.getDocuments()){
                                    String uid=fan.getString("followerUid"),name=fan.getString("followerName");
                                    if(name==null||name.trim().isEmpty())name=uid==null?"KING Fan":"KING "+uid.substring(0,Math.min(6,uid.length()));
                                    names.add(name);
                                }
                                String[] actions={(joined?"💔 Leave Fan Club":"💜 Join Fan Club"),
                                    "👥 View fans ("+total+")", "🎁 Open Gift Center"};
                                new AlertDialog.Builder(this).setTitle("💜 "+(ownerName==null?"KING Host":ownerName)+" Fan Club")
                                    .setMessage("Fans: "+total+"\n"+(joined?"You follow this host.":"Follow the host to join.")
                                        +(total>100?"\nShowing first 100 members in the preview.":""))
                                    .setItems(actions,(dialog,which)->{
                                        if(which==0)toggleFanClub940(joined);
                                        else if(which==1){
                                            if(names.isEmpty())new AlertDialog.Builder(this).setTitle("Fan members").setMessage("No fans yet.").setPositiveButton("OK",null).show();
                                            else new AlertDialog.Builder(this).setTitle("Fan members • "+names.size()+" of "+total)
                                                .setItems(names.toArray(new String[0]),null).setNegativeButton("Close",null).show();
                                        }else giftDialogFor(hostUid,ownerName==null?"Host":ownerName);
                                    }).setNegativeButton("Close",null).show();
                            }).addOnFailureListener(e->{if(!partyUiDead())toast("Fan preview unavailable: "+msg(e));});
                    }).addOnFailureListener(e->{if(!partyUiDead())toast("Fan count unavailable: "+msg(e));});
            }).addOnFailureListener(e->{if(!partyUiDead())toast("Fan membership unavailable: "+msg(e));});
    }
    private void toggleFanClub940(boolean joined){
        if(user==null||db==null||ownerUid==null){toast("Sign in to join Fan Club");return;}
        KingFollowLive9997.toggle(db,user,ownerUid,safeName(),(following,error)->{
            if(isFinishing()||isDestroyed())return;
            if(error!=null){toast("Fan Club could not update: "+msg(error));return;}
            toast(Boolean.TRUE.equals(following)?"Joined Fan Club 💜":"Left Fan Club");
            if(cloudRoom&&roomId!=null)fanClubPanel940();
        });
    }

    private void friendBroadcast610(){
        final String text="Join my KING Plus Party: "+roomName+(roomId==null?"":" • Room "+roomId);
        String[] actions={"📣 Post to KING Square","🔗 Share outside KING Plus","📋 Copy Room ID"};
        new AlertDialog.Builder(this).setTitle("📣 Friend Broadcast").setItems(actions,(d,w)->{
            if(w==0){
                if(!cloudRoom||db==null||user==null){toast("Join a live signed-in Party room first");return;}
                Map<String,Object>m=new HashMap<>();m.put("uid",user.getUid());m.put("name",safeName());m.put("text",text);m.put("topic","Friends");m.put("type","room_broadcast");m.put("roomId",roomId);m.put("roomName",roomName);m.put("createdAt",FieldValue.serverTimestamp());
                db.collection("moments").add(m).addOnSuccessListener(v->{addEvent("friend_broadcast",safeName()+" broadcast this Party to KING Square 📣");toast("Party broadcast posted");}).addOnFailureListener(e->toast("Broadcast failed: "+msg(e)));
            }else if(w==1){
                Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,text);startActivity(Intent.createChooser(i,"Broadcast Party to friends"));
            }else{
                try{android.content.ClipboardManager cm=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);cm.setPrimaryClip(android.content.ClipData.newPlainText("KING Plus room",roomId==null?"":roomId));toast("Room ID copied");}catch(Exception e){toast("Could not copy Room ID");}
            }
        }).setNegativeButton("Close",null).show();
    }
    private void partyMasterHelp610(){
        new AlertDialog.Builder(this).setTitle("📚 Party Master Help").setMessage("Host / Co-host controls\n\n• Lock or unlock seats\n• Approve seat requests\n• Mute or unmute members\n• Kick / ban users\n• Manage room privacy and password\n• Edit room notice\n• View Party Data, ranking and gift history\n• Use KTV, Radio Mic, PK, Games and Interactive Emoji\n\nAll moderation actions use the current KING Plus room/Firebase flow.").setPositiveButton("Open Party Master",(d,w)->partyMasterPanel()).setNegativeButton("Close",null).show();
    }

    private void inviteDialog(){
        if(!cloudRoom||user==null){shareRoom();return;}
        String[] choices={"👥 Invite from Following","🔎 Enter Firebase UID","🔗 Share room link"};
        new AlertDialog.Builder(this).setTitle("Invite to Party").setItems(choices,(d,w)->{
            if(w==0)inviteFollowing940();
            else if(w==1)inviteByUid940();
            else shareRoom();
        }).setNegativeButton("Cancel",null).show();
    }
    private void inviteByUid940(){
        final EditText e=new EditText(this);e.setHint("Friend Firebase UID");e.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("Invite by UID").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Invite",(d,w)->{
            String uid=e.getText().toString().trim();if(uid.isEmpty())return;sendRoomInvite940(uid,"Friend");
        }).show();
    }
    private void inviteFollowing940(){
        if(db==null||user==null){toast("Firebase sign-in required");return;}
        db.collection("follows").whereEqualTo("followerUid",user.getUid()).limit(80).get().addOnSuccessListener(follows->{
            Set<String> ids=new HashSet<>();for(DocumentSnapshot d:follows.getDocuments()){String uid=d.getString("targetUid");if(uid!=null&&!uid.isEmpty()&&!uid.equals(user.getUid())&&!memberUids.containsValue(uid))ids.add(uid);}
            if(ids.isEmpty()){new AlertDialog.Builder(this).setTitle("Invite from Following").setMessage("No followed users are available to invite right now.").setPositiveButton("Invite by UID",(d,w)->inviteByUid940()).setNegativeButton("Close",null).show();return;}
            db.collection("public_profiles").limit(100).get().addOnSuccessListener(profiles->{
                List<String> labels=new ArrayList<>();List<String> uids=new ArrayList<>();
                for(DocumentSnapshot p:profiles.getDocuments()){String uid=p.getString("uid");if(uid==null||uid.isEmpty())uid=p.getId();if(!ids.contains(uid))continue;labels.add("👤 "+str(p,"displayName","KING User"));uids.add(uid);}
                for(String uid:ids)if(!uids.contains(uid)){labels.add("👤 KING User • Firebase member");uids.add(uid);}
                if(labels.isEmpty()){toast("No followed users available");return;}
                new AlertDialog.Builder(this).setTitle("👥 Invite Following • "+labels.size()).setItems(labels.toArray(new String[0]),(d,w)->sendRoomInvite940(uids.get(w),labels.get(w).replace("👤 ",""))).setNegativeButton("Close",null).show();
            }).addOnFailureListener(e->toast("Profiles unavailable: "+msg(e)));
        }).addOnFailureListener(e->toast("Following list unavailable: "+msg(e)));
    }
    private void sendRoomInvite940(String uid,String name){
        if(uid==null||uid.trim().isEmpty())return;final String target=uid.trim();
        Runnable send=()->CloudBackend.sendRoomInvite(target,roomId,roomName,(ok,m)->runOnUiThread(()->toast(ok?"Invite sent to "+name:m)));
        if(roomPrivate&&isModerator()&&db!=null){
            Map<String,Object> grant=new HashMap<>();grant.put("uid",target);grant.put("grantedBy",user.getUid());grant.put("entryEpoch",roomEntryEpoch9936);grant.put("createdAt",FieldValue.serverTimestamp());
            db.collection("live_rooms").document(roomId).collection("access").document(target).set(grant)
                .addOnSuccessListener(v->send.run()).addOnFailureListener(x->toast("Private invite failed: "+msg(x)));
        }else send.run();
    }
    private void reportHost(){String target=ownerUid==null?ownerName:ownerUid;CloudSync.submitReport(this,target,"Live party report",(ok,m)->runOnUiThread(()->toast(m)));}
    /**
     * Cross-device Block Host uses the SAME document ID/schema as
     * KingPublicProfileActivity.blockId(). Requires Firebase auth and server.
     * A successful write syncs the block list; it does NOT by itself enforce
     * blocking in every chat/room screen. Never report offline fake success.
     */
    private void blockHost(){
        if(!cloudRoom||db==null||user==null||ownerUid==null||ownerUid.trim().isEmpty()){
            toast("Sign in to a real Party Room to block its host");
            return;
        }
        final String hostUid=ownerUid.trim();
        if(hostUid.equals(user.getUid())){toast("You cannot block yourself");return;}
        new AlertDialog.Builder(this)
            .setTitle("🚫 Block this host?")
            .setMessage("Save this host to your KING Plus account block list? "+
                "This will sync with Profile Block/Unblock. "+
                "Existing Party voice/chat filtering is not automatic.")
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Block",(dialog,which)->{
                final String accountUid=user.getUid();
                final DocumentReference blockedRef=db.collection("blocks")
                    .document(accountUid+"_"+hostUid);
                blockedRef.get(com.google.firebase.firestore.Source.SERVER)
                    .addOnSuccessListener(snapshot->{
                        if(isFinishing()||isDestroyed())return;
                        if(snapshot.exists()){
                            toast("Already on your account's blocked list");
                            return;
                        }
                        Map<String,Object> values=new HashMap<>();
                        values.put("ownerUid",accountUid);
                        values.put("targetUid",hostUid);
                        values.put("createdAt",FieldValue.serverTimestamp());
                        blockedRef.set(values).addOnSuccessListener(ignored->{
                            if(isFinishing()||isDestroyed())return;
                            Set<String> local=new HashSet<>(
                                prefs.getStringSet("blocked",new HashSet<>()));
                            local.add(hostUid);
                            prefs.edit().putStringSet("blocked",local).apply();
                            toast("Host added to account Block List");
                        }).addOnFailureListener(error->{
                            if(!isFinishing()&&!isDestroyed())
                                toast("Could not save host block. Check connection and retry.");
                        });
                    }).addOnFailureListener(error->{
                        if(!isFinishing()&&!isDestroyed())
                            toast("Could not verify block list from server. Retry.");
                    });
            }).show();
    }
    private void editAnnouncement(){final EditText e=new EditText(this);e.setText(announcement);new AlertDialog.Builder(this).setTitle("Room announcement").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{String s=e.getText().toString().trim();if(s.isEmpty())return;if(s.length()>220)s=s.substring(0,220);final String notice940=s;if(cloudRoom){setRoomValue("announcement",notice940);addEvent("notice",safeName()+" • "+notice940);}else{announcement=notice940;prefs.edit().putString("notice_"+roomId,notice940).apply();if(announcementLabel!=null)announcementLabel.setText("📢  "+notice940);}}).show();}
    private void setRoomFlag(String key,boolean value){
        if(!isModerator()){toast("Host/co-host only");return;}
        if(!cloudRoom){
            if("locked".equals(key))roomLocked=value;else muteAll=value;
            prefs.edit().putBoolean(("locked".equals(key)?"locked_":"mute_")+roomId,value).apply();
            toast("Room updated");return;
        }
        if(db==null||user==null||roomId==null){toast("Live Party connection required");return;}
        final String room10020=roomId,actor10020=user.getUid();
        final int visit10020=presenceGeneration967;
        if(!isActivePresence967(room10020,actor10020,visit10020)){toast("Room changed");return;}
        final DocumentReference roomRef10020=db.collection("live_rooms").document(room10020);
        roomRef10020.update(key,value,"updatedAt",FieldValue.serverTimestamp())
            .addOnSuccessListener(done->{
                if(!isActivePresence967(room10020,actor10020,visit10020)||!isModerator())return;
                toast(value?("muteAll".equals(key)?"Room microphones muted":"Room updated"):"Room updated");
                if("muteAll".equals(key)&&value)muteExistingRoomSeats10020(room10020,actor10020,visit10020);
            }).addOnFailureListener(e->{
                if(isActivePresence967(room10020,actor10020,visit10020))
                    toast("Could not update room: "+msg(e));
            });
    }
    // Each live occupant is checked at write time. A seat reassigned on a
    // second phone cannot be muted using a stale server snapshot.
    private void muteExistingRoomSeats10020(String room10020,String actor10020,int visit10020){
        if(!isActivePresence967(room10020,actor10020,visit10020)||!isModerator()||db==null)return;
        final DocumentReference roomRef10020=db.collection("live_rooms").document(room10020);
        roomRef10020.collection("seats").get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(seats10020->{
                if(!isActivePresence967(room10020,actor10020,visit10020)||!isModerator())return;
                for(DocumentSnapshot previous10020:seats10020.getDocuments()){
                    final String uid10020=previous10020.getString("uid");
                    if(uid10020==null||uid10020.isEmpty()
                        ||!Boolean.TRUE.equals(previous10020.getBoolean("micOn")))continue;
                    db.runTransaction(tx10020->{
                        DocumentSnapshot liveRoom10020=tx10020.get(roomRef10020);
                        DocumentSnapshot liveSeat10020=tx10020.get(previous10020.getReference());
                        if(!liveRoom10020.exists()||Boolean.TRUE.equals(liveRoom10020.getBoolean("closed"))
                            ||!Boolean.TRUE.equals(liveRoom10020.getBoolean("muteAll")))
                            return false;
                        if(!liveSeat10020.exists()||!uid10020.equals(liveSeat10020.getString("uid"))
                            ||!Boolean.TRUE.equals(liveSeat10020.getBoolean("micOn")))return false;
                        tx10020.update(previous10020.getReference(),"micOn",false);
                        return true;
                    }).addOnFailureListener(err->{
                        if(isActivePresence967(room10020,actor10020,visit10020))
                            toast("Some microphones could not be muted. Reopen the room controls.");
                    });
                }
            }).addOnFailureListener(err->{
                if(isActivePresence967(room10020,actor10020,visit10020))
                    toast("Could not read live mic seats. Please retry.");
            });
    }
    private void setRoomValue(String key,Object value){if(db==null)return;db.collection("live_rooms").document(roomId).update(key,value,"updatedAt",FieldValue.serverTimestamp()).addOnFailureListener(e->toast(msg(e)));}
    private void closeRoom(){
        if(!isOwner()){toast("Host only");return;}
        if(!cloudRoom){leaveRoom();return;}
        if(db==null||user==null||roomId==null){toast("Room connection required");return;}
        final String closedRoom10020=roomId,owner10020=user.getUid();
        final int visit10020=presenceGeneration967;
        DocumentReference ref10020=db.collection("live_rooms").document(closedRoom10020);
        db.runTransaction(tx10020->{
            DocumentSnapshot live10020=tx10020.get(ref10020);
            if(!live10020.exists()||!owner10020.equals(live10020.getString("ownerUid")))
                throw new IllegalStateException("You are no longer this room's host");
            if(Boolean.TRUE.equals(live10020.getBoolean("closed")))return false;
            tx10020.update(ref10020,"closed",true,"updatedAt",FieldValue.serverTimestamp());
            return true;
        }).addOnSuccessListener(done->{
            if(isActivePresence967(closedRoom10020,owner10020,visit10020)){
                toast("Party room closed");leaveRoom();
            }
        }).addOnFailureListener(err->{
            if(isActivePresence967(closedRoom10020,owner10020,visit10020))
                toast("Room was NOT closed: "+msg(err));
        });
    }

    private boolean categoryMatches(String selected,String category){
        if(selected==null||"Hot".equalsIgnoreCase(selected))return true;
        if("Music".equalsIgnoreCase(selected))return "Music".equalsIgnoreCase(category)||"Sing".equalsIgnoreCase(category)||"KTV".equalsIgnoreCase(category)||"Radio".equalsIgnoreCase(category);
        if("Game".equalsIgnoreCase(selected))return "Game".equalsIgnoreCase(category)||"PK".equalsIgnoreCase(category)||"Pick Me".equalsIgnoreCase(category);
        if("Video".equalsIgnoreCase(selected))return "Multi Video".equalsIgnoreCase(category)||"Video".equalsIgnoreCase(category);
        if("Event".equalsIgnoreCase(selected))return "Event".equalsIgnoreCase(category)||"Birthday".equalsIgnoreCase(category)||"Wedding".equalsIgnoreCase(category)||"Family".equalsIgnoreCase(category);
        return selected.equalsIgnoreCase(category);
    }
    private int themeAccentFor954(String theme){if("KTV".equals(theme))return 0xff5ca7ff;if("PK".equals(theme))return 0xffff6c83;if("Love".equals(theme))return 0xffff70b2;if("Game".equals(theme))return 0xff55dda4;if("Royal".equals(theme))return 0xffffd76a;if("Neon".equals(theme))return 0xff35e8ff;if("Galaxy".equals(theme))return 0xffa887ff;if("Festival".equals(theme))return 0xffff946e;if("Ice".equals(theme))return 0xff7de8ff;return 0xff35e6b3;}
    private int themeAccent954(){return themeAccentFor954(roomTheme);}
    private String themeIcon954(){if("KTV".equals(roomTheme))return "🎤";if("PK".equals(roomTheme))return "⚔️";if("Love".equals(roomTheme))return "💗";if("Game".equals(roomTheme))return "🎮";if("Royal".equals(roomTheme))return "👑";if("Neon".equals(roomTheme))return "💡";if("Galaxy".equals(roomTheme))return "🌌";if("Festival".equals(roomTheme))return "🎉";if("Ice".equals(roomTheme))return "❄️";return "✨";}
    private TextView partyStatCard954(String icon,String value,String label){TextView t=tv(icon+"\\n"+value+"\\n"+label,11,0xff302b35,true);t.setGravity(Gravity.CENTER);t.setBackground(bg(Color.WHITE,14));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(80),1);lp.setMargins(dp(3),dp(3),dp(3),dp(3));return t;}
    private void addPartyStat954(LinearLayout root,String icon,String label,String value){LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(12),dp(4),dp(12),dp(4));row.setBackground(bg(0xfff6f3f8,13));TextView a=tv(icon+"  "+label,13,0xff39323f,true);row.addView(a,new LinearLayout.LayoutParams(0,dp(46),1));TextView b=tv(value,13,0xff6d527f,true);b.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);row.addView(b,new LinearLayout.LayoutParams(dp(150),dp(46)));LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(52));rp.setMargins(0,dp(3),0,dp(3));root.addView(row,rp);}
    
    private int themeBackground(){
        if("KTV".equals(roomTheme))return 0xff101d46;
        if("PK".equals(roomTheme))return 0xff321b50;
        if("Love".equals(roomTheme))return 0xff3d1830;
        if("Game".equals(roomTheme))return 0xff16382f;
        if("Royal".equals(roomTheme))return 0xff28194d;
        if("Neon".equals(roomTheme))return 0xff081d29;
        if("Galaxy".equals(roomTheme))return 0xff100d2f;
        if("Festival".equals(roomTheme))return 0xff401529;
        if("Ice".equals(roomTheme))return 0xff17485d;
        return BG2;
    }
    private void membersDialog(){
        List<String> labels=new ArrayList<>(); List<String> ids=new ArrayList<>();
        if(cloudRoom){
            if(memberNames.isEmpty()){labels.add("No members loaded");ids.add("");}
            else for(String n:memberNames){labels.add(n);String uid=memberUids.get(n);ids.add(uid==null?"":uid);}
        } else {
            labels.add(ownerName+" • Host"); ids.add("");
            for(int i=1;i<=maxSeats;i++){String n=seatNames.get(i);if(n!=null&&!labels.contains(n)){labels.add(n);ids.add("");}}
        }
        new AlertDialog.Builder(this).setTitle("👥 Room members").setItems(labels.toArray(new String[0]),(d,w)->{
            String uid=ids.get(w); if(!cloudRoom||uid.isEmpty()){toast(labels.get(w));return;}
            memberProfileDialog(uid,labels.get(w));
        }).setNegativeButton("Close",null).show();
    }
    private void openSeatProfile(int no){
        String name=seatNames.get(no);if(name==null)return;String uid=seatUids.get(no);
        if(uid!=null&&!uid.isEmpty()){showRichProfile(uid,name,uid.equals(ownerUid));return;}
        seatUserMenu(no);
    }

    private void seatUserMenu(int no){
        String name=seatNames.get(no); if(name==null)return; String uid=seatUids.get(no);
        List<String> items=new ArrayList<>(); items.add("💬 Private chat"); items.add("🎁 Send gift"); items.add("＋ Follow"); items.add("⚑ Report");
        if(isModerator()){
            items.add(Boolean.FALSE.equals(seatMics.get(no))?"🎤 Unmute seat":"🔇 Mute seat");
            items.add("↔ Move to another seat"); items.add("⬇ Remove from seat"); items.add("🚪 Kick from room"); items.add("🚫 Ban from room");
        }
        if(isOwner()&&uid!=null&&!uid.isEmpty()&&!uid.equals(ownerUid))items.add("👑 Co-host role");
        String[] a=items.toArray(new String[0]);
        new AlertDialog.Builder(this).setTitle(name+" • Seat "+no).setItems(a,(d,w)->{
            String x=a[w];
            if(x.contains("Private chat"))openPrivateChat(uid,name);
            else if(x.contains("Gift"))giftDialogFor(uid==null?"":uid,name);
            else if(x.contains("Follow"))followUser(uid,name);
            else if(x.contains("Report"))reportUser(uid,name);
            else if(x.contains("Mute seat")||x.contains("Unmute seat"))hostToggleSeat(no);
            else if(x.contains("Move to another seat"))moveUserToSeatDialog(uid,name,no);
            else if(x.contains("Remove"))hostRemoveSeat(no);
            else if(x.contains("Kick"))kickUser(uid,name);
            else if(x.contains("Ban"))banUser(uid,name);
            else if(x.contains("Co-host"))coHostDialog(uid,name);
        }).setNegativeButton("Close",null).show();
    }
    private void memberUserMenu(String uid,String name){ memberProfileDialog(uid,name); }
    private void memberProfileDialog(String uid,String name){
        if(uid==null||uid.trim().isEmpty())return;
        showRichProfile(uid,name,false);
    }
    private void showRichProfile(String uid,String name,boolean host){
        if(uid!=null&&!uid.trim().isEmpty()){
            logProfileVisit750(uid);
            Intent page=new Intent(this,KingPublicProfileActivity.class);
            page.putExtra("uid",uid);
            page.putExtra("name",name==null||name.trim().isEmpty()?"KING User":name.trim());
            page.putExtra("roomId",roomId);
            page.putExtra("roomRole",host?"HOST":"MEMBER");
            startActivity(page);
            return;
        }
        if(uid==null||uid.isEmpty())return;
        final String fallbackName=(name==null||name.trim().isEmpty())?"KING User":name.trim();
        logProfileVisit750(uid);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setVerticalScrollBarEnabled(false);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setGravity(Gravity.CENTER_HORIZONTAL);root.setPadding(dp(18),dp(10),dp(18),dp(28));root.setBackground(bg(Color.WHITE,24));scroll.addView(root);

        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);
        TextView mention=tv("⚠   @ Mention",15,0xffb3adb8,true);mention.setGravity(Gravity.CENTER_VERTICAL);top.addView(mention,new LinearLayout.LayoutParams(0,dp(44),1));
        TextView more=tv("⋮",26,0xff77717d,true);more.setGravity(Gravity.CENTER);top.addView(more,new LinearLayout.LayoutParams(dp(44),dp(44)));root.addView(top,new LinearLayout.LayoutParams(-1,dp(44)));

        FrameLayout avatarFrame=new FrameLayout(this);avatarFrame.setBackground(bg(0xffe4f3ff,70));
        ImageView avatarImage=new ImageView(this);avatarImage.setScaleType(ImageView.ScaleType.CENTER_CROP);avatarImage.setClipToOutline(true);avatarImage.setBackground(bg(0xffe4f3ff,70));avatarFrame.addView(avatarImage,new FrameLayout.LayoutParams(-1,-1));
        TextView avatarInitial=tv(fallbackName.substring(0,1).toUpperCase(),42,Color.WHITE,true);avatarInitial.setGravity(Gravity.CENTER);avatarInitial.setBackground(bg(0xff4b86d8,70));avatarFrame.addView(avatarInitial,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout.LayoutParams afp=new LinearLayout.LayoutParams(dp(96),dp(96));afp.setMargins(0,0,0,dp(5));root.addView(avatarFrame,afp);

        TextView n=tv((host?"👑 ":"")+fallbackName,20,0xff17131c,true);n.setGravity(Gravity.CENTER);root.addView(n,new LinearLayout.LayoutParams(-1,dp(34)));
        LinearLayout badges=new LinearLayout(this);badges.setGravity(Gravity.CENTER);
        TextView vip=tv("💎 VIP",12,Color.WHITE,true);vip.setGravity(Gravity.CENTER);vip.setBackground(bg(0xff5796df,7));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(dp(78),dp(30));bp.setMargins(dp(3),0,dp(3),0);badges.addView(vip,bp);
        TextView level=tv("Lv 1",12,Color.WHITE,true);level.setGravity(Gravity.CENTER);level.setBackground(bg(0xff7ddc40,7));LinearLayout.LayoutParams lpv=new LinearLayout.LayoutParams(dp(62),dp(30));lpv.setMargins(dp(3),0,dp(3),0);badges.addView(level,lpv);root.addView(badges,new LinearLayout.LayoutParams(-1,dp(38)));

        final String[] issued9999={"Not issued"}; final int[] followers9999={-1};
        TextView identity=tv("▣ KING ID checking… | Followers",13,0xff66606b,true);
        identity.setGravity(Gravity.CENTER);identity.setOnClickListener(v->copyKingId(uid));root.addView(identity,new LinearLayout.LayoutParams(-1,dp(36)));
        if(db!=null)db.collection("public_profiles").document(uid).get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(profile->{if(isFinishing()||isDestroyed())return;
                String verified=KingPublicId9996.verified(profile.getString("publicId"));
                issued9999[0]=verified==null?"Not issued":verified;
                identity.setText("▣ KING ID "+issued9999[0]+" | "+(followers9999[0]<0?"Followers":followers9999[0]+" Followers"));
            }).addOnFailureListener(err->{if(!isFinishing()&&!isDestroyed())identity.setText("▣ KING ID unavailable");});

        LinearLayout cards=new LinearLayout(this);cards.setGravity(Gravity.CENTER);
        TextView vipStat=profileStatCard("⭐  VIP level","vip.0",0xffe6f8ff);TextView contributionStat=profileStatCard("🏆  Contribution","0",0xffe9fff6);TextView charismaStat=profileStatCard("🔥  Charisma","0",0xfffff7d9);
        for(TextView x:new TextView[]{vipStat,contributionStat,charismaStat}){LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(72),1);cp.setMargins(dp(3),dp(7),dp(3),dp(7));cards.addView(x,cp);}root.addView(cards,new LinearLayout.LayoutParams(-1,dp(86)));
        contributionStat.setOnClickListener(v->profileGiftWallDialog(uid,fallbackName));charismaStat.setOnClickListener(v->profileGiftWallDialog(uid,fallbackName));

        LinearLayout family=new LinearLayout(this);family.setGravity(Gravity.CENTER_VERTICAL);family.setPadding(dp(10),dp(8),dp(10),dp(8));family.setBackground(bg(0xfffff6df,12));
        TextView famIcon=tv("👑",28,0xffffa000,false);famIcon.setGravity(Gravity.CENTER);family.addView(famIcon,new LinearLayout.LayoutParams(dp(48),dp(56)));
        LinearLayout famInfo=new LinearLayout(this);famInfo.setOrientation(LinearLayout.VERTICAL);famInfo.addView(tv("Family",12,0xff7d6030,true));famInfo.addView(tv("Not joined",14,0xff30261b,true));famInfo.setGravity(Gravity.CENTER_VERTICAL);family.addView(famInfo,new LinearLayout.LayoutParams(0,dp(56),1));
        TextView famArrow=tv("›",28,0xffaa956f,false);famArrow.setGravity(Gravity.CENTER);family.addView(famArrow,new LinearLayout.LayoutParams(dp(36),dp(56)));family.setOnClickListener(v->profileFamilyDialog(uid,fallbackName));LinearLayout.LayoutParams fmp=new LinearLayout.LayoutParams(-1,dp(78));fmp.setMargins(0,dp(3),0,dp(6));root.addView(family,fmp);

        LinearLayout relation=new LinearLayout(this);relation.setGravity(Gravity.CENTER_VERTICAL);relation.setPadding(dp(10),dp(8),dp(10),dp(8));relation.setBackground(bg(0xffffeaf4,12));
        TextView relIcon=tv("💞",28,0xffff5e9c,false);relIcon.setGravity(Gravity.CENTER);relation.addView(relIcon,new LinearLayout.LayoutParams(dp(56),dp(56)));
        LinearLayout relInfo=new LinearLayout(this);relInfo.setOrientation(LinearLayout.VERTICAL);relInfo.addView(tv("Relationship",12,0xff9e4c70,true));relInfo.addView(tv("Not set",14,0xff3a2130,true));relInfo.setGravity(Gravity.CENTER_VERTICAL);relation.addView(relInfo,new LinearLayout.LayoutParams(0,dp(56),1));
        TextView relBadge=tv("CP",12,0xffe25291,true);relBadge.setGravity(Gravity.CENTER);relation.addView(relBadge,new LinearLayout.LayoutParams(dp(44),dp(56)));relation.setOnClickListener(v->profileRelationshipDialog(uid,fallbackName));LinearLayout.LayoutParams rlp=new LinearLayout.LayoutParams(-1,dp(78));rlp.setMargins(0,0,0,dp(8));root.addView(relation,rlp);

        LinearLayout wallRow=profileInfoRow("Gift Wall","Tap to view real room gifts","🎁   🌹   💗   ›");wallRow.setOnClickListener(v->profileGiftWallDialog(uid,fallbackName));root.addView(wallRow,new LinearLayout.LayoutParams(-1,dp(68)));
        LinearLayout medalsRow=profileInfoRow("Medals","Tap to view achievements","🏅   🥇   👑   ›");medalsRow.setOnClickListener(v->profileMedalsDialog(uid,fallbackName,host));root.addView(medalsRow,new LinearLayout.LayoutParams(-1,dp(68)));

        LinearLayout actions=new LinearLayout(this);actions.setPadding(0,dp(10),0,0);
        TextView follow=pill("👤  Follow",0xffffbd00,()->{});follow.setTextColor(Color.WHITE);follow.setTextSize(16);actions.addView(follow,new LinearLayout.LayoutParams(0,dp(54),1));
        TextView gift=pill("🎁  SEND GIFT",0xffff693e,()->giftDialogFor(uid,fallbackName));gift.setTextColor(Color.WHITE);gift.setTextSize(16);LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(0,dp(54),1);gp.setMargins(dp(10),0,0,0);actions.addView(gift,gp);root.addView(actions,new LinearLayout.LayoutParams(-1,dp(64)));

        AlertDialog dialog=new AlertDialog.Builder(this).setView(scroll).create();
        mention.setOnClickListener(v->{if(composerBox!=null){composerBox.setText("@"+fallbackName+" ");composerBox.requestFocus();}dialog.dismiss();});
        follow.setOnClickListener(v->toggleProfileFollow(uid,fallbackName,follow));
        more.setOnClickListener(v->showProfileMore(uid,fallbackName));
        dialog.setOnShowListener(x->{android.view.Window w=dialog.getWindow();if(w!=null){w.setLayout(-1,(int)(getResources().getDisplayMetrics().heightPixels*.92f));w.setGravity(Gravity.BOTTOM);}});
        dialog.show();

        if(user!=null&&db!=null){
            String relationId=user.getUid()+"__"+uid;
            final ListenerRegistration[] followListener9997={null};
            followListener9997[0]=db.collection("follows").document(relationId).addSnapshotListener((snap,error)->{
                if(!dialog.isShowing()||isFinishing()||isDestroyed()||error!=null||snap==null)return;
                follow.setText(snap.exists()?"✓  Following":"👤  Follow");
            });
            dialog.setOnDismissListener(d->{if(followListener9997[0]!=null){followListener9997[0].remove();followListener9997[0]=null;}});
            db.collection("follows").whereEqualTo("targetUid",uid).get().addOnSuccessListener(q->{if(isFinishing()||isDestroyed())return;
                followers9999[0]=q.size();identity.setText("▣ KING ID "+issued9999[0]+" | "+q.size()+" Followers");});
            db.collection("live_rooms").document(roomId).collection("members").document(uid).get().addOnSuccessListener(m->{
                if(!m.exists())return;String mn=m.getString("name");if(mn!=null&&!mn.trim().isEmpty())n.setText((host?"👑 ":"")+mn.trim());String photo=m.getString("photoUrl");if(photo!=null&&!photo.trim().isEmpty())loadProfilePhoto(avatarImage,avatarInitial,photo.trim());
            });
            db.collection("public_profiles").document(uid).get().addOnSuccessListener(p->{if(p.exists()){String pn=p.getString("displayName");if(pn!=null&&!pn.trim().isEmpty())n.setText((host?"👑 ":"")+pn.trim());Long lv=p.getLong("level");if(lv!=null)level.setText("Lv "+Math.max(1,lv));Long vv=p.getLong("vipLevel");if(vv!=null)vip.setText("💎 VIP "+Math.max(0,vv));}});
            loadProfileRoomStats(uid,vip,level,vipStat,contributionStat,charismaStat);
        }
    }

    private LinearLayout profileInfoRow(String title,String sub,String right){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(10),dp(7),dp(8),dp(7));row.setBackgroundColor(Color.WHITE);
        LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);info.addView(tv(title,16,0xff332e36,true));info.addView(tv(sub,12,0xff9b959f,false));info.setGravity(Gravity.CENTER_VERTICAL);row.addView(info,new LinearLayout.LayoutParams(0,dp(56),1));
        TextView r=tv(right,14,0xff7d7781,false);r.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);row.addView(r,new LinearLayout.LayoutParams(dp(185),dp(56)));return row;
    }

    private TextView profileStatCard(String title,String value,int color){TextView t=tv(title+"\n"+value,11,0xff395065,true);t.setGravity(Gravity.CENTER);t.setBackground(bg(color,10));return t;}
    private String compactNumber(long value){if(value>=1000000L)return String.format(java.util.Locale.US,"%.1fM",value/1000000.0);if(value>=1000L)return String.format(java.util.Locale.US,"%.1fK",value/1000.0);return String.valueOf(Math.max(0L,value));}
    private void copyKingId(String uid){
        if(db==null||uid==null||uid.isEmpty()){toast("KING ID unavailable");return;}
        db.collection("public_profiles").document(uid).get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(profile->{if(isFinishing()||isDestroyed())return;
             String id=KingPublicId9996.verified(profile.getString("publicId"));
             if(id==null){toast("Verified KING ID not issued");return;}
             ClipboardManager cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
             if(cm!=null)cm.setPrimaryClip(ClipData.newPlainText("KING ID",id));toast("KING ID copied: "+id);
          }).addOnFailureListener(err->toast("KING ID unavailable: "+msg(err)));
    }
    private void loadProfileRoomStats(String uid,TextView vip,TextView level,TextView vipStat,TextView contributionStat,TextView charismaStat){
        if(db==null||roomId==null||uid==null)return;db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(100).get().addOnSuccessListener(q->{long sent=0,received=0,activity=0;for(DocumentSnapshot d:q.getDocuments()){String a=d.getString("actorUid"),t=d.getString("targetUid"),type=d.getString("type");Long raw=d.getLong("giftValue");long v=raw==null?0:Math.max(0,raw);if(uid.equals(a)){activity++;if("gift".equals(type))sent+=v;}if(uid.equals(t)&&"gift".equals(type))received+=v;}int v=(int)Math.min(LevelSystem.MAX_VIP,sent/10000),lv=(int)Math.min(99,1+activity/10+(sent+received)/50000);vip.setText("💎 VIP "+v);level.setText("Lv "+Math.max(1,lv));vipStat.setText("⭐  VIP level\nvip."+v);contributionStat.setText("🏆  Contribution\n"+compactNumber(sent));charismaStat.setText("🔥  Charisma\n"+compactNumber(received));});
    }
    private void logProfileVisit750(String targetUid){if(user==null||db==null||targetUid==null||targetUid.isEmpty()||targetUid.equals(user.getUid()))return;Map<String,Object>v=new HashMap<>();v.put("uid",user.getUid());v.put("name",safeName());v.put("visitedAt",FieldValue.serverTimestamp());v.put("updatedAt",FieldValue.serverTimestamp());db.collection("public_profiles").document(targetUid).collection("visitors").document(user.getUid()).set(v,SetOptions.merge());}
    private void profileFamilyDialog(String uid,String name){boolean self=user!=null&&uid.equals(user.getUid());if(self){SharedPreferences p=getSharedPreferences("MainActivity",MODE_PRIVATE);String family=p.getString("family_name","Not joined"),code=p.getString("family_code","");new AlertDialog.Builder(this).setTitle("👑 Family").setMessage("Current family: "+family+(code.isEmpty()?"":"\nFamily code: "+code)).setPositiveButton("Open Family Center",(d,w)->openCommunityHub700("Family")).setNeutralButton("Share Family",(d,w)->shareFamilyRoom()).setNegativeButton("Close",null).show();}else new AlertDialog.Builder(this).setTitle("👑 "+name+" • Family").setMessage("Family details stay private until shared. You can invite this user in private chat or share your Family room.").setPositiveButton("Private chat",(d,w)->openPrivateChat(uid,name)).setNeutralButton("Share Family",(d,w)->shareFamilyRoom()).setNegativeButton("Close",null).show();}
    private void profileRelationshipDialog(String uid,String name){if(user!=null&&uid.equals(user.getUid())){new AlertDialog.Builder(this).setTitle("💞 Relationship").setMessage("Manage your relationship from the Me page.").setPositiveButton("Open Me page",(d,w)->{Intent i=new Intent(this,MainActivity.class);i.putExtra("openTab",4);startActivity(i);}).setNegativeButton("Close",null).show();return;}new AlertDialog.Builder(this).setTitle("💞 "+name).setMessage("Start a private chat or send a relationship gift.").setPositiveButton("Private chat",(d,w)->openPrivateChat(uid,name)).setNeutralButton("Send Gift",(d,w)->giftDialogFor(uid,name)).setNegativeButton("Close",null).show();}
    private void profileGiftWallDialog(String uid,String name){if(db==null||roomId==null){toast("Live room required");return;}db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(100).get().addOnSuccessListener(q->{ArrayList<String> rows=new ArrayList<>();long total=0;for(DocumentSnapshot d:q.getDocuments())if("gift".equals(d.getString("type"))&&uid.equals(d.getString("targetUid"))){String g=d.getString("giftName"),a=d.getString("actorName");Long val=d.getLong("giftValue"),qty=d.getLong("giftQty");long v=val==null?0:val;total+=v;rows.add((g==null?"Gift":g)+" x"+(qty==null?1:qty)+" • from "+(a==null?"User":a)+" • 💎"+v);}if(rows.isEmpty()){new AlertDialog.Builder(this).setTitle("🎁 "+name+" • Gift Wall").setMessage("No gifts received in this room yet.").setPositiveButton("Send first gift",(d,w)->giftDialogFor(uid,name)).setNegativeButton("Close",null).show();return;}new AlertDialog.Builder(this).setTitle("🎁 Gift Wall • Recent 100 events 💎"+compactNumber(total)).setItems(rows.toArray(new String[0]),null).setPositiveButton("Send Gift",(d,w)->giftDialogFor(uid,name)).setNegativeButton("Close",null).show();}).addOnFailureListener(e->toast("Gift Wall unavailable: "+msg(e)));}
    private void profileMedalsDialog(String uid,String name,boolean host){if(db==null||roomId==null){toast("Live room required");return;}db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(100).get().addOnSuccessListener(q->{long sent=0,received=0,activity=0;for(DocumentSnapshot d:q.getDocuments()){Long val=d.getLong("giftValue");if(uid.equals(d.getString("actorUid"))){activity++;if("gift".equals(d.getString("type"))&&val!=null)sent+=val;}if(uid.equals(d.getString("targetUid"))&&"gift".equals(d.getString("type"))&&val!=null)received+=val;}ArrayList<String> m=new ArrayList<>();if(host)m.add("👑 Room Host");if(activity>=5)m.add("🏅 Active Member");if(sent>0)m.add("💎 Supporter • "+compactNumber(sent));if(received>0)m.add("🌟 Gift Star • "+compactNumber(received));if(m.isEmpty())m.add("No medals earned in this room yet");new AlertDialog.Builder(this).setTitle("🏅 "+name+" • Recent room medals").setItems(m.toArray(new String[0]),null).setPositiveButton("OK",null).show();}).addOnFailureListener(e->toast("Medals unavailable: "+msg(e)));}

    private void openCommunityHub700(String tab){Intent i=new Intent(this,CommunityHubActivity.class);i.putExtra("tab",tab);startActivity(i);}
    private void backpackGiftDialog700(){
        SharedPreferences bp=getSharedPreferences("king_backpack",MODE_PRIVATE);
        String[] keys={"rose","heart","star","firework"};
        String[] names={"Rose","Heart","Star","Firework"};
        String[] icons={"🌹","💗","⭐","🎆"};
        List<String> rows=new ArrayList<>();List<Integer> idx=new ArrayList<>();
        for(int i=0;i<keys.length;i++){
            int count=bp.getInt(keys[i],0);
            if(count>0){rows.add(icons[i]+" "+names[i]+" ×"+count);idx.add(i);}
        }
        if(rows.isEmpty()){
            new AlertDialog.Builder(this).setTitle("🎒 Backpack")
                .setMessage("No free gifts yet. Claim one in Collection Center.")
                .setPositiveButton("Collection",(d,w)->openCommunityHub700("Collection"))
                .setNegativeButton("Close",null).show();return;
        }
        if(giftTargetName==null||giftTargetName.isEmpty()){
            giftTargetName=ownerName;giftTargetUid=ownerUid;
        }
        new AlertDialog.Builder(this).setTitle("🎒 Backpack • To "+giftTargetName)
            .setItems(rows.toArray(new String[0]),(dialog,which)->{
                final int i=idx.get(which);
                final String key=keys[i],name=names[i],icon=icons[i];
                final String targetUid=giftTargetUid,targetName=giftTargetName;
                int available=bp.getInt(key,0);
                if(!cloudRoom||db==null||user==null||!memberSeen){
                    toast("Join a live Party to send a real Backpack Gift");return;
                }
                if(!KingBackpackGift9950.canBegin(available,giftSendPending971)){
                    toast("No available Gift or previous Gift is still synchronizing");return;
                }
                sendGiftQuantityVerified9950(targetUid,targetName,name,icon,0,1,0,()->{
                    int remaining=KingBackpackGift9950.afterCommitted(bp.getInt(key,0));
                    bp.edit().putInt(key,remaining).apply();
                });
                // No speculative 'sent' message or local animation. The
                // existing Gift listener will render recipient-confirmed events.
            })
            .setNeutralButton("Collection",(d,w)->openCommunityHub700("Collection"))
            .setNegativeButton("Close",null).show();
    }
    private void pkHistory940(){
        if(!cloudRoom||db==null||roomId==null){toast("Open a live Party room first");return;}
        db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(180).get().addOnSuccessListener(snap->{
            List<String> rows=new ArrayList<>();
            for(DocumentSnapshot d:snap.getDocuments()){
                String type=d.getString("type");if(!"audio_pk".equals(type)&&!"audio_pk_end".equals(type))continue;
                String text=d.getString("text");if(text==null||text.trim().isEmpty())text="audio_pk".equals(type)?"Audio PK started":"Audio PK ended";
                com.google.firebase.Timestamp at=d.getTimestamp("createdAt");String when=at==null?"":new java.text.SimpleDateFormat("dd MMM • HH:mm",java.util.Locale.US).format(at.toDate());
                rows.add(("audio_pk".equals(type)?"⚔ START  ":"🏁 END  ")+(when.isEmpty()?"":when+"  •  ")+text);if(rows.size()>=40)break;
            }
            if(rows.isEmpty()){new AlertDialog.Builder(this).setTitle("📜 PK History").setMessage("No Audio PK history in this room yet.").setPositiveButton("OK",null).show();return;}
            new AlertDialog.Builder(this).setTitle("📜 PK History").setItems(rows.toArray(new String[0]),null).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast("PK history unavailable: "+msg(e)));
    }

    private void pkTeamSetup940(){
        if(!cloudRoom||db==null||roomId==null){toast("Live room required");return;}
        if(!isModerator()){toast("Host/co-host only");return;}
        List<Integer> occupied=new ArrayList<>();for(int no=1;no<=maxSeats;no++)if(seatUids.get(no)!=null)occupied.add(no);
        if(occupied.isEmpty()){new AlertDialog.Builder(this).setTitle("⚔ PK Team Setup").setMessage("No occupied mic seats yet.").setPositiveButton("OK",null).show();return;}
        DocumentReference state=db.collection("live_rooms").document(roomId).collection("room_settings").document("audio_pk");
        state.get().addOnSuccessListener(doc->{
            Map<String,Object> teams=new HashMap<>();Object raw=doc.get("seatTeams");if(raw instanceof Map)teams.putAll((Map<String,Object>)raw);
            String[] rows=new String[occupied.size()];for(int i=0;i<occupied.size();i++){int no=occupied.get(i);String team=String.valueOf(teams.get(String.valueOf(no)));String badge="red".equalsIgnoreCase(team)?"🔴 RED":"blue".equalsIgnoreCase(team)?"🔵 BLUE":(no%2==0?"🔴 AUTO":"🔵 AUTO");rows[i]="Seat "+no+" • "+seatNames.get(no)+" • "+badge;}
            new AlertDialog.Builder(this).setTitle("⚔ PK Team Setup").setItems(rows,(d,w)->{
                int no=occupied.get(w);String[] choose={"🔴 RED team","🔵 BLUE team","↺ Auto by seat"};
                new AlertDialog.Builder(this).setTitle("Seat "+no+" • "+seatNames.get(no)).setItems(choose,(a,b)->{
                    if(b==0)teams.put(String.valueOf(no),"red");else if(b==1)teams.put(String.valueOf(no),"blue");else teams.remove(String.valueOf(no));
                    Map<String,Object> patch=new HashMap<>();patch.put("seatTeams",teams);patch.put("teamUpdatedAt",FieldValue.serverTimestamp());patch.put("teamUpdatedBy",user==null?"":user.getUid());
                    state.set(patch,SetOptions.merge()).addOnSuccessListener(v->{addEvent("audio_pk_team",safeName()+" updated PK teams");pkTeamSetup940();}).addOnFailureListener(e->toast("PK team update failed: "+msg(e)));
                }).setNegativeButton("Cancel",null).show();
            }).setNeutralButton("Reset Auto",(d,w)->{Map<String,Object> patch=new HashMap<>();patch.put("seatTeams",new HashMap<String,Object>());patch.put("teamUpdatedAt",FieldValue.serverTimestamp());state.set(patch,SetOptions.merge()).addOnSuccessListener(v->toast("PK teams reset to auto"));}).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast("PK team setup unavailable: "+msg(e)));
    }

    private void pkChallengeSearch940(){
        if(!cloudRoom||db==null||user==null||roomId==null){toast("Live Party room required");return;}
        if(!isModerator()){toast("Host/co-host only");return;}
        db.collection("live_rooms").limit(60).get().addOnSuccessListener(snap->{
            List<DocumentSnapshot> rooms940=new ArrayList<>();List<String> labels940=new ArrayList<>();
            for(DocumentSnapshot d:snap.getDocuments()){
                if(d==null||!d.exists()||d.getId().equals(roomId)||Boolean.TRUE.equals(d.getBoolean("closed")))continue;
                String targetOwner=d.getString("ownerUid");if(targetOwner==null||targetOwner.isEmpty()||targetOwner.equals(user.getUid()))continue;
                String targetRoom=str(d,"name","KING Party"),targetHost=str(d,"ownerName","KING Host");
                rooms940.add(d);labels940.add("⚔ "+targetRoom+"  •  "+targetHost);
            }
            if(labels940.isEmpty()){new AlertDialog.Builder(this).setTitle("⚔ PK Challenge").setMessage("No other live Party rooms are available right now.").setPositiveButton("OK",null).show();return;}
            final EditText search940=new EditText(this);search940.setHint("Search room or host");search940.setSingleLine(true);
            new AlertDialog.Builder(this).setTitle("⚔ PK Challenge • "+labels940.size()+" rooms").setView(search940)
                .setPositiveButton("Search",(d,w)->showPkChallengeResults940(rooms940,search940.getText().toString().trim()))
                .setNeutralButton("Show all",(d,w)->showPkChallengeResults940(rooms940,""))
                .setNegativeButton("Cancel",null).show();
        }).addOnFailureListener(e->toast("PK room search unavailable: "+msg(e)));
    }
    private void showPkChallengeResults940(List<DocumentSnapshot> rooms940,String query){
        String needle=query==null?"":query.trim().toLowerCase(java.util.Locale.US);
        List<DocumentSnapshot> filtered940=new ArrayList<>();List<String> labels940=new ArrayList<>();
        for(DocumentSnapshot d:rooms940){
            String rn=str(d,"name","KING Party"),host=str(d,"ownerName","KING Host");
            String hay=(rn+" "+host+" "+d.getId()).toLowerCase(java.util.Locale.US);
            if(!needle.isEmpty()&&!hay.contains(needle))continue;
            filtered940.add(d);labels940.add("⚔ "+rn+"  •  "+host);
        }
        if(labels940.isEmpty()){new AlertDialog.Builder(this).setTitle("PK room search").setMessage("No matching live room.").setPositiveButton("Search again",(d,w)->pkChallengeSearch940()).setNegativeButton("Close",null).show();return;}
        new AlertDialog.Builder(this).setTitle("Choose PK opponent").setItems(labels940.toArray(new String[0]),(d,w)->confirmPkChallenge940(filtered940.get(w))).setNegativeButton("Close",null).show();
    }
    private void confirmPkChallenge940(DocumentSnapshot target){
        if(target==null||user==null)return;
        String targetUid=target.getString("ownerUid"),targetRoom=str(target,"name","KING Party"),targetHost=str(target,"ownerName","KING Host");
        if(targetUid==null||targetUid.isEmpty()){toast("Opponent host is unavailable");return;}
        new AlertDialog.Builder(this).setTitle("⚔ Challenge "+targetHost+"?")
            .setMessage("Room: "+targetRoom+"\n\nThe opponent host will receive a secure PK invitation to join your Party room.")
            .setPositiveButton("Send Challenge",(d,w)->{
                String challengeName="⚔ PK Challenge • "+roomName+" vs "+targetRoom;
                CloudBackend.sendRoomInvite(targetUid,roomId,challengeName,(ok,m)->runOnUiThread(()->{
                    if(ok){addEvent("pk_challenge",safeName()+" challenged "+targetHost+" • "+targetRoom);toast("PK challenge sent to "+targetHost);}
                    else toast(m);
                }));
            }).setNegativeButton("Cancel",null).show();
    }

    private void audioPkPanel700(){
        if(!cloudRoom||db==null||roomId==null){toast("Live room required");return;}
        DocumentReference state=db.collection("live_rooms").document(roomId).collection("room_settings").document("audio_pk");
        state.get().addOnSuccessListener(pk->{
            boolean active=Boolean.TRUE.equals(pk.getBoolean("active"));
            com.google.firebase.Timestamp started=pk.getTimestamp("startedAt");
            Long durationRaw=pk.getLong("durationSec");long duration=durationRaw==null?180L:Math.max(30L,durationRaw);
            long elapsed=started==null?0L:Math.max(0L,(System.currentTimeMillis()-started.toDate().getTime())/1000L);
            long remain=Math.max(0L,duration-elapsed);
            if(active&&remain<=0){active=false;if(isModerator())state.update("active",false,"endedAt",FieldValue.serverTimestamp()).addOnFailureListener(x->{});}
            final boolean pkActive=active;final long pkRemain=remain;final long pkDuration=duration;final com.google.firebase.Timestamp pkStarted=started;
            db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(250).get().addOnSuccessListener(ev->{
                long red=0,blue=0;int gifts=0;
                for(DocumentSnapshot e:ev.getDocuments()){
                    if(!"gift".equals(e.getString("type")))continue;
                    com.google.firebase.Timestamp at=e.getTimestamp("createdAt");if(pkStarted!=null&&at!=null&&at.compareTo(pkStarted)<0)continue;
                    Long raw=e.getLong("giftValue");long score=raw==null?0:Math.max(0,raw);String actor=e.getString("actorUid");Integer seat=null;
                    if(actor!=null)for(Map.Entry<Integer,String>x:seatUids.entrySet())if(actor.equals(x.getValue())){seat=x.getKey();break;}
                    Object customRaw951=pk.get("seatTeams");Map<String,Object> customTeams951=customRaw951 instanceof Map?(Map<String,Object>)customRaw951:null;String customTeam951=seat==null||customTeams951==null?null:String.valueOf(customTeams951.get(String.valueOf(seat)));
                    if("red".equalsIgnoreCase(customTeam951))red+=score;else if("blue".equalsIgnoreCase(customTeam951))blue+=score;else if(seat!=null&&seat%2==0)red+=score;else if(seat!=null)blue+=score;else if((gifts%2)==0)red+=score;else blue+=score;gifts++;
                }
                final long redScore=red,blueScore=blue;
                LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(14),dp(10),dp(14),dp(14));root.setBackgroundColor(0xff15121e);
                TextView stateChip951=tv(pkActive?"● LIVE AUDIO PK":"○ PK RESULT / READY",11,pkActive?0xff65e6a6:0xffc7bdd5,true);stateChip951.setGravity(Gravity.CENTER);stateChip951.setBackground(bg(pkActive?0xff183c31:0xff292432,14));root.addView(stateChip951,new LinearLayout.LayoutParams(-1,dp(36)));
                TextView timer=tv(pkActive?String.format(java.util.Locale.US,"⏱  %02d:%02d",pkRemain/60,pkRemain%60):"🏁  PK RESULT",19,pkActive?0xffffd84d:Color.WHITE,true);timer.setGravity(Gravity.CENTER);root.addView(timer,new LinearLayout.LayoutParams(-1,dp(46)));
                android.widget.ProgressBar progress951=new android.widget.ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress951.setMax((int)Math.min(Integer.MAX_VALUE,Math.max(1L,pkDuration)));progress951.setProgress((int)Math.min(pkDuration,Math.max(0L,pkDuration-pkRemain)));LinearLayout.LayoutParams pp951=new LinearLayout.LayoutParams(-1,dp(10));pp951.setMargins(dp(8),0,dp(8),dp(8));root.addView(progress951,pp951);

                LinearLayout teams=new LinearLayout(this);teams.setGravity(Gravity.CENTER);
                LinearLayout redCard=new LinearLayout(this);redCard.setOrientation(LinearLayout.VERTICAL);redCard.setGravity(Gravity.CENTER);redCard.setBackground(bg(0xff5c2230,16));
                TextView redTitle=tv("🔴 RED",15,Color.WHITE,true);redTitle.setGravity(Gravity.CENTER);redCard.addView(redTitle,new LinearLayout.LayoutParams(-1,dp(30)));
                TextView redVal=tv(compactNumber(redScore),27,0xffffc5cd,true);redVal.setGravity(Gravity.CENTER);redCard.addView(redVal,new LinearLayout.LayoutParams(-1,dp(48)));
                TextView vs=tv("VS",18,0xffffd84d,true);vs.setGravity(Gravity.CENTER);
                LinearLayout blueCard=new LinearLayout(this);blueCard.setOrientation(LinearLayout.VERTICAL);blueCard.setGravity(Gravity.CENTER);blueCard.setBackground(bg(0xff203b66,16));
                TextView blueTitle=tv("🔵 BLUE",15,Color.WHITE,true);blueTitle.setGravity(Gravity.CENTER);blueCard.addView(blueTitle,new LinearLayout.LayoutParams(-1,dp(30)));
                TextView blueVal=tv(compactNumber(blueScore),27,0xffc5dcff,true);blueVal.setGravity(Gravity.CENTER);blueCard.addView(blueVal,new LinearLayout.LayoutParams(-1,dp(48)));
                teams.addView(redCard,new LinearLayout.LayoutParams(0,dp(86),1));teams.addView(vs,new LinearLayout.LayoutParams(dp(52),dp(86)));teams.addView(blueCard,new LinearLayout.LayoutParams(0,dp(86),1));root.addView(teams,new LinearLayout.LayoutParams(-1,dp(92)));

                LinearLayout bar=new LinearLayout(this);TextView rb=new TextView(this);rb.setBackgroundColor(0xffe34d66);TextView bb=new TextView(this);bb.setBackgroundColor(0xff4b85e5);bar.addView(rb,new LinearLayout.LayoutParams(0,dp(10),(float)Math.max(1L,redScore)));bar.addView(bb,new LinearLayout.LayoutParams(0,dp(10),(float)Math.max(1L,blueScore)));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(10));bp.setMargins(0,dp(10),0,dp(8));root.addView(bar,bp);
                String result=redScore==blueScore?"🤝 Draw":redScore>blueScore?"🏆 Red team leads":"🏆 Blue team leads";if(!pkActive)result=redScore==blueScore?"🤝 PK ended in a draw":redScore>blueScore?"🏆 Red team wins":"🏆 Blue team wins";
                TextView resultView=tv(result,15,Color.WHITE,true);resultView.setGravity(Gravity.CENTER);root.addView(resultView,new LinearLayout.LayoutParams(-1,dp(42)));
                TextView giftCount=tv(gifts==0?(pkActive?"No PK gifts yet • send a gift to score":"No gift score recorded in the last PK"):"🎁 PK gifts  "+gifts,11,MUTED,true);giftCount.setGravity(Gravity.CENTER);root.addView(giftCount,new LinearLayout.LayoutParams(-1,dp(30)));

                LinearLayout quick951=new LinearLayout(this);quick951.setGravity(Gravity.CENTER);TextView hist951=tv("🕘 History",11,Color.WHITE,true);hist951.setGravity(Gravity.CENTER);hist951.setBackground(bg(0xff322b42,12));hist951.setOnClickListener(v->pkHistory940());TextView gift951=tv("🎁 Send Gift",11,Color.WHITE,true);gift951.setGravity(Gravity.CENTER);gift951.setBackground(bg(0xff523152,12));gift951.setOnClickListener(v->giftShopPanel());LinearLayout.LayoutParams qp951=new LinearLayout.LayoutParams(0,dp(42),1);qp951.setMargins(dp(3),dp(4),dp(3),0);quick951.addView(hist951,qp951);quick951.addView(gift951,new LinearLayout.LayoutParams(qp951));root.addView(quick951,new LinearLayout.LayoutParams(-1,dp(50)));

                AlertDialog.Builder b=new AlertDialog.Builder(this).setTitle("⚔️ Audio PK").setView(root).setPositiveButton(pkActive?"Refresh":"Close",(d,w)->{if(pkActive)audioPkPanel700();}).setNegativeButton(pkActive?"Close":null,null);
                if(isModerator()){
                    if(pkActive)b.setNeutralButton("End PK",(d,w)->{Map<String,Object>end=new HashMap<>();end.put("active",false);end.put("endedAt",FieldValue.serverTimestamp());end.put("endedBy",user==null?"":user.getUid());state.set(end,SetOptions.merge()).addOnSuccessListener(v->{addEvent("audio_pk_end",safeName()+" ended Audio PK");audioPkPanel700();});});
                    else b.setNeutralButton("Start 3 min PK",(d,w)->{Map<String,Object>start=new HashMap<>();start.put("active",true);start.put("durationSec",180);start.put("startedAt",FieldValue.serverTimestamp());start.put("startedBy",user==null?"":user.getUid());start.put("startedByName",safeName());state.set(start,SetOptions.merge()).addOnSuccessListener(v->{addEvent("audio_pk",safeName()+" started 3-minute Audio PK ⚔️");audioPkPanel700();}).addOnFailureListener(e->toast("PK start failed: "+msg(e)));});
                }
                b.show();
            }).addOnFailureListener(e->new AlertDialog.Builder(this).setTitle("PK score unavailable").setMessage(msg(e)).setPositiveButton("Retry",(d,w)->audioPkPanel700()).setNegativeButton("Close",null).show());
        }).addOnFailureListener(e->new AlertDialog.Builder(this).setTitle("PK state unavailable").setMessage(msg(e)).setPositiveButton("Retry",(d,w)->audioPkPanel700()).setNegativeButton("Close",null).show());
    }

    private int firstOpenSeat700(){for(int i=1;i<=maxSeats;i++)if(!seatUids.containsKey(i)&&!lockedSeats.contains(i))return i;return -1;}
    // Loop Mic shares the same room-bound, server-read seat-request approval flow.
    // It no longer silently redirects a user's requested mic to some other seat.
    private void loopMicPanel700(){
        if(!cloudRoom||db==null||roomId==null||user==null||!memberSeen){
            toast("Join a real live Party to use Loop Mic");return;
        }
        if(!canModerateSeatsV530()){
            int seat=firstOpenSeat700();
            if(seat<1){toast("No open mic seat");return;}
            requestSeat(seat);return;
        }
        final String room10017=roomId,actor10017=user.getUid();
        final int generation10017=presenceGeneration967;
        db.collection("live_rooms").document(room10017).collection("seat_requests")
            .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(q->{
                if(!seatRequestContext10016(room10017,actor10017,generation10017))return;
                if(q.isEmpty()){toast("Loop Mic queue is empty");return;}
                List<DocumentSnapshot> docs=q.getDocuments();List<String> rows=new ArrayList<>();
                for(DocumentSnapshot d:docs){
                    Long seat=d.getLong("seatNo");
                    rows.add(str(d,"name","User")+" • Requested Seat "+(seat==null?"?":seat));
                }
                new AlertDialog.Builder(this).setTitle("🔁 Loop Mic requests")
                    .setItems(rows.toArray(new String[0]),(x,w)->{
                        if(!seatRequestContext10016(room10017,actor10017,generation10017)){
                            toast("Party changed; reopen Loop Mic");return;
                        }
                        DocumentSnapshot request=docs.get(w);
                        Long seat=request.getLong("seatNo");
                        if(seat==null||seat<1||seat>maxSeats){toast("Invalid requested seat");return;}
                        seatRequestDecision(request,seat.intValue(),room10017,actor10017,generation10017);
                    }).setNegativeButton("Close",null).show();
            }).addOnFailureListener(e->{
                if(seatRequestContext10016(room10017,actor10017,generation10017))
                    toast("Loop Mic queue unavailable: "+msg(e));
            });
    }

    private String publicId(String uid){if(uid==null||uid.isEmpty())return "000000";long value=Integer.toUnsignedLong(uid.hashCode());return String.valueOf((value%900000L)+100000L);}

    private SharedPreferences mainProfilePrefs868(){return getSharedPreferences("MainActivity",MODE_PRIVATE);}
    private String localProfilePhoto868(){String s=mainProfilePrefs868().getString("profile_photo","");return s==null?"":s.trim();}
    private String cloudProfilePhoto868(){String s=mainProfilePrefs868().getString("profile_photo_cloud","");return s==null?"":s.trim();}
    private void applyProfilePhoto868(ImageView image,TextView fallback,String source){
        if(source==null||source.trim().isEmpty())return;String s=source.trim();
        if(s.startsWith("content://")||s.startsWith("file://")||s.startsWith("android.resource://")){
            if(KingSafeImageUri10070.show(image,Uri.parse(s))&&image.getDrawable()!=null)fallback.setVisibility(View.GONE);
            return;
        }
        loadProfilePhoto(image,fallback,s);
    }
    private void refreshOwnMemberPhoto868(){
        if(!cloudRoom||db==null||user==null||roomId==null)return;String cloud=cloudProfilePhoto868();
        if(cloud.isEmpty()&&user.getPhotoUrl()!=null)cloud=user.getPhotoUrl().toString();
        if(!cloud.isEmpty())db.collection("live_rooms").document(roomId).collection("members").document(user.getUid()).update("photoUrl",cloud).addOnFailureListener(e->{});
    }

    private void loadProfilePhoto(ImageView image,TextView fallback,String url){
        if(image==null||url==null||url.isEmpty())return;
        image.setTag(url);
        Bitmap cached=photoCache540.get(url);
        if(cached!=null&&!cached.isRecycled()){
            image.setImageBitmap(cached);
            if(fallback!=null)fallback.setVisibility(View.GONE);
            return;
        }
        try{
            roomImageExecutor964.execute(()->{
                Bitmap bitmap=null;
                try{
                    URL source=new URL(url);
                    if(!"https".equalsIgnoreCase(source.getProtocol()))return;
                    // Decode only the bounds on the first stream. Never allocate a full photo.
                    BitmapFactory.Options bounds=new BitmapFactory.Options();
                    bounds.inJustDecodeBounds=true;
                    java.net.URLConnection c=source.openConnection();
                    c.setConnectTimeout(7000);c.setReadTimeout(7000);
                    try(java.io.InputStream in=c.getInputStream()){
                        BitmapFactory.decodeStream(in,null,bounds);
                    }
                    if(bounds.outWidth<=0||bounds.outHeight<=0)return;
                    int sample=1;
                    while(bounds.outWidth/sample>256||bounds.outHeight/sample>256)sample*=2;
                    BitmapFactory.Options options=new BitmapFactory.Options();
                    options.inSampleSize=sample;
                    options.inPreferredConfig=Bitmap.Config.RGB_565;
                    java.net.URLConnection small=source.openConnection();
                    small.setConnectTimeout(7000);small.setReadTimeout(7000);
                    try(java.io.InputStream in=small.getInputStream()){
                        bitmap=BitmapFactory.decodeStream(in,null,options);
                    }
                    if(bitmap==null)return;
                    if(roomImageExecutor964.isShutdown())return;
                    photoCache540.put(url,bitmap);
                    final Bitmap photo964=bitmap;
                    runOnUiThread(()->{
                        if(!isFinishing()&&!isDestroyed()&&url.equals(image.getTag())){
                            image.setImageBitmap(photo964);
                            if(fallback!=null)fallback.setVisibility(View.GONE);
                        }
                    });
                }catch(Exception ignored){}
                catch(OutOfMemoryError ignored){
                    // The operating system can be under native-memory pressure even if Java heap is small.
                }
            });
        }catch(java.util.concurrent.RejectedExecutionException ignored){}
    }

    private void toggleProfileFollow(String uid,String name,TextView button){
        if(user==null||db==null){toast("Google / Firebase sign-in required");return;}
        if(uid==null||uid.equals(user.getUid())){toast("This is you");return;}
        button.setEnabled(false);
        KingFollowLive9997.toggle(db,user,uid,safeName(),(following,error)->{
            if(isFinishing()||isDestroyed())return;
            button.setEnabled(true);
            if(error!=null){toast("Follow not changed: "+msg(error));return;}
            button.setText(Boolean.TRUE.equals(following)?"✓  Following":"👤  Follow");
            toast(Boolean.TRUE.equals(following)?"Following "+name:"Unfollowed "+name);
        });
    }

    private void showProfileMore(String uid,String name){
        List<String> items=new ArrayList<>();items.add("💬 Private chat");items.add("🎁 Send gift");items.add("⚑ Report");
        int seatNo=-1;for(Map.Entry<Integer,String> e:seatUids.entrySet())if(uid!=null&&uid.equals(e.getValue())){seatNo=e.getKey();break;}
        final int occupiedSeat=seatNo;
        if(isModerator()&&uid!=null&&!uid.equals(ownerUid)){
            if(occupiedSeat>0){items.add(Boolean.FALSE.equals(seatMics.get(occupiedSeat))?"🎤 Unmute seat":"🔇 Mute seat");items.add("↔ Move seat");items.add("⬇ Remove from seat");}
            else items.add("🎙 Assign mic seat");
            items.add("🚪 Kick");items.add("🚫 Ban");
            if(isOwner())items.add("👑 Co-host role");
        }else if(uid!=null&&(user==null||!uid.equals(user.getUid())))items.add("🚫 Block");
        new AlertDialog.Builder(this).setTitle(name).setItems(items.toArray(new String[0]),(d,w)->{String x=items.get(w);
            if(x.contains("Private"))openPrivateChat(uid,name);else if(x.contains("Gift"))giftDialogFor(uid,name);else if(x.contains("Report"))reportUser(uid,name);
            else if(x.contains("Mute seat")||x.contains("Unmute seat"))hostToggleSeat(occupiedSeat);else if(x.contains("Move seat"))moveUserToSeatDialog(uid,name,occupiedSeat);else if(x.contains("Assign mic seat"))moveUserToSeatDialog(uid,name,-1);else if(x.contains("Remove from seat"))hostRemoveSeat(occupiedSeat);
            else if(x.contains("Kick"))kickUser(uid,name);else if(x.contains("Ban"))banUser(uid,name);else if(x.contains("Co-host"))coHostDialog(uid,name);else if(x.contains("Block"))blockUserLocally(uid,name);
        }).setNegativeButton("Close",null).show();
    }

    private void kickUser(String uid,String name){
        if(!isModerator()||uid==null||uid.isEmpty()||uid.equals(ownerUid)){toast("Cannot kick this member");return;}
        String[] reasons={"Room rule violation","Spam / flooding","Abusive behavior","Mic disruption","Other"};
        new AlertDialog.Builder(this).setTitle("🚪 Kick • "+name).setMessage("Choose a reason").setItems(reasons,(d,w)->performKick940(uid,name,reasons[w])).setNegativeButton("Cancel",null).show();
    }
    // Atomic moderation: when the member leaves, every occupied mic seat and
    // their co-host role must be cleared in the same Firestore transaction.
    // A failed write must not announce a successful kick/ban.
    private void performKick940(String uid,String name,String reason){
        moderateMember10011(uid,name,reason,false);
    }
    private void moderateMember10011(String uid,String name,String reason,boolean ban10011){
        if(!isModerator()||user==null||!cloudRoom||db==null||roomId==null
            ||uid==null||uid.isEmpty()||uid.equals(ownerUid)||uid.equals(user.getUid())){
            toast("Host/co-host and an eligible member required");return;
        }
        final String target10011=uid, room10011=roomId, actor10011=user.getUid();
        final int binding10011=roomListenerGeneration982, visit10011=presenceGeneration967;
        final String why10011=(reason==null||reason.trim().isEmpty())?"Moderator action":reason.trim();
        final DocumentReference ref10011=db.collection("live_rooms").document(room10011);
        db.runTransaction(tx->{
            DocumentSnapshot live10011=tx.get(ref10011);
            if(!live10011.exists()||Boolean.TRUE.equals(live10011.getBoolean("closed")))
                throw new IllegalStateException("Room closed or no longer available");
            boolean owner10011=actor10011.equals(live10011.getString("ownerUid"));
            DocumentReference member10011=ref10011.collection("members").document(target10011);
            DocumentReference role10011=ref10011.collection("roles").document(target10011);
            DocumentSnapshot existing10011=tx.get(member10011);
            DocumentSnapshot targetRole10011=tx.get(role10011);
            if(!existing10011.exists())throw new IllegalStateException("Member already left");
            boolean targetCoHost10011=targetRole10011.exists()
                &&"cohost".equals(targetRole10011.getString("role"));
            if(targetCoHost10011&&!owner10011)
                throw new IllegalStateException("Only the host can moderate a co-host");
            // Read all twelve possible stage seats before applying any writes.
            // Firestore retries when a seat changed during this transaction.
            java.util.List<DocumentReference> occupied10011=new java.util.ArrayList<>();
            for(int seat10011=1;seat10011<=12;seat10011++){
                DocumentReference seatRef10011=ref10011.collection("seats").document(String.valueOf(seat10011));
                DocumentSnapshot seatDoc10011=tx.get(seatRef10011);
                if(seatDoc10011.exists()&&target10011.equals(seatDoc10011.getString("uid")))
                    occupied10011.add(seatRef10011);
            }
            tx.delete(member10011);
            for(DocumentReference seatRef10011:occupied10011)tx.delete(seatRef10011);
            if(targetCoHost10011)tx.delete(role10011);
            if(ban10011){
                java.util.Map<String,Object> banData10011=new java.util.HashMap<>();
                banData10011.put("uid",target10011);banData10011.put("name",name);
                banData10011.put("reason",why10011);banData10011.put("active",true);
                banData10011.put("byUid",actor10011);banData10011.put("byName",safeName());
                banData10011.put("createdAt",FieldValue.serverTimestamp());
                tx.set(ref10011.collection("room_bans").document(target10011),banData10011);
            }
            return occupied10011.size();
        }).addOnSuccessListener(cleared10011->{
            if(!activeRoomCallback982(room10011,actor10011,visit10011,binding10011))return;
            addEvent(ban10011?"ban":"kick",name+(ban10011?" banned":" removed")+" • "+why10011);
            toast(name+(ban10011?" banned":" kicked")+" • "+cleared10011+" seats freed");
        }).addOnFailureListener(error10011->{
            if(activeRoomCallback982(room10011,actor10011,visit10011,binding10011))
                toast((ban10011?"Ban":"Kick")+" failed: "+msg(error10011));
        });
    }
    private void banUser(String uid,String name){
        if(!isModerator()||uid==null||uid.isEmpty()||uid.equals(ownerUid)){
            toast("Cannot ban this member");return;
        }
        if(!cloudRoom||db==null){toast("Live room required");return;}
        String[] reasons={"Repeated rule violation","Spam / scam","Abusive behavior","Harassment","Other"};
        new AlertDialog.Builder(this).setTitle("🚫 Ban • "+name)
            .setMessage("Remove from the live room and prevent re-entry until unbanned.")
            .setItems(reasons,(d,w)->moderateMember10011(uid,name,reasons[w],true))
            .setNegativeButton("Cancel",null).show();
    }
    private void coHostDialog(String uid,String name){
        if(!isOwner()||uid==null||uid.isEmpty()||uid.equals(ownerUid)){toast("Host only");return;}
        String[] actions={"Make co-host","Remove co-host"};
        new AlertDialog.Builder(this).setTitle("Co-host • "+name).setItems(actions,(d,w)->{
            DocumentReference ref=db.collection("live_rooms").document(roomId).collection("roles").document(uid);
            if(w==0){
                Map<String,Object> role=new HashMap<>();role.put("uid",uid);role.put("role","cohost");role.put("grantedBy",user.getUid());role.put("updatedAt",FieldValue.serverTimestamp());
                ref.set(role).addOnSuccessListener(v->toast(name+" is now co-host")).addOnFailureListener(e->toast(msg(e)));
            }else ref.delete().addOnSuccessListener(v->toast("Co-host removed")).addOnFailureListener(e->toast(msg(e)));
        }).show();
    }

    private void channelEntranceSettings9931(){
        if(!isOwner()||!cloudRoom||db==null||user==null){toast("Live room Host only");return;}
        String[] items={"🌐 Free entry","👪 Verified KING Family members","🔑 Password required","💜 Following / followed by HOST","🔐 Invite by UID only"};
        int checked="family".equals(roomEntranceMode9931)?1:"password".equals(roomEntranceMode9931)?2:"follow".equals(roomEntranceMode9931)?3:roomPrivate?4:0;
        new AlertDialog.Builder(this).setTitle("🚪 Channel Entrance Settings")
        .setSingleChoiceItems(items,checked,(dialog,choice)->{
            dialog.dismiss();
            if(choice==0)updateEntrance9931("free","");
            else if(choice==1)familyEntrance9931();
            else if(choice==2)setRoomPasswordDialog();
            else if(choice==3)new AlertDialog.Builder(this).setTitle("Host Follow Connections")
                 .setMessage("Only signed-in accounts following the Host or followed by the Host can enter. This does NOT yet include all room members' followers. Existing visitors may lose access.")
                 .setNegativeButton("Cancel",null)
                 .setPositiveButton("Enable",(d,w)->updateEntrance9931("follow","")).show();
            else updateEntrance9931("invite","");
        }).setNegativeButton("Cancel",null).show();
    }
    private void familyEntrance9931(){
        db.collection("users").document(user.getUid()).get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(x->{
            String code=x==null?"":str(x,"familyCode","");
            if(code.isEmpty()){toast("Create or join a KING Family first");return;}
            db.collection("families").document(code).collection("members").document(user.getUid())
              .get(com.google.firebase.firestore.Source.SERVER)
              .addOnSuccessListener(m->{
                if(!m.exists()){toast("Family membership not verified");return;}
                new AlertDialog.Builder(this).setTitle("Family Only")
                  .setMessage("Only members of Family "+code+" may join. Nonmembers lose access, even if they were already seated. Requires Firestore rules deployment.")
                  .setNegativeButton("Cancel",null)
                  .setPositiveButton("Enable",(d,w)->updateEntrance9931("family",code)).show();
              }).addOnFailureListener(e->toast("Family membership check failed"));
          }).addOnFailureListener(e->toast("Family lookup failed"));
    }
    private void updateEntrance9931(String mode,String code){
        if(!isOwner()||!cloudRoom||db==null||roomId==null)return;
        if(!java.util.Arrays.asList("free","family","follow","invite").contains(mode))return;
        Map<String,Object> change=new HashMap<>();
        change.put("isPrivate",!"free".equals(mode));
        change.put("hasPassword",false);
        change.put("entranceMode",mode);
        final String freshEpoch9936=java.util.UUID.randomUUID().toString();
        change.put("entryEpoch",freshEpoch9936);
        change.put("familyCode","family".equals(mode)?code:"");
        change.put("updatedAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).update(change)
          .addOnSuccessListener(v->{roomPrivate=!"free".equals(mode);roomHasPassword=false;roomEntranceMode9931=mode;roomEntryEpoch9936=freshEpoch9936;
            refreshRoomState();toast("Entrance mode: "+mode+" • server rules required");})
          .addOnFailureListener(e->toast("Entrance settings failed: "+msg(e)));
    }
    private void setPrivateRoom(boolean value){
        if(!isOwner()){toast("Host only");return;}
        if(!cloudRoom){roomPrivate=value;toast(value?"Private room enabled":"Room is public");refreshRoomState();return;}
        if(!value){
            updateEntrance9931("free","");return;
        }
        updateEntrance9931("invite","");
    }

    private void setRoomPasswordDialog(){
        if(!isOwner()||!cloudRoom||db==null){toast("Live host room required");return;}
        final EditText e=new EditText(this);e.setHint("At least 4 characters");e.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle(roomHasPassword?"Change room password":"Set room password").setView(e)
            .setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{
                String password=e.getText().toString();if(password.length()<4){toast("Password must be at least 4 characters");return;}
                String hash=passwordProof(roomId,password);if(hash.isEmpty()){toast("Could not secure password");return;}
                final String passwordEpoch9936=java.util.UUID.randomUUID().toString();
                Map<String,Object> secret=new HashMap<>();secret.put("passwordHash",hash);secret.put("ownerUid",user.getUid());secret.put("updatedAt",FieldValue.serverTimestamp());
                db.collection("live_rooms").document(roomId).collection("secrets").document("password").set(secret)
                    .addOnSuccessListener(v->db.collection("live_rooms").document(roomId).update("isPrivate",true,"hasPassword",true,"entranceMode","password","familyCode","","entryEpoch",passwordEpoch9936,"updatedAt",FieldValue.serverTimestamp())
                        .addOnSuccessListener(x->{roomPrivate=true;roomHasPassword=true;roomEntranceMode9931="password";roomEntryEpoch9936=passwordEpoch9936;toast("Password updated • previous grants revoked; members must rejoin");refreshRoomState();})
                        .addOnFailureListener(x->toast(msg(x))))
                    .addOnFailureListener(x->toast("Password save failed: "+msg(x)));
            }).show();
    }
    private void removeRoomPassword(){
        if(!isOwner()||!cloudRoom||db==null)return;
        final String removeEpoch9936=java.util.UUID.randomUUID().toString();
        db.collection("live_rooms").document(roomId).collection("secrets").document("password").delete()
            .addOnSuccessListener(v->db.collection("live_rooms").document(roomId).update("hasPassword",false,"entranceMode","invite","entryEpoch",removeEpoch9936,"updatedAt",FieldValue.serverTimestamp())
                .addOnSuccessListener(x->{roomHasPassword=false;roomEntranceMode9931="invite";roomEntryEpoch9936=removeEpoch9936;toast("Password removed • fresh invites required");}))
            .addOnFailureListener(e->toast(msg(e)));
    }

    private void blockUserLocally(String uid,String name){
        Set<String>b=new HashSet<>(prefs.getStringSet("blocked",new HashSet<>()));b.add(uid);prefs.edit().putStringSet("blocked",b).apply();toast(name+" blocked locally");
    }
    private String memberNameForUid(String uid){
        if(uid==null)return "User"; if(uid.equals(ownerUid))return ownerName==null?"Host":ownerName;
        for(Map.Entry<String,String> e:memberUids.entrySet())if(uid.equals(e.getValue()))return e.getKey();
        return uid.length()>8?"User "+uid.substring(0,8):uid;
    }
    private void hostProfileDialog(){
        if(ownerName==null)ownerName="Host";
        if(ownerUid!=null&&!ownerUid.isEmpty()&&!isOwner()){showRichProfile(ownerUid,ownerName,true);return;}
        String details="Host: "+ownerName+"\nRoom: "+roomName+"\nRoom ID: "+shortId();
        List<String> items=new ArrayList<>();items.add("🔗 Share room");items.add("🏆 Room ranking");items.add("🎁 Gift history");String[] a=items.toArray(new String[0]);
        new AlertDialog.Builder(this).setTitle("👑 Host profile").setMessage(details).setItems(a,(d,w)->{String x=a[w];if(x.contains("Share"))shareRoom();else if(x.contains("ranking"))roomRankingDialog();else giftHistoryDialog();}).setNegativeButton("Close",null).show();
    }
    private void roomInfoDialog(){
        String privacy=roomPrivate?(roomHasPassword?"Private • password":"Private • invite only"):"Public";
        String role=isOwner()?"Host":(coHost?"Co-host":"Member");
        String info="Room: "+roomName+"\nID: "+shortId()+"\nCategory: "+roomCategory+"\nTheme: "+roomTheme+
            "\nMic seats: "+maxSeats+"\nAccess: "+privacy+"\nRole: "+role+"\nSeat lock: "+(roomLocked?"ON":"OFF")+
            "\nMute all: "+(muteAll?"ON":"OFF")+(currentSong==null||currentSong.isEmpty()?"":"\nSong: "+currentSong);
        new AlertDialog.Builder(this).setTitle("ℹ Party room info").setMessage(info).setPositiveButton("OK",null).show();
    }
    private void recentActivityDialog(){
        if(!cloudRoom||db==null||roomId==null||user==null){
            new AlertDialog.Builder(this).setTitle("🕘 Recent activity")
                .setMessage("Join a live Party to see saved activity.")
                .setPositiveButton("OK",null).show();return;
        }
        loadRoomActivityPage10019(roomId,user.getUid(),null,1);
    }
    // Firestore cursor paging, never mixes events after joining a different room.
    private void loadRoomActivityPage10019(String room10019,String actor10019,
                 DocumentSnapshot cursor10019,int page10019){
        if(!partyActivityCurrent10019(room10019,actor10019))return;
        Query q10019=db.collection("live_rooms").document(room10019).collection("events")
            .orderBy("createdAt",Query.Direction.DESCENDING).limit(80);
        if(cursor10019!=null)q10019=q10019.startAfter(cursor10019);
        q10019.get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(snap->{
            if(!partyActivityCurrent10019(room10019,actor10019))return;
            List<String> rows10019=new ArrayList<>();
            for(DocumentSnapshot event10019:snap.getDocuments())
                rows10019.add(str(event10019,"text","Room activity"));
            if(rows10019.isEmpty())rows10019.add("No activity in this page");
            final boolean more10019=snap.size()==80;
            final DocumentSnapshot next10019=more10019?snap.getDocuments().get(snap.size()-1):null;
            final int moreIndex10019=rows10019.size();
            rows10019.add(more10019?"↓ Load older room activity":"✓ End of activity history");
            new AlertDialog.Builder(this).setTitle("🕘 Room activity • Page "+page10019)
                .setItems(rows10019.toArray(new String[0]),(d,w)->{
                    if(!partyActivityCurrent10019(room10019,actor10019))return;
                    if(more10019&&w==moreIndex10019)
                        loadRoomActivityPage10019(room10019,actor10019,next10019,page10019+1);
                }).setPositiveButton("Refresh",(d,w)->{
                    if(partyActivityCurrent10019(room10019,actor10019))
                        loadRoomActivityPage10019(room10019,actor10019,null,1);
                }).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->{
            if(partyActivityCurrent10019(room10019,actor10019))
                new AlertDialog.Builder(this).setTitle("Activity unavailable")
                    .setMessage(msg(e)).setPositiveButton("Retry",(d,w)->
                        loadRoomActivityPage10019(room10019,actor10019,cursor10019,page10019))
                    .setNegativeButton("Close",null).show();
        });
    }
    private boolean partyActivityCurrent10019(String room10019,String actor10019){
        return !isFinishing()&&!isDestroyed()&&cloudRoom&&memberSeen&&db!=null&&user!=null
            &&room10019!=null&&room10019.equals(roomId)
            &&actor10019!=null&&actor10019.equals(user.getUid());
    }
    private void giftHistoryDialog(){
        if(!cloudRoom||db==null||roomId==null||user==null){
            new AlertDialog.Builder(this).setTitle("🎁 Gift history")
                .setMessage("Join a live Firebase Party to see its gift history.")
                .setPositiveButton("OK",null).show();return;
        }
        loadGiftHistoryPage10018(roomId,user.getUid(),null,1);
    }
    // Real per-room cursor pagination. Gift events older than the first 100
    // must remain accessible without ever showing another room's history.
    private void loadGiftHistoryPage10018(String room10018,String actor10018,
                                         DocumentSnapshot cursor10018,int page10018){
        if(isFinishing()||isDestroyed()||!cloudRoom||db==null||user==null
            ||!room10018.equals(roomId)||!actor10018.equals(user.getUid())||!memberSeen)return;
        Query query10018=db.collection("live_rooms").document(room10018)
            .collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(100);
        if(cursor10018!=null)query10018=query10018.startAfter(cursor10018);
        query10018.get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(snap->{
            if(isFinishing()||isDestroyed()||user==null
                ||!room10018.equals(roomId)||!actor10018.equals(user.getUid())||!memberSeen)return;
            List<String> rows10018=new ArrayList<>();
            for(DocumentSnapshot event10018:snap.getDocuments())
                if("gift".equals(event10018.getString("type")))
                    rows10018.add(str(event10018,"text","Gift sent"));
            if(rows10018.isEmpty())rows10018.add("No gifts in this page of room events");
            final boolean more10018=snap.size()==100;
            final DocumentSnapshot next10018=more10018?snap.getDocuments().get(snap.size()-1):null;
            final int olderIndex10018=rows10018.size();
            if(more10018)rows10018.add("↓ Load earlier room gift events");
            if(!more10018)rows10018.add("✓ End of available room gift history");
            new AlertDialog.Builder(this).setTitle("🎁 Room Gift History • Page "+page10018)
                .setItems(rows10018.toArray(new String[0]),(dialog,choice)->{
                    if(!memberSeen||user==null||!room10018.equals(roomId)
                        ||!actor10018.equals(user.getUid()))return;
                    if(more10018&&choice==olderIndex10018)
                        loadGiftHistoryPage10018(room10018,actor10018,next10018,page10018+1);
                }).setNegativeButton("Close",null).show();
        }).addOnFailureListener(error10018->{
            if(!isFinishing()&&!isDestroyed()&&user!=null
                &&room10018.equals(roomId)&&actor10018.equals(user.getUid()))
                new AlertDialog.Builder(this).setTitle("Gift history unavailable")
                    .setMessage(msg(error10018))
                    .setPositiveButton("Retry",(d,w)->
                        loadGiftHistoryPage10018(room10018,actor10018,cursor10018,page10018))
                    .setNegativeButton("Close",null).show();
        });
    }
    private void roomRankingDialog(){
        if(!cloudRoom||db==null||user==null||roomId==null||!memberSeen){
            new AlertDialog.Builder(this).setTitle("🏆 Room ranking")
                .setMessage("Join this live Party to view its verified paid-gift ranking.")
                .setPositiveButton("OK",null).show();return;
        }
        final String rankRoom10018=roomId,rankUid10025=user.getUid();
        final int rankGeneration10025=presenceGeneration967;
        db.collection("live_rooms").document(rankRoom10018).collection("events")
            .orderBy("createdAt",Query.Direction.DESCENDING).limit(250)
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(snap->{
                if(!isActivePresence967(rankRoom10018,rankUid10025,rankGeneration10025)
                    ||!memberSeen)return;
                Map<String,Long>score=new HashMap<>();Map<String,Integer>count=new HashMap<>();
                Map<String,String>displayNames10018=new HashMap<>();
                for(DocumentSnapshot d:snap.getDocuments())if("gift".equals(d.getString("type"))){
                    String uid10018=d.getString("actorUid");Long v=d.getLong("giftValue");
                    // Score only paid gifts attached to a real sender UID.
                    // A FREE gift costs 0 diamonds, and two users may have the same name.
                    if(uid10018==null||uid10018.trim().isEmpty()||v==null||v<=0)continue;
                    String n=str(d,"actorName","User");
                    displayNames10018.put(uid10018,n);
                    score.put(uid10018,score.containsKey(uid10018)?score.get(uid10018)+v:v);
                    count.put(uid10018,count.containsKey(uid10018)?count.get(uid10018)+1:1);
                }
                List<Map.Entry<String,Long>>list=new ArrayList<>(score.entrySet());java.util.Collections.sort(list,(a,b)->Long.compare(b.getValue(),a.getValue()));
                LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(12),dp(8),dp(12),dp(12));root.setBackgroundColor(0xfff7f5fa);
                TextView sub951=tv("Verified paid-gift supporters • latest 250 room events",11,0xff77717f,true);sub951.setGravity(Gravity.CENTER);root.addView(sub951,new LinearLayout.LayoutParams(-1,dp(34)));
                if(list.isEmpty()){TextView empty=tv("🏆\\nNo gift ranking yet\\nSend the first gift to enter the board.",14,0xff81798b,true);empty.setGravity(Gravity.CENTER);root.addView(empty,new LinearLayout.LayoutParams(-1,dp(150)));}
                else{
                    LinearLayout podium=new LinearLayout(this);podium.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
                    int[] order={1,0,2};String[] medals={"🥇","🥈","🥉"};
                    for(int pos:order){if(pos>=list.size())continue;Map.Entry<String,Long> e=list.get(pos);LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setBackground(bg(pos==0?0xffffefad:pos==1?0xffe9edf4:0xffffe1c4,16));TextView med=tv(medals[pos],25,0xff333333,false);med.setGravity(Gravity.CENTER);card.addView(med,new LinearLayout.LayoutParams(-1,dp(38)));TextView nm=tv(displayNames10018.get(e.getKey()),12,0xff2c2930,true);nm.setGravity(Gravity.CENTER);nm.setSingleLine(true);card.addView(nm,new LinearLayout.LayoutParams(-1,dp(26)));TextView val=tv("💎 "+compactNumber(e.getValue()),11,0xff725b00,true);val.setGravity(Gravity.CENTER);card.addView(val,new LinearLayout.LayoutParams(-1,dp(24)));int h=pos==0?dp(112):dp(98);LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,h,1);cp.setMargins(dp(3),0,dp(3),0);podium.addView(card,cp);}
                    root.addView(podium,new LinearLayout.LayoutParams(-1,dp(120)));
                    ScrollView sc=new ScrollView(this);LinearLayout rows=new LinearLayout(this);rows.setOrientation(LinearLayout.VERTICAL);sc.addView(rows);
                    for(int i=3;i<list.size()&&i<20;i++){Map.Entry<String,Long>e=list.get(i);LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(10),0,dp(10),0);row.setBackground(bg(Color.WHITE,12));TextView rank=tv(String.valueOf(i+1),13,0xff81798b,true);rank.setGravity(Gravity.CENTER);row.addView(rank,new LinearLayout.LayoutParams(dp(38),dp(48)));TextView name=tv(displayNames10018.get(e.getKey()),13,0xff2c2930,true);name.setSingleLine(true);row.addView(name,new LinearLayout.LayoutParams(0,dp(48),1));TextView val=tv("💎"+compactNumber(e.getValue())+"  • "+count.get(e.getKey())+" gifts",11,0xff756d7c,true);val.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);row.addView(val,new LinearLayout.LayoutParams(dp(145),dp(48)));LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(50));rp.setMargins(0,dp(2),0,dp(2));rows.addView(row,rp);}
                    root.addView(sc,new LinearLayout.LayoutParams(-1,Math.min(dp(360),Math.max(dp(80),(Math.max(0,Math.min(20,list.size())-3))*dp(52)))));
                }
                AlertDialog dlg951=new AlertDialog.Builder(this).setTitle("🏆 Room Gift Ranking").setView(root).setPositiveButton("Refresh",(d,w)->roomRankingDialog()).setNegativeButton("Close",null).create();dlg951.show();
            })
            .addOnFailureListener(e->{
                if(isActivePresence967(rankRoom10018,rankUid10025,rankGeneration10025)
                    &&memberSeen)
                    new AlertDialog.Builder(this).setTitle("Ranking unavailable")
                        .setMessage(msg(e)).setPositiveButton("Retry",(d,w)->roomRankingDialog())
                        .setNegativeButton("Close",null).show();
            });
    }
    private void allSeatRequestsDialog(){
        if(!canModerateSeatsV530()||!cloudRoom||db==null||roomId==null||user==null){toast("Host/co-host only");return;}
        final String room10016=roomId,actor10016=user.getUid();
        final int generation10016=presenceGeneration967;
        db.collection("live_rooms").document(room10016).collection("seat_requests")
            .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(snap->{
                if(!seatRequestContext10016(room10016,actor10016,generation10016))return;
                if(snap.isEmpty()){toast("No pending seat requests");return;}
                List<DocumentSnapshot> docs=new ArrayList<>();List<String> rows=new ArrayList<>();
                for(DocumentSnapshot d:snap.getDocuments()){
                    Long n=d.getLong("seatNo");String requestedUid10024=d.getString("uid");
                    if(n==null||n<1||n>maxSeats||requestedUid10024==null
                       ||!requestedUid10024.equals(d.getId()))continue;
                    docs.add(d);rows.add(str(d,"name","User")+" • Seat "+n);
                }
                if(rows.isEmpty()){toast("No valid pending seat requests");return;}
                new AlertDialog.Builder(this).setTitle("📥 Seat requests")
                    .setItems(rows.toArray(new String[0]),(x,w)->{
                        if(!seatRequestContext10016(room10016,actor10016,generation10016)){
                            toast("Room changed; reopen requests");return;
                        }
                        DocumentSnapshot req=docs.get(w);Long n=req.getLong("seatNo");
                        if(n!=null)seatRequestDecision(req,n.intValue(),room10016,actor10016,generation10016);
                    }).setNegativeButton("Close",null).show();
            }).addOnFailureListener(e->{
                if(seatRequestContext10016(room10016,actor10016,generation10016))toast(msg(e));
            });
    }
    private void coHostListDialog(){
        if(!isOwner()||!cloudRoom||db==null||roomId==null||user==null){toast("Host only");return;}
        final String room10018=roomId,owner10018=user.getUid();
        final int generation10018=presenceGeneration967;
        db.collection("live_rooms").document(room10018).collection("roles")
            .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(snap->{
            if(!isActivePresence967(room10018,owner10018,generation10018)||!isOwner())return;
            List<DocumentSnapshot> docs=new ArrayList<>();List<String> rows=new ArrayList<>();
            for(DocumentSnapshot d:snap.getDocuments())if("cohost".equals(d.getString("role"))){
                docs.add(d);rows.add("👑 "+memberNameForUid(d.getId()));
            }
            if(rows.isEmpty()){toast("No co-hosts assigned");return;}
            new AlertDialog.Builder(this).setTitle("👑 Co-host list")
                .setItems(rows.toArray(new String[0]),(x,w)->{
                    if(!isActivePresence967(room10018,owner10018,generation10018)||!isOwner())return;
                    DocumentSnapshot role10018=docs.get(w);
                    final String target10018=role10018.getId();
                    final String name10018=memberNameForUid(target10018);
                    new AlertDialog.Builder(this).setTitle(name10018)
                        .setMessage("Remove co-host role from this member?")
                        .setPositiveButton("Remove",(a,b)->{
                            if(!isActivePresence967(room10018,owner10018,generation10018)||!isOwner()){
                                toast("Room or host permission changed. Reopen co-host list.");return;
                            }
                            final DocumentReference roleRef10018=role10018.getReference();
                            db.runTransaction(tx10018->{
                                DocumentSnapshot current10018=tx10018.get(roleRef10018);
                                if(!current10018.exists()||!"cohost".equals(current10018.getString("role")))
                                    throw new IllegalStateException("Co-host role changed. Refresh the list.");
                                tx10018.delete(roleRef10018);return true;
                            }).addOnSuccessListener(ok->{
                                if(isActivePresence967(room10018,owner10018,generation10018)&&isOwner())
                                    toast("Co-host removed: "+name10018);
                            }).addOnFailureListener(error->{
                                if(isActivePresence967(room10018,owner10018,generation10018))
                                    toast("Cannot remove co-host: "+msg(error));
                            });
                        }).setNegativeButton("Cancel",null).show();
                }).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->{
            if(isActivePresence967(room10018,owner10018,generation10018))toast(msg(e));
        });
    }
    private void banListDialog(){
        if(!isModerator()||!cloudRoom||db==null||roomId==null||user==null){toast("Host/co-host only");return;}
        final String banRoom10014=roomId,banActor10014=user.getUid();
        db.collection("live_rooms").document(banRoom10014).collection("room_bans").get().addOnSuccessListener(snap->{
            if(isFinishing()||isDestroyed()||!banRoom10014.equals(roomId)
                ||user==null||!banActor10014.equals(user.getUid())||!isModerator())return;
            List<DocumentSnapshot>docs=new ArrayList<>();List<String>rows=new ArrayList<>();
            for(DocumentSnapshot d:snap.getDocuments())if(Boolean.TRUE.equals(d.getBoolean("active"))){
                docs.add(d);String name=str(d,"name",memberNameForUid(d.getId()));String reason=str(d,"reason","No reason saved");com.google.firebase.Timestamp at=d.getTimestamp("createdAt");String when=at==null?"":new java.text.SimpleDateFormat("dd MMM • HH:mm",java.util.Locale.US).format(at.toDate());
                rows.add("🚫 "+name+"\n"+reason+(when.isEmpty()?"":" • "+when));
            }
            if(rows.isEmpty()){new AlertDialog.Builder(this).setTitle("🚫 Banned users").setMessage("No banned users in this room.").setPositiveButton("OK",null).show();return;}
            new AlertDialog.Builder(this).setTitle("🚫 Banned users • "+rows.size()).setItems(rows.toArray(new String[0]),(x,w)->{
                DocumentSnapshot d=docs.get(w);String name=str(d,"name",memberNameForUid(d.getId()));String reason=str(d,"reason","No reason saved");
                new AlertDialog.Builder(this).setTitle(name).setMessage("Reason: "+reason+"\n\nAllow this user to join again?").setPositiveButton("Unban",(a,b)->{
                    if(isFinishing()||isDestroyed()||!banRoom10014.equals(roomId)
                        ||user==null||!banActor10014.equals(user.getUid())||!isModerator()){
                        toast("Room or moderation role changed. Reopen ban list.");return;
                    }
                    final DocumentReference banRef10019=d.getReference();
                    db.runTransaction(tx10019->{
                        DocumentSnapshot current10019=tx10019.get(banRef10019);
                        if(!current10019.exists()||!Boolean.TRUE.equals(current10019.getBoolean("active"))
                            ||!java.util.Objects.equals(d.getTimestamp("createdAt"),current10019.getTimestamp("createdAt"))
                            ||!java.util.Objects.equals(d.getString("reason"),current10019.getString("reason")))
                            throw new IllegalStateException("Ban changed. Refresh banned users first.");
                        tx10019.delete(banRef10019);return true;
                    }).addOnSuccessListener(v->{
                        if(partyActivityCurrent10019(banRoom10014,banActor10014)&&isModerator()){
                            addEvent("unban",safeName()+" unbanned "+name);toast(name+" unbanned");
                        }
                    }).addOnFailureListener(e->{
                        if(partyActivityCurrent10019(banRoom10014,banActor10014))toast("Unban failed: "+msg(e));
                    });
                }).setNegativeButton("Cancel",null).show();
            }).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast(msg(e)));
    }
    private void showRows(String title,List<String> rows,String empty){
        if(rows==null||rows.isEmpty()){new AlertDialog.Builder(this).setTitle(title).setMessage(empty).setPositiveButton("OK",null).show();return;}
        new AlertDialog.Builder(this).setTitle(title).setItems(rows.toArray(new String[0]),null).setNegativeButton("Close",null).show();
    }

    private void followUser(String uid,String name){
        if(user==null||db==null){toast("Sign in to follow");return;}
        KingFollowLive9997.ensureFollowing(db,user,uid,safeName(),(following,error)->{
            if(isFinishing()||isDestroyed())return;
            if(error!=null)toast("Follow not saved: "+msg(error));
            else toast("Following "+name);
        });
    }
    private void reportUser(String uid,String name){String target=(uid==null||uid.isEmpty())?name:uid;CloudSync.submitReport(this,target,"Party room user report",(ok,m)->runOnUiThread(()->toast(m)));}
    private void hostToggleSeat(int no){
        if(!isModerator()||user==null||!cloudRoom||db==null||roomId==null){
            toast("Host/co-host in a live room only");return;
        }
        final String expectedUid10013=seatUids.get(no);
        if(expectedUid10013==null||expectedUid10013.isEmpty()){
            toast("The selected seat is empty");return;
        }
        if(expectedUid10013.equals(user.getUid())){
            // Only the microphone owner can activate their own actual RTC audio.
            toggleMic();return;
        }
        if(!Boolean.TRUE.equals(seatMics.get(no))){
            toast("Only the seated member can switch their mic on");return;
        }
        final String moderationRoom10013=roomId;
        DocumentReference seat10013=db.collection("live_rooms").document(moderationRoom10013)
            .collection("seats").document(String.valueOf(no));
        db.runTransaction(tx->{
            DocumentSnapshot current10013=tx.get(seat10013);
            if(!current10013.exists()||!expectedUid10013.equals(current10013.getString("uid")))
                throw new IllegalStateException("Seat changed; reopen member menu");
            if(!Boolean.TRUE.equals(current10013.getBoolean("micOn")))return false;
            tx.update(seat10013,"micOn",false);
            return true;
        }).addOnSuccessListener(changed10013->{
            if(moderationRoom10013.equals(roomId)&&Boolean.TRUE.equals(changed10013))
                addEvent("seat_mic", "Host muted seat "+no);
        }).addOnFailureListener(error10013->{
            if(moderationRoom10013.equals(roomId))toast("Mute failed: "+msg(error10013));
        });
    }
    private void hostRemoveSeat(int no){
        if(!isModerator()){toast("Host/co-host only");return;}
        if(cloudRoom&&db!=null){
            // All in-room seat menus must use the same occupant-checked transaction.
            // Never delete a seat blindly after an old menu has been left open.
            kickSeatV530(no,false);
        }else{
            localRemovedSeats.add(no);seatNames.remove(no);seatMics.remove(no);rebuildSeats();
        }
    }
    private void reactionDialog(){
        String[] r={"❤️ Love","😂 Haha","👏 Clap","🔥 Fire","🎉 Party","😍 Wow"};
        new AlertDialog.Builder(this).setTitle("Room reaction").setItems(r,(d,w)->{String emoji=r[w].substring(0,r[w].indexOf(' '));showReactionEffect(emoji);addEvent("reaction",safeName()+" reacted "+emoji);}).setNegativeButton("Close",null).show();
    }
    private void karaokeDialog(){
        String[] songs={"🔎 Search room song library","🎵 Romantic Hits","🎤 Bollywood Mix","🎶 Party Beats","💜 Love Songs","🔥 Trending Music","＋ Custom song title"};
        new AlertDialog.Builder(this).setTitle("Sing / Song request").setMessage(musicNames.isEmpty()?"Room playlist is empty • presets and custom requests are available.":"Room playlist: "+musicNames.size()+" song"+(musicNames.size()==1?"":"s"))
            .setItems(songs,(d,w)->{
                if(w==0){searchKtvSongs940();return;}
                if(w==songs.length-1){final EditText e=new EditText(this);e.setHint("Song title");new AlertDialog.Builder(this).setTitle("Request a song").setView(e).setPositiveButton("Request",(x,y)->requestSong(e.getText().toString().trim())).setNegativeButton("Cancel",null).show();}
                else requestSong(songs[w].substring(songs[w].indexOf(' ')+1));
            }).setNegativeButton("Close",null).show();
    }
    private void searchKtvSongs940(){
        final EditText e=new EditText(this);e.setHint("Search room playlist");e.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("🔎 KTV Song Search").setView(e).setNegativeButton("Cancel",null).setNeutralButton("Show all",(d,w)->showKtvSongResults940(""))
            .setPositiveButton("Search",(d,w)->showKtvSongResults940(e.getText().toString().trim())).show();
    }
    private void showKtvSongResults940(String query){
        String q=query==null?"":query.trim().toLowerCase(java.util.Locale.US);List<String> rows=new ArrayList<>();
        for(String song:musicNames){if(song==null||song.trim().isEmpty())continue;if(q.isEmpty()||song.toLowerCase(java.util.Locale.US).contains(q))rows.add(song);if(rows.size()>=50)break;}
        if(rows.isEmpty()){new AlertDialog.Builder(this).setTitle("KTV Song Search").setMessage(musicNames.isEmpty()?"No songs are in the room playlist yet.":"No song matched “"+query+"”.").setPositiveButton("Custom request",(d,w)->{final EditText x=new EditText(this);x.setHint("Song title");new AlertDialog.Builder(this).setTitle("Request a song").setView(x).setPositiveButton("Request",(a,b)->requestSong(x.getText().toString().trim())).setNegativeButton("Cancel",null).show();}).setNegativeButton("Close",null).show();return;}
        new AlertDialog.Builder(this).setTitle("🎤 Songs • "+rows.size()).setItems(rows.toArray(new String[0]),(d,w)->requestSong(rows.get(w))).setNeutralButton("Search again",(d,w)->searchKtvSongs940()).setNegativeButton("Close",null).show();
    }
    private void requestSong(String song){if(song==null||song.isEmpty())return;currentSong=song;addEvent("song",safeName()+" requested 🎵 "+song);toast("Song request added: "+song);}
    // Distinguish a real Firestore room dice session from the separate game
    // catalog. Do not treat a decorative room button as an active multiplayer game.
    private void openPartyGameChooser9915(){
        if(!cloudRoom||!memberSeen||db==null||user==null||roomId==null){
            toast("Join a live Firebase Party to start room games");return;
        }
        new AlertDialog.Builder(this).setTitle("🎮 KING Party Games")
            .setItems(new String[]{"🎲 Live Room Dice","♟ Live Room Chess","🔪 Flying Knife Arena","🎮 Multiplayer Games Catalog","🎯 Party Play Center"},
                (dialog,choice)->{
                    if(choice==0)openInRoomDice997();
                    else if(choice==1)openLiveChess9917();
                    else if(choice==2)openLiveKnife9923();
                    else if(choice==3)openRoomGames740();
                    else toolsPanel();
                })
            .setNegativeButton("Close",null).show();
    }

    private void openLiveKnife9923(){
        if(!cloudRoom||!memberSeen||db==null||user==null||roomId==null){
            toast("Join a live Party to play Flying Knife");return;
        }
        Intent knife9923=new Intent(this,KingKnifeArena9923Activity.class);
        knife9923.putExtra("roomId",roomId);
        startActivity(knife9923);
    }

    private void openLiveChess9917(){
        if(!cloudRoom||!memberSeen||db==null||user==null||roomId==null){
            toast("Join a live Firebase Party before Chess");return;
        }
        Intent chess9917=new Intent(this,KingLiveChessActivity.class);
        chess9917.putExtra("roomId",roomId);
        startActivity(chess9917);
    }

    private void openInRoomDice997(){
        if(!cloudRoom||!memberSeen||db==null||user==null||roomId==null){
            toast("Join a live Firebase Party before playing");return;
        }
        KingRoomDice997.open(this,db,user,roomId,safeName(),this::openRoomGames740);
    }

    private void openRoomGames740(){
        if(!cloudRoom||db==null||user==null||roomId==null||roomId.trim().isEmpty()){toast("Join a live Firebase Party room for multiplayer games");return;}
        Intent i=new Intent(this,RoomGameActivity.class);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);i.putExtra("displayName",safeName());startActivity(i);
    }

    private void openGames(){openMainTab(1);}
    private void renameRoomDialog(){if(!isOwner())return;final EditText e=new EditText(this);e.setText(roomName);new AlertDialog.Builder(this).setTitle("Rename room").setView(e).setPositiveButton("Save",(d,w)->{String n=e.getText().toString().trim();if(n.length()<2)return;roomName=n;if(cloudRoom)setRoomValue("name",n);renderParty();}).setNegativeButton("Cancel",null).show();}
    private void changeCategoryDialog(){if(!isOwner())return;String[] cats={"Chat","Date","Music","KTV","Radio","Sing","Game","PK","Pick Me","Family","Multi Video","Event","Birthday","Wedding"};new AlertDialog.Builder(this).setTitle("Room category").setItems(cats,(d,w)->{roomCategory=cats[w];if(cloudRoom)setRoomValue("category",roomCategory);renderParty();}).show();}
    private void changeThemeDialog(){if(!isOwner())return;String[] labels={"Classic Emerald","Ocean KTV","PK Red vs Blue","Rose Love","Game Emerald","Royal Purple","Neon Night","Galaxy Stage","Festival Glow","Ice Crystal"};String[] themes={"Classic","KTV","PK","Love","Game","Royal","Neon","Galaxy","Festival","Ice"};new AlertDialog.Builder(this).setTitle("Room theme").setItems(labels,(d,w)->{roomTheme=themes[w];if(cloudRoom)setRoomValue("theme",roomTheme);else prefs.edit().putString("theme_"+roomName,roomTheme).apply();renderParty();}).show();}
    private void changeSeatCount(){if(!isOwner())return;seatLayoutPanel();}

    private void toggleFollow(){
        if(user==null||ownerUid==null||db==null){toast("Firebase sign-in required to follow");return;}
        KingFollowLive9997.toggle(db,user,ownerUid,safeName(),(following,error)->{
            if(isFinishing()||isDestroyed())return;
            if(error!=null){toast("Follow not changed: "+msg(error));return;}
            if(followLabel!=null)followLabel.setText(Boolean.TRUE.equals(following)?"✓ Following":"＋ Follow");
        });
    }

    private boolean currentVisitDocument10026(DocumentSnapshot item){
        if(item==null||roomId==null||roomVisitStarted10026<=0)return false;
        com.google.firebase.Timestamp stamp=item.getTimestamp("createdAt");
        // Firestore SERVER timestamps are authoritative, not local cached rows.
        return stamp!=null&&stamp.toDate().getTime()>=roomVisitStarted10026-1500L;
    }

    // A server snapshot can report absence while a fast network reconnect is
    // restoring this session. Recheck after a short grace period and let the
    // member choose whether to rejoin; never eject on the first snapshot.
    private final android.os.Handler memberRecoveryHandler10077=
        new android.os.Handler(android.os.Looper.getMainLooper());
    private void verifyMissingMemberAndOfferRejoin10077(String expectedRoom,String expectedUid,
            int expectedVisit,int expectedBinding){
        if(memberRecoveryDialogOpen10077||db==null||expectedRoom==null||expectedUid==null)return;
        final String expectedSession=presenceSession967;
        memberRecoveryHandler10077.postDelayed(()->{
            if(!activeRoomCallback982(expectedRoom,expectedUid,expectedVisit,expectedBinding)
                ||!expectedSession.equals(presenceSession967)||memberRecoveryDialogOpen10077)return;
            DocumentReference roomRef=db.collection("live_rooms").document(expectedRoom);
            roomRef.collection("members").document(expectedUid)
                .get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(freshMember->{
                    if(!activeRoomCallback982(expectedRoom,expectedUid,expectedVisit,expectedBinding)
                        ||!expectedSession.equals(presenceSession967))return;
                    if(freshMember!=null&&freshMember.exists()
                        &&expectedUid.equals(freshMember.getString("uid")))return;
                    roomRef.collection("room_bans").document(expectedUid)
                        .get(com.google.firebase.firestore.Source.SERVER)
                        .addOnSuccessListener(ban->{
                            if(!activeRoomCallback982(expectedRoom,expectedUid,expectedVisit,expectedBinding)
                                ||!expectedSession.equals(presenceSession967))return;
                            if(ban!=null&&ban.exists()&&Boolean.TRUE.equals(ban.getBoolean("active"))){
                                toast("You are banned from this room");leaveRoom();return;
                            }
                            roomRef.get(com.google.firebase.firestore.Source.SERVER)
                                .addOnSuccessListener(liveRoom->{
                                    if(!activeRoomCallback982(expectedRoom,expectedUid,expectedVisit,expectedBinding)
                                        ||!expectedSession.equals(presenceSession967))return;
                                    if(liveRoom==null||!liveRoom.exists()
                                        ||Boolean.TRUE.equals(liveRoom.getBoolean("closed"))){
                                        toast("This Party Room is closed");leaveRoom();return;
                                    }
                                    memberRecoveryDialogOpen10077=true;
                                    AlertDialog dialog=new AlertDialog.Builder(this)
                                        .setTitle("Party connection interrupted")
                                        .setMessage("Your room membership could not be confirmed. Rejoin the room or leave for the Party list.")
                                        .setPositiveButton("Rejoin",(d,w)->{
                                            if(!activeRoomCallback982(expectedRoom,expectedUid,expectedVisit,expectedBinding)
                                                ||!expectedSession.equals(presenceSession967))return;
                                            roomRef.get(com.google.firebase.firestore.Source.SERVER)
                                                .addOnSuccessListener(latest->{
                                                    if(!activeRoomCallback982(expectedRoom,expectedUid,expectedVisit,expectedBinding)
                                                        ||!expectedSession.equals(presenceSession967))return;
                                                    if(latest==null||!latest.exists()
                                                        ||Boolean.TRUE.equals(latest.getBoolean("closed"))){
                                                        toast("This Party Room is no longer available");leaveRoom();return;
                                                    }
                                                    openRoomDoc891(latest);
                                                }).addOnFailureListener(error->toast("Could not verify the room. Check internet and retry."));
                                        })
                                        .setNegativeButton("Leave",(d,w)->{
                                            if(activeRoomCallback982(expectedRoom,expectedUid,expectedVisit,expectedBinding)
                                                &&expectedSession.equals(presenceSession967))leaveRoom();
                                        }).create();
                                    dialog.setOnDismissListener(d->memberRecoveryDialogOpen10077=false);
                                    dialog.show();
                                })
                                .addOnFailureListener(error->{
                                    if(activeRoomCallback982(expectedRoom,expectedUid,expectedVisit,expectedBinding))
                                        setOnlineState900(false,"Room check failed • reconnecting");
                                });
                        })
                        .addOnFailureListener(error->{
                            if(activeRoomCallback982(expectedRoom,expectedUid,expectedVisit,expectedBinding))
                                setOnlineState900(false,"Membership check failed • reconnecting");
                        });
                })
                .addOnFailureListener(error->{
                    if(activeRoomCallback982(expectedRoom,expectedUid,expectedVisit,expectedBinding))
                        setOnlineState900(false,"Membership check failed • reconnecting");
                });
        },1200L);
    }

    private void attachCloudRoom() {
        attachV530RealtimeHelpers();
        if(db==null||user==null||roomId==null||roomId.trim().isEmpty())return;
        final DocumentReference room=db.collection("live_rooms").document(roomId);
        final String expectedRoom982=room.getId(),expectedUid982=user.getUid();
        final int visit982=presenceGeneration967,binding982=roomListenerGeneration982;
        roomListener=room.addSnapshotListener((doc,e)->{
            if(!activeRoomCallback982(expectedRoom982,expectedUid982,visit982,binding982))return;
            if(e!=null||doc==null)return;
            if(doc.getMetadata().isFromCache()||doc.getMetadata().hasPendingWrites()){
                // Cached room settings cannot grant a locked-room, moderation, or mic policy transition.
                return;
            }
            if(!doc.exists()){toast("Room removed from server");leaveRoom();return;}
            if(Boolean.TRUE.equals(doc.getBoolean("closed"))){
                toast("Room closed • leaving");leaveRoom();return;
            }
            announcement=str(doc,"announcement",announcement);roomCategory=str(doc,"category",roomCategory);roomTheme=str(doc,"theme",roomTheme);Long ms=doc.getLong("maxSeats");if(ms!=null){maxSeats=(int)Math.max(8,Math.min(12,ms));}Long mm=doc.getLong("maxMembers");if(mm!=null)maxMembers930=(int)Math.max(10,Math.min(200,mm));hostSeatMode=Boolean.TRUE.equals(doc.getBoolean("hostSeatMode"));loadRoomPersonalSettings();rebuildSeats();roomLocked=Boolean.TRUE.equals(doc.getBoolean("locked"));muteAll=Boolean.TRUE.equals(doc.getBoolean("muteAll"));if(muteAll&&!isModerator()&&micOn){micOn=false;revokeMicConsent9956();if(mySeat>0)muteOwnedSeat9920("moderator-mute-all");refreshMicControl();}roomPrivate=Boolean.TRUE.equals(doc.getBoolean("isPrivate"));roomHasPassword=Boolean.TRUE.equals(doc.getBoolean("hasPassword"));roomEntranceMode9931=str(doc,"entranceMode","legacy");roomEntryEpoch9936=str(doc,"entryEpoch","");if(announcementLabel!=null)announcementLabel.setText((roomLocked?"🔒  ":"📢  ")+announcement);refreshRoomState();});
        roleListener=room.collection("roles").document(expectedUid982)
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(doc,e)->{
            if(!activeRoomCallback982(expectedRoom982,expectedUid982,visit982,binding982))return;
            if(e!=null||doc==null||doc.getMetadata().isFromCache()||doc.getMetadata().hasPendingWrites())return;
            boolean old=coHost;coHost=doc.exists()&&"cohost".equals(doc.getString("role"));refreshRoomState();
            if(old!=coHost&&!isOwner()&&page!=null)page.post(()->renderParty());
        });
        selfMemberListener=room.collection("members").document(expectedUid982).addSnapshotListener((doc,e)->{
            if(!activeRoomCallback982(expectedRoom982,expectedUid982,visit982,binding982))return;
            if(e!=null||doc==null)return;
            if(doc.exists())memberSeen=true;
            else if(memberSeen&&!isOwner()&&!doc.getMetadata().isFromCache())
                verifyMissingMemberAndOfferRejoin10077(expectedRoom982,expectedUid982,visit982,binding982);
        });
        roomBanListener=room.collection("room_bans").document(expectedUid982).addSnapshotListener((doc,e)->{
            if(!activeRoomCallback982(expectedRoom982,expectedUid982,visit982,binding982))return;
            if(e==null&&doc!=null&&doc.exists()&&!doc.getMetadata().isFromCache()
                &&Boolean.TRUE.equals(doc.getBoolean("active"))&&!isOwner()){toast("You are banned from this room");leaveRoom();}
        });
        seatLocksListener=room.collection("seat_locks").addSnapshotListener((snap,e)->{
            if(!activeRoomCallback982(expectedRoom982,expectedUid982,visit982,binding982)||e!=null||snap==null)return;
            lockedSeats.clear();lockedSeats940.clear();
            for(DocumentSnapshot d:snap.getDocuments()){
                if(Boolean.TRUE.equals(d.getBoolean("locked"))){
                    try{int seat968=Integer.parseInt(d.getId());lockedSeats.add(seat968);lockedSeats940.add(seat968);}
                    catch(Exception ignored){}
                }
            }
            rebuildSeats();
        });
        seatsListener=room.collection("seats").addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(snap,e)->{if(!activeRoomCallback982(expectedRoom982,expectedUid982,visit982,binding982)||e!=null||snap==null)return;
            // A cached seat cannot grant microphone transmission rights.
            if(snap.getMetadata().isFromCache()||snap.getMetadata().hasPendingWrites()){
                micOn=false;setInlineAudioMuted940(true);return;
            }
            seatNames.clear();seatUids.clear();seatMics.clear();mySeat=-1;for(DocumentSnapshot d:snap.getDocuments()){int no;try{no=Integer.parseInt(d.getId());}catch(Exception ex){continue;}seatNames.put(no,str(d,"name","Guest"));seatUids.put(no,d.getString("uid"));seatMics.put(no,Boolean.TRUE.equals(d.getBoolean("micOn")));if(user.getUid().equals(d.getString("uid"))){
                    mySeat=no;
                    boolean serverMic9956=Boolean.TRUE.equals(d.getBoolean("micOn"));
                    if(!serverMic9956)revokeMicConsent9956();
                    micOn=KingPartyMicConsent9956.canTransmit(localMicConsent9956,cloudRoom,
                        roomId,consentRoom9956,mySeat,consentSeat9956,
                        user.getUid(),d.getString("uid"),serverMic9956,muteAll,isModerator());
                }}
            if(mySeat<1){micOn=false;revokeMicConsent9956();}
            setInlineAudioMuted940(!canTransmitMic9956());rebuildSeats();refreshPeopleCounts();if(micLabel!=null){refreshMicControl();}});
        // Reuses seatLocksListener for both views; no duplicate Firestore subscription.
        membersListener=room.collection("members").addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(snap,e)->{if(!activeRoomCallback982(expectedRoom982,expectedUid982,visit982,binding982)||snap==null||e!=null)return;
            // Report verified live population, not an offline cached headcount.
            if(snap.getMetadata().isFromCache()||snap.getMetadata().hasPendingWrites())return;
            memberNames.clear();memberUids.clear();memberPhotos540.clear();memberVip720.clear();memberLevel720.clear();memberFrame730.clear();memberEffect730.clear();for(DocumentSnapshot m:snap.getDocuments()){String base=str(m,"name","User"),uid=m.getId();String n=base;if(memberUids.containsKey(n)&&uid!=null&&!uid.equals(memberUids.get(n))){String tail=uid.length()>4?uid.substring(uid.length()-4):uid;n=base+" · "+tail;}memberNames.add(n);memberUids.put(n,uid);if(uid!=null&&m.getString("photoUrl")!=null)memberPhotos540.put(uid,m.getString("photoUrl"));Long lv=m.getLong("level"),vip=m.getLong("vipLevel");if(uid!=null){memberLevel720.put(uid,lv==null?1:lv.intValue());memberVip720.put(uid,vip==null?0:vip.intValue());String fr=m.getString("equippedFrame"),ef=m.getString("entranceEffect");if(fr!=null)memberFrame730.put(uid,fr);if(ef!=null)memberEffect730.put(uid,ef);}}rebuildSeats();liveMemberCount=snap.size();refreshPeopleCounts();rebuildMemberStrip();});
        attachCloudMusicPlaylist(room);
        attachMusicSync(room);
        attachGameState(room);
        if(isModerator()){
            seatRequestsListener=room.collection("seat_requests").addSnapshotListener((snap,e)->{
                if(!activeRoomCallback982(expectedRoom982,expectedUid982,visit982,binding982))return;
                pendingSeatRequests=(e==null&&snap!=null)?snap.size():0;
                if(seatRequestLabel!=null)seatRequestLabel.setText("📥 Requests "+pendingSeatRequests);
                refreshRoomState();
            });
        }
        messagesListener=room.collection("messages").orderBy("createdAt",Query.Direction.ASCENDING).limitToLast(120).addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(snap,e)->{if(!activeRoomCallback982(expectedRoom982,expectedUid982,visit982,binding982)||e!=null||snap==null||chatBox==null)return;
            if(snap.getMetadata().isFromCache())return;
            chatBox.removeAllViews();for(DocumentSnapshot d:snap.getDocuments()){if(d.getMetadata().hasPendingWrites()||!currentVisitDocument10026(d))continue;String text=str(d,"text","");if("gift".equals(d.getString("type"))&&text.isEmpty()){Long q=d.getLong("giftQty"),v=d.getLong("giftValue");text=str(d,"senderName","User")+" sent "+str(d,"giftIcon","🎁")+" "+str(d,"giftName","Gift")+" to "+str(d,"targetName","User")+" 🎁 x"+(q==null?1:q)+" • 💎"+(v==null?0:v);}addChatRow(str(d,"senderName","User"),text);}});
        // Watch recipient-confirmed deliveries, not just Firestore write ACK.
        // A sender only sees receipts for Gifts their own account sent.
        if(giftReceiptListener9941!=null)giftReceiptListener9941.remove();
        giftReceiptInitial9941=false;seenGiftReceipts9941.clear();
        giftReceiptListener9941=room.collection("gift_receipts")
            .whereEqualTo("senderUid",expectedUid982).limit(60)
            .addSnapshotListener((receipts,err)->{
              if(!activeRoomCallback982(expectedRoom982,expectedUid982,visit982,binding982)
                 ||err!=null||receipts==null)return;
              if(receipts.getMetadata().isFromCache())return;
              if(!giftReceiptInitial9941){
                  for(DocumentSnapshot row:receipts.getDocuments())seenGiftReceipts9941.add(row.getId());
                  giftReceiptInitial9941=true;return;
              }
              for(DocumentSnapshot row:receipts.getDocuments()){
                  if(!seenGiftReceipts9941.add(row.getId()))continue;
                  if(expectedUid982.equals(row.getString("senderUid"))){
                      String name=str(row,"giftName","Gift");
                      toast("✅ Your "+name+" was received on the other Party phone");
                  }
              }
              if(seenGiftReceipts9941.size()>180){
                  seenGiftReceipts9941.clear();
                  for(DocumentSnapshot row:receipts.getDocuments())seenGiftReceipts9941.add(row.getId());
              }
            });
        eventsListener=room.collection("events").orderBy("createdAt",Query.Direction.ASCENDING).limitToLast(80)
          .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(snap,e)->{
            if(!activeRoomCallback982(expectedRoom982,expectedUid982,visit982,binding982)||e!=null||snap==null||feedBox==null)return;
            if(snap.getMetadata().isFromCache())return; // server must verify before Gift ID dedupe
            feedBox.removeAllViews();refreshSupporters(snap);
            for(DocumentSnapshot d:snap.getDocuments()){
                if(currentVisitDocument10026(d)&&!"live_emoji".equals(d.getString("type")))addFeed(str(d,"text","Room activity"));
                String id=d.getId();
                if(!eventSnapshotReady){
                    processedEventIds.add(id);
                    // Previous versions permanently skipped initial snapshots, so
                    // a gift sent while the phone was offline could never be received.
                    if("gift".equals(d.getString("type"))&&!snap.getMetadata().isFromCache())
                        displayConfirmedGift9942(room,d,true);
                    continue;
                }
                if(processedEventIds.contains(id))continue;
                processedEventIds.add(id);
                String type=d.getString("type");
                if("sync_test".equals(type)&&user!=null&&!user.getUid().equals(d.getString("actorUid"))){ackSyncTest920(room,d);}
                if("gift".equals(type)){
                    if(!snap.getMetadata().isFromCache())
                        displayConfirmedGift9942(room,d,false);
                }
                else if("join".equals(type)){Long lv=d.getLong("actorLevel"),vip=d.getLong("actorVip");showEntranceEffect730(str(d,"actorName","Guest"),lv==null?1:lv.intValue(),vip==null?0:vip.intValue(),str(d,"actorEffect",vip!=null&&vip>=7?"Galaxy Portal":"Welcome Sparkle"),str(d,"actorFrame",vip!=null&&vip>=7?"Galaxy Frame":"Minimal Frame"));}
                else if("reaction".equals(type)){String text=str(d,"text","");String emoji=text.isEmpty()?"✨":text.substring(text.length()-Math.min(2,text.length()));showReactionEffect(emoji);}
            }
            eventSnapshotReady=true;
        });
    }
    private void ackSyncTest920(DocumentReference room,DocumentSnapshot ping){
        String testId=ping.getString("testId");if(testId==null||testId.trim().isEmpty()||user==null)return;
        String safeId=testId.replaceAll("[^A-Za-z0-9_-]","");if(safeId.length()>32)safeId=safeId.substring(0,32);
        DocumentReference ack=room.collection("events").document("ack_"+safeId+"_"+user.getUid());
        final String finalId=testId;
        ack.get().addOnSuccessListener(existing->{if(existing.exists())return;Map<String,Object>m=new HashMap<>();m.put("actorUid",user.getUid());m.put("actorName",safeName());m.put("type","sync_ack");m.put("testId",finalId);m.put("text",safeName()+" acknowledged production sync "+finalId);m.put("createdAt",FieldValue.serverTimestamp());ack.set(m).addOnFailureListener(e->KingStability.nonFatal(this,"sync-ack",e));}).addOnFailureListener(e->KingStability.nonFatal(this,"sync-ack-read",e));
    }

    private void registerMember(){joinMemberThenOpen891();}
    // Leaving or switching a Firebase Room must also release its live mic seat.
    // Formerly only the member record was deleted, leaving a ghost occupied seat.
    // The session guard prevents an older phone from removing the newer session.
    private void unregisterMember(){
        stopHeartbeat900();
        if(user==null||db==null||!cloudRoom||roomId==null||roomId.trim().isEmpty())return;
        final String leavingRoom9919=roomId;
        final String leavingUid9919=user.getUid();
        final String leavingSession9919=presenceSession967;
        final String leavingDevice9919=deviceId891();
        final DocumentReference room9919=db.collection("live_rooms").document(leavingRoom9919);
        final DocumentReference member9919=room9919.collection("members").document(leavingUid9919);
        // Query only seats belonging to this account, rather than trusting a
        // potentially stale mySeat value from a previous Party screen.
        room9919.collection("seats").whereEqualTo("uid",leavingUid9919)
            .limit(16).get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(seatQuery9919->{
                final java.util.ArrayList<DocumentReference> refs9919=new java.util.ArrayList<>();
                for(DocumentSnapshot seat9919:seatQuery9919.getDocuments())
                    refs9919.add(seat9919.getReference());
                db.runTransaction(tx9919->{
                    // Read all documents first; Firestore transactions require it.
                    DocumentSnapshot joined9919=tx9919.get(member9919);
                    final java.util.ArrayList<DocumentSnapshot> held9919=new java.util.ArrayList<>();
                    for(DocumentReference ref9919:refs9919)held9919.add(tx9919.get(ref9919));
                    boolean ownsSession9919=joined9919.exists()
                        && leavingUid9919.equals(joined9919.getString("uid"))
                        && KingRoomPresence967.canRemove(
                            joined9919.getString("sessionId"),leavingSession9919,
                            joined9919.getString("deviceId"),leavingDevice9919);
                    if(!ownsSession9919)return 0;
                    int removed9919=0;
                    for(int i9919=0;i9919<refs9919.size();i9919++){
                        DocumentSnapshot seat9919=held9919.get(i9919);
                        if(seat9919.exists()&&leavingUid9919.equals(seat9919.getString("uid"))){
                            tx9919.delete(refs9919.get(i9919));removed9919++;
                        }
                    }
                    tx9919.delete(member9919);
                    return removed9919;
                }).addOnSuccessListener(removed9919->{
                    android.util.Log.i("KINGPlusParty","Room membership exit synced; mic seats released="+removed9919);
                }).addOnFailureListener(e9919->
                    KingStability.nonFatal(this,"room-exit-seat-transaction-9919",e9919));
            }).addOnFailureListener(e9919->{
                // The server seat QUERY can fail transiently (for example while
                // reconnecting). Read all bounded seat IDs in a transaction so
                // a successful fallback also removes this user's ghost mic seat.
                // Never delete another session's member/seat during a fast rejoin.
                final java.util.ArrayList<DocumentReference> backupRefs10027=new java.util.ArrayList<>();
                for(int seatNumber10027=1;seatNumber10027<=12;seatNumber10027++)
                    backupRefs10027.add(room9919.collection("seats")
                        .document(String.valueOf(seatNumber10027)));
                db.runTransaction(tx10027->{
                    DocumentSnapshot joined10027=tx10027.get(member9919);
                    final java.util.ArrayList<DocumentSnapshot> seatDocs10027=new java.util.ArrayList<>();
                    for(DocumentReference seatRef10027:backupRefs10027)
                        seatDocs10027.add(tx10027.get(seatRef10027));
                    if(!joined10027.exists()||!leavingUid9919.equals(joined10027.getString("uid"))
                        ||!KingRoomPresence967.canRemove(
                            joined10027.getString("sessionId"),leavingSession9919,
                            joined10027.getString("deviceId"),leavingDevice9919))return 0;
                    int released10027=0;
                    for(int seatIndex10027=0;seatIndex10027<backupRefs10027.size();seatIndex10027++){
                        DocumentSnapshot occupied10027=seatDocs10027.get(seatIndex10027);
                        if(occupied10027.exists()
                            &&leavingUid9919.equals(occupied10027.getString("uid"))){
                            tx10027.delete(backupRefs10027.get(seatIndex10027));released10027++;
                        }
                    }
                    tx10027.delete(member9919);
                    return released10027;
                }).addOnSuccessListener(released10027->
                    android.util.Log.i("KINGPlusParty","Fallback room exit; seats released="+released10027))
                  .addOnFailureListener(err10027->
                    KingStability.nonFatal(this,"room-exit-seat-fallback-10027",err10027));
            });
    }

    private void addEvent(String type,String text){if(cloudRoom&&user!=null&&db!=null&&roomId!=null){LevelSystem.Snapshot p720=LevelSystem.read(this);Map<String,Object>d=new HashMap<>();d.put("actorUid",user.getUid());d.put("actorName",safeName());d.put("actorLevel",p720.level);d.put("actorVip",0);d.put("actorFrame",KingCosmetics.frame(this));d.put("actorEffect",KingCosmetics.effect(this));d.put("type",type);d.put("text",text);d.put("createdAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).collection("events").add(d);}else addFeed(text);}

    private void voiceState10065(String message){if(voiceStatus10065!=null)voiceStatus10065.setText(message);}
    private void showVoiceConnection10065(){
        if(!cloudRoom||!memberSeen||partyShell940==null)return;
        if(BuildConfig.CLOUDFLARE_VOICE_ENABLED){
            new AlertDialog.Builder(this).setTitle("Cloudflare voice")
                .setMessage(voiceConferenceConnected10065?"Room voice connected":"Voice is waiting for a room microphone or server connection.")
                .setPositiveButton("Back to room",null)
                .setNeutralButton("Reconnect",(d,w)->{micOn=false;revokeMicConsent9956();stopInRoomVoice940(true);roomVoiceOptedIn957=true;soundOn=true;attachInRoomVoice940(partyShell940);})
                .setNegativeButton("Voice off",(d,w)->{if(soundOn)toggleSound();}).show();return;
        }
        if(voiceConnectionDialog10065!=null)return;
        roomVoiceOptedIn957=true;soundOn=true;
        if(inRoomVoiceView940==null)attachInRoomVoice940(partyShell940);
        final org.jitsi.meet.sdk.JitsiMeetView voice=inRoomVoiceView940;
        if(voice==null){toast("Voice service is unavailable. Try again.");return;}
        if(voice.getParent() instanceof android.view.ViewGroup)((android.view.ViewGroup)voice.getParent()).removeView(voice);
        voice.setAlpha(1f);voice.setClickable(true);voice.setFocusable(true);
        FrameLayout box=new FrameLayout(this);box.addView(voice,new FrameLayout.LayoutParams(-1,dp(380)));
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Voice connection")
            .setView(box).setPositiveButton("Back to room",null)
            .setNeutralButton("Reconnect",(d,w)->{micOn=false;revokeMicConsent9956();stopInRoomVoice940(true);attachInRoomVoice940(partyShell940);})
            .setNegativeButton("Voice off",(d,w)->{if(soundOn)toggleSound();}).create();
        voiceConnectionDialog10065=dialog;
        dialog.setOnDismissListener(d->{
            if(voiceConnectionDialog10065==dialog)voiceConnectionDialog10065=null;
            if(inRoomVoiceView940!=voice)return;
            if(voice.getParent() instanceof android.view.ViewGroup)((android.view.ViewGroup)voice.getParent()).removeView(voice);
            voice.setAlpha(.01f);voice.setClickable(false);voice.setFocusable(false);
            if(partyShell940!=null&&cloudRoom&&!isFinishing()&&!isDestroyed())partyShell940.addView(voice,new FrameLayout.LayoutParams(dp(2),dp(2)));
        });dialog.show();
    }
    private void attachInRoomVoice940(FrameLayout shell){
        if(!cloudRoom||!memberSeen||!roomVoiceOptedIn957||!soundOn||roomId==null||roomId.trim().isEmpty()||user==null||shell==null)return;
        if(BuildConfig.CLOUDFLARE_VOICE_ENABLED){
            if(cloudflareVoice10066!=null)return;
            stopInRoomVoice940(true);
            final String expectedRoom10066=roomId,expectedUid10066=user.getUid();
            final int expectedVisit10066=presenceGeneration967;
            cloudflareVoice10066=new KingCloudflareVoice10066(this,shell,roomId,user.getUid(),presenceSession967,(connected,message)->{
                if(!isActivePresence967(expectedRoom10066,expectedUid10066,expectedVisit10066))return;
                voiceConferenceConnected10065=connected;
                if(!connected){micOn=false;revokeMicConsent9956();if(mySeat>0)muteOwnedSeat9920("cloudflare-disconnected");}
                setVoicePresence900(connected);refreshMicControl();voiceState10065(message);
            });
            inRoomVoiceJoined940=true;voiceState10065("Cloudflare voice connecting…");
            cloudflareVoice10066.start();cloudflareVoice10066.microphone(canTransmitMic9956(),mySeat);return;
        }
        try{
            String slug=("KINGPlus-"+roomId).replaceAll("[^A-Za-z0-9_-]","");
            if(inRoomVoiceJoined940&&inRoomVoiceView940!=null&&slug.equals(inRoomVoiceSlug940)){
                try{if(inRoomVoiceView940.getParent() instanceof android.view.ViewGroup)((android.view.ViewGroup)inRoomVoiceView940.getParent()).removeView(inRoomVoiceView940);}catch(Throwable ignored){}
                FrameLayout.LayoutParams reusedVoiceLp940=new FrameLayout.LayoutParams(dp(2),dp(2));reusedVoiceLp940.gravity=Gravity.TOP|Gravity.LEFT;
                shell.addView(inRoomVoiceView940,reusedVoiceLp940);
                setInlineAudioMuted940(!(micOn&&mySeat>0));
                return;
            }
            stopInRoomVoice940(false);
            KingCrashWatch958.mark(this,"party-jitsi-init");
            org.jitsi.meet.sdk.JitsiMeet.instantiateReactNative(this);
            inRoomVoiceView940=new org.jitsi.meet.sdk.JitsiMeetView(this);
            inRoomVoiceView940.setAlpha(0.01f);
            inRoomVoiceView940.setClickable(false);
            inRoomVoiceView940.setFocusable(false);
            org.jitsi.meet.sdk.JitsiMeetUserInfo info940=new org.jitsi.meet.sdk.JitsiMeetUserInfo();
            info940.setDisplayName(safeName());
            org.jitsi.meet.sdk.JitsiMeetConferenceOptions options940=new org.jitsi.meet.sdk.JitsiMeetConferenceOptions.Builder()
                .setServerURL(new java.net.URL("https://meet.jit.si"))
                .setRoom(slug)
                .setSubject(roomName==null||roomName.trim().isEmpty()?"KING Plus Party":roomName)
                .setAudioMuted(true)
                .setVideoMuted(true)
                .setConfigOverride("startLowBandwidthMode",true)
                .setUserInfo(info940)
                .setFeatureFlag("welcomepage.enabled",false)
                .setFeatureFlag("prejoinpage.enabled",false)
                .setFeatureFlag("toolbox.enabled",false)
                .setFeatureFlag("filmstrip.enabled",false)
                .setFeatureFlag("invite.enabled",false)
                .setFeatureFlag("chat.enabled",false)
                .setFeatureFlag("pip.enabled",false)
                .setFeatureFlag("call-integration.enabled",false)
                .setConfigOverride("disableSelfView",true)
                .setConfigOverride("disableReactions",true)
                .build();
            FrameLayout.LayoutParams voiceLp940=new FrameLayout.LayoutParams(dp(2),dp(2));
            voiceLp940.gravity=Gravity.TOP|Gravity.LEFT;
            shell.addView(inRoomVoiceView940,voiceLp940);
            final String voiceRoom10065=roomId,voiceUid10065=user.getUid();
            final int voiceVisit10065=presenceGeneration967;
            inRoomVoiceSlug940=slug;inRoomVoiceJoined940=true;voiceConferenceConnected10065=false;
            voiceMonitor10065=new KingPartyVoiceMonitor10065(this,"https://meet.jit.si/"+slug,(connected,message)->{
                if(!isActivePresence967(voiceRoom10065,voiceUid10065,voiceVisit10065))return;
                voiceConferenceConnected10065=connected;
                if(!connected){micOn=false;revokeMicConsent9956();if(mySeat>0)muteOwnedSeat9920("voice-disconnected");}
                setVoicePresence900(connected);setInlineAudioMuted940(!canTransmitMic9956());
                refreshMicControl();voiceState10065(message);
            });
            voiceState10065("Voice connecting…");
            inRoomVoiceView940.join(options940);
            setInlineAudioMuted940(!micOn);
        }catch(Throwable voiceError940){
            if(voiceMonitor10065!=null){voiceMonitor10065.close();voiceMonitor10065=null;}
            voiceConferenceConnected10065=false;voiceState10065("Voice unavailable • tap to retry");
            inRoomVoiceJoined940=false;
            try{if(inRoomVoiceView940!=null)inRoomVoiceView940.dispose();}catch(Throwable ignored){}
            inRoomVoiceView940=null;
            android.util.Log.e("KINGPlusParty","Inline room voice failed",voiceError940);
        }
    }
    private void setInlineAudioMuted940(boolean muted){
        if(!inRoomVoiceJoined940)return;
        if(cloudflareVoice10066!=null){cloudflareVoice10066.microphone(!muted&&canTransmitMic9956(),mySeat);return;}
        // Server micOn alone is NEVER microphone consent.
        boolean actualMuted9956=muted||!voiceConferenceConnected10065||!canTransmitMic9956();
        try{
            Intent voiceIntent940=org.jitsi.meet.sdk.BroadcastIntentHelper.buildSetAudioMutedIntent(actualMuted9956);
            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(voiceIntent940);
        }catch(Throwable ignored){}
    }
    private void stopInRoomVoice940(boolean hangup){
        if(cloudflareVoice10066!=null){cloudflareVoice10066.close();cloudflareVoice10066=null;}
        if(voiceMonitor10065!=null){voiceMonitor10065.close();voiceMonitor10065=null;}
        voiceConferenceConnected10065=false;setVoicePresence900(false);voiceState10065("Voice off • tap to connect");
        if(voiceConnectionDialog10065!=null){voiceConnectionDialog10065.setOnDismissListener(null);voiceConnectionDialog10065.dismiss();voiceConnectionDialog10065=null;}

        if(hangup&&inRoomVoiceJoined940&&inRoomVoiceView940!=null){
            try{
                Intent hangup940=org.jitsi.meet.sdk.BroadcastIntentHelper.buildHangUpIntent();
                androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(hangup940);
            }catch(Throwable ignored){}
        }
        try{if(inRoomVoiceView940!=null)inRoomVoiceView940.dispose();}catch(Throwable ignored){}
        try{if(inRoomVoiceView940!=null&&inRoomVoiceView940.getParent() instanceof android.view.ViewGroup)((android.view.ViewGroup)inRoomVoiceView940.getParent()).removeView(inRoomVoiceView940);}catch(Throwable ignored){}
        inRoomVoiceView940=null;
        inRoomVoiceJoined940=false;
        inRoomVoiceSlug940="";
    }

    private int firstFreeMicSeat940(){
        for(int n=1;n<=maxSeats;n++)if(!seatUids.containsKey(n))return n;
        return -1;
    }
    private void requestMicSeat940(){
        if(!cloudRoom||db==null||user==null||roomId==null||!memberSeen){
            toast("Join a live Party room first");return;
        }
        if(muteAll&&!isModerator()){toast("Host muted room microphones");return;}
        final int seatNo=firstFreeMicSeat940();
        if(seatNo<1){toast("All Mic seats are occupied");return;}
        requestSeat(seatNo);
    }
    // All mic-request entry points share one safety-checked room-bound queue.
    // The legacy menu used blind document.delete() and could silently assign
    // an entirely different seat from the one the member requested.
    private void openMicRequests940(){
        if(!cloudRoom||db==null||user==null||!memberSeen||!canModerateSeatsV530()){
            toast("Join this Party as Host/co-host to review Mic requests");return;
        }
        allSeatRequestsDialog();
    }
    private void showSeatLockMenu940(int seatNo){
        if(!isModerator()||!cloudRoom||db==null||roomId==null||user==null)return;
        final String room10019=roomId,actor10019=user.getUid();
        final int generation10019=presenceGeneration967;
        boolean locked=lockedSeats940.contains(seatNo);
        new AlertDialog.Builder(this).setTitle("Mic seat "+seatNo)
            .setItems(new String[]{locked?"🔓 Unlock seat":"🔒 Lock seat","🎙 Take seat"},(d,w)->{
                if(!isActivePresence967(room10019,actor10019,generation10019)||!isModerator()){
                    toast("Room changed. Reopen Seat Lock menu.");return;
                }
                if(w==0)setSeatLock10019(room10019,actor10019,generation10019,seatNo,!locked);
                else takeModeratorSeat940(seatNo);
            }).setNegativeButton("Cancel",null).show();
    }
    private void setSeatLock940(int seatNo,boolean locked){
        if(!isModerator()||!cloudRoom||db==null||roomId==null||user==null)return;
        setSeatLock10019(roomId,user.getUid(),presenceGeneration967,seatNo,locked);
    }
    private void setSeatLock10019(String room10019,String actor10019,int generation10019,
                                 int seatNo,boolean locked){
        if(!isActivePresence967(room10019,actor10019,generation10019)||!isModerator()
            ||seatNo<1||seatNo>maxSeats){toast("Room or moderation permissions changed");return;}
        final DocumentReference ref10019=db.collection("live_rooms").document(room10019)
            .collection("seat_locks").document(String.valueOf(seatNo));
        db.runTransaction(tx10019->{
            DocumentSnapshot live10019=tx10019.get(ref10019);
            boolean isLocked10019=live10019.exists()&&Boolean.TRUE.equals(live10019.getBoolean("locked"));
            if(isLocked10019==locked)
                throw new IllegalStateException("Seat lock already changed. Reopen the menu.");
            if(locked){
                Map<String,Object> value10019=new HashMap<>();
                value10019.put("locked",true);value10019.put("byUid",actor10019);
                value10019.put("updatedAt",FieldValue.serverTimestamp());
                tx10019.set(ref10019,value10019);
            }else tx10019.delete(ref10019);
            return true;
        }).addOnSuccessListener(done->{
            if(isActivePresence967(room10019,actor10019,generation10019)&&isModerator()){
                addEvent(locked?"seat_lock":"seat_unlock",safeName()+
                    (locked?" locked mic seat ":" unlocked mic seat ")+seatNo);
                toast("Seat "+seatNo+(locked?" locked":" unlocked"));
            }
        }).addOnFailureListener(error->{
            if(partyActivityCurrent10019(room10019,actor10019))
                toast("Seat lock was not changed: "+msg(error));
        });
    }
    private void takeModeratorSeat940(int seatNo){
        if(!canModerateSeatsV530()||db==null||roomId==null||user==null)return;
        cloudSeatAction(seatNo);
    }

    private boolean isOwner(){return cloudRoom?user!=null&&ownerUid!=null&&ownerUid.equals(user.getUid()):displayName.equals(ownerName);}
    private boolean isModerator(){return isOwner() || (cloudRoom && coHost);}
    private String safeName(){if(user!=null){String n=user.getDisplayName();if(n!=null&&!n.trim().isEmpty())return n.trim();String p=user.getPhoneNumber();if(p!=null&&p.length()>=4)return "KING "+p.substring(p.length()-4);}return displayName==null||displayName.trim().isEmpty()?"KING User":displayName;}
    private String shortId(){return shortId(roomId);}
    private String shortId(String id){if(id==null)return "000000";return String.valueOf(Math.abs(id.hashCode()%900000)+100000);}
    private String str(DocumentSnapshot d,String key,String fallback){Object raw=d.get(key);String v=raw instanceof String?(String)raw:null;return v==null||v.trim().isEmpty()?fallback:v;}
    private String msg(Exception e){return e==null||e.getLocalizedMessage()==null?"unknown error":e.getLocalizedMessage();}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}

    private void confirmLeave940(){
        if(roomId==null){renderLobby("Hot");return;}
        new AlertDialog.Builder(this).setTitle("Leave Party?")
            .setMessage("You can stay, leave to Party lobby, or discover another live room.")
            .setPositiveButton("Leave",(d,w)->leaveRoom())
            .setNeutralButton("Find another",(d,w)->{leaveRoom();new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(()->showLobbySearch("Hot"),120L);})
            .setNegativeButton("Stay",null).show();
    }
    private void leaveRoom(){
        clearVoiceRejoin9957();roomVoiceOptedIn957=false;
        clearPartyVisuals995(); // Cancel floating emojis and stale gift banners immediately.
        if(chatBox!=null)chatBox.removeAllViews();
        if(feedBox!=null)feedBox.removeAllViews();
        partyShell940=null;stopInRoomVoice940(true);renderLobby("Hot");
    }
    private void addBottomNav(LinearLayout root,int selected){
        LinearLayout nav=new LinearLayout(this);
        nav.setGravity(Gravity.CENTER_VERTICAL);
        nav.setPadding(dp(5),dp(5),dp(5),dp(5));
        nav.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{0xff3a1859,0xff110920}));
        String[] icons={"⌂","♟","◇","✉","●"};
        String[] names={"Party","Games","Discover","Messages","Me"};
        for(int i=0;i<5;i++){
            final int tab=i; boolean active=i==selected;
            LinearLayout item=new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);item.setGravity(Gravity.CENTER);
            if(active){
                GradientDrawable bgNav=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                    new int[]{0xffb83b9f,0xff6226a5});
                bgNav.setCornerRadius(dp(16));bgNav.setStroke(dp(1),0xb5ffdc8b);
                item.setBackground(bgNav);
            }
            TextView icon=tv(icons[i],24,active?0xffffdf80:0xffcab9dd,true);
            icon.setGravity(Gravity.CENTER);
            item.addView(icon,new LinearLayout.LayoutParams(-1,dp(30)));
            TextView label=tv(names[i],11,active?Color.WHITE:0xffc7b9d8,true);
            label.setGravity(Gravity.CENTER);
            item.addView(label,new LinearLayout.LayoutParams(-1,dp(20)));
            item.setContentDescription(names[i]+(active?" selected":""));
            item.setOnClickListener(v->{
                if(tab==0){if(roomId!=null)confirmLeave940();else renderLobby("Hot");}
                else {
                    // Single navigation at a time: rapid taps must never race a second Leave.
                    if(partyNavigationPending10033)return;
                    partyNavigationPending10033=true;
                    for(int k10033=0;k10033<nav.getChildCount();k10033++)
                        nav.getChildAt(k10033).setEnabled(false);
                    // This releases mic, membership, and seats before leaving Party.
                    if(roomId!=null)leaveRoom();
                    KingNav.openRoot(this,tab);
                    finish();
                }
            });
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(58),1);
            lp.setMargins(dp(2),0,dp(2),0);nav.addView(item,lp);
        }
        root.addView(nav,new LinearLayout.LayoutParams(-1,dp(68)));
    }

    private void reducePartyMemory969(int level969){
        if(!KingPartyMemory969.trimVisuals(level969))return;
        photoCache540.evictAll();
        // Visual effects are expendable; Mic/Seat and Firebase membership remain intact.
        clearPartyVisuals995();
        if(KingPartyMemory969.mayPauseIdleVoice(level969,micOn,inRoomVoiceJoined940)){
            // Native Jitsi can retain substantially more memory than Java heap.
            // Do not interrupt actively transmitting microphones.
            roomVoiceOptedIn957=false;
            stopInRoomVoice940(true);
            KingCrashWatch958.mark(this,"party-memory-idle-voice-paused");
        }
    }
    @Override public void onTrimMemory(int level){
        super.onTrimMemory(level);
        reducePartyMemory969(level);
    }
    @Override public void onLowMemory(){
        super.onLowMemory();
        reducePartyMemory969(KingPartyMemory969.RUNNING_CRITICAL);
    }

    @Override protected void onStop(){
        // Explicitly finishing a Party Activity must free its membership before
        // Android eventually invokes onDestroy (which can be delayed or skipped).
        if(isFinishing()){
            // A finishing Activity must not retain microphone consent or a pending
            // reconnect, even when the room was already cleared by a callback.
            clearVoiceRejoin9957();
            revokeMicConsent9956();
            stopPartyJoinTimeout977();
            ++partyReconnectGeneration979;
            clearPartyReconnect979();
            ++roomJoinGeneration966;++presenceGeneration967;
            stopInRoomVoice940(true);
            if(roomId!=null){clearListeners();unregisterMember();}
            roomId=null;cloudRoom=false;memberSeen=false;
        }
        stopSavedRooms9990();
        partyVisualForeground995=false;
        pauseVoiceRejoin9957();
        pauseDisplayedGiftReceipts9955();
        KingGiftSound993.stopRoomAudio();
        clearPartyVisuals995();
        // On background, unfinished Gift bursts were not fully seen.
        // Unmark their IDs so onResume SERVER replay can show/ACK them.
        for(KingGiftDeliveryQueue9944.Entry<PendingGift9944> item9955:pendingGiftEvents9944.clear())
            shownGiftIds9942.remove(item9955.id);
        for(String id9955:inFlightGiftIds9944)shownGiftIds9942.remove(id9955);
        inFlightGiftIds9944.clear();
        if(inRoomVoiceJoined940&&inRoomVoiceView940!=null)try{
            org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostPause(this);
        }catch(Throwable e){KingStability.nonFatal(this,"party-jitsi-pause",e);}
        super.onStop();
    }
    @Override public void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);handlePartyIntent9989(intent);try{org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onNewIntent(intent);}catch(Throwable e){KingStability.nonFatal(this,"party-jitsi-intent",e);}}
    @Override public void requestPermissions(String[] permissions,int requestCode,com.facebook.react.modules.core.PermissionListener listener){try{org.jitsi.meet.sdk.JitsiMeetActivityDelegate.requestPermissions(this,permissions,requestCode,listener);}catch(Throwable e){KingStability.nonFatal(this,"party-jitsi-permission-request",e);}}
    @android.annotation.SuppressLint("MissingSuperCall")
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){super.onRequestPermissionsResult(requestCode,permissions,grantResults);try{org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onRequestPermissionsResult(requestCode,permissions,grantResults);}catch(Throwable e){KingStability.nonFatal(this,"party-jitsi-permission-result",e);}}
    @android.annotation.SuppressLint("MissingSuperCall")
    @Override public void onBackPressed(){if(roomId!=null){confirmLeave940();return;}if(isTaskRoot())KingNav.confirmExit(this);else finish();}
    @Override protected void onDestroy(){if(partyAuth10065!=null){FirebaseAuth.getInstance().removeAuthStateListener(partyAuth10065);partyAuth10065=null;}clearVoiceRejoin9957();revokeMicConsent9956();stopPartyJoinTimeout977();++partyReconnectGeneration979;clearPartyReconnect979();++roomJoinGeneration966;++presenceGeneration967;roomImageExecutor964.shutdownNow();photoCache540.evictAll();KingNetwork.unwatch(this,partyNetworkCallback940);partyNetworkCallback940=null;stopInRoomVoice940(true);unregisterMember();clearListeners();clearDisplayedGiftReceipts9955();try{if(roomMusicPlayer!=null){roomMusicPlayer.release();roomMusicPlayer=null;}}catch(Exception ignored){}try{org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostDestroy(this);}catch(Throwable e){KingStability.nonFatal(this,"party-jitsi-destroy",e);}super.onDestroy();}
}
