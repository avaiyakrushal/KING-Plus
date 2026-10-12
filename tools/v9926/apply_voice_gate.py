#!/usr/bin/env python3
"""KING Plus v9.9.26: separate explicit user consent and Firebase membership verification from media loading."""
from pathlib import Path
import re,sys
root=Path(sys.argv[1])
p=root/'app/src/main/java/com/kingplus/social/VoiceWebActivity.java'
s=p.read_text()
def replace(old,new,label):
    global s
    if s.count(old)!=1:raise RuntimeError(label+' anchor count '+str(s.count(old)))
    s=s.replace(old,new,1)

replace('private WebView webView; private boolean videoMode; private String meetingUrl=""; private TextView retry;',
'''private WebView webView; private boolean videoMode; private String meetingUrl="";
    private TextView retry, consent9926; private String room9926="";
    private boolean approved9926=false,checking9926=false;''','RTC view state')
replace('String roomId=getIntent().getStringExtra("roomId");if(roomId==null||roomId.trim().isEmpty())roomId="lobby";',
'''String roomId=getIntent().getStringExtra("roomId");
        room9926=roomId==null?"":roomId.trim();''','no fake lobby call')
replace('String slug=("KINGPlus-"+roomId).replaceAll("[^A-Za-z0-9_-]","");',
        'String slug=("KINGPlus-"+room9926).replaceAll("[^A-Za-z0-9_-]","");','room slug')
s=s.replace('config.startWithVideoMuted=false','config.startWithVideoMuted=true')
s=s.replace('config.prejoinPageEnabled=false','config.prejoinPageEnabled=true')
replace('frame.addView(retry,new FrameLayout.LayoutParams(-1,-1));setContentView(frame);',
'''frame.addView(retry,new FrameLayout.LayoutParams(-1,-1));
        consent9926=new TextView(this);
        consent9926.setText("KING Plus Voice / Video\n\nTap to verify Party membership & join.\n\nMic and camera start OFF.");
        consent9926.setTextSize(18);
        consent9926.setTextColor(Color.WHITE);
        consent9926.setGravity(Gravity.CENTER);
        consent9926.setPadding(26,26,26,26);
        consent9926.setBackgroundColor(0xff160f2c);
        consent9926.setOnClickListener(v->verifyAndJoin9926());
        frame.addView(consent9926,new FrameLayout.LayoutParams(-1,-1));
        setContentView(frame);'''.replace('Voice / Video\n\nTap','Voice / Video\\n\\nTap').replace('join.\n\nMic','join.\\n\\nMic'),'explicit consent overlay')
replace('settings.setMediaPlaybackRequiresUserGesture(false);',
        'settings.setMediaPlaybackRequiresUserGesture(true);','no autoplay audio')
replace('if(online){if(!pageReady&&hasRequiredPermissions())reloadMeeting(false);}else showRetry("No internet connection");',
        'if(online){if(approved9926&&!pageReady&&hasRequiredPermissions())reloadMeeting(false);}else if(approved9926)showRetry("No internet connection");','no network autoplay')
replace('''        if(!hasRequiredPermissions())requestRuntimePermissions();
        if(KingNetwork.online(this))webView.loadUrl(meetingUrl);else showRetry("No internet connection");
    }

    private void showRetry''',
'''        // Do not start public RTC or even request mic/camera on activity creation.
    }

    private void verifyAndJoin9926(){
        if(checking9926||approved9926)return;
        checking9926=true;
        consent9926.setText("Verifying current KING Plus room membership...");
        NativeMeetBridge.verifyMembership(this,room9926,allowed->{
            checking9926=false;
            if(isFinishing()||isDestroyed())return;
            if(!allowed){
                consent9926.setText("Live room membership required.\n\nTap to retry verification.");
                return;
            }
            approved9926=true;
            consent9926.setVisibility(View.GONE);
            if(!hasRequiredPermissions())requestRuntimePermissions();
            else reloadMeeting(true);
        });
    }

    private void showRetry'''.replace('required.\n\nTap','required.\\n\\nTap'),'verify before call')
replace('private void showRetry(String reason){if(isFinishing())return;',
        'private void showRetry(String reason){if(isFinishing()||!approved9926)return;','retry gate')
replace('private void reloadMeeting(boolean manual){if(webView==null||meetingUrl.isEmpty())return;',
        'private void reloadMeeting(boolean manual){if(!approved9926||webView==null||meetingUrl.isEmpty())return;','join gate')
replace('if(hasRequiredPermissions())reloadMeeting(true);else Toast.makeText(this,',
        'if(approved9926&&hasRequiredPermissions())reloadMeeting(true);else Toast.makeText(this,','permission callback gate')
replace('if(!pageReady&&KingNetwork.online(this)&&hasRequiredPermissions())handler.postDelayed',
        'if(approved9926&&!pageReady&&KingNetwork.online(this)&&hasRequiredPermissions())handler.postDelayed','resume gate')
p.write_text(s)
assert 'NativeMeetBridge.verifyMembership(this,room9926' in s
assert 'approved9926&&!pageReady' in s
assert 'settings.setMediaPlaybackRequiresUserGesture(true)' in s
assert 'startWithVideoMuted=false' not in s
print('PASS compatibility media requires server membership and user tap before streaming')
