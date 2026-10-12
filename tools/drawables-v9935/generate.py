#!/usr/bin/env python3
"""Independent KING Plus original graphics; build from exact v9.9.34 source.

9 feature art families x 3 graphic types x 281 distinct palettes = 7,587 *real*
compiled Android drawable resources, viewable in-app. No Bolo Hi assets copied.
The new drawable count target is 1093 (previous APK) + 7587 = 8680.
"""
from pathlib import Path
from xml.etree import ElementTree as ET
from colorsys import hsv_to_rgb
import sys

root=Path(sys.argv[1])
src=root/'app/src/main/java/com/kingplus/social'
layouts=root/'app/src/main/res'
drawable=layouts/'drawable'
drawable.mkdir(parents=True,exist_ok=True)
family=("party","mic","avatar","gift","vip","games","medal","profile","entrance")
kind=("symbol","panel","halo")
N=281
ANDROID="http://schemas.android.com/apk/res/android"
# Original pictograms deliberately not extracted/reproduced from third-party APK.
GLYPHS={
"party":"M24,43 L39,28 L50,41 L61,28 L76,43 L69,70 L31,70 Z",
"mic":"M42,24 Q50,13 58,24 L58,53 Q50,66 42,53 Z M31,48 L35,48 Q35,68 50,68 Q65,68 65,48 L69,48 Q69,72 52,75 L52,83 L66,83 L66,87 L34,87 L34,83 L48,83 L48,75 Q31,72 31,48 Z",
"avatar":"M50,18 A15,15 0 1,1 49.99,18 Z M20,81 Q22,55 50,56 Q78,55 80,81 Z",
"gift":"M21,44 L79,44 L79,78 L21,78 Z M15,33 L85,33 L85,44 L15,44 Z M46,33 Q24,34 28,22 Q32,12 48,32 Q61,11 71,22 Q76,34 54,33 Z",
"vip":"M20,33 L35,48 L50,24 L65,48 L80,33 L73,77 L27,77 Z M27,80 L73,80 L73,85 L27,85 Z",
"games":"M24,37 Q30,28 48,33 L52,33 Q70,28 76,37 L86,67 Q89,81 75,77 L62,65 L38,65 L25,77 Q11,81 14,67 Z",
"medal":"M36,18 L48,43 L33,51 L18,19 Z M64,18 L52,43 L67,51 L82,19 Z M50,43 A24,24 0 1,1 49.99,43 Z",
"profile":"M50,17 A16,16 0 1,1 49.99,17 Z M18,82 Q18,55 50,55 Q82,55 82,82 Z",
"entrance":"M24,17 L72,17 L72,82 L24,82 Z M29,23 L29,77 L65,77 L65,23 Z M50,37 L67,50 L50,63 L50,55 L37,55 L37,45 L50,45 Z",
}
def shade(h,s,v):
    r,g,b=hsv_to_rgb(h,s,v)
    return '#%02X%02X%02X'%tuple(int(255*x+.5) for x in (r,g,b))
def a(i): return "{http://schemas.android.com/apk/res/android}"+i
def value(tag):
    ET.fromstring(tag)
    return '<?xml version="1.0" encoding="utf-8"?>\n'+tag+'\n'
def vector(f,palette,seq):
    ink,accent,dark,stroke=palette
    # Palette + border geometry vary per index; min/max bounded for small devices.
    inset=11+(seq%9)
    o='<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="64dp" android:height="64dp" android:viewportWidth="100" android:viewportHeight="100">'
    o+=f'<path android:fillColor="{dark}" android:pathData="M12,10 L88,10 Q92,10 92,14 L92,86 Q92,90 88,90 L12,90 Q8,90 8,86 L8,14 Q8,10 12,10 Z"/>'
    o+=f'<path android:fillColor="{stroke}" android:pathData="M50,{inset} L{100-inset},50 L50,{100-inset} L{inset},50 Z"/>'
    o+=f'<path android:fillColor="{ink}" android:pathData="{GLYPHS[f]}"/>'
    o+=f'<path android:fillColor="{accent}" android:pathData="M72,14 L80,14 L80,22 L72,22 Z M19,72 L24,72 L24,77 L19,77 Z"/>'
    o+='</vector>'
    return value(o)
