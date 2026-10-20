package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Source;
import java.util.*;

/**
 * Me → real cloud privacy / notifications / blocked users.
 *
 * The 10 preferences are ACCOUNT-SYNCED, not a promise that every back-end
 * push producer respects them. Backend-enforced: new direct messages respect
 * recipient allowPrivateMessages, Party invites respect allowRoomInvites, and
 * both account block directions are checked. Discovery, presence and display
 * toggles are synced preferences and may not be enforced by every legacy surface.
 */
public final class KingMePrivacy9984Activity extends Activity {
 private FirebaseFirestore db;
 private FirebaseUser account;
 private DocumentReference prefDoc;
 private String uid="",page="privacy";
 private LinearLayout root;
 private boolean busy=false;
 private Map<String,Object> values=new HashMap<>();
 private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 private TextView label(String value,int size){
  TextView v=new TextView(this);v.setText(value);v.setTextSize(size);
  v.setTextColor(Color.WHITE);v.setPadding(dp(12),dp(12),dp(12),dp(12));
  return v;
 }
 private void toast(String value){Toast.makeText(this,value,Toast.LENGTH_LONG).show();}
 private Button button(String title,Runnable clicked){
  Button b=new Button(this);b.setText(title);b.setAllCaps(false);
  root.addView(b,new LinearLayout.LayoutParams(-1,dp(54)));
  b.setOnClickListener(v->{if(!busy)clicked.run();});return b;
 }
 private boolean correctAccount(){
  FirebaseUser current=FirebaseAuth.getInstance().getCurrentUser();
  return !uid.isEmpty()&&current!=null&&uid.equals(current.getUid());
 }
 @Override public void onCreate(Bundle b){
  super.onCreate(b);db=FirebaseFirestore.getInstance();
  account=FirebaseAuth.getInstance().getCurrentUser();
  if(account!=null)uid=account.getUid();
  String requested=getIntent().getStringExtra("page");
  if("notifications".equals(requested)||"blocked".equals(requested))page=requested;
  if(!uid.isEmpty())prefDoc=db.collection("king_me_preferences").document(uid);
  showLoading();if(correctAccount())refresh();
 }
 private void reset(){
  ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(0xff19152e);
  root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
  root.setPadding(dp(12),dp(16),dp(12),dp(40));scroll.addView(root);
  setContentView(scroll);
  TextView head=label("‹ KING Plus • Me Privacy & Notifications",21);
  head.setTextColor(0xffffdf87);head.setOnClickListener(v->finish());
  root.addView(head);
 }
 private void showLoading(){
  reset();root.addView(label(correctAccount()?"Loading verified account settings…":
      "Sign in to manage cloud privacy and block settings.",15));
 }
 private void refresh(){
  if(!correctAccount()){showLoading();return;}
  prefDoc.get(Source.SERVER).addOnSuccessListener(d->{
   if(!correctAccount()||isFinishing()||isDestroyed())return;
   values=new HashMap<>(KingMePrivacyPolicy9984.defaults(uid));
   if(d.exists())for(String key:values.keySet())
      if(KingMePrivacyPolicy9984.permitted(key)&&d.get(key) instanceof Boolean)
          values.put(key,d.getBoolean(key));
   render();
  }).addOnFailureListener(err->{
    if(isFinishing()||isDestroyed())return;
    reset();root.addView(label("Cloud privacy unavailable. No local fake changes were made.",15));
    button("Retry online",this::refresh);
  });
 }
 private void render(){
  if(!correctAccount())return;
  reset();
  LinearLayout tabs=new LinearLayout(this);tabs.setOrientation(LinearLayout.HORIZONTAL);
  root.addView(tabs,new LinearLayout.LayoutParams(-1,dp(55)));
  for(String target:new String[]{"privacy","notifications","blocked"}){
   Button b=new Button(this);b.setAllCaps(false);b.setText(target.equals("privacy")?"Privacy":
           target.equals("notifications")?"Alerts":"Blocked");
   LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,1);
   tabs.addView(b,lp);b.setOnClickListener(v->{page=target;render();});
  }
  switch(page){
   case "notifications":renderNotifications();break;
   case "blocked":renderBlocked();break;
   default:renderPrivacy();break;
  }
 }
 private void renderPrivacy(){
   root.addView(label("🛡 Account Privacy — synced across signed-in devices",18));
   root.addView(label("New private messages and Cloud Party invitations respect their saved permissions and your Block List. Find-me, online-status and entrance-display preferences sync online but are not enforced on every legacy surface yet.",13));
   for(int i=0;i<KingMePrivacyPolicy9984.PRIVACY.length;i++)
      toggle(KingMePrivacyPolicy9984.PRIVACY[i],KingMePrivacyPolicy9984.PRIVACY_LABELS[i]);
   button("🚫 Review / unblock accounts",()->{page="blocked";render();});
 }
 private void renderNotifications(){
   root.addView(label("🔔 Notification preferences — saved to your Firebase account",18));
   root.addView(label("When Cloud Functions are deployed, message, follow, room-invite and gift alerts use these saved choices. Android notification permission is controlled separately below.",13));
   for(int i=0;i<KingMePrivacyPolicy9984.NOTIFY.length;i++)
      toggle(KingMePrivacyPolicy9984.NOTIFY[i],KingMePrivacyPolicy9984.NOTIFY_LABELS[i]);
   button("⚙ Android notification permission",()->{
    try{Intent i=new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
      i.putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName());startActivity(i);}
    catch(Exception ignored){toast("Open Android Settings → Apps → KING Plus → Notifications");}
   });
 }
 private void toggle(String key,String title){
  boolean current=Boolean.TRUE.equals(values.get(key));
  button((current?"🟢 ":"⚪ ")+title+"  • "+(current?"ON":"OFF"),
    ()->saveOption(key,!current));
 }
 private void saveOption(String key,boolean next){
  if(!correctAccount()||!KingMePrivacyPolicy9984.permitted(key))return;
  busy=true;
  prefDoc.get(Source.SERVER).addOnSuccessListener(snapshot->{
    if(!correctAccount()){busy=false;return;}
    if(snapshot.exists()){
      Map<String,Object> fields=new HashMap<>();
      fields.put(key,next);fields.put("updatedAt",FieldValue.serverTimestamp());
      prefDoc.update(fields).addOnSuccessListener(v->{busy=false;refresh();})
        .addOnFailureListener(err->{busy=false;toast("Cloud setting not saved: "+err.getMessage());refresh();});
    }else{
      Map<String,Object> data=KingMePrivacyPolicy9984.defaults(uid);
      data.put(key,next);data.put("updatedAt",FieldValue.serverTimestamp());
      prefDoc.set(data).addOnSuccessListener(v->{busy=false;refresh();})
        .addOnFailureListener(err->{busy=false;toast("Cloud setting not saved: "+err.getMessage());refresh();});
    }
  }).addOnFailureListener(err->{busy=false;toast("Could not verify account settings. Retry online.");});
 }
 private void renderBlocked(){
   root.addView(label("🚫 Real account Block List",18));
   root.addView(label("Blocks are saved to Firebase. New private messages from/to blocked accounts are denied by Server Rules. Blocking does not retroactively delete conversations or kick users from Party rooms.",13));
   final EditText entry=new EditText(this);entry.setTextColor(Color.WHITE);
   entry.setHintTextColor(0xffc0b5d8);entry.setHint("Full Firebase KING account UID");
   root.addView(entry,new LinearLayout.LayoutParams(-1,dp(56)));
   button("Block this Account UID",()->{
     String target=entry.getText().toString().trim();
     if(!KingMePrivacyPolicy9984.validUid(target)||target.equals(uid)){
       toast("Enter a different full Firebase UID");return;
     }
     new AlertDialog.Builder(this).setTitle("Block user?")
       .setMessage("Reject future direct messages between these accounts.")
       .setNegativeButton("Cancel",null)
       .setPositiveButton("Block",(d,w)->block(target)).show();
   });
   button("↻ Refresh blocked users",this::render);
   final LinearLayout listing=new LinearLayout(this);listing.setOrientation(LinearLayout.VERTICAL);
   root.addView(listing);
   listing.addView(label("Loading your blocked accounts…",14));
   final String owner=uid;
   db.collection("blocks").whereEqualTo("ownerUid",owner).limit(100)
     .get(Source.SERVER).addOnSuccessListener(result->{
      if(!correctAccount()||!owner.equals(uid)||!"blocked".equals(page)||
          isFinishing()||isDestroyed())return;
      listing.removeAllViews();
      if(result.isEmpty())listing.addView(label("No blocked users in your cloud account.",14));
      for(DocumentSnapshot item:result.getDocuments()){
        if(!owner.equals(item.getString("ownerUid")))continue;
        String target=item.getString("targetUid");
        if(!KingMePrivacyPolicy9984.validUid(target))continue;
        Button button=new Button(this);button.setText("🚫 "+target+"   • Unblock");
        button.setAllCaps(false);listing.addView(button);
        button.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Unblock user?")
            .setMessage(target).setNegativeButton("Cancel",null)
            .setPositiveButton("Unblock",(d,w)->unblock(item.getReference())).show());
      }
     }).addOnFailureListener(e->{
        listing.removeAllViews();listing.addView(label("Blocked users unavailable. Retry online.",14));
     });
 }
 private void block(String target){
   if(!correctAccount()||busy)return;
   busy=true;
   final String owner=uid;
   Map<String,Object> data=new HashMap<>();
   data.put("ownerUid",owner);data.put("targetUid",target);
   data.put("createdAt",FieldValue.serverTimestamp());
   db.collection("blocks").document(owner+"_"+target).set(data)
     .addOnSuccessListener(v->{busy=false;if(correctAccount())render();})
     .addOnFailureListener(e->{busy=false;toast("Block not saved: "+e.getMessage());});
 }
 private void unblock(DocumentReference ref){
  if(!correctAccount()||busy)return;
  busy=true;
  ref.delete().addOnSuccessListener(v->{busy=false;if(correctAccount())render();})
    .addOnFailureListener(e->{busy=false;toast("Unblock not saved: "+e.getMessage());});
 }
}
