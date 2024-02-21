package curtis1509.farmerslife;

import org.bukkit.Material;
import static org.bukkit.Bukkit.getLogger;

public class DepositCrop {

    Material material;
    double reward;
    double bonusMultiplier;
    boolean bonusActive;
    String type = null;

    public DepositCrop(Material material, double reward, String type) {
        this.material = material;
        this.reward = reward;
        if (type != null)
            this.type = type;
    }

    public double getReward() {
        return reward * bonusMultiplier;
    }

    public Material getMaterial() {
        return material;
    }

    public void setBonusActive(boolean bonusStatus, double bonusMultiplier) {
        if (bonusStatus)
            this.bonusMultiplier = bonusMultiplier;
        else
            this.bonusMultiplier = 1;
    }

    public boolean bonusAppliesToThisItem() {
        return type != null;
    }

    public String getType() {
        return type;
    }

    public double getMultiplier(){
        return bonusMultiplier;
    }

}
