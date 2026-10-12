#!/usr/bin/env python3
"""Turn KING Reaction game into a real GO-timed contest; fix old-round crashes."""
from pathlib import Path
import sys,re
root=Path(sys.argv[1]);src=root/'app/src/main/java/com/kingplus/social'
p=src/'RoomGameActivity.java';s=p.read_text()
old='''    private void reactionControls863(){final long shown=android.os.SystemClock.elapsedRealtime();Button b=button("⚡ TAP NOW");b.setTextSize(20);b.setOnClickListener(v->{long ms=Math.max(1,android.os.SystemClock.elapsedRealtime()-shown);int score=(int)Math.max(1,100000-Math.min(99999,ms));submit("Reaction "+ms+" ms",score);});controls.addView(b,new LinearLayout.LayoutParams(-1,dp(58)));}'''
new=r'''    // Never count a reaction until a delayed GO. The phase survives
    // Firebase snapshots that recreate controls for the same live round.
    private final android.os.Handler reactionClock9927=new android.os.Handler(android.os.Looper.getMainLooper());
    private final java.security.SecureRandom reactionRng9927=new java.security.SecureRandom();
    private long reactionRound9927=-1L,reactionGreenAt9927=0L;
    private int reactionPhase9927=0; // ready, waiting, GO, finished
    private Button reactionButton9927;
    private TextView reactionText9927;
    private final Runnable reactionGo9927=new Runnable(){
        @Override public void run(){
            if(isFinishing()||isDestroyed()||!"reaction".equals(type)
                ||!"active".equals(status)||roundId!=reactionRound9927
                ||reactionPhase9927!=1)return;
            reactionGreenAt9927=android.os.SystemClock.elapsedRealtime();
            reactionPhase9927=2;
            if(reactionButton9927!=null){
                reactionButton9927.setText("🟢 GO! TAP NOW");
                reactionButton9927.setBackground(bg(0xff087e4e,16));
            }
            if(reactionText9927!=null)reactionText9927.setText("🟢 GO! Tap once. Fastest real reaction wins.");
        }
    };
    private void reactionControls863(){
        if(reactionRound9927!=roundId){
            reactionClock9927.removeCallbacks(reactionGo9927);
            reactionRound9927=roundId;reactionGreenAt9927=0L;reactionPhase9927=0;
        }
        reactionText9927=tv("⚡ Wait for GREEN, then tap quickly!\nEarly taps count as FALSE START.",14,GOLD,true);
        reactionText9927.setGravity(Gravity.CENTER);
        controls.addView(reactionText9927,new LinearLayout.LayoutParams(-1,dp(67)));
        reactionButton9927=button("▶ START REACTION");reactionButton9927.setTextSize(18);
        reactionButton9927.setOnClickListener(v->{
            if(!gameMemberVerified992||!"reaction".equals(type)||!"active".equals(status)
                ||reactionRound9927!=roundId)return;
            long now=android.os.SystemClock.elapsedRealtime();
            if(reactionPhase9927==0){
                reactionPhase9927=1;reactionText9927.setText("🟣 WAIT… do not tap yet!");
                reactionButton9927.setText("✋ WAIT FOR GREEN");
                reactionButton9927.setBackground(bg(0xff964080,16));
                reactionClock9927.removeCallbacks(reactionGo9927);
                reactionClock9927.postDelayed(reactionGo9927,1500+reactionRng9927.nextInt(2501));
            }else if(reactionPhase9927==1){
                reactionPhase9927=3;reactionClock9927.removeCallbacks(reactionGo9927);
                reactionText9927.setText("❌ FALSE START • tapped before GO");
                reactionButton9927.setEnabled(false);
                submit("False start before GO",KingReactionRules9927.FALSE_START);
            }else if(reactionPhase9927==2){
                reactionPhase9927=3;reactionClock9927.removeCallbacks(reactionGo9927);
                reactionButton9927.setEnabled(false);
                long elapsed=Math.max(1,now-reactionGreenAt9927);
                if(elapsed>KingReactionRules9927.MAX_VALID_MS){
                    reactionText9927.setText("⏱ Too late • time limit exceeded");
                    submit("Reaction expired",0);return;
                }
                int score=KingReactionRules9927.score(elapsed);
                reactionText9927.setText("⚡ "+elapsed+" ms • submitted to Party players");
                submit("Reaction "+elapsed+" ms",score);
            }
        });
        controls.addView(reactionButton9927,new LinearLayout.LayoutParams(-1,dp(64)));
        if(reactionPhase9927==1){
            reactionButton9927.setText("✋ WAIT FOR GREEN");
            reactionText9927.setText("🟣 WAIT… do not tap yet!");
        }else if(reactionPhase9927==2){
            reactionButton9927.setText("🟢 GO! TAP NOW");
            reactionButton9927.setBackground(bg(0xff087e4e,16));
            reactionText9927.setText("🟢 GO! Tap once.");
        }else if(reactionPhase9927==3){
            reactionButton9927.setEnabled(false);
            reactionButton9927.setText("✓ Reaction submitted for this round");
        }
    }'''
if s.count(old)!=1:raise RuntimeError('Exact v9.9.26 Reaction method not found')
s=s.replace(old,new)
original='        sheepClock9925.removeCallbacks(sheepTick9925);'
if s.count(original)<1:raise RuntimeError('Missing lifecycle clean up')
s=s.replace(original,original+'\n        reactionClock9927.removeCallbacks(reactionGo9927);',1)
old_round='if(current.exists()&&("active".equals(current.getString("status"))||current.getLong("roundId")!=previous))throw new IllegalStateException("Round changed. Reopen the controls.");'
if s.count(old_round)!=1:raise RuntimeError('Missing stale old game state guard')
s=s.replace(old_round,'''if(current.exists()){
                    Long storedRound9927=current.getLong("roundId");
                    if("active".equals(current.getString("status"))||storedRound9927==null||storedRound9927.longValue()!=previous)
                        throw new IllegalStateException("Round changed. Reopen the controls.");
                }''')
old_submit='active.getLong("roundId")!=expected||!expectedType.equals(active.getString("type"))'
if s.count(old_submit)!=1:raise RuntimeError('Missing submit round guard')
s=s.replace(old_submit,'active.getLong("roundId")==null||active.getLong("roundId").longValue()!=expected||!expectedType.equals(active.getString("type"))')
p.write_text(s)
(src/'KingReactionRules9927.java').write_text(Path('tools/v9927/KingReactionRules9927.java').read_text())
g=root/'app/build.gradle';b=g.read_text()
b,count=re.subn(r"versionCode\s+205;\s*versionName\s+'9\.9\.26[^']*'",
     "versionCode 206; versionName '9.9.27-real-reaction-party-test'",b)
if count!=1:raise RuntimeError('Expected verified v9.9.26 source versionCode 205')
g.write_text(b)
party=(src/'PartyActivity.java').read_text()
for token in ['openLiveChess9917','openLiveKnife9923','approveOrMoveMemberSeat9913','muteOwnedSeat9920','addRoyalHero9924(page)']:
    if token not in party:raise RuntimeError('Previous feature lost: '+token)
assert 'videoMatchIncoming9922' in (src/'KingMultiVideoActivity.java').read_text()
assert 'KingSheepRace9925' in s and 'KingReactionRules9927.score' in s
assert 'get(Source.SERVER)' in (src/'NativeMeetBridge.java').read_text()
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
print('PASS actual GO reaction, false start, no malformed-round crash, KING Royal/Party/Video/Chess/Sheep intact')
