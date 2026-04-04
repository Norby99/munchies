#!/usr/bin/bash

SPEC_NAMES=()

for file in $(cd ./build/openapi/ && find .  -name "*.yml"); do
  base_name=${file%.yml}
  npx @redocly/cli build-docs "./build/openapi/$file" -o "./docs/pages/openapi/$base_name/index.html"
  spec_name=${base_name#./}
  echo "$spec_name"
  SPEC_NAMES+=("$spec_name")
done

for spec in "${SPEC_NAMES[@]}"; do
  printf '\n' >> ./docs/pages/openapi/index.md
  echo "- [$spec-service](/docs/pages/openapi/$spec/index.html)" >> ./docs/pages/openapi/index.md
done