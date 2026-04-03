#!/usr/bin/env bash

SERVICE=$1

if [ -z "$SERVICE" ]; then
    echo "Error: You must specify the service name or 'all'."
    echo "Usage: ./scripts/k8s-deploy.sh <service-name|all>"
    exit 1
fi

echo "Connecting to Minikube's Docker environment..."
eval $(minikube docker-env)

if [ "$SERVICE" == "all" ]; then
    SERVICES="order-service"
else
    SERVICES=$SERVICE
fi

for srv in $SERVICES; do
    echo "==========================================="
    echo "Processing: $srv"
    echo "==========================================="

    echo "Building Docker image for $srv using Gradle..."
    ./gradlew :${srv}:dockerBuild

    # Supports both a single file (k8s/[service].yml)
    # and a folder (k8s/[service]/)
    if [ -f "k8s/${srv}.yml" ]; then
        MANIFEST="k8s/${srv}.yml"
        echo "Applying Kubernetes manifest $MANIFEST..."
        minikube kubectl -- apply -f $MANIFEST
    elif [ -d "k8s/${srv}" ]; then
        echo "Applying namespace first, then full manifest in k8s/${srv}/..."
        minikube kubectl -- apply -f k8s/${srv}/namespace.yml
        minikube kubectl -- apply -f k8s/${srv}/
    else
        echo "Warning: No manifest found for $srv (checked k8s/${srv}.yml and k8s/${srv}/)."
        continue
    fi

    echo "Restarting deployments and statefulsets..."
    minikube kubectl -- rollout restart deployment -n $srv || true
    minikube kubectl -- rollout restart statefulset -n $srv || true

    echo ""
done

echo "Deployment Completed!"