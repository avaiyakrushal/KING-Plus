from pathlib import Path
import re

P=Path('app/src/main/java/com/kingplus/social/PartyActivity.java')
B=Path('app/build.gradle')
s=P.read_text(encoding='utf-8')

s=s.replace('if(existing!=null&&!existing.equals(user==null?null:user.getUid())){seatUserMenu(no);return;}',
            'if(existing!=null){openSeatProfile(no);return;}',1)
s=s.replace('else if (seatNames.get(no)!=null) { seatUserMenu(no); return; }',
            'else if (seatNames.get(no)!=null) { openSeatProfile(no); return; }',1)

start=s.index('    private void toolsPanel(){')
end=s.index('\n    private TextView toolTile(', start)
new_tools=r'''    private void toolsPanel(){
        ScrollView sv=new ScrollView(this);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(16),dp(14),dp(16),dp(18));root.setBackgroundColor(0xff242333);sv.addView(root);
        TextView playTitle=tv("Play Center",22,Color.WHITE,true);root.addView(playTitle,new LinearLayout.LayoutParams(-1,dp(46)));
        String[] pIcons={"⭐","🌷","🥊","💣","📦","🎡","🎯","⚔️","💌"};
        String[] pLabels={"Counter","Guess It","Truth or Dare","Pass the Bomb","Fan Box","Wheel Challenge","Party Wheel","Room Battle","Vote"};
        addToolGrid(root,pIcons,pLabels,true);
        TextView toolsTitle=tv("Tools",22,Color.WHITE,true);LinearLayout.LayoutParams ttp=new LinearLayout.LayoutParams(-1,dp(52));ttp.topMargin=dp(10);root.addView(toolsTitle,ttp);
        String[] icons={"🎟","🪑","✋","💬","🎵","🎨","📋","👑","🏰"};
        String[] labels={"Events","Room seat","Enable queue","Private chat","Music","Atmosphere","Income","Share Family","Theme Room"};
        addToolGrid(root,icons,labels,false);
        AlertDialog dialog=new AlertDialog.Builder(this).setView(sv).create();
        dialog.setOnShowListener(v->{android.view.Window w=dialog.getWindow();if(w!=null){w.setLayout(-1,(int)(getResources().getDisplayMetrics().heightPixels*.74f));w.setGravity(Gravity.BOTTOM);}});
        dialog.show();
    }

    private void addToolGrid(LinearLayout host,String[] icons,String[] labels,boolean playCenter){
        int index=0;
        while(index<labels.length){
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);
            for(int c=0;c<5;c++){
                if(index>=labels.length){View spacer=new View(this);row.addView(spacer,new LinearLayout.LayoutParams(0,dp(94),1));continue;}
                final String label=labels[index];final String icon=icons[index];
                TextView tile=toolTile(icon,label,()->{if(playCenter)runPlayCenter(label);else runRoomTool(label);});
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(94),1);lp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(tile,lp);index++;
            }
            host.addView(row,new LinearLayout.LayoutParams(-1,dp(100)));
        }
    }

    private void runRoomTool(String label){
        if("Events".equals(label))eventsPanel();
        else if("Room seat".equals(label))roomSeatPanel();
        else if("Enable queue".equals(label))toggleSeatQueue();
        else if("Private chat".equals(label))privateChatDialog();
        else if("Music".equals(label))musicPanel();
        else if("Atmosphere".equals(label))atmospherePanel();
        else if("Income".equals(label))giftHistoryDialog();
        else if("Share Family".equals(label))shareFamilyRoom();
        else if("Theme Room".equals(label))themeRoomPanel();
    }

    private void runPlayCenter(String label){
        if("Room Battle".equals(label)){pkBattle();return;}
        if("Vote".equals(label)){votePanel();return;}
        if("Truth or Dare".equals(label)){truthOrDare();return;}
        if("Guess It".equals(label)){guessItPanel();return;}
        if("Counter".equals(label)){counterPanel();return;}
        if("Pass the Bomb".equals(label)){passBombPanel();return;}
        if("Fan Box".equals(label)){fanBoxPanel();return;}
        if("Wheel Challenge".equals(label)||"Party Wheel".equals(label)){wheelPanel(label);return;}
        toast(label);
    }

    private void roomSeatPanel(){
        List<String> rows=new ArrayList<>();
        for(int i=1;i<=maxSeats;i++){String n=seatNames.get(i);boolean locked=lockedSeats.contains(i);rows.add("NO."+i+"  "+(n==null?(locked?"🔒 Locked":"＋ Empty"):"👤 "+n));}
        new AlertDialog.Builder(this).setTitle("🪑 Room seat").setItems(rows.toArray(new String[0]),(d,w)->seatAction(w+1)).setNegativeButton("Close",null).show();
    }

    private void toggleSeatQueue(){
        if(cloudRoom&&db!=null){
            db.collection("live_rooms").document(roomId).get().addOnSuccessListener(doc->{boolean next=!Boolean.TRUE.equals(doc.getBoolean("queueEnabled"));setRoomValue("queueEnabled",next);addEvent("queue",safeName()+" turned seat queue "+(next?"ON":"OFF"));toast("Seat queue "+(next?"enabled":"disabled"));}).addOnFailureListener(e->toast(msg(e)));
        }else{boolean next=!prefs.getBoolean("queue_"+roomId,false);prefs.edit().putBoolean("queue_"+roomId,next).apply();toast("Seat queue "+(next?"enabled":"disabled"));}
    }

    private void atmospherePanel(){
        String[] items={"✨ Classic","💙 KTV","💗 Love","🎮 Game","👑 Royal"};
        new AlertDialog.Builder(this).setTitle("🎨 Atmosphere • "+roomTheme).setItems(items,(d,w)->{
            if(!isOwner()){toast("Host controls the room atmosphere");return;}
            String[] raw={"Classic","KTV","Love","Game","Royal"};roomTheme=raw[w];if(cloudRoom)setRoomValue("theme",roomTheme);else prefs.edit().putString("theme_"+roomName,roomTheme).apply();renderParty();
        }).setNegativeButton("Close",null).show();
    }

    private void themeRoomPanel(){atmospherePanel();}

    private void shareFamilyRoom(){
        Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,"👑 Join my KING Plus family room • "+roomName+" • Room ID "+shortId());startActivity(Intent.createChooser(i,"Share Family"));
    }

    private void counterPanel(){
        final String key="counter_"+roomId;int current=prefs.getInt(key,0);
        new AlertDialog.Builder(this).setTitle("⭐ Counter").setMessage("Room counter: "+current).setPositiveButton("+1",(d,w)->{int n=prefs.getInt(key,0)+1;prefs.edit().putInt(key,n).apply();addEvent("play",safeName()+" increased Counter to "+n);toast("Counter: "+n);}).setNeutralButton("Reset",(d,w)->prefs.edit().putInt(key,0).apply()).setNegativeButton("Close",null).show();
    }

    private void guessItPanel(){
        final int answer=1+new java.util.Random().nextInt(9);final EditText input=new EditText(this);input.setHint("Guess 1 to 9");input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(this).setTitle("🌷 Guess It").setView(input).setPositiveButton("Guess",(d,w)->{int g=-1;try{g=Integer.parseInt(input.getText().toString().trim());}catch(Exception ignored){}String r=g==answer?"🎉 Correct!":"Answer was "+answer;addEvent("play",safeName()+" played Guess It • "+r);toast(r);}).setNegativeButton("Close",null).show();
    }

    private void truthOrDare(){
        String[] truth={"What made you smile today?","Who is your best friend here?","What is your favourite song?","What is your dream trip?"};
        String[] dare={"Send 😂 in chat","Say hello to everyone","Send a rose gift","Take an empty mic seat"};
        new AlertDialog.Builder(this).setTitle("🥊 Truth or Dare").setItems(new String[]{"Truth","Dare"},(d,w)->{String text=w==0?truth[new java.util.Random().nextInt(truth.length)]:dare[new java.util.Random().nextInt(dare.length)];addEvent("play",safeName()+" chose "+(w==0?"Truth":"Dare"));new AlertDialog.Builder(this).setTitle(w==0?"Truth":"Dare").setMessage(text).setPositiveButton("Done",null).show();}).setNegativeButton("Close",null).show();
    }

    private void passBombPanel(){
        List<String> names=new ArrayList<>();names.add(ownerName==null?"Host":ownerName);for(String n:seatNames.values())if(n!=null&&!names.contains(n))names.add(n);String picked=names.get(new java.util.Random().nextInt(names.size()));addEvent("play","💣 Bomb passed to "+picked);new AlertDialog.Builder(this).setTitle("💣 Pass the Bomb").setMessage("Bomb landed on\n\n🔥 "+picked+" 🔥").setPositiveButton("Pass again",(d,w)->passBombPanel()).setNegativeButton("Close",null).show();
    }

    private void fanBoxPanel(){
        new AlertDialog.Builder(this).setTitle("📦 Fan Box").setMessage("Fan Box uses real room activity. Gifts and supporters received in this room appear in Gift history and Ranking.").setPositiveButton("Gift history",(d,w)->giftHistoryDialog()).setNeutralButton("Ranking",(d,w)->roomRankingDialog()).setNegativeButton("Close",null).show();
    }

    private void wheelPanel(String title){
        String[] prizes={"🌹 Rose","⭐ 10 points","😂 Funny task","🎤 Take mic","💗 Heart","👑 Crown challenge","🎁 Gift challenge","🎉 Party shoutout"};String result=prizes[new java.util.Random().nextInt(prizes.length)];addEvent("play",safeName()+" spun "+title+" • "+result);new AlertDialog.Builder(this).setTitle("🎡 "+title).setMessage(result).setPositiveButton("Spin again",(d,w)->wheelPanel(title)).setNegativeButton("Close",null).show();
    }

    private void votePanel(){
        String[] options={"👍 Yes","👎 No","❤️ Love it","😂 Funny"};new AlertDialog.Builder(this).setTitle("💌 Room Vote").setItems(options,(d,w)->{addEvent("vote",safeName()+" voted "+options[w]);toast("Vote sent: "+options[w]);}).setNegativeButton("Close",null).show();
    }
'''
s=s[:start]+new_tools+s[end:]

