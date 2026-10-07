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
new='''        TextView recentTitle=label("Hot someone to chat",13,0xff5f5964,true);recentTitle.setPadding(dp(18),0,0,0);root.addView(recentTitle,new LinearLayout.LayoutParams(-1,dp(34)));
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


# Me/Profile realtime counters and wallet loading/error states.
main=pkg/'MainActivity.java'
q=main.read_text()
old='''        TextView beans=smallBadge("💎 0",0xfffff7cc,0xff6c5b00);profileTopBalance=beans;beans.setOnClickListener(v->walletPage());top.addView(beans,new LinearLayout.LayoutParams(dp(78),dp(34)));
        TextView coins=smallBadge("◈ 0",0xfffff7cc,0xff6c5b00);profileWalletCoins=coins;coins.setOnClickListener(v->walletPage());'''
new='''        TextView beans=smallBadge("💎 "+Math.max(0,coinBalance),0xfffff7cc,0xff6c5b00);profileTopBalance=beans;beans.setOnClickListener(v->walletPage());top.addView(beans,new LinearLayout.LayoutParams(dp(78),dp(34)));
        TextView coins=smallBadge("◈ "+Math.max(0,coinBalance),0xfffff7cc,0xff6c5b00);profileWalletCoins=coins;coins.setOnClickListener(v->walletPage());'''
if old not in q: raise SystemExit('Me wallet badge marker missing')
q=q.replace(old,new,1)

old='''        LinearLayout counts=new LinearLayout(this);counts.setGravity(Gravity.CENTER);profileFollowersNumber=addProfileStat(counts,"0","Followers",()->peoplePage("Followers"));profileFollowingNumber=addProfileStat(counts,"0","Following",()->peoplePage("Following"));profileFriendsNumber=addProfileStat(counts,"0","Friends",()->peoplePage("Friends"));String coupling=getPreferences(0).getString("coupling_name","");'''
new='''        LinearLayout counts=new LinearLayout(this);counts.setGravity(Gravity.CENTER);profileFollowersNumber=addProfileStat(counts,"…","Followers",()->peoplePage("Followers"));profileFollowingNumber=addProfileStat(counts,"…","Following",()->peoplePage("Following"));profileFriendsNumber=addProfileStat(counts,"…","Friends",()->peoplePage("Friends"));String coupling=getPreferences(0).getString("coupling_name","");'''
if old not in q: raise SystemExit('Me social count marker missing')
q=q.replace(old,new,1)

old='''        firestore.collection("follows").whereEqualTo("followerUid",uid).get().addOnSuccessListener(out->{
            final Set<String> followingSet=new HashSet<>();
            for(DocumentSnapshot d:out.getDocuments()){String x=d.getString("targetUid");if(x!=null)followingSet.add(x);}
            if(profileFollowingNumber!=null)profileFollowingNumber.setText(String.valueOf(followingSet.size()));
            firestore.collection("follows").whereEqualTo("targetUid",uid).get().addOnSuccessListener(in->{
                int followers=0,friends=0;
                for(DocumentSnapshot d:in.getDocuments()){String x=d.getString("followerUid");if(x!=null){followers++;if(followingSet.contains(x))friends++;}}
                if(profileFollowersNumber!=null)profileFollowersNumber.setText(String.valueOf(followers));
                if(profileFriendsNumber!=null)profileFriendsNumber.setText(String.valueOf(friends));
            });
        });
        refreshServerWallet();'''
new='''        firestore.collection("follows").whereEqualTo("followerUid",uid).get().addOnSuccessListener(out->{
            final Set<String> followingSet=new HashSet<>();
            for(DocumentSnapshot d:out.getDocuments()){String x=d.getString("targetUid");if(x!=null)followingSet.add(x);}
            if(profileFollowingNumber!=null)profileFollowingNumber.setText(String.valueOf(followingSet.size()));
            firestore.collection("follows").whereEqualTo("targetUid",uid).get().addOnSuccessListener(in->{
                int followers=0,friends=0;
                for(DocumentSnapshot d:in.getDocuments()){String x=d.getString("followerUid");if(x!=null){followers++;if(followingSet.contains(x))friends++;}}
                if(profileFollowersNumber!=null)profileFollowersNumber.setText(String.valueOf(followers));
                if(profileFriendsNumber!=null)profileFriendsNumber.setText(String.valueOf(friends));
            }).addOnFailureListener(e->{if(profileFollowersNumber!=null)profileFollowersNumber.setText("—");if(profileFriendsNumber!=null)profileFriendsNumber.setText("—");});
        }).addOnFailureListener(e->{if(profileFollowingNumber!=null)profileFollowingNumber.setText("—");if(profileFollowersNumber!=null)profileFollowersNumber.setText("—");if(profileFriendsNumber!=null)profileFriendsNumber.setText("—");});
        refreshServerWallet();'''
if old not in q: raise SystemExit('Me follow load marker missing')
q=q.replace(old,new,1)

old='''    private void refreshServerWallet(){
        if(firestore==null||firebaseAuth==null||firebaseAuth.getCurrentUser()==null)return;
        firestore.collection("wallets").document(firebaseAuth.getCurrentUser().getUid()).get().addOnSuccessListener(doc->{
            Object raw=doc.get("coins"); long coins=raw instanceof Number?Math.max(0,Math.round(((Number)raw).doubleValue())):0;
            coinBalance=(int)Math.min(Integer.MAX_VALUE,coins);
            if(profileTopBalance!=null)profileTopBalance.setText("💎 "+coins);
            if(profileWalletCoins!=null)profileWalletCoins.setText("💎 "+coins);
        });
    }'''
new='''    private void refreshServerWallet(){
        if(profileTopBalance!=null)profileTopBalance.setText("💎 "+Math.max(0,coinBalance));
        if(profileWalletCoins!=null)profileWalletCoins.setText("◈ "+Math.max(0,coinBalance));
        if(firestore==null||firebaseAuth==null||firebaseAuth.getCurrentUser()==null)return;
        firestore.collection("wallets").document(firebaseAuth.getCurrentUser().getUid()).get().addOnSuccessListener(doc->{
            Object raw=doc.get("coins"); long coins=raw instanceof Number?Math.max(0,Math.round(((Number)raw).doubleValue())):Math.max(0,coinBalance);
            coinBalance=(int)Math.min(Integer.MAX_VALUE,coins);
            if(profileTopBalance!=null)profileTopBalance.setText("💎 "+coins);
            if(profileWalletCoins!=null)profileWalletCoins.setText("◈ "+coins);
        }).addOnFailureListener(e->{if(profileTopBalance!=null)profileTopBalance.setText("💎 "+Math.max(0,coinBalance));if(profileWalletCoins!=null)profileWalletCoins.setText("◈ "+Math.max(0,coinBalance));});
    }'''
if old not in q: raise SystemExit('Me wallet refresh marker missing')
q=q.replace(old,new,1)
main.write_text(q)
print('v9.4.0 Me profile realtime loading states applied')


# Deep-flow game shortcuts must use realtime Party multiplayer too.
deep=pkg/'KingDeepFlowActivity.java'
q=deep.read_text()
old='''    private void openGame(String code){if("ludo".equalsIgnoreCase(code)){startActivity(new Intent(this,KingLudoLobbyActivity.class));return;}Intent i=new Intent(this,GamePlayActivity.class);i.putExtra("game",code);startActivity(i);}'''
new='''    private void openGame(String code){
        if("ludo".equalsIgnoreCase(code)){startActivity(new Intent(this,KingLudoLobbyActivity.class));return;}
        if(roomId!=null&&!roomId.trim().isEmpty()){
            Intent i=new Intent(this,RoomGameActivity.class);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);i.putExtra("displayName",safe(mainPrefs.getString("name","KING User"),"KING User"));i.putExtra("requestedGame",code);startActivity(i);return;
        }
        Intent i=new Intent(this,PartyActivity.class);i.putExtra("requestedGame",code);i.putExtra("displayName",safe(mainPrefs.getString("name","KING User"),"KING User"));startActivity(i);
    }'''
if old not in q: raise SystemExit('DeepFlow v9.4 game route marker missing')
q=q.replace(old,new,1)
deep.write_text(q)
print('v9.4.0 DeepFlow games routed to Party multiplayer')

# Party More menu parity: present it as a bottom drawer rather than a centered modal.
party=pkg/'PartyActivity.java'
q=party.read_text()
old='''holder[0]=new AlertDialog.Builder(this).setView(scroll).create();holder[0].setOnShowListener(v->{android.view.Window w=holder[0].getWindow();if(w!=null){w.setBackgroundDrawable(bg(Color.WHITE,18));w.setGravity(Gravity.CENTER);w.setLayout((int)(getResources().getDisplayMetrics().widthPixels*.90f),(int)(getResources().getDisplayMetrics().heightPixels*.88f));}});holder[0].show();'''
new='''holder[0]=new AlertDialog.Builder(this).setView(scroll).create();holder[0].setOnShowListener(v->{android.view.Window w=holder[0].getWindow();if(w!=null){w.setBackgroundDrawable(bg(Color.WHITE,24));w.setGravity(Gravity.BOTTOM);w.setLayout(-1,(int)(getResources().getDisplayMetrics().heightPixels*.72f));android.view.WindowManager.LayoutParams lp=w.getAttributes();lp.dimAmount=.28f;w.setAttributes(lp);w.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);}});holder[0].show();'''
if old not in q: raise SystemExit('Party More centered-dialog marker missing')
q=q.replace(old,new,1)
party.write_text(q)
print('v9.4.0 Party More bottom drawer visual parity applied')


# Real social cards: cloud photo + VIP/level instead of initial-only profile rows.
social=pkg/'SocialActivity.java'
q=social.read_text()
old='''        for (DocumentSnapshot d : docs) { String uid=d.getString("uid"); if(uid==null||uid.isEmpty()||me!=null&&uid.equals(me.getUid())) continue; addPerson(uid, nameOf(d), clean(d.getString("bio"))); shown++; }'''
new='''        for (DocumentSnapshot d : docs) { String uid=d.getString("uid"); if(uid==null||uid.isEmpty()||me!=null&&uid.equals(me.getUid())) continue; addPerson(uid, nameOf(d), clean(d.getString("bio")),clean(d.getString("photoUrl")),d.getLong("level"),d.getLong("vipLevel")); shown++; }'''
if old not in q: raise SystemExit('Social renderProfiles marker missing')
q=q.replace(old,new,1)

old='''        db.collection("public_profiles").document(uid).get().addOnSuccessListener(d->{ if(d.exists())addPerson(uid,nameOf(d),clean(d.getString("bio"))); else addPerson(uid,"KING "+shortUid(uid),""); });'''
new='''        db.collection("public_profiles").document(uid).get().addOnSuccessListener(d->{ if(d.exists())addPerson(uid,nameOf(d),clean(d.getString("bio")),clean(d.getString("photoUrl")),d.getLong("level"),d.getLong("vipLevel")); else addPerson(uid,"KING "+shortUid(uid),"","",null,null); });'''
if old not in q: raise SystemExit('Social loadProfileCard marker missing')
q=q.replace(old,new,1)

old='''    private void addPerson(String uid, String name, String bio) {
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
    }'''
new='''    private void addPerson(String uid, String name, String bio,String photo,Long level,Long vip) {
        LinearLayout card=new LinearLayout(this); card.setGravity(Gravity.CENTER_VERTICAL); card.setPadding(dp(14),dp(10),dp(12),dp(10)); card.setBackground(bg(Color.WHITE,18));
        String initial=(name==null||name.trim().isEmpty())?"K":name.trim().substring(0,1).toUpperCase(Locale.US);
        android.widget.FrameLayout avatarBox=new android.widget.FrameLayout(this);TextView av=label(initial,22,Color.WHITE,true);av.setGravity(Gravity.CENTER);av.setBackground(bg(PURPLE,28));avatarBox.addView(av,new android.widget.FrameLayout.LayoutParams(-1,-1));
        android.widget.ImageView image=new android.widget.ImageView(this);image.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);avatarBox.addView(image,new android.widget.FrameLayout.LayoutParams(-1,-1));if(photo!=null&&!photo.isEmpty())loadSocialPhoto940(image,av,photo);
        card.addView(avatarBox,new LinearLayout.LayoutParams(dp(56),dp(56)));
        LinearLayout info=new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL); info.setPadding(dp(12),0,dp(4),0);
        info.addView(label(name,16,DARK,true),new LinearLayout.LayoutParams(-1,dp(23)));
        LinearLayout badges=new LinearLayout(this);badges.setGravity(Gravity.CENTER_VERTICAL);TextView id=label("ID "+publicId(uid),10,PURPLE,true);badges.addView(id,new LinearLayout.LayoutParams(0,dp(18),1));TextView lv=label("VIP "+(vip==null?0:vip)+"  ·  Lv."+(level==null?1:level),10,0xff7b5c00,true);lv.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);badges.addView(lv,new LinearLayout.LayoutParams(dp(105),dp(18)));info.addView(badges,new LinearLayout.LayoutParams(-1,dp(19)));
        TextView b=label(bio.isEmpty()?"KING Plus member":bio,11,MUTED,false); b.setMaxLines(1); info.addView(b,new LinearLayout.LayoutParams(-1,dp(18)));
        card.addView(info,new LinearLayout.LayoutParams(0,dp(60),1));
        TextView arrow=label("›",28,0xff9993a6,false); card.addView(arrow);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(84)); lp.setMargins(0,dp(5),0,dp(5)); list.addView(card,lp); card.setOnClickListener(v->openProfile(uid,name,bio));
    }
    private void loadSocialPhoto940(android.widget.ImageView image,TextView fallback,String url){
        final String source=url;image.setTag(source);new Thread(()->{try{java.net.URLConnection c=new java.net.URL(source).openConnection();c.setConnectTimeout(7000);c.setReadTimeout(7000);try(java.io.InputStream in=c.getInputStream()){android.graphics.Bitmap bm=android.graphics.BitmapFactory.decodeStream(in);if(bm!=null)runOnUiThread(()->{if(!isFinishing()&&source.equals(image.getTag())){image.setImageBitmap(bm);fallback.setVisibility(View.GONE);}});}}catch(Exception ignored){}}).start();
    }'''
if old not in q: raise SystemExit('Social addPerson marker missing')
q=q.replace(old,new,1)
social.write_text(q)
print('v9.4.0 real social profile card visuals applied')

# Party lobby network auto-recovery after connectivity returns.
party=pkg/'PartyActivity.java'
q=party.read_text()
old='''    private String requestedGame940="";'''
new='''    private String requestedGame940="";
    private android.net.ConnectivityManager.NetworkCallback partyNetworkCallback940;
    private boolean partyLastOnline940;
    private String currentLobbyTab940="Hot";'''
if old not in q: raise SystemExit('Party requestedGame field marker missing for network recovery')
q=q.replace(old,new,1)

old='''        localCoins = prefs.getInt("coins", 2500);'''
new='''        localCoins = prefs.getInt("coins", 2500);
        partyLastOnline940=KingNetwork.online(this);
        partyNetworkCallback940=KingNetwork.watch(this,online->runOnUiThread(()->{
            boolean recovered=online&&!partyLastOnline940;partyLastOnline940=online;
            if(recovered&&roomId==null&&!isFinishing()&&!isDestroyed())renderLobby(currentLobbyTab940);
        }));'''
if old not in q: raise SystemExit('Party localCoins marker missing for network recovery')
q=q.replace(old,new,1)

old='''    private void renderLobby(String selected) {
        clearListeners(); unregisterMember(); roomId = null; cloudRoom = false;'''
new='''    private void renderLobby(String selected) {
        currentLobbyTab940=selected==null||selected.trim().isEmpty()?"Hot":selected;
        clearListeners(); unregisterMember(); roomId = null; cloudRoom = false;'''
if old not in q: raise SystemExit('Party renderLobby marker missing for network recovery')
q=q.replace(old,new,1)

old='''    @Override protected void onDestroy(){stopInRoomVoice940(true);unregisterMember();clearListeners();try{if(roomMusicPlayer!=null){roomMusicPlayer.release();roomMusicPlayer=null;}}catch(Exception ignored){}org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostDestroy(this);super.onDestroy();}'''
new='''    @Override protected void onDestroy(){KingNetwork.unwatch(this,partyNetworkCallback940);partyNetworkCallback940=null;stopInRoomVoice940(true);unregisterMember();clearListeners();try{if(roomMusicPlayer!=null){roomMusicPlayer.release();roomMusicPlayer=null;}}catch(Exception ignored){}org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostDestroy(this);super.onDestroy();}'''
if old not in q: raise SystemExit('Party onDestroy marker missing for network recovery')
q=q.replace(old,new,1)
party.write_text(q)
print('v9.4.0 Party lobby network auto-recovery applied')


# Party member strip: real cloud avatars + equipped frames, not initials only.
party=pkg/'PartyActivity.java'
q=party.read_text()
old='''    private void rebuildMemberStrip() {
        if(memberStripBox==null)return;
        memberStripBox.removeAllViews();
        List<String> names=new ArrayList<>();
        if(ownerName!=null&&!ownerName.trim().isEmpty())names.add(ownerName);
        if(cloudRoom){for(String n:memberNames)if(n!=null&&!n.trim().isEmpty()&&!names.contains(n))names.add(n);}
        if(names.isEmpty())names.add("Host");
        int shown=0;
        for(String n:names){
            if(shown++>=6)break;
            String initial=n.trim().isEmpty()?"?":n.trim().substring(0,1).toUpperCase();
            TextView av=tv(initial,12,Color.WHITE,true); av.setGravity(Gravity.CENTER); av.setBackground(bg(0xff5b3a78,40));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(34),dp(34)); lp.setMargins(0,0,dp(5),0); memberStripBox.addView(av,lp);
        }
        int extra=Math.max(0,names.size()-6);TextView label=tv(cloudRoom?("Members "+liveMemberCount+(extra>0?" • +"+extra:"")):"Host",11,MUTED,false); memberStripBox.addView(label,new LinearLayout.LayoutParams(0,dp(34),1));
    }'''
new='''    private void rebuildMemberStrip() {
        if(memberStripBox==null)return;
        memberStripBox.removeAllViews();
        List<String> names=new ArrayList<>();List<String> uids=new ArrayList<>();
        if(ownerName!=null&&!ownerName.trim().isEmpty()){names.add(ownerName);uids.add(ownerUid==null?"":ownerUid);}
        if(cloudRoom){for(String n:memberNames){if(n==null||n.trim().isEmpty()||names.contains(n))continue;names.add(n);String uid=memberUids.get(n);uids.add(uid==null?"":uid);}}
        if(names.isEmpty()){names.add("Host");uids.add("");}
        int shown=0;
        for(int i=0;i<names.size()&&shown<6;i++,shown++){
            String n=names.get(i),uid=i<uids.size()?uids.get(i):"";
            String initial=n.trim().isEmpty()?"?":n.trim().substring(0,1).toUpperCase();
            TextView fallback=tv(initial,12,Color.WHITE,true);fallback.setGravity(Gravity.CENTER);fallback.setBackground(bg(0xff5b3a78,40));
            FrameLayout frame=new FrameLayout(this);String equipped=memberFrame730.get(uid);if(equipped==null||equipped.trim().isEmpty())equipped="Minimal Frame";frame.setBackground(KingCosmetics.avatarFrame(this,equipped,uid!=null&&!uid.isEmpty()&&uid.equals(ownerUid)));frame.setPadding(dp(2),dp(2),dp(2),dp(2));
            frame.addView(fallback,new FrameLayout.LayoutParams(-1,-1));ImageView photo=new ImageView(this);photo.setScaleType(ImageView.ScaleType.CENTER_CROP);frame.addView(photo,new FrameLayout.LayoutParams(-1,-1));
            String photoUrl=memberPhotos540.get(uid);if(user!=null&&uid!=null&&uid.equals(user.getUid())){String own=cloudProfilePhoto868();if(own.isEmpty()&&user.getPhotoUrl()!=null)own=user.getPhotoUrl().toString();photoUrl=own;}if(photoUrl!=null&&!photoUrl.isEmpty())applyProfilePhoto868(photo,fallback,photoUrl);
            final String tapUid=uid, tapName=n;frame.setOnClickListener(v->{if(tapUid!=null&&!tapUid.isEmpty())memberProfileDialog(tapUid,tapName);});
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(36),dp(36));lp.setMargins(0,0,dp(5),0);memberStripBox.addView(frame,lp);
        }
        int extra=Math.max(0,names.size()-6);TextView label=tv(cloudRoom?("Members "+liveMemberCount+(extra>0?" • +"+extra:"")):"Host",11,MUTED,false);memberStripBox.addView(label,new LinearLayout.LayoutParams(0,dp(36),1));
    }'''
if old not in q: raise SystemExit('Party member strip marker missing')
q=q.replace(old,new,1)
party.write_text(q)
print('v9.4.0 Party member strip cloud avatar parity applied')

# Discover and Messages auto-recover when validated network connectivity returns.
discover=pkg/'DiscoverActivity.java'
q=discover.read_text()
old='''    private String activePeopleTab="Recommended";'''
new='''    private String activePeopleTab="Recommended";
    private android.net.ConnectivityManager.NetworkCallback discoverNetworkCallback940;
    private boolean discoverLastOnline940;'''
if old not in q: raise SystemExit('Discover active tab marker missing for auto recovery')
q=q.replace(old,new,1)
old='''        publishStats();
        renderDiscover();
    }'''
new='''        publishStats();
        renderDiscover();
        discoverLastOnline940=KingNetwork.online(this);
        discoverNetworkCallback940=KingNetwork.watch(this,online->runOnUiThread(()->{
            boolean recovered=online&&!discoverLastOnline940;discoverLastOnline940=online;
            if(recovered&&!isFinishing()&&!isDestroyed())renderDiscover();
        }));
    }'''
if old not in q: raise SystemExit('Discover onCreate tail marker missing for auto recovery')
q=q.replace(old,new,1)
old='''    @Override public void onBackPressed(){KingNav.confirmExit(this);}
}'''
new='''    @Override public void onBackPressed(){KingNav.confirmExit(this);}
    @Override protected void onDestroy(){KingNetwork.unwatch(this,discoverNetworkCallback940);discoverNetworkCallback940=null;super.onDestroy();}
}'''
if old not in q: raise SystemExit('Discover lifecycle tail marker missing for auto recovery')
q=q.replace(old,new,1)
discover.write_text(q)

inbox=pkg/'InboxActivity.java'
q=inbox.read_text()
old='''    private String inboxState940="";'''
new='''    private String inboxState940="";
    private android.net.ConnectivityManager.NetworkCallback inboxNetworkCallback940;
    private boolean inboxLastOnline940;'''
if old not in q: raise SystemExit('Inbox state field marker missing for auto recovery')
q=q.replace(old,new,1)
old='''        renderShell();
        loadInbox();
    }'''
new='''        renderShell();
        loadInbox();
        inboxLastOnline940=KingNetwork.online(this);
        inboxNetworkCallback940=KingNetwork.watch(this,online->runOnUiThread(()->{
            boolean recovered=online&&!inboxLastOnline940;inboxLastOnline940=online;
            if(recovered&&!isFinishing()&&!isDestroyed()){if(threadListener!=null){threadListener.remove();threadListener=null;}loadInbox();}
            else if(!online&&!isFinishing()&&!isDestroyed()){inboxState940="Offline • showing recent local conversations";renderModels(models);}
        }));
    }'''
if old not in q: raise SystemExit('Inbox onCreate tail marker missing for auto recovery')
q=q.replace(old,new,1)
old='''    @Override protected void onDestroy(){if(threadListener!=null)threadListener.remove();super.onDestroy();}'''
new='''    @Override protected void onDestroy(){KingNetwork.unwatch(this,inboxNetworkCallback940);inboxNetworkCallback940=null;if(threadListener!=null)threadListener.remove();super.onDestroy();}'''
if old not in q: raise SystemExit('Inbox onDestroy marker missing for auto recovery')
q=q.replace(old,new,1)
inbox.write_text(q)
print('v9.4.0 Discover and Messages network auto-recovery applied')


# Messages system channels: load real backend notification feed and make actionable invites/messages/follows.
inbox=pkg/'InboxActivity.java'
q=inbox.read_text()
old='''    private void addSystemRows940(){addSystemRow940("🔔","Interactive notifications","Follows, gifts and room activity",this::showNotifications);addSystemRow940("👑","KING Official","Safety, events and app notices",this::showNotifications);addSystemRow940("🎤","Room invitations","Party and game invitations",this::showNotifications);}'''
new='''    private void addSystemRows940(){addSystemRow940("🔔","Interactive notifications","Follows, gifts and room activity",()->showCloudNotifications940(""));addSystemRow940("👑","KING Official","Safety, events and app notices",()->showCloudNotifications940(""));addSystemRow940("🎤","Room invitations","Party and game invitations",()->showCloudNotifications940("room_invite"));}'''
if old not in q: raise SystemExit('Inbox system row actions marker missing')
q=q.replace(old,new,1)

old='''    private void showNotifications(){
        String[] items={"Follow activity","Likes & gifts","Room invitations","KING Plus updates"};
        new AlertDialog.Builder(this).setTitle("Notifications").setItems(items,(d,w)->Toast.makeText(this,items[w],Toast.LENGTH_SHORT).show()).setNegativeButton("Close",null).show();
    }'''
new='''    private void showNotifications(){showCloudNotifications940("");}
    private void showCloudNotifications940(String filter){
        if(me==null||db==null){new AlertDialog.Builder(this).setTitle("Notifications").setMessage("Sign in to load your real KING Plus notifications.").setPositiveButton("OK",null).show();return;}
        db.collection("notifications").document(me.getUid()).collection("items").orderBy("createdAt",com.google.firebase.firestore.Query.Direction.DESCENDING).limit(50).get()
            .addOnSuccessListener(snap->{
                List<DocumentSnapshot> docs=new ArrayList<>();List<String> rows=new ArrayList<>();
                for(DocumentSnapshot n:snap.getDocuments()){
                    Object dataObj=n.get("data");String type="";if(dataObj instanceof Map){Object t=((Map<?,?>)dataObj).get("type");if(t!=null)type=String.valueOf(t);}
                    if(filter!=null&&!filter.isEmpty()&&!filter.equals(type))continue;
                    docs.add(n);String title=n.getString("title"),body=n.getString("body");Timestamp at=n.getTimestamp("createdAt");
                    String when=formatTime(at);boolean unread=!Boolean.TRUE.equals(n.getBoolean("read"));rows.add((unread?"● ":"")+(title==null?"KING Plus":title)+(when.isEmpty()?"":"  •  "+when)+"\\n"+(body==null?"":body));
                }
                if(rows.isEmpty()){new AlertDialog.Builder(this).setTitle(filter!=null&&!filter.isEmpty()?"Room invitations":"Notifications").setMessage(filter!=null&&!filter.isEmpty()?"No room invitations yet.":"No notifications yet.").setPositiveButton("OK",null).show();return;}
                new AlertDialog.Builder(this).setTitle(filter!=null&&!filter.isEmpty()?"Room invitations":"Notifications").setItems(rows.toArray(new String[0]),(d,w)->openNotification940(docs.get(w))).setNegativeButton("Close",null).show();
            }).addOnFailureListener(e->new AlertDialog.Builder(this).setTitle("Notifications").setMessage("Notification feed is unavailable right now.").setPositiveButton("Retry",(d,w)->showCloudNotifications940(filter)).setNegativeButton("Close",null).show());
    }
    private void openNotification940(DocumentSnapshot n){
        if(n!=null&&!Boolean.TRUE.equals(n.getBoolean("read")))n.getReference().update("read",true).addOnFailureListener(e->{});
        Object dataObj=n.get("data");if(!(dataObj instanceof Map)){return;}Map<?,?> data=(Map<?,?>)dataObj;String type=data.get("type")==null?"":String.valueOf(data.get("type"));
        if("room_invite".equals(type)){
            String rid=data.get("roomId")==null?"":String.valueOf(data.get("roomId"));if(rid.isEmpty()){Toast.makeText(this,"Room invite is missing its room ID",Toast.LENGTH_SHORT).show();return;}
            db.collection("live_rooms").document(rid).get().addOnSuccessListener(room->{if(room==null||!room.exists()||Boolean.TRUE.equals(room.getBoolean("closed"))){Toast.makeText(this,"This Party room is no longer available",Toast.LENGTH_SHORT).show();return;}Intent i=new Intent(this,PartyActivity.class);i.putExtra("directRoomId",rid);i.putExtra("directRoomName",room.getString("name"));i.putExtra("directOwnerUid",room.getString("ownerUid"));i.putExtra("directOwnerName",room.getString("ownerName"));i.putExtra("directPrivate",Boolean.TRUE.equals(room.getBoolean("isPrivate")));i.putExtra("directPassword",Boolean.TRUE.equals(room.getBoolean("hasPassword")));startActivity(i);});
        }else if("direct_message".equals(type)){
            String uid=data.get("senderUid")==null?"":String.valueOf(data.get("senderUid"));String name=n.getString("title");openChat(name==null?"KING Friend":name,uid);
        }else if("follow".equals(type)){
            String uid=data.get("followerUid")==null?"":String.valueOf(data.get("followerUid"));if(uid.isEmpty())return;Intent i=new Intent(this,KingPublicProfileActivity.class);i.putExtra("uid",uid);startActivity(i);
        }else Toast.makeText(this,n.getString("body")==null?"KING Plus notification":n.getString("body"),Toast.LENGTH_SHORT).show();
    }'''
if old not in q: raise SystemExit('Inbox static notification marker missing')
q=q.replace(old,new,1)
inbox.write_text(q)
print('v9.4.0 real Firebase notification feed applied')

# Queue failed direct text messages and flush them deterministically after reconnect.
chat=pkg/'ChatActivity.java'
q=chat.read_text()
old='''    private boolean cloudMode;'''
new='''    private boolean cloudMode;
    private android.net.ConnectivityManager.NetworkCallback chatNetworkCallback940;
    private boolean chatLastOnline940;'''
if old not in q: raise SystemExit('Chat cloudMode marker missing for retry queue')
q=q.replace(old,new,1)

old='''        if (cloudMode) listenCloud(); else loadLocal();
    }'''
new='''        if (cloudMode) listenCloud(); else loadLocal();
        chatLastOnline940=KingNetwork.online(this);
        chatNetworkCallback940=KingNetwork.watch(this,online->runOnUiThread(()->{
            boolean recovered=online&&!chatLastOnline940;chatLastOnline940=online;
            if(recovered&&cloudMode&&!isFinishing()&&!isDestroyed()){status.setText("Back online • syncing pending messages…");flushPendingText940();}
        }));
        if(cloudMode&&chatLastOnline940)flushPendingText940();
    }'''
if old not in q: raise SystemExit('Chat onCreate tail marker missing for retry queue')
q=q.replace(old,new,1)

old='''.addOnFailureListener(e -> {status.setText("Offline • message saved on this phone");saveLocal(text,type,mediaUrl,true);});'''
new='''.addOnFailureListener(e -> {status.setText("Offline • message saved on this phone");if("text".equals(type))queuePendingText940(text);saveLocal(text,type,mediaUrl,true);});'''
if q.count(old)<1: raise SystemExit('Chat message add failure marker missing for retry queue')
q=q.replace(old,new,1)
old='''}).addOnFailureListener(e -> {status.setText("Offline • message saved on this phone");saveLocal(text,type,mediaUrl,true);});'''
new='''}).addOnFailureListener(e -> {status.setText("Offline • message saved on this phone");if("text".equals(type))queuePendingText940(text);saveLocal(text,type,mediaUrl,true);});'''
if old not in q: raise SystemExit('Chat thread failure marker missing for retry queue')
q=q.replace(old,new,1)

marker2='''    private void saveLocal(String text, String type, String media, boolean mine) {'''
helpers='''    private String pendingKey940(){return "pending_cloud_"+safeKey(chatId==null?peerName:chatId);}
    private void queuePendingText940(String text){
        if(text==null||text.trim().isEmpty())return;
        try{
            SharedPreferences p=getSharedPreferences("chat_store",MODE_PRIVATE);JSONArray a=new JSONArray(p.getString(pendingKey940(),"[]"));
            JSONObject o=new JSONObject();o.put("id",java.util.UUID.randomUUID().toString().replace("-",""));o.put("text",text);o.put("ts",System.currentTimeMillis());a.put(o);
            while(a.length()>100){JSONArray n=new JSONArray();for(int i=1;i<a.length();i++)n.put(a.get(i));a=n;}
            p.edit().putString(pendingKey940(),a.toString()).apply();
        }catch(Exception ignored){}
    }
    private void flushPendingText940(){
        if(!cloudMode||db==null||me==null||!KingNetwork.online(this))return;
        try{
            SharedPreferences p=getSharedPreferences("chat_store",MODE_PRIVATE);JSONArray a=new JSONArray(p.getString(pendingKey940(),"[]"));if(a.length()==0){if(status!=null)status.setText("Realtime chat");return;}
            JSONObject o=a.getJSONObject(0);String id=o.optString("id",""),text=o.optString("text","");if(id.isEmpty()||text.isEmpty()){removePendingHead940(a,p);flushPendingText940();return;}
            Map<String,Object> thread=new HashMap<>();List<String> members=new ArrayList<>();members.add(me.getUid());members.add(peerUid);thread.put("members",members);Map<String,Object> names=new HashMap<>();names.put(me.getUid(),myName);names.put(peerUid,peerName);thread.put("memberNames",names);thread.put("lastMessage",text);thread.put("lastSenderUid",me.getUid());thread.put("updatedAt",FieldValue.serverTimestamp());
            db.collection("direct_threads").document(chatId).set(thread,SetOptions.merge()).addOnSuccessListener(v->{
                Map<String,Object> msg=new HashMap<>();msg.put("senderUid",me.getUid());msg.put("recipientUid",peerUid);msg.put("senderName",myName);msg.put("text",text);msg.put("type","text");msg.put("clientId",id);msg.put("createdAt",FieldValue.serverTimestamp());
                db.collection("direct_threads").document(chatId).collection("messages").document("q_"+id).set(msg).addOnSuccessListener(x->{removePendingHead940(a,p);CloudBackend.sendDirectMessageNotification(peerUid,myName,text,(ok,m)->{});flushPendingText940();}).addOnFailureListener(x->{if(status!=null)status.setText("Pending message will retry");});
            }).addOnFailureListener(x->{if(status!=null)status.setText("Pending message will retry");});
        }catch(Exception ignored){}
    }
    private void removePendingHead940(JSONArray a,SharedPreferences p){
        try{JSONArray n=new JSONArray();for(int i=1;i<a.length();i++)n.put(a.get(i));p.edit().putString(pendingKey940(),n.toString()).apply();}catch(Exception ignored){}
    }

'''
if marker2 not in q: raise SystemExit('Chat saveLocal marker missing for queue helpers')
q=q.replace(marker2,helpers+marker2,1)

old='''    @Override protected void onDestroy() { if(messagesListener!=null)messagesListener.remove(); if(threadListener!=null)threadListener.remove(); stopVoice(false); super.onDestroy(); }'''
new='''    @Override protected void onDestroy() { KingNetwork.unwatch(this,chatNetworkCallback940);chatNetworkCallback940=null;if(messagesListener!=null)messagesListener.remove(); if(threadListener!=null)threadListener.remove(); stopVoice(false); super.onDestroy(); }'''
if old not in q: raise SystemExit('Chat onDestroy marker missing for retry queue')
q=q.replace(old,new,1)
chat.write_text(q)
print('v9.4.0 direct text pending retry queue applied')

# Dedicated in-app Multi Video page using the same Party room conference.
multi=pkg/'KingMultiVideoActivity.java'
multi.write_text(r'''package com.kingplus.social;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class KingMultiVideoActivity extends androidx.fragment.app.FragmentActivity implements org.jitsi.meet.sdk.JitsiMeetActivityInterface {
    private org.jitsi.meet.sdk.JitsiMeetView meetView;
    private String roomId="",roomName="KING Plus Multi Video",displayName="KING User";

    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    private TextView tv(String s,int z,int c,boolean b){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);if(b)t.setTypeface(null,Typeface.BOLD);return t;}
    private String safe(String s,String f){return s==null||s.trim().isEmpty()?f:s.trim();}

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        roomId=safe(getIntent().getStringExtra("roomId"),"lobby");
        roomName=safe(getIntent().getStringExtra("roomName"),"KING Plus Multi Video");
        displayName=safe(getIntent().getStringExtra("displayName"),"KING User");

        FrameLayout shell=new FrameLayout(this);shell.setBackgroundColor(0xff0d0b15);
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);shell.addView(body,new FrameLayout.LayoutParams(-1,-1));

        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(10),0,dp(12),0);head.setBackgroundColor(0xff171322);
        TextView back=tv("‹",38,Color.WHITE,false);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->finish());head.addView(back,new LinearLayout.LayoutParams(dp(50),dp(58)));
        LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);TextView title=tv(roomName,17,Color.WHITE,true);TextView sub=tv("📹 Multi Video • KING Plus Party",11,0xffc7bdd5,true);info.addView(title,new LinearLayout.LayoutParams(-1,dp(30)));info.addView(sub,new LinearLayout.LayoutParams(-1,dp(22)));head.addView(info,new LinearLayout.LayoutParams(0,dp(58),1));
        TextView leave=tv("Leave",12,Color.WHITE,true);leave.setGravity(Gravity.CENTER);leave.setBackground(bg(0xff7b3fd0,18));leave.setOnClickListener(v->finish());head.addView(leave,new LinearLayout.LayoutParams(dp(72),dp(38)));
        body.addView(head,new LinearLayout.LayoutParams(-1,dp(62)));

        FrameLayout stage=new FrameLayout(this);body.addView(stage,new LinearLayout.LayoutParams(-1,0,1));
        TextView loading=tv("Connecting Multi Video…",14,0xffcfc8da,true);loading.setGravity(Gravity.CENTER);stage.addView(loading,new FrameLayout.LayoutParams(-1,-1));

        try{
            org.jitsi.meet.sdk.JitsiMeet.instantiateReactNative(this);
            meetView=new org.jitsi.meet.sdk.JitsiMeetView(this);
            stage.addView(meetView,new FrameLayout.LayoutParams(-1,-1));
            String slug=("KINGPlus-"+roomId).replaceAll("[^A-Za-z0-9_-]","");
            org.jitsi.meet.sdk.JitsiMeetUserInfo userInfo=new org.jitsi.meet.sdk.JitsiMeetUserInfo();userInfo.setDisplayName(displayName);
            org.jitsi.meet.sdk.JitsiMeetConferenceOptions options=new org.jitsi.meet.sdk.JitsiMeetConferenceOptions.Builder()
                .setServerURL(new java.net.URL("https://meet.jit.si"))
                .setRoom(slug)
                .setSubject(roomName)
                .setAudioMuted(false)
                .setVideoMuted(false)
                .setUserInfo(userInfo)
                .setFeatureFlag("welcomepage.enabled",false)
                .setFeatureFlag("prejoinpage.enabled",false)
                .setFeatureFlag("invite.enabled",false)
                .setFeatureFlag("chat.enabled",false)
                .setFeatureFlag("recording.enabled",false)
                .setFeatureFlag("live-streaming.enabled",false)
                .setFeatureFlag("pip.enabled",true)
                .build();
            meetView.join(options);loading.setVisibility(View.GONE);
        }catch(Throwable e){loading.setText("Multi Video could not start • tap back and retry");}
        setContentView(shell);
    }

    @Override protected void onResume(){super.onResume();org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostResume(this);}
    @Override protected void onStop(){org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostPause(this);super.onStop();}
    @Override public void onNewIntent(Intent intent){super.onNewIntent(intent);org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onNewIntent(intent);}
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onActivityResult(this,requestCode,resultCode,data);}
    @Override public void requestPermissions(String[] permissions,int requestCode,com.facebook.react.modules.core.PermissionListener listener){org.jitsi.meet.sdk.JitsiMeetActivityDelegate.requestPermissions(this,permissions,requestCode,listener);}
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onRequestPermissionsResult(requestCode,permissions,grantResults);}
    @Override protected void onDestroy(){try{if(meetView!=null){meetView.dispose();meetView=null;}}catch(Throwable ignored){}org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostDestroy(this);super.onDestroy();}
}''')

manifest=root/'app/src/main/AndroidManifest.xml'
q=manifest.read_text()
old='''        <activity android:name=".PartyActivity" android:exported="false" android:windowSoftInputMode="adjustResize" />'''
new='''        <activity android:name=".PartyActivity" android:exported="false" android:windowSoftInputMode="adjustResize" />
        <activity android:name=".KingMultiVideoActivity" android:exported="false" />'''
if '.KingMultiVideoActivity' not in q:
    if old not in q: raise SystemExit('manifest Party marker missing for Multi Video')
    q=q.replace(old,new,1)
manifest.write_text(q)

party=pkg/'PartyActivity.java'
q=party.read_text()
old='''    private String currentLobbyTab940="Hot";'''
new='''    private String currentLobbyTab940="Hot";
    private FrameLayout partyShell940;'''
if old not in q: raise SystemExit('Party lobby field marker missing for Multi Video')
q=q.replace(old,new,1)

old='''        attachInRoomVoice940(shell);'''
new='''        partyShell940=shell;
        attachInRoomVoice940(shell);'''
if old not in q: raise SystemExit('Party attach inline voice marker missing for Multi Video')
q=q.replace(old,new,1)

old='''    private void openVideoRoom(){NativeMeetBridge.launch(this,roomId,roomName,safeName(),true);}'''
new='''    private void openVideoRoom(){
        if(!cloudRoom||roomId==null||roomId.trim().isEmpty()){toast("Join a live Party room first");return;}
        stopInRoomVoice940(true);
        Intent i=new Intent(this,KingMultiVideoActivity.class);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);i.putExtra("displayName",safeName());startActivity(i);
    }'''
if old not in q: raise SystemExit('Party openVideoRoom marker missing')
q=q.replace(old,new,1)

old='''        setInlineAudioMuted940(!(micOn&&mySeat>0));
        if(seatsBox!=null)rebuildSeats();'''
new='''        if(cloudRoom&&roomId!=null&&partyShell940!=null&&!inRoomVoiceJoined940)attachInRoomVoice940(partyShell940);
        setInlineAudioMuted940(!(micOn&&mySeat>0));
        if(seatsBox!=null)rebuildSeats();'''
if old not in q: raise SystemExit('Party onResume inline voice marker missing for video return')
q=q.replace(old,new,1)
party.write_text(q)
print('v9.4.0 dedicated in-app Multi Video room applied')

# Expose Multi Video as a first-class Party lobby category.
party=pkg/'PartyActivity.java'
q=party.read_text()
old='''        String[] names = {"Hot","Event","Date","Music","Game"};'''
new='''        String[] names = {"Hot","Event","Date","Music","Game","Video"};'''
if old not in q: raise SystemExit('Party lobby tabs marker missing for Video')
q=q.replace(old,new,1)
old='''hot.setOnClickListener(v->{if(user!=null&&db!=null){pendingCreateCategory=selected;createRoomDialog();}else requireSignInForCreate();});'''
new='''hot.setOnClickListener(v->{if(user!=null&&db!=null){pendingCreateCategory="Video".equalsIgnoreCase(selected)?"Multi Video":selected;createRoomDialog();}else requireSignInForCreate();});'''
if old not in q: raise SystemExit('Party lobby create marker missing for Video')
q=q.replace(old,new,1)
party.write_text(q)
print('v9.4.0 Multi Video lobby entrance applied')


# Server Firestore triggers are the single source for direct-message/follow notifications.
chat=pkg/'ChatActivity.java'
q=chat.read_text()
q=q.replace('''                    .addOnSuccessListener(r -> {status.setText("Realtime chat");CloudBackend.sendDirectMessageNotification(peerUid, myName, text, (ok,m)->{});})''','''                    .addOnSuccessListener(r -> {status.setText("Realtime chat");})''',1)
q=q.replace('''db.collection("direct_threads").document(chatId).collection("messages").add(msg).addOnSuccessListener(r->CloudBackend.sendDirectMessageNotification(peerUid,myName,"🎁 "+name+" ×"+qty,(ok,m)->{})).addOnFailureListener''','''db.collection("direct_threads").document(chatId).collection("messages").add(msg).addOnSuccessListener(r->{}).addOnFailureListener''',1)
q=q.replace('''removePendingHead940(a,p);CloudBackend.sendDirectMessageNotification(peerUid,myName,text,(ok,m)->{});flushPendingText940();''','''removePendingHead940(a,p);flushPendingText940();''',1)
chat.write_text(q)

social=pkg/'SocialActivity.java'
q=social.read_text().replace('''            CloudBackend.sendFollowNotification(uid,myName,(ok,m)->{});\n''','',1)
social.write_text(q)

pub=pkg/'KingPublicProfileActivity.java'
q=pub.read_text().replace('''CloudBackend.sendFollowNotification(uid,displayName(),(ok,msg)->{});''','',1)
pub.write_text(q)

discover=pkg/'DiscoverActivity.java'
q=discover.read_text().replace('''CloudBackend.sendFollowNotification(uid,displayName,(ok,m)->{});''','',1)
discover.write_text(q)

party=pkg/'PartyActivity.java'
q=party.read_text().replace('''CloudBackend.sendFollowNotification(uid,safeName(),(ok,m)->{});''','',1)
party.write_text(q)
print('v9.4.0 duplicate client push calls removed')


# Direct chat header parity: real peer avatar, canonical name and VIP/level.
chat=pkg/'ChatActivity.java'
q=chat.read_text()
old='''    private TextView status;'''
new='''    private TextView status;
    private TextView peerNameView940;
    private TextView peerAvatarFallback940;
    private android.widget.ImageView peerAvatarImage940;'''
if old not in q: raise SystemExit('Chat status field marker missing for profile header')
q=q.replace(old,new,1)

old='''        render();
        if (cloudMode) listenCloud(); else loadLocal();'''
new='''        render();
        loadPeerHeader940();
        if (cloudMode) listenCloud(); else loadLocal();'''
if old not in q: raise SystemExit('Chat render marker missing for peer header')
q=q.replace(old,new,1)

old='''        TextView avatar = label("●", 26, PURPLE, true); avatar.setGravity(Gravity.CENTER); avatar.setBackground(bg(0xffefe8ff, 28)); head.addView(avatar, new LinearLayout.LayoutParams(dp(50), dp(50)));
        LinearLayout info = new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL); info.setPadding(dp(10), 0, 0, 0);
        TextView n = label(peerName, 17, DARK, true); info.addView(n); status = label(cloudMode ? "Connecting…" : "Local / test chat", 12, 0xff777186, false); info.addView(status);
        head.addView(info, new LinearLayout.LayoutParams(0, dp(54), 1));'''
new='''        android.widget.FrameLayout avatarBox940=new android.widget.FrameLayout(this);peerAvatarFallback940=label(peerName==null||peerName.trim().isEmpty()?"K":peerName.trim().substring(0,1).toUpperCase(Locale.US),20,Color.WHITE,true);peerAvatarFallback940.setGravity(Gravity.CENTER);peerAvatarFallback940.setBackground(bg(PURPLE,28));avatarBox940.addView(peerAvatarFallback940,new android.widget.FrameLayout.LayoutParams(-1,-1));peerAvatarImage940=new android.widget.ImageView(this);peerAvatarImage940.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);avatarBox940.addView(peerAvatarImage940,new android.widget.FrameLayout.LayoutParams(-1,-1));avatarBox940.setOnClickListener(v->openPeerProfile940());head.addView(avatarBox940,new LinearLayout.LayoutParams(dp(50),dp(50)));
        LinearLayout info = new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL); info.setPadding(dp(10), 0, 0, 0);info.setOnClickListener(v->openPeerProfile940());
        peerNameView940 = label(peerName, 17, DARK, true); info.addView(peerNameView940); status = label(cloudMode ? "Connecting…" : "Local / test chat", 12, 0xff777186, false); info.addView(status);
        head.addView(info, new LinearLayout.LayoutParams(0, dp(54), 1));'''
if old not in q: raise SystemExit('Chat header avatar marker missing')
q=q.replace(old,new,1)

marker='''    private void addTool(LinearLayout row, String text, Runnable action) {'''
helpers='''    private void openPeerProfile940(){
        if(peerUid==null||peerUid.trim().isEmpty())return;
        Intent i=new Intent(this,KingPublicProfileActivity.class);i.putExtra("uid",peerUid);i.putExtra("name",peerName);startActivity(i);
    }
    private void loadPeerHeader940(){
        if(!cloudMode||db==null||peerUid==null||peerUid.isEmpty())return;
        db.collection("public_profiles").document(peerUid).get().addOnSuccessListener(p->{
            if(p==null||!p.exists())return;
            String cloudName=clean(p.getString("displayName"),peerName);if(!cloudName.isEmpty()){peerName=cloudName;if(peerNameView940!=null)peerNameView940.setText(cloudName);}
            Long vip=p.getLong("vipLevel"),lv=p.getLong("level");if(peerNameView940!=null&&(vip!=null||lv!=null))peerNameView940.setText(cloudName+"   VIP "+(vip==null?0:vip)+" · Lv."+(lv==null?1:lv));
            String photo=clean(p.getString("photoUrl"),"");if(!photo.isEmpty()&&peerAvatarImage940!=null&&peerAvatarFallback940!=null)loadPeerPhoto940(photo);
        }).addOnFailureListener(e->{});
    }
    private void loadPeerPhoto940(String url){
        final String source=url;peerAvatarImage940.setTag(source);new Thread(()->{try{java.net.URLConnection c=new java.net.URL(source).openConnection();c.setConnectTimeout(7000);c.setReadTimeout(7000);try(java.io.InputStream in=c.getInputStream()){android.graphics.Bitmap bm=android.graphics.BitmapFactory.decodeStream(in);if(bm!=null)runOnUiThread(()->{if(!isFinishing()&&peerAvatarImage940!=null&&source.equals(peerAvatarImage940.getTag())){peerAvatarImage940.setImageBitmap(bm);if(peerAvatarFallback940!=null)peerAvatarFallback940.setVisibility(View.GONE);}});}}catch(Exception ignored){}}).start();
    }

'''
if marker not in q: raise SystemExit('Chat addTool marker missing for peer header helpers')
q=q.replace(marker,helpers+marker,1)
chat.write_text(q)
print('v9.4.0 real direct-chat peer header applied')


# Party lobby room counts stay realtime and listener-safe.
party=pkg/'PartyActivity.java'
q=party.read_text()
old='''    private FrameLayout partyShell940;'''
new='''    private FrameLayout partyShell940;
    private final List<ListenerRegistration> lobbyMemberCountListeners940=new ArrayList<>();'''
if old not in q: raise SystemExit('Party shell field marker missing for live room counts')
q=q.replace(old,new,1)

old='''    private void clearListeners() {
        stopHeartbeat900();'''
new='''    private void clearListeners() {
        stopHeartbeat900();
        for(ListenerRegistration l940:new ArrayList<>(lobbyMemberCountListeners940)){try{if(l940!=null)l940.remove();}catch(Exception ignored){}}
        lobbyMemberCountListeners940.clear();'''
if old not in q: raise SystemExit('Party clearListeners marker missing for live room counts')
q=q.replace(old,new,1)

old='''        if(db!=null&&id!=null&&!id.isEmpty())db.collection("live_rooms").document(id).collection("members").get().addOnSuccessListener(x->count.setText(String.valueOf(Math.max(1,x.size())))).addOnFailureListener(e->count.setText("LIVE"));'''
new='''        if(db!=null&&id!=null&&!id.isEmpty()){ListenerRegistration countListener940=db.collection("live_rooms").document(id).collection("members").addSnapshotListener((x,e)->{if(e!=null||x==null){count.setText("LIVE");return;}count.setText(String.valueOf(x.size()));});lobbyMemberCountListeners940.add(countListener940);}'''
if old not in q: raise SystemExit('Party room one-shot member count marker missing')
q=q.replace(old,new,1)
party.write_text(q)
print('v9.4.0 realtime lobby member counts applied')


# Public profile: real Followers / Following / Friends counters.
pub=pkg/'KingPublicProfileActivity.java'
q=pub.read_text()
old='''    private FirebaseFirestore db; private FirebaseUser me; private String uid="", name="KING User"; private LinearLayout body; private TextView followBtn;'''
new='''    private FirebaseFirestore db; private FirebaseUser me; private String uid="", name="KING User"; private LinearLayout body; private TextView followBtn;
    private TextView publicFollowers940,publicFollowing940,publicFriends940;'''
if old not in q: raise SystemExit('Public profile fields marker missing for counters')
q=q.replace(old,new,1)

old='''body.addView(actions);
        section("Profile");'''
new='''body.addView(actions);addPublicCounts940();
        section("Profile");'''
if old not in q: raise SystemExit('Public profile actions marker missing for counters')
q=q.replace(old,new,1)

marker='''    private TextView action(String s){'''
helpers='''    private void addPublicCounts940(){
        LinearLayout counts=new LinearLayout(this);counts.setGravity(Gravity.CENTER);counts.setPadding(0,dp(6),0,dp(6));
        publicFollowers940=publicStat940(counts,"…","Followers");publicFollowing940=publicStat940(counts,"…","Following");publicFriends940=publicStat940(counts,"…","Friends");
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(66));cp.setMargins(0,dp(5),0,dp(5));body.addView(counts,cp);loadPublicCounts940();
    }
    private TextView publicStat940(LinearLayout row,String value,String label){
        TextView t=tv(value+"\\n"+label,13,INK,true);t.setGravity(Gravity.CENTER);t.setBackground(bg(Color.WHITE,12));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(58),1);p.setMargins(dp(2),0,dp(2),0);row.addView(t,p);return t;
    }
    private void setPublicStat940(TextView v,long n,String label){if(v!=null)v.setText(n+"\\n"+label);}
    private void failPublicCounts940(){if(publicFollowers940!=null)publicFollowers940.setText("—\\nFollowers");if(publicFollowing940!=null)publicFollowing940.setText("—\\nFollowing");if(publicFriends940!=null)publicFriends940.setText("—\\nFriends");}
    private void loadPublicCounts940(){
        if(db==null||uid==null||uid.isEmpty()){failPublicCounts940();return;}
        db.collection("follows").whereEqualTo("followerUid",uid).get().addOnSuccessListener(out->{
            java.util.Set<String> following=new java.util.HashSet<>();for(DocumentSnapshot d:out.getDocuments()){String x=d.getString("targetUid");if(x!=null&&!x.isEmpty())following.add(x);}setPublicStat940(publicFollowing940,following.size(),"Following");
            db.collection("follows").whereEqualTo("targetUid",uid).get().addOnSuccessListener(in->{long followers=0,friends=0;for(DocumentSnapshot d:in.getDocuments()){String x=d.getString("followerUid");if(x!=null&&!x.isEmpty()){followers++;if(following.contains(x))friends++;}}setPublicStat940(publicFollowers940,followers,"Followers");setPublicStat940(publicFriends940,friends,"Friends");}).addOnFailureListener(e->failPublicCounts940());
        }).addOnFailureListener(e->failPublicCounts940());
    }

'''
if marker not in q: raise SystemExit('Public profile action marker missing for counters')
q=q.replace(marker,helpers+marker,1)
pub.write_text(q)
print('v9.4.0 public profile real social counters applied')


# Public profile visits: log real visitor records from every profile entry point.
pub=pkg/'KingPublicProfileActivity.java'
q=pub.read_text()
old='''    @Override public void onCreate(Bundle b){super.onCreate(b);uid=getIntent().getStringExtra("uid");name=getIntent().getStringExtra("name");if(uid==null)uid="";if(name==null||name.trim().isEmpty())name="KING User";try{db=FirebaseFirestore.getInstance();me=FirebaseAuth.getInstance().getCurrentUser();}catch(Exception ignored){}render();load();}'''
new='''    @Override public void onCreate(Bundle b){super.onCreate(b);uid=getIntent().getStringExtra("uid");name=getIntent().getStringExtra("name");if(uid==null)uid="";if(name==null||name.trim().isEmpty())name="KING User";try{db=FirebaseFirestore.getInstance();me=FirebaseAuth.getInstance().getCurrentUser();}catch(Exception ignored){}render();logProfileVisit940();load();}'''
if old not in q: raise SystemExit('Public profile onCreate marker missing for visitor tracking')
q=q.replace(old,new,1)

marker='''    private void load(){'''
helper='''    private void logProfileVisit940(){
        if(db==null||me==null||uid==null||uid.isEmpty()||uid.equals(me.getUid()))return;
        Map<String,Object> visit=new HashMap<>();visit.put("uid",me.getUid());visit.put("name",displayName());visit.put("visitedAt",FieldValue.serverTimestamp());
        db.collection("public_profiles").document(uid).collection("visitors").document(me.getUid()).set(visit,com.google.firebase.firestore.SetOptions.merge()).addOnFailureListener(e->{});
    }
'''
if marker not in q: raise SystemExit('Public profile load marker missing for visitor helper')
q=q.replace(marker,helper+marker,1)
pub.write_text(q)
print('v9.4.0 public profile visitor tracking applied')


# Me/Profile: real owner-only visitor count and recent visitor list.
main=pkg/'MainActivity.java'
q=main.read_text()
old='''    private TextView profileFollowersNumber, profileFollowingNumber, profileFriendsNumber, profileTopBalance, profileWalletCoins;'''
new='''    private TextView profileFollowersNumber, profileFollowingNumber, profileFriendsNumber, profileTopBalance, profileWalletCoins, profileVisitorsChip940;'''
if old not in q: raise SystemExit('Main profile field marker missing for visitors')
q=q.replace(old,new,1)

old='''        LinearLayout hub=new LinearLayout(this);String[] hi={"🔎 Search","📝 Moments","💞 CP","🎒 Collection"};String[] ht={"Search","Moments","Relationship","Collection"};for(int i=0;i<hi.length;i++){final String tab=ht[i];TextView x=new TextView(this);x.setText(hi[i]);x.setTextSize(11);x.setGravity(Gravity.CENTER);x.setTextColor(0xff41394b);x.setBackground(background(0xfff3f0f7,12));x.setOnClickListener(v->openCommunityHub700(tab));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(46),1);lp.setMargins(dp(3),0,dp(3),0);hub.addView(x,lp);}content.addView(hub,new LinearLayout.LayoutParams(-1,dp(52)));'''
new='''        LinearLayout hub=new LinearLayout(this);String[] hi={"👣 Visitors","📝 Moments","💞 CP","🎒 Collection"};String[] ht={"Visitors","Moments","Relationship","Collection"};for(int i=0;i<hi.length;i++){final String tab=ht[i];TextView x=new TextView(this);x.setText(hi[i]);x.setTextSize(11);x.setGravity(Gravity.CENTER);x.setTextColor(0xff41394b);x.setBackground(background(0xfff3f0f7,12));if("Visitors".equals(tab))profileVisitorsChip940=x;x.setOnClickListener(v->{if("Visitors".equals(tab))showProfileVisitors940();else openCommunityHub700(tab);});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(46),1);lp.setMargins(dp(3),0,dp(3),0);hub.addView(x,lp);}content.addView(hub,new LinearLayout.LayoutParams(-1,dp(52)));'''
if old not in q: raise SystemExit('Main profile hub marker missing for visitors')
q=q.replace(old,new,1)

old='''        refreshServerWallet();
    }'''
new='''        loadProfileVisitors940(uid);
        refreshServerWallet();
    }'''
if old not in q: raise SystemExit('Main profile data tail marker missing for visitors')
q=q.replace(old,new,1)

marker='''    private void refreshServerWallet(){'''
helpers='''    private void loadProfileVisitors940(String uid){
        if(profileVisitorsChip940!=null)profileVisitorsChip940.setText("👣 Visitors …");
        if(firestore==null||uid==null||uid.isEmpty())return;
        firestore.collection("public_profiles").document(uid).collection("visitors").limit(100).get().addOnSuccessListener(s->{if(profileVisitorsChip940!=null)profileVisitorsChip940.setText("👣 Visitors "+s.size());}).addOnFailureListener(e->{if(profileVisitorsChip940!=null)profileVisitorsChip940.setText("👣 Visitors —");});
    }
    private void showProfileVisitors940(){
        if(firestore==null||firebaseAuth==null||firebaseAuth.getCurrentUser()==null){Toast.makeText(this,"Sign in to view visitors",Toast.LENGTH_SHORT).show();return;}
        String uid=firebaseAuth.getCurrentUser().getUid();
        firestore.collection("public_profiles").document(uid).collection("visitors").orderBy("visitedAt",com.google.firebase.firestore.Query.Direction.DESCENDING).limit(50).get().addOnSuccessListener(snap->{
            if(snap.isEmpty()){new AlertDialog.Builder(this).setTitle("👣 Profile Visitors").setMessage("No profile visitors yet.").setPositiveButton("OK",null).show();return;}
            java.util.List<String> rows=new java.util.ArrayList<>();java.util.List<String> ids=new java.util.ArrayList<>();
            for(DocumentSnapshot d:snap.getDocuments()){String n=d.getString("name");if(n==null||n.trim().isEmpty())n="KING User";com.google.firebase.Timestamp t=d.getTimestamp("visitedAt");String when=t==null?"":java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT,java.text.DateFormat.SHORT).format(t.toDate());rows.add("👤 "+n+(when.isEmpty()?"":"  •  "+when));ids.add(d.getId());}
            new AlertDialog.Builder(this).setTitle("👣 Profile Visitors").setItems(rows.toArray(new String[0]),(dlg,w)->{Intent i=new Intent(this,KingPublicProfileActivity.class);i.putExtra("uid",ids.get(w));startActivity(i);}).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->new AlertDialog.Builder(this).setTitle("👣 Profile Visitors").setMessage("Visitors are unavailable right now.").setPositiveButton("Retry",(d,w)->showProfileVisitors940()).setNegativeButton("Close",null).show());
    }

'''
if marker not in q: raise SystemExit('Main wallet marker missing for visitor helpers')
q=q.replace(marker,helpers+marker,1)
main.write_text(q)
print('v9.4.0 Me profile visitor count and history applied')


# Enforce KING Plus no-billing requirement: remove Play Billing code/dependency and all recharge launch UI.
gradle=root/'app/build.gradle'
q=gradle.read_text()
q=q.replace("    implementation 'com.android.billingclient:billing:9.1.0'\n","")
gradle.write_text(q)
billing=pkg/'BillingManager.java'
if billing.exists(): billing.unlink()

main=pkg/'MainActivity.java'
q=main.read_text()
old='''        base("KING Plus v3.0","Security, notifications, billing and release readiness");'''
new='''        base("KING Plus v9.4","Security, notifications and release readiness");'''
if old in q:q=q.replace(old,new,1)
old='''        text("💳 Google Play recharge",19,Color.WHITE,true);
        text("Play Billing never credits coins locally. A completed purchase token is sent to the backend for Google Play verification before server wallet credit.",13,MUTED,false);
        button("💳 Open Play Recharge",PURPLE,()->BillingManager.showRecharge(this));'''
new='''        text("💳 No-billing mode",19,Color.WHITE,true);
        text("KING Plus does not start Google Play purchases or charge real money in this build. Wallet, gifts and parity flows remain no-billing.",13,MUTED,false);'''
if old not in q: raise SystemExit('Main Play Billing readiness marker missing')
q=q.replace(old,new,1)
main.write_text(q)
print('v9.4.0 Play Billing code and dependency removed')


# Mobile OTP resilience: non-dismissing validation, resend token, and in-flight protection.
main=pkg/'MainActivity.java'
q=main.read_text()
old='''    private String phoneVerificationId;'''
new='''    private String phoneVerificationId;
    private PhoneAuthProvider.ForceResendingToken phoneResendToken940;
    private AlertDialog otpDialog940;
    private boolean otpVerifyInFlight940;'''
if old not in q: raise SystemExit('phone verification field marker missing')
q=q.replace(old,new,1)

old='''                @Override public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                    phoneVerificationId = verificationId;
                    Toast.makeText(MainActivity.this, "OTP sent", Toast.LENGTH_SHORT).show();
                    if (!isFinishing() && !isDestroyed()) showOtpDialog(number);
                }'''
new='''                @Override public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                    phoneVerificationId = verificationId;
                    phoneResendToken940 = token;
                    Toast.makeText(MainActivity.this, "OTP sent", Toast.LENGTH_SHORT).show();
                    if (!isFinishing() && !isDestroyed()) showOtpDialog(number);
                }'''
if old not in q: raise SystemExit('phone onCodeSent marker missing')
q=q.replace(old,new,1)

old='''    private void showOtpDialog(String number) {
        final EditText otp = new EditText(this); otp.setHint("6-digit OTP"); otp.setSingleLine(true);
        otp.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        new AlertDialog.Builder(this).setTitle("Verify " + number).setMessage("Enter the SMS OTP sent by Firebase.")
            .setView(otp).setNegativeButton("Cancel", null).setPositiveButton("Verify", (d,w) -> {
                String code = otp.getText().toString().trim();
                if (phoneVerificationId == null || code.length() < 6) { Toast.makeText(this, "Enter the 6-digit OTP", Toast.LENGTH_SHORT).show(); return; }
                signInWithPhoneCredential(PhoneAuthProvider.getCredential(phoneVerificationId, code), number);
            }).show();
    }'''
new='''    private void showOtpDialog(String number) {
        if(otpDialog940!=null&&otpDialog940.isShowing())otpDialog940.dismiss();
        final EditText otp = new EditText(this); otp.setHint("6-digit OTP"); otp.setSingleLine(true);otp.setMaxLines(1);
        otp.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        otpDialog940=new AlertDialog.Builder(this).setTitle("Verify " + number).setMessage("Enter the 6-digit SMS OTP. You can resend if it does not arrive.")
            .setView(otp).setNegativeButton("Cancel",(d,w)->{otpVerifyInFlight940=false;otpDialog940=null;})
            .setNeutralButton("Resend OTP",null).setPositiveButton("Verify",null).create();
        otpDialog940.setOnShowListener(x->{
            Button verify=otpDialog940.getButton(AlertDialog.BUTTON_POSITIVE);
            Button resend=otpDialog940.getButton(AlertDialog.BUTTON_NEUTRAL);
            verify.setOnClickListener(v->{
                if(otpVerifyInFlight940)return;
                String code=otp.getText().toString().trim();
                if(phoneVerificationId==null||!code.matches("[0-9]{6}")){otp.setError("Enter the 6-digit OTP");return;}
                otpVerifyInFlight940=true;verify.setEnabled(false);verify.setText("Verifying…");
                signInWithPhoneCredential(PhoneAuthProvider.getCredential(phoneVerificationId,code),number);
            });
            resend.setOnClickListener(v->{if(!otpVerifyInFlight940)resendRealOtp940(number,resend);});
        });
        otpDialog940.setOnDismissListener(d->{if(otpDialog940!=null&&!otpVerifyInFlight940)otpDialog940=null;});
        otpDialog940.show();
    }
    private void resendRealOtp940(String number,Button resend){
        if(firebaseAuth==null||phoneResendToken940==null){Toast.makeText(this,"Resend is not ready yet",Toast.LENGTH_SHORT).show();return;}
        resend.setEnabled(false);resend.setText("Sending…");
        try{
            PhoneAuthOptions options=PhoneAuthOptions.newBuilder(firebaseAuth).setPhoneNumber(number).setTimeout(60L,TimeUnit.SECONDS).setActivity(this)
                .setForceResendingToken(phoneResendToken940)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                    @Override public void onVerificationCompleted(PhoneAuthCredential credential){signInWithPhoneCredential(credential,number);}
                    @Override public void onVerificationFailed(com.google.firebase.FirebaseException e){runOnUiThread(()->{resend.setEnabled(true);resend.setText("Resend OTP");showPhoneError(e.getLocalizedMessage());});}
                    @Override public void onCodeSent(String verificationId,PhoneAuthProvider.ForceResendingToken token){phoneVerificationId=verificationId;phoneResendToken940=token;runOnUiThread(()->{resend.setEnabled(true);resend.setText("Resend OTP");Toast.makeText(MainActivity.this,"New OTP sent",Toast.LENGTH_SHORT).show();});}
                }).build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        }catch(RuntimeException e){resend.setEnabled(true);resend.setText("Resend OTP");showPhoneError(e.getLocalizedMessage());}
    }'''
if old not in q: raise SystemExit('OTP dialog marker missing')
q=q.replace(old,new,1)

old='''    private void signInWithPhoneCredential(PhoneAuthCredential credential, String number) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(this, "OTP verification failed: " + (task.getException() == null ? "Invalid OTP" : task.getException().getMessage()), Toast.LENGTH_LONG).show();
                return;
            }
            String shortNumber = number.length() > 4 ? "KING " + number.substring(number.length() - 4) : "KING User";
            saveLocalSession(shortNumber, "Mobile");
        });
    }'''
new='''    private void signInWithPhoneCredential(PhoneAuthCredential credential, String number) {
        if(firebaseAuth==null){otpVerifyInFlight940=false;showPhoneError("Firebase Authentication is unavailable.");return;}
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (!task.isSuccessful()) {
                otpVerifyInFlight940=false;
                if(otpDialog940!=null&&otpDialog940.isShowing()){Button verify=otpDialog940.getButton(AlertDialog.BUTTON_POSITIVE);if(verify!=null){verify.setEnabled(true);verify.setText("Verify");}}
                Toast.makeText(this, "OTP verification failed: " + (task.getException() == null ? "Invalid OTP" : task.getException().getMessage()), Toast.LENGTH_LONG).show();
                return;
            }
            otpVerifyInFlight940=false;
            if(otpDialog940!=null&&otpDialog940.isShowing())otpDialog940.dismiss();otpDialog940=null;
            String shortNumber = number.length() > 4 ? "KING " + number.substring(number.length() - 4) : "KING User";
            saveLocalSession(shortNumber, "Mobile");
        });
    }'''
if old not in q: raise SystemExit('phone credential sign-in marker missing')
q=q.replace(old,new,1)
main.write_text(q)
print('v9.4.0 resilient mobile OTP verification and resend applied')


# Video-reference parity: Game page uses My Games + Recommended visual grid.
main=pkg/'MainActivity.java'
q=main.read_text()
start=q.find("    private void games(){")
end=q.find("    private void discover(){",start)
if start<0 or end<0: raise SystemExit('Main games/discover boundary missing for video parity')
new_games=r'''    private void games(){
        incrementMission("mission_game",1);
        screen="games";stopMic();
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(0xfffafafa);
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(16),dp(12),dp(10),dp(6));
        TextView h=new TextView(this);h.setText("Game");h.setTextSize(30);h.setTextColor(0xff171717);h.setTypeface(null,Typeface.BOLD);head.addView(h,new LinearLayout.LayoutParams(0,dp(56),1));
        TextView search=new TextView(this);search.setText("⌕");search.setTextSize(26);search.setTextColor(0xff4f4a57);search.setGravity(Gravity.CENTER);search.setOnClickListener(v->discover());head.addView(search,new LinearLayout.LayoutParams(dp(50),dp(50)));root.addView(head,new LinearLayout.LayoutParams(-1,dp(66)));

        ScrollView sv=new ScrollView(this);LinearLayout host=new LinearLayout(this);host.setOrientation(LinearLayout.VERTICAL);host.setPadding(dp(14),0,dp(14),dp(18));sv.addView(host);
        TextView mine=new TextView(this);mine.setText("My Games");mine.setTextSize(16);mine.setTypeface(null,Typeface.BOLD);mine.setTextColor(0xff27242b);mine.setPadding(dp(2),dp(8),0,dp(8));host.addView(mine,new LinearLayout.LayoutParams(-1,dp(42)));
        LinearLayout myRow=new LinearLayout(this);myRow.setGravity(Gravity.TOP);
        addGameVideoCard940(myRow,"ludo","Ludo","Online");
        addGameVideoCard940(myRow,"werewolf","Werewolf","Party");
        addGameVideoCard940(myRow,"draw","Draw","Friends");
        host.addView(myRow,new LinearLayout.LayoutParams(-1,dp(142)));

        LinearLayout titleRow=new LinearLayout(this);titleRow.setGravity(Gravity.CENTER_VERTICAL);TextView rec=new TextView(this);rec.setText("Recommended");rec.setTextSize(16);rec.setTypeface(null,Typeface.BOLD);rec.setTextColor(0xff27242b);titleRow.addView(rec,new LinearLayout.LayoutParams(0,dp(44),1));TextView more=new TextView(this);more.setText("More ›");more.setTextSize(12);more.setTextColor(0xff8a7d92);more.setGravity(Gravity.CENTER);titleRow.addView(more,new LinearLayout.LayoutParams(dp(72),dp(44)));host.addView(titleRow);

        String[][] games={{"bingo","Bingo"},{"domino","Domino"},{"spy","Spy"},{"sheep","Sheep"},{"zoo","Zoo"},{"memory","Memory"},{"rps","RPS"},{"dice","Dice"},{"wheel","Wheel"}};
        LinearLayout row=null;
        for(int i=0;i<games.length;i++){
            if(i%3==0){row=new LinearLayout(this);row.setGravity(Gravity.TOP);host.addView(row,new LinearLayout.LayoutParams(-1,dp(148)));}
            addGameVideoCard940(row,games[i][0],games[i][1],"Realtime");
        }
        root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));addBottomNav(root,1);setContentView(root);
    }
    private void addGameVideoCard940(LinearLayout row,String code,String title,String badge){
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(4),dp(4),dp(4),dp(6));card.setBackground(background(Color.WHITE,12));card.setOnClickListener(v->openPlayableGame(code));
        KingGameArtView art=new KingGameArtView(this,code,title);card.addView(art,new LinearLayout.LayoutParams(-1,dp(88)));
        TextView nm=new TextView(this);nm.setText(title);nm.setTextSize(12);nm.setTypeface(null,Typeface.BOLD);nm.setTextColor(0xff27242b);nm.setGravity(Gravity.CENTER_VERTICAL);nm.setSingleLine(true);card.addView(nm,new LinearLayout.LayoutParams(-1,dp(24)));
        TextView sub=new TextView(this);sub.setText("● "+badge);sub.setTextSize(9);sub.setTextColor(0xff23a578);sub.setSingleLine(true);card.addView(sub,new LinearLayout.LayoutParams(-1,dp(18)));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(136),1);p.setMargins(dp(4),dp(2),dp(4),dp(6));row.addView(card,p);
    }

