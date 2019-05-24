package com.wiseninja;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import org.apache.commons.cli.*;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        String inputFilePath = "";
        String outputFilePath = "";
        int windowSize = 0;

        Options options = new Options();
        options.addOption("input_file", true, "path to the input file");
        options.addOption("window_size", true, "time window size");

        CommandLineParser cmdParser = new DefaultParser();
        try {
            CommandLine cmd = cmdParser.parse( options, args);
            inputFilePath = cmd.getOptionValue("input_file");
            windowSize = Integer.parseInt(cmd.getOptionValue("window_size"));
            outputFilePath = new File(inputFilePath).getParentFile().getPath();

        } catch (ParseException e) {
            e.printStackTrace();
        }

        Gson parser = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext) ->
                LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"))).create();


        ArrayList<Event> events = getEventsFromFile(inputFilePath, parser);

        //establishing start/end time borders for calculation
        LocalDateTime startTime = events.get(0).timestamp.minusSeconds(events.get(0).timestamp.getSecond());
        LocalDateTime endTime = events.get(events.size() - 1).timestamp.plusSeconds(60 - events.get(events.size() - 1).timestamp.getSecond());
        LocalDateTime currentTime = startTime;
        PreciseTimeMovingAverage avgCalculator = new PreciseTimeMovingAverage(Duration.ofMinutes(windowSize));

        //adding the total samples for the moving average
        for (Event event : events)
        {
            avgCalculator.AddSample(event.duration, event.timestamp);
        }

        ArrayList<Result> results = calculateMovingAverages(endTime, currentTime, avgCalculator);
        writeAveragesToFile(parser, results, outputFilePath);
    }

    private static ArrayList<Event> getEventsFromFile(String inputFilePath, Gson parser) {
        ArrayList<Event> events = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(inputFilePath))) {
            while (scanner.hasNextLine()) {
                Event event = parser.fromJson(scanner.nextLine(), Event.class);
                events.add(event);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return events;
    }

    private static void writeAveragesToFile(Gson parser, ArrayList<Result> results, String outputDir) {
        File f = new File(outputDir +"\\analysis_results.json");
        if(f.exists()){
            f.delete();
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try(BufferedWriter writer= new BufferedWriter(new FileWriter(outputDir +"\\analysis_results.json", true))) {
            for (Result result : results){
                String jsonResult = parser.toJson(result, Result.class);
                writer.append(jsonResult);
                writer.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<Result> calculateMovingAverages(LocalDateTime endTime, LocalDateTime currentTime, PreciseTimeMovingAverage avg) {
        ArrayList<Result> results = new ArrayList<>();
        while (currentTime.isBefore(endTime) || currentTime.isEqual(endTime)) {
            double average = avg.CalculateAverage(currentTime);
            Result result = new Result();
            result.date = currentTime.toString();
            result.average_delivery_time = average;
            results.add(result);
            currentTime = currentTime.plusMinutes(1);
        }
        return results;
    }
}
