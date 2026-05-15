#!/bin/bash

SPEC_NAMES=()

for file in $(cd ./build/typescript/ && find . -maxdepth 1 -mindepth 1 -type d -printf '%f\n'); do
  echo "$file"
  SPEC_NAMES+=("$file")
done

echo "SPEC_NAMES: ${SPEC_NAMES[*]}"

printf '<!DOCTYPE html>
        <html lang="en">
        <head>
            <title>Munchies Typescript Docs</title><meta name="viewport" content="width=device-width,initial-scale=1">
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        </head>
        <body class="bg-light">
        <main class="container py-5">
            <h1 class="h4 mb-3">Munchies Typescript documentation</h1>
            <ul class="list-group shadow-sm">' > ./docs/pages/typescript/index.html

for spec in "${SPEC_NAMES[@]}"; do
  printf '<li class="list-group-item"><a class="text-decoration-none fw-medium" href="%s/index.html">%s</a></li>' "$spec" "$spec" >> ./docs/pages/typescript/index.html
done

printf '</ul>
        </main>
        </body>
        </html>' >> ./docs/pages/typescript/index.html