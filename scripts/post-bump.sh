
echo -e \"group=io.github.niestrat99\nversion=$1\" > gradle.properties
git add gradle.properties
git commit -m "build: Update version to v$1"
git tag -a v$1 -m "Version v$1"
git push origin v$1