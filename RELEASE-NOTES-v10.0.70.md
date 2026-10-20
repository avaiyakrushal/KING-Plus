# KING Plus v10.0.70 — Android 16 media URI crash fix (TEST)

- Addresses the reported Android 16 `SecurityException` from `ImageView` lazily opening a MediaDocumentsProvider URI during layout.
- Replaces direct `ImageView.setImageURI` calls with bounded eager bitmap decoding through `ContentResolver.openInputStream`; inaccessible or revoked document grants now fall back without crashing the view measurement pass.
- Covers Party member photos/room covers, post photos, and profile gallery images; Party exit still clears room listeners and releases membership before showing the lobby.
- Version code 349. This code was built as a signed TEST APK by CI. It has not been verified on the reported vivo V2575 device. The fix is carried forward in v10.0.71.

Run `python3 tools/android16-media-uri-v10070/check_source.py` from the project root. Device reproduction and Android build verification remain required.
