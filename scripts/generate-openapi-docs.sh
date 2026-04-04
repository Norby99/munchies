#!/usr/bin/bash

SPEC_NAMES = ()

for file in $(cd ./build/openapi/ && find .  -name "*.yml"); do
  base_name=${file%.yml}
  npx @redocly/cli build-docs "./build/openapi/$file" -o "./docs/pages/openapi/$base_name/index.html"
  SPEC_NAMES+=("$base_name")
done

for spec in "${SPECS_DIR[@]}}"; do
  echo "- [$spec-service](./docs/pages/openapi/$spec/index.html)" >> ./docs/pages/openapi/index.md
done