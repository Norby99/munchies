// suggestion-modules.d.ts
import type { com } from "./munchies-suggestion-shared";

export type MenuItemDTO = com.munchies.suggestion.infrastructure.adapter.dto.MenuItemDTO;
export declare const MenuItemDTO: typeof com.munchies.suggestion.infrastructure.adapter.dto.MenuItemDTO;

export type SuggestedMenuItemDTO =
  com.munchies.suggestion.infrastructure.adapter.dto.SuggestedMenuItemDTO;
export declare const SuggestedMenuItemDTO: typeof
  com.munchies.suggestion.infrastructure.adapter.dto.SuggestedMenuItemDTO;

export type SuggestionConfidenceDTO =
  com.munchies.suggestion.infrastructure.adapter.dto.SuggestionConfidenceDTO;
export declare const SuggestionConfidenceDTO: typeof
  com.munchies.suggestion.infrastructure.adapter.dto.SuggestionConfidenceDTO;

export type SuggestionRequestDTO =
  com.munchies.suggestion.infrastructure.adapter.dto.SuggestionRequestDTO;
export declare const SuggestionRequestDTO: typeof
  com.munchies.suggestion.infrastructure.adapter.dto.SuggestionRequestDTO;

export type SuggestionResponseDTO =
  com.munchies.suggestion.infrastructure.adapter.dto.SuggestionResponseDTO;
export declare const SuggestionResponseDTO: typeof
  com.munchies.suggestion.infrastructure.adapter.dto.SuggestionResponseDTO;

export type UserPreferenceDTO =
  com.munchies.suggestion.infrastructure.adapter.dto.UserPreferenceDTO;
export declare const UserPreferenceDTO: typeof
  com.munchies.suggestion.infrastructure.adapter.dto.UserPreferenceDTO;

export declare const SuggestionServiceConfig: typeof
  com.munchies.suggestion.infrastructure.adapter.inbound.web.config.SuggestionServiceConfig;

export type SuggestionAPI =
  com.munchies.suggestion.infrastructure.adapter.inbound.SuggestionAPI;
export declare const SuggestionAPI: typeof
  com.munchies.suggestion.infrastructure.adapter.inbound.SuggestionAPI;

export type SuggestMenuItemAPI =
  com.munchies.suggestion.infrastructure.adapter.inbound.JsSuggestMenuItemAPI;
export declare const SuggestMenuItemAPI: typeof
  com.munchies.suggestion.infrastructure.adapter.inbound.JsSuggestMenuItemAPI;

export type SuggestItemRequest =
  com.munchies.suggestion.infrastructure.adapter.inbound.request.SuggestItemRequest;
export declare const SuggestItemRequest: typeof
  com.munchies.suggestion.infrastructure.adapter.inbound.request.SuggestItemRequest;

export declare const suggestItemRequestFromJson: typeof
  com.munchies.suggestion.infrastructure.adapter.inbound.request.suggestItemRequestFromJson;

export type SuggestItemResponse =
  com.munchies.suggestion.infrastructure.adapter.outbound.response.SuggestItemResponse;
export declare const SuggestItemResponse: typeof
  com.munchies.suggestion.infrastructure.adapter.outbound.response.SuggestItemResponse;

export declare const suggestItemResponseFromJson: typeof
  com.munchies.suggestion.infrastructure.adapter.outbound.response.suggestItemResponseFromJson;
export declare const getSuggestItemResponseFromJson: typeof
  com.munchies.suggestion.infrastructure.adapter.outbound.response.getSuggestItemResponseFromJson;
