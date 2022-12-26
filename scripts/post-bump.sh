
echo -e \"group=io.github.niestrat99\nversion=$1\" > gradle.properties
git add gradle.properties
git commit -m "build: Update version to $1"
git push
git push origin $1