# Design

This chapter explains how _Munchies_ meets the [requirements](../02-requirements/requirements.md). It is organized as
follows:

| Section                                                      | Question it answers                                        |
|--------------------------------------------------------------|------------------------------------------------------------|
| [Architecture](architecture.md)                              | Which style the system follows, and why                    |
| [Modelling](modelling.md)                                    | Which entities, events, messages and state exist           |
| [Interaction](interaction.md)                                | How components communicate                                 |
| [Behaviour](behaviour.md)                                    | How each component reacts, and who updates the state       |
| [Data and Consistency Issues](data-and-consistency-issues.md) | What is stored, where, and which guarantees hold          |
| [Infrastructure](infrastructure.md)                          | Which components are deployed, and how they find each other |
| [Fault-Tolerance](fault-tolerance.md)                        | What happens when a component fails                        |
| [Availability](availability.md)                              | How load is spread, and what a network partition causes    |
| [Security](security.md)                                      | How access is controlled, and which cryptography is used   |

The main design decisions, and the requirements driving them, are the following:

| Decision                                                           | Main driver                    | Discussed in                                                  |
|--------------------------------------------------------------------|--------------------------------|---------------------------------------------------------------|
| The system is split into services, one per bounded context         | `NFR1`, `NFR9`, `NFR17`        | [Architecture](architecture.md)                               |
| Every client request goes through a single stateless gateway       | `FR-A7`, `NFR12`               | [Architecture](architecture.md), [Security](security.md)      |
| Business requests are synchronous, notifications are asynchronous  | `NFR6`                         | [Interaction](interaction.md)                                 |
| Events carry all the data their consumer needs                     | `NFR6`                         | [Interaction](interaction.md)                                 |
| Each service owns its data                                         | `NFR9`                         | [Data and Consistency Issues](data-and-consistency-issues.md) |
| Orders are stored as an append-only sequence of events             | `FR-O8`, `NFR8`                | [Data and Consistency Issues](data-and-consistency-issues.md) |
| Reads and writes on orders use separate models                     | `FR-O7`, `NFR10`               | [Data and Consistency Issues](data-and-consistency-issues.md) |
| Only stateless services are replicated                             | `NFR1`, `NFR3`                 | [Availability](availability.md)                               |

The chapter describes the system as it is designed today. Parts that do not exist yet are always marked as _planned_,
and each section ends with the limits of the current design.
