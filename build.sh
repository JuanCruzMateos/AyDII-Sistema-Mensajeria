#!/bin/bash

echo "Starting Maven build process..."

mvn clean compile package

if [ $? -eq 0 ]; then
  echo "Build successful! Artifact is ready in the target/ folder."
else
  echo "Build failed. Check logs above."
fi
