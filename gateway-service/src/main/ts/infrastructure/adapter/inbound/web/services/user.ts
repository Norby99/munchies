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
    try {
      const uri = process.env.USER_SERVICE_URL;
      if (!uri) throw new Error("USER_SERVICE_URL is not defined in .env");

      const res = syncRequest("GET", uri + this.getPath() + id);

      console.log("Called user-service");

      console.log("raw body to string is " + res.body.toString());

      const response = getUserResponseFromJson(res.body.toString());
      console.log("Got this response: " + response);
      if (response.result.type != "GetUserSuccess")
        return response.result as GetUserFailure;
      return response.result as GetUserSuccess;
    } catch (e: any) {
      console.log("Something went wrong");
      console.log("error is " + JSON.stringify(e));
      return new GetUserFailure(JSON.stringify(e));
    }
  }
}
