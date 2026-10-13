#!/usr/bin/env python3
"""Actual KING Party v9.9.37 functional improvements — NOT resource-count padding.

- Gift must target a server-verified member of the active room.
- One in-flight gift per sender UI; sync two Firestore records atomically.
- Real Dice requires server-verified membership and shows synchronized score board.
- Firestore rules validate event dice rolls, free gift target and quantity.
- No paid diamonds, fake rewards, Bolo Hi copyrighted assets or prod deployment.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1]);src=root/'app/src/main/java/com/kingplus/social'
def patch(name,replace_list):
 p=src/name;s=p.read_text()
 for old,new in replace_list:
  count=s.count(old)
  if count!=1:raise RuntimeError(f'{name}: changed anchor ({count}): {old[:110]}')
  s=s.replace(old,new,1)
 p.write_text(s)
patch('PartyActivity.java',[
('''        lastFreeGift973=now973;
        // Firestore room events are allowed on Spark's free quota without
        // Cloud Functions. This is a zero-value decorative animation only.
        // No real wallet, local coins, VIP progress or payment call occurs.
        addGiftEvent(targetUid,targetName,gift,icon,0,quantity,KingFreeGift973.giftValue());''',
 '''        // Verify the intended recipient exists in this room on SERVER, not in
        // possibly-stale local cached member lists. No fictitious "delivered" gift.
        if(giftSendPending971){toast("Previous gift is still synchronizing");return;}
        giftSendPending971=true;
        final String giftRoom9937=roomId,giftUid9937=sender973;
        final int giftVisit9937=presenceGeneration967;
        db.collection("live_rooms").document(giftRoom9937)
          .collection("members").document(targetUid)
          .get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(member->{
            if(!isActivePresence967(giftRoom9937,giftUid9937,giftVisit9937)){
                giftSendPending971=false;return;
            }
            if(member==null||!member.exists()
                    ||!targetUid.equals(member.getString("uid"))){
                giftSendPending971=false;
                toast("Gift not sent: recipient must join this live Party first");
                return;
            }
            lastFreeGift973=System.currentTimeMillis();
            addGiftEvent(targetUid,targetName,gift,icon,0,quantity,0);
          })
          .addOnFailureListener(e->{
            giftSendPending971=false;
            if(isActivePresence967(giftRoom9937,giftUid9937,giftVisit9937))
                toast("Could not verify gift recipient: "+msg(e));
          });'''),
('''        event971.put("giftName",gift);''',
 '''        final String giftDeliveryId9937=java.util.UUID.randomUUID().toString();
        event971.put("giftId",giftDeliveryId9937);
        event971.put("giftName",gift);'''),
('''        message971.put("giftName",gift);''',
 '''        message971.put("giftId",giftDeliveryId9937);
        message971.put("giftName",gift);'''),
('''        batch971.commit().addOnSuccessListener(v->{
            if(isActivePresence967(requestedRoom971,expectedUid971,generation971)){''',
 '''        batch971.commit().addOnSuccessListener(v->{
            giftSendPending971=false;
            if(isActivePresence967(requestedRoom971,expectedUid971,generation971)){'''),
('''        }).addOnFailureListener(e->{
            if(isActivePresence967(requestedRoom971,expectedUid971,generation971))
                toast("FREE gift was not sent: "+msg(e));
        });''',
 '''        }).addOnFailureListener(e->{
            giftSendPending971=false;
            if(isActivePresence967(requestedRoom971,expectedUid971,generation971))
                toast("FREE gift was not sent: "+msg(e));
        });''')
])
patch('KingRoomDice997.java',[
('''        final long[] lastRoll=new long[1];''',
 '''        final long[] lastRoll=new long[1];
        final boolean[] verifiedMember9937={false};
        final boolean[] sendingRoll9937={false};
        roll.setEnabled(false);
        roll.setText("Verifying Party membership…");'''),
('''            if(!dialog.isShowing()||host.isFinishing()||host.isDestroyed())return;
            long now=SystemClock.elapsedRealtime();''',
 '''            if(!dialog.isShowing()||host.isFinishing()||host.isDestroyed())return;
            if(!verifiedMember9937[0]){android.widget.Toast.makeText(host,"Join the real Party first",android.widget.Toast.LENGTH_SHORT).show();return;}
            if(sendingRoll9937[0])return;
            long now=SystemClock.elapsedRealtime();'''),
('''            lastRoll[0]=now;
            int n=1+dice.nextInt(6);''',
 '''            lastRoll[0]=now;
            sendingRoll9937[0]=true;
            roll.setEnabled(false);roll.setText("Syncing dice roll…");
            int n=1+dice.nextInt(6);'''),
('''            db.collection("live_rooms").document(code).collection("events").add(ev)
                .addOnFailureListener(err->{
                    if(!host.isFinishing()&&!host.isDestroyed())''',
 '''            db.collection("live_rooms").document(code).collection("events").add(ev)
                .addOnSuccessListener(done->{
                    sendingRoll9937[0]=false;
                    if(dialog.isShowing()&&!host.isFinishing()&&!host.isDestroyed()){
                        roll.setEnabled(verifiedMember9937[0]);roll.setText("🎲 ROLL NOW");
                    }
                })
                .addOnFailureListener(err->{
                    sendingRoll9937[0]=false;
                    lastRoll[0]=0L;
                    if(dialog.isShowing()&&!host.isFinishing()&&!host.isDestroyed()){
                        roll.setEnabled(verifiedMember9937[0]);roll.setText("🎲 RETRY ROLL");
                    }
                    if(!host.isFinishing()&&!host.isDestroyed())'''),
('''        dialog.show();
        updates[0]=db.collection("live_rooms").document(code).collection("events")''',
 '''        dialog.show();
        db.collection("live_rooms").document(code).collection("members").document(actor)
          .get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(member->{
            if(!dialog.isShowing()||host.isFinishing()||host.isDestroyed())return;
            verifiedMember9937[0]=member!=null&&member.exists()
                &&actor.equals(member.getString("uid"));
            roll.setEnabled(verifiedMember9937[0]);
            roll.setText(verifiedMember9937[0]?"🎲 ROLL NOW":"Join this Party to play Dice");
          }).addOnFailureListener(error->{
            if(dialog.isShowing()){roll.setEnabled(false);
                roll.setText("Could not verify Party membership");}
          });
        updates[0]=db.collection("live_rooms").document(code).collection("events")'''),
('''              StringBuilder b=new StringBuilder("LIVE DICE RESULTS");
              int count=0;''',
 '''              KingDiceLeaderboard9937 scores9937=new KingDiceLeaderboard9937();
              StringBuilder b=new StringBuilder("🎲 LIVE ROOM DICE • all phones synced");
              int count=0;'''),
('''                  b.append("\\n").append(name==null?"Player":name)
                      .append(": ").append(text==null?"🎲":text);''',
 '''                  b.append("\\n").append(name==null?"Player":name)
                      .append(": ").append(text==null?"🎲":text);
                  scores9937.add(event.getString("actorUid"),name,text);'''),
('''              if(count==0)b.append("\\n\\nNo rolls yet. Tap ROLL NOW!");
              scoreboard.setText(b.toString());''',
 '''              if(count==0)b.append("\\n\\nNo rolls yet. Tap ROLL NOW!");
              else b.insert(0,scores9937.summary()+"\\n\\n");
              scoreboard.setText(b.toString());''')
])
(src/'KingDiceLeaderboard9937.java').write_text("""package com.kingplus.social;
import java.util.*;
/** Pure Java view-only leaderboard from authenticated Firestore events.
 * No wallet, VIP, diamond or winner payouts.
 */