'''
q=q[:start]+new_games+q[end:]
main.write_text(q)
print('v9.4.0 video-reference Game page parity applied')

# Video-reference parity: Messages top notification banner before recent people.
inbox=pkg/'InboxActivity.java'
q=inbox.read_text()
marker='''        root.addView(head);
'''
insert='''        root.addView(head);

        LinearLayout notice940=new LinearLayout(this);notice940.setGravity(Gravity.CENTER_VERTICAL);notice940.setPadding(dp(14),dp(6),dp(10),dp(6));notice940.setBackground(bg(0xfffff8d8,12));
        LinearLayout noticeText940=new LinearLayout(this);noticeText940.setOrientation(LinearLayout.VERTICAL);TextView nt940=label("Interactive Notifications",13,DARK,true);TextView ns940=label("Follows, gifts, room activity and invitations",10,0xff8a7d58,false);noticeText940.addView(nt940,new LinearLayout.LayoutParams(-1,dp(22)));noticeText940.addView(ns940,new LinearLayout.LayoutParams(-1,dp(19)));notice940.addView(noticeText940,new LinearLayout.LayoutParams(0,dp(42),1));
        TextView view940=label("View",11,0xff2b2500,true);view940.setGravity(Gravity.CENTER);view940.setBackground(bg(0xffffe500,10));view940.setOnClickListener(v->showCloudNotifications940(""));notice940.addView(view940,new LinearLayout.LayoutParams(dp(68),dp(36)));
        LinearLayout.LayoutParams np940=new LinearLayout.LayoutParams(-1,dp(58));np940.setMargins(dp(12),0,dp(12),dp(6));root.addView(notice940,np940);
