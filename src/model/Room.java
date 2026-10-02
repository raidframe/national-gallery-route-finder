package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Represents one gallery room, including its map position and artworks.
public class Room {
    private final String id;
    private final String name;
    private final double mapX;
    private final double mapY;
    private final List<Artwork> artworks = new ArrayList<>();

    public Room(String id, String name, double mapX, double mapY) {
        this.id = id;
        this.name = name;
        this.mapX = mapX;
        this.mapY = mapY;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getMapX() {
        return mapX;
    }

    public double getMapY() {
        return mapY;
    }

    public void addArtwork(String title, String artist) {
        artworks.add(new Artwork(title, artist, id));
    }

    public List<Artwork> getArtworks() {
        return Collections.unmodifiableList(artworks);
    }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}
