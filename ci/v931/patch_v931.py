from pathlib import Path
import sys

root=Path(sys.argv[1])
party=root/'app/src/main/java/com/kingplus/social/PartyActivity.java'
main=root/'app/src/main/java/com/kingplus/social/MainActivity.java'

# ---------------- Party: open UI immediately + same-account no blocking dialog ----------------
s=party.read_text()

old='''        if(roomPrivate&&!isOwner()) tryPrivateEntry(); else joinMemberThenOpen891();
    }
'''
new='''        if(roomPrivate&&!isOwner()) tryPrivateEntry();
        else {
            renderParty();
            setOnlineState900(false,"Joining • syncing account");
            joinMemberThenOpen891();
        }
    }
'''
if old not in s: raise SystemExit('openCloudRoom marker missing')
s=s.replace(old,new,1)

old='''            if(oldDevice!=null&&!oldDevice.isEmpty()&&!oldDevice.equals(now)){
                new AlertDialog.Builder(this).setTitle("Same KING account on another phone")
                    .setMessage("This Google/Firebase account is already present in the room from another device. Two phones using the same account count as one KING user. To appear as two different people, sign in with two different Google accounts.\n\nContinue as the same KING user?")
                    .setNegativeButton("Cancel",(d,w)->renderLobby("Hot"))
                    .setPositiveButton("Continue", (d,w)->write.run()).show();
            }else if(oldDoc!=null&&oldDoc.exists()) write.run();
'''
new='''            if(oldDevice!=null&&!oldDevice.isEmpty()&&!oldDevice.equals(now)){
                toast("Same KING account • syncing this phone");
                write.run();
            }else if(oldDoc!=null&&oldDoc.exists()) write.run();
'''
if old not in s: raise SystemExit('same-account dialog marker missing')
s=s.replace(old,new,1)

old='''        Map<String,Object> full=new HashMap<>();full.put("name",name);full.put("ownerUid",user.getUid());full.put("ownerName",safeName());if(user.getPhotoUrl()!=null)full.put("ownerPhoto",user.getPhotoUrl().toString());full.put("category",category);'''
new='''        Map<String,Object> full=new HashMap<>();full.put("name",name);full.put("ownerUid",user.getUid());full.put("ownerName",safeName());String ownerPhoto931=cloudProfilePhoto868();if(ownerPhoto931.isEmpty()&&user.getPhotoUrl()!=null)ownerPhoto931=user.getPhotoUrl().toString();if(!ownerPhoto931.isEmpty())full.put("ownerPhoto",ownerPhoto931);full.put("category",category);'''
if old not in s: raise SystemExit('room owner photo marker missing')
s=s.replace(old,new,1)

s=s.replace('''d.put("appVersion","9.3.0")''','''d.put("appVersion","9.3.1")''',1)
party.write_text(s)

# ---------------- Main: one Gmail = one canonical cloud identity ----------------
s=main.read_text()

old='''            PushNotifications.refreshToken();
            restoreCloudCosmeticsThenSync();
        }
'''
new='''            PushNotifications.refreshToken();
            restoreCanonicalGoogleProfile931(signedInUser, displayName, false);
        }
'''
if old not in s: raise SystemExit('onCreate restore marker missing')
s=s.replace(old,new,1)

start=s.index('''            String name = user.getDisplayName();
''')
end=s.index('''            home();
''',start)+len('''            home();
''')
old_block=s[start:end]
new_block='''            restoreCanonicalGoogleProfile931(user, fallbackName, true);
'''
s=s[:start]+new_block+s[end:]

