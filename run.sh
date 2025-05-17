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

if [[ "$#" -gt 3 || ( "$1" != "server" && "$1" != "client" && "$1" != "monitor" && "$1" != "broker" ) ]]; then
    log "$RED" "Usage: $0 <server|client|monitor|broker> [server_number: one|two|three]"
    exit 1
else
  if [[ "$1" == "server" ]]; then
      if [[ "$#" -ne 2 || ( "$2" != "one" && "$2" != "two" && "$2" != "three" ) ]]; then
          log "$RED" "Usage: $0 server <one|two|three>"
          exit 1
      fi
      log "$CYAN" "Running server instance number $2 ..."
      java -jar ./"$1"/target/"$1"-3.0.0.jar "$2"
  else
      log "$CYAN" "Running $1 Application..."
      java -jar ./"$1"/target/"$1"-3.0.0.jar
  fi

  EXIT_CODE=$?
  if [[ "$EXIT_CODE" -eq 0 ]]; then
      log "$GREEN" "Application finished successfully with exit code $EXIT_CODE."
  else
      log "$RED" "Application exited with error code $EXIT_CODE."
  fi
fi