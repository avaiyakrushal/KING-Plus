#!/usr/bin/env python3
"""KING Plus 9.9.60: Real Tic Tac Toe must use actual Ready=true live Party
members and fairly select two participants from ALL eligible players.
No XML/Drawable additions, no paid wallet changes.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1]); src=root/'app/src/main/java/com/kingplus/social'
p=src/'RoomGameActivity.java'
content=p.read_text()
a=content.index('    private void startTtt864(){')
b=content.index('    private void submitTtt864(int idx){',a)
start=content[a:b]
def rep(old,new):
 global start
 assert start.count(old)==1,("Expected exact v9.9.59 TTT code:",old[:130],start.count(old))
 start=start.replace(old,new,1)
rep('''                    if(KingTttLiveRoster9951.valid(uid9951,
                        freshReady9951.exists(),
                        s(freshReady9951.getString("uid")),
                        freshMember9951.exists(),
                        s(freshMember9951.getString("uid"))))''',
'''                    if(KingTttLiveRoster9951.valid(uid9951,
                        freshReady9951.exists(),
                        s(freshReady9951.getString("uid")),
                        freshReady9951.getBoolean("ready"),
                        freshMember9951.exists(),
                        s(freshMember9951.getString("uid"))))''')
rep('''                    if(active9951.size()>=2)break;''','''                    // Collect ALL currently Ready Party members; not just
                    // the first two names alphabetically.''')
rep('''                DocumentSnapshot x9951=active9951.get(0),o9951=active9951.get(1);''',
'''                // SecureRandom chooses two distinct real Ready members from
                // the entire live pool. The same pair is locked to this round
                // and all other members remain spectators.
                int[] pair9960=KingTttFairRoster9960.pickPair(
                    active9951.size(),new java.security.SecureRandom());
                DocumentSnapshot x9951=active9951.get(pair9960[0]),
                    o9951=active9951.get(pair9960[1]);''')
assert 'Source.SERVER' in start and 'db.runTransaction(tx->' in start
assert 'tx.set(state(),value9951)' in start
p.write_text(content[:a]+start+content[b:])

helper=src/'KingTttLiveRoster9951.java'; h=helper.read_text()
marker='''  public static boolean valid(String expectedUid,
      boolean readyExists,String readyUid,boolean memberExists,String memberUid){'''
assert h.count(marker)==1
overload='''  /** Explicit Ready flag must still be true in the transaction's SERVER read. */
  public static boolean valid(String expectedUid,
      boolean readyExists,String readyUid,Boolean readyFlag,
      boolean memberExists,String memberUid){
    return Boolean.TRUE.equals(readyFlag)
       && valid(expectedUid,readyExists,readyUid,memberExists,memberUid);
  }
'''
helper.write_text(h.replace(marker,overload+marker,1))

(src/'KingTttFairRoster9960.java').write_text('''package com.kingplus.social;
import java.security.SecureRandom;
/** Uniformly select two DIFFERENT current Party Ready members for live X/O. */
public final class KingTttFairRoster9960 {
  private KingTttFairRoster9960(){}
  public static int[] pickPair(int eligibleCount,SecureRandom random){
    if(eligibleCount<2||eligibleCount>50||random==null)
      throw new IllegalArgumentException("Need 2-50 live Ready Party players");
    final int x=random.nextInt(eligibleCount);
    final int other=random.nextInt(eligibleCount-1);
    // Exclude X, keep O uniform across every remaining player.
    final int o=other>=x?other+1:other;
    return new int[]{x,o};
  }
}
''')
g=root/'app/build.gradle'; v=g.read_text()
oldver="versionCode 238; versionName '9.9.59-live-ready-fair-hidden-roles-test'"
assert v.count(oldver)==1,'Require complete original 9.9.59 source'
g.write_text(v.replace(oldver,
    "versionCode 239; versionName '9.9.60-fair-live-ttt-ready-test'"))
party=(src/'PartyActivity.java').read_text()
for name in ('KingPartyVoiceReconnect9957','KingPartyMicConsent9956','KingGiftReceiptRetry9955'):
 assert name in party,name
assert 'KingPartySocialRoster9959' in content
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print('PASS real Ready=true Party Tic Tac Toe plus fair two-live-player pairing; Voice/Gifts preserved')
