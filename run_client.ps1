function Write-Log {
    param (
        [string]$Message,
        [string]$Color = "White"
    )
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] $Message" -ForegroundColor $Color
}

Write-Log "Running Client Application..." "Cyan"

# Run the application
java -jar ./client/target/client-2.0.0.jar
$exitCode = $LASTEXITCODE

if ($exitCode -eq 0) {
    Write-Log "Application finished successfully with exit code $exitCode." "Green"
} else {
    Write-Log "Application exited with error code $exitCode." "Red"
}
