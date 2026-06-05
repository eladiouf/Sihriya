param(
    [Parameter(Mandatory = $true)]
    [string]$ProjectRoot,

    [Parameter(Mandatory = $true)]
    [string]$BundleDir,

    [Parameter(Mandatory = $true)]
    [string]$StandaloneDir,

    [Parameter(Mandatory = $true)]
    [string]$MinecraftVersion,

    [Parameter(Mandatory = $true)]
    [string]$ForgeVersion,

    [string]$ServerPort = '25567',

    [int]$TimeoutSeconds = 180
)

$ErrorActionPreference = 'Stop'

function Require-File {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,
        [Parameter(Mandatory = $true)]
        [string]$Label
    )

    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "$Label is missing: $Path"
    }
}

function Write-Utf8NoBom {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,
        [Parameter(Mandatory = $true)]
        [string]$Content
    )

    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($Path, $Content, $utf8NoBom)
}

$projectRoot = (Resolve-Path -LiteralPath $ProjectRoot).Path
$bundleDir = (Resolve-Path -LiteralPath $BundleDir).Path
if (Test-Path -LiteralPath $StandaloneDir) {
    $standaloneDir = (Resolve-Path -LiteralPath $StandaloneDir).Path
} else {
    $standaloneDir = $StandaloneDir
}

$modsDir = Join-Path $bundleDir 'mods'
$configDir = Join-Path $bundleDir 'config'
$defaultConfigsDir = Join-Path $bundleDir 'defaultconfigs'
$runtimeDependencyNames = @(
    'epic-fight-20.14.17-mc1.20.1-forge.jar',
    'epic-fight-invincible-lib-20.14.8.2-mc1.20.1-forge.jar'
)

Require-File -Path (Join-Path $modsDir 'sihriya-1.0.0.jar') -Label 'Release smoke Sihriya jar'
Require-File -Path (Join-Path $modsDir 'statmod-1.0.0.jar') -Label 'Release smoke STAT Mod jar'
Require-File -Path (Join-Path $configDir 'fml.toml') -Label 'Release smoke fml.toml'
Require-File -Path (Join-Path $defaultConfigsDir 'forge-server.toml') -Label 'Release smoke forge-server.toml'

New-Item -ItemType Directory -Path $standaloneDir -Force | Out-Null

$installerDir = Join-Path $standaloneDir '_installer'
New-Item -ItemType Directory -Path $installerDir -Force | Out-Null

$forgeCoordinate = "$MinecraftVersion-$ForgeVersion"
$installerJar = Join-Path $installerDir "forge-$forgeCoordinate-installer.jar"
$installerUrl = "https://maven.minecraftforge.net/net/minecraftforge/forge/$forgeCoordinate/forge-$forgeCoordinate-installer.jar"
$winArgsFile = Join-Path $standaloneDir "libraries/net/minecraftforge/forge/$forgeCoordinate/win_args.txt"
$userJvmArgsFile = Join-Path $standaloneDir 'user_jvm_args.txt'

if (-not (Test-Path -LiteralPath $installerJar -PathType Leaf)) {
    Invoke-WebRequest -Uri $installerUrl -OutFile $installerJar
}

if (-not (Test-Path -LiteralPath $winArgsFile -PathType Leaf) -or -not (Test-Path -LiteralPath $userJvmArgsFile -PathType Leaf)) {
    & java -jar $installerJar --installServer | Out-Null
}

foreach ($name in @('mods', 'config', 'defaultconfigs', 'world', 'logs', 'crash-reports')) {
    $target = Join-Path $standaloneDir $name
    if (Test-Path -LiteralPath $target) {
        Remove-Item -LiteralPath $target -Recurse -Force
    }
}
foreach ($name in @('eula.txt', 'server.properties', 'standalone-run.stdout.log', 'standalone-run.stderr.log')) {
    $target = Join-Path $standaloneDir $name
    if (Test-Path -LiteralPath $target) {
        Remove-Item -LiteralPath $target -Force
    }
}

Copy-Item -LiteralPath $modsDir -Destination (Join-Path $standaloneDir 'mods') -Recurse -Force
Copy-Item -LiteralPath $configDir -Destination (Join-Path $standaloneDir 'config') -Recurse -Force
Copy-Item -LiteralPath $defaultConfigsDir -Destination (Join-Path $standaloneDir 'defaultconfigs') -Recurse -Force

