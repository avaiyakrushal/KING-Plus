package com.kingplus.social;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

/** Read-only verified Diamond Wallet from a single externally hosted Worker.
 * Never returns locally invented balance or writes any currency on the phone.
 * Google Cloud Billing / paid Firebase Functions are not used.
 */
public final class KingBackend976 {
    private static final ExecutorService POOL=Executors.newFixedThreadPool(2);
    private KingBackend976(){}

    public static final class Wallet {
        public final long diamonds;
        public final long rechargeTotal;
        public final int vipLevel;
        Wallet(long diamonds,long rechargeTotal,int vipLevel){
            this.diamonds=diamonds;this.rechargeTotal=rechargeTotal;this.vipLevel=vipLevel;
        }
    }
    public interface WalletCallback {
        void completed(Wallet verifiedWallet,String error);
    }

    /** Restrict the user-supplied backend to the actual, intended Cloudflare
     * Worker name. Before public release, pin a single account-specific URL
     * using an owner-controlled BuildConfig or signed remote configuration.
     */
    public static String validatedWorkerUrl(String raw){
        return KingWalletRules976.trustedWorkerUrl(raw);
    }

    public static String workerUrl(Context context){
        SharedPreferences p=context.getSharedPreferences("king_phone_otp_config",Context.MODE_PRIVATE);
        return validatedWorkerUrl(p.getString("backend_url",""));
    }

    public static JSONObject boundedResponse(HttpURLConnection connection)throws Exception {
        int responseCode=connection.getResponseCode();
        if(responseCode>=300&&responseCode<400)
            throw new SecurityException("Unexpected redirect from verified backend");
        InputStream source=responseCode>=400?connection.getErrorStream():connection.getInputStream();
        if(source==null)throw new java.io.IOException("No backend response");
        try(InputStream in=source;ByteArrayOutputStream buffer=new ByteArrayOutputStream()){
            byte[] chunk=new byte[4096];
            int n;
            while((n=in.read(chunk))!=-1){
                if(buffer.size()+n>16384)throw new java.io.IOException("Unexpected large backend response");
                buffer.write(chunk,0,n);
            }
            JSONObject result=new JSONObject(buffer.toString(StandardCharsets.UTF_8.name()));
            if(responseCode>=400 && !result.has("error"))
                result.put("error","Backend denied request (HTTP "+responseCode+")");
            return result;
        }
    }

    public static void fetchWallet(Activity screen,WalletCallback done){
        if(screen==null||done==null)return;
        FirebaseUser me=FirebaseAuth.getInstance().getCurrentUser();
        String base=workerUrl(screen);
        if(me==null){done.completed(null,"Please sign in to check Diamonds.");return;}
        if(base==null){
            done.completed(null,"KING Plus's verified Wallet server is not configured yet. No payment is enabled.");
            return;
        }
        final String expectedUid=me.getUid();
        me.getIdToken(false).addOnSuccessListener((GetTokenResult auth)->{
            if(screen.isFinishing()||screen.isDestroyed())return;
            String jwt=auth.getToken();
            if(jwt==null||jwt.isEmpty()){
                done.completed(null,"Your Firebase session could not be verified.");return;
            }
            POOL.execute(()->{
                Wallet wallet=null;String error=null;
                HttpURLConnection conn=null;
                try{
                    conn=(HttpURLConnection)new URL(base+"/wallet").openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(15000);
                    conn.setInstanceFollowRedirects(false);
                    conn.setRequestProperty("Accept","application/json");
                    conn.setRequestProperty("Authorization","Bearer "+jwt);
                    JSONObject result=boundedResponse(conn);
                    if(!result.optBoolean("ok",false)){
                        error=result.optString("error","Wallet is currently unavailable.");
                    }else{
                        long diamonds=result.optLong("diamonds",-1);
                        long total=result.optLong("rechargeTotal",-1);
                        int vip=result.optInt("vipLevel",-1);
                        if(!KingWalletRules976.validWallet(diamonds,total,vip))
                            error="Verified wallet returned invalid values.";
                        else wallet=new Wallet(diamonds,total,vip);
                    }
                }catch(Exception e){error="Cannot reach the verified Wallet server. Try again later.";}
                finally{if(conn!=null)conn.disconnect();}
                final Wallet outcome=wallet;final String message=error;
                screen.runOnUiThread(()->{
                    FirebaseUser current=FirebaseAuth.getInstance().getCurrentUser();
                    if(!screen.isFinishing()&&!screen.isDestroyed()&&current!=null
                        &&expectedUid.equals(current.getUid())){
                        done.completed(outcome,message);
                    }
                });
            });
        }).addOnFailureListener(error->{
            if(!screen.isFinishing()&&!screen.isDestroyed()){
                done.completed(null,"Unable to verify Firebase session. Sign in again.");
            }
        });
    }
}
