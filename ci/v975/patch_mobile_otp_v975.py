#!/usr/bin/env python3
"""Android v9.7.5: offer real provider OTP without Firebase Phone Auth Blaze.

The actual SMS OTP and payment backend is separately deployed on Cloudflare
Workers Free (MSG91+Turnstile+Razorpay secrets remain server-only). A user can
configure their HTTPS backend endpoint in the new phone Activity. Until the
owner configures provider credentials, requests fail clearly and no fake user
is signed in. Google login is retained.
"""
from pathlib import Path
import shutil,sys
root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
shutil.copy2(Path(__file__).with_name('KingPhoneOtp975Activity.java'),
             pkg/'KingPhoneOtp975Activity.java')
main=pkg/'MainActivity.java'
s=main.read_text()
def once(old,new,name):
 global s
 n=s.count(old)
 if n!=1:raise SystemExit(f'{name}: expected one marker; got {n}: {old[:120]!r}')
 s=s.replace(old,new,1);print('PASS',name)
# The existing Firebase PhoneAuth PhoneAuthProvider on Firebase Spark cannot send
# real SMS. Keep old private code for migration but route exposed mobile buttons
# to the server-verified custom token flow.
begin=s.index('    private void mobileLogin() {')
end=s.index('    private void showTestOtpDialog(',begin)
s=s[:begin]+'''    private void mobileLogin() {
        startActivity(new Intent(this,KingPhoneOtp975Activity.class));
    }
''' +s[end:]
print('PASS Mobile login opens real phone OTP backend, not Firebase SMS/TEST code')

once(
 'text("Google uses Firebase. Mobile offers real Firebase SMS OTP plus FREE TEST OTP. Facebook/WhatsApp OTP require provider setup and are not faked.", 12, MUTED, false);',
 'text("Google login remains available. Real Mobile OTP uses a separately configured secure provider, not Firebase SMS/Blaze. WhatsApp/Facebook need approved setup.", 12, MUTED, false);',
 'correct login status: Firebase SMS/TEST is NOT offered')

once(
 'text("Use Real SMS OTP when Firebase Phone Authentication is enabled. FREE TEST OTP 123456 remains available and sends no SMS.", 15, MUTED, false);',
 'text("Mobile OTP needs a configured low-cost Cloudflare Worker and approved SMS provider. It is not a free TEST OTP, and Firebase SMS/Blaze is not required.", 15, MUTED, false);',
 'mobile help explains configured provider')

once(
 '+"Real SMS OTP code path: present"+line+"WhatsApp OTP provider: not configured"',
 '+"Secure custom-token Mobile OTP: setup needed"+line+"WhatsApp OTP provider: not configured"',
 'do not report Firebase PhoneAuth as working under Spark')

main.write_text(s)
manifest=root/'app/src/main/AndroidManifest.xml'
m=manifest.read_text()
old='<activity android:name=".KingRecharge974Activity" android:exported="false" />'
if m.count(old)!=1:raise SystemExit('Expected v9.7.4 Recharge Activity registration')
m=m.replace(old,old+'\n        <activity android:name=".KingPhoneOtp975Activity" android:exported="false" />',1)
manifest.write_text(m)
gradle=root/'app/build.gradle'
g=gradle.read_text()
old="versionCode 165; versionName '9.7.4-play-diamond-recharge-preparation'"
if g.count(old)!=1:raise SystemExit('Expected v9.7.4 compiled baseline')
gradle.write_text(g.replace(old,"versionCode 166; versionName '9.7.5-cloudflare-mobile-otp-preparation'",1))
print('PASS buildable Android v9.7.5 source prepared for secure mobile SMS OTP')
