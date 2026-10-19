# KING Plus v10.0.69 — TEST source

This source bundle is for `com.kingplus.social`, Android version code `348` and
version name `10.0.69-server-game-rules`.

## Scope and validation

The bundle includes the Android app, Firebase rules/functions, source checks, and
the v10.0.69 server game rules work. The Bolo Hi static comparison is in
`BOLO-COMPARISON-v10.0.69-REVIEW.md`.

Run the non-game source checks from the project root:

```sh
python3 tools/four-pages-v10048/check_source.py
python3 tools/party-gifts-me-v10064/check_source.py
python3 tools/home-me-party-v10067/check_source.py
```

These source checks do not establish Android rendering, live Firebase behavior,
or two-phone acceptance. The release gate state is recorded in
`release/final-release-evidence.json`.

## Build requirements

The project uses Java 17, Gradle 8.9, and Android SDK 35. This ZIP has no Gradle
wrapper, APK/AAB, production signing key, or configured CI debug keystore. A
signed build requires an authorized signing setup. See `BUILD-NOTES-READ-ME.txt`
for the build status and `LUDO-PRODUCTION-v10.0.69.md` for the game validation
notes.

This is TEST source, not a final production release. Live provider setup,
production deployment, and physical two-phone testing remain acceptance gates.
