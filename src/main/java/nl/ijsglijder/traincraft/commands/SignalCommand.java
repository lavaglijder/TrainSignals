package nl.ijsglijder.traincraft.commands;

import nl.ijsglijder.traincraft.TrainCraft;
import nl.ijsglijder.traincraft.signals.LookingDirection;
import nl.ijsglijder.traincraft.signals.SignalClass;
import nl.ijsglijder.traincraft.signals.SignalManager;
import nl.ijsglijder.traincraft.signals.SignalVector;
import nl.ijsglijder.traincraft.signals.signalTypes.SignalType;
import nl.ijsglijder.traincraft.signals.signalTypes.StationSignal;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SignalCommand implements CommandExecutor, Listener {

    //Creating
    HashMap<Player, Integer> playerCreateStage = new HashMap<>();
    HashMap<Player, Location> playerLocationSigStage = new HashMap<>();
    // Redstone signal location for station
    HashMap<Player, Location> playerLocationStaStage = new HashMap<>();
    // Redstone torch location for speed limiter
    HashMap<Player, Location> playerLocationSpeedStage = new HashMap<>();
    HashMap<Player, Location> playerLocationBlock1Stage = new HashMap<>();
    HashMap<Player, Location> playerLocationBlock2Stage = new HashMap<>();
    HashMap<Player, String> playerSignalName = new HashMap<>();
    HashMap<Player, LookingDirection> playerDirectionStage = new HashMap<>();
    HashMap<Player, List<String>> playerLinkStage = new HashMap<>();
    HashMap<Player, SignalType> playerSignalType = new HashMap<>();
    HashMap<Player, Integer> playerStationLength = new HashMap<>();
    HashMap<Player, String> playerStationLink = new HashMap<>();

    //Deletion
    List<Player> playerDeleteStage = new ArrayList<>();

    //Linking
    HashMap<Player, SignalClass> signalClassHashMap = new HashMap<>();
    HashMap<Player, Integer> linkingStage = new HashMap<>();
    HashMap<Player, List<String>> signalLinks = new HashMap<>();
    List<Player> unLinkingPlayers = new ArrayList<>();

    public SignalCommand() {
        Bukkit.getServer().getPluginManager().registerEvents(this, TrainCraft.getPlugin(TrainCraft.class));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,  @NotNull String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("Sorry you cannot use this command!");
            return true;
        }

        Player player = (Player) sender;

        if(args.length == 0) {
            player.sendMessage(ChatColor.GREEN + "Signal command help:\n\n/signal create\nCreate a new signal");
            return true;
        }

        if(args[0].equalsIgnoreCase("create")) {
            playerCreateStage.put(player, 1);
            player.sendMessage(ChatColor.DARK_AQUA + "Please type the signal name");
            return true;
        }

        if(args[0].equalsIgnoreCase("delete")) {
            playerDeleteStage.add(player);
            player.sendMessage(ChatColor.DARK_AQUA + "Hit the cobblestone wall of the signal to remove it.");
            return true;
        }

        if(args[0].equalsIgnoreCase("link")) {
            linkingStage.put(player, 1);
            player.sendMessage(ChatColor.DARK_AQUA + "Hit the cobblestone wall of the signal you want to select");
            return true;
        }

        if(args[0].equalsIgnoreCase("unlink")) {
            linkingStage.put(player, 1);
            unLinkingPlayers.add(player);
            player.sendMessage(ChatColor.DARK_AQUA + "Hit the cobblestone wall of the signal you want to select");
            return true;
        }

        if(args[0].equalsIgnoreCase("refreshdetector")) {
            if(args.length < 2) {
                player.sendMessage(ChatColor.GRAY + "Usage: /signal refreshdetector (signalID)");
                return true;
            }
            String signalID = args[1];
            SignalClass signalClass = TrainCraft.getSignalManager().getSignal(signalID);
            if(signalClass == null) {
                player.sendMessage(ChatColor.RED + "Cannot find the signal");
                return true;
            }
            signalClass.recalculateDetector();
            player.sendMessage(ChatColor.GRAY + "DetectorRegion has been refreshed");
            return true;
        }



        return true;
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if(playerCreateStage.containsKey(player)) {
            event.setCancelled(true);

            int stage = playerCreateStage.get(player);


            switch(stage) {
                case 1:
                    playerSignalName.put(player, event.getMessage().split(" ")[0]);
                    playerCreateStage.replace(player, 2);
                    player.sendMessage(ChatColor.DARK_AQUA + "Please look at the direction that you want the signal to be faced and say \"done\"");
                    break;
                case 2:
                    if(event.getMessage().equalsIgnoreCase("done")) {
                        double rotation = (((player.getLocation().getYaw()) % 360) - 180);
                        if (rotation < 0) {
                            rotation += 360.0;
                        }
                        LookingDirection lookingDirection = LookingDirection.getDirection(rotation);

                        playerDirectionStage.put(player, lookingDirection);
                        playerCreateStage.replace(player, 3);
                        assert lookingDirection != null;
                        player.sendMessage(ChatColor.DARK_AQUA + "Please select the ground where the signal is going to be. You selected " + lookingDirection.toString() + " | " + rotation);
                        break;
                    }
                    player.sendMessage(ChatColor.DARK_AQUA + "Please look at the direction that you want the signal to be faced and say \"done\"");
                    break;
                case 3:
                    player.sendMessage(ChatColor.DARK_AQUA + "Please select the ground where the signal is going to be");
                    break;
                case 4:
                    player.sendMessage(ChatColor.DARK_AQUA + "Please select the block where the station redstone input is going to be");
                    break;
                case 5:
                    player.sendMessage(ChatColor.DARK_AQUA + "Please select the block where the train will slowdown on yellow signal is going to be");
                    break;
                case 6:
                    player.sendMessage(ChatColor.DARK_AQUA + "Please select the rail where the first train block location is");
                    break;
                case 7:
                    player.sendMessage(ChatColor.DARK_AQUA + "Please select the rail where the second train block location is");
                    break;
                case 8:
                    if(event.getMessage().equalsIgnoreCase("done")) {
                        player.sendMessage(ChatColor.DARK_AQUA + "What type of signal do you want, types are\n\nNormal\nStation");
                        playerCreateStage.replace(player, 9);
                        break;
                    }
                    player.sendMessage(ChatColor.DARK_AQUA + "Select the linked signals. Type \"done\" when you linked all the signals");
                    break;
                case 9:
                    SignalType type = SignalType.valueOf(event.getMessage().toUpperCase());
                    switch(type) {
                        case NORMAL:
                            player.sendMessage("Finished setup");
                            playerSignalType.put(player, type);
                            finishSetup(player);
                            playerCreateStage.remove(player);
                            break;
                        case STATION:
                            playerSignalType.put(player, type);
                            playerCreateStage.replace(player, 10);
                            player.sendMessage(ChatColor.DARK_AQUA + "Please type the length in seconds that the trains need to wait at the station.");
                            break;
                        default:
                            player.sendMessage(ChatColor.DARK_AQUA + "What type of signal do you want, types are\n\nNormal\nStation");
                            break;
                    }
                    break;
                case 10:

                    switch(playerSignalType.get(player)) {
                        case STATION:
                            try {
                                int i = Integer.parseInt(event.getMessage());
                                playerStationLength.put(player, i);
                                player.sendMessage(ChatColor.DARK_AQUA + "Select the signal before the station");
                                playerCreateStage.replace(player, 11);
                            } catch(NumberFormatException e) {
                                player.sendMessage(ChatColor.DARK_AQUA + "Please type the length in seconds that the trains need to wait at the station.");
                            }
                            break;
                        case NORMAL:
                            break;
                    }
                    break;
                default:
                    player.sendMessage("Error!");
                    playerCreateStage.remove(player);
                    break;
            }
        }
        if(linkingStage.containsKey(player)) {
            event.setCancelled(true);

            switch(linkingStage.get(player)) {
                case 1:
                    player.sendMessage(ChatColor.DARK_AQUA + "Hit the cobblestone wall of the signal you want to select");
                    break;
                case 2:
                    if(event.getMessage().equalsIgnoreCase("done")) {
                        SignalClass signalClass = signalClassHashMap.get(player);
                        FileConfiguration signals = TrainCraft.getFileManager().getFile("signals.yml").getFc();
                        List<String> strings = signals.getStringList(signalClass.getSignalID() + ".linkedSignals");
                        signalLinks.get(player).forEach(s -> {
                            if(unLinkingPlayers.contains(player)) {
                                TrainCraft.getSignalManager().removeLink(s, signalClass.getSignalID());
                                strings.remove(s);
                            } else {
                                TrainCraft.getSignalManager().addLink(s, signalClass.getSignalID());
                                strings.add(s);
                            }
                        });

                        signals.set(signalClass.getSignalID() + ".linkedSignals", strings);

                        linkingStage.remove(player);
                        if(unLinkingPlayers.contains(player)) {
                            player.sendMessage("Finished unlinking");
                            unLinkingPlayers.remove(player);
                        } else{
                            player.sendMessage("Finished linking");
                        }
                        break;
                    }
                    if(unLinkingPlayers.contains(player)) {
                        player.sendMessage("Now select the signals that you want to remove the link");
                    } else {
                        player.sendMessage("Now select the signals that you want to link it with");
                    }
                    break;
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        SignalManager signalManager = TrainCraft.getSignalManager();
        if(playerCreateStage.containsKey(player)) {
            Block block = event.getClickedBlock();
            if(block == null || block.getType() == Material.AIR) {
                return;
            }

            int stage = playerCreateStage.get(player);


            switch(stage) {
                case 1:
                case 2:
                case 9:
                case 10:
                    break;
                case 3:
                    event.setCancelled(true);
                    playerLocationSigStage.put(player, block.getLocation().add(0,1,0));
                    playerCreateStage.replace(player, 4);
                    player.sendMessage(ChatColor.DARK_AQUA + "Please select the block where the station redstone input is going to be");
                    break;
                case 4:
                    event.setCancelled(true);
                    playerLocationStaStage.put(player, block.getLocation());
                    playerCreateStage.replace(player, 5);
                    player.sendMessage(ChatColor.DARK_AQUA + "Please select the block where the train will slowdown on yellow signal is going to be");
                    break;
                case 5:
                    event.setCancelled(true);
                    playerLocationSpeedStage.put(player, block.getLocation());
                    playerCreateStage.replace(player, 6);
                    player.sendMessage(ChatColor.DARK_AQUA + "Please select the rail where the first train block location is");

                    break;
                case 6:
                    event.setCancelled(true);
                    playerLocationBlock1Stage.put(player, block.getLocation());
                    playerCreateStage.replace(player, 7);
                    player.sendMessage(ChatColor.DARK_AQUA + "Please select the rail where the second train block location is");
                    break;
                case 7:
                    event.setCancelled(true);
                    playerLocationBlock2Stage.put(player, block.getLocation());
                    playerCreateStage.replace(player, 8);
                    playerLinkStage.put(player, new ArrayList<>());
                    player.sendMessage(ChatColor.DARK_AQUA + "Select the linked signals. Type \"done\" when you linked all the signals");
                    break;
                case 8:
                    event.setCancelled(true);
                    if(block.getType() != Material.COBBLESTONE_WALL || TrainCraft.getSignalManager().getSignal(new SignalVector(block.getLocation())) == null) {
                        player.sendMessage(ChatColor.DARK_AQUA + "Please hit the cobblestone wall of the signal!");
                        break;
                    }
                    List<String> stringList = playerLinkStage.get(player);
                    stringList.add(TrainCraft.getSignalManager().getSignal(new SignalVector(block.getLocation())).getSignalID());
                    playerLinkStage.replace(player, stringList);
                    player.sendMessage(TrainCraft.getSignalManager().getSignal(new SignalVector(block.getLocation())).toString() + " has been added to the links, type \"done\" when you are done");
                    break;
                case 11:
                    switch(playerSignalType.get(player)) {
                        case STATION:
                            event.setCancelled(true);
                            if(block.getType() != Material.COBBLESTONE_WALL || TrainCraft.getSignalManager().getSignal(new SignalVector(block.getLocation())) == null) {
                                player.sendMessage(ChatColor.DARK_AQUA + "Please hit the cobblestone wall of the signal!");
                                break;
                            }
                            playerStationLink.put(player, TrainCraft.getSignalManager().getSignal(new SignalVector(block.getLocation())).getSignalID());
                            player.sendMessage("Finish");
                            finishSetup(player);
                            playerCreateStage.remove(player);
                            break;
                        case NORMAL:
                            break;
                    }
                    break;
                default:
                    player.sendMessage("Error!");
                    playerCreateStage.remove(player);
                    break;
            }
        }
        if(playerDeleteStage.contains(player)) {
            Block block = event.getClickedBlock();
            if(block == null || block.getType() == Material.AIR || event.getHand() != EquipmentSlot.HAND) {
                return;
            }

            if(block.getType() != Material.COBBLESTONE_WALL) {
                player.sendMessage(ChatColor.GREEN + "You need to hit the cobblestone wall of the signal!") ;
            }

            SignalClass signal = signalManager.getSignal(new SignalVector(block.getLocation()));

            if(signal == null) {
                player.sendMessage(ChatColor.RED + "That is not a signal!");
                return;
            }

            signalManager.removeSignal(signal, true);
            playerDeleteStage.remove(player);
            player.sendMessage(ChatColor.RED + "removed the signal!");
        }
        if(linkingStage.containsKey(player)) {
            Block block = event.getClickedBlock();
            event.setCancelled(true);
            if(block == null || block.getType() == Material.AIR || event.getHand() != EquipmentSlot.HAND) {
                return;
            }

            if(block.getType() != Material.COBBLESTONE_WALL) {
                player.sendMessage(ChatColor.GREEN + "You need to hit the cobblestone wall of the signal!") ;
            }

            switch(linkingStage.get(player)) {
                case 1:
                    SignalClass signal = signalManager.getSignal(new SignalVector(block.getLocation()));

                    if(signal == null) {
                        player.sendMessage(ChatColor.RED + "That is not a signal!");
                        break;
                    }
                    signalClassHashMap.put(player, signal);
                    if(unLinkingPlayers.contains(player)) {
                        player.sendMessage("Now select the signals that you want to remove the link");
                    } else {
                        player.sendMessage("Now select the signals that you want to link it with");
                    }
                    linkingStage.replace(player, 2);
                    signalLinks.put(player, new ArrayList<>());
                    break;
                case 2:
                    SignalClass signal2 = signalManager.getSignal(new SignalVector(block.getLocation()));

                    if(signal2 == null) {
                        player.sendMessage(ChatColor.RED + "That is not a signal!");
                        break;
                    }
                    List<String> linkedSignals = signalLinks.get(player);
                    linkedSignals.add(signal2.getSignalID());
                    signalLinks.replace(player, linkedSignals);
                    if(unLinkingPlayers.contains(player)) {
                        player.sendMessage("Added " + signal2.getSignalID() + " to the unlinked signals. Type \"done\" to finish the linking");
                    } else {
                        player.sendMessage("Added " + signal2.getSignalID() + " to the linked signals. Type \"done\" to finish the linking");
                    }
            }


        }
    }

    public void finishSetup(Player player) {

        SignalManager signalManager = TrainCraft.getSignalManager();
        FileConfiguration signalsData = TrainCraft.getFileManager().getFile("signals.yml").getFc();

        String name = playerSignalName.get(player);

        Location signalLoc = playerLocationSigStage.get(player);
        signalsData.set(name + ".coords", new SignalVector(signalLoc).toString());
        playerLocationSigStage.remove(player);

        Location stationLoc = playerLocationStaStage.get(player);
        signalsData.set(name + ".coordsStation", new SignalVector(stationLoc).toString());
        playerLocationStaStage.remove(player);

        Location speedLimitLoc = playerLocationSpeedStage.get(player);
        signalsData.set(name + ".coordsSpeedlimitSetter", new SignalVector(speedLimitLoc).toString());
        playerLocationSpeedStage.remove(player);

        Location block1Loc = playerLocationBlock1Stage.get(player);
        signalsData.set(name + ".coordsBlock1", new SignalVector(block1Loc).toString());
        playerLocationBlock1Stage.remove(player);

        Location block2Loc = playerLocationBlock2Stage.get(player);
        signalsData.set(name + ".coordsBlock2", new SignalVector(block2Loc).toString());
        playerLocationBlock2Stage.remove(player);

        LookingDirection direction = playerDirectionStage.get(player);
        signalsData.set(name + ".direction", direction.toString());
        playerDirectionStage.remove(player);

        List<String> links = playerLinkStage.get(player);
        signalsData.set(name + ".linkedSignals", links);
        playerLinkStage.remove(player);

        SignalType type = playerSignalType.get(player);
        signalsData.set(name + ".type", type.toString());

        switch(playerSignalType.get(player)) {
            case STATION:
                int  waitLength = playerStationLength.get(player);
                signalsData.set(name + ".waitTime", waitLength);
                playerStationLength.remove(player);

                String linkedSignal = playerStationLink.get(player);
                signalsData.set(name + ".stationSignal", linkedSignal);
                playerStationLink.remove(player);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        signalManager.addSignal(new StationSignal(signalLoc, name, direction, stationLoc, speedLimitLoc, block1Loc, block2Loc, waitLength, linkedSignal));
                    }
                }.runTaskLater(TrainCraft.getPlugin(TrainCraft.class), 0);
                links.forEach(s -> signalManager.addLink(s, name));
                signalManager.addStationLink(linkedSignal, name);
                break;
            case NORMAL:
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        signalManager.addSignal(new SignalClass(signalLoc, name, direction, stationLoc, speedLimitLoc, block1Loc, block2Loc));
                    }
                }.runTaskLater(TrainCraft.getPlugin(TrainCraft.class), 0);
                links.forEach(s -> signalManager.addLink(name, s));
                break;
        }
        playerSignalType.remove(player);

        TrainCraft.getFileManager().getFile("signals.yml").save();
    }
}
