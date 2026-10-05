# KING Plus v8.6.1

Based on the verified v8.5.3 source plus all v8.6.0 parity patches.

Party Room UI fixes from the supplied phone screenshots:
- Bottom message composer is reserved as a fixed 74dp bar and no longer shares the scrollable room area.
- System-bar insets and keyboard resizing are handled separately to prevent double shrinking / clipped send controls on smaller phones.
- Plus, emoji, mic, gift and send controls were compacted slightly so the message field remains usable on narrow screens.
- Successful sends clear the composer and hide the keyboard; failed cloud sends preserve the message for retry.
- Local message rendering is guarded so a vendor emoji-rendering exception cannot leave the composer stuck.
- Raw internal errors such as `length=27; index=27` are no longer exposed in the UI.
- Emoji and gift sheets use shorter bottom-sheet heights so they do not crowd the fixed composer.
- Live emoji rendering has an additional bounds-safe icon fallback.

All v8.6.0 gift catalog, live emoji/effects, playable game routing, Online Ludo integration, no-billing mode and TEST OTP behavior are preserved.

CI verifies compilation and packaging. Physical-device runtime and two-phone multiplayer testing are still separate checks.
