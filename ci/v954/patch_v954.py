from pathlib import Path
import sys

root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
p=pkg/'PartyActivity.java'
s=p.read_text()

def replace_method(src, signature, replacement):
    start=src.find(signature)
    if start<0: raise SystemExit('signature missing: '+signature)
    brace=src.find('{',start)
    depth=0; state='code'; quote=''; esc=False; i=brace; end=None
    while i<len(src):
        ch=src[i]; nx=src[i+1] if i+1<len(src) else ''
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
                if depth==0: end=i+1; break
        i+=1
    if end is None: raise SystemExit('method end missing: '+signature)
    return src[:start]+replacement+src[end:]

s=replace_method(s,'private void atmospherePanel()',r'''private void atmospherePanel(){
        String[] names954={"Classic","KTV","PK","Love","Game","Royal","Neon","Galaxy","Festival","Ice"};
        String[] icons954={"✨","🎤","⚔️","💗","🎮","👑","💡","🌌","🎉","❄️"};
        String[] desc954={"Clean KING room","Blue KTV stage","Red/blue battle","Warm social room","Game-night green","Premium royal violet","Electric cyan","Deep-space purple","Celebration room","Cool ice lounge"};
        LinearLayout root954=new LinearLayout(this);root954.setOrientation(LinearLayout.VERTICAL);root954.setPadding(dp(12),dp(8),dp(12),dp(10));root954.setBackgroundColor(0xfff8f6fa);
        TextView current954=tv("Current  •  "+themeIcon954()+"  "+roomTheme,13,0xff38313f,true);current954.setGravity(Gravity.CENTER);current954.setBackground(bg(0xfffff0bd,14));root954.addView(current954,new LinearLayout.LayoutParams(-1,dp(42)));
        ScrollView scroll954=new ScrollView(this);LinearLayout list954=new LinearLayout(this);list954.setOrientation(LinearLayout.VERTICAL);scroll954.addView(list954);
        for(int i=0;i<names954.length;i++){
            final String chosen954=names954[i];
            LinearLayout row954=new LinearLayout(this);row954.setGravity(Gravity.CENTER_VERTICAL);row954.setPadding(dp(10),dp(4),dp(10),dp(4));row954.setBackground(bg(chosen954.equals(roomTheme)?0xfffff2c8:Color.WHITE,14));
            TextView icon954=tv(icons954[i],24,themeAccentFor954(chosen954),false);icon954.setGravity(Gravity.CENTER);row954.addView(icon954,new LinearLayout.LayoutParams(dp(48),dp(56)));
            LinearLayout info954=new LinearLayout(this);info954.setOrientation(LinearLayout.VERTICAL);info954.addView(tv(names954[i],14,0xff2e2933,true),new LinearLayout.LayoutParams(-1,dp(28)));info954.addView(tv(desc954[i],11,0xff81798a,false),new LinearLayout.LayoutParams(-1,dp(24)));row954.addView(info954,new LinearLayout.LayoutParams(0,dp(56),1));
            TextView mark954=tv(chosen954.equals(roomTheme)?"✓":"›",16,chosen954.equals(roomTheme)?0xff1a8f67:0xff8c8493,true);mark954.setGravity(Gravity.CENTER);row954.addView(mark954,new LinearLayout.LayoutParams(dp(36),dp(56)));
            row954.setOnClickListener(v->{if(!isOwner()){toast("Host controls the room atmosphere");return;}roomTheme=chosen954;if(cloudRoom)setRoomValue("theme",roomTheme);else prefs.edit().putString("theme_"+roomName,roomTheme).apply();renderParty();});
            LinearLayout.LayoutParams rp954=new LinearLayout.LayoutParams(-1,dp(62));rp954.setMargins(0,dp(3),0,dp(3));list954.addView(row954,rp954);
        }
        root954.addView(scroll954,new LinearLayout.LayoutParams(-1,Math.min(dp(520),(int)(getResources().getDisplayMetrics().heightPixels*.60f))));
        new AlertDialog.Builder(this).setTitle("🎨 Room Atmosphere").setView(root954).setNegativeButton("Close",null).show();
    }''')

