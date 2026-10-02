package graph;

import model.Artwork;
import model.Edge;
import model.Room;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Custom graph structure: rooms are vertices and connections are weighted edges.
public class GalleryGraph {
    private final Map<String, Room> rooms = new LinkedHashMap<>();
    private final Map<String, List<Edge>> connections = new HashMap<>();

    public void addRoom(String id, String name, double mapX, double mapY) {
        rooms.put(id, new Room(id, name, mapX, mapY));
        connections.put(id, new ArrayList<>());
    }

    // Adds an artwork to an existing room.
    public void addArtwork(String roomId, String title, String artist) {
        Room room = rooms.get(roomId);
        if (room != null) {
            room.addArtwork(title, artist);
        }
    }

    public void addConnection(String roomA, String roomB, int distance) {
        connections.get(roomA).add(new Edge(roomA, roomB, distance));
        connections.get(roomB).add(new Edge(roomB, roomA, distance));
    }

    public boolean hasRoom(String id) {
        return rooms.containsKey(id);
    }

    public Room getRoom(String id) {
        return rooms.get(id);
    }

    public Collection<Room> getRooms() {
        return Collections.unmodifiableCollection(rooms.values());
    }

    public List<Edge> getEdgesFrom(String roomId) {
        return connections.getOrDefault(roomId, Collections.emptyList());
    }

    public int getDistance(String from, String to) {
        for (Edge edge : getEdgesFrom(from)) {
            if (edge.getTo().equals(to)) {
                return edge.getDistance();
            }
        }
        return 0;
    }

    public int calculateDistance(List<String> route) {
        int total = 0;
        for (int i = 0; i < route.size() - 1; i++) {
            total += getDistance(route.get(i), route.get(i + 1));
        }
        return total;
    }

    public List<Artwork> getArtworksOnRoute(List<String> route) {
        List<Artwork> artworks = new ArrayList<>();
        for (String roomId : route) {
            Room room = rooms.get(roomId);
            if (room != null) {
                artworks.addAll(room.getArtworks());
            }
        }
        return artworks;
    }
}
