param(
    [string]$Output = 'artifacts/swiftpay-load.pcapng',
    [int]$Requests = 1000000,
    [int]$TargetTps = 250,
    [int]$Concurrency = 64,
    [string]$CaptureImage = 'nicolaka/netshoot:latest'
)

New-Item -ItemType Directory -Force (Split-Path $Output) | Out-Null
$outputPath = (Resolve-Path (Split-Path $Output)).Path
$pcap = [IO.Path]::ChangeExtension($Output, '.pcap')
$pcapName = Split-Path $pcap -Leaf
$appContainer = (& docker compose ps -q app).Trim()
if (-not $appContainer) { throw 'The app container is not running. Start it with docker compose up -d --build.' }
$captureName = 'swiftpay-http-capture'
& docker rm -f $captureName 2>$null | Out-Null
$captureId = (& docker run -d --name $captureName --network "container:$appContainer" -v "${outputPath}:/captures" $CaptureImage tcpdump -i any -s 0 -U -w "/captures/$pcapName" 'tcp port 8080').Trim()
if (-not $captureId) { throw 'Could not start the Docker packet capture container.' }
try {
    $jdkHome = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { 'C:\Program Files\Eclipse Adoptium\jdk-21.0.3.9-hotspot' }
    $java = Join-Path $jdkHome 'bin\java.exe'
    $javac = Join-Path $jdkHome 'bin\javac.exe'
    if (-not (Test-Path $java) -or -not (Test-Path $javac)) { throw "Java 21 JDK not found at $jdkHome" }
    & $javac "$PSScriptRoot\LoadTest.java"
    if ($LASTEXITCODE -ne 0) { throw 'LoadTest.java compilation failed.' }
    & $java -cp $PSScriptRoot LoadTest 'http://localhost:8080' $Requests $TargetTps $Concurrency
    if ($LASTEXITCODE -ne 0) { throw 'Java load test failed.' }
} finally {
    & docker stop $captureName | Out-Null
    & docker rm $captureName | Out-Null
}
if (-not (Test-Path $pcap) -or (Get-Item $pcap).Length -eq 0) {
    throw "Docker capture failed or produced an empty file: $pcap"
}
$outputName = Split-Path $Output -Leaf
& docker run --rm -v "${outputPath}:/captures" $CaptureImage editcap -F pcapng "/captures/$pcapName" "/captures/$outputName" | Out-Null
Remove-Item $pcap -Force
if (-not (Test-Path $Output) -or (Get-Item $Output).Length -eq 0) {
    throw "PCAPNG conversion failed or produced an empty file: $Output"
}
$captureBytes = [IO.File]::ReadAllBytes($Output)
$captureText = [Text.Encoding]::ASCII.GetString($captureBytes)
if ($captureText.IndexOf('POST /v1/payments') -lt 0) {
    throw "PCAP does not contain SwiftPay payment request traffic: $Output"
}
Write-Host "PCAP written to $Output"
