# KING Plus v3.0 release-candidate rules
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keep class com.google.firebase.messaging.FirebaseMessagingService { *; }
-keep class com.kingplus.social.KingMessagingService { *; }
-keep class com.kingplus.social.CloudBackend { *; }
-keep class com.kingplus.social.BillingManager { *; }
