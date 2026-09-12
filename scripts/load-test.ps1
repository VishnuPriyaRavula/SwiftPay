param(
    [string]$BaseUrl = 'http://localhost:8080',
    [int]$Requests = 1000000,
    [int]$TargetTps = 250,
    [int]$Concurrency = 64
)

$sender = '11111111-1111-1111-1111-111111111111'
$receiver = '22222222-2222-2222-2222-222222222222'
$body = @{ senderId = $sender; receiverId = $receiver; amount = 0.01; currency = 'USD' } | ConvertTo-Json -Compress
Add-Type -AssemblyName System.Net.Http
$client = [System.Net.Http.HttpClient]::new()
$client.Timeout = [TimeSpan]::FromSeconds(30)
$semaphore = [System.Threading.SemaphoreSlim]::new($Concurrency)
$success = 0; $failed = 0; $stopwatch = [Diagnostics.Stopwatch]::StartNew()
$tasks = [System.Collections.Generic.List[object]]::new()

for ($i = 1; $i -le $Requests; $i++) {
    $semaphore.Wait()
    $transactionId = [Guid]::NewGuid().ToString()
    $requestClient = $client
    $requestUrl = "$BaseUrl/v1/payments"
    $requestBody = $body
    $requestId = $transactionId
    $requestSemaphore = $semaphore
    $tasks.Add([System.Threading.Tasks.Task]::Run([Func[int]]{
        try {
            $content = [System.Net.Http.StringContent]::new($requestBody, [Text.Encoding]::UTF8, 'application/json')
            $content.Headers.Add('Idempotency-Key', $requestId)
            $response = $requestClient.PostAsync($requestUrl, $content).GetAwaiter().GetResult()
            if ([int]$response.StatusCode -ge 200 -and [int]$response.StatusCode -lt 300) { return 1 }
            return 0
        } catch { return 0 } finally { $requestSemaphore.Release() | Out-Null }
    }))
    if ($tasks.Count -ge $Concurrency) {
        [System.Threading.Tasks.Task]::WaitAll($tasks.ToArray())
        foreach ($task in $tasks) { if ($task.Result -eq 1) { $success++ } else { $failed++ } }
        $tasks.Clear()
        $expectedSeconds = $i / $TargetTps
        $delay = $expectedSeconds - $stopwatch.Elapsed.TotalSeconds
        if ($delay -gt 0) { Start-Sleep -Milliseconds ([int]($delay * 1000)) }
    }
}
$stopwatch.Stop()
$elapsed = [Math]::Max($stopwatch.Elapsed.TotalSeconds, 0.001)
[pscustomobject]@{ Requests = $Requests; Success = $success; Failed = $failed; ElapsedSeconds = [Math]::Round($elapsed, 2); ActualTps = [Math]::Round($Requests / $elapsed, 2); TargetTps = $TargetTps } | Format-List
