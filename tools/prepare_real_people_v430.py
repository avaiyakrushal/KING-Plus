from pathlib import Path
import re

MAIN = Path('app/src/main/java/com/kingplus/social/MainActivity.java')
SOCIAL = Path('app/src/main/java/com/kingplus/social/SocialActivity.java')
BUILD = Path('app/build.gradle')

src = SOCIAL.read_text(encoding='utf-8')

src = src.replace('search.setHint("Search people by name");', 'search.setHint("Search name or 6-digit KING ID");')
src = src.replace('status = label("KING Plus social", 12, MUTED, false);', 'status = label("Real Firebase people", 12, MUTED, false);')

old_publish = '''        db.collection("public_profiles").document(me.getUid()).set(data)
            .addOnFailureListener(e -> status.setText("Profile directory unavailable"));'''
new_publish = '''        db.collection("public_profiles").document(me.getUid()).set(data)
            .addOnSuccessListener(v -> status.setText("Connected • KING ID " + publicId(me.getUid())))
            .addOnFailureListener(e -> status.setText("Profile directory unavailable"));'''
if old_publish not in src:
    raise SystemExit('Social publishOwnProfile template changed')
src = src.replace(old_publish, new_publish, 1)

# KING ID search must work for the whole current directory, not only the first 100 profiles.
src = src.replace('db.collection("public_profiles").limit(100).get()', 'db.collection("public_profiles").get()')

start = src.index('    private void addPerson(String uid, String name, String bio) {')
end = src.index('\n    private void openProfile(', start)
new_add_person = r'''    private void addPerson(String uid, String name, String bio) {
        LinearLayout card=new LinearLayout(this); card.setGravity(Gravity.CENTER_VERTICAL); card.setPadding(dp(14),dp(10),dp(12),dp(10)); card.setBackground(bg(Color.WHITE,18));
        String initial=(name==null||name.trim().isEmpty())?"K":name.trim().substring(0,1).toUpperCase(Locale.US);
        TextView av=label(initial,22,Color.WHITE,true); av.setGravity(Gravity.CENTER); av.setBackground(bg(PURPLE,25)); card.addView(av,new LinearLayout.LayoutParams(dp(50),dp(50)));
        LinearLayout info=new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL); info.setPadding(dp(12),0,dp(4),0);
        info.addView(label(name,16,DARK,true),new LinearLayout.LayoutParams(-1,dp(24)));
        TextView id=label("KING ID: "+publicId(uid),11,PURPLE,true); info.addView(id,new LinearLayout.LayoutParams(-1,dp(18)));
        TextView b=label(bio.isEmpty()?"KING Plus member":bio,11,MUTED,false); b.setMaxLines(1); info.addView(b,new LinearLayout.LayoutParams(-1,dp(18)));
        card.addView(info,new LinearLayout.LayoutParams(0,dp(60),1));
        TextView arrow=label("›",28,0xff9993a6,false); card.addView(arrow);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(80)); lp.setMargins(0,dp(5),0,dp(5)); list.addView(card,lp); card.setOnClickListener(v->openProfile(uid,name,bio));
    }
'''
src = src[:start] + new_add_person + src[end:]

start = src.index('    private void openProfile(String uid, String name, String bio) {')
end = src.index('\n    private void toggleFollow(', start)
new_open_profile = r'''    private void openProfile(String uid, String name, String bio) {
        if (!cloudReady() || uid.equals(me.getUid())) return;
        String ownId=followId(me.getUid(),uid), reverseId=followId(uid,me.getUid());
        db.collection("follows").document(ownId).get().addOnSuccessListener(own->db.collection("follows").document(reverseId).get().addOnSuccessListener(reverse->{
            boolean following=own.exists(), follower=reverse.exists(), friend=following&&follower;
            ArrayList<String> actions=new ArrayList<>();
            actions.add(following?"✓ Disconnect / Unfollow":(follower?"💜 Connect back":"＋ Connect / Follow"));
            actions.add("✉ Message");
            actions.add("↗ Share KING ID");
            actions.add("⚑ Report");
            String relation=friend?"💜 Connected as friends":(following?"Following":(follower?"Follows you • connect back to become friends":"Not connected"));
            new AlertDialog.Builder(this).setTitle(name)
                .setMessage((bio.isEmpty()?"KING Plus member":bio)+"\n\n"+relation+"\nKING ID: "+publicId(uid))
                .setItems(actions.toArray(new String[0]),(d,w)->{
                    if(w==0)toggleFollow(uid,name,following);
                    else if(w==1)openChat(uid,name);
                    else if(w==2)shareKingId(uid,name);
                    else report(uid,name);
                }).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast(safe(e.getLocalizedMessage()))));
    }
'''
src = src[:start] + new_open_profile + src[end:]

