package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DiscoverActivity extends Activity {
    private static final int DARK=0xff15131a, MUTED=0xff8d8993, YELLOW=0xffffdf18, PURPLE=0xff7c4dff;
    private FirebaseUser me;
    private FirebaseFirestore db;
    private LinearLayout content;
    private int gameScore, chatScore, eventScore;
    private String displayName="KING User";
    private boolean statsExplicit10024=false;
    private String activePeopleTab="Hot";
    // One authenticated live Follow subscription serves ALL profile cards and recommendations.
    // This prevents one Firestore listener per card and refreshes on changes from another phone.
    private com.google.firebase.firestore.ListenerRegistration followListener10061;
    private final Map<String,List<TextView>> followButtons10061=new HashMap<>();
    private final Set<String> liveFollowing10061=new HashSet<>();
    private final Set<String> pendingFollow10061=new HashSet<>();
    private int followListenerGeneration10061;
    private boolean followLoaded10061;
    private String followOwner10061="";
    private android.net.ConnectivityManager.NetworkCallback discoverNetworkCallback940;
    private boolean discoverLastOnline940;

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        try{me=FirebaseAuth.getInstance().getCurrentUser();}catch(Exception ignored){me=null;}
        try{db=FirebaseFirestore.getInstance();}catch(Exception ignored){db=null;}
        Intent in=getIntent();
        if(in!=null){
            displayName=safe(in.getStringExtra("displayName"),displayName);
            statsExplicit10024=in.hasExtra("gameScore")||in.hasExtra("chatScore")||in.hasExtra("eventScore");
            gameScore=Math.max(0,in.getIntExtra("gameScore",0));
            chatScore=Math.max(0,in.getIntExtra("chatScore",0));
            eventScore=Math.max(0,in.getIntExtra("eventScore",0));
        }
        publishStats();
        renderDiscover();
        if(!statsExplicit10024)loadDiscoverStats10030(me==null?null:me.getUid());
        discoverLastOnline940=KingNetwork.online(this);
        discoverNetworkCallback940=KingNetwork.watch(this,online->runOnUiThread(()->{
            boolean recovered=online&&!discoverLastOnline940;discoverLastOnline940=online;
            if(recovered&&!isFinishing()&&!isDestroyed()
                &&content!=null&&content.isAttachedToWindow())renderDiscover();
        }));
    }

    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private TextView tv(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,Typeface.BOLD);return t;}
    private String safe(String s,String fallback){return s==null||s.trim().isEmpty()?fallback:s.trim();}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    private void setSafe(View root){setContentView(root);final int l=root.getPaddingLeft(),t=root.getPaddingTop(),r=root.getPaddingRight(),b=root.getPaddingBottom();ViewCompat.setOnApplyWindowInsetsListener(root,(v,i)->{Insets x=i.getInsets(WindowInsetsCompat.Type.systemBars());v.setPadding(l,t+x.top,r,b+x.bottom);return i;});ViewCompat.requestApplyInsets(root);}

    private void publishStats(){
        if(me==null||db==null||!statsExplicit10024)return;
        FirebaseUser current10024=FirebaseAuth.getInstance().getCurrentUser();
        if(current10024==null||!me.getUid().equals(current10024.getUid()))return;
        Map<String,Object> x=new HashMap<>();x.put("uid",me.getUid());
        Intent in10024=getIntent();
        if(in10024!=null&&in10024.hasExtra("gameScore"))x.put("gameScore",gameScore);
        if(in10024!=null&&in10024.hasExtra("chatScore"))x.put("chatScore",chatScore);
        if(in10024!=null&&in10024.hasExtra("eventScore"))x.put("eventScore",eventScore);
        x.put("updatedAt",FieldValue.serverTimestamp());
        db.collection("discover_stats").document(me.getUid())
            .set(x,com.google.firebase.firestore.SetOptions.merge()).addOnFailureListener(e->{});
    }

    private void loadDiscoverStats10030(String owner10030){
        if(owner10030==null||db==null)return;
        db.collection("discover_stats").document(owner10030)
            .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(doc10030->{
                FirebaseUser signed10030=FirebaseAuth.getInstance().getCurrentUser();
                if(isFinishing()||isDestroyed()||signed10030==null
                    ||!owner10030.equals(signed10030.getUid())||me==null
                    ||!owner10030.equals(me.getUid())||statsExplicit10024)return;
                gameScore=verifiedScore10030(doc10030,"gameScore");
                chatScore=verifiedScore10030(doc10030,"chatScore");
                eventScore=verifiedScore10030(doc10030,"eventScore");
                // Only refresh the actual landing view, never replace an open profile.
                if(content!=null&&content.isAttachedToWindow())renderDiscover();
            }).addOnFailureListener(error10030->{
                // Unverified values remain zero; never represent them as paid Diamonds.
            });
    }
    private int verifiedScore10030(DocumentSnapshot doc10030,String field10030){
        Object n10030=doc10030==null?null:doc10030.get(field10030);
        if(!(n10030 instanceof Number))return 0;
        return (int)Math.min(Integer.MAX_VALUE,Math.max(0,((Number)n10030).longValue()));
    }

    private void renderDiscover(){
        followButtons10061.clear();
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackground(KingRoyalTheme10021.page());
        root.addView(header(),new LinearLayout.LayoutParams(-1,dp(74)));
        ScrollView sv=new ScrollView(this);content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(14),dp(12),dp(14),dp(36));sv.addView(content);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        addDiscoverEntrances940();
        addMatchTabs940();
        loadProfileGrid940();
        sectionTitle("Ranks & influence","More",()->showRankingPage("Chat Talent","chatScore"));
        addSmallRankingRow();
        sectionTitle("Live recommendations","Activity Hall",this::openActivityHall);
        loadActivityRecommendations();
        sectionTitle("Game Master","More",()->showRankingPage("Game Master","gameScore"));
        loadTalentGrid("gameScore",false);
        root.addView(bottomNav(),new LinearLayout.LayoutParams(-1,dp(72)));
        setSafe(root);
    }

    private View header(){
        LinearLayout h=new LinearLayout(this);h.setGravity(Gravity.CENTER_VERTICAL);h.setPadding(dp(16),dp(4),dp(10),0);h.setBackground(KingRoyalTheme10021.header(this));
        TextView title=tv("Discover",31,0xff080808,true);h.addView(title,new LinearLayout.LayoutParams(0,dp(62),1));
        TextView search=tv("⌕",34,0xff111111,false);search.setGravity(Gravity.CENTER);search.setOnClickListener(v->startActivity(new Intent(this,SocialActivity.class)));h.addView(search,new LinearLayout.LayoutParams(dp(56),dp(58)));
        TextView influence=tv("✦",27,0xff111111,true);influence.setGravity(Gravity.CENTER);influence.setBackground(bg(0xfff4f3f6,22));influence.setOnClickListener(v->showInfluence("Chat Talent",chatScore));LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(48),dp(48));ip.setMargins(dp(4),0,0,0);h.addView(influence,ip);return h;
    }

    private void addDiscoverEntrances940(){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);
        addDiscoverEntrance940(row,"🎤","Party","Live voice rooms",()->startActivity(new Intent(this,PartyActivity.class)));
        addDiscoverEntrance940(row,"🎵","KTV","Sing & queue",()->startActivity(new Intent(this,PartyActivity.class)));
        addDiscoverEntrance940(row,"🎮","Games","Games and multiplayer rooms",()->KingNav.openRoot(this,1));
        addDiscoverEntrance940(row,"👑","Family","Family center",()->{Intent i=new Intent(this,CommunityHubActivity.class);i.putExtra("tab","Family");startActivity(i);});
        HorizontalScrollView strip=new HorizontalScrollView(this);strip.setHorizontalScrollBarEnabled(false);strip.setFillViewport(false);strip.addView(row);
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(112));rp.setMargins(0,dp(2),0,dp(8));content.addView(strip,rp);
    }
    private void addDiscoverEntrance940(LinearLayout row,String icon,String title,String sub,Runnable action){
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(4),dp(6),dp(4),dp(4));card.setBackground(KingRoyalTheme10021.card(this,16));
        TextView av=tv(icon,25,DARK,false);av.setGravity(Gravity.CENTER);card.addView(av,new LinearLayout.LayoutParams(-1,dp(34)));
        TextView t=tv(title,12,DARK,true);t.setGravity(Gravity.CENTER);card.addView(t,new LinearLayout.LayoutParams(-1,dp(23)));
        TextView s=tv(sub,9,MUTED,false);s.setGravity(Gravity.CENTER);s.setSingleLine(false);s.setMaxLines(2);card.addView(s,new LinearLayout.LayoutParams(-1,dp(30)));
        card.setOnClickListener(v->KingSafe.run(this,"ui-action",action));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(113),dp(103));p.setMargins(dp(3),0,dp(6),0);row.addView(card,p);
    }

    private void addMatchTabs940(){
        LinearLayout tabs=new LinearLayout(this);tabs.setGravity(Gravity.CENTER_VERTICAL);tabs.setPadding(0,dp(2),0,dp(10));
        String[] n={"Hot","New","Nearby","Follow"};for(int i=0;i<n.length;i++){final String label=n[i];boolean selected=label.equals(activePeopleTab);TextView t=tv(label,14,selected?0xff111111:0xff77737d,true);t.setGravity(Gravity.CENTER);t.setBackground(bg(selected?0xffffe91f:0xfff5f4f7,12));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(44),1);p.setMargins(dp(3),0,dp(3),0);tabs.addView(t,p);t.setOnClickListener(v->{activePeopleTab=label;for(int k=0;k<tabs.getChildCount();k++){TextView tab=(TextView)tabs.getChildAt(k);boolean on=tab==t;tab.setBackground(bg(on?0xffffe91f:0xfff5f4f7,12));tab.setTextColor(on?0xff111111:0xff77737d);}loadProfileGrid940();});}
        content.addView(tabs,new LinearLayout.LayoutParams(-1,dp(54)));
    }
    private void loadProfileGrid940(){
        // Give every refresh its own grid identity. A slow earlier Firebase result
        // must never overwrite the newer tab, even when the user goes Hot → New → Hot.
        View old10029=content.findViewWithTag("profile_grid_940");
        if(old10029!=null&&old10029.getParent()==content)content.removeView(old10029);
        LinearLayout grid=new LinearLayout(this);grid.setTag("profile_grid_940");
        grid.setOrientation(LinearLayout.VERTICAL);
        content.addView(grid,new LinearLayout.LayoutParams(-1,-2));
        if(me==null||db==null){TextView state=emptyText("Sign in to discover KING profiles");grid.addView(state,new LinearLayout.LayoutParams(-1,dp(90)));return;}
        if(!KingNetwork.online(this)){TextView offline=emptyText("Offline • tap to retry Discover");offline.setOnClickListener(v->loadProfileGrid940());grid.addView(offline,new LinearLayout.LayoutParams(-1,dp(90)));return;}
        if("Nearby".equals(activePeopleTab)&&nearbyCity10047().isEmpty()){
            TextView setup10048=emptyText("Set your Hometown in Me → Edit Profile to discover people nearby • tap to edit");
            setup10048.setOnClickListener(v->{if(!isFinishing()&&!isDestroyed())
                KingNav.openRoot(this,4);});
            grid.addView(setup10048,new LinearLayout.LayoutParams(-1,dp(94)));
            return;
        }
        TextView loading=emptyText("Loading KING profiles…");grid.addView(loading,new LinearLayout.LayoutParams(-1,dp(72)));
        if("Follow".equals(activePeopleTab)){
            loadFollowedProfiles10022(grid,me.getUid());return;
        }
        Query q=db.collection("public_profiles").limit(60);String city=nearbyCity10047();
        if("Nearby".equals(activePeopleTab)&&!city.isEmpty())
            q=db.collection("public_profiles").whereEqualTo("hometown",city).limit(60);
        else if("New".equals(activePeopleTab))
            q=db.collection("public_profiles").orderBy("createdAt",Query.Direction.DESCENDING).limit(60);
        else if("Hot".equals(activePeopleTab))
            q=db.collection("public_profiles").orderBy("level",Query.Direction.DESCENDING).limit(60);
        final String tab10022=activePeopleTab,owner10022=me.getUid();
        q.get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(snap->{
            if(!discoverCurrent10022(grid,owner10022,tab10022))return;
            List<DocumentSnapshot> docs10022=new ArrayList<>(snap.getDocuments());
            if(!docs10022.isEmpty())publicCursors10028.put(grid,docs10022.get(docs10022.size()-1));
            if("New".equals(tab10022))docs10022.sort((a,b)->{
                com.google.firebase.Timestamp at=a.getTimestamp("createdAt"),bt=b.getTimestamp("createdAt");
                return Long.compare(bt==null?0:bt.toDate().getTime(),at==null?0:at.toDate().getTime());
            });
            else if("Hot".equals(tab10022))docs10022.sort((a,b)->{
                Long av=a.getLong("level"),bv=b.getLong("level");
                return Long.compare(bv==null?0:bv,av==null?0:av);
            });
            // Render in batches, so the first eight no longer hide the rest of the real results.
            showDiscoverProfiles10022(grid,docs10022,owner10022,tab10022,0);
        }).addOnFailureListener(e->{if(!discoverCurrent10022(grid,owner10022,tab10022))return;grid.removeAllViews();TextView retry=emptyText("Discover unavailable • tap to retry");retry.setOnClickListener(v->loadProfileGrid940());grid.addView(retry,new LinearLayout.LayoutParams(-1,dp(90)));});
    }
    // Preserve the Firestore server cursor BEFORE client-side Hot/New sorting.
    // Weak keys avoid retaining discarded grids after navigating to another tab.
    private final java.util.Map<LinearLayout,DocumentSnapshot> publicCursors10028=
        new java.util.WeakHashMap<>();
    private final ArrayList<DocumentSnapshot> followedPeople10023=new ArrayList<>();
    private final Set<String> followedUids10023=new HashSet<>();
    private void loadFollowedProfiles10022(LinearLayout grid10022,String owner10022){
        followedPeople10023.clear();followedUids10023.clear();
        loadFollowedPage10023(grid10022,owner10022,null);
    }
    private void loadFollowedPage10023(LinearLayout grid10022,String owner10022,
            DocumentSnapshot cursor10023){
        if(!discoverCurrent10022(grid10022,owner10022,"Follow"))return;
        Query page10023=db.collection("follows").whereEqualTo("followerUid",owner10022).limit(50);
        if(cursor10023!=null)page10023=page10023.startAfter(cursor10023);
        page10023.get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(follows10023->{
            if(!discoverCurrent10022(grid10022,owner10022,"Follow"))return;
            List<com.google.android.gms.tasks.Task<DocumentSnapshot>> tasks10023=new ArrayList<>();
            for(DocumentSnapshot f10023:follows10023.getDocuments()){
                String target10023=f10023.getString("targetUid");
                if(KingDocumentId10036.valid(target10023)&&!owner10022.equals(target10023)
                    &&followedUids10023.add(target10023))
                    tasks10023.add(db.collection("public_profiles").document(target10023).get(com.google.firebase.firestore.Source.SERVER));
            }
            final DocumentSnapshot next10023=follows10023.size()==50
                ?follows10023.getDocuments().get(follows10023.size()-1):null;
            com.google.android.gms.tasks.Tasks.whenAllComplete(tasks10023).addOnSuccessListener(results10023->{
                if(!discoverCurrent10022(grid10022,owner10022,"Follow"))return;
                int failed10036=0;
                for(com.google.android.gms.tasks.Task<DocumentSnapshot> task10024:tasks10023){
                    if(!task10024.isSuccessful()){failed10036++;continue;}
                    if(task10024.getResult()!=null&&task10024.getResult().exists())
                        followedPeople10023.add(task10024.getResult());
                }
                showDiscoverProfiles10022(grid10022,new ArrayList<>(followedPeople10023),owner10022,"Follow",0);
                if(failed10036>0){
                    TextView retry10036=emptyText(failed10036+" followed profiles could not load • tap to retry");
                    retry10036.setOnClickListener(v->{if(discoverCurrent10022(grid10022,owner10022,"Follow"))loadProfileGrid940();});
                    grid10022.addView(retry10036,new LinearLayout.LayoutParams(-1,dp(64)));
                }
                if(next10023!=null){
                    TextView more10023=tv("↓ Load 50 more followed profiles",14,DARK,true);
                    more10023.setGravity(Gravity.CENTER);more10023.setBackground(KingRoyalTheme10021.card(this,14));
                    more10023.setOnClickListener(v->{
                        if(!discoverCurrent10022(grid10022,owner10022,"Follow")
                            ||more10023.getParent()!=grid10022)return;
                        more10023.setEnabled(false);more10023.setText("Loading followed profiles…");
                        loadFollowedPage10023(grid10022,owner10022,next10023);
                    });
                    grid10022.addView(more10023,new LinearLayout.LayoutParams(-1,dp(52)));
                }
            }).addOnFailureListener(e->{
                if(!discoverCurrent10022(grid10022,owner10022,"Follow"))return;
                TextView retry10023=emptyText("Followed profiles unavailable • tap to retry");
                retry10023.setOnClickListener(v->loadProfileGrid940());
                grid10022.addView(retry10023,new LinearLayout.LayoutParams(-1,dp(70)));
            });
        }).addOnFailureListener(e->{
            if(!discoverCurrent10022(grid10022,owner10022,"Follow"))return;
            TextView retry10023=emptyText("Follow list unavailable • tap to retry");
            retry10023.setOnClickListener(v->loadProfileGrid940());
            grid10022.removeAllViews();grid10022.addView(retry10023,new LinearLayout.LayoutParams(-1,dp(90)));
        });
    }
    private boolean discoverCurrent10022(LinearLayout grid10022,String uid10022,String tab10022){
        FirebaseUser current10022=FirebaseAuth.getInstance().getCurrentUser();
        return !isFinishing()&&!isDestroyed()&&current10022!=null&&uid10022.equals(current10022.getUid())
            &&tab10022.equals(activePeopleTab)&&content!=null&&grid10022.getParent()==content;
    }
    private void showDiscoverProfiles10022(LinearLayout grid10022,List<DocumentSnapshot> docs10022,
            String uid10022,String tab10022,int cursor10022){
        if(!discoverCurrent10022(grid10022,uid10022,tab10022))return;
        if(cursor10022==0)grid10022.removeAllViews();
        int shown10022=0;LinearLayout row10022=null;
        int ix10022=cursor10022;
        for(;ix10022<docs10022.size()&&shown10022<8;ix10022++){
            DocumentSnapshot p10022=docs10022.get(ix10022);
            String id10022=p10022.getId();
            if(!KingDocumentId10036.valid(id10022)||uid10022.equals(id10022))continue;
            if(!KingPublicMeVisibility9984.discoverable(p10022))continue;
            if(shown10022%2==0){row10022=new LinearLayout(this);row10022.setGravity(Gravity.TOP);
                grid10022.addView(row10022,new LinearLayout.LayoutParams(-1,dp(264)));}
            addProfileCard940(row10022,p10022);shown10022++;
        }
        if(cursor10022==0&&shown10022==0){grid10022.addView(emptyText("No matching KING profiles yet • tap to refresh"),new LinearLayout.LayoutParams(-1,dp(90)));return;}
        if(ix10022<docs10022.size()){
            final int next10022=ix10022;
            TextView more10022=tv("↓ See more "+tab10022+" profiles",14,DARK,true);
            more10022.setGravity(Gravity.CENTER);more10022.setBackground(KingRoyalTheme10021.card(this,14));
            more10022.setOnClickListener(v->{if(!discoverCurrent10022(grid10022,uid10022,tab10022))return;
                if(more10022.getParent()!=grid10022)return;grid10022.removeView(more10022);
                showDiscoverProfiles10022(grid10022,docs10022,uid10022,tab10022,next10022);});
            grid10022.addView(more10022,new LinearLayout.LayoutParams(-1,dp(52)));
        }else if(!"Follow".equals(tab10022)){
            nextPublicProfilePage10025(grid10022,docs10022,uid10022,tab10022);
        }
    }
    // A first page of 60 profiles is not the whole Discover directory.
    // Replace it with subsequent server pages without mixing accounts, tabs or cities.
    // v10.0.28: Append profiles from each server cursor, preserving previous profile cards.
    private void showDiscoverProfiles10028(LinearLayout grid10028,List<DocumentSnapshot> docs10028,
            String owner10028,String tab10028,int index10028){
        if(!discoverCurrent10022(grid10028,owner10028,tab10028))return;
        int shown10028=0;LinearLayout row10028=null;int i10028=index10028;
        for(;i10028<docs10028.size()&&shown10028<8;i10028++){
            DocumentSnapshot p10028=docs10028.get(i10028);
            if(!KingDocumentId10036.valid(p10028.getId())||owner10028.equals(p10028.getId()))continue;
            if(!KingPublicMeVisibility9984.discoverable(p10028))continue;
            if(shown10028%2==0){row10028=new LinearLayout(this);row10028.setGravity(Gravity.TOP);
                grid10028.addView(row10028,new LinearLayout.LayoutParams(-1,dp(264)));}
            addProfileCard940(row10028,p10028);shown10028++;
        }
        if(i10028<docs10028.size()){
            final int next10028=i10028;
            TextView more10028=tv("↓ See more "+tab10028+" profiles",14,DARK,true);
            more10028.setGravity(Gravity.CENTER);more10028.setBackground(KingRoyalTheme10021.card(this,14));
            more10028.setOnClickListener(v->{if(!discoverCurrent10022(grid10028,owner10028,tab10028)
                ||more10028.getParent()!=grid10028)return;
                more10028.setEnabled(false);grid10028.removeView(more10028);
                showDiscoverProfiles10028(grid10028,docs10028,owner10028,tab10028,next10028);});
            grid10028.addView(more10028,new LinearLayout.LayoutParams(-1,dp(52)));
        } else nextPublicProfilePage10025(grid10028,docs10028,owner10028,tab10028);
    }
    private void nextPublicProfilePage10025(LinearLayout grid10025,List<DocumentSnapshot> docs10025,
            String owner10025,String tab10025){
        if("Follow".equals(tab10025)||docs10025.size()<60)return;
        final DocumentSnapshot last10025=publicCursors10028.get(grid10025);
        if(last10025==null)return;
        final String city10025=nearbyCity10047();
        TextView next10025=tv("↓ Next 60 "+tab10025+" profiles",14,DARK,true);
        next10025.setGravity(Gravity.CENTER);next10025.setBackground(KingRoyalTheme10021.card(this,14));
        next10025.setOnClickListener(v->{
            if(!discoverCurrent10022(grid10025,owner10025,tab10025)
                ||next10025.getParent()!=grid10025)return;
            next10025.setEnabled(false);next10025.setText("Loading next public profiles…");
            Query page10025=db.collection("public_profiles").limit(60);
            if("Nearby".equals(tab10025)&&!city10025.isEmpty())
                page10025=db.collection("public_profiles").whereEqualTo("hometown",city10025).limit(60);
            else if("New".equals(tab10025))
                page10025=db.collection("public_profiles").orderBy("createdAt",Query.Direction.DESCENDING).limit(60);
            else if("Hot".equals(tab10025))
                page10025=db.collection("public_profiles").orderBy("level",Query.Direction.DESCENDING).limit(60);
            page10025=page10025.startAfter(last10025);
            page10025.get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(snap10025->{
                    if(!discoverCurrent10022(grid10025,owner10025,tab10025))return;
                    if(snap10025.isEmpty()){
                        next10025.setText("✓ No further profiles");return;
                    }
                    List<DocumentSnapshot> nextDocs10025=new ArrayList<>(snap10025.getDocuments());
                    publicCursors10028.put(grid10025,nextDocs10025.get(nextDocs10025.size()-1));
                    sortPublicProfiles10025(nextDocs10025,tab10025);
                    if(next10025.getParent()!=grid10025)return;
                    grid10025.removeView(next10025);
                    // Append this server page; do not erase the profiles the user just browsed.
                    showDiscoverProfiles10028(grid10025,nextDocs10025,owner10025,tab10025,0);
                    TextView back10025=tv("↑ Back to the first profiles",13,DARK,true);
                    back10025.setGravity(Gravity.CENTER);
                    back10025.setOnClickListener(backView10025->{
                        if(discoverCurrent10022(grid10025,owner10025,tab10025))loadProfileGrid940();});
                    grid10025.addView(back10025,0,new LinearLayout.LayoutParams(-1,dp(44)));
                }).addOnFailureListener(error10025->{
                    if(!discoverCurrent10022(grid10025,owner10025,tab10025))return;
                    next10025.setEnabled(true);next10025.setText("Retry loading next 60 profiles");
                });
        });
        grid10025.addView(next10025,new LinearLayout.LayoutParams(-1,dp(52)));
    }
    private void sortPublicProfiles10025(List<DocumentSnapshot> docs10025,String tab10025){
        if("New".equals(tab10025))docs10025.sort((a,b)->{
            com.google.firebase.Timestamp at=a.getTimestamp("createdAt"),bt=b.getTimestamp("createdAt");
            return Long.compare(bt==null?0:bt.toDate().getTime(),at==null?0:at.toDate().getTime());
        });
        else if("Hot".equals(tab10025))docs10025.sort((a,b)->{
            Long av=a.getLong("level"),bv=b.getLong("level");
            return Long.compare(bv==null?0:bv,av==null?0:av);
        });
    }

    private void addProfileCard940(LinearLayout row,DocumentSnapshot p){
        String uid=p.getId(),name=nameOf(p);LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(5),dp(5),dp(5),dp(7));card.setBackground(KingRoyalTheme10021.card(this,16));
        android.widget.FrameLayout hero=new android.widget.FrameLayout(this);TextView fallback=tv(initial(name),40,Color.WHITE,true);fallback.setGravity(Gravity.CENTER);fallback.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xffef9aa9,0xff7f5cf1}));hero.addView(fallback,new android.widget.FrameLayout.LayoutParams(-1,-1));String photo=p.getString("photoUrl");if(photo!=null&&!photo.trim().isEmpty()){android.widget.ImageView im=new android.widget.ImageView(this);im.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);hero.addView(im,new android.widget.FrameLayout.LayoutParams(-1,-1));loadProfileImage940(im,fallback,photo);}card.addView(hero,new LinearLayout.LayoutParams(-1,dp(98)));
        TextView nm=tv(shortName(name),14,DARK,true);nm.setGravity(Gravity.CENTER_VERTICAL);nm.setSingleLine(true);card.addView(nm,new LinearLayout.LayoutParams(-1,dp(30)));TextView id=tv("KING ID "+issued10039(p),10,MUTED,false);card.addView(id,new LinearLayout.LayoutParams(-1,dp(22)));KingVipIdentityStyle.load(this,uid,nm,id);
        TextView chat=tv("● Chat",12,0xff126d50,true);chat.setGravity(Gravity.CENTER);chat.setBackground(bg(0xffd9f7ea,12));chat.setOnClickListener(v->openDirectChat10022(uid,name));card.addView(chat,new LinearLayout.LayoutParams(-1,dp(32)));
        TextView follow=tv("＋ Follow",12,DARK,true);follow.setGravity(Gravity.CENTER);follow.setBackground(bg(YELLOW,10));card.addView(follow,new LinearLayout.LayoutParams(-1,dp(34)));bindFollow(uid,follow);
        card.setOnClickListener(v->openProfile(uid));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(246),1);cp.setMargins(dp(4),dp(4),dp(4),dp(4));row.addView(card,cp);
    }
    private void openDirectChat10022(String uid10022,String name10022){
        FirebaseUser current10022=FirebaseAuth.getInstance().getCurrentUser();
        if(current10022==null||!KingDocumentId10036.valid(uid10022)||uid10022.equals(current10022.getUid())){
            toast("Sign in and choose another real KING user to chat");return;
        }
        Intent chat10022=new Intent(this,ChatActivity.class);
        chat10022.putExtra("peerUid",uid10022);chat10022.putExtra("peerName",name10022);
        startActivity(chat10022);
    }
    private void loadProfileImage940(android.widget.ImageView image,TextView fallback,String url){
        KingProfileImages10068.load(this,image,fallback,url);
    }

    private void addRankingHero(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(14),dp(14),dp(14),dp(14));box.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xfff2d3ff,0xffc084ee,0xffffe0f7}));
        TextView cap=tv("Chat Talents",19,0xff6f3b00,true);cap.setGravity(Gravity.CENTER);box.addView(cap,new LinearLayout.LayoutParams(-1,dp(34)));
        TextView medal=tv("🏆",46,0xffd48b00,true);medal.setGravity(Gravity.CENTER);box.addView(medal,new LinearLayout.LayoutParams(-1,dp(62)));
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER);box.addView(top,new LinearLayout.LayoutParams(-1,dp(92)));
        loadTopThree(top,"chatScore");
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(210));p.setMargins(0,dp(6),0,dp(12));content.addView(box,p);
    }

    private boolean rankingCurrent10031(String account10031,LinearLayout host10031){
        FirebaseUser current10031=FirebaseAuth.getInstance().getCurrentUser();
        return !isFinishing()&&!isDestroyed()&&current10031!=null&&me!=null
            &&account10031.equals(current10031.getUid())
            &&account10031.equals(me.getUid())&&host10031.getParent()!=null;
    }
    private void loadTopThree(LinearLayout host,String field){
        if(me==null||db==null){for(int i=0;i<3;i++)host.addView(rankMini("—",i+1),new LinearLayout.LayoutParams(0,-1,1));return;}
        final String rankOwner10031=me.getUid();
        db.collection("discover_stats").orderBy(field,Query.Direction.DESCENDING).limit(3)
            .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(q->{
                if(!rankingCurrent10031(rankOwner10031,host))return;
                host.removeAllViews();List<DocumentSnapshot> docs=q.getDocuments();
                if(docs.isEmpty()){host.addView(emptyText("No ranking data yet"),new LinearLayout.LayoutParams(-1,-1));return;}
                int n=1;
                for(DocumentSnapshot d:docs){
                    final TextView card=rankMini("KING Player",n++);
                    host.addView(card,new LinearLayout.LayoutParams(0,-1,1));
                    db.collection("public_profiles").document(d.getId())
                        .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(p->{
                            if(rankingCurrent10031(rankOwner10031,host)&&card.getParent()==host&&p.exists())
                                card.setText("👤\n"+shortName(nameOf(p))+"\n"+scoreOf(d,field));
                        });
                }
            }).addOnFailureListener(e->{
                if(!rankingCurrent10031(rankOwner10031,host))return;
                host.removeAllViews();host.addView(emptyText("Ranking unavailable"));
            });
    }

    private TextView rankMini(String name,int rank){TextView t=tv("👤\n"+shortName(name)+"\n#"+rank,12,0xff5c3b00,true);t.setGravity(Gravity.CENTER);return t;}
    private TextView emptyText(String s){TextView t=tv(s,13,MUTED,false);t.setGravity(Gravity.CENTER);return t;}

    private void addSmallRankingRow(){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);TextView a=smallRankCard("🏆 Contribution Ranking");TextView b=smallRankCard("⭐ Charisma Ranking");a.setOnClickListener(v->showRankingPage("Game Master","gameScore"));b.setOnClickListener(v->showRankingPage("Chat Talent","chatScore"));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(76),1);p.setMargins(dp(2),0,dp(5),0);row.addView(a,p);p=new LinearLayout.LayoutParams(0,dp(76),1);p.setMargins(dp(5),0,dp(2),0);row.addView(b,p);content.addView(row,new LinearLayout.LayoutParams(-1,dp(84)));
    }
    private TextView smallRankCard(String s){TextView t=tv(s,13,DARK,true);t.setGravity(Gravity.CENTER_VERTICAL);t.setPadding(dp(14),0,dp(8),0);t.setBackground(KingRoyalTheme10021.card(this,14));return t;}

    private void sectionTitle(String title,String more,Runnable action){LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(0),dp(14),0,dp(8));TextView t=tv(title,19,KingRoyalTheme10021.INK,true);t.setGravity(Gravity.CENTER_VERTICAL);t.setMaxLines(2);r.addView(t,new LinearLayout.LayoutParams(0,dp(46),1));TextView m=tv(more+"  ›",14,0xff333037,true);m.setGravity(Gravity.CENTER);m.setBackground(KingRoyalTheme10021.card(this,12));m.setOnClickListener(v->KingSafe.run(this,"ui-action",action));r.addView(m,new LinearLayout.LayoutParams(dp(112),dp(42)));content.addView(r);}

    private boolean discoverSectionCurrent10033(LinearLayout section10033,String account10033){
        FirebaseUser signed10033=FirebaseAuth.getInstance().getCurrentUser();
        return !isFinishing()&&!isDestroyed()&&me!=null&&signed10033!=null
            &&account10033.equals(me.getUid())&&account10033.equals(signed10033.getUid())
            &&content!=null&&section10033.getParent()==content&&content.isAttachedToWindow();
    }
    private void loadActivityRecommendations(){
        LinearLayout grid=new LinearLayout(this);grid.setOrientation(LinearLayout.VERTICAL);content.addView(grid);
        if(me==null||db==null){grid.addView(emptyText("Sign in to see real live activities"),new LinearLayout.LayoutParams(-1,dp(80)));return;}
        final String owner10033=me.getUid();
        // Fetch open rooms on the server, not the latest four including closed rooms.
        // Avoid a Firestore composite index dependency by sorting the small active set locally.
        db.collection("live_rooms").whereEqualTo("closed",false).limit(24)
            .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(q->{
                if(!discoverSectionCurrent10033(grid,owner10033))return;
                grid.removeAllViews();List<DocumentSnapshot> docs=new ArrayList<>(q.getDocuments());
                docs.sort((a,b)->{com.google.firebase.Timestamp at=a.getTimestamp("createdAt"),bt=b.getTimestamp("createdAt");
                    return Long.compare(bt==null?0:bt.toDate().getTime(),at==null?0:at.toDate().getTime());});
                int c=0;LinearLayout row=null;
                for(DocumentSnapshot d:docs){if(Boolean.TRUE.equals(d.getBoolean("closed")))continue;
                    if(c%2==0){row=new LinearLayout(this);row.setGravity(Gravity.TOP);grid.addView(row,new LinearLayout.LayoutParams(-1,dp(188)));}
                    addActivityCard(row,d);if(++c>=4)break;
                }
                if(c==0)grid.addView(emptyText("No open Party rooms yet"),new LinearLayout.LayoutParams(-1,dp(80)));
            }).addOnFailureListener(e->{
                if(!discoverSectionCurrent10033(grid,owner10033))return;
                grid.removeAllViews();TextView retry10033=emptyText("Live Party rooms unavailable • tap to retry");
                retry10033.setOnClickListener(v->{if(discoverSectionCurrent10033(grid,owner10033))renderDiscover();});
                grid.addView(retry10033,new LinearLayout.LayoutParams(-1,dp(80)));
            });
    }

    private void addActivityCard(LinearLayout row,DocumentSnapshot d){
        String name=safe(d.getString("name"),"Live Party"),cat=safe(d.getString("category"),"Chat"),host=safe(d.getString("ownerName"),"KING Host");
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(10),dp(10),dp(10),dp(8));card.setBackground(bg(0xffeeeeee,13));
        TextView banner=tv(activityEmoji(cat)+"   "+cat,18,Color.WHITE,true);banner.setGravity(Gravity.CENTER);banner.setBackground(bg(activityColor(cat),12));card.addView(banner,new LinearLayout.LayoutParams(-1,dp(92)));
        TextView n=tv(name,14,DARK,true);n.setMaxLines(1);n.setPadding(dp(2),dp(7),0,0);card.addView(n,new LinearLayout.LayoutParams(-1,dp(34)));
        TextView sub=tv(host+"   •   ● Ongoing",11,0xff5c5a62,false);card.addView(sub,new LinearLayout.LayoutParams(-1,dp(30)));card.setOnClickListener(v->openDirectRoom(d));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(176),1);p.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(card,p);
    }
    private int activityColor(String c){int[] x={0xff8a5cf6,0xffef7d52,0xff3ba8e8,0xffe15d8f,0xff4bbd8a};return x[Math.abs(c.hashCode())%x.length];}
    private String activityEmoji(String c){if(c.toLowerCase(Locale.US).contains("music")||c.toLowerCase(Locale.US).contains("sing"))return "🎵";if(c.toLowerCase(Locale.US).contains("game"))return "🎮";if(c.toLowerCase(Locale.US).contains("event"))return "🎉";return "💬";}
    private void openDirectRoom(DocumentSnapshot d){Intent i=new Intent(this,PartyActivity.class);i.putExtra("displayName",displayName);i.putExtra("directRoomId",d.getId());i.putExtra("directRoomName",safe(d.getString("name"),"Live Party"));i.putExtra("directOwnerUid",safe(d.getString("ownerUid"),""));i.putExtra("directOwnerName",safe(d.getString("ownerName"),"KING Host"));i.putExtra("directPrivate",Boolean.TRUE.equals(d.getBoolean("isPrivate")));i.putExtra("directPassword",Boolean.TRUE.equals(d.getBoolean("hasPassword")));startActivity(i);}
    private void openActivityHall(){Intent i=new Intent(this,PartyActivity.class);i.putExtra("displayName",displayName);startActivity(i);}

    private void loadTalentGrid(String field,boolean chatMode){
        LinearLayout grid=new LinearLayout(this);grid.setOrientation(LinearLayout.VERTICAL);content.addView(grid);
        if(me==null||db==null){grid.addView(emptyText("Sign in to see real talent rankings"),new LinearLayout.LayoutParams(-1,dp(90)));return;}
        final String owner10033=me.getUid();
        db.collection("discover_stats").orderBy(field,Query.Direction.DESCENDING).limit(4)
            .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(q->{
                if(!discoverSectionCurrent10033(grid,owner10033))return;
                grid.removeAllViews();List<DocumentSnapshot> docs=q.getDocuments();
                if(docs.isEmpty()){grid.addView(emptyText("No talent data yet"),new LinearLayout.LayoutParams(-1,dp(90)));return;}
                LinearLayout row=null;int c=0;
                for(DocumentSnapshot d:docs){if(c%2==0){row=new LinearLayout(this);grid.addView(row,new LinearLayout.LayoutParams(-1,dp(205)));}addTalentCard(row,d,field,chatMode);c++;}
            }).addOnFailureListener(e->{
                if(!discoverSectionCurrent10033(grid,owner10033))return;
                grid.removeAllViews();TextView retry10033=emptyText("Talent ranking unavailable • tap to retry");
                retry10033.setOnClickListener(v->{if(discoverSectionCurrent10033(grid,owner10033))renderDiscover();});
                grid.addView(retry10033,new LinearLayout.LayoutParams(-1,dp(90)));
            });
    }
    private void addTalentCard(LinearLayout row,DocumentSnapshot stat,String field,boolean chatMode){
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER_HORIZONTAL);card.setPadding(dp(8),dp(8),dp(8),dp(8));card.setBackground(bg(Color.WHITE,14));
        TextView av=tv("👤",34,0xff524866,true);av.setGravity(Gravity.CENTER);av.setBackground(bg(0xfff1edf7,38));card.addView(av,new LinearLayout.LayoutParams(dp(76),dp(76)));
        TextView name=tv("KING Talent",14,DARK,true);name.setGravity(Gravity.CENTER);name.setMaxLines(1);card.addView(name,new LinearLayout.LayoutParams(-1,dp(32)));
        TextView score=tv(scoreOf(stat,field)+" pts",12,MUTED,true);score.setGravity(Gravity.CENTER);card.addView(score,new LinearLayout.LayoutParams(-1,dp(28)));
        TextView action=tv(chatMode?"💬 Chat room  ›":"🎮 Game profile  ›",12,DARK,true);action.setGravity(Gravity.CENTER_VERTICAL);action.setPadding(dp(10),0,0,0);action.setBackground(bg(0xfff4f3f7,10));card.addView(action,new LinearLayout.LayoutParams(-1,dp(42)));
        final String owner10033=me==null?"":me.getUid();
        db.collection("public_profiles").document(stat.getId()).get().addOnSuccessListener(p->{
            FirebaseUser now10033=FirebaseAuth.getInstance().getCurrentUser();
            if(p.exists()&&now10033!=null&&owner10033.equals(now10033.getUid())
                &&card.getParent()!=null&&!isFinishing()&&!isDestroyed())name.setText(shortName(nameOf(p)));
        });card.setOnClickListener(v->openProfile(stat.getId()));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(194),1);p.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(card,p);
    }

    private void addPeopleTabs(){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,dp(16),0,dp(8));TextView rec=tab("Recommended",true);TextView near=tab("Nearby",false);row.addView(rec,new LinearLayout.LayoutParams(dp(150),dp(52)));row.addView(near,new LinearLayout.LayoutParams(dp(100),dp(52)));content.addView(row);rec.setOnClickListener(v->{activePeopleTab="Recommended";loadRecommended(false);});near.setOnClickListener(v->{activePeopleTab="Nearby";loadRecommended(true);});
    }
    private TextView tab(String s,boolean active){TextView t=tv(s,19,active?DARK:0xff4f4c54,active);t.setGravity(Gravity.CENTER_VERTICAL);return t;}

    private boolean nearbyResultsCurrent10032(LinearLayout list10032,String owner10032,boolean nearby10032){
        FirebaseUser signed10032=FirebaseAuth.getInstance().getCurrentUser();
        return !isFinishing()&&!isDestroyed()&&signed10032!=null&&me!=null
            &&owner10032.equals(signed10032.getUid())&&owner10032.equals(me.getUid())
            &&(nearby10032?"Nearby":"Recommended").equals(activePeopleTab)
            &&content!=null&&list10032.getParent()==content;
    }
    private void loadRecommended(boolean nearby){
        LinearLayout list=findOrCreatePeopleList();list.removeAllViews();
        if(me==null||db==null){list.addView(emptyText("Sign in with Google / Firebase to discover real people"),new LinearLayout.LayoutParams(-1,dp(90)));return;}
        final String owner10032=me.getUid();
        String city=nearbyCity10047();
        Query q=db.collection("public_profiles").limit(30);
        if(nearby&&!city.isEmpty())q=db.collection("public_profiles").whereEqualTo("hometown",city).limit(30);
        q.get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(s->{
            if(!nearbyResultsCurrent10032(list,owner10032,nearby))return;
            list.removeAllViews();int shown=0;
            for(DocumentSnapshot d:s.getDocuments()){
                String uid=safe(d.getString("uid"),d.getId());
                if(uid.equals(owner10032))continue;
                addPersonRow(list,d);shown++;if(shown>=12)break;
            }
            if(shown==0){String msg=nearby?(city.isEmpty()?"Set your hometown in Edit Profile to use Nearby":"No nearby profiles yet"):"No recommended profiles yet";
                list.addView(emptyText(msg),new LinearLayout.LayoutParams(-1,dp(90)));}
        }).addOnFailureListener(error->{
            if(!nearbyResultsCurrent10032(list,owner10032,nearby))return;
            list.removeAllViews();TextView retry10032=emptyText("People directory unavailable • tap to retry");
            retry10032.setOnClickListener(v->{if(nearbyResultsCurrent10032(list,owner10032,nearby))loadRecommended(nearby);});
            list.addView(retry10032,new LinearLayout.LayoutParams(-1,dp(90)));
        });
    }
    private LinearLayout findOrCreatePeopleList(){
        Object tag=content.findViewWithTag("people_list");if(tag instanceof LinearLayout)return (LinearLayout)tag;LinearLayout list=new LinearLayout(this);list.setTag("people_list");list.setOrientation(LinearLayout.VERTICAL);content.addView(list);return list;
    }
    private void addPersonRow(LinearLayout host,DocumentSnapshot p){
        String uid=p.getId(),name=nameOf(p),tags=safe(p.getString("tags"),"KING Plus member");
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(4),dp(7),dp(0),dp(7));
        TextView av=tv(initial(name),22,Color.WHITE,true);av.setGravity(Gravity.CENTER);av.setBackground(bg(0xff7f5cf1,34));row.addView(av,new LinearLayout.LayoutParams(dp(68),dp(68)));
        LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);info.setPadding(dp(12),0,dp(6),0);TextView n=tv(name,15,DARK,true);n.setMaxLines(1);info.addView(n,new LinearLayout.LayoutParams(-1,dp(28)));info.addView(tv("KING ID "+issued10039(p)+"  |  "+tags,11,MUTED,false),new LinearLayout.LayoutParams(-1,dp(23)));info.addView(tv("KING Plus",11,0xffa19ca8,false),new LinearLayout.LayoutParams(-1,dp(22)));row.addView(info,new LinearLayout.LayoutParams(0,dp(76),1));
        TextView follow=tv("Follow",14,0xff111111,true);follow.setGravity(Gravity.CENTER);follow.setBackground(bg(YELLOW,10));row.addView(follow,new LinearLayout.LayoutParams(dp(104),dp(48)));bindFollow(uid,follow);row.setOnClickListener(v->openProfile(uid));
        host.addView(row,new LinearLayout.LayoutParams(-1,dp(92)));View line=new View(this);line.setBackgroundColor(0xffe8e7eb);host.addView(line,new LinearLayout.LayoutParams(-1,1));
    }
    private boolean followAccountCurrent10028(String account){FirebaseUser now=FirebaseAuth.getInstance().getCurrentUser();
        return !isFinishing()&&!isDestroyed()&&now!=null&&account.equals(now.getUid())&&me!=null&&account.equals(me.getUid());}
    private void bindFollow(String uid,TextView b){
        if(me==null||db==null||!KingDocumentId10036.valid(uid)||uid.equals(me.getUid())){
            b.setEnabled(false);b.setText("Unavailable");return;
        }
        List<TextView> views=followButtons10061.get(uid);
        if(views==null){views=new ArrayList<>();followButtons10061.put(uid,views);}
        if(!views.contains(b))views.add(b);
        b.setOnClickListener(v->toggleFollow10061(uid));
        updateFollowButton10061(uid,b);
    }
    private void updateFollowButton10061(String uid,TextView button){
        if(me==null||!followAccountCurrent10028(me.getUid())){
            button.setEnabled(false);button.setText("Sign in");return;
        }
        if(pendingFollow10061.contains(uid)){
            button.setEnabled(false);button.setText("Saving…");return;
        }
        button.setEnabled(followLoaded10061);
        button.setText(!followLoaded10061?"Checking…":liveFollowing10061.contains(uid)?"✓ Following":"＋ Follow");
    }
    private void refreshFollowButtons10061(){
        for(Map.Entry<String,List<TextView>> entry:followButtons10061.entrySet())
            for(TextView button:entry.getValue())updateFollowButton10061(entry.getKey(),button);
    }
    private void stopFollowListener10061(){
        ++followListenerGeneration10061;
        if(followListener10061!=null){followListener10061.remove();followListener10061=null;}
        followLoaded10061=false;followOwner10061="";liveFollowing10061.clear();
    }
    private void startFollowListener10061(){
        stopFollowListener10061();
        FirebaseUser signed=FirebaseAuth.getInstance().getCurrentUser();
        if(signed==null||db==null){refreshFollowButtons10061();return;}
        final String owner=signed.getUid();followOwner10061=owner;
        final int generation=followListenerGeneration10061;
        followListener10061=db.collection("follows").whereEqualTo("followerUid",owner)
            .addSnapshotListener((snap,error)->{
                if(generation!=followListenerGeneration10061||!followAccountCurrent10028(owner))return;
                if(error!=null||snap==null){followLoaded10061=false;refreshFollowButtons10061();return;}
                liveFollowing10061.clear();
                for(DocumentSnapshot doc:snap.getDocuments()){
                    String target=doc.getString("targetUid");
                    if(KingDocumentId10036.valid(target)&&owner.equals(doc.getString("followerUid"))
                        &&doc.getId().equals(followId(owner,target)))liveFollowing10061.add(target);
                }
                followLoaded10061=true;refreshFollowButtons10061();
            });
        refreshFollowButtons10061();
    }
    private void toggleFollow10061(String uid){
        FirebaseUser signed=FirebaseAuth.getInstance().getCurrentUser();
        if(signed==null||me==null||db==null||!signed.getUid().equals(me.getUid())
            ||!KingDocumentId10036.valid(uid)||uid.equals(signed.getUid())
            ||!pendingFollow10061.add(uid))return;
        final String owner=signed.getUid(),id=followId(owner,uid);
        refreshFollowButtons10061();
        com.google.firebase.firestore.DocumentReference ref=db.collection("follows").document(id);
        db.runTransaction(transaction->{
            DocumentSnapshot existing=transaction.get(ref);
            if(existing.exists()){
                if(!owner.equals(existing.getString("followerUid"))||!uid.equals(existing.getString("targetUid")))
                    throw new IllegalStateException("Follow ID/account mismatch");
                transaction.delete(ref);return false;
            }
            Map<String,Object> fields=new HashMap<>();fields.put("followerUid",owner);
            String followerName=displayName==null||displayName.trim().isEmpty()
                ?"KING User":displayName.trim();
            if(followerName.length()>60)followerName=followerName.substring(0,60);
            fields.put("targetUid",uid);fields.put("followerName",followerName);
            fields.put("createdAt",FieldValue.serverTimestamp());
            transaction.set(ref,fields);return true;
        }).addOnSuccessListener(isFollowing->{
            if(!followAccountCurrent10028(owner))return;
            pendingFollow10061.remove(uid);
            if(Boolean.TRUE.equals(isFollowing))liveFollowing10061.add(uid);
            else liveFollowing10061.remove(uid);
            followLoaded10061=true;refreshFollowButtons10061();
        }).addOnFailureListener(error->{
            pendingFollow10061.remove(uid);
            if(followAccountCurrent10028(owner)){
                refreshFollowButtons10061();toast("Follow update failed • retry online");
            }
        });
    }

    private void openProfile(String uid){
        if(!KingDocumentId10036.valid(uid))return;
        Intent i=new Intent(this,KingPublicProfileActivity.class);
        i.putExtra("uid",uid);
        i.putExtra("name","KING Player");
        startActivity(i);
    }

    private void shareUser(String uid,String name){
        if(me==null||db==null||!KingDocumentId10036.valid(uid)){toast("Sign in first");return;}
        final String account10039=me.getUid();
        db.collection("public_profiles").document(uid).get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(p->{
            if(!followAccountCurrent10028(account10039))return;
            String id=p.exists()?KingPublicId9996.verified(p.getString("publicId")):null;
            if(id==null){toast("This KING ID is not issued yet");return;}
            Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT,"Find "+safe(p.getString("displayName"),name)+" on KING Plus • KING ID: "+id);
            startActivity(Intent.createChooser(i,"Share verified KING ID"));
          }).addOnFailureListener(e->{if(followAccountCurrent10028(account10039))toast("KING ID unavailable • retry online");});
    }

    private void showRankingPage(String title,String field){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(0xfff7f7fa);LinearLayout h=new LinearLayout(this);h.setGravity(Gravity.CENTER_VERTICAL);h.setPadding(dp(12),0,dp(12),0);TextView back=tv("‹",40,DARK,false);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->renderDiscover());h.addView(back,new LinearLayout.LayoutParams(dp(52),dp(64)));h.addView(tv(title,27,DARK,true),new LinearLayout.LayoutParams(0,dp(64),1));root.addView(h);ScrollView sv=new ScrollView(this);LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);list.setPadding(dp(14),dp(8),dp(14),dp(24));sv.addView(list);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));setSafe(root);
        if(me==null||db==null){list.addView(emptyText("Sign in to view ranking"));return;}
        final String rankOwner10035=me.getUid();
        list.addView(emptyText("Loading verified KING rankings…"));
        db.collection("discover_stats").orderBy(field,Query.Direction.DESCENDING).limit(50)
            .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(q->{
                if(!rankingCurrent10035(list,rankOwner10035))return;
                list.removeAllViews();int rank=1;
                for(DocumentSnapshot d:q.getDocuments())addRankingRow(list,d,field,rank++);
                if(q.isEmpty())list.addView(emptyText("No ranking data yet"));
            }).addOnFailureListener(e->{
                if(!rankingCurrent10035(list,rankOwner10035))return;
                list.removeAllViews();TextView retry10035=emptyText("Rankings unavailable • tap to retry");
                retry10035.setOnClickListener(v->{if(rankingCurrent10035(list,rankOwner10035))showRankingPage(title,field);});
                list.addView(retry10035);
            });
    }
    private boolean rankingCurrent10035(LinearLayout rows10035,String uid10035){
        FirebaseUser current10035=FirebaseAuth.getInstance().getCurrentUser();
        return !isFinishing()&&!isDestroyed()&&current10035!=null
            &&uid10035.equals(current10035.getUid())&&me!=null&&uid10035.equals(me.getUid())
            &&rows10035.isAttachedToWindow();
    }
    private void addRankingRow(LinearLayout host,DocumentSnapshot s,String field,int rank){LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(8),dp(8),dp(8),dp(8));TextView num=tv(String.valueOf(rank),17,rank<=3?0xffd98a00:MUTED,true);num.setGravity(Gravity.CENTER);r.addView(num,new LinearLayout.LayoutParams(dp(44),dp(64)));TextView av=tv("👤",26,Color.WHITE,true);av.setGravity(Gravity.CENTER);av.setBackground(bg(0xff7861d8,30));r.addView(av,new LinearLayout.LayoutParams(dp(60),dp(60)));LinearLayout inf=new LinearLayout(this);inf.setOrientation(LinearLayout.VERTICAL);inf.setPadding(dp(10),0,0,0);TextView n=tv("KING Player",15,DARK,true);inf.addView(n);inf.addView(tv(scoreOf(s,field)+" EXP",12,MUTED,false));r.addView(inf,new LinearLayout.LayoutParams(0,dp(64),1));final String rankOwner10035=me==null?"":me.getUid();
        r.setEnabled(false);
        db.collection("public_profiles").document(s.getId())
            .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(p->{
            if(!rankingCurrent10035(host,rankOwner10035)||r.getParent()!=host)return;
            if(!p.exists()||!KingDocumentId10036.valid(p.getId())){
                n.setText("Profile unavailable");return;
            }
            n.setText(nameOf(p));r.setEnabled(true);
            final String verifiedUid10044=p.getId();
            r.setOnClickListener(v->{if(r.isEnabled()&&rankingCurrent10035(host,rankOwner10035))openProfile(verifiedUid10044);});
        }).addOnFailureListener(e->{
            if(rankingCurrent10035(host,rankOwner10035)&&r.getParent()==host)
                n.setText("Profile unavailable • retry rankings");
        });host.addView(r,new LinearLayout.LayoutParams(-1,dp(82)));}

    private void showInfluence(String type,int score){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(0xff17122e);LinearLayout h=new LinearLayout(this);h.setGravity(Gravity.CENTER_VERTICAL);h.setPadding(dp(12),0,dp(12),0);TextView back=tv("‹",40,Color.WHITE,false);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->renderDiscover());h.addView(back,new LinearLayout.LayoutParams(dp(52),dp(64)));TextView title=tv("My Influence",27,Color.WHITE,true);h.addView(title,new LinearLayout.LayoutParams(0,dp(64),1));TextView rec=tv("▤",25,Color.WHITE,true);rec.setGravity(Gravity.CENTER);rec.setOnClickListener(v->showDataRecord());h.addView(rec,new LinearLayout.LayoutParams(dp(52),dp(64)));root.addView(h);
        ScrollView sv=new ScrollView(this);LinearLayout b=new LinearLayout(this);b.setOrientation(LinearLayout.VERTICAL);b.setPadding(dp(18),dp(18),dp(18),dp(30));sv.addView(b);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));TextView glow=tv("✦",92,Color.WHITE,true);glow.setGravity(Gravity.CENTER);b.addView(glow,new LinearLayout.LayoutParams(-1,dp(180)));TextView t=tv(type,22,Color.WHITE,true);t.setGravity(Gravity.CENTER);b.addView(t);TextView sc=tv(score+" EXP",16,0xffcdbbff,true);sc.setGravity(Gravity.CENTER);b.addView(sc,new LinearLayout.LayoutParams(-1,dp(50)));b.addView(influenceProgress(score),new LinearLayout.LayoutParams(-1,dp(58)));addMedalPanel(b,score);addHonorPanel(b);setSafe(root);
    }
    private View influenceProgress(int score){LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);TextView a=tv(String.valueOf(score),13,Color.WHITE,true);r.addView(a,new LinearLayout.LayoutParams(dp(58),dp(44)));View line=new View(this);line.setBackgroundColor(0xff6f51d7);r.addView(line,new LinearLayout.LayoutParams(0,dp(8),1));TextView z=tv(nextThreshold(score)+"",13,Color.WHITE,true);z.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);r.addView(z,new LinearLayout.LayoutParams(dp(70),dp(44)));return r;}
    private void addMedalPanel(LinearLayout b,int score){LinearLayout p=new LinearLayout(this);p.setOrientation(LinearLayout.VERTICAL);p.setPadding(dp(14),dp(12),dp(14),dp(14));p.setBackground(bg(0xff2b244b,16));TextView h=tv("✦  Rank Medal  ✦",20,Color.WHITE,true);h.setGravity(Gravity.CENTER);p.addView(h,new LinearLayout.LayoutParams(-1,dp(50)));LinearLayout row=new LinearLayout(this);int[] th={0,25,1500,3000,6500};for(int i=0;i<th.length;i++){TextView m=tv((score>=th[i]?"🏅":"⬡")+"\n"+th[i],12,Color.WHITE,true);m.setGravity(Gravity.CENTER);row.addView(m,new LinearLayout.LayoutParams(0,dp(86),1));}p.addView(row);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(155));lp.setMargins(0,dp(14),0,dp(12));b.addView(p,lp);}
    private void addHonorPanel(LinearLayout b){LinearLayout p=new LinearLayout(this);p.setOrientation(LinearLayout.VERTICAL);p.setPadding(dp(14),dp(12),dp(14),dp(14));p.setBackground(bg(0xff2b244b,16));TextView h=tv("✦  Honor Display  ✦",20,Color.WHITE,true);h.setGravity(Gravity.CENTER);p.addView(h,new LinearLayout.LayoutParams(-1,dp(50)));TextView x=tv("Talent Honor Ranking      Talent in Spotlight\n\nGame Master • Chat Talent • Event Star",14,0xffddd4ff,false);x.setGravity(Gravity.CENTER);p.addView(x,new LinearLayout.LayoutParams(-1,dp(90)));b.addView(p,new LinearLayout.LayoutParams(-1,dp(150)));}
    private int nextThreshold(int s){int[]x={25,1500,3000,6500};for(int v:x)if(s<v)return v;return 6500;}

    private void showDataRecord(){showDataRecord10029("Chat Talent");}
    private void showDataRecord10029(String selected10029){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xfff7f8fb);
        LinearLayout h=new LinearLayout(this);h.setGravity(Gravity.CENTER_VERTICAL);h.setPadding(dp(12),0,dp(12),0);
        TextView back=tv("‹",40,DARK,false);back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v->showInfluence(selected10029,
            "Game Master".equals(selected10029)?gameScore:
                "Event Star".equals(selected10029)?eventScore:chatScore));
        h.addView(back,new LinearLayout.LayoutParams(dp(52),dp(64)));
        h.addView(tv("Data Record",27,DARK,false),new LinearLayout.LayoutParams(0,dp(64),1));root.addView(h);
        LinearLayout tabs=new LinearLayout(this);tabs.setGravity(Gravity.CENTER);
        String[] names10029={"Game Master","Chat Talent","Event Star"};
        for(String name10029:names10029){
            boolean active10029=selected10029.equals(name10029);
            TextView t=tv(name10029,12,active10029?DARK:MUTED,active10029);
            t.setGravity(Gravity.CENTER);t.setBackground(bg(active10029?0xffffe69c:0xfff1edf7,8));
            t.setOnClickListener(v->showDataRecord10029(name10029));
            LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,dp(58),1);
            tp.setMargins(dp(3),dp(2),dp(3),dp(2));tabs.addView(t,tp);
        }
        root.addView(tabs);
        ScrollView sv=new ScrollView(this);LinearLayout list=new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);list.setPadding(dp(18),dp(12),dp(18),dp(20));
        sv.addView(list);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        list.addView(tv("Current available activity totals • not a 30-day history",13,MUTED,false),
            new LinearLayout.LayoutParams(-1,dp(56)));
        if("Game Master".equals(selected10029))recordRow(list,"Game activity EXP",gameScore);
        else if("Event Star".equals(selected10029))recordRow(list,"Room & event activity EXP",eventScore);
        else recordRow(list,"Live chat / speaking EXP",chatScore);
        list.addView(tv("Historical daily changes require server-side dated activity records.",12,MUTED,false),
            new LinearLayout.LayoutParams(-1,dp(64)));
        setSafe(root);
    }
    private void recordRow(LinearLayout host,String title,int value){LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(2),dp(10),dp(2),dp(10));TextView t=tv(title,16,DARK,true);r.addView(t,new LinearLayout.LayoutParams(0,dp(58),1));TextView v=tv((value>=0?"+":"")+value,16,DARK,true);v.setGravity(Gravity.CENTER);r.addView(v,new LinearLayout.LayoutParams(dp(80),dp(58)));host.addView(r,new LinearLayout.LayoutParams(-1,dp(78)));}
    private String nearbyCity10047(){
        FirebaseUser active=FirebaseAuth.getInstance().getCurrentUser();
        if(active==null||me==null||!active.getUid().equals(me.getUid()))return "";
        android.content.SharedPreferences profile=getSharedPreferences("MainActivity",MODE_PRIVATE);
        if(!active.getUid().equals(profile.getString("firebase_uid","")))return "";
        String city=profile.getString("hometown","");
        return city==null?"":city.trim();
    }

    private int scoreDelta(){
        FirebaseUser signed=FirebaseAuth.getInstance().getCurrentUser();
        if(signed==null||me==null||!signed.getUid().equals(me.getUid()))return 0;
        String key="last_total_"+signed.getUid();
        int now=gameScore+chatScore+eventScore;
        android.content.SharedPreferences prefs=getSharedPreferences("discover_public",MODE_PRIVATE);
        int previous=prefs.getInt(key,now);prefs.edit().putInt(key,now).apply();
        return now-previous;
    }

    private int scoreOf(DocumentSnapshot d,String field){Long x=d.getLong(field);return x==null?0:(int)Math.min(Integer.MAX_VALUE,Math.max(0,x));}
    private String nameOf(DocumentSnapshot d){return safe(d.getString("displayName"),"KING "+publicId(d.getId()));}
    private String initial(String s){String x=safe(s,"K");return x.substring(0,1).toUpperCase(Locale.US);}
    private String shortName(String s){String x=safe(s,"KING User");return x.length()>12?x.substring(0,11)+"…":x;}
    private String publicId(String uid){return "Not issued";}
    private String issued10039(DocumentSnapshot d){String x=KingPublicId9996.verified(d.getString("publicId"));return x==null?"Not issued":x;}
    private String followId(String a,String b){return a+"__"+b;}

    private View bottomNav(){LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER);nav.setBackground(KingRoyalTheme10021.bottomNav(this));addNav(nav,"⌂\nParty",0);addNav(nav,"▣\nGame",1);addNav(nav,"◉\nDiscover",2);addNav(nav,"▢\nMessages",3);addNav(nav,"♙\nMe",4);return nav;}
    private void addNav(LinearLayout nav,String label,int tab){TextView t=tv(label,11,tab==2?0xff111111:0xff8c8991,tab==2);t.setGravity(Gravity.CENTER);if(tab==2)t.setBackground(bg(0xffffec3a,28));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(64),1);p.setMargins(dp(5),dp(3),dp(5),dp(3));nav.addView(t,p);t.setOnClickListener(v->{if(tab!=2)KingNav.openRoot(this,tab);});}
    @Override protected void onResume(){
        super.onResume();
        FirebaseUser current10024=FirebaseAuth.getInstance().getCurrentUser();
        String now10024=current10024==null?null:current10024.getUid();
        String was10024=me==null?null:me.getUid();
        if(!java.util.Objects.equals(now10024,was10024)){
            me=current10024;pendingFollow10061.clear();followedPeople10023.clear();followedUids10023.clear();
            startFollowListener10061();
            // Scores carried by the original Intent belong to the old account.
            statsExplicit10024=false;gameScore=0;chatScore=0;eventScore=0;
            renderDiscover();
            loadDiscoverStats10030(now10024);
        }
    }
    @Override protected void onStart(){super.onStart();startFollowListener10061();}
    @Override protected void onStop(){stopFollowListener10061();super.onStop();}
    @Override public void onBackPressed(){if(isTaskRoot())KingNav.confirmExit(this);else finish();}
    @Override protected void onDestroy(){stopFollowListener10061();KingNetwork.unwatch(this,discoverNetworkCallback940);discoverNetworkCallback940=null;super.onDestroy();}
}
