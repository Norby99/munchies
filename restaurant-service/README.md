# Restaurant Service

Restaurant Service è un microservizio per la gestione della configurazione dei ristoranti, incluso le tavole, i menu, i driver e la configurazione dell'output della cucina.

## Struttura del Progetto

### Package Principali

#### Domain Models (`com.munchies.restaurant.domain`)
Modelli di dominio che rappresentano le entità principali secondo l'architettura DDD:

**Aggregate Roots** (AggregateRoot):
- **Restaurant**: Configurazione principale del ristorante (nome, indirizzo, telefono, email)
- **RestaurantConfiguration**: Configurazione completa del ristorante con tutte le sottoconfigurazioni
- **Table**: Tavole del ristorante (numero, capacità)
- **Menu**: Menu del ristorante con gli elementi
- **Driver**: Driver per le consegne

**Entities** (Entity):
- **MenuItem**: Elemento del menu con nome, descrizione, prezzo e tipologia di cucina
- **Cuisine**: Tipologia di cucina con capacità produttiva (items per ora)

**Value Objects** (EntityId):
- **RestaurantId**: Identificatore univoco del ristorante
- **TableId**: Identificatore univoco della tavola
- **MenuId**: Identificatore univoco del menu
- **MenuItemId**: Identificatore univoco dell'elemento del menu
- **DriverId**: Identificatore univoco del driver
- **CuisineId**: Identificatore univoco della tipologia di cucina

#### API Controllers (`com.munchies.restaurant.api`)
Interfacce delle API HTTP:
- **RestaurantHTTPApi**: API per la creazione, gestione e eliminazione di ristoranti
- **RestaurantConfigurationHTTPApi**: API per la gestione della configurazione del ristorante
- **TableHTTPApi**: API per l'aggiunta, modifica e eliminazione delle tavole
- **MenuHTTPApi**: API per la gestione dei menu
- **DriverHTTPApi**: API per la gestione dei driver
- **CuisineHTTPApi**: API per la configurazione dell'output della cucina

#### Models/DTOs (`com.munchies.restaurant.model`)
Data Transfer Objects per le richieste HTTP:
- **RestaurantRequest**: DTO per la creazione e modifica di ristoranti
- **TableRequest**: DTO per la creazione e modifica di tavole
- **MenuRequest**: DTO per la creazione e modifica di menu
- **DriverRequest**: DTO per la creazione e modifica di driver
- **CuisineRequest**: DTO per la configurazione della cucina
- **RestaurantConfigurationRequest**: DTO per la configurazione del ristorante

#### BDD Tests (`com.munchies.restaurant.bdd`)
Test Behavior-Driven Development con Cucumber:
- **RestaurantCreationSteps**: Step definition per la creazione e gestione dei ristoranti
- **RestaurantConfigurationSteps**: Step definition per la configurazione del ristorante
- **TableManagementSteps**: Step definition per la gestione delle tavole
- **MenuConfigurationSteps**: Step definition per la configurazione del menu
- **DriverConfigurationSteps**: Step definition per la configurazione dei driver
- **CuisineOutputConfigurationSteps**: Step definition per la configurazione dell'output della cucina

#### Feature Files (`src/test/resources/features`)
Scenari BDD in formato Gherkin:
- `restaurant_creation.feature`: Scenari per la creazione e gestione dei ristoranti
- `restaurant_configuration.feature`: Scenari per la configurazione del ristorante
- `table_management.feature`: Scenari per la gestione delle tavole
- `menu_configuration.feature`: Scenari per la configurazione dei menu
- `driver_configuration.feature`: Scenari per la configurazione dei driver
- `cuisine_output_configuration.feature`: Scenari per la configurazione dell'output della cucina

## Funzionalità

### Gestione Ristoranti
- Creazione di nuovi ristoranti con informazioni di base (nome, indirizzo, telefono, email)
- Recupero dei dettagli del ristorante
- Modifica dei dati del ristorante
- Eliminazione dei ristoranti
- Elenco di tutti i ristoranti
- Aggiunta di nuove tavole con numero e capacità
- Modifica della capacità e dello stato delle tavole
- Eliminazione delle tavole

### Configurazione Menu
- Creazione di menu con elementi configurabili
- Aggiunta di elementi menu con prezzo e tipologia di cucina
- Aggiornamento e eliminazione di menu

### Gestione Driver
- Aggiunta di nuovi driver con informazioni di contatto
- Modifica dei dati dei driver
- Gestione dello stato attivo/inattivo dei driver

### Configurazione Cucina
- Configurazione della tipologia di cucina
- Impostazione del formato di output (THERMAL_PRINTER, DISPLAY_SCREEN, ecc.)
- Aggiornamento dei formati di output

## Dipendenze

- Kotlin
- Micronaut Framework
- Project Reactor (Reactive Programming)
- Cucumber (BDD Testing)

## Build

Per compilare il progetto:
```bash
./gradlew build
```

Per eseguire i test:
```bash
./gradlew test
```

Per eseguire i test BDD:
```bash
./gradlew bddTest
```

