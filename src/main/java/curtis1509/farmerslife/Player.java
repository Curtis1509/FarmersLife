package curtis1509.farmerslife;

import dev.jcsoftware.jscoreboards.JPerPlayerMethodBasedScoreboard;
import dev.jcsoftware.jscoreboards.JScoreboardTeam;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;

import static curtis1509.farmerslife.FarmersLife.*;
import static org.bukkit.Bukkit.getLogger;

public class Player {

    String playerName;
    JPerPlayerMethodBasedScoreboard scoreboard;
    JScoreboardTeam team;
    org.bukkit.entity.Player player;
    Inventory deathInventory;
    int golemIronSoldToday = 0;
    int deathInventoryi = 3;
    Skills skills;
    double todaysCash = 0;
    boolean inPen = false;
    LinkedList<ItemStack> deliveryOrder = new LinkedList<ItemStack>();


    public Player(org.bukkit.entity.Player player, int profitSkill, boolean protection, boolean bedperk, boolean teleport) {
        skills = new Skills(profitSkill, protection, bedperk, teleport, player);
        this.player = player;
        this.playerName = player.getName();
        scoreboard = new JPerPlayerMethodBasedScoreboard();
        scoreboard.setTitle(player, "&aFarmers HUD");
        scoreboard.setLines(
                player, "&6$" + economy.getBalance(player)
        );
        scoreboard.addPlayer(player);
        scoreboard.updateScoreboard();
    }

    public LinkedList<ItemStack> getDeliveryOrder(){
        return deliveryOrder;
    }
    public void addDelivery(ItemStack itemStack){
        deliveryOrder.add(itemStack);
    }

    public double getTodaysCash() {
        return todaysCash;
    }

    public void addToTodaysCash(double addCash) {
        todaysCash += addCash;
    }

    public void resetTodaysCash() {
        todaysCash = 0;
    }

    public void reloadPlayerName() {
        this.playerName = player.getName();
    }

    public void reloadScoreboard() {
        scoreboard = new JPerPlayerMethodBasedScoreboard();
        scoreboard.setTitle(player, "&aFarmers HUD");
        scoreboard.setLines(
                player, "&6$" + economy.getBalance(player)
        );
        scoreboard.addPlayer(player);
        scoreboard.updateScoreboard();
    }

    public Skills getSkills() {
        return skills;
    }

    public void setPlayer(org.bukkit.entity.Player player) {
        this.player = player;
    }

    public void storeInventory(Inventory inventory) {
        this.deathInventory = inventory;
        getLogger().info("storing inventory " + playerName);
    }

    public Inventory getDeathInventory() {
        return deathInventory;
    }

    public void clearDeathInventory() {
        deathInventory = null;
    }

    public void removeCash(double amount) {
        economy.withdrawPlayer(player, amount);
        scoreboard.setLines(
                player, "&a$&6" + economy.getBalance(player)
        );
        scoreboard.updateScoreboard();
    }

    public double getCash() {
        return economy.getBalance(player);
    }

    public void updateScoreboard(String time) {
        scoreboard.setLines(
                player, "&6$" + Math.floor(economy.getBalance(player)), time, "Weather Season: &6" + weather, "Days Remaining: &6" + dayNumber
        );
        scoreboard.updateScoreboard();
    }

    public String getName() {
        return player.getName();
    }

    public org.bukkit.entity.Player getPlayer() {
        return player;
    }

    public JPerPlayerMethodBasedScoreboard getScoreboard() {
        return scoreboard;
    }

    public void addCash(double cash) {
        economy.depositPlayer(player, cash);
        scoreboard.setTitle(player, "Farmers HUD");
        scoreboard.setLines(
                player, "$" + Math.floor(economy.getBalance(player)));
        scoreboard.updateScoreboard();
    }

}
