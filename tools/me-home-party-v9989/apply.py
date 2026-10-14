from pathlib import Path
import subprocess,sys,shutil
root=Path(sys.argv[1]).resolve(); assets=Path(__file__).resolve().parent
build=root/'app/build.gradle'
assert "versionCode 267;" in build.read_text(),'Requires v9.9.88 source'
subprocess.run(['patch','--batch','--fuzz=0','-p1','-i',str(assets/'changes.patch')],cwd=root,check=True)
shutil.copy2(assets/'KingAccountRoomHistory9989.java',root/'app/src/main/java/com/kingplus/social/KingAccountRoomHistory9989.java')
s=build.read_text().replace("versionCode 267; versionName '9.9.88-me-verified-vip-noble-test'","versionCode 268; versionName '9.9.89-me-home-party-test'")
build.write_text(s)
test=root/'tools/me-page-v9981/KingMeRoomHistory9981Test.java'
test.write_text(test.read_text().replace('recent.size()==2','recent.size()==3'))
