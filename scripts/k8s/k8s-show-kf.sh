#!/usr/bin/env bash

TOPIC=$1

if [ -z "$TOPIC" ]; then
    echo "Error: You must specify a topic name."
    echo "Usage: ./gradlew showKf -Ptopic=<name>"
    exit 1
fi

echo "Connecting to Kafka to tail topic: $TOPIC..."
echo "Press Ctrl+C to stop."

# Using a temporary pod to consume messages
minikube kubectl -- run kafka-client --restart='Never' -n kafka --image docker.io/bitnami/kafka:3.8.0-debian-12-r14 --command -- sleep infinity
minikube kubectl -- wait --for=condition=ready pod/kafka-client -n kafka
minikube kubectl -- exec -n kafka -it kafka-client -- \
  kafka-topics.sh \
    --bootstrap-server kafka:9092 \
    --list
