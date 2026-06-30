import {
  GetUserAPI,
  GetUserResponse,
  GetUserFailure,
  GetUserResult,
  GetUserSuccess,
  getUserResponseFromJson,
} from "munchies-user-service-shared/kotlin/user-modules";

import { request as axiosRequest } from "@main/infrastructure/adapter/middleware/route";

import axios from "axios";

export class GetUser extends GetUserAPI {
  async getUser(id: string): Promise<GetUserResponse> {
    try {
      const uri = process.env.USER_SERVICE_URL;
      if (!uri) throw new Error("USER_SERVICE_URL is not defined in .env");
      return axiosRequest(
        uri + this.getPath() + id,
        this.getMethod(),
        () => "",
        getUserResponseFromJson,
        (result: GetUserResult) => {
          if (result.type == GetUserSuccess.name)
            return result as GetUserSuccess;
          else return result as GetUserFailure;
        },
        (result: GetUserResult) => new GetUserResponse(result),
        (err) => new GetUserFailure(err),
      );
    } catch (e: any) {
      console.log("Something went wrong");
      console.log("error is " + JSON.stringify(e));
      return Promise.resolve(
        new GetUserResponse(new GetUserFailure(JSON.stringify(e))),
      );
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
  registerUserResponseFromJson,
} from "munchies-user-service-shared/kotlin/user-modules";

export class RegisterUser extends RegisterUserAPI {
  registerUser(request: RegisterUserRequest): Promise<RegisterUserResponse> {
    try {
      const uri = process.env.USER_SERVICE_URL;
      if (!uri) throw new Error("USER_SERVICE_URL is not defined in .env");

      return axiosRequest(
        uri,
        this.getMethod(),
        request.toJson,
        registerUserResponseFromJson,
        (result: RegisterUserResult) => {
          if (result.type == RegisterUserSuccess.name)
            return result as RegisterUserSuccess;
          else return result as RegisterUserFailure;
        },
        (result: RegisterUserResult) => new RegisterUserResponse(result),
        (err) => new RegisterUserFailure(err),
      );
    } catch (e: any) {
      console.log("Something went wrong");
      console.log("error is " + JSON.stringify(e));
      return Promise.resolve(
        new RegisterUserResponse(new RegisterUserFailure(JSON.stringify(e))),
      );
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
  loginUserResponseFromJson,
} from "munchies-user-service-shared/kotlin/user-modules";
export class LoginUser extends LoginUserAPI {
  loginUser(request: LoginUserRequest): Promise<LoginUserResponse> {
    try {
      const uri = process.env.USER_SERVICE_URL;
      if (!uri) throw new Error("USER_SERVICE_URL is not defined in .env");

      return axiosRequest(
        uri + this.getPath(),
        this.getMethod(),
        request.toJson,
        loginUserResponseFromJson,
        (result) => {
          if (result.type == LoginUserSuccess.name)
            return result as LoginUserSuccess;
          else return result as LoginUserFailure;
        },
        (result: LoginUserResult) => new LoginUserResponse(result),
        (err) => new LoginUserFailure(err),
      );
    } catch (e: any) {
      console.log("Something went wrong");
      console.log("error is " + JSON.stringify(e));
      return Promise.resolve(
        new LoginUserResponse(new LoginUserFailure(JSON.stringify(e))),
      );
    }
  }
}

import {
  UpdateUserInfoAPI,
  UpdateUserInfoRequest,
  UpdateUserInfoResponse,
  UpdateUserInfoResult,
  UpdateUserInfoSuccess,
  UpdateUserInfoFailure,
  updateUserInfoResponseFromJson,
} from "munchies-user-service-shared/kotlin/user-modules";
export class UpdateUserInfo extends UpdateUserInfoAPI {
  updateUserInfo(
    request: UpdateUserInfoRequest,
  ): Promise<UpdateUserInfoResponse> {
    try {
      const uri = process.env.USER_SERVICE_URL;
      if (!uri) throw new Error("USER_SERVICE_URL is not defined in .env");

      return axiosRequest(
        uri + this.getPath(),
        this.getMethod(),
        request.toJson,
        updateUserInfoResponseFromJson,
        (result) => {
          if (result.type == UpdateUserInfoSuccess.name)
            return result as UpdateUserInfoSuccess;
          else return result as UpdateUserInfoFailure;
        },
        (result: UpdateUserInfoResult) => new UpdateUserInfoResponse(result),
        (err) => new UpdateUserInfoFailure(err),
      );
    } catch (e: any) {
      console.log("Something went wrong");
      console.log("error is " + JSON.stringify(e));
      return Promise.resolve(
        new UpdateUserInfoResponse(
          new UpdateUserInfoFailure(JSON.stringify(e)),
        ),
      );
    }
  }
}

import {
  UpdateUserPasswordAPI,
  UpdateUserPasswordRequest,
  UpdateUserPasswordResponse,
  UpdateUserPasswordResult,
  UpdateUserPasswordSuccess,
  UpdateUserPasswordFailure,
  updateUserPasswordResponseFromJson,
} from "munchies-user-service-shared/kotlin/user-modules";
export class UpdateUserPassword extends UpdateUserPasswordAPI {
  updateUserPassword(
    request: UpdateUserPasswordRequest,
  ): Promise<UpdateUserPasswordResponse> {
    try {
      const uri = process.env.USER_SERVICE_URL;
      if (!uri) throw new Error("USER_SERVICE_URL is not defined in .env");

      return axiosRequest(
        uri + this.getPath(),
        this.getMethod(),
        request.toJson,
        updateUserPasswordResponseFromJson,
        (result) => {
          if (result.type == UpdateUserPasswordSuccess.name)
            return result as UpdateUserPasswordSuccess;
          else return result as UpdateUserPasswordFailure;
        },
        (result: UpdateUserPasswordResult) =>
          new UpdateUserPasswordResponse(result),
        (err) => new UpdateUserPasswordFailure(err),
      );
    } catch (e: any) {
      console.log("Something went wrong");
      console.log("error is " + JSON.stringify(e));
      return Promise.resolve(
        new UpdateUserPasswordResponse(
          new UpdateUserPasswordFailure(JSON.stringify(e)),
        ),
      );
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
  deleteUserResponseFromJson,
} from "munchies-user-service-shared/kotlin/user-modules";
export class DeleteUser extends DeleteUserAPI {
  deleteUser(request: DeleteUserRequest): Promise<DeleteUserResponse> {
    try {
      const uri = process.env.USER_SERVICE_URL;
      if (!uri) throw new Error("USER_SERVICE_URL is not defined in .env");

      return axiosRequest(
        uri + this.getPath(),
        this.getMethod(),
        request.toJson,
        deleteUserResponseFromJson,
        (result) => {
          if (result.type == DeleteUserSuccess.name)
            return result as DeleteUserSuccess;
          else return result as DeleteUserFailure;
        },
        (result: DeleteUserResult) =>
          new DeleteUserResponse(result),
        (err) => new DeleteUserFailure(err),
      );
    } catch (e: any) {
      console.log("Something went wrong");
      console.log("error is " + JSON.stringify(e));
      return Promise.resolve(
        new DeleteUserResponse(new DeleteUserFailure(JSON.stringify(e))),
      );
    }
  }
}
