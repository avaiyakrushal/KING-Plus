# KING Plus v10.0.65 — voice and live-state reliability

Continues the exact signed v10.0.64 source. Version code 344. Same TEST signing key; no redesign or data reset.

- A confirmed room join starts audience audio with microphone muted. Microphone transmission still requires the user's own seat consent, current Firebase account, backend seat permission and a real native conference-joined event.
- Native conference broadcasts and a 45-second join timeout replace the assumption that calling join() means voice connected. Only matching Jitsi conference URLs update connection state. Disconnect/timeout revokes mic consent and mutes the owned seat. Late events after disposal are ignored.
- Sound off closes native audio and turns off the microphone. Voice connection status opens the actual provider UI for authentication/errors and offers reconnect or voice off. Provider authentication can therefore be completed visibly instead of being hidden in a two-pixel view.
- Native voice presence now follows real connection state. Session-checked transactions update existing membership only, so late callbacks cannot recreate removed members or overwrite a later room visit.
- Account changes stop Party voice immediately. External voice membership verification rechecks the Firebase account after asynchronous lookups.
- Wallet screen subscribes to server snapshots and detaches on pause/navigation. Gift history immediately resets on account changes.
- A paid gift is confirmed only if the server returns the exact operation gift ID for the account/request and a valid resulting balance. An old endpoint's success boolean is insufficient. Durable retry keys remain pending after ambiguous replies.

Validation: backend transaction regressions, pure Java conference/receipt policies, real Firestore profile/wallet/lifecycle/rules tests and complete Android compilation/signing continuity in CI.

Still blocked: the v10.0.64 gift backend deployment was denied while enabling artifactregistry.googleapis.com (job 117446626337). The deployment identity also could not enable Cloud Build in the prior attempt. This version does not change cloud IAM, billing or merchant activation. It remains a TEST build. Physical two-phone audio, mic host controls and gift-animation acceptance have not been performed. Public meet.jit.si is not an authenticated KING production RTC service.

Primary SDK integration reference: https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-android-sdk/ and the official Java SDK sample https://github.com/jitsi/jitsi-meet-sdk-samples/blob/master/android/java/JitsiSDKTest/app/src/main/java/net/jitsi/sdktest/MainActivity.java
