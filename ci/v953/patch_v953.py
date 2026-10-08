from pathlib import Path
import sys,re

root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'

def replace_method(path, signature, replacement):
    p=pkg/path
    s=p.read_text()
    start=s.find(signature)
    if start<0: raise SystemExit(f'{path}: signature not found: {signature}')
    brace=s.find('{',start)
    depth=0; state='code'; quote=''; esc=False; i=brace; end=None
    while i<len(s):
        ch=s[i]; nx=s[i+1] if i+1<len(s) else ''
        if state=='string':
            if esc: esc=False
            elif ch=='\\': esc=True
            elif ch==quote: state='code'
        elif state=='line':
            if ch=='\n': state='code'
        elif state=='block':
            if ch=='*' and nx=='/': state='code'; i+=1
        else:
            if ch=='/' and nx=='/': state='line'; i+=1
            elif ch=='/' and nx=='*': state='block'; i+=1
            elif ch in ('"',"'"): state='string'; quote=ch
            elif ch=='{': depth+=1
            elif ch=='}':
                depth-=1
                if depth==0: end=i+1; break
        i+=1
    if end is None: raise SystemExit(f'{path}: method end not found')
    p.write_text(s[:start]+replacement+s[end:])

# Preserve typed Create Party room name across cover picker and option refreshes.
p=pkg/'PartyActivity.java'; s=p.read_text()
if 'pendingCreateName953' not in s:
    m=re.search(r'(?m)^\s*private\s+int\s+pendingCreateSeats[^;]*;',s)
    if not m: raise SystemExit('pendingCreateSeats field marker missing')
    s=s[:m.end()]+'\n    private String pendingCreateName953="";'+s[m.end():]
    p.write_text(s)

