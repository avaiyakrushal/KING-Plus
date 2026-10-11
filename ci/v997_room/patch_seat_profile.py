#!/usr/bin/env python3
from pathlib import Path
import sys
f=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social/PartyActivity.java'
s=f.read_text()
def patch(old,new,label):
 global s
 if s.count(old)!=1:raise RuntimeError(label+' anchor '+str(s.count(old)))
 s=s.replace(old,new,1)
 print('PASS',label)
patch('if (targetUidV530 != null && !user.getUid().equals(targetUidV530)) { showOccupiedSeatMenuV530(no); return; }',
      'if (targetUidV530 != null && !user.getUid().equals(targetUidV530)) { openSeatProfile(no); return; }',
      'tap occupied seat as moderator opens user profile')
patch('seat.setOnClickListener(v->seatAction(seatNo)); row.addView(seat,new LinearLayout.LayoutParams(0,dp(80),1));',
      '''seat.setOnClickListener(v->seatAction(seatNo));
                seat.setOnLongClickListener(v->{
                    if(canModerateSeatsV530()&&seatUids.get(seatNo)!=null){
                        showOccupiedSeatMenuV530(seatNo);
                        return true;
                    }
                    return false;
                });
                row.addView(seat,new LinearLayout.LayoutParams(0,dp(80),1));''',
      'long press occupied seat opens moderation actions')
patch('page.putExtra("name",name==null||name.trim().isEmpty()?"KING User":name.trim());\n            startActivity(page);',
      'page.putExtra("name",name==null||name.trim().isEmpty()?"KING User":name.trim());\n            page.putExtra("roomId",roomId);\n            page.putExtra("roomRole",host?"HOST":"MEMBER");\n            startActivity(page);',
      'pass active room for real charisma and Noble status')
patch('if(isModerator()&&user!=null&&!uid.equals(user.getUid()))showProfileMore(uid,labels.get(w));else memberProfileDialog(uid,labels.get(w));',
      'memberProfileDialog(uid,labels.get(w));',
      'Member list opens public profile even for room admins')
f.write_text(s)
