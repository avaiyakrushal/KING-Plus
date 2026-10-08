from pathlib import Path
import re, sys

root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'

def req(path, old, new, label):
    p=pkg/path
    s=p.read_text()
    if old not in s:
        raise SystemExit(f'{path}: missing marker: {label}')
    p.write_text(s.replace(old,new,1))

def opt(path, old, new):
    p=pkg/path
    s=p.read_text()
    if old in s:
        p.write_text(s.replace(old,new,1))
        return True
    return False

# Shared retry/error state for network-backed flows.
(pkg/'KingUiState.java').write_text(r'''package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;

public final class KingUiState {
    private KingUiState(){}
    public static void error(Activity a,String title,String detail,Runnable retry){
        if(a==null||a.isFinishing()||a.isDestroyed())return;
        String body=(detail==null||detail.trim().isEmpty())?"Please check your connection and try again.":detail;
        AlertDialog.Builder b=new AlertDialog.Builder(a).setTitle(title).setMessage(body).setNegativeButton("Close",null);
        if(retry!=null)b.setPositiveButton("Retry",(d,w)->KingSafe.run(a,"retry:"+title,retry));
        b.show();
    }
}
''')

# Keep large room effects from piling up on low-memory phones.
(pkg/'KingEffectBudget.java').write_text(r'''package com.kingplus.social;

import android.app.ActivityManager;
import android.content.Context;
import java.util.concurrent.ConcurrentHashMap;

public final class KingEffectBudget {
    private KingEffectBudget(){}
    private static final ConcurrentHashMap<String,Long> last=new ConcurrentHashMap<>();
    public static boolean allow(Context c,String kind){
        boolean low=false;
        try{
            ActivityManager am=(ActivityManager)c.getSystemService(Context.ACTIVITY_SERVICE);
            low=am!=null&&am.isLowRamDevice();
        }catch(Throwable ignored){}
        long gap;
        if("gift".equals(kind))gap=low?700L:180L;
        else if("emoji".equals(kind))gap=low?420L:90L;
        else gap=low?500L:120L;
        long now=android.os.SystemClock.elapsedRealtime();
        Long prev=last.put(kind,now);
        return prev==null||now-prev>=gap;
    }
}
''')

# Party: explicit microphone permission before enabling room audio.
party=pkg/'PartyActivity.java'
q=party.read_text()
pat=r'(private void toggleMic\(\)\s*\{)'
if not re.search(pat,q):
    raise SystemExit('PartyActivity: toggleMic method not found')
q=re.sub(pat, r'''\1
        if(!micOn && android.os.Build.VERSION.SDK_INT>=23 &&
           checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)!=android.content.pm.PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO},942);
            toast("Allow microphone permission, then tap Mic again");
            return;
        }''', q, count=1)

# Party: when network returns while inside a room, rebind realtime listeners/heartbeat/voice state.
old='''        partyNetworkCallback940=KingNetwork.watch(this,online->runOnUiThread(()->{
            boolean recovered=online&&!partyLastOnline940;partyLastOnline940=online;
            if(recovered&&roomId==null&&!isFinishing()&&!isDestroyed())renderLobby(currentLobbyTab940);
        }));'''
new='''        partyNetworkCallback940=KingNetwork.watch(this,online->runOnUiThread(()->{
            boolean recovered=online&&!partyLastOnline940;partyLastOnline940=online;
            if(!recovered||isFinishing()||isDestroyed())return;
            if(roomId==null){renderLobby(currentLobbyTab940);return;}
            toast("Back online • reconnecting Party room");
            clearListeners();
            renderParty();
            startHeartbeat900();
            setInlineAudioMuted940(!(micOn&&mySeat>0));
        }));'''
if old not in q:
    raise SystemExit('PartyActivity: network recovery marker missing')
q=q.replace(old,new,1)

# KTV/PK network failures become retryable states instead of dead-end toasts.
q=q.replace('''.addOnFailureListener(e->toast("KTV queue unavailable: "+msg(e)))''',
            '''.addOnFailureListener(e->KingUiState.error(this,"KTV queue unavailable",msg(e),this::ktvQueuePanel))''')
q=q.replace('''.addOnFailureListener(e->toast("KTV state unavailable: "+msg(e)))''',
            '''.addOnFailureListener(e->KingUiState.error(this,"KTV state unavailable",msg(e),this::ktvQueuePanel))''')
