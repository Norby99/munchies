import { Request, RequestHandler, Response } from "express";
import {
  HttpMethod,
  AuthRole,
  ErrorResponse,
} from "munchies-commons/kotlin/commons-modules";
import {
  RestaurantServiceConfig,
  SearchRestaurantsAPI,
  SearchRestaurantsResponse,
} from "munchies-restaurant-service-shared/kotlin/restaurant-modules";
import { AuthedRequest } from "../../auth";
import { request } from "../internal-client";
import { SimpleRoute } from "../simple-route";

/**
 * GET /restaurants/?q=<text>: customer search of restaurants by name or address. Only the `q`
 * query parameter is forwarded (anything else the client adds is dropped); a missing or blank
 * `q` lists every restaurant.
 */
export class SearchRestaurantsRoute
  extends SearchRestaurantsAPI<SearchRestaurantsResponse | ErrorResponse>
  implements SimpleRoute<SearchRestaurantsResponse>
{
  constructor() {
    super();
    let api = this;

    this.path = api.getPath();
    this.method = api.getMethod();
    this.authRole = api.getRequiredAuthRole();
  }

  path: string;
  method: HttpMethod;
  authRole: AuthRole | null;

  async searchRestaurants(
    query: string,
  ): Promise<SearchRestaurantsResponse | ErrorResponse> {
    const uri = process.env.RESTAURANT_SERVICE_URL;
    if (!uri)
      return Promise.resolve(
        new ErrorResponse("Missing Restaurant Service URL", 500),
      );

    const params = new URLSearchParams({
      [RestaurantServiceConfig.SEARCH_QUERY_PARAM]: query,
    });
    return request<SearchRestaurantsResponse>(
      `${uri}${this.path}?${params.toString()}`,
      this.method,
      "",
      this.parseResponse,
      this.parseError,
    );
  }

  private handler: {
    forward: (
      req: AuthedRequest,
    ) => Promise<SearchRestaurantsResponse | ErrorResponse>;
    respond: RequestHandler;
  } = {
    forward: async (req: AuthedRequest) => {
      try {
        // Express parses a repeated parameter (?q=a&q=b) into an array: keep only the first.
        const raw = req.query?.[RestaurantServiceConfig.SEARCH_QUERY_PARAM];
        const query = Array.isArray(raw) ? String(raw[0] ?? "") : String(raw ?? "");
        return await this.searchRestaurants(query);
      } catch (err: any) {
        return new ErrorResponse(
          "SearchRestaurants forward: \n" + String(err),
          500,
        );
      }
    },
    respond: async (req: Request, res: Response) => {
      const result = await this.forward(req as AuthedRequest);
      res.status(result.code).type("json").send(result.toJson());
    },
  };

  forward = this.handler.forward;
  respond = this.handler.respond;
}
