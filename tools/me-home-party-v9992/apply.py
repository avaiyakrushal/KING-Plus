from pathlib import Path
import subprocess, sys
root=Path(sys.argv[1]).resolve()
p=root/'app/build.gradle'
assert "versionCode 270;" in p.read_text(), "Exact v9.9.91 tested base source is required"
patch=Path(__file__).resolve().with_name('changes.patch')
assert patch.is_file() and patch.read_text().endswith("\n"),"Complete patch is required"
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(patch)],cwd=root,check=True)
assert "versionCode 271;" in p.read_text()
print('PASS v9.9.92 applied Me Gallery, Saved Rooms, and Party invite patch')
