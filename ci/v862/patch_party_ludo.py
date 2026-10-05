#!/usr/bin/env python3
from pathlib import Path
import sys

root=Path(sys.argv[1]) if len(sys.argv)>1 else Path('/tmp/src')
room=root/'app/src/main/java/com/kingplus/social/RoomGameActivity.java'
ludo=root/'app/src/main/java/com/kingplus/social/OnlineLudoActivity.java'
if not room.exists() or not ludo.exists():
    raise SystemExit('Required game source files not found')

r=room.read_text()

def r_replace(old,new,label):
    global r
    if old not in r:
        raise SystemExit('PATCH_MISSING room: '+label)
    r=r.replace(old,new,1)

old_ludo='Button ludo=button("🎲 Online Ludo");ludo.setOnClickListener(v->startActivity(new Intent(this,OnlineLudoActivity.class)));LinearLayout.LayoutParams ludoLp=new LinearLayout.LayoutParams(-1,dp(52));ludoLp.setMargins(0,dp(6),0,dp(4));controls.addView(ludo,ludoLp);'
new_ludo='Button ludo=button("🎲 Online Ludo • Party Room");ludo.setOnClickListener(v->openPartyLudo862());LinearLayout.LayoutParams ludoLp=new LinearLayout.LayoutParams(-1,dp(52));ludoLp.setMargins(0,dp(6),0,dp(4));controls.addView(ludo,ludoLp);'
r_replace(old_ludo,new_ludo,'room ludo button')

anchor='    private void rpsControls(){'
helper='''    private void openPartyLudo862(){
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

'''
if anchor not in r:
    raise SystemExit('PATCH_MISSING room: rps anchor')
r=r.replace(anchor,helper+anchor,1)
room.write_text(r)

x=ludo.read_text()
def x_replace(old,new,label):
    global x
    if old not in x:
        raise SystemExit('PATCH_MISSING ludo: '+label)
    x=x.replace(old,new,1)

old_oncreate='    @Override public void onCreate(Bundle b){super.onCreate(b);try{db=FirebaseFirestore.getInstance();me=FirebaseAuth.getInstance().getCurrentUser();}catch(Exception e){db=null;me=null;}build();if(db==null||me==null){status("Sign in with Google / phone first to play Online Ludo.",0xffffa6a6);setLobbyEnabled(false);return;}String incoming=cleanCode(getIntent().getStringExtra("ludoCode"));if(!incoming.isEmpty()){codeInput.setText(incoming);joinMatch(incoming);}else {String saved=getPreferences(MODE_PRIVATE).getString("match_"+me.getUid(),"");if(!saved.isEmpty()){codeInput.setText(saved);status("Previous room: "+saved+" • Tap Join to reconnect",GOLD);}}}'
new_oncreate='''    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        try{db=FirebaseFirestore.getInstance();me=FirebaseAuth.getInstance().getCurrentUser();}catch(Exception e){db=null;me=null;}
        build();
        if(db==null||me==null){status("Sign in with Google / phone first to play Online Ludo.",0xffffa6a6);setLobbyEnabled(false);return;}
        String incoming=cleanCode(getIntent().getStringExtra("ludoCode"));
        boolean partyMode=getIntent().getBooleanExtra("partyRoomMode",false);
        boolean partyHost=getIntent().getBooleanExtra("partyRoomHost",false);
        String partyRoomId=s(getIntent().getStringExtra("partyRoomId"));
        String partyRoomName=s(getIntent().getStringExtra("partyRoomName"));
        if(!incoming.isEmpty()){
            codeInput.setText(incoming);
            if(partyMode&&!partyRoomId.isEmpty())openPartyMatch862(incoming,partyHost,partyRoomId,partyRoomName);
            else joinMatch(incoming);
        }else {
            String saved=getPreferences(MODE_PRIVATE).getString("match_"+me.getUid(),"");
            if(!saved.isEmpty()){codeInput.setText(saved);status("Previous room: "+saved+" • Tap Join to reconnect",GOLD);}
        }
    }'''
x_replace(old_oncreate,new_oncreate,'onCreate party routing')

anchor2='    private void createMatch(int capacity){'
helper2='''    private void openPartyMatch862(String c,boolean canCreate,String partyRoomId,String partyRoomName){
        if(c.length()!=6){toast("Party Ludo code unavailable");return;}
        if(me==null||!KingNetwork.online(this)){toast("Internet connection required");return;}
        DocumentReference r=db.collection("ludo_matches").document(c);
        status(canCreate?"Opening Party Ludo room…":"Joining Party Ludo room…",GOLD);
        db.runTransaction(tx->{
            DocumentSnapshot d=tx.get(r);
            if(!d.exists()){
                if(!canCreate)throw new IllegalStateException("Host must open Party Ludo first");
                Map<String,Object> fresh=fresh(c,4);
                fresh.put("partyRoomKey",partyRoomId);
                fresh.put("partyRoomName",partyRoomName);
                tx.set(r,fresh);
                return null;
            }
            Map<String,Object> o=d.getData();
            if(o==null)throw new IllegalStateException("Party Ludo room unavailable");
            String key=s(o.get("partyRoomKey"));
            if(key.isEmpty()||!partyRoomId.equals(key))throw new IllegalStateException("Party Ludo code is already in use");
            List<String> ps=strList(o.get("players"));
            if(ps.contains(me.getUid()))return null;
            if(!"lobby".equals(s(o.get("phase"))))throw new IllegalStateException("Party Ludo match already started");
            int cap=i(o.get("capacity"));
            int seat=-1;
            for(int q=0;q<4;q++)if((cap==4||q==0||q==2)&&ps.get(q).isEmpty()){seat=q;break;}
            if(seat<0)throw new IllegalStateException("Party Ludo room is full");
            List<String> names=strList(o.get("names"));List<Boolean> ready=boolList(o.get("ready"));
            ps.set(seat,me.getUid());names.set(seat,name());ready.set(seat,false);
            Map<String,Object> n=copy(o);n.put("players",ps);n.put("names",names);n.put("ready",ready);action(n,"join",seat);tx.set(r,n);return null;
        }).addOnSuccessListener(v->{open(c);status("Party Room Ludo • "+(partyRoomName.isEmpty()?c:partyRoomName),GREEN);})
          .addOnFailureListener(e->showError("Party Ludo",e));
    }

'''
if anchor2 not in x:
    raise SystemExit('PATCH_MISSING ludo: createMatch anchor')
x=x.replace(anchor2,helper2+anchor2,1)
ludo.write_text(x)
print('v8.6.2 Party Room Ludo integration applied')
