package curtis1509.farmerslife;

import org.bukkit.block.Block;

public class DepositBox {

    boolean shipmentBox;
    Block chest;
    String owner;
    int id;

    public DepositBox(Block chest, String owner, int id, boolean shipmentBox){
        this.chest = chest;
        this.owner = owner;
        this.id = id;
        this.shipmentBox = shipmentBox;
    }

    public void makeShipmentBox(){
        shipmentBox = true;
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

    public boolean isShipmentBox(){
        return shipmentBox;
    }

}
