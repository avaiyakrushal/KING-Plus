# KING Plus v9.9.35 — Original drawable design library (TEST)

## Precise resource target
The previous verified signed v9.9.34 APK has **1,093** compiled `res/drawable*` resources. This independently generated artwork package adds **7,587** XML drawable resources: **9 KING feature families × 3 formats × 281 palettes = 7,587**. The signed v9.9.35 TEST APK must have exactly **8,680** compiled `res/drawable*` resources (the CI step counts the ZIP entries and fails otherwise).

## Real user-facing implementation
- Nine categories: party, mic, avatar, gift, VIP, games, medal, profile, entrance.
- Three distinct graphics for every style: a feature-specific original pictogram `<vector>`, a readable rounded linear-gradient `<shape>` panel and a radial aura panel; 281 deterministic palettes and varying line/rounding treatments.
- In-app **Settings → KING Drawable Studio** browses all styles and displays the actual vector, rounded card and aura by resource lookup. The user can opt into using the selected graphic as the KING menu hero background; selection persists in local preferences and is used in the app’s real screens. Existing navigation offers a link to the corresponding KING feature. Uses accessible image descriptions and white-on-dark text for selected custom hero.
- Existing room, social, original games, gift and payment-protection code retained; signed debug release upgrade check and verified Play receipt unit tests in CI. This is not a production release; paid recharge remains OFF.

## Key limitation, important for honest parity reporting
**8,680 drawable resources do NOT mean that 8,680 separate game/animation/function assets or Bolo Hi's proprietary icons were copied.** This is a mathematically exact resource count reached with independent KING Plus imagery and genuine reusable UI themes. To create equivalent visual quality and functionality, concrete screens, 3D models and verified end-to-end flows still need real feature work and device acceptance. The Bolo Hi APK has third-party bundled resources and a Unity engine; a count alone is an unreliable similarity score.

## Reproduce
```sh
python3 tools/drawables-v9935/generate.py /path/to/checksum-verified-v9.9.34-full-source
gradle :app:assembleDebug
```
No Bolo Hi extracted proprietary assets, licensed icons, or code is included. Source generator must fail rather than modify an unexpected release base.