#!/usr/bin/env python3
"""KING Plus v9.7.8 – no spurious 'Killed by signal' login popup.

The v9.7.5 screenshot showed REASON_SIGNALED, no matching previous process,
zero RSS/PSS, and a launcher that remained alive long enough to display the
modal. Android's REASON_SIGNALED is NOT by itself proof of a native crash:
getStatus() carries the actual signal. Treat SIGKILL/SIGTERM as diagnostic
process termination rather than unsolicited crash popup. Keep genuine SIGABRT,
SIGSEGV, SIGBUS, SIGILL, SIGFPE, ANR, LMK, and native-crash diagnostics, with
raw signal codes so a later user screenshot can identify fault types.
"""
from pathlib import Path
import shutil,sys

root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
crash=pkg/'KingCrashWatch958.java'
shutil.copy2(Path(__file__).with_name('KingExitPolicy978.java'),
             pkg/'KingExitPolicy978.java')
s=crash.read_text()

def one(old,new,label):
    global s
    n=s.count(old)
    if n!=1:raise SystemExit(f'{label}: expected one source marker; got {n}: {old[:150]!r}')
    s=s.replace(old,new,1)
    print('PASS',label)

one(
'''                if (!isAbnormal(item.getReason())) continue;
                if (item.getTimestamp() <= alreadySeen || item.getTimestamp() > now + 60000L) continue;''',
'''                if (!isAbnormal(item.getReason())) continue;
                if (item.getTimestamp() <= alreadySeen || item.getTimestamp() > now + 60000L) continue;
                if (item.getReason() == ApplicationExitInfo.REASON_SIGNALED) {
                    final boolean previousAppPid978=previousPid>0&&previousPid==item.getPid()
                        && previousStageAt>0&&previousStageAt<=item.getTimestamp()
                        && item.getTimestamp()-previousStageAt<=15L*60*1000;
                    // SIGKILL on app install/update, OS process cleanup, and many
                    // memory kills is not enough evidence to claim an app crash.
                    if (!KingExitPolicy978.showSignalAlert(
                            item.getStatus(),previousAppPid978)) {
                        p.edit()
                            .putLong("quietExitAt978",item.getTimestamp())
                            .putInt("quietExitStatus978",item.getStatus())
                            .putInt("quietExitReason978",item.getReason()).apply();
                        continue;
                    }
                }''',
'Do not call ambiguous SIGKILL/SIGTERM a new crash')

one(
'''            String report = "Android exit: " + label(relevant.getReason())
                + "\\nInstalled KING Plus version: " + installedVersion''',
'''            String report = "Android exit: " + label(relevant.getReason())
                + (relevant.getReason() == ApplicationExitInfo.REASON_SIGNALED
                    ? "\\nActual signal: "+KingExitPolicy978.signalLabel(relevant.getStatus()) : "")
                + "\\nOS status code: " + relevant.getStatus()
                + "\\nInstalled KING Plus version: " + installedVersion''',
'Include raw Android signal and process exit status for actionable future reports')

one(
'''            new AlertDialog.Builder(a)
                .setTitle("KING Plus – new crash report")''',
'''            new AlertDialog.Builder(a)
                .setTitle("KING Plus – abnormal Android process exit")''',
'Do not overstate unexplained signal as proven Java/native crash')

crash.write_text(s)
gradle=root/'app/build.gradle'
g=gradle.read_text()
old="versionCode 168; versionName '9.7.7-party-join-timeout-retry'"
if g.count(old)!=1:raise SystemExit('Expected v9.7.7 successful APK source baseline')
gradle.write_text(g.replace(old,
    "versionCode 169; versionName '9.7.8-startup-signal-attribution'",1))
print('PASS v9.7.8 signal diagnostics applied, all prior apps and logic preserved')
