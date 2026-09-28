from pathlib import Path
import json

required_files = [
    Path('app/google-services.json'),
    Path('firestore.rules'),
    Path('firebase.json'),
    Path('functions/package.json'),
    Path('functions/index.js'),
    Path('functions/v3.js'),
    Path('app/src/main/java/com/kingplus/social/BillingManager.java'),
    Path('app/src/main/java/com/kingplus/social/CloudBackend.java'),
    Path('app/src/main/java/com/kingplus/social/LiveCloudActivity.java'),
    Path('app/src/main/java/com/kingplus/social/KingMessagingService.java'),
]

missing = [str(p) for p in required_files if not p.exists()]
if missing:
    raise SystemExit('Missing required production files: ' + ', '.join(missing))

config = json.loads(Path('app/google-services.json').read_text(encoding='utf-8'))
packages = []
for client in config.get('client', []):
    info = client.get('client_info', {}).get('android_client_info', {})
    if info.get('package_name'):
        packages.append(info['package_name'])
if 'com.kingplus.social' not in packages:
    raise SystemExit('google-services.json does not contain com.kingplus.social')

billing = Path('app/src/main/java/com/kingplus/social/BillingManager.java').read_text(encoding='utf-8')
for product in ('king_coins_100', 'king_coins_600', 'king_coins_1300'):
    if product not in billing:
        raise SystemExit(f'Missing Play product ID: {product}')

package_json = json.loads(Path('functions/package.json').read_text(encoding='utf-8'))
main_name = package_json.get('main', 'index.js')
main_path = Path('functions') / main_name
if not main_path.exists():
    raise SystemExit(f'Firebase Functions main entry does not exist: {main_path}')
backend = Path('functions/index.js').read_text(encoding='utf-8') + '\n' + main_path.read_text(encoding='utf-8')
for callable_name in ('sendGift', 'sendRoomInvite', 'verifyPlayPurchase', 'moderateReport', 'banUser'):
    if callable_name not in backend:
        raise SystemExit(f'Missing backend export/implementation: {callable_name}')

# v3.0.1 production hardening must be applied before deploy/build.
for marker in ('async function isBanned(uid)', 'assertActiveUser', 'assertReceivableUser', 'sendRoomInvite: secureSendRoomInvite'):
    if marker not in backend:
        raise SystemExit(f'Missing production abuse hardening: {marker}')

rules = Path('firestore.rules').read_text(encoding='utf-8')
if 'match /wallets/{uid}' not in rules or 'allow write: if false' not in rules:
    raise SystemExit('Production wallet client-write lock is missing from Firestore rules')
if 'match /reports/{reportId}' not in rules:
    raise SystemExit('Moderation report rules are missing')
if 'match /play_purchase_receipts/{receiptId}' not in rules:
    raise SystemExit('Play purchase receipt rules are missing')
if 'function activeUser()' not in rules or 'match /bans/{uid}' not in rules:
    raise SystemExit('Ban enforcement is missing from Firestore rules')

manifest = Path('app/src/main/AndroidManifest.xml').read_text(encoding='utf-8')
for permission in ('android.permission.INTERNET', 'android.permission.RECORD_AUDIO', 'android.permission.POST_NOTIFICATIONS'):
    if permission not in manifest:
        raise SystemExit(f'Missing manifest permission: {permission}')

print(f'KING Plus production smoke checks passed (functions main: {main_name}; ban enforcement: enabled)')
