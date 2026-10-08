from pathlib import Path
import sys

root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'

# Add independently implemented local/offline Ludo.
(pkg/'KingOfflineLudoActivity.java').write_text(Path(__file__).with_name('KingOfflineLudoActivity.java').read_text())

manifest=root/'app/src/main/AndroidManifest.xml'
m=manifest.read_text()
needle='''        <activity android:name=".OnlineLudoActivity" android:exported="false" />
'''
if needle not in m: raise SystemExit('OnlineLudo manifest marker missing')
if 'KingOfflineLudoActivity' not in m:
    m=m.replace(needle,needle+'''        <activity android:name=".KingOfflineLudoActivity" android:exported="false" />
''',1)
manifest.write_text(m)

# Ludo lobby: Offline must be truly offline instead of opening Firebase Online Ludo.
p=pkg/'KingLudoLobbyActivity.java'; s=p.read_text()
old='''row3.addView(mode("🎲","OFFLINE",()->openOnline(2)),lp());'''
new='''row3.addView(mode("🎲","OFFLINE",this::openOffline),lp());'''
if old not in s: raise SystemExit('Ludo offline route marker missing')
s=s.replace(old,new,1)
old='''    private void openParty(){Intent i=new Intent(this,PartyActivity.class);startActivity(i);}
'''
new='''    private void openParty(){Intent i=new Intent(this,PartyActivity.class);startActivity(i);}
    private void openOffline(){Intent i=new Intent(this,KingOfflineLudoActivity.class);startActivity(i);}
'''
if old not in s: raise SystemExit('Ludo openParty marker missing')
s=s.replace(old,new,1);p.write_text(s)

# Room Games: visible sync/loading/live/result states and reconnect feedback.
p=pkg/'RoomGameActivity.java'; s=p.read_text()
old='''    private LinearLayout page,controls,movesBox,readyBox; private TextView stateText,roleText;
'''
new='''    private LinearLayout page,controls,movesBox,readyBox; private TextView stateText,roleText,phaseHint950,connection950; private android.widget.ProgressBar syncSpinner950;
'''
if old not in s: raise SystemExit('RoomGame field marker missing')
s=s.replace(old,new,1)
old='''        roleText=tv("Checking room role…",12,MUTED,false);page.addView(roleText);
'''
new='''        roleText=tv("Checking room role…",12,MUTED,false);page.addView(roleText);
        LinearLayout sync950=new LinearLayout(this);sync950.setGravity(Gravity.CENTER_VERTICAL);sync950.setPadding(dp(4),0,dp(4),0);syncSpinner950=new android.widget.ProgressBar(this);sync950.addView(syncSpinner950,new LinearLayout.LayoutParams(dp(30),dp(30)));connection950=tv("Connecting to Party game state…",12,MUTED,true);sync950.addView(connection950,new LinearLayout.LayoutParams(0,dp(44),1));page.addView(sync950,new LinearLayout.LayoutParams(-1,dp(44)));
        phaseHint950=tv("Loading multiplayer state…",13,0xffe8ddf7,true);phaseHint950.setGravity(Gravity.CENTER);phaseHint950.setBackground(bg(0xff291d3d,14));LinearLayout.LayoutParams ph950=new LinearLayout.LayoutParams(-1,dp(54));ph950.setMargins(0,dp(4),0,dp(4));page.addView(phaseHint950,ph950);
'''
if old not in s: raise SystemExit('RoomGame role marker missing')
s=s.replace(old,new,1)
old='''        build(); if(db==null||me==null||roomId.isEmpty()){stateText.setText("Sign in and open this from a live Party room.");return;} ensureMembership900(); }
'''
new='''        build(); if(db==null||me==null||roomId.isEmpty()){stateText.setText("Sign in and open this from a live Party room.");if(syncSpinner950!=null)syncSpinner950.setVisibility(View.GONE);if(connection950!=null)connection950.setText("Party room connection unavailable");if(phaseHint950!=null)phaseHint950.setText("Open Games from a live Party room to play with room members.");return;} ensureMembership900(); }
'''
if old not in s: raise SystemExit('RoomGame onCreate marker missing')
s=s.replace(old,new,1)
old='''    private void finishRole(){roleText.setText(moderator?"👑 Host / Co-host • start rounds when players are ready":"👤 Player • tap Ready, then join the active round");attach();listenReady750();renderControls();}
'''
new='''    private void finishRole(){roleText.setText(moderator?"👑 Host / Co-host • start rounds when players are ready":"👤 Player • tap Ready, then join the active round");if(syncSpinner950!=null)syncSpinner950.setVisibility(View.GONE);if(connection950!=null){connection950.setText("● Realtime room game connected");connection950.setTextColor(0xff55e2a4);}attach();listenReady750();renderControls();}
'''
if old not in s: raise SystemExit('RoomGame finishRole marker missing')
s=s.replace(old,new,1)
old='''renderState();listenMoves();renderControls();'''
new='''renderState();updatePhase950();listenMoves();renderControls();'''
if old not in s: raise SystemExit('RoomGame render state marker missing')
s=s.replace(old,new,1)
marker='''    private void showGameHistory940(){
'''
helper='''    private void updatePhase950(){
        if(phaseHint950==null)return;
        String game=type==null||type.isEmpty()?"Room game":type.replace('_',' ').toUpperCase(java.util.Locale.US);
        if("active".equals(status))phaseHint950.setText("● LIVE  •  "+game+"\\nMoves sync instantly with Party room players");
        else if("finished".equals(status))phaseHint950.setText("🏁 ROUND FINISHED  •  "+game+"\\nReview the result or start the next round");
        else if("closed".equals(status)||type==null||type.isEmpty())phaseHint950.setText("Choose a room game • players can Ready before the host starts");
        else phaseHint950.setText(game+"  •  "+status.toUpperCase(java.util.Locale.US));
    }

'''
if marker not in s: raise SystemExit('RoomGame history marker missing')
s=s.replace(marker,helper+marker,1);p.write_text(s)

