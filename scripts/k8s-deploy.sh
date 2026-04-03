#!/usr/bin/env bash

# Usage: ./scripts/k8s-deploy.sh <service-name | all>
# Example for a single service: ./scripts/k8s-deploy.sh order-service
# Example for all services: ./scripts/k8s-deploy.sh all

SERVICE=$1

if [ -z "$SERVICE" ]; then
    echo "Error: You must specify the service name or 'all'."
    echo "Usage: ./scripts/k8s-deploy.sh <service-name|all>"
    exit 1
fi

echo "🔄 Connecting to Minikube's Docker environment..."
eval $(minikube docker-env)

if [ "$SERVICE" == "all" ]; then
    # Base list of your services. You can add or remove them based on your needs
    SERVICES="order-service" # add the other ones here separated by spaces when you have them with Docker
else
    SERVICES=$SERVICE
fi

for srv in $SERVICES; do
    echo "==========================================="
    echo "🚀 Processing: $srv"
    echo "==========================================="

    echo "📦 1/3 Building Docker image for $srv using Gradle..."
    ./gradlew :${srv}:dockerBuild

    if [ -f "k8s/${srv}.yml" ]; then
        echo "☸️  2/3 Applying Kubernetes manifest in k8s/${srv}.yml..."
        minikube kubectl -- apply -f k8s/${srv}.yml

        echo "🔄 3/3 Restarting deployment to pick up the new image..."
        minikube kubectl -- rollout restart deployment $srv || true
    else
        echo "⚠️  Warning: The manifest file k8s/${srv}.yml does not exist. Make sure you created it."
    fi
    echo ""
done

echo "✅ Deployment Completed!"
