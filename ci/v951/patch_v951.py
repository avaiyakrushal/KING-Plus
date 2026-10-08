from pathlib import Path
import sys

root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'

def replace_method(path, signature, replacement):
    p=pkg/path
    s=p.read_text()
    start=s.find(signature)
    if start<0:
        raise SystemExit(f'{path}: signature not found: {signature}')
    brace=s.find('{',start)
    if brace<0: raise SystemExit(f'{path}: opening brace not found')
    depth=0; state='code'; quote=''; esc=False; i=brace
    end=None
    while i<len(s):
        ch=s[i]; nx=s[i+1] if i+1<len(s) else ''
        if state=='string':
            if esc: esc=False
            elif ch=='\\': esc=True
            elif ch==quote: state='code'
        elif state=='line':
            if ch=='\n': state='code'
        elif state=='block':
            if ch=='*' and nx=='/': state='code'; i+=1
        else:
            if ch=='/' and nx=='/': state='line'; i+=1
            elif ch=='/' and nx=='*': state='block'; i+=1
            elif ch in ('"',"'"): state='string'; quote=ch
            elif ch=='{': depth+=1
            elif ch=='}':
                depth-=1
                if depth==0:
                    end=i+1; break
        i+=1
    if end is None: raise SystemExit(f'{path}: method end not found: {signature}')
    p.write_text(s[:start]+replacement+s[end:])

