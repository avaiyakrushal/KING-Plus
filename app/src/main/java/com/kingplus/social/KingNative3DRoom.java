package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.widget.HorizontalScrollView;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.FieldValue;
import java.util.HashMap;
import java.util.Map;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * ORIGINAL KING Plus GLES 2.0 3D Party room: real depth/lighting and camera orbit.
 * Realtime 3D seat occupancy is read from the *existing* authenticated Firebase Room
 * collection; no fake users, no client auth/role changes, no copied models/assets.
 * The 3D scene is a first-stage geometric room, not a fully rigged social avatar world.
 */
public final class KingNative3DRoom {
    private KingNative3DRoom(){}

    public static void open(Activity host,FirebaseFirestore db,FirebaseUser user,
                            String roomId,Runnable openExistingVideo){
        if(host==null||db==null||user==null||roomId==null||roomId.isEmpty()
                ||host.isFinishing()||host.isDestroyed())return;
        final int d=(int)(host.getResources().getDisplayMetrics().density+.5f);
        final LinearLayout content=new LinearLayout(host);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(0xff0a1024);

        final FrameLayout stage=new FrameLayout(host);
        final WorldSurface surface=new WorldSurface(host);
        stage.addView(surface,new FrameLayout.LayoutParams(-1,-1));
        final TextView heading=new TextView(host);
        heading.setText("KING Plus • Native 3D Party");
        heading.setTextSize(16);heading.setTextColor(Color.WHITE);
        heading.setGravity(Gravity.CENTER_HORIZONTAL);
        heading.setShadowLayer(5,1,2,Color.BLACK);
        FrameLayout.LayoutParams hp=new FrameLayout.LayoutParams(-1,48*d,Gravity.TOP);
        stage.addView(heading,hp);
        // Prior fixed 360dp room + controls could clip the bottom of the dialog
        // on lower-height Android devices. Fit the stage and allow scrolling.
        final float roomDensity=Math.max(1f,host.getResources().getDisplayMetrics().density);
        final int availableDp=(int)(host.getResources().getDisplayMetrics().heightPixels/roomDensity);
        final int stageHeightDp=Math.max(300,Math.min(620,availableDp-220));
        content.addView(stage,new LinearLayout.LayoutParams(-1,stageHeightDp*d));

        final TextView status=new TextView(host);
        status.setText("Connecting real room seats to 3D World…");
        status.setTextColor(0xffb8f7ee);status.setTextSize(12);
        status.setPadding(12*d,6*d,12*d,6*d);status.setMaxLines(4);
        content.addView(status,new LinearLayout.LayoutParams(-1,68*d));
        // Tappable actual Firebase participants, never sample/demo identities.
        final String[] liveUids=new String[12],liveNames=new String[12];
        final TextView[] seatButtons=new TextView[12];
        HorizontalScrollView seatScroll=new HorizontalScrollView(host);
        seatScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout seatRow=new LinearLayout(host);
        seatRow.setPadding(8*d,4*d,8*d,4*d);
        for(int n=0;n<12;n++){
            final int index=n;
            TextView seat=new TextView(host);
            seat.setText((n+1)+"  •  Empty");
            seat.setTextSize(11);seat.setTextColor(0xffb3acd2);
            seat.setGravity(Gravity.CENTER);
            seat.setPadding(8*d,4*d,8*d,4*d);
            seat.setBackgroundColor(0xff242044);
            seat.setContentDescription("Seat "+(n+1)+" empty");
            seat.setOnClickListener(v->{
                if(host.isFinishing()||host.isDestroyed())return;
                String uid=liveUids[index];
                if(uid==null||uid.isEmpty())return;
                Intent intent=new Intent(host,KingPublicProfileActivity.class);
                intent.putExtra("uid",uid);
                intent.putExtra("name",liveNames[index]);
                intent.putExtra("roomId",roomId);
                intent.putExtra("roomRole","MEMBER");
                host.startActivity(intent);
            });
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(106*d,42*d);
            lp.setMargins(3*d,0,3*d,0);seatRow.addView(seat,lp);
            seatButtons[n]=seat;
        }
        seatScroll.addView(seatRow);
        content.addView(seatScroll,new LinearLayout.LayoutParams(-1,52*d));
        final Map<String,KingAvatarMotion9929.Pose> poses9929=new HashMap<>();
        final ListenerRegistration[] motionWatcher9929={null};
        final boolean[] writing9929={false};
        final LinearLayout actionRow9929=new LinearLayout(host);
        actionRow9929.setGravity(Gravity.CENTER);
        final String[] actionNames9929={"◀","▲","▼","▶","💃 Dance"};
        final int[] dx9929={-1,0,0,1,0};
        final int[] dz9929={0,-1,1,0,0};
        for(int a=0;a<actionNames9929.length;a++){
            final int index=a;
            TextView key=new TextView(host);
            key.setText(actionNames9929[a]);key.setGravity(Gravity.CENTER);
            key.setTextSize(a==4?12:20);key.setTextColor(Color.WHITE);
            key.setBackgroundColor(a==4?0xff6130a1:0xff303e6a);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,48*d,a==4?1.4f:1f);
            lp.setMargins(2*d,0,2*d,0);actionRow9929.addView(key,lp);
            key.setOnClickListener(v->{
                if(writing9929[0]||host.isFinishing()||host.isDestroyed())return;
                String uid=user.getUid();int seated=-1;
                for(int n=0;n<12;n++)if(uid.equals(liveUids[n])){seated=n;break;}
                if(seated<0){status.setText("Join a real Party seat before moving your avatar");return;}
                KingAvatarMotion9929.Pose current=poses9929.get(uid);
                if(current==null)current=new KingAvatarMotion9929.Pose(
                    (seated%6-2.5f)*1.60f,seated/6==0?-1.55f:1.22f,"idle");
                KingAvatarMotion9929.Pose next=index==4?current.dance():
                    current.move(dx9929[index],dz9929[index]);
                Map<String,Object> data=new HashMap<>();
                data.put("uid",uid);
                data.put("x",(double)next.x);data.put("z",(double)next.z);
                data.put("action",next.action);
                data.put("updatedAt",FieldValue.serverTimestamp());
                writing9929[0]=true;
                db.collection("live_rooms").document(roomId).collection("avatar_motion")
                  .document(uid).set(data).addOnCompleteListener(t->{
                    writing9929[0]=false;
                    if(host.isFinishing()||host.isDestroyed())return;
                    status.setText(t.isSuccessful()?
                        "🌐 Live Avatar "+("dance".equals(next.action)?"dancing":"moved")+
                        " • changes sent to other room members":
                        "3D movement unavailable • Firebase rules deployment may be required");
                  });
            });
        }
        content.addView(actionRow9929,new LinearLayout.LayoutParams(-1,55*d));
        final TextView hint=new TextView(host);
        hint.setText("Use arrows to walk • Dance • Drag to orbit • Pinch to zoom");
        hint.setTextColor(0xffbfb9d1);hint.setTextSize(11);
        hint.setGravity(Gravity.CENTER);hint.setPadding(6*d,3*d,6*d,8*d);
        content.addView(hint,new LinearLayout.LayoutParams(-1,45*d));

