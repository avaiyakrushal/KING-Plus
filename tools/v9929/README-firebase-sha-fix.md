# KING Plus Android app signing

The verified v9.9.29 TEST APK has Firebase SHA-1 registered, SHA-256 not registered as checked by GitHub Actions run 38250003329. Phone sign-in may need this SHA-256 fingerprint for Android Play Integrity. A production signer may differ from the TEST signer. Do not claim SMS OTP or Facebook production login is working until physical-device acceptance passes.
