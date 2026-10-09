'use strict';

/** Matches Android LevelSystem.vipThreshold/vipFor. Server owns real VIP points. */
const MAX_VIP = 50;
function threshold(vip) {
  if (!Number.isInteger(vip) || vip <= 0) return 0;
  const level = Math.min(MAX_VIP, vip);
  const early = [0,500,1500,3500,7000,13000,22000,36000,56000,85000,125000,180000,250000];
  if (level <= 12) return early[level];
  const n = level - 12;
  return 250000 + 80000*n + 120000*n*n;
}
function levelFromVerifiedSpend(raw) {
  if (!Number.isSafeInteger(raw) || raw < 0) return 0;
  let level = 0;
  while (level < MAX_VIP && raw >= threshold(level + 1)) level++;
  return level;
}
module.exports = {MAX_VIP, threshold, levelFromVerifiedSpend};
