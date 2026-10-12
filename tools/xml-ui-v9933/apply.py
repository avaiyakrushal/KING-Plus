#!/usr/bin/env python3
"""Add REAL, used Android XML layout resources to verified KING full release v9.9.32.
No fake padding to 4,000 XML; these 9 layouts are inflated by actual screens.
Usage: python3 tools/xml-ui-v9933/apply.py FULL_RELEASE_SOURCE_DIR
"""
from pathlib import Path
import sys
from xml.etree import ElementTree as ET

root=Path(sys.argv[1])
src=root/'app/src/main/java/com/kingplus/social'
layouts=root/'app/src/main/res/layout'
layouts.mkdir(parents=True,exist_ok=True)
xmlns='http://schemas.android.com/apk/res/android'
prefix='<?xml version="1.0" encoding="utf-8"?>\n'
def attrs(id='',width='wrap_content',height='wrap_content',extra=''):
    return (' android:id="@+id/'+id+'"' if id else '')+' android:layout_width="'+width+'" android:layout_height="'+height+'"'+extra
def text(id, extra=''):
    return '<TextView'+attrs(id,'wrap_content','wrap_content',extra)+'/>\n'
def x(root_tag,inner,extra=''):
    return prefix+'<'+root_tag+' xmlns:android="'+xmlns+'"'+attrs(width='match_parent',height='wrap_content',extra=extra)+'>\n'+inner+'</'+root_tag+'>\n'
V={
'king_deepflow_header':x('LinearLayout',
    text('king_ui_back',' android:layout_width="48dp" android:layout_height="54dp" android:gravity="center" android:contentDescription="Back"')
    .replace(' android:layout_width="wrap_content"','',1).replace(' android:layout_height="wrap_content"','',1)
    +'<TextView'+attrs('king_ui_title','0dp','54dp',' android:layout_weight="1" android:gravity="center_vertical" android:maxLines="1" android:ellipsize="end"')+'/>\n'
    +'<TextView'+attrs('king_ui_close','48dp','54dp',' android:gravity="center" android:contentDescription="Close"')+'/>\n',
    ' android:orientation="horizontal" android:gravity="center_vertical" android:paddingStart="8dp" android:paddingEnd="8dp" android:paddingTop="6dp" android:paddingBottom="4dp"'),
'king_deepflow_row':x('LinearLayout',
    '<TextView'+attrs('king_row_icon','44dp','58dp',' android:gravity="center"')+'/>\n'
    +'<LinearLayout'+attrs('king_row_text','0dp','58dp',' android:layout_weight="1" android:orientation="vertical" android:gravity="center_vertical"')+'>\n'
    +'<TextView'+attrs('king_row_title','match_parent','31dp',' android:gravity="center_vertical" android:singleLine="true" android:ellipsize="end"')+'/>\n'
    +'<TextView'+attrs('king_row_desc','match_parent','23dp',' android:gravity="center_vertical" android:singleLine="true" android:ellipsize="end"')+'/>\n'
    +'</LinearLayout><TextView'+attrs('king_row_arrow','34dp','58dp',' android:gravity="center"')+'/>\n',
    ' android:orientation="horizontal" android:gravity="center_vertical" android:paddingStart="12dp" android:paddingEnd="8dp" android:paddingTop="4dp" android:paddingBottom="4dp" android:focusable="true" android:clickable="true"'),
'king_deepflow_hero':x('LinearLayout',
    '<TextView'+attrs('king_hero_title','match_parent','wrap_content',' android:maxLines="2"')+'/>\n'
    +'<TextView'+attrs('king_hero_desc','match_parent','wrap_content',' android:maxLines="3"')+'/>\n',
    ' android:orientation="vertical" android:gravity="center_vertical" android:padding="16dp"'),
'king_deepflow_section':prefix+'<TextView xmlns:android="'+xmlns+'"'+attrs('king_section_title','match_parent','38dp',' android:gravity="center_vertical" android:paddingStart="12dp" android:paddingEnd="12dp"')+'/>\n',
'king_deepflow_chip':prefix+'<TextView xmlns:android="'+xmlns+'"'+attrs('king_chip_title','match_parent','46dp',' android:gravity="center" android:padding="8dp" android:clickable="true" android:focusable="true"')+'/>\n',
'king_gift_header':x('LinearLayout',
    '<TextView'+attrs('king_gift_back','50dp','56dp',' android:gravity="center" android:contentDescription="Back"')+'/>\n'
    +'<TextView'+attrs('king_gift_heading','0dp','56dp',' android:layout_weight="1" android:gravity="center_vertical"')+'/>\n'
    +'<TextView'+attrs('king_gift_balance','110dp','44dp',' android:gravity="center"')+'/>\n',
    ' android:orientation="horizontal" android:gravity="center_vertical"'),
'king_gift_hero':prefix+'<TextView xmlns:android="'+xmlns+'"'+attrs('king_gift_hero_text','match_parent','90dp',' android:paddingStart="16dp" android:paddingEnd="16dp" android:gravity="center_vertical"')+'/>\n',
'king_gift_row':x('LinearLayout',
    '<TextView'+attrs('king_gift_icon','54dp','62dp',' android:gravity="center"')+'/>\n'
    +'<LinearLayout'+attrs('king_gift_text','0dp','62dp',' android:layout_weight="1" android:gravity="center_vertical" android:orientation="vertical"')+'>\n'
    +'<TextView'+attrs('king_gift_name','match_parent','31dp',' android:singleLine="true" android:ellipsize="end"')+'/>\n'
    +'<TextView'+attrs('king_gift_meta','match_parent','24dp',' android:singleLine="true" android:ellipsize="end"')+'/>\n'
    +'</LinearLayout><TextView'+attrs('king_gift_action','70dp','62dp',' android:gravity="center"')+'/>\n',
    ' android:orientation="horizontal" android:gravity="center_vertical" android:paddingStart="10dp" android:paddingEnd="8dp" android:paddingTop="5dp" android:paddingBottom="5dp" android:clickable="true" android:focusable="true"'),
'king_gift_history':prefix+'<TextView xmlns:android="'+xmlns+'"'+attrs('king_gift_history_item','match_parent','48dp',' android:gravity="center_vertical" android:paddingStart="12dp" android:paddingEnd="12dp"')+'/>\n',
}
# Check every XML before committing. No dummy layouts or different resource variants.
for name,xml in V.items():
    ET.fromstring(xml)
    dst=layouts/(name+'.xml')
    if dst.exists(): raise RuntimeError('Layout name collision: '+name)
    dst.write_text(xml)
