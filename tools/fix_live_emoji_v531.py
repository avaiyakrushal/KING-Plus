from pathlib import Path
import re

party = Path('app/src/main/java/com/kingplus/social/PartyActivity.java')
s = party.read_text(encoding='utf-8')

# v5.3 used a small 88dp LinearLayout inside the scroll body.  That looked like
# an ordinary reaction row, not a room-wide live emoji.  v5.3.1 moves the same
# realtime event stream to a transparent full-screen overlay above the room.
old_stage = '''        liveEmojiStageV530 = new LinearLayout(this); liveEmojiStageV530.setGravity(Gravity.CENTER);\n        liveEmojiStageV530.setClipChildren(false); liveEmojiStageV530.setClipToPadding(false);\n        LinearLayout.LayoutParams liveStageLpV530 = new LinearLayout.LayoutParams(-1,dp(88));\n        liveStageLpV530.setMargins(0,dp(2),0,dp(2)); page.addView(liveEmojiStageV530,liveStageLpV530);\n'''
if old_stage in s:
    s = s.replace(old_stage, '        // v5.3.1: live emoji uses a full-screen overlay attached to the room shell.\n', 1)

s = s.replace('    private LinearLayout liveEmojiStageV530;\n', '    private FrameLayout liveEmojiStageV530;\n', 1)

overlay_anchor = '        setSafeContentView(shell);\n'
overlay_code = '''        setSafeContentView(shell);\n        liveEmojiStageV530 = new FrameLayout(this);\n        liveEmojiStageV530.setClipChildren(false); liveEmojiStageV530.setClipToPadding(false);\n        liveEmojiStageV530.setClickable(false); liveEmojiStageV530.setFocusable(false);\n        FrameLayout.LayoutParams liveOverlayLpV531 = new FrameLayout.LayoutParams(-1,-1);\n        liveOverlayLpV531.bottomMargin = dp(64);\n        shell.addView(liveEmojiStageV530, liveOverlayLpV531);\n'''
if 'liveOverlayLpV531' not in s:
    if overlay_anchor not in s:
        raise SystemExit('v5.3.1: room shell anchor not found')
    s = s.replace(overlay_anchor, overlay_code, 1)

pattern = re.compile(r'''    private void showLiveEmojiEffectV530\(String emojiV530, String senderV530\) \{.*?\n    \}\n\n    private void showEmptySeatMenuV530''', re.S)
replacement = r'''    private void showLiveEmojiEffectV530(String emojiV530, String senderV530) {
        if (emojiV530 == null || emojiV530.trim().isEmpty()) return;
        if (liveEmojiStageV530 == null) { addFeed((senderV530==null?"User":senderV530)+"  "+emojiV530); return; }
        final String senderSafeV531 = senderV530==null?"User":senderV530;
        liveEmojiStageV530.post(() -> {
            if (liveEmojiStageV530 == null) return;
            final int widthV531 = Math.max(liveEmojiStageV530.getWidth(), dp(320));
            final int heightV531 = Math.max(liveEmojiStageV530.getHeight(), dp(480));

            TextView heroV531 = tv(emojiV530,74,Color.WHITE,false);
            heroV531.setGravity(Gravity.CENTER); heroV531.setAlpha(0f); heroV531.setScaleX(.35f); heroV531.setScaleY(.35f);
            FrameLayout.LayoutParams heroLpV531 = new FrameLayout.LayoutParams(dp(118),dp(118));
            heroLpV531.gravity = Gravity.CENTER; liveEmojiStageV530.addView(heroV531,heroLpV531);
            heroV531.animate().alpha(1f).scaleX(1.28f).scaleY(1.28f).rotationBy(8f).setDuration(320)
                .withEndAction(() -> heroV531.animate().alpha(0f).scaleX(.82f).scaleY(.82f).translationY(-dp(170)).rotationBy(-16f)
                    .setStartDelay(520).setDuration(900).withEndAction(() -> { if(liveEmojiStageV530!=null) liveEmojiStageV530.removeView(heroV531); }).start()).start();

            for (int iV531=0;iV531<6;iV531++) {
                final int indexV531=iV531;
                final int seedV531=(emojiV530+senderSafeV531+System.nanoTime()+iV531).hashCode() & 0x7fffffff;
                final int sizeV531=dp(iV531==0?66:52);
                TextView fxV531=tv(emojiV530,iV531==0?48:38,Color.WHITE,false); fxV531.setGravity(Gravity.CENTER);
                fxV531.setAlpha(0f); fxV531.setScaleX(.45f); fxV531.setScaleY(.45f);
                FrameLayout.LayoutParams fxLpV531=new FrameLayout.LayoutParams(sizeV531,sizeV531);
                fxLpV531.gravity=Gravity.BOTTOM|Gravity.START;
                fxLpV531.leftMargin=dp(8)+(seedV531 % Math.max(dp(20),widthV531-sizeV531-dp(16)));
                fxLpV531.bottomMargin=dp(74)+(indexV531*dp(7));
                liveEmojiStageV530.addView(fxV531,fxLpV531);
                float driftV531=((seedV531 % 121)-60);
                float riseV531=(heightV531*.52f)+dp(indexV531*20);
                fxV531.animate().alpha(1f).scaleX(1.18f).scaleY(1.18f).translationX(driftV531).translationY(-riseV531)
                    .rotationBy((seedV531%2==0?1:-1)*(10+(seedV531%22))).setStartDelay(indexV531*85L).setDuration(1450L+(indexV531*110L))
                    .withEndAction(() -> fxV531.animate().alpha(0f).scaleX(.72f).scaleY(.72f).setDuration(280)
                        .withEndAction(() -> { if(liveEmojiStageV530!=null) liveEmojiStageV530.removeView(fxV531); }).start()).start();
            }
        });
    }

    private void showEmptySeatMenuV530'''
s, count = pattern.subn(replacement, s, count=1)
if count != 1 and 'final String senderSafeV531' not in s:
    raise SystemExit('v5.3.1: live emoji animation method not found')

# Give slower networks enough time for the serverTimestamp event to arrive.
s = s.replace('if(ageV530>5500)continue;', 'if(ageV530>12000)continue;', 1)

# Store the emoji explicitly as well as the legacy text field, while keeping the
# existing event schema compatible with deployed Firestore rules.
s = s.replace('dV530.put("type","live_emoji"); dV530.put("text",emojiV530); dV530.put("createdAt",FieldValue.serverTimestamp());',
              'dV530.put("type","live_emoji"); dV530.put("text",emojiV530); dV530.put("emoji",emojiV530); dV530.put("createdAt",FieldValue.serverTimestamp());', 1)
s = s.replace('String actorV530=str(docV530,"actorName","User"); String emojiV530=str(docV530,"text","😊");',
              'String actorV530=str(docV530,"actorName","User"); String emojiV530=str(docV530,"emoji",str(docV530,"text","😊"));', 1)

# Build-time assertion: the APK must contain both sender and receiver wiring.
required = ['this::showLiveEmojiPanelV530','private void sendLiveEmojiV530(','"live_emoji"','liveEmojiListenerV530=','liveOverlayLpV531','senderSafeV531']
missing = [x for x in required if x not in s]
if missing:
    raise SystemExit('v5.3.1: live emoji wiring incomplete: '+', '.join(missing))

party.write_text(s, encoding='utf-8')
print('v5.3.1 full-screen realtime live emoji applied')
