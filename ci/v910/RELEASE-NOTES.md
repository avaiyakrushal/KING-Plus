# KING Plus v9.1.0 — Production Completion

Continues the requested 1–12 completion list on top of v9.0.0.

- Login: Mobile Number now offers real Firebase SMS OTP and the existing FREE TEST OTP 123456. Facebook/WhatsApp OTP are never faked; diagnostics show whether provider configuration is present.
- Stability: installs an additional local crash/non-fatal diagnostics log with Help Center share/clear actions.
- Party Room: owner periodically cleans members/seats stale for more than 5 minutes; heartbeat/join failures are recorded as non-fatal diagnostics.
- KTV: Finish & Save creates KTV history entries; KTV History can be viewed from the stage.
- PK: Finish + Result calculates Red/Blue scores, stores a shared pk_result event and exposes PK History.
- Family: daily check-in uses a deterministic per-user/per-day document so repeated check-ins cannot silently create unlimited activity entries.
- Match: adds public profile search by display name or 6-digit KING ID.
- Existing v9.0.0 multi-device room heartbeat, Jitsi voice launch, gift/emoji sync, game membership self-heal, profile sync and Online Diagnostics remain.
- No billing remains unchanged.

External blockers that cannot be solved by APK source alone:
- Firebase production Security Rules deployment is still blocked by Google IAM 403. The service account needs Service Usage access and Firebase Rules Admin.
- Facebook sign-in needs Meta App credentials/Firebase provider configuration.
- WhatsApp OTP needs an approved WhatsApp/OTP provider and secure backend.
