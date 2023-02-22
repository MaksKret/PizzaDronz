package uk.ac.ed.inf.Exceptions;


/**
 * Thrown in Order class, when the order contains no items.
 * @see uk.ac.ed.inf.Order
 */
public class NoItemsInOrderException extends Exception{
    public NoItemsInOrderException(String s) { super(s); }
}
