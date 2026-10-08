from pathlib import Path
import sys

root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
here=Path(__file__).parent.parent/'v943'

ambient=here/'KingPartyAmbientView.java'
if not ambient.exists(): raise SystemExit('theme-aware ambient source missing')
(pkg/'KingPartyAmbientView.java').write_text(ambient.read_text())

party=pkg/'PartyActivity.java'
q=party.read_text()

old='''            KingPartyAmbientView ambient943=new KingPartyAmbientView(this);'''
new='''            KingPartyAmbientView ambient943=new KingPartyAmbientView(this,roomTheme);'''
if old not in q: raise SystemExit('Party ambient constructor marker missing')
q=q.replace(old,new,1)

old='''    private void atmospherePanel(){
        String[] items={"✨ Classic","💙 KTV","💗 Love","🎮 Game","👑 Royal"};
        new AlertDialog.Builder(this).setTitle("🎨 Atmosphere • "+roomTheme).setItems(items,(d,w)->{
            if(!isOwner()){toast("Host controls the room atmosphere");return;}
            String[] raw={"Classic","KTV","PK","Love","Game","Royal","Neon","Galaxy","Festival","Ice"};roomTheme=raw[w];if(cloudRoom)setRoomValue("theme",roomTheme);else prefs.edit().putString("theme_"+roomName,roomTheme).apply();renderParty();
        }).setNegativeButton("Close",null).show();
    }'''
new='''    private void atmospherePanel(){
        String[] items={"✨ Classic","🎤 KTV","⚔ PK","💗 Love","🎮 Game","👑 Royal","💡 Neon","🌌 Galaxy","🎉 Festival","❄ Ice"};
        String[] raw={"Classic","KTV","PK","Love","Game","Royal","Neon","Galaxy","Festival","Ice"};
        new AlertDialog.Builder(this).setTitle("🎨 Atmosphere • "+roomTheme).setItems(items,(d,w)->{
            if(!isOwner()){toast("Host controls the room atmosphere");return;}
            if(w<0||w>=raw.length)return;
            roomTheme=raw[w];
            if(cloudRoom){setRoomValue("theme",roomTheme);addEvent("theme",safeName()+" changed atmosphere to "+roomTheme);}
            else prefs.edit().putString("theme_"+roomName,roomTheme).apply();
            toast(roomTheme+" atmosphere applied");
            renderParty();
        }).setNegativeButton("Close",null).show();
    }'''
if old not in q: raise SystemExit('Party atmosphere mapping marker missing')
q=q.replace(old,new,1)

old='''avatarFrame.addView(av,new FrameLayout.LayoutParams(-1,-1));seat.addView(avatarFrame,new LinearLayout.LayoutParams(dp(56),dp(56)));'''
new='''avatarFrame.addView(av,new FrameLayout.LayoutParams(-1,-1));
                if(n!=null){
                    TextView micState944=tv(Boolean.FALSE.equals(seatMics.get(no))?"🔇":"🎙",8,Color.WHITE,true);
                    micState944.setGravity(Gravity.CENTER);micState944.setBackground(bg(Boolean.FALSE.equals(seatMics.get(no))?0xffc9475b:0xff24a87b,12));
                    FrameLayout.LayoutParams micLp944=new FrameLayout.LayoutParams(dp(21),dp(21));micLp944.gravity=Gravity.RIGHT|Gravity.BOTTOM;
                    avatarFrame.addView(micState944,micLp944);
                }
                seat.addView(avatarFrame,new LinearLayout.LayoutParams(dp(56),dp(56)));'''
if old not in q: raise SystemExit('Party seat avatar marker missing')
q=q.replace(old,new,1)

party.write_text(q)

gradle=root/'app/build.gradle'
g=gradle.read_text()
old="versionCode 142; versionName '9.4.3-final-parity'"
if old not in g: raise SystemExit('v9.4.3 version marker missing')
gradle.write_text(g.replace(old,"versionCode 143; versionName '9.4.4-visual-depth'",1))

(root/'V9.4.4-WORKLOG.md').write_text('''# KING Plus v9.4.4 visual-depth pass

- fixed the Party atmosphere menu mapping bug
- exposed all ten KING room themes: Classic, KTV, PK, Love, Game, Royal, Neon, Galaxy, Festival, Ice
- Party ambient animation now changes with the selected room theme
- occupied mic seats now show a visible mic/muted state badge on the avatar frame
- existing entry effects, VIP/profile frames, gift bursts, live emoji overlays, loading/retry states and moderation controls are retained
''')
print('KING Plus v9.4.4 visual-depth patch applied')
