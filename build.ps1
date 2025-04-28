function Write-Log {
    param (
        [string]$Message,
        [string]$Color = "White"
    )
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] $Message" -ForegroundColor $Color
}

Write-Log "Starting Maven build process..." "Cyan"

mvn clean compile package

if ($LASTEXITCODE -eq 0) {
    Write-Log "Build successful! Artifact is ready in the target/ folder." "Green"
} else {
    Write-Log "Build failed. Check logs above." "Red"
    exit 1
}
