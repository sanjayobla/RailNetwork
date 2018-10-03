package main.RailNetwork;

import java.util.*;

public class RailNetwork {

    private Map<String, Map<String, Integer>> stationsMap = new HashMap<>();

    //The network is defined as a list of source-destination strings with the distance between them
    //Format: <Station 1> <Station 2> <Distance>, <Station 5> <Station 1> <Distance>,...
    //Ex: A B 5, B C 4, C D 8, D C 8, D E 6, A D 5, C E 2, E B 3, A E 7
    public RailNetwork(String stationsMapString) {
        String[] edgeList = stationsMapString.split(", ");
        for (String edge: edgeList) {
            String[] components = edge.split(" ");
            String source = components[0];
            String destination = components[1];
            Integer distance = Integer.parseInt(components[2]);
            if (!stationsMap.containsKey(source)) {
                Map<String, Integer> sourceMap = new HashMap<>();
                sourceMap.put(destination, distance);
                stationsMap.put(source, sourceMap);
            } else {
                Map<String, Integer> sourceMap = stationsMap.get(source);
                sourceMap.put(destination, distance);
            }
        }
    }

    //Distance queries can either look for the distance as number of hops or route length
    public enum DistanceMetric {
        HOPS,
        ROUTELENGTH
    }

    //Distance queries can specify that the total route distance be equal to,
    //less than or less than or equal to the threshold specified
    public enum DistanceComparator {
        EQUAL,
        LESSTHANOREQUAL,
        LESSTHAN
    }

    static class StationToVisit {
        String stationName;
        int distanceFromSource;

        StationToVisit(String stationName, int distanceFromSource) {
            this.stationName = stationName;
            this.distanceFromSource = distanceFromSource;
        }
    }

    //This method calculates and prints the total distance of a given route in the format <Station1>-<Station2>-<Station3>-...
    //Ex: A-E-B-C-D
    public void printRouteDistance(String route) {

        String[] stations = route.split("-");
        int totalDistance = 0;

        //Split the route string into source-destination pairs for easier computation
        List<String[]> sourceDestinationPairs = new ArrayList<>();
        for (int i = 0; i < stations.length-1; i++) {
            String[] sourceDestinationPair = {stations[i], stations[i+1]};
            sourceDestinationPairs.add(sourceDestinationPair);
        }

        for (String[] sourceDestinationPair: sourceDestinationPairs) {
            String source = sourceDestinationPair[0];
            String destination = sourceDestinationPair[1];
            if (!stationsMap.containsKey(source)) {
                System.out.print("NO SUCH ROUTE");
                return;
            } else {
                Map<String, Integer> sourceStations = stationsMap.get(source);
                if (!sourceStations.containsKey(destination)) {
                    System.out.print("NO SUCH ROUTE");
                    return;
                } else {
                    totalDistance += sourceStations.get(destination);
                }
            }
        }
        System.out.print(totalDistance);
    }

    //This method returns the number of routes between the source and destination
    //that satisfy the threshold condition according to the distance metric specified
    public int numberOfRoutes(String source, String destination, int threshold, DistanceMetric distanceMetric, DistanceComparator distanceComparator) {

        int numRoutes = 0;
        Stack<StationToVisit> stationsToVisit = new Stack<>();

        //If the starting station does not have a track to any other station, return immediately
        if (!stationsMap.containsKey(source)) {
            return 0;
        } else {
            stationsToVisit.push(new StationToVisit(source, 0));

            //Visit every destination of every subsequent station until there are no stations to visit
            while(!stationsToVisit.empty()) {
                StationToVisit currentStation = stationsToVisit.pop();
                Map<String, Integer> destinationsFromCurrentStation = stationsMap.get(currentStation.stationName);
                for (String nextStation : destinationsFromCurrentStation.keySet()) {
                    int distanceToNextStation;
                    if (distanceMetric.equals(DistanceMetric.HOPS)) {
                        distanceToNextStation = currentStation.distanceFromSource + 1;
                    } else {
                        distanceToNextStation = currentStation.distanceFromSource + destinationsFromCurrentStation.get(nextStation);
                    }
                    if (nextStation.equals(destination)) {
                        if (compare(distanceToNextStation, threshold, distanceComparator)) {
                            numRoutes++;
                        }
                    }

                    //There will be stations to visit until the threshold value is reached
                    if (distanceToNextStation <= threshold) {
                        stationsToVisit.push(new StationToVisit(nextStation, distanceToNextStation));
                    }
                }
            }
        }
        return numRoutes;
    }

