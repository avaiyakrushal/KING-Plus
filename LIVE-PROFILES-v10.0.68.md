# KING Plus 10.0.68 — Live profiles and message history TEST

Continues the uploaded BoloHi 5.30.5 APK static-layout comparison recorded in
BOLO-COMPARISON-v10.0.67.md. The reference was not installed or run.

| Gap found in KING | Change |
|---|---|
| Another person's profile only loaded once | Foreground Firestore subscription updates name/photo/bio, retains scroll and restarts on return. Cached/offline status is explicit; missing server profiles do not display an invented identity. |
| Chat identity stayed stale after another phone edited it | Foreground live peer subscription updates name and photo, including cleared photo. |
| Messages older than the latest 100 were unreachable | Bounded 100-message cursor pages with Older, Newer and Latest actions. New messages cannot replace an open history page. Returning to Latest uses the live window. |
| Chat gift history ended after 100 messages | Older pages and return to Latest are available even if a page contains no gifts. These are direct-chat gift cards; paid room gift ledger remains separate. |
| Background listeners could mark unseen messages read | Chat listeners stop when hidden; receipt policy requires foreground, server confirmation, latest page and scroll near bottom. Reading history does not acknowledge unseen live messages. |
| Discover/Chat decoded arbitrary full-size remote profile pictures | Shared bounded two-worker HTTPS thumbnail loader limits bytes to 3 MiB and decoded size to 640px, checks account/view identity and recycles rejected images. Public Profile uses the same loader. |
| Chat controls could overlap system navigation or keyboard | System/IME insets and reduced tool strips while typing; history controls are native Android XML. |

Validation: 265-message Firestore emulator history with tied timestamps, older gifts,
repeatable page cursors, outsider read denial and immutable messages; cross-account live
profile edits; Java read-receipt policy scenarios; previous Home/Me/Party/gift/voice
regressions; full Android compilation and same signing certificate.

TEST only. Real Android visual/lifecycle/keyboard QA remains pending. Emulator tests
verify backend semantics and pure Java policy, not actual two-phone rendering. This
is not complete BoloHi visual/function parity. Live authenticated voice, paid gifts,
recharge and VIP production activation, Firebase production rules deployment and
provider setup remain pending. Cloudflare stays disabled; no paid service is activated.
