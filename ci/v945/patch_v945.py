from pathlib import Path
import sys,re
root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'

# ---------- Social / People: network recovery + retryable states ----------
p=pkg/'SocialActivity.java';s=p.read_text()
old='''    private TextView status;'''
new='''    private TextView status;
    private android.net.ConnectivityManager.NetworkCallback socialNetworkCallback945;
    private boolean socialOnline945=true;'''
if old not in s: raise SystemExit('Social status field marker')
s=s.replace(old,new,1)

old='''        if (cloudReady()) {
            publishOwnProfile();
            loadDiscover();
        } else {
            status.setText("Local/test mode • sign in with Firebase for live people search");
            showLocalDemo();
        }
    }'''
new='''        if (cloudReady()) {
            publishOwnProfile();
            loadDiscover();
        } else {
            status.setText("Local/test mode • sign in with Firebase for live people search");
            showLocalDemo();
        }
        socialOnline945=KingNetwork.online(this);
        socialNetworkCallback945=KingNetwork.watch(this,online->runOnUiThread(()->{
            boolean recovered=online&&!socialOnline945;socialOnline945=online;
            if(isFinishing()||isDestroyed())return;
            if(!online){status.setText("Offline • People will reconnect automatically");showRetry945("Offline • tap Retry when connection returns",this::loadDiscover);return;}
            if(recovered&&cloudReady()){status.setText("Back online • refreshing people…");publishOwnProfile();loadDiscover();}
        }));
    }'''
if old not in s: raise SystemExit('Social onCreate tail marker')
s=s.replace(old,new,1)

marker='''    private void publishOwnProfile() {'''
helper='''    private void showRetry945(String text,Runnable retry){
        if(list==null)return;list.removeAllViews();
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(14),dp(14),dp(14),dp(14));card.setBackground(bg(Color.WHITE,16));
        TextView msg=label(text,13,MUTED,true);msg.setGravity(Gravity.CENTER);card.addView(msg,new LinearLayout.LayoutParams(-1,dp(48)));
        TextView again=label("↻ Retry",14,Color.WHITE,true);again.setGravity(Gravity.CENTER);again.setBackground(bg(PURPLE,14));again.setOnClickListener(v->KingSafe.run(this,"social-retry",retry));LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(dp(128),dp(44));ap.setMargins(0,dp(6),0,0);card.addView(again,ap);
        list.addView(card,new LinearLayout.LayoutParams(-1,dp(118)));
    }

'''
if marker not in s: raise SystemExit('Social publish marker')
s=s.replace(marker,helper+marker,1)

repls=[
('''.addOnFailureListener(e -> { setLoading("ID search unavailable"); toast(safe(e.getLocalizedMessage())); });''',
 '''.addOnFailureListener(e -> showRetry945("ID search unavailable",this::runSearch));'''),
('''.addOnFailureListener(e -> { setLoading("Search unavailable"); toast(safe(e.getLocalizedMessage())); });''',
 '''.addOnFailureListener(e -> showRetry945("Search unavailable",this::runSearch));'''),
('''.addOnFailureListener(e -> { setLoading("People directory unavailable"); toast(safe(e.getLocalizedMessage())); });''',
 '''.addOnFailureListener(e -> showRetry945("People directory unavailable",this::loadDiscover));'''),
('''}).addOnFailureListener(e->{setLoading("Could not load following");toast(safe(e.getLocalizedMessage()));});''',
 '''}).addOnFailureListener(e->showRetry945("Could not load following",this::loadFollowing));'''),
('''}).addOnFailureListener(e->{setLoading("Could not load followers");toast(safe(e.getLocalizedMessage()));});''',
 '''}).addOnFailureListener(e->showRetry945("Could not load followers",this::loadFollowers));'''),
('''}).addOnFailureListener(e->{setLoading("Could not load friends");toast(safe(e.getLocalizedMessage()));});''',
 '''}).addOnFailureListener(e->showRetry945("Could not load friends",this::loadFriends));''')
]
for a,b in repls:
    if a in s:s=s.replace(a,b,1)

# append lifecycle cleanup before final class brace
pos=s.rfind('}')
if pos<0: raise SystemExit('Social final brace')
s=s[:pos]+'''    @Override protected void onDestroy(){KingNetwork.unwatch(this,socialNetworkCallback945);socialNetworkCallback945=null;super.onDestroy();}\n'''+s[pos:]
p.write_text(s)

