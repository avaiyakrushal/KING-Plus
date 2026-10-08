from pathlib import Path
import sys

root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'

def replace_method(path, signature, replacement):
    p=pkg/path
    s=p.read_text()
    start=s.find(signature)
    if start<0: raise SystemExit(f'{path}: signature not found: {signature}')
    brace=s.find('{',start)
    depth=0; state='code'; quote=''; esc=False; i=brace; end=None
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
                if depth==0: end=i+1; break
        i+=1
    if end is None: raise SystemExit(f'{path}: method end not found')
    p.write_text(s[:start]+replacement+s[end:])

replace_method('CommunityHubActivity.java','private void relationships()',r'''private void relationships(){
        body.addView(tv("Relationship / CP",21,DARK,true));
        TextView intro952=tv("Build a mutual KING connection • requests, private chat and shared profile identity",12,MUTED,false);intro952.setPadding(0,0,0,dp(8));body.addView(intro952,new LinearLayout.LayoutParams(-1,dp(46)));
        if(!cloud()){body.addView(tv("Sign in to use Relationship.",14,MUTED,false));return;}

        LinearLayout quick952=new LinearLayout(this);quick952.setGravity(Gravity.CENTER);
        TextView find952=button("🔎 Find People",()->render("Search"));TextView moments952=button("▣ Square",()->render("Moments"));
        LinearLayout.LayoutParams qp952=new LinearLayout.LayoutParams(0,dp(46),1);qp952.setMargins(dp(2),0,dp(2),0);quick952.addView(find952,qp952);quick952.addView(moments952,new LinearLayout.LayoutParams(qp952));body.addView(quick952,new LinearLayout.LayoutParams(-1,dp(52)));

        if(!targetUid.isEmpty()&&!targetUid.equals(me.getUid())){
            LinearLayout selected952=new LinearLayout(this);selected952.setGravity(Gravity.CENTER_VERTICAL);selected952.setPadding(dp(12),dp(8),dp(12),dp(8));selected952.setBackground(bg(0xffffedf5,16));
            TextView who952=tv("💞  "+safe(targetName,"KING User"),15,DARK,true);selected952.addView(who952,new LinearLayout.LayoutParams(0,dp(46),1));
            TextView req952=button("Send Request",()->sendRelationship(targetUid,safe(targetName,"KING User")));selected952.addView(req952,new LinearLayout.LayoutParams(dp(126),dp(44)));LinearLayout.LayoutParams slp952=new LinearLayout.LayoutParams(-1,dp(58));slp952.setMargins(0,dp(6),0,dp(8));body.addView(selected952,slp952);
        }

        body.addView(tv("Your Relationship Status",16,DARK,true));
        TextView loading952=tv("Loading requests and CP connections…",13,MUTED,true);loading952.setGravity(Gravity.CENTER);loading952.setBackground(bg(CARD,14));body.addView(loading952,new LinearLayout.LayoutParams(-1,dp(58)));
        Set<String>seen=new HashSet<>();final int[] accepted952={0},pending952={0};
        db.collection("relationships").whereEqualTo("uidA",me.getUid()).limit(30).get().addOnSuccessListener(a->{
            for(DocumentSnapshot d:a.getDocuments()){seen.add(d.getId());String st=safe(d.getString("status"),"pending");if("accepted".equals(st))accepted952[0]++;else if("pending".equals(st))pending952[0]++;addRelationship(d);}
            db.collection("relationships").whereEqualTo("uidB",me.getUid()).limit(30).get().addOnSuccessListener(b->{
                for(DocumentSnapshot d:b.getDocuments())if(seen.add(d.getId())){String st=safe(d.getString("status"),"pending");if("accepted".equals(st))accepted952[0]++;else if("pending".equals(st))pending952[0]++;addRelationship(d);}
                loading952.setText("💞 CP / Friends  "+accepted952[0]+"     ⏳ Pending  "+pending952[0]);
                if(seen.isEmpty()){TextView empty952=tv("No relationship requests yet.\\nUse Find People, open a profile and send a request.",13,MUTED,false);empty952.setGravity(Gravity.CENTER);body.addView(empty952,new LinearLayout.LayoutParams(-1,dp(88)));}
            }).addOnFailureListener(e->loading952.setText("Could not load incoming relationship requests"));
        }).addOnFailureListener(e->loading952.setText("Relationship service unavailable right now"));
    }''')

