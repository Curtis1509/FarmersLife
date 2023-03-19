package curtis1509.farmerslife;

import dev.jcsoftware.jscoreboards.JPerPlayerMethodBasedScoreboard;
import dev.jcsoftware.jscoreboards.JScoreboardTeam;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.HashMap;
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
    private HashMap<String, Double> dailyDepositHistory = new HashMap<>();


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

    public HashMap<String, Double> getDailyDepositHistory(){
        return dailyDepositHistory;
    }

    public void updateDailyDepositHistory(String item, double amount){
        if (dailyDepositHistory.containsKey(item))
            dailyDepositHistory.replace(item, amount + dailyDepositHistory.get(item));
        else
            dailyDepositHistory.put(item, amount);
    }

    public void clearDailyDepositHistory(){
        dailyDepositHistory.clear();
    }

    public void refreshPlayer(){
        setPlayer(player);
        reloadScoreboard();
        scoreboard.updateScoreboard();
        reloadPlayerName();
    }

    public LinkedList<ItemStack> getDeliveryOrder() {
        return deliveryOrder;
    }

    public void addDelivery(ItemStack itemStack) {
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

    public void updateScoreboard(String time) {

        DecimalFormat df = new DecimalFormat("#.##");
        df.setGroupingUsed(true);
        df.setGroupingSize(3);

        scoreboard.setLines(
                player, "&6$" + df.format(Math.floor(economy.getBalance(player))), time, "Weather Season: &6" + weather, "Days Remaining: &6" + Functions.normalizeDayNumber()
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

}
