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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class InboxActivity extends Activity {
    private static final int BG = 0xfffff8ed;
    private static final int PURPLE = 0xff8a49ed;
    private static final int DARK = 0xff17131f;
    private static final int MUTED = 0xff817b86;

    private LinearLayout list;
    private ListenerRegistration threadListener;
    private int inboxListenerGeneration10030;
    private FirebaseUser me;
    private FirebaseFirestore db;
    private final ArrayList<ThreadModel> models = new ArrayList<>();
    private String inboxState940="";
    private String activeSearch10029="";
    private android.net.ConnectivityManager.NetworkCallback inboxNetworkCallback940;
    private boolean inboxLastOnline940;

    private static class ThreadModel {
        String icon, name, preview, peerUid, time;
        boolean unread;
        ThreadModel(String icon,String name,String preview,String peerUid,String time,boolean unread){
            this.icon=icon;this.name=name;this.preview=preview;this.peerUid=peerUid;this.time=time;this.unread=unread;
        }
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        try { me = FirebaseAuth.getInstance().getCurrentUser(); } catch (Exception ignored) { me = null; }
        try { db = FirebaseFirestore.getInstance(); } catch (Exception ignored) { db = null; }
        renderShell();
        loadInbox();
        if(getIntent()!=null&&getIntent().getBooleanExtra("openNotifications10010",false))
            showCloudNotifications940("");
        inboxLastOnline940=KingNetwork.online(this);
        inboxNetworkCallback940=KingNetwork.watch(this,online->runOnUiThread(()->{
            boolean recovered=online&&!inboxLastOnline940;inboxLastOnline940=online;
            if(recovered&&!isFinishing()&&!isDestroyed())loadInbox();
            else if(!online&&!isFinishing()&&!isDestroyed()){inboxState940="Offline • cached thread summaries only";renderCurrentInbox10029();}
        }));
    }

    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
    private GradientDrawable bg(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private TextView label(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(null, Typeface.BOLD); return t;
    }

    private void renderShell() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackground(KingRoyalTheme10021.page());

        LinearLayout head = new LinearLayout(this); head.setGravity(Gravity.CENTER_VERTICAL); head.setPadding(dp(18),dp(10),dp(8),dp(4));head.setBackground(KingRoyalTheme10021.header(this));
        TextView title = label("Messages",27,DARK,true); head.addView(title,new LinearLayout.LayoutParams(0,dp(58),1));
        TextView search = label("⌕",29,DARK,false); search.setGravity(Gravity.CENTER); search.setOnClickListener(v->showSearch()); head.addView(search,new LinearLayout.LayoutParams(dp(50),dp(54)));
        TextView plus = label("＋",29,DARK,false); plus.setGravity(Gravity.CENTER); plus.setOnClickListener(v->newChatDialog()); head.addView(plus,new LinearLayout.LayoutParams(dp(50),dp(54)));
        root.addView(head);

        LinearLayout notice940=new LinearLayout(this);notice940.setGravity(Gravity.CENTER_VERTICAL);notice940.setPadding(dp(14),dp(6),dp(10),dp(6));notice940.setBackground(KingRoyalTheme10021.card(this,14));
        LinearLayout noticeText940=new LinearLayout(this);noticeText940.setOrientation(LinearLayout.VERTICAL);TextView nt940=label("Interactive Notifications",13,DARK,true);TextView ns940=label("Follows, gifts, room activity and invitations",10,0xff8a7d58,false);noticeText940.addView(nt940,new LinearLayout.LayoutParams(-1,dp(22)));noticeText940.addView(ns940,new LinearLayout.LayoutParams(-1,dp(19)));notice940.addView(noticeText940,new LinearLayout.LayoutParams(0,dp(42),1));
        TextView view940=label("View",11,0xff2b2500,true);view940.setGravity(Gravity.CENTER);view940.setBackground(bg(0xffffe500,10));view940.setOnClickListener(v->showCloudNotifications940(""));notice940.addView(view940,new LinearLayout.LayoutParams(dp(68),dp(36)));
        LinearLayout.LayoutParams np940=new LinearLayout.LayoutParams(-1,dp(58));np940.setMargins(dp(12),0,dp(12),dp(6));root.addView(notice940,np940);

        TextView recentTitle=label("Hot someone to chat",13,0xff5f5964,true);recentTitle.setPadding(dp(18),0,0,0);root.addView(recentTitle,new LinearLayout.LayoutParams(-1,dp(34)));
        root.addView(recentFriends940(),new LinearLayout.LayoutParams(-1,dp(78)));
        View dividerTop = new View(this); dividerTop.setBackgroundColor(0xffeeecef); root.addView(dividerTop,new LinearLayout.LayoutParams(-1,dp(1)));

        ScrollView sv = new ScrollView(this); list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); list.setPadding(0,0,0,dp(16)); sv.addView(list);
        root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        addBottomNav(root,3);
        setSafeContentView10021(root);
    }

    private void setSafeContentView10021(View root){
        setContentView(root);
        final int l=root.getPaddingLeft(),t=root.getPaddingTop(),rr=root.getPaddingRight(),b=root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root,(v,insets)->{
            Insets safe=insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(l+safe.left,t+safe.top,rr+safe.right,b+safe.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private LinearLayout recentPeopleRow10022;
    private View recentFriends940(){
        android.widget.HorizontalScrollView sc=new android.widget.HorizontalScrollView(this);
        sc.setHorizontalScrollBarEnabled(false);
        recentPeopleRow10022=new LinearLayout(this);recentPeopleRow10022.setPadding(dp(12),0,dp(12),dp(6));
        sc.addView(recentPeopleRow10022);return sc;
    }
    private void refreshRecentFriends10022(){
        if(recentPeopleRow10022==null)return;
        recentPeopleRow10022.removeAllViews();int shown10022=0;
        for(ThreadModel m10022:models){
            if(m10022.peerUid==null||m10022.peerUid.trim().isEmpty())continue;
            addRecentFriend940(recentPeopleRow10022,m10022.name,m10022.peerUid);
            if(++shown10022==8)break;
        }
        if(shown10022==0){TextView hint10022=label("Your real conversations will appear here",12,MUTED,false);
            hint10022.setGravity(Gravity.CENTER_VERTICAL);
            recentPeopleRow10022.addView(hint10022,new LinearLayout.LayoutParams(-1,dp(64)));}
    }
    private void addRecentFriend940(LinearLayout row,String name,String uid){LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setGravity(Gravity.CENTER);TextView av=label(initial(name),17,Color.WHITE,true);av.setGravity(Gravity.CENTER);av.setBackground(bg(0xff9a75e8,25));box.addView(av,new LinearLayout.LayoutParams(dp(48),dp(48)));TextView nm=label(name,9,0xff6b6570,false);nm.setGravity(Gravity.CENTER);nm.setSingleLine(true);box.addView(nm,new LinearLayout.LayoutParams(dp(68),dp(20)));box.setOnClickListener(v->openChat(name,uid));row.addView(box,new LinearLayout.LayoutParams(dp(74),dp(72)));}
    private void addInboxState940(){
        if(inboxState940==null||inboxState940.trim().isEmpty())return;
        TextView state=label(inboxState940,12,inboxState940.startsWith("Syncing")?0xff675d76:0xff8b5b12,true);
        state.setGravity(Gravity.CENTER_VERTICAL);state.setPadding(dp(16),0,dp(16),0);
        state.setBackground(bg(inboxState940.startsWith("Syncing")?0xfff3eefb:0xfffff4d6,10));
        if(inboxState940.contains("retry"))state.setOnClickListener(v->{
            FirebaseUser actual=FirebaseAuth.getInstance().getCurrentUser();
            if(actual!=null&&me!=null&&actual.getUid().equals(me.getUid()))loadInbox();});
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(42));p.setMargins(dp(12),dp(7),dp(12),dp(7));list.addView(state,p);
    }
    private void addSystemRows940(){addSystemRow940("🔔","Interactive notifications","Follows, gifts and room activity",()->showCloudNotifications940(""));addSystemRow940("👑","KING Official","Safety, events and app notices",()->showCloudNotifications940(""));addSystemRow940("🎤","Room invitations","Party and game invitations",()->showCloudNotifications940("room_invite"));}
    private void addSystemRow940(String icon,String title,String sub,Runnable action){LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(16),dp(8),dp(14),dp(8));row.setBackground(KingRoyalTheme10021.card(this,14));TextView av=label(icon,23,DARK,false);av.setGravity(Gravity.CENTER);av.setBackground(bg(0xfffff1d6,27));row.addView(av,new LinearLayout.LayoutParams(dp(52),dp(52)));LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);info.setPadding(dp(12),0,0,0);info.addView(label(title,15,DARK,true),new LinearLayout.LayoutParams(-1,dp(28)));TextView sb=label(sub,12,MUTED,false);sb.setSingleLine(true);info.addView(sb,new LinearLayout.LayoutParams(-1,dp(24)));row.addView(info,new LinearLayout.LayoutParams(0,dp(52),1));TextView ar=label("›",24,0xffaaa4ad,false);ar.setGravity(Gravity.CENTER);row.addView(ar,new LinearLayout.LayoutParams(dp(34),dp(52)));row.setOnClickListener(v->KingSafe.run(this,"ui-action",action));LinearLayout.LayoutParams rowLp=new LinearLayout.LayoutParams(-1,dp(76));rowLp.setMargins(dp(10),dp(4),dp(10),dp(4));list.addView(row,rowLp);}
    private void addQuick(LinearLayout host,String icon,String text,Runnable action){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setGravity(Gravity.CENTER);
        TextView av=label(icon,25,DARK,false);av.setGravity(Gravity.CENTER);av.setBackground(bg(0xffffedf4,28));box.addView(av,new LinearLayout.LayoutParams(dp(52),dp(52)));
        TextView name=label(text,12,0xff514b55,true);name.setGravity(Gravity.CENTER);box.addView(name,new LinearLayout.LayoutParams(-1,dp(28)));
        box.setOnClickListener(v->KingSafe.run(this,"ui-action",action));host.addView(box,new LinearLayout.LayoutParams(0,dp(84),1));
    }

    private void loadInbox() {
        final int callbackGeneration10030=++inboxListenerGeneration10030;
        if(threadListener!=null){threadListener.remove();threadListener=null;}
        me=FirebaseAuth.getInstance().getCurrentUser();
        models.clear();
        // v10.0.23: Names alone are not verified Firebase identities. Never show ghost threads.
        // Real conversations populate from the authenticated user's direct_threads listener.
        if(me==null)inboxState940="Sign in to sync real conversations";
        else if(db==null)inboxState940="Cloud unavailable • showing local conversations";
        else inboxState940="Syncing conversations…";
        renderCurrentInbox10029();

        if (me == null || db == null) return;
        final String sessionUid10023=me.getUid();
        threadListener = db.collection("direct_threads").whereArrayContains("members", sessionUid10023)
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(snap,error)->{
                if(callbackGeneration10030!=inboxListenerGeneration10030
                    ||!inboxCurrent10023(sessionUid10023))return;
                if(error!=null){
                    inboxState940="Cloud sync unavailable • tap to retry";
                    renderCurrentInbox10029();
                    return;
                }
                // Cached snapshots can be useful offline, but must not be labelled live.
                inboxState940=!KingNetwork.online(this)||(snap!=null&&snap.getMetadata().isFromCache())
                    ?"Offline • cached thread summaries only":"";
                renderCloudThreads(snap);
            });
    }

    private boolean inboxCurrent10023(String session10023){
        FirebaseUser current10023=FirebaseAuth.getInstance().getCurrentUser();
        return !isFinishing()&&!isDestroyed()&&current10023!=null
            &&me!=null&&session10023.equals(current10023.getUid())&&session10023.equals(me.getUid());
    }
    @Override protected void onResume(){
        super.onResume();
        FirebaseUser current10023=FirebaseAuth.getInstance().getCurrentUser();
        String uid10023=current10023==null?null:current10023.getUid();
        if(me==null?uid10023!=null:!me.getUid().equals(uid10023)){
            if(threadListener!=null){threadListener.remove();threadListener=null;}
            me=current10023;models.clear();activeSearch10029="";loadInbox();
        }
    }
    private void renderCloudThreads(QuerySnapshot snap) {
        models.clear();
        if (snap != null) {
            List<DocumentSnapshot> docs = new ArrayList<>(snap.getDocuments());
            Collections.sort(docs,(a,b)->Long.compare(timeOf(b),timeOf(a)));
            Set<String> seen = new HashSet<>();
            for (DocumentSnapshot doc : docs) {
                // Direct chat must contain exactly two unique real user IDs.
                // A group or malformed document cannot masquerade as a private thread.
                Object membersValue10033=doc.get("members");
                if(!(membersValue10033 instanceof List))continue;
                List<?> members10033=(List<?>)membersValue10033;
                if(members10033.size()!=2||!(members10033.get(0) instanceof String)
                    ||!(members10033.get(1) instanceof String))continue;
                String first10033=(String)members10033.get(0);
                String second10033=(String)members10033.get(1);
                if(!KingSixPages10038.exactPrivateThreadMembers(first10033,second10033,me.getUid()))continue;
                String peerUid=first10033.equals(me.getUid())?second10033:first10033;
                String peerName=peerUid; Object namesObj=doc.get("memberNames");
                if(namesObj instanceof Map){Object n=((Map<?,?>)namesObj).get(peerUid);if(n!=null)peerName=String.valueOf(n);}
                if(doc.getString("lastMessage")==null&&doc.getTimestamp("updatedAt")==null)continue;
                // Only claim a peer after validating a usable thread, otherwise a
                // stale skeleton can mask a real conversation below it.
                if(!seen.add(peerUid))continue;
                String last=KingSixPages10040.safePreview(doc.getString("lastMessage"),180);if(last.isEmpty())last="Open conversation";
                Timestamp updated=doc.getTimestamp("updatedAt");
                models.add(new ThreadModel(initial(peerName),peerName,last,peerUid,formatTime(updated),isUnread(doc)));
            }
        }
        // Do not merge unverified name-only local threads into a real Firestore inbox.
        renderCurrentInbox10029();
    }

    private List<ThreadModel> searchResults10029(){
        if(activeSearch10029==null||activeSearch10029.isEmpty())return models;
        ArrayList<ThreadModel> filtered10029=new ArrayList<>();
        for(ThreadModel thread10029:models){
            if((thread10029.name!=null&&thread10029.name.toLowerCase(Locale.ROOT).contains(activeSearch10029))
                ||(thread10029.peerUid!=null&&thread10029.peerUid.toLowerCase(Locale.ROOT).contains(activeSearch10029))
                ||(thread10029.preview!=null&&thread10029.preview.toLowerCase(Locale.ROOT).contains(activeSearch10029)))
                filtered10029.add(thread10029);
        }
        return filtered10029;
    }
    private void renderCurrentInbox10029(){renderModels(searchResults10029());}

    private long timeOf(DocumentSnapshot d){Timestamp t=d.getTimestamp("updatedAt");return t==null?0:t.toDate().getTime();}
    private String formatTime(Timestamp t){
        if(t==null)return ""; Date d=t.toDate(); long diff=System.currentTimeMillis()-d.getTime();
        if(diff<24L*60*60*1000)return new SimpleDateFormat("HH:mm",Locale.getDefault()).format(d);
        if(diff<48L*60*60*1000)return "Yesterday";
        return new SimpleDateFormat("dd/MM",Locale.getDefault()).format(d);
    }
    private String initial(String name){if(name==null||name.trim().isEmpty())return "●";return name.trim().substring(0,1).toUpperCase(Locale.getDefault());}

    private void renderModels(List<ThreadModel> source){
        if(list==null)return; refreshRecentFriends10022();list.removeAllViews();addSystemRows940();addInboxState940();
        if(source==null||source.isEmpty()){
            LinearLayout empty=new LinearLayout(this);empty.setOrientation(LinearLayout.VERTICAL);empty.setGravity(Gravity.CENTER);empty.setPadding(dp(24),dp(70),dp(24),dp(24));
            TextView icon=label("💬",52,0xffbbb4c1,false);icon.setGravity(Gravity.CENTER);empty.addView(icon,new LinearLayout.LayoutParams(-1,dp(72)));
            TextView t=label(activeSearch10029.isEmpty()?"No messages yet":"No matching messages",18,DARK,true);
            t.setGravity(Gravity.CENTER);empty.addView(t);
            TextView s=label(me==null?"Sign in to sync real conversations":"Start a chat from Discover or Friends",13,MUTED,false);s.setGravity(Gravity.CENTER);empty.addView(s);
            list.addView(empty,new LinearLayout.LayoutParams(-1,dp(230)));return;
        }
        for(ThreadModel m:source)addThreadRow(m);
    }

    private void addThreadRow(ThreadModel m){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(16),dp(10),dp(14),dp(10));row.setBackground(KingRoyalTheme10021.card(this,14));
        TextView avatar=label(m.icon,20,Color.WHITE,true);avatar.setGravity(Gravity.CENTER);avatar.setBackground(bg(0xffa86af1,28));row.addView(avatar,new LinearLayout.LayoutParams(dp(52),dp(52)));
        LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);info.setPadding(dp(12),0,dp(6),0);
        TextView n=label(m.name,16,DARK,true);info.addView(n,new LinearLayout.LayoutParams(-1,dp(27)));
        TextView p=label(m.preview,13,m.unread?0xff3a3540:MUTED,m.unread);p.setSingleLine(true);info.addView(p,new LinearLayout.LayoutParams(-1,dp(25)));
        row.addView(info,new LinearLayout.LayoutParams(0,dp(54),1));
        LinearLayout meta=new LinearLayout(this);meta.setOrientation(LinearLayout.VERTICAL);meta.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        TextView tm=label(m.time,11,0xffa09aa5,false);tm.setGravity(Gravity.RIGHT);meta.addView(tm,new LinearLayout.LayoutParams(dp(72),dp(23)));
        TextView dot=label(m.unread?"●":"",14,PURPLE,true);dot.setGravity(Gravity.RIGHT);meta.addView(dot,new LinearLayout.LayoutParams(dp(72),dp(25)));
        row.addView(meta,new LinearLayout.LayoutParams(dp(78),dp(52)));
        row.setOnClickListener(v->openChat(m.name,m.peerUid)); LinearLayout.LayoutParams rowLp=new LinearLayout.LayoutParams(-1,dp(80));rowLp.setMargins(dp(10),dp(4),dp(10),dp(4));list.addView(row,rowLp);
        View line=new View(this);line.setBackgroundColor(0xffefedf1);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(1));lp.leftMargin=dp(80);list.addView(line,lp);
    }

    private boolean isUnread(DocumentSnapshot doc) {
        if (me == null) return false; String lastSender=doc.getString("lastSenderUid"); if(me.getUid().equals(lastSender))return false;
        Timestamp updated=doc.getTimestamp("updatedAt");if(updated==null)return false;Object readObj=doc.get("readAt");if(!(readObj instanceof Map))return true;
        Object own=((Map<?,?>)readObj).get(me.getUid());return !(own instanceof Timestamp)||((Timestamp)own).compareTo(updated)<0;
    }

    private void showSearch(){
        final EditText q=new EditText(this);q.setHint("Search name, message or verified UID");q.setSingleLine(true);
        q.setText(activeSearch10029);q.setSelection(q.length());
        new AlertDialog.Builder(this).setTitle("Search").setView(q).setNegativeButton("Cancel",null).setPositiveButton("Search",(d,w)->{
            activeSearch10029=q.getText().toString().trim().toLowerCase(Locale.ROOT);
            renderCurrentInbox10029();
        }).setNeutralButton("Show all",(d,w)->{activeSearch10029="";renderCurrentInbox10029();}).show();
    }

    private void showNotifications(){showCloudNotifications940("");}
    private void showCloudNotifications940(String filter){
        FirebaseUser actual10024=FirebaseAuth.getInstance().getCurrentUser();
        if(actual10024==null||db==null){new AlertDialog.Builder(this).setTitle("Notifications").setMessage("Sign in to load your real KING Plus notifications.").setPositiveButton("OK",null).show();return;}
        final String noticeAccount10010=actual10024.getUid();
        db.collection("notifications").document(noticeAccount10010).collection("items")
            .orderBy("createdAt",com.google.firebase.firestore.Query.Direction.DESCENDING).limit(200).get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(snap->{
                if(!noticeAccountCurrent10010(noticeAccount10010))return;
                List<DocumentSnapshot> docs=new ArrayList<>();List<String> rows=new ArrayList<>();
                for(DocumentSnapshot n:snap.getDocuments()){
                    Object dataObj=n.get("data");String type="";if(dataObj instanceof Map){Object t=((Map<?,?>)dataObj).get("type");if(t!=null)type=String.valueOf(t);}
                    if(filter!=null&&!filter.isEmpty()&&!filter.equals(type))continue;
                    docs.add(n);String title=n.getString("title"),body=n.getString("body");Timestamp at=n.getTimestamp("createdAt");
                    String when=formatTime(at);boolean unread=!Boolean.TRUE.equals(n.getBoolean("read"));rows.add((unread?"● ":"")+(title==null?"KING Plus":title)+(when.isEmpty()?"":"  •  "+when)+"\n"+(body==null?"":body));
                }
                if(rows.isEmpty()){new AlertDialog.Builder(this).setTitle(filter!=null&&!filter.isEmpty()?"Room invitations":"Notifications").setMessage(filter!=null&&!filter.isEmpty()?"No room invitations yet.":"No notifications yet.").setPositiveButton("OK",null).show();return;}
                new AlertDialog.Builder(this).setTitle(filter!=null&&!filter.isEmpty()?"Room invitations":"Notifications")
                    .setItems(rows.toArray(new String[0]),(d,w)->{
                        if(noticeAccountCurrent10010(noticeAccount10010))openNotification940(docs.get(w));
                    }).setNegativeButton("Close",null).show();
            }).addOnFailureListener(e->{if(!noticeAccountCurrent10010(noticeAccount10010))return;
                new AlertDialog.Builder(this).setTitle("Notifications")
                    .setMessage("Notification feed is unavailable right now.")
                    .setPositiveButton("Retry",(d,w)->showCloudNotifications940(filter))
                    .setNegativeButton("Close",null).show();});
    }
    private boolean noticeAccountCurrent10010(String account){
        com.google.firebase.auth.FirebaseUser current=FirebaseAuth.getInstance().getCurrentUser();
        return !isFinishing()&&!isDestroyed()&&current!=null&&account.equals(current.getUid());
    }
    private void openNotification940(DocumentSnapshot n){
        if(n==null||me==null||!noticeAccountCurrent10010(me.getUid()))return;
        final String noticeOwner10033=me.getUid();
        Object dataObj=n.get("data");
        if(!(dataObj instanceof Map)){
            Toast.makeText(this,"Notification details are unavailable",Toast.LENGTH_SHORT).show();return;
        }
        Map<?,?> data=(Map<?,?>)dataObj;
        String type=data.get("type")==null?"":String.valueOf(data.get("type"));
        // Invalid invitations and messages remain unread for a retry.
        if("room_invite".equals(type) && (data.get("roomId")==null||String.valueOf(data.get("roomId")).trim().isEmpty())){
            Toast.makeText(this,"Invitation is missing its room ID",Toast.LENGTH_SHORT).show();return;
        }
        if("direct_message".equals(type)&& (data.get("senderUid")==null
            ||String.valueOf(data.get("senderUid")).trim().isEmpty()
            ||noticeOwner10033.equals(String.valueOf(data.get("senderUid"))))) {
            Toast.makeText(this,"Notification has no valid sender",Toast.LENGTH_SHORT).show();return;
        }
        if("room_invite".equals(type)){
            String rid=data.get("roomId")==null?"":String.valueOf(data.get("roomId"));if(!KingDocumentId10036.valid(rid)){Toast.makeText(this,"Room invite is missing its room ID",Toast.LENGTH_SHORT).show();return;}
            final String invitingAccount10024=FirebaseAuth.getInstance().getCurrentUser()==null?"":FirebaseAuth.getInstance().getCurrentUser().getUid();
            db.collection("live_rooms").document(rid).get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(room->{if(!noticeAccountCurrent10010(invitingAccount10024))return;if(room==null||!room.exists()||Boolean.TRUE.equals(room.getBoolean("closed"))){Toast.makeText(this,"This Party room is no longer available",Toast.LENGTH_SHORT).show();return;}Intent i=new Intent(this,PartyActivity.class);i.putExtra("directRoomId",rid);i.putExtra("directRoomName",room.getString("name"));i.putExtra("directOwnerUid",room.getString("ownerUid"));i.putExtra("directOwnerName",room.getString("ownerName"));i.putExtra("directPrivate",Boolean.TRUE.equals(room.getBoolean("isPrivate")));i.putExtra("directPassword",Boolean.TRUE.equals(room.getBoolean("hasPassword")));startActivity(i);markNotificationRead10036(n,noticeOwner10033);})
                .addOnFailureListener(error->{if(!noticeAccountCurrent10010(invitingAccount10024))return;
                    new AlertDialog.Builder(this).setTitle("Room unavailable")
                        .setMessage("Could not check this invitation. It remains unread; try again online.")
                        .setPositiveButton("Retry",(dialog,which)->{if(noticeAccountCurrent10010(invitingAccount10024))openNotification940(n);})
                        .setNegativeButton("Close",null).show();});
        }else if("direct_message".equals(type)){
            String uid=data.get("senderUid")==null?"":String.valueOf(data.get("senderUid"));
            if(!KingDocumentId10036.valid(uid)||uid.equals(noticeOwner10033)){
                Toast.makeText(this,"This message does not contain a valid sender",Toast.LENGTH_SHORT).show();return;
            }
            String name=n.getString("title");openChat(name==null?"KING Friend":name,uid);markNotificationRead10036(n,noticeOwner10033);
        }else if("follow".equals(type)){
            String uid=data.get("followerUid")==null?"":String.valueOf(data.get("followerUid"));if(!KingDocumentId10036.valid(uid))return;Intent i=new Intent(this,KingPublicProfileActivity.class);i.putExtra("uid",uid);startActivity(i);markNotificationRead10036(n,noticeOwner10033);
        }else {Toast.makeText(this,n.getString("body")==null?"KING Plus notification":n.getString("body"),Toast.LENGTH_SHORT).show();markNotificationRead10036(n,noticeOwner10033);}
    }
    private void markNotificationRead10036(DocumentSnapshot notice,String owner){
        if(!noticeAccountCurrent10010(owner)||Boolean.TRUE.equals(notice.getBoolean("read")))return;
        notice.getReference().update("read",true).addOnFailureListener(error->{
            if(noticeAccountCurrent10010(owner))Toast.makeText(this,"Could not mark notification read",Toast.LENGTH_SHORT).show();
        });
    }
    private void openSocial(){startActivity(new Intent(this,SocialActivity.class));}

    private void newChatDialog() {
        final EditText name=new EditText(this);name.setHint("Friend name");final EditText uid=new EditText(this);uid.setHint("KING ID, VIP888888 or Firebase UID");
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(18),0,dp(18),0);box.addView(name);box.addView(uid);
        new AlertDialog.Builder(this).setTitle("New message").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Open",(d,w)->{
            String n=name.getText().toString().trim(),id10022=uid.getText().toString().trim();
            if(!KingIdentity964.sixDigits(id10022)&&!KingIdentity964.firebaseUid(id10022)&&!id10022.matches("(?i)VIP[1-9][0-9]{5}")){Toast.makeText(this,"Enter a six-digit KING ID or real Firebase UID",Toast.LENGTH_LONG).show();return;}
            if(me!=null&&id10022.equals(me.getUid())){Toast.makeText(this,"You cannot message yourself",Toast.LENGTH_SHORT).show();return;}
            if(n.isEmpty())n="KING Friend";
            final String requestedName10025=n,requestedUid10025=id10022;
            FirebaseUser requestOwner10025=FirebaseAuth.getInstance().getCurrentUser();
            if(requestOwner10025==null||db==null){Toast.makeText(this,"Sign in to start a real chat",Toast.LENGTH_SHORT).show();return;}
            final String requester10025=requestOwner10025.getUid();
            if(requestedUid10025.matches("(?i)VIP[1-9][0-9]{5}")){
                final String premium=requestedUid10025.substring(3);
                db.collection("king_vip_number_registry").document(premium).get(com.google.firebase.firestore.Source.SERVER)
                    .addOnSuccessListener(reserved->{
                        if(!inboxCurrent10023(requester10025))return;
                        String peerUid=reserved.getString("uid");
                        if(!KingDocumentId10036.valid(peerUid)||peerUid.equals(requester10025)){
                            Toast.makeText(this,"VIP ID not found",Toast.LENGTH_LONG).show();return;}
                        db.collection("king_vip_identity_grants").document(peerUid).get(com.google.firebase.firestore.Source.SERVER)
                            .addOnSuccessListener(grant->{
                                if(!inboxCurrent10023(requester10025))return;
                                if(!peerUid.equals(grant.getString("uid"))||!premium.equals(grant.getString("vipNumber"))){
                                    Toast.makeText(this,"VIP ID not verified",Toast.LENGTH_LONG).show();return;}
                                db.collection("public_profiles").document(peerUid).get(com.google.firebase.firestore.Source.SERVER)
                                    .addOnSuccessListener(profile->{if(inboxCurrent10023(requester10025)&&profile.exists()){
                                        String verified=profile.getString("displayName");
                                        openChat(verified==null||verified.trim().isEmpty()?requestedName10025:verified,peerUid);
                                    }});
                            });
                    }).addOnFailureListener(err->{if(inboxCurrent10023(requester10025))Toast.makeText(this,"VIP lookup unavailable",Toast.LENGTH_LONG).show();});
                return;
            }
            if(KingIdentity964.sixDigits(requestedUid10025)){
                db.collection("public_profiles").whereEqualTo("publicId",requestedUid10025)
                  .limit(2).get(com.google.firebase.firestore.Source.SERVER)
                  .addOnSuccessListener(found->{
                    if(!inboxCurrent10023(requester10025))return;
                    if(found.size()!=1){Toast.makeText(this,"KING ID not found or duplicated",Toast.LENGTH_LONG).show();return;}
                    DocumentSnapshot peer=found.getDocuments().get(0);
                    if(!requestedUid10025.equals(KingPublicId9996.verified(peer.getString("publicId")))
                      ||!KingDocumentId10036.valid(peer.getId()))return;
                    String verified=peer.getString("displayName");
                    openChat(verified==null||verified.trim().isEmpty()?requestedName10025:verified,peer.getId());
                  }).addOnFailureListener(e->{if(inboxCurrent10023(requester10025))
                    Toast.makeText(this,"KING ID lookup unavailable • retry online",Toast.LENGTH_LONG).show();});
                return;
            }
            db.collection("public_profiles").document(requestedUid10025)
                .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(peer10025->{
                    if(!inboxCurrent10023(requester10025))return;
                    if(!peer10025.exists()){
                        Toast.makeText(this,"No KING profile found for this UID",Toast.LENGTH_LONG).show();return;
                    }
                    String verifiedName10025=peer10025.getString("displayName");
                    openChat(verifiedName10025==null||verifiedName10025.trim().isEmpty()
                        ?requestedName10025:verifiedName10025,requestedUid10025);
                }).addOnFailureListener(e->{
                    if(inboxCurrent10023(requester10025))
                        Toast.makeText(this,"Cannot verify this KING UID right now. Retry online.",Toast.LENGTH_LONG).show();
                });}).show();
    }
    private void openChat(String name,String peerUid){
        FirebaseUser current10025=FirebaseAuth.getInstance().getCurrentUser();
        if(me==null||current10025==null||!me.getUid().equals(current10025.getUid())
            ||!KingDocumentId10036.valid(peerUid)||peerUid.equals(me.getUid())){
            Toast.makeText(this,"Reopen Messages with your current KING account",Toast.LENGTH_LONG).show();return;
        }
        Intent i=new Intent(this,ChatActivity.class);i.putExtra("peerName",name);i.putExtra("peerUid",peerUid);startActivity(i);}

    private void addBottomNav(LinearLayout root,int selected){
        LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER);nav.setBackground(KingRoyalTheme10021.bottomNav(this));String[] ni={"⌂\nHome","🎤\nParty","◇\nDiscover","✉\nMessages","●\nMe"};
        for(int i=0;i<ni.length;i++){TextView n=label(ni[i],12,i==selected?0xffd2b100:0xff777777,true);n.setGravity(Gravity.CENTER);final int k=i;n.setOnClickListener(v->{if(k!=3)KingNav.openRoot(this,k);});nav.addView(n,new LinearLayout.LayoutParams(0,dp(62),1));}root.addView(nav);
    }

    @Override public void onBackPressed(){if(isTaskRoot())KingNav.confirmExit(this);else finish();}

    @Override protected void onStop(){
        // Release hidden page listeners; resume will subscribe with a fresh generation.
        ++inboxListenerGeneration10030;
        if(threadListener!=null){threadListener.remove();threadListener=null;}
        super.onStop();
    }
    @Override protected void onStart(){
        super.onStart();
        FirebaseUser current10031=FirebaseAuth.getInstance().getCurrentUser();
        if(me==null?(current10031!=null)
            :current10031==null||!me.getUid().equals(current10031.getUid()))
            activeSearch10029="";
        if(inboxStartedOnce10031)loadInbox();
        inboxStartedOnce10031=true;
    }
    private boolean inboxStartedOnce10031;
    @Override protected void onDestroy(){
        ++inboxListenerGeneration10030;
        KingNetwork.unwatch(this,inboxNetworkCallback940);inboxNetworkCallback940=null;
        if(threadListener!=null){threadListener.remove();threadListener=null;}
        super.onDestroy();
    }
}
