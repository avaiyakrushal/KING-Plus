package com.kingplus.social;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Free, real 5x5 Bingo card: 5 B/I/N/G/O columns, 1..75 balls, free center.
 * Keeps logic deterministic for tests when caller supplies a Random.
 * Never grants diamonds, coins, paid rewards or VIP entitlements.
 */
public final class KingBingoRules9980 {
    private KingBingoRules9980() {}
    public static List<Integer> newCard(java.util.Random random) {
        if (random == null) throw new IllegalArgumentException("random required");
        List<Integer> card = new ArrayList<>(Collections.nCopies(25, 0));
        for (int col = 0; col < 5; col++) {
            List<Integer> bucket = new ArrayList<>();
            for (int n = col * 15 + 1; n <= col * 15 + 15; n++) bucket.add(n);
            Collections.shuffle(bucket, random);
            for (int row = 0; row < 5; row++) card.set(row * 5 + col, bucket.get(row));
        }
        card.set(12, 0);
        return card;
    }
    public static List<Integer> newCard() {return newCard(new SecureRandom());}
    public static boolean validCard(List<Integer> card) {
        if (card == null || card.size() != 25 || card.get(12) == null || card.get(12) != 0) return false;
        Set<Integer> seen = new HashSet<>();
        for (int pos=0;pos<25;pos++) {
            Integer value=card.get(pos);
            if(value == null) return false;
            if(pos==12) continue;
            int min = (pos % 5)*15+1;
            if(value<min || value>min+14 || !seen.add(value))return false;
        }
        return true;
    }
    public static boolean validCalls(List<Integer> calls) {
        if (calls == null || calls.isEmpty() || calls.get(0) == null || calls.get(0) != 0 || calls.size()>76)return false;
        Set<Integer> seen = new HashSet<>();
        for (Integer n:calls) if(n==null||n<0||n>75||!seen.add(n))return false;
        return true;
    }
    public static boolean isWinningLine(List<Integer> card, List<Integer> calls) {
        if(!validCard(card)||!validCalls(calls))return false;
        Set<Integer> called=new HashSet<>(calls);
        for(int row=0;row<5;row++) {
            boolean yes=true;
            for(int col=0;col<5;col++)yes &= called.contains(card.get(row*5+col));
            if(yes)return true;
        }
        for(int col=0;col<5;col++) {
            boolean yes=true;
            for(int row=0;row<5;row++)yes &= called.contains(card.get(row*5+col));
            if(yes)return true;
        }
        boolean diag=true,other=true;
        for(int i=0;i<5;i++){
            diag &= called.contains(card.get(i*5+i));
            other &= called.contains(card.get(i*5+4-i));
        }
        return diag||other;
    }
    public static List<Integer> remaining(List<Integer> calls) {
        if(!validCalls(calls))throw new IllegalArgumentException("invalid call sequence");
        Set<Integer> used=new HashSet<>(calls);
        List<Integer> left=new ArrayList<>();
        for(int n=1;n<=75;n++)if(!used.contains(n))left.add(n);
        return left;
    }
}
