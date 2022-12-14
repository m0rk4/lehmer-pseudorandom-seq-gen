package com.morka.pseudorandomseqgen.controller;

import com.morka.pseudorandomseqgen.dto.GeneratorDistributionDto;
import com.morka.pseudorandomseqgen.dto.GeneratorMathematicalExpectationDto;
import com.morka.pseudorandomseqgen.service.LehmerPseudoRandomSequenceGenerator;
import com.morka.pseudorandomseqgen.service.PseudoRandomGeneratorBuilder;
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

import static com.morka.pseudorandomseqgen.controller.NumericTextFormatterFactory.createNumericTextFormatter;
import static java.util.Collections.singletonList;

public class MainController {

    private final ExecutorService threadPool;

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

    @FXML
    private Label mathExpectation;

    @FXML
    private Label standardDeviation;

    @FXML
    private Label variance;

    public MainController(ExecutorService threadPool, PseudoRandomGeneratorFacade facade) {
        this.threadPool = threadPool;
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

        PseudoRandomGeneratorBuilder originalBuilder = new LehmerPseudoRandomSequenceGenerator.Builder()
                .mod(mod)
                .coefficient(coefficient)
                .seed(seed);

        final int iterationsCount = 5 * 1_000_000;
        final int intervalsCount = 20;

        Task<PeriodLengthAndValue> periodLengthAndValueTask = TaskFactory.create(() -> {
            long targetValue = facade.getLastValueAfterIterations(originalBuilder.build(), iterationsCount);
            long periodLength = facade.getPeriodLength(originalBuilder.build(), targetValue);
            long periodValue = facade.getLastValueAfterIterations(originalBuilder.build(), periodLength);
            return new PeriodLengthAndValue(periodLength, periodValue);
        });
        periodLengthAndValueTask.setOnSucceeded(e -> {
            PeriodLengthAndValue periodLengthAndValue = (PeriodLengthAndValue) e.getSource().getValue();
            PseudoRandomGeneratorBuilder builderWithPeriodSeed = new LehmerPseudoRandomSequenceGenerator.Builder()
                    .mod(mod)
                    .coefficient(coefficient)
                    .seed(periodLengthAndValue.value());

            calculateAndUpdatePeriodLength(originalBuilder, periodLengthAndValue.length(), builderWithPeriodSeed);
            calculateAndUpdateEstimateOfUniformity(originalBuilder, iterationsCount);
            calculateAndUpdateGeneratorDistribution(originalBuilder, iterationsCount, intervalsCount);
            calculateAndUpdateSeriesStatistics(originalBuilder, iterationsCount);
        });
        threadPool.submit(periodLengthAndValueTask);
    }

    private void calculateAndUpdateSeriesStatistics(PseudoRandomGeneratorBuilder originalBuilder, int iterationsCount) {
        Task<SeriesCharacteristics> seriesCharacteristicsTask = TaskFactory.create(
                () -> calculateSeriesCharacteristics(originalBuilder, iterationsCount));
        seriesCharacteristicsTask.setOnSucceeded(e ->
                fillSeriesCharacteristics((SeriesCharacteristics) e.getSource().getValue()));
        threadPool.submit(seriesCharacteristicsTask);
    }

    private void calculateAndUpdateGeneratorDistribution(PseudoRandomGeneratorBuilder originalBuilder,
                                                         int iterationsCount,
                                                         int intervalsCount) {
        Task<GeneratorDistributionDto> generatorDistributionTask = TaskFactory.create(() ->
                facade.getGeneratorDistributionInformation(originalBuilder.build(), iterationsCount, intervalsCount));
        generatorDistributionTask.setOnSucceeded(e -> fillChart((GeneratorDistributionDto) e.getSource().getValue()));
        threadPool.submit(generatorDistributionTask);
    }

    private void calculateAndUpdateEstimateOfUniformity(PseudoRandomGeneratorBuilder originalBuilder,
                                                        int iterationsCount) {
        Task<Double> estimateOfUniformityTask = TaskFactory.create(() ->
                facade.getIndirectEstimateOfUniformity(originalBuilder.build(), iterationsCount));
        estimateOfUniformityTask.setOnSucceeded(e -> fillEstimateOfUniformity((double) e.getSource().getValue()));
        threadPool.submit(estimateOfUniformityTask);
    }

    private void calculateAndUpdatePeriodLength(PseudoRandomGeneratorBuilder originalBuilder,
                                                long periodLength,
                                                PseudoRandomGeneratorBuilder builderWithPeriodSeed) {
        CompletableFuture<Long> originalStartIndexTask = CompletableFuture.supplyAsync(
                () -> facade.getTailLengthInAperiodicSequence(originalBuilder, periodLength), threadPool);
        CompletableFuture<Long> candidateStartIndexTask = CompletableFuture.supplyAsync(
                () -> facade.getTailLengthInAperiodicSequence(builderWithPeriodSeed, periodLength), threadPool);
        CompletableFuture.allOf(originalStartIndexTask, candidateStartIndexTask).thenAccept(__ -> {
            try {
                long originalStartIndex = originalStartIndexTask.get();
                long candidateStartIndex = candidateStartIndexTask.get();
                Platform.runLater(() -> fillPeriodsInformation(periodLength, originalStartIndex, candidateStartIndex));
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private SeriesCharacteristics calculateSeriesCharacteristics(PseudoRandomGeneratorBuilder originalBuilder,
                                                                 int iterationsCount) {
        GeneratorMathematicalExpectationDto expectationDto =
                facade.getMathematicalExpectation(originalBuilder.build(), iterationsCount);
        double variance = facade.getVariance(expectationDto);
        double standardDeviation = facade.getStandardDeviation(variance);
        return new SeriesCharacteristics(expectationDto.expectation(), variance, standardDeviation);
    }

    private void fillSeriesCharacteristics(SeriesCharacteristics seriesCharacteristics) {
        mathExpectation.setText(String.valueOf(seriesCharacteristics.mathExpectation()));
        variance.setText(String.valueOf(seriesCharacteristics.variance()));
        standardDeviation.setText(String.valueOf(seriesCharacteristics.standardDeviation()));
    }

    private void fillEstimateOfUniformity(double estimateValue) {
        estimationOfUniformity.setText(String.valueOf(estimateValue));
    }

    private void fillPeriodsInformation(long periodLength, long originalStartIndexTask, long candidateStartIndex) {
        this.periodLength.setText(String.valueOf(periodLength));
        this.aperiodicLength.setText(
                "min(%d, %d) + %d".formatted(originalStartIndexTask, candidateStartIndex, periodLength)
        );
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

    private record SeriesCharacteristics(double mathExpectation, double variance, double standardDeviation) {
    }

    private record PeriodLengthAndValue(long length, long value) {
    }
}
