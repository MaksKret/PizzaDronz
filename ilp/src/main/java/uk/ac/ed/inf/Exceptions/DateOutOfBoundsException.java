package uk.ac.ed.inf.Exceptions;

/**
 * Thrown in Main to validate that the passed in date falls between two pre-defined dates.
 * @see uk.ac.ed.inf.Main
 */
public class DateOutOfBoundsException extends Exception{
    public DateOutOfBoundsException(String s) {
        super(s);
    }
}