'''
if marker not in q: raise SystemExit('Inbox head marker missing for video notification banner')
q=q.replace(marker,insert,1)
inbox.write_text(q)
print('v9.4.0 video-reference Messages banner applied')


# Video-reference first-login profile completion: name + gender + Continue before Home.
main=pkg/'MainActivity.java'
q=main.read_text()

old='''                if(!existing)syncPublicProfile();
                finishGoogleIdentity931(navigateHome,true);'''
new='''                if(!existing){showFirstProfileSetup940(user,canonicalName,canonicalPhoto,navigateHome);return;}
                getPreferences(0).edit().putBoolean("profile_complete_"+user.getUid(),true).apply();
                finishGoogleIdentity931(navigateHome,true);'''
if old not in q: raise SystemExit('Google new-profile branch marker missing for onboarding')
q=q.replace(old,new,1)

old='''            String shortNumber = number.length() > 4 ? "KING " + number.substring(number.length() - 4) : "KING User";
            saveLocalSession(shortNumber, "Mobile");'''
new='''            String shortNumber = number.length() > 4 ? "KING " + number.substring(number.length() - 4) : "KING User";
            FirebaseUser phoneUser940=firebaseAuth.getCurrentUser();
            if(phoneUser940!=null&&firestore!=null){
                firestore.collection("public_profiles").document(phoneUser940.getUid()).get().addOnSuccessListener(doc->{
                    if(doc!=null&&doc.exists()){getPreferences(0).edit().putBoolean("profile_complete_"+phoneUser940.getUid(),true).apply();saveLocalSession(shortNumber,"Mobile");}
                    else showFirstProfileSetup940(phoneUser940,shortNumber,"",true);
                }).addOnFailureListener(e->saveLocalSession(shortNumber,"Mobile"));
            }else if(phoneUser940!=null&&!getPreferences(0).getBoolean("profile_complete_"+phoneUser940.getUid(),false))showFirstProfileSetup940(phoneUser940,shortNumber,"",true);
            else saveLocalSession(shortNumber,"Mobile");'''
if old not in q: raise SystemExit('Mobile sign-in success marker missing for onboarding')
q=q.replace(old,new,1)

marker='''    private void saveLocalSession(String name, String provider) {'''
helpers=r'''    private void showFirstProfileSetup940(FirebaseUser user,String suggestedName,String photoUrl,boolean navigateHome){
        if(user==null){if(navigateHome)home();return;}
        screen="profile_setup";stopMic();
        final String[] selectedGender940={getPreferences(0).getString("gender","Male")};
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.WHITE);root.setPadding(dp(22),dp(32),dp(22),dp(26));
        TextView skipTitle940=new TextView(this);skipTitle940.setText("Complete your profile");skipTitle940.setTextSize(20);skipTitle940.setTypeface(null,Typeface.BOLD);skipTitle940.setTextColor(0xff222222);skipTitle940.setGravity(Gravity.CENTER);root.addView(skipTitle940,new LinearLayout.LayoutParams(-1,dp(46)));

        android.widget.FrameLayout avatarWrap940=new android.widget.FrameLayout(this);TextView avatar940=new TextView(this);String n0=(suggestedName==null||suggestedName.trim().isEmpty())?"K":suggestedName.trim().substring(0,1).toUpperCase();avatar940.setText(n0);avatar940.setTextSize(34);avatar940.setTypeface(null,Typeface.BOLD);avatar940.setTextColor(Color.WHITE);avatar940.setGravity(Gravity.CENTER);avatar940.setBackground(background(0xff27a9ad,52));avatarWrap940.addView(avatar940,new android.widget.FrameLayout.LayoutParams(-1,-1));LinearLayout.LayoutParams avp940=new LinearLayout.LayoutParams(dp(104),dp(104));avp940.gravity=Gravity.CENTER_HORIZONTAL;avp940.setMargins(0,dp(8),0,dp(18));root.addView(avatarWrap940,avp940);

        EditText name940=new EditText(this);name940.setHint("Your name");name940.setSingleLine(true);name940.setText(suggestedName==null?"":suggestedName);name940.setTextColor(0xff222222);name940.setHintTextColor(0xff9a9a9a);name940.setPadding(dp(14),0,dp(14),0);name940.setBackground(background(0xfff5f5f7,10));root.addView(name940,new LinearLayout.LayoutParams(-1,dp(54)));

        TextView sexLabel940=new TextView(this);sexLabel940.setText("Your sex");sexLabel940.setTextSize(14);sexLabel940.setTypeface(null,Typeface.BOLD);sexLabel940.setTextColor(0xff555555);sexLabel940.setPadding(dp(2),dp(18),0,dp(8));root.addView(sexLabel940,new LinearLayout.LayoutParams(-1,dp(52)));
        LinearLayout genders940=new LinearLayout(this);genders940.setGravity(Gravity.CENTER);
        TextView male940=new TextView(this);male940.setText("♂  Male");male940.setTextSize(14);male940.setGravity(Gravity.CENTER);male940.setTypeface(null,Typeface.BOLD);
        TextView female940=new TextView(this);female940.setText("♀  Female");female940.setTextSize(14);female940.setGravity(Gravity.CENTER);female940.setTypeface(null,Typeface.BOLD);
        Runnable refreshGender940=()->{boolean male="Male".equals(selectedGender940[0]);male940.setTextColor(male?0xff3f67dc:0xff777777);female940.setTextColor(male?0xff777777:0xffd85ba2);male940.setBackground(background(male?0xffeef2ff:0xfff5f5f5,10));female940.setBackground(background(male?0xfff5f5f5:0xffffeef7,10));};
        male940.setOnClickListener(v->{selectedGender940[0]="Male";refreshGender940.run();});female940.setOnClickListener(v->{selectedGender940[0]="Female";refreshGender940.run();});refreshGender940.run();
        LinearLayout.LayoutParams gp940=new LinearLayout.LayoutParams(0,dp(54),1);gp940.setMargins(dp(4),0,dp(4),0);genders940.addView(male940,gp940);genders940.addView(female940,new LinearLayout.LayoutParams(gp940));root.addView(genders940,new LinearLayout.LayoutParams(-1,dp(58)));

        android.widget.Space flex940=new android.widget.Space(this);root.addView(flex940,new LinearLayout.LayoutParams(-1,0,1));
        TextView cont940=new TextView(this);cont940.setText("Continue");cont940.setTextSize(16);cont940.setTypeface(null,Typeface.BOLD);cont940.setTextColor(0xff171717);cont940.setGravity(Gravity.CENTER);cont940.setBackground(background(0xffffe500,10));
        cont940.setOnClickListener(v->{String name=name940.getText().toString().trim();if(name.isEmpty()){name940.setError("Name required");return;}displayName=name;SharedPreferences.Editor ed=getPreferences(0).edit().putString("name",name).putString("gender",selectedGender940[0]).putString("firebase_uid",user.getUid()).putBoolean("profile_complete_"+user.getUid(),true);if(photoUrl!=null&&!photoUrl.trim().isEmpty())ed.putString("profile_photo_cloud",photoUrl.trim());ed.apply();syncPublicProfile();if(navigateHome)home();});
        root.addView(cont940,new LinearLayout.LayoutParams(-1,dp(56)));setContentView(root);
    }

