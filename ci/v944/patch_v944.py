from pathlib import Path
import sys,re
root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'

# Party: visible offline/reconnect banner instead of silent network loss.
p=pkg/'PartyActivity.java';s=p.read_text()
field='''    private boolean partyLastOnline940;'''
if field not in s: raise SystemExit('Party network field marker missing')
s=s.replace(field,field+'''\n    private TextView partyConnectionBanner944;''',1)

head='''        roomRoot.addView(head,new LinearLayout.LayoutParams(-1,dp(56)));'''
if head not in s: raise SystemExit('Party room header marker missing')
s=s.replace(head,head+'''
        partyConnectionBanner944=tv("Offline • Party room will reconnect automatically",11,Color.WHITE,true);
        partyConnectionBanner944.setGravity(Gravity.CENTER);
        partyConnectionBanner944.setBackground(bg(0xff8a3b45,10));
        partyConnectionBanner944.setVisibility(partyLastOnline940?View.GONE:View.VISIBLE);
        roomRoot.addView(partyConnectionBanner944,new LinearLayout.LayoutParams(-1,partyLastOnline940?0:dp(30)));
''',1)

old='''        partyNetworkCallback940=KingNetwork.watch(this,online->runOnUiThread(()->{
            boolean recovered=online&&!partyLastOnline940;partyLastOnline940=online;
            if(!recovered||isFinishing()||isDestroyed())return;
            if(roomId==null){renderLobby(currentLobbyTab940);return;}
            toast("Back online • reconnecting Party room");
            clearListeners();
            renderParty();
            startHeartbeat900();
            setInlineAudioMuted940(!(micOn&&mySeat>0));
        }));'''
new='''        partyNetworkCallback940=KingNetwork.watch(this,online->runOnUiThread(()->{
            boolean recovered=online&&!partyLastOnline940;partyLastOnline940=online;
            if(isFinishing()||isDestroyed())return;
            updatePartyConnection944(online);
            if(!recovered)return;
            if(roomId==null){renderLobby(currentLobbyTab940);return;}
            toast("Back online • reconnecting Party room");
            clearListeners();
            renderParty();
            startHeartbeat900();
            setInlineAudioMuted940(!(micOn&&mySeat>0));
        }));'''
if old not in s: raise SystemExit('Party v9.4.2 network callback marker missing')
s=s.replace(old,new,1)

marker='''    private void renderLobby(String selected) {'''
helper='''    private void updatePartyConnection944(boolean online){
        if(partyConnectionBanner944==null)return;
        partyConnectionBanner944.setText(online?"Back online • syncing Party room":"Offline • Party room will reconnect automatically");
        partyConnectionBanner944.setBackground(bg(online?0xff286a52:0xff8a3b45,10));
        partyConnectionBanner944.setVisibility(online?View.GONE:View.VISIBLE);
        android.view.ViewGroup.LayoutParams lp=partyConnectionBanner944.getLayoutParams();
        if(lp!=null){lp.height=online?0:dp(30);partyConnectionBanner944.setLayoutParams(lp);}
    }

'''
if marker not in s: raise SystemExit('Party renderLobby helper marker missing')
s=s.replace(marker,helper+marker,1)
p.write_text(s)

# Multi Video: Firestore seat/control listener reconnect and offline read-only state.
p=pkg/'KingMultiVideoActivity.java';s=p.read_text()
field='''    private int mySeat=-1; private boolean micOn=false,cameraOn=false,moderator=false,owner940=false,cohost940=false,leaving940=false;'''
newfield='''    private int mySeat=-1; private boolean micOn=false,cameraOn=false,moderator=false,owner940=false,cohost940=false,leaving940=false;
    private android.net.ConnectivityManager.NetworkCallback videoNetworkCallback944; private boolean videoOnline944=true;'''
if field not in s: raise SystemExit('MultiVideo state field marker missing')
s=s.replace(field,newfield,1)

