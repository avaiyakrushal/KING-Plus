package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

/** Native controls for KING Plus voice-room seats and host moderation. */
public class RoomControlActivity extends Activity {
    private final RoomSeatManager seats=new RoomSeatManager();
    private String roomId,uid,name,ownerUid;
    private int mySeat=1;
    private boolean muted=false,isHost=false,isCoHost=false;
    private LinearLayout root,requestsBox;
    private GridLayout seatGrid;
    private ListenerRegistration seatListener,requestListener;

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        roomId=getIntent().getStringExtra("roomId"); if(roomId==null||roomId.trim().isEmpty()) roomId="lobby";
        uid=FirebaseAuth.getInstance().getUid(); if(uid==null) uid="guest-"+System.currentTimeMillis();
        name=getIntent().getStringExtra("name"); if(name==null||name.trim().isEmpty()) name="KING User";
        ownerUid=getIntent().getStringExtra("ownerUid"); isHost=ownerUid!=null&&ownerUid.equals(uid);
        loadRoleAndRender();
    }
    private void loadRoleAndRender(){ FirebaseFirestore.getInstance().collection("live_rooms").document(roomId).collection("roles").document(uid).get().addOnSuccessListener(doc->{String role=doc.getString("role");isCoHost="cohost".equals(role)||"admin".equals(role);render();}).addOnFailureListener(e->render()); }
    private void render(){
        ScrollView scroll=new ScrollView(this); root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24,36,24,36); scroll.addView(root); setContentView(scroll);
        TextView title=new TextView(this); title.setText("KING Plus Voice Room\nRole: "+(isHost?"Host":isCoHost?"Co-host":"Member")); title.setTextSize(22); title.setGravity(Gravity.CENTER); root.addView(title,new LinearLayout.LayoutParams(-1,-2));
        TextView seatsTitle=new TextView(this); seatsTitle.setText("8 Voice Seats"); seatsTitle.setTextSize(18); seatsTitle.setPadding(0,24,0,8); root.addView(seatsTitle);
        seatGrid=new GridLayout(this); seatGrid.setColumnCount(2); root.addView(seatGrid,new LinearLayout.LayoutParams(-1,-2)); renderEmptySeats();
        add("Request Seat",v->seats.requestSeat(roomId,uid,name,this::result));
        add("Mic Mute / Unmute",v->{muted=!muted;seats.setMuted(roomId,mySeat,muted,this::result);});
        add("Leave My Seat",v->seats.leaveSeat(roomId,mySeat,this::result));
        if(isHost||isCoHost){ TextView r=new TextView(this);r.setText("Pending Seat Requests");r.setTextSize(18);r.setPadding(0,24,0,8);root.addView(r);requestsBox=new LinearLayout(this);requestsBox.setOrientation(LinearLayout.VERTICAL);root.addView(requestsBox);add("Kick / Ban User",v->moderationDialog()); }
        if(isHost) add("Assign Co-host",v->roleDialog());
        add("Back",v->finish()); startListeners();
    }
    private void renderEmptySeats(){ seatGrid.removeAllViews(); for(int i=1;i<=8;i++) addSeatCell(i,"Empty",false); }
    private void addSeatCell(int index,String occupant,boolean isMuted){ Button b=new Button(this); b.setAllCaps(false); b.setText("Seat "+index+"\n"+occupant+(isMuted?" 🔇":"")); b.setOnClickListener(v->{mySeat=index;if("Empty".equals(occupant)) seats.setSeat(roomId,index,uid,name,muted,this::result); else if(isHost||isCoHost) seatActionDialog(index);}); GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(6,6,6,6);seatGrid.addView(b,p); }
    private void startListeners(){
        seatListener=seats.listen(roomId,(snap,error)->{ if(error!=null||snap==null)return; runOnUiThread(()->{renderEmptySeats(); java.util.Map<Integer,DocumentSnapshot> map=new java.util.HashMap<>();for(DocumentSnapshot d:snap.getDocuments()){Long n=d.getLong("index");if(n!=null)map.put(n.intValue(),d);}seatGrid.removeAllViews();for(int i=1;i<=8;i++){DocumentSnapshot d=map.get(i);if(d==null)addSeatCell(i,"Empty",false);else{String n=d.getString("name");Boolean m=d.getBoolean("muted");addSeatCell(i,n==null?"KING User":n,Boolean.TRUE.equals(m));}}});});
        if(isHost||isCoHost) requestListener=seats.listenRequests(roomId,(snap,error)->{if(error!=null||snap==null)return;runOnUiThread(()->{requestsBox.removeAllViews();for(DocumentSnapshot d:snap.getDocuments()){String rid=d.getString("uid"),rn=d.getString("name");Button b=new Button(this);b.setAllCaps(false);b.setText((rn==null?"User":rn)+"\nApprove / Reject");b.setOnClickListener(v->requestDialog(rid,rn));requestsBox.addView(b,new LinearLayout.LayoutParams(-1,-2));}if(snap.isEmpty()){TextView e=new TextView(this);e.setText("No pending requests");requestsBox.addView(e);}});});
    }
    private void requestDialog(String rid,String rn){ final String[] choices={"Approve to seat 1","Approve to seat 2","Approve to seat 3","Approve to seat 4","Approve to seat 5","Approve to seat 6","Approve to seat 7","Approve to seat 8","Reject"}; new AlertDialog.Builder(this).setTitle(rn==null?"Seat request":rn).setItems(choices,(d,which)->{if(which==8)seats.rejectRequest(roomId,rid,this::result);else seats.approveRequest(roomId,rid,rn==null?"KING User":rn,which+1,this::result);}).show(); }
    private void seatActionDialog(int index){ new AlertDialog.Builder(this).setTitle("Seat "+index).setItems(new String[]{"Lock seat","Unlock seat","Mute seat","Unmute seat"},(d,w)->{if(w==0)seats.lockSeat(roomId,index,true,this::result);else if(w==1)seats.lockSeat(roomId,index,false,this::result);else seats.setMuted(roomId,index,w==2,this::result);}).show(); }
    private void moderationDialog(){ EditText target=new EditText(this);target.setHint("Firebase UID to ban");new AlertDialog.Builder(this).setTitle("Kick / Ban from room").setView(target).setNegativeButton("Cancel",null).setPositiveButton("Ban",(d,w)->{String id=target.getText().toString().trim();if(id.isEmpty()||id.equals(uid)){result(false,"Enter another user's UID");return;}seats.banFromRoom(roomId,id,"Host moderation",this::result);}).show(); }
    private void roleDialog(){ EditText target=new EditText(this);target.setHint("Firebase UID for co-host");new AlertDialog.Builder(this).setTitle("Assign Co-host").setView(target).setNegativeButton("Cancel",null).setPositiveButton("Assign",(d,w)->{String id=target.getText().toString().trim();if(id.isEmpty()){result(false,"Enter user UID");return;}seats.setRole(roomId,id,"cohost",this::result);}).show(); }
    private void add(String text,View.OnClickListener click){Button b=new Button(this);b.setText(text);b.setOnClickListener(click);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=12;root.addView(b,p);}
    private void result(boolean ok,String message){runOnUiThread(()->Toast.makeText(this,(ok?"✓ ":"⚠ ")+message,Toast.LENGTH_SHORT).show());}
    @Override protected void onDestroy(){if(seatListener!=null)seatListener.remove();if(requestListener!=null)requestListener.remove();super.onDestroy();}
}
