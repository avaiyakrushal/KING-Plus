from pathlib import Path
import sys,re
root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
p=pkg/'PartyActivity.java'
s=p.read_text()

# 1) Room Battle must never fabricate a result outside a real synced Party room.
pattern=r'        if\(db==null\|\|!cloudRoom\)\{int a=.*?return;\}'
new='''        if(db==null||!cloudRoom||roomId==null){new AlertDialog.Builder(this).setTitle("⚔ Room Battle").setMessage("Room Battle uses real live-room gift/activity data. Join or create a live Party room first.").setPositiveButton("Open Party Lobby",(d,w)->renderLobby("Hot")).setNegativeButton("Close",null).show();return;}'''
s,count=re.subn(pattern,new,s,count=1)
if count!=1: raise SystemExit('Party pkBattle fake fallback marker missing')

# 2) Gift bursts: throttle only the expensive full-screen layer, never lose the compact event.
old='''    private void showGiftEffect(String actor,String target,String gift,String icon,long qty,long value){
        if(!KingEffectBudget.allow(this,"gift"))return;if(partyUiDead())return;KingSoundFx.gift(this);'''
new='''    private void showGiftEffect(String actor,String target,String gift,String icon,long qty,long value){
        boolean fullGiftFx946=KingEffectBudget.allow(this,"gift");if(partyUiDead())return;KingSoundFx.gift(this);'''
if old not in s: raise SystemExit('Gift effect budget marker missing')
s=s.replace(old,new,1)

old='''        if(liveEmojiStageV530!=null&&!partyUiDead()){try{GiftBurstView fx=new GiftBurstView(this,ic,title,sub,accent);FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-1,-1);liveEmojiStageV530.addView(fx,lp);long duration=total>=50000?4300:total>=10000?3600:2900;fx.start(()->{try{if(liveEmojiStageV530!=null&&fx.getParent()==liveEmojiStageV530)liveEmojiStageV530.removeView(fx);}catch(Exception ignored){}},duration);}catch(Exception e){android.util.Log.e("KINGPlusParty","Gift burst failed",e);}}'''
new='''        if(fullGiftFx946&&liveEmojiStageV530!=null&&!partyUiDead()){try{GiftBurstView fx=new GiftBurstView(this,ic,title,sub,accent);fx.setContentDescription(a+" sent "+g+" x"+Math.max(1,qty)+" to "+t);FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-1,-1);liveEmojiStageV530.addView(fx,lp);long duration=total>=50000?4300:total>=10000?3600:2900;fx.start(()->{try{if(liveEmojiStageV530!=null&&fx.getParent()==liveEmojiStageV530)liveEmojiStageV530.removeView(fx);}catch(Exception ignored){}},duration);}catch(Exception e){android.util.Log.e("KINGPlusParty","Gift burst failed",e);}}'''
if old not in s: raise SystemExit('Gift full-screen marker missing')
s=s.replace(old,new,1)

old='''        if(reactionBanner!=null&&!partyUiDead()){try{reactionBanner.animate().cancel();reactionBanner.setText(ic+"  "+a+" → "+t+"  "+g+" x"+Math.max(1,qty)+(giftCombo720>1?"  🔥x"+giftCombo720:"")+(total<=0?"  FREE":"  💎"+compactNumber(total)));reactionBanner.setTextSize(16);reactionBanner.setAlpha(1f);reactionBanner.setVisibility(View.VISIBLE);reactionBanner.setBackground(bg(accent,28));reactionBanner.postDelayed(()->{if(reactionBanner!=null&&!partyUiDead())try{reactionBanner.animate().cancel();reactionBanner.animate().alpha(0f).setDuration(500).withEndAction(()->{if(reactionBanner!=null&&!partyUiDead()){reactionBanner.setVisibility(View.GONE);reactionBanner.setAlpha(1f);reactionBanner.setTextSize(42);reactionBanner.setBackground(bg(0x663c1f58,30));}}).start();}catch(Exception ignored){}},2000);}catch(Exception e){android.util.Log.e("KINGPlusParty","Gift banner failed",e);}}'''
new='''        if(reactionBanner!=null&&!partyUiDead()){try{String giftA11y946=a+" sent "+g+" x"+Math.max(1,qty)+" to "+t;reactionBanner.animate().cancel();reactionBanner.setText(ic+"  "+a+" → "+t+"  "+g+" x"+Math.max(1,qty)+(giftCombo720>1?"  🔥x"+giftCombo720:"")+(total<=0?"  FREE":"  💎"+compactNumber(total)));reactionBanner.setContentDescription(giftA11y946);reactionBanner.announceForAccessibility(giftA11y946);reactionBanner.setTextSize(16);reactionBanner.setAlpha(1f);reactionBanner.setVisibility(View.VISIBLE);reactionBanner.setBackground(bg(accent,28));reactionBanner.postDelayed(()->{if(reactionBanner!=null&&!partyUiDead())try{reactionBanner.animate().cancel();reactionBanner.animate().alpha(0f).setDuration(500).withEndAction(()->{if(reactionBanner!=null&&!partyUiDead()){reactionBanner.setVisibility(View.GONE);reactionBanner.setAlpha(1f);reactionBanner.setTextSize(42);reactionBanner.setBackground(bg(0x663c1f58,30));}}).start();}catch(Exception ignored){}},2000);}catch(Exception e){android.util.Log.e("KINGPlusParty","Gift banner failed",e);}}'''
if old not in s: raise SystemExit('Gift banner marker missing')
s=s.replace(old,new,1)

