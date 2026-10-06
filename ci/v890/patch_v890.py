from pathlib import Path
import sys
root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
(pkg/'KingPresentationView.java').write_text(Path(__file__).with_name('KingPresentationView.java').read_text())
eco=pkg/'KingEcosystemProActivity.java'
s=eco.read_text()

old='''    private void hero(String a,String b){TextView h=tv(a+"\\n"+b,17,Color.WHITE,true);h.setBackground(bg(CARD,18));h.setPadding(dp(16),dp(14),dp(16),dp(14));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(10));body.addView(h,lp);}'''
new='''    private void hero(String a,String b){TextView h=tv(a+"\\n"+b,17,Color.WHITE,true);h.setBackground(bg(CARD,18));h.setPadding(dp(16),dp(14),dp(16),dp(14));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(8));body.addView(h,lp);KingPresentationView visual=new KingPresentationView(this,route);LinearLayout.LayoutParams vp=new LinearLayout.LayoutParams(-1,dp(126));vp.setMargins(0,0,0,dp(10));body.addView(visual,vp);}'''
if old not in s: raise SystemExit('hero marker missing')
s=s.replace(old,new,1)

old='''section("Singer stage");LinearLayout stage=new LinearLayout(this);'''
new='''if(!roomId.isEmpty()){TextView stopStage=button("■ Stop shared KTV stage",this::stopKtvStage890);LinearLayout.LayoutParams slp=new LinearLayout.LayoutParams(-1,dp(44));slp.setMargins(0,dp(8),0,0);body.addView(stopStage,slp);}section("Singer stage");LinearLayout stage=new LinearLayout(this);'''
if old not in s: raise SystemExit('KTV singer stage marker missing')
s=s.replace(old,new,1)

old='''    private void toggleRecording(){'''
new='''    private void stopKtvStage890(){if(!room())return;Map<String,Object>m=new HashMap<>();m.put("phase","idle");m.put("song","Waiting for singer");m.put("singerName","—");m.put("durationSec",0);m.put("startedAtMs",0);m.put("updatedAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).collection("room_settings").document("ktv").set(m,SetOptions.merge()).addOnSuccessListener(v->toast("KTV stage stopped")).addOnFailureListener(e->toast("Host/co-host permission required"));}
    private void toggleRecording(){'''
if old not in s: raise SystemExit('toggleRecording marker missing')
s=s.replace(old,new,1)

old='''row("📣","Family Activity Feed","Members can post safe activity events",()->familyActivity(code,"activity"));row("📤","Share Family Code","Invite friends using "+code,()->{'''
new='''row("📣","Family Activity Feed","Open recent family activity",()->familyFeed890(code));row("➕","Post Family Activity","Add a safe family activity event",()->familyActivity(code,"activity"));row("📤","Share Family Code","Invite friends using "+code,()->{'''
if old not in s: raise SystemExit('Family activity row marker missing')
s=s.replace(old,new,1)

old='''    private void contributeFamily(String code,long amount){'''
new='''    private void familyFeed890(String code){if(!cloud())return;db.collection("families").document(code).collection("activities").orderBy("createdAt",Query.Direction.DESCENDING).limit(40).get().addOnSuccessListener(q->{ArrayList<String>rows=new ArrayList<>();for(DocumentSnapshot d:q.getDocuments()){String who=safe(d.getString("name"),"Family member"),type=safe(d.getString("type"),"activity"),txt=safe(d.getString("text"),"Family activity");rows.add(("checkin".equals(type)?"✅ ":"📣 ")+who+"\\n"+txt);}if(rows.isEmpty())rows.add("No Family activity yet.");new AlertDialog.Builder(this).setTitle("📣 Family Activity Feed").setItems(rows.toArray(new String[0]),null).setPositiveButton("Post activity",(d,w)->familyActivity(code,"activity")).setNegativeButton("Close",null).show();}).addOnFailureListener(e->toast("Family feed unavailable"));}
    private void contributeFamily(String code,long amount){'''
if old not in s: raise SystemExit('contributeFamily marker missing')
s=s.replace(old,new,1)

old='''    private void rankRow(int n,String name,String score,String uid){String medal=n==1?"🥇":n==2?"🥈":n==3?"🥉":"#"+n;TextView r=tv(medal+"   "+name+"\\n      "+score,14,Color.WHITE,true);r.setBackground(bg(CARD,14));r.setOnClickListener(v->{Intent i=new Intent(this,KingPublicProfileActivity.class);i.putExtra("uid",uid);i.putExtra("name",name);startActivity(i);});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(58));lp.setMargins(0,dp(3),0,dp(3));body.addView(r,lp);}'''
new='''    private void rankRow(int n,String name,String score,String uid){boolean top=n<=3;String medal=n==1?"🥇":n==2?"🥈":n==3?"🥉":"#"+n;TextView r=tv(medal+"   "+name+"\\n      "+score,top?15:14,Color.WHITE,true);int card=n==1?0xff6b5420:n==2?0xff4c4961:n==3?0xff6b4435:CARD;r.setBackground(bg(card,top?18:14));r.setGravity(Gravity.CENTER_VERTICAL);r.setOnClickListener(v->{Intent i=new Intent(this,KingPublicProfileActivity.class);i.putExtra("uid",uid);i.putExtra("name",name);startActivity(i);});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(top?68:58));lp.setMargins(0,dp(3),0,dp(3));body.addView(r,lp);}'''
if old not in s: raise SystemExit('rankRow marker missing')
s=s.replace(old,new,1)

eco.write_text(s)
print('v8.9.0 visual parity patch applied')