replace_method('PartyActivity.java','private void ktvQueuePanel()',r'''private void ktvQueuePanel(){
        if(!cloudRoom||db==null||roomId==null){karaokeDialog();return;}
        LinearLayout loadingBox951=new LinearLayout(this);loadingBox951.setGravity(Gravity.CENTER_VERTICAL);loadingBox951.setPadding(dp(18),dp(12),dp(18),dp(12));
        android.widget.ProgressBar loadingSpin951=new android.widget.ProgressBar(this);loadingBox951.addView(loadingSpin951,new LinearLayout.LayoutParams(dp(34),dp(34)));
        TextView loadingText951=tv("Loading KTV stage and queue…",13,0xff55515f,true);loadingBox951.addView(loadingText951,new LinearLayout.LayoutParams(0,dp(52),1));
        final AlertDialog loading951=new AlertDialog.Builder(this).setTitle("🎤 KTV").setView(loadingBox951).setCancelable(false).create();loading951.show();

        DocumentReference ktv=db.collection("live_rooms").document(roomId).collection("room_settings").document("ktv");
        ktv.get().addOnSuccessListener(state->db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(120).get().addOnSuccessListener(snap->{
            if(loading951.isShowing())loading951.dismiss();
            boolean active=Boolean.TRUE.equals(state.getBoolean("active"));String nowSong=str(state,"song","");String singer=str(state,"singerName","");
            String currentRequest=str(state,"requestEventId","");Set<String> completedRequests951=new HashSet<>();Object completedRaw951=state.get("completedRequestIds");if(completedRaw951 instanceof List)for(Object x:(List<?>)completedRaw951)if(x!=null)completedRequests951.add(String.valueOf(x));
            List<DocumentSnapshot> waiting951=new ArrayList<>();
            for(DocumentSnapshot req:snap.getDocuments())if("song".equals(req.getString("type"))&&!req.getId().equals(currentRequest)&&!completedRequests951.contains(req.getId()))waiting951.add(req);

            LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(14),dp(10),dp(14),dp(12));root.setBackgroundColor(0xff17131f);
            TextView live951=tv(active?"● LIVE KTV  •  "+waiting951.size()+" waiting":"○ KTV STAGE IDLE  •  "+waiting951.size()+" waiting",11,active?0xff65e6a6:0xffb9afc8,true);live951.setGravity(Gravity.CENTER);live951.setBackground(bg(active?0xff183c31:0xff282332,14));root.addView(live951,new LinearLayout.LayoutParams(-1,dp(36)));

            LinearLayout stage=new LinearLayout(this);stage.setOrientation(LinearLayout.VERTICAL);stage.setGravity(Gravity.CENTER);stage.setPadding(dp(10),dp(8),dp(10),dp(8));stage.setBackground(bg(active?0xff452858:0xff24202c,18));
            TextView mic=tv(active?"🎤":"🎙",36,Color.WHITE,false);mic.setGravity(Gravity.CENTER);stage.addView(mic,new LinearLayout.LayoutParams(-1,dp(50)));
            TextView singerView=tv(active?singer:"KTV Stage",18,active?0xffffd75a:Color.WHITE,true);singerView.setGravity(Gravity.CENTER);stage.addView(singerView,new LinearLayout.LayoutParams(-1,dp(32)));
            TextView songView=tv(active?(nowSong.isEmpty()?"Live performance":nowSong):"Request a song to enter the queue",12,MUTED,true);songView.setGravity(Gravity.CENTER);stage.addView(songView,new LinearLayout.LayoutParams(-1,dp(26)));
            TextView stageState951=tv(active?"Mic ON • live inside Party room":"Waiting for the next singer",10,active?0xff7beab7:MUTED,true);stageState951.setGravity(Gravity.CENTER);stage.addView(stageState951,new LinearLayout.LayoutParams(-1,dp(22)));
            LinearLayout.LayoutParams stageLp951=new LinearLayout.LayoutParams(-1,dp(138));stageLp951.setMargins(0,dp(7),0,0);root.addView(stage,stageLp951);

            LinearLayout actions=new LinearLayout(this);actions.setGravity(Gravity.CENTER);actions.setPadding(0,dp(6),0,dp(6));
            TextView request=tv("＋ Request Song",11,Color.WHITE,true);request.setGravity(Gravity.CENTER);request.setBackground(bg(0xff553979,12));request.setOnClickListener(v->karaokeDialog());LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(0,dp(42),1);ap.setMargins(dp(3),0,dp(3),0);actions.addView(request,ap);
            if(isModerator()){TextView control=tv(active?"⏹ End Singer":"🎛 Music",11,Color.WHITE,true);control.setGravity(Gravity.CENTER);control.setBackground(bg(0xff4a395c,12));control.setOnClickListener(v->{if(active){String finishedRequest951=str(state,"requestEventId","");String finishedSong951=str(state,"song","");String finishedSinger951=str(state,"singerName","Guest");Map<String,Object>end=new HashMap<>();end.put("active",false);end.put("endedAt",FieldValue.serverTimestamp());end.put("endedBy",user==null?"":user.getUid());end.put("requestEventId","");if(!finishedRequest951.isEmpty())end.put("completedRequestIds",FieldValue.arrayUnion(finishedRequest951));ktv.set(end,SetOptions.merge()).addOnSuccessListener(x->{addEvent("ktv_end",finishedSinger951+" finished KTV • "+finishedSong951);ktvQueuePanel();}).addOnFailureListener(e->toast("Could not end KTV: "+msg(e)));}else musicPanel();});actions.addView(control,new LinearLayout.LayoutParams(ap));}
            TextView history951=tv("🕘 History",11,Color.WHITE,true);history951.setGravity(Gravity.CENTER);history951.setBackground(bg(0xff3e3550,12));history951.setOnClickListener(v->ktvHistory940());actions.addView(history951,new LinearLayout.LayoutParams(ap));
            root.addView(actions,new LinearLayout.LayoutParams(-1,dp(54)));

            TextView qTitle=tv("Song Request Queue  •  "+waiting951.size(),14,Color.WHITE,true);qTitle.setPadding(dp(2),dp(6),0,dp(5));root.addView(qTitle,new LinearLayout.LayoutParams(-1,dp(38)));
            ScrollView scroll=new ScrollView(this);LinearLayout queue=new LinearLayout(this);queue.setOrientation(LinearLayout.VERTICAL);scroll.addView(queue);
            int shown=0;
            for(DocumentSnapshot req:waiting951){
                String raw=str(req,"text","Song request");String requestedSong=raw;int mark=raw.indexOf(" requested 🎵 ");if(mark>=0)requestedSong=raw.substring(mark+" requested 🎵 ".length()).trim();
                final String song=requestedSong;final DocumentSnapshot requestDoc=req;
                LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(10),dp(6),dp(8),dp(6));row.setBackground(bg(0xff24202e,12));
                TextView icon=tv("🎵",22,Color.WHITE,false);icon.setGravity(Gravity.CENTER);row.addView(icon,new LinearLayout.LayoutParams(dp(42),dp(46)));
                LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);TextView songText=tv(song,13,Color.WHITE,true);songText.setSingleLine(true);TextView userText=tv(str(req,"actorName","Guest"),10,MUTED,false);info.addView(songText,new LinearLayout.LayoutParams(-1,dp(24)));info.addView(userText,new LinearLayout.LayoutParams(-1,dp(18)));row.addView(info,new LinearLayout.LayoutParams(0,dp(46),1));
                TextView next=tv(isModerator()?"START ›":"WAIT",10,isModerator()?0xffffd75a:MUTED,true);next.setGravity(Gravity.CENTER);row.addView(next,new LinearLayout.LayoutParams(dp(64),dp(46)));
                row.setOnClickListener(v->{if(!isModerator()){toast("Host/co-host selects the next singer");return;}new AlertDialog.Builder(this).setTitle("Start KTV singer?").setMessage(str(requestDoc,"actorName","Guest")+" • "+song).setPositiveButton("Start",(d,w)->startKtvRequestVisual940(requestDoc,song,ktv)).setNegativeButton("Cancel",null).show();});
                LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(60));rp.setMargins(0,dp(3),0,dp(3));queue.addView(row,rp);if(++shown>=20)break;
            }
            if(shown==0){TextView empty=tv(active?"Queue empty • current singer is on stage":"No waiting song requests\\nTap Request Song to be first in queue",12,MUTED,true);empty.setGravity(Gravity.CENTER);queue.addView(empty,new LinearLayout.LayoutParams(-1,dp(86)));}
            root.addView(scroll,new LinearLayout.LayoutParams(-1,Math.min(dp(350),Math.max(dp(92),shown*dp(66)+dp(24)))));
            AlertDialog dialog951=new AlertDialog.Builder(this).setTitle("🎤 KTV Stage").setView(root).setPositiveButton("Refresh",(d,w)->ktvQueuePanel()).setNegativeButton("Close",null).create();
            dialog951.setOnShowListener(v->{android.view.Window win=dialog951.getWindow();if(win!=null){int h=Math.min(dp(690),(int)(getResources().getDisplayMetrics().heightPixels*.82f));win.setLayout(-1,h);}});
            dialog951.show();
        }).addOnFailureListener(e->{if(loading951.isShowing())loading951.dismiss();new AlertDialog.Builder(this).setTitle("KTV unavailable").setMessage("Could not load the song queue.\\n\\n"+msg(e)).setPositiveButton("Retry",(d,w)->ktvQueuePanel()).setNegativeButton("Close",null).show();})).addOnFailureListener(e->{if(loading951.isShowing())loading951.dismiss();new AlertDialog.Builder(this).setTitle("KTV unavailable").setMessage("Could not load the KTV stage state.\\n\\n"+msg(e)).setPositiveButton("Retry",(d,w)->ktvQueuePanel()).setNegativeButton("Close",null).show();});
    }''')

