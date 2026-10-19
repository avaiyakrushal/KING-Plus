package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class KingPublicProfileActivity extends Activity {
    private static final int BG=0xfff7f5fa, INK=0xff211b29, MUTED=0xff827a8c, PURPLE=0xff6d48d7, GOLD=0xffffdf19;
    private FirebaseFirestore db; private FirebaseUser me; private String uid="", name="KING User"; private LinearLayout body; private TextView followBtn; private String activeRoomId997="";
    private TextView publicFollowers940,publicFollowing940,publicFriends940;
    private com.google.firebase.firestore.ListenerRegistration incoming965,outgoing965,followDoc965;
    private final java.util.Set<String> publicFollowerSet965=new java.util.HashSet<>();
    private final java.util.Set<String> publicFollowingSet965=new java.util.HashSet<>();
    private boolean publicIncomingLoaded965,publicOutgoingLoaded965;
    private com.google.firebase.firestore.ListenerRegistration profileLive10068;
    private int profileEpoch10068;
    private boolean profileVisible10068;
    private java.util.Map<String,Object> profileData10068;
    private TextView profileState10068;
    private ScrollView profileScroll10068;
    private boolean profileCurrent10068(int epoch){
        FirebaseUser now=FirebaseAuth.getInstance().getCurrentUser();
        return profileVisible10068&&epoch==profileEpoch10068&&!isFinishing()&&!isDestroyed()
            &&me!=null&&now!=null&&me.getUid().equals(now.getUid());
    }

    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}    
    private GradientDrawable bg(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    private TextView tv(String s,int z,int c,boolean b){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);if(b)t.setTypeface(null,Typeface.BOLD);t.setPadding(dp(12),dp(5),dp(12),dp(5));t.setGravity(Gravity.CENTER_VERTICAL);return t;}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    @Override public void onCreate(Bundle b){super.onCreate(b);uid=getIntent().getStringExtra("uid");name=getIntent().getStringExtra("name");if(uid==null)uid="";if(name==null||name.trim().isEmpty())name="KING User";try{db=FirebaseFirestore.getInstance();me=FirebaseAuth.getInstance().getCurrentUser();}catch(Exception ignored){}activeRoomId997=getIntent().getStringExtra("roomId");if(activeRoomId997==null)activeRoomId997="";render();logProfileVisit940();}
    private void render(){LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);TextView back=tv("‹",38,INK,false);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->finish());head.addView(back,new LinearLayout.LayoutParams(dp(50),dp(56)));TextView title=tv("Profile",20,INK,true);head.addView(title,new LinearLayout.LayoutParams(0,dp(56),1));root.addView(head);profileState10068=tv("Loading online profile…",12,MUTED,false);root.addView(profileState10068);ScrollView sv=new ScrollView(this);profileScroll10068=sv;body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(14),dp(8),dp(14),dp(30));sv.addView(body);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);}
    private void logProfileVisit940(){
        if(db==null||me==null||!KingDocumentId10036.valid(uid)||uid.equals(me.getUid()))return;
        Map<String,Object> visit=new HashMap<>();visit.put("uid",me.getUid());visit.put("name",displayName());visit.put("visitedAt",FieldValue.serverTimestamp());
        db.collection("public_profiles").document(uid).collection("visitors").document(me.getUid()).set(visit,com.google.firebase.firestore.SetOptions.merge()).addOnFailureListener(e->{});
    }
    private void load(){
        if(profileLive10068!=null)profileLive10068.remove();
        final int epoch=++profileEpoch10068;
        if(db==null||me==null||!KingDocumentId10036.valid(uid)){
            body.removeAllViews();body.addView(tv("Sign in to open a valid KING profile",15,MUTED,false));return;
        }
        profileLive10068=db.collection("public_profiles").document(uid)
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(doc,error)->{
                if(!profileCurrent10068(epoch))return;
                if(error!=null||doc==null){profileState10068.setText("Profile unavailable online • tap to retry");
                    profileState10068.setOnClickListener(v->load());return;}
                profileState10068.setText(doc.getMetadata().isFromCache()?"Cached profile • waiting for server":"Live profile");
                if(!doc.exists()){
                    if(doc.getMetadata().isFromCache())return;
                    removePublicFollowListeners965();profileData10068=null;body.removeAllViews();
                    body.addView(tv("This profile is no longer available",16,MUTED,true));return;
                }
                if(doc.getMetadata().hasPendingWrites())return;
                if(profileData10068!=null&&profileData10068.equals(doc.getData()))return;
                int position=profileScroll10068.getScrollY();profileData10068=new java.util.HashMap<>(doc.getData());
                removePublicFollowListeners965();showProfile(doc);
                profileScroll10068.post(()->{if(profileCurrent10068(epoch))profileScroll10068.scrollTo(0,position);});
            });
    }

    private void showProfile(DocumentSnapshot p){
        body.removeAllViews();String n=p.exists()?safe(p.getString("displayName"),name):name;name=n;Long lv=p.getLong("level"),vip=0L,xp=p.getLong("xp"),vipPoints=0L;String frame=safe(p.getString("equippedFrame"),"Minimal Frame"),effect=safe(p.getString("entranceEffect"),"Welcome Sparkle"),badge=safe(p.getString("nameBadge"),"None"),bubble=safe(p.getString("chatBubble"),"Default Bubble"),bio=safe(p.getString("bio"),"KING Plus member"),tags=safe(p.getString("tags"),"");String photo=safe(p.getString("photoUrl"),"");
        LinearLayout profileHead=new LinearLayout(this);profileHead.setGravity(Gravity.CENTER_VERTICAL);profileHead.setPadding(dp(12),dp(10),dp(12),dp(10));profileHead.setBackground(bg(Color.WHITE,18));
        android.widget.FrameLayout avatar=new android.widget.FrameLayout(this);TextView fallback=tv(n.isEmpty()?"K":n.substring(0,1).toUpperCase(),28,Color.WHITE,true);fallback.setGravity(Gravity.CENTER);fallback.setBackground(bg(0xff8a63db,40));avatar.addView(fallback,new android.widget.FrameLayout.LayoutParams(-1,-1));android.widget.ImageView image=new android.widget.ImageView(this);image.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);avatar.addView(image,new android.widget.FrameLayout.LayoutParams(-1,-1));if(!photo.isEmpty())loadProfilePhoto940(image,fallback,photo);profileHead.addView(avatar,new LinearLayout.LayoutParams(dp(86),dp(86)));
        LinearLayout idBlock=new LinearLayout(this);idBlock.setOrientation(LinearLayout.VERTICAL);idBlock.setPadding(dp(12),0,0,0);TextView nm=tv(n,20,INK,true);idBlock.addView(nm,new LinearLayout.LayoutParams(-1,dp(32)));String issued9998=KingPublicId9996.verified(p.getString("publicId"));
        TextView pid=tv("◇ KING ID  "+(issued9998==null?"Not issued":issued9998)+" ◇",12,MUTED,true);pid.setBackground(bg(0xfff2eff6,14));idBlock.addView(pid,new LinearLayout.LayoutParams(-1,dp(32)));TextView lvLine=tv("VIP not verified  ·  Activity Lv."+(lv==null?1:lv),12,PURPLE,true);idBlock.addView(lvLine,new LinearLayout.LayoutParams(-1,dp(28)));profileHead.addView(idBlock,new LinearLayout.LayoutParams(0,dp(90),1));body.addView(profileHead,new LinearLayout.LayoutParams(-1,dp(108)));
        KingVipIdentityStyle.load(this,uid,nm,pid);

        TextView hero=tv("KING Plus • "+LevelSystem.levelTier((int)(long)(lv==null?1L:lv))+"  •  "+(xp==null?0:xp)+" XP",13,MUTED,true);hero.setGravity(Gravity.CENTER);hero.setBackground(bg(0xfffff0a5,14));body.addView(hero,new LinearLayout.LayoutParams(-1,dp(44)));
        // Public paid prestige requires a trusted backend grant, never self-authored profile fields.
        String nobleText=KingPrestigeIdentity9988.nobleLabel(false,0);
        TextView nobleBadge997=tv(nobleText,13,0xff684c91,true);
        nobleBadge997.setBackground(bg(0xfff4ecff,12));
        LinearLayout.LayoutParams nobleLp997=new LinearLayout.LayoutParams(-1,dp(44));
        nobleLp997.topMargin=dp(4);
        body.addView(nobleBadge997,nobleLp997);
        final String prestigeUid9988=uid;
        db.collection("king_public_prestige").document(prestigeUid9988)
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(grant->{
                if(isFinishing()||isDestroyed()||!prestigeUid9988.equals(uid))return;
                if(!grant.exists())return;
                Long v=grant.getLong("vipLevel"),nobleTier9988=grant.getLong("nobleLevel");
                Long total=grant.getLong("rechargeTotal");
                if(v==null||nobleTier9988==null||total==null||
                    !KingPrestigeIdentity9988.allowedVerifiedPublic(
                        v.intValue(),nobleTier9988.intValue(),total.longValue()))return;
                lvLine.setText("✓ Verified VIP "+v+" · Activity Lv."+(lv==null?1:lv));
                nobleBadge997.setText("✓ Verified Noble "+nobleTier9988+" (server record)");
            }).addOnFailureListener(err->{
                // Never substitute unverified local state on network error.
            });
        if(!activeRoomId997.isEmpty()&&db!=null){
            final String viewingUid=uid;
            final String viewingRoom=activeRoomId997;
            TextView charisma997=tv("🔥 Room Charisma • Loading actual room gifts…",
                13,0xff6d4e1b,true);
            charisma997.setBackground(bg(0xfffff5dc,12));
            LinearLayout.LayoutParams charismaLp997=new LinearLayout.LayoutParams(-1,dp(58));
            charismaLp997.topMargin=dp(4);
            body.addView(charisma997,charismaLp997);
            db.collection("live_rooms").document(viewingRoom).collection("events")
                .orderBy("createdAt",Query.Direction.DESCENDING).limit(150)
                .get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(events->{
                    if(isFinishing()||isDestroyed())return;
                    long received=0;
                    for(DocumentSnapshot event:events.getDocuments()){
                        if(!"gift".equals(event.getString("type"))
                            ||!viewingUid.equals(event.getString("targetUid")))continue;
                        Long quantity=event.getLong("quantity");
                        if(quantity==null)quantity=event.getLong("qty");
                        received+=Math.min(9,Math.max(1,quantity==null?1:quantity));
                    }
                    charisma997.setText("🔥 Room Charisma • "+received+
                        " received gift(s) in recent 150 events\n"+
                        "Free activity count only • no paid rank");
                })
                .addOnFailureListener(error->{
                    if(!isFinishing()&&!isDestroyed())
                        charisma997.setText("🔥 Room Charisma unavailable: "+
                            String.valueOf(error.getLocalizedMessage()));
                });
        }
        // Real public Gift Wish: resolves from the saved KING account, not
        // private phone preferences or invented demo gift achievements.
        final String wishUid9983=uid;
        TextView wishBadge9983=tv("🎁 Gift Wish • Loading…",13,PURPLE,true);
        wishBadge9983.setBackground(bg(0xfffff6d5,12));
        LinearLayout.LayoutParams wishLp9983=new LinearLayout.LayoutParams(-1,dp(42));
        wishLp9983.topMargin=dp(4);
        body.addView(wishBadge9983,wishLp9983);
        db.collection("king_me_wishes").document(wishUid9983)
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(w->{
                if(isFinishing()||isDestroyed()||!wishUid9983.equals(uid))return;
                String chosen=w.exists()?w.getString("wish"):null;
                wishBadge9983.setText(KingMeGiftSummary9983.validWish(chosen)
                    ?"🎁 Gift Wish • "+chosen:"🎁 No Gift Wish selected");
            }).addOnFailureListener(err->{
                if(!isFinishing()&&!isDestroyed()&&wishUid9983.equals(uid))
                    wishBadge9983.setText("🎁 Gift Wish unavailable online");
            });
        TextView cosmetics=tv("🖼 "+frame+"     ✨ "+effect+(badge.equals("None")?"":"\n"+KingCosmetics.badgeEmoji(badge)+" "+badge+"     💬 "+bubble),12,PURPLE,true);cosmetics.setGravity(Gravity.CENTER_VERTICAL);cosmetics.setBackground(bg(0xfff7f3fb,13));LinearLayout.LayoutParams c953=new LinearLayout.LayoutParams(-1,dp(badge.equals("None")?48:62));c953.setMargins(0,dp(5),0,0);body.addView(cosmetics,c953);
        TextView about=tv(bio+(tags.isEmpty()?"":"\n# "+tags),14,INK,false);about.setBackground(bg(Color.WHITE,14));about.setPadding(dp(12),dp(8),dp(12),dp(8));body.addView(about,new LinearLayout.LayoutParams(-1,dp(88)));

        LinearLayout actions=new LinearLayout(this);actions.setGravity(Gravity.CENTER);followBtn=action("＋ Follow");followBtn.setOnClickListener(v->KingSafe.run(this,"public-profile-follow",this::toggleFollow));actions.addView(followBtn,new LinearLayout.LayoutParams(0,dp(50),1));TextView msg=action("✉ Message");msg.setOnClickListener(v->KingSafe.run(this,"public-profile-chat",this::openChat));actions.addView(msg,new LinearLayout.LayoutParams(0,dp(50),1));TextView gift=action("🎁 Gift");gift.setOnClickListener(v->KingSafe.run(this,"public-profile-gift",this::openGift));actions.addView(gift,new LinearLayout.LayoutParams(0,dp(50),1));body.addView(actions);addPublicCounts940();

        section("Profile & Collection");
        row("⭐ Levels & VIP","Activity XP is TEST • VIP only verified by server",()->openDeep("vip"));
        row("🎭 Collection & cosmetics","Frame, entrance, chat bubble and medals",()->showPublicCollection953(p));
        row("🎁 Gift history","Secure sent/received gift ledger",this::openGift);
        row("💞 Relationship","CP / relationship center",()->openCommunity("Relationship"));
        row("🏠 Family","Family square and party",()->openCommunity("Family"));
        section("Safety & Sharing");row("↗ Share KING ID","Share this profile",this::share);row("⚑ Report","Report this user",this::report);row("🚫 Block / Unblock","Keep this user out of your social feed",this::toggleBlock);checkFollow();
    }
    private void showPublicCollection953(DocumentSnapshot p){Long lv=p.getLong("level"),vip=0L,xp=p.getLong("xp"),vp=0L;String frame=safe(p.getString("equippedFrame"),"Minimal Frame"),effect=safe(p.getString("entranceEffect"),"Welcome Sparkle"),bubble=safe(p.getString("chatBubble"),"Default Bubble"),badge=safe(p.getString("nameBadge"),"None");LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(14),dp(8),dp(14),dp(10));TextView title=tv("👑 "+name+" • Collection",17,INK,true);title.setGravity(Gravity.CENTER);root.addView(title,new LinearLayout.LayoutParams(-1,dp(44)));String medals=((lv==null?1:lv)>=10?"🏅 Rising Star  ":"")+((lv==null?1:lv)>=30?"🎖 Elite Voice  ":"")+"";TextView progress=tv("⭐ Lv."+(lv==null?1:lv)+" • "+(xp==null?0:xp)+" XP\n💎 VIP "+(vip==null?0:vip)+" • "+(vp==null?0:vp)+" points",13,INK,true);progress.setBackground(bg(0xfffff1bd,14));progress.setPadding(dp(12),dp(8),dp(12),dp(8));root.addView(progress,new LinearLayout.LayoutParams(-1,dp(66)));TextView cosmetic=tv("🖼 "+frame+"\n✨ "+effect+"\n💬 "+bubble+(badge.equals("None")?"":"\n"+KingCosmetics.badgeEmoji(badge)+" "+badge),12,PURPLE,true);cosmetic.setBackground(bg(0xfff6f2fa,14));cosmetic.setPadding(dp(12),dp(8),dp(12),dp(8));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,badge.equals("None")?dp(86):dp(106));cp.setMargins(0,dp(6),0,dp(6));root.addView(cosmetic,cp);TextView medal=tv(medals.isEmpty()?"No public medals unlocked yet":medals,12,MUTED,true);medal.setGravity(Gravity.CENTER);root.addView(medal,new LinearLayout.LayoutParams(-1,dp(48)));new AlertDialog.Builder(this).setTitle("Collection").setView(root).setPositiveButton("Send Gift",(d,w)->openGift()).setNegativeButton("Close",null).show();}

    private void loadProfilePhoto940(android.widget.ImageView image,TextView fallback,String url){
        KingProfileImages10068.load(this,image,fallback,url);
    }

    private void addPublicCounts940(){
        LinearLayout counts=new LinearLayout(this);counts.setGravity(Gravity.CENTER);counts.setPadding(0,dp(6),0,dp(6));
        publicFollowers940=publicStat940(counts,"…","Followers");publicFollowing940=publicStat940(counts,"…","Following");publicFriends940=publicStat940(counts,"…","Friends");
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(66));cp.setMargins(0,dp(5),0,dp(5));body.addView(counts,cp);loadPublicCounts940();
    }
    private TextView publicStat940(LinearLayout row,String value,String label){
        TextView t=tv(value+"\n"+label,13,INK,true);t.setGravity(Gravity.CENTER);t.setBackground(bg(Color.WHITE,12));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(58),1);p.setMargins(dp(2),0,dp(2),0);row.addView(t,p);return t;
    }
    private void setPublicStat940(TextView v,long n,String label){if(v!=null)v.setText(n+"\n"+label);}
    private void failPublicCounts940(){if(publicFollowers940!=null)publicFollowers940.setText("—\nFollowers");if(publicFollowing940!=null)publicFollowing940.setText("—\nFollowing");if(publicFriends940!=null)publicFriends940.setText("—\nFriends");}
    private void removePublicFollowListeners965(){
        if(incoming965!=null){incoming965.remove();incoming965=null;}
        if(outgoing965!=null){outgoing965.remove();outgoing965=null;}
        if(followDoc965!=null){followDoc965.remove();followDoc965=null;}
        publicFollowerSet965.clear();publicFollowingSet965.clear();
        publicIncomingLoaded965=false;publicOutgoingLoaded965=false;
    }

    private void drawPublicFollowCounts965(){
        if(isFinishing()||isDestroyed())return;
        if(publicIncomingLoaded965)
            setPublicStat940(publicFollowers940,publicFollowerSet965.size(),"Followers");
        if(publicOutgoingLoaded965)
            setPublicStat940(publicFollowing940,publicFollowingSet965.size(),"Following");
        if(publicIncomingLoaded965&&publicOutgoingLoaded965){
            int mutual965=0;
            for(String u:publicFollowerSet965)if(publicFollowingSet965.contains(u))mutual965++;
            setPublicStat940(publicFriends940,mutual965,"Friends");
        }
    }

    private void loadPublicCounts940(){
        if(db==null||uid==null||uid.isEmpty()){failPublicCounts940();return;}
        if(incoming965!=null){incoming965.remove();incoming965=null;}
        if(outgoing965!=null){outgoing965.remove();outgoing965=null;}
        publicIncomingLoaded965=false;publicOutgoingLoaded965=false;
        outgoing965=db.collection("follows").whereEqualTo("followerUid",uid)
            .addSnapshotListener((snap,e)->{
                if(isFinishing()||isDestroyed())return;
                if(e!=null||snap==null){failPublicCounts940();return;}
                publicFollowingSet965.clear();
                for(DocumentSnapshot d:snap.getDocuments()){
                    String value=d.getString("targetUid");
                    if(value!=null&&!value.isEmpty())publicFollowingSet965.add(value);
                }
                publicOutgoingLoaded965=true;
                drawPublicFollowCounts965();
            });
        incoming965=db.collection("follows").whereEqualTo("targetUid",uid)
            .addSnapshotListener((snap,e)->{
                if(isFinishing()||isDestroyed())return;
                if(e!=null||snap==null){failPublicCounts940();return;}
                publicFollowerSet965.clear();
                for(DocumentSnapshot d:snap.getDocuments()){
                    String value=d.getString("followerUid");
                    if(value!=null&&!value.isEmpty())publicFollowerSet965.add(value);
                }
                publicIncomingLoaded965=true;
                drawPublicFollowCounts965();
            });
    }

    private TextView action(String s){TextView t=tv(s,12,Color.WHITE,true);t.setGravity(Gravity.CENTER);t.setBackground(bg(PURPLE,12));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(50),1);lp.setMargins(dp(2),dp(6),dp(2),dp(6));t.setLayoutParams(lp);return t;}
    private void section(String s){TextView t=tv(s.toUpperCase(),12,MUTED,true);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(38));lp.setMargins(0,dp(8),0,0);body.addView(t,lp);}    
    private void row(String a,String b,Runnable r){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setPadding(dp(14),dp(8),dp(14),dp(8));x.setBackground(bg(Color.WHITE,14));TextView t=tv(a,15,INK,true);TextView d=tv(b,11,MUTED,false);x.addView(t,new LinearLayout.LayoutParams(-1,dp(30)));x.addView(d,new LinearLayout.LayoutParams(-1,dp(25)));x.setOnClickListener(v->KingSafe.run(this,"public-profile-row:"+a,r));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(62));lp.setMargins(0,dp(4),0,dp(4));body.addView(x,lp);}    
    private String followId(){return me==null?"":KingSocialIdentity965.followId(me.getUid(),uid);}    
    private void checkFollow(){
        if(followDoc965!=null){followDoc965.remove();followDoc965=null;}
        if(me==null||db==null||me.getUid().equals(uid)){
            if(followBtn!=null)followBtn.setText(me!=null&&me.getUid().equals(uid)?"My Profile":"＋ Follow");
            return;
        }
        // A live listener reflects changes made from other devices immediately.
        followDoc965=db.collection("follows").document(followId()).addSnapshotListener((d,e)->{
            if(!isFinishing()&&!isDestroyed()&&followBtn!=null){
                if(e!=null){followBtn.setText("Follow unavailable");return;}
                followBtn.setText(d!=null&&d.exists()?"✓ Following":"＋ Follow");
            }
        });
        migrateLegacyFollow965();
    }
    private void migrateLegacyFollow965(){
        if(me==null||db==null||uid==null||uid.isEmpty()||uid.equals(me.getUid()))return;
        // v9.6.4 Public Profile used _ while every other Social/Party page used __.
        final com.google.firebase.firestore.DocumentReference oldRef=
            db.collection("follows").document(KingSocialIdentity965.legacyFollowId(me.getUid(),uid));
        oldRef.get().addOnSuccessListener(oldDoc->{
            if(!oldDoc.exists()||!me.getUid().equals(oldDoc.getString("followerUid"))
                ||!uid.equals(oldDoc.getString("targetUid")))return;
            final com.google.firebase.firestore.DocumentReference current=
                db.collection("follows").document(followId());
            current.get().addOnSuccessListener(nowDoc->{
                if(nowDoc.exists()){
                    oldRef.delete().addOnFailureListener(e->{});
                    return;
                }
                java.util.Map<String,Object> migrated=new java.util.HashMap<>();
                migrated.put("followerUid",me.getUid());
                migrated.put("targetUid",uid);
                migrated.put("createdAt",oldDoc.get("createdAt")==null
                    ?com.google.firebase.firestore.FieldValue.serverTimestamp():oldDoc.get("createdAt"));
                current.set(migrated).addOnSuccessListener(v->
                    oldRef.delete().addOnFailureListener(e->{}))
                    .addOnFailureListener(e->toast("Old Follow will sync when network returns"));
            });
        });
    }


    private void toggleFollow(){
        if(me==null||db==null){toast("Sign in first");return;}
        final String target=uid;
        KingFollowLive9997.toggle(db,me,target,displayName(),(following,error)->{
            if(isFinishing()||isDestroyed()||!target.equals(uid))return;
            if(error!=null){toast("Follow not changed • "+safe(error.getMessage()));return;}
            toast(Boolean.TRUE.equals(following)?"Following":"Unfollowed");
            checkFollow();
        });
    }
    private void openChat(){if(me==null){toast("Sign in first");return;}Intent i=new Intent(this,ChatActivity.class);i.putExtra("peerUid",uid);i.putExtra("peerName",name);startActivity(i);}    
    private void openGift(){Intent i=new Intent(this,KingGiftCenterActivity.class);i.putExtra("targetUid",uid);i.putExtra("targetName",name);startActivity(i);}    
    private void openCommunity(String tab){Intent i=new Intent(this,CommunityHubActivity.class);i.putExtra("tab",tab);startActivity(i);}    
    private void openDeep(String route){Intent i=new Intent(this,KingDeepFlowActivity.class);i.putExtra("route",route);startActivity(i);}    
    private void share(){
        if(me==null||db==null||uid==null||uid.isEmpty()){toast("Sign in to share a KING profile");return;}
        final String target=uid;
        db.collection("public_profiles").document(target)
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(p->{
                if(isFinishing()||isDestroyed()||!target.equals(uid))return;
                String id=KingPublicId9996.verified(p.getString("publicId"));
                if(id==null){toast("This profile has no issued KING ID");return;}
                Intent send=new Intent(Intent.ACTION_SEND);send.setType("text/plain");
                send.putExtra(Intent.EXTRA_TEXT,"KING Plus • "+name+" • KING ID "+id);
                startActivity(Intent.createChooser(send,"Share profile"));
            }).addOnFailureListener(error->{if(!isFinishing()&&!isDestroyed())toast("Could not verify KING ID • try again");});
    }
    private void report(){if(me==null||db==null){toast("Sign in first");return;}String[] reasons={"Spam","Harassment","Inappropriate content","Impersonation","Other"};new AlertDialog.Builder(this).setTitle("Report "+name).setItems(reasons,(d,w)->{Map<String,Object>m=new HashMap<>();m.put("reporterUid",me.getUid());m.put("targetUid",uid);m.put("reason",reasons[w]);m.put("source","profile");m.put("createdAt",FieldValue.serverTimestamp());db.collection("reports").add(m).addOnSuccessListener(v->toast("Report submitted")).addOnFailureListener(e->toast("Report failed"));}).setNegativeButton("Cancel",null).show();}
    private String blockId(){return me==null?"":me.getUid()+"_"+uid;}    
    private void toggleBlock(){if(me==null||db==null){toast("Sign in first");return;}if(me.getUid().equals(uid)){toast("You cannot block yourself");return;}db.collection("blocks").document(blockId()).get().addOnSuccessListener(d->{if(d.exists())d.getReference().delete().addOnSuccessListener(v->toast("Unblocked"));else{Map<String,Object>m=new HashMap<>();m.put("ownerUid",me.getUid());m.put("targetUid",uid);m.put("createdAt",FieldValue.serverTimestamp());d.getReference().set(m).addOnSuccessListener(v->toast("Blocked"));}});}
    private String displayName(){String n=getSharedPreferences("MainActivity",MODE_PRIVATE).getString("name","KING User");return safe(n,"KING User");}
    private String safe(String s,String f){return s==null||s.trim().isEmpty()?f:s.trim();} private String safe(String s){return safe(s,"Unknown error");}
    private String publicId(String uid){return KingSocialIdentity965.publicId(uid);}
    @Override protected void onStart(){
        super.onStart();
        FirebaseUser signed=FirebaseAuth.getInstance().getCurrentUser();
        if(me==null||signed==null||!me.getUid().equals(signed.getUid())){finish();return;}
        profileVisible10068=true;load();
        if(publicFollowers940!=null && incoming965==null && outgoing965==null)loadPublicCounts940();
        if(followBtn!=null && followDoc965==null)checkFollow();
    }
    @Override protected void onStop(){
        profileVisible10068=false;++profileEpoch10068;
        if(profileLive10068!=null){profileLive10068.remove();profileLive10068=null;}
        removePublicFollowListeners965();
        super.onStop();
    }
    @Override protected void onDestroy(){
        if(profileLive10068!=null){profileLive10068.remove();profileLive10068=null;}
        removePublicFollowListeners965();
        super.onDestroy();
    }
    
}
