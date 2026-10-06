from pathlib import Path
import sys
root=Path(sys.argv[1])
party=root/'app/src/main/java/com/kingplus/social/PartyActivity.java'
manifest=root/'app/src/main/AndroidManifest.xml'
s=party.read_text()

old='''        ViewCompat.setOnApplyWindowInsetsListener(root,(v,insets)->{
            Insets bars=insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(l,t+bars.top,r,b+bars.bottom);
            return insets;
        });
'''
new='''        ViewCompat.setOnApplyWindowInsetsListener(root,(v,insets)->{
            Insets bars=insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime=insets.getInsets(WindowInsetsCompat.Type.ime());
            boolean keyboard=insets.isVisible(WindowInsetsCompat.Type.ime());
            int bottom=keyboard?Math.max(bars.bottom,ime.bottom):bars.bottom;
            v.setPadding(l,t+bars.top,r,b+bottom);
            if(keyboard&&roomChatScroll!=null)roomChatScroll.post(()->roomChatScroll.fullScroll(View.FOCUS_DOWN));
            return insets;
        });
'''
if old not in s: raise SystemExit('insets block missing')
s=s.replace(old,new,1)

old='''        composerBox=new EditText(this); composerBox.setHint("Type message..."); composerBox.setTextColor(0xff111111); composerBox.setHintTextColor(0xff777777); composerBox.setSingleLine(true); composerBox.setTextSize(14); composerBox.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEND); composerBox.setOnEditorActionListener((v, actionId, event)->{ if(actionId==android.view.inputmethod.EditorInfo.IME_ACTION_SEND){sendMessage(composerBox);return true;}return false;}); composerBox.setBackground(bg(0xfff3f5f5,20)); composerBox.setPadding(dp(8),0,dp(8),0);
'''
new='''        composerBox=new EditText(this); composerBox.setHint("Type message..."); composerBox.setTextColor(0xff111111); composerBox.setHintTextColor(0xff777777); composerBox.setSingleLine(true); composerBox.setTextSize(14); composerBox.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEND); composerBox.setOnEditorActionListener((v, actionId, event)->{ if(actionId==android.view.inputmethod.EditorInfo.IME_ACTION_SEND){sendMessage(composerBox);return true;}return false;}); composerBox.setBackground(bg(0xfff3f5f5,20)); composerBox.setPadding(dp(10),0,dp(10),0);
        composerBox.setOnFocusChangeListener((v,focused)->{if(focused){ViewCompat.requestApplyInsets(shell);if(roomChatScroll!=null)roomChatScroll.postDelayed(()->roomChatScroll.fullScroll(View.FOCUS_DOWN),120);}});
'''
if old not in s: raise SystemExit('composer block missing')
s=s.replace(old,new,1)