# Online Ludo: clear reconnect/sync state instead of appearing frozen.
p=pkg/'OnlineLudoActivity.java'; s=p.read_text()
old='''    private TextView statusText,codeText,turnText; private EditText codeInput;
'''
new='''    private TextView statusText,codeText,turnText,networkChip950; private android.widget.ProgressBar sync950; private EditText codeInput;
'''
if old not in s: raise SystemExit('OnlineLudo field marker missing')
s=s.replace(old,new,1)
old='''        statusText=tv("Create a room or join with a 6-character code",15,GOLD,true);statusText.setGravity(Gravity.CENTER);statusText.setBackground(bg(CARD,16));LinearLayout.LayoutParams st=new LinearLayout.LayoutParams(-1,-2);st.setMargins(0,dp(6),0,dp(10));page.addView(statusText,st);
'''
new='''        statusText=tv("Create a room or join with a 6-character code",15,GOLD,true);statusText.setGravity(Gravity.CENTER);statusText.setBackground(bg(CARD,16));LinearLayout.LayoutParams st=new LinearLayout.LayoutParams(-1,-2);st.setMargins(0,dp(6),0,dp(6));page.addView(statusText,st);
        LinearLayout net950=new LinearLayout(this);net950.setGravity(Gravity.CENTER_VERTICAL);sync950=new android.widget.ProgressBar(this);sync950.setVisibility(View.GONE);net950.addView(sync950,new LinearLayout.LayoutParams(dp(28),dp(28)));networkChip950=tv("● Ready",11,GREEN,true);net950.addView(networkChip950,new LinearLayout.LayoutParams(0,dp(36),1));page.addView(net950,new LinearLayout.LayoutParams(-1,dp(38)));
'''
if old not in s: raise SystemExit('OnlineLudo status marker missing')
s=s.replace(old,new,1)
marker='''    private void enableTree(View v,boolean enabled){'''
helper='''    private void connection950(String text,int color,boolean loading){if(networkChip950!=null){networkChip950.setText(text);networkChip950.setTextColor(color);}if(sync950!=null)sync950.setVisibility(loading?View.VISIBLE:View.GONE);}

'''
if marker not in s: raise SystemExit('OnlineLudo helper insert marker missing')
s=s.replace(marker,helper+marker,1)
old='''    private void open(String c){serverReady=false;code=c;getPreferences(MODE_PRIVATE).edit().putString("match_"+me.getUid(),c).apply();codeInput.setText(c);setLobbyEnabled(false);codeText.setText("Room  "+c);listen();}
'''
new='''    private void open(String c){serverReady=false;connection950("↻ Connecting to live match…",GOLD,true);code=c;getPreferences(MODE_PRIVATE).edit().putString("match_"+me.getUid(),c).apply();codeInput.setText(c);setLobbyEnabled(false);codeText.setText("Room  "+c);listen();}
'''
if old not in s: raise SystemExit('OnlineLudo open marker missing')
s=s.replace(old,new,1)
old='''    private void listen(){if(listener!=null)listener.remove();listener=ref().addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(d,e)->{if(e!=null){serverReady=false;render();showError("Live match",e);return;}if(d==null||!d.exists()){state=null;status("Room closed",0xffffa6a6);render();setLobbyEnabled(true);return;}serverReady=!d.getMetadata().isFromCache()&&!d.getMetadata().hasPendingWrites();state=d.getData();render();if(serverReady&&state!=null&&"resolve".equals(s(state.get("phase")))&&me!=null&&strList(state.get("players")).contains(me.getUid()))resolveDice();});}
'''
new='''    private void listen(){if(listener!=null)listener.remove();connection950("↻ Syncing room state…",GOLD,true);listener=ref().addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(d,e)->{if(e!=null){serverReady=false;connection950("● Reconnecting • actions paused",0xffffa6a6,true);render();showError("Live match",e);return;}if(d==null||!d.exists()){state=null;connection950("● Room closed",0xffffa6a6,false);status("Room closed",0xffffa6a6);render();setLobbyEnabled(true);return;}serverReady=!d.getMetadata().isFromCache()&&!d.getMetadata().hasPendingWrites();connection950(serverReady?"● Live server synced":"↻ Reconnecting • read only",serverReady?GREEN:GOLD,!serverReady);state=d.getData();render();if(serverReady&&state!=null&&"resolve".equals(s(state.get("phase")))&&me!=null&&strList(state.get("players")).contains(me.getUid()))resolveDice();});}
'''
if old not in s: raise SystemExit('OnlineLudo listen marker missing')
s=s.replace(old,new,1)
old='''status((serverReady?"Online • ":"Reconnecting • Read only • ")+(cap==2?"1 vs 1":"4 Players")+" • "+phase.toUpperCase(Locale.US),GREEN);'''
new='''status((serverReady?"Online • ":"Reconnecting • Read only • ")+(cap==2?"1 vs 1":"4 Players")+" • "+phase.toUpperCase(Locale.US),serverReady?GREEN:GOLD);'''
if old not in s: raise SystemExit('OnlineLudo render status marker missing')
s=s.replace(old,new,1);p.write_text(s)

