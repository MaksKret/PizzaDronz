package uk.ac.ed.inf.Exceptions;

/**
 * Thrown in Order class when Pizza in the Order does not match any pizzas available from participating Restaurants.
 * @see uk.ac.ed.inf.Order
 */
public class PizzaNotFoundException extends Exception{
    public PizzaNotFoundException(String s) {
        super(s);
    }
}
