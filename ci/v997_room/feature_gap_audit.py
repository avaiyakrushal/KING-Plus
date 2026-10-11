#!/usr/bin/env python3
from pathlib import Path
import re
root=Path('/tmp/v997/app/src/main/java/com/kingplus/social')
javas={f.name:f.read_text(errors='ignore') for f in root.glob('*.java')}
print('SOURCE_CLASSES',len(javas))
categories={
 "login_google":['GoogleSignIn','GoogleAuthProvider','signInWithCredential'],
 "login_facebook":['FacebookAuthProvider','LoginManager','com.facebook'],
 "login_otp":['PhoneAuthProvider','verifyPhoneNumber','smsCode'],
 "direct_chat":['direct_threads','Private Chat','openPrivateChat'],
 "user_id_search":['whereEqualTo("publicId"','public_profiles','searchButton'],
 "follow":['"follows"','Follow Back','Following'],
 "party_room":['"live_rooms"','createRoom','roomId'],
 "moderator":['cohost','administratorDialog','roomModerator'],
 "ban_mute":['banUser','muteAll','kickUser'],
 "board_announcement":['roomBillboardDialog','editAnnouncement'],
 "family_club":['family','familyOwner','familyCode'],
 "karaoke_ktv":['ktvQueuePanel','songRequest','Song request'],
 "live_video":['LiveStream','Camera2','RTMP','rtmp://'],
 "video_call":['videoCall','VideoCall','Multi Video'],
 "ludo":['OnlineLudoActivity','openPartyLudo','ludo'],
 "multiplayer":['RoomGameActivity','game_moves','game_ready'],
 "werewolf_spy_draw":['werewolf','spy','draw'],
 "minigames":['sheep','zoo','bingo','dice'],
 "emojis":['KingEmojiAccess993','showLiveEmojiPanelV530','120 ACTION EMOJI'],
 "animated_gifts":['KingGiftArtView','KingGiftSound993','giftHero994'],
 "vip_recharge":['LIVE_RECHARGE','PAID_VIP_ACTIVE','billingClient'],
 "noble_level":['nobleLevel','Noble','noble'],
 "progress_xp":['LevelSystem','xp','levelTier'],
 "3d_world":['UnityPlayer','Sceneform','GLSurfaceView','ThreeD'],
 "virtual_avatar":['Avatar3D','avatar editor','avatarFrame'],
 "real_money":['BillingClient','purchaseToken','Google Play Billing'],
 "events":['event','Weekly Gift Card','Room ranking'],
 "notifications":['FirebaseMessagingService','NotificationChannel','notification'],
 "report_block":['Report','blockUser','room_bans'],
 "themes":['roomTheme','KingRoomStageView','theme'],
 "random_matching":['randomMatch','matchmaking'],
 "official_channels":['Official Channel','official_rooms','officialChannels']
}
for key,patterns in categories.items():
 hit=[]
 for pattern in patterns:
  files=[name for name,content in javas.items() if pattern.lower() in content.lower()]
  hit.append(f"{pattern}: {len(files)} files"+(" ("+",".join(files[:4])+")" if files else ""))
 print('FEATURE',key,' | '.join(hit))
print('APP_SPECIFIC')
for name in ['PartyActivity.java','RoomGameActivity.java','KingGiftArtView.java','KingPublicProfileActivity.java','KingRecharge974Activity.java','KingPremiumAccess981.java']:
 if name not in javas:continue
 c=javas[name]
 for pattern in ['LIVE_RECHARGE = false','PAID_VIP_ACTIVE = false','88 gifts','120 ACTION EMOJI','giftVipRequired','Noble','Administrator','playInRoom','vipPoints','game_ready','JitsiMeet','Multi Video','3D']:
  if pattern.lower() in c.lower():print('EVIDENCE',name,pattern)
