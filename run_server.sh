#!/bin/bash

GREEN='\033[0;32m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

log() {
  local COLOR=$1
  local MESSAGE=$2
  local TIMESTAMP
  TIMESTAMP=$(date +"%Y-%m-%d %H:%M:%S")
  echo -e "[${TIMESTAMP}] ${COLOR}${MESSAGE}${NC}"
}

log "$CYAN" "Running Server Application..."

# Run the application
java -jar ./server/target/server-2.0.0.jar
EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
  log "$GREEN" "Application finished successfully with exit code $EXIT_CODE."
else
  log "$RED" "Application exited with error code $EXIT_CODE."
fi
