// common-modules.js
const generated = require("./munchies-commons.js");
const _commons = generated.com.munchies.commons;

module.exports = {
  UUIDEntityId: _commons.UUIDEntityId,
  newId: _commons.UUIDEntityId.Companion.newId
};