package nl.ijsglijder.traincraft.signals;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.detector.DetectorListener;
import com.bergerkiller.bukkit.tc.detector.DetectorRegion;
import nl.ijsglijder.traincraft.TrainSignals;
import nl.ijsglijder.traincraft.files.TrainFile;
import nl.ijsglijder.utils.pathFinding.TrainSignalPathFinding;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class SignalBlock implements DetectorListener {

    SignalClass signal;
    DetectorRegion region;

    public SignalBlock(Block block1, List<SignalVector> block2, SignalClass signalClass) {
        this.signal = signalClass;

        TrainFile file = TrainSignals.getFileManager().getFile("signals.yml");
        FileConfiguration signals = file.getFc();

        if(!signals.contains(signal.getSignalID() + ".detectorRegion") ||  DetectorRegion.getRegion(UUID.fromString(Objects.requireNonNull(signals.getString(signal.getSignalID() + ".detectorRegion")))) == null) {
            Vector vector = new Vector(-1,0,0);

            switch(signalClass.getLookingDirection()) {
                case NORTH:
                    vector = new Vector(0,0,1);
                    break;
                case EAST:
                    vector = new Vector(-1,0,0);
                    break;
                case WEST:
                    vector = new Vector(1,0,0);
                    break;
                case SOUTH:
                    vector = new Vector(0,0,-1);
                    break;
            }

            List<Block> blockCollection = TrainSignalPathFinding.getRoute(block1, vector, block2);

            if(blockCollection.size() <= 1) {
                switch(signalClass.getLookingDirection()) {
                    case NORTH:
                        vector = new Vector(0,0,-1);
                        break;
                    case EAST:
                        vector = new Vector(1,0,0);
                        break;
                    case WEST:
                        vector = new Vector(-1,0,0);
                        break;
                    case SOUTH:
                        vector = new Vector(0,0,1);
                        break;
                }
                blockCollection = TrainSignalPathFinding.getRoute(block1, vector, new ArrayList<>(block2));
                if(blockCollection.size() <= 1) {
                    TrainSignals.getPlugin(TrainSignals.class).getLogger().info("Failed to get the detection region for signal " + signal.getSignalID() + " at " + signal.getVector().toString());
                }
            }

            if(blockCollection.isEmpty()) {
                blockCollection.add(block1);
            }

            this.region = DetectorRegion.create(blockCollection);

            signals.set(signal.getSignalID() + ".detectorRegion", this.region.getUniqueId().toString());
        } else {
            this.region = DetectorRegion.getRegion(UUID.fromString(Objects.requireNonNull(signals.getString(signal.getSignalID() + ".detectorRegion"))));
        }

        region.register(this);
    }

    @Override
    public void onRegister(DetectorRegion detectorRegion) {
        signal.getSignals().forEach(signalVector -> signal.setSignalStatus(SignalClass.SignalStatus.GREEN, signalVector));
    }

    @Override
    public void onUnregister(DetectorRegion detectorRegion) {
    }

    @Override
    public void onLeave(MinecartMember<?> minecartMember) {
    }

    @Override
    public void onEnter(MinecartMember<?> minecartMember) {
    }

    @Override
    public void onLeave(MinecartGroup minecartGroup) {
        signal.onTrainLeave(minecartGroup);
    }

    @Override
    public void onEnter(MinecartGroup minecartGroup) {
        signal.onTrainEnter(minecartGroup);
    }

    @Override
    public void onUnload(MinecartGroup minecartGroup) {

    }

    @Override
    public void onUpdate(MinecartMember<?> minecartMember) {
    }

    @Override
    public void onUpdate(MinecartGroup minecartGroup) {
    }
}
