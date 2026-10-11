#!/usr/bin/env python3
"""Apply tested parity/stability pass to the matching v9.9.10 Stage15 source.
Do not copy Bolo Hi proprietary art, code, media or server resources.
"""
from pathlib import Path
import re
import sys

root=Path(sys.argv[1])
java=root/'app/src/main/java/com/kingplus/social'
def one(s,old,new,label):
    n=s.count(old)
    if n!=1:raise RuntimeError(f'{label}: expected one matching anchor, found {n}')
    return s.replace(old,new,1)

m=java/'MainActivity.java'
s=m.read_text(encoding='utf-8')
s=one(s,'else if(displayName.isEmpty())login();',
    'else if(displayName.isEmpty() || firebaseAuth==null || firebaseAuth.getCurrentUser()==null)login();','login')
s=one(s,'    private void games(){\n        incrementMission("mission_game",1);',
    '    private void games(){\n        // Opening a Games tab is not a completed game.','game-points')
start=s.index('    private void createRoom() {')
end=s.index('    private void addChat(',start)
assert '    private void room(String name) {' in s[start:end]
s=s[:start]+'''    // All real rooms must be created and controlled by Firebase PartyActivity.
    private void createRoom(){ openPartyActivity(); }
    private void room(String name){ openPartyActivity(); }

'''+s[end:]
s=one(s,'int members=28+Math.abs((currentRoom==null?0:currentRoom.hashCode())%220);',
      'String members="open live Party to view";','fake-member-count')
s=one(s,'"Members • "+(28+Math.abs(currentRoom.hashCode()%220))',
      '"Members • local preview unavailable"','fake-member-list')
s=one(s,'smallBadge("💎 "+Math.max(0,coinBalance),','smallBadge("💎 —",','profile-first-wallet')
s=one(s,'smallBadge("◈ "+Math.max(0,coinBalance),','smallBadge("◈ —",','profile-first-coins')
s=one(s,'''        if(profileTopBalance!=null)profileTopBalance.setText("💎 "+Math.max(0,coinBalance));
        if(profileWalletCoins!=null)profileWalletCoins.setText("◈ "+Math.max(0,coinBalance));''',
'''        // Local test credits must never appear as spendable server Diamonds.
        if(profileTopBalance!=null)profileTopBalance.setText("💎 —");
        if(profileWalletCoins!=null)profileWalletCoins.setText("◈ —");''','wallet-initial')
s=one(s,'''            Object raw=doc.get("coins"); long coins=raw instanceof Number?Math.max(0,Math.round(((Number)raw).doubleValue())):Math.max(0,coinBalance);
            coinBalance=(int)Math.min(Integer.MAX_VALUE,coins);
            if(profileTopBalance!=null)profileTopBalance.setText("💎 "+coins);
            if(profileWalletCoins!=null)profileWalletCoins.setText("◈ "+coins);''',
'''            Object raw=doc.get("coins");
            if(!(raw instanceof Number))return;
            long coins=Math.max(0,Math.round(((Number)raw).doubleValue()));
            coinBalance=(int)Math.min(Integer.MAX_VALUE,coins);
            if(profileTopBalance!=null)profileTopBalance.setText("💎 "+coins);
            if(profileWalletCoins!=null)profileWalletCoins.setText("◈ "+coins);''','server-only')
s=one(s,'''        }).addOnFailureListener(e->{if(profileTopBalance!=null)profileTopBalance.setText("💎 "+Math.max(0,coinBalance));if(profileWalletCoins!=null)profileWalletCoins.setText("◈ "+Math.max(0,coinBalance));});''',
'''        }).addOnFailureListener(e->{
            if(profileTopBalance!=null)profileTopBalance.setText("💎 —");
            if(profileWalletCoins!=null)profileWalletCoins.setText("◈ —");
            KingStability.nonFatal(this,"wallet-read",e);
        });''','wallet-failure')
s=one(s,'text("Gift Activity",18,Color.WHITE,true);text("🎁 Sent gifts   ×"+giftCount',
      'text("On-device gift activity (not paid Diamonds)",18,Color.WHITE,true);text("🎁 Sent gifts   ×"+giftCount','gift-label')
m.write_text(s,encoding='utf-8')

p=java/'PartyActivity.java'
s=p.read_text(encoding='utf-8')
s=one(s,'    private boolean partyVisualForeground995=true;',
'''    private boolean partyVisualForeground995=true;
    // Ignore stale Firebase lobby results after page change or navigation.
    private int lobbyRenderGeneration9911;''','generation-field')
s=one(s,'''    private void renderLobby(String selected) {
        stopPartyJoinTimeout977();''',
'''    private void renderLobby(String selected) {
        final int generation9911=++lobbyRenderGeneration9911;
        stopPartyJoinTimeout977();''','generation-open')
