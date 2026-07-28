import axios from "axios";
import {
  CreateRestaurantAPI,
  CreateRestaurantRequest,
  CreateRestaurantResponse,
} from "munchies-restaurant-service-shared/kotlin/restaurant-modules";
import { ErrorResponse } from "munchies-commons/kotlin/commons-modules";
import { SimpleRoute, createSimpleRoute } from "../simple-route";

export class CreateRestaurantRoute
  extends CreateRestaurantAPI
  implements SimpleRoute<CreateRestaurantResponse>
{
  path = this.getPath();
  method = this.getMethod();
  authRole = this.getRequiredAuthRole();
  onAuthFail = (msg: string) => new ErrorResponse(msg);

  async createRestaurant(
    request: CreateRestaurantRequest
  ): Promise<CreateRestaurantResponse> {
    const uri = process.env.RESTAURANT_SERVICE_URL;
    if (!uri) throw new ErrorResponse("Missing Restaurant Service URL");
    const response = await axios.post(
      uri + this.path,
      request.toJson(),
      {
        headers: { "Content-Type": "application/json" },
        transformResponse: [(data) => data],
        validateStatus: () => true,
      }
    );
    if (response.status >= 400) {
      throw this.parseError(response.data);
    }
    return this.parseResponse(response.data);
  }

  private handler = createSimpleRoute(
    (json) => this.parseRequest(json),
    async (request) => {
      return this.createRestaurant(request);
    },
    201,
  );

  forward = this.handler.forward;
  respond = this.handler.respond;
}
