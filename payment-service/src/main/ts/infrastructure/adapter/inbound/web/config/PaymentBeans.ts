import { ProcessPayment } from "@main/application/port/inbound/ProcessPayment";
import { ProcessPaymentUseCase } from "@main/application/usecase/ProcessPaymentUseCase";
import { PaymentRepository } from "@main/domain/port/payment-repository";
import { PaymentGateway } from "@main/domain/port/payment-gateway";
import { InMemoryPaymentRepository } from "@main/infrastructure/adapter/outbound/memory/InMemoryPaymentRepository";
import { PaymentMongoRepository } from "@main/infrastructure/adapter/outbound/mongo/repository/payment-mongo-repository";
import { FakePaymentGateway } from "@main/infrastructure/adapter/outbound/payment/FakePaymentGateway";

export interface PaymentServices {
  processPayment: ProcessPayment;
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
    const processPayment = new ProcessPaymentUseCase(
      paymentRepository,
      paymentGateway
    );

    return {
      paymentRepository,
      paymentGateway,
      paymentServices: {
        processPayment,
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
    const processPayment = new ProcessPaymentUseCase(
      paymentRepository,
      paymentGateway
    );

    return {
      paymentRepository,
      paymentGateway,
      paymentServices: {
        processPayment,
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
