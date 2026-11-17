import mongoose from "mongoose";
import { logger } from "@main/infrastructure/adapter/outbound/logging/logger";

export async function connectDB(): Promise<void> {
  const uri = process.env.MONGODB_URI;
  if (!uri) throw new Error("MONGODB_URI is not defined in .env");

  mongoose.connection.on("connected", () => logger.info("MongoDB connected"));
  mongoose.connection.on("error", (err) =>
    logger.error({ err }, "MongoDB error")
  );
  mongoose.connection.on("disconnected", () =>
    logger.info("MongoDB disconnected")
  );

  await mongoose.connect(uri);
}

export async function disconnectDB(): Promise<void> {
  await mongoose.disconnect();
}
