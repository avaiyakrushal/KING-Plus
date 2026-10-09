"""v9.5.9: phone-friendly shareable Party deep links and actual room-ID joining.
No Firestore permission changes, no-billing/test OTP untouched.
"""
from pathlib import Path
import shutil,sys
root=Path(sys.argv[1]);pkg=root/'app/src/main/java/com/kingplus/social'
shutil.copy2(Path(__file__).with_name('KingRoomInvite959.java'),pkg/'KingRoomInvite959.java')

def replace(path,old,new,label):
 p=pkg/path;s=p.read_text()
 if s.count(old)!=1:raise SystemExit(label+': marker count '+str(s.count(old))+' '+repr(old[:100]))
 p.write_text(s.replace(old,new,1))
 print('PASS',label)

# Existing share message includes a valid document ID but cannot be tapped to open the app.
p=pkg/'PartyActivity.java'
s=p.read_text()
start=s.find('    private void shareRoom(){')
if start<0:raise SystemExit('shareRoom function missing')
end=s.find('\n',start)
if end<0:raise SystemExit('shareRoom is not a one-line method; update patch')
line=s[start:end]
if not line.rstrip().endswith('}'):raise SystemExit('shareRoom expected one-line method')
new=r'''    private void shareRoom(){
        if(!cloudRoom || roomId==null || roomId.trim().isEmpty()){
            toast("Open a live Party room before sharing.");
            return;
        }
        String link959=KingRoomInvite959.link(roomId);
        if(link959.isEmpty()){toast("Room ID is invalid for sharing");return;}
        String message959="Join my KING Plus Party: "+(roomName==null?"Live Party":roomName)
            +"\nOpen room: "+link959
            +"\nRoom Code: "+shortId()
            +"\nKINGROOM:"+roomId
            +"\n\nInstall/open KING Plus, sign in, and tap the link. If the link is not clickable, Party → Join by link / ID and paste this message."
            +"\nPrivate/password rooms still require host approval or their existing access checks.";
        Intent share959=new Intent(Intent.ACTION_SEND);
        share959.setType("text/plain");
        share959.putExtra(Intent.EXTRA_TEXT,message959);
        try{startActivity(Intent.createChooser(share959,"Share Party Room"));}
        catch(Exception e){KingStability.nonFatal(this,"share-party-invite-959",e);toast("No app available to share Party");}
    }

    private boolean tryJoinRoomInput959(String input959){
        final String id959=KingRoomInvite959.parse(input959);
        if(id959.isEmpty())return false;
        if(user==null||db==null){toast("Sign in first to join Party rooms");return true;}
        KingCrashWatch958.mark(this,"party-join-invite-959");
        db.collection("live_rooms").document(id959).get()
            .addOnSuccessListener(doc959->{
                if(isFinishing()||isDestroyed())return;
                if(doc959==null||!doc959.exists()||Boolean.TRUE.equals(doc959.getBoolean("closed"))){
                    toast("Party room not found or already closed");return;
                }
                openRoomDoc891(doc959); // existing private/ban/password enforcement
            }).addOnFailureListener(e->{
                if(!isFinishing()&&!isDestroyed())toast("Could not look up Party room: "+msg(e));
            });
        return true;
    }

    private void joinRoomLinkDialog959(){
        EditText box959=new EditText(this);
        box959.setSingleLine(true);
        box959.setHint("Paste Room ID or kingplus://party/...");
        box959.setSelectAllOnFocus(true);
        new AlertDialog.Builder(this).setTitle("🔗 Join Party by link / ID")
            .setMessage("Paste the shared KINGROOM code or complete room invitation.")
            .setView(box959)
            .setPositiveButton("Join",(d,w)->{
                if(!tryJoinRoomInput959(box959.getText().toString().trim()))
                    toast("Paste a valid full Room ID or KING Plus invite");
            }).setNegativeButton("Cancel",null).show();
    }'''
s=s[:start]+new+s[end:]
p.write_text(s)
print('PASS', 'real room invite links with fallback and exact lookup')

replace('PartyActivity.java',
    '        root.addView(chipScroll,new LinearLayout.LayoutParams(-1,dp(46)));',
    '''        root.addView(chipScroll,new LinearLayout.LayoutParams(-1,dp(46)));
        TextView joinLink959=pill("🔗 Join by link / Room ID",0xff6244a0,this::joinRoomLinkDialog959);
        joinLink959.setTextSize(12);
        joinLink959.setContentDescription("Join KING Plus Party using invitation link or full room ID");
        LinearLayout.LayoutParams joinLp959=new LinearLayout.LayoutParams(-1,dp(42));
        joinLp959.setMargins(dp(12),0,dp(12),dp(5));
        root.addView(joinLink959,joinLp959);''',
    'visible Party lobby link join')

# Android allows explicit custom link opening on exported MainActivity.
manifest=root/'app/src/main/AndroidManifest.xml'
m=manifest.read_text()
needle='<activity android:name=".MainActivity" android:exported="true" android:launchMode="singleTask">'
if m.count(needle)!=1:raise SystemExit('unexpected MainActivity manifest marker')
manifest.write_text(m.replace(needle,needle+'''
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="kingplus" android:host="party" />
            </intent-filter>''',1))
print('PASS','MainActivity opens kingplus party links')

replace('MainActivity.java',
  '        KingStability.install(this);',
  '''        KingStability.install(this);
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(()->{
            if(isFinishing()||isDestroyed())return;
            Intent incoming959=getIntent();
            String incomingRoom959=KingRoomInvite959.parse(incoming959==null?null:incoming959.getDataString());
            if(!incomingRoom959.isEmpty())routePartyInvite959(incoming959);
            else if(firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null){
                String pending959=getPreferences(0).getString("pendingRoomInvite959","");
                if(!pending959.isEmpty())routePartyInvite959(new Intent().setData(
                    android.net.Uri.parse(KingRoomInvite959.link(pending959))));
            }
        },1250L);''',
  'handle deep link on app launch after login initialization')

replace('MainActivity.java',
  '        setIntent(intent);\n        safeUiAction940("main-on-new-intent",()->{',
  '''        setIntent(intent);
        if(routePartyInvite959(intent))return;
        safeUiAction940("main-on-new-intent",()->{''',
  'handle Party links for running MainActivity singleTask')

replace('MainActivity.java',
  '    private void openPartyActivity() {',
  '''    private boolean routePartyInvite959(Intent input959){
        String id959=KingRoomInvite959.parse(input959==null?null:input959.getDataString());
        if(id959.isEmpty())return false;
        if(firebaseAuth==null||firebaseAuth.getCurrentUser()==null){
            getPreferences(0).edit().putString("pendingRoomInvite959",id959).apply();
            Toast.makeText(this,"Sign in to KING Plus, then open the Party invite again.",Toast.LENGTH_LONG).show();
            login();
            return true;
        }
        getPreferences(0).edit().remove("pendingRoomInvite959").apply();
        KingCrashWatch958.mark(this,"main-open-party-invite-959");
        Intent party959=new Intent(this,PartyActivity.class);
        party959.putExtra("directRoomId",id959);
        startActivity(party959);
        return true;
    }

    private void openPartyActivity() {''',
  'route validated invite to existing Party room access checks')

gradle=root/'app/build.gradle'
s=gradle.read_text()
old="versionCode 149; versionName '9.5.8-whole-app-stability'"
if s.count(old)!=1:raise SystemExit('expected v9.5.8 source baseline')
gradle.write_text(s.replace(old,"versionCode 150; versionName '9.5.9-party-invite-links'",1))
print('PASS', 'v9.5.9 version')
