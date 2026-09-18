param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$MinecraftVersion
)

$ErrorActionPreference = 'Stop'

$Root = $PSScriptRoot
$Project = Join-Path $Root "versions\$MinecraftVersion"

if (-not (Test-Path $Project -PathType Container)) {
    throw "Unsupported Minecraft version: $MinecraftVersion"
}

$Gradle = "C:\Gradle\gradle-9.7.1\bin\gradle.bat"
if (-not (Test-Path $Gradle -PathType Leaf)) {
    $GradleCommand = Get-Command gradle -ErrorAction SilentlyContinue
    if ($null -eq $GradleCommand) {
        throw "Gradle not found. Expected C:\Gradle\gradle-9.7.1\bin\gradle.bat or gradle in PATH."
    }
    $Gradle = $GradleCommand.Source
}

$Task = if ($MinecraftVersion -like "26.*") { "jar" } else { "remapJar" }

Write-Host "Building Sonarus Helper 1.0 for Minecraft $MinecraftVersion ($Task)..."
Push-Location $Project
try {
    & $Gradle $Task --console=plain
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle failed for Minecraft $MinecraftVersion with exit code $LASTEXITCODE"
    }
} finally {
    Pop-Location
}

$Jar = Get-ChildItem (Join-Path $Project "build\libs\*.jar") |
    Where-Object { $_.Name -notmatch "sources|dev" } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if ($null -eq $Jar) {
    throw "No production JAR found for Minecraft $MinecraftVersion"
}

$Dist = Join-Path $Root "dist"
New-Item -ItemType Directory -Force -Path $Dist | Out-Null
$Target = Join-Path $Dist "sonarus-helper-1.0-mc$MinecraftVersion.jar"
Copy-Item $Jar.FullName $Target -Force

Write-Host "Built: $Target"
