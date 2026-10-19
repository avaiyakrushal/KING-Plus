# KING Plus v10.0.73 — TEST source

This source bundle is for `com.kingplus.social`, Android version code `350` and
version name `10.0.73-party-cleanup-race-fix`.

## Latest change

The Android 16 crash report showed `ImageView` lazily opening a document URI
without a current provider grant. App image views now use a bounded eager decoder
that catches revoked/missing URI access and leaves a fallback instead of failing
during layout. Party search now spans all room categories and keeps cursor paging
for additional results. Party cleanup now rechecks member presence and removes
stale membership and seats in a transaction, so a reconnect racing with cleanup
is not ejected based on an old snapshot. Details are in `RELEASE-NOTES-v10.0.73.md`.

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

This is TEST source. Physical-device rendering, live service behavior and
two-device acceptance remain unverified.
