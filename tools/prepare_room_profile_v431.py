from pathlib import Path
import re

PARTY=Path('app/src/main/java/com/kingplus/social/PartyActivity.java')
BUILD=Path('app/build.gradle')

s=PARTY.read_text(encoding='utf-8')
for imp in [
    'import android.graphics.Bitmap;\n',
    'import android.graphics.BitmapFactory;\n',
    'import android.widget.FrameLayout;\n',
    'import android.widget.ImageView;\n',
    'import java.net.URL;\n',
]:
    if imp not in s:
        if imp.startswith('import android.graphics.'):
            s=s.replace('import android.graphics.Color;\n', 'import android.graphics.Color;\n'+imp,1)
        elif imp.startswith('import android.widget.'):
            s=s.replace('import android.widget.EditText;\n', 'import android.widget.EditText;\n'+imp,1)
        else:
            s=s.replace('import java.util.ArrayList;\n', imp+'import java.util.ArrayList;\n',1)

start=s.index('    private void showRichProfile(String uid,String name,boolean host){')
end=s.index('\n    private void kickUser(', start)
new_method=r'''    private void showRichProfile(String uid,String name,boolean host){
        if(uid==null||uid.isEmpty())return;
        final String fallbackName=(name==null||name.trim().isEmpty())?"KING User":name.trim();
        ScrollView scroll=new ScrollView(this);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setGravity(Gravity.CENTER_HORIZONTAL);root.setPadding(dp(18),dp(12),dp(18),dp(16));root.setBackground(bg(Color.WHITE,24));scroll.addView(root);

        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);
        TextView mention=tv("⚠   @ Mention",15,0xffb3adb8,true);mention.setGravity(Gravity.CENTER_VERTICAL);top.addView(mention,new LinearLayout.LayoutParams(0,dp(44),1));
        TextView more=tv("⋮",26,0xff77717d,true);more.setGravity(Gravity.CENTER);top.addView(more,new LinearLayout.LayoutParams(dp(44),dp(44)));root.addView(top,new LinearLayout.LayoutParams(-1,dp(44)));

        FrameLayout avatarFrame=new FrameLayout(this);avatarFrame.setBackground(bg(0xffe4f3ff,70));
        ImageView avatarImage=new ImageView(this);avatarImage.setScaleType(ImageView.ScaleType.CENTER_CROP);avatarImage.setClipToOutline(true);avatarImage.setBackground(bg(0xffe4f3ff,70));avatarFrame.addView(avatarImage,new FrameLayout.LayoutParams(-1,-1));
        TextView avatarInitial=tv(fallbackName.substring(0,1).toUpperCase(),42,Color.WHITE,true);avatarInitial.setGravity(Gravity.CENTER);avatarInitial.setBackground(bg(0xff4b86d8,70));avatarFrame.addView(avatarInitial,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout.LayoutParams afp=new LinearLayout.LayoutParams(dp(104),dp(104));afp.setMargins(0,0,0,dp(5));root.addView(avatarFrame,afp);

        TextView n=tv((host?"👑 ":"")+fallbackName,20,0xff17131c,true);n.setGravity(Gravity.CENTER);root.addView(n,new LinearLayout.LayoutParams(-1,dp(34)));
        LinearLayout badges=new LinearLayout(this);badges.setGravity(Gravity.CENTER);
        TextView vip=tv("💎 VIP",12,Color.WHITE,true);vip.setGravity(Gravity.CENTER);vip.setBackground(bg(0xff5796df,7));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(dp(78),dp(30));bp.setMargins(dp(3),0,dp(3),0);badges.addView(vip,bp);
        TextView level=tv("Lv 1",12,Color.WHITE,true);level.setGravity(Gravity.CENTER);level.setBackground(bg(0xff7ddc40,7));LinearLayout.LayoutParams lpv=new LinearLayout.LayoutParams(dp(62),dp(30));lpv.setMargins(dp(3),0,dp(3),0);badges.addView(level,lpv);root.addView(badges,new LinearLayout.LayoutParams(-1,dp(38)));

        TextView identity=tv("▣  KING ID "+publicId(uid)+"   |   Followers",13,0xff66606b,true);identity.setGravity(Gravity.CENTER);root.addView(identity,new LinearLayout.LayoutParams(-1,dp(34)));

        LinearLayout cards=new LinearLayout(this);cards.setGravity(Gravity.CENTER);
        String[] cs={"⭐  VIP level\n—","🏆  Contribution\n—","🔥  Charisma\n—"};int[] cc={0xffe6f8ff,0xffe9fff6,0xfffff7d9};
        for(int i=0;i<cs.length;i++){TextView x=tv(cs[i],11,0xff395065,true);x.setGravity(Gravity.CENTER);x.setBackground(bg(cc[i],10));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(70),1);cp.setMargins(dp(3),dp(8),dp(3),dp(8));cards.addView(x,cp);}root.addView(cards,new LinearLayout.LayoutParams(-1,dp(86)));

        LinearLayout family=new LinearLayout(this);family.setGravity(Gravity.CENTER_VERTICAL);family.setPadding(dp(10),dp(8),dp(10),dp(8));family.setBackground(bg(0xfffff6df,12));
        TextView famIcon=tv("👑",28,0xffffa000,false);famIcon.setGravity(Gravity.CENTER);family.addView(famIcon,new LinearLayout.LayoutParams(dp(48),dp(48)));
        LinearLayout famInfo=new LinearLayout(this);famInfo.setOrientation(LinearLayout.VERTICAL);famInfo.addView(tv("Family",12,0xff7d6030,true));famInfo.addView(tv("Not joined",14,0xff30261b,true));family.addView(famInfo,new LinearLayout.LayoutParams(0,dp(48),1));
        TextView famArrow=tv("›",28,0xffaa956f,false);famArrow.setGravity(Gravity.CENTER);family.addView(famArrow,new LinearLayout.LayoutParams(dp(36),dp(48)));LinearLayout.LayoutParams fmp=new LinearLayout.LayoutParams(-1,dp(68));fmp.setMargins(0,dp(3),0,dp(6));root.addView(family,fmp);

        LinearLayout relation=new LinearLayout(this);relation.setGravity(Gravity.CENTER_VERTICAL);relation.setPadding(dp(10),dp(8),dp(10),dp(8));relation.setBackground(bg(0xffffeaf4,12));
        TextView relIcon=tv("💞",28,0xffff5e9c,false);relIcon.setGravity(Gravity.CENTER);relation.addView(relIcon,new LinearLayout.LayoutParams(dp(56),dp(48)));
        LinearLayout relInfo=new LinearLayout(this);relInfo.setOrientation(LinearLayout.VERTICAL);relInfo.addView(tv("Relationship",12,0xff9e4c70,true));relInfo.addView(tv("Not set",14,0xff3a2130,true));relation.addView(relInfo,new LinearLayout.LayoutParams(0,dp(48),1));
        TextView relBadge=tv("CP",12,0xffe25291,true);relBadge.setGravity(Gravity.CENTER);relation.addView(relBadge,new LinearLayout.LayoutParams(dp(44),dp(48)));LinearLayout.LayoutParams rlp=new LinearLayout.LayoutParams(-1,dp(68));rlp.setMargins(0,0,0,dp(8));root.addView(relation,rlp);

        LinearLayout wallRow=profileInfoRow("Gift Wall","No gifts yet","🎁   🌹   💗   ›");root.addView(wallRow,new LinearLayout.LayoutParams(-1,dp(62)));
        LinearLayout medalsRow=profileInfoRow("Medals","No medals yet","🏅   🥇   👑   ›");root.addView(medalsRow,new LinearLayout.LayoutParams(-1,dp(62)));

        LinearLayout actions=new LinearLayout(this);actions.setPadding(0,dp(10),0,0);
        TextView follow=pill("👤  Follow",0xffffbd00,()->{});follow.setTextColor(Color.WHITE);follow.setTextSize(16);actions.addView(follow,new LinearLayout.LayoutParams(0,dp(54),1));
        TextView gift=pill("🎁  SEND GIFT",0xffff693e,()->giftDialogFor(uid,fallbackName));gift.setTextColor(Color.WHITE);gift.setTextSize(16);LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(0,dp(54),1);gp.setMargins(dp(10),0,0,0);actions.addView(gift,gp);root.addView(actions,new LinearLayout.LayoutParams(-1,dp(64)));

        AlertDialog dialog=new AlertDialog.Builder(this).setView(scroll).create();
        mention.setOnClickListener(v->{if(composerBox!=null){composerBox.setText("@"+fallbackName+" ");composerBox.requestFocus();}dialog.dismiss();});
        follow.setOnClickListener(v->toggleProfileFollow(uid,fallbackName,follow));
        more.setOnClickListener(v->showProfileMore(uid,fallbackName));
        dialog.setOnShowListener(x->{android.view.Window w=dialog.getWindow();if(w!=null){w.setLayout(-1,-2);w.setGravity(Gravity.BOTTOM);}});
        dialog.show();

        if(user!=null&&db!=null){
            String relationId=user.getUid()+"__"+uid;
            db.collection("follows").document(relationId).get().addOnSuccessListener(d->follow.setText(d.exists()?"✓  Following":"👤  Follow"));
            db.collection("follows").whereEqualTo("targetUid",uid).get().addOnSuccessListener(q->identity.setText("▣  KING ID "+publicId(uid)+"   |   "+q.size()+" Followers"));
            db.collection("live_rooms").document(roomId).collection("members").document(uid).get().addOnSuccessListener(m->{
                if(!m.exists())return;String mn=m.getString("name");if(mn!=null&&!mn.trim().isEmpty())n.setText((host?"👑 ":"")+mn.trim());String photo=m.getString("photoUrl");if(photo!=null&&!photo.trim().isEmpty())loadProfilePhoto(avatarImage,avatarInitial,photo.trim());
            });
            db.collection("public_profiles").document(uid).get().addOnSuccessListener(p->{if(p.exists()){String pn=p.getString("displayName");if(pn!=null&&!pn.trim().isEmpty())n.setText((host?"👑 ":"")+pn.trim());}});
        }
    }

    private LinearLayout profileInfoRow(String title,String sub,String right){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(10),dp(5),dp(8),dp(5));row.setBackgroundColor(Color.WHITE);
        LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);info.addView(tv(title,16,0xff332e36,true));info.addView(tv(sub,12,0xff9b959f,false));row.addView(info,new LinearLayout.LayoutParams(0,dp(52),1));
        TextView r=tv(right,14,0xff7d7781,false);r.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);row.addView(r,new LinearLayout.LayoutParams(dp(185),dp(52)));return row;
    }

    private String publicId(String uid){if(uid==null||uid.isEmpty())return "000000";long value=Integer.toUnsignedLong(uid.hashCode());return String.valueOf((value%900000L)+100000L);}

    private void loadProfilePhoto(ImageView image,TextView fallback,String url){
        new Thread(()->{try{Bitmap b=BitmapFactory.decodeStream(new URL(url).openStream());if(b!=null)runOnUiThread(()->{image.setImageBitmap(b);fallback.setVisibility(View.GONE);});}catch(Exception ignored){}}).start();
    }

    private void toggleProfileFollow(String uid,String name,TextView button){
        if(user==null||db==null){toast("Google / Firebase sign-in required");return;}if(uid.equals(user.getUid())){toast("This is you");return;}
        String id=user.getUid()+"__"+uid;DocumentReference ref=db.collection("follows").document(id);
        ref.get().addOnSuccessListener(doc->{
            if(doc.exists())ref.delete().addOnSuccessListener(v->{button.setText("👤  Follow");toast("Disconnected from "+name);});
            else{Map<String,Object>d=new HashMap<>();d.put("followerUid",user.getUid());d.put("targetUid",uid);d.put("followerName",safeName());d.put("createdAt",FieldValue.serverTimestamp());ref.set(d).addOnSuccessListener(v->{button.setText("✓  Following");CloudBackend.sendFollowNotification(uid,safeName(),(ok,m)->{});toast("Following "+name);});}
        }).addOnFailureListener(e->toast(msg(e)));
    }

    private void showProfileMore(String uid,String name){
        List<String> items=new ArrayList<>();items.add("💬 Private chat");items.add("⚑ Report");
        if(isModerator()&&!uid.equals(ownerUid)){items.add("🚪 Kick");items.add("🚫 Ban");}else items.add("🚫 Block");
        new AlertDialog.Builder(this).setTitle(name).setItems(items.toArray(new String[0]),(d,w)->{String x=items.get(w);if(x.contains("Private"))openPrivateChat(uid,name);else if(x.contains("Report"))reportUser(uid,name);else if(x.contains("Kick"))kickUser(uid,name);else if(x.contains("Ban"))banUser(uid,name);else if(x.contains("Block"))blockUserLocally(uid,name);}).setNegativeButton("Close",null).show();
    }
'''
s=s[:start]+new_method+s[end:]

