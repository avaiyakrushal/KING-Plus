# KING Plus 10.0.69 — production rule synchronization and Ludo validation TEST

Read-only production audit (Actions 39060946385) found deployed Firestore source
SHA256 29459a61a5d14461f808570d538a484cb79030af5f027429fbf27dafbdfe3ad0.
It was older than the current app and lacked several new collections, including
preferences/wishes and verified public prestige. Production rules are backed up
and may be replaced only if their hash still matches the audited source.

The full 69-suite Firestore run exposed 3 failures. Two fixtures used superseded
Family membership and non-atomic gift schemas; update them to the current schemas
without weakening rules. Ludo had a real missing state-machine validator.

Ludo now validates player slot ownership, readiness, host-only start/cancel/rematch,
turns, per-piece movement, safe-square/capture rules, exact home, winner, timeout,
owner transfer and immutable match bindings. Match lists are private and active
boards are visible only to players. Lobby code discovery remains available.
Server-time SHA256 die derivation uses nibble rejection, reproducible in Android
and Firestore rules. A client cannot simply submit its chosen die. This is a free
practice game; it is not a cryptographic random beacon or cash-prize system.
Upgrade to v10.0.69 is required for the new die derivation.

Validation must include all 69 existing Firestore suites, Java die vectors,
Android compilation and signer comparison before publishing new production rules.
The deployment takes no payment, does not enable APIs/billing/subscriptions and
writes no user documents. It verifies the applied rules by rereading their hash.

Still not full BoloHi parity. Authenticated voice/video, production gift/payment/VIP
services, Storage configuration and real Android two-phone acceptance remain
separate, unresolved gates. Cloud Build/Artifact Registry were DISABLED and several
production inventories returned 403; that denial is not evidence that billing is off.
