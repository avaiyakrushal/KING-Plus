# KING Plus v9.9.82 — Agency, Agent and Diamond Request Handbook

## વહીવટ: Agency કોને આપવી અને કેવી રીતે?
1. નવા Agency Ownerએ પોતાના ફોનમાં KING Plusના Google/Firebase એકાઉન્ટથી Login કરવું અને પોતાના પૂર્ણ Firebase UID મેળવવો.
2. KING Plusમાં Super Adminને Backend દ્વારા **server-issued Firebase custom claim `admin: true`** આપેલો હોવો જોઈએ. માત્ર એડમીન ઇમેલ સરખી હોવાથી અધિકાર મળતો નથી. આ claim કોઈપણ Android UI/Firestore user documentથી બનાવવી નહીં.
3. Super Admin પોતાના ફોનમાં **Me → Agency & Agent Recharge** ખોલે, Agency Ownerનો પૂર્ણ KING UID અને Agencyનું નામ લખે, **Approve Agency** દબાવે. Firebase `king_agencies/{uid}`માં audit timestamp સાથે active role સાચવાય છે.
4. Agency Owner હવે પોતાની Me Agency સ્ક્રીનમાંથી નવો Agent UID નાખી **Authorize Agent** દબાવી શકે. એ Agent `king_agents/{uid}`માં પોતાના Agency UID હેઠળ બંધાય છે. બીજા Agencyને takeover કરવાની પરવાનગી નથી.
5. Agent પોતાના Me → Agency & Agent Rechargeમાંથી ગ્રાહકનો Firebase UID અને 100 / 600 / 1300 Diamond pack પસંદ કરીને **Recharge Request** બનાવે. `king_agent_recharge_requests/{orderId}`માં `status:'pending'`, `createdAt`, `agentUid`, `agencyUid`, `targetUid`, `diamonds` રહે છે.
6. Agentને પોતાની Requests, Agencyને તેના Agents અને તેની Requests અને Super Adminને બધી Requestsની યાદી મળે.
7. Super Admin Agency suspend કરે તો તેના બધા Agents નવી Requests નહીં કરી શકે; Agency Owner Agent suspend કરે તો એ Agent બંધ થઈ જશે.

## Security model, currently TEST ONLY
- **Admin identity:** Firebase Auth custom claim `admin:true` issued exclusively by trusted Firebase Admin SDK. Developer/admin must provision the initial account securely (not through an app UI). Never ask for service account private keys in ChatGPT.
- **No artificial credit:** A Pending Request means **zero Diamonds credited**. Android App and Firestore Rules both forbid client-side writes to `wallets`, `wallet_ledger`, or `wallet_operations`; even `admin:true` alone cannot mint money-equivalent virtual currency.
- **No fake settlements:** `status` cannot be updated from the Android client, even by an Admin; arbitrary `credited`/ `approved` status denied. Order entries cannot be deleted. Agents cannot forge another `agentUid` or assign themselves agency owner.
- **Production prerequisites before real Diamond recharges:** legally/contractually permitted merchant/channel for sales of virtual currencies; verified merchant transaction/receipt; refunds/chargebacks; independent append-only double-entry ledger with transaction IDs; replay/idempotency; server-side agency inventory (prefunded) or merchant-linked settlement; commission/tax reporting; audited operations; deploy Firestore Rules, App Check and secure service accounts; 2-phone acceptance testing.
- **Google Play Store policy:** in-app purchases of digital virtual currency normally require Google Play Billing unless you qualify for a documented policy exception or have enrolled in an alternative billing program; off-platform payment redirects in Play-distributed apps are restricted. Check https://support.google.com/googleplay/android-developer/answer/9858738 before enabling any live Agency Recharge checkout.
- Old `KingRecharge974Activity.LIVE_RECHARGE=false`; `KingPremiumAccess981.PAID_VIP_ACTIVE=false`; no real invoices or payments are collected by this TEST version.

## Acceptance evidence
Automated `agency-roles-and-recharge-firestore.test.mjs` with Admin, two Agency roles, real Agent, customer, and unauthorized account tests:
- Admin creates/suspends Agency; outsider denied.
- Only approved active Agency may create/suspend its own Agent; no role escalation or cross-Agency takeover.
- Agent can submit real immutable PENDING request; attempt to mark paid or to submit excessive diamonds is rejected.
- Suspending Agency prevents subordinate Agent from placing an order.
- Client writes to protected wallet or ledger are rejected, even with Admin claim.
- Original Games, Bingo, Party/Gift tests and Android signing are rerun with the new APK.

**Release**: New v9.9.82 APK is a **TEST foundation for Agency authorization and requests**, not a production paid recharge service.
