from pathlib import Path
import sys
root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
(pkg/'KingLudoLobbyActivity.java').write_text(Path(__file__).with_name('KingLudoLobbyActivity.java').read_text())
(pkg/'KingVipVisualActivity.java').write_text(Path(__file__).with_name('KingVipVisualActivity.java').read_text())
(pkg/'KingGiftArtView.java').write_text(Path(__file__).with_name('KingGiftArtView.java').read_text())
(pkg/'KingRoomCoverView.java').write_text(Path(__file__).with_name('KingRoomCoverView.java').read_text())

manifest=root/'app/src/main/AndroidManifest.xml'
m=manifest.read_text()
needle='''        <activity android:name=".OnlineLudoActivity" android:exported="false" />
'''
if needle not in m: raise SystemExit('manifest Ludo marker missing')
m=m.replace(needle,'''        <activity android:name=".KingLudoLobbyActivity" android:exported="false" />
        <activity android:name=".KingVipVisualActivity" android:exported="false" />
'''+needle,1)
manifest.write_text(m)

main=pkg/'MainActivity.java'
s=main.read_text()
old='''    private void openPlayableGame(String game){if("ludo".equalsIgnoreCase(game)){startActivity(new Intent(this,OnlineLudoActivity.class));return;}Intent i=new Intent(this,GamePlayActivity.class);i.putExtra("game",game);startActivity(i);}
'''
new='''    private void openPlayableGame(String game){if("ludo".equalsIgnoreCase(game)){startActivity(new Intent(this,KingLudoLobbyActivity.class));return;}Intent i=new Intent(this,GamePlayActivity.class);i.putExtra("game",game);startActivity(i);}
'''
if old not in s: raise SystemExit('Main ludo route missing')
s=s.replace(old,new,1)

# Bolo-like Me drawer order/spacing while keeping KING-only privilege pages accessible.
old='''        String[] items={"Community Center","Square / Moments","Privilege Pack","Privilege Shop","Vip Level","Noble Center","Family","Relationship / CP","User level center","Wallet","Invite Friends","Settings","Help Center","Rules and Policies"};
        String[] icons={"square","square","privilege_pack","privilege_shop","vip_level","data_center","family","family","user_level_center","wallet","invite_friends","settings","help_centre","rules_policies"};
        Runnable[] actions={()->openCommunityHub700("Search"),()->openCommunityHub700("Moments"),()->openDeepFlow810("privilege_pack"),()->openDeepFlow810("privilege_shop"),()->openDeepFlow810("vip"),()->openDeepFlow810("noble"),()->openCommunityHub700("Family"),()->openCommunityHub700("Relationship"),()->openDeepFlow810("level_center"),()->openDeepFlow810("wallet"),()->shareText("Join me on KING Plus. My ID: "+(firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null?publicId(firebaseAuth.getCurrentUser().getUid()):displayName)),()->openDeepFlow810("settings"),()->openDeepFlow810("help"),()->openDeepFlow810("rules")};
'''
new='''        String[] items={"Square","Privilege Pack","Privilege Shop","Vip Level","Noble Center","Family","User level center","Wallet","Invite Friends","Settings","Help Center","Rules and Policies"};
        String[] icons={"square","privilege_pack","privilege_shop","vip_level","data_center","family","user_level_center","wallet","invite_friends","settings","help_centre","rules_policies"};
        Runnable[] actions={()->openCommunityHub700("Moments"),()->openDeepFlow810("privilege_pack"),()->openDeepFlow810("privilege_shop"),()->startActivity(new Intent(this,KingVipVisualActivity.class)),()->openDeepFlow810("noble"),()->openCommunityHub700("Family"),()->openDeepFlow810("level_center"),()->openDeepFlow810("wallet"),()->shareText("Join me on KING Plus. My ID: "+(firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null?publicId(firebaseAuth.getCurrentUser().getUid()):displayName)),()->openDeepFlow810("settings"),()->openDeepFlow810("help"),()->openDeepFlow810("rules")};
'''
if old not in s: raise SystemExit('drawer marker missing')
s=s.replace(old,new,1)
main.write_text(s)

