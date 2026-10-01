from pathlib import Path
import re
P=Path('app/src/main/java/com/kingplus/social/PartyActivity.java')
B=Path('app/build.gradle')
s=P.read_text(encoding='utf-8')

s=s.replace('private int maxSeats = 12;','private int maxSeats = 8;',1)
s=s.replace('r.put("category",category); r.put("theme","Classic"); r.put("maxSeats",12);','r.put("category",category); r.put("theme","Classic"); r.put("maxSeats",8);',1)
s=s.replace('maxSeats=prefs.getInt("maxSeats_"+name,12);','maxSeats=prefs.getInt("maxSeats_"+name,8);',1)

start=s.index('    private void renderParty() {')
end=s.index('\n    private void addMemberStrip()', start)
new_render=r'''    private void renderParty() {
        clearListeners();
        FrameLayout shell=new FrameLayout(this);shell.setBackgroundColor(themeBackground());
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(themeBackground());scroll.setVerticalScrollBarEnabled(false);
        page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(14),dp(10),dp(14),dp(18));scroll.addView(page);
        FrameLayout.LayoutParams bodyLp=new FrameLayout.LayoutParams(-1,-1);bodyLp.bottomMargin=dp(68);shell.addView(scroll,bodyLp);
        setSafeContentView(shell);

        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);
        TextView back=tv("‹",38,Color.WHITE,true);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->leaveRoom());head.addView(back,new LinearLayout.LayoutParams(dp(48),dp(58)));
        LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);info.setPadding(dp(5),0,0,0);
        TextView rn=tv(roomName,18,Color.WHITE,true);rn.setSingleLine(true);rn.setOnClickListener(v->hostProfileDialog());info.addView(rn,new LinearLayout.LayoutParams(-1,dp(30)));
        LinearLayout mini=new LinearLayout(this);mini.setGravity(Gravity.CENTER_VERTICAL);TextView miniAv=tv("👑",13,Color.WHITE,true);miniAv.setGravity(Gravity.CENTER);miniAv.setBackground(bg(0x665b3a78,30));mini.addView(miniAv,new LinearLayout.LayoutParams(dp(28),dp(28)));viewerLabel=pill("1",0x66352b43,this::membersDialog);viewerLabel.setTextSize(11);LinearLayout.LayoutParams vlp=new LinearLayout.LayoutParams(dp(38),dp(28));vlp.setMargins(dp(5),0,0,0);mini.addView(viewerLabel,vlp);info.addView(mini,new LinearLayout.LayoutParams(-1,dp(30)));
        head.addView(info,new LinearLayout.LayoutParams(0,dp(62),1));
        TextView share=pill("↗",0x55352b43,this::shareRoom);share.setTextSize(23);head.addView(share,new LinearLayout.LayoutParams(dp(48),dp(46)));
        TextView more=pill("☰",0x55352b43,this::roomMenu);more.setTextSize(22);LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(dp(48),dp(46));mp.setMargins(dp(7),0,0,0);head.addView(more,mp);page.addView(head,new LinearLayout.LayoutParams(-1,dp(66)));

        LinearLayout badges=new LinearLayout(this);badges.setGravity(Gravity.CENTER_VERTICAL);badges.setPadding(0,dp(3),0,dp(6));
        String compact=shortId();if(compact.length()>2)compact=compact.substring(compact.length()-2);
        TextView no=pill("🔷  No."+compact,0x66231c35,null);badges.addView(no,new LinearLayout.LayoutParams(0,dp(38),1));
        TextView heart=pill("💗  Lv. 1 Heart",0x6623152f,null);LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(0,dp(38),1);hp.setMargins(dp(7),0,0,0);badges.addView(heart,hp);
        TextView edit=pill(isOwner()?"▣ Edit":"▣ Billboard",0x66241d35,isOwner()?this::roomMenu:this::roomRankingDialog);LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(0,dp(38),1);ep.setMargins(dp(7),0,0,0);badges.addView(edit,ep);page.addView(badges,new LinearLayout.LayoutParams(-1,dp(46)));

        roomLiveBadge=pill("👥 1 • 🎙 0",0x00302747,null);roomLiveBadge.setVisibility(View.GONE);page.addView(roomLiveBadge,new LinearLayout.LayoutParams(1,1));
        roomStateLabel=tv("",1,Color.TRANSPARENT,false);roomStateLabel.setVisibility(View.GONE);page.addView(roomStateLabel,new LinearLayout.LayoutParams(1,1));refreshRoomState();
        supporterLabel=tv("",1,Color.TRANSPARENT,false);supporterLabel.setVisibility(View.GONE);page.addView(supporterLabel,new LinearLayout.LayoutParams(1,1));
        announcementLabel=tv("",1,Color.TRANSPARENT,false);announcementLabel.setVisibility(View.GONE);page.addView(announcementLabel,new LinearLayout.LayoutParams(1,1));
        seatRequestLabel=tv("",1,Color.TRANSPARENT,false);seatRequestLabel.setVisibility(View.GONE);page.addView(seatRequestLabel,new LinearLayout.LayoutParams(1,1));

        reactionBanner=tv("",42,Color.WHITE,true);reactionBanner.setGravity(Gravity.CENTER);reactionBanner.setVisibility(View.GONE);reactionBanner.setBackground(bg(0x663c1f58,30));page.addView(reactionBanner,new LinearLayout.LayoutParams(-1,dp(58)));

        seatsBox=new LinearLayout(this);seatsBox.setOrientation(LinearLayout.VERTICAL);LinearLayout.LayoutParams sbp=new LinearLayout.LayoutParams(-1,-2);sbp.setMargins(0,dp(3),0,dp(8));page.addView(seatsBox,sbp);rebuildSeats();

        LinearLayout notice=new LinearLayout(this);notice.setOrientation(LinearLayout.VERTICAL);notice.setPadding(dp(12),dp(10),dp(12),dp(10));notice.setBackground(bg(0x553befe0,18));
        TextView handle=tv("⌒",25,0x77ffffff,true);handle.setGravity(Gravity.CENTER);notice.addView(handle,new LinearLayout.LayoutParams(-1,dp(24)));
        TextView safety=tv("KING Plus Safety • Be respectful. Minors are not allowed in livestream rooms. Child endangerment, harassment and prohibited content can lead to removal or bans.",12,0xffffe892,true);safety.setLineSpacing(dp(2),1f);notice.addView(safety,new LinearLayout.LayoutParams(-1,-2));
        TextView ann=tv("📢  "+announcement,12,Color.WHITE,true);ann.setPadding(dp(4),dp(8),dp(4),dp(4));ann.setOnClickListener(v->{if(isModerator())editAnnouncement();});notice.addView(ann,new LinearLayout.LayoutParams(-1,-2));
        LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,-2);np.setMargins(0,dp(3),0,dp(8));page.addView(notice,np);

        LinearLayout weekly=new LinearLayout(this);weekly.setGravity(Gravity.CENTER_VERTICAL);weekly.setPadding(dp(10),dp(8),dp(8),dp(8));weekly.setBackground(bg(0x5537d8c5,14));
        TextView wi=tv("🎟",26,Color.WHITE,true);wi.setGravity(Gravity.CENTER);weekly.addView(wi,new LinearLayout.LayoutParams(dp(48),dp(56)));
        TextView wt=tv("Congrats, Weekly Gift Cards are available!\nLimited TEST rewards • no billing",12,Color.WHITE,true);weekly.addView(wt,new LinearLayout.LayoutParams(0,dp(56),1));
        TextView view=pill("View",0xffffffff,this::weeklyGiftCardDialog);view.setTextColor(0xffffb900);weekly.addView(view,new LinearLayout.LayoutParams(dp(78),dp(38)));page.addView(weekly,new LinearLayout.LayoutParams(-1,dp(72)));

        musicStatusLabel=tv(currentSong.isEmpty()?"":"🎵 "+currentSong,11,0xffe8dcf6,true);musicStatusLabel.setVisibility(currentSong.isEmpty()?View.GONE:View.VISIBLE);musicStatusLabel.setBackground(bg(0x442c1c43,10));musicStatusLabel.setPadding(dp(10),dp(5),dp(10),dp(5));musicStatusLabel.setOnClickListener(v->musicPanel());page.addView(musicStatusLabel,new LinearLayout.LayoutParams(-1,-2));

        feedBox=new LinearLayout(this);feedBox.setOrientation(LinearLayout.VERTICAL);page.addView(feedBox);seedLocalFeed();
        chatBox=new LinearLayout(this);chatBox.setOrientation(LinearLayout.VERTICAL);page.addView(chatBox);seedLocalChat();

        LinearLayout actions=new LinearLayout(this);actions.setGravity(Gravity.CENTER_VERTICAL);actions.setPadding(0,dp(8),0,dp(6));
        TextView events=pill("🎯 Events",0x66342f47,this::eventsPanel);actions.addView(events,new LinearLayout.LayoutParams(0,dp(42),1));
        TextView ranking=pill("🏆 Ranking",0x66342f47,this::roomRankingDialog);LinearLayout.LayoutParams rkp=new LinearLayout.LayoutParams(0,dp(42),1);rkp.setMargins(dp(6),0,0,0);actions.addView(ranking,rkp);
        TextView tools=pill("▦ Tools",0x66342f47,this::toolsPanel);LinearLayout.LayoutParams tlp=new LinearLayout.LayoutParams(0,dp(42),1);tlp.setMargins(dp(6),0,0,0);actions.addView(tools,tlp);page.addView(actions,new LinearLayout.LayoutParams(-1,dp(56)));

        LinearLayout composer=new LinearLayout(this);composer.setGravity(Gravity.CENTER_VERTICAL);composer.setPadding(dp(8),dp(7),dp(8),dp(7));composer.setBackgroundColor(0xdd201a2d);
        TextView plus=pill("＋",0x0030283f,this::toolsPanel);plus.setTextSize(28);composer.addView(plus,new LinearLayout.LayoutParams(dp(48),dp(50)));
        composerBox=new EditText(this);composerBox.setHint("Send a message...");composerBox.setTextColor(Color.WHITE);composerBox.setHintTextColor(0xffd1c7d6);composerBox.setSingleLine(true);composerBox.setBackground(bg(0x663d334a,24));composerBox.setPadding(dp(14),0,dp(10),0);LinearLayout.LayoutParams cbp=new LinearLayout.LayoutParams(0,dp(48),1);cbp.setMargins(dp(3),0,dp(4),0);composer.addView(composerBox,cbp);
        TextView emoji=pill("😊",0x0030283f,this::emojiPanel);composer.addView(emoji,new LinearLayout.LayoutParams(dp(42),dp(46)));
        micLabel=pill(micOn?"🎤":"🎙",0x0030283f,this::toggleMic);micLabel.setTextSize(25);composer.addView(micLabel,new LinearLayout.LayoutParams(dp(46),dp(48)));
        TextView grid=pill("▦",0x0030283f,this::toolsPanel);grid.setTextSize(24);composer.addView(grid,new LinearLayout.LayoutParams(dp(46),dp(48)));
        TextView gift=pill("🎁",0xffffb900,this::giftShopPanel);gift.setTextSize(24);LinearLayout.LayoutParams glp=new LinearLayout.LayoutParams(dp(52),dp(52));glp.setMargins(dp(3),0,0,0);composer.addView(gift,glp);
        FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(-1,dp(64));cp.gravity=Gravity.BOTTOM;shell.addView(composer,cp);

        if(cloudRoom)attachCloudRoom();else{viewerLabel.setText("1");fillLocalSeats();if(page!=null)page.postDelayed(()->showEntranceEffect(safeName()),250);}
    }
'''
s=s[:start]+new_render+s[end:]

