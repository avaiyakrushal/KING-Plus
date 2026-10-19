from pathlib import Path
root=Path(__file__).resolve().parents[2]
s=(root/"app/src/main/java/com/kingplus/social/DiscoverActivity.java").read_text()
g=(root/"app/build.gradle").read_text()
assert "versionCode 356" in g
assert "10.0.77-party-rejoin-choice" in g
assert s.count('orderBy("level",Query.Direction.DESCENDING).limit(60)')==2
first=s.index('else if("Hot".equals(activePeopleTab))')
next_page=s.index('else if("Hot".equals(tab10025))')
assert first<next_page
assert 'page10025=page10025.startAfter(last10025);' in s[next_page:]
assert 'publicCursors10028.put(grid10025,nextDocs10025.get(nextDocs10025.size()-1));' in s
print("PASS Discover Hot ranking uses a level-ordered server cursor on every page")
