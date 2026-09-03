# Munchies
**Corso:** Software Project Engineering
**Membri del team:** Norbert Gabos (0001191794), Alessandro Agosta, Emiliano Rattini
**Repository link:** [https://github.com/Norby99/munchies](https://github.com/Norby99/munchies)

## 1. Introduzione

Il presente documento descrive la progettazione, l'architettura e le pratiche di sviluppo di una piattaforma distribuita per la gestione degli ordini alimentari e delle prenotazioni nei ristoranti. Il progetto nasce con l'obiettivo di applicare in un contesto realistico le metodologie avanzate di *Software Project Engineering*, focalizzandosi in modo particolare sul Domain-Driven Design, sull'automazione dei processi DevOps e sulla realizzazione di un'architettura multi-target scalabile.

Il dominio applicativo è stato modellato per gestire le interazioni complesse tra due tipologie principali di attori, offrendo funzionalità distinte basate sui loro ruoli:

*   **Clienti (End-Users):** Gli utenti finali utilizzano la piattaforma per esplorare il catalogo dei ristoranti aderenti, consultare i menu, comporre ed effettuare ordini per la consegna. Inoltre, il sistema offre loro la possibilità di prenotare fisicamente un tavolo presso le strutture.
*   **Manager (Ristoratori):** Gli esercenti dispongono di un'interfaccia dedicata per l'amministrazione e la configurazione del proprio locale. Attraverso il sistema, i manager possono aggiornare le informazioni anagrafiche, definire e modificare gli orari di apertura, gestire dinamicamente i menu offerti e organizzare la topologia e la disponibilità dei tavoli.

Per supportare l'indipendenza di questi domini e garantire elevati standard di manutenibilità, il sistema è stato ingegnerizzato adottando un'architettura a microservizi. Questa scelta ci ha permesso non solo di isolare logicamente i *Bounded Context* delineati dal DDD, ma anche di implementare un ecosistema eterogeneo basato su due diverse piattaforme target (Kotlin e TypeScript). L'intero ciclo di vita del software è supportato da pratiche rigorose di Continuous Integration e Continuous Delivery, garantendo test automatizzati, containerizzazione e orchestrazione fluida dei servizi.

## 2. Processo di Sviluppo e Gestione del Progetto
[Spiega le metodologie usate per organizzare il lavoro di squadra.]
* **Metodologia:** [es. Scrum, Kanban]
* **Branching Model:** [es. GitFlow, GitHub Flow, Trunk-based development]
* **Issue Tracking e PR:** [Come avete gestito le task board, le code review e la tracciabilità delle modifiche]

### 2.1 Branching Model

Per coordinare lo sviluppo parallelo e garantire la massima stabilità della codebase, abbiamo adottato GitFlow come branching model, affiancato da rigide policy di repository su GitHub.Protezione dei Branch Principali:
- Sui branch **master** e **develop** è configurato il divieto assoluto di force push per prevenire alterazioni distruttive della cronologia.
- Pull Request e Merge Strategy: Qualsiasi immissione di codice verso master o develop richiede obbligatoriamente l'apertura di una Pull Request. Per preservare una commit history lineare e pulita, la strategia di merge è forzata esclusivamente sul Rebase and Merge.
- Status Check Bloccanti: Il merge di una PR è interdetto finché non vengono superati con successo tutti i controlli automatici eseguiti da GitHub Actions (fasi di build, test e code quality).
- Gestione Proattiva dei Conflitti: Prima di poter unire una feature branch in develop, la branch stessa deve risultare strettamente allineata con l'ultimo stato di develop. Questo vincolo delega la responsabilità della risoluzione degli eventuali merge conflict al singolo sviluppatore, che deve aggiornare e ribasare localmente il proprio codice prima di finalizzare la Pull Request.

### 2.2 Issue Tracking e Segnalazione Bug

Per standardizzare la raccolta delle anomalie e facilitarne la riproduzione, abbiamo implementato degli *Issue Template*. Il modulo dedicato ai bug report obbliga la compilazione di campi essenziali per il debugging, richiedendo i dettagli dell'azione utente (comportamento atteso vs reale), gli eventuali log applicativi e l'ambiente di esecuzione. Inoltre, impone una classificazione esplicita della gravità (da *Critical* a *Cosmetic/Trivial*), permettendo team di valutare immediatamente l'impatto del bug sull'applicazione e stabilire un ordine di priorità chiaro per le correzioni.

###### Non so se ha senso mettere che abbiamo usato la roba di github project per tenere traccia delle robe fatte.

## 3. Domain-Driven Design (DDD)
[Questa è una delle sezioni a cui il corso tiene di più. Mostra come il codice riflette il dominio reale.]
* **Ubiquitous Language:** [Breve glossario dei termini chiave usati sia nel dominio che nel codice]
* **Bounded Contexts:** [Come avete isolato i diversi sottodomini del progetto]
* **Entità, Value Objects e Aggregati:** [Le scelte implementative principali che garantiscono la validità del dominio]

## 4. Architettura e Target Multi-Piattaforma
[Dimostra chiaramente il requisito dei "2+ target platforms" con runtime o build system differenti.]
* **Piattaforma 1 (es. Backend JVM/Kotlin):** [Descrizione, ruolo e build system (es. Gradle)]
* **Piattaforma 2 (es. Frontend NodeJS o Core in Rust/C++):** [Descrizione, ruolo e build system (es. npm/Cargo)]
* **Integrazione:** [Spiega come i due target comunicano tra loro (es. API REST, gRPC, FFI)]

- frontend
- gateway
- notification
- order
- payment
- restaurant
- scheduler
- table
- user

## 5. Pratiche DevOps e Continuous Integration
[Descrivi la "Full-scale automation" che avete implementato tramite GitHub Actions, GitLab CI, ecc.]
* **Pipeline di Build:** [Come vengono compilati e pacchettizzati i due target in modo automatizzato]
* **Testing Automatizzato:** [Copertura dei test, tipologie (Unit, Integration) e come la CI blocca le PR in caso di fallimento]
* **Code Quality:** [Strumenti di analisi statica utilizzati, es. SonarQube, linter automatici]



## 5. Pratiche DevOps e Continuous Integration

Per garantire l'affidabilità del sistema e supportare l'automazione continua, la validazione del codice è strutturata secondo una rigorosa **Piramide dei Test**. Ogni livello incrementa il perimetro di esecuzione, passando dall'isolamento della singola classe all'orchestrazione dell'intero sistema distribuito:

* **Unit Testing:** Tramite **JUnit** e **Kotest**, validiamo in totale isolamento la logica di business e le regole di dominio, assicurando un feedback immediato e privo di dipendenze infrastrutturali.
* **Integration Testing:** Verifichiamo il layer di persistenza sollevando dinamicamente istanze reali di database tramite **Testcontainers**. Questo approccio annulla le inconsistenze tipiche dei DB in-memory, testando l'interazione diretta e reale con MongoDB.
* **Component Testing:** Validiamo il singolo microservizio nella sua interezza con un approccio *black-box*. Inizializzando il contesto applicativo **Micronaut** in sinergia con i container di Testcontainers, simuliamo chiamate REST per certificare il routing, la serializzazione e i contratti API del servizio.
* **End-to-End Testing:** Sfruttiamo l'automazione di **Docker Compose** per il provisioning di un ambiente effimero di simil-produzione che include tutti i microservizi, le istanze MongoDB dedicate e il broker Kafka. Su questa infrastruttura eseguiamo suite automatizzate in ottica **BDD**, validando i flussi HTTP per rispecchiare e certificare le User Story originali.


## 6. Continuous Delivery e Containerizzazione
[Affronta il requisito di deploy automation.]
* **Containerizzazione:** [Descrivi i Dockerfile utilizzati, citando eventuali multi-stage builds per alleggerire le immagini finali]
* **Orchestrazione:** [Descrivi il `docker-compose.yml` o i manifest Kubernetes usati per far girare l'intero sistema in modo riproducibile]
* **Release Automation:** [Come vengono create automaticamente le release (es. Semantic Versioning) e il push degli artefatti su registry come Docker Hub o GitHub Packages]

Per soddisfare il requisito di deploy automation e garantire la riproducibilità dell'ambiente di esecuzione, il progetto adotta un'infrastruttura basata su containerizzazione e orchestrazione, progettata per supportare la nostra architettura a microservizi eterogenea.

### 6.1 Containerizzazione Dinamica e Build Automatica
Il sistema è composto da microservizi sviluppati su due piattaforme target differenti: Kotlin (JVM) e TypeScript (Node.js). Per automatizzare la creazione delle immagini Docker evitando duplicazioni, abbiamo implementato un approccio basato su template.
Il sistema di build (Gradle) è stato configurato in modo intelligente: durante la fase di pacchettizzazione, Gradle identifica automaticamente il linguaggio del microservizio in questione e seleziona il template del Dockerfile appropriato (per JVM o per Node.js) per generare l'immagine finale, a prescindere dallo stack tecnologico del singolo servizio.

### 6.2 Orchestrazione Locale e Integrazione (Docker Compose)
Per la validazione dell'intero sistema, i test di integrazione e l'ambiente di sviluppo locale, abbiamo definito un file `docker-compose.yml` che orchestra l'intera architettura. 
L'infrastruttura rispetta i principi cardine dei microservizi e del Domain-Driven Design:
* **Database-per-service:** Ogni microservizio possiede un proprio container MongoDB dedicato, garantendo un forte isolamento dei dati e l'indipendenza dei *Bounded Context*.
* **Event-Driven Communication:** È presente un container Kafka condiviso, utilizzato come message broker per la comunicazione asincrona tra i servizi.

L'intero ciclo di rilascio locale è stato mascherato e automatizzato dietro a task Gradle custom. Eseguendo il comando `./gradlew composeUp`, il sistema in totale autonomia provvede a:
1. Compilare i sorgenti (Kotlin e TypeScript).
2. Generare le relative immagini Docker tramite i template.
3. Avviare la rete di container (servizi, istanze MongoDB, Kafka).
Allo stesso modo, il comando `./gradlew composeDown` si occupa dello smantellamento pulito dell'ambiente.

### 6.3 Verso l'Ambiente di Produzione (Kubernetes)
Sebbene Docker Compose fornisca un'automazione eccellente per il testing e lo sviluppo, **TODO**
## 7. Conclusioni
[Riepilogo finale sui vantaggi riscontrati nell'applicare l'ingegneria del software, l'automazione e il DDD al vostro specifico problema.]

NOTE:
- i commit verificati su github li abbiamo provati, ma l'integrazione di github fa cagare al cazzo (microsoft sei del mestiere?)
- 
