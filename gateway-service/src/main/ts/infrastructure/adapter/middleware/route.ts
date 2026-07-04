import axios, { AxiosInstance, AxiosResponse, AxiosStatic } from "axios";
import {
  AuthRole,
  HttpMethod,
  newId,
} from "munchies-commons/kotlin/commons-modules";

const axiosClient = axios.create({
  transformRequest: [(data) => data],
  transformResponse: [(data) => data],
  headers: {
    "Content-Type": "application/json",
    common: {
      "Content-Type": "application/json",
    },
  },
  validateStatus: (status: number) => status <= 500,
});

axiosClient.interceptors.request.use((config) => {
  console.info("OUT:", typeof config.data, config.data);
  return config;
});

axiosClient.interceptors.response.use((response) => {
  console.info("IN:", typeof response.data, response.data);
  return response;
});

function axiosMethodChooser(
  base: AxiosInstance,
  uri: string,
  method: HttpMethod,
  body: string = "",
): Promise<AxiosResponse> {
  switch (method.name) {
    case HttpMethod.POST.name:
      return base.post(uri, body);
    case HttpMethod.DELETE.name:
      return base.delete(uri);
    case HttpMethod.PATCH.name:
      return base.patch(uri, body);
    default:
      return base.get(uri);
  }
}

export async function request<
  Response extends { result: Result; code: number },
  Result extends { type: string },
  Success extends Result,
  Failure extends Result,
>(
  uri: string,
  httpMethod: HttpMethod,
  toJson: () => string,
  fromJson: (json: string) => Response,
  toResult: (result: Result) => Success | Failure,
  toResponse: (result: Success | Failure, code: number) => Response,
  toFailure: (err: string) => Failure,
): Promise<Response> {
  return axiosMethodChooser(axiosClient, uri, httpMethod, toJson())
    .then((value) => {
      console.log("received data: ", value.data);
      const response = fromJson(value.data);
      return toResponse(toResult(response.result), response.code);
    })
    .catch((err) => {
      console.error("-----Error-----");
      console.error("when " + httpMethod.name + " to " + uri);
      console.error("with: " + toJson());
      console.error("result err: ", err);
      console.error("-----End Error-----");
      return toResponse(toFailure(JSON.stringify(err)), 500);
    });
}

export function convertRouteToExpress(path: string): string {
  return path.replace(/\{([^}]+)\}/g, ":$1");
}

export function fillPath(path: string, ...values: (string | number)[]): string {
  let i = 0;
  return path.replace(/\{([^}]+)\}/g, () => {
    if (i >= values.length) {
      throw new Error("Not enough values provided for path params");
    }
    return String(values[i++]);
  });
}

import { API } from "munchies-commons/kotlin/commons-modules";
import {
  Express,
  RequestHandler,
} from "express";
function getRoute<Service extends API>(
  app: Express,
  service: Service,
  ...handlers: RequestHandler[]
): Express {
  switch (service.getMethod().name) {
    case HttpMethod.POST.name:
      return app.post(convertRouteToExpress(service.getPath()), ...handlers);
    case HttpMethod.DELETE.name:
      return app.delete(convertRouteToExpress(service.getPath()), ...handlers);
    case HttpMethod.PATCH.name:
      return app.patch(convertRouteToExpress(service.getPath()), ...handlers);
    default:
      return app.get(convertRouteToExpress(service.getPath()), ...handlers);
  }
}

import { GetUser, RegisterUser } from "../inbound/web/services/user";
import { AuthedRequest, injectCookie, requireAuth, requireRole } from "./auth";
import {
  GetUserResponse,
  GetUserFailure,
  RegisterUserRequest,
  UserDTO,
  RegisterUserResponse,
  RegisterUserFailure,
} from "munchies-user-service-shared/kotlin/user-modules";
export const routes: ((app: Express, next: any) => void)[] = [
  (app: Express, next) => {
    const service = new GetUser();
    getRoute<GetUser>(
      app,
      service,
      requireAuth(),
      requireRole(
        service.getRequiredAuthRole(),
        (msg, code) => new GetUserResponse(new GetUserFailure(msg), code),
      ),
      async (req: AuthedRequest, res) => {
        console.info("Received: ", req)
        const response = await service.getUser(req.user!!.id);
        console.info("Responding: ", response)
        res.status(response.code).type("json").send(response.toJson());
      },
    );
  },
  (app: Express, next: any) => {
    const service = new RegisterUser();
    getRoute<RegisterUser>(app, service, async (req, res) => {
      const id = newId();
      const response = await service.registerUser(
        new RegisterUserRequest(
          new UserDTO(
            id,
            "" + crypto.randomUUID(),
            "" + crypto.randomUUID(),
            AuthRole.CUSTOMER.name,
          ),
          "password hash",
          "salt value",
        ),
      );
      console.log("newid: ", id)
      injectCookie(
        res,
        { id: id, role: AuthRole.CUSTOMER },  
      )?.status(response.code).type("json").send(response.toJson())
        ?? res.status(500).type('json').send(
          new RegisterUserResponse(new RegisterUserFailure(
            "Couldnt create token"
          ), 500)
        )
    });
  },
];
