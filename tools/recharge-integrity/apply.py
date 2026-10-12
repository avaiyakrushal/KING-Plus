"""Patch the verified full release without replacing unrelated backend logic."""
from pathlib import Path
import shutil
import sys

repo = Path(__file__).resolve().parents[2]
target = Path(sys.argv[1]).resolve()
backend = target / 'functions' / 'v3.js'
text = backend.read_text()
start = text.index('const verifyPlayPurchase = onCall(')
end = text.index('\nconst ', start + 1)
replacement = (repo / 'tools/recharge-integrity/verify-callable.js').read_text().rstrip()
text = text[:start] + replacement + '\n' + text[end:]
for name in ['play-purchase-service.js', 'recharge-policy.js', 'king-vip-level.js']:
    source = repo / 'functions' / name
    dest = target / 'functions' / name
    if source.resolve() != dest.resolve():
        shutil.copyfile(source, dest)
backend.write_text(text)
print('Applied receipt integrity to full release; unrelated backend preserved.')
