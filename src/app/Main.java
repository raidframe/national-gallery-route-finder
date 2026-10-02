package app;

import algorithm.RouteFinder;
import data.GalleryData;
import graph.GalleryGraph;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.Artwork;
import model.PathResult;
import model.Room;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Main JavaFX user interface for the route finder.
public class Main extends Application {
    private final GalleryGraph graph = GalleryData.createGraph();
    private final RouteFinder routeFinder = new RouteFinder(graph);

    private ComboBox<Room> startBox;
    private ComboBox<Room> destinationBox;
    private TextField avoidField;
    private TextField waypointField;
    private TextField artistField;
    private Spinner<Integer> maxRoutesSpinner;
    private TextArea routeOutput;
    private ListView<String> artworkList;
    private Pane mapPane;
    private Image mapImage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        loadMapImage();

        // Main layout: controls on the left, map in the center, artworks on the right.
        BorderPane root = new BorderPane();
        root.setLeft(createControls());
        root.setCenter(createMap());
        root.setRight(createArtworkPanel());

        Scene scene = new Scene(root, 1200, 760);
        stage.setTitle("National Gallery Route Finder");
        stage.setScene(scene);
        stage.show();

        drawRoomMarkers();
    }

    private VBox createControls() {
        // Route input fields.
        startBox = new ComboBox<>();
        destinationBox = new ComboBox<>();
        startBox.setItems(FXCollections.observableArrayList(graph.getRooms()));
        destinationBox.setItems(FXCollections.observableArrayList(graph.getRooms()));
        startBox.getSelectionModel().select(graph.getRoom("60"));
        destinationBox.getSelectionModel().select(graph.getRoom("34"));

        avoidField = new TextField();
        avoidField.setPromptText("Example: 9, 11, 12");

        waypointField = new TextField();
        waypointField.setPromptText("Example: 30, 45");

        artistField = new TextField();
        artistField.setPromptText("Example: Turner, Van Gogh");

        maxRoutesSpinner = new Spinner<>(1, 20, 5);
        maxRoutesSpinner.setEditable(true);

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        form.add(new Label("Start"), 0, 0);
        form.add(startBox, 1, 0);
        form.add(new Label("Destination"), 0, 1);
        form.add(destinationBox, 1, 1);
        form.add(new Label("Avoid rooms"), 0, 2);
        form.add(avoidField, 1, 2);
        form.add(new Label("Waypoints"), 0, 3);
        form.add(waypointField, 1, 3);
        form.add(new Label("Artists"), 0, 4);
        form.add(artistField, 1, 4);
        form.add(new Label("Max DFS"), 0, 5);
        form.add(maxRoutesSpinner, 1, 5);

        Button oneDfsButton = new Button("One DFS Route");
        oneDfsButton.setMaxWidth(Double.MAX_VALUE);
        oneDfsButton.setOnAction(event -> showSingleRoute(routeFinder.findOneRoute(
                getStart(), getDestination(), getAvoidRooms(), getWaypoints()
        )));

        Button multipleDfsButton = new Button("Multiple DFS Routes");
        multipleDfsButton.setMaxWidth(Double.MAX_VALUE);
        multipleDfsButton.setOnAction(event -> showMultipleRoutes(routeFinder.findMultipleRoutes(
                getStart(), getDestination(), getAvoidRooms(), getWaypoints(), maxRoutesSpinner.getValue()
        )));

        Button dijkstraButton = new Button("Shortest Route - Dijkstra");
        dijkstraButton.setMaxWidth(Double.MAX_VALUE);
        dijkstraButton.setOnAction(event -> showSingleRoute(routeFinder.shortestDijkstra(
                getStart(), getDestination(), getAvoidRooms(), getWaypoints()
        )));

        Button interestingButton = new Button("Most Interesting Route");
        interestingButton.setMaxWidth(Double.MAX_VALUE);
        interestingButton.setOnAction(event -> showInterestingRoute());

        routeOutput = new TextArea();
        routeOutput.setEditable(false);
        routeOutput.setWrapText(true);
        routeOutput.setPrefRowCount(12);

        VBox buttons = new VBox(8, oneDfsButton, multipleDfsButton, dijkstraButton, interestingButton);

        VBox controls = new VBox(12, title("Route Finder"), form, buttons, title("Results"), routeOutput);
        controls.setPadding(new Insets(12));
        controls.setPrefWidth(340);

        return controls;
    }

    private ScrollPane createMap() {
        ImageView mapView = new ImageView(mapImage);
        mapView.setPreserveRatio(true);

        mapPane = new Pane(mapView);
        mapPane.setPrefSize(mapImage.getWidth(), mapImage.getHeight());
        mapPane.setMinSize(mapImage.getWidth(), mapImage.getHeight());
        mapPane.setMaxSize(mapImage.getWidth(), mapImage.getHeight());

        ScrollPane scrollPane = new ScrollPane(mapPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);
        return scrollPane;
    }

    private VBox createArtworkPanel() {
        artworkList = new ListView<>();
        artworkList.setCellFactory(list -> {
            ListCell<String> cell = new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                    setWrapText(true);
                }
            };
            cell.prefWidthProperty().bind(artworkList.widthProperty().subtract(24));
            return cell;
        });
        VBox.setVgrow(artworkList, Priority.ALWAYS);

        VBox panel = new VBox(10, title("Artworks on Route"), artworkList);
        panel.setPadding(new Insets(12));
        panel.setPrefWidth(390);
        panel.setMinWidth(340);
        return panel;
    }

    private Label title(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 16));
        return label;
    }

    private void showSingleRoute(PathResult result) {
        clearDisplayedPaths();
        artworkList.getItems().clear();

        if (result.isEmpty()) {
            routeOutput.setText("No route found.");
            drawRoomMarkers();
            return;
        }

        routeOutput.setText(result.getMethod() + "\n"
                + String.join(" -> ", result.getRoomIds()) + "\n"
                + "Distance: " + result.getDistance() + "\n"
                + "Rooms visited: " + result.getRoomIds().size());

        drawRoomRoute(result.getRoomIds(), Color.DEEPSKYBLUE);
        showArtworks(result.getRoomIds());
    }

    private void showMultipleRoutes(List<PathResult> results) {
        clearDisplayedPaths();
        artworkList.getItems().clear();

        if (results.isEmpty()) {
            routeOutput.setText("No routes found.");
            drawRoomMarkers();
            return;
        }

        StringBuilder builder = new StringBuilder();
        Color[] colors = {Color.DEEPSKYBLUE, Color.ORANGE, Color.LIMEGREEN, Color.MAGENTA, Color.RED};

        for (int i = 0; i < results.size(); i++) {
            PathResult result = results.get(i);
            builder.append(i + 1)
                    .append(". ")
                    .append(String.join(" -> ", result.getRoomIds()))
                    .append(" (")
                    .append(result.getDistance())
                    .append(")\n\n");
            drawRoomRoute(result.getRoomIds(), colors[i % colors.length]);
        }

        routeOutput.setText(builder.toString());
        showArtworks(results.get(0).getRoomIds());
    }

    private void showInterestingRoute() {
        Set<String> artists = parseSet(artistField.getText());
        if (artists.isEmpty()) {
            clearDisplayedPaths();
            artworkList.getItems().clear();
            drawRoomMarkers();
            routeOutput.setText("Enter at least one favourite artist first.\n"
                    + "Example: Van Gogh, Turner, Monet");
            return;
        }

        showSingleRoute(routeFinder.mostInterestingRoute(
                getStart(), getDestination(), getAvoidRooms(), getWaypoints(), artists
        ));
    }

    private void drawRoomRoute(List<String> roomIds, Color color) {
        drawRoomMarkers();

        // Draw lines between room coordinates on top of the map.
        for (int i = 0; i < roomIds.size() - 1; i++) {
            Room from = graph.getRoom(roomIds.get(i));
            Room to = graph.getRoom(roomIds.get(i + 1));
            Line line = new Line(from.getMapX(), from.getMapY(), to.getMapX(), to.getMapY());
            line.setStroke(color);
            line.setStrokeWidth(4);
            line.setOpacity(0.85);
            line.getStyleClass().add("room-route");
            mapPane.getChildren().add(line);
        }

        for (String roomId : roomIds) {
            Room room = graph.getRoom(roomId);
            Circle circle = new Circle(room.getMapX(), room.getMapY(), 9, Color.YELLOW);
            circle.setStroke(Color.BLACK);
            circle.getStyleClass().add("room-route");
            mapPane.getChildren().add(circle);
        }
    }

    private void drawRoomMarkers() {
        if (mapPane == null) {
            return;
        }
        mapPane.getChildren().removeIf(node -> node.getStyleClass().contains("marker"));
        for (Room room : graph.getRooms()) {
            Circle marker = new Circle(room.getMapX(), room.getMapY(), 4, Color.WHITE);
            marker.setStroke(Color.BLACK);
            marker.setOpacity(0.9);
            marker.getStyleClass().add("marker");

            Label label = new Label(room.getId());
            label.setLayoutX(room.getMapX() + 5);
            label.setLayoutY(room.getMapY() - 9);
            label.setTextFill(Color.BLACK);
            label.setStyle("-fx-font-size: 9px; -fx-background-color: rgba(255,255,255,0.75);");
            label.getStyleClass().add("marker");

            mapPane.getChildren().addAll(marker, label);
        }
    }

    // Removes the old route before drawing a new one.
    private void clearDisplayedPaths() {
        if (mapPane == null) {
            return;
        }
        mapPane.getChildren().removeIf(node ->
                node.getStyleClass().contains("room-route")
                        || node.getStyleClass().contains("marker")
        );
    }

    private void showArtworks(List<String> route) {
        List<String> items = new ArrayList<>();
        for (Artwork artwork : graph.getArtworksOnRoute(route)) {
            items.add(artwork.toString());
        }
        artworkList.setItems(FXCollections.observableArrayList(items));
    }

    private String getStart() {
        return startBox.getValue().getId();
    }

    private String getDestination() {
        return destinationBox.getValue().getId();
    }

    private Set<String> getAvoidRooms() {
        return parseSet(avoidField.getText());
    }

    private List<String> getWaypoints() {
        return new ArrayList<>(parseSet(waypointField.getText()));
    }

    private Set<String> parseSet(String text) {
        // User enters comma-separated room ids or artist names.
        Set<String> values = new LinkedHashSet<>();
        for (String part : text.split(",")) {
            String value = part.trim();
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
        return values;
    }

    private void loadMapImage() {
        InputStream stream = getClass().getResourceAsStream("/resources/floorplan-level-2.png");
        if (stream == null) {
            throw new IllegalStateException("Map image was not found in src/resources.");
        }
        mapImage = new Image(stream);
    }
}
