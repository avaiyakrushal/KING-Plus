package com.kingplus.social;

/** Conference join requests are not proof that media is connected. */
public final class KingVoiceSession10065 {
    private String expectedUrl;
    private boolean connected;
    public KingVoiceSession10065(String url){expectedUrl=url;}
    public boolean accepts(String url){
        if(expectedUrl==null||url==null)return false;
        try{
            java.net.URI a=new java.net.URI(expectedUrl),b=new java.net.URI(url);
            return "https".equalsIgnoreCase(b.getScheme())&&b.getRawUserInfo()==null
                &&a.getHost().equalsIgnoreCase(b.getHost())&&(b.getPort()==-1||b.getPort()==443)
                &&a.getPath().equalsIgnoreCase(b.getPath());
        }catch(Exception ignored){return false;}
    }
    public boolean joined(String url){if(!accepts(url))return false;connected=true;return true;}
    public boolean terminated(String url){if(!accepts(url))return false;connected=false;return true;}
    public boolean connected(){return connected;}
    public void close(){connected=false;expectedUrl=null;}
}
