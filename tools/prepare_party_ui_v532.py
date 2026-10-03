from pathlib import Path
import re

party = Path('app/src/main/java/com/kingplus/social/PartyActivity.java')
s = party.read_text(encoding='utf-8')

render = r'''    private void renderParty() {
        clearListeners();

        final int ROOM_GREEN_V532 = 0xff008e6b;
        final int SEAT_GREEN_V532 = 0xff3db896;
        final int SEAT_TEXT_V532 = 0xff85e3cb;

        FrameLayout shell = new FrameLayout(this);
        shell.setBackgroundColor(ROOM_GREEN_V532);

        LinearLayout roomRoot = new LinearLayout(this);
        roomRoot.setOrientation(LinearLayout.VERTICAL);
        roomRoot.setPadding(0,0,0,0);
        FrameLayout.LayoutParams roomRootLp = new FrameLayout.LayoutParams(-1,-1);
        roomRootLp.bottomMargin = dp(64);
        shell.addView(roomRoot, roomRootLp);

        // Top header: back, room title + online count, share, menu.
        LinearLayout head = new LinearLayout(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setPadding(dp(14),0,dp(14),0);
        TextView back = tv("‹",34,Color.WHITE,true); back.setGravity(Gravity.CENTER); back.setOnClickListener(v->leaveRoom());
        head.addView(back,new LinearLayout.LayoutParams(dp(30),dp(56)));

        LinearLayout info = new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL); info.setGravity(Gravity.CENTER_VERTICAL); info.setPadding(dp(10),0,0,0);
        TextView rn = tv(roomName==null?"KING Plus Party":roomName,15,Color.WHITE,true); rn.setSingleLine(true); rn.setPadding(0,0,0,0); rn.setOnClickListener(v->hostProfileDialog());
        info.addView(rn,new LinearLayout.LayoutParams(-1,dp(28)));
        viewerLabel = pill("👤 1",0x33000000,this::membersDialog); viewerLabel.setTextSize(10); viewerLabel.setPadding(dp(6),0,dp(8),0);
        info.addView(viewerLabel,new LinearLayout.LayoutParams(dp(58),dp(22)));
        head.addView(info,new LinearLayout.LayoutParams(0,dp(56),1));

        TextView share = pill("↗",0x2effffff,this::shareRoom); share.setTextSize(18); head.addView(share,new LinearLayout.LayoutParams(dp(32),dp(32)));
        TextView more = pill("≡",0x2effffff,this::roomMenu); more.setTextSize(18); LinearLayout.LayoutParams moreLp=new LinearLayout.LayoutParams(dp(32),dp(32)); moreLp.setMargins(dp(8),0,0,0); head.addView(more,moreLp);
        roomRoot.addView(head,new LinearLayout.LayoutParams(-1,dp(56)));

        // Rank / heart / edit bar.
        LinearLayout badges = new LinearLayout(this); badges.setGravity(Gravity.CENTER_VERTICAL); badges.setPadding(dp(14),dp(2),dp(14),dp(6));
        String compact=shortId(); if(compact.length()>2) compact=compact.substring(compact.length()-2);
        roomLevelLabel=pill("💎 No."+compact,0xff8e24aa,null); roomLevelLabel.setTextSize(10); roomLevelLabel.setPadding(dp(8),0,dp(8),0);
        badges.addView(roomLevelLabel,new LinearLayout.LayoutParams(-2,dp(24)));
        heartLevelLabel=pill("💖 Lv. 1 Heart",0x26000000,null); heartLevelLabel.setTextSize(10); heartLevelLabel.setPadding(dp(8),0,dp(8),0);
        LinearLayout.LayoutParams heartLp=new LinearLayout.LayoutParams(-2,dp(24)); heartLp.setMargins(dp(8),0,0,0); badges.addView(heartLevelLabel,heartLp);
        View badgeSpacer=new View(this); badges.addView(badgeSpacer,new LinearLayout.LayoutParams(0,1,1));
        TextView edit=pill("📑 Edit",0x33000000,isOwner()?this::roomControlCenter:this::roomBillboardDialog); edit.setTextSize(11); edit.setPadding(dp(10),0,dp(10),0);
        badges.addView(edit,new LinearLayout.LayoutParams(-2,dp(26)));
        roomRoot.addView(badges,new LinearLayout.LayoutParams(-1,dp(36)));

        // Hidden state labels retained for existing realtime/controller logic.
        roomLiveBadge=pill("👥 1 • 🎙 0",0x00302747,null); roomLiveBadge.setVisibility(View.GONE); roomRoot.addView(roomLiveBadge,new LinearLayout.LayoutParams(1,1));
        roomStateLabel=tv("",1,Color.TRANSPARENT,false); roomStateLabel.setVisibility(View.GONE); roomRoot.addView(roomStateLabel,new LinearLayout.LayoutParams(1,1)); refreshRoomState();
        supporterLabel=tv("",1,Color.TRANSPARENT,false); supporterLabel.setVisibility(View.GONE); roomRoot.addView(supporterLabel,new LinearLayout.LayoutParams(1,1));
        announcementLabel=tv("",1,Color.TRANSPARENT,false); announcementLabel.setVisibility(View.GONE); roomRoot.addView(announcementLabel,new LinearLayout.LayoutParams(1,1));
        seatRequestLabel=tv("",1,Color.TRANSPARENT,false); seatRequestLabel.setVisibility(View.GONE); roomRoot.addView(seatRequestLabel,new LinearLayout.LayoutParams(1,1));
        reactionBanner=tv("",1,Color.TRANSPARENT,false); reactionBanner.setVisibility(View.GONE); roomRoot.addView(reactionBanner,new LinearLayout.LayoutParams(1,1));

        // 8-seat compact grid.
        seatsBox = new LinearLayout(this); seatsBox.setOrientation(LinearLayout.VERTICAL); seatsBox.setPadding(0,dp(4),0,dp(6));
        roomRoot.addView(seatsBox,new LinearLayout.LayoutParams(-1,dp(170))); rebuildSeats();

        // Chat + safety section takes the remaining height.
        LinearLayout chatSection = new LinearLayout(this); chatSection.setOrientation(LinearLayout.VERTICAL); chatSection.setPadding(dp(12),0,dp(12),0);
        TextView safety = tv("KING Plus Safety • Be respectful. Child endangerment, harassment and prohibited content can lead to removal or bans.",10,0xffffe580,true);
        safety.setBackground(bg(0x2e004d3a,10)); safety.setPadding(dp(8),dp(7),dp(8),dp(7));
        safety.setOnClickListener(v->{ if(isModerator()) editAnnouncement(); });
        chatSection.addView(safety,new LinearLayout.LayoutParams(-1,-2));

        ScrollView chatScroll = new ScrollView(this); chatScroll.setFillViewport(true); chatScroll.setVerticalScrollBarEnabled(true); chatScroll.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout chatContent = new LinearLayout(this); chatContent.setOrientation(LinearLayout.VERTICAL); chatContent.setPadding(0,dp(2),0,dp(2));
        feedBox = new LinearLayout(this); feedBox.setOrientation(LinearLayout.VERTICAL); chatContent.addView(feedBox); seedLocalFeed();
        chatBox = new LinearLayout(this); chatBox.setOrientation(LinearLayout.VERTICAL); chatContent.addView(chatBox); seedLocalChat();
        chatScroll.addView(chatContent,new ScrollView.LayoutParams(-1,-2));
        chatSection.addView(chatScroll,new LinearLayout.LayoutParams(-1,0,1));

        HorizontalScrollView quickScroll = new HorizontalScrollView(this); quickScroll.setHorizontalScrollBarEnabled(false); quickScroll.setFillViewport(false);
        LinearLayout quick = new LinearLayout(this); quick.setOrientation(LinearLayout.HORIZONTAL); quick.setGravity(Gravity.CENTER_VERTICAL);
        addQuickChipV532(quick,"Nice to meet everyone!",composerBox);
        addQuickChipV532(quick,"😂😂😂😂😂",composerBox);
        addQuickChipV532(quick,"Hi",composerBox);
        addQuickChipV532(quick,"Nice",composerBox);
        quickScroll.addView(quick,new HorizontalScrollView.LayoutParams(-2,dp(34)));
        chatSection.addView(quickScroll,new LinearLayout.LayoutParams(-1,dp(38)));
        roomRoot.addView(chatSection,new LinearLayout.LayoutParams(-1,0,1));

        // Bottom input panel, matching the supplied layout while preserving all room functions.
        LinearLayout composer = new LinearLayout(this); composer.setGravity(Gravity.CENTER_VERTICAL); composer.setPadding(dp(8),dp(7),dp(8),dp(7)); composer.setBackgroundColor(Color.WHITE);
        TextView plus=pill("＋",0x00ffffff,this::toolsPanel); plus.setTextColor(0xff333333); plus.setTextSize(24); composer.addView(plus,new LinearLayout.LayoutParams(dp(38),dp(48)));
        composerBox=new EditText(this); composerBox.setHint("Type message..."); composerBox.setTextColor(0xff111111); composerBox.setHintTextColor(0xff777777); composerBox.setSingleLine(true); composerBox.setBackground(null); composerBox.setPadding(dp(8),0,dp(8),0);
        composer.addView(composerBox,new LinearLayout.LayoutParams(0,dp(48),1));
        TextView emoji=pill("😊",0x00ffffff,this::showLiveEmojiPanelV530); emoji.setTextColor(0xff333333); emoji.setTextSize(23); composer.addView(emoji,new LinearLayout.LayoutParams(dp(42),dp(46)));
        micLabel=pill(micOn?"🎤":"🎙",0x00ffffff,this::toggleMic); micLabel.setTextColor(0xff333333); micLabel.setTextSize(23); composer.addView(micLabel,new LinearLayout.LayoutParams(dp(42),dp(46)));
        TextView gift=pill("🎁",0x00ffffff,this::giftShopPanel); gift.setTextColor(0xff333333); gift.setTextSize(22); composer.addView(gift,new LinearLayout.LayoutParams(dp(42),dp(46)));
        TextView send=pill("➤",0x00ffffff,()->sendMessage(composerBox)); send.setTextColor(0xff008e6b); send.setTextSize(23); composer.addView(send,new LinearLayout.LayoutParams(dp(42),dp(46)));
        FrameLayout.LayoutParams composerLp=new FrameLayout.LayoutParams(-1,dp(64)); composerLp.gravity=Gravity.BOTTOM; shell.addView(composer,composerLp);

        setSafeContentView(shell);

        // Full-screen transparent live emoji overlay must remain above the room UI.
        liveEmojiStageV530 = new FrameLayout(this); liveEmojiStageV530.setClipChildren(false); liveEmojiStageV530.setClipToPadding(false); liveEmojiStageV530.setClickable(false); liveEmojiStageV530.setFocusable(false);
        FrameLayout.LayoutParams liveOverlayLpV531 = new FrameLayout.LayoutParams(-1,-1); liveOverlayLpV531.bottomMargin=dp(64); shell.addView(liveEmojiStageV530,liveOverlayLpV531);

        musicStatusLabel=tv(currentSong.isEmpty()?"":"🎵 "+currentSong,11,Color.WHITE,true); musicStatusLabel.setVisibility(View.GONE);

        if(cloudRoom) attachCloudRoom();
        else { viewerLabel.setText("👤 1"); fillLocalSeats(); if(roomRoot!=null) roomRoot.postDelayed(()->showEntranceEffect(safeName()),250); }
    }

    private void addQuickChipV532(LinearLayout row, String text, EditText ignored) {
        TextView chip = tv(text,11,0xff222222,false); chip.setGravity(Gravity.CENTER); chip.setBackground(bg(Color.WHITE,14)); chip.setPadding(dp(12),dp(4),dp(12),dp(4));
        chip.setOnClickListener(v->{ if(composerBox!=null){ composerBox.setText(text); composerBox.setSelection(composerBox.getText().length()); } });
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,dp(30)); lp.setMargins(0,dp(2),dp(6),dp(2)); row.addView(chip,lp);
    }

'''

