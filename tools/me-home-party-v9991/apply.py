from pathlib import Path
import subprocess,sys
root=Path(sys.argv[1]).resolve();assets=Path(__file__).resolve().parent
base=root/'app/build.gradle';assert 'versionCode 269;' in base.read_text(),'Exact v9.9.90 source required'
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(assets/'changes.patch')],cwd=root,check=True)
assert 'versionCode 270;' in base.read_text()
