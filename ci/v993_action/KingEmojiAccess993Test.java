package com.kingplus.social;
public class KingEmojiAccess993Test {
 private static int n=0;
 private static void yes(boolean ok,String s){if(!ok)throw new AssertionError(s);n++;}
 public static void main(String[] args){
  yes(KingEmojiAccess993.CATALOG_SIZE==120,"120");
  yes(KingEmojiAccess993.canSend(0,0,0),"old free");
  yes(KingEmojiAccess993.canSend(49,0,0),"old compatibility");
  yes(KingEmojiAccess993.canSend(73,0,0),"new free action");
  yes(!KingEmojiAccess993.canSend(105,0,0),"level locked");
  yes(KingEmojiAccess993.canSend(105,15,0),"level earned");
  yes(!KingEmojiAccess993.canSend(110,999,999),"paid VIP cannot bypass while disabled");
  yes(!KingEmojiAccess993.canSend(119,999,999),"VIP top locked");
  yes(!KingEmojiAccess993.canSend(-1,0,0),"invalid low");
  yes(!KingEmojiAccess993.canSend(120,0,0),"invalid high");
  yes(KingEmojiAccess993.range(50,75).length==25,"single tab cap");
  yes(KingEmojiAccess993.range(100,120).length==20,"level+VIP");
  yes(KingEmojiAccess993.lockMessage(111).contains("preview only"),"honest VIP gate");
  System.out.println("PASS "+n+" KING action-emoji level and non-billing VIP guard cases");
 }
}
