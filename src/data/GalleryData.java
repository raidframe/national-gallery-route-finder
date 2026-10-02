package data;

import graph.GalleryGraph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

// Loads the gallery graph from simple CSV resource files.
public class GalleryData {
    public static GalleryGraph createGraph() {
        GalleryGraph graph = new GalleryGraph();
        loadRooms(graph);
        loadArtworks(graph);
        loadConnections(graph);
        return graph;
    }

    private static void loadRooms(GalleryGraph graph) {
        readDataFile("/resources/rooms.csv", parts -> {
            String id = parts[0];
            String name = parts[1];
            double x = Double.parseDouble(parts[2]);
            double y = Double.parseDouble(parts[3]);
            graph.addRoom(id, name, x, y);
        });
    }

    private static void loadArtworks(GalleryGraph graph) {
        readDataFile("/resources/artworks.csv", parts ->
                graph.addArtwork(parts[0], parts[1], parts[2])
        );
    }

    private static void loadConnections(GalleryGraph graph) {
        readDataFile("/resources/connections.csv", parts ->
                graph.addConnection(parts[0], parts[1], Integer.parseInt(parts[2]))
        );
    }

    private static void readDataFile(String resourceName, CsvLineReader lineReader) {
        InputStream stream = GalleryData.class.getResourceAsStream(resourceName);
        if (stream == null) {
            throw new IllegalStateException("Missing data file: " + resourceName);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue;
                }
                lineReader.read(line.split(";"));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read " + resourceName, exception);
        }
    }

    private interface CsvLineReader {
        void read(String[] parts);
    }
}
