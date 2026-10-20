package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RoomGameActivity extends Activity {
    private static final int BG=0xff1e1333, CARD=0xff34234f, PURPLE=0xff8a49ed, GOLD=0xffffd768, MUTED=0xffcfc4df;
    private FirebaseFirestore db; private FirebaseUser me;
    private String roomId="",roomName="Live Room",displayName="KING User",requestedGame="";
    private boolean moderator;
    private long roundId; private String type="",prompt="",result="",status="closed"; private String tttBoard864=".........",tttTurnUid864="",tttXUid864="",tttOUid864="",tttXName864="X",tttOName864="O"; private boolean tttProcessing864; private String memoryLayout865=""; private long memoryRound865; private int memoryFirst865=-1,memoryPairs865,memoryAttempts865,dominoTarget865,treasureChest10100; private boolean memoryComparing865; private boolean[] memoryDone865=new boolean[8]; private Button[] memoryButtons865=new Button[8]; private long memoryStart865;
    private ListenerRegistration stateListener,movesListener,readyListener,moderatorRoleListener;
    // A state metadata update is NOT a new round. Re-subscribing game_moves
    // on every snapshot drops live score state and can restart result checks.
    private long observedMoveRound9979=-1L;
    private String observedMoveType9979=""; private long privateRoleRound866; private String privateRole866="",privateSecret866="",drawSketch866="",drawerUid866="",drawerName866=""; private DrawPad866 drawPad866;
    private LinearLayout page,controls,movesBox,readyBox; private TextView stateText,roleText,phaseHint950,connection950; private android.widget.ProgressBar syncSpinner950;
    private final List<DocumentSnapshot> currentMoves=new ArrayList<>();
    private boolean gameMemberVerified992=false;
    private boolean gameStateVerified9943=false;
    private final java.util.ArrayList<String> frozenRoster9947=new java.util.ArrayList<>();
    private long lastAutoSettlementAttempt9953=-1L;
    private long autoRetryRound9954=-1L;
    private int autoRetryFailures9954=0;
    private boolean autoSettlementInFlight9954=false;
    private final android.os.Handler settlementHandler9954=
        new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable settlementRetryTask9954;

    @Override public void onCreate(Bundle b){super.onCreate(b);try{db=FirebaseFirestore.getInstance();me=FirebaseAuth.getInstance().getCurrentUser();}catch(Exception e){db=null;me=null;}
        roomId=s(getIntent().getStringExtra("roomId"));roomName=s(getIntent().getStringExtra("roomName"));displayName=s(getIntent().getStringExtra("displayName"));requestedGame=s(getIntent().getStringExtra("requestedGame"));if(roomName.isEmpty())roomName="Live Room";if(displayName.isEmpty())displayName="KING User";
        build(); if(db==null||me==null||roomId.isEmpty()){stateText.setText("Sign in and open this from a live Party room.");if(syncSpinner950!=null)syncSpinner950.setVisibility(View.GONE);if(connection950!=null)connection950.setText("Party room connection unavailable");if(phaseHint950!=null)phaseHint950.setText("Open Games from a live Party room to play with room members.");return;} ensureMembership900(); }
    @Override protected void onDestroy(){
        clearScheduledSettlement9954();
        autoSettlementInFlight9954=false;
        // Closing Games does not unready the other phone in the SAME Party.
        // Ready changes only on explicit user action / real room departure.
        sheepClock9925.removeCallbacks(sheepTick9925);
        reactionClock9927.removeCallbacks(reactionGo9927);
        if(membershipListener9938!=null)membershipListener9938.remove();if(stateListener!=null)stateListener.remove();if(movesListener!=null)movesListener.remove();if(readyListener!=null)readyListener.remove();if(readyMembersListener9971!=null)readyMembersListener9971.remove();if(moderatorRoleListener!=null)moderatorRoleListener.remove();super.onDestroy();}

    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);} private String s(String x){return x==null?"":x.trim();}
    private GradientDrawable bg(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    private TextView tv(String x,int size,int color,boolean bold){TextView v=new TextView(this);v.setText(x);v.setTextSize(size);v.setTextColor(color);if(bold)v.setTypeface(null,Typeface.BOLD);v.setPadding(dp(12),dp(8),dp(12),dp(8));return v;}
    private Button button(String x){Button b=new Button(this);b.setText(x);b.setTextColor(Color.WHITE);b.setTextSize(14);b.setAllCaps(false);b.setBackground(bg(PURPLE,16));return b;}
    private void toast(String x){Toast.makeText(this,x,Toast.LENGTH_SHORT).show();}

    private void build(){LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);ScrollView sc=new ScrollView(this);page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(16),dp(12),dp(16),dp(28));sc.addView(page);root.addView(sc,new LinearLayout.LayoutParams(-1,-1));
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);TextView back=tv("‹",34,Color.WHITE,true);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->finish());head.addView(back,new LinearLayout.LayoutParams(dp(48),dp(52)));LinearLayout hi=new LinearLayout(this);hi.setOrientation(LinearLayout.VERTICAL);hi.addView(tv("🎮 Room Multiplayer",23,Color.WHITE,true));hi.addView(tv(roomName,12,MUTED,false));head.addView(hi,new LinearLayout.LayoutParams(0,dp(62),1));page.addView(head);
        roleText=tv("Checking room role…",12,MUTED,false);page.addView(roleText);
        LinearLayout sync950=new LinearLayout(this);sync950.setGravity(Gravity.CENTER_VERTICAL);sync950.setPadding(dp(4),0,dp(4),0);syncSpinner950=new android.widget.ProgressBar(this);sync950.addView(syncSpinner950,new LinearLayout.LayoutParams(dp(30),dp(30)));connection950=tv("Connecting to Party game state…",12,MUTED,true);sync950.addView(connection950,new LinearLayout.LayoutParams(0,dp(44),1));page.addView(sync950,new LinearLayout.LayoutParams(-1,dp(44)));
        phaseHint950=tv("Loading multiplayer state…",13,0xffe8ddf7,true);phaseHint950.setGravity(Gravity.CENTER);phaseHint950.setBackground(bg(0xff291d3d,14));LinearLayout.LayoutParams ph950=new LinearLayout.LayoutParams(-1,dp(54));ph950.setMargins(0,dp(4),0,dp(4));page.addView(phaseHint950,ph950);
        if(!requestedGame.isEmpty()){TextView requested=tv("🎮 Selected from Games: "+requestedGame+" • realtime room mode",12,GOLD,true);requested.setBackground(bg(CARD,12));page.addView(requested,new LinearLayout.LayoutParams(-1,dp(54)));Button selectedStart=button("▶ "+requestedGame+" • Start / Join");selectedStart.setOnClickListener(v->startRequestedGame940(requestedGame));LinearLayout.LayoutParams sgp=new LinearLayout.LayoutParams(-1,dp(50));sgp.setMargins(0,dp(4),0,dp(6));page.addView(selectedStart,sgp);}
        stateText=tv("Waiting for active round",18,GOLD,true);stateText.setGravity(Gravity.CENTER);stateText.setBackground(bg(CARD,18));LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.setMargins(0,dp(8),0,dp(8));page.addView(stateText,sp);
        Button history940=button("📜 Recent game results");history940.setOnClickListener(v->showGameHistory940());LinearLayout.LayoutParams hp940=new LinearLayout.LayoutParams(-1,dp(48));hp940.setMargins(0,0,0,dp(8));page.addView(history940,hp940);
        controls=new LinearLayout(this);controls.setOrientation(LinearLayout.VERTICAL);page.addView(controls,new LinearLayout.LayoutParams(-1,-2));
        TextView readyTitle=tv("Players ready",16,Color.WHITE,true);readyTitle.setPadding(dp(2),dp(14),0,dp(4));page.addView(readyTitle);readyBox=new LinearLayout(this);readyBox.setOrientation(LinearLayout.VERTICAL);page.addView(readyBox,new LinearLayout.LayoutParams(-1,-2));
        TextView live=tv("Live players / moves",16,Color.WHITE,true);live.setPadding(dp(2),dp(14),0,dp(4));page.addView(live);movesBox=new LinearLayout(this);movesBox.setOrientation(LinearLayout.VERTICAL);page.addView(movesBox,new LinearLayout.LayoutParams(-1,-2));
        setSafe(root); }
    private void setSafe(View root){setContentView(root);ViewCompat.setOnApplyWindowInsetsListener(root,(v,in)->{Insets z=in.getInsets(WindowInsetsCompat.Type.systemBars());v.setPadding(z.left,z.top,z.right,z.bottom);return in;});ViewCompat.requestApplyInsets(root);}

    private DocumentReference room(){return db.collection("live_rooms").document(roomId);} private DocumentReference state(){return room().collection("game_state").document("current");}
    // Real live membership, not a one-off check that stays true after removal.
    private ListenerRegistration membershipListener9938;
    private void ensureMembership900(){
        if(me==null||db==null||roomId.isEmpty())return;
        final String checkingUid=me.getUid(),checkingRoom=roomId;
        if(membershipListener9938!=null)membershipListener9938.remove();
        membershipListener9938=room().collection("members").document(checkingUid)
          .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(member,error)->{
            com.google.firebase.auth.FirebaseUser actual10045=com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if(isFinishing()||isDestroyed()||!checkingRoom.equals(roomId)
                ||me==null||!checkingUid.equals(me.getUid())
                ||actual10045==null||!checkingUid.equals(actual10045.getUid()))return;
            // Offline cache (or listener error) cannot prove a moderator removed
            // this player. Keep existing UI while waiting for SERVER evidence.
            if(error!=null||member==null||member.getMetadata().isFromCache()
                ||member.getMetadata().hasPendingWrites()){
                gameStateVerified9943=false;
                if(connection950!=null)connection950.setText("● Reconnecting to Party membership SERVER");
                if(syncSpinner950!=null)syncSpinner950.setVisibility(View.VISIBLE);
                return;
            }
            boolean active=member.exists() && checkingUid.equals(member.getString("uid"));
            if(!active){
                if(gameMemberVerified992){
                    if(stateListener!=null){stateListener.remove();stateListener=null;}
                    if(movesListener!=null){movesListener.remove();movesListener=null;}
                    if(readyListener!=null){readyListener.remove();readyListener=null;}
                    if(readyMembersListener9971!=null){readyMembersListener9971.remove();readyMembersListener9971=null;}
                    readySeen9971=false;membersSeen9971=false;
                    readyDocs9971=new ArrayList<>();memberNames9971=new HashMap<>();
                    if(moderatorRoleListener!=null){moderatorRoleListener.remove();moderatorRoleListener=null;}
                    controls.removeAllViews();movesBox.removeAllViews();readyBox.removeAllViews();
                }
                sheepClock9925.removeCallbacks(sheepTick9925);
                sheepStart9925=0L;
                sheepStartPending9940=false;
                clearScheduledSettlement9954();
                autoSettlementInFlight9954=false;
                gameMemberVerified992=false;gameStateVerified9943=false;frozenRoster9947.clear();moderator=false;
                roleText.setText(error==null?"Join this Party Room first to play.":"Party membership verification failed");
                connection950.setText("● Room game locked • rejoin from Party");
                if(syncSpinner950!=null)syncSpinner950.setVisibility(View.GONE);
                return;
            }
            if(gameMemberVerified992){
                if(!gameStateVerified9943&&stateListener!=null){
                    // Refresh the game listener: a membership-only reconnect
                    // might otherwise leave gameplay locked indefinitely.
                    if(connection950!=null)connection950.setText("● Party membership restored • syncing game");
                    attach();
                }
                return;
            }
            gameMemberVerified992=true;
            roleText.setText("● Live Party membership verified by Firebase SERVER");
            loadRoleAndListen();
          });
    }
    private void loadRoleAndListen(){
        if(me==null||db==null||!gameMemberVerified992)return;
        final String owner10046=me.getUid(), room10046=roomId;
        room().get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(d->{
            if(!roleSessionCurrent10046(owner10046,room10046))return;
            if(d==null||!d.exists()){
                moderator=false;roleText.setText("Room closed • return to Party");return;
            }
            String owner=s(d.getString("ownerUid"));
            if(owner10046.equals(owner)){moderator=true;finishRole();}
            else{
                if(moderatorRoleListener!=null)moderatorRoleListener.remove();
                moderatorRoleListener=room().collection("roles").document(owner10046)
                    .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(r,e)->{
                        if(!roleSessionCurrent10046(owner10046,room10046))return;
                        if(e!=null||r==null||r.getMetadata().isFromCache()||r.getMetadata().hasPendingWrites()){
                            moderator=false;roleText.setText("Checking host/co-host permission on SERVER");return;
                        }
                        moderator=r.exists()&&"cohost".equals(r.getString("role"));
                        finishRole();
                    });
            }
        }).addOnFailureListener(e->{
            if(roleSessionCurrent10046(owner10046,room10046))roleText.setText("Room unavailable • reopen from Party");
        });
    }
    private boolean roleSessionCurrent10046(String uid,String room){
        FirebaseUser current=FirebaseAuth.getInstance().getCurrentUser();
        return !isFinishing()&&!isDestroyed()&&gameMemberVerified992&&me!=null&&current!=null
            &&uid.equals(me.getUid())&&uid.equals(current.getUid())&&room.equals(roomId);
    }
    private void finishRole(){roleText.setText(moderator?"👑 Host / Co-host • start rounds when players are ready":"👤 Player • tap Ready, then join the active round");gameStateVerified9943=false;
        if(connection950!=null){connection950.setText("● Checking live round on Firebase SERVER");connection950.setTextColor(0xffffd768);}
        attach();listenReady750();renderControls();}
    private void attach(){if(stateListener!=null)stateListener.remove();stateListener=state().addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(d,e)->{
        com.google.firebase.auth.FirebaseUser actual10045=com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if(isFinishing()||isDestroyed()||me==null||actual10045==null
            ||!me.getUid().equals(actual10045.getUid())||!gameMemberVerified992)return;
        gameStateVerified9943=e==null&&d!=null&&!d.getMetadata().isFromCache()&&!d.getMetadata().hasPendingWrites();
        if(!gameStateVerified9943){
            if(connection950!=null){connection950.setText("● Game reconnecting — waiting for SERVER");
                connection950.setTextColor(0xffffd768);}
            if(syncSpinner950!=null)syncSpinner950.setVisibility(View.VISIBLE);
            if(e!=null&&stateText!=null)stateText.setText("Game state error: "+e.getMessage());
            return;
        }
        if(connection950!=null){connection950.setText("● Game round verified by Firebase SERVER");connection950.setTextColor(0xff55e2a4);}
        if(syncSpinner950!=null)syncSpinner950.setVisibility(View.GONE);
        frozenRoster9947.clear();
        if(d.exists()){
            Object participants9947=d.get("participantUids");
            if(participants9947 instanceof java.util.List)
                for(Object participant9947:(java.util.List<?>)participants9947)
                    if(participant9947 instanceof String)
                        frozenRoster9947.add((String)participant9947);
        }
        final long priorRound9954=roundId;
        if(!d.exists()){roundId=0;type="";prompt="";result="";status="closed";resetTttState864();resetMemoryState865("",0);dominoTarget865=0;treasureChest10100=0;resetSocialState866();}else{Long r=d.getLong("roundId");roundId=r==null?0:r;type=s(d.getString("type"));prompt=s(d.getString("prompt"));result=s(d.getString("result"));status=s(d.getString("status"));if("ttt".equals(type)){tttBoard864=s(d.getString("board"));if(tttBoard864.length()!=9)tttBoard864=".........";tttTurnUid864=s(d.getString("turnUid"));tttXUid864=s(d.getString("xUid"));tttOUid864=s(d.getString("oUid"));tttXName864=s(d.getString("xName"));tttOName864=s(d.getString("oName"));}else resetTttState864();if("memory".equals(type)){String ml=s(d.getString("memoryLayout"));if(roundId!=memoryRound865||!ml.equals(memoryLayout865))resetMemoryState865(ml,roundId);}else resetMemoryState865("",0);Long dt=d.getLong("dominoTarget");dominoTarget865=dt==null?0:Math.max(0,Math.min(6,dt.intValue()));Long tc=d.getLong("treasureChest");treasureChest10100=tc==null?0:Math.max(0,Math.min(3,tc.intValue()));if("draw".equals(type)){drawerUid866=s(d.getString("drawerUid"));drawerName866=s(d.getString("drawerName"));drawSketch866=s(d.getString("sketch"));if(privateRoleRound866!=roundId){privateRoleRound866=roundId;privateRole866="";privateSecret866="";}}else if("werewolf".equals(type)||"spy".equals(type)){drawerUid866="";drawerName866="";drawSketch866="";if(privateRoleRound866!=roundId){privateRoleRound866=roundId;privateRole866="";privateSecret866="";fetchPrivateRole866();} }else resetSocialState866();}
        if(priorRound9954!=roundId||!"active".equals(status)){
            clearScheduledSettlement9954();
            autoSettlementInFlight9954=false;
            autoRetryRound9954=roundId;
            autoRetryFailures9954=0;
            lastAutoSettlementAttempt9953=-1L;
        }
        renderState();updatePhase950();listenMoves();renderControls();});}
    private void listenMoves(){
        if(!KingPartyMoveSubscription9979.needsNewListener(movesListener!=null,
              observedMoveRound9979,observedMoveType9979,roundId,type)){
            // Snapshot was just a server timestamp/metadata/result change.
            // Keep the existing Firebase stream and the moves already shown
            // to the other phone; renderState()/renderControls() still run.
            return;
        }
        if(movesListener!=null){movesListener.remove();movesListener=null;}
        observedMoveRound9979=-1L;observedMoveType9979="";
        currentMoves.clear();
        // The previous listener downloaded ALL historic departed players'
        // scores on EVERY phone, even after version 9.9.76 preserved those
        // scores intentionally. Keep only this SERVER round's real moves.
        if(roundId<=0){renderMoves();return;}
        final long observingRound9977=roundId;
        observedMoveRound9979=observingRound9977;
        observedMoveType9979=type;
        movesListener=room().collection("game_moves")
            .whereEqualTo("roundId",observingRound9977)
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(q,e)->{
                // A delayed callback from a removed earlier-round listener
                // must never repaint or auto-settle a newly started game.
                if(observingRound9977!=roundId||!roleSessionCurrent10046(me.getUid(),roomId)
                   ||isFinishing()||isDestroyed())return;
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
                else maybeAutoFinish9953();
            });
    }
    private void updatePhase950(){
        if(phaseHint950==null)return;
        String game=type==null||type.isEmpty()?"Room game":type.replace('_',' ').toUpperCase(java.util.Locale.US);
        if("active".equals(status))phaseHint950.setText("● LIVE  •  "+game+"\nMoves sync instantly with Party room players");
        else if("finished".equals(status))phaseHint950.setText("🏁 ROUND FINISHED  •  "+game+"\nReview the result or start the next round");
        else if("closed".equals(status)||type==null||type.isEmpty())phaseHint950.setText("Choose a room game • players can Ready before the host starts");
        else phaseHint950.setText(game+"  •  "+status.toUpperCase(java.util.Locale.US));
    }

    private void showGameHistory940(){
        if(db==null||roomId.isEmpty()){toast("Open from a live Party room");return;}
        room().collection("events").orderBy("createdAt",com.google.firebase.firestore.Query.Direction.DESCENDING).limit(120).get().addOnSuccessListener(snap->{
            java.util.List<String> rows=new java.util.ArrayList<>();
            for(DocumentSnapshot d:snap.getDocuments()){
                String type=s(d.getString("type")),text=s(d.getString("text"));
                if(!"play".equals(type)||text.isEmpty())continue;
                if(text.startsWith("🏁")||text.contains(" wins")||text.contains(" Draw")||text.contains(" ended ")){rows.add(text);if(rows.size()>=20)break;}
            }
            if(rows.isEmpty()&&!result.isEmpty())rows.add("🏁 "+result.replace("\n"," • "));
            if(rows.isEmpty()){new AlertDialog.Builder(this).setTitle("📜 Game History").setMessage("No finished Party game rounds yet.").setPositiveButton("OK",null).show();return;}
            new AlertDialog.Builder(this).setTitle("📜 Recent game results").setItems(rows.toArray(new String[0]),(d,w)->new AlertDialog.Builder(this).setTitle("Round result").setMessage(rows.get(w).replace(" • ","\n")).setPositiveButton("OK",null).show()).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast("Game history unavailable: "+e.getMessage()));
    }

    private void renderState(){
        if(roundId>0&&"ttt".equals(type)&&!"active".equals(status)&&!result.isEmpty()){
            // A final X/O board must remain visible, not disappear when the
            // SERVER changes the round to closed after win/draw.
            stateText.setText("❌⭕ Final board\n"+tttBoardText864()+"\n"+result);
            return;
        }
        if(roundId==0||!"active".equals(status)){stateText.setText(result.isEmpty()?"No active multiplayer round":"Last result\n"+result);return;}if("ttt".equals(type)){String turn=tttTurnUid864.equals(tttXUid864)?tttXName864:tttTurnUid864.equals(tttOUid864)?tttOName864:"Player";stateText.setText("❌⭕  "+tttXName864+" vs "+tttOName864+"\nTurn: "+turn+"\n"+tttBoardText864());return;}stateText.setText(gameIcon(type)+"  "+prompt+(result.isEmpty()?"":"\n"+result));}
    private String gameIcon(String t){if("rps".equals(t))return "✊";if("dice".equals(t))return "🎲";if("number".equals(t))return "🔢";if("coin".equals(t))return "🪙";if("wheel".equals(t))return "🎡";if("bingo".equals(t))return "🎯";if("slot".equals(t))return "🎰";if("reaction".equals(t))return "⚡";if("highlow".equals(t))return "🃏";if("sheep".equals(t))return "🐑";if("zoo".equals(t))return "🦁";if("memory".equals(t))return "🧠";if("domino".equals(t))return "🁣";if("treasure".equals(t))return "💎";if("werewolf".equals(t))return "🐺";if("spy".equals(t))return "🕵";if("draw".equals(t))return "🎨";if("ttt".equals(t))return "❌⭕";return "🎮";}

    private boolean hasMyMove(){if(me==null)return false;for(DocumentSnapshot d:currentMoves)if(me.getUid().equals(d.getString("uid")))return true;return false;}

    private void renderControls(){
        if(!"sheep".equals(type)||!"active".equals(status)||sheepRound9925!=roundId){
            sheepClock9925.removeCallbacks(sheepTick9925);
        }
        controls.removeAllViews();
        // REAL multiplayer 5x5 Bingo, not legacy one-tap Pick Number 1..9.
        Button realBingo9980=button("🎱 LIVE BINGO 5×5 • Real Party Match");
        realBingo9980.setOnClickListener(v->{
            android.content.Intent play=new android.content.Intent(
                this, KingLiveBingo9980Activity.class);
            play.putExtra("roomId",roomId);play.putExtra("roomName",roomName);
            startActivity(play);
        });
        controls.addView(realBingo9980,new LinearLayout.LayoutParams(-1,dp(52)));

        Button ready=button("✅ Ready / Not Ready");ready.setOnClickListener(v->toggleReady750());controls.addView(ready,new LinearLayout.LayoutParams(-1,dp(50)));
        if(moderator&&"active".equals(status)){
            Button cancel9947=button("🚫 Cancel stuck round • no winner");
            cancel9947.setOnClickListener(v->confirmCancelRound9947());
            LinearLayout.LayoutParams clp9947=new LinearLayout.LayoutParams(-1,dp(50));
            clp9947.setMargins(0,dp(6),0,dp(6));
            controls.addView(cancel9947,clp9947);
        }
        Button ludo=button("🎲 Online Ludo • Party Room");ludo.setOnClickListener(v->openPartyLudo862());LinearLayout.LayoutParams ludoLp=new LinearLayout.LayoutParams(-1,dp(52));ludoLp.setMargins(0,dp(6),0,dp(4));controls.addView(ludo,ludoLp);
        Button chess9917=button("♟ Live Chess • Two Party Players");
        chess9917.setOnClickListener(v->{
            if(!gameMemberVerified992||db==null||me==null||roomId.isEmpty()){toast("Join the Party to play Chess");return;}
            Intent i9917=new Intent(this,KingLiveChessActivity.class);
            i9917.putExtra("roomId",roomId);startActivity(i9917);
        });
        LinearLayout.LayoutParams chessLp9917=new LinearLayout.LayoutParams(-1,dp(52));
        chessLp9917.setMargins(0,dp(4),0,dp(4));controls.addView(chess9917,chessLp9917);
        Button flying9923=button("🔪 Flying Knife Arena • LIVE Party Score");
        flying9923.setOnClickListener(v->{
            if(!gameMemberVerified992||db==null||me==null||roomId.isEmpty()){
                toast("Join the Party to play Flying Knife");return;
            }
            Intent knife9923=new Intent(this,KingKnifeArena9923Activity.class);
            knife9923.putExtra("roomId",roomId);
            startActivity(knife9923);
        });
        LinearLayout.LayoutParams flp9923=new LinearLayout.LayoutParams(-1,dp(52));
        flp9923.setMargins(0,dp(4),0,dp(4));controls.addView(flying9923,flp9923);
        TextView allGames=tv("All playable Party games",15,Color.WHITE,true);allGames.setPadding(dp(2),dp(10),0,dp(5));controls.addView(allGames);
        String[] localCodes={"tic_tac_toe","rps","dice","guess","slot","coin","memory","reaction","highlow","wheel","sheep","werewolf","spy","draw","bingo","zoo","domino","treasure"};
        String[] localNames={"❌⭕ Tic Tac Toe","✊ RPS","🎲 Dice","🔢 Guess","🎰 Slot","🪙 Coin","🧠 Memory","⚡ Reaction","🃏 High/Low","🎡 Wheel","🐑 Sheep","🐺 Werewolf","🕵 Spy","🎨 Draw & Guess","🎯 Pick 1–9","🦁 Crazy Zoo","🁣 Domino","💎 Treasure Hunt"};
        for(int base=0;base<localCodes.length;base+=3){
            LinearLayout row=new LinearLayout(this);
            for(int j=0;j<3&&base+j<localCodes.length;j++){int k=base+j;final String code=localCodes[k];Button g=button(localNames[k]);g.setTextSize(11);g.setOnClickListener(v->openPlayableRoomGame(code));LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(0,dp(50),1);gp.setMargins(dp(2),dp(3),dp(2),dp(3));row.addView(g,gp);}
            controls.addView(row);
        }
        if(moderator){
            TextView h=tv("Start a synchronized room round",15,Color.WHITE,true);controls.addView(h);
            String[] n={"❌⭕ Tic Tac Toe","✊ RPS","🎲 Dice Duel","🔢 Pick Number","🪙 Coin Pick","🎡 Lucky Wheel","🎯 Pick 1–9","🎰 Slot Clash","⚡ Reaction Tap","🃏 High / Low","🐑 Live Sheep Sprint","🦁 Crazy Zoo","🧠 Memory Duel","🁣 Domino Match","🐺 Werewolf","🕵 Spy Game","🎨 Draw & Guess","💎 Treasure Hunt"};String[] t={"ttt","rps","dice","number","coin","wheel","bingo","slot","reaction","highlow","sheep","zoo","memory","domino","werewolf","spy","draw","treasure"};
            for(int base=0;base<n.length;base+=3){LinearLayout row=new LinearLayout(this);for(int j=0;j<3&&base+j<n.length;j++){int i=base+j;final String gt=t[i];Button b=button(n[i]);b.setEnabled(!"active".equals(status));b.setOnClickListener(v->startRound(gt));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(52),1);lp.setMargins(dp(2),dp(3),dp(2),dp(3));row.addView(b,lp);}controls.addView(row);}
            if(roundId>0&&!"active".equals(status)){LinearLayout rr=new LinearLayout(this);Button rem=button("↻ Rematch");rem.setOnClickListener(v->rematch750());rr.addView(rem,new LinearLayout.LayoutParams(0,dp(50),1));Button reset=button("Clear Ready");reset.setOnClickListener(v->clearReady750());LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,dp(50),1);rp.setMargins(dp(6),0,0,0);rr.addView(reset,rp);controls.addView(rr);}
        }
        if(roundId>0&&"active".equals(status)){
            if("ttt".equals(type)){tttControls864();}
            else if("werewolf".equals(type)||"spy".equals(type)){hiddenRoleControls866();}
            else if("draw".equals(type)){drawGuessControls866();}
            else{
                TextView h=tv(hasMyMove()?"Your move is locked ✓":"Your move",15,Color.WHITE,true);controls.addView(h);
                if(KingPartyRoundRoster9972.isOrdinaryGame(type)
                    &&KingPartyRoundRoster9972.valid(frozenRoster9947)
                    &&(me==null||!frozenRoster9947.contains(me.getUid()))){
                    controls.addView(tv("👀 Spectating this round • tap Ready for the next match",13,MUTED,false));
                }else if(!hasMyMove()){if("treasure".equals(type))treasureControls10100();else if("rps".equals(type))rpsControls();else if("dice".equals(type))diceControls();else if("number".equals(type))numberControls();else if("coin".equals(type))coinControls();else if("wheel".equals(type))wheelControls();else if("bingo".equals(type))bingoControls();else if("slot".equals(type))slotControls863();else if("reaction".equals(type))reactionControls863();else if("highlow".equals(type))highLowControls863();else if("sheep".equals(type))sheepControls863();else if("zoo".equals(type))zooControls863();else if("memory".equals(type))memoryControls865();else if("domino".equals(type))dominoControls865();}
                else controls.addView(tv("Submitted for this round • wait for the host/co-host to finish",13,MUTED,false));
                if(moderator){
                    java.util.ArrayList<String> verifiedMoves9973=new java.util.ArrayList<>();
                    for(DocumentSnapshot move9973:currentMoves){
                        if(type.equals(s(move9973.getString("type")))
                           &&move9973.getId().equals(s(move9973.getString("uid")))
                           &&!move9973.getMetadata().hasPendingWrites())
                            verifiedMoves9973.add(move9973.getId());
                    }
                    boolean rosterLive9975=membersSeen9971&&
                        KingPartyLiveRoster9975.allPresent(
                            frozenRoster9947,memberNames9971.keySet());
                    boolean finished9973=KingPartyAllPlayed9973.complete(type,
                        frozenRoster9947,verifiedMoves9973);
                    Button finish=button(!rosterLive9975&&membersSeen9971?
                        "🚫 Player left • Cancel round (no winner)":
                        finished9973?
                        "🏁 All players finished • show result":
                        "⏳ Waiting for every Ready player's move");
                    finish.setEnabled(finished9973&&rosterLive9975&&gameStateVerified9943);
                    finish.setOnClickListener(v->finishRound());
                    LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,dp(54));
                    fp.setMargins(0,dp(8),0,0);controls.addView(finish,fp);
                }
            }
        }
    }
    private String normalizeRoomGame940(String code){
        String g=code==null?"":code.toLowerCase(java.util.Locale.US).trim();
        if("tic_tac_toe".equals(g)||"tic tac toe".equals(g))return "ttt";
        if("guess".equals(g)||"guess number".equals(g))return "number";
        if(g.contains("ludo"))return "ludo";
        if(g.contains("treasure")||g.contains("hunt"))return "treasure";
        if(g.contains("werewolf"))return "werewolf";
        if(g.contains("spy"))return "spy";
        if(g.contains("draw"))return "draw";
        if(g.contains("bingo"))return "bingo";
        if(g.contains("domino"))return "domino";
        if(g.contains("sheep"))return "sheep";
        if(g.contains("zoo"))return "zoo";
        if(g.contains("memory"))return "memory";
        if(g.contains("reaction"))return "reaction";
        if(g.contains("high"))return "highlow";
        if(g.contains("wheel"))return "wheel";
        if(g.contains("slot"))return "slot";
        if(g.contains("coin"))return "coin";
        if(g.contains("rock")||g.contains("rps"))return "rps";
        if(g.contains("dice"))return "dice";
        if(g.contains("number"))return "number";
        return g;
    }
    private void startRequestedGame940(String code){
        String gt=normalizeRoomGame940(code);
        if("ludo".equals(gt)){openPartyLudo862();return;}
        if(!moderator){toast("Tap Ready • host/co-host starts "+code+" for the room");return;}
        startRound(gt);
    }
    private void openPlayableRoomGame(String code){startRequestedGame940(code);}
    private void openPartyLudo862(){
        if(roomId.isEmpty()){toast("Open Ludo from a live Party room");return;}
        String code=partyLudoCode862(roomId);
        Intent i=new Intent(this,OnlineLudoActivity.class);
        i.putExtra("ludoCode",code);
        i.putExtra("partyRoomMode",true);
        i.putExtra("partyRoomHost",moderator);
        i.putExtra("partyRoomId",roomId);
        i.putExtra("partyRoomName",roomName);
        startActivity(i);
    }
    private String partyLudoCode862(String seed){
        final String chars="ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        try{
            byte[] d=java.security.MessageDigest.getInstance("SHA-256").digest(seed.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder b=new StringBuilder(6);
            for(int i=0;i<6;i++)b.append(chars.charAt((d[i]&0xff)%chars.length()));
            return b.toString();
        }catch(Exception ignored){
            long h=seed.hashCode()&0xffffffffL;StringBuilder b=new StringBuilder(6);
            for(int i=0;i<6;i++){b.append(chars.charAt((int)(h%chars.length())));h=(h*1103515245L+12345L)&0xffffffffL;}
            return b.toString();
        }
    }

    private void resetTttState864(){tttBoard864=".........";tttTurnUid864="";tttXUid864="";tttOUid864="";tttXName864="X";tttOName864="O";tttProcessing864=false;}
    private String tttBoardText864(){String b=tttBoard864==null||tttBoard864.length()!=9?".........":tttBoard864;StringBuilder o=new StringBuilder();for(int i=0;i<9;i++){char c=b.charAt(i);o.append(c=='.'?'·':c);if(i%3==2&&i<8)o.append("\n");else if(i%3!=2)o.append("   ");}return o.toString();}
    private boolean tttWin864(String b,char p){if(b==null||b.length()!=9)return false;int[][]w={{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};for(int[]a:w)if(b.charAt(a[0])==p&&b.charAt(a[1])==p&&b.charAt(a[2])==p)return true;return false;}
    private boolean tttFull864(String b){return b!=null&&b.length()==9&&b.indexOf('.')<0;}
    private void tttControls864(){
        String uid=me==null?"":me.getUid();String symbol=uid.equals(tttXUid864)?"X":uid.equals(tttOUid864)?"O":"";boolean myTurn=uid.equals(tttTurnUid864)&&!symbol.isEmpty();
        TextView info=tv(symbol.isEmpty()?"Spectating • "+tttXName864+" (X) vs "+tttOName864+" (O)":("You are "+symbol+(myTurn?" • Your turn":" • Wait for opponent")),14,myTurn?GOLD:MUTED,true);controls.addView(info);
        String b=tttBoard864==null||tttBoard864.length()!=9?".........":tttBoard864;
        for(int r=0;r<3;r++){LinearLayout row=new LinearLayout(this);for(int c=0;c<3;c++){final int idx=r*3+c;char cell=b.charAt(idx);Button v=button(cell=='.'?"":String.valueOf(cell));v.setTextSize(28);v.setEnabled(myTurn&&cell=='.'&&!hasMyMove());v.setOnClickListener(x->submitTtt864(idx));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(68),1);lp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(v,lp);}controls.addView(row);}
        if(hasMyMove())controls.addView(tv("Move sent • waiting for room host to confirm",13,MUTED,false));
        if(moderator&&hasPendingTttMove9951()){
            Button retry9951=button("↻ Confirm pending Tic Tac Toe turn");
            retry9951.setOnClickListener(v->processTttMove864());
            controls.addView(retry9951,new LinearLayout.LayoutParams(-1,dp(48)));
        }
    }
    private boolean hasPendingTttMove9951(){
        for(DocumentSnapshot d:currentMoves)
            if(tttTurnUid864.equals(s(d.getString("uid")))
               &&s(d.getString("move")).startsWith("cell:"))return true;
        return false;
    }
    private void startTtt864(){
        if(!moderator||!gameMemberVerified992||!gameStateVerified9943
           ||!"closed".equals(status)||me==null||db==null){
            toast("Wait for a live Host and a finished previous round");return;
        }
        final long previous9951=roundId;
        room().collection("game_ready")
           .whereEqualTo("ready",true)
           .get(com.google.firebase.firestore.Source.SERVER)
           .addOnSuccessListener(snapshot9951->{
            java.util.List<DocumentSnapshot> ready9951=new ArrayList<>(snapshot9951.getDocuments());
            Collections.sort(ready9951,(a,b)->
                s(a.getString("name")).compareToIgnoreCase(s(b.getString("name"))));
            if(ready9951.size()<2){toast("Two Party players must tap Ready");return;}
            if(ready9951.size()>50){toast("Too many Ready players");return;}
            final long next9951=Math.max(System.currentTimeMillis(),previous9951+1);
            // Firestore retries this transaction if either Ready or membership
            // changes. Never start against an offline/removed ghost player.
            db.runTransaction(tx->{
                DocumentSnapshot st9951=tx.get(state());
                Long stored9951=st9951.exists()?st9951.getLong("roundId"):null;
                if(st9951.exists()&&("active".equals(st9951.getString("status"))
                    ||stored9951==null||stored9951.longValue()!=previous9951))
                    throw new IllegalStateException("Party round changed");
                java.util.ArrayList<DocumentSnapshot> active9951=new java.util.ArrayList<>();
                for(DocumentSnapshot displayedReady9951:ready9951){
                    String uid9951=s(displayedReady9951.getId());
                    if(uid9951.isEmpty())continue;
                    DocumentSnapshot freshReady9951=tx.get(
                        room().collection("game_ready").document(uid9951));
                    DocumentSnapshot freshMember9951=tx.get(
                        room().collection("members").document(uid9951));
                    if(KingTttLiveRoster9951.valid(uid9951,
                        freshReady9951.exists(),
                        s(freshReady9951.getString("uid")),
                        freshReady9951.getBoolean("ready"),
                        freshMember9951.exists(),
                        s(freshMember9951.getString("uid"))))
                        active9951.add(freshReady9951);
                    // Collect ALL currently Ready Party members; not just
                    // the first two names alphabetically.
                }
                if(active9951.size()<2)
                    throw new IllegalStateException(
                        "Only live Ready Party members can start; ask two members to tap Ready");
                // SecureRandom chooses two distinct real Ready members from
                // the entire live pool. The same pair is locked to this round
                // and all other members remain spectators.
                int[] pair9960=KingTttFairRoster9960.pickPair(
                    active9951.size(),new java.security.SecureRandom());
                DocumentSnapshot x9951=active9951.get(pair9960[0]),
                    o9951=active9951.get(pair9960[1]);
                String xu9951=x9951.getId(),ou9951=o9951.getId();
                String xn9951=s(x9951.getString("name")),on9951=s(o9951.getString("name"));
                if(xn9951.isEmpty())xn9951="Player X";
                if(on9951.isEmpty())on9951="Player O";
                Map<String,Object> value9951=new HashMap<>();
                value9951.put("actorUid",me.getUid());
                value9951.put("actorName",displayName);
                value9951.put("type","ttt");
                value9951.put("prompt",xn9951+" (X) vs "+on9951+" (O)");
                value9951.put("result","");
                value9951.put("status","active");
                value9951.put("roundId",next9951);
                value9951.put("board",".........");
                value9951.put("turnUid",xu9951);
                value9951.put("xUid",xu9951);
                value9951.put("oUid",ou9951);
                value9951.put("xName",xn9951);
                value9951.put("oName",on9951);
                value9951.put("participantUids",
                    java.util.Arrays.asList(xu9951,ou9951));
                value9951.put("updatedAt",FieldValue.serverTimestamp());
                // Clear ONLY the selected players' old move slots as part of
                // the same atomic round start, not a stale cached query.
                tx.delete(room().collection("game_moves").document(xu9951));
                tx.delete(room().collection("game_moves").document(ou9951));
                tx.set(state(),value9951);
                return xn9951+" (X) vs "+on9951+" (O)";
            }).addOnSuccessListener(names9951->
                postGameEvent750("❌⭕ "+displayName+
                    " started live Tic Tac Toe: "+names9951))
              .addOnFailureListener(e->toast("Tic Tac Toe not started: "+e.getMessage()));
        }).addOnFailureListener(e->toast("Live Ready players unavailable: "+e.getMessage()));
    }
    private void submitTtt864(int idx){
        if(me==null||db==null||!gameMemberVerified992||!gameStateVerified9943||
            !"ttt".equals(type)||!"active".equals(status)||idx<0||idx>8){
            toast("Reconnecting — wait for the live Party turn");return;}if(!me.getUid().equals(tttTurnUid864)){toast("Wait for your turn");return;}String b=tttBoard864==null?"":tttBoard864;if(b.length()!=9||b.charAt(idx)!='.'){toast("Choose an empty square");return;}final long expected=roundId;DocumentReference mine=room().collection("game_moves").document(me.getUid());db.runTransaction(tx->{DocumentSnapshot active=tx.get(state()),existing=tx.get(mine);Long r=active.getLong("roundId");String board=s(active.getString("board"));if(!active.exists()||r==null||r!=expected||!"active".equals(active.getString("status"))||!"ttt".equals(active.getString("type"))||!me.getUid().equals(active.getString("turnUid"))||board.length()!=9||board.charAt(idx)!='.')throw new IllegalStateException("Turn changed");if(existing.exists())throw new IllegalStateException("Previous move is still pending");Map<String,Object>d=new HashMap<>();d.put("uid",me.getUid());d.put("name",displayName.length()>80?displayName.substring(0,80):displayName);d.put("roundId",expected);d.put("type","ttt");d.put("move","cell:"+idx);d.put("score",idx);d.put("updatedAt",FieldValue.serverTimestamp());tx.set(mine,d);return null;}).addOnSuccessListener(v->toast("Move sent ✓")).addOnFailureListener(e->toast("Move failed: "+e.getMessage()));
    }
    private void processTttMove864(){
        if(tttProcessing864||!moderator||!"ttt".equals(type)||!"active".equals(status)||tttTurnUid864.isEmpty())return;DocumentSnapshot pick=null;for(DocumentSnapshot d:currentMoves){if(tttTurnUid864.equals(s(d.getString("uid")))&&s(d.getString("move")).startsWith("cell:")){pick=d;break;}}if(pick==null)return;final DocumentReference moveRef=pick.getReference();final long expected=roundId;tttProcessing864=true;db.runTransaction(tx->{DocumentSnapshot st=tx.get(state()),mv=tx.get(moveRef);if(!st.exists()||!mv.exists())throw new IllegalStateException("Move disappeared");Long r=st.getLong("roundId");String uid=s(mv.getString("uid")),turn=s(st.getString("turnUid")),board=s(st.getString("board")),move=s(mv.getString("move"));if(r==null||r!=expected||!"active".equals(st.getString("status"))||!"ttt".equals(st.getString("type"))||!uid.equals(turn)||board.length()!=9||!move.startsWith("cell:"))throw new IllegalStateException("Stale move");int idx;try{idx=Integer.parseInt(move.substring(5));}catch(Exception e){throw new IllegalStateException("Bad move");}if(idx<0||idx>8||board.charAt(idx)!='.')throw new IllegalStateException("Square unavailable");String xu=s(st.getString("xUid")),ou=s(st.getString("oUid")),xn=s(st.getString("xName")),on=s(st.getString("oName"));char mark=uid.equals(xu)?'X':uid.equals(ou)?'O':'?';if(mark=='?')throw new IllegalStateException("Not a player");char[] a=board.toCharArray();a[idx]=mark;String nb=new String(a);boolean win=tttWin864(nb,mark),draw=!win&&tttFull864(nb);Map<String,Object> u=new HashMap<>();u.put("board",nb);u.put("turnUid",win||draw?"":(mark=='X'?ou:xu));u.put("status",win||draw?"closed":"active");u.put("result",win?("🏆 "+(mark=='X'?xn:on)+" wins Tic Tac Toe"):draw?"🤝 Tic Tac Toe draw":"");u.put("prompt",xn+" (X) vs "+on+" (O)");u.put("actorUid",me.getUid());u.put("updatedAt",FieldValue.serverTimestamp());tx.update(state(),u);tx.delete(moveRef);return null;}).addOnSuccessListener(v->{tttProcessing864=false;}).addOnFailureListener(e->{
            // Do NOT silently delete the real player's valid move when a Host
            // momentarily loses connection. The Host can retry in this room.
            tttProcessing864=false;
            toast("Turn not confirmed yet • Host can tap Retry");
        });
    }

    private void treasureControls10100(){TextView hint=tv("💎 Find the hidden chest • one pick per Ready player • no paid reward",13,MUTED,true);controls.addView(hint);LinearLayout row=new LinearLayout(this);for(int chest=1;chest<=3;chest++){final int pick=chest;Button b=button("🧰 Chest "+chest);b.setOnClickListener(v->submit("Chest "+pick,0));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(54),1);lp.setMargins(dp(3),0,dp(3),0);row.addView(b,lp);}controls.addView(row);}
    private void rpsControls(){LinearLayout row=new LinearLayout(this);String[] m={"Rock","Paper","Scissors"},e={"✊","✋","✌"};for(int i=0;i<3;i++){final String x=m[i];Button b=button(e[i]+" "+x);b.setOnClickListener(v->submit(x,0));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(52),1);lp.setMargins(dp(2),0,dp(2),0);row.addView(b,lp);}controls.addView(row);}
    private void diceControls(){Button b=button("🎲 Roll my dice");b.setOnClickListener(v->{int n=KingPartyGameFairPlay9949.dice();submit("Rolled "+n,n);});controls.addView(b,new LinearLayout.LayoutParams(-1,dp(54)));}
    private void numberControls(){LinearLayout row=new LinearLayout(this);EditText in=new EditText(this);in.setHint("1–30");in.setTextColor(Color.WHITE);in.setHintTextColor(0xff9e94ad);in.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);in.setSingleLine(true);in.setBackground(bg(CARD,14));in.setPadding(dp(14),0,dp(14),0);row.addView(in,new LinearLayout.LayoutParams(0,dp(52),1));Button b=button("Submit");b.setOnClickListener(v->{try{int n=Integer.parseInt(in.getText().toString().trim());if(n<1||n>30){toast("Choose 1–30");return;}submit(String.valueOf(n),n);}catch(Exception e){toast("Choose 1–30");}});LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(dp(100),dp(52));bp.setMargins(dp(8),0,0,0);row.addView(b,bp);controls.addView(row);}
    private void coinControls(){LinearLayout row=new LinearLayout(this);Button h=button("🪙 Heads");h.setOnClickListener(v->submit("Heads",1));Button t=button("🪙 Tails");t.setOnClickListener(v->submit("Tails",0));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(52),1);lp.setMargins(dp(2),0,dp(2),0);row.addView(h,lp);row.addView(t,lp);controls.addView(row);}
    private void wheelControls(){Button b=button("🎡 Spin my wheel");b.setOnClickListener(v->{int n=KingPartyGameFairPlay9949.wheel();submit("Spin "+n,n);});controls.addView(b,new LinearLayout.LayoutParams(-1,dp(54)));}
    private void bingoControls(){LinearLayout row=new LinearLayout(this);for(int n=1;n<=9;n++){if(n==4||n==7){controls.addView(row);row=new LinearLayout(this);}final int x=n;Button b=button(String.valueOf(n));b.setOnClickListener(v->submit("Pick "+x,x));row.addView(b,new LinearLayout.LayoutParams(0,dp(48),1));}controls.addView(row);}
    private void slotControls863(){Button b=button("🎰 Spin once");b.setOnClickListener(v->{int a=KingPartyGameFairPlay9949.slotDigit(),c=KingPartyGameFairPlay9949.slotDigit(),d=KingPartyGameFairPlay9949.slotDigit();int score=a+c+d+(a==c&&c==d?50:(a==c||c==d||a==d?20:0));submit("Spin "+a+"-"+c+"-"+d,score);});controls.addView(b,new LinearLayout.LayoutParams(-1,dp(54)));}
    // Never count a reaction until a delayed GO. The phase survives
    // Firebase snapshots that recreate controls for the same live round.
    private final android.os.Handler reactionClock9927=new android.os.Handler(android.os.Looper.getMainLooper());
    private final java.security.SecureRandom reactionRng9927=new java.security.SecureRandom();
    private long reactionRound9927=-1L,reactionGreenAt9927=0L;
    private int reactionPhase9927=0; // ready, waiting, GO, finished
    private Button reactionButton9927;
    private TextView reactionText9927;
    private final Runnable reactionGo9927=new Runnable(){
        @Override public void run(){
            if(isFinishing()||isDestroyed()||!"reaction".equals(type)
                ||!"active".equals(status)||roundId!=reactionRound9927
                ||reactionPhase9927!=1)return;
            reactionGreenAt9927=android.os.SystemClock.elapsedRealtime();
            reactionPhase9927=2;
            if(reactionButton9927!=null){
                reactionButton9927.setText("🟢 GO! TAP NOW");
                reactionButton9927.setBackground(bg(0xff087e4e,16));
            }
            if(reactionText9927!=null)reactionText9927.setText("🟢 GO! Tap once. Fastest real reaction wins.");
        }
    };
    private void reactionControls863(){
        if(reactionRound9927!=roundId){
            reactionClock9927.removeCallbacks(reactionGo9927);
            reactionRound9927=roundId;reactionGreenAt9927=0L;reactionPhase9927=0;
        }
        reactionText9927=tv("⚡ Wait for GREEN, then tap quickly!\nEarly taps count as FALSE START.",14,GOLD,true);
        reactionText9927.setGravity(Gravity.CENTER);
        controls.addView(reactionText9927,new LinearLayout.LayoutParams(-1,dp(67)));
        reactionButton9927=button("▶ START REACTION");reactionButton9927.setTextSize(18);
        reactionButton9927.setOnClickListener(v->{
            if(!gameMemberVerified992||!"reaction".equals(type)||!"active".equals(status)
                ||reactionRound9927!=roundId)return;
            long now=android.os.SystemClock.elapsedRealtime();
            if(reactionPhase9927==0){
                reactionPhase9927=1;reactionText9927.setText("🟣 WAIT… do not tap yet!");
                reactionButton9927.setText("✋ WAIT FOR GREEN");
                reactionButton9927.setBackground(bg(0xff964080,16));
                reactionClock9927.removeCallbacks(reactionGo9927);
                reactionClock9927.postDelayed(reactionGo9927,1500+reactionRng9927.nextInt(2501));
            }else if(reactionPhase9927==1){
                reactionPhase9927=3;reactionClock9927.removeCallbacks(reactionGo9927);
                reactionText9927.setText("❌ FALSE START • tapped before GO");
                reactionButton9927.setEnabled(false);
                submit("False start before GO",KingReactionRules9927.FALSE_START);
            }else if(reactionPhase9927==2){
                reactionPhase9927=3;reactionClock9927.removeCallbacks(reactionGo9927);
                reactionButton9927.setEnabled(false);
                long elapsed=Math.max(1,now-reactionGreenAt9927);
                if(elapsed>KingReactionRules9927.MAX_VALID_MS){
                    reactionText9927.setText("⏱ Too late • time limit exceeded");
                    submit("Reaction expired",0);return;
                }
                int score=KingReactionRules9927.score(elapsed);
                reactionText9927.setText("⚡ "+elapsed+" ms • submitted to Party players");
                submit("Reaction "+elapsed+" ms",score);
            }
        });
        controls.addView(reactionButton9927,new LinearLayout.LayoutParams(-1,dp(64)));
        if(reactionPhase9927==1){
            reactionButton9927.setText("✋ WAIT FOR GREEN");
            reactionText9927.setText("🟣 WAIT… do not tap yet!");
        }else if(reactionPhase9927==2){
            reactionButton9927.setText("🟢 GO! TAP NOW");
            reactionButton9927.setBackground(bg(0xff087e4e,16));
            reactionText9927.setText("🟢 GO! Tap once.");
        }else if(reactionPhase9927==3){
            reactionButton9927.setEnabled(false);
            reactionButton9927.setText("✓ Reaction submitted for this round");
        }
    }
    private void highLowControls863(){LinearLayout row=new LinearLayout(this);Button h=button("⬆ High 51–100");Button l=button("⬇ Low 1–50");h.setOnClickListener(v->submit("High",1));l.setOnClickListener(v->submit("Low",0));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(54),1);lp.setMargins(dp(2),0,dp(2),0);row.addView(h,lp);row.addView(l,lp);controls.addView(row);}
    // v9.9.25: A real timed tap-to-run Sheep Battle. The old implementation
    // simply used Math.random() and claimed the sheep had raced. Players now
    // actually race for eight seconds before submitting their measured taps
    // as the existing verified Firebase room game move for this round.
    private static final long SHEEP_RACE_MS_9925=8000L;
    private final android.os.Handler sheepClock9925=new android.os.Handler(android.os.Looper.getMainLooper());
    private long sheepRound9925=-1L, sheepStart9925=0L;
    private int sheepTaps9925=0;
    private boolean sheepFinished9925=false;
    private boolean sheepStartPending9940=false;
    private TextView sheepLive9925;
    private Button sheepTap9925;
    private android.widget.ProgressBar sheepProgress9925;
    private final Runnable sheepTick9925=new Runnable(){
        @Override public void run(){
            if(isFinishing()||isDestroyed())return;
            if(!gameMemberVerified992||!"sheep".equals(type)||!"active".equals(status)||roundId!=sheepRound9925){
                sheepStart9925=0;sheepClock9925.removeCallbacks(this);return;
            }
            if(sheepStart9925<=0||sheepFinished9925)return;
            long elapsed=android.os.SystemClock.elapsedRealtime()-sheepStart9925;
            if(elapsed>=SHEEP_RACE_MS_9925){
                sheepFinished9925=true;
                if(sheepTap9925!=null)sheepTap9925.setEnabled(false);
                if(sheepLive9925!=null)sheepLive9925.setText("🏁 Finished! "+sheepTaps9925+" taps • submitting score to Party...");
                int score=KingSheepRace9925.score(sheepTaps9925,SHEEP_RACE_MS_9925);
                submit("Sheep sprint "+score+" taps in 8s",score);
                return;
            }
            updateSheepDisplay9925(elapsed);
            sheepClock9925.postDelayed(this,100);
        }
    };
    private void updateSheepDisplay9925(long elapsed){
        int left=(int)Math.max(0,(SHEEP_RACE_MS_9925-elapsed+999)/1000);
        if(sheepLive9925!=null)sheepLive9925.setText("🐑  "+sheepTaps9925+" strides   •   ⏱ "+left+"s remaining");
        if(sheepProgress9925!=null)sheepProgress9925.setProgress(KingSheepRace9925.progress(sheepTaps9925));
    }
    private void sheepControls863(){
        if(sheepRound9925!=roundId){
            sheepClock9925.removeCallbacks(sheepTick9925);
            sheepRound9925=roundId;sheepStart9925=0L;sheepTaps9925=0;sheepFinished9925=false;sheepStartPending9940=false;
        }
        TextView guide=tv("🐑 KING Sheep Sprint • 8-second Party challenge\nTap as fast as you can. Highest real tap count wins!",14,GOLD,true);
        guide.setGravity(Gravity.CENTER);guide.setBackground(bg(CARD,16));
        controls.addView(guide,new LinearLayout.LayoutParams(-1,dp(74)));
        sheepLive9925=tv("Ready • press START",16,Color.WHITE,true);sheepLive9925.setGravity(Gravity.CENTER);
        controls.addView(sheepLive9925,new LinearLayout.LayoutParams(-1,dp(52)));
        sheepProgress9925=new android.widget.ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        sheepProgress9925.setMax(100);sheepProgress9925.setProgress(KingSheepRace9925.progress(sheepTaps9925));
        LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,dp(16));pp.setMargins(0,dp(4),0,dp(8));controls.addView(sheepProgress9925,pp);
        sheepTap9925=button(sheepStart9925==0?"▶ START SHEEP RACE":"🐑 TAP • RUN FASTER!");
        sheepTap9925.setTextSize(18);sheepTap9925.setEnabled(!sheepFinished9925&&!sheepStartPending9940);
        sheepTap9925.setOnClickListener(v->{
            if(!gameMemberVerified992||!"sheep".equals(type)||!"active".equals(status)||sheepRound9925!=roundId)return;
            long now=android.os.SystemClock.elapsedRealtime();
            if(sheepStart9925==0){
                if(sheepStartPending9940||db==null||me==null)return;
                sheepStartPending9940=true;
                sheepTap9925.setEnabled(false);
                sheepLive9925.setText("⏳ Starting verified 8-second race on Party server…");
                final long startingRound9940=roundId;
                final String startUid9940=me.getUid();
                java.util.Map<String,Object> proof9940=new java.util.HashMap<>();
                proof9940.put("uid",startUid9940);
                proof9940.put("roundId",startingRound9940);
                proof9940.put("startedAt",FieldValue.serverTimestamp());
                room().collection("sheep_starts").document(startUid9940).set(proof9940)
                  .addOnSuccessListener(done->{
                    sheepStartPending9940=false;
                    if(isFinishing()||isDestroyed()||!gameMemberVerified992||
                       sheepRound9925!=startingRound9940||roundId!=startingRound9940||
                       !"sheep".equals(type)||!"active".equals(status))return;
                    sheepStart9925=android.os.SystemClock.elapsedRealtime();
                    sheepTaps9925=0;
                    if(sheepTap9925!=null){
                        sheepTap9925.setEnabled(true);
                        sheepTap9925.setText("🐑 TAP • RUN FASTER!");
                    }
                    sheepClock9925.removeCallbacks(sheepTick9925);
                    sheepClock9925.post(sheepTick9925);
                  }).addOnFailureListener(e->{
                    sheepStartPending9940=false;
                    if(isFinishing()||isDestroyed()||roundId!=startingRound9940)return;
                    if(sheepTap9925!=null){sheepTap9925.setEnabled(true);
                        sheepTap9925.setText("▶ RETRY START");}
                    if(sheepLive9925!=null)sheepLive9925.setText("Could not start room race: "+e.getMessage());
                  });
                return;
            }
            long elapsed=now-sheepStart9925;
            if(sheepFinished9925||elapsed>=SHEEP_RACE_MS_9925)return;
            sheepTaps9925=KingSheepRace9925.nextTap(sheepTaps9925);
            updateSheepDisplay9925(elapsed);
        });
        controls.addView(sheepTap9925,new LinearLayout.LayoutParams(-1,dp(64)));
        if(sheepStart9925>0&&!sheepFinished9925)updateSheepDisplay9925(android.os.SystemClock.elapsedRealtime()-sheepStart9925);
        if(sheepFinished9925)sheepLive9925.setText("✅ "+sheepTaps9925+" actual taps submitted • waiting for other racers");
    }
    private void zooControls863(){LinearLayout row=new LinearLayout(this);String[] animal={"🦁 Lion","🐯 Tiger","🐼 Panda"};for(String a:animal){final String choice=a;Button b=button(a);b.setOnClickListener(v->{int power=25+(int)(Math.random()*76);submit(choice+" power "+power,power);});row.addView(b,new LinearLayout.LayoutParams(0,dp(54),1));}controls.addView(row);}




    private void resetMemoryState865(String layout,long round){
        memoryLayout865=layout==null?"":layout;memoryRound865=round;memoryFirst865=-1;memoryPairs865=0;memoryAttempts865=0;memoryComparing865=false;memoryDone865=new boolean[8];memoryButtons865=new Button[8];memoryStart865=0;
    }
    private String memoryLayoutForRound865(long seed){
        List<Character>a=new ArrayList<>();for(char c='A';c<='D';c++){a.add(c);a.add(c);}
        java.util.Random r=new java.util.Random(seed^0x6b696e67706c7573L);Collections.shuffle(a,r);StringBuilder b=new StringBuilder(8);for(char c:a)b.append(c);return b.toString();
    }
    private void memoryControls865(){
        if(memoryLayout865==null||memoryLayout865.length()!=8){controls.addView(tv("Memory board unavailable • host can rematch",13,MUTED,false));return;}
        if(memoryStart865==0)memoryStart865=android.os.SystemClock.elapsedRealtime();
        controls.addView(tv("Find all 4 pairs • everyone gets the same shuffled board",13,MUTED,false));
        for(int r=0;r<2;r++){LinearLayout row=new LinearLayout(this);for(int c=0;c<4;c++){final int idx=r*4+c;Button b=button(memoryDone865[idx]?String.valueOf(memoryLayout865.charAt(idx)):"?");b.setTextSize(24);memoryButtons865[idx]=b;b.setEnabled(!memoryComparing865&&!memoryDone865[idx]&&!hasMyMove());b.setOnClickListener(v->memoryTap865(idx));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(66),1);lp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(b,lp);}controls.addView(row);}
        controls.addView(tv("Attempts: "+memoryAttempts865+"  •  Pairs: "+memoryPairs865+"/4",13,GOLD,true));
    }
    private void memoryTap865(int idx){
        if(memoryComparing865||idx<0||idx>=8||memoryDone865[idx]||hasMyMove()||memoryLayout865.length()!=8)return;Button b=memoryButtons865[idx];if(b==null)return;b.setText(String.valueOf(memoryLayout865.charAt(idx)));b.setEnabled(false);
        if(memoryFirst865<0){memoryFirst865=idx;return;}if(memoryFirst865==idx)return;final int first=memoryFirst865;memoryFirst865=-1;memoryAttempts865++;
        if(memoryLayout865.charAt(first)==memoryLayout865.charAt(idx)){memoryDone865[first]=memoryDone865[idx]=true;memoryPairs865++;if(memoryPairs865>=4){long ms=Math.max(1,android.os.SystemClock.elapsedRealtime()-memoryStart865);int score=(int)Math.max(1,100000-Math.min(85000,ms)-Math.min(12000,memoryAttempts865*600));submit("Cleared in "+memoryAttempts865+" attempts • "+ms+" ms",score);}return;}
        memoryComparing865=true;b.postDelayed(()->{try{if(!memoryDone865[first]&&memoryButtons865[first]!=null){memoryButtons865[first].setText("?");memoryButtons865[first].setEnabled(true);}if(!memoryDone865[idx]&&memoryButtons865[idx]!=null){memoryButtons865[idx].setText("?");memoryButtons865[idx].setEnabled(true);}}finally{memoryComparing865=false;}},650);
    }
    private void dominoControls865(){
        controls.addView(tv("Chain end: [ "+dominoTarget865+" ] • choose the tile whose LEFT side matches",14,GOLD,true));
        int correct=(int)(Math.abs(roundId)%6);for(int base=0;base<6;base+=3){LinearLayout row=new LinearLayout(this);for(int j=0;j<3;j++){final int i=base+j;int left=i==correct?dominoTarget865:(dominoTarget865+i+1)%7;int right=(dominoTarget865*3+i*2+2)%7;final int fleft=left;final String label="["+left+"|"+right+"]";Button b=button(label);b.setTextSize(18);b.setOnClickListener(v->submit(label,fleft==dominoTarget865?100:0));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(58),1);lp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(b,lp);}controls.addView(row);}
    }


    private boolean socialGame866(String g){return "werewolf".equals(g)||"spy".equals(g)||"draw".equals(g);}
    private void resetSocialState866(){if(!socialGame866(type)){privateRoleRound866=0;privateRole866="";privateSecret866="";drawSketch866="";drawerUid866="";drawerName866="";drawPad866=null;}}
    private String roleDocId866(long round,String uid){return "game_"+round+"_"+(uid==null?"":uid);}
    /** Fetch a real recipient-only role once per active round. If missing or
     * temporarily offline, display a Retry control, never recursively render
     * into another unbounded Firebase get() call.
     */
    private long privateFetchRound9966=-1L;
    private boolean privateFetchPending9966=false;
    private boolean privateFetchFailed9966=false;
    private String privateFetchUid9966="";
    private void fetchPrivateRole866(){
        if(me==null||db==null||roundId<=0||
           !("werewolf".equals(type)||"spy".equals(type)||
             ("draw".equals(type)&&me.getUid().equals(drawerUid866))))return;
        final long expected=roundId;final String expectedType=type;
        final String uid=me.getUid();
        if(privateRoleRound866!=expected){
            privateRoleRound866=expected;
            privateRole866="";privateSecret866="";
            privateFetchRound9966=-1L;privateFetchFailed9966=false;
            privateFetchPending9966=false;privateFetchUid9966="";
        }
        if(!KingPartyPrivateRoleLoad9966.needsRequest(
             expected,privateFetchRound9966,privateFetchPending9966,
             privateRole866,privateSecret866,expectedType))return;
        privateFetchRound9966=expected;
        privateFetchPending9966=true;
        privateFetchFailed9966=false;
        privateFetchUid9966=uid;
        room().collection("room_invites").document(roleDocId866(expected,uid))
          .get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(doc->{
              if(!uid.equals(privateFetchUid9966)||me==null||
                 !uid.equals(me.getUid())||roundId!=expected||
                 !expectedType.equals(type))return;
              privateFetchPending9966=false;
              Long stored=doc==null?null:doc.getLong("roundId");
              boolean eligible=doc!=null&&doc.exists()&&stored!=null&&
                  stored.longValue()==expected&&uid.equals(doc.getString("recipientUid"))
                  &&expectedType.equals(doc.getString("gameType"));
              if(eligible){
                  privateRole866=s(doc.getString("role"));
                  privateSecret866=s(doc.getString("secret"));
              }
              if(!eligible||privateRole866.isEmpty()||
                 ("draw".equals(expectedType)&&privateSecret866.isEmpty())){
                  privateRole866="Role unavailable";
                  privateSecret866="";
                  privateFetchFailed9966=true;
              }
              renderControls();
          }).addOnFailureListener(err->{
              if(!uid.equals(privateFetchUid9966)||me==null||
                 !uid.equals(me.getUid())||roundId!=expected||
                 !expectedType.equals(type))return;
              privateFetchPending9966=false;
              privateFetchFailed9966=true;
              privateRole866="Role unavailable";
              privateSecret866="";
              renderControls();
          });
    }
    private void retryPrivateRole9966(){
        if(privateFetchPending9966)return;
        privateFetchRound9966=-1L;privateFetchFailed9966=false;
        privateRole866="";privateSecret866="";
        fetchPrivateRole866();
    }
    private void hiddenRoleControls866(){
        String title="werewolf".equals(type)?"🐺 Werewolf":"🕵 Spy Game";
        if(privateRole866.isEmpty())controls.addView(tv("Loading your private role…",14,MUTED,true));
        else{String text="Your private role: "+privateRole866;if(!privateSecret866.isEmpty())text+="\nSecret: "+privateSecret866;controls.addView(tv(text,16,GOLD,true));}
        if(privateFetchFailed9966){
            Button retry=button("↻ Retry my private role");
            retry.setOnClickListener(v->retryPrivateRole9966());
            controls.addView(retry,new LinearLayout.LayoutParams(-1,dp(48)));
        }
        controls.addView(tv("Use the Party Room voice/chat to discuss. Your role is only shown on your screen.",13,MUTED,false));
        Button vote=button("🗳 Vote / change vote");vote.setOnClickListener(v->votePlayer866());LinearLayout.LayoutParams vp=new LinearLayout.LayoutParams(-1,dp(54));vp.setMargins(0,dp(6),0,0);controls.addView(vote,vp);
        if(moderator){Button finish=button("🏁 Finish vote & reveal result");finish.setOnClickListener(v->finishHiddenRound866());LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,dp(54));fp.setMargins(0,dp(7),0,0);controls.addView(finish,fp);}
    }
    /** Actual active members of this frozen Party round, not stale Ready docs. */
    private void votePlayer866(){
        if(me==null||db==null||roundId<=0||!gameMemberVerified992)return;
        final long expected=roundId;
        final String game=type, voter=me.getUid();
        if(!("werewolf".equals(game)||"spy".equals(game)))return;
        state().get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(st->{
            if(!KingSocialVoteRoster9964.isCurrent(st.exists(),
                    st.getLong("roundId"),expected,
                    s(st.getString("status")),s(st.getString("type")),game))return;
            final java.util.List<String> locked=KingSocialVoteRoster9964.roster(
                st.get("participantUids"));
            if(!locked.contains(voter)){
                toast("You are spectating this round • cannot vote");return;
            }
            room().collection("members")
              .get(com.google.firebase.firestore.Source.SERVER)
              .addOnSuccessListener(members->{
                if(me==null||!voter.equals(me.getUid())||roundId!=expected)return;
                final java.util.ArrayList<String> choices=new java.util.ArrayList<>();
                final java.util.ArrayList<String> labels=new java.util.ArrayList<>();
                for(DocumentSnapshot m:members.getDocuments()){
                    String uid=m.getId();
                    if(!uid.equals(s(m.getString("uid")))||!locked.contains(uid))continue;
                    choices.add(uid);
                    String name=s(m.getString("name"));
                    labels.add(name.isEmpty()?"Player":name);
                }
                if(choices.isEmpty()){toast("No active Party members to vote for");return;}
                String[] labelArray=labels.toArray(new String[0]);
                new AlertDialog.Builder(this)
                  .setTitle("🗳 Vote • "+("werewolf".equals(game)?
                    "Who is the Werewolf?":"Who is the Spy?"))
                  .setItems(labelArray,(dialog,which)->{
                    if(which<0||which>=choices.size())return;
                    final String target=choices.get(which),targetName=labels.get(which);
                    if(me==null||!voter.equals(me.getUid())||roundId!=expected)return;
                    // A Party room / player may leave between list and tap.
                    // Fresh transaction prevents a ghost or spectator vote.
                    db.runTransaction(tx->{
                        DocumentSnapshot live=tx.get(state());
                        DocumentSnapshot own=tx.get(
                            room().collection("members").document(voter));
                        DocumentSnapshot other=tx.get(
                            room().collection("members").document(target));
                        if(!KingSocialVoteRoster9964.isCurrent(live.exists(),
                                live.getLong("roundId"),expected,
                                s(live.getString("status")),
                                s(live.getString("type")),game) ||
                            !KingSocialVoteRoster9964.validBallot(
                                voter,target,KingSocialVoteRoster9964.roster(
                                    live.get("participantUids")),
                                own.exists()&&voter.equals(s(own.getString("uid"))),
                                other.exists()&&target.equals(s(other.getString("uid")))))
                            throw new IllegalStateException(
                                "Round/Party player changed; reopen voting");
                        Map<String,Object> value=new HashMap<>();
                        value.put("uid",voter);value.put("name",displayName);
                        value.put("roundId",expected);value.put("choice",target);
                        value.put("choiceName",targetName);
                        value.put("updatedAt",FieldValue.serverTimestamp());
                        tx.set(room().collection("game_votes").document(voter),value);
                        return null;
                    }).addOnSuccessListener(v->toast("Vote saved: "+targetName))
                      .addOnFailureListener(e->toast("Vote denied: "+e.getMessage()));
                  }).setNegativeButton("Close",null).show();
              }).addOnFailureListener(e->toast("Live Party members unavailable"));
          }).addOnFailureListener(e->toast("Active Party round unavailable"));
    }
    /** Ignore ghosts, spectators and forged voter IDs at round settlement. */
    private void finishHiddenRound866(){
        if(!moderator||me==null||db==null||
          !("werewolf".equals(type)||"spy".equals(type))||roundId<=0)return;
        final long expected=roundId;
        final String game=type,targetRole="werewolf".equals(game)?"Werewolf":"Spy";
        state().get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(stateDoc->{
            if(!KingSocialVoteRoster9964.isCurrent(stateDoc.exists(),
                  stateDoc.getLong("roundId"),expected,
                  s(stateDoc.getString("status")),
                  s(stateDoc.getString("type")),game))return;
            final java.util.List<String> locked=KingSocialVoteRoster9964.roster(
                  stateDoc.get("participantUids"));
            room().collection("game_votes")
              .get(com.google.firebase.firestore.Source.SERVER)
              .addOnSuccessListener(votes->{
                if(roundId!=expected)return;
                final Map<String,Integer> counts=new HashMap<>();
                final Map<String,String> names=new HashMap<>();
                for(DocumentSnapshot d:votes.getDocuments()){
                    Long r=d.getLong("roundId");
                    if(r==null||r.longValue()!=expected)continue;
                    String voter=d.getId(),choice=s(d.getString("choice"));
                    if(!KingSocialVoteRoster9964.validVoteDocument(
                         voter,s(d.getString("uid")),choice,locked))continue;
                    counts.put(choice,counts.containsKey(choice)?
                        counts.get(choice)+1:1);
                    names.put(choice,s(d.getString("choiceName")));
                }
                room().collection("room_invites")
                  .get(com.google.firebase.firestore.Source.SERVER)
                  .addOnSuccessListener(invites->{
                    if(roundId!=expected)return;
                    String targetUid="",targetName=targetRole;
                    for(DocumentSnapshot d:invites.getDocuments()){
                        Long r=d.getLong("roundId");
                        if(r==null||r.longValue()!=expected
                           ||!game.equals(s(d.getString("gameType"))))continue;
                        String u=s(d.getString("recipientUid"));
                        if(targetRole.equals(s(d.getString("role")))
                           &&locked.contains(u)){
                            targetUid=u;targetName=s(d.getString("recipientName"));break;
                        }
                    }
                    if(targetUid.isEmpty()){
                        toast("Private role data is unavailable");return;
                    }
                    int max=0;java.util.List<String> top=new ArrayList<>();
                    for(Map.Entry<String,Integer> e:counts.entrySet()){
                        if(e.getValue()>max){
                            max=e.getValue();top.clear();top.add(e.getKey());
                        }else if(e.getValue()==max)top.add(e.getKey());
                    }
                    String out;
                    if(max==0)out="No valid votes • "+targetName+" was the "+targetRole;
                    else if(top.size()!=1)out="Vote tied • "+targetName+" was the "+targetRole;
                    else{
                        String picked=top.get(0),pickedName=names.get(picked);
                        if(pickedName==null||pickedName.isEmpty())pickedName="Player";
                        boolean found=targetUid.equals(picked);
                        out=(found?("werewolf".equals(game)?"🏆 Village wins":
                           "🏆 Players found the Spy"):("werewolf".equals(game)?
                           "🐺 Werewolf escaped":"🕵 Spy escaped"))
                           +"\nMost voted: "+pickedName+"\n"+targetRole+": "+targetName;
                    }
                    closeSocialRound866(out);
                  }).addOnFailureListener(e->toast("Private roles unavailable"));
              }).addOnFailureListener(e->toast("Party votes unavailable"));
          }).addOnFailureListener(e->toast("Party game changed; refresh"));
    }
    private void submitDrawGuess866(EditText box){
        if(me==null||roundId<=0||("draw".equals(type)
            &&me.getUid().equals(drawerUid866)))return;String guess=box==null?"":s(box.getText().toString());if(guess.isEmpty()){toast("Enter a guess");return;}if(guess.length()>80)guess=guess.substring(0,80);Map<String,Object>v=new HashMap<>();v.put("uid",me.getUid());v.put("name",displayName);v.put("roundId",roundId);v.put("choice",guess);v.put("updatedAt",FieldValue.serverTimestamp());final String sent=guess;room().collection("game_votes").document(me.getUid()).set(v).addOnSuccessListener(x->{box.setText("");toast("Guess sent: "+sent);}).addOnFailureListener(e->toast("Guess failed: "+e.getMessage()));
    }
    /** Actual answer comes only from the private drawer invite on SERVER.
     * All recorded guesses must be current-round, signed-UID participants.
     */
    private void finishDraw866(){
        if(!moderator||me==null||db==null||!"draw".equals(type)||roundId<=0)return;
        final long expected=roundId;
        final String drawer=drawerUid866;
        if(drawer==null||drawer.isEmpty()){toast("No live drawer");return;}
        state().get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(current9965->{
            if(!KingSocialVoteRoster9964.isCurrent(current9965.exists(),
                    current9965.getLong("roundId"),expected,
                    s(current9965.getString("status")),
                    s(current9965.getString("type")),"draw"))return;
            final java.util.List<String> frozen9965=
                KingSocialVoteRoster9964.roster(current9965.get("participantUids"));
            if(!frozen9965.contains(drawer)){toast("Drawer left the round");return;}
            room().collection("room_invites").document(roleDocId866(expected,drawer))
              .get(com.google.firebase.firestore.Source.SERVER)
              .addOnSuccessListener(invite9965->{
                if(roundId!=expected||!invite9965.exists()||
                  !"draw".equals(s(invite9965.getString("gameType")))||
                  !"Drawer".equals(s(invite9965.getString("role")))||
                  !drawer.equals(s(invite9965.getString("recipientUid")))||
                  invite9965.getLong("roundId")==null||
                  invite9965.getLong("roundId").longValue()!=expected){
                    toast("Private draw word not available");return;
                }
                final String answer=s(invite9965.getString("secret"));
                if(answer.isEmpty()){toast("Private draw word is empty");return;}
                room().collection("game_votes")
                  .get(com.google.firebase.firestore.Source.SERVER)
                  .addOnSuccessListener(q->{
                    if(roundId!=expected)return;
                    List<String> winners=new ArrayList<>();
                    int total=0;
                    for(DocumentSnapshot d:q.getDocuments()){
                        Long r=d.getLong("roundId");
                        String id=d.getId(),claimed=s(d.getString("uid"));
                        if(r==null||r.longValue()!=expected||
                          !id.equals(claimed)||!frozen9965.contains(id)||
                          id.equals(drawer))continue;
                        total++;
                        if(answer.equalsIgnoreCase(s(d.getString("choice")).trim()))
                            winners.add(s(d.getString("name")));
                    }
                    final String out="🎨 Word: "+answer+"\n"+
                      (winners.isEmpty()?"No correct guess":"🏆 Correct: "+join(winners))+
                      "\nGuesses: "+total;
                    closeSocialRound866(out);
                  }).addOnFailureListener(e->toast("Could not read guesses"));
              }).addOnFailureListener(e->toast("Private draw word unavailable"));
          }).addOnFailureListener(e->toast("Active draw round unavailable"));
    }
    private void closeSocialRound866(String out){
        if(!moderator||me==null||db==null||roundId<=0)return;
        final long expected=roundId; final String expectedType=type;
        final String expectedUid=me.getUid();
        db.runTransaction(tx->{
            DocumentSnapshot fresh=tx.get(state());
            DocumentSnapshot owner=tx.get(
                room().collection("members").document(expectedUid));
            if(!owner.exists()||!expectedUid.equals(s(owner.getString("uid")))||
               !KingSocialVoteRoster9964.isCurrent(fresh.exists(),
                   fresh.getLong("roundId"),expected,
                   s(fresh.getString("status")),
                   s(fresh.getString("type")),expectedType))
                throw new IllegalStateException("Party round changed; cannot finish");
            Map<String,Object> update=new HashMap<>();
            update.put("actorUid",expectedUid);
            update.put("result",out);update.put("status","closed");
            update.put("updatedAt",FieldValue.serverTimestamp());
            tx.update(state(),update);
            return null;
        }).addOnSuccessListener(v->postGameEvent750(
             "🏁 "+out.replace("\n"," • ")))
          .addOnFailureListener(e->toast("Finish denied: "+e.getMessage()));
    }
    private void drawGuessControls866(){
        boolean drawer=me!=null&&me.getUid().equals(drawerUid866);controls.addView(tv(drawer?("🎨 You are drawing • secret word: "+
            (privateRoleRound866==roundId&&!privateSecret866.isEmpty()?
             privateSecret866:"Loading your private word…")):("🎨 "+(drawerName866.isEmpty()?"Host":drawerName866)+" is drawing • guess the word"),15,drawer?GOLD:Color.WHITE,true));
        if(drawer&&(privateRoleRound866!=roundId||privateSecret866.isEmpty())
            &&!privateFetchPending9966&&!privateFetchFailed9966)
            fetchPrivateRole866();
        if(drawer&&privateFetchFailed9966){
            Button retry=button("↻ Retry private drawing word");
            retry.setOnClickListener(v->retryPrivateRole9966());
            controls.addView(retry,new LinearLayout.LayoutParams(-1,dp(48)));
        }
        drawPad866=new DrawPad866(drawSketch866,drawer&&moderator
            &&privateRoleRound866==roundId&&!privateSecret866.isEmpty());drawPad866.setBackgroundColor(Color.WHITE);LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,dp(260));pp.setMargins(0,dp(6),0,dp(6));controls.addView(drawPad866,pp);
        if(drawer&&moderator){Button clear=button("🧽 Clear canvas");clear.setOnClickListener(v->publishDrawSketch866(""));controls.addView(clear,new LinearLayout.LayoutParams(-1,dp(48)));}
        else{LinearLayout row=new LinearLayout(this);EditText in=new EditText(this);in.setHint("Type your guess");in.setSingleLine(true);in.setTextColor(Color.WHITE);in.setHintTextColor(0xffa99fbb);in.setBackground(bg(CARD,14));in.setPadding(dp(12),0,dp(12),0);row.addView(in,new LinearLayout.LayoutParams(0,dp(52),1));Button send=button("Guess");send.setOnClickListener(v->submitDrawGuess866(in));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(dp(100),dp(52));bp.setMargins(dp(8),0,0,0);row.addView(send,bp);controls.addView(row);}
        if(moderator){Button finish=button("🏁 Finish drawing round");finish.setOnClickListener(v->finishDraw866());LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,dp(54));fp.setMargins(0,dp(7),0,0);controls.addView(finish,fp);}
    }
    /** Only the current actual private-word drawer may alter a running
     * Party canvas. Transaction ensures delayed callbacks cannot overwrite
     * a different round or closed/changed game. Security rules agree.
     */
    private void publishDrawSketch866(String sketch){
        if(!moderator||!"draw".equals(type)||me==null||db==null||
           !gameMemberVerified992||!gameStateVerified9943)return;
        if(!KingLiveDrawRound9967.validSketch(sketch)){
            toast("Canvas data is invalid or full");return;
        }
        final long expected=roundId;
        final String uid=me.getUid(),encoded=sketch;
        if(!KingLiveDrawRound9967.allowed(true,expected,roundId,
               "active",type,drawerUid866,uid,true,
               privateRoleRound866==expected&&!privateSecret866.isEmpty()))return;
        db.runTransaction(tx->{
            DocumentSnapshot live=tx.get(state());
            DocumentSnapshot member=tx.get(
                room().collection("members").document(uid));
            Long actual=live.getLong("roundId");
            String owner=s(live.getString("drawerUid"));
            if(!KingLiveDrawRound9967.allowed(
                live.exists(),expected,actual==null?-1L:actual.longValue(),
                s(live.getString("status")),s(live.getString("type")),
                owner,uid,
                member.exists()&&uid.equals(s(member.getString("uid"))),
                true))
                throw new IllegalStateException(
                    "Party game/drawer changed: sketch was not sent");
            Map<String,Object> update=new HashMap<>();
            update.put("actorUid",uid);
            update.put("sketch",encoded);
            update.put("updatedAt",FieldValue.serverTimestamp());
            tx.update(state(),update);
            return null;
        }).addOnFailureListener(err->toast(
            "Drawing sync denied: "+err.getMessage()));
    }
    private class DrawPad866 extends View{
        private final Paint pen=new Paint(Paint.ANTI_ALIAS_FLAG);private String sketch;private final boolean editable;private float lastX,lastY;private long lastPoint;
        DrawPad866(String s,boolean edit){super(RoomGameActivity.this);sketch=s==null?"":s;editable=edit;pen.setColor(0xff3d216b);pen.setStrokeWidth(dp(4));pen.setStrokeCap(Paint.Cap.ROUND);pen.setStrokeJoin(Paint.Join.ROUND);pen.setStyle(Paint.Style.STROKE);setMinimumHeight(dp(240));}
        private int nx(float x){return getWidth()<=0?0:Math.max(0,Math.min(1000,Math.round(x*1000f/getWidth())));}private int ny(float y){return getHeight()<=0?0:Math.max(0,Math.min(1000,Math.round(y*1000f/getHeight())));}private float sx(int x){return getWidth()*x/1000f;}private float sy(int y){return getHeight()*y/1000f;}
        @Override protected void onDraw(Canvas c){super.onDraw(c);if(sketch==null||sketch.isEmpty())return;String[] strokes=sketch.split("\\|");for(String st:strokes){if(st.isEmpty())continue;String[] pts=st.split(";");float px=-1,py=-1;for(String pt:pts){if(pt.isEmpty())continue;String[] xy=pt.split(",");if(xy.length!=2)continue;try{float x=sx(Integer.parseInt(xy[0])),y=sy(Integer.parseInt(xy[1]));if(px>=0)c.drawLine(px,py,x,y,pen);px=x;py=y;}catch(Exception ignored){}}}}
        private void add(float x,float y,boolean fresh){if(sketch.length()>11800)return;String pt=nx(x)+","+ny(y)+";";sketch+=(fresh?"|":"")+pt;invalidate();}
        @Override public boolean onTouchEvent(MotionEvent e){if(!editable)return false;float x=e.getX(),y=e.getY();if(e.getAction()==MotionEvent.ACTION_DOWN){lastX=x;lastY=y;lastPoint=android.os.SystemClock.elapsedRealtime();add(x,y,true);return true;}if(e.getAction()==MotionEvent.ACTION_MOVE){long now=android.os.SystemClock.elapsedRealtime();if(now-lastPoint>=45&&(Math.abs(x-lastX)+Math.abs(y-lastY)>6)){lastX=x;lastY=y;lastPoint=now;add(x,y,false);}return true;}if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL){add(x,y,false);publishDrawSketch866(sketch);return true;}return true;}
    }
    /** Start hidden-role and drawing games only with live authenticated Party players. */
    private void startSocialRound866(String game){
        if(!moderator||!gameMemberVerified992||!gameStateVerified9943||
           me==null||db==null||!"closed".equals(status)){
            toast("Wait for a verified Host and a finished round");return;
        }
        final long previous9958=roundId;
        final long next9958=Math.max(System.currentTimeMillis(),previous9958+1);
        final String hostUid9958=me.getUid();
        room().collection("game_ready")
           .whereEqualTo("ready",true)
           .get(com.google.firebase.firestore.Source.SERVER)
           .addOnSuccessListener(readySnap9958->{
            java.util.ArrayList<DocumentSnapshot> readyDocs9958=new java.util.ArrayList<>(readySnap9958.getDocuments());
            Collections.sort(readyDocs9958,(x,y)->x.getId().compareTo(y.getId()));
            if(readyDocs9958.size()>40){toast("Up to 40 Ready Party members per social game");return;}
            db.runTransaction(tx->{
                DocumentSnapshot last9958=tx.get(state());
                if(last9958.exists()){
                    Long stored9958=last9958.getLong("roundId");
                    if(stored9958==null||stored9958.longValue()!=previous9958||
                       !"closed".equals(s(last9958.getString("status"))))
                        throw new IllegalStateException("Another Party game started; refresh");
                }else if(previous9958!=0L)
                    throw new IllegalStateException("Party game state changed");
                java.util.ArrayList<String> liveUids9958=new java.util.ArrayList<>();
                java.util.HashMap<String,String> liveNames9958=new java.util.HashMap<>();
                // All transaction reads BEFORE any role or game-state writes.
                for(DocumentSnapshot candidate9958:readyDocs9958){
                    final String uid9958=candidate9958.getId();
                    if(uid9958.isEmpty())continue;
                    DocumentSnapshot freshReady9958=tx.get(
                        room().collection("game_ready").document(uid9958));
                    DocumentSnapshot actualMember9958=tx.get(
                        room().collection("members").document(uid9958));
                    if(KingPartySocialRoster9959.isEligible(uid9958,
                           freshReady9958.exists(),s(freshReady9958.getString("uid")),
                           freshReady9958.getBoolean("ready"),
                           actualMember9958.exists(),s(actualMember9958.getString("uid")))){
                        liveUids9958.add(uid9958);
                        String nm9958=s(freshReady9958.getString("name"));
                        liveNames9958.put(uid9958,nm9958.isEmpty()?"Player":nm9958);
                    }
                }
                // Host/drawer must be an actual explicitly Ready Party participant.
                if(!liveUids9958.contains(hostUid9958))
                    throw new IllegalStateException("Host must join Party and tap Ready first");
                if(!KingPartySocialRoster9958.valid(game,liveUids9958))
                    throw new IllegalStateException("Not enough live Ready members for "+game+
                        " • ask players in the Party to tap Ready");
                // Fair play: Host can also draw Werewolf/Spy, not only other players.
                final String target9958="draw".equals(game)?"":
                    KingPartySocialRoster9959.pickHiddenRole(liveUids9958,
                        new java.security.SecureRandom());
                final String secret9958="spy".equals(game)?KingPartyHiddenWord9965.spy(new java.security.SecureRandom()):"";
                String hostRole9958="Moderator / observer",hostSecret9958="";
                if(!"draw".equals(game)){
                    for(String uid9958:liveUids9958){
                        String role9958="werewolf".equals(game)?
                            (uid9958.equals(target9958)?"Werewolf":"Villager"):
                            (uid9958.equals(target9958)?"Spy":"Player");
                        String personal9958="spy".equals(game)&&!uid9958.equals(target9958)?secret9958:"";
                        if(uid9958.equals(hostUid9958)){
                            // The Host is a real participant too. Cache for immediate UI,
                            // but DO NOT skip their private recipient-only Firestore invite.
                            // This is needed when Android re-creates the Host Activity.
                            hostRole9958=role9958;hostSecret9958=personal9958;
                        }
                        java.util.Map<String,Object> invite9958=new HashMap<>();
                        invite9958.put("senderUid",hostUid9958);
                        invite9958.put("senderName",displayName);
                        invite9958.put("recipientUid",uid9958);
                        invite9958.put("recipientName",liveNames9958.get(uid9958));
                        invite9958.put("kind","game_role");invite9958.put("gameType",game);
                        invite9958.put("roundId",next9958);invite9958.put("role",role9958);
                        invite9958.put("secret",personal9958);
                        invite9958.put("createdAt",FieldValue.serverTimestamp());
                        tx.set(room().collection("room_invites").document(roleDocId866(next9958,uid9958)),invite9958);
                    }
                }
                if("draw".equals(game)){
                    // Make a secret ONCE for this accepted transaction; no
                    // public roomId/roundId hash can reveal it to spectators.
                    String word9965=KingPartyHiddenWord9965.draw(
                        new java.security.SecureRandom());
                    hostRole9958="Drawer";
                    hostSecret9958=word9965;
                    // Only the actual drawer receives the secret word. The
                    // invite remains retrievable after Activity recreation.
                    Map<String,Object> privateWord9965=new HashMap<>();
                    privateWord9965.put("senderUid",hostUid9958);
                    privateWord9965.put("senderName",displayName);
                    privateWord9965.put("recipientUid",hostUid9958);
                    privateWord9965.put("recipientName",liveNames9958.get(hostUid9958));
                    privateWord9965.put("kind","game_role");
                    privateWord9965.put("gameType","draw");
                    privateWord9965.put("roundId",next9958);
                    privateWord9965.put("role","Drawer");
                    privateWord9965.put("secret",word9965);
                    privateWord9965.put("createdAt",FieldValue.serverTimestamp());
                    tx.set(room().collection("room_invites")
                         .document(roleDocId866(next9958,hostUid9958)),privateWord9965);
                }
                java.util.Map<String,Object> st9958=new HashMap<>();
                st9958.put("actorUid",hostUid9958);st9958.put("actorName",displayName);
                st9958.put("type",game);st9958.put("status","active");
                st9958.put("roundId",next9958);st9958.put("result","");
                st9958.put("participantUids",liveUids9958);
                st9958.put("prompt","draw".equals(game)?"Draw & Guess • "+displayName+" is drawing":
                  "werewolf".equals(game)?"Discuss in Party voice/chat, then vote for Werewolf":
                    "Secret location • ask questions and vote for the Spy");
                if("draw".equals(game)){
                    st9958.put("drawerUid",hostUid9958);
                    st9958.put("drawerName",displayName);
                    st9958.put("sketch","");
                }
                st9958.put("updatedAt",FieldValue.serverTimestamp());
                tx.set(state(),st9958);
                return new String[]{hostRole9958,hostSecret9958,String.valueOf(liveUids9958.size())};
            }).addOnSuccessListener(started9958->{
                if(isFinishing()||isDestroyed())return;
                privateRoleRound866=next9958;
                privateRole866=started9958[0];
                privateSecret866=started9958[1];
                postGameEvent750("🎮 "+displayName+" started "+game+" with "+started9958[2]+" live Party players");
            }).addOnFailureListener(e->toast("Could not start real Party game: "+e.getMessage()));
        }).addOnFailureListener(e->toast("Live Party Ready list unavailable: "+e.getMessage()));
    }

    private void startRound(String game){
        if(socialGame866(game)){startSocialRound866(game);return;}
        if("ttt".equals(game)){startTtt864();return;}
        if(!moderator||!"closed".equals(status)){toast("Finish the current round first");return;}
        if(!KingPartyRoundRoster9972.isOrdinaryGame(game)){
            toast("Unsupported Party game type");return;
        }
        // Use SERVER Ready=true and an independently verified live members
        // snapshot. Old Ready=false or departed ghosts must not block real games
        // or fill the 50-player limit. The transaction rechecks every member.
        room().collection("game_ready").whereEqualTo("ready",true)
          .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(q->{
            room().collection("members")
              .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(members->{
                java.util.HashSet<String> memberUids9972=new java.util.HashSet<>();
                for(DocumentSnapshot member:members.getDocuments())
                    if(member.getId().equals(s(member.getString("uid"))))
                        memberUids9972.add(member.getId());
                java.util.ArrayList<String> frozenPlayers9946=new java.util.ArrayList<>();
                for(DocumentSnapshot ready:q.getDocuments()){
                    if(KingPartyRoundRoster9972.realReady(ready.getId(),
                           s(ready.getString("uid")),ready.getBoolean("ready"),
                           memberUids9972.contains(ready.getId())))
                        frozenPlayers9946.add(ready.getId());
                }
                Collections.sort(frozenPlayers9946);
                if(!KingPartyRoundRoster9972.valid(frozenPlayers9946)){
                    toast("Need 2–50 live Ready Party players • stale users are ignored");return;
                }
                startRoundNow750(game,frozenPlayers9946);
              }).addOnFailureListener(e->toast("Live Party member check failed: "+e.getMessage()));
          }).addOnFailureListener(e->toast("Ready check failed: "+e.getMessage()));
    }
    private void startRoundNow750(String game,java.util.List<String> frozenPlayers9946){
        if(!moderator)return;
        if(KingPartyRoundRoster9972.isOrdinaryGame(game)
           &&!KingRoomRoster9946.valid(frozenPlayers9946)){
            toast("Ready participants changed; reopen round");return;
        }
        final long previous=roundId;
        // No unbounded collection read or hundreds of ghost-move deletions.
        // Only the verified Ready roster needs previous per-UID moves cleared.
        {
            long next=Math.max(System.currentTimeMillis(),previous+1);
            String p="treasure".equals(game)?"Choose one of three chests • no paid prize":"rps".equals(game)?"Choose Rock, Paper or Scissors":"dice".equals(game)?"Roll once • highest roll wins":"number".equals(game)?"Pick 1–30 • closest to a shared draw wins":"coin".equals(game)?"Pick Heads or Tails":"wheel".equals(game)?"Spin once • highest score wins":"bingo".equals(game)?"Pick 1–9 • closest to a shared draw wins":"slot".equals(game)?"Spin once • best reel score wins":"reaction".equals(game)?"Tap as fast as you can when the control appears":"highlow".equals(game)?"Pick High 51–100 or Low 1–50":"sheep".equals(game)?"Run once • highest sheep speed wins":"memory".equals(game)?"Memory Duel • clear the same 4-pair board":"domino".equals(game)?"Domino Match • match the shared chain end":"Release one animal • highest power wins";
            Map<String,Object> value=new HashMap<>();
            value.put("actorUid",me.getUid());value.put("actorName",displayName);value.put("type",game);value.put("prompt",p);value.put("result","");value.put("status","active");value.put("roundId",next);
            if(KingPartyRoundRoster9972.isOrdinaryGame(game))
                value.put("participantUids",new java.util.ArrayList<>(frozenPlayers9946));
            if("memory".equals(game))value.put("memoryLayout",memoryLayoutForRound865(next));if("domino".equals(game))value.put("dominoTarget",new java.security.SecureRandom().nextInt(7));if("treasure".equals(game))value.put("treasureChest",1+new java.security.SecureRandom().nextInt(3));value.put("updatedAt",FieldValue.serverTimestamp());
            db.runTransaction(tx->{
                DocumentSnapshot current=tx.get(state());
                if(current.exists()){
                    Long storedRound9927=current.getLong("roundId");
                    if("active".equals(current.getString("status"))||storedRound9927==null||storedRound9927.longValue()!=previous)
                        throw new IllegalStateException("Round changed. Reopen the controls.");
                }
                // A previous SERVER Ready list can still contain a departed user.
                // Fresh transaction reads are retried if a player leaves/unreadies.
                if(KingPartyRoundRoster9972.isOrdinaryGame(game)){
                    java.util.ArrayList<String> activeReady9952=new java.util.ArrayList<>();
                    for(String candidate9952:frozenPlayers9946){
                        if(candidate9952==null||candidate9952.isEmpty())continue;
                        DocumentSnapshot actualReady9952=tx.get(
                            room().collection("game_ready").document(candidate9952));
                        DocumentSnapshot actualMember9952=tx.get(
                            room().collection("members").document(candidate9952));
                        if(KingLivePartyRoster9952.isActiveReady(candidate9952,
                            actualReady9952.exists(),s(actualReady9952.getString("uid")),
                            actualReady9952.getBoolean("ready"),
                            actualMember9952.exists(),s(actualMember9952.getString("uid"))))
                            activeReady9952.add(candidate9952);
                    }
                    if(!KingRoomRoster9946.valid(activeReady9952))
                        throw new IllegalStateException(
                            "Need two real Ready Party players • ask members to tap Ready");
                    value.put("participantUids",activeReady9952);
                }
                // SERVER transaction reads of each participant's previous
                // move happen BEFORE ANY write. Each gets a fresh move slot
                // atomically with the new round. Ghost/spectator moves remain
                // harmless because they belong to a different round/roster.
                java.util.List<String> cleanup9976=
                    KingPartyMoveCleanup9976.roster(value.get("participantUids"));
                if(!KingPartyMoveCleanup9976.valid(cleanup9976))
                    throw new IllegalStateException("Live Ready roster unavailable for rematch");
                java.util.List<DocumentReference> previousMoves9976=new java.util.ArrayList<>();
                for(String uid9976:cleanup9976){
                    DocumentReference oldMove9976=room().collection("game_moves").document(uid9976);
                    if(tx.get(oldMove9976).exists())previousMoves9976.add(oldMove9976);
                }
                // At most 50 existing move deletes + one game_state write.
                for(DocumentReference oldMove9976:previousMoves9976)
                    tx.delete(oldMove9976);
                tx.set(state(),value);return null;
            }).addOnSuccessListener(v->postGameEvent750("🎮 "+displayName+" started "+game.toUpperCase()+" round"))
              .addOnFailureListener(e->toast("Start failed: "+e.getMessage()));
        }
    }
    private void postGameEvent750(String text){Map<String,Object>e=new HashMap<>();e.put("actorUid",me.getUid());e.put("actorName",displayName);e.put("type","play");e.put("text",text);e.put("createdAt",FieldValue.serverTimestamp());room().collection("events").add(e);}

    private void submit(String move,int score){
        if(roundId==0||!"active".equals(status)||me==null||!gameMemberVerified992
           ||!gameStateVerified9943||!KingNetwork.online(this)){
           toast("Party Game reconnecting — wait for verified round");return;}
        final long expected=roundId;final String expectedType=type;
        if(KingPartyRoundRoster9972.isOrdinaryGame(expectedType)
            &&!KingPartyRoundRoster9972.canPlay(frozenRoster9947,me.getUid())){
            toast("Spectating • you were not Ready at this round's start");return;
        }
        DocumentReference mine=room().collection("game_moves").document(me.getUid());
        db.runTransaction(tx->{
            DocumentSnapshot active=tx.get(state()),existing=tx.get(mine);
            DocumentSnapshot membership=tx.get(room().collection("members").document(me.getUid()));
            if(!membership.exists()||!me.getUid().equals(s(membership.getString("uid")))
                ||!KingPartyRoundRoster9972.canPlay(
                    KingPartyRoundRoster9972.asRoster(active.get("participantUids")),me.getUid()))
                throw new IllegalStateException("Only actual Ready round participants may move");
            if(!active.exists()||!"active".equals(active.getString("status"))||active.getLong("roundId")==null||active.getLong("roundId").longValue()!=expected||!expectedType.equals(active.getString("type")))throw new IllegalStateException("Round changed");
            if(existing.exists())throw new IllegalStateException("Your move is already locked");
            Map<String,Object> d=new HashMap<>();d.put("uid",me.getUid());d.put("name",displayName.length()>80?displayName.substring(0,80):displayName);d.put("roundId",expected);d.put("type",expectedType);d.put("move",move);d.put("score",score);d.put("updatedAt",FieldValue.serverTimestamp());
            tx.set(mine,d);return null;
        }).addOnSuccessListener(v->toast("Move locked ✓")).addOnFailureListener(e->toast("Move failed: "+e.getMessage()));
    }

    private ListenerRegistration readyMembersListener9971;
    private boolean readySeen9971=false,membersSeen9971=false,readyToggleBusy9971=false;
    private List<DocumentSnapshot> readyDocs9971=new ArrayList<>();
    private Map<String,String> memberNames9971=new HashMap<>();
    private void renderVerifiedReady9971(){
        if(readyBox==null||isFinishing()||isDestroyed()||!gameMemberVerified992)return;
        readyBox.removeAllViews();
        if(!readySeen9971||!membersSeen9971){
            readyBox.addView(tv("⏳ Verifying real Ready Party players with SERVER…",13,MUTED,false));
            return;
        }
        List<String> rows=new ArrayList<>();
        for(DocumentSnapshot d:readyDocs9971){
            String uid=d.getId();
            if(!KingRealReadyRoster9971.realReady(uid,s(d.getString("uid")),
                  d.getBoolean("ready"),memberNames9971.containsKey(uid)))continue;
            String name=memberNames9971.get(uid);
            rows.add("✅ "+(name==null||name.isEmpty()?"Player":name));
        }
        Collections.sort(rows,String.CASE_INSENSITIVE_ORDER);
        if(rows.isEmpty())readyBox.addView(tv("No current Party members are Ready",13,MUTED,false));
        for(String row:rows){
            TextView label=tv(row,14,Color.WHITE,true);
            label.setBackground(bg(CARD,12));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(44));
            lp.setMargins(0,dp(3),0,dp(3));readyBox.addView(label,lp);
        }
    }
    private void listenReady750(){
        if(readyListener!=null)readyListener.remove();
        if(readyMembersListener9971!=null)readyMembersListener9971.remove();
        readySeen9971=false;membersSeen9971=false;
        readyDocs9971=new ArrayList<>();memberNames9971=new HashMap<>();
        renderVerifiedReady9971();
        readyListener=room().collection("game_ready").whereEqualTo("ready",true)
          .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(q,e)->{
            if(!gameMemberVerified992||isFinishing()||isDestroyed())return;
            readySeen9971=e==null&&q!=null&&!q.getMetadata().isFromCache()
                &&!q.getMetadata().hasPendingWrites();
            readyDocs9971=readySeen9971?new ArrayList<>(q.getDocuments()):new ArrayList<>();
            renderVerifiedReady9971();
          });
        readyMembersListener9971=room().collection("members")
          .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(q,e)->{
            if(!gameMemberVerified992||isFinishing()||isDestroyed())return;
            membersSeen9971=e==null&&q!=null&&!q.getMetadata().isFromCache()
                &&!q.getMetadata().hasPendingWrites();
            memberNames9971=new HashMap<>();
            if(membersSeen9971)for(DocumentSnapshot d:q.getDocuments()){
                String uid=d.getId();
                if(uid.equals(s(d.getString("uid"))))
                    memberNames9971.put(uid,s(d.getString("name")));
            }
            renderVerifiedReady9971();
            if("active".equals(status)&&KingPartyRoundRoster9972.isOrdinaryGame(type))
                renderControls(); // refresh winner gate when Party member leaves
          });
    }
    /** Explicit Ready toggle: always check current SERVER member + own Ready
     * in one Firestore transaction. A double tap cannot produce two writes.
     */
    private void toggleReady750(){
        if(!KingNetwork.online(this)||!gameMemberVerified992||me==null||db==null||readyToggleBusy9971){
            toast("Waiting for verified Party member connection");return;
        }
        final String uid=me.getUid();
        final DocumentReference mine=room().collection("game_ready").document(uid);
        final DocumentReference member=room().collection("members").document(uid);
        readyToggleBusy9971=true;
        db.runTransaction(tx->{
            DocumentSnapshot live=tx.get(member),prior=tx.get(mine);
            if(!live.exists()||!uid.equals(s(live.getString("uid"))))
                throw new IllegalStateException("Not a live Party member");
            boolean opted=prior.exists()&&KingRealReadyRoster9971.realReady(
                uid,s(prior.getString("uid")),prior.getBoolean("ready"),true);
            if(opted)tx.delete(mine);
            else{
                Map<String,Object> data=new HashMap<>();
                data.put("uid",uid);data.put("name",s(live.getString("name")));
                data.put("ready",true);
                data.put("updatedAt",FieldValue.serverTimestamp());
                tx.set(mine,data);
            }
            return !opted;
        }).addOnSuccessListener(enabled->{
            readyToggleBusy9971=false;
            toast(Boolean.TRUE.equals(enabled)?"Ready ✓":"Not Ready");
        }).addOnFailureListener(e->{
            readyToggleBusy9971=false;toast("Ready failed: "+e.getMessage());
        });
    }
    private void clearReady750(){
        if(!moderator)return;
        room().collection("game_ready").get().addOnSuccessListener(q->{for(DocumentSnapshot d:q.getDocuments())d.getReference().delete();toast("Ready list cleared");});
    }
    private void rematch750(){
        if(!moderator)return;
        String game=type.isEmpty()?"rps":type;
        if("ttt".equals(game)){startTtt864();return;}
        startRound(game);
    }

    private void renderMoves(){
        movesBox.removeAllViews();
        if(KingPartyRoundRoster9972.isOrdinaryGame(type)
            &&"active".equals(status)&&KingRoomRoster9946.valid(frozenRoster9947)){
            java.util.ArrayList<String> realSubmissions9947=new java.util.ArrayList<>();
            for(DocumentSnapshot submitted9947:currentMoves)
                if(type.equals(submitted9947.getString("type"))
                   &&submitted9947.getId().equals(submitted9947.getString("uid")))
                    realSubmissions9947.add(submitted9947.getId());
            int count9947=KingRoomCancel9947.countValid(frozenRoster9947,realSubmissions9947);
            movesBox.addView(tv("👥 Ready moves: "+count9947+"/"+frozenRoster9947.size()
                +(count9947==frozenRoster9947.size()?" • all played":" • waiting for players"),
                13,MUTED,true));
        }
        if(currentMoves.isEmpty()){movesBox.addView(tv("Waiting for players…",13,MUTED,false));return;}List<DocumentSnapshot> sorted=new ArrayList<>(currentMoves);Collections.sort(sorted,(a,b)->s(a.getString("name")).compareToIgnoreCase(s(b.getString("name"))));for(DocumentSnapshot d:sorted){String name=s(d.getString("name")),move=s(d.getString("move"));TextView row=tv("👤 "+(name.isEmpty()?"Player":name)+"  •  "+KingPartyGameFairPlay9949.visibleMove(type,status,gameStateVerified9943,move),14,Color.WHITE,true);row.setBackground(bg(CARD,12));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(46));lp.setMargins(0,dp(3),0,dp(3));movesBox.addView(row,lp);}}
    /** Explicit host decision after a player leaves/disconnects. Never award a winner. */
    private void confirmCancelRound9947(){
        if(!KingRoomCancel9947.canCancel(moderator,gameMemberVerified992,
              gameStateVerified9943,status,roundId)){
            toast("Cancel is only available during a verified active Party game");
            return;
        }
        final long expectedRound9947=roundId;
        final String expectedType9947=type;
        new AlertDialog.Builder(this)
            .setTitle("🚫 Cancel this Party game?")
            .setMessage("If a player has left, cancel this round without declaring a winner. "+
                "Submitted moves remain in history until the next round. Start/Rematch will "+
                "let the Ready players begin a fresh game.")
            .setNegativeButton("Keep playing",null)
            .setPositiveButton("Cancel • no winner",(dialog,which)->
                cancelRound9947(expectedRound9947,expectedType9947))
            .show();
    }
    private void cancelRound9947(long expectedRound9947,String expectedType9947){
        if(me==null||db==null||
           !KingRoomCancel9947.canCancel(moderator,gameMemberVerified992,
              gameStateVerified9943,status,roundId)
           ||roundId!=expectedRound9947||!type.equals(expectedType9947)){
            toast("Game changed • cancellation not sent");return;
        }
        final String canceller9947=me.getUid();
        db.runTransaction(tx->{
            DocumentSnapshot latest=tx.get(state());
            if(!latest.exists()||latest.getLong("roundId")==null||
               latest.getLong("roundId").longValue()!=expectedRound9947||
               !expectedType9947.equals(latest.getString("type"))||
               !"active".equals(latest.getString("status")))
                throw new IllegalStateException("This round already changed or finished");
            java.util.Map<String,Object> fields=new java.util.HashMap<>();
            fields.put("status","closed");
            fields.put("cancelled",true);
            fields.put("result",KingRoomCancel9947.RESULT);
            fields.put("actorUid",canceller9947);
            fields.put("updatedAt",FieldValue.serverTimestamp());
            tx.update(state(),fields);return null;
        }).addOnSuccessListener(v->{
            postGameEvent750("🏁 "+KingRoomCancel9947.RESULT);
            toast("Round cancelled without a winner • Rematch is available");
        }).addOnFailureListener(e->toast("Cancel failed: "+e.getMessage()));
    }

    /** A true SERVER snapshot can complete a played round without another Host tap.
     * Only the Host/Co-host attempts settlement; all actual scoring and status
     * writes still use finishRound's fresh SERVER reads and Firestore transaction.
     */
    private void maybeAutoFinish9953(){
        if(autoSettlementInFlight9954||settlementRetryTask9954!=null||
           lastAutoSettlementAttempt9953==roundId||me==null||isFinishing()||isDestroyed())return;
        java.util.ArrayList<String> submitted9953=new java.util.ArrayList<>();
        for(DocumentSnapshot move9953:currentMoves)
            if(type.equals(s(move9953.getString("type")))
               &&move9953.getId().equals(s(move9953.getString("uid"))))
                submitted9953.add(move9953.getId());
        if(!KingPartyAllPlayed9973.shouldFinish(type,status,moderator,
                gameMemberVerified992,gameStateVerified9943,roundId,
                frozenRoster9947,submitted9953))return;
        if(autoRetryRound9954!=roundId){
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
        if(!gameMemberVerified992||!gameStateVerified9943||!moderator||
           me==null||roundId==0||!"active".equals(status)){
            autoSettlementInFlight9954=false;
            toast("Join a verified active Party round before finishing");return;
        }
        final long expected=roundId;
        final String expectedType=type;
        final String roundOwner10042=me.getUid();
        // Finishing must fetch this round's SERVER moves only: 610+ ghost
        // history records must not stall a genuine multiplayer winner.
        room().collection("game_moves")
            .whereEqualTo("roundId",expected)
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(q->{
                FirebaseUser now10042=FirebaseAuth.getInstance().getCurrentUser();
                if(isFinishing()||isDestroyed()||now10042==null
                   ||!roundOwner10042.equals(now10042.getUid())||!gameMemberVerified992)return;
                java.util.ArrayList<DocumentSnapshot> a=new java.util.ArrayList<>();
                java.util.ArrayList<String> submitted=new java.util.ArrayList<>();
                for(DocumentSnapshot doc:q.getDocuments()){
                    Long number=doc.getLong("roundId");
                    if(number!=null&&number.longValue()==expected
                       &&expectedType.equals(doc.getString("type"))
                       &&doc.getId().equals(doc.getString("uid"))
                       &&!doc.getMetadata().hasPendingWrites()){
                        a.add(doc);submitted.add(doc.getId());
                    }
                }
                if(a.size()<2){
                    settleFailed9954(automatic9954,expected,expectedType);
                    if(!automatic9954)toast("At least 2 real players must submit");
                    return;
                }
                state().get(com.google.firebase.firestore.Source.SERVER)
                  .addOnSuccessListener(serverRound->{
                    FirebaseUser serverNow10042=FirebaseAuth.getInstance().getCurrentUser();
                    if(isFinishing()||isDestroyed()||serverNow10042==null
                       ||!roundOwner10042.equals(serverNow10042.getUid())||!gameMemberVerified992)return;
                    if(!serverRound.exists()||roundId!=expected||
                        !expectedType.equals(type)||!"active".equals(status)
                        ||serverRound.getLong("roundId")==null||
                        serverRound.getLong("roundId").longValue()!=expected||
                        !expectedType.equals(serverRound.getString("type"))||
                        !"active".equals(serverRound.getString("status"))){
                        settleSucceeded9954(automatic9954);
                        if(!automatic9954)toast("Game round changed, refresh Party");
                        return;
                    }
                    final java.util.List<?> frozen=serverRound.get("participantUids")
                         instanceof java.util.List?
                         (java.util.List<?>)serverRound.get("participantUids"):null;
                    if(KingPartyRoundRoster9972.isOrdinaryGame(expectedType)
                       &&(!KingPartyRoundRoster9972.containsOnly(frozen,submitted)
                           ||!KingPartyAllPlayed9973.complete(expectedType,frozen,submitted))){
                        settleFailed9954(automatic9954,expected,expectedType);
                        if(!automatic9954)toast("Wait for ALL Ready players to submit; if one left use Cancel • no winner");
                        return;
                    }
                    if(KingPartyRoundRoster9972.isOrdinaryGame(expectedType)&&frozen!=null
                       &&!KingPartyAllPlayed9973.complete(expectedType,frozen,submitted)){
                        settleFailed9954(automatic9954,expected,expectedType);
                        if(!automatic9954)toast("Waiting for all "+frozen.size()+
                             " Ready players • received "+submitted.size()+" moves");
                        return;
                    }
                    String result=resultFor(a);
                    if(result.length()>300)result=result.substring(0,297)+"...";
                    final String finalResult=result;
                    java.util.Map<String,Object> values=new java.util.HashMap<>();
                    values.put("actorUid",me.getUid());
                    values.put("result",result);
                    values.put("status","closed");
                    values.put("updatedAt",FieldValue.serverTimestamp());
                    db.runTransaction(tx->{
                        DocumentSnapshot latest=tx.get(state());
                        if(!latest.exists()||latest.getLong("roundId")==null
                           ||latest.getLong("roundId").longValue()!=expected
                           ||!expectedType.equals(latest.getString("type"))
                           ||!"active".equals(latest.getString("status")))
                            throw new IllegalStateException("Round changed or already finished");
                        if(KingPartyRoundRoster9972.isOrdinaryGame(expectedType)){
                            Object current=latest.get("participantUids");
                            if(!(current instanceof java.util.List)
                               ||!frozen.equals(current)
                               ||!KingPartyAllPlayed9973.complete(expectedType,
                                  (java.util.List<?>)current,submitted))
                                throw new IllegalStateException("All Ready players must finish");
                            // Read actual current move documents as part of the SAME
                            // Firestore transaction as the result. This catches a
                            // late deletion/changed move after the prior SERVER read.
                            java.util.Map<String,DocumentSnapshot> priorMoves9973=new java.util.HashMap<>();
                            for(DocumentSnapshot prior9973:a)
                                priorMoves9973.put(prior9973.getId(),prior9973);
                            for(Object obj9973:(java.util.List<?>)current){
                                String uid9973=(String)obj9973;
                                // Immutable move persists after the sender leaves:
                                // also re-read real Party membership in this SAME
                                // winner transaction or do not declare a winner.
                                DocumentSnapshot liveMember9975=tx.get(
                                  room().collection("members").document(uid9973));
                                if(!KingPartyLiveRoster9975.member(uid9973,
                                      liveMember9975.exists(),
                                      s(liveMember9975.getString("uid"))))
                                    throw new IllegalStateException(
                                      "Ready player left Party • Cancel without winner");
                                DocumentSnapshot currentMove9973=tx.get(
                                  room().collection("game_moves").document(uid9973));
                                DocumentSnapshot expectedMove9973=priorMoves9973.get(uid9973);
                                if(!currentMove9973.exists()||expectedMove9973==null
                                  ||!uid9973.equals(s(currentMove9973.getString("uid")))
                                  ||!expectedType.equals(s(currentMove9973.getString("type")))
                                  ||currentMove9973.getLong("roundId")==null
                                  ||currentMove9973.getLong("roundId").longValue()!=expected
                                  ||!s(currentMove9973.getString("move")).equals(
                                     s(expectedMove9973.getString("move")))
                                  ||!java.util.Objects.equals(currentMove9973.get("score"),
                                     expectedMove9973.get("score")))
                                    throw new IllegalStateException("Game move changed before winner settlement");
                            }
                        }
                        if(KingPartyRoundRoster9972.isOrdinaryGame(expectedType)&&frozen!=null){
                            Object current=latest.get("participantUids");
                            if(!(current instanceof java.util.List)
                               ||!frozen.equals(current)
                               ||!KingRoomRoster9946.complete((java.util.List<?>)current,submitted))
                                throw new IllegalStateException("Ready roster changed; cannot publish winner");
                        }
                        tx.update(state(),values);
                        return null;
                    }).addOnSuccessListener(v->{
                        settleSucceeded9954(automatic9954);
                        postGameEvent750("🏁 "+finalResult.replace("\n"," • "));
                        new AlertDialog.Builder(this).setTitle("🏁 Round result")
                            .setMessage(finalResult)
                            .setPositiveButton("Rematch",(dialog,which)->rematch750())
                            .setNegativeButton("Close",null).show();
                    }).addOnFailureListener(e->{
                        String error9975=e==null?"":String.valueOf(e.getMessage());
                        if(error9975.contains("Ready player left Party")){
                            autoSettlementInFlight9954=false;
                            clearScheduledSettlement9954();
                            lastAutoSettlementAttempt9953=expected;
                            toast("A player left • Host: Cancel without winner");
                            return;
                        }
                        settleFailed9954(automatic9954,expected,expectedType);
                        if(!automatic9954)toast("Finish failed: "+error9975);
                    });
                }).addOnFailureListener(e->{
                    settleFailed9954(automatic9954,expected,expectedType);
                    if(!automatic9954)toast("Could not verify Ready players: "+e.getMessage());
                });
            }).addOnFailureListener(e->{
                settleFailed9954(automatic9954,expected,expectedType);
                if(!automatic9954)toast("Could not load round moves: "+e.getMessage());
            });
    }
    private String resultFor(List<DocumentSnapshot>a){
        if("treasure".equals(type)){List<String>w=new ArrayList<>();for(DocumentSnapshot d:a)if(KingTreasureHuntRules10100.hit(s(d.getString("move")),treasureChest10100))w.add(s(d.getString("name")));return "💎 Treasure Hunt • Chest "+treasureChest10100+"\n"+(w.isEmpty()?"No one found the treasure this round.":"🎉 Found by: "+join(w))+"\nNo diamonds or VIP rewards were granted.";}
        if("rps".equals(type)){
            java.util.List<String> names9945=new java.util.ArrayList<>();
            java.util.List<String> choices9945=new java.util.ArrayList<>();
            for(DocumentSnapshot d:a){
                names9945.add(s(d.getString("name")));
                choices9945.add(s(d.getString("move")));
            }
            return KingRpsRules9945.result(names9945,choices9945);
        }
        if("memory".equals(type)){int best=-1;List<String>w=new ArrayList<>();for(DocumentSnapshot d:a){Long n=d.getLong("score");int v=n==null?0:n.intValue();if(v>best){best=v;w.clear();w.add(s(d.getString("name"))+" • "+s(d.getString("move")));}else if(v==best)w.add(s(d.getString("name"))+" • "+s(d.getString("move")));}return "🧠 Memory Duel\n🏆 "+join(w);}if("domino".equals(type)){List<String>w=new ArrayList<>();for(DocumentSnapshot d:a){Long n=d.getLong("score");if(n!=null&&n.intValue()>=100)w.add(s(d.getString("name"))+" • "+s(d.getString("move")));}return "🁣 Chain end "+dominoTarget865+"\n🏆 "+join(w);}if("slot".equals(type)||"sheep".equals(type)||"zoo".equals(type)){int best=-1;List<String>w=new ArrayList<>();for(DocumentSnapshot d:a){Long n=d.getLong("score");int v=n==null?0:n.intValue();if(v>best){best=v;w.clear();w.add(s(d.getString("name"))+" • "+s(d.getString("move")));}else if(v==best)w.add(s(d.getString("name"))+" • "+s(d.getString("move")));}String label="slot".equals(type)?"🎰 Best reel score: ":"sheep".equals(type)?"🐑 Most real taps in 8s: ":"🦁 Highest zoo power: ";return label+best+"\n🏆 "+join(w);}if("reaction".equals(type)){int best=-1;List<String>w=new ArrayList<>();for(DocumentSnapshot d:a){Long n=d.getLong("score");int v=n==null?0:n.intValue();if(v>best){best=v;w.clear();w.add(s(d.getString("name"))+" • "+s(d.getString("move")));}else if(v==best)w.add(s(d.getString("name"))+" • "+s(d.getString("move")));}return "⚡ Fastest reaction\n🏆 "+join(w);}if("highlow".equals(type)){int draw=1+new java.security.SecureRandom().nextInt(100);String win=draw>=51?"High":"Low";List<String>w=new ArrayList<>();for(DocumentSnapshot d:a)if(win.equals(s(d.getString("move"))))w.add(s(d.getString("name")));return "🃏 Room card: "+draw+" • "+win+"\n🏆 "+join(w);}if("dice".equals(type)||"wheel".equals(type)){int best=-1;List<String>w=new ArrayList<>();for(DocumentSnapshot d:a){Long n=d.getLong("score");int v=n==null?0:n.intValue();if(v>best){best=v;w.clear();w.add(s(d.getString("name")));}else if(v==best)w.add(s(d.getString("name")));}return ("wheel".equals(type)?"🎡 Highest spin: ":"🎲 Highest roll: ")+best+"\n🏆 "+join(w);}if("number".equals(type)||"bingo".equals(type)){int cap="bingo".equals(type)?9:30,target=1+new java.security.SecureRandom().nextInt(cap),best=999;List<String>w=new ArrayList<>();for(DocumentSnapshot d:a){Long n=d.getLong("score");int v=n==null?0:n.intValue(),diff=Math.abs(v-target);if(diff<best){best=diff;w.clear();w.add(s(d.getString("name"))+" ("+v+")");}else if(diff==best)w.add(s(d.getString("name"))+" ("+v+")");}return ("bingo".equals(type)?"🎯 Room number: ":"🔢 Winning number: ")+target+"\n🏆 "+join(w);}if("coin".equals(type)){String win=new java.security.SecureRandom().nextBoolean()?"Heads":"Tails";List<String>w=new ArrayList<>();for(DocumentSnapshot d:a)if(win.equals(s(d.getString("move"))))w.add(s(d.getString("name")));return "🪙 "+win+" wins\n🏆 "+join(w);}Set<String>moves=new HashSet<>();Map<String,List<String>>names=new HashMap<>();for(DocumentSnapshot d:a){String m=s(d.getString("move")),n=s(d.getString("name"));moves.add(m);if(!names.containsKey(m))names.put(m,new ArrayList<>());names.get(m).add(n);}if(moves.size()==1||moves.size()==3)return "🤝 Draw • "+moves.toString();String win=moves.contains("Rock")&&moves.contains("Scissors")?"Rock":moves.contains("Scissors")&&moves.contains("Paper")?"Scissors":"Paper";return "🏆 "+win+" wins\n"+join(names.get(win));}
    private String join(List<String>x){if(x==null||x.isEmpty())return "No winner";StringBuilder b=new StringBuilder();for(String s:x){if(b.length()>0)b.append(", ");b.append(s==null||s.isEmpty()?"Player":s);}return b.toString();}
}

