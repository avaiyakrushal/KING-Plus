from pathlib import Path
import json
import re

required_files = [
    Path('app/google-services.json'),
    Path('firestore.rules'),
    Path('firebase.json'),
    Path('functions/index.js'),
    Path('app/src/main/java/com/kingplus/social/BillingManager.java'),
    Path('app/src/main/java/com/kingplus/social/CloudBackend.java'),
    Path('app/src/main/java/com/kingplus/social/LiveCloudActivity.java'),
    Path('app/src/main/java/com/kingplus/social/KingMessagingService.java'),
]

missing = [str(p) for p in required_files if not p.exists()]
if missing:
    raise SystemExit('Missing required production files: ' + ', '.join(missing))

config = json.loads(Path('app/google-services.json').read_text(encoding='utf-8'))
clients = config.get('client', [])
packages = []
for client in clients:
    info = client.get('client_info', {}).get('android_client_info', {})
    if info.get('package_name'):
        packages.append(info['package_name'])
if 'com.kingplus.social' not in packages:
    raise SystemExit('google-services.json does not contain com.kingplus.social')

billing = Path('app/src/main/java/com/kingplus/social/BillingManager.java').read_text(encoding='utf-8')
for product in ('king_coins_100', 'king_coins_600', 'king_coins_1300'):
    if product not in billing:
        raise SystemExit(f'Missing Play product ID: {product}')

functions = Path('functions/index.js').read_text(encoding='utf-8')
for callable_name in ('sendGift', 'sendRoomInvite', 'verifyPlayPurchase', 'moderateReport', 'banUser'):
    if re.search(rf'exports\.{re.escape(callable_name)}\s*=', functions) is None:
        raise SystemExit(f'Missing backend export: {callable_name}')

rules = Path('firestore.rules').read_text(encoding='utf-8')
if 'match /wallets/{uid}' not in rules or 'allow write: if false' not in rules:
    raise SystemExit('Production wallet client-write lock is missing from Firestore rules')
if 'match /reports/{reportId}' not in rules:
    raise SystemExit('Moderation report rules are missing')

manifest = Path('app/src/main/AndroidManifest.xml').read_text(encoding='utf-8')
for permission in ('android.permission.INTERNET', 'android.permission.RECORD_AUDIO', 'android.permission.POST_NOTIFICATIONS'):
    if permission not in manifest:
        raise SystemExit(f'Missing manifest permission: {permission}')

print('KING Plus production smoke checks passed')
