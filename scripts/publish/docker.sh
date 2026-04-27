#!/usr/bin/bash

set -euo pipefail

if [ "$#" -lt 1 ]; then
  echo "Usage: $0 <version>"
  exit 1
fi

VERSION=$1

echo "$DOCKER_PASSWORD" | docker login --username "$DOCKERHUB_USERNAME" --password-stdin

services=(
user-service
restaurant-service
payment-service
gateway-service
order-service
table-service
notification-service
)

for service in "${services[@]}"; do
    local_image="${service}:latest"
    remote_image="${DOCKERHUB_USERNAME}/${service}:${VERSION}"

    if (docker tag "$local_image" "$remote_image") ; then
      echo "$remote_image"
      docker push "$remote_image"
    else
      echo "Failed to tag image: $local_image as $remote_image" >&2
    fi
done

docker logout