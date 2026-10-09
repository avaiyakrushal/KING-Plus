# KING Plus v9.7.3 — NO BILLING / Free Gifts (Spark plan)

## User requirement
Do NOT ask user for a credit card, paid Google Cloud billing, a Blaze plan or an actual coin purchase. No one should be told to enable Cloud Functions to use basic Party Emoji, Chat or decorative FREE Gifts.

## What the APK changes
- Party Room Gift Shop labels its catalog **FREE / No billing**, restricts quantity to 1, 3 or 9, limits send frequency to one per 1.5 seconds and creates only a zero-value Firestore room Gift event.
- A FREE Gift has `freeGift=true`, `giftValue=0`, `giftUnitCost=0`, explicit FREE message and no Level/VIP/wallet operations. Other users' existing room event listener can display the same gift animation.
- Private Chat Gift Shop labels its choices FREE and sends a Firestore message of type `text`, with no local TEST coin deduction, no Cloud Function call, no VIP promotion or paid receipt.
- The VIP screen is an informative preview only; no server wallet or Cloud Function is invoked to manufacture paid status.
- Already developed Family, Online Ludo, Mic/Seat, Room Presence and Live Emoji code is retained.
- No backend deployment, billing setup, Firebase plan upgrade or paid API is performed by this GitHub Actions workflow.

## Firebase free-tier limits
The project must remain on Spark/no-billing for **zero monetary charges**. Firestore's no-cost quota and supported Authentication services are subject to limits. Excess usage may stop working until quotas reset rather than be charged, provided no billing account is linked.
Sources: https://firebase.google.com/docs/projects/billing/firebase-pricing-plans and https://firebase.google.com/docs/firestore/pricing.
Cloud Functions are available on Blaze, not required for FREE Party/Chat Gift messages.

## Multi-phone acceptance checks
1. Install v9.7.3 over previous KING Plus when Android signature permits; do not uninstall and clear user settings unless necessary.
2. Confirm user never sees a payment/billing prompt in the FREE Gift flow or Paid VIP preview.
3. From account A, create and join a public Party, account B joins the same room. A selects B and sends a FREE Gift; B sees a matching event/animation. Verify neither balance nor VIP changes.
4. A's repeated sends inside 1.5 seconds should be throttled; quantity above 9 should be blocked.
5. Open private Chat from A to B, send FREE Rose. B should see the text FREE Gift and both phones' coins/levels remain unchanged.
6. Toggle Live Emoji and Mic/Seat, create/join a Family and test standard Chat, then play Ludo. All existing features must still be tested independently on real phones.
7. Try with poor network; if Firestore rejects an event, confirm no one is told a paid gift transferred or that real money was deducted.
8. If Free Firestore quota is exceeded, writes can fail until it resets. This is a free-tier capacity limit, NOT a payment requirement.
9. Monitor the lower-memory vivo phone for any recurrence of Party-related Android exits.

## Limits
Automated unit tests and APK compilation cannot confirm functionality on multiple physical phones. This version **does not implement real-money gifting or paid VIP**, and is not a claim that all previously tracked app features are complete.