# Party Gift Shop: faster access to Wall/Rank/Backpack + safe send + polished sheet motion.
p=pkg/'PartyActivity.java'; s=p.read_text()
old='''        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);TextView title=tv("Gift Shop • 64 gifts",19,Color.WHITE,true);top.addView(title,new LinearLayout.LayoutParams(0,dp(42),1));giftBalanceLabel=pill("💎 "+localCoins,0xff302d40,null);top.addView(giftBalanceLabel,new LinearLayout.LayoutParams(dp(110),dp(38)));root.addView(top);
'''
new='''        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);TextView title=tv("Gift Shop • 64 gifts",19,Color.WHITE,true);top.addView(title,new LinearLayout.LayoutParams(0,dp(42),1));giftBalanceLabel=pill("💎 "+localCoins,0xff302d40,null);top.addView(giftBalanceLabel,new LinearLayout.LayoutParams(dp(110),dp(38)));root.addView(top);
        LinearLayout giftQuick950=new LinearLayout(this);giftQuick950.setGravity(Gravity.CENTER);String[] gq950={"🎁 Wall","🏆 Rank","🎒 Backpack"};for(String q950:gq950){TextView b950=pill(q950,0xff2b2938,()->{if(giftShopDialog!=null)giftShopDialog.dismiss();if(q950.contains("Wall"))giftWall620();else if(q950.contains("Rank"))roomRankingDialog();else backpackGiftDialog700();});b950.setTextSize(11);LinearLayout.LayoutParams qp950=new LinearLayout.LayoutParams(0,dp(38),1);qp950.setMargins(dp(2),0,dp(2),dp(4));giftQuick950.addView(b950,qp950);}root.addView(giftQuick950,new LinearLayout.LayoutParams(-1,dp(42)));
'''
if old not in s: raise SystemExit('Gift top marker missing')
s=s.replace(old,new,1)
old='''TextView send=pill("SEND",0xffffd92e,()->{int total=giftCosts[giftSelectedIndex]*giftQuantity;sendGiftQuantity(giftTargetUid,giftTargetName,giftNames[giftSelectedIndex],giftIcons[giftSelectedIndex],giftCosts[giftSelectedIndex],giftQuantity,total);if(giftBalanceLabel!=null)giftBalanceLabel.setText("💎 "+localCoins);});'''
new='''TextView send=pill("SEND",0xffffd92e,()->KingSafe.run(this,"gift-send",()->{int total=giftCosts[giftSelectedIndex]*giftQuantity;if(total>localCoins){toast("Not enough test diamonds for this gift combo");return;}sendGiftQuantity(giftTargetUid,giftTargetName,giftNames[giftSelectedIndex],giftIcons[giftSelectedIndex],giftCosts[giftSelectedIndex],giftQuantity,total);if(giftBalanceLabel!=null)giftBalanceLabel.setText("💎 "+localCoins);}));'''
if old not in s: raise SystemExit('Gift send marker missing')
s=s.replace(old,new,1)
old='''        giftShopDialog.setOnShowListener(v->{android.view.Window w=giftShopDialog.getWindow();if(w!=null){w.setBackgroundDrawableResource(android.R.color.transparent);w.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);w.setGravity(Gravity.BOTTOM);w.setLayout(-1,(int)(getResources().getDisplayMetrics().heightPixels*.68f));}});giftShopDialog.show();
'''
new='''        giftShopDialog.setOnShowListener(v->{android.view.Window w=giftShopDialog.getWindow();if(w!=null){w.setBackgroundDrawableResource(android.R.color.transparent);w.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);w.setGravity(Gravity.BOTTOM);int h950=Math.min(dp(650),Math.max(dp(430),(int)(getResources().getDisplayMetrics().heightPixels*.72f)));w.setLayout(-1,h950);root.setTranslationY(dp(42));root.setAlpha(.88f);root.animate().translationY(0).alpha(1f).setDuration(190).start();}});giftShopDialog.show();
'''
if old not in s: raise SystemExit('Gift show marker missing')
s=s.replace(old,new,1)

