#!/usr/bin/env python3
"""Post-five sections, Stage 6: ordered Login design, Family and Community correctness.
Signed Firebase identity and official auth providers remain unchanged.
Preserves Family association on CloudSync and rejects stale server values.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social'

def patch(file, replacements):
 f=root/file
 s=f.read_text()
 for old,new,label in replacements:
  n=s.count(old)
  if n!=1:raise RuntimeError(f'{file}: expected exactly one {label}, got {n}')
  s=s.replace(old,new,1)
  print('PASS',file,label)
 f.write_text(s)

patch('MainActivity.java',[
 ('terms.setText("Terms & Privacy  •  Trouble logging in?");',
  'terms.setText("Terms & Privacy  •  Trouble logging in?");\n        terms.setContentDescription("Terms of service, privacy policy, and login help");\n        terms.setFocusable(true);',
  'Login terms/help accessible to screen readers and keyboard controls')
])
patch('CloudSync.java',[
 ('import com.google.firebase.firestore.FirebaseFirestore;',
  'import com.google.firebase.firestore.FirebaseFirestore;\nimport com.google.firebase.firestore.SetOptions;',
  'Firestore merge import'),
 ('db.collection("users").document(user.getUid()).set(profile)',
  'db.collection("users").document(user.getUid()).set(profile, SetOptions.merge())',
  'CRITICAL prevent profile resync wiping Family code, roles, or account preferences')
])
patch('CommunityHubActivity.java',[
 ('db.collection("users").document(me.getUid()).get().addOnSuccessListener(u->{',
  'db.collection("users").document(me.getUid()).get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(u->{',
  'Family uses latest private account document from SERVER'),
 ('db.collection("families").document(code).collection("members").document(me.getUid()).get().addOnSuccessListener(m->{',
  'db.collection("families").document(code).collection("members").document(me.getUid()).get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(m->{',
  'Family membership is verified on SERVER'),
 ('}).addOnFailureListener(e->familyJoinCreate());}).addOnFailureListener(e->familyJoinCreate());}',
  '}).addOnFailureListener(e->familyError955("Family membership unavailable",e,()->render("Family")));}).addOnFailureListener(e->familyError955("Family lookup unavailable",e,()->render("Family")));}',
  'Never offer creating new Family on access/network errors'),
 ('db.collection("public_profiles").whereEqualTo("publicId",raw).limit(20).get().addOnSuccessListener',
  'db.collection("public_profiles").whereEqualTo("publicId",raw).limit(20).get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener',
  'KING ID search reads SERVER, not stale offline directory'),
 ('z.get().addOnSuccessListener(s->renderUsers(s.getDocuments()))',
  'z.get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(s->renderUsers(s.getDocuments()))',
  'Name/Discovery search reads SERVER'),
 ('db.collection("live_rooms").orderBy("createdAt",Query.Direction.DESCENDING).limit(80).get().addOnSuccessListener',
  'db.collection("live_rooms").orderBy("createdAt",Query.Direction.DESCENDING).limit(80).get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener',
  'Room search reads SERVER, not ghost room cache'),
 ('String text=e.getText().toString().trim();if(text.isEmpty())return;Map<String,Object>m=',
  'String text=e.getText().toString().trim();if(text.isEmpty())return;if(text.length()>1500){toast("Moments must be under 1500 characters");return;}Map<String,Object>m=',
  'Bound Community post size to protect slow networks and Firestore limits'),
])
print('STAGE6 LOGIN/FAMILY/COMMUNITY PATCHED - no changes to billing, role privileges or auth providers')
