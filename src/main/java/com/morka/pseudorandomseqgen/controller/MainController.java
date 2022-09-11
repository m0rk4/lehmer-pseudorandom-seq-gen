package com.morka.pseudorandomseqgen.controller;

import com.morka.pseudorandomseqgen.service.LehmerPseudoRandomSequenceGenerator;
import com.morka.pseudorandomseqgen.service.PseudoRandomGeneratorFacade;
import com.morka.pseudorandomseqgen.service.PseudoRandomSequenceGenerator;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TextField;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.morka.pseudorandomseqgen.controller.NumericTextFormatterFactory.createNumericTextFormatter;

public class MainController {

    private final PseudoRandomGeneratorFacade facade;

    @FXML
    private BarChart<String, Number> barChart;

    @FXML
    private TextField coefficientField;

    @FXML
    private TextField modField;

    @FXML
    private TextField seedField;

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

        long targetValue = facade.getLastAfterIterations(originalBuilder.build(), 5 * 1_000_000);
        long periodLength = facade.getPeriod(originalBuilder.build(), targetValue);
        long periodValue = facade.getLastAfterIterations(originalBuilder.build(), periodLength);

        LehmerPseudoRandomSequenceGenerator.Builder builderWithPeriodSeed = new LehmerPseudoRandomSequenceGenerator.Builder()
                .mod(mod)
                .coefficient(coefficient)
                .seed(periodValue);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Task<Long> originalStartIndexTask = TailCounterTaskFactory.create(originalBuilder, periodLength);
        Task<Long> candidateStartIndexTask = TailCounterTaskFactory.create(builderWithPeriodSeed, periodLength);
        executorService.submit(originalStartIndexTask);
        executorService.submit(candidateStartIndexTask);
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS))
                executorService.shutdownNow();
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

        try {
            long originalStartIndex = originalStartIndexTask.get();
            long candidateStartIndex = candidateStartIndexTask.get();
            long aperiodicSequenceLength = Math.min(originalStartIndex, candidateStartIndex) + periodLength;
            estimate(originalBuilder.build(), (int) aperiodicSequenceLength);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        draw(originalBuilder.build(), 2 * 1_000_000);
    }

    private void estimate(PseudoRandomSequenceGenerator generator, int iterations) {
        assert iterations % 2 == 0;

        double[] numbers = new double[iterations];
        for (int i = 0; i < iterations; i++)
            numbers[i] = generator.getNextDouble();

        int inCircle = 0;
        for (int i = 1; i < numbers.length; i += 2) {
            double sumOfSquares = numbers[i - 1] * numbers[i - 1] + numbers[i] * numbers[i];
            if (sumOfSquares < 1.0)
                inCircle++;
        }

        System.out.println(inCircle);
        System.out.println(2.0 * inCircle / numbers.length);
    }

    private void draw(PseudoRandomSequenceGenerator generator, int iterations) {
        double[] numbers = new double[iterations];
        for (int i = 0; i < numbers.length; i++)
            numbers[i] = generator.getNextDouble();

        Arrays.sort(numbers);

        final int intervalsCount = 20;
        final double maxDiff = numbers[numbers.length - 1] - numbers[0];
        final double intervalLength = maxDiff / intervalsCount;

        long[] hits = new long[20];
        for (double number : numbers) {
            for (int j = 0; j < intervalsCount; j++) {
                double start = j * intervalLength;
                double end = (j + 1) * intervalLength;
                if (number > start && number <= end) {
                    hits[j]++;
                    break;
                }
            }
        }

        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        for (int i = 0; i < intervalsCount; i++) {
            double start = i * intervalLength;
            double end = (i + 1) * intervalLength;
            series1.getData().add(new XYChart.Data<>("%.3f - %.3f".formatted(start, end), (double) hits[i] / iterations));
        }
        barChart.getData().setAll(Collections.singletonList(series1));
    }

    private long getControlValue(TextField textField) {
        return (Long) textField.getTextFormatter().getValue();
    }
}
