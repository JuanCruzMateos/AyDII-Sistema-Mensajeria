# Define colors for logging
$GREEN = "Green"
$RED = "Red"
$CYAN = "Cyan"

# Function to log messages with a timestamp and color
function Log {
    param (
        [string]$Color,
        [string]$Message
    )
    $Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$Timestamp] $Message" -ForegroundColor $Color
}

# Check if the correct number of arguments is provided
if ($args.Count -ne 1 -and $args.Count -ne 2) {
	Log $RED "Usage: .\run.ps1 <server|client|monitor|broker> [server_number: one|two|three]"
    exit 1
}

# Validate the argument
if ($args[0] -eq "server"){
	if ($args.Count -ne 2 -or ($args[1] -ne "one" -and $args[1] -ne "two" -and $args[1] -ne "three")){
		Log $RED "Usage: .\run.ps1 server <one|two|three>"
		exit 1
	}
	# Run server
	log $CYAN "Running server instance number $($args[1]) ..."
    java -jar ./$($args[0])/target/$($args[0])-4.0.0.jar "$($args[1])"
}
else{
	if ($args[0] -ne "client" -and $args[0] -ne "monitor" -and $args[0] -ne "broker"){
		Log $RED "Invalid argument. Please specify 'server', 'client', 'monitor' or 'broker'."
		exit 1
	}
	else{
		# Run the specified application
		Log $CYAN "Running $($args[0]) Application..."
		java -jar "./$($args[0])/target/$($args[0])-4.0.0.jar"
		$ExitCode = $LASTEXITCODE
	}
}

# Check the exit code and log the result
if ($ExitCode -eq 0) {
    Log $GREEN "Application finished successfully with exit code $ExitCode."
} else {
    Log $RED "Application exited with error code $ExitCode."
}