old=r'''    private void sendMessage(EditText box) {
        if(box==null)return;
        String text=box.getText()==null?"":box.getText().toString().trim();
        if(text.isEmpty())return;
        if(text.length()>500){box.setError("Maximum 500 characters");return;}
        String outgoing=text;
        if(!pendingReplyText620.isEmpty())outgoing="↪ "+pendingReplyName620+": "+shortChatPreview620(pendingReplyText620)+"\n"+text;
        final String finalOutgoing=outgoing;
        if(cloudRoom&&user!=null&&db!=null){
            Map<String,Object>d=new HashMap<>();d.put("senderUid",user.getUid());d.put("senderName",safeName());d.put("text",finalOutgoing);d.put("type","text");d.put("createdAt",FieldValue.serverTimestamp());
            db.collection("live_rooms").document(roomId).collection("messages").add(d)
                .addOnSuccessListener(v->{
                    if(partyUiDead())return;
                    box.setText("");clearRoomReply620();hideRoomKeyboard(box);
                })
                .addOnFailureListener(e->{if(!partyUiDead())toast("Message failed: "+msg(e));});
        }else{
            appendLocalChat(displayName,finalOutgoing);
            try{ addChatRow(displayName,finalOutgoing); }catch(Exception e){ android.util.Log.e("KINGPlusParty","Local chat row render failed",e); }
            box.setText("");clearRoomReply620();hideRoomKeyboard(box);
        }
    }
'''
new=r'''    private void sendMessage(EditText box) {
        if(box==null)return;
        String text=box.getText()==null?"":box.getText().toString().trim();
        if(text.isEmpty())return;
        if(text.length()>500){box.setError("Maximum 500 characters");return;}
        String outgoing=text;
        if(!pendingReplyText620.isEmpty())outgoing="↪ "+pendingReplyName620+": "+shortChatPreview620(pendingReplyText620)+"\n"+text;
        final String finalOutgoing=outgoing;
        final String originalText=text;
        final boolean keepKeyboard=box.hasFocus();
        // Clear immediately so a successfully sent message never remains in the composer.
        box.setText("");
        clearRoomReply620();
        if(keepKeyboard){box.requestFocus();ViewCompat.requestApplyInsets(box);}
        if(cloudRoom&&user!=null&&db!=null){
            Map<String,Object>d=new HashMap<>();d.put("senderUid",user.getUid());d.put("senderName",safeName());d.put("text",finalOutgoing);d.put("type","text");d.put("createdAt",FieldValue.serverTimestamp());
            db.collection("live_rooms").document(roomId).collection("messages").add(d)
                .addOnSuccessListener(v->{
                    if(partyUiDead())return;
                    if(keepKeyboard){box.requestFocus();keepRoomKeyboard881(box);}else hideRoomKeyboard(box);
                    if(roomChatScroll!=null)roomChatScroll.post(()->roomChatScroll.fullScroll(View.FOCUS_DOWN));
                })
                .addOnFailureListener(e->{
                    if(partyUiDead())return;
                    if(box.getText()==null||box.getText().length()==0){box.setText(originalText);box.setSelection(box.length());}
                    box.requestFocus();keepRoomKeyboard881(box);toast("Message failed: "+msg(e));
                });
        }else{
            appendLocalChat(displayName,finalOutgoing);
            try{ addChatRow(displayName,finalOutgoing); }catch(Exception e){ android.util.Log.e("KINGPlusParty","Local chat row render failed",e); }
            if(keepKeyboard){box.requestFocus();keepRoomKeyboard881(box);}else hideRoomKeyboard(box);
            if(roomChatScroll!=null)roomChatScroll.post(()->roomChatScroll.fullScroll(View.FOCUS_DOWN));
        }
    }
    private void keepRoomKeyboard881(EditText box){
        if(box==null)return;
        try{
            box.postDelayed(()->{
                try{
                    box.requestFocus();
                    android.view.inputmethod.InputMethodManager imm=(android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    if(imm!=null)imm.showSoftInput(box,android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                    ViewCompat.requestApplyInsets(box);
                }catch(Exception ignored){}
            },80);
        }catch(Exception ignored){}
    }
'''
if old not in s: raise SystemExit('sendMessage block missing')
s=s.replace(old,new,1)

