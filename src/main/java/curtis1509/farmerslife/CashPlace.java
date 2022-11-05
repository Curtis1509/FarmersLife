package curtis1509.farmerslife;

public class CashPlace {
    String player;
    double cash;
    public CashPlace(String player, double cash){
        this.player = player;
        this.cash = cash;
    }

    public double getCash(){
        return cash;
    }
    public String getPlayer(){
        return player;
    }
}
