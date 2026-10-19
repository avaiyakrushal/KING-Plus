package com.kingplus.social;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import org.jitsi.meet.sdk.BroadcastEvent;

/** One scoped native conference monitor; owns its receiver and join timeout. */
public final class KingPartyVoiceMonitor10065 {
    public interface Callback {void state(boolean connected,String message);}
    private final LocalBroadcastManager broadcasts;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final KingVoiceSession10065 session;
    private boolean closed;
    private final Callback callback;
    private final Runnable timeout=this::onTimeout;
    private void onTimeout(){if(!closed&&!session.connected())callback.state(false,"Voice not connected • tap Voice connection");}
    private final BroadcastReceiver receiver=new BroadcastReceiver(){
        @Override public void onReceive(Context context,Intent intent){
            if(closed||intent==null)return;
            try{
                BroadcastEvent event=new BroadcastEvent(intent);
                Object value=event.getData().get("url");String url=value instanceof String?(String)value:null;
                if(event.getType()==BroadcastEvent.Type.CONFERENCE_JOINED&&session.joined(url)){
                    handler.removeCallbacks(timeout);callback.state(true,"Voice connected");
                }else if(event.getType()==BroadcastEvent.Type.CONFERENCE_TERMINATED&&session.terminated(url)){
                    handler.removeCallbacks(timeout);callback.state(false,"Voice disconnected • tap to retry");
                }
            }catch(RuntimeException ignored){/* Malformed/foreign native broadcast is not media proof. */}
        }
    };
    public KingPartyVoiceMonitor10065(Context context,String url,Callback callback){
        this.callback=callback;session=new KingVoiceSession10065(url);
        broadcasts=LocalBroadcastManager.getInstance(context.getApplicationContext());
        IntentFilter filter=new IntentFilter();
        filter.addAction(BroadcastEvent.Type.CONFERENCE_JOINED.getAction());
        filter.addAction(BroadcastEvent.Type.CONFERENCE_TERMINATED.getAction());
        broadcasts.registerReceiver(receiver,filter);handler.postDelayed(timeout,45000L);
    }
    public void close(){if(closed)return;closed=true;handler.removeCallbacks(timeout);broadcasts.unregisterReceiver(receiver);session.close();}
}
