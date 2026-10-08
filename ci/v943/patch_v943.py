from pathlib import Path
import sys
root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'

def replace(path,old,new,label):
    p=pkg/path;s=p.read_text()
    if old not in s: raise SystemExit(f'{path}: missing {label}')
    p.write_text(s.replace(old,new,1))

# RoomGame: deterministic reconnect and richer finished-round state.
p=pkg/'RoomGameActivity.java';s=p.read_text()
old='''    private ListenerRegistration stateListener,movesListener,readyListener,moderatorRoleListener; private long privateRoleRound866;'''
new='''    private ListenerRegistration stateListener,movesListener,readyListener,moderatorRoleListener; private android.net.ConnectivityManager.NetworkCallback gameNetworkCallback943; private boolean gameOnline943=true; private long privateRoleRound866;'''
if old not in s: raise SystemExit('RoomGame listener fields')
s=s.replace(old,new,1)

old='''        build(); if(db==null||me==null||roomId.isEmpty()){stateText.setText("Sign in and open this from a live Party room.");return;} ensureMembership900(); }'''
new='''        build(); if(db==null||me==null||roomId.isEmpty()){stateText.setText("Sign in and open this from a live Party room.");return;}
        gameOnline943=KingNetwork.online(this);
        gameNetworkCallback943=KingNetwork.watch(this,online->runOnUiThread(()->{
            boolean recovered=online&&!gameOnline943;gameOnline943=online;
            if(isFinishing()||isDestroyed())return;
            if(!online){roleText.setText("Offline • Room Game will reconnect automatically");stateText.setText("Connection lost • your submitted move stays synced");setGameUiEnabled943(false);return;}
            setGameUiEnabled943(true);
            if(recovered){roleText.setText("Back online • reconnecting multiplayer…");detachRealtime943();ensureMembership900();}
        }));
        ensureMembership900(); }'''
if old not in s: raise SystemExit('RoomGame onCreate tail')
s=s.replace(old,new,1)

old='''    @Override protected void onDestroy(){
        if(me!=null&&db!=null&&!roomId.isEmpty())try{room().collection("game_ready").document(me.getUid()).delete();}catch(Exception ignored){}
        if(stateListener!=null)stateListener.remove();if(movesListener!=null)movesListener.remove();if(readyListener!=null)readyListener.remove();if(moderatorRoleListener!=null)moderatorRoleListener.remove();super.onDestroy();}'''
new='''    @Override protected void onDestroy(){
        KingNetwork.unwatch(this,gameNetworkCallback943);gameNetworkCallback943=null;
        if(me!=null&&db!=null&&!roomId.isEmpty())try{room().collection("game_ready").document(me.getUid()).delete();}catch(Exception ignored){}
        detachRealtime943();super.onDestroy();}
    private void detachRealtime943(){
        if(stateListener!=null){stateListener.remove();stateListener=null;}if(movesListener!=null){movesListener.remove();movesListener=null;}if(readyListener!=null){readyListener.remove();readyListener=null;}if(moderatorRoleListener!=null){moderatorRoleListener.remove();moderatorRoleListener=null;}
    }
    private void setGameUiEnabled943(boolean yes){
        setGameTreeEnabled943(controls,yes);setGameTreeEnabled943(readyBox,yes);setGameTreeEnabled943(movesBox,yes);
    }
    private void setGameTreeEnabled943(View v,boolean yes){if(v==null)return;v.setEnabled(yes);v.setAlpha(yes?1f:.62f);if(v instanceof android.view.ViewGroup){android.view.ViewGroup g=(android.view.ViewGroup)v;for(int i=0;i<g.getChildCount();i++)setGameTreeEnabled943(g.getChildAt(i),yes);}}
'''
if old not in s: raise SystemExit('RoomGame onDestroy')
s=s.replace(old,new,1)

old='''        Map<String,Object>m=new HashMap<>();m.put("uid",me.getUid());m.put("name",displayName.length()>80?displayName.substring(0,80):displayName);m.put("online",true);m.put("appVersion","9.0.0");m.put("lastSeenAt",FieldValue.serverTimestamp());'''
new='''        Map<String,Object>m=new HashMap<>();m.put("uid",me.getUid());m.put("name",displayName.length()>80?displayName.substring(0,80):displayName);m.put("online",true);m.put("appVersion","9.4.3");m.put("lastSeenAt",FieldValue.serverTimestamp());'''
if old not in s: raise SystemExit('RoomGame membership version')
s=s.replace(old,new,1)

old='''    private void renderState(){if(roundId==0||!"active".equals(status)){stateText.setText(result.isEmpty()?"No active multiplayer round":"Last result\n"+result);return;}if("ttt".equals(type)){String turn=tttTurnUid864.equals(tttXUid864)?tttXName864:tttTurnUid864.equals(tttOUid864)?tttOName864:"Player";stateText.setText("❌⭕  "+tttXName864+" vs "+tttOName864+"\nTurn: "+turn+"\n"+tttBoardText864());return;}stateText.setText(gameIcon(type)+"  "+prompt+(result.isEmpty()?"":"\n"+result));}'''
new='''    private void renderState(){if(roundId==0||!"active".equals(status)){if(result.isEmpty()){stateText.setText("🎮 No active multiplayer round\nChoose a game or wait for the host");stateText.setTextColor(MUTED);}else{stateText.setText("🏁 ROUND COMPLETE\n"+result+"\n↻ Host / Co-host can start a rematch");stateText.setTextColor(GOLD);}return;}stateText.setTextColor(GOLD);if("ttt".equals(type)){String turn=tttTurnUid864.equals(tttXUid864)?tttXName864:tttTurnUid864.equals(tttOUid864)?tttOName864:"Player";stateText.setText("❌⭕  "+tttXName864+" vs "+tttOName864+"\nTurn: "+turn+"\n"+tttBoardText864());return;}stateText.setText(gameIcon(type)+"  "+prompt+(result.isEmpty()?"":"\n"+result));}'''
if old not in s: raise SystemExit('RoomGame renderState')
s=s.replace(old,new,1)

