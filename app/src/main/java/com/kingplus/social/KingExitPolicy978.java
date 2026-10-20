package com.kingplus.social;

/**
 * Android ApplicationExitInfo.REASON_SIGNALED alone is not proof of app code crash.
 * getStatus() is a POSIX signal number for this reason. SIGKILL/SIGTERM
 * frequently represent OS/user lifecycle termination (including upgrade and LMK).
 *
 * This helper controls only intrusive crash popups. The OS history is retained
 * by Android and any benign event can be logged without blocking app login.
 */
public final class KingExitPolicy978 {
    private KingExitPolicy978() {}

    public static final int SIGKILL = 9;
    public static final int SIGTERM = 15;
    public static final int SIGABRT = 6;
    public static final int SIGSEGV = 11;
    public static final int SIGBUS = 7;
    public static final int SIGILL = 4;
    public static final int SIGFPE = 8;

    public static boolean isTerminationSignal(int status) {
        return status == SIGKILL || status == SIGTERM;
    }
    public static boolean isNativeFaultSignal(int status) {
        return status == SIGABRT || status == SIGSEGV || status == SIGBUS
            || status == SIGILL || status == SIGFPE;
    }
    public static boolean showSignalAlert(int status, boolean samePreviousAppProcess) {
        // Signal 9/15 is an unconfirmed process termination, not a proven crash.
        if (isTerminationSignal(status) || status <= 0) return false;
        // A core-fault signal deserves a dialog even if the previous process PID
        // could not be matched during reinstall or an early startup failure.
        if (isNativeFaultSignal(status)) return true;
        // Other signaled exits without PID/stage evidence are only diagnostic.
        return samePreviousAppProcess && status < 64;
    }
    public static String signalLabel(int status) {
        switch(status) {
            case SIGKILL: return "SIGKILL (9) — OS terminated process; cause unconfirmed";
            case SIGTERM: return "SIGTERM (15) — orderly termination signal";
            case SIGABRT: return "SIGABRT (6) — abort";
            case SIGSEGV: return "SIGSEGV (11) — memory access fault";
            case SIGBUS: return "SIGBUS (7) — bus fault";
            case SIGILL: return "SIGILL (4) — illegal instruction";
            case SIGFPE: return "SIGFPE (8) — arithmetic fault";
            default: return "Signal " + status + " (unknown)";
        }
    }
}
