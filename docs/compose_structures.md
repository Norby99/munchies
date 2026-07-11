# Docker Compose Structure and Usage

## Directory Structure

All Docker Compose files are located in the `/config` directory. The structure is organized as follows:

- `/config/kafka`: Configuration for the Kafka cluster.
- `/config/mongodb`: Configuration for MongoDB.
- `/config/service`: Configuration that manages all the individual microservices.

*Note: If you need to add a new service, please follow the conventions and examples set by the existing services.*

## Usage Commands

### Start Services

To start the services:

```bash
./gradlew composeUp
```

### Stop Services

To stop the running services:

```bash
./gradlew composeDown
```

### Delete Volumes

To delete the volumes associated with the services:

```bash
./gradlew composeDown -Preset
```

## Logging and Debugging

### Service Logs

To view the standard Docker logs for a specific service:

```bash
docker logs munchies-[service-name]
```

### Database Logs

To show the collections of a specific service's database:

```bash
./gradlew composeShowDb -Pservice=[service-name]
```

### Specific Collection Logs

To view the database logs or content for a single, specific collection:

```bash
./gradlew composeShowDb -Pservice=[service-name] -Pcollection=[collection-name]
```

# FAQ

## composeUp hangs indefinitely

This is because some services do not pass the health check.
It might happen if:

- The service does not expose any API port -> Just remove the port mapping from the `docker-compose.yml`
- The service stops running -> In this case you have to debug the service or just don't use it in the compose file.
