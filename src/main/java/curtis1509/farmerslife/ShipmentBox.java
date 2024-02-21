package curtis1509.farmerslife;

import static curtis1509.farmerslife.Functions.message;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class ShipmentBox extends Box {

    public ShipmentBox(Block chest, String owner, int id) {
        super(chest, owner, id);
    }

    public void deliverItems() {
        for (ItemStack item : Functions.getPlayer(getOwner()).getDeliveryOrder()) {
            getBox().getInventory().addItem(item);
        }
        Functions.getPlayer(getOwner()).getDeliveryOrder().clear();
        message(Functions.getPlayer(getOwner()).getPlayer(), "Your delivery has arrived inside your shipment box");
    }

}