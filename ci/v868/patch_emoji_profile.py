from pathlib import Path
import sys
root=Path(sys.argv[1])
party=root/'app/src/main/java/com/kingplus/social/PartyActivity.java'
main=root/'app/src/main/java/com/kingplus/social/MainActivity.java'
ps=party.read_text()
ms=main.read_text()

old_fill='''    private void fillEmojiGrid610(LinearLayout grid,String[] emojis,int columns,boolean livePack){
        grid.removeAllViews();
        if(emojis==null||emojis.length==0){TextView empty=tv("No favourites yet\\nLong-press any Face or Expression to add it here.",14,0xff777777,false);empty.setGravity(Gravity.CENTER);grid.addView(empty,new LinearLayout.LayoutParams(-1,dp(150)));return;}
        LinearLayout row=null;
        for(int i=0;i<emojis.length;i++){
            if(i%columns==0){row=new LinearLayout(this);row.setGravity(Gravity.CENTER);grid.addView(row,new LinearLayout.LayoutParams(-1,dp(columns==6?58:(columns==5?70:78))));}
            final String token=emojis[i];int live=LiveEmojiView.parse(token);View cell;
            if(ReferenceEmojiView.parse(token)>=0){cell=new ReferenceEmojiView(this,ReferenceEmojiView.parse(token),true);}
            else if(stickerResource550(token)!=0){android.widget.ImageView im=new android.widget.ImageView(this);im.setImageResource(stickerResource550(token));im.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);im.setContentDescription("Sticker "+token);cell=im;}
            else if(live>=0){cell=new LiveEmojiView(this,live,0);cell.setContentDescription("Send animated "+LiveEmojiView.LABELS[live]);}
            else{TextView text=tv(token,columns==6?31:34,0xff202020,false);text.setGravity(Gravity.CENTER);text.setContentDescription("Send "+token);cell=text;}
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-1,1);cp.setMargins(dp(2),dp(2),dp(2),dp(2));row.addView(cell,cp);
            cell.setOnClickListener(v->sendLiveEmojiV530(token));
            if(!livePack)cell.setOnLongClickListener(v->{toggleFavouriteEmoji610(token);return true;});
        }
        if(row!=null){int rem=emojis.length%columns;if(rem>0)for(int i=rem;i<columns;i++)row.addView(new View(this),new LinearLayout.LayoutParams(0,1,1));}
    }
'''
new_fill='''    private void fillEmojiGrid610(LinearLayout grid,String[] emojis,int columns,boolean livePack){
        grid.removeAllViews();
        if(emojis==null||emojis.length==0){TextView empty=tv("No favourites yet\\nLong-press any Face or Expression to add it here.",14,0xff777777,false);empty.setGravity(Gravity.CENTER);grid.addView(empty,new LinearLayout.LayoutParams(-1,dp(150)));return;}
        final int rowHeight=columns>=6?dp(64):dp(72);
        final int artSize=columns>=6?dp(46):dp(52);
        LinearLayout row=null;
        for(int i=0;i<emojis.length;i++){
            if(i%columns==0){row=new LinearLayout(this);row.setGravity(Gravity.CENTER);grid.addView(row,new LinearLayout.LayoutParams(-1,rowHeight));}
            final String token=emojis[i];int live=LiveEmojiView.parse(token);View art;
            if(ReferenceEmojiView.parse(token)>=0){art=new ReferenceEmojiView(this,ReferenceEmojiView.parse(token),true);}
            else if(stickerResource550(token)!=0){android.widget.ImageView im=new android.widget.ImageView(this);im.setImageResource(stickerResource550(token));im.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);im.setAdjustViewBounds(false);im.setContentDescription("Sticker "+token);art=im;}
            else if(live>=0){art=new LiveEmojiView(this,live,0);art.setContentDescription("Send animated "+LiveEmojiView.LABELS[live]);}
            else{TextView text=tv(token,columns>=6?30:33,0xff202020,false);text.setGravity(Gravity.CENTER);text.setPadding(0,0,0,0);text.setContentDescription("Send "+token);art=text;}
            FrameLayout cell=new FrameLayout(this);cell.setForegroundGravity(Gravity.CENTER);cell.setPadding(dp(2),dp(2),dp(2),dp(2));
            FrameLayout.LayoutParams ap=new FrameLayout.LayoutParams(artSize,artSize,Gravity.CENTER);cell.addView(art,ap);
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,rowHeight,1);cp.setMargins(dp(1),dp(1),dp(1),dp(1));row.addView(cell,cp);
            cell.setOnClickListener(v->sendLiveEmojiV530(token));
            if(!livePack)cell.setOnLongClickListener(v->{toggleFavouriteEmoji610(token);return true;});
        }
        if(row!=null){int rem=emojis.length%columns;if(rem>0)for(int i=rem;i<columns;i++)row.addView(new View(this),new LinearLayout.LayoutParams(0,rowHeight,1));}
    }
'''
if old_fill not in ps: raise SystemExit('fillEmojiGrid610 block not found')
ps=ps.replace(old_fill,new_fill,1)

