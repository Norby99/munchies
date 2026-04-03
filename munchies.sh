#!/usr/bin/env bash

# ==========================================
# Munchies Main CLI Tool
# ==========================================

COMMAND=$1
ARG1=$2
ARG2=$3

function show_help {
    echo "Usage: ./munchies.sh <command> [args...]"
    echo ""
    echo "Available commands:"
    echo "  deploy <service-name|all>                  Deploy a service to Minikube"
    echo "  undeploy <service-name|all> [--wipe-data]  Undeploy a service from Minikube (pass --wipe-data to delete DB volumes)"
    echo "  show-db <service-name> [collection-name]   Show MongoDB data for a specific service"
    echo "  help                                       Show this help message"
    echo ""
    echo "Examples:"
    echo "  ./munchies.sh deploy order-service"
    echo "  ./munchies.sh undeploy order-service --wipe-data"
    echo "  ./munchies.sh show-db order-service order"
}

if [ -z "$COMMAND" ]; then
    show_help
    exit 1
fi

case "$COMMAND" in
    deploy)
        if [ -z "$ARG1" ]; then
            echo "Error: You must specify the service name or 'all'."
            exit 1
        fi
        ./scripts/k8s-deploy.sh "$ARG1"
        ;;
    undeploy)
        if [ -z "$ARG1" ]; then
            echo "Error: You must specify the service name or 'all'."
            exit 1
        fi
        ./scripts/k8s-undeploy.sh "$ARG1" "$ARG2"
        ;;
    show-db)
        if [ -z "$ARG1" ]; then
            echo "Error: You must specify the service name."
            exit 1
        fi
        ./scripts/k8s-show-db.sh "$ARG1" "$ARG2"
        ;;
    help)
        show_help
        ;;
    *)
        echo "Error: Unknown command '$COMMAND'."
        echo ""
        show_help
        exit 1
        ;;
esac

