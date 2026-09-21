# AGENTS.md

Guidance for coding agents working in this repository.

## What this is

Java 17 Maven monorepo with two Spring Boot microservices orchestrated by Temporal. There is **no database**. Inventory is hardcoded in memory; orders are stored in memory. Do **not** add Docker or a Temporal server to this repo — developers already run Temporal locally (`127.0.0.1:7233`, override with `TEMPORAL_ADDRESS`).

## Modules

| Module | Role |
| --- | --- |
| `common` | Shared Temporal contracts and models only (no Spring Boot `main`) |
| `inventory-service` | HTTP on `:8081` + worker on `inventory-task-queue` |
| `order-service` | HTTP on `:8080` + `PlaceOrderWorkflow` + worker on `order-task-queue` |

Package root: `com.example.shop`.

## Temporal rules

- Workflows orchestrate only. No I/O, no `Thread.sleep`, no Spring beans inside workflow implementations. Use `Workflow.getLogger` and `Workflow.newActivityStub`.
- Activities do I/O and in-memory store updates. Annotate implementations with `@ActivityImpl` / `@WorkflowImpl` and task queues from `TaskQueues`.
- The order workflow must call inventory **via Temporal activities** (cross-queue stubs), not REST.
- Keep compensation: if `recordOrder` fails after `reserve`, call `release`.
- Non-retryable inventory errors: `InsufficientStockException`, `UnknownSkuException`, `IllegalArgumentException`.
- Shared interfaces and DTOs live in `common`. Implementations stay in the owning service.

## Models

Public class names must match file names:

- `CustomerOrder` — place-order request
- `Order` — placed order result
- `Item` — catalog/stock row
- `ReservationResult` — inventory reservation

## Maven / run

Always use the wrapper (`./mvnw`). Never run `spring-boot:run` on the parent POM.

```bash
./mvnw clean verify
./mvnw -pl inventory-service -am spring-boot:run
./mvnw -pl order-service -am spring-boot:run
```

Parent and `common` skip the Spring Boot plugin (`skip=true`). Runnable modules set `skip=false` and an explicit `mainClass`.

Starting stock: `WIDGET-RED` 10, `WIDGET-BLUE` 5, `GADGET-1` 2.

## Style

- Keep this a small sample: no extra frameworks, DBs, or services unless asked.
- Prefer deterministic Temporal tests with `TestWorkflowEnvironment` in `order-service`.
- Do not commit secrets. Do not change git config or force-push.
