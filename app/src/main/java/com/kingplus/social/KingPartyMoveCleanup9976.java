package com.kingplus.social;
import java.util.*;
/** Targets only the frozen, real 2–50 Ready players, NEVER all room history. */
public final class KingPartyMoveCleanup9976 {
  private KingPartyMoveCleanup9976(){}
  public static List<String> roster(Object value){
    if(!(value instanceof List))return Collections.emptyList();
    List<?> raw=(List<?>)value;
    if(raw.size()<2||raw.size()>50)return Collections.emptyList();
    List<String> clean=new ArrayList<>();
    Set<String> unique=new HashSet<>();
    for(Object part:raw){
      if(!(part instanceof String))return Collections.emptyList();
      String uid=(String)part;
      if(uid.isEmpty()||!unique.add(uid))return Collections.emptyList();
      clean.add(uid);
    }
    return clean;
  }
  public static boolean valid(List<String> roster){return roster!=null&&roster.size()>=2&&roster.size()<=50;}
}
