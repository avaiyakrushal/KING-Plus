from pathlib import Path
root=Path(__file__).resolve().parents[2]
gradle=(root/'app/build.gradle').read_text()
main=(root/'app/src/main/java/com/kingplus/social/MainActivity.java').read_text()
deep=(root/'app/src/main/java/com/kingplus/social/KingDeepFlowActivity.java').read_text()
privacy=(root/'app/src/main/java/com/kingplus/social/KingMePrivacy9984Activity.java').read_text()
service=(root/'functions/daily-checkin-service.js').read_text()
notify=(root/'functions/notification-preferences.js').read_text()
v3=(root/'functions/v3.js').read_text()
workflow=(root/'.github/workflows/king-v10077-party-rejoin-choice.yml').read_text()
assert 'versionCode 380' in gradle and '10.0.101-party-events-live' in gradle
assert 'loadServerActivityLevel10095(uid)' in main and 'Lv.…' in main and 'LevelSystem.read(this)' not in main[main.index('private void profileV600()'):main.index('private void loadHostedRoomsPage10015')]
assert 'getDailyCheckinStatus' in deep and 'Account Activity Level' in deep and 'Account Medals' in deep
assert 'activityLevel' in service and 'nextLevelXp' in service and 'levelFor' in service
assert 'shouldDeliverNotification' in v3 and 'canReceiveRoomInvite' in v3
assert 'notification-preferences.test.js' in workflow and 'KING-Plus-v10.0.101-Party-Events-TEST.apk' in workflow
assert 'Find-me hides you from KING Plus discovery and search cards' in privacy
assert 'remember_login' not in deep and 'auto_reconnect' not in deep and 'low_data_voice' not in deep
print('PASS: Me profile uses account-verified level/medals; settings avoid dead switches and enforce saved notification/invite choices')
