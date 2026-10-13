package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.content.Intent;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import com.google.firebase.Timestamp;
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
 * KING Plus agency management and recharge-order portal.
 *
 * SECURITY: An order is NOT a Diamond credit. No client code here may write
 * wallets, wallet_ledger, or VIP. Only approved merchant + trusted backend
 * verification can perform actual financial credits. No payment is collected.
 */
public final class KingAgency9982Activity extends Activity {
    private FirebaseFirestore db;
    private FirebaseUser user;
    private String uid="";
    private boolean admin, activeAgency, activeAgent, busy;
    private String agencyUid="";
    private LinearLayout content;
    private TextView info,roles;
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
    private TextView text(String value,int size){
        TextView t=new TextView(this);t.setText(value);t.setTextColor(Color.WHITE);
        t.setTextSize(size);t.setPadding(dp(10),dp(10),dp(10),dp(10));return t;
    }
    private void line(String message){content.addView(text(message,14));}
    private Button button(String label, Runnable task){
        Button b=new Button(this);b.setAllCaps(false);b.setText(label);b.setTextSize(14);
        content.addView(b,new LinearLayout.LayoutParams(-1,dp(54)));
        b.setOnClickListener(v->{if(!busy)task.run();});return b;
    }
    private void title(String value){
        TextView h=text(value,19);h.setTextColor(0xffffdd72);h.setTypeface(null,1);
        content.addView(h);
    }
    private EditText field(String hint) {
        EditText input=new EditText(this);input.setSingleLine(true);input.setTextColor(Color.WHITE);
        input.setHintTextColor(0xffbbb3d0);input.setTextSize(14);input.setHint(hint);
        content.addView(input,new LinearLayout.LayoutParams(-1,dp(52)));return input;
    }
    private void notice(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    private void saving(boolean loading){busy=loading;if(info!=null)info.setText(loading?"Saving securely…":"No payments / Diamonds are credited by this portal.");}
    private String safeId(String raw){
        if(raw==null)return null;String s=raw.trim();
        if(!KingAgencyPolicy9982.validUid(s))return null;return s;
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        db=FirebaseFirestore.getInstance();
        user=FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null)uid=user.getUid();
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.setBackgroundColor(0xff19152d);
        content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(14),dp(14),dp(14),dp(32));sc.addView(content);setContentView(sc);
        if(uid.isEmpty()){title("Agency & Agent Recharge");line("Sign in to your KING Plus account first.");return;}
        user.getIdTokenResult(true).addOnSuccessListener(token->{
            Object flag=token.getClaims().get("admin");
            admin=Boolean.TRUE.equals(flag);
            refresh();
        }).addOnFailureListener(e->{admin=false;refresh();});
    }
    private void refresh(){
        db.collection("king_agencies").document(uid).get(Source.SERVER).addOnSuccessListener(myAgency->{
            activeAgency=myAgency.exists()&&Boolean.TRUE.equals(myAgency.getBoolean("active"));
            if(activeAgency)agencyUid=uid;
            db.collection("king_agents").document(uid).get(Source.SERVER).addOnSuccessListener(myAgent->{
                activeAgent=myAgent.exists()&&Boolean.TRUE.equals(myAgent.getBoolean("active"));
                if(!activeAgency)agencyUid=activeAgent?myAgent.getString("agencyUid"):"";
                render();
            }).addOnFailureListener(e->{activeAgent=false;render();});
        }).addOnFailureListener(e->{activeAgency=false;activeAgent=false;render();});
    }
    private void render(){
        content.removeAllViews();
        TextView back=text("‹   KING Plus • Agency & Agents",22);
        back.setOnClickListener(v->finish());content.addView(back);
        title("🏢 Agency → Agent → Recharge");
        roles=text((admin?"👑 Super Admin   ":"")+(activeAgency?"🏢 Agency Owner   ":"")+
             (activeAgent?"💼 Agent":"")+"\nUID: "+uid,14);content.addView(roles);
        info=text("No payments / Diamonds are credited by this portal.",13);info.setTextColor(0xffffdc8b);
        content.addView(info);
        line("A recharge order means Pending Review. Only the verified payment backend may update the customer's true Wallet and VIP.");
        button("↻ Refresh live roles",this::refresh);
        if(admin)showAdmin();
        if(activeAgency)showAgency();
        if(activeAgent||activeAgency)showRecharge();
        if(!admin&&!activeAgency&&!activeAgent)
           line("Agency access is invitation-only. Give your signed-in KING UID to the Super Admin. Ordinary users cannot make themselves Agents.");
        button("💎 View verified Diamond Wallet",()->startActivity(new Intent(this,KingWallet976Activity.class)));
    }
    private void showAdmin(){
        title("👑 Super Admin • Assign Agency");
        final EditText id=field("KING User UID to approve as Agency");
        final EditText name=field("Agency display name");
        button("✓ Approve / Reactivate Agency",()->{
            String target=safeId(id.getText().toString());
            String label=name.getText().toString().trim();
            if(target==null||label.isEmpty()||label.length()>80){notice("Enter correct UID and Agency name");return;}
            saving(true);
            DocumentReference ref=db.collection("king_agencies").document(target);
            ref.get(Source.SERVER).addOnSuccessListener(old->{
                Map<String,Object> v=new HashMap<>();
                v.put("uid",target);v.put("name",label);v.put("active",true);
                v.put("approvedByUid",uid);v.put("updatedAt",FieldValue.serverTimestamp());
                if(!old.exists())v.put("createdAt",FieldValue.serverTimestamp());
                ref.set(v,com.google.firebase.firestore.SetOptions.merge()).addOnSuccessListener(ok->{
                    saving(false);notice("Agency approved • "+target);refresh();
                }).addOnFailureListener(e->{saving(false);notice("Agency approval rejected: "+e.getMessage());});
            }).addOnFailureListener(e->{saving(false);notice("Cannot verify agency: "+e.getMessage());});
        });
        button("⛔ Suspend Agency UID",()->{
            String target=safeId(id.getText().toString());if(target==null){notice("Enter Agency UID");return;}
            new AlertDialog.Builder(this).setTitle("Suspend Agency?")
               .setMessage("This will stop the Agency and all its Agents from creating new Recharge Requests.")
               .setPositiveButton("Suspend",(d,w)->{
                  saving(true);
                  db.collection("king_agencies").document(target).update(
                    "active",false,"updatedAt",FieldValue.serverTimestamp())
                    .addOnSuccessListener(v->{saving(false);notice("Agency suspended");refresh();})
                    .addOnFailureListener(e->{saving(false);notice("Suspend failed: "+e.getMessage());});
               }).setNegativeButton("Cancel",null).show();
        });
        button("🧾 Review all Recharge Requests",()->listOrders(null,true));
    }
    private void showAgency(){
        title("🏢 Agency • Assign Agents");
        EditText target=field("New Agent's KING User UID");
        button("✓ Authorize Agent",()->setAgent(target.getText().toString(),true));
        button("⛔ Suspend Agent",()->setAgent(target.getText().toString(),false));
        button("👥 List my Agents",this::listAgents);
        button("🧾 My Agency Recharge Requests",()->listOrders("agencyUid",false));
    }
    private void setAgent(String raw,boolean allow){
        String target=safeId(raw);if(target==null){notice("Enter valid Agent KING UID");return;}
        if(target.equals(uid)){notice("Agency owner already has recharge-request access");return;}
        DocumentReference ref=db.collection("king_agents").document(target);
        saving(true);
        ref.get(Source.SERVER).addOnSuccessListener(old->{
            if(old.exists()&&!uid.equals(old.getString("agencyUid"))){
                saving(false);notice("Agent belongs to a different agency. Cannot take over.");return;
            }
            if(!old.exists()&&!allow){saving(false);notice("Agent not assigned");return;}
            Map<String,Object> v=new HashMap<>();v.put("uid",target);v.put("agencyUid",uid);
            v.put("active",allow);v.put("updatedAt",FieldValue.serverTimestamp());
            if(!old.exists())v.put("createdAt",FieldValue.serverTimestamp());
            ref.set(v,com.google.firebase.firestore.SetOptions.merge()).addOnSuccessListener(x->{
                saving(false);notice(allow?"Agent authorized":"Agent suspended");refresh();
            }).addOnFailureListener(e->{saving(false);notice("Agent update rejected: "+e.getMessage());});
        }).addOnFailureListener(e->{saving(false);notice("Cannot read Agent: "+e.getMessage());});
    }
    private void listAgents(){
        db.collection("king_agents").whereEqualTo("agencyUid",uid).limit(40)
        .get(Source.SERVER).addOnSuccessListener(q->{
            StringBuilder names=new StringBuilder();
            for(DocumentSnapshot d:q.getDocuments())
                names.append(Boolean.TRUE.equals(d.getBoolean("active"))?"✓ ":"× ")
                .append(d.getId()).append("\n");
            new AlertDialog.Builder(this).setTitle("My Agents").setMessage(
                names.length()==0?"No Agents have been authorized.":names.toString())
                .setPositiveButton("Close",null).show();
        }).addOnFailureListener(e->notice("Agent list unavailable: "+e.getMessage()));
    }
    private void showRecharge(){
        title("💎 Agent Recharge Request");
        line("Choose recipient UID carefully. Submitting creates an auditable PENDING order, NOT actual Diamond delivery.");
        EditText recipient=field("Customer's KING Plus UID");
        Spinner packs=new Spinner(this);
        String[] choices={"100 Diamonds","600 Diamonds","1,300 Diamonds"};
        packs.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,choices));
        content.addView(packs);
        button("Send verified-account Recharge Request",()->{
            String target=safeId(recipient.getText().toString());
            if(target==null){notice("Enter valid customer KING UID");return;}
            int[] amounts={100,600,1300};int amount=amounts[packs.getSelectedItemPosition()];
            String parent=activeAgency?uid:agencyUid;
            if(!KingAgencyPolicy9982.validUid(parent)){notice("Agency is unavailable");return;}
            new AlertDialog.Builder(this).setTitle("Submit Recharge Request?")
              .setMessage("Customer: "+target+"\nPack: "+amount+" Diamonds\n\nSTATUS: Pending review. No payment or Wallet credit occurs now.")
              .setPositiveButton("Submit",(d,w)->submit(target,parent,amount))
              .setNegativeButton("Cancel",null).show();
        });
        button("🧾 My Agent Orders",()->listOrders("agentUid",false));
    }
    private void submit(String target,String parent,int amount){
        if(!KingAgencyPolicy9982.validPack(amount)||busy)return;
        saving(true);
        DocumentReference order=db.collection("king_agent_recharge_requests").document();
        Map<String,Object> doc=new HashMap<>();
        doc.put("agentUid",uid);doc.put("agencyUid",parent);
        doc.put("targetUid",target);doc.put("diamonds",amount);
        doc.put("status","pending");doc.put("createdAt",FieldValue.serverTimestamp());
        order.set(doc).addOnSuccessListener(v->{
            saving(false);
            new AlertDialog.Builder(this).setTitle("Request Created")
               .setMessage("Order: "+order.getId()+"\nStatus: PENDING\nNo Diamonds credited. Admin/payment verification required.")
               .setPositiveButton("OK",null).show();
        }).addOnFailureListener(e->{saving(false);notice("Request denied: "+e.getMessage());});
    }
    private void listOrders(String scope,boolean all){
        Query query=db.collection("king_agent_recharge_requests");
        if(!all)query=query.whereEqualTo(scope,"agencyUid".equals(scope)?uid:uid);
        query=query.limit(40);
        query.get(Source.SERVER).addOnSuccessListener(snapshot->{
            StringBuilder report=new StringBuilder();
            for(DocumentSnapshot d:snapshot.getDocuments()){
                if(report.length()>3200)break;
                report.append(d.getString("status")).append(" • ")
                    .append(d.getLong("diamonds")).append(" 💎\n")
                    .append(d.getString("targetUid")).append("\nOrder: ")
                    .append(d.getId()).append("\n\n");
            }
            new AlertDialog.Builder(this).setTitle("Recharge Requests (NO CASH CREDIT)")
                .setMessage(report.length()==0?"No requests yet.":report.toString())
                .setPositiveButton("Close",null).show();
        }).addOnFailureListener(e->notice("Order review unavailable: "+e.getMessage()));
    }
}
