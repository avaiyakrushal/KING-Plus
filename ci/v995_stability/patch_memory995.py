#!/usr/bin/env python3
"""KING Plus v9.9.5 — memory-safe animations and Party lifecycle.

Base: fully signed v9.9.4 Android project restored from GitHub build artifact.
Gifts/Emojis, VIP locks, Firebase, audio & direct social features remain intact.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1]);pkg=root/'app/src/main/java/com/kingplus/social'
party=pkg/'PartyActivity.java'
def swap(path,before,after,label):
    s=path.read_text()
    n=s.count(before)
    if n!=1: raise SystemExit(f'FAILED {label}, anchor appears {n} times: {before[:140]!r}')
    path.write_text(s.replace(before,after,1))
    print('PASS',label)

# The previous animations used attached-only timers, so invisibility and
# backgrounding could continue invalidating their canvases. Stop on hidden.
for filename,old_frame,new_frame in [
    ('KingRoomStageView.java','if(isAttachedToWindow())postInvalidateDelayed(60);',
                              'if(isAttachedToWindow()&&isShown()&&getWindowVisibility()==View.VISIBLE)postInvalidateDelayed(110);'),
    ('KingLoginBackdropView.java','if(isAttachedToWindow())postInvalidateDelayed(66);',
                               'if(isAttachedToWindow()&&isShown()&&getWindowVisibility()==View.VISIBLE)postInvalidateDelayed(120);'),
    ('KingEmojiAuraView992.java','if(isAttachedToWindow())postInvalidateDelayed(55);',
                               'if(isAttachedToWindow()&&isShown()&&getWindowVisibility()==View.VISIBLE)postInvalidateDelayed(95);')
]:
    f=pkg/filename
    swap(f,old_frame,new_frame,filename+' no hidden redraw')
    content=f.read_text()
    anchor='    @Override protected void onDraw(Canvas c){'
    assert content.count(anchor)==1,filename
    replacement='''    @Override protected void onWindowVisibilityChanged(int visibility){
        super.onWindowVisibilityChanged(visibility);
        if(visibility==View.VISIBLE)invalidate();
    }
    @Override protected void onAttachedToWindow(){
        super.onAttachedToWindow();
        invalidate();
    }
''' + anchor
    f.write_text(content.replace(anchor,replacement,1))
    print('PASS',filename,'restart-on-visible without timer leaks')

# Gift art already checks visibility, but should avoid drawing while Window is hidden
# even if its cached global visible rectangle remains non-empty for one frame.
f=pkg/'KingGiftArtView.java'
swap(f,
'''if(isAttachedToWindow()&&isShown()&&getWindowVisibility()==VISIBLE
                &&getGlobalVisibleRect(visible)){''',
'''if(isAttachedToWindow()&&isShown()&&getWindowVisibility()==View.VISIBLE
                &&getGlobalVisibleRect(visible)){''',
'gift hero and catalog redraw only when Activity window visible')

s=party.read_text()
# A central, idempotent cleanup makes background and room-leave deterministic:
# detach GiftBurst and KingEntranceView (their onDetached stops ValueAnimators),
# remove transient emoji/reaction views, release queued opacity animations.
anchor='''    private void clearListeners() {'''
assert s.count(anchor)==1
patch='''    private boolean partyVisualForeground995=true;

    /** Clears transient presentation only — never removes membership or seats.
     * Detaching GiftBurstView stops its active ValueAnimator. An entire stage
     * can be removed when Room becomes invisible without affecting Firebase.
     */
    private void clearPartyVisuals995(){
        for(View v995:new ArrayList<>(activeReactions560.values())){
            if(v995==null)continue;
            try{
                v995.animate().cancel();
                v995.clearAnimation();
                if(v995.getParent() instanceof android.view.ViewGroup)
                    ((android.view.ViewGroup)v995.getParent()).removeView(v995);
            }catch(Exception ignored){}
        }
        activeReactions560.clear();
        FrameLayout stage995=liveEmojiStageV530;
        if(stage995!=null){
            try{
                for(int idx995=stage995.getChildCount()-1;idx995>=0;idx995--){
                    View visual995=stage995.getChildAt(idx995);
                    if(visual995==null)continue;
                    visual995.animate().cancel();
                    visual995.clearAnimation();
                    stage995.removeViewAt(idx995);
                }
            }catch(Exception e){
                KingStability.nonFatal(this,"party-visual-trim",e);
            }
        }
        giftFxActive971=0;
        if(reactionBanner!=null){
            reactionBanner.animate().cancel();
            reactionBanner.clearAnimation();
            reactionBanner.setVisibility(View.GONE);
        }
    }

'''
s=s.replace(anchor,patch+anchor,1)
# On backgrounding, clear expensive native/Canvas animated overlays but do not
# touch voice Jitsi/Mic or Firestore listeners; those have existing policy.
needle='''    @Override protected void onStop(){if(inRoomVoiceJoined940)try{org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostPause(this);}catch(Throwable e){KingStability.nonFatal(this,"party-jitsi-pause",e);}super.onStop();}'''
repl='''    @Override protected void onStop(){
        partyVisualForeground995=false;
        clearPartyVisuals995();
        if(inRoomVoiceJoined940)try{
            org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostPause(this);
        }catch(Throwable e){KingStability.nonFatal(this,"party-jitsi-pause",e);}
        super.onStop();
    }'''
assert s.count(needle)==1
s=s.replace(needle,repl,1)
needle='''    @Override protected void onResume(){
        super.onResume();'''
repl='''    @Override protected void onResume(){
        super.onResume();
        partyVisualForeground995=true;'''
assert s.count(needle)==1
s=s.replace(needle,repl,1)
# Reusing helper for room transitions ensures the now detached stage has
# no callback left to animate it even before onDestroy executes.
needle='''        for(View reaction:activeReactions560.values()){reaction.animate().cancel();if(reaction.getParent() instanceof android.view.ViewGroup)((android.view.ViewGroup)reaction.getParent()).removeView(reaction);}activeReactions560.clear();'''
assert s.count(needle)==1
s=s.replace(needle,'''        clearPartyVisuals995();''',1)
# Lower-memory system callbacks should remove *all* overlay animations, not
# just the tracked emoji effect views. Existing Jitsi voice protection remains.
needle='''        for(View effect969:new ArrayList<>(activeReactions560.values())){
            if(effect969==null)continue;
            effect969.animate().cancel();
            if(effect969.getParent() instanceof android.view.ViewGroup)
                ((android.view.ViewGroup)effect969.getParent()).removeView(effect969);
        }
        activeReactions560.clear();'''
assert s.count(needle)==1
s=s.replace(needle,'''        clearPartyVisuals995();''',1)
# Background Firebase events still come through, but must never allocate a
# fullscreen GiftBurst or play audio while user is inside Games or Chrome.
needle='''    private void showGiftEffect(String actor,String target,String gift,String icon,long qty,long value){
        KingGiftSound993.play(this,gift);if(partyUiDead())return;KingSoundFx.gift(this);'''
repl='''    private void showGiftEffect(String actor,String target,String gift,String icon,long qty,long value){
        if(partyUiDead()||!partyVisualForeground995)return;
        KingGiftSound993.play(this,gift);KingSoundFx.gift(this);'''
assert s.count(needle)==1
s=s.replace(needle,repl,1)
needle='''    private void showLiveEmojiEffect560(String emoji,String sender,String uid){
        if(emoji==null||emoji.trim().isEmpty()||isFinishing()||isDestroyed())return;'''
repl='''    private void showLiveEmojiEffect560(String emoji,String sender,String uid){
        if(!partyVisualForeground995||emoji==null||emoji.trim().isEmpty()||isFinishing()||isDestroyed())return;'''
assert s.count(needle)==1
s=s.replace(needle,repl,1)
# Do not allocate entrance overlays due to Firebase listener while backgrounded.
needle='''    private void showEntranceEffect730(String name,int level,int vip,String effect,String frame){if(partyUiDead())return;'''
repl='''    private void showEntranceEffect730(String name,int level,int vip,String effect,String frame){if(partyUiDead()||!partyVisualForeground995)return;'''
assert s.count(needle)==1
s=s.replace(needle,repl,1)
# On leaving Party, existing listeners and presence cleanup are still used.
party.write_text(s)
print('PASS Party lifecycle: foreground-only effects + complete background, trim and Room-leave overlay cleanup')

gradle=root/'app/build.gradle'
swap(gradle,"versionCode 183; versionName '9.9.4-distinct-3d-gifts-self'",
     "versionCode 184; versionName '9.9.5-party-memory-background-stability'",
     'upgrade code 184; existing APK remains upgradeable')
print('DONE: KING Plus v9.9.5 Party visual memory cleanup, with all room social/game logic intact')
