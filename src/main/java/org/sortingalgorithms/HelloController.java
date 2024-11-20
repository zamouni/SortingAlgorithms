package org.sortingalgorithms;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;

import static org.sortingalgorithms.SaveImage.saveChartAsImage;
import static org.sortingalgorithms.SortingAlgorithms.*;

public class HelloController implements Initializable {

    @FXML
    private ComboBox<String> AlgorithmComboBox;

    @FXML
    private BarChart<String, Number> AllAlgorithmsChart;

    @FXML
    private ComboBox<Integer> InputSizeComboBox;

    @FXML
    private LineChart<Number, Number> SortingTime;

    @FXML
    private VBox ProgressContainer;  // VBox for progress bars

    // To store the progress bar and label for each algorithm
    private Map<String, ProgressBar> progressBars = new HashMap<>();
    private Map<String, Label> labels = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AlgorithmComboBox.setItems(FXCollections.observableArrayList(
                "Selection Sort", "Bubble Sort", "Insertion Sort", "Merge Sort", "Quick Sort", "Heap Sort"
        ));

        InputSizeComboBox.setItems(FXCollections.observableArrayList(10, 100, 1000, 10000, 100000, 1000000));

        initializeLineChart();
        initializeBarChart();
    }

    private void initializeLineChart() {
        SortingTime.setTitle("Sorting Time Increase with Input Size");
        SortingTime.getXAxis().setLabel("Input Size");
        SortingTime.getYAxis().setLabel("Time (ms)");
    }

    private void initializeBarChart() {
        AllAlgorithmsChart.setTitle("Comparison of Algorithms by Sorting Time");
        AllAlgorithmsChart.getXAxis().setLabel("Algorithm");
        AllAlgorithmsChart.getYAxis().setLabel("Time (ms)");
    }

    @FXML
    private void updateLineChart() {
        if (AllAlgorithmsChart.isVisible()){
            ProgressContainer.getChildren().clear();
            progressBars.clear();
            labels.clear();
        }

        SortingTime.setVisible(true);
        AllAlgorithmsChart.setVisible(false);

        String selectedAlgorithm = AlgorithmComboBox.getValue();
        if (selectedAlgorithm == null) return;

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(selectedAlgorithm);

        // Add progress indicator
        addProgressIndicator(selectedAlgorithm);

        int totalSizes = InputSizeComboBox.getItems().size();
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                int completedSizes = 0;
                for (Integer size : InputSizeComboBox.getItems()) {
                    double time = runAlgorithm(selectedAlgorithm, size);
                    Platform.runLater(() -> series.getData().add(new XYChart.Data<>(size, time)));

                    completedSizes++;
                    double progress = completedSizes / (double) totalSizes;
                    UpdateProgress(selectedAlgorithm, progress);
                }
                return null;
            }
        };

        SortingTime.getData().add(series);
        new Thread(task).start();
    }

    @FXML
    private void updateBarChart() {
        SortingTime.setVisible(false);
        AllAlgorithmsChart.setVisible(true);

        ProgressContainer.getChildren().clear();
        progressBars.clear();
        labels.clear();

        Integer selectedSize = InputSizeComboBox.getValue();
        if (selectedSize == null) return;

        AllAlgorithmsChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Sorting Time for Array Size: " + selectedSize);

        for (String algorithm : AlgorithmComboBox.getItems()) {
            addProgressIndicator(algorithm);

            Task<Double> task = createSortingTask(algorithm, selectedSize, time -> {
                Platform.runLater(() -> series.getData().add(new XYChart.Data<>(algorithm, time)));
                UpdateProgress(algorithm, 1.0);  // Set progress to complete
            });

            new Thread(task).start();
        }

        AllAlgorithmsChart.getData().add(series);
    }

    // Add progress bar only once for each algorithm
    private void addProgressIndicator(String algorithm) {
        if (!progressBars.containsKey(algorithm)) {
            ProgressBar progressBar = new ProgressBar(0);
            progressBars.put(algorithm, progressBar);

            Label label = new Label(algorithm + " Progress");
            labels.put(algorithm, label);

            Platform.runLater(() -> {
                ProgressContainer.getChildren().addAll(label, progressBar);
            });
        }
    }

    // Update progress for the specified algorithm
    private void UpdateProgress(String algorithm, double progress) {
        ProgressBar progressBar = progressBars.get(algorithm);
        if (progressBar != null) {
            Platform.runLater(() -> progressBar.setProgress(progress));
        }
    }

    // Create the sorting task to run the algorithm
    private Task<Double> createSortingTask(String algorithm, int size, SortingTimeCallback callback) {
        return new Task<>() {
            @Override
            protected Double call() {
                double time = runAlgorithm(algorithm, size);
                callback.onSortingTimeCalculated(time);
                return time;
            }
        };
    }

    // Run the selected sorting algorithm
    private double runAlgorithm(String algorithm, int size) {
        int[] array = generateRandomArray(size);
        long startTime = System.nanoTime();

        switch (algorithm) {
            case "Selection Sort" -> selectionSort(array);
            case "Bubble Sort" -> bubbleSort(array);
            case "Insertion Sort" -> insertionSort(array);
            case "Merge Sort" -> mergeSort(array);
            case "Quick Sort" -> quickSort(array, 0, array.length - 1);
            case "Heap Sort" -> heapSort(array);
            default -> throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
        }

        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000.0;  // Convert to milliseconds
    }

    private int[] generateRandomArray(int size) {
        int[] array = new int[size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(10000);
        }
        return array;
    }

    interface SortingTimeCallback {
        void onSortingTimeCalculated(double time);
    }

    @FXML
    private void clearCharts() {
        // Clear the LineChart data
        SortingTime.getData().clear(); // Clears all series in the LineChart

        // Clear the BarChart data
        AllAlgorithmsChart.getData().clear(); // Clears all series in the BarChart

        // Clear the progress VBox and reset the maps
        ProgressContainer.getChildren().clear(); // Remove progress bars and labels
        progressBars.clear(); // Clear the map of progress bars
        labels.clear(); // Clear the map of labels
    }

    @FXML
    private void captureChart() {
        // Determine which chart is visible (LineChart or BarChart)
        Node visibleChart = getVisibleChart();

        if (visibleChart != null) {
            // Capture the visible chart with PrinterJob
            saveChartAsImage(visibleChart);
        }
    }

    // Get the currently visible chart (LineChart or BarChart)
    private Node getVisibleChart() {
        if (SortingTime.isVisible()) {
            return SortingTime;
        } else if (AllAlgorithmsChart.isVisible()) {
            return AllAlgorithmsChart;
        }
        return null; // No visible chart
    }
}
