# KING Plus v10.0.71 — Party search parity (TEST)

- Party room text search now searches across every category, even when the user opened the search dialog from a specific category tab.
- Search retains the existing 50-room cursor pagination. The user can move through more pages to find a room; there is no deployed full-text index, so matches are not returned from the entire database in one request.
- Keeps the Android 16 media URI crash fix: app-owned image views eagerly decode bounded local document images and fall back safely when access is unavailable.
- Version code 350; version name `10.0.71-global-party-search`.

Source regression checks and the Android CI build pass. Cross-category search has not been accepted on a physical phone with live room data. Search results are still subject to Firebase's live-room data and rules.
