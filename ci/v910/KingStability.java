package com.kingplus.social;

import android.content.Context;
import android.os.Build;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Lightweight local crash/non-fatal diagnostics for production testing. */
public final class KingStability {
    private static volatile boolean installed;
    private KingStability(){}

    public static void install(Context c){
        if(installed||c==null)return;
        installed=true;
        final Context app=c.getApplicationContext();
        final Thread.UncaughtExceptionHandler previous=Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread,error)->{
            write(app,"FATAL",error);
            if(previous!=null)previous.uncaughtException(thread,error);
        });
    }
    public static void nonFatal(Context c,String area,Throwable error){
        if(c==null)return;
        write(c.getApplicationContext(),"NON_FATAL "+(area==null?"":area),error);
    }
    private static synchronized void write(Context c,String kind,Throwable error){
        try{
            File f=new File(c.getFilesDir(),"kingplus-diagnostics.log");
            boolean append=f.exists()&&f.length()<1024*1024;
            if(!append&&f.exists())f.delete();
            try(PrintWriter w=new PrintWriter(new FileOutputStream(f,true))){
                w.println("================================================");
                w.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(new Date())+"  "+kind);
                w.println("device="+Build.MANUFACTURER+" "+Build.MODEL+" android="+Build.VERSION.RELEASE+" sdk="+Build.VERSION.SDK_INT);
                if(error!=null)error.printStackTrace(w);
            }
        }catch(Exception ignored){}
    }
    public static File logFile(Context c){return c==null?null:new File(c.getFilesDir(),"kingplus-diagnostics.log");}
    public static void clear(Context c){try{File f=logFile(c);if(f!=null&&f.exists())f.delete();}catch(Exception ignored){}}
}