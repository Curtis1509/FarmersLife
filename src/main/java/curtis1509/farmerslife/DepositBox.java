package curtis1509.farmerslife;

import org.bukkit.block.Block;

public class DepositBox {

    Block chest;
    String owner;
    int id;

    public DepositBox(Block chest, String owner, int id){
        this.chest = chest;
        this.owner = owner;
        this.id = id;
    }

    public Block getDepositBox(){
        return chest;
    }

    public String getOwner(){
        return owner;
    }

    public int getID(){
        return id;
    }

}
