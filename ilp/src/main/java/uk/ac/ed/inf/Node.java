package uk.ac.ed.inf;

import java.util.List;
import java.util.Map;

/**
 * Class Node represents a Step on a Drone's path. During path calculation represents a possible move for the Drone.
 * @see uk.ac.ed.inf.DroneControl#calculateDronesPath(List, Map, List,List)
 */
public class Node implements Comparable<Node> {

    /**
     * Constant that dictates the minimum allowed distance between two Nodes' coordinates.
     */
    private static final double PROXIMITY_CONSTANT = 0.000075;

    /**
     * LngLat coordinate of a Node on the Drone's path
     */
    private LngLat coordinate;

    /**
     * Weight represents a result of a weight function in calculatePathForOrder. The smaller, the closer the Node is to Restaurant's location.
     */
    private Double weight;

    /**
     * Compass direction of current Node, in relation to the Node that came before in the Path.
     * (Ie, if this Node's coordinate resulted from a calculation of nextPosition of other Node's coordinate in the E direction, then directionFromParent will be assigned E)
     */
    private Compass directionFromParent;

    /**
     * Milliseconds that have passed since the start of Drone's path calculation for the day.
     */
    private long ticksSinceStartOfCalculation;

    /**
     * Represents whether Node is in Central Area.
     */
    private boolean inCentralArea;

    public Node(LngLat coordinate, Double weight, Compass directionFromParent, long ticksSinceStartOfCalculation, boolean inCentralArea){
        this.coordinate = coordinate;
        this.weight = weight;
        this.directionFromParent = directionFromParent;
        this.ticksSinceStartOfCalculation = ticksSinceStartOfCalculation;
        this.inCentralArea = inCentralArea;
    }

    /**
     * Checks if the given coordinate is closer than distance in the PROXIMITY_CONSTANT.
     *
     * @param coordinate      LngLat coordinate.
     * @return                <code>true</code> if the distance between given coordinate and Node's coordinate is smaller than PROXIMITY_CONSTANT.
     */
    public boolean closeToLngLat(LngLat coordinate) {
        return this.coordinate.distanceTo(coordinate) < PROXIMITY_CONSTANT;
    }

    /**
     * Creates a clone of this Node.
     *
     * @return                New Node object with the same values.
     */
    public Node cloneNode(){
        return new Node(coordinate, weight, directionFromParent, ticksSinceStartOfCalculation, inCentralArea);
    }

    /**
     * Overrides the comparison method for Node. Necessary for comparing Nodes in a Min Heap priority queue based on their weight
     * used in calculatePathForOrder.
     *
     * @param otherNode       the object to be compared.
     * @return                <code>  1 </code>  if this Node's weight is larger.
     *                        <code> -1 </code>  if the given Node's weight is larger
     *                        <code>  0 </code>  if both Nodes' weights are equal.
     */
    @Override
    public int compareTo(Node otherNode) {
        return Double.compare(this.weight, otherNode.weight);
    }


    ////////////////////////
    ///      SETTERS     ///
    ////////////////////////

    public void setCoordinate(LngLat coordinate) {
        this.coordinate = coordinate;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public void setDirectionFromParent(Compass directionFromParent) {
        this.directionFromParent = directionFromParent;
    }

    public void setTicksSinceStartOfCalculation(long ticksSinceStartOfCalculation) {
        this.ticksSinceStartOfCalculation = ticksSinceStartOfCalculation;
    }

    public void setInCentralArea(boolean inCentralArea){
        this.inCentralArea = inCentralArea;
    }

    ////////////////////////
    ///      GETTERS     ///
    ////////////////////////

    public LngLat getCoordinate() {
        return coordinate;
    }

    public Double getWeight() {
        return weight;
    }

    public Compass getDirectionFromParent() {
        return directionFromParent;
    }

    public long getTicksSinceStartOfCalculation() {
        return ticksSinceStartOfCalculation;
    }

    public boolean getInCentralArea(){ return this.inCentralArea; }
}