assert len(V)==9

def edit(path, start, end, new):
    text=path.read_text()
    if text.count(start)!=1 or text.count(end)!=1:raise RuntimeError('Anchor changed: '+path.name+' / '+start[:100])
    a=text.index(start);b=text.index(end,a)
    if b<=a:raise RuntimeError('Bad end anchor')
    path.write_text(text[:a]+new+text[b:])

deep=src/'KingDeepFlowActivity.java'
text=deep.read_text()
start='        LinearLayout head=new LinearLayout(this);'
end='        root.addView(head);'
assert text.count(start)==1 and text.count(end)==1
a=text.index(start);b=text.index(end,a)
new='''        LinearLayout head=(LinearLayout)getLayoutInflater().inflate(R.layout.king_deepflow_header,root,false);
        TextView back=head.findViewById(R.id.king_ui_back);
        back.setText("‹");back.setTextSize(38);back.setTextColor(fg());
        back.setOnClickListener(v->back());
        TextView title=head.findViewById(R.id.king_ui_title);
        title.setText(titleFor(route));title.setTextSize(21);title.setTypeface(null,Typeface.BOLD);title.setTextColor(fg());
        TextView close=head.findViewById(R.id.king_ui_close);
        close.setText("✕");close.setTextSize(22);close.setTextColor(fg());
        if(route.equals("profile_menu"))close.setOnClickListener(v->finish());
        else close.setVisibility(View.GONE);
'''
deep.write_text(text[:a]+new+text[b:])

