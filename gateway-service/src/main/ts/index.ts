import "@main/infrastructure/adapter/inbound/web/services/user";
import { GetUser } from "@main/infrastructure/adapter/inbound/web/services/user";
import express from "express";
import { GetUserResponse } from "munchies-user-service-shared/kotlin/user-modules";
async function main(): Promise<void> {
  const app = express();
  app.use(express.json());
  app.get("/users/:id", async (req, res) => {
    const { id } = req.params;

    const result = await new GetUser().getUser(id);

    console.log("result is: " + result);

    const response = new GetUserResponse(result);
    
    res.status(200).type("json").send(response.toJson());  
  });
  const PORT = process.env.PORT ?? 8080;
  app.listen(PORT, () => {
    console.log("Gateway Server online");
  });
}

main();
