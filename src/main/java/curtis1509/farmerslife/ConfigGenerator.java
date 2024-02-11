package curtis1509.farmerslife;

import static org.bukkit.Bukkit.getLogger;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigGenerator {

  public void generateConfigs() {
    generateConfig("shop", "shop_template.yml");
    generateConfig("animalCosts", "animalCosts_template.yml");
    generateConfig("weather", "weather_template.yml");
  }

  public void generateConfig(String outputName, String templateName) {
    // Path where the YAML file will be generated
    String outputPath = "plugins/FarmersLife/" + outputName + ".yml";

    // Get the absolute path
    String absolutePath = Paths.get(outputPath).toAbsolutePath().toString();
    getLogger().info("Checking if file exists: " + absolutePath);

    // Check if the file already exists
    if (Files.exists(Paths.get(outputPath))) {
      getLogger().info("The necessary file " + outputName + "  already exists and won't be generated.");
      return;
    }

    try (InputStream inputStream = getClass().getResourceAsStream("/" + templateName)) {
      if (inputStream == null) {
        getLogger().info("The resource for the necessary file " + outputName + " was not found. Template name: " + templateName);
        return;
      }

      StringBuilder yamlContent = new StringBuilder();
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(inputStream));
      String line;
      while ((line = reader.readLine()) != null) {
        yamlContent.append(line).append("\n");
      }

      // Write YAML content to the output file
      Path outputFile = Paths.get(outputPath);
      Files.createDirectories(outputFile.getParent());
      Files.write(outputFile, yamlContent.toString().getBytes());
    } catch (IOException e) {
      getLogger().info("There was an error generating files :" + e.getMessage());
    }
  }
}