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
    private static final int BG = 0xfffbfafc;
    private static final int PURPLE = 0xff8a49ed;
    private static final int DARK = 0xff17131f;
    private static final int MUTED = 0xff817b86;

    private LinearLayout list;
    private ListenerRegistration threadListener;
    private FirebaseUser me;
    private FirebaseFirestore db;
    private final ArrayList<ThreadModel> models = new ArrayList<>();

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
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(BG);

        LinearLayout head = new LinearLayout(this); head.setGravity(Gravity.CENTER_VERTICAL); head.setPadding(dp(18),dp(10),dp(8),dp(4));
        TextView title = label("Messages",27,DARK,true); head.addView(title,new LinearLayout.LayoutParams(0,dp(58),1));
        TextView search = label("⌕",29,DARK,false); search.setGravity(Gravity.CENTER); search.setOnClickListener(v->showSearch()); head.addView(search,new LinearLayout.LayoutParams(dp(50),dp(54)));
        TextView plus = label("＋",29,DARK,false); plus.setGravity(Gravity.CENTER); plus.setOnClickListener(v->newChatDialog()); head.addView(plus,new LinearLayout.LayoutParams(dp(50),dp(54)));
        root.addView(head);

        LinearLayout quick = new LinearLayout(this); quick.setGravity(Gravity.CENTER); quick.setPadding(dp(12),dp(2),dp(12),dp(8));
        addQuick(quick,"👤","Followers",()->openSocial());
        addQuick(quick,"👥","Friends",()->openSocial());
        addQuick(quick,"🔔","Notifications",this::showNotifications);
        root.addView(quick,new LinearLayout.LayoutParams(-1,dp(92)));

        View dividerTop = new View(this); dividerTop.setBackgroundColor(0xffeeecef); root.addView(dividerTop,new LinearLayout.LayoutParams(-1,dp(1)));

        ScrollView sv = new ScrollView(this); list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); list.setPadding(0,0,0,dp(16)); sv.addView(list);
        root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        addBottomNav(root,3);
        setContentView(root);
    }

    private void addQuick(LinearLayout host,String icon,String text,Runnable action){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setGravity(Gravity.CENTER);
        TextView av=label(icon,25,DARK,false);av.setGravity(Gravity.CENTER);av.setBackground(bg(0xffffedf4,28));box.addView(av,new LinearLayout.LayoutParams(dp(52),dp(52)));
        TextView name=label(text,12,0xff514b55,true);name.setGravity(Gravity.CENTER);box.addView(name,new LinearLayout.LayoutParams(-1,dp(28)));
        box.setOnClickListener(v->action.run());host.addView(box,new LinearLayout.LayoutParams(0,dp(84),1));
    }

    private void loadInbox() {
        models.clear();
        Set<String> local = getSharedPreferences("chat_store", MODE_PRIVATE).getStringSet("recent_names", new HashSet<>());
        for (String name : local) if (name != null && !name.trim().isEmpty()) models.add(new ThreadModel("●",name,"Tap to continue chatting","","",false));
        renderModels(models);

        if (me == null || db == null) return;
        threadListener = db.collection("direct_threads").whereArrayContains("members", me.getUid())
            .addSnapshotListener((snap,error)->{
                if(error!=null){Toast.makeText(this,"Messages could not refresh",Toast.LENGTH_SHORT).show();return;}
                renderCloudThreads(snap);
            });
    }

    private void renderCloudThreads(QuerySnapshot snap) {
        models.clear();
        if (snap != null) {
            List<DocumentSnapshot> docs = new ArrayList<>(snap.getDocuments());
            Collections.sort(docs,(a,b)->Long.compare(timeOf(b),timeOf(a)));
            Set<String> seen = new HashSet<>();
            for (DocumentSnapshot doc : docs) {
                List<String> members = (List<String>) doc.get("members"); if (members == null) continue;
                String peerUid = null; for(String uid:members) if(!me.getUid().equals(uid)){peerUid=uid;break;} if(peerUid==null) continue;
                String peerName=peerUid; Object namesObj=doc.get("memberNames");
                if(namesObj instanceof Map){Object n=((Map<?,?>)namesObj).get(peerUid);if(n!=null)peerName=String.valueOf(n);}
                if(!seen.add(peerUid))continue;
                String last=doc.getString("lastMessage");if(last==null||last.trim().isEmpty())last="Open conversation";
                Timestamp updated=doc.getTimestamp("updatedAt");
                models.add(new ThreadModel(initial(peerName),peerName,last,peerUid,formatTime(updated),isUnread(doc)));
            }
        }
        Set<String> local = getSharedPreferences("chat_store", MODE_PRIVATE).getStringSet("recent_names", new HashSet<>());
        for(String name:local){boolean found=false;for(ThreadModel m:models)if(m.name.equalsIgnoreCase(name)){found=true;break;}if(!found)models.add(new ThreadModel(initial(name),name,"Tap to continue chatting","","",false));}
        renderModels(models);
    }

    private long timeOf(DocumentSnapshot d){Timestamp t=d.getTimestamp("updatedAt");return t==null?0:t.toDate().getTime();}
    private String formatTime(Timestamp t){
        if(t==null)return ""; Date d=t.toDate(); long diff=System.currentTimeMillis()-d.getTime();
        if(diff<24L*60*60*1000)return new SimpleDateFormat("HH:mm",Locale.getDefault()).format(d);
        if(diff<48L*60*60*1000)return "Yesterday";
        return new SimpleDateFormat("dd/MM",Locale.getDefault()).format(d);
    }
    private String initial(String name){if(name==null||name.trim().isEmpty())return "●";return name.trim().substring(0,1).toUpperCase(Locale.getDefault());}

    private void renderModels(List<ThreadModel> source){
        if(list==null)return; list.removeAllViews();
        if(source==null||source.isEmpty()){
            LinearLayout empty=new LinearLayout(this);empty.setOrientation(LinearLayout.VERTICAL);empty.setGravity(Gravity.CENTER);empty.setPadding(dp(24),dp(70),dp(24),dp(24));
            TextView icon=label("💬",52,0xffbbb4c1,false);icon.setGravity(Gravity.CENTER);empty.addView(icon,new LinearLayout.LayoutParams(-1,dp(72)));
            TextView t=label("No messages yet",18,DARK,true);t.setGravity(Gravity.CENTER);empty.addView(t);
            TextView s=label(me==null?"Sign in to sync real conversations":"Start a chat from Discover or Friends",13,MUTED,false);s.setGravity(Gravity.CENTER);empty.addView(s);
            list.addView(empty,new LinearLayout.LayoutParams(-1,dp(230)));return;
        }
        for(ThreadModel m:source)addThreadRow(m);
    }

    private void addThreadRow(ThreadModel m){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(16),dp(10),dp(14),dp(10));row.setBackgroundColor(Color.WHITE);
        TextView avatar=label(m.icon,20,Color.WHITE,true);avatar.setGravity(Gravity.CENTER);avatar.setBackground(bg(0xffa86af1,28));row.addView(avatar,new LinearLayout.LayoutParams(dp(52),dp(52)));
        LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);info.setPadding(dp(12),0,dp(6),0);
        TextView n=label(m.name,16,DARK,true);info.addView(n,new LinearLayout.LayoutParams(-1,dp(27)));
        TextView p=label(m.preview,13,m.unread?0xff3a3540:MUTED,m.unread);p.setSingleLine(true);info.addView(p,new LinearLayout.LayoutParams(-1,dp(25)));
        row.addView(info,new LinearLayout.LayoutParams(0,dp(54),1));
        LinearLayout meta=new LinearLayout(this);meta.setOrientation(LinearLayout.VERTICAL);meta.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        TextView tm=label(m.time,11,0xffa09aa5,false);tm.setGravity(Gravity.RIGHT);meta.addView(tm,new LinearLayout.LayoutParams(dp(72),dp(23)));
        TextView dot=label(m.unread?"●":"",14,PURPLE,true);dot.setGravity(Gravity.RIGHT);meta.addView(dot,new LinearLayout.LayoutParams(dp(72),dp(25)));
        row.addView(meta,new LinearLayout.LayoutParams(dp(78),dp(52)));
        row.setOnClickListener(v->openChat(m.name,m.peerUid)); list.addView(row,new LinearLayout.LayoutParams(-1,dp(74)));
        View line=new View(this);line.setBackgroundColor(0xffefedf1);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(1));lp.leftMargin=dp(80);list.addView(line,lp);
    }

    private boolean isUnread(DocumentSnapshot doc) {
        if (me == null) return false; String lastSender=doc.getString("lastSenderUid"); if(me.getUid().equals(lastSender))return false;
        Timestamp updated=doc.getTimestamp("updatedAt");if(updated==null)return false;Object readObj=doc.get("readAt");if(!(readObj instanceof Map))return true;
        Object own=((Map<?,?>)readObj).get(me.getUid());return !(own instanceof Timestamp)||((Timestamp)own).compareTo(updated)<0;
    }

    private void showSearch(){
        final EditText q=new EditText(this);q.setHint("Search messages");q.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("Search").setView(q).setNegativeButton("Cancel",null).setPositiveButton("Search",(d,w)->{
            String s=q.getText().toString().trim().toLowerCase(Locale.getDefault());ArrayList<ThreadModel> filtered=new ArrayList<>();
            for(ThreadModel m:models)if(s.isEmpty()||m.name.toLowerCase(Locale.getDefault()).contains(s)||m.preview.toLowerCase(Locale.getDefault()).contains(s))filtered.add(m);
            renderModels(filtered);
        }).setNeutralButton("Show all",(d,w)->renderModels(models)).show();
    }

    private void showNotifications(){
        String[] items={"Follow activity","Likes & gifts","Room invitations","KING Plus updates"};
        new AlertDialog.Builder(this).setTitle("Notifications").setItems(items,(d,w)->Toast.makeText(this,items[w],Toast.LENGTH_SHORT).show()).setNegativeButton("Close",null).show();
    }
    private void openSocial(){startActivity(new Intent(this,SocialActivity.class));}

    private void newChatDialog() {
        final EditText name=new EditText(this);name.setHint("Friend name");final EditText uid=new EditText(this);uid.setHint("KING/Firebase UID (optional)");
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(18),0,dp(18),0);box.addView(name);box.addView(uid);
        new AlertDialog.Builder(this).setTitle("New message").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Open",(d,w)->{
            String n=name.getText().toString().trim();if(n.isEmpty())n="New Friend";openChat(n,uid.getText().toString().trim());}).show();
    }
    private void openChat(String name,String peerUid){Intent i=new Intent(this,ChatActivity.class);i.putExtra("peerName",name);i.putExtra("peerUid",peerUid);startActivity(i);}

    private void addBottomNav(LinearLayout root,int selected){
        LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER);nav.setBackgroundColor(Color.WHITE);String[] ni={"⌂\nParty","♟\nGame","◇\nDiscover","✉\nMessages","●\nMe"};
        for(int i=0;i<ni.length;i++){TextView n=label(ni[i],12,i==selected?PURPLE:0xff777777,true);n.setGravity(Gravity.CENTER);final int k=i;n.setOnClickListener(v->{if(k==3)return;Intent x=new Intent(this,MainActivity.class);x.putExtra("openTab",k);startActivity(x);finish();});nav.addView(n,new LinearLayout.LayoutParams(0,dp(62),1));}root.addView(nav);
    }

    @Override protected void onDestroy(){if(threadListener!=null)threadListener.remove();super.onDestroy();}
}
