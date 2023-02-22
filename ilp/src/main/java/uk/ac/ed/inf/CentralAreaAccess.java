package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Accesses and de-serializes the definition of the Central Area from the REST-service.
 */
public class CentralAreaAccess {

    /**
     * Instance of the CentralAreaClass Singleton.
     */
    private static CentralAreaAccess instance;

    /**
     * List of LngLat objects representing edges of the Central Area.
     */
    private List<LngLat> edgesOfCentralArea;

    /**
     * Json properties of the Central Area. Used to pull information from the REST-service into.
     *
     * @param name         String type value. Name of the edge of Central Area.
     * @param longitude    double type value. Longitude coordinate of an edge of Central Area.
     * @param latitude     double type value. Latitude coordinate of an edge of Central Area.
     */
    private record CentralAreaProperties(@JsonProperty("name") String name, @JsonProperty("longitude") double longitude, @JsonProperty("latitude") double latitude){}

    /**
     * Retrieves data about the definition of the CentralArea from a REST-service with a provided URL, and de-serializes
     * it into an 2D array of doubles, representing the edges of the Central area.
     *
     * @param url   URL of the REST-service to be accessed.
     */
    private CentralAreaAccess( String url ) {
        try {
            var url1 = new URL(url + "centralArea");
            CentralAreaProperties[] response = new ObjectMapper().readValue(url1, CentralAreaProperties[].class);

            edgesOfCentralArea = new ArrayList<>();
            for (CentralAreaProperties centralAreaProperties : response) {
                edgesOfCentralArea.add(new LngLat(centralAreaProperties.longitude, centralAreaProperties.latitude));
            }
        } catch (IOException e) {
            // in case a URL is malformed or the server under a given address has not been found it will return empty list
            edgesOfCentralArea = new ArrayList<LngLat>();
        }
    }

    ////////////////////////
    ///      GETTERS     ///
    ////////////////////////

    /** Used to retrieve the Singleton class. In case if the Singleton doesn't already exist, it creates and returns
     * a new instance of it, by pulling its definition from a REST-service defined by the URL passed in.
     *
     * @param url   URL object. Address of the REST-service, defining the CentralArea edges.
     * @return      Instance of the singleton class.
     */
    public static CentralAreaAccess getInstance(String url) {
        if (instance == null) {
            instance = new CentralAreaAccess(url);
        }
        return instance;
    }

    /** Returns the definition of the Central Area after de-serialization into a 2D array of type Double.
     *
     * @return      Array of edges of the Central Area, in the {{longitude, latitude}} format.
     */
    public List<LngLat> getEdgesOfCentralArea(){
        return edgesOfCentralArea;
    }
}


