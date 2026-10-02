package algorithm;

import graph.GalleryGraph;
import model.Artwork;
import model.Edge;
import model.PathResult;
import model.Room;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

// Contains DFS and Dijkstra route-finding algorithms.
public class RouteFinder {
    private final GalleryGraph graph;

    public RouteFinder(GalleryGraph graph) {
        this.graph = graph;
    }

    // Returns the first route found by DFS.
    public PathResult findOneRoute(String start, String end, Set<String> avoid, List<String> waypoints) {
        List<PathResult> routes = findMultipleRoutes(start, end, avoid, waypoints, 1);
        if (routes.isEmpty()) {
            return empty("DFS");
        }
        return routes.get(0);
    }

    // Finds several possible route permutations using DFS.
    public List<PathResult> findMultipleRoutes(String start, String end, Set<String> avoid, List<String> waypoints, int maxRoutes) {
        if (!graph.hasRoom(start) || !graph.hasRoom(end) || avoid.contains(start) || avoid.contains(end)) {
            return Collections.emptyList();
        }

        List<List<String>> routes = new ArrayList<>();
        List<String> stops = buildStops(start, end, waypoints);
        findDfsThroughStops(stops, 0, avoid, maxRoutes, new ArrayList<>(), routes);

        List<PathResult> results = new ArrayList<>();
        for (List<String> route : routes) {
            results.add(new PathResult(route, graph.calculateDistance(route), "DFS"));
        }
        return results;
    }

    // Finds the shortest route by total edge distance.
    public PathResult shortestDijkstra(String start, String end, Set<String> avoid, List<String> waypoints) {
        return routeThroughStops(start, end, avoid, waypoints, false, Collections.emptySet(), "Dijkstra");
    }

    // Finds a route that includes a room with one of the user's favourite artists.
    public PathResult mostInterestingRoute(String start, String end, Set<String> avoid, List<String> waypoints, Set<String> artists) {
        PathResult bestRoute = empty("Interesting Dijkstra");

        for (Room room : graph.getRooms()) {
            if (avoid.contains(room.getId()) || !hasFavouriteArtist(room, artists)) {
                continue;
            }

            List<String> interestingStops = new ArrayList<>(waypoints);
            if (!room.getId().equals(start) && !room.getId().equals(end) && !interestingStops.contains(room.getId())) {
                interestingStops.add(room.getId());
            }

            PathResult route = routeThroughStops(
                    start,
                    end,
                    avoid,
                    interestingStops,
                    false,
                    Collections.emptySet(),
                    "Interesting Dijkstra via room " + room.getId()
            );

            if (!route.isEmpty() && (bestRoute.isEmpty() || route.getDistance() < bestRoute.getDistance())) {
                bestRoute = route;
            }
        }

        if (!bestRoute.isEmpty()) {
            return bestRoute;
        }

        return routeThroughStops(start, end, avoid, waypoints, true, artists, "Interesting Dijkstra");
    }

    // Runs Dijkstra separately between start, waypoints, and destination.
    private PathResult routeThroughStops(
            String start,
            String end,
            Set<String> avoid,
            List<String> waypoints,
            boolean interesting,
            Set<String> artists,
            String method
    ) {
        if (!graph.hasRoom(start) || !graph.hasRoom(end) || avoid.contains(start) || avoid.contains(end)) {
            return empty(method);
        }

        List<String> stops = buildStops(start, end, waypoints);
        List<String> fullRoute = new ArrayList<>();

        for (int i = 0; i < stops.size() - 1; i++) {
            List<String> segment = dijkstra(stops.get(i), stops.get(i + 1), avoid, interesting, artists);
            if (segment.isEmpty()) {
                return empty(method);
            }
            addSegment(fullRoute, segment);
        }

        return new PathResult(fullRoute, graph.calculateDistance(fullRoute), method);
    }

    private void findDfsThroughStops(
            List<String> stops,
            int stopIndex,
            Set<String> avoid,
            int maxRoutes,
            List<String> currentRoute,
            List<List<String>> routes
    ) {
        if (routes.size() >= maxRoutes) {
            return;
        }

        if (stopIndex == stops.size() - 1) {
            routes.add(new ArrayList<>(currentRoute));
            return;
        }

        List<List<String>> segmentRoutes = new ArrayList<>();
        dfs(stops.get(stopIndex), stops.get(stopIndex + 1), avoid, new HashSet<>(), new ArrayList<>(), segmentRoutes, maxRoutes);

        for (List<String> segment : segmentRoutes) {
            int oldSize = currentRoute.size();
            addSegment(currentRoute, segment);
            findDfsThroughStops(stops, stopIndex + 1, avoid, maxRoutes, currentRoute, routes);
            trim(currentRoute, oldSize);
        }
    }

