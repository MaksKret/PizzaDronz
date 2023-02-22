package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Accesses and de-serializes the definition for all No-FLy Zones from the REST-service.
 */
public class NoFlyZonesAccess {

    /**
     * Instance of the NoFlyZonesAccess Singleton.
     */
    private static NoFlyZonesAccess instance;

    /**
     * List of lists of edges of different No-Fly zones.
     */
    private List<List<LngLat>> noFlyZones;

    /**
     * Record of JSON properties. Used to pull data from the REST server into.
     *
     * @param name           Name of the No-Fly-Zone.
     * @param coordinates    Edges of the polygon area of a No-Fly-Zone.
     */
    private record NoFlyZoneProperties(@JsonProperty("name") String name, @JsonProperty("coordinates") double[][] coordinates){}

    /**
     * Accesses the REST-service and deserializes the No-Fly zones into the "noFlyZones" map.
     *
     * @param baseServerURL  String of the Base REST server address, like: "https://website.net/"
     */
    private NoFlyZonesAccess(String baseServerURL) {

        try {
            var serverAddress = new URL(baseServerURL + "noFlyZones");
            NoFlyZoneProperties[] response = new ObjectMapper().readValue(serverAddress, NoFlyZoneProperties[].class);
            noFlyZones = new ArrayList<>();

            for (NoFlyZoneProperties noFlyZoneProperties : response) {

                // number of edges for i'th no fly zone
                int z = noFlyZoneProperties.coordinates.length;
                // list of edge coordinates list for i'th no fly zone
                List<LngLat> lnglatcoors = new ArrayList<>();

                // adding list of edges to a list
                for (int j = 0; j < z; j++) {
                    double lng = noFlyZoneProperties.coordinates[j][0];
                    double lat = noFlyZoneProperties.coordinates[j][1];
                    lnglatcoors.add(new LngLat(lng, lat));
                }

                noFlyZones.add(lnglatcoors);
            }
        } catch(IOException ex){
            // in case a URL is malformed or the server under a given address has not been found it will return empty list
            noFlyZones = new ArrayList<List<LngLat>>();
        }
    }


    ////////////////////////
    ///      GETTERS     ///
    ////////////////////////

    /**
     * Returns the instance of the NoFlyZonesAccess Singleton. In the case that the class has not yet been instantiated, it
     * retrieves the date from the REST-service with the provided base server url.
     *
     * @param baseServerURL  String of the Base REST server address, like: "https://website.net/"
     * @return               Instance of this class.
     */
    public static NoFlyZonesAccess getInstance( String baseServerURL ){
        if (instance == null) {
            instance = new NoFlyZonesAccess(baseServerURL);
        }
        return instance;
    }

    /**
     * Used for retrieving the definitions of all No-Fly-Zones.
     *
     * @return               Returns the noFlyZone map between names of No-Fly-Zones and their edge coordinates.
     */
    public List<List<LngLat>> getNoFlyZones() {
        return noFlyZones;
    }
}