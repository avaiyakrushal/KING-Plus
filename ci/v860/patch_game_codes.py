#!/usr/bin/env python3
from pathlib import Path
import sys
root=Path(sys.argv[1]) if len(sys.argv)>1 else Path('/tmp/src')
p=root/'app/src/main/java/com/kingplus/social/MainActivity.java'
s=p.read_text()
old='private String playableCode730(String game){String g=game==null?"":game.toLowerCase(java.util.Locale.US);if(g.contains("ludo"))return "ludo";if(g.contains("sheep"))return "sheep";if(g.contains("werewolf"))return "werewolf";if(g.contains("spy"))return "spy";if(g.contains("draw"))return "draw";if(g.contains("bingo"))return "bingo";if(g.contains("zoo"))return "zoo";if(g.contains("domino"))return "domino";if(g.contains("slot"))return "slot";return "dice";}'
new='private String playableCode730(String game){String g=game==null?"":game.toLowerCase(java.util.Locale.US);if(g.contains("ludo"))return "ludo";if(g.contains("tic"))return "tic_tac_toe";if(g.contains("sheep"))return "sheep";if(g.contains("werewolf"))return "werewolf";if(g.contains("spy"))return "spy";if(g.contains("draw"))return "draw";if(g.contains("bingo"))return "bingo";if(g.contains("zoo"))return "zoo";if(g.contains("domino"))return "domino";if(g.contains("slot"))return "slot";if(g.contains("coin"))return "coin";if(g.contains("memory"))return "memory";if(g.contains("reaction"))return "reaction";if(g.contains("high"))return "highlow";if(g.contains("wheel"))return "wheel";if(g.contains("rock")||g.contains("rps"))return "rps";if(g.contains("guess"))return "guess";return "dice";}'
if old not in s: raise SystemExit('playableCode730 signature changed; patch not applied')
p.write_text(s.replace(old,new,1))
print('all game card routes wired')
