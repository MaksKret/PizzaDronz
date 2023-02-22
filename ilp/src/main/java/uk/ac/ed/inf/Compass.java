package uk.ac.ed.inf;

/**
 * Enum representing Compass's 16 major axis, and their respective angles (represented in degrees).
 */
public enum Compass {
    E   (0),
    ENE (22.5),
    NE  (45),
    NNE (67.5),
    N   (90),
    NNW (112.5),
    NW  (135),
    WNW (157.5),
    W   (180),
    WSW (202.5),
    SW  (225),
    SSW (247.5),
    S   (270),
    SSE (292.5),
    SE  (315),
    ESE (337.5);

    /**
     * double type value angle. Represents compass direction's angle.
     */
    public final double angle;

    /**
     * Constructor for the Enum type. Necessary to add angles to corresponding compass directions.
     *
     * @param angle        Angle of the corresponding Compass direction.
     */
    Compass(double angle) {
        this.angle = angle;
    }

    /**
     * Given a compass direction of Compass Enum type, it returns a direction directly opposite to it (180 degrees more).
     *
     * @param direction    Compass Enum type. One of the compass' 16 major axis.
     * @return             Compass Enum type. Returns an opposite direction to the one provided.
     */
    public static Compass getOpposite(Compass direction){

        if (direction == null){
            return null;
        }
        switch(direction){
            case E -> { return W; }
            case ENE -> { return WSW; }
            case NE -> { return SW; }
            case NNE -> { return SSW; }
            case N -> { return S; }
            case NNW -> { return SSE; }
            case NW -> { return SE; }
            case WNW -> { return ESE; }
            case W -> { return E; }
            case WSW -> { return ENE;}
            case SW -> { return NE; }
            case SSW -> { return NNE; }
            case S -> { return N; }
            case SSE -> { return NNW; }
            case SE -> { return NW; }
            case ESE -> { return WNW; }
        }
        return null;
    }
}
