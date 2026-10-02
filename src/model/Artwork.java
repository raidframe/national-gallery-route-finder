package model;

// Stores one artwork and the room where it is displayed.
public class Artwork {
    private final String title;
    private final String artist;
    private final String roomId;

    public Artwork(String title, String artist, String roomId) {
        this.title = title;
        this.artist = artist;
        this.roomId = roomId;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getRoomId() {
        return roomId;
    }

    @Override
    public String toString() {
        return title + " - " + artist + " (Room " + roomId + ")";
    }
}
