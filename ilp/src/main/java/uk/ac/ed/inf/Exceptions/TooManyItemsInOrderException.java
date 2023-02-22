package uk.ac.ed.inf.Exceptions;

/**
 * Thrown in Order class, when the order contains more than 4 items.
 * @see uk.ac.ed.inf.Order
 */
public class TooManyItemsInOrderException extends Exception{
    public TooManyItemsInOrderException(String s) { super(s); }
}

