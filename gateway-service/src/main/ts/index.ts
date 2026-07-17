import express from "express";
import expressListEndpoints from "express-list-endpoints";
import cookieParser from "cookie-parser";
import { applyRoutes } from "./infrastructure/adapter/middleware/routes/routes";
async function main(): Promise<void> {
  const app = express();
  app.use(express.raw({ type: "application/json" }));
  app.use(cookieParser());

  applyRoutes(app);

  const PORT = process.env.PORT ?? 8080;
  app.listen(PORT, () => {
    console.log("Gateway Server online");

    expressListEndpoints(app).forEach(({ methods, path }) => {
      console.log(`${methods.join(",").padEnd(10)} ${path}`);
    });
  });
}

main();
