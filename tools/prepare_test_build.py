from pathlib import Path
import re

MAIN = Path('app/src/main/java/com/kingplus/social/MainActivity.java')
BUILD = Path('app/build.gradle')

src = MAIN.read_text(encoding='utf-8')

# Login screen: clearly label test/mobile mode and make help/legal actions clickable.
src = src.replace(
    'button("📱  Continue with Mobile Number", PURPLE, this::mobileLogin);\n'
    '        text("Terms & Privacy  •  Trouble logging in?", 13, MUTED, false);\n'
    '        text("Mobile login now uses Firebase SMS OTP. Google login is Firebase-ready. Facebook still needs Meta App credentials.", 12, MUTED, false);',
    'button("📱  Continue with Mobile Number • TEST", PURPLE, this::mobileLogin);\n'
    '        button("Terms & Privacy", CARD, this::termsPrivacyPage);\n'
    '        button("Trouble logging in?", CARD, this::troubleLoginPage);\n'
    '        text("FREE TEST MODE: Mobile login does not send SMS. Use OTP 123456. Google/Facebook still require provider setup.", 12, MUTED, false);'
)

# Replace the production phone-login entry point with a billing-free local test login.
start = src.index('    private void mobileLogin() {')
end = src.index('    private String normalizePhone', start)
replacement = '''    private void mobileLogin() {
        final EditText phone = new EditText(this);
        phone.setHint("Mobile number, e.g. +919876543210");
        phone.setSingleLine(true);
        phone.setInputType(InputType.TYPE_CLASS_PHONE);
        new AlertDialog.Builder(this)
            .setTitle("Mobile login • FREE TEST MODE")
            .setMessage("No SMS will be sent. Enter any valid mobile number, then use OTP 123456.")
            .setView(phone)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Continue", (d,w) -> {
                String number = normalizePhone(phone.getText().toString());
                if (number == null) {
                    Toast.makeText(this, "Enter a valid mobile number", Toast.LENGTH_SHORT).show();
                    return;
                }
                showTestOtpDialog(number);
            }).show();
    }
    private void showTestOtpDialog(String number) {
        final EditText otp = new EditText(this);
        otp.setHint("Test OTP: 123456");
        otp.setSingleLine(true);
        otp.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        new AlertDialog.Builder(this)
            .setTitle("Verify • TEST MODE")
            .setMessage("No SMS was sent. Use test OTP 123456 for " + number + ".")
            .setView(otp)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Verify", (d,w) -> {
                String code = otp.getText().toString().trim();
                if (!"123456".equals(code)) {
                    Toast.makeText(this, "Wrong test OTP. Use 123456", Toast.LENGTH_LONG).show();
                    return;
                }
                String shortNumber = number.length() > 4 ? "KING " + number.substring(number.length() - 4) : "KING User";
                saveLocalSession(shortNumber, "Mobile Test");
            }).show();
    }
'''
src = src[:start] + replacement + src[end:]

# Add legal/help pages once.
marker = '    private boolean ensureFirebaseReady() {'
if 'private void termsPrivacyPage()' not in src:
    helpers = '''    private void termsPrivacyPage() {
        screen = "terms";
        base("Terms & Privacy", "KING Plus test build");
        text("Terms", 20, Color.WHITE, true);
        text("Use KING Plus respectfully. Do not post illegal, abusive, deceptive or harmful content. Test coins, gifts, ranks and rewards have no cash value.", 15, MUTED, false);
        text("Privacy", 20, Color.WHITE, true);
        text("This test build stores local-mode chat and profile activity on this device. Firebase is used for enabled real-account cloud features.", 15, MUTED, false);
        button("Back to Login", PURPLE, this::login);
    }
    private void troubleLoginPage() {
        screen = "login_help";
        base("Trouble logging in?", "KING Plus sign-in help");
        text("Mobile • FREE TEST MODE", 18, Color.WHITE, true);
        text("Enter a valid mobile number and use OTP 123456. No SMS is sent and no billing is required.", 15, MUTED, false);
        text("Google", 18, Color.WHITE, true);
        text("Google error 10 means the installed APK signing SHA-1 is not registered for the Firebase/Google OAuth Android client.", 15, MUTED, false);
        text("Facebook", 18, Color.WHITE, true);
        text("Facebook sign-in stays disabled until Meta App credentials and the Firebase Facebook provider are configured.", 15, MUTED, false);
        button("Back to Login", PURPLE, this::login);
    }

'''
    src = src.replace(marker, helpers + marker)

# Give Google status 10 a useful explanation instead of only a numeric error.
old_google = '            } catch (ApiException e) { Toast.makeText(this, "Google sign-in cancelled/failed: " + e.getStatusCode(), Toast.LENGTH_LONG).show(); }'
new_google = '''            } catch (ApiException e) {
                if (e.getStatusCode() == 10) {
                    new AlertDialog.Builder(this).setTitle("Google sign-in setup")
                        .setMessage("Error 10: this APK signing SHA-1 is not registered in Firebase/Google OAuth yet. Mobile TEST login is available now.")
                        .setPositiveButton("OK", null).show();
                } else {
                    Toast.makeText(this, "Google sign-in cancelled/failed: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
                }
            }'''
src = src.replace(old_google, new_google)

src = src.replace(
    'Use a real phone number with country code. Firebase Phone Authentication must be enabled and SHA fingerprints configured.',
    'FREE TEST MODE: enter a valid phone number and use OTP 123456. No SMS is sent. Production SMS OTP can be restored after Firebase provider setup.'
)

# BoloHi-style Messages tab: launch the dedicated inbox and conversation activities.
if 'startActivity(new Intent(this, InboxActivity.class));' not in src:
    start = src.index('    private void messages() {')
    end = src.index('\n    private void addBottomNav', start)
    src = src[:start] + '''    private void messages() {
        screen="messages";
        stopMic();
        startActivity(new Intent(this, InboxActivity.class));
    }
''' + src[end:]

if 'new Intent(this, ChatActivity.class)' not in src:
    start = src.index('    private void conversation(String who){')
    end = src.index('\n    private void momentsPage()', start)
    src = src[:start] + '''    private void conversation(String who){
        Intent i = new Intent(this, ChatActivity.class);
        i.putExtra("peerName", who);
        startActivity(i);
    }
''' + src[end:]

MAIN.write_text(src, encoding='utf-8')

gradle = BUILD.read_text(encoding='utf-8')
gradle = re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 43; versionName '3.1.0'", gradle)
BUILD.write_text(gradle, encoding='utf-8')

print('Prepared KING Plus v3.1.0 no-billing test OTP + BoloHi-style chat build')
