#!/usr/bin/env python3
"""KING v9.9.54 - bounded reconnect retries for real Party RPS/Dice/Coin.
Keep existing SERVER result reads and immutable Firestore transaction.
Transient offline failures never permanently disable auto settlement.
No trusted-server game claim and no paid billing.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1]);src=root/'app/src/main/java/com/kingplus/social'
p=src/'RoomGameActivity.java';s=p.read_text()
def rep(old,new):
 global s
 n=s.count(old)
 if n!=1:raise RuntimeError(f"Expected exactly one verified source anchor ({n}): {old[:120]!r}")
 s=s.replace(old,new,1)
rep('    private long lastAutoSettlementAttempt9953=-1L;\n',
'''    private long lastAutoSettlementAttempt9953=-1L;
    private long autoRetryRound9954=-1L;
    private int autoRetryFailures9954=0;
    private boolean autoSettlementInFlight9954=false;
    private final android.os.Handler settlementHandler9954=
        new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable settlementRetryTask9954;
''')
rep('''    @Override protected void onDestroy(){
        if(gameMemberVerified992''','''    @Override protected void onDestroy(){
        clearScheduledSettlement9954();
        autoSettlementInFlight9954=false;
        if(gameMemberVerified992''')
rep('''                gameMemberVerified992=false;gameStateVerified9943=false;frozenRoster9947.clear();moderator=false;''',
'''                clearScheduledSettlement9954();
                autoSettlementInFlight9954=false;
                gameMemberVerified992=false;gameStateVerified9943=false;frozenRoster9947.clear();moderator=false;''')
rep('''        if(!d.exists()){roundId=0;type="";prompt="";result="";status="closed";resetTttState864();''',
'''        final long priorRound9954=roundId;
        if(!d.exists()){roundId=0;type="";prompt="";result="";status="closed";resetTttState864();''')
rep('''}else resetSocialState866();}renderState();updatePhase950();listenMoves();renderControls();});}''',
''' }else resetSocialState866();}
        if(priorRound9954!=roundId||!"active".equals(status)){
            clearScheduledSettlement9954();
            autoSettlementInFlight9954=false;
            autoRetryRound9954=roundId;
            autoRetryFailures9954=0;
            lastAutoSettlementAttempt9953=-1L;
        }
        renderState();updatePhase950();listenMoves();renderControls();});}''')
rep('''    private void maybeAutoFinish9953(){
        if(lastAutoSettlementAttempt9953==roundId||me==null)return;''',
'''    private void maybeAutoFinish9953(){
        if(autoSettlementInFlight9954||settlementRetryTask9954!=null||
           lastAutoSettlementAttempt9953==roundId||me==null||isFinishing()||isDestroyed())return;''')
rep('''        lastAutoSettlementAttempt9953=roundId;
        finishRound();
    }

    private void finishRound(){
''',
'''        if(autoRetryRound9954!=roundId){
            autoRetryRound9954=roundId;
            autoRetryFailures9954=0;
        }
        lastAutoSettlementAttempt9953=roundId;
        autoSettlementInFlight9954=true;
        finishRound(true);
    }

    private void clearScheduledSettlement9954(){
        if(settlementRetryTask9954!=null){
            settlementHandler9954.removeCallbacks(settlementRetryTask9954);
            settlementRetryTask9954=null;
        }
    }
    private void settleFailed9954(boolean automatic,long expected,String expectedGame){
        if(!automatic)return;
        autoSettlementInFlight9954=false;
        if(!KingRoomSettleRetry9954.allowRetry(autoRetryFailures9954,
              expected==roundId&&expectedGame.equals(type),moderator,
              gameMemberVerified992,"active".equals(status))
            ||isFinishing()||isDestroyed()){
            if(autoRetryFailures9954>=KingRoomSettleRetry9954.MAX_RETRIES &&
               expected==roundId&&"active".equals(status))
                toast("Automatic result failed • Host may tap Finish or Cancel");
            return;
        }
        autoRetryFailures9954++;
        long delay9954=KingRoomSettleRetry9954.delayMs(autoRetryFailures9954);
        clearScheduledSettlement9954();
        settlementRetryTask9954=()->{
            settlementRetryTask9954=null;
            if(isFinishing()||isDestroyed()||roundId!=expected||
               !expectedGame.equals(type)||!"active".equals(status)||
               !gameMemberVerified992||!moderator||!gameStateVerified9943)return;
            lastAutoSettlementAttempt9953=-1L;
            maybeAutoFinish9953();
        };
        settlementHandler9954.postDelayed(settlementRetryTask9954,delay9954);
        if(connection950!=null)
            connection950.setText("⏳ Game result reconnect • retry "+autoRetryFailures9954+
                 "/"+KingRoomSettleRetry9954.MAX_RETRIES);
    }
    private void settleSucceeded9954(boolean automatic){
        if(!automatic)return;
        clearScheduledSettlement9954();
        autoSettlementInFlight9954=false;
        autoRetryFailures9954=0;
    }

    private void finishRound(){finishRound(false);}
    private void finishRound(boolean automatic9954){
''')
rep('''           me==null||roundId==0||!"active".equals(status)){
            toast("Join a verified active Party round before finishing");return;''',
'''           me==null||roundId==0||!"active".equals(status)){
            autoSettlementInFlight9954=false;
            toast("Join a verified active Party round before finishing");return;''')
a=s.index('    private void finishRound(boolean automatic9954){')
b=s.index('    private String resultFor(List<DocumentSnapshot>a){',a)
part=s[a:b]
def fix(old,new):
 global part
 if part.count(old)!=1:raise RuntimeError("Expected finishRound callback: "+old[:100])
 part=part.replace(old,new,1)
fix('''                if(a.size()<2){toast("At least 2 real players must submit");return;}''',
'''                if(a.size()<2){
                    settleFailed9954(automatic9954,expected,expectedType);
                    if(!automatic9954)toast("At least 2 real players must submit");
                    return;
                }''')
fix('''                        toast("Game round changed, refresh Party");return;''',
'''                        settleSucceeded9954(automatic9954);
                        if(!automatic9954)toast("Game round changed, refresh Party");
                        return;''')
fix('''                        toast("Waiting for all "+frozen.size()+
                             " Ready players • received "+submitted.size()+" moves");
                        return;''',
'''                        settleFailed9954(automatic9954,expected,expectedType);
                        if(!automatic9954)toast("Waiting for all "+frozen.size()+
                             " Ready players • received "+submitted.size()+" moves");
                        return;''')
fix('''                    }).addOnSuccessListener(v->{
                        postGameEvent750(''',
'''                    }).addOnSuccessListener(v->{
                        settleSucceeded9954(automatic9954);
                        postGameEvent750(''')
fix('''                    }).addOnFailureListener(e->toast("Finish failed: "+e.getMessage()));''',
'''                    }).addOnFailureListener(e->{
                        settleFailed9954(automatic9954,expected,expectedType);
                        if(!automatic9954)toast("Finish failed: "+e.getMessage());
                    });''')
fix('''                }).addOnFailureListener(e->toast("Could not verify Ready players: "+e.getMessage()));''',
'''                }).addOnFailureListener(e->{
                    settleFailed9954(automatic9954,expected,expectedType);
                    if(!automatic9954)toast("Could not verify Ready players: "+e.getMessage());
                });''')
fix('''            }).addOnFailureListener(e->toast("Could not load round moves: "+e.getMessage()));''',
'''            }).addOnFailureListener(e->{
                settleFailed9954(automatic9954,expected,expectedType);
                if(!automatic9954)toast("Could not load round moves: "+e.getMessage());
            });''')
s=s[:a]+part+s[b:];p.write_text(s)
(src/'KingRoomSettleRetry9954.java').write_text("""package com.kingplus.social;
/** Bounded host-side reconnect retries; no client-authoritative award. */
public final class KingRoomSettleRetry9954 {
 private KingRoomSettleRetry9954(){}
 public static final int MAX_RETRIES=4;
 public static boolean allowRetry(int failures,boolean sameRound,
                            boolean moderator,boolean liveMember,boolean active){
   return failures>=0&&failures<MAX_RETRIES&&sameRound&&moderator&&liveMember&&active;
 }
 public static long delayMs(int attempt){
   if(attempt<=0)return 0L;
   return 1000L*(1L<<Math.min(4,attempt));
 }
}
""")
g=root/'app/build.gradle';v=g.read_text()
old="versionCode 232; versionName '9.9.53-auto-party-round-finish-test'"
assert v.count(old)==1
g.write_text(v.replace(old,"versionCode 233; versionName '9.9.54-retry-real-party-results-test'"))
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print("PASS reconnect retry with SERVER-confirmed round, manual Host fallback, paid OFF")
