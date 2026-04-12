#!/usr/bin/bash

SPEC_NAMES=()

for file in $(cd ./build/openapi/ && find .  -name "*.yml"); do
  base_name=${file%.yml}
  npx @redocly/cli build-docs "./build/openapi/$file" -o "./docs/pages/openapi/$base_name/index.html"
  spec_name=${base_name#./}
  SPEC_NAMES+=("$spec_name")
done

printf '<!DOCTYPE html>
        <html lang="en">
        <head>
            <title>Munchies OpenAPI</title><meta name="viewport" content="width=device-width,initial-scale=1">
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        </head>
        <body class="bg-light">
        <main class="container py-5">
            <h1 class="h4 mb-3">Munchies OpenAPI</h1>
            <ul class="list-group shadow-sm">' > ./docs/pages/openapi/index.html

for spec in "${SPEC_NAMES[@]}"; do
  #printf '<li><a href="%s/index.html">%s-service</a></li>' "$spec" "$spec" >> ./docs/pages/openapi/index.html

  printf '<li class="list-group-item"><a class="text-decoration-none fw-medium" href="%s/index.html">%s-service</a></li>' "$spec" "$spec" >> ./docs/pages/openapi/index.html

done

printf '</ul>
        </main>
        </body>
        </html>' >> ./docs/pages/openapi/index.html