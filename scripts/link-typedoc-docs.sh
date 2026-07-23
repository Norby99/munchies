#!/bin/bash

SPEC_NAMES=()

for file in $(cd ./build/typescript/ && find . -maxdepth 1 -mindepth 1 -type d -printf '%f\n'); do
  echo "$file"
  SPEC_NAMES+=("$file")
done

echo "SPEC_NAMES: ${SPEC_NAMES[*]}"

for spec in "${SPEC_NAMES[@]}"; do
  printf '<li class="list-group-item docs-card"><a class="text-decoration-none fw-medium" href="%s/index.html">%s</a></li>' "$spec" "$spec" >> ./docs/pages/typescript/index.html
done

printf '</ul>
        </main>
        </body>
        </html>' >> ./docs/pages/typescript/index.html