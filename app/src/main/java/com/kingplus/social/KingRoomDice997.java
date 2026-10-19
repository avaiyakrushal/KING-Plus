package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.SystemClock;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/** Small Firebase multiplayer Dice panel remains on top of PartyActivity.
 * No diamonds or wallet updates. Uses existing signed-in room event rules.
 */
public final class KingRoomDice997 {
    private KingRoomDice997(){}
    private static final SecureRandom dice=new SecureRandom();

    private static int dp(Activity a,int n){
        return (int)(a.getResources().getDisplayMetrics().density*n+.5f);
    }
    private static TextView item(Activity a,String text,int size,boolean bold,int color){
        TextView t=new TextView(a);t.setText(text);t.setTextSize(size);
        t.setTextColor(color);t.setPadding(dp(a,10),dp(a,8),dp(a,10),dp(a,8));
        t.setGravity(Gravity.CENTER);
        if(bold)t.setTypeface(null,Typeface.BOLD);
        return t;
    }
    private static GradientDrawable bg(int color,int radius,Activity a){
        GradientDrawable b=new GradientDrawable();b.setColor(color);
        b.setCornerRadius(dp(a,radius));return b;
    }
    public static void open(Activity host,FirebaseFirestore db,FirebaseUser me,
                            String roomId,String displayName,Runnable moreGames){
        if(host==null||db==null||me==null||roomId==null||roomId.trim().isEmpty())return;
        final String code=roomId.trim(), actor=me.getUid();
        final LinearLayout container=new LinearLayout(host);
        container.setPadding(dp(host,12),dp(host,8),dp(host,12),dp(host,10));
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundColor(0xfff5f0ff);
        TextView instructions=item(host,"🎲 LIVE Party Dice\nBoth phones see each other's rolls. Just for fun — no money or diamonds.",12,true,0xff3a2853);
        container.addView(instructions,new LinearLayout.LayoutParams(-1,dp(host,70)));
        TextView scoreboard=item(host,"Connecting to Party game events…",13,false,0xff251b35);
        scoreboard.setGravity(Gravity.LEFT|Gravity.TOP);
        ScrollView scroll=new ScrollView(host);
        scroll.addView(scoreboard);
        scroll.setBackground(bg(0xffffffff,14,host));
        container.addView(scroll,new LinearLayout.LayoutParams(-1,dp(host,172)));
        TextView roll=item(host,"🎲 ROLL NOW",17,true,Color.WHITE);
        roll.setBackground(bg(0xff774add,14,host));
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(host,56));
        rp.setMargins(0,dp(host,9),0,dp(host,6));
        container.addView(roll,rp);
        TextView games=item(host,"🎮 OPEN ALL ROOM MULTIPLAYER GAMES",12,true,Color.WHITE);
        games.setBackground(bg(0xff364c70,14,host));
        container.addView(games,new LinearLayout.LayoutParams(-1,dp(host,50)));
        final AlertDialog dialog=new AlertDialog.Builder(host)
            .setTitle("🎮 Play inside KING Party").setView(container)
            .setNegativeButton("Close",null).create();
        final ListenerRegistration[] updates=new ListenerRegistration[1];
        final long[] lastRoll=new long[1];
        final boolean[] verifiedMember9937={false};
        final boolean[] sendingRoll9937={false};
        roll.setEnabled(false);
        roll.setText("Verifying Party membership…");
        roll.setOnClickListener(v->{
            if(!dialog.isShowing()||host.isFinishing()||host.isDestroyed())return;
            if(!verifiedMember9937[0]){android.widget.Toast.makeText(host,"Join the real Party first",android.widget.Toast.LENGTH_SHORT).show();return;}
            if(sendingRoll9937[0])return;
            long now=SystemClock.elapsedRealtime();
            if(now-lastRoll[0]<3000L){
                android.widget.Toast.makeText(host,"Wait 3 seconds",android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            lastRoll[0]=now;
            sendingRoll9937[0]=true;
            roll.setEnabled(false);roll.setText("Syncing dice roll…");
            int n=1+dice.nextInt(6);
            Map<String,Object> ev=new HashMap<>();
            ev.put("actorUid",actor);
            ev.put("actorName",displayName==null?"KING User":displayName);
            ev.put("type","dice_room_997");
            ev.put("text","🎲 "+n+" / 6");
            ev.put("createdAt",FieldValue.serverTimestamp());
            db.collection("live_rooms").document(code).collection("events").add(ev)
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
                    if(!host.isFinishing()&&!host.isDestroyed())
                        android.widget.Toast.makeText(host,"Dice sync failed: "+
                          err.getLocalizedMessage(),android.widget.Toast.LENGTH_LONG).show();
                });
        });
        games.setOnClickListener(v->{
            dialog.dismiss();
            if(moreGames!=null&&!host.isFinishing()&&!host.isDestroyed())moreGames.run();
        });
        dialog.setOnDismissListener(v->{
            if(updates[0]!=null){updates[0].remove();updates[0]=null;}
        });
        dialog.show();
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
        updates[0]=db.collection("live_rooms").document(code).collection("events")
          .orderBy("createdAt",Query.Direction.DESCENDING).limit(35)
          .addSnapshotListener((snapshot,error)->{
              if(!dialog.isShowing()||host.isFinishing()||host.isDestroyed())return;
              if(error!=null){scoreboard.setText("Realtime game unavailable: "+
                        error.getLocalizedMessage());return;}
              KingDiceLeaderboard9937 scores9937=new KingDiceLeaderboard9937();
              StringBuilder b=new StringBuilder("🎲 LIVE ROOM DICE • all phones synced");
              int count=0;
              if(snapshot!=null)for(DocumentSnapshot event:snapshot.getDocuments()){
                  if(!"dice_room_997".equals(event.getString("type")))continue;
                  String name=event.getString("actorName");
                  String text=event.getString("text");
                  b.append("\n").append(name==null?"Player":name)
                      .append(": ").append(text==null?"🎲":text);
                  scores9937.add(event.getString("actorUid"),name,text);
                  if(++count>=14)break;
              }
              if(count==0)b.append("\n\nNo rolls yet. Tap ROLL NOW!");
              else b.insert(0,scores9937.summary()+"\n\n");
              scoreboard.setText(b.toString());
          });
    }
}
