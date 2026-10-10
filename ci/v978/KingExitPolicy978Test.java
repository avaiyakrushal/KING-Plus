package com.kingplus.social;
public final class KingExitPolicy978Test {
    private static int tests;
    private static void check(boolean value,boolean expected,String label){
        if(value!=expected)throw new AssertionError(label);tests++;
    }
    public static void main(String[] args){
        check(KingExitPolicy978.showSignalAlert(9,false),false,"screenshot SIGKILL no stage");
        check(KingExitPolicy978.showSignalAlert(9,true),false,"SIGKILL even with process marker");
        check(KingExitPolicy978.showSignalAlert(15,true),false,"SIGTERM");
        check(KingExitPolicy978.showSignalAlert(0,true),false,"unknown status");
        check(KingExitPolicy978.showSignalAlert(11,false),true,"SIGSEGV early startup");
        check(KingExitPolicy978.showSignalAlert(6,false),true,"SIGABRT early startup");
        check(KingExitPolicy978.showSignalAlert(7,false),true,"SIGBUS");
        check(KingExitPolicy978.showSignalAlert(4,false),true,"SIGILL");
        check(KingExitPolicy978.showSignalAlert(8,false),true,"SIGFPE");
        check(KingExitPolicy978.showSignalAlert(10,false),false,"unattributed other signal");
        check(KingExitPolicy978.showSignalAlert(10,true),true,"same process signal 10");
        check(KingExitPolicy978.showSignalAlert(99,true),false,"out of range");
        check(KingExitPolicy978.isTerminationSignal(9),true,"OS kill");
        check(KingExitPolicy978.isTerminationSignal(11),false,"not OS kill");
        check(KingExitPolicy978.signalLabel(9).contains("SIGKILL"),true,"raw signal retained");
        check(KingExitPolicy978.signalLabel(11).contains("SIGSEGV"),true,"fault name");
        System.out.println("PASS KING Plus v9.7.8 exit report classification: "+tests+" cases");
    }
}
