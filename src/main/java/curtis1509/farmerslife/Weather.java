package curtis1509.farmerslife;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class Weather {

    private int lengthOfSeason;
    private int dayInSeason;
    private String nameOfSeason;
    private double chanceOfRainingInDrySeason;
    private double chanceOfRainingInWetSeason;
    private boolean rainingToday = false;
    private Random random = new Random();

    public Weather() {
        loadWeatherFile();
        setDailyWeather();
    }

    public void newDay() {
        updateSeason();
        setDailyWeather();
        saveWeatherFile();
    }

    private void loadWeatherFile() {
        FileConfiguration weatherConfig = FarmersLife.fileReader.getWeatherFileConfiguration();
        this.lengthOfSeason = weatherConfig.getInt("lengthOfSeason");
        this.dayInSeason = weatherConfig.getInt("dayInSeason");
        this.nameOfSeason = weatherConfig.getString("nameOfSeason");
        this.chanceOfRainingInDrySeason = weatherConfig.getDouble("chanceOfRainingInDrySeason");
        this.chanceOfRainingInWetSeason = weatherConfig.getDouble("chanceOfRainingInWetSeason");
    }

    private void saveWeatherFile() {
        FileConfiguration weatherConfig = FarmersLife.fileReader.getWeatherFileConfiguration();
        weatherConfig.set("dayInSeason", dayInSeason);
        weatherConfig.set("nameOfSeason", nameOfSeason);
        FarmersLife.fileReader.saveWeatherFile(weatherConfig);
    }

    public void setDailyWeather() {

        double diceRoll = random.nextDouble();
        double chance = chanceOfRainingInDrySeason;

        if (nameOfSeason.equals("Wet"))
            chance = chanceOfRainingInWetSeason;

        if (diceRoll <= chance){
            rainingToday = true;
        } else {
            rainingToday = false;
        }

    }

    public void enforceDailyWeather(){
        FarmersLife.world.setStorm(rainingToday);
    }

    public boolean isRainingToday(){
        return rainingToday;
    }

    private void updateSeason() {
        dayInSeason++;
        if (dayInSeason == lengthOfSeason + 1){
            dayInSeason = 1;
            if (nameOfSeason.equals("Wet")){
                nameOfSeason = "Dry";
                Functions.broadcast("The season is shifting into a dry weather season. Expect plenty of sunshine");
            }
            else {
                nameOfSeason = "Wet";
                Functions.broadcast("The season is shifting into a wet weather season. Expect plenty of rain");
            }
        }
    }

    public String getCurrentSeason() {
        return nameOfSeason;
    }

    public int getDayInSeason() {
        return dayInSeason;
    }

    public int getSeasonLength(){
        return lengthOfSeason;
    }

}