replace_method('PartyActivity.java','private void renderCreateRoomPage()',r'''private void renderCreateRoomPage(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(14),dp(6),dp(14),dp(10));root.setBackgroundColor(0xff07372b);

        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);
        TextView back=tv("‹",32,Color.WHITE,false);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->renderLobby("Hot"));head.addView(back,new LinearLayout.LayoutParams(dp(44),dp(50)));
        TextView title953=tv("Create Party",18,0xffe9f7f2,true);title953.setGravity(Gravity.CENTER);head.addView(title953,new LinearLayout.LayoutParams(0,dp(50),1));
        TextView help953=tv("ⓘ",19,0xffd0ebe2,true);help953.setGravity(Gravity.CENTER);help953.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Create Party").setMessage("Choose a cover, privacy, mic seats and room type. Up to 100 listeners can join while the selected seats are used for stage / mic users.").setPositiveButton("OK",null).show());head.addView(help953,new LinearLayout.LayoutParams(dp(44),dp(50)));root.addView(head);

        ScrollView sv=new ScrollView(this);sv.setFillViewport(false);LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(4),0,dp(4),dp(8));sv.addView(body);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));

        FrameLayout cover=new FrameLayout(this);cover.setBackground(bg(0x33000000,18));
        KingRoomCoverView createCover940=new KingRoomCoverView(this,pendingCreateCategory,safeName()+"'s Party");cover.addView(createCover940,new FrameLayout.LayoutParams(-1,-1));
        ImageView preview=new ImageView(this);preview.setScaleType(ImageView.ScaleType.CENTER_CROP);cover.addView(preview,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout overlay953=new LinearLayout(this);overlay953.setOrientation(LinearLayout.VERTICAL);overlay953.setGravity(Gravity.BOTTOM|Gravity.LEFT);overlay953.setPadding(dp(12),dp(10),dp(12),dp(10));overlay953.setBackground(bg(0x52000000,18));
        TextView coverTitle953=tv("▣  Change cover",13,Color.WHITE,true);TextView coverType953=tv(pendingCreateCategory+"  •  "+(pendingCreatePrivate?"Private":"Public"),11,0xffe6eee9,true);overlay953.addView(coverTitle953,new LinearLayout.LayoutParams(-1,dp(28)));overlay953.addView(coverType953,new LinearLayout.LayoutParams(-1,dp(23)));cover.addView(overlay953,new FrameLayout.LayoutParams(-1,-1));
        cover.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("image/*");i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);startActivityForResult(i,ROOM_COVER_PICK_REQUEST);});
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(178));cp.setMargins(0,dp(6),0,dp(7));body.addView(cover,cp);
        if(pendingCreateCoverUri!=null){try{preview.setImageURI(pendingCreateCoverUri);coverTitle953.setText("✎  Change cover");}catch(Exception ignored){}}
        TextView tip=tv("A clear cover helps your room stand out",11,0xffffe86c,true);tip.setGravity(Gravity.CENTER);body.addView(tip,new LinearLayout.LayoutParams(-1,dp(30)));

        LinearLayout flags=new LinearLayout(this);flags.setGravity(Gravity.CENTER);
        TextView pub=tv(pendingCreatePrivate?"🔒  Private":"🌐  Public",13,Color.WHITE,true);pub.setGravity(Gravity.CENTER);pub.setBackground(bg(0x2b4fd0aa,14));
        LinearLayout.LayoutParams f953=new LinearLayout.LayoutParams(0,dp(48),1);f953.setMargins(dp(2),0,dp(4),0);flags.addView(pub,f953);
        TextView seat=tv("🎙  "+pendingCreateSeats+" Seats",13,Color.WHITE,true);seat.setGravity(Gravity.CENTER);seat.setBackground(bg(0x2b4fd0aa,14));LinearLayout.LayoutParams s953=new LinearLayout.LayoutParams(0,dp(48),1);s953.setMargins(dp(4),0,dp(2),0);flags.addView(seat,s953);body.addView(flags,new LinearLayout.LayoutParams(-1,dp(54)));

        final LinearLayout seatPreview953=new LinearLayout(this);seatPreview953.setOrientation(LinearLayout.VERTICAL);
        TextView seatsTitle953=tv("Stage / Mic Preview",12,0xffbfe5d8,true);seatsTitle953.setPadding(dp(2),dp(4),0,0);body.addView(seatsTitle953,new LinearLayout.LayoutParams(-1,dp(30)));
        renderCreateSeatPreview953(seatPreview953,pendingCreateSeats);body.addView(seatPreview953,new LinearLayout.LayoutParams(-1,-2));

        pub.setOnClickListener(v->{pendingCreatePrivate=!pendingCreatePrivate;pub.setText(pendingCreatePrivate?"🔒  Private":"🌐  Public");coverType953.setText(pendingCreateCategory+"  •  "+(pendingCreatePrivate?"Private":"Public"));});
        seat.setOnClickListener(v->{String[] values={"8 seats","10 seats","12 seats"};new AlertDialog.Builder(this).setTitle("Stage / mic seats").setItems(values,(d,w)->{pendingCreateSeats=w==0?8:w==1?10:12;seat.setText("🎙  "+pendingCreateSeats+" Seats");renderCreateSeatPreview953(seatPreview953,pendingCreateSeats);}).setNegativeButton("Cancel",null).show();});

        LinearLayout nameBox953=new LinearLayout(this);nameBox953.setOrientation(LinearLayout.VERTICAL);nameBox953.setPadding(dp(12),dp(5),dp(12),dp(4));nameBox953.setBackground(bg(0x22000000,12));
        TextView nameLabel953=tv("Room name",11,0xff93b8ac,true);nameBox953.addView(nameLabel953,new LinearLayout.LayoutParams(-1,dp(24)));
        final EditText name=new EditText(this);name.setHint("Room name");name.setHintTextColor(0xff9cb8ae);name.setTextColor(Color.WHITE);name.setTextSize(17);name.setSingleLine(true);name.setBackgroundColor(Color.TRANSPARENT);name.setPadding(0,0,0,0);
        String defaultName953=pendingCreateName953==null?"":pendingCreateName953.trim();if(defaultName953.isEmpty()){defaultName953=safeName().trim();if(defaultName953.isEmpty())defaultName953="KING";defaultName953=defaultName953+"'s Party";}name.setText(defaultName953);name.setSelection(name.getText().length());
        name.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){pendingCreateName953=s==null?"":s.toString();}public void afterTextChanged(android.text.Editable e){}});
        nameBox953.addView(name,new LinearLayout.LayoutParams(-1,dp(42)));LinearLayout.LayoutParams nlp953=new LinearLayout.LayoutParams(-1,dp(72));nlp953.setMargins(0,dp(8),0,dp(6));body.addView(nameBox953,nlp953);

        TextView type=tv("Room type:  "+pendingCreateCategory+"   ›",13,0xffe4f5ef,true);type.setGravity(Gravity.CENTER_VERTICAL);type.setPadding(dp(12),0,dp(10),0);type.setBackground(bg(0x22000000,12));
        type.setOnClickListener(v->{String[] cats={"Hot","Chat","Event","Date","Music","Game","KTV","Radio","PK","Pick Me","Family","Multi Video"};new AlertDialog.Builder(this).setTitle("Choose room type").setItems(cats,(d,w)->{pendingCreateCategory=cats[w];type.setText("Room type:  "+pendingCreateCategory+"   ›");coverType953.setText(pendingCreateCategory+"  •  "+(pendingCreatePrivate?"Private":"Public"));}).show();});body.addView(type,new LinearLayout.LayoutParams(-1,dp(52)));
        TextView audience953=tv("👥 Up to 100 listeners  •  "+pendingCreateSeats+" stage seats",11,0xffbfe5d8,true);audience953.setGravity(Gravity.CENTER);body.addView(audience953,new LinearLayout.LayoutParams(-1,dp(34)));

        TextView start=tv(roomCreateInFlight921?"Creating Party…":"🎉  Start Room",16,0xff171717,true);start.setGravity(Gravity.CENTER);start.setBackground(bg(roomCreateInFlight921?0xffb9ad48:0xffffee00,14));start.setEnabled(!roomCreateInFlight921);
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(56));sp.setMargins(dp(4),dp(6),dp(4),dp(2));root.addView(start,sp);
        start.setOnClickListener(v->{if(roomCreateInFlight921)return;String n=name.getText().toString().trim();if(n.length()<2){String base=safeName().trim();if(base.isEmpty())base="KING";n=base+"'s Party";name.setText(n);}pendingCreateName953=n;createCloudRoom(n,pendingCreateCategory,pendingCreateSeats,pendingCreatePrivate);});
        setSafeContentView(root);
    }''')

