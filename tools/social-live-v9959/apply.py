#!/usr/bin/env python3
"""KING v9.9.59: real Ready flag and fair Host-inclusive hidden roles.
Applies only to the complete, verified v9.9.58 Android source artifact.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1]); src=root/'app/src/main/java/com/kingplus/social'
p=src/'RoomGameActivity.java'; content=p.read_text()
start=content.index('    private void startSocialRound866(String game){')
end=content.index('\n    private void startRound(String game){',start)
s=content[start:end]
def rep(old,new):
 global s
 assert s.count(old)==1,'v9.9.58 source contract changed: '+old[:90]
 s=s.replace(old,new,1)
rep('''if(KingPartySocialRoster9958.isLive(uid9958,
                           freshReady9958.exists(),s(freshReady9958.getString("uid")),
                           actualMember9958.exists(),s(actualMember9958.getString("uid")))){''',
'''if(KingPartySocialRoster9959.isEligible(uid9958,
                           freshReady9958.exists(),s(freshReady9958.getString("uid")),
                           freshReady9958.getBoolean("ready"),
                           actualMember9958.exists(),s(actualMember9958.getString("uid")))){''')
rep('''                if(!KingPartySocialRoster9958.valid(game,liveUids9958))''',
'''                // Host/drawer must be an actual explicitly Ready Party participant.
                if(!liveUids9958.contains(hostUid9958))
                    throw new IllegalStateException("Host must join Party and tap Ready first");
                if(!KingPartySocialRoster9958.valid(game,liveUids9958))''')
rep('''                java.util.ArrayList<String> possible9958=new java.util.ArrayList<>(liveUids9958);
                possible9958.remove(hostUid9958);
                if(!"draw".equals(game)&&possible9958.isEmpty())
                    throw new IllegalStateException("Need another live Party member for a private role");
                final String target9958="draw".equals(game)?"":possible9958.get(
                    new java.security.SecureRandom().nextInt(possible9958.size()));''',
'''                // Fair play: Host can also draw Werewolf/Spy, not only other players.
                final String target9958="draw".equals(game)?"":
                    KingPartySocialRoster9959.pickHiddenRole(liveUids9958,
                        new java.security.SecureRandom());''')
p.write_text(content[:start]+s+content[end:])
(src/'KingPartySocialRoster9959.java').write_text('''package com.kingplus.social;
import java.security.SecureRandom;
import java.util.List;
/** Only real explicitly Ready Party members can join hidden-role games. */
public final class KingPartySocialRoster9959 {
 private KingPartySocialRoster9959(){}
 public static boolean isEligible(String uid,boolean readyExists,String readyUid,
                                   Boolean readyFlag,boolean memberExists,String memberUid){
   return uid!=null&&!uid.isEmpty()&&readyExists&&Boolean.TRUE.equals(readyFlag)
       &&memberExists&&uid.equals(readyUid)&&uid.equals(memberUid);
 }
 /** Uniform choice from ALL locked Party players, including Host. */
 public static String pickHiddenRole(List<String> activePartyUids,SecureRandom random){
   if(activePartyUids==null||activePartyUids.isEmpty()||random==null)
       throw new IllegalArgumentException("Need active Party players");
   return activePartyUids.get(random.nextInt(activePartyUids.size()));
 }
}
''')
g=root/'app/build.gradle';v=g.read_text()
old="versionCode 237; versionName '9.9.58-social-games-live-roster-test'"
assert v.count(old)==1,'Requires verified v9.9.58 source'
g.write_text(v.replace(old,"versionCode 238; versionName '9.9.59-live-ready-fair-hidden-roles-test'"))
assert 'KingPartyVoiceReconnect9957' in (src/'PartyActivity.java').read_text()
assert 'KingGiftReceiptRetry9955' in (src/'PartyActivity.java').read_text()
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print('PASS real Ready, verified Host, fair hidden roles; Party Voice and Gifts preserved')
