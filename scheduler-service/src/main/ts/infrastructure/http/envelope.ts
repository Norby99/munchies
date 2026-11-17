import { Response } from "express";
import { AppError } from "../../application/errors";

/** Platform-wide success envelope: { result, code }. */
export function ok<T>(res: Response, result: T, code = 200): void {
  res.status(code).type("json").json({ result, code });
}

/** Platform-wide error envelope: { result: <message>, code }. */
export function fail(res: Response, err: unknown): void {
  if (err instanceof AppError) {
    res.status(err.status).type("json").json({ result: err.message, code: err.status });
    return;
  }
  if (err instanceof RangeError) {
    res.status(400).type("json").json({ result: err.message, code: 400 });
    return;
  }
  console.error("[scheduler] unhandled error:", err);
  res.status(500).type("json").json({ result: "Internal scheduler error", code: 500 });
}