        final TextView video=new TextView(host);
        video.setText("📹 Open Multi Video");video.setTextColor(Color.WHITE);
        video.setTextSize(15);video.setGravity(Gravity.CENTER);
        video.setBackgroundColor(0xff5137a1);
        LinearLayout.LayoutParams vp=new LinearLayout.LayoutParams(-1,51*d);
        vp.setMargins(15*d,0,15*d,10*d);content.addView(video,vp);

        final ScrollView responsiveWorld9915=new ScrollView(host);
        responsiveWorld9915.setFillViewport(false);
        responsiveWorld9915.setVerticalScrollBarEnabled(false);
        responsiveWorld9915.addView(content,new ScrollView.LayoutParams(-1,-2));
        final AlertDialog dialog=new AlertDialog.Builder(host)
                .setTitle("🌐 KING 3D Avatar World • Live Seats")
                .setView(responsiveWorld9915)
                .setNegativeButton("Close",null).create();
        final ListenerRegistration[] listener={null};
        final Handler frames=new Handler(Looper.getMainLooper());
        final boolean[] active={true};
        final Runnable[] draw={null};
        draw[0]=()->{
            if(!active[0])return;
            if(dialog.isShowing()&&!host.isFinishing()&&!host.isDestroyed()){
                surface.requestRender();
                frames.postDelayed(draw[0],90L); // <=12 FPS: old/low-RAM phones
            }
        };
        video.setOnClickListener(v->{
            dialog.dismiss();
            if(openExistingVideo!=null&&!host.isFinishing()&&!host.isDestroyed())
                openExistingVideo.run();
        });
        dialog.setOnShowListener(v->{
            android.view.Window win=dialog.getWindow();
            if(win!=null)win.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                (int)(host.getResources().getDisplayMetrics().heightPixels*.94f));
            surface.onResume();
            frames.post(draw[0]);
        });
        dialog.setOnDismissListener(v->{
            active[0]=false;
            frames.removeCallbacks(draw[0]);
            if(listener[0]!=null){listener[0].remove();listener[0]=null;}
            if(motionWatcher9929[0]!=null){motionWatcher9929[0].remove();motionWatcher9929[0]=null;}
            surface.onPause();
        });
        dialog.show();
        motionWatcher9929[0]=db.collection("live_rooms").document(roomId)
            .collection("avatar_motion").addSnapshotListener((snap,error)->{
                if(!active[0]||host.isFinishing()||host.isDestroyed())return;
                if(error!=null){
                    status.setText("Avatar movement offline: "+error.getMessage());
                    return;
                }
                poses9929.clear();
                if(snap!=null)for(DocumentSnapshot doc:snap.getDocuments()){
                    String id=doc.getString("uid"),action=doc.getString("action");
                    Double x=doc.getDouble("x"),z=doc.getDouble("z");
                    if(id==null||!id.equals(doc.getId())||x==null||z==null||
                        !KingAvatarMotion9929.actionAllowed(action))continue;
                    try{poses9929.put(id,new KingAvatarMotion9929.Pose(x.floatValue(),z.floatValue(),action));}
                    catch(IllegalArgumentException ignored){}
                }
                surface.updateMotions(liveUids,poses9929);
            });
        listener[0]=db.collection("live_rooms").document(roomId).collection("seats")
            .addSnapshotListener((snapshot,error)->{
                if(!active[0]||host.isFinishing()||host.isDestroyed())return;
                if(error!=null){
                    status.setText("Firebase seats unavailable: "+
                        (error.getLocalizedMessage()==null?"Permission/network error":
                         error.getLocalizedMessage())+
                        "\nThis screen never substitutes fake room members.");
                    surface.updateOccupants(new boolean[12],new String[12]);
                    for(int i=0;i<12;i++){
                        liveUids[i]=null;liveNames[i]=null;
                        seatButtons[i].setText((i+1)+"  •  Offline");
                        seatButtons[i].setContentDescription("Seat "+(i+1)+" unavailable");
                    }
                    return;
                }
                final boolean[] occupants=new boolean[12];
                final String[] names=new String[12];
                final String[] uids=new String[12];
                int count=0;
                if(snapshot!=null)for(DocumentSnapshot doc:snapshot.getDocuments()){
                    Long n=doc.getLong("seatNo");
                    int seat=0;
                    if(n!=null)seat=n.intValue();
                    if(seat<1||seat>12){
                        try{seat=Integer.parseInt(doc.getId());}
                        catch(NumberFormatException ignored){}
                    }
                    if(seat<1||seat>12)continue;
                    String uid=doc.getString("uid");
                    if(uid==null||uid.isEmpty())continue;
                    occupants[seat-1]=true;
                    uids[seat-1]=uid;
                    String name=doc.getString("name");
                    names[seat-1]=(name==null||name.trim().isEmpty())?"KING User":name;
                    count++;
                }
                surface.updateOccupants(occupants,uids);
                for(int i=0;i<12;i++){
                    liveUids[i]=uids[i];liveNames[i]=names[i];
                    if(uids[i]!=null){
                        String shortName=names[i].length()>10?names[i].substring(0,10)+"…":names[i];
                        seatButtons[i].setText("● "+(i+1)+" "+shortName);
                        seatButtons[i].setTextColor(Color.WHITE);
                        seatButtons[i].setContentDescription("Seat "+(i+1)+" "+names[i]+" view profile");
                    }else{
                        seatButtons[i].setText((i+1)+"  •  Empty");
                        seatButtons[i].setTextColor(0xffb3acd2);
                        seatButtons[i].setContentDescription("Seat "+(i+1)+" empty");
                    }
                }
                surface.updateMotions(liveUids,poses9929);
                StringBuilder namesLine=new StringBuilder();
                for(int i=0;i<12;i++)if(occupants[i]){
                    if(namesLine.length()>0)namesLine.append(" · ");
                    namesLine.append("#").append(i+1).append(" ")
                            .append(names[i].length()>12?names[i].substring(0,12):names[i]);
                }
                status.setText("🟢 Firebase synced • "+count+
                    " occupied seat"+(count==1?"":"s")+
                    "\n"+(count==0?"No one is currently seated.":namesLine.toString()));
            });
    }

    private static final class WorldSurface extends GLSurfaceView {
        final RoomRenderer scene;
        float lastX,oldDistance=-1;
        WorldSurface(Activity activity){
            super(activity);
            setEGLContextClientVersion(2);
            setEGLConfigChooser(8,8,8,8,16,0);
            setPreserveEGLContextOnPause(false);
            scene=new RoomRenderer();
            setRenderer(scene);
            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        }
        void updateOccupants(boolean[] seats,String[] uids){
            scene.occupied=Arrays.copyOf(seats,12);
            scene.occupantUids=Arrays.copyOf(uids,12);
            requestRender();
        }
        void updateMotions(String[] uids,Map<String,KingAvatarMotion9929.Pose> poses){
            float[] xs=new float[12],zs=new float[12];boolean[] dances=new boolean[12];
            java.util.Arrays.fill(xs,Float.NaN);
            java.util.Arrays.fill(zs,Float.NaN);
            for(int i=0;i<12;i++){
                KingAvatarMotion9929.Pose p=poses.get(uids[i]);
                if(p!=null){xs[i]=p.x;zs[i]=p.z;dances[i]="dance".equals(p.action);}
            }
            scene.motionX=xs;scene.motionZ=zs;scene.motionDance=dances;
            requestRender();
        }
        @Override public boolean onTouchEvent(MotionEvent event){
            switch(event.getActionMasked()){
                case MotionEvent.ACTION_DOWN:
                    lastX=event.getX();oldDistance=-1;return true;
                case MotionEvent.ACTION_POINTER_DOWN:
                    if(event.getPointerCount()>1)
                        oldDistance=(float)Math.hypot(event.getX(0)-event.getX(1),
                                                       event.getY(0)-event.getY(1));
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if(event.getPointerCount()>1){
                        float distance=(float)Math.hypot(event.getX(0)-event.getX(1),
                                                        event.getY(0)-event.getY(1));
                        if(oldDistance>0)scene.radius=Math.max(8,Math.min(25,
                            scene.radius+(oldDistance-distance)*.032f));
                        oldDistance=distance;
                    }else{
                        scene.angle+=(event.getX()-lastX)*.009f;
                        lastX=event.getX();
                    }
                    requestRender();return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    oldDistance=-1;return true;
                default:return true;
            }
        }
    }

    private static final class RoomRenderer implements GLSurfaceView.Renderer {
        volatile boolean[] occupied=new boolean[12];
        volatile String[] occupantUids=new String[12];
        volatile float[] motionX=new float[12],motionZ=new float[12];
        volatile boolean[] motionDance=new boolean[12];
        // Renderer-owned positions: smooth snapshots without extra Firebase writes.
        final float[] visualX9932=new float[12], visualZ9932=new float[12];
        final String[] visualUid9932=new String[12];
        volatile float angle=0.35f,radius=15f;
        final float[] projection=new float[16],view=new float[16],
            model=new float[16],mv=new float[16],mvp=new float[16];
        FloatBuffer vertices,normals,sphereVertices,sphereNormals;
        int program,aPosition,aNormal,uMvp,uModel,uTint;

        // 6 cube faces, two triangles each. Colors are per-object and shaded
        // using per-face normals in the original GLES20 shader.
        static final float[] CUBE={
          // front
          -.5f,-.5f,.5f, .5f,-.5f,.5f, .5f,.5f,.5f,
          -.5f,-.5f,.5f, .5f,.5f,.5f, -.5f,.5f,.5f,
          // back
          .5f,-.5f,-.5f, -.5f,-.5f,-.5f, -.5f,.5f,-.5f,
          .5f,-.5f,-.5f, -.5f,.5f,-.5f, .5f,.5f,-.5f,
          // left
          -.5f,-.5f,-.5f, -.5f,-.5f,.5f, -.5f,.5f,.5f,
          -.5f,-.5f,-.5f, -.5f,.5f,.5f, -.5f,.5f,-.5f,
          // right
          .5f,-.5f,.5f, .5f,-.5f,-.5f, .5f,.5f,-.5f,
          .5f,-.5f,.5f, .5f,.5f,-.5f, .5f,.5f,.5f,
          // top
          -.5f,.5f,.5f, .5f,.5f,.5f, .5f,.5f,-.5f,
          -.5f,.5f,.5f, .5f,.5f,-.5f, -.5f,.5f,-.5f,
          // bottom
          -.5f,-.5f,-.5f, .5f,-.5f,-.5f, .5f,-.5f,.5f,
          -.5f,-.5f,-.5f, .5f,-.5f,.5f, -.5f,-.5f,.5f
        };
        @Override public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 gl,
                                               javax.microedition.khronos.egl.EGLConfig cfg){
            GLES20.glClearColor(.023f,.035f,.11f,1f);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glDisable(GLES20.GL_CULL_FACE);
            vertices=buffer(CUBE);
            float[] n=new float[CUBE.length];
            float[][] faces={{0,0,1},{0,0,-1},{-1,0,0},{1,0,0},{0,1,0},{0,-1,0}};
            for(int face=0;face<6;face++)for(int v=0;v<6;v++)
                for(int axis=0;axis<3;axis++)
                    n[(face*6+v)*3+axis]=faces[face][axis];
            normals=buffer(n);
            float[] globe=makeSphere9928(14,20);
            sphereVertices=buffer(globe);
            sphereNormals=buffer(globe);
            String vertex="attribute vec3 aPosition;attribute vec3 aNormal;"+
              "uniform mat4 uMvp;uniform mat4 uModel;varying vec3 vNormal;"+
              "void main(){gl_Position=uMvp*vec4(aPosition,1.);"+
              "vNormal=mat3(uModel)*aNormal;}";
            String fragment="precision mediump float;uniform vec4 uTint;varying vec3 vNormal;"+
              "void main(){float d=max(dot(normalize(vNormal),"+
              "normalize(vec3(-0.4,0.85,0.45))),0.);"+
              "gl_FragColor=vec4(uTint.rgb*(0.54+0.46*d),uTint.a);}";
            program=GLES20.glCreateProgram();
            GLES20.glAttachShader(program,shader(GLES20.GL_VERTEX_SHADER,vertex));
            GLES20.glAttachShader(program,shader(GLES20.GL_FRAGMENT_SHADER,fragment));
            GLES20.glLinkProgram(program);
            int[] linked=new int[1];
            GLES20.glGetProgramiv(program,GLES20.GL_LINK_STATUS,linked,0);
            if(linked[0]!=GLES20.GL_TRUE)
                throw new IllegalStateException("KING 3D shader link error: "+
                    GLES20.glGetProgramInfoLog(program));
            aPosition=GLES20.glGetAttribLocation(program,"aPosition");
            aNormal=GLES20.glGetAttribLocation(program,"aNormal");
            uMvp=GLES20.glGetUniformLocation(program,"uMvp");
            uModel=GLES20.glGetUniformLocation(program,"uModel");
            uTint=GLES20.glGetUniformLocation(program,"uTint");
        }
        private static FloatBuffer buffer(float[] data){
            FloatBuffer b=ByteBuffer.allocateDirect(data.length*4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
            b.put(data);b.position(0);return b;
        }
        private static int shader(int type,String code){
            int s=GLES20.glCreateShader(type);
            GLES20.glShaderSource(s,code);GLES20.glCompileShader(s);
            int[] ok=new int[1];GLES20.glGetShaderiv(s,GLES20.GL_COMPILE_STATUS,ok,0);
            if(ok[0]!=GLES20.GL_TRUE)
                throw new IllegalStateException("KING 3D shader compile: "+
                    GLES20.glGetShaderInfoLog(s));
            return s;
        }
        @Override public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 gl,int w,int h){
            GLES20.glViewport(0,0,w,h);
            Matrix.perspectiveM(projection,0,50f,Math.max(1,w)/(float)Math.max(1,h),.1f,80f);
        }
        private void box(float x,float y,float z,float sx,float sy,float sz,
                         float r,float g,float b){
            Matrix.setIdentityM(model,0);
            Matrix.translateM(model,0,x,y,z);
            Matrix.scaleM(model,0,sx,sy,sz);
            Matrix.multiplyMM(mv,0,view,0,model,0);
            Matrix.multiplyMM(mvp,0,projection,0,mv,0);
            GLES20.glUniformMatrix4fv(uMvp,1,false,mvp,0);
            GLES20.glUniformMatrix4fv(uModel,1,false,model,0);
            GLES20.glUniform4f(uTint,r,g,b,1f);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,36);
        }
        // Procedural low-poly sphere, stored once on the GPU-facing buffers.
        // Head/hair/hands are independently rendered without any copied assets.
        private static float[] makeSphere9928(int bands,int slices){
            float[] mesh=new float[bands*slices*18];int p=0;
            for(int y=0;y<bands;y++)for(int x=0;x<slices;x++){
                float t0=(float)Math.PI*y/bands,t1=(float)Math.PI*(y+1)/bands;
                float a0=(float)(2*Math.PI)*x/slices,a1=(float)(2*Math.PI)*(x+1)/slices;
                float[] tl=point9928(t0,a0),tr=point9928(t0,a1),
                    bl=point9928(t1,a0),br=point9928(t1,a1);
                for(float[] v:new float[][]{tl,bl,br,tl,br,tr})
                    for(float axis:v)mesh[p++]=axis;
            }
            return mesh;
        }
        private static float[] point9928(float theta,float phi){
            return new float[]{(float)(Math.sin(theta)*Math.cos(phi)),
                (float)Math.cos(theta),(float)(Math.sin(theta)*Math.sin(phi))};
        }
        private void sphere9928(float x,float y,float z,float sx,float sy,float sz,
                                float r,float g,float b){
            Matrix.setIdentityM(model,0);Matrix.translateM(model,0,x,y,z);
            Matrix.scaleM(model,0,sx,sy,sz);
            Matrix.multiplyMM(mv,0,view,0,model,0);
            Matrix.multiplyMM(mvp,0,projection,0,mv,0);
            GLES20.glUniformMatrix4fv(uMvp,1,false,mvp,0);
            GLES20.glUniformMatrix4fv(uModel,1,false,model,0);
            GLES20.glUniform4f(uTint,r,g,b,1f);
            sphereVertices.position(0);sphereNormals.position(0);
            GLES20.glVertexAttribPointer(aPosition,3,GLES20.GL_FLOAT,false,0,sphereVertices);
            GLES20.glVertexAttribPointer(aNormal,3,GLES20.GL_FLOAT,false,0,sphereNormals);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,sphereVertices.capacity()/3);
            vertices.position(0);normals.position(0);
            GLES20.glVertexAttribPointer(aPosition,3,GLES20.GL_FLOAT,false,0,vertices);
            GLES20.glVertexAttribPointer(aNormal,3,GLES20.GL_FLOAT,false,0,normals);
        }
        @Override public void onDrawFrame(javax.microedition.khronos.opengles.GL10 gl){
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
            float a=angle,dist=radius;
            Matrix.setLookAtM(view,0,(float)Math.sin(a)*dist,6.4f,
                (float)Math.cos(a)*dist,0,0,-.6f,0,1,0);
            GLES20.glUseProgram(program);
            vertices.position(0);normals.position(0);
            GLES20.glEnableVertexAttribArray(aPosition);
            GLES20.glEnableVertexAttribArray(aNormal);
            GLES20.glVertexAttribPointer(aPosition,3,GLES20.GL_FLOAT,false,0,vertices);
            GLES20.glVertexAttribPointer(aNormal,3,GLES20.GL_FLOAT,false,0,normals);

            // Original royal stage, actual 3D walls/floor, glowing trim.
            box(0,-.12f,0,11,.2f,8,.10f,.18f,.34f);
            box(0,-.07f,-2.2f,10,.16f,2.3f,.20f,.13f,.45f);
            box(0,2.6f,-4.05f,11,5.4f,.20f,.11f,.08f,.25f);
            box(-5.2f,2f,-3.4f,.22f,4,.9f,.20f,.24f,.55f);
            box(5.2f,2f,-3.4f,.22f,4,.9f,.20f,.24f,.55f);
            box(0,4.0f,-3.8f,8,.18f,.30f,.88f,.68f,.29f);
            box(0,1.4f,-3.8f,5.7f,.10f,.20f,.18f,.77f,.91f);
            box(0,0.10f,-3.25f,3.0f,.20f,1.0f,.34f,.31f,.65f);
            box(0,1.3f,-3.60f,.8f,1.9f,.6f,.66f,.39f,.92f);
            box(0,2.35f,-3.6f,1.15f,.35f,.8f,.90f,.73f,.31f);

            boolean[] live=occupied; // immutable copy, set from Firestore UI thread
            String[] people=occupantUids;
            float t=(android.os.SystemClock.uptimeMillis()%100000)/1000f;
            for(int i=0;i<12;i++){
                int row=i/6,col=i%6;
                float seatX=(col-2.5f)*1.60f,seatZ=row==0?-1.55f:1.22f;
                box(seatX,.05f,seatZ,1.16f,.13f,.9f,.24f,.25f,.48f);
                box(seatX,.1f,seatZ-.36f,1.12f,.08f,.06f,.75f,.62f,.23f);
                if(i>=live.length||!live[i])continue;
                float goalX=i<motionX.length&&!Float.isNaN(motionX[i])?motionX[i]:seatX;
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
                // Stable UID-linked procedural outfit: same character palette on both phones.
                int stable=uid==null?i:uid.hashCode();
                int style=(stable&0x7fffffff)%6;
                float[] reds={.82f,.30f,.81f,.40f,.93f,.29f};
                float[] greens={.36f,.76f,.53f,.51f,.72f,.60f};
                float[] blues={.72f,.88f,.35f,.91f,.37f,.75f};
                float bob=(float)Math.sin(t*(dancing?6f:1.5f)+i*.9f)*(dancing?.17f:.035f)
                    +(walking?.045f*Math.abs((float)Math.sin(t*9f+i)):0f);
                float cr=reds[style],cg=greens[style],cb=blues[style];
                // Actual articulated, rounded KING characters rather than cube-only
                // placeholders. Deterministic UID palette matches on both phones.
                float wave=(float)Math.sin(t*(dancing?8f:2f)+i)*(dancing?.29f:.12f);
                float skin=((stable>>>5)&1)==0?.94f:.69f;
                float hair=((stable>>>7)&1)==0?.16f:.43f;
                float head=1.23f+bob;
                box(x,.67f+bob,z,.43f,.61f,.32f,cr,cg,cb); // jacket
                box(x,.96f+bob,z+.175f,.22f,.16f,.035f,.88f,.70f,.29f); // badge
                sphere9928(x,head,z,.27f,.30f,.25f,skin,skin*.76f,skin*.65f);
                sphere9928(x,head+.24f,z-.035f,.29f,.11f,.26f,hair,hair*.8f,hair*.70f);
                sphere9928(x-.11f,head+.04f,z+.235f,.023f,.036f,.019f,.07f,.08f,.12f);
                sphere9928(x+.11f,head+.04f,z+.235f,.023f,.036f,.019f,.07f,.08f,.12f);
                sphere9928(x,head-.045f,z+.258f,.038f,.041f,.025f,skin,skin*.69f,skin*.59f);
                box(x-.34f,.66f+bob+wave,z,.14f,.48f,.19f,cr,cg,cb);
                box(x+.34f,.66f+bob-wave,z,.14f,.48f,.19f,cr,cg,cb);
                sphere9928(x-.34f,.38f+bob+wave,z,.09f,.09f,.09f,skin,skin*.76f,skin*.65f);
                sphere9928(x+.34f,.38f+bob-wave,z,.09f,.09f,.09f,skin,skin*.76f,skin*.65f);
                float leftLift=walking?KingAvatarAnimation9932.lift(t,i):0f;
                float rightLift=walking?KingAvatarAnimation9932.lift(t,i+1):0f;
                box(x-.13f,.23f+bob+leftLift,z,.15f,.36f,.20f,.20f,.19f,.33f);
                box(x+.13f,.23f+bob+rightLift,z,.15f,.36f,.20f,.20f,.19f,.33f);
                sphere9928(x-.13f,.07f+bob+leftLift,z+.08f,.14f,.075f,.19f,.13f,.13f,.18f);
                sphere9928(x+.13f,.07f+bob+rightLift,z+.08f,.14f,.075f,.19f,.13f,.13f,.18f);
            }
            GLES20.glDisableVertexAttribArray(aPosition);
            GLES20.glDisableVertexAttribArray(aNormal);
        }
    }
}
