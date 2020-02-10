package nl.ijsglijder.traincraft.signals;

public enum LookingDirection {

    NORTH,EAST,SOUTH,WEST;

    public static LookingDirection getDirection(double degree) {

        if(degree <= 45 || degree >= 315) {
            return LookingDirection.NORTH;
        }
        if(degree > 45 && degree < 135) {
            return LookingDirection.EAST;
        }
        if(degree > 255 && degree < 315) {
            return LookingDirection.WEST;
        }
        if(degree >= 135 && degree <= 225) {
            return LookingDirection.SOUTH;
        }
        return null;
    }
}