$standaloneModsDir = Join-Path $standaloneDir 'mods'
foreach ($jarName in $runtimeDependencyNames) {
    $candidatePaths = @(
        (Join-Path $projectRoot "libs/$jarName"),
        (Join-Path $projectRoot "../STAT_MOD/libs/$jarName"),
        (Join-Path $projectRoot "../statmod-push/libs/$jarName")
    )
    $resolvedDependency = $candidatePaths | Where-Object { Test-Path -LiteralPath $_ -PathType Leaf } | Select-Object -First 1
    if (-not $resolvedDependency) {
        throw "Missing standalone runtime dependency jar $jarName. Checked: $($candidatePaths -join ', ')"
    }
    Copy-Item -LiteralPath $resolvedDependency -Destination (Join-Path $standaloneModsDir $jarName) -Force
}

Write-Utf8NoBom -Path (Join-Path $standaloneDir 'eula.txt') -Content @"
#By changing the setting below to TRUE you are indicating your agreement to our EULA (https://aka.ms/MinecraftEULA).
eula=true
"@

Write-Utf8NoBom -Path (Join-Path $standaloneDir 'server.properties') -Content @"
allow-flight=false
allow-nether=true
broadcast-console-to-ops=true
broadcast-rcon-to-ops=true
difficulty=easy
enable-command-block=false
enable-jmx-monitoring=false
enable-query=false
enable-rcon=false
enforce-secure-profile=false
enforce-whitelist=false
force-gamemode=false
function-permission-level=2
gamemode=survival
generate-structures=true
generator-settings={}
hardcore=false
hide-online-players=false
level-name=world
level-seed=
level-type=minecraft:normal
max-chained-neighbor-updates=1000000
max-players=20
max-tick-time=60000
max-world-size=29999984
motd=Sihriya standalone release smoke
network-compression-threshold=256
online-mode=false
op-permission-level=4
player-idle-timeout=0
prevent-proxy-connections=false
pvp=true
query.port=$ServerPort
rate-limit=0
rcon.password=
rcon.port=25575
require-resource-pack=false
resource-pack=
resource-pack-id=
resource-pack-prompt=
resource-pack-sha1=
server-ip=
server-port=$ServerPort
simulation-distance=10
spawn-animals=true
spawn-monsters=true
spawn-npcs=true
spawn-protection=16
sync-chunk-writes=true
text-filtering-config=
use-native-transport=true
view-distance=10
white-list=false
"@

$stdoutLog = Join-Path $standaloneDir 'standalone-run.stdout.log'
$stderrLog = Join-Path $standaloneDir 'standalone-run.stderr.log'
$latestLog = Join-Path $standaloneDir 'logs/latest.log'
$launchArgs = @(
    '@user_jvm_args.txt',
    "@libraries/net/minecraftforge/forge/$forgeCoordinate/win_args.txt",
    'nogui'
)

$process = $null
try {
    $process = Start-Process -FilePath 'java' -ArgumentList $launchArgs -WorkingDirectory $standaloneDir -RedirectStandardOutput $stdoutLog -RedirectStandardError $stderrLog -PassThru -WindowStyle Hidden
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    $result = 'timeout'
    $failurePatterns = @(
        'has failed to load correctly',
        'Failed to start the minecraft server',
        'Crash report saved to',
        'Encountered an unexpected exception'
    )

    while ((Get-Date) -lt $deadline) {
        if (Test-Path -LiteralPath $latestLog -PathType Leaf) {
            $logText = Get-Content -LiteralPath $latestLog -Raw
            if ($logText -match 'Done \(' -or $logText -match 'Done!') {
                $result = 'done'
                break
            }
            foreach ($pattern in $failurePatterns) {
                if ($logText -match [regex]::Escape($pattern)) {
                    $result = 'failed'
                    break
                }
            }
            if ($result -eq 'failed') {
                break
            }
        }
        if ($process.HasExited) {
            $result = 'exited'
            break
        }
        Start-Sleep -Seconds 2
        $process.Refresh()
    }

    if ($result -ne 'done') {
        $tail = if (Test-Path -LiteralPath $latestLog -PathType Leaf) {
            (Get-Content -LiteralPath $latestLog -Tail 60) -join [Environment]::NewLine
        } else {
            'latest.log not found'
        }
        throw "Standalone release server smoke failed with state '$result'. Log tail:`n$tail"
    }

    $finalLog = Get-Content -LiteralPath $latestLog -Raw
    foreach ($required in @('Done (', '[statmod/]: STAT Mod ready on server')) {
        if ($finalLog -notmatch [regex]::Escape($required)) {
            throw "Standalone release server smoke log is missing required marker: $required"
        }
    }

    Write-Host "Standalone release server smoke passed in $standaloneDir."
}
finally {
    if ($process -and -not $process.HasExited) {
        Stop-Process -Id $process.Id -Force
        Start-Sleep -Seconds 2
    }
}
