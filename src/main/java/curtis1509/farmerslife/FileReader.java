package curtis1509.farmerslife;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

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

    public void createBackup() throws IOException {
        Files.createDirectories(Paths.get("plugins/FarmersLife/Backups"));
        String date = Calendar.getInstance().getTime().getSeconds() + "_secs_" + Calendar.getInstance().getTime().getHours() + "_hours_" + Calendar.getInstance().getTime().getMinutes() + "mins_" + Calendar.getInstance().getTime().getDay() + "_" + Calendar.getInstance().getTime().getMonth() + "_" + Calendar.getInstance().getTime().getYear();

        File playersFileBackup = new File("plugins/FarmersLife/Backups/players" + date + ".yml");
        Files.copy(playersFileT.toPath(), playersFileBackup.toPath(), StandardCopyOption.COPY_ATTRIBUTES);

        File depositsFileBackup = new File("plugins/FarmersLife/Backups/deposits" + date + ".yml");
        Files.copy(depositsFileT.toPath(), depositsFileBackup.toPath(), StandardCopyOption.COPY_ATTRIBUTES);

        File statsFileBackup = new File("plugins/FarmersLife/Backups/stats" + date + ".yml");
        Files.copy(statsFileT.toPath(), statsFileBackup.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
    }

    HashMap<String, Double> stats = new HashMap<>();

    public void addStat(String material, double stat) throws IOException {

        if (stats.containsKey(material)) {
            stats.replace(material, stats.get(material) + stat);
        } else
            stats.put(material, stat);
    }

    public void newDay() throws IOException {
        FileConfiguration weatherConfig = YamlConfiguration.loadConfiguration(weatherFileT);
        int day = weatherConfig.getInt("day.count");
        day++;
        if (day > 40) {
            day = 1;
            Bukkit.broadcastMessage("Attention players, we are now entering a wet weather season. Expect lots of rain and thunder for the next 20 days!");
        } else if (day == 21) {
            Bukkit.broadcastMessage("Attention players, we are now entering a dry weather season. Expect lots of sun and nearly no rain for the next 20 days!");
        }
        weatherConfig.set("day.count", day);
        weatherConfig.save(weatherFileT);
        if (day > 20 && day < 40) {
            FarmersLife.weather = "Dry";
        } else if (day > 0 && day < 20) {
            FarmersLife.weather = "Wet";
        }

        if (day <= 20)
            FarmersLife.dayNumber = 20 - day;
        else
            FarmersLife.dayNumber = 40 - day;
    }

    public void getWeather() throws IOException {
        FileConfiguration weatherConfig = YamlConfiguration.loadConfiguration(weatherFileT);
        int day = weatherConfig.getInt("day.count");
        if (day == 0) {
            day = 1;
            weatherConfig.set("day.count", 1);
            weatherConfig.save(weatherFileT);
            FarmersLife.weather = "Wet";
        }
        if (day > 20 && day < 40) {
            FarmersLife.weather = "Dry";
        } else if (day > 0 && day < 20) {
            FarmersLife.weather = "Wet";
        } else if (day > 40) {
            FarmersLife.weather = "Wet";
            weatherConfig.set("day.count", 1);
            weatherConfig.save(weatherFileT);
        }
        if (day <= 20)
            FarmersLife.dayNumber = 20 - day;
        else
            FarmersLife.dayNumber = 40 - day;
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
            for (Player p : FarmersLife.players) {
                if (p.getName().equals(playerName))
                    multiplier = p.getSkills().skillProfits.getMultiplier();
            }
            statsConfig.set(day + "." + playerName + "." + "multiplier", "x" + multiplier);
            statsConfig.save(statsFileT);
            stats.clear();

            for (Player p : FarmersLife.players) {
                if (p.getName().equals(playerName))
                    p.addToTodaysCash(total * multiplier);
            }
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

    public void removeDepositData(DepositBox depositBox) throws IOException {
        FileConfiguration depositConfig = YamlConfiguration.loadConfiguration(depositsFileT);
        String id = "" + depositBox.getID();
        if (depositConfig.contains(id)) {
            depositConfig.set(id, null);
        }
        depositConfig.save(depositsFileT);
        FarmersLife.depositBoxes.remove(depositBox);
    }

    public void savePlayers() {

        CreateFile();

        try {

            FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playersFileT);
            for (Player player : FarmersLife.players) {
                playerConfig.set(player.getName() + ".cash", player.getCash());
                double previousCash = playerConfig.getDouble("alltimecash");
                playerConfig.set(player.getName() + ".alltimecash", player.getCash() + previousCash);
                playerConfig.set(player.getName() + ".profit", player.getSkills().skillProfits.getLevel());
                playerConfig.set(player.getName() + ".protection", player.getSkills().protection);
                playerConfig.set(player.getName() + ".bedperk", player.getSkills().bedperk);
            }
            playerConfig.save(playersFileT);

            FileConfiguration depositConfig = YamlConfiguration.loadConfiguration(depositsFileT);
            for (DepositBox deposits : FarmersLife.depositBoxes) {
                String id = "deposits." + deposits.getID();
                depositConfig.set(id + ".owner", deposits.getOwner());
                depositConfig.set(id + ".shipment", deposits.isShipmentBox());
                depositConfig.set(id + ".x", deposits.getDepositBox().getLocation().getBlockX());
                depositConfig.set(id + ".y", deposits.getDepositBox().getLocation().getBlockY());
                depositConfig.set(id + ".z", deposits.getDepositBox().getLocation().getBlockZ());
            }
            depositConfig.save(depositsFileT);

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
                Location location = new Location(FarmersLife.world, depositConfig.getInt("deposits." + child + ".x"), depositConfig.getInt("deposits." + child + ".y"), depositConfig.getInt("deposits." + child + ".z"));
                String owner = depositConfig.getString("deposits." + child + ".owner");
                boolean shipment = depositConfig.getBoolean("deposits." + child + ".shipment");
                Block chest = location.getBlock();
                FarmersLife.depositBoxes.add(new DepositBox(chest, owner, id,shipment));
            }
        } catch (NullPointerException e) {
            getLogger().info("Deposit Box File has no contents");
        }
    }

    public boolean loadProtection(String playerName) {
        FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playersFileT);
        boolean protection;
        if (playerConfig.contains(playerName + ".protection"))
            protection = playerConfig.getBoolean(playerName + ".protection");
        else {
            protection = false;
            playerConfig.set(playerName + ".protection", false);
        }
        return protection;
    }

    public boolean loadBedPerk(String playerName) {
        FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playersFileT);
        boolean bedperk;
        if (playerConfig.contains(playerName + ".bedperk"))
            bedperk = playerConfig.getBoolean(playerName + ".bedperk");
        else {
            bedperk = false;
            playerConfig.set(playerName + ".bedperk", false);
        }
        return bedperk;
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
}