replace_method('PartyActivity.java','private void audioPkPanel700()',r'''private void audioPkPanel700(){
        if(!cloudRoom||db==null||roomId==null){toast("Live room required");return;}
        DocumentReference state=db.collection("live_rooms").document(roomId).collection("room_settings").document("audio_pk");
        state.get().addOnSuccessListener(pk->{
            boolean active=Boolean.TRUE.equals(pk.getBoolean("active"));
            com.google.firebase.Timestamp started=pk.getTimestamp("startedAt");
            Long durationRaw=pk.getLong("durationSec");long duration=durationRaw==null?180L:Math.max(30L,durationRaw);
            long elapsed=started==null?0L:Math.max(0L,(System.currentTimeMillis()-started.toDate().getTime())/1000L);
            long remain=Math.max(0L,duration-elapsed);
            if(active&&remain<=0){active=false;if(isModerator())state.update("active",false,"endedAt",FieldValue.serverTimestamp()).addOnFailureListener(x->{});}
            final boolean pkActive=active;final long pkRemain=remain;final long pkDuration=duration;final com.google.firebase.Timestamp pkStarted=started;
            db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(250).get().addOnSuccessListener(ev->{
                long red=0,blue=0;int gifts=0;
                for(DocumentSnapshot e:ev.getDocuments()){
                    if(!"gift".equals(e.getString("type")))continue;
                    com.google.firebase.Timestamp at=e.getTimestamp("createdAt");if(pkStarted!=null&&at!=null&&at.compareTo(pkStarted)<0)continue;
                    Long raw=e.getLong("giftValue");long score=raw==null?0:Math.max(0,raw);String actor=e.getString("actorUid");Integer seat=null;
                    if(actor!=null)for(Map.Entry<Integer,String>x:seatUids.entrySet())if(actor.equals(x.getValue())){seat=x.getKey();break;}
                    Object customRaw951=pk.get("seatTeams");Map<String,Object> customTeams951=customRaw951 instanceof Map?(Map<String,Object>)customRaw951:null;String customTeam951=seat==null||customTeams951==null?null:String.valueOf(customTeams951.get(String.valueOf(seat)));
                    if("red".equalsIgnoreCase(customTeam951))red+=score;else if("blue".equalsIgnoreCase(customTeam951))blue+=score;else if(seat!=null&&seat%2==0)red+=score;else if(seat!=null)blue+=score;else if((gifts%2)==0)red+=score;else blue+=score;gifts++;
                }
                final long redScore=red,blueScore=blue;
                LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(14),dp(10),dp(14),dp(14));root.setBackgroundColor(0xff15121e);
                TextView stateChip951=tv(pkActive?"● LIVE AUDIO PK":"○ PK RESULT / READY",11,pkActive?0xff65e6a6:0xffc7bdd5,true);stateChip951.setGravity(Gravity.CENTER);stateChip951.setBackground(bg(pkActive?0xff183c31:0xff292432,14));root.addView(stateChip951,new LinearLayout.LayoutParams(-1,dp(36)));
                TextView timer=tv(pkActive?String.format(java.util.Locale.US,"⏱  %02d:%02d",pkRemain/60,pkRemain%60):"🏁  PK RESULT",19,pkActive?0xffffd84d:Color.WHITE,true);timer.setGravity(Gravity.CENTER);root.addView(timer,new LinearLayout.LayoutParams(-1,dp(46)));
                android.widget.ProgressBar progress951=new android.widget.ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress951.setMax((int)Math.min(Integer.MAX_VALUE,Math.max(1L,pkDuration)));progress951.setProgress((int)Math.min(pkDuration,Math.max(0L,pkDuration-pkRemain)));LinearLayout.LayoutParams pp951=new LinearLayout.LayoutParams(-1,dp(10));pp951.setMargins(dp(8),0,dp(8),dp(8));root.addView(progress951,pp951);

                LinearLayout teams=new LinearLayout(this);teams.setGravity(Gravity.CENTER);
                LinearLayout redCard=new LinearLayout(this);redCard.setOrientation(LinearLayout.VERTICAL);redCard.setGravity(Gravity.CENTER);redCard.setBackground(bg(0xff5c2230,16));
                TextView redTitle=tv("🔴 RED",15,Color.WHITE,true);redTitle.setGravity(Gravity.CENTER);redCard.addView(redTitle,new LinearLayout.LayoutParams(-1,dp(30)));
                TextView redVal=tv(compactNumber(redScore),27,0xffffc5cd,true);redVal.setGravity(Gravity.CENTER);redCard.addView(redVal,new LinearLayout.LayoutParams(-1,dp(48)));
                TextView vs=tv("VS",18,0xffffd84d,true);vs.setGravity(Gravity.CENTER);
                LinearLayout blueCard=new LinearLayout(this);blueCard.setOrientation(LinearLayout.VERTICAL);blueCard.setGravity(Gravity.CENTER);blueCard.setBackground(bg(0xff203b66,16));
                TextView blueTitle=tv("🔵 BLUE",15,Color.WHITE,true);blueTitle.setGravity(Gravity.CENTER);blueCard.addView(blueTitle,new LinearLayout.LayoutParams(-1,dp(30)));
                TextView blueVal=tv(compactNumber(blueScore),27,0xffc5dcff,true);blueVal.setGravity(Gravity.CENTER);blueCard.addView(blueVal,new LinearLayout.LayoutParams(-1,dp(48)));
                teams.addView(redCard,new LinearLayout.LayoutParams(0,dp(86),1));teams.addView(vs,new LinearLayout.LayoutParams(dp(52),dp(86)));teams.addView(blueCard,new LinearLayout.LayoutParams(0,dp(86),1));root.addView(teams,new LinearLayout.LayoutParams(-1,dp(92)));

                LinearLayout bar=new LinearLayout(this);TextView rb=new TextView(this);rb.setBackgroundColor(0xffe34d66);TextView bb=new TextView(this);bb.setBackgroundColor(0xff4b85e5);bar.addView(rb,new LinearLayout.LayoutParams(0,dp(10),(float)Math.max(1L,redScore)));bar.addView(bb,new LinearLayout.LayoutParams(0,dp(10),(float)Math.max(1L,blueScore)));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(10));bp.setMargins(0,dp(10),0,dp(8));root.addView(bar,bp);
                String result=redScore==blueScore?"🤝 Draw":redScore>blueScore?"🏆 Red team leads":"🏆 Blue team leads";if(!pkActive)result=redScore==blueScore?"🤝 PK ended in a draw":redScore>blueScore?"🏆 Red team wins":"🏆 Blue team wins";
                TextView resultView=tv(result,15,Color.WHITE,true);resultView.setGravity(Gravity.CENTER);root.addView(resultView,new LinearLayout.LayoutParams(-1,dp(42)));
                TextView giftCount=tv(gifts==0?(pkActive?"No PK gifts yet • send a gift to score":"No gift score recorded in the last PK"):"🎁 PK gifts  "+gifts,11,MUTED,true);giftCount.setGravity(Gravity.CENTER);root.addView(giftCount,new LinearLayout.LayoutParams(-1,dp(30)));

                LinearLayout quick951=new LinearLayout(this);quick951.setGravity(Gravity.CENTER);TextView hist951=tv("🕘 History",11,Color.WHITE,true);hist951.setGravity(Gravity.CENTER);hist951.setBackground(bg(0xff322b42,12));hist951.setOnClickListener(v->pkHistory940());TextView gift951=tv("🎁 Send Gift",11,Color.WHITE,true);gift951.setGravity(Gravity.CENTER);gift951.setBackground(bg(0xff523152,12));gift951.setOnClickListener(v->giftShopPanel());LinearLayout.LayoutParams qp951=new LinearLayout.LayoutParams(0,dp(42),1);qp951.setMargins(dp(3),dp(4),dp(3),0);quick951.addView(hist951,qp951);quick951.addView(gift951,new LinearLayout.LayoutParams(qp951));root.addView(quick951,new LinearLayout.LayoutParams(-1,dp(50)));

                AlertDialog.Builder b=new AlertDialog.Builder(this).setTitle("⚔️ Audio PK").setView(root).setPositiveButton(pkActive?"Refresh":"Close",(d,w)->{if(pkActive)audioPkPanel700();}).setNegativeButton(pkActive?"Close":null,null);
                if(isModerator()){
                    if(pkActive)b.setNeutralButton("End PK",(d,w)->{Map<String,Object>end=new HashMap<>();end.put("active",false);end.put("endedAt",FieldValue.serverTimestamp());end.put("endedBy",user==null?"":user.getUid());state.set(end,SetOptions.merge()).addOnSuccessListener(v->{addEvent("audio_pk_end",safeName()+" ended Audio PK");audioPkPanel700();});});
                    else b.setNeutralButton("Start 3 min PK",(d,w)->{Map<String,Object>start=new HashMap<>();start.put("active",true);start.put("durationSec",180);start.put("startedAt",FieldValue.serverTimestamp());start.put("startedBy",user==null?"":user.getUid());start.put("startedByName",safeName());state.set(start,SetOptions.merge()).addOnSuccessListener(v->{addEvent("audio_pk",safeName()+" started 3-minute Audio PK ⚔️");audioPkPanel700();}).addOnFailureListener(e->toast("PK start failed: "+msg(e)));});
                }
                b.show();
            }).addOnFailureListener(e->new AlertDialog.Builder(this).setTitle("PK score unavailable").setMessage(msg(e)).setPositiveButton("Retry",(d,w)->audioPkPanel700()).setNegativeButton("Close",null).show());
        }).addOnFailureListener(e->new AlertDialog.Builder(this).setTitle("PK state unavailable").setMessage(msg(e)).setPositiveButton("Retry",(d,w)->audioPkPanel700()).setNegativeButton("Close",null).show());
    }''')

