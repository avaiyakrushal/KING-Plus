#!/usr/bin/env python3
from pathlib import Path
import re, sys

root=Path(sys.argv[1]) if len(sys.argv)>1 else Path('/tmp/src')
party=root/'app/src/main/java/com/kingplus/social/PartyActivity.java'
live=root/'app/src/main/java/com/kingplus/social/LiveEmojiView.java'

if not party.exists():
    raise SystemExit('PartyActivity.java not found')

s=party.read_text()

def replace_once(old,new,label):
    global s
    if old not in s:
        raise SystemExit('PATCH_MISSING: '+label)
    s=s.replace(old,new,1)

# 1) Let Android resize for the keyboard, while our root only consumes system bars.
# The previous implementation consumed IME insets AND could be resized by the window,
# which could double-shrink the room and crowd/cut the composer on smaller phones.
pat=r'''    private void setSafeContentView\(View root\) \{.*?\n    \}\n    private void clearListeners\(\) \{'''
new='''    private void setSafeContentView(View root) {\n        try{ getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE); }catch(Exception ignored){}\n        setContentView(root);\n        final int l=root.getPaddingLeft(), t=root.getPaddingTop(), r=root.getPaddingRight(), b=root.getPaddingBottom();\n        ViewCompat.setOnApplyWindowInsetsListener(root,(v,insets)->{\n            Insets bars=insets.getInsets(WindowInsetsCompat.Type.systemBars());\n            v.setPadding(l,t+bars.top,r,b+bars.bottom);\n            return insets;\n        });\n        ViewCompat.requestApplyInsets(root);\n    }\n    private void clearListeners() {'''
s,n=re.subn(pat,new,s,count=1,flags=re.S)
if n!=1: raise SystemExit('PATCH_MISSING: setSafeContentView')

# 2) Reserve enough room for a slightly taller, fixed composer.
replace_once('roomRootLp.bottomMargin = dp(64);','roomRootLp.bottomMargin = dp(74);','room bottom margin')
replace_once('LinearLayout composer = new LinearLayout(this); composer.setGravity(Gravity.CENTER_VERTICAL); composer.setPadding(dp(8),dp(7),dp(8),dp(7)); composer.setBackgroundColor(Color.WHITE);',
             'LinearLayout composer = new LinearLayout(this); composer.setGravity(Gravity.CENTER_VERTICAL); composer.setPadding(dp(6),dp(7),dp(6),dp(9)); composer.setBackgroundColor(Color.WHITE); composer.setMinimumHeight(dp(70));',
             'composer padding')
replace_once('composer.addView(composerBox,new LinearLayout.LayoutParams(0,dp(48),1));',
             'composerBox.setMinWidth(0); composer.addView(composerBox,new LinearLayout.LayoutParams(0,dp(48),1));',
             'composer input width')
replace_once('TextView plus=pill("＋",0x00ffffff,this::toolsPanel); plus.setTextColor(0xff333333); plus.setTextSize(24); composer.addView(plus,new LinearLayout.LayoutParams(dp(38),dp(48)));',
             'TextView plus=pill("＋",0x00ffffff,this::toolsPanel); plus.setTextColor(0xff333333); plus.setTextSize(24); composer.addView(plus,new LinearLayout.LayoutParams(dp(34),dp(48)));',
             'plus width')
replace_once('TextView emoji=pill("😊",0x00ffffff,this::showLiveEmojiPanelV530); emoji.setTextColor(0xff333333); emoji.setTextSize(23); composer.addView(emoji,new LinearLayout.LayoutParams(dp(42),dp(46)));',
             'TextView emoji=pill("😊",0x00ffffff,this::showLiveEmojiPanelV530); emoji.setTextColor(0xff333333); emoji.setTextSize(22); composer.addView(emoji,new LinearLayout.LayoutParams(dp(38),dp(46)));',
             'emoji width')
replace_once('micLabel=pill(micOn?"🎤":"🎙",0x00ffffff,this::toggleMic); micLabel.setTextColor(0xff333333); micLabel.setTextSize(23); composer.addView(micLabel,new LinearLayout.LayoutParams(dp(42),dp(46)));',
             'micLabel=pill(micOn?"🎤":"🎙",0x00ffffff,this::toggleMic); micLabel.setTextColor(0xff333333); micLabel.setTextSize(22); composer.addView(micLabel,new LinearLayout.LayoutParams(dp(38),dp(46)));',
             'mic width')
replace_once('TextView gift=pill("🎁",0x00ffffff,this::giftShopPanel); gift.setTextColor(0xff333333); gift.setTextSize(22); composer.addView(gift,new LinearLayout.LayoutParams(dp(42),dp(46)));',
             'TextView gift=pill("🎁",0x00ffffff,this::giftShopPanel); gift.setTextColor(0xff333333); gift.setTextSize(21); composer.addView(gift,new LinearLayout.LayoutParams(dp(38),dp(46)));',
             'gift width')