deep=pkg/'KingDeepFlowActivity.java'
s=deep.read_text()
s=s.replace('''case "vip": vipCenter();break;''','''case "vip": startActivity(new Intent(this,KingVipVisualActivity.class));finish();break;''',1)
old='''    private void openGame(String code){Intent i=new Intent(this,GamePlayActivity.class);i.putExtra("game",code);startActivity(i);}
'''
new='''    private void openGame(String code){if("ludo".equalsIgnoreCase(code)){startActivity(new Intent(this,KingLudoLobbyActivity.class));return;}Intent i=new Intent(this,GamePlayActivity.class);i.putExtra("game",code);startActivity(i);}
'''
if old not in s: raise SystemExit('deep openGame marker missing')
s=s.replace(old,new,1)
deep.write_text(s)

gp=pkg/'GamePlayActivity.java'
s=gp.read_text()
s=s.replace('''else if("ludo".equals(game)){startActivity(new android.content.Intent(this,OnlineLudoActivity.class));finish();}''','''else if("ludo".equals(game)){startActivity(new android.content.Intent(this,KingLudoLobbyActivity.class));finish();}''',1)
gp.write_text(s)

party=pkg/'PartyActivity.java'
s=party.read_text()
old='''            TextView icon=tv(unlocked?giftIcons[i]:"🔒",36,unlocked?Color.WHITE:0xff77727e,false);icon.setGravity(Gravity.CENTER);card.addView(icon,new LinearLayout.LayoutParams(-1,dp(45)));
'''
new='''            View icon;if(unlocked){icon=new KingGiftArtView(this,idx,giftNames[i]);}else{TextView locked=tv("🔒",31,0xff77727e,false);locked.setGravity(Gravity.CENTER);icon=locked;}card.addView(icon,new LinearLayout.LayoutParams(-1,dp(45)));
'''
if old not in s: raise SystemExit('gift icon marker missing')
s=s.replace(old,new,1)

old='''                fillEmojiGrid610(grid,pack,k==2||k==3?6:5,k<2);
                for(int n=0;n<tabs.getChildCount();n++){View t=tabs.getChildAt(n);t.setBackgroundColor(n==k?0xfffff4a3:Color.WHITE);}
'''
new='''                fillEmojiGrid610(grid,pack,k==2||k==3?6:5,k<2);
                if(k==1||k==2){TextView vipPack=tv("👑 VIP Exclusive Stickers  •  KING original pack",12,0xff765d00,true);vipPack.setGravity(Gravity.CENTER_VERTICAL);vipPack.setPadding(dp(10),0,dp(10),0);vipPack.setBackground(bg(0xfffff3c4,10));grid.addView(vipPack,0,new LinearLayout.LayoutParams(-1,dp(42)));}
                for(int n=0;n<tabs.getChildCount();n++){View t=tabs.getChildAt(n);t.setBackgroundColor(n==k?0xfffff4a3:Color.WHITE);}
'''
if old not in s: raise SystemExit('emoji tab marker missing')
s=s.replace(old,new,1)
party.write_text(s)


# ---------------- Discover visual parity: profile grid first, ranks still available ----------------
discover=pkg/'DiscoverActivity.java'
d=discover.read_text()
old='''        addRankingHero();
        addSmallRankingRow();
        sectionTitle("Activity recommendation","Activity Hall",this::openActivityHall);
        loadActivityRecommendations();
        sectionTitle("Game Master","More",()->showRankingPage("Game Master","gameScore"));
        loadTalentGrid("gameScore",false);
        sectionTitle("Chat Talents","More",()->showRankingPage("Chat Talent","chatScore"));
        loadTalentGrid("chatScore",true);
        addPeopleTabs();
        loadRecommended(false);
'''
new='''        addMatchTabs940();
        loadProfileGrid940();
        sectionTitle("Ranks & influence","More",()->showRankingPage("Chat Talent","chatScore"));
        addSmallRankingRow();
        sectionTitle("Live recommendations","Activity Hall",this::openActivityHall);
        loadActivityRecommendations();
        sectionTitle("Game Master","More",()->showRankingPage("Game Master","gameScore"));
        loadTalentGrid("gameScore",false);
'''
if old not in d: raise SystemExit('discover render marker missing')
d=d.replace(old,new,1)

