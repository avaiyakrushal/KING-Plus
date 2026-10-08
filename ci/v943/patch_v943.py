from pathlib import Path
import re,sys

root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
main=pkg/'MainActivity.java'
q=main.read_text()

# Stable session snapshot: prevents auth state changing between null-check and getUid().
marker='''    private void walletPage(){'''
helper='''    private com.google.firebase.auth.FirebaseUser currentSessionUser943(){
        try{return firebaseAuth==null?null:firebaseAuth.getCurrentUser();}
        catch(Throwable e){KingStability.nonFatal(this,"auth-current-user",e);return null;}
    }

'''
if marker not in q: raise SystemExit('Main wallet marker missing for session helper')
if 'currentSessionUser943()' not in q:
    q=q.replace(marker,helper+marker,1)

old='''        if(firestore!=null&&firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null){
            firestore.collection("wallets").document(firebaseAuth.getCurrentUser().getUid()).get().addOnSuccessListener(doc->{Object raw=doc.get("coins");long c=raw instanceof Number?Math.max(0,Math.round(((Number)raw).doubleValue())):0;coinBalance=(int)Math.min(Integer.MAX_VALUE,c);bal.setText("💎 "+c+" Diamonds");});
        }'''
new='''        final com.google.firebase.auth.FirebaseUser walletUser943=currentSessionUser943();
        if(firestore!=null&&walletUser943!=null){
            final String walletUid943=walletUser943.getUid();
            firestore.collection("wallets").document(walletUid943).get().addOnSuccessListener(doc->{Object raw=doc.get("coins");long c=raw instanceof Number?Math.max(0,Math.round(((Number)raw).doubleValue())):Math.max(0,coinBalance);coinBalance=(int)Math.min(Integer.MAX_VALUE,c);bal.setText("💎 "+c+" Diamonds");})
                .addOnFailureListener(e->{bal.setText("💎 "+Math.max(0,coinBalance)+" Diamonds");KingUiState.error(this,"Wallet unavailable",e.getLocalizedMessage(),this::walletPage);});
        }'''
if old not in q: raise SystemExit('walletPage auth marker missing')
q=q.replace(old,new,1)

old='''    private void loadRealProfileData(){
        if(firestore==null||firebaseAuth==null||firebaseAuth.getCurrentUser()==null){
            if(profileFollowersNumber!=null)profileFollowersNumber.setText("0");
            if(profileFollowingNumber!=null)profileFollowingNumber.setText("0");
            if(profileFriendsNumber!=null)profileFriendsNumber.setText("0");
            if(profileTopBalance!=null)profileTopBalance.setText("💎 0");
            if(profileWalletCoins!=null)profileWalletCoins.setText("💎 0");
            return;
        }
        final String uid=firebaseAuth.getCurrentUser().getUid();'''
new='''    private void loadRealProfileData(){
        final com.google.firebase.auth.FirebaseUser profileUser943=currentSessionUser943();
        if(firestore==null||profileUser943==null){
            if(profileFollowersNumber!=null)profileFollowersNumber.setText("0");
            if(profileFollowingNumber!=null)profileFollowingNumber.setText("0");
            if(profileFriendsNumber!=null)profileFriendsNumber.setText("0");
            if(profileTopBalance!=null)profileTopBalance.setText("💎 0");
            if(profileWalletCoins!=null)profileWalletCoins.setText("◈ 0");
            return;
        }
        final String uid=profileUser943.getUid();'''
if old not in q: raise SystemExit('loadRealProfileData auth marker missing')
q=q.replace(old,new,1)

old='''    private void showProfileVisitors940(){
        if(firestore==null||firebaseAuth==null||firebaseAuth.getCurrentUser()==null){Toast.makeText(this,"Sign in to view visitors",Toast.LENGTH_SHORT).show();return;}
        String uid=firebaseAuth.getCurrentUser().getUid();'''
new='''    private void showProfileVisitors940(){
        final com.google.firebase.auth.FirebaseUser visitorUser943=currentSessionUser943();
        if(firestore==null||visitorUser943==null){Toast.makeText(this,"Sign in to view visitors",Toast.LENGTH_SHORT).show();return;}
        final String uid=visitorUser943.getUid();'''
if old not in q: raise SystemExit('showProfileVisitors auth marker missing')
q=q.replace(old,new,1)

old='''    private void refreshServerWallet(){
        if(profileTopBalance!=null)profileTopBalance.setText("💎 "+Math.max(0,coinBalance));
        if(profileWalletCoins!=null)profileWalletCoins.setText("◈ "+Math.max(0,coinBalance));
        if(firestore==null||firebaseAuth==null||firebaseAuth.getCurrentUser()==null)return;
        firestore.collection("wallets").document(firebaseAuth.getCurrentUser().getUid()).get().addOnSuccessListener(doc->{'''
new='''    private void refreshServerWallet(){
        if(profileTopBalance!=null)profileTopBalance.setText("💎 "+Math.max(0,coinBalance));
        if(profileWalletCoins!=null)profileWalletCoins.setText("◈ "+Math.max(0,coinBalance));
        final com.google.firebase.auth.FirebaseUser walletUser943=currentSessionUser943();
        if(firestore==null||walletUser943==null)return;
        final String walletUid943=walletUser943.getUid();
        firestore.collection("wallets").document(walletUid943).get().addOnSuccessListener(doc->{'''
if old not in q: raise SystemExit('refreshServerWallet auth marker missing')
q=q.replace(old,new,1)

