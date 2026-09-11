import { ProcessPayment } from "@main/application/port/inbound/ProcessPayment";
import { ProcessPaymentUseCase } from "@main/application/usecase/ProcessPaymentUseCase";
import { PaymentRepository } from "@main/domain/port/payment-repository";
import { PaymentGateway } from "@main/domain/port/payment-gateway";
import { OrderServiceClient } from "@main/domain/port/order-service-client";
import { InMemoryPaymentRepository } from "@main/infrastructure/adapter/outbound/memory/InMemoryPaymentRepository";
import { PaymentMongoRepository } from "@main/infrastructure/adapter/outbound/mongo/repository/payment-mongo-repository";
import { FakePaymentGateway } from "@main/infrastructure/adapter/outbound/payment/FakePaymentGateway";
import { OrderServiceHttpClient } from "@main/infrastructure/adapter/outbound/order/OrderServiceHttpClient";

export interface PaymentServices {
  processPayment: ProcessPayment;
  orderServiceClient: OrderServiceClient;
}

export class PaymentBeans {
  private static instance: {
    paymentRepository: PaymentRepository;
    paymentGateway: PaymentGateway;
    paymentServices: PaymentServices;
  } | null = null;

  static createInMemoryBeans(): {
    paymentRepository: PaymentRepository;
    paymentGateway: PaymentGateway;
    paymentServices: PaymentServices;
  } {
    const paymentRepository = new InMemoryPaymentRepository();
    const paymentGateway = new FakePaymentGateway();
    const orderServiceClient = new OrderServiceHttpClient();
    const processPayment = new ProcessPaymentUseCase(
      paymentRepository,
      paymentGateway
    );

    return {
      paymentRepository,
      paymentGateway,
      paymentServices: {
        processPayment,
        orderServiceClient,
      },
    };
  }

  static createMongoBeans(): {
    paymentRepository: PaymentRepository;
    paymentGateway: PaymentGateway;
    paymentServices: PaymentServices;
  } {
    const paymentRepository = new PaymentMongoRepository();
    const paymentGateway = new FakePaymentGateway();
    const orderServiceClient = new OrderServiceHttpClient();
    const processPayment = new ProcessPaymentUseCase(
      paymentRepository,
      paymentGateway
    );

    return {
      paymentRepository,
      paymentGateway,
      paymentServices: {
        processPayment,
        orderServiceClient,
      },
    };
  }

  static getDefaultServices(): PaymentServices {
    if (!this.instance) {
      this.instance = this.createMongoBeans();
    }
    return this.instance.paymentServices;
  }

  static resetInstance(): void {
    this.instance = null;
  }
}
