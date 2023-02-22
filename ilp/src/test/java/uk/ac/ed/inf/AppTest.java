package uk.ac.ed.inf;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.ed.inf.Exceptions.InvalidPizzaCombinationException;
import uk.ac.ed.inf.Exceptions.NoItemsInOrderException;
import uk.ac.ed.inf.Exceptions.RestaurantNotAccessibleException;
import uk.ac.ed.inf.Exceptions.TooManyItemsInOrderException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AppTest extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    public void testTest(){
        assertTrue(true);
    }
}
