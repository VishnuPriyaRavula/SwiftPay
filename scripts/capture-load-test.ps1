param(
    [string]$Output = 'artifacts/swiftpay-load.pcapng',
    [int]$Requests = 1000000,
    [int]$TargetTps = 250
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
    & "$PSScriptRoot\load-test.ps1" -Requests $Requests -TargetTps $TargetTps
} finally {
    & 'C:\Windows\System32\pktmon.exe' stop | Out-Null
    & 'C:\Windows\System32\pktmon.exe' etl2pcap $etl -o $Output | Out-Null
    Remove-Item $etl -ErrorAction SilentlyContinue
}
if (-not (Test-Path $Output) -or (Get-Item $Output).Length -eq 0) {
    throw "PCAP conversion failed or produced an empty file: $Output"
}
Write-Host "PCAP written to $Output"
