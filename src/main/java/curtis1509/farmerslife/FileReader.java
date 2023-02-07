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
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static curtis1509.farmerslife.FarmersLife.*;
import static curtis1509.farmerslife.Functions.addToInventory;
import static curtis1509.farmerslife.Functions.getMaterial;
import static org.bukkit.Bukkit.getLogger;

public class FileReader {

    public String read(String filename) {

        StringBuilder processString = new StringBuilder();
        Scanner scanner = null;

        try {
            scanner = new Scanner(new File(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (true) {
            assert scanner != null;
            if (!scanner.hasNextLine()) break;
            while (scanner.hasNext()) {
                processString.append(" ").append(scanner.next());
            }
        }

        return processString.toString();
    }

    File playersFileT = new File("plugins/FarmersLife/players.yml");
    File depositsFileT = new File("plugins/FarmersLife/deposits.yml");
    File statsFileT = new File("plugins/FarmersLife/stats.yml");
    File weatherFileT = new File("plugins/FarmersLife/weather.yml");

    FileConfiguration statsConfig = YamlConfiguration.loadConfiguration(statsFileT);
    FileConfiguration shopConfig = YamlConfiguration.loadConfiguration(new File("plugins/FarmersLife/shop.yml"));

    public void FileProcessNewDay(){
        saveDays();
        savePlayers();
        saveDeposits();
        savePens();
        createBackup();
    }
    public void FileProcessReloadShop(){
        loadBuyShop();
        loadAnimalCosts();
        loadDepositShop();
        loadGeneralStore();
    }
    public void FileProcessEnablePlugin(){
        CreateFile();
        loadDepositShop();
        loadDeposits();
        loadPens();
        loadNametags();
    }
    public void FileProcessDisablePlugin(){
        savePlayers();
        saveDeposits();
        savePens();
    }

    public void throwFileError(IOException exception, String fileName){
        System.out.println("There was an error whilst saving " + fileName);
        exception.printStackTrace();
    }

    public void saveDays(){
        try {
            FileConfiguration weatherConfig = YamlConfiguration.loadConfiguration(weatherFileT);
            weatherConfig.set("day.count", dayNumber);
            weatherConfig.save(weatherFileT);
        }
        catch(IOException exception){
            throwFileError(exception,"weather");
        }
    }

    //MARKED FOR REWORK
    public void CreateFile() {
        try {
            Files.createDirectories(Paths.get("plugins/FarmersLife"));
            playersFileT.createNewFile();
            depositsFileT.createNewFile();
            statsFileT.createNewFile();
            weatherFileT.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createBackup() {
        try {
            Files.createDirectories(Paths.get("plugins/FarmersLife/Backups"));
            String date = Calendar.getInstance().getTime().getSeconds() + "_secs_" + Calendar.getInstance().getTime().getHours() + "_hours_" + Calendar.getInstance().getTime().getMinutes() + "mins_" + Calendar.getInstance().getTime().getDay() + "_" + Calendar.getInstance().getTime().getMonth() + "_" + Calendar.getInstance().getTime().getYear();

            File playersFileBackup = new File("plugins/FarmersLife/Backups/players" + date + ".yml");
            Files.copy(playersFileT.toPath(), playersFileBackup.toPath(), StandardCopyOption.COPY_ATTRIBUTES);

            File depositsFileBackup = new File("plugins/FarmersLife/Backups/deposits" + date + ".yml");
            Files.copy(depositsFileT.toPath(), depositsFileBackup.toPath(), StandardCopyOption.COPY_ATTRIBUTES);

            File statsFileBackup = new File("plugins/FarmersLife/Backups/stats" + date + ".yml");
            Files.copy(statsFileT.toPath(), statsFileBackup.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
        }
        catch(IOException exception){
            System.out.println("There was an error whilst creating the backup");
            exception.printStackTrace();
        }
    }

    HashMap<String, Double> stats = new HashMap<>();

    public void addStat(String material, double stat) throws IOException {

        if (stats.containsKey(material)) {
            stats.replace(material, stats.get(material) + stat);
        } else
            stats.put(material, stat);
    }

    public void getWeather() throws IOException {
        FileConfiguration weatherConfig = YamlConfiguration.loadConfiguration(weatherFileT);
        int day = weatherConfig.getInt("day.count");
        if (day == 0) {
            day = 1;
            weatherConfig.set("day.count", 1);
            weatherConfig.save(weatherFileT);
            weather = "Wet";
        }
        if (day > 20 && day < 40) {
            weather = "Dry";
        } else if (day > 0 && day < 20) {
            weather = "Wet";
        } else if (day > 40) {
            weather = "Wet";
            weatherConfig.set("day.count", 1);
            weatherConfig.save(weatherFileT);
        }
        if (day <= 20)
            dayNumber = 20 - day;
        else
            dayNumber = 40 - day;
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
            statsConfig.save(statsFileT);
            stats.clear();

            for (Player p : players) {
                if (p.getName().equals(playerName))
                    p.addToTodaysCash(total * multiplier);
            }
        }
    }

    public void removeDepositData(DepositBox depositBox) throws IOException {
        FileConfiguration depositConfig = YamlConfiguration.loadConfiguration(depositsFileT);
        String id = "deposits." + depositBox.getID();
        if (depositConfig.contains(id)) {
            depositConfig.set(id, null);
        }
        depositConfig.save(depositsFileT);
        depositBoxes.remove(depositBox);
    }

    public void removePenData(Pen pen) throws IOException {
        FileConfiguration depositConfig = YamlConfiguration.loadConfiguration(depositsFileT);
        String id = "pens." + pen.id;
        if (depositConfig.contains(id)) {
            depositConfig.set(id, null);
        }
        depositConfig.save(depositsFileT);
        pens.remove(pen);
    }

    public void removeNametagData(String nametag) throws IOException {
        FileConfiguration depositConfig = YamlConfiguration.loadConfiguration(depositsFileT);
        String id = "nametags." + nametag;
        if (depositConfig.contains(id)) {
            depositConfig.set(id, null);
        }
        depositConfig.save(depositsFileT);
        animalNames.remove(nametag);
    }

    public void saveDeposits(){
        FileConfiguration depositConfig = YamlConfiguration.loadConfiguration(depositsFileT);
        for (DepositBox deposits : depositBoxes) {
            String id = "deposits." + deposits.getID();
            depositConfig.set(id + ".owner", deposits.getOwner());
            depositConfig.set(id + ".shipment", deposits.isShipmentBox());
            depositConfig.set(id + ".x", deposits.getDepositBox().getLocation().getBlockX());
            depositConfig.set(id + ".y", deposits.getDepositBox().getLocation().getBlockY());
            depositConfig.set(id + ".z", deposits.getDepositBox().getLocation().getBlockZ());
        }
        try {
            depositConfig.save(depositsFileT);
        }catch(IOException exception){
            throwFileError(exception,"deposits");
        }
    }

    public void savePens(){
        FileConfiguration depositConfig = YamlConfiguration.loadConfiguration(depositsFileT);
        for (Pen pen : pens) {
            String id = "pens." + pen.id;
            depositConfig.set(id + ".owner", pen.owner);
            depositConfig.set(id + ".a.x", pen.pointA.getBlockX());
            depositConfig.set(id + ".a.y", pen.pointA.getBlockY());
            depositConfig.set(id + ".a.z", pen.pointA.getBlockZ());

            depositConfig.set(id + ".b.x", pen.pointB.getBlockX());
            depositConfig.set(id + ".b.y", pen.pointB.getBlockY());
            depositConfig.set(id + ".b.z", pen.pointB.getBlockZ());
        }

        for (String key : animalNames.keySet()) {
            String id = "nametags." + key;
            depositConfig.set(id + ".days", animalNames.get(key));
        }

        try {
            depositConfig.save(depositsFileT);
        }catch(IOException exception){
            throwFileError(exception, "deposits");
        }
    }

    public void savePlayers() {

        CreateFile();

        try {

            FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playersFileT);
            for (Player player : players) {
                playerConfig.set(player.getName() + ".cash", economy.getBalance(player.getPlayer()));
                double previousCash = playerConfig.getDouble("alltimecash");
                playerConfig.set(player.getName() + ".alltimecash", economy.getBalance(player.getPlayer()) + previousCash);
                playerConfig.set(player.getName() + ".profit", player.getSkills().skillProfits.getLevel());
                playerConfig.set(player.getName() + ".protection", player.getSkills().protection);
                playerConfig.set(player.getName() + ".bedperk", player.getSkills().bedperk);
            }
            playerConfig.save(playersFileT);



            createBackup();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadDeposits() {

        try {
            FileConfiguration depositConfig = YamlConfiguration.loadConfiguration(depositsFileT);
            ConfigurationSection section = depositConfig.getConfigurationSection("deposits");
            Set<String> childSections = section.getKeys(false);

            for (String child : childSections) {
                int id = Integer.parseInt(child);
                Location location = new Location(world, depositConfig.getInt("deposits." + child + ".x"), depositConfig.getInt("deposits." + child + ".y"), depositConfig.getInt("deposits." + child + ".z"));
                String owner = depositConfig.getString("deposits." + child + ".owner");
                boolean shipment = depositConfig.getBoolean("deposits." + child + ".shipment");
                Block chest = location.getBlock();
                depositBoxes.add(new DepositBox(chest, owner, id,shipment));
            }
        } catch (NullPointerException e) {
            getLogger().info("Deposit Box File has no contents");
        }
    }

    public void loadPens() {
        try {
            FileConfiguration depositConfig = YamlConfiguration.loadConfiguration(depositsFileT);
            ConfigurationSection section = depositConfig.getConfigurationSection("pens");
            Set<String> childSections = section.getKeys(false);

            for (String child : childSections) {
                int id = Integer.parseInt(child);
                Location locationA = new Location(world, depositConfig.getInt("pens." + child + ".a.x"), depositConfig.getInt("pens." + child + ".a.y"), depositConfig.getInt("pens." + child + ".a.z"));
                Location locationB = new Location(world, depositConfig.getInt("pens." + child + ".b.x"), depositConfig.getInt("pens." + child + ".b.y"), depositConfig.getInt("pens." + child + ".b.z"));
                String owner = depositConfig.getString("pens." + child + ".owner");
                pens.add(new Pen(locationA, locationB, owner, id));
            }
        } catch (NullPointerException e) {
            getLogger().info("Pens File has no contents");
        }
    }

    public void loadNametags() {
        try {
            FileConfiguration depositConfig = YamlConfiguration.loadConfiguration(depositsFileT);
            ConfigurationSection section = depositConfig.getConfigurationSection("nametags");
            Set<String> childSections = section.getKeys(false);

            for (String child : childSections) {
                int days = depositConfig.getInt("nametags." + child + ".days");
                animalNames.put(child,days);
            }
        } catch (NullPointerException e) {
            getLogger().info("Nametags File has no contents");
        }
    }

    public int loadPlayerSkillProfits(String playerName) {
        FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playersFileT);
        int level;
        if (playerConfig.contains(playerName + ".profit"))
            level = playerConfig.getInt(playerName + ".profit");
        else
            level = 0;
        return level;
    }

    public boolean loadPerk(String playerName, String perkName) {
        FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playersFileT);
        boolean perk;
        if (playerConfig.contains(playerName + "."+perkName))
            perk = playerConfig.getBoolean(playerName + "."+perkName);
        else {
            perk = false;
            playerConfig.set(playerName + "."+perkName, false);
        }
        return perk;
    }

    //MARKED FOR REWORK : Should loop keys and perform similar call to method created for loadShop functions: getMaterial(String) then if non-null result returned cost should be applied to it and then stored.
    public void loadAnimalCosts() {
        FileConfiguration costConfig = YamlConfiguration.loadConfiguration(new File("plugins/FarmersLife/animalCosts.yml"));
        Set<String> items = Objects.requireNonNull(costConfig.getConfigurationSection("animals")).getKeys(false);
        if (costConfig.contains("animals.cow")){
            animalCost.put(EntityType.COW, costConfig.getDouble("animals.cow.cost"));
        }
        if (costConfig.contains("animals.pig")){
            animalCost.put(EntityType.PIG, costConfig.getDouble("animals.pig.cost"));
        }
        if (costConfig.contains("animals.sheep")){
            animalCost.put(EntityType.SHEEP, costConfig.getDouble("animals.sheep.cost"));
        }
        if (costConfig.contains("animals.panda")){
            animalCost.put(EntityType.PANDA, costConfig.getDouble("animals.panda.cost"));
        }
        if (costConfig.contains("animals.donkey")){
            animalCost.put(EntityType.DONKEY, costConfig.getDouble("animals.donkey.cost"));
        }
        if (costConfig.contains("animals.horse")){
            animalCost.put(EntityType.HORSE, costConfig.getDouble("animals.horse.cost"));
        }
        if (costConfig.contains("animals.chicken")){
            animalCost.put(EntityType.CHICKEN, costConfig.getDouble("animals.chicken.cost"));
        }
        if (costConfig.contains("animals.villager")){
            animalCost.put(EntityType.VILLAGER, costConfig.getDouble("animals.villager.cost"));
        }
        if (costConfig.contains("animals.cat")){
            animalCost.put(EntityType.CAT, costConfig.getDouble("animals.cat.cost"));
        }
        if (costConfig.contains("animals.dog")){
            animalCost.put(EntityType.WOLF, costConfig.getDouble("animals.dog.cost"));
        }
        if (costConfig.contains("animals.axolotl")){
            animalCost.put(EntityType.AXOLOTL, costConfig.getDouble("animals.axolotl.cost"));
        }
        if (costConfig.contains("animals.allay")){
            animalCost.put(EntityType.ALLAY, costConfig.getDouble("animals.allay.cost"));
        }
        if (costConfig.contains("animals.bee")){
            animalCost.put(EntityType.BEE, costConfig.getDouble("animals.bee.cost"));
        }
        if (costConfig.contains("animals.bat")){
            animalCost.put(EntityType.BAT, costConfig.getDouble("animals.bat.cost"));
        }
        if (costConfig.contains("animals.dolphin")){
            animalCost.put(EntityType.DOLPHIN, costConfig.getDouble("animals.dolphin.cost"));
        }
        if (costConfig.contains("animals.tropicalfish")){
            animalCost.put(EntityType.TROPICAL_FISH, costConfig.getDouble("animals.tropicalfish.cost"));
        }
        if (costConfig.contains("animals.pufferfish")){
            animalCost.put(EntityType.PUFFERFISH, costConfig.getDouble("animals.pufferfish.cost"));
        }
        if (costConfig.contains("animals.salmon")){
            animalCost.put(EntityType.SALMON, costConfig.getDouble("animals.salmon.cost"));
        }
        if (costConfig.contains("animals.cod")){
            animalCost.put(EntityType.COD, costConfig.getDouble("animals.cod.cost"));
        }
        if (costConfig.contains("animals.fox")){
            animalCost.put(EntityType.FOX, costConfig.getDouble("animals.fox.cost"));
        }
        if (costConfig.contains("animals.rabbit")){
            animalCost.put(EntityType.RABBIT, costConfig.getDouble("animals.rabbit.cost"));
        }
        if (costConfig.contains("animals.frog")){
            animalCost.put(EntityType.FROG, costConfig.getDouble("animals.frog.cost"));
        }
        if (costConfig.contains("animals.skeletonhorse")){
            animalCost.put(EntityType.SKELETON_HORSE, costConfig.getDouble("animals.skeletonhorse.cost"));
        }
        if (costConfig.contains("animals.parrot")){
            animalCost.put(EntityType.PARROT, costConfig.getDouble("animals.parrot.cost"));
        }
        if (costConfig.contains("animals.llama")){
            animalCost.put(EntityType.LLAMA, costConfig.getDouble("animals.llama.cost"));
        }
        if (costConfig.contains("animals.goat")){
            animalCost.put(EntityType.GOAT, costConfig.getDouble("animals.goat.cost"));
        }
        if (costConfig.contains("animals.mule")){
            animalCost.put(EntityType.MULE, costConfig.getDouble("animals.mule.cost"));
        }
        if (costConfig.contains("animals.mooshroom")){
            animalCost.put(EntityType.MUSHROOM_COW, costConfig.getDouble("animals.mooshroom.cost"));
        }
        if (costConfig.contains("animals.squid")){
            animalCost.put(EntityType.SQUID, costConfig.getDouble("animals.squid.cost"));
        }
    }

    //Load Deposit Shop
    public void loadDepositShop(){
        ConfigurationSection section = shopConfig.getConfigurationSection("depositableitems");
        Set<String> childSections = section.getKeys(false);

        for (String child : childSections) {
            Material material = getMaterial(child);
            if (material != null)
                depositCrops.add(new DepositCrop(material, shopConfig.getInt("depositableitems." + child + "cost")));
            else
                getLogger().info(child + " could not be identified when loading the depositableitems section in shop.yml");
        }
    }

    //Load General Store
    public void loadGeneralStore(){
        ConfigurationSection section = shopConfig.getConfigurationSection("generalitems");
        Set<String> childSections = section.getKeys(false);

        for (String child : childSections) {
            Material material = getMaterial(child);
            if (material != null)
                addToInventory(seedsInventory, material, shopConfig.getInt("generalitems." + child + "cost"), shopConfig.getInt("generalitems." + child + "amount"));
            else
                getLogger().info(child + " could not be identified");
        }
    }

    public void loadBuyShop(){
        int itemSlots = shopConfig.getInt("slots");
        Set<String> items = Objects.requireNonNull(shopConfig.getConfigurationSection("items")).getKeys(false);
        double defaultShopChance = shopConfig.getDouble("shopchance");
        double defaultSpecialChance = shopConfig.getDouble("specialchance");
        double defaultFreeChance = shopConfig.getDouble("freechance");
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
                            double freeChance = defaultFreeChance;
                            int amount = shopConfig.getInt("items." + item + ".amount");

                            if (shopConfig.contains("items." + item + ".specialchance"))
                                shopChance = shopConfig.getDouble("items." + item + ".specialchance");
                            if (shopConfig.contains("items." + item + ".freechance"))
                                shopChance = shopConfig.getDouble("items." + item + ".freechance");

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

