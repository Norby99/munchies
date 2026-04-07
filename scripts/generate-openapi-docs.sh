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
            <title>Munchies OpenAPI</title>
        </head>
        <body>
        <ul>' > ./docs/pages/openapi/index.html

for spec in "${SPEC_NAMES[@]}"; do
  echo "<li><a href=\"$spec/index.html\">$spec-service</a></li> " >> ./docs/pages/openapi/index.html
done

printf '</ul>
        </body>
        </html>' >> ./docs/pages/openapi/index.html