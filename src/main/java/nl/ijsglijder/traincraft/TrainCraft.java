package nl.ijsglijder.traincraft;

import nl.ijsglijder.traincraft.commands.SignalCommand;
import nl.ijsglijder.traincraft.files.FileManager;
import nl.ijsglijder.traincraft.files.SignalDataFile;
import nl.ijsglijder.traincraft.signals.LookingDirection;
import nl.ijsglijder.traincraft.signals.SignalClass;
import nl.ijsglijder.traincraft.signals.SignalManager;
import nl.ijsglijder.traincraft.signals.signalTypes.StationSignal;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class TrainCraft extends JavaPlugin {

    static FileManager fileManager;
    static SignalManager signalManager;

    @Override
    public void onEnable() {
        getLogger().info("Initializing plugin");
        fileManager = new FileManager();
        signalManager = new SignalManager();
        fileManager.addFile(new SignalDataFile());

        FileConfiguration signals = fileManager.getFile("signals.yml").getFc();

        signals.getKeys(false).forEach(key -> {
            String direction = signals.getString(key + ".direction");

            assert direction != null;
            LookingDirection lookingDirection = LookingDirection.valueOf(direction.toUpperCase());

            String[] coordsString1 = Objects.requireNonNull(signals.getString(key + ".coords")).split(";");
            Location loc = new Location(Bukkit.getWorld(coordsString1[0]), Integer.parseInt(coordsString1[1]), Integer.parseInt(coordsString1[2]), Integer.parseInt(coordsString1[3]));
            String[] coordsString2 = Objects.requireNonNull(signals.getString(key + ".coordsStation")).split(";");
            Location loc2 = new Location(Bukkit.getWorld(coordsString2[0]), Integer.parseInt(coordsString2[1]), Integer.parseInt(coordsString2[2]), Integer.parseInt(coordsString2[3]));
            String[] coordsString3 = Objects.requireNonNull(signals.getString(key + ".coordsSpeedlimitSetter")).split(";");
            Location loc3 = new Location(Bukkit.getWorld(coordsString3[0]), Integer.parseInt(coordsString3[1]), Integer.parseInt(coordsString3[2]), Integer.parseInt(coordsString3[3]));
            String[] coordsString4 = Objects.requireNonNull(signals.getString(key + ".coordsBlock1")).split(";");
            Location loc4 = new Location(Bukkit.getWorld(coordsString4[0]), Integer.parseInt(coordsString4[1]), Integer.parseInt(coordsString4[2]), Integer.parseInt(coordsString4[3]));
            String[] coordsString5 = Objects.requireNonNull(signals.getString(key + ".coordsBlock2")).split(";");
            Location loc5 = new Location(Bukkit.getWorld(coordsString5[0]), Integer.parseInt(coordsString5[1]), Integer.parseInt(coordsString5[2]), Integer.parseInt(coordsString5[3]));

            if(signals.contains(key + ".type") && Objects.requireNonNull(signals.getString(key + ".type")).equalsIgnoreCase("station")) {
                String stationSignal = signals.getString(key + ".stationSignal");
                int waitTime = signals.getInt(key + ".waitTime");
                signalManager.addSignal(new StationSignal(loc, key, lookingDirection, loc2, loc3, loc4, loc5, waitTime, stationSignal));
                signalManager.addStationLink(stationSignal, key);
            } else {
                signalManager.addSignal(new SignalClass(loc, key, lookingDirection, loc2, loc3, loc4, loc5));
            }



            signals.getStringList(key + ".linkedSignals").forEach(s -> signalManager.addLink(s, key));
            getLogger().info(signalManager.getSignals().size() + " signals loaded");
        });

        Objects.requireNonNull(getCommand("signal")).setExecutor(new SignalCommand());
    }

    @Override
    public void onDisable() {
        fileManager.saveAll();
        getLogger().info("Saved all files");
    }

    public static FileManager getFileManager() {
        return fileManager;
    }

    public static SignalManager getSignalManager() {
        return signalManager;
    }
}
