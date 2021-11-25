package curtis1509.farmerslife;

import org.bukkit.Material;

public class DepositCrop {

    Material material;
    int reward;

    public DepositCrop(Material material, int reward){
        this.material = material;
        this.reward = reward;
    }

    public int getReward(){
        return reward;
    }

    public Material getMaterial(){
        return material;
    }

}
