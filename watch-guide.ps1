# Watcher GUIDE-DEV.md
# Lance ce script PowerShell pour surveiller les changements en temps réel.

$watcher = New-Object System.IO.FileSystemWatcher
$watcher.Path = "$PSScriptRoot"
$watcher.Filter = "GUIDE-DEV.md"
$watcher.EnableRaisingEvents = $true

$action = {
    $change = $Event.SourceEventArgs
    $name = $change.Name
    $type = $change.ChangeType
    $time = Get-Date -Format "HH:mm:ss"
    Write-Host "[$time] Fichier modifié : $name ($type)" -ForegroundColor Cyan
    
    # Affiche les dernières lignes modifiées
    $lines = Get-Content -Path $change.FullPath -Tail 5
    Write-Host "--- Dernières lignes ---" -ForegroundColor Yellow
    $lines | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
    Write-Host "------------------------" -ForegroundColor Yellow
    Write-Host "Dis à l'IA de vérifier les changements !" -ForegroundColor Green
}

Register-ObjectEvent $watcher "Changed" -Action $action | Out-Null
Register-ObjectEvent $watcher "Created" -Action $action | Out-Null

Write-Host "🔍 Surveillance de GUIDE-DEV.md activée..." -ForegroundColor Green
Write-Host "Appuie sur Ctrl+C pour arrêter." -ForegroundColor Yellow

while ($true) {
    Start-Sleep -Seconds 1
}
