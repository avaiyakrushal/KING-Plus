#!/usr/bin/env python3
"""KING Plus v9.9.20: real compact Party seats and owned-mic privacy.
Preserve all v9.9.19 features and independent original source.
"""
from pathlib import Path
import sys,re

base=Path(sys.argv[1])
party=base/"app/src/main/java/com/kingplus/social/PartyActivity.java"
s=party.read_text(encoding="utf-8")

def swap(old,new,label):
    global s
    n=s.count(old)
    assert n==1,f"{label} anchor changed ({n})"
    s=s.replace(old,new,1)

# Prior smaller-screen seat height in renderParty() was overwritten each
# time rebuildSeats() ran; size the actual rows and avatars consistently.
swap(
"""        final int columns=displaySeatsV532==10?5:4;final int rows=(displaySeatsV532+columns-1)/columns;
        android.view.ViewGroup.LayoutParams layout=seatsBox.getLayoutParams();if(layout!=null){layout.height=dp(rows*80+(rows-1)*8+10);seatsBox.setLayoutParams(layout);}""",
"""        final int columns=displaySeatsV532==10?5:4;
        final int rows=(displaySeatsV532+columns-1)/columns;
        final float density9920=Math.max(1f,getResources().getDisplayMetrics().density);
        final int heightDp9920=(int)(getResources().getDisplayMetrics().heightPixels/density9920);
        final int widthDp9920=(int)(getResources().getDisplayMetrics().widthPixels/density9920);
        final boolean compact9920=heightDp9920<720||widthDp9920<370;
        final int avatarDp9920=compact9920?(widthDp9920<340?40:44):56;
        final int labelDp9920=compact9920?19:22;
        final int rowDp9920=compact9920?68:80;
        final int rowGapDp9920=compact9920?4:8;
        android.view.ViewGroup.LayoutParams layout=seatsBox.getLayoutParams();
        if(layout!=null){
            layout.height=dp(rows*rowDp9920+(rows-1)*rowGapDp9920+10);
            seatsBox.setLayoutParams(layout);
        }""","grid-responsive-computed-size")
swap("seat.addView(avatarFrame,new LinearLayout.LayoutParams(dp(56),dp(56)));",
     "seat.addView(avatarFrame,new LinearLayout.LayoutParams(dp(avatarDp9920),dp(avatarDp9920)));",
     "compact-avatar")
swap("seat.addView(lab,new LinearLayout.LayoutParams(-1,dp(22)));",
     "seat.addView(lab,new LinearLayout.LayoutParams(-1,dp(labelDp9920)));",
     "compact-seat-label")
swap("row.addView(seat,new LinearLayout.LayoutParams(0,dp(80),1));",
     "row.addView(seat,new LinearLayout.LayoutParams(0,dp(rowDp9920),1));",
     "compact-seat-item")
swap("LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(80)); if(r>0) rp.setMargins(0,dp(8),0,0); seatsBox.addView(row,rp);",
     "LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(rowDp9920)); if(r>0) rp.setMargins(0,dp(rowGapDp9920),0,0); seatsBox.addView(row,rp);",
     "compact-grid-row")

# A stale Android onResume() or moderator mute-all callback must not change
# the microphone bit on a seat now belonging to a different UID.
anchor="""    private void refreshMicControl(){"""
insert="""    /**
     * Privacy guard: turn OFF this account's current live mic only if Firestore
     * still says the same UID owns that seat. Local UI numbers may be stale
     * after a multi-phone room switch or moderator's seat reassignment.
     */
    private void muteOwnedSeat9920(String cause9920){
        if(db==null||user==null||!cloudRoom||roomId==null||mySeat<1)return;
        final String uid9920=user.getUid();
        final String room9920=roomId;
        final int seat9920=mySeat;
        final DocumentReference ref9920=db.collection("live_rooms").document(room9920)
            .collection("seats").document(String.valueOf(seat9920));
        db.runTransaction(tx9920->{
            DocumentSnapshot doc9920=tx9920.get(ref9920);
            if(doc9920.exists()&&uid9920.equals(doc9920.getString("uid"))
                &&Boolean.TRUE.equals(doc9920.getBoolean("micOn")))
                tx9920.update(ref9920,"micOn",false);
            return null;
        }).addOnFailureListener(error9920->
            KingStability.nonFatal(this,"mic-owner-guard-9920:"+cause9920,error9920));
    }

"""
swap(anchor,insert+anchor,"mic-owner-helper")
swap(
"""                db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(mySeat)).update("micOn",false).addOnFailureListener(e->{});""",
"""                muteOwnedSeat9920("return-from-external-voice");""",
"on-resume-mic-owner")
swap(
"""if(mySeat>0)room.collection("seats").document(String.valueOf(mySeat)).update("micOn",false).addOnFailureListener(x->{});""",
"""if(mySeat>0)muteOwnedSeat9920("moderator-mute-all");""",
"mute-all-mic-owner")

# A stale and unreachable branch was pretending the same occupied seat could
# be directly claimed after the first check returned; remove this ambiguity.
swap(
"""            if(existing!=null){openSeatProfile(no);return;}
            if(existing!=null){cloudSeatAction(no);return;}""",
"""            if(existing!=null){openSeatProfile(no);return;}""",
"occupied-seat-duplicate")

# Keep the previous responsive selection of the actual stage; the new grid
# size now comes from rebuildSeats(), not a hard-coded 148/178 layout.
swap(
"""        int seatsDp9916=displayDp9916<680?148:178;
        roomRoot.addView(seatsBox,new LinearLayout.LayoutParams(-1,dp(seatsDp9916))); rebuildSeats();""",
"""        int seatsDp9916=displayDp9916<720?152:178;
        roomRoot.addView(seatsBox,new LinearLayout.LayoutParams(-1,dp(seatsDp9916)));
        rebuildSeats(); // compute final row/avatar dimensions based on current screen""",
"party-stage-label")

party.write_text(s,encoding="utf-8")
gfile=base/"app/build.gradle";g=gfile.read_text(encoding="utf-8")
g,n=re.subn(r"versionCode\s+198;\s*versionName\s+'9\.9\.19[^']*'",
            "versionCode 199; versionName '9.9.20-compact-stage-mic-privacy-test'",g)
assert n==1,"Must use v9.9.19 signed source as input"
gfile.write_text(g,encoding="utf-8")

for requirement in (
    'KingLiveChessActivity.class',
    'approveOrMoveMemberSeat9913',
    'chatRoom9918',
    'whereEqualTo("uid",leavingUid9919)',
    'KingNative3DRoom.open(',
    'muteOwnedSeat9920("moderator-mute-all")',
    'rowDp9920'
):
    assert requirement in s,requirement
assert s.count('muteOwnedSeat9920(')==3
assert s.count('if(existing!=null){cloudSeatAction(no);return;}')==0
assert (base/"app/src/main/java/com/kingplus/social/KingLiveChessActivity.java").is_file()
print("PASS v9.9.20 compact seats, seat owner mic guards, no duplicate seat actions and full Chess retention")
