#!/usr/bin/env bash

SERVICE=$1
TARGET_COLLECTION=$2

if [ -z "$SERVICE" ]; then
    echo "Error: You must specify the service name."
    echo "Usage: ./scripts/docker-show-db.sh <service-name> [collection-name]"
    exit 1
fi

# We assume docker compose mapped the mongodb container to this consistent name
CONTAINER_NAME="munchies-mongo-${SERVICE}"

if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "Error: Could not find a running MongoDB container named '$CONTAINER_NAME'."
    exit 1
fi

echo "Found database container: $CONTAINER_NAME"

echo "==========================================="
echo "Databases available on the server:"
echo "==========================================="
docker exec -t "$CONTAINER_NAME" mongosh --quiet --eval "show dbs"

# Retrieve all db names, ignoring system ones
DBS=$(docker exec -t "$CONTAINER_NAME" mongosh --quiet --eval "db.adminCommand('listDatabases').databases.map(d => d.name).join(' ')" | tr -d '\r')

for dbName in $DBS; do
    if [[ "$dbName" == "admin" || "$dbName" == "config" || "$dbName" == "local" ]]; then
        continue # Ignore system dbs
    fi

    echo ""
    echo "==========================================="
    echo "Processing Database: '$dbName'"
    echo "==========================================="
    echo "Collections:"
    docker exec -t "$CONTAINER_NAME" mongosh "$dbName" --quiet --eval "show collections"

    echo ""
    if [ -n "$TARGET_COLLECTION" ]; then
        echo "Documents within collection '$TARGET_COLLECTION' in '$dbName':"
    else
        echo "Documents within each collection in '$dbName':"
    fi
    echo "-------------------------------------------"
    COLLECTIONS=$(docker exec -t "$CONTAINER_NAME" mongosh "$dbName" --quiet --eval "db.getCollectionNames().join(' ')" | tr -d '\r')

    for coll in $COLLECTIONS; do
        if [ -n "$TARGET_COLLECTION" ] && [ "$coll" != "$TARGET_COLLECTION" ]; then
            continue
        fi

        if [ -n "$coll" ]; then
            echo "--> Collection: $coll"
            docker exec -t "$CONTAINER_NAME" mongosh "$dbName" --quiet --eval "db.$coll.find().pretty()"
            echo ""
        fi
    done
done

