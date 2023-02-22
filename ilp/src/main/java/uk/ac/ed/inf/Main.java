package uk.ac.ed.inf;

import uk.ac.ed.inf.Exceptions.DateOutOfBoundsException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Main class of the PizzaDronz application.
 */
public class Main {

    /**
     * All orders must be scheduled for at or after this date.
     */
    private static final LocalDate DATE_START = LocalDate.of(2023, 1,1);

    /**
     * All orders must be scheduled for at or before this date.
     */
    private static final LocalDate DATE_END = LocalDate.of(2023,5,31);

    /**
     * Validates that the date is in the correct ISO 8601 format. And that it falls between DATE_START and DATE_END.
     *
     * @param date    String that represents a date.
     * @return        <code>true</code> if the date is in a valid format, and it falls between the two specified dates.
     *
     * @throws DateTimeParseException    Is thrown when the date in the wrong format.
     */
    private static boolean isValidDate(String date) throws DateTimeParseException {

        LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        return (LocalDate.parse(date).isEqual(DATE_START) || LocalDate.parse(date).isAfter(DATE_START)) &&
                (LocalDate.parse(date).isEqual(DATE_END) || LocalDate.parse(date).isBefore(DATE_END));
    }

    /**
     * Main method of the Application.
     * @param args    User input.
     */

    public static void main(String[] args) {

        // Date to complete all the order on, and URL of the Rest Server
        String date = args[0];
        String baseURL = args[1];

        try {
            if (!isValidDate(date)) {
                throw new DateOutOfBoundsException("Date out of bounds");
            }
        } catch (NullPointerException e) {
            System.out.println("Date has not been supplied.");
            System.exit(1);
        } catch (DateTimeParseException e) {
            System.out.println("Date supplied in the wrong format.");
            System.exit(1);
        } catch (DateOutOfBoundsException e) {
            System.out.println("Date out of bounds.");
            System.exit(1);
        }


        // Validation that the provided url string ends with "/", and that it's a correct URL
        try {
            if (!baseURL.endsWith("/")) {
                baseURL = baseURL + "/";
            }
        } catch (NullPointerException e) {
            throw new NullPointerException("No URL string provided.");
        }


        // Accessing the Server for all the necessary data
        CentralAreaAccess centralAreaInstance = CentralAreaAccess.getInstance(baseURL);
        NoFlyZonesAccess noFlyZonesInstance = NoFlyZonesAccess.getInstance(baseURL);
        Map<String, Restaurant> availableRestaurants = Restaurant.getRestaurantsFromRestServer(baseURL);
        List<Order> todaysValidatedOrders = Order.getOrdersByDate(baseURL, date, availableRestaurants.values().stream().toList());


        // Returns a Map between an Order Number and a path for that Order (Only Valid Delivered orders will appear in the Map)
        Map<String, List<Node>> orderPathMap = DroneControl.calculateDronesPath(todaysValidatedOrders, availableRestaurants, centralAreaInstance.getEdgesOfCentralArea(), noFlyZonesInstance.getNoFlyZones());


        // Extracting coordinates of flightpath
        var pathCoordinates = new ArrayList<LngLat>();
        for (List<Node> nodes : orderPathMap.values()) {
            for (Node node : nodes) {
                pathCoordinates.add(node.getCoordinate());
            }
        }

        // Generating the necessary files for the date
        FileGeneration.toJsonFileOrders(todaysValidatedOrders, date);
        FileGeneration.toJsonFileFlightpath(orderPathMap, date);
        FileGeneration.toGeojsonLine(pathCoordinates, date);
    }

}
