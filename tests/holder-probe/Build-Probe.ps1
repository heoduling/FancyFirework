param(
    [Parameter(Mandatory=$true)][string]$RuntimeDir,
    [Parameter(Mandatory=$true)][string]$OutputDir,
    [string]$Jdk = 'D:/java'
)
$ErrorActionPreference = 'Stop'
foreach ($folder in 'agent','probe') {
    $path = Join-Path $OutputDir $folder
    if (-not (Test-Path -LiteralPath $path)) { New-Item -ItemType Directory -Path $path | Out-Null }
}
& "$Jdk/bin/javac.exe" '-J-Duser.language=en' --release 25 -encoding UTF-8 -d "$OutputDir/agent" "$PSScriptRoot/SnapshotAgent.java" "$PSScriptRoot/SnapshotCounter.java"
if ($LASTEXITCODE) { throw 'Snapshot observer compilation failed' }
@('Manifest-Version: 1.0','Premain-Class: ffprobe.SnapshotAgent','') | Set-Content -LiteralPath "$OutputDir/agent-manifest.mf" -Encoding ascii
& "$Jdk/bin/jar.exe" --create --file "$OutputDir/snapshot-agent.jar" --manifest "$OutputDir/agent-manifest.mf" -C "$OutputDir/agent" ffprobe
if ($LASTEXITCODE) { throw 'Agent archive failed' }
& "$Jdk/bin/jar.exe" --create --file "$OutputDir/snapshot-counter.jar" -C "$OutputDir/agent" ffprobe/SnapshotCounter.class
if ($LASTEXITCODE) { throw 'Counter archive failed' }
$libraries = @(Get-ChildItem -LiteralPath "$RuntimeDir/libraries" -Recurse -File -Filter '*.jar' | Where-Object { $_.Name -match '^(shiroha-api|adventure-api|adventure-key|examination-api|bungeecord-chat|guava-)' })
$annotations = Get-ChildItem -LiteralPath "$env:USERPROFILE/.m2/repository/org/jetbrains/annotations" -Recurse -File -Filter '*.jar' | Select-Object -Last 1
$classpath = (@($libraries.FullName) + @($annotations.FullName)) -join ';'
& "$Jdk/bin/javac.exe" '-J-Duser.language=en' --release 25 -encoding UTF-8 -classpath $classpath -d "$OutputDir/probe" "$PSScriptRoot/HolderProbe.java"
if ($LASTEXITCODE) { throw 'Integration probe compilation failed' }
Copy-Item -LiteralPath "$PSScriptRoot/plugin.yml" -Destination "$OutputDir/probe/plugin.yml"
& "$Jdk/bin/jar.exe" --create --file "$RuntimeDir/plugins/HolderProbe.jar" -C "$OutputDir/probe" .
if ($LASTEXITCODE) { throw 'Probe archive failed' }
