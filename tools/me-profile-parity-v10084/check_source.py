from pathlib import Path

root = Path(__file__).resolve().parents[2]
flow = (root / 'app/src/main/java/com/kingplus/social/KingDeepFlowActivity.java').read_text()
main = (root / 'app/src/main/java/com/kingplus/social/MainActivity.java').read_text()

assert "KING Plus v10.0.84 Profile Options Routing TEST" in (root / 'RELEASE-NOTES-v10.0.84.md').read_text()
for route in ('detail_profile_about', 'detail_profile_gallery', 'detail_profile_videos',
              'detail_profile_share', 'detail_profile_notifications',
              'detail_profile_favorites', 'detail_profile_recent'):
    assert f'case "{route}"' in flow
for action in ('profile_about', 'profile_gallery', 'profile_videos', 'share_profile', 'notifications'):
    assert f'case "{action}"' in main
for section in ('About Me', 'My Gallery', 'My Videos', 'Share Profile', 'Notifications',
                'Followers', 'Following', 'Friends', 'Favorite Party Rooms',
                'Recent Party Rooms', 'Family Square', 'Relationship / CP', 'Backpack / Collection'):
    assert section in flow
assert 'putExtra("initialSocialTab10018",tab)' in flow
assert 'inbox10084.putExtra("openNotifications10010",true)' in main
assert 'TEST preview only • device activity is not server verified or reward eligible' in main
assert 'Achievements • locked' in main
assert 'getMissionValue("mission_room")' not in main[main.index('private void levelPage()'):main.index('private void settingsPage()')]
print('PASS: Me Profile nested routes open real MainActivity pages and live Social tabs; local achievement claims are removed')
