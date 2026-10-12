'use strict';
const {test} = require('node:test');
const assert = require('node:assert/strict');
const {createHash, randomUUID} = require('node:crypto');
const {createPurchaseService} = require('./play-purchase-service');
const hash = s => createHash('sha256').update(s).digest('hex');

// Unit mode serializes transactions; emulator mode uses actual Firestore retries.
function memoryDb() {
  const docs = new Map();
  let queue = Promise.resolve();
  return {
    collection: name => ({doc: id => ({path: `${name}/${id}`})}),
    runTransaction(fn) {
      const run = queue.then(async () => {
        const pending = [];
        const value = await fn({
          get: async ref => ({exists: docs.has(ref.path), data: () => docs.get(ref.path), get: k => docs.get(ref.path)?.[k]}),
          set: (ref, data, options) => pending.push([ref, data, options])
        });
        for (const [ref, data, options] of pending)
          docs.set(ref.path, options?.merge ? {...docs.get(ref.path), ...data} : data);
        return value;
      });
      queue = run.catch(() => {});
      return run;
    }
  };
}

let emulator;
if (process.env.KING_PURCHASE_EMULATOR === '1') {
  assert.match(process.env.FIRESTORE_EMULATOR_HOST || '', /^(127\.0\.0\.1|localhost):\d+$/);
  const {initializeApp} = require('firebase-admin/app');
  const {getFirestore} = require('firebase-admin/firestore');
  emulator = getFirestore(initializeApp({projectId: 'demo-king-receipt-integrity'}));
}

function fixture() {
  const uid = `buyer-${randomUUID()}`;
  const token = `token-${randomUUID()}`;
  const db = emulator || memoryDb();
  const state = {
    purchase: {purchaseState: 0, consumptionState: 0, quantity: 1, refundableQuantity: 1,
      obfuscatedExternalAccountId: hash(uid), productId: 'king_coins_100'},
    consumed: 0, notified: 0, verifyCalls: 0, failConsume: false, failNotify: false, failVerify: false
  };
  const input = {uid, productId: 'king_coins_100', purchaseToken: token};
  const deps = {
    db, timestamp: () => 123,
    getPurchase: async () => { state.verifyCalls++; if (state.failVerify) throw Error(token); return {...state.purchase}; },
    consumePurchase: async () => { state.consumed++; if (state.failConsume) throw Error(token); },
    notify: async () => { state.notified++; if (state.failNotify) throw Error('FCM failure'); }
  };
  const service = createPurchaseService(deps);
  const ref = (collection, id) => db.collection(collection).doc(id);
  const write = (collection, id, value) => db.runTransaction(async tx => tx.set(ref(collection, id), value));
  const read = (collection, id) => db.runTransaction(async tx => (await tx.get(ref(collection, id))).data());
  return {uid, token, db, state, input, deps, service, write, read};
}

test('verified credit creates one receipt, one ledger and recharge-only VIP', async () => {
  const f = fixture();
  await f.write('wallets', f.uid, {coins: 50, vipPoints: 99999, rechargeDiamondsTotal: 600, giftScore: 20});
  const result = await f.service(f.input);
  assert.equal(result.balance, 150);
  assert.equal(result.alreadyCredited, false);
  const wallet = await f.read('wallets', f.uid);
  assert.equal(wallet.vipPoints, 700);
  assert.equal(wallet.giftScore, 20);
  assert.equal((await f.read('play_purchase_receipts', hash(f.token))).coins, 100);
  assert.equal((await f.read('wallet_ledger', `play_${hash(f.token)}`)).coins, 100);
  assert.equal(f.state.notified, 1);
});

for (const [name, change] of [
  ['pending', {purchaseState: 2}], ['cancelled', {purchaseState: 1}],
  ['null state coercion', {purchaseState: null}], ['boolean state coercion', {purchaseState: false}],
  ['string state coercion', {purchaseState: '0'}], ['missing state', {purchaseState: undefined}],
  ['unbound account', {obfuscatedExternalAccountId: undefined}],
  ['wrong account', {obfuscatedExternalAccountId: hash('someone-else')}],
  ['different product', {productId: 'king_coins_1300'}],
  ['license-test purchase in paid wallet', {purchaseType: 0}],
  ['promotional purchase in paid wallet', {purchaseType: 1}],
  ['rewarded purchase in paid wallet', {purchaseType: 2}],
  ['multi quantity', {quantity: 2}], ['zero quantity', {quantity: 0}],
  ['null quantity', {quantity: null}], ['string quantity', {quantity: '1'}],
  ['fully refunded', {refundableQuantity: 0}],
  ['missing consumption state', {consumptionState: undefined}],
  ['consumed without receipt', {consumptionState: 1}]
]) test(`rejects ${name} without a wallet write or consumption`, async () => {
  const f = fixture(); Object.assign(f.state.purchase, change);
  await assert.rejects(f.service(f.input));
  assert.equal(await f.read('wallets', f.uid), undefined);
  assert.equal(f.state.consumed, 0);
});

