from pathlib import Path
import re

MAIN = Path('app/src/main/java/com/kingplus/social/MainActivity.java')
BUILD = Path('app/build.gradle')
CLOUD = Path('app/src/main/java/com/kingplus/social/CloudSync.java')

src = MAIN.read_text(encoding='utf-8')

# Restore an existing Firebase/Google session before deciding whether to show Login.
needle = '        if (displayName.isEmpty()) login(); else home();\n'
replacement = '        restoreFirebaseGoogleSession();\n        if (displayName.isEmpty()) login(); else home();\n'
if needle not in src:
    raise SystemExit('Login decision point changed')
src = src.replace(needle, replacement, 1)

# Add a Firebase session restore helper before the Google login entry point.
marker = '    private void googleLogin() {\n'
helper = r'''    private void restoreFirebaseGoogleSession() {
        if (firebaseAuth == null) return;
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) return;
        String name = user.getDisplayName();
        if (name == null || name.trim().isEmpty()) {
            String email = user.getEmail();
            if (email != null && email.contains("@")) name = email.substring(0, email.indexOf('@'));
        }
        if (name == null || name.trim().isEmpty()) name = "KING User";
        displayName = name.trim();
        SharedPreferences.Editor e = getPreferences(0).edit()
            .putString("name", displayName)
            .putString("login_provider", "Google/Firebase")
            .putString("firebase_uid", user.getUid());
        if (user.getEmail() != null) e.putString("google_email", user.getEmail());
        if (user.getPhotoUrl() != null) e.putString("google_photo_url", user.getPhotoUrl().toString());
        e.apply();
    }

'''
if 'private void restoreFirebaseGoogleSession()' not in src:
    if marker not in src:
        raise SystemExit('googleLogin marker changed')
    src = src.replace(marker, helper + marker, 1)

# Replace Google login with a fresh account-picker flow. This avoids stale cached credentials
# while keeping Firebase as the authoritative signed-in session.
start = src.index('    private void googleLogin() {')
end = src.index('    private void firebaseAuthWithGoogle(', start)
new_google = r'''    private void googleLogin() {
        if (!ensureFirebaseReady()) return;
        int id = getResources().getIdentifier("default_web_client_id", "string", getPackageName());
        if (id == 0) {
            new AlertDialog.Builder(this).setTitle("Google login setup")
                .setMessage("Google OAuth client ID is missing from this build. Check app/google-services.json.")
                .setPositiveButton("OK", null).show();
            return;
        }
        String webClientId = getString(id);
        if (webClientId == null || webClientId.trim().isEmpty()) {
            new AlertDialog.Builder(this).setTitle("Google login setup")
                .setMessage("Google OAuth web client ID is empty in this build.")
                .setPositiveButton("OK", null).show();
            return;
        }
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .build();
        googleSignInClient = GoogleSignIn.getClient(this, options);
        // Sign out only from the Google picker cache when the user explicitly taps Google login.
        // The Firebase session remains authoritative and will be recreated after account selection.
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            if (isFinishing() || isDestroyed()) return;
            startActivityForResult(googleSignInClient.getSignInIntent(), GOOGLE_SIGN_IN_REQUEST);
        });
    }
'''
src = src[:start] + new_google + src[end:]

# Replace Firebase credential completion with real profile persistence + non-blocking cloud sync.
start = src.index('    private void firebaseAuthWithGoogle(')
end = src.index('    private void mobileLogin()', start)
new_auth = r'''    private void firebaseAuthWithGoogle(String idToken, String fallbackName) {
        if (firebaseAuth == null || idToken == null || idToken.trim().isEmpty()) {
            Toast.makeText(this, "Google token is unavailable. Please try again.", Toast.LENGTH_LONG).show();
            return;
        }
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (!task.isSuccessful()) {
                String reason = task.getException() == null ? "Unknown Firebase error" : task.getException().getLocalizedMessage();
                new AlertDialog.Builder(this).setTitle("Google login failed")
                    .setMessage(reason == null ? "Firebase could not sign in this Google account." : reason)
                    .setPositiveButton("Try again", (d,w) -> googleLogin())
                    .setNegativeButton("Cancel", null).show();
                return;
            }
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Google account connected but Firebase user is unavailable.", Toast.LENGTH_LONG).show();
                return;
            }
            String name = user.getDisplayName();
            if (name == null || name.trim().isEmpty()) name = fallbackName;
            if ((name == null || name.trim().isEmpty()) && user.getEmail() != null && user.getEmail().contains("@"))
                name = user.getEmail().substring(0, user.getEmail().indexOf('@'));
            if (name == null || name.trim().isEmpty()) name = "KING User";
            displayName = name.trim();

            SharedPreferences.Editor e = getPreferences(0).edit()
                .putString("name", displayName)
                .putString("login_provider", "Google")
                .putString("firebase_uid", user.getUid());
            if (user.getEmail() != null) e.putString("google_email", user.getEmail());
            if (user.getPhotoUrl() != null) e.putString("google_photo_url", user.getPhotoUrl().toString());
            e.apply();

            try { PushNotifications.refreshToken(); } catch (Exception ignored) { }
            try {
                CloudSync.syncProfileAndTestWallet(this, getPreferences(0), displayName,
                    coinBalance, giftCount, receivedGiftCount,
                    (ok,message) -> runOnUiThread(() -> {
                        if (!ok) Toast.makeText(this, "Google login successful • cloud sync pending", Toast.LENGTH_SHORT).show();
                    }));
            } catch (Exception ignored) { }
            Toast.makeText(this, "Google login successful", Toast.LENGTH_SHORT).show();
            home();
        });
    }
'''
src = src[:start] + new_auth + src[end:]