'''
if marker not in q: raise SystemExit('saveLocalSession marker missing for onboarding helper')
q=q.replace(marker,helpers+marker,1)
main.write_text(q)
print('v9.4.0 first-login profile completion applied')


# Video-reference Me/Profile: Favorites/Status tabs + real Recommended for you list.
main=pkg/'MainActivity.java'
q=main.read_text()
profile_anchor940='''content.addView(hub,new LinearLayout.LayoutParams(-1,dp(52)));'''
if profile_anchor940 not in q: raise SystemExit('Profile hub anchor missing for video parity')
q=q.replace(profile_anchor940,profile_anchor940+'''\n        addProfileVideoTabs940(content);\n        addProfileRecommendations940(content);''',1)

marker='''    private void rankingsPage(){'''
helpers=r'''    private void addProfileVideoTabs940(LinearLayout list){
        LinearLayout tabs=new LinearLayout(this);tabs.setGravity(Gravity.CENTER);tabs.setPadding(0,dp(3),0,dp(8));
        TextView fav=new TextView(this);fav.setText("Favorites");fav.setTextSize(14);fav.setTypeface(null,Typeface.BOLD);fav.setTextColor(0xff222222);fav.setGravity(Gravity.CENTER);fav.setBackground(background(0xffffe500,10));fav.setOnClickListener(v->backpackPage());
        TextView status=new TextView(this);status.setText("Status");status.setTextSize(14);status.setTypeface(null,Typeface.BOLD);status.setTextColor(0xff6f6877);status.setGravity(Gravity.CENTER);status.setBackground(background(0xfff0eef4,10));status.setOnClickListener(v->momentsPage());
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(42),1);p.setMargins(dp(3),0,dp(3),0);tabs.addView(fav,p);tabs.addView(status,new LinearLayout.LayoutParams(p));list.addView(tabs,new LinearLayout.LayoutParams(-1,dp(52)));
    }
    private void addProfileRecommendations940(LinearLayout list){
        TextView title940=new TextView(this);title940.setText("Recommended for you");title940.setTextSize(16);title940.setTypeface(null,Typeface.BOLD);title940.setTextColor(0xff2a2730);title940.setPadding(dp(3),dp(8),0,dp(5));list.addView(title940,new LinearLayout.LayoutParams(-1,dp(42)));
        LinearLayout box940=new LinearLayout(this);box940.setOrientation(LinearLayout.VERTICAL);list.addView(box940,new LinearLayout.LayoutParams(-1,-2));
        TextView loading940=new TextView(this);loading940.setText("Finding KING profiles…");loading940.setTextSize(12);loading940.setTextColor(0xff8a8391);loading940.setPadding(dp(10),dp(12),dp(10),dp(12));box940.addView(loading940,new LinearLayout.LayoutParams(-1,dp(48)));
        if(firestore==null||firebaseAuth==null||firebaseAuth.getCurrentUser()==null){loading940.setText("Sign in to see recommendations");return;}
        String own=firebaseAuth.getCurrentUser().getUid();
        firestore.collection("public_profiles").limit(8).get().addOnSuccessListener(snap->{box940.removeAllViews();int shown=0;for(DocumentSnapshot d:snap.getDocuments()){String uid=d.getString("uid");if(uid==null||uid.isEmpty())uid=d.getId();if(uid.equals(own))continue;String n=d.getString("displayName");if(n==null||n.trim().isEmpty())n="KING User";Long vip=d.getLong("vipLevel"),lv=d.getLong("level");addProfileRecommendationRow940(box940,uid,n,vip==null?0:vip,lv==null?1:lv);if(++shown>=5)break;}if(shown==0){TextView e=new TextView(this);e.setText("No recommendations yet");e.setTextSize(12);e.setTextColor(0xff8a8391);e.setPadding(dp(10),dp(12),dp(10),dp(12));box940.addView(e,new LinearLayout.LayoutParams(-1,dp(48)));}}).addOnFailureListener(e->{loading940.setText("Recommendations unavailable • tap Status or try again later");});
    }
    private void addProfileRecommendationRow940(LinearLayout box,String uid,String name,long vip,long level){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(10),dp(8),dp(10),dp(8));row.setBackground(background(Color.WHITE,12));
        TextView av=new TextView(this);av.setText(name.isEmpty()?"K":name.substring(0,1).toUpperCase());av.setTextSize(18);av.setTypeface(null,Typeface.BOLD);av.setTextColor(Color.WHITE);av.setGravity(Gravity.CENTER);av.setBackground(background(0xff7c62d5,24));row.addView(av,new LinearLayout.LayoutParams(dp(48),dp(48)));
        LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);info.setPadding(dp(10),0,0,0);TextView nm=new TextView(this);nm.setText(name);nm.setTextSize(14);nm.setTypeface(null,Typeface.BOLD);nm.setTextColor(0xff25222a);info.addView(nm,new LinearLayout.LayoutParams(-1,dp(25)));TextView sub=new TextView(this);sub.setText("VIP "+vip+"  •  Lv."+level+"  •  ID "+publicId(uid));sub.setTextSize(10);sub.setTextColor(0xff7b7480);info.addView(sub,new LinearLayout.LayoutParams(-1,dp(20)));row.addView(info,new LinearLayout.LayoutParams(0,dp(48),1));TextView arrow=new TextView(this);arrow.setText("›");arrow.setTextSize(24);arrow.setTextColor(0xffaaa4ad);arrow.setGravity(Gravity.CENTER);row.addView(arrow,new LinearLayout.LayoutParams(dp(30),dp(48)));row.setOnClickListener(v->{Intent i=new Intent(this,KingPublicProfileActivity.class);i.putExtra("uid",uid);i.putExtra("name",name);startActivity(i);});LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(70));rp.setMargins(0,dp(4),0,dp(4));box.addView(row,rp);
    }

