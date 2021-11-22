package curtis1509.farmerslife;

import dev.jcsoftware.jscoreboards.JGlobalMethodBasedScoreboard;
import dev.jcsoftware.jscoreboards.JScoreboardTeam;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Ageable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

public class FarmersLife extends JavaPlugin implements Listener, CommandExecutor {

    public World world;
    JGlobalMethodBasedScoreboard scoreboard;
    JScoreboardTeam team;

    @Override
    public void onEnable() {
        getLogger().info("Farmers Life has been enabled!");
        scheduleTimer(this.getServer().getWorld("world"));
        scoreboard = new JGlobalMethodBasedScoreboard();
        scoreboard.setTitle("Farmers Cash");
        scoreboard.setLines(
                "$" + cash
        );
        team = scoreboard.createTeam(
                "Team Name", // The internal name of the team. Can be used to find it later if you don't store a reference to it
                "", // The display name (prefix) of the team
                ChatColor.RED // The color of the team. Will change the player's name color
        );
        myInventory.setItem(0, new ItemStack(Material.DIRT, 1));
        Bukkit.getOnlinePlayers().forEach(this::addToScoreboard);
        getServer().getPluginManager().registerEvents(this, this);
    }

    int cash = 0;

    private void addToScoreboard(Player player) {
        scoreboard.addPlayer(player);
        team.addPlayer(player);
        scoreboard.updateScoreboard();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked(); // The player that clicked the item
        ItemStack clicked = event.getCurrentItem(); // The item that was clicked
        Inventory inventory = event.getInventory(); // The inventory that was clicked in
        if (inventory == myInventory) { // The inventory is our custom Inventory
            assert clicked != null;
            if (clicked.getType() == Material.DIRT) { // The item that the player clicked it dirt
                event.setCancelled(true); // Make it so the dirt is back in its original spot
                player.closeInventory(); // Closes there inventory
                doubleWheat = player.getName();
                // player.getInventory().addItem(new ItemStack(Material.DIRT, 1)); // Adds dirt
            }
        }
    }


    public String doubleWheat = "";


    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        getLogger().log(Level.INFO, "Player " + event.getPlayer().getName() + " is logging in! Welcome to FarmLife!");

    }

    boolean CurtisOnScoreboard = false;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!CurtisOnScoreboard) {
            addToScoreboard(event.getPlayer());
            CurtisOnScoreboard = true;
        }
        cash++;
        scoreboard.setLines("$" + cash);
        scoreboard.updateScoreboard();
        if (event.getClickedBlock().getType().equals(Material.CHEST) && waitingForPlayer.equals(event.getPlayer().getName())) {
            getLogger().log(Level.INFO, "Player " + event.getPlayer().getName() + " clicked");
            event.getPlayer().sendMessage("Cool beans! Put some crops in here overnight and you get some gold in return!");
            waitingForPlayer = "";
            location = event.getClickedBlock().getLocation();
            block = (Block) event.getClickedBlock().getLocation().getBlock();

        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (doubleWheat.equals(event.getPlayer().getName()))
            if (Objects.requireNonNull(event.getBlock()).getType() == Material.WHEAT && event.getBlock().getBlockData() instanceof Ageable) {
                Ageable age = (Ageable) event.getBlock().getBlockData();
                if (age.getAge() == age.getMaximumAge()) {
                    event.getPlayer().sendMessage("Ahoy you broke some wheat");
                    ItemStack gold = new ItemStack(Material.GOLD_INGOT, 20);
                    Objects.requireNonNull(this.getServer().getWorld("world")).dropItem(event.getBlock().getLocation(), gold);
                }
            }
    }

    Location location;
    Block block;
    public String waitingForPlayer = "";

    public int getAmountOf(Material item, Inventory inventory) {
        int i = 0;
        for (ItemStack is : inventory.getContents()) {
            if (is != null) {
                //getLogger().info(is.getType().getData().getName());
                if (is.getType() == item) {
                    i += is.getAmount();
                }
            }
        }
        return i;
    }

    public void scheduleTimer(final World world) {
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                long time = world.getTime();
                if (time == 5) {
                    getLogger().info("Executing code");
                    if (world.getBlockAt(location).getType().equals(Material.CHEST)) {
                        Chest chest = (Chest) block.getState();
                        int goldAmount = 1;

                        cash += getAmountOf(Material.WHEAT, chest.getInventory());
                        goldAmount += getAmountOf(Material.WHEAT, chest.getInventory());
                        scoreboard.updateScoreboard();

                        chest.getInventory().clear();

                        ItemStack gold = new ItemStack(Material.GOLD_INGOT, goldAmount);
                        chest.getInventory().addItem(gold);
                    }
                }
            }
        }, 1, 1);
    }

    public static Inventory myInventory = Bukkit.createInventory(null, 9, "My custom Inventory!");

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("box")) {
            waitingForPlayer = sender.getName();
            Objects.requireNonNull(Bukkit.getPlayer(waitingForPlayer)).sendMessage("Hells yeah, select a chest and you can use it to make some thicc gold ingots");
            Thread wait = new Thread() {
                public void run() {
                    try {
                        Thread.sleep(10000);
                        if (!waitingForPlayer.equals(""))
                            Objects.requireNonNull(Bukkit.getPlayer(waitingForPlayer)).sendMessage("Bruz you took too long to select a chest so we've cancelled your command");
                        waitingForPlayer = "";
                    } catch (InterruptedException e) {
                        e.printStackTrace();

                    }
                }
            };
            wait.start();
            return true;
        } else if (cmd.getName().equalsIgnoreCase("buy")) {
            Objects.requireNonNull(Bukkit.getPlayer(sender.getName())).openInventory(myInventory);
        }
        return false;
    }


    @Override
    public void onDisable() {
        getLogger().info("Farmers Life has been disabled!");
    }

}
