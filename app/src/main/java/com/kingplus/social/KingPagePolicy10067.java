package com.kingplus.social;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Shared category aliases and truthful server-wallet values. No device coin fallback. */
final class KingPagePolicy10067 {
    static List<String> categories(String selected) {
        List<String> aliases;
        if(selected==null||"All".equalsIgnoreCase(selected)||"Hot".equalsIgnoreCase(selected))return new ArrayList<>();
        if("Music".equalsIgnoreCase(selected))aliases=Arrays.asList("Music","Sing","KTV","Radio");
        else if("KTV".equalsIgnoreCase(selected))aliases=Arrays.asList("KTV");
        else if("Radio".equalsIgnoreCase(selected))aliases=Arrays.asList("Radio");
        else if("Game".equalsIgnoreCase(selected))aliases=Arrays.asList("Game","PK","Pick Me");
        else if("PK".equalsIgnoreCase(selected))aliases=Arrays.asList("PK");
        else if("Pick Me".equalsIgnoreCase(selected))aliases=Arrays.asList("Pick Me");
        else if("Video".equalsIgnoreCase(selected))aliases=Arrays.asList("Multi Video","Video");
        else if("Event".equalsIgnoreCase(selected))aliases=Arrays.asList("Event","Birthday","Wedding","Family");
        else if("Family".equalsIgnoreCase(selected))aliases=Arrays.asList("Family");
        else if("Team Up".equalsIgnoreCase(selected))aliases=Arrays.asList("Team Up","TeamUp","Gang Up");
        else if("Date".equalsIgnoreCase(selected))aliases=Arrays.asList("Date");
        else return Arrays.asList(selected,selected.toLowerCase(Locale.ROOT));
        List<String> values=new ArrayList<>();
        for(String alias:aliases){values.add(alias);String lower=alias.toLowerCase(Locale.ROOT);if(!alias.equals(lower))values.add(lower);}
        return values;
    }
    static Long balance(Object value) {
        return value instanceof Long&&(Long)value>=0&&(Long)value<=1000000000L?(Long)value:null;
    }
    static int palette(String id,int count){return Math.floorMod(id==null?0:id.hashCode(),count);}
}
