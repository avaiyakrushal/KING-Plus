package com.kingplus.social;
public final class KingAvatarMotion9929Test {
    static void yes(boolean v,String msg){if(!v)throw new AssertionError(msg);}
    static void invalid(Runnable f){boolean got=false;try{f.run();}catch(IllegalArgumentException e){got=true;}yes(got,"Must refuse invalid avatar motion");}
    public static void main(String[] args){
        KingAvatarMotion9929.Pose p=new KingAvatarMotion9929.Pose(0,0,"idle");
        p=p.move(1,0);yes(Math.abs(p.x-.55f)<.001,"right should move");
        p=p.move(0,-1);yes(Math.abs(p.z+.55f)<.001,"forward should move");
        p=p.dance();yes("dance".equals(p.action),"dance should animate");
        p=p.dance();yes("idle".equals(p.action),"dance toggles off");
        p=new KingAvatarMotion9929.Pose(44,99,"walk");
        yes(p.x==KingAvatarMotion9929.MAX_X&&p.z==KingAvatarMotion9929.MAX_Z,"world boundaries hold");
        invalid(()->new KingAvatarMotion9929.Pose(Float.NaN,1,"walk"));
        invalid(()->new KingAvatarMotion9929.Pose(1,1,"owner"));
        invalid(()->new KingAvatarMotion9929.Pose(1,1,"idle").move(5,0));
        System.out.println("PASS KING bounded walk, dance, ownership-friendly motion state math");
    }
}