    // Recursive DFS helper used for route permutations.
    private void dfs(
            String current,
            String end,
            Set<String> avoid,
            Set<String> visited,
            List<String> path,
            List<List<String>> routes,
            int maxRoutes
    ) {
        if (routes.size() >= maxRoutes || avoid.contains(current)) {
            return;
        }

        visited.add(current);
        path.add(current);

        if (current.equals(end)) {
            routes.add(new ArrayList<>(path));
        } else {
            for (Edge edge : graph.getEdgesFrom(current)) {
                if (!visited.contains(edge.getTo()) && !avoid.contains(edge.getTo())) {
                    dfs(edge.getTo(), end, avoid, visited, path, routes, maxRoutes);
                }
            }
        }

        path.remove(path.size() - 1);
        visited.remove(current);
    }

    // Standard Dijkstra implementation using a priority queue.
    private List<String> dijkstra(String start, String end, Set<String> avoid, boolean interesting, Set<String> artists) {
        Map<String, Integer> distance = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<NodeCost> queue = new PriorityQueue<>(Comparator.comparingInt(NodeCost::getCost));

        for (Room room : graph.getRooms()) {
            distance.put(room.getId(), Integer.MAX_VALUE);
        }

        distance.put(start, 0);
        queue.add(new NodeCost(start, 0));

        while (!queue.isEmpty()) {
            NodeCost current = queue.poll();
            if (current.getCost() != distance.get(current.getRoomId())) {
                continue;
            }
            if (current.getRoomId().equals(end)) {
                break;
            }

            for (Edge edge : graph.getEdgesFrom(current.getRoomId())) {
                if (avoid.contains(edge.getTo()) && !edge.getTo().equals(end)) {
                    continue;
                }

                int newCost = current.getCost() + routeCost(edge, interesting, artists);
                if (newCost < distance.get(edge.getTo())) {
                    distance.put(edge.getTo(), newCost);
                    previous.put(edge.getTo(), current.getRoomId());
                    queue.add(new NodeCost(edge.getTo(), newCost));
                }
            }
        }

        return rebuildPath(start, end, previous);
    }

    private int routeCost(Edge edge, boolean interesting, Set<String> artists) {
        if (!interesting || artists.isEmpty()) {
            return edge.getDistance();
        }

        Room destination = graph.getRoom(edge.getTo());
        for (Artwork artwork : destination.getArtworks()) {
            if (containsIgnoreCase(artists, artwork.getArtist())) {
                return 1;
            }
        }
        return edge.getDistance() + 60;
    }

    private List<String> rebuildPath(String start, String end, Map<String, String> previous) {
        List<String> path = new ArrayList<>();
        String current = end;

        while (current != null) {
            path.add(current);
            if (current.equals(start)) {
                Collections.reverse(path);
                return path;
            }
            current = previous.get(current);
        }

        return Collections.emptyList();
    }

    private List<String> buildStops(String start, String end, List<String> waypoints) {
        List<String> stops = new ArrayList<>();
        stops.add(start);
        stops.addAll(waypoints);
        stops.add(end);
        return stops;
    }

    private void addSegment(List<String> route, List<String> segment) {
        for (String room : segment) {
            if (route.isEmpty() || !route.get(route.size() - 1).equals(room)) {
                route.add(room);
            }
        }
    }

    private void trim(List<String> list, int size) {
        while (list.size() > size) {
            list.remove(list.size() - 1);
        }
    }

    private boolean containsIgnoreCase(Set<String> values, String target) {
        for (String value : values) {
            if (value.equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasFavouriteArtist(Room room, Set<String> artists) {
        for (Artwork artwork : room.getArtworks()) {
            if (containsIgnoreCase(artists, artwork.getArtist())) {
                return true;
            }
        }
        return false;
    }

    private PathResult empty(String method) {
        return new PathResult(Collections.emptyList(), 0, method);
    }

    private static class NodeCost {
        private final String roomId;
        private final int cost;

        NodeCost(String roomId, int cost) {
            this.roomId = roomId;
            this.cost = cost;
        }

        String getRoomId() {
            return roomId;
        }

        int getCost() {
            return cost;
        }
    }
}
