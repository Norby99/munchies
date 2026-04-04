#!/usr/bin/bash

for file in $(cd ./build/openapi/ && find .  -name "*.yml"); do
  echo "$file"
  base_name=${file%.yml}
  echo "$base_name"
  npx @redocly/cli build-docs "./build/openapi/$file" -o "./docs/pages/openapi/$base_name/index.html"
done