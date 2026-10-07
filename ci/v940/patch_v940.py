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
new='''        addDiscoverEntrances940();
        addMatchTabs940();
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
helpers='''    private void addDiscoverEntrances940(){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);
        addDiscoverEntrance940(row,"🎤","Party","Live voice rooms",()->startActivity(new Intent(this,PartyActivity.class)));
        addDiscoverEntrance940(row,"🎵","KTV","Sing & queue",()->startActivity(new Intent(this,PartyActivity.class)));
        addDiscoverEntrance940(row,"🎮","Games","Realtime multiplayer",()->KingNav.openRoot(this,1));
        addDiscoverEntrance940(row,"👑","Family","Family center",()->{Intent i=new Intent(this,CommunityHubActivity.class);i.putExtra("tab","Family");startActivity(i);});
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(94));rp.setMargins(0,dp(2),0,dp(8));content.addView(row,rp);
    }
    private void addDiscoverEntrance940(LinearLayout row,String icon,String title,String sub,Runnable action){
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(4),dp(6),dp(4),dp(4));card.setBackground(bg(0xfff7f4fb,14));
        TextView av=tv(icon,25,DARK,false);av.setGravity(Gravity.CENTER);card.addView(av,new LinearLayout.LayoutParams(-1,dp(34)));
        TextView t=tv(title,12,DARK,true);t.setGravity(Gravity.CENTER);card.addView(t,new LinearLayout.LayoutParams(-1,dp(23)));
        TextView s=tv(sub,9,MUTED,false);s.setGravity(Gravity.CENTER);s.setSingleLine(true);card.addView(s,new LinearLayout.LayoutParams(-1,dp(20)));
        card.setOnClickListener(v->action.run());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(88),1);p.setMargins(dp(3),0,dp(3),0);row.addView(card,p);
    }

    private void addMatchTabs940(){
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

# Messages loading/offline/error states: retain local/system content instead of blank/toast-only failures.
inbox=pkg/'InboxActivity.java'
d=inbox.read_text()
old='''    private final ArrayList<ThreadModel> models = new ArrayList<>();'''
new='''    private final ArrayList<ThreadModel> models = new ArrayList<>();
    private String inboxState940="";'''
if old not in d: raise SystemExit('Inbox models field marker missing')
d=d.replace(old,new,1)

old='''    private void loadInbox() {
        models.clear();
        Set<String> local = getSharedPreferences("chat_store", MODE_PRIVATE).getStringSet("recent_names", new HashSet<>());
        for (String name : local) if (name != null && !name.trim().isEmpty()) models.add(new ThreadModel("●",name,"Tap to continue chatting","","",false));
        renderModels(models);

        if (me == null || db == null) return;
        threadListener = db.collection("direct_threads").whereArrayContains("members", me.getUid())
            .addSnapshotListener((snap,error)->{
                if(error!=null){Toast.makeText(this,"Messages could not refresh",Toast.LENGTH_SHORT).show();return;}
                renderCloudThreads(snap);
            });
    }'''
new='''    private void loadInbox() {
        models.clear();
        Set<String> local = getSharedPreferences("chat_store", MODE_PRIVATE).getStringSet("recent_names", new HashSet<>());
        for (String name : local) if (name != null && !name.trim().isEmpty()) models.add(new ThreadModel("●",name,"Tap to continue chatting","","",false));
        if(me==null)inboxState940="Sign in to sync real conversations";
        else if(db==null)inboxState940="Cloud unavailable • showing local conversations";
        else inboxState940="Syncing conversations…";
        renderModels(models);

        if (me == null || db == null) return;
        threadListener = db.collection("direct_threads").whereArrayContains("members", me.getUid())
            .addSnapshotListener((snap,error)->{
                if(error!=null){
                    inboxState940="Offline / sync unavailable • showing recent local conversations";
                    renderModels(models);
                    return;
                }
                inboxState940="";
                renderCloudThreads(snap);
            });
    }'''
if old not in d: raise SystemExit('Inbox loadInbox marker missing')
d=d.replace(old,new,1)

old='''    private void renderModels(List<ThreadModel> source){
        if(list==null)return; list.removeAllViews();addSystemRows940();
        if(source==null||source.isEmpty()){
'''
new='''    private void renderModels(List<ThreadModel> source){
        if(list==null)return; list.removeAllViews();addSystemRows940();addInboxState940();
        if(source==null||source.isEmpty()){
'''
if old not in d: raise SystemExit('Inbox v9.4 renderModels marker missing')
d=d.replace(old,new,1)

marker='''    private void addSystemRows940(){'''
helper='''    private void addInboxState940(){
        if(inboxState940==null||inboxState940.trim().isEmpty())return;
        TextView state=label(inboxState940,12,inboxState940.startsWith("Syncing")?0xff675d76:0xff8b5b12,true);
        state.setGravity(Gravity.CENTER_VERTICAL);state.setPadding(dp(16),0,dp(16),0);
        state.setBackground(bg(inboxState940.startsWith("Syncing")?0xfff3eefb:0xfffff4d6,10));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(42));p.setMargins(dp(12),dp(7),dp(12),dp(7));list.addView(state,p);
    }
'''
if marker not in d: raise SystemExit('Inbox system rows marker missing for state helper')
d=d.replace(marker,helper+marker,1)
inbox.write_text(d)
print('v9.4.0 Messages loading/offline states applied')

# Direct chat resilience: never leave a blank conversation or silently lose a send on cloud failure.
chat=pkg/'ChatActivity.java'
q=chat.read_text()
old='''                if (error != null) { status.setText("Cloud unavailable • local fallback available"); Toast.makeText(this,"Realtime chat error: "+safe(error.getLocalizedMessage()),Toast.LENGTH_SHORT).show(); return; }
                if (snap == null) return;
                List<DocumentSnapshot> docs = new ArrayList<>(snap.getDocuments()); java.util.Collections.reverse(docs);
                messages.removeAllViews();
                for (DocumentSnapshot doc : docs) renderCloudMessage(doc);
                markRead(); scrollDown();'''
new='''                if (error != null) { status.setText("Offline • showing local fallback"); loadLocal(); return; }
                if (snap == null) { status.setText("Sync unavailable • showing local fallback"); loadLocal(); return; }
                status.setText("Realtime chat");
                List<DocumentSnapshot> docs = new ArrayList<>(snap.getDocuments()); java.util.Collections.reverse(docs);
                messages.removeAllViews();
                if(docs.isEmpty())addBubble(false,"Start the conversation 👋","text","",new Date(),false);
                else for (DocumentSnapshot doc : docs) renderCloudMessage(doc);
                markRead(); scrollDown();'''
if old not in q: raise SystemExit('Chat realtime listener marker missing')
q=q.replace(old,new,1)

old='''                db.collection("direct_threads").document(chatId).collection("messages").add(msg)
                    .addOnSuccessListener(r -> CloudBackend.sendDirectMessageNotification(peerUid, myName, text, (ok,m)->{}))
                    .addOnFailureListener(e -> Toast.makeText(this,"Message failed: "+safe(e.getLocalizedMessage()),Toast.LENGTH_SHORT).show());
            }).addOnFailureListener(e -> Toast.makeText(this,"Chat could not start: "+safe(e.getLocalizedMessage()),Toast.LENGTH_SHORT).show());'''
new='''                db.collection("direct_threads").document(chatId).collection("messages").add(msg)
                    .addOnSuccessListener(r -> {status.setText("Realtime chat");CloudBackend.sendDirectMessageNotification(peerUid, myName, text, (ok,m)->{});})
                    .addOnFailureListener(e -> {status.setText("Offline • message saved on this phone");saveLocal(text,type,mediaUrl,true);});
            }).addOnFailureListener(e -> {status.setText("Offline • message saved on this phone");saveLocal(text,type,mediaUrl,true);});'''
if old not in q: raise SystemExit('Chat cloud send marker missing')
q=q.replace(old,new,1)

old='''            .addOnSuccessListener(url -> { status.setText("Realtime chat"); sendCloudMessage(type.equals("photo") ? "📷 Photo" : "🎤 Voice note", type, url.toString()); })
            .addOnFailureListener(e -> { status.setText("Upload failed"); Toast.makeText(this,"Upload failed: "+safe(e.getLocalizedMessage()),Toast.LENGTH_SHORT).show(); });'''
new='''            .addOnSuccessListener(url -> { status.setText("Realtime chat"); sendCloudMessage(type.equals("photo") ? "📷 Photo" : "🎤 Voice note", type, url.toString()); })
            .addOnFailureListener(e -> { status.setText("Offline • media saved locally"); saveLocal(type.equals("photo") ? "📷 Photo" : "🎤 Voice note", type, uri.toString(), true); });'''
if old not in q: raise SystemExit('Chat media upload marker missing')
q=q.replace(old,new,1)
chat.write_text(q)
print('v9.4.0 direct chat offline fallback applied')


# ---------------- Party lobby loading/offline/retry states ----------------
party=pkg/'PartyActivity.java'
q=party.read_text()
old='''        if (user != null && db != null) {
            LinearLayout cloudList = new LinearLayout(this); cloudList.setOrientation(LinearLayout.VERTICAL); page.addView(cloudList);
            roomsListener = db.collection("live_rooms").orderBy("createdAt", Query.Direction.DESCENDING).limit(30)
                .addSnapshotListener((snap,error) -> {
                    if (error != null) { showLobbyError(cloudList,"Live rooms could not load. Tap to retry.",selected); return; }
                    cloudList.removeAllViews();'''
new='''        if (user != null && db != null) {
            LinearLayout cloudList = new LinearLayout(this); cloudList.setOrientation(LinearLayout.VERTICAL); page.addView(cloudList);
            TextView lobbyState940=tv(KingNetwork.online(this)?"Loading live Party rooms…":"Offline • checking cached Party rooms…",13,0xff756c7d,true);
            lobbyState940.setGravity(Gravity.CENTER);lobbyState940.setBackground(bg(0xfff3eef7,14));LinearLayout.LayoutParams lobbyStateLp940=new LinearLayout.LayoutParams(-1,dp(64));lobbyStateLp940.setMargins(dp(6),dp(8),dp(6),0);cloudList.addView(lobbyState940,lobbyStateLp940);
            roomsListener = db.collection("live_rooms").orderBy("createdAt", Query.Direction.DESCENDING).limit(30)
                .addSnapshotListener((snap,error) -> {
                    if (error != null) { showLobbyError(cloudList,KingNetwork.online(this)?"Live rooms unavailable • tap to retry":"Offline • cached rooms unavailable • tap to retry",selected); return; }
                    cloudList.removeAllViews();'''
if old not in q: raise SystemExit('Party lobby listener marker missing')
q=q.replace(old,new,1)

old='''    private void showLobbyError(LinearLayout host,String message,String selected){
        host.removeAllViews();TextView e=tv(message,14,0xff8b4d64,true);e.setGravity(Gravity.CENTER);e.setBackground(bg(0xffffe9ef,16));e.setOnClickListener(v->renderLobby(selected));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(90));p.setMargins(dp(6),dp(12),dp(6),0);host.addView(e,p);
    }'''
new='''    private void showLobbyError(LinearLayout host,String message,String selected){
        host.removeAllViews();LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setGravity(Gravity.CENTER);box.setBackground(bg(0xfffff1e8,16));
        TextView e=tv(message,14,0xff8b4d64,true);e.setGravity(Gravity.CENTER);box.addView(e,new LinearLayout.LayoutParams(-1,dp(48)));
        TextView retry=tv("↻ Retry",13,0xff5b2aa8,true);retry.setGravity(Gravity.CENTER);retry.setBackground(bg(0xffffffff,16));retry.setOnClickListener(v->renderLobby(selected));LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(dp(120),dp(38));rp.gravity=Gravity.CENTER;box.addView(retry,rp);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(112));p.setMargins(dp(6),dp(12),dp(6),0);host.addView(box,p);
    }'''
if old not in q: raise SystemExit('Party lobby error helper marker missing')
q=q.replace(old,new,1)
party.write_text(q)
print('v9.4.0 Party lobby loading/offline states applied')

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
        setInlineAudioMuted940(!(micOn&&mySeat>0));
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
            String slug=("KINGPlus-"+roomId).replaceAll("[^A-Za-z0-9_-]","");
            if(inRoomVoiceJoined940&&inRoomVoiceView940!=null&&slug.equals(inRoomVoiceSlug940)){
                try{if(inRoomVoiceView940.getParent() instanceof android.view.ViewGroup)((android.view.ViewGroup)inRoomVoiceView940.getParent()).removeView(inRoomVoiceView940);}catch(Throwable ignored){}
                FrameLayout.LayoutParams reusedVoiceLp940=new FrameLayout.LayoutParams(dp(2),dp(2));reusedVoiceLp940.gravity=Gravity.TOP|Gravity.LEFT;
                shell.addView(inRoomVoiceView940,reusedVoiceLp940);
                setInlineAudioMuted940(!(micOn&&mySeat>0));
                return;
            }
            stopInRoomVoice940(false);
            org.jitsi.meet.sdk.JitsiMeet.instantiateReactNative(this);
            inRoomVoiceView940=new org.jitsi.meet.sdk.JitsiMeetView(this);
            inRoomVoiceView940.setAlpha(0.01f);
            inRoomVoiceView940.setClickable(false);
            inRoomVoiceView940.setFocusable(false);
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

# ---------------- Game navigation: no exposed simulated multiplayer paths ----------------
main=pkg/'MainActivity.java'
q=main.read_text()
old='''    private void openPlayableGame(String game){if("ludo".equalsIgnoreCase(game)){startActivity(new Intent(this,KingLudoLobbyActivity.class));return;}Intent i=new Intent(this,GamePlayActivity.class);i.putExtra("game",game);startActivity(i);}'''
new='''    private void openPlayableGame(String game){
        String g=game==null?"":game.toLowerCase(java.util.Locale.US);
        if("ludo".equals(g)){startActivity(new Intent(this,KingLudoLobbyActivity.class));return;}
        if(g.equals("werewolf")||g.equals("spy")||g.equals("draw")||g.equals("bingo")||g.equals("domino")||g.equals("sheep")||g.equals("zoo")){
            kingOpenGameRoom(game);
            return;
        }
        Intent i=new Intent(this,GamePlayActivity.class);i.putExtra("game",game);startActivity(i);
    }'''
if old not in q: raise SystemExit('Main openPlayableGame marker missing')
q=q.replace(old,new,1)

old='''            if(w==0)kingStartGame(game,"Quick Match");
            else if(w==1)kingStartGame(game,"1 vs 1");
            else if(w==2)kingStartGame(game,"Team Match");
            else if(w==3)kingInviteGame(game);
            else if(w==4)kingOpenGameRoom(game);
            else kingGameRules(game);'''
new='''            if(w==0)openPlayableGame(playableCode730(game));
            else if(w==1||w==2||w==4)kingOpenGameRoom(game);
            else if(w==3)kingInviteGame(game);
            else kingGameRules(game);'''
if old not in q: raise SystemExit('Main game mode routing marker missing')
q=q.replace(old,new,1)
main.write_text(q)

# Carry the selected social game into the Party screen instead of dropping the extra.
party=pkg/'PartyActivity.java'
q=party.read_text()
old='''    private String inRoomVoiceSlug940="";'''
new='''    private String inRoomVoiceSlug940="";
    private String requestedGame940="";'''
if old not in q: raise SystemExit('Party inline voice field marker missing for requested game')
q=q.replace(old,new,1)

old='''        displayName = getIntent().getStringExtra("displayName");
        if (displayName == null || displayName.trim().isEmpty()) displayName = safeName();'''
new='''        displayName = getIntent().getStringExtra("displayName");
        if (displayName == null || displayName.trim().isEmpty()) displayName = safeName();
        requestedGame940=safe(getIntent().getStringExtra("requestedGame"),"");'''
if old not in q: raise SystemExit('Party onCreate displayName marker missing for requested game')
q=q.replace(old,new,1)

old='''        roomRoot.addView(badges,new LinearLayout.LayoutParams(-1,dp(36)));

        // Hidden state labels retained for existing realtime/controller logic.'''
new='''        roomRoot.addView(badges,new LinearLayout.LayoutParams(-1,dp(36)));
        if(!requestedGame940.isEmpty()){
            TextView gamePrompt940=pill("🎮 "+requestedGame940+" • Open Room Games",0x553b1b6b,()->{
                Intent gi940=new Intent(this,RoomGameActivity.class);
                gi940.putExtra("roomId",roomId);
                gi940.putExtra("roomName",roomName);
                gi940.putExtra("displayName",safeName());
                gi940.putExtra("requestedGame",requestedGame940);
                startActivity(gi940);
            });
            gamePrompt940.setTextSize(11);
            roomRoot.addView(gamePrompt940,new LinearLayout.LayoutParams(-1,dp(34)));
        }

        // Hidden state labels retained for existing realtime/controller logic.'''
if old not in q: raise SystemExit('Party badges marker missing for requested game banner')
q=q.replace(old,new,1)
party.write_text(q)

# Show selected-game intent in multiplayer controls so the requested mode is not lost.
rg=pkg/'RoomGameActivity.java'
q=rg.read_text()
old='''    private String roomId="",roomName="Live Room",displayName="KING User";'''
new='''    private String roomId="",roomName="Live Room",displayName="KING User",requestedGame="";'''
if old not in q: raise SystemExit('RoomGame field marker missing')
q=q.replace(old,new,1)
old='''        roomId=s(getIntent().getStringExtra("roomId"));roomName=s(getIntent().getStringExtra("roomName"));displayName=s(getIntent().getStringExtra("displayName"));if(roomName.isEmpty())roomName="Live Room";if(displayName.isEmpty())displayName="KING User";'''
new='''        roomId=s(getIntent().getStringExtra("roomId"));roomName=s(getIntent().getStringExtra("roomName"));displayName=s(getIntent().getStringExtra("displayName"));requestedGame=s(getIntent().getStringExtra("requestedGame"));if(roomName.isEmpty())roomName="Live Room";if(displayName.isEmpty())displayName="KING User";'''
if old not in q: raise SystemExit('RoomGame onCreate marker missing')
q=q.replace(old,new,1)
old='''        roleText=tv("Checking room role…",12,MUTED,false);page.addView(roleText);'''
new='''        roleText=tv("Checking room role…",12,MUTED,false);page.addView(roleText);
        if(!requestedGame.isEmpty()){TextView requested=tv("🎮 Selected from Games: "+requestedGame+" • tap Ready, then host/co-host starts the synchronized round",12,GOLD,true);requested.setBackground(bg(CARD,12));page.addView(requested,new LinearLayout.LayoutParams(-1,dp(54)));}'''
if old not in q: raise SystemExit('RoomGame role marker missing')
q=q.replace(old,new,1)
rg.write_text(q)


# Party control cleanup after inline RTC
party=pkg/'PartyActivity.java'
q=party.read_text()
q=q.replace('''        items.add("🎙 Open voice room"); items.add("📹 Multi Video"); items.add("🎵 Song request"); items.add("😊 Reaction");''','''        items.add(micOn?"🎤 Mic OFF":"🎤 Mic ON"); items.add("📹 Multi Video"); items.add("🎵 Song request"); items.add("😊 Reaction");''',1)
q=q.replace('''        else if(x.contains("Open voice"))openVoice();
        else if(x.contains("Multi Video"))openVideoRoom();''','''        else if(x.contains("Mic ON")||x.contains("Mic OFF"))toggleMic();
        else if(x.contains("Multi Video"))openVideoRoom();''',1)
q=q.replace('''        else if(x.contains("Games"))openDeepFlow810("game_center");''','''        else if(x.contains("Games"))openRoomGames740();''',1)
q=q.replace('''        else if("Voice Room".equals(label))openVoice();
        else if("Multi Video".equals(label))openVideoRoom();''','''        else if("Mic ON".equals(label)||"Mic OFF".equals(label))toggleMic();
        else if("Multi Video".equals(label))openVideoRoom();''',1)
q=q.replace('''"Find User","Notice","Voice Room","Multi Video"''','''"Find User","Notice",micOn?"Mic OFF":"Mic ON","Multi Video"''',1)
old='''        seatsListener=room.collection("seats").addSnapshotListener((snap,e)->{if(e!=null||snap==null)return;seatNames.clear();seatUids.clear();seatMics.clear();mySeat=-1;for(DocumentSnapshot d:snap.getDocuments()){int no;try{no=Integer.parseInt(d.getId());}catch(Exception ex){continue;}seatNames.put(no,str(d,"name","Guest"));seatUids.put(no,d.getString("uid"));seatMics.put(no,!Boolean.FALSE.equals(d.getBoolean("micOn")));if(user.getUid().equals(d.getString("uid"))){mySeat=no;micOn=Boolean.TRUE.equals(d.getBoolean("micOn"));}}rebuildSeats();refreshPeopleCounts();if(micLabel!=null){refreshMicControl();}});'''
new='''        seatsListener=room.collection("seats").addSnapshotListener((snap,e)->{if(e!=null||snap==null)return;seatNames.clear();seatUids.clear();seatMics.clear();mySeat=-1;for(DocumentSnapshot d:snap.getDocuments()){int no;try{no=Integer.parseInt(d.getId());}catch(Exception ex){continue;}seatNames.put(no,str(d,"name","Guest"));seatUids.put(no,d.getString("uid"));seatMics.put(no,!Boolean.FALSE.equals(d.getBoolean("micOn")));if(user.getUid().equals(d.getString("uid"))){mySeat=no;micOn=Boolean.TRUE.equals(d.getBoolean("micOn"));}}setInlineAudioMuted940(!(micOn&&mySeat>0));rebuildSeats();refreshPeopleCounts();if(micLabel!=null){refreshMicControl();}});'''
if old in q:q=q.replace(old,new,1)
party.write_text(q)
print('v9.4.0 Party controls cleaned')

# Keep embedded RTC in sync with seat removal and host Mute All.
party=pkg/'PartyActivity.java'
q=party.read_text()
old='''        seatsListener=room.collection("seats").addSnapshotListener((snap,e)->{if(e!=null||snap==null)return;seatNames.clear();seatUids.clear();seatMics.clear();mySeat=-1;for(DocumentSnapshot d:snap.getDocuments()){int no;try{no=Integer.parseInt(d.getId());}catch(Exception ex){continue;}seatNames.put(no,str(d,"name","Guest"));seatUids.put(no,d.getString("uid"));seatMics.put(no,!Boolean.FALSE.equals(d.getBoolean("micOn")));if(user.getUid().equals(d.getString("uid"))){mySeat=no;micOn=Boolean.TRUE.equals(d.getBoolean("micOn"));}}setInlineAudioMuted940(!(micOn&&mySeat>0));rebuildSeats();refreshPeopleCounts();if(micLabel!=null){refreshMicControl();}});'''
new='''        seatsListener=room.collection("seats").addSnapshotListener((snap,e)->{if(e!=null||snap==null)return;seatNames.clear();seatUids.clear();seatMics.clear();mySeat=-1;for(DocumentSnapshot d:snap.getDocuments()){int no;try{no=Integer.parseInt(d.getId());}catch(Exception ex){continue;}seatNames.put(no,str(d,"name","Guest"));seatUids.put(no,d.getString("uid"));seatMics.put(no,!Boolean.FALSE.equals(d.getBoolean("micOn")));if(user.getUid().equals(d.getString("uid"))){mySeat=no;micOn=Boolean.TRUE.equals(d.getBoolean("micOn"));}}if(mySeat<1)micOn=false;setInlineAudioMuted940(!(micOn&&mySeat>0));rebuildSeats();refreshPeopleCounts();if(micLabel!=null){refreshMicControl();}});'''
if old in q:q=q.replace(old,new,1)

old='''muteAll=Boolean.TRUE.equals(doc.getBoolean("muteAll"));roomPrivate=Boolean.TRUE.equals(doc.getBoolean("isPrivate"));'''
new='''muteAll=Boolean.TRUE.equals(doc.getBoolean("muteAll"));if(muteAll&&!isModerator()&&micOn){micOn=false;setInlineAudioMuted940(true);if(mySeat>0)room.collection("seats").document(String.valueOf(mySeat)).update("micOn",false).addOnFailureListener(x->{});refreshMicControl();}roomPrivate=Boolean.TRUE.equals(doc.getBoolean("isPrivate"));'''
if old in q:q=q.replace(old,new,1)
party.write_text(q)
print('v9.4.0 RTC moderation sync applied')

# Deduplicate sender gift animations and keep emoji replay state bounded.
party=pkg/'PartyActivity.java'
q=party.read_text()
old='''                if(ok){toast(message);addGiftEvent(targetUid,targetName,gift,icon,unitCost,quantity,totalCost);return;}
                if(localCoins>=totalCost){localCoins-=totalCost;prefs.edit().putInt("coins",localCoins).apply();addGiftEvent(targetUid,targetName,gift,icon,unitCost,quantity,totalCost);toast("TEST room gift synced • backend wallet unavailable");}'''
new='''                if(ok){toast(message);showGiftEffect(safeName(),targetName,gift,icon,quantity,totalCost);addGiftEvent(targetUid,targetName,gift,icon,unitCost,quantity,totalCost);return;}
                if(localCoins>=totalCost){localCoins-=totalCost;prefs.edit().putInt("coins",localCoins).apply();showGiftEffect(safeName(),targetName,gift,icon,quantity,totalCost);addGiftEvent(targetUid,targetName,gift,icon,unitCost,quantity,totalCost);toast("TEST room gift synced • backend wallet unavailable");}'''
if old not in q: raise SystemExit('gift cloud send marker missing')
q=q.replace(old,new,1)

old='''                if("gift".equals(type)){Long q=d.getLong("giftQty");Long v=d.getLong("giftValue");String gift=d.getString("giftName");showGiftEffect(str(d,"actorName","User"),str(d,"targetName","Host"),gift,giftIconFor(gift),q==null?1:q,v==null?1:v);}'''
new='''                if("gift".equals(type)){Long q=d.getLong("giftQty");Long v=d.getLong("giftValue");String gift=d.getString("giftName");String actorUid=d.getString("actorUid");if(user==null||actorUid==null||!user.getUid().equals(actorUid))showGiftEffect(str(d,"actorName","User"),str(d,"targetName","Host"),gift,giftIconFor(gift),q==null?1:q,v==null?1:v);}'''
if old not in q: raise SystemExit('gift event listener marker missing')
q=q.replace(old,new,1)

old='''            for(DocumentSnapshot docV530:snapV530.getDocuments()){
                if(!"live_emoji".equals(docV530.getString("type")) || seenLiveEmojiEventsV530.contains(docV530.getId())) continue;
                seenLiveEmojiEventsV530.add(docV530.getId()); Object tsV530=docV530.get("createdAt");
                if(tsV530 instanceof com.google.firebase.Timestamp){long ageV530=System.currentTimeMillis()-((com.google.firebase.Timestamp)tsV530).toDate().getTime();if(ageV530>12000)continue;}
                String actorV530=str(docV530,"actorName","User"); String emojiV530=str(docV530,"emoji",str(docV530,"text","😊"));
                if(!user.getUid().equals(docV530.getString("actorUid"))) showLiveEmojiEffect560(emojiV530,actorV530,docV530.getString("actorUid"));
            }
        });'''
new='''            java.util.HashSet<String> currentEmojiIds940=new java.util.HashSet<>();
            for(DocumentSnapshot docV530:snapV530.getDocuments()){
                currentEmojiIds940.add(docV530.getId());
                if(!"live_emoji".equals(docV530.getString("type")) || seenLiveEmojiEventsV530.contains(docV530.getId())) continue;
                seenLiveEmojiEventsV530.add(docV530.getId()); Object tsV530=docV530.get("createdAt");
                if(tsV530 instanceof com.google.firebase.Timestamp){long ageV530=System.currentTimeMillis()-((com.google.firebase.Timestamp)tsV530).toDate().getTime();if(ageV530>12000)continue;}
                String actorV530=str(docV530,"actorName","User"); String emojiV530=str(docV530,"emoji",str(docV530,"text","😊"));
                if(!user.getUid().equals(docV530.getString("actorUid"))) showLiveEmojiEffect560(emojiV530,actorV530,docV530.getString("actorUid"));
            }
            if(seenLiveEmojiEventsV530.size()>80)seenLiveEmojiEventsV530.retainAll(currentEmojiIds940);
        });'''
if old not in q: raise SystemExit('live emoji listener marker missing')
q=q.replace(old,new,1)
party.write_text(q)
print('v9.4.0 gift and emoji replay protection applied')

# Route exposed Party feature tiles to working room flows instead of presentation-only pages.
party=pkg/'PartyActivity.java'
q=party.read_text()
q=q.replace('''        else if("KTV Stage".equals(label))openParity870("ktv");
        else if("PK Arena".equals(label))openParity870("pk");
        else if("Match".equals(label))openParity870("match");
        else if("Party Stage".equals(label))openParity870("stage");
        else if("VIP Rank".equals(label))openParity870("vip");''','''        else if("KTV Stage".equals(label))ktvQueuePanel();
        else if("PK Arena".equals(label))audioPkPanel700();
        else if("Match".equals(label))openRoomGames740();
        else if("Party Stage".equals(label))roomSeatPanel();
        else if("VIP Rank".equals(label))roomRankingDialog();''',1)
q=q.replace('''        else if(x.contains("KTV Stage"))openParity870("ktv");
        else if(x.contains("PK Arena"))openParity870("pk");
        else if(x.contains("Match Center"))openParity870("match");
        else if(x.contains("Party Stage"))openParity870("stage");
        else if(x.contains("VIP & Rank"))openParity870("vip");''','''        else if(x.contains("KTV Stage"))ktvQueuePanel();
        else if(x.contains("PK Arena"))audioPkPanel700();
        else if(x.contains("Match Center"))openRoomGames740();
        else if(x.contains("Party Stage"))roomSeatPanel();
        else if(x.contains("VIP & Rank"))roomRankingDialog();''',1)
party.write_text(q)
print('v9.4.0 functional Party feature routing applied')

# Synced timed Audio PK state: same timer/scores for all room members.
party=pkg/'PartyActivity.java'
q=party.read_text()
old='''    private void audioPkPanel700(){if(!cloudRoom||db==null){toast("Live room required");return;}db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(200).get().addOnSuccessListener(q->{long red=0,blue=0;int gifts=0;for(DocumentSnapshot e:q.getDocuments()){if(!"gift".equals(e.getString("type")))continue;Long raw=e.getLong("giftValue");long score=raw==null?0:Math.max(0,raw);String actor=e.getString("actorUid");Integer seat=null;if(actor!=null)for(Map.Entry<Integer,String>x:seatUids.entrySet())if(actor.equals(x.getValue())){seat=x.getKey();break;}if(seat!=null&&seat%2==0)red+=score;else if(seat!=null)blue+=score;else if((gifts%2)==0)red+=score;else blue+=score;gifts++;}String msg="🔴 Red "+compactNumber(red)+"\\n\\n🔵 Blue "+compactNumber(blue)+"\\n\\n"+(red==blue?"🤝 Draw":red>blue?"🏆 Red leads":"🏆 Blue leads");AlertDialog.Builder b=new AlertDialog.Builder(this).setTitle("⚔️ Audio PK").setMessage(msg).setPositiveButton("Refresh",(d,w)->audioPkPanel700()).setNegativeButton("Close",null);if(isModerator())b.setNeutralButton("Start PK",(d,w)->addEvent("audio_pk",safeName()+" started Audio PK ⚔️"));b.show();});}'''
new='''    private void audioPkPanel700(){
        if(!cloudRoom||db==null||roomId==null){toast("Live room required");return;}
        DocumentReference state=db.collection("live_rooms").document(roomId).collection("game_state").document("audio_pk");
        state.get().addOnSuccessListener(pk->{
            boolean active=Boolean.TRUE.equals(pk.getBoolean("active"));
            com.google.firebase.Timestamp started=pk.getTimestamp("startedAt");
            Long durationRaw=pk.getLong("durationSec");long duration=durationRaw==null?180L:Math.max(30L,durationRaw);
            long elapsed=started==null?0L:Math.max(0L,(System.currentTimeMillis()-started.toDate().getTime())/1000L);
            long remain=Math.max(0L,duration-elapsed);
            if(active&&remain<=0){active=false;if(isModerator())state.update("active",false,"endedAt",FieldValue.serverTimestamp()).addOnFailureListener(x->{});}
            final boolean pkActive=active;final long pkRemain=remain;final com.google.firebase.Timestamp pkStarted=started;
            db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(250).get().addOnSuccessListener(ev->{
                long red=0,blue=0;int gifts=0;
                for(DocumentSnapshot e:ev.getDocuments()){
                    if(!"gift".equals(e.getString("type")))continue;
                    com.google.firebase.Timestamp at=e.getTimestamp("createdAt");
                    if(pkStarted!=null&&at!=null&&at.compareTo(pkStarted)<0)continue;
                    Long raw=e.getLong("giftValue");long score=raw==null?0:Math.max(0,raw);
                    String actor=e.getString("actorUid");Integer seat=null;
                    if(actor!=null)for(Map.Entry<Integer,String>x:seatUids.entrySet())if(actor.equals(x.getValue())){seat=x.getKey();break;}
                    if(seat!=null&&seat%2==0)red+=score;else if(seat!=null)blue+=score;else if((gifts%2)==0)red+=score;else blue+=score;gifts++;
                }
                String timer=pkActive?String.format(java.util.Locale.US,"%02d:%02d",pkRemain/60,pkRemain%60):"Not running";
                String result=red==blue?"🤝 Draw":red>blue?"🏆 Red leads":"🏆 Blue leads";
                String msg="⏱ "+timer+"\\n\\n🔴 Red  "+compactNumber(red)+"\\n\\n🔵 Blue  "+compactNumber(blue)+"\\n\\n"+result+"\\n\\nGifts in this PK: "+gifts;
                AlertDialog.Builder b=new AlertDialog.Builder(this).setTitle("⚔️ Audio PK").setMessage(msg).setPositiveButton(pkActive?"Refresh":"Close",(d,w)->{if(pkActive)audioPkPanel700();}).setNegativeButton(pkActive?"Close":null,null);
                if(isModerator()){
                    if(pkActive)b.setNeutralButton("End PK",(d,w)->{Map<String,Object>end=new HashMap<>();end.put("active",false);end.put("endedAt",FieldValue.serverTimestamp());end.put("endedBy",user==null?"":user.getUid());state.set(end,SetOptions.merge()).addOnSuccessListener(v->{addEvent("audio_pk_end",safeName()+" ended Audio PK");audioPkPanel700();});});
                    else b.setNeutralButton("Start 3 min PK",(d,w)->{Map<String,Object>start=new HashMap<>();start.put("active",true);start.put("durationSec",180);start.put("startedAt",FieldValue.serverTimestamp());start.put("startedBy",user==null?"":user.getUid());start.put("startedByName",safeName());state.set(start,SetOptions.merge()).addOnSuccessListener(v->{addEvent("audio_pk",safeName()+" started 3-minute Audio PK ⚔️");audioPkPanel700();}).addOnFailureListener(e->toast("PK start failed: "+msg(e)));});
                }
                b.show();
            }).addOnFailureListener(e->toast("PK scores unavailable: "+msg(e)));
        }).addOnFailureListener(e->toast("PK state unavailable: "+msg(e)));
    }'''
if old not in q: raise SystemExit('Audio PK base marker missing')
q=q.replace(old,new,1)
party.write_text(q)
print('v9.4.0 synchronized timed Audio PK applied')

# Synced KTV stage state over room game_state/ktv while keeping immutable request events.
party=pkg/'PartyActivity.java'
q=party.read_text()
old='''    private void ktvQueuePanel(){
        if(!cloudRoom||db==null||roomId==null){karaokeDialog();return;}
        db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(120).get().addOnSuccessListener(snap->{
            List<String> rows=new ArrayList<>();for(DocumentSnapshot d:snap.getDocuments())if("song".equals(d.getString("type"))){String text=d.getString("text");if(text!=null&&!text.trim().isEmpty())rows.add("🎵 "+text);if(rows.size()>=20)break;}
            List<String> actions=new ArrayList<>();actions.add("＋ Request a song");if(isModerator())actions.add("🎛 Open room music player");actions.addAll(rows);
            new AlertDialog.Builder(this).setTitle("🎤 KTV Queue").setItems(actions.toArray(new String[0]),(d,w)->{String x=actions.get(w);if(x.startsWith("＋"))karaokeDialog();else if(x.contains("music player"))musicPanel();}).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast("KTV queue unavailable: "+msg(e)));
    }'''
new='''    private void ktvQueuePanel(){
        if(!cloudRoom||db==null||roomId==null){karaokeDialog();return;}
        DocumentReference ktv=db.collection("live_rooms").document(roomId).collection("game_state").document("ktv");
        ktv.get().addOnSuccessListener(state->db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(120).get().addOnSuccessListener(snap->{
            boolean active=Boolean.TRUE.equals(state.getBoolean("active"));
            String nowSong=str(state,"song","");
            String singer=str(state,"singerName","");
            List<String> actions=new ArrayList<>();List<DocumentSnapshot> requestDocs=new ArrayList<>();
            if(active&&!nowSong.isEmpty())actions.add("🎤 Now Singing • "+singer+" • "+nowSong);
            actions.add("＋ Request a song");
            if(isModerator()){actions.add("🎛 Open room music player");if(active)actions.add("⏹ End current singer");}
            for(DocumentSnapshot doc:snap.getDocuments()){
                if(!"song".equals(doc.getString("type")))continue;
                String text=doc.getString("text");if(text==null||text.trim().isEmpty())continue;
                actions.add("🎵 "+text);requestDocs.add(doc);if(requestDocs.size()>=20)break;
            }
            new AlertDialog.Builder(this).setTitle("🎤 KTV Queue").setItems(actions.toArray(new String[0]),(d,w)->{
                String x=actions.get(w);
                if(x.startsWith("🎤 Now Singing")){toast(active?"KTV stage is live":"No singer");return;}
                if(x.startsWith("＋")){karaokeDialog();return;}
                if(x.contains("music player")){musicPanel();return;}
                if(x.startsWith("⏹")){
                    Map<String,Object>end=new HashMap<>();end.put("active",false);end.put("endedAt",FieldValue.serverTimestamp());end.put("endedBy",user==null?"":user.getUid());
                    ktv.set(end,SetOptions.merge()).addOnSuccessListener(v->{addEvent("ktv_end",safeName()+" ended KTV singer");ktvQueuePanel();});return;
                }
                if(!x.startsWith("🎵"))return;
                if(!isModerator()){toast("Host/co-host selects the next singer");return;}
                int offset=actions.size()-requestDocs.size();int ri=w-offset;if(ri<0||ri>=requestDocs.size())return;
                DocumentSnapshot req=requestDocs.get(ri);String raw=str(req,"text","Song request");
                String song=raw;int mark=raw.indexOf(" requested 🎵 ");if(mark>=0)song=raw.substring(mark+" requested 🎵 ".length()).trim();final String selectedSong940=song;
                Map<String,Object>stage=new HashMap<>();stage.put("active",true);stage.put("song",selectedSong940);stage.put("singerUid",str(req,"actorUid",""));stage.put("singerName",str(req,"actorName","Guest"));stage.put("requestEventId",req.getId());stage.put("startedAt",FieldValue.serverTimestamp());stage.put("startedBy",user==null?"":user.getUid());
                ktv.set(stage,SetOptions.merge()).addOnSuccessListener(v->{addEvent("ktv_start",safeName()+" put "+str(req,"actorName","Guest")+" on KTV stage • "+selectedSong940);ktvQueuePanel();}).addOnFailureListener(e->toast("KTV stage failed: "+msg(e)));
            }).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast("KTV queue unavailable: "+msg(e)))).addOnFailureListener(e->toast("KTV state unavailable: "+msg(e)));
    }'''
if old not in q: raise SystemExit('KTV queue base marker missing')
q=q.replace(old,new,1)
party.write_text(q)
print('v9.4.0 synchronized KTV stage applied')

# Route every exposed non-Ludo game into synchronized Party multiplayer, never local simulated matches.
main=pkg/'MainActivity.java'
q=main.read_text()
old='''    private void openPlayableGame(String game){
        String g=game==null?"":game.toLowerCase(java.util.Locale.US);
        if("ludo".equals(g)){startActivity(new Intent(this,KingLudoLobbyActivity.class));return;}
        if(g.equals("werewolf")||g.equals("spy")||g.equals("draw")||g.equals("bingo")||g.equals("domino")||g.equals("sheep")||g.equals("zoo")){
            kingOpenGameRoom(game);
            return;
        }
        Intent i=new Intent(this,GamePlayActivity.class);i.putExtra("game",game);startActivity(i);
    }'''
new='''    private void openPlayableGame(String game){
        String g=game==null?"":game.toLowerCase(java.util.Locale.US);
        if("ludo".equals(g)){startActivity(new Intent(this,KingLudoLobbyActivity.class));return;}
        kingOpenGameRoom(game);
    }'''
if old not in q: raise SystemExit('Main v9.4 multiplayer route marker missing')
q=q.replace(old,new,1)
main.write_text(q)

rg=pkg/'RoomGameActivity.java'
q=rg.read_text()
old='''    private void openPlayableRoomGame(String code){
        Intent i=new Intent(this,GamePlayActivity.class);
        i.putExtra("game",code);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);i.putExtra("displayName",displayName);
        startActivity(i);
    }'''
new='''    private String normalizeRoomGame940(String code){
        String g=code==null?"":code.toLowerCase(java.util.Locale.US).trim();
        if("tic_tac_toe".equals(g)||"tic tac toe".equals(g))return "ttt";
        if("guess".equals(g)||"guess number".equals(g))return "number";
        if(g.contains("ludo"))return "ludo";
        if(g.contains("werewolf"))return "werewolf";
        if(g.contains("spy"))return "spy";
        if(g.contains("draw"))return "draw";
        if(g.contains("bingo"))return "bingo";
        if(g.contains("domino"))return "domino";
        if(g.contains("sheep"))return "sheep";
        if(g.contains("zoo"))return "zoo";
        if(g.contains("memory"))return "memory";
        if(g.contains("reaction"))return "reaction";
        if(g.contains("high"))return "highlow";
        if(g.contains("wheel"))return "wheel";
        if(g.contains("slot"))return "slot";
        if(g.contains("coin"))return "coin";
        if(g.contains("rock")||g.contains("rps"))return "rps";
        if(g.contains("dice"))return "dice";
        if(g.contains("number"))return "number";
        return g;
    }
    private void startRequestedGame940(String code){
        String gt=normalizeRoomGame940(code);
        if("ludo".equals(gt)){openPartyLudo862();return;}
        if(!moderator){toast("Tap Ready • host/co-host starts "+code+" for the room");return;}
        startRound(gt);
    }
    private void openPlayableRoomGame(String code){startRequestedGame940(code);}'''
if old not in q: raise SystemExit('RoomGame local game route marker missing')
q=q.replace(old,new,1)

old='''        if(!requestedGame.isEmpty()){TextView requested=tv("🎮 Selected from Games: "+requestedGame+" • tap Ready, then host/co-host starts the synchronized round",12,GOLD,true);requested.setBackground(bg(CARD,12));page.addView(requested,new LinearLayout.LayoutParams(-1,dp(54)));}'''
new='''        if(!requestedGame.isEmpty()){TextView requested=tv("🎮 Selected from Games: "+requestedGame+" • realtime room mode",12,GOLD,true);requested.setBackground(bg(CARD,12));page.addView(requested,new LinearLayout.LayoutParams(-1,dp(54)));Button selectedStart=button("▶ "+requestedGame+" • Start / Join");selectedStart.setOnClickListener(v->startRequestedGame940(requestedGame));LinearLayout.LayoutParams sgp=new LinearLayout.LayoutParams(-1,dp(50));sgp.setMargins(0,dp(4),0,dp(6));page.addView(selectedStart,sgp);}'''
if old not in q: raise SystemExit('RoomGame requested game prompt marker missing')
q=q.replace(old,new,1)
rg.write_text(q)
print('v9.4.0 all exposed games routed to synchronized room multiplayer')

# Route Party Family actions into the real Community/Family data flow.
party=pkg/'PartyActivity.java'
q=party.read_text()
old='''    private void familyPartyPanel610(){
        String[] actions={"🏠 Family room info","👥 Invite family/friends","🎉 Post family activity","📢 Family announcement"};
        new AlertDialog.Builder(this).setTitle("💞 Family Party").setItems(actions,(d,w)->{
            if(w==0)new AlertDialog.Builder(this).setTitle("Family Party").setMessage("Room: "+roomName+"\\nOnline: "+(cloudRoom?liveMemberCount:Math.max(1,memberNames.size()))+"\\nSeats: "+seatNames.size()+"/"+maxSeats).setPositiveButton("OK",null).show();
            else if(w==1)shareRoom();
            else if(w==2){addEvent("family_activity",safeName()+" started a Family Party activity");toast("Family activity posted to room");}
            else roomNoticePanel();
        }).setNegativeButton("Close",null).show();
    }'''
new='''    private void familyPartyPanel610(){
        String[] actions={"👑 Open Family Center","🏠 Family room info","👥 Invite family/friends","🎉 Post family activity","📢 Family announcement"};
        new AlertDialog.Builder(this).setTitle("💞 Family Party").setItems(actions,(d,w)->{
            if(w==0)openCommunityHub700("Family");
            else if(w==1)new AlertDialog.Builder(this).setTitle("Family Party").setMessage("Room: "+roomName+"\\nOnline: "+(cloudRoom?liveMemberCount:Math.max(1,memberNames.size()))+"\\nSeats: "+seatNames.size()+"/"+maxSeats).setPositiveButton("Family Center",(x,y)->openCommunityHub700("Family")).setNegativeButton("Close",null).show();
            else if(w==2)shareRoom();
            else if(w==3){addEvent("family_activity",safeName()+" started a Family Party activity");toast("Family activity posted to room");}
            else roomNoticePanel();
        }).setNegativeButton("Close",null).show();
    }'''
if old not in q: raise SystemExit('Family Party base marker missing')
q=q.replace(old,new,1)

old='''new AlertDialog.Builder(this).setTitle("👑 Family").setMessage("Current family: "+family+(code.isEmpty()?"":"\\nFamily code: "+code)).setPositiveButton("Open Me page",(d,w)->{Intent i=new Intent(this,MainActivity.class);i.putExtra("openTab",4);startActivity(i);}).setNeutralButton("Share Family",(d,w)->shareFamilyRoom()).setNegativeButton("Close",null).show();'''
new='''new AlertDialog.Builder(this).setTitle("👑 Family").setMessage("Current family: "+family+(code.isEmpty()?"":"\\nFamily code: "+code)).setPositiveButton("Open Family Center",(d,w)->openCommunityHub700("Family")).setNeutralButton("Share Family",(d,w)->shareFamilyRoom()).setNegativeButton("Close",null).show();'''
if old in q:q=q.replace(old,new,1)

party.write_text(q)
print('v9.4.0 real Family routing applied')

# Robust Create Party visual pass using stable, granular markers.
party=pkg/'PartyActivity.java'
q=party.read_text()

old='''FrameLayout cover=new FrameLayout(this);cover.setBackground(bg(0x22000000,10));ImageView preview=new ImageView(this);'''
new='''FrameLayout cover=new FrameLayout(this);cover.setBackground(bg(0x33000000,18));KingRoomCoverView createCover940=new KingRoomCoverView(this,pendingCreateCategory,safeName()+"'s Party");cover.addView(createCover940,new FrameLayout.LayoutParams(-1,-1));ImageView preview=new ImageView(this);'''
if old not in q: raise SystemExit('Create Party cover marker missing')
q=q.replace(old,new,1)

q=q.replace('''TextView coverText=tv("▣\\nChange cover",14,Color.WHITE,true);coverText.setGravity(Gravity.CENTER);cover.addView(coverText,new FrameLayout.LayoutParams(-1,-1));''','''TextView coverText=tv("▣\\nChange cover",13,Color.WHITE,true);coverText.setGravity(Gravity.CENTER);coverText.setBackground(bg(0x44000000,18));cover.addView(coverText,new FrameLayout.LayoutParams(-1,-1));''',1)
q=q.replace('''LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(dp(110),dp(110));cp.setMargins(0,dp(8),0,dp(8));''','''LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(dp(134),dp(134));cp.setMargins(0,dp(10),0,dp(6));''',1)
q=q.replace('''if(pendingCreateCoverUri!=null){try{preview.setImageURI(pendingCreateCoverUri);coverText.setText("");}catch(Exception ignored){}}''','''if(pendingCreateCoverUri!=null){try{preview.setImageURI(pendingCreateCoverUri);coverText.setText("✎");coverText.setGravity(Gravity.RIGHT|Gravity.BOTTOM);coverText.setPadding(0,0,dp(10),dp(8));}catch(Exception ignored){}}''',1)

old='''LinearLayout flags=new LinearLayout(this);flags.setGravity(Gravity.CENTER);TextView pub=tv(pendingCreatePrivate?"🔒 Private":"🔓 Public",13,Color.WHITE,true);pub.setGravity(Gravity.CENTER);pub.setOnClickListener(v->{pendingCreatePrivate=!pendingCreatePrivate;renderCreateRoomPage();});flags.addView(pub,new LinearLayout.LayoutParams(0,dp(42),1));TextView seat=tv("Seat: "+pendingCreateSeats,13,Color.WHITE,true);seat.setGravity(Gravity.CENTER);seat.setOnClickListener(v->{int[] vals={8,10,12};int idx=0;for(int i=0;i<vals.length;i++)if(vals[i]==pendingCreateSeats)idx=i;pendingCreateSeats=vals[(idx+1)%vals.length];renderCreateRoomPage();});flags.addView(seat,new LinearLayout.LayoutParams(0,dp(42),1));body.addView(flags,new LinearLayout.LayoutParams(-1,dp(48)));'''
new='''LinearLayout flags=new LinearLayout(this);flags.setGravity(Gravity.CENTER);TextView pub=tv(pendingCreatePrivate?"🔒  Private":"🔓  Public",13,Color.WHITE,true);pub.setGravity(Gravity.CENTER);pub.setBackground(bg(0x224fd0aa,14));pub.setOnClickListener(v->{pendingCreatePrivate=!pendingCreatePrivate;renderCreateRoomPage();});LinearLayout.LayoutParams pubLp940=new LinearLayout.LayoutParams(0,dp(44),1);pubLp940.setMargins(dp(3),0,dp(5),0);flags.addView(pub,pubLp940);TextView seat=tv("🎙  Seat: "+pendingCreateSeats,13,Color.WHITE,true);seat.setGravity(Gravity.CENTER);seat.setBackground(bg(0x224fd0aa,14));seat.setOnClickListener(v->{int[] vals={8,10,12};int idx=0;for(int i=0;i<vals.length;i++)if(vals[i]==pendingCreateSeats)idx=i;pendingCreateSeats=vals[(idx+1)%vals.length];renderCreateRoomPage();});LinearLayout.LayoutParams seatLp940=new LinearLayout.LayoutParams(0,dp(44),1);seatLp940.setMargins(dp(5),0,dp(3),0);flags.addView(seat,seatLp940);body.addView(flags,new LinearLayout.LayoutParams(-1,dp(50)));'''
if old not in q: raise SystemExit('Create Party flags marker missing')
q=q.replace(old,new,1)

old='''final EditText name=new EditText(this);name.setHint("Room name");name.setHintTextColor(0xff9cb8ae);name.setTextColor(Color.WHITE);name.setTextSize(18);name.setSingleLine(true);name.setBackgroundColor(Color.TRANSPARENT);name.setPadding(dp(4),dp(10),dp(4),dp(10));String defaultRoomName921=safeName().trim();if(defaultRoomName921.isEmpty())defaultRoomName921="KING";name.setText(defaultRoomName921+"'s Party");name.setSelection(name.getText().length());body.addView(name,new LinearLayout.LayoutParams(-1,dp(58)));View line=new View(this);line.setBackgroundColor(0x446bd8b4);body.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));'''
new='''LinearLayout nameBox940=new LinearLayout(this);nameBox940.setOrientation(LinearLayout.VERTICAL);nameBox940.setPadding(dp(12),dp(5),dp(12),dp(4));nameBox940.setBackground(bg(0x18000000,12));TextView nameLabel940=tv("Room name",11,0xff93b8ac,true);nameBox940.addView(nameLabel940,new LinearLayout.LayoutParams(-1,dp(24)));final EditText name=new EditText(this);name.setHint("Room name");name.setHintTextColor(0xff9cb8ae);name.setTextColor(Color.WHITE);name.setTextSize(17);name.setSingleLine(true);name.setBackgroundColor(Color.TRANSPARENT);name.setPadding(0,0,0,0);String defaultRoomName921=safeName().trim();if(defaultRoomName921.isEmpty())defaultRoomName921="KING";name.setText(defaultRoomName921+"'s Party");name.setSelection(name.getText().length());nameBox940.addView(name,new LinearLayout.LayoutParams(-1,dp(42)));body.addView(nameBox940,new LinearLayout.LayoutParams(-1,dp(70)));'''
if old not in q: raise SystemExit('Create Party name marker missing')
q=q.replace(old,new,1)

old='''TextView type=tv("Room type: "+pendingCreateCategory+"  ›",13,0xffd5eee5,true);type.setGravity(Gravity.CENTER_VERTICAL);type.setPadding(dp(4),0,0,0);type.setOnClickListener(v->{String[] cats={"Chat","Event","Date","Music","Game","KTV","Radio","PK","Pick Me","Family","Multi Video"};'''
new='''TextView type=tv("Room type:  "+pendingCreateCategory+"   ›",13,0xffe4f5ef,true);type.setGravity(Gravity.CENTER_VERTICAL);type.setPadding(dp(12),0,dp(10),0);type.setBackground(bg(0x18000000,12));type.setOnClickListener(v->{String[] cats={"Hot","Chat","Event","Date","Music","Game","KTV","Radio","PK","Pick Me","Family","Multi Video"};'''
if old not in q: raise SystemExit('Create Party type marker missing')
q=q.replace(old,new,1)

old='''LinearLayout seats=new LinearLayout(this);seats.setOrientation(LinearLayout.VERTICAL);for(int r=0;r<2;r++){LinearLayout rr=new LinearLayout(this);rr.setGravity(Gravity.CENTER);for(int c=0;c<4;c++){int no=r*4+c+1;TextView bubble=tv("+\\nNO."+no,11,0xffcce8de,true);bubble.setGravity(Gravity.CENTER);bubble.setBackground(bg(0x224fd0aa,45));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,dp(72),1);bp.setMargins(dp(5),dp(5),dp(5),dp(5));rr.addView(bubble,bp);}seats.addView(rr,new LinearLayout.LayoutParams(-1,dp(82)));}body.addView(seats,new LinearLayout.LayoutParams(-1,dp(168)));'''
new='''LinearLayout seats=new LinearLayout(this);seats.setOrientation(LinearLayout.VERTICAL);int rows940=(pendingCreateSeats+3)/4;int seatNo940=1;for(int r=0;r<rows940;r++){LinearLayout rr=new LinearLayout(this);rr.setGravity(Gravity.CENTER);for(int c=0;c<4;c++){if(seatNo940<=pendingCreateSeats){TextView bubble=tv("+\\nNO."+seatNo940,11,0xffd8eee7,true);bubble.setGravity(Gravity.CENTER);bubble.setBackground(bg(0x2b4fd0aa,45));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,dp(70),1);bp.setMargins(dp(6),dp(5),dp(6),dp(5));rr.addView(bubble,bp);seatNo940++;}else{View empty940=new View(this);rr.addView(empty940,new LinearLayout.LayoutParams(0,dp(70),1));}}seats.addView(rr,new LinearLayout.LayoutParams(-1,dp(80)));}body.addView(seats,new LinearLayout.LayoutParams(-1,rows940*dp(80)));'''
if old not in q: raise SystemExit('Create Party seats marker missing')
q=q.replace(old,new,1)

q=q.replace('''TextView start=tv(roomCreateInFlight921?"Creating Party…":"🎉 Start Room",15,0xff171717,true);start.setGravity(Gravity.CENTER);start.setBackground(bg(roomCreateInFlight921?0xffb9ad48:0xffffee00,10));''','''TextView start=tv(roomCreateInFlight921?"Creating Party…":"🎉  Start Room",16,0xff171717,true);start.setGravity(Gravity.CENTER);start.setBackground(bg(roomCreateInFlight921?0xffb9ad48:0xffffee00,14));''',1)

party.write_text(q)
print('v9.4.0 robust Create Party visual pass applied')

# Preserve canonical KING identity across Google re-login, including offline/profile-read failure.
main=pkg/'MainActivity.java'
q=main.read_text()
old='''    private void restoreCanonicalGoogleProfile931(FirebaseUser user,String fallbackName,boolean navigateHome){
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
    }'''
new='''    private void restoreCanonicalGoogleProfile931(FirebaseUser user,String fallbackName,boolean navigateHome){
        if(user==null)return;
        clearCrossAccountIdentity931(user.getUid());
        final SharedPreferences identityPrefs=getPreferences(0);
        final boolean sameAccount=user.getUid().equals(identityPrefs.getString("firebase_uid",""));
        final String localName=sameAccount?identityPrefs.getString("name",""):"";
        final String localPhoto=sameAccount?identityPrefs.getString("profile_photo_cloud",""):"";
        final String googleName=googleName931(user,fallbackName);
        final String googlePhoto=user.getPhotoUrl()==null?"":user.getPhotoUrl().toString();
        final String safeLocalName=localName==null?"":localName.trim();
        final String safeLocalPhoto=localPhoto==null?"":localPhoto.trim();
        if(firestore==null){
            applyCanonicalIdentity931(user,safeLocalName.isEmpty()?googleName:safeLocalName,safeLocalPhoto.isEmpty()?googlePhoto:safeLocalPhoto,null);
            finishGoogleIdentity931(navigateHome,false);return;
        }
        firestore.collection("public_profiles").document(user.getUid()).get()
            .addOnSuccessListener(doc->{
                boolean existing=doc!=null&&doc.exists();
                String canonicalName=existing?doc.getString("displayName"):null;
                if(canonicalName==null||canonicalName.trim().isEmpty())canonicalName=safeLocalName.isEmpty()?googleName:safeLocalName;
                String canonicalPhoto=existing?doc.getString("photoUrl"):null;
                if(canonicalPhoto==null||canonicalPhoto.trim().isEmpty())canonicalPhoto=safeLocalPhoto.isEmpty()?googlePhoto:safeLocalPhoto;
                applyCanonicalIdentity931(user,canonicalName,canonicalPhoto,doc);
                if(!existing)syncPublicProfile();
                finishGoogleIdentity931(navigateHome,true);
            })
            .addOnFailureListener(e->{
                applyCanonicalIdentity931(user,safeLocalName.isEmpty()?googleName:safeLocalName,safeLocalPhoto.isEmpty()?googlePhoto:safeLocalPhoto,null);
                KingStability.nonFatal(this,"canonical-profile-read",e);finishGoogleIdentity931(navigateHome,false);
            });
    }'''
if old not in q: raise SystemExit('canonical Google profile method marker missing')
q=q.replace(old,new,1)
main.write_text(q)
print('v9.4.0 canonical profile re-login hardening applied')

# Discover profile grid loading/offline/retry states.
discover=pkg/'DiscoverActivity.java'
q=discover.read_text()
old='''    private void loadProfileGrid940(){
        Object old=content.findViewWithTag("profile_grid_940");LinearLayout grid;if(old instanceof LinearLayout){grid=(LinearLayout)old;grid.removeAllViews();}else{grid=new LinearLayout(this);grid.setTag("profile_grid_940");grid.setOrientation(LinearLayout.VERTICAL);content.addView(grid,new LinearLayout.LayoutParams(-1,-2));}
        if(me==null||db==null){grid.addView(emptyText("Sign in to discover KING profiles"),new LinearLayout.LayoutParams(-1,dp(90)));return;}
        Query q=db.collection("public_profiles").limit(24);String city=getSharedPreferences("discover_public",MODE_PRIVATE).getString("city","");
        if("Nearby".equals(activePeopleTab)&&!city.isEmpty())q=db.collection("public_profiles").whereEqualTo("hometown",city).limit(24);
        q.get().addOnSuccessListener(snap->{grid.removeAllViews();LinearLayout row=null;int shown=0;for(DocumentSnapshot p:snap.getDocuments()){String uid=safe(p.getString("uid"),p.getId());if(uid.equals(me.getUid()))continue;if(shown%2==0){row=new LinearLayout(this);row.setGravity(Gravity.TOP);grid.addView(row,new LinearLayout.LayoutParams(-1,dp(222)));}addProfileCard940(row,p);shown++;if(shown>=8)break;}if(shown==0)grid.addView(emptyText("No matching KING profiles yet"),new LinearLayout.LayoutParams(-1,dp(90)));}).addOnFailureListener(e->{grid.removeAllViews();grid.addView(emptyText("Profile discovery unavailable"),new LinearLayout.LayoutParams(-1,dp(90)));});
    }'''
new='''    private void loadProfileGrid940(){
        Object old=content.findViewWithTag("profile_grid_940");LinearLayout grid;if(old instanceof LinearLayout){grid=(LinearLayout)old;grid.removeAllViews();}else{grid=new LinearLayout(this);grid.setTag("profile_grid_940");grid.setOrientation(LinearLayout.VERTICAL);content.addView(grid,new LinearLayout.LayoutParams(-1,-2));}
        if(me==null||db==null){TextView state=emptyText("Sign in to discover KING profiles");grid.addView(state,new LinearLayout.LayoutParams(-1,dp(90)));return;}
        if(!KingNetwork.online(this)){TextView offline=emptyText("Offline • tap to retry Discover");offline.setOnClickListener(v->loadProfileGrid940());grid.addView(offline,new LinearLayout.LayoutParams(-1,dp(90)));return;}
        TextView loading=emptyText("Loading KING profiles…");grid.addView(loading,new LinearLayout.LayoutParams(-1,dp(72)));
        Query q=db.collection("public_profiles").limit(24);String city=getSharedPreferences("discover_public",MODE_PRIVATE).getString("city","");
        if("Nearby".equals(activePeopleTab)&&!city.isEmpty())q=db.collection("public_profiles").whereEqualTo("hometown",city).limit(24);
        q.get().addOnSuccessListener(snap->{grid.removeAllViews();LinearLayout row=null;int shown=0;for(DocumentSnapshot p:snap.getDocuments()){String uid=safe(p.getString("uid"),p.getId());if(uid.equals(me.getUid()))continue;if(shown%2==0){row=new LinearLayout(this);row.setGravity(Gravity.TOP);grid.addView(row,new LinearLayout.LayoutParams(-1,dp(222)));}addProfileCard940(row,p);shown++;if(shown>=8)break;}if(shown==0){TextView empty=emptyText("No matching KING profiles yet • tap to refresh");empty.setOnClickListener(v->loadProfileGrid940());grid.addView(empty,new LinearLayout.LayoutParams(-1,dp(90)));}}).addOnFailureListener(e->{grid.removeAllViews();TextView retry=emptyText("Discover unavailable • tap to retry");retry.setOnClickListener(v->loadProfileGrid940());grid.addView(retry,new LinearLayout.LayoutParams(-1,dp(90)));});
    }'''
if old not in q: raise SystemExit('Discover profile grid method marker missing')
q=q.replace(old,new,1)
discover.write_text(q)
print('v9.4.0 Discover loading/offline states applied')

print('v9.4.0 parity batch 1 applied')