marker='''    private void restoreCloudCosmeticsThenSync() {
'''
helpers='''    private void clearCrossAccountIdentity931(String uid){
        SharedPreferences p=getPreferences(0);String previous=p.getString("firebase_uid","");
        if(previous!=null&&!previous.isEmpty()&&!previous.equals(uid)){
            p.edit().remove("name").remove("profile_photo").remove("profile_photo_cloud").remove("bio").remove("tags").remove("hometown").remove("birthday").apply();
            displayName="";
        }
    }
    private String googleName931(FirebaseUser user,String fallback){
        String n=user==null?null:user.getDisplayName();
        if(n==null||n.trim().isEmpty())n=fallback;
        if((n==null||n.trim().isEmpty())&&user!=null&&user.getEmail()!=null&&user.getEmail().contains("@"))n=user.getEmail().substring(0,user.getEmail().indexOf('@'));
        if(n==null||n.trim().isEmpty())n=user==null?"KING User":"KING "+publicId(user.getUid());
        return n.trim();
    }
    private void restoreCanonicalGoogleProfile931(FirebaseUser user,String fallbackName,boolean navigateHome){
        if(user==null)return;
        clearCrossAccountIdentity931(user.getUid());
        final String googleName=googleName931(user,fallbackName);
        final String googlePhoto=user.getPhotoUrl()==null?"":user.getPhotoUrl().toString();
        if(firestore==null){applyCanonicalIdentity931(user,googleName,googlePhoto,null);finishGoogleIdentity931(navigateHome,false);return;}
        firestore.collection("public_profiles").document(user.getUid()).get()
            .addOnSuccessListener(doc->{
                boolean existing=doc!=null&&doc.exists();
                String canonicalName=existing?doc.getString("displayName"):null;
                if(canonicalName==null||canonicalName.trim().isEmpty())canonicalName=googleName;
                String canonicalPhoto=existing?doc.getString("photoUrl"):null;
                if(canonicalPhoto==null||canonicalPhoto.trim().isEmpty())canonicalPhoto=googlePhoto;
                applyCanonicalIdentity931(user,canonicalName,canonicalPhoto,doc);
                if(!existing)syncPublicProfile();
                finishGoogleIdentity931(navigateHome,true);
            })
            .addOnFailureListener(e->{applyCanonicalIdentity931(user,googleName,googlePhoto,null);KingStability.nonFatal(this,"canonical-profile-read",e);finishGoogleIdentity931(navigateHome,false);});
    }
    private void applyCanonicalIdentity931(FirebaseUser user,String canonicalName,String canonicalPhoto,com.google.firebase.firestore.DocumentSnapshot doc){
        displayName=canonicalName==null||canonicalName.trim().isEmpty()?googleName931(user,null):canonicalName.trim();
        SharedPreferences.Editor e=getPreferences(0).edit().putString("name",displayName).putString("login_provider","Google").putString("firebase_uid",user.getUid());
        if(user.getEmail()!=null)e.putString("google_email",user.getEmail());
        if(user.getPhotoUrl()!=null)e.putString("google_photo_url",user.getPhotoUrl().toString());
        if(canonicalPhoto!=null&&!canonicalPhoto.trim().isEmpty())e.putString("profile_photo_cloud",canonicalPhoto.trim());
        if(doc!=null&&doc.exists()){
            String bio=doc.getString("bio"),tags=doc.getString("tags"),home=doc.getString("hometown"),birthday=doc.getString("birthday");
            if(bio!=null)e.putString("bio",bio);if(tags!=null)e.putString("tags",tags);if(home!=null)e.putString("hometown",home);if(birthday!=null)e.putString("birthday",birthday);
            String frame=doc.getString("equippedFrame"),effect=doc.getString("entranceEffect");
            if(frame!=null&&!frame.trim().isEmpty())KingCosmetics.setFrame(this,frame.trim());
            if(effect!=null&&!effect.trim().isEmpty())KingCosmetics.setEffect(this,effect.trim());
        }
        e.apply();
        try{
            com.google.firebase.auth.UserProfileChangeRequest.Builder b=new com.google.firebase.auth.UserProfileChangeRequest.Builder().setDisplayName(displayName);
            if(canonicalPhoto!=null&&!canonicalPhoto.trim().isEmpty())b.setPhotoUri(android.net.Uri.parse(canonicalPhoto.trim()));
            user.updateProfile(b.build()).addOnFailureListener(x->{});
        }catch(Exception ignored){}
    }
    private void finishGoogleIdentity931(boolean navigateHome,boolean cloudRestored){
        try{PushNotifications.refreshToken();}catch(Exception ignored){}
        try{CloudSync.syncProfileAndTestWallet(this,getPreferences(0),displayName,coinBalance,giftCount,receivedGiftCount,(ok,message)->{});}catch(Exception ignored){}
        if(navigateHome){Toast.makeText(this,cloudRestored?"Google login • KING profile restored":"Google login successful",Toast.LENGTH_SHORT).show();home();}
        else if("profile".equals(screen))profile();
    }
    private void loadRemoteAvatar931(ImageView image,TextView fallback,String url){
        if(url==null||url.trim().isEmpty())return;final String source=url.trim();image.setTag(source);
        new Thread(()->{try{java.net.URLConnection c=new java.net.URL(source).openConnection();c.setConnectTimeout(8000);c.setReadTimeout(8000);try(java.io.InputStream in=c.getInputStream()){android.graphics.BitmapFactory.Options o=new android.graphics.BitmapFactory.Options();o.inSampleSize=2;android.graphics.Bitmap bm=android.graphics.BitmapFactory.decodeStream(in,null,o);if(bm!=null)runOnUiThread(()->{if(!isFinishing()&&source.equals(image.getTag())){image.setImageBitmap(bm);if(fallback!=null)fallback.setVisibility(View.GONE);}});}}}catch(Exception ignored){}}).start();
    }
    private View profileAvatarView931(String localPhoto,String remotePhoto){
        FrameLayout box=new FrameLayout(this);TextView fallback=new TextView(this);fallback.setText("👑");fallback.setTextSize(39);fallback.setGravity(Gravity.CENTER);fallback.setBackground(background(0xffffefd1,44));box.addView(fallback,new FrameLayout.LayoutParams(-1,-1));
        ImageView img=new ImageView(this);img.setScaleType(ImageView.ScaleType.CENTER_CROP);img.setBackground(background(0x00ffffff,44));box.addView(img,new FrameLayout.LayoutParams(-1,-1));
        if(localPhoto!=null&&!localPhoto.trim().isEmpty()){try{img.setImageURI(Uri.parse(localPhoto.trim()));if(img.getDrawable()!=null)fallback.setVisibility(View.GONE);}catch(Exception ignored){}}
        else if(remotePhoto!=null&&!remotePhoto.trim().isEmpty())loadRemoteAvatar931(img,fallback,remotePhoto);
        return box;
    }

'''
if marker not in s: raise SystemExit('restoreCloud marker missing')
s=s.replace(marker,helpers+marker,1)

old='''        final String photo=getPreferences(0).getString("profile_photo","");
'''
new='''        final String photo=getPreferences(0).getString("profile_photo","");final String remotePhoto931=getPreferences(0).getString("profile_photo_cloud",getPreferences(0).getString("google_photo_url",""));
'''
if old not in s: raise SystemExit('profile photo declaration missing')
s=s.replace(old,new,1)

old='''        String equippedFrame730=KingCosmetics.frame(this);View avatar;
        if(photo.isEmpty()){TextView a=new TextView(this);a.setText("👑");a.setTextSize(39);a.setGravity(Gravity.CENTER);a.setBackground(background(0xffffefd1,44));avatar=a;}else{ImageView a=new ImageView(this);a.setScaleType(ImageView.ScaleType.CENTER_CROP);try{a.setImageURI(Uri.parse(photo));}catch(Exception ignored){}a.setBackground(background(0xffffefd1,44));avatar=a;}
'''
new='''        String equippedFrame730=KingCosmetics.frame(this);View avatar=profileAvatarView931(photo,remotePhoto931);
'''
if old not in s: raise SystemExit('profile avatar block missing')
s=s.replace(old,new,1)

main.write_text(s)
print('v9.3.1 room-open + canonical Gmail identity patch applied')
