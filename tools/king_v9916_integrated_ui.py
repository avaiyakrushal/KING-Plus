#!/usr/bin/env python3
"""KING v9.9.16: original design, room quick actions, responsiveness and login safety."""
from pathlib import Path
import re,sys
root=Path(sys.argv[1]);src=root/'app/src/main/java/com/kingplus/social'
party=src/'PartyActivity.java';s=party.read_text()
def one(old,new,label):
 global s
 n=s.count(old)
 if n!=1:raise RuntimeError(f'{label} anchor expected 1 found {n}')
 s=s.replace(old,new,1)
one('''        roomRoot.addView(play997,playLp997);
        if(!requestedGame940.isEmpty()){''','''        roomRoot.addView(play997,playLp997);
        // Visible actions open existing real Firebase-backed screens.
        HorizontalScrollView roomActions9916=new HorizontalScrollView(this);
        roomActions9916.setHorizontalScrollBarEnabled(false);
        LinearLayout actionRow9916=new LinearLayout(this);
        actionRow9916.setGravity(Gravity.CENTER_VERTICAL);
        actionRow9916.setPadding(dp(12),0,dp(12),0);
        String[] actionNames9916={"🎙 Seats","👥 Members","🔗 Invite","🛡 Host","🎁 Gifts"};
        Runnable[] actionCallbacks9916={this::roomSeatPanel,this::membersDialog,
            this::shareRealRoom996,this::moderationPanel,this::giftShopPanel};
        for(int i9916=0;i9916<actionNames9916.length;i9916++){
            TextView action9916=pill(actionNames9916[i9916],0x77422b73,actionCallbacks9916[i9916]);
            action9916.setTextSize(11);action9916.setTextColor(Color.WHITE);
            action9916.setContentDescription(actionNames9916[i9916]);
            LinearLayout.LayoutParams lp9916=new LinearLayout.LayoutParams(dp(96),dp(36));
            lp9916.setMargins(dp(3),0,dp(3),0);
            actionRow9916.addView(action9916,lp9916);
        }
        roomActions9916.addView(actionRow9916,new HorizontalScrollView.LayoutParams(-2,dp(40)));
        roomRoot.addView(roomActions9916,new LinearLayout.LayoutParams(-1,dp(42)));
        if(!requestedGame940.isEmpty()){''','party toolbar')
one('''        roomRoot.addView(seatsBox,new LinearLayout.LayoutParams(-1,dp(178))); rebuildSeats();''',
'''        int displayDp9916=(int)(getResources().getDisplayMetrics().heightPixels/
            Math.max(1f,getResources().getDisplayMetrics().density));
        int seatsDp9916=displayDp9916<680?148:178;
        roomRoot.addView(seatsBox,new LinearLayout.LayoutParams(-1,dp(seatsDp9916))); rebuildSeats();''','small device')
old='''    private void addBottomNav(LinearLayout root,int selected){LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER);nav.setBackgroundColor(Color.WHITE);String[] ni={"⌂\\nParty","♟\\nGame","◇\\nDiscover","✉\\nMessages","●\\nMe"};for(int i=0;i<ni.length;i++){TextView n=tv(ni[i],12,i==selected?0xffd8b900:0xff777777,true);n.setGravity(Gravity.CENTER);final int k=i;n.setOnClickListener(v->{if(k==0){if(roomId!=null)confirmLeave940();else renderLobby("Hot");}else KingNav.openRoot(this,k);});nav.addView(n,new LinearLayout.LayoutParams(0,dp(62),1));}root.addView(nav);}'''
new='''    private void addBottomNav(LinearLayout root,int selected){
        LinearLayout nav=new LinearLayout(this);
        nav.setGravity(Gravity.CENTER_VERTICAL);
        nav.setPadding(dp(5),dp(5),dp(5),dp(5));
        nav.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{0xff261741,0xff130e29}));
        String[] icons={"⌂","♟","◇","✉","●"};
        String[] names={"Party","Games","Discover","Messages","Me"};
        for(int i=0;i<5;i++){
            final int tab=i; boolean active=i==selected;
            LinearLayout item=new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);item.setGravity(Gravity.CENTER);
            if(active){
                GradientDrawable bgNav=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                    new int[]{0xff6e46b9,0xff482c83});
                bgNav.setCornerRadius(dp(16));bgNav.setStroke(dp(1),0x77ffe7ad);
                item.setBackground(bgNav);
            }
            TextView icon=tv(icons[i],24,active?0xffffdf80:0xffcab9dd,true);
            icon.setGravity(Gravity.CENTER);
            item.addView(icon,new LinearLayout.LayoutParams(-1,dp(30)));
            TextView label=tv(names[i],11,active?Color.WHITE:0xffc7b9d8,true);
            label.setGravity(Gravity.CENTER);
            item.addView(label,new LinearLayout.LayoutParams(-1,dp(20)));
            item.setContentDescription(names[i]+(active?" selected":""));
            item.setOnClickListener(v->{
                if(tab==0){if(roomId!=null)confirmLeave940();else renderLobby("Hot");}
                else KingNav.openRoot(this,tab);
            });
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(58),1);
            lp.setMargins(dp(2),0,dp(2),0);nav.addView(item,lp);
        }
        root.addView(nav,new LinearLayout.LayoutParams(-1,dp(68)));
    }'''
one(old,new,'bottom navigation')
party.write_text(s)
main=src/'MainActivity.java';m=main.read_text()
old='''if(displayName==null||displayName.trim().isEmpty())login();else home();'''
new='''if(displayName==null||displayName.trim().isEmpty()||firebaseAuth==null||firebaseAuth.getCurrentUser()==null)login();else home();'''
n=m.count(old)
if n!=1:raise RuntimeError(f'auth recovery anchor found {n}')
main.write_text(m.replace(old,new,1))
gradle=root/'app/build.gradle';g=gradle.read_text()
g,n=re.subn(r"versionCode\s+194;\s*versionName\s+'9\.9\.15[^']*'",
            "versionCode 195; versionName '9.9.16-integrated-room-ui-test'",g)
if n!=1:raise RuntimeError('expected v9.9.15 source')
gradle.write_text(g)
assert 'approveOrMoveMemberSeat9913' in s
assert 'KingNative3DRoom.open(' in s
assert 'openPartyGameChooser9915' in s
assert 'private void openVideoRoom()' in s
print('PASS unified room actions, small-screen seat view, original KING nav, Firebase login recovery')
