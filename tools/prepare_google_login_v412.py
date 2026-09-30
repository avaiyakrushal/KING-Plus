from pathlib import Path
import re

MAIN = Path('app/src/main/java/com/kingplus/social/MainActivity.java')
BUILD = Path('app/build.gradle')

src = MAIN.read_text(encoding='utf-8')

old = '''    private void firebaseAuthWithGoogle(String idToken, String fallbackName) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(this, "Google sign-in failed: " + (task.getException() == null ? "Unknown error" : task.getException().getMessage()), Toast.LENGTH_LONG).show();
                return;
            }
            FirebaseUser user = firebaseAuth.getCurrentUser();
            String name = user != null && user.getDisplayName() != null && !user.getDisplayName().trim().isEmpty() ? user.getDisplayName() : fallbackName;
            saveLocalSession(name, "Google");
        });
    }
'''
new = '''    private void firebaseAuthWithGoogle(String idToken, String fallbackName) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (!task.isSuccessful()) {
                String reason = task.getException() == null ? "Unknown error" : task.getException().getMessage();
                new AlertDialog.Builder(this).setTitle("Google sign-in failed")
                    .setMessage(reason == null ? "Unknown error" : reason)
                    .setPositiveButton("OK", null).show();
                return;
            }
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Google account connected but Firebase user is unavailable", Toast.LENGTH_LONG).show();
                return;
            }
            String name = user.getDisplayName();
            if (name == null || name.trim().isEmpty()) name = fallbackName;
            if (name == null || name.trim().isEmpty()) name = "KING " + publicId(user.getUid());
            name = name.trim();

            SharedPreferences.Editor edit = getPreferences(0).edit()
                .putString("name", name)
                .putString("login_provider", "Google")
                .putString("firebase_uid", user.getUid());
            if (user.getEmail() != null) edit.putString("email", user.getEmail());
            if (user.getPhotoUrl() != null) edit.putString("profile_photo", user.getPhotoUrl().toString());
            edit.apply();

            displayName = name;
            PushNotifications.refreshToken();
            syncPublicProfile();
            CloudSync.syncProfileAndTestWallet(this, getPreferences(0), displayName, coinBalance, giftCount, receivedGiftCount,
                (ok,message) -> runOnUiThread(() -> {
                    Toast.makeText(this, ok ? "Google account connected" : message, Toast.LENGTH_LONG).show();
                    home();
                }));
        });
    }
'''
if old not in src:
    raise SystemExit('Google auth template changed')
src = src.replace(old, new, 1)

old_err = '''                if (e.getStatusCode() == 10) {
                    new AlertDialog.Builder(this).setTitle("Google sign-in setup")
                        .setMessage("Error 10: this APK signing SHA-1 is not registered in Firebase/Google OAuth yet. Mobile TEST login is available now.")
                        .setPositiveButton("OK", null).show();
                } else {
'''
new_err = '''                if (e.getStatusCode() == 10) {
                    new AlertDialog.Builder(this).setTitle("Google sign-in configuration")
                        .setMessage("Error 10 (DEVELOPER_ERROR). Confirm this APK SHA-1 is registered in Firebase Project Settings and Google provider is enabled, then try again.")
                        .setPositiveButton("OK", null).show();
                } else {
'''
if old_err in src:
    src = src.replace(old_err, new_err, 1)

# If a real Firebase Google user already exists, always refresh local identity fields at startup.
needle = '''        if (firebaseAuth != null && firebaseAuth.getCurrentUser() != null) {
            FirebaseUser signedInUser = firebaseAuth.getCurrentUser();
            if (displayName.isEmpty()) {
                String restored = signedInUser.getDisplayName();
                if (restored == null || restored.trim().isEmpty()) restored = "KING " + publicId(signedInUser.getUid());
                displayName = restored.trim();
                getPreferences(0).edit().putString("name", displayName).putString("login_provider", "Firebase").apply();
            }
            PushNotifications.refreshToken();
            syncPublicProfile();
        }
'''
replace = '''        if (firebaseAuth != null && firebaseAuth.getCurrentUser() != null) {
            FirebaseUser signedInUser = firebaseAuth.getCurrentUser();
            String restored = signedInUser.getDisplayName();
            if (restored == null || restored.trim().isEmpty()) restored = displayName;
            if (restored == null || restored.trim().isEmpty()) restored = "KING " + publicId(signedInUser.getUid());
            displayName = restored.trim();
            SharedPreferences.Editor authEdit = getPreferences(0).edit()
                .putString("name", displayName)
                .putString("firebase_uid", signedInUser.getUid());
            if (signedInUser.getEmail() != null) authEdit.putString("email", signedInUser.getEmail());
            if (signedInUser.getPhotoUrl() != null) authEdit.putString("profile_photo", signedInUser.getPhotoUrl().toString());
            authEdit.apply();
            PushNotifications.refreshToken();
            syncPublicProfile();
        }
'''
if needle not in src:
    raise SystemExit('Firebase restore template changed')
src = src.replace(needle, replace, 1)

MAIN.write_text(src, encoding='utf-8')

gradle = BUILD.read_text(encoding='utf-8')
gradle = re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 63; versionName '4.1.2'", gradle)
BUILD.write_text(gradle, encoding='utf-8')
print('Prepared KING Plus v4.1.2 Google login finalization')
