package com.kingplus.social;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Canonical navigation: visible mini-game tiles always open a real, interactive game.
 * Ludo has its own complete online/offline lobby. Other local titles use GamePlayActivity.
 */
public final class KingGameRoutes980 {
    private KingGameRoutes980() {}
    private static final Map<String,String> MAP;
    static {
        HashMap<String,String> m=new HashMap<>();
        for(String g:new String[]{"ludo","rps","dice","guess","coin","memory",
                "reaction","highlow","wheel","sheep","werewolf","spy","draw",
                "bingo","zoo","domino","slot","tic_tac_toe"})m.put(g,g);
        m.put("draw & guess","draw");
        m.put("sheep fight","sheep");
        m.put("ludo master","ludo");
        m.put("rock paper scissors","rps");
        m.put("dice duel","dice");
        m.put("crazy zoo","zoo");
        m.put("number","guess");
        m.put("tic tac toe","tic_tac_toe");
        MAP=Collections.unmodifiableMap(m);
    }
    public static String localGame(String name) {
        if(name==null)return null;
        return MAP.get(name.toLowerCase(Locale.US).trim());
    }
    public static boolean playableLocal(String name) {return localGame(name)!=null;}
    public static boolean onlineRoomRound(String name) {
        String code=localGame(name);
        return "rps".equals(code)||"dice".equals(code)||"coin".equals(code)
            ||"wheel".equals(code)||"bingo".equals(code)||"guess".equals(code);
    }
    public static String roomRoundType(String name){
        String code=localGame(name);
        return "guess".equals(code)?"number":onlineRoomRound(code)?code:null;
    }
}
