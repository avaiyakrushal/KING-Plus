'use strict';

// Raw Realtime SFU, not RealtimeKit. No credential ever leaves this module.
class VoiceError extends Error {
  constructor(code, message) { super(message); this.code = code; }
}
const validId = x => typeof x === 'string' && /^[A-Za-z0-9_-]{1,128}$/.test(x);
const fail = (code, message) => { throw new VoiceError(code, message); };
function description(value, type) {
  if (!value || value.type !== type || typeof value.sdp !== 'string' ||
      value.sdp.length > 96000 || !/^v=0\r?\n/.test(value.sdp) ||
      !/^m=audio /m.test(value.sdp) || /^m=(video|application) /m.test(value.sdp))
    fail('invalid-argument', 'Only an audio connection is permitted.');
  return {type, sdp: value.sdp};
}
function maySpeak(state, uid, seat) {
  return Number.isInteger(seat) && seat >= 1 && seat <= state.maxSeats &&
    state.seats.some(s => s.id === String(seat) && s.uid === uid && s.micOn === true) &&
    (!state.muteAll || state.ownerUid === uid || state.coHost === true);
}
function admitted(state, uid, visit) {
  return state && !state.closed && !state.banned && state.memberUid === uid && state.visit === visit;
}

function createVoiceService({config, authority, store, api, now = Date.now, randomId}) {
  return async function voice({uid, data = {}}) {
    if (!validId(uid)) fail('unauthenticated', 'Sign in to use room voice.');
    const cfg = await config();
    if (data.action === 'config') return {
      provider: 'cloudflare', enabled: cfg.enabled === true && cfg.zeroCostVerified === true,
      message: cfg.enabled && cfg.zeroCostVerified ? 'Cloudflare voice ready' :
        'Cloudflare voice is not activated. Account and zero-cost setup are pending.'
    };
    const {action, roomId, visit} = data;
    if (!validId(roomId) || !validId(visit)) fail('invalid-argument', 'A current room visit is required.');
    if (!['join','publish','listen','answer','tracks','leave'].includes(action))
      fail('invalid-argument', 'Unknown voice operation.');

    // Cleanup must work after a kick, a room closure or an operator kill switch.
    let session;
    if (action !== 'join') {
      if (!validId(data.handle)) fail('invalid-argument', 'Voice handle required.');
      session = await store.get(data.handle);
      if (!session || session.uid !== uid || session.roomId !== roomId || session.visit !== visit)
        fail('permission-denied', 'This voice connection belongs to another room visit.');
      if (action === 'leave') {
        if (!await store.lock(data.handle, now())) fail('aborted', 'Voice negotiation is busy.');
        try {
          session = await store.get(data.handle);
          if (!session) return {ok: true};
          await store.patch(data.handle, {retiring: true});
          if (session.mids.length) await api(session.sessionId, 'tracks/close', {
            force: true, tracks: session.mids.map(mid => ({mid}))
          }, 'PUT');
          await store.remove(data.handle);
          await store.release(uid, roomId, visit, data.handle);
          return {ok: true};
        } finally { await store.unlock(data.handle); }
      }
    }
    if (cfg.enabled !== true || cfg.zeroCostVerified !== true)
      fail('failed-precondition', 'Cloudflare voice activation and zero-cost verification are pending.');
    if (!cfg.appId || !cfg.secret) fail('failed-precondition', 'Voice server credentials are missing.');
    const state = await authority(roomId, uid);
    if (!admitted(state, uid, visit)) fail('permission-denied', 'Join this open Party room first.');
    if (session && (session.retiring || now() - session.createdAt > 60 * 60 * 1000))
      fail('failed-precondition', 'Reconnect room voice.');

    if (action === 'join') {
      // A server-side per-user slot prevents unbounded sessions from repeated taps.
      const handle = randomId();
      if (!await store.reserve(uid, roomId, visit, handle, now()))
        fail('resource-exhausted', 'A voice connection already exists. Leave it before reconnecting.');
      try {
        const response = await api(null, 'new', null, 'POST');
        if (!validId(response.sessionId)) fail('unavailable', 'Voice session could not be created.');
        await store.put(handle, {uid, roomId, visit, sessionId: response.sessionId,
          createdAt: now(), mids: [], sources: [], published: false, pending: false});
        return {ok: true, handle};
      } catch (error) { await store.release(uid, roomId, visit, handle); throw error; }
    }
    if (action === 'tracks') {
      const publications = await store.publications(roomId);
      const tracks = [];
      for (const p of publications) {
        if (p.uid === uid || p.retiring || now() - p.createdAt > 60 * 60 * 1000) continue;
        const speaker = await authority(roomId, p.uid);
        if (admitted(speaker, p.uid, p.visit) && maySpeak(speaker, p.uid, p.seat))
          tracks.push({handle: p.handle});
      }
      return {ok: true, tracks};
    }
    // Each call owns a bounded lease; overlapping negotiations are rejected.
    if (!await store.lock(data.handle, now())) fail('aborted', 'Voice negotiation is busy.');
    try {
      session = await store.get(data.handle);
      if (!session || session.retiring) fail('failed-precondition', 'Voice connection ended.');
      if (action === 'answer') {
        if (!session.pending) fail('failed-precondition', 'No voice answer is expected.');
        const result = await api(session.sessionId, 'renegotiate', {
          sessionDescription: description(data.description, 'answer')
        }, 'PUT');
        await store.patch(data.handle, {pending: false});
        return {ok: true, result};
      }
      if (session.pending) fail('failed-precondition', 'Complete the previous voice negotiation first.');
      if (action === 'publish') {
        if (session.published) fail('already-exists', 'Microphone is already published.');
        if (!maySpeak(state, uid, data.seat)) fail('permission-denied', 'An enabled microphone seat is required.');
        if (typeof data.mid !== 'string' || !/^\d{1,3}$/.test(data.mid))
          fail('invalid-argument', 'Microphone track is invalid.');
        const result = await api(session.sessionId, 'tracks/new', {
          sessionDescription: description(data.description, 'offer'),
          tracks: [{location: 'local', mid: data.mid, trackName: 'mic'}]
        }, 'POST');
        // Track success is separate from HTTP success. Never publish a failed allocation.
        const track = result.tracks?.find(t => t.mid === data.mid && !t.errorCode);
        if (track) await store.patch(data.handle, {mids: [...new Set([...session.mids, data.mid])]});
        if (!track || result.errorCode || result.requiresImmediateRenegotiation)
          fail('unavailable', 'Microphone negotiation needs a fresh voice connection.');
        await store.patch(data.handle, {published: true, seat: data.seat,
          mids: [...session.mids, data.mid]});
        const latest = await authority(roomId, uid);
        if (!admitted(latest, uid, visit) || !maySpeak(latest, uid, data.seat)) {
          await store.patch(data.handle, {retiring: true});
          await api(session.sessionId, 'tracks/close', {force:true, tracks:[{mid:data.mid}]}, 'PUT');
          fail('permission-denied', 'Microphone permission changed while connecting.');
        }
        return {ok: true, result};
      }
      if (!Array.isArray(data.sources) || data.sources.length < 1 || data.sources.length > 12 ||
          !data.sources.every(validId) || new Set(data.sources).size !== data.sources.length)
        fail('invalid-argument', 'Choose up to twelve room microphones.');
      if (data.sources.some(h => (session.sources || []).includes(h)))
        fail('already-exists', 'This microphone is already connected.');
      if ((session.sources || []).length + data.sources.length > 32)
        fail('resource-exhausted', 'Reconnect voice before adding more microphones.');
      const tracks = [];
      for (const handle of data.sources) {
        const p = await store.get(handle);
        if (!p || p.retiring || p.roomId !== roomId || p.uid === uid || !p.published ||
            now() - p.createdAt > 60 * 60 * 1000)
          fail('permission-denied', 'Microphone does not belong to this room.');
        const speaker = await authority(roomId, p.uid);
        if (!admitted(speaker, p.uid, p.visit) || !maySpeak(speaker, p.uid, p.seat))
          fail('permission-denied', 'Microphone is no longer available.');
        tracks.push({location: 'remote', sessionId: p.sessionId, trackName: 'mic'});
      }
      const result = await api(session.sessionId, 'tracks/new', {tracks}, 'POST');
      const allocated = (result.tracks || []).filter(t => typeof t.mid === 'string' && !t.errorCode);
      await store.patch(data.handle, {mids: [...new Set([...session.mids, ...allocated.map(t => t.mid)])],
        sources: [...(session.sources || []), ...data.sources], pending: !!result.sessionDescription});
      if (result.errorCode || allocated.length !== tracks.length)
        fail('unavailable', 'Some room microphones could not connect. Reconnect voice.');
      return {ok: true, result};
    } finally { await store.unlock(data.handle); }
  };
}

function createCloudflareApi({appId, secret, fetchImpl = fetch}) {
  return async (sessionId, operation, body, method) => {
    if (!validId(appId) || (sessionId !== null && !validId(sessionId)))
      fail('failed-precondition', 'Invalid voice server configuration.');
    const path = sessionId === null ? 'sessions/new' : `sessions/${sessionId}/${operation}`;
    let response;
    try {
      response = await fetchImpl(`https://rtc.live.cloudflare.com/v1/apps/${appId}/${path}`, {
        method, headers: {Authorization: `Bearer ${secret}`, 'Content-Type': 'application/json'},
        ...(body === null ? {} : {body: JSON.stringify(body)}), signal: AbortSignal.timeout(12000)
      });
      if (!response.ok) fail('unavailable', 'Voice server is unavailable. Reconnect voice.');
      const result = await response.json();
      if (result.errorCode) fail('unavailable', 'Voice server could not complete this operation.');
      return result;
    } catch (error) {
      if (error instanceof VoiceError) throw error;
      fail('unavailable', 'Voice server did not respond. Reconnect voice.');
    }
  };
}
module.exports = {createVoiceService, createCloudflareApi, VoiceError, maySpeak, admitted};
