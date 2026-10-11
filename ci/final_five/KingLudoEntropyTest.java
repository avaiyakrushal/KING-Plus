package com.kingplus.social;

public final class KingLudoEntropyTest {
    private KingLudoEntropyTest(){}
    public static void main(String[] args){
        final long sec=1791710000L;
        final String code="ABC123";
        final int rev=42;
        int x=KingLudoDiceEntropy999.dice(sec,120000000,code,rev);
        for(int i=0;i<50;i++)
            if(x!=KingLudoDiceEntropy999.dice(sec,120000000,code,rev))
                throw new AssertionError("same server state must yield same die");
        int[] counts=new int[7];
        for(int i=0;i<60000;i++){
            int die=KingLudoDiceEntropy999.dice(sec+i/1000,
                (int)(((long)i*683_713L)%1_000_000_000L),code,i%997);
            if(die<1||die>6)throw new AssertionError("die out of range");
            counts[die]++;
        }
        for(int n=1;n<=6;n++)
            if(counts[n]<8700||counts[n]>11300)
                throw new AssertionError("statistical sanity check failed for "+n+": "+counts[n]);
        try {
            KingLudoDiceEntropy999.dice(0,0,code,1);
            throw new AssertionError("no server time must be rejected");
        }catch(IllegalArgumentException correct){}
        try {
            KingLudoDiceEntropy999.dice(sec,-1,code,1);
            throw new AssertionError("invalid nanos must be rejected");
        }catch(IllegalArgumentException correct){}
        System.out.println("PASS 60000 unbiased-range Ludo dice cases, deterministic replay and invalid-input rejection");
        System.out.println("NOTE source of entropy is Firestore server timestamp; not cryptographic anti-cheat or monetary gaming");
    }
}