s=replace_method(s,'private void partyDataPanel()',r'''private void partyDataPanel(){
        if(!cloudRoom||db==null||roomId==null){
            LinearLayout root954=new LinearLayout(this);root954.setOrientation(LinearLayout.VERTICAL);root954.setPadding(dp(12),dp(8),dp(12),dp(12));
            addPartyStat954(root954,"👥","Members",String.valueOf(Math.max(1,memberNames.size())));
            addPartyStat954(root954,"🎙","Occupied seats",seatNames.size()+" / "+maxSeats);
            addPartyStat954(root954,themeIcon954(),"Theme",roomTheme);
            addPartyStat954(root954,"🏷","Room type",roomCategory);
            new AlertDialog.Builder(this).setTitle("📊 Party Data").setView(root954).setPositiveButton("OK",null).show();return;
        }
        LinearLayout wait954=new LinearLayout(this);wait954.setGravity(Gravity.CENTER_VERTICAL);wait954.setPadding(dp(18),dp(10),dp(18),dp(10));android.widget.ProgressBar spin954=new android.widget.ProgressBar(this);wait954.addView(spin954,new LinearLayout.LayoutParams(dp(34),dp(34)));TextView loading954=tv("Loading room activity…",13,0xff5c5564,true);wait954.addView(loading954,new LinearLayout.LayoutParams(0,dp(52),1));final AlertDialog waiting954=new AlertDialog.Builder(this).setTitle("📊 Party Data").setView(wait954).setCancelable(false).create();waiting954.show();
        final long[] giftValue={0};final int[] gifts={0};final int[] events={0};
        db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(200).get().addOnSuccessListener(es->{
            events[0]=es.size();for(DocumentSnapshot e:es.getDocuments())if("gift".equals(e.getString("type"))){gifts[0]++;Long v=e.getLong("giftValue");if(v!=null)giftValue[0]+=Math.max(0,v);}
            db.collection("live_rooms").document(roomId).collection("messages").orderBy("createdAt",Query.Direction.DESCENDING).limit(200).get().addOnSuccessListener(ms->{
                if(waiting954.isShowing())waiting954.dismiss();
                LinearLayout root954=new LinearLayout(this);root954.setOrientation(LinearLayout.VERTICAL);root954.setPadding(dp(12),dp(8),dp(12),dp(12));root954.setBackgroundColor(0xfffaf8fc);
                TextView live954=tv("● LIVE ROOM  •  "+themeIcon954()+" "+roomTheme,12,0xff198b64,true);live954.setGravity(Gravity.CENTER);live954.setBackground(bg(0xffe5f8f0,14));root954.addView(live954,new LinearLayout.LayoutParams(-1,dp(40)));
                LinearLayout statsA954=new LinearLayout(this);statsA954.setGravity(Gravity.CENTER);statsA954.addView(partyStatCard954("👥",String.valueOf(liveMemberCount),"Online"),new LinearLayout.LayoutParams(0,dp(84),1));statsA954.addView(partyStatCard954("🎙",seatNames.size()+"/"+maxSeats,"Seats"),new LinearLayout.LayoutParams(0,dp(84),1));root954.addView(statsA954,new LinearLayout.LayoutParams(-1,dp(90)));
                LinearLayout statsB954=new LinearLayout(this);statsB954.setGravity(Gravity.CENTER);statsB954.addView(partyStatCard954("💬",String.valueOf(ms.size()),"Messages"),new LinearLayout.LayoutParams(0,dp(84),1));statsB954.addView(partyStatCard954("🎁",String.valueOf(gifts[0]),"Gifts"),new LinearLayout.LayoutParams(0,dp(84),1));statsB954.addView(partyStatCard954("💎",compactNumber(giftValue[0]),"Gift value"),new LinearLayout.LayoutParams(0,dp(84),1));root954.addView(statsB954,new LinearLayout.LayoutParams(-1,dp(90)));
                TextView details954=tv("Category  "+roomCategory+"     •     Recent events  "+events[0],11,0xff77707f,true);details954.setGravity(Gravity.CENTER);root954.addView(details954,new LinearLayout.LayoutParams(-1,dp(40)));
                new AlertDialog.Builder(this).setTitle("📊 Party Data").setView(root954).setPositiveButton("Ranking",(d,w)->roomRankingDialog()).setNeutralButton("Gift history",(d,w)->giftHistoryDialog()).setNegativeButton("Close",null).show();
            }).addOnFailureListener(e->{if(waiting954.isShowing())waiting954.dismiss();new AlertDialog.Builder(this).setTitle("Party data unavailable").setMessage(msg(e)).setPositiveButton("Retry",(d,w)->partyDataPanel()).setNegativeButton("Close",null).show();});
        }).addOnFailureListener(e->{if(waiting954.isShowing())waiting954.dismiss();new AlertDialog.Builder(this).setTitle("Party data unavailable").setMessage(msg(e)).setPositiveButton("Retry",(d,w)->partyDataPanel()).setNegativeButton("Close",null).show();});
    }''')

