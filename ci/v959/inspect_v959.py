from pathlib import Path
import sys
root=Path(sys.argv[1]); pkg=root/'app/src/main/java/com/kingplus/social'
def method(t, name):
 import re
 matches=list(re.finditer(r'\b'+re.escape(name)+r'\s*\(',t))
 defs=[]
 for m in matches:
  line=t.rfind('\n',0,m.start())+1
  prefix=t[line:m.start()].strip()
  if not re.search(r'(?:private|public|protected|void|boolean|String|int)\s*$',prefix):continue
  if '=' in prefix or '->' in prefix:continue
  b=t.find('{',m.end())
  semi=t.find(';',m.end())
  if b<0 or (semi>=0 and semi<b):continue
  depth=0; state='code'; i=b; esc=False
  while i<len(t):
   ch=t[i]; nxt=t[i+1] if i+1<len(t) else ''
   if state=='str':
    if esc:esc=False
    elif ch=='\\':esc=True
    elif ch=='"':state='code'
   elif state=='comment':
    if ch=='\n':state='code'
   elif state=='block':
    if ch=='*' and nxt=='/':state='code';i+=1
   else:
    if ch=='"':state='str'
    elif ch=='/' and nxt=='/':state='comment';i+=1
    elif ch=='/' and nxt=='*':state='block';i+=1
    elif ch=='{':depth+=1
    elif ch=='}':
     depth-=1
     if depth==0:break
   i+=1
  defs.append((line,t[line:i+1]))
 return defs
for file,names in {
 'PartyActivity.java':['renderLobby','shareRoom','inviteDialog','createCloudRoom','openCloudRoom','openRoomDoc891','renderParty','roomMenu','finishMemberJoin','addRoomCard','joinRoomDialog','showLobbySearch','enterRoom','openRoom'],
 'MainActivity.java':['onCreate','onNewIntent','openParty','openPartyActivity','partyRoomsPage'],
}.items():
 t=(pkg/file).read_text(errors='replace')
 print('==================',file,'==================')
 for name in names:
  ms=method(t,name);print('METHOD',name,'COUNT',len(ms))
  for at,v in ms[:1]:print('  LINE',t[:at].count('\n')+1,'SOURCE:',v[:12000])
print('=== MANIFEST ===')
print((root/'app/src/main/AndroidManifest.xml').read_text()[:8000])
