$ErrorActionPreference = 'Stop'

$Versions = @(
    "1.21.6",
    "1.21.7",
    "1.21.8",
    "1.21.9",
    "1.21.10",
    "1.21.11",
    "26.1",
    "26.1.1",
    "26.1.2",
    "26.2",
    "26.3"
)

foreach ($Version in $Versions) {
    & (Join-Path $PSScriptRoot "build-one.ps1") $Version
}

Write-Host ""
Write-Host "All Sonarus Helper 1.0 builds completed."
Get-ChildItem (Join-Path $PSScriptRoot "dist\*.jar") | Select-Object Name, Length
