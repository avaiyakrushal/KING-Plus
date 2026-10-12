#!/usr/bin/env python3
"""Apply original 3D live walk/dance directly to VERIFIED v9.9.28 release source."""
from pathlib import Path
import re,sys
root=Path(sys.argv[1]);src=root/'app/src/main/java/com/kingplus/social'
p=src/'KingNative3DRoom.java';s=p.read_text()
def rep(old,new,label):
 global s
 n=s.count(old)
 if n!=1:raise RuntimeError(f'{label}: expected 1 anchor, found {n}')
 s=s.replace(old,new,1)
rep('import com.google.firebase.firestore.ListenerRegistration;',
'''import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.FieldValue;
import java.util.HashMap;
import java.util.Map;''','Firestore imports')
rep('        final TextView hint=new TextView(host);',
'''        final Map<String,KingAvatarMotion9929.Pose> poses9929=new HashMap<>();
        final ListenerRegistration[] motionWatcher9929={null};
        final boolean[] writing9929={false};
        final LinearLayout actionRow9929=new LinearLayout(host);
        actionRow9929.setGravity(Gravity.CENTER);
        final String[] actionNames9929={"◀","▲","▼","▶","💃 Dance"};
        final int[] dx9929={-1,0,0,1,0};
        final int[] dz9929={0,-1,1,0,0};
        for(int a=0;a<actionNames9929.length;a++){
            final int index=a;
            TextView key=new TextView(host);
            key.setText(actionNames9929[a]);key.setGravity(Gravity.CENTER);
            key.setTextSize(a==4?12:20);key.setTextColor(Color.WHITE);
            key.setBackgroundColor(a==4?0xff6130a1:0xff303e6a);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,48*d,a==4?1.4f:1f);
            lp.setMargins(2*d,0,2*d,0);actionRow9929.addView(key,lp);
            key.setOnClickListener(v->{
                if(writing9929[0]||host.isFinishing()||host.isDestroyed())return;
                String uid=user.getUid();int seated=-1;
                for(int n=0;n<12;n++)if(uid.equals(liveUids[n])){seated=n;break;}
                if(seated<0){status.setText("Join a real Party seat before moving your avatar");return;}
                KingAvatarMotion9929.Pose current=poses9929.get(uid);
                if(current==null)current=new KingAvatarMotion9929.Pose(
                    (seated%6-2.5f)*1.60f,seated/6==0?-1.55f:1.22f,"idle");
                KingAvatarMotion9929.Pose next=index==4?current.dance():
                    current.move(dx9929[index],dz9929[index]);
                Map<String,Object> data=new HashMap<>();
                data.put("uid",uid);
                data.put("x",(double)next.x);data.put("z",(double)next.z);
                data.put("action",next.action);
                data.put("updatedAt",FieldValue.serverTimestamp());
                writing9929[0]=true;
                db.collection("live_rooms").document(roomId).collection("avatar_motion")
                  .document(uid).set(data).addOnCompleteListener(t->{
                    writing9929[0]=false;
                    if(host.isFinishing()||host.isDestroyed())return;
                    status.setText(t.isSuccessful()?
                        "🌐 Live Avatar "+("dance".equals(next.action)?"dancing":"moved")+
                        " • changes sent to other room members":
                        "3D movement unavailable • Firebase rules deployment may be required");
                  });
            });
        }
        content.addView(actionRow9929,new LinearLayout.LayoutParams(-1,55*d));
        final TextView hint=new TextView(host);''','actual motion controls')
rep('hint.setText("Drag to rotate • Pinch to zoom • 3D seats reflect live Firebase data");',
    'hint.setText("Use arrows to walk • Dance • Drag to orbit • Pinch to zoom");','new instructions')
rep('            if(listener[0]!=null){listener[0].remove();listener[0]=null;}',
'''            if(listener[0]!=null){listener[0].remove();listener[0]=null;}
            if(motionWatcher9929[0]!=null){motionWatcher9929[0].remove();motionWatcher9929[0]=null;}''',
    'unsubscribe motion listener on close')
rep('        dialog.show();',
'''        dialog.show();
        motionWatcher9929[0]=db.collection("live_rooms").document(roomId)
            .collection("avatar_motion").addSnapshotListener((snap,error)->{
                if(!active[0]||host.isFinishing()||host.isDestroyed())return;
                if(error!=null){
                    status.setText("Avatar movement offline: "+error.getMessage());
                    return;
                }
                poses9929.clear();
                if(snap!=null)for(DocumentSnapshot doc:snap.getDocuments()){
                    String id=doc.getString("uid"),action=doc.getString("action");
                    Double x=doc.getDouble("x"),z=doc.getDouble("z");
                    if(id==null||!id.equals(doc.getId())||x==null||z==null||
                        !KingAvatarMotion9929.actionAllowed(action))continue;
                    try{poses9929.put(id,new KingAvatarMotion9929.Pose(x.floatValue(),z.floatValue(),action));}
                    catch(IllegalArgumentException ignored){}
                }
                surface.updateMotions(liveUids,poses9929);
            });''','subscribe real live pose collection')
rep('                StringBuilder namesLine=new StringBuilder();',
'''                surface.updateMotions(liveUids,poses9929);
                StringBuilder namesLine=new StringBuilder();''','motion refresh on seat change')
