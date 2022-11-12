package curtis1509.farmerslife;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.Ageable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.Crops;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FarmersLife extends JavaPlugin implements Listener, CommandExecutor {

    public static DefaultFiles defaultFiles = new DefaultFiles();
    public static LinkedList<DepositBox> depositBoxes = new LinkedList<>();
    public static LinkedList<String> waitingForPlayer = new LinkedList<>();
    public static Inventory menuInventory = Bukkit.createInventory(null, 9, "Farmers Life Menu");
    public static Inventory spawnerInventory = Bukkit.createInventory(null, 9, "Buy Spawners Inventory");
    public static Inventory seedsInventory = Bukkit.createInventory(null, 18, "Seeds");
    public static Inventory buyInventory = Bukkit.createInventory(null, 18, "Buy");
    public static Inventory buyInventory2 = Bukkit.createInventory(null, 54, "Buy");
    public static Inventory teleportInventory = Bukkit.createInventory(null, 54, "Teleport HUB");
    public static LinkedList<DepositCrop> depositCrops = new LinkedList<>();
    public static LinkedList<curtis1509.farmerslife.Player> players = new LinkedList<>();
    public long day;
    public static int dayNumber;
    public static World world;
    public static String weather = "Wet";
    FileReader fileReader = new FileReader();


    public void checkOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            getLogger().log(Level.INFO, "Player " + player.getName() + " is logging in! Welcome to FarmLife!");
            boolean playerExists = false;
            for (curtis1509.farmerslife.Player fPlayer : players) {
                if (fPlayer.getPlayer().getName().equals(fPlayer.getPlayer().getName())) {
                    fPlayer.setPlayer(fPlayer.getPlayer());
                    playerExists = true;
                    fPlayer.reloadScoreboard();
                    fPlayer.scoreboard.updateScoreboard();
                    fPlayer.reloadPlayerName();
                }
            }
            player.sendTitle(ChatColor.GOLD + "Farmers Life", ChatColor.BLUE + weather + " Season");
            if (!playerExists) {
                players.add(new curtis1509.farmerslife.Player(player, fileReader.loadPlayerSkillProfits(player.getName()), fileReader.loadCreative(player.getName()), fileReader.loadProtection(player.getName()), fileReader.loadBedPerk(player.getName()), fileReader.loadPerk(player.getName(), "teleport")));
                player.sendMessage("Welcome to FarmersLife!");
            } else {
                player.sendMessage("Welcome back to FarmersLife!");
            }
            if (world.hasStorm())
                player.sendMessage("We are currently in a " + weather + " weather season. It is storming today");
            else
                player.sendMessage("We are currently in a " + weather + " weather season. It is sunny today");

            player.getInventory().setItem(8, new ItemStack(Material.COMPASS, 1));
            ItemMeta compassMeta = player.getInventory().getItem(8).getItemMeta();
            compassMeta.setDisplayName("Farmers Compass");
            compassMeta.setLore(Collections.singletonList("Farmers Compass is a collection of everything you need to become a thriving farmer!"));
            player.getInventory().getItem(8).setItemMeta(compassMeta);
            punishLogout.remove(player.getName());
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
            meta.setDisplayName(player.getName());
            ArrayList<String> lore = new ArrayList<>();
            lore.add("Teleport");
            lore.add("$250");
            meta.setLore(lore);
            meta.setOwningPlayer(player);
            playerHead.setItemMeta(meta);
            teleportInventory.addItem(playerHead);
        }
    }

    @Override
    public void onEnable() {

        defaultFiles = new DefaultFiles();
        depositBoxes = new LinkedList<>();
        waitingForPlayer = new LinkedList<>();
        menuInventory = Bukkit.createInventory(null, 9, "Farmers Life Menu");
        spawnerInventory = Bukkit.createInventory(null, 9, "Buy Spawners Inventory");
        seedsInventory = Bukkit.createInventory(null, 18, "Seeds");
        buyInventory = Bukkit.createInventory(null, 18, "Buy");
        buyInventory2 = Bukkit.createInventory(null, 54, "Buy");
        teleportInventory = Bukkit.createInventory(null, 54, "Teleport HUB");
        depositCrops = new LinkedList<>();
        players = new LinkedList<>();


        setupEconomy();
        getLogger().info("Farmers Life has been enabled!");
        world = getServer().getWorld("world");
        fileReader.CreateFile();
        try {
            loadDepositShop();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

        item = new ItemStack(Material.PLAYER_HEAD, 1);
        itemMeta = item.getItemMeta();
        itemMeta.setDisplayName("Teleport");
        itemMeta.setLore(Collections.singletonList("Teleport To Players"));
        item.setItemMeta(itemMeta);
        menuInventory.setItem(3, item);

        item = new ItemStack(Material.EMERALD_BLOCK, 1);
        itemMeta = item.getItemMeta();
        itemMeta.setDisplayName("Crates & Keys");
        itemMeta.setLore(Collections.singletonList("Coming Soon"));
        item.setItemMeta(itemMeta);
        menuInventory.setItem(5, item);

        item = new ItemStack(Material.FEATHER, 1);
        itemMeta = item.getItemMeta();
        itemMeta.setDisplayName("Flying Feather $10,000");
        itemMeta.setLore(Collections.singletonList("Right Click to enable flight. $20 / second"));
        item.setItemMeta(itemMeta);
        menuInventory.setItem(6, item);

        item = new ItemStack(Material.ENCHANTED_BOOK, 1);
        itemMeta = item.getItemMeta();
        itemMeta.setDisplayName("Card Pack $5000");
        itemMeta.setLore(Collections.singletonList("5 Randomly Enchanted Books"));
        item.setItemMeta(itemMeta);
        menuInventory.setItem(7, item);

        item = new ItemStack(Material.CHEST, 1);
        itemMeta = item.getItemMeta();
        itemMeta.setDisplayName("Set Deposit Box");
        itemMeta.setLore(Collections.singletonList("Set your deposit box then throw your harvests in and wait until morning"));
        item.setItemMeta(itemMeta);
        menuInventory.setItem(8, item);

        try {
            fileReader.getWeather();
        } catch (IOException e) {
            e.printStackTrace();
        }
        populateBuyInventory();
        setWeather();

        checkOnlinePlayers();

    }

    public static Economy economy;

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

    LinkedList<BuyItem> buyItems = new LinkedList<>();

    public Material getMaterial(String string) {
        for (Material material : Material.values()) {
            if (material.name().equals(string)) {
                return material;
            }
        }
        return null;
    }

    public void loadSpawnerShop() {
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

    public void loadShop() throws IOException {
        buyInventory = Bukkit.createInventory(null, 18, "Buy - Items Change Daily");
        FileReader fileReader = new FileReader();
        String dataIn = "";
        try {
            dataIn = fileReader.read("plugins/FarmersLife/shop.txt");
        } catch (Exception e) {
            FileWriter writer = new FileWriter("plugins/FarmersLife/shop.txt");
            writer.write(defaultFiles.defaultShop);
            writer.close();
            dataIn = fileReader.read("plugins/FarmersLife/shop.txt");
        }
        String[] data = dataIn.split(" ");

        LinkedList<Material> items = new LinkedList<Material>();
        Random random;

        while (items.size() < 10) {
            for (int i = 1; i < data.length; i++) {
                String matName = data[i];
                Material material = getMaterial(data[i]);
                i++;
                int price = Integer.parseInt(data[i]);
                i++;
                int amount = Integer.parseInt(data[i]);
                if (material != null) {
                    random = new Random();
                    int chance = random.nextInt(data.length - items.size());
                    if (chance == 1 && !items.contains(material)) {
                        addToInventory(buyInventory, material, price, amount);
                        items.add(material);
                    }
/*                else if (i <= 90 * 3)
                    addToInventory(buyInventory2, material, price, amount);*/
                } else
                    getLogger().info(matName + " could not be identified");
            }
        }
/*        addToInventory(buyInventory, Material.PAPER, "Next Page", "", 53);
        addToInventory(buyInventory2, Material.STONE_BUTTON, "Previous Page", "", 53);*/
    }

    public void loadDepositShop() throws IOException {
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

    public void loadSeedsShop() throws IOException {
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


    public void populateBuyInventory() {
        try {
            loadShop();
            loadSeedsShop();
        } catch (IOException e) {
            System.out.println("Failed To Load Shops");
        }
        loadSpawnerShop();
    }

    public void addToInventory(Inventory inventory, Material material, int price, int amount) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setLore(Collections.singletonList("$" + price));
        item.setItemMeta(itemMeta);
        inventory.addItem(item);
        buyItems.add(new BuyItem(material, price, amount));
    }

    public void addToInventory(Inventory inventory, Material material, String name, String text, int index) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(name);
        itemMeta.setLore(Collections.singletonList(text));
        item.setItemMeta(itemMeta);
        inventory.setItem(index, item);
    }

    @Override
    public void onDisable() {
        getLogger().info("Farmers Life has been disabled!");
        fileReader.savePlayers();
    }

    public void giveCompass(Player player) {
        player.getInventory().setItem(8, new ItemStack(Material.COMPASS, 1));
        ItemMeta compassMeta = player.getInventory().getItem(8).getItemMeta();
        compassMeta.setDisplayName("Farmers Compass");
        compassMeta.setLore(Collections.singletonList("Farmers Compass is a collection of everything you need to become a thriving farmer!"));
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
        event.getPlayer().sendTitle(ChatColor.GOLD + "Farmers Life", ChatColor.BLUE + weather + " Season");
        if (!playerExists) {
            players.add(new curtis1509.farmerslife.Player(event.getPlayer(), fileReader.loadPlayerSkillProfits(event.getPlayer().getName()), fileReader.loadCreative(event.getPlayer().getName()), fileReader.loadProtection(event.getPlayer().getName()), fileReader.loadBedPerk(event.getPlayer().getName()), fileReader.loadPerk(event.getPlayer().getName(), "teleport")));
            event.getPlayer().sendMessage("Welcome to FarmersLife!");
        } else {
            event.getPlayer().sendMessage("Welcome back to FarmersLife!");
        }
        if (world.hasStorm())
            event.getPlayer().sendMessage("We are currently in a " + weather + " weather season. It is storming today");
        else
            event.getPlayer().sendMessage("We are currently in a " + weather + " weather season. It is sunny today");

        event.getPlayer().getInventory().setItem(8, new ItemStack(Material.COMPASS, 1));
        ItemMeta compassMeta = event.getPlayer().getInventory().getItem(8).getItemMeta();
        compassMeta.setDisplayName("Farmers Compass");
        compassMeta.setLore(Collections.singletonList("Farmers Compass is a collection of everything you need to become a thriving farmer!"));
        event.getPlayer().getInventory().getItem(8).setItemMeta(compassMeta);
        punishLogout.remove(event.getPlayer().getName());
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
        meta.setDisplayName(event.getPlayer().getName());
        ArrayList<String> lore = new ArrayList<>();
        lore.add("Teleport");
        lore.add("$250");
        meta.setLore(lore);
        meta.setOwningPlayer(event.getPlayer());
        playerHead.setItemMeta(meta);
        teleportInventory.addItem(playerHead);
    }

    LinkedList<String> punishLogout = new LinkedList<>();

    @EventHandler
    public void onLogout(PlayerQuitEvent event) {
        if (world.getTime() >= 18000) {
            punishLogout.add(event.getPlayer().getName());
        }
        for (ItemStack head : teleportInventory) {
            if (head.getItemMeta().getDisplayName().equals(event.getPlayer().getName())) {
                teleportInventory.remove(head);
            }
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (event.getRecipe().getResult().getType().toString().toLowerCase().contains("chestplate") || event.getRecipe().getResult().getType().toString().toLowerCase().contains("leggings")
                || event.getRecipe().getResult().getType().toString().toLowerCase().contains("boots") || event.getRecipe().getResult().getType().toString().toLowerCase().contains("helmet")) {
            for (curtis1509.farmerslife.Player player : players) {
                if (player.getSkills().protection && player.getName().equals(event.getWhoClicked().getName())) {
                    Objects.requireNonNull(event.getCurrentItem()).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
                    player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.BLOCK_ANVIL_USE, 3, 1);
                }
            }
        }
    }

    @EventHandler
    public void craft(PrepareItemCraftEvent event) {
        boolean golemNerf = false;
        try {
            for (ItemStack item : event.getInventory().getContents()) {
                assert item != null;
                if (item.getItemMeta() != null) {
                    if (item.getItemMeta().getDisplayName().contains("GOLEM")) {
                        golemNerf = true;
                    }
                }

            }
            if (golemNerf) {
                ItemStack result = Objects.requireNonNull(event.getRecipe()).getResult();
                ItemMeta meta = event.getRecipe().getResult().getItemMeta();
                meta.setDisplayName("GOLEM " + Objects.requireNonNull(event.getInventory().getResult()).getType().name());
                result.setItemMeta(meta);
                event.getInventory().setResult(result);
            }
        } catch (NullPointerException ignored) {
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        event.getPlayer().getInventory().setItem(8, new ItemStack(Material.COMPASS, 1));
        ItemMeta compassMeta = event.getPlayer().getInventory().getItem(8).getItemMeta();
        compassMeta.setDisplayName("Farmers Compass");
        compassMeta.setLore(Collections.singletonList("Farmers Compass is a collection of everything you need to become a thriving farmer!"));
        event.getPlayer().getInventory().getItem(8).setItemMeta(compassMeta);
    }

    public LinkedList<DepositBox> getDepositBoxes(Player player) {
        LinkedList<DepositBox> boxes = new LinkedList<>();
        for (DepositBox box : depositBoxes) {
            if (box.getOwner().equals(player.getName())) {
                boxes.add(box);
            }
        }
        return boxes;
    }

    public DepositBox getDeliveryBox(Player player) {
        for (DepositBox box : depositBoxes) {
            if (box.getOwner().equals(player.getName())) {
                if (box.isShipmentBox()) {
                    return box;
                }
            }
        }
        return null;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        try {
            Player player = (Player) event.getWhoClicked(); // The player that clicked the item
            ItemStack clicked = event.getCurrentItem(); // The item that was clicked
            Inventory inventory = event.getInventory(); // The inventory that was clicked in

            for (curtis1509.farmerslife.Player p : players) {
                if (inventory == p.getSkills().skillsInventory) {
                    if (event.getClickedInventory() == p.getSkills().skillsInventory) {
                        if (Objects.requireNonNull(event.getCurrentItem()).getType() == Material.CHEST) {
                            event.setCancelled(true);
                            if (p.getSkills().skillProfits.LevelUp(p, economy.getBalance(player))) {
                                p.getSkills().skillsInventory.clear();
                                p.getSkills().populateSkillsInventory(player);
                                ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 1);
                            }
                        } else if (event.getCurrentItem().getType() == Material.FEATHER) {
                            event.setCancelled(true);
                            if (!p.getSkills().hasCreative())
                                p.getSkills().buyCreative(p);
                            else
                                p.getSkills().toggleCreative(p.getPlayer());
                        } else if (event.getCurrentItem().getType() == Material.DIAMOND_CHESTPLATE) {
                            event.setCancelled(true);
                            if (!p.getSkills().protection)
                                p.getSkills().buyProtection(p);
                        } else if (event.getCurrentItem().getType() == Material.RED_BED) {
                            event.setCancelled(true);
                            if (!p.getSkills().bedperk)
                                p.getSkills().buyBedPerk(p);
                        } else if (event.getCurrentItem().getType() == Material.APPLE) {
                            event.setCancelled(true);
                            p.getSkills().buyHeart(player);
                        }
                    }
                }
            }

            if (inventory == teleportInventory) {
                event.setCancelled(true);
                if (clicked.getType() == Material.PLAYER_HEAD) {
                    String playerName = clicked.getItemMeta().getDisplayName();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getName().equals(playerName)) {
                            for (curtis1509.farmerslife.Player pp : players) {
                                if ((!pp.getName().equals(p.getName())) && pp.getName().equals(event.getWhoClicked().getName())) {
                                    if (pp.getCash() >= 250) {
                                        pp.removeCash(250);
                                        event.getWhoClicked().teleport(p.getPlayer().getLocation());
                                        ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                        event.getWhoClicked().sendMessage("You teleported to " + p.getName());
                                        p.sendMessage(pp.getName() + " teleported to you");
                                    } else {
                                        event.getWhoClicked().sendMessage("You need $250 to teleport");
                                    }
                                } else {
                                    event.getWhoClicked().sendMessage("You can't teleport to yourself");
                                }
                            }
                        }
                    }
                }
            }

            if (inventory == menuInventory) {
                assert clicked != null;
                if (clicked.getType() == Material.WHEAT_SEEDS) {
                    event.setCancelled(true);
                    event.getWhoClicked().openInventory(seedsInventory);
                } else if (clicked.getType() == Material.EXPERIENCE_BOTTLE) {
                    event.setCancelled(true);
                    for (curtis1509.farmerslife.Player p : players) {
                        if (event.getWhoClicked().getName().equals(p.getPlayer().getName())) {
                            event.getWhoClicked().openInventory(p.getSkills().skillsInventory);
                        }
                    }
                } else if (clicked.getType() == Material.GOLD_INGOT) {
                    event.setCancelled(true);
                    event.getWhoClicked().openInventory(buyInventory);
                } else if (clicked.getType() == Material.SPAWNER && event.getClickedInventory() == menuInventory) {
                    event.getWhoClicked().openInventory(spawnerInventory);
                } else if (clicked.getType() == Material.ENCHANTED_BOOK && event.getClickedInventory() == menuInventory) {
                    event.setCancelled(true);
                    for (curtis1509.farmerslife.Player p : players) {
                        if (event.getWhoClicked().getName().equals(p.getPlayer().getName())) {
                            if (p.getCash() >= 5000) {
                                p.removeCash(5000);
                                p.getPlayer().playSound(p.getPlayer().getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 3, 1);
                                Skills.spawnFireworks(p.getPlayer().getLocation(), 1);
                                event.getWhoClicked().sendMessage("You've bought a card pack");
                                giveCardPack(p.getPlayer());
                            } else {
                                event.getWhoClicked().sendMessage("You don't have enough money for a card pack");
                            }
                        }
                    }
                } else if (clicked.getType() == Material.FEATHER) {
                    event.setCancelled(true);
                    if (economy.getBalance(((Player) event.getWhoClicked()).getPlayer()) > 10000) {
                        economy.withdrawPlayer(((Player) event.getWhoClicked()).getPlayer(), 10000);
                        giveFlyingFeather(((Player) event.getWhoClicked()).getPlayer());
                    }
                } else if (clicked.getType() == Material.PLAYER_HEAD) {
                    event.setCancelled(true);
                    event.getWhoClicked().openInventory(teleportInventory);
                } else if (clicked.getType() == Material.EMERALD_BLOCK) {
                    event.setCancelled(true);
                    ((Player) event.getWhoClicked()).getPlayer().sendMessage("This feature is coming soon and then you can degen all you like");
                } else if (clicked.getType() == Material.CHEST) {
                    event.setCancelled(true);
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
            if (inventory == spawnerInventory && event.getClickedInventory() == spawnerInventory) {
                if (clicked.getType() == Material.SPAWNER) {
                    for (curtis1509.farmerslife.Player p : players) {
                        if (p.getPlayer() == event.getWhoClicked()) {
                            if (p.getCash() >= 15000) {
                                p.removeCash(15000);
                                p.getPlayer().getInventory().addItem(spawnerInventory.getItem(1));
                            } else
                                p.getPlayer().sendMessage("Sorry, you don't have enough money to buy a spawner");
                            event.setCancelled(true);
                        }
                    }
                }
            }

            if ((inventory == buyInventory || inventory == buyInventory2 || inventory == seedsInventory) && (event.getClickedInventory() == buyInventory2 || event.getClickedInventory() == buyInventory || event.getClickedInventory() == seedsInventory)) {
                for (BuyItem item : buyItems) {
                    assert clicked != null;
                    if (item.getMaterial() == clicked.getType()) {
                        for (curtis1509.farmerslife.Player p : players) {
                            if (p.getPlayer() == event.getWhoClicked()) {
                                if (p.getCash() >= item.getCost()) {
                                    if (getDeliveryBox(player) != null) {
                                        p.removeCash(item.getCost());
                                        ItemStack addingItem = new ItemStack(item.getMaterial(), item.getAmount());
                                        ItemMeta meta = event.getCurrentItem().getItemMeta();
                                        assert meta != null;
                                        if (!meta.getDisplayName().contains("Pokeball"))
                                            meta.setDisplayName("Shop " + addingItem.getType().name());
                                        meta.setLore(Collections.singletonList("Can only sell harvests from this item"));
                                        addingItem.setItemMeta(meta);

                                        getPlayer((Player) event.getWhoClicked()).getDeliveryOrder().add(addingItem);
                                        event.getWhoClicked().sendMessage("Your order will arrive in your delivery box at 6am tomorrow");
                                    } else {
                                        if (getDepositBoxes((Player) event.getWhoClicked()).size() == 0) {
                                            event.getWhoClicked().sendMessage("You need a Deposit Box before you can order a delivery");
                                        } else {
                                            event.getWhoClicked().sendMessage("Set a delivery box to receive your items in. Type /box");
                                        }
                                    }
                                }
                                event.setResult(Event.Result.DENY);
                                event.setCancelled(true);
                            }
                        }
                    }
                }
                if (clicked.getType() == Material.STONE_BUTTON) {
                    if (inventory == buyInventory2) {
                        event.getWhoClicked().openInventory(buyInventory);
                        event.setResult(Event.Result.DENY);
                        event.setCancelled(true);
                    }

                }
                if (clicked.getType() == Material.PAPER) {
                    if (inventory == buyInventory) {
                        event.setResult(Event.Result.DENY);
                        event.setCancelled(true);
                        event.getWhoClicked().openInventory(buyInventory2);
                    }
                }
            }
            for (curtis1509.farmerslife.Player p : players) {
                if (inventory == p.getDeathInventory()) {
                    if (event.getClickedInventory() == p.getDeathInventory()) {
                        if (p.getPlayer() == event.getWhoClicked() && event.getCurrentItem() != null) {
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
            if (Objects.requireNonNull(Objects.requireNonNull(event.getCurrentItem()).getItemMeta()).getDisplayName().equals("Farmers Compass")) {
                event.setCancelled(true);
            }
        } catch (
                NullPointerException e) {
        }

    }

    public void giveCardPack(Player player) {
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

    public void giveFlyingFeather(Player player) {
        ItemStack flyingFeather = new ItemStack(Material.FEATHER, 1);
        ItemMeta meta = flyingFeather.getItemMeta();
        meta.setDisplayName("Flying Feather");
        meta.setLore(Collections.singletonList("Right Click To Toggle Flight. $20 per second"));
        flyingFeather.setItemMeta(meta);
        player.getInventory().addItem(flyingFeather);
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        try {
            if (Objects.requireNonNull(Objects.requireNonNull(event.getItem()).getItemMeta()).getDisplayName().equals("Farmers Compass") || Objects.requireNonNull(Objects.requireNonNull(event.getItem()).getItemMeta()).getDisplayName().equals("Farmers HUD")) {
                event.getPlayer().openInventory(menuInventory);
            }
            if (Objects.requireNonNull(Objects.requireNonNull(event.getItem()).getItemMeta()).getDisplayName().equals("Flying Feather")) {
                if (!event.getPlayer().isFlying() && !event.getPlayer().getAllowFlight() && (economy.getBalance(event.getPlayer()) > 5)) {
                    event.getPlayer().sendMessage("Flying Enabled");
                    event.getPlayer().setAllowFlight(true);
                    event.getPlayer().setFlying(true);
                } else if (event.getPlayer().isFlying() || event.getPlayer().getAllowFlight()) {
                    event.getPlayer().sendMessage("Flying Disabled");
                    event.getPlayer().setAllowFlight(false);
                    event.getPlayer().setFlying(false);
                } else {
                    event.getPlayer().sendMessage("You need at least $5 to fly");
                }
            }
        } catch (NullPointerException e) {
        }
    }

    @EventHandler
    public void onPlayerDropEvent(PlayerDropItemEvent event) {
        if (Objects.requireNonNull(event.getItemDrop().getItemStack().getItemMeta()).getDisplayName().equals("Farmers Compass")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        try {
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
                            if (box.getOwner().equals(event.getPlayer().getName())) {
                                for (DepositBox resetBox : getDepositBoxes(event.getPlayer())) {
                                    resetBox.shipmentBox = false;
                                }
                                event.getPlayer().sendMessage("This is now your shipment box. Anything you buy from the shop will be delivered here each morning");
                                box.makeShipmentBox();
                                waitingForPlayer.remove(event.getPlayer().getName());
                                taken = true;
                                break;
                            } else {
                                event.getPlayer().sendMessage("Sorry that deposit box is taken by " + box.getOwner());
                                taken = true;
                                waitingForPlayer.remove(event.getPlayer().getName());
                            }
                        }
                    }

                    if (!taken) {
                        event.getPlayer().sendMessage("Cool beans! Put some crops in here overnight and you can get some money in return!");
                        event.getPlayer().sendMessage("Would you like to receive deliveries in this box too? Type /box");
                        waitingForPlayer.remove(event.getPlayer().getName());
                        Random random = new Random();
                        depositBoxes.add(new DepositBox((Block) event.getClickedBlock().getLocation().getBlock(), event.getPlayer().getName(), random.nextInt(100000), false));
                    }
                }
            }
        } catch (NullPointerException ignored) {

        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) throws IOException {

        if (event.getBlock().getType() == Material.CARROTS || event.getBlock().getType() == Material.POTATOES || event.getBlock().getType() == Material.COCOA_BEANS) {
            if (event.getBlock().getBlockData() instanceof Ageable age) {
                if (age.getAge() != age.getMaximumAge()) {

                    List<ItemStack> drops = new ArrayList<>(event.getBlock().getDrops());
                    for (ItemStack d : drops) {
                        ItemMeta meta = d.getItemMeta();
                        meta.setDisplayName("Immature " + d.getType().name());
                        d.setItemMeta(meta);
                    }
                    event.setCancelled(true);
                    event.getBlock().setType(Material.AIR);
                    for (ItemStack d : drops) {
                        event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), d);
                    }
                }
            }
        }

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

    public double nerfIncome(ItemStack itemStack, String owner) {
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

    public double getAmountOfCash(LinkedList<DepositCrop> depositCrops, Inventory inventory, String owner) throws IOException {
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

    public void sendClickableCommand(Player player, String message, String command) {
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command));
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
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getType() == EntityType.IRON_GOLEM) {

            List<ItemStack> drops = new ArrayList<>(event.getDrops());
            for (ItemStack d : drops) {
                ItemMeta meta = d.getItemMeta();
                meta.setDisplayName("GOLEM " + d.getType().name());
                d.setItemMeta(meta);
            }
            event.getDrops().clear();
            for (ItemStack d : drops) {
                event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), d);
            }
        }
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

        event.getEntity().sendMessage("Oh no! You were knocked out unconscious and lost some items");
        sendClickableCommand(event.getEntity(), "Click to &2[GET] &f some of your lost items back", "deathinventory");
        giveCompass(event.getEntity());
    }

    public void setWeather() {
        Random random = new Random();
        int r = random.nextInt(10);
        if (weather.equals("Wet")) {
            world.setStorm(r < 5);
            stormingAllDay = world.hasStorm();
        } else {
            world.setStorm(r > 8);
        }
    }

    public curtis1509.farmerslife.Player getPlayer(Player player) {
        for (curtis1509.farmerslife.Player p : players) {
            if (p.getPlayer() == player) {
                return p;
            }
        }
        return null;
    }

    public curtis1509.farmerslife.Player getPlayer(String player) {
        getLogger().info(player + " NAME");
        for (curtis1509.farmerslife.Player p : players) {

            getLogger().info(player + " LOOP");
            if (p.getPlayer().getName().equals(player)) {
                return p;
            }
        }
        return null;
    }

    boolean stormingAllDay;

    @EventHandler
    public void captureBabyVillager(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType() == EntityType.VILLAGER && event.getDamager() instanceof Player) {
            if (((Player) event.getDamager()).getInventory().getItemInMainHand().getType() == Material.SHULKER_BOX) {
                BlockStateMeta meta = (BlockStateMeta) ((Player) event.getDamager()).getInventory().getItemInMainHand().getItemMeta();
                ((Villager) event.getEntity()).setHealth(0);
                ShulkerBox box = (ShulkerBox) meta.getBlockState();
                ItemStack spawnEgg = new ItemStack(Material.VILLAGER_SPAWN_EGG, 1);
                box.getInventory().addItem(spawnEgg);
                meta.setBlockState(box);
                box.update();
                ItemStack boxStack = new ItemStack(Material.SHULKER_BOX, 1);
                boxStack.setItemMeta(meta);
                ((Player) event.getDamager()).getInventory().setItemInMainHand(boxStack);

                event.getDamager().sendMessage("Ahhh, human trafficking. It's a bit unethical but it makes money so...");
            }
        }
    }

    boolean sleep = false;

    public void scheduleTimer(final World world) {
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                for (curtis1509.farmerslife.Player player : players) {
                    player.updateScoreboard(getTime());
                    if (player.player.isFlying() && !player.getSkills().hasCreative()) {
                        economy.withdrawPlayer(player.player, 1);
                        if (economy.getBalance(player.player) < 5) {
                            player.player.setFlying(false);
                            player.player.sendMessage("You don't have enough money to continue flying");
                        }
                    }
                }
                long time = world.getTime();
                if (time > 15000 && Bukkit.getOnlinePlayers().size() > 1) {
                    int total = 0;
                    boolean noOneInbed = true;
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!player.isSleeping()) {
                            for (curtis1509.farmerslife.Player p : players) {
                                if (p.getName().equals(player.getName())) {
                                    if (p.getSkills().bedperk) {
                                        if (withinRangeOfBed(p))
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
                        sleep = true;

                }
                if (weather.equals("Wet") && stormingAllDay)
                    if (time == 19000 || time == 18000 || time == 17000 || time == 16000 || time == 15000 || time == 14000 || time == 13000 || time == 12000 || time == 11000 || time == 10000 || time == 9000 || time == 8000 || time == 7000 || time == 6000 || time == 5000 || time == 4000 || time == 3000 || time == 2000 || time == 1000) {
                        Random random = new Random();
                        if (world.hasStorm())
                            world.setStorm(random.nextInt(10) > 2);
                        else
                            world.setStorm(random.nextInt(5) > 2);
                    }
                if (time == 19000) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!player.isSleeping()) {
                            player.sendTitle(ChatColor.BLUE + "It's Getting Late", ChatColor.GRAY + " Zzzzzz...");
                            player.sendMessage("It's getting late you're going to pass out soon... Hurry back to bed!");
                        }
                    }
                }
                if (time == 20000 || sleep) {
                    sleep = false;
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!player.isSleeping()) {
                            int cash = 0;
                            boolean passOut = true;
                            for (curtis1509.farmerslife.Player p : players) {
                                if (p.getName().equals(player.getName())) {
                                    cash = (int) Math.floor(p.getCash() * 0.1);
                                    if (cash > 1000) {
                                        cash = 1000;
                                    }
                                    if (p.getSkills().bedperk) {
                                        if (withinRangeOfBed(p))
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
                                giveCompass(player);
                            }
                        }
                    }
                    for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                        if (punishLogout.contains(player.getName())) {
                            int cash = 0;
                            for (curtis1509.farmerslife.Player p : players) {
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
                    punishLogout.clear();
                    world.setTime(0);
                }
                if (time == 5) {
                    day = world.getGameTime();
                    try {
                        fileReader.newDay();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setWeather();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (world.hasStorm()) {
                            player.sendTitle(ChatColor.GOLD + "Rise and Shine", ChatColor.BLUE + " Rainy Day");
                        } else {
                            player.sendTitle(ChatColor.GOLD + "Rise and Shine", ChatColor.BLUE + " Sunny Day");
                        }
                    }
                    for (DepositBox box : depositBoxes) {
                        if (box.getDepositBox().getType() == Material.CHEST) {
                            Chest chest = (Chest) box.getDepositBox().getState();
                            for (curtis1509.farmerslife.Player player : players) {
                                if (box.getOwner().equals(player.getName())) {

                                    try {
                                        player.addCash(getAmountOfCash(depositCrops, chest.getInventory(), player.getName()) * player.getSkills().skillProfits.getMultiplier());
                                        fileReader.saveStats(day, player.getName());
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
                            int buckets = getAmountOf(milkBuckets, chest.getInventory());
                            chest.getInventory().clear();
                            chest.getInventory().addItem(new ItemStack(Material.BUCKET, buckets));

                            for (ItemStack itemStack : deliveryItems) {
                                chest.getInventory().addItem(itemStack);
                            }

                            if (box.isShipmentBox() && getPlayer(box.getOwner()) != null) {
                                boolean itemsDelivered = false;
                                for (ItemStack item : getPlayer(box.getOwner()).getDeliveryOrder()) {
                                    itemsDelivered = true;
                                    chest.getInventory().addItem(item);
                                }
                                getPlayer(box.getOwner()).getDeliveryOrder().clear();
                                if (itemsDelivered)
                                    getPlayer(box.getOwner()).getPlayer().sendMessage("Your delivery has arrived inside your shipment box");
                            }
                        }
                    }

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
                        player.getPlayer().sendMessage("You made $" + player.getTodaysCash() + " yesterday");
                        player.resetTodaysCash();
                    }

                    if (!bestPlayerName.equals("") && bestCashAmount > 0)
                        Bukkit.broadcastMessage("Well done to " + bestPlayerName + " for making the most money yesterday for a total of $" + bestCashAmount);
                    bestPlayerName = "";
                    bestCashAmount = 0;

                    fileReader.savePlayers();

                    try {
                        loadShop();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }, 1, 1);
    }

    public String bestPlayerName = "";
    public double bestCashAmount = 0;

    public boolean withinRangeOfBed(curtis1509.farmerslife.Player player) {
        int x = player.getPlayer().getLocation().getBlockX();
        int y = player.getPlayer().getLocation().getBlockY();
        int z = player.getPlayer().getLocation().getBlockZ();

        int bx = Objects.requireNonNull(player.getPlayer().getBedSpawnLocation()).getBlockX();
        int by = Objects.requireNonNull(player.getPlayer().getBedSpawnLocation()).getBlockY();
        int bz = Objects.requireNonNull(player.getPlayer().getBedSpawnLocation()).getBlockZ();

        double distance = Math.sqrt(Math.pow(x - bx, 2) + Math.pow(y - by, 2) + Math.pow(z - bz, 2));
        return distance <= 100;
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
        } else if (cmd.getName().equalsIgnoreCase("deathinventory") || cmd.getName().equalsIgnoreCase("di")) {
            for (curtis1509.farmerslife.Player player : players) {
                if (player.getName().equals(sender.getName()) && player.getDeathInventory() != null) {
                    Objects.requireNonNull(Bukkit.getPlayer(sender.getName())).openInventory(player.getDeathInventory());
                }
            }
        } else if (cmd.getName().equalsIgnoreCase("reloadshop")) {
            buyInventory.clear();
            buyInventory2.clear();
            buyItems.clear();
            try {
                loadShop();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            depositCrops.clear();
            try {
                loadDepositShop();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            seedsInventory.clear();
            try {
                loadSeedsShop();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

}
