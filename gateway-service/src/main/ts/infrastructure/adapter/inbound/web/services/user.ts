import { com } from "munchies-user-service-shared";
import {
  UserDTO,
  GetUserAPI,
  GetUserResponse,
  GetUserFailure,
  GetUserResult,
  GetUserSuccess,
  getUserResponseFromJson,
} from "munchies-user-service-shared/kotlin/user-modules";

import syncRequest from "sync-request";

export class GetUser extends GetUserAPI {
  getUser(id: string): GetUserResult {
    const uri = "http://user-service:8080"; //process.env.USER_SERVICE_URL;
    if (!uri) throw new Error("USER_SERVICE_URL is not defined in .env");

    try {
      const res = syncRequest("GET", uri + this.getPath() + id);

      console.log("Called user-service");

      const response = getUserResponseFromJson(res.getBody("utf8"));
      console.log("Got this response: " + response);
      if (response.result.type != "Success")
        throw new Error((response.result as GetUserFailure).reason);
      console.log("response is not failure");
      return response.result;
    } catch (e: any) {
      console.log("response is a failure");
      return new GetUserFailure(e.toString());
    }
  }
}
