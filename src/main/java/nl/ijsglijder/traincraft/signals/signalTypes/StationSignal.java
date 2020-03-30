package nl.ijsglijder.traincraft.signals.signalTypes;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import nl.ijsglijder.traincraft.TrainSignals;
import nl.ijsglijder.traincraft.signals.LookingDirection;
import nl.ijsglijder.traincraft.signals.SignalClass;
import nl.ijsglijder.traincraft.signals.SignalManager;
import nl.ijsglijder.traincraft.signals.switcher.SwitcherSignal;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class StationSignal extends SignalClass {

    int waitTime;
    List<MinecartGroup> trainsReadyToStart = new ArrayList<>();
    String signalBefore;
    BukkitRunnable bukkitRunnable;

    public StationSignal(Location location, String signalID, LookingDirection direction, Location stationInput, Location speedLimitSetter, Location block1, Location block2, int waitTime, String signalBefore) {
        super(location, signalID, direction, stationInput, speedLimitSetter, block1, block2);

        this.signalBefore = signalBefore;
        this.waitTime = waitTime;
        new BukkitRunnable() {
            @Override
            public void run() {
                setSignalStatus(SignalStatus.RED);
            }
        }.runTaskLater(TrainSignals.getPlugin(TrainSignals.class), 20);
    }

    public void onTrainEnterStation(MinecartGroup minecartGroup) {
        this.bukkitRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isTrainInBlock()) {
                    if(!isOccupiedNext()) {
                        setSignalStatus(SignalStatus.GREEN);
                    } else {
                        setSignalStatus(SignalStatus.YELLOW);
                    }
                }
                trainsReadyToStart.add(minecartGroup);
            }
        };
        this.bukkitRunnable.runTaskLater(TrainSignals.getPlugin(TrainSignals.class), 20 * waitTime);
    }

    public void onTrainLeavesStation(MinecartGroup minecartGroup) {
        setSignalStatus(SignalStatus.RED);
        trainsReadyToStart.remove(minecartGroup);
        this.bukkitRunnable.cancel();
    }

    @Override
    public void onTrainEnter(MinecartGroup minecartGroup) {
        setSignalStatus(SignalStatus.RED);
        trainsReadyToStart.remove(minecartGroup);
        super.addInBlock(minecartGroup);

        SignalManager signalManager = TrainSignals.getSignalManager();
        if(signalManager.getLinkedSignals(getSignalID()) != null) {
            TrainSignals.getSignalManager().getLinkedSignals(getSignalID()).forEach(s -> {
                if(signalManager.getSignal(s) != null)  TrainSignals.getSignalManager().getSignal(s).onNextBlockNotClear();
            });
        }
        if(signalManager.getLinkedStationSignals(getSignalID()) != null) {
            signalManager.getLinkedStationSignals(getSignalID()).forEach(s -> {
                if(signalManager.getSignal(s) != null) {
                    SignalClass signalSelected = signalManager.getSignal(s);
                    if(signalSelected instanceof StationSignal) {
                        StationSignal stationSignal = (StationSignal) signalSelected;
                        stationSignal.onTrainEnterStation(minecartGroup);
                    }
                }
            });
        }
        if(signalManager.getLinkedSwitcherSignals(getSignalID()) != null) {
            signalManager.getLinkedSwitcherSignals(getSignalID()).forEach(s -> {
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

    @Override
    public void onTrainLeave(MinecartGroup minecartGroup) {
        if(trainsReadyToStart.size() > 0) setSignalStatus(isOccupiedNext() ? SignalStatus.YELLOW: SignalStatus.GREEN);
        super.removeInBlock(minecartGroup);

        SignalManager signalManager = TrainSignals.getSignalManager();
        if(signalManager.getLinkedSignals(getSignalID()) != null) {
            signalManager.getLinkedSignals(getSignalID()).forEach(s -> {
                if(signalManager.getSignal(s) != null) signalManager.getSignal(s).onNextBlockClear();
            });
        }
        if(signalManager.getLinkedSwitcherSignals(getSignalID()) != null) {
            signalManager.getLinkedSwitcherSignals(getSignalID()).forEach(s -> {
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

    @Override
    public void onNextBlockRed() {
        this.setOccupiedNext(true);
    }

    @Override
    public void onNextBlockUnRed() {
        this.setOccupiedNext(false);
    }

    @Override
    public void onNextBlockClear() {
    }

    @Override
    public void onNextBlockNotClear() {
    }
}
