param([string]$PostgresContainer = 'hackathonproject-postgres-1')

& docker exec $PostgresContainer psql -U swiftpay -d swiftpay -c @"
SELECT status, count(*) AS payments
FROM payments
GROUP BY status
ORDER BY status;

SELECT
  (SELECT count(*) FROM payments WHERE status = 'COMPLETED') AS completed_payments,
  (SELECT count(*) FROM payment_analytics) AS analytics_rows,
  (SELECT COALESCE(sum(amount), 0) FROM payments WHERE status = 'COMPLETED') AS completed_volume,
  (SELECT COALESCE(sum(amount), 0) FROM payment_analytics) AS analytics_volume;
"@
