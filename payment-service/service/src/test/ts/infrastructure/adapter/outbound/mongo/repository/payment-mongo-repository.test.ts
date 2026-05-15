import { beforeEach, describe, expect, it, vi } from "vitest";
import {
  Currency,
  PaymentStatus,
  newUUIDEntityId,
} from "@main/domain/external-modules";
import { Payment } from "@main/domain/model/Payment";
import { PaymentId } from "@main/domain/model/PaymentId";

vi.mock(
  "@main/infrastructure/adapter/outbound/mongo/document/payment-document",
  () => ({
    PaymentDocument: {},
    PaymentModel: {
      findById: vi.fn(),
      findByIdAndUpdate: vi.fn(),
      findByIdAndDelete: vi.fn(),
      find: vi.fn(),
    },
  })
);

vi.mock(
  "@main/infrastructure/adapter/outbound/mongo/factory/payment-factory",
  () => ({
    PaymentFactory: {
      toDomain: vi.fn(),
      toDocument: vi.fn(),
    },
  })
);

import { PaymentModel } from "@main/infrastructure/adapter/outbound/mongo/document/payment-document";
import { PaymentFactory } from "@main/infrastructure/adapter/outbound/mongo/factory/payment-factory";
import { PaymentMongoRepository } from "@main/infrastructure/adapter/outbound/mongo/repository/payment-mongo-repository";

const mockedPaymentModel = vi.mocked(PaymentModel);
const mockedPaymentFactory = vi.mocked(PaymentFactory);

describe("PaymentMongoRepository", () => {
  let repository: PaymentMongoRepository;

  beforeEach(() => {
    vi.resetAllMocks();
    repository = new PaymentMongoRepository();
  });

  const createPayment = (id: string, amount = 10) =>
    new Payment(
      new PaymentId(id),
      PaymentStatus.PENDING,
      amount,
      newUUIDEntityId(`${id}-order`),
      Currency.AUD,
      null
    );

  const createDocument = (id: string, amount = 10) => ({
    _id: id,
    status: PaymentStatus.PENDING,
    amount,
    orderId: `${id}-order`,
    currency: Currency.AUD,
    payedAt: null,
  });

  it("returns the mapped payment when the document exists", async () => {
    const document = createDocument("payment-1", 25);
    const payment = createPayment("payment-1", 25);

    mockedPaymentModel.findById.mockResolvedValue(document as never);
    mockedPaymentFactory.toDomain.mockReturnValue(payment);

    await expect(
      repository.findById(new PaymentId("payment-1"))
    ).resolves.toEqual(payment);
    expect(mockedPaymentModel.findById).toHaveBeenCalledWith("payment-1");
    expect(mockedPaymentFactory.toDomain).toHaveBeenCalledWith(document);
  });

  it("returns null when the payment document does not exist", async () => {
    mockedPaymentModel.findById.mockResolvedValue(null);

    await expect(
      repository.findById(new PaymentId("missing-payment"))
    ).resolves.toBeNull();
    expect(mockedPaymentFactory.toDomain).not.toHaveBeenCalled();
  });

  it("saves the mapped payment with upsert enabled", async () => {
    const payment = createPayment("payment-2", 42);
    const document = createDocument("payment-2", 42);

    mockedPaymentFactory.toDocument.mockReturnValue(document);
    mockedPaymentModel.findByIdAndUpdate.mockResolvedValue(document as never);

    await expect(repository.save(payment)).resolves.toBeUndefined();
    expect(mockedPaymentFactory.toDocument).toHaveBeenCalledWith(payment);
    expect(mockedPaymentModel.findByIdAndUpdate).toHaveBeenCalledWith(
      document._id,
      document,
      { upsert: true, new: true }
    );
  });

  it("updates the payment and returns the mapped updated payment", async () => {
    const payment = createPayment("payment-3", 55);
    const document = createDocument("payment-3", 55);
    const updatedPayment = createPayment("payment-3", 55);

    mockedPaymentFactory.toDocument.mockReturnValue(document);
    mockedPaymentModel.findByIdAndUpdate.mockResolvedValue(document as never);
    mockedPaymentFactory.toDomain.mockReturnValue(updatedPayment);

    await expect(repository.update(payment)).resolves.toEqual(updatedPayment);
    expect(mockedPaymentModel.findByIdAndUpdate).toHaveBeenCalledWith(
      document._id,
      { $set: document },
      { new: true, runValidators: true }
    );
    expect(mockedPaymentFactory.toDomain).toHaveBeenCalledWith(document);
  });

  it("deletes the payment and returns the mapped removed payment", async () => {
    const payment = createPayment("payment-4", 15);
    const document = createDocument("payment-4", 15);
    const removedPayment = createPayment("payment-4", 15);

    mockedPaymentModel.findByIdAndDelete.mockResolvedValue(document as never);
    mockedPaymentFactory.toDomain.mockReturnValue(removedPayment);

    await expect(repository.delete(payment)).resolves.toEqual(removedPayment);
    expect(mockedPaymentModel.findByIdAndDelete).toHaveBeenCalledWith(
      "payment-4"
    );
    expect(mockedPaymentFactory.toDomain).toHaveBeenCalledWith(document);
  });

  it("throws when deleting a missing payment", async () => {
    mockedPaymentModel.findByIdAndDelete.mockResolvedValue(null);

    await expect(
      repository.delete(createPayment("missing-payment"))
    ).rejects.toThrow("Payment missing-payment not found");
    expect(mockedPaymentFactory.toDomain).not.toHaveBeenCalled();
  });

  it("returns the first matching payment from the mapped documents", async () => {
    const firstDocument = createDocument("payment-5", 15);
    const secondDocument = createDocument("payment-6", 30);
    const firstPayment = createPayment("payment-5", 15);
    const secondPayment = createPayment("payment-6", 30);

    mockedPaymentModel.find.mockResolvedValue([
      firstDocument,
      secondDocument,
    ] as never);
    mockedPaymentFactory.toDomain
      .mockImplementationOnce(() => firstPayment)
      .mockImplementationOnce(() => secondPayment);

    await expect(
      repository.findByPredicate((payment) => Number(payment.amount) >= 20)
    ).resolves.toEqual(secondPayment);
  });

  it("returns null when no mapped payments satisfy the predicate", async () => {
    const document = createDocument("payment-7", 12);
    const payment = createPayment("payment-7", 12);

    mockedPaymentModel.find.mockResolvedValue([document] as never);
    mockedPaymentFactory.toDomain.mockReturnValue(payment);

    await expect(
      repository.findByPredicate((payment) => Number(payment.amount) > 100)
    ).resolves.toBeNull();
  });

  it("returns null when no payment documents are found", async () => {
    mockedPaymentModel.find.mockResolvedValue([]);

    await expect(repository.findByPredicate(() => true)).resolves.toBeNull();
  });
});
