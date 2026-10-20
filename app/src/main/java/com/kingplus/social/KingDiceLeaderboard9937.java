package com.kingplus.social;
import java.util.*;
/** Pure Java view-only leaderboard from authenticated Firestore events.
 * No wallet, VIP, diamond or winner payouts.
 */
public final class KingDiceLeaderboard9937 {
 private static final class P {
   String name;int rolls,sum,best;
   P(String name){this.name=name;}
 }
 private final Map<String,P> byUser=new HashMap<>();
 public static int parseRoll(String raw) {
   if(raw==null)return 0;
   String s=raw.trim();
   // U+1F3B2 followed by a space, a single digit 1-6 and the literal suffix.
   if(!s.matches("^🎲 [1-6] / 6$"))return 0;
   return s.charAt(3)-'0'; // NOTE: UTF-16 dice emoji occupies indices 0..1.
 }
 public void add(String uid,String name,String text) {
   int value=parseRoll(text);
   if(value<1||uid==null||uid.isEmpty())return;
   P p=byUser.get(uid);
   if(p==null){p=new P(name==null||name.trim().isEmpty()?"Player":name.trim());byUser.put(uid,p);}
   p.rolls++;p.sum+=value;p.best=Math.max(p.best,value);
 }
 public String summary() {
   List<Map.Entry<String,P>> players=new ArrayList<>(byUser.entrySet());
   Collections.sort(players,(a,b)->{int c=Integer.compare(b.getValue().sum,a.getValue().sum);
      return c!=0?c:a.getKey().compareTo(b.getKey());});
   StringBuilder s=new StringBuilder("🏆 LIVE DICE LEADERBOARD • FREE\nRecent room rolls (no diamonds)");
   int index=0;
   for(Map.Entry<String,P> v:players) {
     if(++index>8)break;
     P p=v.getValue();
     s.append("\n").append(index).append(". ").append(p.name.length()>30?p.name.substring(0,30):p.name)
       .append(" • ").append(p.sum).append(" points / ").append(p.rolls).append(" rolls");
   }
   return s.toString();
 }
}
