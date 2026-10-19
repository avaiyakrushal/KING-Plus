package com.kingplus.social;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import org.json.JSONObject;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** Local-only audio WebRTC endpoint. Cloudflare secrets remain in the broker. */
final class KingCloudflareVoice10066 implements KingRoomVoice10066 {
    private static final String ORIGIN="https://king-voice.invalid/";
    private final Activity activity;
    private final FrameLayout parent;
    private final String room, uid, visit;
    private final Listener listener;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private WebView web;
    private boolean closed, ready, micAllowed;
    private int seat=-1;
    private String handle;
    private final Runnable timeout=()->{report(false,"Voice connection timed out • tap to retry");close();};
    KingCloudflareVoice10066(Activity activity, FrameLayout parent, String room,
            String uid, String visit, Listener listener) {
        this.activity=activity;this.parent=parent;this.room=room;this.uid=uid;this.visit=visit;this.listener=listener;
    }
    private boolean active(){
        FirebaseUser current=FirebaseAuth.getInstance().getCurrentUser();
        return !closed&&!activity.isFinishing()&&!activity.isDestroyed()&&current!=null&&uid.equals(current.getUid());
    }
    private void report(boolean connected,String message){
        if(!active())return;
        if(connected)handler.removeCallbacks(timeout);
        listener.onState(connected,message);
    }
    @Override public void start(){
        if(!active()||web!=null)return;
        try {
            web=new WebView(activity);
            web.getSettings().setJavaScriptEnabled(true);
            web.getSettings().setAllowFileAccess(false);
            web.getSettings().setAllowContentAccess(false);
            web.getSettings().setMediaPlaybackRequiresUserGesture(false);
            web.getSettings().setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW);
            web.addJavascriptInterface(new Bridge(),"KingVoice");
            web.setWebViewClient(new WebViewClient(){
                @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){return true;}
                @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){
                    return new WebResourceResponse("text/plain","UTF-8",new ByteArrayInputStream(new byte[0]));
                }
                @Override public void onPageFinished(WebView v,String url){
                    if(!active()||!ORIGIN.equals(url))return;
                    ready=true;evaluate("KingRtc.start()");microphone(micAllowed,seat);
                }
            });
            web.setWebChromeClient(new WebChromeClient(){
                @Override public void onPermissionRequest(PermissionRequest request){
                    handler.post(()->{
                        String[] resources=request.getResources();
                        if(active()&&micAllowed&&ORIGIN.equals(request.getOrigin().toString()+
                            (request.getOrigin().toString().endsWith("/")?"":"/"))&&resources.length==1&&
                            PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resources[0])&&
                            activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED)
                            request.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});
                        else request.deny();
                    });
                }
            });
            String script;
            try(java.io.InputStream input=activity.getAssets().open("king_cloudflare_voice_10066.js")){
                java.io.ByteArrayOutputStream buffer=new java.io.ByteArrayOutputStream();
                byte[] bytes=new byte[4096];int n;
                while((n=input.read(bytes))!=-1)buffer.write(bytes,0,n);
                script=buffer.toString("UTF-8");
            }
            parent.addView(web,new FrameLayout.LayoutParams(2,2));
            web.loadDataWithBaseURL(ORIGIN,"<!doctype html><meta name=viewport content='width=device-width'>"+
                "<meta http-equiv=Content-Security-Policy content=\"default-src 'none'; script-src 'unsafe-inline'; media-src blob:\">"+
                "<body><script>"+script+"</script>","text/html","UTF-8",null);
            handler.postDelayed(timeout,45000);
        }catch(Exception error){report(false,"Cloudflare voice unavailable • tap to retry");close();}
    }
    private void evaluate(String script){if(web!=null&&!closed)web.evaluateJavascript(script,null);}
    @Override public void microphone(boolean allowed,int seat){
        micAllowed=allowed;this.seat=seat;
        if(ready&&active())evaluate("KingRtc.microphone("+allowed+","+seat+")");
    }
    @Override public void close(){
        if(closed)return;
        if(active()&&handle!=null){
            Map<String,Object> data=new HashMap<>();data.put("action","leave");data.put("handle",handle);
            data.put("roomId",room);data.put("visit",visit);
            FirebaseFunctions.getInstance().getHttpsCallable("partyVoice").call(data);
        }
        // JS closes media locally before the view is destroyed. The broker also validates every operation.
        if(web!=null)web.evaluateJavascript("KingRtc.close()",null);
        closed=true;micAllowed=false;handler.removeCallbacksAndMessages(null);
        if(web!=null){web.removeJavascriptInterface("KingVoice");parent.removeView(web);web.destroy();web=null;}
    }
    private final class Bridge {
        @JavascriptInterface public void state(boolean connected,String message){
            handler.post(()->report(connected,message==null?"Voice unavailable":message.substring(0,Math.min(160,message.length()))));
        }
        @JavascriptInterface public void call(String id,String raw){handler.post(()->{
            if(!active())return;
            try {
                JSONObject payload=new JSONObject(raw);
                Map<String,Object> data=new HashMap<>();
                java.util.Iterator<String> keys=payload.keys();
                while(keys.hasNext()){
                    String key=keys.next();Object value=payload.get(key);
                    if(value instanceof JSONObject){Map<String,Object> map=new HashMap<>();JSONObject object=(JSONObject)value;
                        java.util.Iterator<String> nested=object.keys();while(nested.hasNext()){String k=nested.next();map.put(k,object.get(k));}value=map;}
                    if(value instanceof org.json.JSONArray){java.util.List<Object> list=new java.util.ArrayList<>();org.json.JSONArray array=(org.json.JSONArray)value;
                        for(int i=0;i<array.length();i++)list.add(array.get(i));value=list;}
                    data.put(key,value);
                }
                data.put("roomId",room);data.put("visit",visit);
                if("publish".equals(data.get("action"))&&!micAllowed){reply(id,null,"Microphone consent was revoked.");return;}
                FirebaseFunctions.getInstance().getHttpsCallable("partyVoice").call(data)
                    .addOnSuccessListener(result->{
                        if("join".equals(data.get("action"))&&result.getData() instanceof Map){
                            Object value=((Map<?,?>)result.getData()).get("handle");
                            if(value instanceof String)handle=(String)value;
                            handler.removeCallbacks(timeout);
                        }
                        if(active())reply(id,result.getData(),null);
                        else if("join".equals(data.get("action"))&&handle!=null){
                            FirebaseUser current=FirebaseAuth.getInstance().getCurrentUser();
                            if(current!=null&&uid.equals(current.getUid())){
                                Map<String,Object> cleanup=new HashMap<>();cleanup.put("action","leave");cleanup.put("handle",handle);
                                cleanup.put("roomId",room);cleanup.put("visit",visit);
                                FirebaseFunctions.getInstance().getHttpsCallable("partyVoice").call(cleanup);
                            }
                        }
                    })
                    .addOnFailureListener(error->{if(active())reply(id,null,"Cloudflare voice setup or connection is unavailable.");});
            }catch(Exception error){reply(id,null,"Voice request is invalid.");}
        });}
    }
    private void reply(String id,Object result,String error){
        if(!active())return;
        String json="null";
        if(result instanceof Map)json=new JSONObject((Map<?,?>)result).toString();
        evaluate("KingRtc.reply("+JSONObject.quote(id)+","+json+","+(error==null?"null":JSONObject.quote(error))+")");
    }
}