# ---------- Public Profile: visible loading + retry ----------
p=pkg/'KingPublicProfileActivity.java';s=p.read_text()
old='''    private void load(){body.removeAllViews();if(uid.isEmpty()){body.addView(tv("Profile unavailable",16,MUTED,true));return;}if(db==null){body.addView(tv("Connect to Firebase to load this profile",15,MUTED,false));return;}db.collection("public_profiles").document(uid).get().addOnSuccessListener(this::showProfile).addOnFailureListener(e->body.addView(tv("Could not load profile: "+safe(e.getMessage()),14,MUTED,false)));}'''
new='''    private void load(){
        body.removeAllViews();
        if(uid.isEmpty()){body.addView(tv("Profile unavailable",16,MUTED,true));return;}
        if(db==null){profileRetry945("Connect to Firebase to load this profile");return;}
        TextView loading=tv("Loading KING profile…",14,MUTED,true);loading.setGravity(Gravity.CENTER);body.addView(loading,new LinearLayout.LayoutParams(-1,dp(86)));
        db.collection("public_profiles").document(uid).get().addOnSuccessListener(this::showProfile).addOnFailureListener(e->profileRetry945("Could not load profile • "+safe(e.getMessage())));
    }
    private void profileRetry945(String message){
        body.removeAllViews();TextView msg=tv(message,14,MUTED,true);msg.setGravity(Gravity.CENTER);body.addView(msg,new LinearLayout.LayoutParams(-1,dp(72)));
        TextView retry=action("↻ Retry Profile");retry.setOnClickListener(v->KingSafe.run(this,"public-profile-retry",this::load));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(48));p.setMargins(0,dp(6),0,0);body.addView(retry,p);
    }'''
if old not in s: raise SystemExit('PublicProfile load marker')
s=s.replace(old,new,1)
p.write_text(s)

# ---------- Community / Family: retryable public data operations ----------
p=pkg/'CommunityHubActivity.java';s=p.read_text()
marker='''    private String publicId(String uid){if(uid==null)return"000000";return String.valueOf(Integer.toUnsignedLong(uid.hashCode())%900000L+100000L);}'''
helper=marker+'''
    private void communityRetry945(String message,Runnable retry){
        if(body==null)return;TextView state=tv(message+"   •   ↻ Retry",13,MUTED,true);state.setGravity(Gravity.CENTER);state.setBackground(bg(CARD,14));state.setOnClickListener(v->KingSafe.run(this,"community-retry",retry));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(58));p.setMargins(0,dp(8),0,dp(8));body.addView(state,p);
    }'''
if marker not in s: raise SystemExit('Community publicId marker')
s=s.replace(marker,helper,1)

s=s.replace('''.addOnFailureListener(e->toast("Search unavailable"));return;''',
            '''.addOnFailureListener(e->communityRetry945("People search unavailable",()->searchPeople(raw)));return;''',1)
s=s.replace('''.addOnFailureListener(e->toast("Search unavailable")));}''',
            '''.addOnFailureListener(e->communityRetry945("People search unavailable",()->searchPeople(raw))));}''',1)
s=s.replace('''}).addOnFailureListener(e->toast("Moments unavailable"));''',
            '''}).addOnFailureListener(e->communityRetry945("Moments unavailable",this::loadMoments));''',1)

# Add failure path to searchRooms method by editing only that method.
a=s.find('    private void searchRooms(String raw)')
b=s.find('    private void moments()',a)
if a<0 or b<0: raise SystemExit('Community searchRooms boundary')
part=s[a:b]
if '.addOnFailureListener' not in part:
    end=part.rfind('});}')
    if end<0: raise SystemExit('Community searchRooms completion')
    part=part[:end]+'''}).addOnFailureListener(e->communityRetry945("Room search unavailable",()->searchRooms(raw)));}'''+part[end+4:]
    s=s[:a]+part+s[b:]

# Family root lookup retry.
a=s.find('    private void showFamily(String code)')
b=s.find('    private int familyBagReward940',a)
if a>=0 and b>a:
    part=s[a:b]
    if 'Family unavailable' not in part:
        end=part.rfind('});}')
        if end>=0:
            part=part[:end]+'''}).addOnFailureListener(e->communityRetry945("Family unavailable",()->showFamily(code)));}'''+part[end+4:]
            s=s[:a]+part+s[b:]
p.write_text(s)

