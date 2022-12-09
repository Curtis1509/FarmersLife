package curtis1509.farmerslife;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static org.bukkit.Bukkit.getLogger;

public class Functions implements CommandExecutor {

    public static HashMap<String, String> waitingForPlayer = new HashMap<>();
    public static HashMap<String, Location> waitingForPenB = new HashMap<>();
    public static HashMap<String, Integer> animalNames = new HashMap<>();
    public static HashMap<EntityType, Double> animalCost = new HashMap<>();
    public static Inventory menuInventory = Bukkit.createInventory(null, 9, "Farmers Life Menu");
    public static Inventory spawnerInventory = Bukkit.createInventory(null, 9, "Buy Spawners Inventory");
    public static Inventory seedsInventory = Bukkit.createInventory(null, 18, "Seeds");
    public static Inventory buyInventory = Bukkit.createInventory(null, 9, "Buy");
    public static Inventory buyInventory2 = Bukkit.createInventory(null, 54, "Buy");
    public static LinkedList<DepositCrop> depositCrops = new LinkedList<>();
    public static LinkedList<curtis1509.farmerslife.Player> players = new LinkedList<>();
    public static LinkedList<BuyItem> buyItems = new LinkedList<>();
    public static LinkedList<String> interactQueue = new LinkedList<>();
    public static LinkedList<String> punishLogout = new LinkedList<>();
    public static LinkedList<DepositBox> depositBoxes = new LinkedList<>();
    public static LinkedList<Pen> pens = new LinkedList<>();
    public static LinkedList<Location> shopBlockLocations = new LinkedList<>();
    public static String weather = "Wet";
    public static String bestPlayerName = "";
    public static long day;
    public static int dayNumber;
    public static World world;
    public static FileReader fileReader = new FileReader();
    public static boolean sleep = false;
    public static boolean stormingAllDay;
    public static double bestCashAmount = 0;
    public static DefaultFiles defaultFiles = new DefaultFiles();
    public static Economy economy;

    public static double calculateAnimalPayout(Entity e, curtis1509.farmerslife.Player player) {
        double multiplier = animalNames.get(e.getCustomName()) * 0.15;
        double payout = animalCost.get(e.getType()) + (animalCost.get(e.getType()) * multiplier);
        payout = payout * player.getSkills().skillProfits.getMultiplier();
        return payout;
    }

    public static boolean isDepositBox(Location location) {
        for (DepositBox box : depositBoxes) {
            if (box.getDepositBox().getLocation().equals(location)) {
                return true;
            }
        }
        return false;
    }

    public static DepositBox getDepositBox(Location location) {
        for (DepositBox box : depositBoxes) {
            if (box.getDepositBox().getLocation().equals(location)) {
                return box;
            }
        }
        return null;
    }

    public static Material getMaterial(String string) {
        for (Material material : Material.values()) {
            if (material.name().equals(string)) {
                return material;
            }
        }
        return null;
    }

    public static void loadSpawnerShop() {
        ItemStack item = new ItemStack(Material.SPAWNER, 1);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName("Zombie Spawner | $15,000");
        item.setItemMeta(itemMeta);
        spawnerInventory.setItem(1, item);
        BlockStateMeta blockMeta = (BlockStateMeta) Objects.requireNonNull(spawnerInventory.getItem(1)).getItemMeta();
        CreatureSpawner spawner = (CreatureSpawner) blockMeta.getBlockState();
        spawner.setSpawnedType(EntityType.ZOMBIE);
        blockMeta.setBlockState(spawner);
        item.setItemMeta(blockMeta);
        spawnerInventory.setItem(1, item);
    }

    public static void loadDepositShop() throws IOException {
        FileReader fileReader = new FileReader();
        String dataIn = "";
        try {
            dataIn = fileReader.read("plugins/FarmersLife/depositShop.txt");
        } catch (Exception e) {
            FileWriter writer = new FileWriter("plugins/FarmersLife/depositShop.txt");
            writer.write(defaultFiles.defaultDepositShop);
            writer.close();
            dataIn = fileReader.read("plugins/FarmersLife/depositShop.txt");
        }
        String[] data = dataIn.split(" ");
        for (int i = 1; i < data.length; i++) {
            String matName = data[i];
            Material material = getMaterial(data[i]);
            i++;
            double price = Double.parseDouble(data[i]);
            if (material != null)
                depositCrops.add(new DepositCrop(material, price));
            else
                getLogger().info(matName + " could not be identified");
        }
    }

