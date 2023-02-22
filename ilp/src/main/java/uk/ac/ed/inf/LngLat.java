package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Represents longitude and latitude co-ordinates that define a precise location
 * of a point on Earth.
 *
 * @param lng  longitude coordinate of a point.
 * @param lat  latitude coordinate of a point.
 */
public record LngLat(@JsonProperty("longitude") double lng, @JsonProperty("latitude") double lat) {

    /**
     * Constant representing the distance of a single move.
     */
    private static final Double MOVE_DISTANCE = 0.00015;

    /**
     * Constant representing move tolerance. If a point is within this constant's distance away from another it's considered to be close.
     */
    private static final Double MOVE_TOLERANCE = 0.00015;

    /**
     * Verifies whether the LngLat object is within The Central Area of a campus,
     * edges of which are obtained from a REST-server supplied in the Main method of the application.
     *
     * @return             <code>true</code> if the coordinates of LngLat record fall withing the Central Area.
     */
    public boolean inCentralArea(List<LngLat> edgesOfCentralArea){
        return inArea(edgesOfCentralArea);
    }

    /**
     * Verifies whether the LngLat object is within The Central Area of a campus,
     * edges of which are obtained from a REST-server supplied in the Main method of the application.
     *
     * @return                <code>true</code> if coordinates the LngLat object fall within a No-Fly Zone.
     */
    public boolean pointInsideNoFlyZone(List<List<LngLat>> noFlyZones) {

        for (List<LngLat> noFlyZone : noFlyZones){
            if (inArea(noFlyZone)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether line between two given LngLat points crosses any of the No-Fly zones' edges.
     *
     * @param pointOne      First point that defines a line.
     * @param pointTwo      Second point that defines a line.
     * @param noFlyZones    List of Lists of Edges defining No-Fly zones.
     * @return              <code>true</code> if the line defined by the two points crosses any of the No-Fly zones' edges.
     */
    public static boolean lineCrossesNoFlyZone(LngLat pointOne, LngLat pointTwo, List<List<LngLat>> noFlyZones){

        for(List<LngLat> noFlyZone: noFlyZones){

            int len = noFlyZone.size();
            for(int i = 0; i < len; i++){
                if (lineIntersection(noFlyZone.get(i), noFlyZone.get((i + 1) % len), pointOne, pointTwo)){
                    return true;
                }
            }
        }
        return false;
    }

    /** Measures the pythagorean distance between a provided coordinate and itself.
     *
     * @param coordinate   LngLat record. A coordinate to measure the distance between.
     * @return             Double value type. Pythagorean distance between itself and the supplied coordinate.
     */
    public double distanceTo( LngLat coordinate ){
        return Math.sqrt(Math.pow(this.lng - coordinate.lng(),2) + Math.pow(this.lat - coordinate.lat(),2));
    }

    /** Verifies that the distance between itself and the supplied coordinate is strictly less than the MOVE_TOLERANCE.
     *
     * @param coordinate   LngLat record.
     * @return             <code>true</code> if the distance between itself and the supplied coordinate is
     *                     strictly less than 0.00015.
     */
    public boolean closeTo( LngLat coordinate ) {
        return this.distanceTo(coordinate) < MOVE_TOLERANCE;
    }

    /** Calculates the next position of the drone based on the compass direction provided.
     *
     * @param direction    Enum type object. Compass direction.
     * @return             returns a LngLat record that represents the next position of a drone if it makes a
     *                     move in the specified compass direction.
     */
    public LngLat nextPosition (Compass direction){

        if (direction == null) {
            return new LngLat(this.lng,this.lat);
        }
        return new LngLat(
                this.lng + MOVE_DISTANCE * Math.cos(Math.toRadians(direction.angle)),
                this.lat + MOVE_DISTANCE * Math.sin(Math.toRadians(direction.angle)));
    }

    /**
     * Given a LngLat object it checks whether its Longitude and Latitude values equal to this LngLat object's Lng Lat values.
     *
     * @param coordinate   LngLat object. Coordinates of a point.
     * @return             <code>true</code> if Lng Lat values between itself and a given object are equal.
     */
    public boolean sameCoordinates (LngLat coordinate){
        return this.lng == coordinate.lng && this.lat == coordinate.lat;
    }

    /**
     * Check if the LngLat coordinate falls within a polygon Area defined by a given List of edges.
     *
     * @param edgesOfArea  List of LngLat coordinates defining a polygon Area.
     * @return             true when the point is within the polygon area.
     */
    private boolean inArea(List<LngLat> edgesOfArea){

        boolean inside = false;
        int len = edgesOfArea.size();
        // counts how many times point passes through some line in the polygon
        for (int i = 0; i < len; i++) {
            if (pointIntersectsEdge(edgesOfArea.get(i), edgesOfArea.get((i + 1) % len), this.lng, this.lat))
                inside = !inside;
        }
        return inside;
    }

    /**
     * Helper function for inArea. Check whether a point defined by "currentXPos" and "currentYPos"
     * crosses a line between "pointOne" and "pointTwo".
     *
     * @param pointOne         First point defining an edge of an Area.
     * @param pointTwo         Second point defining an edge of an Area.
     * @param currentXPos      Longitude coordinate of a point.
     * @param currentYPos      Latitude coordinate of a point.
     *
     * @return                 <code>true</code> if a line projected from a point to infinity crosses the edge.
     */
    private static boolean pointIntersectsEdge(LngLat pointOne, LngLat pointTwo, double currentXPos, double currentYPos){

        // have to ensure that the first parameter holds point with the lower y value
        if (pointOne.lat > pointTwo.lat)
            return pointIntersectsEdge(pointTwo, pointOne, currentXPos, currentYPos);

        // move the current point up a little when its y value equals either of the other y values
        if (currentYPos == pointOne.lat || currentYPos == pointTwo.lat)
            currentYPos += 0.0000015;

        // if the current point above, below or in front of the line
        if (currentYPos > pointTwo.lat || currentYPos < pointOne.lat || currentXPos >= max(pointOne.lng, pointTwo.lng))
            return false;

        // checks if the current point is placed in front of the line
        if (currentXPos < min(pointOne.lng, pointTwo.lng))
            return true;

        // if the gradient between the current point and pointOne is greater than the gradient of the line, then
        // the current point is in front of the line
        double mFromOneToCurrent = (currentYPos - pointOne.lat) / (currentXPos - pointOne.lng);
        double mFromOneToTwo = (pointTwo.lat - pointOne.lat) / (pointTwo.lng - pointOne.lng);
        return mFromOneToCurrent >= mFromOneToTwo;

    }

    /**
     * Helper function for "lineCrossesNoFlyZone". Checks whether a path between two points intersects with an
     * Edge (defined by two points) of a No-Fly zone.
     *
     * https://en.wikipedia.org/wiki/Line–line_intersection
     *
     * @param one       Point defining the first point of an edge of a No-Fly Zone.
     * @param two       Point defining the second point of an edge of a No-Fly Zone.
     * @param three     Point defining the first point of a path between two Nodes.
     * @param four      Point defining the second point of a path between two Nodes.
     *
     * @return          <code>true</code> if the lines intersect.
     */
    public static boolean lineIntersection(LngLat one, LngLat two, LngLat three, LngLat four){

        // Line 1 => one and two
        // Line 2 => three and four

        double t = ((one.lng - three.lng)*(three.lat - four.lat) - (one.lat - three.lat)*(three.lng - four.lng))
                   /
                   ((one.lng - two.lng)*(three.lat - four.lat) - (one.lat - two.lat)*(three.lng - four.lng));

        double u = ((one.lng - three.lng)*(one.lat - two.lat) - (one.lat - three.lat)*(one.lng - two.lng))
                   /
                   ((one.lng - two.lng)*(three.lat - four.lat) - (one.lat - two.lat)*(three.lng - four.lng));

        return (0 <= t) && (t <= 1) && (0 <= u) && (u <= 1);
    }


    ////////////////////////
    ///      GETTERS     ///
    ////////////////////////

    public double getLng(){
        return lng;
    }
    public double getLat(){
        return lat;
    }

}
