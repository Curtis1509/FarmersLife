package curtis1509.farmerslife;

import dev.jcsoftware.jscoreboards.JPerPlayerMethodBasedScoreboard;
import dev.jcsoftware.jscoreboards.JScoreboardTeam;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.Bukkit.getLogger;

public class Player {

    int cash;
    String playerName;
    JPerPlayerMethodBasedScoreboard scoreboard;
    JScoreboardTeam team;
    org.bukkit.entity.Player player;
    Inventory deathInventory;
    int deathInventoryi = 3;

    public Player(org.bukkit.entity.Player player, int cash){
        this.cash = cash;
        this.player = player;
        this.playerName = player.getName();
        scoreboard = new JPerPlayerMethodBasedScoreboard();
        scoreboard.setTitle(player,"Farmers HUD");
        scoreboard.setLines(
                player,"$" + cash
        );
        scoreboard.addPlayer(player);
        scoreboard.updateScoreboard();
    }

    public void setPlayer(org.bukkit.entity.Player player){
        this.player = player;
    }

    public void storeInventory(Inventory inventory){
        this.deathInventory = inventory;
        getLogger().info("storing inventory " + playerName);
    }

    public Inventory getDeathInventory(){
        return deathInventory;
    }

    public void clearDeathInventory(){
        deathInventory = null;
    }

    public void removeCash(int amount){
        cash-=amount;
        scoreboard.setLines(
                player,"$" + this.cash
        );
        scoreboard.updateScoreboard();
    }

    public int getCash(){
        return cash;
    }

    public void updateScoreboard(String time){
        scoreboard.setLines(
                player,"$" + this.cash,time
        );
        scoreboard.updateScoreboard();
    }

    public String getName(){
        return player.getName();
    }

    public org.bukkit.entity.Player getPlayer(){
        return player;
    }

    public JPerPlayerMethodBasedScoreboard getScoreboard(){
        return scoreboard;
    }

    public void addCash(int cash){
        this.cash+=cash;
        scoreboard.setTitle(player,"Farmers HUD");
        scoreboard.setLines(
                player,"$" + this.cash
        );
        scoreboard.updateScoreboard();
    }

}
