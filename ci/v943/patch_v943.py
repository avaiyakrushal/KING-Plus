from pathlib import Path
import re,sys

root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'

def replace_once(path, old, new, label):
    p=pkg/path
    s=p.read_text()
    if old not in s:
        raise SystemExit(f'{path}: missing marker {label}')
    p.write_text(s.replace(old,new,1))

# ---------- Shared auth snapshot helper ----------
(pkg/'KingAuth.java').write_text(r'''package com.kingplus.social;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public final class KingAuth {
    private KingAuth(){}
    public static FirebaseUser user(FirebaseAuth auth){
        try{return auth==null?null:auth.getCurrentUser();}catch(Throwable ignored){return null;}
    }
    public static String uid(FirebaseAuth auth){
        FirebaseUser u=user(auth);return u==null?null:u.getUid();
    }
}
''')

# ---------- Original KING Plus ambient Party visual ----------
(pkg/'KingPartyAmbientView.java').write_text(r'''package com.kingplus.social;

import android.content.Context;
import android.graphics.*;
import android.view.View;

public final class KingPartyAmbientView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private String room="KING Party";
    private String state="LIVE";
    private int seed=1;

    public KingPartyAmbientView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
    public void setRoom(String roomName,boolean locked,boolean moderator,int online){
        room=roomName==null||roomName.trim().isEmpty()?"KING Party":roomName.trim();
        state=(locked?"🔒 ":"")+"LIVE • "+Math.max(1,online)+" online"+(moderator?" • HOST":"");
        seed=Math.abs(room.hashCode());
        invalidate();
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        float w=getWidth(),h=getHeight();if(w<=0||h<=0)return;
        int a=Color.rgb(70+(seed%70),38+((seed/7)%55),125+((seed/13)%80));
        int b=Color.rgb(130+((seed/17)%70),55+((seed/23)%70),145+((seed/29)%80));
        p.setShader(new LinearGradient(0,0,w,h,a,b,Shader.TileMode.CLAMP));
        RectF r=new RectF(0,0,w,h);c.drawRoundRect(r,h*.32f,h*.32f,p);p.setShader(null);
        p.setColor(0x28ffffff);
        c.drawCircle(w*.82f,h*.22f,h*.58f,p);c.drawCircle(w*.12f,h*.82f,h*.38f,p);
        p.setColor(Color.WHITE);p.setTextSize(Math.max(28f,h*.28f));p.setTypeface(Typeface.DEFAULT_BOLD);
        c.drawText(room,18,h*.48f,p);
        p.setTextSize(Math.max(20f,h*.18f));p.setTypeface(Typeface.DEFAULT);
        p.setColor(0xddffffff);c.drawText(state,18,h*.78f,p);
    }
}
''')

# ---------- MainActivity: eliminate auth race dereferences ----------
main=pkg/'MainActivity.java'
q=main.read_text()

# helper used by Invite Friends drawer
anchor='''    private void sideMenu(){'''
helper='''    private String publicIdOrName943(){
        com.google.firebase.auth.FirebaseUser u=KingAuth.user(firebaseAuth);
        return u==null?displayName:publicId(u.getUid());
    }

'''
if helper not in q:
    if anchor not in q: raise SystemExit('MainActivity sideMenu marker missing')
    q=q.replace(anchor,helper+anchor,1)

q=q.replace('''()->shareText("Join me on KING Plus. My ID: "+(firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null?publicId(firebaseAuth.getCurrentUser().getUid()):displayName))''',
            '''()->shareText("Join me on KING Plus. My ID: "+publicIdOrName943())''',1)

# walletPage: snapshot user once and expose retry on failure
old='''        if(firestore!=null&&firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null){
            firestore.collection("wallets").document(firebaseAuth.getCurrentUser().getUid()).get().addOnSuccessListener(doc->{Object raw=doc.get("coins");long c=raw instanceof Number?Math.max(0,Math.round(((Number)raw).doubleValue())):0;coinBalance=(int)Math.min(Integer.MAX_VALUE,c);bal.setText("💎 "+c+" Diamonds");});
        }'''
new='''        com.google.firebase.auth.FirebaseUser walletUser943=KingAuth.user(firebaseAuth);
        if(firestore!=null&&walletUser943!=null){
            firestore.collection("wallets").document(walletUser943.getUid()).get()
                .addOnSuccessListener(doc->{Object raw=doc.get("coins");long c=raw instanceof Number?Math.max(0,Math.round(((Number)raw).doubleValue())):0;coinBalance=(int)Math.min(Integer.MAX_VALUE,c);bal.setText("💎 "+c+" Diamonds");})
                .addOnFailureListener(e->{bal.setText("💎 "+Math.max(0,coinBalance)+" Diamonds");bal.setOnClickListener(v->walletPage());});
        }'''
if old not in q: raise SystemExit('MainActivity wallet auth marker missing')
q=q.replace(old,new,1)

# Snapshot current user inside profile/stat methods. Do not call getCurrentUser twice.
q=q.replace('''        final String uid=firebaseAuth.getCurrentUser().getUid();''',
            '''        final com.google.firebase.auth.FirebaseUser statsUser943=KingAuth.user(firebaseAuth);if(statsUser943==null)return;
        final String uid=statsUser943.getUid();''',1)
q=q.replace('''        String uid=firebaseAuth.getCurrentUser().getUid();''',
            '''        com.google.firebase.auth.FirebaseUser visitorUser943=KingAuth.user(firebaseAuth);if(visitorUser943==null){Toast.makeText(this,"Session expired • sign in again",Toast.LENGTH_SHORT).show();return;}
        String uid=visitorUser943.getUid();''',1)

