# Define colors for logging
$GREEN = "`e[0;32m"
$RED = "`e[0;31m"
$CYAN = "`e[0;36m"
$NC = "`e[0m"

# Function to log messages with a timestamp and color
function Log {
    param (
        [string]$Color,
        [string]$Message
    )
    $Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$Timestamp] $Color$Message$NC"
}

# Check if the correct number of arguments is provided
if ($args.Count -ne 1) {
    Log $RED "Usage: .\run.ps1 <server|client>"
    exit 1
}

# Validate the argument
if ($args[0] -ne "server" -and $args[0] -ne "client") {
    Log $RED "Invalid argument. Please specify 'server' or 'client'."
    exit 1
}

# Run the specified application
Log $CYAN "Running $($args[0]) Application..."
java -jar "./$($args[0])/target/$($args[0])-2.0.0.jar"
$ExitCode = $LASTEXITCODE

# Check the exit code and log the result
if ($ExitCode -eq 0) {
    Log $GREEN "Application finished successfully with exit code $ExitCode."
} else {
    Log $RED "Application exited with error code $ExitCode."
}