package curtis1509.farmerslife;

import org.bukkit.Material;

public class DepositCrop {

    Material material;
    double reward;

    public DepositCrop(Material material, double reward){
        this.material = material;
        this.reward = reward;
    }

    public double getReward(){
        return reward;
    }

    public Material getMaterial(){
        return material;
    }

}