# Harden the Google activity-result handling and surface common configuration errors.
pattern = re.compile(r'''        if \(requestCode == GOOGLE_SIGN_IN_REQUEST\) \{.*?\n            return;\n        \}\n''', re.S)
m = pattern.search(src)
if not m:
    raise SystemExit('Google onActivityResult block changed')
new_result = r'''        if (requestCode == GOOGLE_SIGN_IN_REQUEST) {
            if (data == null) {
                Toast.makeText(this, "Google sign-in cancelled", Toast.LENGTH_SHORT).show();
                return;
            }
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account == null || account.getIdToken() == null) {
                    Toast.makeText(this, "Google account did not return a Firebase token", Toast.LENGTH_LONG).show();
                    return;
                }
                String fallback = account.getDisplayName();
                if ((fallback == null || fallback.trim().isEmpty()) && account.getEmail() != null && account.getEmail().contains("@"))
                    fallback = account.getEmail().substring(0, account.getEmail().indexOf('@'));
                firebaseAuthWithGoogle(account.getIdToken(), fallback == null ? "Google User" : fallback);
            } catch (ApiException e) {
                int code = e.getStatusCode();
                String message;
                if (code == 10) message = "Google error 10: this APK signing SHA-1 is not registered for the Android OAuth client.";
                else if (code == 7) message = "Network error while contacting Google. Check internet and try again.";
                else if (code == 12501) message = "Google sign-in was cancelled.";
                else message = "Google sign-in failed. Error code: " + code;
                new AlertDialog.Builder(this).setTitle("Google sign-in")
                    .setMessage(message)
                    .setPositiveButton("Try again", (d,w) -> googleLogin())
                    .setNegativeButton("Close", null).show();
            }
            return;
        }
'''
src = src[:m.start()] + new_result + src[m.end():]

# Login copy: make it explicit that Google/Gmail is live, while test mobile remains separate.
src = src.replace('button("G  Continue with Google", 0xff4285f4, this::googleLogin);',
                  'button("G  Continue with Google / Gmail", 0xff4285f4, this::googleLogin);')
src = src.replace('FREE TEST MODE: Mobile login does not send SMS. Use OTP 123456. Google/Facebook still require provider setup.',
                  'Google / Gmail uses real Firebase Authentication. Mobile stays in FREE TEST mode with OTP 123456. Facebook still requires Meta provider setup.')

MAIN.write_text(src, encoding='utf-8')

# Add authenticated identity metadata to Firestore profile sync.
cloud = CLOUD.read_text(encoding='utf-8')
needle = '        profile.put("displayName", displayName);\n'
extra = '''        profile.put("displayName", displayName);\n        profile.put("uid", user.getUid());\n        if (user.getEmail() != null) profile.put("email", user.getEmail());\n        if (user.getPhotoUrl() != null) profile.put("photoUrl", user.getPhotoUrl().toString());\n        profile.put("authProvider", "firebase");\n'''
if needle in cloud and 'profile.put("authProvider", "firebase")' not in cloud:
    cloud = cloud.replace(needle, extra, 1)
CLOUD.write_text(cloud, encoding='utf-8')

gradle = BUILD.read_text(encoding='utf-8')
gradle = re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 63; versionName '4.2.0'", gradle)
BUILD.write_text(gradle, encoding='utf-8')

print('Prepared KING Plus v4.2.0 real Google/Gmail Firebase login')