replace_method('PartyActivity.java','private void roomRankingDialog()',r'''private void roomRankingDialog(){
        if(!cloudRoom||db==null){new AlertDialog.Builder(this).setTitle("🏆 Room ranking").setMessage("Live gift ranking is available in Firebase rooms.").setPositiveButton("OK",null).show();return;}
        db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(250).get()
            .addOnSuccessListener(snap->{
                Map<String,Long>score=new HashMap<>();Map<String,Integer>count=new HashMap<>();
                for(DocumentSnapshot d:snap.getDocuments())if("gift".equals(d.getString("type"))){String n=str(d,"actorName","User");Long v=d.getLong("giftValue");long value=v==null?1:Math.max(1,v);score.put(n,(score.containsKey(n)?score.get(n):0L)+value);count.put(n,(count.containsKey(n)?count.get(n):0)+1);}
                List<Map.Entry<String,Long>>list=new ArrayList<>(score.entrySet());java.util.Collections.sort(list,(a,b)->Long.compare(b.getValue(),a.getValue()));
                LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(12),dp(8),dp(12),dp(12));root.setBackgroundColor(0xfff7f5fa);
                TextView sub951=tv("Top supporters • based on gift value in this room",11,0xff77717f,true);sub951.setGravity(Gravity.CENTER);root.addView(sub951,new LinearLayout.LayoutParams(-1,dp(34)));
                if(list.isEmpty()){TextView empty=tv("🏆\\nNo gift ranking yet\\nSend the first gift to enter the board.",14,0xff81798b,true);empty.setGravity(Gravity.CENTER);root.addView(empty,new LinearLayout.LayoutParams(-1,dp(150)));}
                else{
                    LinearLayout podium=new LinearLayout(this);podium.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
                    int[] order={1,0,2};String[] medals={"🥇","🥈","🥉"};
                    for(int pos:order){if(pos>=list.size())continue;Map.Entry<String,Long> e=list.get(pos);LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setBackground(bg(pos==0?0xffffefad:pos==1?0xffe9edf4:0xffffe1c4,16));TextView med=tv(medals[pos],25,0xff333333,false);med.setGravity(Gravity.CENTER);card.addView(med,new LinearLayout.LayoutParams(-1,dp(38)));TextView nm=tv(e.getKey(),12,0xff2c2930,true);nm.setGravity(Gravity.CENTER);nm.setSingleLine(true);card.addView(nm,new LinearLayout.LayoutParams(-1,dp(26)));TextView val=tv("💎 "+compactNumber(e.getValue()),11,0xff725b00,true);val.setGravity(Gravity.CENTER);card.addView(val,new LinearLayout.LayoutParams(-1,dp(24)));int h=pos==0?dp(112):dp(98);LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,h,1);cp.setMargins(dp(3),0,dp(3),0);podium.addView(card,cp);}
                    root.addView(podium,new LinearLayout.LayoutParams(-1,dp(120)));
                    ScrollView sc=new ScrollView(this);LinearLayout rows=new LinearLayout(this);rows.setOrientation(LinearLayout.VERTICAL);sc.addView(rows);
                    for(int i=3;i<list.size()&&i<20;i++){Map.Entry<String,Long>e=list.get(i);LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(10),0,dp(10),0);row.setBackground(bg(Color.WHITE,12));TextView rank=tv(String.valueOf(i+1),13,0xff81798b,true);rank.setGravity(Gravity.CENTER);row.addView(rank,new LinearLayout.LayoutParams(dp(38),dp(48)));TextView name=tv(e.getKey(),13,0xff2c2930,true);name.setSingleLine(true);row.addView(name,new LinearLayout.LayoutParams(0,dp(48),1));TextView val=tv("💎"+compactNumber(e.getValue())+"  • "+count.get(e.getKey())+" gifts",11,0xff756d7c,true);val.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);row.addView(val,new LinearLayout.LayoutParams(dp(145),dp(48)));LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(50));rp.setMargins(0,dp(2),0,dp(2));rows.addView(row,rp);}
                    root.addView(sc,new LinearLayout.LayoutParams(-1,Math.min(dp(360),Math.max(dp(80),(Math.max(0,Math.min(20,list.size())-3))*dp(52)))));
                }
                AlertDialog dlg951=new AlertDialog.Builder(this).setTitle("🏆 Room Gift Ranking").setView(root).setPositiveButton("Refresh",(d,w)->roomRankingDialog()).setNegativeButton("Close",null).create();dlg951.show();
            })
            .addOnFailureListener(e->new AlertDialog.Builder(this).setTitle("Ranking unavailable").setMessage(msg(e)).setPositiveButton("Retry",(d,w)->roomRankingDialog()).setNegativeButton("Close",null).show());
    }''')

