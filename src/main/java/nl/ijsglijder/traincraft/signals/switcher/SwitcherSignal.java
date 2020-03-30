package nl.ijsglijder.traincraft.signals.switcher;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import nl.ijsglijder.traincraft.TrainSignals;
import nl.ijsglijder.traincraft.signals.LookingDirection;
import nl.ijsglijder.traincraft.signals.SignalClass;
import nl.ijsglijder.traincraft.signals.SignalManager;
import nl.ijsglijder.traincraft.signals.SignalVector;
import nl.ijsglijder.traincraft.signals.signalTypes.StationSignal;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SwitcherSignal extends SignalClass {

    List<SignalVector> queueOrder = new ArrayList<>();
    HashMap<SignalVector, MinecartGroup> queue = new HashMap<>();
    HashMap<String, SignalVector> linkedSignals;

    public SwitcherSignal(List<Location> locations, String signalID, LookingDirection direction, HashMap<SignalVector, Location> stationInput, HashMap<SignalVector, Location> speedLimitSetter, Location block1, List<Location> block2,HashMap<String, SignalVector> linkedSignals) {
        super(locations, signalID, direction, stationInput, speedLimitSetter, block1, block2);
        this.linkedSignals = linkedSignals;
    }

    public void trainQueueEnter(SignalClass signalClass, MinecartGroup minecartGroup) {
        if(!linkedSignals.containsKey(signalClass.getSignalID())) return;
        SignalVector linkedTo = linkedSignals.get(signalClass.getSignalID());

        if(queue.isEmpty()) {
            queue.put(linkedTo, minecartGroup);
            queueOrder.add(linkedTo);
            getSignals().forEach(signalVector -> {
                if(!signalVector.equals(linkedTo)) {
                    setSignalStatus(SignalStatus.RED, signalVector);
                }
            });
        } else {
            queue.put(linkedTo, minecartGroup);
            queueOrder.add(linkedTo);
        }
    }
    public void trainQueueLeave(SignalClass signalClass, MinecartGroup minecartGroup) {
        if(!linkedSignals.containsKey(signalClass.getSignalID())) return;
        SignalVector linkedTo = linkedSignals.get(signalClass.getSignalID());
        queue.remove(linkedTo, minecartGroup);
        queueOrder.remove(linkedTo);
    }

    @Override
    public void onTrainEnter(MinecartGroup minecartGroup) {
        setSignalStatus(SignalStatus.RED);
        SignalManager signalManager = TrainSignals.getSignalManager();

        getInBlock().add(minecartGroup);

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

        onNextBlockNotClear();
    }

    @Override
    public void onTrainLeave(MinecartGroup minecartGroup) {
        getInBlock().remove(minecartGroup);
        if(getInBlock().size() <= 0) {
            if(!queue.isEmpty()) {
                SignalVector firstKey = queueOrder.get(0);
                MinecartGroup minecartGroup1 = queue.get(firstKey);

                setSignalStatus(SignalStatus.YELLOW, firstKey);
                getSignals().forEach(signalVector -> {
                    if(!signalVector.equals(firstKey)) setSignalStatus(SignalStatus.RED, signalVector);
                });
            } else {
                setSignalStatus(SignalStatus.YELLOW);
            }
            SignalManager signalManager = TrainSignals.getSignalManager();

            if(signalManager.getLinkedSignals(getSignalID()) != null) {
                TrainSignals.getSignalManager().getLinkedSignals(getSignalID()).forEach(s -> {
                    if(signalManager.getSignal(s) != null)  TrainSignals.getSignalManager().getSignal(s).onNextBlockClear();
                });
            }
            if(signalManager.getLinkedStationSignals(getSignalID()) != null) {
                signalManager.getLinkedStationSignals(getSignalID()).forEach(s -> {
                    if(signalManager.getSignal(s) != null) {
                        SignalClass signalSelected = signalManager.getSignal(s);
                        if(signalSelected instanceof StationSignal) {
                            StationSignal stationSignal = (StationSignal) signalSelected;
                            stationSignal.onTrainLeavesStation(minecartGroup);
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
                            switcherSignal.trainQueueLeave(this, minecartGroup);
                        }
                    }
                });
            }

            onNextBlockClear();
        }
    }

    @Override
    public void setSignalStatus(SignalStatus status) {
        if(status != SignalStatus.NONE && status != SignalStatus.YELLOW &&status != SignalStatus.RED) status = SignalStatus.YELLOW;
        super.setSignalStatus(status);
    }

    @Override
    public void setSignalStatus(SignalStatus status, SignalVector vector) {
        if(status != SignalStatus.NONE && status != SignalStatus.YELLOW &&status != SignalStatus.RED) status = SignalStatus.YELLOW;
        super.setSignalStatus(status, vector);
    }
}