insert=s.index('    private void seatUserMenu(int no){')
helper=r'''    private void openSeatProfile(int no){
        String name=seatNames.get(no);if(name==null)return;String uid=seatUids.get(no);
        if(uid!=null&&!uid.isEmpty()){showRichProfile(uid,name,uid.equals(ownerUid));return;}
        seatUserMenu(no);
    }

'''
s=s[:insert]+helper+s[insert:]

start=s.index('    private void showProfileMore(String uid,String name){')
end=s.index('\n    private void kickUser(', start)
new_more=r'''    private void showProfileMore(String uid,String name){
        List<String> items=new ArrayList<>();items.add("💬 Private chat");items.add("🎁 Send gift");items.add("⚑ Report");
        int seatNo=-1;for(Map.Entry<Integer,String> e:seatUids.entrySet())if(uid!=null&&uid.equals(e.getValue())){seatNo=e.getKey();break;}
        final int occupiedSeat=seatNo;
        if(isModerator()&&uid!=null&&!uid.equals(ownerUid)){
            if(occupiedSeat>0){items.add(Boolean.FALSE.equals(seatMics.get(occupiedSeat))?"🎤 Unmute seat":"🔇 Mute seat");items.add("↔ Move seat");items.add("⬇ Remove from seat");}
            items.add("🚪 Kick");items.add("🚫 Ban");
            if(isOwner())items.add("👑 Co-host role");
        }else if(uid!=null&&(user==null||!uid.equals(user.getUid())))items.add("🚫 Block");
        new AlertDialog.Builder(this).setTitle(name).setItems(items.toArray(new String[0]),(d,w)->{String x=items.get(w);
            if(x.contains("Private"))openPrivateChat(uid,name);else if(x.contains("Gift"))giftDialogFor(uid,name);else if(x.contains("Report"))reportUser(uid,name);
            else if(x.contains("Mute seat")||x.contains("Unmute seat"))hostToggleSeat(occupiedSeat);else if(x.contains("Move seat"))moveUserToSeatDialog(uid,name,occupiedSeat);else if(x.contains("Remove from seat"))hostRemoveSeat(occupiedSeat);
            else if(x.contains("Kick"))kickUser(uid,name);else if(x.contains("Ban"))banUser(uid,name);else if(x.contains("Co-host"))coHostDialog(uid,name);else if(x.contains("Block"))blockUserLocally(uid,name);
        }).setNegativeButton("Close",null).show();
    }
'''
s=s[:start]+new_more+s[end:]