q=q.replace('''.addOnFailureListener(e->toast("PK scores unavailable: "+msg(e)))''',
            '''.addOnFailureListener(e->KingUiState.error(this,"PK scores unavailable",msg(e),this::audioPkPanel700))''')
q=q.replace('''.addOnFailureListener(e->toast("PK state unavailable: "+msg(e)))''',
            '''.addOnFailureListener(e->KingUiState.error(this,"PK state unavailable",msg(e),this::audioPkPanel700))''')

# Bound gift/emoji animation pressure.
for name,kind in [('showGiftEffect','gift'),('showLiveEmojiEffect560','emoji')]:
    method_pat=rf'((?:private|public|protected)\s+void\s+{name}\s*\([^)]*\)\s*\{{)'
    if re.search(method_pat,q):
        q=re.sub(method_pat, rf'''\1
        if(!KingEffectBudget.allow(this,"{kind}"))return;''', q, count=1)
    else:
        raise SystemExit(f'PartyActivity: {name} method not found')

party.write_text(q)

# Room game history/realtime failures should offer retry.
rg=pkg/'RoomGameActivity.java'
q=rg.read_text()
q=q.replace('''.addOnFailureListener(e->toast("Game history unavailable: "+e.getMessage()));''',
            '''.addOnFailureListener(e->KingUiState.error(this,"Game history unavailable",e.getMessage(),this::showGameHistory940));''')
rg.write_text(q)

# Discover: keep clear loading/offline/retry states even after subsequent edits.
discover=pkg/'DiscoverActivity.java'
q=discover.read_text()
if 'Discover unavailable • tap to retry' not in q:
    old='''}).addOnFailureListener(e->{grid.removeAllViews();grid.addView(emptyText("Profile discovery unavailable"),new LinearLayout.LayoutParams(-1,dp(90)));});'''
    new='''}).addOnFailureListener(e->{grid.removeAllViews();TextView retry=emptyText("Discover unavailable • tap to retry");retry.setOnClickListener(v->loadProfileGrid940());grid.addView(retry,new LinearLayout.LayoutParams(-1,dp(90)));});'''
    if old in q:q=q.replace(old,new,1)
discover.write_text(q)

# Prevent keyboard/dialog clipping on major interactive screens.
for name in [
    'MainActivity.java','PartyActivity.java','ChatActivity.java','InboxActivity.java',
    'DiscoverActivity.java','CommunityHubActivity.java','KingPublicProfileActivity.java',
    'KingVipVisualActivity.java','RoomGameActivity.java','KingMultiVideoActivity.java'
]:
    p=pkg/name
    if not p.exists(): continue
    s=p.read_text()
    if 'SOFT_INPUT_ADJUST_RESIZE' in s: continue
    m=re.search(r'(super\.onCreate\([^;]+;\s*)',s)
    if m:
        insert=m.group(1)+'''try{getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);}catch(Throwable ignored){}'''
        s=s[:m.start()]+insert+s[m.end():]
        p.write_text(s)

# Version bump.
gradle=root/'app/build.gradle'
g=gradle.read_text()
old="versionCode 140; versionName '9.4.1-stability'"
if old not in g:
    raise SystemExit('app/build.gradle: v9.4.1 marker missing')
gradle.write_text(g.replace(old,"versionCode 141; versionName '9.4.2-parity-next'",1))

(root/'V9.4.2-WORKLOG.md').write_text('''# KING Plus v9.4.2 parity/runtime batch

Implemented in this batch:
- explicit microphone permission gate before Party mic activation
- active Party room auto-rebind after network recovery
- low-memory gift/live-emoji animation pressure guard
- retryable KTV/PK/game-history failure states
- Discover retry state retention
- adjustResize keyboard/inset protection on major interactive screens
- no change to no-billing / TEST OTP behavior

Existing v9.4/v9.4.1 work retained:
- synchronized Party games/Ludo routing
- inline Party voice and mic seat queue
- gifts/live emoji realtime dedupe
- KTV/PK synchronized room state
- social/profile/VIP/family flows and original KING visuals
- navigation crash guards
''')
print('KING Plus v9.4.2 consolidated parity/runtime patch applied')
