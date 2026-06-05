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

    [string]$AssetsDir = '',

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

function Test-RuleAllowed {
    param(
        [AllowNull()]
        [object[]]$Rules
    )

    if (-not $Rules -or $Rules.Count -eq 0) {
        return $true
    }

    $allowed = $false
    foreach ($rule in $Rules) {
        $matches = $true
        if ($null -ne $rule.os) {
            if ($rule.os.name -and $rule.os.name -ne 'windows') {
                $matches = $false
            }
            if ($rule.os.arch -and $rule.os.arch -notin @('x86', 'x86_64', 'amd64')) {
                $matches = $false
            }
        }

        if ($matches) {
            if ($rule.action -eq 'allow') {
                $allowed = $true
            } elseif ($rule.action -eq 'disallow') {
                $allowed = $false
            }
        }
    }

    return $allowed
}

function Get-OfficialVersionJson {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Version
    )

    $manifest = Invoke-RestMethod 'https://launchermeta.mojang.com/mc/game/version_manifest_v2.json'
    $baseMetadata = $manifest.versions | Where-Object { $_.id -eq $Version } | Select-Object -First 1
    if (-not $baseMetadata) {
        throw "Could not find official Minecraft metadata for version $Version"
    }
    return Invoke-RestMethod $baseMetadata.url
}

