package com.kingplus.social;
public final class KingPagePolicy10067Test {
    private static void check(boolean ok){if(!ok)throw new AssertionError();}
    public static void main(String[] args){
        check(KingPagePolicy10067.categories("Music").contains("KTV"));
        check(KingPagePolicy10067.categories("Music").contains("radio"));
        check(KingPagePolicy10067.categories("Event").contains("Wedding"));
        check(KingPagePolicy10067.categories("Game").contains("Pick Me"));
        check(KingPagePolicy10067.categories("PK").contains("PK"));
        check(KingPagePolicy10067.categories("Pick Me").contains("Pick Me"));
        check(KingPagePolicy10067.categories("Family").contains("Family"));
        check(KingPagePolicy10067.categories("Team Up").contains("Gang Up"));
        check(KingPagePolicy10067.categories("Video").contains("Multi Video"));
        check(KingPagePolicy10067.categories("Hot").isEmpty());
        check(KingPagePolicy10067.categories("All").isEmpty());
        check(KingPagePolicy10067.balance(7L)==7L);
        for(Object malformed:new Object[]{null,7,7.0,"7",-1L,1000000001L})check(KingPagePolicy10067.balance(malformed)==null);
        for(String id:new String[]{"polygenelubricants","GydZG_","room",null}){
            int color=KingPagePolicy10067.palette(id,6);check(color>=0&&color<6);
        }
        System.out.println("PASS category aliases, distinct trusted wallet values and negative-hash room covers");
    }
}
