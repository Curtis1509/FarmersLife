package curtis1509.farmerslife;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.Objects;

public class Skills {

    public Inventory skillsInventory = Bukkit.createInventory(null, 18, "Skills");
    SkillProfits skillProfits;
    boolean creative;
    boolean isCreative = false;
    boolean protection;
    boolean bedperk;
    boolean teleport;

    public Skills(int skillProfitsLevel, boolean creative, boolean protection, boolean bedperk, boolean teleport, Player player) {
        this.skillProfits = new SkillProfits(skillProfitsLevel);
        this.creative = creative;
        this.protection = protection;
        this.bedperk = bedperk;
        this.teleport = teleport;
        populateSkillsInventory(player);
    }

    public void populateSkillsInventory(Player player) {
        skillsInventory.addItem(createItem(Material.CHEST, "Profits " + (skillProfits.getLevel() + 1) + " | $" + (int) Math.ceil(skillProfits.cost),
                "+0.1x | " + " Current: " + skillProfits.getMultiplier() + "x"));
        if (creative)
            skillsInventory.addItem(createItem(Material.FEATHER, "Fly Mode",
                    "OFF"));
        else
            skillsInventory.addItem(createItem(Material.FEATHER, "Fly Mode | $1,000,000,000",
                    "Obtainable for the most hardcore farmers!"));
        if (!protection)
            skillsInventory.addItem(createItem(Material.DIAMOND_CHESTPLATE, "Protection III | $42,000",
                    "This is permanent and will be applied to any armour piece you craft."));
        else
            skillsInventory.addItem(createItem(Material.DIAMOND_CHESTPLATE, "Protection III",
                    "UNLOCKED"));
        if (!bedperk)
            skillsInventory.addItem(createItem(Material.RED_BED, "Bed Perk | $20,000",
                    "If you are within 100 blocks of your bed you will not pass out"));
        else
            skillsInventory.addItem(createItem(Material.RED_BED, "Bed Perk",
                    "UNLOCKED"));


        int health = (int) (player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / 2);
        double iterations = health - 10;
        double cost = 10000;
        for (int i = 0; i < iterations; i++) {
            cost += 1000;
            cost *= 1.05;
        }

        if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() < 60)
            skillsInventory.addItem(createItem(Material.APPLE, "+1 Heart | $"  + (int)Math.ceil(cost), health+1 + "/30 Hearts"));
        else
            skillsInventory.addItem(createItem(Material.APPLE, "MAXIMUM HEALTH REACHED", health + "/30 Hearts"));
    }

    public void toggleCreative(Player player) {
        isCreative = !isCreative;
        if (isCreative) {
            skillsInventory.setItem(1, createItem(Material.EMERALD, "Fly Mode",
                    "ON"));
            //player.setGameMode(GameMode.CREATIVE);
            player.setAllowFlight(true);
            player.setFlying(true);
        } else {
            skillsInventory.setItem(1, createItem(Material.EMERALD, "Fly Mode",
                    "OFF"));
            player.setAllowFlight(false);
            player.setFlying(false);
            //player.setGameMode(GameMode.SURVIVAL);
        }
    }

    public boolean buyHeart(Player player) {
        int health = (int) (player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / 2);
        double iterations = health - 10;
        double cost = 10000;
        for (int i = 0; i < iterations; i++) {
            cost += 1000;
            cost *= 1.05;
        }

        if (FarmersLife.economy.getBalance(player) > cost) {
            if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() < 60) {
                FarmersLife.economy.withdrawPlayer(player, cost);
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health*2 + 2);
                skillsInventory.remove(Material.APPLE);
                cost += 1000;
                cost *= 1.05;
                if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() < 60)
                    skillsInventory.addItem(createItem(Material.APPLE, "+1 Heart | $" + (int)Math.ceil(cost), health+1 + "/30 Hearts"));
                else
                    skillsInventory.addItem(createItem(Material.APPLE, "MAXIMUM HEALTH REACHED", health+1 + "/30 Hearts"));
                spawnFireworks(player.getLocation(),1);
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                return true;
            } else player.sendMessage("You have the maximum 30/30 hearts");
        } else
            player.sendMessage("Sorry, you don't have enough money to buy more health.");
        return false;
    }

    public void buyCreative(curtis1509.farmerslife.Player player) {
        if (player.getCash() >= 1000000000) {
            player.removeCash(1000000000);
            creative = true;
            toggleCreative(player.getPlayer());
            Bukkit.broadcastMessage("Attention all players! " + player.getName() + " has obtained fly mode!");
            for (Player p : Bukkit.getOnlinePlayers()) {
                spawnFireworks(p.getLocation(), 1);
            }
        } else {
            player.getPlayer().sendMessage("Sorry, you don't have enough money to fly. You need $" + (1000000000 - player.getCash()) + " more");
        }
    }

    public void protectArmour(Player player) {
        Objects.requireNonNull(player.getInventory().getChestplate()).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
        Objects.requireNonNull(player.getInventory().getLeggings()).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
        Objects.requireNonNull(player.getInventory().getHelmet()).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
        Objects.requireNonNull(player.getInventory().getBoots()).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
    }

    public void buyProtection(curtis1509.farmerslife.Player player) {
        if (player.getCash() >= 42000) {
            player.removeCash(42000);
            protection = true;
            protectArmour(player.getPlayer());
            Bukkit.broadcastMessage("Congratulations to " + player.getPlayer().getName() + " for unlocking the permanent Protection III");
            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.BLOCK_ANVIL_USE, 3, 1);
            skillsInventory.setItem(2, createItem(Material.DIAMOND_CHESTPLATE, "Protection III",
                    "UNLOCKED"));
        } else {
            player.getPlayer().sendMessage("Sorry, you don't have enough money for Protection III. You need $" + (42000 - player.getCash()) + " more");
        }
    }

    public void buyBedPerk(curtis1509.farmerslife.Player player) {
        if (player.getCash() >= 20000) {
            player.removeCash(20000);
            bedperk = true;
            Bukkit.broadcastMessage("Congratulations to " + player.getPlayer().getName() + " for unlocking the bed perk!");
            spawnFireworks(player.getPlayer().getLocation(), 1);
            skillsInventory.setItem(3, createItem(Material.RED_BED, "Bed Perk",
                    "UNLOCKED"));
        } else {
            player.getPlayer().sendMessage("Sorry, you don't have enough money for Bed Perk. You need $" + (20000 - player.getCash()) + " more");
        }
    }

    public static void spawnFireworks(Location location, int amount) {
        Location loc = location;
        location.setY(loc.getY() + 4);
        Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();

        fwm.setPower(0);
        fwm.addEffect(FireworkEffect.builder().withColor(Color.LIME, Color.RED, Color.BLUE).flicker(true).build());

        fw.setFireworkMeta(fwm);
        fw.setInvulnerable(true);
        fw.detonate();

        for (int i = 0; i < amount; i++) {
            Firework fw2 = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
            fw2.setFireworkMeta(fwm);
        }
    }


    public ItemStack createItem(Material material, String name, String description) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(name);
        itemMeta.setLore(Collections.singletonList(description));
        item.setItemMeta(itemMeta);
        return item;
    }

    public boolean hasCreative() {
        return creative;
    }

}
