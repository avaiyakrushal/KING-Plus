#!/usr/bin/env python3
from pathlib import Path
import re, sys
root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'

p=pkg/'KingSafe.java'
s=p.read_text()
mark='public final class KingSafe {'
if 'public static void open(' not in s:
    s=s.replace(mark,mark+'''
    public static void open(android.app.Activity a, android.content.Intent i, String label){
        if(a==null||a.isFinishing()||(android.os.Build.VERSION.SDK_INT>=17&&a.isDestroyed())||i==null)return;
        try{a.startActivity(i);}catch(Throwable e){
            try{android.widget.Toast.makeText(a,(label==null?"Screen":label)+" unavailable",android.widget.Toast.LENGTH_SHORT).show();}catch(Throwable ignored){}
        }
    }
''',1)
p.write_text(s)

p=pkg/'MainActivity.java'
s=p.read_text()
pattern=r'startActivity\(new Intent\(this,\s*([A-Za-z0-9_]+)\.class\)\);'
s,n=re.subn(pattern,lambda m:'KingSafe.open(this,new Intent(this,%s.class),"%s");'%(m.group(1),m.group(1)),s)
if n < 1:
    raise SystemExit('No MainActivity navigation routes patched')
p.write_text(s)

g=root/'app/build.gradle'
s=g.read_text()
old="versionCode 142; versionName '9.4.3-stability-followup'"
if old not in s: raise SystemExit('version marker missing')
g.write_text(s.replace(old,"versionCode 143; versionName '9.4.4-navigation-guard'",1))
print('v9.4.4 navigation guard applied',n)
