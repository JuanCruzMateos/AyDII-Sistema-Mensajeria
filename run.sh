#!/bin/bash

GREEN='\033[0;32m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m'

log() {
  local COLOR=$1
  local MESSAGE=$2
  local TIMESTAMP
  TIMESTAMP=$(date +"%Y-%m-%d %H:%M:%S")
  echo -e "[${TIMESTAMP}] ${COLOR}${MESSAGE}${NC}"
}

if [[ $# -ne 1 ]]; then
    log "$RED" "Usage: $0 <server|client|monitor|broker>"
    exit 1
elif [[ $1 != "server" && $1 != "client" && $1 != "monitor" && $1 != "broker" ]]; then
    log "$RED" "Invalid argument. Please specify 'server', 'client', 'monitor' or 'broker'."
    exit 1
else
    log "$CYAN" "Running $1 Application..."
    java -jar ./"$1"/target/"$1"-3.0.0.jar
    EXIT_CODE=$?

    if [[ $EXIT_CODE -eq 0 ]]; then
      log "$GREEN" "Application finished successfully with exit code $EXIT_CODE."
    else
      log "$RED" "Application exited with error code $EXIT_CODE."
    fi
fi

