package uk.ac.ed.inf.Exceptions;

/**
 * Thrown in DroneControl class, when the algorithm runs out of time before being able to calculate the path to a Restaurant.
 * @see uk.ac.ed.inf.DroneControl
 */
public class RestaurantNotAccessibleException extends Exception{
    public RestaurantNotAccessibleException(String s) {
        super(s);
    }
}