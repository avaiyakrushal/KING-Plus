#!/usr/bin/env python3
"""Integrity gate for the ONE integrated KING Plus production release.

A successful Android assembleDebug or Firestore emulator is a TEST success,
never proof of real voice, paid recharge, world, or production readiness.
"""
import argparse,json,re,sys
from pathlib import Path

def audit(path, mode):
    data=json.loads(Path(path).read_text(encoding="utf-8"))
    gates=data.get("gates",[])
    required={f"{n:02d}" for n in range(1,17)}
    found=[g.get("id") for g in gates]
    if len(found)!=16 or set(found)!=required or len(set(found))!=16:
        raise ValueError("Exactly 16 distinct release gates 01–16 must be tracked")
    failures=[]
    for g in gates:
        if not isinstance(g.get("acceptance"),str) or not g["acceptance"].strip():
            failures.append(g["id"]+" acceptance missing")
        if g.get("status") not in ("unverified","blocked","in_progress","passed"):
            failures.append(g["id"]+" unknown status")
        e=g.get("evidence")
        if not isinstance(e,list):
            failures.append(g["id"]+" evidence not a list")
        elif g.get("status")=="passed":
            # Specific externally reviewable proof, not a bare claim.
            if not e or any(not isinstance(x,str) or not (
                x.startswith("https://") or x.startswith("github-run:") or
                x.startswith("verified-report:")) for x in e):
                failures.append(g["id"]+" passed without reviewable evidence")
        elif mode=="final":
            failures.append(g["id"]+" "+g["key"]+" is "+g["status"])
    if mode=="final" and data.get("release_kind")!="FINAL_CANDIDATE":
        failures.append("release_kind is not FINAL_CANDIDATE")
    if failures:
        print("KING Integrated 16-Gate "+mode.upper()+" NO-GO",file=sys.stderr)
        for issue in failures: print("  - "+issue,file=sys.stderr)
        return 1
    print("KING Integrated 16-Gate "+mode.upper()+" PASS")
    if mode=="test":
        outstanding=[x for x in gates if x["status"]!="passed"]
        print(f"Development/TEST accepted for tracking; {len(outstanding)}/16 FINAL gates remain unverified.")
    return 0

def main():
    p=argparse.ArgumentParser()
    p.add_argument("--mode",choices=("test","final"),required=True)
    p.add_argument("--file",default="release/final-release-evidence.json")
    a=p.parse_args()
    try: return audit(a.file,a.mode)
    except (ValueError,KeyError,OSError,json.JSONDecodeError) as ex:
        print("KING release matrix invalid:",ex,file=sys.stderr)
        return 1

if __name__=="__main__":sys.exit(main())
