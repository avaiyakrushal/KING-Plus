#!/usr/bin/env python3
"""KING Plus 9.9.1 original cinematic visual refresh on verified v9.9.0 source.

Never copy third-party Bolo Hi art. Keep Firebase/Auth/game/payment logic intact.
All new visuals drawn locally on Android Canvas.
"""
from pathlib import Path
import shutil,sys
root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
src=Path(__file__).parent
for name in ['GiftBurstView.java','KingGiftArtView.java',
             'KingRoomStageView.java','KingLoginBackdropView.java',
             'KingEmojiAuraView992.java']:
    assert (src/name).exists(),name
    assert (pkg/name).exists() or name=='KingEmojiAuraView992.java',name
    shutil.copyfile(src/name,pkg/name)
    print('PASS replace/add',name)

def once(file,old,new,label):
    text=file.read_text()
    found=text.count(old)
    if found!=1:
        raise SystemExit(f'FAILED {label}: anchor count {found}, {old[:110]!r}')
    file.write_text(text.replace(old,new,1))
    print('PASS',label)

main=pkg/'MainActivity.java'
m=main.read_text()
start=m.index('    private void login() {')
end=m.index('    private void socialProviderSetupRequired(',start)
assert m.count('    private void login() {')==1
replacement='''    private void login() {
        screen = "login";
        stopMic();
        android.widget.FrameLayout root = new android.widget.FrameLayout(this);
        root.setBackgroundColor(0xff0a0927);
        // Animated original, high-contrast royal login backdrop.
        KingLoginBackdropView backdrop=new KingLoginBackdropView(this);
        root.addView(backdrop,new android.widget.FrameLayout.LayoutParams(-1,-1));
        View shade=new View(this);
        shade.setBackgroundColor(0x1d030515);
        root.addView(shade,new android.widget.FrameLayout.LayoutParams(-1,-1));
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        page=new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setPadding(dp(24),dp(20),dp(24),dp(30));
        scroll.addView(page);
        root.addView(scroll,new android.widget.FrameLayout.LayoutParams(-1,-1));
        setSafeContentView(root);

        View heroSpacer=new View(this);
        page.addView(heroSpacer,new LinearLayout.LayoutParams(-1,dp(90)));

        ImageView logo=new ImageView(this);
        logo.setImageResource(R.drawable.king_logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logo.setElevation(dp(8));
        LinearLayout.LayoutParams logoLp=new LinearLayout.LayoutParams(dp(114),dp(114));
        logoLp.gravity=Gravity.CENTER_HORIZONTAL;
        logoLp.bottomMargin=dp(5);
        page.addView(logo,logoLp);
        TextView heading=new TextView(this);
        heading.setText("KING Plus");heading.setTextSize(37);
        heading.setTextColor(Color.WHITE);
        heading.setTypeface(android.graphics.Typeface.create("sans-serif-black",android.graphics.Typeface.BOLD));
        heading.setGravity(Gravity.CENTER);
        heading.setShadowLayer(10f,0,5,0xee05001f);
        page.addView(heading,new LinearLayout.LayoutParams(-1,dp(54)));
        TextView subtitle=new TextView(this);
        subtitle.setText("VOICE  ✦  PARTY  ✦  FRIENDS  ✦  GAMES");
        subtitle.setTextColor(0xffffe6a5);subtitle.setTextSize(12);
        subtitle.setLetterSpacing(.08f);subtitle.setGravity(Gravity.CENTER);
        page.addView(subtitle,new LinearLayout.LayoutParams(-1,dp(27)));
        View gap=new View(this);page.addView(gap,new LinearLayout.LayoutParams(-1,dp(19)));

        LinearLayout outer=page;
        LinearLayout authCard=new LinearLayout(this);
        authCard.setOrientation(LinearLayout.VERTICAL);
        authCard.setPadding(dp(16),dp(18),dp(16),dp(14));
        android.graphics.drawable.GradientDrawable card=new android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
            new int[]{0xee281644,0xee151639,0xf02b1048});
        card.setCornerRadius(dp(24));
        card.setStroke(dp(1),0x88ffdd98);
        authCard.setBackground(card);
        authCard.setElevation(dp(8));
        LinearLayout.LayoutParams cardLp=new LinearLayout.LayoutParams(-1,-2);
        cardLp.leftMargin=dp(1);cardLp.rightMargin=dp(1);
        outer.addView(authCard,cardLp);
        page=authCard;
        TextView welcome=text("WELCOME TO KING",19,Color.WHITE,true);
        welcome.setGravity(Gravity.CENTER);
        TextView hint=text("Choose how you'd like to sign in",12,0xffe1cfee,false);
        hint.setGravity(Gravity.CENTER);
        button("G  Continue with Google / Gmail",0xff2569db,this::googleLogin);
        button("f  Facebook • setup required",0xff2452a8,
              ()->socialProviderSetupRequired("Facebook"));
        button("📱  Continue with Mobile Number",0xff8550ce,this::mobileLogin);
        page=outer;

        TextView terms=new TextView(this);
        terms.setText("Terms & Privacy  •  Trouble logging in?");
        terms.setTextColor(0xfff5e5f7);terms.setTextSize(12);
        terms.setGravity(Gravity.CENTER);terms.setPadding(0,dp(16),0,dp(8));
        terms.setOnClickListener(v->new android.app.AlertDialog.Builder(this)
              .setItems(new String[]{"Terms & Privacy","Trouble logging in?"},(d,w)->{
                   if(w==0)termsPrivacyPage();else troubleLoginPage();
              }).show());
        outer.addView(terms,new LinearLayout.LayoutParams(-1,-2));
        TextView legal=new TextView(this);
        legal.setText("Google sign-in ready • Mobile OTP and Facebook need secure provider setup.");
        legal.setTextColor(0xffc3b7de);legal.setTextSize(11);
        legal.setGravity(Gravity.CENTER);
        outer.addView(legal,new LinearLayout.LayoutParams(-1,-2));
    }
'''
m=m[:start]+replacement+m[end:]
main.write_text(m)
print('PASS rebuilt secure Google/Mobile/Facebook login screen with original premium royal design')

