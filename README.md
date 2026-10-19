# KING Plus v10.0.70 — TEST source

This source bundle is for `com.kingplus.social`, Android version code `349` and
version name `10.0.70-android16-media-crash-fix`.

## Latest change

The Android 16 crash report showed `ImageView` lazily opening a document URI
without a current provider grant. App image views now use a bounded eager decoder
that catches revoked/missing URI access and leaves a fallback instead of failing
during layout. Party exit also clears room listeners and releases membership before
returning to the lobby. Details are in `RELEASE-NOTES-v10.0.70.md`.

## Validation

Run the source-level checks from the project root:

```sh
python3 tools/android16-media-uri-v10070/check_source.py
python3 tools/four-pages-v10048/check_source.py
python3 tools/party-gifts-me-v10064/check_source.py
python3 tools/home-me-party-v10067/check_source.py
```

Source checks do not establish Android rendering, signature validity, or
crash-free behavior on a physical device. The release gate state is recorded in
`release/final-release-evidence.json`.

## Build status

This ZIP contains source only; it has no Gradle wrapper, APK/AAB, production
signing key, or configured CI debug keystore. Building a signed APK requires an
authorized signing setup. This remains TEST source, not a verified release.
