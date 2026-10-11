#!/usr/bin/env python3
"""Independent KING Plus 3D live identity; never import reference app assets."""
from pathlib import Path
import re,sys
root=Path(sys.argv[1]);path=root/"app/src/main/java/com/kingplus/social/KingNative3DRoom.java"
s=path.read_text()
def change(old,new):
 global s
 assert s.count(old)==1,("Unexpected 3D source",old[:90],s.count(old))
 s=s.replace(old,new,1)
change("import android.app.AlertDialog;","import android.app.AlertDialog;\nimport android.content.Intent;\nimport android.widget.HorizontalScrollView;")
change("        content.addView(status,new LinearLayout.LayoutParams(-1,68*d));",
"""        content.addView(status,new LinearLayout.LayoutParams(-1,68*d));
        // Tappable actual Firebase participants, never sample/demo identities.
        final String[] liveUids=new String[12],liveNames=new String[12];
        final TextView[] seatButtons=new TextView[12];
        HorizontalScrollView seatScroll=new HorizontalScrollView(host);
        seatScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout seatRow=new LinearLayout(host);
        seatRow.setPadding(8*d,4*d,8*d,4*d);
        for(int n=0;n<12;n++){
            final int index=n;
            TextView seat=new TextView(host);
            seat.setText((n+1)+"  •  Empty");
            seat.setTextSize(11);seat.setTextColor(0xffb3acd2);
            seat.setGravity(Gravity.CENTER);
            seat.setPadding(8*d,4*d,8*d,4*d);
            seat.setBackgroundColor(0xff242044);
            seat.setContentDescription("Seat "+(n+1)+" empty");
            seat.setOnClickListener(v->{
                if(host.isFinishing()||host.isDestroyed())return;
                String uid=liveUids[index];
                if(uid==null||uid.isEmpty())return;
                Intent intent=new Intent(host,KingPublicProfileActivity.class);
                intent.putExtra("uid",uid);
                intent.putExtra("name",liveNames[index]);
                intent.putExtra("roomId",roomId);
                intent.putExtra("roomRole","MEMBER");
                host.startActivity(intent);
            });
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(106*d,42*d);
            lp.setMargins(3*d,0,3*d,0);seatRow.addView(seat,lp);
            seatButtons[n]=seat;
        }
        seatScroll.addView(seatRow);
        content.addView(seatScroll,new LinearLayout.LayoutParams(-1,52*d));""")
change("                    surface.updateOccupants(new boolean[12]);",
"""                    surface.updateOccupants(new boolean[12],new String[12]);
                    for(int i=0;i<12;i++){
                        liveUids[i]=null;liveNames[i]=null;
                        seatButtons[i].setText((i+1)+"  •  Offline");
                        seatButtons[i].setContentDescription("Seat "+(i+1)+" unavailable");
                    }""")
change("                final String[] names=new String[12];",
"                final String[] names=new String[12];\n                final String[] uids=new String[12];")
change("                    occupants[seat-1]=true;\n                    String name=doc.getString(\"name\");",
"                    occupants[seat-1]=true;\n                    uids[seat-1]=uid;\n                    String name=doc.getString(\"name\");")
change("                surface.updateOccupants(occupants);",
"""                surface.updateOccupants(occupants,uids);
                for(int i=0;i<12;i++){
                    liveUids[i]=uids[i];liveNames[i]=names[i];
                    if(uids[i]!=null){
                        String shortName=names[i].length()>10?names[i].substring(0,10)+"…":names[i];
                        seatButtons[i].setText("● "+(i+1)+" "+shortName);
                        seatButtons[i].setTextColor(Color.WHITE);
                        seatButtons[i].setContentDescription("Seat "+(i+1)+" "+names[i]+" view profile");
                    }else{
                        seatButtons[i].setText((i+1)+"  •  Empty");
                        seatButtons[i].setTextColor(0xffb3acd2);
                        seatButtons[i].setContentDescription("Seat "+(i+1)+" empty");
                    }
                }""")
change("""        void updateOccupants(boolean[] seats){
            scene.occupied=Arrays.copyOf(seats,12);
            requestRender();
        }""",
"""        void updateOccupants(boolean[] seats,String[] uids){
            scene.occupied=Arrays.copyOf(seats,12);
            scene.occupantUids=Arrays.copyOf(uids,12);
            requestRender();
        }""")
change("        volatile boolean[] occupied=new boolean[12];",
"        volatile boolean[] occupied=new boolean[12];\n        volatile String[] occupantUids=new String[12];")
change("            boolean[] live=occupied; // immutable copy, set from Firestore UI thread",
"            boolean[] live=occupied; // immutable copy, set from Firestore UI thread\n            String[] people=occupantUids;")
change("""                float hue=i%5;
                float bob=(float)Math.sin(t*1.5+i*.9f)*.025f;
                float cr=.32f+.1f*hue,cg=.55f-.055f*hue,cb=.85f;""",
"""                // Stable UID-linked procedural outfit: same character palette on both phones.
                String uid=i<people.length?people[i]:null;
                int stable=uid==null?i:uid.hashCode();
                int style=(stable&0x7fffffff)%6;
                float[] reds={.82f,.30f,.81f,.40f,.93f,.29f};
                float[] greens={.36f,.76f,.53f,.51f,.72f,.60f};
                float[] blues={.72f,.88f,.35f,.91f,.37f,.75f};
                float bob=(float)Math.sin(t*1.5+i*.9f)*.035f;
                float cr=reds[style],cg=greens[style],cb=blues[style];""")
change("box(x-.33f,.66f+bob,z,.16f,.58f,.22f,cr,cg,cb);",
"box(x-.33f,.66f+bob+(float)Math.sin(t*2+i)*.12f,z,.16f,.58f,.22f,cr,cg,cb);")
change("box(x+.33f,.66f+bob,z,.16f,.58f,.22f,cr,cg,cb);",
"box(x+.33f,.66f+bob-(float)Math.sin(t*2+i)*.12f,z,.16f,.58f,.22f,cr,cg,cb);")
path.write_text(s)
gradle=root/"app/build.gradle";g=gradle.read_text()
g,n=re.subn(r"versionCode\s+192;\s*versionName\s+'9\.9\.13-party-seat-approval-test'",
             "versionCode 193; versionName '9.9.14-3d-live-profile-test'",g)
assert n==1,"expected verified v9.9.13 source"
gradle.write_text(g)
assert "approveOrMoveMemberSeat9913" in (root/"app/src/main/java/com/kingplus/social/PartyActivity.java").read_text()
print("PASS: Firebase seat identities, tappable public profiles, original UID-stable procedural 3D avatars")
