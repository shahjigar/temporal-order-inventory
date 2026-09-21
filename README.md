# Temporal order + inventory sample

Two Java microservices in one Maven repo. The **order service** starts a Temporal workflow; that workflow calls **inventory activities** on a separate task queue before recording the order. There is no database: catalog and orders live in memory.

Uses the Temporal server already running on your machine (`127.0.0.1:7233` by default).

## Layout

| Module | What it does |
| --- | --- |
| `common` | Shared workflow/activity interfaces and DTOs |
| `inventory-service` | HTTP catalog (`:8081`) + worker on `inventory-task-queue` |
| `order-service` | HTTP orders (`:8080`) + `PlaceOrderWorkflow` + worker on `order-task-queue` |

Hardcoded starting stock:

- `WIDGET-RED` — 10
- `WIDGET-BLUE` — 5
- `GADGET-1` — 2

## How an order is placed

1. `POST /api/orders` on the order service starts `PlaceOrderWorkflow`.
2. The workflow runs an **inventory activity** (`reserve`) on `inventory-task-queue`.
3. If stock is available, it runs an **order activity** (`recordOrder`) on `order-task-queue`.
4. If recording the order fails, the workflow **releases** the reservation (compensation).

## Prerequisites

- JDK 17+
- Temporal server on localhost (port `7233`)

If Temporal is on another host or port:

```bash
export TEMPORAL_ADDRESS=127.0.0.1:7233
```

## Run

Use `-pl` so Maven starts a **service** module, not the parent repo (the parent has no `main` class). `-am` still builds `common` first.

Terminal 1 — inventory worker + API:

```bash
./mvnw -pl inventory-service -am spring-boot:run
```

Terminal 2 — order worker + API:

```bash
./mvnw -pl order-service -am spring-boot:run
```

Place an order (this waits for the workflow to finish):

```bash
curl -s -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"customerId":"cust-1","sku":"WIDGET-RED","quantity":2}'
```

Inspect stock after the reservation:

```bash
curl -s http://localhost:8081/api/inventory/WIDGET-RED
```

Insufficient stock returns HTTP 409:

```bash
curl -s -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"customerId":"cust-1","sku":"GADGET-1","quantity":99}'
```

Tests (in-memory Temporal test server, no local Temporal required):

```bash
./mvnw test
```

## Temporal pieces

- **Workflow** — `PlaceOrderWorkflow` / `PlaceOrderWorkflowImpl` (orchestration only; no I/O)
- **Activities** — `InventoryActivities` (inventory service) and `OrderActivities` (order service)
- **Workers** — started automatically with each Spring Boot app
- **Task queues** — `order-task-queue` and `inventory-task-queue` in `TaskQueues`

The order workflow talks to inventory **through Temporal**, not REST. Inventory still exposes REST so you can inspect hardcoded stock.
