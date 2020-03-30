package nl.ijsglijder.traincraft.signals;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import nl.ijsglijder.traincraft.TrainSignals;
import nl.ijsglijder.traincraft.files.TrainFile;
import nl.ijsglijder.traincraft.signals.signalTypes.StationSignal;
import nl.ijsglijder.traincraft.signals.switcher.SwitcherSignal;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class SignalClass {

    private HashMap<SignalVector, Location> stationInput = new HashMap<>(), speedLimitSetter = new HashMap<>();
    private List<SignalVector> signals = new ArrayList<>(), blockSignal2 = new ArrayList<>();
    private Block blockSignal1;
    private LookingDirection lookingDirection;
    private String signalID;
    private HashMap<SignalVector, ArmorStand> as1 = new HashMap<>(),as2 = new HashMap<>(),as3 = new HashMap<>();
    private SignalBlock signalBlock;
    List<MinecartGroup> inBlock = new ArrayList<>();
    private boolean occupiedNext = false;
    private SignalStatus currentStatus;
    private SignalVector signalVector;
    private HashMap<SignalVector, SignalStatus> signalStatusHashMap = new HashMap<>();

    public SignalClass(Location location, String signalID, LookingDirection direction, Location stationInput, Location speedLimitSetter, Location block1, Location block2) {
        this.signalID = signalID;
        this.lookingDirection = direction;
        this.blockSignal1 = block1.getBlock();
        this.blockSignal2.add(new SignalVector(block2));
        this.currentStatus = SignalStatus.NONE;
        this.signalVector = new SignalVector(location);
        this.stationInput.put(signalVector, stationInput);
        this.speedLimitSetter.put(signalVector, speedLimitSetter);

        List<Location> locationList = new ArrayList<>();

        locationList.add(location.clone().add(1,0,0));
        locationList.add(location.clone().add(0,0,1));
        locationList.add(location.clone().add(-1,0,0));
        locationList.add(location.clone().add(0,0,-1));

        locationList.forEach(location1 -> {
            if(!location1.getChunk().equals(location.getChunk())) {
                for (Entity entity : location1.getChunk().getEntities()) {
                    if(entity.getScoreboardTags().contains("Signal_" + signalID)) entity.remove();
                }
            }
        });
        for (Entity entity : location.getChunk().getEntities()) {
            if(entity.getScoreboardTags().contains("Signal_" + signalID)) entity.remove();
        }

        addSignal(location);

        registerSignalBlock();
    }

    public SignalClass(List<Location> locations, String signalID, LookingDirection direction, HashMap<SignalVector, Location> stationInput, HashMap<SignalVector, Location> speedLimitSetter, Location block1, List<Location> block2) {
        this.signalID = signalID;
        this.stationInput = stationInput;
        this.speedLimitSetter = speedLimitSetter;
        this.lookingDirection = direction;
        this.blockSignal1 = block1.getBlock();
        List<Block> blocks2 = new ArrayList<>();
        block2.forEach(location -> blocks2.add(location.getBlock()));
        blocks2.forEach(block -> this.blockSignal2.add(new SignalVector(block.getLocation())));
        this.currentStatus = SignalStatus.NONE;
        Location locationFirst = locations.get(0);
        if(locationFirst != null) this.signalVector = new SignalVector(locationFirst);

        locations.forEach(location1 -> {
            List<Location> locationList = new ArrayList<>();

            locationList.add(location1.clone().add(1,0,0));
            locationList.add(location1.clone().add(0,0,1));
            locationList.add(location1.clone().add(-1,0,0));
            locationList.add(location1.clone().add(0,0,-1));

            locationList.forEach(location -> {
                if(!location.getChunk().equals(location1.getChunk())) {
                    for (Entity entity : location.getChunk().getEntities()) {
                        if(entity.getScoreboardTags().contains("Signal_" + signalID)) entity.remove();
                    }
                }
            });

            for (Entity entity : location1.getChunk().getEntities()) {
                if(entity.getScoreboardTags().contains("Signal_" + signalID)) entity.remove();
            }
        });

        locations.forEach(this::addSignal);

        registerSignalBlock();
    }

    public void registerSignalBlock() {
        Block block1 = this.blockSignal1;
        List<SignalVector> block2Locs = this.blockSignal2;
        SignalClass signalClass = this;
        new BukkitRunnable() {
            @Override
            public void run() {
                signalBlock = new SignalBlock(block1, block2Locs, signalClass);
            }
        }.runTask(TrainSignals.getPlugin(TrainSignals.class));
    }

    public void addSignal(Location location) {
        SignalVector vector = new SignalVector(location);
        signals.add(vector);

        Location loc1 = location.clone();
        signalStatusHashMap.put(vector, SignalStatus.NONE);

        loc1.getWorld().getBlockAt(loc1).setType(Material.COBBLESTONE_WALL);
        loc1.getWorld().getBlockAt(loc1.add(0,1,0)).setType(Material.BLACK_CONCRETE);
        loc1.getWorld().getBlockAt(loc1.add(0,1,0)).setType(Material.BLACK_CONCRETE);

        switch(this.lookingDirection) {
            case NORTH:
                loc1.add(0.5,0,-0.01);
                break;
            case EAST:
                loc1.add(1,0,0.5);
                break;
            case SOUTH:
                loc1.add(0.5,0,1);
                break;
            case WEST:
                loc1.add(-0.01,0 ,0.5);
                break;
        }


        loc1.add(0, -2, 0);

        ArmorStand as3 = loc1.getWorld().spawn(loc1.add(0,0.5,0), ArmorStand.class);
        as3.setSilent(true);
        as3.setCollidable(false);
        as3.setInvulnerable(true);
        as3.setBasePlate(false);
        as3.setGravity(false);
        as3.setArms(false);
        as3.setSmall(true);
        as3.setDisabledSlots(EquipmentSlot.CHEST, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.HEAD);
        Objects.requireNonNull(as3.getEquipment()).setHelmet(new ItemStack(Material.GRAY_CONCRETE));
        as3.setLeftArmPose(new EulerAngle(0, 50, 90));
        as3.addScoreboardTag("Signal_"+signalID);
        as3.setVisible(false);

        ArmorStand as2 = loc1.getWorld().spawn(loc1.add(0,0.6,0), ArmorStand.class);
        as2.setSilent(true);
        as2.setCollidable(false);
        as2.setInvulnerable(true);
        as2.setBasePlate(false);
        as2.setGravity(false);
        as2.setArms(false);
        as2.setSmall(true);
        as2.setDisabledSlots(EquipmentSlot.CHEST, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.HEAD);
        Objects.requireNonNull(as2.getEquipment()).setHelmet(new ItemStack(Material.GRAY_CONCRETE));
        as2.setLeftArmPose(new EulerAngle(0, 50, 90));
        as2.addScoreboardTag("Signal_"+signalID);
        as2.setVisible(false);

        ArmorStand as1 = loc1.getWorld().spawn(loc1.add(0,0.6,0), ArmorStand.class);
        as1.setSilent(true);
        as1.setCollidable(false);
        as1.setInvulnerable(true);
        as1.setBasePlate(false);
        as1.setGravity(false);
        as1.setArms(false);
        as1.setSmall(true);
        as1.setDisabledSlots(EquipmentSlot.CHEST, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.HEAD);
        Objects.requireNonNull(as1.getEquipment()).setHelmet(new ItemStack(Material.GRAY_CONCRETE));
        as1.setLeftArmPose(new EulerAngle(0, 50, 90));
        as1.addScoreboardTag("Signal_"+signalID);
        as1.setVisible(false);

        this.as1.put(vector, as1);
        this.as2.put(vector, as2);
        this.as3.put(vector, as3);
        TrainSignals.getSignalManager().getSignals().put(new SignalVector(location), this);
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

        SignalManager signalManager = TrainSignals.getSignalManager();
        if(signalManager.getLinkedSignals(signalID) != null) {
            TrainSignals.getSignalManager().getLinkedSignals(signalID).forEach(s -> {
                if(signalManager.getSignal(s) != null)  TrainSignals.getSignalManager().getSignal(s).onNextBlockNotClear();
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
        if(signalManager.getLinkedSwitcherSignals(signalID) != null) {
            signalManager.getLinkedSwitcherSignals(signalID).forEach(s -> {
                if(signalManager.getSignal(s) != null) {
                    SignalClass signalSelected = signalManager.getSignal(s);
                    if(signalSelected instanceof SwitcherSignal) {
                        SwitcherSignal switcherSignal = (SwitcherSignal) signalSelected;
                        switcherSignal.trainQueueEnter(this, minecartGroup);
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

        SignalManager signalManager = TrainSignals.getSignalManager();
        if(signalManager.getLinkedSignals(signalID) != null) {
            signalManager.getLinkedSignals(signalID).forEach(s -> {
                if(signalManager.getSignal(s) != null) signalManager.getSignal(s).onNextBlockClear();
            });
        }
        if(signalManager.getLinkedStationSignals(signalID) != null) {
            signalManager.getLinkedStationSignals(signalID).forEach(s -> {
                if(signalManager.getSignal(s) != null) {
                    SignalClass signalSelected = signalManager.getSignal(s);
                    if(signalSelected instanceof StationSignal) {
                        StationSignal stationSignal = (StationSignal) signalSelected;
                        stationSignal.onTrainLeavesStation(minecartGroup);
                    }
                }
            });
        }
        if(signalManager.getLinkedSwitcherSignals(signalID) != null) {
            signalManager.getLinkedSwitcherSignals(signalID).forEach(s -> {
                if(signalManager.getSignal(s) != null) {
                    SignalClass signalSelected = signalManager.getSignal(s);
                    if(signalSelected instanceof SwitcherSignal) {
                        SwitcherSignal switcherSignal = (SwitcherSignal) signalSelected;
                        switcherSignal.trainQueueLeave(this, minecartGroup);
                    }
                }
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
        TrainFile file = TrainSignals.getFileManager().getFile("signals.yml");
        FileConfiguration signals = file.getFc();
        signals.set(this.getSignalID() + ".detectorRegion", null);
        this.signalBlock.region.remove();
    }

    public void delete() {
        as3.forEach((signalVector1, armorStand) -> armorStand.remove());
        as2.forEach((signalVector1, armorStand) -> armorStand.remove());
        as1.forEach((signalVector1, armorStand) -> armorStand.remove());
        signals.forEach(location1 -> {
            location1.asLocation().getBlock().setType(Material.AIR);
            location1.asLocation().add(0,1,0).getBlock().setType(Material.AIR);
            location1.asLocation().add(0,1,0).getBlock().setType(Material.AIR);
        });
    }

    public void setSignalStatus(SignalStatus status) {
        signals.forEach(signalVector1 -> setSignalStatus(status, signalVector1));
    }
    public void setSignalStatus(SignalStatus status, SignalVector vector) {
        SignalManager signalManager = TrainSignals.getSignalManager();
        SignalStatus status1 = signalStatusHashMap.get(vector);
        if(status1 == null) status1 = SignalStatus.NONE;
        switch (status) {
            case RED:
                Objects.requireNonNull(as1.get(vector).getEquipment()).setHelmet(new ItemStack(Material.GRAY_CONCRETE));
                Objects.requireNonNull(as2.get(vector).getEquipment()).setHelmet(new ItemStack(Material.GRAY_CONCRETE));
                Objects.requireNonNull(as3.get(vector).getEquipment()).setHelmet(new ItemStack(Material.RED_CONCRETE));
                this.stationInput.get(vector).getBlock().setType(Material.TORCH);
                if(!status1.equals(SignalStatus.RED)) {
                    if(!this.currentStatus.equals(SignalStatus.RED)) {
                        if(signalManager.getLinkedSignals(signalID) != null) {
                            signalManager.getLinkedSignals(signalID).forEach(s -> {
                                if(signalManager.getSignal(s) != null) signalManager.getSignal(s).onNextBlockRed();
                            });
                        }
                    }
                    this.currentStatus = status;
                    signalStatusHashMap.replace(vector, status);
                }
                break;
            case GREEN:
                Objects.requireNonNull(as1.get(vector).getEquipment()).setHelmet(new ItemStack(Material.GREEN_CONCRETE));
                Objects.requireNonNull(as2.get(vector).getEquipment()).setHelmet(new ItemStack(Material.GRAY_CONCRETE));
                Objects.requireNonNull(as3.get(vector).getEquipment()).setHelmet(new ItemStack(Material.GRAY_CONCRETE));

                this.stationInput.get(vector).getBlock().setType(Material.REDSTONE_TORCH);
                this.speedLimitSetter.get(vector).getBlock().setType(Material.TORCH);

                if(status1.equals(SignalStatus.RED)) {
                    AtomicBoolean conti = new AtomicBoolean(true);

                    signalStatusHashMap.forEach((signalVector1, signalStatus) -> {
                        if(signalVector1 != vector) if(signalStatus == SignalStatus.RED) conti.set(false);
                    });

                    if(conti.get()) {
                        if(this.currentStatus.equals(SignalStatus.RED)) {
                            if(signalManager.getLinkedSignals(signalID) != null) {
                                signalManager.getLinkedSignals(signalID).forEach(s -> {
                                    if(signalManager.getSignal(s) != null) signalManager.getSignal(s).onNextBlockUnRed();
                                });
                            }
                        }
                        this.currentStatus = status;
                    }
                    signalStatusHashMap.replace(vector, status);
                }
                break;
            case YELLOW:
                Objects.requireNonNull(as1.get(vector).getEquipment()).setHelmet(new ItemStack(Material.GRAY_CONCRETE));
                Objects.requireNonNull(as2.get(vector).getEquipment()).setHelmet(new ItemStack(Material.YELLOW_CONCRETE));
                Objects.requireNonNull(as3.get(vector).getEquipment()).setHelmet(new ItemStack(Material.GRAY_CONCRETE));
                this.stationInput.get(vector).getBlock().setType(Material.REDSTONE_TORCH);
                this.speedLimitSetter.get(vector).getBlock().setType(Material.REDSTONE_TORCH);
                if(status1.equals(SignalStatus.RED)) {
                    AtomicBoolean conti = new AtomicBoolean(true);

                    signalStatusHashMap.forEach((signalVector1, signalStatus) -> {
                        if(signalVector1 != vector) if(signalStatus == SignalStatus.RED) conti.set(false);
                    });

                    if(conti.get()) {
                        if(this.currentStatus.equals(SignalStatus.RED)) {
                            if(signalManager.getLinkedSignals(signalID) != null) {
                                signalManager.getLinkedSignals(signalID).forEach(s -> {
                                    if(signalManager.getSignal(s) != null) signalManager.getSignal(s).onNextBlockUnRed();
                                });
                            }
                        }
                        this.currentStatus = status;
                    }
                    signalStatusHashMap.replace(vector, status);
                }
                break;
            case NONE:
                Objects.requireNonNull(as1.get(vector).getEquipment()).setHelmet(new ItemStack(Material.GRAY_CONCRETE));
                Objects.requireNonNull(as2.get(vector).getEquipment()).setHelmet(new ItemStack(Material.GRAY_CONCRETE));
                Objects.requireNonNull(as3.get(vector).getEquipment()).setHelmet(new ItemStack(Material.GRAY_CONCRETE));
                this.stationInput.get(vector).getBlock().setType(Material.TORCH);
                this.speedLimitSetter.get(vector).getBlock().setType(Material.TORCH);
                if(status1.equals(SignalStatus.RED)) {
                    AtomicBoolean conti = new AtomicBoolean(true);

                    signalStatusHashMap.forEach((signalVector1, signalStatus) -> {
                        if(signalVector1 != vector) if(signalStatus == SignalStatus.RED) conti.set(false);
                    });

                    if(conti.get()) {
                        if(this.currentStatus.equals(SignalStatus.RED)) {
                            if(signalManager.getLinkedSignals(signalID) != null) {
                                signalManager.getLinkedSignals(signalID).forEach(s -> {
                                    if(signalManager.getSignal(s) != null) signalManager.getSignal(s).onNextBlockUnRed();
                                });
                            }
                        }
                        this.currentStatus = status;
                    }
                    signalStatusHashMap.replace(vector, status);
                }
                break;
        }
    }

    public enum SignalStatus {
        RED, GREEN, YELLOW, NONE
    }

    public HashMap<SignalVector, Location> getStationInput() {
        return stationInput;
    }

    public HashMap<SignalVector, Location> getSpeedLimitSetter() {
        return speedLimitSetter;
    }

    public String getSignalID() {
        return signalID;
    }

    public HashMap<SignalVector, ArmorStand> getAs1() {
        return as1;
    }

    public HashMap<SignalVector, ArmorStand> getAs2() {
        return as2;
    }

    public HashMap<SignalVector, ArmorStand> getAs3() {
        return as3;
    }

    public SignalVector getVector() {
        return signalVector;
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
        this.signalBlock = new SignalBlock(blockSignal1, blockSignal2, this);
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

    public List<SignalVector> getSignals() {
        return signals;
    }

    public Block getBlockSignal1() {
        return blockSignal1;
    }

    public List<SignalVector> getBlockSignal2() {
        return blockSignal2;
    }

    public SignalVector getSignalVector() {
        return signalVector;
    }
}
