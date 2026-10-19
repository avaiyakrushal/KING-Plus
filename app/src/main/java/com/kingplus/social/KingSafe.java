package com.kingplus.social;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

public final class KingSafe {
    private KingSafe(){}
    private static Context context(Object owner){
        if(owner instanceof Context)return (Context)owner;
        if(owner instanceof View)return ((View)owner).getContext();
        return null;
    }
    public static void run(Object owner,String area,Runnable action){
        try{if(action!=null)action.run();}
        catch(Throwable error){
            Context c=context(owner);
            if(c!=null)KingStability.nonFatal(c,area,error);
            if(c!=null){
                try{
                    if(!(c instanceof Activity)||(!((Activity)c).isFinishing()&&!((Activity)c).isDestroyed()))
                        Toast.makeText(c,"This action could not open safely. Please try again.",Toast.LENGTH_LONG).show();
                }catch(Throwable ignored){}
            }
        }
    }
}
