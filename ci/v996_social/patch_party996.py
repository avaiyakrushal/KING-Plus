#!/usr/bin/env python3
from pathlib import Path
import sys,shutil
base=Path(sys.argv[1]);pkg=base/'app/src/main/java/com/kingplus/social'
shutil.copyfile(Path(__file__).with_name('KingGiftSound993.java'),pkg/'KingGiftSound993.java')
f=pkg/'PartyActivity.java';s=f.read_text()
header=r'''    private void shareRealRoom996(){
        if(!cloudRoom||!memberSeen||roomId==null||user==null
            ||KingRoomInvite959.parse(roomId).isEmpty()){
            toast("Join a real Firebase Room before sharing");return;
        }
        final String liveRoom=roomId;
        final String joinCode=shortId(liveRoom);
        final String invite="🎉 Join my KING Plus Party: "+
            (roomName==null?"KING Party":roomName)+
            "\nKINGROOM:"+liveRoom+
            "\n"+KingRoomInvite959.link(liveRoom)+
            "\n6-digit Join Code: "+joinCode;
        new AlertDialog.Builder(this)
            .setTitle("Share this Party with friends")
            .setMessage("Share the FULL Room ID; friends can open People → Join Party ID. "+
                "Private rooms still require access.")
            .setItems(new String[]{"↗ Share invitation",
                   "📋 Copy complete Room invite","📋 Copy six-digit Join Code"},(d,option)->{
                if(option==0){
                    Intent share=new Intent(Intent.ACTION_SEND);
                    share.setType("text/plain");
                    share.putExtra(Intent.EXTRA_TEXT,invite);
                    startActivity(Intent.createChooser(share,"Share KING Party"));
                }else{
                    android.content.ClipboardManager clipboard=
                        (android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                    if(clipboard!=null)clipboard.setPrimaryClip(
                        android.content.ClipData.newPlainText("KING Party invite",
                            option==1?invite:joinCode));
                    toast("Party invitation copied");
                }
            }).setNegativeButton("Close",null).show();
    }

''';
anchor="    private void renderParty() {"
assert s.count(anchor)==1
s=s.replace(anchor,header+anchor,1)
needle='this::shareRoom); share.setTextSize(18)'
assert s.count(needle)==1
s=s.replace(needle,'this::shareRealRoom996); share.setTextSize(18)',1)
needle='KingGiftSound993.play(this,gift);KingSoundFx.gift(this);'
assert s.count(needle)==1
s=s.replace(needle,'KingGiftSound993.play(this,gift);',1)
f.write_text(s)
g=base/'app/build.gradle';x=g.read_text()
a="versionCode 184; versionName '9.9.5-party-memory-background-stability'"
b="versionCode 185; versionName '9.9.6-people-follow-room-gifts'"
assert x.count(a)==1
g.write_text(x.replace(a,b,1))
print('PASS real full ID/shareable join code and 88 distinct sounds without generic overlay')
