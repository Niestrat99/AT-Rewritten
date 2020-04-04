package io.github.at;

import io.github.at.main.CoreClass;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class UpdateChecker {

    private final static String versionURL = "https://api.spiget.org/v2/resources/64139/versions?size=1000";
    private final static String descriptionURL = "https://api.spiget.org/v2/resources/64139/updates?size=1000";

    public static Object[] getUpdate() {
        CoreClass core = CoreClass.getInstance();
        try {
            JSONArray versionsArray = getURLResults(versionURL);
            if (versionsArray != null) {
                int size = Objects.requireNonNull(versionsArray).size();
                String lastVersion = ((JSONObject) versionsArray.get(size - 1)).get("name").toString();
                String currentVersion = core.getDescription().getVersion();
                if (!lastVersion.equals(currentVersion)) {
                    JSONArray updatesArray = getURLResults(descriptionURL);
                    if (updatesArray != null) {
                        int updateSize = updatesArray.size();
                        String updateName = ((JSONObject) updatesArray.get(updateSize - 1)).get("title").toString();
                        return new Object[]{lastVersion, updateName};
                    }
                }
            }

        } catch (ParseException | IOException e) {
            e.printStackTrace();
            return null;

        }
        return null;
    }

    private static JSONArray getURLResults(String urlStr) throws IOException, ParseException {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", "AdvancedTeleportPA");
        InputStream inputStream = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream);
        return (JSONArray) new JSONParser().parse(reader);
    }
}
