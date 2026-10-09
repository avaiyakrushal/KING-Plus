"""Buildable v9.6.2 Ludo turn safety patch on successful v9.6.1 source.

- Honor 60 seconds before allowing idle-turn skips, with opponent-only authorization.
- Require actual room owner and all players Ready before starting.
- Guard stale Firestore callbacks after match switch/Activity destruction.
- Avoid invalid turn/piece dereference on damaged remote state.
- Preserve no-billing/test OTP, native crash watcher, room invite links and emoji.
"""
from pathlib import Path
import shutil,sys
root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
game=pkg/'OnlineLudoActivity.java'
shutil.copy2(Path(__file__).with_name('KingLudoTurn962.java'),pkg/'KingLudoTurn962.java')

def patch(path,old,new,label):
    p=pkg/path
    t=p.read_text()
    count=t.count(old)
    if count != 1:
        raise SystemExit(f'{label}: expected exactly one marker, got {count}: {old[:140]!r}')
    p.write_text(t.replace(old,new,1))
    print('PASS',label)

patch('OnlineLudoActivity.java',
      '        super.onCreate(b);\n        try{db=FirebaseFirestore.getInstance();',
      '        super.onCreate(b);\n        KingCrashWatch958.mark(this,"online-ludo-created");\n        try{db=FirebaseFirestore.getInstance();',
      'native process exit breadcrumb for Online Ludo')

patch('OnlineLudoActivity.java',
      'if(ps.contains(me.getUid())&&(phase.equals("roll")||phase.equals("move")||phase.equals("resolve"))){Button skip=btn("Skip inactive turn (after 60 seconds)");skip.setOnClickListener(v->transaction("timeout",o->{o.put("phase","roll");o.put("turn",(long)nextTurn(i(o.get("turn")),i(o.get("capacity"))));o.put("sixes",0L);o.put("dice",0L);action(o,"timeout",-1);}));actionsBox.addView(skip,new LinearLayout.LayoutParams(-1,dp(48)));}',
      'if(ps.contains(me.getUid())&&turn>=0&&turn<ps.size()&&!me.getUid().equals(ps.get(turn))&&(phase.equals("roll")||phase.equals("move")||phase.equals("resolve"))){Button skip=btn("Skip idle turn (60s minimum)");skip.setOnClickListener(v->skipInactiveTurn962());actionsBox.addView(skip,new LinearLayout.LayoutParams(-1,dp(48)));}',
      'timeout button available only to opposing player')

patch('OnlineLudoActivity.java',
      'private void startMatch(){transaction("start",o->{o.put("phase","roll");action(o,"start",-1);});}',
      'private void startMatch(){transaction("start",o->{if(!KingLudoTurn962.canStart(me.getUid(),s(o.get("ownerUid")),s(o.get("phase")),strList(o.get("players")),boolList(o.get("ready")),i(o.get("capacity"))))throw new IllegalStateException("Host can start only when all players are ready");o.put("phase","roll");action(o,"start",-1);});}',
      'authoritative latest-document host/ready gate before start')

insert=(
'    private void skipInactiveTurn962(){\n'
'        transaction("timeout",o->{\n'
'            int currentTurn962=i(o.get("turn"));\n'
'            Object updated962=o.get("updatedAt");\n'
'            long updatedMs962=updated962 instanceof Timestamp ? ((Timestamp)updated962).toDate().getTime() : 0L;\n'
'            if(!KingLudoTurn962.canSkip(me.getUid(),s(o.get("phase")),currentTurn962,\n'
'                    strList(o.get("players")),updatedMs962,System.currentTimeMillis()))\n'
'                throw new IllegalStateException("Only another player can skip after 60 seconds of inactivity");\n'
'            o.put("phase","roll");\n'
'            o.put("turn",(long)nextTurn(currentTurn962,i(o.get("capacity"))));\n'
'            o.put("sixes",0L);\n'
'            o.put("dice",0L);\n'
'            action(o,"timeout",-1);\n'
'        });\n'
'    }\n'
)
patch('OnlineLudoActivity.java',
      '    private boolean canStart(List<String> ps,List<Boolean> rd,int cap)',
      insert+'    private boolean canStart(List<String> ps,List<Boolean> rd,int cap)',
      'implement real 60 second server timestamp inactivity check')

patch('OnlineLudoActivity.java',
      'private void listen(){if(listener!=null)listener.remove();connection950("↻ Syncing room state…",GOLD,true);listener=ref().addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(d,e)->{',
      'private void listen(){if(listener!=null)listener.remove();final String listeningCode962=code;connection950("↻ Syncing room state…",GOLD,true);listener=ref().addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(d,e)->{if(isFinishing()||isDestroyed()||!listeningCode962.equals(code))return;',
      'discard stale room snapshots after Activity exit/reconnect')

patch('OnlineLudoActivity.java',
      'private boolean canMove(int seat,int piece){if(!serverReady||state==null||me==null||!"move".equals(s(state.get("phase"))))return false;',
      'private boolean canMove(int seat,int piece){if(piece<0||piece>=4||!serverReady||state==null||me==null||!"move".equals(s(state.get("phase"))))return false;',
      'prevent invalid Ludo piece indices')

gradle=root/'app/build.gradle';t=gradle.read_text()
old="versionCode 152; versionName '9.6.1-native-crash-attribution'"
if t.count(old)!=1:raise SystemExit('v9.6.1 source baseline missing')
gradle.write_text(t.replace(old,"versionCode 153; versionName '9.6.2-ludo-turn-guards'",1))
rules=Path(__file__).parents[2]/'firestore.rules'
if not rules.exists():raise SystemExit('updated firestore.rules not found in checkout')
updated=rules.read_text()
if "request.time >= resource.data.updatedAt + duration.value(60,'s')" not in updated:
    raise SystemExit('server-side 60-second timeout validation missing in rules')
shutil.copy2(rules,root/'firestore.rules')
print('PASS staged updated Firestore rules in source; backend deployment is a separate step')
