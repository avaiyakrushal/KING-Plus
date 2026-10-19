package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import com.google.firebase.storage.FirebaseStorage;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Original KING Plus social hub: Search, Moments, Family, Relationship and Collection. */
public class CommunityHubActivity extends Activity {
    private static final int BG=0xfff7f6fb,DARK=0xff201b2c,MUTED=0xff817a8c,CARD=0xffffffff,PURPLE=0xff7c4dff;
    private FirebaseUser me; private FirebaseFirestore db; private LinearLayout body; private String displayName="KING User";
    private String selected="Search", targetUid="", targetName="";
    private String selectedTopic940="All";
    private int searchGeneration9994;
    private boolean relationshipRequestInFlight10012;
    // A Moment POST stays in flight when users switch between Home tabs.
    // Keep the gate on the Activity, rather than only the discarded button.
    private final java.util.Set<String> followPending10033=new java.util.HashSet<>();
    private String pendingMomentAccount10013;
    private String pendingMomentText10048, pendingMomentId10048;
    private String searchDraft10007="";
    // Shared live Moments shown by Home, Me and Party room broadcasts.
    // Pagination is explicit to avoid silently hiding older account posts.
    private int momentsQueryLimit10000=80;
    private int momentsDisplayLimit10000=40;
    // Keep large feeds usable on phones: only first three photos auto-download.
    private int autoMomentPhotos10001=3;
    private LinearLayout activeMomentsFeed10004;
    private final List<com.google.firebase.firestore.ListenerRegistration> momentLikes10000=new ArrayList<>();

