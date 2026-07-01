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
import expressListEndpoints from "express-list-endpoints";
import { GetUserSuccess, LoginUserRequest, RegisterUserRequest, UserDTO } from "munchies-user-service-shared/kotlin/user-modules";
import { routes } from "./infrastructure/adapter/middleware/route";
import { newId } from "munchies-commons/kotlin/commons-modules";
async function main(): Promise<void> {
  const app = express();
  app.use(express.json());
 
  routes.forEach(route => {
    route(app, () => {})
  })
    
  app.get("/", async (req, res) => {
    const response = await new RegisterUser().registerUser(
      new RegisterUserRequest(
        new UserDTO(
          newId(),
          "username" + crypto.randomUUID(),
          "email" + crypto.randomUUID(),
          "CUSTOMER"
        ),
        "hashed password",
        "salt"
      )
    )
    
    res.status(200).type("json").send(response.toJson());
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
