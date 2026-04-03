#!/usr/bin/env bash

# Usage: ./scripts/k8s-undeploy.sh <service-name | all> [--wipe-data]
# Example for a single service: ./scripts/k8s-undeploy.sh order-service
# Example for all services: ./scripts/k8s-undeploy.sh all
# Example wiping data: ./scripts/k8s-undeploy.sh order-service --wipe-data

SERVICE=$1
WIPE_DATA=${2:-false}

if [ -z "$SERVICE" ]; then
    echo "Error: You must specify the service name or 'all'."
    echo "Usage: ./scripts/k8s-undeploy.sh <service-name|all> [--wipe-data]"
    exit 1
fi

if [ "$SERVICE" == "all" ]; then
    SERVICES="order-service" # keep in sync with k8s-deploy.sh
else
    SERVICES=$SERVICE
fi

for srv in $SERVICES; do
    echo "==========================================="
    echo "Undeploying: $srv"
    echo "==========================================="

    if [ -f "k8s/${srv}.yml" ]; then
        echo "Deleting resources from k8s/${srv}.yml..."
        minikube kubectl -- delete -f k8s/${srv}.yml --ignore-not-found
    elif [ -d "k8s/${srv}" ]; then
        if [ "$WIPE_DATA" == "--wipe-data" ]; then
            echo "Deleting all resources from k8s/${srv}/..."
            minikube kubectl -- delete -f k8s/${srv}/ --ignore-not-found
        else
            echo "Deleting resources from k8s/${srv}/ (excluding PVC and Namespace)..."
            for file in k8s/${srv}/*.yml k8s/${srv}/*.yaml; do
                [ -e "$file" ] || continue
                if [[ "$file" == *"pvc"* ]] || [[ "$file" == *"namespace"* ]]; then
                    continue
                fi
                minikube kubectl -- delete -f "$file" --ignore-not-found
            done
        fi
    else
        echo "Warning: No manifest found for $srv. Attempting namespace deletion anyway..."
    fi

    if [ "$WIPE_DATA" == "--wipe-data" ]; then
        echo "Wiping PersistentVolumeClaims for $srv..."
        minikube kubectl -- delete pvc --all -n $srv --ignore-not-found
        echo "Deleting namespace $srv..."
        minikube kubectl -- delete namespace $srv --ignore-not-found
    else
        echo "Keeping PersistentVolumeClaims (pass '--wipe-data' to delete them)."
    fi

    echo ""
done

echo "Undeploy Completed!"