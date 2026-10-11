package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.TextView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
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
        content.addView(stage,new LinearLayout.LayoutParams(-1,360*d));

        final TextView status=new TextView(host);
        status.setText("Connecting real room seats to 3D World…");
        status.setTextColor(0xffb8f7ee);status.setTextSize(12);
        status.setPadding(12*d,6*d,12*d,6*d);status.setMaxLines(4);
        content.addView(status,new LinearLayout.LayoutParams(-1,68*d));
        final TextView hint=new TextView(host);
        hint.setText("Drag to rotate • Pinch to zoom • 3D seats reflect live Firebase data");
        hint.setTextColor(0xffbfb9d1);hint.setTextSize(11);
        hint.setGravity(Gravity.CENTER);hint.setPadding(6*d,3*d,6*d,8*d);
        content.addView(hint,new LinearLayout.LayoutParams(-1,45*d));

        final TextView video=new TextView(host);
        video.setText("📹 Open Multi Video");video.setTextColor(Color.WHITE);
        video.setTextSize(15);video.setGravity(Gravity.CENTER);
        video.setBackgroundColor(0xff5137a1);
        LinearLayout.LayoutParams vp=new LinearLayout.LayoutParams(-1,51*d);
        vp.setMargins(15*d,0,15*d,10*d);content.addView(video,vp);

        final AlertDialog dialog=new AlertDialog.Builder(host)
                .setTitle("🌐 3D World • Live Seats")
                .setView(content)
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
            surface.onResume();
            frames.post(draw[0]);
        });
        dialog.setOnDismissListener(v->{
            active[0]=false;
            frames.removeCallbacks(draw[0]);
            if(listener[0]!=null){listener[0].remove();listener[0]=null;}
            surface.onPause();
        });
        dialog.show();
        listener[0]=db.collection("live_rooms").document(roomId).collection("seats")
            .addSnapshotListener((snapshot,error)->{
                if(!active[0]||host.isFinishing()||host.isDestroyed())return;
                if(error!=null){
                    status.setText("Firebase seats unavailable: "+
                        (error.getLocalizedMessage()==null?"Permission/network error":
                         error.getLocalizedMessage())+
                        "\nThis screen never substitutes fake room members.");
                    surface.updateOccupants(new boolean[12]);
                    return;
                }
                final boolean[] occupants=new boolean[12];
                final String[] names=new String[12];
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
                    String name=doc.getString("name");
                    names[seat-1]=(name==null||name.trim().isEmpty())?"KING User":name;
                    count++;
                }
                surface.updateOccupants(occupants);
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
        void updateOccupants(boolean[] seats){
            scene.occupied=Arrays.copyOf(seats,12);
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
        volatile float angle=0.35f,radius=15f;
        final float[] projection=new float[16],view=new float[16],
            model=new float[16],mv=new float[16],mvp=new float[16];
        FloatBuffer vertices,normals;
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
            float t=(android.os.SystemClock.uptimeMillis()%100000)/1000f;
            for(int i=0;i<12;i++){
                int row=i/6,col=i%6;
                float x=(col-2.5f)*1.60f,z=row==0?-1.55f:1.22f;
                box(x,.05f,z,1.16f,.13f,.9f,.24f,.25f,.48f);
                box(x,.1f,z-.36f,1.12f,.08f,.06f,.75f,.62f,.23f);
                if(i>=live.length||!live[i])continue;
                float hue=i%5;
                float bob=(float)Math.sin(t*1.5+i*.9f)*.025f;
                float cr=.32f+.1f*hue,cg=.55f-.055f*hue,cb=.85f;
                box(x,.60f+bob,z,.45f,.72f,.32f,cr,cg,cb); // torso
                box(x,1.20f+bob,z,.34f,.34f,.32f,.97f,.73f,.55f); // head
                box(x-.33f,.66f+bob,z,.16f,.58f,.22f,cr,cg,cb);
                box(x+.33f,.66f+bob,z,.16f,.58f,.22f,cr,cg,cb);
                box(x-.13f,.20f+bob,z,.15f,.35f,.20f,.28f,.31f,.48f);
                box(x+.13f,.20f+bob,z,.15f,.35f,.20f,.28f,.31f,.48f);
                box(x,1.44f+bob,z,.43f,.075f,.43f,.86f,.69f,.30f);
            }
            GLES20.glDisableVertexAttribArray(aPosition);
            GLES20.glDisableVertexAttribArray(aNormal);
        }
    }
}