# Create room seat preview helper.
p=pkg/'PartyActivity.java'; s=p.read_text()
marker='''    private void createCloudRoom('''
helper=r'''    private void renderCreateSeatPreview953(LinearLayout host,int count){host.removeAllViews();int rows=(count+3)/4,no=1;for(int r=0;r<rows;r++){LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);for(int c=0;c<4;c++){if(no<=count){TextView seat=tv(no==1?"👑\\nHOST":"＋\\n"+no,10,0xffd8eee7,true);seat.setGravity(Gravity.CENTER);seat.setBackground(bg(no==1?0x445fd7b2:0x2b4fd0aa,40));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(62),1);lp.setMargins(dp(5),dp(3),dp(5),dp(3));row.addView(seat,lp);no++;}else row.addView(new View(this),new LinearLayout.LayoutParams(0,dp(62),1));}host.addView(row,new LinearLayout.LayoutParams(-1,dp(68)));}}
    
'''
if marker not in s: raise SystemExit('Party createCloudRoom helper marker missing')
s=s.replace(marker,helper+marker,1);p.write_text(s)

replace_method('KingPublicProfileActivity.java','private void showProfile(DocumentSnapshot p)',r'''private void showProfile(DocumentSnapshot p){
        body.removeAllViews();String n=p.exists()?safe(p.getString("displayName"),name):name;name=n;Long lv=p.getLong("level"),vip=p.getLong("vipLevel"),xp=p.getLong("xp"),vipPoints=p.getLong("vipPoints");String frame=safe(p.getString("equippedFrame"),"Minimal Frame"),effect=safe(p.getString("entranceEffect"),"Welcome Sparkle"),badge=safe(p.getString("nameBadge"),"None"),bubble=safe(p.getString("chatBubble"),"Default Bubble"),bio=safe(p.getString("bio"),"KING Plus member"),tags=safe(p.getString("tags"),"");String photo=safe(p.getString("photoUrl"),"");
        LinearLayout profileHead=new LinearLayout(this);profileHead.setGravity(Gravity.CENTER_VERTICAL);profileHead.setPadding(dp(12),dp(10),dp(12),dp(10));profileHead.setBackground(bg(Color.WHITE,18));
        android.widget.FrameLayout avatar=new android.widget.FrameLayout(this);TextView fallback=tv(n.isEmpty()?"K":n.substring(0,1).toUpperCase(),28,Color.WHITE,true);fallback.setGravity(Gravity.CENTER);fallback.setBackground(bg(0xff8a63db,40));avatar.addView(fallback,new android.widget.FrameLayout.LayoutParams(-1,-1));android.widget.ImageView image=new android.widget.ImageView(this);image.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);avatar.addView(image,new android.widget.FrameLayout.LayoutParams(-1,-1));if(!photo.isEmpty())loadProfilePhoto940(image,fallback,photo);profileHead.addView(avatar,new LinearLayout.LayoutParams(dp(86),dp(86)));
        LinearLayout idBlock=new LinearLayout(this);idBlock.setOrientation(LinearLayout.VERTICAL);idBlock.setPadding(dp(12),0,0,0);TextView nm=tv(n,20,INK,true);idBlock.addView(nm,new LinearLayout.LayoutParams(-1,dp(32)));TextView pid=tv("◇ KING ID  "+publicId(uid)+" ◇",12,MUTED,true);pid.setBackground(bg(0xfff2eff6,14));idBlock.addView(pid,new LinearLayout.LayoutParams(-1,dp(32)));TextView lvLine=tv("VIP "+(vip==null?0:vip)+"     Lv."+(lv==null?1:lv),12,PURPLE,true);idBlock.addView(lvLine,new LinearLayout.LayoutParams(-1,dp(28)));profileHead.addView(idBlock,new LinearLayout.LayoutParams(0,dp(90),1));body.addView(profileHead,new LinearLayout.LayoutParams(-1,dp(108)));

        TextView hero=tv("KING Plus • "+LevelSystem.levelTier((int)(long)(lv==null?1L:lv))+"  •  "+(xp==null?0:xp)+" XP",13,MUTED,true);hero.setGravity(Gravity.CENTER);hero.setBackground(bg(0xfffff0a5,14));body.addView(hero,new LinearLayout.LayoutParams(-1,dp(44)));
        TextView cosmetics=tv("🖼 "+frame+"     ✨ "+effect+(badge.equals("None")?"":"\\n"+KingCosmetics.badgeEmoji(badge)+" "+badge+"     💬 "+bubble),12,PURPLE,true);cosmetics.setGravity(Gravity.CENTER_VERTICAL);cosmetics.setBackground(bg(0xfff7f3fb,13));LinearLayout.LayoutParams c953=new LinearLayout.LayoutParams(-1,dp(badge.equals("None")?48:62));c953.setMargins(0,dp(5),0,0);body.addView(cosmetics,c953);
        TextView about=tv(bio+(tags.isEmpty()?"":"\\n# "+tags),14,INK,false);about.setBackground(bg(Color.WHITE,14));about.setPadding(dp(12),dp(8),dp(12),dp(8));body.addView(about,new LinearLayout.LayoutParams(-1,dp(88)));

        LinearLayout actions=new LinearLayout(this);actions.setGravity(Gravity.CENTER);followBtn=action("＋ Follow");followBtn.setOnClickListener(v->KingSafe.run(this,"public-profile-follow",this::toggleFollow));actions.addView(followBtn,new LinearLayout.LayoutParams(0,dp(50),1));TextView msg=action("✉ Message");msg.setOnClickListener(v->KingSafe.run(this,"public-profile-chat",this::openChat));actions.addView(msg,new LinearLayout.LayoutParams(0,dp(50),1));TextView gift=action("🎁 Gift");gift.setOnClickListener(v->KingSafe.run(this,"public-profile-gift",this::openGift));actions.addView(gift,new LinearLayout.LayoutParams(0,dp(50),1));body.addView(actions);addPublicCounts940();

        section("Profile & Collection");
        row("⭐ Level & VIP","XP "+(xp==null?0:xp)+" • VIP "+(vip==null?0:vip)+" • "+(vipPoints==null?0:vipPoints)+" points",()->openDeep("vip"));
        row("🎭 Collection & cosmetics","Frame, entrance, chat bubble and medals",()->showPublicCollection953(p));
        row("🎁 Gift history","Secure sent/received gift ledger",this::openGift);
        row("💞 Relationship","CP / relationship center",()->openCommunity("Relationship"));
        row("🏠 Family","Family square and party",()->openCommunity("Family"));
        section("Safety & Sharing");row("↗ Share KING ID","Share this profile",this::share);row("⚑ Report","Report this user",this::report);row("🚫 Block / Unblock","Keep this user out of your social feed",this::toggleBlock);checkFollow();
    }''')

