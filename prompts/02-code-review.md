# Prompt: AI-Generated Backend Review

Review the proposed SwiftPay change as a senior backend engineer.

Check:
- REST semantics, validation, status codes, and OpenAPI accuracy
- Redis idempotency behavior and expiry
- PostgreSQL transaction boundaries and pessimistic locking
- Kafka serialization, retries, duplicate delivery, ordering, and failure handling
- Insufficient funds, currency mismatch, missing accounts, and self-transfer
- Concurrency, performance, and connection usage
- Error leakage, secrets, and unsafe input handling
- Analytics deduplication and reconciliation
- Test completeness and operational documentation

Return findings ordered by severity, then required tests and deployment risks. Do not approve code only because it compiles.