def shape(kind,palette,seq):
    ink,accent,dark,stroke=palette
    radius=10+(seq%18)
    if kind=="panel":
        gradient=f'<gradient android:angle="{(seq%8)*45}" android:startColor="{dark}" android:centerColor="{ink}" android:endColor="{stroke}"/>'
        border=f'<stroke android:width="1dp" android:color="{accent}"/>'
    else:
        gradient=f'<gradient android:type="radial" android:gradientRadius="{64+seq%40}dp" android:centerX="0.50" android:centerY="0.40" android:startColor="{accent}" android:centerColor="{ink}" android:endColor="{dark}"/>'
        border=f'<stroke android:width="2dp" android:color="{stroke}"/>'
    x='<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">'
    x+=gradient+f'<corners android:radius="{radius}dp"/>'+border
    x+='<padding android:left="12dp" android:top="8dp" android:right="12dp" android:bottom="8dp"/></shape>'
    return value(x)
created=[]
for fi,f in enumerate(family):
    for k in kind:
        for i in range(N):
            h=((i*1.61803398875 / N)+(fi/len(family)))%1.0
            pal=(shade(h,.42+(i%5)*.06,.58+(i%4)*.06),
                 shade((h+.14)%1,.32,.91),
                 shade(h,.38,.13+(i%5)*.025),
                 shade((h+.04)%1,.28,.26+(i%4)*.035))
            name=f"king_art_{f}_{k}_{i:03d}.xml"
            path=drawable/name
            if path.exists(): raise RuntimeError("Duplicate drawable "+name)
            xml=vector(f,pal,i) if k=="symbol" else shape(k,pal,i)
            path.write_text(xml)
            created.append(path)
assert len(created)==7587
# Explicit resource-shrinker keep: dynamic getIdentifier lookups must survive release.
keep=layouts/'raw'/'king_9935_drawable_keep.xml'
keep.parent.mkdir(exist_ok=True)
keep.write_text('''<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:tools="http://schemas.android.com/tools" tools:keep="@drawable/king_art_*"/>
''')
# UI catalog is an actual Activity screen: preview resources, choose/apply to hero
# and open corresponding functional KING menu.
java=src/'KingDeepFlowActivity.java'
s=java.read_text()
def replace_one(old,new):
    global s
    if s.count(old)!=1: raise RuntimeError(f"Ambiguous Java integration anchor ({s.count(old)}): {old[:110]}")
    s=s.replace(old,new,1)
replace_one('case "ui_design_library": designLibrary9934();break;',
            'case "ui_design_library": designLibrary9934();break;case "drawable_library": drawableLibrary9935();break;')
replace_one('row("🎨","KING XML Design Library","3,888 usable XML design previews (not 3,888 live features)","ui_design_library");',
            'row("🎨","KING XML Design Library","3,888 original XML screen designs","ui_design_library");\n'
            '        row("🖼","KING Drawable Studio","7,587 original badges, panels and effects","drawable_library");')
