package com.kingplus.social;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
/** Bounded FIFO for actual on-screen Gift effects, no bogus delivery ACK.
 * Call only from Android main thread.
 */
public final class KingGiftDeliveryQueue9944<T> {
 public static final int LIMIT=24;
 public static final class Entry<T> {
   public final String id; public final T payload;
   Entry(String id,T payload){this.id=id;this.payload=payload;}
 }
 private final ArrayDeque<Entry<T>> pending=new ArrayDeque<>();
 private final HashSet<String> ids=new HashSet<>();
 public boolean offer(String id,T value){
   if(id==null||id.isEmpty()||value==null||pending.size()>=LIMIT||!ids.add(id))return false;
   pending.addLast(new Entry<>(id,value));return true;
 }
 public Entry<T> poll(){
   Entry<T> e=pending.pollFirst();if(e!=null)ids.remove(e.id);return e;
 }
 public List<Entry<T>> clear(){
   List<Entry<T>> prior=new ArrayList<>(pending);pending.clear();ids.clear();return prior;
 }
 public boolean isEmpty(){return pending.isEmpty();}
 public int size(){return pending.size();}
}
