import "@main/infrastructure/adapter/inbound/web/services/user";
import {
  GetUser,
  RegisterUser,
  LoginUser,
  UpdateUserInfo,
  UpdateUserPassword,
  DeleteUser,
} from "@main/infrastructure/adapter/inbound/web/services/user";
import express from "express";
async function main(): Promise<void> {
  const app = express();
  app.use(express.json());
  app.get("/users/:id", async (req, res) => {
    const { id } = req.params;

    const response = await new GetUser().getUser(id);

    console.log("result is: " + response);
    
    res.status(200).type("json").send(response.toJson());  
  });
  const PORT = process.env.PORT ?? 8080;
  app.listen(PORT, () => {
    console.log("Gateway Server online");
  });
}

main();
