package curtis1509.farmerslife;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static curtis1509.farmerslife.FarmersLife.*;
import static curtis1509.farmerslife.Functions.*;
import static org.bukkit.Bukkit.getLogger;

public class FileReader {

    //YAML File Objects
    File saveDir = new File("plugins/FarmersLife/save.yml");
    File configDir = new File("plugins/FarmersLife/config.yml");
    File depositHistoryDir = new File("plugins/FarmersLife/depositHistory.yml");

    //YAML Configuration Objects
    FileConfiguration saveFile = YamlConfiguration.loadConfiguration(saveDir);
    FileConfiguration configFile = YamlConfiguration.loadConfiguration(configDir);
    FileConfiguration depositHistoryFile = YamlConfiguration.loadConfiguration(depositHistoryDir);

    public void FileProcessNewDay(){
        saveWeather();
        savePlayers();
        saveDeposits();
        savePens();
        saveDepositHistory();
        createBackup();
    }
    public void FileProcessReloadShop(){
        loadBuyShop();
        loadAnimalCosts();
        loadDepositShop();
        loadGeneralStore();
    }
    public void FileProcessEnablePlugin(){
        createFiles();
        loadDepositShop();
        loadDeposits();
        loadGeneralStore();
        loadPens();
        loadNametags();
        loadAnimalCosts();
        loadWeather();
    }
    public void FileProcessDisablePlugin(){
        savePlayers();
        saveDeposits();
        savePens();
        saveDepositHistory();
    }

    public void throwFileError(IOException exception, String fileName){
        System.out.println("There was an error whilst saving " + fileName);
        getLogger().info(exception.toString());
    }

