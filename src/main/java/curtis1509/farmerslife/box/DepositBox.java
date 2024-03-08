package curtis1509.farmerslife.box;

import java.io.IOException;
import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import curtis1509.farmerslife.DepositCrop;
import curtis1509.farmerslife.FarmersLife;
import curtis1509.farmerslife.Functions;

public class DepositBox extends Box {

    public DepositBox(Block chest, String owner, int id) {
        super(chest, owner, id);
    }

    public void processBox() {
        try {
            FarmersLife.economy.depositPlayer(Functions.getPlayer(getOwner()).getPlayer(),
                    Functions.getAmountOfCash(FarmersLife.depositCrops, getBox().getInventory(), getOwner())
                            * Functions.getPlayer(getOwner()).getSkills().skillProfits.getMultiplier());
            FarmersLife.fileReader.saveStats(FarmersLife.day, getOwner());
        } catch (IOException e) {
            e.printStackTrace();
        }
        processMilkBuckets();
    }

    public void processMilkBuckets() {
        LinkedList<DepositCrop> milkBuckets = new LinkedList<>();
        milkBuckets.add(new DepositCrop(Material.MILK_BUCKET, 0, null));
        milkBuckets.add(new DepositCrop(Material.BUCKET, 0, null));
        int buckets = Functions.getAmountOf(milkBuckets, getBox().getInventory());
        getBox().getInventory().clear();
        getBox().getInventory().addItem(new ItemStack(Material.BUCKET, buckets));
    }

}
