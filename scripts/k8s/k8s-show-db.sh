#!/usr/bin/env bash

SERVICE=$1
TARGET_COLLECTION=$2

if [ -z "$SERVICE" ]; then
    echo "Error: You must specify the service name."
    echo "Usage: ./scripts/k8s-show-db.sh <service-name> [collection-name]"
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

# Retrieve all db names, ignoring system ones
DBS=$(minikube kubectl -- exec -t $POD_NAME -n $SERVICE -- mongosh --quiet --eval "db.adminCommand('listDatabases').databases.map(d => d.name).join(' ')" | tr -d '\r')

for dbName in $DBS; do
    if [[ "$dbName" == "admin" || "$dbName" == "config" || "$dbName" == "local" ]]; then
        continue # Ignore system dbs
    fi

    echo ""
    echo "==========================================="
    echo "Processing Database: '$dbName'"
    echo "==========================================="
    echo "Collections:"
    minikube kubectl -- exec -t $POD_NAME -n $SERVICE -- mongosh $dbName --quiet --eval "show collections"

    echo ""
    if [ -n "$TARGET_COLLECTION" ]; then
        echo "Documents within collection '$TARGET_COLLECTION' in '$dbName':"
    else
        echo "Documents within each collection in '$dbName':"
    fi
    echo "-------------------------------------------"
    COLLECTIONS=$(minikube kubectl -- exec -t $POD_NAME -n $SERVICE -- mongosh $dbName --quiet --eval "db.getCollectionNames().join(' ')" | tr -d '\r')

    for coll in $COLLECTIONS; do
        if [ -n "$TARGET_COLLECTION" ] && [ "$coll" != "$TARGET_COLLECTION" ]; then
            continue
        fi

        # If the collection is empty do not query, but join(' ') handles strings here.
        if [ -n "$coll" ]; then
            echo "--> Collection: $coll"
            minikube kubectl -- exec -t $POD_NAME -n $SERVICE -- mongosh $dbName --quiet --eval "db.$coll.find().pretty()"
            echo ""
        fi
    done
done