    @Override public void onCreate(Bundle b){super.onCreate(b);try{me=FirebaseAuth.getInstance().getCurrentUser();db=FirebaseFirestore.getInstance();}catch(Exception ignored){}SharedPreferences p=getSharedPreferences("MainActivity",MODE_PRIVATE);
        // Never display account A's locally cached Home name for signed-in account B.
        String cachedUid10032=p.getString("firebase_uid","");
        String signedUid10032=me==null?"":me.getUid();
        displayName=me==null?"KING User":safe(me.getDisplayName(),
            signedUid10032.equals(cachedUid10032)?p.getString("name","KING User"):"KING User");
        String t=getIntent().getStringExtra("tab");if(t!=null&&!t.isEmpty())selected=t;targetUid=safe(getIntent().getStringExtra("targetUid"),"");targetName=safe(getIntent().getStringExtra("targetName"),"");render(selected);}
    private com.google.firebase.firestore.ListenerRegistration familyMessages956;
    private com.google.firebase.firestore.ListenerRegistration familyMembers9986;
    private String realFamilyRole9987="member";
    private void stopFamilyChatOnly9987(){
        if(familyMessages956!=null){familyMessages956.remove();familyMessages956=null;}
    }
    private boolean familyStopped956;
    private void stopFamilyMessages956(){
        if(familyMessages956!=null){familyMessages956.remove();familyMessages956=null;}
        if(familyMembers9986!=null){familyMembers9986.remove();familyMembers9986=null;}
    }
    @Override protected void onStop(){++searchGeneration9994;stopMoments9990();familyStopped956=true;stopFamilyMessages956();super.onStop();}
    @Override protected void onStart(){
        super.onStart();
        FirebaseUser signed10031=FirebaseAuth.getInstance().getCurrentUser();
        if(signed10031==null){stopMoments9990();stopFamilyMessages956();finish();return;}
        if(me==null||!signed10031.getUid().equals(me.getUid())){
            // A new signed-in account gets a new live Home session, never cached posts.
            ++searchGeneration9994;stopMoments9990();stopFamilyMessages956();
            me=signed10031;pendingMomentAccount10013=null;
            pendingMomentText10048=null;pendingMomentId10048=null;
            relationshipRequestInFlight10012=false;
            followPending10033.clear();
            searchDraft10007="";momentsQueryLimit10000=80;momentsDisplayLimit10000=40;
            targetUid="";targetName="";
            displayName=safe(signed10031.getDisplayName(),"KING User");
            familyStopped956=false;render(selected);return;
        }
        if(familyStopped956){familyStopped956=false;render(selected);}
    }
    @Override protected void onDestroy(){stopMoments9990();stopFamilyMessages956();super.onDestroy();}
    private boolean cloud(){return me!=null&&db!=null;}
    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    private TextView tv(String s,int z,int c,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);if(bold)t.setTypeface(null,Typeface.BOLD);t.setPadding(dp(8),dp(6),dp(8),dp(6));return t;}
    private TextView button(String s,Runnable r){TextView t=tv(s,14,Color.WHITE,true);t.setGravity(Gravity.CENTER);t.setBackground(bg(PURPLE,14));t.setOnClickListener(v->KingSafe.run(this,"community-button:"+s,r));return t;}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    private String safe(String s,String f){return s==null||s.trim().isEmpty()?f:s.trim();}
    private String publicId(String uid){if(uid==null)return"000000";return String.valueOf(Integer.toUnsignedLong(uid.hashCode())%900000L+100000L);}

    private void render(String tab){++searchGeneration9994;stopMoments9990();stopFamilyMessages956();selected=tab;LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(8),dp(8),dp(8),dp(5));TextView back=tv("‹",38,DARK,false);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->finish());head.addView(back,new LinearLayout.LayoutParams(dp(48),dp(54)));TextView title=tv("KING Community",23,DARK,true);head.addView(title,new LinearLayout.LayoutParams(0,dp(54),1));root.addView(head);HorizontalScrollView hs=new HorizontalScrollView(this);hs.setHorizontalScrollBarEnabled(false);LinearLayout tabs=new LinearLayout(this);String[] ts={"Search","Moments","Topics","Family","Relationship","Collection","Visitors"};for(String x:ts){TextView t=tv(x,13,x.equals(tab)?Color.WHITE:DARK,true);t.setGravity(Gravity.CENTER);t.setBackground(bg(x.equals(tab)?PURPLE:0xffece9f2,14));t.setOnClickListener(v->render(x));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(104),dp(42));lp.setMargins(dp(4),0,dp(4),0);tabs.addView(t,lp);}hs.addView(tabs);root.addView(hs,new LinearLayout.LayoutParams(-1,dp(50)));ScrollView sv=new ScrollView(this);body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(14),dp(12),dp(14),dp(30));sv.addView(body);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);if("Moments".equals(tab))moments();else if("Topics".equals(tab))topics940();else if("Family".equals(tab))family();else if("Relationship".equals(tab))relationships();else if("Collection".equals(tab))collection();else if("Visitors".equals(tab))visitors750();else search();}

    private void search(){body.addView(tv("Find people & live rooms",21,DARK,true));EditText q=new EditText(this);q.setHint("Name, 6-digit KING ID or room");q.setSingleLine(true);
        q.setText(searchDraft10007);
        q.setSelection(q.getText().length());
        q.addTextChangedListener(new android.text.TextWatcher(){
            public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            public void onTextChanged(CharSequence s,int start,int before,int count){searchDraft10007=s.toString();}
            public void afterTextChanged(android.text.Editable e){}
        });q.setBackground(bg(CARD,14));q.setPadding(dp(12),0,dp(12),0);body.addView(q,new LinearLayout.LayoutParams(-1,dp(52)));LinearLayout row=new LinearLayout(this);TextView people=button("👤 People",()->searchPeople(q.getText().toString().trim()));TextView rooms=button("🎤 Rooms",()->searchRooms(q.getText().toString().trim()));LinearLayout.LayoutParams a=new LinearLayout.LayoutParams(0,dp(48),1);a.setMargins(0,dp(8),dp(4),0);row.addView(people,a);LinearLayout.LayoutParams c=new LinearLayout.LayoutParams(0,dp(48),1);c.setMargins(dp(4),dp(8),0,0);row.addView(rooms,c);body.addView(row);body.addView(tv("Search uses real signed-in KING Plus profiles and live rooms.",12,MUTED,false));}
    private void clearResults(String title){while(body.getChildCount()>4)body.removeViewAt(4);body.addView(tv(title,17,DARK,true));}
    /** A completed Firebase request must not paint onto a different Home tab or account. */
    private boolean currentSection10003(String tab,int generation,LinearLayout host,String account){
        return generation==searchGeneration9994 && tab.equals(selected) && body==host
            && !isFinishing() && !isDestroyed()
            && account!=null && KingSavedRoomsCloud9990.account(account);
    }
    private boolean validSearch9994(int token,LinearLayout host,String account){
        return token==searchGeneration9994 && "Search".equals(selected)
            && body==host && !isFinishing() && !isDestroyed()
            && account!=null && KingSavedRoomsCloud9990.account(account);
    }
    private void searchPeople(String raw){
        if(!cloud()||!KingSavedRoomsCloud9990.account(me.getUid())){toast("Sign in required");return;}
        final int token=++searchGeneration9994;
        final LinearLayout host=body;final String account=me.getUid();
        clearResults("People results");
        final String q=raw.toLowerCase(Locale.US);
        Query query=raw.matches("[0-9]{6}")
            ? db.collection("public_profiles").whereEqualTo("publicId",raw).limit(20)
            :q.isEmpty()?db.collection("public_profiles").limit(30)
            :db.collection("public_profiles").orderBy("searchName").startAt(q).endAt(q+"\uf8ff").limit(30);
        final TextView waiting=tv("Searching people…",13,MUTED,false);
        host.addView(waiting);
        query.get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(snap->{if(!validSearch9994(token,host,account))return;
                host.removeView(waiting);renderUsers(snap.getDocuments(),host);})
            .addOnFailureListener(error->{if(!validSearch9994(token,host,account))return;
                waiting.setText("People search unavailable • tap to retry.");
                waiting.setOnClickListener(v->{if(validSearch9994(token,host,account))searchPeople(raw);});
            });
    }
    private void renderUsers(List<DocumentSnapshot> docs,LinearLayout host){
        int n=0;for(DocumentSnapshot d:docs){String uid=d.getId();if(!KingDocumentId10036.valid(uid)||me!=null&&uid.equals(me.getUid()))continue;String name=safe(d.getString("displayName"),"KING User");Long lv=d.getLong("level"),vv=d.getLong("vipLevel");String issuedId9996=KingPublicId9996.verified(d.getString("publicId"));TextView r=tv("👤  "+name+"   • ID "+(issuedId9996==null?"Not issued":issuedId9996)+"\n     Lv."+(lv==null?1:lv)+"  VIP "+(vv==null?0:vv),14,DARK,true);r.setBackground(bg(CARD,14));r.setOnClickListener(v->userActions(uid,name,issuedId9996));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(66));lp.setMargins(0,dp(5),0,dp(5));host.addView(r,lp);n++;}if(n==0)host.addView(tv("No matching people.",14,MUTED,false));
    }
    private void userActions(String uid,String name,String issuedId9996){String[] a={"Follow / Unfollow","Private Chat","Relationship / CP","Gift in Chat","Share KING ID"};new AlertDialog.Builder(this).setTitle(name).setItems(a,(d,w)->{if(w==0)toggleFollow(uid,name);else if(w==1||w==3)openChat(uid,name);else if(w==2){targetUid=uid;targetName=name;render("Relationship");}else shareId(uid,name,issuedId9996);}).show();}
    private void toggleFollow(String uid,String name){
        if(!cloud()||!KingSavedRoomsCloud9990.account(me.getUid())){toast("Sign in required");return;}
        if(uid==null||uid.trim().isEmpty()||uid.equals(me.getUid()))return;
        final String owner10033=me.getUid();
        final String flight10033=owner10033+"__"+uid;
        if(!followPending10033.add(flight10033)){toast("Follow update is still saving");return;}
        KingFollowLive9997.toggle(db,me,uid,displayName,(following,error)->{
            followPending10033.remove(flight10033);
            if(isFinishing()||isDestroyed()||!KingSavedRoomsCloud9990.account(owner10033)
                ||me==null||!owner10033.equals(me.getUid()))return;
            if(error!=null){toast("Follow not changed • "+error.getMessage());return;}
            toast(Boolean.TRUE.equals(following)?"Following "+name:"Unfollowed "+name);
        });
    }
    private void openChat(String uid,String name){Intent i=new Intent(this,ChatActivity.class);i.putExtra("peerUid",uid);i.putExtra("peerName",name);startActivity(i);}
    private void shareId(String uid,String name,String issuedId9996){
        // The former hash(uid) generated unofficial ID numbers that could
        // lead another phone to the wrong account. Share only issued IDs.
        String id=KingPublicId9996.verified(issuedId9996);
        if(id==null){toast("This profile has no verified KING ID yet");return;}
        Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT,"Find "+name+" on KING Plus • KING ID "+id);
        startActivity(Intent.createChooser(i,"Share KING ID"));
    }
    private void searchRooms(String raw){
        if(!cloud()||!KingSavedRoomsCloud9990.account(me.getUid())){toast("Sign in required");return;}
        final int token=++searchGeneration9994;
        final LinearLayout host=body;final String account=me.getUid();
        final String q=raw.toLowerCase(Locale.US);
        clearResults("Live room results");
        loadOlderRooms9995(q,host,account,token,null,3);
    }
    /** Search up to four pages automatically when a named room has no matches.
     * Keep an explicit older-rooms button so long histories remain user-controlled. */
    private void loadOlderRooms9995(String q,LinearLayout host,String account,int token,DocumentSnapshot after,int extraAutoPages){
        if(!validSearch9994(token,host,account))return;
        final TextView progress=tv(after==null?"Searching live rooms…":"Loading older live rooms…",13,MUTED,false);
        host.addView(progress);
        Query query=db.collection("live_rooms").orderBy("createdAt",Query.Direction.DESCENDING).limit(80);
        if(after!=null)query=query.startAfter(after);
        query.get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(snapshot->{
            if(!validSearch9994(token,host,account))return;
            host.removeView(progress);
            List<DocumentSnapshot> docs=snapshot.getDocuments();
            int shown=0,processed=0;
            DocumentSnapshot last=after;
            for(DocumentSnapshot d:docs){
                last=d;processed++;
                if(Boolean.TRUE.equals(d.getBoolean("closed")))continue;
                String name=safe(d.getString("name"),"Live Party"),cat=safe(d.getString("category"),"Chat"),owner=safe(d.getString("ownerName"),"KING Host");
                if(!q.isEmpty()&&!(name+" "+cat+" "+owner+" "+d.getId()).toLowerCase(Locale.US).contains(q))continue;
                TextView item=tv("🎤  "+name+"\n     "+cat+" • Host "+owner,14,DARK,true);
                item.setBackground(bg(CARD,14));
                final String room=d.getId();
                item.setOnClickListener(v->{if(!validSearch9994(token,host,account))return;
                    Intent intent=new Intent(this,PartyActivity.class);
                    intent.putExtra("directRoomId",room);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);startActivity(intent);
                });
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(68));lp.setMargins(0,dp(5),0,dp(5));host.addView(item,lp);
                if(++shown>=30)break;
            }
            boolean hasNext=last!=null && (processed<docs.size()||docs.size()==80);
            if(shown==0 && hasNext && !q.isEmpty() && extraAutoPages>0){
                // A named room can be older than the newest 80; scan the next
                // page instead of incorrectly reporting no results yet.
                loadOlderRooms9995(q,host,account,token,last,extraAutoPages-1);
                return;
            }
            if(shown==0)host.addView(tv("No matching rooms in this page.",13,MUTED,false));
            if(hasNext){
                final DocumentSnapshot cursor=last;
                TextView more=button("↓ Search older live rooms",()->{});
                more.setOnClickListener(v->{if(!validSearch9994(token,host,account))return;
                    host.removeView(more);loadOlderRooms9995(q,host,account,token,cursor,0);
                });
                host.addView(more,new LinearLayout.LayoutParams(-1,dp(46)));
            } else if(after!=null){
                host.addView(tv("End of live room search.",12,MUTED,false));
            }
        }).addOnFailureListener(error->{if(!validSearch9994(token,host,account))return;
            progress.setText("Room search unavailable • check connection and retry.");
            progress.setOnClickListener(v->{if(!validSearch9994(token,host,account))return;
                host.removeView(progress);loadOlderRooms9995(q,host,account,token,after,extraAutoPages);
            });
        });
    }


    private void moments(){
        body.addView(tv("Moments / Square",21,DARK,true));
        TextView refresh10061=button("↻ Refresh Live Home Moments",()->loadMoments());
        body.addView(refresh10061,new LinearLayout.LayoutParams(-1,dp(42)));
        if(!cloud()){body.addView(tv("Sign in to publish Moments.",14,MUTED,false));return;}
        TextView topic=tv("🏷 Topic: "+selectedTopic940+"   ›",13,DARK,true);topic.setGravity(Gravity.CENTER_VERTICAL);topic.setBackground(bg(0xffeee9ff,12));topic.setOnClickListener(v->chooseTopic940(true));body.addView(topic,new LinearLayout.LayoutParams(-1,dp(46)));
        EditText e=new EditText(this);e.setHint("Share a moment…");e.setBackground(bg(CARD,14));e.setMinLines(2);LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(-1,dp(78));ep.setMargins(0,dp(8),0,0);body.addView(e,ep);
        TextView post=button("Post Moment",()->{});post.setOnClickListener(v->postMoment(e,post));LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,dp(46));pp.setMargins(0,dp(7),0,dp(10));body.addView(post,pp);
        loadMoments();
    }
    private boolean currentMomentPost10007(String uid,int generation,LinearLayout host){
        return !isFinishing()&&!isDestroyed()&&me!=null
            &&KingMeHomePartyGuard10007.momentCallbackCurrent(uid,me.getUid(),
                generation,searchGeneration9994,"Moments".equals(selected),host==body);
    }
    private void postMoment(EditText e,TextView post){
        if(!cloud()||!KingSavedRoomsCloud9990.account(me.getUid())){toast("Sign in required");return;}
        if(pendingMomentAccount10013!=null){toast("A Moment is still posting. Check its result first.");return;}
        final String text=e.getText().toString().trim();
        if(text.isEmpty()){e.setError("Write a Moment first");return;}
        if(text.length()>1000){e.setError("Moments must be under 1000 characters");return;}
        final String postingUid10048=me.getUid();
        final LinearLayout postingHost10048=body;
        final int postingGeneration10048=searchGeneration9994;
        final String id10048;
        if(text.equals(pendingMomentText10048)&&pendingMomentId10048!=null)
            id10048=pendingMomentId10048;
        else{
            id10048="moment_"+java.util.UUID.randomUUID().toString().replace("-","");
            pendingMomentText10048=text;pendingMomentId10048=id10048;
        }
        pendingMomentAccount10013=postingUid10048;
        post.setEnabled(false);post.setText("Posting…");
        Map<String,Object> moment10048=new HashMap<>();
        moment10048.put("uid",postingUid10048);moment10048.put("name",displayName);
        moment10048.put("text",text);
        moment10048.put("topic","All".equals(selectedTopic940)?"General":selectedTopic940);
        moment10048.put("createdAt",FieldValue.serverTimestamp());
        final DocumentReference ref10048=db.collection("moments").document(id10048);
        ref10048.set(moment10048).addOnSuccessListener(ok10048->{
            finishMomentPost10048(postingUid10048,postingGeneration10048,postingHost10048,
                id10048,text,e,post,true,false);
        }).addOnFailureListener(err10048->{
            if(!postingUid10048.equals(pendingMomentAccount10013))return;
            // A write ACK can time out *after* the server accepted it. Never
            // generate a new document ID until this exact one was checked.
            if(currentMomentPost10007(postingUid10048,postingGeneration10048,postingHost10048))
                post.setText("Checking delivery…");
            ref10048.get(com.google.firebase.firestore.Source.SERVER).addOnCompleteListener(check10048->{
                boolean confirmed10048=check10048.isSuccessful()
                    &&check10048.getResult()!=null&&check10048.getResult().exists()
                    &&postingUid10048.equals(check10048.getResult().getString("uid"));
                finishMomentPost10048(postingUid10048,postingGeneration10048,postingHost10048,
                    id10048,text,e,post,confirmed10048,true);
            });
        });
    }
    private void finishMomentPost10048(String uid10048,int generation10048,
            LinearLayout host10048,String id10048,String text10048,EditText editor10048,
            TextView button10048,boolean success10048,boolean checked10048){
        if(uid10048.equals(pendingMomentAccount10013))pendingMomentAccount10013=null;
        if(success10048&&id10048.equals(pendingMomentId10048)){
            pendingMomentText10048=null;pendingMomentId10048=null;
        }
        if(!currentMomentPost10007(uid10048,generation10048,host10048))return;
        button10048.setEnabled(true);
        button10048.setText(success10048?"Post Moment":"Retry Moment");
        if(success10048){
            if(editor10048.getText().toString().trim().equals(text10048))editor10048.setText("");
            if(checked10048)toast("Moment posted • verified on server");
        }else toast("Moment delivery uncertain • retry uses the same post ID");
    }
    private com.google.firebase.firestore.ListenerRegistration momentsListener9990;
    private int momentsGeneration9990;
    private void stopMomentLikes10000(){
        for(com.google.firebase.firestore.ListenerRegistration listener:momentLikes10000){
            if(listener!=null)listener.remove();
        }
        momentLikes10000.clear();
    }
    private void stopMoments9990(){
        ++momentsGeneration9990;
        stopMomentLikes10000();
        if(momentsListener9990!=null){momentsListener9990.remove();momentsListener9990=null;}
    }
    private void loadMoments(){
        stopMoments9990();final int generation=momentsGeneration9990;
        // Replacing a paged Moments subscription must also replace its old cards.
        // Detached Firestore listeners alone do not remove their rendered Views.
        if(activeMomentsFeed10004!=null && activeMomentsFeed10004.getParent()==body)
            body.removeView(activeMomentsFeed10004);
        final LinearLayout feed=new LinearLayout(this);feed.setOrientation(LinearLayout.VERTICAL);
        activeMomentsFeed10004=feed;body.addView(feed);
        final String account=me.getUid(),topicFilter=selectedTopic940;
        feed.addView(tv("Loading live Moments…",14,MUTED,false));
        momentsListener9990=db.collection("moments").orderBy("createdAt",Query.Direction.DESCENDING).limit(momentsQueryLimit10000)
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(snap,error)->{
                if(generation!=momentsGeneration9990||isFinishing()||isDestroyed()||!"Moments".equals(selected)
                    ||!KingSavedRoomsCloud9990.account(account))return;
                stopMomentLikes10000();
                feed.removeAllViews();
                if(error!=null||snap==null){
                    TextView retry=tv("Moments unavailable • tap to retry",14,MUTED,false);
                    retry.setOnClickListener(v->{if(generation==momentsGeneration9990)loadMoments();});
                    feed.addView(retry);return;
                }
                if(snap.getMetadata().hasPendingWrites())
                    feed.addView(tv("Posting changes • waiting for server confirmation",13,MUTED,false));
                else if(snap.getMetadata().isFromCache())
                    feed.addView(tv("Offline / cached Moments • reconnect to refresh",13,MUTED,false));
                int shown=0,matched=0;
                autoMomentPhotos10001=3;
                for(DocumentSnapshot doc:snap.getDocuments()){
                    String topic=safe(doc.getString("topic"),"General");
                    if(!"All".equals(topicFilter)&&!topicFilter.equalsIgnoreCase(topic))continue;
                    ++matched;
                    if(shown<momentsDisplayLimit10000){addMoment(doc,feed);++shown;}
                }
                if(shown==0)feed.addView(tv("No Moments found in the newest "+snap.size()+" posts for "+topicFilter+".",14,MUTED,false));
                final boolean moreVisible=matched>shown;
                final boolean olderAvailable=snap.size()>=momentsQueryLimit10000 && momentsQueryLimit10000<320;
                if(moreVisible||olderAvailable){
                    TextView more=button(moreVisible?"↓ Show more Moments ("+shown+" shown)":"↓ Search older Moments",()->{});
                    more.setOnClickListener(v->{
                        if(generation!=momentsGeneration9990 ||!"Moments".equals(selected)
                            ||!KingSavedRoomsCloud9990.account(account)||more.getParent()!=feed
                            ||!more.isEnabled())return;
                        more.setEnabled(false);
                        if(moreVisible)momentsDisplayLimit10000=Math.min(320,momentsDisplayLimit10000+40);
                        else momentsQueryLimit10000=Math.min(320,momentsQueryLimit10000+80);
                        loadMoments();
                    });
                    feed.addView(more,new LinearLayout.LayoutParams(-1,dp(48)));
                } else if(momentsQueryLimit10000>=320 && snap.size()>=320){
                    final DocumentSnapshot last10018=snap.getDocuments().get(snap.size()-1);
                    LinearLayout history10018=new LinearLayout(this);
                    history10018.setOrientation(LinearLayout.VERTICAL);
                    feed.addView(history10018);
                    TextView older10018=button("↓ Load Moments older than the latest 320",()->{});
                    older10018.setOnClickListener(v->{
                        if(generation!=momentsGeneration9990 || !"Moments".equals(selected)
                            || !KingSavedRoomsCloud9990.account(account) || history10018.getParent()!=feed
                            ||older10018.getParent()!=feed)return;
                        older10018.setEnabled(false);
                        feed.removeView(older10018);
                        loadOlderMoments10018(history10018,last10018,generation,account,topicFilter);
                    });
                    feed.addView(older10018,new LinearLayout.LayoutParams(-1,dp(46)));
                }
            });
    }
    // Real server-cursor history; not an ever-growing in-memory 320-row query.
    private void loadOlderMoments10018(LinearLayout history10018,DocumentSnapshot cursor10018,
                                      int generation10018,String account10018,String topic10018){
        if(cursor10018==null||generation10018!=momentsGeneration9990
            ||!"Moments".equals(selected)||!KingSavedRoomsCloud9990.account(account10018)
            ||history10018.getParent()==null)return;
        TextView progress10018=tv("⌛ Loading older Moments…",13,MUTED,false);
        for(int i10024=history10018.getChildCount()-1;i10024>=0;i10024--){
            View child10024=history10018.getChildAt(i10024);
            if("empty-topic-page-10024".equals(child10024.getTag()))history10018.removeView(child10024);
        }
        history10018.addView(progress10018);
        db.collection("moments").orderBy("createdAt",Query.Direction.DESCENDING)
            .startAfter(cursor10018).limit(80).get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(page10018->{
                if(generation10018!=momentsGeneration9990||!"Moments".equals(selected)
                    ||isFinishing()||isDestroyed()||!KingSavedRoomsCloud9990.account(account10018)
                    ||history10018.getParent()==null)return;
                history10018.removeView(progress10018);
                autoMomentPhotos10001=0;int shown10018=0;
                for(DocumentSnapshot d10018:page10018.getDocuments()){
                    String topicRow10018=safe(d10018.getString("topic"),"General");
                    if(!"All".equals(topic10018)&&!topic10018.equalsIgnoreCase(topicRow10018))continue;
                    addMoment(d10018,history10018);shown10018++;
                }
                if(shown10018==0){
                    TextView empty10024=tv("No matching Moments in this batch • load older pages",12,MUTED,false);
                    empty10024.setTag("empty-topic-page-10024");history10018.addView(empty10024);
                }
                if(page10018.size()==80){
                    DocumentSnapshot next10018=page10018.getDocuments().get(page10018.size()-1);
                    TextView more10018=button("↓ Load even older Moments",()->{});
                    more10018.setOnClickListener(v->{
                        if(generation10018!=momentsGeneration9990||!"Moments".equals(selected)
                            ||!KingSavedRoomsCloud9990.account(account10018)||history10018.getParent()==null)return;
                        if(more10018.getParent()!=history10018)return;
                        history10018.removeView(more10018);
                        loadOlderMoments10018(history10018,next10018,generation10018,account10018,topic10018);
                    });
                    history10018.addView(more10018,new LinearLayout.LayoutParams(-1,dp(46)));
                }else history10018.addView(tv("✓ End of Moments history",12,MUTED,false));
            }).addOnFailureListener(error10018->{
                if(generation10018!=momentsGeneration9990||!"Moments".equals(selected)
                    ||!KingSavedRoomsCloud9990.account(account10018)||history10018.getParent()==null)return;
                progress10018.setText("↻ Could not load older Moments • tap to retry");
                progress10018.setOnClickListener(v->{
                    if(generation10018!=momentsGeneration9990||!"Moments".equals(selected)
                        ||!KingSavedRoomsCloud9990.account(account10018)
                        ||progress10018.getParent()!=history10018)return;
                    progress10018.setEnabled(false);
                    history10018.removeView(progress10018);
                    loadOlderMoments10018(history10018,cursor10018,generation10018,account10018,topic10018);
                });
            });
    }
    private void showMomentPhoto9991(String url,LinearLayout card,boolean loadAutomatically10001){
        if(url==null||!url.startsWith("https://firebasestorage.googleapis.com/"))return;
        TextView placeholder=tv("📷 Loading photo…",12,MUTED,false);
        card.addView(placeholder);
        ImageView image=new ImageView(this);image.setAdjustViewBounds(true);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setVisibility(android.view.View.GONE);
        card.addView(image,new LinearLayout.LayoutParams(-1,dp(210)));
        final boolean[] photoLoading10001={false};
        Runnable load=()->{
            if(photoLoading10001[0]||isFinishing()||isDestroyed()||image.getParent()==null)return;
            photoLoading10001[0]=true;
            placeholder.setText("📷 Loading photo…");
            try{
                FirebaseStorage.getInstance().getReferenceFromUrl(url).getBytes(10L*1024*1024)
                  .addOnSuccessListener(bytes->{
                    photoLoading10001[0]=false;
                    if(isFinishing()||isDestroyed()||image.getParent()==null)return;
                    BitmapFactory.Options opts=new BitmapFactory.Options();opts.inJustDecodeBounds=true;
                    BitmapFactory.decodeByteArray(bytes,0,bytes.length,opts);
                    opts.inJustDecodeBounds=false;opts.inSampleSize=Math.max(1,Math.max(opts.outWidth/800,opts.outHeight/800));
                    android.graphics.Bitmap bitmap=BitmapFactory.decodeByteArray(bytes,0,bytes.length,opts);
                    if(bitmap!=null){image.setImageBitmap(bitmap);image.setVisibility(android.view.View.VISIBLE);placeholder.setVisibility(android.view.View.GONE);}
                    else placeholder.setText("Photo not available • tap to retry");
                  }).addOnFailureListener(err->{photoLoading10001[0]=false;
                      if(image.getParent()!=null)placeholder.setText("Photo unavailable • tap to retry");});
            }catch(Exception ex){photoLoading10001[0]=false;placeholder.setText("Photo unavailable • tap to retry");}
        };
        placeholder.setOnClickListener(v->load.run());
        if(loadAutomatically10001)load.run();
        else placeholder.setText("📷 Tap to load this photo");
    }
    private void addMoment(DocumentSnapshot d,LinearLayout feed){
        if(me==null)return;
        String uid=safe(d.getString("uid"),""),name=safe(d.getString("name"),"KING User"),text=safe(d.getString("text"),"");
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12),dp(10),dp(12),dp(10));card.setBackground(bg(CARD,14));
        card.addView(tv("● "+name,14,DARK,true));
        String topic=safe(d.getString("topic"),"General");
        TextView tag=tv("#"+topic,11,PURPLE,true);
        tag.setOnClickListener(v->{selectedTopic940=topic;render("Moments");});
        card.addView(tag,new LinearLayout.LayoutParams(-1,dp(30)));
        card.addView(tv(text,15,DARK,false));
        String photoUrl10001=d.getString("photoUrl");
        boolean autoPhoto10001=photoUrl10001!=null && photoUrl10001.startsWith("https://firebasestorage.googleapis.com/") && autoMomentPhotos10001-->0;
        showMomentPhoto9991(photoUrl10001,card,autoPhoto10001);
        String type=safe(d.getString("type"),""),roomId=safe(d.getString("roomId"),""),roomName=safe(d.getString("roomName"),"Live Party");
        if("room_broadcast".equals(type)&&!roomId.isEmpty()){
            final String momentRoom10020=roomId, momentAccount10020=me.getUid();
            TextView join=button("🎤 Join "+roomName,()->{
                if(db==null||isFinishing()||isDestroyed()||!"Moments".equals(selected)
                    ||me==null||!momentAccount10020.equals(me.getUid())
                    ||!KingSavedRoomsCloud9990.account(momentAccount10020)){
                    toast("Open Moments again to join this Party");return;
                }
                db.collection("live_rooms").document(momentRoom10020)
                    .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(live10020->{
                        if(isFinishing()||isDestroyed()||!"Moments".equals(selected)
                            ||me==null||!momentAccount10020.equals(me.getUid())
                            ||!KingSavedRoomsCloud9990.account(momentAccount10020))return;
                        if(!live10020.exists()||Boolean.TRUE.equals(live10020.getBoolean("closed"))){
                            toast("This Party room has ended");return;
                        }
                        Intent i=new Intent(this,PartyActivity.class);
                        i.putExtra("directRoomId",momentRoom10020);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(i);
                    }).addOnFailureListener(err->{
                        if(me!=null&&momentAccount10020.equals(me.getUid()))
                            toast("Could not check Party room. Try again.");
                    });
            });
            LinearLayout.LayoutParams jp=new LinearLayout.LayoutParams(-1,dp(44));jp.setMargins(0,dp(6),0,dp(6));card.addView(join,jp);
        }
        LinearLayout actions=new LinearLayout(this);
        TextView like=button("♡ Like",()->{});like.setEnabled(false);
        like.setOnClickListener(v->toggleMomentLike9990(d.getReference(),like));
        final String account=me.getUid();
        final int momentTicket10000=momentsGeneration9990;
        momentLikes10000.add(d.getReference().collection("likes").document(account)
          .addSnapshotListener((liked,error)->{
            if(momentTicket10000!=momentsGeneration9990 ||isFinishing()||isDestroyed()
                ||!"Moments".equals(selected)||!KingSavedRoomsCloud9990.account(account)
                ||like.getParent()==null)return;
            if(error!=null||liked==null){like.setText("Like unavailable");like.setEnabled(false);return;}
            if(!Boolean.TRUE.equals(like.getTag())){
                like.setText(liked.exists()?"♥ Liked":"♡ Like");like.setEnabled(true);
            }
          }));
        actions.addView(like,new LinearLayout.LayoutParams(0,dp(42),1));
        TextView other;
        if(!uid.equals(account))other=button("💬 Message",()->openChat(uid,name));
        else other=button("Delete",()->new AlertDialog.Builder(this)
            .setTitle("Delete this Moment?").setMessage("Remove this post from the shared feed?")
            .setNegativeButton("Cancel",null).setPositiveButton("Delete",(dialog,w)->
                deleteOwnedMoment10019(d.getReference(),account)).show());
        LinearLayout.LayoutParams op=new LinearLayout.LayoutParams(0,dp(42),1);op.setMargins(dp(6),0,0,0);
        actions.addView(other,op);
        TextView share10025=button("↗ Share",()->{
            if(isFinishing()||isDestroyed()||!"Moments".equals(selected)
                ||!KingSavedRoomsCloud9990.account(account))return;
            Intent shareIntent10025=new Intent(Intent.ACTION_SEND);
            shareIntent10025.setType("text/plain");
            shareIntent10025.putExtra(Intent.EXTRA_TEXT,
                "KING Plus Moment • "+name+" • #"+topic+"\n"+text+"\nShared from KING Plus");
            try{startActivity(Intent.createChooser(shareIntent10025,"Share KING Moment"));}
            catch(Exception err10025){toast("No sharing app available");}
        });
        LinearLayout.LayoutParams shareLp10025=new LinearLayout.LayoutParams(0,dp(42),1);
        shareLp10025.setMargins(dp(5),0,0,0);
        actions.addView(share10025,shareLp10025);card.addView(actions);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(6),0,dp(6));feed.addView(card,lp);
    }
    // Delete only the same live post owned by the current signed-in account.
    // A dialog left open while switching accounts must never delete the old post.
    private void deleteOwnedMoment10019(DocumentReference ref10019,String owner10019){
        if(ref10019==null||me==null||!owner10019.equals(me.getUid())
            ||!KingSavedRoomsCloud9990.account(owner10019)||!"Moments".equals(selected)){
            toast("Account changed. Reopen your Moment.");return;
        }
        db.runTransaction(tx10019->{
            DocumentSnapshot live10019=tx10019.get(ref10019);
            if(!live10019.exists()||!owner10019.equals(live10019.getString("uid")))
                throw new IllegalStateException("Moment changed or was already deleted");
            tx10019.delete(ref10019);return true;
        }).addOnSuccessListener(v->{
            if(me!=null&&owner10019.equals(me.getUid())&&"Moments".equals(selected)
                &&!isFinishing()&&!isDestroyed())toast("Moment deleted");
        }).addOnFailureListener(e->{
            if(me!=null&&owner10019.equals(me.getUid())&&!isFinishing()&&!isDestroyed())
                toast("Could not delete Moment: "+e.getMessage());
        });
    }
    private void toggleMomentLike9990(DocumentReference ref,TextView button){
        if(me==null||!KingSavedRoomsCloud9990.account(me.getUid()))return;
        button.setEnabled(false);button.setTag(Boolean.TRUE);String uid=me.getUid();DocumentReference like=ref.collection("likes").document(uid);
        db.runTransaction(tx->{
            DocumentSnapshot livePost10020=tx.get(ref);
            if(!livePost10020.exists())throw new IllegalStateException("Moment was deleted. Refresh your feed.");
            DocumentSnapshot existing=tx.get(like);
            if(existing.exists()){tx.delete(like);return false;}
            Map<String,Object> data=new HashMap<>();data.put("uid",uid);data.put("createdAt",FieldValue.serverTimestamp());tx.set(like,data);return true;
        }).addOnSuccessListener(liked->{
            if(me!=null&&uid.equals(me.getUid())&&"Moments".equals(selected)
                &&!isFinishing()&&!isDestroyed()&&button.getParent()!=null){
                button.setTag(null);button.setEnabled(true);button.setText(liked?"♥ Liked":"♡ Like");
            }
        }).addOnFailureListener(e->{
            if(me!=null&&uid.equals(me.getUid())&&"Moments".equals(selected)
                &&!isFinishing()&&!isDestroyed()&&button.getParent()!=null){
                button.setTag(null);button.setEnabled(true);toast("Could not update Like. Please retry.");
            }
        });
    }

    private String[] topicNames940(){return new String[]{"All","General","Friends","Music","Games","KTV","Ludo","Family","Love","PK","Newcomers"};}
    private void chooseTopic940(boolean returnToMoments){
        String[] topics=topicNames940();
        new AlertDialog.Builder(this).setTitle("🏷 Choose Topic").setItems(topics,(d,w)->{selectedTopic940=topics[w];if(returnToMoments)render("Moments");else render("Topics");}).setNegativeButton("Close",null).show();
    }
    private void topics940(){
        body.addView(tv("Topic Square",21,DARK,true));
        body.addView(tv("Topic counts are from the latest 100 Moments. Open a topic to load older posts too.",13,MUTED,false));
        if(!cloud()){body.addView(tv("Sign in to view Topic Square.",14,MUTED,false));return;}
        LinearLayout grid=new LinearLayout(this);grid.setOrientation(LinearLayout.VERTICAL);body.addView(grid,new LinearLayout.LayoutParams(-1,-2));
        final LinearLayout topicHost10002=body;
        final int topicGeneration10002=searchGeneration9994;
        final String topicAccount10002=me.getUid();
        db.collection("moments").orderBy("createdAt",Query.Direction.DESCENDING).limit(100).get().addOnSuccessListener(s->{
            if(!"Topics".equals(selected)||body!=topicHost10002||searchGeneration9994!=topicGeneration10002
                ||isFinishing()||isDestroyed()||!KingSavedRoomsCloud9990.account(topicAccount10002))return;
            Map<String,Integer> counts=new HashMap<>();for(String t:topicNames940())counts.put(t,0);
            int total=0;for(DocumentSnapshot d:s.getDocuments()){String topic=safe(d.getString("topic"),"General");counts.put(topic,counts.containsKey(topic)?counts.get(topic)+1:1);total++;}counts.put("All",total);
            String[] topics=topicNames940();for(String t:topics){TextView row=tv("#"+t+"   •   "+(counts.containsKey(t)?counts.get(t):0)+" posts",14,DARK,true);row.setGravity(Gravity.CENTER_VERTICAL);row.setBackground(bg(CARD,14));row.setOnClickListener(v->{selectedTopic940=t;render("Moments");});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(54));lp.setMargins(0,dp(4),0,dp(4));grid.addView(row,lp);}
        }).addOnFailureListener(e->{
            if("Topics".equals(selected)&&body==topicHost10002&&searchGeneration9994==topicGeneration10002
                &&!isFinishing()&&!isDestroyed()&&KingSavedRoomsCloud9990.account(topicAccount10002))
                topicHost10002.addView(tv("Topic Square unavailable right now.",14,MUTED,false));
        });
    }

    private void family(){
        body.addView(tv("Family & Agency",21,DARK,true));
        if(!cloud()){body.addView(tv("Sign in to use Family.",14,MUTED,false));return;}
        final int ticket10008=searchGeneration9994;
        final LinearLayout familyHost10008=body;
        final String familyAccount10008=me.getUid();
        db.collection("users").document(familyAccount10008)
            .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(u->{
                if(!currentSection10003("Family",ticket10008,familyHost10008,familyAccount10008))return;
                String code=safe(u.getString("familyCode"),"");
                if(!KingSixPages10038.validFamilyCode(code)){
                    if(!code.isEmpty())toast("Stored Family code is invalid; join again with a valid code");
                    familyJoinCreate();return;
                }
                db.collection("families").document(code).collection("members")
                    .document(familyAccount10008).get(com.google.firebase.firestore.Source.SERVER)
                    .addOnSuccessListener(m->{
                        if(!currentSection10003("Family",ticket10008,familyHost10008,familyAccount10008))return;
                        if(m.exists())showFamily(code);
                        else{clearFamily();familyJoinCreate();}
                    }).addOnFailureListener(e->{
                        if(currentSection10003("Family",ticket10008,familyHost10008,familyAccount10008))
                            familyError955("Family membership unavailable",e,()->render("Family"));
                    });
            }).addOnFailureListener(e->{
                if(currentSection10003("Family",ticket10008,familyHost10008,familyAccount10008))
                    familyError955("Family lookup unavailable",e,()->render("Family"));
            });
    }
    private AlertDialog familyLoading955(String text){LinearLayout box=new LinearLayout(this);box.setGravity(Gravity.CENTER_VERTICAL);box.setPadding(dp(18),dp(10),dp(18),dp(10));android.widget.ProgressBar spin=new android.widget.ProgressBar(this);box.addView(spin,new LinearLayout.LayoutParams(dp(34),dp(34)));TextView label=tv(text,13,MUTED,true);box.addView(label,new LinearLayout.LayoutParams(0,dp(52),1));AlertDialog d=new AlertDialog.Builder(this).setTitle("KING Family").setView(box).setCancelable(false).create();d.show();return d;}
    private void familyError955(String title,Exception e,Runnable retry){new AlertDialog.Builder(this).setTitle(title).setMessage(e==null?"Please try again.":String.valueOf(e.getMessage())).setPositiveButton("Retry",(d,w)->retry.run()).setNegativeButton("Close",null).show();}
    
    private void familyJoinCreate(){
        TextView intro955=tv("👑 Create or Join a KING Family",20,DARK,true);intro955.setGravity(Gravity.CENTER);intro955.setBackground(bg(0xffffefd0,18));body.addView(intro955,new LinearLayout.LayoutParams(-1,dp(60)));
        TextView note955=tv("Real live Family members, roles and chat. Lucky Bags are TEST-only; real Family treasury and level rewards are not yet enabled.",12,MUTED,false);note955.setGravity(Gravity.CENTER);note955.setPadding(dp(10),dp(6),dp(10),dp(8));body.addView(note955,new LinearLayout.LayoutParams(-1,dp(62)));

        body.addView(tv("Create Family",16,DARK,true));
        EditText name=new EditText(this);name.setHint("Family name • 3–24 characters");name.setSingleLine(true);name.setMaxLines(1);name.setBackground(bg(CARD,14));name.setPadding(dp(12),0,dp(12),0);body.addView(name,new LinearLayout.LayoutParams(-1,dp(52)));
        TextView create=button("👑 Create Family",()->{String n=name.getText().toString().trim();if(n.length()<3||n.length()>24){toast("Family name must be 3–24 characters");return;}new AlertDialog.Builder(this).setTitle("Create Family?").setMessage(n+"\n\nYou will become the Family owner.").setPositiveButton("Create",(d,w)->createFamily(n,0)).setNegativeButton("Cancel",null).show();});LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(48));cp.setMargins(0,dp(8),0,dp(18));body.addView(create,cp);

        body.addView(tv("Join with Family Code",16,DARK,true));
        EditText code=new EditText(this);code.setHint("6-character Family code");code.setSingleLine(true);code.setAllCaps(true);code.setBackground(bg(CARD,14));code.setPadding(dp(12),0,dp(12),0);body.addView(code,new LinearLayout.LayoutParams(-1,dp(52)));
        TextView join=button("Join Family",()->{String c=code.getText().toString().trim().toUpperCase(Locale.US);if(!KingSixPages10038.validFamilyCode(c)){toast("Enter a valid 6-character Family code (letters and 2-9)");return;}joinFamily(c);});LinearLayout.LayoutParams jp=new LinearLayout.LayoutParams(-1,dp(48));jp.setMargins(0,dp(8),0,0);body.addView(join,jp);
        TextView privacy955=tv("Only people with the exact Family code can join. The owner can remove members.",11,MUTED,false);privacy955.setGravity(Gravity.CENTER);LinearLayout.LayoutParams pp955=new LinearLayout.LayoutParams(-1,dp(52));pp955.setMargins(0,dp(8),0,0);body.addView(privacy955,pp955);
    }
    private String newCode(){
        String alphabet="ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        java.security.SecureRandom rng=new java.security.SecureRandom();
        StringBuilder code=new StringBuilder(6);
        for(int i=0;i<6;i++)code.append(alphabet.charAt(rng.nextInt(alphabet.length())));
        return code.toString();
    }
    private void createFamily(String name,int attempt){
        final String normalizedName956=name==null?"":name.trim();
        if(normalizedName956.length()<3||normalizedName956.length()>24){toast("Family name must be 3–24 characters");return;}
        if(me==null||db==null){toast("Sign in to create a Family");return;}
        if(attempt>5){new AlertDialog.Builder(this).setTitle("Could not create Family").setMessage("We could not reserve a unique Family code. Please try again.").setPositiveButton("Retry",(d,w)->createFamily(normalizedName956,0)).setNegativeButton("Close",null).show();return;}
        final String familyUid10009=me.getUid();
        final LinearLayout familyHost10009=body;
        final int familyGeneration10009=searchGeneration9994;
        final String familyName955=normalizedName956;
        final String code=newCode();
        final DocumentReference r=db.collection("families").document(code);
        final DocumentReference ownerRef10009=r.collection("members").document(familyUid10009);
        final AlertDialog loading955=familyLoading955("Creating "+familyName955+"…");
        Map<String,Object> f=new HashMap<>();f.put("name",familyName955);f.put("code",code);
        f.put("ownerUid",familyUid10009);f.put("ownerName",displayName);f.put("level",1);
        f.put("treasury",0);f.put("memberCount",1);f.put("createdAt",FieldValue.serverTimestamp());
        f.put("updatedAt",FieldValue.serverTimestamp());
        Map<String,Object> mem=new HashMap<>();mem.put("uid",familyUid10009);
        mem.put("name",displayName);mem.put("role","owner");mem.put("joinedAt",FieldValue.serverTimestamp());
        db.runTransaction(tx->{
            DocumentSnapshot previous10009=tx.get(r);
            if(previous10009.exists())throw new IllegalStateException("KING_FAMILY_CODE_COLLISION_10009");
            // Never reserve a Family without its owner membership or delete a
            // pre-existing Family after a failed separate membership write.
            tx.set(r,f);
            tx.set(ownerRef10009,mem);
            return null;
        }).addOnSuccessListener(done->{
            if(loading955.isShowing())loading955.dismiss();
            Map<String,Object> activity=new HashMap<>();activity.put("type","created");
            activity.put("text",displayName+" created the Family");activity.put("uid",familyUid10009);
            activity.put("createdAt",FieldValue.serverTimestamp());
            r.collection("activities").add(activity).addOnFailureListener(e->{});
            // Persist the Family link even if the user switched tabs while
            // the atomic creation was committing. Do not jump tabs afterward.
            saveCreatedFamily10009(familyUid10009,code,familyName955,
                familyGeneration10009,familyHost10009);
        }).addOnFailureListener(error->{
            if(loading955.isShowing())loading955.dismiss();
            if(!currentSection10003("Family",familyGeneration10009,familyHost10009,familyUid10009))return;
            if(KingMeHomePartyGuard10009.isFamilyCodeCollision(error==null?null:error.getMessage()))
                createFamily(familyName955,attempt+1);
            else familyError955("Family creation failed",error,()->createFamily(familyName955,0));
        });
    }
    private void saveCreatedFamily10009(String ownerUid,String code,String name,
                                        int generation,LinearLayout host){
        Map<String,Object> link=new HashMap<>();link.put("familyCode",code);
        link.put("familyName",name);
        db.collection("users").document(ownerUid).set(link,SetOptions.merge())
            .addOnSuccessListener(v->{
                if(KingSavedRoomsCloud9990.account(ownerUid)){
                    getSharedPreferences("MainActivity",MODE_PRIVATE).edit()
                        .putString("family_code",code).putString("family_name",name).apply();
                    if(currentSection10003("Family",generation,host,ownerUid))render("Family");
                }
            }).addOnFailureListener(error->{
                if(currentSection10003("Family",generation,host,ownerUid))
                    familyError955("Family created, but profile link failed",error,
                        ()->saveCreatedFamily10009(ownerUid,code,name,generation,host));
            });
    }
    private void joinFamily(String code){
        code=code==null?"":code.trim().toUpperCase(Locale.US);
        if(!KingSixPages10038.validFamilyCode(code)){
            toast("Family code is invalid; check the six letters/numbers");return;
        }
        if(me==null||db==null){toast("Sign in to join a Family");return;}
        final String familyCode955=code;DocumentReference r=db.collection("families").document(familyCode955);final AlertDialog loading955=familyLoading955("Checking Family "+familyCode955+"…");
        r.get().addOnSuccessListener(f->{
            if(!f.exists()){if(loading955.isShowing())loading955.dismiss();new AlertDialog.Builder(this).setTitle("Family not found").setMessage("Check the 6-character code and try again.").setPositiveButton("OK",null).show();return;}
            String familyName955=safe(f.getString("name"),"KING Family"),ownerUid955=safe(f.getString("ownerUid"),"");
            if(me.getUid().equals(ownerUid955)){if(loading955.isShowing())loading955.dismiss();saveFamily(familyCode955,familyName955);return;}
            final DocumentReference membership9986=r.collection("members").document(me.getUid());
            membership9986.get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(existing9986->{
                    if(existing9986.exists()){
                        if(loading955.isShowing())loading955.dismiss();
                        saveFamily(familyCode955,familyName955);
                        return;
                    }
                    Map<String,Object> member956=new HashMap<>();
                    member956.put("uid",me.getUid());member956.put("name",displayName);
                    member956.put("role","member");
                    member956.put("joinedAt",FieldValue.serverTimestamp());
                    Map<String,Object> ownerLink10010=new HashMap<>();
                    ownerLink10010.put("familyCode",familyCode955);
                    ownerLink10010.put("familyName",familyName955);
                    final String joiningAccount10010=me.getUid();
                    final LinearLayout joiningHost10010=body;
                    final int joiningGeneration10010=searchGeneration9994;
                    com.google.firebase.firestore.WriteBatch join10010=db.batch();
                    join10010.set(membership9986,member956);
                    join10010.set(db.collection("users").document(joiningAccount10010),
                        ownerLink10010,SetOptions.merge());
                    join10010.commit().addOnSuccessListener(v->{
                        if(loading955.isShowing())loading955.dismiss();
                        if(!KingSavedRoomsCloud9990.account(joiningAccount10010))return;
                        getSharedPreferences("MainActivity",MODE_PRIVATE).edit()
                            .putString("family_code",familyCode955)
                            .putString("family_name",familyName955).apply();
                        if(currentSection10003("Family",joiningGeneration10010,
                            joiningHost10010,joiningAccount10010))render("Family");
                    }).addOnFailureListener(e->{if(loading955.isShowing())loading955.dismiss();
                        if(KingSavedRoomsCloud9990.account(joiningAccount10010))
                            familyError955("Could not join Family",e,()->joinFamily(familyCode955));});
                }).addOnFailureListener(e->{
                    if(loading955.isShowing())loading955.dismiss();
                    familyError955("Could not verify Family membership",e,()->joinFamily(familyCode955));
                });
        }).addOnFailureListener(e->{if(loading955.isShowing())loading955.dismiss();familyError955("Family lookup failed",e,()->joinFamily(familyCode955));});
    }
    private void saveFamily(String code,String name){
        if(me==null||db==null||!KingSavedRoomsCloud9990.account(me.getUid()))return;
        final String owner10046=me.getUid();
        final LinearLayout host10046=body;
        final int generation10046=searchGeneration9994;
        Map<String,Object>u=new HashMap<>();u.put("familyCode",code);u.put("familyName",name);
        db.collection("users").document(owner10046).set(u,SetOptions.merge())
            .addOnSuccessListener(v->{
                if(!KingSavedRoomsCloud9990.account(owner10046))return;
                getSharedPreferences("MainActivity",MODE_PRIVATE).edit()
                    .putString("family_code",code).putString("family_name",name).apply();
                if(currentSection10003("Family",generation10046,host10046,owner10046))render("Family");
            }).addOnFailureListener(e->{
                if(currentSection10003("Family",generation10046,host10046,owner10046))
                    familyError955("Could not save Family",e,()->saveFamily(code,name));
            });
    }
    private void leaveJoinedFamily10010(String familyCode,DocumentReference familyRef){
        if(me==null||db==null)return;
        final String leavingUid10010=me.getUid();
        final LinearLayout leavingHost10010=body;
        final int leavingGeneration10010=searchGeneration9994;
        Map<String,Object> unlink10010=new HashMap<>();
        unlink10010.put("familyCode",FieldValue.delete());
        unlink10010.put("familyName",FieldValue.delete());
        com.google.firebase.firestore.WriteBatch batch10010=db.batch();
        batch10010.delete(familyRef.collection("members").document(leavingUid10010));
        batch10010.set(db.collection("users").document(leavingUid10010),
            unlink10010,SetOptions.merge());
        batch10010.commit().addOnSuccessListener(v->{
            if(!KingSavedRoomsCloud9990.account(leavingUid10010))return;
            getSharedPreferences("MainActivity",MODE_PRIVATE).edit()
                .remove("family_code").remove("family_name").apply();
            if(currentSection10003("Family",leavingGeneration10010,
                leavingHost10010,leavingUid10010))render("Family");
        }).addOnFailureListener(error->{
            if(currentSection10003("Family",leavingGeneration10010,
                leavingHost10010,leavingUid10010))
                familyError955("Could not leave Family",error,
                    ()->leaveJoinedFamily10010(familyCode,familyRef));
        });
    }
    private void clearFamily(){
        if(me==null||db==null||!KingSavedRoomsCloud9990.account(me.getUid()))return;
        final String owner10046=me.getUid();
        // Leaving an invalid Family may need local UI clearing immediately,
        // but a delayed response from the old account cannot touch new prefs.
        getSharedPreferences("MainActivity",MODE_PRIVATE).edit()
            .remove("family_code").remove("family_name").apply();
        Map<String,Object>u=new HashMap<>();u.put("familyCode",FieldValue.delete());u.put("familyName",FieldValue.delete());
        db.collection("users").document(owner10046).set(u,SetOptions.merge());
    }
    private void showFamily(String code){
        final LinearLayout familyBody956=body;
        DocumentReference r=db.collection("families").document(code);
        r.get().addOnSuccessListener(f->{
            if(body!=familyBody956||familyStopped956||isFinishing()||isDestroyed())return;
            if(!f.exists()){clearFamily();render("Family");return;}
            String name=safe(f.getString("name"),"KING Family"),owner=safe(f.getString("ownerUid"),"");
            Long levelRaw=f.getLong("level"),treasuryRaw=f.getLong("treasury");long familyLevel=levelRaw==null?1:Math.max(1,levelRaw),treasury=treasuryRaw==null?0:Math.max(0,treasuryRaw);

            LinearLayout hero951=new LinearLayout(this);hero951.setOrientation(LinearLayout.VERTICAL);hero951.setGravity(Gravity.CENTER);hero951.setPadding(dp(12),dp(10),dp(12),dp(10));hero951.setBackground(bg(0xffffefd0,18));
            TextView familyName951=tv("👑 "+name,21,DARK,true);familyName951.setGravity(Gravity.CENTER);hero951.addView(familyName951,new LinearLayout.LayoutParams(-1,dp(36)));
            TextView code951=tv("Family Code  "+code,12,MUTED,true);code951.setGravity(Gravity.CENTER);hero951.addView(code951,new LinearLayout.LayoutParams(-1,dp(28)));body.addView(hero951,new LinearLayout.LayoutParams(-1,dp(82)));

            LinearLayout stats951=new LinearLayout(this);stats951.setGravity(Gravity.CENTER);TextView memberStat951=familyStat951("👥","…","Members");TextView levelStat951=familyStat951("⭐",String.valueOf(familyLevel),"Level");TextView treasuryStat951=familyStat951("◇","—","Unverified");TextView activityStat951=familyStat951("🎉","…","Activity");stats951.addView(memberStat951,new LinearLayout.LayoutParams(0,dp(76),1));stats951.addView(levelStat951,new LinearLayout.LayoutParams(0,dp(76),1));stats951.addView(treasuryStat951,new LinearLayout.LayoutParams(0,dp(76),1));stats951.addView(activityStat951,new LinearLayout.LayoutParams(0,dp(76),1));LinearLayout.LayoutParams statLp951=new LinearLayout.LayoutParams(-1,dp(82));statLp951.setMargins(0,dp(8),0,dp(8));body.addView(stats951,statLp951);

            LinearLayout acts=new LinearLayout(this);TextView share=button("Share Code",()->{Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,"Join my KING Plus Family "+name+" • Code "+code);startActivity(Intent.createChooser(i,"Share Family"));});acts.addView(share,new LinearLayout.LayoutParams(0,dp(46),1));if(!owner.equals(me.getUid())){TextView leave=button("Leave",()->new AlertDialog.Builder(this).setTitle("Leave "+name+"?").setMessage("You can rejoin later with the Family code.").setPositiveButton("Leave",(d,w)->leaveJoinedFamily10010(code,r)).setNegativeButton("Cancel",null).show());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(46),1);lp.setMargins(dp(6),0,0,0);acts.addView(leave,lp);}body.addView(acts);
            if(owner.equals(me.getUid())){
                TextView rename9987=button("✏ Edit Family Name",()->{
                    EditText renameInput9987=new EditText(this);
                    renameInput9987.setSingleLine(true);
                    renameInput9987.setText(name);
                    new AlertDialog.Builder(this).setTitle("Rename Family")
                        .setView(renameInput9987)
                        .setPositiveButton("Save",(dialog,which)->{
                            String renamed=renameInput9987.getText().toString().trim();
                            if(renamed.length()<2||renamed.length()>80){
                                toast("Family name: 2–80 characters");return;
                            }
                            Map<String,Object> updates=new HashMap<>();
                            updates.put("name",renamed);
                            updates.put("updatedAt",FieldValue.serverTimestamp());
                            r.update(updates).addOnSuccessListener(v->render("Family"))
                                .addOnFailureListener(e->toast("Family rename rejected"));
                        }).setNegativeButton("Cancel",null).show();
                });
                body.addView(rename9987,new LinearLayout.LayoutParams(-1,dp(46)));
            }
            TextView luckyBag951=button("🧧 Family Lucky Bag",()->familyLuckyBag940(code));LinearLayout.LayoutParams lbp951=new LinearLayout.LayoutParams(-1,dp(48));lbp951.setMargins(0,dp(8),0,dp(8));body.addView(luckyBag951,lbp951);

            body.addView(tv("Members",16,DARK,true));LinearLayout memberList951=new LinearLayout(this);memberList951.setOrientation(LinearLayout.VERTICAL);body.addView(memberList951,new LinearLayout.LayoutParams(-1,-2));
            EditText memberSearch956=new EditText(this);memberSearch956.setHint("Search Family members");memberSearch956.setSingleLine(true);memberSearch956.setEnabled(false);body.addView(memberSearch956,body.indexOfChild(memberList951),new LinearLayout.LayoutParams(-1,dp(48)));
            memberSearch956.addTextChangedListener(new android.text.TextWatcher(){
                public void beforeTextChanged(CharSequence s,int start,int count,int after){}
                public void onTextChanged(CharSequence s,int start,int before,int count){filterFamilyMembers956(memberList951,s.toString());}
                public void afterTextChanged(android.text.Editable s){}
            });
            attachLiveFamilyMembers9986(r,memberList951,memberStat951,memberSearch956,
                owner,code,familyBody956);

            r.collection("activities").orderBy("createdAt",Query.Direction.DESCENDING).limit(50).get()
                .addOnSuccessListener(a->{if(body==familyBody956 && !familyStopped956 && "Family".equals(selected))
                    activityStat951.setText("🎉\n"+a.size()+"\nActivity");})
                .addOnFailureListener(e->{if(body==familyBody956 && !familyStopped956 && "Family".equals(selected))
                    activityStat951.setText("🎉\n—\nActivity");});

            body.addView(tv("Family Chat",16,DARK,true));LinearLayout chat=new LinearLayout(this);chat.setOrientation(LinearLayout.VERTICAL);TextView chatLoading951=tv("Loading family messages…",12,MUTED,false);chat.addView(chatLoading951,new LinearLayout.LayoutParams(-1,dp(42)));body.addView(chat);
            // Do NOT detach familyMembers9986 here. Old code cancelled the
            // real-time member listener just after attaching it.
            stopFamilyChatOnly9987();
            if(!familyStopped956&&!isFinishing()&&!isDestroyed()&&"Family".equals(selected))familyMessages956=r.collection("messages").orderBy("createdAt",Query.Direction.DESCENDING).limit(30).addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(snapshot,error)->{
                FirebaseUser real10039=FirebaseAuth.getInstance().getCurrentUser();
                if(body!=familyBody956||familyStopped956||isFinishing()||isDestroyed()||!"Family".equals(selected)
                   ||me==null||real10039==null||!real10039.getUid().equals(me.getUid()))return;
                chat.removeAllViews();
                if(error!=null||snapshot==null){TextView retry10040=tv("Family Chat unavailable • tap to retry",13,MUTED,true);
                    retry10040.setOnClickListener(v->{if(body==familyBody956 && "Family".equals(selected))render("Family");});
                    chat.addView(retry10040);return;}
                if(snapshot.getMetadata().isFromCache()){
                    chat.addView(tv("Offline • cached Family messages, waiting for server",12,MUTED,true));
                }
                List<DocumentSnapshot> messages956=new ArrayList<>(snapshot.getDocuments());Collections.reverse(messages956);
                if(messages956.isEmpty())chat.addView(tv("No Family messages yet. Say hello 👋",13,MUTED,false));
                for(DocumentSnapshot message956:messages956){
                    String senderUid9987=safe(message956.getString("uid"),"");
                    TextView messageView9987=tv(
                        safe(message956.getString("name"),"User")+": "+
                        KingSixPages10040.safePreview(message956.getString("text"),1000)
                        +(message956.getMetadata().hasPendingWrites()?"  • Sending…":""),13,DARK,false);
                    chat.addView(messageView9987);
                    if(!snapshot.getMetadata().isFromCache()&&!snapshot.getMetadata().hasPendingWrites()
                        &&!message956.getMetadata().hasPendingWrites()
                        &&KingFamilyModeration9987.canModerateMessage(
                        me.getUid(),owner,realFamilyRole9987,senderUid9987)){
                        messageView9987.setOnLongClickListener(v->{
                            new AlertDialog.Builder(this).setTitle("Moderate Family Chat")
                                .setMessage("Delete this message for all Family members?")
                                .setPositiveButton("Delete",(dialog,which)->message956.getReference()
                                    .delete().addOnFailureListener(deleteFailure9987->toast("Message deletion denied")))
                                .setNegativeButton("Cancel",null).show();
                            return true;
                        });
                    }
                }
            });
            LinearLayout comp=new LinearLayout(this);EditText msg=new EditText(this);msg.setHint("Message family…");msg.setSingleLine(true);msg.setBackground(bg(CARD,14));comp.addView(msg,new LinearLayout.LayoutParams(0,dp(48),1));final boolean[] sending10033={false};final String chatOwner10033=me.getUid();
            final String[] retryText10043={null},retryId10043={null};
            final TextView[] sendButton10033={null};
            TextView send=button("Send",()->{
                if(sending10033[0]||me==null||!chatOwner10033.equals(me.getUid()))return;
                FirebaseUser signed10033=FirebaseAuth.getInstance().getCurrentUser();
                if(signed10033==null||!chatOwner10033.equals(signed10033.getUid()))return;
                String t=msg.getText().toString().trim();if(t.isEmpty())return;
                if(t.length()>1000){toast("Family messages can contain up to 1000 characters");return;}
                sending10033[0]=true;sendButton10033[0].setEnabled(false);sendButton10033[0].setText("Sending…");
                Map<String,Object>m=new HashMap<>();m.put("uid",chatOwner10033);m.put("name",displayName);
                m.put("text",t);m.put("createdAt",FieldValue.serverTimestamp());
                final String sendId10042;
                if(t.equals(retryText10043[0])&&retryId10043[0]!=null)sendId10042=retryId10043[0];
                else{sendId10042="msg_"+java.util.UUID.randomUUID().toString().replace("-","");
                    retryText10043[0]=t;retryId10043[0]=sendId10042;}
                r.collection("messages").document(sendId10042).set(m).addOnSuccessListener(v->{
                    sending10033[0]=false;
                    if(sendId10042.equals(retryId10043[0])){retryId10043[0]=null;retryText10043[0]=null;}
                    FirebaseUser now10035=FirebaseAuth.getInstance().getCurrentUser();
                    if(isFinishing()||isDestroyed()||now10035==null||!chatOwner10033.equals(now10035.getUid())
                        ||body!=familyBody956||!"Family".equals(selected)||sendButton10033[0].getParent()!=comp)return;
                    if(msg.getText().toString().trim().equals(t))msg.setText("");
                    sendButton10033[0].setEnabled(true);sendButton10033[0].setText("Send");
                }).addOnFailureListener(e->{
                    // An ACK failure may still mean the write succeeded. Keep
                    // Send locked until exact document verification finishes.
                    FirebaseUser now10035=FirebaseAuth.getInstance().getCurrentUser();
                    if(isFinishing()||isDestroyed()||now10035==null||!chatOwner10033.equals(now10035.getUid())
                        ||body!=familyBody956||!"Family".equals(selected)||sendButton10033[0].getParent()!=comp)return;
                    sendButton10033[0].setText("Checking delivery…");
                    r.collection("messages").document(sendId10042).get(com.google.firebase.firestore.Source.SERVER)
                        .addOnSuccessListener(check->{
                            FirebaseUser verifying10047=FirebaseAuth.getInstance().getCurrentUser();
                            if(verifying10047==null||!chatOwner10033.equals(verifying10047.getUid())
                                ||body!=familyBody956||familyStopped956||!"Family".equals(selected))return;
                            sending10033[0]=false;
                            if(sendButton10033[0].getParent()==comp){
                                sendButton10033[0].setEnabled(true);sendButton10033[0].setText("Send");}
                            if(check.exists()){
                                if(sendId10042.equals(retryId10043[0])){retryId10043[0]=null;retryText10043[0]=null;}
                                if(msg.getText().toString().trim().equals(t))msg.setText("");
                                toast("Message sent • delivery confirmed");
                            }else toast("Message failed • tap Send to retry");
                        }).addOnFailureListener(error->{
                            FirebaseUser verifying10047=FirebaseAuth.getInstance().getCurrentUser();
                            if(verifying10047==null||!chatOwner10033.equals(verifying10047.getUid())
                                ||body!=familyBody956||familyStopped956||!"Family".equals(selected))return;
                            sending10033[0]=false;
                            if(sendButton10033[0].getParent()==comp){
                                sendButton10033[0].setEnabled(true);sendButton10033[0].setText("Retry Send");}
                            toast("Delivery uncertain • check chat before retrying");
                        });});
            });sendButton10033[0]=send;LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(dp(72),dp(48));sp.setMargins(dp(6),0,0,0);comp.addView(send,sp);body.addView(comp);
        }).addOnFailureListener(e->body.addView(tv("Family unavailable • pull back and retry",14,MUTED,false)));
    }

    private void attachLiveFamilyMembers9986(
        DocumentReference family,LinearLayout list,TextView total,EditText search,
        String ownerUid,String code,LinearLayout originalBody){
        if(familyMembers9986!=null)familyMembers9986.remove();
        familyMembers9986=family.collection("members").addSnapshotListener((snapshot,error)->{
            if(body!=originalBody||familyStopped956||isFinishing()||isDestroyed()
                ||!"Family".equals(selected)||me==null)return;
            list.removeAllViews();
            if(error!=null||snapshot==null){
                total.setText("👥\\n—\\nMembers");
                list.addView(tv("Cannot refresh Family members. Reopen Family.",13,MUTED,false));
                return;
            }
            boolean ownMembership=false;
            realFamilyRole9987="member";
            for(DocumentSnapshot member:snapshot.getDocuments()){
                if(me.getUid().equals(member.getId())){
                    ownMembership=true;
                    realFamilyRole9987=safe(member.getString("role"),"member");
                }
            }
            if(!ownMembership){
                // When removed by Owner on another phone, stop displaying stale
                // chat and revoke local Family shortcut without deleting history.
                stopFamilyMessages956();clearFamily();render("Family");return;
            }
            total.setText("👥\\n"+snapshot.size()+"\\nMembers");
            search.setEnabled(true);
            if(snapshot.isEmpty())list.addView(tv("No Family members",13,MUTED,false));
            for(DocumentSnapshot member:snapshot.getDocuments()){
                final String memberUid=member.getId();
                final String memberName=safe(member.getString("name"),"KING User");
                final String role=safe(member.getString("role"),"member");
                TextView label=tv(KingMeFamilyRoles9986.badge(role)+" • "+memberName,
                    14,DARK,true);
                list.addView(label,new LinearLayout.LayoutParams(-1,dp(44)));
                if(KingMeFamilyRoles9986.mayChange(me.getUid(),ownerUid,memberUid,"member")){
                    label.setOnLongClickListener(v->{
                        showFamilyRoleOptions9986(family,memberUid,memberName,ownerUid);
                        return true;
                    });
                }else if(KingFamilyModeration9987.canRemoveMember(me.getUid(),ownerUid,
                        realFamilyRole9987,memberUid,role)){
                    label.setOnLongClickListener(v->{
                        new AlertDialog.Builder(this).setTitle("Admin • Remove Member")
                            .setMessage("Remove "+memberName+" from the Family? Admins cannot remove moderators, admins or owners.")
                            .setPositiveButton("Remove",(d,w)->family.collection("members").document(memberUid)
                                .delete().addOnFailureListener(e->toast("Family admin removal denied")))
                            .setNegativeButton("Cancel",null).show();
                        return true;
                    });
                }
            }
            filterFamilyMembers956(list,search.getText().toString());
        });
    }
    private void showFamilyRoleOptions9986(
        DocumentReference family,String memberUid,String name,String ownerUid){
        if(me==null||!KingMeFamilyRoles9986.mayChange(
            me.getUid(),ownerUid,memberUid,"member"))return;
        String[] options={"🛡 Make Admin","⭐ Make Moderator","👤 Regular Member","🚫 Remove from Family"};
        new AlertDialog.Builder(this).setTitle("Manage "+name)
            .setItems(options,(dialog,index)->{
                if(me==null||!me.getUid().equals(ownerUid))return;
                final DocumentReference member=family.collection("members").document(memberUid);
                if(index==3){
                    new AlertDialog.Builder(this).setTitle("Remove Family member?")
                        .setMessage(name+" will immediately lose Family Chat and access.")
                        .setPositiveButton("Remove",(d,w)->member.delete()
                            .addOnFailureListener(e->familyError955("Could not remove Family member",e,()->render("Family"))))
                        .setNegativeButton("Cancel",null).show();
                    return;
                }
                String role=index==0?"admin":index==1?"moderator":"member";
                if(!KingMeFamilyRoles9986.mayChange(me.getUid(),ownerUid,memberUid,role))return;
                member.update("role",role)
                    .addOnFailureListener(e->familyError955(
                        "Family role change rejected",e,()->render("Family")));
            }).setNegativeButton("Cancel",null).show();
    }

    private void filterFamilyMembers956(LinearLayout list,String query){
        String q=query.trim().toLowerCase(Locale.ROOT);
        for(int i=0;i<list.getChildCount();i++){View child=list.getChildAt(i);if(child instanceof TextView)child.setVisibility(((TextView)child).getText().toString().toLowerCase(Locale.ROOT).contains(q)?View.VISIBLE:View.GONE);}
    }
    private TextView familyStat951(String icon,String value,String label){TextView t=tv(icon+"\n"+value+"\n"+label,11,DARK,true);t.setGravity(Gravity.CENTER);t.setBackground(bg(CARD,13));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(72),1);lp.setMargins(dp(2),0,dp(2),0);return t;}

    private int familyBagReward940(String packetId,int total,int slots){
        String key=packetId+":"+me.getUid();int h=key.hashCode()&0x7fffffff;
        int avg=Math.max(1,total/Math.max(1,slots));int low=Math.max(1,avg/2),high=Math.max(low,avg+Math.max(1,avg/2));
        return low+(h%Math.max(1,high-low+1));
    }
    private void familyLuckyBag940(String code){
        if(!cloud()||code==null||code.isEmpty()){toast("Family sign-in required");return;}
        final DocumentReference family=db.collection("families").document(code);
        family.collection("activities").orderBy("createdAt",Query.Direction.DESCENDING).limit(100).get().addOnSuccessListener(s->{
            DocumentSnapshot active=null;long now=System.currentTimeMillis();
            for(DocumentSnapshot d:s.getDocuments()){
                if(!"lucky_bag".equals(d.getString("type")))continue;
                Long exp=d.getLong("expiresAtMs");if(exp!=null&&exp>now){active=d;break;}
            }
            if(active==null){
                new AlertDialog.Builder(this).setTitle("🧧 Family Lucky Bag").setMessage("No active Lucky Bag.\n\nRewards are TEST coins only and have no cash value.")
                    .setPositiveButton("Create Lucky Bag",(d,w)->createFamilyLuckyBag940(code)).setNegativeButton("Close",null).show();return;
            }
            String pid=safe(active.getString("packetId"),active.getId());Long totalRaw=active.getLong("totalCoins"),slotsRaw=active.getLong("slots");int total=totalRaw==null?0:totalRaw.intValue(),slots=slotsRaw==null?1:Math.max(1,slotsRaw.intValue());
            int claimed=0;boolean mine=false;for(DocumentSnapshot d:s.getDocuments()){if(!pid.equals(d.getString("packetId"))||!"lucky_bag_claim".equals(d.getString("type")))continue;claimed++;if(me.getUid().equals(d.getString("uid")))mine=true;}
            int left=Math.max(0,slots-claimed);String sender=safe(active.getString("name"),"Family member");
            AlertDialog.Builder b=new AlertDialog.Builder(this).setTitle("🧧 Family Lucky Bag").setMessage("From: "+sender+"\nTEST coins: "+total+"\nClaims left: "+left+"/"+slots+"\n\nNo real money or billing.");
            if(!mine&&left>0)b.setPositiveButton("Open",(d,w)->claimFamilyLuckyBag940(code,pid,total,slots));
            else b.setPositiveButton(mine?"Already opened":"Finished",null);
            b.setNeutralButton("Create new",(d,w)->createFamilyLuckyBag940(code)).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast("Lucky Bag unavailable"));
    }
    private void createFamilyLuckyBag940(String code){
        final EditText amount=new EditText(this);amount.setHint("Total TEST coins (10–1000)");amount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        final EditText slots=new EditText(this);slots.setHint("Claims (1–20)");slots.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(22),0,dp(22),0);box.addView(amount,new LinearLayout.LayoutParams(-1,dp(54)));box.addView(slots,new LinearLayout.LayoutParams(-1,dp(54)));
        new AlertDialog.Builder(this).setTitle("Create Family Lucky Bag").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Create",(d,w)->{
            int total,n;try{total=Integer.parseInt(amount.getText().toString().trim());n=Integer.parseInt(slots.getText().toString().trim());}catch(Exception e){toast("Enter valid numbers");return;}
            if(total<10||total>1000||n<1||n>20||total<n){toast("Use 10–1000 coins and 1–20 claims");return;}
            String pid=String.valueOf(System.currentTimeMillis())+"_"+me.getUid().substring(0,Math.min(6,me.getUid().length()));
            Map<String,Object>x=new HashMap<>();x.put("uid",me.getUid());x.put("name",displayName);x.put("type","lucky_bag");x.put("packetId",pid);x.put("totalCoins",total);x.put("slots",n);x.put("expiresAtMs",System.currentTimeMillis()+300000L);x.put("createdAt",FieldValue.serverTimestamp());
            db.collection("families").document(code).collection("activities").document("bag_"+pid).set(x).addOnSuccessListener(v->{toast("Family Lucky Bag created");familyLuckyBag940(code);}).addOnFailureListener(e->toast("Could not create Lucky Bag"));
        }).show();
    }
    private void claimFamilyLuckyBag940(String code,String packetId,int total,int slots){
        DocumentReference activities=db.collection("families").document(code).collection("activities").document("claim_"+packetId+"_"+me.getUid());
        activities.get().addOnSuccessListener(existing->{
            if(existing.exists()){toast("You already opened this Lucky Bag");return;}
            db.collection("families").document(code).collection("activities").orderBy("createdAt",Query.Direction.DESCENDING).limit(100).get().addOnSuccessListener(s->{
                int claimed=0;for(DocumentSnapshot d:s.getDocuments())if(packetId.equals(d.getString("packetId"))&&"lucky_bag_claim".equals(d.getString("type")))claimed++;
                if(claimed>=slots){toast("Lucky Bag is finished");return;}
                int reward=familyBagReward940(packetId,total,slots);Map<String,Object>x=new HashMap<>();x.put("uid",me.getUid());x.put("name",displayName);x.put("type","lucky_bag_claim");x.put("packetId",packetId);x.put("reward",reward);x.put("createdAt",FieldValue.serverTimestamp());
                activities.set(x).addOnSuccessListener(v->{SharedPreferences p=getSharedPreferences("MainActivity",MODE_PRIVATE);int coins=p.getInt("coins",2500);p.edit().putInt("coins",coins+reward).apply();toast("Lucky Bag +"+reward+" TEST coins");}).addOnFailureListener(e->toast("Lucky Bag claim failed"));
            }).addOnFailureListener(e->toast("Could not check Lucky Bag"));
        }).addOnFailureListener(e->toast("Could not check your claim"));
    }

    private void relationships(){
        body.addView(tv("Relationship / CP",21,DARK,true));
        TextView intro952=tv("Build a mutual KING connection • requests, private chat and shared profile identity",12,MUTED,false);intro952.setPadding(0,0,0,dp(8));body.addView(intro952,new LinearLayout.LayoutParams(-1,dp(46)));
        if(!cloud()){body.addView(tv("Sign in to use Relationship.",14,MUTED,false));return;}

        LinearLayout quick952=new LinearLayout(this);quick952.setGravity(Gravity.CENTER);
        TextView find952=button("🔎 Find People",()->render("Search"));TextView moments952=button("▣ Square",()->render("Moments"));
        LinearLayout.LayoutParams qp952=new LinearLayout.LayoutParams(0,dp(46),1);qp952.setMargins(dp(2),0,dp(2),0);quick952.addView(find952,qp952);quick952.addView(moments952,new LinearLayout.LayoutParams(qp952));body.addView(quick952,new LinearLayout.LayoutParams(-1,dp(52)));

        if(!targetUid.isEmpty()&&!targetUid.equals(me.getUid())){
            LinearLayout selected952=new LinearLayout(this);selected952.setGravity(Gravity.CENTER_VERTICAL);selected952.setPadding(dp(12),dp(8),dp(12),dp(8));selected952.setBackground(bg(0xffffedf5,16));
            TextView who952=tv("💞  "+safe(targetName,"KING User"),15,DARK,true);selected952.addView(who952,new LinearLayout.LayoutParams(0,dp(46),1));
            TextView req952=button("Send Request",()->sendRelationship(targetUid,safe(targetName,"KING User")));selected952.addView(req952,new LinearLayout.LayoutParams(dp(126),dp(44)));LinearLayout.LayoutParams slp952=new LinearLayout.LayoutParams(-1,dp(58));slp952.setMargins(0,dp(6),0,dp(8));body.addView(selected952,slp952);
        }

        LinearLayout cpActions10017=new LinearLayout(this);
        cpActions10017.setGravity(Gravity.CENTER_VERTICAL);
        final String cpRefreshAccount10017=me.getUid();
        TextView refreshCp10017=button("↻ Refresh CP & Requests",()->{
            if(me!=null&&cpRefreshAccount10017.equals(me.getUid())
                &&"Relationship".equals(selected)&&!isFinishing()&&!isDestroyed())
                render("Relationship");
        });
        cpActions10017.addView(refreshCp10017,new LinearLayout.LayoutParams(-1,dp(46)));
        body.addView(cpActions10017,new LinearLayout.LayoutParams(-1,dp(54)));
        body.addView(tv("Your Relationship Status",16,DARK,true));
        TextView loading952=tv("Loading requests and CP connections…",13,MUTED,true);loading952.setGravity(Gravity.CENTER);loading952.setBackground(bg(CARD,14));body.addView(loading952,new LinearLayout.LayoutParams(-1,dp(58)));
        Set<String> seen10016=new HashSet<>();
        final int[] accepted10016={0},pending10016={0};
        final int cpTicket10016=searchGeneration9994;
        final LinearLayout cpHost10016=body;
        final String cpAccount10016=me.getUid();
        final Runnable updateCounts10016=()->{
            if(!currentSection10003("Relationship",cpTicket10016,cpHost10016,cpAccount10016))return;
            loading952.setText("💞 Loaded CP / Friends " + accepted10016[0] +
                "     ⏳ Pending " + pending10016[0]);
        };
        // Both directions use their own server cursor; requests after the first 30
        // no longer disappear, and switching tabs invalidates outstanding callbacks.
        loadRelationshipPage10016("uidA",cpAccount10016,cpTicket10016,cpHost10016,
            seen10016,accepted10016,pending10016,loading952,updateCounts10016,null,
            ()->loadRelationshipPage10016("uidB",cpAccount10016,cpTicket10016,cpHost10016,
                seen10016,accepted10016,pending10016,loading952,updateCounts10016,null,
                ()->{
                    if(currentSection10003("Relationship",cpTicket10016,cpHost10016,cpAccount10016)
                        &&seen10016.isEmpty()){
                        TextView empty10016=tv("No relationship requests yet.\nFind people and send a request.",13,MUTED,false);
                        empty10016.setGravity(Gravity.CENTER);
                        cpHost10016.addView(empty10016,new LinearLayout.LayoutParams(-1,dp(88)));
                    }
                }));
    }
    private void loadRelationshipPage10016(String field10016,String account10016,int token10016,
            LinearLayout host10016,Set<String>seen10016,int[]accepted10016,int[]pending10016,
            TextView summary10016,Runnable refresh10016,DocumentSnapshot cursor10016,Runnable afterFirst10016){
        if(!currentSection10003("Relationship",token10016,host10016,account10016))return;
        final TextView progress10016=tv(cursor10016==null?"⌛ Loading CP requests…":"⌛ Loading more CP requests…",
            13,MUTED,false);
        host10016.addView(progress10016,new LinearLayout.LayoutParams(-1,dp(38)));
        Query query10016=db.collection("relationships").whereEqualTo(field10016,account10016).limit(30);
        if(cursor10016!=null)query10016=query10016.startAfter(cursor10016);
        query10016.get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(snap10016->{
            if(!currentSection10003("Relationship",token10016,host10016,account10016))return;
            host10016.removeView(progress10016);
            for(DocumentSnapshot d10016:snap10016.getDocuments()){
                if(!seen10016.add(d10016.getId()))continue;
                String st10016=safe(d10016.getString("status"),"pending");
                if("accepted".equals(st10016))accepted10016[0]++;
                else if("pending".equals(st10016))pending10016[0]++;
                addRelationship(d10016);
            }
            refresh10016.run();
            if(snap10016.size()==30){
                final DocumentSnapshot next10016=snap10016.getDocuments().get(snap10016.size()-1);
                TextView more10016=button("↓ Load more CP requests",()->{});
                more10016.setOnClickListener(v->{
                    if(!currentSection10003("Relationship",token10016,host10016,account10016))return;
                    host10016.removeView(more10016);
                    loadRelationshipPage10016(field10016,account10016,token10016,host10016,
                        seen10016,accepted10016,pending10016,summary10016,refresh10016,next10016,null);
                });
                host10016.addView(more10016,new LinearLayout.LayoutParams(-1,dp(44)));
            }
            if(afterFirst10016!=null)afterFirst10016.run();
        }).addOnFailureListener(error10016->{
            if(!currentSection10003("Relationship",token10016,host10016,account10016))return;
            progress10016.setText("↻ Unable to load CP requests • tap to retry");
            progress10016.setOnClickListener(v->{
                if(!currentSection10003("Relationship",token10016,host10016,account10016))return;
                host10016.removeView(progress10016);
                loadRelationshipPage10016(field10016,account10016,token10016,host10016,
                    seen10016,accepted10016,pending10016,summary10016,refresh10016,cursor10016,afterFirst10016);
            });
        });
    }

    private String pairId(String a,String b){return KingMeCouplePolicy9985.pairId(a,b);}
    // Firestore security permits CREATE of a pending CP request, not overwrite.
    // Read-before-set is unsafe: two simultaneous devices could reset a confirmed CP.
    private void sendRelationship(String uid,String name){
        if(!cloud()||me==null||uid==null||uid.length()<4||uid.equals(me.getUid())){
            toast("Select another signed-in KING member");return;
        }
        if(relationshipRequestInFlight10012){toast("Request is already sending");return;}
        relationshipRequestInFlight10012=true;
        final String account10012=me.getUid();
        final String target10012=uid;
        String a=account10012.compareTo(target10012)<0?account10012:target10012;
        String b=account10012.compareTo(target10012)<0?target10012:account10012;
        String mine=safe(displayName,"KING User"),other=safe(name,"KING User");
        if(mine.length()>80)mine=mine.substring(0,80);
        if(other.length()>80)other=other.substring(0,80);
        Map<String,Object> request10012=new HashMap<>();
        request10012.put("uidA",a);request10012.put("uidB",b);
        request10012.put("nameA",a.equals(account10012)?mine:other);
        request10012.put("nameB",b.equals(account10012)?mine:other);
        request10012.put("initiatorUid",account10012);
        request10012.put("status","pending");
        request10012.put("createdAt",FieldValue.serverTimestamp());
        db.collection("relationships").document(pairId(a,b)).set(request10012)
          .addOnSuccessListener(v->{
              relationshipRequestInFlight10012=false;
              if(isFinishing()||isDestroyed()||me==null||!account10012.equals(me.getUid()))return;
              toast("CP request sent");targetUid="";targetName="";
              if("Relationship".equals(selected))render("Relationship");
          }).addOnFailureListener(e->{
              relationshipRequestInFlight10012=false;
              if(!isFinishing()&&!isDestroyed()&&me!=null&&account10012.equals(me.getUid()))
                  toast("Request not sent • existing request or permission denied");
          });
    }
    private void addRelationship(DocumentSnapshot d){
        if(d==null||me==null)return;
        final String account10012=me.getUid();
        final String a=safe(d.getString("uidA"),""),b=safe(d.getString("uidB"),"");
        if(!account10012.equals(a)&&!account10012.equals(b))return;
        final String other=account10012.equals(a)?b:a;
        final String name=account10012.equals(a)?safe(d.getString("nameB"),"KING User"):safe(d.getString("nameA"),"KING User");
        final String status=safe(d.getString("status"),"pending");
        final String initiator=safe(d.getString("initiatorUid"),"");
        TextView row=tv("💞 "+name+" • "+status,14,DARK,true);
        row.setBackground(bg(CARD,14));
        row.setOnClickListener(v->{
            if(me==null||!account10012.equals(me.getUid())||!"Relationship".equals(selected))return;
            List<String> ops=new ArrayList<>();
            if("pending".equals(status)&&!initiator.equals(account10012)){
                ops.add("Accept");ops.add("Decline");
            }
            ops.add("Private Chat");ops.add("Remove / Unpair");
            new AlertDialog.Builder(this).setTitle(name).setItems(ops.toArray(new String[0]),(x,w)->{
                String op=ops.get(w);
                if("Private Chat".equals(op)){openChat(other,name);return;}
                if("Remove / Unpair".equals(op)){
                    new AlertDialog.Builder(this).setTitle("Remove "+name+"?")
                        .setMessage("The CP connection or request will be deleted for both members.")
                        .setNegativeButton("Cancel",null)
                        .setPositiveButton("Remove",(confirm,which)->{
                            if(me==null||!account10012.equals(me.getUid())||!"Relationship".equals(selected))return;
                            // The confirmation could have remained open while a second phone
                            // accepted/recreated this CP. Delete only the exact observed state.
                            final com.google.firebase.Timestamp originallyCreated10015=d.getTimestamp("createdAt");
                            db.runTransaction(tx10015->{
                                DocumentSnapshot now10015=tx10015.get(d.getReference());
                                if(!now10015.exists()
                                    ||!status.equals(now10015.getString("status"))
                                    ||!initiator.equals(now10015.getString("initiatorUid"))
                                    ||!java.util.Objects.equals(originallyCreated10015,now10015.getTimestamp("createdAt"))
                                    ||!account10012.equals(a)&&!account10012.equals(b))
                                    throw new IllegalStateException("CP changed on another phone. Refresh Home.");
                                tx10015.delete(d.getReference());return null;
                            }).addOnSuccessListener(done->refreshRelationship10012(account10012))
                              .addOnFailureListener(error->{
                                  if(me!=null&&account10012.equals(me.getUid())){
                                      toast("Could not remove CP: "+safe(error.getMessage(),"Refresh Home"));
                                      refreshRelationship10012(account10012);
                                  }
                              });
                        })
                        .show();return;
                }
                // The list is a snapshot. A second phone may have already accepted,
                // declined or deleted this request; never act on stale row state.
                if(me==null||!account10012.equals(me.getUid())||!"Relationship".equals(selected))return;
                final DocumentReference cpRef10014=d.getReference();
                final String requestedState10014="Accept".equals(op)?"accepted":"declined";
                db.runTransaction(transaction10014->{
                    DocumentSnapshot latest10014=transaction10014.get(cpRef10014);
                    if(!latest10014.exists()
                        ||!"pending".equals(latest10014.getString("status"))
                        ||account10012.equals(latest10014.getString("initiatorUid"))
                        ||(!account10012.equals(latest10014.getString("uidA"))
                            && !account10012.equals(latest10014.getString("uidB"))))
                        throw new IllegalStateException("CP request was already changed. Refresh Home.");
                    transaction10014.update(cpRef10014,"status",requestedState10014,
                        "updatedAt",FieldValue.serverTimestamp());
                    return null;
                }).addOnSuccessListener(done->{
                    if(!isFinishing()&&!isDestroyed()&&me!=null&&account10012.equals(me.getUid()))
                        refreshRelationship10012(account10012);
                }).addOnFailureListener(error->{
                    if(!isFinishing()&&!isDestroyed()&&me!=null&&account10012.equals(me.getUid()))
                        toast("CP request not changed: "+safe(error.getMessage(),"Refresh and try again"));
                });
            }).show();
        });
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(52));
        lp.setMargins(0,dp(5),0,dp(5));body.addView(row,lp);
    }
    private void refreshRelationship10012(String account10012){
        if(!isFinishing()&&!isDestroyed()&&me!=null
            &&account10012.equals(me.getUid())&&"Relationship".equals(selected))render("Relationship");
    }

    private void visitors750(){
        body.addView(tv("Profile Visitors",21,DARK,true));
        body.addView(tv("Recent profile visits • load older visits below",12,MUTED,false));
        if(!cloud()){body.addView(tv("Sign in to see profile visitors.",14,MUTED,false));return;}
        final LinearLayout host10005=body;
        final int token10005=searchGeneration9994;
        final String owner10005=me.getUid();
        loadVisitorPage10005(host10005,token10005,owner10005,null,0);
    }
    /** Cursor pagination: older visitor records remain available after the newest 50. */
    private void loadVisitorPage10005(LinearLayout host,int token,String owner,
                                       DocumentSnapshot cursor,int page){
        if(!currentSection10003("Visitors",token,host,owner))return;
        final TextView progress=tv(cursor==null?"Loading recent visitors…":"Loading older visitors…",
            13,MUTED,true);
        progress.setGravity(Gravity.CENTER);progress.setBackground(bg(CARD,14));
        LinearLayout.LayoutParams progressLp=new LinearLayout.LayoutParams(-1,dp(54));
        progressLp.setMargins(0,dp(8),0,dp(6));host.addView(progress,progressLp);
        Query q=db.collection("public_profiles").document(owner).collection("visitors")
            .orderBy("visitedAt",Query.Direction.DESCENDING).limit(50);
        if(cursor!=null)q=q.startAfter(cursor);
        q.get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(snapshot->{
              if(!currentSection10003("Visitors",token,host,owner))return;
              host.removeView(progress);
              List<DocumentSnapshot> records=snapshot.getDocuments();
              if(records.isEmpty()){
                  host.addView(tv(page==0?"No profile visitors yet.":"All older visitors loaded.",
                      13,MUTED,false));return;
              }
              if(page==0)host.addView(tv("Most recent visits first",12,MUTED,false));
              int shown=0;
              for(DocumentSnapshot d:records){
                  final String uid=safe(d.getString("uid"),d.getId());
                  if(uid.equals(owner))continue;
                  final String name=safe(d.getString("name"),"KING User");
                  com.google.firebase.Timestamp timestamp=d.getTimestamp("visitedAt");
                  String when=timestamp==null?"Recently":new java.text.SimpleDateFormat(
                      "dd MMM • HH:mm",java.util.Locale.US).format(timestamp.toDate());
                  LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);
                  row.setPadding(dp(12),dp(7),dp(10),dp(7));row.setBackground(bg(CARD,14));
                  TextView avatar=tv("👁",20,PURPLE,false);avatar.setGravity(Gravity.CENTER);
                  row.addView(avatar,new LinearLayout.LayoutParams(dp(44),dp(50)));
                  LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);
                  info.addView(tv(name,14,DARK,true),new LinearLayout.LayoutParams(-1,dp(27)));
                  info.addView(tv(when,11,MUTED,false),new LinearLayout.LayoutParams(-1,dp(22)));
                  row.addView(info,new LinearLayout.LayoutParams(0,dp(50),1));
                  TextView open=tv("Profile ›",11,PURPLE,true);open.setGravity(Gravity.CENTER);
                  row.addView(open,new LinearLayout.LayoutParams(dp(82),dp(50)));
                  row.setOnClickListener(v->{
                      if(!currentSection10003("Visitors",token,host,owner))return;
                      Intent intent=new Intent(this,KingPublicProfileActivity.class);
                      intent.putExtra("uid",uid);intent.putExtra("name",name);startActivity(intent);
                  });
                  LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(64));
                  lp.setMargins(0,dp(4),0,dp(4));host.addView(row,lp);shown++;
              }
              if(shown==0)host.addView(tv("No additional visitor profiles on this page.",12,MUTED,false));
              if(records.size()==50){
                  final DocumentSnapshot next=records.get(records.size()-1);
                  TextView more=button("↓ Load older visitors",()->{});
                  more.setOnClickListener(v->{
                      if(!currentSection10003("Visitors",token,host,owner))return;
                      host.removeView(more);
                      loadVisitorPage10005(host,token,owner,next,page+1);
                  });
                  host.addView(more,new LinearLayout.LayoutParams(-1,dp(48)));
              }else host.addView(tv("All older visitors loaded.",12,MUTED,false));
          }).addOnFailureListener(error->{
              if(!currentSection10003("Visitors",token,host,owner))return;
              progress.setText("Visitors unavailable • tap to retry");
              progress.setOnClickListener(v->{
                  if(!currentSection10003("Visitors",token,host,owner))return;
                  host.removeView(progress);
                  loadVisitorPage10005(host,token,owner,cursor,page);
              });
          });
    }

    private void collection(){
        LevelSystem.Snapshot s=LevelSystem.read(this);
        body.addView(tv("Collection Center",21,DARK,true));
        TextView subtitle952=tv("Wardrobe, medals, backpack and progression in one place",12,MUTED,false);body.addView(subtitle952,new LinearLayout.LayoutParams(-1,dp(36)));

        LinearLayout hero952=new LinearLayout(this);hero952.setOrientation(LinearLayout.VERTICAL);hero952.setPadding(dp(14),dp(10),dp(14),dp(10));hero952.setBackground(bg(0xffffefd0,18));
        TextView tier952=tv("👑 "+LevelSystem.levelTier(s.level)+"   •   VIP "+s.vipLevel,17,DARK,true);tier952.setGravity(Gravity.CENTER);hero952.addView(tier952,new LinearLayout.LayoutParams(-1,dp(36)));
        TextView prog952=tv("Lv."+s.level+" / "+LevelSystem.MAX_LEVEL+"   •   XP "+s.xp+" / "+s.nextLevelXp(),12,MUTED,true);prog952.setGravity(Gravity.CENTER);hero952.addView(prog952,new LinearLayout.LayoutParams(-1,dp(28)));
        android.widget.ProgressBar levelBar952=new android.widget.ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);long nextXp952=Math.max(1,s.nextLevelXp());levelBar952.setMax((int)Math.min(Integer.MAX_VALUE,nextXp952));levelBar952.setProgress((int)Math.min(Integer.MAX_VALUE,Math.max(0,s.xp)));hero952.addView(levelBar952,new LinearLayout.LayoutParams(-1,dp(10)));
        LinearLayout.LayoutParams hp952=new LinearLayout.LayoutParams(-1,dp(92));hp952.setMargins(0,dp(6),0,dp(8));body.addView(hero952,hp952);

        TextView wardrobe=button("👗 Open Wardrobe • Frames, Effects, Bubbles & Badges",()->startActivity(new Intent(this,KingWardrobeActivity.class)));body.addView(wardrobe,new LinearLayout.LayoutParams(-1,dp(50)));
        TextView equipped952=tv("🖼 "+KingCosmetics.frame(this)+"   ✨ "+KingCosmetics.effect(this)+"\n💬 "+KingCosmetics.bubble(this),12,DARK,true);equipped952.setGravity(Gravity.CENTER_VERTICAL);equipped952.setBackground(bg(CARD,14));equipped952.setPadding(dp(12),dp(8),dp(12),dp(8));LinearLayout.LayoutParams eqp952=new LinearLayout.LayoutParams(-1,dp(62));eqp952.setMargins(0,dp(6),0,dp(8));body.addView(equipped952,eqp952);

        body.addView(tv("Medals & Achievements",16,DARK,true));
        LinearLayout medals952=new LinearLayout(this);medals952.setOrientation(LinearLayout.VERTICAL);
        String[][] medalData952={{"🏅","Rising Star",s.level>=10?"Unlocked":"Lv.10"},{"🎖","Elite Voice",s.level>=30?"Unlocked":"Lv.30"},{"💎","VIP Supporter",s.vipLevel>=3?"Unlocked":"VIP 3"},{"👑","Royal Patron",s.vipLevel>=8?"Unlocked":"VIP 8"}};
        for(String[] m952:medalData952){TextView row952=tv(m952[0]+"  "+m952[1]+"     •     "+m952[2],13,DARK,true);row952.setBackground(bg(CARD,13));LinearLayout.LayoutParams mlp952=new LinearLayout.LayoutParams(-1,dp(46));mlp952.setMargins(0,dp(2),0,dp(2));medals952.addView(row952,mlp952);}body.addView(medals952,new LinearLayout.LayoutParams(-1,-2));

        body.addView(tv("Backpack",16,DARK,true));SharedPreferences bp=getSharedPreferences("king_backpack",MODE_PRIVATE);String[] keys={"rose","heart","star","firework"},names={"Rose","Heart","Star","Firework"},icons={"🌹","💗","⭐","🎆"};
        LinearLayout bag952=new LinearLayout(this);bag952.setGravity(Gravity.CENTER);for(int i=0;i<keys.length;i++){TextView item952=tv(icons[i]+"\n"+names[i]+"\n×"+bp.getInt(keys[i],0),11,DARK,true);item952.setGravity(Gravity.CENTER);item952.setBackground(bg(CARD,13));LinearLayout.LayoutParams ilp952=new LinearLayout.LayoutParams(0,dp(78),1);ilp952.setMargins(dp(2),dp(3),dp(2),dp(3));bag952.addView(item952,ilp952);}body.addView(bag952,new LinearLayout.LayoutParams(-1,dp(84)));

        TextView vip952=button("💎 Open VIP Center",()->startActivity(new Intent(this,KingVipVisualActivity.class)));TextView claim=button("🎁 Claim Daily Free Gift",()->claimDaily(bp));TextView party=button("🎤 Open Party • Use Backpack",()->KingNav.openRoot(this,0));LinearLayout.LayoutParams blp952=new LinearLayout.LayoutParams(-1,dp(48));blp952.setMargins(0,dp(6),0,0);body.addView(vip952,blp952);body.addView(claim,new LinearLayout.LayoutParams(blp952));body.addView(party,new LinearLayout.LayoutParams(blp952));
    }
    private void claimDaily(SharedPreferences bp){long day=System.currentTimeMillis()/86400000L,last=bp.getLong("claim_day",-1);if(day==last){toast("Daily gift already claimed");return;}String[] keys={"rose","heart","star","firework"};String key=keys[(int)(day%keys.length)];bp.edit().putLong("claim_day",day).putInt(key,bp.getInt(key,0)+1).apply();toast("Daily gift added to Backpack");render("Collection");}
}
