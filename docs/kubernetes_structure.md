# Build setup

Every service must include a YAML file in the `/k8s/` folder named with its own service name. This could be a single file (es. `/k8s/order-service.yml`) or a directory containing multiple YAML files (es. `/k8s/order-service/*.yml`).

# Helpful scripts

There are some helpful scripts to deploy/undeploy the services and also some utility functions
inside the `scripts` folder.
This scripts can be executed via gradle:

## Deploy

To deploy the services:

```bash
./gradlew deploy
```

## Undeploy

To undeploy the services:

```bash
./gradlew undeploy
```

To undeploy the services and also wipe all the statefulsets (es. DBs):

```bash
./gradlew undeploy [-PwipeData=true]
```

## Debug

To show all the collections of a service:

```bash
./gradlew showDb -Pservice=<service>
```

To only show one specific collection:

```bash
./gradlew showDb -Pservice=<service> [-Pcollection=<collection>]
```

To show data relative to Kafka:

```bash
./gradlew showKf [-Ptopic=<topic>]
```

To show pods and deployments:

```bash
./gradlew k8sInfo
```

<aside>

NOTE: If you have just deployed a service and your PC is slow, the show-db command will return you no collections.

</aside>

## Add new scripts

At this moment if you want to add a new feature to munchies script you have to also modify the script and add a case to the switch.