# ---------- VIP: turn presentation-only controls into useful navigation ----------
p=pkg/'KingVipVisualActivity.java';s=p.read_text()
old='''TextView info=tv("ⓘ",20,Color.WHITE,false);info.setGravity(Gravity.CENTER);head.addView(info,new LinearLayout.LayoutParams(dp(48),dp(54)));'''
new='''TextView info=tv("ⓘ",20,Color.WHITE,false);info.setGravity(Gravity.CENTER);info.setOnClickListener(v->new android.app.AlertDialog.Builder(this).setTitle("KING Plus VIP").setMessage("VIP progression is activity/test-point based in this no-billing build. Frames, badges and entrance effects are managed in Wardrobe.").setPositiveButton("OK",null).show());head.addView(info,new LinearLayout.LayoutParams(dp(48),dp(54)));'''
if old not in s: raise SystemExit('VIP info marker')
s=s.replace(old,new,1)

old='''LinearLayout rewards=new LinearLayout(this);String[] ri={"🎁","👑","🖼","✨"};String[] rn={"Gift","Badge","Frame","Entrance"};for(int i=0;i<4;i++){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setGravity(Gravity.CENTER);x.setBackground(grad(0x332a91e8,0x22111122,12));x.addView(tv(ri[i],28,Color.WHITE,false),new LinearLayout.LayoutParams(-1,dp(48)));TextView n=tv(rn[i],10,0xffd6d6e5,true);n.setGravity(Gravity.CENTER);x.addView(n,new LinearLayout.LayoutParams(-1,dp(28)));LinearLayout.LayoutParams xp=new LinearLayout.LayoutParams(0,dp(82),1);xp.setMargins(dp(3),0,dp(3),0);rewards.addView(x,xp);}body.addView(rewards,new LinearLayout.LayoutParams(-1,dp(86)));'''
new='''LinearLayout rewards=new LinearLayout(this);String[] ri={"🎁","👑","🖼","✨"};String[] rn={"Gift","Badge","Frame","Entrance"};for(int i=0;i<4;i++){final int idx=i;LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setGravity(Gravity.CENTER);x.setBackground(grad(0x332a91e8,0x22111122,12));x.addView(tv(ri[i],28,Color.WHITE,false),new LinearLayout.LayoutParams(-1,dp(48)));TextView n=tv(rn[i],10,0xffd6d6e5,true);n.setGravity(Gravity.CENTER);x.addView(n,new LinearLayout.LayoutParams(-1,dp(28)));x.setOnClickListener(v->{if(idx==0){android.content.Intent q=new android.content.Intent(this,PartyActivity.class);q.putExtra("requestedPanel","gift");startActivity(q);}else startActivity(new android.content.Intent(this,KingWardrobeActivity.class));});LinearLayout.LayoutParams xp=new LinearLayout.LayoutParams(0,dp(82),1);xp.setMargins(dp(3),0,dp(3),0);rewards.addView(x,xp);}body.addView(rewards,new LinearLayout.LayoutParams(-1,dp(86)));'''
if old not in s: raise SystemExit('VIP rewards marker')
s=s.replace(old,new,1)

old='''TextView more=tv("See More  ›",12,0xffcbd8ff,true);more.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);body.addView(more,new LinearLayout.LayoutParams(-1,dp(42)));'''
new='''TextView more=tv("See More  ›",12,0xffcbd8ff,true);more.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);more.setOnClickListener(v->startActivity(new android.content.Intent(this,KingWardrobeActivity.class)));body.addView(more,new LinearLayout.LayoutParams(-1,dp(42)));'''
if old not in s: raise SystemExit('VIP see more marker')
s=s.replace(old,new,1)
p.write_text(s)

# Version bump.
g=root/'app/build.gradle';x=g.read_text();old="versionCode 143; versionName '9.4.4-party-video-social'"
if old not in x: raise SystemExit('v9.4.4 version marker')
g.write_text(x.replace(old,"versionCode 144; versionName '9.4.5-social-profile-runtime'",1))

(root/'V9.4.5-WORKLOG.md').write_text('''# KING Plus v9.4.5 social/profile runtime batch
- People directory watches connectivity and refreshes after network recovery.
- Search, Discover, Following, Followers and Friends expose actionable Retry cards.
- Public profiles display loading and a Retry Profile action on failure.
- Community people/room search and Moments expose retry states.
- Family root load exposes Retry on failure.
- VIP info is actionable; reward tiles and See More route into Gift/Party or Wardrobe.
''')
print('v9.4.5 social/profile runtime patch applied')
