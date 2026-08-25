import axios, { AxiosInstance, AxiosResponse, AxiosStatic } from "axios";

export const axiosClient = axios.create({
  transformRequest: [(data) => data],
  transformResponse: [(data) => data],
  headers: {
    "Content-Type": "application/json",
  },
  validateStatus: (status: number) => status <= 500,
  timeout: 10_000,
  
});

axiosClient.interceptors.request.use((config) => {
  console.info("OUT:", typeof config.data, config.data);
  return config;
});

axiosClient.interceptors.response.use((response) => {
  console.info("IN:", typeof response.data, response.data);
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
      return base.delete(uri);
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
      console.log("received data: ", value.data);
      
      if (value.status >= 400) {
        return errorFromJson(value.data);
      }
      
      return responseFromJson(value.data);
    })
    .catch((err) => {
      console.error("-----Error-----");
      console.error("when " + httpMethod.name + " to " + uri);
      console.error("with: " + body);
      console.error("result err: ", err);
      console.error("-----End Error-----");
      return new ErrorResponse("Internal Axios Request: \n" + String(err), 500);
      
    });
}

