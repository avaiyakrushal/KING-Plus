#!/usr/bin/env python3
"""Read-only inspection of verified v9.8.6 Party join latency and duplicate join event paths."""
from pathlib import Path
import sys,re
pkg=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social'
party=(pkg/'PartyActivity.java').read_text()
print("PartyActivity lines:",len(party.splitlines()))
needle=[
    'showJoiningRoom964()', 'joinMemberThenOpen891()', 'checkCrowdCapacity930(',
    'finishMemberJoin921(', 'openCloudRoom(', 'openRoomDoc891(',
    'minimalMember921()', 'startHeartbeat900()', 'ensureRoomMembership900(',
    'renderParty()', 'showPartyJoinFailure977(', 'reconnectPartyFirestore979(',
    'setOnlineState900(', 'startRoom(', 'createRoom(', 'createCloudRoom(',
]
for word in needle:
    print("METHOD",word,"count",party.count(word))
print("SERVER queries:",len(re.findall(r'\.get\(com\.google\.firebase\.firestore\.Source\.SERVER\)',party)))
for marker in ['private void joinMemberThenOpen891()', 'private void checkCrowdCapacity930(',
               'private void finishMemberJoin921(', 'private void openCloudRoom(',
               'private void showJoiningRoom964()', 'private void startHeartbeat900()',
               'private void ensureRoomMembership900()', 'private void renderParty()',
               'private void reconnectPartyFirestore979(']:
    start=party.find(marker)
    if start<0:
        print("NOT FOUND",marker)
        continue
    brace=party.find('{',start)
    depth=0
    end=None
    for i in range(brace,len(party)):
        if party[i]=='{': depth+=1
        elif party[i]=='}':
            depth-=1
            if depth==0:
                end=i+1
                break
    lines=party[start:end].splitlines() if end else []
    print("\n##",marker,"total_lines",len(lines))
    if marker in ['private void joinMemberThenOpen891()', 'private void checkCrowdCapacity930(',
                  'private void finishMemberJoin921(',
                  'private void reconnectPartyFirestore979(']:
        for n,line in enumerate(lines[:165],1):
            print(f"{n:3} {line[:235]}")
    else:
        keywords=['.set(','.update(','.get(', 'addOnSuccessListener','addOnFailureListener',
                  'server','member','join', 'roomId', 'start', 'renderParty',
                  'Thread', 'post', 'timeout', 'guard', 'activeRoom']
        for n,line in enumerate(lines[:260],1):
            if any(k.lower() in line.lower() for k in keywords):
                print(f"{n:3} {line[:235]}")
print("\n## JOIN messages / events: relevant source lines")
src=party.splitlines()
for i,line in enumerate(src):
    if any(x in line.lower() for x in ['joined the room', 'joined party', 'joined room', 'room joined']):
        print(f"{i+1}: "+line[:235])
        print("\n".join(f"{j+1}: "+src[j][:235] for j in range(max(0,i-4),min(len(src),i+5))))
print("INSPECTION COMPLETE")

print("\n## CREATE/OPEN JOIN CALLSITE CONTEXT")
src=party.splitlines()
for i,line in enumerate(src):
    if any(x in line for x in ['private void createCloudRoom(', 'private void openCloudRoom(', 
                               'void openCloudRoom(', 'private void finishMemberJoin921()',
                               'private void openRoomDoc891(', 'private void joinMemberThenOpen891()',
                               'private void openParty(', 'private void renderLobby(',
                               'private void roomJoinError891(']):
        print("\nAT LINE",i+1,":",line.strip()[:220])
        for n in range(i,min(i+48,len(src))):
            print(f"{n+1}: "+src[n][:240])
print("\n## Room member write count and guarded join entrypoints")
for i,line in enumerate(src):
    if ('memberSeen=true' in line or '.set(member' in line or 
        'joinMemberThenOpen891();' in line or 'createCloudRoom(' in line or
        'openCloudRoom(' in line or 'finishMemberJoin921();' in line):
        print(f"{i+1}: "+line[:300])
