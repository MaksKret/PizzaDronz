package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a real life Restaurant locations.
 */
public class Restaurant {

    /**
     * Name of the participating restaurant. JSON Property.
     */
    @JsonProperty("name")
    private String name;

    /**
     * An array that holds Menu items, representing all the food currently available
     * to buy from the restaurant. JSON property.
     */
    @JsonProperty("menu")
    private Menu[] menuItems;

    /**
     * Location of the restaurant as a LngLat record, representing the real life location
     * of the restaurant. JSON unwrapped.
     */
    @JsonUnwrapped
    private LngLat location;

    /** Given a URL address, it retrieves data about all currently participating restaurants in the delivery app
     *  from a REST-service, and de-serializes it into a Map between Restaurant's name and a Restaurant object.
     *
     * @param baseServerURL     Address of the REST-service, from which we will retrieve and then de-serialize data.
     * @return                  An array of type Restaurant. All currently participating restaurants within the delivery app.
     */
    public static Map<String, Restaurant> getRestaurantsFromRestServer(String baseServerURL ){

        try {
            var url = new URL(baseServerURL + "restaurants");
            Restaurant[] restaurants = new ObjectMapper().readValue(url, Restaurant[].class);

            var restaurantMap = new HashMap<String, Restaurant>();
            for(Restaurant restaurant: restaurants){
                restaurantMap.put(restaurant.name, restaurant);
            }
            return restaurantMap;
        } catch(IOException e){
            // in case a URL is malformed or the server under a given address has not been found it will return empty map
            return new HashMap<String, Restaurant>();
        }
    }

    ////////////////////////
    ///      GETTERS     ///
    ////////////////////////

    public String getName() {
        return this.name;
    }
    public Menu[] getMenuItems(){
        return menuItems;
    }
    public LngLat getLocation(){
        return location;
    }

}