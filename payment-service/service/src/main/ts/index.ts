const c = require("munchies-commons");
const commons = c.com.munchies.commons as {
  getUUID: () => string;
  UUIDEntityId: new (value?: string) => unknown;
};

console.log("Starting payment service...");
console.log(commons);
console.log(commons.getUUID());

const exp1 = new commons.UUIDEntityId();
console.log(exp1);

const exp2 = new commons.UUIDEntityId("prova");
console.log(exp2);
