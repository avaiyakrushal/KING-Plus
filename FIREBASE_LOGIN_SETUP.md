# KING Plus login setup

## Mobile OTP
1. Firebase Console > Authentication > Sign-in method > Phone: Enable.
2. Firebase Console > Project settings > Android app `com.kingplus.social`: add the SHA-1 and SHA-256 of the key that signs the APK.
3. Download a fresh `google-services.json` after changing Firebase settings and replace `app/google-services.json`.
4. Build/install the APK again. Test with a real mobile number in E.164 format, such as `+91xxxxxxxxxx`.

## Google login
1. Firebase Console > Authentication > Sign-in method > Google: Enable.
2. Add the signing SHA-1/SHA-256 to the Android app in Firebase.
3. Ensure Firebase/Google Cloud creates a Web OAuth 2.0 client for the project.
4. Download a fresh `google-services.json`. It must generate `default_web_client_id` during the Android build.

The current `google-services.json` in this source has an empty `oauth_client` array, so Google sign-in cannot complete until a fresh Firebase config contains the required OAuth client configuration.

## Facebook login
Real Facebook login requires Meta developer credentials. Create/configure a Meta app, enable Facebook Login, then add its App ID and App Secret under Firebase Authentication > Facebook. Do not put the App Secret directly in Android source code.
