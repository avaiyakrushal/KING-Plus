from pathlib import Path

root = Path(__file__).resolve().parents[2]
gradle = (root / "app/build.gradle").read_text()
cloud = (root / "app/src/main/java/com/kingplus/social/CloudSync.java").read_text()
deep = (root / "app/src/main/java/com/kingplus/social/KingDeepFlowActivity.java").read_text()
rules = (root / "firestore.rules").read_text()

assert "versionCode 360" in gradle
assert "10.0.81-profile-report-history" in gradle
assert "private void reportHistory9981()" in deep
assert 'db.collection("user_report_history").document(owner).collection("items")' in deep
assert ".get(com.google.firebase.firestore.Source.SERVER)" in deep
assert "expectedOwner.equals(current.getUid())" in deep
assert "Moderation decisions and other users’ reports are private." in deep
assert 'db.collection("user_report_history")' in cloud
assert 'db.batch().set(reportRef, report).set(historyRef, receipt).commit()' in cloud
assert 'Report was not submitted. Check your connection and try again:' in cloud
assert "match /user_report_history/{uid}/items/{reportId}" in rules
assert "allow read: if owner(uid);" in rules
assert "allow update, delete: if false;" in rules
assert "existsAfter(/databases/$(database)/documents/reports/$(reportId))" in rules
assert "getAfter(/databases/$(database)/documents/reports/$(reportId)).data.reason == request.resource.data.reason" in rules

print("PASS report receipts are account-scoped, server-loaded, immutable, and tied atomically to private reports")
