#!/usr/bin/env bash

# ==========================================
# Munchies Main CLI Tool
# ==========================================

COMMAND=$1
shift

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
        ./scripts/k8s-deploy.sh "$@"
        ;;
    undeploy)
        ./scripts/k8s-undeploy.sh "$@"
        ;;
    show-db)
        ./scripts/k8s-show-db.sh "$@"
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

