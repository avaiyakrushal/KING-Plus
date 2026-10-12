const verifyPlayPurchase = onCall(async request => {
  const uid = requireAuth(request);
  if (request.auth.token?.firebase?.sign_in_provider === 'anonymous')
    throw new HttpsError('unauthenticated', 'Sign in to a permanent account before recharge.');
  const {createPurchaseService, PurchaseError} = require('./play-purchase-service');
  const auth = new google.auth.GoogleAuth({scopes: ['https://www.googleapis.com/auth/androidpublisher']});
  const publisher = google.androidpublisher({version: 'v3', auth});
  const service = createPurchaseService({
    db, timestamp: () => FieldValue.serverTimestamp(),
    getPurchase: async (productId, token) => (await publisher.purchases.products.get({packageName: PACKAGE_NAME, productId, token})).data,
    consumePurchase: (productId, token) => publisher.purchases.products.consume({packageName: PACKAGE_NAME, productId, token}),
    notify: (target, coins, productId) => sendUserNotification(target, 'Recharge complete', `${coins} Diamonds added`, {type: 'play_purchase', productId})
  });
  try {
    return await service({uid, productId: request.data?.productId, purchaseToken: request.data?.purchaseToken});
  } catch (error) {
    if (error instanceof PurchaseError) throw new HttpsError(error.code, error.message);
    // Do not log Google API error objects: they can contain purchase tokens.
    throw new HttpsError('internal', 'Recharge verification could not finish. Retry later.');
  }
});
