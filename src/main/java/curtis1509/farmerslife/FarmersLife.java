package curtis1509.farmerslife;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Random;

import static curtis1509.farmerslife.Functions.getPlayer;
import static curtis1509.farmerslife.Functions.message;

public class FarmersLife extends JavaPlugin implements CommandExecutor {

    public static HashMap<String, String> waitingForPlayer = new HashMap<>();
    public static HashMap<String, Location> waitingForPenB = new HashMap<>();
    public static HashMap<String, Integer> animalNames = new HashMap<>();
    public static HashMap<EntityType, Double> animalCost = new HashMap<>();
    public static Inventory menuInventory = Bukkit.createInventory(null, 9, "Farmers Life Menu");
    public static Inventory spawnerInventory = Bukkit.createInventory(null, 9, "Buy Spawners Inventory");
    public static Inventory generalInventory = Bukkit.createInventory(null, 18, "Sally's General Store");
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
    public static String bestPlayerName = "";
    public static int day;
    public static String season;
    public static int seasonLength;
    public static World world;
    public static FileReader fileReader = new FileReader();
    public static boolean shouldDayEndEarly = false;
    public static boolean stormingAllDay;
    public static double bestCashAmount = 0;
    public static DefaultFiles defaultFiles = new DefaultFiles();
    public static Economy economy;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new Events(), this);
        world = getServer().getWorld("world");
        gameloop(this.getServer().getWorld("world"));

        setupEconomy();
        fileReader.FileProcessEnablePlugin();
        populateFarmersCompass();
        Functions.populateBuyInventory();
        Functions.setWeather();
        Functions.initAllPlayers();
    }

    @Override
    public void onDisable() {
        fileReader.FileProcessDisablePlugin();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public void populateFarmersCompass(){
        Functions.addToInventory(menuInventory, Material.WHEAT_SEEDS, "Sally's General Store", "", 0);
        Functions.addToInventory(menuInventory, Material.EXPERIENCE_BOTTLE, "Skills Shop", "", 1);
        Functions.addToInventory(menuInventory, Material.GOLD_INGOT, "Items Shop", "", 2);
        Functions.addToInventory(menuInventory, Material.OAK_FENCE, "Set Selling Pen", "Sell of yer' cattle from an ol' rusty pen", 7);
        Functions.addToInventory(menuInventory, Material.ENCHANTED_BOOK, "Card Pack $5000", "5 Randomly Enchanted Books", 5);
        Functions.addToInventory(menuInventory, Material.CHEST, "Set Deposit Box", "Set your deposit box then throw your harvests in and wait until morning", 8);

    }

    public void gameloop(final World world) {
        getServer().getScheduler().scheduleSyncRepeatingTask((Plugin) this, new Runnable() {
            public void run() {

                long time = world.getTime();

                //Gameplay events
                for (curtis1509.farmerslife.Player player : players) {
                    player.updateScoreboard(Functions.getTime());
                    isPlayerCrossingSellingPenBoundry(player);
                }

                updateWeather(time);

                //Time Events
                if (time > 15000 && Bukkit.getOnlinePlayers().size() > 1)
                    shouldDayEndEarly = endDayEarly();
                else if (time == 19000)
                    broadcastLateMessage();
                else if (time == 20000 || shouldDayEndEarly)
                    endDay();
                else if (time == 5)
                    newDay();

            }
        }, 1, 1);
    }

    public void isPlayerCrossingSellingPenBoundry(Player player) {
        boolean inPen = false;
        for (Pen pen : pens) {
            if (pen.insidePen(player.getPlayer().getLocation())) {
                if (!player.inPen) {
                    message(player.getPlayer(), "You're inside " + pen.owner + "'s selling pen");
                    player.inPen = true;
                }
                inPen = true;
            }
        }
        if (player.inPen && !inPen)
            message(player.player, "You've left the selling pen");
        player.inPen = (inPen);
    }

    public void broadcastLateMessage() {
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            if (!player.isSleeping()) {
                Functions.sendTitle(player, "It's Getting Late", "Zzzzzzz....");
                message(player, "It's getting late you're going to pass out soon... Hurry back to bed!");
            }
        }
    }

    public boolean endDayEarly() {
        int total = 0;
        boolean noOneInbed = true;
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            if (!player.isSleeping()) {
                for (curtis1509.farmerslife.Player p : players) {
                    if (p.getName().equals(player.getName())) {
                        if (p.getSkills().bedperk) {
                            if (Functions.withinRangeOfBed(p))
                                total++;
                        }
                    }
                }
            } else {
                total++;
                noOneInbed = false;
            }
        }
        if (total == Bukkit.getOnlinePlayers().size() && !noOneInbed)
            return true;
        return false;
    }

    public void updateWeather(long time) {
        if (season.equals("Wet") && stormingAllDay) {
            if (time == 19000 || time == 18000 || time == 17000 || time == 16000 || time == 15000 || time == 14000 || time == 13000 || time == 12000 || time == 11000 || time == 10000 || time == 9000 || time == 8000 || time == 7000 || time == 6000 || time == 5000 || time == 4000 || time == 3000 || time == 2000 || time == 1000) {
                Random random = new Random();
                if (world.hasStorm())
                    world.setStorm(random.nextInt(10) > 2);
                else
                    world.setStorm(random.nextInt(5) > 2);
            }
        }
    }

    public void processSellingPens() {
        for (Entity mob : world.getEntities()) {
            if (mob.getCustomName() != null && mob.getType() != EntityType.PLAYER) {
                for (Pen pen : pens) {
                    if (pen.insidePen(mob.getLocation())) {
                        economy.depositPlayer(pen.owner, Functions.calculateAnimalPayout(mob, Functions.getPlayer(pen.owner)));
                        Functions.getPlayer(pen.owner).addToTodaysCash(Functions.calculateAnimalPayout(mob, Functions.getPlayer(pen.owner)));
                        mob.remove();
                    }
                }
            }
        }
    }

    public void processDepositBoxes() {
        for (DepositBox box : depositBoxes) {
            if (box.getDepositBox().getType() == Material.CHEST) {
                Chest chest = (Chest) box.getDepositBox().getState();
                for (curtis1509.farmerslife.Player player : players) {
                    if (box.getOwner().equals(player.getName())) {

                        try {
                            economy.depositPlayer(player.getPlayer(), Functions.getAmountOfCash(depositCrops, chest.getInventory(), player.getName()) * player.getSkills().skillProfits.getMultiplier());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Thread wait = new Thread(() -> {
                        try {
                            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_CHICKEN_AMBIENT, 3, 1);
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_CHICKEN_DEATH, 3, 1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    wait.start();
                }

                LinkedList<ItemStack> deliveryItems = new LinkedList<>();
                for (ItemStack delivery : chest.getInventory().getContents()) {
                    if (delivery != null) {
                        if (delivery.getItemMeta().getDisplayName().contains("Shop"))
                            deliveryItems.add(delivery);
                    }
                }

                processMilkBuckets(chest);

                for (ItemStack itemStack : deliveryItems)
                    chest.getInventory().addItem(itemStack);

                if (box.isShipmentBox() && Functions.getPlayer(box.getOwner()) != null) {
                    boolean itemsDelivered = false;
                    for (ItemStack item : Functions.getPlayer(box.getOwner()).getDeliveryOrder()) {
                        itemsDelivered = true;
                        chest.getInventory().addItem(item);
                    }
                    Functions.getPlayer(box.getOwner()).getDeliveryOrder().clear();
                    if (itemsDelivered)
                        message(Functions.getPlayer(box.getOwner()).getPlayer(), "Your delivery has arrived inside your shipment box");
                }
            }
        }
    }

    public void announceMostProfitableFarmer() {

        for (curtis1509.farmerslife.Player player : players) {
            player.golemIronSoldToday = 0;
            if (bestPlayerName.equals("")) {
                bestPlayerName = player.getName();
                bestCashAmount = player.getTodaysCash();
            } else {
                if (player.getTodaysCash() > bestCashAmount) {
                    bestCashAmount = player.getTodaysCash();
                    bestPlayerName = player.getName();
                }
            }
            message(player.getPlayer(), "You made $" + (int) Math.floor(player.getTodaysCash()) + " yesterday");
            player.resetTodaysCash();
        }

        if (!bestPlayerName.equals("") && bestCashAmount > 0)
            Functions.broadcast("Well done to " + bestPlayerName + " for making the most money yesterday for a total of $" + (int) Math.floor(bestCashAmount));
        bestPlayerName = "";
        bestCashAmount = 0;
    }

    public static void broadcastTitle(String upper, String lower) {
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            if (world.hasStorm()) {
                player.sendTitle(net.md_5.bungee.api.ChatColor.GOLD + upper, net.md_5.bungee.api.ChatColor.BLUE + lower);
            } else {
                player.sendTitle(net.md_5.bungee.api.ChatColor.GOLD + upper, ChatColor.BLUE + lower);
            }
        }
    }


    public void newDay() {

        for (String key : animalNames.keySet()) {
            animalNames.replace(key, animalNames.get(key) + 1);
        }

        //Set the weather and announce the new day and weather
        Functions.setWeather();

        processSellingPens();
        processDepositBoxes();
        announceMostProfitableFarmer();
        Functions.reloadShop();
        fileReader.FileProcessNewDay();

    }

    //Milk Bucket Exchange. Takes milk buckets in deposit box (chest), processes them, and then returns empty buckets to the deposit box.
    public void processMilkBuckets(Chest chest) {
        LinkedList<DepositCrop> milkBuckets = new LinkedList<>();
        milkBuckets.add(new DepositCrop(Material.MILK_BUCKET, 0));
        milkBuckets.add(new DepositCrop(Material.BUCKET, 0));
        int buckets = Functions.getAmountOf(milkBuckets, chest.getInventory());
        chest.getInventory().clear();
        chest.getInventory().addItem(new ItemStack(Material.BUCKET, buckets));
    }

    public void endDay() {
        shouldDayEndEarly = false; //This is to reset the alternate condition where new day can be started with everyone in bed.
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            if (!player.isSleeping() && playerShouldPassOut(player))
                passOut(player);
        }
        punishPlayersForLoggingOutAtBedtime();
        world.setTime(0);

    }

    public boolean playerShouldPassOut(org.bukkit.entity.Player player) {
        if (getPlayer(player).getSkills().bedperk) {
            if (Functions.withinRangeOfBed(getPlayer(player)))
                return false;
        }
        return true;
    }

    public void passOut(org.bukkit.entity.Player player) {
        Location location = player.getBedSpawnLocation();
        if (location != null)
            player.teleport(player.getBedSpawnLocation());
        else
            player.teleport(world.getSpawnLocation());

        int withdrawAmount = (int) Math.floor(economy.getBalance(player) * 0.1);
        if (withdrawAmount > 1000)
            withdrawAmount = 1000;
        economy.withdrawPlayer(player, withdrawAmount);

        message(player, "Oh no! You passed out.");
        message(player, "We saw some people rummaging through your pockets, they stole $" + withdrawAmount);
        player.setFoodLevel(3);
    }

    public void punishPlayersForLoggingOutAtBedtime() {
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            if (punishLogout.contains(player.getName())) {
                int withdrawAmount = (int) Math.floor(economy.getBalance(player) * 0.15);
                if (withdrawAmount > 2000)
                    withdrawAmount = 2000;
                economy.withdrawPlayer(player, withdrawAmount);
            }
        }
        punishLogout.clear();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("box")) {
            if (!waitingForPlayer.containsKey(sender.getName())) {
                waitingForPlayer.put(sender.getName(), "box");
                message(sender, "Great! Click a chest to make it your deposit box.");
                Thread wait = new Thread(() -> {
                    try {
                        Thread.sleep(10000);
                        if (waitingForPlayer.get(sender.getName()).equals("box")) {
                            message(sender, "You took too long to select a chest. Try again!");
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
            message(sender, "Shop Reloaded");
        } else if (cmd.getName().equalsIgnoreCase("pen") && !waitingForPlayer.containsKey(sender.getName())) {
            message(sender, "Left click a block to set the first corner in your Selling Pen");
            waitingForPlayer.put(sender.getName(), "pen");
            Thread wait = new Thread(() -> {
                try {
                    Thread.sleep(20000);
                    if (waitingForPlayer.get(sender.getName()).equals("pen")) {
                        message(sender, "You took too long to select a corner. Try again!");
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
                    message(sender, "You've removed your Selling Pen");
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