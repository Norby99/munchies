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
minikube kubectl -- run kafka-tail-$$ --rm -i --image=quay.io/strimzi/kafka:0.51.0-kafka-4.2.0 --restart=Never --namespace kafka -- \
    bin/kafka-console-consumer.sh \
    --bootstrap-server munchies-kafka-kafka-bootstrap:9092 \
    --topic "$TOPIC" \
    --from-beginning

