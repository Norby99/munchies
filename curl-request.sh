# Salva il cookie in locale
curl -c testCookie.txt --data-binary @testBody.txt -H "Content-Type: application/json" -X POST "http://localhost:8086/users/register"

# Carica il cookie da locale
curl -b testCookie.txt --data-binary @testBody.txt -H "Content-Type: application/json" -X GET "http://localhost:8086/users/"