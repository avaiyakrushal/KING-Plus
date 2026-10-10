package com.kingplus.social;
public final class KingGameRoutes980Test {
    private static int n;
    private static void eq(boolean got,boolean expected,String label){
        if(got!=expected)throw new AssertionError(label);n++;
    }
    public static void main(String[] args){
        String[] codes={"ludo","rps","dice","guess","coin","memory","reaction",
            "highlow","wheel","sheep","werewolf","spy","draw","bingo","zoo",
            "domino","slot","tic_tac_toe"};
        for(String code:codes)eq(KingGameRoutes980.playableLocal(code),true,code);
        eq(KingGameRoutes980.playableLocal("unknown"),false,"unknown titles hidden");
        eq(KingGameRoutes980.playableLocal(null),false,"null");
        eq(KingGameRoutes980.localGame("Sheep Fight").equals("sheep"),true,"Sheep");
        eq(KingGameRoutes980.localGame("Ludo Master").equals("ludo"),true,"Ludo");
        eq(KingGameRoutes980.localGame("Draw & Guess").equals("draw"),true,"Draw");
        eq(KingGameRoutes980.localGame("Crazy Zoo").equals("zoo"),true,"Zoo");
        eq(KingGameRoutes980.onlineRoomRound("rps"),true,"online rps");
        eq(KingGameRoutes980.onlineRoomRound("dice"),true,"online dice");
        eq(KingGameRoutes980.onlineRoomRound("bingo"),true,"online bingo");
        eq(KingGameRoutes980.onlineRoomRound("werewolf"),false,"online Werewolf not misrepresented");
        eq(KingGameRoutes980.onlineRoomRound("draw"),false,"pass-the-phone drawing not online");
        eq("number".equals(KingGameRoutes980.roomRoundType("guess")),true,"server number round");
        eq(KingGameRoutes980.roomRoundType("zoo")==null,true,"Zoo not room online");
        System.out.println("PASS KING Plus v9.8.0 game routing: "+n+" checks");
    }
}
