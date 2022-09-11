package com.morka.pseudorandomseqgen.controller;

import com.morka.pseudorandomseqgen.dto.GeneratorDistributionDto;
import com.morka.pseudorandomseqgen.service.LehmerPseudoRandomSequenceGenerator;
import com.morka.pseudorandomseqgen.service.PseudoRandomGeneratorFacade;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.morka.pseudorandomseqgen.controller.NumericTextFormatterFactory.createNumericTextFormatter;
import static java.lang.Math.min;
import static java.util.Collections.singletonList;

public class MainController {
    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(4);

    private final PseudoRandomGeneratorFacade facade;

    @FXML
    private BarChart<String, Number> barChart;

    @FXML
    private TextField coefficientField;

    @FXML
    private TextField modField;

    @FXML
    private TextField seedField;

    @FXML
    private Label periodLength;

    @FXML
    private Label aperiodicLength;

    @FXML
    private Label estimationOfUniformity;

    public MainController(PseudoRandomGeneratorFacade facade) {
        this.facade = facade;
    }

    @FXML
    void initialize() {
        modField.setTextFormatter(createNumericTextFormatter());
        coefficientField.setTextFormatter(createNumericTextFormatter());
        seedField.setTextFormatter(createNumericTextFormatter());
    }

    @FXML
    void onGenerateAction() {
        long mod = getControlValue(modField);
        long coefficient = getControlValue(coefficientField);
        long seed = getControlValue(seedField);

        LehmerPseudoRandomSequenceGenerator.Builder originalBuilder = new LehmerPseudoRandomSequenceGenerator.Builder()
                .mod(mod)
                .coefficient(coefficient)
                .seed(seed);

        final int iterationsCount = 5 * 1_000_000;
        final int intervalsCount = 20;

        long targetValue = facade.getLastValueAfterIterations(originalBuilder.build(), iterationsCount);
        long periodLength = facade.getPeriodLength(originalBuilder.build(), targetValue);
        long periodValue = facade.getLastValueAfterIterations(originalBuilder.build(), periodLength);

        LehmerPseudoRandomSequenceGenerator.Builder builderWithPeriodSeed = new LehmerPseudoRandomSequenceGenerator.Builder()
                .mod(mod)
                .coefficient(coefficient)
                .seed(periodValue);

        CompletableFuture<Long> originalStartIndexTask = CompletableFuture.supplyAsync(
                () -> facade.getTailLengthInAperiodicSequence(originalBuilder, periodLength), THREAD_POOL);
        CompletableFuture<Long> candidateStartIndexTask = CompletableFuture.supplyAsync(
                () -> facade.getTailLengthInAperiodicSequence(builderWithPeriodSeed, periodLength), THREAD_POOL);
        CompletableFuture.allOf(originalStartIndexTask, candidateStartIndexTask).thenAccept(__ -> {
            try {
                long aperiodicLength = min(originalStartIndexTask.get(), candidateStartIndexTask.get()) + periodLength;
                Platform.runLater(() -> fillPeriodsInformation(periodLength, aperiodicLength));
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });

        final Task<Double> estimateOfUniformityTask = TaskFactory.create(() ->
                facade.getIndirectEstimateOfUniformity(originalBuilder.build(), iterationsCount));
        estimateOfUniformityTask.setOnSucceeded(e -> fillEstimateOfUniformity((double) e.getSource().getValue()));

        final Task<GeneratorDistributionDto> generatorDistributionTask = TaskFactory.create(() ->
                facade.getGeneratorDistributionInformation(originalBuilder.build(), iterationsCount, intervalsCount));
        generatorDistributionTask.setOnSucceeded(e -> fillChart((GeneratorDistributionDto) e.getSource().getValue()));

        THREAD_POOL.submit(estimateOfUniformityTask);
        THREAD_POOL.submit(generatorDistributionTask);
    }

    private void fillEstimateOfUniformity(double estimateValue) {
        estimationOfUniformity.setText(String.valueOf(estimateValue));
    }

    private void fillPeriodsInformation(long periodLength, long aperiodicLength) {
        this.periodLength.setText(String.valueOf(periodLength));
        this.aperiodicLength.setText(String.valueOf(aperiodicLength));
    }

    private void fillChart(GeneratorDistributionDto dto) {
        XYChart.Series<String, Number> series = getChartSeries(dto);
        barChart.getData().setAll(singletonList(series));
    }

    private XYChart.Series<String, Number> getChartSeries(GeneratorDistributionDto dto) {
        int[] hits = dto.hits();
        double intervalLength = dto.intervalLength();
        int intervalsCount = dto.intervalsCount();
        int iterations = dto.iterations();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 0; i < intervalsCount; i++) {
            double start = i * intervalLength;
            double end = (i + 1) * intervalLength;
            series.getData()
                    .add(new XYChart.Data<>("%.3f - %.3f".formatted(start, end), (double) hits[i] / iterations));
        }
        return series;
    }

    private long getControlValue(TextField textField) {
        return (Long) textField.getTextFormatter().getValue();
    }
}