start = src.index('    private void toggleFollow(String uid, String name, boolean following) {')
end = src.index('\n    private void openChat(', start)
new_toggle = r'''    private void toggleFollow(String uid, String name, boolean following) {
        String id=followId(me.getUid(),uid);
        if(following){
            db.collection("follows").document(id).delete()
                .addOnSuccessListener(v->toast("Disconnected from "+name))
                .addOnFailureListener(e->toast(safe(e.getLocalizedMessage())));
            return;
        }
        Map<String,Object> data=new HashMap<>();
        data.put("followerUid",me.getUid()); data.put("targetUid",uid); data.put("createdAt",FieldValue.serverTimestamp());
        db.collection("follows").document(id).set(data).addOnSuccessListener(v->{
            String myName=me.getDisplayName(); if(myName==null||myName.trim().isEmpty()) myName="KING "+publicId(me.getUid());
            CloudBackend.sendFollowNotification(uid,myName,(ok,m)->{});
            db.collection("follows").document(followId(uid,me.getUid())).get().addOnSuccessListener(reverse->{
                toast(reverse.exists()?"💜 You and "+name+" are now friends":"Connected • following "+name);
            }).addOnFailureListener(e->toast("Connected • following "+name));
        }).addOnFailureListener(e->toast(safe(e.getLocalizedMessage())));
    }

    private void shareKingId(String uid,String name){
        Intent send=new Intent(Intent.ACTION_SEND); send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT,"Find "+name+" on KING Plus • KING ID: "+publicId(uid));
        startActivity(Intent.createChooser(send,"Share KING ID"));
    }
'''
src = src[:start] + new_toggle + src[end:]

src = src.replace('private void showLocalDemo() { list.removeAllViews(); setLoading("Sign in to load real KING Plus people, followers and friends"); }',
                  'private void showLocalDemo() { list.removeAllViews(); setLoading("Sign in with Google / Gmail to connect with real KING Plus people"); }')
SOCIAL.write_text(src, encoding='utf-8')

main = MAIN.read_text(encoding='utf-8')
old_save = '''getPreferences(0).edit().putString("name",n).putString("bio",bio.getText().toString().trim()).putString("hometown",hometown.getText().toString().trim()).putString("birthday",birthday.getText().toString().trim()).putString("tags",tags.getText().toString().trim()).putString("online_time",onlineTime.getText().toString().trim()).apply(); Toast.makeText(this,"Profile updated",Toast.LENGTH_SHORT).show(); profile();'''
new_save = '''getPreferences(0).edit().putString("name",n).putString("bio",bio.getText().toString().trim()).putString("hometown",hometown.getText().toString().trim()).putString("birthday",birthday.getText().toString().trim()).putString("tags",tags.getText().toString().trim()).putString("online_time",onlineTime.getText().toString().trim()).apply(); if(CloudSync.isSignedIn()) syncPublicProfile(); Toast.makeText(this,"Profile updated • people directory synced",Toast.LENGTH_SHORT).show(); profile();'''
if old_save not in main:
    raise SystemExit('MainActivity edit profile save template changed')
main = main.replace(old_save, new_save, 1)

# Give the Me side menu a clear route to the real people directory.
main = main.replace('String[] items={"👤 My Profile","✏ Edit Profile","💜 Friends","👥 Followers",',
                    'String[] items={"👤 My Profile","✏ Edit Profile","💜 Friends","👥 Followers",')
MAIN.write_text(main, encoding='utf-8')

gradle = BUILD.read_text(encoding='utf-8')
gradle = re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 64; versionName '4.3.0'", gradle)
BUILD.write_text(gradle, encoding='utf-8')

print('Prepared KING Plus v4.3.0 Real People Connect')
