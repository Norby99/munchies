// suggestion-modules.js
const generated = require("./munchies-suggestion-shared.js");
const _suggestion = generated.com.munchies.suggestion;
const _inbound = _suggestion.infrastructure.adapter.inbound;
const _outbound = _suggestion.infrastructure.adapter.outbound;
const _dto = _suggestion.infrastructure.adapter.dto;

module.exports = {
  // DTOs
  MenuItemDTO: _dto.MenuItemDTO,
  SuggestedMenuItemDTO: _dto.SuggestedMenuItemDTO,
  SuggestionConfidenceDTO: _dto.SuggestionConfidenceDTO,
  SuggestionRequestDTO: _dto.SuggestionRequestDTO,
  SuggestionResponseDTO: _dto.SuggestionResponseDTO,
  UserPreferenceDTO: _dto.UserPreferenceDTO,

  // Config
  SuggestionServiceConfig: _inbound.web.config.SuggestionServiceConfig,

  // API
  SuggestionAPI: _inbound.SuggestionAPI,
  SuggestMenuItemAPI: _inbound.JsSuggestMenuItemAPI,

  // Request & Deserializer
  SuggestItemRequest: _inbound.request.SuggestItemRequest,
  suggestItemRequestFromJson: _inbound.request.suggestItemRequestFromJson,

  // Response & Deserializer
  SuggestItemResponse: _outbound.response.SuggestItemResponse,
  suggestItemResponseFromJson: _outbound.response.suggestItemResponseFromJson,
  getSuggestItemResponseFromJson: _outbound.response.getSuggestItemResponseFromJson,
};
