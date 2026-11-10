/** Application-level error with an HTTP status the inbound adapter maps 1:1. */
export class AppError extends Error {
  constructor(
    message: string,
    readonly status: number,
  ) {
    super(message);
    this.name = "AppError";
  }
}

export const badRequest = (m: string) => new AppError(m, 400);
export const notFound = (m: string) => new AppError(m, 404);
export const conflict = (m: string) => new AppError(m, 409);
export const upstream = (m: string) => new AppError(m, 502);
