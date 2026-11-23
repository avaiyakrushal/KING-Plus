Warning: truncated output (original token count: 165486)
Total output lines: 8388

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
    private boolean roomVoicePolicyVerified;
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
    private boolean cloudflareVoiceReady10066;
    private boolean cloudflareVoiceConfigLoading10066;
    private String cloudflareVoiceConfigMessage10066="";
    private String cloudflareVoiceConfigSession10066="";
    private long cloudflareVoiceConfigCheckedAt10066;
    private int cloudflareVoiceConfigGeneration10066;
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
        // Accept Home's room-search handoff so search results are applied on first lobby render.
        lobbyFilter = safe(getIntent().getStringExtra("lobbySearch"), "");
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
        // Home Create Room opens the actual creation screen, not just the lobby.
        // Direct room links retain priority and must never trigger creation.
        if(getIntent().getBooleanExtra("openCreateRoom",false)
                && getIntent().getStringExtra("directRoomId")==null){
            getIntent().removeExtra("openCreateRoom");
            createRoomDialog();
        }
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
        try{…149486 tokens truncated…nal java.util.ArrayList<DocumentReference> refs9919=new java.util.ArrayList<>();
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
    private void checkCloudflareVoiceConfig10066(String expectedRoom,String expectedUid,
            int expectedVisit,int expectedBinding,boolean force){
        String sessionKey=expectedRoom+"/"+presenceSession967;
        long now=System.currentTimeMillis();
        if(cloudflareVoiceConfigLoading10066)return;
        long cacheMillis=force?5_000L:30_000L;
        if(sessionKey.equals(cloudflareVoiceConfigSession10066)
                &&now-cloudflareVoiceConfigCheckedAt10066<cacheMillis)return;
        cloudflareVoiceConfigSession10066=sessionKey;
        cloudflareVoiceConfigLoading10066=true;
        int request=++cloudflareVoiceConfigGeneration10066;
        if((roomPrivate||roomHasPassword)&&!cloudflareVoiceReady10066)
            voiceState10065("Checking private room voice service…");
        Map<String,Object> data=new HashMap<>();data.put("action","config");
        com.google.firebase.functions.FirebaseFunctions.getInstance()
            .getHttpsCallable("partyVoice").call(data)
            .addOnSuccessListener(result->{
                if(request!=cloudflareVoiceConfigGeneration10066||
                    !activeRoomCallback982(expectedRoom,expectedUid,expectedVisit,expectedBinding))return;
                cloudflareVoiceConfigLoading10066=false;
                cloudflareVoiceConfigCheckedAt10066=System.currentTimeMillis();
                Object raw=result.getData();
                Map<?,?> response=raw instanceof Map?(Map<?,?>)raw:java.util.Collections.emptyMap();
                cloudflareVoiceReady10066=Boolean.TRUE.equals(response.get("enabled"));
                Object message=response.get("message");
                cloudflareVoiceConfigMessage10066=message instanceof String
                    ?safe((String)message,"Voice service is not ready"):"Voice service is not ready";
                if(!cloudflareVoiceReady10066&&cloudflareVoice10066!=null){
                    micOn=false;revokeMicConsent9956();clearVoiceRejoin9957();
                    if(mySeat>0)muteOwnedSeat9920("voice-config-disabled");
                    stopInRoomVoice940(true);
                }
                refreshMicControl();
                if(roomPrivate||roomHasPassword)
                    voiceState10065(cloudflareVoiceReady10066
                        ?"Private room voice ready • tap to connect":cloudflareVoiceConfigMessage10066);
            })
            .addOnFailureListener(error->{
                if(request!=cloudflareVoiceConfigGeneration10066||
                    !activeRoomCallback982(expectedRoom,expectedUid,expectedVisit,expectedBinding))return;
                cloudflareVoiceConfigLoading10066=false;
                cloudflareVoiceConfigCheckedAt10066=System.currentTimeMillis();
                cloudflareVoiceReady10066=false;
                cloudflareVoiceConfigMessage10066="Could not check voice server setup. Tap retry after reconnecting.";
                refreshMicControl();
                if(roomPrivate||roomHasPassword)voiceState10065(cloudflareVoiceConfigMessage10066);
            });
    }
    private boolean canConnectPartyVoice(){
        return roomVoicePolicyVerified&&(cloudflareVoiceReady10066
            ||(!roomPrivate&&!roomHasPassword
                &&("free".equals(roomEntranceMode9931)||"legacy".equals(roomEntranceMode9931))));
    }

    private void showPartyVoiceUnavailable(){
        String message=!roomVoicePolicyVerified?"Waiting for room settings • tap to retry"
            :cloudflareVoiceConfigLoading10066?"Checking private room voice service…"
            :!cloudflareVoiceConfigMessage10066.isEmpty()?cloudflareVoiceConfigMessage10066
            :"Private voice requires the configured voice service";
        voiceState10065(message);
        toast(message);
    }

    private void showVoiceConnection10065(){
        if(!cloudRoom||!memberSeen||partyShell940==null)return;
        if(!canConnectPartyVoice()){
            if(roomVoicePolicyVerified&&(roomPrivate||roomHasPassword)&&user!=null)
                checkCloudflareVoiceConfig10066(roomId,user.getUid(),presenceGeneration967,
                    roomListenerGeneration982,true);
            showPartyVoiceUnavailable();
            return;
        }
        if(cloudflareVoiceReady10066){
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
        if(!canConnectPartyVoice()){
            roomVoiceOptedIn957=false;
            soundOn=false;
            showPartyVoiceUnavailable();
            return;
        }
        if(cloudflareVoiceReady10066){
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
