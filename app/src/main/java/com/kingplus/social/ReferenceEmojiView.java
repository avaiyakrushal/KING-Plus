package com.kingplus.social;
import android.content.Context;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
public final class ReferenceEmojiView extends SVGAImageView {
    private static final String[] FILES={"im_interactive_emoji_heart.svga","im_interactive_emoji_cry.svga","im_interactive_emoji_smile.svga","im_emoji_full_heart.svga","im_emoji_full_cry.svga","im_emoji_full_smile.svga"};
    public static int parse(String token){if(token!=null&&token.matches("\\[ref:[0-5]\\]"))return token.charAt(5)-'0';return -1;}
    public ReferenceEmojiView(Context context,int index,boolean loop){super(context);setLoops(loop?0:1);setClearsAfterDetached(true);setContentDescription(new String[]{"Heart","Cry","Smile","Full heart","Full cry","Full smile"}[index]);
        new SVGAParser(context).decodeFromAssets("reference_emoji/"+FILES[index],new SVGAParser.ParseCompletion(){
            @Override public void onComplete(SVGAVideoEntity video){setVideoItem(video);startAnimation();}
            @Override public void onError(){setContentDescription("Animation unavailable");}
        },null);
    }
}
