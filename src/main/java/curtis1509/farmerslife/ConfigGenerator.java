package curtis1509.farmerslife;

import static org.bukkit.Bukkit.getLogger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigGenerator {

  public void generateConfigs() {
    updateOrGenerateConfig("shop", "shop.yml");
    updateOrGenerateConfig("animalCosts", "animal_costs.yml");
    updateOrGenerateConfig("weather", "weather.yml");
  }

  public void updateOrGenerateConfig(String outputName, String templateName) {
    if (tryToUpdateExistingConfiguration(outputName, templateName) == false) {
      generateConfig(outputName, templateName);
    }
  }

  public void generateConfig(String outputName, String templateName) {
    String outputPath = "plugins/FarmersLife/" + outputName + ".yml";
    try (InputStream inputStream = getClass().getResourceAsStream("/yaml_templates/" + templateName)) {
      if (inputStream == null) {
        getLogger().info(
            "The resource for the necessary file " + outputName + " was not found in jar file. Template name: "
                + templateName);
        return;
      }

      StringBuilder yamlContent = new StringBuilder();
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(inputStream));
      String line;

      while ((line = reader.readLine()) != null) {
        yamlContent.append(line).append("\n");
      }

      Path outputFile = Paths.get(outputPath);
      Files.createDirectories(outputFile.getParent());
      Files.write(outputFile, yamlContent.toString().getBytes());
    } catch (IOException e) {
      getLogger().info("There was an error generating files :" + e.getMessage());
    }
  }

  public boolean tryToUpdateExistingConfiguration(String outputName, String templateName) {
    String outputPath = "plugins/FarmersLife/" + outputName + ".yml";

    if (Files.exists(Paths.get(outputPath))) {

      YamlConfiguration existingConfig = YamlConfiguration.loadConfiguration(new File(outputPath));
      YamlConfiguration templateConfig = YamlConfiguration.loadConfiguration(
          new InputStreamReader(getClass().getResourceAsStream("/yaml_templates/" + templateName)));

      // Check and add missing keys
      for (String key : templateConfig.getKeys(true)) {
        if (!existingConfig.contains(key)) {
            existingConfig.set(key, templateConfig.get(key));
            getLogger().info("Added missing key '" + key + "' to the existing config.");
        }
    }

      try {
        existingConfig.save(outputPath);
        getLogger().info("Config file updated successfully.");
        return true;
      } catch (IOException e) {
        getLogger().info("Failed to update config file: " + e.getMessage());
        return false;
      }
    }
    return false;
  }
}