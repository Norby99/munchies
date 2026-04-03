#!/usr/bin/env bash

SERVICE=$1

if [ -z "$SERVICE" ]; then
    echo "Error: You must specify the service name."
    echo "Usage: ./scripts/k8s-show-db-data.sh <service-name>"
    exit 1
fi

echo "Finding MongoDB pod for namespace: $SERVICE..."
POD_NAME=$(minikube kubectl -- get pods -n $SERVICE | awk '/mongodb/ {print $1; exit}')

if [ -z "$POD_NAME" ]; then
    echo "Error: Could not find a MongoDB pod in namespace '$SERVICE'."
    exit 1
fi

echo "Found database pod: $POD_NAME"
echo "==========================================="
echo "Databases available on the server:"
echo "==========================================="
minikube kubectl -- exec -t $POD_NAME -n $SERVICE -- mongosh --quiet --eval "show dbs"

echo ""
echo "==========================================="
echo "Collections in database '$SERVICE':"
echo "==========================================="
minikube kubectl -- exec -t $POD_NAME -n $SERVICE -- mongosh $SERVICE --quiet --eval "show collections"

echo ""
echo "==========================================="
echo "Documents within each collection:"
echo "==========================================="
COLLECTIONS=$(minikube kubectl -- exec -t $POD_NAME -n $SERVICE -- mongosh $SERVICE --quiet --eval "db.getCollectionNames().join(' ')" | tr -d '\r')

for coll in $COLLECTIONS; do
    echo "--> Collection: $coll"
    minikube kubectl -- exec -t $POD_NAME -n $SERVICE -- mongosh $SERVICE --quiet --eval "db.$coll.find().pretty()"
    echo ""
done

