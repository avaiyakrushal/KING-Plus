#!/usr/bin/env python3
"""No FINAL acceptance is possible based on CI compile alone."""
import json,subprocess,sys,tempfile
from pathlib import Path
script=Path("tools/integrated-release/check_release.py")
data=json.loads(Path("release/final-release-evidence.json").read_text())
assert len(data["gates"])==16
def run(mode,path):
    return subprocess.run([sys.executable,str(script),"--mode",mode,"--file",str(path)],
                          capture_output=True,text=True)
with tempfile.TemporaryDirectory() as d:
    file=Path(d)/"gates.json"
    file.write_text(json.dumps(data))
    assert run("test",file).returncode==0,run("test",file).stderr
    assert run("final",file).returncode!=0,"Unverified Final must fail closed"
    data["release_kind"]="FINAL_CANDIDATE"
    for gate in data["gates"]:
        gate["status"]="passed"
        gate["evidence"]=["verified-report:real-test-"+gate["id"]]
    file.write_text(json.dumps(data))
    assert run("final",file).returncode==0,run("final",file).stderr
    data["gates"][0]["evidence"]=[]
    file.write_text(json.dumps(data))
    assert run("final",file).returncode!=0,"Missing verification must block final"
    data["gates"].pop()
    file.write_text(json.dumps(data))
    assert run("test",file).returncode!=0,"All sixteen areas mandatory"
print("PASS integrated 16 feature gates: only genuine recorded evidence permits FINAL")
