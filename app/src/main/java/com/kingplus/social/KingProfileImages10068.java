package com.kingplus.social;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** Small profile thumbnails only. Never decode an unbounded network image. */
final class KingProfileImages10068 {
    private static final ThreadPoolExecutor workers=new ThreadPoolExecutor(2,2,20,TimeUnit.SECONDS,
        new ArrayBlockingQueue<Runnable>(80),new ThreadPoolExecutor.DiscardOldestPolicy());
    private static String account(){FirebaseUser u=FirebaseAuth.getInstance().getCurrentUser();return u==null?"":u.getUid();}
    static void load(Activity activity,ImageView image,TextView fallback,String url){
        final String source=url==null?"":url.trim(),owner=account();
        if(source.equals(image.getTag())&&image.getDrawable()!=null)return;
        image.setTag(source);image.setImageDrawable(null);fallback.setVisibility(View.VISIBLE);
        if(!source.startsWith("https://")||source.length()>4096)return;
        workers.execute(()->{
            Bitmap bitmap=null;HttpURLConnection connection=null;
            try{
                URL parsed=new URL(source);
                if(!"https".equalsIgnoreCase(parsed.getProtocol()))return;
                connection=(HttpURLConnection)parsed.openConnection();
                connection.setConnectTimeout(6500);connection.setReadTimeout(6500);
                connection.setInstanceFollowRedirects(false);
                if(connection.getResponseCode()!=200||connection.getContentLengthLong()>3*1024*1024)return;
                byte[] bytes;
                try(InputStream in=connection.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){
                    byte[] block=new byte[8192];int count,total=0;
                    while((count=in.read(block))!=-1){total+=count;if(total>3*1024*1024)return;out.write(block,0,count);}
                    bytes=out.toByteArray();
                }
                BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;
                BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
                if(options.outWidth<1||options.outHeight<1||options.outWidth>32768||options.outHeight>32768)return;
                options.inSampleSize=1;
                while(options.outWidth/options.inSampleSize>640||options.outHeight/options.inSampleSize>640)options.inSampleSize*=2;
                options.inJustDecodeBounds=false;
                bitmap=BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
                if(bitmap==null)return;
                final Bitmap ready=bitmap;
                activity.runOnUiThread(()->{
                    if(!activity.isFinishing()&&!activity.isDestroyed()&&image.isAttachedToWindow()
                        &&owner.equals(account())&&source.equals(image.getTag())){
                        image.setImageBitmap(ready);fallback.setVisibility(View.GONE);
                    }else ready.recycle();
                });
                bitmap=null;
            }catch(Exception|OutOfMemoryError ignored){
                // Keep the initial visible if a remote thumbnail cannot be loaded.
            }finally{if(bitmap!=null)bitmap.recycle();if(connection!=null)connection.disconnect();}
        });
    }
}
