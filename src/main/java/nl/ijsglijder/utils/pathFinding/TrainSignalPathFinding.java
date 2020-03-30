package nl.ijsglijder.utils.pathFinding;

import com.bergerkiller.bukkit.tc.cache.RailSignCache;
import com.bergerkiller.bukkit.tc.rails.type.RailType;
import com.bergerkiller.bukkit.tc.utils.TrackMovingPoint;
import nl.ijsglijder.traincraft.signals.SignalVector;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Rail;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrainSignalPathFinding {

    public static List<Block> getRoute(Block from, Vector vector, SignalVector to) {
        return getRoute(from, vector, Collections.singletonList(to), Collections.singletonList(to), 0);
    }

    public static List<Block> getRoute(Block from, Vector vector, SignalVector to, int distancePast) {
        return getRoute(from, vector, Collections.singletonList(to), Collections.singletonList(to), distancePast);
    }

    public static List<Block> getRoute(Block from, Vector vector, List<SignalVector> to) {
        return getRoute(from, vector, to, new ArrayList<>(to), 0);
    }

    public static List<Block> getRoute(Block from, Vector vector, List<SignalVector> to, List<SignalVector> allTo, int distancePast) {
        TrackMovingPoint trackMovingPoint = new TrackMovingPoint(from.getLocation(), vector);

        List<Block> blockList = new ArrayList<>();
        boolean foundDest = false;
        trackMovingPoint.next();

        blockList.add(trackMovingPoint.current);
        while(trackMovingPoint.hasNext() && distancePast < 150 && !to.isEmpty()) {
            trackMovingPoint.next();
            Block current = trackMovingPoint.current;

            blockList.add(current);

            if(to.contains(new SignalVector(current.getLocation()))) {
                to.remove(new SignalVector(current.getLocation()));
                foundDest = true;
                break;
            }
            if(allTo.contains(new SignalVector(current.getLocation()))) {
                foundDest = true;
                break;
            }

            if(isSwitcher(current.getLocation()) && !from.equals(current)) {
                if(current.getBlockData() instanceof Rail) {
                    Rail rail = (Rail) current.getBlockData();
                    Rail.Shape previousShape = rail.getShape();
                    List<Rail.Shape> possible = railsPossibleRoutes(current.getLocation());

                    for(Rail.Shape shape : possible) {
                        rail.setShape(shape);
                        current.setBlockData(rail);

                        List<Block> blockFound = getRoute(current, trackMovingPoint.currentDirection, to, allTo, distancePast);
                        blockList.addAll(blockFound);
                        if(blockList.size() > 0) {
                            foundDest = true;
                        }
                    }
                    rail.setShape(previousShape);
                    current.setBlockData(rail);
                }
                break;
            }
            distancePast++;
        }

        if(foundDest) {
            return blockList;
        }
        return new ArrayList<>();
    }

    public static boolean isSwitcher(Location location) {
        if(RailType.findRailPiece(location) != null && RailType.findRailPiece(location).signs().length > 0) {
            RailSignCache.TrackedSign[] signs = RailType.findRailPiece(location).signs();

            for (RailSignCache.TrackedSign sign : signs) {
                if(sign.sign.getLines()[1].equalsIgnoreCase("switcher")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isRail(Location location) {
        return RailType.findRailPiece(location) != null;
    }

    /*
    Only allows same Y
     */

    public static List<Rail.Shape> railsPossibleRoutes(Location railLoc) {
        if(!railLoc.getBlock().getType().equals(Material.RAIL)) return new ArrayList<>();

        List<Rail.Shape> blockList = new ArrayList<>();

        //Same Y
        Location location2 = railLoc.clone().add(1,0,0);
        Location location3 = railLoc.clone().add(-1,0,0);
        Location location4 = railLoc.clone().add(0,0,1);
        Location location5 = railLoc.clone().add(0,0,-1);

        if(location2.getBlock().getType().equals(Material.RAIL)) {
            RailType railType = RailType.getType(location2.getBlock());
            if(railType.getDirection(location2.getBlock()).toString().contains("EAST")) {
                blockList.add(Rail.Shape.NORTH_EAST);
                blockList.add(Rail.Shape.EAST_WEST);
                blockList.add(Rail.Shape.NORTH_WEST);
            }
        }
        if(location3.getBlock().getType().equals(Material.RAIL)) {
            RailType railType = RailType.getType(location3.getBlock());
            if(railType.getDirection(location3.getBlock()).toString().contains("WEST") || railType.getDirection(location3.getBlock()).toString().equalsIgnoreCase("EAST")) {
                blockList.add(Rail.Shape.SOUTH_EAST);
                blockList.add(Rail.Shape.EAST_WEST);
                blockList.add(Rail.Shape.NORTH_EAST);
            }
        }
        if(location4.getBlock().getType().equals(Material.RAIL)) {
            RailType railType = RailType.getType(location4.getBlock());
            if(railType.getDirection(location4.getBlock()).toString().contains("SOUTH")) {
                blockList.add(Rail.Shape.NORTH_WEST);
                blockList.add(Rail.Shape.NORTH_SOUTH);
                blockList.add(Rail.Shape.NORTH_EAST);
            }
        }
        if(location5.getBlock().getType().equals(Material.RAIL)) {
            RailType railType = RailType.getType(location5.getBlock());
            if(railType.getDirection(location5.getBlock()).toString().contains("NORTH") || railType.getDirection(location5.getBlock()).toString().equalsIgnoreCase("SOUTH")) {
                blockList.add(Rail.Shape.SOUTH_EAST);
                blockList.add(Rail.Shape.NORTH_SOUTH);
                blockList.add(Rail.Shape.SOUTH_WEST);
            }
        }

        List<Rail.Shape> blockFaces = new ArrayList<>(blockList);
        List<Rail.Shape> blockFaces2 = new ArrayList<>();
        blockFaces.forEach(blockFace -> {
            if(blockFaces2.contains(blockFace)) blockList.remove(blockFace);
            blockFaces2.add(blockFace);
        });

        return blockList;
    }

}
