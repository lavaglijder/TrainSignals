package nl.ijsglijder.traincraft.signals;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;

public class SignalVector {
    private static HashMap<Integer, SignalVector> signalVectorHashMap;
    World world;
    int x,y,z;

    public SignalVector(World world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public SignalVector(String world, int x, int y, int z) {
        this(Bukkit.getWorld(world), x,y,z);
    }

    public SignalVector(Location location) {
        this(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public SignalVector(String vectorString) {
        String[] splitted = vectorString.split(";");
        if(splitted.length != 4) {
            world = Bukkit.getWorlds().get(0);
            x = 0;
            y = 0;
            z = 0;
        } else {
            world = Bukkit.getWorld(splitted[0]);
            x = Integer.parseInt(splitted[1]);
            y = Integer.parseInt(splitted[2]);
            z = Integer.parseInt(splitted[3]);
        }

    }

    public Location asLocation() {
        return new Location(world,x,y,z);
    }

    public World getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String toString() {
        return world.getName() + ";" + x + ";" + y + ";" + z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignalVector that = (SignalVector) o;
        return x == that.x &&
                y == that.y &&
                z == that.z &&
                world.getUID().equals(that.world.getUID());
    }

    @Override
    public int hashCode() {
        int hash = 17 * 31 + world.getUID().hashCode();
        hash = hash * 31 + x;
        hash = hash * 31 + y;
        hash = hash * 31 + z;
        return hash;
    }
}
