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
    assert f'openMeAction9981("{action}")' in flow
for page in ('private void profileAboutPage()', 'private void galleryPage()',
             'private void videoPage()', 'private void shareMyIssuedKingId9998()',
             'private void notificationsCenter()'):
    assert page in main, f'missing MainActivity profile destination: {page}'
assert 'getIntent().getStringExtra("meAction9981")' in main
assert 'routeMeAction9981(action9981)' in main
for section in ('About Me', 'My Gallery', 'My Videos', 'Share Profile', 'Notifications',
                'Followers', 'Following', 'Friends', 'Favorite Party Rooms',
                'Recent Party Rooms', 'Family Square', 'Relationship / CP', 'Backpack / Collection'):
    assert section in flow
assert 'putExtra("initialSocialTab10018",tab)' in flow
assert 'inbox10010.putExtra("openNotifications10010",true)' in main
assert 'TEST preview only • device activity is not server verified or reward eligible' in main
assert 'Achievements • locked' in main
assert 'getMissionValue("mission_room")' not in main[main.index('private void levelPage()'):main.index('private void settingsPage()')]
print('PASS: Me Profile detail routes relay to MainActivity destinations and live Social tabs; local achievement claims are removed')
