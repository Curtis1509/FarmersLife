package curtis1509.farmerslife;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;

public class Box {

    Block chest;
    String owner;
    int id;

    public Box(Block chest, String owner, int id){
        this.chest = chest;
        this.owner = owner;
        this.id = id;
    }

    public Chest getBox(){
        return (Chest) chest.getState();
    }

    public String getOwner(){
        return owner;
    }

    public int getID(){
        return id;
    }

}
