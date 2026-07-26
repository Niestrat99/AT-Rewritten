package io.github.niestrat99.advancedteleport.update;

import io.github.niestrat99.advancedteleport.CoreAdvancedTeleport;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class SpigetUpdateChecker implements UpdateChecker {

    private static final String versionURL =
            "https://api.spiget.org/v2/resources/64139/versions/latest";

    public AvailableUpdate getLatestVersion() {
        try {
            JSONObject latestVersionObj = getURLResults(versionURL);
            if (latestVersionObj == null) return null;
            String newVersion = (String) latestVersionObj.get("name");
            // we are a little stupid
            if (newVersion.equals(CoreAdvancedTeleport.getInstance().getPlugin().getDescription().getVersion()))
                return null;
            long latestTimestamp = (long) latestVersionObj.get("releaseDate");
            return new AvailableUpdate(newVersion, ZonedDateTime.ofInstant(Instant.ofEpochSecond(latestTimestamp), ZoneId.of("UTC")));
        } catch (ParseException e) {
            CoreAdvancedTeleport.getInstance().getPlugin().getLogger().severe("Failed to parse update information.");
            e.printStackTrace();
            return null;
        } catch (IOException ex) {
            CoreAdvancedTeleport.getInstance().getPlugin()
                    .getLogger()
                    .severe("Failed to get plugin update information, is Spiget down?");
            return null;
        }
    }

    private static JSONObject getURLResults(String urlStr) throws IOException, ParseException {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", "Niestrat99/AT-Rewritten/" + CoreAdvancedTeleport.getInstance().getPlugin().getDescription().getVersion());
        InputStream inputStream = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream);
        return (JSONObject) new JSONParser().parse(reader);
    }

    @Override
    public String getDownloadLink() {
        return "https://www.spigotmc.org/resources/advancedteleport.64139/";
    }
}
