package curtis1509.farmerslife;

import org.apache.commons.lang.ObjectUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.Set;

import static org.bukkit.Bukkit.getLogger;

public class FileReader {

    //Just loads the entire file into a string to be later split down in the main class.
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

    public void CreateFile() {
        try {
            Files.createDirectories(Paths.get("plugins/FarmersLife"));
            playersFileT.createNewFile();
            depositsFileT.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createBackup() throws IOException {
        Files.createDirectories(Paths.get("plugins/FarmersLife/Backups"));
        String date = java.util.Calendar.getInstance().getTime().getSeconds() + "_secs_" + java.util.Calendar.getInstance().getTime().getHours() + "_hours_" + java.util.Calendar.getInstance().getTime().getMinutes() + "mins_" + java.util.Calendar.getInstance().getTime().getDay() + "_" + java.util.Calendar.getInstance().getTime().getMonth() + "_" + java.util.Calendar.getInstance().getTime().getYear();

        File playersFileBackup = new File("plugins/FarmersLife/Backups/players" + date + ".yml");
        Files.copy(playersFileT.toPath(), playersFileBackup.toPath(), StandardCopyOption.COPY_ATTRIBUTES);

        File depositsFileBackup = new File("plugins/FarmersLife/Backups/deposits" + date + ".yml");
        Files.copy(depositsFileT.toPath(), depositsFileBackup.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
    }

    public double loadPlayerCash(String playerName) {
        FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playersFileT);
        double cash;
        if (playerConfig.contains(playerName + ".cash"))
            cash = playerConfig.getDouble(playerName + ".cash");
        else
            cash = 100;
        return cash;
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
            for (curtis1509.farmerslife.Player player : FarmersLife.players) {
                playerConfig.set(player.getName() + ".cash", player.getCash());
                playerConfig.set(player.getName() + ".profit", player.getSkills().skillProfits.getLevel());
            }
            playerConfig.save(playersFileT);

            FileConfiguration depositConfig = YamlConfiguration.loadConfiguration(depositsFileT);
            for (DepositBox deposits : FarmersLife.depositBoxes) {
                String id = "deposits." + deposits.getID();
                depositConfig.set(id + ".owner", deposits.getOwner());
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
                Block chest = location.getBlock();
                FarmersLife.depositBoxes.add(new DepositBox(chest, owner, id));
            }
        } catch (NullPointerException e) {
            getLogger().info("Deposit Box File has no contents");
        }
    }

}