rep('        @Override public boolean onTouchEvent(MotionEvent event){',
'''        void updateMotions(String[] uids,Map<String,KingAvatarMotion9929.Pose> poses){
            float[] xs=new float[12],zs=new float[12];boolean[] dances=new boolean[12];
            java.util.Arrays.fill(xs,Float.NaN);
            java.util.Arrays.fill(zs,Float.NaN);
            for(int i=0;i<12;i++){
                KingAvatarMotion9929.Pose p=poses.get(uids[i]);
                if(p!=null){xs[i]=p.x;zs[i]=p.z;dances[i]="dance".equals(p.action);}
            }
            scene.motionX=xs;scene.motionZ=zs;scene.motionDance=dances;
            requestRender();
        }
        @Override public boolean onTouchEvent(MotionEvent event){''','GLES transfer')
rep('        volatile String[] occupantUids=new String[12];',
'''        volatile String[] occupantUids=new String[12];
        volatile float[] motionX=new float[12],motionZ=new float[12];
        volatile boolean[] motionDance=new boolean[12];''','thread visible poses')
rep('''                float x=(col-2.5f)*1.60f,z=row==0?-1.55f:1.22f;
                box(x,.05f,z,1.16f,.13f,.9f,.24f,.25f,.48f);
                box(x,.1f,z-.36f,1.12f,.08f,.06f,.75f,.62f,.23f);
                if(i>=live.length||!live[i])continue;''',
'''                float seatX=(col-2.5f)*1.60f,seatZ=row==0?-1.55f:1.22f;
                box(seatX,.05f,seatZ,1.16f,.13f,.9f,.24f,.25f,.48f);
                box(seatX,.1f,seatZ-.36f,1.12f,.08f,.06f,.75f,.62f,.23f);
                if(i>=live.length||!live[i])continue;
                float x=i<motionX.length&&!Float.isNaN(motionX[i])?motionX[i]:seatX;
                float z=i<motionZ.length&&!Float.isNaN(motionZ[i])?motionZ[i]:seatZ;
                boolean dancing=i<motionDance.length&&motionDance[i];''','live avatar positions')
rep('''                float bob=(float)Math.sin(t*1.5+i*.9f)*.035f;''',
'''                float bob=(float)Math.sin(t*(dancing?6f:1.5f)+i*.9f)*(dancing?.17f:.035f);''',
'live dance bounce')
rep('''                float wave=(float)Math.sin(t*2+i)*.12f;''',
'''                float wave=(float)Math.sin(t*(dancing?8f:2f)+i)*(dancing?.29f:.12f);''',
'live dance arm movements')
p.write_text(s)
rules=root/'firestore.rules';rr=rules.read_text()
marker='      match /knife_duel_scores/{playerUid} {'
assert rr.count(marker)==1
secured='''      // An authenticated PARTY MEMBER can publish only their own bounded avatar pose.
      match /avatar_motion/{motionUid} {
        allow read: if roomAccess(roomId) && roomMember(roomId);
        allow create: if roomAccess(roomId) && roomMember(roomId)
          && request.auth.uid == motionUid
          && request.resource.data.keys().hasOnly(['uid','x','z','action','updatedAt'])
          && request.resource.data.uid == motionUid
          && request.resource.data.x is number
          && request.resource.data.x >= -4.4 && request.resource.data.x <= 4.4
          && request.resource.data.z is number
          && request.resource.data.z >= -2.6 && request.resource.data.z <= 2.7
          && request.resource.data.action in ['idle','walk','dance']
          && request.resource.data.updatedAt == request.time;
        allow update: if roomAccess(roomId) && roomMember(roomId)
          && request.auth.uid == motionUid
          && resource.data.uid == motionUid && request.resource.data.uid == motionUid
          && request.resource.data.diff(resource.data).affectedKeys()
               .hasOnly(['x','z','action','updatedAt'])
          && request.resource.data.x is number
          && request.resource.data.x >= -4.4 && request.resource.data.x <= 4.4
          && request.resource.data.z is number
          && request.resource.data.z >= -2.6 && request.resource.data.z <= 2.7
          && request.resource.data.action in ['idle','walk','dance']
          && request.resource.data.updatedAt == request.time;
        allow delete: if roomAccess(roomId) && motionUid == request.auth.uid;
      }

'''
rules.write_text(rr.replace(marker,secured+marker,1))
(src/'KingAvatarMotion9929.java').write_text(
 Path('tools/v9929/KingAvatarMotion9929.java').read_text())
gradle=root/'app/build.gradle';g=gradle.read_text()
old_version="versionCode 207; versionName '9.9.28-3d-avatar-world-test'"
assert g.count(old_version)==1,'Expected verified KING v9.9.28 Android source version'
g=g.replace(old_version,"versionCode 208; versionName '9.9.29-live-avatar-motion-test'")
gradle.write_text(g)
assert 'videoMatchIncoming9922' in (src/'KingMultiVideoActivity.java').read_text()
assert 'openLiveKnife9923' in (src/'PartyActivity.java').read_text()
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
print('PASS original v9.9.28 release successfully upgraded to real 3D Party walk & dance')
