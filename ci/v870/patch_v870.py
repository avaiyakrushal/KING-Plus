from pathlib import Path
import sys
root=Path(sys.argv[1])
party=root/'app/src/main/java/com/kingplus/social/PartyActivity.java'
manifest=root/'app/src/main/AndroidManifest.xml'
rules=root/'firestore.rules'
newact=root/'app/src/main/java/com/kingplus/social/KingParityHubActivity.java'
newact.write_text(Path(__file__).with_name('KingParityHubActivity.java').read_text())
ps=party.read_text()

old='''        FrameLayout shell = new FrameLayout(this);
        shell.setBackgroundResource(R.drawable.ref_voice_room_bg);
'''
new='''        FrameLayout shell = new FrameLayout(this);
        shell.setBackground(premiumRoomBackground());
        addRoomBackgroundGlow(shell);
'''
if old not in ps: raise SystemExit('render background marker missing')
ps=ps.replace(old,new,1)

old='''        roomLevelLabel=pill("💎 No."+compact,0xff8e24aa,null); roomLevelLabel.setTextSize(10); roomLevelLabel.setPadding(dp(8),0,dp(8),0);
'''
new='''        roomLevelLabel=pill("💎 No."+compact,0xff8e24aa,()->openParity870("vip")); roomLevelLabel.setTextSize(10); roomLevelLabel.setPadding(dp(8),0,dp(8),0);
'''
if old not in ps: raise SystemExit('level badge marker missing')
ps=ps.replace(old,new,1)
old='''        heartLevelLabel=pill("💖 Lv. 1 Heart",0x26000000,null); heartLevelLabel.setTextSize(10); heartLevelLabel.setPadding(dp(8),0,dp(8),0);
'''
new='''        heartLevelLabel=pill("💖 Lv. 1 Heart",0x26000000,()->openParity870("match")); heartLevelLabel.setTextSize(10); heartLevelLabel.setPadding(dp(8),0,dp(8),0);
'''
if old not in ps: raise SystemExit('heart marker missing')
ps=ps.replace(old,new,1)

old='''        String[] icons={"🎟","🪑","✋","💬","🎵","🎨","📋","👑","🏰","🛡","📊","🎤","📻","🎁","💫","🔎","📢","🎙","📹","🎁","🏆","🗨","🎒","🌐","💞","⚔️","🔁"};
        String[] labels={"Events","Room seat",queueOn?"Queue ON":"Enable queue","Private chat","Music","Atmosphere","Income","Share Family","Theme Room","Party Master","Party Data","KTV Queue","Radio Mic","Lucky Gift","Gift Wish","Find User","Notice","Voice Room","Multi Video","Gift Wall","Gift Rank","Chat History","Backpack","Community","Relationship","Audio PK","Loop Mic","Room Games","Asset Pack"};
'''
new='''        String[] icons={"🎟","🪑","✋","💬","🎵","🎨","📋","👑","🏰","🛡","📊","🎤","📻","🎁","💫","🔎","📢","🎙","📹","🎁","🏆","🗨","🎒","🌐","💞","⚔️","🔁","🎮","📦","✨","🎤","⚔️","🎲","🏰","💎"};
        String[] labels={"Events","Room seat",queueOn?"Queue ON":"Enable queue","Private chat","Music","Atmosphere","Income","Share Family","Theme Room","Party Master","Party Data","KTV Queue","Radio Mic","Lucky Gift","Gift Wish","Find User","Notice","Voice Room","Multi Video","Gift Wall","Gift Rank","Chat History","Backpack","Community","Relationship","Audio PK","Loop Mic","Room Games","Asset Pack","Parity Center","KTV Stage","PK Arena","Match","Party Stage","VIP Rank"};
'''
if old not in ps: raise SystemExit('tools arrays marker missing')
ps=ps.replace(old,new,1)

old='''        else if("Room Games".equals(label))openRoomGames740();
        else if("Asset Pack".equals(label))startActivity(new Intent(this,AssetPackActivity.class));
'''
new='''        else if("Room Games".equals(label))openRoomGames740();
        else if("Asset Pack".equals(label))startActivity(new Intent(this,AssetPackActivity.class));
        else if("Parity Center".equals(label))openParity870("home");
        else if("KTV Stage".equals(label))openParity870("ktv");
        else if("PK Arena".equals(label))openParity870("pk");
        else if("Match".equals(label))openParity870("match");
        else if("Party Stage".equals(label))openParity870("stage");
        else if("VIP Rank".equals(label))openParity870("vip");
'''
if old not in ps: raise SystemExit('runRoomTool marker missing')
ps=ps.replace(old,new,1)

old='''        items.add("💞 Family Party"); items.add("📣 Friend Broadcast"); items.add("📚 Party Master Help");
'''
new='''        items.add("💞 Family Party"); items.add("📣 Friend Broadcast"); items.add("📚 Party Master Help");
        items.add("✨ KING Parity Center"); items.add("🎤 KTV Stage"); items.add("⚔️ PK Arena"); items.add("🎲 Match Center"); items.add("🏰 Party Stage"); items.add("💎 VIP & Rank");
'''
if old not in ps: raise SystemExit('room menu insertion marker missing')
ps=ps.replace(old,new,1)

