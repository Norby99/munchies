#!/usr/bin/bash

docker compose -f config/docker/docker-compose.yml up -d
./gradlew clean build check run
