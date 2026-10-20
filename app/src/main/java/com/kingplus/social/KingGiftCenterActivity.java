package com.kingplus.social;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import java.util.*;

/** Shared paid catalog, server wallet and live sent/received history. */
public class KingGiftCenterActivity extends Activity {
    private FirebaseFirestore db;
    private String uid="",targetUid="",targetName="Member";
    private LinearLayout body,history;
    private TextView balance;
    private ListenerRegistration wallet,sent,received;
    private final Map<String,DocumentSnapshot> rows=new HashMap<>();
    private final DocumentSnapshot[] cursor=new DocumentSnapshot[2];
    private final boolean[] more={false,false},loading={false,false};
    private final String[] errors={null,null};
    private int generation;
    private boolean sending;
    private FirebaseAuth.AuthStateListener accountListener10065;
    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}
    private TextView text(String value){TextView t=new TextView(this);t.setText(value);t.setTextSize(15);t.setTextColor(Color.WHITE);t.setPadding(dp(12),dp(14),dp(12),dp(14));return t;}
    @Override public void onCreate(Bundle state){
        super.onCreate(state);db=FirebaseFirestore.getInstance();
        String target=getIntent().getStringExtra("targetUid"),name=getIntent().getStringExtra("targetName");
        if(target!=null)targetUid=target;if(name!=null&&!name.trim().isEmpty())targetName=name;
        LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(0xff171420);
        LinearLayout header=(LinearLayout)getLayoutInflater().inflate(R.layout.king_gift_header,root,false);
        TextView back=header.findViewById(R.id.king_gift_back);back.setText("‹");back.setTextSize(32);back.setTextColor(Color.WHITE);back.setOnClickListener(v->finish());
        TextView title=header.findViewById(R.id.king_gift_heading);title.setText("Gifts & History");title.setTextSize(19);title.setTextColor(Color.WHITE);
        balance=header.findViewById(R.id.king_gift_balance);balance.setTextColor(0xffffd768);root.addView(header);
        ScrollView scroll=new ScrollView(this);body=new LinearLayout(this);body.setOrientation(1);body.setPadding(dp(12),0,dp(12),dp(32));scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        body.addView(text(targetUid.isEmpty()?"Your sent and received gifts":"Send to "+targetName));
        body.addView(text("Diamond gifts spend your verified balance. Received gift credit is a recognition score. VIP comes from verified recharge."));
        if(!targetUid.isEmpty())for(int i=0;i<KingGiftCatalog10064.NAMES.length;i++){
            final int k=i;TextView item=text(KingGiftCatalog10064.ICONS[i]+"  "+KingGiftCatalog10064.NAMES[i]+"  • 💎 "+KingGiftCatalog10064.COSTS[i]+"  ›");
            item.setMinHeight(dp(56));item.setOnClickListener(v->quantity(k));body.addView(item);
        }
        TextView retryPending=text("↻ Check pending gift");
        retryPending.setOnClickListener(v->KingGiftPurchase10064.retryPending(this,(ok,message)->{
            sending=false;Toast.makeText(this,message,Toast.LENGTH_LONG).show();
        }));body.addView(retryPending);
        body.addView(text("GIFT HISTORY"));history=new LinearLayout(this);history.setOrientation(1);body.addView(history);setContentView(root);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(root,(v,insets)->{
            androidx.core.graphics.Insets safe=insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(safe.left,safe.top,safe.right,safe.bottom);return insets;
        });
    }
    @Override protected void onStart(){
        super.onStart();accountListener10065=auth->start();FirebaseAuth.getInstance().addAuthStateListener(accountListener10065);
    }
    @Override protected void onStop(){
        if(accountListener10065!=null){FirebaseAuth.getInstance().removeAuthStateListener(accountListener10065);accountListener10065=null;}
        stop();super.onStop();
    }
    private void stop(){++generation;if(wallet!=null)wallet.remove();if(sent!=null)sent.remove();if(received!=null)received.remove();wallet=sent=received=null;}
    private boolean active(int ticket){FirebaseUser me=FirebaseAuth.getInstance().getCurrentUser();return ticket==generation&&!isFinishing()&&!isDestroyed()&&me!=null&&uid.equals(me.getUid());}
    private void start(){
        stop();sending=false;rows.clear();Arrays.fill(cursor,null);Arrays.fill(more,false);Arrays.fill(loading,false);Arrays.fill(errors,null);
        FirebaseUser me=FirebaseAuth.getInstance().getCurrentUser();
        if(me==null){uid="";balance.setText("Sign in");history.removeAllViews();history.addView(text("Sign in to see gifts."));return;}
        uid=me.getUid();final int ticket=generation;balance.setText("💎 checking…");
        wallet=db.collection("wallets").document(uid).addSnapshotListener(MetadataChanges.INCLUDE,(d,e)->{
            if(!active(ticket))return;
            Object coins=d==null?null:d.get("coins");Object score=d==null?null:d.get("giftScore");
            boolean verified=e==null&&d!=null&&!d.getMetadata().isFromCache()&&!d.getMetadata().hasPendingWrites();
            balance.setText("💎 "+(verified&&coins instanceof Long?coins:"—")+"\nGifts "+(verified?(score instanceof Long?score:0):"—"));
        });
        sent=watch(0,ticket);received=watch(1,ticket);
    }
    private Query query(int side){return db.collection("wallet_ledger").whereEqualTo(side==0?"senderUid":"targetUid",uid).orderBy("createdAt",Query.Direction.DESCENDING).limit(50);}
    private ListenerRegistration watch(int side,int ticket){return query(side).addSnapshotListener(MetadataChanges.INCLUDE,(snap,error)->{
        if(!active(ticket))return;
        if(error!=null){errors[side]="Gift history unavailable • tap to retry";renderHistory();return;}
        if(snap==null||snap.getMetadata().isFromCache()||snap.getMetadata().hasPendingWrites())return;
        errors[side]=null;for(DocumentSnapshot row:snap.getDocuments())rows.put(row.getId(),row);
        if(cursor[side]==null&&!snap.isEmpty())cursor[side]=snap.getDocuments().get(snap.size()-1);
        more[side]=snap.size()==50||cursor[side]!=null&&more[side];renderHistory();
    });}
    private void older(int side){
        if(loading[side]||cursor[side]==null)return;loading[side]=true;int ticket=generation;renderHistory();
        query(side).startAfter(cursor[side]).get(Source.SERVER).addOnSuccessListener(snap->{
            if(!active(ticket))return;loading[side]=false;
            for(DocumentSnapshot row:snap.getDocuments())rows.put(row.getId(),row);
            if(!snap.isEmpty())cursor[side]=snap.getDocuments().get(snap.size()-1);
            more[side]=snap.size()==50;errors[side]=null;renderHistory();
        }).addOnFailureListener(e->{if(active(ticket)){loading[side]=false;errors[side]="Older gifts unavailable • retry";renderHistory();}});
    }
    private void renderHistory(){
        history.removeAllViews();List<DocumentSnapshot> list=new ArrayList<>(rows.values());
        list.sort((a,b)->{com.google.firebase.Timestamp x=a.getTimestamp("createdAt"),y=b.getTimestamp("createdAt");return x==null?(y==null?0:1):y==null?-1:y.compareTo(x);});
        int count=0;
        for(DocumentSnapshot d:list){
            if(!"gift".equals(d.getString("type")))continue;
            String sender=d.getString("senderUid"),target=d.getString("targetUid");
            if(!uid.equals(sender)&&!uid.equals(target))continue;
            if(!targetUid.isEmpty()&&!targetUid.equals(sender)&&!targetUid.equals(target))continue;
            TextView line=(TextView)getLayoutInflater().inflate(R.layout.king_gift_history,history,false);
            Object quantity=d.get("quantity"),cost=d.get("cost");
            line.setText((uid.equals(sender)?"↗ Sent ":"↙ Received ")+d.getString("giftName")+
                (quantity instanceof Long?" x"+quantity:"")+" • 💎 "+(cost instanceof Long?cost:"—"));
            line.setTextSize(14);line.setTextColor(Color.WHITE);line.setMinHeight(dp(52));history.addView(line);count++;
        }
        if(count==0)history.addView(text("No confirmed gifts loaded."));
        for(int side=0;side<2;side++){
            final int direction=side;
            if(errors[side]!=null){TextView retry=text(errors[side]);retry.setOnClickListener(v->{if(cursor[direction]==null)start();else older(direction);});history.addView(retry);}
            else if(more[side]){TextView next=text(loading[side]?"Loading…":"Load older "+(side==0?"sent":"received")+" gifts ↓");next.setOnClickListener(v->older(direction));history.addView(next);}
        }
    }
    private void quantity(int index){
        new android.app.AlertDialog.Builder(this).setTitle(KingGiftCatalog10064.NAMES[index]).setItems(new String[]{"1","3","9"},(d,w)->{
            int qty=new int[]{1,3,9}[w];long total=(long)KingGiftCatalog10064.COSTS[index]*qty;
            if(total>1000000){Toast.makeText(this,"Choose a smaller quantity",Toast.LENGTH_SHORT).show();return;}
            new android.app.AlertDialog.Builder(this).setMessage("Send "+qty+" × "+KingGiftCatalog10064.NAMES[index]+" for "+total+" Diamonds?")
                .setNegativeButton("Cancel",null).setPositiveButton("Send",(dialog,which)->send(index,qty,(int)total)).show();
        }).setNegativeButton("Cancel",null).show();
    }
    private void send(int index,int qty,int total){
        if(sending)return;sending=true;
        KingGiftPurchase10064.send(this,null,targetUid,KingGiftCatalog10064.NAMES[index],qty,total,(ok,message)->{
            sending=false;Toast.makeText(this,message,Toast.LENGTH_LONG).show();
        });
    }
}