party=pkg/'PartyActivity.java'
once(party,
'''        String[] names954={"Classic","KTV","PK","Love","Game","Royal","Neon","Galaxy","Festival","Ice"};
        String[] icons954={"✨","🎤","⚔️","💗","🎮","👑","💡","🌌","🎉","❄️"};
        String[] desc954={"Clean KING room","Blue KTV stage","Red/blue battle","Warm social room","Game-night green","Premium royal violet","Electric cyan","Deep-space purple","Celebration room","Cool ice lounge"};''',
'''        String[] names954={"Classic","KTV","PK","Love","Game","Royal","Neon","Galaxy","Festival","Ice","Diamond","Sakura","Ocean","Sunset","Aurora"};
        String[] icons954={"✨","🎤","⚔️","💗","🎮","👑","💡","🌌","🎉","❄️","💎","🌸","🌊","🌇","🌈"};
        String[] desc954={"KING stage • green","Blue KTV light show","Battle spotlights","Rose pink glow","Game arena","Golden royal stage","Electric cyan","Space starfield","Celebration lights","Frozen lounge","Sparkling crystal palace","Pink sakura night","Underwater neon","Warm purple sunset","Northern sky lights"};''',
'Expose fifteen original, animated room themes in Atmosphere')

once(party,
'''        else if(live>=0)reaction=new LiveEmojiView(this,live,4000);''',
'''        else if(live>=0)reaction=new KingEmojiAuraView992(this,live,4000);''',
'Turn all 50 live emoji effects into animated neon-aura / 3D-style reactions')

once(party,
'''        LinearLayout composer = new LinearLayout(this); composer.setGravity(Gravity.CENTER_VERTICAL); composer.setPadding(dp(6),dp(7),dp(6),dp(9)); composer.setBackgroundColor(Color.WHITE); composer.setMinimumHeight(dp(70));''',
'''        LinearLayout composer = new LinearLayout(this); composer.setGravity(Gravity.CENTER_VERTICAL); composer.setPadding(dp(6),dp(7),dp(6),dp(9)); composer.setBackgroundColor(0xfffcf4ff); composer.setMinimumHeight(dp(70));''',
'Upgrade Party message composer visual without changing handlers')

once(party,
'''        TextView emoji=pill("😊",0x00ffffff,this::showLiveEmojiPanelV530); emoji.setTextColor(0xff333333); emoji.setTextSize(22); composer.addView(emoji,new LinearLayout.LayoutParams(dp(38),dp(46)));''',
'''        TextView emoji=pill("🌈",0x00ffffff,this::showLiveEmojiPanelV530); emoji.setTextColor(0xff732eb6); emoji.setTextSize(23); composer.addView(emoji,new LinearLayout.LayoutParams(dp(38),dp(46)));''',
'Highlight LIVE Emoji access with colorful composer entry')

once(party,
'''        TextView gift=pill("🎁",0x00ffffff,this::giftShopPanel); gift.setTextColor(0xff333333); gift.setTextSize(21); composer.addView(gift,new LinearLayout.LayoutParams(dp(38),dp(46)));''',
'''        TextView gift=pill("🎁",0x00ffffff,this::giftShopPanel); gift.setTextColor(0xffaf3583); gift.setTextSize(24); gift.setContentDescription("Animated cinematic Gift Shop"); composer.addView(gift,new LinearLayout.LayoutParams(dp(38),dp(46)));''',
'Highlight visual Gift Shop access while preserving real FREE gift sending')

gradle=root/'app/build.gradle'
once(gradle,
"versionCode 179; versionName '9.9.0-room-emoji-games-wallet-reliability'",
"versionCode 180; versionName '9.9.1-cinematic-gifts-themes-login'",
'Bump Android version code for install-over v9.9.0')
print('DONE KING Plus 9.9.1: richer art and visual effects, no API billing/OTP bypass')
