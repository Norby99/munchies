// common-modules.js
const generated = require("./munchies-commons.js");
const _commons = generated.com.munchies.commons;

module.exports = {
  UUIDEntityId: _commons.UUIDEntityId,
  newId: _commons.UUIDEntityId.Companion.newId,
  InputValidator: _commons.domain.port.InputValidator,
  InputValidatorResult: _commons.domain.port.InputValidatorResult,
  ValidInput: _commons.domain.port.ValidInput,
  InvalidInput: _commons.domain.port.InvalidInput,
  TokenProvider: _commons.domain.port.TokenProvider,
  GenerateTokenResult: _commons.domain.port.GenerateTokenResult,
  GenerateTokenSuccess: _commons.domain.port.GenerateTokenSuccess,
  GenerateTokenFailure: _commons.domain.port.GenerateTokenFailure,
  ValidateTokenResult: _commons.domain.port.ValidateTokenResult,
  ValidateTokenSuccess: _commons.domain.port.ValidateTokenSuccess,
  ValidateTokenFailure: _commons.domain.port.ValidateTokenFailure,
  RefreshTokenResult: _commons.domain.port.RefreshTokenResult,
  RefreshTokenSuccess: _commons.domain.port.RefreshTokenSuccess,
  RefreshTokenFailure: _commons.domain.port.RefreshTokenFailure,
  JWT_SECRET_ENV_NAME: _commons.domain.port.JWT_SECRET_ENV_NAME,
  ID_CLAIM: _commons.domain.port.ID_CLAIM,
  ROLE_CLAIM: _commons.domain.port.ROLE_CLAIM,
  EXPIRATION_CLAIM: _commons.domain.port.EXPIRATION_CLAIM,
  JWT_SECRET_ALGORITHM: _commons.domain.port.JWT_SECRET_ALGORITHM,
  HttpMethod: _commons.infrastructure.adapter.HttpMethod,
  AuthRole: _commons.domain.port.AuthRole,
  TokenDecoder: _commons.domain.port.TokenDecoder,
  DecodedTokenResult: _commons.domain.port.DecodedTokenResult,
  DecodedTokenSuccess: _commons.domain.port.DecodedTokenSuccess,
  DecodedTokenFailure: _commons.domain.port.DecodedTokenFailure,
  API: _commons.infrastructure.adapter.API,
};