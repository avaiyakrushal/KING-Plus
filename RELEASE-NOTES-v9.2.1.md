# KING Plus v9.2.1 — Room Create/Open Fix

Fixes the concrete failure shown in the user's screenshots:

- Create Party now pre-fills a usable room name. A blank room name no longer makes Start Room appear to do nothing.
- Start Room shows a creating state and prevents repeated taps from producing duplicate rooms.
- Room creation now retries compatible Firestore payloads: current full schema, compact schema with joinCode, then legacy schema.
- Critical fix: when the room document is successfully created but the owner/member write rejects the rich member payload, KING Plus retries a minimal compatible member payload and still opens the Party Room.
- Heartbeat and membership self-heal also fall back to a compact member schema for older deployed Firestore rules.
- Member appVersion updated to 9.2.1.
- Existing v9.2.0 Production Test Center remains unchanged.

If all three room-create payloads are permission-denied, the app now surfaces the Firebase error; that case requires production Firestore/IAM configuration rather than an APK-only workaround.