old='''        if(firestore==null||firebaseAuth==null||firebaseAuth.getCurrentUser()==null){loading940.setText("Sign in to see recommendations");return;}
        String own=firebaseAuth.getCurrentUser().getUid();
        firestore.collection("public_profiles").limit(8).get().addOnSuccessListener(snap->{box940.removeAllViews();int shown=0;for(DocumentSnapshot d:snap.getDocuments()){String uid=d.getString("uid");if(uid==null||uid.isEmpty())uid=d.getId();if(uid.equals(own))continue;String n=d.getString("displayName");if(n==null||n.trim().isEmpty())n="KING User";Long vip=d.getLong("vipLevel"),lv=d.getLong("level");addProfileRecommendationRow940(box940,uid,n,vip==null?0:vip,lv==null?1:lv);if(++shown>=5)break;}if(shown==0){TextView e=new TextView(this);e.setText("No recommendations yet");e.setTextSize(12);e.setTextColor(0xff8a8391);e.setPadding(dp(10),dp(12),dp(10),dp(12));box940.addView(e,new LinearLayout.LayoutParams(-1,dp(48)));}}).addOnFailureListener(e->{loading940.setText("Recommendations unavailable • tap Status or try again later");});'''
new='''        final com.google.firebase.auth.FirebaseUser recommendationUser943=currentSessionUser943();
        if(firestore==null||recommendationUser943==null){loading940.setText("Sign in to see recommendations");return;}
        final String own=recommendationUser943.getUid();
        loading940.setOnClickListener(null);
        firestore.collection("public_profiles").limit(8).get().addOnSuccessListener(snap->{box940.removeAllViews();int shown=0;for(DocumentSnapshot d:snap.getDocuments()){String uid=d.getString("uid");if(uid==null||uid.isEmpty())uid=d.getId();if(uid.equals(own))continue;String n=d.getString("displayName");if(n==null||n.trim().isEmpty())n="KING User";Long vip=d.getLong("vipLevel"),lv=d.getLong("level");addProfileRecommendationRow940(box940,uid,n,vip==null?0:vip,lv==null?1:lv);if(++shown>=5)break;}if(shown==0){TextView e=new TextView(this);e.setText("No recommendations yet");e.setTextSize(12);e.setTextColor(0xff8a8391);e.setPadding(dp(10),dp(12),dp(10),dp(12));box940.addView(e,new LinearLayout.LayoutParams(-1,dp(48)));}})
            .addOnFailureListener(e->{loading940.setText("Recommendations unavailable • tap to retry");loading940.setOnClickListener(v->{box940.removeAllViews();addProfileRecommendations940(box940);});});'''
if old not in q: raise SystemExit('profile recommendations auth marker missing')
q=q.replace(old,new,1)

# Better retry on Party-record load instead of sending user away.
old='''}).addOnFailureListener(e->mePartyRecordV600(host,"🎤","Party records unavailable","Tap to open Party",this::openPartyActivity));'''
new='''}).addOnFailureListener(e->mePartyRecordV600(host,"🎤","Party records unavailable","Tap to retry",()->loadPartyRecordsV600(host)));'''
if old not in q: raise SystemExit('party record failure marker missing')
q=q.replace(old,new,1)

main.write_text(q)

# Public profile social counts: failed counters can be tapped to retry.
pub=pkg/'KingPublicProfileActivity.java'
q=pub.read_text()
old='''    private void failPublicCounts940(){if(publicFollowers940!=null)publicFollowers940.setText("—\\nFollowers");if(publicFollowing940!=null)publicFollowing940.setText("—\\nFollowing");if(publicFriends940!=null)publicFriends940.setText("—\\nFriends");}'''
new='''    private void failPublicCounts940(){
        if(publicFollowers940!=null){publicFollowers940.setText("↻\\nFollowers");publicFollowers940.setOnClickListener(v->loadPublicCounts940());}
        if(publicFollowing940!=null){publicFollowing940.setText("↻\\nFollowing");publicFollowing940.setOnClickListener(v->loadPublicCounts940());}
        if(publicFriends940!=null){publicFriends940.setText("↻\\nFriends");publicFriends940.setOnClickListener(v->loadPublicCounts940());}
    }'''
if old not in q: raise SystemExit('public counter failure marker missing')
q=q.replace(old,new,1)

old='''    private void setPublicStat940(TextView v,long n,String label){if(v!=null)v.setText(n+"\\n"+label);}'''
new='''    private void setPublicStat940(TextView v,long n,String label){if(v!=null){v.setOnClickListener(null);v.setText(n+"\\n"+label);}}'''
if old not in q: raise SystemExit('public counter set marker missing')
q=q.replace(old,new,1)
pub.write_text(q)

# Version bump.
gradle=root/'app/build.gradle'
g=gradle.read_text()
old="versionCode 141; versionName '9.4.2-parity-next'"
if old not in g: raise SystemExit('v9.4.2 version marker missing')
gradle.write_text(g.replace(old,"versionCode 142; versionName '9.4.3-session-social'",1))

(root/'V9.4.3-WORKLOG.md').write_text('''# KING Plus v9.4.3 session/social hardening

- snapshots FirebaseUser before using UID so auth changes cannot invalidate a prior null check
- hardens Wallet, Me/Profile counters, Visitors, server wallet refresh and Recommendations
- converts Wallet and Recommendations failures into retryable states
- converts Party-record failure into in-place retry
- public profile Followers/Following/Friends failure counters become tap-to-retry
- keeps v9.4.2 Party/Games/Gift/Emoji/KTV/PK reconnect and UI fixes unchanged
''')
print('KING Plus v9.4.3 session/social hardening applied')