# Public collection detail helper.
p=pkg/'KingPublicProfileActivity.java'; s=p.read_text()
marker='''    private void loadProfilePhoto940('''
helper=r'''    private void showPublicCollection953(DocumentSnapshot p){Long lv=p.getLong("level"),vip=p.getLong("vipLevel"),xp=p.getLong("xp"),vp=p.getLong("vipPoints");String frame=safe(p.getString("equippedFrame"),"Minimal Frame"),effect=safe(p.getString("entranceEffect"),"Welcome Sparkle"),bubble=safe(p.getString("chatBubble"),"Default Bubble"),badge=safe(p.getString("nameBadge"),"None");LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(14),dp(8),dp(14),dp(10));TextView title=tv("👑 "+name+" • Collection",17,INK,true);title.setGravity(Gravity.CENTER);root.addView(title,new LinearLayout.LayoutParams(-1,dp(44)));String medals=((lv==null?1:lv)>=10?"🏅 Rising Star  ":"")+((lv==null?1:lv)>=30?"🎖 Elite Voice  ":"")+((vip==null?0:vip)>=3?"💎 VIP Supporter  ":"")+((vip==null?0:vip)>=8?"👑 Royal Patron":"");TextView progress=tv("⭐ Lv."+(lv==null?1:lv)+" • "+(xp==null?0:xp)+" XP\\n💎 VIP "+(vip==null?0:vip)+" • "+(vp==null?0:vp)+" points",13,INK,true);progress.setBackground(bg(0xfffff1bd,14));progress.setPadding(dp(12),dp(8),dp(12),dp(8));root.addView(progress,new LinearLayout.LayoutParams(-1,dp(66)));TextView cosmetic=tv("🖼 "+frame+"\\n✨ "+effect+"\\n💬 "+bubble+(badge.equals("None")?"":"\\n"+KingCosmetics.badgeEmoji(badge)+" "+badge),12,PURPLE,true);cosmetic.setBackground(bg(0xfff6f2fa,14));cosmetic.setPadding(dp(12),dp(8),dp(12),dp(8));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,badge.equals("None")?dp(86):dp(106));cp.setMargins(0,dp(6),0,dp(6));root.addView(cosmetic,cp);TextView medal=tv(medals.isEmpty()?"No public medals unlocked yet":medals,12,MUTED,true);medal.setGravity(Gravity.CENTER);root.addView(medal,new LinearLayout.LayoutParams(-1,dp(48)));new AlertDialog.Builder(this).setTitle("Collection").setView(root).setPositiveButton("Send Gift",(d,w)->openGift()).setNegativeButton("Close",null).show();}

'''
if marker not in s: raise SystemExit('PublicProfile helper marker missing')
s=s.replace(marker,helper+marker,1);p.write_text(s)

