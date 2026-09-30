package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EconomyActivity extends Activity {
    private static final int BG = 0xfff7f7fb;
    private static final int DARK = 0xff181528;
    private static final int PURPLE = 0xff7c4dff;
    private static final int MUTED = 0xff777186;

    private FirebaseUser me;
    private FirebaseFirestore db;
    private LinearLayout body;
    private LinearLayout cloudBox;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        try { me = FirebaseAuth.getInstance().getCurrentUser(); } catch (Exception ignored) { me = null; }
        try { db = FirebaseFirestore.getInstance(); } catch (Exception ignored) { db = null; }
        render();
    }

    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
    private GradientDrawable bg(int color, int radius) { GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d; }
    private TextView label(String text,int size,int color,boolean bold) { TextView t=new TextView(this);t.setText(text);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,Typeface.BOLD);return t; }

    private void render() {
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(12),dp(12),dp(12),dp(8));
        TextView back=label("‹",38,DARK,false);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->finish());head.addView(back,new LinearLayout.LayoutParams(dp(48),dp(56)));
        LinearLayout titles=new LinearLayout(this);titles.setOrientation(LinearLayout.VERTICAL);titles.addView(label("Economy Center",27,DARK,true));titles.addView(label("Gifts • Wallet • Levels • Frames",12,MUTED,false));head.addView(titles,new LinearLayout.LayoutParams(0,dp(60),1));
        TextView refresh=label("↻",27,PURPLE,true);refresh.setGravity(Gravity.CENTER);refresh.setOnClickListener(v->{renderLocal();loadCloud();});head.addView(refresh,new LinearLayout.LayoutParams(dp(52),dp(52)));root.addView(head);

        ScrollView scroll=new ScrollView(this);body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(14),dp(4),dp(14),dp(24));scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
        renderLocal(); loadCloud();
    }

    private void renderLocal() {
        if(body==null)return; body.removeAllViews();
        int coins=EconomyManager.getTestCoins(this,getSharedPreferences("king_party",MODE_PRIVATE).getInt("coins",2500));
        long sent=EconomyManager.sentCoins(this), received=EconomyManager.receivedCoins(this);
        int userLevel=EconomyManager.userLevel(this), wealth=EconomyManager.wealthLevel(this), charm=EconomyManager.charmLevel(this);
        int vip=vipTier(wealth,charm);
        addHero("💎 "+coins+" TEST Coins","No-billing test wallet • real-money recharge stays disabled");
        addCard("🏆 User Level  "+userLevel,"Activity XP: "+EconomyManager.userXp(this));
        addCard("💰 Wealth Level  "+wealth,"Gift value sent: "+sent+" coins • "+EconomyManager.giftsSent(this)+" gifts");
        addCard("💜 Charm Level  "+charm,"Gift value received: "+received+" coins • "+EconomyManager.giftsReceived(this)+" gifts");
        addCard("👑 VIP "+vip,"VIP tier is progression-based in this no-billing build");
        action("🎁 Gift Catalog",this::giftCatalog);
        action("📜 TEST Gift History",this::localHistory);
        action("🖼 Profile Frame • "+EconomyManager.equippedProfileFrame(this),this::profileFrames);
        action("🎙 Room Frame • "+EconomyManager.equippedRoomFrame(this),this::roomFrames);
        action("🧪 Simulate received TEST gift",()->{
            EconomyManager.recordTestGiftReceived(this,100,"Diamond","Test User");
            Toast.makeText(this,"TEST gift received • Charm progress updated",Toast.LENGTH_SHORT).show();renderLocal();loadCloud();
        });
        TextView serverTitle=label("Server wallet / progression",19,DARK,true);serverTitle.setPadding(dp(4),dp(18),0,dp(8));body.addView(serverTitle);
        cloudBox=new LinearLayout(this);cloudBox.setOrientation(LinearLayout.VERTICAL);body.addView(cloudBox);
    }

    private void loadCloud() {
        if(cloudBox==null)return; cloudBox.removeAllViews();
        if(me==null||db==null){addCloud("Sign in with Firebase to view secure server wallet and gift progression.");return;}
        addCloud("Loading secure wallet…");
        db.collection("wallets").document(me.getUid()).get().addOnSuccessListener(wallet->{
            cloudBox.removeAllViews();
            long coins=wallet.exists()&&wallet.getLong("coins")!=null?wallet.getLong("coins"):0L;
            addCloud("💎 Server balance: "+coins+" coins");
            db.collection("economy_profiles").document(me.getUid()).get().addOnSuccessListener(profile->renderCloudProgress(profile)).addOnFailureListener(e->addCloud("Progression not available yet: "+safe(e.getLocalizedMessage())));
            loadServerHistory();
        }).addOnFailureListener(e->{cloudBox.removeAllViews();addCloud("Server wallet unavailable: "+safe(e.getLocalizedMessage()));});
    }

    private void renderCloudProgress(DocumentSnapshot p) {
        long sent=num(p,"sentCoins"),received=num(p,"receivedCoins"),giftsSent=num(p,"giftsSent"),giftsReceived=num(p,"giftsReceived"),xp=num(p,"userXp");
        addCloud("🏆 Server User Level "+EconomyManager.level(xp,500L)+" • XP "+xp);
        addCloud("💰 Server Wealth Level "+EconomyManager.level(sent,1000L)+" • sent "+sent+" coins / "+giftsSent+" gifts");
        addCloud("💜 Server Charm Level "+EconomyManager.level(received,1000L)+" • received "+received+" coins / "+giftsReceived+" gifts");
    }

    private void loadServerHistory() {
        addCloud("Recent server gift activity:");
        db.collection("wallet_ledger").whereEqualTo("senderUid",me.getUid()).limit(10).get().addOnSuccessListener(s->{
            for(DocumentSnapshot d:s.getDocuments()) addCloud("↗ "+giftLine(d,"Sent"));
        }).addOnFailureListener(e->addCloud("Sent history unavailable"));
        db.collection("wallet_ledger").whereEqualTo("targetUid",me.getUid()).limit(10).get().addOnSuccessListener(s->{
            for(DocumentSnapshot d:s.getDocuments()) addCloud("↙ "+giftLine(d,"Received"));
        }).addOnFailureListener(e->addCloud("Received history unavailable"));
    }

    private String giftLine(DocumentSnapshot d,String prefix) {
        String gift=d.getString("giftName");if(gift==null||gift.trim().isEmpty())gift="Gift";
        Long cost=d.getLong("cost");return prefix+" "+gift+" • "+(cost==null?0:cost)+" coins";
    }

    private void giftCatalog() {
        String[] gifts={"🌹 Rose • 10","❤️ Heart • 50","🍫 Chocolate • 100","🚗 Car • 500","👑 Crown • 1000","🏰 Castle • 5000","🎆 Firework • 10000"};
        new AlertDialog.Builder(this).setTitle("🎁 KING Gift Catalog").setItems(gifts,null).setMessage("Send gifts from a Party room after selecting a recipient.").setPositiveButton("Close",null).show();
    }

    private void localHistory() {
        List<String> raw=EconomyManager.localHistory(this);List<String> rows=new ArrayList<>();
        for(String x:raw){String[] p=x.split("\\|",5);if(p.length<5)continue;long ts;try{ts=Long.parseLong(p[4]);}catch(Exception e){ts=System.currentTimeMillis();}rows.add(("SENT".equals(p[0])?"↗ ":"↙ ")+p[1]+" • "+p[2]+" coins • "+p[3]+"\n"+DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(new Date(ts)));}
        if(rows.isEmpty())rows.add("No TEST gift history yet");
        new AlertDialog.Builder(this).setTitle("TEST Gift History").setItems(rows.toArray(new String[0]),null).setPositiveButton("Close",null).show();
    }

    private void profileFrames() {
        int wealth=EconomyManager.wealthLevel(this),charm=EconomyManager.charmLevel(this);
        String[] names={"Classic","Silver","Royal","Charm Halo"};int[] needWealth={1,2,5,1};int[] needCharm={1,1,1,5};
        new AlertDialog.Builder(this).setTitle("Profile Frame").setItems(names,(d,w)->{if(wealth<needWealth[w]||charm<needCharm[w]){toast("Unlock requires Wealth "+needWealth[w]+" / Charm "+needCharm[w]);return;}EconomyManager.equipProfileFrame(this,names[w]);toast(names[w]+" equipped");renderLocal();loadCloud();}).show();
    }

    private void roomFrames() {
        int wealth=EconomyManager.wealthLevel(this);
        String[] names={"Classic","Neon Party","Royal Stage","Diamond Room"};int[] need={1,3,6,10};
        new AlertDialog.Builder(this).setTitle("Room Frame").setItems(names,(d,w)->{if(wealth<need[w]){toast("Unlock requires Wealth Level "+need[w]);return;}EconomyManager.equipRoomFrame(this,names[w]);toast(names[w]+" equipped");renderLocal();loadCloud();}).show();
    }

    private int vipTier(int wealth,int charm) { int score=Math.max(wealth,charm);return score>=20?4:score>=10?3:score>=5?2:score>=3?1:0; }
    private long num(DocumentSnapshot d,String key){Long v=d==null?null:d.getLong(key);return v==null?0L:v;}
    private void addHero(String title,String sub){LinearLayout c=card();TextView a=label(title,28,Color.WHITE,true);c.setBackground(bg(PURPLE,20));c.addView(a);TextView b=label(sub,13,0xffeee6ff,false);b.setPadding(0,dp(6),0,0);c.addView(b);body.addView(c,params(105));}
    private void addCard(String title,String sub){LinearLayout c=card();c.addView(label(title,17,DARK,true));TextView b=label(sub,13,MUTED,false);b.setPadding(0,dp(5),0,0);c.addView(b);body.addView(c,params(74));}
    private LinearLayout card(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(15),dp(12),dp(15),dp(12));c.setBackground(bg(Color.WHITE,18));return c;}
    private LinearLayout.LayoutParams params(int h){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(h));p.setMargins(0,dp(5),0,dp(5));return p;}
    private void action(String text,Runnable r){TextView x=label(text,15,DARK,true);x.setGravity(Gravity.CENTER_VERTICAL);x.setPadding(dp(16),0,dp(16),0);x.setBackground(bg(Color.WHITE,16));x.setOnClickListener(v->r.run());body.addView(x,params(58));}
    private void addCloud(String text){TextView x=label(text,13,DARK,false);x.setPadding(dp(12),dp(7),dp(12),dp(7));x.setBackground(bg(Color.WHITE,12));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(2),0,dp(2));cloudBox.addView(x,p);}
    private void toast(String text){Toast.makeText(this,text,Toast.LENGTH_SHORT).show();}
    private String safe(String s){return s==null||s.trim().isEmpty()?"unknown error":s;}
}
