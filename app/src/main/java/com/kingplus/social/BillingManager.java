package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryProductDetailsResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BillingManager implements PurchasesUpdatedListener {
    private static BillingManager active;
    private static final String[] PRODUCT_IDS = {
        "king_coins_100", "king_coins_600", "king_coins_1300"
    };

    private final Activity activity;
    private final BillingClient billingClient;
    private final List<ProductDetails> products = new ArrayList<>();

    private BillingManager(Activity activity) {
        this.activity = activity;
        billingClient = BillingClient.newBuilder(activity)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .enableAutoServiceReconnection()
            .build();
    }

    public static void showRecharge(Activity activity) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            new AlertDialog.Builder(activity)
                .setTitle("Firebase sign-in required")
                .setMessage("Google Play recharge needs a real Firebase account so the verified purchase can be credited to the correct server wallet.")
                .setPositiveButton("OK", null).show();
            return;
        }
        active = new BillingManager(activity);
        active.connectAndLoad();
    }

    private void connectAndLoad() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override public void onBillingSetupFinished(BillingResult result) {
                if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                    showError("Google Play Billing unavailable: " + result.getDebugMessage());
                    return;
                }
                queryProducts();
            }
            @Override public void onBillingServiceDisconnected() {
                // Automatic service reconnection is enabled.
            }
        });
    }

    private void queryProducts() {
        List<QueryProductDetailsParams.Product> query = new ArrayList<>();
        for (String id : PRODUCT_IDS) {
            query.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.INAPP)
                .build());
        }
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
            .setProductList(query).build();

        billingClient.queryProductDetailsAsync(params, (BillingResult result, QueryProductDetailsResult detailsResult) -> {
            if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                showError("Could not load Play products: " + result.getDebugMessage());
                return;
            }
            products.clear();
            products.addAll(detailsResult.getProductDetailsList());
            if (products.isEmpty()) {
                showError("No recharge products are available yet. Create king_coins_100, king_coins_600 and king_coins_1300 as one-time products in Play Console and publish to an internal test track.");
                return;
            }
            showProductPicker();
        });
    }

    private void showProductPicker() {
        String[] labels = new String[products.size()];
        for (int i = 0; i < products.size(); i++) {
            ProductDetails p = products.get(i);
            labels[i] = p.getName() + " • " + p.getProductId();
        }
        new AlertDialog.Builder(activity)
            .setTitle("Google Play Recharge")
            .setMessage("Coins are credited only after server verification. The app never trusts a client-side purchase result.")
            .setItems(labels, (dialog, which) -> launch(products.get(which)))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void launch(ProductDetails product) {
        List<ProductDetails.OneTimePurchaseOfferDetails> offers =
            product.getOneTimePurchaseOfferDetailsList();
        if (offers == null || offers.isEmpty()) {
            showError("This Play product has no eligible purchase offer.");
            return;
        }
        String offerToken = offers.get(0).getOfferToken();

        BillingFlowParams.ProductDetailsParams detailsParams =
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(product)
                .setOfferToken(offerToken)
                .build();

        BillingFlowParams.Builder builder = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(java.util.Collections.singletonList(detailsParams));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) builder.setObfuscatedAccountId(sha256(user.getUid()));

        BillingResult result = billingClient.launchBillingFlow(activity, builder.build());
        if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            showError("Purchase could not start: " + result.getDebugMessage());
        }
    }

    @Override public void onPurchasesUpdated(BillingResult result, List<Purchase> purchases) {
        if (result.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) return;
        if (result.getResponseCode() != BillingClient.BillingResponseCode.OK || purchases == null) {
            showError("Purchase update failed: " + result.getDebugMessage());
            return;
        }
        for (Purchase purchase : purchases) {
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                Toast.makeText(activity, "Payment pending. Coins will be credited only after Play marks it purchased.", Toast.LENGTH_LONG).show();
            } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                verifyOnServer(purchase);
            }
        }
    }

    private void verifyOnServer(Purchase purchase) {
        if (purchase.getProducts().isEmpty()) return;
        Map<String,Object> data = new HashMap<>();
        data.put("productId", purchase.getProducts().get(0));
        data.put("purchaseToken", purchase.getPurchaseToken());

        FirebaseFunctions.getInstance().getHttpsCallable("verifyPlayPurchase").call(data)
            .addOnSuccessListener(result -> {
                Object raw = result.getData();
                String message = "Purchase verified by server.";
                if (raw instanceof Map) {
                    Object m = ((Map<?,?>) raw).get("message");
                    if (m != null) message = String.valueOf(m);
                }
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            })
            .addOnFailureListener(error ->
                showError("Purchase is not credited yet. Server verification failed: " +
                    (error.getLocalizedMessage() == null ? "unknown error" : error.getLocalizedMessage())));
    }

    private void showError(String message) {
        if (activity.isFinishing() || activity.isDestroyed()) return;
        activity.runOnUiThread(() -> new AlertDialog.Builder(activity)
            .setTitle("Recharge status")
            .setMessage(message)
            .setPositiveButton("OK", null).show());
    }

    private static String sha256(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder();
            for (byte b : bytes) out.append(String.format("%02x", b));
            return out.toString();
        } catch (Exception ignored) {
            return Integer.toHexString(value.hashCode());
        }
    }
}
