param(
    [string]$Output = 'artifacts/swiftpay-load.pcapng',
    [int]$Requests = 1000000,
    [int]$TargetTps = 250,
    [int]$Concurrency = 64
)

$principal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
if (-not $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    throw 'Run this script from an Administrator PowerShell so pktmon can access the capture driver.'
}

New-Item -ItemType Directory -Force (Split-Path $Output) | Out-Null
$etl = [IO.Path]::ChangeExtension($Output, '.etl')
& 'C:\Windows\System32\pktmon.exe' filter remove | Out-Null
& 'C:\Windows\System32\pktmon.exe' filter add -p 8080 | Out-Null
& 'C:\Windows\System32\pktmon.exe' start --etw | Out-Null
try {
    $java = (Get-Command java.exe -ErrorAction Stop).Source
    $javac = (Get-Command javac.exe -ErrorAction Stop).Source
    & $javac "$PSScriptRoot\LoadTest.java"
    if ($LASTEXITCODE -ne 0) { throw 'LoadTest.java compilation failed.' }
    & $java -cp $PSScriptRoot LoadTest 'http://localhost:8080' $Requests $TargetTps $Concurrency
    if ($LASTEXITCODE -ne 0) { throw 'Java load test failed.' }
} finally {
    & 'C:\Windows\System32\pktmon.exe' stop | Out-Null
    & 'C:\Windows\System32\pktmon.exe' etl2pcap $etl -o $Output | Out-Null
    Remove-Item $etl -ErrorAction SilentlyContinue
}
if (-not (Test-Path $Output) -or (Get-Item $Output).Length -eq 0) {
    throw "PCAP conversion failed or produced an empty file: $Output"
}
$captureBytes = [IO.File]::ReadAllBytes($Output)
$captureText = [Text.Encoding]::ASCII.GetString($captureBytes)
if ($captureText.IndexOf('POST /v1/payments') -lt 0) {
    throw "PCAP does not contain SwiftPay payment request traffic: $Output"
}
Write-Host "PCAP written to $Output"
