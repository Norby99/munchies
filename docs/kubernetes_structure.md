# Build setup

Every service must include a YAML file in the `/k8s/` folder named with its own service name. This could be a single file (es. `/k8s/order-service.yml`) or a directory containing multiple YAML files (es. `/k8s/order-service/*.yml`).

# Helpful scripts

There is a bash file in the root directory called `munchies.sh`. It is a simple wrapper for all the scripts contained in the `/scripts/` folder.

## Deploy

To deploy a service or all of them:

```bash
./munchies.sh deploy [service/all]
```

## Undeploy

To undeploy a service or all of them:

```bash
./munchies.sh undeploy [service/all]
```

To undeploy a service and also wipe all the statefulsets (es. DBs):

```bash
./munchies.sh undeploy [service/all] --wipe-data
```

## Debug

To show all the collections of a service:

```bash
./munchies.sh show-db [service]
```

To only show one specific collection:

```bash
./munchies.sh show-db [service] [collection-name]
```

<aside>
💡

NOTE: If you have just deployed a service and your PC is slow, the show-db command will return you no collections.

</aside>

## Add new scripts

At this moment if you want to add a new feature to munchies script you have to also modify the script and add a case to the switch.