old='''            if(roundId>0&&!"active".equals(status)){LinearLayout rr=new LinearLayout(this);Button rem=button("↻ Rematch");rem.setOnClickListener(v->rematch750());rr.addView(rem,new LinearLayout.LayoutParams(0,dp(50),1));Button reset=button("Clear Ready");reset.setOnClickListener(v->clearReady750());LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,dp(50),1);rp.setMargins(dp(6),0,0,0);rr.addView(reset,rp);controls.addView(rr);}
        }
        if(roundId>0&&"active".equals(status)){'''
new='''            if(roundId>0&&!"active".equals(status)){LinearLayout rr=new LinearLayout(this);Button rem=button("↻ Rematch");rem.setOnClickListener(v->rematch750());rr.addView(rem,new LinearLayout.LayoutParams(0,dp(50),1));Button reset=button("Clear Ready");reset.setOnClickListener(v->clearReady750());LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,dp(50),1);rp.setMargins(dp(6),0,0,0);rr.addView(reset,rp);controls.addView(rr);}
        }else if(roundId>0&&!"active".equals(status)){TextView wait=tv("Round complete • waiting for Host / Co-host rematch",13,MUTED,true);wait.setGravity(Gravity.CENTER);controls.addView(wait,new LinearLayout.LayoutParams(-1,dp(48)));Button backParty=button("← Back to Party Room");backParty.setOnClickListener(v->finish());controls.addView(backParty,new LinearLayout.LayoutParams(-1,dp(48)));}
        if(roundId>0&&"active".equals(status)){'''
if old not in s: raise SystemExit('RoomGame inactive controls')
s=s.replace(old,new,1)

# Listener failures become retryable instead of dead ends.
s=s.replace('''if(e!=null){stateText.setText("Game state error: "+e.getMessage());return;}''','''if(e!=null){stateText.setText("Game state disconnected • tap result/history or wait for reconnect");roleText.setText("Realtime game state unavailable • reconnecting…");return;}''',1)
s=s.replace('''if(e!=null){movesBox.removeAllViews();movesBox.addView(tv("Unable to load moves: "+e.getMessage(),13,MUTED,false));return;}''','''if(e!=null){movesBox.removeAllViews();TextView retry=tv("Moves disconnected • tap to reconnect",13,MUTED,true);retry.setOnClickListener(v->{detachRealtime943();ensureMembership900();});movesBox.addView(retry);return;}''',1)
p.write_text(s)

# Online Ludo: foreground auto-reconnect and explicit manual retry/read-only state.
p=pkg/'OnlineLudoActivity.java';s=p.read_text()
old='''    @Override protected void onDestroy(){if(listener!=null)listener.remove();super.onDestroy();}'''
new='''    @Override protected void onResume(){super.onResume();if(me!=null&&!code.isEmpty()&&KingNetwork.online(this)){status("Checking live Ludo connection…",GOLD);listen();}}
    @Override protected void onDestroy(){if(listener!=null)listener.remove();super.onDestroy();}'''
if old not in s: raise SystemExit('Ludo onDestroy marker')
s=s.replace(old,new,1)

old='''if(state==null){codeText.setText(code.isEmpty()?"Room —":"Room  "+code);turnText.setText("Create or join a room to start");return;}'''
new='''if(state==null){codeText.setText(code.isEmpty()?"Room —":"Room  "+code);turnText.setText(code.isEmpty()?"Create or join a room to start":"Room data unavailable • reconnect when online");if(!code.isEmpty()){Button retry=btn("↻ Reconnect Room");retry.setOnClickListener(v->{if(KingNetwork.online(this))listen();else toast("Internet connection required");});actionsBox.addView(retry,new LinearLayout.LayoutParams(-1,dp(50)));}return;}'''
if old not in s: raise SystemExit('Ludo null state')
s=s.replace(old,new,1)

old='''status((serverReady?"Online • ":"Reconnecting • Read only • ")+(cap==2?"1 vs 1":"4 Players")+" • "+phase.toUpperCase(Locale.US),GREEN);'''
new='''status((serverReady?"Online • ":"Reconnecting • Read only • ")+(cap==2?"1 vs 1":"4 Players")+" • "+phase.toUpperCase(Locale.US),serverReady?GREEN:GOLD);'''
if old not in s: raise SystemExit('Ludo reconnect status')
s=s.replace(old,new,1)
p.write_text(s)

# Version bump.
g=root/'app/build.gradle';x=g.read_text();old="versionCode 141; versionName '9.4.2-parity-next'"
if old not in x: raise SystemExit('v9.4.2 version marker missing')
g.write_text(x.replace(old,"versionCode 142; versionName '9.4.3-games-reconnect'",1))

(root/'V9.4.3-WORKLOG.md').write_text('''# KING Plus v9.4.3 games/reconnect batch

- Room multiplayer auto-recovers Firestore membership/listeners after connectivity returns.
- Room game UI becomes read-only/visibly offline when connection drops.
- Finished room rounds now show a clear result/rematch state for hosts and waiting/back-to-party state for players.
- Move stream failures expose a tap-to-reconnect action.
- Online Ludo refreshes its live listener on foreground resume.
- Online Ludo shows a manual Reconnect Room action when a saved room has no current state.
- Ludo reconnect/read-only status uses a distinct warning state.
''')
print('v9.4.3 games/reconnect patch applied')
