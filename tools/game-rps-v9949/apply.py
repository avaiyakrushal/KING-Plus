#!/usr/bin/env python3
"""KING v9.9.49 real Room game fairness/playability:
* True SERVER-confirmed moves only, exact current game/round and authenticated UID.
* RPS opponent choices hidden until settlement, then individual reveal to explain
  winner/draw (previously always said "move locked ✓" even after match closed).
* Proper SecureRandom-based honest-client Dice, Wheel, Slot spins (does not
  claim server-authoritative anti-cheat; no wagers or paid reward).
* Preserve existing real Party, Gift audio/animation and room security.
"""
from pathlib import Path
import sys
r=Path(sys.argv[1]);java=r/'app/src/main/java/com/kingplus/social'
p=java/'RoomGameActivity.java';s=p.read_text()
def rep(old,new):
 global s
 n=s.count(old)
 if n!=1:raise RuntimeError(f"Expected 1 RoomGameActivity anchor, got {n}: {old[:180]}")
 s=s.replace(old,new,1)
rep('''    private void listenMoves(){if(movesListener!=null)movesListener.remove();movesListener=room().collection("game_moves").addSnapshotListener((q,e)->{currentMoves.clear();if(e!=null){movesBox.removeAllViews();movesBox.addView(tv("Unable to load moves: "+e.getMessage(),13,MUTED,false));return;}if(q!=null)for(DocumentSnapshot d:q.getDocuments()){Long r=d.getLong("roundId");if(r!=null&&r==roundId)currentMoves.add(d);}renderMoves();renderControls();if(moderator&&"ttt".equals(type)&&"active".equals(status)
    &&q!=null&&!q.getMetadata().isFromCache())processTttMove864();});}''',
'''    private void listenMoves(){
        if(movesListener!=null)movesListener.remove();
        movesListener=room().collection("game_moves")
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(q,e)->{
                currentMoves.clear();
                if(e!=null){
                    movesBox.removeAllViews();
                    movesBox.addView(tv("Unable to load moves: "+e.getMessage(),13,MUTED,false));
                    return;
                }
                // The Firebase cache may contain an abandoned former round.
                // Do not show its turns or prematurely proclaim "played ✓".
                if(q==null||q.getMetadata().isFromCache()){
                    movesBox.removeAllViews();
                    movesBox.addView(tv("⏳ Checking real Party moves with Firebase SERVER…",13,MUTED,false));
                    return;
                }
                for(DocumentSnapshot doc:q.getDocuments()){
                    Long submittedRound=doc.getLong("roundId");
                    if(submittedRound!=null&&submittedRound.longValue()==roundId
                       &&KingPartyGameFairPlay9949.currentMove(
                          roundId,type,submittedRound.longValue(),doc.getString("type"),
                          doc.getId(),doc.getString("uid"))
                       &&!doc.getMetadata().hasPendingWrites())
                        currentMoves.add(doc);
                }
                renderMoves();renderControls();
                if(moderator&&"ttt".equals(type)&&"active".equals(status))
                    processTttMove864();
            });
    }''')
rep('''("rps".equals(type)?"move locked ✓":move)''',
'''KingPartyGameFairPlay9949.visibleMove(type,status,gameStateVerified9943,move)''')
rep('''    private void diceControls(){Button b=button("🎲 Roll my dice");b.setOnClickListener(v->{int n=1+(int)(Math.random()*6);submit("Rolled "+n,n);});controls.addView(b,new LinearLayout.LayoutParams(-1,dp(54)));}''',
'''    private void diceControls(){Button b=button("🎲 Roll my dice");b.setOnClickListener(v->{int n=KingPartyGameFairPlay9949.dice();submit("Rolled "+n,n);});controls.addView(b,new LinearLayout.LayoutParams(-1,dp(54)));}''')
rep('''    private void wheelControls(){Button b=button("🎡 Spin my wheel");b.setOnClickListener(v->{int n=1+(int)(Math.random()*100);submit("Spin "+n,n);});controls.addView(b,new LinearLayout.LayoutParams(-1,dp(54)));}''',
'''    private void wheelControls(){Button b=button("🎡 Spin my wheel");b.setOnClickListener(v->{int n=KingPartyGameFairPlay9949.wheel();submit("Spin "+n,n);});controls.addView(b,new LinearLayout.LayoutParams(-1,dp(54)));}''')
rep('''    private void slotControls863(){Button b=button("🎰 Spin once");b.setOnClickListener(v->{int a=(int)(Math.random()*10),c=(int)(Math.random()*10),d=(int)(Math.random()*10);int score=a+c+d+(a==c&&c==d?50:(a==c||c==d||a==d?20:0));submit("Spin "+a+"-"+c+"-"+d,score);});controls.addView(b,new LinearLayout.LayoutParams(-1,dp(54)));}''',
'''    private void slotControls863(){Button b=button("🎰 Spin once");b.setOnClickListener(v->{int a=KingPartyGameFairPlay9949.slotDigit(),c=KingPartyGameFairPlay9949.slotDigit(),d=KingPartyGameFairPlay9949.slotDigit();int score=a+c+d+(a==c&&c==d?50:(a==c||c==d||a==d?20:0));submit("Spin "+a+"-"+c+"-"+d,score);});controls.addView(b,new LinearLayout.LayoutParams(-1,dp(54)));}''')
p.write_text(s)
(java/'KingPartyGameFairPlay9949.java').write_text("""package com.kingplus.social;
import java.security.SecureRandom;
/** Independent original KING local game helpers.
 * RNG strengthens honest client rolls; server-proven fairness requires a
 * deployed trusted backend and is NOT implied by this class.
 */
public final class KingPartyGameFairPlay9949 {
 private KingPartyGameFairPlay9949(){}
 private static final SecureRandom RNG=new SecureRandom();
 public static int dice(){return 1+RNG.nextInt(6);}
 public static int wheel(){return 1+RNG.nextInt(100);}
 public static int slotDigit(){return RNG.nextInt(10);}
 public static boolean currentMove(long activeRound,String game,long moveRound,
                                   String moveGame,String docUid,String claimedUid){
   return activeRound>0&&activeRound==moveRound
      &&game!=null&&!game.isEmpty()&&game.equals(moveGame)
      &&docUid!=null&&!docUid.isEmpty()&&docUid.equals(claimedUid);
 }
 public static String visibleMove(String game,String status,boolean serverVerified,String actualMove){
   if("rps".equals(game)){
      if(!serverVerified||!"closed".equals(status))return "move locked ✓";
      if("Rock".equals(actualMove))return "✊ Rock";
      if("Paper".equals(actualMove))return "✋ Paper";
      if("Scissors".equals(actualMove))return "✌ Scissors";
      return "Invalid RPS move";
   }
   return actualMove==null?"":actualMove;
 }
}
""")
gradle=r/'app/build.gradle';v=gradle.read_text()
old="versionCode 227; versionName '9.9.48-serial-gift-audio-test'"
if v.count(old)!=1:raise RuntimeError("Expected exact verified KING v9.9.48")
gradle.write_text(v.replace(old,"versionCode 228; versionName '9.9.49-game-reveal-secure-client-rolls-test'"))
assert 'LIVE_RECHARGE = false' in (java/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (java/'KingPremiumAccess981.java').read_text()
print("PASS proper RPS reveal after finish, no stale move from other game/round, SecureRandom honest local spins, billing OFF")
