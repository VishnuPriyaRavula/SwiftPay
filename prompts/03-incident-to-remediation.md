# Prompt: Incident and Feedback to Remediation

Given a SwiftPay user report, API error, application log, Kafka lag report, database query, or monitoring alert:

1. Summarize the customer impact.
2. Separate observed facts from hypotheses.
3. Identify the likely controlling code path.
4. Propose the cheapest reproducing check.
5. Generate a focused regression test.
6. Propose the smallest remediation with rollback notes.
7. Define validation metrics: error rate, latency, consumer lag, pending payments, and analytics reconciliation.
8. Produce release notes and an operational follow-up.

Do not recommend a production change without a verification step.
