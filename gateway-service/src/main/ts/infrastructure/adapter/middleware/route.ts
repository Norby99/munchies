import axios, { AxiosResponse, AxiosStatic } from "axios";
import {
  HttpMethod
} from "munchies-commons/kotlin/commons-modules"

function axiosMethodChooser(
  base: AxiosStatic,
  uri: string, 
  method: HttpMethod,
  body: string = "",
): Promise<AxiosResponse> {
  axios.defaults.validateStatus = (status: number) => status <= 500;
  switch (method) {
    case HttpMethod.POST:
      return base.post(uri, body)
    case HttpMethod.DELETE:
      return base.delete(uri)
    case HttpMethod.PATCH:
      return base.patch(uri, body)
    default:
      return base.get(uri)
  }
}

export async function request<
  Response extends { result: Result },
  Result extends { type: string },
  Success extends Result,
  Failure extends Result>
  (
  uri: string,
  httpMethod: HttpMethod,
  toJson: () => string = () => "", 
  fromJson: (json: string) => Response,
  toResult: (result: Result) => Success | Failure,
  toResponse: (result: Success | Failure) => Response,
  toFailure: (err: string) => Failure
): Promise<Response> {
  
  return axiosMethodChooser(
    axios, uri, httpMethod, toJson()
  ).then(value => {
    const response = fromJson(JSON.stringify(value.data));
    return toResponse(toResult(response.result));
  })
  .catch(err => {
    return toResponse(toFailure(JSON.stringify(err)))
  }) 
}
