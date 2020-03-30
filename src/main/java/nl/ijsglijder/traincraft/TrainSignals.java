package nl.ijsglijder.traincraft;

import nl.ijsglijder.traincraft.commands.SignalCommand;
import nl.ijsglijder.traincraft.files.FileManager;
import nl.ijsglijder.traincraft.files.SignalDataFile;
import nl.ijsglijder.traincraft.signals.LookingDirection;
import nl.ijsglijder.traincraft.signals.SignalClass;
import nl.ijsglijder.traincraft.signals.SignalManager;
import nl.ijsglijder.traincraft.signals.SignalVector;
import nl.ijsglijder.traincraft.signals.signalTypes.StationSignal;
import nl.ijsglijder.traincraft.signals.switcher.SwitcherSignal;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class TrainSignals extends JavaPlugin {

    static FileManager fileManager;
    static SignalManager signalManager;

    @Override
    public void onEnable() {
        getLogger().info("Initializing plugin");
        fileManager = new FileManager();
        signalManager = new SignalManager();
        fileManager.addFile(new SignalDataFile());

        FileConfiguration signals = fileManager.getFile("signals.yml").getFc();

        for (String key : signals.getKeys(false)) {
            String direction = signals.getString(key + ".direction");

            assert direction != null;
            LookingDirection lookingDirection = LookingDirection.valueOf(direction.toUpperCase());

            if(signals.contains(key + ".type") && Objects.requireNonNull(signals.getString(key + ".type")).equalsIgnoreCase("switcher")) {
                List<Location> signalCoords = asVectorListLoc(signals.getStringList(key + ".coords"));
                HashMap<SignalVector, Location> stationCoords = new HashMap<>();
                HashMap<SignalVector, Location> speedLimitCoords = new HashMap<>();
                Location detectorBlock1 = (new SignalVector(Objects.requireNonNull(signals.getString(key + ".coordsBlock1")))).asLocation();
                List<Location> detectorBlocks2 = asVectorListLoc(signals.getStringList(key + ".coordsBlock2"));
                HashMap<String, SignalVector> linkedSignals = new HashMap<>();
                Set<String> signalLinks = signals.getKeys(true);
                signalLinks.removeIf(signalLink -> (!signalLink.startsWith(key + ".switcherLink.") && !signalLink.startsWith(key + ".coordsStation.") && !signalLink.startsWith(key + ".coordsSpeedlimitSetter.")));
                for (String signalLink : signalLinks) {
                    if(signalLink.startsWith(key + ".switcherLink.")) {
                        String key2 = signalLink.replaceFirst(key + ".switcherLink.", "");
                        SignalVector value = new SignalVector(Objects.requireNonNull(signals.getString(signalLink)));
                        linkedSignals.put(key2, value);
                        signalManager.addSwitcherLink(key2, key);
                    } else if(signalLink.startsWith(key + ".coordsStation.")) {
                        String key2 = signalLink.replaceFirst(key + ".coordsStation.", "");
                        SignalVector keyVal = new SignalVector(key2);
                        Location value = new SignalVector(Objects.requireNonNull(signals.getString(signalLink))).asLocation();
                        stationCoords.put(keyVal, value);
                    } else if(signalLink.startsWith(key + ".coordsSpeedlimitSetter.")) {
                        String key2 = signalLink.replaceFirst(key + ".coordsSpeedlimitSetter.", "");
                        SignalVector keyVal = new SignalVector(key2);
                        Location value = new SignalVector(Objects.requireNonNull(signals.getString(signalLink))).asLocation();
                        speedLimitCoords.put(keyVal, value);
                    }
                }

                signalManager.addSignal(new SwitcherSignal(signalCoords, key, lookingDirection, stationCoords, speedLimitCoords, detectorBlock1, detectorBlocks2, linkedSignals));

                signals.getStringList(key + ".linkedSignals").forEach(s -> signalManager.addLink(s, key));
                continue;
            }
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
        }
        getLogger().info(signalManager.getSignals().size() + " signals loaded");
        registerCommand(Objects.requireNonNull(getCommand("signal")), new SignalCommand());
    }

    public void registerCommand(PluginCommand command, CommandExecutor commandExecutor) {
        command.setExecutor(commandExecutor);
        if(commandExecutor instanceof TabCompleter) {
            command.setTabCompleter((TabCompleter) commandExecutor);
        }
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

    public List<SignalVector> asVectorList(List<String> string) {
        List<SignalVector> returns = new ArrayList<>();
        for (String s : string) {
            returns.add(new SignalVector(s));
        }
        return returns;
    }

    public List<Location> asVectorListLoc(List<String> string) {
        List<Location> returns = new ArrayList<>();
        for (String s : string) {
            returns.add(new SignalVector(s).asLocation());
        }
        return returns;
    }

    public List<Location> asVectorListLoc(SignalVector[] signalVectors) {
        List<Location> returns = new ArrayList<>();
        for (SignalVector s : signalVectors) {
            returns.add(s.asLocation());
        }
        return returns;
    }

}