old='''        else if(x.contains("Party Master Help"))partyMasterHelp610();
        else if(x.contains("Share"))shareRoom();
'''
new='''        else if(x.contains("Party Master Help"))partyMasterHelp610();
        else if(x.contains("Parity Center"))openParity870("home");
        else if(x.contains("KTV Stage"))openParity870("ktv");
        else if(x.contains("PK Arena"))openParity870("pk");
        else if(x.contains("Match Center"))openParity870("match");
        else if(x.contains("Party Stage"))openParity870("stage");
        else if(x.contains("VIP & Rank"))openParity870("vip");
        else if(x.contains("Share"))shareRoom();
'''
if old not in ps: raise SystemExit('menu handler marker missing')
ps=ps.replace(old,new,1)

marker='''    private void openDeepFlow810(String route){Intent i=new Intent(this,KingDeepFlowActivity.class);i.putExtra("route",route);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);startActivity(i);}
'''
add='''    private void openDeepFlow810(String route){Intent i=new Intent(this,KingDeepFlowActivity.class);i.putExtra("route",route);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);startActivity(i);}
    private void openParity870(String route){Intent i=new Intent(this,KingParityHubActivity.class);i.putExtra("route",route);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);startActivity(i);}
'''
if marker not in ps: raise SystemExit('openDeepFlow marker missing')
ps=ps.replace(marker,add,1)

old='''        if("KTV".equals(roomTheme)){a=0xff15245e;b=0xff1b5c88;c=0xff07182f;}
        else if("Love".equals(roomTheme)){a=0xff6b1739;b=0xffa3315c;c=0xff2b1021;}
'''
new='''        if("KTV".equals(roomTheme)){a=0xff15245e;b=0xff1b5c88;c=0xff07182f;}
        else if("PK".equals(roomTheme)){a=0xff7a193a;b=0xff392a75;c=0xff153c82;}
        else if("Love".equals(roomTheme)){a=0xff6b1739;b=0xffa3315c;c=0xff2b1021;}
'''
if old not in ps: raise SystemExit('premium theme marker missing')
ps=ps.replace(old,new,1)

old='''        if("KTV".equals(roomTheme))return 0xff101d46;
        if("Love".equals(roomTheme))return 0xff3d1830;
'''
new='''        if("KTV".equals(roomTheme))return 0xff101d46;
        if("PK".equals(roomTheme))return 0xff321b50;
        if("Love".equals(roomTheme))return 0xff3d1830;
'''
if old not in ps: raise SystemExit('room base marker missing')
ps=ps.replace(old,new,1)

old='''String[] labels={"Classic Emerald","Ocean KTV","Rose Love","Game Emerald","Royal Purple","Neon Night","Galaxy Stage","Festival Glow","Ice Crystal"};String[] themes={"Classic","KTV","Love","Game","Royal","Neon","Galaxy","Festival","Ice"};'''
new='''String[] labels={"Classic Emerald","Ocean KTV","PK Red vs Blue","Rose Love","Game Emerald","Royal Purple","Neon Night","Galaxy Stage","Festival Glow","Ice Crystal"};String[] themes={"Classic","KTV","PK","Love","Game","Royal","Neon","Galaxy","Festival","Ice"};'''
if old not in ps: raise SystemExit('changeTheme arrays missing')
ps=ps.replace(old,new,1)

old='''String[] raw={"Classic","KTV","Love","Game","Royal","Neon","Galaxy","Festival","Ice"};roomTheme=raw[w];'''
new='''String[] raw={"Classic","KTV","PK","Love","Game","Royal","Neon","Galaxy","Festival","Ice"};roomTheme=raw[w];'''
if old in ps:
    ps=ps.replace('''String[] items={"Classic Emerald","Ocean KTV","Rose Love","Game Emerald","Royal Purple","Neon Night","Galaxy Stage","Festival Glow","Ice Crystal"};''','''String[] items={"Classic Emerald","Ocean KTV","PK Red vs Blue","Rose Love","Game Emerald","Royal Purple","Neon Night","Galaxy Stage","Festival Glow","Ice Crystal"};''',1)
    ps=ps.replace(old,new,1)

party.write_text(ps)

ms=manifest.read_text()
old='<activity android:name=".PartyActivity" android:exported="false" />'
new='<activity android:name=".PartyActivity" android:exported="false" />\n        <activity android:name=".KingParityHubActivity" android:exported="false" />'
if old not in ms: raise SystemExit('manifest activity marker missing')
manifest.write_text(ms.replace(old,new,1))

rs=rules.read_text()
old="'level','xp','vipLevel','vipPoints','levelTier','equippedFrame','entranceEffect','updatedAt'"
new="'level','xp','vipLevel','vipPoints','levelTier','equippedFrame','entranceEffect','photoUrl','updatedAt'"
if old not in rs: raise SystemExit('public profile keys marker missing')
rs=rs.replace(old,new,1)
old="""        && (!('entranceEffect' in request.resource.data) || (request.resource.data.entranceEffect is string && request.resource.data.entranceEffect.size() <= 40));"""
new="""        && (!('entranceEffect' in request.resource.data) || (request.resource.data.entranceEffect is string && request.resource.data.entranceEffect.size() <= 40))
        && (!('photoUrl' in request.resource.data) || (request.resource.data.photoUrl is string && request.resource.data.photoUrl.size() <= 2048));"""
if old not in rs: raise SystemExit('photoUrl validation marker missing')
rules.write_text(rs.replace(old,new,1))
print('v8.7.0 patch applied')