start=s.index('    private void toolsPanel(){')
end=s.index('\n    private void addToolGrid(', start)
new_tools=r'''    private void toolsPanel(){
        ScrollView sv=new ScrollView(this);sv.setFillViewport(true);sv.setVerticalScrollBarEnabled(false);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(16),dp(14),dp(16),dp(22));root.setBackgroundColor(0xff242333);sv.addView(root);
        TextView playTitle=tv("Play Center",22,Color.WHITE,true);root.addView(playTitle,new LinearLayout.LayoutParams(-1,dp(48)));
        String[] pIcons={"⭐","🌷","🥊","💣","📦","🎡","🎯","⚔️","💌"};
        String[] pLabels={"Counter","Guess It","Truth or Dare","Pass the Bomb","Fan Box","Wheel Challenge","Party Wheel","Room Battle","Vote"};
        addToolGrid(root,pIcons,pLabels,true);
        TextView toolsTitle=tv("Tools",22,Color.WHITE,true);LinearLayout.LayoutParams ttp=new LinearLayout.LayoutParams(-1,dp(52));ttp.topMargin=dp(12);root.addView(toolsTitle,ttp);
        boolean queueOn=cloudRoom?false:prefs.getBoolean("queue_"+roomId,false);
        String[] icons={"🎟","🪑","✋","💬","🎵","🎨","📋","👑","🏰"};
        String[] labels={"Events","Room seat",queueOn?"Queue ON":"Enable queue","Private chat","Music","Atmosphere","Income","Share Family","Theme Room"};
        addToolGrid(root,icons,labels,false);
        AlertDialog dialog=new AlertDialog.Builder(this).setView(sv).create();
        dialog.setOnShowListener(v->{android.view.Window w=dialog.getWindow();if(w!=null){w.setLayout(-1,(int)(getResources().getDisplayMetrics().heightPixels*.72f));w.setGravity(Gravity.BOTTOM);w.setBackgroundDrawable(bg(0xff242333,22));}});
        dialog.show();
    }
'''
s=s[:start]+new_tools+s[end:]
s=s.replace('else if("Enable queue".equals(label))toggleSeatQueue();','else if(label.contains("queue")||label.contains("Queue"))toggleSeatQueue();',1)