replace_method('PartyActivity.java','private void familyPartyPanel610()',r'''private void familyPartyPanel610(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(14),dp(10),dp(14),dp(12));root.setBackgroundColor(0xfffaf7fc);
        TextView hero951=tv("👑  FAMILY PARTY",18,0xff302839,true);hero951.setGravity(Gravity.CENTER);hero951.setBackground(bg(0xffffe9b8,16));root.addView(hero951,new LinearLayout.LayoutParams(-1,dp(52)));
        TextView room951=tv(roomName+"\\n"+(cloudRoom?liveMemberCount:Math.max(1,memberNames.size()))+" online  •  "+seatNames.size()+"/"+maxSeats+" seats",13,0xff625b69,true);room951.setGravity(Gravity.CENTER);root.addView(room951,new LinearLayout.LayoutParams(-1,dp(62)));
        LinearLayout grid951=new LinearLayout(this);grid951.setOrientation(LinearLayout.VERTICAL);
        String[] labels951={"👑 Family Center","👥 Invite Family / Friends","🎉 Family Activity","📢 Family Announcement","🏠 Room Info","🎁 Family Gifts"};
        for(int i=0;i<labels951.length;i+=2){LinearLayout row=new LinearLayout(this);for(int j=i;j<Math.min(i+2,labels951.length);j++){final int k=j;TextView v=tv(labels951[j],12,0xff382f43,true);v.setGravity(Gravity.CENTER);v.setBackground(bg(Color.WHITE,13));v.setOnClickListener(x->{if(k==0)openCommunityHub700("Family");else if(k==1)shareRoom();else if(k==2){addEvent("family_activity",safeName()+" started a Family Party activity");toast("Family activity posted to room");}else if(k==3)roomNoticePanel();else if(k==4)new AlertDialog.Builder(this).setTitle("Family Party Room").setMessage("Room: "+roomName+"\\nOnline: "+(cloudRoom?liveMemberCount:Math.max(1,memberNames.size()))+"\\nSeats: "+seatNames.size()+"/"+maxSeats).setPositiveButton("Family Center",(d,w)->openCommunityHub700("Family")).setNegativeButton("Close",null).show();else giftShopPanel();});LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(58),1);cp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(v,cp);}grid951.addView(row,new LinearLayout.LayoutParams(-1,dp(64)));}
        root.addView(grid951,new LinearLayout.LayoutParams(-1,-2));
        TextView note951=tv("Family activities, gifts and chat stay inside KING Plus. Open Family Center for members, Lucky Bag and Family Chat.",11,0xff81798b,false);note951.setPadding(dp(8),dp(8),dp(8),dp(8));root.addView(note951,new LinearLayout.LayoutParams(-1,dp(58)));
        new AlertDialog.Builder(this).setTitle("💞 Family Party").setView(root).setNegativeButton("Close",null).show();
    }''')

