#!/usr/bin/env bash

set -e
trap 'if [ $? -ne 0 ]; then echo "Deployment failed!"; fi' EXIT

SERVICE=$1

if [ -z "$SERVICE" ]; then
    echo "Error: You must specify the service name or 'all'."
    echo "Usage: ./scripts/k8s-deploy.sh <service-name|all>"
    exit 1
fi

echo "Connecting to Minikube's Docker environment..."
eval $(minikube docker-env)

if [ "$SERVICE" == "all" ]; then
    SERVICES=""
    for item in k8s/*; do
        if [ -d "$item" ]; then
            SERVICES="$SERVICES $(basename "$item")"
        elif [[ "$item" == *.yml ]]; then
            SERVICES="$SERVICES $(basename "$item" .yml)"
        fi
    done
else
    SERVICES=$SERVICE
fi

for srv in $SERVICES; do
    echo "==========================================="
    echo "Processing: $srv"
    echo "==========================================="

    if [ -f "${srv}/service/build.gradle.kts" ]; then
        echo "Building Docker image for $srv (using :${srv}:service)..."
        ./gradlew :${srv}:service:dockerBuild
        # Micronaut calls the image 'service:latest' by default for a module named 'service'
        # We retag it with the root project name so deployment.yml can find it
        docker tag service:latest ${srv}:latest || true
    else
        echo "Building Docker image for $srv using Gradle..."
        ./gradlew :${srv}:dockerBuild
    fi

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