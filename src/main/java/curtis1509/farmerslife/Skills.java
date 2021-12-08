package curtis1509.farmerslife;

import org.bukkit.*;
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

    public Skills(int skillProfitsLevel, boolean creative, boolean protection) {
        this.skillProfits = new SkillProfits(skillProfitsLevel);
        this.creative = creative;
        this.protection = protection;
        populateSkillsInventory();
    }

    public void populateSkillsInventory() {
        skillsInventory.addItem(createItem(Material.CHEST, "Profits " + (skillProfits.getLevel() + 1) + " | $" + (int) Math.ceil(skillProfits.cost),
                "+0.1x | " + " Current: " + skillProfits.getMultiplier() + "x"));
        if (creative)
            skillsInventory.addItem(createItem(Material.EMERALD, "Creative Mode",
                    "OFF"));
        else
            skillsInventory.addItem(createItem(Material.EMERALD, "Creative Mode | $1,000,000,000",
                    "Obtainable for the most hardcore farmers!"));
        if (!protection)
        skillsInventory.addItem(createItem(Material.DIAMOND_CHESTPLATE, "Protection III | $42,000",
                "This is permanent and will be applied to any armour piece you craft."));
        else
            skillsInventory.addItem(createItem(Material.DIAMOND_CHESTPLATE, "Protection III",
                    "UNLOCKED"));
    }

    public void toggleCreative(Player player) {
        isCreative = !isCreative;
        if (isCreative) {
            skillsInventory.setItem(1,createItem(Material.EMERALD, "Creative Mode",
                    "ON"));
            player.setGameMode(GameMode.CREATIVE);
        } else {
            skillsInventory.setItem(1, createItem(Material.EMERALD, "Creative Mode",
                    "OFF"));
            player.setGameMode(GameMode.SURVIVAL);
        }
    }

    public void buyCreative(curtis1509.farmerslife.Player player) {
        if (player.getCash() >= 1000000000) {
            player.removeCash(1000000000);
            creative = true;
            toggleCreative(player.getPlayer());
            Bukkit.broadcastMessage("Attention all players! " + player.getName() + " has obtained creative mode! All hail our ruler " + player.getName());
            for (Player p : Bukkit.getOnlinePlayers()){
                spawnFireworks(p.getLocation(),1);
            }
        }
        else
        {
            player.getPlayer().sendMessage("Sorry, you don't have enough money to become a farming god. You need $" + (1000000000-player.getCash()) + " more");
        }
    }

    public void protectArmour(Player player){
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
            player.getPlayer().sendMessage("Congratulations! You now have Protection III unlocked!");
            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.BLOCK_ANVIL_USE, 3, 1);
            skillsInventory.setItem(2,createItem(Material.DIAMOND_CHESTPLATE, "Protection III",
                    "UNLOCKED"));
        }
        else
        {
            player.getPlayer().sendMessage("Sorry, you don't have enough money for Protection III. You need $" + (42000-player.getCash()) + " more");
        }
    }

    public static void spawnFireworks(Location location, int amount){
        Location loc = location;
        location.setY(loc.getY() + 4);
        Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();

        fwm.setPower(0);
        fwm.addEffect(FireworkEffect.builder().withColor(Color.LIME, Color.RED, Color.BLUE).flicker(true).build());

        fw.setFireworkMeta(fwm);
        fw.setInvulnerable(true);
        fw.detonate();

        for(int i = 0;i<amount; i++){
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
