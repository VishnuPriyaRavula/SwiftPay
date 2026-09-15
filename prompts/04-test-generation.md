# Prompt: Test Generation

Generate tests for the SwiftPay payment flow using real behavior where possible.

Cover:
- Valid payment returns 202 and creates PENDING state
- Redis idempotency replay returns the original transaction
- Validation rejects invalid IDs, amount, and currency
- Kafka completion performs one debit and one credit atomically
- Duplicate completion events do not duplicate analytics rows
- Insufficient funds and currency mismatch become FAILED
- Missing accounts and self-transfers fail safely
- Ledger history is newest-first
- Analytics row count and volume reconcile with completed payments
- Docker and health smoke checks

For every test, state the input, expected HTTP/event/database outcome, and cleanup requirements.
