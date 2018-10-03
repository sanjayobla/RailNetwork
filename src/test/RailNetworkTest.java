package test;

import main.RailNetwork.NoSuchRouteException;
import main.RailNetwork.RailNetwork;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class RailNetworkTest {

    private RailNetwork kiwilandNetwork;

    @Before
    public void setUp() throws Exception {
        String graph = "A B 5, B C 4, C D 8, D C 8, D E 6, A D 5, C E 2, E B 3, A E 7";

        //Initialize the rail network
        kiwilandNetwork = new RailNetwork(graph);
    }

    @Test
    public void testPrintRouteDistance() {
        PrintStream originalOut = System.out;

        List<String> testInputs = new ArrayList<>(Arrays.asList("A-B-C", "A-D", "A-D-C", "A-E-B-C-D", "A-E-D"));
        List<String> testOutputs = new ArrayList<>(Arrays.asList("9", "5", "13", "22", "NO SUCH ROUTE"));

        for (int i = 0; i < testInputs.size(); i++) {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));
            kiwilandNetwork.printRouteDistance(testInputs.get(i));
            String output = new String(outContent.toByteArray());
            Assert.assertEquals(testOutputs.get(i), output);
        }

        System.setOut(originalOut);

    }

    @Test
    public void testNumberOfRoutesRouteExists() {
        int output = kiwilandNetwork.numberOfRoutes("C", "C", 3, RailNetwork.DistanceMetric.HOPS, RailNetwork.DistanceComparator.LESSTHANOREQUAL);
        Assert.assertEquals(2, output);
        output = kiwilandNetwork.numberOfRoutes("A", "C", 4, RailNetwork.DistanceMetric.HOPS, RailNetwork.DistanceComparator.EQUAL);
        Assert.assertEquals(3, output);
        output = kiwilandNetwork.numberOfRoutes("C", "C", 30, RailNetwork.DistanceMetric.ROUTELENGTH, RailNetwork.DistanceComparator.LESSTHAN);
        Assert.assertEquals(7, output);
    }

    @Test
    public void testNumberOfRoutesRouteDoesNotExist() {
        int output = kiwilandNetwork.numberOfRoutes("C", "Z", 30, RailNetwork.DistanceMetric.ROUTELENGTH, RailNetwork.DistanceComparator.LESSTHAN);
        Assert.assertEquals(0, output);
    }

    @Test
    public void testLengthOfShortestRouteRouteExists() {
        try {
            int output = kiwilandNetwork.lengthOfShortestRoute("A", "C");
            Assert.assertEquals(9, output);
            output = kiwilandNetwork.lengthOfShortestRoute("B", "B");
            Assert.assertEquals(9, output);
        } catch (NoSuchRouteException e) {
            Assert.fail("Route exists but got NoSuchRouteException");
        }
    }

    @Test
    public void testLengthOfShortestRouteRouteDoesNotExist() {
        try {
            int output = kiwilandNetwork.lengthOfShortestRoute("A", "Z");
            fail("Did not throw NoSuchRouteException");
        } catch (NoSuchRouteException e) {
            assertEquals("There is no route between A and Z", e.getMessage());
        }
    }
}
