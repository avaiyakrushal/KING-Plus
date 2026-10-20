'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { DEFAULT_OWNER_SETTINGS, publicOwnerSettings, validateOwnerSettings } = require('./owner-settings');

test('owner settings default to a quiet, non-maintenance app', () => {
  assert.deepEqual(DEFAULT_OWNER_SETTINGS, { announcementEnabled: false, maintenanceMode: false, announcement: '' });
  assert.deepEqual(publicOwnerSettings(), DEFAULT_OWNER_SETTINGS);
});

test('owner settings validate bounded input and public read exposes only intended fields', () => {
  const saved = validateOwnerSettings({ announcementEnabled: true, maintenanceMode: false, announcement: '  Update  ', updatedBy: 'secret' });
  assert.deepEqual(saved, { announcementEnabled: true, maintenanceMode: false, announcement: 'Update' });
  assert.deepEqual(publicOwnerSettings({ ...saved, updatedBy: 'secret', updatedAt: 'private metadata' }), saved);
});

test('owner settings reject wrong types and oversized announcements', () => {
  assert.equal(validateOwnerSettings({ announcementEnabled: 1, maintenanceMode: false, announcement: '' }), null);
  assert.equal(validateOwnerSettings({ announcementEnabled: true, maintenanceMode: false, announcement: 'x'.repeat(301) }), null);
  assert.equal(validateOwnerSettings([]), null);
});
