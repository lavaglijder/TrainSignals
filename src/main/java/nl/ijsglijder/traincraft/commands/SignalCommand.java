package nl.ijsglijder.traincraft.commands;

import nl.ijsglijder.traincraft.TrainSignals;
import nl.ijsglijder.traincraft.signals.LookingDirection;
import nl.ijsglijder.traincraft.signals.SignalClass;
import nl.ijsglijder.traincraft.signals.SignalManager;
import nl.ijsglijder.traincraft.signals.SignalVector;
import nl.ijsglijder.traincraft.signals.signalTypes.SignalType;
import nl.ijsglijder.traincraft.signals.signalTypes.StationSignal;
import nl.ijsglijder.traincraft.signals.switcher.SwitcherSignal;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SignalCommand implements CommandExecutor, Listener, TabCompleter {

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

    //Switcher hashmaps
    HashMap<Player, SignalVector> playerSwitcherSelectedSignal = new HashMap<>();
    HashMap<Player, List<SignalVector>> playerSwitcherSignals = new HashMap<>();
    HashMap<Player, HashMap<SignalVector, SignalVector>> playerSwitcherStations = new HashMap<>();
    HashMap<Player, HashMap<SignalVector, SignalVector>> playerSwitcherSpeedlimiters = new HashMap<>();
    HashMap<Player, HashMap<String, SignalVector>> playerSwitcherLinks = new HashMap<>();
    HashMap<Player, List<SignalVector>> playerSwitcherBlock2 = new HashMap<>();

    //Deletion
    List<Player> playerDeleteStage = new ArrayList<>();

    //Linking
    HashMap<Player, SignalClass> signalClassHashMap = new HashMap<>();
    HashMap<Player, Integer> linkingStage = new HashMap<>();
    HashMap<Player, List<String>> signalLinks = new HashMap<>();
    List<Player> unLinkingPlayers = new ArrayList<>();

    public SignalCommand() {
        Bukkit.getServer().getPluginManager().registerEvents(this, TrainSignals.getPlugin(TrainSignals.class));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,  @NotNull String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("Sorry you cannot use this command!");
            return true;
        }

        Player player = (Player) sender;

        if(!player.hasPermission("trainsignals.use")) {
            player.sendMessage(ChatColor.RED + "You do not have enough perms to use this command.");
            return true;
        }

        if(args.length == 0) {
            player.sendMessage(ChatColor.GREEN + "Signal command help:\n" + ChatColor.AQUA + "\n/signal create    Create a new signal\n/signal delete    Delete a signal\n/signal link    add a link on a signal" +
                    "\n/signal unlink   delete a link on a signal\n/signal refreshdetector    refresh a detectorregion");
            return true;
        }

        if(args[0].equalsIgnoreCase("create")) {
            playerCreateStage.put(player, 0);
            player.sendMessage(ChatColor.DARK_AQUA + "What type of signal do you want, types are\n\nNormal\nStation\nSwitcher");
            return true;
        }

        if(args[0].equalsIgnoreCase("delete")) {
            playerDeleteStage.add(player);
            player.sendMessage(ChatColor.BLUE + "Hit the cobblestone wall of the signal to remove it.");
            return true;
        }

        if(args[0].equalsIgnoreCase("link")) {
            linkingStage.put(player, 1);
            player.sendMessage(ChatColor.BLUE + "Hit the cobblestone wall of the signal you want to select");
            return true;
        }

        if(args[0].equalsIgnoreCase("unlink")) {
            linkingStage.put(player, 1);
            unLinkingPlayers.add(player);
            player.sendMessage(ChatColor.BLUE + "Hit the cobblestone wall of the signal you want to select");
            return true;
        }

        if(args[0].equalsIgnoreCase("refreshdetector")) {
            if(args.length < 2) {
                player.sendMessage(ChatColor.BLUE + "Usage: /signal refreshdetector (signalID)");
                return true;
            }
            String signalID = args[1];
            SignalClass signalClass = TrainSignals.getSignalManager().getSignal(signalID);
            if(signalClass == null) {
                player.sendMessage(ChatColor.RED + "Cannot find the signal");
                return true;
            }
            signalClass.recalculateDetector();
            player.sendMessage(ChatColor.BLUE + "DetectorRegion has been refreshed");
            return true;
        }



        return true;
    }

    /*
    Stages:
        Global:
            0: Type choosing
            1: Name choosing
            2: Direction choosing
            3: Signal location > Switcher multiple
            4: Station red
            5: Speedlimit Red
            6: Block 1 location
        Switcher:
            7: Block 2 locations
            8: Selecting signal for linking
            9: link to signal
        Other:
            7: Block 2 location
            8: Add links
     */
    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if(playerCreateStage.containsKey(player)) {
            event.setCancelled(true);

            int stage = playerCreateStage.get(player);


            SignalType signalType = playerSignalType.getOrDefault(player, SignalType.NORMAL);
            switch(stage) {
                case 0:
                    SignalType type = SignalType.valueOf(message.toUpperCase());
                    switch(type) {
                        case NORMAL:
                        case SWITCHER:
                        case STATION:
                            playerSignalType.put(player, type);
                            playerCreateStage.replace(player, 1);
                            player.sendMessage(ChatColor.BLUE + "What will the name be of the signal");
                            break;
                        default:
                            player.sendMessage(ChatColor.DARK_AQUA + "What type of signal do you want, types are\n\nNormal\nStation\nSwitcher");
                            break;
                    }
                    break;
                case 1:
                    playerSignalName.put(player, message.split(" ")[0]);
                    playerCreateStage.replace(player, 2);
                    player.sendMessage(ChatColor.DARK_AQUA + "Please look at the direction that you want the signal to be faced and say \"done\"");
                    break;
                case 2:
                    if(message.equalsIgnoreCase("done")) {
                        double rotation = (((player.getLocation().getYaw()) % 360) - 180);
                        if (rotation < 0) {
                            rotation = rotation + 360.0;
                        }
                        LookingDirection lookingDirection = LookingDirection.getDirection(rotation);

                        playerDirectionStage.put(player, lookingDirection);
                        playerCreateStage.replace(player, 3);
                        assert lookingDirection != null;
                        player.sendMessage(ChatColor.BLUE + "Please select the ground where the signal is going to be. You selected " + lookingDirection.toString() + " as rotation");
                        break;
                    }
                    player.sendMessage(ChatColor.DARK_AQUA + "Please look at the direction that you want the signal to be faced and say \"done\"");
                    break;
                case 3:
                    if(signalType == SignalType.SWITCHER) {
                        if(playerSwitcherSignals.containsKey(player) && message.equalsIgnoreCase("done")) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    playerSwitcherSignals.get(player).forEach(signalVector -> signalVector.asLocation().clone().getBlock().setType(Material.COBBLESTONE_WALL));
                                }
                            }.runTask(TrainSignals.getPlugin(TrainSignals.class));
                            playerCreateStage.replace(player, 6);
                            player.sendMessage(ChatColor.DARK_AQUA + "Please select the rail where the first rail block Location is");
                            break;
                        }
                        player.sendMessage(ChatColor.BLUE + "Please select the ground where a signal is going to be");
                        break;
                    }
                    player.sendMessage(ChatColor.BLUE + "Please select the ground where the signal is going to be");
                    break;
                case 4:
                    if (signalType == SignalType.SWITCHER) {
                        player.sendMessage(ChatColor.DARK_AQUA + "Please select the block where the station redstone input of the signal being created is going to be");
                        break;
                    }
                    player.sendMessage(ChatColor.DARK_AQUA + "Please select the block where the station redstone input is going to be");
                    break;
                case 5:
                    if (signalType == SignalType.SWITCHER) {
                        player.sendMessage(ChatColor.BLUE + "Please select the block where the train will slowdown on yellow signal of the signal being created is going to be");
                        break;
                    }
                    player.sendMessage(ChatColor.BLUE + "Please select the block where the train will slowdown on yellow signal is going to be");
                    break;
                case 6:
                    player.sendMessage(ChatColor.DARK_AQUA + "Please select the rail where the first rail block location is");
                    break;
                case 7:
                    if(signalType == SignalType.SWITCHER) {
                        if(message.equalsIgnoreCase("done")) {
                            playerCreateStage.replace(player, 8);
                            player.sendMessage(ChatColor.DARK_AQUA + "Select the signal that you want to select. Please hit the cobblestone wall placed on the signal location");
                            break;
                        }
                        player.sendMessage(ChatColor.BLUE + "Please select the rails where the second rail blocks Locations are");
                        break;
                    }
                    player.sendMessage(ChatColor.BLUE + "Please select the rail where the second rail block location is");
                    break;
                case 8:
                    if(signalType == SignalType.SWITCHER) {
                        if(event.getMessage().equalsIgnoreCase("done")) {
                            if(playerSwitcherLinks.get(player) != null && playerSwitcherLinks.get(player).size() > 0) {
                                finishSetup(player);
                                player.sendMessage(ChatColor.GREEN + "Setup finished");
                                break;
                            }
                        }
                        player.sendMessage(ChatColor.DARK_AQUA + "Select the signal that you want to select. Please hit the cobblestone wall placed on the signal location");
                        break;
                    }
                    if(message.equalsIgnoreCase("done")) {
                        switch (signalType) {
                            case STATION:
                                playerCreateStage.replace(player, 9);
                                player.sendMessage(ChatColor.BLUE + "Please type the length in seconds that the trains need to wait at the station.");
                                break;
                            case NORMAL:
                                finishSetup(player);
                                player.sendMessage(ChatColor.GREEN + "Setup completed");
                                break;
                        }
                        break;
                    }
                    player.sendMessage(ChatColor.DARK_AQUA + "Select the linked signals. Type \"done\" when you linked all the signals");
                    break;
                case 9:
                    switch(signalType) {
                        case SWITCHER:
                            break;
                        case STATION:
                            try {
                                int i = Integer.parseInt(message);
                                playerStationLength.put(player, i);
                                player.sendMessage(ChatColor.BLUE + "Select the signal before the station");
                                playerCreateStage.replace(player, 10);
                            } catch(NumberFormatException e) {
                                player.sendMessage(ChatColor.RED + "Please type the length in seconds that the trains need to wait at the station.");
                            }
                            break;
                        case NORMAL:
                            break;
                    }
                    break;
                case 10:
                    player.sendMessage(ChatColor.BLUE + "Select the signal before the station");
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
                    if(message.equalsIgnoreCase("done")) {
                        SignalClass signalClass = signalClassHashMap.get(player);
                        FileConfiguration signals = TrainSignals.getFileManager().getFile("signals.yml").getFc();
                        List<String> strings = signals.getStringList(signalClass.getSignalID() + ".linkedSignals");
                        signalLinks.get(player).forEach(s -> {
                            if(unLinkingPlayers.contains(player)) {
                                TrainSignals.getSignalManager().removeLink(s, signalClass.getSignalID());
                                strings.remove(s);
                            } else {
                                TrainSignals.getSignalManager().addLink(s, signalClass.getSignalID());
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
        SignalManager signalManager = TrainSignals.getSignalManager();
        if(playerCreateStage.containsKey(player)) {
            Block block = event.getClickedBlock();
            if(block == null || block.getType() == Material.AIR) {
                return;
            }

            int stage = playerCreateStage.get(player);


            SignalType signalType = playerSignalType.getOrDefault(player, SignalType.NORMAL);
            final Location blockLocation = block.getLocation();
            final SignalVector blockSignalVector = new SignalVector(blockLocation);
            SignalClass blockSignal = TrainSignals.getSignalManager().getSignal(blockSignalVector);
            switch(stage) {
                case 3:
                    event.setCancelled(true);
                    if(signalType == SignalType.SWITCHER) {
                        if(!playerSwitcherSignals.containsKey(player)) playerSwitcherSignals.put(player, new ArrayList<>());
                        List<SignalVector> switcherSignals = playerSwitcherSignals.get(player);
                        if(switcherSignals == null) switcherSignals = new ArrayList<>();
                        SignalVector signalVector = new SignalVector(blockLocation.clone().add(0, 1, 0));
                        switcherSignals.add(signalVector);
                        playerSwitcherSignals.replace(player, switcherSignals);
                        playerSwitcherSelectedSignal.put(player, signalVector);

                        player.sendMessage(ChatColor.DARK_AQUA + "Please select the block where the station redstone input of the signal being created is going to be");
                        playerCreateStage.replace(player, 4);
                        break;
                    }
                    playerLocationSigStage.put(player, blockLocation.add(0,1,0));
                    playerCreateStage.replace(player, 4);
                    player.sendMessage(ChatColor.DARK_AQUA + "Please select the block where the station redstone input is going to be");
                    break;
                case 4:
                    event.setCancelled(true);
                    if(signalType == SignalType.SWITCHER) {
                        if(!playerSwitcherStations.containsKey(player)) playerSwitcherStations.put(player, new HashMap<>());
                        HashMap<SignalVector, SignalVector> switcherStations = playerSwitcherStations.get(player);
                        if(switcherStations == null) switcherStations = new HashMap<>();
                        switcherStations.put(playerSwitcherSelectedSignal.get(player), blockSignalVector);
                        playerSwitcherStations.replace(player, switcherStations);

                        player.sendMessage(ChatColor.BLUE + "Please select the block where the train will slowdown on yellow signal of the signal being created is going to be");
                        playerCreateStage.replace(player, 5);
                        break;
                    }
                    playerLocationStaStage.put(player, blockLocation);
                    playerCreateStage.replace(player, 5);
                    player.sendMessage(ChatColor.BLUE + "Please select the block where the train will slowdown on yellow signal is going to be");
                    break;
                case 5:
                    event.setCancelled(true);
                    if(signalType == SignalType.SWITCHER) {
                        if(!playerSwitcherSpeedlimiters.containsKey(player)) playerSwitcherSpeedlimiters.put(player, new HashMap<>());
                        HashMap<SignalVector, SignalVector> switcherSpeedlimiters = playerSwitcherSpeedlimiters.get(player);
                        if(switcherSpeedlimiters == null) switcherSpeedlimiters = new HashMap<>();
                        switcherSpeedlimiters.put(playerSwitcherSelectedSignal.get(player), blockSignalVector);
                        playerSwitcherSpeedlimiters.replace(player, switcherSpeedlimiters);

                        player.sendMessage(ChatColor.BLUE + "Signal added, to add another signal, please select the ground again. If you done with signal creation please type \"done\"");
                        playerCreateStage.replace(player, 3);
                        break;
                    }
                    playerLocationSpeedStage.put(player, blockLocation);
                    playerCreateStage.replace(player, 6);
                    player.sendMessage(ChatColor.DARK_AQUA + "Please select the rail where the first rail block Location is");
                    break;
                case 6:
                    event.setCancelled(true);
                    playerLocationBlock1Stage.put(player, blockLocation);
                    playerCreateStage.replace(player, 7);
                    if(signalType == SignalType.SWITCHER) {
                        player.sendMessage(ChatColor.BLUE + "Please select the rails where the second rail blocks Locations are. These are on the side where the rails split");
                        break;
                    }
                    player.sendMessage(ChatColor.BLUE + "Please select the rail where the second rail block Location is");
                    break;
                case 7:
                    event.setCancelled(true);
                    if(signalType == SignalType.SWITCHER) {
                        if(!playerSwitcherBlock2.containsKey(player)) playerSwitcherBlock2.put(player, new ArrayList<>());
                        List<SignalVector> signalVectors = playerSwitcherBlock2.get(player);

                        signalVectors.add(new SignalVector(blockLocation));
                        playerSwitcherBlock2.replace(player, signalVectors);
                        player.sendMessage(ChatColor.AQUA + "Type \"done\" when you selected all the blocks");
                        break;
                    }
                    playerLocationBlock2Stage.put(player, blockLocation);
                    playerCreateStage.replace(player, 8);
                    playerLinkStage.put(player, new ArrayList<>());
                    player.sendMessage(ChatColor.DARK_AQUA + "Select the linked signals. Type \"done\" when you linked all the signals");
                    break;
                case 8:
                    event.setCancelled(true);
                    if(signalType == SignalType.SWITCHER) {
                        if(!playerSwitcherSignals.get(player).contains(blockSignalVector)) {
                            player.sendMessage("This is not a signal what you are creating.");
                            break;
                        }
                        if(!playerSwitcherLinks.containsKey(player)) playerSwitcherLinks.put(player, new HashMap<>());
                        playerSwitcherSelectedSignal.replace(player, blockSignalVector);
                        playerCreateStage.replace(player, 9);
                        player.sendMessage(ChatColor.BLUE + "Please select the signal you are linking it with.");
                        break;
                    }
                    if(block.getType() != Material.COBBLESTONE_WALL || blockSignal == null) {
                        player.sendMessage(ChatColor.BLUE + "Please hit the cobblestone wall of the signal!");
                        break;
                    }
                    List<String> stringList = playerLinkStage.get(player);
                    stringList.add(blockSignal.getSignalID());
                    playerLinkStage.replace(player, stringList);
                    player.sendMessage(ChatColor.BLUE.toString() + blockSignal.getSignalID() + " has been added to the links, type \"done\" when you are done");
                    break;
                case 9:
                    event.setCancelled(true);
                    if(signalType == SignalType.SWITCHER) {
                        if(block.getType() != Material.COBBLESTONE_WALL || blockSignal == null) {
                            player.sendMessage(ChatColor.BLUE + "Please hit the cobblestone wall of the signal!");
                            break;
                        }
                        HashMap<String, SignalVector> switcherLinks = playerSwitcherLinks.get(player);
                        switcherLinks.put(blockSignal.getSignalID(), playerSwitcherSelectedSignal.get(player));
                        playerCreateStage.replace(player, 8);
                        player.sendMessage(ChatColor.DARK_AQUA + "Please select another signal or type \"done\" when you are done");
                        break;
                    }
                case 10:
                    switch(playerSignalType.get(player)) {
                        case STATION:
                            event.setCancelled(true);
                            if(block.getType() != Material.COBBLESTONE_WALL || blockSignal == null) {
                                player.sendMessage(ChatColor.DARK_AQUA + "Please hit the cobblestone wall of the signal!");
                                break;
                            }
                            playerStationLink.put(player, blockSignal.getSignalID());
                            player.sendMessage(ChatColor.DARK_AQUA + "Finish");
                            finishSetup(player);
                            playerCreateStage.remove(player);
                            break;
                        case NORMAL:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
        if(playerDeleteStage.contains(player)) {
            Block block = event.getClickedBlock();
            if(block == null || block.getType() == Material.AIR || event.getHand() != EquipmentSlot.HAND) {
                return;
            }

            if(block.getType() != Material.COBBLESTONE_WALL) {
                player.sendMessage(ChatColor.RED + "You need to hit the cobblestone wall of the signal!") ;
            }

            SignalClass signal = signalManager.getSignal(new SignalVector(block.getLocation()));

            if(signal == null) {
                player.sendMessage(ChatColor.RED + "That is not a signal!");
                return;
            }

            signalManager.removeSignal(signal, true);
            playerDeleteStage.remove(player);
            player.sendMessage(ChatColor.DARK_GREEN + "removed the signal!");
        }
        if(linkingStage.containsKey(player)) {
            Block block = event.getClickedBlock();
            event.setCancelled(true);
            if(block == null || block.getType() == Material.AIR || event.getHand() != EquipmentSlot.HAND) {
                return;
            }

            if(block.getType() != Material.COBBLESTONE_WALL) {
                player.sendMessage(ChatColor.RED + "You need to hit the cobblestone wall of the signal!") ;
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
                        player.sendMessage(ChatColor.AQUA + "Now select the signals that you want to remove the link");
                    } else {
                        player.sendMessage(ChatColor.AQUA + "Now select the signals that you want to link it with");
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
                        player.sendMessage(ChatColor.BLUE + "Added " + signal2.getSignalID() + " to the unlinked signals. Type \"done\" to finish the linking");
                    } else {
                        player.sendMessage(ChatColor.BLUE + "Added " + signal2.getSignalID() + " to the linked signals. Type \"done\" to finish the linking");
                    }
            }


        }
    }

    public void finishSetup(Player player) {

        SignalManager signalManager = TrainSignals.getSignalManager();
        FileConfiguration signalsData = TrainSignals.getFileManager().getFile("signals.yml").getFc();

        playerCreateStage.remove(player);

        String name = playerSignalName.get(player);
        playerSignalName.remove(player);

        SignalType signalType = playerSignalType.get(player);
        TrainSignals plugin = TrainSignals.getPlugin(TrainSignals.class);
        if(signalType == SignalType.SWITCHER) {
            signalsData.set(name + ".type", signalType.toString());
            playerSignalType.remove(player);

            HashMap<String, SignalVector> links = playerSwitcherLinks.get(player);
            HashMap<String, String> linksAsString = new HashMap<>();
            links.forEach((s, signalVector) -> linksAsString.put(s, signalVector.toString()));
            playerSwitcherLinks.remove(player);
            signalsData.set(name + ".switcherLink", linksAsString);

            List<SignalVector> signals = playerSwitcherSignals.get(player);
            List<String> signalsAsString = new ArrayList<>();
            signals.forEach(signalVector -> signalsAsString.add(signalVector.toString()));
            List<Location> signalsAsLoc = new ArrayList<>();
            signals.forEach(signalVector -> signalsAsLoc.add(signalVector.asLocation()));
            playerSwitcherSignals.remove(player);
            signalsData.set(name + ".coords", signalsAsString);

            playerSwitcherSelectedSignal.remove(player);

            List<SignalVector> blocks2 = playerSwitcherBlock2.get(player);
            List<String> blocks2AsString = new ArrayList<>();
            blocks2.forEach(signalVector -> blocks2AsString.add(signalVector.toString()));
            List<Location> blocks2AsLoc = new ArrayList<>();
            blocks2.forEach(signalVector -> blocks2AsLoc.add(signalVector.asLocation()));
            playerSwitcherBlock2.get(player);
            signalsData.set(name + ".coordsBlock2", blocks2AsString);

            HashMap<SignalVector, SignalVector> speedlimiters = playerSwitcherSpeedlimiters.get(player);
            HashMap<String, String> speedlimitersAsString = new HashMap<>();
            speedlimiters.forEach((s, signalVector) -> speedlimitersAsString.put(s.toString(), signalVector.toString()));
            HashMap<SignalVector, Location> speedLimitersAsLoc = new HashMap<>();
            speedlimiters.forEach((signalVector, signalVector2) -> speedLimitersAsLoc.put(signalVector, signalVector2.asLocation()));
            playerSwitcherSpeedlimiters.remove(player);
            signalsData.set(name + ".coordsSpeedlimitSetter", speedlimitersAsString);

            HashMap<SignalVector, SignalVector> stations = playerSwitcherStations.get(player);
            HashMap<String, String> stationsAsString = new HashMap<>();
            stations.forEach((s, signalVector) -> stationsAsString.put(s.toString(), signalVector.toString()));
            HashMap<SignalVector, Location> stationsAsLoc = new HashMap<>();
            stations.forEach((signalVector, signalVector2) -> stationsAsLoc.put(signalVector, signalVector2.asLocation()));
            playerSwitcherStations.remove(player);
            signalsData.set(name + ".coordsStation", stationsAsString);

            Location block1 = playerLocationBlock1Stage.get(player);
            playerLocationBlock1Stage.remove(player);
            signalsData.set(name + ".coordsBlock1", new SignalVector(block1).toString());

            LookingDirection direction = playerDirectionStage.get(player);
            playerDirectionStage.remove(player);
            signalsData.set(name + ".direction", direction.toString());

            new BukkitRunnable() {
                @Override
                public void run() {
                    signalManager.addSignal(new SwitcherSignal(signalsAsLoc, name, direction, stationsAsLoc, speedLimitersAsLoc, block1, blocks2AsLoc, links));
                }
            }.runTask(plugin);
            links.forEach((s, signalVector) -> signalManager.addSwitcherLink(s, name));
            return;
        }

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

        signalsData.set(name + ".type", signalType.toString());

        switch(signalType) {
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
                }.runTaskLater(plugin, 0);
                links.forEach(s -> signalManager.addLink(s, name));
                signalManager.addStationLink(linkedSignal, name);
                break;
            case NORMAL:
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        signalManager.addSignal(new SignalClass(signalLoc, name, direction, stationLoc, speedLimitLoc, block1Loc, block2Loc));
                    }
                }.runTaskLater(plugin, 0);
                links.forEach(s -> signalManager.addLink(name, s));
                break;
        }
        playerSignalType.remove(player);

        TrainSignals.getFileManager().getFile("signals.yml").save();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> strings = new ArrayList<>();

        if(args.length == 0) {
            strings.add("link");
            strings.add("unlink");
            strings.add("delete");
            strings.add("refreshdetector");
            strings.add("create");
        }
        if(args.length == 1) {
            strings.add("link");
            strings.add("unlink");
            strings.add("delete");
            strings.add("refreshdetector");
            strings.add("create");
        }
        if(args.length == 2) {
            switch(args[0]) {
                case "link":
                case "unlink":
                case "delete":
                case "create":
                    break;
                case "refreshdetector":
                    TrainSignals.getSignalManager().getSignals().forEach((signalVector, signalClass) -> strings.add(signalClass.getSignalID()));
                    break;
            }
        }

        Collections.sort(strings);

        List<String> finalCompletions = new ArrayList<>();
        strings.forEach(s -> {
            if(s.startsWith(args[args.length - 1]) && !s.equalsIgnoreCase(args[args.length - 1])) finalCompletions.add(s);
        });
        return finalCompletions;
    }
}
