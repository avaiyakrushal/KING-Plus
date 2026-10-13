#!/usr/bin/env python3
"""KING v9.9.64: prevent fake one-phone random Quick Match outcomes.
Real multiplayer choices go into the authenticated Party/Game flow.
Unimplemented mini-games explicitly display Not Ready instead of fabricated
wins/points. No changes to working Party games, gifting, mic/privacy or billing.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1]);src=root/'app/src/main/java/com/kingplus/social'
p=src/'MainActivity.java'
s=p.read_text()
if '    private void kingStartGame(String game,String mode){' not in s:
    print('SOURCE_GAME_ACTUAL_V9963_BEGIN',flush=True)
    for file_name,start_line,end_line in [
        ('MainActivity.java',975,1040),
        ('MainActivity.java',1145,1265),
        ('PartyActivity.java',1770,1800),
        ('RoomGameActivity.java',685,738)]:
        lines=(src/file_name).read_text().splitlines()
        print('FILE',file_name,'LINES',start_line,'THROUGH',end_line,flush=True)
        for index in range(start_line-1,min(end_line,len(lines))):
            print(str(index+1)+':'+lines[index][:260],flush=True)
    print('SOURCE_GAME_ACTUAL_V9963_END',flush=True)
    raise AssertionError('Legacy fake quick game method is absent: patch current routing from evidence')
begin=s.index('    private void kingStartGame(String game,String mode){')
end=s.index('    private void kingRecordGame(',begin)
body=s[begin:end]
assert 'kingRecordGame(' in body or 'Math.random()' in body, 'v9.9.63 no longer has fake quick match path: inspect instead'
assert 'kingOpenGameRoom(' in s, 'Must preserve existing Party launcher'
assert 'new Intent(this,PartyActivity.class)' in s,'MainActivity Party intent required'
new='''    /** Real match entry: do not manufacture a rival, score or fake win locally.
     * A valid synchronized game requires actual verified Party members.
     */
    private void kingStartGame(String game,String mode){
        if(game==null||game.trim().isEmpty()){Toast.makeText(this,
            "Choose a real Party game",Toast.LENGTH_SHORT).show();return;}
        final String game9964=game,mode9964=mode==null?"Party":mode;
        int route9964=KingRealGameRoute9964.route(game9964);
        if(route9964==KingRealGameRoute9964.PARTY_GAME){
            new AlertDialog.Builder(this)
                .setTitle(game9964+" • "+mode9964)
                .setMessage("This game needs real online players in a KING Party Room. "
                    +"Join the room, ask players to tap Ready and start it from Party Games. "
                    +"The result is synchronized with Firebase; no fake local rival, "
                    +"win or Game Points will be awarded.")
                .setPositiveButton("Open real Party", (d,w)->kingOpenGameRoom(game9964))
                .setNegativeButton("Cancel",null).show();
            return;
        }
        if(route9964==KingRealGameRoute9964.VOICE_ONLY){
            new AlertDialog.Builder(this).setTitle(game9964+" • Voice Room")
                .setMessage("KING Plus can open a voice Party for this game. "
                    +"It does not embed or control the separate third-party game.")
                .setPositiveButton("Open voice Party",(d,w)->kingOpenGameRoom(game9964))
                .setNegativeButton("Cancel",null).show();
            return;
        }
        // Bingo, Crazy Zoo and unverified games cannot pretend to be online
        // by drawing Math.random wins or adding invented persistent GP.
        new AlertDialog.Builder(this).setTitle(game9964+" • Not live yet")
            .setMessage("Real multiplayer "+game9964+
                " is not yet implemented/verified in KING Plus. "
                +"No match was played and no Game Points were credited.")
            .setPositiveButton("OK",null).show();
    }

'''
s=s[:begin]+new+s[end:]
# Safeguard actual Party launch path, never link to fake statistics.
assert 'kingOpenGameRoom(game9964)' in s
assert 'kingRecordGame(' not in s[s.index('    private void kingStartGame('):s.index('    private void kingRecordGame(')]
p.write_text(s)

(src/'KingRealGameRoute9964.java').write_text('''package com.kingplus.social;
/** Real matchmaking entry policy. This is NOT a certification that a
 * specific Party game's full rules have passed two-physical-phone testing.
 */
public final class KingRealGameRoute9964 {
 private KingRealGameRoute9964(){}
 public static final int NOT_READY=0,PARTY_GAME=1,VOICE_ONLY=2;
 public static int route(String title){
   if(title==null)return NOT_READY;
   String game=title.trim().toLowerCase(java.util.Locale.ROOT);
   if(game.startsWith("ludo")||game.startsWith("sheep")
       ||game.startsWith("werewolf")||game.startsWith("spy")
       ||game.startsWith("draw & guess"))return PARTY_GAME;
   if(game.startsWith("free fire"))return VOICE_ONLY;
   // Do not fabricate a local Bingo/Crazy Zoo/Chess outcome.
   return NOT_READY;
 }
}
''')
g=root/'app/build.gradle';v=g.read_text()
old="versionCode 242; versionName '9.9.63-live-ready-game-gift-overflow-test'"
assert v.count(old)==1,'Must start at genuine complete v9.9.63 source'
g.write_text(v.replace(old,"versionCode 243; versionName '9.9.64-real-party-game-launch-no-fake-wins-test'"))
assert 'KingPartyMicConsent9956' in (src/'PartyActivity.java').read_text()
assert 'KingGiftReceiptDurable9962' in (src/'PartyActivity.java').read_text()
assert 'KingTttFairRoster9960.pickPair' in (src/'RoomGameActivity.java').read_text()
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print('PASS main Game Quick Match no fake wins or random scores; actual Party entry for game engine')
