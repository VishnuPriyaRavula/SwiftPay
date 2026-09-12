import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public final class LoadTest {
    public static void main(String[] args) throws Exception {
        String baseUrl = args.length > 0 ? args[0] : "http://localhost:8080";
        int requests = args.length > 1 ? Integer.parseInt(args[1]) : 1_000_000;
        int targetTps = args.length > 2 ? Integer.parseInt(args[2]) : 250;
        int concurrency = args.length > 3 ? Integer.parseInt(args[3]) : 64;
        String body = "{\"senderId\":\"11111111-1111-1111-1111-111111111111\","
                + "\"receiverId\":\"22222222-2222-2222-2222-222222222222\","
                + "\"amount\":0.01,\"currency\":\"USD\"}";

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        Semaphore permits = new Semaphore(concurrency);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();
        CompletableFuture<?>[] pending = new CompletableFuture<?>[concurrency];
        long start = System.nanoTime();
        long intervalNanos = 1_000_000_000L / targetTps;

        for (int i = 0; i < requests; i++) {
            permits.acquire();
            String idempotencyKey = UUID.randomUUID().toString();
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/v1/payments"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Idempotency-Key", idempotencyKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            CompletableFuture<HttpResponse<String>> future = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            future.handle((response, error) -> {
                if (error == null && response.statusCode() >= 200 && response.statusCode() < 300) success.incrementAndGet();
                else failed.incrementAndGet();
                permits.release();
                return null;
            });
            long targetElapsed = (long) (i + 1) * intervalNanos;
            long remaining = targetElapsed - (System.nanoTime() - start);
            if (remaining > 0) Thread.sleep(Duration.ofNanos(remaining));
        }
        for (int i = 0; i < concurrency; i++) permits.acquire();
        double elapsed = (System.nanoTime() - start) / 1_000_000_000.0;
        System.out.printf("Requests=%d Success=%d Failed=%d ElapsedSeconds=%.2f ActualTps=%.2f TargetTps=%d%n",
                requests, success.get(), failed.get(), elapsed, requests / elapsed, targetTps);
    }
}
