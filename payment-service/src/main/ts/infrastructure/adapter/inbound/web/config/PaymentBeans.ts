import { ProcessPayment } from "@main/application/port/inbound/ProcessPayment";
import { ProcessPaymentUseCase } from "@main/application/usecase/ProcessPaymentUseCase";
import { PaymentRepository } from "@main/domain/port/payment-repository";
import { PaymentGateway } from "@main/domain/port/payment-gateway";
import { OrderServiceClient } from "@main/domain/port/order-service-client";
import { PaymentNotificationPublisher } from "@main/domain/port/payment-notification-publisher";
import { InMemoryPaymentRepository } from "@main/infrastructure/adapter/outbound/memory/InMemoryPaymentRepository";
import { PaymentMongoRepository } from "@main/infrastructure/adapter/outbound/mongo/repository/payment-mongo-repository";
import { FakePaymentGateway } from "@main/infrastructure/adapter/outbound/payment/FakePaymentGateway";
import { OrderServiceHttpClient } from "@main/infrastructure/adapter/outbound/order/OrderServiceHttpClient";
import { KafkaPaymentNotificationPublisher } from "@main/infrastructure/adapter/outbound/kafka/KafkaPaymentNotificationPublisher";

export interface PaymentServices {
  processPayment: ProcessPayment;
  orderServiceClient: OrderServiceClient;
  paymentNotificationPublisher: PaymentNotificationPublisher;
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
    const paymentNotificationPublisher = new KafkaPaymentNotificationPublisher();
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
        paymentNotificationPublisher,
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
    const paymentNotificationPublisher = new KafkaPaymentNotificationPublisher();
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
        paymentNotificationPublisher,
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
