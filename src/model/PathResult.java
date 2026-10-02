package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Stores the result of a route search.
public class PathResult {
    private final List<String> roomIds;
    private final int distance;
    private final String method;

    public PathResult(List<String> roomIds, int distance, String method) {
        this.roomIds = new ArrayList<>(roomIds);
        this.distance = distance;
        this.method = method;
    }

    public List<String> getRoomIds() {
        return Collections.unmodifiableList(roomIds);
    }

    public int getDistance() {
        return distance;
    }

    public String getMethod() {
        return method;
    }

    public boolean isEmpty() {
        return roomIds.isEmpty();
    }

    @Override
    public String toString() {
        if (roomIds.isEmpty()) {
            return "No route found";
        }
        return method + ": " + String.join(" -> ", roomIds) + " (" + distance + ")";
    }
}
