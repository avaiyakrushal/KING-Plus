package com.kingplus.social;
import java.util.*;
public final class KingVoiceAndGift10065Test {
 private static void check(boolean condition){if(!condition)throw new AssertionError();}
 public static void main(String[] args){
  KingVoiceSession10065 voice=new KingVoiceSession10065("https://meet.jit.si/KINGPlus-real-room");
  check(!voice.connected());
  check(!voice.joined("https://meet.jit.si/KINGPlus-other-room"));
  check(!voice.joined("https://meet.jit.si.evil.test/KINGPlus-real-room"));
  check(!voice.joined("http://meet.jit.si/KINGPlus-real-room"));
  check(!voice.joined("https://evil@meet.jit.si/KINGPlus-real-room"));
  check(!voice.joined(null));
  check(voice.joined("https://meet.jit.si/KINGPlus-real-room#config.audioOnly=true"));
  check(voice.connected());
  check(voice.joined("https://meet.jit.si/kingplus-real-room"));
  check(!voice.terminated("https://meet.jit.si/another"));check(voice.connected());
  check(voice.terminated("https://meet.jit.si/KINGPlus-real-room"));check(!voice.connected());
  voice.close();check(!voice.joined("https://meet.jit.si/KINGPlus-real-room"));
  Map<String,Object> receipt=new HashMap<>();receipt.put("ok",true);receipt.put("balance",123L);
  check(!KingGiftReply10065.confirmed(receipt,"alice","r-1"));
  receipt.put("giftId",KingGiftReply10065.giftId("alice","r-1"));check(KingGiftReply10065.confirmed(receipt,"alice","r-1"));
  check(!KingGiftReply10065.confirmed(receipt,"bob","r-1"));
  check(!KingGiftReply10065.confirmed(receipt,"alice","r-2"));
  for(Object invalid:new Object[]{-1L,1.5,Double.NaN,Double.POSITIVE_INFINITY,"123",1000000001L}){
   receipt.put("balance",invalid);check(!KingGiftReply10065.confirmed(receipt,"alice","r-1"));
  }
  receipt.put("balance",0d);check(KingGiftReply10065.confirmed(receipt,"alice","r-1"));
  check(KingGiftReply10065.giftId("alice","r-1").equals("gift_1beff2b010aa7d651881c6d7581710d8f4d47795bd8ad3d21ecf4d05d7e7c2d4"));
  System.out.println("PASS voice event scope/lifecycle and exact gift response receipt validation");
 }
}
