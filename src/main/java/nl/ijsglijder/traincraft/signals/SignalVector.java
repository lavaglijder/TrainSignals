package nl.ijsglijder.traincraft.signals;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public class SignalVector {
    World world;
    int x,y,z;

    public SignalVector(World world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public SignalVector(Location location) {
        this.world = location.getWorld();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
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
                world.equals(that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z);
    }
}
