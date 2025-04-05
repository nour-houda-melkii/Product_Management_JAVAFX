package controllers;

import Services.DataPersistenceService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.util.List;

public class DataHistoryController {
    @FXML
    private ListView<String> historyListView;

    @FXML
    private TextArea detailsTextArea;

    @FXML
    private Label titleLabel;

    @FXML
    private Button closeButton;

    private DataPersistenceService dataPersistenceService;

    @FXML
    public void initialize() {
        dataPersistenceService = new DataPersistenceService();
        loadHistoryData();

        // Set up selection event for the list view
        historyListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        detailsTextArea.setText(newValue);
                    }
                }
        );

        // Set up close button
        closeButton.setOnAction(event -> {
            ((Stage) closeButton.getScene().getWindow()).close();
        });
    }

    private void loadHistoryData() {
        List<String> historyEntries = dataPersistenceService.getAllHistoryEntries();
        ObservableList<String> observableEntries = FXCollections.observableArrayList(historyEntries);
        historyListView.setItems(observableEntries);

        titleLabel.setText("Data History - " + historyEntries.size() + " entries");
    }
}