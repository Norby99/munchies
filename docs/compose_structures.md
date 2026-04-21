# Docker Compose Structure and Usage

## Directory Structure
All Docker Compose files are located in the `/config` directory. The structure is organized as follows:
- `/config/kafka`: Configuration for the Kafka cluster.
- `/config/mongodb`: Template configurations for MongoDB.
- `/config/service`: Configuration that manages all the individual microservices.

*Note: If you need to add a new service, please follow the conventions and examples set by the existing services.*

## Usage Commands

### Start Services
To start the services in detached mode (running in the background):
```bash
./gradlew composeUp -Pd
```
*(The `-Pd` flag is used to run the containers in detached mode).*

### Stop Services
To stop the running services:
```bash
./gradlew composeDown
```

### Delete Orphan Containers
To stop services and remove orphan containers along with attached volumes:
```bash
./gradlew composeDown -v
```

## Logging and Debugging

### Service Logs
To view the standard Docker logs for a specific service:
```bash
docker logs munchies-[service-name]
```

### Database Logs
To view the database logs or content for a specific service:
```bash
./gradlew composeShowDb -Pservice=[service-name]
```

### Specific Collection Logs
To view the database logs or content for a single, specific collection:
```bash
./gradlew composeShowDb -Pservice=[service-name] -Pcollection=[collection-name]
```
