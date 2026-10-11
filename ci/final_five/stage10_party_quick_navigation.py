#!/usr/bin/env python3
"""Stage UX 1: coherent quick Party controls, retaining ALL legacy tools.

UX issue: a >50-option tool matrix and Firebase developer instructions dominate
daily use. Provide clear one-tap Party quick sheet and keep "All Tools" intact.
Never copy proprietary Bolo Hi artwork/source. No Firebase schema changes.
"""
from pathlib import Path
import sys
p=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social'
party=p/'PartyActivity.java'
s=party.read_text()
anchor='    private void roomMenu() {\n        List<String> items=new ArrayList<>();'
assert s.count(anchor)==1,f'Party menu anchor mismatch ({s.count(anchor)})'
insert=r'''    /** Primary Party controls must be reachable without the 50+ item toolbox.
     * In-room voice/seat/game state remains in the SAME PartyActivity.
     * The long tool panel is still available as "All Tools".
     */
    private void roomMenu(){
        final java.util.List<String> quick=new java.util.ArrayList<>();
        quick.add(micOn?"🎤 Mic OFF":"🎤 Mic ON");
        quick.add("👥 Members");
        quick.add("🎮 Games");
        quick.add("💫 Interactive Emoji");
        quick.add("🎁 Gift Wall");
        quick.add("💬 Private chat");
        quick.add("📹 Multi Video");
        quick.add("🌐 3D World");
        quick.add("🔎 Find room user");
        if(isModerator())quick.add("🛡 Administrator");
        else quick.add("⚙ Room settings");
        quick.add("🔗 Share room");
        quick.add("⋯ All Tools");
        android.widget.ScrollView scroll=new android.widget.ScrollView(this);
        scroll.setFillViewport(false);
        scroll.setVerticalScrollBarEnabled(false);
        android.widget.LinearLayout panel=new android.widget.LinearLayout(this);
        panel.setOrientation(android.widget.LinearLayout.VERTICAL);
        panel.setPadding(dp(16),dp(14),dp(16),dp(20));
        panel.setBackgroundColor(0xfffcfbff);
        scroll.addView(panel);
        android.widget.TextView heading=tv("KING Party • Quick Tools",19,0xff282036,true);
        heading.setGravity(android.view.Gravity.CENTER_VERTICAL);
        panel.addView(heading,new android.widget.LinearLayout.LayoutParams(-1,dp(40)));
        android.widget.TextView intro=tv("Tap once to open • Your voice room stays active",12,0xff78707d,false);
        panel.addView(intro,new android.widget.LinearLayout.LayoutParams(-1,dp(30)));
        final android.app.AlertDialog[] dialog={null};
        android.widget.LinearLayout line=null;
        for(int i=0;i<quick.size();i++){
            if(i%3==0){
                line=new android.widget.LinearLayout(this);
                line.setGravity(android.view.Gravity.CENTER_VERTICAL);
                line.setWeightSum(3f);
                android.widget.LinearLayout.LayoutParams rowp=
                    new android.widget.LinearLayout.LayoutParams(-1,dp(88));
                rowp.setMargins(0,dp(4),0,dp(4));
                panel.addView(line,rowp);
            }
            final String item=quick.get(i);
            android.widget.TextView tile=tv(item,12,0xff292036,true);
            tile.setGravity(android.view.Gravity.CENTER);
            tile.setMaxLines(2);
            tile.setEllipsize(android.text.TextUtils.TruncateAt.END);
            tile.setContentDescription(item);
            tile.setBackground(bg(0xfff0ecf8,14));
            android.widget.LinearLayout.LayoutParams tp=
                new android.widget.LinearLayout.LayoutParams(0,dp(81),1f);
            tp.setMargins(dp(4),dp(3),dp(4),dp(3));
            line.addView(tile,tp);
            tile.setOnClickListener(v->{
                if(dialog[0]!=null)dialog[0].dismiss();
                if(item.contains("All Tools"))roomMenuAll();
                else runPartyAction("quick-tool:"+item,()->handleRoomMenuItem810(item));
            });
        }
        final android.app.AlertDialog sheet=new android.app.AlertDialog.Builder(this)
            .setView(scroll)
            .setNegativeButton("Close",null)
            .create();
        dialog[0]=sheet;
        sheet.setOnShowListener(v->{
            android.view.Window win=sheet.getWindow();
            if(win!=null){
                win.setBackgroundDrawable(bg(0xfffcfbff,22));
                win.setGravity(android.view.Gravity.BOTTOM);
                win.setLayout(-1,(int)(getResources().getDisplayMetrics().heightPixels*.64f));
            }
        });
        sheet.show();
    }

    // Advanced tools preserved for all previously built features.
    private void roomMenuAll() {
        List<String> items=new ArrayList<>();'''
s=s.replace(anchor,insert,1)
assert s.count('private void roomMenuAll()')==1 and s.count('private void roomMenu()')==1
assert 'handleRoomMenuItem810(x)' in s and 'KingRoomDice997.open(' in s
party.write_text(s)
print('PASS Party primary tool menu now compact 12-action single-tap sheet')
print('PASS all original advanced toolbox choices retained via All Tools')
social=p/'SocialActivity.java';s=social.read_text()
t='TextView help992=label("Ask friends to download KING Plus, sign in with another Google account, search this ID, then Follow and Message.",12,DARK,false);'
assert s.count(t)==1,'Social developer help anchor missing'
s=s.replace(t,t+'\n            help992.setVisibility(android.view.View.GONE);',1)
t2='TextView trueUid964=label("My verified account UID: "+me.getUid()+"  •  Tap to copy",11,MUTED,false);'
assert s.count(t2)==1,'Social UID debug line anchor missing'
s=s.replace(t2,t2+'\n            trueUid964.setVisibility(android.view.View.GONE);',1)
s=s.replace('status = label("Real Firebase people", 12, MUTED, false);',
            'status = label("Discover • Follow • Chat", 12, MUTED, false);',1)
assert 'connectionTest996' in s
social.write_text(s)
print('PASS People/Discovery hides developer-facing tips/long UID, still keeps Test Connection')
print('NO production auth, game, room, billable credits or security permissions changed')
