#!/bin/bash

OUTPUT_FILE="merged_code.txt"
SRC_DIR="app/src/main"

# Start fresh
rm -f "$OUTPUT_FILE"
touch "$OUTPUT_FILE"

# Find source files (.kt, .java, .xml, AndroidManifest.xml)
find "$SRC_DIR" -type f \( -name "*.kt" -o -name "*.java" -o -name "*.xml" -o -name "AndroidManifest.xml" \) | while read -r file; do
    echo "=========================== $file ===========================" >> "$OUTPUT_FILE"
    cat "$file" >> "$OUTPUT_FILE"
    echo -e "\n\n" >> "$OUTPUT_FILE"
done

# Optional: add Gradle files
for config_file in build.gradle.kts settings.gradle.kts gradle.properties app/build.gradle.kts app/proguard-rules.pro; do
    if [[ -f "$config_file" ]]; then
        echo "=========================== $config_file ===========================" >> "$OUTPUT_FILE"
        cat "$config_file" >> "$OUTPUT_FILE"
        echo -e "\n\n" >> "$OUTPUT_FILE"
    fi
done

echo "✅ All code merged into: $OUTPUT_FILE"