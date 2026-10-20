'use strict';

const test=require('node:test');
const assert=require('node:assert/strict');
const {shouldDeliverNotification,canReceiveRoomInvite}=require('./notification-preferences');

test('Me notification choices suppress their matching server notification category',()=>{
  assert.equal(shouldDeliverNotification({type:'direct_message'},{notifyMessages:false}),false);
  assert.equal(shouldDeliverNotification({type:'follow'},{notifySocial:false}),false);
  assert.equal(shouldDeliverNotification({type:'room_invite'},{notifyRoomInvites:false}),false);
  assert.equal(shouldDeliverNotification({type:'gift'},{notifyGifts:false}),false);
  assert.equal(shouldDeliverNotification({type:'game_invite'},{notifyGames:false}),false);
});

test('missing preferences and unrelated system events keep existing delivery behavior',()=>{
  assert.equal(shouldDeliverNotification({type:'gift'},null),true);
  assert.equal(shouldDeliverNotification({type:'moderation'},{}),true);
  assert.equal(shouldDeliverNotification({type:'gift'},{notifyGifts:true}),true);
});

test('room invite permission defaults on and a saved opt-out is enforced',()=>{
  assert.equal(canReceiveRoomInvite(null),true);
  assert.equal(canReceiveRoomInvite({allowRoomInvites:true}),true);
  assert.equal(canReceiveRoomInvite({allowRoomInvites:false}),false);
});
