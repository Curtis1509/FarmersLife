package curtis1509.farmerslife;

import dev.jcsoftware.jscoreboards.JPerPlayerMethodBasedScoreboard;
import dev.jcsoftware.jscoreboards.JScoreboardTeam;
import org.bukkit.inventory.Inventory;

import static org.bukkit.Bukkit.getLogger;

public class Player {

    String playerName;
    JPerPlayerMethodBasedScoreboard scoreboard;
    JScoreboardTeam team;
    org.bukkit.entity.Player player;
    Inventory deathInventory;
    int deathInventoryi = 3;
    Skills skills;
    double todaysCash = 0;

    public Player(org.bukkit.entity.Player player, double cash, int profitSkill, boolean creative, boolean protection, boolean bedperk, boolean teleport) {
        skills = new Skills(profitSkill, creative, protection, bedperk, teleport);
        this.player = player;
        this.playerName = player.getName();
        scoreboard = new JPerPlayerMethodBasedScoreboard();
        scoreboard.setTitle(player, "Farmers HUD");
        scoreboard.setLines(
                player, "$" + cash
        );
        scoreboard.addPlayer(player);
        scoreboard.updateScoreboard();
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
        scoreboard.setTitle(player, "Farmers HUD");
        scoreboard.setLines(
                player, "$" + FarmersLife.economy.getBalance(player)
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
        FarmersLife.economy.withdrawPlayer(player, amount);
        scoreboard.setLines(
                player, "$" + FarmersLife.economy.getBalance(player)
        );
        scoreboard.updateScoreboard();
    }

    public double getCash() {
        return FarmersLife.economy.getBalance(player);
    }

    public void updateScoreboard(String time) {
        scoreboard.setLines(
                player, "$" + Math.floor(FarmersLife.economy.getBalance(player)), time, "Weather Season: " + FarmersLife.weather, "Days Remaining: " + FarmersLife.dayNumber
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
        FarmersLife.economy.depositPlayer(player, cash);
        scoreboard.setTitle(player, "Farmers HUD");
        scoreboard.setLines(
                player, "$" + Math.floor(FarmersLife.economy.getBalance(player)));
        scoreboard.updateScoreboard();
    }

}
