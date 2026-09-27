import com.munchies.commons.domain.port.AuthRole
import com.munchies.commons.infrastructure.adapter.HttpMethod
import com.munchies.commons.infrastructure.adapter.HttpMethod.GET
import com.munchies.commons.infrastructure.adapter.SimpleAPI
import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.suggestion.infrastructure.adapter.inbound.SuggestionAPI
import com.munchies.suggestion.infrastructure.adapter.inbound.request.SuggestItemRequest
import com.munchies.suggestion.infrastructure.adapter.inbound.request.suggestItemRequestFromJson
import com.munchies.suggestion.infrastructure.adapter.inbound.web.config.SuggestionServiceConfig
import com.munchies.suggestion.infrastructure.adapter.outbound.response.SuggestItemResponse
import com.munchies.suggestion.infrastructure.adapter.outbound.response.getSuggestItemResponseFromJson
import kotlin.js.Promise



@JsExport
abstract class JsGetUserAPI<E : WebResponse<Any>> : SuggestionAPI<Promise<E>>,
  SimpleAPI<SuggestItemRequest, SuggestItemResponse>() {
  override fun getMethod(): HttpMethod = GET
  override fun getPath(): String = SuggestionServiceConfig.SERVICE_PATH
  override fun getPort(): Int = SuggestionServiceConfig.SERVICE_PORT
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER

  abstract override fun suggestMenuItem(request: SuggestItemRequest): Promise<E>

  override fun parseRequest(json: String): SuggestItemRequest = suggestItemRequestFromJson(json)
  override fun parseResponse(json: String): SuggestItemResponse = getSuggestItemResponseFromJson(json)
}
