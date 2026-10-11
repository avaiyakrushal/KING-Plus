#!/usr/bin/env python3
"""KING Plus 9.9.2: phone-to-phone KING ID -> Follow -> DM -> Party join.

Based on signed v9.9.1 source. Uses existing backend:
public_profiles, follows, direct_threads, live_rooms. No billing / rules changes.
Every Room invite still opens PartyActivity's server-verified joining path.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
def replace_one(file,old,new,name):
    source=file.read_text()
    count=source.count(old)
    if count!=1: raise SystemExit(f'FAILED {name}: expected 1 anchor in {file}, got {count}: {old[:160]!r}')
    file.write_text(source.replace(old,new,1))
    print('PASS',name)

helper=(Path(__file__).parent/'KingSocialShare992.java')
assert helper.is_file()
(pkg/'KingSocialShare992.java').write_text(helper.read_text())
print('PASS validate shared KING ID / room link helper')

social=pkg/'SocialActivity.java'
replace_one(social,
'''        if(me!=null){
            TextView trueUid964=label("My verified account UID: "+me.getUid()+"  •  Tap to copy",11,MUTED,false);''',
'''        if(me!=null){
            final String ownerSocialUid992=me.getUid();
            final String ownKingId992=KingSocialIdentity965.publicId(ownerSocialUid992);
            LinearLayout connectBar992=new LinearLayout(this);
            connectBar992.setOrientation(LinearLayout.VERTICAL);
            connectBar992.setPadding(dp(14),dp(10),dp(14),dp(12));
            connectBar992.setBackground(bg(0xffeae4ff,18));
            TextView kingIdText992=label("👑  My KING ID: "+ownKingId992,19,PURPLE,true);
            kingIdText992.setOnClickListener(v->{
                android.content.ClipboardManager cb=(android.content.ClipboardManager)getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                if(cb!=null)cb.setPrimaryClip(android.content.ClipData.newPlainText("My KING ID",ownKingId992));
                toast("KING ID copied — friends can find you by ID");
            });
            connectBar992.addView(kingIdText992,new LinearLayout.LayoutParams(-1,dp(36)));
            TextView help992=label("Ask friends to download KING Plus, sign in with another Google account, search this ID, then Follow and Message.",12,DARK,false);
            connectBar992.addView(help992);
            LinearLayout socialActions992=new LinearLayout(this);
            socialActions992.setGravity(Gravity.CENTER);
            addQuick(socialActions992,"↗  Share\nKING ID",()->{
                Intent share=new Intent(Intent.ACTION_SEND);share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT,KingSocialShare992.shareKingId(
                  ownKingId992,ownerSocialUid992,KingSocialShare992.APP_APK_URL));
                startActivity(Intent.createChooser(share,"Invite friends to KING Plus"));
            });
            addQuick(socialActions992,"⬇  Share\nApp APK",()->{
                Intent share=new Intent(Intent.ACTION_SEND);share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT,
                    "Download KING Plus APK and sign in with your own Google account: "+
                    KingSocialShare992.APP_APK_URL);
                startActivity(Intent.createChooser(share,"Share KING Plus APK"));
            });
            addQuick(socialActions992,"🎉  Join\nParty ID",this::socialJoinParty992);
            connectBar992.addView(socialActions992,new LinearLayout.LayoutParams(-1,dp(70)));
            root.addView(connectBar992);
            TextView trueUid964=label("My verified account UID: "+me.getUid()+"  •  Tap to copy",11,MUTED,false);''',
'Show KING ID, sharing and real Join Party buttons in People page')
replace_one(social,
'''        String raw = search.getText().toString().trim();
        final int generation970=++socialViewGeneration970;''',
'''        String raw = KingSocialShare992.extractAccount(search.getText().toString().trim());
        if(!raw.equals(search.getText().toString().trim()))search.setText(raw);
        final int generation970=++socialViewGeneration970;''',
'Allow pasting entire shared KING ID or verified UID invitation into search')
replace_one(social,
'''    private void runSearch() {''',
'''    private void socialJoinParty992(){
        if(!cloudReady()){toast("Sign in before joining a Party");return;}
        final EditText roomCode992=new EditText(this);
        roomCode992.setHint("Paste Room ID or KINGROOM: invite");
        roomCode992.setSingleLine(true);
        new AlertDialog.Builder(this)
            .setTitle("🎉 Join friends in a Party Room")
            .setMessage("Paste the Room ID your friend shared. Server rules and room privacy are still enforced.")
            .setView(roomCode992)
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Join", (d,w)->{
                String id992=KingRoomInvite959.parse(roomCode992.getText().toString());
                if(id992.isEmpty()){toast("Please paste a valid full Party Room ID");return;}
                Intent join992=new Intent(this,PartyActivity.class);
                join992.putExtra("directRoomId",id992);
                join992.putExtra("displayName",
                    getSharedPreferences("MainActivity",MODE_PRIVATE).getString("name","KING User"));
                startActivity(join992);
            }).show();
    }

    private void runSearch() {''',
'Join another user Party from Firestore-validated full Room ID')
replace_one(social,
'''    @Override public void onCreate(Bundle state) {
        super.onCreate(state);''',
'''    @Override public void onCreate(Bundle state) {
        super.onCreate(state);''',
'Keep Social Activity lifecycle unchanged' ) if False else None

chat=pkg/'ChatActivity.java'
replace_one(chat,
'''    private void shareRoom() {
        String room = mainPrefs().getString("last_room", "KING Lounge"); sendText("🔗 Join me in KING Plus room: " + room);
    }''',
'''    private void shareRoom() {
        if(!cloudMode||me==null){Toast.makeText(this,
            "Sign in to share a real Firebase Party",Toast.LENGTH_LONG).show();return;}
        final String room992=mainPrefs().getString("last_live_room_id_992","");
        final String account992=mainPrefs().getString("last_live_room_account_992","");
        final String name992=mainPrefs().getString("last_live_room_name_992","KING Party");
        if(!account992.equals(me.getUid())||KingRoomInvite959.parse(room992).isEmpty()){
            new AlertDialog.Builder(this).setTitle("No recent live Party")
              .setMessage("Open and join a live KING Plus Party Room first. Then return here and share its real Room ID.")
              .setPositiveButton("Open Party",(d,w)->startActivity(new Intent(this,PartyActivity.class)))
              .setNegativeButton("Cancel",null).show();
            return;
        }
        sendText("🎉 Join me in KING Plus: "+name992+
             "\nKINGROOM:"+room992+"\n"+KingRoomInvite959.link(room992));
    }''',
'Replace non-functional room-name chat share with real joinable Firebase Room ID')
replace_one(chat,
'''        else { TextView body = label(text, 15, mine ? Color.WHITE : DARK, false); bubble.addView(body); }
        if (!media.isEmpty())''',
'''        else { TextView body = label(text, 15, mine ? Color.WHITE : DARK, false); bubble.addView(body); }
        final String fullRoomId992=cloudMode?KingRoomInvite959.parse(text):"";
        if(!fullRoomId992.isEmpty()){
            TextView joinParty992=label("🎉 Join this Party Room  →",14,
                mine?0xffffe4ac:0xff7131b5,true);
            joinParty992.setPadding(dp(6),dp(12),dp(6),dp(10));
            joinParty992.setOnClickListener(v->{
                if(me==null){Toast.makeText(this,"Sign in first",Toast.LENGTH_SHORT).show();return;}
                Intent intent992=new Intent(this,PartyActivity.class);
                intent992.putExtra("directRoomId",fullRoomId992);
                intent992.putExtra("displayName",myName);
                startActivity(intent992);
            });
            bubble.addView(joinParty992);
        }
        if (!media.isEmpty())''',
'Make shared real Party invites tappable in private chat')

party=pkg/'PartyActivity.java'
replace_one(party,
'''    private void renderParty() {
        KingCrashWatch958.mark(this,"party-render");''',
'''    private void renderParty() {
        KingCrashWatch958.mark(this,"party-render");
        // Store only a SERVER-verified joined Room for the current signed-in
        // account, never a local/fake/private bypass or unconfirmed lobby room.
        if(cloudRoom&&memberSeen&&roomId!=null&&user!=null
               &&!KingRoomInvite959.parse(roomId).isEmpty()){
            getSharedPreferences("MainActivity",MODE_PRIVATE).edit()
                .putString("last_live_room_id_992",roomId)
                .putString("last_live_room_name_992",roomName==null?"KING Party":roomName)
                .putString("last_live_room_account_992",user.getUid())
                .apply();
        }''',
'Persist room join invite only after server-acknowledged member join')

gradle=root/'app/build.gradle'
replace_one(gradle,
"versionCode 180; versionName '9.9.1-cinematic-gifts-themes-login'",
"versionCode 181; versionName '9.9.2-id-follow-chat-party-social'",
'Bump to installable 9.9.2 code 181')
print('DONE v9.9.2 ID lookup, sharing APK, Follow & realtime Message links, Party invites')
