package uk.ac.ed.inf;

import uk.ac.ed.inf.Exceptions.RestaurantNotAccessibleException;

import java.util.*;

/**
 * Class that Calculates the path Drone is supposed to for a given list of Orders.
 */
public class DroneControl {

    /**
     * Starting Coordinates of the Drone
     */
    private static final LngLat DRONE_START_COORDINATES = new LngLat(-3.186874, 55.944494);

    /**
     * Maximum number of moves a Drone can make before running out of battery.
     */
    private static final int DRONE_MOVES_LIMIT= 2000;

    /**
     * System time at the start of path calculation.
     */
    private static long timeAtStartOfCalculation = 0;

    /**
     * Calculates the path for all Valid Orders in a Given Order List.
     *
     * @param todaysOrders       List of Validated Orders.
     * @param restaurants        List of all available Restaurants.
     * @param centralArea        List of edges of the Central Area, which the drone cannot leave once entered on the way back from the Restaurant.
     * @param noFlyZones         List of lists of edge coordinates defining zones that the Drone cannot enter.
     * @return                   Map between the Order ID number and a Drone's path for that Order.
     */
    public static Map<String, List<Node>> calculateDronesPath(List<Order> todaysOrders, Map<String, Restaurant> restaurants, List<LngLat> centralArea, List<List<LngLat>> noFlyZones) {

        // Arranges the orders by the closest restaurants
        var orderedOrders = new ArrayList<Order>();
        List<String> orderedRestaurants = sortRestaurantsByPathLength(restaurants.values().stream().toList(), centralArea, noFlyZones);

        // Some Orders will have the Restaurant name set to null, but that's okay since they must be invalid if that is the case
        for(String restaurantName: orderedRestaurants) {
            orderedOrders.addAll(todaysOrders.stream().filter(order -> restaurantName.equals(order.getRestaurantName())).toList());
        }

        // LinkedHashMap to maintain the insertion order (necessary as List of orders is ordered by Restaurant name)
        var pathMap = new LinkedHashMap<String, List<Node>>();

        // starts the timer that then gets put into nodes
        int movesUsed = 0;
        timeAtStartOfCalculation = System.currentTimeMillis();

        // Main loop for Path calculation
        for(Order order: orderedOrders){
            if(movesUsed > DRONE_MOVES_LIMIT){
                break;
            }

            if(order.getOrderOutcome() == OrderOutcome.ValidButNotDelivered){

                LngLat restaurantLocation = restaurants.get(order.getRestaurantName()).getLocation();

                try {
                    List<Node> pathTo = calculatePathForOrder(restaurantLocation, centralArea, noFlyZones);
                    // since drone starts at starting point AT, and it always returns the same way, it will always end up exactly back at AT
                    List<Node> pathFrom = backtrackPath(pathTo);

                    // Assembling the Full path for the order
                    var orderPath = new ArrayList<Node>();

                    // HOVER operation add to path calculation
                    orderPath.addAll(pathTo);
                    orderPath.addAll(pathFrom);

                    order.setOrderOutcome(OrderOutcome.Delivered);

                    movesUsed += orderPath.size();
                    pathMap.put(order.getOrderNo(), orderPath);

                } catch (RestaurantNotAccessibleException ignored) {
                    // notice that if a Restaurant was inaccessible it would not have been returned in
                    // the list from "orderRestaurantByPathLength" method, hence it should never be thrown here
                }
            }
        }
        return pathMap;
    }