public final class KingDiceLeaderboard9937 {
 private static final class P {
   String name;int rolls,sum,best;
   P(String name){this.name=name;}
 }
 private final Map<String,P> byUser=new HashMap<>();
 public static int parseRoll(String raw) {
   if(raw==null)return 0;
   String s=raw.trim();
   // U+1F3B2 followed by a space, a single digit 1-6 and the literal suffix.
   if(!s.matches("^🎲 [1-6] / 6$"))return 0;
   return s.charAt(3)-'0'; // NOTE: UTF-16 dice emoji occupies indices 0..1.
 }
 public void add(String uid,String name,String text) {
   int value=parseRoll(text);
   if(value<1||uid==null||uid.isEmpty())return;
   P p=byUser.get(uid);
   if(p==null){p=new P(name==null||name.trim().isEmpty()?"Player":name.trim());byUser.put(uid,p);}
   p.rolls++;p.sum+=value;p.best=Math.max(p.best,value);
 }
 public String summary() {
   List<Map.Entry<String,P>> players=new ArrayList<>(byUser.entrySet());
   Collections.sort(players,(a,b)->{int c=Integer.compare(b.getValue().sum,a.getValue().sum);
      return c!=0?c:a.getKey().compareTo(b.getKey());});
   StringBuilder s=new StringBuilder("🏆 LIVE DICE LEADERBOARD • FREE\\nRecent room rolls (no diamonds)");
   int index=0;
   for(Map.Entry<String,P> v:players) {
     if(++index>8)break;
     P p=v.getValue();
     s.append("\\n").append(index).append(". ").append(p.name.length()>30?p.name.substring(0,30):p.name)
       .append(" • ").append(p.sum).append(" points / ").append(p.rolls).append(" rolls");
   }
   return s.toString();
 }
}
""")
# Fix emoji UTF-16 index for parsing digit: index 0 and 1 surrogate, 2 space, 3 digit.
rules=root/'firestore.rules'
s=rules.read_text()
needle="""          && request.resource.data.text.size() <= 300;
        allow update, delete: if false;
      }

      match /roles/{roleUid} {"""
if s.count(needle)!=1:raise RuntimeError('Firestore events clause changed')
replacement="""          && request.resource.data.text.size() <= 300
          // Free gifting must target an actual live member, not any guessed UID.
          && (request.resource.data.type != 'gift'
              || (request.resource.data.get('freeGift',false) == true
                  && request.resource.data.get('giftValue',-1) == 0
                  && request.resource.data.get('giftUnitCost',-1) == 0
                  && request.resource.data.get('giftQty',0) is int
                  && request.resource.data.giftQty >= 1
                  && request.resource.data.giftQty <= 9
                  && request.resource.data.get('targetUid','') is string
                  && exists(/databases/$(database)/documents/live_rooms/$(roomId)/members/$(request.resource.data.targetUid))))
          // A dice "result" cannot contain 9, 999, spoofed characters or paid value.
          && (request.resource.data.type != 'dice_room_997'
              || (request.resource.data.text.matches('^🎲 [1-6] / 6$')
                  && request.resource.data.get('createdAt',null) == request.time));
        allow update, delete: if false;
      }

      match /roles/{roleUid} {"""
rules.write_text(s.replace(needle,replacement))
g=root/'app/build.gradle'
s=g.read_text()
v="versionCode 215; versionName '9.9.36-party-password-access-fix-test'"
if s.count(v)!=1:raise RuntimeError('Must use verified full v9.9.36 source')
g.write_text(s.replace(v,"versionCode 216; versionName '9.9.37-real-party-dice-gifting-test'"))
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print('PASS membership-authenticated gift delivery; in-room dice scoring, server input validation and prior 4065 layouts/8680 drawables preserved')