s=one(s,'''                .addSnapshotListener((snap,error) -> {
                    if (error != null) { showLobbyError(cloudList,''',
'''                .addSnapshotListener((snap,error) -> {
                    if(partyUiDead() || generation9911!=lobbyRenderGeneration9911 || roomId!=null)return;
                    if (error != null) { showLobbyError(cloudList,''','generation-callback')
s=one(s,'''        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(12),dp(4),dp(8),0);
        TextView brand=tv("KING Plus",25,0xff171717,true);head.addView(brand,new LinearLayout.LayoutParams(0,dp(54),1));
        TextView searchIcon=tv("⌕",28,0xff222222,false);searchIcon.setGravity(Gravity.CENTER);searchIcon.setOnClickListener(v->showLobbySearch(selected));head.addView(searchIcon,new LinearLayout.LayoutParams(dp(42),dp(50)));
        TextView history940=tv("🕘",18,0xff222222,false);history940.setGravity(Gravity.CENTER);history940.setContentDescription("Recent and favorite rooms");history940.setOnClickListener(v->roomHistoryPanel940());head.addView(history940,new LinearLayout.LayoutParams(dp(42),dp(50)));
        TextView hot=tv("🔥",23,0xff222222,false);hot.setGravity(Gravity.CENTER);hot.setContentDescription("Create Party");hot.setOnClickListener(v->{if(user!=null&&db!=null){pendingCreateCategory="Video".equalsIgnoreCase(selected)?"Multi Video":selected;createRoomDialog();}else requireSignInForCreate();});head.addView(hot,new LinearLayout.LayoutParams(dp(46),dp(50)));root.addView(head);''',
'''        // Consistent KING Plus identity, explicit live status and familiar room-create icon.
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(14),dp(12),dp(10),dp(9));
        head.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            new int[]{0xff25113e,0xff532f9b,0xff281347}));
        LinearLayout titleStack9911=new LinearLayout(this);titleStack9911.setOrientation(LinearLayout.VERTICAL);
        TextView brand=tv("KING Plus",25,Color.WHITE,true);titleStack9911.addView(brand,new LinearLayout.LayoutParams(-1,dp(39)));
        String connection9911=user==null?"SIGN IN FOR LIVE ROOMS":(KingNetwork.online(this)?"● LIVE PARTY ROOMS":"● OFFLINE • RETRY WHEN CONNECTED");
        TextView status9911=tv(connection9911,10,0xffffdd8b,true);titleStack9911.addView(status9911,new LinearLayout.LayoutParams(-1,dp(24)));
        head.addView(titleStack9911,new LinearLayout.LayoutParams(0,dp(64),1));
        TextView searchIcon=tv("⌕",28,Color.WHITE,false);searchIcon.setGravity(Gravity.CENTER);searchIcon.setContentDescription("Search Party rooms");searchIcon.setOnClickListener(v->showLobbySearch(selected));head.addView(searchIcon,new LinearLayout.LayoutParams(dp(42),dp(50)));
        TextView history940=tv("🕘",18,Color.WHITE,false);history940.setGravity(Gravity.CENTER);history940.setContentDescription("Recent and favorite rooms");history940.setOnClickListener(v->roomHistoryPanel940());head.addView(history940,new LinearLayout.LayoutParams(dp(42),dp(50)));
        TextView hot=tv("🏠",23,Color.WHITE,false);hot.setGravity(Gravity.CENTER);hot.setContentDescription("Create Party");hot.setOnClickListener(v->{if(user!=null&&db!=null){pendingCreateCategory="Video".equalsIgnoreCase(selected)?"Multi Video":selected;createRoomDialog();}else requireSignInForCreate();});head.addView(hot,new LinearLayout.LayoutParams(dp(46),dp(50)));root.addView(head);''','lobby-visual')
s=one(s,'t.setBackground(bg(active?0xffffee00:0xfff5f5f5,8));',
      't.setBackground(bg(active?0xffffd768:0xfff5f5f5,12));','lobby-chips')
p.write_text(s,encoding='utf-8')

g=root/'app/build.gradle'
s=g.read_text(encoding='utf-8')
s,n=re.subn(r"versionCode\s+187;\s*versionName\s+'9\.9\.8-real-friends-server-sync'",
            "versionCode 190; versionName '9.9.11-parity-pass1-test'",s)
assert n==1,'source versionCode unexpectedly changed'
g.write_text(s,encoding='utf-8')
assert '28+Math.abs(name.hashCode()%220)' not in m.read_text(encoding='utf-8')
assert 'generation9911!=lobbyRenderGeneration9911' in p.read_text(encoding='utf-8')
print('PASS parity patches: login/home, party, profile/social, gifting/game fairness, stability')