needle='db.collection("public_profiles").document(uid).get().addOnSuccessListener(p->{if(p.exists()){String pn=p.getString("displayName");if(pn!=null&&!pn.trim().isEmpty())n.setText((host?"👑 ":"")+pn.trim());}});'
repl='db.collection("public_profiles").document(uid).get().addOnSuccessListener(p->{if(p.exists()){String pn=p.getString("displayName");if(pn!=null&&!pn.trim().isEmpty())n.setText((host?"👑 ":"")+pn.trim());Long lv=p.getLong("level");if(lv!=null)level.setText("Lv "+Math.max(1,lv));Long vv=p.getLong("vipLevel");if(vv!=null)vip.setText("💎 VIP "+Math.max(0,vv));}});'
if needle in s:s=s.replace(needle,repl,1)
else:print('warning: public profile enrichment template not found')

P.write_text(s,encoding='utf-8')
b=B.read_text(encoding='utf-8')
b=re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 66; versionName '4.4.0'", b)
B.write_text(b,encoding='utf-8')
Path('CHANGELOG-v4.4.0.txt').write_text('KING Plus v4.4.0 — Party Room Complete Reference Flow\n\nSeat avatars now open the rich user profile sheet directly. The plus/more panel is rebuilt as Play Center + Tools using the supplied room references: Counter, Guess It, Truth or Dare, Pass the Bomb, Fan Box, Wheel Challenge, Party Wheel, Room Battle, Vote, Events, Room seat, Enable queue, Private chat, Music, Atmosphere, Income, Share Family and Theme Room. Profile overflow retains host/co-host seat moderation. No-billing and TEST OTP 123456 remain unchanged.\n',encoding='utf-8')
print('Prepared v4.4.0 Party Room Complete Reference Flow')
