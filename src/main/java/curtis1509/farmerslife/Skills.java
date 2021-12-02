package curtis1509.farmerslife;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class Skills {

    public Inventory skillsInventory = Bukkit.createInventory(null, 18, "Skills");
    SkillProfits skillProfits;

    public Skills(int skillProfitsLevel) {
        this.skillProfits = new SkillProfits(skillProfitsLevel);
        populateSkillsInventory();
    }

    public void populateSkillsInventory() {
        skillsInventory.addItem(createItem(Material.CHEST, "Profits " + (skillProfits.getLevel()+1) + " | $" + (int)Math.ceil(skillProfits.cost),
                "+0.1x | " + " Current: " + skillProfits.getMultiplier()+"x"));
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
