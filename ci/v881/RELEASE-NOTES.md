# KING Plus v8.8.1 — Chat / Gift / Keyboard Fix

Fixes reported directly from the Party Room screenshots.

- Message text clears immediately when Send/IME Send is pressed; if Firebase send fails, the unsent text is restored.
- Keyboard stays available for continued chatting after a successful send instead of leaving stale text in the input.
- Party Room now applies IME insets, so the composer bar is laid out above the Android keyboard instead of underneath it.
- PartyActivity explicitly uses adjustResize.
- Chat bubbles now use one stable width and consistent padding/min-height. Emoji/sticker/live-message artwork uses a consistent 64dp presentation.
- Gift cards keep four equal columns. The final partial row gets spacers, preventing one or two remaining gifts from stretching into oversized cards.
- Gift names are single-line ellipsized so card height remains stable.
- Existing v8.8.0 KTV, PK, Family, Rank, gifts, games and Ecosystem Pro behavior is preserved.
