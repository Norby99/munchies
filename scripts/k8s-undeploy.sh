#!/usr/bin/env bash

# Usage: ./scripts/k8s-undeploy.sh <service-name | all>
# Example for a single service: ./scripts/k8s-undeploy.sh order-service
# Example for all services: ./scripts/k8s-undeploy.sh all

SERVICE=$1

if [ -z "$SERVICE" ]; then
    echo "Error: You must specify the service name or 'all'."
    echo "Usage: ./scripts/k8s-undeploy.sh <service-name|all>"
    exit 1
fi

if [ "$SERVICE" == "all" ]; then
    echo "🗑️  Removing ALL Kubernetes resources and databases..."
    minikube kubectl -- delete -f k8s/
else
    echo "🗑️  Removing resources for application $SERVICE..."
    if [ -f "k8s/${SERVICE}.yml" ]; then
        minikube kubectl -- delete -f k8s/${SERVICE}.yml
    else
        echo "⚠️  The manifest file k8s/${SERVICE}.yml does not exist."
    fi

    # Automatically removes the isolated database if it exists
    if [ -f "k8s/${SERVICE:0:10}mongodb.yml" ] || [ -f "k8s/order-mongodb.yml" ]; then
       # Let's simplify by checking if there's an associated db with the known pattern
       DB_FILE="k8s/$(echo $SERVICE | cut -d'-' -f1)-mongodb.yml"
       if [ -f "$DB_FILE" ]; then
           echo "🗑️  Removing associated database ($DB_FILE)..."
           minikube kubectl -- delete -f $DB_FILE
       fi
    fi
fi

echo "✅ Undeploy completed!"
