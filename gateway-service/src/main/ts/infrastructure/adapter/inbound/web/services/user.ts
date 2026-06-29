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

import axios from "axios";
axios.defaults.validateStatus = (status: number) => status <= 500;

export class GetUser extends GetUserAPI {
  async getUser(id: string): Promise<GetUserResponse> {
    try {
      const uri = process.env.USER_SERVICE_URL;
      if (!uri) throw new Error("USER_SERVICE_URL is not defined in .env");

      const result = axios.get(uri + this.getPath() + id).then(value => {

        const response = getUserResponseFromJson(JSON.stringify(value.data))

        if (response.result.type != "GetUserSuccess")
          return new GetUserResponse(response.result as GetUserFailure);
        return new GetUserResponse(response.result as GetUserSuccess);
      }).catch(e => {
        console.log("axios error: " + JSON.stringify(e).toString());
        return new GetUserResponse(new GetUserFailure(e));
      });
      
      return result;
        
    }
    catch(e: any){
      console.log("Something went wrong");
      console.log("error is " + JSON.stringify(e));
      return Promise.resolve( new GetUserResponse( new GetUserFailure(JSON.stringify(e))));
    }
  }
}

import {
  RegisterUserAPI,
    RegisterUserResponse,
    RegisterUserResult,
    RegisterUserSuccess,
    RegisterUserFailure,
    RegisterUserRequest,
    registerUserResponseFromJson
} from "munchies-user-service-shared/kotlin/user-modules"

export class RegisterUser extends RegisterUserAPI{
  registerUser(request: RegisterUserRequest): Promise<RegisterUserResponse> {
    try {
      const uri = process.env.USER_SERVICE_URL;
      if (!uri) throw new Error("USER_SERVICE_URL is not defined in .env");
    
      const result = axios.post(
        uri + this.getPath(),
        request.toJson()
      ).then(value => {
        const response = registerUserResponseFromJson(JSON.stringify(value.data))
        if (response.result.type != "RegisterUserSuccess")
          return new RegisterUserResponse(response.result as RegisterUserFailure);
        return new RegisterUserResponse(response.result as RegisterUserSuccess);
        }).catch(e => {
          console.log("axios error: " + JSON.stringify(e).toString());
          return new RegisterUserResponse(new RegisterUserFailure(e));
        });
          
      return result;
            
      }
      catch(e: any){
        console.log("Something went wrong");
        console.log("error is " + JSON.stringify(e));
        return Promise.resolve(new RegisterUserResponse(new RegisterUserFailure(JSON.stringify(e))));
      }
  }
}

import {
  LoginUserAPI,
  LoginUserRequest,
  LoginUserResponse,
  LoginUserResult,
  LoginUserSuccess,
  LoginUserFailure,
  loginUserResponseFromJson
} from "munchies-user-service-shared/kotlin/user-modules"
export class LoginUser extends LoginUserAPI{
  loginUser(request: LoginUserRequest): Promise<LoginUserResponse> {
    try {
          const uri = process.env.USER_SERVICE_URL;
          if (!uri) throw new Error("USER_SERVICE_URL is not defined in .env");
        
          const result = axios.post(
            uri + this.getPath(),
            request.toJson()
          ).then(value => {
            const response = loginUserResponseFromJson(JSON.stringify(value.data))
            if (response.result.type != "LoginUserSuccess")
              return new LoginUserResponse(response.result as LoginUserFailure);
            return new LoginUserResponse(response.result as LoginUserSuccess);
            }).catch(e => {
              console.log("axios error: " + JSON.stringify(e).toString());
              return new LoginUserResponse(new LoginUserFailure(e));
            });
              
          return result;
                
          }
          catch(e: any){
            console.log("Something went wrong");
            console.log("error is " + JSON.stringify(e));
            return Promise.resolve(new LoginUserResponse(new LoginUserFailure(JSON.stringify(e))));
          }  }
}




import {
  UpdateUserInfoAPI,
  UpdateUserInfoRequest,
  UpdateUserInfoResponse,
  UpdateUserInfoResult,
  UpdateUserInfoSuccess,
  UpdateUserInfoFailure,
  updateUserInfoResponseFromJson
} from "munchies-user-service-shared/kotlin/user-modules"
export class UpdateUserInfo extends UpdateUserInfoAPI{
  updateUserInfo(request: UpdateUserInfoRequest): Promise<UpdateUserInfoResponse> {
    try {
              const uri = process.env.USER_SERVICE_URL;
              if (!uri) throw new Error("USER_SERVICE_URL is not defined in .env");
            
              const result = axios.post(
                uri + this.getPath(),
                request.toJson()
              ).then(value => {
                const response = updateUserInfoResponseFromJson(JSON.stringify(value.data))
                if (response.result.type != "UpdateUserInfoSuccess")
                  return new UpdateUserInfoResponse(response.result as UpdateUserInfoFailure);
                return new UpdateUserInfoResponse(response.result as UpdateUserInfoSuccess);
                }).catch(e => {
                  console.log("axios error: " + JSON.stringify(e).toString());
                  return new UpdateUserInfoResponse(new UpdateUserInfoFailure(e));
                });
                  
              return result;
                    
              }
              catch(e: any){
                console.log("Something went wrong");
                console.log("error is " + JSON.stringify(e));
                return Promise.resolve(new UpdateUserInfoResponse(new UpdateUserInfoFailure(JSON.stringify(e))));
              }  }
}

import {
  UpdateUserPasswordAPI,
  UpdateUserPasswordRequest,
  UpdateUserPasswordResponse,
  UpdateUserPasswordResult,
  UpdateUserPasswordSuccess,
  UpdateUserPasswordFailure,
  updateUserPasswordResponseFromJson
} from "munchies-user-service-shared/kotlin/user-modules"
export class UpdateUserPassword extends UpdateUserPasswordAPI{
  updateUserPassword(request: UpdateUserPasswordRequest): Promise<UpdateUserPasswordResponse> {
    try {
              const uri = process.env.USER_SERVICE_URL;
              if (!uri) throw new Error("USER_SERVICE_URL is not defined in .env");
            
              const result = axios.post(
                uri + this.getPath(),
                request.toJson()
              ).then(value => {
                const response = updateUserPasswordResponseFromJson(JSON.stringify(value.data))
                if (response.result.type != "UpdateUserPasswordSuccess")
                  return new UpdateUserPasswordResponse(response.result as UpdateUserPasswordFailure);
                return new UpdateUserPasswordResponse(response.result as UpdateUserPasswordSuccess);
                }).catch(e => {
                  console.log("axios error: " + JSON.stringify(e).toString());
                  return new UpdateUserPasswordResponse(new UpdateUserPasswordFailure(e));
                });
                  
              return result;
                    
              }
              catch(e: any){
                console.log("Something went wrong");
                console.log("error is " + JSON.stringify(e));
                return Promise.resolve(new UpdateUserPasswordResponse(new UpdateUserPasswordFailure(JSON.stringify(e))));
              } 
  }
}

import {
  DeleteUserAPI,
  DeleteUserRequest,
  DeleteUserResponse,
  DeleteUserResult,
  DeleteUserSuccess,
  DeleteUserFailure,
  deleteUserResponseFromJson
} from "munchies-user-service-shared/kotlin/user-modules"
export class DeleteUser{
  
}