edit(deep,'    private void section(String s){','    private void row(String icon,String name,String desc,String target){',
'''    private void section(String s){
        TextView v=(TextView)getLayoutInflater().inflate(R.layout.king_deepflow_section,body,false);
        v.setText(s.toUpperCase(java.util.Locale.ROOT));
        v.setTextSize(12);v.setTextColor(sub());v.setTypeface(null,Typeface.BOLD);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(38));lp.setMargins(0,dp(8),0,0);body.addView(v,lp);
    }
''')
edit(deep,'    private void row(String icon,String name,String desc,Runnable action){','    private void chip(String text,Runnable action){',
'''    private void row(String icon,String name,String desc,Runnable action){
        LinearLayout r=(LinearLayout)getLayoutInflater().inflate(R.layout.king_deepflow_row,body,false);
        r.setBackground(bg(card(),14));
        TextView ic=r.findViewById(R.id.king_row_icon);
        ic.setText(icon);ic.setTextSize(22);ic.setTextColor(fg());
        TextView n=r.findViewById(R.id.king_row_title);
        n.setText(name);n.setTextSize(15);n.setTextColor(fg());n.setTypeface(null,Typeface.BOLD);
        TextView ds=r.findViewById(R.id.king_row_desc);
        if(desc==null||desc.isEmpty())ds.setVisibility(View.GONE);
        else{ds.setText(desc);ds.setTextSize(11);ds.setTextColor(sub());}
        TextView ar=r.findViewById(R.id.king_row_arrow);
        ar.setText("›");ar.setTextSize(25);ar.setTextColor(sub());
        r.setContentDescription(name+(desc==null?"":", "+desc));
        r.setOnClickListener(v->KingSafe.run(this,"ui-action",action));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(62));
        lp.setMargins(0,dp(4),0,dp(4));body.addView(r,lp);
    }
''')
edit(deep,'    private void chip(String text,Runnable action){','    private void hero(String title,String desc){',
'''    private void chip(String text,Runnable action){
        TextView t=(TextView)getLayoutInflater().inflate(R.layout.king_deepflow_chip,body,false);
        t.setText(text);t.setTextSize(13);t.setTextColor(dark()?Color.WHITE:INK);t.setTypeface(null,Typeface.BOLD);
        t.setBackground(bg(dark()?0xff383246:0xffeeeaf6,14));
        t.setOnClickListener(v->KingSafe.run(this,"ui-action",action));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(46));
        lp.setMargins(0,dp(4),0,dp(4));body.addView(t,lp);
    }
''')
edit(deep,'    private void hero(String title,String desc){','    private void profileMenu(){',
'''    private void hero(String title,String desc){
        LinearLayout h=(LinearLayout)getLayoutInflater().inflate(R.layout.king_deepflow_hero,body,false);
        h.setBackground(bg(dark()?0xff2d293b:0xfffff3bd,18));
        TextView a=h.findViewById(R.id.king_hero_title),b=h.findViewById(R.id.king_hero_desc);
        a.setText(title);a.setTextSize(18);a.setTextColor(fg());a.setTypeface(null,Typeface.BOLD);
        b.setText(desc);b.setTextSize(12);b.setTextColor(sub());
        body.addView(h,new LinearLayout.LayoutParams(-1,dp(92)));
    }
''')

