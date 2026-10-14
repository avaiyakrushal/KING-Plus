from pathlib import Path
import sys
root=Path(sys.argv[1])
home=root/'app/src/main/java/com/kingplus/social/CommunityHubActivity.java'
s=home.read_text()
if 'private void saveCreatedFamily10009(' in s:
 print('PASS v10.0.9 new Family link handler already present')
 sys.exit(0)
needle='    private void joinFamily(String code){'
assert s.count(needle)==1
helper='''    private void saveCreatedFamily10009(String ownerUid,String code,String name,
                                        int generation,LinearLayout host){
        Map<String,Object> link=new HashMap<>();link.put("familyCode",code);
        link.put("familyName",name);
        db.collection("users").document(ownerUid).set(link,SetOptions.merge())
            .addOnSuccessListener(v->{
                if(KingSavedRoomsCloud9990.account(ownerUid)){
                    getSharedPreferences("MainActivity",MODE_PRIVATE).edit()
                        .putString("family_code",code).putString("family_name",name).apply();
                    if(currentSection10003("Family",generation,host,ownerUid))render("Family");
                }
            }).addOnFailureListener(error->{
                if(currentSection10003("Family",generation,host,ownerUid))
                    familyError955("Family created, but profile link failed",error,
                        ()->saveCreatedFamily10009(ownerUid,code,name,generation,host));
            });
    }
'''
home.write_text(s.replace(needle,helper+needle))
print('PASS added v10.0.9 Family owner profile linking handler')
