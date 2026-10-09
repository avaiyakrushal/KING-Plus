package com.kingplus.social;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * Recharge UI prepared for official Google Play one-time consumable purchases.
 * LIVE_RECHARGE=false until Play Console products, merchant setup, and verified
 * Firebase backend are deployed. NEVER mint diamonds from a local purchase result.
 */
public final class KingRecharge974Activity extends Activity implements PurchasesUpdatedListener {
    public static final boolean LIVE_RECHARGE = false;
    public static final String[] PRODUCT_IDS = {
        "king_coins_100","king_coins_600","king_coins_1300"
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

    private int dp(int size){return (int)(size*getResources().getDisplayMetrics().density+0.5f);}
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

        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout column=new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(16),dp(16),dp(16),dp(30));
        column.setBackgroundColor(0xff111329);
        scroll.addView(column);
        setContentView(scroll);

        TextView title=item("‹     💎 Diamond Recharge",23,Color.WHITE,true);
        title.setOnClickListener(v->finish());
        column.addView(title);
        TextView subtitle=item("KING Plus • verified Diamonds & VIP progress",13,0xffb6b6cc,false);
        column.addView(subtitle);

        balance=item("💎 Verified Wallet: loading…",20,0xffffdf75,true);
        balance.setBackground(bg(0xff29264e));column.addView(balance);
        vip=item("👑 VIP Recharge Level: loading…",17,0xffffdf75,true);
        column.addView(vip);
        status=item("",13,0xffc8c7d0,false);column.addView(status);
        TextView info=item(
            "Recharge gives Diamonds for Gifts and increases VIP Level. Sending a Gift spends Diamonds but does not increase VIP. Gifts have no cash-out value.",
            13,0xffc8c7d0,false);
        column.addView(info);

        if(signedInUid==null){
            status.setText("Sign in with your own KING Plus account to see your verified balance.");
            return;
        }

        for(int i=0;i<PRODUCT_IDS.length;i++){
            final String sku=PRODUCT_IDS[i];
            TextView row=item("💎 "+AMOUNTS[i]+" Diamonds  •  Google Play price",16,Color.WHITE,true);
            row.setBackground(bg(0xff32295a));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(80));
            lp.setMargins(0,dp(8),0,0);
            column.addView(row,lp);
            row.setOnClickListener(v->startPurchase(sku));
            productButtons.put(sku,row);
        }

        TextView notice=item(
            "This build does not request any payment. Purchase buttons activate only when Google Play products, verified backend, and merchant setup are ready.",
            13,0xffffd666,false);
        notice.setBackground(bg(0xff423219));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.setMargins(0,dp(14),0,0);column.addView(notice,lp);

        walletRegistration=FirebaseFirestore.getInstance().collection("wallets").document(signedInUid)
            .addSnapshotListener((doc,error)->{
                if(isFinishing()||isDestroyed()||!sameAccount())return;
                if(error!=null){status.setText("Verified wallet unavailable. No Diamonds have been changed.");return;}
                long coins=doc==null||doc.getLong("coins")==null?0:Math.max(0,doc.getLong("coins"));
                long points=doc==null||doc.getLong("rechargeDiamondsTotal")==null?
                    0:Math.max(0,doc.getLong("rechargeDiamondsTotal"));
                long level=doc==null||doc.getLong("vipLevel")==null?0:Math.max(0,doc.getLong("vipLevel"));
                balance.setText("💎 Verified Diamonds: "+coins);
                vip.setText("👑 VIP "+level+"  •  Recharge total: "+points);
            });
        if(LIVE_RECHARGE)connectBilling();
        else status.setText("Recharge setup pending. Payments are OFF; no money will be charged.");
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
    }
    @Override protected void onDestroy(){
        if(walletRegistration!=null){walletRegistration.remove();walletRegistration=null;}
        if(billingClient!=null)billingClient.endConnection();
        super.onDestroy();
    }
}
