package curtis1509.farmerslife;

import org.bukkit.Material;

public class BuyItem {
    Material material;
    int cost;
    int amount;
    public BuyItem(Material material, int cost, int amount){
        this.material = material;
        this.cost=cost;
        this.amount=amount;
    }
    public int getCost(){
        return cost;
    }
    public int getAmount(){
        return amount;
    }
    public Material getMaterial(){
        return material;
    }
}