marker='''    private void addRankingHero(){
'''
helpers='''    private void addMatchTabs940(){
        LinearLayout tabs=new LinearLayout(this);tabs.setGravity(Gravity.CENTER_VERTICAL);tabs.setPadding(0,dp(2),0,dp(10));
        String[] n={"Hot","New","Nearby","Follow"};for(int i=0;i<n.length;i++){final String label=n[i];TextView t=tv(label,14,i==0?0xff111111:0xff77737d,true);t.setGravity(Gravity.CENTER);t.setBackground(bg(i==0?0xffffe91f:0xfff5f4f7,12));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(44),1);p.setMargins(dp(3),0,dp(3),0);tabs.addView(t,p);t.setOnClickListener(v->{activePeopleTab=label;loadProfileGrid940();});}
        content.addView(tabs,new LinearLayout.LayoutParams(-1,dp(54)));
    }
    private void loadProfileGrid940(){
        Object old=content.findViewWithTag("profile_grid_940");LinearLayout grid;if(old instanceof LinearLayout){grid=(LinearLayout)old;grid.removeAllViews();}else{grid=new LinearLayout(this);grid.setTag("profile_grid_940");grid.setOrientation(LinearLayout.VERTICAL);content.addView(grid,new LinearLayout.LayoutParams(-1,-2));}
        if(me==null||db==null){grid.addView(emptyText("Sign in to discover KING profiles"),new LinearLayout.LayoutParams(-1,dp(90)));return;}
        Query q=db.collection("public_profiles").limit(24);String city=getSharedPreferences("discover_public",MODE_PRIVATE).getString("city","");
        if("Nearby".equals(activePeopleTab)&&!city.isEmpty())q=db.collection("public_profiles").whereEqualTo("hometown",city).limit(24);
        q.get().addOnSuccessListener(snap->{grid.removeAllViews();LinearLayout row=null;int shown=0;for(DocumentSnapshot p:snap.getDocuments()){String uid=safe(p.getString("uid"),p.getId());if(uid.equals(me.getUid()))continue;if(shown%2==0){row=new LinearLayout(this);row.setGravity(Gravity.TOP);grid.addView(row,new LinearLayout.LayoutParams(-1,dp(222)));}addProfileCard940(row,p);shown++;if(shown>=8)break;}if(shown==0)grid.addView(emptyText("No matching KING profiles yet"),new LinearLayout.LayoutParams(-1,dp(90)));}).addOnFailureListener(e->{grid.removeAllViews();grid.addView(emptyText("Profile discovery unavailable"),new LinearLayout.LayoutParams(-1,dp(90)));});
    }
    private void addProfileCard940(LinearLayout row,DocumentSnapshot p){
        String uid=safe(p.getString("uid"),p.getId()),name=nameOf(p);LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(5),dp(5),dp(5),dp(7));card.setBackground(bg(Color.WHITE,14));
        android.widget.FrameLayout hero=new android.widget.FrameLayout(this);TextView fallback=tv(initial(name),40,Color.WHITE,true);fallback.setGravity(Gravity.CENTER);fallback.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xffef9aa9,0xff7f5cf1}));hero.addView(fallback,new android.widget.FrameLayout.LayoutParams(-1,-1));String photo=p.getString("photoUrl");if(photo!=null&&!photo.trim().isEmpty()){android.widget.ImageView im=new android.widget.ImageView(this);im.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);hero.addView(im,new android.widget.FrameLayout.LayoutParams(-1,-1));loadProfileImage940(im,fallback,photo);}card.addView(hero,new LinearLayout.LayoutParams(-1,dp(132)));
        TextView nm=tv(shortName(name),14,DARK,true);nm.setGravity(Gravity.CENTER_VERTICAL);nm.setSingleLine(true);card.addView(nm,new LinearLayout.LayoutParams(-1,dp(30)));TextView id=tv("KING ID "+publicId(uid),10,MUTED,false);card.addView(id,new LinearLayout.LayoutParams(-1,dp(22)));
        TextView chat=tv("● Chat",12,0xff126d50,true);chat.setGravity(Gravity.CENTER);chat.setBackground(bg(0xffd9f7ea,12));chat.setOnClickListener(v->openProfile(uid));card.addView(chat,new LinearLayout.LayoutParams(-1,dp(32)));card.setOnClickListener(v->openProfile(uid));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(214),1);cp.setMargins(dp(4),dp(4),dp(4),dp(4));row.addView(card,cp);
    }
    private void loadProfileImage940(android.widget.ImageView image,TextView fallback,String url){
        final String source=url;image.setTag(source);new Thread(()->{try{java.net.URLConnection c=new java.net.URL(source).openConnection();c.setConnectTimeout(7000);c.setReadTimeout(7000);try(java.io.InputStream in=c.getInputStream()){android.graphics.Bitmap bm=android.graphics.BitmapFactory.decodeStream(in);if(bm!=null)runOnUiThread(()->{if(source.equals(image.getTag())){image.setImageBitmap(bm);fallback.setVisibility(View.GONE);}});}}catch(Exception ignored){}}).start();
    }

'''
if marker not in d: raise SystemExit('discover helper insert marker missing')
d=d.replace(marker,helpers+marker,1)
discover.write_text(d)

