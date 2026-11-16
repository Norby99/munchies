#!/bin/sh
# Runs the shadcn-vue CLI inside a throwaway Node container, so nothing is installed on the host.
#
#   scripts/shadcn-vue.sh add button card dialog
#   scripts/shadcn-vue.sh add button --overwrite
#
# The container works on a private copy of frontend-service (without node_modules), runs
# `npm ci` and then `npx shadcn-vue@latest <args>`, and copies back only what the CLI created or
# changed: files under src/, package.json, package-lock.json and components.json. The host's
# node_modules is never touched; the Docker build installs the new dependencies with `npm ci`.
set -eu

cd "$(dirname "$0")/.."
NODE_IMAGE="node:24.0.0-alpine" # keep in sync with the Dockerfile build stage

docker run --rm -i \
  --user "$(id -u):$(id -g)" \
  -e HOME=/tmp -e npm_config_cache=/tmp/.npm -e npm_config_update_notifier=false \
  -v "$PWD":/project \
  "$NODE_IMAGE" sh -eu -c '
    mkdir -p /tmp/work
    cd /project
    tar --exclude=./node_modules --exclude=./dist --exclude=./build --exclude=./.gradle -cf - . | tar -xf - -C /tmp/work
    cd /tmp/work
    npm ci --no-audit --no-fund --loglevel=error
    touch /tmp/marker
    sleep 1
    npx -y shadcn-vue@latest "$@"
    for f in package.json package-lock.json components.json $(find src -type f -newer /tmp/marker); do
      if [ -f "$f" ] && ! cmp -s "$f" "/project/$f"; then
        mkdir -p "/project/$(dirname "$f")"
        cp "$f" "/project/$f"
        echo "updated: $f"
      fi
    done
  ' shadcn-vue "$@"
