'use strict';
// Server-owned KING Plus paid Gift prices. User-controlled cost is NEVER authoritative.
// Only the original noncash-out in-app cosmetic catalog is supported.
const names=[
 'Gold Rose','Love Heart','Diamond Ring','Sweet Kiss','Lucky Star','Birthday Cake','Magic Wand','Coffee Date',
 'Ice Cream','Candy Box','Teddy Bear','Perfume','Gold Teapot','Gold Mask','Luxury Bag','Jeweled Box',
 'Crystal Swan','Royal Throne','Moon Palace','Golden Horse','Golden Eagle','Royal Crown','Super Car','Sport Bike',
 'Yacht','Private Jet','Helicopter','Rocket','Galaxy Ship','Flying Carpet','Royal Castle','Dream Villa',
 'Firework','Party Bus','Music Stage','DJ Booth','Lucky Fish','Peacock','Butterfly','Unicorn',
 'Panda','Tiger','Lionheart Glory','King Lion','White Wolf','Phoenix','King Dragon','Ice Dragon',
 'Super Star','Diamond Rain','Heart Rain','Galaxy','Universe','Aurora','Meteor Shower','Moon Walk',
 'VIP Crown','Emperor Crown','Royal Scepter','Golden Wings','Angel Wings','Fame Trophy','Champion Cup','KING Throne'
];
const costs=[
 50,58,68,80,94,110,128,150,176,205,240,281,329,385,450,527,
 617,721,844,987,1155,1352,1581,1850,2165,2533,2963,3467,4057,4746,5553,6497,
 7602,8894,10406,12175,14245,16667,19500,22815,26693,31231,36541,42753,50020,58524,
 68473,80113,93733,109667,128311,150124,175645,205504,240440,281315,329138,385092,
 450557,520000,520000,520000,520000
];
if(names.length!==costs.length)throw Error('Server gift catalog is corrupt');
const prices=Object.freeze(Object.fromEntries(names.map((name,i)=>[name,costs[i]])));
const MAX_VALUE=1000000;
function priceFor(nameAndQuantity){
  if(typeof nameAndQuantity!=='string'||nameAndQuantity.length>100)return null;
  // Client must always specify quantity to prevent ambiguity.
  const match=/^(.+?) x([1-9])$/.exec(nameAndQuantity);
  if(!match||!Object.hasOwn(prices,match[1]))return null;
  const qty=Number(match[2]), unit=prices[match[1]], total=qty*unit;
  if(!Number.isSafeInteger(total)||total<1||total>MAX_VALUE)return null;
  return Object.freeze({giftName:match[1],quantity:qty,unit,total});
}
module.exports={prices,priceFor,MAX_VALUE};
