package io.github.niestrat99.advancedteleport.config;

import io.github.niestrat99.advancedteleport.CoreClass;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.*;

/**
 * ConfigurationMaster
 *
 * This makes it easier to update and manage configurations.
 * It includes commenting and a title, all updated dynamically.
 */
public abstract class ConfigurationMaster {

    private FileConfiguration config;
    private File configFile;
    private HashMap<String, String> comments;
    private List<String> currentLines;
    private HashMap<String, String> sections;
    private List<String> nodeOrder;

    /**
     *
     * @param name
     */
    public ConfigurationMaster(String name) {
        // Creates the config file object
        configFile = new File(CoreClass.getInstance().getDataFolder(), name + ".yml");
        // If it doesn't exist though, create it
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        //
        config = YamlConfiguration.loadConfiguration(configFile);
        currentLines = new ArrayList<>();
        comments = new HashMap<>();
        sections = new HashMap<>();

        loadDefaults();
        moveToNew();
        config.options().copyDefaults(true);
        save(true);
        postSave();
        loadTitle();
        writeSections();
        writeComments();
        save(false);

    }

    public void loadTitle() {
        List<String> title = new ArrayList<>(Arrays.asList(
                "################################################################################",
                "#                         -<( Advanced Teleport )>-                            #",
                "#                                    - Made by Niestrat99 and Thatsmusic99     #",
                "#                                                                              #",
                "################################################################################",
                "#  A rapidly growing teleportation plugin looking to break the boundaries of   #",
                "#  traditional teleport plugins.                                               #",
                "#                                                                              #",
                "#  Spigot page - https://www.spigotmc.org/resources/advanced-teleport.64139/   #",
                "#  Wiki - https://github.com/Niestrat99/AT-Rewritten/wiki                      #",
                "#  Discord - https://discord.gg/mgWbbN4                                        #",
                "################################################################################",
                ""

        ));
        for (int i = 0; i < title.size(); i++) {
            currentLines.add(i, title.get(i));
        }
    }

    public abstract void loadDefaults();

    public void addDefault(String path, Object value) {
        config.addDefault(path, value);
        nodeOrder.add(path);
    }

    public void addDefault(String path, Object value, String section, String comment) {
        addDefault(path, value);
        addComment(path, comment);
        addSection(path, section);
    }

    public void addDefault(String path, Object value, String comment) {
        addDefault(path, value);
        addComment(path, comment);
    }

    public void addComment(String path, String comment) {
        comments.put(path, comment);
    }

    public void addSection(String beforePath, String section) {
        sections.put(beforePath, section);
    }

    public abstract void postSave();

    public void moveToNew() {}

    public void moveTo(String oldPath, String newPath) {
        Object object = config.get(oldPath);
        config.set(newPath, object);
        config.set(oldPath, null);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void writeComments() {
        // For each comment to be made...
        for (String path : comments.keySet()) {
            // Get all the divisions made in the config
            String[] divisions = path.split("\\.");

            writeComment(path, divisions, 0, 0);
        }
    }

    private void writeComment(String path, String[] divisions, int iteration, int startingLine) {
        StringBuilder indent = new StringBuilder();
        for (int j = 0; j < iteration; j++) {
            indent.append("  ");
        }
        // Go through each line in the file
        for (int i = startingLine; i < currentLines.size(); i++) {
            String line = currentLines.get(i);
            if (!line.startsWith(indent.toString())) return;
            if (line.startsWith("#")) continue;
            if (line.startsWith(indent.toString() + divisions[iteration]) ||
                    line.startsWith(indent.toString() + "'" + divisions[iteration] + "'")) {
                iteration += 1;
                if (iteration == divisions.length) {
                    if (iteration == 1) {
                        currentLines.add("");
                    }
                    int currentLine = i;
                    String[] rawComment = comments.get(path).split("\n");
                    for (String commentPart : rawComment) {
                        currentLines.add(currentLine, indent + "# " + commentPart);
                        currentLine++;
                    }
                    break;
                } else {
                    writeComment(path, divisions, iteration, i + 1);
                }
            }
        }
    }

    public void writeSections() {
        // For each path the section is to be written above...
        for (String path : sections.keySet()) {
            // For each line in the file currently...
            for (int i = 0; i < currentLines.size(); i++) {
                if (currentLines.get(i).startsWith(path)) {
                    String section = sections.get(path);
                    StringBuilder length = new StringBuilder();
                    length.append("###");
                    for (int j = 0; j < section.length(); j++) {
                        length.append("#");
                    }
                    length.append("###");
                    currentLines.add(i, "");
                    currentLines.add(i, length.toString());
                    currentLines.add(i, "#  " + section + "  #");
                    currentLines.add(i, length.toString());
                    currentLines.add(i, "");
                    break;
                }
            }
        }
    }

    // Get order of nodes
    // Go through each line
    // If not in correct position, move to correct location

    public void rearrangeNodes() {
        // For each
        int size = nodeOrder.size();
        int overhead = 0;
        List<String> overheadParts = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String path = nodeOrder.get(i + overhead);
            String[] parts = path.split("\\.");
            
            overhead += path.split("\\.").length - 1;

        }
    }

    public void checkNode(String path, int deepness, int currentLocation) {

    }


    public void save(boolean isConfig) {
        try {
            if (isConfig) {
                config.save(configFile);
                BufferedReader reader = new BufferedReader(new FileReader(configFile));
                String currentLine;
                while ((currentLine = reader.readLine()) != null) {
                    if (currentLine.startsWith("#")) continue;
                    currentLines.add(currentLine);
                }
                reader.close();
            } else {
                // Opens up a new file writer
                FileWriter writer = new FileWriter(configFile);
                // For each line to write...
                for (String line : currentLines) {
                    // Write that and add in a break.
                    writer.write(line);
                    writer.write("\n");
                }
                // Close the writer.
                writer.close();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
