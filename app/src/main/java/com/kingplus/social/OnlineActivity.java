package com.kingplus.social;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.*;

/** Authenticated cloud screens. Local demo balances never enter cloud requests. */
public class OnlineActivity extends Activity {
    private LinearLayout page;
    private String uid;
    private boolean admin, pushEnabled, busy;
    private int generation;
    private interface Done { void run(Object result); }
    private Map<String,Object> args(Object... pairs) {
        Map<String,Object> m=new HashMap<>();
        for(int i=0;i<pairs.length;i+=2)m.put((String)pairs[i],pairs[i+1]);
        return m;
    }
    @SuppressWarnings("unchecked") private Map<String,Object> map(Object value){return (Map<String,Object>)value;}
    @SuppressWarnings("unchecked") private List<Map<String,Object>> list(Object value){return (List<Map<String,Object>>)value;}
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if(user==null){Toast.makeText(this,"Online features need Google or verified phone sign-in. Test OTP is local only.",Toast.LENGTH_LONG).show();finish();return;}
        uid=user.getUid(); show("Connecting…");
        call("kpBootstrap",args(),value->{Map<String,Object> r=map(value);admin=Boolean.TRUE.equals(r.get("admin"));pushEnabled=Boolean.TRUE.equals(r.get("notificationsEnabled"));
            KingMessagingService.register(this);String section=getIntent().getStringExtra("section");
            if("rankings".equals(section))rankings();else if("notifications".equals(section))inbox();else if("safety".equals(section))safety();else wallet();});
    }
    private void show(String title){
        generation++;ScrollView scroll=new ScrollView(this);page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);
        int pad=(int)(20*getResources().getDisplayMetrics().density);page.setPadding(pad,pad,pad,pad);page.setBackgroundColor(0xff0c1026);
        scroll.addView(page);setContentView(scroll);label(title);button("Back to KING Plus",this::finish);
    }
    private TextView label(String s){TextView t=new TextView(this);t.setText(s);t.setTextColor(Color.WHITE);t.setTextSize(18);t.setPadding(0,16,0,16);page.addView(t);return t;}
    private void button(String s,Runnable run){Button b=new Button(this);b.setText(s);b.setAllCaps(false);page.addView(b);b.setOnClickListener(v->{if(!busy)run.run();});}
    private EditText input(String hint){EditText e=new EditText(this);e.setHint(hint);e.setSingleLine(true);e.setTextColor(Color.WHITE);e.setHintTextColor(Color.LTGRAY);page.addView(e);return e;}
    private void call(String name,Map<String,Object> data,Done done){
        FirebaseUser current=FirebaseAuth.getInstance().getCurrentUser();
        if(current==null||!uid.equals(current.getUid())){finish();return;}
        if(busy)return;busy=true;final int view=generation;
        FirebaseFunctions.getInstance("us-central1").getHttpsCallable(name).call(data).addOnCompleteListener(this,task->{
            busy=false;if(isFinishing()||view!=generation)return;
            FirebaseUser active=FirebaseAuth.getInstance().getCurrentUser();if(active==null||!uid.equals(active.getUid())){finish();return;}
            if(task.isSuccessful()){done.run(task.getResult().getData());return;}
            String message="Online service unavailable. Check connection or server setup, then retry.";
            Exception e=task.getException();if(e instanceof FirebaseFunctionsException){
                switch(((FirebaseFunctionsException)e).getCode()){
                    case PERMISSION_DENIED:message="This action is not allowed. The account may be blocked or suspended.";break;
                    case FAILED_PRECONDITION:message="Not enough coins or account setup is incomplete.";break;
                    case INVALID_ARGUMENT:message="Check the account ID and entered details.";break;
                    case NOT_FOUND:message="Account or online service not found.";break;
                    case RESOURCE_EXHAUSTED:message="Daily report limit reached. Try again tomorrow.";break;
                    default:break;
                }
            }
            if("kpSendGift".equals(name) && e instanceof FirebaseFunctionsException){
                FirebaseFunctionsException.Code code=((FirebaseFunctionsException)e).getCode();
                if(code==FirebaseFunctionsException.Code.INVALID_ARGUMENT || code==FirebaseFunctionsException.Code.FAILED_PRECONDITION || code==FirebaseFunctionsException.Code.NOT_FOUND || code==FirebaseFunctionsException.Code.PERMISSION_DENIED){
                    pending().edit().clear().apply();
                    new AlertDialog.Builder(this).setTitle("Gift not sent").setMessage(message).setPositiveButton("Back to wallet",(d,w)->wallet()).show();return;
                }
            }
            new AlertDialog.Builder(this).setTitle("Not completed").setMessage(message).setPositiveButton("Retry",(d,w)->call(name,data,done)).setNegativeButton("Close",null).show();
        });
    }
    private android.content.SharedPreferences pending(){return getSharedPreferences("online_pending_"+uid,0);}
    private void wallet(){
        show("Online wallet");TextView balance=label("Loading balance…");
        TextView id=label("Your account ID: "+uid);id.setTextIsSelectable(true);
        label("Online coins and charm are virtual. Local demo rewards are separate.");
        button("Claim 100 daily coins",()->call("kpDailyReward",args(),v->{Toast.makeText(this,Boolean.TRUE.equals(map(v).get("alreadyClaimed"))?"Already claimed today":"100 coins added",Toast.LENGTH_SHORT).show();wallet();}));
        button("Send a gift",this::gift);
        button("Transaction history",this::ledger);
        button("Online rankings",this::rankings);
        button("Notifications",this::inbox);
        button("Block / report",this::safety);
        if(admin)button("Admin report review",this::adminReports);
        call("kpWallet",args(),v->{Map<String,Object> r=map(v);balance.setText(r.get("balance")+" coins\nSent: "+r.get("sentGifts")+" • Received: "+r.get("receivedGifts"));});
    }
    private void gift(){
        show("Send online gift");
        if(pending().contains("requestId")){
            label("A gift request is awaiting confirmation. Retry checks the same request without charging twice.");
            label("To: "+pending().getString("to","")+" • "+pending().getString("gift",""));
            button("Check / retry pending gift",this::sendPending);button("Back to wallet",this::wallet);return;
        }
        EditText target=input("Recipient account ID");
        String[] keys={"rose","heart","candy","cake","diamond","car","crown","fireworks"};
        int[] prices={1,5,10,50,100,500,999,1999};
        for(int i=0;i<keys.length;i++){final String key=keys[i];final int price=prices[i];button(key+" • "+price+" coins",()->{
            String to=target.getText().toString().trim();if(to.isEmpty()||to.equals(uid)){target.setError("Enter another user's account ID");return;}
            new AlertDialog.Builder(this).setTitle("Confirm gift").setMessage("Send "+key+" to "+to+" for "+price+" coins?").setNegativeButton("Cancel",null).setPositiveButton("Send",(d,w)->{
                if(busy || pending().contains("requestId"))return;
                if(!pending().edit().putString("to",to).putString("gift",key).putString("requestId",UUID.randomUUID().toString()).commit()){Toast.makeText(this,"Could not save request. Gift was not sent.",Toast.LENGTH_LONG).show();return;}sendPending();}).show();
        });}
    }
    private void sendPending(){
        call("kpSendGift",args("to",pending().getString("to",""),"gift",pending().getString("gift",""),"requestId",pending().getString("requestId","")),v->{pending().edit().clear().apply();Toast.makeText(this,"Gift sent",Toast.LENGTH_SHORT).show();wallet();});
    }
    private void ledger(){show("Latest 50 transactions");call("kpLedger",args(),v->{if(list(v).isEmpty())label("No transactions yet.");for(Map<String,Object> row:list(v))label(row.get("kind")+" • "+row.get("amount")+" coins"+(row.containsKey("gift")?" • "+row.get("gift"):""));});}
    private void rankings(){show("Online rankings • all time");button("Top senders",()->ranking("sentCoins"));button("Top receivers",()->ranking("receivedCoins"));ranking("sentCoins");}
    private void ranking(String metric){show(metric.equals("sentCoins")?"Top senders • all time":"Top receivers • charm");button("Switch ranking",()->ranking(metric.equals("sentCoins")?"receivedCoins":"sentCoins"));call("kpRankings",args("metric",metric),v->{int n=0;for(Map<String,Object> row:list(v)){label((++n)+". "+row.get("name")+" • "+row.get("score"));TextView id=label("ID: "+row.get("uid"));id.setTextIsSelectable(true);}if(n==0)label("No online gifts yet.");});}
    private void inbox(){
        show("Notifications • latest 50");
        button(pushEnabled?"Turn push notifications off":"Turn push notifications on",()->{
            if(!pushEnabled&&Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},71);return;}
            updatePush(!pushEnabled);
        });
        call("kpInbox",args(),v->{if(list(v).isEmpty())label("No notifications yet.");for(Map<String,Object> row:list(v))button((Boolean.TRUE.equals(row.get("read"))?"✓ ":"● ")+row.get("body"),()->call("kpReadNotice",args("id",row.get("id")),r->inbox()));});
    }
    private void updatePush(boolean enabled){call("kpNotificationSettings",args("enabled",enabled),v->{pushEnabled=enabled;if(enabled)KingMessagingService.register(this);inbox();});}
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results){super.onRequestPermissionsResult(request,permissions,results);if(request==71&&results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED)updatePush(true);}
    private void safety(){
        show("Online safety");EditText target=input("Account ID or room name");EditText reason=input("Report reason");
        button("Block account",()->block(target.getText().toString().trim(),true));
        button("Unblock account",()->block(target.getText().toString().trim(),false));
        button("Report account",()->report(target,reason,"user"));button("Report room",()->report(target,reason,"room"));
        button("My blocked accounts",()->{show("Blocked accounts • first 100");call("kpBlocks",args(),v->{if(list(v).isEmpty())label("No blocked accounts.");for(Map<String,Object> row:list(v))button("Unblock "+row.get("id"),()->block((String)row.get("id"),false));});});
        label("Blocking prevents online gifts in both directions. Online chat is not connected yet.");
    }
    private void block(String target,boolean value){if(target.isEmpty())return;call("kpSetBlock",args("target",target,"blocked",value),v->{Toast.makeText(this,value?"Account blocked":"Account unblocked",Toast.LENGTH_SHORT).show();safety();});}
    private void report(EditText target,EditText reason,String kind){
        if(target.getText().toString().trim().isEmpty()||reason.getText().toString().trim().isEmpty()){reason.setError("Enter target and reason");return;}
        call("kpReport",args("target",target.getText().toString().trim(),"reason",reason.getText().toString().trim(),"kind",kind,"requestId",UUID.randomUUID().toString()),v->{new AlertDialog.Builder(this).setTitle("Report submitted").setMessage("Reference: "+map(v).get("reportId")).setPositiveButton("OK",null).show();});
    }
    private void adminReports(){
        show("Admin • open reports (up to 50)");call("kpAdminReports",args(),v->{if(list(v).isEmpty())label("No open reports.");for(Map<String,Object> row:list(v))button(row.get("kind")+": "+row.get("target")+"\n"+row.get("reason"),()->review(row));});
    }
    private void review(Map<String,Object> row){
        show("Review report");label(row.get("target")+"\n"+row.get("reason"));EditText note=input("Review note");
        String[] actions="user".equals(row.get("kind"))?new String[]{"dismiss","warn","suspend"}:new String[]{"dismiss"};
        if("room".equals(row.get("kind")))label("Room enforcement needs the future online room service.");
        for(String action:actions)button(action,()->{if(note.getText().toString().trim().isEmpty()){note.setError("Note required");return;}
            new AlertDialog.Builder(this).setTitle("Confirm "+action).setMessage("Apply this review to "+row.get("target")+"?").setNegativeButton("Cancel",null).setPositiveButton("Confirm",(d,w)->call("kpResolveReport",args("id",row.get("id"),"note",note.getText().toString().trim(),"action",action),v->adminReports())).show();});
    }
}
