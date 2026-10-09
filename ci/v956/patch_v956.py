"""Family joining, live messages, and member search on the v9.5.5 source."""
from pathlib import Path
import sys

root = Path(sys.argv[1])
p = root / 'app/src/main/java/com/kingplus/social/CommunityHubActivity.java'
s = p.read_text()

def replace(old, new):
    global s
    if s.count(old) != 1:
        raise SystemExit('Expected exactly one source marker: ' + old[:100])
    s = s.replace(old, new, 1)

# Java lambda captures must use the normalized final value.
replace('if(name==null)name="";name=name.trim();',
        'final String normalizedName956=name==null?"":name.trim();')
replace('if(name.length()<3||name.length()>24)',
        'if(normalizedName956.length()<3||normalizedName956.length()>24)')
replace('(d,w)->createFamily(name,0)', '(d,w)->createFamily(normalizedName956,0)')
replace('final String familyName955=name;', 'final String familyName955=normalizedName956;')

# A non-member cannot read the members collection under the existing rules.
# Joining is a self-owned write, permitted by those rules. Do not update the
# owner-only aggregate family document; the UI counts actual member documents.
start = s.index('            r.collection("members").document(me.getUid()).get().addOnSuccessListener(existing->{')
end_marker = '            }).addOnFailureListener(e->{if(loading955.isShowing())loading955.dismiss();familyError955("Could not check membership",e,()->joinFamily(familyCode955));});'
end = s.index(end_marker, start) + len(end_marker)
s = s[:start] + '''            Map<String,Object> member956=new HashMap<>();
            member956.put("uid",me.getUid());member956.put("name",displayName);member956.put("role","member");
            r.collection("members").document(me.getUid()).set(member956,SetOptions.merge())
                .addOnSuccessListener(v->{if(loading955.isShowing())loading955.dismiss();saveFamily(familyCode955,familyName955);})
                .addOnFailureListener(e->{if(loading955.isShowing())loading955.dismiss();familyError955("Could not join Family",e,()->joinFamily(familyCode955));});''' + s[end:]

replace('    private void render(String tab){selected=tab;',
        '    private void render(String tab){stopFamilyMessages956();selected=tab;')
replace('    private void showFamily(String code){',
        '    private void showFamily(String code){\n        final LinearLayout familyBody956=body;')
replace('        r.get().addOnSuccessListener(f->{\n            if(!f.exists()){clearFamily();',
        '        r.get().addOnSuccessListener(f->{\n            if(body!=familyBody956||familyStopped956||isFinishing()||isDestroyed())return;\n            if(!f.exists()){clearFamily();')
replace('    private boolean cloud(){', '''    private com.google.firebase.firestore.ListenerRegistration familyMessages956;
    private boolean familyStopped956;
    private void stopFamilyMessages956(){if(familyMessages956!=null){familyMessages956.remove();familyMessages956=null;}}
    @Override protected void onStop(){familyStopped956=true;stopFamilyMessages956();super.onStop();}
    @Override protected void onStart(){super.onStart();if(familyStopped956){familyStopped956=false;if("Family".equals(selected))render("Family");}}
    @Override protected void onDestroy(){stopFamilyMessages956();super.onDestroy();}
    private boolean cloud(){''')

old_start = '            r.collection("messages").orderBy("createdAt",Query.Direction.DESCENDING).limit(30).get()'
start = s.index(old_start)
end = s.index('\n', start)
s = s[:start] + '''            stopFamilyMessages956();
            if(!familyStopped956&&!isFinishing()&&!isDestroyed()&&"Family".equals(selected))familyMessages956=r.collection("messages").orderBy("createdAt",Query.Direction.DESCENDING).limit(30).addSnapshotListener((snapshot,error)->{
                if(body!=familyBody956||familyStopped956||isFinishing()||isDestroyed()||!"Family".equals(selected))return;
                chat.removeAllViews();
                if(error!=null||snapshot==null){chat.addView(tv("Family Chat unavailable. Reopen Family to retry.",13,MUTED,false));return;}
                List<DocumentSnapshot> messages956=new ArrayList<>(snapshot.getDocuments());Collections.reverse(messages956);
                if(messages956.isEmpty())chat.addView(tv("No Family messages yet. Say hello 👋",13,MUTED,false));
                for(DocumentSnapshot message956:messages956)chat.addView(tv(safe(message956.getString("name"),"User")+": "+safe(message956.getString("text"),""),13,DARK,false));
            });''' + s[end:]
replace('if(t.isEmpty())return;Map<String,Object>m=new HashMap<>();m.put("uid",me.getUid());m.put("name",displayName);m.put("text",t);',
        'if(t.isEmpty())return;if(t.length()>1000){toast("Family messages can contain up to 1000 characters");return;}Map<String,Object>m=new HashMap<>();m.put("uid",me.getUid());m.put("name",displayName);m.put("text",t);')
replace('r.collection("messages").add(m).addOnSuccessListener(v->{msg.setText("");render("Family");})',
        'r.collection("messages").add(m).addOnSuccessListener(v->{if(msg.getText().toString().trim().equals(t))msg.setText("");})')

marker = 'body.addView(memberList951,new LinearLayout.LayoutParams(-1,-2));'
replace(marker, marker + '''
            EditText memberSearch956=new EditText(this);memberSearch956.setHint("Search Family members");memberSearch956.setSingleLine(true);body.addView(memberSearch956,body.indexOfChild(memberList951),new LinearLayout.LayoutParams(-1,dp(48)));
            memberSearch956.addTextChangedListener(new android.text.TextWatcher(){
                public void beforeTextChanged(CharSequence s,int start,int count,int after){}
                public void onTextChanged(CharSequence s,int start,int before,int count){filterFamilyMembers956(memberList951,s.toString());}
                public void afterTextChanged(android.text.Editable s){}
            });''')
replace('if(s.isEmpty())memberList951.addView(', 'memberSearch956.setEnabled(true);if(s.isEmpty())memberList951.addView(')
replace('body.addView(memberSearch956,body.indexOfChild(memberList951),new LinearLayout.LayoutParams(-1,dp(48)));',
        'memberSearch956.setEnabled(false);body.addView(memberSearch956,body.indexOfChild(memberList951),new LinearLayout.LayoutParams(-1,dp(48)));')
replace('    private TextView familyStat951(', '''    private void filterFamilyMembers956(LinearLayout list,String query){
        String q=query.trim().toLowerCase(Locale.ROOT);
        for(int i=0;i<list.getChildCount();i++){View child=list.getChildAt(i);if(child instanceof TextView)child.setVisibility(((TextView)child).getText().toString().toLowerCase(Locale.ROOT).contains(q)?View.VISIBLE:View.GONE);}
    }
    private TextView familyStat951(''')

# Earlier generated UI text escaped line breaks twice.
s=s.replace('\\\\n','\\n')
p.write_text(s)
g=root/'app/build.gradle'
t=g.read_text()
old="versionCode 146; versionName '9.5.5-family-settings-polish'"
if t.count(old)!=1: raise SystemExit('Unexpected base version')
g.write_text(t.replace(old,"versionCode 147; versionName '9.5.6-family-live-fix'"))
print('v9.5.6: Family join permissions, live chat, member search and text fixes applied')
