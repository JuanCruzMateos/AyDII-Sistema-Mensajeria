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

log "$CYAN" "Starting Maven build process..."

if mvn clean compile package; then
  log "$GREEN" "Build successful! Artifact is ready in the target/ folder."
else
  log "$RED" "Build failed. Check logs above."
  exit 1
fi
