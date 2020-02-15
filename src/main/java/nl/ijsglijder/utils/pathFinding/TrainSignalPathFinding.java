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
import java.util.List;

public class TrainSignalPathFinding {

    public static List<Block> getRoute(Block from, Vector vector, Block to) {
        return getRoute(from, vector, to, 0);
    }

    public static List<Block> getRoute(Block from, Vector vector, Block to, int distancePast) {
        TrackMovingPoint trackMovingPoint = new TrackMovingPoint(from.getLocation(), vector);

        List<Block> blockList = new ArrayList<>();
        boolean foundDest = false;
        trackMovingPoint.next();

        blockList.add(trackMovingPoint.current);
        while(trackMovingPoint.hasNext() && distancePast < 150) {
            trackMovingPoint.next();
            Block current = trackMovingPoint.current;

            blockList.add(current);

            if(new SignalVector(to.getLocation()).equals(new SignalVector(current.getLocation()))) {
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

                        List<Block> blockFound = getRoute(current, trackMovingPoint.currentDirection, to, distancePast);
                        if(blockFound.size() > 0) {
                            foundDest = true;
                            blockList.addAll(blockFound);
                            break;
                        }
                    }
                    rail.setShape(previousShape);
                    current.setBlockData(rail);
                } else {
                    break;
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

    public static List<Block> getRoute(Block from, Vector vector, List<Block> to) {
        TrackMovingPoint trackMovingPoint = new TrackMovingPoint(from.getLocation(), vector);

        List<Block> blockList = new ArrayList<>();
        boolean foundDest = false;
        trackMovingPoint.next();

        blockList.add(trackMovingPoint.current);
        int distancePast = 0;
        while(trackMovingPoint.hasNext() && distancePast < 150) {
            trackMovingPoint.next();
            Block current = trackMovingPoint.current;

            blockList.add(current);

            if(to.contains(current)) {
                to.remove(current);
                if(to.isEmpty()) {
                    break;
                }
            }
            if(isSwitcher(current.getLocation()) && !from.equals(current)) {
                if(current.getBlockData() instanceof Rail) {
                    Rail rail = (Rail) current.getBlockData();
                    Rail.Shape previousShape = rail.getShape();
                    List<Rail.Shape> possible = railsPossibleRoutes(current.getLocation());

                    for(Rail.Shape shape : possible) {
                        rail.setShape(shape);
                        current.setBlockData(rail);

                        List<Block> blockFound = getRoute(current, trackMovingPoint.currentDirection, to, distancePast);
                        blockList.addAll(blockFound);
                    }
                    rail.setShape(previousShape);
                    current.setBlockData(rail);
                }
                break;
            }
            distancePast++;
        }

        if(to.isEmpty()) {
            return blockList;
        }
        return new ArrayList<>();
    }

    public static List<Block> getRoute(Block from, Vector vector, List<Block> to, int distancePast) {
        TrackMovingPoint trackMovingPoint = new TrackMovingPoint(from.getLocation(), vector);

        List<Block> blockList = new ArrayList<>();
        boolean foundDest = false;
        trackMovingPoint.next();

        blockList.add(trackMovingPoint.current);
        while(trackMovingPoint.hasNext() && distancePast < 150) {
            trackMovingPoint.next();
            Block current = trackMovingPoint.current;

            blockList.add(current);

            if(to.contains(current)) {
                to.remove(current);
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

                        List<Block> blockFound = getRoute(current, trackMovingPoint.currentDirection, to, distancePast);
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
            if(railType.getDirection(location3.getBlock()).toString().contains("WEST")) {
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
            if(railType.getDirection(location5.getBlock()).toString().contains("NORTH")) {
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
