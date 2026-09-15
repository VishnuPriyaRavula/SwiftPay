# SwiftPay AI-Native Engineering Playbook

This playbook describes the requirement-to-production workflow used for the SwiftPay payment ledger.

## 1. Input and Context

Inputs include the hackathon requirements, user feedback, incident reports, API errors, Kafka lag, database state, and load-test evidence. The engineer first supplies the AI agent with the relevant source files, API contract, data model, deployment manifests, logs, and acceptance criteria.

## 2. Understand and Plan

Before changing code, identify the owning controller, service, repository, event schema, database transaction boundary, and operational dependencies. Produce a plan that names affected files, API compatibility risks, migration impact, test cases, and rollback considerations.

## 3. Implement

Use the agent to generate or modify a small vertical slice. For payment changes, preserve the invariant that PostgreSQL is authoritative, account updates are atomic, duplicate transaction IDs are idempotent for 24 hours, and Kafka events are replay-safe.

## 4. Test and Validate

Generate unit, API, integration, negative, boundary, and event-consumer tests. Run Maven tests, inspect Surefire reports, validate Docker Compose, exercise the live API, and reconcile payment totals against analytics totals. Never accept generated code solely because it compiles.

## 5. Review

Review correctness, concurrency, duplicate delivery, insufficient funds, currency mismatch, database locking, event ordering, retry behavior, error responses, observability, and backward compatibility. Check that OpenAPI reflects the implementation.

## 6. Deploy and Observe

Build the executable Spring Boot JAR and Docker image. Deploy with Compose or Kubernetes, verify health probes, inspect structured application logs, monitor Kafka consumer lag, and compare completed payment volume with analytics volume.

## 7. Feedback Loop

Convert a production symptom into a reproducible test, locate the controlling code path, implement the smallest fix, run focused validation, update documentation, and preserve the evidence in CI artifacts or the release submission.

## Acceptance Evidence

- `mvn verify` and Surefire reports
- Docker Compose health and API smoke checks
- `load-test-result.log` for 1,000,000 requests at 250 TPS
- `scripts/reconcile.ps1` showing zero pending payments and matching analytics totals
- A non-empty PCAPNG containing `POST /v1/payments` traffic
- GitHub Actions build and test artifacts