    /**
     * Calculates the path between DRONE_START_COORDINATES and a given location.
     *
     * @param endCoor        End point for which we need to calculate the path to.
     * @param centralArea    List of edges of the Central Area, which the drone cannot leave once entered on the way back from the Restaurant.
     * @param noFlyZones     List of lists of edge coordinates defining zones that the Drone cannot enter.
     * @return               List of Node objects defining the path between DRONE_START_COORDINATES and a given location.
     * @throws RestaurantNotAccessibleException When the algorithm runs out of time before being able to find a path to a Restaurant.
     */
    private static List<Node> calculatePathForOrder(LngLat endCoor, List<LngLat> centralArea, List<List<LngLat>> noFlyZones) throws RestaurantNotAccessibleException {

        var openList = new PriorityQueue<Node>();

        // Mapping between the Node and edges to other nodes
        var edges = new HashMap<Node, List<Node>>();
        // Mapping from a child Node to its parent
        var childTOparent = new HashMap<Node, Node>();

        var start = new Node(DRONE_START_COORDINATES, DRONE_START_COORDINATES.distanceTo(endCoor), null,  System.currentTimeMillis() - timeAtStartOfCalculation, false);
        var end = new Node(endCoor, null, null, 0, false);

        openList.add(start);

        long timeSinceStartOfThisPathCalculation = System.currentTimeMillis();

        while(!openList.isEmpty()){

            // if the runtime for this calculation is over 10 seconds, deem Restaurant as inaccessible and move on
            if(System.currentTimeMillis() - timeSinceStartOfThisPathCalculation > 10000){
                throw new RestaurantNotAccessibleException("Restaurant not accessible.");
            }

            // n -> current node in the loop
            Node currentPos = openList.peek();

            // Ensures that the
            try{
                if(Objects.requireNonNull(currentPos).getCoordinate().closeTo(end.getCoordinate())){
                    childTOparent.put(end, currentPos);
                    break;
                }
            } catch (NullPointerException e){
                // if there is no more Nodes in the priority queue means the algorithm could not have found the path to the Restaurant
                throw new RestaurantNotAccessibleException("Restaurant not accessible.");
            }

            edges.put(currentPos, new ArrayList<Node>());

            for(Compass direction: Compass.values()){

                LngLat nextCoor = currentPos.getCoordinate().nextPosition(direction);

                boolean nextCoorInCentralArea = nextCoor.inCentralArea(centralArea);

                if (currentPos.equals(start) // starting node does not need to be verified
                        || !nextCoor.sameCoordinates(childTOparent.get(currentPos).getCoordinate())
                        && !LngLat.lineCrossesNoFlyZone(currentPos.getCoordinate(), nextCoor, noFlyZones)
                        && !nextCoor.pointInsideNoFlyZone(noFlyZones)
                        && !coordinateCloseToSomeNode(nextCoor, edges.keySet())
                        && !childCameBackToCentralArea(nextCoorInCentralArea, currentPos.getInCentralArea())){

                    Double weight = 1.6 * nextCoor.distanceTo(endCoor) - nextCoor.distanceTo(DRONE_START_COORDINATES);
                    var nextMove = new Node(nextCoor, weight, direction, System.currentTimeMillis() - timeAtStartOfCalculation, nextCoorInCentralArea);

                    // add Node to the graph, and add undirected edge between it and its parent
                    edges.put(nextMove, new ArrayList<Node>());
                    edges.get(currentPos).add(nextMove);
                    edges.get(nextMove).add(currentPos);

                    // make a mapping between the new node and its parent
                    childTOparent.put(nextMove, currentPos);

                    openList.add(nextMove);
                }
            }

            // this removes the current element n in the current While loop iteration, and prevents it from ever being considered again
            openList.remove(currentPos);
        }

        // end's parent, not to include actual end Node in the final path
        var pathReconstruct = new ArrayList<Node>();
        Node pathRecNode = childTOparent.get(end);

        // only the starting point will have a null parent
        while(childTOparent.get(pathRecNode) != null){
            pathReconstruct.add(pathRecNode);
            pathRecNode = childTOparent.get(pathRecNode);
        }

        // this adds the Drone's Starting point to the path
        pathReconstruct.add(pathRecNode);

        // Added, to reverse path so we have Start -> End instead of End -> Start
        Collections.reverse(pathReconstruct);

        return pathReconstruct;
    }

