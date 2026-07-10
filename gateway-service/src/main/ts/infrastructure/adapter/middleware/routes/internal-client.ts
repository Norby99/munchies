import axios, { AxiosInstance, AxiosResponse, AxiosStatic } from "axios";

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

import { HttpMethod } from "munchies-commons/kotlin/commons-modules";
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

async function request<
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

import { API } from "munchies-commons/kotlin/commons-modules";
import { InternalRoute } from "./route-definition";
export function internalAxiosRequest<
  Service extends API,
  Request extends { toJson(): string },
  Response extends { toJson(): string; result: Result; code: number },
  Result extends { type: string },
  Success extends Result,
  Failure extends Result & { reason: string },
  >(
  uri: string,
  route: InternalRoute<Service, Request, Response, Result, Success, Failure>,
  input: Request,
): Promise<Response> {
  return request(
    uri,
    route.service.getMethod(),
    input.toJson,
    route.parseResponse,
    route.parseResult,
    route.generateResponse,
    route.generateFailure
  )
}