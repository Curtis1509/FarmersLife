package curtis1509.farmerslife;

public class SkillProfits {

    double cost = 0;
    int level = 0;

    public SkillProfits(int level){
        this.level = level;
        for (int i = 0; i < level; i++){
            cost+=500;
            cost*=1.1;
        }
        if (level == 0)
            cost = 500;
    }

    public int getLevel(){
        return level;
    }

    public double getMultiplier(){
        return 1 + level*0.1;
    }

    public double getCost(){
        return cost;
    }

    public boolean LevelUp(Player player, double money){
        if (money >= cost){
            player.removeCash(cost);
            level++;
            cost+=500;
            cost*=1.1;
            return true;
        }
        else
            return false;
    }

}
