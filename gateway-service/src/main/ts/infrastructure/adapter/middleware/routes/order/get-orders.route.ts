import {
    Request,
    RequestHandler,
    Response,
} from "express";
import {
    HttpMethod,
    AuthRole,
    ErrorResponse,
} from "munchies-commons/kotlin/commons-modules";
import {
    GetOrdersAPI,
    GetOrdersResponse,
} from "munchies-order-service-shared/kotlin/order-modules";
import {AuthedRequest} from "../../auth";
import {request} from "../internal-client";
import {SimpleRoute} from "../simple-route";
import {com} from "munchies-order-service-shared";
import OrderServiceConfig = com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig;

export class GetOrdersRoute
    extends GetOrdersAPI<GetOrdersResponse | ErrorResponse>
    implements SimpleRoute<GetOrdersResponse> {
    constructor() {
        super();
        let api: GetOrdersAPI = this;
        this.path = api.getPath();
        this.method = api.getMethod();
        this.authRole = api.getRequiredAuthRole();
    }

    path: string;
    method: HttpMethod;
    authRole: AuthRole | null;

    async getOrders(
        restaurantId: string | null,
        customerId: string | null,
        orderStatus: string | null,
    ): Promise<GetOrdersResponse | ErrorResponse> {
        const uri = process.env.ORDER_SERVICE_URL;
        if (!uri)
            return Promise.resolve(
                new ErrorResponse("Missing Order Service URL", 500),
            );

        const params = new URLSearchParams();
        if (restaurantId) params.append("restaurantId", restaurantId);
        if (customerId) params.append("customerId", customerId);
        if (orderStatus) params.append("status", orderStatus);

        const queryString = params.toString();
        const targetPath =
            uri + this.path + OrderServiceConfig.GET_ORDERS_PATH +
            (queryString ? `?${queryString}` : "");

        const response = request<GetOrdersResponse>(
            targetPath,
            this.method,
            "",
            this.parseResponse,
            this.parseError,
        );
        return response;
    }

    private handler: {
        forward: (
            req: AuthedRequest,
        ) => Promise<GetOrdersResponse | ErrorResponse>;
        respond: RequestHandler;
    } = {
        forward: async (req: AuthedRequest) => {
            try {
                const restaurantId = req.query.restaurantId
                    ? String(req.query.restaurantId)
                    : null;
                const customerId = req.query.customerId
                    ? String(req.query.customerId)
                    : null;
                const orderStatus = req.query.status
                    ? String(req.query.status)
                    : null;

                return this.getOrders(restaurantId, customerId, orderStatus);
            } catch (err: any) {
                return new ErrorResponse(
                    "GetOrders forward: \n" + String(err),
                    500,
                );
            }
        },
        respond: async (req: Request, res: Response) => {
            const result = await this.forward(req as AuthedRequest);
            res.status(result.code).type("json").send(result.toJson());
        },
    };

    forward = this.handler.forward;
    respond = this.handler.respond;
}