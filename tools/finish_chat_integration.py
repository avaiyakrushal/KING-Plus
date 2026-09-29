from pathlib import Path

path = Path('app/src/main/java/com/kingplus/social/MainActivity.java')
text = path.read_text(encoding='utf-8')

old = '''    private void messages() {
        screen="messages"; stopMic();'''
new = '''    private void messages() {
        // Firebase-authenticated users use the real Firestore inbox. Keep the old local
        // conversation screen only as a fallback for legacy/local-only test sessions.
        if (firebaseAuth != null && firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, ChatInboxActivity.class));
            return;
        }
        screen="messages"; stopMic();'''

if new in text:
    print('Cloud chat inbox already integrated in MainActivity.')
elif old in text:
    path.write_text(text.replace(old, new, 1), encoding='utf-8')
    print('Integrated ChatInboxActivity into the Messages tab.')
else:
    raise SystemExit('Could not find the Messages method insertion point; refusing a blind edit.')
