import axios, { AxiosInstance, AxiosResponse, AxiosStatic } from "axios";
import { logger } from "../logger";

export const axiosClient = axios.create({
  transformRequest: [(data) => data],
  transformResponse: [(data) => data],
  headers: {
    "Content-Type": "application/json",
  },
  validateStatus: (status: number) => status <= 500,
  timeout: 10_000,
  
});

// Payloads are not logged: they may carry credentials or personal data.
axiosClient.interceptors.request.use((config) => {
  logger.debug({ method: config.method, url: config.url }, "Upstream request");
  return config;
});

axiosClient.interceptors.response.use((response) => {
  logger.debug({ status: response.status, url: response.config?.url }, "Upstream response");
  return response;
});

import {
  ErrorResponse,
  HttpMethod,
  JsonEncodable,
  WebResponse,
} from "munchies-commons/kotlin/commons-modules";
function axiosMethodChooser(
  base: AxiosInstance,
  uri: string,
  method: HttpMethod,
  body: string = ""
): Promise<AxiosResponse> {
  switch (method.name) {
    case HttpMethod.POST.name:
      return base.post(uri, body);
    case HttpMethod.PUT.name:
      return base.put(uri, body);
    case HttpMethod.DELETE.name:
      return base.delete(uri, { data: body });
    case HttpMethod.PATCH.name:
      return base.patch(uri, body);
    default:
      return base.get(uri);
  }
}

export async function request<
  Response
>(
  uri: string,
  httpMethod: HttpMethod,
  body: string,
  responseFromJson: (json: string) => Response,
  errorFromJson: (json: string) => ErrorResponse,
): Promise<Response | ErrorResponse> {
  return axiosMethodChooser(axiosClient, uri, httpMethod, body)
    .then((value) => {
      if (value.status >= 400) {
        return errorFromJson(value.data);
      }
      
      return responseFromJson(value.data);
    })
    .catch((err) => {
      logger.error({ err, method: httpMethod.name, uri }, "Upstream request failed");
      return new ErrorResponse("Internal Axios Request: \n" + String(err), 500);
      
    });
}

