package tools;

import algorithm.RouteFinder;
import data.GalleryData;
import graph.GalleryGraph;

import java.util.Collections;
import java.util.Set;

// Simple timing helper for comparing the main algorithms.
public class SimpleBenchmark {
    public static void main(String[] args) {
        GalleryGraph graph = GalleryData.createGraph();
        RouteFinder finder = new RouteFinder(graph);

        int repeats = 10_000;
        long start = System.nanoTime();
        for (int i = 0; i < repeats; i++) {
            finder.shortestDijkstra("60", "34", Collections.emptySet(), Collections.emptyList());
        }
        long dijkstraTime = System.nanoTime() - start;

        start = System.nanoTime();
        for (int i = 0; i < repeats; i++) {
            finder.mostInterestingRoute("60", "45", Collections.emptySet(), Collections.emptyList(), Set.of("Van Gogh"));
        }
        long interestingTime = System.nanoTime() - start;

        System.out.println("Simple benchmark, " + repeats + " repeats");
        System.out.println("Dijkstra average: " + dijkstraTime / repeats + " ns");
        System.out.println("Interesting route average: " + interestingTime / repeats + " ns");
    }
}
