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

public class PartyActivity extends Activity {
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
    private String pendingCreateCategory = "Hot";
    private int pendingCreateSeats = 8;
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
    private final Map<Integer,View> seatViews560=new HashMap<>();
    private final Map<String,View> activeReactions560=new HashMap<>();
    private TextView viewerLabel;
    private TextView announcementLabel;
    private TextView micLabel;
    private TextView followLabel;
    private EditText composerBox;
    private TextView reactionBanner;
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
    private TextView supporterLabel;
    private final Set<String> processedEventIds = new HashSet<>();
    private boolean eventSnapshotReady;
    private String giftTargetUid = "";
    private String giftTargetName = "";
    private String giftCategory = "Activity";
    private int giftQuantity = 1;
    private int giftSelectedIndex = 0;
    private final String[] giftNames = {"Gold Rose","Gold Teapot","Gold Mask","Luxury Set","Super Car","Jeweled Box","Lionheart Glory","Super Star","Love Heart","Flying Crown","Royal Castle","Firework","Lucky Fish","Couple Ring","Magic Star","Parcel Box","Crystal Swan","Galaxy Ship","Royal Throne","Phoenix","Diamond Rain","Moon Palace","VIP Crown","King Dragon"};
    private final String[] giftIcons = {"🌹","🫖","🎭","👜","🏎️","🎁","🦁","⭐","💗","👑","🏰","🎆","🐠","💍","🌟","📦","🦢","🚀","🪑","🔥","💎","🌙","♛","🐉"};
    private final int[] giftCosts = {200,3000,3000,10000,13200,13200,19800,59800,50,1000,5000,10000,800,2400,5200,999,7500,22000,36000,48000,68000,88000,120000,168000};
    private final String[] giftCategories = {"Activity","Activity","Activity","Activity","Classic","Classic","Fame","Fame","Relationship","Flying","Privilege","Flying","Filters","Relationship","Privilege","Parcel","Classic","Flying","Privilege","Fame","Privilege","Flying","Privilege","Fame"};
    private final int[] giftVipRequired = {0,0,0,0,0,0,1,2,0,0,2,0,0,0,1,0,2,3,4,5,6,7,8,10};
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
    private final android.util.LruCache<String,Bitmap> photoCache540 = new android.util.LruCache<>(24);
    private final Map<Integer,String> seatNames = new HashMap<>();
    private final Map<Integer,String> seatUids = new HashMap<>();
    private final Map<Integer,Boolean> seatMics = new HashMap<>();
    private FrameLayout liveEmojiStageV530;
    private ListenerRegistration liveEmojiListenerV530;
    private ListenerRegistration seatInviteListenerV530;
    private ListenerRegistration roleListenerV530;
    private boolean roomModeratorV530 = false;
    private boolean seatInviteDialogOpenV530 = false;
    private String lastSeatInviteKeyV530 = "";
    private final Set<String> seenLiveEmojiEventsV530 = new HashSet<>();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        try { db = FirebaseFirestore.getInstance(); } catch (Exception ignored) { db = null; }
        try { storage = FirebaseStorage.getInstance(); } catch (Exception ignored) { storage = null; }
        try { user = FirebaseAuth.getInstance().getCurrentUser(); } catch (Exception ignored) { user = null; }
        prefs = getSharedPreferences("king_party", MODE_PRIVATE);
        loadMusicPlaylist();
        displayName = getIntent().getStringExtra("displayName");
        if (displayName == null || displayName.trim().isEmpty()) displayName = safeName();
        localCoins = prefs.getInt("coins", 2500);
        String directRoomId=getIntent().getStringExtra("directRoomId");
        if(directRoomId!=null&&!directRoomId.trim().isEmpty()&&db!=null&&user!=null){
            String directName=getIntent().getStringExtra("directRoomName");
            String directOwnerUid=getIntent().getStringExtra("directOwnerUid");
            String directOwnerName=getIntent().getStringExtra("directOwnerName");
            boolean directPrivate=getIntent().getBooleanExtra("directPrivate",false);
            boolean directPassword=getIntent().getBooleanExtra("directPassword",false);
            openCloudRoom(directRoomId,safe(directName,"Live Party"),directOwnerUid,safe(directOwnerName,"KING Host"),directPrivate,directPassword);
        } else renderLobby("Hot");
    }

    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
    private String safe(String s,String fallback){ return s==null||s.trim().isEmpty()?fallback:s.trim(); }
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
        if (action != null) v.setOnClickListener(x -> runPartyAction(text, action)); return v;
    }
    private void runPartyAction(String label, Runnable action){
        if(action==null || isFinishing() || isDestroyed()) return;
        try{ action.run(); }catch(Exception e){
            android.util.Log.e("KINGPlusParty","Party action failed: "+label,e);
            if(!isFinishing()&&!isDestroyed()) toast("Action failed safely • "+msg(e));
        }
    }
    private boolean partyUiDead(){ return isFinishing() || isDestroyed(); }
    private void setSafeContentView(View root) {
        setContentView(root);
        final int l=root.getPaddingLeft(), t=root.getPaddingTop(), r=root.getPaddingRight(), b=root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root,(v,insets)->{
            Insets bars=insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(l,t+bars.top,r,b+bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }
    private void clearListeners() {
        for(View reaction:activeReactions560.values()){reaction.animate().cancel();if(reaction.getParent() instanceof android.view.ViewGroup)((android.view.ViewGroup)reaction.getParent()).removeView(reaction);}activeReactions560.clear();
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

    private void renderLobby(String selected) {
        clearListeners(); unregisterMember(); roomId = null; cloudRoom = false;
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(0xfffbfafc);

        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(12),dp(4),dp(8),0);
        TextView brand=tv("KING Plus",25,0xff171717,true);head.addView(brand,new LinearLayout.LayoutParams(0,dp(54),1));
        TextView searchIcon=tv("⌕",28,0xff222222,false);searchIcon.setGravity(Gravity.CENTER);searchIcon.setOnClickListener(v->showLobbySearch(selected));head.addView(searchIcon,new LinearLayout.LayoutParams(dp(46),dp(50)));
        TextView hot=tv("🔥",23,0xff222222,false);hot.setGravity(Gravity.CENTER);hot.setContentDescription("Create Party");hot.setOnClickListener(v->{if(user!=null&&db!=null){pendingCreateCategory=selected;createRoomDialog();}else requireSignInForCreate();});head.addView(hot,new LinearLayout.LayoutParams(dp(46),dp(50)));root.addView(head);

        HorizontalScrollView chipScroll = new HorizontalScrollView(this); chipScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout chips = new LinearLayout(this); chips.setOrientation(LinearLayout.HORIZONTAL); chips.setPadding(dp(12),dp(3),dp(12),dp(8)); chipScroll.addView(chips);
        String[] names = {"Hot","Event","Date","Music","Game"};
        for (String n : names) {
            boolean active=n.equalsIgnoreCase(selected);
            TextView t = tv(n,13,active?0xff19151e:0xff6c6671,active); t.setGravity(Gravity.CENTER);
            t.setBackground(bg(active?0xffffee00:0xfff5f5f5,8));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(74),dp(32)); p.setMargins(dp(3),0,dp(3),0); chips.addView(t,p);
            t.setOnClickListener(v -> renderLobby(n));
        }
        root.addView(chipScroll,new LinearLayout.LayoutParams(-1,dp(46)));

        if(!lobbyFilter.isEmpty()) {
            LinearLayout filterBar=new LinearLayout(this);filterBar.setGravity(Gravity.CENTER_VERTICAL);filterBar.setPadding(dp(14),0,dp(14),dp(6));
            TextView f=tv("Search: "+lobbyFilter,12,0xff6f6875,true);f.setBackground(bg(0xfffff1cf,14));f.setGravity(Gravity.CENTER_VERTICAL);filterBar.addView(f,new LinearLayout.LayoutParams(0,dp(32),1));
            TextView clear=tv("×",22,0xff6f6875,true);clear.setGravity(Gravity.CENTER);clear.setOnClickListener(v->{lobbyFilter="";renderLobby(selected);});LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(dp(42),dp(32));cp.setMargins(dp(6),0,0,0);filterBar.addView(clear,cp);root.addView(filterBar);
        }

        ScrollView scroll = new ScrollView(this); page = new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setPadding(dp(10),dp(2),dp(10),dp(16)); scroll.addView(page);
        if (user != null && db != null) {
            LinearLayout cloudList = new LinearLayout(this); cloudList.setOrientation(LinearLayout.VERTICAL); page.addView(cloudList);
            roomsListener = db.collection("live_rooms").orderBy("createdAt", Query.Direction.DESCENDING).limit(30)
                .addSnapshotListener((snap,error) -> {
                    if (error != null) { showLobbyError(cloudList,"Live rooms could not load. Tap to retry.",selected); return; }
                    cloudList.removeAllViews();
                    int shown = 0;
                    LinearLayout row = null;
                    if (snap != null) {
                        for (DocumentSnapshot doc : snap.getDocuments()) {
                            if (Boolean.TRUE.equals(doc.getBoolean("closed"))) continue;
                            String name = str(doc,"name","Live Party");
                            String host = str(doc,"ownerName","KING Host");
                            String id = doc.getId();
                            String category = str(doc,"category","Hot");
                            boolean priv = Boolean.TRUE.equals(doc.getBoolean("isPrivate"));
                            boolean password = Boolean.TRUE.equals(doc.getBoolean("hasPassword"));
                            String q = lobbyFilter.toLowerCase(java.util.Locale.US);
                            if(!categoryMatches(selected,category)) continue;
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
                    if (shown == 0) showEmptyLobby(cloudList,selected);
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
    private void openMainTab(int tab){ KingNav.openRoot(this,tab); }
    private void showPartyActions(String selected){
        new AlertDialog.Builder(this).setItems(new String[]{"＋ Create Party","↻ Refresh live rooms"},(d,w)->{
            if(w==0){if(user!=null&&db!=null)createRoomDialog();else requireSignInForCreate();}
            else renderLobby(selected);
        }).show();
    }
    private void showLobbySearch(String selected){
        final EditText e=new EditText(this);e.setHint("Search room, host or ID");e.setSingleLine(true);e.setText(lobbyFilter);
        new AlertDialog.Builder(this).setTitle("Search Party").setView(e).setNegativeButton("Cancel",null).setNeutralButton("Clear",(d,w)->{lobbyFilter="";renderLobby(selected);})
            .setPositiveButton("Search",(d,w)->{lobbyFilter=e.getText().toString().trim();renderLobby(selected);}).show();
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
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setGravity(Gravity.CENTER);box.setPadding(dp(18),dp(18),dp(18),dp(18));box.setBackground(bg(0xfff4f0f6,16));
        TextView icon=tv("🎙",42,0xff8a49ed,true);icon.setGravity(Gravity.CENTER);box.addView(icon);
        TextView title=tv("No live rooms here yet",16,0xff2d2831,true);title.setGravity(Gravity.CENTER);box.addView(title);
        TextView sub=tv("Create the first real "+selected+" Party room.",13,0xff77717d,false);sub.setGravity(Gravity.CENTER);box.addView(sub);
        TextView create=tv("＋ Create Party",14,Color.WHITE,true);create.setGravity(Gravity.CENTER);create.setBackground(bg(PURPLE,18));create.setOnClickListener(v->createRoomDialog());LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(dp(160),dp(44));cp.gravity=Gravity.CENTER_HORIZONTAL;cp.setMargins(0,dp(8),0,0);box.addView(create,cp);
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(210));bp.setMargins(dp(6),dp(12),dp(6),0);host.addView(box,bp);
    }
    private void showLobbyError(LinearLayout host,String message,String selected){
        host.removeAllViews();TextView e=tv(message,14,0xff8b4d64,true);e.setGravity(Gravity.CENTER);e.setBackground(bg(0xffffe9ef,16));e.setOnClickListener(v->renderLobby(selected));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(90));p.setMargins(dp(6),dp(12),dp(6),0);host.addView(e,p);
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
    private void addLiveRoomGridCard(LinearLayout row,String name,String hostName,String id,String category,boolean priv,boolean password,String photoUrl,Runnable action) {
        int[] palette={0xfff8b6d2,0xffefc3a6,0xffb6d3f8,0xffa8e0ca,0xffd5c0f4,0xffffc6b5};
        int cardColor=palette[Math.abs((id==null?name:id).hashCode())%palette.length];
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setBackground(bg(Color.WHITE,10));
        FrameLayout cover=new FrameLayout(this);cover.setBackground(bg(cardColor,10));
        ImageView img=new ImageView(this);img.setScaleType(ImageView.ScaleType.CENTER_CROP);cover.addView(img,new FrameLayout.LayoutParams(-1,-1));
        TextView initial=tv(roomAvatarText(hostName,category),34,0xff4f3f4e,true);initial.setGravity(Gravity.CENTER);initial.setBackground(bg(0x33ffffff,0));cover.addView(initial,new FrameLayout.LayoutParams(-1,-1));
        if(photoUrl!=null&&!photoUrl.trim().isEmpty())loadProfilePhoto(img,initial,photoUrl.trim());
        TextView tag=tv(("Music".equalsIgnoreCase(category)?"Music":"● "+("Hot".equalsIgnoreCase(category)?"Chat":category)),10,Color.WHITE,true);tag.setGravity(Gravity.CENTER);tag.setBackground(bg(0xff2ccf7a,8));FrameLayout.LayoutParams tp=new FrameLayout.LayoutParams(dp(58),dp(23),Gravity.BOTTOM|Gravity.LEFT);tp.setMargins(dp(7),0,0,dp(7));cover.addView(tag,tp);
        TextView count=tv("LIVE",10,Color.WHITE,true);count.setGravity(Gravity.CENTER);count.setBackground(bg(0x99000000,10));FrameLayout.LayoutParams ctp=new FrameLayout.LayoutParams(dp(48),dp(23),Gravity.TOP|Gravity.RIGHT);ctp.setMargins(0,dp(7),dp(7),0);cover.addView(count,ctp);
        if(db!=null&&id!=null&&!id.isEmpty())db.collection("live_rooms").document(id).collection("members").get().addOnSuccessListener(x->count.setText(String.valueOf(Math.max(1,x.size())))).addOnFailureListener(e->count.setText("LIVE"));
        card.addView(cover,new LinearLayout.LayoutParams(-1,dp(137)));
        TextView title=tv(name,13,0xff1b1b1b,true);title.setMaxLines(1);title.setPadding(dp(5),dp(4),dp(4),0);card.addView(title,new LinearLayout.LayoutParams(-1,dp(30)));
        TextView host=tv((priv?"🔒 ":"")+(password?"🔑 ":"")+hostName,11,0xff777777,false);host.setMaxLines(1);host.setPadding(dp(5),0,dp(4),dp(3));card.addView(host,new LinearLayout.LayoutParams(-1,dp(28)));
        card.setOnClickListener(v->action.run());
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
        card.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0,-1,1); cp.setMargins(dp(3),dp(3),dp(3),dp(3)); row.addView(card,cp);
    }

    private void createRoomDialog() {
        if(user==null||db==null){requireSignInForCreate();return;}
        pendingCreateSeats=8;pendingCreatePrivate=false;pendingCreateCoverUri=null;
        renderCreateRoomPage();
    }
    private void renderCreateRoomPage(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(8),dp(18),dp(18));root.setBackgroundColor(0xff033426);
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);TextView back=tv("‹",32,Color.WHITE,false);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->renderLobby("Hot"));head.addView(back,new LinearLayout.LayoutParams(dp(44),dp(48)));TextView spacer=tv("Create Party",16,0xffd9eee7,true);spacer.setGravity(Gravity.CENTER);head.addView(spacer,new LinearLayout.LayoutParams(0,dp(48),1));root.addView(head);
        ScrollView sv=new ScrollView(this);LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setGravity(Gravity.CENTER_HORIZONTAL);sv.addView(body);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        FrameLayout cover=new FrameLayout(this);cover.setBackground(bg(0x22000000,10));ImageView preview=new ImageView(this);preview.setScaleType(ImageView.ScaleType.CENTER_CROP);cover.addView(preview,new FrameLayout.LayoutParams(-1,-1));TextView coverText=tv("▣\nChange cover",14,Color.WHITE,true);coverText.setGravity(Gravity.CENTER);cover.addView(coverText,new FrameLayout.LayoutParams(-1,-1));cover.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("image/*");i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);startActivityForResult(i,ROOM_COVER_PICK_REQUEST);});LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(dp(110),dp(110));cp.setMargins(0,dp(8),0,dp(8));body.addView(cover,cp);
        if(pendingCreateCoverUri!=null){try{preview.setImageURI(pendingCreateCoverUri);coverText.setText("");}catch(Exception ignored){}}
        TextView tip=tv("Upload cover picture to gain more views",11,0xffffee00,true);tip.setGravity(Gravity.CENTER);body.addView(tip,new LinearLayout.LayoutParams(-1,dp(34)));
        LinearLayout flags=new LinearLayout(this);flags.setGravity(Gravity.CENTER);TextView pub=tv(pendingCreatePrivate?"🔒 Private":"🔓 Public",13,Color.WHITE,true);pub.setGravity(Gravity.CENTER);pub.setOnClickListener(v->{pendingCreatePrivate=!pendingCreatePrivate;renderCreateRoomPage();});flags.addView(pub,new LinearLayout.LayoutParams(0,dp(42),1));TextView seat=tv("Seat: "+pendingCreateSeats,13,Color.WHITE,true);seat.setGravity(Gravity.CENTER);seat.setOnClickListener(v->{int[] vals={8,10,12};int idx=0;for(int i=0;i<vals.length;i++)if(vals[i]==pendingCreateSeats)idx=i;pendingCreateSeats=vals[(idx+1)%vals.length];renderCreateRoomPage();});flags.addView(seat,new LinearLayout.LayoutParams(0,dp(42),1));body.addView(flags,new LinearLayout.LayoutParams(-1,dp(48)));
        final EditText name=new EditText(this);name.setHint("Room name");name.setHintTextColor(0xff9cb8ae);name.setTextColor(Color.WHITE);name.setTextSize(18);name.setSingleLine(true);name.setBackgroundColor(Color.TRANSPARENT);name.setPadding(dp(4),dp(10),dp(4),dp(10));body.addView(name,new LinearLayout.LayoutParams(-1,dp(58)));View line=new View(this);line.setBackgroundColor(0x446bd8b4);body.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));
        TextView type=tv("Room type: "+pendingCreateCategory+"  ›",13,0xffd5eee5,true);type.setGravity(Gravity.CENTER_VERTICAL);type.setPadding(dp(4),0,0,0);type.setOnClickListener(v->{String[] cats={"Chat","Event","Date","Music","Game","KTV","Radio","PK","Pick Me","Family","Multi Video"};new AlertDialog.Builder(this).setTitle("Choose room type").setItems(cats,(d,w)->{pendingCreateCategory=cats[w];renderCreateRoomPage();}).show();});body.addView(type,new LinearLayout.LayoutParams(-1,dp(52)));
        LinearLayout seats=new LinearLayout(this);seats.setOrientation(LinearLayout.VERTICAL);for(int r=0;r<2;r++){LinearLayout rr=new LinearLayout(this);rr.setGravity(Gravity.CENTER);for(int c=0;c<4;c++){int no=r*4+c+1;TextView bubble=tv("+\nNO."+no,11,0xffcce8de,true);bubble.setGravity(Gravity.CENTER);bubble.setBackground(bg(0x224fd0aa,45));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,dp(72),1);bp.setMargins(dp(5),dp(5),dp(5),dp(5));rr.addView(bubble,bp);}seats.addView(rr,new LinearLayout.LayoutParams(-1,dp(82)));}body.addView(seats,new LinearLayout.LayoutParams(-1,dp(168)));
        TextView start=tv("🎉 Start Room",15,0xff171717,true);start.setGravity(Gravity.CENTER);start.setBackground(bg(0xffffee00,10));LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(54));sp.setMargins(0,dp(26),0,dp(8));body.addView(start,sp);start.setOnClickListener(v->{String n=name.getText().toString().trim();if(n.length()<2){name.setError("Enter a room name");return;}createCloudRoom(n,pendingCreateCategory,pendingCreateSeats,pendingCreatePrivate);});
        setSafeContentView(root);
    }
    private void createCloudRoom(String name,String category,int seats,boolean priv){
        Map<String,Object> r=new HashMap<>();r.put("name",name);r.put("ownerUid",user.getUid());r.put("ownerName",safeName());if(user.getPhotoUrl()!=null)r.put("ownerPhoto",user.getPhotoUrl().toString());r.put("category",category);r.put("theme","Classic");r.put("maxSeats",seats);r.put("isPrivate",priv);r.put("hasPassword",false);r.put("announcement",announcement);r.put("locked",false);r.put("muteAll",false);r.put("closed",false);r.put("createdAt",FieldValue.serverTimestamp());r.put("updatedAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").add(r).addOnSuccessListener(ref->{maxSeats=seats;if(pendingCreateCoverUri!=null&&storage!=null)uploadRoomCoverV600(ref,pendingCreateCoverUri);openCloudRoom(ref.getId(),name,user.getUid(),safeName(),priv,false);}).addOnFailureListener(e->toast("Room create failed: "+msg(e)));
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
        roomId=id; roomName=name; ownerUid=hostUid; ownerName=hostName; cloudRoom=true; mySeat=-1; micOn=false; coHost=false; roomPrivate=priv; roomHasPassword=password; memberSeen=false; localRemovedSeats.clear(); lockedSeats.clear();
        processedEventIds.clear(); eventSnapshotReady=false;
        if(roomPrivate&&!isOwner()) tryPrivateEntry(); else { registerMember(); renderParty(); }
    }
    private void tryPrivateEntry(){
        if(user==null||db==null||roomId==null)return;
        Map<String,Object>d=new HashMap<>();d.put("uid",user.getUid());d.put("name",safeName());d.put("joinedAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).collection("members").document(user.getUid()).set(d)
            .addOnSuccessListener(v->{memberSeen=true;addEvent("join",safeName()+" joined the room");renderParty();if(page!=null)page.postDelayed(()->showEntranceEffect(safeName()),350);})
            .addOnFailureListener(e->{if(roomHasPassword)passwordRoomJoinDialog();else toast("Private room • invite access required");});
    }
    private void passwordRoomJoinDialog(){
        final EditText e=new EditText(this);e.setHint("Room password");e.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("🔐 Private Party").setMessage("Enter the room password").setView(e)
            .setNegativeButton("Cancel",null).setPositiveButton("Join",(d,w)->{
                String password=e.getText().toString();if(password.length()<4){toast("Password must be at least 4 characters");return;}
                String proof=passwordProof(roomId,password);if(proof.isEmpty()){toast("Password check unavailable");return;}
                Map<String,Object> access=new HashMap<>();access.put("uid",user.getUid());access.put("passwordHash",proof);access.put("createdAt",FieldValue.serverTimestamp());
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

    private void renderParty() {
        clearListeners();

        final int ROOM_GREEN_V532 = 0xff008e6b;
        final int SEAT_GREEN_V532 = 0x38ffffff;
        final int SEAT_TEXT_V532 = 0xbfffffff;

        FrameLayout shell = new FrameLayout(this);
        shell.setBackgroundResource(R.drawable.ref_voice_room_bg);

        LinearLayout roomRoot = new LinearLayout(this);
        roomRoot.setOrientation(LinearLayout.VERTICAL);
        roomRoot.setPadding(0,0,0,0);
        FrameLayout.LayoutParams roomRootLp = new FrameLayout.LayoutParams(-1,-1);
        roomRootLp.bottomMargin = dp(64);
        shell.addView(roomRoot, roomRootLp);

        // Top header: back, room title + online count, share, menu.
        LinearLayout head = new LinearLayout(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setPadding(dp(14),0,dp(14),0);
        TextView back = tv("‹",34,Color.WHITE,true); back.setGravity(Gravity.CENTER); back.setOnClickListener(v->leaveRoom());
        head.addView(back,new LinearLayout.LayoutParams(dp(30),dp(56)));

        LinearLayout info = new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL); info.setGravity(Gravity.CENTER_VERTICAL); info.setPadding(dp(10),0,0,0);
        TextView rn = tv(roomName==null?"KING Plus Party":roomName,15,Color.WHITE,true); rn.setSingleLine(true); rn.setPadding(0,0,0,0); rn.setOnClickListener(v->hostProfileDialog());
        info.addView(rn,new LinearLayout.LayoutParams(-1,dp(28)));
        viewerLabel = pill("👤 1",0x33000000,this::membersDialog); viewerLabel.setTextSize(10); viewerLabel.setPadding(dp(6),0,dp(8),0);
        info.addView(viewerLabel,new LinearLayout.LayoutParams(dp(58),dp(22)));
        head.addView(info,new LinearLayout.LayoutParams(0,dp(56),1));

        TextView share = pill("↗",0x2effffff,this::shareRoom); share.setTextSize(18); head.addView(share,new LinearLayout.LayoutParams(dp(32),dp(32)));
        TextView more = pill("≡",0x2effffff,this::roomMenu); more.setTextSize(18); LinearLayout.LayoutParams moreLp=new LinearLayout.LayoutParams(dp(32),dp(32)); moreLp.setMargins(dp(8),0,0,0); head.addView(more,moreLp);
        roomRoot.addView(head,new LinearLayout.LayoutParams(-1,dp(56)));

        // Rank / heart / edit bar.
        LinearLayout badges = new LinearLayout(this); badges.setGravity(Gravity.CENTER_VERTICAL); badges.setPadding(dp(14),dp(2),dp(14),dp(6));
        String compact=shortId(); if(compact.length()>2) compact=compact.substring(compact.length()-2);
        roomLevelLabel=pill("💎 No."+compact,0xff8e24aa,null); roomLevelLabel.setTextSize(10); roomLevelLabel.setPadding(dp(8),0,dp(8),0);
        badges.addView(roomLevelLabel,new LinearLayout.LayoutParams(-2,dp(24)));
        heartLevelLabel=pill("💖 Lv. 1 Heart",0x26000000,null); heartLevelLabel.setTextSize(10); heartLevelLabel.setPadding(dp(8),0,dp(8),0);
        LinearLayout.LayoutParams heartLp=new LinearLayout.LayoutParams(-2,dp(24)); heartLp.setMargins(dp(8),0,0,0); badges.addView(heartLevelLabel,heartLp);
        View badgeSpacer=new View(this); badges.addView(badgeSpacer,new LinearLayout.LayoutParams(0,1,1));
        TextView edit=pill("📑 Edit",0x33000000,isOwner()?this::roomControlCenter:this::roomBillboardDialog); edit.setTextSize(11); edit.setPadding(dp(10),0,dp(10),0);
        badges.addView(edit,new LinearLayout.LayoutParams(-2,dp(26)));
        roomRoot.addView(badges,new LinearLayout.LayoutParams(-1,dp(36)));

        // Hidden state labels retained for existing realtime/controller logic.
        roomLiveBadge=pill("👥 1 • 🎙 0",0x00302747,null); roomLiveBadge.setVisibility(View.GONE); roomRoot.addView(roomLiveBadge,new LinearLayout.LayoutParams(1,1));
        roomStateLabel=tv("",1,Color.TRANSPARENT,false); roomStateLabel.setVisibility(View.GONE); roomRoot.addView(roomStateLabel,new LinearLayout.LayoutParams(1,1)); refreshRoomState();
        supporterLabel=tv("",1,Color.TRANSPARENT,false); supporterLabel.setVisibility(View.GONE); roomRoot.addView(supporterLabel,new LinearLayout.LayoutParams(1,1));
        announcementLabel=tv("",1,Color.TRANSPARENT,false); announcementLabel.setVisibility(View.GONE); roomRoot.addView(announcementLabel,new LinearLayout.LayoutParams(1,1));
        seatRequestLabel=tv("",1,Color.TRANSPARENT,false); seatRequestLabel.setVisibility(View.GONE); roomRoot.addView(seatRequestLabel,new LinearLayout.LayoutParams(1,1));
        reactionBanner=tv("",1,Color.TRANSPARENT,false); reactionBanner.setVisibility(View.GONE); roomRoot.addView(reactionBanner,new LinearLayout.LayoutParams(1,1));

        // 8-seat compact grid.
        seatsBox = new LinearLayout(this); seatsBox.setOrientation(LinearLayout.VERTICAL); seatsBox.setPadding(0,dp(4),0,dp(6));
        roomRoot.addView(seatsBox,new LinearLayout.LayoutParams(-1,dp(178))); rebuildSeats();

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
        LinearLayout composer = new LinearLayout(this); composer.setGravity(Gravity.CENTER_VERTICAL); composer.setPadding(dp(8),dp(7),dp(8),dp(7)); composer.setBackgroundColor(Color.WHITE);
        TextView plus=pill("＋",0x00ffffff,this::toolsPanel); plus.setTextColor(0xff333333); plus.setTextSize(24); composer.addView(plus,new LinearLayout.LayoutParams(dp(38),dp(48)));
        composerBox=new EditText(this); composerBox.setHint("Type message..."); composerBox.setTextColor(0xff111111); composerBox.setHintTextColor(0xff777777); composerBox.setSingleLine(true); composerBox.setTextSize(14); composerBox.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEND); composerBox.setOnEditorActionListener((v, actionId, event)->{ if(actionId==android.view.inputmethod.EditorInfo.IME_ACTION_SEND){sendMessage(composerBox);return true;}return false;}); composerBox.setBackground(bg(0xfff3f5f5,20)); composerBox.setPadding(dp(8),0,dp(8),0);
        composer.addView(composerBox,new LinearLayout.LayoutParams(0,dp(48),1));
        TextView emoji=pill("😊",0x00ffffff,this::showLiveEmojiPanelV530); emoji.setTextColor(0xff333333); emoji.setTextSize(23); composer.addView(emoji,new LinearLayout.LayoutParams(dp(42),dp(46)));
        micLabel=pill(micOn?"🎤":"🎙",0x00ffffff,this::toggleMic); micLabel.setTextColor(0xff333333); micLabel.setTextSize(23); composer.addView(micLabel,new LinearLayout.LayoutParams(dp(42),dp(46)));
        TextView gift=pill("🎁",0x00ffffff,this::giftShopPanel); gift.setTextColor(0xff333333); gift.setTextSize(22); composer.addView(gift,new LinearLayout.LayoutParams(dp(42),dp(46)));
        TextView send=pill("➤",0x00ffffff,()->sendMessage(composerBox)); send.setTextColor(0xff008e6b); send.setTextSize(23); composer.addView(send,new LinearLayout.LayoutParams(dp(38),dp(46)));
        refreshMicControl();
        plus.setContentDescription("Open Play Center"); emoji.setContentDescription("Emoji and reactions"); gift.setContentDescription("Send a gift"); send.setContentDescription("Send message");
        FrameLayout.LayoutParams composerLp=new FrameLayout.LayoutParams(-1,dp(64)); composerLp.gravity=Gravity.BOTTOM; shell.addView(composer,composerLp);

        setSafeContentView(shell);

        // Full-screen transparent live emoji overlay must remain above the room UI.
        liveEmojiStageV530 = new FrameLayout(this); liveEmojiStageV530.setClipChildren(false); liveEmojiStageV530.setClipToPadding(false); liveEmojiStageV530.setClickable(false); liveEmojiStageV530.setFocusable(false);
        FrameLayout.LayoutParams liveOverlayLpV531 = new FrameLayout.LayoutParams(-1,-1); liveOverlayLpV531.bottomMargin=dp(64); shell.addView(liveEmojiStageV530,liveOverlayLpV531);

        musicStatusLabel=tv(currentSong.isEmpty()?"":"🎵 "+currentSong,11,Color.WHITE,true); musicStatusLabel.setVisibility(View.GONE);

        if(cloudRoom) attachCloudRoom();
        else { viewerLabel.setText("👤 1"); fillLocalSeats(); if(roomRoot!=null) roomRoot.postDelayed(()->showEntranceEffect(safeName()),250); }
    }

    private void addQuickChipV532(LinearLayout row, String text, EditText ignored) {
        TextView chip = tv(text,11,0xff222222,false); chip.setGravity(Gravity.CENTER); chip.setBackground(bg(Color.WHITE,14)); chip.setPadding(dp(12),dp(4),dp(12),dp(4));
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
        List<String> names=new ArrayList<>();
        if(ownerName!=null&&!ownerName.trim().isEmpty())names.add(ownerName);
        if(cloudRoom){for(String n:memberNames)if(n!=null&&!n.trim().isEmpty()&&!names.contains(n))names.add(n);}
        if(names.isEmpty())names.add("Host");
        int shown=0;
        for(String n:names){
            if(shown++>=6)break;
            String initial=n.trim().isEmpty()?"?":n.trim().substring(0,1).toUpperCase();
            TextView av=tv(initial,12,Color.WHITE,true); av.setGravity(Gravity.CENTER); av.setBackground(bg(0xff5b3a78,40));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(34),dp(34)); lp.setMargins(0,0,dp(5),0); memberStripBox.addView(av,lp);
        }
        TextView label=tv(cloudRoom?"Members":"Host",11,MUTED,false); memberStripBox.addView(label,new LinearLayout.LayoutParams(0,dp(34),1));
    }

    private void addQuick(LinearLayout row,String text,Runnable action) {
        TextView v = pill(text,CARD,action); v.setTextSize(11); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(56),1);p.setMargins(dp(2),0,dp(2),0);row.addView(v,p);
    }
    private void rebuildSeats() {
        if (seatsBox == null) return;
        seatsBox.removeAllViews();seatViews560.clear();
        final int displaySeatsV532 = maxSeats==10?10:(maxSeats==12?12:8);
        final int columns=displaySeatsV532==10?5:4;final int rows=(displaySeatsV532+columns-1)/columns;
        android.view.ViewGroup.LayoutParams layout=seatsBox.getLayoutParams();if(layout!=null){layout.height=dp(rows*80+(rows-1)*8+10);seatsBox.setLayoutParams(layout);}
        for (int r=0;r<rows;r++) {
            LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER); row.setWeightSum(columns);
            for (int c=0;c<columns;c++) {
                int no=r*columns+c+1; if(no>displaySeatsV532) break;
                LinearLayout seat=new LinearLayout(this); seat.setOrientation(LinearLayout.VERTICAL); seat.setGravity(Gravity.CENTER); seat.setPadding(dp(2),dp(2),dp(2),0);
                String n=seatNames.get(no); boolean mine=cloudRoom?user!=null&&user.getUid().equals(seatUids.get(no)):no==mySeat; boolean seatLocked=cloudRoom&&lockedSeats.contains(no);
                boolean hostVisualV532 = n!=null && (cloudRoom ? ownerUid!=null && ownerUid.equals(seatUids.get(no)) : no==mySeat);
                String icon = hostVisualV532 ? "👑" : (n==null?(seatLocked?"🔒":"＋"):(mine?"👑":avatarForSeat(no,n)));
                TextView av=tv(icon,hostVisualV532?25:(n==null?22:24),hostVisualV532?Color.WHITE:(n==null?0xbfffffff:Color.WHITE),true);
                av.setGravity(Gravity.CENTER); av.setBackground(bg(hostVisualV532?0xff52b69a:(n==null?0x38ffffff:(mine?0xff2e9d83:seatColor(no))),27));
                String seatUid720=seatUids.get(no);int vip720=seatUid720==null?0:(memberVip720.containsKey(seatUid720)?memberVip720.get(seatUid720):0);int level720=seatUid720==null?0:(memberLevel720.containsKey(seatUid720)?memberLevel720.get(seatUid720):0);String frame730=seatUid720==null?"Minimal Frame":memberFrame730.get(seatUid720);
                if(frame730==null||frame730.trim().isEmpty())frame730=vip720>=10?"Crown Frame":vip720>=7?"Galaxy Frame":vip720>=4?"Royal Frame":"Minimal Frame";
                if(mine){LevelSystem.Snapshot me720=LevelSystem.read(this);vip720=me720.vipLevel;level720=me720.level;frame730=KingCosmetics.frame(this);}
                FrameLayout avatarFrame=new FrameLayout(this);avatarFrame.setBackground(KingCosmetics.avatarFrame(this,frame730,hostVisualV532));avatarFrame.setPadding(dp(4),dp(4),dp(4),dp(4));avatarFrame.setClipToOutline(true);ImageView photo=new ImageView(this);photo.setScaleType(ImageView.ScaleType.CENTER_CROP);avatarFrame.addView(photo,new FrameLayout.LayoutParams(-1,-1));avatarFrame.addView(av,new FrameLayout.LayoutParams(-1,-1));seat.addView(avatarFrame,new LinearLayout.LayoutParams(dp(56),dp(56)));
                String photoUrl=memberPhotos540.get(seatUids.get(no));if(mine&&user!=null&&user.getPhotoUrl()!=null)photoUrl=user.getPhotoUrl().toString();if(n!=null&&photoUrl!=null&&!photoUrl.isEmpty())loadProfilePhoto(photo,av,photoUrl);
                String label;
                if(hostVisualV532) label="👑 "+shortSeatName(n)+(vip720>0?" • V"+vip720:"");
                else if(n==null) label=seatLocked?"NO."+no+" 🔒":"NO."+no;
                else label=(mine?"👑 ":"")+shortSeatName(n)+(level720>0?" • Lv"+level720:"")+(vip720>0?" • V"+vip720:"")+(Boolean.FALSE.equals(seatMics.get(no))?" 🔇":"");
                TextView lab=tv(label,10,n==null&&!hostVisualV532?0xd8ffffff:Color.WHITE,n!=null||hostVisualV532); lab.setSingleLine(true); lab.setEllipsize(android.text.TextUtils.TruncateAt.END); lab.setGravity(Gravity.CENTER); lab.setPadding(0,dp(2),0,0); seat.addView(lab,new LinearLayout.LayoutParams(-1,dp(22)));
                seatViews560.put(no,seat);final int seatNo=no; seat.setOnClickListener(v->seatAction(seatNo)); row.addView(seat,new LinearLayout.LayoutParams(0,dp(80),1));
            }
            LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(80)); if(r>0) rp.setMargins(0,dp(8),0,0); seatsBox.addView(row,rp);
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
            if (targetUidV530 != null && !user.getUid().equals(targetUidV530)) { showOccupiedSeatMenuV530(no); return; }
            if (targetUidV530 == null) { showEmptySeatMenuV530(no); return; }
        }
        if(cloudRoom){
            String existing=seatUids.get(no);
            if(existing!=null){openSeatProfile(no);return;}
            if(existing!=null){cloudSeatAction(no);return;}
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
        if(user==null||db==null){toast("Sign in required");return;}
        String requestId=user.getUid()+"_"+no;
        Map<String,Object>d=new HashMap<>();d.put("uid",user.getUid());d.put("name",safeName());d.put("seatNo",no);d.put("createdAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).collection("seat_requests").document(requestId).set(d)
            .addOnSuccessListener(v->toast("Seat "+no+" request sent to host"))
            .addOnFailureListener(e->toast("Seat request failed: "+msg(e)));
    }
    private void showSeatRequests(int no){
        if(!isModerator()||db==null)return;
        db.collection("live_rooms").document(roomId).collection("seat_requests").whereEqualTo("seatNo",no).get()
            .addOnSuccessListener(snap->{
                if(snap==null||snap.isEmpty()){toast("No requests for seat "+no);return;}
                List<DocumentSnapshot> docs=snap.getDocuments();List<String> labels=new ArrayList<>();
                for(DocumentSnapshot d:docs)labels.add(str(d,"name","User"));
                new AlertDialog.Builder(this).setTitle("Seat "+no+" requests").setItems(labels.toArray(new String[0]),(x,w)->seatRequestDecision(docs.get(w),no))
                    .setNegativeButton("Close",null).show();
            }).addOnFailureListener(e->toast(msg(e)));
    }
    private void seatRequestDecision(DocumentSnapshot req,int no){
        String uid=req.getString("uid");String name=str(req,"name","User");if(uid==null)return;
        String[] items={"✓ Approve","✕ Deny"};
        new AlertDialog.Builder(this).setTitle(name+" → Seat "+no).setItems(items,(d,w)->{
            if(w==0)approveSeatRequest(req.getId(),uid,name,no);else req.getReference().delete();
        }).show();
    }
    private void approveSeatRequest(String requestId,String uid,String name,int no){
        if(!isModerator()||db==null)return;
        DocumentReference seat=db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(no));
        Map<String,Object>d=new HashMap<>();d.put("uid",uid);d.put("name",name);d.put("micOn",false);d.put("joinedAt",FieldValue.serverTimestamp());
        Runnable assign=()->seat.set(d).addOnSuccessListener(v->{
            db.collection("live_rooms").document(roomId).collection("seat_requests").document(requestId).delete();
            addEvent("seat",name+" was approved for seat "+no);
        }).addOnFailureListener(e->toast("Approve failed: "+msg(e)));
        Integer oldSeat=null;for(Map.Entry<Integer,String> x:new HashMap<>(seatUids).entrySet())if(uid.equals(x.getValue())){oldSeat=x.getKey();break;}
        if(oldSeat!=null&&oldSeat!=no){
            db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(oldSeat)).delete()
                .addOnSuccessListener(v->assign.run()).addOnFailureListener(e->toast("Could not move existing seat: "+msg(e)));
        }else assign.run();
    }
    private void cloudSeatAction(int no) {
        if(user==null||db==null)return; DocumentReference ref=db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(no));
        String existing=seatUids.get(no);
        if(existing!=null&&!existing.equals(user.getUid())){seatUserMenu(no);return;}
        if(existing!=null){ref.delete().addOnSuccessListener(v->{mySeat=-1;micOn=false;addEvent("leave",displayName+" left mic seat");});return;}
        Map<String,Object>d=new HashMap<>();d.put("uid",user.getUid());d.put("name",safeName());d.put("micOn",false);d.put("joinedAt",FieldValue.serverTimestamp());
        Runnable take=()->ref.set(d).addOnSuccessListener(v->{mySeat=no;micOn=false;addEvent("seat",safeName()+" took seat "+no);}).addOnFailureListener(e->toast("Seat unavailable: "+msg(e)));
        if(mySeat>0&&mySeat!=no){
            db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(mySeat)).delete()
                .addOnSuccessListener(v->take.run()).addOnFailureListener(e->toast("Could not move seat: "+msg(e)));
        } else take.run();
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
                        if (nameV530.toLowerCase().contains(qV530)) {
                            hitsV530.add(docV530);
                            labelsV530.add(nameV530 + "  •  " + str(docV530,"ownerName","Host"));
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

    private void showLiveEmojiPanelV530() {
        final LinearLayout sheet=new LinearLayout(this);sheet.setOrientation(LinearLayout.VERTICAL);sheet.setBackgroundColor(Color.WHITE);
        LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);bar.setPadding(dp(8),0,dp(4),0);
        TextView title=tv("Emoji & Stickers",15,0xff333333,true);bar.addView(title,new LinearLayout.LayoutParams(0,dp(44),1));
        TextView close=tv("×",27,0xff666666,false);close.setGravity(Gravity.CENTER);bar.addView(close,new LinearLayout.LayoutParams(dp(48),dp(44)));sheet.addView(bar);

        ScrollView scroll=new ScrollView(this);LinearLayout grid=new LinearLayout(this);grid.setOrientation(LinearLayout.VERTICAL);grid.setPadding(dp(6),dp(4),dp(6),dp(4));scroll.addView(grid);sheet.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout tabs=new LinearLayout(this);tabs.setGravity(Gravity.CENTER);sheet.addView(tabs,new LinearLayout.LayoutParams(-1,dp(52)));

        String[] labels={"Live","More","Classic","Faces","Saved"};
        String[] reference={"[ref:0]","[ref:1]","[ref:2]","[ref:3]","[ref:4]","[ref:5]"};
        String[] live=new String[12];for(int i=0;i<live.length;i++)live[i]=LiveEmojiView.token(i);
        String[] faces={"😀","😃","😄","😁","😆","😅","😂","🤣","😊","😇","🙂","🙃","😉","😌","😍","🥰","😘","😗","😙","😚","😋","😛","😝","😜","🤪","🤨","🧐","🤓","😎","🥳"};
        String[] expressions={"🤩","🥹","🥺","😢","😭","😤","😡","🤬","🤯","😱","😨","😰","😥","😓","🤗","🤔","🫡","🤭","🫢","🫣","😶‍🌫️","😐","😑","😬","🙄","😴","🤤","🤒","🤕","🤧","🥵","🥶","😵","🤠","👻","💀"};

        final AlertDialog dialog=new AlertDialog.Builder(this).setView(sheet).create();
        close.setOnClickListener(v->dialog.dismiss());
        for(int i=0;i<labels.length;i++){
            final int k=i;TextView tab=tv(labels[i],12,0xff555555,true);tab.setGravity(Gravity.CENTER);tabs.addView(tab,new LinearLayout.LayoutParams(0,dp(50),1));
            tab.setOnClickListener(v->{
                String[] pack=k==0?reference:(k==1?live:(k==2?stickerPack550(0,67):(k==3?faces:favouriteEmojiPack610())));
                fillEmojiGrid610(grid,pack,k==2||k==3?6:4,k<2);
                for(int n=0;n<tabs.getChildCount();n++){View t=tabs.getChildAt(n);t.setBackgroundColor(n==k?0xfffff4a3:Color.WHITE);}
            });
        }
        tabs.getChildAt(0).performClick();
        dialog.setOnShowListener(v->{android.view.Window w=dialog.getWindow();if(w!=null){w.setBackgroundDrawableResource(android.R.color.transparent);w.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);w.setGravity(Gravity.BOTTOM);w.setLayout(-1,Math.min(dp(430),(int)(getResources().getDisplayMetrics().heightPixels*.52f)));}});
        dialog.show();
    }

    private void fillEmojiGrid610(LinearLayout grid,String[] emojis,int columns,boolean livePack){
        grid.removeAllViews();
        if(emojis==null||emojis.length==0){TextView empty=tv("No favourites yet\nLong-press any Face or Expression to add it here.",14,0xff777777,false);empty.setGravity(Gravity.CENTER);grid.addView(empty,new LinearLayout.LayoutParams(-1,dp(150)));return;}
        LinearLayout row=null;
        for(int i=0;i<emojis.length;i++){
            if(i%columns==0){row=new LinearLayout(this);row.setGravity(Gravity.CENTER);grid.addView(row,new LinearLayout.LayoutParams(-1,dp(columns==6?58:78)));}
            final String token=emojis[i];int live=LiveEmojiView.parse(token);View cell;
            if(ReferenceEmojiView.parse(token)>=0){cell=new ReferenceEmojiView(this,ReferenceEmojiView.parse(token),true);}
            else if(stickerResource550(token)!=0){android.widget.ImageView im=new android.widget.ImageView(this);im.setImageResource(stickerResource550(token));im.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);im.setContentDescription("Sticker "+token);cell=im;}
            else if(live>=0){cell=new LiveEmojiView(this,live,0);cell.setContentDescription("Send animated "+LiveEmojiView.LABELS[live]);}
            else{TextView text=tv(token,columns==6?31:34,0xff202020,false);text.setGravity(Gravity.CENTER);text.setContentDescription("Send "+token);cell=text;}
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-1,1);cp.setMargins(dp(2),dp(2),dp(2),dp(2));row.addView(cell,cp);
            cell.setOnClickListener(v->sendLiveEmojiV530(token));
            if(!livePack)cell.setOnLongClickListener(v->{toggleFavouriteEmoji610(token);return true;});
        }
        if(row!=null){int rem=emojis.length%columns;if(rem>0)for(int i=rem;i<columns;i++)row.addView(new View(this),new LinearLayout.LayoutParams(0,1,1));}
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

    private void sendLiveEmojiV530(String emoji) {
        String visual=stickerFallback610(emoji);
        showLiveEmojiEffect560(visual,displayName,user==null?null:user.getUid());
        if(cloudRoom && db!=null && user!=null && roomId!=null){
            Map<String,Object> event=new HashMap<>();event.put("actorUid",user.getUid());event.put("actorName",safeName());event.put("type","live_emoji");event.put("text",visual);event.put("emoji",visual);event.put("createdAt",FieldValue.serverTimestamp());
            Map<String,Object> message=new HashMap<>();message.put("senderUid",user.getUid());message.put("senderName",safeName());message.put("text",visual);message.put("createdAt",FieldValue.serverTimestamp());
            com.google.firebase.firestore.WriteBatch batch=db.batch();DocumentReference room=db.collection("live_rooms").document(roomId);batch.set(room.collection("messages").document(),message);batch.set(room.collection("events").document(),event);batch.commit().addOnFailureListener(e->toast("Shown on this phone only. Emoji not sent to room: "+msg(e)));
        }else{appendLocalChat(displayName,visual);}
    }

    private void showLiveEmojiEffectV530(String emoji,String sender){showLiveEmojiEffect560(emoji,sender,null);}
    private void showLiveEmojiEffect560(String emoji,String sender,String uid){
        if(emoji==null||emoji.trim().isEmpty())return;emoji=stickerFallback610(emoji);
        if(!cloudRoom)addChatRow(sender==null?"User":sender,emoji);
        final FrameLayout stage=liveEmojiStageV530;if(stage==null)return;
        final String key=uid!=null?uid:(sender==null?"local":sender);
        View previous=activeReactions560.remove(key);if(previous!=null){previous.animate().cancel();stage.removeView(previous);}
        int live=LiveEmojiView.parse(emoji);final View reaction;
        if(ReferenceEmojiView.parse(emoji)>=0)reaction=new ReferenceEmojiView(this,ReferenceEmojiView.parse(emoji),false);
        else if(stickerResource550(emoji)!=0)reaction=stickerImage852(stickerResource550(emoji));
        else if(live>=0)reaction=new LiveEmojiView(this,live,4000);
        else{TextView text=tv(emoji,42,Color.WHITE,false);text.setGravity(Gravity.CENTER);reaction=text;}
        activeReactions560.put(key,reaction);
        FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(dp(82),dp(82));lp.gravity=Gravity.TOP|Gravity.LEFT;lp.leftMargin=Math.max(0,(stage.getWidth()-dp(82))/2);lp.topMargin=dp(102);
        for(Map.Entry<Integer,View> entry:seatViews560.entrySet()){
            boolean match=uid!=null?uid.equals(seatUids.get(entry.getKey())):entry.getKey()==mySeat;
            if(match){int[] seatXY=new int[2],stageXY=new int[2];entry.getValue().getLocationOnScreen(seatXY);stage.getLocationOnScreen(stageXY);lp.leftMargin=Math.max(0,seatXY[0]-stageXY[0]+(entry.getValue().getWidth()-dp(82))/2);lp.topMargin=Math.max(0,seatXY[1]-stageXY[1]-dp(7));break;}
        }
        if(ReferenceEmojiView.parse(emoji)>=3){lp.width=stage.getWidth();lp.height=stage.getHeight();lp.leftMargin=0;lp.topMargin=0;}
        stage.addView(reaction,lp);reaction.setScaleX(.6f);reaction.setScaleY(.6f);reaction.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
        reaction.postDelayed(()->{reaction.animate().alpha(0f).setDuration(250).withEndAction(()->{stage.removeView(reaction);if(activeReactions560.get(key)==reaction)activeReactions560.remove(key);}).start();},3750);
    }

    private void showEmptySeatMenuV530(int seatNoV530) {
        new AlertDialog.Builder(this).setTitle("Seat "+seatNoV530).setItems(new String[]{"👑 Take this seat","📨 Invite member to this seat"},(d,w) -> {
            if(w==0) cloudSeatAction(seatNoV530); else inviteMemberToSeatV530(seatNoV530);
        }).setNegativeButton("Cancel",null).show();
    }

    private void showOccupiedSeatMenuV530(int seatNoV530) {
        final String uidV530 = seatUids.get(seatNoV530); final String nameV530 = seatNames.get(seatNoV530)==null?"Member":seatNames.get(seatNoV530);
        final boolean micV530 = !Boolean.FALSE.equals(seatMics.get(seatNoV530));
        String[] actionsV530 = {micV530?"🔇 Mute seat":"🎤 Unmute seat","⤵ Kick from seat","🚪 Kick from room"};
        new AlertDialog.Builder(this).setTitle(nameV530+" • Seat "+seatNoV530).setItems(actionsV530,(d,w) -> {
            if(w==0) setSeatMicV530(seatNoV530,!micV530);
            else if(w==1) kickSeatV530(seatNoV530,false);
            else confirmKickRoomV530(seatNoV530,uidV530,nameV530);
        }).setNegativeButton("Cancel",null).show();
    }

    private void setSeatMicV530(int seatNoV530, boolean onV530) {
        if(!canModerateSeatsV530() || db==null || roomId==null) { toast("Host / co-host only"); return; }
        db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(seatNoV530)).update("micOn",onV530)
            .addOnSuccessListener(v -> addEvent("seat_mic",(seatNames.get(seatNoV530)==null?"Member":seatNames.get(seatNoV530))+(onV530?" was unmuted":" was muted")))
            .addOnFailureListener(e -> toast("Seat update failed: "+msg(e)));
    }

    private void kickSeatV530(int seatNoV530, boolean silentV530) {
        if(!canModerateSeatsV530() || db==null || roomId==null) { toast("Host / co-host only"); return; }
        String nameV530=seatNames.get(seatNoV530)==null?"Member":seatNames.get(seatNoV530);
        db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(seatNoV530)).delete()
            .addOnSuccessListener(v -> { if(!silentV530)addEvent("seat_kick",nameV530+" was removed from seat "+seatNoV530); })
            .addOnFailureListener(e -> toast("Kick failed: "+msg(e)));
    }

    private void confirmKickRoomV530(int seatNoV530, String uidV530, String nameV530) {
        new AlertDialog.Builder(this).setTitle("Kick "+nameV530+" from room?").setMessage("This removes the member from the current room. It does not ban them.")
            .setNegativeButton("Cancel",null).setPositiveButton("Kick",(d,w) -> kickFromRoomV530(seatNoV530,uidV530,nameV530)).show();
    }

    private void kickFromRoomV530(int seatNoV530, String uidV530, String nameV530) {
        if(!canModerateSeatsV530() || db==null || roomId==null || uidV530==null) { toast("Host / co-host only"); return; }
        kickSeatV530(seatNoV530,true);
        db.collection("live_rooms").document(roomId).collection("members").document(uidV530).delete()
            .addOnSuccessListener(v -> addEvent("room_kick",nameV530+" was kicked from the room"))
            .addOnFailureListener(e -> toast("Room kick failed: "+msg(e)));
    }

    private void inviteMemberToSeatV530(int seatNoV530) {
        if(!canModerateSeatsV530() || db==null || user==null || roomId==null) { toast("Host / co-host only"); return; }
        db.collection("live_rooms").document(roomId).collection("members").get().addOnSuccessListener(snapV530 -> {
            final List<String> uidsV530=new ArrayList<>(); final List<String> namesV530=new ArrayList<>();
            for(DocumentSnapshot docV530:snapV530.getDocuments()){
                String uidV530=docV530.getString("uid"); if(uidV530==null) uidV530=docV530.getId();
                if(uidV530.equals(user.getUid()) || seatUids.containsValue(uidV530)) continue;
                uidsV530.add(uidV530); namesV530.add(str(docV530,"name","Member"));
            }
            if(uidsV530.isEmpty()){toast("No available room members to invite");return;}
            String[] labelsV530=new String[namesV530.size()]; for(int i=0;i<labelsV530.length;i++) labelsV530[i]="👤 "+namesV530.get(i);
            new AlertDialog.Builder(this).setTitle("Invite to Seat "+seatNoV530).setItems(labelsV530,(d,w) -> {
                String uidV530=uidsV530.get(w), nameV530=namesV530.get(w);
                Map<String,Object> inviteV530=new HashMap<>(); inviteV530.put("targetUid",uidV530); inviteV530.put("targetName",nameV530); inviteV530.put("seatId",String.valueOf(seatNoV530)); inviteV530.put("seatNo",seatNoV530);
                inviteV530.put("invitedByUid",user.getUid()); inviteV530.put("invitedByName",safeName()); inviteV530.put("createdAt",FieldValue.serverTimestamp());
                db.collection("live_rooms").document(roomId).collection("seat_invites").document(uidV530).set(inviteV530)
                    .addOnSuccessListener(v -> {toast("Invite sent to "+nameV530);addEvent("seat_invite",safeName()+" invited "+nameV530+" to seat "+seatNoV530);})
                    .addOnFailureListener(e -> toast("Invite failed: "+msg(e)));
            }).setNegativeButton("Cancel",null).show();
        }).addOnFailureListener(e -> toast("Members unavailable: "+msg(e)));
    }

    private void attachV530RealtimeHelpers() {
        if(db==null || user==null || roomId==null) return;
        DocumentReference roomV530=db.collection("live_rooms").document(roomId);
        roleListenerV530=roomV530.collection("roles").document(user.getUid()).addSnapshotListener((docV530,eV530) -> {
            roomModeratorV530 = isOwner() || (docV530!=null && docV530.exists() && "cohost".equals(docV530.getString("role")));
        });
        seatInviteListenerV530=roomV530.collection("seat_invites").document(user.getUid()).addSnapshotListener((docV530,eV530) -> {
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
        liveEmojiListenerV530=roomV530.collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(20).addSnapshotListener((snapV530,eV530) -> {
            if(eV530!=null){toast("Live emoji sync unavailable: "+msg(eV530));return;}if(snapV530==null)return;
            for(DocumentSnapshot docV530:snapV530.getDocuments()){
                if(!"live_emoji".equals(docV530.getString("type")) || seenLiveEmojiEventsV530.contains(docV530.getId())) continue;
                seenLiveEmojiEventsV530.add(docV530.getId()); Object tsV530=docV530.get("createdAt");
                if(tsV530 instanceof com.google.firebase.Timestamp){long ageV530=System.currentTimeMillis()-((com.google.firebase.Timestamp)tsV530).toDate().getTime();if(ageV530>12000)continue;}
                String actorV530=str(docV530,"actorName","User"); String emojiV530=str(docV530,"emoji",str(docV530,"text","😊"));
                if(!user.getUid().equals(docV530.getString("actorUid"))) showLiveEmojiEffect560(emojiV530,actorV530,docV530.getString("actorUid"));
            }
        });
    }

    private void acceptSeatInviteV530(DocumentSnapshot inviteV530, int seatNoV530) {
        if(db==null || user==null || roomId==null) return;
        if(seatUids.get(seatNoV530)!=null && !user.getUid().equals(seatUids.get(seatNoV530))){toast("That seat is no longer available");inviteV530.getReference().delete();return;}
        Map<String,Object> dV530=new HashMap<>();dV530.put("uid",user.getUid());dV530.put("name",safeName());dV530.put("micOn",false);dV530.put("joinedAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(seatNoV530)).set(dV530)
            .addOnSuccessListener(v -> {mySeat=seatNoV530;inviteV530.getReference().delete();addEvent("seat",safeName()+" accepted a seat invite for seat "+seatNoV530);})
            .addOnFailureListener(e -> toast("Could not accept seat invite: "+msg(e)));
    }

    private void toggleMic() {
        if(mySeat<1){toast("Take a mic seat first");return;} if(muteAll&&!isModerator()){toast("Host muted all seats");return;} micOn=!micOn;
        if(cloudRoom&&user!=null&&db!=null) db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(mySeat)).update("micOn",micOn);
        else { prefs.edit().putBoolean("mic_"+roomId,micOn).apply();seatMics.put(mySeat,micOn);rebuildSeats(); }
        refreshMicControl();
    }
    private void refreshMicControl(){
        if(micLabel==null)return;
        micLabel.setText("");android.graphics.drawable.Drawable micArt=getDrawable(micOn?R.drawable.ref_ico_bottom_mic_on_black:R.drawable.ref_ico_bottom_mic_off_black);micArt.setBounds(0,0,dp(24),dp(24));micLabel.setCompoundDrawables(null,micArt,null,null);
        micLabel.setSingleLine(true);
        micLabel.setTextSize(21);
        micLabel.setPadding(0,dp(10),0,0);
        micLabel.setTextColor(micOn?0xff008e6b:0xff666666);
        micLabel.setBackground(bg(micOn?0xffd8f5e9:0xfff0edf5,22));
        micLabel.setContentDescription(micOn?"Microphone on. Tap to mute":"Microphone off. Tap to unmute");
    }
    private void toggleSound(){soundOn=!soundOn;toast(soundOn?"Room sound on":"Room sound muted");}
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
        musicStateListener=room.collection("music_state").document("current").addSnapshotListener((d,e)->{
            if(e!=null||d==null||!d.exists())return;
            String actor=d.getString("actorUid");if(user!=null&&user.getUid().equals(actor))return;
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

    private void openVoice(){NativeMeetBridge.launch(this,roomId,roomName,safeName(),false);}
    private void openVideoRoom(){NativeMeetBridge.launch(this,roomId,roomName,safeName(),true);}

    private void sendMessage(EditText box) {
        String text=box.getText().toString().trim();if(text.isEmpty())return;if(text.length()>500){box.setError("Maximum 500 characters");return;}
        String outgoing=text;if(!pendingReplyText620.isEmpty())outgoing="↪ "+pendingReplyName620+": "+shortChatPreview620(pendingReplyText620)+"\n"+text;final String finalOutgoing=outgoing;
        if(cloudRoom&&user!=null&&db!=null){Map<String,Object>d=new HashMap<>();d.put("senderUid",user.getUid());d.put("senderName",safeName());d.put("text",finalOutgoing);d.put("type","text");d.put("createdAt",FieldValue.serverTimestamp());
            db.collection("live_rooms").document(roomId).collection("messages").add(d).addOnSuccessListener(v->{box.setText("");clearRoomReply620();}).addOnFailureListener(e->toast("Message failed: "+msg(e)));
        }else{appendLocalChat(displayName,finalOutgoing);addChatRow(displayName,finalOutgoing);box.setText("");clearRoomReply620();}
    }
    private void seedLocalChat(){if(cloudRoom)return;String saved=prefs.getString("chat_"+roomId,"");if(!saved.isEmpty())for(String row:saved.split("\\n")){String[]p=row.split("\\|",2);if(p.length==2)addChatRow(p[0],p[1]);}}
    private void appendLocalChat(String who,String text){String old=prefs.getString("chat_"+roomId,"");prefs.edit().putString("chat_"+roomId,old+who.replace("|","")+"|"+text.replace("\n"," ")+"\n").apply();}
    private void addChatRow(String who,String text){
        if(chatBox==null)return;
        boolean follow=roomChatScroll!=null && !roomChatScroll.canScrollVertically(1);
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(9),dp(4),dp(9),dp(5));boolean giftMessage=text!=null&&text.contains("🎁")&&(text.contains(" sent ")||text.contains("Gift"));card.setBackground(bg(giftMessage?0x55b78925:0x20ffffff,12));
        TextView n=tv((giftMessage?"🎁  ":"👤  ")+chatIdentity750(who),11,giftMessage?0xffffdf6b:0xffffedaf,true);n.setPadding(0,0,0,dp(2));
        card.addView(n,new LinearLayout.LayoutParams(-2,-2));
        TextView msg=tv(text,(text!=null&&text.codePointCount(0,text.length())<=6&&text.codePoints().anyMatch(c->c>0x2000))?34:14,Color.WHITE,false);msg.setPadding(0,0,0,0);
        text=stickerFallback610(text);msg.setText(text);int live=LiveEmojiView.parse(text);if(ReferenceEmojiView.parse(text)>=0)card.addView(new ReferenceEmojiView(this,ReferenceEmojiView.parse(text),false),new LinearLayout.LayoutParams(dp(72),dp(72)));else if(stickerResource550(text)!=0)card.addView(stickerImage852(stickerResource550(text)),new LinearLayout.LayoutParams(dp(64),dp(64)));else if(live>=0)card.addView(new LiveEmojiView(this,live,4500),new LinearLayout.LayoutParams(dp(72),dp(72)));else{card.addView(msg,new LinearLayout.LayoutParams(-2,-2));}
        final String actionText620=text;card.setOnLongClickListener(v->{roomChatMessageActions620(who,actionText620);return true;});n.setOnClickListener(v->roomChatMessageActions620(who,actionText620));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,-2);p.setMargins(0,dp(3),dp(24),dp(3));chatBox.addView(card,p);
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
        if(!cloudRoom||db==null){new AlertDialog.Builder(this).setTitle("🎁 Gift Wall").setMessage("Gift Wall becomes live inside a Firebase room. Gifts sent in this test room still appear in chat.").setPositiveButton("Gift Shop",(d,w)->giftShopPanel()).setNegativeButton("Close",null).show();return;}
        db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(250).get().addOnSuccessListener(snap->{Map<String,Long> giftValue=new HashMap<>();Map<String,Integer> giftCount=new HashMap<>();long total=0;int events=0;for(DocumentSnapshot d:snap.getDocuments())if("gift".equals(d.getString("type"))){String g=str(d,"giftIcon","🎁")+" "+str(d,"giftName","Gift");Long v=d.getLong("giftValue");long value=v==null?0:Math.max(0,v);giftValue.put(g,(giftValue.containsKey(g)?giftValue.get(g):0L)+value);giftCount.put(g,(giftCount.containsKey(g)?giftCount.get(g):0)+1);total+=value;events++;}List<Map.Entry<String,Long>> list=new ArrayList<>(giftValue.entrySet());java.util.Collections.sort(list,(a,b)->Long.compare(b.getValue(),a.getValue()));StringBuilder out=new StringBuilder();out.append("Room gifts: ").append(events).append("   •   Value: 💎").append(compactNumber(total)).append("\n\n");int rank=1;for(Map.Entry<String,Long> e:list){out.append(rank++).append(". ").append(e.getKey()).append("  ×").append(giftCount.get(e.getKey())).append("  • 💎").append(compactNumber(e.getValue())).append("\n");if(rank>12)break;}if(events==0)out.append("No gifts yet");new AlertDialog.Builder(this).setTitle("🎁 Gift Wall").setMessage(out.toString()).setPositiveButton("Gift Shop",(d,w)->giftShopPanel()).setNeutralButton("Ranking",(d,w)->roomRankingDialog()).setNegativeButton("Close",null).show();}).addOnFailureListener(e->toast("Gift Wall unavailable: "+msg(e)));
    }

    private void seedLocalFeed(){if(cloudRoom)return;}
    private void addFeed(String text){if(feedBox==null)return;TextView t=tv("✨  "+text,12,0xffffd46b,true);t.setBackground(bg(0x18000000,9));t.setPadding(dp(8),dp(4),dp(8),dp(4));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(2),0,dp(2));feedBox.addView(t,p);}

    private void giftRecipientDialog(){ giftShopPanel(); }
    private void giftDialog(){ giftShopPanel(); }
    private void giftDialogFor(String targetUid,String targetLabel) {
        giftTargetUid=targetUid==null?"":targetUid; giftTargetName=targetLabel==null?"Host":targetLabel; giftShopPanel();
    }
    private void giftShopPanel(){
        if(ownerName==null)ownerName="KING Host";
        if(giftTargetName==null||giftTargetName.isEmpty()){giftTargetName="👑 "+ownerName;giftTargetUid=ownerUid==null?"":ownerUid;}
        giftQuantity=1; giftCategory="Activity"; giftSelectedIndex=0;
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(10),dp(8),dp(10),dp(10));root.setBackgroundColor(0xff181824);
        LinearLayout banner=new LinearLayout(this);banner.setGravity(Gravity.CENTER_VERTICAL);banner.setPadding(dp(10),dp(4),dp(8),dp(4));banner.setBackground(bg(0xff7d36d6,14));
        TextView wt=tv("🎟  Weekly Gift Card",16,Color.WHITE,true);banner.addView(wt,new LinearLayout.LayoutParams(0,dp(42),1));TextView arrow=tv("›",28,Color.WHITE,true);arrow.setGravity(Gravity.CENTER);banner.addView(arrow,new LinearLayout.LayoutParams(dp(40),dp(42)));banner.setOnClickListener(v->{if(giftShopDialog!=null)giftShopDialog.dismiss();weeklyGiftCardDialog();});root.addView(banner,new LinearLayout.LayoutParams(-1,dp(40)));
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);TextView title=tv("Gift",19,Color.WHITE,true);top.addView(title,new LinearLayout.LayoutParams(0,dp(42),1));giftBalanceLabel=pill("💎 "+localCoins,0xff302d40,null);top.addView(giftBalanceLabel,new LinearLayout.LayoutParams(dp(104),dp(38)));root.addView(top);
        giftSelectionLabel=tv("To: "+giftTargetName+"  •  "+giftNames[giftSelectedIndex]+" x"+giftQuantity,12,0xffffdf6b,true);root.addView(giftSelectionLabel);
        HorizontalScrollView peopleScroll=new HorizontalScrollView(this);peopleScroll.setHorizontalScrollBarEnabled(false);LinearLayout people=new LinearLayout(this);people.setGravity(Gravity.CENTER_VERTICAL);people.setPadding(0,dp(3),0,dp(5));addGiftRecipientChip(people,"👑 "+ownerName,ownerUid==null?"":ownerUid);for(int no=1;no<=maxSeats;no++){String n=seatNames.get(no);if(n==null||n.equals(displayName))continue;addGiftRecipientChip(people,no+" • "+n,seatUids.get(no)==null?"":seatUids.get(no));}peopleScroll.addView(people);root.addView(peopleScroll,new LinearLayout.LayoutParams(-1,dp(50)));
        HorizontalScrollView catScroll=new HorizontalScrollView(this);catScroll.setHorizontalScrollBarEnabled(false);LinearLayout cats=new LinearLayout(this);String[] cs={"Activity","Classic","Flying","Relationship","Fame","Privilege","Filters","Parcel"};for(String c:cs){TextView t=tv(c,12,c.equals(giftCategory)?Color.WHITE:0xff9d98a6,c.equals(giftCategory));t.setGravity(Gravity.CENTER);t.setOnClickListener(v->{giftCategory=c;rebuildGiftGrid();});LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(dp(76),dp(38));cp.setMargins(dp(2),0,dp(2),0);cats.addView(t,cp);}catScroll.addView(cats);root.addView(catScroll,new LinearLayout.LayoutParams(-1,dp(42)));
        ScrollView giftsScroll=new ScrollView(this);giftGridBox=new LinearLayout(this);giftGridBox.setOrientation(LinearLayout.VERTICAL);giftsScroll.addView(giftGridBox);root.addView(giftsScroll,new LinearLayout.LayoutParams(-1,0,1));rebuildGiftGrid();
        LinearLayout qty=new LinearLayout(this);qty.setGravity(Gravity.CENTER);int[] qs={1,9,49,99,499};for(int q:qs){TextView b=pill(String.valueOf(q),q==1?0xff776a00:0xff343143,()->{giftQuantity=q;giftSelectionLabel.setText("To: "+giftTargetName+"  •  "+giftNames[giftSelectedIndex]+" x"+giftQuantity);});if(q==1)b.setBackground(bg(0xff5d5520,10));LinearLayout.LayoutParams qp=new LinearLayout.LayoutParams(0,dp(44),1);qp.setMargins(dp(3),0,dp(3),0);qty.addView(b,qp);}TextView send=pill("Send",0xffffea00,()->{int total=giftCosts[giftSelectedIndex]*giftQuantity;sendGiftQuantity(giftTargetUid,giftTargetName,giftNames[giftSelectedIndex],giftIcons[giftSelectedIndex],giftCosts[giftSelectedIndex],giftQuantity,total);if(giftBalanceLabel!=null)giftBalanceLabel.setText("💎 "+localCoins);});send.setTextColor(Color.BLACK);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(dp(68),dp(44));sp.setMargins(dp(5),0,0,0);qty.addView(send,sp);root.addView(qty);
        giftShopDialog=new AlertDialog.Builder(this).setView(root).create();
        giftShopDialog.setOnShowListener(v->{android.view.Window w=giftShopDialog.getWindow();if(w!=null){w.setBackgroundDrawableResource(android.R.color.transparent);w.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);w.setGravity(Gravity.BOTTOM);w.setLayout(-1,(int)(getResources().getDisplayMetrics().heightPixels*.58f));}});giftShopDialog.show();
    }
    private void addGiftRecipientChip(LinearLayout row,String label,String uid){
        TextView chip=pill(label,0xff343143,()->{giftTargetName=label;giftTargetUid=uid==null?"":uid;if(giftSelectionLabel!=null)giftSelectionLabel.setText("To: "+giftTargetName+" • "+giftNames[giftSelectedIndex]+" x"+giftQuantity);});
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(130),dp(42));p.setMargins(dp(2),0,dp(2),0);row.addView(chip,p);
    }
    private void rebuildGiftGrid(){
        if(giftGridBox==null)return;giftGridBox.removeAllViews();LinearLayout row=null;int col=0;boolean chosen=false;
        for(int i=0;i<giftNames.length;i++){
            if(!giftCategory.equals(giftCategories[i]))continue;if(!chosen){giftSelectedIndex=i;chosen=true;if(giftSelectionLabel!=null)giftSelectionLabel.setText("To: "+giftTargetName+"  •  "+giftNames[giftSelectedIndex]+" x"+giftQuantity);}
            if(row==null||col==4){row=new LinearLayout(this);row.setGravity(Gravity.CENTER);giftGridBox.addView(row,new LinearLayout.LayoutParams(-1,dp(108)));col=0;}
            final int idx=i;final int vipNeed=giftVipRequired[i];final int myVip=LevelSystem.read(this).vipLevel;final boolean unlocked=myVip>=vipNeed;LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setBackground(bg(unlocked?0xff20202d:0xff171720,12));card.setPadding(dp(2),dp(4),dp(2),dp(2));
            TextView nw=tv(vipNeed>0?(unlocked?"VIP ✓":"VIP "+vipNeed):"",9,unlocked?0xff68e89d:0xffffb04f,true);nw.setGravity(Gravity.RIGHT);card.addView(nw,new LinearLayout.LayoutParams(-1,dp(16)));
            TextView icon=tv(unlocked?giftIcons[i]:"🔒",34,unlocked?Color.WHITE:0xff77727e,false);icon.setGravity(Gravity.CENTER);card.addView(icon,new LinearLayout.LayoutParams(-1,dp(42)));
            TextView name=tv(giftNames[i],9,unlocked?0xfff5f2f7:0xff817b88,true);name.setGravity(Gravity.CENTER);card.addView(name,new LinearLayout.LayoutParams(-1,dp(22)));
            TextView cost=tv("◇ "+giftCosts[i],9,0xff9a97a5,false);cost.setGravity(Gravity.CENTER);card.addView(cost,new LinearLayout.LayoutParams(-1,dp(20)));
            card.setOnClickListener(v->{if(!unlocked){toast("Unlocks at VIP "+vipNeed);return;}giftSelectedIndex=idx;if(giftSelectionLabel!=null)giftSelectionLabel.setText("To: "+giftTargetName+"  •  "+giftNames[giftSelectedIndex]+" x"+giftQuantity);});LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(102),1);cp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(card,cp);col++;
        }
        if(!chosen){TextView none=tv("No gifts in this category yet",13,MUTED,false);giftGridBox.addView(none);}
    }
    private void sendGift(String gift,int cost) { sendGiftTo(ownerUid==null?"":ownerUid,ownerName,gift,cost); }
    private void sendGiftTo(String targetUid,String targetName,String gift,int cost) {
        sendGiftQuantity(targetUid,targetName,gift,"🎁",cost,1,cost);
    }
    private void sendGiftQuantity(String targetUid,String targetName,String gift,String icon,int unitCost,int quantity,int totalCost){
        int idx=-1;for(int i=0;i<giftNames.length;i++)if(giftNames[i].equals(gift)){idx=i;break;}if(idx>=0&&LevelSystem.read(this).vipLevel<giftVipRequired[idx]){toast("Gift unlocks at VIP "+giftVipRequired[idx]);return;}
        if(quantity<1||totalCost<1)return;
        String label=icon+" "+gift+" x"+quantity;
        if(cloudRoom&&totalCost>50000000){toast("Live gift total is limited to 50000000 coins • choose a smaller quantity");return;}
        if(cloudRoom&&user!=null&&targetUid!=null&&!targetUid.isEmpty()&&!targetUid.equals(user.getUid())){
            CloudBackend.sendGift(targetUid,gift+" x"+quantity,totalCost,(ok,message)->runOnUiThread(()->{
                toast(message);
                if(ok)addGiftEvent(targetUid,targetName,gift,icon,unitCost,quantity,totalCost);
            }));return;
        }
        if(localCoins<totalCost){toast("Not enough TEST coins • need "+totalCost);return;}
        localCoins-=totalCost;prefs.edit().putInt("coins",localCoins).apply();
        addGiftEvent(targetUid,targetName,gift,icon,unitCost,quantity,totalCost);
        showGiftEffect(safeName(),targetName,gift,icon,quantity,totalCost);
        if(supporterLabel!=null)supporterLabel.setText("🏆 Top supporter • "+safeName()+" • "+totalCost+" gift coins");
        toast(label+" sent • TEST balance "+localCoins);
    }
    private void addGiftEvent(String targetUid,String targetName,String gift,String icon,int unitCost,int quantity,int totalCost){
        String text=safeName()+" sent "+icon+" "+gift+" to "+targetName+" 🎁 x"+quantity;
        LevelSystem.Snapshot progress700=LevelSystem.read(this);
        if(cloudRoom&&user!=null&&db!=null&&roomId!=null){
            Map<String,Object>d=new HashMap<>();d.put("actorUid",user.getUid());d.put("actorName",safeName());d.put("actorLevel",progress700.level);d.put("actorVip",progress700.vipLevel);d.put("type","gift");d.put("text",text);d.put("giftName",gift);d.put("giftIcon",icon);d.put("giftValue",totalCost);d.put("giftQty",quantity);d.put("giftUnitCost",unitCost);d.put("targetUid",targetUid==null?"":targetUid);d.put("targetName",targetName==null?"User":targetName);d.put("createdAt",FieldValue.serverTimestamp());
            DocumentReference room=db.collection("live_rooms").document(roomId);room.collection("events").add(d);
            Map<String,Object>m=new HashMap<>();m.put("senderUid",user.getUid());m.put("senderName",safeName());m.put("senderLevel",progress700.level);m.put("senderVip",progress700.vipLevel);m.put("text",text);m.put("type","gift");m.put("giftName",gift);m.put("giftIcon",icon);m.put("giftValue",totalCost);m.put("giftQty",quantity);m.put("targetUid",targetUid==null?"":targetUid);m.put("targetName",targetName==null?"User":targetName);m.put("createdAt",FieldValue.serverTimestamp());room.collection("messages").add(m);
        }else{addFeed(text);appendLocalChat(displayName,text);addChatRow(displayName,text);}
        awardGiftProgress700(totalCost);
    }
    private void awardGiftProgress700(long value){LevelSystem.Snapshot before=LevelSystem.read(this),after=LevelSystem.gift(this,value);if(after.level>before.level)toast("⭐ Level up • Lv."+after.level);if(after.vipLevel>before.vipLevel)toast("💎 VIP up • VIP "+after.vipLevel);syncMyProgress700(after);}
    private void syncMyProgress700(LevelSystem.Snapshot p){if(user==null||db==null)return;Map<String,Object>m=new HashMap<>();m.put("uid",user.getUid());m.put("displayName",safeName());m.put("searchName",safeName().toLowerCase(java.util.Locale.US));m.put("publicId",publicId(user.getUid()));m.put("level",p.level);m.put("xp",p.xp);m.put("vipLevel",p.vipLevel);m.put("vipPoints",p.vipPoints);m.put("levelTier",LevelSystem.levelTier(p.level));m.put("updatedAt",FieldValue.serverTimestamp());db.collection("public_profiles").document(user.getUid()).set(m,SetOptions.merge());}

    private void shareRoom(){Intent s=new Intent(Intent.ACTION_SEND);s.setType("text/plain");s.putExtra(Intent.EXTRA_TEXT,"Join my KING Plus Party: "+roomName+" • Room ID "+shortId());startActivity(Intent.createChooser(s,"Share Party"));}
    private void pkBattle(){
        if(db==null||!cloudRoom){int a=10+new java.util.Random().nextInt(91),b=10+new java.util.Random().nextInt(91);new AlertDialog.Builder(this).setTitle("⚔ Room Battle").setMessage("Team KING  "+a+"  vs  Guest  "+b+"\n\n"+(a>=b?"🏆 Team KING wins":"🏆 Guest wins")).setPositiveButton("Again",(d,w)->pkBattle()).setNegativeButton("Close",null).show();return;}
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
        String[] icons={"🎟","🪑","✋","💬","🎵","🎨","📋","👑","🏰","🛡","📊","🎤","📻","🎁","💫","🔎","📢","🎙","📹","🎁","🏆","🗨","🎒","🌐","💞","⚔️","🔁"};
        String[] labels={"Events","Room seat",queueOn?"Queue ON":"Enable queue","Private chat","Music","Atmosphere","Income","Share Family","Theme Room","Party Master","Party Data","KTV Queue","Radio Mic","Lucky Gift","Gift Wish","Find User","Notice","Voice Room","Multi Video","Gift Wall","Gift Rank","Chat History","Backpack","Community","Relationship","Audio PK","Loop Mic","Room Games","Asset Pack"};
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
        else if("Voice Room".equals(label))openVoice();
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
        gameStateListener=room.collection("game_state").document("current").addSnapshotListener((doc,e)->{
            if(e!=null||doc==null)return;
            if(!doc.exists()){activeGameRound=0L;activeGameType="";activeGamePrompt="";activeGameResult="";gameSnapshotReady=true;return;}
            Long rawRound=doc.getLong("roundId");long round=rawRound==null?0L:rawRound;
            String type=str(doc,"type","game"),prompt=str(doc,"prompt",""),result=str(doc,"result",""),status=str(doc,"status","active");
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
        reactionBanner.setText(text);reactionBanner.setTextSize(15);reactionBanner.setAlpha(1f);reactionBanner.setVisibility(View.VISIBLE);reactionBanner.setBackground(bg(0xcc38265d,28));
        reactionBanner.postDelayed(()->{if(reactionBanner!=null)reactionBanner.animate().alpha(0f).setDuration(600).withEndAction(()->{if(reactionBanner!=null){reactionBanner.setVisibility(View.GONE);reactionBanner.setAlpha(1f);reactionBanner.setTextSize(42);reactionBanner.setBackground(bg(0x663c1f58,30));}}).start();},2600);
    }

    private void publishGameState(String type,String prompt,String result){publishGameState(type,prompt,result,null);}
    private void publishGameState(String type,String prompt,String result,Map<String,Object> extras){
        long round=System.currentTimeMillis();
        if(!cloudRoom||db==null||user==null){activeGameRound=round;activeGameType=type;activeGamePrompt=prompt;activeGameResult=result;showGameStateBanner(type,prompt,result);addEvent("game",safeName()+" started "+prompt);return;}
        if(!isModerator()){toast("Host/co-host starts room games");return;}
        Map<String,Object>d=new HashMap<>();d.put("actorUid",user.getUid());d.put("actorName",safeName());d.put("type",type);d.put("prompt",prompt==null?"":prompt);d.put("result",result==null?"":result);d.put("status","active");d.put("roundId",round);d.put("updatedAt",FieldValue.serverTimestamp());if(extras!=null)d.putAll(extras);
        db.collection("live_rooms").document(roomId).collection("game_state").document("current").set(d)
            .addOnSuccessListener(v->{activeGameRound=round;activeGameType=type;activeGamePrompt=prompt==null?"":prompt;activeGameResult=result==null?"":result;addEvent("game",safeName()+" started "+(prompt==null?type:prompt));})
            .addOnFailureListener(e->toast("Game start failed: "+msg(e)));
    }

    private void closeActiveGame(){
        if(!cloudRoom||db==null||user==null||!isModerator()){toast("Host/co-host only");return;}
        Map<String,Object>u=new HashMap<>();u.put("status","closed");u.put("actorUid",user.getUid());u.put("updatedAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).collection("game_state").document("current").update(u).addOnSuccessListener(v->{addEvent("game",safeName()+" closed the room game");toast("Game closed");}).addOnFailureListener(e->toast(msg(e)));
    }

    private void startNewVoteDialog(){
        if(!canStartRoomGame("Vote"))return;
        final EditText input=new EditText(this);input.setHint("Vote question");input.setText("Do you like this Party Room?");
        new AlertDialog.Builder(this).setTitle("💌 Start Room Vote").setView(input).setPositiveButton("Start",(d,w)->{
            String q=input.getText().toString().trim();if(q.isEmpty())q="Room vote";
            ArrayList<String> opts=new ArrayList<>();opts.add("👍 Yes");opts.add("👎 No");opts.add("❤️ Love it");opts.add("😂 Funny");Map<String,Object>x=new HashMap<>();x.put("options",opts);publishGameState("vote",q,"",x);
        }).setNegativeButton("Cancel",null).show();
    }

    private void submitCurrentVote(){
        if(!cloudRoom||db==null||user==null){localVotePanel();return;}
        db.collection("live_rooms").document(roomId).collection("game_state").document("current").get().addOnSuccessListener(doc->{
            if(!doc.exists()||!"vote".equals(doc.getString("type"))||"closed".equals(doc.getString("status"))){toast("No active room vote");return;}
            Long r=doc.getLong("roundId");long round=r==null?0:r;String question=str(doc,"prompt","Room vote");ArrayList<String> options=new ArrayList<>();Object raw=doc.get("options");if(raw instanceof List)for(Object o:(List<?>)raw)if(o!=null)options.add(String.valueOf(o));if(options.isEmpty()){options.add("👍 Yes");options.add("👎 No");options.add("❤️ Love it");options.add("😂 Funny");}
            String[] a=options.toArray(new String[0]);new AlertDialog.Builder(this).setTitle("💌 "+question).setItems(a,(d,w)->{
                Map<String,Object>vote=new HashMap<>();vote.put("uid",user.getUid());vote.put("name",safeName());vote.put("roundId",round);vote.put("choice",a[w]);vote.put("choiceIndex",w);vote.put("updatedAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).collection("game_votes").document(user.getUid()).set(vote).addOnSuccessListener(v->{addEvent("vote",safeName()+" voted "+a[w]);toast("Vote sent: "+a[w]);}).addOnFailureListener(e->toast("Vote failed: "+msg(e)));
            }).setNeutralButton(isModerator()?"Results":"Close",(d,w)->{if(isModerator())showVoteResults();}).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast("Vote unavailable: "+msg(e)));
    }

    private void showVoteResults(){
        if(!cloudRoom||db==null){toast("Live room required");return;}
        db.collection("live_rooms").document(roomId).collection("game_state").document("current").get().addOnSuccessListener(state->{
            if(!state.exists()||!"vote".equals(state.getString("type"))){toast("No active vote");return;}Long rr=state.getLong("roundId");long round=rr==null?0:rr;String question=str(state,"prompt","Room vote");
            db.collection("live_rooms").document(roomId).collection("game_votes").get().addOnSuccessListener(snap->{Map<String,Integer>counts=new HashMap<>();int total=0;for(DocumentSnapshot v:snap.getDocuments()){Long r=v.getLong("roundId");if(r==null||r!=round)continue;String c=str(v,"choice","Vote");counts.put(c,counts.containsKey(c)?counts.get(c)+1:1);total++;}StringBuilder out=new StringBuilder(question).append("\n\n");String[] known={"👍 Yes","👎 No","❤️ Love it","😂 Funny"};for(String k:known)out.append(k).append("  •  ").append(counts.containsKey(k)?counts.get(k):0).append('\n');out.append("\nTotal votes: ").append(total);new AlertDialog.Builder(this).setTitle("💌 Vote Results").setMessage(out.toString()).setPositiveButton("Vote",(d,w)->submitCurrentVote()).setNeutralButton(isModerator()?"Close vote":null,(d,w)->{if(isModerator())closeActiveGame();}).setNegativeButton("Close",null).show();}).addOnFailureListener(e->toast(msg(e)));
        });
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
        String[] items={"✨ Classic","💙 KTV","💗 Love","🎮 Game","👑 Royal"};
        new AlertDialog.Builder(this).setTitle("🎨 Atmosphere • "+roomTheme).setItems(items,(d,w)->{
            if(!isOwner()){toast("Host controls the room atmosphere");return;}
            String[] raw={"Classic","KTV","Love","Game","Royal","Neon","Galaxy","Festival","Ice"};roomTheme=raw[w];if(cloudRoom)setRoomValue("theme",roomTheme);else prefs.edit().putString("theme_"+roomName,roomTheme).apply();renderParty();
        }).setNegativeButton("Close",null).show();
    }

    private void themeRoomPanel(){atmospherePanel();}

    private void shareFamilyRoom(){
        Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,"👑 Join my KING Plus family room • "+roomName+" • Room ID "+shortId());startActivity(Intent.createChooser(i,"Share Family"));
    }

    private void counterPanel(){giftSendingCounterDialog();}

    private void guessItPanel(){
        if(!cloudRoom||db==null||user==null){final int answer=1+new java.util.Random().nextInt(9);final EditText input=new EditText(this);input.setHint("Guess 1 to 9");input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);new AlertDialog.Builder(this).setTitle("🌷 Guess It").setView(input).setPositiveButton("Guess",(d,w)->{int g=-1;try{g=Integer.parseInt(input.getText().toString().trim());}catch(Exception ignored){}String r=g==answer?"🎉 Correct!":"Answer was "+answer;addEvent("play",safeName()+" played Guess It • "+r);toast(r);}).setNegativeButton("Close",null).show();return;}
        if(isModerator()){int answer=1+new java.util.Random().nextInt(9);Map<String,Object>x=new HashMap<>();x.put("answer",answer);publishGameState("guess","Guess a number from 1 to 9","",x);new AlertDialog.Builder(this).setTitle("🌷 Guess It").setMessage("Room game started. Everyone can open Guess It and submit a guess.").setPositiveButton("OK",null).show();return;}
        db.collection("live_rooms").document(roomId).collection("game_state").document("current").get().addOnSuccessListener(doc->{if(!doc.exists()||!"guess".equals(doc.getString("type"))||"closed".equals(doc.getString("status"))){toast("Host/co-host has not started Guess It");return;}Long a=doc.getLong("answer");int answer=a==null?-1:a.intValue();final EditText input=new EditText(this);input.setHint("Guess 1 to 9");input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);new AlertDialog.Builder(this).setTitle("🌷 "+str(doc,"prompt","Guess It")).setView(input).setPositiveButton("Guess",(d,w)->{int g=-1;try{g=Integer.parseInt(input.getText().toString().trim());}catch(Exception ignored){}String r=g==answer?"🎉 Correct!":"❌ Try again";addEvent("guess",safeName()+" guessed "+g+" • "+r);toast(r);}).setNegativeButton("Close",null).show();});
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

    private void votePanel(){
        if(!cloudRoom||db==null||user==null){localVotePanel();return;}
        if(!isModerator()){submitCurrentVote();return;}
        String current="vote".equals(activeGameType)&&activeGameRound>0?"Current: "+activeGamePrompt:"No active vote";new AlertDialog.Builder(this).setTitle("💌 Room Vote • "+current).setItems(new String[]{"Start new vote","Vote now","View results","Close active vote"},(d,w)->{if(w==0)startNewVoteDialog();else if(w==1)submitCurrentVote();else if(w==2)showVoteResults();else closeActiveGame();}).setNegativeButton("Close",null).show();
    }

    private TextView toolTile(String icon,String label,Runnable action){
        android.text.SpannableString title=new android.text.SpannableString(icon+"\n\n"+label);
        title.setSpan(new android.text.style.AbsoluteSizeSpan(24,true),0,icon.length(),android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextView t=tv("",10,Color.WHITE,false);t.setText(title);t.setGravity(Gravity.CENTER);t.setBackground(bg(0x123f3d53,16));t.setOnClickListener(v->action.run());t.setPadding(dp(2),dp(6),dp(2),dp(6));t.setContentDescription(label);return t;
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
        if("VIP".equals(category)&&prefs.getInt("vip_level",0)<1){toast("VIP sticker is locked in this no-billing build");return;}
        if(composerBox!=null){composerBox.setText(value);sendMessage(composerBox);}
        showReactionEffect(value);addEvent("sticker",safeName()+" sent "+category+" sticker "+value);
    }

    private void showReactionEffect(String emoji){
        final TextView banner=reactionBanner;if(banner==null||partyUiDead())return;
        try{
            banner.animate().cancel();banner.clearAnimation();banner.setTranslationX(0f);banner.setAlpha(1f);banner.setVisibility(View.VISIBLE);
            banner.setText((emoji==null?"✨":emoji)+"   "+safeName());
            banner.postDelayed(()->{
                final TextView current=reactionBanner;if(current==null||partyUiDead())return;
                try{current.animate().cancel();current.animate().alpha(0f).setDuration(500).withEndAction(()->{if(reactionBanner==current&&!partyUiDead()){current.setVisibility(View.GONE);current.setAlpha(1f);current.setTranslationX(0f);}}).start();}
                catch(Exception e){android.util.Log.e("KINGPlusParty","Reaction animation failed",e);}
            },1400);
        }catch(Exception e){android.util.Log.e("KINGPlusParty","Reaction effect failed",e);}
    }
    private void showEntranceEffect(String name){LevelSystem.Snapshot p720=LevelSystem.read(this);showEntranceEffect730(name,p720.level,p720.vipLevel,KingCosmetics.effect(this),KingCosmetics.frame(this));}
    private void showEntranceEffect720(String name,int level,int vip){showEntranceEffect730(name,level,vip,vip>=10?"Royal Arrival":vip>=7?"Galaxy Portal":vip>=5?"Rose Shower":vip>=3?"Crown Drop":"Welcome Sparkle",vip>=10?"Crown Frame":vip>=7?"Galaxy Frame":vip>=4?"Royal Frame":"Minimal Frame");}
    private void showEntranceEffect730(String name,int level,int vip,String effect,String frame){if(partyUiDead())return;loadRoomPersonalSettings();if(skipEntranceVisuals||"None".equals(effect)||liveEmojiStageV530==null)return;if(!skipEntranceSound)KingSoundFx.join(this);
        try{KingEntranceView fx=new KingEntranceView(this,name,effect,frame);FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-1,-1);liveEmojiStageV530.addView(fx,lp);fx.start(()->{try{if(liveEmojiStageV530!=null&&fx.getParent()==liveEmojiStageV530)liveEmojiStageV530.removeView(fx);}catch(Exception ignored){}});}catch(Exception e){android.util.Log.e("KINGPlusParty","Entrance overlay failed",e);}
        if(reactionBanner==null||partyUiDead())return;try{reactionBanner.animate().cancel();reactionBanner.clearAnimation();String badge=(vip>0?" 💎VIP "+vip:"")+(level>0?" • Lv."+level:"");reactionBanner.setText(KingCosmetics.effectEmoji(effect)+" "+(name==null?"Guest":name)+badge+" • "+frame);reactionBanner.setTextSize(vip>=7?18:17);reactionBanner.setAlpha(1f);reactionBanner.setVisibility(View.VISIBLE);reactionBanner.setBackground(KingCosmetics.idFrame(this,frame));reactionBanner.setTranslationY(-dp(60));reactionBanner.animate().translationY(0).setDuration(360).start();reactionBanner.postDelayed(()->{if(reactionBanner!=null&&!partyUiDead())reactionBanner.animate().translationY(-dp(40)).alpha(0f).setDuration(480).withEndAction(()->{if(reactionBanner!=null&&!partyUiDead()){reactionBanner.setTranslationY(0);reactionBanner.setVisibility(View.GONE);reactionBanner.setAlpha(1f);reactionBanner.setTextSize(42);reactionBanner.setBackground(bg(0x663c1f58,30));}}).start();},2100);}catch(Exception e){android.util.Log.e("KINGPlusParty","Entrance banner failed",e);}
    }
    private void showGiftEffect(String actor,String target,String gift,String icon,long qty,long value){if(partyUiDead())return;KingSoundFx.gift(this);
        String a=(actor==null||actor.isEmpty())?"User":actor;String t=(target==null||target.isEmpty())?"Host":target;String g=(gift==null||gift.isEmpty())?"Gift":gift;String ic=(icon==null||icon.isEmpty())?"🎁":icon;
        long now=System.currentTimeMillis();if(a.equals(lastGiftActor720)&&g.equals(lastGiftName720)&&now-lastGiftAt720<7000)giftCombo720++;else giftCombo720=1;lastGiftActor720=a;lastGiftName720=g;lastGiftAt720=now;
        long total=Math.max(0,value);String tier=total>=50000?"MYTHIC":total>=10000?"ROYAL":total>=2500?"PREMIUM":"GIFT";int accent=total>=50000?0xffff2fa6:total>=10000?0xffffc13a:total>=2500?0xff38c8ff:0xff9a67ff;
        String title=tier+" • "+ic+" "+g+(giftCombo720>1?"  COMBO ×"+giftCombo720:"");String sub=a+" → "+t+" • x"+Math.max(1,qty)+(total<=0?" • FREE":" • 💎"+compactNumber(total));
        if(liveEmojiStageV530!=null&&!partyUiDead()){try{GiftBurstView fx=new GiftBurstView(this,ic,title,sub,accent);FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-1,-1);liveEmojiStageV530.addView(fx,lp);long duration=total>=50000?4300:total>=10000?3600:2900;fx.start(()->{try{if(liveEmojiStageV530!=null&&fx.getParent()==liveEmojiStageV530)liveEmojiStageV530.removeView(fx);}catch(Exception ignored){}},duration);}catch(Exception e){android.util.Log.e("KINGPlusParty","Gift burst failed",e);}}
        if(reactionBanner!=null&&!partyUiDead()){try{reactionBanner.animate().cancel();reactionBanner.setText(ic+"  "+a+" → "+t+"  "+g+" x"+Math.max(1,qty)+(giftCombo720>1?"  🔥x"+giftCombo720:"")+(total<=0?"  FREE":"  💎"+compactNumber(total)));reactionBanner.setTextSize(16);reactionBanner.setAlpha(1f);reactionBanner.setVisibility(View.VISIBLE);reactionBanner.setBackground(bg(accent,28));reactionBanner.postDelayed(()->{if(reactionBanner!=null&&!partyUiDead())try{reactionBanner.animate().cancel();reactionBanner.animate().alpha(0f).setDuration(500).withEndAction(()->{if(reactionBanner!=null&&!partyUiDead()){reactionBanner.setVisibility(View.GONE);reactionBanner.setAlpha(1f);reactionBanner.setTextSize(42);reactionBanner.setBackground(bg(0x663c1f58,30));}}).start();}catch(Exception ignored){}},2000);}catch(Exception e){android.util.Log.e("KINGPlusParty","Gift banner failed",e);}}
    }
    private void refreshSupporters(com.google.firebase.firestore.QuerySnapshot snap){
        if(snap==null)return;Map<String,Long> score=new HashMap<>();long totalGift=0;long activity=Math.max(1,snap.size());
        for(DocumentSnapshot d:snap.getDocuments())if("gift".equals(d.getString("type"))){String n=str(d,"actorName","User");Long raw=d.getLong("giftValue");long v=raw==null?1L:Math.max(1L,raw);totalGift+=v;score.put(n,score.containsKey(n)?score.get(n)+v:v);}
        int roomLv=(int)Math.min(99,1+(activity/12)+(totalGift/25000));int heartLv=(int)Math.min(99,1+(totalGift/10000));String compact=shortId();if(compact.length()>2)compact=compact.substring(compact.length()-2);
        if(roomLevelLabel!=null)roomLevelLabel.setText("🔷 Lv."+roomLv+" • No."+compact);if(heartLevelLabel!=null)heartLevelLabel.setText("💗 Heart Lv."+heartLv);
        if(supporterLabel==null)return;List<Map.Entry<String,Long>> list=new ArrayList<>(score.entrySet());java.util.Collections.sort(list,(a,b)->Long.compare(b.getValue(),a.getValue()));
        if(list.isEmpty()){supporterLabel.setText("🏆 Top supporters • waiting for gifts");return;}
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
            addActivityTiles(body,new String[]{"💞","🍒","🐔","🐯","🪄","🏎️","🍬","🎰"},new String[]{"Relationship","Crazy Fruits","Chicken Run","Animal PK","Magic Lamp","Forza Motorsport","Candy Legend","Lucky Slot"},false);
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
        if(name.contains("Mobile Legends")||name.contains("PUBG")||name.contains("Free Fire")){openRoomGames740();return;}
        new AlertDialog.Builder(this).setTitle(name).setMessage("Template is active in this room. Members can chat, take seats, send gifts and join room activities.").setPositiveButton("OK",null).show();
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
        if("Forza Motorsport".equals(name)){simpleRaceEvent();return;}
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
        String[] cars={"🏎️ Red","🚙 Blue","🚗 Gold","🏁 KING"};String win=cars[new java.util.Random().nextInt(cars.length)];addEvent("play",safeName()+" started Forza Motorsport • winner "+win);
        new AlertDialog.Builder(this).setTitle("🏎️ Forza Motorsport").setMessage("Winner: "+win).setPositiveButton("Race again",(d,w)->simpleRaceEvent()).setNegativeButton("Close",null).show();
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
        if(!cloudRoom||db==null){new AlertDialog.Builder(this).setTitle("Administrator").setMessage("Live room administrators are available in Firebase rooms.").setPositiveButton("OK",null).show();return;}
        db.collection("live_rooms").document(roomId).collection("roles").get().addOnSuccessListener(snap->{
            List<String> rows=new ArrayList<>();rows.add("👑 Channel Host • "+(ownerName==null?"Host":ownerName));int admins=0;
            for(DocumentSnapshot d:snap.getDocuments())if("cohost".equals(d.getString("role"))){admins++;rows.add("🛡 Administrator • "+memberNameForUid(d.getId()));}
            rows.add("Administrator ("+admins+"/5) • Permissions");if(isOwner()&&admins<5)rows.add("＋ Add administrator");final int count=admins;
            new AlertDialog.Builder(this).setTitle("Administrator").setItems(rows.toArray(new String[0]),(x,w)->{String item=rows.get(w);if(item.startsWith("＋"))chooseAdministrator(count);else if(item.contains("Permissions"))new AlertDialog.Builder(this).setTitle("Administrator permissions").setMessage("Administrators can moderate members, manage mic seats, seat locks, mute-all and announcements. Only the room host can change ownership-sensitive settings.").setPositiveButton("OK",null).show();}).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast(msg(e)));
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
        new AlertDialog.Builder(this).setTitle("From the channel • "+roomName).setItems(items,(d,w)->{String x=items[w];if(x.contains("Members"))membersDialog();else if(x.contains("Party & Live"))roomInfoDialog();else if(x.contains("Billboard"))roomBillboardDialog();else if(x.contains("theme"))themeRoomPanel();else if(x.contains("profile"))hostProfileDialog();else if(x.contains("Robot"))roomRobotDialog();else if(x.contains("Administrator"))administratorDialog();else if(x.contains("settings"))roomSettingsPanel();else reportHost();}).setNegativeButton("Close",null).show();
    }

    private void roomRobotDialog(){
        String[] cmds={"Welcome new members","Post safety reminder","Start a room vote","Open Templates & Events"};
        new AlertDialog.Builder(this).setTitle("🤖 KING Room Robot").setItems(cmds,(d,w)->{if(w==0)addEvent("robot","🤖 Welcome to "+roomName+"! Be kind and enjoy the party.");else if(w==1)addEvent("robot","🤖 Safety reminder: keep the room respectful and report prohibited content.");else if(w==2)votePanel();else activityCenterPanel();}).setNegativeButton("Close",null).show();
    }

    private GradientDrawable premiumRoomBackground(){
        int a=0xff07956f,b=0xff04b4a3,c=0xff062f38;
        if("KTV".equals(roomTheme)){a=0xff15245e;b=0xff1b5c88;c=0xff07182f;}
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
        String access=roomPrivate?(roomHasPassword?"🔑 Password":"🔐 Private"):"🌐 Public";
        String role=isOwner()?"👑 Host":(coHost?"🛡 Co-host":"👤 Member");
        String lock=roomLocked?"🔒 Seats locked":"🔓 Seats open";
        String mute=muteAll?"🔇 Mute all":"🎤 Voice open";
        String req=isModerator()?" • 📥 "+pendingSeatRequests:"";
        int occupied=seatUids.isEmpty()?seatNames.size():seatUids.size();
        roomStateLabel.setText(access+"  •  "+role+"  •  "+lock+"  •  "+mute+"  •  🎙 "+occupied+"/"+maxSeats+req);
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
        if(!isModerator()||!cloudRoom||db==null)return;
        if(seatUids.get(targetSeat)!=null){toast("Seat is already occupied");return;}
        WriteBatch batch=db.batch();
        if(currentSeat>0)batch.delete(db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(currentSeat)));
        else for(Map.Entry<Integer,String> e:new HashMap<>(seatUids).entrySet())if(uid.equals(e.getValue()))batch.delete(db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(e.getKey())));
        Map<String,Object> data=new HashMap<>();data.put("uid",uid);data.put("name",name);data.put("micOn",false);data.put("joinedAt",FieldValue.serverTimestamp());
        batch.set(db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(targetSeat)),data);
        batch.commit().addOnSuccessListener(v->{addEvent("seat",name+" moved to seat "+targetSeat);toast(name+" → Seat "+targetSeat);})
            .addOnFailureListener(e->toast("Move failed: "+msg(e)));
    }

    private void grantCurrentMembersAccessThen(Runnable done){
        if(!cloudRoom||db==null||user==null){done.run();return;}
        WriteBatch batch=db.batch();Set<String> ids=new HashSet<>(memberUids.values());ids.add(user.getUid());
        for(String uid:ids){
            if(uid==null||uid.isEmpty())continue;
            Map<String,Object> grant=new HashMap<>();grant.put("uid",uid);grant.put("grantedBy",user.getUid());grant.put("createdAt",FieldValue.serverTimestamp());
            batch.set(db.collection("live_rooms").document(roomId).collection("access").document(uid),grant);
        }
        batch.commit().addOnSuccessListener(v->done.run()).addOnFailureListener(e->toast("Could not preserve current member access: "+msg(e)));
    }

    private void roomMenu() {
        List<String> items=new ArrayList<>();
        items.add("ℹ Room info"); items.add("👤 Host profile"); items.add("💬 Private chat"); items.add("🎯 Templates & Events"); items.add("🎟 Weekly Gift Card");
        items.add("▣ Billboard"); items.add("👥 Channel / Group"); items.add("🛡 Administrator"); items.add("⚙ Room settings");
        items.add("🎙 Open voice room"); items.add("📹 Multi Video"); items.add("🎵 Song request"); items.add("😊 Reaction");
        items.add("👥 Members"); items.add("🏆 Room ranking"); items.add("🎁 Gift history"); items.add("🕘 Recent activity");
        items.add("🎮 Games"); items.add("⚔ PK battle"); items.add("🧭 Full feature center");
        items.add("🛡 Party Master"); items.add("📊 Party Data"); items.add("🎤 KTV Queue"); items.add("📻 Radio Mic Queue");
        items.add("🎁 Lucky Gift"); items.add("💫 Gift Wish"); items.add("🔎 Find room user"); items.add("📢 Notice center");
        items.add("💫 Interactive Emoji"); items.add("🎧 Audio PK"); items.add("🎁 Gift Wall"); items.add("🔍 Online User Search");
        items.add("💞 Family Party"); items.add("📣 Friend Broadcast"); items.add("📚 Party Master Help");
        items.add("🔗 Share room"); items.add("🔔 Invite by Firebase UID"); items.add("⚑ Report host"); items.add("🚫 Block host");
        if(isModerator()){
            items.add("🛡 Host controls"); items.add("📥 Seat request center"); items.add("🚫 Banned users");
            items.add(roomLocked?"🔓 Unlock seats":"🔒 Lock seats"); items.add(muteAll?"🎤 Unmute all seats":"🔇 Mute all seats"); items.add("📢 Edit announcement");
        }
        if(isOwner()){
            items.add("👑 Co-host list"); items.add("✏ Rename room"); items.add("🗂 Change category"); items.add("🎨 Room theme"); items.add("🎙 Change mic seats");
            items.add(roomPrivate?"🔓 Make room public":"🔐 Make room private"); items.add(roomHasPassword?"🔑 Change room password":"🔑 Set room password");
            if(roomHasPassword)items.add("🗑 Remove room password"); items.add("⛔ Close room");
        }
        ScrollView scroll=new ScrollView(this);scroll.setVerticalScrollBarEnabled(false);LinearLayout panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(dp(14),dp(14),dp(14),dp(18));panel.setBackgroundColor(Color.WHITE);scroll.addView(panel);
        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);TextView av=tv("👑",28,0xff222222,true);av.setGravity(Gravity.CENTER);header.addView(av,new LinearLayout.LayoutParams(dp(52),dp(58)));LinearLayout hi=new LinearLayout(this);hi.setOrientation(LinearLayout.VERTICAL);TextView nm=tv(roomName,18,0xff202020,true);nm.setPadding(0,0,0,0);TextView meta=tv((coHost?"Administrator / Co-host":"KING Party")+"  •  "+(roomPrivate?"Private":"Public"),11,0xff777777,false);meta.setPadding(0,0,0,0);hi.addView(nm,new LinearLayout.LayoutParams(-1,dp(30)));hi.addView(meta,new LinearLayout.LayoutParams(-1,dp(24)));header.addView(hi,new LinearLayout.LayoutParams(0,dp(58),1));panel.addView(header,new LinearLayout.LayoutParams(-1,dp(64)));
        View div=new View(this);div.setBackgroundColor(0xffeeeeee);panel.addView(div,new LinearLayout.LayoutParams(-1,dp(1)));
        final AlertDialog[] holder=new AlertDialog[1];
        for(String label:items){final String x=label;TextView row=tv(label,14,0xff222222,false);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(10),0,dp(8),0);row.setBackground(bg(Color.WHITE,8));row.setOnClickListener(v->{if(holder[0]!=null)holder[0].dismiss();handleRoomMenuItem810(x);});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(50));lp.setMargins(0,dp(1),0,dp(1));panel.addView(row,lp);}
        holder[0]=new AlertDialog.Builder(this).setView(scroll).create();holder[0].setOnShowListener(v->{android.view.Window w=holder[0].getWindow();if(w!=null){w.setBackgroundDrawable(bg(Color.WHITE,18));w.setGravity(Gravity.CENTER);w.setLayout((int)(getResources().getDisplayMetrics().widthPixels*.90f),(int)(getResources().getDisplayMetrics().heightPixels*.88f));}});holder[0].show();
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
        else if(x.contains("Room settings"))roomSettingsPanel();
        else if(x.contains("Open voice"))openVoice();
        else if(x.contains("Multi Video"))openVideoRoom();
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
        else if(x.contains("Games"))openDeepFlow810("game_center");
        else if(x.contains("PK battle"))pkBattle();
        else if(x.contains("Party Master"))partyMasterPanel();
        else if(x.contains("Party Data"))partyDataPanel();
        else if(x.contains("KTV Queue"))ktvQueuePanel();
        else if(x.contains("Radio Mic"))radioMicPanel();
        else if(x.contains("Lucky Gift"))luckyGiftPanel();
        else if(x.contains("Gift Wish"))giftWishPanel();
        else if(x.contains("Find room user"))findRoomUserPanel();
        else if(x.contains("Notice center"))roomNoticePanel();
        else if(x.contains("Interactive Emoji"))showLiveEmojiPanelV530();
        else if(x.contains("Audio PK"))pkBattle();
        else if(x.contains("Gift Wall"))giftWall620();
        else if(x.contains("Online User Search"))findRoomUserPanel();
        else if(x.contains("Family Party"))familyPartyPanel610();
        else if(x.contains("Friend Broadcast"))friendBroadcast610();
        else if(x.contains("Party Master Help"))partyMasterHelp610();
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
            String text="Members: "+Math.max(1,memberNames.size())+"\nOccupied seats: "+seatNames.size()+"/"+maxSeats+"\nRoom type: "+roomCategory+"\nTheme: "+roomTheme;
            new AlertDialog.Builder(this).setTitle("📊 Party Data").setMessage(text).setPositiveButton("OK",null).show();return;
        }
        final long[] giftValue={0};final int[] gifts={0};final int[] events={0};
        db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(200).get().addOnSuccessListener(es->{
            events[0]=es.size();for(DocumentSnapshot e:es.getDocuments())if("gift".equals(e.getString("type"))){gifts[0]++;Long v=e.getLong("giftValue");if(v!=null)giftValue[0]+=Math.max(0,v);}
            db.collection("live_rooms").document(roomId).collection("messages").orderBy("createdAt",Query.Direction.DESCENDING).limit(200).get().addOnSuccessListener(ms->{
                String text="Online members: "+liveMemberCount+"\nOccupied seats: "+seatNames.size()+"/"+maxSeats+"\nRecent messages: "+ms.size()+"\nRecent room events: "+events[0]+"\nGift events: "+gifts[0]+"\nGift value: 💎"+compactNumber(giftValue[0])+"\nCategory: "+roomCategory+"\nTheme: "+roomTheme;
                new AlertDialog.Builder(this).setTitle("📊 Party Data").setMessage(text).setPositiveButton("Ranking",(d,w)->roomRankingDialog()).setNeutralButton("Gift history",(d,w)->giftHistoryDialog()).setNegativeButton("Close",null).show();
            }).addOnFailureListener(e->toast("Party data unavailable: "+msg(e)));
        }).addOnFailureListener(e->toast("Party data unavailable: "+msg(e)));
    }

    private void ktvQueuePanel(){
        if(!cloudRoom||db==null||roomId==null){karaokeDialog();return;}
        db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(120).get().addOnSuccessListener(snap->{
            List<String> rows=new ArrayList<>();for(DocumentSnapshot d:snap.getDocuments())if("song".equals(d.getString("type"))){String text=d.getString("text");if(text!=null&&!text.trim().isEmpty())rows.add("🎵 "+text);if(rows.size()>=20)break;}
            List<String> actions=new ArrayList<>();actions.add("＋ Request a song");if(isModerator())actions.add("🎛 Open room music player");actions.addAll(rows);
            new AlertDialog.Builder(this).setTitle("🎤 KTV Queue").setItems(actions.toArray(new String[0]),(d,w)->{String x=actions.get(w);if(x.startsWith("＋"))karaokeDialog();else if(x.contains("music player"))musicPanel();}).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast("KTV queue unavailable: "+msg(e)));
    }

    private void radioMicPanel(){
        boolean queueOn=cloudRoom?true:prefs.getBoolean("queue_"+roomId,false);
        List<String> items=new ArrayList<>();items.add("Status • "+(queueOn?"Mic queue available":"Mic queue off"));
        if(isModerator()){items.add("📥 Open seat request center");items.add("✋ "+(queueOn?"Toggle mic queue":"Enable mic queue"));items.add("🪑 Manage seats");}
        else{items.add("✋ Request first empty mic");items.add("🪑 View seats");}
        new AlertDialog.Builder(this).setTitle("📻 Radio Mic Queue").setItems(items.toArray(new String[0]),(d,w)->{String x=items.get(w);if(x.contains("request center"))allSeatRequestsDialog();else if(x.contains("Toggle")||x.contains("Enable"))toggleSeatQueue();else if(x.contains("Request first")){for(int i=1;i<=maxSeats;i++)if(seatUids.get(i)==null&&!lockedSeats.contains(i)){requestSeat(i);return;}toast("No empty mic seat");}else if(x.contains("seats"))roomSeatPanel();}).setNegativeButton("Close",null).show();
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
        List<String> names=new ArrayList<>();List<String> uids=new ArrayList<>();
        if(ownerName!=null){names.add("👑 "+ownerName);uids.add(ownerUid==null?"":ownerUid);}for(String n:memberNames){String uid=memberUids.get(n);if(uid==null||uid.isEmpty()||uids.contains(uid))continue;names.add("👤 "+n);uids.add(uid);}if(names.isEmpty()){toast("No room members loaded");return;}
        new AlertDialog.Builder(this).setTitle("🔎 Find room user").setItems(names.toArray(new String[0]),(d,w)->{String uid=uids.get(w),name=names.get(w).replace("👑 ","").replace("👤 ","");if(uid.isEmpty())toast("Profile unavailable");else memberProfileDialog(uid,name);}).setNegativeButton("Close",null).show();
    }

    private void roomNoticePanel(){
        String msg=(announcement==null||announcement.trim().isEmpty())?"No room notice":announcement;
        new AlertDialog.Builder(this).setTitle("📢 Room Notice").setMessage(msg).setPositiveButton(isModerator()?"Edit":"OK",(d,w)->{if(isModerator())editAnnouncement();}).setNeutralButton("Billboard",(d,w)->roomBillboardDialog()).setNegativeButton("Close",null).show();
    }

    private void familyPartyPanel610(){
        String[] actions={"🏠 Family room info","👥 Invite family/friends","🎉 Post family activity","📢 Family announcement"};
        new AlertDialog.Builder(this).setTitle("💞 Family Party").setItems(actions,(d,w)->{
            if(w==0)new AlertDialog.Builder(this).setTitle("Family Party").setMessage("Room: "+roomName+"\nOnline: "+(cloudRoom?liveMemberCount:Math.max(1,memberNames.size()))+"\nSeats: "+seatNames.size()+"/"+maxSeats).setPositiveButton("OK",null).show();
            else if(w==1)shareRoom();
            else if(w==2){addEvent("family_activity",safeName()+" started a Family Party activity");toast("Family activity posted to room");}
            else roomNoticePanel();
        }).setNegativeButton("Close",null).show();
    }
    private void friendBroadcast610(){
        String text="Join my KING Plus Party: "+roomName+(roomId==null?"":" • Room "+roomId);
        Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,text);startActivity(Intent.createChooser(i,"Broadcast Party to friends"));
    }
    private void partyMasterHelp610(){
        new AlertDialog.Builder(this).setTitle("📚 Party Master Help").setMessage("Host / Co-host controls\n\n• Lock or unlock seats\n• Approve seat requests\n• Mute or unmute members\n• Kick / ban users\n• Manage room privacy and password\n• Edit room notice\n• View Party Data, ranking and gift history\n• Use KTV, Radio Mic, PK, Games and Interactive Emoji\n\nAll moderation actions use the current KING Plus room/Firebase flow.").setPositiveButton("Open Party Master",(d,w)->partyMasterPanel()).setNegativeButton("Close",null).show();
    }

    private void inviteDialog(){
        if(!cloudRoom||user==null){shareRoom();return;}
        final EditText e=new EditText(this);e.setHint("Friend Firebase UID");
        new AlertDialog.Builder(this).setTitle("Invite to Party").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Invite",(d,w)->{
            String uid=e.getText().toString().trim();if(uid.isEmpty())return;
            Runnable send=()->CloudBackend.sendRoomInvite(uid,roomId,roomName,(ok,m)->runOnUiThread(()->toast(m)));
            if(roomPrivate&&isModerator()&&db!=null){
                Map<String,Object> grant=new HashMap<>();grant.put("uid",uid);grant.put("grantedBy",user.getUid());grant.put("createdAt",FieldValue.serverTimestamp());
                db.collection("live_rooms").document(roomId).collection("access").document(uid).set(grant)
                    .addOnSuccessListener(v->send.run()).addOnFailureListener(x->toast("Private invite failed: "+msg(x)));
            }else send.run();
        }).show();
    }
    private void reportHost(){String target=ownerUid==null?ownerName:ownerUid;CloudSync.submitReport(this,target,"Live party report",(ok,m)->runOnUiThread(()->toast(m)));}
    private void blockHost(){Set<String>b=new HashSet<>(prefs.getStringSet("blocked",new HashSet<>()));b.add(ownerUid==null?ownerName:ownerUid);prefs.edit().putStringSet("blocked",b).apply();toast("Host blocked locally");}
    private void editAnnouncement(){final EditText e=new EditText(this);e.setText(announcement);new AlertDialog.Builder(this).setTitle("Room announcement").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{String s=e.getText().toString().trim();if(s.isEmpty())return;if(cloudRoom)setRoomValue("announcement",s);else{announcement=s;prefs.edit().putString("notice_"+roomId,s).apply();announcementLabel.setText("📢  "+s);}}).show();}
    private void setRoomFlag(String key,boolean value){if(!isModerator()){toast("Host/co-host only");return;}if(cloudRoom)setRoomValue(key,value);else{if("locked".equals(key))roomLocked=value;else muteAll=value;prefs.edit().putBoolean(("locked".equals(key)?"locked_":"mute_")+roomId,value).apply();toast("Room updated");}}
    private void setRoomValue(String key,Object value){if(db==null)return;db.collection("live_rooms").document(roomId).update(key,value,"updatedAt",FieldValue.serverTimestamp()).addOnFailureListener(e->toast(msg(e)));}
    private void closeRoom(){if(!isOwner()){toast("Host only");return;}if(cloudRoom)setRoomValue("closed",true);leaveRoom();}

    private boolean categoryMatches(String selected,String category){
        if(selected==null||"Hot".equalsIgnoreCase(selected))return true;
        if("Music".equalsIgnoreCase(selected))return "Music".equalsIgnoreCase(category)||"Sing".equalsIgnoreCase(category)||"KTV".equalsIgnoreCase(category)||"Radio".equalsIgnoreCase(category);
        if("Game".equalsIgnoreCase(selected))return "Game".equalsIgnoreCase(category)||"PK".equalsIgnoreCase(category)||"Pick Me".equalsIgnoreCase(category);
        if("Video".equalsIgnoreCase(selected))return "Multi Video".equalsIgnoreCase(category);
        if("Event".equalsIgnoreCase(selected))return "Event".equalsIgnoreCase(category)||"Birthday".equalsIgnoreCase(category)||"Wedding".equalsIgnoreCase(category)||"Family".equalsIgnoreCase(category);
        return selected.equalsIgnoreCase(category);
    }
    private int themeBackground(){
        if("KTV".equals(roomTheme))return 0xff101d46;
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
        if(uid==null||uid.isEmpty())return;if(user!=null&&uid.equals(user.getUid())){toast("This is you");return;}showRichProfile(uid,name,false);
    }
    private void showRichProfile(String uid,String name,boolean host){
        if(uid!=null&&!uid.trim().isEmpty()){
            logProfileVisit750(uid);
            Intent page=new Intent(this,KingPublicProfileActivity.class);
            page.putExtra("uid",uid);
            page.putExtra("name",name==null||name.trim().isEmpty()?"KING User":name.trim());
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

        TextView identity=tv("▣  KING ID "+publicId(uid)+"   |   Followers",13,0xff66606b,true);identity.setGravity(Gravity.CENTER);identity.setOnClickListener(v->copyKingId(uid));root.addView(identity,new LinearLayout.LayoutParams(-1,dp(36)));

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
            db.collection("follows").document(relationId).get().addOnSuccessListener(d->follow.setText(d.exists()?"✓  Following":"👤  Follow"));
            db.collection("follows").whereEqualTo("targetUid",uid).get().addOnSuccessListener(q->identity.setText("▣  KING ID "+publicId(uid)+"   |   "+q.size()+" Followers"));
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
    private void copyKingId(String uid){String value=publicId(uid);ClipboardManager cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);if(cm!=null)cm.setPrimaryClip(ClipData.newPlainText("KING ID",value));toast("KING ID copied: "+value);}
    private void loadProfileRoomStats(String uid,TextView vip,TextView level,TextView vipStat,TextView contributionStat,TextView charismaStat){
        if(db==null||roomId==null||uid==null)return;db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(100).get().addOnSuccessListener(q->{long sent=0,received=0,activity=0;for(DocumentSnapshot d:q.getDocuments()){String a=d.getString("actorUid"),t=d.getString("targetUid"),type=d.getString("type");Long raw=d.getLong("giftValue");long v=raw==null?0:Math.max(0,raw);if(uid.equals(a)){activity++;if("gift".equals(type))sent+=v;}if(uid.equals(t)&&"gift".equals(type))received+=v;}int v=(int)Math.min(LevelSystem.MAX_VIP,sent/10000),lv=(int)Math.min(99,1+activity/10+(sent+received)/50000);vip.setText("💎 VIP "+v);level.setText("Lv "+Math.max(1,lv));vipStat.setText("⭐  VIP level\nvip."+v);contributionStat.setText("🏆  Contribution\n"+compactNumber(sent));charismaStat.setText("🔥  Charisma\n"+compactNumber(received));});
    }
    private void logProfileVisit750(String targetUid){if(user==null||db==null||targetUid==null||targetUid.isEmpty()||targetUid.equals(user.getUid()))return;Map<String,Object>v=new HashMap<>();v.put("uid",user.getUid());v.put("name",safeName());v.put("updatedAt",FieldValue.serverTimestamp());db.collection("public_profiles").document(targetUid).collection("visitors").document(user.getUid()).set(v,SetOptions.merge());}
    private void profileFamilyDialog(String uid,String name){boolean self=user!=null&&uid.equals(user.getUid());if(self){SharedPreferences p=getSharedPreferences("MainActivity",MODE_PRIVATE);String family=p.getString("family_name","Not joined"),code=p.getString("family_code","");new AlertDialog.Builder(this).setTitle("👑 Family").setMessage("Current family: "+family+(code.isEmpty()?"":"\nFamily code: "+code)).setPositiveButton("Open Me page",(d,w)->{Intent i=new Intent(this,MainActivity.class);i.putExtra("openTab",4);startActivity(i);}).setNeutralButton("Share Family",(d,w)->shareFamilyRoom()).setNegativeButton("Close",null).show();}else new AlertDialog.Builder(this).setTitle("👑 "+name+" • Family").setMessage("Family details stay private until shared. You can invite this user in private chat or share your Family room.").setPositiveButton("Private chat",(d,w)->openPrivateChat(uid,name)).setNeutralButton("Share Family",(d,w)->shareFamilyRoom()).setNegativeButton("Close",null).show();}
    private void profileRelationshipDialog(String uid,String name){if(user!=null&&uid.equals(user.getUid())){new AlertDialog.Builder(this).setTitle("💞 Relationship").setMessage("Manage your relationship from the Me page.").setPositiveButton("Open Me page",(d,w)->{Intent i=new Intent(this,MainActivity.class);i.putExtra("openTab",4);startActivity(i);}).setNegativeButton("Close",null).show();return;}new AlertDialog.Builder(this).setTitle("💞 "+name).setMessage("Start a private chat or send a relationship gift.").setPositiveButton("Private chat",(d,w)->openPrivateChat(uid,name)).setNeutralButton("Send Gift",(d,w)->giftDialogFor(uid,name)).setNegativeButton("Close",null).show();}
    private void profileGiftWallDialog(String uid,String name){if(db==null||roomId==null){toast("Live room required");return;}db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(100).get().addOnSuccessListener(q->{ArrayList<String> rows=new ArrayList<>();long total=0;for(DocumentSnapshot d:q.getDocuments())if("gift".equals(d.getString("type"))&&uid.equals(d.getString("targetUid"))){String g=d.getString("giftName"),a=d.getString("actorName");Long val=d.getLong("giftValue"),qty=d.getLong("giftQty");long v=val==null?0:val;total+=v;rows.add((g==null?"Gift":g)+" x"+(qty==null?1:qty)+" • from "+(a==null?"User":a)+" • 💎"+v);}if(rows.isEmpty()){new AlertDialog.Builder(this).setTitle("🎁 "+name+" • Gift Wall").setMessage("No gifts received in this room yet.").setPositiveButton("Send first gift",(d,w)->giftDialogFor(uid,name)).setNegativeButton("Close",null).show();return;}new AlertDialog.Builder(this).setTitle("🎁 Gift Wall • Total 💎"+compactNumber(total)).setItems(rows.toArray(new String[0]),null).setPositiveButton("Send Gift",(d,w)->giftDialogFor(uid,name)).setNegativeButton("Close",null).show();}).addOnFailureListener(e->toast("Gift Wall unavailable: "+msg(e)));}
    private void profileMedalsDialog(String uid,String name,boolean host){if(db==null||roomId==null){toast("Live room required");return;}db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(100).get().addOnSuccessListener(q->{long sent=0,received=0,activity=0;for(DocumentSnapshot d:q.getDocuments()){Long val=d.getLong("giftValue");if(uid.equals(d.getString("actorUid"))){activity++;if("gift".equals(d.getString("type"))&&val!=null)sent+=val;}if(uid.equals(d.getString("targetUid"))&&"gift".equals(d.getString("type"))&&val!=null)received+=val;}ArrayList<String> m=new ArrayList<>();if(host)m.add("👑 Room Host");if(activity>=5)m.add("🏅 Active Member");if(sent>0)m.add("💎 Supporter • "+compactNumber(sent));if(received>0)m.add("🌟 Gift Star • "+compactNumber(received));if(m.isEmpty())m.add("No medals earned in this room yet");new AlertDialog.Builder(this).setTitle("🏅 "+name+" • Medals").setItems(m.toArray(new String[0]),null).setPositiveButton("OK",null).show();}).addOnFailureListener(e->toast("Medals unavailable: "+msg(e)));}

    private void openCommunityHub700(String tab){Intent i=new Intent(this,CommunityHubActivity.class);i.putExtra("tab",tab);startActivity(i);}
    private void backpackGiftDialog700(){
        SharedPreferences bp=getSharedPreferences("king_backpack",MODE_PRIVATE);String[] keys={"rose","heart","star","firework"},names={"Rose","Heart","Star","Firework"},icons={"🌹","💗","⭐","🎆"};List<String> rows=new ArrayList<>();List<Integer> idx=new ArrayList<>();for(int i=0;i<keys.length;i++){int n=bp.getInt(keys[i],0);if(n>0){rows.add(icons[i]+" "+names[i]+" ×"+n);idx.add(i);}}if(rows.isEmpty()){new AlertDialog.Builder(this).setTitle("🎒 Backpack").setMessage("No free gifts yet. Claim one in Collection Center.").setPositiveButton("Collection",(d,w)->openCommunityHub700("Collection")).setNegativeButton("Close",null).show();return;}if(giftTargetName==null||giftTargetName.isEmpty()){giftTargetName=ownerName;giftTargetUid=ownerUid;}new AlertDialog.Builder(this).setTitle("🎒 Backpack • To "+giftTargetName).setItems(rows.toArray(new String[0]),(d,w)->{int i=idx.get(w),count=bp.getInt(keys[i],0);if(count<=0)return;bp.edit().putInt(keys[i],count-1).apply();String text=safeName()+" sent Backpack "+icons[i]+" "+names[i]+" to "+giftTargetName;if(cloudRoom&&db!=null&&user!=null){Map<String,Object>e=new HashMap<>();e.put("actorUid",user.getUid());e.put("actorName",safeName());e.put("type","gift");e.put("text",text);e.put("giftName",names[i]);e.put("giftIcon",icons[i]);e.put("giftValue",0);e.put("giftQty",1);e.put("targetUid",giftTargetUid==null?"":giftTargetUid);e.put("targetName",giftTargetName);e.put("createdAt",FieldValue.serverTimestamp());DocumentReference room=db.collection("live_rooms").document(roomId);room.collection("events").add(e);Map<String,Object>m=new HashMap<>(e);m.put("senderUid",user.getUid());m.put("senderName",safeName());room.collection("messages").add(m);}showGiftEffect(safeName(),giftTargetName,names[i],icons[i],1,0);awardGiftProgress700(0);toast("Backpack gift sent");}).setNeutralButton("Collection",(d,w)->openCommunityHub700("Collection")).setNegativeButton("Close",null).show();
    }
    private void audioPkPanel700(){if(!cloudRoom||db==null){toast("Live room required");return;}db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(200).get().addOnSuccessListener(q->{long red=0,blue=0;int gifts=0;for(DocumentSnapshot e:q.getDocuments()){if(!"gift".equals(e.getString("type")))continue;Long raw=e.getLong("giftValue");long score=raw==null?0:Math.max(0,raw);String actor=e.getString("actorUid");Integer seat=null;if(actor!=null)for(Map.Entry<Integer,String>x:seatUids.entrySet())if(actor.equals(x.getValue())){seat=x.getKey();break;}if(seat!=null&&seat%2==0)red+=score;else if(seat!=null)blue+=score;else if((gifts%2)==0)red+=score;else blue+=score;gifts++;}String msg="🔴 Red "+compactNumber(red)+"\n\n🔵 Blue "+compactNumber(blue)+"\n\n"+(red==blue?"🤝 Draw":red>blue?"🏆 Red leads":"🏆 Blue leads");AlertDialog.Builder b=new AlertDialog.Builder(this).setTitle("⚔️ Audio PK").setMessage(msg).setPositiveButton("Refresh",(d,w)->audioPkPanel700()).setNegativeButton("Close",null);if(isModerator())b.setNeutralButton("Start PK",(d,w)->addEvent("audio_pk",safeName()+" started Audio PK ⚔️"));b.show();});}
    private int firstOpenSeat700(){for(int i=1;i<=maxSeats;i++)if(!seatUids.containsKey(i)&&!lockedSeats.contains(i))return i;return -1;}
    private void loopMicPanel700(){if(!cloudRoom||db==null){toast("Live room required");return;}if(!isModerator()){int seat=firstOpenSeat700();if(seat<1){toast("No open mic seat");return;}requestSeat(seat);return;}db.collection("live_rooms").document(roomId).collection("seat_requests").get().addOnSuccessListener(q->{if(q.isEmpty()){toast("Loop Mic queue is empty");return;}List<DocumentSnapshot>docs=q.getDocuments();List<String>rows=new ArrayList<>();for(DocumentSnapshot d:docs){Long n=d.getLong("seatNo");rows.add(str(d,"name","User")+" • Seat "+(n==null?"auto":n));}new AlertDialog.Builder(this).setTitle("🔁 Loop Mic Queue").setItems(rows.toArray(new String[0]),(x,w)->{DocumentSnapshot r=docs.get(w);int seat=firstOpenSeat700();Long req=r.getLong("seatNo");if(req!=null&&!seatUids.containsKey(req.intValue())&&!lockedSeats.contains(req.intValue()))seat=req.intValue();if(seat<1){toast("No open seat");return;}approveSeatRequest(r.getId(),r.getString("uid"),str(r,"name","User"),seat);}).setNegativeButton("Close",null).show();});}

    private String publicId(String uid){if(uid==null||uid.isEmpty())return "000000";long value=Integer.toUnsignedLong(uid.hashCode());return String.valueOf((value%900000L)+100000L);}

    private void loadProfilePhoto(ImageView image,TextView fallback,String url){
        Bitmap cached=photoCache540.get(url);if(cached!=null){image.setImageBitmap(cached);fallback.setVisibility(View.GONE);return;}
        image.setTag(url);new Thread(()->{try{java.net.URLConnection connection=new URL(url).openConnection();connection.setConnectTimeout(8000);connection.setReadTimeout(8000);try(java.io.InputStream input=connection.getInputStream()){BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=2;Bitmap bitmap=BitmapFactory.decodeStream(input,null,options);if(bitmap!=null){photoCache540.put(url,bitmap);runOnUiThread(()->{if(!isFinishing()&&url.equals(image.getTag())){image.setImageBitmap(bitmap);fallback.setVisibility(View.GONE);}});}}}catch(Exception ignored){}}).start();
    }

    private void toggleProfileFollow(String uid,String name,TextView button){
        if(user==null||db==null){toast("Google / Firebase sign-in required");return;}if(uid.equals(user.getUid())){toast("This is you");return;}
        String id=user.getUid()+"__"+uid;DocumentReference ref=db.collection("follows").document(id);
        ref.get().addOnSuccessListener(doc->{
            if(doc.exists())ref.delete().addOnSuccessListener(v->{button.setText("👤  Follow");toast("Disconnected from "+name);});
            else{Map<String,Object>d=new HashMap<>();d.put("followerUid",user.getUid());d.put("targetUid",uid);d.put("followerName",safeName());d.put("createdAt",FieldValue.serverTimestamp());ref.set(d).addOnSuccessListener(v->{button.setText("✓  Following");CloudBackend.sendFollowNotification(uid,safeName(),(ok,m)->{});toast("Following "+name);});}
        }).addOnFailureListener(e->toast(msg(e)));
    }

    private void showProfileMore(String uid,String name){
        List<String> items=new ArrayList<>();items.add("💬 Private chat");items.add("🎁 Send gift");items.add("⚑ Report");
        int seatNo=-1;for(Map.Entry<Integer,String> e:seatUids.entrySet())if(uid!=null&&uid.equals(e.getValue())){seatNo=e.getKey();break;}
        final int occupiedSeat=seatNo;
        if(isModerator()&&uid!=null&&!uid.equals(ownerUid)){
            if(occupiedSeat>0){items.add(Boolean.FALSE.equals(seatMics.get(occupiedSeat))?"🎤 Unmute seat":"🔇 Mute seat");items.add("↔ Move seat");items.add("⬇ Remove from seat");}
            items.add("🚪 Kick");items.add("🚫 Ban");
            if(isOwner())items.add("👑 Co-host role");
        }else if(uid!=null&&(user==null||!uid.equals(user.getUid())))items.add("🚫 Block");
        new AlertDialog.Builder(this).setTitle(name).setItems(items.toArray(new String[0]),(d,w)->{String x=items.get(w);
            if(x.contains("Private"))openPrivateChat(uid,name);else if(x.contains("Gift"))giftDialogFor(uid,name);else if(x.contains("Report"))reportUser(uid,name);
            else if(x.contains("Mute seat")||x.contains("Unmute seat"))hostToggleSeat(occupiedSeat);else if(x.contains("Move seat"))moveUserToSeatDialog(uid,name,occupiedSeat);else if(x.contains("Remove from seat"))hostRemoveSeat(occupiedSeat);
            else if(x.contains("Kick"))kickUser(uid,name);else if(x.contains("Ban"))banUser(uid,name);else if(x.contains("Co-host"))coHostDialog(uid,name);else if(x.contains("Block"))blockUserLocally(uid,name);
        }).setNegativeButton("Close",null).show();
    }

    private void kickUser(String uid,String name){
        if(!isModerator()||uid==null||uid.isEmpty()||uid.equals(ownerUid)){toast("Cannot kick this member");return;}
        if(!cloudRoom||db==null){toast("Live room required");return;}
        for(Map.Entry<Integer,String> entry:new HashMap<>(seatUids).entrySet()){
            if(uid.equals(entry.getValue()))db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(entry.getKey())).delete();
        }
        db.collection("live_rooms").document(roomId).collection("members").document(uid).delete()
            .addOnSuccessListener(v->{addEvent("kick",name+" was removed by a moderator");toast(name+" kicked");})
            .addOnFailureListener(e->toast("Kick failed: "+msg(e)));
    }
    private void banUser(String uid,String name){
        if(!isModerator()||uid==null||uid.isEmpty()||uid.equals(ownerUid)){toast("Cannot ban this member");return;}
        if(!cloudRoom||db==null){toast("Live room required");return;}
        Map<String,Object> ban=new HashMap<>();ban.put("uid",uid);ban.put("active",true);ban.put("byUid",user.getUid());ban.put("createdAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).collection("room_bans").document(uid).set(ban)
            .addOnSuccessListener(v->kickUser(uid,name)).addOnFailureListener(e->toast("Ban failed: "+msg(e)));
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
    private void setPrivateRoom(boolean value){
        if(!isOwner()){toast("Host only");return;}
        if(!cloudRoom){roomPrivate=value;toast(value?"Private room enabled":"Room is public");refreshRoomState();return;}
        if(!value){
            roomPrivate=false;setRoomValue("isPrivate",false);toast("Room is public");refreshRoomState();return;
        }
        grantCurrentMembersAccessThen(()->{
            roomPrivate=true;setRoomValue("isPrivate",true);toast("Private room enabled • current members kept access");refreshRoomState();
        });
    }

    private void setRoomPasswordDialog(){
        if(!isOwner()||!cloudRoom||db==null){toast("Live host room required");return;}
        final EditText e=new EditText(this);e.setHint("At least 4 characters");e.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle(roomHasPassword?"Change room password":"Set room password").setView(e)
            .setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{
                String password=e.getText().toString();if(password.length()<4){toast("Password must be at least 4 characters");return;}
                String hash=passwordProof(roomId,password);if(hash.isEmpty()){toast("Could not secure password");return;}
                Map<String,Object> secret=new HashMap<>();secret.put("passwordHash",hash);secret.put("ownerUid",user.getUid());secret.put("updatedAt",FieldValue.serverTimestamp());
                db.collection("live_rooms").document(roomId).collection("secrets").document("password").set(secret)
                    .addOnSuccessListener(v->grantCurrentMembersAccessThen(()->db.collection("live_rooms").document(roomId).update("isPrivate",true,"hasPassword",true,"updatedAt",FieldValue.serverTimestamp())
                        .addOnSuccessListener(x->{roomPrivate=true;roomHasPassword=true;toast("Password room enabled • current members kept access");refreshRoomState();})
                        .addOnFailureListener(x->toast(msg(x)))))
                    .addOnFailureListener(x->toast("Password save failed: "+msg(x)));
            }).show();
    }
    private void removeRoomPassword(){
        if(!isOwner()||!cloudRoom||db==null)return;
        db.collection("live_rooms").document(roomId).collection("secrets").document("password").delete()
            .addOnSuccessListener(v->db.collection("live_rooms").document(roomId).update("hasPassword",false,"updatedAt",FieldValue.serverTimestamp())
                .addOnSuccessListener(x->{roomHasPassword=false;toast("Password removed • room remains private");}))
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
        if(!cloudRoom||db==null){new AlertDialog.Builder(this).setTitle("🕘 Recent activity").setMessage("Local test room activity is shown directly on the Party page.").setPositiveButton("OK",null).show();return;}
        db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(40).get()
            .addOnSuccessListener(snap->{List<String> rows=new ArrayList<>();for(DocumentSnapshot d:snap.getDocuments())rows.add(str(d,"text","Room activity"));showRows("🕘 Recent activity",rows,"No room activity yet");})
            .addOnFailureListener(e->toast(msg(e)));
    }
    private void giftHistoryDialog(){
        if(!cloudRoom||db==null){new AlertDialog.Builder(this).setTitle("🎁 Gift history").setMessage("Gift history for local rooms stays in test activity only.").setPositiveButton("OK",null).show();return;}
        db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(100).get()
            .addOnSuccessListener(snap->{List<String> rows=new ArrayList<>();for(DocumentSnapshot d:snap.getDocuments())if("gift".equals(d.getString("type")))rows.add(str(d,"text","Gift sent"));showRows("🎁 Gift history",rows,"No gifts sent in this room yet");})
            .addOnFailureListener(e->toast(msg(e)));
    }
    private void roomRankingDialog(){
        if(!cloudRoom||db==null){new AlertDialog.Builder(this).setTitle("🏆 Room ranking").setMessage("Live gift ranking is available in Firebase rooms.").setPositiveButton("OK",null).show();return;}
        db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(150).get()
            .addOnSuccessListener(snap->{Map<String,Long>score=new HashMap<>();Map<String,Integer>count=new HashMap<>();for(DocumentSnapshot d:snap.getDocuments())if("gift".equals(d.getString("type"))){String n=str(d,"actorName","User");Long v=d.getLong("giftValue");long value=v==null?1:Math.max(1,v);score.put(n,(score.containsKey(n)?score.get(n):0L)+value);count.put(n,(count.containsKey(n)?count.get(n):0)+1);}List<Map.Entry<String,Long>>list=new ArrayList<>(score.entrySet());java.util.Collections.sort(list,(a,b)->Long.compare(b.getValue(),a.getValue()));List<String>rows=new ArrayList<>();int rank=1;for(Map.Entry<String,Long>e:list){rows.add((rank==1?"🥇 ":rank==2?"🥈 ":rank==3?"🥉 ":rank+". ")+e.getKey()+" • 💎"+compactNumber(e.getValue())+" • "+count.get(e.getKey())+" gifts");rank++;if(rank>20)break;}showRows("🏆 Room gift ranking",rows,"No gift ranking yet");})
            .addOnFailureListener(e->toast(msg(e)));
    }
    private void allSeatRequestsDialog(){
        if(!isModerator()||!cloudRoom||db==null){toast("Host/co-host only");return;}
        db.collection("live_rooms").document(roomId).collection("seat_requests").get().addOnSuccessListener(snap->{
            if(snap.isEmpty()){toast("No pending seat requests");return;}List<DocumentSnapshot>docs=snap.getDocuments();List<String>rows=new ArrayList<>();
            for(DocumentSnapshot d:docs){Long n=d.getLong("seatNo");rows.add(str(d,"name","User")+" • Seat "+(n==null?"?":n));}
            new AlertDialog.Builder(this).setTitle("📥 Seat requests").setItems(rows.toArray(new String[0]),(x,w)->{DocumentSnapshot req=docs.get(w);Long n=req.getLong("seatNo");if(n!=null)seatRequestDecision(req,n.intValue());}).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast(msg(e)));
    }
    private void coHostListDialog(){
        if(!isOwner()||!cloudRoom||db==null){toast("Host only");return;}
        db.collection("live_rooms").document(roomId).collection("roles").get().addOnSuccessListener(snap->{
            List<DocumentSnapshot>docs=new ArrayList<>();List<String>rows=new ArrayList<>();
            for(DocumentSnapshot d:snap.getDocuments())if("cohost".equals(d.getString("role"))){docs.add(d);rows.add("👑 "+memberNameForUid(d.getId()));}
            if(rows.isEmpty()){toast("No co-hosts assigned");return;}
            new AlertDialog.Builder(this).setTitle("👑 Co-host list").setItems(rows.toArray(new String[0]),(x,w)->{DocumentSnapshot d=docs.get(w);String uid=d.getId();String name=memberNameForUid(uid);new AlertDialog.Builder(this).setTitle(name).setMessage("Remove co-host role?").setPositiveButton("Remove",(a,b)->d.getReference().delete().addOnSuccessListener(v->toast("Co-host removed")).addOnFailureListener(e->toast(msg(e)))).setNegativeButton("Cancel",null).show();}).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast(msg(e)));
    }
    private void banListDialog(){
        if(!isModerator()||!cloudRoom||db==null){toast("Host/co-host only");return;}
        db.collection("live_rooms").document(roomId).collection("room_bans").get().addOnSuccessListener(snap->{
            List<DocumentSnapshot>docs=new ArrayList<>();List<String>rows=new ArrayList<>();
            for(DocumentSnapshot d:snap.getDocuments())if(Boolean.TRUE.equals(d.getBoolean("active"))){docs.add(d);rows.add("🚫 "+memberNameForUid(d.getId()));}
            if(rows.isEmpty()){toast("No banned users");return;}
            new AlertDialog.Builder(this).setTitle("🚫 Banned users").setItems(rows.toArray(new String[0]),(x,w)->{DocumentSnapshot d=docs.get(w);String name=memberNameForUid(d.getId());new AlertDialog.Builder(this).setTitle(name).setMessage("Allow this user to join again?").setPositiveButton("Unban",(a,b)->d.getReference().delete().addOnSuccessListener(v->toast(name+" unbanned")).addOnFailureListener(e->toast(msg(e)))).setNegativeButton("Cancel",null).show();}).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast(msg(e)));
    }
    private void showRows(String title,List<String> rows,String empty){
        if(rows==null||rows.isEmpty()){new AlertDialog.Builder(this).setTitle(title).setMessage(empty).setPositiveButton("OK",null).show();return;}
        new AlertDialog.Builder(this).setTitle(title).setItems(rows.toArray(new String[0]),null).setNegativeButton("Close",null).show();
    }

    private void followUser(String uid,String name){
        if(user==null||db==null||uid==null||uid.isEmpty()){toast("Live Firebase user required");return;} if(uid.equals(user.getUid())){toast("This is you");return;}
        String id=user.getUid()+"__"+uid;DocumentReference ref=db.collection("follows").document(id);Map<String,Object>d=new HashMap<>();d.put("followerUid",user.getUid());d.put("targetUid",uid);d.put("followerName",safeName());d.put("createdAt",FieldValue.serverTimestamp());ref.set(d).addOnSuccessListener(v->toast("Following "+name)).addOnFailureListener(e->toast(msg(e)));
    }
    private void reportUser(String uid,String name){String target=(uid==null||uid.isEmpty())?name:uid;CloudSync.submitReport(this,target,"Party room user report",(ok,m)->runOnUiThread(()->toast(m)));}
    private void hostToggleSeat(int no){
        if(!isModerator()){toast("Host/co-host only");return;} boolean next=!Boolean.TRUE.equals(seatMics.get(no));
        if(cloudRoom&&db!=null)db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(no)).update("micOn",next).addOnFailureListener(e->toast(msg(e)));
        else{seatMics.put(no,next);rebuildSeats();}
    }
    private void hostRemoveSeat(int no){
        if(!isModerator()){toast("Host/co-host only");return;}
        if(cloudRoom&&db!=null)db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(no)).delete().addOnSuccessListener(v->addEvent("seat",safeName()+" removed a mic seat")).addOnFailureListener(e->toast(msg(e)));
        else{localRemovedSeats.add(no);seatNames.remove(no);seatMics.remove(no);rebuildSeats();}
    }
    private void reactionDialog(){
        String[] r={"❤️ Love","😂 Haha","👏 Clap","🔥 Fire","🎉 Party","😍 Wow"};
        new AlertDialog.Builder(this).setTitle("Room reaction").setItems(r,(d,w)->{String emoji=r[w].substring(0,r[w].indexOf(' '));showReactionEffect(emoji);addEvent("reaction",safeName()+" reacted "+emoji);}).setNegativeButton("Close",null).show();
    }
    private void karaokeDialog(){
        String[] songs={"🎵 Romantic Hits","🎤 Bollywood Mix","🎶 Party Beats","💜 Love Songs","🔥 Trending Music","＋ Custom song title"};
        new AlertDialog.Builder(this).setTitle("Sing / Song request").setItems(songs,(d,w)->{
            if(w==songs.length-1){final EditText e=new EditText(this);e.setHint("Song title");new AlertDialog.Builder(this).setTitle("Request a song").setView(e).setPositiveButton("Request",(x,y)->requestSong(e.getText().toString().trim())).setNegativeButton("Cancel",null).show();}
            else requestSong(songs[w].substring(songs[w].indexOf(' ')+1));
        }).setNegativeButton("Close",null).show();
    }
    private void requestSong(String song){if(song==null||song.isEmpty())return;currentSong=song;addEvent("song",safeName()+" requested 🎵 "+song);toast("Song request added: "+song);}
    private void openRoomGames740(){
        if(!cloudRoom||db==null||user==null||roomId==null||roomId.trim().isEmpty()){toast("Join a live Firebase Party room for multiplayer games");return;}
        Intent i=new Intent(this,RoomGameActivity.class);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);i.putExtra("displayName",safeName());startActivity(i);
    }

    private void openGames(){KingNav.openRoot(this,1);}
    private void renameRoomDialog(){if(!isOwner())return;final EditText e=new EditText(this);e.setText(roomName);new AlertDialog.Builder(this).setTitle("Rename room").setView(e).setPositiveButton("Save",(d,w)->{String n=e.getText().toString().trim();if(n.length()<2)return;roomName=n;if(cloudRoom)setRoomValue("name",n);renderParty();}).setNegativeButton("Cancel",null).show();}
    private void changeCategoryDialog(){if(!isOwner())return;String[] cats={"Chat","Date","Music","KTV","Radio","Sing","Game","PK","Pick Me","Family","Multi Video","Event","Birthday","Wedding"};new AlertDialog.Builder(this).setTitle("Room category").setItems(cats,(d,w)->{roomCategory=cats[w];if(cloudRoom)setRoomValue("category",roomCategory);renderParty();}).show();}
    private void changeThemeDialog(){if(!isOwner())return;String[] labels={"Classic Emerald","Ocean KTV","Rose Love","Game Emerald","Royal Purple","Neon Night","Galaxy Stage","Festival Glow","Ice Crystal"};String[] themes={"Classic","KTV","Love","Game","Royal","Neon","Galaxy","Festival","Ice"};new AlertDialog.Builder(this).setTitle("Room theme").setItems(labels,(d,w)->{roomTheme=themes[w];if(cloudRoom)setRoomValue("theme",roomTheme);else prefs.edit().putString("theme_"+roomName,roomTheme).apply();renderParty();}).show();}
    private void changeSeatCount(){if(!isOwner())return;seatLayoutPanel();}

    private void toggleFollow(){if(user==null||ownerUid==null){toast("Firebase sign-in required to follow");return;}String id=user.getUid()+"__"+ownerUid;DocumentReference ref=db.collection("follows").document(id);ref.get().addOnSuccessListener(doc->{if(doc.exists()){ref.delete();followLabel.setText("＋ Follow");}else{Map<String,Object>d=new HashMap<>();d.put("followerUid",user.getUid());d.put("targetUid",ownerUid);d.put("followerName",safeName());d.put("createdAt",FieldValue.serverTimestamp());ref.set(d);followLabel.setText("✓ Following");}});}

    private void attachCloudRoom() {
        attachV530RealtimeHelpers();
        if(db==null||user==null)return;DocumentReference room=db.collection("live_rooms").document(roomId);
        roomListener=room.addSnapshotListener((doc,e)->{if(e!=null||doc==null||!doc.exists())return;announcement=str(doc,"announcement",announcement);roomCategory=str(doc,"category",roomCategory);roomTheme=str(doc,"theme",roomTheme);Long ms=doc.getLong("maxSeats");if(ms!=null){maxSeats=(int)Math.max(8,Math.min(12,ms));}hostSeatMode=Boolean.TRUE.equals(doc.getBoolean("hostSeatMode"));loadRoomPersonalSettings();rebuildSeats();roomLocked=Boolean.TRUE.equals(doc.getBoolean("locked"));muteAll=Boolean.TRUE.equals(doc.getBoolean("muteAll"));roomPrivate=Boolean.TRUE.equals(doc.getBoolean("isPrivate"));roomHasPassword=Boolean.TRUE.equals(doc.getBoolean("hasPassword"));if(announcementLabel!=null)announcementLabel.setText((roomLocked?"🔒  ":"📢  ")+announcement);refreshRoomState();if(Boolean.TRUE.equals(doc.getBoolean("closed"))&&!isOwner()){toast("Room was closed by host");leaveRoom();}});
        roleListener=room.collection("roles").document(user.getUid()).addSnapshotListener((doc,e)->{
            boolean old=coHost;coHost=e==null&&doc!=null&&doc.exists()&&"cohost".equals(doc.getString("role"));refreshRoomState();
            if(old!=coHost&&!isOwner()&&page!=null)page.post(()->renderParty());
        });
        selfMemberListener=room.collection("members").document(user.getUid()).addSnapshotListener((doc,e)->{
            if(e!=null||doc==null)return;
            if(doc.exists())memberSeen=true;
            else if(memberSeen&&!isOwner()){toast("You were removed from this room");leaveRoom();}
        });
        roomBanListener=room.collection("room_bans").document(user.getUid()).addSnapshotListener((doc,e)->{
            if(e==null&&doc!=null&&doc.exists()&&Boolean.TRUE.equals(doc.getBoolean("active"))&&!isOwner()){toast("You are banned from this room");leaveRoom();}
        });
        seatLocksListener=room.collection("seat_locks").addSnapshotListener((snap,e)->{
            if(e!=null||snap==null)return;lockedSeats.clear();
            for(DocumentSnapshot d:snap.getDocuments()){if(Boolean.TRUE.equals(d.getBoolean("locked"))){try{lockedSeats.add(Integer.parseInt(d.getId()));}catch(Exception ignored){}}}
            rebuildSeats();
        });
        seatsListener=room.collection("seats").addSnapshotListener((snap,e)->{if(e!=null||snap==null)return;seatNames.clear();seatUids.clear();seatMics.clear();mySeat=-1;for(DocumentSnapshot d:snap.getDocuments()){int no;try{no=Integer.parseInt(d.getId());}catch(Exception ex){continue;}seatNames.put(no,str(d,"name","Guest"));seatUids.put(no,d.getString("uid"));seatMics.put(no,!Boolean.FALSE.equals(d.getBoolean("micOn")));if(user.getUid().equals(d.getString("uid"))){mySeat=no;micOn=Boolean.TRUE.equals(d.getBoolean("micOn"));}}rebuildSeats();refreshPeopleCounts();if(micLabel!=null){refreshMicControl();}});
        membersListener=room.collection("members").addSnapshotListener((snap,e)->{if(snap==null)return;memberNames.clear();memberUids.clear();memberPhotos540.clear();memberVip720.clear();memberLevel720.clear();memberFrame730.clear();memberEffect730.clear();for(DocumentSnapshot m:snap.getDocuments()){String n=str(m,"name","User"),uid=m.getString("uid");memberNames.add(n);memberUids.put(n,uid);if(uid!=null&&m.getString("photoUrl")!=null)memberPhotos540.put(uid,m.getString("photoUrl"));Long lv=m.getLong("level"),vip=m.getLong("vipLevel");if(uid!=null){memberLevel720.put(uid,lv==null?1:lv.intValue());memberVip720.put(uid,vip==null?0:vip.intValue());String fr=m.getString("equippedFrame"),ef=m.getString("entranceEffect");if(fr!=null)memberFrame730.put(uid,fr);if(ef!=null)memberEffect730.put(uid,ef);}}rebuildSeats();liveMemberCount=snap.size();refreshPeopleCounts();rebuildMemberStrip();});
        attachCloudMusicPlaylist(room);
        attachMusicSync(room);
        attachGameState(room);
        if(isModerator()){
            seatRequestsListener=room.collection("seat_requests").addSnapshotListener((snap,e)->{
                pendingSeatRequests=(e==null&&snap!=null)?snap.size():0;
                if(seatRequestLabel!=null)seatRequestLabel.setText("📥 Requests "+pendingSeatRequests);
                refreshRoomState();
            });
        }
        messagesListener=room.collection("messages").orderBy("createdAt",Query.Direction.ASCENDING).limitToLast(120).addSnapshotListener((snap,e)->{if(e!=null||snap==null||chatBox==null)return;chatBox.removeAllViews();for(DocumentSnapshot d:snap.getDocuments()){String text=str(d,"text","");if("gift".equals(d.getString("type"))&&text.isEmpty()){Long q=d.getLong("giftQty"),v=d.getLong("giftValue");text=str(d,"senderName","User")+" sent "+str(d,"giftIcon","🎁")+" "+str(d,"giftName","Gift")+" to "+str(d,"targetName","User")+" 🎁 x"+(q==null?1:q)+" • 💎"+(v==null?0:v);}addChatRow(str(d,"senderName","User"),text);}});
        eventsListener=room.collection("events").orderBy("createdAt",Query.Direction.ASCENDING).limitToLast(80).addSnapshotListener((snap,e)->{
            if(e!=null||snap==null||feedBox==null)return;feedBox.removeAllViews();refreshSupporters(snap);
            for(DocumentSnapshot d:snap.getDocuments()){
                if(!"live_emoji".equals(d.getString("type")))addFeed(str(d,"text","Room activity"));
                String id=d.getId();if(!eventSnapshotReady){processedEventIds.add(id);continue;}if(processedEventIds.contains(id))continue;processedEventIds.add(id);
                String type=d.getString("type");
                if("gift".equals(type)){Long q=d.getLong("giftQty");Long v=d.getLong("giftValue");String gift=d.getString("giftName");showGiftEffect(str(d,"actorName","User"),str(d,"targetName","Host"),gift,giftIconFor(gift),q==null?1:q,v==null?1:v);}
                else if("join".equals(type)){Long lv=d.getLong("actorLevel"),vip=d.getLong("actorVip");showEntranceEffect730(str(d,"actorName","Guest"),lv==null?1:lv.intValue(),vip==null?0:vip.intValue(),str(d,"actorEffect",vip!=null&&vip>=7?"Galaxy Portal":"Welcome Sparkle"),str(d,"actorFrame",vip!=null&&vip>=7?"Galaxy Frame":"Minimal Frame"));}
                else if("reaction".equals(type)){String text=str(d,"text","");String emoji=text.isEmpty()?"✨":text.substring(text.length()-Math.min(2,text.length()));showReactionEffect(emoji);}
            }
            eventSnapshotReady=true;
        });
    }
    private void registerMember(){if(user==null||db==null||roomId==null)return;LevelSystem.Snapshot p720=LevelSystem.read(this);String frame730=KingCosmetics.frame(this),effect730=KingCosmetics.effect(this);Map<String,Object>d=new HashMap<>();d.put("uid",user.getUid());d.put("name",safeName());d.put("level",p720.level);d.put("vipLevel",p720.vipLevel);d.put("equippedFrame",frame730);d.put("entranceEffect",effect730);if(user.getPhotoUrl()!=null)d.put("photoUrl",user.getPhotoUrl().toString());d.put("joinedAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).collection("members").document(user.getUid()).set(d)
            .addOnSuccessListener(v->{addEvent("join",safeName()+" joined the room");if(page!=null)page.postDelayed(()->showEntranceEffect730(safeName(),p720.level,p720.vipLevel,effect730,frame730),350);})
            .addOnFailureListener(e->toast("Join blocked: private room, room ban, or permission denied"));}
    private void unregisterMember(){if(user!=null&&db!=null&&cloudRoom&&roomId!=null)db.collection("live_rooms").document(roomId).collection("members").document(user.getUid()).delete();}
    private void addEvent(String type,String text){if(cloudRoom&&user!=null&&db!=null&&roomId!=null){LevelSystem.Snapshot p720=LevelSystem.read(this);Map<String,Object>d=new HashMap<>();d.put("actorUid",user.getUid());d.put("actorName",safeName());d.put("actorLevel",p720.level);d.put("actorVip",p720.vipLevel);d.put("actorFrame",KingCosmetics.frame(this));d.put("actorEffect",KingCosmetics.effect(this));d.put("type",type);d.put("text",text);d.put("createdAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).collection("events").add(d);}else addFeed(text);}

    private boolean isOwner(){return cloudRoom?user!=null&&ownerUid!=null&&ownerUid.equals(user.getUid()):displayName.equals(ownerName);}
    private boolean isModerator(){return isOwner() || (cloudRoom && coHost);}
    private String safeName(){if(user!=null){String n=user.getDisplayName();if(n!=null&&!n.trim().isEmpty())return n.trim();String p=user.getPhoneNumber();if(p!=null&&p.length()>=4)return "KING "+p.substring(p.length()-4);}return displayName==null||displayName.trim().isEmpty()?"KING User":displayName;}
    private String shortId(){return shortId(roomId);}
    private String shortId(String id){if(id==null)return "000000";return String.valueOf(Math.abs(id.hashCode()%900000)+100000);}
    private String str(DocumentSnapshot d,String key,String fallback){String v=d.getString(key);return v==null||v.trim().isEmpty()?fallback:v;}
    private String msg(Exception e){return e==null||e.getLocalizedMessage()==null?"unknown error":e.getLocalizedMessage();}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}

    private void leaveRoom(){unregisterMember();clearListeners();renderLobby("Hot");}
    private void addBottomNav(LinearLayout root,int selected){LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER);nav.setBackgroundColor(Color.WHITE);String[] ni={"⌂\nParty","♟\nGame","◇\nDiscover","✉\nMessages","●\nMe"};for(int i=0;i<ni.length;i++){TextView n=tv(ni[i],12,i==selected?0xffd8b900:0xff777777,true);n.setGravity(Gravity.CENTER);final int k=i;n.setOnClickListener(v->{if(k==0){if(roomId!=null)leaveRoom();else renderLobby("Hot");}else KingNav.openRoot(this,k);});nav.addView(n,new LinearLayout.LayoutParams(0,dp(62),1));}root.addView(nav);}

    @Override public void onBackPressed(){if(roomId!=null)leaveRoom();else KingNav.confirmExit(this);}
    @Override protected void onDestroy(){unregisterMember();clearListeners();try{if(roomMusicPlayer!=null){roomMusicPlayer.release();roomMusicPlayer=null;}}catch(Exception ignored){}super.onDestroy();}
}
