package com.kingplus.social;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.InputType;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.google.firebase.functions.FirebaseFunctions;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** VIP cosmetic SHOP: money is never charged here. Actual verified recharge
 * precedes Diamond spending; only server transactions award these items.
 */
public final class KingVipIdentityShopActivity extends Activity {
    private final String[] colors={"gold","royal_blue","rose","emerald","violet","cyan"};
    private LinearLayout column;
    private EditText numberInput;
    private TextView status,previewName,owned,priceNum,priceColor,balance;
    private Button buyNumber,buyColor;
    private String chosenColor="gold",uid="";
    private boolean shopEnabled=false,requesting=false;
    private long numberPrice=-1,colorPrice=-1;
    private final int dark=0xff141126,gold=0xffffd54f;
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable card(){GradientDrawable d=new GradientDrawable();d.setColor(0xff282141);d.setCornerRadius(dp(14));return d;}
    private TextView label(String s,int size,int color,boolean strong){
        TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);
        if(strong)v.setTypeface(null,Typeface.BOLD);
        v.setPadding(dp(12),dp(10),dp(12),dp(10));return v;
    }
    private void row(TextView v){column.addView(v,new LinearLayout.LayoutParams(-1,-2));}
    private Button action(String s){Button b=new Button(this);b.setAllCaps(false);b.setText(s);b.setTextColor(0xff231822);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(gold));column.addView(b,new LinearLayout.LayoutParams(-1,dp(52)));return b;}
    private boolean accountOk(){FirebaseUser u=FirebaseAuth.getInstance().getCurrentUser();return u!=null&&uid.equals(u.getUid())&&!isFinishing()&&!isDestroyed();}
    @Override public void onCreate(Bundle state){super.onCreate(state);
        ScrollView scroll=new ScrollView(this);column=new LinearLayout(this);column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(14),dp(10),dp(14),dp(28));column.setBackgroundColor(dark);scroll.addView(column);setContentView(scroll);
        TextView title=label("‹   👑 VIP Identity Shop",23,Color.WHITE,true);title.setOnClickListener(v->finish());row(title);
        row(label("Unique VIP numbers and colored names. Ownership is permanent and tied to your KING account. Each purchase spends verified server Diamonds only.",13,0xffded5ec,false));
        FirebaseUser signed=FirebaseAuth.getInstance().getCurrentUser();
        if(signed==null||signed.isAnonymous()){row(label("Sign in to your permanent KING account first.",15,gold,true));return;}
        uid=signed.getUid();
        owned=label("My VIP identity: verifying server…",14,gold,true);row(owned);
        balance=label("💎 Wallet: loading…",13,Color.WHITE,true);row(balance);
        row(label("👑 Choose a unique 6-digit VIP Number",17,gold,true));
        row(label("A VIP number is separate from your normal free KING ID. It can only be sold once; no two customers can buy the same VIP number.",12,0xffc3b7d9,false));
        numberInput=new EditText(this);numberInput.setSingleLine(true);numberInput.setHint("e.g. 888888");numberInput.setTextColor(Color.WHITE);numberInput.setHintTextColor(0xffad9fb6);numberInput.setInputType(InputType.TYPE_CLASS_NUMBER);numberInput.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(6)});numberInput.setBackground(card());column.addView(numberInput,new LinearLayout.LayoutParams(-1,dp(54)));
        priceNum=label("VIP number price: checking…",13,0xffdecdf6,true);row(priceNum);
        buyNumber=action("Buy VIP Number");buyNumber.setOnClickListener(v->{String n=numberInput.getText().toString();if(!KingVipIdentityStyle.validNumber(n)){numberInput.setError("Enter 6 digits, not starting with 0");return;}buy("vip_number",n);});
        row(label("🎨 Choose VIP Name Color",17,gold,true));
        previewName=label("👑 KING VIP NAME",22,KingVipIdentityStyle.color(chosenColor),true);previewName.setGravity(Gravity.CENTER);previewName.setBackground(card());row(previewName);
        LinearLayout choices=new LinearLayout(this);choices.setOrientation(LinearLayout.VERTICAL);column.addView(choices);
        for(String color:colors){TextView chip=label("●  "+KingVipIdentityStyle.colorLabel(color),14,KingVipIdentityStyle.color(color),true);chip.setBackground(card());LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(42));cp.setMargins(0,dp(2),0,dp(2));choices.addView(chip,cp);chip.setOnClickListener(v->{chosenColor=color;previewName.setTextColor(KingVipIdentityStyle.color(color));previewName.setText("👑 VIP NAME • "+KingVipIdentityStyle.colorLabel(color));});}
        priceColor=label("VIP name color price: checking…",13,0xffdecdf6,true);row(priceColor);
        buyColor=action("Buy Selected Name Color");buyColor.setOnClickListener(v->buy("name_color",chosenColor));
        status=label("Checking VIP store…",13,0xffdfc9eb,false);row(status);
        row(label("Payments stay OFF until merchant and recharge verification are configured. This screen never collects card, UPI, OTP or bank details. All ownership is server-issued.",12,0xffc8bacc,false));
        refresh();
    }
    private void refresh(){if(!accountOk())return;
        shopEnabled=false;updateButtons();
        FirebaseFirestore db=FirebaseFirestore.getInstance();
        db.collection("king_vip_style_catalog").document("current").get(Source.SERVER).addOnSuccessListener(d->{
            if(!accountOk())return;
            Object a=d.get("vipNumberDiamonds"),b=d.get("nameColorDiamonds");
            numberPrice=a instanceof Number?((Number)a).longValue():-1;
            colorPrice=b instanceof Number?((Number)b).longValue():-1;
            shopEnabled=d.exists()&&Boolean.TRUE.equals(d.getBoolean("enabled"))&&"verified_diamonds".equals(d.getString("paymentMode"))&&numberPrice>0&&colorPrice>0;
            priceNum.setText(numberPrice>0?"VIP Number: "+numberPrice+" 💎 Diamonds":"VIP Number price: awaiting merchant setup");
            priceColor.setText(colorPrice>0?"VIP Name Color: "+colorPrice+" 💎 Diamonds":"VIP Name Color price: awaiting merchant setup");
            status.setText(shopEnabled?"VIP Shop available • server-verified Diamonds only":"VIP Shop setup pending • NO PAYMENTS or deductions");
            updateButtons();
        }).addOnFailureListener(e->{if(accountOk()){status.setText("Shop verification unavailable • purchases disabled");updateButtons();}});
        db.collection("king_vip_identity_grants").document(uid).get(Source.SERVER).addOnSuccessListener(d->{
            if(!accountOk())return;String number=d.getString("vipNumber"),color=d.getString("nameColor");
            owned.setText("My VIP identity: "+(KingVipIdentityStyle.validNumber(number)?"👑 ID "+number:"no premium ID")+"  •  "+KingVipIdentityStyle.colorLabel(color));
        });
        db.collection("wallets").document(uid).get(Source.SERVER).addOnSuccessListener(d->{if(!accountOk())return;Object n=d.get("coins");balance.setText("💎 Verified Wallet: "+(n instanceof Number?((Number)n).longValue():"unavailable"));})
          .addOnFailureListener(e->{if(accountOk())balance.setText("💎 Wallet verification unavailable");});
    }
    private void updateButtons(){if(buyNumber!=null)buyNumber.setEnabled(shopEnabled&&!requesting);if(buyColor!=null)buyColor.setEnabled(shopEnabled&&!requesting);}
    private void buy(String product,String selection){
        if(!accountOk()||!shopEnabled||requesting)return;
        new android.app.AlertDialog.Builder(this).setTitle("Confirm VIP purchase")
            .setMessage(("vip_number".equals(product)?"VIP ID ":"Name color ")+selection+"\nPrice: "+("vip_number".equals(product)?numberPrice:colorPrice)+" verified Diamonds\n\nDeduct only if the server confirms availability and balance?")
            .setNegativeButton("Cancel",null).setPositiveButton("Confirm",(dialog,which)->{
                if(!accountOk())return;
                requesting=true;updateButtons();status.setText("Checking purchase with KING server…");
                Map<String,Object> payload=new HashMap<>();payload.put("product",product);
                payload.put("vip_number".equals(product)?"number":"color",selection);
                payload.put("requestId",UUID.randomUUID().toString().replace("-",""));
                FirebaseFunctions.getInstance().getHttpsCallable("buyVipIdentity").call(payload)
                    .addOnSuccessListener(result->{if(!accountOk())return;requesting=false;status.setText("Server-confirmed VIP purchase successful");refresh();})
                    .addOnFailureListener(error->{if(!accountOk())return;requesting=false;updateButtons();status.setText("Purchase NOT completed: "+String.valueOf(error.getLocalizedMessage()));});
            }).show();
    }
}
