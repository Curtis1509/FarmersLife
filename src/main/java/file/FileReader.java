package file;

import static curtis1509.farmerslife.FarmersLife.*;
import static curtis1509.farmerslife.Functions.*;
import static org.bukkit.Bukkit.getLogger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import org.apache.commons.lang.ObjectUtils.Null;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import curtis1509.farmerslife.DepositCrop;
import curtis1509.farmerslife.Pen;
import curtis1509.farmerslife.Player;
import curtis1509.farmerslife.box.DepositBox;
import curtis1509.farmerslife.box.ShipmentBox;

public class FileReader {

  HashMap<String, Double> stats = new HashMap<>();

  // YAML File Objects
  File playersFile = new File("plugins/FarmersLife/players.yml");
  File depositsFile = new File("plugins/FarmersLife/deposits.yml");
  File statsFile = new File("plugins/FarmersLife/stats.yml");
  File weatherFile = new File("plugins/FarmersLife/weather.yml");
  File shopFile = new File("plugins/FarmersLife/shop.yml");
  File animalCostFile = new File("plugins/FarmersLife/animalCosts.yml");

  // YAML Configuration Objects
  FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playersFile);
  FileConfiguration statsConfig = YamlConfiguration.loadConfiguration(statsFile);
  FileConfiguration shopConfig = YamlConfiguration.loadConfiguration(shopFile);
  FileConfiguration animalCostConfig = YamlConfiguration.loadConfiguration(animalCostFile);
  FileConfiguration weatherConfig = YamlConfiguration.loadConfiguration(weatherFile);
  FileConfiguration depositConfig = YamlConfiguration.loadConfiguration(depositsFile);

  public void FileProcessNewDay() {
    savePlayers();
    saveDeposits();
    savePens();
    createBackup();
  }

  public void FileProcessReloadShop(boolean includeDepositShop) {
    loadBuyShop();
    loadAnimalCosts();
    if (includeDepositShop) {
      loadDepositShop();
    }
    loadGeneralStore();
  }

  public void FileProcessEnablePlugin() {
    CreateFile();
    loadDepositShop();
    loadDeposits();
    loadGeneralStore();
    loadPens();
    loadNametags();
    loadAnimalCosts();
  }

  public void FileProcessDisablePlugin() {
    savePlayers();
    saveDeposits();
    savePens();
  }

  public void throwFileError(IOException exception, String fileName) {
    System.out.println("There was an error whilst saving " + fileName);
    getLogger().info(exception.getMessage());
  }

  // MARKED FOR REWORK
  public void CreateFile() {
    try {
      Files.createDirectories(Paths.get("plugins/FarmersLife"));
      playersFile.createNewFile();
      depositsFile.createNewFile();
      statsFile.createNewFile();
      // weatherFile.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void createBackup() {
    try {
      Files.createDirectories(Paths.get("plugins/FarmersLife/Backups"));
      String date = Calendar.getInstance().getTime().getSeconds() + "_secs_"
          + Calendar.getInstance().getTime().getHours() + "_hours_" + Calendar.getInstance().getTime().getMinutes()
          + "mins_" + Calendar.getInstance().getTime().getDay() + "_" + Calendar.getInstance().getTime().getMonth()
          + "_" + Calendar.getInstance().getTime().getYear();

      File playersFileBackup = new File("plugins/FarmersLife/Backups/players" + date + ".yml");
      Files.copy(playersFile.toPath(), playersFileBackup.toPath(), StandardCopyOption.COPY_ATTRIBUTES);

      File depositsFileBackup = new File("plugins/FarmersLife/Backups/deposits" + date + ".yml");
      Files.copy(depositsFile.toPath(), depositsFileBackup.toPath(), StandardCopyOption.COPY_ATTRIBUTES);

      File statsFileBackup = new File("plugins/FarmersLife/Backups/stats" + date + ".yml");
      Files.copy(statsFile.toPath(), statsFileBackup.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
    } catch (IOException exception) {
      System.out.println("There was an error whilst creating the backup. File likely exists already");
    }
  }

  public void addStat(String material, double stat) throws IOException {
    if (stats.containsKey(material)) {
      stats.replace(material, stats.get(material) + stat);
    } else
      stats.put(material, stat);
  }

  public FileConfiguration getWeatherFileConfiguration() {
    return weatherConfig;
  }

  public void saveWeatherFile(FileConfiguration configuration) {
    try {
      configuration.save(weatherFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void saveStats(long day, String playerName) throws IOException {
    if (!stats.isEmpty()) {
      int total = 0;

      for (Map.Entry<String, Double> entry : stats.entrySet()) {
        String key = entry.getKey();
        double value = entry.getValue();
        total += value;
        statsConfig.set(day + "." + playerName + "." + key, "$" + value);
      }
      statsConfig.set(day + "." + playerName + "." + "total", "$" + total);
      double multiplier = 0;
      for (Player p : players) {
        if (p.getName().equals(playerName))
          multiplier = p.getSkills().skillProfits.getMultiplier();
      }
      statsConfig.set(day + "." + playerName + "." + "multiplier", "x" + multiplier);
      statsConfig.save(statsFile);
      stats.clear();

      for (Player p : players) {
        if (p.getName().equals(playerName))
          p.addToTodaysCash(total * multiplier);
      }
    }
  }

  public void removeDepositData(DepositBox depositBox) throws IOException {
    String id = "deposits." + depositBox.getID();
    if (depositConfig.contains(id)) {
      depositConfig.set(id, null);
    }
    depositConfig.save(depositsFile);
    depositBoxes.remove(depositBox);
  }

  public void removePenData(Pen pen) throws IOException {
    FileConfiguration depositConfig = YamlConfiguration.loadConfiguration(depositsFile);
    String id = "pens." + pen.getId();
    if (depositConfig.contains(id)) {
      depositConfig.set(id, null);
    }
    depositConfig.save(depositsFile);
    pens.remove(pen);
  }

  public void removeNametagData(String nametag) throws IOException {
    FileConfiguration depositConfig = YamlConfiguration.loadConfiguration(depositsFile);
    String id = "nametags." + nametag;
    if (depositConfig.contains(id)) {
      depositConfig.set(id, null);
    }
    depositConfig.save(depositsFile);
    animalNames.remove(nametag);
  }

  public void saveDeposits() {
    FileConfiguration depositConfig = YamlConfiguration.loadConfiguration(depositsFile);
    for (DepositBox deposits : depositBoxes) {
      String id = "deposits." + deposits.getID();
      depositConfig.set(id + ".owner", deposits.getOwner());
      depositConfig.set(id + ".x", deposits.getBox().getLocation().getBlockX());
      depositConfig.set(id + ".y", deposits.getBox().getLocation().getBlockY());
      depositConfig.set(id + ".z", deposits.getBox().getLocation().getBlockZ());
    }

    for (ShipmentBox shipments : shipmentBoxes) {
      String id = "shipments." + shipments.getID();
      depositConfig.set(id + ".owner", shipments.getOwner());
      depositConfig.set(id + ".x", shipments.getBox().getLocation().getBlockX());
      depositConfig.set(id + ".y", shipments.getBox().getLocation().getBlockY());
      depositConfig.set(id + ".z", shipments.getBox().getLocation().getBlockZ());
    }

    try {
      depositConfig.save(depositsFile);
    } catch (IOException exception) {
      throwFileError(exception, "deposits");
    }
  }

  public void savePens() {
    FileConfiguration depositConfig = YamlConfiguration.loadConfiguration(depositsFile);
    for (Pen pen : pens) {
      String id = "pens." + pen.getId();
      depositConfig.set(id + ".owner", pen.getOwner());
      depositConfig.set(id + ".a.x", pen.getPointA().getBlockX());
      depositConfig.set(id + ".a.y", pen.getPointA().getBlockY());
      depositConfig.set(id + ".a.z", pen.getPointA().getBlockZ());

      depositConfig.set(id + ".b.x", pen.getPointB().getBlockX());
      depositConfig.set(id + ".b.y", pen.getPointB().getBlockY());
      depositConfig.set(id + ".b.z", pen.getPointB().getBlockZ());
    }

    for (String key : animalNames.keySet()) {
      String id = "nametags." + key;
      depositConfig.set(id + ".days", animalNames.get(key));
    }

    try {
      depositConfig.save(depositsFile);
    } catch (IOException exception) {
      throwFileError(exception, "deposits");
    }
  }

  public void savePlayers() {
    try {
      for (Player player : players) {
        playerConfig.set(player.getName() + ".cash", economy.getBalance(player.getPlayer()));
        double previousCash = playerConfig.getDouble("alltimecash");
        playerConfig.set(player.getName() + ".alltimecash", economy.getBalance(player.getPlayer()) + previousCash);
        playerConfig.set(player.getName() + ".profit", player.getSkills().skillProfits.getLevel());
        playerConfig.set(player.getName() + ".protection", player.getSkills().hasProtection());
        playerConfig.set(player.getName() + ".bedperk", player.getSkills().hasBedPerk());
      }
      playerConfig.save(depositsFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void loadDeposits() {
    try {
      FileConfiguration depositConfig = YamlConfiguration.loadConfiguration(depositsFile);
      ConfigurationSection section = depositConfig.getConfigurationSection("deposits");
      Set<String> childSections = section.getKeys(false);

      for (String child : childSections) {
        int id = Integer.parseInt(child);
        Location location = new Location(world, depositConfig.getInt("deposits." + child + ".x"),
            depositConfig.getInt("deposits." + child + ".y"), depositConfig.getInt("deposits." + child + ".z"));
        String owner = depositConfig.getString("deposits." + child + ".owner");
        Block chest = location.getBlock();
        depositBoxes.add(new DepositBox(chest, owner, id));
      }
    } catch (NullPointerException e) {
      getLogger().info("Deposit Box File has no contents");
    }
  }

  public void loadShipments() {
    try {
      FileConfiguration depositConfig = YamlConfiguration.loadConfiguration(depositsFile);
      ConfigurationSection section = depositConfig.getConfigurationSection("deposits");
      Set<String> childSections = section.getKeys(false);

      for (String child : childSections) {
        int id = Integer.parseInt(child);
        Location location = new Location(world, depositConfig.getInt("shipments." + child + ".x"),
            depositConfig.getInt("shipments." + child + ".y"), depositConfig.getInt("shipments." + child + ".z"));
        String owner = depositConfig.getString("shipments." + child + ".owner");
        Block chest = location.getBlock();
        shipmentBoxes.add(new ShipmentBox(chest, owner, id));
      }
    } catch (NullPointerException e) {
      getLogger().info("Shipment Box File has no contents");
    }
  }

  public void loadPens() {
    try {
      FileConfiguration depositConfig = YamlConfiguration.loadConfiguration(depositsFile);
      ConfigurationSection section = depositConfig.getConfigurationSection("pens");
      Set<String> childSections = section.getKeys(false);

      for (String child : childSections) {
        int id = Integer.parseInt(child);
        Location locationA = new Location(world, depositConfig.getInt("pens." + child + ".a.x"),
            depositConfig.getInt("pens." + child + ".a.y"), depositConfig.getInt("pens." + child + ".a.z"));
        Location locationB = new Location(world, depositConfig.getInt("pens." + child + ".b.x"),
            depositConfig.getInt("pens." + child + ".b.y"), depositConfig.getInt("pens." + child + ".b.z"));
        String owner = depositConfig.getString("pens." + child + ".owner");
        pens.add(new Pen(locationA, locationB, owner, id));
      }
    } catch (NullPointerException e) {
      getLogger().info("Pens File has no contents");
    }
  }

  public void loadNametags() {
    try {
      FileConfiguration depositConfig = YamlConfiguration.loadConfiguration(depositsFile);
      ConfigurationSection section = depositConfig.getConfigurationSection("nametags");
      Set<String> childSections = section.getKeys(false);

      for (String child : childSections) {
        int days = depositConfig.getInt("nametags." + child + ".days");
        animalNames.put(child, days);
      }
    } catch (NullPointerException e) {
      getLogger().info("Nametags File has no contents");
    }
  }

  public int loadPlayerSkillProfits(String playerName) {
    FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playersFile);
    int level;
    if (playerConfig.contains(playerName + ".profit"))
      level = playerConfig.getInt(playerName + ".profit");
    else
      level = 0;
    return level;
  }

  public boolean loadPerk(String playerName, String perkName) {
    FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playersFile);
    boolean perk;
    if (playerConfig.contains(playerName + "." + perkName))
      perk = playerConfig.getBoolean(playerName + "." + perkName);
    else {
      perk = false;
      playerConfig.set(playerName + "." + perkName, false);
    }
    return perk;
  }

  public void loadAnimalCosts() {
    Set<String> entities = Objects.requireNonNull(animalCostConfig.getConfigurationSection("animals")).getKeys(false);
    for (String entity : entities) {
      EntityType ent = getEntity(entity.toUpperCase());
      if (ent != null) {
        animalCost.put(ent, animalCostConfig.getDouble("animals." + entity + ".cost"));
      } else
        getLogger().info("failed to find EntityType:" + entity.toUpperCase());
    }
  }

  public void loadDepositShop() {
    Set<String> items = Objects.requireNonNull(shopConfig.getConfigurationSection("depositableitems")).getKeys(false);
    for (String item : items) {
      Material material = getMaterial(item);
      if (material != null)
        depositCrops.add(new DepositCrop(material, shopConfig.getDouble("depositableitems." + item + ".cost"),
            shopConfig.getString("depositableitems." + item + ".type")));
      else
        getLogger().info(item + " could not be identified when loading the depositableitems section in shop.yml");
    }
  }

  public void loadGeneralStore() {
    Set<String> items = Objects.requireNonNull(shopConfig.getConfigurationSection("generalitems")).getKeys(false);
    for (String item : items) {
      Material material = getMaterial(item);
      if (material != null)
        addToInventory(generalInventory, material, shopConfig.getInt("generalitems." + item + ".cost"),
            shopConfig.getInt("generalitems." + item + ".amount"));
      else
        getLogger().info(item + " could not be identified");
    }
  }

  public String getConfigurationString(YamlConfiguration config, String item) {
    if (!config.getString(item).equals(null)) {
      return config.getString(item);
    }
    return null;
  }

  public int getConfigurationInt(YamlConfiguration config, String item) {
    if (!config.getString(item).equals(null)) {
      return config.getInt(item);
    }
    return 0;
  }

  public double getConfigurationDouble(YamlConfiguration config, String item) {
    if (!config.getString(item).equals(null)) {
      return config.getDouble(item);
    }
    return 0;
  }

  public boolean getConfigurationBoolean(YamlConfiguration config, String item) {
    if (!config.getString(item).equals(null)) {
      return config.getBoolean(item);
    }
    return false;
  }

  public void loadBuyShop() {
    int itemSlots = shopConfig.getInt("slots");
    Set<String> items = Objects.requireNonNull(shopConfig.getConfigurationSection("items")).getKeys(false);
    double defaultShopChance = shopConfig.getDouble("shopchance");
    double defaultSpecialChance = shopConfig.getDouble("specialchance");
    buyInventory = Bukkit.createInventory(null, itemSlots, "Farmers Daily Shop");
    LinkedList<Material> selectedMaterials = new LinkedList<Material>();
    Random random = new Random();
    int failedItems = 0;
    while (selectedMaterials.size() < itemSlots && selectedMaterials.size() < items.size() - failedItems) {
      failedItems = 0;
      for (String item : items) {
        Material material = getMaterial(item);
        if (material != null) {
          if (!selectedMaterials.contains(material)) {
            double shopChance = defaultShopChance;
            if (shopConfig.contains("items." + item + ".shopchance"))
              shopChance = shopConfig.getDouble("items." + item + ".shopchance");
            int chance = random.nextInt(1 + items.size() - selectedMaterials.size());
            boolean picked = false;
            if (chance == 1) {
              if (shopChance < 1) {
                double confirmSelect = random.nextDouble(1);
                picked = !(confirmSelect > shopChance);
              } else
                picked = true;
            }

            if (picked) {
              double specialChance = defaultSpecialChance;
              int amount = shopConfig.getInt("items." + item + ".amount");

              if (shopConfig.contains("items." + item + ".specialchance"))
                shopChance = shopConfig.getDouble("items." + item + ".specialchance");

              int cost = shopConfig.getInt("items." + item + ".cost");
              int originalCost = cost;
              boolean isSpecial = false;
              if (shopConfig.contains("items." + item + ".special")) {
                int special = shopConfig.getInt("items." + item + ".special");
                double doSpecial = random.nextDouble(1);
                if (!(doSpecial > specialChance)) {
                  cost = special;
                  isSpecial = true;
                }
              }

              addToInventory(buyInventory, material, cost, amount, isSpecial, originalCost - cost);
              selectedMaterials.add(material);
            }
          }
        } else {
          getLogger().info(item + " could not be identified");
          failedItems++;
        }
      }
    }
  }
}