'''
if marker not in q: raise SystemExit('Rankings marker missing for profile video helpers')
q=q.replace(marker,helpers+marker,1)
main.write_text(q)
print('v9.4.0 video-reference Me profile recommendations applied')


# Restore game navigation helpers that lived between games() and discover() in the verified base.
main=pkg/'MainActivity.java'
q=main.read_text()
helper_marker='''    private void discover(){'''
helpers='''    private void openPlayableGame(String game){
        String g=game==null?"":game.toLowerCase(java.util.Locale.US).trim();
        if("ludo".equals(g)){startActivity(new Intent(this,KingLudoLobbyActivity.class));return;}
        kingOpenGameRoom(game);
    }
    private void kingOpenGameRoom(String game){
        Intent i=new Intent(this,PartyActivity.class);
        i.putExtra("requestedGame",game);
        i.putExtra("displayName",displayName==null||displayName.trim().isEmpty()?"KING User":displayName);
        startActivity(i);
    }
    private void kingGameModes(String game){openPlayableGame(game);}

'''
if 'private void openPlayableGame(String game)' not in q:
    if helper_marker not in q: raise SystemExit('Discover marker missing for restored game helpers')
    q=q.replace(helper_marker,helpers+helper_marker,1)
elif 'private void kingGameModes(String game)' not in q:
    if helper_marker not in q: raise SystemExit('Discover marker missing for kingGameModes helper')
    q=q.replace(helper_marker,'    private void kingGameModes(String game){openPlayableGame(game);}\n\n'+helper_marker,1)
main.write_text(q)
print('v9.4.0 video Game navigation helpers restored')


# Bolo-reference parity: realtime Mic-up request queue with host/co-host approval.
party=pkg/'PartyActivity.java'
q=party.read_text()

old='''    private void toggleMic() {
        if(mySeat<1){toast("Take a mic seat first");return;}
        if(muteAll&&!isModerator()){toast("Host muted all seats");return;}
        micOn=!micOn;
        if(cloudRoom){
            setVoicePresence900(micOn);
            if(user!=null&&db!=null)db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(mySeat)).update("micOn",micOn).addOnFailureListener(e->toast("Mic sync failed: "+msg(e)));
            refreshMicControl();
            setInlineAudioMuted940(!micOn);
            toast(micOn?"Mic ON • live inside Party room":"Mic OFF");
            return;
        }
        prefs.edit().putBoolean("mic_"+roomId,micOn).apply();seatMics.put(mySeat,micOn);rebuildSeats();refreshMicControl();
    }'''
new='''    private void toggleMic() {
        if(mySeat<1){
            if(cloudRoom){requestMicSeat940();return;}
            toast("Take a mic seat first");return;
        }
        if(muteAll&&!isModerator()){toast("Host muted all seats");return;}
        micOn=!micOn;
        if(cloudRoom){
            setVoicePresence900(micOn);
            if(user!=null&&db!=null)db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(mySeat)).update("micOn",micOn).addOnFailureListener(e->toast("Mic sync failed: "+msg(e)));
            refreshMicControl();
            setInlineAudioMuted940(!micOn);
            toast(micOn?"Mic ON • live inside Party room":"Mic OFF");
            return;
        }
        prefs.edit().putBoolean("mic_"+roomId,micOn).apply();seatMics.put(mySeat,micOn);rebuildSeats();refreshMicControl();
    }'''
if old not in q: raise SystemExit('final toggleMic marker missing for Mic-up queue')
q=q.replace(old,new,1)

# Add Mic requests to the rich More sheet for moderators.
q=q.replace('''items.add(micOn?"🎤 Mic OFF":"🎤 Mic ON"); items.add("📹 Multi Video"); items.add("🎵 Song request"); items.add("😊 Reaction");''',
'''items.add(micOn?"🎤 Mic OFF":"🎤 Mic ON"); if(isModerator())items.add("🎙 Mic requests"); items.add("📹 Multi Video"); items.add("🎵 Song request"); items.add("😊 Reaction");''',1)
q=q.replace('''else if(x.contains("Mic ON")||x.contains("Mic OFF"))toggleMic();
        else if(x.contains("Multi Video"))openVideoRoom();''',
'''else if(x.contains("Mic ON")||x.contains("Mic OFF"))toggleMic();
        else if(x.contains("Mic requests"))openMicRequests940();
        else if(x.contains("Multi Video"))openVideoRoom();''',1)

marker='''    private boolean isOwner(){'''
helpers='''    private int firstFreeMicSeat940(){
        for(int n=1;n<=maxSeats;n++)if(!seatUids.containsKey(n))return n;
        return -1;
    }
    private void requestMicSeat940(){
        if(!cloudRoom||db==null||user==null){toast("Join a live Party room first");return;}
        if(muteAll&&!isModerator()){toast("Host muted room microphones");return;}
        final int seatNo=firstFreeMicSeat940();
        if(seatNo<1){toast("All mic seats are occupied");return;}
        DocumentReference req=db.collection("live_rooms").document(roomId).collection("seat_requests").document(user.getUid());
        req.get().addOnSuccessListener(existing->{
            if(existing!=null&&existing.exists()){Long n=existing.getLong("seatNo");toast("Mic request pending"+(n==null?"":" • Seat "+n));return;}
            Map<String,Object>d=new HashMap<>();d.put("uid",user.getUid());d.put("name",safeName());d.put("seatNo",seatNo);d.put("createdAt",FieldValue.serverTimestamp());
            req.set(d).addOnSuccessListener(v->{toast("Mic request sent • waiting for host");addEvent("mic_request",safeName()+" requested mic seat "+seatNo+" 🎙");}).addOnFailureListener(e->toast("Mic request failed: "+msg(e)));
        }).addOnFailureListener(e->toast("Mic request unavailable: "+msg(e)));
    }
    private void openMicRequests940(){
        if(!cloudRoom||db==null||user==null){toast("Live Party room required");return;}
        if(!isModerator()){toast("Host/co-host only");return;}
        com.google.firebase.firestore.CollectionReference requests=db.collection("live_rooms").document(roomId).collection("seat_requests");
        requests.orderBy("createdAt",Query.Direction.ASCENDING).limit(30).get().addOnSuccessListener(snap->{
            if(snap==null||snap.isEmpty()){new AlertDialog.Builder(this).setTitle("🎙 Mic requests").setMessage("No one is waiting for a mic seat.").setPositiveButton("OK",null).show();return;}
            List<DocumentSnapshot> docs=new ArrayList<>(snap.getDocuments());String[] rows=new String[docs.size()];
            for(int i=0;i<docs.size();i++){DocumentSnapshot d=docs.get(i);Long n=d.getLong("seatNo");rows[i]=str(d,"name","Guest")+"   •   Seat "+(n==null?"?":n);}
            new AlertDialog.Builder(this).setTitle("🎙 Mic requests • "+docs.size()).setItems(rows,(dlg,w)->reviewMicRequest940(docs.get(w))).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast("Mic requests unavailable: "+msg(e)));
    }
    private void reviewMicRequest940(DocumentSnapshot request){
        if(request==null||!isModerator())return;
        final String uid=request.getString("uid");final String name=str(request,"name","Guest");Long raw=request.getLong("seatNo");final int preferred=raw==null?-1:raw.intValue();
        new AlertDialog.Builder(this).setTitle(name).setMessage("Requested mic seat "+(preferred>0?preferred:"")+"\\n\\nApprove to place this member on stage with mic OFF.")
            .setPositiveButton("Approve",(d,w)->approveMicRequest940(request,uid,name,preferred))
            .setNeutralButton("Reject",(d,w)->request.getReference().delete().addOnSuccessListener(v->toast("Mic request rejected")).addOnFailureListener(e->toast("Reject failed: "+msg(e))))
            .setNegativeButton("Cancel",null).show();
    }
    private void approveMicRequest940(DocumentSnapshot request,String uid,String name,int preferred){
        if(uid==null||uid.isEmpty()||db==null)return;
        int seat=(preferred>0&&!seatUids.containsKey(preferred))?preferred:firstFreeMicSeat940();
        if(seat<1){toast("No free mic seat");return;}
        final int targetSeat=seat;
        DocumentReference seatRef=db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(targetSeat));
        Map<String,Object>s=new HashMap<>();s.put("uid",uid);s.put("name",name);s.put("micOn",false);s.put("joinedAt",FieldValue.serverTimestamp());
        seatRef.set(s).addOnSuccessListener(v->request.getReference().delete().addOnSuccessListener(x->{addEvent("seat",name+" approved to mic seat "+targetSeat+" 🎙");toast(name+" moved to seat "+targetSeat);}).addOnFailureListener(e->toast("Seat approved; request cleanup failed")))
            .addOnFailureListener(e->toast("Could not assign seat: "+msg(e)));
    }

'''
if marker not in q: raise SystemExit('isOwner marker missing for Mic-up helpers')
q=q.replace(marker,helpers+marker,1)
party.write_text(q)
print('v9.4.0 realtime Mic-up request queue applied')


# Bolo-reference parity: Multi Video viewer/seat/mic/camera state synchronized through Firebase.
multi=pkg/'KingMultiVideoActivity.java'
multi.write_text(r'''package com.kingplus.social;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class KingMultiVideoActivity extends androidx.fragment.app.FragmentActivity implements org.jitsi.meet.sdk.JitsiMeetActivityInterface {
    private org.jitsi.meet.sdk.JitsiMeetView meetView;
    private String roomId="",roomName="KING Plus Multi Video",displayName="KING User";
    private FirebaseFirestore db; private FirebaseUser me; private ListenerRegistration seatsListener,roomListener,roleListener;
    private final Map<Integer,String> seatUids=new HashMap<>(),seatNames=new HashMap<>();
    private int mySeat=-1; private boolean micOn=false,cameraOn=false,moderator=false;
    private LinearLayout seatsBar,controls; private TextView stateText,seatAction,micAction,camAction;

    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    private TextView tv(String s,int z,int c,boolean b){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);if(b)t.setTypeface(null,Typeface.BOLD);return t;}
    private String safe(String s,String f){return s==null||s.trim().isEmpty()?f:s.trim();}
    private void toast(String s){if(!isFinishing())android.widget.Toast.makeText(this,s,android.widget.Toast.LENGTH_SHORT).show();}

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        roomId=safe(getIntent().getStringExtra("roomId"),"");
        roomName=safe(getIntent().getStringExtra("roomName"),"KING Plus Multi Video");
        displayName=safe(getIntent().getStringExtra("displayName"),"KING User");
        try{db=FirebaseFirestore.getInstance();me=FirebaseAuth.getInstance().getCurrentUser();}catch(Exception ignored){}

        FrameLayout shell=new FrameLayout(this);shell.setBackgroundColor(0xff0d0b15);
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);shell.addView(body,new FrameLayout.LayoutParams(-1,-1));

        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(10),0,dp(12),0);head.setBackgroundColor(0xff171322);
        TextView back=tv("‹",38,Color.WHITE,false);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->finish());head.addView(back,new LinearLayout.LayoutParams(dp(50),dp(58)));
        LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);TextView title=tv(roomName,17,Color.WHITE,true);stateText=tv("📹 Multi Video • viewer mode",11,0xffc7bdd5,true);info.addView(title,new LinearLayout.LayoutParams(-1,dp(30)));info.addView(stateText,new LinearLayout.LayoutParams(-1,dp(22)));head.addView(info,new LinearLayout.LayoutParams(0,dp(58),1));
        TextView leave=tv("Leave",12,Color.WHITE,true);leave.setGravity(Gravity.CENTER);leave.setBackground(bg(0xff7b3fd0,18));leave.setOnClickListener(v->finish());head.addView(leave,new LinearLayout.LayoutParams(dp(72),dp(38)));
        body.addView(head,new LinearLayout.LayoutParams(-1,dp(62)));

        FrameLayout stage=new FrameLayout(this);body.addView(stage,new LinearLayout.LayoutParams(-1,0,1));
        TextView loading=tv("Connecting Multi Video…",14,0xffcfc8da,true);loading.setGravity(Gravity.CENTER);stage.addView(loading,new FrameLayout.LayoutParams(-1,-1));

        seatsBar=new LinearLayout(this);seatsBar.setGravity(Gravity.CENTER);seatsBar.setPadding(dp(8),dp(5),dp(8),dp(3));seatsBar.setBackgroundColor(0xff171322);body.addView(seatsBar,new LinearLayout.LayoutParams(-1,dp(68)));
        controls=new LinearLayout(this);controls.setGravity(Gravity.CENTER);controls.setPadding(dp(6),dp(3),dp(6),dp(8));controls.setBackgroundColor(0xff171322);
        seatAction=control("＋ Sit Down",()->toggleSeat940());micAction=control("🎤 Mic OFF",()->toggleMic940());camAction=control("📷 Camera OFF",()->toggleCamera940());
        controls.addView(seatAction,controlLp());controls.addView(micAction,controlLp());controls.addView(camAction,controlLp());body.addView(controls,new LinearLayout.LayoutParams(-1,dp(62)));

        try{
            org.jitsi.meet.sdk.JitsiMeet.instantiateReactNative(this);
            meetView=new org.jitsi.meet.sdk.JitsiMeetView(this);stage.addView(meetView,new FrameLayout.LayoutParams(-1,-1));
            String slug=("KINGPlus-"+roomId).replaceAll("[^A-Za-z0-9_-]","");
            org.jitsi.meet.sdk.JitsiMeetUserInfo userInfo=new org.jitsi.meet.sdk.JitsiMeetUserInfo();userInfo.setDisplayName(displayName);
            org.jitsi.meet.sdk.JitsiMeetConferenceOptions options=new org.jitsi.meet.sdk.JitsiMeetConferenceOptions.Builder()
                .setServerURL(new java.net.URL("https://meet.jit.si")).setRoom(slug).setSubject(roomName)
                .setAudioMuted(true).setVideoMuted(true).setUserInfo(userInfo)
                .setFeatureFlag("welcomepage.enabled",false).setFeatureFlag("prejoinpage.enabled",false)
                .setFeatureFlag("invite.enabled",false).setFeatureFlag("chat.enabled",false)
                .setFeatureFlag("recording.enabled",false).setFeatureFlag("live-streaming.enabled",false)
                .setFeatureFlag("pip.enabled",true).build();
            meetView.join(options);loading.setVisibility(View.GONE);
        }catch(Throwable e){loading.setText("Multi Video could not start • tap back and retry");}
        setContentView(shell);attachRealtime940();renderSeats940();refreshControls940();
    }

    private TextView control(String text,Runnable action){TextView v=tv(text,12,Color.WHITE,true);v.setGravity(Gravity.CENTER);v.setBackground(bg(0xff372b4c,15));v.setOnClickListener(x->action.run());return v;}
    private LinearLayout.LayoutParams controlLp(){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(48),1);p.setMargins(dp(4),0,dp(4),0);return p;}

    private void attachRealtime940(){
        if(db==null||me==null||roomId.isEmpty()){stateText.setText("📹 Multi Video • sign in required for seats");return;}
        DocumentReference room=db.collection("live_rooms").document(roomId);
        roomListener=room.addSnapshotListener((doc,e)->{if(e==null&&doc!=null&&doc.exists()){moderator=me.getUid().equals(doc.getString("ownerUid"));refreshControls940();}});
        roleListener=room.collection("roles").document(me.getUid()).addSnapshotListener((doc,e)->{if(e==null&&doc!=null&&doc.exists()&&"cohost".equals(doc.getString("role")))moderator=true;refreshControls940();});
        seatsListener=room.collection("video_seats").addSnapshotListener((snap,e)->{
            if(e!=null||snap==null)return;seatUids.clear();seatNames.clear();mySeat=-1;micOn=false;cameraOn=false;
            for(DocumentSnapshot d:snap.getDocuments()){int no;try{no=Integer.parseInt(d.getId());}catch(Exception ex){continue;}seatUids.put(no,d.getString("uid"));seatNames.put(no,safe(d.getString("name"),"Guest"));if(me.getUid().equals(d.getString("uid"))){mySeat=no;micOn=Boolean.TRUE.equals(d.getBoolean("micOn"));cameraOn=Boolean.TRUE.equals(d.getBoolean("cameraOn"));}}
            applyMedia940();renderSeats940();refreshControls940();
        });
    }

    private void renderSeats940(){
        if(seatsBar==null)return;seatsBar.removeAllViews();
        for(int i=1;i<=6;i++){final int seat=i;String uid=seatUids.get(i),name=seatNames.get(i);boolean mine=me!=null&&me.getUid().equals(uid);
            TextView v=tv(uid==null?"＋":(mine?"●":safe(name,"G").substring(0,1).toUpperCase()),uid==null?18:14,mine?0xffffe500:Color.WHITE,true);v.setGravity(Gravity.CENTER);v.setBackground(bg(mine?0xff51431b:(uid==null?0xff30283b:0xff47345e),24));v.setContentDescription(uid==null?"Empty video seat "+i:"Video seat "+i+" "+name);v.setOnClickListener(x->seatTap940(seat));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(50),1);p.setMargins(dp(3),0,dp(3),0);seatsBar.addView(v,p);}
    }

    private void seatTap940(int seat){
        String uid=seatUids.get(seat);
        if(uid==null){if(mySeat>0){toast("Leave your current video seat first");return;}takeSeat940(seat);return;}
        if(me!=null&&me.getUid().equals(uid)){leaveSeat940();return;}
        if(moderator)new android.app.AlertDialog.Builder(this).setTitle(seatNames.get(seat)).setMessage("Remove this member from video seat "+seat+"?").setPositiveButton("Remove",(d,w)->removeSeat940(seat)).setNegativeButton("Cancel",null).show();
        else toast(seatNames.get(seat)+" is on video seat "+seat);
    }

    private void toggleSeat940(){if(mySeat>0)leaveSeat940();else{for(int i=1;i<=6;i++)if(!seatUids.containsKey(i)){takeSeat940(i);return;}toast("All video seats are occupied");}}
    private void takeSeat940(int seat){
        if(db==null||me==null||roomId.isEmpty()){toast("Sign in required");return;}
        Map<String,Object>s=new HashMap<>();s.put("uid",me.getUid());s.put("name",displayName);s.put("micOn",false);s.put("cameraOn",true);s.put("joinedAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).collection("video_seats").document(String.valueOf(seat)).set(s)
            .addOnSuccessListener(v->toast("Video seat "+seat+" joined")).addOnFailureListener(e->toast("Seat unavailable: "+e.getMessage()));
    }
    private void leaveSeat940(){if(db==null||mySeat<1)return;db.collection("live_rooms").document(roomId).collection("video_seats").document(String.valueOf(mySeat)).delete().addOnFailureListener(e->toast("Could not leave seat"));}
    private void removeSeat940(int seat){if(!moderator||db==null)return;db.collection("live_rooms").document(roomId).collection("video_seats").document(String.valueOf(seat)).delete().addOnFailureListener(e->toast("Remove failed"));}
    private void toggleMic940(){if(mySeat<1){toast("Sit on a video seat first");return;}micOn=!micOn;syncOwnSeat940("micOn",micOn);applyAudio940(!micOn);refreshControls940();}
    private void toggleCamera940(){if(mySeat<1){toast("Sit on a video seat first");return;}cameraOn=!cameraOn;syncOwnSeat940("cameraOn",cameraOn);applyVideo940(!cameraOn);refreshControls940();}
    private void syncOwnSeat940(String key,boolean value){if(db==null||mySeat<1)return;db.collection("live_rooms").document(roomId).collection("video_seats").document(String.valueOf(mySeat)).update(key,value).addOnFailureListener(e->toast("Video seat sync failed"));}
    private void applyMedia940(){applyAudio940(mySeat<1||!micOn);applyVideo940(mySeat<1||!cameraOn);}
    private void applyAudio940(boolean muted){try{Intent i=org.jitsi.meet.sdk.BroadcastIntentHelper.buildSetAudioMutedIntent(muted);LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);}catch(Throwable ignored){}}
    private void applyVideo940(boolean muted){try{Intent i=org.jitsi.meet.sdk.BroadcastIntentHelper.buildSetVideoMutedIntent(muted);LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);}catch(Throwable ignored){}}
    private void refreshControls940(){
        if(stateText!=null)stateText.setText(mySeat>0?("📹 Video seat "+mySeat+(moderator?" • Host controls":"")):("📹 Multi Video • viewer mode"+(moderator?" • Host controls":"")));
        if(seatAction!=null)seatAction.setText(mySeat>0?"↥ Leave Seat":"＋ Sit Down");
        if(micAction!=null){micAction.setText(micOn?"🎤 Mic ON":"🎤 Mic OFF");micAction.setAlpha(mySeat>0?1f:.45f);}
        if(camAction!=null){camAction.setText(cameraOn?"📷 Camera ON":"📷 Camera OFF");camAction.setAlpha(mySeat>0?1f:.45f);}
    }

    @Override protected void onResume(){super.onResume();org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostResume(this);}
    @Override protected void onStop(){org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostPause(this);super.onStop();}
    @Override public void onNewIntent(Intent intent){super.onNewIntent(intent);org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onNewIntent(intent);}
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onActivityResult(this,requestCode,resultCode,data);}
    @Override public void requestPermissions(String[] permissions,int requestCode,com.facebook.react.modules.core.PermissionListener listener){org.jitsi.meet.sdk.JitsiMeetActivityDelegate.requestPermissions(this,permissions,requestCode,listener);}
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onRequestPermissionsResult(requestCode,permissions,grantResults);}
    @Override protected void onDestroy(){if(seatsListener!=null)seatsListener.remove();if(roomListener!=null)roomListener.remove();if(roleListener!=null)roleListener.remove();try{if(meetView!=null){meetView.dispose();meetView=null;}}catch(Throwable ignored){}org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostDestroy(this);super.onDestroy();}
}''')
print('v9.4.0 realtime Multi Video seats and media controls applied')


# Bolo-reference parity: Party game history/result panel backed by real room events.
rg=pkg/'RoomGameActivity.java'
q=rg.read_text()
old='''        stateText=tv("Waiting for active round",18,GOLD,true);stateText.setGravity(Gravity.CENTER);stateText.setBackground(bg(CARD,18));LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.setMargins(0,dp(8),0,dp(10));page.addView(stateText,sp);'''
new='''        stateText=tv("Waiting for active round",18,GOLD,true);stateText.setGravity(Gravity.CENTER);stateText.setBackground(bg(CARD,18));LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.setMargins(0,dp(8),0,dp(8));page.addView(stateText,sp);
        Button history940=button("📜 Recent game results");history940.setOnClickListener(v->showGameHistory940());LinearLayout.LayoutParams hp940=new LinearLayout.LayoutParams(-1,dp(48));hp940.setMargins(0,0,0,dp(8));page.addView(history940,hp940);'''
if old not in q: raise SystemExit('RoomGame state header marker missing for history')
q=q.replace(old,new,1)

marker='''    private void renderState(){'''
helper='''    private void showGameHistory940(){
        if(db==null||roomId.isEmpty()){toast("Open from a live Party room");return;}
        room().collection("events").orderBy("createdAt",com.google.firebase.firestore.Query.Direction.DESCENDING).limit(120).get().addOnSuccessListener(snap->{
            java.util.List<String> rows=new java.util.ArrayList<>();
            for(DocumentSnapshot d:snap.getDocuments()){
                String type=s(d.getString("type")),text=s(d.getString("text"));
                if(!"play".equals(type)||text.isEmpty())continue;
                if(text.startsWith("🏁")||text.contains(" wins")||text.contains(" Draw")||text.contains(" ended ")){rows.add(text);if(rows.size()>=20)break;}
            }
            if(rows.isEmpty()&&!result.isEmpty())rows.add("🏁 "+result.replace("\\n"," • "));
            if(rows.isEmpty()){new AlertDialog.Builder(this).setTitle("📜 Game History").setMessage("No finished Party game rounds yet.").setPositiveButton("OK",null).show();return;}
            new AlertDialog.Builder(this).setTitle("📜 Recent game results").setItems(rows.toArray(new String[0]),(d,w)->new AlertDialog.Builder(this).setTitle("Round result").setMessage(rows.get(w).replace(" • ","\\n")).setPositiveButton("OK",null).show()).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast("Game history unavailable: "+e.getMessage()));
    }

'''
if marker not in q: raise SystemExit('RoomGame renderState marker missing for history helper')
q=q.replace(marker,helper+marker,1)
rg.write_text(q)
print('v9.4.0 Party game history/result panel applied')


# Bolo-reference Multi Video: host/co-host sit-down invitation flow.
multi=pkg/'KingMultiVideoActivity.java'
q=multi.read_text()

q=q.replace('''import java.util.HashMap;
import java.util.Map;''','''import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;''',1)

q=q.replace('''private FirebaseFirestore db; private FirebaseUser me; private ListenerRegistration seatsListener,roomListener,roleListener;''',
'''private FirebaseFirestore db; private FirebaseUser me; private ListenerRegistration seatsListener,roomListener,roleListener,inviteListener;
    private boolean inviteDialogOpen940=false;''',1)

old='''        seatsListener=room.collection("video_seats").addSnapshotListener((snap,e)->{
            if(e!=null||snap==null)return;seatUids.clear();seatNames.clear();mySeat=-1;micOn=false;cameraOn=false;
            for(DocumentSnapshot d:snap.getDocuments()){int no;try{no=Integer.parseInt(d.getId());}catch(Exception ex){continue;}seatUids.put(no,d.getString("uid"));seatNames.put(no,safe(d.getString("name"),"Guest"));if(me.getUid().equals(d.getString("uid"))){mySeat=no;micOn=Boolean.TRUE.equals(d.getBoolean("micOn"));cameraOn=Boolean.TRUE.equals(d.getBoolean("cameraOn"));}}
            applyMedia940();renderSeats940();refreshControls940();
        });'''
new='''        seatsListener=room.collection("video_seats").addSnapshotListener((snap,e)->{
            if(e!=null||snap==null)return;seatUids.clear();seatNames.clear();mySeat=-1;micOn=false;cameraOn=false;
            for(DocumentSnapshot d:snap.getDocuments()){int no;try{no=Integer.parseInt(d.getId());}catch(Exception ex){continue;}seatUids.put(no,d.getString("uid"));seatNames.put(no,safe(d.getString("name"),"Guest"));if(me.getUid().equals(d.getString("uid"))){mySeat=no;micOn=Boolean.TRUE.equals(d.getBoolean("micOn"));cameraOn=Boolean.TRUE.equals(d.getBoolean("cameraOn"));}}
            applyMedia940();renderSeats940();refreshControls940();
        });
        inviteListener=room.collection("video_seat_invites").document(me.getUid()).addSnapshotListener((doc,e)->{
            if(e!=null||doc==null||!doc.exists()||inviteDialogOpen940||mySeat>0)return;
            Long raw=doc.getLong("seatNo");int seat=raw==null?-1:raw.intValue();if(seat<1||seat>6)return;
            inviteDialogOpen940=true;String inviter=safe(doc.getString("inviterName"),"Host");
            new android.app.AlertDialog.Builder(this).setTitle("📹 Video seat invitation").setMessage(inviter+" invited you to video seat "+seat+".")
                .setPositiveButton("Sit Down",(d,w)->{inviteDialogOpen940=false;acceptVideoInvite940(doc,seat);})
                .setNegativeButton("Reject",(d,w)->{inviteDialogOpen940=false;doc.getReference().delete().addOnFailureListener(x->{});})
                .setOnCancelListener(d->inviteDialogOpen940=false).show();
        });'''
if old not in q: raise SystemExit('Multi Video seats listener marker missing for invite flow')
q=q.replace(old,new,1)

old='''            TextView v=tv(uid==null?"＋":(mine?"●":safe(name,"G").substring(0,1).toUpperCase()),uid==null?18:14,mine?0xffffe500:Color.WHITE,true);v.setGravity(Gravity.CENTER);v.setBackground(bg(mine?0xff51431b:(uid==null?0xff30283b:0xff47345e),24));v.setContentDescription(uid==null?"Empty video seat "+i:"Video seat "+i+" "+name);v.setOnClickListener(x->seatTap940(seat));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(50),1);p.setMargins(dp(3),0,dp(3),0);seatsBar.addView(v,p);}'''
new='''            TextView v=tv(uid==null?"＋":(mine?"●":safe(name,"G").substring(0,1).toUpperCase()),uid==null?18:14,mine?0xffffe500:Color.WHITE,true);v.setGravity(Gravity.CENTER);v.setBackground(bg(mine?0xff51431b:(uid==null?0xff30283b:0xff47345e),24));v.setContentDescription(uid==null?"Empty video seat "+i:"Video seat "+i+" "+name);v.setOnClickListener(x->seatTap940(seat));v.setOnLongClickListener(x->{if(moderator&&uid==null){inviteToVideoSeat940(seat);return true;}return false;});
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(50),1);p.setMargins(dp(3),0,dp(3),0);seatsBar.addView(v,p);}'''
if old not in q: raise SystemExit('Multi Video seat render marker missing for invite long press')
q=q.replace(old,new,1)

marker='''    private void toggleSeat940(){'''
helpers='''    private void inviteToVideoSeat940(int seat){
        if(!moderator||db==null||me==null){toast("Host/co-host only");return;}
        db.collection("live_rooms").document(roomId).collection("members").limit(100).get().addOnSuccessListener(snap->{
            List<DocumentSnapshot> docs=new ArrayList<>();List<String> rows=new ArrayList<>();
            for(DocumentSnapshot d:snap.getDocuments()){
                String uid=d.getString("uid");if(uid==null||uid.isEmpty()||uid.equals(me.getUid())||seatUids.containsValue(uid))continue;
                docs.add(d);rows.add(safe(d.getString("name"),"KING User"));
            }
            if(rows.isEmpty()){toast("No available room members to invite");return;}
            new android.app.AlertDialog.Builder(this).setTitle("Invite to video seat "+seat).setItems(rows.toArray(new String[0]),(dlg,w)->{
                DocumentSnapshot target=docs.get(w);String uid=target.getString("uid");String name=safe(target.getString("name"),"KING User");
                Map<String,Object> inv=new HashMap<>();inv.put("recipientUid",uid);inv.put("recipientName",name);inv.put("seatNo",seat);inv.put("inviterUid",me.getUid());inv.put("inviterName",displayName);inv.put("createdAt",FieldValue.serverTimestamp());
                db.collection("live_rooms").document(roomId).collection("video_seat_invites").document(uid).set(inv)
                    .addOnSuccessListener(v->toast("Video seat invite sent to "+name)).addOnFailureListener(e->toast("Invite failed: "+e.getMessage()));
            }).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast("Members unavailable: "+e.getMessage()));
    }
    private void acceptVideoInvite940(DocumentSnapshot invite,int seat){
        if(me==null||db==null){return;}
        if(seatUids.containsKey(seat)){toast("That video seat is no longer free");invite.getReference().delete().addOnFailureListener(e->{});return;}
        Map<String,Object>s=new HashMap<>();s.put("uid",me.getUid());s.put("name",displayName);s.put("micOn",false);s.put("cameraOn",true);s.put("joinedAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).collection("video_seats").document(String.valueOf(seat)).set(s)
            .addOnSuccessListener(v->invite.getReference().delete().addOnSuccessListener(x->toast("Joined video seat "+seat)).addOnFailureListener(x->{}))
            .addOnFailureListener(e->toast("Could not join invited seat: "+e.getMessage()));
    }

'''
if marker not in q: raise SystemExit('Multi Video toggleSeat marker missing for invite helpers')
q=q.replace(marker,helpers+marker,1)

q=q.replace('''@Override protected void onDestroy(){if(seatsListener!=null)seatsListener.remove();if(roomListener!=null)roomListener.remove();if(roleListener!=null)roleListener.remove();''',
'''@Override protected void onDestroy(){if(seatsListener!=null)seatsListener.remove();if(roomListener!=null)roomListener.remove();if(roleListener!=null)roleListener.remove();if(inviteListener!=null)inviteListener.remove();''',1)

multi.write_text(q)
print('v9.4.0 Multi Video sit-down invitation flow applied')


# Bolo-reference parity: visual Gift Wall backed by real room gift events.
party=pkg/'PartyActivity.java'
q=party.read_text()
gift_start=q.find("    private void giftWall620(){")
gift_end=q.find("    private void seedLocalFeed()",gift_start)
if gift_start<0 or gift_end<0: raise SystemExit('Gift Wall method boundary missing')
gift_method=r'''    private void giftWall620(){
        if(!cloudRoom||db==null){new AlertDialog.Builder(this).setTitle("🎁 Gift Wall").setMessage("Gift Wall becomes live inside a Firebase room.").setPositiveButton("Gift Shop",(d,w)->giftShopPanel()).setNegativeButton("Close",null).show();return;}
        db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(250).get().addOnSuccessListener(snap->{
            Map<String,Long> giftValue=new HashMap<>();Map<String,Integer> giftCount=new HashMap<>();long total=0;int events=0;
            for(DocumentSnapshot d:snap.getDocuments())if("gift".equals(d.getString("type"))){String g=str(d,"giftIcon","🎁")+" "+str(d,"giftName","Gift");Long v=d.getLong("giftValue");long value=v==null?0:Math.max(0,v);giftValue.put(g,(giftValue.containsKey(g)?giftValue.get(g):0L)+value);giftCount.put(g,(giftCount.containsKey(g)?giftCount.get(g):0)+1);total+=value;events++;}
            List<Map.Entry<String,Long>> list=new ArrayList<>(giftValue.entrySet());java.util.Collections.sort(list,(a,b)->Long.compare(b.getValue(),a.getValue()));
            LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(14),dp(10),dp(14),dp(14));root.setBackgroundColor(0xff171320);
            LinearLayout summary=new LinearLayout(this);summary.setGravity(Gravity.CENTER);summary.setBackground(bg(0xff261c36,16));
            TextView totalGifts=tv("🎁 "+events,17,Color.WHITE,true);totalGifts.setGravity(Gravity.CENTER);summary.addView(totalGifts,new LinearLayout.LayoutParams(0,dp(58),1));
            TextView totalValue=tv("💎 "+compactNumber(total),17,0xffffd35a,true);totalValue.setGravity(Gravity.CENTER);summary.addView(totalValue,new LinearLayout.LayoutParams(0,dp(58),1));root.addView(summary,new LinearLayout.LayoutParams(-1,dp(62)));
            ScrollView scroll=new ScrollView(this);LinearLayout grid=new LinearLayout(this);grid.setOrientation(LinearLayout.VERTICAL);grid.setPadding(0,dp(8),0,dp(6));scroll.addView(grid);
            if(list.isEmpty()){TextView empty=tv("No gifts in this room yet",14,MUTED,true);empty.setGravity(Gravity.CENTER);grid.addView(empty,new LinearLayout.LayoutParams(-1,dp(100)));}
            LinearLayout row=null;int col=0,shown=0;
            for(Map.Entry<String,Long> e:list){if(shown++>=12)break;if(row==null||col==3){row=new LinearLayout(this);row.setGravity(Gravity.TOP);grid.addView(row,new LinearLayout.LayoutParams(-1,dp(116)));col=0;}
                String gift=e.getKey();Integer cnt=giftCount.get(gift);LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(3),dp(4),dp(3),dp(4));card.setBackground(bg(0xff282235,14));
                String icon="🎁",name=gift;int sp=gift.indexOf(' ');if(sp>0){icon=gift.substring(0,sp);name=gift.substring(sp+1);}
                TextView iv=tv(icon,31,Color.WHITE,false);iv.setGravity(Gravity.CENTER);card.addView(iv,new LinearLayout.LayoutParams(-1,dp(43)));
                TextView nm=tv(name,10,Color.WHITE,true);nm.setGravity(Gravity.CENTER);nm.setSingleLine(true);card.addView(nm,new LinearLayout.LayoutParams(-1,dp(22)));
                TextView meta=tv("×"+(cnt==null?0:cnt)+"   💎"+compactNumber(e.getValue()),9,0xffffd35a,true);meta.setGravity(Gravity.CENTER);card.addView(meta,new LinearLayout.LayoutParams(-1,dp(24)));
                LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(108),1);cp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(card,cp);col++;
            }
            if(row!=null&&col>0&&col<3)for(int i=col;i<3;i++){View spacer=new View(this);LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(0,dp(108),1);ep.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(spacer,ep);}
            root.addView(scroll,new LinearLayout.LayoutParams(-1,Math.min(dp(420),Math.max(dp(120),((list.size()+2)/3)*dp(116)+dp(14)))));
            new AlertDialog.Builder(this).setTitle("🎁 Gift Wall").setView(root).setPositiveButton("Gift Shop",(d,w)->giftShopPanel()).setNeutralButton("Ranking",(d,w)->roomRankingDialog()).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast("Gift Wall unavailable: "+msg(e)));
    }'''
q=q[:gift_start]+gift_method+"\n\n"+q[gift_end:]
party.write_text(q)
print('v9.4.0 visual Gift Wall cards applied')


# Bolo-reference parity: visual synchronized Audio PK arena/result panel.
party=pkg/'PartyActivity.java'
q=party.read_text()
pk_start=q.find("    private void audioPkPanel700(){")
pk_end=q.find("    private int firstOpenSeat700()",pk_start)
if pk_start<0 or pk_end<0: raise SystemExit('Audio PK method boundary missing')
pk_method=r'''    private void audioPkPanel700(){
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
                    com.google.firebase.Timestamp at=e.getTimestamp("createdAt");if(pkStarted!=null&&at!=null&&at.compareTo(pkStarted)<0)continue;
                    Long raw=e.getLong("giftValue");long score=raw==null?0:Math.max(0,raw);String actor=e.getString("actorUid");Integer seat=null;
                    if(actor!=null)for(Map.Entry<Integer,String>x:seatUids.entrySet())if(actor.equals(x.getValue())){seat=x.getKey();break;}
                    if(seat!=null&&seat%2==0)red+=score;else if(seat!=null)blue+=score;else if((gifts%2)==0)red+=score;else blue+=score;gifts++;
                }
                final long redScore=red,blueScore=blue;
                LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(14),dp(10),dp(14),dp(14));root.setBackgroundColor(0xff15121e);
                TextView timer=tv(pkActive?String.format(java.util.Locale.US,"⏱  %02d:%02d",pkRemain/60,pkRemain%60):"🏁  PK RESULT",18,pkActive?0xffffd84d:Color.WHITE,true);timer.setGravity(Gravity.CENTER);root.addView(timer,new LinearLayout.LayoutParams(-1,dp(46)));
                LinearLayout teams=new LinearLayout(this);teams.setGravity(Gravity.CENTER);
                LinearLayout redCard=new LinearLayout(this);redCard.setOrientation(LinearLayout.VERTICAL);redCard.setGravity(Gravity.CENTER);redCard.setBackground(bg(0xff5c2230,16));
                TextView redTitle=tv("🔴 RED",15,Color.WHITE,true);redTitle.setGravity(Gravity.CENTER);redCard.addView(redTitle,new LinearLayout.LayoutParams(-1,dp(30)));
                TextView redVal=tv(compactNumber(redScore),27,0xffffc5cd,true);redVal.setGravity(Gravity.CENTER);redCard.addView(redVal,new LinearLayout.LayoutParams(-1,dp(48)));
                TextView vs=tv("VS",18,0xffffd84d,true);vs.setGravity(Gravity.CENTER);
                LinearLayout blueCard=new LinearLayout(this);blueCard.setOrientation(LinearLayout.VERTICAL);blueCard.setGravity(Gravity.CENTER);blueCard.setBackground(bg(0xff203b66,16));
                TextView blueTitle=tv("🔵 BLUE",15,Color.WHITE,true);blueTitle.setGravity(Gravity.CENTER);blueCard.addView(blueTitle,new LinearLayout.LayoutParams(-1,dp(30)));
                TextView blueVal=tv(compactNumber(blueScore),27,0xffc5dcff,true);blueVal.setGravity(Gravity.CENTER);blueCard.addView(blueVal,new LinearLayout.LayoutParams(-1,dp(48)));
                teams.addView(redCard,new LinearLayout.LayoutParams(0,dp(86),1));teams.addView(vs,new LinearLayout.LayoutParams(dp(52),dp(86)));teams.addView(blueCard,new LinearLayout.LayoutParams(0,dp(86),1));root.addView(teams,new LinearLayout.LayoutParams(-1,dp(92)));
                LinearLayout bar=new LinearLayout(this);long sum=Math.max(1L,redScore+blueScore);TextView rb=new TextView(this);rb.setBackgroundColor(0xffe34d66);TextView bb=new TextView(this);bb.setBackgroundColor(0xff4b85e5);bar.addView(rb,new LinearLayout.LayoutParams(0,dp(10),(float)Math.max(1L,redScore)));bar.addView(bb,new LinearLayout.LayoutParams(0,dp(10),(float)Math.max(1L,blueScore)));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(10));bp.setMargins(0,dp(10),0,dp(8));root.addView(bar,bp);
                String result=redScore==blueScore?"🤝 Draw":redScore>blueScore?"🏆 Red team leads":"🏆 Blue team leads";
                if(!pkActive)result=redScore==blueScore?"🤝 PK ended in a draw":redScore>blueScore?"🏆 Red team wins":"🏆 Blue team wins";
                TextView resultView=tv(result,15,Color.WHITE,true);resultView.setGravity(Gravity.CENTER);root.addView(resultView,new LinearLayout.LayoutParams(-1,dp(42)));
                TextView giftCount=tv("🎁 PK gifts  "+gifts,11,MUTED,true);giftCount.setGravity(Gravity.CENTER);root.addView(giftCount,new LinearLayout.LayoutParams(-1,dp(28)));
                AlertDialog.Builder b=new AlertDialog.Builder(this).setTitle("⚔️ Audio PK").setView(root).setPositiveButton(pkActive?"Refresh":"Close",(d,w)->{if(pkActive)audioPkPanel700();}).setNegativeButton(pkActive?"Close":null,null);
                if(isModerator()){
                    if(pkActive)b.setNeutralButton("End PK",(d,w)->{Map<String,Object>end=new HashMap<>();end.put("active",false);end.put("endedAt",FieldValue.serverTimestamp());end.put("endedBy",user==null?"":user.getUid());state.set(end,SetOptions.merge()).addOnSuccessListener(v->{addEvent("audio_pk_end",safeName()+" ended Audio PK");audioPkPanel700();});});
                    else b.setNeutralButton("Start 3 min PK",(d,w)->{Map<String,Object>start=new HashMap<>();start.put("active",true);start.put("durationSec",180);start.put("startedAt",FieldValue.serverTimestamp());start.put("startedBy",user==null?"":user.getUid());start.put("startedByName",safeName());state.set(start,SetOptions.merge()).addOnSuccessListener(v->{addEvent("audio_pk",safeName()+" started 3-minute Audio PK ⚔️");audioPkPanel700();}).addOnFailureListener(e->toast("PK start failed: "+msg(e)));});
                }
                b.show();
            }).addOnFailureListener(e->toast("PK scores unavailable: "+msg(e)));
        }).addOnFailureListener(e->toast("PK state unavailable: "+msg(e)));
    }'''
q=q[:pk_start]+pk_method+"\n\n"+q[pk_end:]
party.write_text(q)
print('v9.4.0 visual Audio PK arena applied')


# Bolo-reference parity: visual synchronized KTV stage and request queue.
party=pkg/'PartyActivity.java'
q=party.read_text()
ktv_start=q.find("    private void ktvQueuePanel(){")
ktv_end=q.find("    private void radioMicPanel()",ktv_start)
if ktv_start<0 or ktv_end<0: raise SystemExit('KTV method boundary missing')
ktv_method=r'''    private void ktvQueuePanel(){
        if(!cloudRoom||db==null||roomId==null){karaokeDialog();return;}
        DocumentReference ktv=db.collection("live_rooms").document(roomId).collection("game_state").document("ktv");
        ktv.get().addOnSuccessListener(state->db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(120).get().addOnSuccessListener(snap->{
            boolean active=Boolean.TRUE.equals(state.getBoolean("active"));String nowSong=str(state,"song","");String singer=str(state,"singerName","");
            LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(14),dp(10),dp(14),dp(12));root.setBackgroundColor(0xff17131f);
            LinearLayout stage=new LinearLayout(this);stage.setOrientation(LinearLayout.VERTICAL);stage.setGravity(Gravity.CENTER);stage.setPadding(dp(10),dp(8),dp(10),dp(8));stage.setBackground(bg(active?0xff39234f:0xff24202c,16));
            TextView mic=tv(active?"🎤":"🎙",32,Color.WHITE,false);mic.setGravity(Gravity.CENTER);stage.addView(mic,new LinearLayout.LayoutParams(-1,dp(45)));
            TextView singerView=tv(active?singer:"KTV Stage",17,active?0xffffd75a:Color.WHITE,true);singerView.setGravity(Gravity.CENTER);stage.addView(singerView,new LinearLayout.LayoutParams(-1,dp(30)));
            TextView songView=tv(active?nowSong:"No singer on stage",12,MUTED,true);songView.setGravity(Gravity.CENTER);stage.addView(songView,new LinearLayout.LayoutParams(-1,dp(25)));root.addView(stage,new LinearLayout.LayoutParams(-1,dp(108)));

            LinearLayout actions=new LinearLayout(this);actions.setGravity(Gravity.CENTER);actions.setPadding(0,dp(6),0,dp(6));
            TextView request=tv("＋ Request Song",11,Color.WHITE,true);request.setGravity(Gravity.CENTER);request.setBackground(bg(0xff553979,12));request.setOnClickListener(v->karaokeDialog());LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(0,dp(42),1);ap.setMargins(dp(3),0,dp(3),0);actions.addView(request,ap);
            if(isModerator()){TextView control=tv(active?"⏹ End Singer":"🎛 Music",11,Color.WHITE,true);control.setGravity(Gravity.CENTER);control.setBackground(bg(0xff4a395c,12));control.setOnClickListener(v->{if(active){Map<String,Object>end=new HashMap<>();end.put("active",false);end.put("endedAt",FieldValue.serverTimestamp());end.put("endedBy",user==null?"":user.getUid());ktv.set(end,SetOptions.merge()).addOnSuccessListener(x->{addEvent("ktv_end",safeName()+" ended KTV singer");ktvQueuePanel();});}else musicPanel();});actions.addView(control,new LinearLayout.LayoutParams(ap));}
            root.addView(actions,new LinearLayout.LayoutParams(-1,dp(54)));

            TextView qTitle=tv("Song Request Queue",14,Color.WHITE,true);qTitle.setPadding(dp(2),dp(6),0,dp(5));root.addView(qTitle,new LinearLayout.LayoutParams(-1,dp(38)));
            ScrollView scroll=new ScrollView(this);LinearLayout queue=new LinearLayout(this);queue.setOrientation(LinearLayout.VERTICAL);scroll.addView(queue);
            int shown=0;String currentRequest=str(state,"requestEventId","");
            for(DocumentSnapshot req:snap.getDocuments()){
                if(!"song".equals(req.getString("type")))continue;if(req.getId().equals(currentRequest))continue;
                String raw=str(req,"text","Song request");String requestedSong=raw;int mark=raw.indexOf(" requested 🎵 ");if(mark>=0)requestedSong=raw.substring(mark+" requested 🎵 ".length()).trim();
                final String song=requestedSong;final DocumentSnapshot requestDoc=req;
                LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(10),dp(6),dp(8),dp(6));row.setBackground(bg(0xff24202e,12));
                TextView icon=tv("🎵",22,Color.WHITE,false);icon.setGravity(Gravity.CENTER);row.addView(icon,new LinearLayout.LayoutParams(dp(42),dp(46)));
                LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);TextView songText=tv(song,13,Color.WHITE,true);songText.setSingleLine(true);TextView userText=tv(str(req,"actorName","Guest"),10,MUTED,false);info.addView(songText,new LinearLayout.LayoutParams(-1,dp(24)));info.addView(userText,new LinearLayout.LayoutParams(-1,dp(18)));row.addView(info,new LinearLayout.LayoutParams(0,dp(46),1));
                TextView next=tv(isModerator()?"START ›":"WAIT",10,isModerator()?0xffffd75a:MUTED,true);next.setGravity(Gravity.CENTER);row.addView(next,new LinearLayout.LayoutParams(dp(64),dp(46)));
                row.setOnClickListener(v->{if(!isModerator()){toast("Host/co-host selects the next singer");return;}new AlertDialog.Builder(this).setTitle("Start KTV singer?").setMessage(str(requestDoc,"actorName","Guest")+" • "+song).setPositiveButton("Start",(d,w)->startKtvRequestVisual940(requestDoc,song,ktv)).setNegativeButton("Cancel",null).show();});
                LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(60));rp.setMargins(0,dp(3),0,dp(3));queue.addView(row,rp);if(++shown>=20)break;
            }
            if(shown==0){TextView empty=tv("No waiting song requests",12,MUTED,true);empty.setGravity(Gravity.CENTER);queue.addView(empty,new LinearLayout.LayoutParams(-1,dp(72)));}
            root.addView(scroll,new LinearLayout.LayoutParams(-1,Math.min(dp(360),Math.max(dp(90),shown*dp(66)+dp(20)))));
            new AlertDialog.Builder(this).setTitle("🎤 KTV").setView(root).setPositiveButton("Refresh",(d,w)->ktvQueuePanel()).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast("KTV queue unavailable: "+msg(e)))).addOnFailureListener(e->toast("KTV state unavailable: "+msg(e)));
    }
    private void startKtvRequestVisual940(DocumentSnapshot req,String song,DocumentReference ktv){
        Map<String,Object>stage=new HashMap<>();stage.put("active",true);stage.put("song",song);stage.put("singerUid",str(req,"actorUid",""));stage.put("singerName",str(req,"actorName","Guest"));stage.put("requestEventId",req.getId());stage.put("startedAt",FieldValue.serverTimestamp());stage.put("startedBy",user==null?"":user.getUid());
        ktv.set(stage,SetOptions.merge()).addOnSuccessListener(v->{addEvent("ktv_start",safeName()+" put "+str(req,"actorName","Guest")+" on KTV stage • "+song);ktvQueuePanel();}).addOnFailureListener(e->toast("KTV stage failed: "+msg(e)));
    }
