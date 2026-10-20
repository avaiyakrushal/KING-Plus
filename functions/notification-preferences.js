'use strict';

const TYPE_TO_PREFERENCE = Object.freeze({
  direct_message: 'notifyMessages',
  follow: 'notifySocial',
  room_invite: 'notifyRoomInvites',
  gift: 'notifyGifts',
  game_invite: 'notifyGames',
});

function shouldDeliverNotification(data, preferences) {
  const key = TYPE_TO_PREFERENCE[data && data.type];
  // Preserve existing notification behavior for system/moderation events and
  // for accounts that have not created a preferences document yet.
  return !key || !preferences || preferences[key] !== false;
}

function canReceiveRoomInvite(preferences) {
  return !preferences || preferences.allowRoomInvites !== false;
}

module.exports = { TYPE_TO_PREFERENCE, shouldDeliverNotification, canReceiveRoomInvite };
