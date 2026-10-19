'use strict';
const crypto = require('node:crypto');
const {createVoiceService, createCloudflareApi, VoiceError} = require('./cloudflare-voice');

// Dependency-injected for emulator tests. This does not enable or deploy any service.
function createFirebaseVoice({db, config}) {
  const sessions = db.collection('server_voice_sessions');
  const slots = db.collection('server_voice_slots');
  const slotId = (uid, room, visit) => crypto.createHash('sha256')
    .update([uid, room, visit].join('\0')).digest('hex');
  const store = {
    get: async handle => {const d = await sessions.doc(handle).get(); return d.exists ? {...d.data(), handle} : null;},
    put: (handle, value) => sessions.doc(handle).set(value),
    patch: (handle, value) => sessions.doc(handle).update(value),
    remove: handle => sessions.doc(handle).delete(),
    publications: async roomId => (await sessions.where('roomId', '==', roomId).get()).docs
      .map(d => ({...d.data(), handle:d.id})).filter(s => s.published && !s.retiring),
    reserve: (uid, room, visit, handle, createdAt) => db.runTransaction(async tx => {
      const ref = slots.doc(slotId(uid, room, visit)), d = await tx.get(ref);
      if (d.exists) return false;
      tx.set(ref, {handle, createdAt}); return true;
    }),
    release: (uid, room, visit, handle) => db.runTransaction(async tx => {
      const ref = slots.doc(slotId(uid, room, visit)), d = await tx.get(ref);
      if (d.exists && d.get('handle') === handle) tx.delete(ref);
    }),
    // Never steal an expired lock: an uncertain remote mutation requires retirement.
    lock: handle => db.runTransaction(async tx => {
      const ref = sessions.doc(handle), d = await tx.get(ref);
      if (!d.exists || d.get('locked') === true) return false;
      tx.update(ref, {locked:true}); return true;
    }),
    unlock: handle => db.runTransaction(async tx => {
      const ref = sessions.doc(handle), d = await tx.get(ref);
      if (d.exists) tx.update(ref, {locked:false});
    })
  };
  async function authority(roomId, uid) {
    const room = db.collection('live_rooms').doc(roomId);
    const [r, m, ban, roomBan, role, seats] = await Promise.all([
      room.get(), room.collection('members').doc(uid).get(), db.collection('bans').doc(uid).get(),
      room.collection('room_bans').doc(uid).get(), room.collection('roles').doc(uid).get(),
      room.collection('seats').get()
    ]);
    return {closed: !r.exists || r.get('closed') === true,
      banned: ban.get('active') === true || roomBan.get('active') === true,
      memberUid: m.get('uid'), visit: m.get('sessionId'),
      ownerUid: r.get('ownerUid'), coHost: role.get('role') === 'cohost',
      maxSeats: [8,10,12].includes(r.get('maxSeats')) ? r.get('maxSeats') : 8,
      muteAll: r.get('muteAll') === true,
      seats: seats.docs.map(d => ({...d.data(), id:d.id}))};
  }
  const service = createVoiceService({config, authority, store,
    randomId: () => crypto.randomUUID(),
    api: async (...args) => {
      const cfg = await config();
      if (!cfg.appId || !cfg.secret) throw new VoiceError('failed-precondition', 'Voice server credentials are missing.');
      return createCloudflareApi(cfg)(...args);
    }});
  async function reconcile(snapshot) {
    for (const d of snapshot.docs) {
      const s = d.data(), state = await authority(s.roomId, s.uid);
      const {admitted, maySpeak} = require('./cloudflare-voice');
      if (!s.retiring && admitted(state,s.uid,s.visit) &&
          (!s.published || maySpeak(state,s.uid,s.seat))) continue;
      // Hide it immediately, before forced media closure. Failures remain retryable.
      await d.ref.update({retiring:true});
      if (!await store.lock(d.id)) throw new VoiceError('aborted','Voice mutation still in progress.');
      try {
        const latest = await store.get(d.id);
        if (latest?.mids.length) {
          const cfg = await config();
          await createCloudflareApi(cfg)(latest.sessionId,'tracks/close',{
            force:true,tracks:latest.mids.map(mid=>({mid}))
          },'PUT');
        }
        await store.remove(d.id);
        await store.release(s.uid,s.roomId,s.visit,d.id);
      } finally { await store.unlock(d.id); }
    }
  }
  return {service, store, authority,
    reconcileRoom: roomId => sessions.where('roomId','==',roomId).get().then(reconcile),
    reconcileUser: uid => sessions.where('uid','==',uid).get().then(reconcile)};
}
module.exports = {createFirebaseVoice};