old='''        setContentView(shell);attachRealtime940();renderSeats940();refreshControls940();'''
new='''        setContentView(shell);
        videoOnline944=KingNetwork.online(this);
        videoNetworkCallback944=KingNetwork.watch(this,online->runOnUiThread(()->{
            boolean recovered=online&&!videoOnline944;videoOnline944=online;
            if(isFinishing()||isDestroyed())return;
            setVideoOnline944(online);
            if(recovered){detachRealtime944();attachRealtime940();toast("Multi Video reconnected");}
        }));
        attachRealtime940();renderSeats940();refreshControls940();setVideoOnline944(videoOnline944);'''
if old not in s: raise SystemExit('MultiVideo onCreate tail missing')
s=s.replace(old,new,1)

old='''    private void attachRealtime940(){
        if(db==null||me==null||roomId.isEmpty()){stateText.setText("📹 Multi Video • sign in required for seats");return;}'''
new='''    private void attachRealtime940(){
        detachRealtime944();
        if(db==null||me==null||roomId.isEmpty()){stateText.setText("📹 Multi Video • sign in required for seats");return;}'''
if old not in s: raise SystemExit('MultiVideo attach marker missing')
s=s.replace(old,new,1)

marker='''    private void reduceEvents940(QuerySnapshot snap){'''
helper='''    private void detachRealtime944(){
        try{if(eventsListener!=null)eventsListener.remove();}catch(Throwable ignored){}eventsListener=null;
        try{if(roomListener!=null)roomListener.remove();}catch(Throwable ignored){}roomListener=null;
        try{if(roleListener!=null)roleListener.remove();}catch(Throwable ignored){}roleListener=null;
    }
    private void setVideoOnline944(boolean online){
        if(stateText!=null){
            if(!online)stateText.setText("Offline • Multi Video seats will reconnect automatically");
            else refreshControls940();
        }
        if(controls!=null){controls.setAlpha(online?1f:.55f);controls.setEnabled(online);}
        if(seatsBar!=null){seatsBar.setAlpha(online?1f:.70f);seatsBar.setEnabled(online);}
    }

'''
if marker not in s: raise SystemExit('MultiVideo reducer marker missing')
s=s.replace(marker,helper+marker,1)

old='''    @Override protected void onDestroy(){heartbeat940.removeCallbacks(heartbeatTask940);if(eventsListener!=null)eventsListener.remove();if(roomListener!=null)roomListener.remove();if(roleListener!=null)roleListener.remove();try{if(meetView!=null){meetView.dispose();meetView=null;}}catch(Throwable ignored){}org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostDestroy(this);super.onDestroy();}'''
new='''    @Override protected void onDestroy(){KingNetwork.unwatch(this,videoNetworkCallback944);videoNetworkCallback944=null;heartbeat940.removeCallbacks(heartbeatTask940);detachRealtime944();try{if(meetView!=null){meetView.dispose();meetView=null;}}catch(Throwable ignored){}org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostDestroy(this);super.onDestroy();}'''
if old not in s: raise SystemExit('MultiVideo onDestroy marker missing')
s=s.replace(old,new,1)
p.write_text(s)

# Version bump.
g=root/'app/build.gradle';x=g.read_text();old="versionCode 142; versionName '9.4.3-games-reconnect'"
if old not in x: raise SystemExit('v9.4.3 version marker missing')
g.write_text(x.replace(old,"versionCode 143; versionName '9.4.4-party-video-social'",1))

(root/'V9.4.4-WORKLOG.md').write_text('''# KING Plus v9.4.4 Party / Multi Video runtime states

- Party Room now shows a visible offline banner instead of silently waiting.
- Party Room banner disappears after connectivity returns and normal realtime rebind runs.
- Multi Video Firestore room/role/event listeners are detached before re-attach to avoid duplicate callbacks.
- Multi Video becomes visibly read-only while offline.
- Multi Video re-attaches realtime seat/control listeners after network recovery.
- Multi Video network watcher is cleaned up on destroy.
''')
print('v9.4.4 Party/Multi Video patch applied')
