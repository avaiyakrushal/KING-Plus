#!/usr/bin/env python3
"""Audit latest staged UX: EVERY one-tap Party action must target real implementation.
Report clear, checkable route anchors (no UI completion claims)."""
from pathlib import Path
p=Path('/tmp/kingroute/app/src/main/java/com/kingplus/social')
def show(name,terms,before=3,after=72):
 f=p/name
 if not f.exists():print("MISSING",name);return
 lines=f.read_text(errors='replace').splitlines()
 print("\nFILE",name,"LINES",len(lines))
 for term in terms:
  matches=[i for i,x in enumerate(lines) if term in x]
  print("ANCHOR",term,"COUNT",len(matches),"LINES",[i+1 for i in matches[:15]])
  for k in matches[:2]:
   print("---")
   for j in range(max(0,k-before),min(len(lines),k+after)):
    print(f"{j+1:05} {lines[j][:950]}")
show('PartyActivity.java',['private void roomMenu(){','private void handleRoomMenuItem810(','private void roomMenuAll()','private void handleBack','void onBackPressed','void roomMenu()'],1,90)
show('SocialActivity.java',['void onBackPressed','void render(', 'Test Connection','Private chat'],1,16)