start=s.index('    private void runRoomEvent(String name){')
end=s.index('\n    private void weeklyGiftCardDialog()', start)
new_events=r'''    private void runRoomEvent(String name){
        if("PK Battle".equals(name)){pkBattle();return;}
        if("Relationship".equals(name)){relationshipEvent();return;}
        if("Crazy Fruits".equals(name)){fruitEvent();return;}
        if("Chicken Run".equals(name)){chickenRunEvent();return;}
        if("Animal PK".equals(name)){animalPkEvent();return;}
        if("Lucky Slot".equals(name)){luckySlotEvent();return;}
        if("Treasure Hunt".equals(name)){treasureHuntEvent();return;}
        if("Room Quiz".equals(name)){roomQuizEvent();return;}
        toast(name);
    }
    private void relationshipEvent(){
        List<String> labels=new ArrayList<>();List<String> ids=new ArrayList<>();
        for(String n:memberNames){String uid=memberUids.get(n);if(n!=null&&!n.equals(displayName)&&uid!=null&&!uid.isEmpty()){labels.add(n);ids.add(uid);}}
        for(int i=1;i<=maxSeats;i++){String n=seatNames.get(i),uid=seatUids.get(i);if(n!=null&&!n.equals(displayName)&&uid!=null&&!uid.isEmpty()&&!labels.contains(n)){labels.add(n);ids.add(uid);}}
        if(labels.isEmpty()){toast("Another signed-in room member is required");return;}
        new AlertDialog.Builder(this).setTitle("💞 Relationship").setItems(labels.toArray(new String[0]),(d,w)->{String n=labels.get(w),uid=ids.get(w);addEvent("relationship",safeName()+" sent a relationship invite to "+n+" 💞");new AlertDialog.Builder(this).setTitle("Invite sent").setMessage("A room event was posted for "+n+". You can continue in private chat or send a gift.").setPositiveButton("Private chat",(x,y)->openPrivateChat(uid,n)).setNeutralButton("Send Gift",(x,y)->giftDialogFor(uid,n)).setNegativeButton("Close",null).show();}).setNegativeButton("Close",null).show();
    }
    private void fruitEvent(){String[] f={"🍒","🍋","🍇","🍉","🍓"};java.util.Random r=new java.util.Random();String a=f[r.nextInt(f.length)],b=f[r.nextInt(f.length)],c=f[r.nextInt(f.length)];boolean win=a.equals(b)&&b.equals(c);String result=a+"   "+b+"   "+c+"\n\n"+(win?"🎉 JACKPOT":"Try again");addEvent("play",safeName()+" played Crazy Fruits • "+a+b+c);new AlertDialog.Builder(this).setTitle("🍒 Crazy Fruits").setMessage(result).setPositiveButton("Spin again",(d,w)->fruitEvent()).setNegativeButton("Close",null).show();}
    private void chickenRunEvent(){String[] lanes={"Lane 1","Lane 2","Lane 3"};new AlertDialog.Builder(this).setTitle("🐔 Chicken Run").setItems(lanes,(d,w)->{int winner=new java.util.Random().nextInt(3);boolean ok=w==winner;String result=(ok?"🏆 Winner! ":"🐔 Winner was ")+lanes[winner];addEvent("play",safeName()+" played Chicken Run • "+result);new AlertDialog.Builder(this).setTitle("Chicken Run").setMessage(result).setPositiveButton("Again",(x,y)->chickenRunEvent()).setNegativeButton("Close",null).show();}).setNegativeButton("Close",null).show();}
    private void animalPkEvent(){String[] animals={"🦁 Lion","🐯 Tiger","🐼 Panda","🦊 Fox"};new AlertDialog.Builder(this).setTitle("🐯 Animal PK").setItems(animals,(d,w)->{int me=30+new java.util.Random().nextInt(71),other=30+new java.util.Random().nextInt(71);String res=animals[w]+" "+me+"  vs  KING Bot "+other+"\n\n"+(me>=other?"🏆 You win":"Try again");addEvent("play",safeName()+" played Animal PK • "+me+":"+other);new AlertDialog.Builder(this).setTitle("Animal PK").setMessage(res).setPositiveButton("Again",(x,y)->animalPkEvent()).setNegativeButton("Close",null).show();}).setNegativeButton("Close",null).show();}
    private void luckySlotEvent(){String[] x={"7️⃣","⭐","💎","👑","🍒"};java.util.Random r=new java.util.Random();String a=x[r.nextInt(x.length)],b=x[r.nextInt(x.length)],c=x[r.nextInt(x.length)];int score=(a.equals(b)&&b.equals(c))?100:(a.equals(b)||b.equals(c)||a.equals(c)?30:5);addEvent("play",safeName()+" spun Lucky Slot • score "+score);new AlertDialog.Builder(this).setTitle("🎰 Lucky Slot").setMessage(a+"   "+b+"   "+c+"\n\nScore: "+score).setPositiveButton("Spin again",(d,w)->luckySlotEvent()).setNegativeButton("Close",null).show();}
    private void treasureHuntEvent(){String[] ch={"Chest 1","Chest 2","Chest 3"};new AlertDialog.Builder(this).setTitle("💎 Treasure Hunt").setItems(ch,(d,w)->{int treasure=new java.util.Random().nextInt(3);String res=w==treasure?"🎁 Treasure found!":"Empty chest • treasure was in "+ch[treasure];addEvent("play",safeName()+" played Treasure Hunt • "+res);new AlertDialog.Builder(this).setTitle("Treasure Hunt").setMessage(res).setPositiveButton("Again",(x,y)->treasureHuntEvent()).setNegativeButton("Close",null).show();}).setNegativeButton("Close",null).show();}
    private void roomQuizEvent(){String q="Which feature opens the room tool sheet?";String[] a={"＋ button","Back button","Room name","Heart badge"};new AlertDialog.Builder(this).setTitle("❓ Room Quiz").setMessage(q).setItems(a,(d,w)->{boolean ok=w==0;String res=ok?"✅ Correct":"❌ Correct answer: ＋ button";addEvent("play",safeName()+" answered Room Quiz • "+(ok?"correct":"wrong"));new AlertDialog.Builder(this).setTitle("Room Quiz").setMessage(res).setPositiveButton("OK",null).show();}).setNegativeButton("Close",null).show();}
'''
s=s[:start]+new_events+s[end:]

