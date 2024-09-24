#!/bin/bash

# Set the module name and output directory
MODULE_NAME="idlibrary"  # Replace with your library module's name
OUTPUT_DIR="./Releases"

# Create the output directory if it doesn't exist
mkdir -p $OUTPUT_DIR

# Clean the project and build the AAR file
echo "Cleaning project..."
./gradlew clean

echo "Building AAR..."
./gradlew :$MODULE_NAME:assembleRelease

# Move the AAR file to the output directory
AAR_PATH="./$MODULE_NAME/build/outputs/aar/${MODULE_NAME}-release.aar"
if [ -f "$AAR_PATH" ]; then
    mv $AAR_PATH $OUTPUT_DIR/
    echo "AAR file successfully built and moved to $OUTPUT_DIR"
else
    echo "AAR file was not found. Build might have failed."
fi
