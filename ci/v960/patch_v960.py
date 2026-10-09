"""v9.6.0: stabilize realtime Live Emoji without copying third-party assets.

Parity milestone:
 - Static previews; only dispatched effects animate (prevents up to 50 continuous software-layer renderers).
 - SVGA preview plays once instead of infinite looping.
 - Listener callback checks active room and Activity lifetime.
 - Full-screen effect uses MATCH_PARENT even before layout measurement.
 - Preserve original Party voice, auth, gift and no-billing logic.
"""
from pathlib import Path
import sys

root=Path(sys.argv[1]); pkg=root/'app/src/main/java/com/kingplus/social'
party=pkg/'PartyActivity.java'
live=pkg/'LiveEmojiView.java'
def replace(p,old,new,label):
    content=p.read_text()
    count=content.count(old)
    if count!=1:
        raise SystemExit(f'{label}: expected exactly one source marker, found {count}: {old[:110]!r}')
    p.write_text(content.replace(old,new,1))
    print('STAGE_OK',label)

replace(party,
        'art=new ReferenceEmojiView(this,ReferenceEmojiView.parse(token),true);',
        'art=new ReferenceEmojiView(this,ReferenceEmojiView.parse(token),false);',
        'SVGA selection previews run once, not forever')

replace(party,
        'art=new LiveEmojiView(this,live,0);art.setContentDescription("Send animated "+LiveEmojiView.LABELS[live]);',
        'art=new LiveEmojiView(this,live,-1);art.setContentDescription("Preview "+LiveEmojiView.LABELS[live]+"; tap to animate in the room");',
        'Static previews for all original KING Plus 50 Live Emoji')

replace(live,
        '        setLayerType(View.LAYER_TYPE_SOFTWARE,null);',
        '        if(duration>=0)setLayerType(View.LAYER_TYPE_SOFTWARE,null); // Static picker previews do not need a software layer.',
        'Avoid continuous software-layer cache for static previews')

replace(live,
        '        boolean running=duration==0||elapsed<duration;\n        float t=running?elapsed/1000f:0f;',
        '        boolean staticPreview=duration<0;\n        boolean running=!staticPreview&&(duration==0||elapsed<duration);\n        float t=staticPreview?0f:(running?elapsed/1000f:0f);',
        'Stop 33ms redraw scheduling for selection previews')

replace(party,
        '        if(emoji==null||emoji.trim().isEmpty())return;emoji=stickerFallback610(emoji);\n        if(!cloudRoom)addChatRow(sender==null?"User":sender,emoji);',
        '        if(emoji==null||emoji.trim().isEmpty()||isFinishing()||isDestroyed())return;\n        emoji=stickerFallback610(emoji);\n        if(!cloudRoom)addChatRow(sender==null?"User":sender,emoji);',
        'Ignore late emoji animations after activity exit')

replace(party,
        '        final FrameLayout stage=liveEmojiStageV530;if(stage==null)return;',
        '        final FrameLayout stage=liveEmojiStageV530;if(stage==null||stage.getParent()==null)return;',
        'Prevent animations targeting a detached room scene')

replace(party,
        'lp.width=stage.getWidth();lp.height=stage.getHeight();lp.leftMargin=0;lp.topMargin=0;',
        'lp.width=-1;lp.height=-1;lp.leftMargin=0;lp.topMargin=0;',
        'Full-screen animations fill screen before first layout pass')

replace(party,
        '        liveEmojiListenerV530=roomV530.collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(20).addSnapshotListener((snapV530,eV530) -> {\n            if(eV530!=null){toast("Live emoji sync unavailable: "+msg(eV530));return;}if(snapV530==null)return;',
        '        final String activeRoomForEmoji960=roomId;\n        final String activeUserForEmoji960=user.getUid();\n        liveEmojiListenerV530=roomV530.collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(20).addSnapshotListener((snapV530,eV530) -> {\n            if(isFinishing()||isDestroyed()||!cloudRoom||roomId==null||!activeRoomForEmoji960.equals(roomId))return;\n            if(eV530!=null){toast("Live emoji sync unavailable: "+msg(eV530));return;}if(snapV530==null)return;',
        'Discard old-room Firebase emoji events after room switch')

replace(party,
        '                if(!user.getUid().equals(docV530.getString("actorUid"))) showLiveEmojiEffect560(emojiV530,actorV530,docV530.getString("actorUid"));',
        '                if(!activeUserForEmoji960.equals(docV530.getString("actorUid"))) showLiveEmojiEffect560(emojiV530,actorV530,docV530.getString("actorUid"));',
        'Stable UID capture for live emoji listener')

gradle=root/'app/build.gradle';g=gradle.read_text()
old="versionCode 150; versionName '9.5.9-party-invite-links'"
if g.count(old)!=1:raise SystemExit('Build is not based on successfully compiled v9.5.9 source')
gradle.write_text(g.replace(old,"versionCode 151; versionName '9.6.0-live-emoji-stability'",1))
print('PASS', 'v9.6.0 Android app version')
