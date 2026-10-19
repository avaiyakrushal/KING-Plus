package com.kingplus.social;
import java.util.List;

/** Deterministic independent KING Knife Arena physics. No reference-game assets. */
public final class KingKnifeRules9923 {
  private KingKnifeRules9923(){}
  public static final float COLLISION_RADIANS=0.32f;
  public static final int KNIVES_PER_ROUND=7;
  public static float normalize(float angle){
    while(angle>Math.PI)angle-=(float)(2*Math.PI);
    while(angle< -Math.PI)angle+=(float)(2*Math.PI);
    return angle;
  }
  /** The knife tip is fixed at the six-o'clock entry point. */
  public static float localAngle(float rotatingWheelRadians){
    return normalize((float)(Math.PI/2)-rotatingWheelRadians);
  }
  public static boolean hitsBlade(List<Float> lockedLocalAngles,float incomingLocalAngle){
    for(float angle:lockedLocalAngles)
      if(Math.abs(normalize(angle-incomingLocalAngle))<COLLISION_RADIANS)return true;
    return false;
  }
  public static boolean roundCleared(int successfulKnives){
    return successfulKnives>=KNIVES_PER_ROUND;
  }
}