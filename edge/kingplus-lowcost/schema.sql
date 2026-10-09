-- Cloudflare D1. Apply: wrangler d1 execute kingplus-auth-pay --remote --file=schema.sql
-- A successful captured payment is credited by a SQLite trigger in the SAME
-- transaction as a pending->captured transition; retries credit exactly once.
CREATE TABLE IF NOT EXISTS otp_requests (
  phone TEXT PRIMARY KEY,
  requested_at INTEGER NOT NULL,
  expires_at INTEGER NOT NULL,
  attempts INTEGER NOT NULL DEFAULT 0,
  used INTEGER NOT NULL DEFAULT 0
);
CREATE TABLE IF NOT EXISTS otp_ip_windows (
  ip_hash TEXT PRIMARY KEY,
  window_start INTEGER NOT NULL,
  sent_count INTEGER NOT NULL DEFAULT 0
);
CREATE TABLE IF NOT EXISTS recharge_orders (
  order_id TEXT PRIMARY KEY,
  firebase_uid TEXT NOT NULL,
  sku TEXT NOT NULL,
  diamonds INTEGER NOT NULL CHECK (diamonds>0),
  amount_paise INTEGER NOT NULL CHECK(amount_paise>0),
  currency TEXT NOT NULL CHECK(currency='INR'),
  status TEXT NOT NULL DEFAULT 'created'
    CHECK(status IN ('created','captured','refunded')),
  payment_id TEXT UNIQUE,
  created_at INTEGER NOT NULL,
  captured_at INTEGER
);
CREATE TABLE IF NOT EXISTS wallets (
  firebase_uid TEXT PRIMARY KEY,
  diamonds INTEGER NOT NULL DEFAULT 0 CHECK(diamonds>=0),
  recharge_total INTEGER NOT NULL DEFAULT 0 CHECK(recharge_total>=0),
  created_at INTEGER NOT NULL DEFAULT (unixepoch()),
  updated_at INTEGER NOT NULL DEFAULT (unixepoch())
);
CREATE TABLE IF NOT EXISTS recharge_ledger (
  order_id TEXT PRIMARY KEY,
  firebase_uid TEXT NOT NULL,
  payment_id TEXT NOT NULL UNIQUE,
  diamonds INTEGER NOT NULL,
  credited_at INTEGER NOT NULL
);
CREATE TRIGGER IF NOT EXISTS verified_payment_credit
AFTER UPDATE OF status ON recharge_orders
WHEN OLD.status='created' AND NEW.status='captured'
BEGIN
  INSERT INTO wallets(firebase_uid,diamonds,recharge_total,updated_at)
  VALUES (NEW.firebase_uid,NEW.diamonds,NEW.diamonds,unixepoch())
  ON CONFLICT(firebase_uid) DO UPDATE SET
    diamonds=wallets.diamonds+excluded.diamonds,
    recharge_total=wallets.recharge_total+excluded.recharge_total,
    updated_at=unixepoch();
  INSERT INTO recharge_ledger(order_id,firebase_uid,payment_id,diamonds,credited_at)
  VALUES(NEW.order_id,NEW.firebase_uid,NEW.payment_id,NEW.diamonds,unixepoch());
END;
CREATE INDEX IF NOT EXISTS idx_recharge_by_uid
 ON recharge_orders(firebase_uid,created_at DESC);
