#!/usr/bin/bash

VERSION=$1

if [ -z "$VERSION" ]; then
  echo "Usage: $0 <version>"
  exit 1
fi

for file in $(cd ./build/js/packages/ && find . -maxdepth 1 -mindepth 1 -type d | grep -v '\-test$'); do
  if ! (
    cd ./build/js/packages/"$file" &&
    npm version "$VERSION" &&
    npm pkg fix &&
    npm publish --access public
  ); then
    echo "Failed to publish package: $file" >&2
    exit 1
  fi
done
