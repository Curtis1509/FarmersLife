package curtis1509.farmerslife;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigGenerator {

  public void generateConfigs() {
    generateConfig("shop", "shop_template.yml");
    generateConfig("animalCosts", "animalCosts_template.yml");
  }

  public void generateConfig(String outputName, String templateName) {
    // Path where the YAML file will be generated
    String outputPath = "/FarmersLife/" + outputName + ".yml";

    // Check if the file already exists
    if (Files.exists(Paths.get(outputPath))) {
      System.out.println("YAML file already exists at: " + outputPath);
      return;
    }

    // Read YAML template from resources
    try (
      InputStream inputStream = ConfigGenerator.class.getResourceAsStream(
          templateName
        )
    ) {
      if (inputStream == null) {
        System.out.println("Error: Template file not found in resources.");
        return;
      }

      StringBuilder yamlContent = new StringBuilder();
      BufferedReader reader = new BufferedReader(
        new InputStreamReader(inputStream)
      );
      String line;
      while ((line = reader.readLine()) != null) {
        yamlContent.append(line).append("\n");
      }

      // Write YAML content to the output file
      Path outputFile = Paths.get(outputPath);
      Files.createDirectories(outputFile.getParent());
      Files.write(outputFile, yamlContent.toString().getBytes());
      System.out.println("YAML file generated successfully at: " + outputPath);
    } catch (IOException e) {
      System.out.println(
        "An error occurred while generating the YAML file: " + e.getMessage()
      );
    }
  }
}
