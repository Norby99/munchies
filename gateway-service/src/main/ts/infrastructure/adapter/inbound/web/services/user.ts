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

import axios from "axios";
axios.defaults.validateStatus = (status: number) => status <= 500;

import type { AxiosResponse } from "axios";

export class GetUser extends GetUserAPI {
  async getUser(id: string): Promise<GetUserResult> {
    try {
      const uri = process.env.USER_SERVICE_URL;
      if (!uri) throw new Error("USER_SERVICE_URL is not defined in .env");

      const result = axios.get(uri + this.getPath() + id).then(value => {
        
        const response = getUserResponseFromJson(JSON.stringify(value.data))
        
        if (response.result.type != "GetUserSuccess")
          return response.result as GetUserFailure;
        return response.result as GetUserSuccess;
      }).catch(e => {
        console.log("axios error: " + JSON.stringify(e).toString());
        return new GetUserFailure(e);
      });
      
      return result;
        
    }
    catch(e: any){
      console.log("Something went wrong");
      console.log("error is " + JSON.stringify(e));
      return Promise.resolve(new GetUserFailure(JSON.stringify(e)));
    }
  }
}
