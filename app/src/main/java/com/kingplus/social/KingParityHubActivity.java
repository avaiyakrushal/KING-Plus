package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** KING Plus original parity center: KTV stage, PK arena, family/VIP entry and social matching. */
public class KingParityHubActivity extends Activity {
    private static final int BG=0xff170b2d, CARD=0xff2a1747, CARD2=0xff35205a, PURPLE=0xff8a49ed, GOLD=0xffffd768, MUTED=0xffc7b9d8;
    private FirebaseFirestore db; private FirebaseUser me; private LinearLayout body; private TextView title;
    private String route="home", roomId="", roomName="KING Party";
    private SharedPreferences prefs;
    private String homeRoomCategory="Hot";
    private int homeRoomRequest;
    private int routeGeneration;
    private ListenerRegistration homeRoomsListener;
    private DocumentSnapshot homeRoomCursor;
    private final ArrayList<DocumentSnapshot> homeRoomPrevious=new ArrayList<>();

    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    private TextView tv(String s,int z,int c,boolean b){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);if(b)t.setTypeface(null,Typeface.BOLD);t.setPadding(dp(12),dp(7),dp(12),dp(7));return t;}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    private String safe(String s,String f){return s==null||s.trim().isEmpty()?f:s.trim();}
    private String name(){if(me!=null&&me.getDisplayName()!=null&&!me.getDisplayName().trim().isEmpty())return me.getDisplayName().trim();return prefs.getString("name","KING User");}

    @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("MainActivity",MODE_PRIVATE);route=safe(getIntent().getStringExtra("route"),"home");roomId=safe(getIntent().getStringExtra("roomId"),"");roomName=safe(getIntent().getStringExtra("roomName"),"KING Party");try{db=FirebaseFirestore.getInstance();me=FirebaseAuth.getInstance().getCurrentUser();}catch(Exception ignored){}shell();show(route);}

    private void shell(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(8),dp(8),dp(8),0);
        TextView back=tv("‹",38,Color.WHITE,true);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->finish());head.addView(back,new LinearLayout.LayoutParams(dp(52),dp(56)));
        title=tv("KING Center",20,Color.WHITE,true);head.addView(title,new LinearLayout.LayoutParams(0,dp(56),1));root.addView(head);
        HorizontalScrollView hsv=new HorizontalScrollView(this);hsv.setHorizontalScrollBarEnabled(false);LinearLayout tabs=new LinearLayout(this);tabs.setPadding(dp(8),0,dp(8),dp(8));
        String[][] all={{"✨","Home","home"},{"🎤","KTV","ktv"},{"⚔️","PK","pk"},{"🎲","Match","match"},{"🏰","Stage","stage"},{"👑","Family","family"},{"💎","VIP/Rank","vip"}};
        for(String[] x:all){TextView t=tv(x[0]+" "+x[1],12,Color.WHITE,true);t.setGravity(Gravity.CENTER);t.setBackground(bg(CARD2,16));String r=x[2];t.setOnClickListener(v->KingSafe.run(this,"parity-tab:"+r,()->show(r)));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(104),dp(42));lp.setMargins(dp(4),0,dp(4),0);tabs.addView(t,lp);}hsv.addView(tabs);root.addView(hsv,new LinearLayout.LayoutParams(-1,dp(52)));
        ScrollView sv=new ScrollView(this);sv.setFillViewport(true);body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(14),dp(10),dp(14),dp(30));sv.addView(body);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));root.addView(rootNav(),new LinearLayout.LayoutParams(-1,dp(64)));setContentView(root);
        ViewCompat.setOnApplyWindowInsetsListener(root,(view,insets)->{
            Insets safeArea=insets.getInsets(WindowInsetsCompat.Type.systemBars()
                | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(safeArea.left,safeArea.top,safeArea.right,safeArea.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    /** Persistent five-destination navigation shared with the rest of KING Plus. */
    private LinearLayout rootNav(){
        LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER);nav.setPadding(dp(4),dp(4),dp(4),dp(4));nav.setBackgroundColor(0xff211238);
        String[] labels={"⌂\nHome","🎤\nParty","◉\nDiscover","▢\nMessages","♙\nMe"};
        for(int tab=0;tab<labels.length;tab++){
            final int destination=tab;
            TextView item=tv(labels[tab],11,destination==0?GOLD:MUTED,destination==0);item.setGravity(Gravity.CENTER);
            if(destination==0)item.setBackground(bg(CARD2,14));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,1);lp.setMargins(dp(2),0,dp(2),0);nav.addView(item,lp);
            item.setOnClickListener(v->KingSafe.run(this,"home-root-nav-"+destination,()->{
                if(destination==0){if(!"home".equals(route))show("home");}
                else if(destination==1){Intent i=new Intent(this,PartyActivity.class);startActivity(i);}
                else KingNav.openRoot(this,destination==2?2:destination==3?3:4);
            }));
        }
        return nav;
    }

    private void show(String r){++routeGeneration;stopHomeRoomListener();route=r;body.removeAllViews();if(title!=null)title.setText(titleFor(r));if("ktv".equals(r))ktv();else if("pk".equals(r))pk();else if("match".equals(r))match();else if("stage".equals(r))stage();else if("family".equals(r))openFamily();else if("vip".equals(r))vip();else home();}
    private String titleFor(String r){if("ktv".equals(r))return "KTV Stage";if("pk".equals(r))return "PK Arena";if("match".equals(r))return "Match";if("stage".equals(r))return "Party Stage";if("family".equals(r))return "Family";if("vip".equals(r))return "VIP & Rank";return "KING Center";}
    private void hero(String a,String b){TextView h=tv(a+"\n"+b,18,Color.WHITE,true);h.setGravity(Gravity.CENTER_VERTICAL);h.setBackground(bg(CARD,18));h.setPadding(dp(18),dp(16),dp(18),dp(16));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(8));body.addView(h,lp);KingPresentationView visual=new KingPresentationView(this,route);LinearLayout.LayoutParams vp=new LinearLayout.LayoutParams(-1,dp(158));vp.setMargins(0,0,0,dp(12));body.addView(visual,vp);}
    private TextView action(String s,Runnable r){TextView t=tv(s,14,Color.WHITE,true);t.setGravity(Gravity.CENTER);t.setBackground(bg(PURPLE,14));t.setOnClickListener(v->KingSafe.run(this,"parity-action:"+s,r));return t;}
    private void card(String icon,String a,String b,Runnable r){LinearLayout x=new LinearLayout(this);x.setGravity(Gravity.CENTER_VERTICAL);x.setPadding(dp(12),dp(8),dp(12),dp(8));x.setBackground(bg(CARD,16));TextView i=tv(icon,28,Color.WHITE,false);i.setGravity(Gravity.CENTER);x.addView(i,new LinearLayout.LayoutParams(dp(54),dp(62)));LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);TextView aa=tv(a,16,Color.WHITE,true);aa.setPadding(0,0,0,0);TextView bb=tv(b,11,MUTED,false);bb.setPadding(0,dp(2),0,0);info.addView(aa);info.addView(bb);x.addView(info,new LinearLayout.LayoutParams(0,dp(62),1));TextView ar=tv("›",28,GOLD,true);ar.setGravity(Gravity.CENTER);x.addView(ar,new LinearLayout.LayoutParams(dp(36),dp(62)));x.setOnClickListener(v->KingSafe.run(this,"parity-card:"+a,r));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(78));lp.setMargins(0,dp(5),0,dp(5));body.addView(x,lp);}
    private boolean cloud(){if(me==null||db==null){toast("Google / Firebase sign-in required");return false;}return true;}
    private boolean room(){if(!cloud())return false;if(roomId.isEmpty()){toast("Open this from a live Party room");return false;}return true;}

    private void home(){
        // BoloHi-inspired social discovery layout, implemented with original KING Plus UI.
        hero("👑 KING Plus","Meet friends • Join live voice rooms • Share moments");

        // Compact discovery categories, similar to the quick browsing pattern in social-room apps.
        HorizontalScrollView categoriesScroll=new HorizontalScrollView(this);
        categoriesScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout categories=new LinearLayout(this);
        categories.setGravity(Gravity.CENTER_VERTICAL);
        categories.setPadding(0,dp(2),0,dp(10));
        String[][] categoryItems={{"✨","For You","home"},{"🎤","Voice Rooms","party"},{"🎮","Games","games"},{"🆕","New People","match"},{"👑","VIP","vip"}};
        for(String[] item:categoryItems){
            TextView chip=tv(item[0]+"  "+item[1],12,Color.WHITE,true);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(dp(14),0,dp(14),0);
            chip.setBackground(bg(CARD2,22));
            LinearLayout.LayoutParams chipLp=new LinearLayout.LayoutParams(-2,dp(40));
            chipLp.setMargins(0,0,dp(8),0);
            categories.addView(chip,chipLp);
            chip.setOnClickListener(v->KingSafe.run(this,"home-category:"+item[2],()->{
                if("home".equals(item[2]))show("home");
                else if("party".equals(item[2]))openPartyLobby();
                else if("games".equals(item[2]))openPro880("arcade");
                else if("match".equals(item[2]))show("match");
                else show("vip");
            }));
        }
        categoriesScroll.addView(categories);
        body.addView(categoriesScroll,new LinearLayout.LayoutParams(-1,dp(48)));

        LinearLayout quick=new LinearLayout(this);
        quick.setOrientation(LinearLayout.HORIZONTAL);
        quick.setGravity(Gravity.CENTER);
        TextView search=action("⌕  Search rooms",()->showHomeRoomSearch());
        quick.addView(search,new LinearLayout.LayoutParams(0,dp(46),1));
        TextView create=action("＋  Create Room",this::openPartyCreate);
        LinearLayout.LayoutParams createLp=new LinearLayout.LayoutParams(0,dp(46),1);
        createLp.setMargins(dp(8),0,0,0);
        quick.addView(create,createLp);
        LinearLayout.LayoutParams quickLp=new LinearLayout.LayoutParams(-1,dp(46));
        quickLp.setMargins(0,0,0,dp(12));
        body.addView(quick,quickLp);

        LinearLayout liveHeader=new LinearLayout(this);
        liveHeader.setGravity(Gravity.CENTER_VERTICAL);
        liveHeader.addView(tv("Live Voice Rooms",18,Color.WHITE,true),
            new LinearLayout.LayoutParams(0,-2,1));
        body.addView(liveHeader);
        LinearLayout roomRows=new LinearLayout(this);
        roomRows.setOrientation(LinearLayout.VERTICAL);
        liveHeader.addView(action("Refresh",()->loadHomeLiveRooms(roomRows)),
            new LinearLayout.LayoutParams(-2,dp(48)));
        HorizontalScrollView roomFilters=new HorizontalScrollView(this);
        roomFilters.setHorizontalScrollBarEnabled(false);
        LinearLayout filterRow=new LinearLayout(this);
        for(String category:new String[]{"Hot","Popular","Music","Game","Event","Family","Video"}){
            TextView filter=action(category,()->{
                if(!homeRoomCategory.equals(category)){
                    homeRoomCursor=null;
                    homeRoomPrevious.clear();
                }
                homeRoomCategory=category;
                for(int i=0;i<filterRow.getChildCount();i++){
                    TextView option=(TextView)filterRow.getChildAt(i);
                    option.setSelected(category.contentEquals(option.getText()));
                    option.setBackground(bg(option.isSelected()?PURPLE:CARD2,14));
                }
                loadHomeLiveRooms(roomRows);
            });
            filter.setSelected(category.equals(homeRoomCategory));
            filter.setBackground(bg(filter.isSelected()?PURPLE:CARD2,14));
            LinearLayout.LayoutParams filterParams=new LinearLayout.LayoutParams(-2,dp(48));
            filterParams.setMargins(0,0,dp(6),0);
            filterRow.addView(filter,filterParams);
        }
        roomFilters.addView(filterRow);
        body.addView(roomFilters,new LinearLayout.LayoutParams(-1,dp(52)));
        body.addView(roomRows,new LinearLayout.LayoutParams(-1,-2));
        loadHomeLiveRooms(roomRows);

        TextView discoverTitle=tv("Discover more on KING Plus",17,Color.WHITE,true);
        discoverTitle.setPadding(dp(2),dp(12),dp(2),dp(4));
        body.addView(discoverTitle);
        card("🎤","Voice Party","Join friends and talk together in live rooms",this::openPartyLobby);
        card("🎮","Games & Activities","Explore games and interactive activities",()->openPro880("arcade"));
        card("💬","Meet New People","Discover KING Plus members and profiles",()->show("match"));
        card("🎵","KTV Stage","Shared song requests and singer stage",()->show("ktv"));
        card("⚔️","PK Arena","Red vs Blue room battle and score",()->show("pk"));
        // Keep the original destinations individually reachable; do not merge
        // Family and VIP into one card that would silently hide the VIP route.
        card("🏰","Party Stage","Switch room scene and theme: Royal, KTV, PK, Galaxy",()->show("stage"));
        card("👑","Family","Family community and member center",()->show("family"));
        card("💎","VIP & Rank","VIP privileges, cosmetics and public rankings",()->show("vip"));
    }

    private void openPartyLobby(){Intent i=new Intent(this,PartyActivity.class);startActivity(i);}
    private void openPartyCreate(){Intent i=new Intent(this,PartyActivity.class);i.putExtra("openCreateRoom",true);startActivity(i);}

    private void showHomeRoomSearch(){
        EditText query=new EditText(this);query.setSingleLine(true);query.setHint("Room name, host or room ID");
        new AlertDialog.Builder(this).setTitle("Search live Party rooms").setView(query)
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Search",(d,w)->{String q=query.getText().toString().trim();if(q.isEmpty()){openPartyLobby();return;}Intent i=new Intent(this,PartyActivity.class);i.putExtra("lobbySearch",q);startActivity(i);})
            .show();
    }

    private boolean canShowHomeRooms(LinearLayout rows,int request,String uid){
        FirebaseUser current=FirebaseAuth.getInstance().getCurrentUser();
        return !isFinishing()&&!isDestroyed()&&"home".equals(route)
            && rows.getParent()==body&&request==homeRoomRequest
            && current!=null&&uid.equals(current.getUid());
    }

    private void loadHomeLiveRooms(LinearLayout roomRows){
        stopHomeRoomListener();
        int request=++homeRoomRequest;
        roomRows.removeAllViews();
        FirebaseUser signedIn=db==null?null:FirebaseAuth.getInstance().getCurrentUser();
        if(signedIn==null){
            roomRows.addView(action("Sign in to discover live rooms",()->{
                Intent intent=new Intent(this,MainActivity.class);
                intent.putExtra("forceLogin",true);
                startActivity(intent);
            }),new LinearLayout.LayoutParams(-1,dp(56)));
            return;
        }
        String uid=signedIn.getUid();
        roomRows.addView(tv("Loading live rooms…",13,MUTED,false));
        if("Popular".equals(homeRoomCategory)){
            loadPopularHomeRooms(roomRows,request,uid);
            return;
        }
        Query query=db.collection("live_rooms").whereEqualTo("closed",false);
        List<String> aliases=KingPagePolicy10067.categories(homeRoomCategory);
        if(!aliases.isEmpty())query=query.whereIn("category",aliases);
        query=query.orderBy("createdAt",Query.Direction.DESCENDING);
        if(homeRoomCursor!=null)query=query.startAfter(homeRoomCursor);
        homeRoomsListener=query.limit(8).addSnapshotListener((snapshot,error)->{
            if(!canShowHomeRooms(roomRows,request,uid))return;
            if(error!=null){
                roomRows.removeAllViews();
                roomRows.addView(action("Could not load rooms. Tap to retry.",
                    ()->loadHomeLiveRooms(roomRows)),new LinearLayout.LayoutParams(-1,-2));
                return;
            }
            roomRows.removeAllViews();
            boolean cached=snapshot.getMetadata().isFromCache();
            if(cached)roomRows.addView(tv("Showing saved rooms. Joining requires a connection.",13,MUTED,false));
            List<DocumentSnapshot> rooms=snapshot.getDocuments();
            for(DocumentSnapshot doc:rooms)addHomeRoomCard(roomRows,doc);
            if(rooms.isEmpty())roomRows.addView(tv(cached
                ?"No saved rooms available. Connect and refresh."
                :"No live rooms right now.",13,MUTED,false));
            LinearLayout pageControls=new LinearLayout(this);
            pageControls.setGravity(Gravity.CENTER);
            if(!homeRoomPrevious.isEmpty()){
                pageControls.addView(action("‹ Previous",()->{
                    homeRoomCursor=homeRoomPrevious.remove(homeRoomPrevious.size()-1);
                    loadHomeLiveRooms(roomRows);
                }),new LinearLayout.LayoutParams(0,dp(44),1));
            }
            if(snapshot.size()==8){
                TextView next=action("More rooms ›",()->{
                    if(snapshot.getDocuments().isEmpty())return;
                    homeRoomPrevious.add(homeRoomCursor);
                    homeRoomCursor=snapshot.getDocuments().get(snapshot.size()-1);
                    loadHomeLiveRooms(roomRows);
                });
                LinearLayout.LayoutParams nextParams=new LinearLayout.LayoutParams(0,dp(44),1);
                nextParams.setMargins(dp(8),0,0,0);
                pageControls.addView(next,nextParams);
            }
            if(pageControls.getChildCount()>0)roomRows.addView(pageControls,
                new LinearLayout.LayoutParams(-1,dp(48)));
            roomRows.addView(action("View all Party Rooms",this::openPartyLobby),
                new LinearLayout.LayoutParams(-1,dp(48)));
        });
    }

    private void stopHomeRoomListener(){
        if(homeRoomsListener!=null){
            homeRoomsListener.remove();
            homeRoomsListener=null;
        }
    }

    private Task<QuerySnapshot> readRecentRooms(Query filter,int limit,Source source){
        return filter.orderBy("createdAt",Query.Direction.DESCENDING).limit(limit).get(source)
            .continueWithTask(read->{
                Exception error=read.getException();
                if(error instanceof FirebaseFirestoreException
                    &&((FirebaseFirestoreException)error).getCode()==FirebaseFirestoreException.Code.FAILED_PRECONDITION)
                    return filter.limit(limit).get(source);
                return read;
            });
    }

    private void loadPopularHomeRooms(LinearLayout rows,int request,String uid){
        Query recentQuery=db.collection("live_rooms").whereEqualTo("closed",false);
        if(homeRoomCursor!=null)recentQuery=recentQuery.startAfter(homeRoomCursor);
        readRecentRooms(recentQuery,24,Source.SERVER)
            .addOnSuccessListener(snapshot->{
                if(!canShowHomeRooms(rows,request,uid))return;
                Map<DocumentSnapshot,Task<AggregateQuerySnapshot>> counts=new LinkedHashMap<>();
                Timestamp cutoff=new Timestamp(new Date(System.currentTimeMillis()-60_000L));
                for(DocumentSnapshot room:snapshot.getDocuments()){
                    String mode=roomText(room,"entranceMode","legacy");
                    if(Boolean.TRUE.equals(room.get("isPrivate"))||Boolean.TRUE.equals(room.get("hasPassword"))
                        ||(!"legacy".equals(mode)&&!"free".equals(mode)))continue;
                    counts.put(room,room.getReference().collection("members")
                        .whereGreaterThan("lastSeenAt",cutoff).count().get(AggregateSource.SERVER));
                }
                Tasks.whenAllComplete(counts.values()).addOnSuccessListener(ignored->{
                    if(!canShowHomeRooms(rows,request,uid))return;
                    List<DocumentSnapshot> ranked=new ArrayList<>();
                    Map<DocumentSnapshot,Long> recent=new HashMap<>();
                    for(Map.Entry<DocumentSnapshot,Task<AggregateQuerySnapshot>> entry:counts.entrySet()){
                        Task<AggregateQuerySnapshot> count=entry.getValue();
                        if(count.isSuccessful()){
                            long active=count.getResult().getCount();
                            if(active>0)ranked.add(entry.getKey());
                            recent.put(entry.getKey(),active);
                        }
                    }
                    if(!counts.isEmpty()&&recent.isEmpty()){
                        showPopularError(rows);return;
                    }
                    rows.removeAllViews();
                    rows.addView(tv("Activity in up to 24 open rooms · last 60 seconds",12,MUTED,false));
                    if(recent.size()<counts.size())rows.addView(tv("Some activity counts were unavailable.",12,MUTED,false));
                    ranked.sort((a,b)->Long.compare(recent.get(b),recent.get(a)));
                    for(DocumentSnapshot room:ranked.subList(0,Math.min(8,ranked.size())))
                        addHomeRoomCard(rows,room,recent.get(room)+" active");
                    if(ranked.isEmpty())rows.addView(tv("No active public rooms in these results.",13,MUTED,false));
                    LinearLayout pageControls=new LinearLayout(this);
                    pageControls.setGravity(Gravity.CENTER);
                    if(!homeRoomPrevious.isEmpty()){
                        pageControls.addView(action("‹ Previous",()->{
                            homeRoomCursor=homeRoomPrevious.remove(homeRoomPrevious.size()-1);
                            loadHomeLiveRooms(rows);
                        }),new LinearLayout.LayoutParams(0,dp(44),1));
                    }
                    if(snapshot.size()==24){
                        TextView next=action("More rooms ›",()->{
                            if(snapshot.isEmpty())return;
                            homeRoomPrevious.add(homeRoomCursor);
                            homeRoomCursor=snapshot.getDocuments().get(snapshot.size()-1);
                            loadHomeLiveRooms(rows);
                        });
                        LinearLayout.LayoutParams nextParams=new LinearLayout.LayoutParams(0,dp(44),1);
                        nextParams.setMargins(dp(8),0,0,0);
                        pageControls.addView(next,nextParams);
                    }
                    if(pageControls.getChildCount()>0)rows.addView(pageControls,
                        new LinearLayout.LayoutParams(-1,dp(48)));
                    rows.addView(action("View all Party Rooms",this::openPartyLobby),
                        new LinearLayout.LayoutParams(-1,dp(48)));
                });
            })
            .addOnFailureListener(error->{
                if(canShowHomeRooms(rows,request,uid))showPopularError(rows);
            });
    }

    private void showPopularError(LinearLayout rows){
        rows.removeAllViews();
        rows.addView(action("Could not load Popular rooms. Tap to retry.",
            ()->loadHomeLiveRooms(rows)),new LinearLayout.LayoutParams(-1,-2));
    }

    private String roomText(DocumentSnapshot room,String field,String fallback){
        Object value=room.get(field);
        return value instanceof String?safe((String)value,fallback):fallback;
    }

    private void addHomeRoomCard(LinearLayout rows,DocumentSnapshot doc){
        addHomeRoomCard(rows,doc,"");
    }

    private void addHomeRoomCard(LinearLayout rows,DocumentSnapshot doc,String activity){
        String name=roomText(doc,"name","Live Party");
        String host=roomText(doc,"ownerName","KING Host");
        String category=roomText(doc,"category","Hot");
        boolean locked=Boolean.TRUE.equals(doc.get("isPrivate"))||Boolean.TRUE.equals(doc.get("hasPassword"));
        LinearLayout item=new LinearLayout(this);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(10),dp(8),dp(10),dp(8));
        item.setBackground(bg(CARD,14));

        FrameLayout thumbnail=new FrameLayout(this);
        thumbnail.setBackground(bg(CARD2,10));
        thumbnail.setClipToOutline(true);
        ImageView image=new ImageView(this);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        thumbnail.addView(image,new FrameLayout.LayoutParams(-1,-1));
        TextView fallback=tv("🎤",18,GOLD,false);
        fallback.setGravity(Gravity.CENTER);
        thumbnail.addView(fallback,new FrameLayout.LayoutParams(-1,-1));
        item.addView(thumbnail,new LinearLayout.LayoutParams(dp(54),dp(54)));

        LinearLayout details=new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setPadding(dp(10),0,0,0);
        TextView label=tv(name,14,Color.WHITE,true);
        label.setPadding(0,0,0,0);
        label.setMaxLines(2);
        label.setEllipsize(TextUtils.TruncateAt.END);
        details.addView(label);
        TextView meta=tv(category+" • "+host+(activity.isEmpty()?"":" • "+activity),12,MUTED,false);
        meta.setPadding(0,dp(2),0,0);
        meta.setMaxLines(2);
        meta.setEllipsize(TextUtils.TruncateAt.END);
        details.addView(meta);
        item.addView(details,new LinearLayout.LayoutParams(0,-2,1));
        item.addView(tv(locked?"🔒":"Join",12,GOLD,true));
        item.setContentDescription("Join "+name+", "+category+", hosted by "+host
            +(locked?", restricted room":""));
        item.setOnClickListener(v->{
            Intent intent=new Intent(this,PartyActivity.class);
            intent.putExtra("directRoomId",doc.getId());
            startActivity(intent);
        });
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2);
        params.setMargins(0,dp(4),0,dp(4));
        rows.addView(item,params);
        KingProfileImages10068.load(this,image,fallback,
            roomText(doc,"coverUrl",roomText(doc,"ownerPhoto","")));
    }

    private boolean isCurrentRoute(int generation){
        return generation==routeGeneration&&!isFinishing()&&!isDestroyed();
    }

    private void ktv(){final int generation=routeGeneration;hero("🎤 KTV • "+roomName,roomId.isEmpty()?"Open from Party Room for synchronized queue":"Room-synchronized requests • host/co-host can put a song on stage");if(!roomId.isEmpty()){LinearLayout buttons=new LinearLayout(this);TextView add=action("＋ Request song",this::requestSong);buttons.addView(add,new LinearLayout.LayoutParams(0,dp(48),1));TextView refresh=action("↻ Refresh",()->show("ktv"));LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,dp(48),1);rp.setMargins(dp(8),0,0,0);buttons.addView(refresh,rp);body.addView(buttons);}
        if(!room())return;db.collection("live_rooms").document(roomId).get().addOnSuccessListener(d->{if(!isCurrentRoute(generation))return;String current=safe(d.getString("currentSong"),"No song on stage");TextView now=tv("🎙 NOW ON STAGE\n"+current,16,GOLD,true);now.setGravity(Gravity.CENTER);now.setBackground(bg(0xff3c245e,16));body.addView(now,new LinearLayout.LayoutParams(-1,dp(78)));});
        body.addView(tv("Queue",16,Color.WHITE,true));db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(100).get().addOnSuccessListener(q->{if(!isCurrentRoute(generation))return;int shown=0;for(DocumentSnapshot d:q.getDocuments()){if(!"ktv_request".equals(d.getString("type")))continue;String song=safe(d.getString("songName"),safe(d.getString("text"),"Song"));String who=safe(d.getString("actorName"),"User");TextView row=tv("🎵 "+song+"\n   requested by "+who,14,Color.WHITE,true);row.setBackground(bg(CARD,14));row.setOnClickListener(v->startSong(song));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(64));lp.setMargins(0,dp(4),0,dp(4));body.addView(row,lp);if(++shown>=25)break;}if(shown==0)body.addView(tv("No song requests yet.",13,MUTED,false));}).addOnFailureListener(e->{if(isCurrentRoute(generation))body.addView(tv("KTV queue unavailable",12,MUTED,false));});}
    private void requestSong(){if(!room())return;EditText e=new EditText(this);e.setHint("Song title");e.setSingleLine(true);new AlertDialog.Builder(this).setTitle("Request KTV song").setView(e).setPositiveButton("Request",(d,w)->{String s=e.getText().toString().trim();if(s.isEmpty())return;Map<String,Object>m=new HashMap<>();m.put("actorUid",me.getUid());m.put("actorName",name());m.put("type","ktv_request");m.put("songName",s);m.put("text",name()+" requested 🎵 "+s);m.put("createdAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).collection("events").add(m).addOnSuccessListener(v->{toast("Added to KTV queue");show("ktv");}).addOnFailureListener(x->toast("Request failed"));}).setNegativeButton("Cancel",null).show();}
    private void startSong(String song){if(!room())return;new AlertDialog.Builder(this).setTitle(song).setMessage("Host/co-host can put this song on the room stage.").setPositiveButton("Start on stage",(d,w)->{Map<String,Object>m=new HashMap<>();m.put("currentSong",song);m.put("updatedAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).update(m).addOnSuccessListener(v->{toast("KTV stage updated");show("ktv");}).addOnFailureListener(e->toast("Host/co-host permission required"));}).setNegativeButton("Close",null).show();}

    private void pk(){hero("⚔️ Audio PK • "+roomName,"Join Red or Blue. Gifts sent by team members increase the live PK score.");if(!room())return;LinearLayout join=new LinearLayout(this);TextView red=action("🔴 Join Red",()->joinPk("red"));red.setBackground(bg(0xffb93852,14));join.addView(red,new LinearLayout.LayoutParams(0,dp(50),1));TextView blue=action("🔵 Join Blue",()->joinPk("blue"));blue.setBackground(bg(0xff356ac3,14));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,dp(50),1);bp.setMargins(dp(8),0,0,0);join.addView(blue,bp);body.addView(join);TextView start=action("⚡ Start / reset PK round",this::startPk);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(48));sp.setMargins(0,dp(8),0,dp(10));body.addView(start,sp);loadPkScore();}
    private void joinPk(String team){if(!room())return;Map<String,Object>m=new HashMap<>();m.put("actorUid",me.getUid());m.put("actorName",name());m.put("type","pk_join");m.put("team",team);m.put("text",name()+" joined "+("red".equals(team)?"Red":"Blue")+" PK team");m.put("createdAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).collection("events").add(m).addOnSuccessListener(v->{toast("Joined "+team+" team");show("pk");}).addOnFailureListener(e->toast("Could not join PK"));}
    private void startPk(){if(!room())return;Map<String,Object>m=new HashMap<>();m.put("active",true);m.put("roundId",System.currentTimeMillis());m.put("actorUid",me.getUid());m.put("startedAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).collection("room_settings").document("pk").set(m).addOnSuccessListener(v->toast("PK round started")).addOnFailureListener(e->toast("Host/co-host permission required"));}
    private void loadPkScore(){
        final int generation=routeGeneration;
        com.google.firebase.firestore.DocumentReference room=db.collection("live_rooms").document(roomId);
        room.collection("room_settings").document("pk").get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(round->{
                if(!isCurrentRoute(generation))return;
                Object started=round.get("startedAt");
                if(!Boolean.TRUE.equals(round.get("active"))||!(started instanceof Timestamp)){
                    renderPkScore("No active PK round");
                    return;
                }
                room.collection("events").whereGreaterThanOrEqualTo("createdAt",started)
                    .orderBy("createdAt",Query.Direction.ASCENDING).get(com.google.firebase.firestore.Source.SERVER)
                    .addOnSuccessListener(events->{
                        if(!isCurrentRoute(generation))return;
                        Map<String,String> teams=new HashMap<>();
                        long red=0,blue=0;
                        for(DocumentSnapshot event:events.getDocuments()){
                            Object actor=event.get("actorUid");
                            if(!(actor instanceof String))continue;
                            if("pk_join".equals(event.get("type"))){
                                Object team=event.get("team");
                                if("red".equals(team)||"blue".equals(team))teams.put((String)actor,(String)team);
                            }else if("gift".equals(event.get("type"))&&event.get("giftValue") instanceof Number){
                                long value=Math.max(0,((Number)event.get("giftValue")).longValue());
                                if("red".equals(teams.get(actor)))red+=value;
                                else if("blue".equals(teams.get(actor)))blue+=value;
                            }
                        }
                        renderPkScore("Red "+red+" · Blue "+blue);
                    })
                    .addOnFailureListener(error->{if(isCurrentRoute(generation))renderPkScore("PK score unavailable");});
            })
            .addOnFailureListener(error->{if(isCurrentRoute(generation))renderPkScore("PK round unavailable");});
    }

    private void renderPkScore(String text){
        TextView score=tv(text,20,Color.WHITE,true);
        score.setGravity(Gravity.CENTER);
        score.setBackground(bg(CARD2,18));
        body.addView(score,new LinearLayout.LayoutParams(-1,dp(84)));
        body.addView(action("Refresh score",()->show("pk")),new LinearLayout.LayoutParams(-1,dp(48)));
    }

    private void match(){final int generation=routeGeneration;hero("🎲 KING Match","Browse real public KING Plus profiles. Open a card to follow, message or send gifts.");card("🔎","Search KING profiles","Find by name or 6-digit KING ID",this::matchSearch910);if(!cloud())return;db.collection("public_profiles").limit(40).get().addOnSuccessListener(q->{if(!isCurrentRoute(generation))return;List<DocumentSnapshot>list=new ArrayList<>(q.getDocuments());Collections.shuffle(list);int n=0;for(DocumentSnapshot d:list){String uid=safe(d.getString("uid"),d.getId());if(me!=null&&uid.equals(me.getUid()))continue;String nm=safe(d.getString("displayName"),"KING User");Long lv=d.getLong("level"),vip=d.getLong("vipLevel");TextView row=tv("👤  "+nm+"   💎 VIP "+(vip==null?0:vip)+"\n     KING ID "+(KingPublicId9996.verified(d.getString("publicId"))==null?"Not issued":KingPublicId9996.verified(d.getString("publicId")))+" • Lv."+(lv==null?1:lv),14,Color.WHITE,true);row.setBackground(bg(CARD,16));row.setOnClickListener(v->openProfile(uid,nm));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(70));lp.setMargins(0,dp(5),0,dp(5));body.addView(row,lp);if(++n>=24)break;}if(n==0)body.addView(tv("No public profiles yet.",13,MUTED,false));}).addOnFailureListener(e->{if(isCurrentRoute(generation))body.addView(tv("Match list unavailable",13,MUTED,false));});}
    private void matchSearch910(){
        if(!cloud())return;final EditText e=new EditText(this);e.setHint("Name or KING ID");e.setSingleLine(true);new AlertDialog.Builder(this).setTitle("Find KING user").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Search",(d,w)->{String q=e.getText().toString().trim().toLowerCase(Locale.US);if(q.isEmpty())return;
        int generation=routeGeneration;
        Query profiles=db.collection("public_profiles");
        Query search=KingPublicId9996.verified(q)!=null
            ?profiles.whereEqualTo("publicId",q).limit(30):profiles.limit(100);
        search.get().addOnSuccessListener(snap->{if(!isCurrentRoute(generation))return;ArrayList<String>labels=new ArrayList<>();ArrayList<DocumentSnapshot>docs=new ArrayList<>();for(DocumentSnapshot p:snap.getDocuments()){String uid=safe(p.getString("uid"),p.getId()),nm=safe(p.getString("displayName"),"KING User"),id=(KingPublicId9996.verified(p.getString("publicId"))==null?"Not issued":KingPublicId9996.verified(p.getString("publicId")));if(nm.toLowerCase(Locale.US).contains(q)||id.equalsIgnoreCase(q)){docs.add(p);labels.add(nm+" • KING ID "+id);if(docs.size()>=30)break;}}if(docs.isEmpty()){toast("No matching KING profile");return;}new AlertDialog.Builder(this).setTitle("Matches").setItems(labels.toArray(new String[0]),(x,i)->openProfile(safe(docs.get(i).getString("uid"),docs.get(i).getId()),safe(docs.get(i).getString("displayName"),"KING User"))).setNegativeButton("Close",null).show();}).addOnFailureListener(err->toast("Profile search unavailable"));}).show();
    }

    private String publicId(String uid){long h=Math.abs((long)safe(uid,"").hashCode());return String.format(Locale.US,"%06d",h%1000000L);}
    private void openProfile(String uid,String nm){Intent i=new Intent(this,KingPublicProfileActivity.class);i.putExtra("uid",uid);i.putExtra("name",nm);startActivity(i);}

    private void stage(){hero("🏰 Party Stage","Original KING Plus room scenes. Host can apply a scene to the live room.");String[] themes={"Royal","KTV","PK","Galaxy","Festival","Ice","Neon","Love","Game"};String[] icons={"👑","🎤","⚔️","🌌","🎉","❄️","💜","💗","🎮"};for(int i=0;i<themes.length;i++){String th=themes[i],ic=icons[i];card(ic,th+" Stage",stageDescription(th),()->applyTheme(th));}}
    private String stageDescription(String t){if("KTV".equals(t))return "Blue music stage with singer focus";if("PK".equals(t))return "Red/blue battle presentation";if("Royal".equals(t))return "Gold VIP room presentation";if("Galaxy".equals(t))return "Purple cosmic party scene";return t+" room presentation";}
    private void applyTheme(String theme){if(!room())return;Map<String,Object>m=new HashMap<>();m.put("theme",theme);m.put("updatedAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).update(m).addOnSuccessListener(v->toast(theme+" stage applied")).addOnFailureListener(e->toast("Host permission required"));}

    private void openFamily(){hero("👑 Family Square","Family level • treasury • members • activity • family voice");card("👑","Open Family Pro","Family progress, treasury, member list and daily activity",()->openPro880("family"));card("💬","Family Chat","Open family community chat",()->{Intent i=new Intent(this,CommunityHubActivity.class);i.putExtra("tab","Family");startActivity(i);});card("🎙","Family Voice","Open Family Pro voice controls",()->openPro880("family"));}
    private void openPro880(String route){Intent i=new Intent(this,KingEcosystemProActivity.class);i.putExtra("route",route);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);startActivity(i);}
    private void vip(){startActivity(new Intent(this,KingVipVisualActivity.class));finish();if(true)return;/* legacy parity fallback */} private void vipLegacy940(){hero("💎 VIP & Rank","VIP remains progression-based in this no-billing build. Levels come from real KING Plus activity.");LevelSystem.Snapshot p=LevelSystem.read(this);TextView meCard=tv("👑 "+name()+"\nVIP "+p.vipLevel+" • "+LevelSystem.vipName(p.vipLevel)+"   |   Lv."+p.level+" • "+LevelSystem.levelTier(p.level)+"\n"+p.vipPoints+" VIP points • "+p.xp+" XP",17,Color.WHITE,true);meCard.setBackground(bg(CARD2,18));body.addView(meCard,new LinearLayout.LayoutParams(-1,dp(106)));LinearLayout acts=new LinearLayout(this);TextView perks=action("VIP privileges",()->{Intent i=new Intent(this,KingDeepFlowActivity.class);i.putExtra("route","vip");startActivity(i);});acts.addView(perks,new LinearLayout.LayoutParams(0,dp(48),1));TextView collection=action("Collection",()->{Intent i=new Intent(this,CommunityHubActivity.class);i.putExtra("tab","Collection");startActivity(i);});LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(48),1);cp.setMargins(dp(8),0,0,0);acts.addView(collection,cp);body.addView(acts);body.addView(tv("Public level ranking",16,Color.WHITE,true));if(!cloud())return;db.collection("public_profiles").orderBy("level",Query.Direction.DESCENDING).limit(30).get().addOnSuccessListener(q->{int rank=1;for(DocumentSnapshot d:q.getDocuments()){String uid=safe(d.getString("uid"),d.getId()),nm=safe(d.getString("displayName"),"KING User");Long lv=d.getLong("level"),vv=d.getLong("vipLevel");TextView row=tv((rank<=3?(rank==1?"🥇":rank==2?"🥈":"🥉"):"#"+rank)+"   "+nm+"   • Lv."+(lv==null?1:lv)+" • VIP "+(vv==null?0:vv),13,Color.WHITE,true);row.setBackground(bg(CARD,12));row.setOnClickListener(v->openProfile(uid,nm));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(48));lp.setMargins(0,dp(3),0,dp(3));body.addView(row,lp);rank++;}}).addOnFailureListener(e->body.addView(tv("Ranking unavailable",13,MUTED,false)));}
}
