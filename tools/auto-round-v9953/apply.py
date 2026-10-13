#!/usr/bin/env python3
"""KING v9.9.53: actually finish fully played RPS/Dice/Coin Party rounds.

The active Host/Co-host already has a SERVER-confirmed move listener. When all
frozen Party participants submit immutable real moves, run the EXISTING
server-refetched finishRound() transaction automatically once for that round.
Do not settle from cache, from partial games or from a spectator. Keep Host
Finish/Cancel buttons available if connectivity or automation fails.
"""
from pathlib import Path
import sys
r=Path(sys.argv[1]); src=r/'app/src/main/java/com/kingplus/social'
p=src/'RoomGameActivity.java'; s=p.read_text()
def rep(old,new):
 global s
 count=s.count(old)
 if count != 1: raise RuntimeError(f'Expected one exact anchor, found {count}: {old[:100]}')
 s=s.replace(old,new,1)
rep(
 '    private final java.util.ArrayList<String> frozenRoster9947=new java.util.ArrayList<>();',
 '    private final java.util.ArrayList<String> frozenRoster9947=new java.util.ArrayList<>();\n'
 '    private long lastAutoSettlementAttempt9953=-1L;')
rep(
 '''                if(moderator&&"ttt".equals(type)&&"active".equals(status))
                    processTttMove864();
''',
 '''                if(moderator&&"ttt".equals(type)&&"active".equals(status))
                    processTttMove864();
                else maybeAutoFinish9953();
''')
rep(
 '''                if(!hasMyMove()){if("rps".equals(type))rpsControls();''',
 '''                if(KingRoomRoster9946.lockedGame(type)
                    &&KingRoomRoster9946.valid(frozenRoster9947)
                    &&(me==null||!frozenRoster9947.contains(me.getUid()))){
                    controls.addView(tv("👀 Spectating this round • tap Ready for the next match",13,MUTED,false));
                }else if(!hasMyMove()){if("rps".equals(type))rpsControls();''')
rep(
 '''    private void finishRound(){
''',
 '''    /** A true SERVER snapshot can complete a played round without another Host tap.
     * Only the Host/Co-host attempts settlement; all actual scoring and status
     * writes still use finishRound's fresh SERVER reads and Firestore transaction.
     */
    private void maybeAutoFinish9953(){
        if(lastAutoSettlementAttempt9953==roundId||me==null)return;
        java.util.ArrayList<String> submitted9953=new java.util.ArrayList<>();
        for(DocumentSnapshot move9953:currentMoves)
            if(type.equals(s(move9953.getString("type")))
               &&move9953.getId().equals(s(move9953.getString("uid"))))
                submitted9953.add(move9953.getId());
        if(!KingRoomAutoFinish9953.shouldFinish(type,status,moderator,
                gameMemberVerified992,gameStateVerified9943,roundId,
                frozenRoster9947,submitted9953))return;
        lastAutoSettlementAttempt9953=roundId;
        finishRound();
    }

    private void finishRound(){
''')
# Do not retry the same completion in an uncontrolled listener loop. If Host
# experiences a transient error, the visible manual Finish control remains.
g=r/'app/build.gradle';v=g.read_text()
old="versionCode 231; versionName '9.9.52-live-ready-game-roster-test'"
if v.count(old)!=1:raise RuntimeError('Not verified v9.9.52 full source')
g.write_text(v.replace(old,
 "versionCode 232; versionName '9.9.53-auto-party-round-finish-test'"))
p.write_text(s)
(src/'KingRoomAutoFinish9953.java').write_text("""package com.kingplus.social;
import java.util.List;
/** Use only SERVER-confirmed moves, already filtered by round/game/UID.
 * A Host must finish on the trusted SERVER snapshot, never on local cache.
 */
public final class KingRoomAutoFinish9953 {
 private KingRoomAutoFinish9953(){}
 public static boolean shouldFinish(String type,String status,boolean moderator,
        boolean liveMember,boolean serverVerified,long roundId,
        List<?> participants,List<String> submitted){
   return moderator&&liveMember&&serverVerified&&roundId>0
       &&"active".equals(status)&&KingRoomRoster9946.lockedGame(type)
       &&KingRoomRoster9946.complete(participants,submitted);
 }
}
""")
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print('PASS one-attempt automatic settlement for complete SERVER moves, spectators cannot submit; paid recharge OFF')