    public int lengthOfShortestRoute(String source, String destination) throws NoSuchRouteException {
        Stack<StationToVisit> stationsToVisit = new Stack<>();
        Set<String> visited = new HashSet<>();
        int shortestLength = Integer.MAX_VALUE;
        boolean routeFound = false;

        //If the starting station does not have a track to any other station, return immediately
        if (!stationsMap.containsKey(source)) {
            throw new NoSuchRouteException("There is no route between "+source+" and "+destination);
        } else {
            stationsToVisit.push(new StationToVisit(source, 0));

            //Visit every destination of every subsequent station until there are no stations to visit
            while(!stationsToVisit.empty()) {
                StationToVisit currentStation = stationsToVisit.pop();
                //Mark every station you land on as visited
                visited.add(currentStation.stationName);
                Map<String, Integer> destinationsFromCurrentStation = stationsMap.get(currentStation.stationName);
                for (String nextStation : destinationsFromCurrentStation.keySet()) {
                    int distanceToNextStation = currentStation.distanceFromSource + destinationsFromCurrentStation.get(nextStation);
                    if (nextStation.equals(destination)) {
                        if (distanceToNextStation < shortestLength) {
                            routeFound = true;
                            shortestLength = distanceToNextStation;
                        }
                    }
                    //If this station has not already been visited, add it to the stack of stations to visit
                    if (!visited.contains(nextStation)) {
                        stationsToVisit.push(new StationToVisit(nextStation, distanceToNextStation));
                    }
                }
            }
        }
        if (!routeFound) {
            throw new NoSuchRouteException("There is no route between "+source+" and "+destination);
        } else {
            return shortestLength;
        }
    }

    private static boolean compare(int distanceToNextStation, int threshold, DistanceComparator distanceComparator) {
        if (distanceComparator.equals(DistanceComparator.EQUAL)) {
            return distanceToNextStation == threshold;
        } else if (distanceComparator.equals(DistanceComparator.LESSTHANOREQUAL)) {
            return distanceToNextStation <= threshold;
        } else {
            return distanceToNextStation < threshold;
        }
    }

    public static void main(String[] args) {

        String graph = "A B 5, B C 4, C D 8, D C 8, D E 6, A D 5, C E 2, E B 3, A E 7";

        //Initialize the rail network
        RailNetwork kiwilandNetwork = new RailNetwork(graph);

        System.out.println("#1. The distance of the route A-B-C.");
        kiwilandNetwork.printRouteDistance("A-B-C");
        System.out.println("\n#2. The distance of the route A-D.");
        kiwilandNetwork.printRouteDistance("A-D");
        System.out.println("\n#3. The distance of the route A-D-C.");
        kiwilandNetwork.printRouteDistance("A-D-C");
        System.out.println("\n#4. The distance of the route A-E-B-C-D.");
        kiwilandNetwork.printRouteDistance("A-E-B-C-D");
        System.out.println("\n#5. The distance of the route A-E-D.");
        kiwilandNetwork.printRouteDistance("A-E-D");
        System.out.println("\n#6. The number of trips starting at C and ending at C with a maximum of 3 stops.");
        System.out.println(kiwilandNetwork.numberOfRoutes("C", "C", 3, DistanceMetric.HOPS, DistanceComparator.LESSTHANOREQUAL));
        System.out.println("\n#7. The number of trips starting at A and ending at C with exactly 4 stops.");
        System.out.println(kiwilandNetwork.numberOfRoutes("A", "C", 4, DistanceMetric.HOPS, DistanceComparator.EQUAL));
        try {
            System.out.println("\n#8. The length of the shortest route (in terms of distance to travel) from A to C.");
            System.out.println(kiwilandNetwork.lengthOfShortestRoute("A", "C"));

        } catch (NoSuchRouteException e) {
            System.out.println(e.getMessage());
        }
        try {
            System.out.println("\n#9. The length of the shortest route (in terms of distance to travel) from B to B.");
            System.out.println(kiwilandNetwork.lengthOfShortestRoute("B", "B"));
        } catch (NoSuchRouteException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("\n#10. The number of different routes from C to C with a distance of less than 30.");
        System.out.println(kiwilandNetwork.numberOfRoutes("C", "C", 30, DistanceMetric.ROUTELENGTH, DistanceComparator.LESSTHAN));
    }
}
