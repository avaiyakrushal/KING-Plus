from pathlib import Path
import sys
root=Path(sys.argv[1]) if len(sys.argv)>1 else Path('/tmp/src')
p=root/'app/src/main/java/com/kingplus/social/PartyActivity.java'
s=p.read_text()

def one(old,new,label):
    global s
    if old not in s: raise SystemExit('MISSING '+label)
    s=s.replace(old,new,1)

one('    private TextView giftBalanceLabel;\n    private TextView supporterLabel;',
    '    private TextView giftBalanceLabel;\n    private LinearLayout giftCategoryBar867;\n    private TextView supporterLabel;', 'gift category field')
one('TextView title=tv("Emoji & Stickers",15,0xff333333,true);',
    'TextView title=tv("Live Emoji • 50  |  Stickers",15,0xff333333,true);','emoji title')
one('fillEmojiGrid610(grid,pack,k==2||k==3?6:4,k<2);',
    'fillEmojiGrid610(grid,pack,k==2||k==3?6:5,k<2);','emoji columns')
one('grid.addView(row,new LinearLayout.LayoutParams(-1,dp(columns==6?58:78)))',
    'grid.addView(row,new LinearLayout.LayoutParams(-1,dp(columns==6?58:(columns==5?70:78))))','emoji row height')

