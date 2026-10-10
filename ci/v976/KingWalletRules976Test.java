package com.kingplus.social;
public final class KingWalletRules976Test{
    private static int n;
    private static void check(boolean actual,boolean expected,String label){
        if(actual!=expected)throw new AssertionError(label);
        n++;
    }
    private static void url(String raw,String expected,String label){
        String got=KingWalletRules976.trustedWorkerUrl(raw);
        check(got==null?expected==null:got.equals(expected),true,label);
    }
    public static void main(String[] args){
        url("https://kingplus-lowcost.owner.workers.dev",
            "https://kingplus-lowcost.owner.workers.dev","owner Worker");
        url("https://kingplus-lowcost.owner.workers.dev/",
            "https://kingplus-lowcost.owner.workers.dev","trailing slash");
        url("https://KINGPLUS-LOWCOST.Owner.WORKERS.DEV",
            "https://kingplus-lowcost.owner.workers.dev","case");
        url("http://kingplus-lowcost.owner.workers.dev",null,"no HTTP");
        url("https://wrong-worker.owner.workers.dev",null,"wrong Worker");
        url("https://kingplus-lowcost.owner.workers.dev.attacker.com",null,"subdomain trap");
        url("https://kingplus-lowcost.owner.workers.dev:8443",null,"no unexpected port");
        url("https://evil@kingplus-lowcost.owner.workers.dev",null,"no userinfo");
        url("https://kingplus-lowcost.owner.workers.dev/path",null,"no extra path");
        url("https://kingplus-lowcost.owner.workers.dev?next=/otp",null,"no query");
        url("https://kingplus-lowcost.owner.workers.dev#otp",null,"no fragment");
        url("https://127.0.0.1",null,"no local IP");
        url("https://kingplus-lowcost.owner.workers.dev/a/../",null,"no path traversal");
        url(null,null,"null");
        check(KingWalletRules976.validWallet(0,0,0),true,"empty verified wallet");
        check(KingWalletRules976.validWallet(100,1000,2),true,"real wallet");
        check(KingWalletRules976.validWallet(-1,0,0),false,"negative balance");
        check(KingWalletRules976.validWallet(0,-1,0),false,"negative recharge");
        check(KingWalletRules976.validWallet(0,0,-1),false,"negative VIP");
        check(KingWalletRules976.validWallet(0,0,51),false,"invalid VIP");
        check(KingWalletRules976.validWallet(1000000001,0,0),false,"implausible wallet");
        System.out.println("PASS KING Plus v9.7.6 Wallet rules: "+n+" tests");
    }
}
