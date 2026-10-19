package com.kingplus.social;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.widget.EditText;
import android.text.InputType;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;
import android.app.AlertDialog;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.functions.FirebaseFunctions;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * Recharge UI prepared for official Google Play one-time consumable purchases.
 * LIVE_RECHARGE=false until Play Console products, merchant setup, and verified
 * Firebase backend are deployed. NEVER mint diamonds from a local purchase result.
 */
public final class KingRecharge974Activity extends Activity implements PurchasesUpdatedListener {
    public static final boolean LIVE_RECHARGE = false;
    // Phase 1 is UI/DEMO ONLY. Even an accidentally enabled remote merchant catalog
    // may not start a real-money checkout until a separate reviewed launch.
    public static final boolean LIVE_CASHFREE_RECHARGE = false;
    // Google Play SKUs remain unchanged and OFF. Additional packages are UPI-only.
    public static final String[] PRODUCT_IDS = {
        "king_coins_100","king_coins_600","king_coins_1300"
    };
    private static final String[] UPI_PRODUCT_IDS = {
        "king_coins_100","king_coins_600","king_coins_1300",
        "king_upi_8800","king_upi_17800","king_upi_45000",
        "king_upi_90000","king_upi_180000","king_upi_450000"
    };
    private static final int[] AMOUNTS = {100,600,1300};
    private final Map<String,ProductDetails> products = new HashMap<>();
    private final Map<String,TextView> productButtons = new HashMap<>();
    private FirebaseUser signedIn;
    private BillingClient billingClient;
    private ListenerRegistration walletRegistration;
    private TextView status;
    private TextView balance;
    private TextView vip;
    private boolean billReady;
    private boolean verifying;
    private String signedInUid;
    private boolean upiReady;
    private boolean cashfreeVerifying;
    private TextView upiStatus;
    private EditText upiPhone;
    private LinearLayout upiRows;
    private String cashfreePendingOrder;
    // The reference shop is a layout reference only; payment prices always come from SERVER.
    private LinearLayout rechargeBody;
    private TextView diamondTab, crystalTab, beansBalance;
    private CheckBox rechargeAgreement;
    private LinearLayout upiPreview;
    private long serverCoins,serverVipLevel,serverPoints;
    private Long serverBeans;
    private boolean serverWalletConfirmed;
    private boolean crystalSelected;
    private String selectedMethod="UPI";
    // Keep consent and entered phone while switching payment methods / tabs.
    private boolean agreementAccepted;
    private String enteredPhone="";
    private View diamondUnderline,crystalUnderline;
    private TextView demoButtonView,demoSelectionView;
    private long selectedDemoDiamonds=8800L,selectedDemoCrystals=590L;