# ---------------- Messages visual parity: recent room friends + system channels ----------------
inbox=pkg/'InboxActivity.java'
d=inbox.read_text()
old='''        LinearLayout quick = new LinearLayout(this); quick.setGravity(Gravity.CENTER); quick.setPadding(dp(12),dp(2),dp(12),dp(8));
        addQuick(quick,"👤","Followers",()->openSocial());
        addQuick(quick,"👥","Friends",()->openSocial());
        addQuick(quick,"🔔","Notifications",this::showNotifications);
        root.addView(quick,new LinearLayout.LayoutParams(-1,dp(92)));

        View dividerTop = new View(this); dividerTop.setBackgroundColor(0xffeeecef); root.addView(dividerTop,new LinearLayout.LayoutParams(-1,dp(1)));
'''
new='''        TextView recentTitle=label("Recent friends in room",13,0xff5f5964,true);recentTitle.setPadding(dp(18),0,0,0);root.addView(recentTitle,new LinearLayout.LayoutParams(-1,dp(34)));
        root.addView(recentFriends940(),new LinearLayout.LayoutParams(-1,dp(78)));
        View dividerTop = new View(this); dividerTop.setBackgroundColor(0xffeeecef); root.addView(dividerTop,new LinearLayout.LayoutParams(-1,dp(1)));
'''
if old not in d: raise SystemExit('inbox quick marker missing')
d=d.replace(old,new,1)

old='''    private void renderModels(List<ThreadModel> source){
        if(list==null)return; list.removeAllViews();
        if(source==null||source.isEmpty()){
'''
new='''    private void renderModels(List<ThreadModel> source){
        if(list==null)return; list.removeAllViews();addSystemRows940();
        if(source==null||source.isEmpty()){
'''
if old not in d: raise SystemExit('inbox renderModels marker missing')
d=d.replace(old,new,1)

