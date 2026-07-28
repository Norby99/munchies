import axios, {AxiosInstance, AxiosResponse} from "axios";
import {ErrorResponse, HttpMethod} from "munchies-commons/kotlin/commons-modules";

const axiosClient = axios.create({
    transformRequest: [(data) => data],
    transformResponse: [(data) => data],
    headers: {
        "Content-Type": "application/json",
    },
    validateStatus: () => true,
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

export async function simpleRequest<Response>(
    uri: string,
    method: HttpMethod,
    body: string,
    parseResponse: (json: string) => Response,
    parseError: (json: string) => ErrorResponse,
): Promise<Response | ErrorResponse> {
    return axiosMethodChooser(axiosClient, uri, method, body)
        .then((response) => {
            if (response.status >= 400) {
                return parseError(response.data);
            }
            return parseResponse(response.data);
        })
        .catch((error) => {
            if (error instanceof ErrorResponse) return error;
            return new ErrorResponse(String(error?.message ?? error));
        })
}
