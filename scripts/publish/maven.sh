#!/usr/bin/bash
VERSION=$1
if [ -z "$VERSION" ]; then
  echo "Usage: $0 <version>"
  exit 1
fi

./gradlew clean publishToMavenCentral \
  -PversionName="$VERSION" \
  --no-configuration-cache \
  --no-daemon
