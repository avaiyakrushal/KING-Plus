"""KING Plus v9.5.8 whole-app stability and Android process-exit diagnostics."""
from pathlib import Path
import sys, shutil

root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
if not pkg.is_dir(): raise SystemExit('Missing Android source directory')
source=Path(__file__).with_name('KingCrashWatch958.java')
if not source.is_file(): raise SystemExit('Missing crash watcher source')
shutil.copy2(source,pkg/'KingCrashWatch958.java')

def patch(path,old,new,label):
 p=pkg/path
 s=p.read_text()
 count=s.count(old)
 if count!=1:
  raise SystemExit(f'{label}: expected 1 marker, found {count}: {old[:140]}')
 p.write_text(s.replace(old,new,1))
 print('PATCH_OK',label)

patch('KingApplication.java',
      '        super.onCreate();',
      '        super.onCreate();\n        KingCrashWatch958.install(this);',
      'install global Activity/process watcher')
# Ensure fatal Java exception persists to disk before the system terminates this process.
patch('KingApplication.java',
      '.putString("device", Build.MANUFACTURER + " " + Build.MODEL + " / Android " + Build.VERSION.RELEASE)\n                    .apply();',
      '.putString("device", Build.MANUFACTURER + " " + Build.MODEL + " / Android " + Build.VERSION.RELEASE)\n                    .commit();',
      'persist fatal Java crash log')

patch('MainActivity.java',
      '        KingStability.install(this);\n        installCrashReport();',
      '        KingStability.install(this);\n        // Do not install an Activity-capturing global exception handler every launch.\n        // KingApplication already owns the process-level fatal handler.\n        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(\n            () -> KingCrashWatch958.showPreviousExit(this), 1700L);',
      'prevent MainActivity global-handler leak and present OS exit reason')

patch('PartyActivity.java',
      '    private void renderParty() {\n        clearListeners();',
      '    private void renderParty() {\n        KingCrashWatch958.mark(this,"party-render");\n        clearListeners();',
      'Party rendering breadcrumb')
patch('PartyActivity.java',
      '            stopInRoomVoice940(false);\n            org.jitsi.meet.sdk.JitsiMeet.instantiateReactNative(this);',
      '            stopInRoomVoice940(false);\n            KingCrashWatch958.mark(this,"party-jitsi-init");\n            org.jitsi.meet.sdk.JitsiMeet.instantiateReactNative(this);',
      'native Jitsi initialization breadcrumb')
patch('PartyActivity.java',
      '        try{org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostResume(this);}catch(Throwable e){KingStability.nonFatal(this,"party-jitsi-resume",e);}',
      '        if(inRoomVoiceJoined940)try{org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostResume(this);}catch(Throwable e){KingStability.nonFatal(this,"party-jitsi-resume",e);}',
      'skip Jitsi resume unless voice is joined')
patch('PartyActivity.java',
      '    @Override protected void onStop(){try{org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostPause(this);}catch(Throwable e){KingStability.nonFatal(this,"party-jitsi-pause",e);}super.onStop();}',
      '    @Override protected void onStop(){if(inRoomVoiceJoined940)try{org.jitsi.meet.sdk.JitsiMeetActivityDelegate.onHostPause(this);}catch(Throwable e){KingStability.nonFatal(this,"party-jitsi-pause",e);}super.onStop();}',
      'skip Jitsi pause if voice is not joined')

patch('KingMultiVideoActivity.java',
      '            org.jitsi.meet.sdk.JitsiMeet.instantiateReactNative(this);meetView=new org.jitsi.meet.sdk.JitsiMeetView(this);',
      '            KingCrashWatch958.mark(this,"video-jitsi-init");\n            org.jitsi.meet.sdk.JitsiMeet.instantiateReactNative(this);meetView=new org.jitsi.meet.sdk.JitsiMeetView(this);',
      'Multi Video native startup breadcrumb')

gradle=root/'app/build.gradle'
s=gradle.read_text()
old="versionCode 148; versionName '9.5.7-party-voice-isolation'"
if s.count(old)!=1:raise SystemExit('Unexpected v9.5.7 Gradle version')
gradle.write_text(s.replace(old,"versionCode 149; versionName '9.5.8-whole-app-stability'",1))
print('KING Plus 9.5.8 whole app stability patch applied')
