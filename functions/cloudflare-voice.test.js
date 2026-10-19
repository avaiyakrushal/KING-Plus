'use strict';
const {test} = require('node:test');
const assert = require('node:assert/strict');
const {createVoiceService, createCloudflareApi} = require('./cloudflare-voice');
const offer = {type: 'offer', sdp: 'v=0\r\nm=audio 9 UDP/TLS/RTP/SAVPF 111\r\n'};
const answer = {...offer, type: 'answer'};
function fixture() {
  const sessions = new Map(), slots = new Map(), locks = new Set(), calls = [];
  let count = 0;
  const cfg = {enabled: true, zeroCostVerified: true, appId: 'app', secret: 'server-only'};
  const state = {memberUid: 'alice', visit: 'visit', ownerUid: 'host', maxSeats: 8,
    seats: [{id: '1', uid: 'alice', micOn: true}, {id: '2', uid: 'bob', micOn: true}]};
  const authority = async (room, uid) => ({...state, memberUid: uid});
  const store = {
    get: async h => sessions.get(h),
    put: async (h, s) => sessions.set(h, {...s, handle: h}),
    patch: async (h, s) => { if (sessions.has(h)) sessions.set(h, {...sessions.get(h), ...s}); },
    remove: async h => sessions.delete(h),
    publications: async room => [...sessions.values()].filter(s => s.roomId === room && s.published),
    reserve: async (uid, room, visit, handle) => {
      const k = [uid, room, visit].join('/'); if (slots.has(k)) return false;
      slots.set(k, handle); return true;
    },
    release: async (uid, room, visit, handle) => {
      const k = [uid, room, visit].join('/'); if (slots.get(k) === handle) slots.delete(k);
    },
    lock: async h => { if (locks.has(h)) return false; locks.add(h); return true; },
    unlock: async h => locks.delete(h)
  };
  let response;
  const api = async (sid, op, body, method) => {
    calls.push({sid, op, body, method});
    if (response) return response(op);
    if (op === 'new') return {sessionId: 's' + count};
    if (op === 'tracks/new') return {tracks: body.tracks.map((t,i) => ({...t, mid: t.mid || String(i+1)})),
      sessionDescription: body.sessionDescription ? answer : offer};
    return {};
  };
  const service = createVoiceService({config: async () => cfg, authority, store, api,
    now: () => 100000, randomId: () => 'h' + (++count)});
  const invoke = (action, extra = {}, uid = 'alice') => service({uid,
    data: {action, roomId: 'room', visit: 'visit', ...extra}});
  const join = async uid => (await invoke('join', {}, uid)).handle;
  const publish = h => invoke('publish', {handle: h, seat: 1, mid: '0', description: offer});
  return {cfg, state, sessions, slots, locks, calls, invoke, join, publish,
    response: fn => {response = fn;}};
}
for (const flag of ['enabled', 'zeroCostVerified']) test(`no billable API call without ${flag}`, async () => {
  const f = fixture(); f.cfg[flag] = false;
  assert.equal((await f.invoke('config')).enabled, false);
  await assert.rejects(f.join(), {code: 'failed-precondition'}); assert.equal(f.calls.length, 0);
});
test('configuration never exposes credentials', async () => {
  const f = fixture(); assert(!JSON.stringify(await f.invoke('config')).includes('server-only'));
});
for (const field of ['closed','banned']) test(`reject ${field} room member`, async () => {
  const f = fixture(); f.state[field] = true;
  await assert.rejects(f.join(), {code: 'permission-denied'}); assert.equal(f.calls.length, 0);
});
test('old membership visit cannot create a connection', async () => {
  const f = fixture(); f.state.visit = 'new-visit';
  await assert.rejects(f.join(), {code: 'permission-denied'});
});
test('one session per user visit; release after leave', async () => {
  const f = fixture(), h = await f.join(); await assert.rejects(f.join(), {code: 'resource-exhausted'});
  await f.invoke('leave', {handle: h}); assert(await f.join());
});
test('cannot steal another user or room session', async () => {
  const f = fixture(), h = await f.join();
  for (const action of ['publish','tracks','answer','leave']) {
    await assert.rejects(f.invoke(action, {handle: h}, 'bob'), {code: 'permission-denied'});
    await assert.rejects(f.invoke(action, {handle: h, roomId: 'other'}), {code: 'permission-denied'});
    await assert.rejects(f.invoke(action, {handle: h, visit: 'other'}), {code: 'permission-denied'});
  }
});
test('audience cannot publish, and host mute-all blocks speakers', async () => {
  const f = fixture(), h = await f.join(); f.state.seats[0].micOn = false;
  await assert.rejects(f.publish(h), {code: 'permission-denied'});
  f.state.seats[0].micOn = true; f.state.muteAll = true;
  await assert.rejects(f.publish(h), {code: 'permission-denied'});
  f.state.coHost = true; assert.equal((await f.publish(h)).ok, true);
});
test('microphone publish accepts only audio SDP', async () => {
  const f = fixture(), h = await f.join();
  for (const description of [{...offer, sdp: offer.sdp+'m=video 9 RTP/AVP 96\r\n'}, answer,
    {...offer, sdp: 'x'.repeat(96001)}])
    await assert.rejects(f.invoke('publish', {handle:h, seat:1, mid:'0', description}), {code:'invalid-argument'});
  assert.equal(f.calls.length, 1);
  assert.equal((await f.publish(h)).ok, true);
  await assert.rejects(f.publish(h), {code:'already-exists'});
});
test('discovery hides muted speakers and old visits', async () => {
  const f = fixture(), h = await f.join(); await f.publish(h); const listener = await f.join('bob');
  assert.deepEqual((await f.invoke('tracks', {handle:listener}, 'bob')).tracks, [{handle:h}]);
  f.state.seats[0].micOn = false;
  assert.deepEqual((await f.invoke('tracks', {handle:listener}, 'bob')).tracks, []);
});
test('cross-room or retired sources cannot be subscribed', async () => {
  const f = fixture(), h = await f.join(); await f.publish(h); const listener = await f.join('bob');
  for (const patch of [{roomId:'other'}, {roomId:'room', retiring:true}, {retiring:false, visit:'old'}]) {
    Object.assign(f.sessions.get(h), patch);
    await assert.rejects(f.invoke('listen', {handle:listener, sources:[h]}, 'bob'), {code:'permission-denied'});
  }
});
test('subscription needs answer before next mutation and rejects unsolicited answer', async () => {
  const f = fixture(), h = await f.join(); await f.publish(h); const listener = await f.join('bob');
  await assert.rejects(f.invoke('answer', {handle:listener, description:answer}, 'bob'), {code:'failed-precondition'});
  await f.invoke('listen', {handle:listener, sources:[h]}, 'bob');
  await assert.rejects(f.invoke('listen', {handle:listener, sources:[h]}, 'bob'), {code:'failed-precondition'});
  assert.equal((await f.invoke('answer', {handle:listener, description:answer}, 'bob')).ok, true);
});
test('per-track errors are not treated as microphone success', async () => {
  const f = fixture(), h = await f.join(); f.response(() => ({tracks:[{mid:'0',errorCode:'denied'}]}));
  await assert.rejects(f.publish(h), {code:'unavailable'}); assert.equal(f.sessions.get(h).published, false);
});
test('leave after kick or kill switch still closes allocated tracks', async () => {
  const f = fixture(), h = await f.join(); await f.publish(h); f.cfg.enabled = false; f.state.banned = true;
  assert.equal((await f.invoke('leave', {handle:h})).ok, true);
  assert.deepEqual(f.calls.at(-1).body, {force:true, tracks:[{mid:'0'}]}); assert(!f.sessions.has(h));
});
test('failed cleanup retains retired session for retry; no discovery', async () => {
  const f = fixture(), h = await f.join(); await f.publish(h);
  f.response(() => {throw Error('network');});
  await assert.rejects(f.invoke('leave', {handle:h})); assert.equal(f.sessions.get(h).retiring, true);
  assert.equal(f.locks.size, 0); f.response(() => ({})); await f.invoke('leave', {handle:h});
  assert(!f.sessions.has(h));
});
test('overlapping negotiation or cleanup cannot race', async () => {
  const f = fixture(), h = await f.join(); f.locks.add(h);
  await assert.rejects(f.publish(h), {code:'aborted'});
  await assert.rejects(f.invoke('leave', {handle:h}), {code:'aborted'});
});
test('API constructs fixed server URL, times out, and does not return provider error secrets', async () => {
  let captured;
  const api = createCloudflareApi({appId:'app', secret:'private-secret', fetchImpl:async (url,opts) => {
    captured = {url,opts}; return {ok:true,json:async()=>({sessionId:'session'})};
  }});
  await api(null,'new',null,'POST');
  assert.equal(captured.url,'https://rtc.live.cloudflare.com/v1/apps/app/sessions/new');
  assert.equal(captured.opts.headers.Authorization,'Bearer private-secret');
  assert(captured.opts.signal); assert(!Object.hasOwn(captured.opts,'body'));
  const broken = createCloudflareApi({appId:'app', secret:'private-secret', fetchImpl:async()=>{throw Error('private-secret');}});
  await assert.rejects(broken(null,'new',null,'POST'), e => e.code==='unavailable'&&!e.message.includes('private-secret'));
});
