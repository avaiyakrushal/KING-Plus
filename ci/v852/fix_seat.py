from pathlib import Path
p=Path('/tmp/src/app/src/main/java/com/kingplus/social/OnlineLudoActivity.java')
s=p.read_text()
old='if(cap==2){if(ps.size()>2&&ps.get(2).isEmpty())seat=2;}else for(int x=1;x<4;x++)if(ps.get(x).isEmpty()){seat=x;break;}'
assert old in s
p.write_text(s.replace(old,'for(int x=0;x<4;x++)if((cap==4||x==0||x==2)&&ps.get(x).isEmpty()){seat=x;break;}'))
