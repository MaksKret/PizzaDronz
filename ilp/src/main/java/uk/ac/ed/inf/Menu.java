package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Creates an instance of a Menu item, with the name and price.
 *
 */
public class Menu {

    /**
     * Name of a Menu item.
     */
    @JsonProperty("name")
    private String name;

    /**
     * Price for a Menu item.
     */
    @JsonProperty("priceInPence")
    private int priceInPence;


    ////////////////////////
    ///      GETTERS     ///
    ////////////////////////

    public String getName(){ return this.name; }
    public int getPriceInPence(){ return this.priceInPence; }

}
