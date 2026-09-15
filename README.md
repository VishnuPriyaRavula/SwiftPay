# SwiftPay Real-Time Payment Ledger

SwiftPay is an event-driven P2P payment ledger implemented with Java 21 and Spring Boot. The gateway accepts an idempotent request, persists it as `PENDING`, and emits `PaymentInitiated`. The ledger consumer locks both accounts, applies a debit/credit atomically, and emits `PaymentCompleted` or `PaymentFailed`.

The AI-native engineering workflow is documented in [AI_PLAYBOOK.md](AI_PLAYBOOK.md), with reusable planning, review, incident, and test-generation prompts under [prompts](prompts).

## Run locally

Prerequisites: Java 21 and Docker Compose.

```powershell
mvn test
mvn package
 docker compose up --build
```

The API is available at `http://localhost:8080`. Swagger UI is at `/swagger-ui.html`; health is at `/actuator/health`.

## API

`POST /v1/payments` accepts `senderId`, `receiverId`, `amount`, and a three-letter `currency`. Supply a UUID `Idempotency-Key`; the same key is protected in Redis for 24 hours. The response is `202 Accepted` with status `PENDING`.

`GET /v1/payments/{transactionId}` returns the current state. `GET /v1/ledger/{userId}/payments` returns newest-first history for either side of a payment.

`GET /v1/analytics/volume` returns the number and total amount of completed payments consumed by the analytics worker. The worker persists a deduplicated analytics row for every `PaymentCompleted` event; this local table is the challenge's allowed mock OLAP integration.

## Operational notes

- PostgreSQL is the source of truth for balances and payment state.
- Pessimistic account locks serialize concurrent transfers and prevent double spending.
- Kafka retries listener delivery after transient failures using Spring Kafka's default error handling; failed business validations are recorded as `FAILED`.
- Redis stores only the idempotency key, so losing the cache does not lose financial state.
- `k8s/swiftpay.yaml` includes a namespace, config, rolling deployment, probes, service, and HPA. Create a `swiftpay-secrets` Secret before applying it.
- The GitHub Actions workflow runs tests, packages the application, and builds the container image.
- `mvn verify` includes a PostgreSQL Testcontainers repository integration test when Docker is available; it is explicitly skipped when Docker is unavailable.
- `scripts/reconcile.ps1` compares payment totals with analytics totals after Kafka has drained all `PENDING` rows.
- `scripts/capture-load-test.ps1` requires Administrator PowerShell and fails if the resulting PCAPNG is empty.

## Test evidence

The completed performance run used `scripts/LoadTest.java` and produced 1,000,000 successful requests at 250 TPS. Run the capture wrapper from Administrator PowerShell to produce `artifacts/swiftpay-load.pcapng`; the script validates that the trace contains `POST /v1/payments` before accepting it.

## Demo account setup

Insert accounts in PostgreSQL before posting payments:

```sql
INSERT INTO user_accounts (user_id, balance, currency, version)
VALUES ('11111111-1111-1111-1111-111111111111', 1000.00, 'USD', 0),
       ('22222222-2222-2222-2222-222222222222', 100.00, 'USD', 0);
```
