#!/usr/bin/env python3
"""Static, conservative audit of actual signed-dev KING Party toolbox routes.
Treat the order of Java String.contains tests as Java does. Dynamic menus are
partially represented and may need a human audit; this intentionally does NOT
claim functional 2-device acceptance.
"""
from pathlib import Path
import re,sys
root=Path(sys.argv[1])
f=root/"app/src/main/java/com/kingplus/social/PartyActivity.java"
s=f.read_text()
start=s.index("    private void handleRoomMenuItem810(String x){")
end=s.index("\n    private void openDeepFlow810(",start)
router=s[start:end]
routes=[]
for line_no,line in enumerate(router.splitlines(),1):
    if 'x.contains(' not in line or 'if(' not in line:continue
    strings=re.findall(r'x\.contains\("([^"]+)"\)',line)
    if strings:routes.append((strings,line_no))
labels=re.findall(r'items\.add\("([^"]+)"\)',s[s.index("    private void roomMenuAll()"):start])
q=s[s.index("    private void roomMenu()"):s.index("    private void roomMenuAll()")]
labels+=re.findall(r'quick\.add\("([^"]+)"\)',q)
labels+=["🎤 Mic ON","🎤 Mic OFF","🔊 Gift Audio ON","🔇 Gift Audio OFF",
         "★ Unfavorite room","☆ Favorite room","🔒 Lock seats","🔓 Unlock seats",
         "🔇 Mute all seats","🎤 Unmute all seats",
         "🔓 Make room public","🔐 Make room private",
         "🔑 Set room password","🔑 Change room password"]
errors=[]
print("ROUTE_BRANCH_COUNT",len(routes))
for label in list(dict.fromkeys(labels)):
    matchs=[(n,part,line) for n,(parts,line) in enumerate(routes) for part in parts if part in label]
    if not matchs:
        if "All Tools" in label: print("MATCH",label,"-> handled locally by roomMenuAll()")
        else: errors.append(f"NO_HANDLER: {label}")
    else:print("MATCH",label,matchs[0][1],"route #",matchs[0][0])
print("STATIC_TOOL_LABEL_COUNT",len(set(labels)))
for e in errors:print("ERROR",e)
# A more precise audit for known substring shadows in current code:
if router.find('x.contains("Party Master Help")') > router.find('x.contains("Party Master")'):
    errors.append("SHADOWED Party Master Help")
if router.find('x.contains("PK Challenge")') > router.find('x.contains("PK battle")'):
    print("INFO PK Challenge branch is after PK battle; different case/words")
if errors:raise SystemExit("ROUTE_AUDIT_FAILED "+str(len(errors)))
print("PASS All statically encoded toolbox labels map to a handler")
