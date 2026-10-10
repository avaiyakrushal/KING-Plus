#!/usr/bin/env python3
"""Regression: D1 SQL trigger credits a captured payment exactly once."""
import sqlite3
from pathlib import Path
conn=sqlite3.connect(':memory:')
schema=Path(__file__).with_name('schema.sql').read_text()
conn.executescript(schema)
conn.execute('INSERT INTO recharge_orders '
             '(order_id,firebase_uid,sku,diamonds,amount_paise,currency,status,created_at) '
             "VALUES ('order_AAA','uidA','diamonds_600',600,49900,'INR','created',1)")
conn.execute("UPDATE recharge_orders SET status='captured',payment_id='pay_AAA' "
             "WHERE order_id='order_AAA' AND status='created'")
assert conn.execute("SELECT diamonds,recharge_total FROM wallets WHERE firebase_uid='uidA'").fetchone()==(600,600)
assert conn.execute("SELECT count(*) FROM recharge_ledger").fetchone()[0]==1
conn.execute("UPDATE recharge_orders SET status='captured',payment_id='pay_AAA' "
             "WHERE order_id='order_AAA' AND status='created'")
assert conn.execute("SELECT diamonds,recharge_total FROM wallets WHERE firebase_uid='uidA'").fetchone()==(600,600)
conn.execute('INSERT INTO recharge_orders '
             '(order_id,firebase_uid,sku,diamonds,amount_paise,currency,status,created_at) '
             "VALUES ('order_BBB','uidA','diamonds_100',100,9000,'INR','created',2)")
conn.execute("UPDATE recharge_orders SET status='captured',payment_id='pay_BBB' "
             "WHERE order_id='order_BBB' AND status='created'")
assert conn.execute("SELECT diamonds,recharge_total FROM wallets WHERE firebase_uid='uidA'").fetchone()==(700,700)
assert conn.execute("SELECT count(*) FROM recharge_ledger").fetchone()[0]==2
# Capturing same payment ID for a different order must not credit either wallet.
conn.execute('INSERT INTO recharge_orders '
             '(order_id,firebase_uid,sku,diamonds,amount_paise,currency,status,created_at) '
             "VALUES ('order_CCC','uidB','diamonds_100',100,9000,'INR','created',3)")
try:
    conn.execute("UPDATE recharge_orders SET status='captured',payment_id='pay_AAA' "
                 "WHERE order_id='order_CCC' AND status='created'")
except sqlite3.IntegrityError:
    pass
else:
    raise AssertionError('Duplicate payment ID was incorrectly credited')
assert conn.execute("SELECT count(*) FROM wallets WHERE firebase_uid='uidB'").fetchone()[0]==0
print('PASS SQLite atomic recharge: same webhook 1 credit, separate valid payments accumulate, token replay rejected')