old_seat='String photoUrl=memberPhotos540.get(seatUids.get(no));if(mine&&user!=null&&user.getPhotoUrl()!=null)photoUrl=user.getPhotoUrl().toString();if(n!=null&&photoUrl!=null&&!photoUrl.isEmpty())loadProfilePhoto(photo,av,photoUrl);'
new_seat='String photoUrl=memberPhotos540.get(seatUids.get(no));if(mine){String localPhoto868=localProfilePhoto868();if(!localPhoto868.isEmpty())photoUrl=localPhoto868;else{String cloudPhoto868=cloudProfilePhoto868();if(!cloudPhoto868.isEmpty())photoUrl=cloudPhoto868;else if(user!=null&&user.getPhotoUrl()!=null)photoUrl=user.getPhotoUrl().toString();}}if(n!=null&&photoUrl!=null&&!photoUrl.isEmpty())applyProfilePhoto868(photo,av,photoUrl);'
if old_seat not in ps: raise SystemExit('seat photo line not found')
ps=ps.replace(old_seat,new_seat,1)

old_register='if(user.getPhotoUrl()!=null)d.put("photoUrl",user.getPhotoUrl().toString());'
new_register='String cloudPhoto868=cloudProfilePhoto868();if(!cloudPhoto868.isEmpty())d.put("photoUrl",cloudPhoto868);else if(user.getPhotoUrl()!=null)d.put("photoUrl",user.getPhotoUrl().toString());'
if old_register not in ps: raise SystemExit('register photo line not found')
ps=ps.replace(old_register,new_register,1)

marker='''    private void loadProfilePhoto(ImageView image,TextView fallback,String url){
'''
helpers='''    private SharedPreferences mainProfilePrefs868(){return getSharedPreferences("MainActivity",MODE_PRIVATE);}
    private String localProfilePhoto868(){String s=mainProfilePrefs868().getString("profile_photo","");return s==null?"":s.trim();}
    private String cloudProfilePhoto868(){String s=mainProfilePrefs868().getString("profile_photo_cloud","");return s==null?"":s.trim();}
    private void applyProfilePhoto868(ImageView image,TextView fallback,String source){
        if(source==null||source.trim().isEmpty())return;String s=source.trim();
        if(s.startsWith("content://")||s.startsWith("file://")||s.startsWith("android.resource://")){
            try{image.setImageURI(Uri.parse(s));if(image.getDrawable()!=null)fallback.setVisibility(View.GONE);}catch(Exception ignored){}
            return;
        }
        loadProfilePhoto(image,fallback,s);
    }
    private void refreshOwnMemberPhoto868(){
        if(!cloudRoom||db==null||user==null||roomId==null)return;String cloud=cloudProfilePhoto868();
        if(cloud.isEmpty()&&user.getPhotoUrl()!=null)cloud=user.getPhotoUrl().toString();
        if(!cloud.isEmpty())db.collection("live_rooms").document(roomId).collection("members").document(user.getUid()).update("photoUrl",cloud).addOnFailureListener(e->{});
    }

'''
if marker not in ps: raise SystemExit('loadProfilePhoto marker missing')
ps=ps.replace(marker,helpers+marker,1)

