
# Get the current layout of the file
PROPERTIES_CONTENT=$(cat gradle.properties)

# Replace the current version with the new version, and write to the file again
echo "${PROPERTIES_CONTENT/$1/$2}" > gradle.properties

exit(0)

