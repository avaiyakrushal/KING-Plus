package com.kingplus.social;

/** Pure Java validation for the owner's Cloudflare Worker and Wallet replies.
 * Android/network code must not silently trust arbitrary OTP hosts or balances.
 */
public final class KingWalletRules976 {
    private KingWalletRules976(){}
    public static String trustedWorkerUrl(String raw){
        if(raw==null||raw.trim().length()>240)return null;
        try{
            java.net.URI uri=new java.net.URI(raw.trim());
            String host=uri.getHost();
            if(!"https".equalsIgnoreCase(uri.getScheme())||host==null
                ||!host.toLowerCase(java.util.Locale.US)
                    .matches("kingplus-lowcost\\.[a-z0-9-]+\\.workers\\.dev")
                ||(uri.getPort()!=-1&&uri.getPort()!=443)
                ||uri.getRawUserInfo()!=null||uri.getRawQuery()!=null
                ||uri.getRawFragment()!=null
                ||(uri.getRawPath()!=null&&!uri.getRawPath().isEmpty()
                    &&!"/".equals(uri.getRawPath())))return null;
            return "https://"+host.toLowerCase(java.util.Locale.US);
        }catch(Exception ignored){return null;}
    }
    public static boolean validWallet(long diamonds,long rechargeTotal,int vipLevel){
        return diamonds>=0 && diamonds<=1000000000L
            && rechargeTotal>=0 && rechargeTotal<=1000000000L
            && vipLevel>=0&&vipLevel<=50;
    }
}
