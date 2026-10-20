package com.kingplus.social;

/** Party controls talk to this boundary so a later RTC provider can be substituted. */
interface KingRoomVoice10066 {
    interface Listener { void onState(boolean connected, String message); }
    void start();
    void microphone(boolean allowed, int seat);
    void close();
}
