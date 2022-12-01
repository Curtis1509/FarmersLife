package curtis1509.farmerslife;

import org.bukkit.Location;

public class Pen {
    Location pointA;
    Location pointB;
    String owner;
    int id;

    public Pen(Location pointA, Location pointB, String owner, int id){
        this.pointA = pointA;
        this.pointB = pointB;
        this.owner = owner;
        this.id = id;
    }

    public boolean insidePen(Location location){
        boolean insideX = false;
        if (pointA.getX() < pointB.getX()) {
            if (location.getX() > pointA.getX() && location.getX() < pointB.getX()) {
                insideX = true;
            }
        }else{
            if (location.getX() < pointA.getX() && location.getX() > pointB.getX()) {
                insideX = true;
            }
        }
        boolean insideZ = false;
        if (pointA.getZ() < pointB.getZ()) {
            if (location.getZ() > pointA.getZ() && location.getZ() < pointB.getZ()) {
                insideZ = true;
            }
        }else{
            if (location.getZ() < pointA.getZ() && location.getZ() > pointB.getZ()) {
                insideZ = true;
            }
        }
        boolean insideY = false;
        if (pointA.getY() < pointB.getY()) {
            if (location.getY() > pointA.getY() && location.getY() < pointB.getY()) {
                insideY = true;
            }
        }else{
            if (location.getY() < pointA.getY() && location.getY() > pointB.getY()) {
                insideY = true;
            }
        }
        return insideZ && insideX && insideY;
    }

}
