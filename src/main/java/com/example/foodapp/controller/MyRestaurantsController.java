package com.example.foodapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import java.util.*;
import java.util.Base64;
import java.io.ByteArrayInputStream;
import javafx.scene.layout.HBox;
import java.util.function.Consumer;

public class MyRestaurantsController {
    @FXML private ListView<RestaurantItem> restaurantList;
    @FXML private Button backBtn;
    @FXML private Label messageLabel;

    private String jwtToken;
    private Runnable onBack;
    private ObservableList<RestaurantItem> restaurants = FXCollections.observableArrayList();
    private Consumer<RestaurantItem> onManageMenus;
    private Consumer<RestaurantItem> onEditRestaurant;
    
    public void setOnManageMenus(Consumer<RestaurantItem> c) { this.onManageMenus = c; }
    public void setOnEditRestaurant(Consumer<RestaurantItem> c) { this.onEditRestaurant = c; }

    public void setJwtToken(String token) { this.jwtToken = token; }
    public void setOnBack(Runnable r) { this.onBack = r; }

    @FXML
    public void initialize() {
        restaurantList.setItems(restaurants);
        restaurantList.setCellFactory(list -> new RestaurantCell());
        restaurantList.setOnMouseClicked(ev -> {
            if (ev.getClickCount() == 2) {
                RestaurantItem item = restaurantList.getSelectionModel().getSelectedItem();
                if (item != null && onManageMenus != null) onManageMenus.accept(item);
            }
        });
        backBtn.setOnAction(e -> { if (onBack != null) onBack.run(); });
        fetchMyRestaurants();
    }

    private void fetchMyRestaurants() {
        // Save selection and scroll position
        RestaurantItem selected = restaurantList.getSelectionModel().getSelectedItem();
        Integer selectedId = selected != null ? selected.id : null;
        int scrollIndex = restaurantList.getSelectionModel().getSelectedIndex();

        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL("http://localhost:8000/restaurants/mine");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
                int code = conn.getResponseCode();
                if (code != 200) throw new Exception("Failed to fetch your restaurants: " + code);
                java.util.Scanner sc = new java.util.Scanner(conn.getInputStream(), "UTF-8");
                String json = sc.useDelimiter("\\A").next();
                sc.close();
                List<RestaurantItem> items = parseRestaurants(json);
                Platform.runLater(() -> {
                    restaurants.setAll(items);
                    // Restore selection and scroll
                    if (selectedId != null) {
                        for (int i = 0; i < items.size(); i++) {
                            if (items.get(i).id == selectedId) {
                                restaurantList.getSelectionModel().select(i);
                                restaurantList.scrollTo(i);
                                break;
                            }
                        }
                    } else if (scrollIndex >= 0 && scrollIndex < items.size()) {
                        restaurantList.scrollTo(scrollIndex);
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    restaurants.clear();
                    messageLabel.setText("Error: " + ex.getMessage());
                });
            }
        }).start();
    }

    private List<RestaurantItem> parseRestaurants(String json) {
        List<RestaurantItem> list = new ArrayList<>();
        int idx = 0;
        while ((idx = json.indexOf("\"id\":", idx)) != -1) {
            int id = Integer.parseInt(json.substring(idx + 5, json.indexOf(',', idx + 5)).replaceAll("[^0-9]", ""));
            int nameIdx = json.indexOf("\"name\":", idx);
            int nameStart = json.indexOf('"', nameIdx + 7) + 1;
            int nameEnd = json.indexOf('"', nameStart);
            String name = json.substring(nameStart, nameEnd);
            int logoIdx = json.indexOf("\"logo_base64\":", idx);
            String logo = null;
            if (logoIdx != -1) {
                int logoStart = json.indexOf('"', logoIdx + 14) + 1;
                int logoEnd = json.indexOf('"', logoStart);
                logo = json.substring(logoStart, logoEnd);
            }
            list.add(new RestaurantItem(id, name, logo));
            idx = nameEnd;
        }
        return list;
    }

    public static class RestaurantItem {
        public final int id;
        public final String name;
        public final String logoBase64;
        public RestaurantItem(int id, String name, String logoBase64) {
            this.id = id; this.name = name; this.logoBase64 = logoBase64;
        }
    }

    public class RestaurantCell extends ListCell<RestaurantItem> {
        private final HBox content = new HBox(16);
        private final ImageView imageView = new ImageView();
        private final Label nameLabel = new Label();
        private final Button editBtn = new Button("Edit");
        private final Button deleteBtn = new Button("Delete");
        public RestaurantCell() {
            imageView.setFitHeight(80);
            imageView.setFitWidth(80);
            imageView.setPreserveRatio(true);
            nameLabel.setMaxWidth(Double.MAX_VALUE);
            content.setMaxWidth(Double.MAX_VALUE);
            editBtn.setMaxWidth(Double.MAX_VALUE);
            deleteBtn.setMaxWidth(Double.MAX_VALUE);
            content.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 8 0 8 0;");
            content.getChildren().addAll(imageView, nameLabel, editBtn, deleteBtn);
            editBtn.setOnAction(e -> handleEdit());
            deleteBtn.setOnAction(e -> handleDelete());
        }
        @Override
        protected void updateItem(RestaurantItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                nameLabel.setText(item.name);
                if (item.logoBase64 != null && item.logoBase64.length() > 20 && !item.logoBase64.startsWith("[")) {
                    try {
                        byte[] imgBytes = java.util.Base64.getDecoder().decode(item.logoBase64);
                        imageView.setImage(new Image(new java.io.ByteArrayInputStream(imgBytes)));
                    } catch (Exception e) { imageView.setImage(null); }
                } else {
                    imageView.setImage(null);
                }
                setGraphic(content);
            }
        }
        private void handleEdit() {
            RestaurantItem item = getItem();
            if (item == null) return;
            if (onEditRestaurant != null) {
                onEditRestaurant.accept(item);
            }
        }
        private void handleDelete() {
            RestaurantItem item = getItem();
            if (item == null) return;
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this restaurant and all its menus/items?", ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText("Confirm Delete");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    new Thread(() -> {
                        try {
                            java.net.URL url = new java.net.URL("http://localhost:8000/restaurants/" + item.id);
                            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("DELETE");
                            conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
                            int code = conn.getResponseCode();
                            if (code == 200) {
                                Platform.runLater(() -> {
                                    getListView().getItems().remove(item);
                                    messageLabel.setText("Restaurant deleted.");
                                });
                            } else {
                                Platform.runLater(() -> messageLabel.setText("Failed to delete restaurant."));
                            }
                        } catch (Exception ex) {
                            Platform.runLater(() -> messageLabel.setText("Error: " + ex.getMessage()));
                        }
                    }).start();
                }
            });
        }
    }
} 