start=s.index('    private void giftShopPanel(){')
end=s.index('    private void sendGift(String gift,int cost)',start)
new=r'''    private String giftSelectionText867(){
        int i=Math.max(0,Math.min(giftNames.length-1,giftSelectedIndex));
        long total=(long)giftCosts[i]*Math.max(1,giftQuantity);
        return giftIcons[i]+"  "+giftNames[i]+"  •  x"+Math.max(1,giftQuantity)+"  •  💎"+compactNumber(total)+"\nTo: "+giftTargetName;
    }
    private void refreshGiftSelection867(){if(giftSelectionLabel!=null)giftSelectionLabel.setText(giftSelectionText867());}
    private void giftShopPanel(){
        if(ownerName==null)ownerName="KING Host";
        if(giftTargetName==null||giftTargetName.isEmpty()){giftTargetName="👑 "+ownerName;giftTargetUid=ownerUid==null?"":ownerUid;}
        giftQuantity=1; giftCategory="All"; giftSelectedIndex=0;
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(10),dp(8),dp(10),dp(10));root.setBackgroundColor(0xff181824);
        LinearLayout banner=new LinearLayout(this);banner.setGravity(Gravity.CENTER_VERTICAL);banner.setPadding(dp(10),dp(4),dp(8),dp(4));banner.setBackground(bg(0xff7d36d6,14));
        TextView wt=tv("🎟  Weekly Gift Card",16,Color.WHITE,true);banner.addView(wt,new LinearLayout.LayoutParams(0,dp(42),1));TextView arrow=tv("›",28,Color.WHITE,true);arrow.setGravity(Gravity.CENTER);banner.addView(arrow,new LinearLayout.LayoutParams(dp(40),dp(42)));banner.setOnClickListener(v->{if(giftShopDialog!=null)giftShopDialog.dismiss();weeklyGiftCardDialog();});root.addView(banner,new LinearLayout.LayoutParams(-1,dp(40)));
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);TextView title=tv("Gift Shop • 64 gifts",19,Color.WHITE,true);top.addView(title,new LinearLayout.LayoutParams(0,dp(42),1));giftBalanceLabel=pill("💎 "+localCoins,0xff302d40,null);top.addView(giftBalanceLabel,new LinearLayout.LayoutParams(dp(110),dp(38)));root.addView(top);
        giftSelectionLabel=tv(giftSelectionText867(),13,0xffffe38a,true);giftSelectionLabel.setGravity(Gravity.CENTER_VERTICAL);giftSelectionLabel.setBackground(bg(0xff2a233a,14));LinearLayout.LayoutParams selLp=new LinearLayout.LayoutParams(-1,dp(58));selLp.setMargins(0,0,0,dp(5));root.addView(giftSelectionLabel,selLp);
        HorizontalScrollView peopleScroll=new HorizontalScrollView(this);peopleScroll.setHorizontalScrollBarEnabled(false);LinearLayout people=new LinearLayout(this);people.setGravity(Gravity.CENTER_VERTICAL);people.setPadding(0,dp(3),0,dp(5));addGiftRecipientChip(people,"👑 "+ownerName,ownerUid==null?"":ownerUid);for(int no=1;no<=maxSeats;no++){String n=seatNames.get(no);if(n==null||n.equals(displayName))continue;addGiftRecipientChip(people,no+" • "+n,seatUids.get(no)==null?"":seatUids.get(no));}peopleScroll.addView(people);root.addView(peopleScroll,new LinearLayout.LayoutParams(-1,dp(50)));
        HorizontalScrollView catScroll=new HorizontalScrollView(this);catScroll.setHorizontalScrollBarEnabled(false);giftCategoryBar867=new LinearLayout(this);giftCategoryBar867.setGravity(Gravity.CENTER_VERTICAL);catScroll.addView(giftCategoryBar867);root.addView(catScroll,new LinearLayout.LayoutParams(-1,dp(44)));rebuildGiftCategories867();
        ScrollView giftsScroll=new ScrollView(this);giftGridBox=new LinearLayout(this);giftGridBox.setOrientation(LinearLayout.VERTICAL);giftsScroll.addView(giftGridBox);root.addView(giftsScroll,new LinearLayout.LayoutParams(-1,0,1));rebuildGiftGrid();
        LinearLayout qty=new LinearLayout(this);qty.setGravity(Gravity.CENTER);int[] qs={1,9,49,99,499};for(int q:qs){TextView b=pill(String.valueOf(q),q==1?0xff776a00:0xff343143,()->{giftQuantity=q;refreshGiftSelection867();});LinearLayout.LayoutParams qp=new LinearLayout.LayoutParams(0,dp(44),1);qp.setMargins(dp(3),0,dp(3),0);qty.addView(b,qp);}TextView send=pill("SEND",0xffffd92e,()->{int total=giftCosts[giftSelectedIndex]*giftQuantity;sendGiftQuantity(giftTargetUid,giftTargetName,giftNames[giftSelectedIndex],giftIcons[giftSelectedIndex],giftCosts[giftSelectedIndex],giftQuantity,total);if(giftBalanceLabel!=null)giftBalanceLabel.setText("💎 "+localCoins);});send.setTextColor(Color.BLACK);send.setTextSize(13);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(dp(78),dp(44));sp.setMargins(dp(5),0,0,0);qty.addView(send,sp);root.addView(qty);
        giftShopDialog=new AlertDialog.Builder(this).setView(root).create();
        giftShopDialog.setOnShowListener(v->{android.view.Window w=giftShopDialog.getWindow();if(w!=null){w.setBackgroundDrawableResource(android.R.color.transparent);w.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);w.setGravity(Gravity.BOTTOM);w.setLayout(-1,(int)(getResources().getDisplayMetrics().heightPixels*.68f));}});giftShopDialog.show();
    }
    private void rebuildGiftCategories867(){
        if(giftCategoryBar867==null)return;giftCategoryBar867.removeAllViews();String[] cs={"All","Relationship","Activity","Classic","Flying","Fame","Privilege","Filters","Parcel"};
        for(String c:cs){boolean selected=c.equals(giftCategory);TextView t=tv(c,12,selected?Color.BLACK:0xffd0cad8,true);t.setGravity(Gravity.CENTER);t.setBackground(bg(selected?0xffffd92e:0xff2a2938,12));t.setOnClickListener(v->{giftCategory=c;rebuildGiftCategories867();rebuildGiftGrid();});LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(dp(88),dp(36));cp.setMargins(dp(2),dp(2),dp(2),dp(2));giftCategoryBar867.addView(t,cp);}
    }
    private void addGiftRecipientChip(LinearLayout row,String label,String uid){
        TextView chip=pill(label,0xff343143,()->{giftTargetName=label;giftTargetUid=uid==null?"":uid;refreshGiftSelection867();});
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(132),dp(42));p.setMargins(dp(2),0,dp(2),0);row.addView(chip,p);
    }
    private void rebuildGiftGrid(){
        if(giftGridBox==null)return;giftGridBox.removeAllViews();
        int first=-1;boolean selectedVisible=false;for(int i=0;i<giftNames.length;i++){if("All".equals(giftCategory)||giftCategory.equals(giftCategories[i])){if(first<0)first=i;if(i==giftSelectedIndex)selectedVisible=true;}}
        if(first<0){giftGridBox.addView(tv("No gifts in this category yet",13,MUTED,false));return;}if(!selectedVisible)giftSelectedIndex=first;refreshGiftSelection867();
        LinearLayout row=null;int col=0;int myVip=LevelSystem.read(this).vipLevel;
        for(int i=0;i<giftNames.length;i++){
            if(!"All".equals(giftCategory)&&!giftCategory.equals(giftCategories[i]))continue;
            if(row==null||col==4){row=new LinearLayout(this);row.setGravity(Gravity.CENTER);giftGridBox.addView(row,new LinearLayout.LayoutParams(-1,dp(116)));col=0;}
            final int idx=i;final int vipNeed=giftVipRequired[i];final boolean unlocked=myVip>=vipNeed;final boolean selected=idx==giftSelectedIndex;LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setBackground(bg(selected?0xff47336c:(unlocked?0xff232231:0xff171720),14));card.setPadding(dp(2),dp(3),dp(2),dp(2));card.setAlpha(unlocked?1f:.62f);
            TextView nw=tv(selected?"✓ SELECTED":(vipNeed>0?(unlocked?"VIP ✓":"VIP "+vipNeed):""),9,selected?0xffffd92e:(unlocked?0xff68e89d:0xffffb04f),true);nw.setGravity(Gravity.CENTER);card.addView(nw,new LinearLayout.LayoutParams(-1,dp(18)));
            TextView icon=tv(unlocked?giftIcons[i]:"🔒",36,unlocked?Color.WHITE:0xff77727e,false);icon.setGravity(Gravity.CENTER);card.addView(icon,new LinearLayout.LayoutParams(-1,dp(45)));
            TextView name=tv(giftNames[i],9,unlocked?0xfff5f2f7:0xff817b88,true);name.setGravity(Gravity.CENTER);name.setMaxLines(1);card.addView(name,new LinearLayout.LayoutParams(-1,dp(22)));
            TextView cost=tv("💎 "+giftCosts[i],9,selected?0xffffd92e:0xffaaa5b4,true);cost.setGravity(Gravity.CENTER);card.addView(cost,new LinearLayout.LayoutParams(-1,dp(20)));
            card.setOnClickListener(v->{if(!unlocked){toast("Unlocks at VIP "+vipNeed);return;}giftSelectedIndex=idx;refreshGiftSelection867();rebuildGiftGrid();});LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(110),1);cp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(card,cp);col++;
        }
    }
'''
s=s[:start]+new+s[end:]
p.write_text(s)
print('v8.6.7 Party gift/live polish applied')
