package model;

// Represents a weighted connection between two rooms.
public class Edge {
    private final String from;
    private final String to;
    private final int distance;

    public Edge(String from, String to, int distance) {
        this.from = from;
        this.to = to;
        this.distance = distance;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public int getDistance() {
        return distance;
    }
}
