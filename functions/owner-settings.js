'use strict';

const DEFAULT_OWNER_SETTINGS = Object.freeze({
  announcementEnabled: false,
  maintenanceMode: false,
  announcement: '',
});

function publicOwnerSettings(data = {}) {
  return {
    announcementEnabled: data.announcementEnabled === true,
    maintenanceMode: data.maintenanceMode === true,
    announcement: typeof data.announcement === 'string' ? data.announcement.trim().slice(0, 300) : '',
  };
}

function validateOwnerSettings(input) {
  if (!input || typeof input !== 'object' || Array.isArray(input) ||
      typeof input.announcementEnabled !== 'boolean' ||
      typeof input.maintenanceMode !== 'boolean' ||
      typeof input.announcement !== 'string' || input.announcement.length > 300) {
    return null;
  }
  return {
    announcementEnabled: input.announcementEnabled,
    maintenanceMode: input.maintenanceMode,
    announcement: input.announcement.trim(),
  };
}

module.exports = { DEFAULT_OWNER_SETTINGS, publicOwnerSettings, validateOwnerSettings };