replace_method('CommunityHubActivity.java','private void visitors750()',r'''private void visitors750(){
        body.addView(tv("Profile Visitors",21,DARK,true));
        body.addView(tv("People who recently opened your KING profile",12,MUTED,false));
        if(!cloud()){body.addView(tv("Sign in to see profile visitors.",14,MUTED,false));return;}
        TextView loading952=tv("Loading recent visitors…",13,MUTED,true);loading952.setGravity(Gravity.CENTER);loading952.setBackground(bg(CARD,14));LinearLayout.LayoutParams llp952=new LinearLayout.LayoutParams(-1,dp(58));llp952.setMargins(0,dp(8),0,dp(8));body.addView(loading952,llp952);
        db.collection("public_profiles").document(me.getUid()).collection("visitors")
          .orderBy("updatedAt",Query.Direction.DESCENDING).limit(50).get()
          .addOnSuccessListener(q->{
              loading952.setText(q.isEmpty()?"No profile visitors yet.":"Recent visitors • "+q.size());
              for(DocumentSnapshot d:q.getDocuments()){
                  String uid=safe(d.getString("uid"),d.getId()),name=safe(d.getString("name"),"KING User");
                  com.google.firebase.Timestamp at=d.getTimestamp("updatedAt");String when=at==null?"Recently":new java.text.SimpleDateFormat("dd MMM • HH:mm",java.util.Locale.US).format(at.toDate());
                  LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(12),dp(7),dp(10),dp(7));row.setBackground(bg(CARD,14));
                  TextView icon=tv("👁",20,PURPLE,false);icon.setGravity(Gravity.CENTER);row.addView(icon,new LinearLayout.LayoutParams(dp(44),dp(50)));
                  LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);info.addView(tv(name,14,DARK,true),new LinearLayout.LayoutParams(-1,dp(27)));info.addView(tv(when,11,MUTED,false),new LinearLayout.LayoutParams(-1,dp(22)));row.addView(info,new LinearLayout.LayoutParams(0,dp(50),1));
                  TextView open=tv("Profile ›",11,PURPLE,true);open.setGravity(Gravity.CENTER);row.addView(open,new LinearLayout.LayoutParams(dp(82),dp(50)));
                  row.setOnClickListener(v->{Intent i=new Intent(this,KingPublicProfileActivity.class);i.putExtra("uid",uid);i.putExtra("name",name);startActivity(i);});
                  LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(64));lp.setMargins(0,dp(4),0,dp(4));body.addView(row,lp);
              }
          }).addOnFailureListener(e->loading952.setText("Visitors unavailable right now • try again later"));
    }''')

