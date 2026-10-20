package com.kingplus.social;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class VoiceWebActivity extends Activity {
    private static final int MEDIA_PERMISSION = 501;
    private WebView webView; private boolean videoMode; private String meetingUrl="";
    private TextView retry, consent9926; private String room9926="";
    private boolean approved9926=false,checking9926=false;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private ConnectivityManager.NetworkCallback networkCallback; private int retryAttempt=0; private boolean pageReady=false;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        String roomId=getIntent().getStringExtra("roomId");
        room9926=roomId==null?"":roomId.trim();
        String displayName=getIntent().getStringExtra("displayName");if(displayName==null||displayName.trim().isEmpty())displayName="KING User";
        videoMode=getIntent().getBooleanExtra("video",false);
        String slug=("KINGPlus-"+room9926).replaceAll("[^A-Za-z0-9_-]","");
        String name=android.net.Uri.encode(displayName);
        meetingUrl="https://meet.jit.si/"+slug+(videoMode?"#config.startWithVideoMuted=true&config.startAudioOnly=false":"#config.startWithVideoMuted=true&config.startAudioOnly=true")+"&config.prejoinPageEnabled=true&config.disableDeepLinking=true&userInfo.displayName=\""+name+"\"";

        FrameLayout frame=new FrameLayout(this);webView=new WebView(this);frame.addView(webView,new FrameLayout.LayoutParams(-1,-1));
        retry=new TextView(this);retry.setTextColor(Color.WHITE);retry.setTextSize(16);retry.setGravity(Gravity.CENTER);retry.setBackgroundColor(0xdd171420);retry.setVisibility(View.GONE);retry.setOnClickListener(v->reloadMeeting(true));frame.addView(retry,new FrameLayout.LayoutParams(-1,-1));
        consent9926=new TextView(this);
        consent9926.setText("KING Plus Voice / Video\n\nTap to verify Party membership & join.\n\nMic and camera start OFF.");
        consent9926.setTextSize(18);
        consent9926.setTextColor(Color.WHITE);
        consent9926.setGravity(Gravity.CENTER);
        consent9926.setPadding(26,26,26,26);
        consent9926.setBackgroundColor(0xff160f2c);
        consent9926.setOnClickListener(v->verifyAndJoin9926());
        frame.addView(consent9926,new FrameLayout.LayoutParams(-1,-1));
        setContentView(frame);
        WebSettings settings=webView.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);settings.setMediaPlaybackRequiresUserGesture(true);settings.setAllowContentAccess(true);settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.setWebViewClient(new WebViewClient(){
            @Override public void onPageStarted(WebView view,String url,android.graphics.Bitmap favicon){pageReady=false;}
            @Override public void onPageFinished(WebView view,String url){pageReady=true;retryAttempt=0;retry.setVisibility(View.GONE);}
            @Override public void onReceivedError(WebView view,WebResourceRequest req,WebResourceError err){if(req!=null&&req.isForMainFrame())showRetry("Connection interrupted");}
        });
        webView.setWebChromeClient(new WebChromeClient(){@Override public void onPermissionRequest(PermissionRequest request){runOnUiThread(()->{if(!hasRequiredPermissions()){requestRuntimePermissions();request.deny();return;}List<String> allowed=new ArrayList<>();for(String resource:request.getResources()){if(PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resource))allowed.add(resource);if(videoMode&&PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(resource))allowed.add(resource);}if(allowed.isEmpty())request.deny();else request.grant(allowed.toArray(new String[0]));});}});
        networkCallback=KingNetwork.watch(this,online->runOnUiThread(()->{if(online){if(approved9926&&!pageReady&&hasRequiredPermissions())reloadMeeting(false);}else if(approved9926)showRetry("No internet connection");}));
        // Do not start public RTC or even request mic/camera on activity creation.
    }

    private void verifyAndJoin9926(){
        if(checking9926||approved9926)return;
        checking9926=true;
        consent9926.setText("Verifying current KING Plus room membership...");
        NativeMeetBridge.verifyMembership(this,room9926,allowed->{
            checking9926=false;
            if(isFinishing()||isDestroyed())return;
            if(!allowed){
                consent9926.setText("Live room membership required.\n\nTap to retry verification.");
                return;
            }
            approved9926=true;
            consent9926.setVisibility(View.GONE);
            if(!hasRequiredPermissions())requestRuntimePermissions();
            else reloadMeeting(true);
        });
    }

    private void showRetry(String reason){if(isFinishing()||!approved9926)return;retry.setText(reason+"\nTap to reconnect");retry.setVisibility(View.VISIBLE);handler.removeCallbacksAndMessages(null);long delay=Math.min(15000L,2000L*(1L<<Math.min(3,retryAttempt++)));handler.postDelayed(()->{if(!isFinishing()&&retry.getVisibility()==View.VISIBLE&&KingNetwork.online(this)&&hasRequiredPermissions())reloadMeeting(false);},delay);}
    private void reloadMeeting(boolean manual){if(!approved9926||webView==null||meetingUrl.isEmpty())return;if(!KingNetwork.online(this)){showRetry("No internet connection");return;}if(!hasRequiredPermissions()){requestRuntimePermissions();return;}if(manual)retryAttempt=0;retry.setText("Reconnecting…");retry.setVisibility(View.VISIBLE);webView.stopLoading();webView.loadUrl(meetingUrl);}
    private boolean hasRequiredPermissions(){if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)return false;return !videoMode||checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED;}
    private void requestRuntimePermissions(){if(videoMode)requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.CAMERA},MEDIA_PERMISSION);else requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},MEDIA_PERMISSION);}
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){super.onRequestPermissionsResult(requestCode,permissions,grantResults);if(requestCode==MEDIA_PERMISSION){if(approved9926&&hasRequiredPermissions())reloadMeeting(true);else Toast.makeText(this,"Microphone/camera permission is required",Toast.LENGTH_LONG).show();}}
    @Override protected void onResume(){super.onResume();if(webView!=null)webView.onResume();if(approved9926&&!pageReady&&KingNetwork.online(this)&&hasRequiredPermissions())handler.postDelayed(()->reloadMeeting(false),700);}
    @Override protected void onPause(){if(webView!=null)webView.onPause();super.onPause();}
    @Override public void onBackPressed(){if(webView!=null&&webView.canGoBack())webView.goBack();else super.onBackPressed();}
    @Override protected void onDestroy(){KingNetwork.unwatch(this,networkCallback);handler.removeCallbacksAndMessages(null);if(webView!=null){webView.loadUrl("about:blank");webView.stopLoading();webView.setWebChromeClient(null);webView.setWebViewClient(null);webView.destroy();webView=null;}super.onDestroy();}
}
