package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.net.Uri;
import android.view.View;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.text.InputType;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Secure mobile OTP bridge for an independently deployed Cloudflare Worker.
 * No Firebase Phone SMS/Blaze. Never contains MSG91/Razorpay/service-account keys.
 */
public final class KingPhoneOtp975Activity extends Activity {
    private final ExecutorService network=Executors.newSingleThreadExecutor();
    private EditText server,siteKey,phone,otp;
    private TextView status;
    private WebView captcha;
    private volatile String turnstileToken975="";
    private volatile boolean sending,verifying;
    private boolean destroyed975;

    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
    private TextView label(String text,int size,int color){
        TextView t=new TextView(this);
        t.setText(text);t.setTextColor(color);t.setTextSize(size);
        t.setPadding(dp(8),dp(8),dp(8),dp(8));return t;
    }
    private EditText field(String hint,int type){
        EditText e=new EditText(this);e.setHint(hint);e.setSingleLine(true);
        e.setTextColor(Color.WHITE);e.setHintTextColor(0xffbfbac8);e.setInputType(type);
        e.setPadding(dp(10),dp(10),dp(10),dp(10));return e;
    }
    private Button button(String text,LinearLayout parent){
        Button b=new Button(this);b.setText(text);b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff624bd7));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(52));
        lp.setMargins(0,dp(8),0,0);parent.addView(b,lp);return b;
    }
    private void message(String m){
        if(!destroyed975&&!isFinishing()&&!isDestroyed())
            runOnUiThread(()->status.setText(m));
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        ScrollView scroll=new ScrollView(this);
        LinearLayout col=new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(20),dp(20),dp(20),dp(34));
        col.setBackgroundColor(0xff10132d);
        scroll.addView(col);
        setContentView(scroll);

        TextView top=label("‹     📱 KING Plus Mobile OTP",22,Color.WHITE);
        top.setOnClickListener(v->finish());
        col.addView(top);
        col.addView(label(
            "Sign in with your real phone number. An approved SMS provider sends a one-time code. No Google Cloud Billing is required for this alternative service.",
            13,0xffced1e2));

        final SharedPreferences config=getSharedPreferences("king_phone_otp_config",MODE_PRIVATE);
        server=field("https://your-worker.workers.dev",InputType.TYPE_CLASS_TEXT|
            InputType.TYPE_TEXT_VARIATION_URI);
        server.setText(config.getString("backend_url",""));
        col.addView(label("Secure backend address (configured by KING Plus owner)",13,Color.WHITE));
        col.addView(server);
        siteKey=field("Cloudflare Turnstile site key",InputType.TYPE_CLASS_TEXT);
        siteKey.setText(config.getString("turnstile_site_key",""));
        col.addView(siteKey);
        phone=field("Mobile Number (+91XXXXXXXXXX)",InputType.TYPE_CLASS_PHONE);
        col.addView(phone);

        Button captchaButton=button("1. Verify you are human",col);
        captcha=new WebView(this);
        captcha.setBackgroundColor(0xfff5f5fc);
        captcha.getSettings().setJavaScriptEnabled(true);
        captcha.getSettings().setAllowFileAccess(false);
        captcha.getSettings().setAllowContentAccess(false);
        captcha.getSettings().setDomStorageEnabled(false);
        captcha.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view,String url){
                return !isTrustedCaptchaUrl975(url);
            }
        });
        captcha.addJavascriptInterface(new Object(){
            @JavascriptInterface public void verified(String token){
                if(token!=null&&token.length()>5&&token.length()<4096){
                    turnstileToken975=token;
                    message("Human verification completed. Now request SMS OTP.");
                }
            }
        },"AndroidOtp");
        LinearLayout.LayoutParams clp=new LinearLayout.LayoutParams(-1,dp(170));
        clp.topMargin=dp(8);col.addView(captcha,clp);
        captchaButton.setOnClickListener(v->loadCaptcha975());

        Button send=button("2. Send real SMS OTP",col);
        otp=field("6-digit SMS OTP",InputType.TYPE_CLASS_NUMBER|
            InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        col.addView(otp);
        Button verify=button("3. Verify OTP & Sign In",col);
        status=label("Configure secure backend first. TEST OTP is not accepted here.",13,0xffffd777);
        col.addView(status);
        col.addView(label("This phone login creates a Firebase UID for the verified mobile. Existing Google accounts are not automatically linked; use the original login to keep an existing profile.",
            12,0xffbbbfce));

        send.setOnClickListener(v->sendOtp975());
        verify.setOnClickListener(v->verifyOtp975());
    }

    private String normalizedServer975(){
        try{
            String raw=server.getText().toString().trim();
            Uri uri=Uri.parse(raw);
            String host=uri.getHost();
            if(!"https".equalsIgnoreCase(uri.getScheme())||host==null||host.length()<5||
                uri.getPort()>0&&uri.getPort()!=443||uri.getUserInfo()!=null||
                uri.getQuery()!=null||uri.getFragment()!=null)return null;
            if(uri.getPath()!=null&&!uri.getPath().isEmpty()&&!"/".equals(uri.getPath()))return null;
            return "https://"+host;
        }catch(Exception error){return null;}
    }
    private boolean isTrustedCaptchaUrl975(String raw){
        try{
            Uri uri=Uri.parse(raw);
            String host=uri.getHost();
            return "https".equalsIgnoreCase(uri.getScheme())&&
                ("challenges.cloudflare.com".equals(host)||
                  normalizedServer975()!=null&&host.equals(Uri.parse(normalizedServer975()).getHost()));
        }catch(Exception ignored){return false;}
    }
    private String normalizedPhone975(){
        String s=phone.getText().toString().replaceAll("[ ()-]","");
        if(s.matches("[6-9][0-9]{9}"))return "+91"+s;
        return s.matches("\\+91[6-9][0-9]{9}")?s:null;
    }
    private void loadCaptcha975(){
        final String backend=normalizedServer975();
        final String key=siteKey.getText().toString().trim();
        if(backend==null||!key.matches("[A-Za-z0-9_-]{12,128}")){
            message("Enter your HTTPS Cloudflare Worker address and Turnstile site key.");
            return;
        }
        getSharedPreferences("king_phone_otp_config",MODE_PRIVATE).edit()
            .putString("backend_url",backend).putString("turnstile_site_key",key).apply();
        turnstileToken975="";
        String html="<!doctype html><html><head>"+
            "<meta name='viewport' content='width=device-width,initial-scale=1'>"+
            "<script src='https://challenges.cloudflare.com/turnstile/v0/api.js' async defer></script>"+
            "</head><body style='background:#f5f5fc;padding:20px'>"+
            "<div class='cf-turnstile' data-sitekey='"+key+
            "' data-callback='passed'></div><script>"+
            "function passed(t){AndroidOtp.verified(t)}</script></body></html>";
        captcha.loadDataWithBaseURL(backend+"/",html,"text/html","UTF-8",null);
        message("Complete the human check above.");
    }
    private JSONObject post975(String base,String route,JSONObject data)throws Exception{
        HttpURLConnection conn=(HttpURLConnection)new URL(base+route).openConnection();
        conn.setConnectTimeout(10000);conn.setReadTimeout(15000);
        conn.setRequestMethod("POST");conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type","application/json");
        byte[] payload=data.toString().getBytes(StandardCharsets.UTF_8);
        try(OutputStream out=conn.getOutputStream()){out.write(payload);}
        InputStream input=conn.getResponseCode()>=400?conn.getErrorStream():conn.getInputStream();
        if(input==null)throw new IllegalStateException("Network unavailable");
        String result;
        try(InputStream in=input){
            byte[] buf=new byte[8192];int length=in.read(buf);
            result=length<0?"{}":new String(buf,0,length,StandardCharsets.UTF_8);
        }finally{conn.disconnect();}
        return new JSONObject(result);
    }
    private void sendOtp975(){
        final String endpoint=normalizedServer975(),mobile=normalizedPhone975();
        final String token=turnstileToken975;
        if(endpoint==null||mobile==null){message("Enter a valid HTTPS backend and Indian phone number.");return;}
        if(token.length()<5){message("Complete Cloudflare human verification first.");return;}
        if(sending)return;
        sending=true;turnstileToken975="";
        message("Requesting real SMS OTP…");
        network.execute(()->{
            try{
                JSONObject req=new JSONObject();req.put("phone",mobile);
                req.put("turnstileToken",token);
                JSONObject reply=post975(endpoint,"/otp/request",req);
                message(reply.optBoolean("ok",false)?"SMS OTP sent. Enter the code.":reply.optString("error","SMS provider unavailable"));
            }catch(Exception error){message("OTP server unavailable. Check connection or provider setup.");}
            finally{sending=false;}
        });
    }
    private void verifyOtp975(){
        final String endpoint=normalizedServer975(),mobile=normalizedPhone975();
        final String code=otp.getText().toString().trim();
        if(endpoint==null||mobile==null||!code.matches("[0-9]{6}")){
            message("Enter your correct mobile number, backend and 6-digit OTP.");return;
        }
        if(verifying)return;
        verifying=true;message("Verifying SMS code securely…");
        network.execute(()->{
            try{
                JSONObject request=new JSONObject();
                request.put("phone",mobile);request.put("otp",code);
                JSONObject reply=post975(endpoint,"/otp/verify",request);
                String signedToken=reply.optString("customToken","");
                if(!reply.optBoolean("ok",false)||signedToken.length()<100){
                    message(reply.optString("error","OTP could not be verified"));return;
                }
                runOnUiThread(()->{
                    if(destroyed975||isFinishing())return;
                    FirebaseAuth.getInstance().signInWithCustomToken(signedToken)
                        .addOnSuccessListener(result->{
                            message("Phone verified. Signed in successfully.");
                            Intent home=new Intent(this,MainActivity.class);
                            home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(home);finish();
                        })
                        .addOnFailureListener(e->{
                            message("Firebase refused the verified login. Check service account setup.");
                            verifying=false;
                        });
                });
            }catch(Exception e){message("OTP verification server unavailable.");}
            finally{verifying=false;}
        });
    }

    @Override protected void onDestroy(){
        destroyed975=true;
        network.shutdownNow();
        if(captcha!=null){
            captcha.removeJavascriptInterface("AndroidOtp");
            captcha.stopLoading();captcha.destroy();
        }
        super.onDestroy();
    }
}
