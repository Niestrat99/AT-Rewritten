if [[ -z "${SERVER_FILE}" ]]; then
  echo SERVER_FILE variable is not defined.
  return
fi

echo Copying the AdvancedTeleport jar file to the server...

copy() {
  mv AdvancedTeleport-Bukkit* "${SERVER_FILE}"
}

cd AdvancedTeleport-Bukkit || return
copy

