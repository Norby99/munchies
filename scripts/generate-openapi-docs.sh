#!/usr/bin/bash

SPEC_NAMES=()

for file in $(cd ./build/openapi/ && find .  -name "*.yml"); do
  base_name=${file%.yml}
  npx @redocly/cli build-docs "./build/openapi/$file" -o "./docs/pages/openapi/$base_name/index.html"
  spec_name=${base_name#./}
  SPEC_NAMES+=("$spec_name")
done

for spec in "${SPEC_NAMES[@]}"; do
  #printf '<li><a href="%s/index.html">%s-service</a></li>' "$spec" "$spec" >> ./docs/pages/openapi/index.html

  printf '<li class="list-group-item docs-card"><a class="text-decoration-none fw-medium" href="%s/index.html">%s-service</a></li>' "$spec" "$spec" >> ./docs/pages/openapi/index.html

done

printf '</ul>
        </main>
        </body>
        </html>' >> ./docs/pages/openapi/index.html