marker='''    private void addQuick(LinearLayout host,String icon,String text,Runnable action){
'''
helpers='''    private View recentFriends940(){
        android.widget.HorizontalScrollView sc=new android.widget.HorizontalScrollView(this);sc.setHorizontalScrollBarEnabled(false);LinearLayout row=new LinearLayout(this);row.setPadding(dp(12),0,dp(12),dp(6));Set<String> names=getSharedPreferences("chat_store",MODE_PRIVATE).getStringSet("recent_names",new HashSet<>());int count=0;for(String n:names){if(n==null||n.trim().isEmpty())continue;addRecentFriend940(row,n);if(++count>=6)break;}while(count<5){addRecentFriend940(row,count==0?"KING":("Friend "+(count+1)));count++;}sc.addView(row);return sc;
    }
    private void addRecentFriend940(LinearLayout row,String name){LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setGravity(Gravity.CENTER);TextView av=label(initial(name),17,Color.WHITE,true);av.setGravity(Gravity.CENTER);av.setBackground(bg(0xff9a75e8,25));box.addView(av,new LinearLayout.LayoutParams(dp(48),dp(48)));TextView nm=label(name,9,0xff6b6570,false);nm.setGravity(Gravity.CENTER);nm.setSingleLine(true);box.addView(nm,new LinearLayout.LayoutParams(dp(68),dp(20)));box.setOnClickListener(v->openChat(name,""));row.addView(box,new LinearLayout.LayoutParams(dp(74),dp(72)));}
    private void addSystemRows940(){addSystemRow940("🔔","Interactive notifications","Follows, gifts and room activity",this::showNotifications);addSystemRow940("👑","KING Official","Safety, events and app notices",this::showNotifications);addSystemRow940("🎤","Room invitations","Party and game invitations",this::showNotifications);}
    private void addSystemRow940(String icon,String title,String sub,Runnable action){LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(16),dp(8),dp(14),dp(8));row.setBackgroundColor(Color.WHITE);TextView av=label(icon,23,DARK,false);av.setGravity(Gravity.CENTER);av.setBackground(bg(0xfffff1d6,27));row.addView(av,new LinearLayout.LayoutParams(dp(52),dp(52)));LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);info.setPadding(dp(12),0,0,0);info.addView(label(title,15,DARK,true),new LinearLayout.LayoutParams(-1,dp(28)));TextView sb=label(sub,12,MUTED,false);sb.setSingleLine(true);info.addView(sb,new LinearLayout.LayoutParams(-1,dp(24)));row.addView(info,new LinearLayout.LayoutParams(0,dp(52),1));TextView ar=label("›",24,0xffaaa4ad,false);ar.setGravity(Gravity.CENTER);row.addView(ar,new LinearLayout.LayoutParams(dp(34),dp(52)));row.setOnClickListener(v->action.run());list.addView(row,new LinearLayout.LayoutParams(-1,dp(70)));}
'''
if marker not in d: raise SystemExit('inbox helper marker missing')
d=d.replace(marker,helpers+marker,1)
inbox.write_text(d)


# ---------------- Party lobby room-card visual parity ----------------
party=pkg/'PartyActivity.java'
q=party.read_text()
old='''        FrameLayout cover=new FrameLayout(this);cover.setBackground(bg(cardColor,10));
        ImageView img=new ImageView(this);img.setScaleType(ImageView.ScaleType.CENTER_CROP);cover.addView(img,new FrameLayout.LayoutParams(-1,-1));
        TextView initial=tv(roomAvatarText(hostName,category),34,0xff4f3f4e,true);initial.setGravity(Gravity.CENTER);initial.setBackground(bg(0x33ffffff,0));cover.addView(initial,new FrameLayout.LayoutParams(-1,-1));
        if(photoUrl!=null&&!photoUrl.trim().isEmpty())loadProfilePhoto(img,initial,photoUrl.trim());
'''
new='''        FrameLayout cover=new FrameLayout(this);cover.setBackground(bg(cardColor,10));
        KingRoomCoverView art940=new KingRoomCoverView(this,category,name);cover.addView(art940,new FrameLayout.LayoutParams(-1,-1));
        ImageView img=new ImageView(this);img.setScaleType(ImageView.ScaleType.CENTER_CROP);cover.addView(img,new FrameLayout.LayoutParams(-1,-1));
        TextView initial=tv("",1,Color.TRANSPARENT,false);initial.setVisibility(View.GONE);cover.addView(initial,new FrameLayout.LayoutParams(1,1));
        if(photoUrl!=null&&!photoUrl.trim().isEmpty())loadProfilePhoto(img,initial,photoUrl.trim());
'''
if old not in q: raise SystemExit('room card cover marker missing')
q=q.replace(old,new,1)
party.write_text(q)

print('v9.4.0 parity batch 1 applied')
