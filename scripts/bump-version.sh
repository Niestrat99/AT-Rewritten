version=${1:`cog bump --dry-run --auto`}

if [$! ~= 0] then

  exit 1
fi

current_version=`git describe --tags --abbrev=0`

echo Proposed version to update from $current_version to $version

