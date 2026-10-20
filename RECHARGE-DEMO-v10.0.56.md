# KING Plus v10.0.56 — Recharge Demo Trial

The TEST Recharge screen offers a clearly labeled UI-only DEMO flow to select a sample pack, view a simulated checkout, and confirm that no money was collected. It **never** credits real Diamonds, alters Firebase wallets, starts the gateway, or opens UPI apps. The actual server-enabled Cashfree sandbox still requires sandbox merchant credentials and deployed callable Cloud Functions. Live merchant collection is not enabled.

APK versionCode 335. Preserve prior v10.0.55 recharge UI, UPI backend safety, VIP, Game and other pages. Run `python3 tools/recharge-v10056/check_source.py` and Cashfree/Firestore regression tests in CI.