replace_method('CommunityHubActivity.java','private void collection()',r'''private void collection(){
        LevelSystem.Snapshot s=LevelSystem.read(this);
        body.addView(tv("Collection Center",21,DARK,true));
        TextView subtitle952=tv("Wardrobe, medals, backpack and progression in one place",12,MUTED,false);body.addView(subtitle952,new LinearLayout.LayoutParams(-1,dp(36)));

        LinearLayout hero952=new LinearLayout(this);hero952.setOrientation(LinearLayout.VERTICAL);hero952.setPadding(dp(14),dp(10),dp(14),dp(10));hero952.setBackground(bg(0xffffefd0,18));
        TextView tier952=tv("👑 "+LevelSystem.levelTier(s.level)+"   •   VIP "+s.vipLevel,17,DARK,true);tier952.setGravity(Gravity.CENTER);hero952.addView(tier952,new LinearLayout.LayoutParams(-1,dp(36)));
        TextView prog952=tv("Lv."+s.level+" / "+LevelSystem.MAX_LEVEL+"   •   XP "+s.xp+" / "+s.nextLevelXp(),12,MUTED,true);prog952.setGravity(Gravity.CENTER);hero952.addView(prog952,new LinearLayout.LayoutParams(-1,dp(28)));
        android.widget.ProgressBar levelBar952=new android.widget.ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);long nextXp952=Math.max(1,s.nextLevelXp());levelBar952.setMax((int)Math.min(Integer.MAX_VALUE,nextXp952));levelBar952.setProgress((int)Math.min(Integer.MAX_VALUE,Math.max(0,s.xp)));hero952.addView(levelBar952,new LinearLayout.LayoutParams(-1,dp(10)));
        LinearLayout.LayoutParams hp952=new LinearLayout.LayoutParams(-1,dp(92));hp952.setMargins(0,dp(6),0,dp(8));body.addView(hero952,hp952);

        TextView wardrobe=button("👗 Open Wardrobe • Frames, Effects, Bubbles & Badges",()->startActivity(new Intent(this,KingWardrobeActivity.class)));body.addView(wardrobe,new LinearLayout.LayoutParams(-1,dp(50)));
        TextView equipped952=tv("🖼 "+KingCosmetics.frame(this)+"   ✨ "+KingCosmetics.effect(this)+"\\n💬 "+KingCosmetics.bubble(this),12,DARK,true);equipped952.setGravity(Gravity.CENTER_VERTICAL);equipped952.setBackground(bg(CARD,14));equipped952.setPadding(dp(12),dp(8),dp(12),dp(8));LinearLayout.LayoutParams eqp952=new LinearLayout.LayoutParams(-1,dp(62));eqp952.setMargins(0,dp(6),0,dp(8));body.addView(equipped952,eqp952);

        body.addView(tv("Medals & Achievements",16,DARK,true));
        LinearLayout medals952=new LinearLayout(this);medals952.setOrientation(LinearLayout.VERTICAL);
        String[][] medalData952={{"🏅","Rising Star",s.level>=10?"Unlocked":"Lv.10"},{"🎖","Elite Voice",s.level>=30?"Unlocked":"Lv.30"},{"💎","VIP Supporter",s.vipLevel>=3?"Unlocked":"VIP 3"},{"👑","Royal Patron",s.vipLevel>=8?"Unlocked":"VIP 8"}};
        for(String[] m952:medalData952){TextView row952=tv(m952[0]+"  "+m952[1]+"     •     "+m952[2],13,DARK,true);row952.setBackground(bg(CARD,13));LinearLayout.LayoutParams mlp952=new LinearLayout.LayoutParams(-1,dp(46));mlp952.setMargins(0,dp(2),0,dp(2));medals952.addView(row952,mlp952);}body.addView(medals952,new LinearLayout.LayoutParams(-1,-2));

        body.addView(tv("Backpack",16,DARK,true));SharedPreferences bp=getSharedPreferences("king_backpack",MODE_PRIVATE);String[] keys={"rose","heart","star","firework"},names={"Rose","Heart","Star","Firework"},icons={"🌹","💗","⭐","🎆"};
        LinearLayout bag952=new LinearLayout(this);bag952.setGravity(Gravity.CENTER);for(int i=0;i<keys.length;i++){TextView item952=tv(icons[i]+"\\n"+names[i]+"\\n×"+bp.getInt(keys[i],0),11,DARK,true);item952.setGravity(Gravity.CENTER);item952.setBackground(bg(CARD,13));LinearLayout.LayoutParams ilp952=new LinearLayout.LayoutParams(0,dp(78),1);ilp952.setMargins(dp(2),dp(3),dp(2),dp(3));bag952.addView(item952,ilp952);}body.addView(bag952,new LinearLayout.LayoutParams(-1,dp(84)));

        TextView vip952=button("💎 Open VIP Center",()->startActivity(new Intent(this,KingVipVisualActivity.class)));TextView claim=button("🎁 Claim Daily Free Gift",()->claimDaily(bp));TextView party=button("🎤 Open Party • Use Backpack",()->KingNav.openRoot(this,0));LinearLayout.LayoutParams blp952=new LinearLayout.LayoutParams(-1,dp(48));blp952.setMargins(0,dp(6),0,0);body.addView(vip952,blp952);body.addView(claim,new LinearLayout.LayoutParams(blp952));body.addView(party,new LinearLayout.LayoutParams(blp952));
    }''')

replace_method('MainActivity.java','private void rankingsPage()',r'''private void rankingsPage(){
        screen="rankings"; base("Rankings","Real KING Plus activity only");
        text("👑 KING Rankings",20,Color.WHITE,true);
        if(firebaseAuth==null || firebaseAuth.getCurrentUser()==null || firestore==null){
            text("Sign in to view real KING profiles and live room rankings.",14,MUTED,false);
            button("Sign in",PURPLE,this::login);button("Back to Profile",CARD,this::profile);return;
        }
        text("No sample leaderboard names • ranks use signed-in KING profile progress.",13,MUTED,false);
        button("⭐ Level & XP Rank",PURPLE,()->showGlobalRank952("level"));
        button("💎 VIP Progress Rank",CARD,()->showGlobalRank952("vip"));
        button("🎤 Room Gift Rank",CARD,this::openPartyActivity);
        button("👥 Followers / Friends",CARD,()->startActivity(new Intent(this,SocialActivity.class)));
        text("Top KING progress preview",15,Color.WHITE,true);
        final String requestedScreen952=screen;
        firestore.collection("public_profiles").limit(60).get().addOnSuccessListener(q->{
            if(!requestedScreen952.equals(screen))return;
            List<com.google.firebase.firestore.DocumentSnapshot> docs=new ArrayList<>(q.getDocuments());
            java.util.Collections.sort(docs,(a,b)->Long.compare(rankMetric952(b,"level"),rankMetric952(a,"level")));
            if(docs.isEmpty()){text("No public KING profiles available yet.",13,MUTED,false);return;}
            int shown=0;for(com.google.firebase.firestore.DocumentSnapshot d:docs){if(shown>=5)break;String n=d.getString("displayName");if(n==null||n.trim().isEmpty())n="KING User";Long lv=d.getLong("level"),vip=d.getLong("vipLevel");String medal=shown==0?"🥇":shown==1?"🥈":shown==2?"🥉":"#"+(shown+1);text(medal+"  "+n+"  •  Lv."+(lv==null?1:lv)+"  •  VIP "+(vip==null?0:vip),14,Color.WHITE,true);shown++;}
        }).addOnFailureListener(e->{if(requestedScreen952.equals(screen))text("Ranking preview unavailable right now.",13,MUTED,false);});
        button("Back to Profile",CARD,this::profile);
    }''')

