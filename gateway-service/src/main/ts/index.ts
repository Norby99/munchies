import "@main/infrastructure/adapter/inbound/web/services/user";
import express from "express";
import expressListEndpoints from "express-list-endpoints";
import { routes } from "./infrastructure/adapter/middleware/route";
async function main(): Promise<void> {
  const app = express();
  app.use(express.json());
 
  routes.forEach(route => {
    route(app, () => {})
  })
  
  
  const PORT = process.env.PORT ?? 8080;
  app.listen(PORT, () => {
    console.log("Gateway Server online");
    
    expressListEndpoints(app).forEach(({ methods, path }) => {
      console.log(`${methods.join(',').padEnd(10)} ${path}`);
    })
  });
  
}

main();
