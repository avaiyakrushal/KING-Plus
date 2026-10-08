from pathlib import Path
import re

MAIN = Path('app/src/main/java/com/kingplus/social/MainActivity.java')
RULES = Path('firestore.rules')
BUILD = Path('app/build.gradle')

src = MAIN.read_text(encoding='utf-8')
replacements = [
    ('if(w==0)searchCommunity();else if(w==1)createRoomDialog();else notificationsCenter();',
     'if(w==0)startActivity(new Intent(this,SocialActivity.class));else if(w==1)createRoomDialog();else notificationsCenter();'),
    ('else if(k==3)messages();else openProfileSafely();',
     'else if(k==3)startActivity(new Intent(this,InboxActivity.class));else openProfileSafely();'),
    ('cardLine(list,"👥  Find Friends","Discover new people in the community",()->peoplePage("Discover People"));',
     'cardLine(list,"👥  Find Friends","Search, follow, make friends and message",()->startActivity(new Intent(this,SocialActivity.class)));'),
    ('else if(w==2)peoplePage("Friends");else if(w==3)peoplePage("Followers");',
     'else if(w==2)startActivity(new Intent(this,SocialActivity.class));else if(w==3)startActivity(new Intent(this,SocialActivity.class));'),
]
for old,new in replacements:
    if old not in src:
        raise SystemExit('v3.5.0 MainActivity template changed: '+old[:48])
    src = src.replace(old,new,1)
MAIN.write_text(src,encoding='utf-8')

rules = RULES.read_text(encoding='utf-8')
marker = '    match /live_rooms/{roomId} {'
public_profiles = '''    match /public_profiles/{uid} {
      // Public discovery contains only explicitly safe profile fields; private /users data stays private.
      allow read: if signedIn();
      allow create, update: if owner(uid)
        && request.resource.data.uid == uid
        && request.resource.data.keys().hasOnly(['uid', 'displayName', 'searchName', 'bio', 'tags', 'updatedAt'])
        && request.resource.data.displayName is string
        && request.resource.data.displayName.size() > 0
        && request.resource.data.displayName.size() <= 80
        && request.resource.data.searchName is string
        && request.resource.data.searchName.size() > 0
        && request.resource.data.searchName.size() <= 80
        && request.resource.data.bio is string
        && request.resource.data.bio.size() <= 240
        && request.resource.data.tags is string
        && request.resource.data.tags.size() <= 160;
      allow delete: if owner(uid);
    }

'''
if 'match /public_profiles/{uid}' not in rules:
    if marker not in rules: raise SystemExit('v3.5.0 rules live_rooms marker missing')
    rules = rules.replace(marker, public_profiles + marker, 1)

old = '''      allow create: if activeUser()
        && request.resource.data.members is list
        && request.resource.data.members.size() >= 2
        && request.resource.data.members.size() <= 8
        && request.auth.uid in request.resource.data.members;
      allow update: if activeUser() && request.auth.uid in resource.data.members;'''
new = '''      allow create: if activeUser()
        && request.resource.data.members is list
        && request.resource.data.members.size() == 2
        && request.resource.data.members[0] != request.resource.data.members[1]
        && request.auth.uid in request.resource.data.members;
      allow update: if activeUser()
        && request.auth.uid in resource.data.members
        && request.resource.data.members == resource.data.members
        && request.resource.data.diff(resource.data).affectedKeys()
             .hasOnly(['memberNames', 'lastMessage', 'lastSenderUid', 'updatedAt', 'readAt']);'''
if old not in rules: raise SystemExit('v3.5.0 direct thread rule template changed')
rules = rules.replace(old,new,1)

old = '''          && request.resource.data.senderUid == request.auth.uid
          && request.resource.data.recipientUid is string
          && request.resource.data.text is string'''
new = '''          && request.resource.data.senderUid == request.auth.uid
          && request.resource.data.recipientUid is string
          && request.resource.data.recipientUid != request.auth.uid
          && request.resource.data.recipientUid in get(/databases/$(database)/documents/direct_threads/$(threadId)).data.members
          && request.resource.data.text is string'''
if old not in rules: raise SystemExit('v3.5.0 direct message rule template changed')
rules = rules.replace(old,new,1)

old = '''    match /follows/{followId} {
      allow read: if signedIn();'''
new = '''    match /follows/{followId} {
      allow read: if signedIn()
        && (resource.data.followerUid == request.auth.uid
            || resource.data.targetUid == request.auth.uid);'''
if old not in rules: raise SystemExit('v3.5.0 follow rule template changed')
rules = rules.replace(old,new,1)
RULES.write_text(rules,encoding='utf-8')

gradle = BUILD.read_text(encoding='utf-8')
gradle = re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 50; versionName '3.5.0'", gradle)
BUILD.write_text(gradle,encoding='utf-8')
print('Prepared KING Plus v3.5.0 social discovery, friends and realtime messaging')