render_pat = re.compile(r'    private void renderParty\(\) \{.*?\n    private void addMemberStrip\(\) \{', re.S)
if not render_pat.search(s):
    raise SystemExit('v5.3.2: renderParty anchor not found')
s = render_pat.sub(render + '    private void addMemberStrip() {', s, count=1)

rebuild = r'''    private void rebuildSeats() {
        if (seatsBox == null) return;
        seatsBox.removeAllViews();
        final int displaySeatsV532 = 8;
        for (int r=0;r<2;r++) {
            LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER); row.setWeightSum(4f);
            for (int c=0;c<4;c++) {
                int no=r*4+c+1; if(no>displaySeatsV532) break;
                LinearLayout seat=new LinearLayout(this); seat.setOrientation(LinearLayout.VERTICAL); seat.setGravity(Gravity.CENTER); seat.setPadding(dp(2),dp(2),dp(2),0);
                String n=seatNames.get(no); boolean mine=cloudRoom?user!=null&&user.getUid().equals(seatUids.get(no)):no==mySeat; boolean seatLocked=cloudRoom&&lockedSeats.contains(no);
                boolean hostVisualV532 = no==1 && n==null && ownerName!=null && !ownerName.trim().isEmpty();
                String icon = hostVisualV532 ? "👑" : (n==null?(seatLocked?"🔒":"＋"):(mine?"👑":avatarForSeat(no,n)));
                TextView av=tv(icon,hostVisualV532?25:(n==null?22:24),hostVisualV532?Color.WHITE:(n==null?0xff85e3cb:Color.WHITE),true);
                av.setGravity(Gravity.CENTER); av.setBackground(bg(hostVisualV532?0xff52b69a:(n==null?0xff3db896:(mine?0xff2e9d83:seatColor(no))),27));
                seat.addView(av,new LinearLayout.LayoutParams(dp(54),dp(54)));
                String label;
                if(hostVisualV532) label="👑 "+shortSeatName(ownerName);
                else if(n==null) label=seatLocked?"NO."+no+" 🔒":"NO."+no;
                else label=(mine?"👑 ":"")+shortSeatName(n)+(Boolean.FALSE.equals(seatMics.get(no))?" 🔇":"");
                TextView lab=tv(label,10,n==null&&!hostVisualV532?0xffd8ffffff:Color.WHITE,n!=null||hostVisualV532); lab.setGravity(Gravity.CENTER); lab.setPadding(0,dp(2),0,0); seat.addView(lab,new LinearLayout.LayoutParams(-1,dp(22)));
                final int seatNo=no; seat.setOnClickListener(v->seatAction(seatNo)); row.addView(seat,new LinearLayout.LayoutParams(0,dp(80),1));
            }
            LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(80)); if(r==1) rp.setMargins(0,dp(8),0,0); seatsBox.addView(row,rp);
        }
    }
'''
rebuild_pat = re.compile(r'    private void rebuildSeats\(\) \{.*?\n    private int seatColor', re.S)
if not rebuild_pat.search(s):
    raise SystemExit('v5.3.2: rebuildSeats anchor not found')
s = rebuild_pat.sub(rebuild + '    private int seatColor', s, count=1)

required = ['ROOM_GREEN_V532','displaySeatsV532 = 8','addQuickChipV532','this::showLiveEmojiPanelV530','liveOverlayLpV531','liveEmojiListenerV530=','private void sendLiveEmojiV530(']
missing=[x for x in required if x not in s]
if missing:
    raise SystemExit('v5.3.2 UI/live emoji verification failed: '+', '.join(missing))
party.write_text(s, encoding='utf-8')
print('v5.3.2 compact 8-seat Party Room UI applied')
