package uk.ac.ed.inf.Exceptions;

/**
 * Thrown in Order class, when Pizzas on the order do not all come from the same restaurant.
 * @see uk.ac.ed.inf.Order
 */
public class InvalidPizzaCombinationException extends Exception{
    public InvalidPizzaCombinationException(String s) {
        super(s);
    }
}