oncreate_end='''        } else renderLobby("Hot");
    }

    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
'''
onresume='''        } else renderLobby("Hot");
    }

    @Override protected void onResume(){
        super.onResume();
        if(seatsBox!=null)rebuildSeats();
        refreshOwnMemberPhoto868();
    }

    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
'''
if oncreate_end not in ps: raise SystemExit('onCreate end marker missing')
ps=ps.replace(oncreate_end,onresume,1)

imp='import com.google.firebase.firestore.DocumentSnapshot;\n'
add='import com.google.firebase.firestore.DocumentSnapshot;\nimport com.google.firebase.storage.FirebaseStorage;\nimport com.google.firebase.storage.StorageReference;\n'
if imp not in ms: raise SystemExit('MainActivity import marker missing')
ms=ms.replace(imp,add,1)

sync_marker='''        profile.put("birthday", getPreferences(0).getString("birthday",""));
'''
sync_add='''        profile.put("birthday", getPreferences(0).getString("birthday",""));
        String cloudPhoto868=getPreferences(0).getString("profile_photo_cloud","");
        if(cloudPhoto868!=null&&!cloudPhoto868.trim().isEmpty())profile.put("photoUrl",cloudPhoto868.trim());
        else if(me.getPhotoUrl()!=null)profile.put("photoUrl",me.getPhotoUrl().toString());
'''
if sync_marker not in ms: raise SystemExit('sync marker missing')
ms=ms.replace(sync_marker,sync_add,1)

old_req='''        if (requestCode == 24 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri=data.getData();
            try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); getPreferences(0).edit().putString("profile_photo",uri.toString()).apply(); Toast.makeText(this,"Profile photo updated",Toast.LENGTH_SHORT).show(); profile(); }
            catch (Exception ex) { Toast.makeText(this,"Profile photo could not be saved",Toast.LENGTH_SHORT).show(); }
            return;
        }
'''
new_req='''        if (requestCode == 24 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri=data.getData();
            try {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                getPreferences(0).edit().putString("profile_photo",uri.toString()).apply();
                uploadProfilePhoto868(uri);
                Toast.makeText(this,"Profile photo updated",Toast.LENGTH_SHORT).show();
                profile();
            } catch (Exception ex) { Toast.makeText(this,"Profile photo could not be saved",Toast.LENGTH_SHORT).show(); }
            return;
        }
'''
if old_req not in ms: raise SystemExit('request 24 block missing')
ms=ms.replace(old_req,new_req,1)

show_posts='''    private void showPosts() {
'''
upload_method='''    private void uploadProfilePhoto868(Uri uri){
        if(uri==null||firebaseAuth==null||firebaseAuth.getCurrentUser()==null)return;
        FirebaseUser me=firebaseAuth.getCurrentUser();
        try{
            StorageReference ref=FirebaseStorage.getInstance().getReference().child("chat_media/"+me.getUid()+"/profile/avatar_"+System.currentTimeMillis()+".jpg");
            ref.putFile(uri).continueWithTask(t->{if(!t.isSuccessful()){Exception e=t.getException();if(e!=null)throw e;}return ref.getDownloadUrl();})
                .addOnSuccessListener(download->{
                    String url=download.toString();getPreferences(0).edit().putString("profile_photo_cloud",url).apply();
                    if(firestore!=null){Map<String,Object> photo=new HashMap<>();photo.put("photoUrl",url);photo.put("updatedAt",com.google.firebase.firestore.FieldValue.serverTimestamp());firestore.collection("public_profiles").document(me.getUid()).set(photo,com.google.firebase.firestore.SetOptions.merge());firestore.collection("users").document(me.getUid()).set(photo,com.google.firebase.firestore.SetOptions.merge());}
                    Toast.makeText(this,"Profile photo synced to Party Room",Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e->Toast.makeText(this,"Photo saved on this phone; cloud sync unavailable",Toast.LENGTH_SHORT).show());
        }catch(Exception e){Toast.makeText(this,"Photo saved on this phone; cloud sync unavailable",Toast.LENGTH_SHORT).show();}
    }

'''
if show_posts not in ms: raise SystemExit('showPosts marker missing')
ms=ms.replace(show_posts,upload_method+show_posts,1)

party.write_text(ps)
main.write_text(ms)
print('patched')
