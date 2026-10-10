package com.kingplus.social;

public final class KingRoomCallback982Test {
    private static int checks;
    private static void eq(boolean actual,boolean expected,String why){
        if(actual!=expected)throw new AssertionError(why+" expected="+expected);
        checks++;
    }
    public static void main(String[] args){
        eq(KingRoomCallback982.current("roomA","alice",5,"roomA","alice",5,true,true),true,"current visit");
        eq(KingRoomCallback982.current("roomA","alice",5,"roomB","alice",5,true,true),false,"moved to another room");
        eq(KingRoomCallback982.current("roomA","alice",5,"roomA","bob",5,true,true),false,"account switched");
        eq(KingRoomCallback982.current("roomA","alice",5,"roomA","alice",6,true,true),false,"same room new visit");
        eq(KingRoomCallback982.current("roomA","alice",5,"roomA","alice",5,false,true),false,"back to Lobby");
        eq(KingRoomCallback982.current("roomA","alice",5,"roomA","alice",5,true,false),false,"destroyed activity");
        eq(KingRoomCallback982.current(null,"alice",5,"roomA","alice",5,true,true),false,"null old room");
        eq(KingRoomCallback982.current("","alice",5,"roomA","alice",5,true,true),false,"empty room");
        eq(KingRoomCallback982.current("roomA",null,5,"roomA","alice",5,true,true),false,"null old UID");
        eq(KingRoomCallback982.current("roomA","alice",0,"roomA","alice",0,true,true),false,"invalid visit");
        String report=KingRoomCallback982.supportReport("9.8.2","123456","abc123",
            "Failed to get\ndocument because offline",false,true);
        eq(report.contains("Build: 9.8.2"),true,"version report");
        eq(report.contains("Room code: 123456"),true,"room report");
        eq(report.contains("Internet validated: false"),true,"network report");
        eq(report.contains("Firebase signed in: true"),true,"account report");
        eq(report.contains("Failed to get document because offline"),true,"sanitized error");
        eq(report.indexOf("Failed to get\ndocument")==-1,true,"no newline injection");
        eq(report.length()<700,true,"bounded diagnostics");
        System.out.println("PASS KING Plus v9.8.2 Party room-session isolation: "+checks+" tests");
    }
}
