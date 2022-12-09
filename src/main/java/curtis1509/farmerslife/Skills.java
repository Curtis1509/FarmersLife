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
    boolean protection;
    boolean bedperk;
    boolean teleport;

    public Skills(int skillProfitsLevel, boolean protection, boolean bedperk, boolean teleport, Player player) {
        this.skillProfits = new SkillProfits(skillProfitsLevel);
        this.protection = protection;
        this.bedperk = bedperk;
        this.teleport = teleport;
        populateSkillsInventory(player);
    }

    public void populateSkillsInventory(Player player) {
        skillsInventory.addItem(createItem(Material.CHEST, "Profits " + (skillProfits.getLevel() + 1) + " | $" + (int) Math.ceil(skillProfits.cost),
                "+0.1x | " + " Current: " + (double) Math.round(skillProfits.getMultiplier() * 100) / 100 + "x"));
        if (!protection)
            skillsInventory.addItem(createItem(Material.DIAMOND_CHESTPLATE, "Protection IV | $42,000",
                    "This is permanent and will be applied to any armour piece you craft."));
        else
            skillsInventory.addItem(createItem(Material.DIAMOND_CHESTPLATE, "Protection IV",
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

    public boolean buyHeart(Player player) {
        int health = (int) (player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / 2);
        double iterations = health - 10;
        double cost = 10000;
        for (int i = 0; i < iterations; i++) {
            cost += 1000;
            cost *= 1.05;
        }

        if (Functions.economy.getBalance(player) > cost) {
            if (Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue() < 60) {
                Functions.economy.withdrawPlayer(player, cost);
                Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(health*2 + 2);
                skillsInventory.remove(Material.APPLE);
                cost += 1000;
                cost *= 1.05;
                if (Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue() < 60)
                    skillsInventory.addItem(createItem(Material.APPLE, "+1 Heart | $" + (int)Math.ceil(cost), health+1 + "/30 Hearts"));
                else
                    skillsInventory.addItem(createItem(Material.APPLE, "MAXIMUM HEALTH REACHED", health+1 + "/30 Hearts"));
                spawnFireworks(player.getLocation(),1);
                player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue());
                return true;
            } else player.sendMessage("You have the maximum 30/30 hearts");
        } else
            player.sendMessage("Sorry, you don't have enough money to buy more health.");
        return false;
    }

    public void protectArmour(Player player) {
        try {
            Objects.requireNonNull(player.getInventory().getChestplate()).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
            Objects.requireNonNull(player.getInventory().getLeggings()).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
            Objects.requireNonNull(player.getInventory().getHelmet()).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
            Objects.requireNonNull(player.getInventory().getBoots()).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);

        }catch(Exception ignored){}
    }

    public void buyProtection(curtis1509.farmerslife.Player player) {
        if (player.getCash() >= 42000) {
            player.removeCash(42000);
            protection = true;
            protectArmour(player.getPlayer());
            Bukkit.broadcastMessage("Congratulations to " + player.getPlayer().getName() + " for unlocking the permanent Protection IV");
            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.BLOCK_ANVIL_USE, 3, 1);
            skillsInventory.setItem(1, createItem(Material.DIAMOND_CHESTPLATE, "Protection IV",
                    "UNLOCKED"));
        } else {
            player.getPlayer().sendMessage("Sorry, you don't have enough money for Protection IV. You need $" + (42000 - player.getCash()) + " more");
        }
    }

    public void buyBedPerk(curtis1509.farmerslife.Player player) {
        if (player.getCash() >= 20000) {
            player.removeCash(20000);
            bedperk = true;
            Bukkit.broadcastMessage("Congratulations to " + player.getPlayer().getName() + " for unlocking the bed perk!");
            spawnFireworks(player.getPlayer().getLocation(), 1);
            skillsInventory.setItem(2, createItem(Material.RED_BED, "Bed Perk",
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

}
