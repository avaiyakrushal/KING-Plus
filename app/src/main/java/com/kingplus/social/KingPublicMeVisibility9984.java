package com.kingplus.social;

import com.google.firebase.firestore.DocumentSnapshot;

/** Client display policy mirrored from the account owner's Me privacy choices. */
public final class KingPublicMeVisibility9984 {
    private KingPublicMeVisibility9984() {}

    /** Missing fields preserve legacy profiles' existing discoverability. */
    public static boolean discoverable(DocumentSnapshot profile) {
        return profile == null || !Boolean.FALSE.equals(profile.getBoolean("findMe"));
    }

    /** Missing fields preserve legacy profiles' existing online-status display. */
    public static boolean showOnlineStatus(DocumentSnapshot profile) {
        return profile == null || !Boolean.FALSE.equals(profile.getBoolean("showOnlineStatus"));
    }

    /** Missing fields preserve legacy profiles' existing cosmetic display. */
    public static boolean showEntranceEffects(DocumentSnapshot profile) {
        return profile == null || !Boolean.FALSE.equals(profile.getBoolean("showEntranceEffects"));
    }
}