# Insert theme helpers before themeBackground.
marker='''    private int themeBackground(){'''
helpers=r'''    private int themeAccentFor954(String theme){if("KTV".equals(theme))return 0xff5ca7ff;if("PK".equals(theme))return 0xffff6c83;if("Love".equals(theme))return 0xffff70b2;if("Game".equals(theme))return 0xff55dda4;if("Royal".equals(theme))return 0xffffd76a;if("Neon".equals(theme))return 0xff35e8ff;if("Galaxy".equals(theme))return 0xffa887ff;if("Festival".equals(theme))return 0xffff946e;if("Ice".equals(theme))return 0xff7de8ff;return 0xff35e6b3;}
    private int themeAccent954(){return themeAccentFor954(roomTheme);}
    private String themeIcon954(){if("KTV".equals(roomTheme))return "🎤";if("PK".equals(roomTheme))return "⚔️";if("Love".equals(roomTheme))return "💗";if("Game".equals(roomTheme))return "🎮";if("Royal".equals(roomTheme))return "👑";if("Neon".equals(roomTheme))return "💡";if("Galaxy".equals(roomTheme))return "🌌";if("Festival".equals(roomTheme))return "🎉";if("Ice".equals(roomTheme))return "❄️";return "✨";}
    private TextView partyStatCard954(String icon,String value,String label){TextView t=tv(icon+"\\n"+value+"\\n"+label,11,0xff302b35,true);t.setGravity(Gravity.CENTER);t.setBackground(bg(Color.WHITE,14));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(80),1);lp.setMargins(dp(3),dp(3),dp(3),dp(3));return t;}
    private void addPartyStat954(LinearLayout root,String icon,String label,String value){LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(12),dp(4),dp(12),dp(4));row.setBackground(bg(0xfff6f3f8,13));TextView a=tv(icon+"  "+label,13,0xff39323f,true);row.addView(a,new LinearLayout.LayoutParams(0,dp(46),1));TextView b=tv(value,13,0xff6d527f,true);b.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);row.addView(b,new LinearLayout.LayoutParams(dp(150),dp(46)));LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(52));rp.setMargins(0,dp(3),0,dp(3));root.addView(row,rp);}
    
'''
if marker not in s: raise SystemExit('themeBackground marker missing')
s=s.replace(marker,helpers+marker,1)

# Make current theme visible inside Party and color send action by selected theme.
old='''        onlineStateLabel900=pill("● Online",0x33000000,this::onlineDiagnostics900);onlineStateLabel900.setTextSize(9);onlineStateLabel900.setTextColor(0xff4ff0a3);LinearLayout.LayoutParams onlineLp=new LinearLayout.LayoutParams(-2,dp(24));onlineLp.setMargins(dp(8),0,0,0);badges.addView(onlineStateLabel900,onlineLp);
        View badgeSpacer=new View(this); badges.addView(badgeSpacer,new LinearLayout.LayoutParams(0,1,1));'''
new='''        onlineStateLabel900=pill("● Online",0x33000000,this::onlineDiagnostics900);onlineStateLabel900.setTextSize(9);onlineStateLabel900.setTextColor(0xff4ff0a3);LinearLayout.LayoutParams onlineLp=new LinearLayout.LayoutParams(-2,dp(24));onlineLp.setMargins(dp(8),0,0,0);badges.addView(onlineStateLabel900,onlineLp);
        TextView themeChip954=pill(themeIcon954()+" "+roomTheme,0x33000000,this::atmospherePanel);themeChip954.setTextSize(9);themeChip954.setTextColor(themeAccent954());LinearLayout.LayoutParams themeLp954=new LinearLayout.LayoutParams(-2,dp(24));themeLp954.setMargins(dp(8),0,0,0);badges.addView(themeChip954,themeLp954);
        View badgeSpacer=new View(this); badges.addView(badgeSpacer,new LinearLayout.LayoutParams(0,1,1));'''
if old not in s: raise SystemExit('party badge theme insertion marker missing')
s=s.replace(old,new,1)

old='''send.setTextColor(0xff008e6b);'''
if old not in s: raise SystemExit('send color marker missing')
s=s.replace(old,'''send.setTextColor(themeAccent954());''',1)

p.write_text(s)

gradle=root/'app/build.gradle'; g=gradle.read_text()
old="versionCode 144; versionName '9.5.3-parity-batch4'"
if old not in g: raise SystemExit('v9.5.3 version marker missing')
gradle.write_text(g.replace(old,"versionCode 145; versionName '9.5.4-theme-polish'",1))
print('KING Plus v9.5.4 Party theme and data polish applied')