    private int dp(int size){return (int)(size*getResources().getDisplayMetrics().density+0.5f);}
    private GradientDrawable goldenPanel(){
        GradientDrawable shape=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{0xffffe4aa,0xfffff7e2});
        shape.setCornerRadius(dp(18));
        return shape;
    }
    private GradientDrawable packBackground(){
        GradientDrawable shape=bg(0xfff7f8fc);
        shape.setStroke(dp(1),0xffe7e8ed);
        return shape;
    }
    private GradientDrawable bg(int color) {
        GradientDrawable shape=new GradientDrawable();
        shape.setColor(color);shape.setCornerRadius(dp(12));return shape;
    }
    private TextView item(String text,int size,int color,boolean bold){
        TextView v=new TextView(this);
        v.setText(text);v.setTextSize(size);v.setTextColor(color);
        if(bold)v.setTypeface(null,Typeface.BOLD);
        v.setGravity(Gravity.CENTER_VERTICAL);
        v.setPadding(dp(14),dp(12),dp(14),dp(12));
        return v;
    }

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        signedIn=FirebaseAuth.getInstance().getCurrentUser();
        signedInUid=signedIn==null?null:signedIn.getUid();
        crystalSelected=getIntent()!=null&&getIntent().getBooleanExtra("KING_RECHARGE_CRYSTAL",false);
        if(state!=null){
            crystalSelected=state.getBoolean("recharge_crystal",crystalSelected);
            agreementAccepted=state.getBoolean("recharge_agreement",false);
            selectedMethod=state.getString("recharge_method","UPI");
            enteredPhone=state.getString("recharge_phone","");
            selectedDemoDiamonds=state.getLong("recharge_demo_diamonds",8800L);
            selectedDemoCrystals=state.getLong("recharge_demo_crystals",590L);
        }
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.WHITE);
        setContentView(root);
        LinearLayout top=new LinearLayout(this);top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);top.setPadding(dp(8),dp(8),dp(14),dp(8));
        TextView back=item("‹",30,0xff212121,true);
        top.addView(back,new LinearLayout.LayoutParams(dp(58),dp(48)));
        back.setOnClickListener(v->finish());
        diamondTab=item("Diamonds",19,0xff202020,true);
        diamondTab.setGravity(Gravity.CENTER);
        crystalTab=item("Crystal",19,0xff929292,true);
        crystalTab.setGravity(Gravity.CENTER);
        top.addView(diamondTab,new LinearLayout.LayoutParams(0,dp(54),1));
        top.addView(crystalTab,new LinearLayout.LayoutParams(0,dp(54),1));
        root.addView(top);
        // Underline follows the selected tab, including Crystal on restore.
        LinearLayout stripes=new LinearLayout(this);stripes.setOrientation(LinearLayout.HORIZONTAL);
        View backSpacer=new View(this);
        stripes.addView(backSpacer,new LinearLayout.LayoutParams(dp(66),dp(4)));
        diamondUnderline=new View(this);crystalUnderline=new View(this);
        stripes.addView(diamondUnderline,new LinearLayout.LayoutParams(0,dp(4),1));
        stripes.addView(crystalUnderline,new LinearLayout.LayoutParams(0,dp(4),1));
        root.addView(stripes);
        View divider=new View(this);divider.setBackgroundColor(0xffececf0);
        root.addView(divider,new LinearLayout.LayoutParams(-1,dp(1)));
        diamondTab.setOnClickListener(v->{rememberShopForm();crystalSelected=false;renderRechargeShop();});
        crystalTab.setOnClickListener(v->{rememberShopForm();crystalSelected=true;renderRechargeShop();});
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        rechargeBody=new LinearLayout(this);rechargeBody.setOrientation(LinearLayout.VERTICAL);
        rechargeBody.setBackgroundColor(Color.WHITE);
        scroll.addView(rechargeBody);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        renderRechargeShop();
        if(signedInUid==null){
            status.setText("Sign in to your KING Plus account to see a verified balance.");
            return;
        }
        walletRegistration=FirebaseFirestore.getInstance().collection("wallets").document(signedInUid)
            .addSnapshotListener((doc,error)->{
                if(isFinishing()||isDestroyed()||!sameAccount())return;
                if(error!=null){
                    serverWalletConfirmed=false;
                    if(!crystalSelected){balance.setText("—");vip.setText("Verified wallet unavailable");}
                    status.setText("Wallet server read failed; no Diamonds changed.");return;
                }
                if(doc==null||!doc.exists()||doc.getMetadata().isFromCache()||
                   doc.getMetadata().hasPendingWrites()){
                    serverWalletConfirmed=false;
                    if(!crystalSelected){balance.setText("—");vip.setText("Verified balance not established");}
                    status.setText("Balance must be confirmed by SERVER. Payments are OFF.");
                    return;
                }
                long coins=doc.getLong("coins")==null?0:Math.max(0,doc.getLong("coins"));
                long points=doc.getLong("rechargeDiamondsTotal")==null?
                    0:Math.max(0,doc.getLong("rechargeDiamondsTotal"));
                long level=doc.getLong("vipLevel")==null?0:Math.max(0,doc.getLong("vipLevel"));
                serverWalletConfirmed=true;serverCoins=coins;serverPoints=points;serverVipLevel=level;
                serverBeans=doc.getLong("beans");
                if(!crystalSelected){
                    balance.setText(String.format(java.util.Locale.US,"%,d",coins));
                    vip.setText("VIP "+level+"  •  Verified recharge: "+points);
                    if(beansBalance!=null)beansBalance.setText(serverBeans==null?"Beans —  ›":
                        "Beans "+String.format(java.util.Locale.US,"%,d",Math.max(0,serverBeans))+"  ›");
                }
            });
        if(LIVE_RECHARGE)connectBilling();
    }

    private void rememberShopForm(){
        if(rechargeAgreement!=null)agreementAccepted=rechargeAgreement.isChecked();
        if(upiPhone!=null)enteredPhone=upiPhone.getText().toString();
    }
    @Override protected void onSaveInstanceState(Bundle out){
        rememberShopForm();
        out.putBoolean("recharge_crystal",crystalSelected);
        out.putBoolean("recharge_agreement",agreementAccepted);
        out.putString("recharge_method",selectedMethod);
        out.putString("recharge_phone",enteredPhone);
        out.putLong("recharge_demo_diamonds",selectedDemoDiamonds);
        out.putLong("recharge_demo_crystals",selectedDemoCrystals);
        super.onSaveInstanceState(out);
    }

    /** Native, usable recharge navigation: four methods, two balances, agreement and history. */
    private void renderRechargeShop(){
        if(rechargeBody==null)return;
        upiReady=false;
        upiRows=null;upiStatus=null;upiPreview=null;upiPhone=null;demoButtonView=null;demoSelectionView=null;
        rechargeBody.removeAllViews();
        if(diamondUnderline!=null)diamondUnderline.setBackgroundColor(crystalSelected?Color.WHITE:0xffffd02b);
        if(crystalUnderline!=null)crystalUnderline.setBackgroundColor(crystalSelected?0xffffd02b:Color.WHITE);
        diamondTab.setTextColor(crystalSelected?0xff969696:0xff151515);
        crystalTab.setTextColor(crystalSelected?0xff151515:0xff969696);
        diamondTab.setText(crystalSelected?"Diamonds":"Diamonds  ━");
        crystalTab.setText(crystalSelected?"Crystal  ━":"Crystal");
        LinearLayout hero=new LinearLayout(this);hero.setOrientation(LinearLayout.VERTICAL);
        hero.setBackground(goldenPanel());hero.setPadding(dp(10),dp(5),dp(10),dp(11));
        LinearLayout.LayoutParams h=new LinearLayout.LayoutParams(-1,-2);
        h.setMargins(dp(16),dp(16),dp(16),dp(18));rechargeBody.addView(hero,h);
        if(!crystalSelected){
            beansBalance=item("Beans —  ›",16,0xff946319,true);
            hero.addView(beansBalance);
            beansBalance.setOnClickListener(v->startActivity(new Intent(this,KingWalletVisualActivity.class)));
        }
        TextView heading=item(crystalSelected?"Crystal                DEMO History ›":"Diamonds                  History ›",16,0xff8f6738,false);
        heading.setOnClickListener(v->{if(crystalSelected)showDemoHistory();else showRechargeHistory();});hero.addView(heading);
        balance=item("—",32,0xff151515,true);
        balance.setText(crystalSelected?"—":balance.getText());
        if(crystalSelected)balance.setText("Not activated");
        else if(serverWalletConfirmed)balance.setText(String.format(java.util.Locale.US,"%,d",serverCoins));
        hero.addView(balance);
        vip=item(crystalSelected?"Crystal wallet not configured":serverWalletConfirmed?
            "VIP "+serverVipLevel+"  •  Verified recharge: "+serverPoints:
            "Checking verified wallet…",12,0xff876b4a,false);
        if(!crystalSelected&&serverWalletConfirmed&&beansBalance!=null)
            beansBalance.setText(serverBeans==null?"Beans —  ›":
                "Beans "+String.format(java.util.Locale.US,"%,d",Math.max(0,serverBeans))+"  ›");
        hero.addView(vip);
        if(crystalSelected){
            TextView crystalNotice=item("🔷  Not activated • Crystal wallet and purchase backend pending",13,0xff876b4a,false);
            hero.addView(crystalNotice);
        }
        status=item(crystalSelected?"Crystal recharge is unavailable. No payment can start.":
            "Prices and payment methods are provided by the verified merchant.",12,0xff836b54,false);
        rechargeBody.addView(status);
        demoSelectionView=item(selectedDemoSummary(),14,0xff5941a0,true);
        demoSelectionView.setBackground(bg(0xfff2efff));
        LinearLayout.LayoutParams selectedParams=new LinearLayout.LayoutParams(-1,-2);
        selectedParams.setMargins(dp(14),dp(6),dp(14),dp(8));
        rechargeBody.addView(demoSelectionView,selectedParams);
        if(crystalSelected){
            // The Crystal tab uses the same navigable payment-method accordion,
            // but must never pretend that a separate Crystal ledger or gateway exists.
            methodHeader("UPI", "UPI", true);
            if("UPI".equals(selectedMethod))crystalMethodPreview("UPI");
            methodHeader("Agent", "Agent", false);
            if("Agent".equals(selectedMethod))crystalMethodPreview("Agent");
            methodHeader("Card", "Card", false);
            if("Card".equals(selectedMethod))crystalMethodPreview("Card");
            methodHeader("Google Pay", "Google Pay", false);
            if("Google Pay".equals(selectedMethod))crystalMethodPreview("Google Pay");
            historyAndAgreement();
            return;
        }
        methodHeader("UPI", "UPI", true);
        if("UPI".equals(selectedMethod)){
            if(signedInUid==null){TextView warning=item("Sign in to view merchant-confirmed UPI packs.",13,0xff987545,false);rechargeBody.addView(warning);previewPackRows(false);}
            else addUpiSection(rechargeBody);
        }
        methodHeader("Agent", "Agent", false);
        if("Agent".equals(selectedMethod)){
            TextView portal=item("Open Agent recharge portal  ›",16,0xff95641a,true);
            portal.setOnClickListener(v->startActivity(new Intent(this,KingAgency9982Activity.class)));
            rechargeBody.addView(portal);
            rechargeBody.addView(item("Only authorized agents can access server-confirmed orders. Agent requests cannot credit real Diamonds without verified payment.",12,0xff777777,false));
            previewPackRows(false);
        }
        methodHeader("Card", "Card", false);
        if("Card".equals(selectedMethod)){
            rechargeBody.addView(item("Card payment • not independently enabled",15,0xff685c55,true));
            rechargeBody.addView(item("Visa/Mastercard can be used only if the approved payment gateway offers them. This app does not collect or save card numbers.",13,0xff777777,false));
            previewPackRows(false);
        }
        methodHeader("Google Pay", "Google Pay", false);
        if("Google Pay".equals(selectedMethod)){
            rechargeBody.addView(item("Google Pay • available only through an approved UPI gateway",15,0xff685c55,true));
            rechargeBody.addView(item("This is a no-charge preview. Actual Google Pay availability depends on the approved merchant gateway and applicable store rules.",13,0xff777777,false));
            previewPackRows(false);
        }
        historyAndAgreement();
    }

    private void methodHeader(String label,String icon,boolean first){
        LinearLayout bar=new LinearLayout(this);bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);bar.setPadding(dp(16),dp(10),dp(16),dp(10));
        TextView title=item(("UPI".equals(label)?"🇮🇳  ":"Agent".equals(label)?"💠  ":
            "Card".equals(label)?"💳  ":"🟡  ")+label,17,0xff1e1e1e,true);
        bar.addView(title,new LinearLayout.LayoutParams(0,dp(60),1));
        TextView arrow=item(label.equals(selectedMethod)?"⌃":"⌄",21,0xff333333,true);
        bar.addView(arrow);rechargeBody.addView(bar);
        View line=new View(this);line.setBackgroundColor(0xffeeeeef);
        rechargeBody.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));
        bar.setOnClickListener(v->{
            rememberShopForm();
            selectedMethod=label.equals(selectedMethod)?"":label;
            renderRechargeShop();
        });
    }

    /** A consistent row for preview and server-approved packages. Never invent a cash price. */
    private void addPackageRow(LinearLayout list,boolean crystal,long count,
                               String subtitle,String price,boolean payable,Runnable onBuy){
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(12),dp(12),dp(12),dp(12));
        row.setBackground(packBackground());
        TextView gem=item(crystal?"🔷":"💎",27,crystal?0xff167eab:0xffe9a900,true);
        gem.setGravity(Gravity.CENTER);
        gem.setBackground(bg(crystal?0xffe3f5ff:0xfffff0bf));
        row.addView(gem,new LinearLayout.LayoutParams(dp(58),dp(58)));
        LinearLayout details=new LinearLayout(this);details.setOrientation(LinearLayout.VERTICAL);
        TextView amount=item(String.format(java.util.Locale.US,"%,d",count),19,0xff1c1c23,true);
        amount.setPadding(dp(10),dp(3),dp(4),0);details.addView(amount);
        TextView hint=item(subtitle,12,0xff8c8e9a,false);
        hint.setPadding(dp(10),dp(3),dp(4),0);details.addView(hint);
        row.addView(details,new LinearLayout.LayoutParams(0,dp(66),1));
        TextView badge=item(price,13,payable?0xffba8715:0xff997f51,true);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(bg(Color.WHITE));row.addView(badge);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(91));
        lp.setMargins(dp(12),dp(6),dp(12),dp(4));list.addView(row,lp);
        row.setContentDescription(String.format(java.util.Locale.US,"%,d",count)+
            (crystal?" Crystal":" Diamonds")+". "+subtitle+". "+price);
        row.setOnClickListener(v->{
            if(onBuy!=null)onBuy.run();
            else Toast.makeText(this,"Package unavailable for payment. No charge made.",Toast.LENGTH_LONG).show();
        });
    }

    /** Preview package quantities are examples, NEVER cash prices or live SKUs. */
    private void previewPackRows(boolean crystal){
        LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);
        list.setBackgroundColor(0xfff8f8fc);rechargeBody.addView(list);
        int[] examples=crystal?new int[]{590,900,2250,4500,9000,22500}:
            new int[]{100,600,1300,8800,17800,45000,90000,180000,450000};
        for(int count:examples){
            final long quantity=count;
            addPackageRow(list,crystal,quantity,"Select to try • no real purchase",
                "DEMO ₹0",false,()->chooseDemoPack(crystal,quantity));
        }
    }

    private void crystalMethodPreview(String method){
        rechargeBody.addView(item("Crystal • "+method+" • preview only",15,0xff795d2e,true));
        rechargeBody.addView(item("Crystal wallet, agent delivery and real payment are not active. Pick a sample pack for a no-money UI trial; the real balance never changes.",13,0xff806d54,false));
        previewPackRows(true);
    }

    private void chooseDemoPack(boolean crystal,long quantity){
        if(quantity<=0)return;
        String currency=crystal?"Crystals":"Diamonds";
        new AlertDialog.Builder(this).setTitle("Select "+currency+" sample pack")
            .setMessage(String.format(java.util.Locale.US,"%,d",quantity)+" "+currency+" • "+
                selectedMethod+" preview\nAmount charged: ₹0\nNo real Wallet balance or VIP changes.")
            .setPositiveButton("Select for DEMO",(dialog,which)->{
                if(crystal)selectedDemoCrystals=quantity;
                else selectedDemoDiamonds=quantity;
                if(demoButtonView!=null)demoButtonView.setText(demoButtonLabel());
                if(demoSelectionView!=null)demoSelectionView.setText(selectedDemoSummary());
                Toast.makeText(this,"DEMO pack selected. Use Try Recharge below.",Toast.LENGTH_LONG).show();
            })
            .setNegativeButton("Cancel",null).show();
    }
    private String selectedDemoSummary(){
        return "Selected preview: "+String.format(java.util.Locale.US,"%,d",
            crystalSelected?selectedDemoCrystals:selectedDemoDiamonds)+
            (crystalSelected?" Crystal":" Diamonds")+" • "+
            (selectedMethod.isEmpty()?"Choose method":selectedMethod)+" • ₹0";
    }
    private String demoButtonLabel(){
        long n=crystalSelected?selectedDemoCrystals:selectedDemoDiamonds;
        return "▶ Try "+String.format(java.util.Locale.US,"%,d",n)+
            (crystalSelected?" Crystal":" Diamond")+" (DEMO • ₹0)";
    }
    /** All demo receipts are private to the currently signed-in account on THIS device.
     * Nothing here is a financial receipt, a payment, or a Firestore wallet entry. */
    private String demoReceiptsKey(String uid){return "king_recharge_demo_receipts_v10060_"+uid;}

    private JSONArray demoReceipts(String uid){
        String json=getPreferences(MODE_PRIVATE).getString(demoReceiptsKey(uid),"[]");
        try{return new JSONArray(json);}catch(JSONException invalid){return new JSONArray();}
    }

    private void appendDemoReceipt(String account,boolean crystal,String method,long amount){
        JSONArray previous=demoReceipts(account);
        JSONArray next=new JSONArray();
        JSONObject receipt=new JSONObject();
        try{
            receipt.put("id","DEMO-"+java.util.UUID.randomUUID().toString().substring(0,12).toUpperCase(java.util.Locale.US));
            receipt.put("when",System.currentTimeMillis());
            receipt.put("currency",crystal?"Crystal":"Diamonds");
            receipt.put("method",Arrays.asList("UPI","Agent","Card","Google Pay").contains(method)?method:"UPI");
            receipt.put("quantity",amount);
            receipt.put("chargedPaise",0);
            receipt.put("credited",0);
            next.put(receipt);
            // Keep only 30 local DEMO receipts; never store payment credentials or card data.
            for(int i=0;i<previous.length()&&i<29;i++){
                JSONObject old=previous.optJSONObject(i);
                if(old!=null)next.put(old);
            }
        }catch(JSONException invalid){return;}
        getPreferences(MODE_PRIVATE).edit().putString(demoReceiptsKey(account),next.toString()).apply();
    }

    private void showDemoReceipt(JSONObject receipt){
        if(!sameAccount()||receipt==null)return;
        long when=receipt.optLong("when",0L);
        String date=when<=0?"Unknown":new java.text.SimpleDateFormat("dd MMM yyyy, HH:mm",java.util.Locale.US)
            .format(new java.util.Date(when));
        String info="Receipt: "+receipt.optString("id","DEMO")+
            "\nDate: "+date+
            "\nSample pack: "+String.format(java.util.Locale.US,"%,d",receipt.optLong("quantity",0L))+
            " "+receipt.optString("currency","Diamonds")+
            "\nMethod preview: "+receipt.optString("method","UPI")+
            "\nAmount charged: ₹0\nReal wallet credit: 0\nStatus: DEMO completed"+
            "\n\nDevice-only test receipt. Not proof of a payment.";
        new AlertDialog.Builder(this).setTitle("DEMO receipt • no money")
            .setMessage(info).setPositiveButton("Close",null).show();
    }

    private void listDemoReceipts(String currencyFilter,String methodFilter){
        if(!sameAccount())return;
        final String account=signedInUid;
        JSONArray all=demoReceipts(account);
        ArrayList<JSONObject> receipts=new ArrayList<>();
        ArrayList<String> labels=new ArrayList<>();
        for(int i=0;i<all.length();i++){
            JSONObject r=all.optJSONObject(i);
            if(r==null)continue;
            if(!"All".equals(currencyFilter)&&!currencyFilter.equals(r.optString("currency")))continue;
            if(!"All".equals(methodFilter)&&!methodFilter.equals(r.optString("method")))continue;
            long when=r.optLong("when",0);
            String day=when>0?new java.text.SimpleDateFormat("dd MMM, HH:mm",java.util.Locale.US)
                .format(new java.util.Date(when)):"DEMO";
            labels.add(day+" • "+String.format(java.util.Locale.US,"%,d",r.optLong("quantity",0))+" "+
                r.optString("currency","Diamonds")+" • "+r.optString("method","UPI")+" • ₹0");
            receipts.add(r);
        }
        if(labels.isEmpty()){
            new AlertDialog.Builder(this).setTitle("DEMO history")
                .setMessage("No local DEMO receipts match this filter. No real payment was made.")
                .setPositiveButton("Back",(d,w)->showDemoHistory()).show();return;
        }
        new AlertDialog.Builder(this).setTitle("DEMO receipts • "+currencyFilter+" / "+methodFilter)
            .setItems(labels.toArray(new String[0]),(d,index)->{
                if(sameAccount()&&account.equals(signedInUid))showDemoReceipt(receipts.get(index));
            }).setPositiveButton("Close",null).show();
    }

    private void chooseDemoMethodFilter(String currencyFilter){
        if(!sameAccount())return;
        final String[] methods={"All methods","UPI","Agent","Card","Google Pay"};
        new AlertDialog.Builder(this).setTitle("Filter DEMO payment method")
            .setItems(methods,(d,index)->listDemoReceipts(currencyFilter,index==0?"All":methods[index]))
            .setNegativeButton("Cancel",null).show();
    }

    private void clearDemoReceipts(){
        if(!sameAccount())return;
        final String account=signedInUid;
        new AlertDialog.Builder(this).setTitle("Clear local DEMO history?")
            .setMessage("This clears only DEMO receipts saved on this phone for this account. Real payment orders and Diamond balances remain unchanged.")
            .setPositiveButton("Clear DEMO history",(d,w)->{
                if(!sameAccount()||!account.equals(signedInUid))return;
                getPreferences(MODE_PRIVATE).edit()
                    .remove(demoReceiptsKey(account))
                    .remove("king_recharge_demo_history_"+account)
                    .remove("king_recharge_demo_last_"+account).apply();
                rememberShopForm();renderRechargeShop();
                Toast.makeText(this,"Local DEMO receipts cleared. Real orders untouched.",Toast.LENGTH_LONG).show();
            }).setNegativeButton("Cancel",null).show();
    }

    private void showDemoHistory(){
        if(!sameAccount()){
            new AlertDialog.Builder(this).setMessage("Sign in to view local DEMO receipts.")
                .setPositiveButton("OK",null).show();return;
        }
        final String account=signedInUid;
        final String legacy=getPreferences(MODE_PRIVATE).getString("king_recharge_demo_history_"+account,"");
        final String[] choices={"All DEMO receipts", "Diamonds only", "Crystal only",
            "UPI only", "Agent only", "Card only", "Google Pay only",
            "Previous DEMO history (older versions)", "Clear local DEMO history"};
        new AlertDialog.Builder(this).setTitle("DEMO history • no real payments")
            .setItems(choices,(dialog,index)->{
                if(!sameAccount()||!account.equals(signedInUid))return;
                switch(index){
                    case 0:chooseDemoMethodFilter("All");break;
                    case 1:chooseDemoMethodFilter("Diamonds");break;
                    case 2:chooseDemoMethodFilter("Crystal");break;
                    case 3:listDemoReceipts("All","UPI");break;
                    case 4:listDemoReceipts("All","Agent");break;
                    case 5:listDemoReceipts("All","Card");break;
                    case 6:listDemoReceipts("All","Google Pay");break;
                    case 7:new AlertDialog.Builder(this).setTitle("Older DEMO records")
                        .setMessage(legacy.isEmpty()?"No records from earlier versions.":legacy+"\nDevice-only DEMO records; not actual payments.")
                        .setPositiveButton("Close",null).show();break;
                    case 8:clearDemoReceipts();break;
                    default:break;
                }
            }).setNegativeButton("Close",null).show();
    }

    private void historyAndAgreement(){
        TextView history=item(crystalSelected?"Crystal DEMO history (no payment)   ›":
            "Verified Diamond payment history   ›",14,0xff8f8f8f,true);
        history.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(56));
        lp.setMargins(dp(12),dp(16),dp(12),dp(2));rechargeBody.addView(history,lp);
        history.setOnClickListener(v->{if(crystalSelected)showDemoHistory();else showRechargeHistory();});
        rechargeAgreement=new CheckBox(this);
        rechargeAgreement.setText("I agree to Recharge Agreement");
        rechargeAgreement.setChecked(agreementAccepted);
        rechargeAgreement.setOnCheckedChangeListener((button,checked)->agreementAccepted=checked);
        rechargeAgreement.setTextColor(0xff686868);
        rechargeAgreement.setTextSize(13);
        rechargeBody.addView(rechargeAgreement);
        TextView agreement=item("Read Recharge Agreement  ›",13,0xff3b98b1,false);
        agreement.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("KING Plus Recharge Agreement")
            .setMessage("Payments can start only when a verified merchant and backend are enabled. All prices are approved on the server. Diamonds are non-withdrawable in-app items. The payment gateway verifies every receipt; no Diamonds are credited from screenshots or local confirmations. For cancellations, refunds and applicable store billing terms, use the merchant agreement when activated.")
            .setPositiveButton("OK",null).show());
        rechargeBody.addView(agreement);
        rechargeBody.addView(item("TEST mode • Real checkout disabled. Packages, methods and receipts can be tried with ₹0 DEMO; no wallet changes.",12,0xff9b7637,false));
        addSafeDemoTrial();
    }

    /**
     * Safe UI-only recharge trial for users without Cashfree sandbox credentials.
     * This NEVER calls a payment gateway, never writes to Firestore and never
     * changes the real server-authoritative Diamond / Crystal / VIP balance.
     * An actual Cashfree sandbox payment still requires merchant sandbox keys.
     */
    private void addSafeDemoTrial(){
        TextView demoHistory=item("🧾 DEMO history (device only)   ›",14,0xff654a87,true);
        rechargeBody.addView(demoHistory);
        demoHistory.setOnClickListener(v->showDemoHistory());
        demoButtonView=item(demoButtonLabel(),16,0xff25204c,true);
        demoButtonView.setBackground(bg(0xffeee9ff));
        LinearLayout.LayoutParams buttonParams=new LinearLayout.LayoutParams(-1,dp(60));
        buttonParams.setMargins(dp(14),dp(16),dp(14),dp(6));
        rechargeBody.addView(demoButtonView,buttonParams);
        TextView receipt=item("DEMO only • ₹0 charged • no real wallet changes",12,0xff654a87,false);
        rechargeBody.addView(receipt);
        if(sameAccount()){
            String previous=getPreferences(MODE_PRIVATE).getString("king_recharge_demo_last_"+signedInUid,null);
            if(previous!=null)receipt.setText(previous);
        }
        demoButtonView.setOnClickListener(view->{
            if(!sameAccount()){
                Toast.makeText(this,"Sign in to try Recharge DEMO.",Toast.LENGTH_LONG).show();return;
            }
            if(rechargeAgreement==null||!rechargeAgreement.isChecked()){
                Toast.makeText(this,"Accept Recharge Agreement before the DEMO.",Toast.LENGTH_LONG).show();return;
            }
            final boolean crystal=crystalSelected;
            final String method=selectedMethod.isEmpty()?"UPI":selectedMethod;
            final long chosen=crystal?selectedDemoCrystals:selectedDemoDiamonds;
            final String currency=crystal?"Crystal":"Diamond";
            final String account=signedInUid;
            final TextView currentReceipt=receipt;
            new AlertDialog.Builder(this)
                .setTitle("Simulated "+method+" checkout")
                .setMessage("Sample pack: "+String.format(java.util.Locale.US,"%,d",chosen)+" "+currency+
                    "\nPayment method preview: "+method+
                    "\nAmount charged: ₹0\nNo gateway, UPI app or card entry will open."+
                    "\nNo real Diamonds, Crystal, Beans or VIP points will be credited.")
                .setPositiveButton("Complete DEMO",(dialog,which)->{
                    if(!sameAccount()||!account.equals(signedInUid)||
                       isFinishing()||isDestroyed())return;
                    appendDemoReceipt(account,crystal,method,chosen);
                    String summary="DEMO finished • ₹0 charged • 0 real "+currency+" credited";
                    getPreferences(MODE_PRIVATE).edit()
                        .putString("king_recharge_demo_last_"+account,summary).apply();
                    if(currentReceipt.getParent()!=null)currentReceipt.setText(summary);
                    new AlertDialog.Builder(this).setTitle("DEMO Recharge completed")
                        .setMessage("Sample "+currency+" package and "+method+" selection completed. " +
                            "This was only a UI trial: no payment was made and no real wallet values changed. " +
                            "You can open DEMO history below the agreement.")
                        .setPositiveButton("See DEMO history",(d,w)->showDemoHistory())
                        .setNegativeButton("Close",null).show();
                })
                .setNegativeButton("Cancel",null).show();
        });
    }

    @SuppressWarnings("unchecked")
    private void showRechargeHistory(){
        if(!sameAccount()){
            new AlertDialog.Builder(this).setMessage("Sign in to see your recharge history.").setPositiveButton("OK",null).show();return;
        }
        FirebaseFunctions.getInstance().getHttpsCallable("getCashfreeRechargeHistory").call()
            .addOnSuccessListener(result->{
                if(!sameAccount()||isFinishing()||isDestroyed())return;
                Object data=result.getData();
                if(!(data instanceof Map)||!(((Map<?,?>)data).get("orders") instanceof List)){
                    new AlertDialog.Builder(this).setTitle("Recharge history unavailable")
                        .setMessage("The server did not return a verified order list. Try again later.")
                        .setPositiveButton("OK",null).show();return;
                }
                Object value=((Map<?,?>)data).get("orders");
                List<?> rows=(List<?>)value;
                if(rows.isEmpty()){
                    new AlertDialog.Builder(this).setTitle("Recharge history")
                        .setMessage("No server-recorded UPI recharge orders yet.")
                        .setPositiveButton("OK",null).show();return;
                }
                ArrayList<String> labels=new ArrayList<>();
                ArrayList<String> orderIds=new ArrayList<>();
                ArrayList<String> orderStates=new ArrayList<>();
                for(Object o:rows){
                    if(!(o instanceof Map))continue;
                    Map<?,?> m=(Map<?,?>)o;
                    String id=String.valueOf(m.get("orderId"));
                    if(!id.matches("king_[a-f0-9]{32}"))continue;
                    Object amount=m.get("amountPaise"),quantity=m.get("diamonds");
                    if(!(amount instanceof Number)||!(quantity instanceof Number))continue;
                    String state=String.valueOf(m.get("status"));
                    labels.add("💎 "+quantity+" • ₹"+String.format(java.util.Locale.US,"%.2f",((Number)amount).doubleValue()/100.0)+" • "+state);
                    orderIds.add(id);
                    orderStates.add(state);
                }
                if(labels.isEmpty()){
                    new AlertDialog.Builder(this).setTitle("Recharge history")
                        .setMessage("No verified order records to display.")
                        .setPositiveButton("OK",null).show();return;
                }
                new AlertDialog.Builder(this).setTitle("Recharge history")
                    .setItems(labels.toArray(new String[0]),(d,which)->{
                        String id=orderIds.get(which);
                        String orderState=orderStates.get(which);
                        if("credited".equals(orderState)){
                            new AlertDialog.Builder(this).setTitle("Verified Recharge Receipt")
                                .setMessage("This recharge is already credited. Your server Wallet holds the verified balance. Do not pay again.")
                                .setPositiveButton("OK",null).show();return;
                        }
                        // An old paid order must remain verifiable even when NEW sales
                        // and the new-price catalog are disabled. Preserve this exact ID.
                        getPreferences(MODE_PRIVATE).edit().putString("cashfree_pending_"+signedInUid,id).apply();
                        cashfreePendingOrder=id;
                        if(crystalSelected||!"UPI".equals(selectedMethod)){
                            rememberShopForm();crystalSelected=false;selectedMethod="UPI";renderRechargeShop();
                            cashfreePendingOrder=id;
                        }
                        new AlertDialog.Builder(this).setTitle("Existing Recharge Order")
                            .setMessage("Use Check Payment if already paid, or Continue Payment to reopen the same verified Cashfree link. No new order will be created.")
                            .setPositiveButton("Check Payment",(dialog,button)->confirmUpiOrder())
                            .setNeutralButton("Continue Payment",(dialog,button)->resumeUpiOrder())
                            .setNegativeButton("Cancel",null).show();
                    }).setPositiveButton("Close",null).show();
            }).addOnFailureListener(err->{
                if(sameAccount()&&!isFinishing()&&!isDestroyed())new AlertDialog.Builder(this)
                    .setTitle("Recharge history unavailable")
                    .setMessage("Could not load orders from the server. No balance has changed.")
                    .setPositiveButton("OK",null).show();
            });
    }

    /** Cashfree is a separate merchant-verified UPI path. Play Billing stays OFF. */
    @SuppressWarnings("unchecked")
    private void addUpiSection(LinearLayout column){
        TextView header=item("UPI recharge • merchant-confirmed INR price",15,0xff976d2a,true);
        column.addView(header);
        upiStatus=item("Checking secure UPI merchant setup…",13,0xff737373,false);
        column.addView(upiStatus);
        upiPhone=new EditText(this);
        upiPhone.setSingleLine(true);
        upiPhone.setTextColor(0xff222222);
        upiPhone.setHintTextColor(0xff999999);
        upiPhone.setHint("10-digit mobile for payment");
        upiPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        String authPhone=signedIn.getPhoneNumber();
        if(authPhone!=null&&authPhone.matches("\\+91[6-9][0-9]{9}"))upiPhone.setText(authPhone.substring(3));
        if(enteredPhone.matches("[6-9][0-9]{0,9}"))upiPhone.setText(enteredPhone);
        column.addView(upiPhone);
        upiRows=new LinearLayout(this);
        upiRows.setOrientation(LinearLayout.VERTICAL);
        column.addView(upiRows);
        previewPackRows(false);
        upiPreview=(LinearLayout)rechargeBody.getChildAt(rechargeBody.getChildCount()-1);
        final LinearLayout currentRows=upiRows;
        final TextView currentStatus=upiStatus;
        TextView verify=item("↻ Check Payment / Receive Diamonds",15,Color.WHITE,true);
        verify.setBackground(bg(0xff393866));
        column.addView(verify);
        verify.setOnClickListener(v->{
            if(cashfreePendingOrder==null)recoverUpiOrder();
            else confirmUpiOrder();
        });
        TextView recovery=item("↻ Recover Pending Order / Payment History",14,0xffdbd3ff,true);
        recovery.setBackground(bg(0xff263650));
        column.addView(recovery);
        recovery.setOnClickListener(v->recoverUpiOrder());
        TextView continuePayment=item("↗ Continue Previous Payment (same Order)",14,0xff35385e,true);
        column.addView(continuePayment);
        continuePayment.setOnClickListener(v->{
            if(cashfreePendingOrder==null)recoverUpiOrder();
            else resumeUpiOrder();
        });
        cashfreePendingOrder=getPreferences(MODE_PRIVATE).getString("cashfree_pending_"+signedInUid,null);
        if(cashfreePendingOrder==null)recoverUpiOrder();
        FirebaseFunctions.getInstance().getHttpsCallable("getCashfreeRechargeCatalog").call()
            .addOnSuccessListener(result->{
                if(!sameAccount()||isFinishing()||isDestroyed()||crystalSelected||
                   !"UPI".equals(selectedMethod)||upiRows!=currentRows||upiStatus!=currentStatus)return;
                Object raw=result.getData();
                if(!(raw instanceof Map)){upiStatus.setText("Merchant catalog unavailable. No payment started.");return;}
                Map<String,Object> data=(Map<String,Object>)raw;
                if(!Boolean.TRUE.equals(data.get("enabled"))){
                    upiStatus.setText("Preview packs only. UPI Recharge is not enabled by a verified merchant.");return;
                }
                Object packs=data.get("packs");
                if(!(packs instanceof List)){upiStatus.setText("Payment prices unavailable.");return;}
                boolean hasApproved=false;
                upiStatus.setText("Checking server-approved recharge packages…");
                for(Object rawPack:(List<?>)packs){
                    if(!(rawPack instanceof Map))continue;
                    Map<String,Object> pack=(Map<String,Object>)rawPack;
                    String id=String.valueOf(pack.get("productId"));
                    if(!Arrays.asList(UPI_PRODUCT_IDS).contains(id))continue;
                    Object amount=pack.get("amountPaise");
                    Object count=pack.get("diamonds");
                    if(!(amount instanceof Number)||!(count instanceof Number))continue;
                    long paise=((Number)amount).longValue();
                    if(paise<100||paise>10000000)continue;
                    if(((Number)count).longValue()<1)continue;
                    final String approvedId=id;
                    hasApproved=true;
                    final long packDiamonds=((Number)count).longValue();
                    addPackageRow(upiRows,false,packDiamonds,
                        "Server-approved price • purchase disabled in TEST",
                        "₹"+String.format(java.util.Locale.US,"%,.2f",paise/100.0),
                        LIVE_CASHFREE_RECHARGE,()->{
                            if(!LIVE_CASHFREE_RECHARGE){
                                chooseDemoPack(false,packDiamonds);return;
                            }
                            if(rechargeAgreement==null||!rechargeAgreement.isChecked()){
                                Toast.makeText(this,"Accept Recharge Agreement before continuing.",Toast.LENGTH_LONG).show();return;
                            }
                            startUpiLink(approvedId);
                        });
                }
                upiReady=hasApproved&&LIVE_CASHFREE_RECHARGE;
                if(hasApproved){
                    if(upiPreview!=null)upiPreview.setVisibility(View.GONE);
                    upiStatus.setText("Server catalog checked • purchases disabled in this TEST build. DEMO only.");
                }else upiStatus.setText("Merchant has not approved any recharge packs. Preview only.");
            }).addOnFailureListener(error->{
                if(sameAccount()&&!isFinishing()&&!crystalSelected&&upiRows==currentRows)
                    upiStatus.setText("UPI merchant setup unavailable. No payment requested.");
            });
    }

    private void startUpiLink(String sku){
        if(!LIVE_CASHFREE_RECHARGE||!upiReady||!sameAccount()||cashfreeVerifying){
            Toast.makeText(this,"Real-money Recharge is disabled in this TEST build.",Toast.LENGTH_LONG).show();return;
        }
        String phone=upiPhone.getText().toString().trim();
        if(!phone.matches("[6-9][0-9]{9}")){
            Toast.makeText(this,"Enter 10-digit India mobile number",Toast.LENGTH_LONG).show();return;
        }
        cashfreeVerifying=true;
        upiStatus.setText("Creating secure payment link…");
        Map<String,Object> data=new HashMap<>();data.put("productId",sku);data.put("phone",phone);
        FirebaseFunctions.getInstance().getHttpsCallable("createCashfreeRechargeLink").call(data)
            .addOnSuccessListener(result->{
                cashfreeVerifying=false;
                if(!sameAccount()||isFinishing()||isDestroyed())return;
                if(!(result.getData() instanceof Map))return;
                Map<?,?> row=(Map<?,?>)result.getData();
                String id=String.valueOf(row.get("orderId"));
                String url=String.valueOf(row.get("checkoutUrl"));
                Uri uri=Uri.parse(url);
                boolean secure="https".equals(uri.getScheme())&&
                    ("payments.cashfree.com".equals(uri.getHost())||
                     "payments-test.cashfree.com".equals(uri.getHost()))&&
                    uri.getPath()!=null&&uri.getPath().startsWith("/links/")&&
                    uri.getUserInfo()==null;
                if(!id.matches("king_[a-f0-9]{32}")||!secure){
                    upiStatus.setText("Gateway returned unverified payment URL; payment was blocked.");return;
                }
                cashfreePendingOrder=id;
                getPreferences(MODE_PRIVATE).edit().putString("cashfree_pending_"+signedInUid,id).apply();
                upiStatus.setText("Checkout opened. Diamonds appear ONLY after server confirms payment.");
                try{startActivity(new Intent(Intent.ACTION_VIEW,uri));}
                catch(Exception ex){upiStatus.setText("Payment browser unavailable; payment not confirmed.");}
            }).addOnFailureListener(error->{
                cashfreeVerifying=false;
                if(sameAccount()&&!isFinishing())upiStatus.setText("UPI unavailable: "+error.getLocalizedMessage());
            });
    }

    /** Server-owned orders let a customer recover payment after reinstall or timeout. */
    @SuppressWarnings("unchecked")
    private void recoverUpiOrder(){
        if(!sameAccount()||cashfreeVerifying||upiStatus==null)return;
        cashfreeVerifying=true;
        final String account=signedInUid;
        upiStatus.setText("Checking previous orders securely…");
        FirebaseFunctions.getInstance().getHttpsCallable("getCashfreeRechargeHistory").call()
            .addOnSuccessListener(result->{
                cashfreeVerifying=false;
                if(!sameAccount()||!account.equals(signedInUid)||isFinishing()||isDestroyed())return;
                if(!(result.getData() instanceof Map))return;
                Object raw=((Map<?,?>)result.getData()).get("orders");
                if(!(raw instanceof List))return;
                for(Object o:(List<?>)raw){
                    if(!(o instanceof Map))continue;
                    Map<?,?> order=(Map<?,?>)o;
                    String id=String.valueOf(order.get("orderId"));
                    String status=String.valueOf(order.get("status"));
                    if(id.matches("king_[a-f0-9]{32}")&&
                        ("pending".equals(status)||"creating".equals(status)||
                         "verify-needed".equals(status)||"gateway-error".equals(status))){
                        cashfreePendingOrder=id;
                        getPreferences(MODE_PRIVATE).edit().putString("cashfree_pending_"+account,id).apply();
                        upiStatus.setText("Previous order found. Tap Check Payment for server verification.");
                        return;
                    }
                }
                upiStatus.setText("No pending order found. Past credited orders are securely recorded.");
            }).addOnFailureListener(error->{
                cashfreeVerifying=false;
                if(sameAccount()&&!isFinishing()&&!isDestroyed())
                    upiStatus.setText("History unavailable. Your payment order remains saved on the server.");
            });
    }

    /** A verified older payment link can be reopened without generating another order. */
    @SuppressWarnings("unchecked")
    private void resumeUpiOrder(){
        if(!LIVE_CASHFREE_RECHARGE){
            new AlertDialog.Builder(this).setTitle("Payment not available in TEST")
                .setMessage("The old order remains saved. You may use Check Payment to verify an earlier payment, but this TEST build will not open a real-money checkout.")
                .setPositiveButton("OK",null).show();return;
        }
        if(!sameAccount()||cashfreeVerifying||cashfreePendingOrder==null)return;
        if(rechargeAgreement==null||!rechargeAgreement.isChecked()){
            Toast.makeText(this,"Accept Recharge Agreement before continuing.",Toast.LENGTH_LONG).show();return;
        }
        final String id=cashfreePendingOrder;
        final String account=signedInUid;
        final TextView currentStatus=upiStatus;
        cashfreeVerifying=true;
        upiStatus.setText("Checking original order and payment link on server…");
        Map<String,Object> args=new HashMap<>();args.put("orderId",id);
        FirebaseFunctions.getInstance().getHttpsCallable("resumeCashfreeRecharge").call(args)
            .addOnSuccessListener(result->{
                cashfreeVerifying=false;
                if(!sameAccount()||!account.equals(signedInUid)||isFinishing()||isDestroyed()||
                   upiStatus!=currentStatus||!id.equals(cashfreePendingOrder))return;
                if(!(result.getData() instanceof Map))return;
                Map<?,?> data=(Map<?,?>)result.getData();
                String state=String.valueOf(data.get("status"));
                if("paid".equals(state)){
                    upiStatus.setText("Payment received. Checking verified Diamond credit…");
                    confirmUpiOrder();return;
                }
                if("expired".equals(state)||"cancelled".equals(state)){
                    cashfreePendingOrder=null;
                    getPreferences(MODE_PRIVATE).edit().remove("cashfree_pending_"+signedInUid).apply();
                    upiStatus.setText("Payment link "+state+" (confirmed by Cashfree). No Diamonds credited. You can choose a new pack.");
                    return;
                }
                if(!Boolean.TRUE.equals(data.get("ok"))||!"pending".equals(state)){
                    upiStatus.setText("This order is not payable. Check Payment or contact support.");return;
                }
                String url=String.valueOf(data.get("checkoutUrl"));
                Uri uri=Uri.parse(url);
                String host=uri.getHost();
                if(!"https".equals(uri.getScheme())||
                  !("payments.cashfree.com".equals(host)||"payments-test.cashfree.com".equals(host))||
                  uri.getPath()==null||!uri.getPath().startsWith("/links/")){
                    upiStatus.setText("Unsafe payment link blocked. No new order created.");return;
                }
                try{
                    startActivity(new Intent(Intent.ACTION_VIEW,uri));
                    upiStatus.setText("Existing Cashfree payment reopened. Check Payment when done.");
                }catch(Exception ex){upiStatus.setText("No browser available. Original order remains saved.");}
            }).addOnFailureListener(error->{
                cashfreeVerifying=false;
                if(sameAccount()&&!isFinishing()&&!isDestroyed()&&upiStatus==currentStatus)
                    upiStatus.setText("Payment recovery unavailable. Original order remains saved.");
            });
    }

    @SuppressWarnings("unchecked")
    private void confirmUpiOrder(){
        // Verifying an existing payment is independent of whether new sales are enabled.
        if(!sameAccount()||cashfreeVerifying||cashfreePendingOrder==null)return;
        final String id=cashfreePendingOrder;
        if(upiStatus==null){
            rememberShopForm();crystalSelected=false;selectedMethod="UPI";renderRechargeShop();
        }
        cashfreeVerifying=true;
        upiStatus.setText("Checking payment with Cashfree secure server…");
        Map<String,Object> arg=new HashMap<>();arg.put("orderId",id);
        FirebaseFunctions.getInstance().getHttpsCallable("confirmCashfreeRecharge").call(arg)
            .addOnSuccessListener(result->{
                cashfreeVerifying=false;
                if(!sameAccount()||isFinishing()||isDestroyed()||!id.equals(cashfreePendingOrder))return;
                if(!(result.getData() instanceof Map))return;
                Map<?,?> data=(Map<?,?>)result.getData();
                if(Boolean.TRUE.equals(data.get("ok"))){
                    upiStatus.setText("Verified payment! Diamonds credited by server to your Wallet.");
                    cashfreePendingOrder=null;
                    getPreferences(MODE_PRIVATE).edit().remove("cashfree_pending_"+signedInUid).apply();
                }else{
                    String paymentStatus=String.valueOf(data.get("status"));
                    if("expired".equals(paymentStatus)||"cancelled".equals(paymentStatus)){
                        cashfreePendingOrder=null;
                        getPreferences(MODE_PRIVATE).edit().remove("cashfree_pending_"+signedInUid).apply();
                        upiStatus.setText("Cashfree confirmed "+paymentStatus+" without payment. No Diamonds credited; new recharge is available.");
                    }else upiStatus.setText("Payment not yet verified; NO Diamonds credited. Check again later.");
                }
            }).addOnFailureListener(error->{
                cashfreeVerifying=false;
                if(sameAccount()&&!isFinishing())upiStatus.setText("Payment verification unavailable. No Diamonds credited.");
            });
    }

    private boolean sameAccount(){
        FirebaseUser now=FirebaseAuth.getInstance().getCurrentUser();
        return signedInUid!=null&&now!=null&&signedInUid.equals(now.getUid());
    }

    private void connectBilling(){
        if(!LIVE_RECHARGE||!sameAccount())return;
        billingClient=BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases(PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts().build())
            .build();
        billingClient.startConnection(new BillingClientStateListener(){
            @Override public void onBillingSetupFinished(BillingResult result){
                if(isFinishing()||isDestroyed())return;
                if(result.getResponseCode()!=BillingClient.BillingResponseCode.OK){
                    status.setText("Google Play Billing unavailable. No charge made.");return;
                }
                billReady=true;loadProducts();recoverPurchases();
            }
            @Override public void onBillingServiceDisconnected(){
                billReady=false;
                if(status!=null)status.setText("Google Play disconnected. Reopen Recharge.");
            }
        });
    }

    private void loadProducts(){
        if(!billReady||billingClient==null)return;
        List<QueryProductDetailsParams.Product> entries=new ArrayList<>();
        for(String id:PRODUCT_IDS)
            entries.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id).setProductType(BillingClient.ProductType.INAPP).build());
        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(entries).build(),
            (result,details)->{
                if(isFinishing()||isDestroyed()||!sameAccount())return;
                if(result.getResponseCode()!=BillingClient.BillingResponseCode.OK){
                    status.setText("Google Play products unavailable; nothing was charged.");
                    return;
                }
                products.clear();
                for(ProductDetails product:details.getProductDetailsList()){
                    products.put(product.getProductId(),product);
                    TextView row=productButtons.get(product.getProductId());
                    ProductDetails.OneTimePurchaseOfferDetails offer=product.getOneTimePurchaseOfferDetails();
                    if(row!=null&&offer!=null)
                        row.setText("💎 "+diamondsFor(product.getProductId())+
                            " Diamonds  •  "+offer.getFormattedPrice());
                }
            });
    }

    private int diamondsFor(String id){
        for(int i=0;i<PRODUCT_IDS.length;i++)
            if(PRODUCT_IDS[i].equals(id))return AMOUNTS[i];
        return 0;
    }

    private void startPurchase(String sku){
        if(!LIVE_RECHARGE){
            Toast.makeText(this,"Recharge coming after Google Play verified setup. No payment made.",Toast.LENGTH_LONG).show();
            return;
        }
        if(!sameAccount()||!billReady||billingClient==null){
            Toast.makeText(this,"Sign in and connect to Google Play first",Toast.LENGTH_SHORT).show();
            return;
        }
        ProductDetails product=products.get(sku);
        if(product==null){
            Toast.makeText(this,"This Recharge pack is not available in Google Play",Toast.LENGTH_LONG).show();
            return;
        }
        BillingFlowParams.ProductDetailsParams detail=
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(product).build();
        BillingFlowParams params=BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(Collections.singletonList(detail))
            .setObfuscatedAccountId(uidSha256(signedInUid))
            .build();
        BillingResult launch=billingClient.launchBillingFlow(this,params);
        if(launch.getResponseCode()!=BillingClient.BillingResponseCode.OK)
            status.setText("Google Play payment was not started: "+launch.getDebugMessage());
    }

    private static String uidSha256(String uid){
        try{
            byte[] digest=MessageDigest.getInstance("SHA-256")
                .digest(uid.getBytes(StandardCharsets.UTF_8));
            StringBuilder result=new StringBuilder();
            for(byte b:digest)result.append(String.format(java.util.Locale.US,"%02x",b&0xff));
            return result.toString();
        }catch(Exception fail){throw new IllegalStateException("Cannot bind purchase to user",fail);}
    }

    @Override public void onPurchasesUpdated(BillingResult result,List<Purchase> purchases){
        if(!LIVE_RECHARGE||!sameAccount())return;
        if(result.getResponseCode()==BillingClient.BillingResponseCode.USER_CANCELED){
            status.setText("Payment cancelled • no Diamonds credited");return;
        }
        if(result.getResponseCode()!=BillingClient.BillingResponseCode.OK||purchases==null){
            status.setText("Payment was not completed. No Diamonds credited.");return;
        }
        for(Purchase purchase:purchases)processPurchase(purchase);
    }

    private void recoverPurchases(){
        if(!LIVE_RECHARGE||!billReady||billingClient==null||!sameAccount())return;
        billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP).build(),
            (result,purchases)->{
                if(result.getResponseCode()!=BillingClient.BillingResponseCode.OK||
                   purchases==null||!sameAccount())return;
                for(Purchase purchase:purchases)processPurchase(purchase);
            });
    }

    private void processPurchase(Purchase purchase){
        if(!LIVE_RECHARGE||!sameAccount()||verifying)return;
        if(purchase.getPurchaseState()==Purchase.PurchaseState.PENDING){
            status.setText("Google Play payment pending. Diamonds will be credited only after approval.");
            return;
        }
        if(purchase.getPurchaseState()!=Purchase.PurchaseState.PURCHASED)return;
        String sku=null;
        for(String id:purchase.getProducts())if(diamondsFor(id)>0){sku=id;break;}
        if(sku==null)return;
        verifying=true;
        status.setText("Verifying Google Play receipt on secure server…");
        Map<String,Object> payload=new HashMap<>();
        payload.put("productId",sku);
        payload.put("purchaseToken",purchase.getPurchaseToken());
        FirebaseFunctions.getInstance().getHttpsCallable("verifyPlayPurchase").call(payload)
            .addOnSuccessListener(result->{
                verifying=false;
                if(isFinishing()||isDestroyed()||!sameAccount())return;
                status.setText("Verified Recharge complete. Wallet updates automatically.");
                // No local wallet mutation, TEST coins, VIP or client-side consume.
                // The server handles receipt verification, idempotence, credit and consume.
            })
            .addOnFailureListener(error->{
                verifying=false;
                if(isFinishing()||isDestroyed()||!sameAccount())return;
                status.setText("Recharge NOT credited. Verification unavailable: "+
                    (error.getLocalizedMessage()==null?"contact support":error.getLocalizedMessage())+
                    ". Google Play receipt can be retried safely.");
            });
    }

    @Override protected void onResume(){
        super.onResume();
        if(LIVE_RECHARGE&&billReady)recoverPurchases();
        if(signedInUid!=null&&!sameAccount()){finish();return;}
        if(upiStatus!=null&&cashfreePendingOrder!=null&&!cashfreeVerifying)confirmUpiOrder();
    }
    @Override protected void onDestroy(){
        if(walletRegistration!=null){walletRegistration.remove();walletRegistration=null;}
        if(billingClient!=null)billingClient.endConnection();
        super.onDestroy();
    }
}
