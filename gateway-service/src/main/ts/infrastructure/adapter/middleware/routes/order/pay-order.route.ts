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
    PayOrderAPI,
    PayOrderResponse,
} from "munchies-order-service-shared/kotlin/order-modules";
import {AuthedRequest} from "../../auth";
import {request} from "../internal-client";
import {SimpleRoute} from "../simple-route";
import {fillPath} from "@main/infrastructure/adapter/middleware/routes/routes";

export class PayOrderRoute
    extends PayOrderAPI<PayOrderResponse | ErrorResponse>
    implements SimpleRoute<PayOrderResponse> {
    constructor() {
        super();
        let api: PayOrderAPI = this;

        this.path = api.getPath();
        this.method = api.getMethod();
        this.authRole = api.getRequiredAuthRole();
    }

    path: string;
    method: HttpMethod;
    authRole: AuthRole | null;

    async payOrder(
        id: string,
    ): Promise<PayOrderResponse | ErrorResponse> {
        const uri = process.env.ORDER_SERVICE_URL;
        if (!uri)
            return Promise.resolve(
                new ErrorResponse("Missing Order Service URL", 500),
            );

        const response = request<PayOrderResponse>(
            // this.path is already the full "/orders/{id}/pay" template: only the id is filled in.
            fillPath(uri + this.path, id),
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
        ) => Promise<PayOrderResponse | ErrorResponse>;
        respond: RequestHandler;
    } = {
        forward: async (req: AuthedRequest) => {
            try {
                // The order id comes from the path. The request has no body: JsPayOrderAPI's
                // parseRequest is intentionally unsupported and always throws.
                return this.payOrder(req.params.id);
            } catch (err: any) {
                return new ErrorResponse(
                    "PayOrder forward: \n" + String(err),
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
