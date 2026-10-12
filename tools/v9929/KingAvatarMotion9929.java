package com.kingplus.social;

/** Original KING Party world coordinates. Kept pure Java for offline regression tests. */
public final class KingAvatarMotion9929 {
  public static final float MIN_X=-4.4f,MAX_X=4.4f,MIN_Z=-2.6f,MAX_Z=2.7f,STEP=.55f;
  private KingAvatarMotion9929(){}
  public static float clampX(float x){return Math.max(MIN_X,Math.min(MAX_X,x));}
  public static float clampZ(float z){return Math.max(MIN_Z,Math.min(MAX_Z,z));}
  public static boolean actionAllowed(String a){return "idle".equals(a)||"walk".equals(a)||"dance".equals(a);}
  public static final class Pose {
    public final float x,z;
    public final String action;
    public Pose(float x,float z,String action){
      if(Float.isNaN(x)||Float.isInfinite(x)||Float.isNaN(z)||Float.isInfinite(z))
        throw new IllegalArgumentException("Avatar position must be finite");
      if(!actionAllowed(action))throw new IllegalArgumentException("Invalid avatar action");
      this.x=clampX(x);this.z=clampZ(z);this.action=action;
    }
    public Pose move(int dx,int dz){
      if(Math.abs(dx)+Math.abs(dz)!=1)throw new IllegalArgumentException("Only single-step movement");
      return new Pose(x+dx*STEP,z+dz*STEP,"walk");
    }
    public Pose dance(){return new Pose(x,z,"dance".equals(action)?"idle":"dance");}
  }
}