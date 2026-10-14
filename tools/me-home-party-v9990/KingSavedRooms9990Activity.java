package com.kingplus.social;
import android.app.*;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;
import java.util.*;

/** One live saved-room screen shared by Me and Home/Party. */
public final class KingSavedRooms9990Activity extends Activity {
    private FirebaseFirestore db;private String uid;private ListenerRegistration listener;
    private LinearLayout list;private TextView state,favTab,recentTab;private boolean favorites=true;
    private Object records;private int generation;private boolean loaded,cached;
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private TextView text(String label,int size){TextView t=new TextView(this);t.setText(label);t.setTextSize(size);t.setTextColor(0xfff5eaff);t.setPadding(dp(12),dp(10),dp(12),dp(10));return t;}
    @Override public void onCreate(Bundle b){
        super.onCreate(b);FirebaseUser me=FirebaseAuth.getInstance().getCurrentUser();
        if(me==null){Toast.makeText(this,"Sign in to view saved rooms",Toast.LENGTH_LONG).show();finish();return;}
        uid=me.getUid();db=FirebaseFirestore.getInstance();favorites=!"recent".equals(getIntent().getStringExtra("tab"));
        LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(0xff1b102c);
        TextView title=text("‹  My Party Rooms",23);title.setOnClickListener(v->finish());root.addView(title);
        LinearLayout tabs=new LinearLayout(this);favTab=text("★ Favorites",17);recentTab=text("◷ Recent",17);
        favTab.setOnClickListener(v->{favorites=true;render();});recentTab.setOnClickListener(v->{favorites=false;render();});
        tabs.addView(favTab,new LinearLayout.LayoutParams(0,dp(54),1));tabs.addView(recentTab,new LinearLayout.LayoutParams(0,dp(54),1));root.addView(tabs);
        state=text("Loading your saved rooms…",12);state.setOnClickListener(v->listen());root.addView(state);
        ScrollView scroll=new ScrollView(this);list=new LinearLayout(this);list.setOrientation(1);scroll.addView(list);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        TextView party=text("Open Party Home  ›",17);party.setGravity(Gravity.CENTER);party.setOnClickListener(v->KingNav.openRoot(this,0));root.addView(party);
        setContentView(root);ViewCompat.setOnApplyWindowInsetsListener(root,(v,i)->{Insets x=i.getInsets(WindowInsetsCompat.Type.systemBars());v.setPadding(0,x.top,0,x.bottom);return i;});ViewCompat.requestApplyInsets(root);
    }
    private void stop(){++generation;if(listener!=null){listener.remove();listener=null;}}
    private void listen(){
        stop();if(uid==null||!KingSavedRoomsCloud9990.account(uid)){finish();return;}final int g=generation;
        state.setText("Loading your saved rooms…");
        listener=db.collection("users").document(uid).addSnapshotListener(MetadataChanges.INCLUDE,(doc,error)->{
            if(g!=generation||isFinishing()||isDestroyed()||!KingSavedRoomsCloud9990.account(uid))return;
            if(error!=null||doc==null){state.setText("Saved rooms unavailable • tap to retry");return;}
            records=doc.get(KingSavedRoomsPolicy9990.FIELD);
            if(!doc.contains(KingSavedRoomsPolicy9990.FIELD)){
                records=KingSavedRoomsCloud9990.local(this,uid);
                if(!doc.getMetadata().isFromCache())KingSavedRoomsCloud9990.importLocal(this,db,uid);
            }
            loaded=true;cached=doc.getMetadata().isFromCache();
            KingSavedRoomsCloud9990.cache(this,uid,records);render();
        });
    }
    private void render(){
        favTab.setBackgroundColor(favorites?0xff6e42a5:Color.TRANSPARENT);recentTab.setBackgroundColor(favorites?Color.TRANSPARENT:0xff6e42a5);list.removeAllViews();
        if(!loaded)return;state.setText(cached?"Offline copy • reconnect to sync":"Synced to your account • available on your other phone");
        List<KingSavedRoomsPolicy9990.Room> rooms=KingSavedRoomsPolicy9990.rows(records,favorites);
        if(rooms.isEmpty()){list.addView(text(favorites?"No favorites yet. Tap ★ inside a Party room.":"Join a Party to add it to Recent.",15));return;}
        for(KingSavedRoomsPolicy9990.Room room:rooms){
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);
            String date=room.visited>0?"\n"+java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT,java.text.DateFormat.SHORT).format(new Date(room.visited)):"";
            TextView name=text(room.name+date,16);row.addView(name,new LinearLayout.LayoutParams(0,-2,1));
            name.setOnClickListener(v->{Intent i=new Intent(this,PartyActivity.class);i.putExtra("directRoomId",room.id);i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);startActivity(i);});
            TextView star=text(room.favorite?"★":"☆",27);star.setContentDescription(room.favorite?"Remove favorite":"Save favorite");
            star.setOnClickListener(v->{star.setEnabled(false);KingSavedRoomsCloud9990.change(this,db,uid,room.id,room.name,true,error->{star.setEnabled(true);if(error!=null)new AlertDialog.Builder(this).setTitle("Could not save favorite").setMessage("Check your connection and try again. You can save up to 60 favorites.").setPositiveButton("OK",null).show();});});
            row.addView(star,new LinearLayout.LayoutParams(dp(64),dp(64)));list.addView(row);
        }
    }
    @Override protected void onStart(){super.onStart();if(uid!=null)listen();}
    @Override protected void onStop(){stop();super.onStop();}
}
