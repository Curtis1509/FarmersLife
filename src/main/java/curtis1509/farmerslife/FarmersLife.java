package curtis1509.farmerslife;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Ageable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class FarmersLife extends JavaPlugin implements Listener, CommandExecutor {

    public static LinkedList<DepositBox> depositBoxes = new LinkedList<>();
    public static LinkedList<String> waitingForPlayer = new LinkedList<>();
    public static Inventory menuInventory = Bukkit.createInventory(null, 9, "Farmers Life Menu");
    public static Inventory seedsInventory = Bukkit.createInventory(null, 18, "Seeds");
    public static Inventory buyInventory = Bukkit.createInventory(null, 54, "Buy");
    public static LinkedList<DepositCrop> depositCrops = new LinkedList<>();
    public static LinkedList<curtis1509.farmerslife.Player> players = new LinkedList<>();
    public String doubleWheat = "";
    public static World world;
    FileReader fileReader = new FileReader();


    @Override
    public void onEnable() {
        getLogger().info("Farmers Life has been enabled!");
        world = getServer().getWorld("world");
        fileReader.CreateFile();
        populateDepositCrops();
        scheduleTimer(this.getServer().getWorld("world"));

        fileReader.loadDeposits();
        getServer().getPluginManager().registerEvents(this, this);

        ItemStack item = new ItemStack(Material.WHEAT_SEEDS, 1);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName("Seeds Shop");
        itemMeta.setLore(Collections.singletonList(""));
        item.setItemMeta(itemMeta);
        menuInventory.setItem(0, item);

        item = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
        itemMeta = item.getItemMeta();
        itemMeta.setDisplayName("Skills Shop");
        itemMeta.setLore(Collections.singletonList(""));
        item.setItemMeta(itemMeta);
        menuInventory.setItem(1, item);

        item = new ItemStack(Material.GOLD_INGOT, 1);
        itemMeta = item.getItemMeta();
        itemMeta.setDisplayName("Items Shop");
        itemMeta.setLore(Collections.singletonList(""));
        item.setItemMeta(itemMeta);
        menuInventory.setItem(2, item);

        item = new ItemStack(Material.CHEST, 1);
        itemMeta = item.getItemMeta();
        itemMeta.setDisplayName("Set Deposit Box");
        itemMeta.setLore(Collections.singletonList("Set your deposit box then throw your harvests in and wait until morning"));
        item.setItemMeta(itemMeta);
        menuInventory.setItem(8, item);

        populateBuyInventory();
    }

    LinkedList<BuyItem> buyItems = new LinkedList<>();

    public Material getMaterial(String string) {
        for (Material material : Material.values()) {
            if (material.name().equals(string)) {
                return material;
            }
        }
        return null;
    }

    public void loadShop() {
        FileReader fileReader = new FileReader();
        String dataIn = fileReader.read("plugins/FarmersLife/shop.txt");
        String[] data = dataIn.split(" ");
        for (int i = 1; i < data.length; i++) {
            String matName = data[i];
            Material material = getMaterial(data[i]);
            i++;
            int price = Integer.parseInt(data[i]);
            i++;
            int amount = Integer.parseInt(data[i]);
            if (material != null)
                addToInventory(buyInventory, material, price, amount);
            else
                getLogger().info(matName + " could not be identified");
        }
    }


    public void populateBuyInventory() {
        /*
        addToInventory(buyInventory, Material.DIAMOND, 100, 1);
        addToInventory(buyInventory, Material.IRON_INGOT, 10, 1);
        addToInventory(buyInventory, Material.GOLD_INGOT, 20, 1);
        addToInventory(buyInventory, Material.OAK_PLANKS, 8, 16);
        addToInventory(buyInventory, Material.STONE, 2, 32);
        addToInventory(buyInventory, Material.DIRT, 1, 32);
        addToInventory(buyInventory, Material.DIAMOND_HOE, 200, 1);
        */
        loadShop();

        addToInventory(seedsInventory, Material.WHEAT_SEEDS, 1, 8);
        addToInventory(seedsInventory, Material.MELON_SEEDS, 5, 1);
        addToInventory(seedsInventory, Material.PUMPKIN_SEEDS, 5, 1);
        addToInventory(seedsInventory, Material.CARROT, 2, 8);
        addToInventory(seedsInventory, Material.POTATO, 2, 8);
        addToInventory(seedsInventory, Material.SUGAR_CANE, 2, 8);
        addToInventory(seedsInventory, Material.CACTUS, 3, 1);
        addToInventory(seedsInventory, Material.COCOA_BEANS, 8, 8);
        addToInventory(seedsInventory, Material.BEETROOT_SEEDS, 4, 8);
    }

    public void populateDepositCrops() {
        depositCrops.add(new DepositCrop(Material.WHEAT, 1));
        depositCrops.add(new DepositCrop(Material.EGG, 0.4));
        depositCrops.add(new DepositCrop(Material.MILK_BUCKET, 1.25));
        depositCrops.add(new DepositCrop(Material.CARROT, 0.5));
        depositCrops.add(new DepositCrop(Material.BEEF, 2.0));
        depositCrops.add(new DepositCrop(Material.PORKCHOP, 2.35));
        depositCrops.add(new DepositCrop(Material.CHICKEN, 3.0));
        depositCrops.add(new DepositCrop(Material.POTATO, 0.5));
        depositCrops.add(new DepositCrop(Material.MELON_SLICE, 0.3));
        depositCrops.add(new DepositCrop(Material.PUMPKIN, 4));
        depositCrops.add(new DepositCrop(Material.TROPICAL_FISH, 2));
        depositCrops.add(new DepositCrop(Material.PUFFERFISH, 1.5));
        depositCrops.add(new DepositCrop(Material.COD, 1));
        depositCrops.add(new DepositCrop(Material.SALMON, 1.5));
        depositCrops.add(new DepositCrop(Material.CACTUS, 1));
        depositCrops.add(new DepositCrop(Material.SUGAR_CANE, 0.25));
        depositCrops.add(new DepositCrop(Material.COCOA_BEANS, 4));
    }

    public void addToInventory(Inventory inventory, Material material, int price, int amount) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setLore(Collections.singletonList("$" + price));
        item.setItemMeta(itemMeta);
        inventory.addItem(item);
        buyItems.add(new BuyItem(material, price, amount));
    }

    @Override
    public void onDisable() {
        getLogger().info("Farmers Life has been disabled!");
        fileReader.savePlayers();
    }

    public void giveCompass(Player player) {
        player.getInventory().setItem(8, new ItemStack(Material.COMPASS, 1));
        ItemMeta compassMeta = player.getInventory().getItem(8).getItemMeta();
        compassMeta.setDisplayName("Farmers HUD");
        compassMeta.setLore(Collections.singletonList("Farmers HUD is a collection of everything you need to become a thriving farmer!"));
        player.getInventory().getItem(8).setItemMeta(compassMeta);
    }

    @EventHandler
    public void onLogin(PlayerJoinEvent event) {
        getLogger().log(Level.INFO, "Player " + event.getPlayer().getName() + " is logging in! Welcome to FarmLife!");
        boolean playerExists = false;
        for (curtis1509.farmerslife.Player player : players) {
            if (player.getPlayer().getName().equals(event.getPlayer().getName())) {
                player.setPlayer(event.getPlayer());
                playerExists = true;
                player.reloadScoreboard();
                player.scoreboard.updateScoreboard();
                player.reloadPlayerName();
            }
        }
        if (!playerExists) {
            players.add(new curtis1509.farmerslife.Player(event.getPlayer(), fileReader.loadPlayerCash(event.getPlayer().getName()), fileReader.loadPlayerSkillProfits(event.getPlayer().getName())));
            event.getPlayer().sendMessage("Welcome to FarmersLife!");
        } else {
            event.getPlayer().sendMessage("Welcome back to FarmersLife!");
        }
        event.getPlayer().getInventory().setItem(8, new ItemStack(Material.COMPASS, 1));
        ItemMeta compassMeta = event.getPlayer().getInventory().getItem(8).getItemMeta();
        compassMeta.setDisplayName("Farmers HUD");
        compassMeta.setLore(Collections.singletonList("Farmers HUD is a collection of everything you need to become a thriving farmer!"));
        event.getPlayer().getInventory().getItem(8).setItemMeta(compassMeta);
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent event) {
        //players.removeIf(player -> player.getName().equals(event.getPlayer().getName()));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        event.getPlayer().getInventory().setItem(8, new ItemStack(Material.COMPASS, 1));
        ItemMeta compassMeta = event.getPlayer().getInventory().getItem(8).getItemMeta();
        compassMeta.setDisplayName("Farmers HUD");
        compassMeta.setLore(Collections.singletonList("Farmers HUD is a collection of everything you need to become a thriving farmer!"));
        event.getPlayer().getInventory().getItem(8).setItemMeta(compassMeta);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked(); // The player that clicked the item
        ItemStack clicked = event.getCurrentItem(); // The item that was clicked
        Inventory inventory = event.getInventory(); // The inventory that was clicked in

        for (curtis1509.farmerslife.Player p : players) {
            if (inventory == p.getSkills().skillsInventory) {
                if (event.getClickedInventory() == p.getSkills().skillsInventory) {
                    if (event.getCurrentItem().getType() == Material.CHEST) {
                        event.setCancelled(true);
                        if (p.getSkills().skillProfits.LevelUp(p, p.cash)) {
                            p.getSkills().skillsInventory.clear();
                            p.getSkills().populateSkillsInventory();
                            ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 1);
                        }
                    }
                }
            }
        }

        if (inventory == menuInventory) { // The inventory is our custom Inventory
            assert clicked != null;
            if (clicked.getType() == Material.WHEAT_SEEDS) { // The item that the player clicked it dirt
                event.setCancelled(true); // Make it so the dirt is back in its original spot
                event.getWhoClicked().openInventory(seedsInventory);
                //player.closeInventory(); // Closes there inventory
                //doubleWheat = player.getName();
                // player.getInventory().addItem(new ItemStack(Material.DIRT, 1)); // Adds dirt
            } else if (clicked.getType() == Material.EXPERIENCE_BOTTLE) { // The item that the player clicked it dirt
                event.setCancelled(true); // Make it so the dirt is back in its original spot
                for (curtis1509.farmerslife.Player p : players) {
                    if (event.getWhoClicked().getName().equals(p.getPlayer().getName())) {
                        event.getWhoClicked().openInventory(p.getSkills().skillsInventory);
                    }
                }
            } else if (clicked.getType() == Material.GOLD_INGOT) { // The item that the player clicked it dirt
                event.setCancelled(true); // Make it so the dirt is back in its original spot
                event.getWhoClicked().openInventory(buyInventory);
            } else if (clicked.getType() == Material.CHEST) { // The item that the player clicked it dirt
                event.setCancelled(true); // Make it so the dirt is back in its original spot
                event.getWhoClicked().closeInventory();
                waitingForPlayer.add(event.getWhoClicked().getName());
                event.getWhoClicked().sendMessage("Great! Click a chest to make it your deposit box.");
                Thread wait = new Thread(() -> {
                    try {
                        Thread.sleep(10000);
                        if (waitingForPlayer.contains(event.getWhoClicked().getName())) {
                            event.getWhoClicked().sendMessage("You took too long to select a chest. Try again!");
                            waitingForPlayer.remove(event.getWhoClicked().getName());
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();

                    }
                });
                wait.start();
            }
        }
        if ((inventory == buyInventory || inventory == seedsInventory) && event.getClickedInventory() == buyInventory) {
            for (BuyItem item : buyItems) {
                if (item.getMaterial() == clicked.getType()) {
                    for (curtis1509.farmerslife.Player p : players) {
                        if (p.getPlayer() == event.getWhoClicked()) {
                            if (p.getCash() >= item.getCost()) {
                                p.removeCash(item.getCost());
                                event.getWhoClicked().getInventory().addItem(new ItemStack(item.getMaterial(), item.getAmount()));
                            }
                            event.setResult(Event.Result.DENY);
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
        for (curtis1509.farmerslife.Player p : players) {
            if (inventory == p.getDeathInventory()) {
                if (event.getClickedInventory() == p.getDeathInventory()) {
                    if (p.getPlayer() == event.getWhoClicked()) {
                        event.setCancelled(true);
                        event.getWhoClicked().getInventory().addItem(event.getCurrentItem());
                        inventory.remove(Objects.requireNonNull(event.getCurrentItem()));
                        p.deathInventoryi--;
                        if (p.deathInventoryi == 0 || p.getDeathInventory().isEmpty()) {
                            event.getWhoClicked().closeInventory();
                            p.clearDeathInventory();
                            p.deathInventoryi = 3;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (event.getItem().getItemMeta().getDisplayName().equals("Farmers HUD")) {
                event.getPlayer().openInventory(menuInventory);
            }
        }
    }

    @EventHandler
    public void onPlayerDropEvent(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().getItemMeta().getDisplayName().equals("Farmers HUD")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null) {
            for (DepositBox box : depositBoxes) {
                if (box.getDepositBox().getLocation().toString().equals(event.getClickedBlock().getLocation().toString())) {
                    event.getPlayer().sendMessage("This is " + box.getOwner() + "'s deposit box");
                }
            }

            if (event.getClickedBlock().getType().equals(Material.CHEST) && waitingForPlayer.contains(event.getPlayer().getName())) {

                boolean taken = false;
                for (DepositBox box : depositBoxes) {
                    if (box.getDepositBox().getLocation().toString().equals(event.getClickedBlock().getLocation().toString())) {
                        event.getPlayer().sendMessage("Sorry that deposit box is taken by " + box.getOwner());
                        taken = true;
                        waitingForPlayer.remove(event.getPlayer().getName());
                    }
                }

                getLogger().log(Level.INFO, "Player " + event.getPlayer().getName() + " clicked");
                if (!taken) {
                    event.getPlayer().sendMessage("Cool beans! Put some crops in here overnight and you can get some money in return!");
                    waitingForPlayer.remove(event.getPlayer().getName());
                    Random random = new Random();
                    depositBoxes.add(new DepositBox((Block) event.getClickedBlock().getLocation().getBlock(), event.getPlayer().getName(), random.nextInt(100000)));
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) throws IOException {
        for (DepositBox box : depositBoxes) {
            if (!(box.getOwner().equals(event.getPlayer().getName())) && box.getDepositBox().getLocation().toString().equals(event.getBlock().getLocation().toString())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("You can't break someone else's deposit box");
            } else if ((box.getOwner().equals(event.getPlayer().getName())) && box.getDepositBox().getLocation().toString().equals(event.getBlock().getLocation().toString())) {
                fileReader.removeDepositData(box);
                event.getPlayer().sendMessage("You've destroyed your deposit box");
            }
        }

    }

    public boolean containsItem(LinkedList<DepositCrop> depositCrops, ItemStack itemStack) {
        for (DepositCrop crop : depositCrops) {
            if (crop.getMaterial() == itemStack.getType()) {
                return true;
            }
        }
        return false;
    }

    public DepositCrop getCropFromList(LinkedList<DepositCrop> depositCrops, ItemStack itemStack) {
        for (DepositCrop crop : depositCrops) {
            if (crop.getMaterial() == itemStack.getType()) {
                return crop;
            }
        }
        return null;
    }

    public int getAmountOf(LinkedList<DepositCrop> depositCrops, Inventory inventory) {
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

    public double getAmountOfCash(LinkedList<DepositCrop> depositCrops, Inventory inventory) {
        double i = 0;
        for (ItemStack is : inventory.getContents()) {
            if (is != null) {
                if (containsItem(depositCrops, is)) {
                    i += (is.getAmount() * getCropFromList(depositCrops,is).getReward());
                }
            }
        }
        return i;
    }

    public void sendClickableCommand(Player player, String message, String command) {
        // Make a new component (Bungee API).
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
        // Add a click event to the component.
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command));

        // Send it!
        player.spigot().sendMessage(component);
    }

    public String getTime() {
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

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setKeepInventory(true);
        List<ItemStack> newInventory = new ArrayList<ItemStack>();
        newInventory.add(event.getEntity().getInventory().getHelmet());
        newInventory.add(event.getEntity().getInventory().getChestplate());
        newInventory.add(event.getEntity().getInventory().getLeggings());
        newInventory.add(event.getEntity().getInventory().getBoots());
        for (int i = 0; i < 54; i++) {
            newInventory.add(event.getEntity().getInventory().getItem(i));
        }
        newInventory.add(event.getEntity().getInventory().getItemInOffHand());
        event.getDrops().removeAll(newInventory);

        Random random = new Random();
        Inventory removedInventory = Bukkit.createInventory(null, 9, "Lost and Found : PICK 3 ITEMS");
        LinkedList<Integer> values = new LinkedList<>();
        for (int i = 0; i < 9; i++) {
            int x = random.nextInt(54);
            while (values.contains(x)) {
                x = random.nextInt(54);
            }
            values.add(x);
            if (event.getEntity().getInventory().getItem(x) != null) {
                removedInventory.addItem(event.getEntity().getInventory().getItem(x));
                event.getEntity().getInventory().clear(x);
            }
        }

        for (curtis1509.farmerslife.Player p : players) {
            if (p.getName().equals(event.getEntity().getName()))
                p.storeInventory(removedInventory);
        }

        // event.getEntity().getInventory().clear();

        event.getEntity().sendMessage("Oh no! You were knocked out unconscious and lost some items");
        sendClickableCommand(event.getEntity(), "Click to &2[GET] &f some of your lost items back", "deathinventory");
        giveCompass(event.getEntity());
    }

    public void scheduleTimer(final World world) {
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                for (curtis1509.farmerslife.Player player : players) {
                    player.updateScoreboard(getTime());
                }
                long time = world.getTime();
                if (time == 19000) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!player.isSleeping()) {
                            player.sendMessage("It's getting late you're going to pass out soon... Hurry back to bed!");
                        }
                    }
                }
                if (time == 20000) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!player.isSleeping()) {
                            int cash = 0;
                            for (curtis1509.farmerslife.Player p : players) {
                                if (p.getName().equals(player.getName())) {
                                    cash = (int) Math.floor(p.getCash() * 0.1);
                                    if (cash > 1000) {
                                        cash = 1000;
                                    }
                                    p.removeCash(cash);
                                }
                            }
                            Location l = player.getBedSpawnLocation();
                            if (l != null)
                                player.teleport(player.getBedSpawnLocation());
                            else
                                player.teleport(world.getSpawnLocation());

                            player.sendMessage("Oh no! You passed out.");
                            player.sendMessage("We saw some people rummaging through your pockets, they stole $" + cash);
                            player.setFoodLevel(3);
                            giveCompass(player);
                        }
                    }
                    world.setTime(0);
                }
                if (time == 5) {
                    getLogger().info("Executing code");
                    for (DepositBox box : depositBoxes) {
                        getLogger().info(box.getOwner());
                        if (box.getDepositBox().getType() == Material.CHEST) {
                            Chest chest = (Chest) box.getDepositBox().getState();
                            for (curtis1509.farmerslife.Player player : players) {
                                if (box.getOwner().equals(player.getName())) {

                                    player.addCash(getAmountOfCash(depositCrops, chest.getInventory()) * player.getSkills().skillProfits.getMultiplier());
                                    getLogger().info("adding some cash for " + player.getName());
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

                            //Milk Bucket Exchange
                            LinkedList<DepositCrop> milkBuckets = new LinkedList<>();
                            milkBuckets.add(new DepositCrop(Material.MILK_BUCKET, 0));
                            milkBuckets.add(new DepositCrop(Material.BUCKET, 0));
                            int buckets = getAmountOf(milkBuckets, chest.getInventory());
                            chest.getInventory().clear();
                            chest.getInventory().addItem(new ItemStack(Material.BUCKET, buckets));

                        }
                    }
                    fileReader.savePlayers();
                }
            }
        }, 1, 1);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("box") && !waitingForPlayer.contains(sender.getName())) {
            waitingForPlayer.add(sender.getName());
            sender.sendMessage("Great! Click a chest to make it your deposit box.");
            Thread wait = new Thread(() -> {
                try {
                    Thread.sleep(10000);
                    if (waitingForPlayer.contains(sender.getName())) {
                        sender.sendMessage("You took too long to select a chest. Try again!");
                        waitingForPlayer.remove(sender.getName());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
            });
            wait.start();
            return true;
        } else if (cmd.getName().equalsIgnoreCase("farm")) {
            Objects.requireNonNull(Bukkit.getPlayer(sender.getName())).openInventory(menuInventory);
        } else if (cmd.getName().equalsIgnoreCase("reloadscores")) {
            for (curtis1509.farmerslife.Player p : players) {
                p.reloadScoreboard();
            }
        } else if (cmd.getName().equalsIgnoreCase("deathinventory")) {
            for (curtis1509.farmerslife.Player player : players) {
                if (player.getName().equals(sender.getName()) && player.getDeathInventory() != null) {
                    getLogger().info("returning inventory");
                    Objects.requireNonNull(Bukkit.getPlayer(sender.getName())).openInventory(player.getDeathInventory());
                }
            }
        } else if (cmd.getName().equalsIgnoreCase("reloadshop")) {
            buyInventory.clear();
            loadShop();
        }
        return false;
    }

}
