package nl.ijsglijder.traincraft.signals;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.signactions.detector.DetectorSignPair;
import nl.ijsglijder.traincraft.TrainCraft;
import nl.ijsglijder.traincraft.files.TrainFile;
import nl.ijsglijder.traincraft.signals.signalTypes.StationSignal;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.awt.event.ItemListener;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class SignalClass {

    private Location location, stationInput, speedLimitSetter, blockSignal1,blockSignal2;
    private LookingDirection lookingDirection;
    private String signalID;
    private ArmorStand as1,as2,as3;
    private SignalBlock signalBlock;
    List<MinecartGroup> inBlock = new ArrayList<>();
    private boolean occupiedNext = false;
    private SignalStatus currentStatus;

    public SignalClass(Location location, String signalID, LookingDirection direction, Location stationInput, Location speedLimitSetter, Location block1, Location block2) {
        this.location = location.clone();
        this.signalID = signalID;
        this.stationInput = stationInput;
        this.speedLimitSetter = speedLimitSetter;
        this.lookingDirection = direction;
        this.blockSignal1 = block1;
        this.blockSignal2 = block2;
        this.currentStatus = SignalStatus.NONE;


        location.getWorld().getBlockAt(location).setType(Material.COBBLESTONE_WALL);
        location.getWorld().getBlockAt(location.add(0,1,0)).setType(Material.BLACK_CONCRETE);
        location.getWorld().getBlockAt(location.add(0,1,0)).setType(Material.BLACK_CONCRETE);
        location.add(0, -2, 0);

        stationInput.getBlock().setType(Material.STONE);


        switch(direction) {
            case NORTH:
                location.add(0.5,0,-0.01);
                break;
            case EAST:
                location.add(1,0,0.5);
                break;
            case SOUTH:
                location.add(0.5,0,1);
                break;
            case WEST:
                location.add(-0.01,0 ,0.5);
                break;
        }

        for (Entity entity : location.getChunk().getEntities()) {
            if(entity.getScoreboardTags().contains("Signal_" + signalID)) entity.remove();
        }

        as3 = location.getWorld().spawn(location.add(0,0.5,0), ArmorStand.class);
        as3.setSilent(true);
        as3.setCollidable(false);
        as3.setInvulnerable(true);
        as3.setBasePlate(false);
        as3.setGravity(false);
        as3.setArms(false);
        as3.setSmall(true);
        as3.setDisabledSlots(EquipmentSlot.CHEST, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.HEAD);
        as3.getEquipment().setHelmet(new ItemStack(Material.GRAY_CONCRETE));
        as3.setLeftArmPose(new EulerAngle(0, 50, 90));
        as3.addScoreboardTag("Signal_"+signalID);
        as3.setVisible(false);

        as2 = location.getWorld().spawn(location.add(0,0.6,0), ArmorStand.class);
        as2.setSilent(true);
        as2.setCollidable(false);
        as2.setInvulnerable(true);
        as2.setBasePlate(false);
        as2.setGravity(false);
        as2.setArms(false);
        as2.setSmall(true);
        as2.setDisabledSlots(EquipmentSlot.CHEST, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.HEAD);
        as2.getEquipment().setHelmet(new ItemStack(Material.GRAY_CONCRETE));
        as2.setLeftArmPose(new EulerAngle(0, 50, 90));
        as2.addScoreboardTag("Signal_"+signalID);
        as2.setVisible(false);

        as1 = location.getWorld().spawn(location.add(0,0.6,0), ArmorStand.class);
        as1.setSilent(true);
        as1.setCollidable(false);
        as1.setInvulnerable(true);
        as1.setBasePlate(false);
        as1.setGravity(false);
        as1.setArms(false);
        as1.setSmall(true);
        as1.setDisabledSlots(EquipmentSlot.CHEST, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.HEAD);
        as1.getEquipment().setHelmet(new ItemStack(Material.GRAY_CONCRETE));
        as1.setLeftArmPose(new EulerAngle(0, 50, 90));
        as1.addScoreboardTag("Signal_"+signalID);
        as1.setVisible(false);

        SignalClass signalClass = this;

        new BukkitRunnable() {
            @Override
            public void run() {
                signalBlock = new SignalBlock(block1.getBlock(), block2.getBlock(), signalClass);
            }
        }.runTask(TrainCraft.getPlugin(TrainCraft.class));

    }

    public LookingDirection getLookingDirection() {
        return lookingDirection;
    }

    public SignalBlock getSignalBlock() {
        return signalBlock;
    }

    public void onTrainEnter(MinecartGroup minecartGroup) {
        inBlock.add(minecartGroup);
        this.setSignalStatus(SignalClass.SignalStatus.RED);

        SignalManager signalManager = TrainCraft.getSignalManager();
        if(signalManager.getLinkedSignals(signalID) != null) {
            TrainCraft.getSignalManager().getLinkedSignals(signalID).forEach(s -> {
                if(signalManager.getSignal(s) != null)  TrainCraft.getSignalManager().getSignal(s).onNextBlockNotClear();
            });
        }
        if(signalManager.getLinkedStationSignals(signalID) != null) {
            signalManager.getLinkedStationSignals(signalID).forEach(s -> {
                if(signalManager.getSignal(s) != null) {
                    SignalClass signalSelected = signalManager.getSignal(s);
                    if(signalSelected instanceof StationSignal) {
                        StationSignal stationSignal = (StationSignal) signalSelected;
                        stationSignal.onTrainEnterStation(minecartGroup);
                    }
                }
            });
        }
    }

    public void onTrainLeave(MinecartGroup minecartGroup) {
        inBlock.remove(minecartGroup);

        if(inBlock.size() == 0) {
            if(!occupiedNext) {
                this.setSignalStatus(SignalClass.SignalStatus.GREEN);
            } else {
                this.setSignalStatus(SignalStatus.YELLOW);
            }
        }

        SignalManager signalManager = TrainCraft.getSignalManager();
        if(signalManager.getLinkedSignals(signalID) != null) {
            signalManager.getLinkedSignals(signalID).forEach(s -> {
                if(signalManager.getSignal(s) != null) signalManager.getSignal(s).onNextBlockClear();
            });
        }
    }

    public void onNextBlockClear() {

    }

    public void onNextBlockNotClear() {

    }

    public void onNextBlockRed() {
        occupiedNext = true;
        if(inBlock.size() == 0) setSignalStatus(SignalStatus.YELLOW);
    }

    public void onNextBlockUnRed() {
        occupiedNext = false;
        if(inBlock.size() == 0) setSignalStatus(SignalStatus.GREEN);
    }

    public void onShut() {

    }

    public void removeDetector() {
        TrainFile file = TrainCraft.getFileManager().getFile("signals.yml");
        FileConfiguration signals = file.getFc();
        signals.set(this.getSignalID() + ".detectorRegion", null);
        this.signalBlock.region.remove();
    }

    public void delete() {
        as3.remove();
        as2.remove();
        as1.remove();
        location.getBlock().setType(Material.AIR);
        location.add(0,1,0).getBlock().setType(Material.AIR);
        location.add(0,1,0).getBlock().setType(Material.AIR);
    }

    public void setSignalStatus(SignalStatus status) {
        SignalManager signalManager = TrainCraft.getSignalManager();
        switch (status) {
            case RED:
                as1.getEquipment().setHelmet(new ItemStack(Material.GRAY_CONCRETE));
                as2.getEquipment().setHelmet(new ItemStack(Material.GRAY_CONCRETE));
                as3.getEquipment().setHelmet(new ItemStack(Material.RED_CONCRETE));
                stationInput.getBlock().setType(Material.TORCH);
                speedLimitSetter.getBlock().setType(Material.TORCH);
                if(!this.currentStatus.equals(SignalStatus.RED)) {
                    if(signalManager.getLinkedSignals(signalID) != null) {
                        signalManager.getLinkedSignals(signalID).forEach(s -> {
                            if(signalManager.getSignal(s) != null) signalManager.getSignal(s).onNextBlockRed();
                        });
                    }
                }
                this.currentStatus = SignalStatus.RED;
                break;
            case GREEN:
                as1.getEquipment().setHelmet(new ItemStack(Material.GREEN_CONCRETE));
                as2.getEquipment().setHelmet(new ItemStack(Material.GRAY_CONCRETE));
                as3.getEquipment().setHelmet(new ItemStack(Material.GRAY_CONCRETE));
                stationInput.getBlock().setType(Material.REDSTONE_TORCH);
                speedLimitSetter.getBlock().setType(Material.TORCH);
                if(this.currentStatus.equals(SignalStatus.RED)) {
                    if(signalManager.getLinkedSignals(signalID) != null) {
                        signalManager.getLinkedSignals(signalID).forEach(s -> {
                            if(signalManager.getSignal(s) != null) signalManager.getSignal(s).onNextBlockUnRed();
                        });
                    }
                }
                this.currentStatus = SignalStatus.GREEN;
                break;
            case YELLOW:
                as1.getEquipment().setHelmet(new ItemStack(Material.GRAY_CONCRETE));
                as2.getEquipment().setHelmet(new ItemStack(Material.YELLOW_CONCRETE));
                as3.getEquipment().setHelmet(new ItemStack(Material.GRAY_CONCRETE));
                stationInput.getBlock().setType(Material.REDSTONE_TORCH);
                speedLimitSetter.getBlock().setType(Material.REDSTONE_TORCH);
                if(this.currentStatus.equals(SignalStatus.RED)) {
                    if(signalManager.getLinkedSignals(signalID) != null) {
                        signalManager.getLinkedSignals(signalID).forEach(s -> {
                            if(signalManager.getSignal(s) != null) signalManager.getSignal(s).onNextBlockUnRed();
                        });
                    }
                }
                this.currentStatus = SignalStatus.YELLOW;
                break;
            case NONE:
                as1.getEquipment().setHelmet(new ItemStack(Material.GRAY_CONCRETE));
                as2.getEquipment().setHelmet(new ItemStack(Material.GRAY_CONCRETE));
                as3.getEquipment().setHelmet(new ItemStack(Material.GRAY_CONCRETE));
                stationInput.getBlock().setType(Material.TORCH);
                speedLimitSetter.getBlock().setType(Material.TORCH);
                if(this.currentStatus.equals(SignalStatus.RED)) {
                    if(signalManager.getLinkedSignals(signalID) != null) {
                        signalManager.getLinkedSignals(signalID).forEach(s -> {
                            if(signalManager.getSignal(s) != null) signalManager.getSignal(s).onNextBlockUnRed();
                        });
                    }
                }
                this.currentStatus = SignalStatus.NONE;
                break;
        }
    }

    public enum SignalStatus {
        RED, GREEN, YELLOW, NONE
    }

    public Location getLocation() {
        return location;
    }

    public Location getStationInput() {
        return stationInput;
    }

    public Location getSpeedLimitSetter() {
        return speedLimitSetter;
    }

    public String getSignalID() {
        return signalID;
    }

    public ArmorStand getAs1() {
        return as1;
    }

    public ArmorStand getAs2() {
        return as2;
    }

    public ArmorStand getAs3() {
        return as3;
    }

    public SignalVector getVector() {
        return new SignalVector(getLocation().getWorld(), getLocation().getBlockX(), getLocation().getBlockY(), getLocation().getBlockZ());
    }

    public List<MinecartGroup> getInBlock() {
        return inBlock;
    }

    public boolean isTrainInBlock() {
        return inBlock.size() > 0;
    }

    public boolean isOccupiedNext() {
        return occupiedNext;
    }

    public void setOccupiedNext(boolean occupiedNext) {
        this.occupiedNext = occupiedNext;
    }

    public void addInBlock(MinecartGroup minecartGroup) {
        inBlock.add(minecartGroup);
    }

    public void removeInBlock(MinecartGroup minecartGroup) {
        inBlock.remove(minecartGroup);
    }

    public void recalculateDetector() {
        this.removeDetector();
        this.signalBlock = new SignalBlock(blockSignal1.getBlock(), blockSignal2.getBlock(), this);
    }

    public void setSignalBlock(SignalBlock signalBlock) {
        this.signalBlock = signalBlock;
    }

    public SignalStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(SignalStatus currentStatus) {
        this.currentStatus = currentStatus;
    }
}
