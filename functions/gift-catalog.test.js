'use strict';
const assert=require('node:assert/strict');
const {prices,priceFor,MAX_VALUE}=require('./gift-catalog');
let n=0;function eq(a,b){assert.deepEqual(a,b);n++}
eq(Object.keys(prices).length,64);
eq(priceFor('Gold Rose x1'),{giftName:'Gold Rose',quantity:1,unit:50,total:50});
eq(priceFor('Love Heart x9'),{giftName:'Love Heart',quantity:9,unit:58,total:522});
eq(priceFor('Diamond Ring x3'),{giftName:'Diamond Ring',quantity:3,unit:68,total:204});
eq(priceFor('Gold Rose x0'),null);
eq(priceFor('Gold Rose x99'),null);
eq(priceFor('Custom Gift x1'),null);
eq(priceFor('KING Throne x9'),null); // exceeds current max gift value
eq(priceFor(null),null);
eq(priceFor('Gold Rose'),null);
eq(MAX_VALUE,1000000);
console.log('PASS KING Plus server-priced noncash paid gifts: '+n+' tests');
