package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapbox.geojson.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * Class that makes generating different files easier.
 */
public class FileGeneration {

    /**
     * Given a list of Orders writes each Order's specific details into the file.
     *
     * @param orders           List of Orders.
     */
    public static void toJsonFileOrders(List<Order> orders, String date){
        try {
            // create a list of maps
            var listOfOrders = new ArrayList<Map<String, Object>>();

            for(Order order: orders){
                var move = new LinkedHashMap<String, Object>();
                move.put("orderNo", order.getOrderNo());
                move.put("outcome", order.getOrderOutcome());
                move.put("costInPence", order.getPriceTotalInPence());
                listOfOrders.add(move);
            }

            // create object mapper instance
            var mapper = new ObjectMapper();

            // convert map to JSON file
            mapper.writerWithDefaultPrettyPrinter().writeValue(Paths.get("deliveries-" + date + ".json").toFile(), listOfOrders);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Given a map between Order's number and a List of Nodes representing a flight
     * path of the drone for that Order, it writes details about each move between two Nodes into the file.
     *
     * @param orderPathMap      Map between the Order Number and the Flight path (as a List of Nodes) for that Order.
     */
    public static void toJsonFileFlightpath(Map<String, List<Node>> orderPathMap, String date){
        try {
            // create a list of maps
            var listOfMoves = new ArrayList<Map<String, Object>>();

            for(String orderNo: orderPathMap.keySet()) {

                List<Node> orderPath = orderPathMap.get(orderNo);

                for (int i = 0; i < orderPath.size() - 1; i++) {

                    Node node = orderPath.get(i);
                    Node nextNode = orderPath.get(i + 1);
                    var move = new LinkedHashMap<String, Object>();

                    move.put("orderNo", orderNo);
                    move.put("fromLongitude", node.getCoordinate().getLng());
                    move.put("fromLatitude", node.getCoordinate().getLat());
                    // to accommodate for HOVER operation
                    if(nextNode.getDirectionFromParent() == null){
                        move.put("angle", null);
                    } else{
                        move.put("angle", nextNode.getDirectionFromParent().angle);
                    }
                    move.put("toLongitude", nextNode.getCoordinate().getLng());
                    move.put("toLatitude", nextNode.getCoordinate().getLat());
                    move.put("ticksSinceStartOfCalculation", nextNode.getTicksSinceStartOfCalculation());

                    listOfMoves.add(move);
                }
            }

            // create object mapper instance
            var mapper = new ObjectMapper();

            // convert map to JSON file
            mapper.writerWithDefaultPrettyPrinter().writeValue(Paths.get("flightpath-" + date + ".json").toFile(), listOfMoves);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Given a flightpath represented as a List of LngLat coordinates, it prints into the file all of them in the GeoJson format.
     *
     * @param lnglatPoints     List of LngLat coordinates.
     * @param date             Name of the file to be created/written to.
     */
    public static void toGeojsonLine(List<LngLat> lnglatPoints, String date) {

        var points = new ArrayList<Point>();
        for (LngLat lnglatPoint : lnglatPoints) {
            points.add(Point.fromLngLat(lnglatPoint.getLng(), lnglatPoint.getLat()));
        }

        Geometry line = LineString.fromLngLats(points);
        Feature feature = Feature.fromGeometry(line);
        FeatureCollection fColl = FeatureCollection.fromFeature(feature);
        String x = fColl.toJson();

        BufferedWriter bw;
        try {
            // buffered writer will ensure that if file exists it will be overwritten
            bw = new BufferedWriter(new FileWriter("drone-" + date + ".geojson"));
            bw.write(x);
            bw.close();
        } catch (IOException e){
            System.out.println("File cannot be created/opened");
            System.exit(1);
        }
    }
}
