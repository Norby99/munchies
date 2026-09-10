# Project Structure & Build System
This project uses Gradle as its build system.

## Project Structure
_Munchies_ is a *monorepo* with a *multi project* structure. These are the project available inside the repository:

- ```architecture-rules``` (Kotlin): contains Konsist's architectural tests for a clean DDD architecture
- ```commons``` (Multiplatform): contains common code for all other projects
- ```e2e-test``` (Kotlin): contains end-to-end tests for the services
- ```frontend-service``` (Typescript): contains the code for the frontend
- ```gateway-service``` (Typescript): contains the gateway's microservice code
- ```gateway-shared``` (Multiplatform): contains the gateway's API signatures
- ```notification-service``` (Typescript): contains the notification's microservice code
- ```notification-shared``` (Multiplatform): contains the notification's API signatures
- ```order-service``` (Kotlin): contains the order's microservice code
- ```order-shared``` (Multiplatform): contains the order's API signatures
- ```payment-service``` (Typescript): contains the payment's microservice code
- ```payment-shared``` (Typescript): contains the payment's API signatures
- ```restaurant-service``` (Kotlin): contains the restaurant's microservice code
- ```restaurant-shared``` (Multiplatform): contains the restaurant's API signatures 
- table-reservation-service (Typescript): contains the table reservation's microservice code (INCOMPLETE)
- table-reservation-shared (Multiplatform): contains the table reservation's API signatures (INCOMPLETE)
- ```user-service``` (Kotlin): contains the user's microservice code
- ```user-shared``` (Multiplatform): contains the user's API signatures


## Build System

## Shared build logic

## Dependencies