replace_method('KingDeepFlowActivity.java','private void nobleCenter()',r'''private void nobleCenter(){
        LevelSystem.Snapshot s=LevelSystem.read(this);int noble=Math.min(10,s.vipLevel/5);int nextNoble=Math.min(10,noble+1);int targetVip=Math.min(50,Math.max(5,nextNoble*5));
        hero("♛ Noble "+noble,"VIP "+s.vipLevel+" • next Noble tier at VIP "+targetVip);
        android.widget.ProgressBar bar953=new android.widget.ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);bar953.setMax(50);bar953.setProgress(Math.min(50,s.vipLevel));LinearLayout.LayoutParams bp953=new LinearLayout.LayoutParams(-1,dp(16));bp953.setMargins(dp(4),dp(4),dp(4),dp(8));body.addView(bar953,bp953);
        TextView status953=tv(noble>=10?"MAX NOBLE 10":"VIP "+s.vipLevel+" / "+targetVip+" for Noble "+nextNoble,12,sub(),true);status953.setGravity(Gravity.CENTER);body.addView(status953,new LinearLayout.LayoutParams(-1,dp(34)));
        section("Noble privileges");
        String[] perks953={"Profile crown & Noble identity","Priority room entrance styling","Noble seat / name presentation","Exclusive badge & collection status"};
        for(int i=0;i<perks953.length;i++)row(i<noble?"♛":"◇",perks953[i],noble>i?"Active":"Progress through VIP to unlock","detail_noble_"+Math.min(10,i+1));
        section("Noble tiers");
        for(int i=1;i<=10;i++){final int n=i;row(i<=noble?"♛":"🔒","Noble "+i,"Unlocks at VIP "+(i*5),()->{if(s.vipLevel<n*5)toast("Unlocks at VIP "+(n*5));else go("detail_noble_"+n);});}
    }''')