    /**
     * Orders available Restaurants' names based on how close they are to the DRONE_START_COORDINATES.
     *
     * @param restaurants     List of all available Restaurants.
     * @param centralArea     List of edges of the Central Area, which the drone cannot leave once entered on the way back from the Restaurant.
     * @param noFlyZone       List of lists of Edges defining zones that the Drone cannot enter.
     * @return                Sorted List of Restaurant names. In ascending order, from shortest path to longest.
     */
    private static List<String> sortRestaurantsByPathLength(List<Restaurant> restaurants, List<LngLat> centralArea, List<List<LngLat>> noFlyZone) {

        // Each restaurant is defined by it's path distance
        var orderedRestaurants = new ArrayList<String>();
        var shortestPath = Integer.MAX_VALUE;

        for (Restaurant restaurant : restaurants) {
            String restaurantName = restaurant.getName();
            LngLat restaurantLocation = restaurant.getLocation();

            try {
                List<Node> pathToRestaurant = calculatePathForOrder(restaurantLocation, centralArea, noFlyZone);
                // Approximation of path
                int pathLength = pathToRestaurant.size() * 2 + 1;

                if (pathLength < shortestPath) {
                    orderedRestaurants.add(0, restaurantName);
                    shortestPath = pathToRestaurant.size();
                } else {
                    orderedRestaurants.add(restaurantName);
                }

            } catch (RestaurantNotAccessibleException ignored) {
                // If the Restaurant is inaccessible it simply won't be added to the final list
                // and this Exception will never be thrown again in "calculateDronesPath" method
            }
        }
        return orderedRestaurants;
    }

    /**
     * Given a list of Nodes, reverses that list and returns it as a new List of Nodes.
     *
     * @param pathToBacktrack    List of Nodes.
     * @return                   New Reverses List of Nodes.
     */
    private static List<Node> backtrackPath(List<Node> pathToBacktrack){

        // clones all elements in the passed in List, this is necessary to update Nodes' values
        var reversedPath = new ArrayList<Node>(pathToBacktrack.stream().map(Node::cloneNode).toList());
        Collections.reverse(reversedPath);

        // First Node is the HOVER node over Restaurant
        Node newChild = reversedPath.get(0);
        newChild.setDirectionFromParent(null);
        newChild.setTicksSinceStartOfCalculation(System.currentTimeMillis() - timeAtStartOfCalculation);

        for(int i = 1; i < reversedPath.size()-1; i++){
            newChild = reversedPath.get(i);
            newChild.setDirectionFromParent(Compass.getOpposite(newChild.getDirectionFromParent()));
            newChild.setTicksSinceStartOfCalculation(System.currentTimeMillis() - timeAtStartOfCalculation);
        }

        // We need to set the angle to when moving to Starting point
        Node secondLastChild = reversedPath.get(reversedPath.size()-1);
        for(Compass direction: Compass.values()){
            // if the position of Starting point is in a given direction relative to second last node, then
            if(secondLastChild.getCoordinate().sameCoordinates(newChild.getCoordinate().nextPosition(direction))){
                // this sets the direction to Starting point from second last Node in the path
                secondLastChild.setDirectionFromParent(direction);
                secondLastChild.setTicksSinceStartOfCalculation(System.currentTimeMillis() - timeAtStartOfCalculation);
                break;
            }
        }

        // Last Node added is the HOVER node over Starting position
        Node hoverOverSP = new Node(DRONE_START_COORDINATES,0.0,null, System.currentTimeMillis() - timeAtStartOfCalculation, true);
        reversedPath.add(hoverOverSP);

        return reversedPath;
    }

    /**
     * Given a collection of Nodes, checks that no Node's coordinate is close to a given LngLat coordinate.
     *
     * @param coordinate       LngLat coordinate.
     * @param nodes            Collection of Nodes.
     * @return                 <code>true</code> if some Node in the collection exist with coordinate close to a given coordinate.
     *
     * @see uk.ac.ed.inf.Node#closeToLngLat(LngLat)
     */
    private static boolean coordinateCloseToSomeNode(LngLat coordinate, Collection<Node> nodes){
        for (Node node : nodes){
            if (node.closeToLngLat(coordinate)){
                return true;
            }
        }
        return false;
    }

    /**
     * Checks that Drone's potential path did not come back to Central Area once it has left it.
     *
     * @param childInCentralArea     Boolean value signifying whether Child Node is in Central Area.
     * @param parentInCentralArea    Boolean value signifying whether Parent Node is in Central Area.
     * @return                        <code>true</code> If the Path from Parent to Child came back to Central Area.
     */
    private static boolean childCameBackToCentralArea(boolean childInCentralArea, boolean parentInCentralArea){

        // If the parent is not in Central Area, but the child is, it means that path came back to Central Area which is not intended
        return childInCentralArea && !parentInCentralArea;
    }
}