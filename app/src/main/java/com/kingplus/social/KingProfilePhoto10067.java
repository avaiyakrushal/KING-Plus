package com.kingplus.social;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

final class KingProfilePhoto10067 {
    // Decode and resize on a worker thread; strip source metadata before upload.
    static byte[] prepare(Context context,Uri uri) throws Exception {
        String mime=context.getContentResolver().getType(uri);
        if(mime!=null&&!mime.startsWith("image/"))throw new IllegalArgumentException("Choose an image.");
        byte[] source;
        try(InputStream input=context.getContentResolver().openInputStream(uri);
            ByteArrayOutputStream bytes=new ByteArrayOutputStream()){
            if(input==null)throw new IllegalArgumentException("Photo cannot be opened.");
            byte[] block=new byte[8192];int n,total=0;
            while((n=input.read(block))!=-1){total+=n;if(total>8*1024*1024)throw new IllegalArgumentException("Choose a photo smaller than 8 MB.");bytes.write(block,0,n);}
            source=bytes.toByteArray();
        }
        BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;
        BitmapFactory.decodeByteArray(source,0,source.length,options);
        if(options.outWidth<1||options.outHeight<1||options.outWidth>32768||options.outHeight>32768)
            throw new IllegalArgumentException("This photo format cannot be read.");
        options.inSampleSize=1;
        while(options.outWidth/options.inSampleSize>640||options.outHeight/options.inSampleSize>640)options.inSampleSize*=2;
        options.inJustDecodeBounds=false;options.inPreferredConfig=Bitmap.Config.ARGB_8888;
        Bitmap bitmap=BitmapFactory.decodeByteArray(source,0,source.length,options);
        if(bitmap==null)throw new IllegalArgumentException("Photo could not be decoded.");
        try {
            Matrix matrix=new Matrix();
            int orientation=ExifInterface.ORIENTATION_NORMAL;
            try{orientation=new ExifInterface(new ByteArrayInputStream(source)).getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);}catch(Exception ignored){}
            switch(orientation){
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:matrix.setScale(-1,1);break;
                case ExifInterface.ORIENTATION_ROTATE_180:matrix.setRotate(180);break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:matrix.setScale(1,-1);break;
                case ExifInterface.ORIENTATION_TRANSPOSE:matrix.setRotate(90);matrix.postScale(-1,1);break;
                case ExifInterface.ORIENTATION_ROTATE_90:matrix.setRotate(90);break;
                case ExifInterface.ORIENTATION_TRANSVERSE:matrix.setRotate(270);matrix.postScale(-1,1);break;
                case ExifInterface.ORIENTATION_ROTATE_270:matrix.setRotate(270);break;
            }
            if(!matrix.isIdentity()){
                Bitmap rotated=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
                if(rotated!=bitmap){bitmap.recycle();bitmap=rotated;}
            }
            try(ByteArrayOutputStream result=new ByteArrayOutputStream()){
                if(!bitmap.compress(Bitmap.CompressFormat.JPEG,85,result))throw new IllegalArgumentException("Photo could not be prepared.");
                return result.toByteArray();
            }
        }finally{bitmap.recycle();}
    }
}