function Add-UniqueRelativePath {
    param(
        [AllowEmptyCollection()]
        [System.Collections.Generic.List[string]]$Target,
        [Parameter(Mandatory = $true)]
        [string]$Value
    )

    $normalized = $Value.Replace('\', '/')
    if (-not $Target.Contains($normalized)) {
        $Target.Add($normalized)
    }
}

function Get-LibraryArtifactPaths {
    param(
        [Parameter(Mandatory = $true)]
        [object[]]$Libraries
    )

    $result = New-Object 'System.Collections.Generic.List[string]'
    foreach ($library in $Libraries) {
        if (-not (Test-RuleAllowed $library.rules)) {
            continue
        }
        if ($null -ne $library.downloads -and $null -ne $library.downloads.artifact -and $library.downloads.artifact.path) {
            Add-UniqueRelativePath -Target $result -Value (Join-Path 'libraries' $library.downloads.artifact.path)
        }
    }
    return $result
}

function Get-WindowsNativeJarPaths {
    param(
        [Parameter(Mandatory = $true)]
        [object[]]$Libraries
    )

    $result = New-Object 'System.Collections.Generic.List[string]'
    foreach ($library in $Libraries) {
        if (-not (Test-RuleAllowed $library.rules)) {
            continue
        }
        if ($null -eq $library.downloads -or $null -eq $library.downloads.classifiers) {
            continue
        }

        $nativeDownload = $library.downloads.classifiers.'natives-windows'
        if ($null -eq $nativeDownload) {
            $nativeDownload = $library.downloads.classifiers.'natives-windows-64'
        }
        if ($null -ne $nativeDownload -and $nativeDownload.path) {
            Add-UniqueRelativePath -Target $result -Value (Join-Path 'libraries' $nativeDownload.path)
        }
    }
    return $result
}

function Ensure-LibraryFile {
    param(
        [Parameter(Mandatory = $true)]
        [string]$RootDir,
        [Parameter(Mandatory = $true)]
        [string]$RelativePath,
        [Parameter(Mandatory = $true)]
        [string]$Url
    )

    $absolutePath = Join-Path $RootDir $RelativePath.Replace('/', '\')
    if (Test-Path -LiteralPath $absolutePath -PathType Leaf) {
        return
    }

    $parentDir = Split-Path -Parent $absolutePath
    New-Item -ItemType Directory -Path $parentDir -Force | Out-Null
    Invoke-WebRequest -Uri $Url -OutFile $absolutePath
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
Require-File -Path (Join-Path $configDir 'epicfight-client.toml') -Label 'Release smoke epicfight-client.toml'

if ([string]::IsNullOrWhiteSpace($AssetsDir)) {
    $AssetsDir = Join-Path $env:USERPROFILE '.gradle/caches/forge_gradle/assets'
}
$assetsDir = (Resolve-Path -LiteralPath $AssetsDir).Path
if (-not (Test-Path -LiteralPath $assetsDir -PathType Container)) {
    throw "Assets directory is missing: $assetsDir"
}

New-Item -ItemType Directory -Path $standaloneDir -Force | Out-Null

$installerDir = Join-Path $standaloneDir '_installer'
New-Item -ItemType Directory -Path $installerDir -Force | Out-Null

$forgeCoordinate = "$MinecraftVersion-$ForgeVersion"
$installerJar = Join-Path $installerDir "forge-$forgeCoordinate-installer.jar"
$installerUrl = "https://maven.minecraftforge.net/net/minecraftforge/forge/$forgeCoordinate/forge-$forgeCoordinate-installer.jar"
$versionJsonPath = Join-Path $standaloneDir "versions/$forgeCoordinate/forge-$forgeCoordinate.json"
if (-not (Test-Path -LiteralPath $versionJsonPath -PathType Leaf)) {
    $versionJsonPath = Join-Path $standaloneDir "versions/$MinecraftVersion-forge-$ForgeVersion/$MinecraftVersion-forge-$ForgeVersion.json"
}
$clientVersionJsonPath = Join-Path $standaloneDir "versions/$MinecraftVersion-forge-$ForgeVersion/$MinecraftVersion-forge-$ForgeVersion.json"

if (-not (Test-Path -LiteralPath $installerJar -PathType Leaf)) {
    Invoke-WebRequest -Uri $installerUrl -OutFile $installerJar
}

if (-not (Test-Path -LiteralPath (Join-Path $standaloneDir 'launcher_profiles.json') -PathType Leaf)) {
    Write-Utf8NoBom -Path (Join-Path $standaloneDir 'launcher_profiles.json') -Content '{"profiles":{},"settings":{},"version":2}'
}

if (-not (Test-Path -LiteralPath $clientVersionJsonPath -PathType Leaf)) {
    & java -jar $installerJar --installClient $standaloneDir | Out-Null
}

Require-File -Path $clientVersionJsonPath -Label 'Standalone Forge client version json'

foreach ($name in @('mods', 'config', 'defaultconfigs', 'logs', 'crash-reports', 'natives', 'assets-link')) {
    $target = Join-Path $standaloneDir $name
    if (Test-Path -LiteralPath $target) {
        Remove-Item -LiteralPath $target -Recurse -Force
    }
}
foreach ($name in @('standalone-client.stdout.log', 'standalone-client.stderr.log', 'standalone-client-args.txt')) {
    $target = Join-Path $standaloneDir $name
    if (Test-Path -LiteralPath $target) {
        Remove-Item -LiteralPath $target -Force
    }
}

Copy-Item -LiteralPath $modsDir -Destination (Join-Path $standaloneDir 'mods') -Recurse -Force
Copy-Item -LiteralPath $configDir -Destination (Join-Path $standaloneDir 'config') -Recurse -Force
if (Test-Path -LiteralPath $defaultConfigsDir -PathType Container) {
    Copy-Item -LiteralPath $defaultConfigsDir -Destination (Join-Path $standaloneDir 'defaultconfigs') -Recurse -Force
}

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

$assetsLink = Join-Path $standaloneDir 'assets-link'
New-Item -ItemType Junction -Path $assetsLink -Target $assetsDir | Out-Null

$officialVersionJson = Get-OfficialVersionJson -Version $MinecraftVersion
$forgeVersionJson = Get-Content -LiteralPath $clientVersionJsonPath -Raw | ConvertFrom-Json

foreach ($library in $officialVersionJson.libraries) {
    if (-not (Test-RuleAllowed $library.rules)) {
        continue
    }

    if ($null -ne $library.downloads -and $null -ne $library.downloads.artifact -and $library.downloads.artifact.path) {
        Ensure-LibraryFile -RootDir $standaloneDir -RelativePath (Join-Path 'libraries' $library.downloads.artifact.path) -Url $library.downloads.artifact.url
    }

    if ($null -ne $library.downloads -and $null -ne $library.downloads.classifiers) {
        $nativeDownload = $library.downloads.classifiers.'natives-windows'
        if ($null -eq $nativeDownload) {
            $nativeDownload = $library.downloads.classifiers.'natives-windows-64'
        }
        if ($null -ne $nativeDownload -and $nativeDownload.path) {
            Ensure-LibraryFile -RootDir $standaloneDir -RelativePath (Join-Path 'libraries' $nativeDownload.path) -Url $nativeDownload.url
        }
    }
}

$legacyClassPath = New-Object 'System.Collections.Generic.List[string]'
foreach ($path in (Get-LibraryArtifactPaths -Libraries $forgeVersionJson.libraries)) {
    Add-UniqueRelativePath -Target $legacyClassPath -Value $path
}

$extraClientRuntimePaths = @(
    "libraries/net/minecraftforge/fmlcore/$MinecraftVersion-$ForgeVersion/fmlcore-$MinecraftVersion-$ForgeVersion.jar",
    "libraries/net/minecraftforge/javafmllanguage/$MinecraftVersion-$ForgeVersion/javafmllanguage-$MinecraftVersion-$ForgeVersion.jar",
    "libraries/net/minecraftforge/lowcodelanguage/$MinecraftVersion-$ForgeVersion/lowcodelanguage-$MinecraftVersion-$ForgeVersion.jar",
    "libraries/net/minecraftforge/mclanguage/$MinecraftVersion-$ForgeVersion/mclanguage-$MinecraftVersion-$ForgeVersion.jar",
    "libraries/net/minecraftforge/forge/$MinecraftVersion-$ForgeVersion/forge-$MinecraftVersion-$ForgeVersion-universal.jar",
    "libraries/net/minecraftforge/forge/$MinecraftVersion-$ForgeVersion/forge-$MinecraftVersion-$ForgeVersion-client.jar",
    "libraries/net/minecraft/client/$MinecraftVersion-$($forgeVersionJson.arguments.game[-1])/client-$MinecraftVersion-$($forgeVersionJson.arguments.game[-1])-extra.jar"
)
foreach ($path in $extraClientRuntimePaths) {
    Add-UniqueRelativePath -Target $legacyClassPath -Value $path
}
foreach ($path in (Get-LibraryArtifactPaths -Libraries $officialVersionJson.libraries)) {
    Add-UniqueRelativePath -Target $legacyClassPath -Value $path
}

$missingRuntimeEntries = @()
foreach ($entry in $legacyClassPath) {
    if (-not (Test-Path -LiteralPath (Join-Path $standaloneDir $entry.Replace('/', '\')) -PathType Leaf)) {
        $missingRuntimeEntries += $entry
    }
}
if ($missingRuntimeEntries.Count -gt 0) {
    throw "Standalone release client smoke is missing runtime libraries:$([Environment]::NewLine) - $($missingRuntimeEntries -join ([Environment]::NewLine + ' - '))"
}

$nativeJarPaths = Get-WindowsNativeJarPaths -Libraries $officialVersionJson.libraries
$nativesDir = Join-Path $standaloneDir 'natives'
New-Item -ItemType Directory -Path $nativesDir -Force | Out-Null
Add-Type -AssemblyName System.IO.Compression.FileSystem
foreach ($nativeJar in $nativeJarPaths) {
    $absoluteNativeJar = Join-Path $standaloneDir $nativeJar.Replace('/', '\')
    if (-not (Test-Path -LiteralPath $absoluteNativeJar -PathType Leaf)) {
        throw "Missing Windows native jar for standalone client smoke: $absoluteNativeJar"
    }
    $zip = [System.IO.Compression.ZipFile]::OpenRead($absoluteNativeJar)
    try {
        foreach ($entry in $zip.Entries) {
            if ($entry.FullName.EndsWith('/')) {
                continue
            }
            if ($entry.FullName -like 'META-INF/*') {
                continue
            }
            $target = Join-Path $nativesDir ([System.IO.Path]::GetFileName($entry.FullName))
            [System.IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $target, $true)
        }
    }
    finally {
        $zip.Dispose()
    }
}

$argsFile = Join-Path $standaloneDir 'standalone-client-args.txt'
$mcpVersion = $forgeVersionJson.arguments.game[-1]
$legacyClassPathValue = $legacyClassPath -join ';'
$argsLines = @(
    "-p libraries/cpw/mods/bootstraplauncher/1.1.2/bootstraplauncher-1.1.2.jar;libraries/cpw/mods/securejarhandler/2.1.10/securejarhandler-2.1.10.jar;libraries/org/ow2/asm/asm-commons/9.9.1/asm-commons-9.9.1.jar;libraries/org/ow2/asm/asm-util/9.9.1/asm-util-9.9.1.jar;libraries/org/ow2/asm/asm-analysis/9.9.1/asm-analysis-9.9.1.jar;libraries/org/ow2/asm/asm-tree/9.9.1/asm-tree-9.9.1.jar;libraries/org/ow2/asm/asm/9.9.1/asm-9.9.1.jar;libraries/net/minecraftforge/JarJarFileSystems/0.3.19/JarJarFileSystems-0.3.19.jar",
    '--add-modules ALL-MODULE-PATH',
    '--add-opens java.base/java.util.jar=cpw.mods.securejarhandler',
    '--add-opens java.base/java.lang.invoke=cpw.mods.securejarhandler',
    '--add-exports java.base/sun.security.util=cpw.mods.securejarhandler',
    '--add-exports jdk.naming.dns/com.sun.jndi.dns=java.naming',
    '-Djava.net.preferIPv6Addresses=system',
    '-DmergeModules=jna-5.10.0.jar,jna-platform-5.10.0.jar',
    '-Djava.library.path=natives',
    '-Djna.tmpdir=natives',
    '-Dorg.lwjgl.system.SharedLibraryExtractPath=natives',
    '-Dio.netty.native.workdir=natives',
    "-DignoreList=bootstraplauncher-1.1.2.jar,securejarhandler-2.1.10.jar,asm-commons-9.9.1.jar,asm-util-9.9.1.jar,asm-analysis-9.9.1.jar,asm-tree-9.9.1.jar,asm-9.9.1.jar,JarJarFileSystems-0.3.19.jar,client-extra,fmlcore,javafmllanguage,lowcodelanguage,mclanguage,forge-,$MinecraftVersion-forge-$ForgeVersion.jar",
    '-DlibraryDirectory=libraries',
    "-DlegacyClassPath=$legacyClassPathValue",
    'cpw.mods.bootstraplauncher.BootstrapLauncher',
    '--username Dev',
    "--version $MinecraftVersion-forge-$ForgeVersion",
    '--gameDir .',
    '--assetsDir assets-link',
    "--assetIndex $($officialVersionJson.assetIndex.id)",
    '--uuid 00000000-0000-0000-0000-000000000000',
    '--accessToken 0',
    '--userType legacy',
    '--versionType release',
    '--launchTarget forgeclient',
    "--fml.forgeVersion $ForgeVersion",
    "--fml.mcVersion $MinecraftVersion",
    '--fml.forgeGroup net.minecraftforge',
    "--fml.mcpVersion $mcpVersion"
)
[System.IO.File]::WriteAllLines($argsFile, $argsLines, (New-Object System.Text.UTF8Encoding($false)))

$stdoutLog = Join-Path $standaloneDir 'standalone-client.stdout.log'
$stderrLog = Join-Path $standaloneDir 'standalone-client.stderr.log'
$latestLog = Join-Path $standaloneDir 'logs/latest.log'
$process = $null
try {
    $process = Start-Process -FilePath 'java' -ArgumentList '@standalone-client-args.txt' -WorkingDirectory $standaloneDir -RedirectStandardOutput $stdoutLog -RedirectStandardError $stderrLog -PassThru -WindowStyle Hidden
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    $result = 'timeout'
    $requiredMarkers = @(
        'Setting user: Dev',
        '[statmod/]: STAT Mod loaded!',
        '[sihriya/]: Sihriya chargé avec Epic Fight + STAT Mod !',
        'OpenAL initialized'
    )
    $failurePatterns = @(
        'has failed to load correctly',
        'Failed to start the minecraft client',
        'Crash report saved to',
        'Encountered an unexpected exception',
        'Missing textures in model',
        'Invalid path in pack',
        'Datapack animation reading failed'
    )

    while ((Get-Date) -lt $deadline) {
        if (Test-Path -LiteralPath $latestLog -PathType Leaf) {
            $logText = Get-Content -LiteralPath $latestLog -Raw
            $missingMarkers = $requiredMarkers | Where-Object { $logText -notmatch [regex]::Escape($_) }
            if ($missingMarkers.Count -eq 0) {
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
        $stdoutTail = if (Test-Path -LiteralPath $stdoutLog -PathType Leaf) {
            (Get-Content -LiteralPath $stdoutLog -Tail 80) -join [Environment]::NewLine
        } else {
            'stdout log not found'
        }
        $stderrTail = if (Test-Path -LiteralPath $stderrLog -PathType Leaf) {
            (Get-Content -LiteralPath $stderrLog -Tail 80) -join [Environment]::NewLine
        } else {
            'stderr log not found'
        }
        $latestTail = if (Test-Path -LiteralPath $latestLog -PathType Leaf) {
            (Get-Content -LiteralPath $latestLog -Tail 120) -join [Environment]::NewLine
        } else {
            'latest.log not found'
        }
        throw "Standalone release client smoke failed with state '$result'. stderr:`n$stderrTail`nstdout:`n$stdoutTail`nlatest.log:`n$latestTail"
    }

    Write-Host "Standalone release client smoke passed in $standaloneDir."
}
finally {
    if ($process -and -not $process.HasExited) {
        Stop-Process -Id $process.Id -Force
        Start-Sleep -Seconds 2
    }
}