replace_method('CommunityHubActivity.java','private void showFamily(String code)',r'''private void showFamily(String code){
        DocumentReference r=db.collection("families").document(code);
        r.get().addOnSuccessListener(f->{
            if(!f.exists()){clearFamily();render("Family");return;}
            String name=safe(f.getString("name"),"KING Family"),owner=safe(f.getString("ownerUid"),"");
            Long levelRaw=f.getLong("level"),treasuryRaw=f.getLong("treasury");long familyLevel=levelRaw==null?1:Math.max(1,levelRaw),treasury=treasuryRaw==null?0:Math.max(0,treasuryRaw);

            LinearLayout hero951=new LinearLayout(this);hero951.setOrientation(LinearLayout.VERTICAL);hero951.setGravity(Gravity.CENTER);hero951.setPadding(dp(12),dp(10),dp(12),dp(10));hero951.setBackground(bg(0xffffefd0,18));
            TextView familyName951=tv("👑 "+name,21,DARK,true);familyName951.setGravity(Gravity.CENTER);hero951.addView(familyName951,new LinearLayout.LayoutParams(-1,dp(36)));
            TextView code951=tv("Family Code  "+code,12,MUTED,true);code951.setGravity(Gravity.CENTER);hero951.addView(code951,new LinearLayout.LayoutParams(-1,dp(28)));body.addView(hero951,new LinearLayout.LayoutParams(-1,dp(82)));

            LinearLayout stats951=new LinearLayout(this);stats951.setGravity(Gravity.CENTER);TextView memberStat951=familyStat951("👥","…","Members");TextView levelStat951=familyStat951("⭐",String.valueOf(familyLevel),"Level");TextView treasuryStat951=familyStat951("💎",String.valueOf(treasury),"Treasury");TextView activityStat951=familyStat951("🎉","…","Activity");stats951.addView(memberStat951,new LinearLayout.LayoutParams(0,dp(76),1));stats951.addView(levelStat951,new LinearLayout.LayoutParams(0,dp(76),1));stats951.addView(treasuryStat951,new LinearLayout.LayoutParams(0,dp(76),1));stats951.addView(activityStat951,new LinearLayout.LayoutParams(0,dp(76),1));LinearLayout.LayoutParams statLp951=new LinearLayout.LayoutParams(-1,dp(82));statLp951.setMargins(0,dp(8),0,dp(8));body.addView(stats951,statLp951);

            LinearLayout acts=new LinearLayout(this);TextView share=button("Share Code",()->{Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,"Join my KING Plus Family "+name+" • Code "+code);startActivity(Intent.createChooser(i,"Share Family"));});acts.addView(share,new LinearLayout.LayoutParams(0,dp(46),1));if(!owner.equals(me.getUid())){TextView leave=button("Leave",()->new AlertDialog.Builder(this).setTitle("Leave "+name+"?").setMessage("You can rejoin later with the Family code.").setPositiveButton("Leave",(d,w)->r.collection("members").document(me.getUid()).delete().addOnSuccessListener(v->{clearFamily();render("Family");})).setNegativeButton("Cancel",null).show());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(46),1);lp.setMargins(dp(6),0,0,0);acts.addView(leave,lp);}body.addView(acts);
            TextView luckyBag951=button("🧧 Family Lucky Bag",()->familyLuckyBag940(code));LinearLayout.LayoutParams lbp951=new LinearLayout.LayoutParams(-1,dp(48));lbp951.setMargins(0,dp(8),0,dp(8));body.addView(luckyBag951,lbp951);

            body.addView(tv("Members",16,DARK,true));LinearLayout memberList951=new LinearLayout(this);memberList951.setOrientation(LinearLayout.VERTICAL);body.addView(memberList951,new LinearLayout.LayoutParams(-1,-2));
            r.collection("members").get().addOnSuccessListener(s->{memberStat951.setText("👥\\n"+s.size()+"\\nMembers");if(s.isEmpty())memberList951.addView(tv("No members loaded",13,MUTED,false));for(DocumentSnapshot d:s.getDocuments()){String uid=d.getId(),n=safe(d.getString("name"),"User"),role=safe(d.getString("role"),"member");TextView x=tv(("owner".equals(role)?"👑 ":"👤 ")+n+" • "+role,14,DARK,true);memberList951.addView(x,new LinearLayout.LayoutParams(-1,dp(44)));if(owner.equals(me.getUid())&&!uid.equals(me.getUid()))x.setOnLongClickListener(v->{new AlertDialog.Builder(this).setTitle(n).setMessage("Remove from Family?").setPositiveButton("Remove",(a,b)->r.collection("members").document(uid).delete().addOnSuccessListener(q->render("Family"))).setNegativeButton("Cancel",null).show();return true;});}}).addOnFailureListener(e->memberStat951.setText("👥\\n—\\nMembers"));

            r.collection("activities").orderBy("createdAt",Query.Direction.DESCENDING).limit(50).get().addOnSuccessListener(a->activityStat951.setText("🎉\\n"+a.size()+"\\nActivity")).addOnFailureListener(e->activityStat951.setText("🎉\\n—\\nActivity"));

            body.addView(tv("Family Chat",16,DARK,true));LinearLayout chat=new LinearLayout(this);chat.setOrientation(LinearLayout.VERTICAL);TextView chatLoading951=tv("Loading family messages…",12,MUTED,false);chat.addView(chatLoading951,new LinearLayout.LayoutParams(-1,dp(42)));body.addView(chat);
            r.collection("messages").orderBy("createdAt",Query.Direction.DESCENDING).limit(30).get().addOnSuccessListener(s->{chat.removeAllViews();List<DocumentSnapshot>ds=new ArrayList<>(s.getDocuments());Collections.reverse(ds);if(ds.isEmpty())chat.addView(tv("No Family messages yet. Say hello 👋",13,MUTED,false));for(DocumentSnapshot d:ds)chat.addView(tv(safe(d.getString("name"),"User")+": "+safe(d.getString("text"),""),13,DARK,false));}).addOnFailureListener(e->{chat.removeAllViews();chat.addView(tv("Family Chat unavailable right now.",13,MUTED,false));});
            LinearLayout comp=new LinearLayout(this);EditText msg=new EditText(this);msg.setHint("Message family…");msg.setSingleLine(true);msg.setBackground(bg(CARD,14));comp.addView(msg,new LinearLayout.LayoutParams(0,dp(48),1));TextView send=button("Send",()->{String t=msg.getText().toString().trim();if(t.isEmpty())return;Map<String,Object>m=new HashMap<>();m.put("uid",me.getUid());m.put("name",displayName);m.put("text",t);m.put("createdAt",FieldValue.serverTimestamp());r.collection("messages").add(m).addOnSuccessListener(v->{msg.setText("");render("Family");}).addOnFailureListener(e->toast("Message failed"));});LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(dp(72),dp(48));sp.setMargins(dp(6),0,0,0);comp.addView(send,sp);body.addView(comp);
        }).addOnFailureListener(e->body.addView(tv("Family unavailable • pull back and retry",14,MUTED,false)));
    }''')

# Add family stat helper before lucky-bag helper.
p=pkg/'CommunityHubActivity.java'; s=p.read_text()
marker='''    private int familyBagReward940(String packetId,int total,int slots){'''
helper='''    private TextView familyStat951(String icon,String value,String label){TextView t=tv(icon+"\\n"+value+"\\n"+label,11,DARK,true);t.setGravity(Gravity.CENTER);t.setBackground(bg(CARD,13));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(72),1);lp.setMargins(dp(2),0,dp(2),0);return t;}

'''
if marker not in s: raise SystemExit('CommunityHub family stat marker missing')
s=s.replace(marker,helper+marker,1);p.write_text(s)

gradle=root/'app/build.gradle'; g=gradle.read_text()
old="versionCode 141; versionName '9.5.0-parity-batch1'"
if old not in g: raise SystemExit('v9.5.0 version marker missing')
gradle.write_text(g.replace(old,"versionCode 142; versionName '9.5.1-parity-batch2'",1))

print('KING Plus v9.5.1 KTV/PK/Family/Rank parity batch applied')
