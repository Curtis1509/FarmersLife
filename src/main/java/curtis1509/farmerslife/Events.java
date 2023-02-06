package curtis1509.farmerslife;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.*;

import static curtis1509.farmerslife.FarmersLife.*;
import static org.bukkit.Bukkit.getLogger;

public class Events extends Functions implements Listener {


    @EventHandler
    public void onLogin(PlayerJoinEvent event) {
        initPlayer(event.getPlayer());
    }
    @EventHandler
    public void onLogout(PlayerQuitEvent event) {
        if (world.getTime() >= 18000) {
            punishLogout.add(event.getPlayer().getName());
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (event.getRecipe().getResult().getType().toString().toLowerCase().contains("chestplate") || event.getRecipe().getResult().getType().toString().toLowerCase().contains("leggings")
                || event.getRecipe().getResult().getType().toString().toLowerCase().contains("boots") || event.getRecipe().getResult().getType().toString().toLowerCase().contains("helmet")) {
            for (curtis1509.farmerslife.Player player : players) {
                if (player.getSkills().protection && player.getName().equals(event.getWhoClicked().getName())) {
                    Objects.requireNonNull(event.getCurrentItem()).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                    player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.BLOCK_ANVIL_USE, 3, 1);
                }
            }
        }
    }

    @EventHandler
    public void craft(PrepareItemCraftEvent event) {
        boolean golemNerf = false;
        boolean shopNerf = false;
        try {
            for (ItemStack item : event.getInventory().getContents()) {
                assert item != null;
                if (item.getItemMeta() != null) {
                    if (item.getItemMeta().getDisplayName().contains("GOLEM"))
                        golemNerf = true;
                    if (item.getItemMeta().getDisplayName().contains("Shop"))
                        shopNerf = true;
                }

            }
            if (golemNerf) {
                ItemStack result = Objects.requireNonNull(event.getRecipe()).getResult();
                ItemMeta meta = event.getRecipe().getResult().getItemMeta();
                meta.setDisplayName("GOLEM " + Objects.requireNonNull(event.getInventory().getResult()).getType().name());
                result.setItemMeta(meta);
                event.getInventory().setResult(result);
            } else if (shopNerf) {
                ItemStack result = Objects.requireNonNull(event.getRecipe()).getResult();
                ItemMeta meta = event.getRecipe().getResult().getItemMeta();
                meta.setDisplayName("Shop " + Objects.requireNonNull(event.getInventory().getResult()).getType().name());
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
    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        try {
            if (Objects.requireNonNull(Objects.requireNonNull(event.getItem()).getItemMeta()).getDisplayName().equals("Farmers Compass") || Objects.requireNonNull(Objects.requireNonNull(event.getItem()).getItemMeta()).getDisplayName().equals("Farmers HUD")) {
                event.getPlayer().openInventory(menuInventory);
            }
        } catch (NullPointerException ignored) {}
    }

    @EventHandler
    public void onPlayerDropEvent(PlayerDropItemEvent event) {
        if (Objects.requireNonNull(event.getItemDrop().getItemStack().getItemMeta()).getDisplayName().equals("Farmers Compass") && event.getPlayer().getInventory().getItem(8) == null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        try {
            if (event.getClickedBlock() != null) {
                for (DepositBox box : depositBoxes) {
                    if (box.getDepositBox().getLocation().toString().equals(event.getClickedBlock().getLocation().toString())) {
                        message(event.getPlayer(),"This is " + box.getOwner() + "'s deposit box");
                    }
                }

                if (waitingForPlayer.get(event.getPlayer().getName()).equals("pen")) {
                    if (!waitingForPenB.containsKey(event.getPlayer().getName())) {
                        message(event.getPlayer(),"Corner Accepted. Now Select Second Corner");
                        waitingForPenB.put(event.getPlayer().getName(), event.getClickedBlock().getLocation());
                    } else {
                        waitingForPlayer.remove(event.getPlayer().getName());
                        Location A = waitingForPenB.get(event.getPlayer().getName());
                        waitingForPenB.remove(event.getPlayer().getName());
                        Location B = event.getClickedBlock().getLocation();
                        Random random = new Random();

                        boolean collided = false;
                        for (Pen pen : pens) {
                            if (pen.insidePen(A) || pen.insidePen(B)) {
                                collided = true;
                                message(event.getPlayer(),"Uh Oh! A selling pen in that location already exists.");
                            }
                        }

                        if (!collided) {
                            if (Pen.checkMaxSize(A, B)) {
                                pens.add(new Pen(A, B, event.getPlayer().getName(), random.nextInt(100000)));
                                message(event.getPlayer(),"You've successfully made a Selling Pen");
                            } else
                                message(event.getPlayer(),"Uh oh this pen exceeds the maximum size of 32 blocks diagonally. Try again");
                        }
                    }
                }

                if (event.getClickedBlock().getType().equals(Material.CHEST) && waitingForPlayer.get(event.getPlayer().getName()).equals("box")) {

                    boolean taken = false;
                    for (DepositBox box : depositBoxes) {
                        if (box.getDepositBox().getLocation().toString().equals(event.getClickedBlock().getLocation().toString())) {
                            if (box.getOwner().equals(event.getPlayer().getName())) {
                                for (DepositBox resetBox : getDepositBoxes(event.getPlayer())) {
                                    resetBox.shipmentBox = false;
                                }
                                message(event.getPlayer(),"This is now your shipment box. Anything you buy from the shop will be delivered here each morning");
                                box.makeShipmentBox();
                                waitingForPlayer.remove(event.getPlayer().getName());
                                taken = true;
                                break;
                            } else {
                                message(event.getPlayer(),"Sorry that deposit box is taken by " + box.getOwner());
                                taken = true;
                                waitingForPlayer.remove(event.getPlayer().getName());
                            }
                        }
                    }

                    if (!taken) {
                        message(event.getPlayer(),"Cool beans! Put some crops in here overnight and you can get some money in return!");
                        message(event.getPlayer(),"Would you like to receive deliveries in this box too? Type /box");
                        waitingForPlayer.remove(event.getPlayer().getName());
                        Random random = new Random();
                        depositBoxes.add(new DepositBox((Block) event.getClickedBlock().getLocation().getBlock(), event.getPlayer().getName(), random.nextInt(100000), false));
                    }
                }
            }
        } catch (NullPointerException ignored) {}
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getItemInHand().getItemMeta().getDisplayName().contains("Shop")) {
            shopBlockLocations.add(event.getBlock().getLocation());
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

        if (shopBlockLocations.contains(event.getBlock().getLocation())) {
            getLogger().info("yes");
            List<ItemStack> drops = new ArrayList<>(event.getBlock().getDrops());
            for (ItemStack d : drops) {
                ItemMeta meta = d.getItemMeta();
                meta.setDisplayName("Shop " + d.getType().name());
                d.setItemMeta(meta);
            }
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            for (ItemStack d : drops) {
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), d);
            }
            shopBlockLocations.remove(event.getBlock().getLocation());
        }

        for (DepositBox box : depositBoxes) {
            if (!(box.getOwner().equals(event.getPlayer().getName())) && box.getDepositBox().getLocation().toString().equals(event.getBlock().getLocation().toString())) {
                event.setCancelled(true);
                message(event.getPlayer(),"You can't break someone else's deposit box");
            } else if ((box.getOwner().equals(event.getPlayer().getName())) && box.getDepositBox().getLocation().toString().equals(event.getBlock().getLocation().toString())) {
                fileReader.removeDepositData(box);
                message(event.getPlayer(),"You've destroyed your deposit box");
            }
        }
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
        Inventory removedInventory = Bukkit.createInventory(null, 9, "Item Recovery Service : PICK 3 ITEMS $250 ea");
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

        getPlayer(event.getEntity()).deathInventoryi = 3;
        message(event.getEntity(),"Oh no! You were knocked out unconscious and lost some items");
        sendClickableCommand(event.getEntity(), "&9[FarmersLife] &6Click to &2[GET] &6 some of your lost items back", "deathinventory");
        giveCompass(event.getEntity());
    }
    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.NAME_TAG) {
            String oldName = event.getRightClicked().getCustomName();
            String name = event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName();
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("FarmersLife")), new Runnable() {
                public void run() {
                    Random random = new Random();
                    if (!animalNames.containsKey(oldName)) {
                        String newName = name + " #" + random.nextInt(100000);
                        event.getRightClicked().setCustomName(newName);
                        if (oldName != null && !animalNames.containsKey(oldName)) {
                            message(event.getPlayer(),"You fixed " + oldName);
                            animalNames.put(newName, 250);
                        } else
                            animalNames.put(newName, 0);
                    } else {
                        event.getRightClicked().setCustomName(oldName);
                        event.setCancelled(true);
                        event.getPlayer().getInventory().getItemInMainHand().add(1);
                    }
                }
            }, 2);
        } else if (event.getRightClicked().getCustomName() != null) {
            if (!interactQueue.contains(event.getPlayer().getName())) {
                interactQueue.add(event.getPlayer().getName());
                String name = event.getRightClicked().getCustomName();
                for (String key : animalNames.keySet()) {
                    if (key.equals(name)) {
                         message(event.getPlayer(),name + " " + animalNames.get(name) + " days old " +
                                "$" + (int) Math.floor(calculateAnimalPayout(event.getRightClicked(), getPlayer(event.getPlayer().getName()))));
                    }
                }
            } else {
                interactQueue.remove(event.getPlayer().getName());
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        try {
            if (Objects.requireNonNull(event.getClickedInventory()).getType() == InventoryType.CHEST && Objects.requireNonNull(event.getCurrentItem()).getType() == Material.AIR) {
                if (isDepositBox(event.getClickedInventory().getLocation())) {
                    event.setCancelled(true);
                    if (!containsItem(depositCrops, event.getWhoClicked().getItemOnCursor())) {
                        message(getPlayer(getDepositBox(event.getClickedInventory().getLocation()).getOwner()).getPlayer(),"That item cannot be sold yet.");
                    } else
                        event.setCancelled(false);
                }
            } else if (event.isShiftClick() && event.getClickedInventory() != event.getWhoClicked().getOpenInventory().getTopInventory() && event.getWhoClicked().getOpenInventory().getTopInventory().getType() == InventoryType.CHEST) {
                if (isDepositBox(event.getWhoClicked().getOpenInventory().getTopInventory().getLocation())) {
                    event.setCancelled(true);
                    if (!containsItem(depositCrops, event.getCurrentItem())) {
                        message(event.getWhoClicked(),"That item cannot be sold yet.");
                    } else
                        event.setCancelled(false);
                }
            }

            org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getWhoClicked();
            ItemStack clicked = event.getCurrentItem();
            Inventory inventory = event.getInventory();

            for (curtis1509.farmerslife.Player p : players) {
                if (inventory == p.getSkills().skillsInventory) {
                    if (event.getClickedInventory() == p.getSkills().skillsInventory) {
                        if (Objects.requireNonNull(event.getCurrentItem()).getType() == Material.CHEST) {
                            event.setCancelled(true);
                            if (p.getSkills().skillProfits.LevelUp(p, economy.getBalance(player))) {
                                p.getSkills().skillsInventory.clear();
                                p.getSkills().populateSkillsInventory(player);
                                ((org.bukkit.entity.Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 1);
                            }
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
                            if (economy.getBalance(p.getPlayer()) >= 5000) {
                                economy.withdrawPlayer(p.getPlayer(),5000);
                                p.getPlayer().playSound(p.getPlayer().getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 3, 1);
                                Skills.spawnFireworks(p.getPlayer().getLocation(), 1);
                                message(event.getWhoClicked(),"You've bought a card pack");
                                giveCardPack(p.getPlayer());
                            } else {
                                message(event.getWhoClicked(),"You don't have enough money for a card pack");
                            }
                        }
                    }
                } else if (clicked.getType() == Material.OAK_FENCE) {
                    event.setCancelled(true);
                    event.getWhoClicked().closeInventory();
                    ((org.bukkit.entity.Player) event.getWhoClicked()).performCommand("pen");
                } else if (clicked.getType() == Material.CHEST) {
                    event.setCancelled(true);
                    event.getWhoClicked().closeInventory();
                    ((org.bukkit.entity.Player) event.getWhoClicked()).performCommand("box");
                }
            }
            if (inventory == spawnerInventory && event.getClickedInventory() == spawnerInventory) {
                if (clicked.getType() == Material.SPAWNER) {
                    for (curtis1509.farmerslife.Player p : players) {
                        if (p.getPlayer() == event.getWhoClicked()) {
                            if (economy.getBalance(p.getPlayer()) >= 15000) {
                                economy.withdrawPlayer(p.getPlayer(),15000);
                                p.getPlayer().getInventory().addItem(spawnerInventory.getItem(1));
                            } else
                                message(p.getPlayer(),"Sorry, you don't have enough money to buy a spawner");
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
                            if (p.getPlayer() == event.getWhoClicked() || Objects.equals(p.getName(), event.getWhoClicked().getName())) {
                                if (economy.getBalance(p.getPlayer()) >= item.getCost()) {
                                    if (getDeliveryBox(player) != null) {
                                        economy.withdrawPlayer(p.getPlayer(),item.getCost());
                                        ItemStack addingItem = new ItemStack(item.getMaterial(), item.getAmount());
                                        ItemMeta meta = Objects.requireNonNull(event.getCurrentItem()).getItemMeta();
                                        assert meta != null;
                                        meta.setDisplayName("Shop " + addingItem.getType().name());
                                        if (item.getMaterial() == Material.CARROT || item.getMaterial() == Material.POTATO)
                                            meta.setLore(Collections.singletonList("Can only sell harvests from this item"));
                                        else
                                            meta.setLore(null);
                                        addingItem.setItemMeta(meta);

                                        Objects.requireNonNull(getPlayer((org.bukkit.entity.Player) event.getWhoClicked())).getDeliveryOrder().add(addingItem);
                                        message(event.getWhoClicked(),"Your order will arrive in your delivery box at 6am tomorrow");
                                    } else {
                                        if (getDepositBoxes((org.bukkit.entity.Player) event.getWhoClicked()).size() == 0) {
                                            message(event.getWhoClicked(),"You need a Deposit Box before you can order a delivery");
                                        } else {
                                            message(event.getWhoClicked(),"Set a delivery box to receive your items in. Type /box");
                                        }
                                    }
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
                        if (p.getPlayer() == event.getWhoClicked() && event.getCurrentItem() != null) {
                            if (economy.getBalance(p.getPlayer()) >= 250) {
                                economy.withdrawPlayer(p.getPlayer(),250);
                                event.setCancelled(true);
                                event.getWhoClicked().getInventory().addItem(event.getCurrentItem());
                                inventory.remove(Objects.requireNonNull(event.getCurrentItem()));
                                p.deathInventoryi--;
                                if (p.deathInventoryi == 0 || p.getDeathInventory().isEmpty()) {
                                    event.getWhoClicked().closeInventory();
                                    p.clearDeathInventory();
                                    p.deathInventoryi = 3;
                                }
                            }else{
                                message(p.getPlayer(),"Insufficient Balance. It costs $250 to retrieve an item.");
                            }
                        }
                    }
                }
            }
            if (Objects.requireNonNull(Objects.requireNonNull(event.getCurrentItem()).getItemMeta()).getDisplayName().equals("Farmers Compass")) {
                if (!Objects.requireNonNull(Objects.requireNonNull(event.getClickedInventory()).getItem(8)).getItemMeta().getDisplayName().equals("Farmers Compass"))
                    event.setCancelled(true);
            }
        } catch (NullPointerException ignored) {}
    }

}