# Emoji sheet: more responsive height and subtle entrance; keep existing realtime events and original KING assets.
old='''        dialog.setOnShowListener(v->{android.view.Window w=dialog.getWindow();if(w!=null){w.setBackgroundDrawableResource(android.R.color.transparent);w.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);w.setGravity(Gravity.BOTTOM);w.setLayout(-1,Math.min(dp(430),(int)(getResources().getDisplayMetrics().heightPixels*.48f)));}});
'''
new='''        dialog.setOnShowListener(v->{android.view.Window w=dialog.getWindow();if(w!=null){w.setBackgroundDrawableResource(android.R.color.transparent);w.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);w.setGravity(Gravity.BOTTOM);int h950=Math.min(dp(560),Math.max(dp(360),(int)(getResources().getDisplayMetrics().heightPixels*.58f)));w.setLayout(-1,h950);sheet.setTranslationY(dp(36));sheet.setAlpha(.9f);sheet.animate().translationY(0).alpha(1f).setDuration(170).start();}});
'''
if old not in s: raise SystemExit('Emoji sheet show marker missing')
s=s.replace(old,new,1)
p.write_text(s)

# Version bump.
gradle=root/'app/build.gradle'; g=gradle.read_text()
old="versionCode 140; versionName '9.4.1-stability'"
if old not in g: raise SystemExit('v9.4.1 version marker missing')
gradle.write_text(g.replace(old,"versionCode 141; versionName '9.5.0-parity-batch1'",1))

print('KING Plus v9.5.0 parity batch 1 applied')
