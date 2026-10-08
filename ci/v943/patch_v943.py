#!/usr/bin/env python3
"""v9.4.3 stability follow-up applied after v9.4.2."""
from pathlib import Path
import sys
root=Path(sys.argv[1]); pkg=root/'app/src/main/java/com/kingplus/social'
p=pkg/'KingEffectBudget.java'
s=p.read_text()
old='''        Long prev=last.put(kind,now);
        return prev==null||now-prev>=gap;'''
new='''        synchronized(last){
            Long prev=last.get(kind);
            if(prev!=null && now>=prev && now-prev<gap)return false;
            last.put(kind,now);
            return true;
        }'''
if old not in s:raise SystemExit('EffectBudget marker missing')
p.write_text(s.replace(old,new,1))
# Clear stale reconnect callbacks and do not retry UI after activity destruction.
p=pkg/'KingUiState.java';s=p.read_text()
old='''        if(retry!=null)b.setPositiveButton("Retry",(d,w)->KingSafe.run(a,"retry:"+title,retry));'''
new='''        if(retry!=null)b.setPositiveButton("Retry",(d,w)->{
            if(!a.isFinishing()&&!a.isDestroyed())KingSafe.run(a,"retry:"+title,retry);
        });'''
if old not in s:raise SystemExit('UI state retry marker missing')
p.write_text(s.replace(old,new,1))
g=root/'app/build.gradle';s=g.read_text()
old="versionCode 141; versionName '9.4.2-parity-next'"
if old not in s:raise SystemExit('Version marker missing')
g.write_text(s.replace(old,"versionCode 142; versionName '9.4.3-stability-followup'",1))
print('v9.4.3 atomic effect throttle and lifecycle-safe retry patch applied')