replace_method('KingDeepFlowActivity.java','private void privilegePack()',r'''private void privilegePack(){
        LevelSystem.Snapshot s=LevelSystem.read(this);hero("🎁 Privilege Pack","VIP "+s.vipLevel+" • Lv."+s.level+" • your unlocked KING identity");
        TextView current953=tv("🖼 "+KingCosmetics.frame(this)+"\\n✨ "+KingCosmetics.effect(this)+"\\n💬 "+KingCosmetics.bubble(this),13,INK,true);current953.setPadding(dp(12),dp(8),dp(12),dp(8));current953.setBackground(bg(0xfffff2c8,14));body.addView(current953,new LinearLayout.LayoutParams(-1,dp(82)));
        section("Identity");row("🖼","ID / Profile Frames","Minimal, Music, Heart, Royal, Galaxy, Crown","detail_frames");row("✨","Entrance Effects","Sparkle, Crown Drop, Rose Shower, Galaxy Portal, Royal Arrival","detail_entrance");row("💬","Chat Bubble","VIP identity styling and message badge","detail_chat_bubble");row("🏷","Name Badge","VIP + Level badges in room and chat","detail_name_badge");
        section("Room");row("🎨","Room Theme","Green, Royal, Galaxy and event themes","detail_room_theme");row("🪑","VIP Seat Style","Seat frame and identity","detail_vip_seat");
        section("Manage");row("🛍","Open Privilege Shop","Equip unlocked frames, effects and bubbles","privilege_shop");row("💎","VIP Progress","Review VIP tiers and privileges","vip");
    }''')

gradle=root/'app/build.gradle'; g=gradle.read_text()
old="versionCode 143; versionName '9.5.2-parity-batch3'"
if old not in g: raise SystemExit('v9.5.2 version marker missing')
gradle.write_text(g.replace(old,"versionCode 144; versionName '9.5.3-parity-batch4'",1))
print('KING Plus v9.5.3 Create Party Public Profile Noble Privilege parity batch applied')
