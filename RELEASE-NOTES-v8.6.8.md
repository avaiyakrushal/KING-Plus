# KING Plus v8.6.8

This stage fixes two issues reported from the Party Room screenshots.

- Emoji / Effects / Stickers / Faces now use a uniform fixed cell and artwork size so mixed packs no longer jump between tiny and oversized boxes.
- A profile photo selected from Me is shown immediately on the user's own Party Room seat using the persisted local URI.
- For Firebase-authenticated users, the selected profile photo is also uploaded through the existing allowed chat_media profile path, saved as the public photo URL, and refreshed into the live room member document so other signed-in room users can see the new avatar when cloud upload is permitted.
- Firebase/Auth, no-billing and TEST OTP behavior are otherwise unchanged.
