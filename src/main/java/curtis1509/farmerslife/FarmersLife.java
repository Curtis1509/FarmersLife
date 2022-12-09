package curtis1509.farmerslife;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

public class FarmersLife extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {

        getServer().getPluginManager().registerEvents(new Events(), this);
        Functions.world = getServer().getWorld("world");
        update(this.getServer().getWorld("world"));

        setupEconomy();
        getLogger().info("Farmers Life has been enabled!");
        Functions.fileReader.CreateFile();
        try {
            Functions.loadDepositShop();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Functions.fileReader.loadDeposits();
        Functions.fileReader.loadPens();
        Functions.fileReader.loadNametags();

        Functions.addToInventory(Functions.menuInventory, Material.WHEAT_SEEDS, "Seeds Shop", "", 0);
        Functions.addToInventory(Functions.menuInventory, Material.EXPERIENCE_BOTTLE, "Skills Shop", "", 1);
        Functions.addToInventory(Functions.menuInventory, Material.GOLD_INGOT, "Items Shop", "", 2);
        Functions.addToInventory(Functions.menuInventory, Material.OAK_FENCE, "Set Selling Pen", "Sell of yer' cattle from an ol' rusty pen", 7);
        Functions.addToInventory(Functions.menuInventory, Material.ENCHANTED_BOOK, "Card Pack $5000", "5 Randomly Enchanted Books", 5);
        Functions.addToInventory(Functions.menuInventory, Material.CHEST, "Set Deposit Box", "Set your deposit box then throw your harvests in and wait until morning", 8);

        try {
            Functions.fileReader.getWeather();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Functions.populateBuyInventory();
        Functions.setWeather();
        Functions.fileReader.loadAnimalCosts();
        Functions.initAllPlayers();
    }

    @Override
    public void onDisable() {
        Functions.fileReader.savePlayers();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        Functions.economy = rsp.getProvider();
        return Functions.economy != null;
    }


    public void update(final World world) {
        getServer().getScheduler().scheduleSyncRepeatingTask((Plugin) this, new Runnable() {
            public void run() {
                for (curtis1509.farmerslife.Player player : Functions.players) {
                    player.updateScoreboard(Functions.getTime());
                    boolean inPen = false;
                    for (Pen pen : Functions.pens) {
                        if (pen.insidePen(player.getPlayer().getLocation())) {
                            if (!player.inPen) {
                                player.getPlayer().sendMessage("You're inside " + pen.owner + "'s selling pen");
                                player.inPen = true;
                            }
                            inPen = true;
                        }
                    }
                    if (player.inPen && !inPen)
                        player.player.sendMessage("You've left the selling pen");
                    player.inPen = (inPen);
                }

                long time = world.getTime();
                if (time > 15000 && Bukkit.getOnlinePlayers().

                        size() > 1) {
                    int total = 0;
                    boolean noOneInbed = true;
                    for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                        if (!player.isSleeping()) {
                            for (curtis1509.farmerslife.Player p : Functions.players) {
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
                        Functions.sleep = true;

                }
                if (Functions.weather.equals("Wet") && Functions.stormingAllDay)
                    if (time == 19000 || time == 18000 || time == 17000 || time == 16000 || time == 15000 || time == 14000 || time == 13000 || time == 12000 || time == 11000 || time == 10000 || time == 9000 || time == 8000 || time == 7000 || time == 6000 || time == 5000 || time == 4000 || time == 3000 || time == 2000 || time == 1000) {
                        Random random = new Random();
                        if (world.hasStorm())
                            world.setStorm(random.nextInt(10) > 2);
                        else
                            world.setStorm(random.nextInt(5) > 2);
                    }
                if (time == 19000) {
                    for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                        if (!player.isSleeping()) {
                            player.sendTitle(net.md_5.bungee.api.ChatColor.BLUE + "It's Getting Late", net.md_5.bungee.api.ChatColor.GRAY + " Zzzzzz...");
                            player.sendMessage("It's getting late you're going to pass out soon... Hurry back to bed!");
                        }
                    }
                }
                if (time == 20000 || Functions.sleep) {
                    Functions.sleep = false;
                    for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                        if (!player.isSleeping()) {
                            int cash = 0;
                            boolean passOut = true;
                            for (curtis1509.farmerslife.Player p : Functions.players) {
                                if (p.getName().equals(player.getName())) {
                                    cash = (int) Math.floor(p.getCash() * 0.1);
                                    if (cash > 1000) {
                                        cash = 1000;
                                    }
                                    if (p.getSkills().bedperk) {
                                        if (Functions.withinRangeOfBed(p))
                                            passOut = false;
                                    }
                                    if (passOut)
                                        p.removeCash(cash);
                                }
                            }
                            if (passOut) {
                                Location l = player.getBedSpawnLocation();
                                if (l != null)
                                    player.teleport(player.getBedSpawnLocation());
                                else
                                    player.teleport(world.getSpawnLocation());

                                player.sendMessage("Oh no! You passed out.");
                                player.sendMessage("We saw some people rummaging through your pockets, they stole $" + cash);
                                player.setFoodLevel(3);
                                Functions.giveCompass(player);
                            }
                        }
                    }
                    for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                        if (Functions.punishLogout.contains(player.getName())) {
                            int cash = 0;
                            for (curtis1509.farmerslife.Player p : Functions.players) {
                                if (p.getName().equals(player.getName())) {
                                    cash = (int) Math.floor(p.getCash() * 0.15);
                                    if (cash > 2000) {
                                        cash = 2000;
                                    }
                                    p.removeCash(cash);
                                }
                            }
                        }
                    }
                    Functions.punishLogout.clear();
                    world.setTime(0);
                }
                if (time == 5) {

                    for (String key : Functions.animalNames.keySet()) {
                        Functions.animalNames.replace(key, Functions.animalNames.get(key) + 1);
                    }

                    Functions.day = world.getGameTime();
                    try {
                        Functions.fileReader.newDay();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Functions.setWeather();
                    for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                        if (world.hasStorm()) {
                            player.sendTitle(net.md_5.bungee.api.ChatColor.GOLD + "Rise and Shine", net.md_5.bungee.api.ChatColor.BLUE + " Rainy Day");
                        } else {
                            player.sendTitle(net.md_5.bungee.api.ChatColor.GOLD + "Rise and Shine", ChatColor.BLUE + " Sunny Day");
                        }
                    }
                    for (Entity mob : world.getEntities()) {
                        try {
                            if (mob.getCustomName() != null && mob.getType() != EntityType.PLAYER) {
                                for (Pen pen : Functions.pens) {
                                    if (pen.insidePen(mob.getLocation())) {
                                        Functions.economy.depositPlayer(pen.owner, Functions.calculateAnimalPayout(mob, Functions.getPlayer(pen.owner)));
                                        Functions.getPlayer(pen.owner).addToTodaysCash(Functions.calculateAnimalPayout(mob, Functions.getPlayer(pen.owner)));
                                        mob.remove();
                                    }
                                }
                            }
                        } catch (NullPointerException e) {
                        }
                    }
                    for (DepositBox box : Functions.depositBoxes) {
                        if (box.getDepositBox().getType() == Material.CHEST) {
                            Chest chest = (Chest) box.getDepositBox().getState();
                            for (curtis1509.farmerslife.Player player : Functions.players) {
                                if (box.getOwner().equals(player.getName())) {

                                    try {
                                        player.addCash(Functions.getAmountOfCash(Functions.depositCrops, chest.getInventory(), player.getName()) * player.getSkills().skillProfits.getMultiplier());
                                        Functions.fileReader.saveStats(Functions.day, player.getName());
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

                            //Milk Bucket Exchange
                            LinkedList<DepositCrop> milkBuckets = new LinkedList<>();
                            milkBuckets.add(new DepositCrop(Material.MILK_BUCKET, 0));
                            milkBuckets.add(new DepositCrop(Material.BUCKET, 0));
                            int buckets = Functions.getAmountOf(milkBuckets, chest.getInventory());
                            chest.getInventory().clear();
                            chest.getInventory().addItem(new ItemStack(Material.BUCKET, buckets));

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
                                    Functions.getPlayer(box.getOwner()).getPlayer().sendMessage("Your delivery has arrived inside your shipment box");
                            }
                        }
                    }

                    for (curtis1509.farmerslife.Player player : Functions.players) {
                        player.golemIronSoldToday = 0;
                        if (Functions.bestPlayerName.equals("")) {
                            Functions.bestPlayerName = player.getName();
                            Functions.bestCashAmount = player.getTodaysCash();
                        } else {
                            if (player.getTodaysCash() > Functions.bestCashAmount) {
                                Functions.bestCashAmount = player.getTodaysCash();
                                Functions.bestPlayerName = player.getName();
                            }
                        }
                        player.getPlayer().sendMessage("You made $" + (int) Math.floor(player.getTodaysCash()) + " yesterday");
                        player.resetTodaysCash();
                    }

                    if (!Functions.bestPlayerName.equals("") && Functions.bestCashAmount > 0)
                        Functions.broadcast("Well done to " + Functions.bestPlayerName + " for making the most money yesterday for a total of $" + (int) Math.floor(Functions.bestCashAmount));
                    Functions.bestPlayerName = "";
                    Functions.bestCashAmount = 0;
                    Functions.fileReader.savePlayers();
                    Functions.reloadShop();
                }
            }
        }, 1, 1);
    }
}