# 3) Live Emoji: keep a lightweight visible/a11y fallback when full animation is rate-limited.
old='''    private void showLiveEmojiEffect560(String emoji,String sender,String uid){
        if(!KingEffectBudget.allow(this,"emoji"))return;
        if(emoji==null||emoji.trim().isEmpty())return;emoji=stickerFallback610(emoji);'''
new='''    private void showLiveEmojiEffect560(String emoji,String sender,String uid){
        boolean fullEmojiFx946=KingEffectBudget.allow(this,"emoji");
        if(emoji==null||emoji.trim().isEmpty())return;emoji=stickerFallback610(emoji);
        final String emojiSender946=sender==null?"User":sender;
        if(reactionBanner!=null&&!partyUiDead()){try{reactionBanner.announceForAccessibility(emojiSender946+" reacted "+emoji);}catch(Throwable ignored){}}
        if(!fullEmojiFx946){showCompactEmoji946(emoji,emojiSender946);return;}'''
if old not in s: raise SystemExit('Emoji budget marker missing')
s=s.replace(old,new,1)

marker='''    private void showEmptySeatMenuV530(int seatNoV530) {'''
helper='''    private void showCompactEmoji946(String emoji,String sender){
        if(reactionBanner==null||partyUiDead())return;
        try{
            reactionBanner.animate().cancel();reactionBanner.setText(emoji+"  "+sender);reactionBanner.setTextSize(18);reactionBanner.setAlpha(1f);reactionBanner.setVisibility(View.VISIBLE);reactionBanner.setBackground(bg(0x88412f5a,24));
            reactionBanner.postDelayed(()->{if(reactionBanner!=null&&!partyUiDead())reactionBanner.animate().alpha(0f).setDuration(220).withEndAction(()->{if(reactionBanner!=null&&!partyUiDead()){reactionBanner.setVisibility(View.GONE);reactionBanner.setAlpha(1f);reactionBanner.setTextSize(42);reactionBanner.setBackground(bg(0x663c1f58,30));}}).start();},850);
        }catch(Throwable e){android.util.Log.e("KINGPlusParty","Compact emoji fallback failed",e);}
    }

'''
if marker not in s: raise SystemExit('Emoji helper insertion marker missing')
s=s.replace(marker,helper+marker,1)

old='''        stage.addView(reaction,lp);reaction.setScaleX(.6f);reaction.setScaleY(.6f);reaction.animate().scaleX(1f).scaleY(1f).setDuration(200).start();'''
new='''        reaction.setContentDescription(emojiSender946+" reaction "+emoji);stage.addView(reaction,lp);reaction.setScaleX(.6f);reaction.setScaleY(.6f);reaction.animate().scaleX(1f).scaleY(1f).setDuration(200).start();'''
if old not in s: raise SystemExit('Emoji reaction add marker missing')
s=s.replace(old,new,1)

# 4) KTV history and start failures get actionable retry instead of dead-end toasts.
s=s.replace('''.addOnFailureListener(e->toast("KTV history unavailable: "+msg(e)));''',
            '''.addOnFailureListener(e->KingUiState.error(this,"KTV history unavailable",msg(e),this::ktvHistory940));''',1)
s=s.replace('''.addOnFailureListener(e->toast("KTV stage failed: "+msg(e)));''',
            '''.addOnFailureListener(e->KingUiState.error(this,"KTV stage failed",msg(e),this::ktvQueuePanel));''',1)

# 5) Room Battle score failure is retryable.
s=s.replace(''').addOnFailureListener(e->toast("Battle score unavailable: "+msg(e)));''',
            ''').addOnFailureListener(e->KingUiState.error(this,"Room Battle unavailable",msg(e),this::pkBattle));''',1)

p.write_text(s)

# Main profile: make failed aggregate counts tap-retry through a compact state.
p=pkg/'MainActivity.java';s=p.read_text()
old='''        }).addOnFailureListener(e->{if(profileFollowingNumber!=null)profileFollowingNumber.setText("—");if(profileFollowersNumber!=null)profileFollowersNumber.setText("—");if(profileFriendsNumber!=null)profileFriendsNumber.setText("—");});'''
new='''        }).addOnFailureListener(e->{if(profileFollowingNumber!=null){profileFollowingNumber.setText("↻");profileFollowingNumber.setContentDescription("Retry profile counts");profileFollowingNumber.setOnClickListener(v->loadRealProfileData());}if(profileFollowersNumber!=null)profileFollowersNumber.setText("—");if(profileFriendsNumber!=null)profileFriendsNumber.setText("—");});'''
if old in s:s=s.replace(old,new,1)
p.write_text(s)

# Version bump.
g=root/'app/build.gradle';x=g.read_text();old="versionCode 144; versionName '9.4.5-social-profile-runtime'"
if old not in x: raise SystemExit('v9.4.5 version marker missing')
g.write_text(x.replace(old,"versionCode 145; versionName '9.4.6-final-polish'",1))

(root/'V9.4.6-WORKLOG.md').write_text('''# KING Plus v9.4.6 final polish batch
- Removed fabricated/random Room Battle results outside real live rooms.
- Gift rate limiting now suppresses only expensive full-screen bursts; compact gift feedback remains visible.
- Gift banners/full-screen effects include accessibility descriptions.
- Live Emoji rate limiting now falls back to a compact visible reaction rather than silently dropping feedback.
- Live Emoji animations include accessibility descriptions/announcements.
- KTV history/start failures and Room Battle failures expose Retry actions.
- Main profile aggregate-count failure exposes a retry affordance.
''')
print('v9.4.6 final polish patch applied')
