import { simpleRequest } from "../simple-internal-client";
import { fillPath } from "../routes";
import { ErrorResponse, HttpMethod } from "munchies-commons/kotlin/commons-modules";

export async function restaurantRequest<Response>(
  path: string,
  method: HttpMethod,
  body: string,
  parseResponse: (json: string) => Response,
  parseError: (json: string) => ErrorResponse,
  ...pathParams: string[]
): Promise<Response | ErrorResponse> {
  const uri = process.env.RESTAURANT_SERVICE_URL;
  if (!uri) return new ErrorResponse("Missing Restaurant Service URL");
  const url = pathParams.length > 0 ? fillPath(uri + path, ...pathParams) : uri + path;
  return simpleRequest(url, method, body, parseResponse, parseError);
}