old='private void pkBattle(){String msg="⚔ PK Battle started: "+ownerName+" vs Guest Team\\n\\nGifts and room activity decide the winner in this test preview.";new AlertDialog.Builder(this).setTitle("PK Battle").setMessage(msg).setPositiveButton("Start",(d,w)->addEvent("pk",displayName+" started a PK battle ⚔")).setNegativeButton("Close",null).show();}'
new='''private void pkBattle(){
        if(db==null||!cloudRoom){int a=10+new java.util.Random().nextInt(91),b=10+new java.util.Random().nextInt(91);new AlertDialog.Builder(this).setTitle("⚔ Room Battle").setMessage("Team KING  "+a+"  vs  Guest  "+b+"\n\n"+(a>=b?"🏆 Team KING wins":"🏆 Guest wins")).setPositiveButton("Again",(d,w)->pkBattle()).setNegativeButton("Close",null).show();return;}
        db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(100).get().addOnSuccessListener(q->{long hostScore=0,guestScore=0;for(DocumentSnapshot e:q.getDocuments()){Long v=e.getLong("giftValue");long score=v==null?1:Math.max(1,v);String actor=e.getString("actorUid");if(ownerUid!=null&&ownerUid.equals(actor))hostScore+=score;else guestScore+=score;}String result="Team Host  "+compactNumber(hostScore)+"  vs  Room  "+compactNumber(guestScore)+"\n\n"+(hostScore>=guestScore?"🏆 Host Team leads":"🏆 Room Team leads");addEvent("pk",safeName()+" opened Room Battle ⚔");new AlertDialog.Builder(this).setTitle("⚔ Room Battle").setMessage(result).setPositiveButton("Refresh",(d,w)->pkBattle()).setNegativeButton("Close",null).show();}).addOnFailureListener(e->toast("Battle score unavailable: "+msg(e)));
    }'''
if old in s:s=s.replace(old,new,1)
else:print('warning pk template changed')

P.write_text(s,encoding='utf-8')
b=B.read_text(encoding='utf-8');b=re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'","versionCode 68; versionName '4.5.0'",b);B.write_text(b,encoding='utf-8')
Path('CHANGELOG-v4.5.0.txt').write_text('KING Plus v4.5.0 — Party Room Reference Layout + Working Plus Panel\n\nParty rooms now open in the supplied reference structure with an 8-seat default, compact header/badges, seats first, safety/announcement cards, chat feed and a fixed bottom bar. The ＋ button opens the full Play Center + Tools sheet. Counter, Guess It, Truth or Dare, Pass the Bomb, Fan Box, Wheel Challenge, Party Wheel, Room Battle, Vote, Events, Room seat, Queue, Private chat, Music, Atmosphere, Income, Share Family and Theme Room all route to working actions. Event tiles now run interactive mini-flows instead of preview-only messages. Existing real Firebase seats, member profiles/KING ID, gifts, chat, host/co-host, moderation and private/password room security stay intact. No billing; TEST OTP 123456 unchanged.\n',encoding='utf-8')
print('Prepared v4.5.0')