'''
q=q[:ktv_start]+ktv_method+"\n\n"+q[ktv_end:]
party.write_text(q)
print('v9.4.0 visual synchronized KTV stage applied')


# Multi Video lifecycle: release owned seat on explicit leave/back so no ghost seats remain.
multi=pkg/'KingMultiVideoActivity.java'
q=multi.read_text()
q=q.replace('''back.setOnClickListener(v->finish());''','''back.setOnClickListener(v->leaveAndFinish940());''',1)
q=q.replace('''leave.setOnClickListener(v->finish());''','''leave.setOnClickListener(v->leaveAndFinish940());''',1)

marker='''    @Override protected void onResume(){'''
helpers='''    private void leaveAndFinish940(){
        if(db!=null&&mySeat>0){int seat=mySeat;mySeat=-1;micOn=false;cameraOn=false;applyMedia940();db.collection("live_rooms").document(roomId).collection("video_seats").document(String.valueOf(seat)).delete().addOnCompleteListener(x->finish());}
        else finish();
    }
    @Override public void onBackPressed(){leaveAndFinish940();}

'''
if marker not in q: raise SystemExit('Multi Video lifecycle marker missing')
q=q.replace(marker,helpers+marker,1)

old='''    @Override protected void onDestroy(){if(seatsListener!=null)seatsListener.remove();if(roomListener!=null)roomListener.remove();if(roleListener!=null)roleListener.remove();if(inviteListener!=null)inviteListener.remove();try{if(meetView!=null){meetView.dispose();meetView=null;}}catch(Throwable ignored){}org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostDestroy(this);super.onDestroy();}'''
new='''    @Override protected void onDestroy(){if(seatsListener!=null)seatsListener.remove();if(roomListener!=null)roomListener.remove();if(roleListener!=null)roleListener.remove();if(inviteListener!=null)inviteListener.remove();if(db!=null&&mySeat>0)db.collection("live_rooms").document(roomId).collection("video_seats").document(String.valueOf(mySeat)).delete().addOnFailureListener(e->{});try{if(meetView!=null){meetView.dispose();meetView=null;}}catch(Throwable ignored){}org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostDestroy(this);super.onDestroy();}'''
if old not in q: raise SystemExit('Multi Video onDestroy marker missing for seat cleanup')
q=q.replace(old,new,1)

multi.write_text(q)
print('v9.4.0 Multi Video seat cleanup applied')


# Bolo-reference parity: explicit Ludo result/rematch/exit state for all players.
ludo=pkg/'OnlineLudoActivity.java'
q=ludo.read_text()
old='''if(("finished".equals(phase)||"cancelled".equals(phase))&&owner){Button rematch=btn("↻ Rematch");rematch.setOnClickListener(v->rematch());actionsBox.addView(rematch,new LinearLayout.LayoutParams(-1,dp(52)));}}'''
new='''if("finished".equals(phase)){int winner=i(state.get("winner"));List<String> names=strList(state.get("names"));TextView resultCard=tv("🏆  "+safeName(names,winner)+" WINS",21,GOLD,true);resultCard.setGravity(Gravity.CENTER);resultCard.setBackground(bg(0xff3d3154,16));LinearLayout.LayoutParams rcp=new LinearLayout.LayoutParams(-1,dp(68));rcp.setMargins(0,dp(8),0,dp(8));actionsBox.addView(resultCard,rcp);if(owner){Button rematch=btn("↻ Play Rematch");rematch.setOnClickListener(v->rematch());actionsBox.addView(rematch,new LinearLayout.LayoutParams(-1,dp(52)));}else{TextView waiting=tv("Waiting for host to start a rematch",13,MUTED,true);waiting.setGravity(Gravity.CENTER);actionsBox.addView(waiting,new LinearLayout.LayoutParams(-1,dp(42)));}Button exit=btn("Exit Ludo");exit.setOnClickListener(v->finish());LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(-1,dp(48));ep.setMargins(0,dp(6),0,0);actionsBox.addView(exit,ep);}else if("cancelled".equals(phase)){TextView cancelled=tv("Match ended",16,MUTED,true);cancelled.setGravity(Gravity.CENTER);actionsBox.addView(cancelled,new LinearLayout.LayoutParams(-1,dp(48)));if(owner){Button rematch=btn("↻ Rematch");rematch.setOnClickListener(v->rematch());actionsBox.addView(rematch,new LinearLayout.LayoutParams(-1,dp(52)));}Button exit=btn("Exit Ludo");exit.setOnClickListener(v->finish());actionsBox.addView(exit,new LinearLayout.LayoutParams(-1,dp(48)));}}'''
if old not in q: raise SystemExit('Ludo rematch action marker missing')
q=q.replace(old,new,1)
ludo.write_text(q)
print('v9.4.0 Ludo result/rematch presentation applied')


# Production-rule compatibility: KTV and Audio PK use the already-established moderator room_settings path.
party=pkg/'PartyActivity.java'
q=party.read_text()
q=q.replace('collection("game_state").document("audio_pk")','collection("room_settings").document("audio_pk")')
q=q.replace('collection("game_state").document("ktv")','collection("room_settings").document("ktv")')
party.write_text(q)
print('v9.4.0 KTV and Audio PK production-rule compatibility applied')


# Production-rule compatible Multi Video: event-sourced seats/media/invites on the established room events stream.
multi=pkg/'KingMultiVideoActivity.java'
multi.write_text(r'''package com.kingplus.social;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KingMultiVideoActivity extends androidx.fragment.app.FragmentActivity implements org.jitsi.meet.sdk.JitsiMeetActivityInterface {
    private org.jitsi.meet.sdk.JitsiMeetView meetView;
    private String roomId="",roomName="KING Plus Multi Video",displayName="KING User";
    private FirebaseFirestore db; private FirebaseUser me; private ListenerRegistration eventsListener,roomListener,roleListener;
    private final Map<Integer,String> seatUids=new HashMap<>(),seatNames=new HashMap<>();
    private final Map<Integer,Boolean> seatMics=new HashMap<>(),seatCameras=new HashMap<>();
    private final Set<String> handledInvites940=new HashSet<>();
    private int mySeat=-1; private boolean micOn=false,cameraOn=false,moderator=false,leaving940=false;
    private LinearLayout seatsBar,controls; private TextView stateText,seatAction,micAction,camAction;
    private final Handler heartbeat940=new Handler(Looper.getMainLooper());
    private final Runnable heartbeatTask940=new Runnable(){@Override public void run(){if(mySeat>0&&!leaving940){Map<String,Object>x=new HashMap<>();x.put("seatNo",mySeat);emit940("video_presence",displayName+" is active on video seat "+mySeat,x);heartbeat940.postDelayed(this,25000L);}}};

    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    private TextView tv(String s,int z,int c,boolean b){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);if(b)t.setTypeface(null,Typeface.BOLD);return t;}
    private String safe(String s,String f){return s==null||s.trim().isEmpty()?f:s.trim();}
    private void toast(String s){if(!isFinishing())android.widget.Toast.makeText(this,s,android.widget.Toast.LENGTH_SHORT).show();}
    private CollectionReference events940(){return db.collection("live_rooms").document(roomId).collection("events");}

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        roomId=safe(getIntent().getStringExtra("roomId"),"");roomName=safe(getIntent().getStringExtra("roomName"),"KING Plus Multi Video");displayName=safe(getIntent().getStringExtra("displayName"),"KING User");
        try{db=FirebaseFirestore.getInstance();me=FirebaseAuth.getInstance().getCurrentUser();}catch(Exception ignored){}

        FrameLayout shell=new FrameLayout(this);shell.setBackgroundColor(0xff0d0b15);LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);shell.addView(body,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(10),0,dp(12),0);head.setBackgroundColor(0xff171322);
        TextView back=tv("‹",38,Color.WHITE,false);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->leaveAndFinish940());head.addView(back,new LinearLayout.LayoutParams(dp(50),dp(58)));
        LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);TextView title=tv(roomName,17,Color.WHITE,true);stateText=tv("📹 Multi Video • viewer mode",11,0xffc7bdd5,true);info.addView(title,new LinearLayout.LayoutParams(-1,dp(30)));info.addView(stateText,new LinearLayout.LayoutParams(-1,dp(22)));head.addView(info,new LinearLayout.LayoutParams(0,dp(58),1));
        TextView leave=tv("Leave",12,Color.WHITE,true);leave.setGravity(Gravity.CENTER);leave.setBackground(bg(0xff7b3fd0,18));leave.setOnClickListener(v->leaveAndFinish940());head.addView(leave,new LinearLayout.LayoutParams(dp(72),dp(38)));body.addView(head,new LinearLayout.LayoutParams(-1,dp(62)));

        FrameLayout stage=new FrameLayout(this);body.addView(stage,new LinearLayout.LayoutParams(-1,0,1));TextView loading=tv("Connecting Multi Video…",14,0xffcfc8da,true);loading.setGravity(Gravity.CENTER);stage.addView(loading,new FrameLayout.LayoutParams(-1,-1));
        seatsBar=new LinearLayout(this);seatsBar.setGravity(Gravity.CENTER);seatsBar.setPadding(dp(8),dp(5),dp(8),dp(3));seatsBar.setBackgroundColor(0xff171322);body.addView(seatsBar,new LinearLayout.LayoutParams(-1,dp(68)));
        controls=new LinearLayout(this);controls.setGravity(Gravity.CENTER);controls.setPadding(dp(6),dp(3),dp(6),dp(8));controls.setBackgroundColor(0xff171322);
        seatAction=control("＋ Sit Down",()->toggleSeat940());micAction=control("🎤 Mic OFF",()->toggleMic940());camAction=control("📷 Camera OFF",()->toggleCamera940());
        controls.addView(seatAction,controlLp());controls.addView(micAction,controlLp());controls.addView(camAction,controlLp());body.addView(controls,new LinearLayout.LayoutParams(-1,dp(62)));

        try{
            org.jitsi.meet.sdk.JitsiMeet.instantiateReactNative(this);meetView=new org.jitsi.meet.sdk.JitsiMeetView(this);stage.addView(meetView,new FrameLayout.LayoutParams(-1,-1));
            String slug=("KINGPlus-"+roomId).replaceAll("[^A-Za-z0-9_-]","");org.jitsi.meet.sdk.JitsiMeetUserInfo ui=new org.jitsi.meet.sdk.JitsiMeetUserInfo();ui.setDisplayName(displayName);
            org.jitsi.meet.sdk.JitsiMeetConferenceOptions options=new org.jitsi.meet.sdk.JitsiMeetConferenceOptions.Builder().setServerURL(new java.net.URL("https://meet.jit.si")).setRoom(slug).setSubject(roomName).setAudioMuted(true).setVideoMuted(true).setUserInfo(ui)
                .setFeatureFlag("welcomepage.enabled",false).setFeatureFlag("prejoinpage.enabled",false).setFeatureFlag("invite.enabled",false).setFeatureFlag("chat.enabled",false).setFeatureFlag("recording.enabled",false).setFeatureFlag("live-streaming.enabled",false).setFeatureFlag("pip.enabled",true).build();
            meetView.join(options);loading.setVisibility(View.GONE);
        }catch(Throwable e){loading.setText("Multi Video could not start • tap back and retry");}
        setContentView(shell);attachRealtime940();renderSeats940();refreshControls940();
    }

    private TextView control(String text,Runnable action){TextView v=tv(text,12,Color.WHITE,true);v.setGravity(Gravity.CENTER);v.setBackground(bg(0xff372b4c,15));v.setOnClickListener(x->action.run());return v;}
    private LinearLayout.LayoutParams controlLp(){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(48),1);p.setMargins(dp(4),0,dp(4),0);return p;}

    private void attachRealtime940(){
        if(db==null||me==null||roomId.isEmpty()){stateText.setText("📹 Multi Video • sign in required for seats");return;}
        roomListener=db.collection("live_rooms").document(roomId).addSnapshotListener((doc,e)->{if(e==null&&doc!=null&&doc.exists()){moderator=me.getUid().equals(doc.getString("ownerUid"));refreshControls940();}});
        roleListener=db.collection("live_rooms").document(roomId).collection("roles").document(me.getUid()).addSnapshotListener((doc,e)->{if(e==null&&doc!=null&&doc.exists()&&"cohost".equals(doc.getString("role")))moderator=true;refreshControls940();});
        eventsListener=events940().orderBy("createdAt",Query.Direction.DESCENDING).limit(250).addSnapshotListener((snap,e)->{if(e!=null||snap==null)return;reduceEvents940(snap);});
    }

    private void reduceEvents940(QuerySnapshot snap){
        seatUids.clear();seatNames.clear();seatMics.clear();seatCameras.clear();
        Map<String,Long> lastActive=new HashMap<>();DocumentSnapshot pendingInvite=null;Set<String> responded=new HashSet<>();
        List<DocumentSnapshot> docs=new ArrayList<>(snap.getDocuments());Collections.reverse(docs);
        for(DocumentSnapshot d:docs){
            String type=safe(d.getString("type"),""),actor=safe(d.getString("actorUid"),"");Long sn=d.getLong("seatNo");int seat=sn==null?-1:sn.intValue();Timestamp ts=d.getTimestamp("createdAt");long when=ts==null?System.currentTimeMillis():ts.toDate().getTime();
            if(!actor.isEmpty()&&(type.startsWith("video_")))lastActive.put(actor,when);
            if("video_seat_claim".equals(type)&&seat>=1&&seat<=6&&!actor.isEmpty()){removeUid940(actor);seatUids.put(seat,actor);seatNames.put(seat,safe(d.getString("actorName"),"Guest"));seatMics.put(seat,false);seatCameras.put(seat,true);}
            else if("video_seat_leave".equals(type)&&seat>=1&&actor.equals(seatUids.get(seat)))clearSeat940(seat);
            else if("video_remove".equals(type)&&seat>=1){String target=safe(d.getString("targetUid"),"");if(target.equals(seatUids.get(seat)))clearSeat940(seat);}
            else if("video_mic".equals(type)&&seat>=1&&actor.equals(seatUids.get(seat)))seatMics.put(seat,Boolean.TRUE.equals(d.getBoolean("enabled")));
            else if("video_camera".equals(type)&&seat>=1&&actor.equals(seatUids.get(seat)))seatCameras.put(seat,Boolean.TRUE.equals(d.getBoolean("enabled")));
            else if("video_invite".equals(type)&&me!=null&&me.getUid().equals(d.getString("targetUid")))pendingInvite=d;
            else if("video_invite_response".equals(type)){String id=safe(d.getString("inviteId"),"");if(!id.isEmpty())responded.add(id);}
        }
        long now=System.currentTimeMillis();for(Integer seat:new ArrayList<>(seatUids.keySet())){String uid=seatUids.get(seat);Long seen=lastActive.get(uid);if(seen!=null&&now-seen>90000L)clearSeat940(seat);}
        mySeat=-1;micOn=false;cameraOn=false;if(me!=null)for(Map.Entry<Integer,String>x:seatUids.entrySet())if(me.getUid().equals(x.getValue())){mySeat=x.getKey();micOn=Boolean.TRUE.equals(seatMics.get(mySeat));cameraOn=Boolean.TRUE.equals(seatCameras.get(mySeat));break;}
        if(mySeat>0){heartbeat940.removeCallbacks(heartbeatTask940);heartbeat940.postDelayed(heartbeatTask940,25000L);}else heartbeat940.removeCallbacks(heartbeatTask940);
        applyMedia940();renderSeats940();refreshControls940();
        if(pendingInvite!=null&&!responded.contains(pendingInvite.getId())&&mySeat<1&&!handledInvites940.contains(pendingInvite.getId()))showInvite940(pendingInvite);
    }
    private void removeUid940(String uid){for(Integer seat:new ArrayList<>(seatUids.keySet()))if(uid.equals(seatUids.get(seat)))clearSeat940(seat);}
    private void clearSeat940(int seat){seatUids.remove(seat);seatNames.remove(seat);seatMics.remove(seat);seatCameras.remove(seat);}

    private void showInvite940(DocumentSnapshot invite){
        handledInvites940.add(invite.getId());Long raw=invite.getLong("seatNo");int seat=raw==null?-1:raw.intValue();if(seat<1||seat>6)return;String inviter=safe(invite.getString("actorName"),"Host");
        new android.app.AlertDialog.Builder(this).setTitle("📹 Video seat invitation").setMessage(inviter+" invited you to video seat "+seat+".")
            .setPositiveButton("Sit Down",(d,w)->{Map<String,Object>r=new HashMap<>();r.put("inviteId",invite.getId());r.put("accepted",true);emit940("video_invite_response",displayName+" accepted a video seat invitation",r);takeSeat940(seat);})
            .setNegativeButton("Reject",(d,w)->{Map<String,Object>r=new HashMap<>();r.put("inviteId",invite.getId());r.put("accepted",false);emit940("video_invite_response",displayName+" rejected a video seat invitation",r);}).show();
    }

    private void renderSeats940(){
        if(seatsBar==null)return;seatsBar.removeAllViews();
        for(int i=1;i<=6;i++){final int seat=i;String uid=seatUids.get(i),name=seatNames.get(i);boolean mine=me!=null&&me.getUid().equals(uid);
            TextView v=tv(uid==null?"＋":(mine?"●":safe(name,"G").substring(0,1).toUpperCase()),uid==null?18:14,mine?0xffffe500:Color.WHITE,true);v.setGravity(Gravity.CENTER);v.setBackground(bg(mine?0xff51431b:(uid==null?0xff30283b:0xff47345e),24));v.setContentDescription(uid==null?"Empty video seat "+i:"Video seat "+i+" "+name);v.setOnClickListener(x->seatTap940(seat));v.setOnLongClickListener(x->{if(moderator&&uid==null){inviteToVideoSeat940(seat);return true;}return false;});
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(50),1);p.setMargins(dp(3),0,dp(3),0);seatsBar.addView(v,p);}
    }
    private void seatTap940(int seat){String uid=seatUids.get(seat);if(uid==null){if(mySeat>0){toast("Leave your current video seat first");return;}takeSeat940(seat);return;}if(me!=null&&me.getUid().equals(uid)){leaveSeat940();return;}if(moderator)new android.app.AlertDialog.Builder(this).setTitle(seatNames.get(seat)).setMessage("Remove this member from video seat "+seat+"?").setPositiveButton("Remove",(d,w)->removeSeat940(seat)).setNegativeButton("Cancel",null).show();else toast(seatNames.get(seat)+" is on video seat "+seat);}
    private void toggleSeat940(){if(mySeat>0)leaveSeat940();else{for(int i=1;i<=6;i++)if(!seatUids.containsKey(i)){takeSeat940(i);return;}toast("All video seats are occupied");}}
    private void takeSeat940(int seat){if(me==null||db==null){toast("Sign in required");return;}if(seatUids.containsKey(seat)){toast("That video seat is occupied");return;}Map<String,Object>x=new HashMap<>();x.put("seatNo",seat);emit940("video_seat_claim",displayName+" joined video seat "+seat,x);}
    private void leaveSeat940(){if(mySeat<1)return;int seat=mySeat;Map<String,Object>x=new HashMap<>();x.put("seatNo",seat);emit940("video_seat_leave",displayName+" left video seat "+seat,x);}
    private void removeSeat940(int seat){if(!moderator)return;String uid=seatUids.get(seat);if(uid==null)return;Map<String,Object>x=new HashMap<>();x.put("seatNo",seat);x.put("targetUid",uid);emit940("video_remove",displayName+" removed "+safe(seatNames.get(seat),"Guest")+" from video seat "+seat,x);}
    private void toggleMic940(){if(mySeat<1){toast("Sit on a video seat first");return;}boolean next=!micOn;Map<String,Object>x=new HashMap<>();x.put("seatNo",mySeat);x.put("enabled",next);emit940("video_mic",displayName+(next?" turned video mic on":" turned video mic off"),x);}
    private void toggleCamera940(){if(mySeat<1){toast("Sit on a video seat first");return;}boolean next=!cameraOn;Map<String,Object>x=new HashMap<>();x.put("seatNo",mySeat);x.put("enabled",next);emit940("video_camera",displayName+(next?" turned camera on":" turned camera off"),x);}

    private void inviteToVideoSeat940(int seat){
        if(!moderator||db==null||me==null){toast("Host/co-host only");return;}
        db.collection("live_rooms").document(roomId).collection("members").limit(100).get().addOnSuccessListener(snap->{List<DocumentSnapshot>docs=new ArrayList<>();List<String>rows=new ArrayList<>();for(DocumentSnapshot d:snap.getDocuments()){String uid=d.getString("uid");if(uid==null||uid.isEmpty()||uid.equals(me.getUid())||seatUids.containsValue(uid))continue;docs.add(d);rows.add(safe(d.getString("name"),"KING User"));}if(rows.isEmpty()){toast("No available room members to invite");return;}new android.app.AlertDialog.Builder(this).setTitle("Invite to video seat "+seat).setItems(rows.toArray(new String[0]),(dlg,w)->{DocumentSnapshot target=docs.get(w);Map<String,Object>x=new HashMap<>();x.put("seatNo",seat);x.put("targetUid",target.getString("uid"));x.put("targetName",safe(target.getString("name"),"KING User"));emit940("video_invite",displayName+" invited "+safe(target.getString("name"),"KING User")+" to video seat "+seat,x);}).setNegativeButton("Close",null).show();}).addOnFailureListener(e->toast("Members unavailable: "+e.getMessage()));
    }

    private void emit940(String type,String text,Map<String,Object> extras){
        if(db==null||me==null||roomId.isEmpty())return;Map<String,Object>d=new HashMap<>();d.put("actorUid",me.getUid());d.put("actorName",displayName);d.put("type",type);d.put("text",text);d.put("createdAt",FieldValue.serverTimestamp());if(extras!=null)d.putAll(extras);events940().add(d).addOnFailureListener(e->toast("Multi Video sync failed"));
    }

    private void applyMedia940(){applyAudio940(mySeat<1||!micOn);applyVideo940(mySeat<1||!cameraOn);}
    private void applyAudio940(boolean muted){try{Intent i=org.jitsi.meet.sdk.BroadcastIntentHelper.buildSetAudioMutedIntent(muted);LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);}catch(Throwable ignored){}}
    private void applyVideo940(boolean muted){try{Intent i=org.jitsi.meet.sdk.BroadcastIntentHelper.buildSetVideoMutedIntent(muted);LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);}catch(Throwable ignored){}}
    private void refreshControls940(){if(stateText!=null)stateText.setText(mySeat>0?("📹 Video seat "+mySeat+(moderator?" • Host controls":"")):("📹 Multi Video • viewer mode"+(moderator?" • Host controls":"")));if(seatAction!=null)seatAction.setText(mySeat>0?"↥ Leave Seat":"＋ Sit Down");if(micAction!=null){micAction.setText(micOn?"🎤 Mic ON":"🎤 Mic OFF");micAction.setAlpha(mySeat>0?1f:.45f);}if(camAction!=null){camAction.setText(cameraOn?"📷 Camera ON":"📷 Camera OFF");camAction.setAlpha(mySeat>0?1f:.45f);}}

    private void leaveAndFinish940(){if(leaving940)return;leaving940=true;heartbeat940.removeCallbacks(heartbeatTask940);if(mySeat>0){int seat=mySeat;Map<String,Object>x=new HashMap<>();x.put("seatNo",seat);if(db!=null&&me!=null)events940().add(new HashMap<String,Object>(){{put("actorUid",me.getUid());put("actorName",displayName);put("type","video_seat_leave");put("text",displayName+" left video seat "+seat);put("seatNo",seat);put("createdAt",FieldValue.serverTimestamp());}}).addOnCompleteListener(v->finish());else finish();}else finish();}
    @Override public void onBackPressed(){leaveAndFinish940();}
    @Override protected void onResume(){super.onResume();org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostResume(this);}
    @Override protected void onStop(){org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostPause(this);super.onStop();}
    @Override public void onNewIntent(Intent intent){super.onNewIntent(intent);org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onNewIntent(intent);}
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onActivityResult(this,requestCode,resultCode,data);}
    @Override public void requestPermissions(String[] permissions,int requestCode,com.facebook.react.modules.core.PermissionListener listener){org.jitsi.meet.sdk.JitsiMeetActivityDelegate.requestPermissions(this,permissions,requestCode,listener);}
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onRequestPermissionsResult(requestCode,permissions,grantResults);}
    @Override protected void onDestroy(){heartbeat940.removeCallbacks(heartbeatTask940);if(eventsListener!=null)eventsListener.remove();if(roomListener!=null)roomListener.remove();if(roleListener!=null)roleListener.remove();try{if(meetView!=null){meetView.dispose();meetView=null;}}catch(Throwable ignored){}org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostDestroy(this);super.onDestroy();}
}''')
print('v9.4.0 production-rule compatible event-sourced Multi Video applied')


# Harden event-sourced Multi Video for long sessions and owner/co-host listener ordering.
multi=pkg/'KingMultiVideoActivity.java'
q=multi.read_text()
q=q.replace('''private int mySeat=-1; private boolean micOn=false,cameraOn=false,moderator=false,leaving940=false;''',
'''private int mySeat=-1; private boolean micOn=false,cameraOn=false,moderator=false,owner940=false,cohost940=false,leaving940=false;''',1)
q=q.replace('''Map<String,Object>x=new HashMap<>();x.put("seatNo",mySeat);emit940("video_presence",displayName+" is active on video seat "+mySeat,x);''',
'''Map<String,Object>x=new HashMap<>();x.put("seatNo",mySeat);x.put("micOn",micOn);x.put("cameraOn",cameraOn);emit940("video_presence",displayName+" is active on video seat "+mySeat,x);''',1)
q=q.replace('''roomListener=db.collection("live_rooms").document(roomId).addSnapshotListener((doc,e)->{if(e==null&&doc!=null&&doc.exists()){moderator=me.getUid().equals(doc.getString("ownerUid"));refreshControls940();}});
        roleListener=db.collection("live_rooms").document(roomId).collection("roles").document(me.getUid()).addSnapshotListener((doc,e)->{if(e==null&&doc!=null&&doc.exists()&&"cohost".equals(doc.getString("role")))moderator=true;refreshControls940();});''',
'''roomListener=db.collection("live_rooms").document(roomId).addSnapshotListener((doc,e)->{owner940=e==null&&doc!=null&&doc.exists()&&me.getUid().equals(doc.getString("ownerUid"));refreshModerator940();});
        roleListener=db.collection("live_rooms").document(roomId).collection("roles").document(me.getUid()).addSnapshotListener((doc,e)->{cohost940=e==null&&doc!=null&&doc.exists()&&"cohost".equals(doc.getString("role"));refreshModerator940();});''',1)
q=q.replace('''            if("video_seat_claim".equals(type)&&seat>=1&&seat<=6&&!actor.isEmpty()){removeUid940(actor);seatUids.put(seat,actor);seatNames.put(seat,safe(d.getString("actorName"),"Guest"));seatMics.put(seat,false);seatCameras.put(seat,true);}
            else if("video_seat_leave".equals(type)&&seat>=1&&actor.equals(seatUids.get(seat)))clearSeat940(seat);''',
'''            if("video_seat_claim".equals(type)&&seat>=1&&seat<=6&&!actor.isEmpty()){removeUid940(actor);seatUids.put(seat,actor);seatNames.put(seat,safe(d.getString("actorName"),"Guest"));seatMics.put(seat,false);seatCameras.put(seat,true);}
            else if("video_presence".equals(type)&&seat>=1&&seat<=6&&!actor.isEmpty()){removeUid940(actor);seatUids.put(seat,actor);seatNames.put(seat,safe(d.getString("actorName"),"Guest"));seatMics.put(seat,Boolean.TRUE.equals(d.getBoolean("micOn")));seatCameras.put(seat,Boolean.TRUE.equals(d.getBoolean("cameraOn")));}
            else if("video_seat_leave".equals(type)&&seat>=1&&actor.equals(seatUids.get(seat)))clearSeat940(seat);''',1)
const marker='''    private void reduceEvents940(QuerySnapshot snap){''';
const helper='''    private void refreshModerator940(){moderator=owner940||cohost940;refreshControls940();}

''';
if(!q.includes(marker)) throw new Error("Multi Video reducer marker missing");
q=q.replace(marker,helper+marker,1);
multi.write_text(q)
print('v9.4.0 Multi Video heartbeat and moderator hardening applied')

print('v9.4.0 parity batch 1 applied')
