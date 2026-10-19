package com.kingplus.social;
/** Pure-Java low-RAM frame interpolation and walk cycle. No client auth writes. */
public final class KingAvatarAnimation9932 {
  private KingAvatarAnimation9932(){}
  public static float smooth(float current,float target){
    if(!Float.isFinite(current)||!Float.isFinite(target))throw new IllegalArgumentException("Non-finite pose");
    float diff=target-current;
    if(Math.abs(diff)<.015f)return target;
    return current+diff*.26f;
  }
  public static boolean isMoving(float x,float z,float targetX,float targetZ){
    return Math.abs(x-targetX)+Math.abs(z-targetZ)>.018f;
  }
  public static float lift(float time,int phase){
    if(!Float.isFinite(time))return 0f;
    return .11f*Math.max(0f,(float)Math.sin(time*9f+phase*(float)Math.PI));
  }
}