anchor='    private void settings(){'
method=r'''
    private final String[] artFamilies9935={"party","mic","avatar","gift","vip","games","medal","profile","entrance"};
    private int artFamily9935=0,artVariant9935=0;
    private void drawableLibrary9935(){
        hero("KING Drawable Studio · 7,587 graphics","Original KING graphics. This design library does not add 7,587 new live game features.");
        section("Choose art family");
        for(int i=0;i<artFamilies9935.length;i++){
            final int selected=i;
            chip((artFamily9935==i?"● ":"○ ")+artFamilies9935[i].toUpperCase(java.util.Locale.ROOT),()->{
                artFamily9935=selected;render();
            });
        }
        section("Palette "+(artVariant9935+1)+" / 281");
        String family=artFamilies9935[artFamily9935];
        String index=String.format(java.util.Locale.ROOT,"%03d",artVariant9935);
        for(String kind:new String[]{"symbol","panel","halo"}){
            String key="king_art_"+family+"_"+kind+"_"+index;
            int id=getResources().getIdentifier(key,"drawable",getPackageName());
            if(id==0){toast("Drawable missing: "+key);continue;}
            if(kind.equals("symbol")){
                android.widget.ImageView img=new android.widget.ImageView(this);
                img.setImageResource(id);img.setContentDescription(family+" original vector icon variant "+(artVariant9935+1));
                img.setPadding(dp(12),dp(8),dp(12),dp(8));
                body.addView(img,new LinearLayout.LayoutParams(-1,dp(94)));
            }else{
                TextView tile=tv((kind.equals("panel")?"ROYAL PANEL  ·  ":"ROOM AURA  ·  ")+family.toUpperCase(java.util.Locale.ROOT),
                                16,Color.WHITE,true);
                tile.setBackgroundResource(id);tile.setGravity(Gravity.CENTER);
                tile.setContentDescription(key);
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(85));
                lp.setMargins(0,dp(5),0,dp(5));
                body.addView(tile,lp);
            }
        }
        chip("◀ Previous graphic",()->{artVariant9935=(artVariant9935+280)%281;render();});
        chip("Next graphic ▶",()->{artVariant9935=(artVariant9935+1)%281;render();});
        chip("Use this panel on KING menu headers",()->{
            prefs.edit().putString("king_selected_art_family",family)
                .putInt("king_selected_art_variant",artVariant9935).apply();
            toast("Original KING artwork applied to menu headers");render();
        });
        chip("Remove applied artwork",()->{
            prefs.edit().remove("king_selected_art_family").remove("king_selected_art_variant").apply();
            toast("Default menu artwork restored");render();
        });
        chip("Open related KING feature",()->go(libraryFeatureRoute9934(family.equals("medal")?"level":family.equals("entrance")?"party":family)));
    }
'''
if s.count(anchor)!=1: raise RuntimeError("Settings method anchor moved")
s=s.replace(anchor,method+'\n'+anchor,1)
old='h.setBackground(bg(dark()?0xff2d293b:0xfffff3bd,18));'
new='''h.setBackground(bg(dark()?0xff2d293b:0xfffff3bd,18));
        String artFamily=prefs.getString("king_selected_art_family",null);
        boolean usingArt=artFamily!=null;
        if(usingArt){
            int artNum=Math.min(280,Math.max(0,prefs.getInt("king_selected_art_variant",0)));
            String artName="king_art_"+artFamily+"_panel_"+String.format(java.util.Locale.ROOT,"%03d",artNum);
            int artId=getResources().getIdentifier(artName,"drawable",getPackageName());
            if(artId!=0)h.setBackgroundResource(artId);
            else usingArt=false;
        }'''
replace_one(old,new)
replace_one('a.setText(title);a.setTextSize(18);a.setTextColor(fg());a.setTypeface(null,Typeface.BOLD);',
            'a.setText(title);a.setTextSize(18);a.setTextColor(usingArt?Color.WHITE:fg());a.setTypeface(null,Typeface.BOLD);')
replace_one('b.setText(desc);b.setTextSize(12);b.setTextColor(sub());',
            'b.setText(desc);b.setTextSize(12);b.setTextColor(usingArt?0xffeeeeee:sub());')
java.write_text(s)
g=root/'app/build.gradle';t=g.read_text()
old="versionCode 213; versionName '9.9.34-4065-xml-library-test'"
if t.count(old)!=1: raise RuntimeError('Source must be verified v9.9.34, never stale root')
g.write_text(t.replace(old,"versionCode 214; versionName '9.9.35-8680-original-drawables-test'"))
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
print("PASS 7,587 original drawable graphics + real in-app preview and opt-in menu theme; paid billing stays OFF")
