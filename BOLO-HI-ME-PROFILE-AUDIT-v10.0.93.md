# Me/Profile route audit — v10.0.93

- Reproduced the Gift Center → Gift History navigation mismatch by comparing the menu action with the available `gift_history` route.
- Corrected the Gift Center row to navigate to the account-scoped server-backed history screen.
- Added a source regression check for both the menu target and route handler.
- Validation: existing source check suite, 20 Node gift-service tests, Firestore emulator tests, and full Android CI build/package checks.
