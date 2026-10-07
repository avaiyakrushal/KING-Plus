from pathlib import Path
import sys
root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
(pkg/'KingLudoLobbyActivity.java').write_text(Path(__file__).with_name('KingLudoLobbyActivity.java').read_text())
(pkg/'KingVipVisualActivity.java').write_text(Path(__file__).with_name('KingVipVisualActivity.java').read_text())
(pkg/'KingGiftArtView.java').write_text(Path(__file__).with_name('KingGiftArtView.java').read_text())
(pkg/'KingRoomCoverView.java').write_text(Path(__file__).with_name('KingRoomCoverView.java').read_text())
(pkg/'KingGameArtView.java').write_text(Path(__file__).with_name('KingGameArtView.java').read_text())
(pkg/'KingLoginBackdropView.java').write_text(Path(__file__).with_name('KingLoginBackdropView.java').read_text())
(pkg/'KingWalletVisualActivity.java').write_text(Path(__file__).with_name('KingWalletVisualActivity.java').read_text())

manifest=root/'app/src/main/AndroidManifest.xml'
m=manifest.read_text()
needle='''        <activity android:name=".OnlineLudoActivity" android:exported="false" />
'''
if needle not in m: raise SystemExit('manifest Ludo marker missing')
m=m.replace(needle,'''        <activity android:name=".KingLudoLobbyActivity" android:exported="false" />
        <activity android:name=".KingVipVisualActivity" android:exported="false" />
        <activity android:name=".KingWalletVisualActivity" android:exported="false" />
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


# ---------------- KTV / PK / Family / Rank presentation parity ----------------
parity=pkg/'KingParityHubActivity.java'
q=parity.read_text()
old='''    private void hero(String a,String b){TextView h=tv(a+"\\n"+b,18,Color.WHITE,true);h.setGravity(Gravity.CENTER_VERTICAL);h.setBackground(bg(CARD,18));h.setPadding(dp(18),dp(16),dp(18),dp(16));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(12));body.addView(h,lp);}
'''
new='''    private void hero(String a,String b){TextView h=tv(a+"\\n"+b,18,Color.WHITE,true);h.setGravity(Gravity.CENTER_VERTICAL);h.setBackground(bg(CARD,18));h.setPadding(dp(18),dp(16),dp(18),dp(16));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(8));body.addView(h,lp);KingPresentationView visual=new KingPresentationView(this,route);LinearLayout.LayoutParams vp=new LinearLayout.LayoutParams(-1,dp(158));vp.setMargins(0,0,0,dp(12));body.addView(visual,vp);}
'''
if old not in q: raise SystemExit('parity hero marker missing')
q=q.replace(old,new,1)
old='''    private void openFamily(){openPro880("family");}
'''
new='''    private void openFamily(){hero("👑 Family Square","Family level • treasury • members • activity • family voice");card("👑","Open Family Pro","Family progress, treasury, member list and daily activity",()->openPro880("family"));card("💬","Family Chat","Open family community chat",()->{Intent i=new Intent(this,CommunityHubActivity.class);i.putExtra("tab","Family");startActivity(i);});card("🎙","Family Voice","Open Family Pro voice controls",()->openPro880("family"));}
'''
if old not in q: raise SystemExit('parity family marker missing')
q=q.replace(old,new,1)
old='''    private void vip(){hero("💎 VIP & Rank","VIP remains progression-based in this no-billing build. Levels come from real KING Plus activity.");'''
new='''    private void vip(){startActivity(new Intent(this,KingVipVisualActivity.class));finish();if(true)return;/* legacy parity fallback */} private void vipLegacy940(){hero("💎 VIP & Rank","VIP remains progression-based in this no-billing build. Levels come from real KING Plus activity.");'''
if old not in q: raise SystemExit('parity vip marker missing')
q=q.replace(old,new,1)
parity.write_text(q)

eco=pkg/'KingEcosystemProActivity.java'
q=eco.read_text()
old='''LinearLayout.LayoutParams vp=new LinearLayout.LayoutParams(-1,dp(126));vp.setMargins(0,0,0,dp(10));body.addView(visual,vp);'''
new='''LinearLayout.LayoutParams vp=new LinearLayout.LayoutParams(-1,dp(172));vp.setMargins(0,0,0,dp(12));body.addView(visual,vp);'''
if old not in q: raise SystemExit('ecosystem hero visual marker missing')
q=q.replace(old,new,1)
old='''private void rankRow(int n,String name,String score,String uid){boolean top=n<=3;String medal=n==1?"🥇":n==2?"🥈":n==3?"🥉":"#"+n;TextView r=tv(medal+"   "+name+"\\n      "+score,top?15:14,Color.WHITE,true);'''
new='''private void rankRow(int n,String name,String score,String uid){boolean top=n<=3;String medal=n==1?"🥇":n==2?"🥈":n==3?"🥉":"#"+n;TextView r=tv(medal+"   "+name+"\\n      "+score,top?17:14,Color.WHITE,true);'''
if old not in q: raise SystemExit('rank row marker missing')
q=q.replace(old,new,1)
eco.write_text(q)


# ---------------- Game home illustration parity ----------------
main=pkg/'MainActivity.java'
q=main.read_text()
old='''        LinearLayout hero=new LinearLayout(this);hero.setOrientation(LinearLayout.VERTICAL);hero.setPadding(dp(16),dp(14),dp(16),dp(12));hero.setBackground(background(0xffdff4ff,16));hero.setOnClickListener(v->openPlayableGame("ludo"));TextView title=new TextView(this);title.setText("🎲  LUDO LORD");title.setTextSize(28);title.setTypeface(null,Typeface.BOLD);title.setTextColor(0xfff0a400);title.setGravity(Gravity.CENTER);hero.addView(title,new LinearLayout.LayoutParams(-1,dp(46)));TextView sub=new TextView(this);sub.setText("The Christmas feature is online, tap to join now!");sub.setTextSize(11);sub.setTextColor(0xff687984);sub.setGravity(Gravity.CENTER);hero.addView(sub,new LinearLayout.LayoutParams(-1,dp(28)));LinearLayout quick=new LinearLayout(this);quick.setGravity(Gravity.CENTER);String[] q={"1 vs 1","ONLINE","SKIN","EVENTS"};for(String x:q){TextView b=new TextView(this);b.setText(x);b.setTextSize(11);b.setTypeface(null,Typeface.BOLD);b.setTextColor(0xff3e5460);b.setGravity(Gravity.CENTER);b.setBackground(background(Color.WHITE,9));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(46),1);lp.setMargins(dp(3),0,dp(3),0);quick.addView(b,lp);}hero.addView(quick,new LinearLayout.LayoutParams(-1,dp(50)));LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,dp(142));hp.setMargins(0,0,0,dp(12));host.addView(hero,hp);TextView best=new TextView(this);best.setText("Best Game Collections");best.setTextSize(15);best.setTypeface(null,Typeface.BOLD);best.setTextColor(0xff222222);best.setPadding(dp(5),dp(4),0,dp(7));host.addView(best,new LinearLayout.LayoutParams(-1,dp(38)));
'''
new='''        LinearLayout hero=new LinearLayout(this);hero.setOrientation(LinearLayout.VERTICAL);hero.setPadding(dp(8),dp(8),dp(8),dp(8));hero.setBackground(background(0xffdff4ff,16));hero.setOnClickListener(v->openPlayableGame("ludo"));KingGameArtView art940=new KingGameArtView(this,"ludo","LUDO LORD");hero.addView(art940,new LinearLayout.LayoutParams(-1,dp(108)));TextView sub=new TextView(this);sub.setText("LUDO LORD • Online • Team • Friends");sub.setTextSize(12);sub.setTextColor(0xff526c79);sub.setTypeface(null,Typeface.BOLD);sub.setGravity(Gravity.CENTER);hero.addView(sub,new LinearLayout.LayoutParams(-1,dp(28)));LinearLayout quick=new LinearLayout(this);quick.setGravity(Gravity.CENTER);String[] q={"1 vs 1","ONLINE","2VS2","EVENTS"};for(String x:q){TextView b=new TextView(this);b.setText(x);b.setTextSize(10);b.setTypeface(null,Typeface.BOLD);b.setTextColor(0xff3e5460);b.setGravity(Gravity.CENTER);b.setBackground(background(Color.WHITE,9));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(38),1);lp.setMargins(dp(3),0,dp(3),0);quick.addView(b,lp);}hero.addView(quick,new LinearLayout.LayoutParams(-1,dp(42)));LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,dp(190));hp.setMargins(0,0,0,dp(12));host.addView(hero,hp);TextView best=new TextView(this);best.setText("Best Game Collections");best.setTextSize(15);best.setTypeface(null,Typeface.BOLD);best.setTextColor(0xff222222);best.setPadding(dp(5),dp(4),0,dp(7));host.addView(best,new LinearLayout.LayoutParams(-1,dp(38)));
'''
if old not in q: raise SystemExit('Ludo hero marker missing')
q=q.replace(old,new,1)

old='''        TextView art=new TextView(this);art.setText(icon);art.setTextSize(43);art.setGravity(Gravity.CENTER);int[] palette={0xff39a7d8,0xffab5ed9,0xffed7b6b,0xff54b9a7,0xffd4a447};int tint=palette[(title.hashCode()&0x7fffffff)%palette.length];android.graphics.drawable.GradientDrawable artBg=new android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.TL_BR,new int[]{tint,0xfff2dffc});artBg.setCornerRadius(dp(14));art.setBackground(artBg);card.addView(art,new LinearLayout.LayoutParams(-1,dp(86)));
'''
new='''        KingGameArtView art=new KingGameArtView(this,playableCode730(game),title);card.addView(art,new LinearLayout.LayoutParams(-1,dp(86)));
'''
if old not in q: raise SystemExit('game card art marker missing')
q=q.replace(old,new,1)
main.write_text(q)


# ---------------- Login premium visual parity ----------------
main=pkg/'MainActivity.java'
q=main.read_text()
old='''        root.setBackgroundResource(R.drawable.login_background);
        ImageView diamonds = new ImageView(this);
        diamonds.setImageResource(R.drawable.diamond_art);
        diamonds.setAlpha(0.42f);
        android.widget.FrameLayout.LayoutParams art = new android.widget.FrameLayout.LayoutParams(dp(320), dp(320), Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        art.topMargin = dp(78);
        root.addView(diamonds, art);
        View shade = new View(this);
        shade.setBackgroundColor(0x55000015);
        root.addView(shade, new android.widget.FrameLayout.LayoutParams(-1, -1));
'''
new='''        root.setBackgroundColor(0xff0b1026);
        KingLoginBackdropView backdrop940=new KingLoginBackdropView(this);
        root.addView(backdrop940,new android.widget.FrameLayout.LayoutParams(-1,-1));
        View shade = new View(this);
        shade.setBackgroundColor(0x22000015);
        root.addView(shade, new android.widget.FrameLayout.LayoutParams(-1, -1));
'''
if old not in q: raise SystemExit('login backdrop marker missing')
q=q.replace(old,new,1)
old='''        TextView heading = text("KING Plus", 31, Color.WHITE, true);
        heading.setGravity(Gravity.CENTER);
        TextView subtitle = text("Login or create your account", 15, MUTED, false);
        subtitle.setGravity(Gravity.CENTER);
        text("Welcome", 23, Color.WHITE, true);
        text("Choose a sign-in method", 14, MUTED, false);
'''
new='''        TextView heading = text("KING Plus", 32, Color.WHITE, true);heading.setGravity(Gravity.CENTER);
        TextView subtitle = text("Voice • Party • Games • Friends", 14, 0xffe9dcff, true);subtitle.setGravity(Gravity.CENTER);
        TextView welcome940=text("Welcome to KING", 21, Color.WHITE, true);welcome940.setGravity(Gravity.CENTER);
        TextView choose940=text("Choose a secure sign-in method", 13, 0xffd6c8e4, false);choose940.setGravity(Gravity.CENTER);
'''
if old not in q: raise SystemExit('login heading marker missing')
q=q.replace(old,new,1)
main.write_text(q)


# ---------------- Wallet visual parity while retaining no-billing ----------------
deep=pkg/'KingDeepFlowActivity.java'
q=deep.read_text()
old="""    private void wallet(){int coins=mainPrefs.getInt("coins",0);hero("💳 My Wallet","TEST / no-billing balance • 💎 "+coins);row("💎","Diamonds","Gift and room test balance","detail_diamonds");row("🫘","Beans / Income","Room contribution and gift income","detail_income");row("🧾","Transaction History","Local and room activity records","wallet_history");row("🎁","Gift Inventory","Backpack and free gifts","gift_center");row("🔐","Wallet Safety","Server-authoritative wallet guidance","detail_wallet_security");}
"""
new="""    private void wallet(){startActivity(new Intent(this,KingWalletVisualActivity.class));finish();}
"""
if old not in q: raise SystemExit('wallet method marker missing')
q=q.replace(old,new,1)
deep.write_text(q)


# ---------------- Public profile visual parity ----------------
pub=pkg/'KingPublicProfileActivity.java'
q=pub.read_text()
old='''String frame=safe(p.getString("equippedFrame"),"Minimal Frame"),effect=safe(p.getString("entranceEffect"),"Welcome Sparkle"),bio=safe(p.getString("bio"),"KING Plus member"),tags=safe(p.getString("tags"),"");TextView hero=tv("👤  "+n+"\\nKING ID: "+publicId(uid)+"   •   Lv."+(lv==null?1:lv)+"   •   VIP "+(vip==null?0:vip),19,INK,true);hero.setBackground(bg(0xfffff0a5,18));hero.setPadding(dp(18),dp(14),dp(18),dp(14));body.addView(hero,new LinearLayout.LayoutParams(-1,dp(102)));'''
new='''String frame=safe(p.getString("equippedFrame"),"Minimal Frame"),effect=safe(p.getString("entranceEffect"),"Welcome Sparkle"),bio=safe(p.getString("bio"),"KING Plus member"),tags=safe(p.getString("tags"),"");String photo=safe(p.getString("photoUrl"),"");LinearLayout profileHead=new LinearLayout(this);profileHead.setGravity(Gravity.CENTER_VERTICAL);profileHead.setPadding(dp(12),dp(10),dp(12),dp(10));profileHead.setBackground(bg(Color.WHITE,18));android.widget.FrameLayout avatar=new android.widget.FrameLayout(this);TextView fallback=tv(n.isEmpty()?"K":n.substring(0,1).toUpperCase(),28,Color.WHITE,true);fallback.setGravity(Gravity.CENTER);fallback.setBackground(bg(0xff8a63db,40));avatar.addView(fallback,new android.widget.FrameLayout.LayoutParams(-1,-1));android.widget.ImageView image=new android.widget.ImageView(this);image.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);avatar.addView(image,new android.widget.FrameLayout.LayoutParams(-1,-1));if(!photo.isEmpty())loadProfilePhoto940(image,fallback,photo);profileHead.addView(avatar,new LinearLayout.LayoutParams(dp(82),dp(82)));LinearLayout idBlock=new LinearLayout(this);idBlock.setOrientation(LinearLayout.VERTICAL);idBlock.setPadding(dp(12),0,0,0);TextView nm=tv(n,20,INK,true);idBlock.addView(nm,new LinearLayout.LayoutParams(-1,dp(34)));TextView pid=tv("◇ ID: "+publicId(uid)+" ◇",12,MUTED,true);pid.setBackground(bg(0xfff2eff6,14));idBlock.addView(pid,new LinearLayout.LayoutParams(-1,dp(34)));TextView lvLine=tv("VIP "+(vip==null?0:vip)+"     Lv."+(lv==null?1:lv),12,PURPLE,true);idBlock.addView(lvLine,new LinearLayout.LayoutParams(-1,dp(30)));profileHead.addView(idBlock,new LinearLayout.LayoutParams(0,dp(88),1));body.addView(profileHead,new LinearLayout.LayoutParams(-1,dp(106)));TextView hero=tv("KING Plus profile • "+LevelSystem.levelTier((int)(long)(lv==null?1L:lv)),13,MUTED,true);hero.setGravity(Gravity.CENTER);hero.setBackground(bg(0xfffff0a5,14));body.addView(hero,new LinearLayout.LayoutParams(-1,dp(44)));'''
if old not in q: raise SystemExit('public profile hero marker missing')
q=q.replace(old,new,1)

marker='''    private TextView action(String s){'''
helpers='''    private void loadProfilePhoto940(android.widget.ImageView image,TextView fallback,String url){
        final String source=url;
        image.setTag(source);
        new Thread(() -> {
            try {
                java.net.URLConnection c=new java.net.URL(source).openConnection();
                c.setConnectTimeout(7000);
                c.setReadTimeout(7000);
                try(java.io.InputStream in=c.getInputStream()){
                    android.graphics.Bitmap bm=android.graphics.BitmapFactory.decodeStream(in);
                    if(bm!=null){
                        runOnUiThread(() -> {
                            if(!isFinishing()&&source.equals(image.getTag())){
                                image.setImageBitmap(bm);
                                fallback.setVisibility(android.view.View.GONE);
                            }
                        });
                    }
                }
            } catch(Exception ignored) { }
        }).start();
    }
'''
if marker not in q: raise SystemExit('public profile action marker missing')
q=q.replace(marker,helpers+marker,1)
pub.write_text(q)


# ---------------- Create Party visual parity ----------------
party=pkg/'PartyActivity.java'
q=party.read_text()
old="""        FrameLayout cover=new FrameLayout(this);cover.setBackground(bg(0x22000000,10));ImageView preview=new ImageView(this);preview.setScaleType(ImageView.ScaleType.CENTER_CROP);cover.addView(preview,new FrameLayout.LayoutParams(-1,-1));TextView coverText=tv("▣\nChange cover",14,Color.WHITE,true);coverText.setGravity(Gravity.CENTER);cover.addView(coverText,new FrameLayout.LayoutParams(-1,-1));cover.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("image/*");i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);startActivityForResult(i,ROOM_COVER_PICK_REQUEST);});LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(dp(110),dp(110));cp.setMargins(0,dp(8),0,dp(8));body.addView(cover,cp);
        if(pendingCreateCoverUri!=null){try{preview.setImageURI(pendingCreateCoverUri);coverText.setText("");}catch(Exception ignored){}}
        TextView tip=tv("Upload cover picture to gain more views",11,0xffffee00,true);tip.setGravity(Gravity.CENTER);body.addView(tip,new LinearLayout.LayoutParams(-1,dp(34)));
        LinearLayout flags=new LinearLayout(this);flags.setGravity(Gravity.CENTER);TextView pub=tv(pendingCreatePrivate?"🔒 Private":"🔓 Public",13,Color.WHITE,true);pub.setGravity(Gravity.CENTER);pub.setOnClickListener(v->{pendingCreatePrivate=!pendingCreatePrivate;renderCreateRoomPage();});flags.addView(pub,new LinearLayout.LayoutParams(0,dp(42),1));TextView seat=tv("Seat: "+pendingCreateSeats,13,Color.WHITE,true);seat.setGravity(Gravity.CENTER);seat.setOnClickListener(v->{int[] vals={8,10,12};int idx=0;for(int i=0;i<vals.length;i++)if(vals[i]==pendingCreateSeats)idx=i;pendingCreateSeats=vals[(idx+1)%vals.length];renderCreateRoomPage();});flags.addView(seat,new LinearLayout.LayoutParams(0,dp(42),1));body.addView(flags,new LinearLayout.LayoutParams(-1,dp(48)));
        TextView audience930=tv("👥 Up to 100 users can join • "+pendingCreateSeats+" stage/mic seats",11,0xffbfe5d8,true);audience930.setGravity(Gravity.CENTER);body.addView(audience930,new LinearLayout.LayoutParams(-1,dp(34)));
        final EditText name=new EditText(this);name.setHint("Room name");name.setHintTextColor(0xff9cb8ae);name.setTextColor(Color.WHITE);name.setTextSize(18);name.setSingleLine(true);name.setBackgroundColor(Color.TRANSPARENT);name.setPadding(dp(4),dp(10),dp(4),dp(10));String defaultRoomName921=safeName().trim();if(defaultRoomName921.isEmpty())defaultRoomName921="KING";name.setText(defaultRoomName921+"'s Party");name.setSelection(name.getText().length());body.addView(name,new LinearLayout.LayoutParams(-1,dp(58)));View line=new View(this);line.setBackgroundColor(0x446bd8b4);body.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));
        TextView type=tv("Room type: "+pendingCreateCategory+"  ›",13,0xffd5eee5,true);type.setGravity(Gravity.CENTER_VERTICAL);type.setPadding(dp(4),0,0,0);type.setOnClickListener(v->{String[] cats={"Chat","Event","Date","Music","Game","KTV","Radio","PK","Pick Me","Family","Multi Video"};new AlertDialog.Builder(this).setTitle("Choose room type").setItems(cats,(d,w)->{pendingCreateCategory=cats[w];renderCreateRoomPage();}).show();});body.addView(type,new LinearLayout.LayoutParams(-1,dp(52)));
        LinearLayout seats=new LinearLayout(this);seats.setOrientation(LinearLayout.VERTICAL);for(int r=0;r<2;r++){LinearLayout rr=new LinearLayout(this);rr.setGravity(Gravity.CENTER);for(int c=0;c<4;c++){int no=r*4+c+1;TextView bubble=tv("+\nNO."+no,11,0xffcce8de,true);bubble.setGravity(Gravity.CENTER);bubble.setBackground(bg(0x224fd0aa,45));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,dp(72),1);bp.setMargins(dp(5),dp(5),dp(5),dp(5));rr.addView(bubble,bp);}seats.addView(rr,new LinearLayout.LayoutParams(-1,dp(82)));}body.addView(seats,new LinearLayout.LayoutParams(-1,dp(168)));
        TextView start=tv(roomCreateInFlight921?"Creating Party…":"🎉 Start Room",15,0xff171717,true);start.setGravity(Gravity.CENTER);start.setBackground(bg(roomCreateInFlight921?0xffb9ad48:0xffffee00,10));start.setEnabled(!roomCreateInFlight921);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(54));sp.setMargins(0,dp(26),0,dp(8));body.addView(start,sp);start.setOnClickListener(v->{if(roomCreateInFlight921)return;String n=name.getText().toString().trim();if(n.length()<2){String base=safeName().trim();if(base.isEmpty())base="KING";n=base+"'s Party";name.setText(n);}createCloudRoom(n,pendingCreateCategory,pendingCreateSeats,pendingCreatePrivate);});
"""
new="""        FrameLayout cover=new FrameLayout(this);cover.setBackground(bg(0x33000000,18));KingRoomCoverView coverArt940=new KingRoomCoverView(this,pendingCreateCategory,safeName()+"'s Party");cover.addView(coverArt940,new FrameLayout.LayoutParams(-1,-1));ImageView preview=new ImageView(this);preview.setScaleType(ImageView.ScaleType.CENTER_CROP);cover.addView(preview,new FrameLayout.LayoutParams(-1,-1));TextView coverText=tv("▣\nChange cover",13,Color.WHITE,true);coverText.setGravity(Gravity.CENTER);coverText.setBackground(bg(0x44000000,18));cover.addView(coverText,new FrameLayout.LayoutParams(-1,-1));cover.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("image/*");i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);startActivityForResult(i,ROOM_COVER_PICK_REQUEST);});LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(dp(134),dp(134));cp.setMargins(0,dp(10),0,dp(6));body.addView(cover,cp);
        if(pendingCreateCoverUri!=null){try{preview.setImageURI(pendingCreateCoverUri);coverText.setText("✎");coverText.setGravity(Gravity.RIGHT|Gravity.BOTTOM);coverText.setPadding(0,0,dp(10),dp(8));}catch(Exception ignored){}}
        TextView tip=tv("Upload cover picture to gain more views",11,0xffffee00,true);tip.setGravity(Gravity.CENTER);body.addView(tip,new LinearLayout.LayoutParams(-1,dp(38)));
        LinearLayout flags=new LinearLayout(this);flags.setGravity(Gravity.CENTER);TextView pub=tv(pendingCreatePrivate?"🔒  Private":"🔓  Public",13,Color.WHITE,true);pub.setGravity(Gravity.CENTER);pub.setBackground(bg(0x224fd0aa,14));pub.setOnClickListener(v->{pendingCreatePrivate=!pendingCreatePrivate;renderCreateRoomPage();});LinearLayout.LayoutParams fp1=new LinearLayout.LayoutParams(0,dp(44),1);fp1.setMargins(dp(3),0,dp(5),0);flags.addView(pub,fp1);TextView seat=tv("🎙  Seat: "+pendingCreateSeats,13,Color.WHITE,true);seat.setGravity(Gravity.CENTER);seat.setBackground(bg(0x224fd0aa,14));seat.setOnClickListener(v->{int[] vals={8,10,12};int idx=0;for(int i=0;i<vals.length;i++)if(vals[i]==pendingCreateSeats)idx=i;pendingCreateSeats=vals[(idx+1)%vals.length];renderCreateRoomPage();});LinearLayout.LayoutParams fp2=new LinearLayout.LayoutParams(0,dp(44),1);fp2.setMargins(dp(5),0,dp(3),0);flags.addView(seat,fp2);body.addView(flags,new LinearLayout.LayoutParams(-1,dp(50)));
        TextView audience930=tv("👥 Up to 100 users can join • stage seats shown below",11,0xffbfe5d8,true);audience930.setGravity(Gravity.CENTER);body.addView(audience930,new LinearLayout.LayoutParams(-1,dp(32)));
        LinearLayout nameBox940=new LinearLayout(this);nameBox940.setOrientation(LinearLayout.VERTICAL);nameBox940.setPadding(dp(12),dp(5),dp(12),dp(4));nameBox940.setBackground(bg(0x18000000,12));TextView nameLabel940=tv("Room name",11,0xff93b8ac,true);nameBox940.addView(nameLabel940,new LinearLayout.LayoutParams(-1,dp(24)));final EditText name=new EditText(this);name.setHint("Room name");name.setHintTextColor(0xff9cb8ae);name.setTextColor(Color.WHITE);name.setTextSize(17);name.setSingleLine(true);name.setBackgroundColor(Color.TRANSPARENT);name.setPadding(0,0,0,0);String defaultRoomName921=safeName().trim();if(defaultRoomName921.isEmpty())defaultRoomName921="KING";name.setText(defaultRoomName921+"'s Party");name.setSelection(name.getText().length());nameBox940.addView(name,new LinearLayout.LayoutParams(-1,dp(42)));body.addView(nameBox940,new LinearLayout.LayoutParams(-1,dp(70)));
        TextView type=tv("Room type:  "+pendingCreateCategory+"   ›",13,0xffe4f5ef,true);type.setGravity(Gravity.CENTER_VERTICAL);type.setPadding(dp(12),0,dp(10),0);type.setBackground(bg(0x18000000,12));type.setOnClickListener(v->{String[] cats={"Hot","Chat","Event","Date","Music","Game","KTV","Radio","PK","Pick Me","Family","Multi Video"};new AlertDialog.Builder(this).setTitle("Choose room type").setItems(cats,(d,w)->{pendingCreateCategory=cats[w];renderCreateRoomPage();}).show();});LinearLayout.LayoutParams typeLp940=new LinearLayout.LayoutParams(-1,dp(50));typeLp940.setMargins(0,dp(8),0,dp(8));body.addView(type,typeLp940);
        LinearLayout seats=new LinearLayout(this);seats.setOrientation(LinearLayout.VERTICAL);int rows940=(pendingCreateSeats+3)/4;int seatNo940=1;for(int r=0;r<rows940;r++){LinearLayout rr=new LinearLayout(this);rr.setGravity(Gravity.CENTER);for(int c=0;c<4;c++){if(seatNo940<=pendingCreateSeats){TextView bubble=tv("+\nNO."+seatNo940,11,0xffd8eee7,true);bubble.setGravity(Gravity.CENTER);bubble.setBackground(bg(0x2b4fd0aa,45));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,dp(70),1);bp.setMargins(dp(6),dp(5),dp(6),dp(5));rr.addView(bubble,bp);seatNo940++;}else{View empty940=new View(this);rr.addView(empty940,new LinearLayout.LayoutParams(0,dp(70),1));}}seats.addView(rr,new LinearLayout.LayoutParams(-1,dp(80)));}body.addView(seats,new LinearLayout.LayoutParams(-1,rows940*dp(80)));
        TextView start=tv(roomCreateInFlight921?"Creating Party…":"🎉  Start Room",16,0xff171717,true);start.setGravity(Gravity.CENTER);start.setBackground(bg(roomCreateInFlight921?0xffb9ad48:0xffffee00,14));start.setEnabled(!roomCreateInFlight921);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(58));sp.setMargins(0,dp(20),0,dp(10));body.addView(start,sp);start.setOnClickListener(v->{if(roomCreateInFlight921)return;String n=name.getText().toString().trim();if(n.length()<2){String base=safeName().trim();if(base.isEmpty())base="KING";n=base+"'s Party";name.setText(n);}createCloudRoom(n,pendingCreateCategory,pendingCreateSeats,pendingCreatePrivate);});
"""
if old in q:
    q=q.replace(old,new,1)
else:
    print('Create Party visual marker diverged; keeping verified v9.3.1 layout')

# ---------------- In-room mic UX: Mic ON/OFF must stay inside Party room ----------------
old='''    private void toggleMic() {
        if(cloudRoom){if(micOn&&mySeat>0){micOn=false;setVoicePresence900(false);if(user!=null&&db!=null)db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(mySeat)).update("micOn",false).addOnFailureListener(e->{});refreshMicControl();toast("Stage mic off • leave the voice conference if it is still open");}else openVoice();return;}
        if(mySeat<1){toast("Take a mic seat first");return;} if(muteAll&&!isModerator()){toast("Host muted all seats");return;} micOn=!micOn;
        prefs.edit().putBoolean("mic_"+roomId,micOn).apply();seatMics.put(mySeat,micOn);rebuildSeats();refreshMicControl();
    }'''
new='''    private void toggleMic() {
        if(mySeat<1){toast("Take a mic seat first");return;}
        if(muteAll&&!isModerator()){toast("Host muted all seats");return;}
        micOn=!micOn;
        if(cloudRoom){
            setVoicePresence900(micOn);
            if(user!=null&&db!=null)db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(mySeat)).update("micOn",micOn).addOnFailureListener(e->toast("Mic sync failed: "+msg(e)));
            refreshMicControl();
            toast(micOn?"Mic ON • staying in Party room":"Mic OFF");
            return;
        }
        prefs.edit().putBoolean("mic_"+roomId,micOn).apply();seatMics.put(mySeat,micOn);rebuildSeats();refreshMicControl();
    }'''
if old not in q: raise SystemExit('v9.3.1 toggleMic marker missing')
q=q.replace(old,new,1)

old='''        micLabel.setContentDescription(micOn?"Live voice active. Tap to mark mic off":"Tap to join the shared live voice room");'''
new='''        micLabel.setContentDescription(micOn?"Mic is on in Party room. Tap to turn off":"Mic is off. Tap to turn on without leaving Party room");'''
if old in q:
    q=q.replace(old,new,1)

# ---------------- Inline audio-only RTC inside PartyActivity ----------------
old='''public class PartyActivity extends Activity {'''
new='''public class PartyActivity extends androidx.fragment.app.FragmentActivity implements org.jitsi.meet.sdk.JitsiMeetActivityInterface {'''
if old not in q: raise SystemExit('PartyActivity class marker missing for inline RTC')
q=q.replace(old,new,1)

old='''    private boolean roomCreateInFlight921;'''
new='''    private boolean roomCreateInFlight921;
    private org.jitsi.meet.sdk.JitsiMeetView inRoomVoiceView940;
    private boolean inRoomVoiceJoined940;
    private String inRoomVoiceSlug940="";'''
if old not in q: raise SystemExit('PartyActivity field marker missing for inline RTC')
q=q.replace(old,new,1)

old='''        setSafeContentView(shell);

        // Full-screen transparent live emoji overlay must remain above the room UI.'''
new='''        setSafeContentView(shell);
        attachInRoomVoice940(shell);

        // Full-screen transparent live emoji overlay must remain above the room UI.'''
if old not in q: raise SystemExit('PartyActivity room shell marker missing for inline RTC')
q=q.replace(old,new,1)

old='''        super.onResume();
        if(seatsBox!=null)rebuildSeats();'''
new='''        super.onResume();
        org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostResume(this);
        if(seatsBox!=null)rebuildSeats();'''
if old not in q: raise SystemExit('PartyActivity onResume marker missing for inline RTC')
q=q.replace(old,new,1)

old='''        super.onActivityResult(requestCode,resultCode,data);'''
new='''        super.onActivityResult(requestCode,resultCode,data);
        org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onActivityResult(this,requestCode,resultCode,data);'''
if old not in q: raise SystemExit('PartyActivity onActivityResult marker missing for inline RTC')
q=q.replace(old,new,1)

old='''    private void openVoice(){
        if(!cloudRoom||user==null||db==null||roomId==null){toast("Join a live Firebase Party room first");return;}
        if(muteAll&&!isModerator()){toast("Host muted room voice");return;}
        setVoicePresence900(true);
        if(mySeat>0){micOn=true;refreshMicControl();db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(mySeat)).update("micOn",true).addOnFailureListener(e->{});}
        else toast("Joining shared room voice • mic seats remain stage controls");
        voiceLaunched900=true;voiceLaunchAt900=System.currentTimeMillis();
        NativeMeetBridge.launch(this,roomId,roomName,safeName(),false);
    }'''
new='''    private void openVoice(){
        if(!cloudRoom||user==null||db==null||roomId==null){toast("Join a live Firebase Party room first");return;}
        if(mySeat<1){toast("Take a mic seat first");return;}
        if(!micOn)toggleMic();else toast("Mic is already ON in this Party room");
    }'''
if old not in q: raise SystemExit('PartyActivity openVoice marker missing for inline RTC')
q=q.replace(old,new,1)

old='''            refreshMicControl();
            toast(micOn?"Mic ON • staying in Party room":"Mic OFF");
            return;'''
new='''            refreshMicControl();
            setInlineAudioMuted940(!micOn);
            toast(micOn?"Mic ON • live inside Party room":"Mic OFF");
            return;'''
if old not in q: raise SystemExit('PartyActivity mic cloud marker missing for inline RTC')
q=q.replace(old,new,1)

old='''    private boolean isOwner(){return cloudRoom?user!=null&&ownerUid!=null&&ownerUid.equals(user.getUid()):displayName.equals(ownerName);}'''
helpers='''    private void attachInRoomVoice940(FrameLayout shell){
        if(!cloudRoom||roomId==null||roomId.trim().isEmpty()||user==null||shell==null)return;
        try{
            stopInRoomVoice940(false);
            org.jitsi.meet.sdk.JitsiMeet.instantiateReactNative(this);
            inRoomVoiceView940=new org.jitsi.meet.sdk.JitsiMeetView(this);
            inRoomVoiceView940.setAlpha(0.01f);
            inRoomVoiceView940.setClickable(false);
            inRoomVoiceView940.setFocusable(false);
            String slug=("KINGPlus-"+roomId).replaceAll("[^A-Za-z0-9_-]","");
            org.jitsi.meet.sdk.JitsiMeetUserInfo info940=new org.jitsi.meet.sdk.JitsiMeetUserInfo();
            info940.setDisplayName(safeName());
            org.jitsi.meet.sdk.JitsiMeetConferenceOptions options940=new org.jitsi.meet.sdk.JitsiMeetConferenceOptions.Builder()
                .setServerURL(new java.net.URL("https://meet.jit.si"))
                .setRoom(slug)
                .setSubject(roomName==null||roomName.trim().isEmpty()?"KING Plus Party":roomName)
                .setAudioMuted(!micOn)
                .setVideoMuted(true)
                .setConfigOverride("startLowBandwidthMode",true)
                .setUserInfo(info940)
                .setFeatureFlag("welcomepage.enabled",false)
                .setFeatureFlag("prejoinpage.enabled",false)
                .setFeatureFlag("toolbox.enabled",false)
                .setFeatureFlag("filmstrip.enabled",false)
                .setFeatureFlag("invite.enabled",false)
                .setFeatureFlag("chat.enabled",false)
                .setFeatureFlag("pip.enabled",false)
                .setFeatureFlag("call-integration.enabled",false)
                .setConfigOverride("disableSelfView",true)
                .setConfigOverride("disableReactions",true)
                .build();
            FrameLayout.LayoutParams voiceLp940=new FrameLayout.LayoutParams(dp(2),dp(2));
            voiceLp940.gravity=Gravity.TOP|Gravity.LEFT;
            shell.addView(inRoomVoiceView940,voiceLp940);
            inRoomVoiceView940.join(options940);
            inRoomVoiceSlug940=slug;
            inRoomVoiceJoined940=true;
            setInlineAudioMuted940(!micOn);
        }catch(Throwable voiceError940){
            inRoomVoiceJoined940=false;
            try{if(inRoomVoiceView940!=null)inRoomVoiceView940.dispose();}catch(Throwable ignored){}
            inRoomVoiceView940=null;
            android.util.Log.e("KINGPlusParty","Inline room voice failed",voiceError940);
        }
    }
    private void setInlineAudioMuted940(boolean muted){
        if(!inRoomVoiceJoined940)return;
        try{
            Intent voiceIntent940=org.jitsi.meet.sdk.BroadcastIntentHelper.buildSetAudioMutedIntent(muted);
            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(voiceIntent940);
        }catch(Throwable ignored){}
    }
    private void stopInRoomVoice940(boolean hangup){
        if(hangup&&inRoomVoiceJoined940){
            try{
                Intent hangup940=org.jitsi.meet.sdk.BroadcastIntentHelper.buildHangUpIntent();
                androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(hangup940);
            }catch(Throwable ignored){}
        }
        try{if(inRoomVoiceView940!=null)inRoomVoiceView940.dispose();}catch(Throwable ignored){}
        try{if(inRoomVoiceView940!=null&&inRoomVoiceView940.getParent() instanceof android.view.ViewGroup)((android.view.ViewGroup)inRoomVoiceView940.getParent()).removeView(inRoomVoiceView940);}catch(Throwable ignored){}
        inRoomVoiceView940=null;
        inRoomVoiceJoined940=false;
        inRoomVoiceSlug940="";
    }

    private boolean isOwner(){return cloudRoom?user!=null&&ownerUid!=null&&ownerUid.equals(user.getUid()):displayName.equals(ownerName);}'''
if old not in q: raise SystemExit('PartyActivity owner marker missing for inline RTC helpers')
q=q.replace(old,helpers,1)

old='''    private void leaveRoom(){unregisterMember();clearListeners();renderLobby("Hot");}'''
new='''    private void leaveRoom(){stopInRoomVoice940(true);unregisterMember();clearListeners();renderLobby("Hot");}'''
if old not in q: raise SystemExit('PartyActivity leaveRoom marker missing for inline RTC')
q=q.replace(old,new,1)

old='''    @Override public void onBackPressed(){if(roomId!=null)leaveRoom();else KingNav.confirmExit(this);}
    @Override protected void onDestroy(){unregisterMember();clearListeners();try{if(roomMusicPlayer!=null){roomMusicPlayer.release();roomMusicPlayer=null;}}catch(Exception ignored){}super.onDestroy();}'''
new='''    @Override protected void onStop(){org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostPause(this);super.onStop();}
    @Override public void onNewIntent(Intent intent){super.onNewIntent(intent);org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onNewIntent(intent);}
    @Override public void requestPermissions(String[] permissions,int requestCode,com.facebook.react.modules.core.PermissionListener listener){org.jitsi.meet.sdk.JitsiMeetActivityDelegate.requestPermissions(this,permissions,requestCode,listener);}
    @android.annotation.SuppressLint("MissingSuperCall")
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onRequestPermissionsResult(requestCode,permissions,grantResults);}
    @Override public void onBackPressed(){if(roomId!=null)leaveRoom();else KingNav.confirmExit(this);}
    @Override protected void onDestroy(){stopInRoomVoice940(true);unregisterMember();clearListeners();try{if(roomMusicPlayer!=null){roomMusicPlayer.release();roomMusicPlayer=null;}}catch(Exception ignored){}org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostDestroy(this);super.onDestroy();}'''
if old not in q: raise SystemExit('PartyActivity lifecycle tail marker missing for inline RTC')
q=q.replace(old,new,1)

party.write_text(q)

print('v9.4.0 parity batch 1 applied')