old='''    private void addChatRow(String who,String text){
        if(chatBox==null)return;
        boolean follow=roomChatScroll!=null && !roomChatScroll.canScrollVertically(1);
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(9),dp(4),dp(9),dp(5));boolean giftMessage=text!=null&&text.contains("🎁")&&(text.contains(" sent ")||text.contains("Gift"));card.setBackground(bg(giftMessage?0x55b78925:0x20ffffff,12));
        TextView n=tv((giftMessage?"🎁  ":"👤  ")+chatIdentity750(who),11,giftMessage?0xffffdf6b:0xffffedaf,true);n.setPadding(0,0,0,dp(2));
        card.addView(n,new LinearLayout.LayoutParams(-2,-2));
        TextView msg=tv(text,safeRoomMessageTextSize861(text),Color.WHITE,false);msg.setPadding(0,0,0,0);
        text=stickerFallback610(text);msg.setText(text);int live=LiveEmojiView.parse(text);if(ReferenceEmojiView.parse(text)>=0)card.addView(new ReferenceEmojiView(this,ReferenceEmojiView.parse(text),false),new LinearLayout.LayoutParams(dp(72),dp(72)));else if(stickerResource550(text)!=0)card.addView(stickerImage852(stickerResource550(text)),new LinearLayout.LayoutParams(dp(64),dp(64)));else if(live>=0)card.addView(new LiveEmojiView(this,live,4500),new LinearLayout.LayoutParams(dp(72),dp(72)));else{card.addView(msg,new LinearLayout.LayoutParams(-2,-2));}
        final String actionText620=text;card.setOnLongClickListener(v->{roomChatMessageActions620(who,actionText620);return true;});n.setOnClickListener(v->roomChatMessageActions620(who,actionText620));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,-2);p.setMargins(0,dp(3),dp(24),dp(3));chatBox.addView(card,p);
        if(follow){final ScrollView scroll=roomChatScroll;scroll.post(()->scroll.fullScroll(View.FOCUS_DOWN));}
    }
'''
new='''    private void addChatRow(String who,String text){
        if(chatBox==null)return;
        boolean follow=roomChatScroll!=null && !roomChatScroll.canScrollVertically(1);
        int bubbleWidth=Math.min(dp(280),Math.max(dp(220),getResources().getDisplayMetrics().widthPixels-dp(54)));
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setMinimumHeight(dp(58));
        card.setPadding(dp(10),dp(6),dp(10),dp(7));boolean giftMessage=text!=null&&text.contains("🎁")&&(text.contains(" sent ")||text.contains("Gift"));card.setBackground(bg(giftMessage?0x55b78925:0x20ffffff,12));
        TextView n=tv((giftMessage?"🎁  ":"👤  ")+chatIdentity750(who),11,giftMessage?0xffffdf6b:0xffffedaf,true);n.setPadding(0,0,0,dp(3));n.setSingleLine(true);n.setEllipsize(android.text.TextUtils.TruncateAt.END);
        card.addView(n,new LinearLayout.LayoutParams(-1,dp(24)));
        TextView msg=tv(text,14,Color.WHITE,false);msg.setPadding(0,0,0,0);msg.setMinHeight(dp(24));
        text=stickerFallback610(text);msg.setText(text);int live=LiveEmojiView.parse(text);
        if(ReferenceEmojiView.parse(text)>=0){ReferenceEmojiView art=new ReferenceEmojiView(this,ReferenceEmojiView.parse(text),false);LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(dp(64),dp(64));ap.gravity=Gravity.CENTER_HORIZONTAL;card.addView(art,ap);}
        else if(stickerResource550(text)!=0){android.widget.ImageView art=stickerImage852(stickerResource550(text));LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(dp(64),dp(64));ap.gravity=Gravity.CENTER_HORIZONTAL;card.addView(art,ap);}
        else if(live>=0){LiveEmojiView art=new LiveEmojiView(this,live,4500);LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(dp(64),dp(64));ap.gravity=Gravity.CENTER_HORIZONTAL;card.addView(art,ap);}
        else{card.addView(msg,new LinearLayout.LayoutParams(-1,-2));}
        final String actionText620=text;card.setOnLongClickListener(v->{roomChatMessageActions620(who,actionText620);return true;});n.setOnClickListener(v->roomChatMessageActions620(who,actionText620));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(bubbleWidth,-2);p.setMargins(0,dp(3),0,dp(3));chatBox.addView(card,p);
        if(follow){final ScrollView scroll=roomChatScroll;scroll.post(()->scroll.fullScroll(View.FOCUS_DOWN));}
    }
'''
if old not in s: raise SystemExit('addChatRow block missing')
s=s.replace(old,new,1)

old='''            card.setOnClickListener(v->{if(!unlocked){toast("Unlocks at VIP "+vipNeed);return;}giftSelectedIndex=idx;refreshGiftSelection867();rebuildGiftGrid();});LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(110),1);cp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(card,cp);col++;
        }
    }
'''
new='''            name.setSingleLine(true);name.setEllipsize(android.text.TextUtils.TruncateAt.END);
            card.setOnClickListener(v->{if(!unlocked){toast("Unlocks at VIP "+vipNeed);return;}giftSelectedIndex=idx;refreshGiftSelection867();rebuildGiftGrid();});LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(110),1);cp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(card,cp);col++;
        }
        // Keep incomplete rows at four equal columns so cards never stretch larger than the others.
        if(row!=null&&col>0&&col<4){
            for(int i=col;i<4;i++){View spacer=new View(this);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(0,dp(110),1);sp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(spacer,sp);}
        }
    }
'''
if old not in s: raise SystemExit('gift grid end marker missing')
s=s.replace(old,new,1)
party.write_text(s)

m=manifest.read_text()
old='<activity android:name=".PartyActivity" android:exported="false" />'
new='<activity android:name=".PartyActivity" android:exported="false" android:windowSoftInputMode="adjustResize" />'
if old not in m: raise SystemExit('manifest PartyActivity marker missing')
manifest.write_text(m.replace(old,new,1))
print('v8.8.1 chat/gift/IME patch applied')