replace_once('TextView send=pill("➤",0x00ffffff,()->sendMessage(composerBox)); send.setTextColor(0xff008e6b); send.setTextSize(23); composer.addView(send,new LinearLayout.LayoutParams(dp(38),dp(46)));',
             'TextView send=pill("➤",0x00ffffff,()->sendMessage(composerBox)); send.setTextColor(0xff008e6b); send.setTextSize(24); send.setGravity(Gravity.CENTER); composer.addView(send,new LinearLayout.LayoutParams(dp(40),dp(48)));',
             'send width')
replace_once('FrameLayout.LayoutParams composerLp=new FrameLayout.LayoutParams(-1,dp(64)); composerLp.gravity=Gravity.BOTTOM; shell.addView(composer,composerLp);',
             'FrameLayout.LayoutParams composerLp=new FrameLayout.LayoutParams(-1,dp(74)); composerLp.gravity=Gravity.BOTTOM; shell.addView(composer,composerLp);',
             'composer height')

# 3) Make send resilient: clear/hide keyboard after a successful send, and do not let
# a chat-row rendering exception leave the typed text stuck in the composer.
send_pat=r'''    private void sendMessage\(EditText box\) \{.*?\n    \}\n    private void hideRoomKeyboard\(EditText box\)\{'''
send_new='''    private void sendMessage(EditText box) {\n        if(box==null)return;\n        String text=box.getText()==null?"":box.getText().toString().trim();\n        if(text.isEmpty())return;\n        if(text.length()>500){box.setError("Maximum 500 characters");return;}\n        String outgoing=text;\n        if(!pendingReplyText620.isEmpty())outgoing="↪ "+pendingReplyName620+": "+shortChatPreview620(pendingReplyText620)+"\\n"+text;\n        final String finalOutgoing=outgoing;\n        if(cloudRoom&&user!=null&&db!=null){\n            Map<String,Object>d=new HashMap<>();d.put("senderUid",user.getUid());d.put("senderName",safeName());d.put("text",finalOutgoing);d.put("type","text");d.put("createdAt",FieldValue.serverTimestamp());\n            db.collection("live_rooms").document(roomId).collection("messages").add(d)\n                .addOnSuccessListener(v->{\n                    if(partyUiDead())return;\n                    box.setText("");clearRoomReply620();hideRoomKeyboard(box);\n                })\n                .addOnFailureListener(e->{if(!partyUiDead())toast("Message failed: "+msg(e));});\n        }else{\n            appendLocalChat(displayName,finalOutgoing);\n            try{ addChatRow(displayName,finalOutgoing); }catch(Exception e){ android.util.Log.e("KINGPlusParty","Local chat row render failed",e); }\n            box.setText("");clearRoomReply620();hideRoomKeyboard(box);\n        }\n    }\n    private void hideRoomKeyboard(EditText box){'''
s,n=re.subn(send_pat,send_new,s,count=1,flags=re.S)
if n!=1: raise SystemExit('PATCH_MISSING: sendMessage/hideRoomKeyboard')

# 4) Avoid leaking raw internal IndexOutOfBounds messages to the user.
replace_once('if(!isFinishing()&&!isDestroyed()) toast("Action failed safely • "+msg(e));',
             'if(!isFinishing()&&!isDestroyed()) toast("Could not complete that action • please try again");',
             'safe action toast')

# 5) Use a guarded emoji-only sizing calculation instead of a stream path that can be
# fragile on some vendor EmojiCompat builds with multi-code-unit emoji sequences.
old='TextView msg=tv(text,(text!=null&&text.codePointCount(0,text.length())<=6&&text.codePoints().anyMatch(c->c>0x2000))?34:14,Color.WHITE,false);msg.setPadding(0,0,0,0);'
new='TextView msg=tv(text,safeRoomMessageTextSize861(text),Color.WHITE,false);msg.setPadding(0,0,0,0);'
replace_once(old,new,'chat message size')
anchor='    private void addChatRow(String who,String text){'
helper='''    private int safeRoomMessageTextSize861(String text){\n        if(text==null||text.isEmpty())return 14;\n        try{\n            int count=0;boolean hasSymbol=false;\n            for(int i=0;i<text.length()&&count<=6;){\n                int cp=text.codePointAt(i);\n                if(cp>0x2000)hasSymbol=true;\n                i+=Math.max(1,Character.charCount(cp));count++;\n            }\n            return count<=6&&hasSymbol?34:14;\n        }catch(Exception ignored){return 14;}\n    }\n\n'''
if anchor not in s: raise SystemExit('PATCH_MISSING: addChatRow anchor')
s=s.replace(anchor,helper+anchor,1)

# 6) Keep emoji and gift sheets above the fixed composer and leave more of the room visible.
s=s.replace("heightPixels*.72f","heightPixels*.62f",1)
s=s.replace("heightPixels*.52f","heightPixels*.48f",1)

party.write_text(s)

# 7) Defensive bound for the live emoji icon table.
if live.exists():
    x=live.read_text()
    old='c.drawText(ICONS[expression],0,-(paint.ascent()+paint.descent())/2f,paint);'
    if old in x:
        x=x.replace(old,'String icon=expression>=0&&expression<ICONS.length?ICONS[expression]:"✨";c.drawText(icon,0,-(paint.ascent()+paint.descent())/2f,paint);',1)
    live.write_text(x)

print('v8.6.1 Party UI/chat patch applied')