s=s.replace('String id=user.getUid()+"_"+uid;DocumentReference ref=db.collection("follows").document(id);', 'String id=user.getUid()+"__"+uid;DocumentReference ref=db.collection("follows").document(id);')
s=s.replace('String id=user.getUid()+"_"+ownerUid;DocumentReference ref=db.collection("follows").document(id);', 'String id=user.getUid()+"__"+ownerUid;DocumentReference ref=db.collection("follows").document(id);')

old='private void registerMember(){if(user==null||db==null||roomId==null)return;Map<String,Object>d=new HashMap<>();d.put("uid",user.getUid());d.put("name",safeName());d.put("joinedAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).collection("members").document(user.getUid()).set(d)'
new='private void registerMember(){if(user==null||db==null||roomId==null)return;Map<String,Object>d=new HashMap<>();d.put("uid",user.getUid());d.put("name",safeName());if(user.getPhotoUrl()!=null)d.put("photoUrl",user.getPhotoUrl().toString());d.put("joinedAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).collection("members").document(user.getUid()).set(d)'
if old not in s: raise SystemExit('Party registerMember template changed')
s=s.replace(old,new,1)
PARTY.write_text(s,encoding='utf-8')

b=BUILD.read_text(encoding='utf-8')
b=re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 65; versionName '4.3.1'", b)
BUILD.write_text(b,encoding='utf-8')
print('Prepared KING Plus v4.3.1 room profile reference UI + visible KING ID')
