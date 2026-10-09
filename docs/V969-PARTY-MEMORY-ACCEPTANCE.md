# KING Plus v9.6.9 — Party Room low-memory acceptance

## Grounded evidence
A prior user's vivo V2575 Android 16 process exit reported `Low memory kill`, PartyActivity/party-render, RSS ≈385 MB (approximately 376 MiB), Java heap around 16 MB. This is **not** a Java heap OOM; native/graphics/network runtime memory may dominate. The exact library causing memory pressure is unknown without on-phone profiler/Android exit trace.

## What changed in this build
- At most four live animated reaction/effect views can remain active in the Party scene. Excess older effects are cancelled and removed from their parent.
- `Activity.onTrimMemory` and `onLowMemory` clear expendable thumbnail bitmap caches and pending visual effects.
- On a **critical** memory trim, release an idle/muted Jitsi view only when `micOn == false`. Actively transmitting microphones are **not** interrupted intentionally. This may temporarily stop receiving Party audio when the user's Mic is already OFF; microphone activation can reconnect.
- Restore/retain v9.6.8 transactional Mic seat ownership, v9.6.7 presence, v9.6.6 NPE safeguards, and v9.6.5 verified social IDs.

## Verification from phone (not covered by automated build)
1. Install v9.6.9 over v9.6.8, preserving application data.
2. On a lower-memory device (vivo V2575), join Party with 3 separate accounts, leave Mic OFF, and send 10+ concurrent Live Emoji from other phones. Verify that no more than four animations visibly overlap at once and the Party remains responsive.
3. Turn Mic ON for 3 minutes and verify other phones hear it. Low-memory trim must **not** force Mic OFF if microphone is active.
4. Turn Mic OFF. Under severe OS memory pressure, idle voice may be released to save native resources; reopening the microphone should rejoin normally. If not, report which step failed.
5. Create/join/leave 10 times. Check Android crash diagnostics; note memory reason, installed version, last screen and crash time.
6. Test Follow, Party Seat Requests/Host approval, multi-phone members, Ludo, Gifts, Chat and other existing functions for regressions.

## Limits
These guards reduce avoidable UI/native pressure but cannot prove the previous `Low memory kill` is fully fixed. Native Jitsi conferencing may remain the main memory consumer; measure with Android Studio profiler or Android `dumpsys meminfo` if later available. A successful Gradle build alone is not a device acceptance test.
