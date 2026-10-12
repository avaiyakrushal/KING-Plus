#!/usr/bin/env python3
"""Original KING v9.9.32: three user-requested video-inspired improvements.
Apply only to the verified *full* v9.9.31 source (not stale repo-root code).
Fail closed when an anchor has changed. No production rules, payments or secret writes.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1])
src=root/'app/src/main/java/com/kingplus/social'
def patchfile(name,changes):
    p=src/name
    text=p.read_text()
    for old,new in changes:
        found=text.count(old)
        if found!=1: raise RuntimeError(f'{name}: expected exactly one anchor; found {found}: {old[:110]}')
        text=text.replace(old,new,1)
    p.write_text(text)

patchfile('KingGiftSound993.java',[
 ('private static final AtomicBoolean busy=new AtomicBoolean(false);',
  'private static final AtomicBoolean busy=new AtomicBoolean(false);\n'
  '    // Bounded, non-overlapping sound queue. Never allocate another concurrent AudioTrack.\n'
  '    private static final java.util.ArrayDeque<String> pending9932=new java.util.ArrayDeque<>();'),
 ('if(now-lastStarted<480||!busy.compareAndSet(false,true))return;',
  'if(!busy.compareAndSet(false,true)){\n'
  '            synchronized(pending9932){\n'
  '                if(pending9932.size()<3 && !pending9932.contains(gift))pending9932.addLast(gift==null?"Gift":gift);\n'
  '            }\n'
  '            return;\n'
  '        }'),
 ('                busy.set(false);\n',
  '                busy.set(false);\n'
  '                final String next9932;\n'
  '                synchronized(pending9932){next9932=pending9932.pollFirst();}\n'
  '                if(next9932!=null && !host.isFinishing() && !host.isDestroyed())\n'
  '                    host.runOnUiThread(()->play(host,next9932));\n')
])
patchfile('KingNative3DRoom.java',[
 ('volatile boolean[] motionDance=new boolean[12];',
  'volatile boolean[] motionDance=new boolean[12];\n'
  '        // Renderer-owned positions: smooth snapshots without extra Firebase writes.\n'
  '        final float[] visualX9932=new float[12], visualZ9932=new float[12];\n'
  '        final String[] visualUid9932=new String[12];'),
 ('''                float x=i<motionX.length&&!Float.isNaN(motionX[i])?motionX[i]:seatX;
                float z=i<motionZ.length&&!Float.isNaN(motionZ[i])?motionZ[i]:seatZ;
                boolean dancing=i<motionDance.length&&motionDance[i];
                // Stable UID-linked procedural outfit: same character palette on both phones.
                String uid=i<people.length?people[i]:null;''',
  '''                float goalX=i<motionX.length&&!Float.isNaN(motionX[i])?motionX[i]:seatX;
                float goalZ=i<motionZ.length&&!Float.isNaN(motionZ[i])?motionZ[i]:seatZ;
                String uid=i<people.length?people[i]:null;
                if(uid==null||!uid.equals(visualUid9932[i])){
                    visualUid9932[i]=uid;
                    visualX9932[i]=seatX;visualZ9932[i]=seatZ;
                }
                float beforeX=visualX9932[i], beforeZ=visualZ9932[i];
                float x=KingAvatarAnimation9932.smooth(beforeX,goalX);
                float z=KingAvatarAnimation9932.smooth(beforeZ,goalZ);
                visualX9932[i]=x;visualZ9932[i]=z;
                boolean walking=KingAvatarAnimation9932.isMoving(x,z,goalX,goalZ);
                boolean dancing=i<motionDance.length&&motionDance[i];
                // Stable UID-linked procedural outfit: same character palette on both phones.'''),
 ('float bob=(float)Math.sin(t*(dancing?6f:1.5f)+i*.9f)*(dancing?.17f:.035f);',
  'float bob=(float)Math.sin(t*(dancing?6f:1.5f)+i*.9f)*(dancing?.17f:.035f)\n'
  '                    +(walking?.045f*Math.abs((float)Math.sin(t*9f+i)):0f);'),
 ('                box(x-.13f,.23f+bob,z,.15f,.36f,.20f,.20f,.19f,.33f);\n'
  '                box(x+.13f,.23f+bob,z,.15f,.36f,.20f,.20f,.19f,.33f);',
  '                float leftLift=walking?KingAvatarAnimation9932.lift(t,i):0f;\n'
  '                float rightLift=walking?KingAvatarAnimation9932.lift(t,i+1):0f;\n'
  '                box(x-.13f,.23f+bob+leftLift,z,.15f,.36f,.20f,.20f,.19f,.33f);\n'
  '                box(x+.13f,.23f+bob+rightLift,z,.15f,.36f,.20f,.20f,.19f,.33f);'),
 ('                sphere9928(x-.13f,.07f+bob,z+.08f,.14f,.075f,.19f,.13f,.13f,.18f);\n'
  '                sphere9928(x+.13f,.07f+bob,z+.08f,.14f,.075f,.19f,.13f,.13f,.18f);',
  '                sphere9928(x-.13f,.07f+bob+leftLift,z+.08f,.14f,.075f,.19f,.13f,.13f,.18f);\n'
  '                sphere9928(x+.13f,.07f+bob+rightLift,z+.08f,.14f,.075f,.19f,.13f,.13f,.18f);')
])
patchfile('PartyActivity.java',[
 ('root.addView(soundToggle993,new LinearLayout.LayoutParams(-1,dp(34)));',
  'root.addView(soundToggle993,new LinearLayout.LayoutParams(-1,dp(34)));\n'
  '        TextView preview9932=pill("▶ Hear selected gift sound • FREE PREVIEW",0xff36305c,\n'
  '            ()->KingGiftSound993.play(this,giftNames[giftSelectedIndex]));\n'
  '        preview9932.setTextSize(11);\n'
  '        root.addView(preview9932,new LinearLayout.LayoutParams(-1,dp(36)));'),
 ('if("VIP".equals(category)&&prefs.getInt("vip_level",0)<1)',
  'if("VIP".equals(category)&&!KingPremiumAccess981.PAID_VIP_ACTIVE)'),
 ('private void simpleRaceEvent(){\n'
  '        String[] cars={"🏎️ Red","🚙 Blue","🚗 Gold","🏁 KING"};String win=cars[new java.util.Random().nextInt(cars.length)];addEvent("play",safeName()+" started Forza Motorsport • winner "+win);\n'
  '        new AlertDialog.Builder(this).setTitle("🏎️ Forza Motorsport").setMessage("Winner: "+win).setPositiveButton("Race again",(d,w)->simpleRaceEvent()).setNegativeButton("Close",null).show();\n'
  '    }',
  '''private void simpleRaceEvent(){
        // An actual timed touch game, not the old random winner message.
        // This is a FREE local skill challenge whose result is visible in room events.
        final long[] started={0L};
        final int[] taps={0};
        final boolean[] finished={false};
        final android.os.Handler clock=new android.os.Handler(android.os.Looper.getMainLooper());
        final Runnable[] finish={null};
        LinearLayout panel=new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(18),dp(16),dp(18),dp(20));
        panel.setBackgroundColor(0xff1d1835);
        TextView status=tv("Tap START, then race for 8 seconds!",18,Color.WHITE,true);
        status.setGravity(Gravity.CENTER);
        panel.addView(status,new LinearLayout.LayoutParams(-1,dp(84)));
        TextView tap=tv("🏎️ START RACE",24,0xffffdd60,true);
        tap.setGravity(Gravity.CENTER);
        tap.setBackground(bg(0xff423078,20));
        panel.addView(tap,new LinearLayout.LayoutParams(-1,dp(130)));
        final AlertDialog dialog=new AlertDialog.Builder(this)
            .setTitle("🏎️ KING Tap Sprint • FREE")
            .setView(panel).setNegativeButton("Close",null).create();
        finish[0]=()->{
            if(finished[0]||!dialog.isShowing())return;
            finished[0]=true;
            long elapsed=android.os.SystemClock.elapsedRealtime()-started[0];
            int score=KingTapRace9932.score(taps[0],elapsed);
            tap.setText("🏁 "+score+" points");
            status.setText("Finished • "+taps[0]+" taps in 8 seconds\\nNo Coins or Diamonds awarded");
            addEvent("play",safeName()+" finished KING Tap Sprint • "+score+" skill points (FREE)");
        };
        tap.setOnClickListener(v->{
            if(finished[0])return;
            if(started[0]==0){
                started[0]=android.os.SystemClock.elapsedRealtime();
                clock.postDelayed(finish[0],KingTapRace9932.DURATION_MS);
            }
            long elapsed=android.os.SystemClock.elapsedRealtime()-started[0];
            if(elapsed>=KingTapRace9932.DURATION_MS){finish[0].run();return;}
            taps[0]++;
            tap.setText("🏎️ TAP FASTER! • "+taps[0]);
            status.setText("⏱ "+Math.max(1,(KingTapRace9932.DURATION_MS-elapsed+999)/1000)+"s remaining");
        });
        dialog.setOnDismissListener(d->clock.removeCallbacks(finish[0]));
        dialog.show();
    }'''),
 ('if(name.contains("Mobile Legends")||name.contains("PUBG")||name.contains("Free Fire")){openRoomGames740();return;}',
  'if(name.contains("Mobile Legends")||name.contains("PUBG")||name.contains("Free Fire")){\n'
  '            new AlertDialog.Builder(this).setTitle("Game not embedded")\n'
  '                .setMessage("This third-party title is not inside KING Plus. Choose one of the KING games that is actually playable in this Party Room.")\n'
  '                .setPositiveButton("Open KING Room Games",(d,w)->openRoomGames740())\n'
  '                .setNegativeButton("Close",null).show();return;}'),
 ('new String[]{"Relationship","Crazy Fruits","Chicken Run","Animal PK","Magic Lamp","Forza Motorsport","Candy Legend","Lucky Slot"}',
  'new String[]{"Relationship","Crazy Fruits","Chicken Run","Animal PK","Magic Lamp","KING Tap Sprint","Candy Legend","Lucky Slot"}'),
 ('if("Forza Motorsport".equals(name)){simpleRaceEvent();return;}',
  'if("KING Tap Sprint".equals(name)||"Forza Motorsport".equals(name)){simpleRaceEvent();return;}')
])
(src/'KingAvatarAnimation9932.java').write_text("""package com.kingplus.social;
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
""")
(src/'KingTapRace9932.java').write_text("""package com.kingplus.social;
/** Local free challenge. No paid rewards; results are untrusted client display only. */
public final class KingTapRace9932 {
  public static final long DURATION_MS=8000L;
  private KingTapRace9932(){}
  public static int score(int taps,long elapsedMs){
    if(elapsedMs<DURATION_MS)return 0;
    return Math.min(500,Math.max(0,taps))*2;
  }
}
""")
gradle=root/'app/build.gradle'; g=gradle.read_text()
old="versionCode 210; versionName '9.9.31-video-room-entrance-test'"
assert g.count(old)==1, 'Must start from exact v9.9.31 full release'
gradle.write_text(g.replace(old,"versionCode 211; versionName '9.9.32-room-3d-gift-games-test'",1))
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
assert 'KingGiftSound993.play(this,gift)' in (src/'PartyActivity.java').read_text()
print('PASS v9.9.32: queued gift audio, sound preview, verified VIP lock, smooth walking, real tap race; paid billing OFF')
