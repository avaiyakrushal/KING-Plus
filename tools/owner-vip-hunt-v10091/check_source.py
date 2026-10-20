from pathlib import Path
root=Path(__file__).resolve().parents[2]
style=(root/'app/src/main/java/com/kingplus/social/KingVipIdentityStyle.java').read_text()
shop=(root/'app/src/main/java/com/kingplus/social/KingVipIdentityShopActivity.java').read_text()
wallet=(root/'app/src/main/java/com/kingplus/social/KingWallet976Activity.java').read_text()
game=(root/'app/src/main/java/com/kingplus/social/GamePlayActivity.java').read_text()
menu=(root/'app/src/main/java/com/kingplus/social/KingDeepFlowActivity.java').read_text()
fn=(root/'functions/v3.js').read_text()
gifts=(root/'functions/room-gift-service.js').read_text()
gift_tests=(root/'functions/room-gift-service.test.js').read_text()
gradle=(root/'app/build.gradle').read_text()
workflow=(root/'.github/workflows/king-v10077-party-rejoin-choice.yml').read_text()
assert 'v.equals("000001")' in style
assert 'getIdToken(false)' in shop and 'token.getClaims().get("admin")' in shop
assert 'getHttpsCallable("claimOwnerVipNumber")' in shop
assert "request.auth.token?.admin!==true" in fn and "king_vip_number_registry'" in fn
assert 'unlimitedSender:request.auth.token?.admin===true' in fn
assert 'if(!unlimitedSender)tx.set(sender' in gifts and 'nonRedeemable:true' in gifts
assert 'unlimitedSender:true' in gift_tests
assert '"💎","Hunt"' in menu and 'if("hunt".equalsIgnoreCase(code))' in menu
assert 'else if("hunt".equals(game))showHunt()' in game
assert 'no wallet Diamonds' in game and 'never spends or awards Diamonds' in game
assert 'token.getClaims().get("admin")' in wallet and 'Gift sending: ∞ • Owner entitlement' in wallet
assert 'versionCode 379' in gradle and '10.0.100-party-treasure-hunt-live' in gradle
assert "versionCode='379'" in workflow and 'KING-Plus-v10.0.100-Party-Treasure-TEST.apk' in workflow
print('PASS: owner VIP and unlimited gift privileges require trusted admin claims; Hunt is local and wallet-free')
