package com.kingplus.social;

/** Executable tests for the pure Party microphone-consent and reconnect policies. */
public final class KingPartyVoicePolicyTest {
    private static int checks;
    private KingPartyVoicePolicyTest() {}

    private static void expect(String name, boolean actual, boolean expected) {
        checks++;
        if (actual != expected) {
            throw new AssertionError(name + ": expected " + expected + " but got " + actual);
        }
    }

    private static boolean transmit(boolean approved, boolean cloud, String room,
            String approvedRoom, int seat, int approvedSeat, String uid,
            String seatOwner, boolean backendMic, boolean muteAll, boolean moderator) {
        return KingPartyMicConsent9956.canTransmit(approved, cloud, room, approvedRoom,
                seat, approvedSeat, uid, seatOwner, backendMic, muteAll, moderator);
    }

    private static boolean reconnect(boolean pending, boolean optedIn, boolean foreground,
            boolean online, boolean cloudRoom, boolean memberSeen, String expectedRoom,
            String actualRoom, String expectedUid, String actualUid,
            boolean generationMatches, boolean viewVisible) {
        return KingPartyVoiceReconnect9957.eligible(pending, optedIn, foreground, online,
                cloudRoom, memberSeen, expectedRoom, actualRoom, expectedUid, actualUid,
                generationMatches, viewVisible);
    }

    public static void main(String[] args) {
        expect("valid mic consent", transmit(true,true,"r1","r1",2,2,"u1","u1",true,false,false), true);
        expect("consent required", transmit(false,true,"r1","r1",2,2,"u1","u1",true,false,false), false);
        expect("cloud room required", transmit(true,false,"r1","r1",2,2,"u1","u1",true,false,false), false);
        expect("room binding", transmit(true,true,"r1","r2",2,2,"u1","u1",true,false,false), false);
        expect("positive seat required", transmit(true,true,"r1","r1",0,0,"u1","u1",true,false,false), false);
        expect("seat binding", transmit(true,true,"r1","r1",2,3,"u1","u1",true,false,false), false);
        expect("signed-in owner binding", transmit(true,true,"r1","r1",2,2,"u1","u2",true,false,false), false);
        expect("backend mic flag", transmit(true,true,"r1","r1",2,2,"u1","u1",false,false,false), false);
        expect("mute-all blocks member", transmit(true,true,"r1","r1",2,2,"u1","u1",true,true,false), false);
        expect("moderator may transmit under mute-all", transmit(true,true,"r1","r1",2,2,"u1","u1",true,true,true), true);
        expect("valid reconnect", reconnect(true,true,true,true,true,true,"r1","r1","u1","u1",true,true), true);
        expect("pending required", reconnect(false,true,true,true,true,true,"r1","r1","u1","u1",true,true), false);
        expect("opt-in required", reconnect(true,false,true,true,true,true,"r1","r1","u1","u1",true,true), false);
        expect("foreground required", reconnect(true,true,false,true,true,true,"r1","r1","u1","u1",true,true), false);
        expect("online required", reconnect(true,true,true,false,true,true,"r1","r1","u1","u1",true,true), false);
        expect("cloud room required for reconnect", reconnect(true,true,true,true,false,true,"r1","r1","u1","u1",true,true), false);
        expect("membership required", reconnect(true,true,true,true,true,false,"r1","r1","u1","u1",true,true), false);
        expect("room identity required", reconnect(true,true,true,true,true,true,"r1","r2","u1","u1",true,true), false);
        expect("uid identity required", reconnect(true,true,true,true,true,true,"r1","r1","u1","u2",true,true), false);
        expect("generation required", reconnect(true,true,true,true,true,true,"r1","r1","u1","u1",false,true), false);
        expect("visible party required", reconnect(true,true,true,true,true,true,"r1","r1","u1","u1",true,false), false);
        expect("nonempty expected room", reconnect(true,true,true,true,true,true,"","r1","u1","u1",true,true), false);
        expect("nonempty expected uid", reconnect(true,true,true,true,true,true,"r1","r1","","u1",true,true), false);
        System.out.println("PASS Party voice policy: " + checks + " executable assertions");
    }
}