# Add global ranking helpers before transaction history.
p=pkg/'MainActivity.java'; s=p.read_text()
marker='''    private void transactionHistoryPage(){'''
helpers=r'''    private long rankMetric952(com.google.firebase.firestore.DocumentSnapshot d,String mode){Long level=d.getLong("level"),xp=d.getLong("xp"),vip=d.getLong("vipLevel"),vp=d.getLong("vipPoints");long l=level==null?1:level,x=xp==null?0:xp,v=vip==null?0:vip,p=vp==null?0:vp;return "vip".equals(mode)?v*1000000000L+p:l*1000000000L+x;}
    private void showGlobalRank952(String mode){
        if(firestore==null){Toast.makeText(this,"Rankings unavailable",Toast.LENGTH_SHORT).show();return;}
        final String title="vip".equals(mode)?"💎 VIP Progress Rank":"⭐ Level & XP Rank";
        LinearLayout load=new LinearLayout(this);load.setGravity(Gravity.CENTER);android.widget.ProgressBar spin=new android.widget.ProgressBar(this);load.addView(spin,new LinearLayout.LayoutParams(dp(42),dp(42)));TextView msg=new TextView(this);msg.setText("  Loading real KING profiles…");msg.setTextSize(13);load.addView(msg);
        final AlertDialog waiting=new AlertDialog.Builder(this).setTitle(title).setView(load).setCancelable(false).create();waiting.show();
        firestore.collection("public_profiles").limit(100).get().addOnSuccessListener(q->{
            if(waiting.isShowing())waiting.dismiss();List<com.google.firebase.firestore.DocumentSnapshot> docs=new ArrayList<>(q.getDocuments());java.util.Collections.sort(docs,(a,b)->Long.compare(rankMetric952(b,mode),rankMetric952(a,mode)));
            LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(12),dp(8),dp(12),dp(10));ScrollView sc=new ScrollView(this);LinearLayout rows=new LinearLayout(this);rows.setOrientation(LinearLayout.VERTICAL);sc.addView(rows);
            int rank=1;for(com.google.firebase.firestore.DocumentSnapshot d:docs){if(rank>30)break;String n=d.getString("displayName");if(n==null||n.trim().isEmpty())n="KING User";Long lv=d.getLong("level"),vip=d.getLong("vipLevel"),xp=d.getLong("xp"),vp=d.getLong("vipPoints");String medal=rank==1?"🥇":rank==2?"🥈":rank==3?"🥉":"#"+rank;TextView row=new TextView(this);row.setText(medal+"  "+n+"\\n"+("vip".equals(mode)?"VIP "+(vip==null?0:vip)+" • "+(vp==null?0:vp)+" points":"Lv."+(lv==null?1:lv)+" • "+(xp==null?0:xp)+" XP"));row.setTextSize(13);row.setTextColor(0xff27232c);row.setTypeface(null,Typeface.BOLD);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(12),dp(5),dp(12),dp(5));row.setBackground(background(rank<=3?0xfffff4ca:0xfff7f5f9,12));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(58));lp.setMargins(0,dp(2),0,dp(2));rows.addView(row,lp);rank++;}if(rank==1){TextView empty=new TextView(this);empty.setText("No public KING profiles available yet.");empty.setGravity(Gravity.CENTER);rows.addView(empty,new LinearLayout.LayoutParams(-1,dp(100)));}root.addView(sc,new LinearLayout.LayoutParams(-1,Math.min(dp(620),Math.max(dp(120),(rank-1)*dp(61)))));new AlertDialog.Builder(this).setTitle(title).setView(root).setPositiveButton("Refresh",(d,w)->showGlobalRank952(mode)).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->{if(waiting.isShowing())waiting.dismiss();new AlertDialog.Builder(this).setTitle(title).setMessage("Ranking unavailable right now.").setPositiveButton("Retry",(d,w)->showGlobalRank952(mode)).setNegativeButton("Close",null).show();});
    }

'''
if marker not in s: raise SystemExit('Main ranking helper marker missing')
s=s.replace(marker,helpers+marker,1);p.write_text(s)

gradle=root/'app/build.gradle'; g=gradle.read_text()
old="versionCode 142; versionName '9.5.1-parity-batch2'"
if old not in g: raise SystemExit('v9.5.1 version marker missing')
gradle.write_text(g.replace(old,"versionCode 143; versionName '9.5.2-parity-batch3'",1))
print('KING Plus v9.5.2 Relationship Visitors Collection Rank parity batch applied')