gift=src/'KingGiftCenterActivity.java'
edit(gift,'    private void render(){','    private void drawCatalog(){',
'''    private void render(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);
        LinearLayout head=(LinearLayout)getLayoutInflater().inflate(R.layout.king_gift_header,root,false);
        TextView back=head.findViewById(R.id.king_gift_back);
        back.setText("‹");back.setTextSize(38);back.setTextColor(Color.WHITE);
        back.setOnClickListener(v->finish());
        TextView title=head.findViewById(R.id.king_gift_heading);
        title.setText("Gift Center");title.setTextSize(20);title.setTextColor(Color.WHITE);title.setTypeface(null,Typeface.BOLD);
        balance=head.findViewById(R.id.king_gift_balance);
        balance.setText("💎 --");balance.setTextSize(13);balance.setTextColor(GOLD);balance.setTypeface(null,Typeface.BOLD);
        root.addView(head);
        ScrollView sv=new ScrollView(this);body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(14),dp(8),dp(14),dp(30));sv.addView(body);
        root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
        TextView hero=(TextView)getLayoutInflater().inflate(R.layout.king_gift_hero,body,false);
        hero.setText("🎁 Send a gift to "+targetName+"\\nKING Plus · TEST gifts until paid billing is verified");
        hero.setTextSize(17);hero.setTextColor(Color.WHITE);hero.setTypeface(null,Typeface.BOLD);
        hero.setBackground(bg(0xff332d46,18));body.addView(hero,new LinearLayout.LayoutParams(-1,dp(90)));
        drawCatalog();
        TextView hh=tv("GIFT HISTORY",12,MUTED,true);body.addView(hh,new LinearLayout.LayoutParams(-1,dp(42)));
        history=new LinearLayout(this);history.setOrientation(LinearLayout.VERTICAL);body.addView(history);
    }
''')
edit(gift,'    private void drawCatalog(){','    private void quantity(int k){',
'''    private void drawCatalog(){
        LevelSystem.Snapshot snapshot=LevelSystem.read(this);
        for(int i=0;i<names.length;i++){
            final int k=i;
            LinearLayout r=(LinearLayout)getLayoutInflater().inflate(R.layout.king_gift_row,body,false);
            r.setBackground(bg(CARD,14));
            TextView icon=r.findViewById(R.id.king_gift_icon);
            icon.setText(icons[i]);icon.setTextSize(28);icon.setTextColor(Color.WHITE);
            TextView n=r.findViewById(R.id.king_gift_name);
            n.setText(names[i]);n.setTextSize(15);n.setTextColor(Color.WHITE);n.setTypeface(null,Typeface.BOLD);
            TextView meta=r.findViewById(R.id.king_gift_meta);
            boolean verifiedVip=vip[i]==0 || (KingPremiumAccess981.PAID_VIP_ACTIVE && snapshot.vipLevel>=vip[i]);
            meta.setText(categories[i]+" • 💎 "+costs[i]+" • "+(vip[i]==0?"Open":"VIP "+vip[i]));
            meta.setTextSize(11);meta.setTextColor(verifiedVip?0xffc9c3d2:0xffff8f8f);
            TextView action=r.findViewById(R.id.king_gift_action);
            action.setText(verifiedVip?"Send ›":"🔒");action.setTextSize(13);
            action.setTextColor(verifiedVip?GOLD:MUTED);action.setTypeface(null,Typeface.BOLD);
            r.setContentDescription(names[i]+", "+categories[i]+", "+costs[i]+" Diamonds, "+(verifiedVip?"available":"locked"));
            r.setOnClickListener(v->{if(!verifiedVip){toast("Paid VIP gift locked until secure recharge is enabled");return;}quantity(k);});
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(66));
            lp.setMargins(0,dp(4),0,dp(4));body.addView(r,lp);
        }
    }
''')
# Also use an actual XML view for every gift ledger row.
text=gift.read_text()
old='TextView r=tv((sent?"↗ Sent ":"↙ Received ")+(g==null?"Gift":g)+"  •  💎"+(c==null?0:c),13,Color.WHITE,true);'
new='TextView r=(TextView)getLayoutInflater().inflate(R.layout.king_gift_history,history,false);r.setText((sent?"↗ Sent ":"↙ Received ")+(g==null?"Gift":g)+"  •  💎"+(c==null?0:c));r.setTextSize(13);r.setTextColor(Color.WHITE);r.setTypeface(null,Typeface.BOLD);'
if text.count(old)!=1:raise RuntimeError('History anchor changed')
gift.write_text(text.replace(old,new,1))
g=root/'app/build.gradle';t=g.read_text()
old="versionCode 211; versionName '9.9.32-room-3d-gift-games-test'"
if t.count(old)!=1:raise RuntimeError('Not based on verified v9.9.32 full source')
g.write_text(t.replace(old,"versionCode 212; versionName '9.9.33-owned-xml-layouts-test'"))
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print('PASS 9 real used XML layouts integrated in DeepFlow and Gift Center; VIP paid status locked; versionCode 212')
