package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Signed-in owner's cloud profile video manager. */
public final class KingProfileVideos10087Activity extends Activity {
    private static final int PICK_VIDEO=10087;
    private static final long MAX_BYTES=10L*1024L*1024L;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String ownerUid="";
    private String pickerUid="";
    private ListenerRegistration videosListener;
    private LinearLayout rows;
    private TextView status;
    private boolean uploading;

    private int dp(int value){return (int)(value*getResources().getDisplayMetrics().density+.5f);}
    private TextView label(String value,int size,int color,boolean bold){
        TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);
        if(bold)t.setTypeface(null,Typeface.BOLD);t.setPadding(dp(10),dp(8),dp(10),dp(8));return t;
    }
    private void toast(String value){if(!isFinishing())Toast.makeText(this,value,Toast.LENGTH_LONG).show();}

    @Override protected void onCreate(Bundle state){
        super.onCreate(state);
        try{auth=FirebaseAuth.getInstance();db=FirebaseFirestore.getInstance();}catch(Exception ignored){}
        FirebaseUser user=auth==null?null:auth.getCurrentUser();
        if(user!=null)ownerUid=user.getUid();
        LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(14),dp(10),dp(14),dp(24));page.setBackgroundColor(0xff11101a);
        TextView back=label("‹   My Cloud Profile Videos",21,Color.WHITE,true);
        back.setOnClickListener(v->finish());page.addView(back);
        page.addView(label("Upload videos to your KING Plus profile. Signed-in devices can see this account's videos. Maximum 10 MB each.",13,0xffc9c3d4,false));
        Button add=new Button(this);add.setText("＋ Upload Video");add.setOnClickListener(v->pickVideo());page.addView(add);
        status=label("",13,0xffc9c3d4,false);page.addView(status);
        rows=new LinearLayout(this);rows.setOrientation(LinearLayout.VERTICAL);page.addView(rows,new LinearLayout.LayoutParams(-1,-2));
        Button done=new Button(this);done.setText("Back");done.setOnClickListener(v->finish());page.addView(done);
        setContentView(page);
        if(ownerUid.isEmpty()){
            status.setText("Sign in to manage cloud profile videos.");
            add.setEnabled(false);
        }else renderVideos();
    }

    private void pickVideo(){
        FirebaseUser user=auth==null?null:auth.getCurrentUser();
        if(user==null||!ownerUid.equals(user.getUid())){toast("Sign in to the same account first");return;}
        if(uploading){toast("A video is already uploading");return;}
        pickerUid=ownerUid;
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent,PICK_VIDEO);
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode!=PICK_VIDEO||resultCode!=RESULT_OK||data==null||data.getData()==null)return;
        Uri uri=data.getData();
        try{getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);}
        catch(Exception e){toast("Video permission was not granted");return;}
        FirebaseUser user=auth==null?null:auth.getCurrentUser();
        if(user==null||!ownerUid.equals(pickerUid)||!pickerUid.equals(user.getUid())){
            toast("Account changed. Choose the video again.");return;
        }
        upload(uri,pickerUid);
    }

    private long measureBytes(Uri uri){
        try(android.content.res.AssetFileDescriptor fd=getContentResolver().openAssetFileDescriptor(uri,"r")){
            if(fd!=null&&fd.getLength()>0)return fd.getLength();
        }catch(Exception ignored){}
        long total=0;
        try(java.io.InputStream in=getContentResolver().openInputStream(uri)){
            if(in==null)return -1;
            byte[] buffer=new byte[8192];int count;
            while((count=in.read(buffer))!=-1){total+=count;if(total>MAX_BYTES)return total;}
            return total;
        }catch(Exception e){return -1;}
    }

    private void upload(Uri uri,String uid){
        FirebaseUser user=auth==null?null:auth.getCurrentUser();
        if(user==null||!uid.equals(user.getUid())||db==null)return;
        String mime=getContentResolver().getType(uri);
        if(mime==null||!mime.matches("video/[A-Za-z0-9.+-]+")){
            toast("Choose a supported video file");return;
        }
        long size=measureBytes(uri);
        if(size<=0||size>MAX_BYTES){toast("Choose a video no larger than 10 MB");return;}
        String id=UUID.randomUUID().toString();
        String path="chat_media/"+uid+"/profile_videos/"+id;
        StorageReference ref=FirebaseStorage.getInstance().getReference().child(path);
        StorageMetadata metadata=new StorageMetadata.Builder().setContentType(mime).build();
        uploading=true;status.setText("Uploading profile video…");
        ref.putFile(uri,metadata).continueWithTask(task->{
            if(!task.isSuccessful()){
                Exception error=task.getException();if(error!=null)throw error;
                throw new IllegalStateException("Storage upload failed");
            }
            return ref.getDownloadUrl();
        }).addOnSuccessListener(url->{
            FirebaseUser current=auth==null?null:auth.getCurrentUser();
            if(current==null||!uid.equals(current.getUid())){ref.delete();uploading=false;return;}
            Map<String,Object> item=new HashMap<>();
            item.put("uid",uid);item.put("url",url.toString());item.put("storagePath",path);
            item.put("fileName","Profile video");item.put("contentType",mime);item.put("sizeBytes",size);
            item.put("createdAt",FieldValue.serverTimestamp());
            db.collection("public_profiles").document(uid).collection("videos").document(id).set(item)
                .addOnSuccessListener(ok->{
                    uploading=false;if(isFinishing()||isDestroyed())return;
                    status.setText("✓ Video synced to your profile");toast("Profile video uploaded");
                }).addOnFailureListener(error->{
                    ref.delete();uploading=false;
                    if(!isFinishing()&&!isDestroyed())status.setText("Video record not saved. Check connection or deployed profile-video rules.");
                });
        }).addOnFailureListener(error->{
            uploading=false;if(!isFinishing()&&!isDestroyed())status.setText("Upload failed. Check Storage access and connection.");
        });
    }

    private void renderVideos(){
        if(videosListener!=null){videosListener.remove();videosListener=null;}
        rows.removeAllViews();status.setText("Loading your account videos…");
        videosListener=db.collection("public_profiles").document(ownerUid).collection("videos")
            .orderBy("createdAt",com.google.firebase.firestore.Query.Direction.DESCENDING).limit(40)
            .addSnapshotListener((snapshot,error)->{
                FirebaseUser user=auth==null?null:auth.getCurrentUser();
                if(isFinishing()||isDestroyed()||user==null||!ownerUid.equals(user.getUid()))return;
                if(error!=null||snapshot==null){status.setText("Cloud videos unavailable • tap to retry");
                    status.setOnClickListener(v->renderVideos());return;}
                rows.removeAllViews();
                if(snapshot.isEmpty()){status.setText("No uploaded profile videos yet.");return;}
                status.setText(snapshot.size()+" account videos • synced live");
                for(DocumentSnapshot doc:snapshot.getDocuments())addVideo(doc);
            });
    }

    private void addVideo(DocumentSnapshot doc){
        String name=doc.getString("fileName"),url=doc.getString("url"),path=doc.getString("storagePath");
        String uid=doc.getString("uid"),id=doc.getId();
        TextView title=label("🎬 "+(name==null?"Profile video":name),15,Color.WHITE,true);rows.addView(title);
        Button play=new Button(this);play.setText("▶ Play");
        play.setOnClickListener(v->{
            FirebaseUser user=auth==null?null:auth.getCurrentUser();
            if(user==null||!ownerUid.equals(user.getUid())||!ownerUid.equals(uid)||url==null||!url.startsWith("https://"))return;
            try{Intent view=new Intent(Intent.ACTION_VIEW);view.setDataAndType(Uri.parse(url),"video/*");startActivity(view);}
            catch(Exception e){toast("No video player is available");}
        });rows.addView(play);
        Button delete=new Button(this);delete.setText("Delete cloud video");
        delete.setOnClickListener(v->{
            FirebaseUser user=auth==null?null:auth.getCurrentUser();
            if(user==null||!ownerUid.equals(user.getUid())||!ownerUid.equals(uid)
                ||!path.equals("chat_media/"+ownerUid+"/profile_videos/"+id))return;
            new AlertDialog.Builder(this).setTitle("Delete this profile video?")
                .setMessage("It will be removed from your account on all devices.")
                .setNegativeButton("Cancel",null).setPositiveButton("Delete",(d,w)->{
                    FirebaseUser current=auth==null?null:auth.getCurrentUser();
                    if(current==null||!ownerUid.equals(current.getUid()))return;
                    delete.setEnabled(false);
                    FirebaseStorage.getInstance().getReference().child(path).delete()
                        .continueWithTask(task->{
                            if(!task.isSuccessful()){Exception e=task.getException();if(e!=null)throw e;throw new IllegalStateException("Storage delete failed");}
                            return doc.getReference().delete();
                        }).addOnFailureListener(e->{if(!isFinishing()&&!isDestroyed()&&delete.getParent()!=null){
                            delete.setEnabled(true);toast("Video could not be deleted");}});
                }).show();
        });rows.addView(delete);
    }

    @Override protected void onDestroy(){
        if(videosListener!=null){videosListener.remove();videosListener=null;}
        super.onDestroy();
    }
}