test('omitted optional product/quantity fields follow documented Play defaults', async () => {
  const f = fixture();
  delete f.state.purchase.productId; delete f.state.purchase.quantity;
  delete f.state.purchase.refundableQuantity;
  assert.equal((await f.service(f.input)).balance, 100);
});

test('concurrent duplicate tokens grant exactly one credit and notification', async () => {
  const f = fixture();
  const result = await Promise.all(Array.from({length: 6}, () => f.service(f.input)));
  assert.equal(result.filter(x => !x.alreadyCredited).length, 1);
  assert.equal((await f.read('wallets', f.uid)).coins, 100);
  assert.equal((await f.read('wallets', f.uid)).rechargeDiamondsTotal, 100);
  assert.equal(f.state.notified, 1);
});

test('duplicate after spending returns current balance, not historic receipt balance', async () => {
  const f = fixture(); await f.service(f.input);
  await f.write('wallets', f.uid, {coins: 7, rechargeDiamondsTotal: 100});
  f.state.purchase.consumptionState = 1;
  const result = await f.service(f.input);
  assert.equal(result.balance, 7); assert.equal(result.alreadyCredited, true);
  assert.equal(f.state.consumed, 1);
});

test('transaction callback retry cannot retain an uncommitted newly-credited flag', async () => {
  const f = fixture(); let notified = 0;
  const service = createPurchaseService({...f.deps, notify: async () => notified++, db: {
    collection: f.db.collection.bind(f.db),
    runTransaction: async fn => {
      // First attempt is aborted by contention and all its writes are discarded.
      await fn({get: async () => ({exists: false}), set: () => {}});
      // A competing request commits this token before the callback is retried.
      await f.write('play_purchase_receipts', hash(f.token), {uid: f.uid, productId: f.input.productId, coins: 100});
      await f.write('wallets', f.uid, {coins: 100, rechargeDiamondsTotal: 100});
      return f.db.runTransaction(fn);
    }
  }});
  assert.equal((await service(f.input)).alreadyCredited, true);
  assert.equal(notified, 0);
});

test('consumption failure is explicit and retry never grants a second credit', async () => {
  const f = fixture(); f.state.failConsume = true;
  assert.equal((await f.service(f.input)).consumptionPending, true);
  f.state.failConsume = false;
  const result = await f.service(f.input);
  assert.equal(result.consumptionPending, false); assert.equal(result.alreadyCredited, true);
  assert.equal((await f.read('wallets', f.uid)).coins, 100);
});

test('notification failure preserves successful wallet transaction', async () => {
  const f = fixture(); f.state.failNotify = true;
  assert.equal((await f.service(f.input)).ok, true);
  assert.equal((await f.service(f.input)).alreadyCredited, true);
});

for (const patch of [{uid: 'other'}, {productId: 'king_coins_600'}, {coins: 600}, {status: 'refunded'}, {refundedAt: 123}])
  test(`rejects conflicting or revoked receipt ${JSON.stringify(patch)}`, async () => {
    const f = fixture();
    await f.write('play_purchase_receipts', hash(f.token), {uid: f.uid, productId: f.input.productId, coins: 100, ...patch});
    await assert.rejects(f.service(f.input)); assert.equal(f.state.consumed, 0);
  });

test('banned account cannot recharge', async () => {
  const f = fixture(); await f.write('bans', f.uid, {active: true});
  await assert.rejects(f.service(f.input), {code: 'permission-denied'});
  assert.equal(await f.read('wallets', f.uid), undefined);
});

test('overflow and corrupt wallet fail without a partial receipt', async () => {
  const f = fixture();
  for (const coins of [1e9, -1, '100', NaN]) {
    await f.write('wallets', f.uid, {coins});
    await assert.rejects(f.service(f.input));
    assert.equal(await f.read('play_purchase_receipts', hash(f.token)), undefined);
  }
});

test('verification outage never leaks a token or mutates a wallet', async () => {
  const f = fixture(); f.state.failVerify = true;
  await assert.rejects(f.service(f.input), e => e.code === 'unavailable' && !e.message.includes(f.token));
  assert.equal(await f.read('wallets', f.uid), undefined);
});

test('invalid product/prototype key/token rejected before contacting Play', async () => {
  const f = fixture();
  for (const productId of ['constructor', '__proto__', 'unknown', null])
    await assert.rejects(f.service({...f.input, productId}), {code: 'invalid-argument'});
  for (const purchaseToken of ['', ' ', ' token ', 'x'.repeat(4097), null])
    await assert.rejects(f.service({...f.input, purchaseToken}), {code: 'invalid-argument'});
  assert.equal(f.state.verifyCalls, 0);
});