old='''        if(firestore==null||firebaseAuth==null||firebaseAuth.getCurrentUser()==null)return;
        firestore.collection("wallets").document(firebaseAuth.getCurrentUser().getUid()).get().addOnSuccessListener(doc->{'''
new='''        com.google.firebase.auth.FirebaseUser walletRefreshUser943=KingAuth.user(firebaseAuth);
        if(firestore==null||walletRefreshUser943==null)return;
        firestore.collection("wallets").document(walletRefreshUser943.getUid()).get().addOnSuccessListener(doc->{'''
if old not in q: raise SystemExit('MainActivity refreshServerWallet marker missing')
q=q.replace(old,new,1)

old='''        if(firestore==null||firebaseAuth==null||firebaseAuth.getCurrentUser()==null){loading940.setText("Sign in to see recommendations");return;}
        String own=firebaseAuth.getCurrentUser().getUid();'''
new='''        com.google.firebase.auth.FirebaseUser recommendUser943=KingAuth.user(firebaseAuth);
        if(firestore==null||recommendUser943==null){loading940.setText("Sign in to see recommendations");return;}
        String own=recommendUser943.getUid();'''
if old not in q: raise SystemExit('MainActivity recommendations marker missing')
q=q.replace(old,new,1)

main.write_text(q)

# ---------- Party: auto-complete mic permission and add visual depth ----------
party=pkg/'PartyActivity.java'
q=party.read_text()

# Original KING ambient strip directly below room badges.
marker='''        roomRoot.addView(badges,new LinearLayout.LayoutParams(-1,dp(36)));'''
ambient='''        roomRoot.addView(badges,new LinearLayout.LayoutParams(-1,dp(36)));
        KingPartyAmbientView ambient943=new KingPartyAmbientView(this);
        ambient943.setRoom(roomName,roomPrivate,isModerator(),cloudRoom?liveMemberCount:Math.max(1,seatNames.size()));
        ambient943.setOnClickListener(v->KingSafe.run(this,"party-theme-ambient",this::themeRoomPanel));
        LinearLayout.LayoutParams ambientLp943=new LinearLayout.LayoutParams(-1,dp(62));ambientLp943.setMargins(0,dp(3),0,dp(4));
        roomRoot.addView(ambient943,ambientLp943);'''
if marker not in q: raise SystemExit('PartyActivity badges marker missing')
q=q.replace(marker,ambient,1)

# Permission result: requestCode 942 belongs to Party mic, Jitsi keeps all other requests.
perm_pat=r'''@android\.annotation\.SuppressLint\("MissingSuperCall"\)\s*.*?@Override\s+public\s+void\s+onRequestPermissionsResult\(int requestCode,String\[\] permissions,int\[\] grantResults\)\s*\{.*?(?=\n\s*@Override\s+public\s+void\s+onBackPressed)'''
perm_new='''@android.annotation.SuppressLint("MissingSuperCall")
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        if(requestCode==942){
            boolean granted=grantResults!=null&&grantResults.length>0&&grantResults[0]==android.content.pm.PackageManager.PERMISSION_GRANTED;
            if(granted){if(page!=null)page.postDelayed(()->KingSafe.run(this,"mic-permission-granted",this::toggleMic),120);}
            else toast("Microphone permission is required to speak on a Party seat");
            return;
        }
        try{org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onRequestPermissionsResult(requestCode,permissions,grantResults);}
        catch(Throwable e){KingStability.nonFatal(this,"party-jitsi-permission-result",e);}
    }'''
q,count=re.subn(perm_pat,perm_new,q,count=1,flags=re.S)
if count!=1: raise SystemExit('PartyActivity permission delegate regex missing')

# Direct room fetch failure becomes retryable rather than silently dumping user back to lobby.
q=q.replace('''.addOnFailureListener(e->{toast("Could not open Party room");renderLobby("Hot");});''',
            '''.addOnFailureListener(e->KingUiState.error(this,"Could not open Party room",msg(e),()->{if(directRoomId!=null&&!directRoomId.isEmpty())db.collection("live_rooms").document(directRoomId).get().addOnSuccessListener(doc->{if(doc!=null&&doc.exists())openRoomDoc891(doc);});else renderLobby("Hot");}));''',1)

party.write_text(q)

# ---------- Social/profile: make action failures visible/retryable ----------
pub=pkg/'KingPublicProfileActivity.java'
if pub.exists():
    q=pub.read_text()
    q=q.replace('''.addOnFailureListener(e->toast("Profile unavailable"));''',
                '''.addOnFailureListener(e->KingUiState.error(this,"Profile unavailable",e.getMessage(),this::loadProfile));''')
    pub.write_text(q)

# ---------- Version ----------
gradle=root/'app/build.gradle'
g=gradle.read_text()
old="versionCode 141; versionName '9.4.2-parity-next'"
if old not in g: raise SystemExit('build.gradle v9.4.2 marker missing')
gradle.write_text(g.replace(old,"versionCode 142; versionName '9.4.3-final-parity-pass'",1))

(root/'V9.4.3-WORKLOG.md').write_text('''# KING Plus v9.4.3 final parity pass — batch A

Completed:
- snapshot FirebaseAuth users before UID access to remove sign-out race windows
- wallet/profile/visitor/recommendation auth hardening
- Party mic permission auto-continues after grant
- Party room ambient visual strip with original KING Plus drawing
- retryable direct-room open failure
- profile failure retry where supported
- retained v9.4.2 reconnect, games, Gift/Emoji, KTV/PK, social/VIP/Family behavior
- no-billing / TEST OTP behavior unchanged

Still requires real device/multi-account verification before any crash-free/full-parity claim.
''')
print('v9.4.3 parity batch A applied')