    //MARKED FOR REWORK
    public void createFiles() {
        try {
            Path dir = Paths.get("plugins/FarmersLife");
            if (!Files.isDirectory(dir))
                Files.createDirectories(dir);
            if (!Files.exists(Path.of(saveDir.getPath())))
                saveDir.createNewFile();
            if (!Files.exists(Path.of(configDir.getPath())))
                configDir.createNewFile();
            if (!Files.exists(Path.of(depositHistoryDir.getPath())))
                depositHistoryDir.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createBackup() {
        try {
            Date time = Calendar.getInstance().getTime();

            //Create the new file with date stamp
            Files.createDirectories(Paths.get("plugins/FarmersLife/Backups"));
            String date = Calendar.getInstance().getTime().getSeconds() + "_secs_" + time.getHours() + "_hours_" + time.getMinutes() + "mins_" + time.getDay() + "_" + time.getMonth() + "_" + time.getYear();

            //Copy the most current save file to the new date stamped file in backups directory.
            File saveFileBackup = new File("plugins/FarmersLife/Backups/save" + date + ".yml");
            Files.copy(saveDir.toPath(), saveFileBackup.toPath(), StandardCopyOption.COPY_ATTRIBUTES);

        }
        catch(IOException exception){
            System.out.println("There was an error whilst creating the backup");
            exception.printStackTrace();
        }
    }

    public void loadWeather() {
        FarmersLife.day = saveFile.getInt("season.day");
        FarmersLife.season = saveFile.getString("season.name");
        FarmersLife.seasonLength = saveFile.getInt("season.length");
    }

    public void saveWeather() {
        try {
            saveFile.set("season.day", FarmersLife.day);
            saveFile.set("season.name", FarmersLife.season);
            saveFile.save(saveDir);
        } catch(IOException exception){
          throwFileError(exception,"weather.yml");
        }
    }

    public void saveDepositHistory() {

        for (Player player : players) {
            if (!player.getDailyDepositHistory().isEmpty()) {

                int total = 0;

                //Set amount earned per item for player
                for (Map.Entry<String, Double> entry : player.getDailyDepositHistory().entrySet()) {
                    String key = entry.getKey();
                    double value = entry.getValue();
                    total += value;
                    depositHistoryFile.set(day + "." + player.playerName + "." + key, "$" + value);
                }
                //Set total amount earned for player
                depositHistoryFile.set(day + "." + player.playerName + "." + "total", "$" + total);

                //Set the multiplier for player on this day
                double multiplier = player.getSkills().skillProfits.getMultiplier();
                depositHistoryFile.set(day + "." + player.playerName + "." + "multiplier", "x" + multiplier);

                player.getDailyDepositHistory().clear();
                player.addToTodaysCash(total * multiplier);

            }
        }

        try {
            depositHistoryFile.save(depositHistoryDir);
        }catch(IOException exception){
            getLogger().info("Failed to save deposit history file");
            getLogger().info(exception.getMessage());
        }

    }

    public void removeDepositData(DepositBox depositBox) throws IOException {
        String id = "deposits." + depositBox.getID();
        if (saveFile.contains(id)) {
            saveFile.set(id, null);
        }
        saveFile.save(saveDir);
        depositBoxes.remove(depositBox);
    }

    public void removePenData(Pen pen) throws IOException {
        String id = "pens." + pen.id;
        if (saveFile.contains(id)) {
            saveFile.set(id, null);
        }
        saveFile.save(saveDir);
        pens.remove(pen);
    }

    public void removeNametagData(String nametag) throws IOException {
        String id = "nametags." + nametag;
        if (saveFile.contains(id)) {
            saveFile.set(id, null);
        }
        saveFile.save(saveDir);
        animalNames.remove(nametag);
    }

    public void saveDeposits(){
        for (DepositBox deposits : depositBoxes) {
            String id = "deposits." + deposits.getID();
            saveFile.set(id + ".owner", deposits.getOwner());
            saveFile.set(id + ".shipment", deposits.isShipmentBox());
            saveFile.set(id + ".x", deposits.getDepositBox().getLocation().getBlockX());
            saveFile.set(id + ".y", deposits.getDepositBox().getLocation().getBlockY());
            saveFile.set(id + ".z", deposits.getDepositBox().getLocation().getBlockZ());
        }
        try {
            saveFile.save(saveDir);
        }catch(IOException exception){
            throwFileError(exception,"deposits");
        }
    }

    public void savePens(){
        for (Pen pen : pens) {
            String id = "pens." + pen.id;
            saveFile.set(id + ".owner", pen.owner);
            saveFile.set(id + ".a.x", pen.pointA.getBlockX());
            saveFile.set(id + ".a.y", pen.pointA.getBlockY());
            saveFile.set(id + ".a.z", pen.pointA.getBlockZ());

            saveFile.set(id + ".b.x", pen.pointB.getBlockX());
            saveFile.set(id + ".b.y", pen.pointB.getBlockY());
            saveFile.set(id + ".b.z", pen.pointB.getBlockZ());
        }

        for (String key : animalNames.keySet()) {
            String id = "nametags." + key;
            saveFile.set(id + ".days", animalNames.get(key));
        }

        try {
            saveFile.save(saveDir);
        }catch(IOException exception){
            throwFileError(exception, "deposits");
        }
    }

    public void savePlayers() {
        try {
            for (Player player : players) {
                saveFile.set("players." + player.getName() + ".cash", economy.getBalance(player.getPlayer()));
                double previousCash = saveFile.getDouble("players." + player.getName() + ".alltimecash");
                saveFile.set("players." + player.getName() + ".alltimecash", economy.getBalance(player.getPlayer()) + previousCash);
                saveFile.set("players." + player.getName() + ".profit", player.getSkills().skillProfits.getLevel());
                saveFile.set("players." + player.getName() + ".protection", player.getSkills().protection);
                saveFile.set("players." + player.getName() + ".bedperk", player.getSkills().bedperk);
            }
            saveFile.save(saveDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Player loadPlayer(org.bukkit.entity.Player player){
        return new curtis1509.farmerslife.Player(player,
                fileReader.loadPlayerSkillProfits("players." + player.getName()),
                fileReader.loadPerk("players." + player.getName(), "protection"),
                fileReader.loadPerk("players." + player.getName(), "bedperk"),
                fileReader.loadPerk("players." + player.getName(), "teleport"));
    }

    public void loadDeposits() {
        try {
            ConfigurationSection section = saveFile.getConfigurationSection("deposits");
            Set<String> childSections = section.getKeys(false);

            for (String child : childSections) {
                int id = Integer.parseInt(child);
                Location location = new Location(world, saveFile.getInt("deposits." + child + ".x"), saveFile.getInt("deposits." + child + ".y"), saveFile.getInt("deposits." + child + ".z"));
                String owner = saveFile.getString("deposits." + child + ".owner");
                boolean shipment = saveFile.getBoolean("deposits." + child + ".shipment");
                Block chest = location.getBlock();
                depositBoxes.add(new DepositBox(chest, owner, id,shipment));
            }
        } catch (NullPointerException e) {
            getLogger().info("Deposit Box File has no contents");
        }
    }

    public void loadPens() {
        try {
            ConfigurationSection section = saveFile.getConfigurationSection("pens");
            Set<String> childSections = section.getKeys(false);

            for (String child : childSections) {
                int id = Integer.parseInt(child);
                Location locationA = new Location(world, saveFile.getInt("pens." + child + ".a.x"), saveFile.getInt("pens." + child + ".a.y"), saveFile.getInt("pens." + child + ".a.z"));
                Location locationB = new Location(world, saveFile.getInt("pens." + child + ".b.x"), saveFile.getInt("pens." + child + ".b.y"), saveFile.getInt("pens." + child + ".b.z"));
                String owner = saveFile.getString("pens." + child + ".owner");
                pens.add(new Pen(locationA, locationB, owner, id));
            }
        } catch (NullPointerException e) {
            getLogger().info("Pens File has no contents");
        }
    }

    public void loadNametags() {
        try {
            ConfigurationSection section = saveFile.getConfigurationSection("nametags");
            Set<String> childSections = section.getKeys(false);

            for (String child : childSections) {
                int days = saveFile.getInt("nametags." + child + ".days");
                animalNames.put(child,days);
            }
        } catch (NullPointerException e) {
            getLogger().info("Nametags File has no contents");
        }
    }

    public int loadPlayerSkillProfits(String playerName) {
        int level;
        if (saveFile.contains(playerName + ".profit"))
            level = saveFile.getInt(playerName + ".profit");
        else
            level = 0;
        return level;
    }

    public boolean loadPerk(String playerName, String perkName) {
        boolean perk;
        if (saveFile.contains(playerName + "."+perkName))
            perk = saveFile.getBoolean(playerName + "."+perkName);
        else {
            perk = false;
            saveFile.set(playerName + "."+perkName, false);
        }
        return perk;
    }
    public void loadAnimalCosts() {
        Set<String> entities = Objects.requireNonNull(configFile.getConfigurationSection("animals")).getKeys(false);
        for (String entity : entities) {
            EntityType ent = getEntity(entity.toUpperCase());
            if (ent!=null){
                animalCost.put(ent, configFile.getDouble("animals."+entity+".cost"));
            }
            else
                getLogger().info("failed to find EntityType:"+entity.toUpperCase());
        }
    }

    public void loadDepositShop(){
        Set<String> items = Objects.requireNonNull(configFile.getConfigurationSection("depositableitems")).getKeys(false);
        for (String item : items) {
            Material material = getMaterial(item);
            if (material != null)
                depositCrops.add(new DepositCrop(material, configFile.getInt("depositableitems." + item + ".cost")));
            else
                getLogger().info(item + " could not be identified when loading the depositableitems section in shop.yml");
        }
    }
    public void loadGeneralStore(){
        Set<String> items = Objects.requireNonNull(configFile.getConfigurationSection("generalitems")).getKeys(false);
        for (String item : items) {
            Material material = getMaterial(item);
            if (material != null)
                addToInventory(generalInventory, material, configFile.getInt("generalitems." + item + ".cost"), configFile.getInt("generalitems." + item + ".amount"));
            else
                getLogger().info(item + " could not be identified");
        }
    }

    public void loadBuyShop(){
        int itemSlots = configFile.getInt("slots");
        Set<String> items = Objects.requireNonNull(configFile.getConfigurationSection("items")).getKeys(false);
        double defaultShopChance = configFile.getDouble("shopchance");
        double defaultSpecialChance = configFile.getDouble("specialchance");
        double defaultFreeChance = configFile.getDouble("freechance");
        buyInventory = Bukkit.createInventory(null, itemSlots, "Farmers Daily Shop");
        LinkedList<Material> selectedMaterials = new LinkedList<Material>();
        Random random = new Random();
        int failedItems = 0;
        while (selectedMaterials.size() < itemSlots && selectedMaterials.size() < items.size()-failedItems){
            failedItems = 0;
            for (String item : items){
                Material material = getMaterial(item);
                if (material != null){
                    if (!selectedMaterials.contains(material)) {
                        double shopChance = defaultShopChance;
                        if (configFile.contains("items." + item + ".shopchance"))
                            shopChance = configFile.getDouble("items." + item + ".shopchance");
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
                            double freeChance = defaultFreeChance;
                            int amount = configFile.getInt("items." + item + ".amount");

                            if (configFile.contains("items." + item + ".specialchance"))
                                shopChance = configFile.getDouble("items." + item + ".specialchance");
                            if (configFile.contains("items." + item + ".freechance"))
                                shopChance = configFile.getDouble("items." + item + ".freechance");

                            int cost = configFile.getInt("items." + item + ".cost");
                            int originalCost = cost;
                            boolean isSpecial = false;
                            if (configFile.contains("items." + item + ".special")) {
                                int special = configFile.getInt("items." + item + ".special");
                                double doSpecial = random.nextDouble(1);
                                if (!(doSpecial > specialChance)) {
                                    cost = special;
                                    isSpecial = true;
                                }
                            }

                            addToInventory(buyInventory, material, cost, amount,isSpecial,originalCost-cost);
                            selectedMaterials.add(material);
                        }
                    }

                }else {
                    getLogger().info(item + " could not be identified");
                    failedItems++;
                }
            }
        }
    }

}

