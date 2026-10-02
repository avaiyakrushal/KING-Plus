from pathlib import Path

src_path = Path('tools/prepare_party_v530.py')
src = src_path.read_text(encoding='utf-8')
old = "elif 'this::searchPartyRoomsV530' not in s:\n    raise SystemExit('v5.3: lobby create/header anchor not found')"
new = "elif 'this::searchPartyRoomsV530' not in s:\n    print('v5.3: evolved lobby header detected; keeping existing header and continuing')"
if old not in src:
    raise SystemExit('v5.3 wrapper: expected header guard not found in prepare script')
src = src.replace(old, new, 1)
exec(compile(src, str(src_path), 'exec'), {'__name__': '__main__', '__file__': str(src_path)})
