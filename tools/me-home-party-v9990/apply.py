from pathlib import Path
import subprocess,shutil,sys
root=Path(sys.argv[1]).resolve();assets=Path(__file__).resolve().parent
p=root/'app/build.gradle';assert 'versionCode 268;' in p.read_text(),'v9.9.89 required'
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(assets/'changes.patch')],cwd=root,check=True)
for name in ['KingSavedRoomsPolicy9990.java','KingSavedRoomsCloud9990.java','KingSavedRooms9990Activity.java']:shutil.copy2(assets/name,root/'app/src/main/java/com/kingplus/social'/name)
p.write_text(p.read_text().replace("versionCode 268; versionName '9.9.89-me-home-party-test'","versionCode 269; versionName '9.9.90-live-saved-rooms-party-test'"))