    public static void loadSeedsShop() throws IOException {
        FileReader fileReader = new FileReader();
        String dataIn = "";
        try {
            dataIn = fileReader.read("plugins/FarmersLife/seedsShop.txt");
        } catch (Exception e) {
            FileWriter writer = new FileWriter("plugins/FarmersLife/seedsShop.txt");
            writer.write(defaultFiles.defaultSeedsShop);
            writer.close();
            dataIn = fileReader.read("plugins/FarmersLife/seedsShop.txt");
        }
        String[] data = dataIn.split(" ");
        for (int i = 1; i < data.length; i++) {
            String matName = data[i];
            Material material = getMaterial(data[i]);
            i++;
            int price = Integer.parseInt(data[i]);
            i++;
            int amount = Integer.parseInt(data[i]);
            if (material != null)
                addToInventory(seedsInventory, material, price, amount);
            else
                getLogger().info(matName + " could not be identified");
        }
    }

    public static void populateBuyInventory() {
        try {
            fileReader.loadBuyShop();
            loadSeedsShop();
        } catch (IOException e) {
            System.out.println("Failed To Load Shops");
        }
        loadSpawnerShop();
    }

    public static void addToInventory(Inventory inventory, Material material, int price, int amount) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setLore(Collections.singletonList("$" + price));
        item.setItemMeta(itemMeta);
        inventory.addItem(item);
        buyItems.add(new BuyItem(material, price, amount));
    }

    public static void addToInventory(Inventory inventory, Material material, int price, int amount, boolean isSpecial, int deduction) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta itemMeta = item.getItemMeta();
        List<String> lore = new ArrayList<String>();
        lore.add("$" + price);
        if (isSpecial) {
            lore.add("-----------------");
            lore.add("SPECIAL");
            lore.add("Original Price: $" + (price + deduction));
        }
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        inventory.addItem(item);
        buyItems.add(new BuyItem(material, price, amount));
    }

    public static void addToInventory(Inventory inventory, Material material, String name, String text, int index) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(name);
        itemMeta.setLore(Collections.singletonList(text));
        item.setItemMeta(itemMeta);
        inventory.setItem(index, item);
    }

    public static void giveCompass(org.bukkit.entity.Player player) {
        player.getInventory().setItem(8, new ItemStack(Material.COMPASS, 1));
        ItemMeta compassMeta = player.getInventory().getItem(8).getItemMeta();
        compassMeta.setDisplayName("Farmers Compass");
        compassMeta.setLore(Collections.singletonList("Access the Farmers Main Menu"));
        player.getInventory().getItem(8).setItemMeta(compassMeta);
    }

    public static void initAllPlayers() {
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            initPlayer(player);
        }
    }

    public static void initPlayer(org.bukkit.entity.Player player) {
        boolean playerExists = false;
        for (curtis1509.farmerslife.Player fPlayer : players) {
            if (fPlayer.getPlayer().getName().equals(player.getName())) {
                playerExists = true;
                fPlayer.setPlayer(player);
                fPlayer.reloadScoreboard();
                fPlayer.scoreboard.updateScoreboard();
                fPlayer.reloadPlayerName();
            }
        }
        player.sendTitle(ChatColor.GOLD + "Farmers Life", ChatColor.BLUE + weather + " Season");
        if (!playerExists)
            players.add(new curtis1509.farmerslife.Player(player, fileReader.loadPlayerSkillProfits(player.getName()), fileReader.loadPerk(player.getName(), "protection"), fileReader.loadPerk(player.getName(), "bedperk"), fileReader.loadPerk(player.getName(), "teleport")));

        if (world.hasStorm())
            player.sendMessage("We are currently in a " + weather + " weather season. It is storming today");
        else
            player.sendMessage("We are currently in a " + weather + " weather season. It is sunny today");

        player.getInventory().setItem(8, new ItemStack(Material.COMPASS, 1));
        ItemMeta compassMeta = player.getInventory().getItem(8).getItemMeta();
        compassMeta.setDisplayName("Farmers Compass");
        compassMeta.setLore(Collections.singletonList("Farmers main menu"));
        player.getInventory().getItem(8).setItemMeta(compassMeta);
        punishLogout.remove(player.getName());
    }

    public static LinkedList<DepositBox> getDepositBoxes(org.bukkit.entity.Player player) {
        LinkedList<DepositBox> boxes = new LinkedList<>();
        for (DepositBox box : depositBoxes) {
            if (box.getOwner().equals(player.getName())) {
                boxes.add(box);
            }
        }
        return boxes;
    }

    public static DepositBox getDeliveryBox(org.bukkit.entity.Player player) {
        for (DepositBox box : depositBoxes) {
            if (box.getOwner().equals(player.getName())) {
                if (box.isShipmentBox()) {
                    return box;
                }
            }
        }
        return null;
    }

    public static void giveCardPack(org.bukkit.entity.Player player) {
        for (int i = 0; i < 5; i++) {
            ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
            assert meta != null;
            Random random = new Random();
            Enchantment[] values = Enchantment.values();
            Enchantment enchantment = values[random.nextInt(values.length)];
            meta.addStoredEnchant(enchantment, random.nextInt(enchantment.getMaxLevel()) + 1, true);
            book.setItemMeta(meta);
            player.getInventory().addItem(book);
        }
    }

    public static boolean containsItem(LinkedList<DepositCrop> depositCrops, ItemStack itemStack) {
        for (DepositCrop crop : depositCrops) {
            if (crop.getMaterial() == itemStack.getType()) {
                return true;
            }
        }
        return false;
    }

    public static DepositCrop getCropFromList(LinkedList<DepositCrop> depositCrops, ItemStack itemStack) {
        for (DepositCrop crop : depositCrops) {
            if (crop.getMaterial() == itemStack.getType()) {
                return crop;
            }
        }
        return null;
    }

    public static int getAmountOf(LinkedList<DepositCrop> depositCrops, Inventory inventory) {
        int i = 0;
        for (ItemStack is : inventory.getContents()) {
            if (is != null) {
                if (containsItem(depositCrops, is)) {
                    i += is.getAmount();
                }
            }
        }
        return i;
    }

    public static double nerfIncome(ItemStack itemStack, String owner) {
        double money = 0;
        if (itemStack.getItemMeta().getDisplayName().contains("GOLEM")) {
            double itemWorth = getCropFromList(depositCrops, itemStack).getReward();
            int golemIronSoldToday = getPlayer(owner).golemIronSoldToday;
            for (int i = 0; i < itemStack.getAmount(); i++) {
                double localMoney = itemWorth;
                for (int j = 0; j < golemIronSoldToday; j++) {
                    if (itemStack.getType() == Material.IRON_BLOCK)
                        localMoney *= 0.91;
                    else
                        localMoney *= 0.99;
                }
                money += localMoney;
                golemIronSoldToday++;
            }
            getPlayer(owner).golemIronSoldToday += golemIronSoldToday;
            getLogger().info("Total For All Golem Iron: " + money);
        }
        return money;
    }

    public static double getAmountOfCash(LinkedList<DepositCrop> depositCrops, Inventory inventory, String owner) throws IOException {
        double i = 0;
        for (ItemStack is : inventory.getContents()) {
            if (is != null) {
                if (containsItem(depositCrops, is) && !Objects.requireNonNull(is.getItemMeta()).getDisplayName().contains("Shop") && !Objects.requireNonNull(is.getItemMeta()).getDisplayName().contains("Immature")) {
                    double itemMoney;
                    if (!is.getItemMeta().getDisplayName().contains("GOLEM"))
                        itemMoney = (is.getAmount() * getCropFromList(depositCrops, is).getReward());
                    else {
                        itemMoney = nerfIncome(is, owner);
                    }
                    i += itemMoney;
                    fileReader.addStat(is.getType().name(), itemMoney);
                }
            }
        }
        return i;
    }

    public static void sendClickableCommand(org.bukkit.entity.Player player, String message, String command) {
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command));
        player.spigot().sendMessage(component);
    }

    public static String getTime() {
        int hours;
        int mins;
        double ticks = (int) world.getTime();
        String ampm = "";
        if (world.getTime() >= 1000 && world.getTime() < 6000) {
            hours = (int) (6 + Math.floor(ticks / 1000));
            mins = (int) Math.floor((60 / (1000 / (ticks % 1000))));
            ampm = "AM";
        } else if (world.getTime() >= 6000 && world.getTime() < 18000) {
            hours = (int) (Math.floor(ticks / 1000)) - 6;
            mins = (int) Math.floor((60 / (1000 / (ticks % 1000))));
            ampm = "PM";
        } else if (world.getTime() >= 18000 && world.getTime() < 23999) {
            hours = (int) (Math.floor(ticks / 1000)) - 18;
            mins = (int) Math.floor((60 / (1000 / (ticks % 1000))));
            ampm = "AM";
        } else {
            hours = 6;
            mins = (int) Math.floor((60 / (1000 / (ticks % 1000))));
            ampm = "AM";
        }
        if (hours == 0)
            hours = 12;
        if (mins < 10)
            return hours + ":0" + mins + ampm;
        return hours + ":" + mins + ampm;
    }

    public static void setWeather() {
        Random random = new Random();
        int r = random.nextInt(10);
        if (weather.equals("Wet")) {
            world.setStorm(r < 5);
            stormingAllDay = world.hasStorm();
        } else {
            world.setStorm(r > 8);
        }
    }

    public static void broadcast(String message) {
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    public static boolean withinRangeOfBed(curtis1509.farmerslife.Player player) {
        int x = player.getPlayer().getLocation().getBlockX();
        int y = player.getPlayer().getLocation().getBlockY();
        int z = player.getPlayer().getLocation().getBlockZ();

        int bx = Objects.requireNonNull(player.getPlayer().getBedSpawnLocation()).getBlockX();
        int by = Objects.requireNonNull(player.getPlayer().getBedSpawnLocation()).getBlockY();
        int bz = Objects.requireNonNull(player.getPlayer().getBedSpawnLocation()).getBlockZ();

        double distance = Math.sqrt(Math.pow(x - bx, 2) + Math.pow(y - by, 2) + Math.pow(z - bz, 2));
        return distance <= 100;
    }

    public static curtis1509.farmerslife.Player getPlayer(org.bukkit.entity.Player player) {
        for (curtis1509.farmerslife.Player p : players) {
            if (p.getPlayer() == player) {
                return p;
            }
        }
        return null;
    }

    public static curtis1509.farmerslife.Player getPlayer(String player) {
        for (curtis1509.farmerslife.Player p : players) {
            if (p.getPlayer().getName().equals(player)) {
                return p;
            }
        }
        return null;
    }

    public static void reloadShop() {
        buyInventory.clear();
        buyInventory2.clear();
        buyItems.clear();
        animalCost.clear();
        fileReader.loadBuyShop();
        fileReader.loadAnimalCosts();
        depositCrops.clear();
        seedsInventory.clear();
        try {
            loadDepositShop();
            loadSeedsShop();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("box")) {
            if (!waitingForPlayer.containsKey(sender.getName())) {
                waitingForPlayer.put(sender.getName(), "box");
                sender.sendMessage("Great! Click a chest to make it your deposit box.");
                Thread wait = new Thread(() -> {
                    try {
                        Thread.sleep(10000);
                        if (waitingForPlayer.get(sender.getName()).equals("box")) {
                            sender.sendMessage("You took too long to select a chest. Try again!");
                            waitingForPlayer.remove(sender.getName());
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                wait.start();
                return true;
            }

        } else if (cmd.getName().equalsIgnoreCase("farm")) {
            Objects.requireNonNull(Bukkit.getPlayer(sender.getName())).openInventory(menuInventory);
        } else if (cmd.getName().equalsIgnoreCase("reloadscores")) {
            for (curtis1509.farmerslife.Player p : players) {
                p.reloadScoreboard();
            }
        } else if (cmd.getName().equalsIgnoreCase("deathinventory") || cmd.getName().equalsIgnoreCase("di")) {
            for (curtis1509.farmerslife.Player player : players) {
                if (player.getName().equals(sender.getName()) && player.getDeathInventory() != null) {
                    Objects.requireNonNull(Bukkit.getPlayer(sender.getName())).openInventory(player.getDeathInventory());
                }
            }
        } else if (cmd.getName().equalsIgnoreCase("reloadshop")) {
            Functions.reloadShop();
            sender.sendMessage("Shop Reloaded");
        } else if (cmd.getName().equalsIgnoreCase("pen") && !waitingForPlayer.containsKey(sender.getName())) {
            sender.sendMessage("Left click the first corner of your selling pen");
            waitingForPlayer.put(sender.getName(), "pen");
            Thread wait = new Thread(() -> {
                try {
                    Thread.sleep(20000);
                    if (waitingForPlayer.get(sender.getName()).equals("pen")) {
                        sender.sendMessage("You took too long to select a pen. Try again!");
                        waitingForPlayer.remove(sender.getName());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            wait.start();
            return true;
        } else if (cmd.getName().equalsIgnoreCase("deletepen")) {
            Pen deletePen = null;
            for (Pen pen : pens) {
                if (pen.insidePen(Functions.getPlayer(sender.getName()).getPlayer().getLocation())) {
                    deletePen = pen;
                    sender.sendMessage("You've removed your pen");
                }
            }
            if (deletePen != null) {
                try {
                    pens.remove(deletePen);
                    fileReader.removePenData(deletePen);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return true;
        }
        return false;
    }
}
