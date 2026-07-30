import crypto from "node:crypto";

const b64url = (obj) => Buffer.from(JSON.stringify(obj)).toString("base64url");

const secret = "blastimus_prime";
const now = Math.floor(Date.now() / 1000);
const header = b64url({ alg: "HS256", typ: "JWT" });
const payload = b64url({ sub: "test-manager-id", id: "test-manager-id", role: "MANAGER", exp: now + 3600, iat: now });
const signature = crypto.createHmac("sha256", secret).update(`${header}.${payload}`).digest("base64url");
const token = `${header}.${payload}.${signature}`;

const body = JSON.stringify({ name: "Test Restaurant", description: "A test", address: "123 Street" });
const res = await fetch("http://localhost:8080/restaurants/", {
  method: "POST",
  headers: { "Content-Type": "application/json", Cookie: `authToken=${token}` },
  body,
});
console.log(res.status, await res.text());
