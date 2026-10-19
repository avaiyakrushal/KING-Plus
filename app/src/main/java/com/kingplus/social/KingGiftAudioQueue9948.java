package com.kingplus.social;
import java.util.ArrayDeque;
/** Main logic for serial Gift sounds, no duplicate-name suppression.
 * Guard externally with an audio lock, so a phone uses at most one AudioTrack.
 */
public final class KingGiftAudioQueue9948 {
 public static final int MAX=24;
 private final ArrayDeque<String> queue=new ArrayDeque<>();
 public boolean offer(String gift){
   if(gift==null||gift.trim().isEmpty()||queue.size()>=MAX)return false;
   queue.addLast(gift);return true;
 }
 public String poll(){return queue.pollFirst();}
 public int size(){return queue.size();}
 public void clear(){queue.clear();}
}
