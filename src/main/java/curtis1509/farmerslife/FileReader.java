package curtis1509.farmerslife;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

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

    public void CreateFile() {
        try {
            Files.createDirectories(Paths.get("plugins/FarmersLife"));
            File playersFile = new File("plugins/FarmersLife/players.txt");
            File depositsFile = new File("plugins/FarmersLife/deposits.txt");
            playersFile.createNewFile();
            depositsFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double loadPlayerCash(String playerName) {
        FileReader fileReader = new FileReader();
        String dataIn = fileReader.read("plugins/FarmersLife/players.txt");
        String[] data = dataIn.split(" ");
        for (int i = 0; i < data.length; i++) {
            if (data[i].equals(playerName))
                return Double.parseDouble(data[i + 1]);
        }
        return 100;
    }

    public boolean playerExists(String playerName) {
        FileReader fileReader = new FileReader();
        String dataIn = fileReader.read("plugins/FarmersLife/players.txt");
        String[] data = dataIn.split(" ");
        for (int i = 0; i < data.length; i++) {
            if (data[i].equals(playerName))
                return true;
        }
        return false;
    }

    public void updatePlayerData(String playerName, double cash, Skills skill) throws IOException {
        String filePath = "plugins/FarmersLife/players.txt";
        Scanner sc = new Scanner(new File(filePath));
        StringBuffer buffer = new StringBuffer();
        while (sc.hasNextLine()) {
            buffer.append(sc.nextLine());
            if (sc.hasNextLine())
                buffer.append(System.lineSeparator());
        }
        String fileContents = buffer.toString();
        sc.close();

        String oldLine = playerName + " " + loadPlayerCash(playerName) + " " + loadPlayerSkillProfits(playerName);
        String newLine = playerName + " " + cash + " " + skill.skillProfits.getLevel();
        fileContents = fileContents.replace(oldLine, newLine);
        FileWriter writer = new FileWriter(filePath);
        writer.append(fileContents);
        writer.flush();
        getLogger().info("updating player data");

    }

    public int loadPlayerSkillProfits(String playerName) {
        FileReader fileReader = new FileReader();
        String dataIn = fileReader.read("plugins/FarmersLife/players.txt");
        String[] data = dataIn.split(" ");
        for (int i = 0; i < data.length; i++) {
            if (data[i].equals(playerName))
                return Integer.parseInt(data[i + 2]);
        }
        return 0;
    }

    public void removeDepositData(DepositBox depositBox) throws IOException {
        String filePath = "plugins/FarmersLife/deposits.txt";
        Scanner sc = new Scanner(new File(filePath));
        StringBuffer buffer = new StringBuffer();
        while (sc.hasNextLine()) {
            buffer.append(sc.nextLine());
            if (sc.hasNextLine())
                buffer.append(System.lineSeparator());
        }
        String fileContents = buffer.toString();
        sc.close();

        String oldLine = " " + depositBox.getOwner() + " " + depositBox.getID() + " " + depositBox.getDepositBox().getLocation().getBlockX() + " "+ depositBox.getDepositBox().getLocation().getBlockY() + " " + depositBox.getDepositBox().getLocation().getBlockZ();
        String newLine = "";
        fileContents = fileContents.replace(oldLine, newLine);
        FileWriter writer = new FileWriter(filePath);
        writer.append(fileContents);
        writer.flush();
        FarmersLife.depositBoxes.remove(depositBox);
    }

    public void savePlayers() {
        CreateFile();
        try {
            FileWriter myWriter = null;
            BufferedWriter bw = null;
            PrintWriter pw = null;
            for (curtis1509.farmerslife.Player player : FarmersLife.players) {
                if (!playerExists(player.getPlayer().getName())) {
                    myWriter = new FileWriter("plugins/FarmersLife/players.txt", true);
                    bw = new BufferedWriter(myWriter);
                    pw = new PrintWriter(bw);
                    pw.print(" " + player.getPlayer().getName() + " " + player.getCash() + " " +player.getSkills().skillProfits.getLevel());
                    pw.flush();
                    myWriter.close();
                    pw.close();
                    bw.close();
                    myWriter.close();
                } else {
                    updatePlayerData(player.getName(), player.getCash(), player.getSkills());
                }
            }
            myWriter = new FileWriter("plugins/FarmersLife/deposits.txt", true);
            bw = new BufferedWriter(myWriter);
            pw = new PrintWriter(bw);
            for (DepositBox deposits : FarmersLife.depositBoxes) {
                if (!depositExists(deposits.getID())) {
                    pw.print(" " + deposits.getOwner() + " " + deposits.getID() + " " + deposits.getDepositBox().getLocation().getBlockX() + " " + deposits.getDepositBox().getLocation().getBlockY() + " " + deposits.getDepositBox().getLocation().getBlockZ());
                }
            }
            pw.flush();
            myWriter.close();
            pw.close();
            bw.close();
            myWriter.close();

        } catch (IOException e) {
            e.printStackTrace();


        }
    }

    public void loadDeposits() {
        FileReader fileReader = new FileReader();
        String dataIn = fileReader.read("plugins/FarmersLife/deposits.txt");
        String[] data = dataIn.split(" ");
        for (int i = 1; i < data.length; i++) {
            String playerName = data[i];
            i++;
            int id = Integer.parseInt(data[i]);
            i++;
            Location location = new Location(FarmersLife.world, Integer.parseInt(data[i]), Integer.parseInt(data[i + 1]), Integer.parseInt(data[i + 2]));
            i += 2;
            Block chest = location.getBlock();
            FarmersLife.depositBoxes.add(new DepositBox(chest, playerName, id));
            if (i > data.length - 3)
                break;
        }
    }


    public boolean depositExists(int id) {
        FileReader fileReader = new FileReader();
        String dataIn = fileReader.read("plugins/FarmersLife/deposits.txt");
        String[] data = dataIn.split(" ");
        for (int i = 0; i < data.length; i++) {
            if (data[i].equals(Integer.toString(id)))
                return true;
        }
        return false;
    }

}
