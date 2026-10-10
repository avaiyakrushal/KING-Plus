from pathlib import Path
import sys,re
root=Path(sys.argv[1]);p=root/'app/src/main/java/com/kingplus/social'
for name in ['KingCrashWatch958.java','MainActivity.java','KingPlusApplication.java','KingApp.java']:
    f=p/name
    if not f.exists():print('MISSING',name);continue
    s=f.read_text(errors='replace')
    print('===',name,'lines',s.count('\n')+1,'len',len(s))
    if name=='KingCrashWatch958.java':
        print(s[:28000])
    else:
        lines=s.splitlines()
        for i,l in enumerate(lines):
            if any(z in l for z in ['showPreviousExit','KingCrashWatch958.install','KingCrashWatch958.mark','getHistoricalProcessExitReasons','setDefaultUncaughtExceptionHandler']):
                print('HIT',i+1)
                for j in range(max(0,i-3),min(len(lines),i+8)):
                    print(str(j+1)+': '+lines[j][:900])
print('=== Gradle ===')
g=root/'app/build.gradle'
print('\n'.join(l for l in g.read_text().splitlines() if 'versionCode' in l))
